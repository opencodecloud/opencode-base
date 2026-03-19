package cloud.opencode.base.id.ulid;

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
 * UlidGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("UlidGenerator 测试")
class UlidGeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            UlidGenerator gen = UlidGenerator.create();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("单例模式")
        void testSingleton() {
            UlidGenerator gen1 = UlidGenerator.create();
            UlidGenerator gen2 = UlidGenerator.create();

            assertThat(gen1).isSameAs(gen2);
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成有效ULID")
        void testGenerate() {
            UlidGenerator gen = UlidGenerator.create();

            String ulid = gen.generate();

            assertThat(ulid).isNotNull();
            assertThat(ulid).hasSize(26);
            assertThat(UlidGenerator.isValid(ulid)).isTrue();
        }

        @Test
        @DisplayName("使用指定时间戳生成")
        void testGenerateWithTimestamp() {
            UlidGenerator gen = UlidGenerator.create();
            long timestamp = System.currentTimeMillis();

            String ulid = gen.generate(timestamp);

            assertThat(ulid).isNotNull();
            assertThat(ulid).hasSize(26);
        }

        @Test
        @DisplayName("生成字节数组")
        void testGenerateBytes() {
            UlidGenerator gen = UlidGenerator.create();

            byte[] bytes = gen.generateBytes();

            assertThat(bytes).isNotNull();
            assertThat(bytes).hasSize(16);
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            UlidGenerator gen = UlidGenerator.create();
            Set<String> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }

        @Test
        @DisplayName("同毫秒内单调递增")
        void testMonotonic() {
            UlidGenerator gen = UlidGenerator.create();
            long timestamp = System.currentTimeMillis();

            String ulid1 = gen.generate(timestamp);
            String ulid2 = gen.generate(timestamp);
            String ulid3 = gen.generate(timestamp);

            // ULIDs should be sorted in generation order
            assertThat(ulid1.compareTo(ulid2)).isLessThan(0);
            assertThat(ulid2.compareTo(ulid3)).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("验证有效ULID")
        void testIsValidTrue() {
            UlidGenerator gen = UlidGenerator.create();
            String ulid = gen.generate();

            assertThat(UlidGenerator.isValid(ulid)).isTrue();
        }

        @Test
        @DisplayName("验证null")
        void testIsValidNull() {
            assertThat(UlidGenerator.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("验证空字符串")
        void testIsValidEmpty() {
            assertThat(UlidGenerator.isValid("")).isFalse();
        }

        @Test
        @DisplayName("验证长度不正确")
        void testIsValidWrongLength() {
            assertThat(UlidGenerator.isValid("01ARZ3NDEK")).isFalse();
            assertThat(UlidGenerator.isValid("01ARZ3NDEKTSV4RRFFQ69G5FAVX")).isFalse();
        }

        @Test
        @DisplayName("验证无效字符")
        void testIsValidInvalidChars() {
            // U is not in Crockford Base32
            assertThat(UlidGenerator.isValid("01ARZ3NDEKTSV4RRFFQ69G5FAU")).isFalse();
        }

        @Test
        @DisplayName("验证大小写不敏感")
        void testIsValidCaseInsensitive() {
            UlidGenerator gen = UlidGenerator.create();
            String ulid = gen.generate();

            assertThat(UlidGenerator.isValid(ulid.toLowerCase())).isTrue();
            assertThat(UlidGenerator.isValid(ulid.toUpperCase())).isTrue();
        }
    }

    @Nested
    @DisplayName("解析方法测试")
    class ParseTests {

        @Test
        @DisplayName("解析有效ULID")
        void testParse() {
            UlidGenerator gen = UlidGenerator.create();
            String ulid = gen.generate();

            var parsed = UlidGenerator.parse(ulid);

            assertThat(parsed).isNotNull();
            assertThat(parsed.ulid()).isEqualTo(ulid);
        }
    }

    @Nested
    @DisplayName("比较方法测试")
    class CompareTests {

        @Test
        @DisplayName("比较两个ULID")
        void testCompare() {
            UlidGenerator gen = UlidGenerator.create();
            String ulid1 = gen.generate();
            String ulid2 = gen.generate();

            int result = UlidGenerator.compare(ulid1, ulid2);

            assertThat(result).isLessThan(0);
        }

        @Test
        @DisplayName("比较相同ULID")
        void testCompareEqual() {
            UlidGenerator gen = UlidGenerator.create();
            String ulid = gen.generate();

            int result = UlidGenerator.compare(ulid, ulid);

            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("类型方法测试")
    class TypeTests {

        @Test
        @DisplayName("获取类型")
        void testGetType() {
            UlidGenerator gen = UlidGenerator.create();

            assertThat(gen.getType()).isEqualTo("ULID");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGeneration() throws InterruptedException {
            UlidGenerator gen = UlidGenerator.create();
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
