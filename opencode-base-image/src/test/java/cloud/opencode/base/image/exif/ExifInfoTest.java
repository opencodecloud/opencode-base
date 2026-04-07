package cloud.opencode.base.image.exif;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * ExifInfo 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ExifInfo 记录测试")
class ExifInfoTest {

    @Nested
    @DisplayName("empty()工厂方法测试")
    class EmptyTests {

        @Test
        @DisplayName("empty返回orientation为0")
        void testEmptyOrientation() {
            ExifInfo empty = ExifInfo.empty();
            assertThat(empty.orientation()).isEqualTo(0);
        }

        @Test
        @DisplayName("empty返回所有nullable字段为null")
        void testEmptyNullableFields() {
            ExifInfo empty = ExifInfo.empty();
            assertThat(empty.cameraMake()).isNull();
            assertThat(empty.cameraModel()).isNull();
            assertThat(empty.dateTime()).isNull();
            assertThat(empty.latitude()).isNull();
            assertThat(empty.longitude()).isNull();
            assertThat(empty.imageWidth()).isNull();
            assertThat(empty.imageHeight()).isNull();
            assertThat(empty.software()).isNull();
        }
    }

    @Nested
    @DisplayName("hasGps()测试")
    class HasGpsTests {

        @Test
        @DisplayName("有纬度和经度时返回true")
        void testHasGpsTrue() {
            ExifInfo info = new ExifInfo(1, null, null, null, 39.9042, 116.4074, null, null, null);
            assertThat(info.hasGps()).isTrue();
        }

        @Test
        @DisplayName("只有纬度时返回false")
        void testHasGpsLatOnly() {
            ExifInfo info = new ExifInfo(1, null, null, null, 39.9042, null, null, null, null);
            assertThat(info.hasGps()).isFalse();
        }

        @Test
        @DisplayName("只有经度时返回false")
        void testHasGpsLonOnly() {
            ExifInfo info = new ExifInfo(1, null, null, null, null, 116.4074, null, null, null);
            assertThat(info.hasGps()).isFalse();
        }

        @Test
        @DisplayName("纬度和经度都为null时返回false")
        void testHasGpsBothNull() {
            ExifInfo info = ExifInfo.empty();
            assertThat(info.hasGps()).isFalse();
        }
    }

    @Nested
    @DisplayName("needsRotation()测试")
    class NeedsRotationTests {

        @Test
        @DisplayName("orientation为1时不需要旋转")
        void testNoRotationForOrientation1() {
            ExifInfo info = new ExifInfo(1, null, null, null, null, null, null, null, null);
            assertThat(info.needsRotation()).isFalse();
        }

        @Test
        @DisplayName("orientation为0时不需要旋转")
        void testNoRotationForOrientation0() {
            ExifInfo info = new ExifInfo(0, null, null, null, null, null, null, null, null);
            assertThat(info.needsRotation()).isFalse();
        }

        @Test
        @DisplayName("orientation为6时需要旋转")
        void testNeedsRotationForOrientation6() {
            ExifInfo info = new ExifInfo(6, null, null, null, null, null, null, null, null);
            assertThat(info.needsRotation()).isTrue();
        }

        @Test
        @DisplayName("orientation为2时需要旋转")
        void testNeedsRotationForOrientation2() {
            ExifInfo info = new ExifInfo(2, null, null, null, null, null, null, null, null);
            assertThat(info.needsRotation()).isTrue();
        }

        @Test
        @DisplayName("orientation为8时需要旋转")
        void testNeedsRotationForOrientation8() {
            ExifInfo info = new ExifInfo(8, null, null, null, null, null, null, null, null);
            assertThat(info.needsRotation()).isTrue();
        }
    }

    @Nested
    @DisplayName("record equals测试")
    class EqualsTests {

        @Test
        @DisplayName("相同值的ExifInfo相等")
        void testEquality() {
            Instant now = Instant.now();
            ExifInfo a = new ExifInfo(6, "Canon", "EOS R5", now, 39.9, 116.4, 4000, 3000, "Lightroom");
            ExifInfo b = new ExifInfo(6, "Canon", "EOS R5", now, 39.9, 116.4, 4000, 3000, "Lightroom");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不同值的ExifInfo不相等")
        void testInequality() {
            ExifInfo a = new ExifInfo(1, "Canon", null, null, null, null, null, null, null);
            ExifInfo b = new ExifInfo(6, "Nikon", null, null, null, null, null, null, null);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("两个empty相等")
        void testEmptyEquality() {
            assertThat(ExifInfo.empty()).isEqualTo(ExifInfo.empty());
        }
    }
}
