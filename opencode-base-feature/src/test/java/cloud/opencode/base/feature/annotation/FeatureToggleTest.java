package cloud.opencode.base.feature.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureToggle 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FeatureToggle 测试")
class FeatureToggleTest {

    @FeatureToggle("test-feature")
    void annotatedMethod() {}

    @FeatureToggle(value = "beta-feature", defaultEnabled = true, description = "Beta feature")
    void fullyAnnotatedMethod() {}

    @FeatureToggle("type-level")
    static class AnnotatedClass {}

    @Nested
    @DisplayName("注解元素测试")
    class AnnotationElementTests {

        @Test
        @DisplayName("value()返回功能键")
        void testValue() throws NoSuchMethodException {
            Method method = FeatureToggleTest.class.getDeclaredMethod("annotatedMethod");
            FeatureToggle annotation = method.getAnnotation(FeatureToggle.class);

            assertThat(annotation.value()).isEqualTo("test-feature");
        }

        @Test
        @DisplayName("defaultEnabled()默认值为false")
        void testDefaultEnabledDefault() throws NoSuchMethodException {
            Method method = FeatureToggleTest.class.getDeclaredMethod("annotatedMethod");
            FeatureToggle annotation = method.getAnnotation(FeatureToggle.class);

            assertThat(annotation.defaultEnabled()).isFalse();
        }

        @Test
        @DisplayName("defaultEnabled()可设置为true")
        void testDefaultEnabledTrue() throws NoSuchMethodException {
            Method method = FeatureToggleTest.class.getDeclaredMethod("fullyAnnotatedMethod");
            FeatureToggle annotation = method.getAnnotation(FeatureToggle.class);

            assertThat(annotation.defaultEnabled()).isTrue();
        }

        @Test
        @DisplayName("description()默认为空")
        void testDescriptionDefault() throws NoSuchMethodException {
            Method method = FeatureToggleTest.class.getDeclaredMethod("annotatedMethod");
            FeatureToggle annotation = method.getAnnotation(FeatureToggle.class);

            assertThat(annotation.description()).isEmpty();
        }

        @Test
        @DisplayName("description()可设置")
        void testDescriptionSet() throws NoSuchMethodException {
            Method method = FeatureToggleTest.class.getDeclaredMethod("fullyAnnotatedMethod");
            FeatureToggle annotation = method.getAnnotation(FeatureToggle.class);

            assertThat(annotation.description()).isEqualTo("Beta feature");
        }
    }

    @Nested
    @DisplayName("注解目标测试")
    class TargetTests {

        @Test
        @DisplayName("可用于方法")
        void testCanBeAppliedToMethod() throws NoSuchMethodException {
            Method method = FeatureToggleTest.class.getDeclaredMethod("annotatedMethod");

            assertThat(method.isAnnotationPresent(FeatureToggle.class)).isTrue();
        }

        @Test
        @DisplayName("可用于类型")
        void testCanBeAppliedToType() {
            assertThat(AnnotatedClass.class.isAnnotationPresent(FeatureToggle.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("注解保留策略测试")
    class RetentionTests {

        @Test
        @DisplayName("运行时可获取")
        void testRuntimeRetention() throws NoSuchMethodException {
            Method method = FeatureToggleTest.class.getDeclaredMethod("annotatedMethod");
            Annotation annotation = method.getAnnotation(FeatureToggle.class);

            assertThat(annotation).isNotNull();
        }
    }
}
