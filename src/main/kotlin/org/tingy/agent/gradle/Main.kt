package org.tingy.agent.gradle

import org.tingy.agent.gradle.utils.log
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

fun main() {
    log("[init] call main method")
    println("You should use Java Agent argument to run this jar!")
}

fun premain(args: String?, instrumentation: Instrumentation) {
    val mirrorUrlTemplate = System.getenv("GRADLE_DISTRIBUTION_URL_TEMPLATE")
    if (mirrorUrlTemplate == null) {
        log("[init] GRADLE_DISTRIBUTION_URL_TEMPLATE is not specified")
        println(
            "> Gradle Agent:\nThe environment variable GRADLE_DISTRIBUTION_URL_TEMPLATE is not specified. " +
                    "Please set it to a valid URL template. " +
                    "For example: https://mirror.host.com/gradle/gradle-%1\$s-%2\$s.zip. \n"
        )
        return
    }

    instrumentation.addTransformer(object : ClassFileTransformer {
        override fun transform(
            loader: ClassLoader?,
            className: String?,
            classBeingRedefined: Class<*>?,
            protectionDomain: ProtectionDomain?,
            classfileBuffer: ByteArray?
        ): ByteArray? {
            if (className == "org/gradle/wrapper/WrapperExecutor") {
                log("[transform] do transform $className")
                return WrapperExecutorTransformer.transform(classfileBuffer)
            }
            return null
        }
    })
    log("[init] add transformer done")
}