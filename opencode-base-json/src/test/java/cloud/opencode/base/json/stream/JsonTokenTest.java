package cloud.opencode.base.json.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonToken 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonToken 测试")
class JsonTokenTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("所有令牌类型存在")
        void testAllTokensExist() {
            assertThat(JsonToken.values())
                .containsExactlyInAnyOrder(
                    JsonToken.START_OBJECT,
                    JsonToken.END_OBJECT,
                    JsonToken.START_ARRAY,
                    JsonToken.END_ARRAY,
                    JsonToken.NAME,
                    JsonToken.STRING,
                    JsonToken.NUMBER,
                    JsonToken.BOOLEAN,
                    JsonToken.NULL,
                    JsonToken.END_DOCUMENT,
                    JsonToken.NOT_AVAILABLE
                );
        }

        @Test
        @DisplayName("valueOf返回正确值")
        void testValueOf() {
            assertThat(JsonToken.valueOf("START_OBJECT")).isEqualTo(JsonToken.START_OBJECT);
            assertThat(JsonToken.valueOf("END_OBJECT")).isEqualTo(JsonToken.END_OBJECT);
            assertThat(JsonToken.valueOf("START_ARRAY")).isEqualTo(JsonToken.START_ARRAY);
            assertThat(JsonToken.valueOf("END_ARRAY")).isEqualTo(JsonToken.END_ARRAY);
            assertThat(JsonToken.valueOf("NAME")).isEqualTo(JsonToken.NAME);
            assertThat(JsonToken.valueOf("STRING")).isEqualTo(JsonToken.STRING);
            assertThat(JsonToken.valueOf("NUMBER")).isEqualTo(JsonToken.NUMBER);
            assertThat(JsonToken.valueOf("BOOLEAN")).isEqualTo(JsonToken.BOOLEAN);
            assertThat(JsonToken.valueOf("NULL")).isEqualTo(JsonToken.NULL);
            assertThat(JsonToken.valueOf("END_DOCUMENT")).isEqualTo(JsonToken.END_DOCUMENT);
            assertThat(JsonToken.valueOf("NOT_AVAILABLE")).isEqualTo(JsonToken.NOT_AVAILABLE);
        }

        @Test
        @DisplayName("无效值抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> JsonToken.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("isStructureStart方法测试")
    class IsStructureStartTests {

        @Test
        @DisplayName("START_OBJECT返回true")
        void testStartObject() {
            assertThat(JsonToken.START_OBJECT.isStructureStart()).isTrue();
        }

        @Test
        @DisplayName("START_ARRAY返回true")
        void testStartArray() {
            assertThat(JsonToken.START_ARRAY.isStructureStart()).isTrue();
        }

        @Test
        @DisplayName("其他类型返回false")
        void testOtherTokens() {
            assertThat(JsonToken.END_OBJECT.isStructureStart()).isFalse();
            assertThat(JsonToken.END_ARRAY.isStructureStart()).isFalse();
            assertThat(JsonToken.NAME.isStructureStart()).isFalse();
            assertThat(JsonToken.STRING.isStructureStart()).isFalse();
            assertThat(JsonToken.NUMBER.isStructureStart()).isFalse();
            assertThat(JsonToken.BOOLEAN.isStructureStart()).isFalse();
            assertThat(JsonToken.NULL.isStructureStart()).isFalse();
            assertThat(JsonToken.END_DOCUMENT.isStructureStart()).isFalse();
            assertThat(JsonToken.NOT_AVAILABLE.isStructureStart()).isFalse();
        }
    }

    @Nested
    @DisplayName("isStructureEnd方法测试")
    class IsStructureEndTests {

        @Test
        @DisplayName("END_OBJECT返回true")
        void testEndObject() {
            assertThat(JsonToken.END_OBJECT.isStructureEnd()).isTrue();
        }

        @Test
        @DisplayName("END_ARRAY返回true")
        void testEndArray() {
            assertThat(JsonToken.END_ARRAY.isStructureEnd()).isTrue();
        }

        @Test
        @DisplayName("其他类型返回false")
        void testOtherTokens() {
            assertThat(JsonToken.START_OBJECT.isStructureEnd()).isFalse();
            assertThat(JsonToken.START_ARRAY.isStructureEnd()).isFalse();
            assertThat(JsonToken.NAME.isStructureEnd()).isFalse();
            assertThat(JsonToken.STRING.isStructureEnd()).isFalse();
            assertThat(JsonToken.NUMBER.isStructureEnd()).isFalse();
            assertThat(JsonToken.BOOLEAN.isStructureEnd()).isFalse();
            assertThat(JsonToken.NULL.isStructureEnd()).isFalse();
            assertThat(JsonToken.END_DOCUMENT.isStructureEnd()).isFalse();
            assertThat(JsonToken.NOT_AVAILABLE.isStructureEnd()).isFalse();
        }
    }

    @Nested
    @DisplayName("isScalarValue方法测试")
    class IsScalarValueTests {

        @Test
        @DisplayName("STRING返回true")
        void testString() {
            assertThat(JsonToken.STRING.isScalarValue()).isTrue();
        }

        @Test
        @DisplayName("NUMBER返回true")
        void testNumber() {
            assertThat(JsonToken.NUMBER.isScalarValue()).isTrue();
        }

        @Test
        @DisplayName("BOOLEAN返回true")
        void testBoolean() {
            assertThat(JsonToken.BOOLEAN.isScalarValue()).isTrue();
        }

        @Test
        @DisplayName("NULL返回true")
        void testNull() {
            assertThat(JsonToken.NULL.isScalarValue()).isTrue();
        }

        @Test
        @DisplayName("其他类型返回false")
        void testOtherTokens() {
            assertThat(JsonToken.START_OBJECT.isScalarValue()).isFalse();
            assertThat(JsonToken.END_OBJECT.isScalarValue()).isFalse();
            assertThat(JsonToken.START_ARRAY.isScalarValue()).isFalse();
            assertThat(JsonToken.END_ARRAY.isScalarValue()).isFalse();
            assertThat(JsonToken.NAME.isScalarValue()).isFalse();
            assertThat(JsonToken.END_DOCUMENT.isScalarValue()).isFalse();
            assertThat(JsonToken.NOT_AVAILABLE.isScalarValue()).isFalse();
        }
    }

    @Nested
    @DisplayName("isValue方法测试")
    class IsValueTests {

        @Test
        @DisplayName("标量值返回true")
        void testScalarValues() {
            assertThat(JsonToken.STRING.isValue()).isTrue();
            assertThat(JsonToken.NUMBER.isValue()).isTrue();
            assertThat(JsonToken.BOOLEAN.isValue()).isTrue();
            assertThat(JsonToken.NULL.isValue()).isTrue();
        }

        @Test
        @DisplayName("结构开始返回true")
        void testStructureStart() {
            assertThat(JsonToken.START_OBJECT.isValue()).isTrue();
            assertThat(JsonToken.START_ARRAY.isValue()).isTrue();
        }

        @Test
        @DisplayName("其他类型返回false")
        void testOtherTokens() {
            assertThat(JsonToken.END_OBJECT.isValue()).isFalse();
            assertThat(JsonToken.END_ARRAY.isValue()).isFalse();
            assertThat(JsonToken.NAME.isValue()).isFalse();
            assertThat(JsonToken.END_DOCUMENT.isValue()).isFalse();
            assertThat(JsonToken.NOT_AVAILABLE.isValue()).isFalse();
        }
    }

    @Nested
    @DisplayName("isNumeric方法测试")
    class IsNumericTests {

        @Test
        @DisplayName("NUMBER返回true")
        void testNumber() {
            assertThat(JsonToken.NUMBER.isNumeric()).isTrue();
        }

        @Test
        @DisplayName("其他类型返回false")
        void testOtherTokens() {
            assertThat(JsonToken.START_OBJECT.isNumeric()).isFalse();
            assertThat(JsonToken.END_OBJECT.isNumeric()).isFalse();
            assertThat(JsonToken.START_ARRAY.isNumeric()).isFalse();
            assertThat(JsonToken.END_ARRAY.isNumeric()).isFalse();
            assertThat(JsonToken.NAME.isNumeric()).isFalse();
            assertThat(JsonToken.STRING.isNumeric()).isFalse();
            assertThat(JsonToken.BOOLEAN.isNumeric()).isFalse();
            assertThat(JsonToken.NULL.isNumeric()).isFalse();
            assertThat(JsonToken.END_DOCUMENT.isNumeric()).isFalse();
            assertThat(JsonToken.NOT_AVAILABLE.isNumeric()).isFalse();
        }
    }

    @Nested
    @DisplayName("令牌语义测试")
    class TokenSemanticsTests {

        @Test
        @DisplayName("对象令牌配对")
        void testObjectTokenPair() {
            assertThat(JsonToken.START_OBJECT.isStructureStart()).isTrue();
            assertThat(JsonToken.END_OBJECT.isStructureEnd()).isTrue();
        }

        @Test
        @DisplayName("数组令牌配对")
        void testArrayTokenPair() {
            assertThat(JsonToken.START_ARRAY.isStructureStart()).isTrue();
            assertThat(JsonToken.END_ARRAY.isStructureEnd()).isTrue();
        }

        @Test
        @DisplayName("所有值类型")
        void testAllValueTypes() {
            int valueCount = 0;
            for (JsonToken token : JsonToken.values()) {
                if (token.isValue()) {
                    valueCount++;
                }
            }
            // STRING, NUMBER, BOOLEAN, NULL, START_OBJECT, START_ARRAY = 6
            assertThat(valueCount).isEqualTo(6);
        }

        @Test
        @DisplayName("所有标量类型")
        void testAllScalarTypes() {
            int scalarCount = 0;
            for (JsonToken token : JsonToken.values()) {
                if (token.isScalarValue()) {
                    scalarCount++;
                }
            }
            // STRING, NUMBER, BOOLEAN, NULL = 4
            assertThat(scalarCount).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Enum标准方法测试")
    class EnumStandardMethodsTests {

        @Test
        @DisplayName("name返回正确名称")
        void testName() {
            assertThat(JsonToken.START_OBJECT.name()).isEqualTo("START_OBJECT");
            assertThat(JsonToken.STRING.name()).isEqualTo("STRING");
        }

        @Test
        @DisplayName("ordinal返回正确序号")
        void testOrdinal() {
            assertThat(JsonToken.START_OBJECT.ordinal()).isEqualTo(0);
            assertThat(JsonToken.END_OBJECT.ordinal()).isEqualTo(1);
        }

        @Test
        @DisplayName("toString返回名称")
        void testToString() {
            assertThat(JsonToken.NUMBER.toString()).isEqualTo("NUMBER");
        }

        @Test
        @DisplayName("compareTo正确比较")
        void testCompareTo() {
            assertThat(JsonToken.START_OBJECT.compareTo(JsonToken.END_OBJECT)).isLessThan(0);
            assertThat(JsonToken.END_OBJECT.compareTo(JsonToken.START_OBJECT)).isGreaterThan(0);
            assertThat(JsonToken.STRING.compareTo(JsonToken.STRING)).isEqualTo(0);
        }
    }
}
