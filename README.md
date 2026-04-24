# aw-image

[![JitPack](https://jitpack.io/v/answufeng/aw-image.svg)](https://jitpack.io/#answufeng/aw-image)

基于 **Coil 2.7** 的 Android 图片加载基础库：面向 **View/XML 的 `ImageView`**，提供更顺手的 Kotlin **DSL**、常用预设、预加载、缓存与进度回调等能力；同时保留 Coil 原生可扩展性（可用 `raw { }` 直配 `ImageRequest.Builder`）。

如果你只想最快接入并跑通第一张图，直接看下面的「5 分钟上手」即可；其它内容都可以后置按需查阅。

| | |
|:--|:--|
| **当前版本** | `1.0.0`（[Git 标签](https://github.com/answufeng/aw-image/tags) / JitPack 同名） |
| **范围** | minSdk **24**；本仓库用 compileSdk 35、**JDK 17** 跑 CI / demo |
| **示例** | 见 [demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md) |

---

## 5 分钟上手（最小接入）

### 1) 添加依赖（JitPack）

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
        google()
        mavenCentral()
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.answufeng:aw-image:1.0.0")
}
```

`implementation` 中的 **版本号与 Git / JitPack 的 tag 一致**（上例为 `1.0.0`）。

### 2) 加载第一张图

```kotlin
imageView.loadImage("https://example.com/photo.jpg")
```

### 3) （可选）全局初始化

不调用 `AwImage.init` 也能加载；需要统一占位图/缓存/日志等时再配。

```kotlin
AwImage.init(this) {
    memoryCacheSize(0.25)
    diskCacheSize(256L * 1024 * 1024)
    placeholder(R.drawable.placeholder)
    error(R.drawable.error)
    logTag("MyApp-Img")
    enableLogging(BuildConfig.DEBUG)
}
// 低内存时可在 onTrimMemory 中：AwImage.onApplicationTrimMemory(this, level)
```

<details>
<summary><b>依赖与传递版本说明（点击展开）</b></summary>

- 本库对 Coil / `coil-gif` / OkHttp 使用 `api`，多数项目**不必**再写 `implementation(coil)`。
- 多模块冲突时请在宿主**统一** `coil` / `okhttp` / `okio` 版本。
- **Release** 请在混淆包上点一遍图片与列表；AAR 已含 consumer 规则。

| 组件 | 版本 |
|------|------|
| Coil、coil-gif、coil-svg | 2.7.0 |
| OkHttp | 4.12.0 |
| kotlinx-coroutines | 1.9.0 |

</details>

---

## 目录（按常见需求跳转）

| 想做什么 | 跳转到 |
|----------|--------|
| 最短时间跑通依赖与加载 | [5 分钟上手（最小接入）](#5-分钟上手最小接入) · [环境要求](#环境要求) |
| 常用 API / DSL / 预加载 | [API 速查](#api-速查) |
| 别踩雷（列表、尺寸、进度回调等） | [易踩坑](#易踩坑强烈建议先看) |
| 能力列表（缓存、变换、预设等） | [能做什么](#能做什么概览) |
| 更深的规则与高级用法 | [进阶](#进阶) |
| Java / R8 / CI 等工程信息 | [工程与平台](#工程与平台) · [发版与维护](#发版与维护) |
| 常见问题 | [常见问题](#常见问题) |

---

## 环境要求

| 项目 | 最低版本 |
|------|----------|
| Android minSdk | 24 |
| 本仓库 compileSdk（验证用） | 35 |
| JDK（仅本仓库 / demo） | 17 |
| Coil（库内对齐） | 2.7.x |

> 接入 AAR 只要你的项目能正常使用 Android / Kotlin 即可；JDK 17 主要是为了能编译本仓库工程（AGP 8.x 的要求）。

---

## API 速查

### 常用扩展

```kotlin
imageView.loadCircle(avatarUrl)
imageView.loadRounded(url, 24f)                    // 或 loadRoundedDp(url, 8f)
imageView.loadSquare(url, edgePx = 200) { roundedCorners(8f) }
imageView.loadWithAspectRatio(url, 16, 9, maxEdgePx = 400)
imageView.loadImage(url, config = AwImagePresets.listThumbnail(200))
```

### DSL 一眼能抄的例子

```kotlin
imageView.loadImage(url) {
    placeholder(R.drawable.loading)
    error(R.drawable.fail)
    roundedCorners(12f)
    crossfade(300)
    // 列表场景建议明确解码尺寸（示例值按需改）
    override(200, 200)
    onError { /* ... */ }
}
```

### 预加载 / 读缓存 / 清缓存

```kotlin
lifecycleScope.launch {
    ImagePreloader.preload(context, url) { size(200, 200) }
    ImagePreloader.preloadAll(context, urls, concurrency = 8) { size(200, 200) }
    ImagePreloader.getDrawable(context, url) { size(200, 200) }
}

AwImage.clearMemoryCache(context)
AwImage.clearDiskCache(context)
AwImage.isCached(context, url) { size(200, 200) }
```

---

## 易踩坑（强烈建议先看）

| 情况 | 建议 |
|------|------|
| 列表/大图不限制**解码尺寸** | 和控件展示尺寸对齐；用 `loadSquare` / `loadWithAspectRatio` 或 [AwImagePresets](aw-image/src/main/java/com/answufeng/image/AwImagePresets.kt) |
| **预加载**和界面加载的 size / 变换不一致 | 同一套 `ImageRequest` 相关配置，否则易重复下、重复解 |
| Release **长期**打开详细日志 | 用 `BuildConfig.DEBUG` 或仅排障时短期 `enableLogging(true)` |

---

## 能做什么（概览）

| 类别 | 说明 |
|------|------|
| 扩展入口 | `loadImage`、`loadCircle`、`loadRounded` / `loadRoundedDp`、`loadCircleWithBorder`、`loadBlur`、`loadSquare`、`loadWithAspectRatio` |
| DSL | `AwImageScope` 直配 `ImageRequest.Builder`，`raw { }` 可写 Coil 里未再封一层的项 |
| 预设 | `AwImagePresets.listThumbnail`、`avatar` 等，少写重复 `override` |
| 预加载 | `ImagePreloader`：单张、批量、并发上限、`getDrawable` |
| 变换 | 灰度、色滤、边框、模糊、裁切、水印等（见包内 `*Transformation`） |
| 缓存与键 | 清内存/盘、查占用、`isCached`；`memoryCacheKey` / `diskCacheKey`、磁盘目录可配 |
| 其它 | GIF；SVG 默认关；占位/错图/兜底；`data==null` 只走 fallback；无网时偏缓存；`lifecycle`；`onProgress`（仅 **http(s) 的 String**，回调可能在子线程 → **`view.post`**）；`defaultRequestListener` 与单次监听合并，**先全局** |

---

## 进阶

**规则摘要**

- `override(w,h)` 的宽高须 **&gt; 0**。自定义 key 时与 `isCached`、预加载**同一套**，才命中。  
- `disableCache` / `memoryCacheOnly` / 各 `CachePolicy` 勿互相打架。  
- 库在应用 `transform` / `circle` 等**之后**才跑 `raw { }`；**勿在 `raw` 里再设 `transformations`**。  
- `onProgress` 依赖内部头 `X-AwImage-Progress-Token`。**`init` 里给的 Drawable 已 `mutate()`，只读。**

<details>
<summary><b>完整 DSL 示例（点击展开）</b></summary>

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

</details>

<details>
<summary><b>请求头与 <code>raw</code></b></summary>

```kotlin
imageView.loadImage(url) {
    addHeader("Authorization", "Bearer $token")
    raw { /* 其余 ImageRequest.Builder 配置，勿在此处再设 transformations */ }
}
```

</details>

**变换类名**：`GrayscaleTransformation`、`ColorFilterTransformation`、`BorderTransformation`、`BlurTransformation`、`CropTransformation`、`WatermarkTransformation`（见 KDoc）。

<details>
<summary><b>预加载与缓存（代码片段）</b></summary>

```kotlin
lifecycleScope.launch {
    ImagePreloader.preload(context, url) { size(200, 200) }
    ImagePreloader.preloadAll(context, urls, concurrency = 8) { size(200, 200) }
    ImagePreloader.getDrawable(context, url) { size(200, 200) }
}

// 清理与查询
AwImage.clearMemoryCache(context)
AwImage.clearDiskCache(context)
AwImage.isCached(context, url) { size(200, 200) }
```

</details>

<details>
<summary><b><code>AwImage.init</code> 常用项（完整见 <code>ImageConfig</code> KDoc）</b></summary>

| 配置 | 默认 |
|------|------|
| `memoryCacheSize` / `memoryCacheMaxSize` / `diskCacheSize` / `diskCacheDir` | 如 25% 内存、磁盘约 100MB、目录在 cache 下 `aw_image_cache` 等 |
| `crossfade` | 开，200ms |
| `enableGif` / `enableSvg` | true / false |
| `strictNetworkForOffline` | true（与「是否已验证网络」等策略相关） |
| `placeholder` / `error` / `fallback` | 未设 |
| `okHttpClient` | 库内默认 + 下载进度拦截 |
| `defaultRequestListener` | 无；若设，与**单次**监听合并，**先**跑全局 |
| `logTag` / `enableLogging` | `aw-image` / false；每次 `init` 先 reset 再套本次块 |

</details>

**从 Glide 换过来（对照）**  
`into(imageView)` → `imageView.loadImage`；`placeholder` / `error` 用法类似；`circleCrop` → `loadCircle`；圆角 → `loadRounded`；`skipMemoryCache` 一类需求 → `disableCache` 等。

---

## 工程与平台

- **Java**：可调用 `ImageView` 上的扩展，生成类名常见为 **`ImageLoadExtensionsKt`**。  
- **R8**：勿整段 `dontshrink` 掉 `com.answufeng.image`；与 Kotlin/Coil 的 keep 策略一并考虑。  
- **列表与内存**：必须约束**解码**尺寸；需要时再打开 [Coil 文档 · 尺寸](https://coil-kt.github.io/coil/getting_started/#image-size) 用 `raw` 等配 `precision`。长列表慎用重模糊。  
- **视频帧封面**：本 AAR 不含 `coil-video`；在宿主加依赖并注册 `VideoFrameDecoder` 即可与 Coil 一致使用。  
- **CI**：[.github/workflows/ci.yml](.github/workflows/ci.yml)（JDK 17 下 `assemble` / `lint` / `ktlint` / `demo:assembleRelease`）。

---

## 常见问题

**现象与处理**

| 现象 | 处理 |
|------|------|
| `override` 报非法参数 | 宽、高用正整数 |
| `onProgress` 没有或 UI 不刷 | 仅 **String** 的 **http(s)** 会走；回调可能在子线程，**`view.post`** 更新 View |
| Release 里扩展找不到 | 确认 consumer 规则生效、勿误伤 shrink |
| 预加载和界面各拉一次 | 统一 **size/变换/自定义 key** |
| 需要**请求 priority** | Coil 2.7 无统一 priority API；在业务层排队或升大版本后按 Coil 新 API 自接 |

**短答**

| 问 | 答 |
|----|----|
| 怎么取消？ | `Disposable.dispose()`，或 `tag` + `AwImage.cancelByTag`，或绑定 `lifecycle` |
| `data == null`？ | 不发起请求，只走全局/DSL 的 fallback 链 |
| 多次 `AwImage.init`？ | 全量覆盖；且每次**先** reset 日志与 tag 再套本次块 |
| RecyclerView 闪动？ | 先保证**尺寸/缓存键**；可试 `memoryCacheOnly` + `lifecycle` |

---

## 发版与维护

**集成方发版前可自检**

- [ ] JDK 17；`ktlint` / `lint` / `demo:assembleRelease` 过  
- [ ] 真机看网络图、列表、**混淆**包  
- [ ] 无重复 Coil/OkHttp；生产关日志；图床 **https**；预加载与展示 **同键**

**本库在 JitPack 发新版的常规步骤**

1. [gradle.properties](gradle.properties) 里 `VERSION_NAME` 与 **Git 标签**一致。  
2. `git tag <x.y.z> && git push origin <x.y.z>`。  
3. 打开 [JitPack](https://jitpack.io/#answufeng/aw-image) 对应该标签 **Build / Get**。

依赖形式：`com.github.answufeng:aw-image:<标签>`

---

## 许可证

[Apache-2.0](LICENSE) · 文档与 **`1.0.0`** 同步
