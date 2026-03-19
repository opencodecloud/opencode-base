package cloud.opencode.base.hash.simhash;

import cloud.opencode.base.hash.OpenHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SimHash 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("SimHash 测试")
class SimHashTest {

    @Nested
    @DisplayName("hash方法测试")
    class HashTests {

        @Test
        @DisplayName("计算文本哈希")
        void testHash() {
            SimHash simHash = OpenHash.simHash().build();

            long hash = simHash.hash("Hello, World!");

            assertThat(hash).isNotEqualTo(0);
        }

        @Test
        @DisplayName("相同文本产生相同哈希")
        void testConsistentHash() {
            SimHash simHash = OpenHash.simHash().build();

            long h1 = simHash.hash("test text");
            long h2 = simHash.hash("test text");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同文本产生不同哈希")
        void testDifferentText() {
            SimHash simHash = OpenHash.simHash().build();

            long h1 = simHash.hash("hello world");
            long h2 = simHash.hash("goodbye world");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("空文本哈希")
        void testEmptyText() {
            SimHash simHash = OpenHash.simHash().build();

            long hash = simHash.hash("");

            // 空文本应该有一个哈希值
            assertThat(hash).isNotNull();
        }
    }

    @Nested
    @DisplayName("fingerprint方法测试")
    class FingerprintTests {

        @Test
        @DisplayName("生成指纹")
        void testFingerprint() {
            SimHash simHash = OpenHash.simHash().build();

            Fingerprint fp = simHash.fingerprint("Hello, World!");

            assertThat(fp).isNotNull();
            assertThat(fp.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("相同文本产生相同指纹")
        void testConsistentFingerprint() {
            SimHash simHash = OpenHash.simHash().build();

            Fingerprint fp1 = simHash.fingerprint("test");
            Fingerprint fp2 = simHash.fingerprint("test");

            assertThat(fp1).isEqualTo(fp2);
        }
    }

    @Nested
    @DisplayName("bits方法测试")
    class BitsTests {

        @Test
        @DisplayName("默认64位")
        void testDefaultBits() {
            SimHash simHash = OpenHash.simHash().build();

            assertThat(simHash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("自定义32位")
        void testCustom32Bits() {
            SimHash simHash = OpenHash.simHash().bits(32).build();

            assertThat(simHash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("hammingDistance静态方法测试")
    class HammingDistanceTests {

        @Test
        @DisplayName("相同值距离为0")
        void testSameValueDistance() {
            long hash = 0x12345678L;

            int distance = SimHash.hammingDistance(hash, hash);

            assertThat(distance).isEqualTo(0);
        }

        @Test
        @DisplayName("完全不同的值")
        void testDifferentValues() {
            int distance = SimHash.hammingDistance(0L, -1L);

            assertThat(distance).isEqualTo(64);
        }

        @Test
        @DisplayName("部分不同的值")
        void testPartialDifference() {
            int distance = SimHash.hammingDistance(0b1010L, 0b1100L);

            assertThat(distance).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("similarity静态方法测试")
    class SimilarityTests {

        @Test
        @DisplayName("相同值相似度为1")
        void testSameValueSimilarity() {
            long hash = 0x12345678L;

            double similarity = SimHash.similarity(hash, hash, 64);

            assertThat(similarity).isEqualTo(1.0);
        }

        @Test
        @DisplayName("完全不同相似度为0")
        void testDifferentValueSimilarity() {
            double similarity = SimHash.similarity(0L, -1L, 64);

            assertThat(similarity).isEqualTo(0.0);
        }

        @Test
        @DisplayName("相似文本有较高相似度")
        void testSimilarTextSimilarity() {
            SimHash simHash = OpenHash.simHash().build();

            long h1 = simHash.hash("The quick brown fox jumps over the lazy dog");
            long h2 = simHash.hash("The quick brown fox leaps over the lazy dog");

            double similarity = SimHash.similarity(h1, h2, 64);

            assertThat(similarity).isGreaterThan(0.5);
        }
    }

    @Nested
    @DisplayName("isSimilar静态方法测试")
    class IsSimilarTests {

        @Test
        @DisplayName("相同值是相似的")
        void testSameValueIsSimilar() {
            long hash = 0x12345678L;

            boolean similar = SimHash.isSimilar(hash, hash, 3);

            assertThat(similar).isTrue();
        }

        @Test
        @DisplayName("在阈值内是相似的")
        void testWithinThreshold() {
            long h1 = 0b1010L;
            long h2 = 0b1000L; // 1 bit different

            boolean similar = SimHash.isSimilar(h1, h2, 3);

            assertThat(similar).isTrue();
        }

        @Test
        @DisplayName("超出阈值不相似")
        void testBeyondThreshold() {
            long h1 = 0b1010L;
            long h2 = 0b0101L; // 4 bits different

            boolean similar = SimHash.isSimilar(h1, h2, 2);

            assertThat(similar).isFalse();
        }
    }

    @Nested
    @DisplayName("builder静态方法测试")
    class BuilderTests {

        @Test
        @DisplayName("创建builder")
        void testBuilder() {
            SimHashBuilder builder = SimHash.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("通过builder构建SimHash")
        void testBuildFromBuilder() {
            SimHash simHash = SimHash.builder()
                    .nGram(3)
                    .bits(64)
                    .build();

            assertThat(simHash).isNotNull();
        }
    }

    @Nested
    @DisplayName("create静态方法测试")
    class CreateTests {

        @Test
        @DisplayName("创建默认SimHash")
        void testCreate() {
            SimHash simHash = SimHash.create();

            assertThat(simHash).isNotNull();
            assertThat(simHash.bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("文本相似度测试")
    class TextSimilarityTests {

        @Test
        @DisplayName("相似文本检测")
        void testSimilarTextDetection() {
            SimHash simHash = OpenHash.simHash().nGram(3).build();

            String text1 = "This is a sample document for testing";
            String text2 = "This is a sample document for testing purposes";

            Fingerprint fp1 = simHash.fingerprint(text1);
            Fingerprint fp2 = simHash.fingerprint(text2);

            assertThat(fp1.similarity(fp2)).isGreaterThan(0.7);
        }

        @Test
        @DisplayName("不相似文本检测")
        void testDissimilarTextDetection() {
            SimHash simHash = OpenHash.simHash().nGram(3).build();

            String text1 = "The weather is nice today";
            String text2 = "Programming in Java is fun";

            Fingerprint fp1 = simHash.fingerprint(text1);
            Fingerprint fp2 = simHash.fingerprint(text2);

            assertThat(fp1.similarity(fp2)).isLessThan(0.8);
        }
    }

    @Nested
    @DisplayName("不同分词器测试")
    class TokenizerTests {

        @Test
        @DisplayName("使用N-gram分词")
        void testNgramTokenizer() {
            SimHash simHash = OpenHash.simHash().nGram(2).build();

            long hash = simHash.hash("hello");

            assertThat(hash).isNotEqualTo(0);
        }

        @Test
        @DisplayName("使用空格分词")
        void testWhitespaceTokenizer() {
            SimHash simHash = OpenHash.simHash().whitespaceTokenizer().build();

            long hash = simHash.hash("hello world");

            assertThat(hash).isNotEqualTo(0);
        }

        @Test
        @DisplayName("使用单词分词")
        void testWordTokenizer() {
            SimHash simHash = OpenHash.simHash().wordTokenizer().build();

            long hash = simHash.hash("hello, world!");

            assertThat(hash).isNotEqualTo(0);
        }

        @Test
        @DisplayName("使用字符分词")
        void testCharacterTokenizer() {
            SimHash simHash = OpenHash.simHash().characterTokenizer().build();

            long hash = simHash.hash("abc");

            assertThat(hash).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("中文文本测试")
    class ChineseTextTests {

        @Test
        @DisplayName("中文文本哈希")
        void testChineseText() {
            SimHash simHash = OpenHash.simHash().nGram(2).build();

            long hash = simHash.hash("你好世界");

            assertThat(hash).isNotEqualTo(0);
        }

        @Test
        @DisplayName("相似中文文本")
        void testSimilarChineseText() {
            SimHash simHash = OpenHash.simHash().nGram(2).build();

            Fingerprint fp1 = simHash.fingerprint("今天天气很好");
            Fingerprint fp2 = simHash.fingerprint("今天天气不错");

            assertThat(fp1.similarity(fp2)).isGreaterThan(0.5);
        }
    }
}
