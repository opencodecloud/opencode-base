package cloud.opencode.base.reflect.scan;

import org.junit.jupiter.api.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * AnnotationScannerTest Tests
 * AnnotationScannerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("AnnotationScanner 测试")
class AnnotationScannerTest {

    @Nested
    @DisplayName("from静态方法测试")
    class FromTests {

        @Test
        @DisplayName("从ClassScanner创建")
        void testFromClassScanner() {
            ClassScanner classScanner = ClassScanner.create();
            AnnotationScanner scanner = AnnotationScanner.from(classScanner);
            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("从ClassLoader创建")
        void testFromClassLoader() {
            AnnotationScanner scanner = AnnotationScanner.from(ClassLoader.getSystemClassLoader());
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("create静态方法测试")
    class CreateTests {

        @Test
        @DisplayName("使用上下文ClassLoader创建")
        void testCreate() {
            AnnotationScanner scanner = AnnotationScanner.create();
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("inPackage方法测试")
    class InPackageTests {

        @Test
        @DisplayName("设置扫描包")
        void testInPackage() {
            AnnotationScanner scanner = AnnotationScanner.create()
                    .inPackage("cloud.opencode.base.reflect.scan");
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("includeMetaAnnotations方法测试")
    class IncludeMetaAnnotationsTests {

        @Test
        @DisplayName("包含元注解")
        void testIncludeMetaAnnotations() {
            AnnotationScanner scanner = AnnotationScanner.create()
                    .includeMetaAnnotations(true);
            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("findClassesWithAnnotation方法测试")
    class FindClassesWithAnnotationTests {

        @Test
        @DisplayName("查找具有注解的类")
        void testFindClassesWithAnnotation() {
            AnnotationScanner scanner = AnnotationScanner.from(
                    ClassScanner.create()
                            .inPackage("cloud.opencode.base.reflect.scan")
                            .includeInnerClasses(true)
                            .filter(c -> c == AnnotatedTestClass.class)
            );
            Set<Class<?>> result = scanner.findClassesWithAnnotation(TestClassAnnotation.class);
            assertThat(result).contains(AnnotatedTestClass.class);
        }

        @Test
        @DisplayName("没有匹配返回空集合")
        void testFindClassesWithAnnotationEmpty() {
            AnnotationScanner scanner = AnnotationScanner.from(
                    ClassScanner.create()
                            .inPackage("cloud.opencode.base.reflect.scan")
                            .includeInnerClasses(true)
                            .filter(c -> c == NonAnnotatedTestClass.class)
            );
            Set<Class<?>> result = scanner.findClassesWithAnnotation(TestClassAnnotation.class);
            assertThat(result).doesNotContain(NonAnnotatedTestClass.class);
        }
    }

    @Nested
    @DisplayName("findClassesWithAnyAnnotation方法测试")
    class FindClassesWithAnyAnnotationTests {

        @Test
        @DisplayName("查找具有任一注解的类")
        void testFindClassesWithAnyAnnotation() {
            AnnotationScanner scanner = AnnotationScanner.from(
                    ClassScanner.create()
                            .inPackage("cloud.opencode.base.reflect.scan")
                            .includeInnerClasses(true)
                            .filter(c -> c == AnnotatedTestClass.class || c == AnotherAnnotatedClass.class)
            );
            Set<Class<?>> result = scanner.findClassesWithAnyAnnotation(
                    TestClassAnnotation.class, AnotherAnnotation.class);
            assertThat(result).contains(AnnotatedTestClass.class, AnotherAnnotatedClass.class);
        }
    }

    @Nested
    @DisplayName("findClassesWithAllAnnotations方法测试")
    class FindClassesWithAllAnnotationsTests {

        @Test
        @DisplayName("查找具有所有注解的类")
        void testFindClassesWithAllAnnotations() {
            AnnotationScanner scanner = AnnotationScanner.from(
                    ClassScanner.create()
                            .inPackage("cloud.opencode.base.reflect.scan")
                            .includeInnerClasses(true)
                            .filter(c -> c == MultiAnnotatedClass.class)
            );
            Set<Class<?>> result = scanner.findClassesWithAllAnnotations(
                    TestClassAnnotation.class, AnotherAnnotation.class);
            assertThat(result).contains(MultiAnnotatedClass.class);
        }
    }

    @Nested
    @DisplayName("findMethodsWithAnnotation方法测试")
    class FindMethodsWithAnnotationTests {

        @Test
        @DisplayName("查找具有注解的方法")
        void testFindMethodsWithAnnotation() {
            AnnotationScanner scanner = AnnotationScanner.from(
                    ClassScanner.create()
                            .inPackage("cloud.opencode.base.reflect.scan")
                            .includeInnerClasses(true)
                            .filter(c -> c == MethodAnnotatedClass.class)
            );
            Map<Class<?>, Set<Method>> result = scanner.findMethodsWithAnnotation(TestMethodAnnotation.class);
            assertThat(result).containsKey(MethodAnnotatedClass.class);
            assertThat(result.get(MethodAnnotatedClass.class)).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("findAllMethodsWithAnnotation方法测试")
    class FindAllMethodsWithAnnotationTests {

        @Test
        @DisplayName("查找所有具有注解的方法（扁平列表）")
        void testFindAllMethodsWithAnnotation() {
            AnnotationScanner scanner = AnnotationScanner.from(
                    ClassScanner.create()
                            .inPackage("cloud.opencode.base.reflect.scan")
                            .includeInnerClasses(true)
                            .filter(c -> c == MethodAnnotatedClass.class)
            );
            List<Method> result = scanner.findAllMethodsWithAnnotation(TestMethodAnnotation.class);
            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("findFieldsWithAnnotation方法测试")
    class FindFieldsWithAnnotationTests {

        @Test
        @DisplayName("查找具有注解的字段")
        void testFindFieldsWithAnnotation() {
            AnnotationScanner scanner = AnnotationScanner.from(
                    ClassScanner.create()
                            .inPackage("cloud.opencode.base.reflect.scan")
                            .includeInnerClasses(true)
                            .filter(c -> c == FieldAnnotatedClass.class)
            );
            Map<Class<?>, Set<Field>> result = scanner.findFieldsWithAnnotation(TestFieldAnnotation.class);
            assertThat(result).containsKey(FieldAnnotatedClass.class);
            assertThat(result.get(FieldAnnotatedClass.class)).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("findAllFieldsWithAnnotation方法测试")
    class FindAllFieldsWithAnnotationTests {

        @Test
        @DisplayName("查找所有具有注解的字段（扁平列表）")
        void testFindAllFieldsWithAnnotation() {
            AnnotationScanner scanner = AnnotationScanner.from(
                    ClassScanner.create()
                            .inPackage("cloud.opencode.base.reflect.scan")
                            .includeInnerClasses(true)
                            .filter(c -> c == FieldAnnotatedClass.class)
            );
            List<Field> result = scanner.findAllFieldsWithAnnotation(TestFieldAnnotation.class);
            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("findConstructorsWithAnnotation方法测试")
    class FindConstructorsWithAnnotationTests {

        @Test
        @DisplayName("查找具有注解的构造器")
        void testFindConstructorsWithAnnotation() {
            AnnotationScanner scanner = AnnotationScanner.from(
                    ClassScanner.create()
                            .inPackage("cloud.opencode.base.reflect.scan")
                            .includeInnerClasses(true)
                            .filter(c -> c == ConstructorAnnotatedClass.class)
            );
            Map<Class<?>, Set<Constructor<?>>> result = scanner.findConstructorsWithAnnotation(TestConstructorAnnotation.class);
            assertThat(result).containsKey(ConstructorAnnotatedClass.class);
        }
    }

    @Nested
    @DisplayName("findParametersWithAnnotation方法测试")
    class FindParametersWithAnnotationTests {

        @Test
        @DisplayName("查找具有注解的参数")
        void testFindParametersWithAnnotation() {
            AnnotationScanner scanner = AnnotationScanner.from(
                    ClassScanner.create()
                            .inPackage("cloud.opencode.base.reflect.scan")
                            .includeInnerClasses(true)
                            .filter(c -> c == ParameterAnnotatedClass.class)
            );
            Map<Executable, Set<Parameter>> result = scanner.findParametersWithAnnotation(TestParamAnnotation.class);
            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("AnnotatedMember记录测试")
    class AnnotatedMemberTests {

        @Test
        @DisplayName("创建AnnotatedMember记录")
        void testAnnotatedMember() throws NoSuchMethodException {
            Method method = MethodAnnotatedClass.class.getDeclaredMethod("annotatedMethod");
            Annotation annotation = method.getAnnotation(TestMethodAnnotation.class);
            AnnotationScanner.AnnotatedMember<Method> member =
                    new AnnotationScanner.AnnotatedMember<>(MethodAnnotatedClass.class, method, annotation);

            assertThat(member.declaringClass()).isEqualTo(MethodAnnotatedClass.class);
            assertThat(member.element()).isEqualTo(method);
            assertThat(member.annotation()).isNotNull();
        }
    }

    // Test annotations
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface TestClassAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface AnotherAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface TestMethodAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface TestFieldAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.CONSTRUCTOR)
    @interface TestConstructorAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface TestParamAnnotation {}

    // Test classes
    @TestClassAnnotation
    static class AnnotatedTestClass {}

    static class NonAnnotatedTestClass {}

    @AnotherAnnotation
    static class AnotherAnnotatedClass {}

    @TestClassAnnotation
    @AnotherAnnotation
    static class MultiAnnotatedClass {}

    static class MethodAnnotatedClass {
        @TestMethodAnnotation
        public void annotatedMethod() {}
    }

    static class FieldAnnotatedClass {
        @TestFieldAnnotation
        private String annotatedField;
    }

    static class ConstructorAnnotatedClass {
        @TestConstructorAnnotation
        public ConstructorAnnotatedClass() {}
    }

    static class ParameterAnnotatedClass {
        public void methodWithAnnotatedParam(@TestParamAnnotation String param) {}
    }
}
