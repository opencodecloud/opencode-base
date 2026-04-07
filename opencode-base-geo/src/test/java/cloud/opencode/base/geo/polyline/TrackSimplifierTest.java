package cloud.opencode.base.geo.polyline;

import cloud.opencode.base.geo.Coordinate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TrackSimplifier 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.3
 */
@DisplayName("TrackSimplifier 测试")
class TrackSimplifierTest {

    @Nested
    @DisplayName("simplify()基本测试")
    class BasicTests {

        @Test
        @DisplayName("null输入返回空列表")
        void testNullInput() {
            assertThat(TrackSimplifier.simplify(null, 100)).isEmpty();
        }

        @Test
        @DisplayName("空列表返回空列表")
        void testEmptyInput() {
            assertThat(TrackSimplifier.simplify(Collections.emptyList(), 100)).isEmpty();
        }

        @Test
        @DisplayName("单点返回单点")
        void testSinglePoint() {
            List<Coordinate> track = List.of(Coordinate.wgs84(116.4, 39.9));
            List<Coordinate> result = TrackSimplifier.simplify(track, 100);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("两点返回两点")
        void testTwoPoints() {
            List<Coordinate> track = List.of(
                    Coordinate.wgs84(116.4, 39.9),
                    Coordinate.wgs84(116.5, 39.95)
            );
            List<Coordinate> result = TrackSimplifier.simplify(track, 100);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("始终保留首尾点")
        void testKeepFirstAndLast() {
            List<Coordinate> track = List.of(
                    Coordinate.wgs84(116.40, 39.90),
                    Coordinate.wgs84(116.41, 39.91),
                    Coordinate.wgs84(116.42, 39.90),
                    Coordinate.wgs84(116.50, 39.95)
            );
            List<Coordinate> result = TrackSimplifier.simplify(track, 100000);
            assertThat(result.getFirst()).isEqualTo(track.getFirst());
            assertThat(result.getLast()).isEqualTo(track.getLast());
        }
    }

    @Nested
    @DisplayName("直线轨迹测试")
    class StraightLineTests {

        @Test
        @DisplayName("直线上的点被简化")
        void testStraightLineSimplified() {
            // Points approximately on a straight line (north-south along 116.4 longitude)
            List<Coordinate> track = new ArrayList<>();
            for (int i = 0; i <= 10; i++) {
                track.add(Coordinate.wgs84(116.4, 39.0 + i * 0.1));
            }

            // With a large tolerance, should reduce to just first and last
            List<Coordinate> result = TrackSimplifier.simplify(track, 100);
            assertThat(result.size()).isLessThanOrEqualTo(3);
            assertThat(result.getFirst()).isEqualTo(track.getFirst());
            assertThat(result.getLast()).isEqualTo(track.getLast());
        }
    }

    @Nested
    @DisplayName("曲线轨迹测试")
    class CurvedTrackTests {

        @Test
        @DisplayName("Z字形轨迹保留拐点")
        void testZigZagKeepsTurns() {
            List<Coordinate> track = List.of(
                    Coordinate.wgs84(116.40, 39.90),   // start
                    Coordinate.wgs84(116.41, 39.95),   // up-right
                    Coordinate.wgs84(116.42, 39.90),   // down-right (turn point)
                    Coordinate.wgs84(116.43, 39.95),   // up-right
                    Coordinate.wgs84(116.44, 39.90)    // end
            );

            // Small tolerance should keep most points
            List<Coordinate> result = TrackSimplifier.simplify(track, 10);
            assertThat(result.size()).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("零容差保留所有点")
        void testZeroToleranceKeepsAll() {
            List<Coordinate> track = List.of(
                    Coordinate.wgs84(116.40, 39.90),
                    Coordinate.wgs84(116.41, 39.91),
                    Coordinate.wgs84(116.42, 39.90),
                    Coordinate.wgs84(116.43, 39.93)
            );

            List<Coordinate> result = TrackSimplifier.simplify(track, 0);
            assertThat(result).hasSize(track.size());
        }
    }

    @Nested
    @DisplayName("容差测试")
    class ToleranceTests {

        @Test
        @DisplayName("负容差抛出异常")
        void testNegativeTolerance() {
            List<Coordinate> track = List.of(
                    Coordinate.wgs84(0, 0), Coordinate.wgs84(0.5, 0.5),
                    Coordinate.wgs84(1, 1));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TrackSimplifier.simplify(track, -1.0));
        }

        @Test
        @DisplayName("NaN容差抛出异常")
        void testNaNTolerance() {
            List<Coordinate> track = List.of(
                    Coordinate.wgs84(0, 0), Coordinate.wgs84(0.5, 0.5),
                    Coordinate.wgs84(1, 1));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TrackSimplifier.simplify(track, Double.NaN));
        }

        @Test
        @DisplayName("无穷大容差仅保留首尾")
        void testInfiniteTolerance() {
            List<Coordinate> track = List.of(
                    Coordinate.wgs84(116.40, 39.90),
                    Coordinate.wgs84(116.41, 39.91),
                    Coordinate.wgs84(116.42, 39.90),
                    Coordinate.wgs84(116.50, 39.95)
            );
            List<Coordinate> result = TrackSimplifier.simplify(track, Double.POSITIVE_INFINITY);
            assertThat(result).hasSize(2);
            assertThat(result.getFirst()).isEqualTo(track.getFirst());
            assertThat(result.getLast()).isEqualTo(track.getLast());
        }

        @Test
        @DisplayName("更大容差产生更少点")
        void testLargerToleranceLessPoints() {
            List<Coordinate> track = List.of(
                    Coordinate.wgs84(116.40, 39.90),
                    Coordinate.wgs84(116.41, 39.95),
                    Coordinate.wgs84(116.42, 39.90),
                    Coordinate.wgs84(116.43, 39.95),
                    Coordinate.wgs84(116.44, 39.90),
                    Coordinate.wgs84(116.50, 39.95)
            );

            List<Coordinate> tight = TrackSimplifier.simplify(track, 10);
            List<Coordinate> loose = TrackSimplifier.simplify(track, 100000);

            assertThat(loose.size()).isLessThanOrEqualTo(tight.size());
        }
    }

    @Nested
    @DisplayName("大轨迹测试")
    class LargeTrackTests {

        @Test
        @DisplayName("大轨迹不导致栈溢出")
        void testLargeTrackNoStackOverflow() {
            // Create a track with 10000 points in a zigzag pattern
            List<Coordinate> track = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                double lng = 116.0 + (i * 0.001);
                double lat = 39.0 + (i % 2 == 0 ? 0.01 : -0.01);
                track.add(Coordinate.wgs84(lng, lat));
            }

            // Should not throw StackOverflowError
            List<Coordinate> result = TrackSimplifier.simplify(track, 100);
            assertThat(result).isNotEmpty();
            assertThat(result.size()).isLessThanOrEqualTo(track.size());
        }

        @Test
        @DisplayName("大量共线点被简化")
        void testManyCollinearPoints() {
            List<Coordinate> track = new ArrayList<>();
            for (int i = 0; i <= 5000; i++) {
                track.add(Coordinate.wgs84(116.0 + i * 0.001, 39.0));
            }

            List<Coordinate> result = TrackSimplifier.simplify(track, 10);
            // Should reduce significantly since they're approximately collinear
            assertThat(result.size()).isLessThan(track.size());
        }
    }

    @Nested
    @DisplayName("distanceToSegment()测试")
    class DistanceToSegmentTests {

        @Test
        @DisplayName("点在线段上距离为零")
        void testPointOnSegment() {
            Coordinate a = Coordinate.wgs84(0.0, 0.0);
            Coordinate b = Coordinate.wgs84(0.0, 1.0);
            Coordinate p = Coordinate.wgs84(0.0, 0.5);

            double dist = TrackSimplifier.distanceToSegment(p, a, b);
            assertThat(dist).isCloseTo(0.0, within(100.0)); // within 100m tolerance for spherical math
        }

        @Test
        @DisplayName("退化线段（同一点）")
        void testDegenerateSegment() {
            Coordinate a = Coordinate.wgs84(116.4, 39.9);
            Coordinate p = Coordinate.wgs84(116.5, 39.9);

            double dist = TrackSimplifier.distanceToSegment(p, a, a);
            assertThat(dist).isPositive();
        }

        @Test
        @DisplayName("远离线段的点距离为正")
        void testFarPoint() {
            Coordinate a = Coordinate.wgs84(0.0, 0.0);
            Coordinate b = Coordinate.wgs84(0.0, 1.0);
            Coordinate p = Coordinate.wgs84(1.0, 0.5);

            double dist = TrackSimplifier.distanceToSegment(p, a, b);
            assertThat(dist).isGreaterThan(100_000); // > 100km
        }
    }

    @Nested
    @DisplayName("与PolylineCodec集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("简化后编码解码一致")
        void testSimplifyThenEncodeDecode() {
            List<Coordinate> track = List.of(
                    Coordinate.wgs84(116.40, 39.90),
                    Coordinate.wgs84(116.405, 39.905),
                    Coordinate.wgs84(116.41, 39.91),
                    Coordinate.wgs84(116.42, 39.90),
                    Coordinate.wgs84(116.50, 39.95)
            );

            List<Coordinate> simplified = TrackSimplifier.simplify(track, 500);
            String encoded = PolylineCodec.encode(simplified);
            List<Coordinate> decoded = PolylineCodec.decode(encoded);

            assertThat(decoded).hasSize(simplified.size());
            for (int i = 0; i < simplified.size(); i++) {
                assertThat(decoded.get(i).latitude())
                        .isCloseTo(simplified.get(i).latitude(), within(0.00001));
                assertThat(decoded.get(i).longitude())
                        .isCloseTo(simplified.get(i).longitude(), within(0.00001));
            }
        }
    }
}
