package io.github.xpler2

import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.params.UnhookParams
import java.lang.reflect.Member
import java.util.TreeMap
import java.util.concurrent.ConcurrentHashMap

object HookerStore {
    private val mHookers = ConcurrentHashMap<Member, HookerFactory>()

    val hookers: Map<Member, HookerFactory>
        get() = mHookers

    fun computeIfAbsent(member: Member, factory: () -> HookerFactory): HookerFactory {
        return mHookers.computeIfAbsent(member) { factory() }
    }

    fun remove(member: Member): HookerFactory? {
        return mHookers.remove(member)
    }

    fun remove(member: Member, factory: HookerFactory): Boolean {
        return mHookers.remove(member, factory)
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
        val entries: MutableList<HookerCallback>,
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
    private var retired = false

    fun register(
        priority: Int,
        callback: HookerCallback,
        onFactoryEmpty: () -> Unit,
    ): UnhookParams? = synchronized(lock) {
        if (retired) {
            return@synchronized null
        }

        buckets[priority]?.let { bucket ->
            bucket.entries.add(callback)
            return@synchronized buildUnhookParams(priority, callback, onFactoryEmpty)
        }

        buckets[priority] = Bucket(
            entries = arrayListOf(callback),
            unhooker = factory(priority),
        )
        buildUnhookParams(priority, callback, onFactoryEmpty)
    }

    private fun buildUnhookParams(
        priority: Int,
        callback: HookerCallback,
        onFactoryEmpty: () -> Unit,
    ): UnhookParams {
        return UnhookParams(
            mOrigin = { target },
            mUnhook = { unregister(priority, callback, onFactoryEmpty) },
        )
    }

    private fun unregister(
        priority: Int,
        callback: HookerCallback,
        onFactoryEmpty: () -> Unit,
    ) {
        val shouldReleaseFactory = synchronized(lock) {
            val current = buckets[priority] ?: return@synchronized false
            if (!current.entries.remove(callback)) {
                return@synchronized false
            }

            if (current.entries.isNotEmpty()) {
                return@synchronized false
            }

            buckets.remove(priority)
            current.unhooker.unhook()

            if (buckets.isEmpty()) {
                retired = true
                true
            } else {
                false
            }
        }

        if (shouldReleaseFactory) {
            onFactoryEmpty()
        }
    }

    fun dispatchBefore(params: BeforeParams) {
        val state = obtainStateForBefore() ?: return
        val snapshot = state.consumeNextBeforeSnapshot() ?: run {
            releaseStateIfDone(state)
            return
        }

        var executedCount = 0
        state.beforeActive++
        try {
            for (call in snapshot) {
                executedCount++
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
            state.pushExecution(
                if (executedCount == snapshot.size) {
                    snapshot
                } else if (executedCount == 0) {
                    emptyArray()
                } else {
                    snapshot.copyOfRange(0, executedCount)
                }
            )
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
                    params.result = lastResult
                    params.throwable = lastThrowable
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
