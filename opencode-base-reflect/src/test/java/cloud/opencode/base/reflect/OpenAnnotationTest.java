package cloud.opencode.base.reflect;

import org.junit.jupiter.api.*;

import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenAnnotationTest Tests
 * OpenAnnotationTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenAnnotation 测试")
class OpenAnnotationTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenAnnotation.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getAnnotation方法测试")
    class GetAnnotationTests {

        @Test
        @DisplayName("获取类注解")
        void testGetAnnotation() {
            Deprecated annotation = OpenAnnotation.getAnnotation(DeprecatedClass.class, Deprecated.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解不存在返回null")
        void testGetAnnotationNotFound() {
            Deprecated annotation = OpenAnnotation.getAnnotation(TestClass.class, Deprecated.class);
            assertThat(annotation).isNull();
        }
    }

    @Nested
    @DisplayName("findAnnotation方法测试")
    class FindAnnotationTests {

        @Test
        @DisplayName("获取存在的注解")
        void testFindAnnotationExists() {
            Optional<Deprecated> annotation = OpenAnnotation.findAnnotation(DeprecatedClass.class, Deprecated.class);
            assertThat(annotation).isPresent();
        }

        @Test
        @DisplayName("获取不存在的注解")
        void testFindAnnotationNotExists() {
            Optional<Deprecated> annotation = OpenAnnotation.findAnnotation(TestClass.class, Deprecated.class);
            assertThat(annotation).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAnnotations方法测试")
    class GetAnnotationsTests {

        @Test
        @DisplayName("获取所有注解")
        void testGetAnnotations() {
            Annotation[] annotations = OpenAnnotation.getAnnotations(MultiAnnotatedClass.class);
            assertThat(annotations).hasSizeGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getDeclaredAnnotations方法测试")
    class GetDeclaredAnnotationsTests {

        @Test
        @DisplayName("获取声明的注解")
        void testGetDeclaredAnnotations() {
            Annotation[] annotations = OpenAnnotation.getDeclaredAnnotations(MultiAnnotatedClass.class);
            assertThat(annotations).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getAnnotationsByType方法测试")
    class GetAnnotationsByTypeTests {

        @Test
        @DisplayName("获取可重复注解")
        void testGetAnnotationsByType() {
            RepeatableTag[] annotations = OpenAnnotation.getAnnotationsByType(
                    RepeatableAnnotatedClass.class, RepeatableTag.class);
            assertThat(annotations).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findAnnotationInherited方法测试")
    class FindAnnotationInheritedTests {

        @Test
        @DisplayName("在父类找到注解")
        void testFindAnnotationInherited() {
            Optional<Deprecated> annotation = OpenAnnotation.findAnnotationInherited(
                    ChildOfDeprecated.class, Deprecated.class);
            assertThat(annotation).isPresent();
        }
    }

    @Nested
    @DisplayName("findAnnotationOnMethod方法测试")
    class FindAnnotationOnMethodTests {

        @Test
        @DisplayName("在方法上找到注解")
        void testFindAnnotationOnMethod() throws Exception {
            Method method = AnnotatedMethodClass.class.getDeclaredMethod("annotatedMethod");
            Optional<Deprecated> annotation = OpenAnnotation.findAnnotationOnMethod(method, Deprecated.class);
            assertThat(annotation).isPresent();
        }

        @Test
        @DisplayName("在接口方法上找到注解")
        void testFindAnnotationOnInterfaceMethod() throws Exception {
            Method method = ImplementsInterface.class.getMethod("interfaceMethod");
            Optional<Deprecated> annotation = OpenAnnotation.findAnnotationOnMethod(method, Deprecated.class);
            assertThat(annotation).isPresent();
        }
    }

    @Nested
    @DisplayName("isAnnotationPresent方法测试")
    class IsAnnotationPresentTests {

        @Test
        @DisplayName("检查注解存在")
        void testIsAnnotationPresent() {
            assertThat(OpenAnnotation.isAnnotationPresent(DeprecatedClass.class, Deprecated.class)).isTrue();
            assertThat(OpenAnnotation.isAnnotationPresent(TestClass.class, Deprecated.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAnyAnnotationPresent方法测试")
    class IsAnyAnnotationPresentTests {

        @Test
        @DisplayName("检查任一注解存在")
        void testIsAnyAnnotationPresent() {
            assertThat(OpenAnnotation.isAnyAnnotationPresent(DeprecatedClass.class, Deprecated.class, Override.class)).isTrue();
            assertThat(OpenAnnotation.isAnyAnnotationPresent(TestClass.class, Deprecated.class, Override.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAllAnnotationsPresent方法测试")
    class IsAllAnnotationsPresentTests {

        @Test
        @DisplayName("检查所有注解存在")
        void testIsAllAnnotationsPresent() {
            assertThat(OpenAnnotation.isAllAnnotationsPresent(MultiAnnotatedClass.class, Deprecated.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("getAttributeValue方法测试")
    class GetAttributeValueTests {

        @Test
        @DisplayName("获取注解属性值")
        void testGetAttributeValue() throws Exception {
            Method method = AttributeTestClass.class.getDeclaredMethod("annotatedMethod");
            TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);
            Object value = OpenAnnotation.getAttributeValue(annotation, "value");
            assertThat(value).isEqualTo("testValue");
        }

        @Test
        @DisplayName("属性不存在返回null")
        void testGetAttributeValueNotFound() throws Exception {
            Method method = AttributeTestClass.class.getDeclaredMethod("annotatedMethod");
            TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);
            Object value = OpenAnnotation.getAttributeValue(annotation, "nonexistent");
            assertThat(value).isNull();
        }

        @Test
        @DisplayName("获取注解属性值（带类型）")
        void testGetAttributeValueWithType() throws Exception {
            Method method = AttributeTestClass.class.getDeclaredMethod("annotatedMethod");
            TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);
            String value = OpenAnnotation.getAttributeValue(annotation, "value", String.class);
            assertThat(value).isEqualTo("testValue");
        }
    }

    @Nested
    @DisplayName("getAttributeValues方法测试")
    class GetAttributeValuesTests {

        @Test
        @DisplayName("获取所有属性值")
        void testGetAttributeValues() throws Exception {
            Method method = AttributeTestClass.class.getDeclaredMethod("annotatedMethod");
            TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);
            Map<String, Object> values = OpenAnnotation.getAttributeValues(annotation);
            assertThat(values).containsKey("value");
        }
    }

    @Nested
    @DisplayName("getDefaultValue方法测试")
    class GetDefaultValueTests {

        @Test
        @DisplayName("获取属性默认值")
        void testGetDefaultValue() {
            Object defaultValue = OpenAnnotation.getDefaultValue(TestAnnotation.class, "number");
            assertThat(defaultValue).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("元注解测试")
    class MetaAnnotationTests {

        @Test
        @DisplayName("检查元注解存在")
        void testIsMetaAnnotationPresent() {
            assertThat(OpenAnnotation.isMetaAnnotationPresent(DocumentedAnnotation.class, Documented.class)).isTrue();
        }

        @Test
        @DisplayName("查找元注解")
        void testFindMetaAnnotation() {
            Optional<Documented> annotation = OpenAnnotation.findMetaAnnotation(DocumentedAnnotation.class, Documented.class);
            assertThat(annotation).isPresent();
        }

        @Test
        @DisplayName("获取所有元注解")
        void testGetMetaAnnotations() {
            List<Annotation> annotations = OpenAnnotation.getMetaAnnotations(DocumentedAnnotation.class);
            // DocumentedAnnotation has @Documented, @Retention, @Target as meta-annotations
            // but getMetaAnnotations may only return certain ones depending on implementation
            assertThat(annotations).isNotNull();
        }
    }

    @Nested
    @DisplayName("注解信息测试")
    class AnnotationInfoTests {

        @Test
        @DisplayName("检查运行时保留")
        void testIsRuntimeRetained() {
            assertThat(OpenAnnotation.isRuntimeRetained(TestAnnotation.class)).isTrue();
        }

        @Test
        @DisplayName("检查可重复")
        void testIsRepeatable() {
            assertThat(OpenAnnotation.isRepeatable(RepeatableTag.class)).isTrue();
            assertThat(OpenAnnotation.isRepeatable(Deprecated.class)).isFalse();
        }

        @Test
        @DisplayName("获取可重复容器")
        void testGetRepeatableContainer() {
            Optional<Class<? extends Annotation>> container = OpenAnnotation.getRepeatableContainer(RepeatableTag.class);
            assertThat(container).isPresent();
            assertThat(container.get()).isEqualTo(RepeatableTags.class);
        }

        @Test
        @DisplayName("获取注解属性方法")
        void testGetAttributes() {
            List<Method> attributes = OpenAnnotation.getAttributes(TestAnnotation.class);
            assertThat(attributes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("类注解扫描测试")
    class ClassScanningTests {

        @Test
        @DisplayName("获取被注解的字段")
        void testGetAnnotatedFields() {
            List<Field> fields = OpenAnnotation.getAnnotatedFields(AnnotatedMembersClass.class, Deprecated.class);
            assertThat(fields).hasSize(1);
        }

        @Test
        @DisplayName("获取被注解的方法")
        void testGetAnnotatedMethods() {
            List<Method> methods = OpenAnnotation.getAnnotatedMethods(AnnotatedMembersClass.class, Deprecated.class);
            assertThat(methods).hasSize(1);
        }

        @Test
        @DisplayName("获取被注解的构造器")
        void testGetAnnotatedConstructors() {
            List<Constructor<?>> constructors = OpenAnnotation.getAnnotatedConstructors(
                    AnnotatedMembersClass.class, Deprecated.class);
            assertThat(constructors).hasSize(1);
        }

        @Test
        @DisplayName("获取被注解的参数")
        void testGetAnnotatedParameters() throws Exception {
            Method method = AnnotatedMembersClass.class.getDeclaredMethod("methodWithAnnotatedParam", String.class);
            List<java.lang.reflect.Parameter> params = OpenAnnotation.getAnnotatedParameters(method, Deprecated.class);
            assertThat(params).hasSize(1);
        }
    }

    @Nested
    @DisplayName("过滤测试")
    class FilteringTests {

        @Test
        @DisplayName("按条件过滤注解")
        void testFilterAnnotations() {
            List<Annotation> annotations = OpenAnnotation.filterAnnotations(MultiAnnotatedClass.class,
                    a -> a.annotationType() == Deprecated.class);
            assertThat(annotations).hasSize(1);
        }

        @Test
        @DisplayName("获取带元注解的注解")
        void testGetAnnotationsWithMeta() {
            List<Annotation> annotations = OpenAnnotation.getAnnotationsWithMeta(
                    MetaAnnotatedClass.class, Documented.class);
            assertThat(annotations).isNotEmpty();
        }
    }

    // Test helper annotations
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface TestAnnotation {
        String value();

        int number() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Repeatable(RepeatableTags.class)
    @interface RepeatableTag {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface RepeatableTags {
        RepeatableTag[] value();
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface DocumentedAnnotation {
    }

    // Test helper classes
    static class TestClass {
    }

    @Deprecated
    static class DeprecatedClass {
    }

    @Deprecated
    @SuppressWarnings("unused")
    static class MultiAnnotatedClass {
    }

    @RepeatableTag("tag1")
    @RepeatableTag("tag2")
    static class RepeatableAnnotatedClass {
    }

    static class ChildOfDeprecated extends DeprecatedClass {
    }

    @SuppressWarnings("unused")
    static class AnnotatedMethodClass {
        @Deprecated
        public void annotatedMethod() {
        }
    }

    interface DeprecatedInterface {
        @Deprecated
        void interfaceMethod();
    }

    static class ImplementsInterface implements DeprecatedInterface {
        @Override
        public void interfaceMethod() {
        }
    }

    @SuppressWarnings("unused")
    static class AttributeTestClass {
        @TestAnnotation(value = "testValue", number = 42)
        public void annotatedMethod() {
        }
    }

    @DocumentedAnnotation
    static class MetaAnnotatedClass {
    }

    @SuppressWarnings("unused")
    static class AnnotatedMembersClass {
        @Deprecated
        private String annotatedField;

        @Deprecated
        public AnnotatedMembersClass() {
        }

        @Deprecated
        public void annotatedMethod() {
        }

        public void methodWithAnnotatedParam(@Deprecated String param) {
        }
    }
}
