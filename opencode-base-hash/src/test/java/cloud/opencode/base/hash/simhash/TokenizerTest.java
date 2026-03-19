package cloud.opencode.base.hash.simhash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tokenizer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("Tokenizer 测试")
class TokenizerTest {

    @Nested
    @DisplayName("whitespace方法测试")
    class WhitespaceTests {

        @Test
        @DisplayName("空格分词")
        void testWhitespace() {
            Tokenizer tokenizer = Tokenizer.whitespace();

            List<String> tokens = tokenizer.tokenize("hello world test");

            assertThat(tokens).containsExactly("hello", "world", "test");
        }

        @Test
        @DisplayName("多个空格")
        void testMultipleSpaces() {
            Tokenizer tokenizer = Tokenizer.whitespace();

            List<String> tokens = tokenizer.tokenize("hello   world");

            assertThat(tokens).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("制表符和换行符")
        void testTabsAndNewlines() {
            Tokenizer tokenizer = Tokenizer.whitespace();

            List<String> tokens = tokenizer.tokenize("hello\tworld\ntest");

            assertThat(tokens).containsExactly("hello", "world", "test");
        }

        @Test
        @DisplayName("空字符串")
        void testEmptyString() {
            Tokenizer tokenizer = Tokenizer.whitespace();

            List<String> tokens = tokenizer.tokenize("");

            assertThat(tokens).isEmpty();
        }

        @Test
        @DisplayName("null输入")
        void testNullInput() {
            Tokenizer tokenizer = Tokenizer.whitespace();

            List<String> tokens = tokenizer.tokenize(null);

            assertThat(tokens).isEmpty();
        }
    }

    @Nested
    @DisplayName("ngram方法测试")
    class NGramTests {

        @Test
        @DisplayName("3-gram分词")
        void testNGram3() {
            Tokenizer tokenizer = Tokenizer.ngram(3);

            List<String> tokens = tokenizer.tokenize("hello");

            assertThat(tokens).containsExactly("hel", "ell", "llo");
        }

        @Test
        @DisplayName("2-gram分词")
        void testNGram2() {
            Tokenizer tokenizer = Tokenizer.ngram(2);

            List<String> tokens = tokenizer.tokenize("abc");

            assertThat(tokens).containsExactly("ab", "bc");
        }

        @Test
        @DisplayName("1-gram分词等于字符分词")
        void testNGram1() {
            Tokenizer tokenizer = Tokenizer.ngram(1);

            List<String> tokens = tokenizer.tokenize("abc");

            assertThat(tokens).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("文本短于N")
        void testShortText() {
            Tokenizer tokenizer = Tokenizer.ngram(5);

            List<String> tokens = tokenizer.tokenize("ab");

            assertThat(tokens).containsExactly("ab");
        }

        @Test
        @DisplayName("空字符串")
        void testEmptyString() {
            Tokenizer tokenizer = Tokenizer.ngram(3);

            List<String> tokens = tokenizer.tokenize("");

            // nGram of empty string returns [""] since the text is shorter than n
            assertThat(tokens).containsExactly("");
        }

        @Test
        @DisplayName("null输入")
        void testNullInput() {
            Tokenizer tokenizer = Tokenizer.ngram(3);

            List<String> tokens = tokenizer.tokenize(null);

            assertThat(tokens).isEmpty();
        }

        @Test
        @DisplayName("N小于等于0抛出异常")
        void testInvalidN() {
            assertThatThrownBy(() -> Tokenizer.ngram(0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> Tokenizer.ngram(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("characters方法测试")
    class CharactersTests {

        @Test
        @DisplayName("字符分词")
        void testCharacters() {
            Tokenizer tokenizer = Tokenizer.characters();

            List<String> tokens = tokenizer.tokenize("abc");

            assertThat(tokens).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("包含空格")
        void testWithSpaces() {
            Tokenizer tokenizer = Tokenizer.characters();

            List<String> tokens = tokenizer.tokenize("a b");

            assertThat(tokens).containsExactly("a", " ", "b");
        }

        @Test
        @DisplayName("空字符串")
        void testEmptyString() {
            Tokenizer tokenizer = Tokenizer.characters();

            List<String> tokens = tokenizer.tokenize("");

            assertThat(tokens).isEmpty();
        }

        @Test
        @DisplayName("null输入")
        void testNullInput() {
            Tokenizer tokenizer = Tokenizer.characters();

            List<String> tokens = tokenizer.tokenize(null);

            assertThat(tokens).isEmpty();
        }
    }

    @Nested
    @DisplayName("words方法测试")
    class WordsTests {

        @Test
        @DisplayName("单词分词")
        void testWords() {
            Tokenizer tokenizer = Tokenizer.words();

            List<String> tokens = tokenizer.tokenize("hello, world!");

            assertThat(tokens).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("包含数字")
        void testWithNumbers() {
            Tokenizer tokenizer = Tokenizer.words();

            List<String> tokens = tokenizer.tokenize("test123 abc456");

            assertThat(tokens).containsExactly("test123", "abc456");
        }

        @Test
        @DisplayName("多标点符号")
        void testMultiplePunctuation() {
            Tokenizer tokenizer = Tokenizer.words();

            List<String> tokens = tokenizer.tokenize("hello...world!!!");

            assertThat(tokens).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("空字符串")
        void testEmptyString() {
            Tokenizer tokenizer = Tokenizer.words();

            List<String> tokens = tokenizer.tokenize("");

            assertThat(tokens).isEmpty();
        }

        @Test
        @DisplayName("null输入")
        void testNullInput() {
            Tokenizer tokenizer = Tokenizer.words();

            List<String> tokens = tokenizer.tokenize(null);

            assertThat(tokens).isEmpty();
        }

        @Test
        @DisplayName("只有标点")
        void testOnlyPunctuation() {
            Tokenizer tokenizer = Tokenizer.words();

            List<String> tokens = tokenizer.tokenize("...,,,!!!");

            assertThat(tokens).isEmpty();
        }
    }

    @Nested
    @DisplayName("cjkCharacters方法测试")
    class CjkCharactersTests {

        @Test
        @DisplayName("中文字符分词")
        void testCjkCharacters() {
            Tokenizer tokenizer = Tokenizer.cjkCharacters();

            List<String> tokens = tokenizer.tokenize("你好世界");

            assertThat(tokens).containsExactly("你", "好", "世", "界");
        }

        @Test
        @DisplayName("混合中英文")
        void testMixedCjkAndLatin() {
            Tokenizer tokenizer = Tokenizer.cjkCharacters();

            List<String> tokens = tokenizer.tokenize("你好hello世界");

            assertThat(tokens).containsExactly("你", "好", "世", "界");
        }

        @Test
        @DisplayName("日文字符")
        void testJapaneseCharacters() {
            Tokenizer tokenizer = Tokenizer.cjkCharacters();

            List<String> tokens = tokenizer.tokenize("日本語");

            assertThat(tokens).hasSize(3);
        }

        @Test
        @DisplayName("空字符串")
        void testEmptyString() {
            Tokenizer tokenizer = Tokenizer.cjkCharacters();

            List<String> tokens = tokenizer.tokenize("");

            assertThat(tokens).isEmpty();
        }

        @Test
        @DisplayName("null输入")
        void testNullInput() {
            Tokenizer tokenizer = Tokenizer.cjkCharacters();

            List<String> tokens = tokenizer.tokenize(null);

            assertThat(tokens).isEmpty();
        }
    }

    @Nested
    @DisplayName("combined方法测试")
    class CombinedTests {

        @Test
        @DisplayName("组合多个分词器")
        void testCombined() {
            Tokenizer combined = Tokenizer.combined(
                    Tokenizer.whitespace(),
                    Tokenizer.ngram(2)
            );

            List<String> tokens = combined.tokenize("ab cd");

            // whitespace: ["ab", "cd"]
            // ngram(2): ["ab", "b ", " c", "cd"]
            assertThat(tokens).contains("ab", "cd");
        }

        @Test
        @DisplayName("组合空分词器")
        void testCombinedEmpty() {
            Tokenizer combined = Tokenizer.combined();

            List<String> tokens = combined.tokenize("hello");

            assertThat(tokens).isEmpty();
        }
    }

    @Nested
    @DisplayName("apply方法测试")
    class ApplyTests {

        @Test
        @DisplayName("apply方法等同于tokenize")
        void testApplyEqualsTokenize() {
            Tokenizer tokenizer = Tokenizer.whitespace();

            List<String> applyResult = tokenizer.apply("hello world");
            List<String> tokenizeResult = tokenizer.tokenize("hello world");

            assertThat(applyResult).isEqualTo(tokenizeResult);
        }
    }

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("实现Tokenizer接口")
        void testImplementTokenizer() {
            Tokenizer custom = text -> List.of(text.split("-"));

            List<String> tokens = custom.tokenize("a-b-c");

            assertThat(tokens).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("作为Function使用")
        void testAsFunction() {
            Tokenizer tokenizer = Tokenizer.whitespace();

            java.util.function.Function<String, List<String>> func = tokenizer;
            List<String> tokens = func.apply("hello world");

            assertThat(tokens).containsExactly("hello", "world");
        }
    }
}
