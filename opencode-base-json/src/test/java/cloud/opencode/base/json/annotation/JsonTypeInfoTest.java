package cloud.opencode.base.json.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonTypeInfo / JsonSubTypes / JsonTypeName 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("多态类型注解测试")
class JsonTypeInfoTest {

    // ==================== JsonTypeInfo Tests ====================

    @Nested
    @DisplayName("JsonTypeInfo - Id枚举测试")
    class IdEnumTests {

        @Test
        @DisplayName("所有Id值存在")
        void testAllIdValues() {
            assertThat(JsonTypeInfo.Id.values())
                .containsExactlyInAnyOrder(
                    JsonTypeInfo.Id.CLASS,
                    JsonTypeInfo.Id.MINIMAL_CLASS,
                    JsonTypeInfo.Id.NAME,
                    JsonTypeInfo.Id.CUSTOM
                );
        }

        @Test
        @DisplayName("valueOf返回正确值")
        void testValueOf() {
            assertThat(JsonTypeInfo.Id.valueOf("CLASS")).isEqualTo(JsonTypeInfo.Id.CLASS);
            assertThat(JsonTypeInfo.Id.valueOf("MINIMAL_CLASS")).isEqualTo(JsonTypeInfo.Id.MINIMAL_CLASS);
            assertThat(JsonTypeInfo.Id.valueOf("NAME")).isEqualTo(JsonTypeInfo.Id.NAME);
            assertThat(JsonTypeInfo.Id.valueOf("CUSTOM")).isEqualTo(JsonTypeInfo.Id.CUSTOM);
        }

        @Test
        @DisplayName("无效Id名抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> JsonTypeInfo.Id.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Id名称正确")
        void testIdNames() {
            assertThat(JsonTypeInfo.Id.CLASS.name()).isEqualTo("CLASS");
            assertThat(JsonTypeInfo.Id.MINIMAL_CLASS.name()).isEqualTo("MINIMAL_CLASS");
            assertThat(JsonTypeInfo.Id.NAME.name()).isEqualTo("NAME");
            assertThat(JsonTypeInfo.Id.CUSTOM.name()).isEqualTo("CUSTOM");
        }

        @Test
        @DisplayName("Id序号正确")
        void testIdOrdinals() {
            assertThat(JsonTypeInfo.Id.CLASS.ordinal()).isEqualTo(0);
            assertThat(JsonTypeInfo.Id.MINIMAL_CLASS.ordinal()).isEqualTo(1);
            assertThat(JsonTypeInfo.Id.NAME.ordinal()).isEqualTo(2);
            assertThat(JsonTypeInfo.Id.CUSTOM.ordinal()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("JsonTypeInfo - As枚举测试")
    class AsEnumTests {

        @Test
        @DisplayName("所有As值存在")
        void testAllAsValues() {
            assertThat(JsonTypeInfo.As.values())
                .containsExactlyInAnyOrder(
                    JsonTypeInfo.As.PROPERTY,
                    JsonTypeInfo.As.WRAPPER_OBJECT,
                    JsonTypeInfo.As.WRAPPER_ARRAY,
                    JsonTypeInfo.As.EXISTING_PROPERTY,
                    JsonTypeInfo.As.EXTERNAL_PROPERTY
                );
        }

        @Test
        @DisplayName("valueOf返回正确值")
        void testValueOf() {
            assertThat(JsonTypeInfo.As.valueOf("PROPERTY")).isEqualTo(JsonTypeInfo.As.PROPERTY);
            assertThat(JsonTypeInfo.As.valueOf("WRAPPER_OBJECT")).isEqualTo(JsonTypeInfo.As.WRAPPER_OBJECT);
            assertThat(JsonTypeInfo.As.valueOf("WRAPPER_ARRAY")).isEqualTo(JsonTypeInfo.As.WRAPPER_ARRAY);
            assertThat(JsonTypeInfo.As.valueOf("EXISTING_PROPERTY")).isEqualTo(JsonTypeInfo.As.EXISTING_PROPERTY);
            assertThat(JsonTypeInfo.As.valueOf("EXTERNAL_PROPERTY")).isEqualTo(JsonTypeInfo.As.EXTERNAL_PROPERTY);
        }

        @Test
        @DisplayName("无效As名抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> JsonTypeInfo.As.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("As名称正确")
        void testAsNames() {
            assertThat(JsonTypeInfo.As.PROPERTY.name()).isEqualTo("PROPERTY");
            assertThat(JsonTypeInfo.As.WRAPPER_OBJECT.name()).isEqualTo("WRAPPER_OBJECT");
            assertThat(JsonTypeInfo.As.WRAPPER_ARRAY.name()).isEqualTo("WRAPPER_ARRAY");
            assertThat(JsonTypeInfo.As.EXISTING_PROPERTY.name()).isEqualTo("EXISTING_PROPERTY");
            assertThat(JsonTypeInfo.As.EXTERNAL_PROPERTY.name()).isEqualTo("EXTERNAL_PROPERTY");
        }

        @Test
        @DisplayName("As序号正确")
        void testAsOrdinals() {
            assertThat(JsonTypeInfo.As.PROPERTY.ordinal()).isEqualTo(0);
            assertThat(JsonTypeInfo.As.WRAPPER_OBJECT.ordinal()).isEqualTo(1);
            assertThat(JsonTypeInfo.As.WRAPPER_ARRAY.ordinal()).isEqualTo(2);
            assertThat(JsonTypeInfo.As.EXISTING_PROPERTY.ordinal()).isEqualTo(3);
            assertThat(JsonTypeInfo.As.EXTERNAL_PROPERTY.ordinal()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("JsonTypeInfo - 注解属性测试")
    class JsonTypeInfoAttributeTests {

        @Test
        @DisplayName("默认值测试")
        void testDefaultValues() {
            JsonTypeInfo annotation = DefaultAnimal.class.getAnnotation(JsonTypeInfo.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.id()).isEqualTo(JsonTypeInfo.Id.NAME);
            assertThat(annotation.include()).isEqualTo(JsonTypeInfo.As.PROPERTY);
            assertThat(annotation.property()).isEqualTo("@type");
            assertThat(annotation.defaultImpl()).isEqualTo(Void.class);
            assertThat(annotation.visible()).isFalse();
        }

        @Test
        @DisplayName("自定义属性测试")
        void testCustomValues() {
            JsonTypeInfo annotation = CustomAnimal.class.getAnnotation(JsonTypeInfo.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.id()).isEqualTo(JsonTypeInfo.Id.CLASS);
            assertThat(annotation.include()).isEqualTo(JsonTypeInfo.As.WRAPPER_OBJECT);
            assertThat(annotation.property()).isEqualTo("clazz");
            assertThat(annotation.defaultImpl()).isEqualTo(DefaultDog.class);
            assertThat(annotation.visible()).isTrue();
        }

        @Test
        @DisplayName("使用MINIMAL_CLASS标识")
        void testMinimalClassId() {
            JsonTypeInfo annotation = MinimalClassAnimal.class.getAnnotation(JsonTypeInfo.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.id()).isEqualTo(JsonTypeInfo.Id.MINIMAL_CLASS);
            assertThat(annotation.include()).isEqualTo(JsonTypeInfo.As.EXTERNAL_PROPERTY);
        }

        @Test
        @DisplayName("使用EXISTING_PROPERTY包含策略")
        void testExistingProperty() {
            JsonTypeInfo annotation = ExistingPropertyAnimal.class.getAnnotation(JsonTypeInfo.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.include()).isEqualTo(JsonTypeInfo.As.EXISTING_PROPERTY);
            assertThat(annotation.property()).isEqualTo("kind");
        }

        @Test
        @DisplayName("使用WRAPPER_ARRAY包含策略")
        void testWrapperArray() {
            JsonTypeInfo annotation = WrapperArrayAnimal.class.getAnnotation(JsonTypeInfo.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.include()).isEqualTo(JsonTypeInfo.As.WRAPPER_ARRAY);
        }
    }

    @Nested
    @DisplayName("JsonTypeInfo - 注解元数据测试")
    class JsonTypeInfoMetadataTests {

        @Test
        @DisplayName("Target为TYPE")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonTypeInfo.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonTypeInfo.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            java.lang.annotation.Documented documented =
                JsonTypeInfo.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }
    }

    // ==================== JsonSubTypes Tests ====================

    @Nested
    @DisplayName("JsonSubTypes - 注解属性测试")
    class JsonSubTypesAttributeTests {

        @Test
        @DisplayName("获取子类型列表")
        void testSubTypesValue() {
            JsonSubTypes annotation = AnnotatedAnimal.class.getAnnotation(JsonSubTypes.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).hasSize(3);
        }

        @Test
        @DisplayName("获取子类型类")
        void testSubTypeClass() {
            JsonSubTypes annotation = AnnotatedAnimal.class.getAnnotation(JsonSubTypes.class);
            JsonSubTypes.Type[] types = annotation.value();

            assertThat(types[0].value()).isEqualTo(DefaultDog.class);
            assertThat(types[1].value()).isEqualTo(DefaultCat.class);
            assertThat(types[2].value()).isEqualTo(DefaultBird.class);
        }

        @Test
        @DisplayName("获取子类型名称")
        void testSubTypeName() {
            JsonSubTypes annotation = AnnotatedAnimal.class.getAnnotation(JsonSubTypes.class);
            JsonSubTypes.Type[] types = annotation.value();

            assertThat(types[0].name()).isEqualTo("dog");
            assertThat(types[1].name()).isEqualTo("cat");
            assertThat(types[2].name()).isEqualTo("bird");
        }

        @Test
        @DisplayName("获取子类型别名")
        void testSubTypeNames() {
            JsonSubTypes annotation = AnnotatedAnimal.class.getAnnotation(JsonSubTypes.class);
            JsonSubTypes.Type[] types = annotation.value();

            assertThat(types[0].names()).isEmpty();
            assertThat(types[1].names()).containsExactly("kitty", "feline");
            assertThat(types[2].names()).isEmpty();
        }

        @Test
        @DisplayName("Type默认值测试")
        void testTypeDefaults() {
            JsonSubTypes annotation = DefaultSubTypesAnimal.class.getAnnotation(JsonSubTypes.class);
            JsonSubTypes.Type[] types = annotation.value();

            assertThat(types).hasSize(1);
            assertThat(types[0].value()).isEqualTo(DefaultDog.class);
            assertThat(types[0].name()).isEmpty();
            assertThat(types[0].names()).isEmpty();
        }
    }

    @Nested
    @DisplayName("JsonSubTypes - 注解元数据测试")
    class JsonSubTypesMetadataTests {

        @Test
        @DisplayName("Target为TYPE")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonSubTypes.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonSubTypes.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            java.lang.annotation.Documented documented =
                JsonSubTypes.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }
    }

    @Nested
    @DisplayName("JsonSubTypes.Type - 内部注解元数据测试")
    class JsonSubTypesTypeMetadataTests {

        @Test
        @DisplayName("Type的Retention为RUNTIME")
        void testTypeRetention() {
            java.lang.annotation.Retention retention =
                JsonSubTypes.Type.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Type的Documented存在")
        void testTypeDocumented() {
            java.lang.annotation.Documented documented =
                JsonSubTypes.Type.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }

        @Test
        @DisplayName("Type的Target为空（仅用于注解内部）")
        void testTypeTarget() {
            java.lang.annotation.Target target =
                JsonSubTypes.Type.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value()).isEmpty();
        }
    }

    // ==================== JsonTypeName Tests ====================

    @Nested
    @DisplayName("JsonTypeName - 注解属性测试")
    class JsonTypeNameAttributeTests {

        @Test
        @DisplayName("获取类型名称")
        void testTypeName() {
            JsonTypeName annotation = DefaultDog.class.getAnnotation(JsonTypeName.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("dog");
        }

        @Test
        @DisplayName("默认值为空字符串")
        void testDefaultValue() {
            JsonTypeName annotation = DefaultNameAnimal.class.getAnnotation(JsonTypeName.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEmpty();
        }

        @Test
        @DisplayName("中文类型名称")
        void testChineseTypeName() {
            JsonTypeName annotation = DefaultCat.class.getAnnotation(JsonTypeName.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("cat");
        }
    }

    @Nested
    @DisplayName("JsonTypeName - 注解元数据测试")
    class JsonTypeNameMetadataTests {

        @Test
        @DisplayName("Target为TYPE")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonTypeName.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonTypeName.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            java.lang.annotation.Documented documented =
                JsonTypeName.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("多态注解组合使用测试")
    class IntegrationTests {

        @Test
        @DisplayName("完整多态注解组合")
        void testFullPolymorphicSetup() {
            JsonTypeInfo typeInfo = AnnotatedAnimal.class.getAnnotation(JsonTypeInfo.class);
            JsonSubTypes subTypes = AnnotatedAnimal.class.getAnnotation(JsonSubTypes.class);

            assertThat(typeInfo).isNotNull();
            assertThat(subTypes).isNotNull();

            assertThat(typeInfo.id()).isEqualTo(JsonTypeInfo.Id.NAME);
            assertThat(typeInfo.property()).isEqualTo("type");
            assertThat(subTypes.value()).hasSize(3);
        }

        @Test
        @DisplayName("子类型的JsonTypeName与JsonSubTypes.Type一致")
        void testTypeNameConsistency() {
            JsonSubTypes subTypes = AnnotatedAnimal.class.getAnnotation(JsonSubTypes.class);
            JsonSubTypes.Type dogType = subTypes.value()[0];

            JsonTypeName dogTypeName = DefaultDog.class.getAnnotation(JsonTypeName.class);

            assertThat(dogType.name()).isEqualTo(dogTypeName.value());
        }

        @Test
        @DisplayName("所有子类型都有JsonTypeName注解")
        void testAllSubTypesHaveTypeName() {
            JsonSubTypes subTypes = AnnotatedAnimal.class.getAnnotation(JsonSubTypes.class);

            for (JsonSubTypes.Type type : subTypes.value()) {
                JsonTypeName typeName = type.value().getAnnotation(JsonTypeName.class);
                assertThat(typeName)
                    .as("Class %s should have @JsonTypeName", type.value().getSimpleName())
                    .isNotNull();
            }
        }
    }

    // ==================== Test Helper Classes ====================

    @JsonTypeInfo(id = JsonTypeInfo.Id.NAME)
    static class DefaultAnimal {}

    @JsonTypeInfo(
        id = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.WRAPPER_OBJECT,
        property = "clazz",
        defaultImpl = DefaultDog.class,
        visible = true
    )
    static class CustomAnimal {}

    @JsonTypeInfo(id = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
    static class MinimalClassAnimal {}

    @JsonTypeInfo(id = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "kind")
    static class ExistingPropertyAnimal {}

    @JsonTypeInfo(id = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_ARRAY)
    static class WrapperArrayAnimal {}

    @JsonTypeInfo(id = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = DefaultDog.class, name = "dog"),
        @JsonSubTypes.Type(value = DefaultCat.class, name = "cat", names = {"kitty", "feline"}),
        @JsonSubTypes.Type(value = DefaultBird.class, name = "bird")
    })
    static class AnnotatedAnimal {}

    @JsonSubTypes({
        @JsonSubTypes.Type(DefaultDog.class)
    })
    static class DefaultSubTypesAnimal {}

    @JsonTypeName("dog")
    static class DefaultDog extends AnnotatedAnimal {}

    @JsonTypeName("cat")
    static class DefaultCat extends AnnotatedAnimal {}

    @JsonTypeName("bird")
    static class DefaultBird extends AnnotatedAnimal {}

    @JsonTypeName
    static class DefaultNameAnimal {}
}
