package cloud.opencode.base.json.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonFormat 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonFormat 注解测试")
class JsonFormatTest {

    @Nested
    @DisplayName("注解属性测试")
    class AnnotationAttributeTests {

        @Test
        @DisplayName("注解可用于字段")
        void testTargetField() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("formattedField");
            JsonFormat annotation = field.getAnnotation(JsonFormat.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("获取pattern属性")
        void testPatternAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("dateField");
            JsonFormat annotation = field.getAnnotation(JsonFormat.class);

            assertThat(annotation.pattern()).isEqualTo("yyyy-MM-dd");
        }

        @Test
        @DisplayName("获取shape属性")
        void testShapeAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("stringShapeField");
            JsonFormat annotation = field.getAnnotation(JsonFormat.class);

            assertThat(annotation.shape()).isEqualTo(JsonFormat.Shape.STRING);
        }

        @Test
        @DisplayName("获取timezone属性")
        void testTimezoneAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("timezoneField");
            JsonFormat annotation = field.getAnnotation(JsonFormat.class);

            assertThat(annotation.timezone()).isEqualTo("Asia/Shanghai");
        }

        @Test
        @DisplayName("获取locale属性")
        void testLocaleAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("localeField");
            JsonFormat annotation = field.getAnnotation(JsonFormat.class);

            assertThat(annotation.locale()).isEqualTo("zh_CN");
        }

        @Test
        @DisplayName("获取lenient属性")
        void testLenientAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("lenientField");
            JsonFormat annotation = field.getAnnotation(JsonFormat.class);

            assertThat(annotation.lenient()).isTrue();
        }

        @Test
        @DisplayName("默认值测试")
        void testDefaultValues() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("formattedField");
            JsonFormat annotation = field.getAnnotation(JsonFormat.class);

            assertThat(annotation.pattern()).isEmpty();
            assertThat(annotation.shape()).isEqualTo(JsonFormat.Shape.ANY);
            assertThat(annotation.timezone()).isEmpty();
            assertThat(annotation.locale()).isEmpty();
            assertThat(annotation.lenient()).isFalse();
        }
    }

    @Nested
    @DisplayName("Shape枚举测试")
    class ShapeEnumTests {

        @Test
        @DisplayName("所有Shape值存在")
        void testAllShapes() {
            assertThat(JsonFormat.Shape.values())
                .containsExactlyInAnyOrder(
                    JsonFormat.Shape.ANY,
                    JsonFormat.Shape.SCALAR,
                    JsonFormat.Shape.ARRAY,
                    JsonFormat.Shape.OBJECT,
                    JsonFormat.Shape.NUMBER,
                    JsonFormat.Shape.NUMBER_INT,
                    JsonFormat.Shape.NUMBER_FLOAT,
                    JsonFormat.Shape.STRING,
                    JsonFormat.Shape.BOOLEAN
                );
        }

        @Test
        @DisplayName("valueOf返回正确值")
        void testValueOf() {
            assertThat(JsonFormat.Shape.valueOf("ANY")).isEqualTo(JsonFormat.Shape.ANY);
            assertThat(JsonFormat.Shape.valueOf("SCALAR")).isEqualTo(JsonFormat.Shape.SCALAR);
            assertThat(JsonFormat.Shape.valueOf("ARRAY")).isEqualTo(JsonFormat.Shape.ARRAY);
            assertThat(JsonFormat.Shape.valueOf("OBJECT")).isEqualTo(JsonFormat.Shape.OBJECT);
            assertThat(JsonFormat.Shape.valueOf("NUMBER")).isEqualTo(JsonFormat.Shape.NUMBER);
            assertThat(JsonFormat.Shape.valueOf("NUMBER_INT")).isEqualTo(JsonFormat.Shape.NUMBER_INT);
            assertThat(JsonFormat.Shape.valueOf("NUMBER_FLOAT")).isEqualTo(JsonFormat.Shape.NUMBER_FLOAT);
            assertThat(JsonFormat.Shape.valueOf("STRING")).isEqualTo(JsonFormat.Shape.STRING);
            assertThat(JsonFormat.Shape.valueOf("BOOLEAN")).isEqualTo(JsonFormat.Shape.BOOLEAN);
        }

        @Test
        @DisplayName("无效Shape名抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> JsonFormat.Shape.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Shape名称正确")
        void testShapeNames() {
            assertThat(JsonFormat.Shape.ANY.name()).isEqualTo("ANY");
            assertThat(JsonFormat.Shape.STRING.name()).isEqualTo("STRING");
            assertThat(JsonFormat.Shape.NUMBER.name()).isEqualTo("NUMBER");
        }

        @Test
        @DisplayName("Shape序号正确")
        void testShapeOrdinals() {
            assertThat(JsonFormat.Shape.ANY.ordinal()).isEqualTo(0);
            assertThat(JsonFormat.Shape.SCALAR.ordinal()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("注解元数据测试")
    class AnnotationMetadataTests {

        @Test
        @DisplayName("Target包含FIELD、METHOD、TYPE")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonFormat.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD, ElementType.TYPE);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonFormat.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            java.lang.annotation.Documented documented =
                JsonFormat.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }
    }

    @Nested
    @DisplayName("类型级别注解测试")
    class TypeLevelAnnotationTests {

        @Test
        @DisplayName("注解可用于类")
        void testAnnotationOnClass() {
            JsonFormat annotation = FormattedClass.class.getAnnotation(JsonFormat.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.shape()).isEqualTo(JsonFormat.Shape.OBJECT);
        }
    }

    // Test helper classes
    static class TestClass {
        @JsonFormat
        String formattedField;

        @JsonFormat(pattern = "yyyy-MM-dd")
        String dateField;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        int stringShapeField;

        @JsonFormat(timezone = "Asia/Shanghai")
        String timezoneField;

        @JsonFormat(locale = "zh_CN")
        String localeField;

        @JsonFormat(lenient = true)
        String lenientField;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class FormattedClass {}
}
