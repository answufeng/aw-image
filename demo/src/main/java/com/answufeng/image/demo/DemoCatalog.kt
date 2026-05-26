package com.answufeng.image.demo

import android.content.Context
import android.content.Intent

sealed class DemoCatalogRow {
    data class Header(val title: String) : DemoCatalogRow()

    data class Entry(
        val icon: String,
        val title: String,
        val subtitle: String,
        val intent: (Context) -> Intent,
    ) : DemoCatalogRow()
}

object DemoCatalog {

    fun rows(): List<DemoCatalogRow> = listOf(
        DemoCatalogRow.Header("基础"),
        DemoCatalogRow.Entry(
            icon = "🌐",
            title = "数据源加载",
            subtitle = "URL / Drawable / Uri；失败回退 img_test",
        ) { Intent(it, BasicLoadActivity::class.java) },
        DemoCatalogRow.Entry(
            icon = "⚠",
            title = "占位、错误与重试",
            subtitle = "fallback / error / retry / 圆形边框",
        ) { Intent(it, ErrorHandlingActivity::class.java) },

        DemoCatalogRow.Header("外观与变换"),
        DemoCatalogRow.Entry(
            icon = "◆",
            title = "形状与变换",
            subtitle = "圆角、圆形、滤镜、裁切、水印",
        ) { Intent(it, TransformActivity::class.java) },
        DemoCatalogRow.Entry(
            icon = "◎",
            title = "圆角与 DSL",
            subtitle = "loadRounded / loadSquare / onProgress / raw",
        ) { Intent(it, AdvancedConfigActivity::class.java) },
        DemoCatalogRow.Entry(
            icon = "🎞",
            title = "GIF 动图",
            subtitle = "Coil GIF 解码",
        ) { Intent(it, GifActivity::class.java) },

        DemoCatalogRow.Header("列表与性能"),
        DemoCatalogRow.Entry(
            icon = "📋",
            title = "RecyclerView 列表",
            subtitle = "loadSquare + tag 取消 + 预加载",
        ) { Intent(it, RecyclerViewActivity::class.java) },
        DemoCatalogRow.Entry(
            icon = "⬇",
            title = "预加载",
            subtitle = "单张 / 批量，与 UI 同 override",
        ) { Intent(it, PreloadActivity::class.java) },

        DemoCatalogRow.Header("缓存与高级"),
        DemoCatalogRow.Entry(
            icon = "💾",
            title = "缓存管理",
            subtitle = "内存 / 磁盘 / 清理 / 占用",
        ) { Intent(it, CacheActivity::class.java) },
        DemoCatalogRow.Entry(
            icon = "⚙",
            title = "高级能力",
            subtitle = "onProgressOnMainThread、tag、isCached、SVG",
        ) { Intent(it, IntegrationsActivity::class.java) },
    )
}
