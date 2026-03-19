package cloud.opencode.base.reflect.invokable;

import cloud.opencode.base.reflect.type.TypeToken;
import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * MethodInvokableTest Tests
 * MethodInvokableTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("MethodInvokable 测试")
class MethodInvokableTest {

    @Nested
    @DisplayName("getMethod方法测试")
    class GetMethodTests {

        @Test
        @DisplayName("获取底层Method")
        void testGetMethod() throws Exception {
            Method method = String.class.getMethod("length");
            MethodInvokable<?> invokable = (MethodInvokable<?>) Invokable.from(method);
            assertThat(invokable.getMethod()).isSameAs(method);
        }
    }

    @Nested
    @DisplayName("getDeclaringClass方法测试")
    class GetDeclaringClassTests {

        @Test
        @DisplayName("获取声明类TypeToken")
        void testGetDeclaringClass() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
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
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            assertThat(invokable.getDeclaringClassRaw()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getReturnType方法测试")
    class GetReturnTypeTests {

        @Test
        @DisplayName("获取返回类型")
        void testGetReturnType() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            TypeToken<?> returnType = invokable.getReturnType();
            assertThat(returnType.getRawType()).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("getParameters方法测试")
    class GetParametersTests {

        @Test
        @DisplayName("获取参数列表")
        void testGetParameters() throws Exception {
            Method method = String.class.getMethod("substring", int.class, int.class);
            Invokable<String, Object> invokable = Invokable.from(method);
            List<Parameter> params = invokable.getParameters();
            assertThat(params).hasSize(2);
        }

        @Test
        @DisplayName("无参方法返回空列表")
        void testGetParametersEmpty() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
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
            Method method = String.class.getMethod("substring", int.class, int.class);
            Invokable<String, Object> invokable = Invokable.from(method);
            List<TypeToken<?>> paramTypes = invokable.getParameterTypes();
            assertThat(paramTypes).hasSize(2);
            assertThat(paramTypes.get(0).getRawType()).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("getExceptionTypes方法测试")
    class GetExceptionTypesTests {

        @Test
        @DisplayName("获取异常类型列表")
        void testGetExceptionTypes() throws Exception {
            Method method = Class.class.getMethod("forName", String.class);
            Invokable<Class, Object> invokable = Invokable.from(method);
            List<TypeToken<? extends Throwable>> exceptionTypes = invokable.getExceptionTypes();
            assertThat(exceptionTypes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getTypeParameters方法测试")
    class GetTypeParametersTests {

        @Test
        @DisplayName("获取类型参数")
        void testGetTypeParameters() throws Exception {
            Method method = java.util.Arrays.class.getMethod("asList", Object[].class);
            Invokable<?, Object> invokable = Invokable.from(method);
            TypeVariable<?>[] typeParams = invokable.getTypeParameters();
            assertThat(typeParams).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("invoke方法测试")
    class InvokeTests {

        @Test
        @DisplayName("调用方法")
        void testInvoke() throws Exception {
            Method method = String.class.getMethod("substring", int.class, int.class);
            Invokable<String, Object> invokable = Invokable.from(method);
            Object result = invokable.invoke("hello", 1, 4);
            assertThat(result).isEqualTo("ell");
        }
    }

    @Nested
    @DisplayName("isVarArgs方法测试")
    class IsVarArgsTests {

        @Test
        @DisplayName("可变参数方法返回true")
        void testIsVarArgsTrue() throws Exception {
            Method method = String.class.getMethod("format", String.class, Object[].class);
            Invokable<?, Object> invokable = Invokable.from(method);
            assertThat(invokable.isVarArgs()).isTrue();
        }

        @Test
        @DisplayName("非可变参数方法返回false")
        void testIsVarArgsFalse() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            assertThat(invokable.isVarArgs()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSynthetic方法测试")
    class IsSyntheticTests {

        @Test
        @DisplayName("非合成方法返回false")
        void testIsSyntheticFalse() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            assertThat(invokable.isSynthetic()).isFalse();
        }
    }

    @Nested
    @DisplayName("isBridge方法测试")
    class IsBridgeTests {

        @Test
        @DisplayName("非桥接方法返回false")
        void testIsBridgeFalse() throws Exception {
            Method method = String.class.getMethod("length");
            MethodInvokable<?> invokable = (MethodInvokable<?>) Invokable.from(method);
            assertThat(invokable.isBridge()).isFalse();
        }
    }

    @Nested
    @DisplayName("isDefault方法测试")
    class IsDefaultTests {

        @Test
        @DisplayName("默认方法返回true")
        void testIsDefaultTrue() throws Exception {
            Method method = java.util.List.class.getMethod("spliterator");
            MethodInvokable<?> invokable = (MethodInvokable<?>) Invokable.from(method);
            assertThat(invokable.isDefault()).isTrue();
        }

        @Test
        @DisplayName("非默认方法返回false")
        void testIsDefaultFalse() throws Exception {
            Method method = String.class.getMethod("length");
            MethodInvokable<?> invokable = (MethodInvokable<?>) Invokable.from(method);
            assertThat(invokable.isDefault()).isFalse();
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("获取方法名")
        void testGetName() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            assertThat(invokable.getName()).isEqualTo("length");
        }
    }

    @Nested
    @DisplayName("getModifiers方法测试")
    class GetModifiersTests {

        @Test
        @DisplayName("获取修饰符")
        void testGetModifiers() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            assertThat(Modifier.isPublic(invokable.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同方法相等")
        void testEquals() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable1 = Invokable.from(method);
            Invokable<String, Object> invokable2 = Invokable.from(method);
            assertThat(invokable1).isEqualTo(invokable2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            assertThat(invokable).isEqualTo(invokable);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同方法有相同hashCode")
        void testHashCode() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable1 = Invokable.from(method);
            Invokable<String, Object> invokable2 = Invokable.from(method);
            assertThat(invokable1.hashCode()).isEqualTo(invokable2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含方法信息")
        void testToString() throws Exception {
            Method method = String.class.getMethod("length");
            Invokable<String, Object> invokable = Invokable.from(method);
            assertThat(invokable.toString()).contains("MethodInvokable");
        }
    }
}
