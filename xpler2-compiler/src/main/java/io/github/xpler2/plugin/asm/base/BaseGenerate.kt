package io.github.xpler2.plugin.asm.base

import java.io.File

abstract class BaseGenerate {
    abstract fun reset()

    abstract fun finish(path: File): Set<String>

    protected abstract fun generateByteCode(): ByteArray
}