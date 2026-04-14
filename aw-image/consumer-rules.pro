# aw-image consumer ProGuard rules

-keep public class com.answufeng.image.AwImage {
    public *;
}
-keep public class com.answufeng.image.AwImage$ImageConfig {
    public *;
}
-keep public class com.answufeng.image.AwImageScope {
    public *;
}
-keep public class com.answufeng.image.ImagePreloader {
    public *;
}

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

-keepclassmembers class com.answufeng.image.ImageExtKt {
    public static ** loadImage(...);
    public static ** loadCircle(...);
    public static ** loadRounded(...);
    public static ** loadBlur(...);
}
