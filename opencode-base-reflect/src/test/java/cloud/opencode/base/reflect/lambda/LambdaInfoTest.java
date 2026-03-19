package cloud.opencode.base.reflect.lambda;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * LambdaInfoTest Tests
 * LambdaInfoTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("LambdaInfo 测试")
class LambdaInfoTest {

    @Nested
    @DisplayName("from工厂方法测试")
    class FromFactoryTests {

        @Test
        @DisplayName("从lambda创建LambdaInfo")
        void testFromLambda() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info).isNotNull();
        }
    }

    @Nested
    @DisplayName("getFunctionalInterfaceClassName方法测试")
    class GetFunctionalInterfaceClassNameTests {

        @Test
        @DisplayName("获取函数式接口类名")
        void testGetFunctionalInterfaceClassName() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info.getFunctionalInterfaceClassName()).contains("SerializableFunction");
        }
    }

    @Nested
    @DisplayName("getFunctionalInterfaceMethodName方法测试")
    class GetFunctionalInterfaceMethodNameTests {

        @Test
        @DisplayName("获取函数式接口方法名")
        void testGetFunctionalInterfaceMethodName() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info.getFunctionalInterfaceMethodName()).isEqualTo("apply");
        }
    }

    @Nested
    @DisplayName("getImplClassName方法测试")
    class GetImplClassNameTests {

        @Test
        @DisplayName("获取实现类名")
        void testGetImplClassName() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info.getImplClassName()).isEqualTo("java.lang.String");
        }
    }

    @Nested
    @DisplayName("getImplMethodName方法测试")
    class GetImplMethodNameTests {

        @Test
        @DisplayName("获取实现方法名")
        void testGetImplMethodName() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info.getImplMethodName()).isEqualTo("length");
        }
    }

    @Nested
    @DisplayName("getImplClass方法测试")
    class GetImplClassTests {

        @Test
        @DisplayName("获取实现类")
        void testGetImplClass() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info.getImplClass()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getImplMethod方法测试")
    class GetImplMethodTests {

        @Test
        @DisplayName("获取实现方法")
        void testGetImplMethod() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            Method method = info.getImplMethod();
            assertThat(method).isNotNull();
        }
    }

    @Nested
    @DisplayName("getCapturedArgCount方法测试")
    class GetCapturedArgCountTests {

        @Test
        @DisplayName("获取捕获参数数量")
        void testGetCapturedArgCount() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info.getCapturedArgCount()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getCapturedArgs方法测试")
    class GetCapturedArgsTests {

        @Test
        @DisplayName("获取所有捕获参数")
        void testGetCapturedArgs() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            Object[] args = info.getCapturedArgs();
            assertThat(args).isNotNull();
        }
    }

    @Nested
    @DisplayName("getSerializedLambda方法测试")
    class GetSerializedLambdaTests {

        @Test
        @DisplayName("获取底层SerializedLambda")
        void testGetSerializedLambda() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info.getSerializedLambda()).isNotNull();
        }
    }

    @Nested
    @DisplayName("isMethodReference方法测试")
    class IsMethodReferenceTests {

        @Test
        @DisplayName("方法引用返回true")
        void testIsMethodReferenceTrue() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info.isMethodReference()).isTrue();
        }

        @Test
        @DisplayName("lambda表达式返回false")
        void testIsMethodReferenceFalse() {
            SerializableFunction<String, Integer> lambda = s -> s.length();
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info.isMethodReference()).isFalse();
        }
    }

    @Nested
    @DisplayName("getImplMethodKind方法测试")
    class GetImplMethodKindTests {

        @Test
        @DisplayName("获取实现方法类型")
        void testGetImplMethodKind() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            assertThat(info.getImplMethodKind()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含类名和方法名")
        void testToString() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = LambdaInfo.from(lambda);
            String str = info.toString();
            assertThat(str).contains("LambdaInfo");
            assertThat(str).contains("String");
            assertThat(str).contains("length");
        }
    }
}
