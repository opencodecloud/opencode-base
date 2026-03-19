package cloud.opencode.base.deepclone.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * CloneIgnoreTest Tests
 * CloneIgnoreTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("CloneIgnore Annotation Tests")
class CloneIgnoreTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = CloneIgnore.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD only")
        void shouldTargetField() {
            var target = CloneIgnore.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(CloneIgnore.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @CloneIgnore
        private Object defaultField;

        @CloneIgnore(reason = "Cache data, not needed in clone")
        private Object fieldWithReason;

        @Test
        @DisplayName("reason should default to empty string")
        void reasonShouldDefaultToEmpty() throws NoSuchFieldException {
            CloneIgnore annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(CloneIgnore.class);
            assertThat(annotation.reason()).isEmpty();
        }

        @Test
        @DisplayName("should support custom reason")
        void shouldSupportCustomReason() throws NoSuchFieldException {
            CloneIgnore annotation = getClass().getDeclaredField("fieldWithReason")
                .getAnnotation(CloneIgnore.class);
            assertThat(annotation.reason()).isEqualTo("Cache data, not needed in clone");
        }
    }

    @Nested
    @DisplayName("Annotation Presence Tests")
    class AnnotationPresenceTests {

        @CloneIgnore
        private Object annotatedField;

        private Object normalField;

        @Test
        @DisplayName("should be present on annotated field")
        void shouldBePresentOnAnnotatedField() throws NoSuchFieldException {
            assertThat(getClass().getDeclaredField("annotatedField")
                .isAnnotationPresent(CloneIgnore.class)).isTrue();
        }

        @Test
        @DisplayName("should not be present on unannotated field")
        void shouldNotBePresentOnUnannotatedField() throws NoSuchFieldException {
            assertThat(getClass().getDeclaredField("normalField")
                .isAnnotationPresent(CloneIgnore.class)).isFalse();
        }
    }
}
