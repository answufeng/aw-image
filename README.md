# aw-image

[![](https://jitpack.io/v/answufeng/aw-image.svg)](https://jitpack.io/#answufeng/aw-image)

基于 [Coil 2.7](https://github.com/coil-kt/coil) 的 Android 图片加载库（Kotlin、传统 `ImageView` / XML）。用 **DSL** 减少样板代码，不屏蔽 Coil 能力，并统一进度、缓存键与 [ProGuard 规则](aw-image/consumer-rules.pro)。

| 项 | 说明 |
|----|------|
| **当前发布** | **`1.0.0`**（见下方「引入」与 Git 标签） |
| **验证环境** | demo：compileSdk 35、minSdk 24、JDK 17 构建 |
| **Compose** | 本库面向 View / XML，不覆盖 Compose（可直接用官方 Coil-Compose） |

---

## 文档导读

1. [引入](#引入) → [快速开始](#快速开始)（约 5 分钟）  
2. [误用防火墙](#误用防火墙) → [内存与列表](#内存与列表滚动建议)  
3. [进阶使用](#进阶使用)（DSL、预加载、缓存、离线）  
4. [故障排除](#故障排除) / [FAQ](#faq)  
5. 演示：[demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md)  

---

## 误用防火墙（必读）

| 误用 | 后果 | 正确做法 |
|------|------|----------|
| 列表/大图不指定 `override` / `size` / 变换 | 解码过大，OOM、卡顿 | 与展示区域一致；用 `loadSquare` / `loadWithAspectRatio` 或 `AwImagePresets` |
| 预加载与展示 **缓存键不一致** | 重复下载 | 同一 URL 用相同 `size` / 变换，或 `memoryCacheKey` / `diskCacheKey` |
| Release 中 `enableLogging(true)` | 噪声、可能泄露 URL | 仅 `BuildConfig.DEBUG` 或排障时短期开启 |

---

## 特性

- **零配置**：不调用 `AwImage.init` 也可用（Coil 默认 `ImageLoader`）
- **扩展函数**：`loadImage`、`loadCircle`、`loadRounded` / `loadRoundedDp`、`loadCircleWithBorder`、`loadBlur`、`loadSquare`、`loadWithAspectRatio`
- **AwImageScope DSL**：直链 `ImageRequest.Builder`；`raw { }` 逃生口
- **AwImagePresets**：`listThumbnail`、`avatar` 等可复用片段
- **预加载**：`ImagePreloader` 单张/批量、并发控制、`getDrawable`
- **变换**：灰度、颜色滤镜、边框、模糊、裁切、水印
- **缓存**：清内存/磁盘、查大小、`isCached`；可选自定义磁盘目录
- **GIF / SVG**（SVG 默认关，在 `init` 里开启）
- **占位 / 错误 / 兜底**：支持 `@DrawableRes` 与 `Drawable`；`data == null` 时走 fallback 链
- **离线**：无网时优先进缓存；`strictNetworkForOffline` 可放宽 **VALIDATED** 判定
- **Lifecycle**：`lifecycle(owner)` 绑定后在销毁时取消
- **进度**：`onProgress`（仅 **http(s) 字符串**；回调可能在子线程，更新 UI 请 `view.post`）
- **全局**：`AwImage.init` 中 `defaultRequestListener`（与单次监听合并，先全局后单次）、`onApplicationTrimMemory` 辅助清内存
- **日志**：`enableLogging` / `logTag`；**每次** `init` 会先将日志关、tag 恢复为 `aw-image`，再应用当次块

---

## 引入

在 [JitPack](https://jitpack.io/#answufeng/aw-image) 启用仓库后依赖（**版本与 [Release 标签](https://github.com/answufeng/aw-image/tags) 一致，例如 `1.0.0`**）：

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.answufeng:aw-image:1.0.0")
}
```

> 本库对 Coil、`coil-gif`、`okhttp3` 使用 **`api`**，一般无需再写 `implementation(coil)`。若与宿主其它依赖冲突，在 `libs.versions.toml` 中统一 `coil` / `okhttp` / `okio` 版本，并执行 `./gradlew :app:dependencies` 排查。  
> **R8/ProGuard**：AAR 已带 [consumer-rules.pro](aw-image/consumer-rules.pro)；**请在 Release 包上验证**图片加载（本仓库 CI 会跑 `:demo:assembleRelease`）。

### 依赖版本（随发布依赖）

| 依赖 | 版本 |
|------|------|
| Coil / coil-gif / coil-svg | 2.7.0 |
| OkHttp | 4.12.0 |
| kotlinx-coroutines | 1.9.0 |

### 内存与列表滚动（建议）

- **低内存**：在 `Application`/`Activity` 的 `onTrimMemory` 中可调用 `AwImage.onApplicationTrimMemory(context, level)`，或 `AwImage.clearMemoryCache` / `imageLoader(context).memoryCache?.clear()`。
- **RecyclerView**：列表项指定解码尺寸；可用 `raw { }` 设置 `precision` 等，见 [Coil sizing](https://coil-kt.github.io/coil/getting_started/#image-size)。
- **模糊**：`BlurTransformation` 在低版本为 CPU 路径，长列表慎用；API 31+ 可用 RenderEffect 路径（见 KDoc）。
- **视频帧**：本 AAR 未带 `coil-video`；需要时在宿主加依赖并按 Coil 文档注册 `VideoFrameDecoder`。

---

## Java 与 R8/ProGuard

- 推荐 Kotlin 调用。Java 通过 `ImageView` 扩展，生成类名一般为 **`ImageLoadExtensionsKt`**（见 consumer-rules）。
- 勿在宿主规则中整包 `dontshrink` 掉 `com.answufeng.image`；与 Kotlin/Coil 的 `-keep` 需兼容。

---

## 工程与 CI

- **工作流**：[.github/workflows/ci.yml](.github/workflows/ci.yml)（JDK 17：`assembleRelease`、`lint`、`ktlint`、demo release）。  
- **本地**：`./gradlew :aw-image:assembleRelease :aw-image:ktlintCheck :aw-image:lintRelease :demo:assembleRelease`  
- **演示**：[demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md)  

---

## 快速开始

### 1. 最简

```kotlin
imageView.loadImage("https://example.com/photo.jpg")
```

### 2. 常见形态

```kotlin
imageView.loadCircle(avatarUrl)
imageView.loadRounded(url, 24f)           // px；dp 用 loadRoundedDp
imageView.loadSquare(url, edgePx = 200) { roundedCorners(8f) }
imageView.loadWithAspectRatio(url, 16, 9, maxEdgePx = 400)
imageView.loadCircleWithBorder(url, borderWidth = 4f, borderColor = Color.WHITE)
imageView.loadBlur(url, radius = 20)
imageView.loadImage(url, config = AwImagePresets.listThumbnail(200))
```

### 3. 可选全局初始化

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
            logTag("MyApp-Img")
            enableLogging(BuildConfig.DEBUG)
            // defaultRequestListener(object : ImageRequest.Listener { ... })
            // strictNetworkForOffline(false)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AwImage.onApplicationTrimMemory(this, level)
    }
}
```

每次 `init` 会**重置**未在块内显式设置的行为：先关日志、tag 回 `aw-image`，再执行块内配置；**建议只在 `Application` 调一次**。

---

## 演示应用

`demo` 模块覆盖基本加载、变换、列表、预加载、缓存、集成等；与 [DEMO_MATRIX.md](demo/DEMO_MATRIX.md) 及主界面「演示清单」对应。

---

## 架构

```
用户代码
  loadImage / loadCircle / loadSquare / loadWithAspectRatio / …
  AwImagePresets
      ↓
 AwImageScope（DSL，ImageRequest.Builder）
      ↓
 AwImage · ImagePreloader · ImageNetworkMonitor · AwImageLogger
      ↓
 自定义 Transformation
      ↓
 Coil + OkHttp
```

---

## 进阶使用

### DSL 要点

- `override(w, h)` 的 `w/h` 必须 **> 0**（否则 `IllegalArgumentException`）。
- `memoryCacheKey` / `diskCacheKey` 与 `isCached`、预加载的 builder 配置需一致才命中。
- `disableCache` / `memoryCacheOnly` / 各 `CachePolicy` 勿混用成互相矛盾。
- `raw { }` 在库侧变换、离线策略**之后**执行；**不要**在 `raw` 里再设 `transformations`（会覆盖 `circle`/`transform`）。
- 使用 `onProgress` 时不要移除头 `X-AwImage-Progress-Token`。
- 全局 `init` 里传入的 `Drawable` 已 `mutate()`，**只读**使用。

### DSL 配置示例

```kotlin
imageView.loadImage(url) {
    placeholder(R.drawable.loading)
    error(R.drawable.fail)
    fallback(R.drawable.fallback)

    circle()
    roundedCorners(12f)
    transform(GrayscaleTransformation(), BorderTransformation(2f, Color.WHITE, circle = true))

    crossfade(300)
    override(200, 200)

    disableCache()           // 与 memoryCacheOnly 等勿矛盾混用
    lifecycle(this@MyActivity)
    tag("feed_list")

    onStart { showProgress() }
    onSuccess { /* SuccessResult */ }
    onError { /* ErrorResult */ }
}
```

### 高级：请求头、`raw`、多阶段占位

```kotlin
imageView.loadImage(url) {
    addHeader("Authorization", "Bearer $token")
    // placeholderMemoryCacheKey("thumb_key_from_previous_request")
    raw {
        // 其它 ImageRequest.Builder 能力；勿在此处再设 transformations
    }
}
```

### 内置变换

| 变换 | 说明 |
|------|------|
| `GrayscaleTransformation()` | 灰度 |
| `ColorFilterTransformation(color)` | 颜色叠层 |
| `BorderTransformation(w, color, circle)` | 边框（可配合圆形） |
| `BlurTransformation(radius, sampling)` | 模糊（高版本可硬件加速） |
| `CropTransformation(x, y, w, h)` | 裁切 |
| `WatermarkTransformation(...)` | 水印 |

### 预加载

```kotlin
lifecycleScope.launch {
    val ok: Boolean = ImagePreloader.preload(context, url) { size(200, 200) }
    val list: List<Boolean> = ImagePreloader.preloadAll(context, urls, concurrency = 8) { size(200, 200) }
    val drawable: Drawable? = ImagePreloader.getDrawable(context, url) { size(200, 200) }
}
```

> 预加载与列表展示须 **相同** `size`/变换，否则缓存键不一致。离线时与 `loadImage` 类似，默认不强行走网（见 KDoc）。

### 缓存

```kotlin
AwImage.clearMemoryCache(context)
AwImage.clearDiskCache(context)
val mem = AwImage.getMemoryCacheSize(context)
val disk = AwImage.getDiskCacheSize(context)
val hit = AwImage.isCached(context, url) { size(200, 200) }
```

### Lifecycle 与按 tag 取消

```kotlin
imageView.loadImage(url) { lifecycle(this@MyActivity) }
// 退出时：AwImage.cancelByTag("feed_list")
```

### 离线

默认无网时以缓存为主；`offlineCacheEnabled(false)` 可在离线下仍尝试网络（依 Coil 策略）。

**全局 `AwImage.init` 配置表**（完整默认值见 KDoc `ImageConfig`）：

| 配置 | 方法 | 默认 |
|------|------|------|
| 内存比例 / 最大字节 / 磁盘大小与目录 | `memoryCacheSize` / `memoryCacheMaxSize` / `diskCacheSize` / `diskCacheDir` | 25% / 按比例 / 100MB / `cache/aw_image_cache` |
| 渐入 | `crossfade` | 开，200ms |
| GIF / SVG | `enableGif` / `enableSvg` | true / false |
| 离线网络判定 | `strictNetworkForOffline` | true（要 VALIDATED） |
| 占位 / 错误 / 兜底 | `placeholder` / `error` / `fallback` | 未设 |
| OkHttp | `okHttpClient` | 库默认 + 进度拦截器 |
| 全局请求监听 | `defaultRequestListener` | 无（与单次合并，**先**全局） |
| Log 与 tag | `logTag` / `enableLogging` | `aw-image` / false；`init` 时先 reset 再应用 |

### 从 Glide 迁移（速查）

| Glide | aw-image |
|-------|----------|
| `into(imageView)` | `imageView.loadImage(url)` |
| `placeholder` / `error` | 参数或 DSL |
| `circleCrop()` | `loadCircle` / `circle()` |
| `RoundedCorners` | `loadRounded` / `roundedCorners` |
| `skipMemoryCache(true)` | `disableCache()` 等 |
| 监听 | `onSuccess` / `onError` 或 `listener` |

---

## 故障排除

| 现象 | 处理 |
|------|------|
| `IllegalArgumentException`（override） | 宽高须为正 |
| `onProgress` 无回调 / UI 不刷 | 仅 String URL；子线程 `post` 到 View |
| Release 调不到扩展 | 用 consumer 规则、勿误 shrink |
| 预加载与界面各下一遍 | 统一尺寸与变换或 key |
| 请求 **priority** | Coil 2.7 无此 API；自研队列或升 Coil 3 再评估 |

> Coil 2.x `ImageRequest` 无 **priority**；需优先级时在宿主用拦截器/队列或换 Coil 3+ 自己接。

---

## FAQ

- **`onProgress`？** 仅 `http`/`https` 字符串 + 库内 `OkHttp`；`view.post` 更新 UI。  
- **取消？** `Disposable.dispose()` 或 `tag` + `cancelByTag` 或 `lifecycle`。  
- **`data == null`？** 不请求，走 `fallback` 链。  
- **多次 `init`？** 覆盖全部；每次先重置日志与 tag 再跑块。  
- **RecyclerView 闪动？** 可试 `memoryCacheOnly` + `lifecycle`；根本仍靠合适 `size`。

---

## 发版前检查

- [ ] JDK 17；`ktlint` / `lint` / `demo:assembleRelease` 通过  
- [ ] 真机验证网络图、列表、混淆包  
- [ ] 宿主无 Coil/OkHttp 双版本  
- [ ] 生产关日志；**https**；预加载与展示 **size 一致**  

---

## 维护者：发版与 JitPack

1. 将 [gradle.properties](gradle.properties) 中 `VERSION_NAME` 与 **Git 标签**一致（如 `1.0.0`）。  
2. 推送代码后打标签并推送：  
   `git tag 1.0.0 && git push origin 1.0.0`  
3. 在 [JitPack](https://jitpack.io) 对仓库点 **Get it** 等构建。依赖：`com.github.answufeng:aw-image:标签`  

---

## 许可证

Apache License 2.0，见 [LICENSE](LICENSE)。

*文档与 **1.0.0** 对齐（2026-04-23）。*
