package cloud.opencode.base.feature.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureVariant 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FeatureVariant 测试")
class FeatureVariantTest {

    @FeatureVariant(feature = "checkout-flow", variant = "A")
    void variantA() {}

    @FeatureVariant(feature = "checkout-flow", variant = "B", percentage = 50, description = "New flow")
    void variantB() {}

    @FeatureVariant(feature = "type-level", variant = "X")
    static class AnnotatedClass {}

    @Nested
    @DisplayName("注解元素测试")
    class AnnotationElementTests {

        @Test
        @DisplayName("feature()返回功能键")
        void testFeature() throws NoSuchMethodException {
            Method method = FeatureVariantTest.class.getDeclaredMethod("variantA");
            FeatureVariant annotation = method.getAnnotation(FeatureVariant.class);

            assertThat(annotation.feature()).isEqualTo("checkout-flow");
        }

        @Test
        @DisplayName("variant()返回变体标识")
        void testVariant() throws NoSuchMethodException {
            Method method = FeatureVariantTest.class.getDeclaredMethod("variantA");
            FeatureVariant annotation = method.getAnnotation(FeatureVariant.class);

            assertThat(annotation.variant()).isEqualTo("A");
        }

        @Test
        @DisplayName("percentage()默认值为0")
        void testPercentageDefault() throws NoSuchMethodException {
            Method method = FeatureVariantTest.class.getDeclaredMethod("variantA");
            FeatureVariant annotation = method.getAnnotation(FeatureVariant.class);

            assertThat(annotation.percentage()).isZero();
        }

        @Test
        @DisplayName("percentage()可设置")
        void testPercentageSet() throws NoSuchMethodException {
            Method method = FeatureVariantTest.class.getDeclaredMethod("variantB");
            FeatureVariant annotation = method.getAnnotation(FeatureVariant.class);

            assertThat(annotation.percentage()).isEqualTo(50);
        }

        @Test
        @DisplayName("description()默认为空")
        void testDescriptionDefault() throws NoSuchMethodException {
            Method method = FeatureVariantTest.class.getDeclaredMethod("variantA");
            FeatureVariant annotation = method.getAnnotation(FeatureVariant.class);

            assertThat(annotation.description()).isEmpty();
        }

        @Test
        @DisplayName("description()可设置")
        void testDescriptionSet() throws NoSuchMethodException {
            Method method = FeatureVariantTest.class.getDeclaredMethod("variantB");
            FeatureVariant annotation = method.getAnnotation(FeatureVariant.class);

            assertThat(annotation.description()).isEqualTo("New flow");
        }
    }

    @Nested
    @DisplayName("注解目标测试")
    class TargetTests {

        @Test
        @DisplayName("可用于方法")
        void testCanBeAppliedToMethod() throws NoSuchMethodException {
            Method method = FeatureVariantTest.class.getDeclaredMethod("variantA");

            assertThat(method.isAnnotationPresent(FeatureVariant.class)).isTrue();
        }

        @Test
        @DisplayName("可用于类型")
        void testCanBeAppliedToType() {
            assertThat(AnnotatedClass.class.isAnnotationPresent(FeatureVariant.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("注解保留策略测试")
    class RetentionTests {

        @Test
        @DisplayName("运行时可获取")
        void testRuntimeRetention() throws NoSuchMethodException {
            Method method = FeatureVariantTest.class.getDeclaredMethod("variantA");
            Annotation annotation = method.getAnnotation(FeatureVariant.class);

            assertThat(annotation).isNotNull();
        }
    }
}
