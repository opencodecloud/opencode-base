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

package cloud.opencode.base.captcha.security;

import cloud.opencode.base.captcha.security.TrajectoryData.Point;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for TrajectoryData record
 * TrajectoryData 记录的全面测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
@DisplayName("TrajectoryData Tests")
class TrajectoryDataTest {

    @Nested
    @DisplayName("Compact Constructor Validation Tests")
    class CompactConstructorValidationTests {

        @Test
        @DisplayName("should throw NullPointerException when points is null")
        void should_throwNPE_when_pointsNull() {
            assertThatThrownBy(() -> new TrajectoryData(null, List.of(0L, 100L), 100L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("points");
        }

        @Test
        @DisplayName("should throw NullPointerException when timestamps is null")
        void should_throwNPE_when_timestampsNull() {
            List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
            assertThatThrownBy(() -> new TrajectoryData(points, null, 100L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("timestamps");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when sizes mismatch")
        void should_throwIAE_when_sizesMismatch() {
            List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
            List<Long> timestamps = List.of(0L, 50L, 100L);

            assertThatThrownBy(() -> new TrajectoryData(points, timestamps, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when fewer than 2 points")
        void should_throwIAE_when_fewerThan2Points() {
            List<Point> points = List.of(new Point(0, 0));
            List<Long> timestamps = List.of(0L);

            assertThatThrownBy(() -> new TrajectoryData(points, timestamps, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when empty points")
        void should_throwIAE_when_emptyPoints() {
            assertThatThrownBy(() -> new TrajectoryData(List.of(), List.of(), 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when totalDurationMs is negative")
        void should_throwIAE_when_negativeDuration() {
            List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
            List<Long> timestamps = List.of(0L, 100L);

            assertThatThrownBy(() -> new TrajectoryData(points, timestamps, -1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalDurationMs");
        }

        @Test
        @DisplayName("should accept valid input with exactly 2 points")
        void should_constructSuccessfully_when_exactly2Points() {
            List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
            List<Long> timestamps = List.of(0L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 100L);

            assertThat(data.points()).hasSize(2);
            assertThat(data.timestamps()).hasSize(2);
            assertThat(data.totalDurationMs()).isEqualTo(100L);
        }

        @Test
        @DisplayName("should accept zero duration")
        void should_constructSuccessfully_when_zeroDuration() {
            List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
            List<Long> timestamps = List.of(0L, 0L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 0L);

            assertThat(data.totalDurationMs()).isZero();
        }
    }

    @Nested
    @DisplayName("Speeds Tests")
    class SpeedsTests {

        @Test
        @DisplayName("should calculate correct speeds for normal trajectory")
        void should_returnCorrectSpeeds_when_normalTrajectory() {
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(3, 4),    // distance = 5, dt = 100 => speed = 0.05
                new Point(6, 8)     // distance = 5, dt = 100 => speed = 0.05
            );
            List<Long> timestamps = List.of(0L, 100L, 200L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 200L);
            List<Double> speeds = data.speeds();

            assertThat(speeds).hasSize(2);
            assertThat(speeds.get(0)).isCloseTo(0.05, within(1e-9));
            assertThat(speeds.get(1)).isCloseTo(0.05, within(1e-9));
        }

        @Test
        @DisplayName("should return 0 speed when time difference is zero")
        void should_returnZeroSpeed_when_timeDiffZero() {
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 0),
                new Point(20, 0)
            );
            List<Long> timestamps = List.of(0L, 0L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 100L);
            List<Double> speeds = data.speeds();

            assertThat(speeds).hasSize(2);
            assertThat(speeds.get(0)).isEqualTo(0.0);
            assertThat(speeds.get(1)).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("should return size = points.size() - 1")
        void should_returnCorrectSize_when_multiplePoints() {
            List<Point> points = List.of(
                new Point(0, 0), new Point(1, 1), new Point(2, 2),
                new Point(3, 3), new Point(4, 4)
            );
            List<Long> timestamps = List.of(0L, 10L, 20L, 30L, 40L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 40L);

            assertThat(data.speeds()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Accelerations Tests")
    class AccelerationsTests {

        @Test
        @DisplayName("should calculate accelerations for normal trajectory")
        void should_returnNonEmptyAccelerations_when_normalTrajectory() {
            // 3 points => 2 speeds => 1 acceleration
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 0),   // speed = 10/100 = 0.1
                new Point(30, 0)    // speed = 20/100 = 0.2, accel = (0.2-0.1)/100 = 0.001
            );
            List<Long> timestamps = List.of(0L, 100L, 200L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 200L);
            List<Double> accels = data.accelerations();

            assertThat(accels).hasSize(1);
            assertThat(accels.get(0)).isCloseTo(0.001, within(1e-9));
        }

        @Test
        @DisplayName("should return empty list when only 2 points")
        void should_returnEmpty_when_only2Points() {
            List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
            List<Long> timestamps = List.of(0L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 100L);

            assertThat(data.accelerations()).isEmpty();
        }

        @Test
        @DisplayName("should show deceleration as negative acceleration")
        void should_showNegativeAcceleration_when_decelerating() {
            // Points: speed decreases
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(30, 0),   // speed = 30/100 = 0.3
                new Point(40, 0)    // speed = 10/100 = 0.1, accel = (0.1-0.3)/100 = -0.002
            );
            List<Long> timestamps = List.of(0L, 100L, 200L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 200L);
            List<Double> accels = data.accelerations();

            assertThat(accels.get(0)).isNegative();
        }
    }

    @Nested
    @DisplayName("Direction Changes Tests")
    class DirectionChangesTests {

        @Test
        @DisplayName("should return 0 for straight line trajectory")
        void should_returnZero_when_straightLine() {
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 0),
                new Point(20, 0),
                new Point(30, 0)
            );
            List<Long> timestamps = List.of(0L, 100L, 200L, 300L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 300L);

            assertThat(data.directionChanges()).isZero();
        }

        @Test
        @DisplayName("should count direction changes for zigzag trajectory")
        void should_countChanges_when_zigzagTrajectory() {
            // Zigzag: right, then up-right, then down-right
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 0),    // direction: 0 degrees (right)
                new Point(20, 20),   // direction: ~63 degrees (up-right, >15 deg change)
                new Point(30, 0)     // direction: ~-63 degrees (down-right, >15 deg change)
            );
            List<Long> timestamps = List.of(0L, 100L, 200L, 300L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 300L);

            assertThat(data.directionChanges()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("should return 0 when only 2 points")
        void should_returnZero_when_only2Points() {
            List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
            List<Long> timestamps = List.of(0L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 100L);

            assertThat(data.directionChanges()).isZero();
        }

        @Test
        @DisplayName("should skip zero-length segments")
        void should_skipZeroLengthSegments_when_duplicatePoints() {
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(0, 0),    // zero-length segment
                new Point(10, 0)
            );
            List<Long> timestamps = List.of(0L, 50L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 100L);

            // Should not throw, zero-length segments skipped
            assertThat(data.directionChanges()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Jitter Standard Deviation Tests")
    class JitterStdDevTests {

        @Test
        @DisplayName("should return zero jitter for a perfect straight line")
        void should_returnZero_when_perfectStraightLine() {
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 0),
                new Point(20, 0),
                new Point(30, 0)
            );
            List<Long> timestamps = List.of(0L, 100L, 200L, 300L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 300L);

            assertThat(data.jitterStdDev()).isCloseTo(0.0, within(1e-9));
        }

        @Test
        @DisplayName("should return positive jitter for zigzag trajectory")
        void should_returnPositiveJitter_when_zigzagLine() {
            // Zigzag around the x-axis
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 5),
                new Point(20, -5),
                new Point(30, 5),
                new Point(40, 0)
            );
            List<Long> timestamps = List.of(0L, 100L, 200L, 300L, 400L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 400L);

            assertThat(data.jitterStdDev()).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("should handle same start and end point")
        void should_handleSameStartEnd_when_closedLoop() {
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 10),
                new Point(0, 0)
            );
            List<Long> timestamps = List.of(0L, 100L, 200L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 200L);

            // Should not throw, falls back to distance-from-first-point calculation
            assertThat(data.jitterStdDev()).isGreaterThanOrEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Point Record Tests")
    class PointRecordTests {

        @Test
        @DisplayName("should have correct equals semantics")
        void should_beEqual_when_sameCoordinates() {
            Point p1 = new Point(10, 20);
            Point p2 = new Point(10, 20);

            assertThat(p1).isEqualTo(p2);
        }

        @Test
        @DisplayName("should not be equal when coordinates differ")
        void should_notBeEqual_when_differentCoordinates() {
            Point p1 = new Point(10, 20);
            Point p2 = new Point(10, 21);

            assertThat(p1).isNotEqualTo(p2);
        }

        @Test
        @DisplayName("should have consistent hashCode for equal points")
        void should_haveSameHashCode_when_equalPoints() {
            Point p1 = new Point(5, 15);
            Point p2 = new Point(5, 15);

            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("should expose x and y accessors")
        void should_exposeAccessors_when_created() {
            Point p = new Point(42, 99);

            assertThat(p.x()).isEqualTo(42);
            assertThat(p.y()).isEqualTo(99);
        }
    }

    @Nested
    @DisplayName("JitterStdDev Edge Case Tests")
    class JitterStdDevEdgeCaseTests {

        @Test
        @DisplayName("should handle exactly 2 points (minimum count)")
        void should_handleMinimumPoints_when_only2Points() {
            List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
            List<Long> timestamps = List.of(0L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 100L);

            // With 2 points forming the line, jitter should be 0
            assertThat(data.jitterStdDev()).isCloseTo(0.0, within(1e-9));
        }
    }

    @Nested
    @DisplayName("DirectionChanges Edge Case Tests")
    class DirectionChangesEdgeCaseTests {

        @Test
        @DisplayName("should handle zero-length segment when two consecutive points are identical")
        void should_skipZeroLength_when_consecutivePointsIdentical() {
            // p1=p2 (zero-length first segment), then p2->p3 has direction
            List<Point> points = List.of(
                new Point(5, 5),
                new Point(5, 5),   // same as first → zero-length segment
                new Point(10, 10), // normal segment
                new Point(20, 0)   // direction change from (10,10) segment
            );
            List<Long> timestamps = List.of(0L, 50L, 100L, 200L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 200L);

            // Should not throw; zero-length segments are skipped
            assertThat(data.directionChanges()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("should handle both segments zero-length")
        void should_handleBothZeroLength_when_threeIdenticalPoints() {
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(0, 0),
                new Point(0, 0)
            );
            List<Long> timestamps = List.of(0L, 50L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 100L);

            // Both segments are zero-length, should be skipped → 0 changes
            assertThat(data.directionChanges()).isZero();
        }
    }

    @Nested
    @DisplayName("Accelerations Edge Case Tests")
    class AccelerationsEdgeCaseTests {

        @Test
        @DisplayName("should return 0 acceleration when time diff is zero between speed intervals")
        void should_returnZeroAcceleration_when_timeDiffZeroBetweenIntervals() {
            // 3 points with timestamps [0, 100, 100] → dt for accel = t[2]-t[1] = 0
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 0),   // speed = 10/100 = 0.1
                new Point(20, 0)    // speed = 10/0 = 0.0 (dt=0), accel = (0.0-0.1)/0 → 0.0
            );
            List<Long> timestamps = List.of(0L, 100L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 100L);
            List<Double> accels = data.accelerations();

            assertThat(accels).hasSize(1);
            assertThat(accels.get(0)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("DirectionChanges Angle Wrapping Tests")
    class DirectionChangesAngleWrappingTests {

        @Test
        @DisplayName("should handle angle difference exceeding PI (wrap-around)")
        void should_handleAngleWraparound_when_angleDiffExceedsPi() {
            // Movement going right (angle ~0) then sharply going left (angle ~PI)
            // The raw diff would be close to PI, then we need a case where it's > PI
            // Going right then going slightly past left to trigger the wrap
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 1),    // direction: ~0 (slightly up-right)
                new Point(9, 0),     // direction: ~-PI+0.1 (slightly down-left — angle wraps)
                new Point(0, -1)     // Another direction
            );
            List<Long> timestamps = List.of(0L, 100L, 200L, 300L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 300L);

            // Should not throw; the wrap-around should be handled
            assertThat(data.directionChanges()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("should count direction change when movement reverses direction completely")
        void should_countChange_when_movementReversesCompletely() {
            // Go right, then go directly left (angle diff = PI exactly)
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(100, 0),   // direction: 0 (right)
                new Point(50, 0)     // direction: PI (left), diff = PI > threshold
            );
            List<Long> timestamps = List.of(0L, 100L, 200L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 200L);

            assertThat(data.directionChanges()).isEqualTo(1);
        }

        @Test
        @DisplayName("should handle second segment zero-length independently")
        void should_skipSecondZeroLength_when_onlySecondSegmentZero() {
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 10),   // first segment has length
                new Point(10, 10)    // second segment zero-length (same as p2)
            );
            List<Long> timestamps = List.of(0L, 100L, 200L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 200L);

            // Second segment is zero-length, should be skipped
            assertThat(data.directionChanges()).isZero();
        }
    }

    @Nested
    @DisplayName("Speeds Edge Case Tests")
    class SpeedsEdgeCaseTests {

        @Test
        @DisplayName("should return all zero speeds when all timestamps are the same")
        void should_returnAllZeroSpeeds_when_allTimestampsSame() {
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 0),
                new Point(20, 0)
            );
            List<Long> timestamps = List.of(100L, 100L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 0L);
            List<Double> speeds = data.speeds();

            assertThat(speeds).hasSize(2);
            assertThat(speeds.get(0)).isEqualTo(0.0);
            assertThat(speeds.get(1)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Defensive Copy Tests")
    class DefensiveCopyTests {

        @Test
        @DisplayName("should not be affected when modifying the input points list")
        void should_notBeAffected_when_inputPointsModified() {
            ArrayList<Point> points = new ArrayList<>(List.of(
                new Point(0, 0), new Point(10, 10), new Point(20, 20)
            ));
            ArrayList<Long> timestamps = new ArrayList<>(List.of(0L, 100L, 200L));

            TrajectoryData data = new TrajectoryData(points, timestamps, 200L);

            // Modify the original list
            points.add(new Point(30, 30));
            timestamps.add(300L);

            assertThat(data.points()).hasSize(3);
            assertThat(data.timestamps()).hasSize(3);
        }

        @Test
        @DisplayName("should return unmodifiable points list")
        void should_returnUnmodifiableList_when_accessingPoints() {
            List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
            List<Long> timestamps = List.of(0L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 100L);

            assertThatThrownBy(() -> data.points().add(new Point(99, 99)))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should return unmodifiable timestamps list")
        void should_returnUnmodifiableList_when_accessingTimestamps() {
            List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
            List<Long> timestamps = List.of(0L, 100L);

            TrajectoryData data = new TrajectoryData(points, timestamps, 100L);

            assertThatThrownBy(() -> data.timestamps().add(999L))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
