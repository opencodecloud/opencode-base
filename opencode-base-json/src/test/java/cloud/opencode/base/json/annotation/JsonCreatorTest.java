package cloud.opencode.base.json.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonCreator / JsonValue 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonCreator / JsonValue 注解测试")
class JsonCreatorTest {

    // ===== JsonCreator Tests =====

    @Nested
    @DisplayName("JsonCreator Mode 枚举测试")
    class ModeEnumTests {

        @Test
        @DisplayName("所有Mode值存在")
        void testAllModeValues() {
            assertThat(JsonCreator.Mode.values())
                .containsExactlyInAnyOrder(
                    JsonCreator.Mode.DEFAULT,
                    JsonCreator.Mode.DELEGATING,
                    JsonCreator.Mode.PROPERTIES,
                    JsonCreator.Mode.DISABLED
                );
        }

        @Test
        @DisplayName("valueOf返回正确值")
        void testValueOf() {
            assertThat(JsonCreator.Mode.valueOf("DEFAULT")).isEqualTo(JsonCreator.Mode.DEFAULT);
            assertThat(JsonCreator.Mode.valueOf("DELEGATING")).isEqualTo(JsonCreator.Mode.DELEGATING);
            assertThat(JsonCreator.Mode.valueOf("PROPERTIES")).isEqualTo(JsonCreator.Mode.PROPERTIES);
            assertThat(JsonCreator.Mode.valueOf("DISABLED")).isEqualTo(JsonCreator.Mode.DISABLED);
        }

        @Test
        @DisplayName("无效Mode名抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> JsonCreator.Mode.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Mode名称正确")
        void testModeNames() {
            assertThat(JsonCreator.Mode.DEFAULT.name()).isEqualTo("DEFAULT");
            assertThat(JsonCreator.Mode.DELEGATING.name()).isEqualTo("DELEGATING");
            assertThat(JsonCreator.Mode.PROPERTIES.name()).isEqualTo("PROPERTIES");
            assertThat(JsonCreator.Mode.DISABLED.name()).isEqualTo("DISABLED");
        }

        @Test
        @DisplayName("Mode序号正确")
        void testModeOrdinals() {
            assertThat(JsonCreator.Mode.DEFAULT.ordinal()).isEqualTo(0);
            assertThat(JsonCreator.Mode.DELEGATING.ordinal()).isEqualTo(1);
            assertThat(JsonCreator.Mode.PROPERTIES.ordinal()).isEqualTo(2);
            assertThat(JsonCreator.Mode.DISABLED.ordinal()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("JsonCreator 默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认mode为DEFAULT")
        void testDefaultMode() throws NoSuchMethodException {
            Constructor<?> ctor = DefaultCreatorClass.class.getDeclaredConstructor(String.class);
            JsonCreator annotation = ctor.getAnnotation(JsonCreator.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.mode()).isEqualTo(JsonCreator.Mode.DEFAULT);
        }
    }

    @Nested
    @DisplayName("JsonCreator 构造函数注解测试")
    class ConstructorAnnotationTests {

        @Test
        @DisplayName("注解可用于构造函数")
        void testAnnotationOnConstructor() throws NoSuchMethodException {
            Constructor<?> ctor = PropertiesCreatorClass.class
                .getDeclaredConstructor(String.class, int.class);
            JsonCreator annotation = ctor.getAnnotation(JsonCreator.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.mode()).isEqualTo(JsonCreator.Mode.PROPERTIES);
        }

        @Test
        @DisplayName("DELEGATING模式构造函数")
        void testDelegatingConstructor() throws NoSuchMethodException {
            Constructor<?> ctor = DelegatingCreatorClass.class
                .getDeclaredConstructor(String.class);
            JsonCreator annotation = ctor.getAnnotation(JsonCreator.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.mode()).isEqualTo(JsonCreator.Mode.DELEGATING);
        }
    }

    @Nested
    @DisplayName("JsonCreator 工厂方法注解测试")
    class FactoryMethodAnnotationTests {

        @Test
        @DisplayName("注解可用于静态工厂方法")
        void testAnnotationOnFactoryMethod() throws NoSuchMethodException {
            Method method = FactoryCreatorClass.class
                .getDeclaredMethod("create", String.class, int.class);
            JsonCreator annotation = method.getAnnotation(JsonCreator.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.mode()).isEqualTo(JsonCreator.Mode.PROPERTIES);
        }

        @Test
        @DisplayName("DELEGATING工厂方法")
        void testDelegatingFactoryMethod() throws NoSuchMethodException {
            Method method = FactoryCreatorClass.class
                .getDeclaredMethod("fromString", String.class);
            JsonCreator annotation = method.getAnnotation(JsonCreator.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.mode()).isEqualTo(JsonCreator.Mode.DELEGATING);
        }

        @Test
        @DisplayName("DISABLED工厂方法")
        void testDisabledFactoryMethod() throws NoSuchMethodException {
            Method method = FactoryCreatorClass.class
                .getDeclaredMethod("disabled", String.class);
            JsonCreator annotation = method.getAnnotation(JsonCreator.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.mode()).isEqualTo(JsonCreator.Mode.DISABLED);
        }
    }

    @Nested
    @DisplayName("JsonCreator 注解元数据测试")
    class CreatorMetadataTests {

        @Test
        @DisplayName("Target包含CONSTRUCTOR和METHOD")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonCreator.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.CONSTRUCTOR, ElementType.METHOD);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonCreator.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            java.lang.annotation.Documented documented =
                JsonCreator.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }
    }

    // ===== JsonValue Tests =====

    @Nested
    @DisplayName("JsonValue 默认值测试")
    class JsonValueDefaultTests {

        @Test
        @DisplayName("默认value为true")
        void testDefaultValue() throws NoSuchMethodException {
            Method method = JsonValueClass.class.getDeclaredMethod("toJson");
            JsonValue annotation = method.getAnnotation(JsonValue.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isTrue();
        }

        @Test
        @DisplayName("可设置value为false")
        void testDisabledValue() throws NoSuchMethodException {
            Method method = JsonValueDisabledClass.class.getDeclaredMethod("toJson");
            JsonValue annotation = method.getAnnotation(JsonValue.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isFalse();
        }
    }

    @Nested
    @DisplayName("JsonValue 方法注解测试")
    class JsonValueMethodTests {

        @Test
        @DisplayName("注解可用于方法")
        void testAnnotationOnMethod() throws NoSuchMethodException {
            Method method = JsonValueClass.class.getDeclaredMethod("toJson");
            JsonValue annotation = method.getAnnotation(JsonValue.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("枚举类中使用JsonValue")
        void testAnnotationOnEnumMethod() throws NoSuchMethodException {
            Method method = StatusEnum.class.getDeclaredMethod("getCode");
            JsonValue annotation = method.getAnnotation(JsonValue.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isTrue();
        }
    }

    @Nested
    @DisplayName("JsonValue 字段注解测试")
    class JsonValueFieldTests {

        @Test
        @DisplayName("注解可用于字段")
        void testAnnotationOnField() throws NoSuchFieldException {
            Field field = JsonValueFieldClass.class.getDeclaredField("rawValue");
            JsonValue annotation = field.getAnnotation(JsonValue.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isTrue();
        }
    }

    @Nested
    @DisplayName("JsonValue 注解元数据测试")
    class JsonValueMetadataTests {

        @Test
        @DisplayName("Target包含METHOD和FIELD")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonValue.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.METHOD, ElementType.FIELD);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonValue.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            java.lang.annotation.Documented documented =
                JsonValue.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }
    }

    // ===== Test helper classes =====

    static class DefaultCreatorClass {
        @JsonCreator
        DefaultCreatorClass(String value) {}
    }

    static class PropertiesCreatorClass {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        PropertiesCreatorClass(
            @JsonProperty("name") String name,
            @JsonProperty("age") int age) {}
    }

    static class DelegatingCreatorClass {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        DelegatingCreatorClass(String json) {}
    }

    static class FactoryCreatorClass {
        private final String name;
        private final int age;

        FactoryCreatorClass(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        static FactoryCreatorClass create(
            @JsonProperty("name") String name,
            @JsonProperty("age") int age) {
            return new FactoryCreatorClass(name, age);
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        static FactoryCreatorClass fromString(String value) {
            return new FactoryCreatorClass(value, 0);
        }

        @JsonCreator(mode = JsonCreator.Mode.DISABLED)
        static FactoryCreatorClass disabled(String value) {
            return new FactoryCreatorClass(value, 0);
        }
    }

    static class JsonValueClass {
        private final String data;

        JsonValueClass(String data) {
            this.data = data;
        }

        @JsonValue
        String toJson() {
            return data;
        }
    }

    static class JsonValueDisabledClass {
        @JsonValue(false)
        String toJson() {
            return "disabled";
        }
    }

    static class JsonValueFieldClass {
        @JsonValue
        final String rawValue = "raw";
    }

    enum StatusEnum {
        ACTIVE("active"),
        INACTIVE("inactive");

        private final String code;

        StatusEnum(String code) {
            this.code = code;
        }

        @JsonValue
        public String getCode() {
            return code;
        }
    }
}
