package cloud.opencode.base.json.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonProperty 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonProperty 注解测试")
class JsonPropertyTest {

    @Nested
    @DisplayName("注解属性测试")
    class AnnotationAttributeTests {

        @Test
        @DisplayName("注解可用于字段")
        void testTargetField() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("renamedField");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解可用于方法")
        void testTargetMethod() throws NoSuchMethodException {
            Method method = TestClass.class.getDeclaredMethod("getRenamedValue");
            JsonProperty annotation = method.getAnnotation(JsonProperty.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("获取value属性")
        void testValueAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("renamedField");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation.value()).isEqualTo("custom_name");
        }

        @Test
        @DisplayName("获取required属性")
        void testRequiredAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("requiredField");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation.required()).isTrue();
        }

        @Test
        @DisplayName("获取defaultValue属性")
        void testDefaultValueAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("defaultValueField");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation.defaultValue()).isEqualTo("\"default\"");
        }

        @Test
        @DisplayName("获取access属性")
        void testAccessAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("readOnlyField");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation.access()).isEqualTo(JsonProperty.Access.READ_ONLY);
        }

        @Test
        @DisplayName("获取index属性")
        void testIndexAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("indexedField");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation.index()).isEqualTo(1);
        }

        @Test
        @DisplayName("默认值测试")
        void testDefaultValues() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("defaultField");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation.value()).isEmpty();
            assertThat(annotation.required()).isFalse();
            assertThat(annotation.defaultValue()).isEmpty();
            assertThat(annotation.access()).isEqualTo(JsonProperty.Access.AUTO);
            assertThat(annotation.index()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Access枚举测试")
    class AccessEnumTests {

        @Test
        @DisplayName("所有Access值存在")
        void testAllAccessValues() {
            assertThat(JsonProperty.Access.values())
                .containsExactlyInAnyOrder(
                    JsonProperty.Access.AUTO,
                    JsonProperty.Access.READ_ONLY,
                    JsonProperty.Access.WRITE_ONLY
                );
        }

        @Test
        @DisplayName("valueOf返回正确值")
        void testValueOf() {
            assertThat(JsonProperty.Access.valueOf("AUTO")).isEqualTo(JsonProperty.Access.AUTO);
            assertThat(JsonProperty.Access.valueOf("READ_ONLY")).isEqualTo(JsonProperty.Access.READ_ONLY);
            assertThat(JsonProperty.Access.valueOf("WRITE_ONLY")).isEqualTo(JsonProperty.Access.WRITE_ONLY);
        }

        @Test
        @DisplayName("无效Access名抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> JsonProperty.Access.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Access名称正确")
        void testAccessNames() {
            assertThat(JsonProperty.Access.AUTO.name()).isEqualTo("AUTO");
            assertThat(JsonProperty.Access.READ_ONLY.name()).isEqualTo("READ_ONLY");
            assertThat(JsonProperty.Access.WRITE_ONLY.name()).isEqualTo("WRITE_ONLY");
        }

        @Test
        @DisplayName("Access序号正确")
        void testAccessOrdinals() {
            assertThat(JsonProperty.Access.AUTO.ordinal()).isEqualTo(0);
            assertThat(JsonProperty.Access.READ_ONLY.ordinal()).isEqualTo(1);
            assertThat(JsonProperty.Access.WRITE_ONLY.ordinal()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("注解元数据测试")
    class AnnotationMetadataTests {

        @Test
        @DisplayName("Target包含FIELD、METHOD、PARAMETER")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonProperty.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonProperty.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            java.lang.annotation.Documented documented =
                JsonProperty.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }
    }

    @Nested
    @DisplayName("参数级别注解测试")
    class ParameterLevelAnnotationTests {

        @Test
        @DisplayName("注解可用于构造函数参数")
        void testAnnotationOnParameter() throws NoSuchMethodException {
            Parameter[] parameters = ConstructorClass.class
                .getDeclaredConstructor(String.class)
                .getParameters();

            JsonProperty annotation = parameters[0].getAnnotation(JsonProperty.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("使用场景测试")
    class UsageScenarioTests {

        @Test
        @DisplayName("蛇形命名字段映射")
        void testSnakeCaseMapping() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("userName");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation.value()).isEqualTo("user_name");
        }

        @Test
        @DisplayName("必填邮箱字段")
        void testRequiredEmail() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("email");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation.required()).isTrue();
        }

        @Test
        @DisplayName("只读创建时间")
        void testReadOnlyCreatedAt() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("createdAt");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation.access()).isEqualTo(JsonProperty.Access.READ_ONLY);
        }

        @Test
        @DisplayName("只写密码")
        void testWriteOnlyPassword() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("password");
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            assertThat(annotation.access()).isEqualTo(JsonProperty.Access.WRITE_ONLY);
        }
    }

    // Test helper classes
    static class TestClass {
        @JsonProperty("custom_name")
        String renamedField;

        @JsonProperty(required = true)
        String requiredField;

        @JsonProperty(defaultValue = "\"default\"")
        String defaultValueField;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String readOnlyField;

        @JsonProperty(index = 1)
        String indexedField;

        @JsonProperty
        String defaultField;

        @JsonProperty("method_name")
        String getRenamedValue() {
            return "value";
        }
    }

    static class ConstructorClass {
        String name;

        ConstructorClass(@JsonProperty("name") String name) {
            this.name = name;
        }
    }

    static class UserClass {
        @JsonProperty("user_name")
        String userName;

        @JsonProperty(value = "email", required = true)
        String email;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String createdAt;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password;
    }
}
