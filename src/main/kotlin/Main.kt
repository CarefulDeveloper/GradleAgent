import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import java.lang.instrument.Instrumentation

private const val TARGET_CLASS_NAME = "org.gradle.wrapper.WrapperExecutor"

fun main() {
    println("You should use Java Agent argument to run this jar!")
}

fun premain(args: String?, instrumentation: Instrumentation) {
    if (args == null) {
        println("> Gradle Agent:\nMirror url template is null\n")
        return
    }

    AgentBuilder.Default().type(ElementMatchers.named(TARGET_CLASS_NAME)).transform { builder, _, _, _, _ ->
        builder.method(ElementMatchers.named("readDistroUrl"))
            .intercept(MethodDelegation.to(WrapperExecutorInterceptor(args)))
    }.installOn(instrumentation)
}