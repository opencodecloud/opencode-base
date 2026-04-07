package cloud.opencode.base.lunar.benchmark;

import cloud.opencode.base.lunar.LunarDate;
import cloud.opencode.base.lunar.LunarMonth;
import cloud.opencode.base.lunar.LunarYear;
import cloud.opencode.base.lunar.OpenLunar;
import cloud.opencode.base.lunar.SolarDate;
import cloud.opencode.base.lunar.calendar.Festival;
import cloud.opencode.base.lunar.calendar.SolarTerm;
import cloud.opencode.base.lunar.calendar.SolarTermInfo;
import cloud.opencode.base.lunar.divination.AuspiciousDay;
import cloud.opencode.base.lunar.divination.YiJi;
import cloud.opencode.base.lunar.ganzhi.BaZi;
import cloud.opencode.base.lunar.ganzhi.GanZhi;
import cloud.opencode.base.lunar.zodiac.Constellation;
import cloud.opencode.base.lunar.zodiac.Zodiac;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance benchmarks for opencode-base-lunar v1.0.3.
 * 农历模块性能基准测试。
 *
 * <p>Lightweight nanoTime-loop benchmarks (non-JMH, sufficient for magnitude issues).</p>
 * <p>轻量级 nanoTime 循环基准测试（非 JMH，足以发现量级问题）。</p>
 *
 * <p>Run with: mvn test -pl opencode-base-lunar -Dtest="LunarBenchmark"</p>
 *
 * @author Leon Soo
 */
class LunarBenchmark {

    private static final int WARMUP = 50_000;
    private static final int ITERATIONS = 500_000;

    /** Measure ops/ms for a given operation */
    private static double benchmark(String name, Runnable op) {
        // Warmup
        for (int i = 0; i < WARMUP; i++) op.run();

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) op.run();
        long elapsed = System.nanoTime() - start;

        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);
        double nsPerOp = (double) elapsed / ITERATIONS;
        System.out.printf("  %-55s %10.0f ops/ms  %8.1f ns/op%n", name, opsPerMs, nsPerOp);
        return nsPerOp;
    }

    // ======================= Date Conversion | 日期转换 =======================

    @Nested
    class DateConversionBenchmarks {

        @Test
        void solarToLunar_singleDate() {
            System.out.println("\n=== Solar → Lunar Conversion (single date) ===");
            var date = LocalDate.of(2024, 6, 15);

            double ns = benchmark("OpenLunar.solarToLunar(2024-06-15)", () ->
                    OpenLunar.solarToLunar(date));

            // O(1) lookup — target < 500 ns/op
            assertThat(ns).isLessThan(2000);
        }

        @Test
        void lunarToSolar_singleDate() {
            System.out.println("\n=== Lunar → Solar Conversion (single date) ===");
            var lunar = LunarDate.of(2024, 5, 10);

            double ns = benchmark("LunarDate.toSolar(2024-闰5-10)", () ->
                    lunar.toSolar());

            assertThat(ns).isLessThan(2000);
        }

        @Test
        void roundTrip_solarLunarSolar() {
            System.out.println("\n=== Round-trip: Solar → Lunar → Solar ===");
            var date = LocalDate.of(2024, 9, 17);

            double ns = benchmark("solarToLunar → toSolar (round-trip)", () -> {
                LunarDate lunar = OpenLunar.solarToLunar(date);
                lunar.toSolar();
            });

            // round-trip: target < 1000 ns/op
            assertThat(ns).isLessThan(5000);
        }

        @Test
        void solarToLunar_rangeOfYears() {
            System.out.println("\n=== Solar → Lunar across year range (1900-2100) ===");
            // Test worst/best case: early year vs late year
            var early = LocalDate.of(1901, 3, 15);
            var mid = LocalDate.of(2000, 6, 15);
            var late = LocalDate.of(2099, 9, 15);

            double earlyNs = benchmark("solarToLunar(1901-03-15) [early year]", () ->
                    OpenLunar.solarToLunar(early));
            double midNs = benchmark("solarToLunar(2000-06-15) [mid year]", () ->
                    OpenLunar.solarToLunar(mid));
            double lateNs = benchmark("solarToLunar(2099-09-15) [late year]", () ->
                    OpenLunar.solarToLunar(late));

            double ratio = lateNs / earlyNs;
            System.out.printf("  → late/early ratio: %.2fx (ideal ≤ 3x with binary search)%n", ratio);
            // Binary search on precomputed YEAR_DAYS — bounded O(log n)
            assertThat(ratio).isLessThan(10);
        }

        @Test
        void solarDate_toLunar_vs_openLunar() {
            System.out.println("\n=== SolarDate.toLunar() vs OpenLunar.solarToLunar() ===");
            var solar = SolarDate.of(2024, 8, 20);
            var date = LocalDate.of(2024, 8, 20);

            double solarDateNs = benchmark("SolarDate.toLunar()", () -> solar.toLunar());
            double openLunarNs = benchmark("OpenLunar.solarToLunar()", () ->
                    OpenLunar.solarToLunar(date));

            double ratio = solarDateNs / openLunarNs;
            System.out.printf("  → SolarDate / OpenLunar ratio: %.2fx (should be ~1x)%n", ratio);
            assertThat(ratio).isLessThan(3);
        }
    }

    // ======================= GanZhi | 干支 =======================

    @Nested
    class GanZhiBenchmarks {

        @Test
        void yearGanZhi() {
            System.out.println("\n=== Year GanZhi Calculation ===");

            double ns = benchmark("GanZhi.ofYear(2024) [modulo arithmetic]", () ->
                    GanZhi.ofYear(2024));

            // Pure modulo — target < 50 ns/op
            assertThat(ns).isLessThan(200);
        }

        @Test
        void monthGanZhi_byNumber() {
            System.out.println("\n=== Month GanZhi (by number) ===");

            double ns = benchmark("GanZhi.ofMonth(2024, 6) [modulo]", () ->
                    GanZhi.ofMonth(2024, 6));

            assertThat(ns).isLessThan(200);
        }

        @Test
        void monthGanZhi_byDate_withSolarTerms() {
            System.out.println("\n=== Month GanZhi (by date, solar term boundaries) ===");
            var date = LocalDate.of(2024, 6, 15);

            double ns = benchmark("GanZhi.ofMonth(LocalDate) [walks 12 jie terms]", () ->
                    GanZhi.ofMonth(date));

            // Walks 12 solar terms + calculates dates — heavier than modulo
            assertThat(ns).isLessThan(10_000);
        }

        @Test
        void dayGanZhi() {
            System.out.println("\n=== Day GanZhi (Julian day algorithm) ===");
            var date = LocalDate.of(2024, 6, 15);

            double ns = benchmark("GanZhi.ofDay(2024-06-15) [epoch + modulo]", () ->
                    GanZhi.ofDay(date));

            // O(1) epoch day math — target < 100 ns/op
            assertThat(ns).isLessThan(500);
        }

        @Test
        void monthGanZhi_numberVsDate() {
            System.out.println("\n=== Month GanZhi: by number vs by date ===");
            var date = LocalDate.of(2024, 6, 15);

            double numNs = benchmark("GanZhi.ofMonth(2024, 6) [modulo]", () ->
                    GanZhi.ofMonth(2024, 6));
            double dateNs = benchmark("GanZhi.ofMonth(LocalDate) [solar terms]", () ->
                    GanZhi.ofMonth(date));

            double ratio = dateNs / numNs;
            System.out.printf("  → date-based / number-based ratio: %.1fx%n", ratio);
            // Solar term walk adds significant cost
        }
    }

    // ======================= BaZi | 八字 =======================

    @Nested
    class BaZiBenchmarks {

        @Test
        void baZi_fullCalculation() {
            System.out.println("\n=== BaZi (Four Pillars) Full Calculation ===");
            var date = LocalDate.of(2024, 2, 4);

            double ns = benchmark("BaZi.of(2024-02-04, 14) [year+month+day+hour]", () ->
                    BaZi.of(date, 14));

            // BaZi = year GanZhi + month GanZhi(solar terms) + day GanZhi + hour GanZhi
            assertThat(ns).isLessThan(20_000);
        }

        @Test
        void baZi_vs_individual_pillars() {
            System.out.println("\n=== BaZi.of() vs individual pillar calculations ===");
            var date = LocalDate.of(2024, 6, 15);

            double baZiNs = benchmark("BaZi.of(date, 10)", () ->
                    BaZi.of(date, 10));

            double individualNs = benchmark("GanZhi.ofYear+ofMonth+ofDay+ofHour", () -> {
                GanZhi year = GanZhi.ofYear(2024);
                GanZhi month = GanZhi.ofMonth(date);
                GanZhi day = GanZhi.ofDay(date);
                GanZhi.ofHour(day, 10);
            });

            double ratio = baZiNs / individualNs;
            System.out.printf("  → BaZi / individual ratio: %.2fx (should be ~1x)%n", ratio);
            assertThat(ratio).isLessThan(3);
        }
    }

    // ======================= Solar Terms | 节气 =======================

    @Nested
    class SolarTermBenchmarks {

        @Test
        void solarTerm_singleDate() {
            System.out.println("\n=== Solar Term Date Calculation ===");

            double ns = benchmark("SolarTerm.LI_CHUN.getDate(2024) [formula]", () ->
                    SolarTerm.LI_CHUN.getDate(2024));

            // O(1) formula calculation — target < 200 ns/op
            assertThat(ns).isLessThan(1000);
        }

        @Test
        void solarTerm_allTermsForYear() {
            System.out.println("\n=== All 24 Solar Terms for a Year ===");

            double ns = benchmark("SolarTerm.ofYear(2024) [24 terms]", () ->
                    SolarTerm.ofYear(2024));

            // 24 × formula + list build
            assertThat(ns).isLessThan(20_000);
        }

        @Test
        void solarTerm_lookup_byDate() {
            System.out.println("\n=== Solar Term Lookup by Date ===");
            var date = LocalDate.of(2024, 2, 4);

            double ns = benchmark("SolarTerm.of(2024-02-04) [scan 24 terms]", () ->
                    SolarTerm.of(date));

            // Scans all 24 terms and compares dates
            assertThat(ns).isLessThan(20_000);
        }
    }

    // ======================= Zodiac & Constellation | 生肖/星座 =======================

    @Nested
    class ZodiacBenchmarks {

        @Test
        void zodiac_lookup() {
            System.out.println("\n=== Zodiac Lookup ===");

            double ns = benchmark("Zodiac.of(2024) [modulo 12]", () ->
                    Zodiac.of(2024));

            // O(1) modulo — target < 20 ns/op
            assertThat(ns).isLessThan(100);
        }

        @Test
        void constellation_lookup() {
            System.out.println("\n=== Constellation Lookup ===");

            double ns = benchmark("Constellation.of(3, 15) [scan 12 ranges]", () ->
                    Constellation.of(3, 15));

            // O(12) range check
            assertThat(ns).isLessThan(500);
        }

        @Test
        void zodiac_vs_constellation() {
            System.out.println("\n=== Zodiac (O(1)) vs Constellation (O(12)) ===");

            double zodiacNs = benchmark("Zodiac.of(2024)", () -> Zodiac.of(2024));
            double constNs = benchmark("Constellation.of(8, 20)", () -> Constellation.of(8, 20));

            double ratio = constNs / zodiacNs;
            System.out.printf("  → Constellation / Zodiac ratio: %.1fx%n", ratio);
        }
    }

    // ======================= LunarYear / LunarMonth | 年/月信息 =======================

    @Nested
    class YearMonthBenchmarks {

        @Test
        void lunarYear_construction() {
            System.out.println("\n=== LunarYear Construction ===");

            double ns = benchmark("LunarYear.of(2024)", () -> LunarYear.of(2024));

            assertThat(ns).isLessThan(200);
        }

        @Test
        void lunarYear_getMonths() {
            System.out.println("\n=== LunarYear.getMonths() (enumerate all months) ===");
            var year = LunarYear.of(2024);

            double ns = benchmark("LunarYear.getMonths() [12-13 months]", () ->
                    year.getMonths());

            // Builds list of LunarMonth objects
            assertThat(ns).isLessThan(5000);
        }

        @Test
        void lunarYear_getTotalDays() {
            System.out.println("\n=== LunarYear.getTotalDays() ===");
            var year = LunarYear.of(2024);

            double ns = benchmark("LunarYear.getTotalDays()", () -> year.getTotalDays());

            // Table lookup + sum — target < 200 ns/op
            assertThat(ns).isLessThan(1000);
        }

        @Test
        void lunarMonth_navigation() {
            System.out.println("\n=== LunarMonth.next() / previous() ===");
            var month = LunarMonth.of(2024, 6, false);

            double nextNs = benchmark("LunarMonth.next()", () -> month.next());
            double prevNs = benchmark("LunarMonth.previous()", () -> month.previous());

            assertThat(nextNs).isLessThan(1000);
            assertThat(prevNs).isLessThan(1000);
        }
    }

    // ======================= LunarDate Arithmetic | 日期运算 =======================

    @Nested
    class DateArithmeticBenchmarks {

        @Test
        void lunarDate_plusDays() {
            System.out.println("\n=== LunarDate.plusDays() ===");
            var lunar = LunarDate.of(2024, 1, 1);

            double ns = benchmark("LunarDate.plusDays(30) [→ solar → add → back]", () ->
                    lunar.plusDays(30));

            // Round-trip: lunar→solar→plusDays→solarToLunar
            assertThat(ns).isLessThan(5000);
        }

        @Test
        void lunarDate_daysUntil() {
            System.out.println("\n=== LunarDate.daysUntil() ===");
            var from = LunarDate.of(2024, 1, 1);
            var to = LunarDate.of(2024, 12, 29);

            double ns = benchmark("LunarDate.daysUntil(~354 days)", () ->
                    from.daysUntil(to));

            // Two lunar→solar conversions + epoch day diff
            assertThat(ns).isLessThan(5000);
        }

        @Test
        void lunarDate_compareTo() {
            System.out.println("\n=== LunarDate.compareTo() ===");
            var a = LunarDate.of(2024, 3, 15);
            var b = LunarDate.of(2024, 8, 20);

            double ns = benchmark("LunarDate.compareTo() [via epoch day]", () ->
                    a.compareTo(b));

            // Two epoch day conversions
            assertThat(ns).isLessThan(5000);
        }
    }

    // ======================= Festival | 节日 =======================

    @Nested
    class FestivalBenchmarks {

        @Test
        void festival_solarLookup() {
            System.out.println("\n=== Solar Festival Lookup ===");
            var date = LocalDate.of(2024, 10, 1);

            double ns = benchmark("Festival.getSolarFestivals(10-01) [scan 14]", () ->
                    Festival.getSolarFestivals(date));

            assertThat(ns).isLessThan(2000);
        }

        @Test
        void festival_lunarLookup_withChuXi() {
            System.out.println("\n=== Lunar Festival Lookup (year-aware 除夕) ===");

            double ns = benchmark("Festival.getLunarFestivals(2024, 12, 29) [除夕]", () ->
                    Festival.getLunarFestivals(2024, 12, 29));

            // Needs to check ChuXi dynamic date
            assertThat(ns).isLessThan(5000);
        }
    }

    // ======================= Divination | 黄历宜忌 =======================

    @Nested
    class DivinationBenchmarks {

        @Test
        void yiJi_calculation() {
            System.out.println("\n=== YiJi (宜忌) Calculation ===");
            var lunar = LunarDate.of(2024, 6, 15);

            double ns = benchmark("YiJi.of(LunarDate) [GanZhi + switch]", () ->
                    YiJi.of(lunar));

            // O(1): day GanZhi → modulo → switch
            assertThat(ns).isLessThan(2000);
        }

        @Test
        void auspiciousDay_findNext() {
            System.out.println("\n=== AuspiciousDay.findNext() (linear scan) ===");
            var from = LocalDate.of(2024, 6, 1);

            // Reduced iterations for O(n) scan
            int warmup = 5_000;
            int iters = 50_000;

            for (int i = 0; i < warmup; i++) AuspiciousDay.findNext(YiJi.JIE_HUN, from);

            long start = System.nanoTime();
            for (int i = 0; i < iters; i++) AuspiciousDay.findNext(YiJi.JIE_HUN, from);
            long elapsed = System.nanoTime() - start;

            double nsPerOp = (double) elapsed / iters;
            double opsPerMs = (double) iters / (elapsed / 1_000_000.0);
            System.out.printf("  %-55s %10.0f ops/ms  %8.1f ns/op%n",
                    "AuspiciousDay.findNext(结婚) [scan ≤60 days]", opsPerMs, nsPerOp);

            // 60 days × (solarToLunar + YiJi) per day
            assertThat(nsPerOp).isLessThan(500_000);
        }
    }

    // ======================= Formatting | 格式化 =======================

    @Nested
    class FormattingBenchmarks {

        @Test
        void lunarDate_format() {
            System.out.println("\n=== LunarDate Formatting ===");
            var lunar = LunarDate.of(2024, 1, 1);

            double formatNs = benchmark("LunarDate.format() [甲辰年 正月初一]", () ->
                    lunar.format());
            double simpleNs = benchmark("LunarDate.formatSimple() [2024-01-01]", () ->
                    lunar.formatSimple());

            double ratio = formatNs / simpleNs;
            System.out.printf("  → format / formatSimple ratio: %.2fx%n", ratio);
        }

        @Test
        void baZi_format() {
            System.out.println("\n=== BaZi Formatting ===");
            var bazi = BaZi.of(LocalDate.of(2024, 2, 4), 14);

            double ns = benchmark("BaZi.format() [甲辰 丙寅 壬戌 丁未]", () ->
                    bazi.format());

            assertThat(ns).isLessThan(1000);
        }
    }

    // ======================= Concurrent Access | 并发访问 =======================

    @Nested
    class ConcurrencyBenchmarks {

        @Test
        void concurrent_solarToLunar_virtualThreads() throws Exception {
            System.out.println("\n=== Concurrent solarToLunar (1000 virtual threads) ===");

            long start = System.nanoTime();
            try (var exec = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
                var futures = java.util.stream.IntStream.range(0, 1000)
                        .mapToObj(i -> exec.submit(() -> {
                            var date = LocalDate.of(2000 + (i % 100), (i % 12) + 1, (i % 28) + 1);
                            return OpenLunar.solarToLunar(date);
                        }))
                        .toList();
                for (var f : futures) {
                    assertThat(f.get()).isNotNull();
                }
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("  1000 virtual threads × solarToLunar: %d ms%n", elapsedMs);
            // Stateless/immutable — no lock contention expected
            assertThat(elapsedMs).isLessThan(5000);
        }

        @Test
        void concurrent_baZi_virtualThreads() throws Exception {
            System.out.println("\n=== Concurrent BaZi (1000 virtual threads) ===");

            long start = System.nanoTime();
            try (var exec = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
                var futures = java.util.stream.IntStream.range(0, 1000)
                        .mapToObj(i -> exec.submit(() -> {
                            var date = LocalDate.of(2000 + (i % 100), (i % 12) + 1, (i % 28) + 1);
                            return BaZi.of(date, i % 24);
                        }))
                        .toList();
                for (var f : futures) {
                    assertThat(f.get()).isNotNull();
                }
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("  1000 virtual threads × BaZi.of(): %d ms%n", elapsedMs);
            assertThat(elapsedMs).isLessThan(5000);
        }
    }

    // ======================= Object Allocation | 对象分配 =======================

    @Nested
    class AllocationBenchmarks {

        @Test
        void allocation_solarToLunar_vs_cached() {
            System.out.println("\n=== Object Allocation: solarToLunar (creates LunarDate each call) ===");
            var date = LocalDate.of(2024, 6, 15);

            // Measure throughput to detect GC pressure
            long start = System.nanoTime();
            for (int i = 0; i < 1_000_000; i++) {
                OpenLunar.solarToLunar(date);
            }
            long elapsed = System.nanoTime() - start;
            double opsPerMs = 1_000_000.0 / (elapsed / 1_000_000.0);
            System.out.printf("  solarToLunar × 1M:  %.0f ops/ms  (GC pressure test)%n", opsPerMs);

            // Should sustain > 100K ops/ms (records are lightweight)
            assertThat(opsPerMs).isGreaterThan(10);
        }

        @Test
        void allocation_lunarYear_getMonths() {
            System.out.println("\n=== Object Allocation: LunarYear.getMonths() (creates List<LunarMonth>) ===");
            var year = LunarYear.of(2024);

            long start = System.nanoTime();
            for (int i = 0; i < 1_000_000; i++) {
                year.getMonths();
            }
            long elapsed = System.nanoTime() - start;
            double opsPerMs = 1_000_000.0 / (elapsed / 1_000_000.0);
            System.out.printf("  LunarYear.getMonths() × 1M:  %.0f ops/ms  (GC pressure test)%n", opsPerMs);

            assertThat(opsPerMs).isGreaterThan(1);
        }
    }

    // ======================= Summary =======================

    @Test
    void printSummaryHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  OpenCode-Base-Lunar v1.0.3 Performance Benchmark");
        System.out.println("  JDK: " + Runtime.version());
        System.out.println("  OS:  " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("  CPU: " + Runtime.getRuntime().availableProcessors() + " cores");
        System.out.println("  Warmup: " + WARMUP + " iterations, Measure: " + ITERATIONS + " iterations");
        System.out.println("=".repeat(80));
    }
}
