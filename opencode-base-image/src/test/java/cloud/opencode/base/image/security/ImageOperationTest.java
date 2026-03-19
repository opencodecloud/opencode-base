package cloud.opencode.base.image.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageOperation 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageOperation 接口测试")
class ImageOperationTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    }

    @Nested
    @DisplayName("apply方法测试")
    class ApplyTests {

        @Test
        @DisplayName("Lambda实现")
        void testLambdaImplementation() throws Exception {
            ImageOperation op = image -> image;
            BufferedImage result = op.apply(testImage);

            assertThat(result).isEqualTo(testImage);
        }

        @Test
        @DisplayName("处理图片操作")
        void testProcessImage() throws Exception {
            ImageOperation op = image -> {
                BufferedImage result = new BufferedImage(
                    image.getWidth() / 2,
                    image.getHeight() / 2,
                    image.getType()
                );
                return result;
            };

            BufferedImage result = op.apply(testImage);

            assertThat(result.getWidth()).isEqualTo(50);
            assertThat(result.getHeight()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("andThen方法测试")
    class AndThenTests {

        @Test
        @DisplayName("链接两个操作")
        void testAndThen() throws Exception {
            ImageOperation op1 = image -> new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            ImageOperation op2 = image -> new BufferedImage(25, 25, BufferedImage.TYPE_INT_RGB);

            ImageOperation combined = op1.andThen(op2);
            BufferedImage result = combined.apply(testImage);

            assertThat(result.getWidth()).isEqualTo(25);
            assertThat(result.getHeight()).isEqualTo(25);
        }

        @Test
        @DisplayName("链接多个操作")
        void testMultipleAndThen() throws Exception {
            ImageOperation op1 = image -> new BufferedImage(80, 80, image.getType());
            ImageOperation op2 = image -> new BufferedImage(60, 60, image.getType());
            ImageOperation op3 = image -> new BufferedImage(40, 40, image.getType());

            ImageOperation combined = op1.andThen(op2).andThen(op3);
            BufferedImage result = combined.apply(testImage);

            assertThat(result.getWidth()).isEqualTo(40);
        }
    }

    @Nested
    @DisplayName("compose方法测试")
    class ComposeTests {

        @Test
        @DisplayName("组合两个操作")
        void testCompose() throws Exception {
            ImageOperation op1 = image -> new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            ImageOperation op2 = image -> new BufferedImage(25, 25, BufferedImage.TYPE_INT_RGB);

            // compose: op2先执行，然后op1
            ImageOperation combined = op1.compose(op2);
            BufferedImage result = combined.apply(testImage);

            assertThat(result.getWidth()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("identity方法测试")
    class IdentityTests {

        @Test
        @DisplayName("返回原图")
        void testIdentity() throws Exception {
            ImageOperation op = ImageOperation.identity();
            BufferedImage result = op.apply(testImage);

            assertThat(result).isEqualTo(testImage);
        }

        @Test
        @DisplayName("identity与其他操作组合")
        void testIdentityWithOthers() throws Exception {
            ImageOperation resize = image -> new BufferedImage(50, 50, image.getType());

            ImageOperation combined = ImageOperation.identity().andThen(resize);
            BufferedImage result = combined.apply(testImage);

            assertThat(result.getWidth()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("withTimeout方法测试")
    class WithTimeoutTests {

        @Test
        @DisplayName("快速操作不超时")
        void testFastOperationNoTimeout() throws Exception {
            ImageOperation op = image -> image;
            ImageOperation wrapped = ImageOperation.withTimeout(op, 1000);

            BufferedImage result = wrapped.apply(testImage);

            assertThat(result).isEqualTo(testImage);
        }

        @Test
        @DisplayName("超时操作抛出异常")
        void testSlowOperationTimeout() {
            ImageOperation slowOp = image -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return image;
            };

            ImageOperation wrapped = ImageOperation.withTimeout(slowOp, 10);

            // 操作会完成但会报告超时
            assertThatThrownBy(() -> wrapped.apply(testImage))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("timed out");
        }
    }
}
