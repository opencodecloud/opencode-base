package cloud.opencode.base.reflect.invokable;

import cloud.opencode.base.reflect.type.TypeToken;
import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ConstructorInvokableTest Tests
 * ConstructorInvokableTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ConstructorInvokable 测试")
class ConstructorInvokableTest {

    @Nested
    @DisplayName("getConstructor方法测试")
    class GetConstructorTests {

        @Test
        @DisplayName("获取底层Constructor")
        void testGetConstructor() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            ConstructorInvokable<String> invokable = (ConstructorInvokable<String>) Invokable.from(constructor);
            assertThat(invokable.getConstructor()).isSameAs(constructor);
        }
    }

    @Nested
    @DisplayName("getDeclaringClass方法测试")
    class GetDeclaringClassTests {

        @Test
        @DisplayName("获取声明类TypeToken")
        void testGetDeclaringClass() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            TypeToken<String> declaringClass = invokable.getDeclaringClass();
            assertThat(declaringClass.getRawType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getDeclaringClassRaw方法测试")
    class GetDeclaringClassRawTests {

        @Test
        @DisplayName("获取原始声明类")
        void testGetDeclaringClassRaw() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            assertThat(invokable.getDeclaringClassRaw()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getReturnType方法测试")
    class GetReturnTypeTests {

        @Test
        @DisplayName("返回类型是声明类")
        void testGetReturnType() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            TypeToken<? extends String> returnType = invokable.getReturnType();
            assertThat(returnType.getRawType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getParameters方法测试")
    class GetParametersTests {

        @Test
        @DisplayName("获取参数列表")
        void testGetParameters() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            List<Parameter> params = invokable.getParameters();
            assertThat(params).hasSize(1);
        }

        @Test
        @DisplayName("无参构造器返回空列表")
        void testGetParametersEmpty() throws Exception {
            Constructor<String> constructor = String.class.getConstructor();
            Invokable<String, String> invokable = Invokable.from(constructor);
            List<Parameter> params = invokable.getParameters();
            assertThat(params).isEmpty();
        }
    }

    @Nested
    @DisplayName("getParameterTypes方法测试")
    class GetParameterTypesTests {

        @Test
        @DisplayName("获取参数类型列表")
        void testGetParameterTypes() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(char[].class, int.class, int.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            List<TypeToken<?>> paramTypes = invokable.getParameterTypes();
            assertThat(paramTypes).hasSize(3);
        }
    }

    @Nested
    @DisplayName("getExceptionTypes方法测试")
    class GetExceptionTypesTests {

        @Test
        @DisplayName("获取异常类型列表")
        void testGetExceptionTypes() throws Exception {
            Constructor<TestClassWithException> constructor = TestClassWithException.class.getConstructor();
            Invokable<TestClassWithException, TestClassWithException> invokable = Invokable.from(constructor);
            List<TypeToken<? extends Throwable>> exceptionTypes = invokable.getExceptionTypes();
            assertThat(exceptionTypes).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getTypeParameters方法测试")
    class GetTypeParametersTests {

        @Test
        @DisplayName("获取类型参数")
        void testGetTypeParameters() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            TypeVariable<?>[] typeParams = invokable.getTypeParameters();
            assertThat(typeParams).isEmpty(); // String constructor has no type params
        }
    }

    @Nested
    @DisplayName("invoke方法测试")
    class InvokeTests {

        @Test
        @DisplayName("调用构造器创建实例")
        void testInvoke() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            String result = invokable.invoke(null, "test");
            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("调用无参构造器")
        void testInvokeNoArgs() throws Exception {
            Constructor<StringBuilder> constructor = StringBuilder.class.getConstructor();
            Invokable<StringBuilder, StringBuilder> invokable = Invokable.from(constructor);
            StringBuilder result = invokable.invoke(null);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("isVarArgs方法测试")
    class IsVarArgsTests {

        @Test
        @DisplayName("可变参数构造器返回true")
        void testIsVarArgsTrue() throws Exception {
            Constructor<VarArgsClass> constructor = VarArgsClass.class.getConstructor(String[].class);
            Invokable<VarArgsClass, VarArgsClass> invokable = Invokable.from(constructor);
            assertThat(invokable.isVarArgs()).isTrue();
        }

        @Test
        @DisplayName("非可变参数构造器返回false")
        void testIsVarArgsFalse() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            assertThat(invokable.isVarArgs()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSynthetic方法测试")
    class IsSyntheticTests {

        @Test
        @DisplayName("非合成构造器返回false")
        void testIsSyntheticFalse() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            assertThat(invokable.isSynthetic()).isFalse();
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("获取构造器名")
        void testGetName() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            assertThat(invokable.getName()).isEqualTo("java.lang.String");
        }
    }

    @Nested
    @DisplayName("getModifiers方法测试")
    class GetModifiersTests {

        @Test
        @DisplayName("获取修饰符")
        void testGetModifiers() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            assertThat(Modifier.isPublic(invokable.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同构造器相等")
        void testEquals() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable1 = Invokable.from(constructor);
            Invokable<String, String> invokable2 = Invokable.from(constructor);
            assertThat(invokable1).isEqualTo(invokable2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            assertThat(invokable).isEqualTo(invokable);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同构造器有相同hashCode")
        void testHashCode() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable1 = Invokable.from(constructor);
            Invokable<String, String> invokable2 = Invokable.from(constructor);
            assertThat(invokable1.hashCode()).isEqualTo(invokable2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含构造器信息")
        void testToString() throws Exception {
            Constructor<String> constructor = String.class.getConstructor(String.class);
            Invokable<String, String> invokable = Invokable.from(constructor);
            assertThat(invokable.toString()).contains("ConstructorInvokable");
        }
    }

    // Test helper classes
    static class TestClassWithException {
        public TestClassWithException() throws Exception {
        }
    }

    static class VarArgsClass {
        public VarArgsClass(String... args) {
        }
    }
}
