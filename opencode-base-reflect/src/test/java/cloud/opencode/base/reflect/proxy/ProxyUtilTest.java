package cloud.opencode.base.reflect.proxy;

import org.junit.jupiter.api.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ProxyUtilTest Tests
 * ProxyUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ProxyUtil 测试")
class ProxyUtilTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = ProxyUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getProxyClass方法测试")
    class GetProxyClassTests {

        @Test
        @DisplayName("获取代理类")
        void testGetProxyClass() {
            ClassLoader loader = TestInterface.class.getClassLoader();
            Class<?> proxyClass = ProxyUtil.getProxyClass(loader, TestInterface.class);
            assertThat(Proxy.isProxyClass(proxyClass)).isTrue();
        }

        @Test
        @DisplayName("缓存代理类")
        void testGetProxyClassCached() {
            ClassLoader loader = TestInterface.class.getClassLoader();
            Class<?> c1 = ProxyUtil.getProxyClass(loader, TestInterface.class);
            Class<?> c2 = ProxyUtil.getProxyClass(loader, TestInterface.class);
            assertThat(c1).isSameAs(c2);
        }

        @Test
        @DisplayName("多接口代理类")
        void testGetProxyClassMultiple() {
            ClassLoader loader = TestInterface.class.getClassLoader();
            Class<?> proxyClass = ProxyUtil.getProxyClass(loader, TestInterface.class, AnotherInterface.class);
            assertThat(Proxy.isProxyClass(proxyClass)).isTrue();
        }
    }

    @Nested
    @DisplayName("newProxyInstance方法测试")
    class NewProxyInstanceTests {

        @Test
        @DisplayName("创建代理实例")
        void testNewProxyInstance() {
            ClassLoader loader = TestInterface.class.getClassLoader();
            InvocationHandler handler = (p, m, a) -> null;
            Object proxy = ProxyUtil.newProxyInstance(loader, new Class<?>[]{TestInterface.class}, handler);
            assertThat(proxy).isInstanceOf(TestInterface.class);
        }

        @Test
        @DisplayName("为单接口创建代理")
        void testNewProxyInstanceSingle() {
            InvocationHandler handler = (p, m, a) -> "result";
            TestInterface proxy = ProxyUtil.newProxyInstance(TestInterface.class, handler);
            assertThat(proxy.getValue()).isEqualTo("result");
        }
    }

    @Nested
    @DisplayName("isProxyClass方法测试")
    class IsProxyClassTests {

        @Test
        @DisplayName("代理类返回true")
        void testIsProxyClassTrue() {
            TestInterface proxy = createProxy();
            assertThat(ProxyUtil.isProxyClass(proxy.getClass())).isTrue();
        }

        @Test
        @DisplayName("普通类返回false")
        void testIsProxyClassFalse() {
            assertThat(ProxyUtil.isProxyClass(String.class)).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testIsProxyClassNull() {
            assertThat(ProxyUtil.isProxyClass(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isProxy方法测试")
    class IsProxyTests {

        @Test
        @DisplayName("代理对象返回true")
        void testIsProxyTrue() {
            TestInterface proxy = createProxy();
            assertThat(ProxyUtil.isProxy(proxy)).isTrue();
        }

        @Test
        @DisplayName("普通对象返回false")
        void testIsProxyFalse() {
            assertThat(ProxyUtil.isProxy("string")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testIsProxyNull() {
            assertThat(ProxyUtil.isProxy(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getInvocationHandler方法测试")
    class GetInvocationHandlerTests {

        @Test
        @DisplayName("获取调用处理器")
        void testGetInvocationHandler() {
            TestInterface proxy = createProxy();
            InvocationHandler handler = ProxyUtil.getInvocationHandler(proxy);
            assertThat(handler).isNotNull();
        }
    }

    @Nested
    @DisplayName("getInvocationHandlerSafe方法测试")
    class GetInvocationHandlerSafeTests {

        @Test
        @DisplayName("代理对象返回Optional.of")
        void testGetInvocationHandlerSafeProxy() {
            TestInterface proxy = createProxy();
            Optional<InvocationHandler> handler = ProxyUtil.getInvocationHandlerSafe(proxy);
            assertThat(handler).isPresent();
        }

        @Test
        @DisplayName("非代理返回Optional.empty")
        void testGetInvocationHandlerSafeNotProxy() {
            Optional<InvocationHandler> handler = ProxyUtil.getInvocationHandlerSafe("string");
            assertThat(handler).isEmpty();
        }
    }

    @Nested
    @DisplayName("getProxyInterfaces方法测试")
    class GetProxyInterfacesTests {

        @Test
        @DisplayName("获取代理接口数组")
        void testGetProxyInterfaces() {
            TestInterface proxy = createProxy();
            Class<?>[] interfaces = ProxyUtil.getProxyInterfaces(proxy);
            assertThat(interfaces).contains(TestInterface.class);
        }

        @Test
        @DisplayName("非代理返回空数组")
        void testGetProxyInterfacesNotProxy() {
            Class<?>[] interfaces = ProxyUtil.getProxyInterfaces("string");
            assertThat(interfaces).isEmpty();
        }
    }

    @Nested
    @DisplayName("getProxyInterfaceList方法测试")
    class GetProxyInterfaceListTests {

        @Test
        @DisplayName("获取代理接口列表")
        void testGetProxyInterfaceList() {
            TestInterface proxy = createProxy();
            List<Class<?>> interfaces = ProxyUtil.getProxyInterfaceList(proxy);
            assertThat(interfaces).contains(TestInterface.class);
        }
    }

    @Nested
    @DisplayName("implementsInterface方法测试")
    class ImplementsInterfaceTests {

        @Test
        @DisplayName("实现接口返回true")
        void testImplementsInterfaceTrue() {
            TestInterface proxy = createProxy();
            assertThat(ProxyUtil.implementsInterface(proxy, TestInterface.class)).isTrue();
        }

        @Test
        @DisplayName("未实现接口返回false")
        void testImplementsInterfaceFalse() {
            TestInterface proxy = createProxy();
            assertThat(ProxyUtil.implementsInterface(proxy, AnotherInterface.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isProxyable方法测试")
    class IsProxyableTests {

        @Test
        @DisplayName("接口可代理")
        void testIsProxyableTrue() {
            assertThat(ProxyUtil.isProxyable(TestInterface.class)).isTrue();
        }

        @Test
        @DisplayName("类不可代理")
        void testIsProxyableFalse() {
            assertThat(ProxyUtil.isProxyable(String.class)).isFalse();
        }

        @Test
        @DisplayName("null不可代理")
        void testIsProxyableNull() {
            assertThat(ProxyUtil.isProxyable(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("areProxyable方法测试")
    class AreProxyableTests {

        @Test
        @DisplayName("所有接口可代理")
        void testAreProxyableTrue() {
            assertThat(ProxyUtil.areProxyable(TestInterface.class, AnotherInterface.class)).isTrue();
        }

        @Test
        @DisplayName("包含类不可代理")
        void testAreProxyableFalse() {
            assertThat(ProxyUtil.areProxyable(TestInterface.class, String.class)).isFalse();
        }

        @Test
        @DisplayName("空数组不可代理")
        void testAreProxyableEmpty() {
            assertThat(ProxyUtil.areProxyable()).isFalse();
        }

        @Test
        @DisplayName("null不可代理")
        void testAreProxyableNull() {
            assertThat(ProxyUtil.areProxyable((Class<?>[]) null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isObjectMethod方法测试")
    class IsObjectMethodTests {

        @Test
        @DisplayName("Object方法返回true")
        void testIsObjectMethodTrue() throws NoSuchMethodException {
            Method method = Object.class.getMethod("toString");
            assertThat(ProxyUtil.isObjectMethod(method)).isTrue();
        }

        @Test
        @DisplayName("非Object方法返回false")
        void testIsObjectMethodFalse() throws NoSuchMethodException {
            Method method = TestInterface.class.getMethod("getValue");
            assertThat(ProxyUtil.isObjectMethod(method)).isFalse();
        }
    }

    @Nested
    @DisplayName("isEqualsMethod方法测试")
    class IsEqualsMethodTests {

        @Test
        @DisplayName("equals方法返回true")
        void testIsEqualsMethodTrue() throws NoSuchMethodException {
            Method method = Object.class.getMethod("equals", Object.class);
            assertThat(ProxyUtil.isEqualsMethod(method)).isTrue();
        }

        @Test
        @DisplayName("非equals方法返回false")
        void testIsEqualsMethodFalse() throws NoSuchMethodException {
            Method method = Object.class.getMethod("toString");
            assertThat(ProxyUtil.isEqualsMethod(method)).isFalse();
        }
    }

    @Nested
    @DisplayName("isHashCodeMethod方法测试")
    class IsHashCodeMethodTests {

        @Test
        @DisplayName("hashCode方法返回true")
        void testIsHashCodeMethodTrue() throws NoSuchMethodException {
            Method method = Object.class.getMethod("hashCode");
            assertThat(ProxyUtil.isHashCodeMethod(method)).isTrue();
        }

        @Test
        @DisplayName("非hashCode方法返回false")
        void testIsHashCodeMethodFalse() throws NoSuchMethodException {
            Method method = Object.class.getMethod("toString");
            assertThat(ProxyUtil.isHashCodeMethod(method)).isFalse();
        }
    }

    @Nested
    @DisplayName("isToStringMethod方法测试")
    class IsToStringMethodTests {

        @Test
        @DisplayName("toString方法返回true")
        void testIsToStringMethodTrue() throws NoSuchMethodException {
            Method method = Object.class.getMethod("toString");
            assertThat(ProxyUtil.isToStringMethod(method)).isTrue();
        }

        @Test
        @DisplayName("非toString方法返回false")
        void testIsToStringMethodFalse() throws NoSuchMethodException {
            Method method = Object.class.getMethod("hashCode");
            assertThat(ProxyUtil.isToStringMethod(method)).isFalse();
        }
    }

    @Nested
    @DisplayName("getDefaultReturnValue方法测试")
    class GetDefaultReturnValueTests {

        @Test
        @DisplayName("void返回null")
        void testDefaultVoid() {
            assertThat(ProxyUtil.getDefaultReturnValue(void.class)).isNull();
            assertThat(ProxyUtil.getDefaultReturnValue(Void.class)).isNull();
        }

        @Test
        @DisplayName("boolean返回false")
        void testDefaultBoolean() {
            assertThat(ProxyUtil.getDefaultReturnValue(boolean.class)).isEqualTo(false);
        }

        @Test
        @DisplayName("int返回0")
        void testDefaultInt() {
            assertThat(ProxyUtil.getDefaultReturnValue(int.class)).isEqualTo(0);
        }

        @Test
        @DisplayName("long返回0L")
        void testDefaultLong() {
            assertThat(ProxyUtil.getDefaultReturnValue(long.class)).isEqualTo(0L);
        }

        @Test
        @DisplayName("double返回0d")
        void testDefaultDouble() {
            assertThat(ProxyUtil.getDefaultReturnValue(double.class)).isEqualTo(0d);
        }

        @Test
        @DisplayName("float返回0f")
        void testDefaultFloat() {
            assertThat(ProxyUtil.getDefaultReturnValue(float.class)).isEqualTo(0f);
        }

        @Test
        @DisplayName("byte返回0")
        void testDefaultByte() {
            assertThat(ProxyUtil.getDefaultReturnValue(byte.class)).isEqualTo((byte) 0);
        }

        @Test
        @DisplayName("short返回0")
        void testDefaultShort() {
            assertThat(ProxyUtil.getDefaultReturnValue(short.class)).isEqualTo((short) 0);
        }

        @Test
        @DisplayName("char返回\\0")
        void testDefaultChar() {
            assertThat(ProxyUtil.getDefaultReturnValue(char.class)).isEqualTo('\0');
        }

        @Test
        @DisplayName("引用类型返回null")
        void testDefaultReference() {
            assertThat(ProxyUtil.getDefaultReturnValue(String.class)).isNull();
        }
    }

    @Nested
    @DisplayName("缓存管理测试")
    class CacheManagementTests {

        @Test
        @DisplayName("清除缓存")
        void testClearCache() {
            ClassLoader loader = TestInterface.class.getClassLoader();
            ProxyUtil.getProxyClass(loader, TestInterface.class);
            ProxyUtil.clearCache();
            assertThat(ProxyUtil.getCacheSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("获取缓存大小")
        void testGetCacheSize() {
            ProxyUtil.clearCache();
            ClassLoader loader = TestInterface.class.getClassLoader();
            ProxyUtil.getProxyClass(loader, TestInterface.class);
            assertThat(ProxyUtil.getCacheSize()).isGreaterThanOrEqualTo(1);
        }
    }

    // Helper methods
    private TestInterface createProxy() {
        InvocationHandler handler = (p, m, a) -> null;
        return ProxyUtil.newProxyInstance(TestInterface.class, handler);
    }

    // Test interfaces
    interface TestInterface {
        String getValue();
    }

    interface AnotherInterface {
        void doSomething();
    }
}
