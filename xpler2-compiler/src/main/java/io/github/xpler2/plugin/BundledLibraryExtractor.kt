package io.github.xpler2.plugin

import java.io.File
import java.security.MessageDigest

internal object BundledLibraryExtractor {
    fun syncResources(resourcePaths: List<String>, outputDirectory: File) {
        outputDirectory.mkdirs()
        clearStaleFiles(outputDirectory, resourcePaths.map(::resolveOutputFileName))

        resourcePaths.forEach { resourcePath ->
            val outputFile = outputDirectory.resolve(resolveOutputFileName(resourcePath))
            writeResourceIfChanged(resourcePath, outputFile)
        }
    }

    fun resourceHash(resourcePath: String): String {
        return sha256Hex(readResourceBytes(resourcePath))
    }

    fun resolveOutputFileName(resourcePath: String): String {
        return resourcePath.substringAfterLast('/')
    }

    private fun clearStaleFiles(outputDirectory: File, expectedFileNames: List<String>) {
        outputDirectory
            .listFiles()
            ?.filter { it.isFile && it.name !in expectedFileNames }
            ?.forEach(File::delete)
    }

    private fun writeResourceIfChanged(resourcePath: String, outputFile: File) {
        val resourceBytes = readResourceBytes(resourcePath)
        if (outputFile.exists() && outputFile.readBytes().contentEquals(resourceBytes)) {
            return
        }

        outputFile.writeBytes(resourceBytes)
    }

    private fun readResourceBytes(resourcePath: String): ByteArray {
        return BundledLibraryExtractor::class.java.classLoader
            .getResourceAsStream(resourcePath)
            ?.use { it.readBytes() }
            ?: throw RuntimeException("Missing bundled library resource: $resourcePath")
    }

    private fun sha256Hex(bytes: ByteArray): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { byte -> "%02x".format(byte) }
    }
}
