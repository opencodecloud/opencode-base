# OpenCode Base Money

**Money and currency utilities - precise amount calculation, multi-currency support, Chinese uppercase conversion, allocation, rounding strategies for JDK 25+**

`opencode-base-money` provides a comprehensive, immutable money representation with precise BigDecimal arithmetic, multi-currency support (23 currencies), exchange rate conversion, Chinese uppercase amount formatting, allocation/splitting, financial validation, money range, and multiple rounding strategies.

## Features

- **Immutable Money Type**: Thread-safe `Money` record with `BigDecimal` precision
- **Multi-Currency Support**: 23 built-in currency definitions (CNY, USD, EUR, GBP, JPY, etc.) with configurable scale
- **Precise Arithmetic**: Add, subtract, multiply, divide with proper rounding
- **Percentage Operations**: `percent()`, `addPercent()`, `subtractPercent()` directly on Money
- **Comparison Utilities**: Static `Money.min()`, `Money.max()`, instance `clamp()`
- **Money Range**: `MoneyRange` record for range checks, clamping, overlap, intersection
- **Rounding Strategies**: Swedish rounding, banker's rounding, step-based rounding, ceiling/floor
- **Chinese Uppercase**: Convert amounts to Chinese financial uppercase characters
- **Money Allocation**: Ratio-based, percentage-based, and even-split allocation with remainder handling
- **Exchange Rates**: Rate conversion with pluggable exchange rate providers
- **Formatting**: Currency symbol, accounting, compact, and code-based formatting
- **Validation**: Amount parsing, positive/non-negative validation
- **Aggregation**: Sum, average, max, min over money collections
- **Discount & Tax**: Apply discount rates, calculate tax, add/remove tax
- **Unified Exception**: All exceptions extend `OpenException` with error codes

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-money</artifactId>
    <version>1.0.3</version>
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
Money m4 = Money.ofMinorUnits(10050, Currency.USD);  // $100.50

// Arithmetic
Money sum = m1.add(Money.of("50.00"));
Money diff = m1.subtract(Money.of("25.25"));
Money product = m1.multiply(2);
Money quotient = m1.divide(3);

// Percentage operations
Money tax = m1.percent(13);              // ¥13.07 (13% of m1)
Money withTax = m1.addPercent(13);       // ¥113.57 (m1 + 13%)
Money discounted = m1.subtractPercent(20); // ¥80.40 (m1 - 20%)

// Comparison
Money bigger = Money.max(m1, Money.of("200"));
Money clamped = m1.clamp(Money.of("10"), Money.of("80"));  // ¥80.00

// Format
System.out.println(m1.format());              // ¥100.50
System.out.println(m1.toChineseUpperCase());  // 壹佰元伍角
```

### Money Range

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

### Rounding Strategies

```java
import cloud.opencode.base.money.calc.MoneyRounding;

Money m = Money.of("10.23");
MoneyRounding.swedish(m);                                    // ¥10.25 (nearest 0.05)
MoneyRounding.bankers(m);                                    // ¥10.23 (HALF_EVEN)
MoneyRounding.roundToStep(m, new BigDecimal("0.5"));        // ¥10.00
MoneyRounding.ceilToStep(m, BigDecimal.ONE);                // ¥11
MoneyRounding.floorToStep(m, BigDecimal.ONE);               // ¥10
```

### Allocation & Aggregation

```java
// Allocate by ratios
List<Money> parts = OpenMoney.allocate(Money.of("100"), 1, 2, 3);

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

### Exchange Rate Conversion

```java
import cloud.opencode.base.money.exchange.FixedRateProvider;
import cloud.opencode.base.money.exchange.ExchangeRate;

// Build rate provider
FixedRateProvider provider = FixedRateProvider.builder()
    .rate(Currency.CNY, Currency.USD, "0.14")
    .rate(Currency.CNY, Currency.EUR, "0.13")
    .build();

// Or use preset common CNY rates
FixedRateProvider common = FixedRateProvider.withCommonCnyRates();

// Convert
Money usd = provider.convert(Money.of("100"), Currency.USD);  // $14.00

// Exchange rate info
ExchangeRate rate = provider.getRateOrThrow(Currency.CNY, Currency.USD);
System.out.println(rate.format());    // 1 CNY = 0.14 USD
ExchangeRate inverse = rate.inverse(); // USD → CNY
```

### Formatting

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

### Validation

```java
import cloud.opencode.base.money.validation.MoneyValidator;

// Parse and validate
BigDecimal amount = MoneyValidator.validateAndParse("100.50");
BigDecimal amount2 = MoneyValidator.validateAndParse("100.505", 3); // custom max scale

// Check validity
MoneyValidator.isValid("100.50");    // true
MoneyValidator.isValid("abc");       // false

// Validate constraints
MoneyValidator.validatePositive(Money.of("100"));
MoneyValidator.validateNonNegative(Money.of("0"));
MoneyValidator.validateNotZero(Money.of("1"));
MoneyValidator.validateRange(Money.of("50"), Money.of("0"), Money.of("100"));
```

### Advanced Allocation

```java
import cloud.opencode.base.money.calc.AllocationUtil;

Money total = Money.of("1000");

// Allocate by BigDecimal weights
List<Money> weighted = AllocationUtil.allocateByWeights(total,
    new BigDecimal("1.5"), new BigDecimal("2.5"), new BigDecimal("6"));

// Split with minimum per part
List<Money> withMin = AllocationUtil.splitWithMinimum(total, 5, Money.of("50"));

// Round-robin allocation (distribute cents evenly)
List<Money> robin = AllocationUtil.splitRoundRobin(total, 3);

// Verify allocation
boolean ok = AllocationUtil.verify(total, robin);  // true
```

## API Reference

### Money

| Method | Description |
|--------|-------------|
| `of(String)` / `of(String, Currency)` | Create from string |
| `of(BigDecimal)` / `of(BigDecimal, Currency)` | Create from BigDecimal |
| `of(long)` / `of(long, Currency)` | Create from long |
| `ofYuan(long)` | Create CNY from yuan |
| `ofCents(long)` | Create CNY from cents |
| `ofMinorUnits(long, Currency)` | Create from minor units for any currency |
| `zero()` / `zero(Currency)` | Create zero money |
| `add(Money)` / `subtract(Money)` | Add / subtract (same currency) |
| `multiply(BigDecimal/long/double)` | Multiply with auto-rounding to currency scale |
| `divide(BigDecimal/long/double)` | Divide with auto-rounding to currency scale |
| `negate()` / `abs()` / `round()` | Negate / absolute value / round to currency scale |
| `percent(int/BigDecimal)` | Calculate percentage (e.g. 13 → 13%) |
| `addPercent(int/BigDecimal)` | Add percentage (markup) |
| `subtractPercent(int/BigDecimal)` | Subtract percentage (discount) |
| `max(Money, Money)` / `min(Money, Money)` | Static max / min of two values |
| `clamp(Money min, Money max)` | Clamp to range |
| `compareTo(Money)` | Compare (same currency) |
| `isGreaterThan/isLessThan/isGreaterOrEqual/isLessOrEqual` | Comparison predicates |
| `isPositive()` / `isNegative()` / `isZero()` | Sign predicates |
| `isNonNegative()` / `isNonPositive()` | Non-negative / non-positive predicates |
| `toCents()` / `toMinorUnits()` | Convert to minor units (long) |
| `convertTo(Currency, BigDecimal)` | Convert to another currency |
| `format()` / `formatNumber()` | Format with/without currency symbol |
| `toChineseUpperCase()` | Convert to Chinese financial uppercase |

### MoneyRange

| Method | Description |
|--------|-------------|
| `of(Money, Money)` / `singleton(Money)` | Create range / single-point range |
| `currency()` | Get range currency |
| `contains(Money)` / `contains(MoneyRange)` | Check containment |
| `clamp(Money)` | Clamp value to range |
| `overlaps(MoneyRange)` | Check if ranges overlap |
| `intersection(MoneyRange)` | Get intersection (null if none) |
| `span(MoneyRange)` | Get bounding range of union |
| `gap(MoneyRange)` | Get gap between ranges (null if overlapping) |
| `isSingleton()` | Check if min equals max |
| `width()` / `midpoint()` | Get width / midpoint |

### MoneyRounding

| Method | Description |
|--------|-------------|
| `swedish(Money)` | Swedish rounding (nearest 0.05) |
| `bankers(Money)` | Banker's rounding (HALF_EVEN) |
| `standard(Money)` | Standard rounding (HALF_UP) |
| `ceil(Money)` / `floor(Money)` | Ceiling / floor to currency scale |
| `roundToStep(Money, BigDecimal)` | Round to nearest step |
| `ceilToStep(Money, BigDecimal)` | Ceiling to nearest step |
| `floorToStep(Money, BigDecimal)` | Floor to nearest step |
| `round(Money, RoundingMode)` | Round with specific mode |
| `round(Money, int, RoundingMode)` | Round with specific scale and mode |

### AllocationUtil

| Method | Description |
|--------|-------------|
| `allocate(Money, int...)` | Allocate by integer ratios |
| `allocateByPercent(Money, int...)` | Allocate by percentages (must sum to 100) |
| `allocateByWeights(Money, BigDecimal...)` | Allocate by decimal weights |
| `split(Money, int)` | Split evenly into N parts |
| `splitWithMinimum(Money, int, Money)` | Split with guaranteed minimum per part |
| `splitRoundRobin(Money, int)` | Round-robin cent distribution |
| `verify(Money, List<Money>)` | Verify allocation sums to total |

### MoneyCalcUtil

| Method | Description |
|--------|-------------|
| `sum(Collection<Money>)` | Sum of collection |
| `sum(Collection<Money>, Currency)` | Sum with specified fallback currency |
| `average(Collection<Money>)` | Average of collection |
| `max(Collection<Money>)` / `min(Collection<Money>)` | Max / min of collection |
| `percentage(Money, Money, int)` | Calculate ratio as decimal (0-1) |
| `percentageInt(Money, Money)` | Calculate ratio as integer (0-100) |
| `applyDiscount(Money, BigDecimal)` | Apply discount rate (0-1) |
| `applyDiscountPercent(Money, int)` | Apply discount percentage (0-100) |
| `calculateTax(Money, BigDecimal)` | Calculate tax amount |
| `addTax(Money, BigDecimal)` | Add tax to amount |
| `removeTax(Money, BigDecimal)` | Remove tax (extract pre-tax from inclusive price) |
| `roundToNearest(Money, BigDecimal)` | Round to nearest value |
| `areEqual(Money, Money)` | Compare equality ignoring scale |

### MoneyFormatUtil

| Method | Description |
|--------|-------------|
| `format(Money)` | Format with currency symbol (e.g. ¥1,234.56) |
| `formatNumber(Money)` | Format number only (e.g. 1,234.56) |
| `formatNumber(BigDecimal, int)` | Format number with specific scale |
| `formatWithCode(Money)` | Format with currency code (e.g. CNY 1,234.56) |
| `formatWithNameZh(Money)` | Format with Chinese name (e.g. 人民币 1,234.56) |
| `formatAccounting(Money)` | Accounting style (negatives in parentheses) |
| `formatWithSign(Money)` | Always show + or - sign |
| `formatNoGrouping(Money)` | No thousands separator |
| `formatCompact(Money)` | Compact (万/亿 for large numbers) |
| `format(Money, Locale)` | Format with locale |
| `format(Money, String)` | Format with custom DecimalFormat pattern |
| `formatPercent(BigDecimal, int)` | Format ratio as percentage string |

### MoneyValidator

| Method | Description |
|--------|-------------|
| `validateAndParse(String)` | Parse and validate amount string (max 2 decimal places) |
| `validateAndParse(String, int)` | Parse with custom max scale |
| `validateRange(BigDecimal)` | Validate within ±100 billion |
| `validatePositive(Money)` | Assert positive |
| `validateNonNegative(Money)` | Assert non-negative |
| `validateNotZero(Money)` | Assert non-zero |
| `validateRange(Money, Money, Money)` | Assert within [min, max] |
| `isValid(String)` / `isValid(String, int)` | Check validity without throwing |
| `getMaxAmount()` / `getMinAmount()` | Get bounds (±100 billion) |

### ExchangeRate

| Method | Description |
|--------|-------------|
| `of(Currency, Currency, BigDecimal)` | Create rate |
| `of(Currency, Currency, String)` | Create rate from string |
| `of(Currency, Currency, BigDecimal, LocalDateTime)` | Create rate with timestamp |
| `identity(Currency)` | Create 1:1 identity rate |
| `convert(BigDecimal)` | Convert amount |
| `inverse()` | Get inverse rate |
| `isExpired(int)` | Check if rate is older than N hours |
| `canConvert(Currency, Currency)` | Check if rate applies to given pair |
| `format()` | Format as string (e.g. "1 CNY = 0.14 USD") |

### Currency

| Method | Description |
|--------|-------------|
| `getCode()` / `getSymbol()` | ISO code / symbol (e.g. "CNY" / "¥") |
| `getName()` / `getNameZh()` / `getNameEn()` | Name in default/Chinese/English |
| `getScale()` | Decimal places (e.g. 2 for CNY, 0 for JPY) |
| `of(String)` | Look up by code (case-insensitive) |
| `isSupported(String)` | Check if code is supported |

## Supported Currencies

CNY, USD, EUR, GBP, JPY, HKD, AUD, SGD, KRW, TWD, INR, THB, NZD, CAD, BRL, MXN, CHF, SEK, NOK, DKK, RUB, AED, ZAR

## Requirements

- Java 25+ (uses records)
- No external dependencies (only depends on opencode-base-core)

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
