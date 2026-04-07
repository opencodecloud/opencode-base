package cloud.opencode.base.neural.tensor;

import cloud.opencode.base.neural.exception.TensorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ImageToTensor}.
 */
@DisplayName("ImageToTensor — 图像到张量桥接工具测试")
class ImageToTensorTest {

    @Nested
    @DisplayName("基本转换测试")
    class BasicConversionTest {

        @Test
        @DisplayName("2x2 红色图像 → tensor[1,3,2,2]，R=1.0, G=0, B=0")
        void redImage() {
            BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 2, 2);
            g.dispose();

            Tensor tensor = ImageToTensor.convert(image);

            assertThat(tensor.shape()).isEqualTo(Shape.of(1, 3, 2, 2));

            // Channel 0 (R) = 1.0 for all pixels
            assertThat(tensor.getFloat(0, 0, 0, 0)).isEqualTo(1.0f);
            assertThat(tensor.getFloat(0, 0, 0, 1)).isEqualTo(1.0f);
            assertThat(tensor.getFloat(0, 0, 1, 0)).isEqualTo(1.0f);
            assertThat(tensor.getFloat(0, 0, 1, 1)).isEqualTo(1.0f);

            // Channel 1 (G) = 0.0
            assertThat(tensor.getFloat(0, 1, 0, 0)).isEqualTo(0.0f);
            assertThat(tensor.getFloat(0, 1, 0, 1)).isEqualTo(0.0f);

            // Channel 2 (B) = 0.0
            assertThat(tensor.getFloat(0, 2, 0, 0)).isEqualTo(0.0f);
            assertThat(tensor.getFloat(0, 2, 0, 1)).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("2x2 白色图像 → 所有通道值为 1.0")
        void whiteImage() {
            BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 2, 2);
            g.dispose();

            Tensor tensor = ImageToTensor.convert(image);

            assertThat(tensor.shape()).isEqualTo(Shape.of(1, 3, 2, 2));

            // All channels should be 1.0
            for (int c = 0; c < 3; c++) {
                for (int y = 0; y < 2; y++) {
                    for (int x = 0; x < 2; x++) {
                        assertThat(tensor.getFloat(0, c, y, x)).isEqualTo(1.0f);
                    }
                }
            }
        }

        @Test
        @DisplayName("1x1 特定颜色图像归一化正确")
        void specificColorNormalization() {
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            // Set pixel to (128, 64, 32) RGB
            image.setRGB(0, 0, new Color(128, 64, 32).getRGB());

            Tensor tensor = ImageToTensor.convert(image);

            assertThat(tensor.shape()).isEqualTo(Shape.of(1, 3, 1, 1));
            assertThat(tensor.getFloat(0, 0, 0, 0)).isCloseTo(128.0f / 255.0f, within(1e-5f));
            assertThat(tensor.getFloat(0, 1, 0, 0)).isCloseTo(64.0f / 255.0f, within(1e-5f));
            assertThat(tensor.getFloat(0, 2, 0, 0)).isCloseTo(32.0f / 255.0f, within(1e-5f));
        }
    }

    @Nested
    @DisplayName("BGR 模式测试")
    class BgrModeTest {

        @Test
        @DisplayName("BGR 模式下通道交换正确：R↔B")
        void bgrSwapsChannels() {
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            // RGB = (200, 100, 50)
            image.setRGB(0, 0, new Color(200, 100, 50).getRGB());

            ImageToTensor.ConvertOptions options = ImageToTensor.ConvertOptions.builder()
                    .bgr(true)
                    .build();

            Tensor tensor = ImageToTensor.convert(image, options);

            assertThat(tensor.shape()).isEqualTo(Shape.of(1, 3, 1, 1));

            // Channel 0 = B, Channel 1 = G, Channel 2 = R
            assertThat(tensor.getFloat(0, 0, 0, 0)).isCloseTo(50.0f / 255.0f, within(1e-5f));
            assertThat(tensor.getFloat(0, 1, 0, 0)).isCloseTo(100.0f / 255.0f, within(1e-5f));
            assertThat(tensor.getFloat(0, 2, 0, 0)).isCloseTo(200.0f / 255.0f, within(1e-5f));
        }
    }

    @Nested
    @DisplayName("ImageNet 均值/标准差归一化测试")
    class NormalizationTest {

        @Test
        @DisplayName("ImageNet 标准归一化计算正确")
        void imagenetNormalization() {
            float[] mean = {0.485f, 0.456f, 0.406f};
            float[] std = {0.229f, 0.224f, 0.225f};

            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            // Pure white pixel: RGB = (255, 255, 255)
            image.setRGB(0, 0, Color.WHITE.getRGB());

            Tensor tensor = ImageToTensor.convert(image, mean, std);

            assertThat(tensor.shape()).isEqualTo(Shape.of(1, 3, 1, 1));

            // R: (1.0 - 0.485) / 0.229
            float expectedR = (1.0f - 0.485f) / 0.229f;
            // G: (1.0 - 0.456) / 0.224
            float expectedG = (1.0f - 0.456f) / 0.224f;
            // B: (1.0 - 0.406) / 0.225
            float expectedB = (1.0f - 0.406f) / 0.225f;

            assertThat(tensor.getFloat(0, 0, 0, 0)).isCloseTo(expectedR, within(1e-4f));
            assertThat(tensor.getFloat(0, 1, 0, 0)).isCloseTo(expectedG, within(1e-4f));
            assertThat(tensor.getFloat(0, 2, 0, 0)).isCloseTo(expectedB, within(1e-4f));
        }

        @Test
        @DisplayName("仅提供 mean 不提供 std 的归一化")
        void meanOnlyNormalization() {
            float[] mean = {0.5f, 0.5f, 0.5f};

            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, Color.WHITE.getRGB());

            Tensor tensor = ImageToTensor.convert(image, mean, null);

            // R: 1.0 - 0.5 = 0.5
            assertThat(tensor.getFloat(0, 0, 0, 0)).isCloseTo(0.5f, within(1e-5f));
            assertThat(tensor.getFloat(0, 1, 0, 0)).isCloseTo(0.5f, within(1e-5f));
            assertThat(tensor.getFloat(0, 2, 0, 0)).isCloseTo(0.5f, within(1e-5f));
        }
    }

    @Nested
    @DisplayName("缩放测试")
    class ResizeTest {

        @Test
        @DisplayName("缩放后输出张量形状正确")
        void resizeToTargetSize() {
            BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, 10, 10);
            g.dispose();

            ImageToTensor.ConvertOptions options = ImageToTensor.ConvertOptions.builder()
                    .targetWidth(4)
                    .targetHeight(4)
                    .build();

            Tensor tensor = ImageToTensor.convert(image, options);

            assertThat(tensor.shape()).isEqualTo(Shape.of(1, 3, 4, 4));
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionTest {

        @Test
        @DisplayName("null 图像抛出 TensorException")
        void nullImage() {
            assertThatThrownBy(() -> ImageToTensor.convert(null))
                    .isInstanceOf(TensorException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("null 选项抛出 TensorException")
        void nullOptions() {
            BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            assertThatThrownBy(() -> ImageToTensor.convert(image, (ImageToTensor.ConvertOptions) null))
                    .isInstanceOf(TensorException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("mean 数组长度不为3抛出 TensorException")
        void invalidMeanLength() {
            BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            assertThatThrownBy(() -> ImageToTensor.convert(image, new float[]{0.5f, 0.5f}, null))
                    .isInstanceOf(TensorException.class)
                    .hasMessageContaining("3");
        }

        @Test
        @DisplayName("std 数组长度不为3抛出 TensorException")
        void invalidStdLength() {
            BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            assertThatThrownBy(() -> ImageToTensor.convert(image, null, new float[]{0.5f}))
                    .isInstanceOf(TensorException.class)
                    .hasMessageContaining("3");
        }

        @Test
        @DisplayName("std 包含零值抛出 TensorException")
        void zeroStd() {
            BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            assertThatThrownBy(() -> ImageToTensor.convert(image, null, new float[]{1.0f, 0.0f, 1.0f}))
                    .isInstanceOf(TensorException.class)
                    .hasMessageContaining("zero");
        }
    }

    @Nested
    @DisplayName("ConvertOptions 构建器测试")
    class ConvertOptionsBuilderTest {

        @Test
        @DisplayName("默认选项值正确")
        void defaultOptions() {
            ImageToTensor.ConvertOptions options = ImageToTensor.ConvertOptions.defaults();

            assertThat(options.bgr()).isFalse();
            assertThat(options.mean()).isNull();
            assertThat(options.std()).isNull();
            assertThat(options.targetWidth()).isZero();
            assertThat(options.targetHeight()).isZero();
        }

        @Test
        @DisplayName("Builder 设置所有字段正确")
        void builderSetsAllFields() {
            float[] mean = {0.485f, 0.456f, 0.406f};
            float[] std = {0.229f, 0.224f, 0.225f};

            ImageToTensor.ConvertOptions options = ImageToTensor.ConvertOptions.builder()
                    .bgr(true)
                    .mean(mean)
                    .std(std)
                    .targetWidth(224)
                    .targetHeight(224)
                    .build();

            assertThat(options.bgr()).isTrue();
            assertThat(options.mean()).containsExactly(0.485f, 0.456f, 0.406f);
            assertThat(options.std()).containsExactly(0.229f, 0.224f, 0.225f);
            assertThat(options.targetWidth()).isEqualTo(224);
            assertThat(options.targetHeight()).isEqualTo(224);
        }

        @Test
        @DisplayName("Builder 防御性拷贝 mean/std 数组")
        void builderDefensiveCopy() {
            float[] mean = {0.5f, 0.5f, 0.5f};
            ImageToTensor.ConvertOptions options = ImageToTensor.ConvertOptions.builder()
                    .mean(mean)
                    .build();

            // Modify the original array — should not affect options
            mean[0] = 0.0f;
            assertThat(options.mean()[0]).isEqualTo(0.5f);
        }
    }
}
