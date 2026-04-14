# aw-image

[![](https://jitpack.io/v/answufeng/aw-image.svg)](https://jitpack.io/#answufeng/aw-image)

基于 Coil 封装的 Android 图片加载库，提供简洁的 DSL API、常用变换和预加载支持。

## 特性

- **零配置**：无需初始化即可使用
- **DSL API**：`loadImage` / `loadCircle` / `loadRounded` / `loadBlur`
- **预加载**：单张/批量预加载，获取 Drawable，并发可控
- **内置变换**：灰度 / 颜色滤镜 / 边框 / 高斯模糊（API 31+ 硬件加速）
- **缓存管理**：内存/磁盘缓存清理，异常安全
- **GIF 支持**：内置 coil-gif
- **Fallback 支持**：data 为 null 时显示兜底图
- **离线智能缓存**：无网络时自动使用缓存，避免加载失败
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

> Coil 以 `api` 方式传递，无需额外声明。

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

## DSL 配置

```kotlin
imageView.loadImage(url) {
    placeholder(R.drawable.loading)
    error(R.drawable.fail)
    fallback(R.drawable.fallback)     // data 为 null 时的兜底图
    circle()
    roundedCorners(12f)
    crossfade(300)
    override(200, 200)
    noCache()
    cacheOnlyOnOffline(false)         // 禁用离线缓存策略
    transform(
        GrayscaleTransformation(),
        BorderTransformation(2f, Color.WHITE, circle = true)
    )
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
| `BlurTransformation(radius, sampling)` | 高斯模糊（API 31+ 使用 RenderEffect 硬件加速） |

## 预加载

```kotlin
lifecycleScope.launch {
    val success = ImagePreloader.preload(context, url)
    ImagePreloader.preloadAll(context, urls, concurrency = 8)
    val drawable = ImagePreloader.get(context, url)
}
```

> `preloadAll` 默认并发数为 8，可通过 `concurrency` 参数调整。

## 缓存管理

```kotlin
val memoryCleared = AwImage.clearMemoryCache(context)
val diskCleared = AwImage.clearDiskCache(context)
```

> 缓存清理方法内置异常保护，返回 `Boolean` 表示操作是否成功。

## 离线智能缓存

默认开启：当设备无网络连接时，自动禁用网络请求，仅从内存/磁盘缓存读取图片。
避免在弱网或离线环境下显示错误图，提升用户体验。

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
| 全局占位图 | `placeholder(resId)` | 0 (不设置) |
| 全局错误图 | `error(resId)` | 0 (不设置) |
| 调试日志 | `enableLogging(enabled)` | false |

## 依赖说明

| 依赖 | 版本 | 用途 |
|------|------|------|
| Coil | 2.7.0 | 图片加载引擎 |
| coil-gif | 2.7.0 | GIF 解码支持 |
| kotlinx-coroutines | 1.9.0 | 协程支持（预加载并发控制） |

## 许可证

Apache License 2.0，详见 [LICENSE](LICENSE)。
