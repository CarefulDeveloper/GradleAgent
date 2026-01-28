# Keep jvm entry code
-keep public class org.tingy.agent.gradle.MainKt {
    *;
}

# Keep inject entry code
-keep public class org.tingy.agent.gradle.UrlReplacer {
    *;
}

# Keep all annotations
-keepattributes *Annotation*

# For net.bytebuddy
-keepnames class net.bytebuddy.** {
    *;
}
-keep class net.bytebuddy.dynamic.Nexus {
    *;
}
-keep @interface net.bytebuddy.implementation.bind.annotation.* {
    *;
}