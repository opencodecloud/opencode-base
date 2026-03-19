package cloud.opencode.base.test.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * FastTestTest Tests
 * FastTestTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("FastTest Annotation Tests")
class FastTestTest {

    @Nested
    @DisplayName("Annotation Properties Tests")
    class AnnotationPropertiesTests {

        @Test
        @DisplayName("Should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = FastTest.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Should target TYPE and METHOD")
        void shouldTargetTypeAndMethod() {
            var target = FastTest.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).contains(ElementType.TYPE, ElementType.METHOD);
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have empty default value")
        void shouldHaveEmptyDefaultValue() throws NoSuchMethodException {
            var method = FastTest.class.getMethod("value");
            var defaultValue = method.getDefaultValue();
            assertThat(defaultValue).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Usage Tests")
    class UsageTests {

        @FastTest("custom description")
        void annotatedMethod() {}

        @Test
        @DisplayName("Should retrieve annotation value")
        void shouldRetrieveAnnotationValue() throws NoSuchMethodException {
            var method = UsageTests.class.getDeclaredMethod("annotatedMethod");
            var annotation = method.getAnnotation(FastTest.class);
            assertThat(annotation.value()).isEqualTo("custom description");
        }
    }
}
