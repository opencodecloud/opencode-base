package cloud.opencode.base.core.reflect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * MethodUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("MethodUtil 测试")
class MethodUtilTest {

    // 测试用注解
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface TestAnnotation {}

    // 测试用接口
    interface TestInterface {
        default String defaultMethod() { return "default"; }
    }

    // 测试用类
    static class Parent {
        public void parentMethod() {}
    }

    static class TestClass extends Parent implements TestInterface {
        private String name;
        private boolean active;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public String getValue() { return "value"; }
        public Boolean getEnabled() { return true; }

        private void privateMethod() {}
        public static void staticMethod() {}

        @TestAnnotation
        public void annotatedMethod() {}

        public String methodWithParams(String a, int b) { return a + b; }
        public List<String> getList() { return null; }
        public void process(List<Integer> items) {}

        public synchronized void syncMethod() {}
    }

    abstract static class AbstractClass {
        public abstract void abstractMethod();
    }

    @Nested
    @DisplayName("获取方法测试")
    class GetMethodsTests {

        @Test
        @DisplayName("getAllMethods 包括父类")
        void testGetAllMethodsWithInheritance() {
            List<Method> methods = MethodUtil.getAllMethods(TestClass.class);
            assertThat(methods).isNotEmpty();

            List<String> names = methods.stream().map(Method::getName).toList();
            assertThat(names).contains("getName", "setName", "parentMethod");
        }

        @Test
        @DisplayName("getDeclaredMethods 不包括父类")
        void testGetDeclaredMethods() {
            List<Method> methods = MethodUtil.getDeclaredMethods(TestClass.class);
            assertThat(methods).isNotEmpty();

            List<String> names = methods.stream().map(Method::getName).toList();
            assertThat(names).contains("getName", "setName");
            assertThat(names).doesNotContain("parentMethod");
        }

        @Test
        @DisplayName("getMethodsByName")
        void testGetMethodsByName() {
            List<Method> methods = MethodUtil.getMethodsByName(TestClass.class, "getName");
            assertThat(methods).hasSize(1);
            assertThat(methods.get(0).getName()).isEqualTo("getName");
        }

        @Test
        @DisplayName("getMethod 精确匹配")
        void testGetMethodExact() {
            Optional<Method> method = MethodUtil.getMethod(TestClass.class, "setName", String.class);
            assertThat(method).isPresent();
            assertThat(method.get().getParameterCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("getMethod 不存在")
        void testGetMethodNotFound() {
            Optional<Method> method = MethodUtil.getMethod(TestClass.class, "nonExistent");
            assertThat(method).isEmpty();
        }
    }

    @Nested
    @DisplayName("按条件过滤方法测试")
    class FilterMethodsTests {

        @Test
        @DisplayName("getMethodsWithAnnotation")
        void testGetMethodsWithAnnotation() {
            List<Method> methods = MethodUtil.getMethodsWithAnnotation(TestClass.class, TestAnnotation.class);
            assertThat(methods).hasSize(1);
            assertThat(methods.get(0).getName()).isEqualTo("annotatedMethod");
        }

        @Test
        @DisplayName("getMethodsByReturnType")
        void testGetMethodsByReturnType() {
            List<Method> methods = MethodUtil.getMethodsByReturnType(TestClass.class, String.class);
            assertThat(methods).isNotEmpty();
            assertThat(methods.stream().map(Method::getName).toList())
                    .contains("getName", "getValue");
        }

        @Test
        @DisplayName("getStaticMethods")
        void testGetStaticMethods() {
            List<Method> methods = MethodUtil.getStaticMethods(TestClass.class);
            assertThat(methods).isNotEmpty();
            assertThat(methods.stream().map(Method::getName).toList()).contains("staticMethod");
        }

        @Test
        @DisplayName("getPublicMethods")
        void testGetPublicMethods() {
            List<Method> methods = MethodUtil.getPublicMethods(TestClass.class);
            assertThat(methods).isNotEmpty();
            assertThat(methods.stream().map(Method::getName).toList())
                    .contains("getName", "setName");
        }
    }

    @Nested
    @DisplayName("Getter/Setter 测试")
    class GetterSetterTests {

        @Test
        @DisplayName("getGetterMethods")
        void testGetGetterMethods() {
            List<Method> getters = MethodUtil.getGetterMethods(TestClass.class);
            assertThat(getters).isNotEmpty();

            List<String> names = getters.stream().map(Method::getName).toList();
            assertThat(names).contains("getName", "getValue", "isActive");
        }

        @Test
        @DisplayName("getSetterMethods")
        void testGetSetterMethods() {
            List<Method> setters = MethodUtil.getSetterMethods(TestClass.class);
            assertThat(setters).isNotEmpty();

            List<String> names = setters.stream().map(Method::getName).toList();
            assertThat(names).contains("setName", "setActive");
        }

        @Test
        @DisplayName("isGetter - get 前缀")
        void testIsGetterWithGet() throws Exception {
            Method method = TestClass.class.getMethod("getName");
            assertThat(MethodUtil.isGetter(method)).isTrue();
        }

        @Test
        @DisplayName("isGetter - is 前缀")
        void testIsGetterWithIs() throws Exception {
            Method method = TestClass.class.getMethod("isActive");
            assertThat(MethodUtil.isGetter(method)).isTrue();
        }

        @Test
        @DisplayName("isGetter - 非 Getter")
        void testIsGetterFalse() throws Exception {
            Method method = TestClass.class.getMethod("setName", String.class);
            assertThat(MethodUtil.isGetter(method)).isFalse();

            Method voidMethod = TestClass.class.getMethod("staticMethod");
            assertThat(MethodUtil.isGetter(voidMethod)).isFalse();
        }

        @Test
        @DisplayName("isSetter")
        void testIsSetter() throws Exception {
            Method method = TestClass.class.getMethod("setName", String.class);
            assertThat(MethodUtil.isSetter(method)).isTrue();

            Method getter = TestClass.class.getMethod("getName");
            assertThat(MethodUtil.isSetter(getter)).isFalse();
        }

        @Test
        @DisplayName("getPropertyNameFromGetter - get")
        void testGetPropertyNameFromGetterGet() throws Exception {
            Method method = TestClass.class.getMethod("getName");
            assertThat(MethodUtil.getPropertyNameFromGetter(method)).isEqualTo("name");
        }

        @Test
        @DisplayName("getPropertyNameFromGetter - is")
        void testGetPropertyNameFromGetterIs() throws Exception {
            Method method = TestClass.class.getMethod("isActive");
            assertThat(MethodUtil.getPropertyNameFromGetter(method)).isEqualTo("active");
        }

        @Test
        @DisplayName("getPropertyNameFromSetter")
        void testGetPropertyNameFromSetter() throws Exception {
            Method method = TestClass.class.getMethod("setName", String.class);
            assertThat(MethodUtil.getPropertyNameFromSetter(method)).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("泛型类型测试")
    class GenericTypeTests {

        @Test
        @DisplayName("getGenericReturnType")
        void testGetGenericReturnType() throws Exception {
            Method method = TestClass.class.getMethod("getList");
            Type type = MethodUtil.getGenericReturnType(method);
            assertThat(type.getTypeName()).contains("List");
            assertThat(type.getTypeName()).contains("String");
        }

        @Test
        @DisplayName("getGenericParameterTypes")
        void testGetGenericParameterTypes() throws Exception {
            Method method = TestClass.class.getMethod("process", List.class);
            Type[] types = MethodUtil.getGenericParameterTypes(method);
            assertThat(types).hasSize(1);
            assertThat(types[0].getTypeName()).contains("Integer");
        }
    }

    @Nested
    @DisplayName("方法调用测试")
    class InvocationTests {

        @Test
        @DisplayName("invoke")
        void testInvoke() throws Exception {
            TestClass obj = new TestClass();
            obj.setName("Test");

            Method method = TestClass.class.getMethod("getName");
            String result = MethodUtil.invoke(obj, method);
            assertThat(result).isEqualTo("Test");
        }

        @Test
        @DisplayName("invoke 带参数")
        void testInvokeWithArgs() throws Exception {
            TestClass obj = new TestClass();
            Method method = TestClass.class.getMethod("methodWithParams", String.class, int.class);

            String result = MethodUtil.invoke(obj, method, "Value", 42);
            assertThat(result).isEqualTo("Value42");
        }

        @Test
        @DisplayName("invokeStatic")
        void testInvokeStatic() throws Exception {
            Method method = TestClass.class.getMethod("staticMethod");
            MethodUtil.invokeStatic(method);
            // 静态方法调用成功即可
        }
    }

    @Nested
    @DisplayName("修饰符检查测试")
    class ModifierCheckTests {

        @Test
        @DisplayName("isAbstract")
        void testIsAbstract() throws Exception {
            Method method = AbstractClass.class.getMethod("abstractMethod");
            assertThat(MethodUtil.isAbstract(method)).isTrue();

            Method concrete = TestClass.class.getMethod("getName");
            assertThat(MethodUtil.isAbstract(concrete)).isFalse();
        }

        @Test
        @DisplayName("isSynchronized")
        void testIsSynchronized() throws Exception {
            Method method = TestClass.class.getMethod("syncMethod");
            assertThat(MethodUtil.isSynchronized(method)).isTrue();

            Method nonSync = TestClass.class.getMethod("getName");
            assertThat(MethodUtil.isSynchronized(nonSync)).isFalse();
        }

        @Test
        @DisplayName("isDefault")
        void testIsDefault() throws Exception {
            Method method = TestInterface.class.getMethod("defaultMethod");
            assertThat(MethodUtil.isDefault(method)).isTrue();

            Method nonDefault = TestClass.class.getMethod("getName");
            assertThat(MethodUtil.isDefault(nonDefault)).isFalse();
        }
    }
}
