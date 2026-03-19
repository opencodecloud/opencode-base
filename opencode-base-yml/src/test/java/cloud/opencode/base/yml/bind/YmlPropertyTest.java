package cloud.opencode.base.yml.bind;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlPropertyTest Tests
 * YmlPropertyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlProperty Annotation Tests")
class YmlPropertyTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = YmlProperty.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD, METHOD, and PARAMETER")
        void shouldTargetFieldMethodAndParameter() {
            var target = YmlProperty.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(YmlProperty.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @YmlProperty("server.port")
        private int simpleField;

        @YmlProperty(value = "server.host", defaultValue = "localhost", required = true)
        private String fullField;

        @Test
        @DisplayName("should read value")
        void shouldReadValue() throws NoSuchFieldException {
            YmlProperty annotation = getClass().getDeclaredField("simpleField")
                .getAnnotation(YmlProperty.class);
            assertThat(annotation.value()).isEqualTo("server.port");
        }

        @Test
        @DisplayName("defaultValue should default to empty string")
        void defaultValueShouldDefaultToEmpty() throws NoSuchFieldException {
            YmlProperty annotation = getClass().getDeclaredField("simpleField")
                .getAnnotation(YmlProperty.class);
            assertThat(annotation.defaultValue()).isEmpty();
        }

        @Test
        @DisplayName("required should default to false")
        void requiredShouldDefaultToFalse() throws NoSuchFieldException {
            YmlProperty annotation = getClass().getDeclaredField("simpleField")
                .getAnnotation(YmlProperty.class);
            assertThat(annotation.required()).isFalse();
        }

        @Test
        @DisplayName("should support all custom values")
        void shouldSupportAllCustomValues() throws NoSuchFieldException {
            YmlProperty annotation = getClass().getDeclaredField("fullField")
                .getAnnotation(YmlProperty.class);
            assertThat(annotation.value()).isEqualTo("server.host");
            assertThat(annotation.defaultValue()).isEqualTo("localhost");
            assertThat(annotation.required()).isTrue();
        }
    }
}
