package cloud.opencode.base.neural.tensor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TensorType}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("TensorType — 张量数据类型枚举")
class TensorTypeTest {

    @Nested
    @DisplayName("枚举值验证")
    class EnumValuesTest {

        @Test
        @DisplayName("应包含5个枚举值")
        void shouldHaveFiveValues() {
            assertThat(TensorType.values()).hasSize(5);
        }

        @Test
        @DisplayName("FLOAT32 字节大小为4")
        void float32ByteSize() {
            assertThat(TensorType.FLOAT32.byteSize()).isEqualTo(4);
        }

        @Test
        @DisplayName("FLOAT64 字节大小为8")
        void float64ByteSize() {
            assertThat(TensorType.FLOAT64.byteSize()).isEqualTo(8);
        }

        @Test
        @DisplayName("INT32 字节大小为4")
        void int32ByteSize() {
            assertThat(TensorType.INT32.byteSize()).isEqualTo(4);
        }

        @Test
        @DisplayName("INT64 字节大小为8")
        void int64ByteSize() {
            assertThat(TensorType.INT64.byteSize()).isEqualTo(8);
        }

        @Test
        @DisplayName("UINT8 字节大小为1")
        void uint8ByteSize() {
            assertThat(TensorType.UINT8.byteSize()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("valueOf 验证")
    class ValueOfTest {

        @Test
        @DisplayName("valueOf 应正确解析所有枚举名称")
        void valueOfShouldResolveAllNames() {
            assertThat(TensorType.valueOf("FLOAT32")).isEqualTo(TensorType.FLOAT32);
            assertThat(TensorType.valueOf("FLOAT64")).isEqualTo(TensorType.FLOAT64);
            assertThat(TensorType.valueOf("INT32")).isEqualTo(TensorType.INT32);
            assertThat(TensorType.valueOf("INT64")).isEqualTo(TensorType.INT64);
            assertThat(TensorType.valueOf("UINT8")).isEqualTo(TensorType.UINT8);
        }
    }
}
