package cloud.opencode.base.yml.bind;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlValueTest Tests
 * YmlValueTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlValue Annotation Tests")
class YmlValueTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = YmlValue.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD and PARAMETER")
        void shouldTargetFieldAndParameter() {
            var target = YmlValue.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.PARAMETER);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(YmlValue.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @YmlValue("server.port")
        private int simpleField;

        @YmlValue(value = "server.host", defaultValue = "localhost")
        private String fieldWithDefault;

        @Test
        @DisplayName("should read value path")
        void shouldReadValuePath() throws NoSuchFieldException {
            YmlValue annotation = getClass().getDeclaredField("simpleField")
                .getAnnotation(YmlValue.class);
            assertThat(annotation.value()).isEqualTo("server.port");
        }

        @Test
        @DisplayName("defaultValue should default to empty string")
        void defaultValueShouldDefaultToEmpty() throws NoSuchFieldException {
            YmlValue annotation = getClass().getDeclaredField("simpleField")
                .getAnnotation(YmlValue.class);
            assertThat(annotation.defaultValue()).isEmpty();
        }

        @Test
        @DisplayName("should support custom default value")
        void shouldSupportCustomDefaultValue() throws NoSuchFieldException {
            YmlValue annotation = getClass().getDeclaredField("fieldWithDefault")
                .getAnnotation(YmlValue.class);
            assertThat(annotation.value()).isEqualTo("server.host");
            assertThat(annotation.defaultValue()).isEqualTo("localhost");
        }
    }
}
