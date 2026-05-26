# aw-image

[![JitPack](https://jitpack.io/v/answufeng/aw-image.svg)](https://jitpack.io/#answufeng/aw-image)

基于 **Coil 2.7** 的 Android 图片加载基础库：面向 **传统 XML 布局 + `ImageView`**（`findViewById` / ViewBinding），提供更顺手的 Kotlin **DSL**、常用预设、预加载、缓存与进度回调等能力；同时保留 Coil 原生可扩展性（可用 `raw { }` 直配 `ImageRequest.Builder`）。

> **不支持 Jetpack Compose**（无 `AsyncImage` 等封装）。Compose 项目请直接使用 [Coil](https://coil-kt.github.io/coil/)。

如果你只想最快接入并跑通第一张图，直接看下面的「5 分钟上手」即可；其它内容都可以后置按需查阅。

| | |
|:--|:--|
| **版本** | `1.1.0`（[JitPack](https://jitpack.io/#answufeng/aw-image)） |
| **仓库** | [github.com/answufeng/aw-image](https://github.com/answufeng/aw-image) |
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
    implementation("com.github.answufeng:aw-image:1.1.0")
}
```

依赖版本号与 JitPack 构建 **tag** 一致（上例为 `1.1.0`）：`git tag 1.1.0 && git push origin 1.1.0` 后在 JitPack 页 Build。

### 2) 加载第一张图

```kotlin
// findViewById
findViewById<ImageView>(R.id.cover).loadImage("https://example.com/photo.jpg")

// ViewBinding
binding.cover.loadImage("https://example.com/photo.jpg")
```

### 3) （可选）全局初始化

不调用 `AwImage.init` 也能加载（走 Coil 默认 `ImageLoader`）；需要统一占位图/缓存/日志等时再配。

请在 **`Application.onCreate` 中调用一次**，传入 `applicationContext`：

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AwImage.init(this) {
            memoryCacheSize(0.25)
            diskCacheSize(256L * 1024 * 1024)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error)
            logTag("MyApp-Img")
            enableLogging(BuildConfig.DEBUG)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AwImage.onApplicationTrimMemory(this, level)
    }
}
```

<details>
<summary><b>与 Coil 的关系（点击展开）</b></summary>

- 本库在 Coil 之上封装 `ImageView` 扩展与 DSL；依赖里已 `api` 传递 **Coil / OkHttp**，多数项目无需再写 `implementation(coil)`。
- **未**调用 `AwImage.init`：使用 Coil 默认全局 `ImageLoader`。
- **已**调用 `AwImage.init`：替换为库内配置的 `ImageLoader`（缓存、GIF、进度拦截等）。
- Coil 未封装的项用 DSL 的 `raw { }` 直接写 `ImageRequest.Builder`。

</details>

<details>
<summary><b>依赖与传递版本说明（点击展开）</b></summary>

- 多模块冲突时请在宿主**统一** `coil` / `okhttp` / `okio` 版本。
- **Release** 请在混淆包上点一遍图片与列表；AAR 已含 [consumer-rules.pro](aw-image/consumer-rules.pro)。

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
| 最短时间跑通依赖与加载 | [5 分钟上手](#5-分钟上手最小接入) · [环境要求](#环境要求) |
| 常用 API / DSL / 预加载 | [API 速查](#api-速查) |
| 进度、取消、预设 | [进度与取消](#进度与取消) · [预设](#预设) |
| 别踩雷（列表、尺寸、圆形等） | [易踩坑](#易踩坑强烈建议先看) |
| 能力总览 | [能做什么](#能做什么概览) |
| 更深的规则与高级用法 | [进阶](#进阶) |
| Java / R8 / CI | [工程与平台](#工程与平台) |
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

### 数据源（`loadImage` 的 `data`）

| 类型 | 示例 |
|------|------|
| 网络 URL | `imageView.loadImage("https://example.com/a.jpg")` |
| Drawable 资源 | `imageView.loadImage(R.drawable.avatar)` |
| 本地文件 | `imageView.loadImage(File("/path/photo.jpg"))` |
| Uri | `imageView.loadImage(contentUri)` |
| `null` | 不发起请求，走 DSL / 全局 `fallback` 链 |

`File`、`content://` Uri 等与 Coil 一致。Demo 演示 **URL、Drawable、应用内 Uri**（见 [demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md)）。

### 常用扩展

```kotlin
imageView.loadRounded(url, 24f)                    // 或 loadRoundedDp(url, 8f)
imageView.loadCircle(avatarUrl)                    // ImageView 须 1:1，见易踩坑
imageView.loadCircleWithBorder(avatarUrl, 4f, Color.WHITE)
imageView.loadBlur(url, radius = 15, sampling = 4)
imageView.loadSquare(url, edgePx = 200) { roundedCorners(8f) }
imageView.loadWithAspectRatio(url, 16, 9, maxEdgePx = 400)
```

### 预设

```kotlin
imageView.loadImage(url, config = AwImagePresets.listThumbnail(200))
imageView.loadImage(avatarUrl, config = AwImagePresets.avatarCircle(128))  // override + circle，须 1:1 控件
```

### DSL 一眼能抄的例子

```kotlin
imageView.loadImage(url) {
    placeholder(R.drawable.loading)
    error(R.drawable.fail)
    roundedCorners(12f)
    crossfade(300)
    override(200, 200)           // 列表/缩略图建议明确解码尺寸
    lifecycle(this@MyActivity)
    retry(2)
    retryOnNetworkReconnect()    // 建议同时 lifecycle(...)，否则可能无法自动清理监听
    onError { /* ... */ }
}
```

### 进度与取消

```kotlin
imageView.loadImage(url) {
    // 回调可能在子线程
    onProgress { current, total -> progressBar.post { /* 更新 UI */ } }
    // 或固定派发到主线程
    onProgressOnMainThread { current, total -> progressBar.progress = current.toInt() }

    tag("feed_list")
}
// 页面退出或刷新时
AwImage.cancelByTag("feed_list")
```

仅 **http(s) 的 String** URL 会走下载进度（内部 `ProgressInterceptor`）。

### 预加载 / 读缓存 / 清缓存

```kotlin
lifecycleScope.launch {
    // 与 loadImage 使用相同 AwImageScope（override、变换、cacheKey），才能命中同一缓存
    ImagePreloader.preload(context, url) { override(200, 200) }
    ImagePreloader.preloadAll(context, urls, concurrency = 8) { override(200, 200) }
    ImagePreloader.getDrawable(context, url) { override(200, 200) }  // 未命中会触发加载
}

AwImage.isCached(context, url) { override(200, 200) }
AwImage.getMemoryCacheSize(context)
AwImage.getDiskCacheSize(context)   // 建议在后台线程调用
AwImage.clearMemoryCache(context)
AwImage.clearDiskCache(context)
```

---

## 易踩坑（强烈建议先看）

| 情况 | 建议 |
|------|------|
| 列表/大图不限制**解码尺寸** | 与控件展示尺寸对齐；用 `loadSquare` / `loadWithAspectRatio` 或 [AwImagePresets](aw-image/src/main/java/com/answufeng/image/AwImagePresets.kt) |
| **预加载**和界面加载的 size / 变换不一致 | 同一套 `ImageRequest` 相关配置，否则易重复下载、重复解码 |
| `loadCircle` / `loadCircleWithBorder` | **ImageView 须 1:1**（如 128dp×128dp），否则呈胶囊形裁切；可用 `AwImagePresets.avatarCircle` |
| `retryOnNetworkReconnect` | 建议配合 **`lifecycle(owner)`**，并从 Activity/Fragment 的 `Context` 加载 |
| Release **长期**打开详细日志 | 用 `BuildConfig.DEBUG` 或仅排障时短期 `enableLogging(true)` |

---

## 能做什么（概览）

| 类别 | 说明 |
|------|------|
| 扩展入口 | `loadImage`、`loadCircle`、`loadRounded` / `loadRoundedDp`、`loadCircleWithBorder`、`loadBlur`、`loadSquare`、`loadWithAspectRatio` |
| DSL | `AwImageScope`；`raw { }` 补 Coil 未封装项 |
| 预设 | `listThumbnail`、`avatarCircle` |
| 预加载 | `ImagePreloader`：单张、批量、`getDrawable` |
| 变换 | 灰度、色滤、边框、模糊、裁切、水印等（见 [进阶](#进阶)） |
| 缓存 | `isCached`、占用查询、清内存/盘；`memoryCacheKey` / `diskCacheKey` |
| 其它 | GIF；SVG 默认关；占位/错图/fallback；`lifecycle`；`tag` / `cancelByTag`；`onProgress` / `onProgressOnMainThread` |

---

## 进阶

**规则摘要**

- `override(w, h)` 宽高须为**正整数**。自定义 cacheKey 时与 `isCached`、预加载**同一套**才命中。
- `disableCache` / `memoryCacheOnly` / 各 `CachePolicy` 勿互相矛盾。
- 库先应用 `transform` / `circle` 等，再执行 `raw { }`；**勿在 `raw` 里再设 `transformations`**。
- `onProgress` 与 `onProgressOnMainThread` **互斥**，后调用者覆盖前者。
- `init` 里配置的 Drawable 已 `mutate()`，请**只读**使用。

<details>
<summary><b>DSL 示例：圆角 / 圆形 / 变换（点击展开）</b></summary>

```kotlin
// 圆角（二选一，勿与 circle() 混用于同一请求）
imageView.loadImage(url) {
    roundedCorners(12f)
    override(200, 200)
}

// 圆形头像（控件须 1:1）
imageView.loadImage(avatarUrl) {
    circle()
    override(128, 128)
}

// 组合变换
imageView.loadImage(url) {
    transform(GrayscaleTransformation())
    crossfade(300)
    lifecycle(this@MyActivity)
    tag("detail")
    listener(
        onStart = { },
        onSuccess = { },
        onError = { },
    )
}
```

</details>

<details>
<summary><b>请求头与 <code>raw</code></b></summary>

```kotlin
imageView.loadImage(url) {
    addHeader("Authorization", "Bearer $token")
    raw { /* 其余 ImageRequest.Builder 配置 */ }
}
```

</details>

<details>
<summary><b>变换类（点击展开）</b></summary>

`GrayscaleTransformation`、`ColorFilterTransformation`、`BorderTransformation`、`BlurTransformation`、`CropTransformation`、`WatermarkTransformation`（详见各类 KDoc）。

</details>

<details>
<summary><b>预加载与缓存（补充）</b></summary>

预加载/清理代码见 [API 速查 · 预加载](#预加载--读缓存--清缓存)。补充：

- `AwImage.cancelAllTaggedRequests()`：取消当前进程内所有已登记 tag 的请求（调试或进程级清理用）。
- 离线策略：`offlineCacheEnabled`（默认 true）在无网时倾向只读缓存。

</details>

<details>
<summary><b><code>AwImage.init</code> 常用项（完整见 <code>ImageConfig</code> KDoc）</b></summary>

| 配置 | 默认 |
|------|------|
| `memoryCacheSize` / `memoryCacheMaxSize` / `diskCacheSize` / `diskCacheDir` | 如 25% 内存、磁盘约 100MB、`cache/aw_image_cache` |
| `crossfade` | 开，200ms |
| `enableGif` / `enableSvg` | true / false |
| `strictNetworkForOffline` | true |
| `placeholder` / `error` / `fallback` | 未设 |
| `okHttpClient` | 默认客户端 + 进度拦截（避免重复添加） |
| `defaultRequestListener` | 无；与单次 `listener` 合并，**先**跑全局 |
| `logTag` / `enableLogging` | `aw-image` / false |

</details>

---

## 工程与平台

### Java

```java
ImageLoadExtensionsKt.loadImage(imageView, "https://example.com/a.jpg", 0, 0, null);
// 带 DSL 需用 AwImageScope 的 SAM / 在 Kotlin 侧封装 config 块
```

### Kotlin / R8 / 其它

- **R8**：勿整段 `dontshrink` 掉 `com.answufeng.image`；AAR 已带 consumer 规则。
- **列表与内存**：必须约束**解码**尺寸；详见 [Coil · Image size](https://coil-kt.github.io/coil/getting_started/#image-size)。长列表慎用重模糊。
- **视频帧**：本 AAR 不含 `coil-video`；宿主自行添加依赖并注册 `VideoFrameDecoder`。
- **CI**：[.github/workflows/ci.yml](.github/workflows/ci.yml)

---

## 常见问题

**现象与处理**

| 现象 | 处理 |
|------|------|
| `override` 报非法参数 | 宽、高用正整数 |
| `onProgress` 没有或 UI 不刷 | 仅 **String** 的 **http(s)**；用 **`onProgressOnMainThread`** 或 `view.post` |
| Release 里扩展找不到 | 确认 consumer 规则生效 |
| 预加载和界面各拉一次 | 统一 **size / 变换 / cacheKey** |
| 需要请求 **priority** | Coil 2.7 无统一 API；业务层排队或查 Coil 文档 |
| **Jetpack Compose** | 请直接用 Coil |

**短答**

| 问 | 答 |
|----|----|
| 怎么取消？ | `Disposable.dispose()`、`tag` + `AwImage.cancelByTag`、`lifecycle` |
| `data == null`？ | 不发起请求，走 fallback 链 |
| 多次 `AwImage.init`？ | 全量覆盖；每次先 reset 再应用新配置 |
| RecyclerView 闪动？ | 对齐尺寸与缓存键；可试 `memoryCacheOnly` + `lifecycle` |

---

## 许可证

[Apache-2.0](LICENSE)
