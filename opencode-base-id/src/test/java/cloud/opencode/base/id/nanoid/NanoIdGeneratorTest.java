package cloud.opencode.base.id.nanoid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

/**
 * NanoIdGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("NanoIdGenerator 测试")
class NanoIdGeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            NanoIdGenerator gen = NanoIdGenerator.create();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("单例模式")
        void testSingleton() {
            NanoIdGenerator gen1 = NanoIdGenerator.create();
            NanoIdGenerator gen2 = NanoIdGenerator.create();

            assertThat(gen1).isSameAs(gen2);
        }

        @Test
        @DisplayName("使用指定长度创建")
        void testCreateWithSize() {
            NanoIdGenerator gen = NanoIdGenerator.create(10);

            assertThat(gen).isNotNull();
            assertThat(gen.getSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("builder方法")
        void testBuilder() {
            NanoIdBuilder builder = NanoIdGenerator.builder();

            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成有效NanoID")
        void testGenerate() {
            NanoIdGenerator gen = NanoIdGenerator.create();

            String id = gen.generate();

            assertThat(id).isNotNull();
            assertThat(id).hasSize(NanoIdGenerator.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("静态方法生成")
        void testRandomNanoId() {
            String id = NanoIdGenerator.randomNanoId();

            assertThat(id).isNotNull();
            assertThat(id).hasSize(NanoIdGenerator.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("使用指定长度生成")
        void testRandomNanoIdWithSize() {
            String id = NanoIdGenerator.randomNanoId(10);

            assertThat(id).isNotNull();
            assertThat(id).hasSize(10);
        }

        @Test
        @DisplayName("使用指定长度和字母表生成")
        void testRandomNanoIdWithSizeAndAlphabet() {
            String id = NanoIdGenerator.randomNanoId(8, "0123456789");

            assertThat(id).isNotNull();
            assertThat(id).hasSize(8);
            assertThat(id).matches("[0-9]+");
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            NanoIdGenerator gen = NanoIdGenerator.create();
            Set<String> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }

        @Test
        @DisplayName("无效长度抛出异常")
        void testInvalidSize() {
            assertThatThrownBy(() -> NanoIdGenerator.randomNanoId(0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> NanoIdGenerator.randomNanoId(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无效字母表抛出异常")
        void testInvalidAlphabet() {
            assertThatThrownBy(() -> NanoIdGenerator.randomNanoId(10, null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> NanoIdGenerator.randomNanoId(10, "a"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("属性访问测试")
    class PropertyTests {

        @Test
        @DisplayName("获取长度")
        void testGetSize() {
            NanoIdGenerator gen = NanoIdGenerator.create();

            assertThat(gen.getSize()).isEqualTo(NanoIdGenerator.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("获取字母表")
        void testGetAlphabet() {
            NanoIdGenerator gen = NanoIdGenerator.create();

            assertThat(gen.getAlphabet()).isEqualTo(NanoIdGenerator.DEFAULT_ALPHABET);
        }

        @Test
        @DisplayName("获取类型")
        void testGetType() {
            NanoIdGenerator gen = NanoIdGenerator.create();

            assertThat(gen.getType()).isEqualTo("NanoID");
        }

        @Test
        @DisplayName("默认常量")
        void testConstants() {
            assertThat(NanoIdGenerator.DEFAULT_SIZE).isEqualTo(21);
            assertThat(NanoIdGenerator.DEFAULT_ALPHABET).isNotNull();
            assertThat(NanoIdGenerator.DEFAULT_ALPHABET).hasSize(64);
        }
    }

    @Nested
    @DisplayName("碰撞分析测试")
    class CollisionAnalysisTests {

        @Test
        @DisplayName("计算碰撞概率")
        void testCollisionProbability() {
            double probability = NanoIdGenerator.collisionProbability(
                    21, NanoIdGenerator.DEFAULT_ALPHABET, 1000000
            );

            assertThat(probability).isGreaterThanOrEqualTo(0);
            assertThat(probability).isLessThan(1);
        }

        @Test
        @DisplayName("分析碰撞风险")
        void testAnalyzeCollision() {
            var analysis = NanoIdGenerator.analyzeCollision(
                    1000000, NanoIdGenerator.DEFAULT_ALPHABET
            );

            assertThat(analysis).isNotNull();
            assertThat(analysis.probability()).isGreaterThanOrEqualTo(0);
            assertThat(analysis.recommendedSize()).isGreaterThan(0);
            assertThat(analysis.humanReadable()).isNotNull();
            assertThat(analysis.entropyBits()).isGreaterThan(0);
        }

        @Test
        @DisplayName("分析碰撞风险使用自定义概率")
        void testAnalyzeCollisionWithTargetProbability() {
            var analysis = NanoIdGenerator.analyzeCollision(
                    1000000, NanoIdGenerator.DEFAULT_ALPHABET, 1e-9
            );

            assertThat(analysis).isNotNull();
            assertThat(analysis.recommendedProbability()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("推荐长度")
        void testRecommendedSize() {
            int size = NanoIdGenerator.recommendedSize(
                    1000000, NanoIdGenerator.DEFAULT_ALPHABET, 1e-15
            );

            assertThat(size).isGreaterThan(0);
        }

        @Test
        @DisplayName("推荐长度边界条件")
        void testRecommendedSizeEdgeCases() {
            int size1 = NanoIdGenerator.recommendedSize(0, NanoIdGenerator.DEFAULT_ALPHABET, 1e-15);
            int size2 = NanoIdGenerator.recommendedSize(1000000, NanoIdGenerator.DEFAULT_ALPHABET, 0);

            assertThat(size1).isEqualTo(NanoIdGenerator.DEFAULT_SIZE);
            assertThat(size2).isEqualTo(NanoIdGenerator.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("CollisionAnalysis isAcceptable方法")
        void testIsAcceptable() {
            var analysis = NanoIdGenerator.analyzeCollision(
                    100, NanoIdGenerator.DEFAULT_ALPHABET
            );

            assertThat(analysis.isAcceptable()).isTrue();
        }

        @Test
        @DisplayName("CollisionAnalysis isLowRisk方法")
        void testIsLowRisk() {
            var analysis = NanoIdGenerator.analyzeCollision(
                    100, NanoIdGenerator.DEFAULT_ALPHABET
            );

            assertThat(analysis.isLowRisk()).isTrue();
        }

        @Test
        @DisplayName("CollisionAnalysis toString方法")
        void testAnalysisToString() {
            var analysis = NanoIdGenerator.analyzeCollision(
                    1000000, NanoIdGenerator.DEFAULT_ALPHABET
            );

            assertThat(analysis.toString()).contains("CollisionAnalysis");
            assertThat(analysis.toString()).contains("count=");
            assertThat(analysis.toString()).contains("recommendedSize=");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGeneration() throws InterruptedException {
            NanoIdGenerator gen = NanoIdGenerator.create();
            int threadCount = 10;
            int idsPerThread = 100;
            Set<String> ids = ConcurrentHashMap.newKeySet();
            CountDownLatch latch = new CountDownLatch(threadCount);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < idsPerThread; j++) {
                            ids.add(gen.generate());
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            assertThat(ids).hasSize(threadCount * idsPerThread);
        }
    }
}
