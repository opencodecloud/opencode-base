package cloud.opencode.base.geo.validation;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.exception.CoordinateOutOfRangeException;
import cloud.opencode.base.geo.exception.InvalidCoordinateException;
import cloud.opencode.base.geo.exception.InvalidGeoHashException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CoordinateValidator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("CoordinateValidator 测试")
class CoordinateValidatorTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("经纬度边界常量正确")
        void testBoundaryConstants() {
            assertThat(CoordinateValidator.MIN_LONGITUDE).isEqualTo(-180.0);
            assertThat(CoordinateValidator.MAX_LONGITUDE).isEqualTo(180.0);
            assertThat(CoordinateValidator.MIN_LATITUDE).isEqualTo(-90.0);
            assertThat(CoordinateValidator.MAX_LATITUDE).isEqualTo(90.0);
        }

        @Test
        @DisplayName("中国边界常量正确")
        void testChinaBoundaryConstants() {
            assertThat(CoordinateValidator.CHINA_MIN_LONGITUDE).isEqualTo(72.004);
            assertThat(CoordinateValidator.CHINA_MAX_LONGITUDE).isEqualTo(137.8347);
            assertThat(CoordinateValidator.CHINA_MIN_LATITUDE).isEqualTo(0.8293);
            assertThat(CoordinateValidator.CHINA_MAX_LATITUDE).isEqualTo(55.8271);
        }

        @Test
        @DisplayName("GeoHash最大长度常量正确")
        void testMaxGeoHashLength() {
            assertThat(CoordinateValidator.MAX_GEOHASH_LENGTH).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("validate(double, double)测试")
    class ValidateDoublesTests {

        @Test
        @DisplayName("有效坐标通过验证")
        void testValidCoordinates() {
            assertThatCode(() -> CoordinateValidator.validate(116.4074, 39.9042))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("边界值通过验证")
        void testBoundaryValues() {
            assertThatCode(() -> {
                CoordinateValidator.validate(-180.0, -90.0);
                CoordinateValidator.validate(180.0, 90.0);
                CoordinateValidator.validate(0, 0);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("NaN经度抛出异常")
        void testNaNLongitude() {
            assertThatThrownBy(() -> CoordinateValidator.validate(Double.NaN, 39.9042))
                .isInstanceOf(InvalidCoordinateException.class)
                .hasMessageContaining("NaN");
        }

        @Test
        @DisplayName("NaN纬度抛出异常")
        void testNaNLatitude() {
            assertThatThrownBy(() -> CoordinateValidator.validate(116.4074, Double.NaN))
                .isInstanceOf(InvalidCoordinateException.class)
                .hasMessageContaining("NaN");
        }

        @Test
        @DisplayName("无穷大经度抛出异常")
        void testInfiniteLongitude() {
            assertThatThrownBy(() -> CoordinateValidator.validate(Double.POSITIVE_INFINITY, 39.9042))
                .isInstanceOf(InvalidCoordinateException.class)
                .hasMessageContaining("Infinite");
        }

        @Test
        @DisplayName("无穷大纬度抛出异常")
        void testInfiniteLatitude() {
            assertThatThrownBy(() -> CoordinateValidator.validate(116.4074, Double.NEGATIVE_INFINITY))
                .isInstanceOf(InvalidCoordinateException.class)
                .hasMessageContaining("Infinite");
        }

        @Test
        @DisplayName("经度越界抛出异常")
        void testLongitudeOutOfRange() {
            assertThatThrownBy(() -> CoordinateValidator.validate(200, 39.9042))
                .isInstanceOf(InvalidCoordinateException.class)
                .hasMessageContaining("Longitude");

            assertThatThrownBy(() -> CoordinateValidator.validate(-200, 39.9042))
                .isInstanceOf(InvalidCoordinateException.class);
        }

        @Test
        @DisplayName("纬度越界抛出异常")
        void testLatitudeOutOfRange() {
            assertThatThrownBy(() -> CoordinateValidator.validate(116.4074, 100))
                .isInstanceOf(InvalidCoordinateException.class)
                .hasMessageContaining("Latitude");

            assertThatThrownBy(() -> CoordinateValidator.validate(116.4074, -100))
                .isInstanceOf(InvalidCoordinateException.class);
        }
    }

    @Nested
    @DisplayName("validate(Coordinate)测试")
    class ValidateCoordinateTests {

        @Test
        @DisplayName("有效坐标对象通过验证")
        void testValidCoordinate() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);
            assertThatCode(() -> CoordinateValidator.validate(coord))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testNullCoordinate() {
            assertThatThrownBy(() -> CoordinateValidator.validate((Coordinate) null))
                .isInstanceOf(InvalidCoordinateException.class)
                .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("isValid(double, double)测试")
    class IsValidDoublesTests {

        @Test
        @DisplayName("有效坐标返回true")
        void testValidReturnsTrue() {
            assertThat(CoordinateValidator.isValid(116.4074, 39.9042)).isTrue();
        }

        @Test
        @DisplayName("NaN返回false")
        void testNaNReturnsFalse() {
            assertThat(CoordinateValidator.isValid(Double.NaN, 39.9042)).isFalse();
            assertThat(CoordinateValidator.isValid(116.4074, Double.NaN)).isFalse();
        }

        @Test
        @DisplayName("无穷大返回false")
        void testInfiniteReturnsFalse() {
            assertThat(CoordinateValidator.isValid(Double.POSITIVE_INFINITY, 39.9042)).isFalse();
            assertThat(CoordinateValidator.isValid(116.4074, Double.NEGATIVE_INFINITY)).isFalse();
        }

        @Test
        @DisplayName("越界返回false")
        void testOutOfRangeReturnsFalse() {
            assertThat(CoordinateValidator.isValid(200, 39.9042)).isFalse();
            assertThat(CoordinateValidator.isValid(116.4074, 100)).isFalse();
        }
    }

    @Nested
    @DisplayName("isValid(Coordinate)测试")
    class IsValidCoordinateTests {

        @Test
        @DisplayName("有效坐标对象返回true")
        void testValidCoordinateReturnsTrue() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);
            assertThat(CoordinateValidator.isValid(coord)).isTrue();
        }

        @Test
        @DisplayName("null坐标返回false")
        void testNullCoordinateReturnsFalse() {
            assertThat(CoordinateValidator.isValid((Coordinate) null)).isFalse();
        }
    }

    @Nested
    @DisplayName("validateInChina(double, double)测试")
    class ValidateInChinaDoublesTests {

        @Test
        @DisplayName("北京坐标通过验证")
        void testBeijingValid() {
            assertThatCode(() -> CoordinateValidator.validateInChina(116.4074, 39.9042))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("东京坐标抛出异常")
        void testTokyoInvalid() {
            assertThatThrownBy(() -> CoordinateValidator.validateInChina(139.6917, 35.6895))
                .isInstanceOf(CoordinateOutOfRangeException.class)
                .hasMessageContaining("not in China");
        }

        @Test
        @DisplayName("无效坐标先抛出InvalidCoordinateException")
        void testInvalidCoordinateFirst() {
            assertThatThrownBy(() -> CoordinateValidator.validateInChina(200, 39.9042))
                .isInstanceOf(InvalidCoordinateException.class);
        }
    }

    @Nested
    @DisplayName("validateInChina(Coordinate)测试")
    class ValidateInChinaCoordinateTests {

        @Test
        @DisplayName("北京坐标对象通过验证")
        void testBeijingCoordinateValid() {
            Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
            assertThatCode(() -> CoordinateValidator.validateInChina(beijing))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testNullCoordinateInvalid() {
            assertThatThrownBy(() -> CoordinateValidator.validateInChina((Coordinate) null))
                .isInstanceOf(InvalidCoordinateException.class);
        }
    }

    @Nested
    @DisplayName("isInChina(double, double)测试")
    class IsInChinaDoublesTests {

        @Test
        @DisplayName("北京返回true")
        void testBeijingReturnsTrue() {
            assertThat(CoordinateValidator.isInChina(116.4074, 39.9042)).isTrue();
        }

        @Test
        @DisplayName("东京返回false")
        void testTokyoReturnsFalse() {
            assertThat(CoordinateValidator.isInChina(139.6917, 35.6895)).isFalse();
        }

        @Test
        @DisplayName("边界值测试")
        void testBoundaryValues() {
            // 中国西边界
            assertThat(CoordinateValidator.isInChina(72.004, 30.0)).isTrue();
            assertThat(CoordinateValidator.isInChina(72.003, 30.0)).isFalse();

            // 中国东边界
            assertThat(CoordinateValidator.isInChina(137.8347, 30.0)).isTrue();
            assertThat(CoordinateValidator.isInChina(137.8348, 30.0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isInChina(Coordinate)测试")
    class IsInChinaCoordinateTests {

        @Test
        @DisplayName("北京坐标对象返回true")
        void testBeijingCoordinateReturnsTrue() {
            Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
            assertThat(CoordinateValidator.isInChina(beijing)).isTrue();
        }

        @Test
        @DisplayName("null坐标返回false")
        void testNullCoordinateReturnsFalse() {
            assertThat(CoordinateValidator.isInChina((Coordinate) null)).isFalse();
        }
    }

    @Nested
    @DisplayName("validateGeoHash()测试")
    class ValidateGeoHashTests {

        @Test
        @DisplayName("有效GeoHash通过验证")
        void testValidGeoHash() {
            assertThatCode(() -> CoordinateValidator.validateGeoHash("wx4g0bce"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null GeoHash抛出异常")
        void testNullGeoHash() {
            assertThatThrownBy(() -> CoordinateValidator.validateGeoHash(null))
                .isInstanceOf(InvalidGeoHashException.class)
                .hasMessageContaining("null or empty");
        }

        @Test
        @DisplayName("空GeoHash抛出异常")
        void testEmptyGeoHash() {
            assertThatThrownBy(() -> CoordinateValidator.validateGeoHash(""))
                .isInstanceOf(InvalidGeoHashException.class)
                .hasMessageContaining("null or empty");
        }

        @Test
        @DisplayName("过长GeoHash抛出异常")
        void testTooLongGeoHash() {
            assertThatThrownBy(() -> CoordinateValidator.validateGeoHash("wx4g0bce12345"))
                .isInstanceOf(InvalidGeoHashException.class)
                .hasMessageContaining("cannot exceed");
        }

        @Test
        @DisplayName("包含无效字符的GeoHash抛出异常")
        void testInvalidCharacters() {
            assertThatThrownBy(() -> CoordinateValidator.validateGeoHash("wx4a0bce"))
                .isInstanceOf(InvalidGeoHashException.class)
                .hasMessageContaining("Invalid GeoHash character");
        }

        @Test
        @DisplayName("大写GeoHash通过验证")
        void testUppercaseGeoHash() {
            assertThatCode(() -> CoordinateValidator.validateGeoHash("WX4G0BCE"))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("isValidGeoHash()测试")
    class IsValidGeoHashTests {

        @Test
        @DisplayName("有效GeoHash返回true")
        void testValidGeoHashReturnsTrue() {
            assertThat(CoordinateValidator.isValidGeoHash("wx4g0bce")).isTrue();
        }

        @Test
        @DisplayName("null返回false")
        void testNullReturnsFalse() {
            assertThat(CoordinateValidator.isValidGeoHash(null)).isFalse();
        }

        @Test
        @DisplayName("空字符串返回false")
        void testEmptyReturnsFalse() {
            assertThat(CoordinateValidator.isValidGeoHash("")).isFalse();
        }

        @Test
        @DisplayName("过长返回false")
        void testTooLongReturnsFalse() {
            assertThat(CoordinateValidator.isValidGeoHash("wx4g0bce12345")).isFalse();
        }

        @Test
        @DisplayName("无效字符返回false")
        void testInvalidCharactersReturnsFalse() {
            assertThat(CoordinateValidator.isValidGeoHash("wx4a0bce")).isFalse();
        }
    }

    @Nested
    @DisplayName("clampLongitude()测试")
    class ClampLongitudeTests {

        @Test
        @DisplayName("正常值不变")
        void testNormalValue() {
            assertThat(CoordinateValidator.clampLongitude(116.4074)).isEqualTo(116.4074);
        }

        @Test
        @DisplayName("超出上限限制到180")
        void testClampToMax() {
            assertThat(CoordinateValidator.clampLongitude(200)).isEqualTo(180.0);
        }

        @Test
        @DisplayName("超出下限限制到-180")
        void testClampToMin() {
            assertThat(CoordinateValidator.clampLongitude(-200)).isEqualTo(-180.0);
        }
    }

    @Nested
    @DisplayName("clampLatitude()测试")
    class ClampLatitudeTests {

        @Test
        @DisplayName("正常值不变")
        void testNormalValue() {
            assertThat(CoordinateValidator.clampLatitude(39.9042)).isEqualTo(39.9042);
        }

        @Test
        @DisplayName("超出上限限制到90")
        void testClampToMax() {
            assertThat(CoordinateValidator.clampLatitude(100)).isEqualTo(90.0);
        }

        @Test
        @DisplayName("超出下限限制到-90")
        void testClampToMin() {
            assertThat(CoordinateValidator.clampLatitude(-100)).isEqualTo(-90.0);
        }
    }

    @Nested
    @DisplayName("normalizeLongitude()测试")
    class NormalizeLongitudeTests {

        @Test
        @DisplayName("正常值不变")
        void testNormalValue() {
            assertThat(CoordinateValidator.normalizeLongitude(116.4074)).isEqualTo(116.4074);
        }

        @Test
        @DisplayName("超过180标准化")
        void testNormalizeOver180() {
            assertThat(CoordinateValidator.normalizeLongitude(200)).isEqualTo(-160.0);
            assertThat(CoordinateValidator.normalizeLongitude(540)).isEqualTo(180.0);
        }

        @Test
        @DisplayName("低于-180标准化")
        void testNormalizeUnderMinus180() {
            assertThat(CoordinateValidator.normalizeLongitude(-200)).isEqualTo(160.0);
            assertThat(CoordinateValidator.normalizeLongitude(-540)).isEqualTo(-180.0);
        }

        @Test
        @DisplayName("NaN返回0")
        void testNaNReturnsZero() {
            assertThat(CoordinateValidator.normalizeLongitude(Double.NaN)).isEqualTo(0);
        }

        @Test
        @DisplayName("无穷大返回0")
        void testInfiniteReturnsZero() {
            assertThat(CoordinateValidator.normalizeLongitude(Double.POSITIVE_INFINITY)).isEqualTo(0);
            assertThat(CoordinateValidator.normalizeLongitude(Double.NEGATIVE_INFINITY)).isEqualTo(0);
        }
    }
}
