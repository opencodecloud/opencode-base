package cloud.opencode.base.test.annotation;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * RepeatTest Tests
 * RepeatTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("Repeat Annotation Tests")
class RepeatTest {

    @Repeat(5)
    void methodWithRepeat5() {
    }

    @Repeat(value = 3, failFast = false)
    void methodWithCustomSettings() {
    }

    @Repeat
    void methodWithDefaultRepeat() {
    }

    @Nested
    @DisplayName("Annotation Properties Tests")
    class AnnotationPropertiesTests {

        @Test
        @DisplayName("Should have value attribute")
        void shouldHaveValueAttribute() throws NoSuchMethodException {
            Method method = RepeatTest.class.getDeclaredMethod("methodWithRepeat5");
            Repeat repeat = method.getAnnotation(Repeat.class);

            assertThat(repeat.value()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should have failFast attribute")
        void shouldHaveFailFastAttribute() throws NoSuchMethodException {
            Method method = RepeatTest.class.getDeclaredMethod("methodWithCustomSettings");
            Repeat repeat = method.getAnnotation(Repeat.class);

            assertThat(repeat.value()).isEqualTo(3);
            assertThat(repeat.failFast()).isFalse();
        }

        @Test
        @DisplayName("Default value should be 1")
        void defaultValueShouldBe1() throws NoSuchMethodException {
            Method method = RepeatTest.class.getDeclaredMethod("methodWithDefaultRepeat");
            Repeat repeat = method.getAnnotation(Repeat.class);

            assertThat(repeat.value()).isEqualTo(1);
        }

        @Test
        @DisplayName("Default failFast should be true")
        void defaultFailFastShouldBeTrue() throws NoSuchMethodException {
            Method method = RepeatTest.class.getDeclaredMethod("methodWithRepeat5");
            Repeat repeat = method.getAnnotation(Repeat.class);

            assertThat(repeat.failFast()).isTrue();
        }
    }

    @Nested
    @DisplayName("Annotation Retention Tests")
    class AnnotationRetentionTests {

        @Test
        @DisplayName("Should be retained at runtime")
        void shouldBeRetainedAtRuntime() throws NoSuchMethodException {
            Method method = RepeatTest.class.getDeclaredMethod("methodWithRepeat5");
            assertThat(method.isAnnotationPresent(Repeat.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Annotation Target Tests")
    class AnnotationTargetTests {

        @Test
        @DisplayName("Should be applicable to methods")
        void shouldBeApplicableToMethods() throws NoSuchMethodException {
            Method method = RepeatTest.class.getDeclaredMethod("methodWithRepeat5");
            Repeat repeat = method.getAnnotation(Repeat.class);

            assertThat(repeat).isNotNull();
        }
    }
}
