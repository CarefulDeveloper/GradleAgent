import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.implementation.bind.annotation.SuperCall
import java.lang.reflect.Method
import java.net.URI
import java.util.concurrent.Callable

private val DISTRIBUTION_REGEX =
    Regex("gradle-(?<version>[0-9]+\\.[0-9]+(-(rc|milestone)-[0-9]+)?)-(?<type>all|bin|src).zip$")

class WrapperExecutorInterceptor(
    private val mirrorTemplateUrl: String
) {

    @RuntimeType
    fun intercept(
        @Origin method: Method,
        @SuperCall callable: Callable<*>
    ): Any {
        try {
            val origin = callable.call()
            val originStr = origin.toString()
            val mirror = getMirrorUrl(originStr)
            return if(mirror == null) {
                println("> Gradle Agent:\nDistribution [$originStr] is not recognized\n")
                origin
            } else {
                println("> Gradle Agent:\nDistribution [$originStr] is mirrored to [$mirror]\n")
                if(origin.javaClass == URI::class.java) {
                    URI.create(mirror)
                } else {
                    mirror
                }
            }
        } catch (e: java.lang.Exception) {
            throw RuntimeException(e)
        }
    }

    private fun getMirrorUrl(origin: String): String? {
        val result = DISTRIBUTION_REGEX.find(origin) ?: return null
        return mirrorTemplateUrl.format(
            result.groups["version"]?.value ?: return null,
            result.groups["type"]?.value ?: return null
        )
    }
}
