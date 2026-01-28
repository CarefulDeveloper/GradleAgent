package org.tingy.agent.gradle

import org.tingy.agent.gradle.utils.log
import java.util.regex.Pattern

private val pattern =
    Pattern.compile("^https?://.*\\.gradle\\.org/.*/gradle-(?<version>[0-9]+(\\.[0-9]+){1,2}(-(rc|milestone)-[0-9]+)?)-(?<type>all|bin|src).zip$")

object UrlReplacer {
    fun replace(url: String): String {
        val matcher = pattern.matcher(url)
        if (!matcher.matches()) {
            log("[replace] Distribution [$url] is not official url, skip")
            println("> Gradle Agent:\nDistribution [$url] is not official url, skip\n")
            return url
        }

        val version = matcher.group("version")
        val type = matcher.group("type")
        if (version == null || type == null) {
            log("[replace] Distribution [$url] match failed, skip")
            println("> Gradle Agent:\nDistribution [$url] match failed, skip\n")
            return url
        }

        val newUrl = String.format(System.getenv("GRADLE_DISTRIBUTION_URL_TEMPLATE"), version, type)
        log("[replace] Distribution [$url] is mirrored to [$url]")
        println("> Gradle Agent:\nDistribution [$url] is mirrored to [$url]\n")
        return newUrl
    }
}