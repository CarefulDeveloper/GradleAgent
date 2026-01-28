import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import proguard.gradle.ProGuardTask

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.guardsquare.proguard") version "7.5.0" apply false
}

group = "org.tingy"
version = "1.3"

dependencies {
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
    testImplementation(platform("org.junit:junit-bom:5.14.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    val outClassPath = layout.buildDirectory.file("tmp/classes")
        .get().asFile.absolutePath
    jvmArgs(
        "-Dgenerate.class.dir=$outClassPath"
    )
}

val shadowJarTask = tasks.named<ShadowJar>("shadowJar") {
    manifest {
        this.attributes["Premain-Class"] = "org.tingy.agent.gradle.MainKt"
        this.attributes["Main-Class"] = "org.tingy.agent.gradle.MainKt"

        // DEBUG ONLY
        this.attributes["Log-Path"] = layout.buildDirectory.file("tmp/log.txt")
            .get().asFile.absolutePath
    }
}

val buildRelease = task<ProGuardTask>("buildRelease") {
    dependsOn(shadowJarTask)

    val ruleFile = project.file("proguard-rules.pro")
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

gradle.taskGraph.whenReady {
    if(allTasks.contains(buildRelease)) {
        shadowJarTask.configure {
            manifest.attributes.remove("Log-Path")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}