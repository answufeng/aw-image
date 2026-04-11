# aw-image

[![](https://jitpack.io/v/answufeng/aw-image.svg)](https://jitpack.io/#answufeng/aw-image)

基于 Coil 封装的 Android 图片加载库，提供简洁的 DSL API、常用变换和预加载支持。

## ✨ 功能特性

- 零配置：无需初始化即可使用
- DSL API：`loadImage` / `loadCircle` / `loadRounded` / `loadBlur`
- 预加载：单张/批量预加载，获取 Drawable
- 内置变换：灰度/颜色滤镜/边框/高斯模糊
- 缓存管理：内存/磁盘缓存清理
- GIF 支持：内置 coil-gif

## 📦 引入方式

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

> Coil 以 `api` 方式传递，无需额外声明。

## 🚀 快速开始

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
            enableGif(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error)
        }
    }
}
```

## 📖 DSL 配置

```kotlin
imageView.loadImage(url) {
    placeholder(R.drawable.loading)
    error(R.drawable.fail)
    circle()
    roundedCorners(12f)
    crossfade(300)
    override(200, 200)
    noCache()
    transform(
        GrayscaleTransformation(),
        BorderTransformation(2f, Color.WHITE, circle = true)
    )
    listener(
        onStart = { showProgress() },
        onSuccess = { hideProgress() },
        onError = { showRetry() }
    )
}
```

## 🎨 内置变换

| 变换 | 说明 |
|------|------|
| `GrayscaleTransformation()` | 灰度效果 |
| `ColorFilterTransformation(color)` | 颜色滤镜 |
| `BorderTransformation(width, color, circle)` | 边框（支持圆形） |
| `BlurTransformation(radius, sampling)` | 高斯模糊 |

## 📡 预加载

```kotlin
lifecycleScope.launch {
    val success = ImagePreloader.preload(context, url)
    ImagePreloader.preloadAll(context, urls)
    val drawable = ImagePreloader.get(context, url)
}
```

## 🗑️ 缓存管理

```kotlin
AwImage.clearMemoryCache(context)
AwImage.clearDiskCache(context)
```

## 📋 依赖说明

| 依赖 | 版本 | 用途 |
|------|------|------|
| Coil | 2.7.0 | 图片加载引擎 |
| coil-gif | 2.7.0 | GIF 解码支持 |

## 📄 License

```
Copyright 2024 answufeng

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
