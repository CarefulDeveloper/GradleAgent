package org.tingy.agent.gradle

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

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

                return object : AdviceAdapter(ASM9, mv, access, name, descriptor) {

                    override fun onMethodExit(opcode: Int) {
                        if (opcode != ARETURN) return

                        when (descriptor) {

                            "()Ljava/lang/String;" -> {
                                // stack: [String]
                                visitFieldInsn(
                                    GETSTATIC,
                                    "org/tingy/agent/gradle/UrlReplacer",
                                    "INSTANCE",
                                    "Lorg/tingy/agent/gradle/UrlReplacer;"
                                )
                                visitInsn(SWAP)
                                visitMethodInsn(
                                    INVOKEVIRTUAL,
                                    "org/tingy/agent/gradle/UrlReplacer",
                                    "replace",
                                    "(Ljava/lang/String;)Ljava/lang/String;",
                                    false
                                )
                            }

                            "()Ljava/net/URI;" -> {
                                // stack: [URI]

                                visitInsn(DUP)
                                visitMethodInsn(
                                    INVOKEVIRTUAL,
                                    "java/net/URI",
                                    "toString",
                                    "()Ljava/lang/String;",
                                    false
                                )

                                // stack: [URI, String]
                                visitFieldInsn(
                                    GETSTATIC,
                                    "org/tingy/agent/gradle/UrlReplacer",
                                    "INSTANCE",
                                    "Lorg/tingy/agent/gradle/UrlReplacer;"
                                )
                                visitInsn(SWAP)

                                // stack: [URI, INSTANCE, String]
                                visitMethodInsn(
                                    INVOKEVIRTUAL,
                                    "org/tingy/agent/gradle/UrlReplacer",
                                    "replace",
                                    "(Ljava/lang/String;)Ljava/lang/String;",
                                    false
                                )

                                // stack: [URI, String]
                                visitTypeInsn(NEW, "java/net/URI")
                                visitInsn(DUP_X1)
                                visitInsn(SWAP)
                                visitMethodInsn(
                                    INVOKESPECIAL,
                                    "java/net/URI",
                                    "<init>",
                                    "(Ljava/lang/String;)V",
                                    false
                                )
                                // stack: [URI]
                            }
                        }
                    }
                }
            }
        }

        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }
}
