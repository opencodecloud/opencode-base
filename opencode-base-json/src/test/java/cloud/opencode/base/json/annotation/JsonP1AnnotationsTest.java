package cloud.opencode.base.json.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * P1 优先级注解测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("P1 注解测试")
class JsonP1AnnotationsTest {

    // ======================== JsonAnySetter ========================

    @Nested
    @DisplayName("JsonAnySetter 注解测试")
    class JsonAnySetterTests {

        @Test
        @DisplayName("注解可用于方法")
        void testTargetMethod() throws NoSuchMethodException {
            Method method = AnySetterClass.class.getDeclaredMethod("setExtra", String.class, Object.class);
            JsonAnySetter annotation = method.getAnnotation(JsonAnySetter.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解可用于字段")
        void testTargetField() throws NoSuchFieldException {
            Field field = AnySetterClass.class.getDeclaredField("extras");
            JsonAnySetter annotation = field.getAnnotation(JsonAnySetter.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("默认enabled为true")
        void testDefaultEnabled() throws NoSuchFieldException {
            Field field = AnySetterClass.class.getDeclaredField("extras");
            JsonAnySetter annotation = field.getAnnotation(JsonAnySetter.class);

            assertThat(annotation.enabled()).isTrue();
        }

        @Test
        @DisplayName("可设置enabled为false")
        void testDisabled() throws NoSuchMethodException {
            Method method = AnySetterClass.class.getDeclaredMethod("setDisabled", String.class, Object.class);
            JsonAnySetter annotation = method.getAnnotation(JsonAnySetter.class);

            assertThat(annotation.enabled()).isFalse();
        }

        @Test
        @DisplayName("Target包含METHOD和FIELD")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonAnySetter.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.METHOD, ElementType.FIELD);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonAnySetter.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            assertThat(JsonAnySetter.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    // ======================== JsonAnyGetter ========================

    @Nested
    @DisplayName("JsonAnyGetter 注解测试")
    class JsonAnyGetterTests {

        @Test
        @DisplayName("注解可用于方法")
        void testTargetMethod() throws NoSuchMethodException {
            Method method = AnyGetterClass.class.getDeclaredMethod("getExtras");
            JsonAnyGetter annotation = method.getAnnotation(JsonAnyGetter.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解可用于字段")
        void testTargetField() throws NoSuchFieldException {
            Field field = AnyGetterClass.class.getDeclaredField("props");
            JsonAnyGetter annotation = field.getAnnotation(JsonAnyGetter.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("默认enabled为true")
        void testDefaultEnabled() throws NoSuchMethodException {
            Method method = AnyGetterClass.class.getDeclaredMethod("getExtras");
            JsonAnyGetter annotation = method.getAnnotation(JsonAnyGetter.class);

            assertThat(annotation.enabled()).isTrue();
        }

        @Test
        @DisplayName("可设置enabled为false")
        void testDisabled() throws NoSuchFieldException {
            Field field = AnyGetterClass.class.getDeclaredField("disabled");
            JsonAnyGetter annotation = field.getAnnotation(JsonAnyGetter.class);

            assertThat(annotation.enabled()).isFalse();
        }

        @Test
        @DisplayName("Target包含METHOD和FIELD")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonAnyGetter.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.METHOD, ElementType.FIELD);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonAnyGetter.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            assertThat(JsonAnyGetter.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    // ======================== JsonRawValue ========================

    @Nested
    @DisplayName("JsonRawValue 注解测试")
    class JsonRawValueTests {

        @Test
        @DisplayName("注解可用于字段")
        void testTargetField() throws NoSuchFieldException {
            Field field = RawValueClass.class.getDeclaredField("payload");
            JsonRawValue annotation = field.getAnnotation(JsonRawValue.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解可用于方法")
        void testTargetMethod() throws NoSuchMethodException {
            Method method = RawValueClass.class.getDeclaredMethod("getRawJson");
            JsonRawValue annotation = method.getAnnotation(JsonRawValue.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("默认value为true")
        void testDefaultValue() throws NoSuchFieldException {
            Field field = RawValueClass.class.getDeclaredField("payload");
            JsonRawValue annotation = field.getAnnotation(JsonRawValue.class);

            assertThat(annotation.value()).isTrue();
        }

        @Test
        @DisplayName("可设置value为false")
        void testDisabled() throws NoSuchFieldException {
            Field field = RawValueClass.class.getDeclaredField("notRaw");
            JsonRawValue annotation = field.getAnnotation(JsonRawValue.class);

            assertThat(annotation.value()).isFalse();
        }

        @Test
        @DisplayName("Target包含METHOD和FIELD")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonRawValue.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.METHOD, ElementType.FIELD);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonRawValue.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            assertThat(JsonRawValue.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    // ======================== JsonRootName ========================

    @Nested
    @DisplayName("JsonRootName 注解测试")
    class JsonRootNameTests {

        @Test
        @DisplayName("注解可用于类型")
        void testTargetType() {
            JsonRootName annotation = RootNameClass.class.getAnnotation(JsonRootName.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("获取value属性")
        void testValueAttribute() {
            JsonRootName annotation = RootNameClass.class.getAnnotation(JsonRootName.class);

            assertThat(annotation.value()).isEqualTo("root");
        }

        @Test
        @DisplayName("获取namespace属性")
        void testNamespaceAttribute() {
            JsonRootName annotation = RootNameWithNs.class.getAnnotation(JsonRootName.class);

            assertThat(annotation.namespace()).isEqualTo("http://example.com/ns");
        }

        @Test
        @DisplayName("默认值测试")
        void testDefaultValues() {
            JsonRootName annotation = RootNameDefaults.class.getAnnotation(JsonRootName.class);

            assertThat(annotation.value()).isEmpty();
            assertThat(annotation.namespace()).isEmpty();
        }

        @Test
        @DisplayName("Target包含TYPE")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonRootName.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonRootName.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            assertThat(JsonRootName.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    // ======================== JsonAlias ========================

    @Nested
    @DisplayName("JsonAlias 注解测试")
    class JsonAliasTests {

        @Test
        @DisplayName("注解可用于字段")
        void testTargetField() throws NoSuchFieldException {
            Field field = AliasClass.class.getDeclaredField("name");
            JsonAlias annotation = field.getAnnotation(JsonAlias.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解可用于方法")
        void testTargetMethod() throws NoSuchMethodException {
            Method method = AliasClass.class.getDeclaredMethod("setEmail", String.class);
            JsonAlias annotation = method.getAnnotation(JsonAlias.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解可用于参数")
        void testTargetParameter() throws NoSuchMethodException {
            Parameter[] parameters = AliasConstructor.class
                .getDeclaredConstructor(String.class)
                .getParameters();

            JsonAlias annotation = parameters[0].getAnnotation(JsonAlias.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("id", "identifier");
        }

        @Test
        @DisplayName("获取多个别名")
        void testMultipleAliases() throws NoSuchFieldException {
            Field field = AliasClass.class.getDeclaredField("name");
            JsonAlias annotation = field.getAnnotation(JsonAlias.class);

            assertThat(annotation.value())
                .containsExactly("user_name", "username", "login");
        }

        @Test
        @DisplayName("获取单个别名")
        void testSingleAlias() throws NoSuchMethodException {
            Method method = AliasClass.class.getDeclaredMethod("setEmail", String.class);
            JsonAlias annotation = method.getAnnotation(JsonAlias.class);

            assertThat(annotation.value()).containsExactly("mail");
        }

        @Test
        @DisplayName("Target包含FIELD、METHOD、PARAMETER")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonAlias.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonAlias.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            assertThat(JsonAlias.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    // ======================== JsonFilter ========================

    @Nested
    @DisplayName("JsonFilter 注解测试")
    class JsonFilterTests {

        @Test
        @DisplayName("注解可用于类型")
        void testTargetType() {
            JsonFilter annotation = FilteredClass.class.getAnnotation(JsonFilter.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解可用于方法")
        void testTargetMethod() throws NoSuchMethodException {
            Method method = FilterMethodClass.class.getDeclaredMethod("getFiltered");
            JsonFilter annotation = method.getAnnotation(JsonFilter.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("methodFilter");
        }

        @Test
        @DisplayName("注解可用于字段")
        void testTargetField() throws NoSuchFieldException {
            Field field = FilterMethodClass.class.getDeclaredField("filtered");
            JsonFilter annotation = field.getAnnotation(JsonFilter.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("fieldFilter");
        }

        @Test
        @DisplayName("获取value属性")
        void testValueAttribute() {
            JsonFilter annotation = FilteredClass.class.getAnnotation(JsonFilter.class);

            assertThat(annotation.value()).isEqualTo("sensitiveFilter");
        }

        @Test
        @DisplayName("Target包含TYPE、METHOD、FIELD")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonFilter.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.TYPE, ElementType.METHOD, ElementType.FIELD);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonFilter.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            assertThat(JsonFilter.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    // ======================== JsonInject ========================

    @Nested
    @DisplayName("JsonInject 注解测试")
    class JsonInjectTests {

        @Test
        @DisplayName("注解可用于字段")
        void testTargetField() throws NoSuchFieldException {
            Field field = InjectClass.class.getDeclaredField("auditor");
            JsonInject annotation = field.getAnnotation(JsonInject.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解可用于方法")
        void testTargetMethod() throws NoSuchMethodException {
            Method method = InjectClass.class.getDeclaredMethod("setContext", String.class);
            JsonInject annotation = method.getAnnotation(JsonInject.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("ctx");
        }

        @Test
        @DisplayName("注解可用于参数")
        void testTargetParameter() throws NoSuchMethodException {
            Parameter[] parameters = InjectConstructor.class
                .getDeclaredConstructor(long.class)
                .getParameters();

            JsonInject annotation = parameters[0].getAnnotation(JsonInject.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("ts");
        }

        @Test
        @DisplayName("获取value属性")
        void testValueAttribute() throws NoSuchFieldException {
            Field field = InjectClass.class.getDeclaredField("auditor");
            JsonInject annotation = field.getAnnotation(JsonInject.class);

            assertThat(annotation.value()).isEqualTo("currentUser");
        }

        @Test
        @DisplayName("获取useInput属性")
        void testUseInputAttribute() throws NoSuchFieldException {
            Field field = InjectClass.class.getDeclaredField("timestamp");
            JsonInject annotation = field.getAnnotation(JsonInject.class);

            assertThat(annotation.useInput()).isFalse();
        }

        @Test
        @DisplayName("默认值测试")
        void testDefaultValues() throws NoSuchFieldException {
            Field field = InjectClass.class.getDeclaredField("defaultInject");
            JsonInject annotation = field.getAnnotation(JsonInject.class);

            assertThat(annotation.value()).isEmpty();
            assertThat(annotation.useInput()).isTrue();
        }

        @Test
        @DisplayName("Target包含FIELD、METHOD、PARAMETER")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonInject.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonInject.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            assertThat(JsonInject.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    // ======================== JsonEnumDefaultValue ========================

    @Nested
    @DisplayName("JsonEnumDefaultValue 注解测试")
    class JsonEnumDefaultValueTests {

        @Test
        @DisplayName("注解可用于枚举常量")
        void testOnEnumConstant() throws NoSuchFieldException {
            Field field = TestColor.class.getDeclaredField("UNKNOWN");
            JsonEnumDefaultValue annotation = field.getAnnotation(JsonEnumDefaultValue.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("未标注的枚举常量无注解")
        void testNonAnnotatedConstant() throws NoSuchFieldException {
            Field field = TestColor.class.getDeclaredField("RED");
            JsonEnumDefaultValue annotation = field.getAnnotation(JsonEnumDefaultValue.class);

            assertThat(annotation).isNull();
        }

        @Test
        @DisplayName("标记注解无属性")
        void testMarkerAnnotation() {
            assertThat(JsonEnumDefaultValue.class.getDeclaredMethods()).isEmpty();
        }

        @Test
        @DisplayName("通过反射查找默认枚举值")
        void testFindDefaultValue() {
            TestColor defaultColor = null;
            for (TestColor color : TestColor.values()) {
                try {
                    Field field = TestColor.class.getDeclaredField(color.name());
                    if (field.isAnnotationPresent(JsonEnumDefaultValue.class)) {
                        defaultColor = color;
                        break;
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }

            assertThat(defaultColor).isEqualTo(TestColor.UNKNOWN);
        }

        @Test
        @DisplayName("Target包含FIELD")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonEnumDefaultValue.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonEnumDefaultValue.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            assertThat(JsonEnumDefaultValue.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    // ======================== Helper Classes ========================

    // JsonAnySetter helpers
    @SuppressWarnings("unused")
    static class AnySetterClass {
        @JsonAnySetter
        Map<String, Object> extras = new LinkedHashMap<>();

        @JsonAnySetter
        void setExtra(String key, Object value) {
            extras.put(key, value);
        }

        @JsonAnySetter(enabled = false)
        void setDisabled(String key, Object value) {
            // disabled
        }
    }

    // JsonAnyGetter helpers
    @SuppressWarnings("unused")
    static class AnyGetterClass {
        @JsonAnyGetter
        Map<String, Object> props = new LinkedHashMap<>();

        @JsonAnyGetter(enabled = false)
        Map<String, Object> disabled = new LinkedHashMap<>();

        @JsonAnyGetter
        Map<String, Object> getExtras() {
            return props;
        }
    }

    // JsonRawValue helpers
    @SuppressWarnings("unused")
    static class RawValueClass {
        @JsonRawValue
        String payload = "{\"key\":\"value\"}";

        @JsonRawValue(false)
        String notRaw = "plain text";

        @JsonRawValue
        String getRawJson() {
            return "{\"a\":1}";
        }
    }

    // JsonRootName helpers
    @JsonRootName("root")
    static class RootNameClass {
        @SuppressWarnings("unused")
        String name;
    }

    @JsonRootName(value = "item", namespace = "http://example.com/ns")
    static class RootNameWithNs {
        @SuppressWarnings("unused")
        String title;
    }

    @JsonRootName
    static class RootNameDefaults {
        @SuppressWarnings("unused")
        String data;
    }

    // JsonAlias helpers
    @SuppressWarnings("unused")
    static class AliasClass {
        @JsonAlias({"user_name", "username", "login"})
        String name;

        @JsonAlias("mail")
        void setEmail(String email) {
            // setter
        }
    }

    static class AliasConstructor {
        @SuppressWarnings("unused")
        String code;

        AliasConstructor(@JsonAlias({"id", "identifier"}) String code) {
            this.code = code;
        }
    }

    // JsonFilter helpers
    @JsonFilter("sensitiveFilter")
    static class FilteredClass {
        @SuppressWarnings("unused")
        String name;
        @SuppressWarnings("unused")
        String ssn;
    }

    @SuppressWarnings("unused")
    static class FilterMethodClass {
        @JsonFilter("fieldFilter")
        String filtered;

        @JsonFilter("methodFilter")
        String getFiltered() {
            return filtered;
        }
    }

    // JsonInject helpers
    @SuppressWarnings("unused")
    static class InjectClass {
        @JsonInject("currentUser")
        String auditor;

        @JsonInject(value = "timestamp", useInput = false)
        long timestamp;

        @JsonInject
        String defaultInject;

        @JsonInject("ctx")
        void setContext(String context) {
            // setter
        }
    }

    static class InjectConstructor {
        @SuppressWarnings("unused")
        long ts;

        InjectConstructor(@JsonInject("ts") long ts) {
            this.ts = ts;
        }
    }

    // JsonEnumDefaultValue helpers
    enum TestColor {
        RED,
        GREEN,
        BLUE,

        @JsonEnumDefaultValue
        UNKNOWN
    }
}
