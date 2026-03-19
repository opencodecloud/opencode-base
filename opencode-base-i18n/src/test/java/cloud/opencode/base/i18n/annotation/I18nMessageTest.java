package cloud.opencode.base.i18n.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * I18nMessage 注解测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("I18nMessage 注解测试")
class I18nMessageTest {

    @Nested
    @DisplayName("注解元数据测试")
    class MetadataTests {

        @Test
        @DisplayName("注解保留策略为RUNTIME")
        void testRetentionPolicy() {
            Retention retention = I18nMessage.class.getAnnotation(Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("注解目标包含METHOD")
        void testTargetMethod() {
            Target target = I18nMessage.class.getAnnotation(Target.class);

            assertThat(target.value()).contains(ElementType.METHOD);
        }

        @Test
        @DisplayName("注解目标包含FIELD")
        void testTargetField() {
            Target target = I18nMessage.class.getAnnotation(Target.class);

            assertThat(target.value()).contains(ElementType.FIELD);
        }

        @Test
        @DisplayName("注解目标包含TYPE")
        void testTargetType() {
            Target target = I18nMessage.class.getAnnotation(Target.class);

            assertThat(target.value()).contains(ElementType.TYPE);
        }

        @Test
        @DisplayName("注解目标包含PARAMETER")
        void testTargetParameter() {
            Target target = I18nMessage.class.getAnnotation(Target.class);

            assertThat(target.value()).contains(ElementType.PARAMETER);
        }

        @Test
        @DisplayName("注解有Documented")
        void testDocumented() {
            assertThat(I18nMessage.class.isAnnotationPresent(Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("注解属性测试")
    class AttributeTests {

        @Test
        @DisplayName("value属性存在")
        void testValueAttribute() throws NoSuchMethodException {
            Method method = I18nMessage.class.getMethod("value");

            assertThat(method.getReturnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("defaultValue属性默认为空字符串")
        void testDefaultValueAttribute() throws NoSuchMethodException {
            Method method = I18nMessage.class.getMethod("defaultValue");

            assertThat(method.getDefaultValue()).isEqualTo("");
        }

        @Test
        @DisplayName("bundle属性默认为空字符串")
        void testBundleAttribute() throws NoSuchMethodException {
            Method method = I18nMessage.class.getMethod("bundle");

            assertThat(method.getDefaultValue()).isEqualTo("");
        }

        @Test
        @DisplayName("args属性默认为空数组")
        void testArgsAttribute() throws NoSuchMethodException {
            Method method = I18nMessage.class.getMethod("args");

            assertThat((String[]) method.getDefaultValue()).isEmpty();
        }

        @Test
        @DisplayName("useKeyAsDefault属性默认为false")
        void testUseKeyAsDefaultAttribute() throws NoSuchMethodException {
            Method method = I18nMessage.class.getMethod("useKeyAsDefault");

            assertThat(method.getDefaultValue()).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("注解使用测试")
    class UsageTests {

        @I18nMessage("test.method.key")
        public String testMethod() {
            return "test";
        }

        @I18nMessage(value = "test.key", defaultValue = "Default Message")
        private String testField;

        @Test
        @DisplayName("方法上使用注解")
        void testOnMethod() throws NoSuchMethodException {
            Method method = UsageTests.class.getMethod("testMethod");

            I18nMessage annotation = method.getAnnotation(I18nMessage.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("test.method.key");
        }

        @Test
        @DisplayName("字段上使用注解")
        void testOnField() throws NoSuchFieldException {
            Field field = UsageTests.class.getDeclaredField("testField");

            I18nMessage annotation = field.getAnnotation(I18nMessage.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("test.key");
            assertThat(annotation.defaultValue()).isEqualTo("Default Message");
        }

        @I18nMessage("test.class.key")
        static class AnnotatedClass {
        }

        @Test
        @DisplayName("类上使用注解")
        void testOnClass() {
            I18nMessage annotation = AnnotatedClass.class.getAnnotation(I18nMessage.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("test.class.key");
        }
    }

    @Nested
    @DisplayName("完整属性测试")
    class FullAttributeTests {

        @I18nMessage(
                value = "full.test.key",
                defaultValue = "Default",
                bundle = "custom-bundle",
                args = {"arg1", "arg2"},
                useKeyAsDefault = true
        )
        private String fullTest;

        @Test
        @DisplayName("所有属性正确设置")
        void testAllAttributes() throws NoSuchFieldException {
            Field field = FullAttributeTests.class.getDeclaredField("fullTest");
            I18nMessage annotation = field.getAnnotation(I18nMessage.class);

            assertThat(annotation.value()).isEqualTo("full.test.key");
            assertThat(annotation.defaultValue()).isEqualTo("Default");
            assertThat(annotation.bundle()).isEqualTo("custom-bundle");
            assertThat(annotation.args()).containsExactly("arg1", "arg2");
            assertThat(annotation.useKeyAsDefault()).isTrue();
        }
    }
}
