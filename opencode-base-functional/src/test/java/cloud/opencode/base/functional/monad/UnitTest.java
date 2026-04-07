package cloud.opencode.base.functional.monad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.3
 */
@DisplayName("Unit 测试")
class UnitTest {

    @Nested
    @DisplayName("INSTANCE 测试")
    class InstanceTests {

        @Test
        @DisplayName("INSTANCE不为null")
        void testInstanceNotNull() {
            assertThat(Unit.INSTANCE).isNotNull();
        }

        @Test
        @DisplayName("INSTANCE是唯一值")
        void testInstanceSingleton() {
            Unit a = Unit.INSTANCE;
            Unit b = Unit.INSTANCE;

            assertThat(a).isSameAs(b);
        }

        @Test
        @DisplayName("INSTANCE的equals自反性")
        void testInstanceEquals() {
            assertThat(Unit.INSTANCE).isEqualTo(Unit.INSTANCE);
        }

        @Test
        @DisplayName("可用作泛型类型参数")
        void testAsGenericTypeParameter() {
            Supplier<Unit> supplier = () -> Unit.INSTANCE;

            Unit result = supplier.get();

            assertThat(result).isEqualTo(Unit.INSTANCE);
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("返回\"()\"")
        void testToString() {
            assertThat(Unit.INSTANCE.toString()).isEqualTo("()");
        }

        @Test
        @DisplayName("字符串表示一致")
        void testToStringConsistency() {
            String first = Unit.INSTANCE.toString();
            String second = Unit.INSTANCE.toString();

            assertThat(first).isEqualTo(second);
        }
    }

    @Nested
    @DisplayName("ignore() 测试")
    class IgnoreTests {

        @Test
        @DisplayName("丢弃字符串输入返回Unit")
        void testIgnoreString() {
            Function<String, Unit> ignore = Unit.ignore();

            Unit result = ignore.apply("hello");

            assertThat(result).isEqualTo(Unit.INSTANCE);
        }

        @Test
        @DisplayName("丢弃整数输入返回Unit")
        void testIgnoreInteger() {
            Function<Integer, Unit> ignore = Unit.ignore();

            Unit result = ignore.apply(42);

            assertThat(result).isEqualTo(Unit.INSTANCE);
        }

        @Test
        @DisplayName("丢弃null输入返回Unit")
        void testIgnoreNull() {
            Function<Object, Unit> ignore = Unit.ignore();

            Unit result = ignore.apply(null);

            assertThat(result).isEqualTo(Unit.INSTANCE);
        }

        @Test
        @DisplayName("可用于Stream.map")
        void testIgnoreInStreamMap() {
            long count = java.util.stream.Stream.of("a", "b", "c")
                    .map(Unit.ignore())
                    .filter(u -> u == Unit.INSTANCE)
                    .count();

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("多次调用返回同一实例")
        void testIgnoreReturnsSameInstance() {
            Function<String, Unit> ignore = Unit.ignore();

            Unit result1 = ignore.apply("a");
            Unit result2 = ignore.apply("b");

            assertThat(result1).isSameAs(result2);
        }
    }

    @Nested
    @DisplayName("supplier() 测试")
    class SupplierTests {

        @Test
        @DisplayName("返回Unit实例")
        void testSupplierReturnsUnit() {
            Supplier<Unit> supplier = Unit.supplier();

            Unit result = supplier.get();

            assertThat(result).isEqualTo(Unit.INSTANCE);
        }

        @Test
        @DisplayName("多次调用返回同一实例")
        void testSupplierReturnsSameInstance() {
            Supplier<Unit> supplier = Unit.supplier();

            Unit result1 = supplier.get();
            Unit result2 = supplier.get();

            assertThat(result1).isSameAs(result2);
        }

        @Test
        @DisplayName("不同supplier返回相同值")
        void testDifferentSuppliersReturnSameValue() {
            Supplier<Unit> supplier1 = Unit.supplier();
            Supplier<Unit> supplier2 = Unit.supplier();

            assertThat(supplier1.get()).isSameAs(supplier2.get());
        }
    }

    @Nested
    @DisplayName("枚举特性测试")
    class EnumTests {

        @Test
        @DisplayName("values()只有一个元素")
        void testValuesHasSingleElement() {
            Unit[] values = Unit.values();

            assertThat(values).hasSize(1);
            assertThat(values[0]).isEqualTo(Unit.INSTANCE);
        }

        @Test
        @DisplayName("valueOf(\"INSTANCE\")返回正确值")
        void testValueOf() {
            Unit unit = Unit.valueOf("INSTANCE");

            assertThat(unit).isEqualTo(Unit.INSTANCE);
        }

        @Test
        @DisplayName("valueOf无效名称抛出异常")
        void testValueOfInvalidName() {
            assertThatThrownBy(() -> Unit.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ordinal为0")
        void testOrdinal() {
            assertThat(Unit.INSTANCE.ordinal()).isZero();
        }

        @Test
        @DisplayName("name为INSTANCE")
        void testName() {
            assertThat(Unit.INSTANCE.name()).isEqualTo("INSTANCE");
        }
    }
}
