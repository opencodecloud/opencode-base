package cloud.opencode.base.json.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonIgnore 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonIgnore 注解测试")
class JsonIgnoreTest {

    @Nested
    @DisplayName("注解属性测试")
    class AnnotationAttributeTests {

        @Test
        @DisplayName("注解可用于字段")
        void testTargetField() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("ignoredField");
            JsonIgnore annotation = field.getAnnotation(JsonIgnore.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解可用于方法")
        void testTargetMethod() throws NoSuchMethodException {
            Method method = TestClass.class.getDeclaredMethod("getIgnoredValue");
            JsonIgnore annotation = method.getAnnotation(JsonIgnore.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("默认serialize为true")
        void testDefaultSerialize() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("ignoredField");
            JsonIgnore annotation = field.getAnnotation(JsonIgnore.class);

            assertThat(annotation.serialize()).isTrue();
        }

        @Test
        @DisplayName("默认deserialize为true")
        void testDefaultDeserialize() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("ignoredField");
            JsonIgnore annotation = field.getAnnotation(JsonIgnore.class);

            assertThat(annotation.deserialize()).isTrue();
        }

        @Test
        @DisplayName("仅忽略序列化")
        void testSerializeOnlyIgnore() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("serializeIgnored");
            JsonIgnore annotation = field.getAnnotation(JsonIgnore.class);

            assertThat(annotation.serialize()).isTrue();
            assertThat(annotation.deserialize()).isFalse();
        }

        @Test
        @DisplayName("仅忽略反序列化")
        void testDeserializeOnlyIgnore() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("deserializeIgnored");
            JsonIgnore annotation = field.getAnnotation(JsonIgnore.class);

            assertThat(annotation.serialize()).isFalse();
            assertThat(annotation.deserialize()).isTrue();
        }

        @Test
        @DisplayName("不忽略任何操作")
        void testIgnoreNothing() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("notIgnored");
            JsonIgnore annotation = field.getAnnotation(JsonIgnore.class);

            assertThat(annotation.serialize()).isFalse();
            assertThat(annotation.deserialize()).isFalse();
        }
    }

    @Nested
    @DisplayName("注解元数据测试")
    class AnnotationMetadataTests {

        @Test
        @DisplayName("Target包含FIELD和METHOD")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonIgnore.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value())
                .containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonIgnore.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            java.lang.annotation.Documented documented =
                JsonIgnore.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }
    }

    @Nested
    @DisplayName("使用场景测试")
    class UsageScenarioTests {

        @Test
        @DisplayName("密码字段完全忽略")
        void testPasswordIgnore() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("password");
            JsonIgnore annotation = field.getAnnotation(JsonIgnore.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.serialize()).isTrue();
            assertThat(annotation.deserialize()).isTrue();
        }

        @Test
        @DisplayName("计算字段只读")
        void testComputedFieldReadOnly() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("computedField");
            JsonIgnore annotation = field.getAnnotation(JsonIgnore.class);

            // Only ignored during deserialization (read-only for output)
            assertThat(annotation.serialize()).isFalse();
            assertThat(annotation.deserialize()).isTrue();
        }

        @Test
        @DisplayName("临时令牌只写")
        void testTempTokenWriteOnly() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("tempToken");
            JsonIgnore annotation = field.getAnnotation(JsonIgnore.class);

            // Only ignored during serialization (write-only from input)
            assertThat(annotation.serialize()).isTrue();
            assertThat(annotation.deserialize()).isFalse();
        }
    }

    // Test helper classes
    static class TestClass {
        @JsonIgnore
        String ignoredField;

        @JsonIgnore(deserialize = false)
        String serializeIgnored;

        @JsonIgnore(serialize = false)
        String deserializeIgnored;

        @JsonIgnore(serialize = false, deserialize = false)
        String notIgnored;

        @JsonIgnore
        String getIgnoredValue() {
            return "ignored";
        }
    }

    static class UserClass {
        String username;

        @JsonIgnore
        String password;

        @JsonIgnore(serialize = false)
        String computedField;

        @JsonIgnore(deserialize = false)
        String tempToken;
    }
}
