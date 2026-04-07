package cloud.opencode.base.image.exif;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ExifTag 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ExifTag 枚举测试")
class ExifTagTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("枚举值数量为6")
        void testEnumCount() {
            assertThat(ExifTag.values()).hasSize(6);
        }

        @Test
        @DisplayName("包含ALL标签")
        void testContainsAll() {
            assertThat(ExifTag.values()).contains(ExifTag.ALL);
        }

        @Test
        @DisplayName("包含ORIENTATION标签")
        void testContainsOrientation() {
            assertThat(ExifTag.values()).contains(ExifTag.ORIENTATION);
        }

        @Test
        @DisplayName("包含GPS标签")
        void testContainsGps() {
            assertThat(ExifTag.values()).contains(ExifTag.GPS);
        }

        @Test
        @DisplayName("包含DATETIME标签")
        void testContainsDatetime() {
            assertThat(ExifTag.values()).contains(ExifTag.DATETIME);
        }

        @Test
        @DisplayName("包含CAMERA标签")
        void testContainsCamera() {
            assertThat(ExifTag.values()).contains(ExifTag.CAMERA);
        }

        @Test
        @DisplayName("包含SOFTWARE标签")
        void testContainsSoftware() {
            assertThat(ExifTag.values()).contains(ExifTag.SOFTWARE);
        }

        @Test
        @DisplayName("枚举值顺序正确")
        void testEnumOrder() {
            assertThat(ExifTag.values()).containsExactly(
                    ExifTag.ALL,
                    ExifTag.ORIENTATION,
                    ExifTag.GPS,
                    ExifTag.DATETIME,
                    ExifTag.CAMERA,
                    ExifTag.SOFTWARE
            );
        }
    }

    @Nested
    @DisplayName("valueOf测试")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf返回正确枚举值")
        void testValueOf() {
            assertThat(ExifTag.valueOf("ALL")).isEqualTo(ExifTag.ALL);
            assertThat(ExifTag.valueOf("ORIENTATION")).isEqualTo(ExifTag.ORIENTATION);
            assertThat(ExifTag.valueOf("GPS")).isEqualTo(ExifTag.GPS);
            assertThat(ExifTag.valueOf("DATETIME")).isEqualTo(ExifTag.DATETIME);
            assertThat(ExifTag.valueOf("CAMERA")).isEqualTo(ExifTag.CAMERA);
            assertThat(ExifTag.valueOf("SOFTWARE")).isEqualTo(ExifTag.SOFTWARE);
        }

        @Test
        @DisplayName("无效名称抛出IllegalArgumentException")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> ExifTag.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
