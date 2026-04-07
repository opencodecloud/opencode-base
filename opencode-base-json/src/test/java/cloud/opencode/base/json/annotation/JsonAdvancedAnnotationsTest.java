package cloud.opencode.base.json.annotation;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.adapter.JsonTypeAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonSerialize / JsonDeserialize / JsonUnwrapped / JsonView 注解测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JSON 高级注解测试")
class JsonAdvancedAnnotationsTest {

    // ===================== Test Adapters =====================

    static class MoneyAdapter implements JsonTypeAdapter<BigDecimal> {
        @Override
        public Class<BigDecimal> getType() {
            return BigDecimal.class;
        }

        @Override
        public JsonNode toJson(BigDecimal value) {
            return JsonNode.of(value.toPlainString());
        }

        @Override
        public BigDecimal fromJson(JsonNode node) {
            return new BigDecimal(node.asString());
        }
    }

    static class ItemAdapter implements JsonTypeAdapter<String> {
        @Override
        public Class<String> getType() {
            return String.class;
        }

        @Override
        public JsonNode toJson(String value) {
            return JsonNode.of(value);
        }

        @Override
        public String fromJson(JsonNode node) {
            return node.asString();
        }
    }

    // ===================== View Markers =====================

    interface Views {
        interface Summary {}
        interface Detail extends Summary {}
        interface Admin extends Detail {}
    }

    // ===================== Test Classes =====================

    static class SerializeTestClass {
        @JsonSerialize(using = MoneyAdapter.class)
        BigDecimal price;

        @JsonSerialize(contentUsing = ItemAdapter.class)
        List<String> items;

        @JsonSerialize(keyUsing = MoneyAdapter.class)
        Map<String, Object> metadata;

        @JsonSerialize(as = CharSequence.class, contentAs = Number.class, keyAs = String.class)
        Object data;

        @JsonSerialize
        String defaultField;
    }

    @JsonSerialize(using = MoneyAdapter.class)
    static class SerializeOnClass {
        String value;
    }

    static class SerializeOnMethod {
        @JsonSerialize(using = MoneyAdapter.class)
        BigDecimal getPrice() {
            return BigDecimal.ZERO;
        }
    }

    static class DeserializeTestClass {
        @JsonDeserialize(using = MoneyAdapter.class)
        BigDecimal price;

        @JsonDeserialize(contentUsing = ItemAdapter.class)
        List<String> items;

        @JsonDeserialize(keyUsing = MoneyAdapter.class)
        Map<String, Object> metadata;

        @JsonDeserialize(as = String.class, contentAs = Integer.class, keyAs = Long.class, builder = StringBuilder.class)
        Object data;

        @JsonDeserialize
        String defaultField;
    }

    @JsonDeserialize(builder = StringBuilder.class)
    static class DeserializeOnClass {
        String value;
    }

    static class DeserializeOnParameter {
        DeserializeOnParameter(@JsonDeserialize(using = MoneyAdapter.class) BigDecimal price) {
        }
    }

    static class UnwrappedTestClass {
        @JsonUnwrapped
        Object defaultUnwrapped;

        @JsonUnwrapped(prefix = "ship_", suffix = "_addr")
        Object shippingAddress;

        @JsonUnwrapped(enabled = false)
        Object disabledUnwrapped;
    }

    static class UnwrappedOnMethod {
        @JsonUnwrapped(prefix = "billing_")
        Object getBillingAddress() {
            return null;
        }
    }

    static class ViewTestClass {
        @JsonView(Views.Summary.class)
        String name;

        @JsonView(Views.Detail.class)
        String email;

        @JsonView(Views.Admin.class)
        String role;

        @JsonView({Views.Summary.class, Views.Admin.class})
        String displayName;

        String noView;
    }

    @JsonView(Views.Summary.class)
    static class ViewOnClass {
        String value;
    }

    static class ViewOnMethod {
        @JsonView(Views.Detail.class)
        String getEmail() {
            return "test@example.com";
        }
    }

    // ===================== JsonSerialize Tests =====================

    @Nested
    @DisplayName("@JsonSerialize 注解测试")
    class JsonSerializeTests {

        @Nested
        @DisplayName("默认值测试")
        class DefaultValueTests {

            @Test
            @DisplayName("所有属性使用默认值")
            void testDefaultValues() throws NoSuchFieldException {
                Field field = SerializeTestClass.class.getDeclaredField("defaultField");
                JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.using()).isEqualTo(JsonTypeAdapter.None.class);
                assertThat(annotation.contentUsing()).isEqualTo(JsonTypeAdapter.None.class);
                assertThat(annotation.keyUsing()).isEqualTo(JsonTypeAdapter.None.class);
                assertThat(annotation.as()).isEqualTo(Void.class);
                assertThat(annotation.contentAs()).isEqualTo(Void.class);
                assertThat(annotation.keyAs()).isEqualTo(Void.class);
            }
        }

        @Nested
        @DisplayName("属性读取测试")
        class AttributeTests {

            @Test
            @DisplayName("using属性返回正确的适配器类")
            void testUsingAttribute() throws NoSuchFieldException {
                Field field = SerializeTestClass.class.getDeclaredField("price");
                JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);

                assertThat(annotation.using()).isEqualTo(MoneyAdapter.class);
            }

            @Test
            @DisplayName("contentUsing属性返回正确的适配器类")
            void testContentUsingAttribute() throws NoSuchFieldException {
                Field field = SerializeTestClass.class.getDeclaredField("items");
                JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);

                assertThat(annotation.contentUsing()).isEqualTo(ItemAdapter.class);
            }

            @Test
            @DisplayName("keyUsing属性返回正确的适配器类")
            void testKeyUsingAttribute() throws NoSuchFieldException {
                Field field = SerializeTestClass.class.getDeclaredField("metadata");
                JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);

                assertThat(annotation.keyUsing()).isEqualTo(MoneyAdapter.class);
            }

            @Test
            @DisplayName("as/contentAs/keyAs属性返回正确的类型")
            void testAsAttributes() throws NoSuchFieldException {
                Field field = SerializeTestClass.class.getDeclaredField("data");
                JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);

                assertThat(annotation.as()).isEqualTo(CharSequence.class);
                assertThat(annotation.contentAs()).isEqualTo(Number.class);
                assertThat(annotation.keyAs()).isEqualTo(String.class);
            }
        }

        @Nested
        @DisplayName("目标测试")
        class TargetTests {

            @Test
            @DisplayName("注解可用于类")
            void testTargetType() {
                JsonSerialize annotation = SerializeOnClass.class.getAnnotation(JsonSerialize.class);
                assertThat(annotation).isNotNull();
                assertThat(annotation.using()).isEqualTo(MoneyAdapter.class);
            }

            @Test
            @DisplayName("注解可用于方法")
            void testTargetMethod() throws NoSuchMethodException {
                Method method = SerializeOnMethod.class.getDeclaredMethod("getPrice");
                JsonSerialize annotation = method.getAnnotation(JsonSerialize.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.using()).isEqualTo(MoneyAdapter.class);
            }

            @Test
            @DisplayName("注解可用于字段")
            void testTargetField() throws NoSuchFieldException {
                Field field = SerializeTestClass.class.getDeclaredField("price");
                JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);

                assertThat(annotation).isNotNull();
            }
        }

        @Nested
        @DisplayName("注解元数据测试")
        class MetadataTests {

            @Test
            @DisplayName("Target包含TYPE、METHOD、FIELD")
            void testTarget() {
                java.lang.annotation.Target target =
                    JsonSerialize.class.getAnnotation(java.lang.annotation.Target.class);

                assertThat(target.value())
                    .containsExactlyInAnyOrder(ElementType.TYPE, ElementType.METHOD, ElementType.FIELD);
            }

            @Test
            @DisplayName("Retention为RUNTIME")
            void testRetention() {
                java.lang.annotation.Retention retention =
                    JsonSerialize.class.getAnnotation(java.lang.annotation.Retention.class);

                assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
            }

            @Test
            @DisplayName("Documented存在")
            void testDocumented() {
                assertThat(JsonSerialize.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
            }
        }
    }

    // ===================== JsonDeserialize Tests =====================

    @Nested
    @DisplayName("@JsonDeserialize 注解测试")
    class JsonDeserializeTests {

        @Nested
        @DisplayName("默认值测试")
        class DefaultValueTests {

            @Test
            @DisplayName("所有属性使用默认值")
            void testDefaultValues() throws NoSuchFieldException {
                Field field = DeserializeTestClass.class.getDeclaredField("defaultField");
                JsonDeserialize annotation = field.getAnnotation(JsonDeserialize.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.using()).isEqualTo(JsonTypeAdapter.None.class);
                assertThat(annotation.contentUsing()).isEqualTo(JsonTypeAdapter.None.class);
                assertThat(annotation.keyUsing()).isEqualTo(JsonTypeAdapter.None.class);
                assertThat(annotation.as()).isEqualTo(Void.class);
                assertThat(annotation.contentAs()).isEqualTo(Void.class);
                assertThat(annotation.keyAs()).isEqualTo(Void.class);
                assertThat(annotation.builder()).isEqualTo(Void.class);
            }
        }

        @Nested
        @DisplayName("属性读取测试")
        class AttributeTests {

            @Test
            @DisplayName("using属性返回正确的适配器类")
            void testUsingAttribute() throws NoSuchFieldException {
                Field field = DeserializeTestClass.class.getDeclaredField("price");
                JsonDeserialize annotation = field.getAnnotation(JsonDeserialize.class);

                assertThat(annotation.using()).isEqualTo(MoneyAdapter.class);
            }

            @Test
            @DisplayName("contentUsing属性返回正确的适配器类")
            void testContentUsingAttribute() throws NoSuchFieldException {
                Field field = DeserializeTestClass.class.getDeclaredField("items");
                JsonDeserialize annotation = field.getAnnotation(JsonDeserialize.class);

                assertThat(annotation.contentUsing()).isEqualTo(ItemAdapter.class);
            }

            @Test
            @DisplayName("keyUsing属性返回正确的适配器类")
            void testKeyUsingAttribute() throws NoSuchFieldException {
                Field field = DeserializeTestClass.class.getDeclaredField("metadata");
                JsonDeserialize annotation = field.getAnnotation(JsonDeserialize.class);

                assertThat(annotation.keyUsing()).isEqualTo(MoneyAdapter.class);
            }

            @Test
            @DisplayName("as/contentAs/keyAs/builder属性返回正确的类型")
            void testAllTypeAttributes() throws NoSuchFieldException {
                Field field = DeserializeTestClass.class.getDeclaredField("data");
                JsonDeserialize annotation = field.getAnnotation(JsonDeserialize.class);

                assertThat(annotation.as()).isEqualTo(String.class);
                assertThat(annotation.contentAs()).isEqualTo(Integer.class);
                assertThat(annotation.keyAs()).isEqualTo(Long.class);
                assertThat(annotation.builder()).isEqualTo(StringBuilder.class);
            }

            @Test
            @DisplayName("builder属性可在类级别指定")
            void testBuilderOnClass() {
                JsonDeserialize annotation = DeserializeOnClass.class.getAnnotation(JsonDeserialize.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.builder()).isEqualTo(StringBuilder.class);
            }
        }

        @Nested
        @DisplayName("目标测试")
        class TargetTests {

            @Test
            @DisplayName("注解可用于类")
            void testTargetType() {
                JsonDeserialize annotation = DeserializeOnClass.class.getAnnotation(JsonDeserialize.class);
                assertThat(annotation).isNotNull();
            }

            @Test
            @DisplayName("注解可用于字段")
            void testTargetField() throws NoSuchFieldException {
                Field field = DeserializeTestClass.class.getDeclaredField("price");
                JsonDeserialize annotation = field.getAnnotation(JsonDeserialize.class);

                assertThat(annotation).isNotNull();
            }

            @Test
            @DisplayName("注解可用于构造函数参数")
            void testTargetParameter() throws NoSuchMethodException {
                Parameter[] params = DeserializeOnParameter.class
                    .getDeclaredConstructor(BigDecimal.class)
                    .getParameters();
                JsonDeserialize annotation = params[0].getAnnotation(JsonDeserialize.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.using()).isEqualTo(MoneyAdapter.class);
            }
        }

        @Nested
        @DisplayName("注解元数据测试")
        class MetadataTests {

            @Test
            @DisplayName("Target包含TYPE、METHOD、FIELD、PARAMETER")
            void testTarget() {
                java.lang.annotation.Target target =
                    JsonDeserialize.class.getAnnotation(java.lang.annotation.Target.class);

                assertThat(target.value())
                    .containsExactlyInAnyOrder(
                        ElementType.TYPE, ElementType.METHOD,
                        ElementType.FIELD, ElementType.PARAMETER);
            }

            @Test
            @DisplayName("Retention为RUNTIME")
            void testRetention() {
                java.lang.annotation.Retention retention =
                    JsonDeserialize.class.getAnnotation(java.lang.annotation.Retention.class);

                assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
            }

            @Test
            @DisplayName("Documented存在")
            void testDocumented() {
                assertThat(JsonDeserialize.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
            }
        }
    }

    // ===================== JsonUnwrapped Tests =====================

    @Nested
    @DisplayName("@JsonUnwrapped 注解测试")
    class JsonUnwrappedTests {

        @Nested
        @DisplayName("默认值测试")
        class DefaultValueTests {

            @Test
            @DisplayName("默认启用且无前缀后缀")
            void testDefaultValues() throws NoSuchFieldException {
                Field field = UnwrappedTestClass.class.getDeclaredField("defaultUnwrapped");
                JsonUnwrapped annotation = field.getAnnotation(JsonUnwrapped.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.enabled()).isTrue();
                assertThat(annotation.prefix()).isEmpty();
                assertThat(annotation.suffix()).isEmpty();
            }
        }

        @Nested
        @DisplayName("属性读取测试")
        class AttributeTests {

            @Test
            @DisplayName("prefix和suffix返回正确的值")
            void testPrefixAndSuffix() throws NoSuchFieldException {
                Field field = UnwrappedTestClass.class.getDeclaredField("shippingAddress");
                JsonUnwrapped annotation = field.getAnnotation(JsonUnwrapped.class);

                assertThat(annotation.enabled()).isTrue();
                assertThat(annotation.prefix()).isEqualTo("ship_");
                assertThat(annotation.suffix()).isEqualTo("_addr");
            }

            @Test
            @DisplayName("enabled为false时禁用展开")
            void testDisabled() throws NoSuchFieldException {
                Field field = UnwrappedTestClass.class.getDeclaredField("disabledUnwrapped");
                JsonUnwrapped annotation = field.getAnnotation(JsonUnwrapped.class);

                assertThat(annotation.enabled()).isFalse();
            }
        }

        @Nested
        @DisplayName("目标测试")
        class TargetTests {

            @Test
            @DisplayName("注解可用于字段")
            void testTargetField() throws NoSuchFieldException {
                Field field = UnwrappedTestClass.class.getDeclaredField("defaultUnwrapped");
                JsonUnwrapped annotation = field.getAnnotation(JsonUnwrapped.class);

                assertThat(annotation).isNotNull();
            }

            @Test
            @DisplayName("注解可用于方法")
            void testTargetMethod() throws NoSuchMethodException {
                Method method = UnwrappedOnMethod.class.getDeclaredMethod("getBillingAddress");
                JsonUnwrapped annotation = method.getAnnotation(JsonUnwrapped.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.prefix()).isEqualTo("billing_");
            }
        }

        @Nested
        @DisplayName("注解元数据测试")
        class MetadataTests {

            @Test
            @DisplayName("Target包含FIELD、METHOD")
            void testTarget() {
                java.lang.annotation.Target target =
                    JsonUnwrapped.class.getAnnotation(java.lang.annotation.Target.class);

                assertThat(target.value())
                    .containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD);
            }

            @Test
            @DisplayName("Retention为RUNTIME")
            void testRetention() {
                java.lang.annotation.Retention retention =
                    JsonUnwrapped.class.getAnnotation(java.lang.annotation.Retention.class);

                assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
            }

            @Test
            @DisplayName("Documented存在")
            void testDocumented() {
                assertThat(JsonUnwrapped.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
            }
        }
    }

    // ===================== JsonView Tests =====================

    @Nested
    @DisplayName("@JsonView 注解测试")
    class JsonViewTests {

        @Nested
        @DisplayName("属性读取测试")
        class AttributeTests {

            @Test
            @DisplayName("单视图返回正确的视图类")
            void testSingleView() throws NoSuchFieldException {
                Field field = ViewTestClass.class.getDeclaredField("name");
                JsonView annotation = field.getAnnotation(JsonView.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).containsExactly(Views.Summary.class);
            }

            @Test
            @DisplayName("多视图返回正确的视图类数组")
            void testMultipleViews() throws NoSuchFieldException {
                Field field = ViewTestClass.class.getDeclaredField("displayName");
                JsonView annotation = field.getAnnotation(JsonView.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value())
                    .containsExactly(Views.Summary.class, Views.Admin.class);
            }

            @Test
            @DisplayName("不同字段使用不同视图")
            void testDifferentViews() throws NoSuchFieldException {
                Field nameField = ViewTestClass.class.getDeclaredField("name");
                Field emailField = ViewTestClass.class.getDeclaredField("email");
                Field roleField = ViewTestClass.class.getDeclaredField("role");

                assertThat(nameField.getAnnotation(JsonView.class).value())
                    .containsExactly(Views.Summary.class);
                assertThat(emailField.getAnnotation(JsonView.class).value())
                    .containsExactly(Views.Detail.class);
                assertThat(roleField.getAnnotation(JsonView.class).value())
                    .containsExactly(Views.Admin.class);
            }

            @Test
            @DisplayName("无JsonView注解的字段返回null")
            void testNoAnnotation() throws NoSuchFieldException {
                Field field = ViewTestClass.class.getDeclaredField("noView");
                JsonView annotation = field.getAnnotation(JsonView.class);

                assertThat(annotation).isNull();
            }
        }

        @Nested
        @DisplayName("视图继承测试")
        class ViewInheritanceTests {

            @Test
            @DisplayName("视图类支持继承关系")
            void testViewInheritance() {
                assertThat(Views.Summary.class.isAssignableFrom(Views.Detail.class)).isTrue();
                assertThat(Views.Detail.class.isAssignableFrom(Views.Admin.class)).isTrue();
                assertThat(Views.Summary.class.isAssignableFrom(Views.Admin.class)).isTrue();
            }
        }

        @Nested
        @DisplayName("目标测试")
        class TargetTests {

            @Test
            @DisplayName("注解可用于类")
            void testTargetType() {
                JsonView annotation = ViewOnClass.class.getAnnotation(JsonView.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).containsExactly(Views.Summary.class);
            }

            @Test
            @DisplayName("注解可用于字段")
            void testTargetField() throws NoSuchFieldException {
                Field field = ViewTestClass.class.getDeclaredField("name");
                JsonView annotation = field.getAnnotation(JsonView.class);

                assertThat(annotation).isNotNull();
            }

            @Test
            @DisplayName("注解可用于方法")
            void testTargetMethod() throws NoSuchMethodException {
                Method method = ViewOnMethod.class.getDeclaredMethod("getEmail");
                JsonView annotation = method.getAnnotation(JsonView.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).containsExactly(Views.Detail.class);
            }
        }

        @Nested
        @DisplayName("注解元数据测试")
        class MetadataTests {

            @Test
            @DisplayName("Target包含METHOD、FIELD、TYPE")
            void testTarget() {
                java.lang.annotation.Target target =
                    JsonView.class.getAnnotation(java.lang.annotation.Target.class);

                assertThat(target.value())
                    .containsExactlyInAnyOrder(ElementType.METHOD, ElementType.FIELD, ElementType.TYPE);
            }

            @Test
            @DisplayName("Retention为RUNTIME")
            void testRetention() {
                java.lang.annotation.Retention retention =
                    JsonView.class.getAnnotation(java.lang.annotation.Retention.class);

                assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
            }

            @Test
            @DisplayName("Documented存在")
            void testDocumented() {
                assertThat(JsonView.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
            }
        }
    }

    // ===================== JsonTypeAdapter.None Tests =====================

    @Nested
    @DisplayName("JsonTypeAdapter.None 占位符测试")
    class NoneClassTests {

        @Test
        @DisplayName("None是JsonTypeAdapter的子类型")
        void testNoneIsSubtype() {
            assertThat(JsonTypeAdapter.class.isAssignableFrom(JsonTypeAdapter.None.class)).isTrue();
        }

        @Test
        @DisplayName("None是抽象类不可实例化")
        void testNoneIsAbstract() {
            assertThat(java.lang.reflect.Modifier.isAbstract(JsonTypeAdapter.None.class.getModifiers())).isTrue();
        }
    }
}
