# 糖尿病血糖助手 App — 开发任务清单 (TODO)

> 基于 `implementation_plan.md` (V4) 拆解，按开发顺序排列。
> 每完成一项主要任务（一级阶段）需提交人工审查，审查通过后才可进入下一阶段。
> 新增全局要求：每个页面与子页面必须提供可独立运行的 `@Preview`（含关键状态预览），用于逐页 UI 审查。

---

## 阶段一：项目初始化与基础架构 🏗️

### 1.1 项目创建
- [x] 创建 Android 项目（Kotlin + Jetpack Compose）
- [x] 配置 `minSdk`、`targetSdk`、`compileSdk`
- [x] 配置项目包名

### 1.2 依赖引入
- [x] Jetpack Compose + Material 3
- [x] Navigation Compose
- [x] Room Database
- [x] Retrofit + OkHttp
- [x] CameraX
- [x] Coil（图片加载）
- [x] Vico（图表库）
- [x] Hilt（依赖注入）
- [x] DataStore

### 1.3 项目模块结构搭建
- [x] 创建 `ui/` 及子目录：theme, navigation, onboarding, auth, home, archive, camera, profile, post, components
- [x] 创建 `data/` 及子目录：local, remote, repository, model
- [x] 创建 `domain/` 及子目录：algorithm, usecase
- [x] 创建 `di/` 目录

### 1.4 主题系统
- [x] 定义主题色板（#C2E05E / #A8D4B9 / #6BBF8B / #4B9B6E / #2E7E5D / #1A5F3A）
- [x] 配置 Material 3 ColorScheme、Typography、Shapes
- [x] 实现深色模式三选一（跟随系统/浅色/深色）— AppThemeMode 枚举
- [x] 现代极简 + 玻璃拟态点缀 + 扁平化 icon

### ✅ 阶段一审查节点

---

## 阶段二：数据层 💾

### 2.1 Room DB 实体与 DAO
- [x] Entity: User（用户信息）
- [x] Entity: HealthProfile（糖尿病类型、胰岛素类型、用量范围、体重、身高）
- [x] Entity: FoodRecord（图片路径、日期、菜品名、AI JSON、状态）
- [x] Entity: CommunityPost（标题、内容、图片、血糖标签、点赞评论数）
- [x] Entity: Comment（内容、用户ID、帖子ID、时间）
- [x] Entity: Favorite（用户ID、帖子ID）
- [x] Entity: CookingMethodTable（烹饪方式、修正值）
- [x] Entity: UsdaCache（原料名、基础碳水值）
- [x] 创建对应 DAO 接口
- [x] 预填充烹饪修正表（蒸/煮+0, 凉拌+1, 煲汤+1, 炒+2, 烧烤+2, 爆炒+3, 干煸+3, 红烧+5, 油炸+6, 糖醋+8）

### 2.2 DataStore 偏好
- [x] 登录状态、深色模式偏好、Onboarding完成状态

### 2.3 Model 类
- [x] AI 返回数据模型（DishRecognitionResult, Dish, Ingredient）
- [x] 血糖计算参数模型
- [x] 胰岛素色带参数模型
- [x] 胰岛素范围常量对象（速效/短效/中效/长效 min/max/起效/峰值/持续）

### 2.4 远程 API 客户端
- [x] DashScope API（Qwen VL）Retrofit 接口 → 阶段六替换为 DeepSeek API
- [x] USDA API Retrofit 接口
- [x] API Key 安全存储
- [x] 网络错误处理与重试

### 2.5 Repository 层
- [x] UserRepository（登录注册、用户信息）
- [x] FoodRecordRepository（档案 CRUD）
- [x] CommunityRepository（帖子/评论/收藏 CRUD）
- [x] AiRepository（Qwen VL 调用封装 → 阶段六替换为 DeepSeek）
- [x] NutritionRepository（USDA 查询 + 缓存逻辑）

### ✅ 阶段二审查节点

---

## 阶段三：核心算法 🧮

### 3.1 Step 1 — 原料综合碳水
- [x] 综合碳水 = USDA基础碳水 + 烹饪修正值
- [x] 原料总碳水 = 综合碳水 × 重量 / 100
- [x] 单元测试（番茄炒蛋示例）

### 3.2 Step 2 — 荤素分类汇总
- [x] 按 category 分荤素，计算各组总碳水/总重量/每g碳水
- [x] 单元测试

### 3.3 Step 3 — 总碳水摄入
- [x] C_total = [每g素碳水×(1-meat_ratio) + 每g荤碳水×meat_ratio] × intake_pct × 总重量
- [x] 滑块拖动实时重算
- [x] 单元测试

### 3.4 Step 4 — 加权平均 GI
- [x] GI_avg = Σ(碳水占比 × gi_value)
- [x] GI → Gamma 参数映射（a = 3.0-(GI/100)*1.5, b = 25-(GI/100)*5）
- [x] 单元测试（验证 GI_avg=35.3）

### 3.5 Step 5 — 血糖波动预测
- [x] Gamma 分布吸收曲线 Absorption(t)
- [x] BG(t) = BG_start + C_total × (GI_avg/100) × 0.05 × Absorption(t)
- [x] 生成 240 个数据点
- [x] 单元测试（验证峰值 ≈ 5.23）

### 3.6 Step 6 — 胰岛素色带
- [x] base = clamp(C_total/12, min, max)
- [x] 三段：蓝[min, base×0.7] 绿[base×0.7, base×1.3] 红[base×1.3, max]
- [x] 宽度比例计算
- [x] 单元测试（速效 C=50g 和 C=150g）

### 3.7 Step 7 — 重点关注时段
- [x] 遍历 BG(t)，找超阈值区间（≥10.0/≥7.8/≤3.9）
- [x] 提取峰值时间+血糖值，按严重程度排序
- [x] 调用 Qwen VL 生成温馨提醒（≤30字）
- [x] 单元测试

### 3.8 UseCase 封装
- [x] AnalyzeFoodUseCase（Step 0~7 完整流程）
- [x] CalculateBloodSugarUseCase（Step 3~5 联动）
- [x] CalculateInsulinBandUseCase（Step 6）
- [x] GenerateAlertUseCase（Step 7）

### ✅ 阶段三审查节点

---

## 阶段四：导航与 Onboarding 🧭

### 4.1 导航架构
- [x] Navigation Compose 路由图
- [x] 底部导航：左侧圆角胶囊（玻璃拟态）首页/档案夹/我的 + 右侧独立圆形拍照按钮
- [x] 所有页面路由定义

### 4.2 Onboarding 5 页
- [x] 欢迎页 → 功能亮点 → 免责声明(强制同意) → 必填健康档案 → 注册登录

### 4.3 登录/注册
- [x] 本地模拟（手机号/邮箱+密码，Room DB）
- [x] 登录状态持久化（DataStore）
- [x] 调整触发逻辑：仅在注册成功或登录成功后进入 Onboarding，引导前不可冷启动拦截

### ✅ 阶段四审查节点

---

## 阶段五：主页重构（纯私有代谢健康数据看板）🏠

- [x] 彻底物理删除社区功能（删除 CommunityPost / Comment / Favorite 实体、DAO、Repository、ViewModel）
- [x] 升级 Room 数据库为版本 4，移除假用户与假帖子预填充
- [x] 顶部 Header 卡片设计（融入“已为您守护控糖 XX 天”温情问候 + 代谢状态药丸胶囊）
- [x] 今日碳水摄入概览（真实查询 Room `food_records` 当日累计摄入量与进度条）
- [x] 最近一次餐食升糖预测卡片（真实菜品摄影缩略图、真实菜名、预估峰值 mmol/L 与达峰时间）
- [x] 真实典雅空态设计（无数据时不展示任何假数据，优雅引导拍照测算）
- [x] AI 代谢微建议卡片（基于真实时段分析生成的温馨提示）
- [x] 底部独立悬浮大按钮（一键调起餐食相机拍摄分析）

### ✅ 阶段五审查节点（重构完成并通过全量编译与单元测试）

---

## 阶段六：拍照 → AI 识别 → 档案详情页（核心流程）📷

> **目标**：用户拍照后，后台自动执行 Step 0~7 算法全流程，直接返回档案详情页。
> **识别模型**：Qwen VL (DashScope) 图像识别 + DeepSeek API 提醒文案生成
> **营养数据**：USDA FoodData Central API（增强中文映射 + 78 食材内置碳水表兜底）
> **关键**：删除所有模拟/预设代码。用户流程 = 点击拍照 → 硬币提示 → 拍照 → Loading(自动分析) → 档案详情页(滑块动态)

### 6.1 AI 模型分工（Qwen VL 图片识别 + DeepSeek 文案生成）
- [x] 创建 DeepSeekApi.kt Retrofit 接口 + DTO，用于提醒文案生成
- [x] AiRepositoryImpl：图片识别 → DashScope (Qwen VL)，提醒文案 → DeepSeek
- [x] 更新 ApiKeyProvider 同时支持 DASHSCOPE_API_KEY + DEEPSEEK_API_KEY
- [x] 两者共存，各司其职

### 6.2 增强 USDA API 集成
- [x] 扩充 translateIngredientName() 中文→英文映射表（78 种常见食材）
- [x] 添加 USDA 查询失败时的降级策略（78 种常见食材内置碳水表兜底）
- [x] 确认 USDA FoodData Central POST /foods/search 接口对接正确

### 6.3 简化拍照流程
- [x] 删除 CapturePreviewScreen.kt（拍照后直接分析，无需预览确认）
- [x] 删除 RecognitionLoadingScreen.kt 模拟识别代码（重写为真实 API 调用）
- [x] 删除 RecognitionResultScreen.kt（RecognitionConfirmScreen + AiInsightsScreen 全部移除）
- [x] 删除 CameraRecognitionViewModel 中的 RecognizedIngredient、runSimulatedRecognition()、所有模拟方法
- [x] 删除 CameraRoute.Preview / Confirm / Insights 路由
- [x] 删除 CookingMethod.kt 枚举（算法使用 AI JSON 中的字符串）

### 6.4 重构 CameraRecognitionViewModel
- [x] 状态：Idle → Loading → AnalysisResult(ArchiveDetailUiState) | Error
- [x] setCapturedImage(uri) → startAnalysis() 调用 AnalyzeFoodUseCase → 产出 ArchiveDetailUiState
- [x] updateSliders(meatRatio, intakePct) 调用 AnalyzeFoodUseCase 实时重算曲线+色带
- [x] saveRecord() 存入 FoodRecord → 跳转档案夹

### 6.5 实现档案详情页（核心 UI）
- [x] 免责声明小字（顶部浅灰，常驻）
- [x] 菜品大图（Coil AsyncImage 加载拍照图片）
- [x] 饭前血糖输入（可选，默认 5.0 mmol/L）
- [x] 荤素比例滑块：范围 0~1，拖动实时触发重算
- [x] 总体摄入量滑块：范围 0%~100%，拖动实时触发重算
- [x] 血糖波动预测曲线：Canvas 图表，240 点平滑曲线，标注 3.9/7.8/10.0 阈值线
- [x] 胰岛素色带：蓝→绿→红三段，动态宽度，标注 IU 刻度，按用户胰岛素类型切换范围
- [x] 重点关注时段卡片：时间+血糖值+严重程度+DeepSeek 生成温馨提醒
- [x] 底部操作栏：3 个分享 icon + 确定保存按钮
- [ ] 页面提供 @Preview 预览（待阶段九统一补齐）

### 6.6 清理导航与路由
- [x] CameraFlowNavHost 精简为 Camera → Loading → Detail 三段
- [x] 注册档案详情页路由（camera_flow/detail）
- [x] 移除旧 Confirm / Insights 路由引用

### ✅ 阶段六审查节点

---

## 阶段七：档案夹页面（现代代谢日历与真实数据回溯）📁

- [x] 水平可滑动日历条（最近 14 天到今天，带真实记录标记圆点）
- [x] 当日统计摘要（当日真实餐食次数与预估总碳水汇总）
- [x] 真实餐食卡片流（从 Room `food_records` 实时观察数据流，真实缩略图、真实菜名、碳水与峰值）
- [x] 典雅空态设计（当日无记录时展示亲和空态提示，拒绝模拟假数据）
- [x] 点击卡片直达 `ArchiveDetailScreen`（真实反序列化历史计算快照，支持二次微调滑块）
- [x] 单条记录真实删除交互（AlertDialog 确认，真实调用 Room DAO 执行持久化删除）

### ✅ 阶段七审查节点（完成并通过单元测试）

---

## 阶段八：个人页面（个人代谢配置与纯私有管理）👤

- [x] 个人代谢健康档案卡片（展示糖尿病类型、胰岛素类型、BMI自动测算、纯本地私有说明）
- [x] 弹窗式健康档案编辑（糖尿病类型单选、胰岛素类型单选、身高体重实时更新并持久化至 Room）
- [x] 外观显示模式（跟随系统 / 浅色模式 / 深色模式，DataStore 实时响应式绑定）
- [x] 医疗健康合规免责声明弹窗（非临床建议详细条款）
- [x] 本地数据管理（清空所有本地记录，带二次防误触确认）
- [x] 关于软件（版本与离线私有架构说明）

### ✅ 阶段八审查节点（完成并通过单元测试）

---

## 阶段九：UI 细节打磨 ✨

- [ ] 弹簧回弹动效、搜索框动画、音轨丘陵动画、页面过渡动画
- [ ] 底部导航栏+个人卡片玻璃拟态
- [ ] 扁平化 icon 设计/选择
- [ ] 免责声明常驻（详情页小字+关于全文+分享附带）
- [ ] 为每个页面与子页面补齐 `@Preview` 预览组合，便于逐页审查 UI

### ✅ 阶段九审查节点

---

## 阶段十：测试与验证 🧪

### 10.1 单元测试
- [ ] 碳水计算、GI加权、血糖曲线、胰岛素色带

### 10.2 UI 测试
- [ ] 滑块交互、动效、搜索栏、导航、深色模式

### 10.3 实机测试
- [ ] 拍照、DeepSeek 解析、USDA 缓存、动画流畅度、瀑布流性能

### 10.4 集成测试
- [ ] 拍照→AI→碳水→血糖→色带→时段→提醒 完整流程
- [ ] 社区完整流程
- [ ] Onboarding→注册→登录 完整流程

### ✅ 阶段十审查节点（最终审查）

---

## 附录：关键常量

| 常量 | 值 | 用途 |
|------|-----|------|
| K | 0.05 mmol/L per g | Step 5 |
| 碳水系数 | 1IU ≈ 12g | Step 6 |
| 高血糖阈值 | 10.0 mmol/L | Step 7 |
| 偏高阈值 | 7.8 mmol/L | Step 7 |
| 低血糖阈值 | 3.9 mmol/L | Step 7 |
| 默认血糖 | 5.0 mmol/L | Step 5 |
| 曲线范围 | 0~240 min | Step 5 |
| 最多识别 | 5 道菜 | Step 0 |
