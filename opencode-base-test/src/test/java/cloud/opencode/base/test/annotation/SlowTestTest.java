package cloud.opencode.base.test.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * SlowTestTest Tests
 * SlowTestTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("SlowTest Annotation Tests")
class SlowTestTest {

    @Nested
    @DisplayName("Annotation Properties Tests")
    class AnnotationPropertiesTests {

        @Test
        @DisplayName("Should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = SlowTest.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Should target TYPE and METHOD")
        void shouldTargetTypeAndMethod() {
            var target = SlowTest.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).contains(ElementType.TYPE, ElementType.METHOD);
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @SlowTest
        static class AnnotatedClass {}

        @Test
        @DisplayName("Should have empty default value")
        void shouldHaveEmptyDefaultValue() throws NoSuchMethodException {
            var method = SlowTest.class.getMethod("value");
            var defaultValue = method.getDefaultValue();
            assertThat(defaultValue).isEqualTo("");
        }

        @Test
        @DisplayName("Should have default expectedMillis of 1000")
        void shouldHaveDefaultExpectedMillis() throws NoSuchMethodException {
            var method = SlowTest.class.getMethod("expectedMillis");
            var defaultValue = method.getDefaultValue();
            assertThat(defaultValue).isEqualTo(1000L);
        }
    }

    @Nested
    @DisplayName("Usage Tests")
    class UsageTests {

        @SlowTest("custom description")
        void annotatedMethod() {}

        @SlowTest(expectedMillis = 5000)
        void annotatedMethodWithExpectedMillis() {}

        @Test
        @DisplayName("Should retrieve annotation value")
        void shouldRetrieveAnnotationValue() throws NoSuchMethodException {
            var method = UsageTests.class.getDeclaredMethod("annotatedMethod");
            var annotation = method.getAnnotation(SlowTest.class);
            assertThat(annotation.value()).isEqualTo("custom description");
        }

        @Test
        @DisplayName("Should retrieve annotation expectedMillis")
        void shouldRetrieveAnnotationExpectedMillis() throws NoSuchMethodException {
            var method = UsageTests.class.getDeclaredMethod("annotatedMethodWithExpectedMillis");
            var annotation = method.getAnnotation(SlowTest.class);
            assertThat(annotation.expectedMillis()).isEqualTo(5000L);
        }
    }
}
