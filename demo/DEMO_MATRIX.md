# aw-image Demo 功能矩阵

主界面菜单 **「演示清单」** 可查看摘要。

| 入口按钮 | 目标 | 说明 |
|----------|------|------|
| 网络 / 本地 / 资源 | `BasicLoadActivity` | 多数据源 |
| 圆角 / 滤镜 / 模糊 / 灰度 | `TransformActivity` / `FilterActivity` | 变换与性能注意 |
| 圆形 / 圆角高级 | `AdvancedConfigActivity` | DSL、`raw {}`、`onProgress`（`post` 更新 UI）、`loadSquare` / `loadWithAspectRatio` |
| 占位 / 错误 | `ErrorHandlingActivity` | 失败兜底 |
| 列表 | `RecyclerViewActivity` | 滚动与解码尺寸 |
| 预加载 | `PreloadActivity` | 批量预加载 |
| GIF | `GifActivity` | 动图 |
| 缓存 | `CacheActivity` | 内存/磁盘清理、低内存提示 |
| 集成 | `IntegrationsActivity` | 与 OkHttp 等组合 |

README [内存与列表](../README.md) 与 **缓存页底部提示** 对照阅读。

## 推荐手测（边界与极端场景）

| 场景 | 建议操作 |
|------|----------|
| 列表 | 快速滑动大图列表，观察解码尺寸与闪烁（对照 README） |
| 弱网 | 仅蜂窝/飞行模式切换，看占位与缓存命中 |
| 低内存 | 开发者选项后台进程限制 + `onTrimMemory` 与手动清缓存 |
| GIF / SVG | 若业务启用，在低端机各跑一轮 |
