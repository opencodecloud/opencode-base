package cloud.opencode.base.id.simple;

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
 * RandomIdGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("RandomIdGenerator 测试")
class RandomIdGeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            RandomIdGenerator gen = RandomIdGenerator.create(16);

            assertThat(gen).isNotNull();
            assertThat(gen.getLength()).isEqualTo(16);
        }

        @Test
        @DisplayName("numeric方法")
        void testNumeric() {
            RandomIdGenerator gen = RandomIdGenerator.numeric(8);

            assertThat(gen).isNotNull();
            assertThat(gen.getChars()).isEqualTo(RandomIdGenerator.NUMERIC);
        }

        @Test
        @DisplayName("hex方法")
        void testHex() {
            RandomIdGenerator gen = RandomIdGenerator.hex(32);

            assertThat(gen).isNotNull();
            assertThat(gen.getChars()).isEqualTo(RandomIdGenerator.HEX_LOWER);
        }

        @Test
        @DisplayName("hexUpper方法")
        void testHexUpper() {
            RandomIdGenerator gen = RandomIdGenerator.hexUpper(32);

            assertThat(gen).isNotNull();
            assertThat(gen.getChars()).isEqualTo(RandomIdGenerator.HEX_UPPER);
        }

        @Test
        @DisplayName("custom方法")
        void testCustom() {
            RandomIdGenerator gen = RandomIdGenerator.custom(10, "ABC");

            assertThat(gen).isNotNull();
            assertThat(gen.getChars()).isEqualTo("ABC");
        }

        @Test
        @DisplayName("custom方法无效参数抛出异常")
        void testCustomInvalid() {
            assertThatThrownBy(() -> RandomIdGenerator.custom(10, null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> RandomIdGenerator.custom(10, ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成字母数字ID")
        void testGenerateAlphanumeric() {
            RandomIdGenerator gen = RandomIdGenerator.create(16);

            String id = gen.generate();

            assertThat(id).isNotNull();
            assertThat(id).hasSize(16);
            assertThat(id).matches("[0-9A-Za-z]+");
        }

        @Test
        @DisplayName("生成数字ID")
        void testGenerateNumeric() {
            RandomIdGenerator gen = RandomIdGenerator.numeric(8);

            String id = gen.generate();

            assertThat(id).isNotNull();
            assertThat(id).hasSize(8);
            assertThat(id).matches("[0-9]+");
        }

        @Test
        @DisplayName("生成十六进制ID")
        void testGenerateHex() {
            RandomIdGenerator gen = RandomIdGenerator.hex(32);

            String id = gen.generate();

            assertThat(id).isNotNull();
            assertThat(id).hasSize(32);
            assertThat(id).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("生成十六进制大写ID")
        void testGenerateHexUpper() {
            RandomIdGenerator gen = RandomIdGenerator.hexUpper(32);

            String id = gen.generate();

            assertThat(id).isNotNull();
            assertThat(id).hasSize(32);
            assertThat(id).matches("[0-9A-F]+");
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            RandomIdGenerator gen = RandomIdGenerator.create(21);
            Set<String> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("属性访问测试")
    class PropertyTests {

        @Test
        @DisplayName("获取长度")
        void testGetLength() {
            RandomIdGenerator gen = RandomIdGenerator.create(16);

            assertThat(gen.getLength()).isEqualTo(16);
        }

        @Test
        @DisplayName("获取字符集")
        void testGetChars() {
            RandomIdGenerator gen = RandomIdGenerator.create(16);

            assertThat(gen.getChars()).isEqualTo(RandomIdGenerator.ALPHANUMERIC);
        }

        @Test
        @DisplayName("获取类型")
        void testGetType() {
            RandomIdGenerator gen = RandomIdGenerator.create(16);

            assertThat(gen.getType()).isEqualTo("Random");
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("ALPHANUMERIC常量")
        void testAlphanumericConstant() {
            assertThat(RandomIdGenerator.ALPHANUMERIC)
                    .isEqualTo("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        }

        @Test
        @DisplayName("NUMERIC常量")
        void testNumericConstant() {
            assertThat(RandomIdGenerator.NUMERIC).isEqualTo("0123456789");
        }

        @Test
        @DisplayName("HEX_LOWER常量")
        void testHexLowerConstant() {
            assertThat(RandomIdGenerator.HEX_LOWER).isEqualTo("0123456789abcdef");
        }

        @Test
        @DisplayName("HEX_UPPER常量")
        void testHexUpperConstant() {
            assertThat(RandomIdGenerator.HEX_UPPER).isEqualTo("0123456789ABCDEF");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGeneration() throws InterruptedException {
            RandomIdGenerator gen = RandomIdGenerator.create(21);
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
