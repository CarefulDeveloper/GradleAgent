import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import java.lang.instrument.Instrumentation

private const val TARGET_CLASS_NAME = "org.gradle.wrapper.WrapperExecutor"

fun main() {
    println("You should use Java Agent argument to run this jar!")
}

fun premain(args: String?, instrumentation: Instrumentation) {
    val mirrorUrlTemplate = System.getenv("GRADLE_DISTRIBUTION_URL_TEMPLATE")
    if (mirrorUrlTemplate == null) {
        println(
            "> Gradle Agent:\nThe environment variable GRADLE_DISTRIBUTION_URL_TEMPLATE is not specified. " +
                    "Please set it to a valid URL template. " +
                    "For example: https://mirror.host.com/gradle/gradle-%1\$s-%2\$s.zip. \n"
        )
        return
    }

    AgentBuilder.Default().type(ElementMatchers.named(TARGET_CLASS_NAME)).transform { builder, _, _, _, _ ->
        builder.method(ElementMatchers.named("readDistroUrl"))
            .intercept(MethodDelegation.to(WrapperExecutorInterceptor(mirrorUrlTemplate)))
    }.installOn(instrumentation)
}