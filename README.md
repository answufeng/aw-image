# aw-image

[![JitPack](https://jitpack.io/v/answufeng/aw-image.svg)](https://jitpack.io/#answufeng/aw-image)

在 Android 上基于 **Coil 2.7**、面向 **`ImageView` + XML** 的图片加载库：用 Kotlin **DSL** 减样板代码，不屏蔽 Coil 能力；附 **进度、缓存键与 [R8 规则](aw-image/consumer-rules.pro)**。Compose 请直接用官方 `coil-compose`。

| | |
|:---|:---|
| **发布版本** | `1.0.0`（与 [Git 标签](https://github.com/answufeng/aw-image/tags) / JitPack 一致） |
| **环境** | minSdk 24，demo 使用 compileSdk 35、**JDK 17** 构建 |
| **示例** | [demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md) |

**阅读顺序**：下面先 [引入](#依赖) → [三行起步](#三行起步) → 需要时再看 [功能](#功能概览) 与 [进阶](#进阶) → [易踩坑](#易踩坑) / [排错](#排错) 。

---

## 依赖

[JitPack](https://jitpack.io/#answufeng/aw-image) 加仓库后：

```kotlin
// settings.gradle.kts — repositories
maven { url = uri("https://jitpack.io") }

// app — build.gradle.kts
dependencies {
    implementation("com.github.answufeng:aw-image:1.0.0")
}
```

- 对 Coil / `coil-gif` / OkHttp 使用 `api`，一般不必再 `implementation(coil)`；版本冲突时在宿主统一 `coil` / `okhttp` / `okio`。
- Release 请验证图片在 **混淆** 下正常（AAR 已带 consumer 规则）。

**传递版本（参考）**

|  |  |
|--|--|
| Coil / coil-gif / coil-svg | 2.7.0 |
| OkHttp | 4.12.0 |
| kotlinx-coroutines | 1.9.0 |

---

## 三行起步

**1. 不初始化也能用**

```kotlin
imageView.loadImage("https://example.com/photo.jpg")
```

**2. 常见封装**

```kotlin
imageView.loadCircle(avatarUrl)
imageView.loadRounded(url, 24f)   // 或 loadRoundedDp(url, 8f)
imageView.loadSquare(url, edgePx = 200) { roundedCorners(8f) }
imageView.loadWithAspectRatio(url, 16, 9, maxEdgePx = 400)
imageView.loadImage(url, config = AwImagePresets.listThumbnail(200))
```

**3. 全局配置（建议只在 `Application` 调一次）**

```kotlin
AwImage.init(this) {
    memoryCacheSize(0.25)
    diskCacheSize(256L * 1024 * 1024)
    placeholder(R.drawable.placeholder)
    error(R.drawable.error)
    logTag("MyApp-Img")
    enableLogging(BuildConfig.DEBUG)
}
```

每次 `init` 会先把日志关掉、Logcat tag 设回 `aw-image`，再执行块内内容。低内存时可在 `onTrimMemory` 里调用 `AwImage.onApplicationTrimMemory(this, level)`。

---

## 功能概览

| 方向 | 内容 |
|------|------|
| **API** | `loadImage` / `loadCircle` / `loadRounded`·`loadRoundedDp` / `loadCircleWithBorder` / `loadBlur` / `loadSquare` / `loadWithAspectRatio`；`AwImageScope` 直连 `ImageRequest.Builder`，支持 `raw { }` |
| **预设** | `AwImagePresets.listThumbnail`、`avatar` 等，减少重复 `override` |
| **预加载** | `ImagePreloader.preload` / `preloadAll` / `getDrawable`（可限并发） |
| **效果** | 灰度、颜色滤镜、边框、模糊、裁切、水印等 `Transformation` |
| **缓存** | 清内存/磁盘、查占用、`isCached`；可自定义 `memoryCacheKey` / `diskCacheKey` 与磁盘目录 |
| **其它** | GIF；SVG 默认关；占位/错误/兜底；`data == null` 走 fallback 链；离线时优先缓存；`lifecycle` 绑定；`onProgress`（仅 http(s) String，**子线程**回调需 `view.post`）；`defaultRequestListener` 与单次监听合并（先全局） |

---

## 易踩坑

| 不要 | 建议 |
|------|------|
| 列表/大图不限制解码尺寸 | 与显示区域一致，或用 `loadSquare` / `loadWithAspectRatio` / 预设 |
| 预加载和界面用的 **size/变换** 不一致 | 同一套配置或同一套 key，否则重复解码 |
| Release 里长期 `enableLogging(true)` | 用 `BuildConfig.DEBUG` 或短期排查时打开 |

---

## 进阶

**DSL 规则（摘）**

- `override(w, h)` 须 **> 0**；`memoryCacheKey` / `diskCacheKey` 与 `isCached`、预加载要一致才命中。  
- `disableCache` / `memoryCacheOnly` / 各 `CachePolicy` 不要互相矛盾。  
- `raw { }` 在库内变换、离线策略**之后**执行；**别在 `raw` 里再设 `transformations`**，会和 `circle()` / `transform()` 抢。  
- `onProgress` 依赖头 `X-AwImage-Progress-Token`，勿删。`init` 里给的 `Drawable` 已 `mutate()`，只读。  

**一段完整 DSL 示例**

```kotlin
imageView.loadImage(url) {
    placeholder(R.drawable.loading)
    error(R.drawable.fail)
    circle()
    roundedCorners(12f)
    transform(GrayscaleTransformation(), BorderTransformation(2f, Color.WHITE, circle = true))
    crossfade(300)
    override(200, 200)
    lifecycle(this@MyActivity)
    tag("feed_list")
    onStart { }
    onSuccess { }
    onError { }
}
```

**头与 `raw`**

```kotlin
imageView.loadImage(url) {
    addHeader("Authorization", "Bearer $token")
    raw { /* 未封装的 ImageRequest.Builder 项 */ }
}
```

**变换类**

`GrayscaleTransformation` · `ColorFilterTransformation` · `BorderTransformation` · `BlurTransformation` · `CropTransformation` · `WatermarkTransformation`（见源码 KDoc）。

**预加载**

```kotlin
lifecycleScope.launch {
    ImagePreloader.preload(context, url) { size(200, 200) }
    ImagePreloader.preloadAll(context, urls, concurrency = 8) { size(200, 200) }
    ImagePreloader.getDrawable(context, url) { size(200, 200) }
}
```

**缓存 API**

`clearMemoryCache` · `clearDiskCache` · `getMemoryCacheSize` · `getDiskCacheSize` · `isCached(context, data) { size(...) }`

**`AwImage.init` 常用项（完整见 `ImageConfig` KDoc）**

| 方法 / 能力 | 默认 |
|-------------|------|
| `memoryCacheSize` / `memoryCacheMaxSize` / `diskCacheSize` / `diskCacheDir` | 25% 内存 / 按百分比 / 100MB 磁盘 / `cache/…/aw_image_cache` |
| `crossfade` | 开，200ms |
| `enableGif` / `enableSvg` | true / false |
| `strictNetworkForOffline` | true（要 VALIDATED 网络） |
| `placeholder` / `error` / `fallback` | 未设 |
| `okHttpClient` | 内部默认 + 进度拦截器 |
| `defaultRequestListener` | 无（有则与单次请求监听合并，**先**执行全局） |
| `logTag` / `enableLogging` | `aw-image` / false |

**自 Glide 换过来（对照）**  
`into(iv)` → `iv.loadImage`；`placeholder`/`error` 同 DSL；`circleCrop` → `loadCircle`；圆角 → `loadRounded`；`skipMemoryCache` → `disableCache` 等。

---

## 运行与工程

- **Java**：可调用 `ImageView` 扩展，类名多为 `ImageLoadExtensionsKt`；优先 Kotlin。  
- **R8 / ProGuard**：不要整包 `dontshrink` 掉 `com.answufeng.image`；与 Kotlin 元数据保留策略兼容。  
- **列表与内存**：列表项要约束解码大小；[Coil 尺寸](https://coil-kt.github.io/coil/getting_started/#image-size) 可配合 `raw` 设 `precision`；模糊在低端机慎用长列表。  
- **视频帧**：本库未带 `coil-video`；需时在宿主加依赖并注册 `VideoFrameDecoder`。  
- **CI**：[.github/workflows/ci.yml](.github/workflows/ci.yml)（JDK 17 下 `assemble` / `lint` / `ktlint` / `demo:assembleRelease`）。

---

## 排错

| 现象 | 处理 |
|------|------|
| `override` 抛 `IllegalArgumentException` | 宽高用正数 |
| `onProgress` 不回调 / UI 不更新 | 仅 `String` URL；回调可能在子线程，用 `view.post` |
| Release 里扩展「没了」 | 用 AAR 自带 consumer 规则，勿误删 shrink 范围 |
| 同 URL 预加载和界面各下一遍 | 统一 `size`、变换或自定义 key |
| 想要请求 **priority** | Coil 2.7 无此字段；用拦截/队列或升级 Coil 大版本后自接 |

**常见问答**

| 问 | 答 |
|----|----|
| 怎么取消？ | 返回的 `Disposable.dispose()`，或 `tag` + `AwImage.cancelByTag`，或 `lifecycle` |
| `data == null`？ | 不发起请求，只走 fallback 链 |
| 多次 `init`？ | 全量覆盖；且每次先 reset 再应用块内容 |
| RecyclerView 闪图？ | 先管准 `size`；可配合 `memoryCacheOnly` 与 `lifecycle` |

---

## 发版前自检

- [ ] JDK 17，本地/CI：`ktlint`、`lint`、`demo` Release 构建通过  
- [ ] 真机测网络图、列表、**混淆**包  
- [ ] 宿主无多份 Coil/OkHttp 冲突；生产关日志、图片用 **https**；预加载与展示 **同尺寸/同 key**

---

## 维护者（JitPack 发版）

1. [gradle.properties](gradle.properties) 里 `VERSION_NAME` 与打的 **Git 标签**一致。  
2. `git tag <版本> && git push origin <版本>`。  
3. 打开 [JitPack 项目页](https://jitpack.io/#answufeng/aw-image) 对应该标签 **Get it / Build**。  
依赖形式：`com.github.answufeng:aw-image:<标签>`

---

## 许可证

[Apache-2.0](LICENSE) · 文档随 **1.0.0** 更新
