# OpenCode Base Money

**Money and currency utilities - precise amount calculation, multi-currency support, Chinese uppercase conversion, allocation for JDK 25+**

`opencode-base-money` provides a comprehensive, immutable money representation with precise BigDecimal arithmetic, multi-currency support, exchange rate conversion, Chinese uppercase amount formatting, allocation/splitting, and financial validation.

## Features

- **Immutable Money Type**: Thread-safe `Money` record with `BigDecimal` precision
- **Multi-Currency Support**: Built-in currency definitions with configurable scale
- **Precise Arithmetic**: Add, subtract, multiply, divide with proper rounding
- **Chinese Uppercase**: Convert amounts to Chinese financial uppercase characters
- **Money Allocation**: Ratio-based, percentage-based, and even-split allocation with remainder handling
- **Exchange Rates**: Rate conversion with pluggable exchange rate providers
- **Formatting**: Currency symbol, accounting, compact, and code-based formatting
- **Validation**: Amount parsing, positive/non-negative validation
- **Aggregation**: Sum, average, max, min over money collections
- **Discount & Tax**: Apply discount rates, calculate tax, add tax

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-money</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.money.OpenMoney;
import cloud.opencode.base.money.Money;
import cloud.opencode.base.money.Currency;

// Create money
Money m1 = Money.of("100.50");          // CNY by default
Money m2 = Money.of("50.25", Currency.USD);
Money m3 = Money.ofCents(10050);         // ¥100.50

// Arithmetic
Money sum = m1.add(Money.of("50.00"));
Money diff = m1.subtract(Money.of("25.25"));
Money product = m1.multiply(2);
Money quotient = m1.divide(3);

// Format
System.out.println(m1.format());              // ¥100.50
System.out.println(m1.toChineseUpperCase());  // 壹佰元伍角

// Chinese uppercase via facade
String chinese = OpenMoney.toChineseUpperCase(new BigDecimal("1234.56"));
// 壹仟贰佰叁拾肆元伍角陆分

// Allocate by ratios
List<Money> parts = OpenMoney.allocate(Money.of("100"), 1, 2, 3);
// [¥16.67, ¥33.33, ¥50.00]

// Even split
List<Money> split = OpenMoney.split(Money.of("100"), 3);

// Aggregation
Money total = OpenMoney.sum(List.of(Money.of("100"), Money.of("200")));

// Discount and tax
Money discounted = OpenMoney.applyDiscount(Money.of("100"), new BigDecimal("0.2"));
Money withTax = OpenMoney.addTax(Money.of("100"), new BigDecimal("0.13"));

// Exchange rate conversion
Money usd = OpenMoney.convert(Money.of("100"), Currency.USD, new BigDecimal("0.14"));
```

## Class Reference

| Class | Description |
|-------|-------------|
| `OpenMoney` | Main facade - static utility for all money operations |
| `Money` | Immutable record representing a monetary amount with currency |
| `Currency` | Currency definitions with code, symbol, and decimal scale |
| `AllocationUtil` | Money allocation by ratios, percentages, and even splits |
| `MoneyCalcUtil` | Aggregation (sum, avg, max, min), discount, and tax calculations |
| `CurrencyMismatchException` | Exception when operating on different currencies |
| `ExchangeRateException` | Exception for exchange rate operation failures |
| `InvalidAmountException` | Exception for invalid amount values |
| `MoneyErrorCode` | Error code enumeration for money exceptions |
| `MoneyException` | Base exception for money module |
| `ExchangeRate` | Exchange rate record between two currencies |
| `ExchangeRateProvider` | SPI interface for exchange rate data sources |
| `FixedRateProvider` | Fixed exchange rate provider implementation |
| `ChineseUtil` | Chinese uppercase amount conversion utility |
| `MoneyFormatUtil` | Money formatting (symbol, code, accounting, compact) |
| `MoneyValidator` | Amount parsing and validation utility |

## Requirements

- Java 25+ (uses records)
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
