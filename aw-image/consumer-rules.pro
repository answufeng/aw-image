# aw-image consumer ProGuard rules

# Public API classes — only keep public/protected members
-keep public class com.answufeng.image.AwImage {
    public *;
}
-keep public class com.answufeng.image.AwImage$ImageConfig {
    public *;
}
-keep public class com.answufeng.image.ImageLoadConfig {
    public *;
}
-keep public class com.answufeng.image.ImagePreloader {
    public *;
}

# Built-in transformations — public API
-keep public class com.answufeng.image.GrayscaleTransformation {
    public *;
}
-keep public class com.answufeng.image.ColorFilterTransformation {
    public *;
}
-keep public class com.answufeng.image.BorderTransformation {
    public *;
}
-keep public class com.answufeng.image.BlurTransformation {
    public *;
}

# Extension functions (compiled to static methods in ImageExtKt)
-keepclassmembers class com.answufeng.image.ImageExtKt {
    public static ** loadImage(...);
    public static ** loadCircle(...);
    public static ** loadRounded(...);
    public static ** loadBlur(...);
}
