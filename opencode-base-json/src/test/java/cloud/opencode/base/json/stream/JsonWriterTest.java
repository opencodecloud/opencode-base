package cloud.opencode.base.json.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonWriter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonWriter 测试")
class JsonWriterTest {

    @Nested
    @DisplayName("接口方法定义测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("接口定义了所有必要方法")
        void testInterfaceMethods() throws NoSuchMethodException {
            // Structure Writing
            assertThat(JsonWriter.class.getMethod("beginObject")).isNotNull();
            assertThat(JsonWriter.class.getMethod("endObject")).isNotNull();
            assertThat(JsonWriter.class.getMethod("beginArray")).isNotNull();
            assertThat(JsonWriter.class.getMethod("endArray")).isNotNull();

            // Name Writing
            assertThat(JsonWriter.class.getMethod("name", String.class)).isNotNull();

            // Value Writing
            assertThat(JsonWriter.class.getMethod("value", String.class)).isNotNull();
            assertThat(JsonWriter.class.getMethod("value", boolean.class)).isNotNull();
            assertThat(JsonWriter.class.getMethod("value", int.class)).isNotNull();
            assertThat(JsonWriter.class.getMethod("value", long.class)).isNotNull();
            assertThat(JsonWriter.class.getMethod("value", double.class)).isNotNull();
            assertThat(JsonWriter.class.getMethod("value", Number.class)).isNotNull();
            assertThat(JsonWriter.class.getMethod("nullValue")).isNotNull();
            assertThat(JsonWriter.class.getMethod("jsonValue", String.class)).isNotNull();

            // Configuration
            assertThat(JsonWriter.class.getMethod("setIndent", String.class)).isNotNull();
            assertThat(JsonWriter.class.getMethod("setSerializeNulls", boolean.class)).isNotNull();
            assertThat(JsonWriter.class.getMethod("setLenient", boolean.class)).isNotNull();
            assertThat(JsonWriter.class.getMethod("isLenient")).isNotNull();
            assertThat(JsonWriter.class.getMethod("setHtmlSafe", boolean.class)).isNotNull();
            assertThat(JsonWriter.class.getMethod("isHtmlSafe")).isNotNull();

            // Lifecycle
            assertThat(JsonWriter.class.getMethod("flush")).isNotNull();
            assertThat(JsonWriter.class.getMethod("close")).isNotNull();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("value(float)是默认方法")
        void testValueFloatIsDefault() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("value", float.class).isDefault()).isTrue();
        }

        @Test
        @DisplayName("value(BigInteger)是默认方法")
        void testValueBigIntegerIsDefault() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("value", BigInteger.class).isDefault()).isTrue();
        }

        @Test
        @DisplayName("value(BigDecimal)是默认方法")
        void testValueBigDecimalIsDefault() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("value", BigDecimal.class).isDefault()).isTrue();
        }

        @Test
        @DisplayName("property(String,String)是默认方法")
        void testPropertyStringIsDefault() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("property", String.class, String.class).isDefault())
                .isTrue();
        }

        @Test
        @DisplayName("property(String,boolean)是默认方法")
        void testPropertyBooleanIsDefault() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("property", String.class, boolean.class).isDefault())
                .isTrue();
        }

        @Test
        @DisplayName("property(String,int)是默认方法")
        void testPropertyIntIsDefault() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("property", String.class, int.class).isDefault())
                .isTrue();
        }

        @Test
        @DisplayName("property(String,long)是默认方法")
        void testPropertyLongIsDefault() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("property", String.class, long.class).isDefault())
                .isTrue();
        }

        @Test
        @DisplayName("property(String,double)是默认方法")
        void testPropertyDoubleIsDefault() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("property", String.class, double.class).isDefault())
                .isTrue();
        }

        @Test
        @DisplayName("property(String,Number)是默认方法")
        void testPropertyNumberIsDefault() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("property", String.class, Number.class).isDefault())
                .isTrue();
        }

        @Test
        @DisplayName("propertyNull是默认方法")
        void testPropertyNullIsDefault() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("propertyNull", String.class).isDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("返回类型测试")
    class ReturnTypeTests {

        @Test
        @DisplayName("结构方法返回JsonWriter")
        void testStructureMethodsReturnJsonWriter() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("beginObject").getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("endObject").getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("beginArray").getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("endArray").getReturnType())
                .isEqualTo(JsonWriter.class);
        }

        @Test
        @DisplayName("name方法返回JsonWriter")
        void testNameReturnsJsonWriter() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("name", String.class).getReturnType())
                .isEqualTo(JsonWriter.class);
        }

        @Test
        @DisplayName("value方法返回JsonWriter")
        void testValueMethodsReturnJsonWriter() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("value", String.class).getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("value", boolean.class).getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("value", int.class).getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("value", long.class).getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("value", double.class).getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("value", Number.class).getReturnType())
                .isEqualTo(JsonWriter.class);
        }

        @Test
        @DisplayName("nullValue方法返回JsonWriter")
        void testNullValueReturnsJsonWriter() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("nullValue").getReturnType())
                .isEqualTo(JsonWriter.class);
        }

        @Test
        @DisplayName("配置方法返回JsonWriter")
        void testConfigMethodsReturnJsonWriter() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("setIndent", String.class).getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("setSerializeNulls", boolean.class).getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("setLenient", boolean.class).getReturnType())
                .isEqualTo(JsonWriter.class);
            assertThat(JsonWriter.class.getMethod("setHtmlSafe", boolean.class).getReturnType())
                .isEqualTo(JsonWriter.class);
        }

        @Test
        @DisplayName("isLenient返回boolean")
        void testIsLenientReturnType() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("isLenient").getReturnType())
                .isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("isHtmlSafe返回boolean")
        void testIsHtmlSafeReturnType() throws NoSuchMethodException {
            assertThat(JsonWriter.class.getMethod("isHtmlSafe").getReturnType())
                .isEqualTo(boolean.class);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承Closeable接口")
        void testImplementsCloseable() {
            assertThat(java.io.Closeable.class.isAssignableFrom(JsonWriter.class)).isTrue();
        }

        @Test
        @DisplayName("继承Flushable接口")
        void testImplementsFlushable() {
            assertThat(java.io.Flushable.class.isAssignableFrom(JsonWriter.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Mock实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("可以创建Mock实现")
        void testMockImplementation() {
            JsonWriter mockWriter = new MockJsonWriter();

            assertThat(mockWriter.isLenient()).isFalse();
            assertThat(mockWriter.isHtmlSafe()).isFalse();
        }

        @Test
        @DisplayName("方法链可用")
        void testMethodChaining() {
            MockJsonWriter writer = new MockJsonWriter();

            JsonWriter result = writer
                .beginObject()
                .name("id").value(1)
                .name("name").value("test")
                .name("active").value(true)
                .name("score").value(95.5)
                .name("data").nullValue()
                .endObject();

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("property便捷方法可用")
        void testPropertyMethods() {
            MockJsonWriter writer = new MockJsonWriter();

            JsonWriter result = writer
                .beginObject()
                .property("name", "test")
                .property("active", true)
                .property("count", 10)
                .property("id", 123L)
                .property("rate", 0.5)
                .property("amount", BigDecimal.TEN)
                .propertyNull("empty")
                .endObject();

            assertThat(result).isNotNull();
        }
    }

    // Mock implementation for testing
    static class MockJsonWriter implements JsonWriter {
        private boolean lenient = false;
        private boolean htmlSafe = false;

        @Override
        public JsonWriter beginObject() {
            return this;
        }

        @Override
        public JsonWriter endObject() {
            return this;
        }

        @Override
        public JsonWriter beginArray() {
            return this;
        }

        @Override
        public JsonWriter endArray() {
            return this;
        }

        @Override
        public JsonWriter name(String name) {
            return this;
        }

        @Override
        public JsonWriter value(String value) {
            return this;
        }

        @Override
        public JsonWriter value(boolean value) {
            return this;
        }

        @Override
        public JsonWriter value(int value) {
            return this;
        }

        @Override
        public JsonWriter value(long value) {
            return this;
        }

        @Override
        public JsonWriter value(double value) {
            return this;
        }

        @Override
        public JsonWriter value(Number value) {
            return this;
        }

        @Override
        public JsonWriter nullValue() {
            return this;
        }

        @Override
        public JsonWriter jsonValue(String json) {
            return this;
        }

        @Override
        public JsonWriter setIndent(String indent) {
            return this;
        }

        @Override
        public JsonWriter setSerializeNulls(boolean serializeNulls) {
            return this;
        }

        @Override
        public JsonWriter setLenient(boolean lenient) {
            this.lenient = lenient;
            return this;
        }

        @Override
        public boolean isLenient() {
            return lenient;
        }

        @Override
        public JsonWriter setHtmlSafe(boolean htmlSafe) {
            this.htmlSafe = htmlSafe;
            return this;
        }

        @Override
        public boolean isHtmlSafe() {
            return htmlSafe;
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    }
}
