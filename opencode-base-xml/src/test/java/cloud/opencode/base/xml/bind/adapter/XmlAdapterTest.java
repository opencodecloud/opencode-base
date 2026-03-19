package cloud.opencode.base.xml.bind.adapter;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlAdapterTest Tests
 * XmlAdapterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlAdapter Tests")
class XmlAdapterTest {

    @Nested
    @DisplayName("Interface Tests")
    class InterfaceTests {

        @Test
        @DisplayName("unmarshal should convert value type to bound type")
        void unmarshalShouldConvertValueTypeToBoundType() throws Exception {
            TestAdapter adapter = new TestAdapter();

            Integer result = adapter.unmarshal("42");

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("marshal should convert bound type to value type")
        void marshalShouldConvertBoundTypeToValueType() throws Exception {
            TestAdapter adapter = new TestAdapter();

            String result = adapter.marshal(42);

            assertThat(result).isEqualTo("42");
        }
    }

    @Nested
    @DisplayName("Default Method Tests")
    class DefaultMethodTests {

        @Test
        @DisplayName("getBoundType should return null by default")
        void getBoundTypeShouldReturnNullByDefault() {
            TestAdapter adapter = new TestAdapter();

            assertThat(adapter.getBoundType()).isNull();
        }

        @Test
        @DisplayName("getValueType should return null by default")
        void getValueTypeShouldReturnNullByDefault() {
            TestAdapter adapter = new TestAdapter();

            assertThat(adapter.getValueType()).isNull();
        }
    }

    @Nested
    @DisplayName("Custom Adapter Tests")
    class CustomAdapterTests {

        @Test
        @DisplayName("custom adapter with type info should return types")
        void customAdapterWithTypeInfoShouldReturnTypes() {
            TypedAdapter adapter = new TypedAdapter();

            assertThat(adapter.getBoundType()).isEqualTo(Integer.class);
            assertThat(adapter.getValueType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("Complex Adapter Tests")
    class ComplexAdapterTests {

        @Test
        @DisplayName("adapter should handle complex types")
        void adapterShouldHandleComplexTypes() throws Exception {
            EnumAdapter adapter = new EnumAdapter();

            Status status = adapter.unmarshal("ACTIVE");
            String value = adapter.marshal(status);

            assertThat(status).isEqualTo(Status.ACTIVE);
            assertThat(value).isEqualTo("ACTIVE");
        }
    }

    // Test adapter implementations

    private static class TestAdapter implements XmlAdapter<String, Integer> {
        @Override
        public Integer unmarshal(String value) {
            return Integer.parseInt(value);
        }

        @Override
        public String marshal(Integer value) {
            return value.toString();
        }
    }

    private static class TypedAdapter implements XmlAdapter<String, Integer> {
        @Override
        public Integer unmarshal(String value) {
            return Integer.parseInt(value);
        }

        @Override
        public String marshal(Integer value) {
            return value.toString();
        }

        @Override
        public Class<Integer> getBoundType() {
            return Integer.class;
        }

        @Override
        public Class<String> getValueType() {
            return String.class;
        }
    }

    private enum Status {
        ACTIVE, INACTIVE
    }

    private static class EnumAdapter implements XmlAdapter<String, Status> {
        @Override
        public Status unmarshal(String value) {
            return Status.valueOf(value);
        }

        @Override
        public String marshal(Status value) {
            return value.name();
        }
    }
}
