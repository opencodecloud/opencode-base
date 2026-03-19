package cloud.opencode.base.json.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonReader 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonReader 测试")
class JsonReaderTest {

    @Nested
    @DisplayName("接口方法定义测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("接口定义了所有必要方法")
        void testInterfaceMethods() throws NoSuchMethodException {
            // Structure Navigation
            assertThat(JsonReader.class.getMethod("beginObject")).isNotNull();
            assertThat(JsonReader.class.getMethod("endObject")).isNotNull();
            assertThat(JsonReader.class.getMethod("beginArray")).isNotNull();
            assertThat(JsonReader.class.getMethod("endArray")).isNotNull();

            // Token Inspection
            assertThat(JsonReader.class.getMethod("hasNext")).isNotNull();
            assertThat(JsonReader.class.getMethod("peek")).isNotNull();

            // Name Reading
            assertThat(JsonReader.class.getMethod("nextName")).isNotNull();

            // Value Reading
            assertThat(JsonReader.class.getMethod("nextString")).isNotNull();
            assertThat(JsonReader.class.getMethod("nextBoolean")).isNotNull();
            assertThat(JsonReader.class.getMethod("nextNull")).isNotNull();
            assertThat(JsonReader.class.getMethod("nextInt")).isNotNull();
            assertThat(JsonReader.class.getMethod("nextLong")).isNotNull();
            assertThat(JsonReader.class.getMethod("nextDouble")).isNotNull();
            assertThat(JsonReader.class.getMethod("nextBigInteger")).isNotNull();
            assertThat(JsonReader.class.getMethod("nextBigDecimal")).isNotNull();
            assertThat(JsonReader.class.getMethod("nextNumber")).isNotNull();

            // Skip Operations
            assertThat(JsonReader.class.getMethod("skipValue")).isNotNull();

            // Path Information
            assertThat(JsonReader.class.getMethod("getPath")).isNotNull();
            assertThat(JsonReader.class.getMethod("getLineNumber")).isNotNull();
            assertThat(JsonReader.class.getMethod("getColumnNumber")).isNotNull();

            // Configuration
            assertThat(JsonReader.class.getMethod("setLenient", boolean.class)).isNotNull();
            assertThat(JsonReader.class.getMethod("isLenient")).isNotNull();

            // Lifecycle
            assertThat(JsonReader.class.getMethod("close")).isNotNull();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("skipObject是默认方法")
        void testSkipObjectIsDefault() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("skipObject").isDefault()).isTrue();
        }

        @Test
        @DisplayName("skipArray是默认方法")
        void testSkipArrayIsDefault() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("skipArray").isDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("返回类型测试")
    class ReturnTypeTests {

        @Test
        @DisplayName("hasNext返回boolean")
        void testHasNextReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("hasNext").getReturnType())
                .isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("peek返回JsonToken")
        void testPeekReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("peek").getReturnType())
                .isEqualTo(JsonToken.class);
        }

        @Test
        @DisplayName("nextName返回String")
        void testNextNameReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("nextName").getReturnType())
                .isEqualTo(String.class);
        }

        @Test
        @DisplayName("nextString返回String")
        void testNextStringReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("nextString").getReturnType())
                .isEqualTo(String.class);
        }

        @Test
        @DisplayName("nextBoolean返回boolean")
        void testNextBooleanReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("nextBoolean").getReturnType())
                .isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("nextInt返回int")
        void testNextIntReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("nextInt").getReturnType())
                .isEqualTo(int.class);
        }

        @Test
        @DisplayName("nextLong返回long")
        void testNextLongReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("nextLong").getReturnType())
                .isEqualTo(long.class);
        }

        @Test
        @DisplayName("nextDouble返回double")
        void testNextDoubleReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("nextDouble").getReturnType())
                .isEqualTo(double.class);
        }

        @Test
        @DisplayName("nextBigInteger返回BigInteger")
        void testNextBigIntegerReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("nextBigInteger").getReturnType())
                .isEqualTo(BigInteger.class);
        }

        @Test
        @DisplayName("nextBigDecimal返回BigDecimal")
        void testNextBigDecimalReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("nextBigDecimal").getReturnType())
                .isEqualTo(BigDecimal.class);
        }

        @Test
        @DisplayName("nextNumber返回Number")
        void testNextNumberReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("nextNumber").getReturnType())
                .isEqualTo(Number.class);
        }

        @Test
        @DisplayName("getPath返回String")
        void testGetPathReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("getPath").getReturnType())
                .isEqualTo(String.class);
        }

        @Test
        @DisplayName("getLineNumber返回int")
        void testGetLineNumberReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("getLineNumber").getReturnType())
                .isEqualTo(int.class);
        }

        @Test
        @DisplayName("getColumnNumber返回int")
        void testGetColumnNumberReturnType() throws NoSuchMethodException {
            assertThat(JsonReader.class.getMethod("getColumnNumber").getReturnType())
                .isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("Closeable继承测试")
    class CloseableInheritanceTests {

        @Test
        @DisplayName("继承Closeable接口")
        void testImplementsCloseable() {
            assertThat(java.io.Closeable.class.isAssignableFrom(JsonReader.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Mock实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("可以创建Mock实现")
        void testMockImplementation() {
            JsonReader mockReader = new MockJsonReader();

            assertThat(mockReader.hasNext()).isFalse();
            assertThat(mockReader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
            assertThat(mockReader.getPath()).isEqualTo("$");
            assertThat(mockReader.getLineNumber()).isEqualTo(1);
            assertThat(mockReader.getColumnNumber()).isEqualTo(1);
            assertThat(mockReader.isLenient()).isFalse();
        }
    }

    // Mock implementation for testing
    static class MockJsonReader implements JsonReader {
        private boolean lenient = false;

        @Override
        public void beginObject() {}

        @Override
        public void endObject() {}

        @Override
        public void beginArray() {}

        @Override
        public void endArray() {}

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public JsonToken peek() {
            return JsonToken.END_DOCUMENT;
        }

        @Override
        public String nextName() {
            return "";
        }

        @Override
        public String nextString() {
            return "";
        }

        @Override
        public boolean nextBoolean() {
            return false;
        }

        @Override
        public void nextNull() {}

        @Override
        public int nextInt() {
            return 0;
        }

        @Override
        public long nextLong() {
            return 0L;
        }

        @Override
        public double nextDouble() {
            return 0.0;
        }

        @Override
        public BigInteger nextBigInteger() {
            return BigInteger.ZERO;
        }

        @Override
        public BigDecimal nextBigDecimal() {
            return BigDecimal.ZERO;
        }

        @Override
        public Number nextNumber() {
            return 0;
        }

        @Override
        public void skipValue() {}

        @Override
        public String getPath() {
            return "$";
        }

        @Override
        public int getLineNumber() {
            return 1;
        }

        @Override
        public int getColumnNumber() {
            return 1;
        }

        @Override
        public void setLenient(boolean lenient) {
            this.lenient = lenient;
        }

        @Override
        public boolean isLenient() {
            return lenient;
        }

        @Override
        public void close() {}
    }
}
