package cloud.opencode.base.hash.bloom;

import cloud.opencode.base.hash.Funnel;
import cloud.opencode.base.hash.OpenHash;
import cloud.opencode.base.hash.exception.OpenHashException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * BloomFilterBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("BloomFilterBuilder 测试")
class BloomFilterBuilderTest {

    @Nested
    @DisplayName("expectedInsertions方法测试")
    class ExpectedInsertionsTests {

        @Test
        @DisplayName("设置预期插入数")
        void testExpectedInsertions() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(1000)
                    .fpp(0.01)
                    .build();

            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("零预期插入数抛出异常")
        void testZeroInsertions() {
            assertThatThrownBy(() -> OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(0)
                    .fpp(0.01)
                    .build())
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("负预期插入数抛出异常")
        void testNegativeInsertions() {
            assertThatThrownBy(() -> OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(-1)
                    .fpp(0.01)
                    .build())
                    .isInstanceOf(OpenHashException.class);
        }
    }

    @Nested
    @DisplayName("fpp方法测试")
    class FppTests {

        @Test
        @DisplayName("设置假阳性率")
        void testFpp() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.001)
                    .build();

            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("零FPP抛出异常")
        void testZeroFpp() {
            assertThatThrownBy(() -> OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.0)
                    .build())
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("负FPP抛出异常")
        void testNegativeFpp() {
            assertThatThrownBy(() -> OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(-0.01)
                    .build())
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("FPP大于等于1抛出异常")
        void testFppTooLarge() {
            assertThatThrownBy(() -> OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(1.0)
                    .build())
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("更小的FPP需要更多位")
        void testSmallerFppNeedsMoreBits() {
            BloomFilter<CharSequence> highFpp = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.1)
                    .build();

            BloomFilter<CharSequence> lowFpp = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.001)
                    .build();

            assertThat(lowFpp.bitSize()).isGreaterThan(highFpp.bitSize());
        }
    }

    @Nested
    @DisplayName("hashFunction方法测试")
    class HashFunctionTests {

        @Test
        @DisplayName("使用自定义哈希函数")
        void testCustomHashFunction() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .hashFunction(OpenHash.xxHash64())
                    .build();

            filter.put("test");
            assertThat(filter.mightContain("test")).isTrue();
        }

        @Test
        @DisplayName("使用不同哈希函数产生不同结果")
        void testDifferentHashFunctions() {
            BloomFilter<CharSequence> filter1 = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .hashFunction(OpenHash.murmur3_128())
                    .build();

            BloomFilter<CharSequence> filter2 = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .hashFunction(OpenHash.xxHash64())
                    .build();

            filter1.put("test");
            filter2.put("test");

            // 两个过滤器都应该包含元素
            assertThat(filter1.mightContain("test")).isTrue();
            assertThat(filter2.mightContain("test")).isTrue();
        }
    }

    @Nested
    @DisplayName("threadSafe方法测试")
    class ThreadSafeTests {

        @Test
        @DisplayName("创建线程安全过滤器")
        void testThreadSafe() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .threadSafe(true)
                    .build();

            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("线程安全过滤器正常工作")
        void testThreadSafeWorks() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .threadSafe(true)
                    .build();

            filter.put("hello");
            assertThat(filter.mightContain("hello")).isTrue();
        }
    }

    @Nested
    @DisplayName("build方法测试")
    class BuildTests {

        @Test
        @DisplayName("构建过滤器")
        void testBuild() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("未设置funnel使用时抛出异常")
        void testBuildWithoutFunnelThrowsOnUse() {
            BloomFilter<String> filter = OpenHash.<String>bloomFilter().build();
            assertThatThrownBy(() -> filter.put("test"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("optimalNumOfBits静态方法测试")
    class OptimalNumOfBitsTests {

        @Test
        @DisplayName("计算最优位数")
        void testOptimalNumOfBits() {
            long bits = BloomFilterBuilder.optimalNumOfBits(1000, 0.01);

            assertThat(bits).isGreaterThan(0);
        }

        @Test
        @DisplayName("更多插入需要更多位")
        void testMoreInsertionsNeedsMoreBits() {
            long bits100 = BloomFilterBuilder.optimalNumOfBits(100, 0.01);
            long bits1000 = BloomFilterBuilder.optimalNumOfBits(1000, 0.01);

            assertThat(bits1000).isGreaterThan(bits100);
        }

        @Test
        @DisplayName("更小FPP需要更多位")
        void testSmallerFppNeedsMoreBits() {
            long bitsHighFpp = BloomFilterBuilder.optimalNumOfBits(1000, 0.1);
            long bitsLowFpp = BloomFilterBuilder.optimalNumOfBits(1000, 0.001);

            assertThat(bitsLowFpp).isGreaterThan(bitsHighFpp);
        }
    }

    @Nested
    @DisplayName("optimalNumOfHashFunctions静态方法测试")
    class OptimalNumOfHashFunctionsTests {

        @Test
        @DisplayName("计算最优哈希函数数量")
        void testOptimalNumOfHashFunctions() {
            int hashCount = BloomFilterBuilder.optimalNumOfHashFunctions(1000, 10000);

            assertThat(hashCount).isGreaterThan(0);
        }

        @Test
        @DisplayName("位数与插入数比例影响哈希函数数量")
        void testRatioAffectsHashCount() {
            int hashCountLowRatio = BloomFilterBuilder.optimalNumOfHashFunctions(1000, 2000);
            int hashCountHighRatio = BloomFilterBuilder.optimalNumOfHashFunctions(1000, 20000);

            assertThat(hashCountHighRatio).isGreaterThan(hashCountLowRatio);
        }
    }

    @Nested
    @DisplayName("自定义Funnel测试")
    class FunnelTests {

        @Test
        @DisplayName("使用自定义Funnel构建")
        void testWithCustomFunnel() {
            Funnel<Integer> intFunnel = (value, hasher) -> hasher.putInt(value);

            BloomFilter<Integer> filter = BloomFilter.builder(intFunnel)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put(42);
            assertThat(filter.mightContain(42)).isTrue();
        }

        @Test
        @DisplayName("使用内置Funnel构建")
        void testWithBuiltinFunnel() {
            BloomFilter<Long> filter = BloomFilter.builder(Funnel.LONG_FUNNEL)
                    .expectedInsertions(100)
                    .fpp(0.01)
                    .build();

            filter.put(12345L);
            assertThat(filter.mightContain(12345L)).isTrue();
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class ChainedCallsTests {

        @Test
        @DisplayName("完整链式调用")
        void testFullChain() {
            BloomFilter<CharSequence> filter = OpenHash.bloomFilter(Funnel.STRING_FUNNEL)
                    .expectedInsertions(500)
                    .fpp(0.005)
                    .hashFunction(OpenHash.murmur3_128())
                    .threadSafe(true)
                    .build();

            assertThat(filter).isNotNull();
            filter.put("chain");
            assertThat(filter.mightContain("chain")).isTrue();
        }
    }
}
