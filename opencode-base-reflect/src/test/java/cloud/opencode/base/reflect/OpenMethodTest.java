package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenMethodTest Tests
 * OpenMethodTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenMethod 测试")
class OpenMethodTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenMethod.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getMethod方法测试")
    class GetMethodTests {

        @Test
        @DisplayName("获取方法")
        void testGetMethod() {
            Method method = OpenMethod.getMethod(TestClass.class, "getName");
            assertThat(method).isNotNull();
            assertThat(method.getName()).isEqualTo("getName");
        }

        @Test
        @DisplayName("获取带参数的方法")
        void testGetMethodWithParams() {
            Method method = OpenMethod.getMethod(TestClass.class, "setName", String.class);
            assertThat(method).isNotNull();
        }

        @Test
        @DisplayName("获取继承的方法")
        void testGetMethodInherited() {
            Method method = OpenMethod.getMethod(ChildClass.class, "parentMethod");
            assertThat(method).isNotNull();
        }

        @Test
        @DisplayName("方法不存在抛出异常")
        void testGetMethodNotFound() {
            assertThatThrownBy(() -> OpenMethod.getMethod(TestClass.class, "nonexistent"))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("不强制访问时获取方法")
        void testGetMethodNoForceAccess() {
            Method method = OpenMethod.getMethod(TestClass.class, "publicMethod", false);
            assertThat(method).isNotNull();
        }
    }

    @Nested
    @DisplayName("getMatchingMethod方法测试")
    class GetMatchingMethodTests {

        @Test
        @DisplayName("精确匹配")
        void testGetMatchingMethodExact() {
            Method method = OpenMethod.getMatchingMethod(TestClass.class, "setName", String.class);
            assertThat(method).isNotNull();
        }

        @Test
        @DisplayName("兼容匹配")
        void testGetMatchingMethodCompatible() {
            Method method = OpenMethod.getMatchingMethod(TestClass.class, "setNumber", Integer.class);
            assertThat(method).isNotNull();
        }
    }

    @Nested
    @DisplayName("getAllMethods方法测试")
    class GetAllMethodsTests {

        @Test
        @DisplayName("获取所有方法")
        void testGetAllMethods() {
            List<Method> methods = OpenMethod.getAllMethods(ChildClass.class);
            List<String> names = methods.stream().map(Method::getName).toList();
            assertThat(names).contains("childMethod", "parentMethod");
        }
    }

    @Nested
    @DisplayName("getDeclaredMethods方法测试")
    class GetDeclaredMethodsTests {

        @Test
        @DisplayName("获取声明的方法列表")
        void testGetDeclaredMethods() {
            List<Method> methods = OpenMethod.getDeclaredMethods(TestClass.class);
            assertThat(methods).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getMethodsWithAnnotation方法测试")
    class GetMethodsWithAnnotationTests {

        @Test
        @DisplayName("获取带注解的方法")
        void testGetMethodsWithAnnotation() {
            List<Method> methods = OpenMethod.getMethodsWithAnnotation(AnnotatedMethodClass.class, Deprecated.class);
            assertThat(methods).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getOverloadMethods方法测试")
    class GetOverloadMethodsTests {

        @Test
        @DisplayName("获取重载方法")
        void testGetOverloadMethods() {
            List<Method> methods = OpenMethod.getOverloadMethods(OverloadedClass.class, "overloaded");
            assertThat(methods).hasSizeGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("getGetters方法测试")
    class GetGettersTests {

        @Test
        @DisplayName("获取所有Getter方法")
        void testGetGetters() {
            List<Method> getters = OpenMethod.getGetters(TestClass.class);
            assertThat(getters).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getSetters方法测试")
    class GetSettersTests {

        @Test
        @DisplayName("获取所有Setter方法")
        void testGetSetters() {
            List<Method> setters = OpenMethod.getSetters(TestClass.class);
            assertThat(setters).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("invokeMethod方法测试")
    class InvokeMethodTests {

        @Test
        @DisplayName("调用方法")
        void testInvokeMethod() {
            TestClass obj = new TestClass();
            obj.setName("test");
            Object result = OpenMethod.invokeMethod(obj, "getName");
            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("调用带参数的方法")
        void testInvokeMethodWithArgs() {
            TestClass obj = new TestClass();
            OpenMethod.invokeMethod(obj, "setName", "newName");
            assertThat(obj.getName()).isEqualTo("newName");
        }

        @Test
        @DisplayName("调用方法（带返回类型）")
        void testInvokeMethodWithReturnType() {
            TestClass obj = new TestClass();
            obj.setName("test");
            String result = OpenMethod.invokeMethod(obj, "getName", String.class);
            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("返回类型不匹配抛出异常")
        void testInvokeMethodTypeMismatch() {
            TestClass obj = new TestClass();
            obj.setName("test");
            assertThatThrownBy(() -> OpenMethod.invokeMethod(obj, "getName", Integer.class))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("invokeStaticMethod方法测试")
    class InvokeStaticMethodTests {

        @Test
        @DisplayName("调用静态方法")
        void testInvokeStaticMethod() {
            Object result = OpenMethod.invokeStaticMethod(StaticMethodClass.class, "staticMethod");
            assertThat(result).isEqualTo("static");
        }

        @Test
        @DisplayName("调用静态方法（带返回类型）")
        void testInvokeStaticMethodWithReturnType() {
            String result = OpenMethod.invokeStaticMethod(StaticMethodClass.class, "staticMethod", String.class);
            assertThat(result).isEqualTo("static");
        }
    }

    @Nested
    @DisplayName("hasMethod方法测试")
    class HasMethodTests {

        @Test
        @DisplayName("检查方法存在")
        void testHasMethod() {
            assertThat(OpenMethod.hasMethod(TestClass.class, "getName")).isTrue();
            assertThat(OpenMethod.hasMethod(TestClass.class, "nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("isGetter方法测试")
    class IsGetterTests {

        @Test
        @DisplayName("检查是否为Getter")
        void testIsGetter() throws Exception {
            Method getter = TestClass.class.getDeclaredMethod("getName");
            Method setter = TestClass.class.getDeclaredMethod("setName", String.class);
            Method boolGetter = BooleanClass.class.getDeclaredMethod("isActive");

            assertThat(OpenMethod.isGetter(getter)).isTrue();
            assertThat(OpenMethod.isGetter(setter)).isFalse();
            assertThat(OpenMethod.isGetter(boolGetter)).isTrue();
        }
    }

    @Nested
    @DisplayName("isSetter方法测试")
    class IsSetterTests {

        @Test
        @DisplayName("检查是否为Setter")
        void testIsSetter() throws Exception {
            Method setter = TestClass.class.getDeclaredMethod("setName", String.class);
            Method getter = TestClass.class.getDeclaredMethod("getName");

            assertThat(OpenMethod.isSetter(setter)).isTrue();
            assertThat(OpenMethod.isSetter(getter)).isFalse();
        }
    }

    @Nested
    @DisplayName("方法遍历测试")
    class IterationTests {

        @Test
        @DisplayName("forEach遍历")
        void testForEach() {
            int[] count = {0};
            OpenMethod.forEach(TestClass.class, m -> count[0]++);
            assertThat(count[0]).isGreaterThan(0);
        }

        @Test
        @DisplayName("findFirst查找")
        void testFindFirst() {
            Optional<Method> method = OpenMethod.findFirst(TestClass.class, m -> m.getName().equals("getName"));
            assertThat(method).isPresent();
        }

        @Test
        @DisplayName("stream流操作")
        void testStream() {
            long count = OpenMethod.stream(TestClass.class).count();
            assertThat(count).isGreaterThan(0);
        }
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class TestClass {
        private String name;
        private int number;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public void publicMethod() {
        }
    }

    @SuppressWarnings("unused")
    static class ParentClass {
        public void parentMethod() {
        }
    }

    static class ChildClass extends ParentClass {
        @SuppressWarnings("unused")
        public void childMethod() {
        }
    }

    @SuppressWarnings("unused")
    static class AnnotatedMethodClass {
        @Deprecated
        public void deprecatedMethod() {
        }

        public void normalMethod() {
        }
    }

    @SuppressWarnings("unused")
    static class OverloadedClass {
        public void overloaded() {
        }

        public void overloaded(String arg) {
        }

        public void overloaded(int arg) {
        }
    }

    @SuppressWarnings("unused")
    static class StaticMethodClass {
        public static String staticMethod() {
            return "static";
        }
    }

    @SuppressWarnings("unused")
    static class BooleanClass {
        private boolean active;

        public boolean isActive() {
            return active;
        }
    }
}
