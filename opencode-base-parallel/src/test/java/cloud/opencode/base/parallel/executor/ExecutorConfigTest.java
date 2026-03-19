package cloud.opencode.base.parallel.executor;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * ExecutorConfigTest Tests
 * ExecutorConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("ExecutorConfig 测试")
class ExecutorConfigTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("defaults创建默认配置")
        void testDefaults() {
            ExecutorConfig config = ExecutorConfig.defaults();

            assertThat(config.getNamePrefix()).isEqualTo("virtual-");
            assertThat(config.getMaxConcurrency()).isEqualTo(Integer.MAX_VALUE);
            assertThat(config.getTaskTimeout()).isNull();
            assertThat(config.isInheritThreadLocals()).isTrue();
            assertThat(config.getUncaughtExceptionHandler()).isNull();
        }

        @Test
        @DisplayName("builder创建构建器")
        void testBuilder() {
            ExecutorConfig.Builder builder = ExecutorConfig.builder();

            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("设置名称前缀")
        void testNamePrefix() {
            ExecutorConfig config = ExecutorConfig.builder()
                    .namePrefix("worker-")
                    .build();

            assertThat(config.getNamePrefix()).isEqualTo("worker-");
        }

        @Test
        @DisplayName("设置最大并发数")
        void testMaxConcurrency() {
            ExecutorConfig config = ExecutorConfig.builder()
                    .maxConcurrency(100)
                    .build();

            assertThat(config.getMaxConcurrency()).isEqualTo(100);
        }

        @Test
        @DisplayName("最大并发数必须为正数")
        void testMaxConcurrencyMustBePositive() {
            assertThatThrownBy(() -> ExecutorConfig.builder().maxConcurrency(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");

            assertThatThrownBy(() -> ExecutorConfig.builder().maxConcurrency(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("设置任务超时")
        void testTaskTimeout() {
            Duration timeout = Duration.ofSeconds(30);
            ExecutorConfig config = ExecutorConfig.builder()
                    .taskTimeout(timeout)
                    .build();

            assertThat(config.getTaskTimeout()).isEqualTo(timeout);
        }

        @Test
        @DisplayName("设置是否继承线程本地变量")
        void testInheritThreadLocals() {
            ExecutorConfig config = ExecutorConfig.builder()
                    .inheritThreadLocals(false)
                    .build();

            assertThat(config.isInheritThreadLocals()).isFalse();
        }

        @Test
        @DisplayName("设置未捕获异常处理器")
        void testUncaughtExceptionHandler() {
            Thread.UncaughtExceptionHandler handler = (t, e) -> {};
            ExecutorConfig config = ExecutorConfig.builder()
                    .uncaughtExceptionHandler(handler)
                    .build();

            assertThat(config.getUncaughtExceptionHandler()).isEqualTo(handler);
        }

        @Test
        @DisplayName("链式调用构建")
        void testChainedBuilder() {
            ExecutorConfig config = ExecutorConfig.builder()
                    .namePrefix("test-")
                    .maxConcurrency(50)
                    .taskTimeout(Duration.ofMinutes(1))
                    .inheritThreadLocals(false)
                    .build();

            assertThat(config.getNamePrefix()).isEqualTo("test-");
            assertThat(config.getMaxConcurrency()).isEqualTo(50);
            assertThat(config.getTaskTimeout()).isEqualTo(Duration.ofMinutes(1));
            assertThat(config.isInheritThreadLocals()).isFalse();
        }
    }
}
