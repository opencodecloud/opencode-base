package cloud.opencode.base.neural.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NeuralException} and its subclasses.
 */
@DisplayName("NeuralException — 神经网络异常测试")
class NeuralExceptionTest {

    @Nested
    @DisplayName("NeuralException 基类")
    class BaseExceptionTest {

        @Test
        @DisplayName("继承 OpenException")
        void extendsOpenException() {
            NeuralException ex = new NeuralException("test");
            assertThat(ex).isInstanceOf(OpenException.class);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("带错误码构造")
        void withErrorCode() {
            NeuralException ex = new NeuralException("test msg", NeuralErrorCode.SHAPE_MISMATCH);
            assertThat(ex.getNeuralErrorCode()).isEqualTo(NeuralErrorCode.SHAPE_MISMATCH);
            assertThat(ex.getComponent()).isEqualTo("neural");
            assertThat(ex.getErrorCode()).isEqualTo(String.valueOf(NeuralErrorCode.SHAPE_MISMATCH.getCode()));
        }

        @Test
        @DisplayName("默认错误码为 UNKNOWN")
        void defaultErrorCode() {
            NeuralException ex = new NeuralException("test msg");
            assertThat(ex.getNeuralErrorCode()).isEqualTo(NeuralErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("带 cause 构造")
        void withCause() {
            RuntimeException cause = new RuntimeException("root");
            NeuralException ex = new NeuralException("test", cause, NeuralErrorCode.BLAS_ERROR);
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getNeuralErrorCode()).isEqualTo(NeuralErrorCode.BLAS_ERROR);
        }

        @Test
        @DisplayName("带 cause 和默认错误码构造")
        void withCauseDefaultCode() {
            RuntimeException cause = new RuntimeException("root");
            NeuralException ex = new NeuralException("test", cause);
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getNeuralErrorCode()).isEqualTo(NeuralErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("getMessage 包含组件和错误码")
        void formattedMessage() {
            NeuralException ex = new NeuralException("something failed", NeuralErrorCode.SHAPE_MISMATCH);
            String msg = ex.getMessage();
            assertThat(msg).contains("[neural]");
            assertThat(msg).contains("(1001)");
            assertThat(msg).contains("something failed");
        }
    }

    @Nested
    @DisplayName("OpExecutionException")
    class OpExecutionExceptionTest {

        @Test
        @DisplayName("继承 NeuralException -> OpenException")
        void hierarchy() {
            OpExecutionException ex = new OpExecutionException("fail", "ReLU");
            assertThat(ex).isInstanceOf(NeuralException.class);
            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("保存 opType")
        void opType() {
            OpExecutionException ex = new OpExecutionException("fail", "Conv2D");
            assertThat(ex.getOpType()).isEqualTo("Conv2D");
            assertThat(ex.getNeuralErrorCode()).isEqualTo(NeuralErrorCode.OP_EXECUTION_FAILED);
        }

        @Test
        @DisplayName("带 cause 构造")
        void withCause() {
            Exception cause = new Exception("root");
            OpExecutionException ex = new OpExecutionException("fail", "LSTM", cause);
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("TensorException")
    class TensorExceptionTest {

        @Test
        @DisplayName("默认错误码为 INVALID_PARAMETERS")
        void defaultCode() {
            TensorException ex = new TensorException("bad tensor");
            assertThat(ex.getNeuralErrorCode()).isEqualTo(NeuralErrorCode.INVALID_PARAMETERS);
        }

        @Test
        @DisplayName("自定义错误码")
        void customCode() {
            TensorException ex = new TensorException("bad shape", NeuralErrorCode.SHAPE_MISMATCH);
            assertThat(ex.getNeuralErrorCode()).isEqualTo(NeuralErrorCode.SHAPE_MISMATCH);
        }
    }

    @Nested
    @DisplayName("ModelLoadException")
    class ModelLoadExceptionTest {

        @Test
        @DisplayName("默认错误码为 MODEL_LOAD_FAILED")
        void defaultCode() {
            ModelLoadException ex = new ModelLoadException("cannot load");
            assertThat(ex.getNeuralErrorCode()).isEqualTo(NeuralErrorCode.MODEL_LOAD_FAILED);
        }
    }

    @Nested
    @DisplayName("ModelFormatException")
    class ModelFormatExceptionTest {

        @Test
        @DisplayName("默认错误码为 MODEL_FORMAT_ERROR")
        void defaultCode() {
            ModelFormatException ex = new ModelFormatException("bad format");
            assertThat(ex.getNeuralErrorCode()).isEqualTo(NeuralErrorCode.MODEL_FORMAT_ERROR);
        }
    }

    @Nested
    @DisplayName("NeuralErrorCode 新增错误码")
    class NewErrorCodesTest {

        @Test
        @DisplayName("6xxx 损失函数错误码")
        void lossErrorCodes() {
            assertThat(NeuralErrorCode.INVALID_LOSS_INPUT.getCode()).isEqualTo(6001);
            assertThat(NeuralErrorCode.LOSS_COMPUTATION_FAILED.getCode()).isEqualTo(6002);
        }

        @Test
        @DisplayName("7xxx 初始化错误码")
        void initErrorCodes() {
            assertThat(NeuralErrorCode.INVALID_INIT_PARAMS.getCode()).isEqualTo(7001);
        }

        @Test
        @DisplayName("8xxx 归一化错误码")
        void normErrorCodes() {
            assertThat(NeuralErrorCode.NORMALIZATION_FAILED.getCode()).isEqualTo(8001);
            assertThat(NeuralErrorCode.NORMALIZER_NOT_FITTED.getCode()).isEqualTo(8002);
        }

        @Test
        @DisplayName("9xxx 指标错误码")
        void metricErrorCodes() {
            assertThat(NeuralErrorCode.INVALID_METRIC_INPUT.getCode()).isEqualTo(9001);
        }
    }
}
