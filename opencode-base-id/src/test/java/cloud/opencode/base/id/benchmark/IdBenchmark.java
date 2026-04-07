package cloud.opencode.base.id.benchmark;

import cloud.opencode.base.id.IdConverter;
import cloud.opencode.base.id.OpenId;
import cloud.opencode.base.id.prefixed.PrefixedId;
import cloud.opencode.base.id.prefixed.TypedIdGenerator;
import cloud.opencode.base.id.snowflake.SafeJsSnowflakeGenerator;
import cloud.opencode.base.id.snowflake.SnowflakeFriendlyId;
import cloud.opencode.base.id.ulid.UlidConfig;
import cloud.opencode.base.id.ulid.UlidGenerator;
import cloud.opencode.base.id.uuid.UuidParser;
import cloud.opencode.base.id.uuid.UuidV7Generator;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance benchmarks for opencode-base-id v1.0.3 new components.
 * nanoTime loop benchmarks (non-JMH, sufficient for order-of-magnitude detection).
 *
 * @author Leon Soo
 */
class IdBenchmark {

    private static final int WARMUP = 50_000;
    private static final int ITERATIONS = 500_000;

    private static double benchmark(String name, Runnable op) {
        for (int i = 0; i < WARMUP; i++) op.run();
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) op.run();
        long elapsed = System.nanoTime() - start;
        double nsPerOp = (double) elapsed / ITERATIONS;
        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);
        System.out.printf("  %-50s %10.0f ops/ms  %8.1f ns/op%n", name, opsPerMs, nsPerOp);
        return nsPerOp;
    }

    // ======================= SafeJsSnowflakeGenerator =======================

    @Test
    void safeJsSnowflake_generate() {
        System.out.println("\n=== SafeJsSnowflakeGenerator ===");

        SafeJsSnowflakeGenerator gen = SafeJsSnowflakeGenerator.create();

        double ns = benchmark("SafeJsSnowflake.generate()", () -> gen.generate());

        // Expected: ~15,625 ns/op (= 1ms / 64 sequence capacity)
        // This is throughput-limited by design (53-bit layout: 6-bit sequence = 64/ms)
        // The spin-wait between ms boundaries dominates latency under sustained load
        assertThat(ns).isLessThan(20_000); // guard: < 20μs (64/ms ceiling)
    }

    @Test
    void safeJsSnowflake_isJsSafe() {
        System.out.println("\n=== SafeJsSnowflakeGenerator.isJsSafe() ===");

        double ns = benchmark("isJsSafe(valid)", () ->
                SafeJsSnowflakeGenerator.isJsSafe(123456789L));

        // Expected: ~2-5ns (two comparisons)
        assertThat(ns).isLessThan(50);
    }

    // ======================= TypedIdGenerator =======================

    @Test
    void typedIdGenerator_generate() {
        System.out.println("\n=== TypedIdGenerator ===");

        TypedIdGenerator gen = TypedIdGenerator.of("usr", UlidGenerator.create());

        double ns = benchmark("TypedIdGenerator.generate()", () -> gen.generate());

        // Expected: dominated by ULID generation (~200-500ns)
        assertThat(ns).isLessThan(2000);
    }

    // ======================= PrefixedId =======================

    @Test
    void prefixedId_parsing() {
        System.out.println("\n=== PrefixedId ===");

        double parseNs = benchmark("PrefixedId.fromString()", () ->
                PrefixedId.fromString("usr_01ARZ3NDEKTSV4RRFFQ69G5FAV"));

        double isValidNs = benchmark("PrefixedId.isValid()", () ->
                PrefixedId.isValid("usr_01ARZ3NDEKTSV4RRFFQ69G5FAV"));

        // fromString: regex match + substring + record construction
        assertThat(parseNs).isLessThan(500);
        assertThat(isValidNs).isLessThan(300);
    }

    // ======================= SnowflakeFriendlyId =======================

    @Test
    void snowflakeFriendlyId_roundTrip() {
        System.out.println("\n=== SnowflakeFriendlyId ===");

        SnowflakeFriendlyId friendly = SnowflakeFriendlyId.ofDefault();
        long snowflakeId = OpenId.snowflakeId();

        double toNs = benchmark("toFriendly()", () ->
                friendly.toFriendly(snowflakeId));

        String friendlyStr = friendly.toFriendly(snowflakeId);

        double fromNs = benchmark("fromFriendly()", () ->
                friendly.fromFriendly(friendlyStr));

        // toFriendly: bit shift + DateTimeFormatter + concat (~200-800ns)
        // fromFriendly: regex + parse + bit shift (~300-1000ns)
        assertThat(toNs).isLessThan(2000);
        assertThat(fromNs).isLessThan(2000);
    }

    // ======================= UuidParser =======================

    @Test
    void uuidParser_parse() {
        System.out.println("\n=== UuidParser ===");

        UuidParser parser = UuidParser.create();
        UUID v7 = UuidV7Generator.create().generate();
        UUID v4 = UUID.randomUUID();

        double v7Ns = benchmark("parse(v7)", () -> parser.parse(v7));
        double v4Ns = benchmark("parse(v4)", () -> parser.parse(v4));

        double extractNs = benchmark("extractTimestamp(v7)", () ->
                parser.extractTimestamp(v7));

        // parse: bit shift + Instant construction (~20-100ns)
        assertThat(v7Ns).isLessThan(500);
        assertThat(v4Ns).isLessThan(200); // no timestamp extraction
        assertThat(extractNs).isLessThan(300);
    }

    // ======================= IdConverter Base58 =======================

    @Test
    void idConverter_base58() {
        System.out.println("\n=== IdConverter Base58 ===");

        long testId = 1705312200123049003L;

        double encodeNs = benchmark("toBase58(snowflake-like)", () ->
                IdConverter.toBase58(testId));

        String encoded = IdConverter.toBase58(testId);

        double decodeNs = benchmark("fromBase58()", () ->
                IdConverter.fromBase58(encoded));

        double validateNs = benchmark("isValidBase58()", () ->
                IdConverter.isValidBase58(encoded));

        // encode: ~11 iterations of divideUnsigned (~50-200ns)
        // decode: ~11 iterations of multiplyExact+addExact (~50-200ns)
        assertThat(encodeNs).isLessThan(500);
        assertThat(decodeNs).isLessThan(500);
        assertThat(validateNs).isLessThan(100);
    }

    // ======================= UlidGenerator with config =======================

    @Test
    void ulidGenerator_monotonicVsNonMonotonic() {
        System.out.println("\n=== UlidGenerator monotonic vs non-monotonic ===");

        UlidGenerator mono = UlidGenerator.create();
        UlidGenerator nonMono = UlidGenerator.create(UlidConfig.nonMonotonic());

        double monoNs = benchmark("UlidGenerator.generate() [monotonic]", () ->
                mono.generate());

        double nonMonoNs = benchmark("UlidGenerator.generate() [non-monotonic]", () ->
                nonMono.generate());

        // Both: SecureRandom.nextBytes(10) + encoding (~300-800ns)
        assertThat(monoNs).isLessThan(2000);
        assertThat(nonMonoNs).isLessThan(2000);
    }

    // ======================= Comparison: all ID types =======================

    @Test
    void allIdTypes_comparison() {
        System.out.println("\n=== All ID Types Comparison ===");

        SafeJsSnowflakeGenerator jsGen = SafeJsSnowflakeGenerator.create();

        benchmark("OpenId.snowflakeId()", () -> OpenId.snowflakeId());
        benchmark("SafeJsSnowflake.generate()", () -> jsGen.generate());
        benchmark("OpenId.uuid() [v4]", () -> OpenId.uuid());
        benchmark("OpenId.uuidV7()", () -> OpenId.uuidV7());
        benchmark("OpenId.ulid() [monotonic]", () -> OpenId.ulid());
        benchmark("OpenId.tsid()", () -> OpenId.tsid());
        benchmark("OpenId.ksuid()", () -> OpenId.ksuid());
        benchmark("OpenId.nanoId()", () -> OpenId.nanoId());
        benchmark("OpenId.simpleId() [atomic]", () -> OpenId.simpleId());
        benchmark("OpenId.timestampId()", () -> OpenId.timestampId());

        System.out.println("  (lower ns/op = faster)");
    }
}
