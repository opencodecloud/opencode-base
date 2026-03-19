package cloud.opencode.base.image.security;

import cloud.opencode.base.image.Image;
import cloud.opencode.base.image.exception.ImageResourceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * SafeImageService 服务测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("SafeImageService 服务测试")
class SafeImageServiceTest {

    private SafeImageService service;

    @BeforeEach
    void setUp() {
        service = SafeImageService.createDefault();
    }

    @AfterEach
    void tearDown() {
        if (service != null) {
            service.close();
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建默认服务")
        void testCreateDefault() {
            SafeImageService svc = SafeImageService.createDefault();

            assertThat(svc).isNotNull();
            svc.close();
        }

        @Test
        @DisplayName("使用构建器创建")
        void testBuilderCreate() {
            SafeImageService svc = SafeImageService.builder()
                .maxFileSize(5_000_000)
                .maxWidth(4000)
                .maxHeight(4000)
                .timeout(Duration.ofSeconds(10))
                .maxConcurrent(5)
                .build();

            assertThat(svc).isNotNull();
            svc.close();
        }
    }

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("设置最大文件大小")
        void testMaxFileSize() {
            SafeImageService svc = SafeImageService.builder()
                .maxFileSize(1_000_000)
                .build();

            assertThat(svc).isNotNull();
            svc.close();
        }

        @Test
        @DisplayName("设置最大宽度")
        void testMaxWidth() {
            SafeImageService svc = SafeImageService.builder()
                .maxWidth(2000)
                .build();

            assertThat(svc).isNotNull();
            svc.close();
        }

        @Test
        @DisplayName("设置最大高度")
        void testMaxHeight() {
            SafeImageService svc = SafeImageService.builder()
                .maxHeight(2000)
                .build();

            assertThat(svc).isNotNull();
            svc.close();
        }

        @Test
        @DisplayName("设置最大尺寸")
        void testMaxDimensions() {
            SafeImageService svc = SafeImageService.builder()
                .maxDimensions(2000, 1500)
                .build();

            assertThat(svc).isNotNull();
            svc.close();
        }

        @Test
        @DisplayName("设置超时时间")
        void testTimeout() {
            SafeImageService svc = SafeImageService.builder()
                .timeout(Duration.ofSeconds(60))
                .build();

            assertThat(svc).isNotNull();
            svc.close();
        }

        @Test
        @DisplayName("设置最大并发数")
        void testMaxConcurrent() {
            SafeImageService svc = SafeImageService.builder()
                .maxConcurrent(10)
                .build();

            assertThat(svc).isNotNull();
            svc.close();
        }
    }

    @Nested
    @DisplayName("process方法测试")
    class ProcessTests {

        @Test
        @DisplayName("处理图片操作")
        void testProcessOperation() {
            BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Image image = new Image(bufferedImage, null);

            Image result = service.process(image, img -> img);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("处理调整大小操作")
        void testProcessResize() {
            BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Image image = new Image(bufferedImage, null);

            Image result = service.process(image, img -> {
                return new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            });

            assertThat(result.getWidth()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("getActiveCount方法测试")
    class GetActiveCountTests {

        @Test
        @DisplayName("初始活动计数为0")
        void testInitialActiveCount() {
            assertThat(service.getActiveCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getAvailablePermits方法测试")
    class GetAvailablePermitsTests {

        @Test
        @DisplayName("初始可用许可数")
        void testInitialAvailablePermits() {
            assertThat(service.getAvailablePermits()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("正常关闭服务")
        void testClose() {
            SafeImageService svc = SafeImageService.createDefault();

            assertThatCode(svc::close).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("多次关闭不抛异常")
        void testMultipleClose() {
            SafeImageService svc = SafeImageService.createDefault();

            svc.close();
            assertThatCode(svc::close).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("AutoCloseable测试")
    class AutoCloseableTests {

        @Test
        @DisplayName("try-with-resources使用")
        void testTryWithResources() {
            assertThatCode(() -> {
                try (SafeImageService svc = SafeImageService.createDefault()) {
                    assertThat(svc).isNotNull();
                }
            }).doesNotThrowAnyException();
        }
    }
}
