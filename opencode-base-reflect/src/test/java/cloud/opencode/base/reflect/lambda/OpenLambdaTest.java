package cloud.opencode.base.reflect.lambda;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenLambdaTest Tests
 * OpenLambdaTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenLambda 测试")
class OpenLambdaTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenLambda.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getInfo方法测试")
    class GetInfoTests {

        @Test
        @DisplayName("获取lambda信息")
        void testGetInfo() {
            SerializableFunction<String, Integer> lambda = String::length;
            LambdaInfo info = OpenLambda.getInfo(lambda);
            assertThat(info).isNotNull();
        }
    }

    @Nested
    @DisplayName("getImplMethod方法测试")
    class GetImplMethodTests {

        @Test
        @DisplayName("获取实现方法")
        void testGetImplMethod() {
            SerializableFunction<String, Integer> lambda = String::length;
            Optional<Method> method = OpenLambda.getImplMethod(lambda);
            assertThat(method).isPresent();
        }
    }

    @Nested
    @DisplayName("getImplMethodName方法测试")
    class GetImplMethodNameTests {

        @Test
        @DisplayName("获取实现方法名")
        void testGetImplMethodName() {
            SerializableFunction<String, Integer> lambda = String::length;
            String name = OpenLambda.getImplMethodName(lambda);
            assertThat(name).isEqualTo("length");
        }
    }

    @Nested
    @DisplayName("getImplClass方法测试")
    class GetImplClassTests {

        @Test
        @DisplayName("获取实现类")
        void testGetImplClass() {
            SerializableFunction<String, Integer> lambda = String::length;
            Class<?> clazz = OpenLambda.getImplClass(lambda);
            assertThat(clazz).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("isMethodReference方法测试")
    class IsMethodReferenceTests {

        @Test
        @DisplayName("方法引用返回true")
        void testIsMethodReferenceTrue() {
            SerializableFunction<String, Integer> lambda = String::length;
            assertThat(OpenLambda.isMethodReference(lambda)).isTrue();
        }

        @Test
        @DisplayName("lambda表达式返回false")
        void testIsMethodReferenceFalse() {
            SerializableFunction<String, Integer> lambda = s -> s.length();
            assertThat(OpenLambda.isMethodReference(lambda)).isFalse();
        }
    }

    @Nested
    @DisplayName("getPropertyName方法测试")
    class GetPropertyNameTests {

        @Test
        @DisplayName("从getter获取属性名")
        void testGetPropertyNameFromGetter() {
            SerializableFunction<TestBean, String> getter = TestBean::getName;
            String name = OpenLambda.getPropertyName(getter);
            assertThat(name).isEqualTo("name");
        }

        @Test
        @DisplayName("从is前缀getter获取属性名")
        void testGetPropertyNameFromIsGetter() {
            SerializableFunction<TestBean, Boolean> getter = TestBean::isActive;
            String name = OpenLambda.getPropertyName(getter);
            assertThat(name).isEqualTo("active");
        }
    }

    @Nested
    @DisplayName("getPropertyNameFromSetter方法测试")
    class GetPropertyNameFromSetterTests {

        @Test
        @DisplayName("从setter获取属性名")
        void testGetPropertyNameFromSetter() {
            SerializableConsumer<TestBean> setter = b -> b.setName("test");
            // Note: Lambda expressions don't have the setter method name
            // This test verifies the method doesn't throw
            assertThatCode(() -> OpenLambda.getPropertyNameFromSetter(setter)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getPropertyClass方法测试")
    class GetPropertyClassTests {

        @Test
        @DisplayName("获取属性类")
        void testGetPropertyClass() {
            SerializableFunction<TestBean, String> getter = TestBean::getName;
            Class<?> propClass = OpenLambda.getPropertyClass(getter);
            assertThat(propClass).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("isFunctionalInterface方法测试")
    class IsFunctionalInterfaceTests {

        @Test
        @DisplayName("函数式接口返回true")
        void testIsFunctionalInterfaceTrue() {
            assertThat(OpenLambda.isFunctionalInterface(Function.class)).isTrue();
        }

        @Test
        @DisplayName("非函数式接口返回false")
        void testIsFunctionalInterfaceFalse() {
            assertThat(OpenLambda.isFunctionalInterface(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getSingleAbstractMethod方法测试")
    class GetSingleAbstractMethodTests {

        @Test
        @DisplayName("获取单一抽象方法")
        void testGetSingleAbstractMethod() {
            Optional<Method> sam = OpenLambda.getSingleAbstractMethod(Function.class);
            assertThat(sam).isPresent();
            assertThat(sam.get().getName()).isEqualTo("apply");
        }
    }

    @Nested
    @DisplayName("classify方法测试")
    class ClassifyTests {

        @Test
        @DisplayName("分类函数式接口")
        void testClassify() {
            var category = OpenLambda.classify(Consumer.class);
            assertThat(category).isEqualTo(FunctionalInterfaceUtil.FunctionalCategory.CONSUMER);
        }
    }

    @Nested
    @DisplayName("constant方法测试")
    class ConstantTests {

        @Test
        @DisplayName("创建常量Supplier")
        void testConstant() {
            Supplier<String> supplier = OpenLambda.constant("test");
            assertThat(supplier.get()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("alwaysTrue方法测试")
    class AlwaysTrueTests {

        @Test
        @DisplayName("创建总是true的Predicate")
        void testAlwaysTrue() {
            Predicate<String> predicate = OpenLambda.alwaysTrue();
            assertThat(predicate.test("any")).isTrue();
            assertThat(predicate.test(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("alwaysFalse方法测试")
    class AlwaysFalseTests {

        @Test
        @DisplayName("创建总是false的Predicate")
        void testAlwaysFalse() {
            Predicate<String> predicate = OpenLambda.alwaysFalse();
            assertThat(predicate.test("any")).isFalse();
            assertThat(predicate.test(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("identity方法测试")
    class IdentityTests {

        @Test
        @DisplayName("创建恒等函数")
        void testIdentity() {
            Function<String, String> identity = OpenLambda.identity();
            assertThat(identity.apply("test")).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("noOp方法测试")
    class NoOpTests {

        @Test
        @DisplayName("创建空操作Consumer")
        void testNoOp() {
            Consumer<String> consumer = OpenLambda.noOp();
            assertThatCode(() -> consumer.accept("test")).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("bind Consumer方法测试")
    class BindConsumerTests {

        @Test
        @DisplayName("绑定Consumer和值")
        void testBindConsumer() {
            StringBuilder sb = new StringBuilder();
            Consumer<String> consumer = sb::append;
            Runnable runnable = OpenLambda.bind(consumer, "test");
            runnable.run();
            assertThat(sb.toString()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("bind Function方法测试")
    class BindFunctionTests {

        @Test
        @DisplayName("绑定Function和输入")
        void testBindFunction() {
            Function<String, Integer> function = String::length;
            Supplier<Integer> supplier = OpenLambda.bind(function, "test");
            assertThat(supplier.get()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("safe Function方法测试")
    class SafeFunctionTests {

        @Test
        @DisplayName("安全包装成功的函数")
        void testSafeFunctionSuccess() {
            OpenLambda.ThrowingFunction<String, Integer> throwing = Integer::parseInt;
            Function<String, Integer> safe = OpenLambda.safe(throwing);
            assertThat(safe.apply("123")).isEqualTo(123);
        }

        @Test
        @DisplayName("安全包装失败的函数抛出异常")
        void testSafeFunctionFailure() {
            OpenLambda.ThrowingFunction<String, Integer> throwing = s -> {
                throw new Exception("test error");
            };
            Function<String, Integer> safe = OpenLambda.safe(throwing);
            assertThatThrownBy(() -> safe.apply("test"))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("safe Consumer方法测试")
    class SafeConsumerTests {

        @Test
        @DisplayName("安全包装成功的消费者")
        void testSafeConsumerSuccess() {
            OpenLambda.ThrowingConsumer<String> throwing = s -> {};
            Consumer<String> safe = OpenLambda.safe(throwing);
            assertThatCode(() -> safe.accept("test")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("安全包装失败的消费者抛出异常")
        void testSafeConsumerFailure() {
            OpenLambda.ThrowingConsumer<String> throwing = s -> {
                throw new Exception("test error");
            };
            Consumer<String> safe = OpenLambda.safe(throwing);
            assertThatThrownBy(() -> safe.accept("test"))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("safe Supplier方法测试")
    class SafeSupplierTests {

        @Test
        @DisplayName("安全包装成功的提供者")
        void testSafeSupplierSuccess() {
            OpenLambda.ThrowingSupplier<String> throwing = () -> "test";
            Supplier<String> safe = OpenLambda.safe(throwing);
            assertThat(safe.get()).isEqualTo("test");
        }

        @Test
        @DisplayName("安全包装失败的提供者抛出异常")
        void testSafeSupplierFailure() {
            OpenLambda.ThrowingSupplier<String> throwing = () -> {
                throw new Exception("test error");
            };
            Supplier<String> safe = OpenLambda.safe(throwing);
            assertThatThrownBy(safe::get)
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    // Test helper class
    static class TestBean {
        private String name;
        private boolean active;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isActive() {
            return active;
        }
    }
}
