package cloud.opencode.base.json.reactive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.*;

/**
 * ReactiveJsonReader 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("ReactiveJsonReader 测试")
class ReactiveJsonReaderTest {

    @Nested
    @DisplayName("接口方法定义测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("接口定义了所有必要方法")
        void testInterfaceMethods() throws NoSuchMethodException {
            // Factory methods
            assertThat(ReactiveJsonReader.class.getMethod("create", InputStream.class)).isNotNull();
            assertThat(ReactiveJsonReader.class.getMethod("create", InputStream.class, int.class)).isNotNull();

            // Read methods
            assertThat(ReactiveJsonReader.class.getMethod("readValues", Class.class)).isNotNull();
            assertThat(ReactiveJsonReader.class.getMethod("readValues", Class.class, int.class)).isNotNull();
            assertThat(ReactiveJsonReader.class.getMethod("readArrayElements", Class.class)).isNotNull();
            assertThat(ReactiveJsonReader.class.getMethod("readArrayElements", Class.class, int.class)).isNotNull();

            // Status methods
            assertThat(ReactiveJsonReader.class.getMethod("isOpen")).isNotNull();
            assertThat(ReactiveJsonReader.class.getMethod("getElementsRead")).isNotNull();

            // Lifecycle
            assertThat(ReactiveJsonReader.class.getMethod("close")).isNotNull();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create(InputStream)创建读取器")
        void testCreateWithInputStream() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));

            ReactiveJsonReader reader = ReactiveJsonReader.create(input);

            assertThat(reader).isNotNull();
            assertThat(reader).isInstanceOf(DefaultReactiveJsonReader.class);
        }

        @Test
        @DisplayName("create(InputStream,int)创建带缓冲区的读取器")
        void testCreateWithBufferSize() {
            InputStream input = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));

            ReactiveJsonReader reader = ReactiveJsonReader.create(input, 4096);

            assertThat(reader).isNotNull();
            assertThat(reader).isInstanceOf(DefaultReactiveJsonReader.class);
        }
    }

    @Nested
    @DisplayName("返回类型测试")
    class ReturnTypeTests {

        @Test
        @DisplayName("readValues返回Flow.Publisher")
        void testReadValuesReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonReader.class.getMethod("readValues", Class.class).getReturnType())
                .isEqualTo(Flow.Publisher.class);
        }

        @Test
        @DisplayName("readArrayElements返回Flow.Publisher")
        void testReadArrayElementsReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonReader.class.getMethod("readArrayElements", Class.class).getReturnType())
                .isEqualTo(Flow.Publisher.class);
        }

        @Test
        @DisplayName("isOpen返回boolean")
        void testIsOpenReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonReader.class.getMethod("isOpen").getReturnType())
                .isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("getElementsRead返回long")
        void testGetElementsReadReturnType() throws NoSuchMethodException {
            assertThat(ReactiveJsonReader.class.getMethod("getElementsRead").getReturnType())
                .isEqualTo(long.class);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承Closeable接口")
        void testImplementsCloseable() {
            assertThat(Closeable.class.isAssignableFrom(ReactiveJsonReader.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("静态方法测试")
    class StaticMethodTests {

        @Test
        @DisplayName("create是静态方法")
        void testCreateIsStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                ReactiveJsonReader.class.getMethod("create", InputStream.class).getModifiers()
            )).isTrue();
        }
    }
}
