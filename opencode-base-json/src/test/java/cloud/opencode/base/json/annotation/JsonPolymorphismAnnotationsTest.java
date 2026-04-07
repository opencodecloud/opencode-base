package cloud.opencode.base.json.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * Polymorphic Annotation Integration Tests - Realistic usage scenarios
 * 多态注解集成测试 - 真实使用场景
 *
 * <p>Tests realistic annotation combinations beyond basic enum/metadata tests,
 * covering Shape hierarchies, creator patterns, include strategies, and auto-detect.</p>
 * <p>测试超越基本枚举/元数据测试的真实注解组合，涵盖 Shape 层次结构、
 * 创建器模式、包含策略和自动检测。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("多态注解集成测试")
class JsonPolymorphismAnnotationsTest {

    // ==================== 1. Annotation Combination Tests ====================

    @Nested
    @DisplayName("注解组合测试 - Shape hierarchy")
    class AnnotationCombinationTests {

        @Test
        @DisplayName("Shape base class has both @JsonTypeInfo and @JsonSubTypes")
        void testShapeHasBothAnnotations() {
            JsonTypeInfo typeInfo = Shape.class.getAnnotation(JsonTypeInfo.class);
            JsonSubTypes subTypes = Shape.class.getAnnotation(JsonSubTypes.class);

            assertThat(typeInfo).isNotNull();
            assertThat(subTypes).isNotNull();
        }

        @Test
        @DisplayName("@JsonTypeInfo uses NAME id and PROPERTY inclusion with custom property")
        void testShapeTypeInfoValues() {
            JsonTypeInfo typeInfo = Shape.class.getAnnotation(JsonTypeInfo.class);

            assertThat(typeInfo.id()).isEqualTo(JsonTypeInfo.Id.NAME);
            assertThat(typeInfo.include()).isEqualTo(JsonTypeInfo.As.PROPERTY);
            assertThat(typeInfo.property()).isEqualTo("type");
        }

        @Test
        @DisplayName("@JsonTypeInfo defaults: defaultImpl=Void.class, visible=false")
        void testShapeTypeInfoDefaults() {
            JsonTypeInfo typeInfo = Shape.class.getAnnotation(JsonTypeInfo.class);

            // 验证未显式设置的属性使用默认值
            assertThat(typeInfo.defaultImpl()).isEqualTo(Void.class);
            assertThat(typeInfo.visible()).isFalse();
        }

        @Test
        @DisplayName("@JsonSubTypes declares exactly 3 subtypes: circle, rectangle, triangle")
        void testShapeSubTypesCount() {
            JsonSubTypes subTypes = Shape.class.getAnnotation(JsonSubTypes.class);
            JsonSubTypes.Type[] types = subTypes.value();

            assertThat(types).hasSize(3);
        }

        @Test
        @DisplayName("@JsonSubTypes.Type maps correct classes")
        void testShapeSubTypeClasses() {
            JsonSubTypes subTypes = Shape.class.getAnnotation(JsonSubTypes.class);
            JsonSubTypes.Type[] types = subTypes.value();

            assertThat(types[0].value()).isEqualTo(Circle.class);
            assertThat(types[1].value()).isEqualTo(Rectangle.class);
            assertThat(types[2].value()).isEqualTo(Triangle.class);
        }

        @Test
        @DisplayName("@JsonSubTypes.Type primary names are correct")
        void testShapeSubTypeNames() {
            JsonSubTypes subTypes = Shape.class.getAnnotation(JsonSubTypes.class);
            JsonSubTypes.Type[] types = subTypes.value();

            assertThat(types[0].name()).isEqualTo("circle");
            assertThat(types[1].name()).isEqualTo("rect");
            assertThat(types[2].name()).isEqualTo("triangle");
        }

        @Test
        @DisplayName("Rectangle has aliases via names() array")
        void testRectangleAliases() {
            JsonSubTypes subTypes = Shape.class.getAnnotation(JsonSubTypes.class);
            JsonSubTypes.Type rectType = subTypes.value()[1];

            // rect has aliases: "rectangle" and "box"
            assertThat(rectType.names()).containsExactly("rectangle", "box");
        }

        @Test
        @DisplayName("Circle and Triangle have no aliases")
        void testNoAliasesForCircleAndTriangle() {
            JsonSubTypes subTypes = Shape.class.getAnnotation(JsonSubTypes.class);

            assertThat(subTypes.value()[0].names()).isEmpty();
            assertThat(subTypes.value()[2].names()).isEmpty();
        }

        @Test
        @DisplayName("Each subtype class has @JsonTypeName matching @JsonSubTypes.Type name")
        void testSubTypeNamesMatchTypeName() {
            JsonSubTypes subTypes = Shape.class.getAnnotation(JsonSubTypes.class);

            for (JsonSubTypes.Type type : subTypes.value()) {
                JsonTypeName typeName = type.value().getAnnotation(JsonTypeName.class);
                assertThat(typeName)
                    .as("@JsonTypeName should be present on %s", type.value().getSimpleName())
                    .isNotNull();
                assertThat(typeName.value())
                    .as("@JsonTypeName value should match @JsonSubTypes.Type name for %s",
                        type.value().getSimpleName())
                    .isEqualTo(type.name());
            }
        }

        @Test
        @DisplayName("All annotations are retained at runtime via reflection")
        void testAnnotationsRetainedAtRuntime() {
            // 验证所有注解在运行时可通过反射获取
            Annotation[] shapeAnnotations = Shape.class.getAnnotations();
            assertThat(shapeAnnotations)
                .extracting(Annotation::annotationType)
                .contains(JsonTypeInfo.class, JsonSubTypes.class);

            Annotation[] circleAnnotations = Circle.class.getAnnotations();
            assertThat(circleAnnotations)
                .extracting(Annotation::annotationType)
                .contains(JsonTypeName.class);
        }

        @Test
        @DisplayName("@JsonSubTypes.Type inner annotation is an annotation type")
        void testSubTypesTypeIsAnnotation() {
            assertThat(JsonSubTypes.Type.class.isAnnotation()).isTrue();
            assertThat(JsonSubTypes.Type.class.getEnclosingClass()).isEqualTo(JsonSubTypes.class);
        }
    }

    // ==================== 2. Wrapper Style Tests ====================

    @Nested
    @DisplayName("包含策略测试 - As wrapper styles")
    class WrapperStyleTests {

        @Test
        @DisplayName("WRAPPER_OBJECT style is retained at runtime")
        void testWrapperObjectStyle() {
            JsonTypeInfo ann = WrapperObjectEvent.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.include()).isEqualTo(JsonTypeInfo.As.WRAPPER_OBJECT);
            assertThat(ann.id()).isEqualTo(JsonTypeInfo.Id.NAME);
        }

        @Test
        @DisplayName("WRAPPER_ARRAY style is retained at runtime")
        void testWrapperArrayStyle() {
            JsonTypeInfo ann = WrapperArrayEvent.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.include()).isEqualTo(JsonTypeInfo.As.WRAPPER_ARRAY);
            assertThat(ann.id()).isEqualTo(JsonTypeInfo.Id.NAME);
        }

        @Test
        @DisplayName("EXISTING_PROPERTY style is retained at runtime")
        void testExistingPropertyStyle() {
            JsonTypeInfo ann = ExistingPropertyEvent.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.include()).isEqualTo(JsonTypeInfo.As.EXISTING_PROPERTY);
            assertThat(ann.property()).isEqualTo("eventType");
        }

        @Test
        @DisplayName("EXTERNAL_PROPERTY style is retained at runtime")
        void testExternalPropertyStyle() {
            JsonTypeInfo ann = ExternalPropertyEvent.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.include()).isEqualTo(JsonTypeInfo.As.EXTERNAL_PROPERTY);
            assertThat(ann.property()).isEqualTo("ext_type");
        }

        @Test
        @DisplayName("PROPERTY style (default) is retained at runtime")
        void testPropertyStyle() {
            JsonTypeInfo ann = Shape.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.include()).isEqualTo(JsonTypeInfo.As.PROPERTY);
        }

        @Test
        @DisplayName("All five As strategies can be applied to different classes simultaneously")
        void testAllFiveStrategiesCoexist() {
            // 验证五种策略可以同时应用于不同的类
            assertThat(Shape.class.getAnnotation(JsonTypeInfo.class).include())
                .isEqualTo(JsonTypeInfo.As.PROPERTY);
            assertThat(WrapperObjectEvent.class.getAnnotation(JsonTypeInfo.class).include())
                .isEqualTo(JsonTypeInfo.As.WRAPPER_OBJECT);
            assertThat(WrapperArrayEvent.class.getAnnotation(JsonTypeInfo.class).include())
                .isEqualTo(JsonTypeInfo.As.WRAPPER_ARRAY);
            assertThat(ExistingPropertyEvent.class.getAnnotation(JsonTypeInfo.class).include())
                .isEqualTo(JsonTypeInfo.As.EXISTING_PROPERTY);
            assertThat(ExternalPropertyEvent.class.getAnnotation(JsonTypeInfo.class).include())
                .isEqualTo(JsonTypeInfo.As.EXTERNAL_PROPERTY);
        }
    }

    // ==================== 3. Id Strategy Tests ====================

    @Nested
    @DisplayName("类型标识策略测试 - Id strategies")
    class IdStrategyTests {

        @Test
        @DisplayName("CLASS id strategy with custom defaultImpl")
        void testClassIdStrategy() {
            JsonTypeInfo ann = ClassIdVehicle.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.id()).isEqualTo(JsonTypeInfo.Id.CLASS);
            assertThat(ann.defaultImpl()).isEqualTo(DefaultVehicle.class);
        }

        @Test
        @DisplayName("MINIMAL_CLASS id strategy")
        void testMinimalClassIdStrategy() {
            JsonTypeInfo ann = MinimalClassVehicle.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.id()).isEqualTo(JsonTypeInfo.Id.MINIMAL_CLASS);
        }

        @Test
        @DisplayName("NAME id strategy (most common)")
        void testNameIdStrategy() {
            JsonTypeInfo ann = Shape.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.id()).isEqualTo(JsonTypeInfo.Id.NAME);
        }

        @Test
        @DisplayName("CUSTOM id strategy with visible=true")
        void testCustomIdStrategy() {
            JsonTypeInfo ann = CustomIdVehicle.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.id()).isEqualTo(JsonTypeInfo.Id.CUSTOM);
            assertThat(ann.visible()).isTrue();
        }

        @Test
        @DisplayName("defaultImpl defaults to Void.class when not specified")
        void testDefaultImplDefault() {
            JsonTypeInfo ann = MinimalClassVehicle.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann.defaultImpl()).isEqualTo(Void.class);
        }

        @Test
        @DisplayName("visible defaults to false when not specified")
        void testVisibleDefault() {
            JsonTypeInfo ann = MinimalClassVehicle.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann.visible()).isFalse();
        }

        @Test
        @DisplayName("visible=true is correctly retained")
        void testVisibleTrue() {
            JsonTypeInfo ann = CustomIdVehicle.class.getAnnotation(JsonTypeInfo.class);

            assertThat(ann.visible()).isTrue();
        }
    }

    // ==================== 4. Creator and Value Tests ====================

    @Nested
    @DisplayName("创建器和值注解测试 - @JsonCreator, @JsonProperty, @JsonValue")
    class CreatorAndValueTests {

        @Test
        @DisplayName("Immutable class constructor has @JsonCreator with PROPERTIES mode")
        void testImmutableClassCreator() throws NoSuchMethodException {
            Constructor<?> ctor = ImmutablePoint.class.getDeclaredConstructor(double.class, double.class);
            JsonCreator creator = ctor.getAnnotation(JsonCreator.class);

            assertThat(creator).isNotNull();
            assertThat(creator.mode()).isEqualTo(JsonCreator.Mode.PROPERTIES);
        }

        @Test
        @DisplayName("Constructor parameters have @JsonProperty annotations")
        void testConstructorParamsHaveJsonProperty() throws NoSuchMethodException {
            Constructor<?> ctor = ImmutablePoint.class.getDeclaredConstructor(double.class, double.class);
            Annotation[][] paramAnnotations = ctor.getParameterAnnotations();

            // 第一个参数: @JsonProperty("x")
            assertThat(paramAnnotations[0]).hasSize(1);
            assertThat(paramAnnotations[0][0]).isInstanceOf(JsonProperty.class);
            assertThat(((JsonProperty) paramAnnotations[0][0]).value()).isEqualTo("x");

            // 第二个参数: @JsonProperty("y")
            assertThat(paramAnnotations[1]).hasSize(1);
            assertThat(paramAnnotations[1][0]).isInstanceOf(JsonProperty.class);
            assertThat(((JsonProperty) paramAnnotations[1][0]).value()).isEqualTo("y");
        }

        @Test
        @DisplayName("Delegating creator on factory method")
        void testDelegatingCreator() throws NoSuchMethodException {
            Method factory = DelegatingWrapper.class.getDeclaredMethod("fromString", String.class);
            JsonCreator creator = factory.getAnnotation(JsonCreator.class);

            assertThat(creator).isNotNull();
            assertThat(creator.mode()).isEqualTo(JsonCreator.Mode.DELEGATING);
        }

        @Test
        @DisplayName("@JsonCreator default mode is DEFAULT")
        void testDefaultCreatorMode() throws NoSuchMethodException {
            Constructor<?> ctor = DefaultModeBean.class.getDeclaredConstructor(String.class);
            JsonCreator creator = ctor.getAnnotation(JsonCreator.class);

            assertThat(creator).isNotNull();
            assertThat(creator.mode()).isEqualTo(JsonCreator.Mode.DEFAULT);
        }

        @Test
        @DisplayName("Enum has @JsonValue on method")
        void testEnumJsonValue() throws NoSuchMethodException {
            Method method = Status.class.getDeclaredMethod("getCode");
            JsonValue jsonValue = method.getAnnotation(JsonValue.class);

            assertThat(jsonValue).isNotNull();
            assertThat(jsonValue.value()).isTrue();
        }

        @Test
        @DisplayName("@JsonValue(false) disables the annotation")
        void testJsonValueDisabled() throws NoSuchMethodException {
            Method method = DisabledValueBean.class.getDeclaredMethod("getValue");
            JsonValue jsonValue = method.getAnnotation(JsonValue.class);

            assertThat(jsonValue).isNotNull();
            assertThat(jsonValue.value()).isFalse();
        }

        @Test
        @DisplayName("@JsonValue on field is allowed")
        void testJsonValueOnField() throws NoSuchFieldException {
            Field field = FieldValueBean.class.getDeclaredField("rawValue");
            JsonValue jsonValue = field.getAnnotation(JsonValue.class);

            assertThat(jsonValue).isNotNull();
            assertThat(jsonValue.value()).isTrue();
        }

        @Test
        @DisplayName("All JsonCreator.Mode enum values exist")
        void testAllCreatorModes() {
            assertThat(JsonCreator.Mode.values())
                .containsExactlyInAnyOrder(
                    JsonCreator.Mode.DEFAULT,
                    JsonCreator.Mode.DELEGATING,
                    JsonCreator.Mode.PROPERTIES,
                    JsonCreator.Mode.DISABLED
                );
        }
    }

    // ==================== 5. Include and Order Tests ====================

    @Nested
    @DisplayName("包含和排序注解测试 - @JsonPropertyOrder, @JsonInclude")
    class IncludeAndOrderTests {

        @Test
        @DisplayName("@JsonPropertyOrder specifies explicit order")
        void testPropertyOrderValue() {
            JsonPropertyOrder order = OrderedBean.class.getAnnotation(JsonPropertyOrder.class);

            assertThat(order).isNotNull();
            assertThat(order.value()).containsExactly("z", "a", "m");
        }

        @Test
        @DisplayName("@JsonPropertyOrder alphabetic=true for remaining properties")
        void testPropertyOrderAlphabetic() {
            JsonPropertyOrder order = OrderedBean.class.getAnnotation(JsonPropertyOrder.class);

            assertThat(order.alphabetic()).isTrue();
        }

        @Test
        @DisplayName("@JsonPropertyOrder defaults: empty value, alphabetic=false")
        void testPropertyOrderDefaults() {
            JsonPropertyOrder order = DefaultOrderBean.class.getAnnotation(JsonPropertyOrder.class);

            assertThat(order).isNotNull();
            assertThat(order.value()).isEmpty();
            assertThat(order.alphabetic()).isFalse();
        }

        @Test
        @DisplayName("Class-level @JsonInclude(NON_NULL)")
        void testClassLevelInclude() {
            JsonInclude include = IncludeBean.class.getAnnotation(JsonInclude.class);

            assertThat(include).isNotNull();
            assertThat(include.value()).isEqualTo(JsonInclude.Include.NON_NULL);
        }

        @Test
        @DisplayName("Field-level @JsonInclude(NON_EMPTY) overrides class-level")
        void testFieldLevelInclude() throws NoSuchFieldException {
            Field field = IncludeBean.class.getDeclaredField("tags");
            JsonInclude include = field.getAnnotation(JsonInclude.class);

            assertThat(include).isNotNull();
            assertThat(include.value()).isEqualTo(JsonInclude.Include.NON_EMPTY);
        }

        @Test
        @DisplayName("Field without @JsonInclude inherits class-level setting")
        void testFieldWithoutInclude() throws NoSuchFieldException {
            Field field = IncludeBean.class.getDeclaredField("name");
            JsonInclude fieldInclude = field.getAnnotation(JsonInclude.class);
            JsonInclude classInclude = IncludeBean.class.getAnnotation(JsonInclude.class);

            // 字段上没有 @JsonInclude，应继承类级别设置
            assertThat(fieldInclude).isNull();
            assertThat(classInclude).isNotNull();
            assertThat(classInclude.value()).isEqualTo(JsonInclude.Include.NON_NULL);
        }

        @Test
        @DisplayName("@JsonInclude content attribute for map/collection content filtering")
        void testIncludeContentAttribute() throws NoSuchFieldException {
            Field field = IncludeBean.class.getDeclaredField("metadata");
            JsonInclude include = field.getAnnotation(JsonInclude.class);

            assertThat(include).isNotNull();
            assertThat(include.value()).isEqualTo(JsonInclude.Include.NON_EMPTY);
            assertThat(include.content()).isEqualTo(JsonInclude.Include.NON_NULL);
        }

        @Test
        @DisplayName("All JsonInclude.Include enum values exist")
        void testAllIncludeValues() {
            assertThat(JsonInclude.Include.values())
                .containsExactlyInAnyOrder(
                    JsonInclude.Include.ALWAYS,
                    JsonInclude.Include.NON_NULL,
                    JsonInclude.Include.NON_ABSENT,
                    JsonInclude.Include.NON_EMPTY,
                    JsonInclude.Include.NON_DEFAULT,
                    JsonInclude.Include.CUSTOM,
                    JsonInclude.Include.USE_DEFAULTS
                );
        }

        @Test
        @DisplayName("@JsonInclude default content is ALWAYS")
        void testIncludeDefaultContent() {
            JsonInclude include = IncludeBean.class.getAnnotation(JsonInclude.class);

            // 类级别注解没有设置 content，默认应为 ALWAYS
            assertThat(include.content()).isEqualTo(JsonInclude.Include.ALWAYS);
        }
    }

    // ==================== 6. Auto-Detect Tests ====================

    @Nested
    @DisplayName("自动检测注解测试 - @JsonAutoDetect")
    class AutoDetectTests {

        @Test
        @DisplayName("fieldVisibility=ANY, getterVisibility=NONE")
        void testCustomAutoDetect() {
            JsonAutoDetect ann = FieldOnlyBean.class.getAnnotation(JsonAutoDetect.class);

            assertThat(ann).isNotNull();
            assertThat(ann.fieldVisibility()).isEqualTo(JsonAutoDetect.Visibility.ANY);
            assertThat(ann.getterVisibility()).isEqualTo(JsonAutoDetect.Visibility.NONE);
        }

        @Test
        @DisplayName("Unspecified visibilities default to DEFAULT")
        void testAutoDetectDefaults() {
            JsonAutoDetect ann = FieldOnlyBean.class.getAnnotation(JsonAutoDetect.class);

            // 未显式设置的属性应使用默认值
            assertThat(ann.setterVisibility()).isEqualTo(JsonAutoDetect.Visibility.DEFAULT);
            assertThat(ann.isGetterVisibility()).isEqualTo(JsonAutoDetect.Visibility.DEFAULT);
            assertThat(ann.creatorVisibility()).isEqualTo(JsonAutoDetect.Visibility.DEFAULT);
        }

        @Test
        @DisplayName("All Visibility enum values exist")
        void testAllVisibilityValues() {
            assertThat(JsonAutoDetect.Visibility.values())
                .containsExactlyInAnyOrder(
                    JsonAutoDetect.Visibility.ANY,
                    JsonAutoDetect.Visibility.NON_PRIVATE,
                    JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC,
                    JsonAutoDetect.Visibility.PUBLIC_ONLY,
                    JsonAutoDetect.Visibility.NONE,
                    JsonAutoDetect.Visibility.DEFAULT
                );
        }

        @Test
        @DisplayName("All five visibility properties can be set independently")
        void testFullyCustomAutoDetect() {
            JsonAutoDetect ann = FullyCustomDetectBean.class.getAnnotation(JsonAutoDetect.class);

            assertThat(ann).isNotNull();
            assertThat(ann.fieldVisibility()).isEqualTo(JsonAutoDetect.Visibility.PUBLIC_ONLY);
            assertThat(ann.getterVisibility()).isEqualTo(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
            assertThat(ann.setterVisibility()).isEqualTo(JsonAutoDetect.Visibility.NON_PRIVATE);
            assertThat(ann.isGetterVisibility()).isEqualTo(JsonAutoDetect.Visibility.ANY);
            assertThat(ann.creatorVisibility()).isEqualTo(JsonAutoDetect.Visibility.NONE);
        }

        @Test
        @DisplayName("@JsonAutoDetect is retained at runtime")
        void testAutoDetectRetention() {
            Annotation[] annotations = FieldOnlyBean.class.getAnnotations();
            assertThat(annotations)
                .extracting(Annotation::annotationType)
                .contains(JsonAutoDetect.class);
        }
    }

    // ==================== Test Helper Classes ====================

    // --- Shape hierarchy (Section 1) ---

    @JsonTypeInfo(id = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Circle.class, name = "circle"),
        @JsonSubTypes.Type(value = Rectangle.class, name = "rect", names = {"rectangle", "box"}),
        @JsonSubTypes.Type(value = Triangle.class, name = "triangle")
    })
    static abstract class Shape {
        abstract double area();
    }

    @JsonTypeName("circle")
    static class Circle extends Shape {
        double radius;

        @Override
        double area() {
            return Math.PI * radius * radius;
        }
    }

    @JsonTypeName("rect")
    static class Rectangle extends Shape {
        double width;
        double height;

        @Override
        double area() {
            return width * height;
        }
    }

    @JsonTypeName("triangle")
    static class Triangle extends Shape {
        double base;
        double height;

        @Override
        double area() {
            return 0.5 * base * height;
        }
    }

    // --- Wrapper style classes (Section 2) ---

    @JsonTypeInfo(id = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    static class WrapperObjectEvent {}

    @JsonTypeInfo(id = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_ARRAY)
    static class WrapperArrayEvent {}

    @JsonTypeInfo(id = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "eventType")
    static class ExistingPropertyEvent {
        @SuppressWarnings("unused")
        String eventType;
    }

    @JsonTypeInfo(id = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "ext_type")
    static class ExternalPropertyEvent {}

    // --- Id strategy classes (Section 3) ---

    @JsonTypeInfo(id = JsonTypeInfo.Id.CLASS, defaultImpl = DefaultVehicle.class)
    static class ClassIdVehicle {}

    @JsonTypeInfo(id = JsonTypeInfo.Id.MINIMAL_CLASS)
    static class MinimalClassVehicle {}

    @JsonTypeInfo(id = JsonTypeInfo.Id.CUSTOM, visible = true)
    static class CustomIdVehicle {}

    static class DefaultVehicle extends ClassIdVehicle {}

    // --- Creator and Value classes (Section 4) ---

    static class ImmutablePoint {
        private final double x;
        private final double y;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        ImmutablePoint(@JsonProperty("x") double x, @JsonProperty("y") double y) {
            this.x = x;
            this.y = y;
        }

        double getX() { return x; }
        double getY() { return y; }
    }

    static class DelegatingWrapper {
        private final String value;

        private DelegatingWrapper(String value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        static DelegatingWrapper fromString(String value) {
            return new DelegatingWrapper(value);
        }

        String getValue() { return value; }
    }

    static class DefaultModeBean {
        private final String name;

        @JsonCreator
        DefaultModeBean(String name) {
            this.name = name;
        }
    }

    enum Status {
        ACTIVE("active"),
        INACTIVE("inactive");

        private final String code;

        Status(String code) {
            this.code = code;
        }

        @JsonValue
        public String getCode() {
            return code;
        }
    }

    static class DisabledValueBean {
        @JsonValue(false)
        public String getValue() {
            return "disabled";
        }
    }

    static class FieldValueBean {
        @JsonValue
        @SuppressWarnings("unused")
        private final String rawValue = "raw";
    }

    // --- Include and Order classes (Section 5) ---

    @JsonPropertyOrder(value = {"z", "a", "m"}, alphabetic = true)
    static class OrderedBean {
        @SuppressWarnings("unused") String z;
        @SuppressWarnings("unused") String a;
        @SuppressWarnings("unused") String m;
        @SuppressWarnings("unused") String x;
    }

    @JsonPropertyOrder
    static class DefaultOrderBean {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class IncludeBean {
        @SuppressWarnings("unused")
        String name;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @SuppressWarnings("unused")
        java.util.List<String> tags;

        @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
        @SuppressWarnings("unused")
        java.util.Map<String, String> metadata;
    }

    // --- Auto-Detect classes (Section 6) ---

    @JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE
    )
    static class FieldOnlyBean {
        @SuppressWarnings("unused")
        private String secret;
    }

    @JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        getterVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC,
        setterVisibility = JsonAutoDetect.Visibility.NON_PRIVATE,
        isGetterVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
    )
    static class FullyCustomDetectBean {}
}
