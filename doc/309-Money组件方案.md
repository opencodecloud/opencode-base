# Money 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-money` 模块提供货币金额处理能力，基于 `BigDecimal` 实现精确计算，避免浮点误差。

**核心特性：**
- 精确的金额计算（基于 BigDecimal，避免浮点误差）
- 多币种支持（CNY、USD、EUR、GBP、JPY、HKD 等）
- 金额格式化（货币符号、千分位、会计格式、紧凑格式）
- 中文大写转换（发票、支票场景）
- 汇率管理与货币转换
- 金额分摊算法（按比例、按百分比、按权重、轮询分配）
- 金额验证（格式、范围、精度）
- 税费与折扣计算

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application Layer                         │
│                    (业务代码处理金额)                              │
└─────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                          Facade Layer                            │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                       OpenMoney                          │    │
│  │    (入口类：创建/大写转换/分摊计算/聚合运算/汇率转换/       │    │
│  │     格式化/折扣/税费/验证)                                 │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                          Core Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │    Money     │  │   Currency   │  │ ExchangeRate │          │
│  │ (金额 Record) │  │  (货币枚举)   │  │  (汇率 Record)│          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                                                       │
│         ▼ 算术运算 / 比较 / 转换 / 格式化                         │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │    add / subtract / multiply / divide / negate / abs      │   │
│  │    compareTo / isGreaterThan / isLessThan / isZero        │   │
│  │    toCents / convertTo / format / toChineseUpperCase      │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        ▼                         ▼                         ▼
┌───────────────┐        ┌───────────────┐        ┌───────────────┐
│    Format     │        │   Exchange    │        │     Calc      │
│    格式化模块  │        │    汇率模块    │        │    计算模块    │
├───────────────┤        ├───────────────┤        ├───────────────┤
│MoneyFormatUtil│        │ExchangeRate   │        │MoneyCalcUtil  │
│ChineseUtil    │        │Provider       │        │AllocationUtil │
│               │        │FixedProvider  │        │               │
├───────────────┤        ├───────────────┤        ├───────────────┤
│  Validation   │        │  Exception    │        │               │
│  验证模块      │        │   异常模块    │        │               │
├───────────────┤        ├───────────────┤        │               │
│MoneyValidator │        │MoneyException │        │               │
│               │        │MoneyErrorCode │        │               │
└───────────────┘        └───────────────┘        └───────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                        JDK BigDecimal                            │
│              (底层精确计算：RoundingMode, MathContext)            │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-money</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构

```
cloud.opencode.base.money
├── Money.java                    # 金额实体 (Record)
├── Currency.java                 # 货币类型枚举
├── OpenMoney.java                # 金额工具类门面（统一入口）
│
├── format/                       # 格式化
│   ├── MoneyFormatUtil.java      # 金额格式化工具类
│   └── ChineseUtil.java          # 中文大写转换
│
├── exchange/                     # 汇率相关
│   ├── ExchangeRate.java         # 汇率 (Record)
│   ├── ExchangeRateProvider.java # 汇率提供者接口
│   └── FixedRateProvider.java    # 固定汇率提供者
│
├── calc/                         # 计算相关
│   ├── MoneyCalcUtil.java        # 金额计算工具类（求和/平均/最大/最小/折扣/税费）
│   └── AllocationUtil.java       # 分摊算法工具类（按比例/百分比/权重/轮询）
│
├── validation/                   # 验证
│   └── MoneyValidator.java       # 金额验证器（格式/范围/精度/正负）
│
└── exception/                    # 异常
    ├── MoneyException.java       # 金额异常基类
    ├── MoneyErrorCode.java       # 错误码枚举
    ├── CurrencyMismatchException.java  # 币种不匹配异常
    ├── InvalidAmountException.java     # 无效金额异常
    └── ExchangeRateException.java      # 汇率异常
```

---

## 3. 核心 API

### 3.1 Currency - 货币类型枚举

货币枚举，定义了常用币种及其符号、名称、精度。

```java
public enum Currency {
    CNY, USD, EUR, GBP, JPY, HKD, ...
}
```

**主要方法：**

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getSymbol()` | `String` | 获取货币符号（如 `¥`、`$`、`€`） |
| `getNameZh()` | `String` | 获取中文名称（如 `人民币`） |
| `getNameEn()` | `String` | 获取英文名称（如 `Chinese Yuan`） |
| `getName()` | `String` | 获取名称（默认返回中文名） |
| `getCode()` | `String` | 获取货币代码（如 `CNY`、`USD`） |
| `getScale()` | `int` | 获取小数位数（如 CNY=2，JPY=0） |
| `of(String code)` | `Currency` | 根据代码查找货币类型 |
| `isSupported(String code)` | `boolean` | 判断是否支持指定货币代码 |

**使用示例：**

```java
Currency cny = Currency.CNY;
System.out.println(cny.getSymbol());   // ¥
System.out.println(cny.getName());     // 人民币
System.out.println(cny.getScale());    // 2

Currency usd = Currency.of("USD");     // 根据代码查找
boolean supported = Currency.isSupported("EUR");  // true
```

### 3.2 Money - 金额实体

不可变金额类型（Record），内部持有 `BigDecimal` 金额和 `Currency` 币种，所有运算返回新实例。

```java
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money>
```

#### 3.2.1 工厂方法

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `of(BigDecimal amount, Currency currency)` | 金额、币种 | `Money` | 指定金额和币种 |
| `of(BigDecimal amount)` | 金额 | `Money` | 默认人民币 |
| `of(String amount, Currency currency)` | 金额字符串、币种 | `Money` | 从字符串创建 |
| `of(String amount)` | 金额字符串 | `Money` | 从字符串创建（默认人民币） |
| `of(long amount, Currency currency)` | 整数金额、币种 | `Money` | 从整数创建 |
| `of(long amount)` | 整数金额 | `Money` | 从整数创建（默认人民币） |
| `ofYuan(long yuan)` | 元 | `Money` | 从元数创建（人民币） |
| `ofCents(long cents)` | 分 | `Money` | 从分数创建（人民币） |
| `zero(Currency currency)` | 币种 | `Money` | 零值 |
| `zero()` | 无 | `Money` | 零值人民币 |

**示例：**

```java
Money m1 = Money.of("100.50");              // ¥100.50
Money m2 = Money.ofCents(10050);            // ¥100.50
Money m3 = Money.of("50.25", Currency.USD); // $50.25
Money m4 = Money.ofYuan(100);               // ¥100.00
Money m5 = Money.zero();                    // ¥0.00
```

#### 3.2.2 算术运算

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `add(Money other)` | 另一金额（需同币种） | `Money` | 加法 |
| `subtract(Money other)` | 另一金额（需同币种） | `Money` | 减法 |
| `multiply(BigDecimal multiplier)` | 乘数 | `Money` | 乘法 |
| `multiply(long multiplier)` | 整数乘数 | `Money` | 乘法 |
| `multiply(double multiplier)` | 浮点乘数 | `Money` | 乘法 |
| `divide(BigDecimal divisor)` | 除数 | `Money` | 除法（HALF_UP 舍入） |
| `divide(long divisor)` | 整数除数 | `Money` | 除法 |
| `divide(double divisor)` | 浮点除数 | `Money` | 除法 |
| `negate()` | 无 | `Money` | 取反 |
| `abs()` | 无 | `Money` | 绝对值 |
| `round()` | 无 | `Money` | 按币种精度四舍五入 |

**示例：**

```java
Money price = Money.of("199.00");
Money total = price.multiply(3);              // ¥597.00
Money discounted = total.subtract(Money.of("50.00"));  // ¥547.00
Money perPerson = discounted.divide(3);       // ¥182.33
Money refund = perPerson.negate();            // ¥-182.33
```

#### 3.2.3 比较方法

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `compareTo(Money other)` | 另一金额 | `int` | Comparable 实现 |
| `isGreaterThan(Money other)` | 另一金额 | `boolean` | 大于 |
| `isLessThan(Money other)` | 另一金额 | `boolean` | 小于 |
| `isGreaterOrEqual(Money other)` | 另一金额 | `boolean` | 大于等于 |
| `isLessOrEqual(Money other)` | 另一金额 | `boolean` | 小于等于 |
| `isPositive()` | 无 | `boolean` | 是否为正数 |
| `isNegative()` | 无 | `boolean` | 是否为负数 |
| `isZero()` | 无 | `boolean` | 是否为零 |

#### 3.2.4 转换与格式化

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `toCents()` | 无 | `long` | 转换为分（移动小数点2位） |
| `convertTo(Currency target, BigDecimal rate)` | 目标币种、汇率 | `Money` | 货币转换 |
| `format()` | 无 | `String` | 格式化（带货币符号，如 `¥100.50`） |
| `formatNumber()` | 无 | `String` | 格式化纯数字（如 `1,234,567.89`） |
| `toChineseUpperCase()` | 无 | `String` | 中文大写（如 `壹佰元伍角`） |

**示例：**

```java
Money m = Money.of("1234567.89");
System.out.println(m.format());              // ¥1,234,567.89
System.out.println(m.formatNumber());        // 1,234,567.89
System.out.println(m.toChineseUpperCase());  // 壹佰贰拾叁万肆仟伍佰陆拾柒元捌角玖分

long cents = m.toCents();                    // 123456789

Money cny = Money.of("100", Currency.CNY);
Money usd = cny.convertTo(Currency.USD, new BigDecimal("0.14"));  // $14.00
```

### 3.3 OpenMoney - 门面入口类

统一入口类，提供金额创建、大写转换、分摊计算、聚合运算、汇率转换、格式化、折扣税费、验证等全部功能的静态方法。

#### 3.3.1 创建方法

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `of(String amount)` | 金额字符串 | `Money` | 创建人民币金额 |
| `of(String amount, Currency currency)` | 金额字符串、币种 | `Money` | 创建指定币种金额 |
| `ofCents(long cents)` | 分 | `Money` | 从分创建 |
| `zero()` | 无 | `Money` | 零值人民币 |
| `zero(Currency currency)` | 币种 | `Money` | 零值指定币种 |

#### 3.3.2 中文大写

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `toChineseUpperCase(BigDecimal amount)` | 金额 | `String` | 金额转中文大写 |
| `toChineseUpperCase(Money money)` | Money 对象 | `String` | Money 转中文大写 |

```java
String chinese = OpenMoney.toChineseUpperCase(new BigDecimal("1234.56"));
// 壹仟贰佰叁拾肆元伍角陆分
```

#### 3.3.3 分摊计算

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `allocate(Money total, int... ratios)` | 总金额、比例数组 | `List<Money>` | 按比例分摊 |
| `split(Money total, int parts)` | 总金额、份数 | `List<Money>` | 等额拆分 |
| `allocateByPercent(Money total, int... percentages)` | 总金额、百分比数组 | `List<Money>` | 按百分比分摊 |

```java
List<Money> parts = OpenMoney.allocate(Money.of("100"), 1, 2, 3);
// [¥16.67, ¥33.33, ¥50.00]（总和恒等于原值）

List<Money> split = OpenMoney.split(Money.of("100"), 3);
// [¥33.33, ¥33.33, ¥33.34]
```

#### 3.3.4 聚合运算

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `sum(Collection<Money> moneys)` | 金额集合 | `Money` | 求和 |
| `average(Collection<Money> moneys)` | 金额集合 | `Money` | 求平均 |
| `max(Collection<Money> moneys)` | 金额集合 | `Money` | 最大值 |
| `min(Collection<Money> moneys)` | 金额集合 | `Money` | 最小值 |

```java
List<Money> orders = List.of(Money.of("100"), Money.of("200"), Money.of("300"));
Money total = OpenMoney.sum(orders);      // ¥600.00
Money avg = OpenMoney.average(orders);    // ¥200.00
Money max = OpenMoney.max(orders);        // ¥300.00
Money min = OpenMoney.min(orders);        // ¥100.00
```

#### 3.3.5 折扣与税费

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `applyDiscount(Money money, BigDecimal discountRate)` | 金额、折扣率 | `Money` | 应用折扣 |
| `calculateTax(Money money, BigDecimal taxRate)` | 金额、税率 | `Money` | 计算税额 |
| `addTax(Money money, BigDecimal taxRate)` | 金额、税率 | `Money` | 含税金额 |

#### 3.3.6 汇率转换

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `convert(Money, Currency, BigDecimal rate)` | 金额、目标币种、汇率 | `Money` | 按汇率转换 |
| `convert(Money, Currency, ExchangeRateProvider)` | 金额、目标币种、汇率提供者 | `Money` | 通过提供者转换 |
| `rate(Currency, Currency, BigDecimal)` | 源币种、目标币种、汇率 | `ExchangeRate` | 创建汇率对象 |

#### 3.3.7 格式化

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `format(Money money)` | Money 对象 | `String` | 默认格式（`¥1,234.56`） |
| `formatWithCode(Money money)` | Money 对象 | `String` | 带货币代码（`CNY 1,234.56`） |
| `formatAccounting(Money money)` | Money 对象 | `String` | 会计格式 |
| `formatCompact(Money money)` | Money 对象 | `String` | 紧凑格式（如 `1.2万`） |

#### 3.3.8 验证

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `validateAndParse(String amountStr)` | 金额字符串 | `BigDecimal` | 验证并解析 |
| `validatePositive(Money money)` | Money 对象 | `void` | 验证正数（否则抛异常） |
| `validateNonNegative(Money money)` | Money 对象 | `void` | 验证非负（否则抛异常） |
| `isValid(String amountStr)` | 金额字符串 | `boolean` | 是否为合法金额 |
| `areEqual(Money m1, Money m2)` | 两个 Money | `boolean` | 金额是否相等 |

### 3.4 MoneyCalcUtil - 金额计算工具类

提供金额集合的聚合运算、折扣税费计算、百分比计算等方法。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `sum(Collection<Money>)` | 金额集合 | `Money` | 求和 |
| `sum(Collection<Money>, Currency)` | 金额集合、币种 | `Money` | 指定币种求和 |
| `average(Collection<Money>)` | 金额集合 | `Money` | 求平均 |
| `max(Collection<Money>)` | 金额集合 | `Money` | 最大值 |
| `min(Collection<Money>)` | 金额集合 | `Money` | 最小值 |
| `percentage(Money part, Money total, int scale)` | 部分、总额、精度 | `BigDecimal` | 计算百分比 |
| `percentageInt(Money part, Money total)` | 部分、总额 | `int` | 计算整数百分比 |
| `applyDiscount(Money, BigDecimal)` | 金额、折扣率 | `Money` | 应用折扣 |
| `applyDiscountPercent(Money, int)` | 金额、折扣百分比 | `Money` | 应用折扣百分比 |
| `calculateTax(Money, BigDecimal)` | 金额、税率 | `Money` | 计算税额 |
| `addTax(Money, BigDecimal)` | 金额、税率 | `Money` | 计算含税金额 |
| `removeTax(Money, BigDecimal)` | 含税金额、税率 | `Money` | 计算不含税金额 |
| `roundToNearest(Money, BigDecimal)` | 金额、取整单位 | `Money` | 按指定单位取整 |
| `areEqual(Money, Money)` | 两个 Money | `boolean` | 金额值相等 |

```java
List<Money> orders = List.of(Money.of("100"), Money.of("200"), Money.of("300"));
Money total = MoneyCalcUtil.sum(orders);              // ¥600.00
Money avg = MoneyCalcUtil.average(orders);            // ¥200.00

Money price = Money.of("1000");
Money discounted = MoneyCalcUtil.applyDiscount(price, new BigDecimal("0.8"));  // ¥800.00
Money tax = MoneyCalcUtil.calculateTax(price, new BigDecimal("0.13"));         // ¥130.00
Money withTax = MoneyCalcUtil.addTax(price, new BigDecimal("0.13"));           // ¥1130.00
Money noTax = MoneyCalcUtil.removeTax(Money.of("1130"), new BigDecimal("0.13")); // ¥1000.00
```

### 3.5 AllocationUtil - 分摊算法工具类

提供多种金额分摊算法，确保分摊后总和恒等于原值。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `allocate(Money total, int... ratios)` | 总金额、比例数组 | `List<Money>` | 按比例分摊 |
| `allocateByPercent(Money total, int... percentages)` | 总金额、百分比数组 | `List<Money>` | 按百分比分摊 |
| `split(Money total, int parts)` | 总金额、份数 | `List<Money>` | 等额拆分 |
| `allocateByWeights(Money total, BigDecimal... weights)` | 总金额、权重数组 | `List<Money>` | 按权重分摊 |
| `splitWithMinimum(Money total, int parts, Money minPerPart)` | 总金额、份数、最低金额 | `List<Money>` | 带最低金额的拆分 |
| `splitRoundRobin(Money total, int parts)` | 总金额、份数 | `List<Money>` | 轮询分配余数 |
| `verify(Money total, List<Money> parts)` | 总金额、分摊结果 | `boolean` | 验证分摊总和是否一致 |

```java
Money total = Money.of("100");

// 按比例 1:2:3 分摊
List<Money> parts = AllocationUtil.allocate(total, 1, 2, 3);
// [¥16.66, ¥33.33, ¥50.01]（余数归最后一份）

// 等额拆分
List<Money> split = AllocationUtil.split(total, 3);
// [¥33.33, ¥33.33, ¥33.34]

// 按权重分摊
List<Money> weighted = AllocationUtil.allocateByWeights(total,
    new BigDecimal("0.5"), new BigDecimal("0.3"), new BigDecimal("0.2"));

// 轮询分配余数
List<Money> roundRobin = AllocationUtil.splitRoundRobin(total, 3);

// 验证分摊结果
boolean valid = AllocationUtil.verify(total, parts);  // true
```

### 3.6 ExchangeRate - 汇率

不可变汇率记录，包含源币种、目标币种、汇率值和时间戳。

```java
public record ExchangeRate(Currency source, Currency target, BigDecimal rate, LocalDateTime timestamp)
```

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `of(Currency source, Currency target, BigDecimal rate)` | 源、目标、汇率 | `ExchangeRate` | 创建汇率 |
| `of(Currency source, Currency target, String rate)` | 源、目标、汇率字符串 | `ExchangeRate` | 从字符串创建 |
| `of(Currency, Currency, BigDecimal, LocalDateTime)` | 含时间戳 | `ExchangeRate` | 创建带时间戳的汇率 |
| `identity(Currency currency)` | 币种 | `ExchangeRate` | 同币种汇率（1:1） |
| `convert(BigDecimal amount)` | 金额 | `BigDecimal` | 按汇率转换金额 |
| `inverse()` | 无 | `ExchangeRate` | 反向汇率 |
| `isExpired(int maxAgeHours)` | 最大有效小时数 | `boolean` | 是否已过期 |
| `canConvert(Currency from, Currency to)` | 源、目标 | `boolean` | 是否可转换 |
| `format()` | 无 | `String` | 格式化显示 |

```java
ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, "0.14");
BigDecimal usd = rate.convert(new BigDecimal("100"));  // 14.00

ExchangeRate inverse = rate.inverse();  // USD -> CNY
boolean expired = rate.isExpired(24);   // 是否超过24小时
```

### 3.7 FixedRateProvider - 固定汇率提供者

实现 `ExchangeRateProvider` 接口，支持 Builder 模式构建。

```java
FixedRateProvider provider = FixedRateProvider.builder()
    .rate(Currency.CNY, Currency.USD, new BigDecimal("0.14"))
    .rate(Currency.CNY, Currency.EUR, new BigDecimal("0.13"))
    .build();

Optional<ExchangeRate> rate = provider.getRate(Currency.CNY, Currency.USD);
Money usd = provider.convert(Money.of("100"), Currency.USD);
```

| 方法 | 说明 |
|------|------|
| `builder()` | 创建 Builder |
| `withCommonCnyRates()` | 预设常见人民币汇率 |
| `getRate(Currency, Currency)` | 获取汇率 |
| `addRate(Currency, Currency, BigDecimal)` | 添加汇率 |
| `removeRate(Currency, Currency)` | 移除汇率 |
| `clearRates()` | 清除所有汇率 |
| `getAllRates()` | 获取所有汇率 |
| `getRateCount()` | 汇率数量 |

### 3.8 MoneyFormatUtil - 金额格式化工具类

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `format(Money money)` | Money | `String` | 带货币符号格式化（`¥1,234.56`） |
| `formatNumber(Money money)` | Money | `String` | 纯数字格式化（`1,234.56`） |
| `formatNumber(BigDecimal, int scale)` | 金额、精度 | `String` | 带精度的数字格式化 |
| `formatWithCode(Money money)` | Money | `String` | 带货币代码（`CNY 1,234.56`） |
| `formatWithNameZh(Money money)` | Money | `String` | 带中文名（`人民币 1,234.56`） |
| `formatAccounting(Money money)` | Money | `String` | 会计格式（负数用括号） |
| `formatWithSign(Money money)` | Money | `String` | 带正负号 |
| `formatNoGrouping(Money money)` | Money | `String` | 不带千分位 |
| `format(Money, Locale locale)` | Money、区域 | `String` | 按区域格式化 |
| `format(Money, String pattern)` | Money、模式 | `String` | 按自定义模式格式化 |
| `formatCompact(Money money)` | Money | `String` | 紧凑格式（`1.2万`/`3.5亿`） |
| `formatPercent(BigDecimal, int scale)` | 比率、精度 | `String` | 百分比格式化 |

```java
Money m = Money.of("1234567.89");
MoneyFormatUtil.format(m);            // ¥1,234,567.89
MoneyFormatUtil.formatWithCode(m);    // CNY 1,234,567.89
MoneyFormatUtil.formatAccounting(m);  // ¥1,234,567.89（负数显示括号）
MoneyFormatUtil.formatCompact(m);     // 123.5万
```

### 3.9 ChineseUtil - 中文大写转换

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `toUpperCase(BigDecimal amount)` | 金额 | `String` | 中文大写（如 `壹佰元伍角`） |
| `toSimplified(BigDecimal amount)` | 金额 | `String` | 简体中文大写 |

```java
ChineseUtil.toUpperCase(new BigDecimal("1234.56"));
// 壹仟贰佰叁拾肆元伍角陆分

ChineseUtil.toUpperCase(new BigDecimal("0"));
// 零元整

ChineseUtil.toUpperCase(new BigDecimal("-100.50"));
// 负壹佰元伍角
```

### 3.10 MoneyValidator - 金额验证器

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `validateAndParse(String amountStr)` | 金额字符串 | `BigDecimal` | 验证格式并解析 |
| `validateAndParse(String, int maxScale)` | 金额字符串、最大精度 | `BigDecimal` | 验证格式、精度并解析 |
| `validateRange(BigDecimal amount)` | 金额 | `void` | 验证范围（-1000亿~1000亿） |
| `validatePositive(Money money)` | Money | `void` | 验证正数 |
| `validateNonNegative(Money money)` | Money | `void` | 验证非负 |
| `validateNotZero(Money money)` | Money | `void` | 验证非零 |
| `validateRange(Money, Money min, Money max)` | 金额、最小、最大 | `void` | 验证范围 |
| `isValid(String amountStr)` | 金额字符串 | `boolean` | 是否合法金额 |
| `isValid(String, int maxScale)` | 金额字符串、精度 | `boolean` | 是否合法金额（含精度检查） |
| `getMaxAmount()` | 无 | `BigDecimal` | 获取最大金额限制 |
| `getMinAmount()` | 无 | `BigDecimal` | 获取最小金额限制 |

```java
BigDecimal amount = MoneyValidator.validateAndParse("100.50");
MoneyValidator.validatePositive(Money.of("100"));     // 通过
MoneyValidator.validateRange(Money.of("500"),
    Money.of("0"), Money.of("1000"));                 // 通过

boolean valid = MoneyValidator.isValid("100.50");     // true
boolean invalid = MoneyValidator.isValid("abc");      // false
```

---

## 4. 异常体系

### 4.1 异常层次结构

```
RuntimeException
└── MoneyException                          # 金额异常基类
    ├── InvalidAmountException              # 无效金额（格式错误/溢出/精度超限）
    ├── CurrencyMismatchException           # 币种不匹配
    └── ExchangeRateException               # 汇率异常（未找到/无效汇率）
```

### 4.2 MoneyErrorCode - 错误码枚举

| 错误码 | 说明 |
|--------|------|
| `INVALID_AMOUNT` (1001) | 无效金额 |
| `AMOUNT_FORMAT_ERROR` (1002) | 金额格式错误 |
| `AMOUNT_OVERFLOW` (1003) | 金额溢出 |
| `AMOUNT_PRECISION_ERROR` (1004) | 金额精度超限 |
| `AMOUNT_NEGATIVE` (1005) | 金额不能为负 |
| `CURRENCY_MISMATCH` (2001) | 币种不匹配 |
| `UNSUPPORTED_CURRENCY` (2002) | 不支持的币种 |
| `RATE_NOT_FOUND` (3001) | 汇率未找到 |
| `RATE_EXPIRED` (3002) | 汇率已过期 |
| `RATE_INVALID` (3003) | 无效汇率 |
| `ALLOCATION_ERROR` (4001) | 分摊计算错误 |
| `ZERO_DIVISOR` (4002) | 除数不能为零 |

**MoneyErrorCode 主要方法：**

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getCode()` | `int` | 数字错误码 |
| `getMessage()` | `String` | 英文错误信息 |
| `getMessageZh()` | `String` | 中文错误信息 |

### 4.3 异常类

**MoneyException** - 基类：

```java
public class MoneyException extends RuntimeException {
    public MoneyException(String message);
    public MoneyException(String message, MoneyErrorCode errorCode);
    public MoneyException(String message, Throwable cause, MoneyErrorCode errorCode);
    public MoneyException(String message, Throwable cause);
    public MoneyErrorCode getErrorCode();
}
```

**CurrencyMismatchException** - 币种不匹配：

```java
public class CurrencyMismatchException extends MoneyException {
    public CurrencyMismatchException(Currency expected, Currency actual);
    public CurrencyMismatchException(String message, Currency expected, Currency actual);
    public Currency getExpected();
    public Currency getActual();
}
```

**InvalidAmountException** - 无效金额：

```java
public class InvalidAmountException extends MoneyException {
    public InvalidAmountException(String message);
    public InvalidAmountException(String message, String invalidValue);
    public InvalidAmountException(String message, Throwable cause);
    public InvalidAmountException(String message, MoneyErrorCode errorCode);
    public static InvalidAmountException formatError(String value);
    public static InvalidAmountException precisionError(String value, int maxScale);
    public static InvalidAmountException overflow(String value);
    public String getInvalidValue();
}
```

**ExchangeRateException** - 汇率异常：

```java
public class ExchangeRateException extends MoneyException {
    public ExchangeRateException(String message);
    public ExchangeRateException(String message, MoneyErrorCode errorCode);
    public ExchangeRateException(String message, Currency source, Currency target);
    public static ExchangeRateException notFound(Currency source, Currency target);
    public static ExchangeRateException invalidRate(String rate);
    public Currency getSource();
    public Currency getTarget();
}
```

---

## 5. 线程安全与性能

### 5.1 线程安全保证

| 类 | 线程安全 | 说明 |
|----|----------|------|
| `Money` | 安全 | 不可变 Record，内部 BigDecimal 也是不可变 |
| `Currency` | 安全 | 枚举单例 |
| `ExchangeRate` | 安全 | 不可变 Record |
| `OpenMoney` | 安全 | 无状态静态方法类 |
| `MoneyCalcUtil` | 安全 | 无状态静态方法类 |
| `AllocationUtil` | 安全 | 无状态静态方法类 |
| `ChineseUtil` | 安全 | 无状态静态方法类 |
| `MoneyFormatUtil` | 安全 | 无状态静态方法类 |
| `MoneyValidator` | 安全 | 无状态静态方法类 |
| `FixedRateProvider` | 安全 | 内部使用 ConcurrentHashMap |

### 5.2 性能最佳实践

```java
// 推荐：链式计算
Money result = Money.of("1000")
    .multiply(0.9)             // 九折
    .subtract(Money.of("50"))  // 减优惠券
    .add(Money.of("10"));      // 加运费

// 推荐：批量操作使用聚合方法
List<Money> orders = ...;
Money total = OpenMoney.sum(orders);

// 推荐：使用 ofCents 避免字符串解析
Money m = Money.ofCents(10050);  // ¥100.50

// 推荐：分摊时一次性计算
List<Money> parts = OpenMoney.allocate(total, 1, 2, 3);

// 推荐：使用字符串构造 BigDecimal
BigDecimal amount = new BigDecimal("0.1");  // 精确

// 避免：使用 double 构造（精度丢失）
// BigDecimal amount = new BigDecimal(0.1);
```

---

## 6. 使用示例

### 6.1 基本使用

```java
// 创建金额
Money m1 = Money.of("100.50");
Money m2 = Money.ofCents(10050);
Money m3 = Money.of("50.25", Currency.USD);

// 运算
Money sum = m1.add(Money.of("50.00"));         // ¥150.50
Money diff = m1.subtract(Money.of("25.25"));   // ¥75.25
Money product = m1.multiply(2);                // ¥201.00

// 格式化
System.out.println(m1.format());               // ¥100.50
System.out.println(m1.toChineseUpperCase());   // 壹佰元伍角
```

### 6.2 订单金额计算

```java
Money itemPrice = Money.of("199.00");
int quantity = 3;
Money discount = Money.of("50.00");
Money shipping = Money.of("10.00");

Money total = itemPrice
    .multiply(quantity)      // ¥597.00
    .subtract(discount)      // ¥547.00
    .add(shipping);          // ¥557.00
```

### 6.3 分摊与验证

```java
Money total = Money.of("1000.00");

// 3人分摊
List<Money> shares = OpenMoney.split(total, 3);
// [¥333.33, ¥333.33, ¥333.34]

// 验证总和
boolean valid = AllocationUtil.verify(total, shares);  // true
```

### 6.4 汇率转换

```java
FixedRateProvider provider = FixedRateProvider.builder()
    .rate(Currency.CNY, Currency.USD, new BigDecimal("0.14"))
    .build();

Money cny = Money.of("100", Currency.CNY);
Money usd = OpenMoney.convert(cny, Currency.USD, provider);  // $14.00
```

### 6.5 数据库交互

```java
// 方案1：存储为分（long）
long cents = money.toCents();            // 保存
Money money = Money.ofCents(cents);      // 读取

// 方案2：存储为 DECIMAL
BigDecimal amount = money.amount();      // 保存
Money money = Money.of(amount, Currency.valueOf(currencyCode));  // 读取
```

---

## 7. 版本信息

| 属性 | 值 |
|------|-----|
| 模块名 | opencode-base-money |
| 最低 JDK | 25 |
| 第三方依赖 | 无 |
| 支持币种 | CNY, USD, EUR, GBP, JPY, HKD 等 |
