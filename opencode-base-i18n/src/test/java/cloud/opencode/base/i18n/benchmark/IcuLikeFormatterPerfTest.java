package cloud.opencode.base.i18n.benchmark;

import cloud.opencode.base.i18n.formatter.IcuLikeFormatter;
import cloud.opencode.base.i18n.plural.PluralCategory;
import cloud.opencode.base.i18n.plural.PluralRules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Throughput benchmark for i18n hot paths.
 * Not a JMH benchmark (no JMH dependency), but validates warm-path performance
 * by running many iterations and reporting ops/ms.
 *
 * Run with: mvn test -pl opencode-base-i18n -Dtest="IcuLikeFormatterPerfTest"
 */
@DisplayName("IcuLikeFormatter performance")
class IcuLikeFormatterPerfTest {

    private static final IcuLikeFormatter FMT = new IcuLikeFormatter();
    private static final int WARMUP = 50_000;
    private static final int ITERATIONS = 500_000;

    @BeforeAll
    static void warmUp() {
        // Warm up all code paths and caches
        for (int i = 0; i < WARMUP; i++) {
            FMT.format("Hello, {name}!", Locale.ENGLISH, Map.of("name", "Alice"));
            FMT.format("{0} has {1, plural, one{# item} other{# items}}.", Locale.ENGLISH, "Bob", 5);
            FMT.format("{gender, select, male{He} female{She} other{They}} liked it.", Locale.ENGLISH, Map.of("gender", "female"));
            FMT.format("Total: {amt, number, #,##0.00}", Locale.ENGLISH, Map.of("amt", 1234.56));
            PluralRules.forLocale(Locale.ENGLISH).select(1L);
        }
    }

    @Test
    @DisplayName("Simple substitution: {name}")
    void simpleSubstitution() {
        String template = "Hello, {name}!";
        Map<String, Object> params = Map.of("name", "Alice");

        long start = System.nanoTime();
        String result = null;
        for (int i = 0; i < ITERATIONS; i++) {
            result = FMT.format(template, Locale.ENGLISH, params);
        }
        long elapsed = System.nanoTime() - start;

        assertThat(result).isEqualTo("Hello, Alice!");
        reportThroughput("Simple {name}", elapsed, ITERATIONS);
    }

    @Test
    @DisplayName("Positional: {0} and {1}")
    void positionalArgs() {
        String template = "{0} and {1}";

        long start = System.nanoTime();
        String result = null;
        for (int i = 0; i < ITERATIONS; i++) {
            result = FMT.format(template, Locale.ENGLISH, "foo", "bar");
        }
        long elapsed = System.nanoTime() - start;

        assertThat(result).isEqualTo("foo and bar");
        reportThroughput("Positional {0} {1}", elapsed, ITERATIONS);
    }

    @Test
    @DisplayName("Plural: {count, plural, one{…} other{…}}")
    void pluralFormatting() {
        String template = "You have {count, plural, =0{no items} one{# item} other{# items}}.";
        Map<String, Object> params = Map.of("count", 5);

        long start = System.nanoTime();
        String result = null;
        for (int i = 0; i < ITERATIONS; i++) {
            result = FMT.format(template, Locale.ENGLISH, params);
        }
        long elapsed = System.nanoTime() - start;

        assertThat(result).isEqualTo("You have 5 items.");
        reportThroughput("Plural", elapsed, ITERATIONS);
    }

    @Test
    @DisplayName("Select: {gender, select, …}")
    void selectFormatting() {
        String template = "{gender, select, male{He} female{She} other{They}} liked it.";
        Map<String, Object> params = Map.of("gender", "female");

        long start = System.nanoTime();
        String result = null;
        for (int i = 0; i < ITERATIONS; i++) {
            result = FMT.format(template, Locale.ENGLISH, params);
        }
        long elapsed = System.nanoTime() - start;

        assertThat(result).isEqualTo("She liked it.");
        reportThroughput("Select", elapsed, ITERATIONS);
    }

    @Test
    @DisplayName("Number: {amount, number, #,##0.00}")
    void numberFormatting() {
        String template = "Total: {amount, number, #,##0.00}";
        Map<String, Object> params = Map.of("amount", 1234567.89);

        long start = System.nanoTime();
        String result = null;
        for (int i = 0; i < ITERATIONS; i++) {
            result = FMT.format(template, Locale.ENGLISH, params);
        }
        long elapsed = System.nanoTime() - start;

        assertThat(result).contains("1,234,567.89");
        reportThroughput("Number format", elapsed, ITERATIONS);
    }

    @Test
    @DisplayName("PluralRules.select() — raw rule evaluation")
    void pluralRulesSelect() {
        PluralRules rules = PluralRules.forLocale(Locale.forLanguageTag("ru"));

        long start = System.nanoTime();
        PluralCategory cat = null;
        for (int i = 0; i < ITERATIONS; i++) {
            cat = rules.select(i % 100);
        }
        long elapsed = System.nanoTime() - start;

        assertThat(cat).isNotNull();
        reportThroughput("PluralRules.select()", elapsed, ITERATIONS);
    }

    @Test
    @DisplayName("Complex: plural + named params + select")
    void complexTemplate() {
        String template = "{name} has {count, plural, =0{no messages} one{# message} other{# messages}} from {sender, select, system{the system} other{{sender}}}.";
        Map<String, Object> params = Map.of("name", "Alice", "count", 3, "sender", "Bob");

        long start = System.nanoTime();
        String result = null;
        for (int i = 0; i < ITERATIONS; i++) {
            result = FMT.format(template, Locale.ENGLISH, params);
        }
        long elapsed = System.nanoTime() - start;

        assertThat(result).isEqualTo("Alice has 3 messages from Bob.");
        reportThroughput("Complex (plural+select+named)", elapsed, ITERATIONS);
    }

    @Test
    @DisplayName("No-placeholder passthrough")
    void noPlaceholder() {
        String template = "This is a static message with no placeholders at all.";

        long start = System.nanoTime();
        String result = null;
        for (int i = 0; i < ITERATIONS; i++) {
            result = FMT.format(template, Locale.ENGLISH, Map.of());
        }
        long elapsed = System.nanoTime() - start;

        assertThat(result).isEqualTo(template);
        reportThroughput("No-placeholder passthrough", elapsed, ITERATIONS);
    }

    private void reportThroughput(String label, long elapsedNanos, int ops) {
        double opsPerMs = (double) ops / (elapsedNanos / 1_000_000.0);
        double nsPerOp = (double) elapsedNanos / ops;
        System.out.printf("  [perf] %-35s  %,.0f ops/ms  |  %,.0f ns/op%n", label, opsPerMs, nsPerOp);
    }
}
