package cloud.opencode.base.id;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * IdGenerator 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("IdGenerator 接口测试")
class IdGeneratorTest {

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("generate方法存在")
        void testGenerateExists() throws NoSuchMethodException {
            var method = IdGenerator.class.getMethod("generate");

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("getType默认方法存在")
        void testGetTypeExists() throws NoSuchMethodException {
            var method = IdGenerator.class.getMethod("getType");

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("generateBatch默认方法存在")
        void testGenerateBatchExists() throws NoSuchMethodException {
            var method = IdGenerator.class.getMethod("generateBatch", int.class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        private IdGenerator<Long> createMockGenerator() {
            return new IdGenerator<>() {
                private long counter = 0;

                @Override
                public Long generate() {
                    return ++counter;
                }
            };
        }

        @Test
        @DisplayName("getType返回类名")
        void testGetTypeDefault() {
            IdGenerator<Long> generator = createMockGenerator();

            // 对于匿名类，getClass().getSimpleName()返回空字符串
            assertThat(generator.getType()).isEmpty();
        }

        @Test
        @DisplayName("generateBatch生成指定数量")
        void testGenerateBatch() {
            IdGenerator<Long> generator = createMockGenerator();

            List<Long> ids = generator.generateBatch(5);

            assertThat(ids).hasSize(5);
            assertThat(ids).containsExactly(1L, 2L, 3L, 4L, 5L);
        }

        @Test
        @DisplayName("generateBatch数量为0返回空列表")
        void testGenerateBatchZero() {
            IdGenerator<Long> generator = createMockGenerator();

            List<Long> ids = generator.generateBatch(0);

            assertThat(ids).isEmpty();
        }

        @Test
        @DisplayName("generateBatch负数抛出异常")
        void testGenerateBatchNegative() {
            IdGenerator<Long> generator = createMockGenerator();

            // 负数会导致ArrayList构造抛出IllegalArgumentException
            assertThatThrownBy(() -> generator.generateBatch(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("自定义实现测试")
    class CustomImplementationTests {

        @Test
        @DisplayName("自定义getType")
        void testCustomGetType() {
            IdGenerator<String> generator = new IdGenerator<>() {
                @Override
                public String generate() {
                    return "custom-id";
                }

                @Override
                public String getType() {
                    return "CustomType";
                }
            };

            assertThat(generator.getType()).isEqualTo("CustomType");
        }
    }
}
