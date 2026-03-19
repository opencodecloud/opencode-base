package cloud.opencode.base.hash.simhash;

import cloud.opencode.base.hash.OpenHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SimHashBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("SimHashBuilder 测试")
class SimHashBuilderTest {

    @Nested
    @DisplayName("tokenizer方法测试")
    class TokenizerTests {

        @Test
        @DisplayName("设置Function分词器")
        void testFunctionTokenizer() {
            SimHash simHash = OpenHash.simHash()
                    .tokenizer(text -> Arrays.asList(text.split(",")))
                    .build();

            long hash = simHash.hash("a,b,c");
            assertThat(hash).isNotEqualTo(0);
        }

        @Test
        @DisplayName("设置Tokenizer实例")
        void testTokenizerInstance() {
            SimHash simHash = OpenHash.simHash()
                    .tokenizer(Tokenizer.whitespace())
                    .build();

            long hash = simHash.hash("hello world");
            assertThat(hash).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("nGram方法测试")
    class NGramTests {

        @Test
        @DisplayName("设置N-gram分词")
        void testNGram() {
            SimHash simHash = OpenHash.simHash()
                    .nGram(3)
                    .build();

            long hash = simHash.hash("hello");
            assertThat(hash).isNotEqualTo(0);
        }

        @Test
        @DisplayName("不同N值产生不同结果")
        void testDifferentNValues() {
            SimHash simHash2 = OpenHash.simHash().nGram(2).build();
            SimHash simHash4 = OpenHash.simHash().nGram(4).build();

            long h2 = simHash2.hash("hello world");
            long h4 = simHash4.hash("hello world");

            // 不同的N-gram大小通常产生不同的哈希
            assertThat(h2).isNotEqualTo(h4);
        }

        @Test
        @DisplayName("N=1等于字符分词")
        void testNGram1() {
            SimHash simHash = OpenHash.simHash().nGram(1).build();

            long hash = simHash.hash("abc");
            assertThat(hash).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("whitespaceTokenizer方法测试")
    class WhitespaceTokenizerTests {

        @Test
        @DisplayName("使用空格分词")
        void testWhitespaceTokenizer() {
            SimHash simHash = OpenHash.simHash()
                    .whitespaceTokenizer()
                    .build();

            long hash = simHash.hash("hello world test");
            assertThat(hash).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("wordTokenizer方法测试")
    class WordTokenizerTests {

        @Test
        @DisplayName("使用单词分词")
        void testWordTokenizer() {
            SimHash simHash = OpenHash.simHash()
                    .wordTokenizer()
                    .build();

            long hash = simHash.hash("hello, world! how are you?");
            assertThat(hash).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("characterTokenizer方法测试")
    class CharacterTokenizerTests {

        @Test
        @DisplayName("使用字符分词")
        void testCharacterTokenizer() {
            SimHash simHash = OpenHash.simHash()
                    .characterTokenizer()
                    .build();

            long hash = simHash.hash("abc");
            assertThat(hash).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("hashFunction方法测试")
    class HashFunctionTests {

        @Test
        @DisplayName("设置哈希函数")
        void testHashFunction() {
            SimHash simHash = OpenHash.simHash()
                    .hashFunction(OpenHash.xxHash64())
                    .build();

            long hash = simHash.hash("test");
            assertThat(hash).isNotEqualTo(0);
        }

        @Test
        @DisplayName("不同哈希函数产生不同结果")
        void testDifferentHashFunctions() {
            SimHash murmur = OpenHash.simHash()
                    .hashFunction(OpenHash.murmur3_128())
                    .build();

            SimHash xxhash = OpenHash.simHash()
                    .hashFunction(OpenHash.xxHash64())
                    .build();

            long h1 = murmur.hash("test");
            long h2 = xxhash.hash("test");

            // 不同哈希函数通常产生不同结果
            assertThat(h1).isNotEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("bits方法测试")
    class BitsTests {

        @Test
        @DisplayName("设置64位")
        void test64Bits() {
            SimHash simHash = OpenHash.simHash()
                    .bits(64)
                    .build();

            assertThat(simHash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("设置32位")
        void test32Bits() {
            SimHash simHash = OpenHash.simHash()
                    .bits(32)
                    .build();

            assertThat(simHash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("无效位数抛出异常")
        void testInvalidBits() {
            assertThatThrownBy(() -> OpenHash.simHash().bits(16).build())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("weightFunction方法测试")
    class WeightFunctionTests {

        @Test
        @DisplayName("设置权重函数")
        void testWeightFunction() {
            SimHash simHash = OpenHash.simHash()
                    .weightFunction(token -> token.length() * 2)
                    .build();

            long hash = simHash.hash("hello world");
            assertThat(hash).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("lengthWeighted方法测试")
    class LengthWeightedTests {

        @Test
        @DisplayName("使用长度作为权重")
        void testLengthWeighted() {
            SimHash simHash = OpenHash.simHash()
                    .whitespaceTokenizer()
                    .lengthWeighted()
                    .build();

            long hash = simHash.hash("a bb ccc");
            assertThat(hash).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("uniformWeight方法测试")
    class UniformWeightTests {

        @Test
        @DisplayName("使用均匀权重")
        void testUniformWeight() {
            SimHash simHash = OpenHash.simHash()
                    .uniformWeight()
                    .build();

            long hash = simHash.hash("hello world");
            assertThat(hash).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("build方法测试")
    class BuildTests {

        @Test
        @DisplayName("构建SimHash")
        void testBuild() {
            SimHash simHash = OpenHash.simHash().build();

            assertThat(simHash).isNotNull();
        }

        @Test
        @DisplayName("使用默认设置构建")
        void testBuildWithDefaults() {
            SimHash simHash = OpenHash.simHash().build();

            assertThat(simHash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("完整链式调用构建")
        void testFullChainBuild() {
            SimHash simHash = OpenHash.simHash()
                    .nGram(3)
                    .hashFunction(OpenHash.murmur3_128())
                    .bits(64)
                    .lengthWeighted()
                    .build();

            assertThat(simHash).isNotNull();
            assertThat(simHash.bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("配置组合测试")
    class CombinationTests {

        @Test
        @DisplayName("自定义分词器和哈希函数")
        void testCustomTokenizerAndHashFunction() {
            SimHash simHash = OpenHash.simHash()
                    .tokenizer(text -> List.of(text.split("\\s+")))
                    .hashFunction(OpenHash.sha256())
                    .bits(64)
                    .build();

            long hash = simHash.hash("hello world test");
            assertThat(hash).isNotEqualTo(0);
        }

        @Test
        @DisplayName("N-gram和权重函数")
        void testNGramAndWeight() {
            SimHash simHash = OpenHash.simHash()
                    .nGram(2)
                    .weightFunction(s -> 1)
                    .build();

            long hash = simHash.hash("hello");
            assertThat(hash).isNotEqualTo(0);
        }
    }
}
