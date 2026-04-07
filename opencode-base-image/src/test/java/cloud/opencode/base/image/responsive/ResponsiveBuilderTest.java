package cloud.opencode.base.image.responsive;

import cloud.opencode.base.image.Image;
import cloud.opencode.base.image.ImageFormat;
import cloud.opencode.base.image.responsive.ResponsiveBuilder.GenerateResult;
import cloud.opencode.base.image.responsive.ResponsiveBuilder.Variant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ResponsiveBuilder}.
 * {@link ResponsiveBuilder} 测试类。
 */
@DisplayName("ResponsiveBuilder - 响应式多尺寸图片生成器")
class ResponsiveBuilderTest {

    @TempDir
    Path tempDir;

    /**
     * Create a test image file with the given dimensions and a colored fill.
     */
    private Path createTestImage(String name, int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.fillOval(width / 4, height / 4, width / 2, height / 2);
        g.dispose();

        Path path = tempDir.resolve(name);
        ImageIO.write(img, "png", path.toFile());
        return path;
    }

    /**
     * Create a test Image object in memory.
     */
    private Image createTestImageObj(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return new Image(img);
    }

    @Nested
    @DisplayName("Variant 记录验证")
    class VariantTest {

        @Test
        @DisplayName("创建有效的 Variant")
        void validVariantCreation() {
            Variant variant = new Variant(64, 64, "icon");
            assertThat(variant.width()).isEqualTo(64);
            assertThat(variant.height()).isEqualTo(64);
            assertThat(variant.name()).isEqualTo("icon");
        }

        @Test
        @DisplayName("宽度小于等于零时抛出异常")
        void widthZeroOrNegativeThrowsException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Variant(0, 100, "bad"))
                    .withMessageContaining("positive");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Variant(-1, 100, "bad"))
                    .withMessageContaining("positive");
        }

        @Test
        @DisplayName("高度小于等于零时抛出异常")
        void heightZeroOrNegativeThrowsException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Variant(100, 0, "bad"))
                    .withMessageContaining("positive");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Variant(100, -5, "bad"))
                    .withMessageContaining("positive");
        }

        @Test
        @DisplayName("名称为 null 时抛出异常")
        void nullNameThrowsException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new Variant(100, 100, null));
        }
    }

    @Nested
    @DisplayName("单图生成测试")
    class SingleGenerateTest {

        private Path sourceImage;

        @BeforeEach
        void setUp() throws IOException {
            sourceImage = createTestImage("source.png", 200, 200);
        }

        @Test
        @DisplayName("2 个变体 x 1 种格式 = 生成 2 个文件")
        void twoVariantsOneFormat() throws Exception {
            Path outputDir = tempDir.resolve("output-2v1f");

            List<GenerateResult> results = ResponsiveBuilder.of(sourceImage)
                    .variant(64, 64, "icon")
                    .variant(128, 128, "thumb")
                    .generate(outputDir);

            assertThat(results).hasSize(2);
            for (GenerateResult r : results) {
                assertThat(r.path()).exists();
                assertThat(r.fileSize()).isGreaterThan(0);
                assertThat(r.format()).isEqualTo(ImageFormat.JPEG);
            }
            assertThat(results.get(0).variant().name()).isEqualTo("icon");
            assertThat(results.get(1).variant().name()).isEqualTo("thumb");
        }

        @Test
        @DisplayName("2 个变体 x 2 种格式 = 生成 4 个文件")
        void twoVariantsTwoFormats() throws Exception {
            Path outputDir = tempDir.resolve("output-2v2f");

            List<GenerateResult> results = ResponsiveBuilder.of(sourceImage)
                    .variant(64, 64, "icon")
                    .variant(128, 128, "thumb")
                    .formats(ImageFormat.JPEG, ImageFormat.PNG)
                    .generate(outputDir);

            assertThat(results).hasSize(4);
            for (GenerateResult r : results) {
                assertThat(r.path()).exists();
                assertThat(r.fileSize()).isGreaterThan(0);
            }

            // Verify files by name
            assertThat(outputDir.resolve("icon.jpg")).exists();
            assertThat(outputDir.resolve("icon.png")).exists();
            assertThat(outputDir.resolve("thumb.jpg")).exists();
            assertThat(outputDir.resolve("thumb.png")).exists();
        }

        @Test
        @DisplayName("文件存在且大小大于零")
        void filesExistAndHavePositiveSize() throws Exception {
            Path outputDir = tempDir.resolve("output-size");

            List<GenerateResult> results = ResponsiveBuilder.of(sourceImage)
                    .variant(50, 50, "small")
                    .formats(ImageFormat.PNG)
                    .generate(outputDir);

            assertThat(results).hasSize(1);
            GenerateResult r = results.getFirst();
            assertThat(Files.exists(r.path())).isTrue();
            assertThat(Files.size(r.path())).isEqualTo(r.fileSize());
            assertThat(r.fileSize()).isGreaterThan(0);
        }

        @Test
        @DisplayName("GenerateResult 包含正确的变体和格式")
        void resultHasCorrectVariantAndFormat() throws Exception {
            Path outputDir = tempDir.resolve("output-check");

            List<GenerateResult> results = ResponsiveBuilder.of(sourceImage)
                    .variant(80, 60, "banner")
                    .formats(ImageFormat.PNG)
                    .generate(outputDir);

            assertThat(results).hasSize(1);
            GenerateResult r = results.getFirst();
            assertThat(r.variant().width()).isEqualTo(80);
            assertThat(r.variant().height()).isEqualTo(60);
            assertThat(r.variant().name()).isEqualTo("banner");
            assertThat(r.format()).isEqualTo(ImageFormat.PNG);
        }

        @Test
        @DisplayName("未配置变体时抛出 IllegalStateException")
        void noVariantsThrowsException() throws Exception {
            assertThatIllegalStateException()
                    .isThrownBy(() -> ResponsiveBuilder.of(sourceImage)
                            .generate(tempDir.resolve("empty")))
                    .withMessageContaining("variant");
        }

        @Test
        @DisplayName("outputDir 为 null 时抛出 NullPointerException")
        void nullOutputDirThrowsException() throws Exception {
            assertThatNullPointerException()
                    .isThrownBy(() -> ResponsiveBuilder.of(sourceImage)
                            .variant(64, 64, "icon")
                            .generate(null));
        }

        @Test
        @DisplayName("使用 Image 对象作为源")
        void fromImageObject() throws Exception {
            Path outputDir = tempDir.resolve("output-from-image");
            Image img = createTestImageObj(200, 200);

            List<GenerateResult> results = ResponsiveBuilder.of(img)
                    .variant(50, 50, "small")
                    .formats(ImageFormat.PNG)
                    .generate(outputDir);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().path()).exists();
        }
    }

    @Nested
    @DisplayName("质量配置测试")
    class QualityTest {

        @Test
        @DisplayName("quality=0.85 正常工作")
        void defaultQualityWorks() throws Exception {
            Path sourceImage = createTestImage("q-source.png", 200, 200);
            Path outputDir = tempDir.resolve("output-quality");

            List<GenerateResult> results = ResponsiveBuilder.of(sourceImage)
                    .variant(100, 100, "test")
                    .quality(0.85f)
                    .generate(outputDir);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().fileSize()).isGreaterThan(0);
        }

        @Test
        @DisplayName("quality=0.0 正常工作")
        void zeroQualityWorks() throws Exception {
            Path sourceImage = createTestImage("q0-source.png", 200, 200);
            Path outputDir = tempDir.resolve("output-q0");

            List<GenerateResult> results = ResponsiveBuilder.of(sourceImage)
                    .variant(100, 100, "test")
                    .quality(0.0f)
                    .generate(outputDir);

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("quality=1.0 正常工作")
        void maxQualityWorks() throws Exception {
            Path sourceImage = createTestImage("q1-source.png", 200, 200);
            Path outputDir = tempDir.resolve("output-q1");

            List<GenerateResult> results = ResponsiveBuilder.of(sourceImage)
                    .variant(100, 100, "test")
                    .quality(1.0f)
                    .generate(outputDir);

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("quality 小于 0 时抛出异常")
        void negativQualityThrowsException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ResponsiveBuilder.of(createTestImageObj(10, 10))
                            .quality(-0.1f))
                    .withMessageContaining("0.0");
        }

        @Test
        @DisplayName("quality 大于 1 时抛出异常")
        void excessiveQualityThrowsException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ResponsiveBuilder.of(createTestImageObj(10, 10))
                            .quality(1.1f))
                    .withMessageContaining("1.0");
        }
    }

    @Nested
    @DisplayName("批量处理测试")
    class BatchTest {

        @Test
        @DisplayName("批量处理 2 个源 → 2 个结果列表")
        void batchTwoSources() throws Exception {
            Path src1 = createTestImage("batch1.png", 200, 200);
            Path src2 = createTestImage("batch2.png", 150, 150);
            Path outputDir = tempDir.resolve("batch-output");

            List<List<GenerateResult>> results = ResponsiveBuilder.batch(List.of(src1, src2))
                    .variant(64, 64, "icon")
                    .formats(ImageFormat.JPEG)
                    .generate(outputDir);

            assertThat(results).hasSize(2);
            assertThat(results.get(0)).hasSize(1);
            assertThat(results.get(1)).hasSize(1);

            // Each source should have its own subdirectory
            assertThat(outputDir.resolve("batch1").resolve("icon.jpg")).exists();
            assertThat(outputDir.resolve("batch2").resolve("icon.jpg")).exists();
        }

        @Test
        @DisplayName("parallel(2) 正确执行")
        void parallelExecution() throws Exception {
            Path src1 = createTestImage("par1.png", 200, 200);
            Path src2 = createTestImage("par2.png", 150, 150);
            Path outputDir = tempDir.resolve("parallel-output");

            List<List<GenerateResult>> results = ResponsiveBuilder.batch(List.of(src1, src2))
                    .variant(50, 50, "small")
                    .formats(ImageFormat.PNG)
                    .parallel(2)
                    .generate(outputDir);

            assertThat(results).hasSize(2);
            for (List<GenerateResult> sourceResults : results) {
                assertThat(sourceResults).hasSize(1);
                assertThat(sourceResults.getFirst().path()).exists();
            }
        }

        @Test
        @DisplayName("单个源失败时 onError 处理器被调用，其他源继续处理")
        void singleFailureWithErrorHandler() throws Exception {
            Path validSource = createTestImage("valid.png", 200, 200);
            Path invalidSource = tempDir.resolve("nonexistent.png");
            Path outputDir = tempDir.resolve("error-output");

            AtomicInteger errorCount = new AtomicInteger(0);

            List<List<GenerateResult>> results = ResponsiveBuilder
                    .batch(List.of(invalidSource, validSource))
                    .variant(50, 50, "small")
                    .formats(ImageFormat.PNG)
                    .onError(e -> errorCount.incrementAndGet())
                    .generate(outputDir);

            assertThat(results).hasSize(2);
            assertThat(errorCount.get()).isEqualTo(1);
            // The invalid source should produce an empty list
            assertThat(results.get(0)).isEmpty();
            // The valid source should succeed
            assertThat(results.get(1)).hasSize(1);
        }

        @Test
        @DisplayName("空源列表 → 空结果")
        void emptySourceList() throws Exception {
            Path outputDir = tempDir.resolve("empty-output");

            List<List<GenerateResult>> results = ResponsiveBuilder
                    .batch(Collections.emptyList())
                    .variant(50, 50, "small")
                    .generate(outputDir);

            assertThat(results).isEmpty();
        }
    }
}
