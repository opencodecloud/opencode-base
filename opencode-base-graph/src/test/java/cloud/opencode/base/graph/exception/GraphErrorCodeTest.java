package cloud.opencode.base.graph.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphErrorCode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("GraphErrorCode 测试")
class GraphErrorCodeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("UNKNOWN错误码")
        void testUnknown() {
            assertThat(GraphErrorCode.UNKNOWN.getCode()).isEqualTo(0);
            assertThat(GraphErrorCode.UNKNOWN.getDescription()).isEqualTo("Unknown error");
        }

        @Test
        @DisplayName("结构错误码（1xxx）")
        void testStructureErrorCodes() {
            assertThat(GraphErrorCode.VERTEX_NOT_FOUND.getCode()).isEqualTo(1001);
            assertThat(GraphErrorCode.EDGE_NOT_FOUND.getCode()).isEqualTo(1002);
            assertThat(GraphErrorCode.DUPLICATE_VERTEX.getCode()).isEqualTo(1003);
            assertThat(GraphErrorCode.DUPLICATE_EDGE.getCode()).isEqualTo(1004);
        }

        @Test
        @DisplayName("算法错误码（2xxx）")
        void testAlgorithmErrorCodes() {
            assertThat(GraphErrorCode.CYCLE_DETECTED.getCode()).isEqualTo(2001);
            assertThat(GraphErrorCode.NO_PATH.getCode()).isEqualTo(2002);
            assertThat(GraphErrorCode.DISCONNECTED.getCode()).isEqualTo(2003);
            assertThat(GraphErrorCode.NEGATIVE_WEIGHT.getCode()).isEqualTo(2004);
            assertThat(GraphErrorCode.INVALID_DIRECTION.getCode()).isEqualTo(2005);
        }

        @Test
        @DisplayName("验证错误码（3xxx）")
        void testValidationErrorCodes() {
            assertThat(GraphErrorCode.INVALID_VERTEX.getCode()).isEqualTo(3001);
            assertThat(GraphErrorCode.INVALID_EDGE.getCode()).isEqualTo(3002);
            assertThat(GraphErrorCode.INVALID_WEIGHT.getCode()).isEqualTo(3003);
        }

        @Test
        @DisplayName("资源错误码（4xxx）")
        void testResourceErrorCodes() {
            assertThat(GraphErrorCode.LIMIT_EXCEEDED.getCode()).isEqualTo(4001);
            assertThat(GraphErrorCode.TIMEOUT.getCode()).isEqualTo(4002);
            assertThat(GraphErrorCode.OUT_OF_MEMORY.getCode()).isEqualTo(4003);
        }
    }

    @Nested
    @DisplayName("getCode方法测试")
    class GetCodeTests {

        @Test
        @DisplayName("所有错误码code不同")
        void testUniqueCode() {
            GraphErrorCode[] values = GraphErrorCode.values();
            int[] codes = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                codes[i] = values[i].getCode();
            }

            // 检查唯一性
            assertThat(codes).doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("getDescription方法测试")
    class GetDescriptionTests {

        @Test
        @DisplayName("描述不为空")
        void testDescriptionNotEmpty() {
            for (GraphErrorCode code : GraphErrorCode.values()) {
                assertThat(code.getDescription()).isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("枚举方法测试")
    class EnumMethodsTests {

        @Test
        @DisplayName("values()返回所有值")
        void testValues() {
            GraphErrorCode[] values = GraphErrorCode.values();

            assertThat(values).isNotEmpty();
            assertThat(values).contains(GraphErrorCode.UNKNOWN);
            assertThat(values).contains(GraphErrorCode.VERTEX_NOT_FOUND);
        }

        @Test
        @DisplayName("valueOf()返回正确的枚举")
        void testValueOf() {
            assertThat(GraphErrorCode.valueOf("UNKNOWN")).isEqualTo(GraphErrorCode.UNKNOWN);
            assertThat(GraphErrorCode.valueOf("NO_PATH")).isEqualTo(GraphErrorCode.NO_PATH);
        }
    }
}
