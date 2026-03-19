package cloud.opencode.base.string.unicode;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ChineseSegmenterTest Tests
 * ChineseSegmenterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("ChineseSegmenter Tests")
class ChineseSegmenterTest {

    @Nested
    @DisplayName("Static segment Tests")
    class StaticSegmentTests {

        @Test
        @DisplayName("Should segment simple Chinese text")
        void shouldSegmentSimpleChineseText() {
            List<String> result = ChineseSegmenter.segment("我们今天");
            assertThat(result).isNotEmpty();
            assertThat(result).contains("我们");
        }

        @Test
        @DisplayName("Should return empty list for null")
        void shouldReturnEmptyListForNull() {
            assertThat(ChineseSegmenter.segment(null)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty string")
        void shouldReturnEmptyListForEmptyString() {
            assertThat(ChineseSegmenter.segment("")).isEmpty();
        }
    }

    @Nested
    @DisplayName("segmentFMM Tests")
    class SegmentFMMTests {

        @Test
        @DisplayName("Should segment with FMM algorithm")
        void shouldSegmentWithFMMAlgorithm() {
            List<String> result = ChineseSegmenter.segmentFMM("中国人民");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty for null")
        void shouldReturnEmptyForNull() {
            assertThat(ChineseSegmenter.segmentFMM(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("segmentBMM Tests")
    class SegmentBMMTests {

        @Test
        @DisplayName("Should segment with BMM algorithm")
        void shouldSegmentWithBMMAlgorithm() {
            List<String> result = ChineseSegmenter.segmentBMM("中国人民");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty for null")
        void shouldReturnEmptyForNull() {
            assertThat(ChineseSegmenter.segmentBMM(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("segmentAndJoin Tests")
    class SegmentAndJoinTests {

        @Test
        @DisplayName("Should join segments with delimiter")
        void shouldJoinSegmentsWithDelimiter() {
            String result = ChineseSegmenter.segmentAndJoin("我们今天", "/");
            assertThat(result).contains("/");
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create segmenter with builder")
        void shouldCreateSegmenterWithBuilder() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder().build();
            assertThat(segmenter).isNotNull();
        }

        @Test
        @DisplayName("Should add custom word")
        void shouldAddCustomWord() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder()
                .addWord("人工智能")
                .build();
            assertThat(segmenter.containsWord("人工智能")).isTrue();
        }

        @Test
        @DisplayName("Should add multiple words")
        void shouldAddMultipleWords() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder()
                .addWords(List.of("机器学习", "深度学习"))
                .build();
            assertThat(segmenter.containsWord("机器学习")).isTrue();
            assertThat(segmenter.containsWord("深度学习")).isTrue();
        }

        @Test
        @DisplayName("Should set max word length")
        void shouldSetMaxWordLength() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder()
                .maxWordLength(10)
                .build();
            assertThat(segmenter).isNotNull();
        }

        @Test
        @DisplayName("Should set keep punctuation")
        void shouldSetKeepPunctuation() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder()
                .keepPunctuation(true)
                .build();
            assertThat(segmenter).isNotNull();
        }

        @Test
        @DisplayName("Should set keep numbers")
        void shouldSetKeepNumbers() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder()
                .keepNumbers(false)
                .build();
            assertThat(segmenter).isNotNull();
        }

        @Test
        @DisplayName("Should ignore null word")
        void shouldIgnoreNullWord() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder()
                .addWord(null)
                .addWord("")
                .build();
            assertThat(segmenter).isNotNull();
        }

        @Test
        @DisplayName("Should ignore null words collection")
        void shouldIgnoreNullWordsCollection() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder()
                .addWords(null)
                .build();
            assertThat(segmenter).isNotNull();
        }
    }

    @Nested
    @DisplayName("Instance Methods Tests")
    class InstanceMethodsTests {

        @Test
        @DisplayName("segmentText should segment Chinese text")
        void segmentTextShouldSegmentChineseText() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder().build();
            List<String> result = segmenter.segmentText("我们今天很开心");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("segmentForward should use FMM")
        void segmentForwardShouldUseFMM() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder().build();
            List<String> result = segmenter.segmentForward("中华人民共和国");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("segmentBackward should use BMM")
        void segmentBackwardShouldUseBMM() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder().build();
            List<String> result = segmenter.segmentBackward("中华人民共和国");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("containsWord should check dictionary")
        void containsWordShouldCheckDictionary() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder()
                .addWord("测试词")
                .build();
            assertThat(segmenter.containsWord("测试词")).isTrue();
            assertThat(segmenter.containsWord("不存在")).isFalse();
        }

        @Test
        @DisplayName("getDictionarySize should return size")
        void getDictionarySizeShouldReturnSize() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder().build();
            assertThat(segmenter.getDictionarySize()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Static Dictionary Methods Tests")
    class StaticDictionaryMethodsTests {

        @Test
        @DisplayName("addToDictionary should add word")
        void addToDictionaryShouldAddWord() {
            ChineseSegmenter.addToDictionary("新增词语");
            ChineseSegmenter segmenter = ChineseSegmenter.builder().build();
            assertThat(segmenter.containsWord("新增词语")).isTrue();
        }

        @Test
        @DisplayName("addToDictionary should add collection")
        void addToDictionaryShouldAddCollection() {
            ChineseSegmenter.addToDictionary(List.of("批量词一", "批量词二"));
            ChineseSegmenter segmenter = ChineseSegmenter.builder().build();
            assertThat(segmenter.containsWord("批量词一")).isTrue();
        }

        @Test
        @DisplayName("addToDictionary should ignore null")
        void addToDictionaryShouldIgnoreNull() {
            ChineseSegmenter.addToDictionary((String) null);
            ChineseSegmenter.addToDictionary((Collection<String>) null);
            // Should not throw
        }
    }

    @Nested
    @DisplayName("getDefault Tests")
    class GetDefaultTests {

        @Test
        @DisplayName("Should return singleton instance")
        void shouldReturnSingletonInstance() {
            ChineseSegmenter first = ChineseSegmenter.getDefault();
            ChineseSegmenter second = ChineseSegmenter.getDefault();
            assertThat(first).isSameAs(second);
        }
    }

    @Nested
    @DisplayName("Mixed Text Tests")
    class MixedTextTests {

        @Test
        @DisplayName("Should handle mixed Chinese and English")
        void shouldHandleMixedChineseAndEnglish() {
            List<String> result = ChineseSegmenter.segment("Hello世界");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle numbers in text")
        void shouldHandleNumbersInText() {
            ChineseSegmenter segmenter = ChineseSegmenter.builder()
                .keepNumbers(true)
                .build();
            List<String> result = segmenter.segmentText("有123个");
            assertThat(result).isNotEmpty();
        }
    }
}
