package cloud.opencode.base.neural.session;

import cloud.opencode.base.neural.exception.NeuralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link SessionConfig}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("SessionConfig — 推理会话配置")
class SessionConfigTest {

    @Nested
    @DisplayName("defaults() 默认值")
    class DefaultsTest {

        @Test
        @DisplayName("threadPoolSize == availableProcessors")
        void defaultThreadPoolSize() {
            SessionConfig config = SessionConfig.defaults();
            assertThat(config.threadPoolSize())
                    .isEqualTo(Runtime.getRuntime().availableProcessors());
        }

        @Test
        @DisplayName("tensorPoolCapacity == 64")
        void defaultTensorPoolCapacity() {
            SessionConfig config = SessionConfig.defaults();
            assertThat(config.tensorPoolCapacity()).isEqualTo(64);
        }

        @Test
        @DisplayName("enableProfiling == false")
        void defaultProfilingDisabled() {
            SessionConfig config = SessionConfig.defaults();
            assertThat(config.enableProfiling()).isFalse();
        }

        @Test
        @DisplayName("maxMemoryBytes == 0 (unlimited)")
        void defaultMaxMemoryUnlimited() {
            SessionConfig config = SessionConfig.defaults();
            assertThat(config.maxMemoryBytes()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("Builder 构建器")
    class BuilderTest {

        @Test
        @DisplayName("builder 设置所有参数")
        void builderSetsAllValues() {
            SessionConfig config = SessionConfig.builder()
                    .threadPoolSize(8)
                    .tensorPoolCapacity(128)
                    .enableProfiling(true)
                    .maxMemoryBytes(1024 * 1024 * 512L)
                    .build();

            assertThat(config.threadPoolSize()).isEqualTo(8);
            assertThat(config.tensorPoolCapacity()).isEqualTo(128);
            assertThat(config.enableProfiling()).isTrue();
            assertThat(config.maxMemoryBytes()).isEqualTo(1024 * 1024 * 512L);
        }

        @Test
        @DisplayName("builder 仅设置部分参数，其余使用默认值")
        void builderPartialOverride() {
            SessionConfig config = SessionConfig.builder()
                    .threadPoolSize(4)
                    .build();

            assertThat(config.threadPoolSize()).isEqualTo(4);
            assertThat(config.tensorPoolCapacity()).isEqualTo(64);
            assertThat(config.enableProfiling()).isFalse();
            assertThat(config.maxMemoryBytes()).isEqualTo(0L);
        }

        @Test
        @DisplayName("threadPoolSize <= 0 → NeuralException")
        void invalidThreadPoolSize() {
            assertThatThrownBy(() -> SessionConfig.builder()
                    .threadPoolSize(0)
                    .build())
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("threadPoolSize");
        }

        @Test
        @DisplayName("threadPoolSize 负数 → NeuralException")
        void negativeThreadPoolSize() {
            assertThatThrownBy(() -> SessionConfig.builder()
                    .threadPoolSize(-1)
                    .build())
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("threadPoolSize");
        }

        @Test
        @DisplayName("tensorPoolCapacity <= 0 → NeuralException")
        void invalidTensorPoolCapacity() {
            assertThatThrownBy(() -> SessionConfig.builder()
                    .tensorPoolCapacity(0)
                    .build())
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("tensorPoolCapacity");
        }

        @Test
        @DisplayName("maxMemoryBytes < 0 → NeuralException")
        void negativeMaxMemory() {
            assertThatThrownBy(() -> SessionConfig.builder()
                    .maxMemoryBytes(-1)
                    .build())
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("maxMemoryBytes");
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("toString 包含所有字段")
        void toStringContainsAllFields() {
            SessionConfig config = SessionConfig.builder()
                    .threadPoolSize(4)
                    .tensorPoolCapacity(32)
                    .enableProfiling(true)
                    .maxMemoryBytes(1024L)
                    .build();

            String str = config.toString();
            assertThat(str).contains("threadPoolSize=4");
            assertThat(str).contains("tensorPoolCapacity=32");
            assertThat(str).contains("enableProfiling=true");
            assertThat(str).contains("maxMemoryBytes=1024");
        }
    }
}
