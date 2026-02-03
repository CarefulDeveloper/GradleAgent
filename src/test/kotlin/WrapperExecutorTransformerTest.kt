import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.tingy.agent.gradle.WrapperExecutorTransformer
import java.io.File

class WrapperExecutorTransformerTest {

    val classDir = File(System.getProperty("generate.class.dir"))

    init {
        classDir.mkdirs()
    }

    private val bWrapperExecutorV1: ByteArray
        get() = readClassBytes("WrapperExecutor.v1.class")

    private val bWrapperExecutorV2: ByteArray
        get() = readClassBytes("WrapperExecutor.v2.class")

    private val bWrapperExecutorV3: ByteArray
        get() = readClassBytes("WrapperExecutor.v3.class")

    @Test
    fun `transform wrapper v1`() {
        val bytes = WrapperExecutorTransformer.transform(bWrapperExecutorV1)
        assertNotNull(bytes)
        File(classDir, "WrapperExecutor.v1.class").writeBytes(bytes)
    }

    @Test
    fun `transform wrapper v2`() {
        val bytes = WrapperExecutorTransformer.transform(bWrapperExecutorV2)
        assertNotNull(bytes)
        File(classDir, "WrapperExecutor.v2.class").writeBytes(bytes)
    }

    @Test
    fun `transform wrapper v3`() {
        val bytes = WrapperExecutorTransformer.transform(bWrapperExecutorV3)
        assertNotNull(bytes)
        File(classDir, "WrapperExecutor.v3.class").writeBytes(bytes)
    }

    private fun readClassBytes(name: String) = ClassLoader.getSystemResourceAsStream(name)!!.readBytes()
}