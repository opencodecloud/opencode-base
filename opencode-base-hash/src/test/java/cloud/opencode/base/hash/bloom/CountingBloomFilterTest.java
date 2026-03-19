package cloud.opencode.base.hash.bloom;

import cloud.opencode.base.hash.Funnel;
import cloud.opencode.base.hash.OpenHash;
import cloud.opencode.base.hash.exception.OpenHashException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CountingBloomFilter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("CountingBloomFilter 测试")
class CountingBloomFilterTest {

    @Nested
    @DisplayName("put方法测试")
    class PutTests {

        @Test
        @DisplayName("添加元素")
        void testPut() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            boolean added = filter.put("hello");

            assertThat(added).isTrue();
            assertThat(filter.mightContain("hello")).isTrue();
        }

        @Test
        @DisplayName("多次添加同一元素")
        void testPutMultipleTimes() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("hello");
            filter.put("hello");
            filter.put("hello");

            assertThat(filter.count("hello")).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("添加多个不同元素")
        void testPutDifferent() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("a");
            filter.put("b");
            filter.put("c");

            assertThat(filter.mightContain("a")).isTrue();
            assertThat(filter.mightContain("b")).isTrue();
            assertThat(filter.mightContain("c")).isTrue();
        }
    }

    @Nested
    @DisplayName("remove方法测试")
    class RemoveTests {

        @Test
        @DisplayName("删除元素")
        void testRemove() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("hello");
            boolean removed = filter.remove("hello");

            assertThat(removed).isTrue();
            assertThat(filter.mightContain("hello")).isFalse();
        }

        @Test
        @DisplayName("多次添加后部分删除")
        void testPartialRemove() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("hello");
            filter.put("hello");
            filter.put("hello");
            filter.remove("hello");

            assertThat(filter.mightContain("hello")).isTrue();
            assertThat(filter.count("hello")).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("完全删除元素")
        void testFullRemove() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("hello");
            filter.put("hello");
            filter.remove("hello");
            filter.remove("hello");

            assertThat(filter.mightContain("hello")).isFalse();
        }

        @Test
        @DisplayName("删除不存在的元素返回false")
        void testRemoveNonExistent() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            boolean removed = filter.remove("notexist");

            assertThat(removed).isFalse();
        }
    }

    @Nested
    @DisplayName("mightContain方法测试")
    class MightContainTests {

        @Test
        @DisplayName("已添加元素返回true")
        void testContains() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("test");

            assertThat(filter.mightContain("test")).isTrue();
        }

        @Test
        @DisplayName("未添加元素通常返回false")
        void testNotContains() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            assertThat(filter.mightContain("notexist")).isFalse();
        }

        @Test
        @DisplayName("删除后不再包含")
        void testNotContainsAfterRemove() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("hello");
            filter.remove("hello");

            assertThat(filter.mightContain("hello")).isFalse();
        }
    }

    @Nested
    @DisplayName("count方法测试")
    class CountTests {

        @Test
        @DisplayName("未添加元素计数为0")
        void testZeroCount() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            assertThat(filter.count("notexist")).isEqualTo(0);
        }

        @Test
        @DisplayName("添加后计数增加")
        void testCountIncreases() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("hello");
            long count1 = filter.count("hello");
            filter.put("hello");
            long count2 = filter.count("hello");

            assertThat(count2).isGreaterThan(count1);
        }

        @Test
        @DisplayName("删除后计数减少")
        void testCountDecreases() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("hello");
            filter.put("hello");
            filter.put("hello");
            long countBefore = filter.count("hello");
            filter.remove("hello");
            long countAfter = filter.count("hello");

            assertThat(countAfter).isLessThan(countBefore);
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清空过滤器")
        void testClear() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("a");
            filter.put("b");
            filter.put("c");
            filter.clear();

            assertThat(filter.mightContain("a")).isFalse();
            assertThat(filter.mightContain("b")).isFalse();
            assertThat(filter.mightContain("c")).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("设置预期插入数")
        void testExpectedInsertions() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(500)
                    .fpp(0.01)
                    .build();

            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("设置假阳性率")
        void testFpp() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.001)
                    .build();

            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("设置计数器位数")
        void testCounterBits() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .counterBits(8)
                    .build();

            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("设置哈希函数")
        void testHashFunction() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .hashFunction(OpenHash.xxHash64())
                    .build();

            filter.put("test");
            assertThat(filter.mightContain("test")).isTrue();
        }

        @Test
        @DisplayName("零预期插入数抛出异常")
        void testZeroInsertions() {
            assertThatThrownBy(() -> OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(0)
                    .fpp(0.01)
                    .build())
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("无效FPP抛出异常")
        void testInvalidFpp() {
            assertThatThrownBy(() -> OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0)
                    .build())
                    .isInstanceOf(OpenHashException.class);
        }
    }

    @Nested
    @DisplayName("自定义Funnel测试")
    class CustomFunnelTests {

        @Test
        @DisplayName("使用Integer Funnel")
        void testIntegerFunnel() {
            CountingBloomFilter<Integer> filter = CountingBloomFilter.builder(Funnel.INTEGER_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put(42);
            filter.put(42);

            assertThat(filter.mightContain(42)).isTrue();
            assertThat(filter.count(42)).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("使用Long Funnel")
        void testLongFunnel() {
            CountingBloomFilter<Long> filter = CountingBloomFilter.builder(Funnel.LONG_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put(123456789L);

            assertThat(filter.mightContain(123456789L)).isTrue();
        }
    }

    @Nested
    @DisplayName("计数器溢出测试")
    class OverflowTests {

        @Test
        @DisplayName("大量添加不导致崩溃")
        void testManyAdditions() {
            CountingBloomFilter<CharSequence> filter = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .counterBits(4)
                    .build();

            // 添加多次（可能超过计数器最大值）
            for (int i = 0; i < 20; i++) {
                filter.put("test");
            }

            assertThat(filter.mightContain("test")).isTrue();
        }
    }

    @Nested
    @DisplayName("与普通BloomFilter比较测试")
    class CompareWithBloomFilterTests {

        @Test
        @DisplayName("支持删除是主要区别")
        void testDeleteCapability() {
            CountingBloomFilter<CharSequence> counting = OpenHash.countingBloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            BloomFilter<CharSequence> regular = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            counting.put("test");
            regular.put("test");

            counting.remove("test");
            // regular 无法删除

            assertThat(counting.mightContain("test")).isFalse();
            assertThat(regular.mightContain("test")).isTrue();
        }
    }
}
