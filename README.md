# aw-image

[![](https://jitpack.io/v/answufeng/aw-image.svg)](https://jitpack.io/#answufeng/aw-image)

基于 Coil 封装的 Android 图片加载库，提供简洁的 DSL API、常用变换和预加载支持。

## 特性

- **零配置**：无需初始化即可使用
- **DSL API**：`loadImage` / `loadCircle` / `loadRounded` / `loadBlur`
- **轻量 Scope**：`AwImageScope` 直接操作 Coil Builder，零中间对象分配
- **预加载**：单张/批量预加载（返回结果），获取 Drawable，并发可控
- **内置变换**：灰度 / 颜色滤镜 / 边框 / 高斯模糊（API 31+ RenderEffect 硬件加速）
- **缓存管理**：内存/磁盘缓存清理，异常安全
- **GIF 支持**：内置 coil-gif
- **Fallback 支持**：data 为 null 时显示兜底图（支持 Drawable）
- **Drawable 支持**：占位图/错误图/兜底图均支持 Drawable 和 @DrawableRes
- **离线智能缓存**：无网络时自动使用缓存，网络恢复实时感知
- **自定义 OkHttpClient**：支持自定义超时、拦截器等
- **日志调试**：可选日志输出，方便排查问题
- **自定义磁盘缓存目录**：支持指定缓存路径

## 引入

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.answufeng:aw-image:1.1.0")
}
```

> Coil 和 OkHttp 以 `api` 方式传递，无需额外声明。

## 快速开始

### 零配置使用

```kotlin
imageView.loadImage("https://example.com/photo.jpg")
imageView.loadCircle(user.avatarUrl)
imageView.loadRounded(url, radiusPx = 12f)
imageView.loadBlur(url)
```

### 可选初始化

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AwImage.init(this) {
            memoryCacheSize(0.25)
            diskCacheSize(256L * 1024 * 1024)
            diskCacheDir(File(cacheDir, "my_image_cache"))
            enableGif(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error)
            enableLogging(BuildConfig.DEBUG)
        }
    }
}
```

### Drawable 占位图/错误图

```kotlin
AwImage.init(this) {
    placeholder(ColorDrawable(Color.GRAY))
    error(ColorDrawable(Color.RED))
}
```

### 自定义 OkHttpClient

```kotlin
AwImage.init(this) {
    okHttpClient(OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain -> /* 统一添加 header */ }
        .build()
    )
}
```

## DSL 配置

`loadImage` 的配置块接收 `AwImageScope`，直接操作 Coil 的 `ImageRequest.Builder`，无中间对象分配。

```kotlin
imageView.loadImage(url) {
    // 占位图/错误图/兜底图（支持 @DrawableRes 和 Drawable）
    placeholder(R.drawable.loading)
    error(R.drawable.fail)
    fallback(R.drawable.fallback)
    fallback(ColorDrawable(Color.GRAY))

    // 变换
    circle()
    roundedCorners(12f)
    roundedCorners(topLeft = 8f, topRight = 8f, bottomRight = 0f, bottomLeft = 0f)
    transform(
        GrayscaleTransformation(),
        BorderTransformation(2f, Color.WHITE, circle = true)
    )

    // 动画
    crossfade(300)
    crossfade(false)    // 禁用渐入动画

    // 尺寸
    override(200, 200)

    // 缓存
    noCache()
    cacheOnlyOnOffline(false)

    // 监听器（累积模式，不会覆盖已设置的回调）
    onStart { showProgress() }
    onSuccess { result -> hideProgress() }
    onError { result -> showRetry() }

    // 或使用 listener 方法（非 null 参数才会覆盖）
    listener(
        onStart = { showProgress() },
        onSuccess = { result -> hideProgress() },
        onError = { result -> showRetry() }
    )
}
```

## 内置变换

| 变换 | 说明 |
|------|------|
| `GrayscaleTransformation()` | 灰度效果 |
| `ColorFilterTransformation(color)` | 颜色滤镜 |
| `BorderTransformation(width, color, circle)` | 边框（支持圆形） |
| `BlurTransformation(radius, sampling)` | 高斯模糊（API 31+ 使用 RenderEffect 硬件加速，低版本回退 StackBlur） |

## 预加载

```kotlin
lifecycleScope.launch {
    // 单张预加载
    val success: Boolean = ImagePreloader.preload(context, url)

    // 批量预加载（返回每个 URL 的加载结果）
    val results: List<Boolean> = ImagePreloader.preloadAll(context, urls, concurrency = 8)
    val successCount = results.count { it }

    // 获取已缓存的 Drawable
    val drawable: Drawable? = ImagePreloader.get(context, url)
}
```

> `preloadAll` 默认并发数为 8，可通过 `concurrency` 参数调整。返回 `List<Boolean>` 表示每个 URL 的加载结果。

## 缓存管理

```kotlin
val memoryCleared = AwImage.clearMemoryCache(context)
val diskCleared = AwImage.clearDiskCache(context)
```

> 缓存清理方法内置异常保护，返回 `Boolean` 表示操作是否成功。

## 离线智能缓存

默认开启：当设备无网络连接时，自动禁用网络请求，仅从内存/磁盘缓存读取图片。
网络状态通过 `ConnectivityManager.NetworkCallback` 实时监听，恢复后自动切换回在线模式。

```kotlin
// 默认行为：离线时自动使用缓存
imageView.loadImage(url)

// 禁用此行为（离线时仍尝试网络请求）
imageView.loadImage(url) {
    cacheOnlyOnOffline(false)
}
```

## 全局配置项

| 配置 | 方法 | 默认值 |
|------|------|--------|
| 内存缓存比例 | `memoryCacheSize(percent)` | 0.25 (25%) |
| 内存缓存字节 | `memoryCacheMaxSize(bytes)` | - (按比例) |
| 磁盘缓存大小 | `diskCacheSize(bytes)` | 100MB |
| 磁盘缓存目录 | `diskCacheDir(file)` | `{cacheDir}/aw_image_cache` |
| 渐入动画 | `crossfade(enabled)` / `crossfade(ms)` | true / 200ms |
| GIF 支持 | `enableGif(enabled)` | true |
| 全局占位图 | `placeholder(resId)` / `placeholder(Drawable)` | 0 (不设置) |
| 全局错误图 | `error(resId)` / `error(Drawable)` | 0 (不设置) |
| 自定义网络层 | `okHttpClient(OkHttpClient)` | Coil 默认 |
| 调试日志 | `enableLogging(enabled)` | false |

## 依赖说明

| 依赖 | 版本 | 用途 |
|------|------|------|
| Coil | 2.7.0 | 图片加载引擎 |
| coil-gif | 2.7.0 | GIF 解码支持 |
| OkHttp | 4.12.0 | 网络层（可自定义） |
| kotlinx-coroutines | 1.9.0 | 协程支持（预加载并发控制） |

## 许可证

Apache License 2.0，详见 [LICENSE](LICENSE)。
