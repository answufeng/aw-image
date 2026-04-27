# aw-image consumer ProGuard rules
# 精准保留公开 API，避免过度混淆
# 宿主 release 可配合 -printusage 抽样，逐步收紧 -keepclassmembers（需回归 loadImage / DSL）

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
    public *** onApplicationTrimMemory(...);
    public *** isCached(...);
    public *** cancelByTag(...);
    public *** cancelAllTaggedRequests();
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
    public *** strictNetworkForOffline(...);
    public boolean isStrictNetworkForOffline();
    public *** placeholder(...);
    public *** error(...);
    public *** fallback(...);
    public *** okHttpClient(...);
    public *** enableLogging(...);
    public *** logTag(...);
    public *** defaultRequestListener(...);
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
    public *** memoryCacheKey(...);
    public *** diskCacheKey(...);
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
    public *** raw(...);
    public *** addHeader(...);
    public *** setHeader(...);
    public *** removeHeader(...);
    public *** headers(...);
    public *** memoryCachePolicy(...);
    public *** diskCachePolicy(...);
    public *** networkCachePolicy(...);
    public *** placeholderMemoryCacheKey(...);
}

# ===========================================================
# 预加载器
# ===========================================================

-keepclassmembers class com.answufeng.image.ImagePreloader {
    public *** preload(...);
    public *** getDrawable(...);
    public *** preloadAll(...);
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

-keepclassmembers class com.answufeng.image.ImageLoadExtensionsKt {
    public static *** loadImage(...);
    public static *** loadCircle(...);
    public static *** loadRounded(...);
    public static *** loadRoundedDp(...);
    public static *** loadCircleWithBorder(...);
    public static *** loadBlur(...);
    public static *** loadSquare(...);
    public static *** loadWithAspectRatio(...);
}

# ===========================================================
# 预设 DSL 片段
# ===========================================================

-keepclassmembers class com.answufeng.image.AwImagePresets {
    public *** listThumbnail(...);
    public *** avatar(...);
}
