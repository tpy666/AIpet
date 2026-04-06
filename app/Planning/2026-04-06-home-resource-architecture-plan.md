# AIpet 首页资源分层重构总结（2026-04-06）

## 1. 本次完成内容

### 1.1 资源分层（总分架构）
已将首页资源从“单文件混放”拆分为“公共层 + Home 子层”：

- 公共层（全局）
  - `res/values/strings.xml`
  - `res/values/colors.xml`
  - `res/values/themes.xml`
  - `res/values/dimens.xml`

- Home 子层（首页专属）
  - `res/values/home_strings.xml`
  - `res/values/home_colors.xml`
  - `res/values/home_styles.xml`
  - `res/values/home_dimens.xml`

### 1.2 首页布局引用迁移
`activity_main.xml` 已迁移为优先使用 Home 子层资源：

- 文案引用：`@string/home_*`
- 颜色引用：`@color/home_*`
- 样式引用：`@style/HomeNavButton`
- 尺寸引用：`@dimen/home_*`

### 1.3 图像资源管理策略
由于 Android `res/drawable` 不支持业务子目录层级，采用“前缀分组”策略：

- 首页背景/容器：`bg_home_*`
- 首页图标占位：`ic_home_*`

该策略可实现“逻辑分组 + 搜索友好 + 可批量替换 PNG”。

---

## 2. 当前资源架构约定

### 2.1 命名规范
- 首页字符串：`home_*`
- 首页颜色：`home_*`
- 首页尺寸：`home_*`
- 首页样式：`Home*` 或 `home_*`
- 首页 drawable：`bg_home_*`、`ic_home_*`

### 2.2 代码与资源引用边界
- `MainActivity`、`activity_main.xml` 只依赖 `home_*` 资源，不直接写硬编码 UI 文案。
- 全局主题仍在 `themes.xml`，页面特化样式优先放 `home_styles.xml`。

---

## 3. 现存风险与优化方向

### 3.1 风险
- 目前首页仍有部分尺寸硬编码（少量 padding/textSize），建议继续迁移到 `home_dimens.xml`。
- 商店与换装图标目前为占位资源，未完成实际 PNG 接入和状态图（normal/pressed/disabled）。

### 3.2 建议优先级
1. **P0（稳定性）**
   - 接入崩溃采集（本地日志 + release 监控）
   - 给定位、天气、聊天网络链路增加超时和重试状态 UI

2. **P1（资源治理）**
   - 将首页所有 textSize、radius、margin 100% 收敛到 `home_dimens.xml`
   - 新增 `home_drawable_mapping.md` 记录每个按钮对应 PNG 文件名规范

3. **P1（多语言）**
   - 新建 `values-zh-rCN/home_strings.xml` 与 `values-en/home_strings.xml`
   - 优先翻译顶部信息、导航、提示文案

4. **P2（UI可维护性）**
   - 为首页按钮建立统一组件样式：
     - `HomeSideActionButton`
     - `HomeInputActionButton`
   - 减少布局重复属性

5. **P2（业务能力）**
   - 商店服饰接入真实商品数据结构（id、name、price、icon、rarity）
   - 换装持久化扩展到“宠物 + 场景 + 装备位”多维模型

---

## 4. 后续开发路线（建议迭代节奏）

### 里程碑 M1：资源与样式治理（1~2 天）
- 完成首页 100% 资源引用收敛
- 完成占位图到 PNG 的无代码替换接入
- 输出资源命名文档

### 里程碑 M2：交互与状态完善（2~3 天）
- 聊天发送/失败/loading 统一状态反馈
- 天气/定位失败兜底文案与重试入口
- 商店-换装-好感度联动规则细化

### 里程碑 M3：国际化与可扩展（2 天）
- 中英文资源拆分与校验
- 首页组件化（可迁移至 Fragment/Compose 的过渡层）

---

## 5. 验收清单（下一轮可直接对照）
- [ ] 首页无硬编码中文文案
- [ ] 首页无硬编码尺寸
- [ ] 首页按钮图标全部可通过替换 PNG 完成换皮
- [ ] Home 资源只存在于 `home_*` 文件
- [ ] 全局资源只保留跨页面通用项
- [ ] Debug 构建 + 启动 + 主要交互（聊天/商店/换装/天气）全链路通过
