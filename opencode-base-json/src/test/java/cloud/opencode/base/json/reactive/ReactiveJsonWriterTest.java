package cloud.opencode.base.json.reactive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.*;

/**
 * ReactiveJsonWriter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("ReactiveJsonWriter 测试")
class ReactiveJsonWriterTest {

    @Nested
    @DisplayName("接口方法定义测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("接口定义了所有必要方法")
        void testInterfaceMethods() throws NoSuchMethodException {
            // Factory methods
            assertThat(ReactiveJsonWriter.class.getMethod("create", OutputStream.class)).isNotNull();
            assertThat(ReactiveJsonWriter.class.getMethod("create", OutputStream.class, int.class)).isNotNull();
            assertThat(ReactiveJsonWriter.class.getMethod("createPretty", OutputStream.class)).isNotNull();

            // Write methods
            assertThat(ReactiveJsonWriter.class.getMethod("write", Flow.Publisher.class)).isNotNull();
            assertThat(ReactiveJsonWriter.class.getMethod("writeAsArray", Flow.Publisher.class)).isNotNull();
            assertThat(ReactiveJsonWriter.class.getMethod("writeObject", Object.class)).isNotNull();

            // Status methods
            assertThat(ReactiveJsonWriter.class.getMethod("flush")).isNotNull();
            assertThat(ReactiveJsonWriter.class.getMethod("isOpen")).isNotNull();
            assertThat(ReactiveJsonWriter.class.getMethod("getElementsWritten")).isNotNull();
            assertThat(ReactiveJsonWriter.class.getMethod("getBytesWritten")).isNotNull();

            // Lifecycle
            assertThat(ReactiveJsonWriter.class.getMethod("close")).isNotNull();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create(OutputStream)创建写入器")
        void testCreateWithOutputStream() {
            OutputStream output = new ByteArrayOutputStream();

            ReactiveJsonWriter writer = ReactiveJsonWriter.create(output);

            assertThat(writer).isNotNull();
            assertThat(writer).isInstanceOf(DefaultReactiveJsonWriter.class);
        }

        @Test
        @DisplayName("create(OutputStream,int)创建带缓冲区的写入器")
        void testCreateWithBufferSize() {
            OutputStream output = new ByteArrayOutputStream();

            ReactiveJsonWriter writer = ReactiveJsonWriter.create(output, 4096);

            assertThat(writer).isNotNull();
            assertThat(writer).isInstanceOf(DefaultReactiveJsonWriter.class);
        }

        @Test
        @DisplayName("createPretty创建美化输出的写入器")
        void testCreatePretty() {
            OutputStream output = new ByteArrayOutputStream();

            ReactiveJsonWriter writer = ReactiveJsonWriter.createPretty(output);

            assertThat(writer).isNotNull();
            assertThat(writer).isInstanceOf(DefaultReactiveJsonWriter.class);
        }
    }

    @Nested
    @DisplayName("返回类型测试")
    class ReturnTypeTests {

        @Test
        @DisplayName("write返回CompletableFuture")
        void testWriteReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonWriter.class.getMethod("write", Flow.Publisher.class).getReturnType())
                .isEqualTo(CompletableFuture.class);
        }

        @Test
        @DisplayName("writeAsArray返回CompletableFuture")
        void testWriteAsArrayReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonWriter.class.getMethod("writeAsArray", Flow.Publisher.class).getReturnType())
                .isEqualTo(CompletableFuture.class);
        }

        @Test
        @DisplayName("writeObject返回CompletableFuture")
        void testWriteObjectReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonWriter.class.getMethod("writeObject", Object.class).getReturnType())
                .isEqualTo(CompletableFuture.class);
        }

        @Test
        @DisplayName("isOpen返回boolean")
        void testIsOpenReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonWriter.class.getMethod("isOpen").getReturnType())
                .isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("getElementsWritten返回long")
        void testGetElementsWrittenReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonWriter.class.getMethod("getElementsWritten").getReturnType())
                .isEqualTo(long.class);
        }

        @Test
        @DisplayName("getBytesWritten返回long")
        void testGetBytesWrittenReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonWriter.class.getMethod("getBytesWritten").getReturnType())
                .isEqualTo(long.class);
        }

        @Test
        @DisplayName("flush返回void")
        void testFlushReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonWriter.class.getMethod("flush").getReturnType())
                .isEqualTo(void.class);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承Closeable接口")
        void testImplementsCloseable() {
            assertThat(Closeable.class.isAssignableFrom(ReactiveJsonWriter.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("静态方法测试")
    class StaticMethodTests {

        @Test
        @DisplayName("create是静态方法")
        void testCreateIsStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                ReactiveJsonWriter.class.getMethod("create", OutputStream.class).getModifiers()
            )).isTrue();
        }

        @Test
        @DisplayName("createPretty是静态方法")
        void testCreatePrettyIsStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                ReactiveJsonWriter.class.getMethod("createPretty", OutputStream.class).getModifiers()
            )).isTrue();
        }
    }
}
