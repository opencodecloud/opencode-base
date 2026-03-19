package cloud.opencode.base.deepclone.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * CloneDeepTest Tests
 * CloneDeepTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("CloneDeep Annotation Tests")
class CloneDeepTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = CloneDeep.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD only")
        void shouldTargetField() {
            var target = CloneDeep.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(CloneDeep.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @CloneDeep
        private Object defaultField;

        @CloneDeep(reason = "Mutable wrapper that needs deep copy")
        private Object fieldWithReason;

        @Test
        @DisplayName("reason should default to empty string")
        void reasonShouldDefaultToEmpty() throws NoSuchFieldException {
            CloneDeep annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(CloneDeep.class);
            assertThat(annotation.reason()).isEmpty();
        }

        @Test
        @DisplayName("should support custom reason")
        void shouldSupportCustomReason() throws NoSuchFieldException {
            CloneDeep annotation = getClass().getDeclaredField("fieldWithReason")
                .getAnnotation(CloneDeep.class);
            assertThat(annotation.reason()).isEqualTo("Mutable wrapper that needs deep copy");
        }
    }
}
