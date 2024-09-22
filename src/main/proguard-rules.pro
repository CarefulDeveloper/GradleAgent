# Keep our entry code
-keep public class MainKt {
    *;
}
-keep public class WrapperExecutorInterceptor {
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