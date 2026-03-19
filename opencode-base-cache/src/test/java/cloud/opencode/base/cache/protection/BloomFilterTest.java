package cloud.opencode.base.cache.protection;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * BloomFilter Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class BloomFilterTest {

    @Test
    void shouldCreateWithExpectedInsertionsAndFpp() {
        BloomFilter<String> filter = new BloomFilter<>(1000, 0.01);

        assertThat(filter.bitSize()).isGreaterThan(0);
        assertThat(filter.hashCount()).isGreaterThan(0);
        assertThat(filter.approximateElementCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateWithBitSizeAndHashCount() {
        BloomFilter<String> filter = new BloomFilter<>(1000, 5);

        assertThat(filter.bitSize()).isEqualTo(1000);
        assertThat(filter.hashCount()).isEqualTo(5);
    }

    @Test
    void shouldThrowOnInvalidExpectedInsertions() {
        assertThatThrownBy(() -> new BloomFilter<>(0, 0.01))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        assertThatThrownBy(() -> new BloomFilter<>(-1, 0.01))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnInvalidFpp() {
        assertThatThrownBy(() -> new BloomFilter<>(1000, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 1");

        assertThatThrownBy(() -> new BloomFilter<>(1000, 1.0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new BloomFilter<>(1000, -0.1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new BloomFilter<>(1000, 1.5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAddAndContain() {
        BloomFilter<String> filter = BloomFilter.create(1000, 0.01);

        boolean changed = filter.add("test");

        assertThat(changed).isTrue();
        assertThat(filter.mightContain("test")).isTrue();
        assertThat(filter.approximateElementCount()).isEqualTo(1);
    }

    @Test
    void shouldReturnFalseForMissing() {
        BloomFilter<String> filter = BloomFilter.create(1000, 0.01);
        filter.add("exists");

        assertThat(filter.mightContain("not-exists")).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAddingDuplicate() {
        BloomFilter<String> filter = BloomFilter.create(1000, 0.01);

        boolean first = filter.add("test");
        boolean second = filter.add("test");

        assertThat(first).isTrue();
        assertThat(second).isFalse(); // Bits already set
    }

    @Test
    void shouldAddAll() {
        BloomFilter<String> filter = BloomFilter.create(1000, 0.01);
        filter.addAll(List.of("a", "b", "c", "d", "e"));

        assertThat(filter.mightContain("a")).isTrue();
        assertThat(filter.mightContain("b")).isTrue();
        assertThat(filter.mightContain("c")).isTrue();
        assertThat(filter.mightContain("d")).isTrue();
        assertThat(filter.mightContain("e")).isTrue();
        assertThat(filter.approximateElementCount()).isEqualTo(5);
    }

    @Test
    void shouldCalculateExpectedFpp() {
        BloomFilter<String> filter = BloomFilter.create(100, 0.01);

        for (int i = 0; i < 50; i++) {
            filter.add("item" + i);
        }

        double fpp = filter.expectedFpp();
        assertThat(fpp).isGreaterThan(0);
        assertThat(fpp).isLessThan(1);
    }

    @Test
    void shouldClear() {
        BloomFilter<String> filter = BloomFilter.create(1000, 0.01);
        filter.add("test");

        assertThat(filter.mightContain("test")).isTrue();

        filter.clear();

        assertThat(filter.mightContain("test")).isFalse();
        assertThat(filter.approximateElementCount()).isEqualTo(0);
    }

    @Test
    void shouldMergeFilters() {
        BloomFilter<String> filter1 = new BloomFilter<>(1000, 5);
        BloomFilter<String> filter2 = new BloomFilter<>(1000, 5);

        filter1.add("a");
        filter2.add("b");

        filter1.merge(filter2);

        assertThat(filter1.mightContain("a")).isTrue();
        assertThat(filter1.mightContain("b")).isTrue();
    }

    @Test
    void shouldThrowOnMergeIncompatibleFilters() {
        BloomFilter<String> filter1 = new BloomFilter<>(1000, 5);
        BloomFilter<String> filter2 = new BloomFilter<>(2000, 5);

        assertThatThrownBy(() -> filter1.merge(filter2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same size");
    }

    @Test
    void shouldThrowOnMergeIncompatibleHashCount() {
        BloomFilter<String> filter1 = new BloomFilter<>(1000, 5);
        BloomFilter<String> filter2 = new BloomFilter<>(1000, 7);

        assertThatThrownBy(() -> filter1.merge(filter2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hash count");
    }

    @Test
    void shouldCreateWithDefaultFpp() {
        BloomFilter<String> filter = BloomFilter.create(1000);

        filter.add("test");
        assertThat(filter.mightContain("test")).isTrue();
    }

    @Test
    void shouldCreateWithCustomFpp() {
        BloomFilter<String> filter = BloomFilter.create(1000, 0.001);

        filter.add("test");
        assertThat(filter.mightContain("test")).isTrue();
    }

    @Test
    void shouldHandleLargeNumberOfElements() {
        BloomFilter<Integer> filter = BloomFilter.create(10000, 0.01);

        for (int i = 0; i < 5000; i++) {
            filter.add(i);
        }

        // All added elements should be found
        for (int i = 0; i < 5000; i++) {
            assertThat(filter.mightContain(i)).isTrue();
        }

        // Check false positive rate is reasonable
        int falsePositives = 0;
        for (int i = 5000; i < 10000; i++) {
            if (filter.mightContain(i)) {
                falsePositives++;
            }
        }

        // False positive rate should be around 1%
        assertThat(falsePositives).isLessThan(200); // Allow some margin
    }

    @Test
    void shouldWorkWithDifferentTypes() {
        BloomFilter<Long> longFilter = BloomFilter.create(100);
        longFilter.add(123L);
        assertThat(longFilter.mightContain(123L)).isTrue();

        BloomFilter<Double> doubleFilter = BloomFilter.create(100);
        doubleFilter.add(3.14);
        assertThat(doubleFilter.mightContain(3.14)).isTrue();

        record TestRecord(String name, int value) {}
        BloomFilter<TestRecord> recordFilter = BloomFilter.create(100);
        recordFilter.add(new TestRecord("test", 1));
        assertThat(recordFilter.mightContain(new TestRecord("test", 1))).isTrue();
    }
}
