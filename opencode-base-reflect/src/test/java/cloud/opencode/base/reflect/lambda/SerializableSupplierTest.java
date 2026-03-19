package cloud.opencode.base.reflect.lambda;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializableSupplierTest Tests
 * SerializableSupplierTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("SerializableSupplier 测试")
class SerializableSupplierTest {

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("获取值")
        void testGet() {
            SerializableSupplier<String> supplier = () -> "test";
            assertThat(supplier.get()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("创建SerializableSupplier")
        void testOf() {
            SerializableSupplier<String> supplier = SerializableSupplier.of(() -> "test");
            assertThat(supplier.get()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("constant工厂方法测试")
    class ConstantTests {

        @Test
        @DisplayName("创建常量提供者")
        void testConstant() {
            SerializableSupplier<String> supplier = SerializableSupplier.constant("constant");
            assertThat(supplier.get()).isEqualTo("constant");
            assertThat(supplier.get()).isEqualTo("constant"); // Multiple calls return same value
        }
    }

    @Nested
    @DisplayName("nullSupplier工厂方法测试")
    class NullSupplierTests {

        @Test
        @DisplayName("创建null提供者")
        void testNullSupplier() {
            SerializableSupplier<String> supplier = SerializableSupplier.nullSupplier();
            assertThat(supplier.get()).isNull();
        }
    }

    @Nested
    @DisplayName("Serializable测试")
    class SerializableTests {

        @Test
        @DisplayName("提供者是可序列化的")
        void testSerializable() {
            SerializableSupplier<String> supplier = () -> "test";
            assertThat(supplier).isInstanceOf(java.io.Serializable.class);
        }
    }
}
