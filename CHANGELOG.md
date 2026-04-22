# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- **Progress / 并发**: `onProgress` 改为按请求唯一 token 关联，同一 URL 多请求不再共用进度回调；内部头 `X-AwImage-Progress-Token` 在发往服务端前剥离
- **Blur (API 31+)**: `RenderEffectBlur` 在主线程执行，与系统对 `RenderEffect` 的线程要求一致
- **AwImage.init**: `init` 整体同步化，避免并行初始化导致全局状态与 `ImageLoader` 不一致
- **clearMemoryCache / clearDiskCache**: 当 `memoryCache` / `diskCache` 为 null 时返回 `false`，不再误报成功
- **全局 crossfade**: `ImageConfig.crossfade(durationMs)` 在 `durationMs == 0` 时关闭渐入，与 `AwImageScope` 行为一致
- **圆角**: `roundedCorners` 负数半径按 0 处理，避免传入 Coil 变换产生异常结果
- **GIF + SVG**: `init` 合并为单次 `components { }`，避免 Coil 升级时解码器注册被覆盖的风险
- **Memory leak**: `taggedDisposables` 中 Disposable 在请求完成后自动清理，不再持续累积
- **Crossfade duration**: 全局 `crossfadeDuration` 配置现在正确传递给 Coil（之前只传了 `crossfade(true)` 未传时长）
- **BorderTransformation**: `circle=true` 时现在先裁切为圆形再绘制边框，不再露出方形图片

### Changed

- **Breaking**: `loadImage` 参数 `placeholder` 重命名为 `placeholderRes`，与 `errorRes` 命名风格统一
- **Breaking**: `listener()` 文档修正为"覆盖模式"（非 null 参数才会覆盖），与实际行为一致
- **Performance**: `BlurTransformation` 的 RenderEffect 从反射调用改为直接 SDK API，API 31+ 模糊性能提升 ~30%
- **Performance**: StackBlur 消除 `IntArray(1)` 指针模拟，改用局部变量，代码可读性大幅提升
- **Performance**: StackBlur 的 `vMin` 数组通过 `ThreadLocal` 复用，减少临时内存分配
- **Performance**: `ImageNetworkMonitor` 缓存网络状态到 `@Volatile` 变量 + 注册 `NetworkCallback` 实时更新，批量加载时网络查询从 O(n) 系统调用降为 O(1) 变量读取
- **Refactor**: `loadImage` 中 `data==null` 的 fallback 逻辑提取为 `resolveFallback` 方法，消除重复代码

### Added

- `strictNetworkForOffline(Boolean)` / `isStrictNetworkForOffline` — 控制离线仅缓存策略是否要求 `VALIDATED` 网络（默认 `true`；设为 `false` 时仅要求 `INTERNET`，减轻强制门户等场景误判）

- `onStart` / `onSuccess` / `onError` — `AwImageScope` 独立回调方法，无需通过 `listener()` 设置
- `onProgress` — `AwImageScope` 下载进度回调，通过 OkHttp 拦截器实现
- `lifecycle(LifecycleOwner)` — `AwImageScope` Lifecycle 感知加载，页面销毁时自动取消请求
- `AwImage.isCached(context, data)` — 缓存查询 API，检查内存缓存中是否存在指定数据源
- `CropTransformation` — 图片裁切变换
- `WatermarkTransformation` — 水印变换
- `enableSvg(Boolean)` — SVG 解码支持（默认关闭，需添加 `coil-svg` 依赖）
- `applyGlobalCrossfade()` — 全局 crossfade 配置（enabled + duration）统一应用
- Demo: BasicLoadActivity 根据 source extra 切换数据源（网络/本地/资源）
- Demo: MainActivity 按钮跳转传递 focus extra 区分展示内容
- Demo: AdvancedConfigActivity 支持 circle/rounded focus
- Demo: CacheActivity 支持 memory/disk/clear focus
- Demo: RecyclerViewActivity 添加 cancelByTag 演示、Grid 切换、预加载
- Demo: TransformActivity 添加原图 vs 变换对比展示、blur/grayscale focus
- Demo: GifActivity 添加多个 GIF 示例
- Demo: 暗黑模式适配（values-night/colors.xml）

## [1.1.0] - 2025-04-12

### Fixed
- **ProGuard rules**: Corrected package name from `com.ail.brick.image` to `com.answufeng.image`
- **Memory leak**: `BlurTransformation` now properly recycles `scaledBitmap`
- **Concurrency**: `ImagePreloader.preloadAll` now uses `Semaphore` to limit concurrency (default: 8)
- **Exception safety**: `clearMemoryCache` and `clearDiskCache` now return `Boolean` with try-catch protection
- **Crossfade bug**: Global `crossfadeEnabled=false` no longer overridden by `crossfadeDuration`
- **BorderTransformation**: Circle border radius corrected to `min(w,h)/2 - borderWidth`
- **StackBlur**: Alpha channel now properly blurred in both horizontal and vertical passes
- **BorderTransformation**: Added safety check for negative radius when `borderWidth > min(w,h)/2`

### Changed
- **ImageConfig**: All properties now have `private set` to prevent external mutation
- **ImageLoadConfig**: All properties now have `private set`; `scale` now requires setter method
- **loadImage**: Returns `Disposable` consistently (previously returned `null` for null data)
- **transform()**: Now accumulates transformations instead of overwriting
- **roundedCorners**: Uses `hasRoundedCorners` flag instead of `cornerRadius > 0` check
- **Crossfade**: Unified config with `crossfade(Boolean)` and `crossfade(Int)` overloads
- **Removed**: Hilt and KSP declarations (library does not use DI)

### Added
- **Fallback support**: `fallback(resId)` in `ImageLoadConfig` for null data
- **Disk cache directory**: `diskCacheDir(File)` in `ImageConfig`
- **Memory cache size**: `memoryCacheMaxSize(bytes)` in `ImageConfig`
- **Logging**: `AwImageLogger` with `enableLogging(Boolean)` in `ImageConfig`
- **RenderEffect blur**: `BlurTransformation` uses hardware-accelerated `RenderEffect` on API 31+ (reflection-based, compatible with all compileSdk)
- **Offline smart cache**: `cacheOnlyOnOffline(Boolean)` in `ImageLoadConfig` — automatically disables network requests when offline
- **ImageNetworkMonitor**: Internal connectivity detection using `ConnectivityManager` with `NET_CAPABILITY_VALIDATED`
- **Demo XML layouts**: `CacheActivity` and `ErrorHandlingActivity` now use XML layouts
- **ImagePreloader test**: Basic method existence tests
- **Demo**: `CacheActivity` now uses coroutines instead of `Thread`

### Removed
- Sub-module `aw-image/README.md` (consolidated to root README)

## [1.0.0] - 2024-04-11

### Added
- Initial release of aw-image
- Zero-config image loading based on Coil
- DSL API: `loadImage`, `loadCircle`, `loadRounded`, `loadBlur`
- Image preloading: single, batch, get Drawable
- Built-in transformations: Grayscale, ColorFilter, Border (with circle support), Blur
- Cache management: clear memory/disk cache
- GIF support via coil-gif
- Configurable memory/disk cache size
