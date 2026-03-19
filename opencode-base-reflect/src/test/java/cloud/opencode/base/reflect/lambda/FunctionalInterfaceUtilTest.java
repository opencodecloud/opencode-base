package cloud.opencode.base.reflect.lambda;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

import static org.assertj.core.api.Assertions.*;

/**
 * FunctionalInterfaceUtilTest Tests
 * FunctionalInterfaceUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("FunctionalInterfaceUtil 测试")
class FunctionalInterfaceUtilTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = FunctionalInterfaceUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("isFunctionalInterface方法测试")
    class IsFunctionalInterfaceTests {

        @Test
        @DisplayName("带@FunctionalInterface注解的接口返回true")
        void testIsFunctionalInterfaceWithAnnotation() {
            assertThat(FunctionalInterfaceUtil.isFunctionalInterface(Function.class)).isTrue();
        }

        @Test
        @DisplayName("只有一个抽象方法的接口返回true")
        void testIsFunctionalInterfaceSingleAbstract() {
            assertThat(FunctionalInterfaceUtil.isFunctionalInterface(Runnable.class)).isTrue();
        }

        @Test
        @DisplayName("非接口返回false")
        void testIsFunctionalInterfaceNotInterface() {
            assertThat(FunctionalInterfaceUtil.isFunctionalInterface(String.class)).isFalse();
        }

        @Test
        @DisplayName("多个抽象方法返回false")
        void testIsFunctionalInterfaceMultipleAbstract() {
            assertThat(FunctionalInterfaceUtil.isFunctionalInterface(MultiMethodInterface.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getSingleAbstractMethod方法测试")
    class GetSingleAbstractMethodTests {

        @Test
        @DisplayName("获取函数式接口的SAM")
        void testGetSingleAbstractMethod() {
            Optional<Method> sam = FunctionalInterfaceUtil.getSingleAbstractMethod(Function.class);
            assertThat(sam).isPresent();
            assertThat(sam.get().getName()).isEqualTo("apply");
        }

        @Test
        @DisplayName("非接口返回空")
        void testGetSingleAbstractMethodNotInterface() {
            Optional<Method> sam = FunctionalInterfaceUtil.getSingleAbstractMethod(String.class);
            assertThat(sam).isEmpty();
        }

        @Test
        @DisplayName("多抽象方法接口返回空")
        void testGetSingleAbstractMethodMultiple() {
            Optional<Method> sam = FunctionalInterfaceUtil.getSingleAbstractMethod(MultiMethodInterface.class);
            assertThat(sam).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAbstractMethods方法测试")
    class GetAbstractMethodsTests {

        @Test
        @DisplayName("获取接口的抽象方法")
        void testGetAbstractMethods() {
            List<Method> methods = FunctionalInterfaceUtil.getAbstractMethods(Function.class);
            assertThat(methods).isNotEmpty();
        }

        @Test
        @DisplayName("非接口返回空列表")
        void testGetAbstractMethodsNotInterface() {
            List<Method> methods = FunctionalInterfaceUtil.getAbstractMethods(String.class);
            assertThat(methods).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDefaultMethods方法测试")
    class GetDefaultMethodsTests {

        @Test
        @DisplayName("获取接口的默认方法")
        void testGetDefaultMethods() {
            List<Method> methods = FunctionalInterfaceUtil.getDefaultMethods(Function.class);
            // Function has andThen and compose as default methods
            assertThat(methods).isNotEmpty();
        }

        @Test
        @DisplayName("非接口返回空列表")
        void testGetDefaultMethodsNotInterface() {
            List<Method> methods = FunctionalInterfaceUtil.getDefaultMethods(String.class);
            assertThat(methods).isEmpty();
        }
    }

    @Nested
    @DisplayName("getStaticMethods方法测试")
    class GetStaticMethodsTests {

        @Test
        @DisplayName("获取接口的静态方法")
        void testGetStaticMethods() {
            List<Method> methods = FunctionalInterfaceUtil.getStaticMethods(Function.class);
            // Function has identity() as static method
            assertThat(methods).isNotEmpty();
        }

        @Test
        @DisplayName("非接口返回空列表")
        void testGetStaticMethodsNotInterface() {
            List<Method> methods = FunctionalInterfaceUtil.getStaticMethods(String.class);
            assertThat(methods).isEmpty();
        }
    }

    @Nested
    @DisplayName("getFunctionalMethodReturnType方法测试")
    class GetFunctionalMethodReturnTypeTests {

        @Test
        @DisplayName("获取函数式方法返回类型")
        void testGetFunctionalMethodReturnType() {
            Class<?> returnType = FunctionalInterfaceUtil.getFunctionalMethodReturnType(Supplier.class);
            assertThat(returnType).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("非函数式接口返回null")
        void testGetFunctionalMethodReturnTypeNonFunctional() {
            Class<?> returnType = FunctionalInterfaceUtil.getFunctionalMethodReturnType(String.class);
            assertThat(returnType).isNull();
        }
    }

    @Nested
    @DisplayName("getFunctionalMethodParameterTypes方法测试")
    class GetFunctionalMethodParameterTypesTests {

        @Test
        @DisplayName("获取函数式方法参数类型")
        void testGetFunctionalMethodParameterTypes() {
            Class<?>[] paramTypes = FunctionalInterfaceUtil.getFunctionalMethodParameterTypes(Consumer.class);
            assertThat(paramTypes).hasSize(1);
        }

        @Test
        @DisplayName("无参函数式接口返回空数组")
        void testGetFunctionalMethodParameterTypesNoParams() {
            Class<?>[] paramTypes = FunctionalInterfaceUtil.getFunctionalMethodParameterTypes(Supplier.class);
            assertThat(paramTypes).isEmpty();
        }
    }

    @Nested
    @DisplayName("getFunctionalMethodArity方法测试")
    class GetFunctionalMethodArityTests {

        @Test
        @DisplayName("获取函数式方法元数")
        void testGetFunctionalMethodArity() {
            int arity = FunctionalInterfaceUtil.getFunctionalMethodArity(BiFunction.class);
            assertThat(arity).isEqualTo(2);
        }

        @Test
        @DisplayName("Supplier元数为0")
        void testGetFunctionalMethodAritySupplier() {
            int arity = FunctionalInterfaceUtil.getFunctionalMethodArity(Supplier.class);
            assertThat(arity).isZero();
        }

        @Test
        @DisplayName("非函数式接口返回-1")
        void testGetFunctionalMethodArityNonFunctional() {
            int arity = FunctionalInterfaceUtil.getFunctionalMethodArity(String.class);
            assertThat(arity).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("returnsVoid方法测试")
    class ReturnsVoidTests {

        @Test
        @DisplayName("Consumer返回void")
        void testReturnsVoidTrue() {
            assertThat(FunctionalInterfaceUtil.returnsVoid(Consumer.class)).isTrue();
        }

        @Test
        @DisplayName("Function不返回void")
        void testReturnsVoidFalse() {
            assertThat(FunctionalInterfaceUtil.returnsVoid(Function.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasNoParameters方法测试")
    class HasNoParametersTests {

        @Test
        @DisplayName("Supplier无参数")
        void testHasNoParametersTrue() {
            assertThat(FunctionalInterfaceUtil.hasNoParameters(Supplier.class)).isTrue();
        }

        @Test
        @DisplayName("Function有参数")
        void testHasNoParametersFalse() {
            assertThat(FunctionalInterfaceUtil.hasNoParameters(Function.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("classify方法测试")
    class ClassifyTests {

        @Test
        @DisplayName("分类Runnable")
        void testClassifyRunnable() {
            var category = FunctionalInterfaceUtil.classify(Runnable.class);
            assertThat(category).isEqualTo(FunctionalInterfaceUtil.FunctionalCategory.RUNNABLE);
        }

        @Test
        @DisplayName("分类Consumer")
        void testClassifyConsumer() {
            var category = FunctionalInterfaceUtil.classify(Consumer.class);
            assertThat(category).isEqualTo(FunctionalInterfaceUtil.FunctionalCategory.CONSUMER);
        }

        @Test
        @DisplayName("分类Supplier")
        void testClassifySupplier() {
            var category = FunctionalInterfaceUtil.classify(Supplier.class);
            assertThat(category).isEqualTo(FunctionalInterfaceUtil.FunctionalCategory.SUPPLIER);
        }

        @Test
        @DisplayName("分类Predicate")
        void testClassifyPredicate() {
            var category = FunctionalInterfaceUtil.classify(Predicate.class);
            assertThat(category).isEqualTo(FunctionalInterfaceUtil.FunctionalCategory.PREDICATE);
        }

        @Test
        @DisplayName("分类Function")
        void testClassifyFunction() {
            var category = FunctionalInterfaceUtil.classify(Function.class);
            assertThat(category).isEqualTo(FunctionalInterfaceUtil.FunctionalCategory.FUNCTION);
        }

        @Test
        @DisplayName("非函数式接口")
        void testClassifyNotFunctional() {
            var category = FunctionalInterfaceUtil.classify(String.class);
            assertThat(category).isEqualTo(FunctionalInterfaceUtil.FunctionalCategory.NOT_FUNCTIONAL);
        }
    }

    @Nested
    @DisplayName("FunctionalCategory枚举测试")
    class FunctionalCategoryTests {

        @Test
        @DisplayName("枚举值存在")
        void testEnumValues() {
            var values = FunctionalInterfaceUtil.FunctionalCategory.values();
            assertThat(values).contains(
                    FunctionalInterfaceUtil.FunctionalCategory.NOT_FUNCTIONAL,
                    FunctionalInterfaceUtil.FunctionalCategory.RUNNABLE,
                    FunctionalInterfaceUtil.FunctionalCategory.CONSUMER,
                    FunctionalInterfaceUtil.FunctionalCategory.SUPPLIER,
                    FunctionalInterfaceUtil.FunctionalCategory.PREDICATE,
                    FunctionalInterfaceUtil.FunctionalCategory.FUNCTION
            );
        }
    }

    // Test helper interface with multiple abstract methods
    interface MultiMethodInterface {
        void method1();
        void method2();
    }
}
