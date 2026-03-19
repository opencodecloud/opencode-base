package cloud.opencode.base.reflect.lambda;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializedLambdaWrapperTest Tests
 * SerializedLambdaWrapperTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("SerializedLambdaWrapper 测试")
class SerializedLambdaWrapperTest {

    @Nested
    @DisplayName("from工厂方法测试")
    class FromFactoryTests {

        @Test
        @DisplayName("从lambda创建包装器")
        void testFrom() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper).isNotNull();
        }
    }

    @Nested
    @DisplayName("fromSafe工厂方法测试")
    class FromSafeTests {

        @Test
        @DisplayName("安全创建包装器")
        void testFromSafe() {
            SerializableFunction<String, Integer> lambda = String::length;
            Optional<SerializedLambdaWrapper> result = SerializedLambdaWrapper.fromSafe(lambda);
            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("getCapturingClass方法测试")
    class GetCapturingClassTests {

        @Test
        @DisplayName("获取捕获类")
        void testGetCapturingClass() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getCapturingClass()).contains("SerializedLambdaWrapperTest");
        }
    }

    @Nested
    @DisplayName("getFunctionalInterfaceClass方法测试")
    class GetFunctionalInterfaceClassTests {

        @Test
        @DisplayName("获取函数式接口类")
        void testGetFunctionalInterfaceClass() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getFunctionalInterfaceClass()).contains("SerializableFunction");
        }
    }

    @Nested
    @DisplayName("getFunctionalInterfaceMethodName方法测试")
    class GetFunctionalInterfaceMethodNameTests {

        @Test
        @DisplayName("获取函数式接口方法名")
        void testGetFunctionalInterfaceMethodName() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getFunctionalInterfaceMethodName()).isEqualTo("apply");
        }
    }

    @Nested
    @DisplayName("getFunctionalInterfaceMethodSignature方法测试")
    class GetFunctionalInterfaceMethodSignatureTests {

        @Test
        @DisplayName("获取函数式接口方法签名")
        void testGetFunctionalInterfaceMethodSignature() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getFunctionalInterfaceMethodSignature()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getImplClass方法测试")
    class GetImplClassTests {

        @Test
        @DisplayName("获取实现类")
        void testGetImplClass() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getImplClass()).isEqualTo("java.lang.String");
        }
    }

    @Nested
    @DisplayName("getImplMethodName方法测试")
    class GetImplMethodNameTests {

        @Test
        @DisplayName("获取实现方法名")
        void testGetImplMethodName() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getImplMethodName()).isEqualTo("length");
        }
    }

    @Nested
    @DisplayName("getImplMethodSignature方法测试")
    class GetImplMethodSignatureTests {

        @Test
        @DisplayName("获取实现方法签名")
        void testGetImplMethodSignature() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getImplMethodSignature()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getImplMethodKind方法测试")
    class GetImplMethodKindTests {

        @Test
        @DisplayName("获取实现方法种类")
        void testGetImplMethodKind() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getImplMethodKind()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("getInstantiatedMethodType方法测试")
    class GetInstantiatedMethodTypeTests {

        @Test
        @DisplayName("获取实例化方法类型")
        void testGetInstantiatedMethodType() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getInstantiatedMethodType()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getCapturedArgCount方法测试")
    class GetCapturedArgCountTests {

        @Test
        @DisplayName("获取捕获参数数量")
        void testGetCapturedArgCount() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getCapturedArgCount()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("isMethodReference方法测试")
    class IsMethodReferenceTests {

        @Test
        @DisplayName("方法引用返回true")
        void testIsMethodReferenceTrue() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.isMethodReference()).isTrue();
        }

        @Test
        @DisplayName("lambda表达式返回false")
        void testIsMethodReferenceFalse() {
            SerializableFunction<String, Integer> lambda = s -> s.length();
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.isMethodReference()).isFalse();
        }
    }

    @Nested
    @DisplayName("isStaticMethodReference方法测试")
    class IsStaticMethodReferenceTests {

        @Test
        @DisplayName("静态方法引用返回true")
        void testIsStaticMethodReferenceTrue() {
            SerializableFunction<String, Integer> lambda = Integer::parseInt;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.isStaticMethodReference()).isTrue();
        }

        @Test
        @DisplayName("实例方法引用返回false")
        void testIsStaticMethodReferenceFalse() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.isStaticMethodReference()).isFalse();
        }
    }

    @Nested
    @DisplayName("isInstanceMethodReference方法测试")
    class IsInstanceMethodReferenceTests {

        @Test
        @DisplayName("实例方法引用返回true")
        void testIsInstanceMethodReferenceTrue() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.isInstanceMethodReference()).isTrue();
        }
    }

    @Nested
    @DisplayName("getPropertyName方法测试")
    class GetPropertyNameTests {

        @Test
        @DisplayName("获取属性名")
        void testGetPropertyName() {
            SerializableFunction<TestBean, String> getter = TestBean::getName;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(getter);
            assertThat(wrapper.getPropertyName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getImplClassAsClass方法测试")
    class GetImplClassAsClassTests {

        @Test
        @DisplayName("获取实现类作为Class对象")
        void testGetImplClassAsClass() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.getImplClassAsClass()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getImplMethod方法测试")
    class GetImplMethodTests {

        @Test
        @DisplayName("获取实现方法")
        void testGetImplMethod() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            Optional<Method> method = wrapper.getImplMethod();
            assertThat(method).isPresent();
        }
    }

    @Nested
    @DisplayName("unwrap方法测试")
    class UnwrapTests {

        @Test
        @DisplayName("获取底层SerializedLambda")
        void testUnwrap() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            assertThat(wrapper.unwrap()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            SerializableFunction<String, Integer> lambda = String::length;
            SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.from(lambda);
            String str = wrapper.toString();
            assertThat(str).contains("SerializedLambdaWrapper");
            assertThat(str).contains("implClass");
            assertThat(str).contains("implMethodName");
        }
    }

    // Test helper class
    static class TestBean {
        private String name;

        public String getName() {
            return name;
        }
    }
}
