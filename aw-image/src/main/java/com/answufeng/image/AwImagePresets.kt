package com.answufeng.image

/**
 * 可复用的 [loadImage] / [ImageView.loadImage] DSL 片段，减少列表缩略图等场景的重复配置。
 *
 * 与 [ImagePreloader.preload]、 [AwImage.isCached] 联用时，请保持**相同**的解码尺寸与变换，
 * 否则缓存键不一致会导致重复下载。
 */
object AwImagePresets {
    /**
     * 列表缩略图：按 [edgePx] 正方形解码（与 `ImagePreloader.preload` 中传入相同 [AwImageScope.override] 以命中缓存）。
     *
     * @param edgePx 边长（px），须 > 0
     * @throws IllegalArgumentException 当 [edgePx] 非法时，由 [AwImageScope.override] 抛出
     */
    fun listThumbnail(edgePx: Int = 200): AwImageScope.() -> Unit =
        {
            override(edgePx, edgePx)
        }

    /**
     * 圆形头像：正方形解码 + 圆形裁切（[ImageView] 须为 1:1 显示区域）。
     *
     * @param edgePx 边长（px），须 > 0
     */
    fun avatarCircle(edgePx: Int = 128): AwImageScope.() -> Unit =
        {
            override(edgePx, edgePx)
            circle()
        }
}
