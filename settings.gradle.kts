dependencyResolutionManagement {
    repositories {
        mavenCentral()
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

