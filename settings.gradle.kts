import org.gradle.api.internal.FeaturePreviews

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        // not used. gradle project maven repository
        maven("https://repo.gradle.org/artifactory/libs-releases/")
    }
}


pluginManagement {
    resolutionStrategy {
        this.eachPlugin {
            if(this.requested.id.id == "com.guardsquare.proguard") {
                useModule("com.guardsquare:proguard-gradle:${this.requested.version}")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "GradleAgent"
enableFeaturePreview(FeaturePreviews.Feature.TYPESAFE_PROJECT_ACCESSORS.name)
