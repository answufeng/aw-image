# aw-image Demo 功能矩阵

## 首页（目录）

按 **基础 → 外观 → 列表 → 缓存** 分组，共 **9** 个入口。

| 分组 | 页面 | 说明 |
|------|------|------|
| 基础 | 数据源加载 | 网络 URL、Drawable、`Uri`（filesDir）；失败回退 `img_test` |
| 基础 | 占位、错误与重试 | fallback / error / retry |
| 外观 | 形状与变换 | 圆角、圆、滤镜、裁切、水印、对照 |
| 外观 | 圆角与 DSL | loadRounded / loadSquare / onProgress / raw |
| 外观 | GIF | 动图 |
| 列表 | RecyclerView | loadSquare + 预加载 |
| 列表 | 预加载 | 单张 / 批量 |
| 缓存 | 缓存管理 | 内存 / 磁盘清理 |
| 缓存 | 高级能力 | onProgress / onProgressOnMainThread、tag、isCached、SVG |

## UI 约定

- 子页统一：`Toolbar` + 卡片分节（`DemoScaffoldActivity` + `DemoUi`）
- 图片槽位为无圆角、无背景的纯 `ImageView`（`include_demo_image.xml`），避免与 `loadRounded` 等效果冲突
- `loadCircle` / `loadCircleWithBorder` 使用 `addSquareImageSlot`（128×128dp），控件须 1:1 否则圆形会被裁成胶囊形
- 左右对照使用 `addComparisonRow`（圆形对照时 `square = true`）
- 支持日/夜主题（菜单切换）
- 网络图依赖外网（picsum 等），无网时部分页面可能空白；Drawable 演示可用本地 `img_test`

## 手测建议

| 场景 | 操作 |
|------|------|
| 列表 | 进入「RecyclerView」，快速滑动，观察缩略图与状态 |
| 预加载 | 先「预加载全部」，再看列表是否更快命中 |
| 弱网 | 进入「占位、错误与重试」，看重试与 error 图 |
| 缓存 | 「缓存管理」查看占用并清理 |
| 圆形 | 「形状与变换」对照行与 1:1 槽位应为正圆 |
