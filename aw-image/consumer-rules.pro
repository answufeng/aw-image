# aw-image consumer ProGuard rules
# 精准保留公开 API，避免过度混淆

# ===========================================================
# 全局配置入口
# ===========================================================

-keepclassmembers class com.answufeng.image.AwImage {
    public *** init(...);
    public *** imageLoader(...);
    public *** clearMemoryCache(...);
    public *** clearDiskCache(...);
    public *** getMemoryCacheSize(...);
    public *** getDiskCacheSize(...);
    public *** isCached(...);
    public *** cancelByTag(...);
    public boolean isInitialized();
}

-keepclassmembers class com.answufeng.image.AwImage$ImageConfig {
    public *** memoryCacheSize(...);
    public *** memoryCacheMaxSize(...);
    public *** diskCacheSize(...);
    public *** diskCacheDir(...);
    public *** crossfade(...);
    public *** enableGif(...);
    public *** enableSvg(...);
    public *** placeholder(...);
    public *** error(...);
    public *** fallback(...);
    public *** okHttpClient(...);
    public *** enableLogging(...);
    public double getMemoryCachePercent();
    public long getDiskCacheSize();
    public boolean isCrossfadeEnabled();
    public int getCrossfadeDuration();
    public boolean isGifEnabled();
    public boolean isSvgEnabled();
    public int getPlaceholderRes();
    public int getErrorRes();
    public int getFallbackRes();
}

# ===========================================================
# DSL 配置作用域
# ===========================================================

-keepclassmembers class com.answufeng.image.AwImageScope {
    public *** placeholder(...);
    public *** error(...);
    public *** fallback(...);
    public *** scale(...);
    public *** circle();
    public *** roundedCorners(...);
    public *** override(...);
    public *** disableCache();
    public *** offlineCacheEnabled(...);
    public *** memoryCacheOnly();
    public *** transform(...);
    public *** crossfade(...);
    public *** tag(...);
    public *** lifecycle(...);
    public *** listener(...);
    public *** onStart(...);
    public *** onSuccess(...);
    public *** onError(...);
    public *** onProgress(...);
}

# ===========================================================
# 预加载器
# ===========================================================

-keepclassmembers class com.answufeng.image.ImagePreloader {
    public suspend *** preload(...);
    public suspend *** getDrawable(...);
    public suspend *** preloadAll(...);
}

# ===========================================================
# 变换类
# ===========================================================

-keepclassmembers class com.answufeng.image.GrayscaleTransformation {
    public <init>();
    public *** transform(...);
}

-keepclassmembers class com.answufeng.image.ColorFilterTransformation {
    public <init>(...);
    public *** transform(...);
}

-keepclassmembers class com.answufeng.image.BorderTransformation {
    public <init>(...);
    public *** transform(...);
}

-keepclassmembers class com.answufeng.image.BlurTransformation {
    public <init>(...);
    public *** transform(...);
}

-keepclassmembers class com.answufeng.image.CropTransformation {
    public <init>(...);
    public *** transform(...);
}

-keepclassmembers class com.answufeng.image.WatermarkTransformation {
    public <init>(...);
    public *** transform(...);
}

# ===========================================================
# 扩展函数（Kotlin 编译为静态方法）
# ===========================================================

-keepclassmembers class com.answufeng.image.ImageExtKt {
    public static *** loadImage(...);
    public static *** loadCircle(...);
    public static *** loadRounded(...);
    public static *** loadRoundedDp(...);
    public static *** loadCircleWithBorder(...);
    public static *** loadBlur(...);
}
