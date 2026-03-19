package cloud.opencode.base.test.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * IntegrationTestTest Tests
 * IntegrationTestTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("IntegrationTest Annotation Tests")
class IntegrationTestTest {

    @Nested
    @DisplayName("Annotation Properties Tests")
    class AnnotationPropertiesTests {

        @Test
        @DisplayName("Should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = IntegrationTest.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Should target TYPE and METHOD")
        void shouldTargetTypeAndMethod() {
            var target = IntegrationTest.class.getAnnotation(java.lang.annotation.Target.class);
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
            var method = IntegrationTest.class.getMethod("value");
            var defaultValue = method.getDefaultValue();
            assertThat(defaultValue).isEqualTo("");
        }

        @Test
        @DisplayName("Should have empty default requires")
        void shouldHaveEmptyDefaultRequires() throws NoSuchMethodException {
            var method = IntegrationTest.class.getMethod("requires");
            var defaultValue = (String[]) method.getDefaultValue();
            assertThat(defaultValue).isEmpty();
        }
    }

    @Nested
    @DisplayName("Usage Tests")
    class UsageTests {

        @IntegrationTest("db test")
        void annotatedMethod() {}

        @IntegrationTest(requires = {"database", "redis"})
        void annotatedMethodWithRequires() {}

        @Test
        @DisplayName("Should retrieve annotation value")
        void shouldRetrieveAnnotationValue() throws NoSuchMethodException {
            var method = UsageTests.class.getDeclaredMethod("annotatedMethod");
            var annotation = method.getAnnotation(IntegrationTest.class);
            assertThat(annotation.value()).isEqualTo("db test");
        }

        @Test
        @DisplayName("Should retrieve annotation requires")
        void shouldRetrieveAnnotationRequires() throws NoSuchMethodException {
            var method = UsageTests.class.getDeclaredMethod("annotatedMethodWithRequires");
            var annotation = method.getAnnotation(IntegrationTest.class);
            assertThat(annotation.requires()).containsExactly("database", "redis");
        }
    }
}
