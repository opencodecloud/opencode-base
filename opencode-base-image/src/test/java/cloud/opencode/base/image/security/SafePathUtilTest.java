package cloud.opencode.base.image.security;

import cloud.opencode.base.image.ImageFormat;
import cloud.opencode.base.image.exception.ImageValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * SafePathUtil 工具类测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("SafePathUtil 工具类测试")
class SafePathUtilTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("validatePath方法测试")
    class ValidatePathTests {

        @Test
        @DisplayName("有效路径通过验证")
        void testValidPath() {
            Path path = tempDir.resolve("image.jpg");

            assertThatCode(() -> SafePathUtil.validatePath(path))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null路径抛出异常")
        void testNullPath() {
            assertThatThrownBy(() -> SafePathUtil.validatePath(null))
                .isInstanceOf(ImageValidationException.class);
        }

        @Test
        @DisplayName("包含..的文件名抛出异常")
        void testPathTraversal() {
            // 使用包含..的文件名而不是路径遍历
            Path path = tempDir.resolve("..evil.jpg");

            assertThatThrownBy(() -> SafePathUtil.validatePath(path))
                .isInstanceOf(ImageValidationException.class);
        }

        @Test
        @DisplayName("无效扩展名抛出异常")
        void testInvalidExtension() {
            Path path = tempDir.resolve("file.txt");

            assertThatThrownBy(() -> SafePathUtil.validatePath(path))
                .isInstanceOf(ImageValidationException.class);
        }

        @Test
        @DisplayName("有效图片扩展名")
        void testValidExtensions() {
            String[] validExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};

            for (String ext : validExtensions) {
                Path path = tempDir.resolve("image." + ext);
                assertThatCode(() -> SafePathUtil.validatePath(path))
                    .doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("validatePath带baseDir方法测试")
    class ValidatePathWithBaseDirTests {

        @Test
        @DisplayName("路径在基础目录内通过验证")
        void testPathInBaseDir() {
            Path path = tempDir.resolve("image.jpg");

            assertThatCode(() -> SafePathUtil.validatePath(path, tempDir))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("路径在基础目录外抛出异常")
        void testPathOutsideBaseDir() {
            Path path = tempDir.getParent().resolve("image.jpg");

            assertThatThrownBy(() -> SafePathUtil.validatePath(path, tempDir))
                .isInstanceOf(ImageValidationException.class);
        }
    }

    @Nested
    @DisplayName("isSafePath方法测试")
    class IsSafePathTests {

        @Test
        @DisplayName("安全路径返回true")
        void testSafePath() {
            Path path = tempDir.resolve("image.jpg");

            assertThat(SafePathUtil.isSafePath(path, tempDir)).isTrue();
        }

        @Test
        @DisplayName("不安全路径返回false")
        void testUnsafePath() {
            Path path = tempDir.getParent().resolve("image.jpg");

            assertThat(SafePathUtil.isSafePath(path, tempDir)).isFalse();
        }

        @Test
        @DisplayName("规范化路径后检查")
        void testNormalizedPath() {
            Path path = tempDir.resolve("subdir/../image.jpg");

            assertThat(SafePathUtil.isSafePath(path, tempDir)).isTrue();
        }
    }

    @Nested
    @DisplayName("sanitizeFilename方法测试")
    class SanitizeFilenameTests {

        @Test
        @DisplayName("正常文件名不变")
        void testNormalFilename() {
            String result = SafePathUtil.sanitizeFilename("image.jpg");

            assertThat(result).isEqualTo("image.jpg");
        }

        @Test
        @DisplayName("移除路径分隔符")
        void testRemovePathSeparators() {
            String result = SafePathUtil.sanitizeFilename("../image.jpg");

            assertThat(result).doesNotContain("/");
            assertThat(result).doesNotContain("..");
        }

        @Test
        @DisplayName("移除特殊字符")
        void testRemoveSpecialChars() {
            String result = SafePathUtil.sanitizeFilename("image<>:\"|?*.jpg");

            assertThat(result).doesNotContain("<");
            assertThat(result).doesNotContain(">");
        }

        @Test
        @DisplayName("null返回默认值")
        void testNullFilename() {
            String result = SafePathUtil.sanitizeFilename(null);

            assertThat(result).isEqualTo("image");
        }

        @Test
        @DisplayName("空字符串返回默认值")
        void testEmptyFilename() {
            String result = SafePathUtil.sanitizeFilename("");

            assertThat(result).isEqualTo("image");
        }

        @Test
        @DisplayName("超长文件名截断")
        void testLongFilename() {
            String longName = "a".repeat(300) + ".jpg";
            String result = SafePathUtil.sanitizeFilename(longName);

            assertThat(result.length()).isLessThanOrEqualTo(255);
        }
    }

    @Nested
    @DisplayName("getExtension方法测试")
    class GetExtensionTests {

        @Test
        @DisplayName("获取jpg扩展名")
        void testGetJpgExtension() {
            String result = SafePathUtil.getExtension("image.jpg");

            assertThat(result).isEqualTo("jpg");
        }

        @Test
        @DisplayName("获取大写扩展名转小写")
        void testGetUpperCaseExtension() {
            String result = SafePathUtil.getExtension("image.JPG");

            assertThat(result).isEqualTo("jpg");
        }

        @Test
        @DisplayName("无扩展名返回空字符串")
        void testNoExtension() {
            String result = SafePathUtil.getExtension("image");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null返回空字符串")
        void testNullFilename() {
            String result = SafePathUtil.getExtension(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("以点开头的文件")
        void testDotFile() {
            String result = SafePathUtil.getExtension(".gitignore");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("isAllowedExtension方法测试")
    class IsAllowedExtensionTests {

        @Test
        @DisplayName("jpg扩展名允许")
        void testJpgAllowed() {
            assertThat(SafePathUtil.isAllowedExtension("jpg")).isTrue();
        }

        @Test
        @DisplayName("png扩展名允许")
        void testPngAllowed() {
            assertThat(SafePathUtil.isAllowedExtension("png")).isTrue();
        }

        @Test
        @DisplayName("大写扩展名允许")
        void testUpperCaseAllowed() {
            assertThat(SafePathUtil.isAllowedExtension("JPG")).isTrue();
        }

        @Test
        @DisplayName("txt扩展名不允许")
        void testTxtNotAllowed() {
            assertThat(SafePathUtil.isAllowedExtension("txt")).isFalse();
        }

        @Test
        @DisplayName("null不允许")
        void testNullNotAllowed() {
            assertThat(SafePathUtil.isAllowedExtension(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getFormat方法测试")
    class GetFormatTests {

        @Test
        @DisplayName("获取jpg格式")
        void testGetJpgFormat() {
            Path path = tempDir.resolve("image.jpg");
            ImageFormat format = SafePathUtil.getFormat(path);

            assertThat(format).isEqualTo(ImageFormat.JPEG);
        }

        @Test
        @DisplayName("获取png格式")
        void testGetPngFormat() {
            Path path = tempDir.resolve("image.png");
            ImageFormat format = SafePathUtil.getFormat(path);

            assertThat(format).isEqualTo(ImageFormat.PNG);
        }

        @Test
        @DisplayName("null路径返回null")
        void testNullPath() {
            ImageFormat format = SafePathUtil.getFormat(null);

            assertThat(format).isNull();
        }

        @Test
        @DisplayName("无效扩展名返回null")
        void testInvalidExtension() {
            Path path = tempDir.resolve("file.txt");
            ImageFormat format = SafePathUtil.getFormat(path);

            assertThat(format).isNull();
        }
    }

    @Nested
    @DisplayName("generateOutputPath方法测试")
    class GenerateOutputPathTests {

        @Test
        @DisplayName("生成带后缀的输出路径")
        void testGenerateWithSuffix() {
            Path input = tempDir.resolve("image.jpg");
            Path output = SafePathUtil.generateOutputPath(input, "_thumb");

            assertThat(output.getFileName().toString()).contains("_thumb");
            assertThat(output.getFileName().toString()).endsWith(".jpg");
        }

        @Test
        @DisplayName("生成新格式的输出路径")
        void testGenerateWithFormat() {
            Path input = tempDir.resolve("image.jpg");
            Path output = SafePathUtil.generateOutputPath(input, ImageFormat.PNG);

            assertThat(output.getFileName().toString()).endsWith(".png");
        }
    }

    @Nested
    @DisplayName("ensureParentExists方法测试")
    class EnsureParentExistsTests {

        @Test
        @DisplayName("创建不存在的父目录")
        void testCreateParentDir() {
            Path path = tempDir.resolve("newdir/image.jpg");

            boolean result = SafePathUtil.ensureParentExists(path);

            assertThat(result).isTrue();
            assertThat(Files.exists(path.getParent())).isTrue();
        }

        @Test
        @DisplayName("已存在的父目录")
        void testExistingParentDir() {
            Path path = tempDir.resolve("image.jpg");

            boolean result = SafePathUtil.ensureParentExists(path);

            assertThat(result).isTrue();
        }
    }
}
