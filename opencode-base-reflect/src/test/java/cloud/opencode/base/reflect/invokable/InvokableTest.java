package cloud.opencode.base.reflect.invokable;

import cloud.opencode.base.reflect.type.TypeToken;
import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * InvokableTest Tests
 * InvokableTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("Invokable 测试")
class InvokableTest {

    @Nested
    @DisplayName("from Method工厂方法测试")
    class FromMethodTests {

        @Test
        @DisplayName("从Method创建Invokable")
        void testFromMethod() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            assertThat(invokable).isNotNull();
            assertThat(invokable).isInstanceOf(MethodInvokable.class);
        }
    }

    @Nested
    @DisplayName("from Constructor工厂方法测试")
    class FromConstructorTests {

        @Test
        @DisplayName("从Constructor创建Invokable")
        void testFromConstructor() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            assertThat(invokable).isNotNull();
            assertThat(invokable).isInstanceOf(ConstructorInvokable.class);
        }
    }

    @Nested
    @DisplayName("invoke方法测试")
    class InvokeTests {

        @Test
        @DisplayName("调用方法")
        void testInvokeMethod() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            Object result = invokable.invoke("test");
            assertThat(result).isEqualTo(4);
        }

        @Test
        @DisplayName("调用构造器")
        void testInvokeConstructor() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            String result = invokable.invoke(null, "test");
            assertThat(result).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("invokeForced方法测试")
    class InvokeForcedTests {

        @Test
        @DisplayName("强制调用方法")
        void testInvokeForced() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            Object result = invokable.invokeForced("test");
            assertThat(result).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("invokeSafe方法测试")
    class InvokeSafeTests {

        @Test
        @DisplayName("安全调用成功")
        void testInvokeSafeSuccess() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            Optional<Object> result = invokable.invokeSafe("test");
            assertThat(result).contains(4);
        }

        @Test
        @DisplayName("安全调用失败返回空")
        void testInvokeSafeFailure() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            Optional<Object> result = invokable.invokeSafe(null);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("setAccessible方法测试")
    class SetAccessibleTests {

        @Test
        @DisplayName("设置可访问性")
        void testSetAccessible() throws Exception {
            Method method = String.class.getDeclaredMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            Invokable<String, Object> result = invokable.setAccessible(true);
            assertThat(result).isSameAs(invokable);
        }
    }

    @Nested
    @DisplayName("修饰符方法测试")
    class ModifierTests {

        @Test
        @DisplayName("检查isPublic")
        void testIsPublic() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            assertThat(invokable.isPublic()).isTrue();
        }

        @Test
        @DisplayName("检查isPrivate")
        void testIsPrivate() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("privateMethod");
            Invokable<TestClass, Object> invokable = Invokable.from(method);
            assertThat(invokable.isPrivate()).isTrue();
        }

        @Test
        @DisplayName("检查isProtected")
        void testIsProtected() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("protectedMethod");
            Invokable<TestClass, Object> invokable = Invokable.from(method);
            assertThat(invokable.isProtected()).isTrue();
        }

        @Test
        @DisplayName("检查isPackagePrivate")
        void testIsPackagePrivate() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("packageMethod");
            Invokable<TestClass, Object> invokable = Invokable.from(method);
            assertThat(invokable.isPackagePrivate()).isTrue();
        }

        @Test
        @DisplayName("检查isStatic")
        void testIsStatic() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("staticMethod");
            Invokable<TestClass, Object> invokable = Invokable.from(method);
            assertThat(invokable.isStatic()).isTrue();
        }

        @Test
        @DisplayName("检查isFinal")
        void testIsFinal() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("finalMethod");
            Invokable<TestClass, Object> invokable = Invokable.from(method);
            assertThat(invokable.isFinal()).isTrue();
        }

        @Test
        @DisplayName("检查isAbstract")
        void testIsAbstract() throws Exception {
            Method method = AbstractClass.class.getDeclaredMethod("abstractMethod");
            Invokable<AbstractClass, Object> invokable = Invokable.from(method);
            assertThat(invokable.isAbstract()).isTrue();
        }

        @Test
        @DisplayName("检查isSynchronized")
        void testIsSynchronized() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("synchronizedMethod");
            Invokable<TestClass, Object> invokable = Invokable.from(method);
            assertThat(invokable.isSynchronized()).isTrue();
        }

        @Test
        @DisplayName("检查isOverridable")
        void testIsOverridable() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("publicMethod");
            Invokable<TestClass, Object> invokable = Invokable.from(method);
            assertThat(invokable.isOverridable()).isTrue();
        }
    }

    @Nested
    @DisplayName("注解方法测试")
    class AnnotationTests {

        @Test
        @DisplayName("检查注解是否存在")
        void testIsAnnotationPresent() throws Exception {
            Method method = TestClass.class.getMethod("annotatedMethod");
            Invokable<TestClass, Object> invokable = Invokable.from(method);
            assertThat(invokable.isAnnotationPresent(Deprecated.class)).isTrue();
        }

        @Test
        @DisplayName("获取注解")
        void testGetAnnotation() throws Exception {
            Method method = TestClass.class.getMethod("annotatedMethod");
            Invokable<TestClass, Object> invokable = Invokable.from(method);
            Deprecated annotation = invokable.getAnnotation(Deprecated.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("获取所有注解")
        void testGetAnnotations() throws Exception {
            Method method = TestClass.class.getMethod("annotatedMethod");
            Invokable<TestClass, Object> invokable = Invokable.from(method);
            assertThat(invokable.getAnnotations()).isNotEmpty();
        }
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class TestClass {
        private void privateMethod() {}
        protected void protectedMethod() {}
        void packageMethod() {}
        public void publicMethod() {}
        public static void staticMethod() {}
        public final void finalMethod() {}
        public synchronized void synchronizedMethod() {}

        @Deprecated
        public void annotatedMethod() {}
    }

    @SuppressWarnings("unused")
    static abstract class AbstractClass {
        abstract void abstractMethod();
    }
}
