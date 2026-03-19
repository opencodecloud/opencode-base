package cloud.opencode.base.reflect.lambda;

import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * LambdaUtilTest Tests
 * LambdaUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("LambdaUtil 测试")
class LambdaUtilTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = LambdaUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getSerializedLambda方法测试")
    class GetSerializedLambdaTests {

        @Test
        @DisplayName("获取SerializedLambda")
        void testGetSerializedLambda() {
            SerializableFunction<String, Integer> lambda = String::length;
            var sl = LambdaUtil.getSerializedLambda(lambda);
            assertThat(sl).isNotNull();
        }
    }

    @Nested
    @DisplayName("getSerializedLambdaSafe方法测试")
    class GetSerializedLambdaSafeTests {

        @Test
        @DisplayName("安全获取SerializedLambda")
        void testGetSerializedLambdaSafe() {
            SerializableFunction<String, Integer> lambda = String::length;
            Optional<java.lang.invoke.SerializedLambda> result = LambdaUtil.getSerializedLambdaSafe(lambda);
            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("getImplClassName方法测试")
    class GetImplClassNameTests {

        @Test
        @DisplayName("获取实现类名")
        void testGetImplClassName() {
            SerializableFunction<String, Integer> lambda = String::length;
            String className = LambdaUtil.getImplClassName(lambda);
            assertThat(className).isEqualTo("java.lang.String");
        }
    }

    @Nested
    @DisplayName("getImplClass方法测试")
    class GetImplClassTests {

        @Test
        @DisplayName("获取实现类")
        void testGetImplClass() {
            SerializableFunction<String, Integer> lambda = String::length;
            Class<?> clazz = LambdaUtil.getImplClass(lambda);
            assertThat(clazz).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getImplMethodName方法测试")
    class GetImplMethodNameTests {

        @Test
        @DisplayName("获取实现方法名")
        void testGetImplMethodName() {
            SerializableFunction<String, Integer> lambda = String::length;
            String methodName = LambdaUtil.getImplMethodName(lambda);
            assertThat(methodName).isEqualTo("length");
        }
    }

    @Nested
    @DisplayName("getImplMethodSignature方法测试")
    class GetImplMethodSignatureTests {

        @Test
        @DisplayName("获取实现方法签名")
        void testGetImplMethodSignature() {
            SerializableFunction<String, Integer> lambda = String::length;
            String signature = LambdaUtil.getImplMethodSignature(lambda);
            assertThat(signature).isNotNull();
        }
    }

    @Nested
    @DisplayName("getFunctionalInterfaceClassName方法测试")
    class GetFunctionalInterfaceClassNameTests {

        @Test
        @DisplayName("获取函数式接口类名")
        void testGetFunctionalInterfaceClassName() {
            SerializableFunction<String, Integer> lambda = String::length;
            String className = LambdaUtil.getFunctionalInterfaceClassName(lambda);
            assertThat(className).contains("SerializableFunction");
        }
    }

    @Nested
    @DisplayName("getFunctionalInterfaceMethodName方法测试")
    class GetFunctionalInterfaceMethodNameTests {

        @Test
        @DisplayName("获取函数式接口方法名")
        void testGetFunctionalInterfaceMethodName() {
            SerializableFunction<String, Integer> lambda = String::length;
            String methodName = LambdaUtil.getFunctionalInterfaceMethodName(lambda);
            assertThat(methodName).isEqualTo("apply");
        }
    }

    @Nested
    @DisplayName("getCapturedArgCount方法测试")
    class GetCapturedArgCountTests {

        @Test
        @DisplayName("获取捕获参数数量")
        void testGetCapturedArgCount() {
            SerializableFunction<String, Integer> lambda = String::length;
            int count = LambdaUtil.getCapturedArgCount(lambda);
            assertThat(count).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("isMethodReference方法测试")
    class IsMethodReferenceTests {

        @Test
        @DisplayName("方法引用返回true")
        void testIsMethodReferenceTrue() {
            SerializableFunction<String, Integer> lambda = String::length;
            assertThat(LambdaUtil.isMethodReference(lambda)).isTrue();
        }

        @Test
        @DisplayName("lambda表达式返回false")
        void testIsMethodReferenceFalse() {
            SerializableFunction<String, Integer> lambda = s -> s.length();
            assertThat(LambdaUtil.isMethodReference(lambda)).isFalse();
        }
    }

    @Nested
    @DisplayName("getImplMethodKind方法测试")
    class GetImplMethodKindTests {

        @Test
        @DisplayName("获取实现方法种类")
        void testGetImplMethodKind() {
            SerializableFunction<String, Integer> lambda = String::length;
            int kind = LambdaUtil.getImplMethodKind(lambda);
            assertThat(kind).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("isStaticMethodReference方法测试")
    class IsStaticMethodReferenceTests {

        @Test
        @DisplayName("静态方法引用返回true")
        void testIsStaticMethodReferenceTrue() {
            SerializableFunction<String, Integer> lambda = Integer::parseInt;
            assertThat(LambdaUtil.isStaticMethodReference(lambda)).isTrue();
        }

        @Test
        @DisplayName("实例方法引用返回false")
        void testIsStaticMethodReferenceFalse() {
            SerializableFunction<String, Integer> lambda = String::length;
            assertThat(LambdaUtil.isStaticMethodReference(lambda)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractPropertyName方法测试")
    class ExtractPropertyNameTests {

        @Test
        @DisplayName("从getter提取属性名")
        void testExtractPropertyNameFromGetter() {
            SerializableFunction<TestBean, String> getter = TestBean::getName;
            String name = LambdaUtil.extractPropertyName(getter);
            assertThat(name).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("extractPropertyNameFromMethodName方法测试")
    class ExtractPropertyNameFromMethodNameTests {

        @Test
        @DisplayName("从get前缀提取")
        void testExtractFromGet() {
            assertThat(LambdaUtil.extractPropertyNameFromMethodName("getName")).isEqualTo("name");
        }

        @Test
        @DisplayName("从is前缀提取")
        void testExtractFromIs() {
            assertThat(LambdaUtil.extractPropertyNameFromMethodName("isActive")).isEqualTo("active");
        }

        @Test
        @DisplayName("从set前缀提取")
        void testExtractFromSet() {
            assertThat(LambdaUtil.extractPropertyNameFromMethodName("setName")).isEqualTo("name");
        }

        @Test
        @DisplayName("无前缀返回原名")
        void testExtractNoPrefix() {
            assertThat(LambdaUtil.extractPropertyNameFromMethodName("calculate")).isEqualTo("calculate");
        }
    }

    @Nested
    @DisplayName("getImplMethod方法测试")
    class GetImplMethodTests {

        @Test
        @DisplayName("获取实现方法")
        void testGetImplMethod() {
            SerializableFunction<String, Integer> lambda = String::length;
            Method method = LambdaUtil.getImplMethod(lambda);
            assertThat(method).isNotNull();
        }
    }

    @Nested
    @DisplayName("getImplMethodSafe方法测试")
    class GetImplMethodSafeTests {

        @Test
        @DisplayName("安全获取实现方法")
        void testGetImplMethodSafe() {
            SerializableFunction<String, Integer> lambda = String::length;
            Optional<Method> method = LambdaUtil.getImplMethodSafe(lambda);
            assertThat(method).isPresent();
        }
    }

    @Nested
    @DisplayName("缓存管理测试")
    class CacheManagementTests {

        @Test
        @DisplayName("获取缓存大小")
        void testGetCacheSize() {
            LambdaUtil.clearCache();
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaUtil.getSerializedLambda(lambda);
            assertThat(LambdaUtil.getCacheSize()).isGreaterThan(0);
        }

        @Test
        @DisplayName("清除缓存")
        void testClearCache() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaUtil.getSerializedLambda(lambda);
            LambdaUtil.clearCache();
            assertThat(LambdaUtil.getCacheSize()).isZero();
        }
    }

    // Test helper class
    static class TestBean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
