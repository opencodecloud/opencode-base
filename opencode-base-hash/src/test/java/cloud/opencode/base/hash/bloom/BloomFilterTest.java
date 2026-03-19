package cloud.opencode.base.hash.bloom;

import cloud.opencode.base.hash.Funnel;
import cloud.opencode.base.hash.OpenHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * BloomFilter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("BloomFilter 测试")
class BloomFilterTest {

    @Nested
    @DisplayName("put方法测试")
    class PutTests {

        @Test
        @DisplayName("添加元素")
        void testPut() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            boolean changed = filter.put("hello");

            assertThat(changed).isTrue();
        }

        @Test
        @DisplayName("重复添加元素返回false")
        void testPutDuplicate() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("hello");
            boolean changed = filter.put("hello");

            assertThat(changed).isFalse();
        }

        @Test
        @DisplayName("添加多个元素")
        void testPutMultiple() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
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
    @DisplayName("putAll方法测试")
    class PutAllTests {

        @Test
        @DisplayName("批量添加元素")
        void testPutAll() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.putAll(java.util.List.of("a", "b", "c"));

            assertThat(filter.mightContain("a")).isTrue();
            assertThat(filter.mightContain("b")).isTrue();
            assertThat(filter.mightContain("c")).isTrue();
        }
    }

    @Nested
    @DisplayName("test/mightContain方法测试")
    class TestTests {

        @Test
        @DisplayName("已添加元素返回true")
        void testContains() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("test");

            assertThat(filter.test("test")).isTrue();
            assertThat(filter.mightContain("test")).isTrue();
        }

        @Test
        @DisplayName("未添加元素可能返回false")
        void testNotContains() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put("hello");

            // 由于是概率性数据结构，未添加的元素通常返回false
            // 但不能保证一定返回false（假阳性）
            assertThat(filter.mightContain("world")).isFalse();
        }

        @Test
        @DisplayName("空过滤器返回false")
        void testEmptyFilter() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            assertThat(filter.mightContain("anything")).isFalse();
        }
    }

    @Nested
    @DisplayName("expectedFpp方法测试")
    class ExpectedFppTests {

        @Test
        @DisplayName("空过滤器FPP为0")
        void testEmptyFpp() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            assertThat(filter.expectedFpp()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("添加元素后FPP增加")
        void testFppIncreases() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            double fppBefore = filter.expectedFpp();
            for (int i = 0; i < 50; i++) {
                filter.put("item" + i);
            }
            double fppAfter = filter.expectedFpp();

            assertThat(fppAfter).isGreaterThan(fppBefore);
        }
    }

    @Nested
    @DisplayName("approximateElementCount方法测试")
    class ApproximateElementCountTests {

        @Test
        @DisplayName("空过滤器计数为0")
        void testEmptyCount() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            assertThat(filter.approximateElementCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("添加元素后计数增加")
        void testCountIncreases() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            for (int i = 0; i < 10; i++) {
                filter.put("item" + i);
            }

            assertThat(filter.approximateElementCount()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("bitSize方法测试")
    class BitSizeTests {

        @Test
        @DisplayName("获取位数组大小")
        void testBitSize() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            assertThat(filter.bitSize()).isGreaterThan(0);
        }

        @Test
        @DisplayName("更多预期插入需要更大位数组")
        void testBitSizeScales() {
            BloomFilter<CharSequence> small = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            BloomFilter<CharSequence> large = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(10000)
                    .fpp(0.01)
                    .build();

            assertThat(large.bitSize()).isGreaterThan(small.bitSize());
        }
    }

    @Nested
    @DisplayName("hashCount方法测试")
    class HashCountTests {

        @Test
        @DisplayName("获取哈希函数数量")
        void testHashCount() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            assertThat(filter.hashCount()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("merge方法测试")
    class MergeTests {

        @Test
        @DisplayName("合并两个过滤器")
        void testMerge() {
            BloomFilter<CharSequence> filter1 = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();
            filter1.put("a");
            filter1.put("b");

            BloomFilter<CharSequence> filter2 = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();
            filter2.put("c");
            filter2.put("d");

            filter1.merge(filter2);

            assertThat(filter1.mightContain("a")).isTrue();
            assertThat(filter1.mightContain("b")).isTrue();
            assertThat(filter1.mightContain("c")).isTrue();
            assertThat(filter1.mightContain("d")).isTrue();
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("序列化和反序列化")
        void testToAndFromBytes() {
            BloomFilter<CharSequence> original = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();
            original.put("hello");
            original.put("world");

            byte[] bytes = original.toBytes();
            BloomFilter<CharSequence> restored = BloomFilter.fromBytes(bytes, Funnel.STRING_FUNNEL);

            assertThat(restored.mightContain("hello")).isTrue();
            assertThat(restored.mightContain("world")).isTrue();
        }

        @Test
        @DisplayName("序列化字节数组非空")
        void testToBytesNotEmpty() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            byte[] bytes = filter.toBytes();

            assertThat(bytes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("自定义Funnel测试")
    class CustomFunnelTests {

        record User(String id, String name) {}

        @Test
        @DisplayName("使用自定义Funnel")
        void testCustomFunnel() {
            Funnel<User> userFunnel = (user, hasher) -> {
                hasher.putUtf8(user.id());
                hasher.putUtf8(user.name());
            };

            BloomFilter<User> filter = BloomFilter.builder(userFunnel)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            User user = new User("1", "Alice");
            filter.put(user);

            assertThat(filter.mightContain(user)).isTrue();
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("创建线程安全过滤器")
        void testThreadSafeFilter() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(1000)
                    .fpp(0.01)
                    .threadSafe(true)
                    .build();

            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("线程安全过滤器功能正常")
        void testThreadSafeFilterWorks() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(1000)
                    .fpp(0.01)
                    .threadSafe(true)
                    .build();

            filter.put("test");

            assertThat(filter.mightContain("test")).isTrue();
        }
    }

    @Nested
    @DisplayName("假阳性率测试")
    class FalsePositiveRateTests {

        @Test
        @DisplayName("实际FPP接近预期FPP")
        void testActualFppCloseToExpected() {
            double expectedFpp = 0.01;
            int insertions = 1000;

            BloomFilter<Integer> filter = BloomFilter.builder(Funnel.INTEGER_FUNNEL)
                    .expectedInsertions(insertions)
                    .fpp(expectedFpp)
                    .build();

            // 插入预期数量的元素
            for (int i = 0; i < insertions; i++) {
                filter.put(i);
            }

            // 测试假阳性率
            int falsePositives = 0;
            int testCount = 10000;
            for (int i = insertions; i < insertions + testCount; i++) {
                if (filter.mightContain(i)) {
                    falsePositives++;
                }
            }

            double actualFpp = (double) falsePositives / testCount;
            // 允许一定的误差范围
            assertThat(actualFpp).isLessThan(expectedFpp * 2);
        }
    }
}
