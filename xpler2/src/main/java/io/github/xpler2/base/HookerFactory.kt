package io.github.xpler2.base

import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.params.UnhookParams
import java.lang.reflect.Member
import java.util.TreeMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object HookerStore {
    private val mHookers = ConcurrentHashMap<Member, HookerFactory>()

    val hookers: Map<Member, HookerFactory>
        get() = mHookers

    fun computeIfAbsent(member: Member, factory: HookerFactory): HookerFactory {
        return mHookers.computeIfAbsent(member) { factory }
    }

    fun remove(member: Member): HookerFactory? {
        return mHookers.remove(member)
    }

    fun get(member: Member): HookerFactory? {
        return mHookers[member]
    }
}

class HookerFactory(
    private val target: Member,
    private val factory: (priority: Int) -> UnhookParams,
) {
    private data class Bucket(
        val priority: Int,
        val entries: CopyOnWriteArrayList<HookerCallback>,
        val unhooker: UnhookParams,
    )

    private class InvocationState(
        private val beforeQueue: ArrayDeque<Array<HookerCallback>>,
    ) {
        private val executedStack = ArrayDeque<Array<HookerCallback>>()
        var beforeActive: Int = 0
        var afterActive: Int = 0

        fun consumeNextBeforeSnapshot(): Array<HookerCallback>? = beforeQueue.removeFirstOrNull()

        fun pushExecution(snapshot: Array<HookerCallback>) {
            executedStack.addLast(snapshot)
        }

        fun popExecution(): Array<HookerCallback>? = if (executedStack.isEmpty()) null else executedStack.removeLast()

        fun markSkip() {
            beforeQueue.clear()
        }

        fun shouldRelease(): Boolean = beforeQueue.isEmpty() && executedStack.isEmpty() && beforeActive == 0 && afterActive == 0
    }

    private val buckets = TreeMap<Int, Bucket>()
    private val lock = Any()
    private val invocationStates = ThreadLocal<ArrayDeque<InvocationState>>()

    fun register(
        priority: Int,
        callback: HookerCallback,
        onEmpty: (Int) -> Unit,
    ): UnhookParams {
        synchronized(lock) {
            buckets[priority]?.let { bucket ->
                bucket.entries.add(callback)

                return UnhookParams(
                    mOrigin = { target },
                    mUnhook = {
                        val notifyPriority = synchronized(lock) {
                            val current = buckets[priority] ?: return@synchronized null
                            current.entries.remove(callback)
                            if (current.entries.isEmpty()) {
                                buckets.remove(priority)
                                current.unhooker.unhook()
                                priority
                            } else {
                                null
                            }
                        }
                        if (notifyPriority != null) {
                            onEmpty(notifyPriority)
                        }
                    },
                )
            }

            buckets[priority] = Bucket(
                priority = priority,
                entries = CopyOnWriteArrayList<HookerCallback>().apply { add(callback) },
                unhooker = factory(priority),
            )
        }

        return UnhookParams(
            mOrigin = { target },
            mUnhook = {
                val notifyPriority = synchronized(lock) {
                    val current = buckets[priority] ?: return@synchronized null
                    current.entries.remove(callback)
                    if (current.entries.isEmpty()) {
                        buckets.remove(priority)
                        current.unhooker.unhook()
                        priority
                    } else {
                        null
                    }
                }
                if (notifyPriority != null) {
                    onEmpty(notifyPriority)
                }
            },
        )
    }

    fun dispatchBefore(params: BeforeParams) {
        val state = obtainStateForBefore() ?: return
        val snapshot = state.consumeNextBeforeSnapshot() ?: run {
            releaseStateIfDone(state)
            return
        }

        val executedSnapshot = mutableListOf<HookerCallback>()
        state.beforeActive++
        try {
            for (call in snapshot) {
                executedSnapshot.add(call)
                try {
                    call.onBefore(params)
                } catch (_: Throwable) {
                    // Ignore callback errors to keep the chain alive.
                }
                if (params.isSkipped) {
                    state.markSkip()
                    break
                }
            }
        } finally {
            state.beforeActive--
            state.pushExecution(executedSnapshot.toTypedArray())
            releaseStateIfDone(state)
        }
    }

    fun dispatchAfter(params: AfterParams) {
        val state = obtainStateForAfter() ?: return
        val execution = state.popExecution() ?: run {
            releaseStateIfDone(state)
            return
        }

        if (execution.isEmpty()) {
            releaseStateIfDone(state)
            return
        }

        if (params.isSkipped) {
            state.markSkip()
        }
        state.afterActive++
        try {
            for (index in execution.indices.reversed()) {
                val after = execution[index]
                val lastResult = params.result
                val lastThrowable = params.throwable
                try {
                    after.onAfter(params)
                } catch (_: Throwable) {
                    if (lastThrowable == null) {
                        params.result = lastResult
                    } else {
                        params.throwable = lastThrowable
                    }
                }
            }
        } finally {
            state.afterActive--
            releaseStateIfDone(state)
        }
    }

    fun isEmpty(): Boolean = synchronized(lock) { buckets.isEmpty() }

    private fun obtainStateForBefore(): InvocationState? {
        val stack = invocationStates.get()
        val current = stack?.lastOrNull()
        if (current == null || current.beforeActive > 0 || current.afterActive > 0) {
            val newState = createState() ?: return null
            val targetStack = stack ?: ArrayDeque<InvocationState>().also { invocationStates.set(it) }
            targetStack.addLast(newState)
            return newState
        }
        if (current.shouldRelease()) {
            releaseState(current)
            return obtainStateForBefore()
        }
        return current
    }

    private fun obtainStateForAfter(): InvocationState? {
        val stack = invocationStates.get() ?: return null
        val current = stack.lastOrNull() ?: return null
        if (current.shouldRelease()) {
            releaseState(current)
            return null
        }
        return current
    }

    private fun createState(): InvocationState? = synchronized(lock) {
        if (buckets.isEmpty()) return null
        val snapshots = ArrayDeque<Array<HookerCallback>>()
        for (bucket in buckets.descendingMap().values) {
            val entries = bucket.entries.toTypedArray()
            if (entries.isNotEmpty()) {
                snapshots.addLast(entries)
            }
        }
        if (snapshots.isEmpty()) return null
        InvocationState(snapshots)
    }

    private fun releaseState(state: InvocationState) {
        val stack = invocationStates.get() ?: return
        val iterator = stack.iterator()
        while (iterator.hasNext()) {
            if (iterator.next() === state) {
                iterator.remove()
                break
            }
        }
        if (stack.isEmpty()) {
            invocationStates.remove()
        }
    }

    private fun releaseStateIfDone(state: InvocationState) {
        if (state.shouldRelease()) {
            releaseState(state)
        }
    }
}