package cloud.opencode.base.reflect;

import org.junit.jupiter.api.*;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * AnnotationUtilTest Tests
 * AnnotationUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("AnnotationUtil 测试")
class AnnotationUtilTest {

    @BeforeEach
    void setUp() {
        AnnotationUtil.clearCache();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = AnnotationUtil.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getAnnotation方法测试")
    class GetAnnotationTests {

        @Test
        @DisplayName("获取类注解")
        void testGetAnnotation() {
            Deprecated annotation = AnnotationUtil.getAnnotation(DeprecatedClass.class, Deprecated.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解不存在返回null")
        void testGetAnnotationNotFound() {
            Deprecated annotation = AnnotationUtil.getAnnotation(TestClass.class, Deprecated.class);
            assertThat(annotation).isNull();
        }

        @Test
        @DisplayName("结果被缓存")
        void testGetAnnotationCached() {
            Deprecated annotation1 = AnnotationUtil.getAnnotation(DeprecatedClass.class, Deprecated.class);
            Deprecated annotation2 = AnnotationUtil.getAnnotation(DeprecatedClass.class, Deprecated.class);
            assertThat(annotation1).isSameAs(annotation2);
        }
    }

    @Nested
    @DisplayName("findAnnotation方法测试")
    class FindAnnotationTests {

        @Test
        @DisplayName("获取存在的注解")
        void testFindAnnotationExists() {
            Optional<Deprecated> annotation = AnnotationUtil.findAnnotation(DeprecatedClass.class, Deprecated.class);
            assertThat(annotation).isPresent();
        }

        @Test
        @DisplayName("获取不存在的注解")
        void testFindAnnotationNotExists() {
            Optional<Deprecated> annotation = AnnotationUtil.findAnnotation(TestClass.class, Deprecated.class);
            assertThat(annotation).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasAnnotation方法测试")
    class HasAnnotationTests {

        @Test
        @DisplayName("检查注解存在")
        void testHasAnnotation() {
            assertThat(AnnotationUtil.hasAnnotation(DeprecatedClass.class, Deprecated.class)).isTrue();
            assertThat(AnnotationUtil.hasAnnotation(TestClass.class, Deprecated.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getAnnotations方法测试")
    class GetAnnotationsTests {

        @Test
        @DisplayName("获取所有注解")
        void testGetAnnotations() {
            Annotation[] annotations = AnnotationUtil.getAnnotations(MultiAnnotatedClass.class);
            assertThat(annotations).hasSizeGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getDeclaredAnnotations方法测试")
    class GetDeclaredAnnotationsTests {

        @Test
        @DisplayName("获取声明的注解")
        void testGetDeclaredAnnotations() {
            Annotation[] annotations = AnnotationUtil.getDeclaredAnnotations(MultiAnnotatedClass.class);
            assertThat(annotations).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getRepeatableAnnotations方法测试")
    class GetRepeatableAnnotationsTests {

        @Test
        @DisplayName("获取可重复注解")
        void testGetRepeatableAnnotations() {
            RepeatableTag[] annotations = AnnotationUtil.getRepeatableAnnotations(
                    RepeatableAnnotatedClass.class, RepeatableTag.class);
            assertThat(annotations).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findMetaAnnotation方法测试")
    class FindMetaAnnotationTests {

        @Test
        @DisplayName("直接注解")
        void testFindMetaAnnotationDirect() {
            Deprecated annotation = AnnotationUtil.findMetaAnnotation(DeprecatedClass.class, Deprecated.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("查找元注解")
        void testFindMetaAnnotation() {
            Documented annotation = AnnotationUtil.findMetaAnnotation(
                    MetaAnnotatedClass.class, Documented.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("元注解不存在返回null")
        void testFindMetaAnnotationNotFound() {
            Inherited annotation = AnnotationUtil.findMetaAnnotation(TestClass.class, Inherited.class);
            assertThat(annotation).isNull();
        }
    }

    @Nested
    @DisplayName("hasMetaAnnotation方法测试")
    class HasMetaAnnotationTests {

        @Test
        @DisplayName("检查元注解存在")
        void testHasMetaAnnotation() {
            assertThat(AnnotationUtil.hasMetaAnnotation(MetaAnnotatedClass.class, Documented.class)).isTrue();
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
            Object value = AnnotationUtil.getAttributeValue(annotation, "value");
            assertThat(value).isEqualTo("testValue");
        }

        @Test
        @DisplayName("属性不存在返回null")
        void testGetAttributeValueNotFound() throws Exception {
            Method method = AttributeTestClass.class.getDeclaredMethod("annotatedMethod");
            TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);
            Object value = AnnotationUtil.getAttributeValue(annotation, "nonexistent");
            assertThat(value).isNull();
        }

        @Test
        @DisplayName("获取注解属性值（指定类型）")
        void testGetAttributeValueWithType() throws Exception {
            Method method = AttributeTestClass.class.getDeclaredMethod("annotatedMethod");
            TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);
            String value = AnnotationUtil.getAttributeValue(annotation, "value", String.class);
            assertThat(value).isEqualTo("testValue");
        }

        @Test
        @DisplayName("类型不匹配抛出异常")
        void testGetAttributeValueTypeMismatch() throws Exception {
            Method method = AttributeTestClass.class.getDeclaredMethod("annotatedMethod");
            TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);
            assertThatThrownBy(() -> AnnotationUtil.getAttributeValue(annotation, "value", Integer.class))
                    .isInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    @DisplayName("getAttributes方法测试")
    class GetAttributesTests {

        @Test
        @DisplayName("获取所有属性")
        void testGetAttributes() throws Exception {
            Method method = AttributeTestClass.class.getDeclaredMethod("annotatedMethod");
            TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);
            Map<String, Object> attributes = AnnotationUtil.getAttributes(annotation);
            assertThat(attributes).containsKey("value");
            assertThat(attributes).containsKey("number");
        }
    }

    @Nested
    @DisplayName("getValue方法测试")
    class GetValueTests {

        @Test
        @DisplayName("获取value属性")
        void testGetValue() throws Exception {
            Method method = AttributeTestClass.class.getDeclaredMethod("annotatedMethod");
            TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);
            Object value = AnnotationUtil.getValue(annotation);
            assertThat(value).isEqualTo("testValue");
        }

        @Test
        @DisplayName("获取value属性（指定类型）")
        void testGetValueWithType() throws Exception {
            Method method = AttributeTestClass.class.getDeclaredMethod("annotatedMethod");
            TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);
            String value = AnnotationUtil.getValue(annotation, String.class);
            assertThat(value).isEqualTo("testValue");
        }
    }

    @Nested
    @DisplayName("isRepeatable方法测试")
    class IsRepeatableTests {

        @Test
        @DisplayName("检查可重复注解")
        void testIsRepeatable() {
            assertThat(AnnotationUtil.isRepeatable(RepeatableTag.class)).isTrue();
            assertThat(AnnotationUtil.isRepeatable(Deprecated.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getRepeatableContainer方法测试")
    class GetRepeatableContainerTests {

        @Test
        @DisplayName("获取可重复注解容器类")
        void testGetRepeatableContainer() {
            Class<? extends Annotation> container = AnnotationUtil.getRepeatableContainer(RepeatableTag.class);
            assertThat(container).isEqualTo(RepeatableTags.class);
        }

        @Test
        @DisplayName("非可重复注解返回null")
        void testGetRepeatableContainerNotRepeatable() {
            Class<? extends Annotation> container = AnnotationUtil.getRepeatableContainer(Deprecated.class);
            assertThat(container).isNull();
        }
    }

    @Nested
    @DisplayName("getAnnotationType方法测试")
    class GetAnnotationTypeTests {

        @Test
        @DisplayName("获取注解类型")
        void testGetAnnotationType() {
            Deprecated annotation = AnnotationUtil.getAnnotation(DeprecatedClass.class, Deprecated.class);
            assertThat(AnnotationUtil.getAnnotationType(annotation)).isEqualTo(Deprecated.class);
        }
    }

    @Nested
    @DisplayName("getAttributeMethods方法测试")
    class GetAttributeMethodsTests {

        @Test
        @DisplayName("获取注解属性方法")
        void testGetAttributeMethods() {
            List<Method> methods = AnnotationUtil.getAttributeMethods(TestAnnotation.class);
            assertThat(methods).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findAnnotationOnClass方法测试")
    class FindAnnotationOnClassTests {

        @Test
        @DisplayName("在当前类找到注解")
        void testFindAnnotationOnClass() {
            Deprecated annotation = AnnotationUtil.findAnnotationOnClass(DeprecatedClass.class, Deprecated.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("在父类找到注解")
        void testFindAnnotationOnSuperclass() {
            Deprecated annotation = AnnotationUtil.findAnnotationOnClass(ChildOfDeprecated.class, Deprecated.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("在接口找到注解")
        void testFindAnnotationOnInterface() {
            Deprecated annotation = AnnotationUtil.findAnnotationOnClass(
                    ImplementsDeprecatedInterface.class, Deprecated.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("注解不存在返回null")
        void testFindAnnotationOnClassNotFound() {
            Inherited annotation = AnnotationUtil.findAnnotationOnClass(TestClass.class, Inherited.class);
            assertThat(annotation).isNull();
        }
    }

    @Nested
    @DisplayName("findAllAnnotationsOnClass方法测试")
    class FindAllAnnotationsOnClassTests {

        @Test
        @DisplayName("收集类层次结构中的所有注解")
        void testFindAllAnnotationsOnClass() {
            List<CustomAnnotation> annotations = AnnotationUtil.findAllAnnotationsOnClass(
                    MultiLevelChild.class, CustomAnnotation.class);
            assertThat(annotations).hasSize(2);
        }
    }

    @Nested
    @DisplayName("clearCache方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除缓存")
        void testClearCache() {
            AnnotationUtil.getAnnotation(DeprecatedClass.class, Deprecated.class);
            AnnotationUtil.clearCache();
            // Should not throw
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

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    @interface CustomAnnotation {
        String value() default "";
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

    @DocumentedAnnotation
    static class MetaAnnotatedClass {
    }

    @SuppressWarnings("unused")
    static class AttributeTestClass {
        @TestAnnotation(value = "testValue", number = 42)
        public void annotatedMethod() {
        }
    }

    static class ChildOfDeprecated extends DeprecatedClass {
    }

    @Deprecated
    interface DeprecatedInterface {
    }

    static class ImplementsDeprecatedInterface implements DeprecatedInterface {
    }

    @CustomAnnotation("parent")
    static class ParentWithCustomAnnotation {
    }

    @CustomAnnotation("child")
    static class MultiLevelChild extends ParentWithCustomAnnotation {
    }
}
