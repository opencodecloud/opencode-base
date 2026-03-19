# OpenCode Base Money

**金额和货币工具库 - 精确金额计算、多币种支持、中文大写转换、金额分摊，适用于 JDK 25+**

`opencode-base-money` 提供全面的不可变金额表示，基于 BigDecimal 精确运算，支持多币种、汇率转换、中文大写金额格式化、金额分摊和金融验证。

## 功能特性

- **不可变金额类型**：线程安全的 `Money` 记录，BigDecimal 精度
- **多币种支持**：内置货币定义，可配置小数位数
- **精确运算**：加、减、乘、除，正确的舍入模式
- **中文大写**：金额转换为中文财务大写字符
- **金额分摊**：按比例、百分比和等额分摊，正确处理余额
- **汇率转换**：可插拔汇率提供者的汇率转换
- **格式化**：货币符号、会计、紧凑和货币代码格式化
- **验证**：金额解析、正数/非负数验证
- **聚合运算**：对金额集合的求和、平均、最大、最小值
- **折扣与税**：应用折扣率、计算税额、加税

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-money</artifactId>
    <version>1.0.0</version>
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

// 运算
Money sum = m1.add(Money.of("50.00"));
Money diff = m1.subtract(Money.of("25.25"));
Money product = m1.multiply(2);
Money quotient = m1.divide(3);

// 格式化
System.out.println(m1.format());              // ¥100.50
System.out.println(m1.toChineseUpperCase());  // 壹佰元伍角

// 通过门面转中文大写
String chinese = OpenMoney.toChineseUpperCase(new BigDecimal("1234.56"));
// 壹仟贰佰叁拾肆元伍角陆分

// 按比例分摊
List<Money> parts = OpenMoney.allocate(Money.of("100"), 1, 2, 3);
// [¥16.67, ¥33.33, ¥50.00]

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

## 类参考

| 类名 | 说明 |
|------|------|
| `OpenMoney` | 主门面 - 所有金额操作的静态工具类 |
| `Money` | 表示带货币的金额的不可变记录 |
| `Currency` | 货币定义，包含代码、符号和小数位数 |
| `AllocationUtil` | 按比例、百分比和等额分摊的金额分配 |
| `MoneyCalcUtil` | 聚合（求和、平均、最大、最小）、折扣和税计算 |
| `CurrencyMismatchException` | 操作不同货币时的异常 |
| `ExchangeRateException` | 汇率操作失败异常 |
| `InvalidAmountException` | 无效金额值异常 |
| `MoneyErrorCode` | 金额异常的错误码枚举 |
| `MoneyException` | 金额模块基础异常 |
| `ExchangeRate` | 两种货币之间的汇率记录 |
| `ExchangeRateProvider` | 汇率数据源的 SPI 接口 |
| `FixedRateProvider` | 固定汇率提供者实现 |
| `ChineseUtil` | 中文大写金额转换工具 |
| `MoneyFormatUtil` | 金额格式化（符号、代码、会计、紧凑） |
| `MoneyValidator` | 金额解析和验证工具 |

## 环境要求

- Java 25+（使用记录类）
- 无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
