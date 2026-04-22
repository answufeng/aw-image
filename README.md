# aw-image

[![](https://jitpack.io/v/answufeng/aw-image.svg)](https://jitpack.io/#answufeng/aw-image)

基于 Coil 封装的 Android 图片加载库，提供简洁的 DSL API、常用变换和预加载支持。  
设计目标：在**不隐藏 Coil 能力**的前提下，用 Kotlin DSL 减少样板代码，并统一缓存键、进度与混淆规则（详见下文「高级 API」与「ProGuard」）。

## 特性

- **零配置**：无需初始化即可使用
- **DSL API**：`loadImage` / `loadCircle` / `loadRounded` / `loadRoundedDp` / `loadCircleWithBorder` / `loadBlur`
- **轻量 Scope**：`AwImageScope` 直接操作 Coil Builder，零中间对象分配
- **高级/逃生口**：`raw { }` 与 `addHeader` / 细粒度 `CachePolicy` / `placeholderMemoryCacheKey`（多阶段图）
- **预加载**：单张/批量预加载（返回结果），获取 Drawable，并发可控
- **内置变换**：灰度 / 颜色滤镜 / 边框 / 高斯模糊（API 31+ RenderEffect 硬件加速）
- **缓存管理**：内存/磁盘缓存清理，缓存查询，异常安全
- **GIF 支持**：内置 coil-gif
- **Fallback 支持**：data 为 null 时显示兜底图（支持 Drawable）
- **Drawable 支持**：占位图/错误图/兜底图均支持 Drawable 和 @DrawableRes
- **离线智能缓存**：无网络时自动使用缓存，网络恢复实时感知
- **Lifecycle 感知**：绑定 LifecycleOwner，页面销毁时自动取消请求
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
    implementation("com.github.answufeng:aw-image:1.2.0")
}
```

> Coil 和 OkHttp 以 `api` 方式传递，宿主**无需**再写 `implementation(coil)` 即可编译使用 Coil/OkHttp 类型；若你自己也依赖了不同版本的 Coil/OkHttp，见下文「与宿主应用的依赖关系」。
>
> 规则文件：`aw-image` 通过 `consumerProguardFiles` 内置 [consumer-rules.pro](aw-image/consumer-rules.pro)，R8/ProGuard 下扩展函数等入口已做保留。Debug 不混淆，**请在 Release 上验证**（本仓库 CI 中示范 `:demo:assembleRelease`）。

## 依赖说明

| 依赖 | 版本 | 用途 |
|------|------|------|
| Coil | 2.7.0 | 图片加载引擎 |
| coil-gif | 2.7.0 | GIF 解码支持 |
| coil-svg | 2.7.0 | SVG 解码支持（默认关闭） |
| OkHttp | 4.12.0 | 网络层（可自定义） |
| kotlinx-coroutines | 1.9.0 | 协程支持（预加载并发控制） |

### 与宿主应用的依赖关系（`api` 传递）

- 本库对 Coil、`coil-gif`、`okhttp3` 使用 **`api`**，与 App 的 **classpath 中只会解析一份** 传递依赖；Gradle 会按 **最近版本/约束** 选一套版本。
- **建议**：宿主**不要**再单独指定与上述表格大版本冲突的 Coil/OkHttp，或在 `gradle/libs.versions.toml` 中**统一** `coil`、`okio`、`okhttp` 版本，避免 `Duplicate class` 或行为不一致。
- 若你**完全不用**本库在 DSL 中暴露的 OkHttp/Coil 类型，理论上仍受传递版本影响；出问题时用 `./gradlew :app:dependencies` 查冲突边。

## Java 与混淆（R8 / ProGuard）

- **Kotlin** 是推荐调用方式。Java 中可通过 `ImageView` 的静态扩展（类名以 **`ImageLoadExtensionsKt`** 为准，见 consumer-rules）调用 `loadImage`，参数较多时可在 Java 中拆成多行，或从 Kotlin 包一层业务方法。
- 混淆时 **AAR 自带 consumer 规则**；若你仍遇 `loadImage` 在运行时消失，请确认 **未在宿主 ProGuard 中 `-dontshrink` 掉整个 `com.answufeng.image`** 且使用与本库**一致版本**的 Kotlin 元数据相关保留规则（Coil 亦自带 consumer 规则）。

## 持续集成

仓库含 [`.github/workflows/android.yml`](.github/workflows/android.yml)：在 **JDK 17** 下执行 `assembleRelease`、`testDebugUnitTest`、`lintRelease` 与 **demo 的 R8 打包**。

## 快速开始 (3 steps)

### Step 1: 加载图片

无需任何初始化，直接在 `ImageView` 上调用扩展函数：

```kotlin
imageView.loadImage("https://example.com/photo.jpg")
```

### Step 2: 常用场景

```kotlin
// 圆形头像
imageView.loadCircle(user.avatarUrl)

// 圆角图片（第二参数为 px，dp 请用 loadRoundedDp）
imageView.loadRounded(url, 24f)
// 或
imageView.loadRoundedDp(url, radiusDp = 8f)

// 带边框的圆形头像
imageView.loadCircleWithBorder(url, borderWidth = 4f, borderColor = Color.WHITE)

// 模糊背景
imageView.loadBlur(url, radius = 20)
```

### Step 3: 可选配置（Application 中初始化）

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AwImage.init(this) {
            memoryCacheSize(0.25)
            diskCacheSize(256L * 1024 * 1024)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error)
            crossfade(300)
            enableLogging(BuildConfig.DEBUG)
            // 可选：仅要求 INTERNET、不要求 VALIDATED，减轻强制门户等场景下「误判离线、只走缓存」
            // strictNetworkForOffline(false)
        }
    }
}
```

## 架构

```
┌─────────────────────────────────────────┐
│              用户代码                     │
├─────────────────────────────────────────┤
│  loadImage / loadCircle / loadRounded   │  ← 扩展函数 API 层
│  loadBlur / loadCircleWithBorder        │
├─────────────────────────────────────────┤
│            AwImageScope                 │  ← DSL 配置层
│  raw / addHeader / CachePolicy 等        │
│  (直接操作 Coil ImageRequest.Builder)     │
├─────────────────────────────────────────┤
│  AwImage (全局配置)  ImagePreloader      │  ← 全局管理层
│  ImageNetworkMonitor       AwImageLogger │
├─────────────────────────────────────────┤
│  Grayscale / ColorFilter / Border /     │  ← 变换层
│  Blur (StackBlur + RenderEffect)        │
├─────────────────────────────────────────┤
│              Coil + OkHttp              │  ← 底层引擎
└─────────────────────────────────────────┘
```

---

## 进阶使用

### DSL 配置

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
    disableCache()
    offlineCacheEnabled(false)
    memoryCacheOnly()

    // Lifecycle 感知（页面销毁时自动取消）
    lifecycle(this@MyActivity)

    // 标签（用于批量取消）
    tag("feed_list")

    // 监听器（独立方法，覆盖模式）
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

### 高级：请求头、细粒度缓存、`raw`、多阶段占位

```kotlin
imageView.loadImage(url) {
    // CDN / 鉴权
    addHeader("Authorization", "Bearer $token")
    // 或
    // setHeader("User-Agent", "MyApp/1.0")

    // 细粒度缓存（与 disableCache() / memoryCacheOnly() 勿矛盾混用）
    // memoryCachePolicy(CachePolicy.ENABLED)
    // diskCachePolicy(CachePolicy.DISABLED)

    // 多阶段：先显示内存里已有的一张小图，再拉全图
    // placeholderMemoryCacheKey("thumb_key_from_previous_request")

    // 任意未封装参数（勿在 raw 中再设 transformations，会与本库 transform/circle 冲突）
    raw {
        // parameters { ... }  // 若需要
    }
}
```

- **`raw { }`**：在应用变换与离线策略之后、绑定监听器之前**依次**执行，适合与 Coil 官方文档一一对应的高级参数。使用 `onProgress` 时不要移除内部进度关联头（`X-AwImage-Progress-Token`），否则进度会失效。
- **全局 `Init` 中的 Drawable**（`placeholder(Drawable)` 等）在内部已 `mutate()`，**只读**使用，勿在多线程改 Drawable 状态。

### 内置变换

| 变换 | 说明 |
|------|------|
| `GrayscaleTransformation()` | 灰度效果 |
| `ColorFilterTransformation(color)` | 颜色滤镜 |
| `BorderTransformation(width, color, circle)` | 边框（支持圆形裁切） |
| `BlurTransformation(radius, sampling)` | 高斯模糊（API 31+ 使用 RenderEffect 硬件加速，低版本回退 StackBlur） |
| `CropTransformation(x, y, width, height)` | 图片裁切 |
| `WatermarkTransformation(watermark, x, y, alpha)` | 水印叠加 |

### 变换示例

```kotlin
// 圆形头像带白色边框
imageView.loadCircleWithBorder(avatarUrl, borderWidth = 4f, borderColor = Color.WHITE)

// 灰度 + 圆角
imageView.loadImage(url) {
    roundedCorners(16f)
    transform(GrayscaleTransformation())
}

// 高斯模糊背景
imageView.loadBlur(url, radius = 20, sampling = 2)

// 图片裁切
imageView.loadImage(url) {
    transform(CropTransformation(0, 0, 200, 200))
}
```

### 预加载

```kotlin
lifecycleScope.launch {
    // 单张预加载
    val success: Boolean = ImagePreloader.preload(context, url)

    // 批量预加载（返回每个 URL 的加载结果）；可选与展示一致的 ImageRequest 配置
    val results: List<Boolean> = ImagePreloader.preloadAll(
        context, urls, concurrency = 8
    ) { size(200, 200) }

    // 获取已缓存的 Drawable
    val drawable: Drawable? = ImagePreloader.getDrawable(context, url) { size(200, 200) }
}
```

> `preloadAll` 默认并发数为 8，可通过 `concurrency` 参数调整。返回 `List<Boolean>` 表示每个 URL 的加载结果。  
> `preload` / `getDrawable` / `preloadAll` 的最后一个参数可传入与 `loadImage` 中相同的 `ImageRequest` 配置（如 `size()`、`transformations {}` 等），保证预加载与列表展示共用缓存键。

### 缓存管理

```kotlin
// 清除缓存
val memoryCleared = AwImage.clearMemoryCache(context)
val diskCleared = AwImage.clearDiskCache(context)

// 查询缓存大小
val memSize = AwImage.getMemoryCacheSize(context)
val diskSize = AwImage.getDiskCacheSize(context)

// 检查是否已缓存（与线加载的 key 须一致，若有尺寸/变换需传入相同配置）
val cached = AwImage.isCached(context, url)
val cachedSized = AwImage.isCached(context, url) { size(200, 200) }
```

> 缓存清理方法内置异常保护，返回 `Boolean` 表示操作是否成功。  
> `isCached` 的第二个重载为 `(context, data) { requestConfig }`，`requestConfig` 与 `loadImage` / `ImageRequest.Builder` 一致；若只传 `data` 则只匹配默认 key。

### Lifecycle 感知

绑定 LifecycleOwner 后，页面销毁时自动取消图片加载请求，避免回调泄漏。

```kotlin
// 在 Activity/Fragment 中
imageView.loadImage(url) {
    lifecycle(this@MyActivity)
}

// 在 RecyclerView Adapter 中
holder.imageView.loadImage(url) {
    lifecycle(fragment)
}
```

### 批量取消

通过标签批量取消图片加载请求：

```kotlin
// 加载时设置标签
imageView.loadImage(url) { tag("feed_list") }

// 退出页面时批量取消
AwImage.cancelByTag("feed_list")
```

### 离线智能缓存

默认开启：当设备无网络连接时，自动禁用网络请求，仅从内存/磁盘缓存读取图片。
网络状态通过 `ConnectivityManager.NetworkCallback` 实时监听，恢复后自动切换回在线模式。

```kotlin
// 默认行为：离线时自动使用缓存
imageView.loadImage(url)

// 禁用此行为（离线时仍尝试网络请求）
imageView.loadImage(url) {
    offlineCacheEnabled(false)
}
```

### 全局配置项

| 配置 | 方法 | 默认值 |
|------|------|--------|
| 内存缓存比例 | `memoryCacheSize(percent)` | 0.25 (25%) |
| 内存缓存字节 | `memoryCacheMaxSize(bytes)` | - (按比例) |
| 磁盘缓存大小 | `diskCacheSize(bytes)` | 100MB |
| 磁盘缓存目录 | `diskCacheDir(file)` | `{cacheDir}/aw_image_cache` |
| 渐入动画 | `crossfade(enabled)` / `crossfade(ms)` | true / 200ms |
| GIF 支持 | `enableGif(enabled)` | true |
| SVG 支持 | `enableSvg(enabled)` | false |
| 全局占位图 | `placeholder(resId)` / `placeholder(Drawable)` | 0 (不设置) |
| 全局错误图 | `error(resId)` / `error(Drawable)` | 0 (不设置) |
| 全局兜底图 | `fallback(resId)` / `fallback(Drawable)` | 0 (不设置) |
| 自定义网络层 | `okHttpClient(OkHttpClient)` | Coil 默认 |
| 调试日志 | `enableLogging(enabled)` | false |

### 从 Glide/Picasso 迁移

| Glide | aw-image |
|-------|----------|
| `Glide.with(context).load(url).into(imageView)` | `imageView.loadImage(url)` |
| `.placeholder(R.drawable.ph)` | `imageView.loadImage(url, placeholderRes = R.drawable.ph)` 或 DSL `placeholder(R.drawable.ph)` |
| `.error(R.drawable.err)` | `imageView.loadImage(url, errorRes = R.drawable.err)` 或 DSL `error(R.drawable.err)` |
| `.circleCrop()` | `imageView.loadCircle(url)` 或 DSL `circle()` |
| `.transform(new RoundedCorners(16))` | `imageView.loadRounded(url, 16f)` 或 DSL `roundedCorners(16f)` |
| `.diskCacheStrategy(DiskCacheStrategy.ALL)` | 默认行为 |
| `.skipMemoryCache(true)` | DSL `disableCache()` |
| `.listener(...)` | DSL `onSuccess { }` / `onError { }` |

---

## FAQ

### Q: 如何取消单个图片加载请求？

`loadImage` 返回 `Disposable` 对象，调用 `dispose()` 即可：

```kotlin
val disposable = imageView.loadImage(url)
disposable.dispose()
```

但通常情况下不需要手动取消，Coil 会在 View detach 或发起新请求时自动取消。

### Q: 如何自定义缓存策略？

```kotlin
imageView.loadImage(url) {
    // 完全禁用缓存
    disableCache()

    // 仅使用内存缓存（跳过磁盘和网络）
    memoryCacheOnly()

    // 离线时仅使用缓存
    offlineCacheEnabled(true)
}
```

### Q: `loadImage` 传 null 会怎样？

当 data 为 null 时，会依次尝试显示：
1. DSL 中设置的 `fallback`
2. 全局 fallback
3. 全局 error
4. 清空 ImageView

不会触发网络请求。

### Q: 如何自定义 OkHttpClient？

```kotlin
AwImage.init(this) {
    okHttpClient(OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain -> /* 统一添加 header */ }
        .build()
    )
}
```

### Q: 多次调用 `AwImage.init()` 会怎样？

会覆盖之前的所有配置。建议仅在 `Application.onCreate()` 中调用一次。

### Q: 如何在 RecyclerView 中避免图片闪烁？

```kotlin
// 快速滑动时仅使用内存缓存
holder.imageView.loadImage(url) {
    memoryCacheOnly()
    lifecycle(fragment)  // 页面销毁时自动取消
}
```

---

## 发版与生产环境检查清单

- [ ] **JDK 17** 构建与 `./gradlew :aw-image:lintRelease :aw-image:testDebugUnitTest` 通过。
- [ ] **Release** 包在真机/模拟器上打开关键列表与图片（含 SVG、GIF 若用）。
- [ ] 宿主与传递依赖中 **Coil/OkHttp 无版本冲突**（`./gradlew :app:dependencies`）。
- [ ] 对网络图使用 **`https`**；`AwImage.init` 中 `enableLogging` 在 release 中应为 **false**。
- [ ] 对「预加载 + 展示」用 **同一 `size()` / 变换**（或同一 `isCached { }` 配置）以保证缓存键一致。

## 许可证

Apache License 2.0，详见 [LICENSE](LICENSE)。

# Last updated: 2026年 4月 22日
