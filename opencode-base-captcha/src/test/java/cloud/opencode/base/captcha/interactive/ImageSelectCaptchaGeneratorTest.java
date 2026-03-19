/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.captcha.interactive;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.generator.CaptchaGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.time.Duration;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ImageSelectCaptchaGenerator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("ImageSelectCaptchaGenerator Tests")
class ImageSelectCaptchaGeneratorTest {

    private static final Set<String> VALID_CATEGORIES = Set.of("circle", "square", "triangle", "star");

    private ImageSelectCaptchaGenerator generator;
    private CaptchaConfig defaultConfig;

    @BeforeEach
    void setUp() {
        generator = new ImageSelectCaptchaGenerator();
        defaultConfig = CaptchaConfig.builder()
                .width(300)
                .height(300)
                .expireTime(Duration.ofMinutes(5))
                .build();
    }

    @Nested
    @DisplayName("Basic Generation Tests")
    class BasicGenerationTests {

        @Test
        @DisplayName("generate returns non-null captcha")
        void generateReturnsNonNullCaptcha() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha).isNotNull();
        }

        @Test
        @DisplayName("generate returns captcha with valid id")
        void generateReturnsCaptchaWithValidId() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.id()).isNotNull();
            assertThat(captcha.id()).isNotEmpty();
            assertThat(captcha.id()).hasSize(32);
        }

        @Test
        @DisplayName("generate returns captcha with IMAGE_SELECT type")
        void generateReturnsCaptchaWithImageSelectType() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.type()).isEqualTo(CaptchaType.IMAGE_SELECT);
        }

        @Test
        @DisplayName("generate returns captcha with non-null image data")
        void generateReturnsCaptchaWithImageData() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.imageData()).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }

        @Test
        @DisplayName("generate returns captcha with valid timestamps")
        void generateReturnsCaptchaWithValidTimestamps() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.createdAt()).isNotNull();
            assertThat(captcha.expiresAt()).isNotNull();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }

        @Test
        @DisplayName("generate returns captcha that is not expired")
        void generateReturnsCaptchaThatIsNotExpired() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.isExpired()).isFalse();
        }

        @Test
        @DisplayName("generate returns captcha with non-null answer")
        void generateReturnsCaptchaWithAnswer() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.answer()).isNotNull();
            assertThat(captcha.answer()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("metadata contains width matching grid calculation")
        void metadataContainsWidth() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("width");
            // Width = GRID_SIZE * CELL_SIZE + (GRID_SIZE - 1) * GAP = 3 * 80 + 2 * 4 = 248
            assertThat((Integer) captcha.metadata().get("width")).isEqualTo(248);
        }

        @Test
        @DisplayName("metadata contains height matching grid calculation")
        void metadataContainsHeight() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("height");
            assertThat((Integer) captcha.metadata().get("height")).isEqualTo(248);
        }

        @Test
        @DisplayName("metadata contains gridSize of 3")
        void metadataContainsGridSize() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("gridSize");
            assertThat((Integer) captcha.metadata().get("gridSize")).isEqualTo(3);
        }

        @Test
        @DisplayName("metadata contains cellSize of 80")
        void metadataContainsCellSize() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("cellSize");
            assertThat((Integer) captcha.metadata().get("cellSize")).isEqualTo(80);
        }

        @Test
        @DisplayName("metadata contains gap of 4")
        void metadataContainsGap() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("gap");
            assertThat((Integer) captcha.metadata().get("gap")).isEqualTo(4);
        }

        @Test
        @DisplayName("metadata contains valid targetCategory")
        void metadataContainsValidTargetCategory() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("targetCategory");
            String category = (String) captcha.metadata().get("targetCategory");
            assertThat(VALID_CATEGORIES).contains(category);
        }

        @Test
        @DisplayName("metadata contains targetCount between 2 and 4")
        void metadataContainsTargetCount() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("targetCount");
            int count = (Integer) captcha.metadata().get("targetCount");
            assertThat(count).isBetween(2, 4);
        }

        @Test
        @DisplayName("metadata contains targetIndices list")
        void metadataContainsTargetIndices() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("targetIndices");
            @SuppressWarnings("unchecked")
            List<Integer> indices = (List<Integer>) captcha.metadata().get("targetIndices");
            assertThat(indices).isNotEmpty();
        }

        @Test
        @DisplayName("metadata contains prompt")
        void metadataContainsPrompt() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("prompt");
            String prompt = (String) captcha.metadata().get("prompt");
            assertThat(prompt).startsWith("Select all ");
            assertThat(prompt).endsWith(" shapes");
        }

        @Test
        @DisplayName("metadata contains all expected keys")
        void metadataContainsAllExpectedKeys() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKeys(
                    "width", "height", "gridSize", "cellSize", "gap",
                    "targetCategory", "targetCount", "targetIndices", "prompt"
            );
        }
    }

    @Nested
    @DisplayName("Target Category Tests")
    class TargetCategoryTests {

        @RepeatedTest(20)
        @DisplayName("target category is always one of valid categories")
        void targetCategoryIsValid() {
            Captcha captcha = generator.generate(defaultConfig);
            String category = (String) captcha.metadata().get("targetCategory");
            assertThat(VALID_CATEGORIES).contains(category);
        }

        @Test
        @DisplayName("prompt includes target category name")
        void promptIncludesTargetCategory() {
            Captcha captcha = generator.generate(defaultConfig);

            String category = (String) captcha.metadata().get("targetCategory");
            String prompt = (String) captcha.metadata().get("prompt");
            assertThat(prompt).contains(category);
        }

        @Test
        @DisplayName("different categories are generated over multiple runs")
        void differentCategoriesGenerated() {
            Set<String> generatedCategories = new HashSet<>();
            for (int i = 0; i < 50; i++) {
                Captcha captcha = generator.generate(defaultConfig);
                generatedCategories.add((String) captcha.metadata().get("targetCategory"));
            }

            assertThat(generatedCategories.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("prompt format is 'Select all <category> shapes'")
        void promptFormatIsCorrect() {
            Captcha captcha = generator.generate(defaultConfig);

            String category = (String) captcha.metadata().get("targetCategory");
            String prompt = (String) captcha.metadata().get("prompt");

            assertThat(prompt).isEqualTo("Select all " + category + " shapes");
        }
    }

    @Nested
    @DisplayName("Target Indices Tests")
    class TargetIndicesTests {

        @Test
        @DisplayName("target indices are within grid bounds (0-8)")
        void targetIndicesWithinBounds() {
            Captcha captcha = generator.generate(defaultConfig);

            @SuppressWarnings("unchecked")
            List<Integer> indices = (List<Integer>) captcha.metadata().get("targetIndices");
            int gridSize = (Integer) captcha.metadata().get("gridSize");
            int totalCells = gridSize * gridSize;

            for (Integer index : indices) {
                assertThat(index).isBetween(0, totalCells - 1);
            }
        }

        @Test
        @DisplayName("target indices are unique")
        void targetIndicesAreUnique() {
            Captcha captcha = generator.generate(defaultConfig);

            @SuppressWarnings("unchecked")
            List<Integer> indices = (List<Integer>) captcha.metadata().get("targetIndices");

            Set<Integer> uniqueIndices = new HashSet<>(indices);
            assertThat(uniqueIndices).hasSize(indices.size());
        }

        @Test
        @DisplayName("target indices are sorted")
        void targetIndicesAreSorted() {
            Captcha captcha = generator.generate(defaultConfig);

            @SuppressWarnings("unchecked")
            List<Integer> indices = (List<Integer>) captcha.metadata().get("targetIndices");

            for (int i = 0; i < indices.size() - 1; i++) {
                assertThat(indices.get(i)).isLessThanOrEqualTo(indices.get(i + 1));
            }
        }

        @Test
        @DisplayName("number of targets matches targetCount")
        void numberOfTargetsMatchesCount() {
            Captcha captcha = generator.generate(defaultConfig);

            int targetCount = (Integer) captcha.metadata().get("targetCount");
            @SuppressWarnings("unchecked")
            List<Integer> indices = (List<Integer>) captcha.metadata().get("targetIndices");

            assertThat(indices).hasSize(targetCount);
        }

        @RepeatedTest(10)
        @DisplayName("target indices are consistently valid")
        void targetIndicesConsistentlyValid() {
            Captcha captcha = generator.generate(defaultConfig);

            @SuppressWarnings("unchecked")
            List<Integer> indices = (List<Integer>) captcha.metadata().get("targetIndices");

            assertThat(indices).isNotEmpty();
            assertThat(indices).allMatch(idx -> idx >= 0 && idx < 9);
        }
    }

    @Nested
    @DisplayName("Answer Format Tests")
    class AnswerFormatTests {

        @Test
        @DisplayName("answer is comma-separated indices")
        void answerIsCommaSeparatedIndices() {
            Captcha captcha = generator.generate(defaultConfig);

            String answer = captcha.answer();
            assertThat(answer).isNotEmpty();
            assertThat(answer).matches("[0-9,]+");
        }

        @Test
        @DisplayName("answer indices are parseable as integers")
        void answerIndicesAreParseable() {
            Captcha captcha = generator.generate(defaultConfig);

            String[] parts = captcha.answer().split(",");
            for (String part : parts) {
                assertThatCode(() -> Integer.parseInt(part))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("answer matches target indices in metadata")
        void answerMatchesTargetIndices() {
            Captcha captcha = generator.generate(defaultConfig);

            @SuppressWarnings("unchecked")
            List<Integer> indices = (List<Integer>) captcha.metadata().get("targetIndices");

            String[] parts = captcha.answer().split(",");
            assertThat(parts).hasSize(indices.size());

            for (int i = 0; i < parts.length; i++) {
                int answerIndex = Integer.parseInt(parts[i]);
                assertThat(answerIndex).isEqualTo(indices.get(i));
            }
        }

        @Test
        @DisplayName("number of answer parts matches target count")
        void numberOfAnswerPartsMatchesTargetCount() {
            Captcha captcha = generator.generate(defaultConfig);

            int targetCount = (Integer) captcha.metadata().get("targetCount");
            String[] parts = captcha.answer().split(",");

            assertThat(parts).hasSize(targetCount);
        }
    }

    @Nested
    @DisplayName("Image Data Tests")
    class ImageDataTests {

        @Test
        @DisplayName("image data is valid PNG")
        void imageDataIsValidPng() {
            Captcha captcha = generator.generate(defaultConfig);

            byte[] imageData = captcha.imageData();
            assertThat(imageData[0]).isEqualTo((byte) 0x89);
            assertThat(imageData[1]).isEqualTo((byte) 0x50);
            assertThat(imageData[2]).isEqualTo((byte) 0x4E);
            assertThat(imageData[3]).isEqualTo((byte) 0x47);
        }

        @Test
        @DisplayName("toBase64 returns valid base64 string")
        void toBase64ReturnsValidString() {
            Captcha captcha = generator.generate(defaultConfig);

            String base64 = captcha.toBase64();
            assertThat(base64).isNotEmpty();

            byte[] decoded = Base64.getDecoder().decode(base64);
            assertThat(decoded).isEqualTo(captcha.imageData());
        }

        @Test
        @DisplayName("toBase64DataUrl returns correct format")
        void toBase64DataUrlReturnsCorrectFormat() {
            Captcha captcha = generator.generate(defaultConfig);

            String dataUrl = captcha.toBase64DataUrl();
            assertThat(dataUrl).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("getMimeType returns image/png")
        void getMimeTypeReturnsPng() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("image data has reasonable size")
        void imageDataHasReasonableSize() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.imageData().length).isGreaterThan(1000);
            assertThat(captcha.imageData().length).isLessThan(500000);
        }
    }

    @Nested
    @DisplayName("Grid Structure Tests")
    class GridStructureTests {

        @Test
        @DisplayName("grid is 3x3")
        void gridIsThreeByThree() {
            Captcha captcha = generator.generate(defaultConfig);

            int gridSize = (Integer) captcha.metadata().get("gridSize");
            assertThat(gridSize).isEqualTo(3);
        }

        @Test
        @DisplayName("total cells is 9")
        void totalCellsIsNine() {
            Captcha captcha = generator.generate(defaultConfig);

            int gridSize = (Integer) captcha.metadata().get("gridSize");
            assertThat(gridSize * gridSize).isEqualTo(9);
        }

        @Test
        @DisplayName("image dimensions match grid calculation")
        void imageDimensionsMatchGrid() {
            Captcha captcha = generator.generate(defaultConfig);

            int gridSize = (Integer) captcha.metadata().get("gridSize");
            int cellSize = (Integer) captcha.metadata().get("cellSize");
            int gap = (Integer) captcha.metadata().get("gap");

            int expectedSize = gridSize * cellSize + (gridSize - 1) * gap;

            assertThat((Integer) captcha.metadata().get("width")).isEqualTo(expectedSize);
            assertThat((Integer) captcha.metadata().get("height")).isEqualTo(expectedSize);
        }

        @Test
        @DisplayName("grid width equals grid height (square grid)")
        void gridIsSquare() {
            Captcha captcha = generator.generate(defaultConfig);

            int width = (Integer) captcha.metadata().get("width");
            int height = (Integer) captcha.metadata().get("height");

            assertThat(width).isEqualTo(height);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns IMAGE_SELECT")
        void getTypeReturnsImageSelect() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.IMAGE_SELECT);
        }

        @Test
        @DisplayName("captcha type is interactive")
        void captchaTypeIsInteractive() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.type().isInteractive()).isTrue();
        }

        @Test
        @DisplayName("captcha type is not text-based")
        void captchaTypeIsNotTextBased() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.type().isTextBased()).isFalse();
        }

        @Test
        @DisplayName("implements CaptchaGenerator interface")
        void implementsCaptchaGeneratorInterface() {
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("created via CaptchaGenerator.forType(IMAGE_SELECT)")
        void createdViaForTypeFactory() {
            CaptchaGenerator factoryGenerator = CaptchaGenerator.forType(CaptchaType.IMAGE_SELECT);

            assertThat(factoryGenerator).isInstanceOf(ImageSelectCaptchaGenerator.class);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("respects custom expire time")
        void respectsCustomExpireTime() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(300)
                    .height(300)
                    .expireTime(Duration.ofMinutes(10))
                    .build();

            Captcha captcha = generator.generate(config);

            long durationMillis = captcha.expiresAt().toEpochMilli() - captcha.createdAt().toEpochMilli();
            assertThat(durationMillis).isCloseTo(Duration.ofMinutes(10).toMillis(), within(1000L));
        }

        @Test
        @DisplayName("grid size is fixed regardless of config dimensions")
        void gridSizeIsFixed() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(500)
                    .height(400)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat((Integer) captcha.metadata().get("gridSize")).isEqualTo(3);
            assertThat((Integer) captcha.metadata().get("cellSize")).isEqualTo(80);
        }

        @Test
        @DisplayName("cell size is fixed at 80")
        void cellSizeIsFixed() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat((Integer) captcha.metadata().get("cellSize")).isEqualTo(80);
        }

        @Test
        @DisplayName("gap is fixed at 4")
        void gapIsFixed() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat((Integer) captcha.metadata().get("gap")).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Randomness Tests")
    class RandomnessTests {

        @Test
        @DisplayName("generates different target indices each time")
        void generatesDifferentTargetIndices() {
            Set<String> allIndices = new HashSet<>();
            for (int i = 0; i < 20; i++) {
                Captcha captcha = generator.generate(defaultConfig);
                @SuppressWarnings("unchecked")
                List<Integer> indices = (List<Integer>) captcha.metadata().get("targetIndices");
                allIndices.add(indices.toString());
            }

            assertThat(allIndices.size()).isGreaterThan(5);
        }

        @Test
        @DisplayName("generates unique ids")
        void generatesUniqueIds() {
            String[] ids = new String[10];
            for (int i = 0; i < 10; i++) {
                ids[i] = generator.generate(defaultConfig).id();
            }

            assertThat(ids).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("generates different image data each time")
        void generatesDifferentImageData() {
            Captcha captcha1 = generator.generate(defaultConfig);
            Captcha captcha2 = generator.generate(defaultConfig);

            assertThat(captcha1.imageData()).isNotEqualTo(captcha2.imageData());
        }

        @Test
        @DisplayName("target count varies within expected range (2-4)")
        void targetCountVaries() {
            Set<Integer> counts = new HashSet<>();
            for (int i = 0; i < 50; i++) {
                Captcha captcha = generator.generate(defaultConfig);
                counts.add((Integer) captcha.metadata().get("targetCount"));
            }

            assertThat(counts.size()).isGreaterThan(1);
            assertThat(counts).allMatch(c -> c >= 2 && c <= 4);
        }

        @Test
        @DisplayName("different categories appear over multiple generations")
        void differentCategoriesAppear() {
            Set<String> categories = new HashSet<>();
            for (int i = 0; i < 50; i++) {
                Captcha captcha = generator.generate(defaultConfig);
                categories.add((String) captcha.metadata().get("targetCategory"));
            }

            assertThat(categories.size()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("Default Generate Tests")
    class DefaultGenerateTests {

        @Test
        @DisplayName("generate() without config uses defaults")
        void generateWithoutConfigUsesDefaults() {
            Captcha captcha = generator.generate();

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotNull().isNotEmpty();
            assertThat(captcha.answer()).isNotNull().isNotEmpty();
            assertThat(captcha.type()).isEqualTo(CaptchaType.IMAGE_SELECT);
        }
    }
}
