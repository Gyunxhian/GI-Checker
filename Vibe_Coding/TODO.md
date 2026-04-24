# 糖尿病血糖助手 App — 开发任务清单 (TODO)

> 基于 `implementation_plan.md` (V4) 拆解，按开发顺序排列。
> 每完成一项主要任务（一级阶段）需提交人工审查，审查通过后才可进入下一阶段。

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
- [x] DashScope API（Qwen VL）Retrofit 接口
- [x] USDA API Retrofit 接口
- [x] API Key 安全存储
- [x] 网络错误处理与重试

### 2.5 Repository 层
- [x] UserRepository（登录注册、用户信息）
- [x] FoodRecordRepository（档案 CRUD）
- [x] CommunityRepository（帖子/评论/收藏 CRUD）
- [x] AiRepository（Qwen VL 调用封装）
- [x] NutritionRepository（USDA 查询 + 缓存逻辑）

### ✅ 阶段二审查节点

---

## 阶段三：核心算法 🧮

### 3.1 Step 1 — 原料综合碳水
- [ ] 综合碳水 = USDA基础碳水 + 烹饪修正值
- [ ] 原料总碳水 = 综合碳水 × 重量 / 100
- [ ] 单元测试（番茄炒蛋示例）

### 3.2 Step 2 — 荤素分类汇总
- [ ] 按 category 分荤素，计算各组总碳水/总重量/每g碳水
- [ ] 单元测试

### 3.3 Step 3 — 总碳水摄入
- [ ] C_total = [每g素碳水×(1-meat_ratio) + 每g荤碳水×meat_ratio] × intake_pct × 总重量
- [ ] 滑块拖动实时重算
- [ ] 单元测试

### 3.4 Step 4 — 加权平均 GI
- [ ] GI_avg = Σ(碳水占比 × gi_value)
- [ ] GI → Gamma 参数映射（a = 3.0-(GI/100)*1.5, b = 25-(GI/100)*5）
- [ ] 单元测试（验证 GI_avg=35.3）

### 3.5 Step 5 — 血糖波动预测
- [ ] Gamma 分布吸收曲线 Absorption(t)
- [ ] BG(t) = BG_start + C_total × (GI_avg/100) × 0.05 × Absorption(t)
- [ ] 生成 240 个数据点
- [ ] 单元测试（验证峰值 ≈ 5.23）

### 3.6 Step 6 — 胰岛素色带
- [ ] base = clamp(C_total/12, min, max)
- [ ] 三段：蓝[min, base×0.7] 绿[base×0.7, base×1.3] 红[base×1.3, max]
- [ ] 宽度比例计算
- [ ] 单元测试（速效 C=50g 和 C=150g）

### 3.7 Step 7 — 重点关注时段
- [ ] 遍历 BG(t)，找超阈值区间（≥10.0/≥7.8/≤3.9）
- [ ] 提取峰值时间+血糖值，按严重程度排序
- [ ] 调用 Qwen VL 生成温馨提醒（≤30字）
- [ ] 单元测试

### 3.8 UseCase 封装
- [ ] AnalyzeFoodUseCase（Step 0~7 完整流程）
- [ ] CalculateBloodSugarUseCase（Step 3~5 联动）
- [ ] CalculateInsulinBandUseCase（Step 6）
- [ ] GenerateAlertUseCase（Step 7）

### ✅ 阶段三审查节点

---

## 阶段四：导航与 Onboarding 🧭

### 4.1 导航架构
- [ ] Navigation Compose 路由图
- [ ] 底部导航：左侧圆角胶囊（玻璃拟态）首页/档案夹/我的 + 右侧独立圆形拍照按钮
- [ ] 所有页面路由定义

### 4.2 Onboarding 5 页
- [ ] 欢迎页 → 功能亮点 → 免责声明(强制同意) → 必填健康档案 → 注册登录

### 4.3 登录/注册
- [ ] 本地模拟（手机号/邮箱+密码，Room DB）
- [ ] 登录状态持久化（DataStore）

### ✅ 阶段四审查节点

---

## 阶段五：主页 🏠

### 5.1 启动欢迎层
- [ ] 全屏"血糖助手已守护您 XX 天了"（28sp+, #2E7E5D）
- [ ] 下滑手势 → 弹簧回弹动效 → 欢迎层弹走
- [ ] 每次进入主页 Tab 都显示

### 5.2 社区顶部操作栏
- [ ] 左侧发帖 icon / 中间筛选标签（推荐/关注/商城）/ 右侧搜索 icon
- [ ] 搜索框横向展开/收缩动画

### 5.3 瀑布流卡片
- [ ] 双列瀑布流（LazyVerticalStaggeredGrid）
- [ ] 卡片：图片+头像+标题+血糖标签+点赞评论数
- [ ] 档案分享 / 自由图文 UGC

### 5.4 帖子详情页
- [ ] 内容展示 + 评论区 + 点赞 + 收藏

### 5.5 发帖页
- [ ] 图文编辑 + 帖子类型选择 + 档案关联 + 发布

### ✅ 阶段五审查节点

---

## 阶段六：拍照与 AI 识别 📷

### 6.1 相机页面
- [ ] CameraX 集成拍照
- [ ] 硬币参照物提示
- [ ] 拍照后预览确认

### 6.2 AI 识别 (Step 0)
- [ ] 上传图片至 Qwen VL
- [ ] 结构化 Prompt（JSON 输出：菜品+原料+重量+烹饪方式+GI）
- [ ] 解析 JSON + 错误处理
- [ ] 最多 5 道菜

### 6.3 USDA API 查询
- [ ] 原料名查询基础碳水
- [ ] 中文→英文映射
- [ ] 查询结果缓存 Room DB

### 6.4 识别→创建档案
- [ ] 存入 FoodRecord → 跳转档案详情页

### ✅ 阶段六审查节点

---

## 阶段七：档案夹与详情页 📁

### 7.1 档案夹页面
- [ ] 按日期分组列表
- [ ] 顶部筛选（全部/已完成✅/未确定⚠️）
- [ ] 卡片（缩略图+日期+菜品名+状态）
- [ ] 左滑删除+确认弹窗

### 7.2 档案详情页布局
- [ ] 免责声明小字 → 菜品大图 → 饭前血糖输入(可选,默认5.0)

### 7.3 荤素比例滑块
- [ ] 范围 0~1，音轨丘陵动画，实时触发重算

### 7.4 摄入量滑块
- [ ] 范围 0%~100%，音轨丘陵动画，实时触发重算

### 7.5 血糖波动预测曲线
- [ ] Vico 图表，240 点平滑曲线，标注阈值线

### 7.6 胰岛素色带
- [ ] 蓝→绿→红三段，动态宽度，标注 IU，按胰岛素类型切换

### 7.7 重点关注时段卡片
- [ ] 时间+血糖值+严重程度+AI温馨提醒

### 7.8 底部操作
- [ ] 3 个分享 icon（自动附免责声明）+ 确定按钮

### ✅ 阶段七审查节点

---

## 阶段八：个人页面 👤

### 8.1 个人卡片
- [ ] 玻璃拟态设计，头像+昵称+统计

### 8.2 健康档案编辑
- [ ] 必填：糖尿病类型、胰岛素类型
- [ ] 选填：用量范围、体重、身高

### 8.3 功能列表
- [ ] 我的帖子 / 我的收藏 / 外观模式 / 设置 / 关于（含免责声明全文）

### ✅ 阶段八审查节点

---

## 阶段九：UI 细节打磨 ✨

- [ ] 弹簧回弹动效、搜索框动画、音轨丘陵动画、页面过渡动画
- [ ] 底部导航栏+个人卡片玻璃拟态
- [ ] 扁平化 icon 设计/选择
- [ ] 免责声明常驻（详情页小字+关于全文+分享附带）

### ✅ 阶段九审查节点

---

## 阶段十：测试与验证 🧪

### 10.1 单元测试
- [ ] 碳水计算、GI加权、血糖曲线、胰岛素色带

### 10.2 UI 测试
- [ ] 滑块交互、动效、搜索栏、导航、深色模式

### 10.3 实机测试
- [ ] 拍照、Qwen VL 解析、USDA 缓存、动画流畅度、瀑布流性能

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
