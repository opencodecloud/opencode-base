package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * EntryTransformerTest Tests
 * EntryTransformerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("EntryTransformer 测试")
class EntryTransformerTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Lambda表达式实现transformEntry")
        void testLambdaImplementation() {
            EntryTransformer<String, Integer, String> transformer =
                    (key, value) -> key + "=" + value;

            String result = transformer.transformEntry("name", 42);

            assertThat(result).isEqualTo("name=42");
        }

        @Test
        @DisplayName("键和值都参与转换")
        void testKeyAndValueBothUsed() {
            EntryTransformer<String, Integer, Integer> transformer =
                    (key, value) -> key.length() + value;

            Integer result = transformer.transformEntry("hello", 10);

            assertThat(result).isEqualTo(15);
        }

        @Test
        @DisplayName("返回不同类型的值")
        void testDifferentReturnType() {
            EntryTransformer<String, String, Boolean> transformer =
                    (key, value) -> key.equals(value);

            assertThat(transformer.transformEntry("abc", "abc")).isTrue();
            assertThat(transformer.transformEntry("abc", "xyz")).isFalse();
        }

        @Test
        @DisplayName("方法引用作为EntryTransformer")
        void testMethodReference() {
            EntryTransformer<String, String, String> transformer =
                    String::concat;

            String result = transformer.transformEntry("hello", " world");

            assertThat(result).isEqualTo("hello world");
        }
    }
}
