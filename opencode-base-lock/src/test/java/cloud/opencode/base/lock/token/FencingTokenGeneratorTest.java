package cloud.opencode.base.lock.token;

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
 * FencingTokenGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
@DisplayName("FencingTokenGenerator 测试")
class FencingTokenGeneratorTest {

    @Nested
    @DisplayName("isIdModuleAvailable 测试")
    class IsIdModuleAvailableTests {

        @Test
        @DisplayName("返回布尔值")
        void shouldReturnBoolean() {
            boolean result = FencingTokenGenerator.isIdModuleAvailable();
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("generateStringToken 测试")
    class GenerateStringTokenTests {

        @Test
        @DisplayName("生成非空字符串")
        void shouldGenerateNonEmptyString() {
            String token = FencingTokenGenerator.generateStringToken();

            assertThat(token).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("生成唯一令牌")
        void shouldGenerateUniqueTokens() {
            Set<String> tokens = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                tokens.add(FencingTokenGenerator.generateStringToken());
            }

            assertThat(tokens).hasSize(1000);
        }

        @Test
        @DisplayName("线程安全生成")
        void shouldBeThreadSafe() throws InterruptedException {
            int threadCount = 10;
            int tokensPerThread = 100;
            Set<String> tokens = ConcurrentHashMap.newKeySet();
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        try {
                            for (int j = 0; j < tokensPerThread; j++) {
                                tokens.add(FencingTokenGenerator.generateStringToken());
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                latch.await();
            }

            assertThat(tokens).hasSize(threadCount * tokensPerThread);
        }
    }

    @Nested
    @DisplayName("generateLongToken 测试")
    class GenerateLongTokenTests {

        @Test
        @DisplayName("生成正数令牌")
        void shouldGeneratePositiveToken() {
            long token = FencingTokenGenerator.generateLongToken();

            assertThat(token).isPositive();
        }

        @Test
        @DisplayName("生成唯一令牌")
        void shouldGenerateUniqueTokens() {
            Set<Long> tokens = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                tokens.add(FencingTokenGenerator.generateLongToken());
            }

            assertThat(tokens).hasSize(1000);
        }

        @Test
        @DisplayName("单调递增（fallback模式）")
        void shouldBeMonotonic() {
            long prev = FencingTokenGenerator.generateLongToken();

            for (int i = 0; i < 100; i++) {
                long current = FencingTokenGenerator.generateLongToken();
                assertThat(current).isGreaterThan(prev);
                prev = current;
            }
        }
    }

    @Nested
    @DisplayName("generatePrefixedToken 测试")
    class GeneratePrefixedTokenTests {

        @Test
        @DisplayName("生成带前缀的令牌")
        void shouldGeneratePrefixedToken() {
            String token = FencingTokenGenerator.generatePrefixedToken("lock");

            assertThat(token).startsWith("lock:");
        }

        @Test
        @DisplayName("不同调用生成不同令牌")
        void shouldGenerateDifferentTokens() {
            String token1 = FencingTokenGenerator.generatePrefixedToken("lock");
            String token2 = FencingTokenGenerator.generatePrefixedToken("lock");

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("支持空前缀")
        void shouldSupportEmptyPrefix() {
            String token = FencingTokenGenerator.generatePrefixedToken("");

            assertThat(token).startsWith(":");
        }
    }
}
