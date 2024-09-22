import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import proguard.gradle.ProGuardTask

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.guardsquare.proguard") version "7.5.0" apply false
}

group = "org.tingy"
version = "1.0"

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.15.1")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("com.github.spotbugs:spotbugs:4.8.6")
    compileOnly("org.ow2.asm:asm:9.7")
    compileOnly("net.java.dev.jna:jna:5.15.0")
}

tasks.test {
    useJUnitPlatform()
}

val shadowJarTask = tasks.named<ShadowJar>("shadowJar") {
    manifest {
        this.attributes["Premain-Class"] = "MainKt"
        this.attributes["Main-Class"] = "MainKt"
    }
}

task<ProGuardTask>("buildRelease") {
    dependsOn(shadowJarTask)

    val ruleFile = project.file("src/main/proguard-rules.pro")
    configuration(ruleFile)
    inputs.file(ruleFile)

    val inputJar = shadowJarTask.get().outputs.files.singleFile
    injars(inputJar)
    inputs.file(inputJar)

    val outJar = File(
        inputJar.parentFile.canonicalPath,
        inputJar.nameWithoutExtension + "-release." + inputJar.extension
    )
    outjars(outJar)
    outputs.files(outJar)

    printmapping(layout.buildDirectory.file("tmp/ProGuard/mapping.txt"))

    doFirst {
        val libraries = mutableListOf<File>()
        // get all java classes(JDK VERSION > 8)
        val service = project.extensions.getByType<JavaToolchainService>()
        val compiler = service.compilerFor(project.extensions.getByType<JavaPluginExtension>().toolchain)
        val javaHome = compiler.get().metadata.installationPath
        javaHome.dir("jmods").asFile.listFiles()?.filter { it.extension == "jmod" }?.forEach {
            libraries.add(it)
        }

        // get all dependence classpath
        val runtimeClasspath = configurations.runtimeClasspath.get().files
        configurations.compileClasspath.get().files.filter { !runtimeClasspath.contains(it) }.forEach {
            libraries.add(it)
        }

        libraryjars(
            mapOf(
                "jarfilter" to "!**.jar",
                "filter" to "!module-info.class"
            ), libraries
        )
    }
}

kotlin {
    jvmToolchain(17)
}