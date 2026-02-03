package org.tingy.agent.gradle

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

object WrapperExecutorTransformer {

    fun transform(bytes: ByteArray?): ByteArray? {
        if (bytes == null) return null

        val cr = ClassReader(bytes)
        val cw = ClassWriter(cr, ClassWriter.COMPUTE_FRAMES)
        val cv = object : ClassVisitor(Opcodes.ASM9, cw) {

            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {
                val mv = super.visitMethod(
                    access, name, descriptor, signature, exceptions
                )

                if (name != "readDistroUrl") return mv

                return ReplaceReturnValueVisitor(mv)
            }
        }

        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }


    /**
     * Replace return value of org/tingy/agent/gradle/WrapperExecutor.getProperty
     *
     * key instruction:
     * ```
     * INVOKESPECIAL org/tingy/agent/gradle/WrapperExecutor.getProperty (Ljava/lang/String;)Ljava/lang/String;
     * INVOKEVIRTUAL org/tingy/agent/gradle/WrapperExecutor.getProperty (Ljava/lang/String;Ljava/lang/String;Z)Ljava/lang/String;
     * ```
     * @param targetOrder The order of the target instruction should be replaced, start from 1.
     */
    private class ReplaceReturnValueVisitor(
        mv: MethodVisitor,
        private val targetOrder: Int = 1
    ) : MethodVisitor(Opcodes.ASM9, mv) {

        private var count = 0

        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            // stack [... String]

            if ("org/gradle/wrapper/WrapperExecutor" == owner
                && "getProperty" == name
                && TARGET_OPCODES.contains(opcode)
                && TARGET_DESCRIPTORS.contains(descriptor)
            ) {
                if (++count == targetOrder) {
                    super.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "org/tingy/agent/gradle/UrlReplacer",
                        "INSTANCE",
                        "Lorg/tingy/agent/gradle/UrlReplacer;"
                    )
                    // stack [... String, UrlReplacer]
                    super.visitInsn(Opcodes.SWAP)
                    // stack [... UrlReplacer, String]
                    super.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "org/tingy/agent/gradle/UrlReplacer",
                        "replace",
                        "(Ljava/lang/String;)Ljava/lang/String;",
                        false
                    )
                    // stack [... String]
                }
            }
        }

        companion object {
            private val TARGET_OPCODES = intArrayOf(Opcodes.INVOKESPECIAL, Opcodes.INVOKEVIRTUAL)
            private val TARGET_DESCRIPTORS = listOf(
                "(Ljava/lang/String;)Ljava/lang/String;",
                "(Ljava/lang/String;Ljava/lang/String;Z)Ljava/lang/String;",
            )
        }
    }
}
