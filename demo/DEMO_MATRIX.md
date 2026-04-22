# aw-image Demo 功能矩阵

主界面菜单 **「演示清单」** 可查看摘要。

| 入口按钮 | 目标 | 说明 |
|----------|------|------|
| 网络 / 本地 / 资源 | `BasicLoadActivity` | 多数据源 |
| 圆角 / 滤镜 / 模糊 / 灰度 | `TransformActivity` / `FilterActivity` | 变换与性能注意 |
| 圆形 / 圆角高级 | `AdvancedConfigActivity` | DSL、`raw {}` |
| 占位 / 错误 | `ErrorHandlingActivity` | 失败兜底 |
| 列表 | `RecyclerViewActivity` | 滚动与解码尺寸 |
| 预加载 | `PreloadActivity` | 批量预加载 |
| GIF | `GifActivity` | 动图 |
| 缓存 | `CacheActivity` | 内存/磁盘清理、低内存提示 |
| 集成 | `IntegrationsActivity` | 与 OkHttp 等组合 |

README [内存与列表](../README.md) 与 **缓存页底部提示** 对照阅读。
