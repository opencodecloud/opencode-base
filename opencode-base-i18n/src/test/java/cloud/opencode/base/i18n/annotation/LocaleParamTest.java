package cloud.opencode.base.i18n.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * LocaleParam 注解测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("LocaleParam 注解测试")
class LocaleParamTest {

    @Nested
    @DisplayName("注解元数据测试")
    class MetadataTests {

        @Test
        @DisplayName("注解保留策略为RUNTIME")
        void testRetentionPolicy() {
            Retention retention = LocaleParam.class.getAnnotation(Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("注解目标仅包含PARAMETER")
        void testTargetParameter() {
            Target target = LocaleParam.class.getAnnotation(Target.class);

            assertThat(target.value()).containsExactly(ElementType.PARAMETER);
        }

        @Test
        @DisplayName("注解有Documented")
        void testDocumented() {
            assertThat(LocaleParam.class.isAnnotationPresent(Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("注解属性测试")
    class AttributeTests {

        @Test
        @DisplayName("type属性默认为LOCALE")
        void testTypeDefault() throws NoSuchMethodException {
            Method method = LocaleParam.class.getMethod("type");

            assertThat(method.getDefaultValue()).isEqualTo(LocaleParam.Type.LOCALE);
        }

        @Test
        @DisplayName("useDefault属性默认为true")
        void testUseDefaultDefault() throws NoSuchMethodException {
            Method method = LocaleParam.class.getMethod("useDefault");

            assertThat(method.getDefaultValue()).isEqualTo(true);
        }

        @Test
        @DisplayName("fallback属性默认为空字符串")
        void testFallbackDefault() throws NoSuchMethodException {
            Method method = LocaleParam.class.getMethod("fallback");

            assertThat(method.getDefaultValue()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Type枚举测试")
    class TypeEnumTests {

        @Test
        @DisplayName("LOCALE类型存在")
        void testLocaleType() {
            assertThat(LocaleParam.Type.LOCALE).isNotNull();
        }

        @Test
        @DisplayName("LANGUAGE_TAG类型存在")
        void testLanguageTagType() {
            assertThat(LocaleParam.Type.LANGUAGE_TAG).isNotNull();
        }

        @Test
        @DisplayName("LOCALE_STRING类型存在")
        void testLocaleStringType() {
            assertThat(LocaleParam.Type.LOCALE_STRING).isNotNull();
        }

        @Test
        @DisplayName("REQUEST类型存在")
        void testRequestType() {
            assertThat(LocaleParam.Type.REQUEST).isNotNull();
        }

        @Test
        @DisplayName("CONTEXT类型存在")
        void testContextType() {
            assertThat(LocaleParam.Type.CONTEXT).isNotNull();
        }

        @Test
        @DisplayName("所有Type值")
        void testAllTypeValues() {
            LocaleParam.Type[] types = LocaleParam.Type.values();

            assertThat(types).containsExactly(
                    LocaleParam.Type.LOCALE,
                    LocaleParam.Type.LANGUAGE_TAG,
                    LocaleParam.Type.LOCALE_STRING,
                    LocaleParam.Type.REQUEST,
                    LocaleParam.Type.CONTEXT
            );
        }

        @Test
        @DisplayName("valueOf方法")
        void testTypeValueOf() {
            assertThat(LocaleParam.Type.valueOf("LOCALE")).isEqualTo(LocaleParam.Type.LOCALE);
            assertThat(LocaleParam.Type.valueOf("LANGUAGE_TAG")).isEqualTo(LocaleParam.Type.LANGUAGE_TAG);
        }
    }

    @Nested
    @DisplayName("注解使用测试")
    class UsageTests {

        public String testMethod(@LocaleParam Locale locale) {
            return "test";
        }

        public String testMethodWithType(@LocaleParam(type = LocaleParam.Type.LANGUAGE_TAG) String languageTag) {
            return "test";
        }

        public String testMethodWithFallback(@LocaleParam(fallback = "en-US") Locale locale) {
            return "test";
        }

        @Test
        @DisplayName("参数上使用注解")
        void testOnParameter() throws NoSuchMethodException {
            Method method = UsageTests.class.getMethod("testMethod", Locale.class);
            Parameter parameter = method.getParameters()[0];

            LocaleParam annotation = parameter.getAnnotation(LocaleParam.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.type()).isEqualTo(LocaleParam.Type.LOCALE);
        }

        @Test
        @DisplayName("指定type使用注解")
        void testWithType() throws NoSuchMethodException {
            Method method = UsageTests.class.getMethod("testMethodWithType", String.class);
            Parameter parameter = method.getParameters()[0];

            LocaleParam annotation = parameter.getAnnotation(LocaleParam.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.type()).isEqualTo(LocaleParam.Type.LANGUAGE_TAG);
        }

        @Test
        @DisplayName("指定fallback使用注解")
        void testWithFallback() throws NoSuchMethodException {
            Method method = UsageTests.class.getMethod("testMethodWithFallback", Locale.class);
            Parameter parameter = method.getParameters()[0];

            LocaleParam annotation = parameter.getAnnotation(LocaleParam.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.fallback()).isEqualTo("en-US");
        }
    }

    @Nested
    @DisplayName("完整属性测试")
    class FullAttributeTests {

        public String testMethodFull(
                @LocaleParam(
                        type = LocaleParam.Type.REQUEST,
                        useDefault = false,
                        fallback = "zh-CN"
                ) Object request
        ) {
            return "test";
        }

        @Test
        @DisplayName("所有属性正确设置")
        void testAllAttributes() throws NoSuchMethodException {
            Method method = FullAttributeTests.class.getMethod("testMethodFull", Object.class);
            Parameter parameter = method.getParameters()[0];

            LocaleParam annotation = parameter.getAnnotation(LocaleParam.class);

            assertThat(annotation.type()).isEqualTo(LocaleParam.Type.REQUEST);
            assertThat(annotation.useDefault()).isFalse();
            assertThat(annotation.fallback()).isEqualTo("zh-CN");
        }
    }
}
