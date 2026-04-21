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
-keep public class com.answufeng.image.CropTransformation {
    public *;
}
-keep public class com.answufeng.image.WatermarkTransformation {
    public *;
}

-keepclassmembers class com.answufeng.image.ImageExtKt {
    public static ** loadImage(...);
    public static ** loadCircle(...);
    public static ** loadRounded(...);
    public static ** loadRoundedDp(...);
    public static ** loadCircleWithBorder(...);
    public static ** loadBlur(...);
}

# Coil (ships its own rules)
-dontwarn coil.**
