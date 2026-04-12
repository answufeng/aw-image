# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
