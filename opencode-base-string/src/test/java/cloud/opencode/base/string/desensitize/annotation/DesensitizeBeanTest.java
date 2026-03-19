package cloud.opencode.base.string.desensitize.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * DesensitizeBeanTest Tests
 * DesensitizeBeanTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("DesensitizeBean Annotation Tests")
class DesensitizeBeanTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = DesensitizeBean.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target TYPE only")
        void shouldTargetType() {
            var target = DesensitizeBean.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(DesensitizeBean.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @DesensitizeBean
        static class DefaultBean {}

        @DesensitizeBean(enabled = false)
        static class DisabledBean {}

        @Test
        @DisplayName("enabled should default to true")
        void enabledShouldDefaultToTrue() {
            DesensitizeBean annotation = DefaultBean.class.getAnnotation(DesensitizeBean.class);
            assertThat(annotation.enabled()).isTrue();
        }

        @Test
        @DisplayName("should support disabled state")
        void shouldSupportDisabledState() {
            DesensitizeBean annotation = DisabledBean.class.getAnnotation(DesensitizeBean.class);
            assertThat(annotation.enabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("Annotation Presence Tests")
    class AnnotationPresenceTests {

        @DesensitizeBean
        static class AnnotatedClass {}

        static class UnannotatedClass {}

        @Test
        @DisplayName("should be present on annotated class")
        void shouldBePresentOnAnnotatedClass() {
            assertThat(AnnotatedClass.class.isAnnotationPresent(DesensitizeBean.class)).isTrue();
        }

        @Test
        @DisplayName("should not be present on unannotated class")
        void shouldNotBePresentOnUnannotatedClass() {
            assertThat(UnannotatedClass.class.isAnnotationPresent(DesensitizeBean.class)).isFalse();
        }
    }
}
