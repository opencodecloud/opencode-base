package cloud.opencode.base.neural.session;

import cloud.opencode.base.neural.exception.NeuralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link InferenceSession}.
 *
 * <p>Note: Full integration tests that load OcmModel are deferred until
 * the model/ package is complete. This file tests the lifecycle and
 * null-guard behavior that does not require a real model.</p>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("InferenceSession — 推理会话")
class InferenceSessionTest {

    @Nested
    @DisplayName("load() null 参数校验")
    class LoadNullGuardTest {

        @Test
        @DisplayName("load(Path) null → NeuralException")
        void loadPathNull() {
            assertThatThrownBy(() -> InferenceSession.load((java.nio.file.Path) null))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("modelPath");
        }

        @Test
        @DisplayName("load(Path, config) null path → NeuralException")
        void loadPathConfigNullPath() {
            assertThatThrownBy(() -> InferenceSession.load(
                    (java.nio.file.Path) null, SessionConfig.defaults()))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("modelPath");
        }

        @Test
        @DisplayName("load(Path, config) null config → NeuralException")
        void loadPathConfigNullConfig() {
            assertThatThrownBy(() -> InferenceSession.load(
                    java.nio.file.Path.of("/nonexistent"), null))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("config");
        }

        @Test
        @DisplayName("load(InputStream) null → NeuralException")
        void loadStreamNull() {
            assertThatThrownBy(() -> InferenceSession.load((java.io.InputStream) null))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("stream");
        }

        @Test
        @DisplayName("load(byte[]) null → NeuralException")
        void loadBytesNull() {
            assertThatThrownBy(() -> InferenceSession.load((byte[]) null))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("modelData");
        }
    }

    // TODO: Full lifecycle and inference tests require OcmModel from model/ package.
    // Once model/ classes are available, add tests for:
    //   - load from valid model file → session is open
    //   - run() with valid inputs → outputs returned
    //   - run() on closed session → NeuralException (SESSION_CLOSED)
    //   - close() idempotent
    //   - isClosed() returns correct state
    //   - warmup() with iterations
    //   - lastProfilingResult() with profiling enabled/disabled
    //   - run() with null inputs → NeuralException
}
