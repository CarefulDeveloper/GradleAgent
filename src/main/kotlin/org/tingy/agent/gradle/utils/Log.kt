package org.tingy.agent.gradle.utils

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.jar.JarFile

private val LOG_TIME_FMT =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

private var logFile: File? = null
private var initFlag = false

private val CLASS = Class.forName("org.tingy.agent.gradle.utils.LogKt")

private fun getLogFile(): File? {
    if (initFlag) {
        return logFile
    }
    try {
        val jarFile = JarFile(File(CLASS.protectionDomain.codeSource.location.toURI()))
        logFile = File(jarFile.manifest.mainAttributes.getValue("Log-Path"))
    } catch (_: Throwable) {
    } finally {
        initFlag = true
    }
    return logFile
}

fun log(message: String) {
    val file = getLogFile() ?: return
    val time = LocalDateTime.now().format(LOG_TIME_FMT)
    val line = "[$time] $message\n"
    file.parentFile?.mkdirs()
    file.appendText(line, Charsets.UTF_8)
}