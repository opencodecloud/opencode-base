package cloud.opencode.base.string.desensitize.annotation;

import cloud.opencode.base.string.desensitize.strategy.DesensitizeType;
import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * DesensitizeTest Tests
 * DesensitizeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("Desensitize Annotation Tests")
class DesensitizeTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = Desensitize.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD and METHOD")
        void shouldTargetFieldAndMethod() {
            var target = Desensitize.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(Desensitize.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Desensitize(DesensitizeType.MOBILE_PHONE)
        private String defaultField;

        @Desensitize(value = DesensitizeType.CUSTOM, startKeep = 3, endKeep = 4, maskChar = '#', customStrategy = "myStrategy")
        private String fullField;

        @Test
        @DisplayName("should read desensitize type")
        void shouldReadDesensitizeType() throws NoSuchFieldException {
            Desensitize annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(Desensitize.class);
            assertThat(annotation.value()).isEqualTo(DesensitizeType.MOBILE_PHONE);
        }

        @Test
        @DisplayName("startKeep should default to 0")
        void startKeepShouldDefaultToZero() throws NoSuchFieldException {
            Desensitize annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(Desensitize.class);
            assertThat(annotation.startKeep()).isZero();
        }

        @Test
        @DisplayName("endKeep should default to 0")
        void endKeepShouldDefaultToZero() throws NoSuchFieldException {
            Desensitize annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(Desensitize.class);
            assertThat(annotation.endKeep()).isZero();
        }

        @Test
        @DisplayName("maskChar should default to asterisk")
        void maskCharShouldDefaultToAsterisk() throws NoSuchFieldException {
            Desensitize annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(Desensitize.class);
            assertThat(annotation.maskChar()).isEqualTo('*');
        }

        @Test
        @DisplayName("customStrategy should default to empty string")
        void customStrategyShouldDefaultToEmpty() throws NoSuchFieldException {
            Desensitize annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(Desensitize.class);
            assertThat(annotation.customStrategy()).isEmpty();
        }

        @Test
        @DisplayName("should support all custom values")
        void shouldSupportAllCustomValues() throws NoSuchFieldException {
            Desensitize annotation = getClass().getDeclaredField("fullField")
                .getAnnotation(Desensitize.class);
            assertThat(annotation.value()).isEqualTo(DesensitizeType.CUSTOM);
            assertThat(annotation.startKeep()).isEqualTo(3);
            assertThat(annotation.endKeep()).isEqualTo(4);
            assertThat(annotation.maskChar()).isEqualTo('#');
            assertThat(annotation.customStrategy()).isEqualTo("myStrategy");
        }
    }
}
