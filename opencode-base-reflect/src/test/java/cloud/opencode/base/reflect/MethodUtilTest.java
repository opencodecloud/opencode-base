package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * MethodUtilTest Tests
 * MethodUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("MethodUtil 测试")
class MethodUtilTest {

    @BeforeEach
    void setUp() {
        MethodUtil.clearCache();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = MethodUtil.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getDeclaredMethods方法测试")
    class GetDeclaredMethodsTests {

        @Test
        @DisplayName("获取声明的方法")
        void testGetDeclaredMethods() {
            Method[] methods = MethodUtil.getDeclaredMethods(TestClass.class);
            assertThat(methods).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getAllMethods方法测试")
    class GetAllMethodsTests {

        @Test
        @DisplayName("获取所有方法包括继承的")
        void testGetAllMethods() {
            List<Method> methods = MethodUtil.getAllMethods(ChildClass.class);
            assertThat(methods).isNotEmpty();
            List<String> methodNames = methods.stream().map(Method::getName).toList();
            assertThat(methodNames).contains("childMethod", "parentMethod");
        }

        @Test
        @DisplayName("结果被缓存")
        void testGetAllMethodsCached() {
            List<Method> methods1 = MethodUtil.getAllMethods(TestClass.class);
            List<Method> methods2 = MethodUtil.getAllMethods(TestClass.class);
            assertThat(methods1).isSameAs(methods2);
        }
    }

    @Nested
    @DisplayName("getMethod方法测试")
    class GetMethodTests {

        @Test
        @DisplayName("按名称和参数类型获取方法")
        void testGetMethod() {
            Method method = MethodUtil.getMethod(TestClass.class, "getName");
            assertThat(method).isNotNull();
            assertThat(method.getName()).isEqualTo("getName");
        }

        @Test
        @DisplayName("获取带参数的方法")
        void testGetMethodWithParams() {
            Method method = MethodUtil.getMethod(TestClass.class, "setName", String.class);
            assertThat(method).isNotNull();
        }

        @Test
        @DisplayName("方法不存在返回null")
        void testGetMethodNotFound() {
            Method method = MethodUtil.getMethod(TestClass.class, "nonexistent");
            assertThat(method).isNull();
        }
    }

    @Nested
    @DisplayName("getMethodOrThrow方法测试")
    class GetMethodOrThrowTests {

        @Test
        @DisplayName("获取存在的方法")
        void testGetMethodOrThrowExists() {
            Method method = MethodUtil.getMethodOrThrow(TestClass.class, "getName");
            assertThat(method).isNotNull();
        }

        @Test
        @DisplayName("方法不存在抛出异常")
        void testGetMethodOrThrowNotFound() {
            assertThatThrownBy(() -> MethodUtil.getMethodOrThrow(TestClass.class, "nonexistent"))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getMethodsByName方法测试")
    class GetMethodsByNameTests {

        @Test
        @DisplayName("按名称获取所有重载方法")
        void testGetMethodsByName() {
            List<Method> methods = MethodUtil.getMethodsByName(OverloadedClass.class, "overloaded");
            assertThat(methods).hasSizeGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("getMethods方法测试")
    class GetMethodsPredicateTests {

        @Test
        @DisplayName("按条件过滤方法")
        void testGetMethodsWithPredicate() {
            List<Method> methods = MethodUtil.getMethods(TestClass.class,
                    m -> m.getName().startsWith("get"));
            assertThat(methods).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getMethodsWithAnnotation方法测试")
    class GetMethodsWithAnnotationTests {

        @Test
        @DisplayName("获取带注解的方法")
        void testGetMethodsWithAnnotation() {
            List<Method> methods = MethodUtil.getMethodsWithAnnotation(AnnotatedMethodClass.class, Deprecated.class);
            assertThat(methods).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getMethodsWithReturnType方法测试")
    class GetMethodsWithReturnTypeTests {

        @Test
        @DisplayName("获取特定返回类型的方法")
        void testGetMethodsWithReturnType() {
            List<Method> methods = MethodUtil.getMethodsWithReturnType(TestClass.class, String.class);
            assertThat(methods).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getGetters方法测试")
    class GetGettersTests {

        @Test
        @DisplayName("获取getter方法")
        void testGetGetters() {
            List<Method> getters = MethodUtil.getGetters(TestClass.class);
            assertThat(getters).isNotEmpty();
            assertThat(getters).allSatisfy(m ->
                    assertThat(m.getName()).startsWith("get").satisfies(
                            n -> assertThat(Character.isUpperCase(n.charAt(3))).isTrue()
                    )
            );
        }
    }

    @Nested
    @DisplayName("getSetters方法测试")
    class GetSettersTests {

        @Test
        @DisplayName("获取setter方法")
        void testGetSetters() {
            List<Method> setters = MethodUtil.getSetters(TestClass.class);
            assertThat(setters).isNotEmpty();
            assertThat(setters).allSatisfy(m ->
                    assertThat(m.getName()).startsWith("set")
            );
        }
    }

    @Nested
    @DisplayName("invoke方法测试")
    class InvokeTests {

        @Test
        @DisplayName("调用方法")
        void testInvoke() throws Exception {
            TestClass obj = new TestClass();
            obj.setName("test");

            Method method = TestClass.class.getDeclaredMethod("getName");
            Object result = MethodUtil.invoke(method, obj);

            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("调用方法并转型")
        void testInvokeWithType() throws Exception {
            TestClass obj = new TestClass();
            obj.setName("test");

            Method method = TestClass.class.getDeclaredMethod("getName");
            String result = MethodUtil.invoke(method, obj, String.class);

            assertThat(result).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("invokeStatic方法测试")
    class InvokeStaticTests {

        @Test
        @DisplayName("调用静态方法")
        void testInvokeStatic() throws Exception {
            Method method = StaticMethodClass.class.getDeclaredMethod("staticMethod");
            Object result = MethodUtil.invokeStatic(method);
            assertThat(result).isEqualTo("static");
        }
    }

    @Nested
    @DisplayName("getReturnType方法测试")
    class GetReturnTypeTests {

        @Test
        @DisplayName("获取方法返回类型")
        void testGetReturnType() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("getName");
            assertThat(MethodUtil.getReturnType(method)).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getGenericReturnType方法测试")
    class GetGenericReturnTypeTests {

        @Test
        @DisplayName("获取方法泛型返回类型")
        void testGetGenericReturnType() throws Exception {
            Method method = GenericMethodClass.class.getDeclaredMethod("getList");
            Type genericType = MethodUtil.getGenericReturnType(method);
            assertThat(genericType.getTypeName()).contains("List");
        }
    }

    @Nested
    @DisplayName("getParameterTypes方法测试")
    class GetParameterTypesTests {

        @Test
        @DisplayName("获取方法参数类型")
        void testGetParameterTypes() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("setName", String.class);
            Class<?>[] paramTypes = MethodUtil.getParameterTypes(method);
            assertThat(paramTypes).containsExactly(String.class);
        }
    }

    @Nested
    @DisplayName("getParameterCount方法测试")
    class GetParameterCountTests {

        @Test
        @DisplayName("获取方法参数数量")
        void testGetParameterCount() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("setName", String.class);
            assertThat(MethodUtil.getParameterCount(method)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getMethodSignature方法测试")
    class GetMethodSignatureTests {

        @Test
        @DisplayName("获取方法签名")
        void testGetMethodSignature() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("setName", String.class);
            String sig = MethodUtil.getMethodSignature(method);
            assertThat(sig).contains("setName").contains("java.lang.String");
        }
    }

    @Nested
    @DisplayName("isGetter方法测试")
    class IsGetterTests {

        @Test
        @DisplayName("检查getter方法")
        void testIsGetter() throws Exception {
            Method getter = TestClass.class.getDeclaredMethod("getName");
            Method setter = TestClass.class.getDeclaredMethod("setName", String.class);

            assertThat(MethodUtil.isGetter(getter)).isTrue();
            assertThat(MethodUtil.isGetter(setter)).isFalse();
        }

        @Test
        @DisplayName("检查boolean的is方法")
        void testIsGetterBoolean() throws Exception {
            Method isMethod = BooleanClass.class.getDeclaredMethod("isActive");
            assertThat(MethodUtil.isGetter(isMethod)).isTrue();
        }
    }

    @Nested
    @DisplayName("isSetter方法测试")
    class IsSetterTests {

        @Test
        @DisplayName("检查setter方法")
        void testIsSetter() throws Exception {
            Method setter = TestClass.class.getDeclaredMethod("setName", String.class);
            Method getter = TestClass.class.getDeclaredMethod("getName");

            assertThat(MethodUtil.isSetter(setter)).isTrue();
            assertThat(MethodUtil.isSetter(getter)).isFalse();
        }
    }

    @Nested
    @DisplayName("getPropertyName方法测试")
    class GetPropertyNameTests {

        @Test
        @DisplayName("从getter获取属性名")
        void testGetPropertyNameFromGetter() throws Exception {
            Method getter = TestClass.class.getDeclaredMethod("getName");
            assertThat(MethodUtil.getPropertyName(getter)).isEqualTo("name");
        }

        @Test
        @DisplayName("从setter获取属性名")
        void testGetPropertyNameFromSetter() throws Exception {
            Method setter = TestClass.class.getDeclaredMethod("setName", String.class);
            assertThat(MethodUtil.getPropertyName(setter)).isEqualTo("name");
        }

        @Test
        @DisplayName("从is方法获取属性名")
        void testGetPropertyNameFromIs() throws Exception {
            Method isMethod = BooleanClass.class.getDeclaredMethod("isActive");
            assertThat(MethodUtil.getPropertyName(isMethod)).isEqualTo("active");
        }
    }

    @Nested
    @DisplayName("isStatic方法测试")
    class IsStaticTests {

        @Test
        @DisplayName("检查静态方法")
        void testIsStatic() throws Exception {
            Method staticMethod = StaticMethodClass.class.getDeclaredMethod("staticMethod");
            Method instanceMethod = TestClass.class.getDeclaredMethod("getName");

            assertThat(MethodUtil.isStatic(staticMethod)).isTrue();
            assertThat(MethodUtil.isStatic(instanceMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAbstract方法测试")
    class IsAbstractTests {

        @Test
        @DisplayName("检查抽象方法")
        void testIsAbstract() throws Exception {
            Method abstractMethod = AbstractClass.class.getDeclaredMethod("abstractMethod");
            Method concreteMethod = TestClass.class.getDeclaredMethod("getName");

            assertThat(MethodUtil.isAbstract(abstractMethod)).isTrue();
            assertThat(MethodUtil.isAbstract(concreteMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("isDefault方法测试")
    class IsDefaultTests {

        @Test
        @DisplayName("检查接口默认方法")
        void testIsDefault() throws Exception {
            Method defaultMethod = InterfaceWithDefault.class.getDeclaredMethod("defaultMethod");
            Method abstractMethod = InterfaceWithDefault.class.getDeclaredMethod("abstractMethod");

            assertThat(MethodUtil.isDefault(defaultMethod)).isTrue();
            assertThat(MethodUtil.isDefault(abstractMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("clearCache方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除所有缓存")
        void testClearCache() {
            MethodUtil.getAllMethods(TestClass.class);
            MethodUtil.clearCache();
            // Should not throw
        }

        @Test
        @DisplayName("清除特定类的缓存")
        void testClearCacheForClass() {
            MethodUtil.getAllMethods(TestClass.class);
            MethodUtil.clearCache(TestClass.class);
            // Should not throw
        }
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class TestClass {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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
    static class OverloadedClass {
        public void overloaded() {
        }

        public void overloaded(String arg) {
        }

        public void overloaded(int arg) {
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
    static class StaticMethodClass {
        public static String staticMethod() {
            return "static";
        }
    }

    @SuppressWarnings("unused")
    static class GenericMethodClass {
        public List<String> getList() {
            return null;
        }
    }

    @SuppressWarnings("unused")
    static class BooleanClass {
        private boolean active;

        public boolean isActive() {
            return active;
        }
    }

    @SuppressWarnings("unused")
    static abstract class AbstractClass {
        public abstract void abstractMethod();
    }

    @SuppressWarnings("unused")
    interface InterfaceWithDefault {
        default void defaultMethod() {
        }

        void abstractMethod();
    }
}
