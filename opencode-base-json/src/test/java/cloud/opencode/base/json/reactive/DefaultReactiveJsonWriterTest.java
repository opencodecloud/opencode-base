package cloud.opencode.base.json.reactive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultReactiveJsonWriter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("DefaultReactiveJsonWriter 测试")
class DefaultReactiveJsonWriterTest {

    @Nested
    @DisplayName("构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用默认设置创建")
        void testDefaultConstructor() {
            OutputStream output = new ByteArrayOutputStream();

            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            assertThat(writer).isNotNull();
            assertThat(writer.isOpen()).isTrue();
        }

        @Test
        @DisplayName("使用自定义缓冲区大小创建")
        void testCustomBufferSize() {
            OutputStream output = new ByteArrayOutputStream();

            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output, 4096);

            assertThat(writer).isNotNull();
            assertThat(writer.isOpen()).isTrue();
        }

        @Test
        @DisplayName("使用美化输出选项创建")
        void testPrettyPrintConstructor() {
            OutputStream output = new ByteArrayOutputStream();

            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output, true);

            assertThat(writer).isNotNull();
            assertThat(writer.isOpen()).isTrue();
        }

        @Test
        @DisplayName("使用所有选项创建")
        void testFullConstructor() {
            OutputStream output = new ByteArrayOutputStream();

            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output, 4096, true);

            assertThat(writer).isNotNull();
            assertThat(writer.isOpen()).isTrue();
        }

        @Test
        @DisplayName("null输出流抛出异常")
        void testNullOutputStream() {
            assertThatThrownBy(() -> new DefaultReactiveJsonWriter(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("output");
        }

        @Test
        @DisplayName("BufferedOutputStream直接使用")
        void testBufferedOutputStream() {
            OutputStream output = new BufferedOutputStream(new ByteArrayOutputStream());

            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            assertThat(writer).isNotNull();
        }
    }

    @Nested
    @DisplayName("isOpen方法测试")
    class IsOpenTests {

        @Test
        @DisplayName("新创建的写入器是打开的")
        void testNewWriterIsOpen() {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            assertThat(writer.isOpen()).isTrue();
        }

        @Test
        @DisplayName("关闭后写入器不再打开")
        void testClosedWriterIsNotOpen() throws IOException {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            writer.close();

            assertThat(writer.isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("getElementsWritten方法测试")
    class GetElementsWrittenTests {

        @Test
        @DisplayName("初始元素写入计数为0")
        void testInitialElementsWritten() {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            assertThat(writer.getElementsWritten()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getBytesWritten方法测试")
    class GetBytesWrittenTests {

        @Test
        @DisplayName("初始字节写入计数为0")
        void testInitialBytesWritten() {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            assertThat(writer.getBytesWritten()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("关闭写入器")
        void testClose() throws IOException {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            writer.close();

            assertThat(writer.isOpen()).isFalse();
        }

        @Test
        @DisplayName("多次关闭是安全的")
        void testMultipleClose() throws IOException {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            writer.close();
            writer.close();

            assertThat(writer.isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("flush方法测试")
    class FlushTests {

        @Test
        @DisplayName("flush不抛出异常")
        void testFlush() {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            assertThatCode(() -> writer.flush()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("write方法测试")
    class WriteTests {

        @Test
        @DisplayName("write返回CompletableFuture")
        void testWriteReturnsCompletableFuture() {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);
            SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

            CompletableFuture<Void> future = writer.write(publisher);
            publisher.close();

            assertThat(future).isNotNull();
        }

        @Test
        @DisplayName("write对null源抛出异常")
        void testWriteNullSource() {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            assertThatThrownBy(() -> writer.write(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("source");
        }

        @Test
        @DisplayName("关闭后write抛出异常")
        void testWriteAfterClose() throws IOException {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);
            writer.close();
            SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

            assertThatThrownBy(() -> writer.write(publisher))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("writeAsArray方法测试")
    class WriteAsArrayTests {

        @Test
        @DisplayName("writeAsArray返回CompletableFuture")
        void testWriteAsArrayReturnsCompletableFuture() {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);
            SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

            CompletableFuture<Void> future = writer.writeAsArray(publisher);
            publisher.close();

            assertThat(future).isNotNull();
        }

        @Test
        @DisplayName("writeAsArray对null源抛出异常")
        void testWriteAsArrayNullSource() {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            assertThatThrownBy(() -> writer.writeAsArray(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("source");
        }

        @Test
        @DisplayName("关闭后writeAsArray抛出异常")
        void testWriteAsArrayAfterClose() throws IOException {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);
            writer.close();
            SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

            assertThatThrownBy(() -> writer.writeAsArray(publisher))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("writeObject方法测试")
    class WriteObjectTests {

        @Test
        @DisplayName("writeObject返回CompletableFuture")
        void testWriteObjectReturnsCompletableFuture() {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            CompletableFuture<Void> future = writer.writeObject("test");

            assertThat(future).isNotNull();
        }

        @Test
        @DisplayName("writeObject对null对象抛出异常")
        void testWriteObjectNull() {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);

            assertThatThrownBy(() -> writer.writeObject(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("object");
        }

        @Test
        @DisplayName("关闭后writeObject抛出异常")
        void testWriteObjectAfterClose() throws IOException {
            OutputStream output = new ByteArrayOutputStream();
            DefaultReactiveJsonWriter writer = new DefaultReactiveJsonWriter(output);
            writer.close();

            assertThatThrownBy(() -> writer.writeObject("test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("是final类")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(DefaultReactiveJsonWriter.class.getModifiers()))
                .isTrue();
        }

        @Test
        @DisplayName("实现ReactiveJsonWriter接口")
        void testImplementsReactiveJsonWriter() {
            assertThat(ReactiveJsonWriter.class.isAssignableFrom(DefaultReactiveJsonWriter.class)).isTrue();
        }

        @Test
        @DisplayName("包级别可见性")
        void testPackagePrivate() {
            int modifiers = DefaultReactiveJsonWriter.class.getModifiers();
            assertThat(java.lang.reflect.Modifier.isPublic(modifiers)).isFalse();
            assertThat(java.lang.reflect.Modifier.isPrivate(modifiers)).isFalse();
            assertThat(java.lang.reflect.Modifier.isProtected(modifiers)).isFalse();
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("类有DEFAULT_BUFFER_SIZE常量")
        void testDefaultBufferSizeConstant() throws NoSuchFieldException {
            assertThat(DefaultReactiveJsonWriter.class.getDeclaredField("DEFAULT_BUFFER_SIZE")).isNotNull();
        }

        @Test
        @DisplayName("类有ARRAY_START常量")
        void testArrayStartConstant() throws NoSuchFieldException {
            assertThat(DefaultReactiveJsonWriter.class.getDeclaredField("ARRAY_START")).isNotNull();
        }

        @Test
        @DisplayName("类有ARRAY_END常量")
        void testArrayEndConstant() throws NoSuchFieldException {
            assertThat(DefaultReactiveJsonWriter.class.getDeclaredField("ARRAY_END")).isNotNull();
        }

        @Test
        @DisplayName("类有COMMA常量")
        void testCommaConstant() throws NoSuchFieldException {
            assertThat(DefaultReactiveJsonWriter.class.getDeclaredField("COMMA")).isNotNull();
        }

        @Test
        @DisplayName("类有NEWLINE常量")
        void testNewlineConstant() throws NoSuchFieldException {
            assertThat(DefaultReactiveJsonWriter.class.getDeclaredField("NEWLINE")).isNotNull();
        }
    }
}
