# OpenCode Base Captcha

零依赖的验证码生成与验证库，支持多种验证码类型，适用于 JDK 25+。

## 功能特性

- 13 种验证码类型（文本、算术、中文、GIF、滑块、旋转、点选、图片选择、音频、拼接、PoW）
- 交互式验证码类型（滑块、旋转、点选、图片选择、拼接碎片排序）
- 音频验证码（16kHz WAV，WCAG 无障碍合规）
- PoW 无感验证（SHA-256 工作量证明，无需用户交互）
- 抗 OCR/AI 增强（贝塞尔穿字噪声、正弦波变形、字符重叠、每字符随机字体、轮廓阴影）
- 自定义字体支持（TTF/OTF 加载，每字符随机字体选择）
- 轨迹分析（TrajectoryAnalyzer 检测机器人鼠标/触摸模式）
- 可配置尺寸、字体、干扰线和难度
- 可插拔存储（内存、Redis）
- 频率限制和反机器人行为分析
- Base64、图片和音频输出渲染
- 基于时间、行为和轨迹的验证
- 自适应难度调整（EASY / MEDIUM / HARD / EXTREME）
- 密封接口设计，保障类型安全
- 指标收集（CaptchaMetrics — 生成计数、成功/失败率、响应时间）
- 预生成缓冲池（CaptchaPool — 后台虚拟线程预生成，高吞吐场景）
- 组合验证器（CompositeValidator — 链式串联多个验证器，短路执行）
- 测试模式生成器（TestCaptchaGenerator — 固定答案，方便单元测试）
- 哈希答案存储（HashedCaptchaStore — SHA-256+salt 纵深防御）
- 事件监听系统（CaptchaEventListener — 生命周期事件回调）

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-captcha</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API 概览

### 核心

| 类名 | 说明 |
|------|------|
| `OpenCaptcha` | 验证码操作的主入口 / 门面类 |
| `Captcha` | 不可变验证码数据记录（id、answer、图片字节、类型） |
| `CaptchaConfig` | 验证码配置（类型、宽度、高度、长度、过期时间） |
| `CaptchaType` | 验证码类型枚举（TEXT、ARITHMETIC、CHINESE、GIF、SLIDER 等） |
| `ValidationResult` | 验证结果记录（成功、消息、结果代码） |
| `CaptchaEventListener` | 生命周期事件回调接口（onGenerated、onValidationSuccess、onValidationFailure） |
| `CaptchaEventDispatcher` | 线程安全的多监听器事件派发器（异常隔离） |
| `CaptchaMetrics` | 轻量级指标收集器（基于 LongAdder，O(1) 开销） |
| `CaptchaPool` | 高吞吐预生成缓冲池（后台虚拟线程填充） |
| `CompositeValidator` | 链式组合验证器（短路执行） |
| `TestCaptchaGenerator` | 固定答案验证码生成器（用于单元测试） |
| `HashedCaptchaStore` | 装饰器模式哈希存储（SHA-256+salt） |
| `HashedCaptchaValidator` | HashedCaptchaStore 验证器（OpenCaptcha 自动检测） |

<details>
<summary>详细方法列表</summary>

#### OpenCaptcha

| 方法 | 说明 |
|------|------|
| `static Captcha create()` | 使用默认配置创建验证码 |
| `static Captcha create(CaptchaConfig config)` | 使用指定配置创建验证码 |
| `static Captcha create(CaptchaType type)` | 创建指定类型的验证码 |
| `static Captcha numeric()` | 创建数字验证码 |
| `static Captcha alpha()` | 创建字母验证码 |
| `static Captcha alphanumeric()` | 创建字母数字验证码 |
| `static Captcha arithmetic()` | 创建算术验证码 |
| `static Captcha chinese()` | 创建中文验证码 |
| `static Captcha gif()` | 创建 GIF 动画验证码 |
| `static Captcha slider()` | 创建滑块验证码 |
| `static Captcha click()` | 创建点击验证码 |
| `static Captcha rotate()` | 创建旋转验证码 |
| `static Captcha audio()` | 创建音频验证码（WAV） |
| `static Captcha jigsaw()` | 创建拼接验证码 |
| `static Captcha pow()` | 创建工作量证明验证码 |
| `static Builder builder()` | 创建构建器（高级用法） |
| `Captcha generate()` | 使用已配置参数生成验证码并持久化到存储 |
| `Captcha generate(CaptchaConfig config)` | 使用配置生成验证码并持久化到存储 |
| `ValidationResult validate(String id, String answer)` | 验证验证码答案 |
| `void render(Captcha captcha, OutputStream out)` | 将验证码渲染到输出流 |
| `CaptchaStore getStore()` | 获取验证码存储 |
| `CaptchaConfig getConfig()` | 获取配置 |
| `CaptchaMetrics getMetrics()` | 获取指标收集器（未配置时为 null） |
| `CaptchaEventListener getEventListener()` | 获取事件监听器（未配置时为 null） |

#### Captcha（record）

| 方法 | 说明 |
|------|------|
| `String id()` | 获取唯一标识符 |
| `CaptchaType type()` | 获取验证码类型 |
| `byte[] imageData()` | 获取图像数据字节 |
| `String answer()` | 获取正确答案 |
| `Map<String, Object> metadata()` | 获取附加元数据 |
| `Instant createdAt()` | 获取创建时间戳 |
| `Instant expiresAt()` | 获取过期时间戳 |
| `String toBase64()` | 将图像数据转换为 Base64 字符串 |
| `String toBase64DataUrl()` | 将图像数据转换为 Base64 数据 URL |
| `String getMimeType()` | 根据验证码类型获取 MIME 类型 |
| `boolean isExpired()` | 检查验证码是否已过期 |
| `<T> T getMetadata(String key)` | 按键获取元数据值 |
| `int getWidth()` | 从元数据获取图像宽度 |
| `int getHeight()` | 从元数据获取图像高度 |

#### CaptchaConfig

| 方法 | 说明 |
|------|------|
| `static CaptchaConfig defaults()` | 创建默认配置 |
| `static Builder builder()` | 创建配置构建器 |
| `Builder toBuilder()` | 从此配置创建构建器 |
| `int getWidth()` | 获取图像宽度 |
| `int getHeight()` | 获取图像高度 |
| `int getLength()` | 获取验证码长度 |
| `CaptchaType getType()` | 获取验证码类型 |
| `Duration getExpireTime()` | 获取过期时间 |
| `int getNoiseLines()` | 获取干扰线数量 |
| `int getNoiseDots()` | 获取干扰点数量 |
| `float getFontSize()` | 获取字体大小 |
| `String getFontName()` | 获取字体名称 |
| `Color getBackgroundColor()` | 获取背景颜色 |
| `Color[] getFontColors()` | 获取字体颜色 |
| `boolean isCaseSensitive()` | 获取大小写敏感标志 |
| `int getGifFrameCount()` | 获取 GIF 帧数 |
| `int getGifDelay()` | 获取 GIF 帧延迟 |
| `List<String> getCustomFontPaths()` | 获取自定义字体文件路径（TTF/OTF） |
| `boolean isRandomFontPerChar()` | 每个字符是否使用随机不同字体 |
| `float getCharOverlapRatio()` | 获取字符重叠比例（0.0-0.5） |
| `boolean isSineWarpEnabled()` | 是否启用正弦波变形 |
| `boolean isOutlineShadowEnabled()` | 是否启用字符轮廓阴影 |
| `boolean isBezierNoiseEnabled()` | 是否启用贝塞尔穿字噪声 |
| `int getPowDifficulty()` | 获取 PoW 前导零位难度（10-32） |
| `float getAudioSpeedVariation()` | 获取音频语速变化范围（0.0-0.5） |

#### CaptchaType（枚举）

| 方法 | 说明 |
|------|------|
| `boolean isInteractive()` | 检查是否为交互式类型（SLIDER、CLICK、ROTATE、IMAGE_SELECT、JIGSAW） |
| `boolean isTextBased()` | 检查是否为文本类型（NUMERIC、ALPHA、ALPHANUMERIC、ARITHMETIC、CHINESE） |
| `boolean isAudio()` | 检查是否为音频类型（AUDIO） |
| `boolean isInvisible()` | 检查是否为无感类型（POW） |

常量：`NUMERIC`、`ALPHA`、`ALPHANUMERIC`、`ARITHMETIC`、`CHINESE`、`GIF`、`SLIDER`、`CLICK`、`ROTATE`、`IMAGE_SELECT`、`AUDIO`、`JIGSAW`、`POW`

#### ValidationResult（record）

| 方法 | 说明 |
|------|------|
| `boolean success()` | 验证是否成功 |
| `String message()` | 结果消息 |
| `ResultCode code()` | 结果代码 |
| `boolean isFailed()` | 检查验证是否失败 |
| `static ValidationResult ok()` | 创建成功结果 |
| `static ValidationResult notFound()` | 创建未找到结果 |
| `static ValidationResult expired()` | 创建已过期结果 |
| `static ValidationResult mismatch()` | 创建不匹配结果 |
| `static ValidationResult rateLimited()` | 创建速率限制结果 |
| `static ValidationResult invalidInput()` | 创建无效输入结果 |
| `static ValidationResult suspiciousBehavior()` | 创建可疑行为结果 |

ResultCode 枚举：`SUCCESS`、`NOT_FOUND`、`EXPIRED`、`MISMATCH`、`RATE_LIMITED`、`INVALID_INPUT`、`SUSPICIOUS_BEHAVIOR`

#### CaptchaMetrics

| 方法 | 说明 |
|------|------|
| `static CaptchaMetrics create()` | 创建新的指标实例 |
| `void recordGeneration(CaptchaType type)` | 记录一次验证码生成事件 |
| `void recordValidation(boolean success, Duration responseTime)` | 记录验证结果及响应时间 |
| `void recordValidation(boolean success)` | 记录验证结果（无响应时间） |
| `MetricsSnapshot snapshot()` | 获取所有指标的不可变时间点快照 |
| `void reset()` | 重置所有计数器并重启运行时间计时 |

MetricsSnapshot 记录：`totalGenerated()`、`totalValidations()`、`successfulValidations()`、`failedValidations()`、`successRate()`、`averageResponseTime()`、`generationsByType()`、`uptime()`

#### CaptchaPool

| 方法 | 说明 |
|------|------|
| `static Builder builder()` | 创建新的池构建器 |
| `Captcha take()` | 从池中获取验证码（池空则实时生成） |
| `int size()` | 获取池中当前可用数量 |
| `boolean isRunning()` | 检查池是否正在运行 |
| `void close()` | 关闭池和后台线程 |

构建器：`.config(CaptchaConfig)`、`.poolSize(int)`（1-10000，默认 100）、`.refillThreshold(float)`（0.0-1.0，默认 0.2）、`.build()`

#### CompositeValidator

| 方法 | 说明 |
|------|------|
| `static CompositeValidator of(CaptchaValidator first, CaptchaValidator... rest)` | 使用可变参数创建（首个失败即短路） |
| `static CompositeValidator ofList(List<CaptchaValidator> validators)` | 从列表创建 |
| `static Builder builder()` | 创建构建器以逐步构造 |
| `ValidationResult validate(String id, String answer)` | 链接所有验证器验证 |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | 带大小写选项验证 |
| `int size()` | 获取链中验证器数量 |
| `List<CaptchaValidator> getValidators()` | 获取验证器列表（不可修改） |

#### TestCaptchaGenerator

| 方法 | 说明 |
|------|------|
| `TestCaptchaGenerator(String fixedAnswer)` | 使用固定答案构造（用于单元测试） |
| `Captcha generate(CaptchaConfig config)` | 生成固定答案、空图像数据的验证码 |
| `CaptchaType getType()` | 返回 ALPHANUMERIC |
| `String getFixedAnswer()` | 获取固定答案 |

#### HashedCaptchaStore

| 方法 | 说明 |
|------|------|
| `static HashedCaptchaStore wrap(CaptchaStore delegate)` | 使用哈希包装存储（不区分大小写） |
| `static HashedCaptchaStore wrap(CaptchaStore delegate, boolean caseSensitive)` | 使用哈希包装存储（可配置大小写） |
| `void store(String id, String answer, Duration ttl)` | SHA-256+salt 哈希后存储答案 |
| `boolean verifyAnswer(String id, String plainAnswer)` | 验证答案但不删除条目 |
| `boolean verifyAndRemove(String id, String plainAnswer)` | 原子验证并删除条目 |
| `ValidationResult verifyAndRemoveResult(String id, String plainAnswer)` | 验证并删除，返回 NOT_FOUND/MISMATCH/SUCCESS |

委托方法：`get()`、`getAndRemove()`、`remove()`、`exists()`、`clearExpired()`、`clearAll()`、`size()`

#### HashedCaptchaValidator

| 方法 | 说明 |
|------|------|
| `HashedCaptchaValidator(HashedCaptchaStore store)` | 使用哈希存储构造 |
| `ValidationResult validate(String id, String answer)` | 验证明文答案与存储哈希 |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | 验证（caseSensitive 被忽略 — 在存储层配置） |

#### CaptchaEventListener（接口）

| 方法 | 说明 |
|------|------|
| `default void onGenerated(Captcha captcha)` | 验证码生成时调用 |
| `default void onValidationSuccess(String captchaId)` | 验证成功时调用 |
| `default void onValidationFailure(String captchaId, ResultCode reason)` | 验证失败时调用 |

#### CaptchaEventDispatcher

| 方法 | 说明 |
|------|------|
| `void addListener(CaptchaEventListener listener)` | 注册事件监听器 |
| `boolean removeListener(CaptchaEventListener listener)` | 移除事件监听器 |
| `int listenerCount()` | 获取已注册监听器数量 |
| `void onGenerated(Captcha captcha)` | 向所有监听器分发生成事件 |
| `void onValidationSuccess(String captchaId)` | 向所有监听器分发验证成功事件 |
| `void onValidationFailure(String captchaId, ResultCode reason)` | 向所有监听器分发验证失败事件 |

</details>

### 生成器

| 类名 | 说明 |
|------|------|
| `CaptchaGenerator` | 验证码生成器密封接口 |
| `AbstractCaptchaGenerator` | 生成器抽象基类 |
| `SpecCaptchaGenerator` | 标准 PNG 文本验证码生成器 |
| `ImageCaptchaGenerator` | 基于图片的验证码生成器 |
| `GifCaptchaGenerator` | GIF 动画验证码生成器 |
| `ArithmeticCaptchaGenerator` | 数学表达式验证码生成器 |
| `ChineseCaptchaGenerator` | 中文字符验证码生成器 |
| `AudioCaptchaGenerator` | 音频验证码生成器（16kHz WAV 音调序列） |
| `PowCaptchaGenerator` | 工作量证明验证码生成器（SHA-256 挑战） |

<details>
<summary>详细方法列表</summary>

#### CaptchaGenerator（密封接口）

| 方法 | 说明 |
|------|------|
| `Captcha generate()` | 使用默认配置生成验证码 |
| `Captcha generate(CaptchaConfig config)` | 使用指定配置生成验证码 |
| `CaptchaType getType()` | 获取支持的验证码类型 |
| `static CaptchaGenerator forType(CaptchaType type)` | 为指定类型创建生成器 |
| `static CaptchaGenerator numeric()` | 创建数字验证码生成器 |
| `static CaptchaGenerator alpha()` | 创建字母验证码生成器 |
| `static CaptchaGenerator alphanumeric()` | 创建字母数字验证码生成器 |
| `static CaptchaGenerator arithmetic()` | 创建算术验证码生成器 |
| `static CaptchaGenerator chinese()` | 创建中文验证码生成器 |
| `static CaptchaGenerator gif()` | 创建 GIF 验证码生成器 |
| `static CaptchaGenerator audio()` | 创建音频验证码生成器 |

#### AbstractCaptchaGenerator（抽象类）

| 方法 | 说明 |
|------|------|
| `protected BufferedImage createImage(CaptchaConfig config)` | 使用配置创建缓冲图像 |
| `protected Graphics2D createGraphics(BufferedImage image, CaptchaConfig config)` | 为图像创建图形 |
| `protected void drawNoise(Graphics2D g, CaptchaConfig config)` | 在图像上绘制噪点 |
| `protected byte[] toBytes(BufferedImage image)` | 将图像转换为 PNG 字节 |
| `protected String generateId()` | 生成唯一验证码 ID |
| `protected Map<String, Object> createMetadata(CaptchaConfig config)` | 创建元数据映射 |
| `protected Captcha buildCaptcha(CaptchaType type, byte[] imageData, String answer, CaptchaConfig config)` | 从生成数据构建验证码 |

#### SpecCaptchaGenerator / ImageCaptchaGenerator / GifCaptchaGenerator / ArithmeticCaptchaGenerator / ChineseCaptchaGenerator

| 方法 | 说明 |
|------|------|
| `Captcha generate(CaptchaConfig config)` | 使用配置生成验证码 |
| `CaptchaType getType()` | 获取支持的验证码类型 |

</details>

### 交互式生成器

| 类名 | 说明 |
|------|------|
| `SliderCaptchaGenerator` | 滑块拼图验证码生成器 |
| `RotateCaptchaGenerator` | 图片旋转验证码生成器 |
| `ClickCaptchaGenerator` | 点选文字验证码生成器 |
| `ImageSelectCaptchaGenerator` | 图片选择验证码生成器 |
| `JigsawCaptchaGenerator` | 拼接碎片排序验证码生成器 |

<details>
<summary>详细方法列表</summary>

#### SliderCaptchaGenerator / RotateCaptchaGenerator / ClickCaptchaGenerator / ImageSelectCaptchaGenerator

| 方法 | 说明 |
|------|------|
| `Captcha generate(CaptchaConfig config)` | 使用配置生成交互式验证码 |
| `CaptchaType getType()` | 获取支持的验证码类型 |

所有交互式生成器实现 `CaptchaGenerator` 密封接口。

</details>

### 渲染器

| 类名 | 说明 |
|------|------|
| `CaptchaRenderer` | 验证码输出渲染器接口 |
| `ImageCaptchaRenderer` | PNG 图片渲染器 |
| `GifCaptchaRenderer` | GIF 图片渲染器 |
| `Base64CaptchaRenderer` | Base64 Data URL 渲染器 |
| `AudioCaptchaRenderer` | 音频 WAV 渲染器 |

<details>
<summary>详细方法列表</summary>

#### CaptchaRenderer（接口）

| 方法 | 说明 |
|------|------|
| `void render(Captcha captcha, OutputStream out)` | 将验证码渲染到输出流 |
| `byte[] renderToBytes(Captcha captcha)` | 将验证码渲染到字节数组 |
| `String renderToBase64(Captcha captcha)` | 将验证码渲染到 Base64 字符串 |
| `String getContentType()` | 获取渲染输出的内容类型 |
| `static CaptchaRenderer image()` | 创建 PNG 图片渲染器 |
| `static CaptchaRenderer gif()` | 创建 GIF 图片渲染器 |
| `static CaptchaRenderer base64()` | 创建 Base64 数据 URL 渲染器 |
| `static CaptchaRenderer audio()` | 创建音频 WAV 渲染器 |

#### ImageCaptchaRenderer / GifCaptchaRenderer / Base64CaptchaRenderer / AudioCaptchaRenderer

均实现 `CaptchaRenderer` 接口，方法签名相同。内容类型：
- `ImageCaptchaRenderer` -> `image/png`
- `GifCaptchaRenderer` -> `image/gif`
- `Base64CaptchaRenderer` -> `text/plain`
- `AudioCaptchaRenderer` -> `audio/wav`

</details>

### 验证器

| 类名 | 说明 |
|------|------|
| `CaptchaValidator` | 验证器接口 |
| `SimpleCaptchaValidator` | 基本文本匹配验证器 |
| `TimeBasedCaptchaValidator` | 带时间过期检查的验证器 |
| `BehaviorCaptchaValidator` | 基于行为分析的验证器 |
| `PowCaptchaValidator` | 工作量证明 nonce 验证器（SHA-256 前导零） |
| `CaptchaRateLimiter` | 验证尝试频率限制器 |

<details>
<summary>详细方法列表</summary>

#### CaptchaValidator（接口）

| 方法 | 说明 |
|------|------|
| `ValidationResult validate(String id, String answer)` | 验证验证码答案 |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | 带大小写敏感选项的验证 |
| `static CaptchaValidator simple(CaptchaStore store)` | 创建简单验证器 |
| `static CaptchaValidator timeBased(CaptchaStore store)` | 创建基于时间的验证器 |

#### SimpleCaptchaValidator

| 方法 | 说明 |
|------|------|
| `SimpleCaptchaValidator(CaptchaStore store)` | 使用存储构造 |
| `ValidationResult validate(String id, String answer)` | 验证答案（大小写不敏感） |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | 带大小写敏感选项的验证 |

#### TimeBasedCaptchaValidator

| 方法 | 说明 |
|------|------|
| `TimeBasedCaptchaValidator(CaptchaStore store)` | 使用存储构造 |
| `void recordCreation(String id)` | 记录验证码创建时间 |
| `ValidationResult validate(String id, String answer)` | 带时间检查的验证 |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | 带时间和大小写敏感的验证 |
| `void clearOldRecords()` | 清除旧的创建时间记录 |

#### BehaviorCaptchaValidator

| 方法 | 说明 |
|------|------|
| `BehaviorCaptchaValidator(CaptchaStore store)` | 使用存储构造 |
| `BehaviorCaptchaValidator(CaptchaStore store, BehaviorAnalyzer analyzer)` | 使用自定义分析器构造 |
| `void recordCreation(String captchaId, String clientId)` | 为客户端记录验证码创建 |
| `ValidationResult validate(String id, String answer)` | 带行为检查的验证 |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | 带行为和大小写敏感的验证 |
| `ValidationResult validate(String id, String answer, String clientId)` | 使用明确客户端 ID 验证 |
| `ValidationResult validate(String id, String answer, String clientId, boolean caseSensitive)` | 使用客户端 ID 和大小写敏感验证 |
| `ValidationResult validate(String id, String answer, String clientId, TrajectoryData trajectory)` | 使用轨迹数据验证 |
| `ValidationResult validate(String id, String answer, String clientId, TrajectoryData trajectory, boolean caseSensitive)` | 使用轨迹数据和大小写敏感选项验证 |
| `BehaviorAnalyzer getAnalyzer()` | 获取行为分析器 |
| `TrajectoryAnalyzer getTrajectoryAnalyzer()` | 获取轨迹分析器 |
| `void clearOldRecords()` | 清除旧的创建记录 |

#### PowCaptchaValidator

| 方法 | 说明 |
|------|------|
| `PowCaptchaValidator(CaptchaStore store)` | 使用存储构造 |
| `static PowCaptchaValidator create(CaptchaStore store)` | 工厂方法 |
| `ValidationResult validate(String id, String answer)` | 验证 PoW nonce |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | 验证 PoW nonce（caseSensitive 被忽略） |

#### CaptchaRateLimiter

| 方法 | 说明 |
|------|------|
| `CaptchaRateLimiter()` | 默认构造（每分钟 10 次） |
| `CaptchaRateLimiter(int maxRequests, Duration window)` | 指定设置构造 |
| `boolean isAllowed(String clientId)` | 检查客户端是否允许请求 |
| `int getRemainingRequests(String clientId)` | 获取客户端剩余请求数 |
| `Duration getTimeUntilReset(String clientId)` | 获取重置前的时间 |
| `void clear(String clientId)` | 清除客户端的速率限制 |
| `void clearExpired()` | 清除所有过期条目 |

</details>

### 存储

| 类名 | 说明 |
|------|------|
| `CaptchaStore` | 验证码数据存储接口 |
| `MemoryCaptchaStore` | 内存存储，带自动淘汰 |
| `RedisCaptchaStore` | 基于 Redis 的存储 |

<details>
<summary>详细方法列表</summary>

#### CaptchaStore（接口）

| 方法 | 说明 |
|------|------|
| `void store(String id, String answer, Duration ttl)` | 存储验证码答案（带 TTL） |
| `Optional<String> get(String id)` | 检索验证码答案 |
| `Optional<String> getAndRemove(String id)` | 检索并删除验证码答案 |
| `void remove(String id)` | 删除验证码 |
| `boolean exists(String id)` | 检查验证码是否存在 |
| `void clearExpired()` | 清除所有过期验证码 |
| `void clearAll()` | 清除所有验证码 |
| `int size()` | 获取当前存储大小 |
| `static CaptchaStore memory()` | 创建内存存储 |
| `static CaptchaStore memory(int maxSize)` | 创建带最大大小的内存存储 |

#### MemoryCaptchaStore（实现 CaptchaStore、AutoCloseable）

| 方法 | 说明 |
|------|------|
| `MemoryCaptchaStore()` | 默认最大大小构造（10000） |
| `MemoryCaptchaStore(int maxSize)` | 指定最大大小构造 |
| `void shutdown()` | 关闭清理调度器 |
| `void close()` | 关闭存储（AutoCloseable，委托给 shutdown） |

以及所有 `CaptchaStore` 接口方法。

#### RedisCaptchaStore

| 方法 | 说明 |
|------|------|
| `static Builder builder()` | 创建构建器 |
| `String getKeyPrefix()` | 获取键前缀 |

构建器方法：`keyPrefix(String)`、`setter(RedisSetter)`、`getter(Function<String, String>)`、`deleter(Consumer<String>)`、`existsChecker(Function<String, Boolean>)`、`build()`。

以及所有 `CaptchaStore` 接口方法。

</details>

### 安全

| 类名 | 说明 |
|------|------|
| `CaptchaSecurity` | 安全配置与执行 |
| `AntiBotStrategy` | 反机器人检测策略 |
| `BehaviorAnalyzer` | 用户行为分析，用于机器人检测 |
| `TrajectoryAnalyzer` | 轨迹分析，用于机器人检测（速度、抖动、方向） |
| `TrajectoryData` | 不可变轨迹记录（坐标点、时间戳、总时长） |

<details>
<summary>详细方法列表</summary>

#### CaptchaSecurity（工具类）

| 方法 | 说明 |
|------|------|
| `static String generateSecureId()` | 生成安全随机 ID |
| `static String generateSecureToken(int length)` | 生成安全随机令牌 |
| `static String hashAnswer(String answer, String salt)` | 对答案哈希以安全存储 |
| `static boolean verifyHashedAnswer(String answer, String hashedAnswer, String salt)` | 验证哈希后的答案 |
| `static boolean constantTimeEquals(String a, String b)` | 常量时间字符串比较 |
| `static String generateSalt()` | 生成随机盐 |

#### AntiBotStrategy

| 方法 | 说明 |
|------|------|
| `AntiBotStrategy(BehaviorAnalyzer analyzer)` | 使用分析器构造 |
| `static AntiBotStrategy create()` | 使用新分析器创建 |
| `AntiBotStrategy withBaseStrength(CaptchaStrength strength)` | 设置基础强度 |
| `AntiBotStrategy withBaseType(CaptchaType type)` | 设置基础类型 |
| `CaptchaStrength recommendStrength(String clientId)` | 推荐客户端验证码强度 |
| `CaptchaType recommendType(String clientId)` | 推荐客户端验证码类型 |
| `boolean shouldBlock(String clientId)` | 检查客户端是否应被阻止 |
| `BehaviorAnalyzer getAnalyzer()` | 获取行为分析器 |

#### BehaviorAnalyzer

| 方法 | 说明 |
|------|------|
| `AnalysisResult analyze(String clientId, Duration responseTime, boolean success)` | 分析客户端行为 |
| `ClientBehavior getBehavior(String clientId)` | 获取客户端行为数据 |
| `void clear(String clientId)` | 清除客户端行为数据 |
| `void clearOld()` | 清除所有旧行为数据 |

AnalysisResult 枚举：`NORMAL`、`SUSPICIOUS_TIMING`、`TOO_MANY_FAILURES`、`CONSISTENT_TIMING`

ClientBehavior 方法：`getLastActivity()`、`getTotalAttempts()`、`getRecentFailures()`

#### TrajectoryAnalyzer

| 方法 | 说明 |
|------|------|
| `TrajectoryResult analyze(TrajectoryData data)` | 分析轨迹以检测机器人 |

TrajectoryResult 枚举：`HUMAN`、`BOT_LINEAR`、`BOT_NO_JITTER`、`BOT_CONSTANT_SPEED`、`BOT_TOO_FAST`、`INSUFFICIENT_DATA`

#### TrajectoryData（record）

| 方法 | 说明 |
|------|------|
| `TrajectoryData(List<Point> points, List<Long> timestamps, long totalDurationMs)` | 带验证的构造器 |
| `List<Point> points()` | 获取坐标序列 |
| `List<Long> timestamps()` | 获取时间戳序列 |
| `long totalDurationMs()` | 获取总时长（毫秒） |
| `List<Double> speeds()` | 计算相邻点间速度 |
| `List<Double> accelerations()` | 计算加速度序列 |
| `int directionChanges()` | 计算方向变化次数（>15 度阈值） |
| `double jitterStdDev()` | 计算抖动标准差 |

TrajectoryData.Point record：`int x()`、`int y()`

</details>

### 辅助

| 类名 | 说明 |
|------|------|
| `CaptchaChars` | 验证码生成字符集 |
| `CaptchaFontUtil` | 字体加载和管理 |
| `CaptchaNoiseUtil` | 干扰线和扭曲绘制工具 |
| `CaptchaDifficultyAdapter` | 自适应难度调整 |
| `CaptchaStrength` | 验证码难度强度枚举 |

<details>
<summary>详细方法列表</summary>

#### CaptchaChars（工具类）

| 方法 | 说明 |
|------|------|
| `static String generate(CaptchaType type, int length)` | 为指定类型生成随机字符 |
| `static String generateFromChars(char[] chars, int length)` | 从字符集生成随机字符 |
| `static String generateChinese(int length)` | 生成随机中文字符 |
| `static String[] generateArithmetic()` | 生成算术表达式 [表达式, 答案] |
| `static Random getRandom()` | 获取 SecureRandom 实例 |
| `static int randomInt(int bound)` | 在范围内生成随机整数 |
| `static int randomInt(int min, int max)` | 在指定范围内生成随机整数 |

字符集常量：`NUMERIC`、`ALPHA_LOWER`、`ALPHA_UPPER`、`ALPHA`、`ALPHANUMERIC`、`CHINESE`

#### CaptchaFontUtil（工具类）

| 方法 | 说明 |
|------|------|
| `static Font getFont(CaptchaConfig config)` | 根据配置获取字体 |
| `static Font getFont(String fontName, float fontSize)` | 获取指定名称和大小的字体 |
| `static Font getRandomStyleFont(Font font)` | 获取随机样式字体 |
| `static Font getRotatedFont(Font font, double angle)` | 获取旋转字体 |
| `static Font getChineseFont(float fontSize)` | 获取适合中文字符的字体 |
| `static Color getRandomColor(CaptchaConfig config)` | 从配置颜色中获取随机颜色 |
| `static Color randomColor()` | 生成随机颜色 |
| `static Color randomLightColor()` | 生成浅随机颜色 |
| `static Color randomDarkColor()` | 生成深随机颜色 |
| `static Font loadCustomFont(String path, float fontSize)` | 从 TTF/OTF 文件加载自定义字体 |
| `static Font[] getRandomFontsPerChar(String baseFontName, List<String> customPaths, float fontSize, int charCount)` | 为每个字符生成随机字体数组（抗 OCR） |

#### CaptchaNoiseUtil（工具类）

| 方法 | 说明 |
|------|------|
| `static void drawNoiseLines(Graphics2D g, CaptchaConfig config)` | 绘制干扰线 |
| `static void drawCurveLines(Graphics2D g, CaptchaConfig config)` | 绘制曲线 |
| `static void drawCubicCurveLines(Graphics2D g, CaptchaConfig config)` | 绘制三次曲线 |
| `static void drawNoiseDots(Graphics2D g, CaptchaConfig config)` | 绘制干扰点 |
| `static void drawBackgroundNoise(Graphics2D g, CaptchaConfig config)` | 绘制背景噪点 |
| `static void shear(Graphics2D g, CaptchaConfig config)` | 应用剪切变换效果 |
| `static void drawGradientBackground(Graphics2D g, CaptchaConfig config)` | 绘制渐变背景 |
| `static void drawInterferencePattern(Graphics2D g, CaptchaConfig config)` | 绘制干扰网格图案 |
| `static void drawBezierNoise(Graphics2D g, int width, int height, int count)` | 绘制穿过字符区域的贝塞尔曲线（抗 OCR） |
| `static BufferedImage applySineWarp(BufferedImage image, double amplitude, double period)` | 施加正弦波变形扭曲 |
| `static void drawOutlineShadow(Graphics2D g, String text, Font font, int x, int y, Color shadowColor)` | 绘制字符轮廓阴影（抗 OCR） |
| `static int calculateOverlapSpacing(int totalWidth, int charCount, float fontSize, float overlapRatio)` | 计算带重叠的字符间距 |

#### CaptchaDifficultyAdapter

| 方法 | 说明 |
|------|------|
| `CaptchaStrength getStrength(String clientId)` | 获取客户端推荐强度 |
| `CaptchaConfig getConfig(String clientId)` | 获取客户端适应配置 |
| `CaptchaConfig getConfig(String clientId, CaptchaConfig baseConfig)` | 使用自定义基础配置获取适应配置 |
| `void recordAttempt(String clientId, boolean success)` | 记录验证尝试 |
| `void reset(String clientId)` | 重置客户端难度 |
| `double getGlobalFailureRate()` | 获取全局失败率（0.0-1.0） |
| `int getFailureCount(String clientId)` | 获取客户端失败次数 |
| `int getTrackedClientCount()` | 获取跟踪的客户端数量 |
| `void clearAll()` | 清除所有客户端记录 |

#### CaptchaStrength（枚举）

| 方法 | 说明 |
|------|------|
| `int getNoiseLines()` | 获取干扰线数量 |
| `int getNoiseDots()` | 获取干扰点数量 |
| `float getFontSize()` | 获取字体大小 |
| `boolean isRandomFontPerChar()` | 是否启用每字符随机字体 |
| `boolean isBezierNoiseEnabled()` | 是否启用贝塞尔噪声 |
| `boolean isSineWarpEnabled()` | 是否启用正弦波变形 |
| `boolean isOutlineShadowEnabled()` | 是否启用轮廓阴影 |
| `float getCharOverlapRatio()` | 获取字符重叠比例 |
| `CaptchaConfig.Builder applyTo(CaptchaConfig.Builder builder)` | 将强度应用到配置构建器 |
| `CaptchaConfig toConfig()` | 使用此强度创建配置 |

常量：`EASY`、`MEDIUM`、`HARD`、`EXTREME`

HARD 启用：随机字体、贝塞尔噪声、轮廓阴影、10% 字符重叠。EXTREME 启用所有 HARD 特性并增加正弦波变形、20% 重叠。

</details>

### 编解码（内部）

| 类名 | 说明 |
|------|------|
| `GifEncoder` | GIF 图片编码器 |
| `LZWEncoder` | GIF 用 LZW 压缩编码器 |
| `NeuQuantEncoder` | NeuQuant 神经网络颜色量化器 |

<details>
<summary>详细方法列表</summary>

#### GifEncoder

| 方法 | 说明 |
|------|------|
| `void setDelay(int ms)` | 设置帧延迟（毫秒） |
| `void setDispose(int code)` | 设置处置代码 |
| `void setRepeat(int iter)` | 设置循环次数（0 = 无限） |
| `void setQuality(int quality)` | 设置颜色量化质量 |
| `boolean start(OutputStream os)` | 开始编码到输出流 |
| `boolean addFrame(BufferedImage im)` | 添加帧到 GIF |
| `boolean finish()` | 完成编码 |

</details>

### 异常

| 类名 | 说明 |
|------|------|
| `CaptchaException` | 验证码基础异常 |
| `CaptchaGenerationException` | 验证码生成失败异常 |
| `CaptchaVerifyException` | 验证码验证失败异常 |
| `CaptchaExpiredException` | 验证码已过期异常 |
| `CaptchaNotFoundException` | 验证码未找到异常 |
| `CaptchaRateLimitException` | 频率限制超出异常 |

<details>
<summary>详细方法列表</summary>

所有异常继承自 `CaptchaException`（继承自 `RuntimeException`），提供以下构造器：

| 方法 | 说明 |
|------|------|
| `(String message)` | 使用消息构造 |
| `(String message, Throwable cause)` | 使用消息和原因构造 |
| `(Throwable cause)` | 使用原因构造 |

</details>

## 快速开始

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.store.CaptchaStore;

// 简单文本验证码（无状态，无存储）
Captcha captcha = OpenCaptcha.create();
String base64 = captcha.toBase64DataUrl();  // 用于 <img> 标签
String answer = captcha.answer();           // 正确答案

// 算术验证码
Captcha mathCaptcha = OpenCaptcha.create(CaptchaConfig.builder()
    .type(CaptchaType.ARITHMETIC)
    .width(200)
    .height(80)
    .build());

// GIF 动画验证码
Captcha gifCaptcha = OpenCaptcha.create(CaptchaConfig.builder()
    .type(CaptchaType.GIF)
    .length(5)
    .build());

// 完整工作流：带存储和验证（构建器模式）
OpenCaptcha openCaptcha = OpenCaptcha.builder()
    .store(CaptchaStore.memory())
    .build();
Captcha stored = openCaptcha.generate();
ValidationResult result = openCaptcha.validate(stored.id(), userInput);
if (result.success()) {
    // 验证通过
}
```

### 音频验证码

```java
import cloud.opencode.base.captcha.*;

// 音频验证码 — WAV 输出，WCAG 无障碍合规
Captcha audio = OpenCaptcha.audio();
String wavBase64 = audio.toBase64DataUrl(); // data:audio/wav;base64,...
String answer = audio.answer();             // 字母数字验证码
```

### 拼接验证码

```java
import cloud.opencode.base.captcha.*;
import java.util.List;

// 拼接验证码 — 用户重新排列打乱的图片碎片
Captcha jigsaw = OpenCaptcha.jigsaw();
List<String> pieces = (List<String>) jigsaw.metadata().get("pieces"); // Base64 碎片图像
int gridSize = (int) jigsaw.metadata().get("gridSize");               // 3（3x3 网格）
String answer = jigsaw.answer();                                       // 如 "1,2,0,..."
```

### PoW（工作量证明）验证码

```java
import cloud.opencode.base.captcha.*;

// PoW 无感验证 — 无需用户交互
Captcha pow = OpenCaptcha.pow();
String challenge = (String) pow.metadata().get("challenge");
int difficulty = (int) pow.metadata().get("difficulty");  // 前导零位数（默认 20）
// 客户端计算 nonce，使 SHA-256(challenge + nonce) 具有 'difficulty' 个前导零位
```

### 抗 OCR 增强配置

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.support.CaptchaStrength;

// HARD 强度自动启用：随机字体、贝塞尔噪声、轮廓阴影、10% 字符重叠
CaptchaConfig hardConfig = CaptchaStrength.HARD.toConfig();
Captcha hardCaptcha = OpenCaptcha.create(hardConfig);

// EXTREME 强度：所有 HARD 特性 + 正弦波变形 + 20% 重叠
CaptchaConfig extremeConfig = CaptchaStrength.EXTREME.toConfig();
Captcha extremeCaptcha = OpenCaptcha.create(extremeConfig);
```

### 自定义字体

```java
import cloud.opencode.base.captcha.*;

// 加载自定义 TTF/OTF 字体，每字符随机选择
CaptchaConfig customFont = CaptchaConfig.builder()
    .customFontPath("/path/to/font.ttf")
    .randomFontPerChar(true)
    .build();
Captcha captcha = OpenCaptcha.create(customFont);
```

### 轨迹分析

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.security.TrajectoryData;
import cloud.opencode.base.captcha.security.TrajectoryData.Point;
import cloud.opencode.base.captcha.store.CaptchaStore;
import cloud.opencode.base.captcha.validator.BehaviorCaptchaValidator;
import java.util.List;

// 使用轨迹数据增强机器人检测
CaptchaStore store = CaptchaStore.memory();
BehaviorCaptchaValidator validator = new BehaviorCaptchaValidator(store);

List<Point> points = List.of(
    new Point(0, 0), new Point(10, 5), new Point(20, 8),
    new Point(35, 12), new Point(50, 10), new Point(70, 15)
);
List<Long> timestamps = List.of(0L, 50L, 120L, 200L, 300L, 420L);
TrajectoryData trajectory = new TrajectoryData(points, timestamps, 420L);

ValidationResult result = validator.validate(captchaId, answer, clientId, trajectory);
if (result.success()) {
    // 人类验证通过
}
```

### 指标收集

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.support.CaptchaMetrics;
import cloud.opencode.base.captcha.support.CaptchaMetrics.MetricsSnapshot;

// 启用指标收集
CaptchaMetrics metrics = CaptchaMetrics.create();
OpenCaptcha openCaptcha = OpenCaptcha.builder()
    .metrics(metrics)
    .build();

openCaptcha.generate();
openCaptcha.validate(id, answer);

// 获取指标快照
MetricsSnapshot snapshot = metrics.snapshot();
long generated = snapshot.totalGenerated();       // 生成总数
double successRate = snapshot.successRate();       // 成功率
```

### 预生成缓冲池

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.support.CaptchaPool;

// 后台预生成验证码，即时取用
try (CaptchaPool pool = CaptchaPool.builder()
        .config(CaptchaConfig.defaults())
        .poolSize(200)
        .build()) {
    Captcha captcha = pool.take(); // O(1) 从池获取，池空则实时生成
}
```

### 组合验证器

```java
import cloud.opencode.base.captcha.validator.*;
import cloud.opencode.base.captcha.store.CaptchaStore;

// 链式串联多个验证器 — 首个失败即短路返回
CaptchaStore store = CaptchaStore.memory();
CaptchaValidator validator = CaptchaValidator.composite(
    CaptchaValidator.simple(store),
    CaptchaValidator.timeBased(store)
);
```

### 测试模式

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.generator.TestCaptchaGenerator;

// 固定答案，方便单元测试
TestCaptchaGenerator testGen = new TestCaptchaGenerator("1234");
Captcha captcha = testGen.generate(CaptchaConfig.defaults());
assert captcha.answer().equals("1234");
```

### 哈希答案存储

```java
import cloud.opencode.base.captcha.store.*;

// 答案哈希后存储，纵深防御
HashedCaptchaStore store = CaptchaStore.hashed(CaptchaStore.memory());
store.store("id", "answer", Duration.ofMinutes(5));
boolean valid = store.verifyAndRemove("id", "answer"); // true
```

### 事件监听

```java
import cloud.opencode.base.captcha.*;

// 注册生命周期事件回调
CaptchaEventDispatcher dispatcher = new CaptchaEventDispatcher();
dispatcher.addListener(new CaptchaEventListener() {
    @Override public void onGenerated(Captcha captcha) {
        log.info("验证码已生成: {}", captcha.id());
    }
    @Override public void onValidationFailure(String id, ValidationResult.ResultCode reason) {
        log.warn("验证码验证失败: {} 原因={}", id, reason);
    }
});

OpenCaptcha openCaptcha = OpenCaptcha.builder()
    .eventListener(dispatcher)
    .build();
```

## 环境要求

- Java 25+

## 许可证

Apache License 2.0
