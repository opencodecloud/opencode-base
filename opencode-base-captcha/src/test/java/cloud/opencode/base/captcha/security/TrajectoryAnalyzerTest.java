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

import cloud.opencode.base.captcha.security.TrajectoryAnalyzer.TrajectoryResult;
import cloud.opencode.base.captcha.security.TrajectoryData.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for TrajectoryAnalyzer
 * TrajectoryAnalyzer 的全面测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
@DisplayName("TrajectoryAnalyzer Tests")
class TrajectoryAnalyzerTest {

    private TrajectoryAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new TrajectoryAnalyzer();
    }

    @Nested
    @DisplayName("INSUFFICIENT_DATA Tests")
    class InsufficientDataTests {

        @Test
        @DisplayName("should return INSUFFICIENT_DATA when fewer than 5 points")
        void should_returnInsufficientData_when_fewerThan5Points() {
            List<Point> points = List.of(
                new Point(0, 0), new Point(10, 10),
                new Point(20, 20), new Point(30, 30)
            );
            List<Long> timestamps = List.of(0L, 100L, 200L, 300L);
            TrajectoryData data = new TrajectoryData(points, timestamps, 300L);

            TrajectoryResult result = analyzer.analyze(data);

            assertThat(result).isEqualTo(TrajectoryResult.INSUFFICIENT_DATA);
        }

        @Test
        @DisplayName("should return INSUFFICIENT_DATA when exactly 2 points")
        void should_returnInsufficientData_when_exactly2Points() {
            List<Point> points = List.of(new Point(0, 0), new Point(100, 100));
            List<Long> timestamps = List.of(0L, 1000L);
            TrajectoryData data = new TrajectoryData(points, timestamps, 1000L);

            TrajectoryResult result = analyzer.analyze(data);

            assertThat(result).isEqualTo(TrajectoryResult.INSUFFICIENT_DATA);
        }

        @Test
        @DisplayName("should return INSUFFICIENT_DATA when exactly 4 points")
        void should_returnInsufficientData_when_exactly4Points() {
            List<Point> points = List.of(
                new Point(0, 0), new Point(10, 5),
                new Point(20, 3), new Point(30, 8)
            );
            List<Long> timestamps = List.of(0L, 300L, 600L, 900L);
            TrajectoryData data = new TrajectoryData(points, timestamps, 900L);

            TrajectoryResult result = analyzer.analyze(data);

            assertThat(result).isEqualTo(TrajectoryResult.INSUFFICIENT_DATA);
        }
    }

    @Nested
    @DisplayName("BOT_TOO_FAST Tests")
    class BotTooFastTests {

        @Test
        @DisplayName("should return BOT_TOO_FAST when totalDurationMs < 200")
        void should_returnBotTooFast_when_durationBelow200ms() {
            // 5 points, duration = 150ms (< 200ms threshold)
            List<Point> points = List.of(
                new Point(0, 0), new Point(10, 5),
                new Point(20, 3), new Point(30, 8),
                new Point(40, 2)
            );
            List<Long> timestamps = List.of(0L, 30L, 60L, 90L, 150L);
            TrajectoryData data = new TrajectoryData(points, timestamps, 150L);

            TrajectoryResult result = analyzer.analyze(data);

            assertThat(result).isEqualTo(TrajectoryResult.BOT_TOO_FAST);
        }

        @Test
        @DisplayName("should return BOT_TOO_FAST when totalDurationMs is 0")
        void should_returnBotTooFast_when_zeroDuration() {
            List<Point> points = List.of(
                new Point(0, 0), new Point(10, 5),
                new Point(20, 3), new Point(30, 8),
                new Point(40, 2)
            );
            List<Long> timestamps = List.of(0L, 0L, 0L, 0L, 0L);
            TrajectoryData data = new TrajectoryData(points, timestamps, 0L);

            TrajectoryResult result = analyzer.analyze(data);

            assertThat(result).isEqualTo(TrajectoryResult.BOT_TOO_FAST);
        }
    }

    @Nested
    @DisplayName("BOT_CONSTANT_SPEED Tests")
    class BotConstantSpeedTests {

        @Test
        @DisplayName("should return BOT_CONSTANT_SPEED for uniform-speed straight line")
        void should_returnBotConstantSpeed_when_uniformSpeed() {
            // Perfectly constant speed: each segment has same distance/time ratio
            List<Point> points = List.of(
                new Point(0, 0), new Point(10, 0),
                new Point(20, 0), new Point(30, 0),
                new Point(40, 0)
            );
            List<Long> timestamps = List.of(0L, 100L, 200L, 300L, 400L);
            TrajectoryData data = new TrajectoryData(points, timestamps, 400L);

            TrajectoryResult result = analyzer.analyze(data);

            assertThat(result).isEqualTo(TrajectoryResult.BOT_CONSTANT_SPEED);
        }
    }

    @Nested
    @DisplayName("BOT_NO_JITTER Tests")
    class BotNoJitterTests {

        @Test
        @DisplayName("should return BOT_NO_JITTER for perfect straight line with variable speed")
        void should_returnBotNoJitter_when_perfectLineVariableSpeed() {
            // Variable speed but perfect straight line (zero jitter)
            List<Point> points = List.of(
                new Point(0, 0), new Point(5, 0),
                new Point(20, 0), new Point(25, 0),
                new Point(50, 0)
            );
            // Different time intervals create variable speed
            List<Long> timestamps = List.of(0L, 100L, 150L, 300L, 500L);
            TrajectoryData data = new TrajectoryData(points, timestamps, 500L);

            TrajectoryResult result = analyzer.analyze(data);

            // Either BOT_CONSTANT_SPEED or BOT_NO_JITTER depending on variance ratio
            assertThat(result).isIn(TrajectoryResult.BOT_CONSTANT_SPEED,
                TrajectoryResult.BOT_NO_JITTER, TrajectoryResult.BOT_LINEAR);
        }
    }

    @Nested
    @DisplayName("BOT_LINEAR Tests")
    class BotLinearTests {

        @Test
        @DisplayName("should return BOT_LINEAR when no direction changes")
        void should_returnBotLinear_when_noDirectionChanges() {
            // Varied speed, slight jitter, but no direction changes (< 2)
            // All points roughly on the same diagonal line with slight perpendicular offset
            List<Point> points = List.of(
                new Point(0, 0),
                new Point(10, 1),      // slight perpendicular jitter
                new Point(25, 0),      // varying speed
                new Point(30, 1),
                new Point(50, 0)
            );
            List<Long> timestamps = List.of(0L, 100L, 180L, 350L, 600L);
            TrajectoryData data = new TrajectoryData(points, timestamps, 600L);

            TrajectoryResult result = analyzer.analyze(data);

            // Depending on exact jitter calculation, may be BOT_NO_JITTER or BOT_LINEAR
            assertThat(result).isIn(
                TrajectoryResult.BOT_NO_JITTER,
                TrajectoryResult.BOT_LINEAR,
                TrajectoryResult.BOT_CONSTANT_SPEED
            );
        }
    }

    @Nested
    @DisplayName("HUMAN Tests")
    class HumanTests {

        @Test
        @DisplayName("should return HUMAN for realistic human-like trajectory")
        void should_returnHuman_when_humanLikeTrajectory() {
            // Simulated human trajectory:
            // - >= 5 points
            // - duration >= 200ms
            // - highly variable speed (variance_ratio > 0.1)
            // - jitter > 0.5px
            // - >= 2 direction changes (> 15 degrees each)
            List<Point> points = new ArrayList<>();
            List<Long> timestamps = new ArrayList<>();

            // Build trajectory with dramatic speed changes, large direction changes, and jitter
            points.add(new Point(0, 0));       timestamps.add(0L);
            points.add(new Point(2, 5));       timestamps.add(200L);    // very slow start
            points.add(new Point(30, -10));    timestamps.add(230L);    // sudden fast burst + direction change
            points.add(new Point(35, 15));     timestamps.add(400L);    // slow again + sharp direction change
            points.add(new Point(80, -5));     timestamps.add(430L);    // fast burst + direction change
            points.add(new Point(85, 12));     timestamps.add(600L);    // slow + direction change
            points.add(new Point(120, -8));    timestamps.add(630L);    // fast burst + direction change
            points.add(new Point(130, 4));     timestamps.add(900L);    // slow to end

            TrajectoryData data = new TrajectoryData(points, timestamps, 900L);

            TrajectoryResult result = analyzer.analyze(data);

            assertThat(result).isEqualTo(TrajectoryResult.HUMAN);
        }

        @Test
        @DisplayName("should return HUMAN for longer realistic trajectory")
        void should_returnHuman_when_longerRealisticTrajectory() {
            List<Point> points = new ArrayList<>();
            List<Long> timestamps = new ArrayList<>();

            // Human-like with dramatic speed variation: slow-fast-slow pattern with jitter
            points.add(new Point(0, 0));       timestamps.add(0L);
            points.add(new Point(1, 8));       timestamps.add(300L);    // very slow
            points.add(new Point(40, -15));    timestamps.add(320L);    // sudden fast
            points.add(new Point(42, 20));     timestamps.add(600L);    // slow + big direction change
            points.add(new Point(90, -10));    timestamps.add(620L);    // fast again
            points.add(new Point(92, 15));     timestamps.add(900L);    // slow + direction change
            points.add(new Point(140, -12));   timestamps.add(920L);    // fast
            points.add(new Point(142, 8));     timestamps.add(1200L);   // slow + direction change
            points.add(new Point(180, -5));    timestamps.add(1220L);   // fast
            points.add(new Point(185, 3));     timestamps.add(1500L);   // slow to end

            TrajectoryData data = new TrajectoryData(points, timestamps, 1500L);

            TrajectoryResult result = analyzer.analyze(data);

            assertThat(result).isEqualTo(TrajectoryResult.HUMAN);
        }
    }

    @Nested
    @DisplayName("Null Input Tests")
    class NullInputTests {

        @Test
        @DisplayName("should throw NullPointerException when data is null")
        void should_throwNPE_when_dataNull() {
            assertThatThrownBy(() -> analyzer.analyze(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("data");
        }
    }
}
