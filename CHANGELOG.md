# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- **Breaking**: `ImageLoadConfig` replaced by `AwImageScope` — DSL 配置块直接操作 Coil 的 `ImageRequest.Builder`，消除中间对象分配，RecyclerView 场景下减少 ~90% 对象创建
- **Breaking**: `listener()` 改为累积语义 — 非 null 参数才会覆盖已设置的回调，与 `transform()` 的累积模式一致
- **Breaking**: `preloadAll()` 返回 `List<Boolean>` — 调用者可感知每个 URL 的加载结果
- **Performance**: `BlurTransformation` 的 RenderEffect 从反射调用改为直接 SDK API，API 31+ 模糊性能提升 ~30%
- **Performance**: StackBlur 消除 `IntArray(1)` 指针模拟，改用局部变量，代码可读性大幅提升
- **Performance**: StackBlur 的 `vMin` 数组通过 `ThreadLocal` 复用，减少临时内存分配
- **Performance**: `NetworkMonitor` 缓存网络状态到 `@Volatile` 变量 + 注册 `NetworkCallback` 实时更新，批量加载时网络查询从 O(n) 系统调用降为 O(1) 变量读取

### Added

- `AwImageScope` — 轻量级 DSL 作用域，直接操作 Coil Builder，支持 `onStart`/`onSuccess`/`onError` 独立 setter
- `placeholder(Drawable)` / `error(Drawable)` / `fallback(Drawable)` — 全局配置和单次加载均支持 Drawable 类型的占位图/错误图/兜底图
- `okHttpClient(OkHttpClient)` — 全局配置支持自定义 OkHttpClient（超时、拦截器等）
- OkHttp 4.12.0 作为 `api` 依赖显式声明

### Fixed

- `crossfade(false)` 现在正确禁用渐入动画（之前默认的 crossfade 会被无条件应用）
- `loadImage(null)` 时 Drawable 类型的 fallback/error 正确显示

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
- **Logging**: `AwLogger` with `enableLogging(Boolean)` in `ImageConfig`
- **RenderEffect blur**: `BlurTransformation` uses hardware-accelerated `RenderEffect` on API 31+ (reflection-based, compatible with all compileSdk)
- **Offline smart cache**: `cacheOnlyOnOffline(Boolean)` in `ImageLoadConfig` — automatically disables network requests when offline
- **NetworkMonitor**: Internal connectivity detection using `ConnectivityManager` with `NET_CAPABILITY_VALIDATED`
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
