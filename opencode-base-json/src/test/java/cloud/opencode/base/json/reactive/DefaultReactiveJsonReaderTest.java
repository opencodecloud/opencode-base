package cloud.opencode.base.json.reactive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultReactiveJsonReader 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("DefaultReactiveJsonReader 测试")
class DefaultReactiveJsonReaderTest {

    @Nested
    @DisplayName("构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用默认缓冲区大小创建")
        void testDefaultConstructor() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));

            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            assertThat(reader).isNotNull();
            assertThat(reader.isOpen()).isTrue();
        }

        @Test
        @DisplayName("使用自定义缓冲区大小创建")
        void testCustomBufferSize() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));

            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input, 4096);

            assertThat(reader).isNotNull();
            assertThat(reader.isOpen()).isTrue();
        }

        @Test
        @DisplayName("null输入流抛出异常")
        void testNullInputStream() {
            assertThatThrownBy(() -> new DefaultReactiveJsonReader(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("input");
        }

        @Test
        @DisplayName("BufferedInputStream直接使用")
        void testBufferedInputStream() {
            InputStream input = new BufferedInputStream(
                new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8))
            );

            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            assertThat(reader).isNotNull();
        }
    }

    @Nested
    @DisplayName("isOpen方法测试")
    class IsOpenTests {

        @Test
        @DisplayName("新创建的读取器是打开的")
        void testNewReaderIsOpen() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            assertThat(reader.isOpen()).isTrue();
        }

        @Test
        @DisplayName("关闭后读取器不再打开")
        void testClosedReaderIsNotOpen() throws IOException {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            reader.close();

            assertThat(reader.isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("getElementsRead方法测试")
    class GetElementsReadTests {

        @Test
        @DisplayName("初始元素读取计数为0")
        void testInitialElementsRead() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            assertThat(reader.getElementsRead()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("关闭读取器")
        void testClose() throws IOException {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            reader.close();

            assertThat(reader.isOpen()).isFalse();
        }

        @Test
        @DisplayName("多次关闭是安全的")
        void testMultipleClose() throws IOException {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            reader.close();
            reader.close();

            assertThat(reader.isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("readValues方法测试")
    class ReadValuesTests {

        @Test
        @DisplayName("readValues返回Publisher")
        void testReadValuesReturnsPublisher() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            Flow.Publisher<String> publisher = reader.readValues(String.class);

            assertThat(publisher).isNotNull();
        }

        @Test
        @DisplayName("readValues带批量大小返回Publisher")
        void testReadValuesWithBatchSize() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            Flow.Publisher<String> publisher = reader.readValues(String.class, 10);

            assertThat(publisher).isNotNull();
        }

        @Test
        @DisplayName("readValues对null类抛出异常")
        void testReadValuesNullClass() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            assertThatThrownBy(() -> reader.readValues(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("clazz");
        }

        @Test
        @DisplayName("关闭后readValues抛出异常")
        void testReadValuesAfterClose() throws IOException {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);
            reader.close();

            assertThatThrownBy(() -> reader.readValues(String.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("readArrayElements方法测试")
    class ReadArrayElementsTests {

        @Test
        @DisplayName("readArrayElements返回Publisher")
        void testReadArrayElementsReturnsPublisher() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            Flow.Publisher<String> publisher = reader.readArrayElements(String.class);

            assertThat(publisher).isNotNull();
        }

        @Test
        @DisplayName("readArrayElements带批量大小返回Publisher")
        void testReadArrayElementsWithBatchSize() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            Flow.Publisher<String> publisher = reader.readArrayElements(String.class, 10);

            assertThat(publisher).isNotNull();
        }

        @Test
        @DisplayName("readArrayElements对null类型抛出异常")
        void testReadArrayElementsNullClass() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);

            assertThatThrownBy(() -> reader.readArrayElements(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("elementType");
        }

        @Test
        @DisplayName("关闭后readArrayElements抛出异常")
        void testReadArrayElementsAfterClose() throws IOException {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
            DefaultReactiveJsonReader reader = new DefaultReactiveJsonReader(input);
            reader.close();

            assertThatThrownBy(() -> reader.readArrayElements(String.class))
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
            assertThat(java.lang.reflect.Modifier.isFinal(DefaultReactiveJsonReader.class.getModifiers()))
                .isTrue();
        }

        @Test
        @DisplayName("实现ReactiveJsonReader接口")
        void testImplementsReactiveJsonReader() {
            assertThat(ReactiveJsonReader.class.isAssignableFrom(DefaultReactiveJsonReader.class)).isTrue();
        }

        @Test
        @DisplayName("包级别可见性")
        void testPackagePrivate() {
            int modifiers = DefaultReactiveJsonReader.class.getModifiers();
            assertThat(java.lang.reflect.Modifier.isPublic(modifiers)).isFalse();
            assertThat(java.lang.reflect.Modifier.isPrivate(modifiers)).isFalse();
            assertThat(java.lang.reflect.Modifier.isProtected(modifiers)).isFalse();
        }
    }
}
