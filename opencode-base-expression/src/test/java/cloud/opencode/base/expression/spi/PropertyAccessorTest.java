package cloud.opencode.base.expression.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertyAccessor Tests
 * PropertyAccessor 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("PropertyAccessor Tests | PropertyAccessor 测试")
class PropertyAccessorTest {

    @Nested
    @DisplayName("Interface Contract Tests | 接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("implementation can read property | 实现可以读取属性")
        void testCanReadProperty() {
            PropertyAccessor accessor = new TestPropertyAccessor();
            assertThat(accessor.canRead("test", "prop")).isTrue();
            assertThat(accessor.read("test", "prop")).isEqualTo("value");
        }

        @Test
        @DisplayName("getSpecificTargetClasses returns types | getSpecificTargetClasses 返回类型")
        void testGetSpecificTargetClasses() {
            PropertyAccessor accessor = new TestPropertyAccessor();
            assertThat(accessor.getSpecificTargetClasses()).containsExactly(String.class);
        }
    }

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("canWrite returns false by default | canWrite 默认返回 false")
        void testCanWriteDefault() {
            PropertyAccessor accessor = new ReadOnlyAccessor();
            assertThat(accessor.canWrite("target", "prop")).isFalse();
        }

        @Test
        @DisplayName("write throws UnsupportedOperationException by default | write 默认抛出 UnsupportedOperationException")
        void testWriteThrowsDefault() {
            PropertyAccessor accessor = new ReadOnlyAccessor();
            assertThatThrownBy(() -> accessor.write("target", "prop", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Writable Accessor Tests | 可写访问器测试")
    class WritableAccessorTests {

        @Test
        @DisplayName("writable accessor can write | 可写访问器可以写入")
        void testCanWrite() {
            WritableAccessor accessor = new WritableAccessor();
            assertThat(accessor.canWrite("target", "prop")).isTrue();
        }

        @Test
        @DisplayName("writable accessor writes value | 可写访问器写入值")
        void testWrite() {
            WritableAccessor accessor = new WritableAccessor();
            accessor.write("target", "prop", "newValue");
            assertThat(accessor.getLastWrittenValue()).isEqualTo("newValue");
        }
    }

    // Test implementations

    private static class TestPropertyAccessor implements PropertyAccessor {
        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return new Class<?>[]{String.class};
        }

        @Override
        public boolean canRead(Object target, String name) {
            return true;
        }

        @Override
        public Object read(Object target, String name) {
            return "value";
        }
    }

    private static class ReadOnlyAccessor implements PropertyAccessor {
        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return null;
        }

        @Override
        public boolean canRead(Object target, String name) {
            return true;
        }

        @Override
        public Object read(Object target, String name) {
            return "value";
        }
    }

    private static class WritableAccessor implements PropertyAccessor {
        private Object lastWrittenValue;

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return null;
        }

        @Override
        public boolean canRead(Object target, String name) {
            return true;
        }

        @Override
        public Object read(Object target, String name) {
            return lastWrittenValue;
        }

        @Override
        public boolean canWrite(Object target, String name) {
            return true;
        }

        @Override
        public void write(Object target, String name, Object value) {
            this.lastWrittenValue = value;
        }

        public Object getLastWrittenValue() {
            return lastWrittenValue;
        }
    }
}
