package cloud.opencode.base.money.benchmark;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;
import cloud.opencode.base.money.MoneyRange;
import cloud.opencode.base.money.calc.AllocationUtil;
import cloud.opencode.base.money.calc.MoneyCalcUtil;
import cloud.opencode.base.money.calc.MoneyRounding;
import cloud.opencode.base.money.format.ChineseUtil;
import cloud.opencode.base.money.format.MoneyFormatUtil;
import cloud.opencode.base.money.validation.MoneyValidator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Money Module Performance Benchmark
 * 金额模块性能基准测试
 *
 * <p>Lightweight nanoTime-based benchmarks covering creation, arithmetic,
 * formatting, allocation, rounding, validation, and range operations.</p>
 * <p>基于 nanoTime 的轻量基准测试，覆盖创建、算术、格式化、分摊、舍入、验证、区间操作。</p>
 *
 * <p><strong>Run:</strong></p>
 * <pre>{@code
 * mvn test -pl opencode-base-money -Dtest="MoneyBenchmark"
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.3
 */
class MoneyBenchmark {

    private static final int WARMUP = 50_000;
    private static final int ITERATIONS = 500_000;

    private static double benchmark(String name, Runnable op) {
        for (int i = 0; i < WARMUP; i++) op.run();

        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) op.run();
        long elapsed = System.nanoTime() - start;

        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);
        double nsPerOp = (double) elapsed / ITERATIONS;
        System.out.printf("  %-50s %10.0f ops/ms  %8.1f ns/op%n", name, opsPerMs, nsPerOp);
        return nsPerOp;
    }

    // ======================= Creation | 创建 =======================

    @Nested
    class CreationBenchmarks {

        @Test
        void moneyCreation() {
            System.out.println("\n=== Money Creation ===");

            double fromStr = benchmark("Money.of(String)", () -> Money.of("1234.56"));
            double fromBd = benchmark("Money.of(BigDecimal)", () -> Money.of(BigDecimal.valueOf(123456, 2)));
            double fromLong = benchmark("Money.of(long)", () -> Money.of(1234L));
            double fromCents = benchmark("Money.ofCents(long)", () -> Money.ofCents(123456));
            double fromMinor = benchmark("Money.ofMinorUnits(long, USD)", () -> Money.ofMinorUnits(12345, Currency.USD));
            double zero = benchmark("Money.zero()", () -> Money.zero());

            // String parsing is the slowest path due to BigDecimal(String) constructor
            assertThat(fromBd).isLessThan(fromStr);
            // All should be < 500 ns/op
            assertThat(fromLong).isLessThan(500);
            assertThat(fromCents).isLessThan(500);
            assertThat(fromMinor).isLessThan(500);
            assertThat(zero).isLessThan(500);
        }
    }

    // ======================= Arithmetic | 算术 =======================

    @Nested
    class ArithmeticBenchmarks {

        @Test
        void arithmeticOps() {
            System.out.println("\n=== Arithmetic Operations ===");

            Money m100 = Money.of("100.50");
            Money m200 = Money.of("200.75");
            BigDecimal rate = new BigDecimal("0.13");

            double addNs = benchmark("add(Money)", () -> m100.add(m200));
            double subNs = benchmark("subtract(Money)", () -> m200.subtract(m100));
            double mulNs = benchmark("multiply(BigDecimal)", () -> m100.multiply(rate));
            double divNs = benchmark("divide(long)", () -> m100.divide(3));
            double pctNs = benchmark("percent(int)", () -> m100.percent(13));
            double addPct = benchmark("addPercent(int)", () -> m100.addPercent(13));
            double negNs = benchmark("negate()", () -> m100.negate());
            double absNs = benchmark("abs()", () -> m100.abs());

            // All arithmetic < 300 ns/op
            assertThat(addNs).isLessThan(300);
            assertThat(subNs).isLessThan(300);
            assertThat(negNs).isLessThan(300);
            assertThat(absNs).isLessThan(300);
        }
    }

    // ======================= Comparison | 比较 =======================

    @Nested
    class ComparisonBenchmarks {

        @Test
        void comparisonOps() {
            System.out.println("\n=== Comparison Operations ===");

            Money m100 = Money.of("100.50");
            Money m200 = Money.of("200.75");
            Money m50 = Money.of("50.25");

            double cmpNs = benchmark("compareTo(Money)", () -> m100.compareTo(m200));
            double maxNs = benchmark("Money.max(a, b)", () -> Money.max(m100, m200));
            double minNs = benchmark("Money.min(a, b)", () -> Money.min(m100, m200));
            double clampNs = benchmark("clamp(min, max)", () -> m200.clamp(m50, m100));
            double isPos = benchmark("isPositive()", () -> m100.isPositive());
            double isZero = benchmark("isZero()", () -> m100.isZero());

            // Comparison should be < 100 ns/op
            assertThat(cmpNs).isLessThan(100);
            assertThat(isPos).isLessThan(100);
            assertThat(isZero).isLessThan(100);
        }
    }

    // ======================= Formatting | 格式化 =======================

    @Nested
    class FormattingBenchmarks {

        @Test
        void formattingOps() {
            System.out.println("\n=== Formatting Operations ===");

            Money m = Money.of("1234567.89");
            Money mLarge = Money.of("99999999.99");

            double fmtNs = benchmark("format()", () -> m.format());
            double fmtNum = benchmark("formatNumber()", () -> m.formatNumber());
            double fmtCode = benchmark("MoneyFormatUtil.formatWithCode", () -> MoneyFormatUtil.formatWithCode(m));
            double fmtAcct = benchmark("MoneyFormatUtil.formatAccounting", () -> MoneyFormatUtil.formatAccounting(m));
            double fmtSign = benchmark("MoneyFormatUtil.formatWithSign", () -> MoneyFormatUtil.formatWithSign(m));
            double fmtNoGrp = benchmark("MoneyFormatUtil.formatNoGrouping", () -> MoneyFormatUtil.formatNoGrouping(m));
            double fmtCmp = benchmark("MoneyFormatUtil.formatCompact", () -> MoneyFormatUtil.formatCompact(mLarge));

            System.out.println("\n  --- Chinese Conversion ---");
            double cnUpper = benchmark("ChineseUtil.toUpperCase", () -> ChineseUtil.toUpperCase(m.amount()));
            double cnSimp = benchmark("ChineseUtil.toSimplified", () -> ChineseUtil.toSimplified(m.amount()));

            // format() is the hot path — should be < 5000 ns/op (NumberFormat creation is costly)
            assertThat(fmtNs).isLessThan(10_000);
            // Chinese conversion should be < 2000 ns/op
            assertThat(cnUpper).isLessThan(5_000);
        }
    }

    // ======================= Aggregation | 聚合 =======================

    @Nested
    class AggregationBenchmarks {

        @Test
        void aggregationOps() {
            System.out.println("\n=== Aggregation Operations ===");

            List<Money> list10 = new ArrayList<>(10);
            for (int i = 1; i <= 10; i++) {
                list10.add(Money.of(BigDecimal.valueOf(i * 100L + i)));
            }

            List<Money> list100 = new ArrayList<>(100);
            for (int i = 1; i <= 100; i++) {
                list100.add(Money.of(BigDecimal.valueOf(i * 10L + i)));
            }

            double sum10 = benchmark("sum(10 items)", () -> MoneyCalcUtil.sum(list10));
            double sum100 = benchmark("sum(100 items)", () -> MoneyCalcUtil.sum(list100));
            double avg10 = benchmark("average(10 items)", () -> MoneyCalcUtil.average(list10));
            double max10 = benchmark("max(10 items)", () -> MoneyCalcUtil.max(list10));
            double min10 = benchmark("min(10 items)", () -> MoneyCalcUtil.min(list10));

            // sum(10) should be roughly 10x faster than sum(100) — O(n)
            assertThat(sum10).isLessThan(sum100);
            // sum(10) should be < 1500 ns/op
            assertThat(sum10).isLessThan(1500);
        }
    }

    // ======================= Allocation | 分摊 =======================

    @Nested
    class AllocationBenchmarks {

        @Test
        void allocationOps() {
            System.out.println("\n=== Allocation Operations ===");

            Money total = Money.of("1000.00");

            double alloc3 = benchmark("allocate(1,2,3)", () -> AllocationUtil.allocate(total, 1, 2, 3));
            double alloc10 = benchmark("allocate(1..10)", () -> AllocationUtil.allocate(total, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
            double split3 = benchmark("split(3)", () -> AllocationUtil.split(total, 3));
            double split10 = benchmark("split(10)", () -> AllocationUtil.split(total, 10));
            double robin3 = benchmark("splitRoundRobin(3)", () -> AllocationUtil.splitRoundRobin(total, 3));
            double pct = benchmark("allocateByPercent(30,30,40)", () -> AllocationUtil.allocateByPercent(total, 30, 30, 40));

            // allocate(3) should be < 2000 ns/op
            assertThat(alloc3).isLessThan(3000);
        }
    }

    // ======================= Tax & Discount | 税和折扣 =======================

    @Nested
    class TaxDiscountBenchmarks {

        @Test
        void taxDiscountOps() {
            System.out.println("\n=== Tax & Discount Operations ===");

            Money m = Money.of("500.00");
            BigDecimal taxRate = new BigDecimal("0.13");
            BigDecimal discountRate = new BigDecimal("0.2");

            double calcTax = benchmark("calculateTax(0.13)", () -> MoneyCalcUtil.calculateTax(m, taxRate));
            double addTax = benchmark("addTax(0.13)", () -> MoneyCalcUtil.addTax(m, taxRate));
            double removeTax = benchmark("removeTax(0.13)", () -> MoneyCalcUtil.removeTax(m, taxRate));
            double discount = benchmark("applyDiscount(0.2)", () -> MoneyCalcUtil.applyDiscount(m, discountRate));
            double discPct = benchmark("applyDiscountPercent(20)", () -> MoneyCalcUtil.applyDiscountPercent(m, 20));

            // All tax/discount ops < 1000 ns/op
            assertThat(calcTax).isLessThan(1000);
            assertThat(addTax).isLessThan(1500);
        }
    }

    // ======================= Rounding | 舍入 =======================

    @Nested
    class RoundingBenchmarks {

        @Test
        void roundingOps() {
            System.out.println("\n=== Rounding Operations ===");

            Money m = Money.of("10.237");
            BigDecimal step05 = new BigDecimal("0.05");
            BigDecimal step1 = BigDecimal.ONE;

            double swedish = benchmark("swedish()", () -> MoneyRounding.swedish(m));
            double bankers = benchmark("bankers()", () -> MoneyRounding.bankers(m));
            double standard = benchmark("standard()", () -> MoneyRounding.standard(m));
            double ceil = benchmark("ceil()", () -> MoneyRounding.ceil(m));
            double floor = benchmark("floor()", () -> MoneyRounding.floor(m));
            double roundStep = benchmark("roundToStep(0.05)", () -> MoneyRounding.roundToStep(m, step05));
            double ceilStep = benchmark("ceilToStep(1)", () -> MoneyRounding.ceilToStep(m, step1));
            double floorStep = benchmark("floorToStep(1)", () -> MoneyRounding.floorToStep(m, step1));

            // Simple rounding < 500 ns/op
            assertThat(bankers).isLessThan(500);
            assertThat(standard).isLessThan(500);
            // Step-based rounding < 1000 ns/op
            assertThat(swedish).isLessThan(1000);
        }
    }

    // ======================= Range | 区间 =======================

    @Nested
    class RangeBenchmarks {

        @Test
        void rangeOps() {
            System.out.println("\n=== Range Operations ===");

            MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("1000"));
            Money m100 = Money.of("100");
            Money m5000 = Money.of("5000");

            double create = benchmark("MoneyRange.of()", () -> MoneyRange.of(Money.of("10"), Money.of("1000")));
            double contains = benchmark("contains(Money)", () -> range.contains(m100));
            double clamp = benchmark("clamp(Money)", () -> range.clamp(m5000));
            double overlaps = benchmark("overlaps(Range)", () -> range.overlaps(MoneyRange.of(Money.of("500"), Money.of("2000"))));
            double width = benchmark("width()", () -> range.width());
            double midpoint = benchmark("midpoint()", () -> range.midpoint());

            // contains/clamp should be < 200 ns/op (just BigDecimal comparisons)
            assertThat(contains).isLessThan(200);
            assertThat(clamp).isLessThan(200);
        }
    }

    // ======================= Validation | 验证 =======================

    @Nested
    class ValidationBenchmarks {

        @Test
        void validationOps() {
            System.out.println("\n=== Validation Operations ===");

            double parse = benchmark("validateAndParse(valid)", () -> MoneyValidator.validateAndParse("12345.67"));
            double isValid = benchmark("isValid(valid)", () -> MoneyValidator.isValid("12345.67"));
            double isInvalid = benchmark("isValid(invalid)", () -> MoneyValidator.isValid("not_a_number"));

            Money m = Money.of("100");
            double valPos = benchmark("validatePositive()", () -> MoneyValidator.validatePositive(m));
            double valNonNeg = benchmark("validateNonNegative()", () -> MoneyValidator.validateNonNegative(m));

            // Validation with regex < 1000 ns/op
            assertThat(parse).isLessThan(2000);
        }
    }

    // ======================= Conversion | 转换 =======================

    @Nested
    class ConversionBenchmarks {

        @Test
        void conversionOps() {
            System.out.println("\n=== Conversion Operations ===");

            Money m = Money.of("12345.67");
            BigDecimal rate = new BigDecimal("0.14");

            double toCents = benchmark("toCents()", () -> m.toCents());
            double toMinor = benchmark("toMinorUnits()", () -> m.toMinorUnits());
            double convert = benchmark("convertTo(USD, 0.14)", () -> m.convertTo(Currency.USD, rate));
            double cnUpper = benchmark("toChineseUpperCase()", () -> m.toChineseUpperCase());

            // toCents should be < 300 ns/op
            assertThat(toCents).isLessThan(500);
        }
    }
}
