# OpenCode Base Money

**金额和货币工具库 - 精确金额计算、多币种支持、中文大写转换、金额分摊、舍入策略，适用于 JDK 25+**

`opencode-base-money` 提供全面的不可变金额表示，基于 BigDecimal 精确运算，支持多币种（23种货币）、汇率转换、中文大写金额格式化、金额分摊、金融验证、金额区间和多种舍入策略。

## 功能特性

- **不可变金额类型**：线程安全的 `Money` 记录，BigDecimal 精度
- **多币种支持**：23种内置货币定义（CNY、USD、EUR、GBP、JPY 等），可配置小数位数
- **精确运算**：加、减、乘、除，正确的舍入模式
- **百分比运算**：`percent()`、`addPercent()`、`subtractPercent()` 直接在 Money 上操作
- **比较工具**：静态 `Money.min()`、`Money.max()`，实例 `clamp()`
- **金额区间**：`MoneyRange` 记录，支持范围检查、夹紧、重叠、交集
- **舍入策略**：瑞典舍入、银行家舍入、步进舍入、向上/向下取整
- **中文大写**：金额转换为中文财务大写字符
- **金额分摊**：按比例、百分比和等额分摊，正确处理余额
- **汇率转换**：可插拔汇率提供者的汇率转换
- **格式化**：货币符号、会计、紧凑和货币代码格式化
- **验证**：金额解析、正数/非负数验证
- **聚合运算**：对金额集合的求和、平均、最大、最小值
- **折扣与税**：应用折扣率、计算税额、加税/去税
- **统一异常**：所有异常继承 `OpenException`，携带错误码

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-money</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.money.OpenMoney;
import cloud.opencode.base.money.Money;
import cloud.opencode.base.money.Currency;

// 创建金额
Money m1 = Money.of("100.50");          // 默认人民币
Money m2 = Money.of("50.25", Currency.USD);
Money m3 = Money.ofCents(10050);         // ¥100.50
Money m4 = Money.ofMinorUnits(10050, Currency.USD);  // $100.50

// 运算
Money sum = m1.add(Money.of("50.00"));
Money diff = m1.subtract(Money.of("25.25"));
Money product = m1.multiply(2);
Money quotient = m1.divide(3);

// 百分比运算
Money tax = m1.percent(13);              // ¥13.07（m1 的 13%）
Money withTax = m1.addPercent(13);       // ¥113.57（m1 + 13%）
Money discounted = m1.subtractPercent(20); // ¥80.40（m1 - 20%）

// 比较
Money bigger = Money.max(m1, Money.of("200"));
Money clamped = m1.clamp(Money.of("10"), Money.of("80"));  // ¥80.00

// 格式化
System.out.println(m1.format());              // ¥100.50
System.out.println(m1.toChineseUpperCase());  // 壹佰元伍角
```

### 金额区间

```java
import cloud.opencode.base.money.MoneyRange;

MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));
range.contains(Money.of("50"));     // true
range.clamp(Money.of("150"));       // ¥100.00
range.width();                       // ¥90.00
range.midpoint();                    // ¥55.00

MoneyRange other = MoneyRange.of(Money.of("80"), Money.of("200"));
range.overlaps(other);               // true
range.intersection(other);           // [¥80.00, ¥100.00]
range.span(other);                   // [¥10.00, ¥200.00]
```

### 舍入策略

```java
import cloud.opencode.base.money.calc.MoneyRounding;

Money m = Money.of("10.23");
MoneyRounding.swedish(m);                                    // ¥10.25（最近的 0.05）
MoneyRounding.bankers(m);                                    // ¥10.23（银行家舍入）
MoneyRounding.roundToStep(m, new BigDecimal("0.5"));        // ¥10.00
MoneyRounding.ceilToStep(m, BigDecimal.ONE);                // ¥11
MoneyRounding.floorToStep(m, BigDecimal.ONE);               // ¥10
```

### 分摊与聚合

```java
// 按比例分摊
List<Money> parts = OpenMoney.allocate(Money.of("100"), 1, 2, 3);

// 等额分摊
List<Money> split = OpenMoney.split(Money.of("100"), 3);

// 聚合运算
Money total = OpenMoney.sum(List.of(Money.of("100"), Money.of("200")));

// 折扣与税
Money discounted = OpenMoney.applyDiscount(Money.of("100"), new BigDecimal("0.2"));
Money withTax = OpenMoney.addTax(Money.of("100"), new BigDecimal("0.13"));

// 汇率转换
Money usd = OpenMoney.convert(Money.of("100"), Currency.USD, new BigDecimal("0.14"));
```

### 汇率转换

```java
import cloud.opencode.base.money.exchange.FixedRateProvider;
import cloud.opencode.base.money.exchange.ExchangeRate;

// 构建汇率提供者
FixedRateProvider provider = FixedRateProvider.builder()
    .rate(Currency.CNY, Currency.USD, "0.14")
    .rate(Currency.CNY, Currency.EUR, "0.13")
    .build();

// 或使用预设的常用人民币汇率
FixedRateProvider common = FixedRateProvider.withCommonCnyRates();

// 转换
Money usd = provider.convert(Money.of("100"), Currency.USD);  // $14.00

// 汇率信息
ExchangeRate rate = provider.getRateOrThrow(Currency.CNY, Currency.USD);
System.out.println(rate.format());    // 1 CNY = 0.14 USD
ExchangeRate inverse = rate.inverse(); // USD → CNY
```

### 格式化

```java
import cloud.opencode.base.money.format.MoneyFormatUtil;

Money m = Money.of("1234567.89");
MoneyFormatUtil.format(m);              // ¥1,234,567.89
MoneyFormatUtil.formatWithCode(m);      // CNY 1,234,567.89
MoneyFormatUtil.formatAccounting(m);    // ¥1,234,567.89
MoneyFormatUtil.formatWithSign(m);      // +¥1,234,567.89
MoneyFormatUtil.formatNoGrouping(m);    // ¥1234567.89
MoneyFormatUtil.formatCompact(m);       // ¥123.46万
MoneyFormatUtil.formatWithNameZh(m);    // 人民币 1,234,567.89
```

### 验证

```java
import cloud.opencode.base.money.validation.MoneyValidator;

// 解析并验证
BigDecimal amount = MoneyValidator.validateAndParse("100.50");
BigDecimal amount2 = MoneyValidator.validateAndParse("100.505", 3); // 自定义最大小数位

// 检查有效性
MoneyValidator.isValid("100.50");    // true
MoneyValidator.isValid("abc");       // false

// 验证约束
MoneyValidator.validatePositive(Money.of("100"));
MoneyValidator.validateNonNegative(Money.of("0"));
MoneyValidator.validateNotZero(Money.of("1"));
MoneyValidator.validateRange(Money.of("50"), Money.of("0"), Money.of("100"));
```

### 高级分摊

```java
import cloud.opencode.base.money.calc.AllocationUtil;

Money total = Money.of("1000");

// 按 BigDecimal 权重分摊
List<Money> weighted = AllocationUtil.allocateByWeights(total,
    new BigDecimal("1.5"), new BigDecimal("2.5"), new BigDecimal("6"));

// 带最小金额分摊
List<Money> withMin = AllocationUtil.splitWithMinimum(total, 5, Money.of("50"));

// 轮询分摊（逐分分配）
List<Money> robin = AllocationUtil.splitRoundRobin(total, 3);

// 验证分摊
boolean ok = AllocationUtil.verify(total, robin);  // true
```

## API 参考

### Money

| 方法 | 说明 |
|------|------|
| `of(String)` / `of(String, Currency)` | 从字符串创建 |
| `of(BigDecimal)` / `of(BigDecimal, Currency)` | 从 BigDecimal 创建 |
| `of(long)` / `of(long, Currency)` | 从 long 创建 |
| `ofYuan(long)` | 从元创建人民币 |
| `ofCents(long)` | 从分创建人民币 |
| `ofMinorUnits(long, Currency)` | 从最小单位创建任意币种 |
| `zero()` / `zero(Currency)` | 创建零金额 |
| `add(Money)` / `subtract(Money)` | 加法 / 减法（同币种） |
| `multiply(BigDecimal/long/double)` | 乘法，自动舍入到货币精度 |
| `divide(BigDecimal/long/double)` | 除法，自动舍入到货币精度 |
| `negate()` / `abs()` / `round()` | 取反 / 绝对值 / 舍入到货币精度 |
| `percent(int/BigDecimal)` | 计算百分比（如 13 表示 13%） |
| `addPercent(int/BigDecimal)` | 加百分比（加价） |
| `subtractPercent(int/BigDecimal)` | 减百分比（折扣） |
| `max(Money, Money)` / `min(Money, Money)` | 静态最大值 / 最小值 |
| `clamp(Money min, Money max)` | 夹紧到范围 |
| `compareTo(Money)` | 比较（同币种） |
| `isGreaterThan/isLessThan/isGreaterOrEqual/isLessOrEqual` | 比较谓词 |
| `isPositive()` / `isNegative()` / `isZero()` | 符号谓词 |
| `isNonNegative()` / `isNonPositive()` | 非负 / 非正谓词 |
| `toCents()` / `toMinorUnits()` | 转换为最小单位（long） |
| `convertTo(Currency, BigDecimal)` | 汇率转换 |
| `format()` / `formatNumber()` | 带/不带货币符号格式化 |
| `toChineseUpperCase()` | 转换为中文大写 |

### MoneyRange

| 方法 | 说明 |
|------|------|
| `of(Money, Money)` / `singleton(Money)` | 创建区间 / 单点区间 |
| `currency()` | 获取区间货币 |
| `contains(Money)` / `contains(MoneyRange)` | 检查包含 |
| `clamp(Money)` | 夹紧值到区间 |
| `overlaps(MoneyRange)` | 检查是否重叠 |
| `intersection(MoneyRange)` | 获取交集（无重叠返回 null） |
| `span(MoneyRange)` | 获取并集外包区间 |
| `gap(MoneyRange)` | 获取间隙（重叠返回 null） |
| `isSingleton()` | 检查是否为单点 |
| `width()` / `midpoint()` | 获取宽度 / 中点 |

### MoneyRounding

| 方法 | 说明 |
|------|------|
| `swedish(Money)` | 瑞典舍入（最近的 0.05） |
| `bankers(Money)` | 银行家舍入（HALF_EVEN） |
| `standard(Money)` | 标准舍入（HALF_UP） |
| `ceil(Money)` / `floor(Money)` | 向上 / 向下取整到货币精度 |
| `roundToStep(Money, BigDecimal)` | 四舍五入到最近的步进值 |
| `ceilToStep(Money, BigDecimal)` | 向上取整到最近的步进值 |
| `floorToStep(Money, BigDecimal)` | 向下取整到最近的步进值 |
| `round(Money, RoundingMode)` | 使用指定模式舍入 |
| `round(Money, int, RoundingMode)` | 使用指定精度和模式舍入 |

### AllocationUtil

| 方法 | 说明 |
|------|------|
| `allocate(Money, int...)` | 按整数比例分摊 |
| `allocateByPercent(Money, int...)` | 按百分比分摊（须合计 100） |
| `allocateByWeights(Money, BigDecimal...)` | 按小数权重分摊 |
| `split(Money, int)` | 等额分摊为 N 份 |
| `splitWithMinimum(Money, int, Money)` | 带保底最小金额的分摊 |
| `splitRoundRobin(Money, int)` | 轮询逐分分配 |
| `verify(Money, List<Money>)` | 验证分摊总和等于原额 |

### MoneyCalcUtil

| 方法 | 说明 |
|------|------|
| `sum(Collection<Money>)` | 集合求和 |
| `sum(Collection<Money>, Currency)` | 指定默认货币的集合求和 |
| `average(Collection<Money>)` | 集合平均值 |
| `max(Collection<Money>)` / `min(Collection<Money>)` | 集合最大 / 最小值 |
| `percentage(Money, Money, int)` | 计算比率（0-1 小数） |
| `percentageInt(Money, Money)` | 计算比率（0-100 整数） |
| `applyDiscount(Money, BigDecimal)` | 应用折扣率（0-1） |
| `applyDiscountPercent(Money, int)` | 应用折扣百分比（0-100） |
| `calculateTax(Money, BigDecimal)` | 计算税额 |
| `addTax(Money, BigDecimal)` | 加税 |
| `removeTax(Money, BigDecimal)` | 去税（从含税价提取税前金额） |
| `roundToNearest(Money, BigDecimal)` | 四舍五入到最近的值 |
| `areEqual(Money, Money)` | 忽略精度比较是否相等 |

### MoneyFormatUtil

| 方法 | 说明 |
|------|------|
| `format(Money)` | 带货币符号格式化（如 ¥1,234.56） |
| `formatNumber(Money)` | 仅数字格式化（如 1,234.56） |
| `formatNumber(BigDecimal, int)` | 按指定精度格式化数字 |
| `formatWithCode(Money)` | 带货币代码格式化（如 CNY 1,234.56） |
| `formatWithNameZh(Money)` | 带中文名称格式化（如 人民币 1,234.56） |
| `formatAccounting(Money)` | 会计格式（负数用括号） |
| `formatWithSign(Money)` | 始终显示 + 或 - 号 |
| `formatNoGrouping(Money)` | 无千位分隔符 |
| `formatCompact(Money)` | 紧凑格式（大数用万/亿） |
| `format(Money, Locale)` | 按地区格式化 |
| `format(Money, String)` | 自定义 DecimalFormat 模式 |
| `formatPercent(BigDecimal, int)` | 格式化百分比字符串 |

### MoneyValidator

| 方法 | 说明 |
|------|------|
| `validateAndParse(String)` | 解析并验证金额字符串（默认最多2位小数） |
| `validateAndParse(String, int)` | 自定义最大小数位数 |
| `validateRange(BigDecimal)` | 验证在 ±1000亿 范围内 |
| `validatePositive(Money)` | 断言为正 |
| `validateNonNegative(Money)` | 断言非负 |
| `validateNotZero(Money)` | 断言非零 |
| `validateRange(Money, Money, Money)` | 断言在 [最小值, 最大值] 内 |
| `isValid(String)` / `isValid(String, int)` | 检查有效性（不抛异常） |
| `getMaxAmount()` / `getMinAmount()` | 获取边界值（±1000亿） |

### ExchangeRate

| 方法 | 说明 |
|------|------|
| `of(Currency, Currency, BigDecimal)` | 创建汇率 |
| `of(Currency, Currency, String)` | 从字符串创建汇率 |
| `of(Currency, Currency, BigDecimal, LocalDateTime)` | 创建带时间戳的汇率 |
| `identity(Currency)` | 创建 1:1 等值汇率 |
| `convert(BigDecimal)` | 转换金额 |
| `inverse()` | 获取逆汇率 |
| `isExpired(int)` | 检查是否超过 N 小时 |
| `canConvert(Currency, Currency)` | 检查是否适用于给定货币对 |
| `format()` | 格式化为字符串（如 "1 CNY = 0.14 USD"） |

### Currency

| 方法 | 说明 |
|------|------|
| `getCode()` / `getSymbol()` | ISO 代码 / 符号（如 "CNY" / "¥"） |
| `getName()` / `getNameZh()` / `getNameEn()` | 默认/中文/英文名称 |
| `getScale()` | 小数位数（如 CNY 为 2，JPY 为 0） |
| `of(String)` | 按代码查找（不区分大小写） |
| `isSupported(String)` | 检查代码是否支持 |

## 支持的货币

CNY, USD, EUR, GBP, JPY, HKD, AUD, SGD, KRW, TWD, INR, THB, NZD, CAD, BRL, MXN, CHF, SEK, NOK, DKK, RUB, AED, ZAR

## 环境要求

- Java 25+（使用记录类）
- 无外部依赖（仅依赖 opencode-base-core）

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
