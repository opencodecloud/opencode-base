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

package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Stopwatch class
 * Stopwatch 类的全面测试
 *
 * @author Test
 * @since JDK 25, opencode-base-core V1.0.0
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Stopwatch Tests")
class StopwatchTest {

    // ==================== Factory Methods Tests ====================

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("createUnstarted creates stopped stopwatch")
        void createUnstartedIsNotRunning() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertFalse(sw.isRunning());
            assertEquals(0, sw.elapsedNanos());
        }

        @Test
        @DisplayName("createStarted creates running stopwatch")
        void createStartedIsRunning() {
            Stopwatch sw = Stopwatch.createStarted();
            assertTrue(sw.isRunning());
        }

        @Test
        @DisplayName("createStarted records elapsed time")
        void createStartedRecordsTime() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            assertTrue(sw.elapsedNanos() > 0);
        }
    }

    // ==================== start() Tests ====================

    @Nested
    @DisplayName("start() Method")
    class StartMethodTests {

        @Test
        @DisplayName("start sets isRunning to true")
        void startSetsRunning() {
            Stopwatch sw = Stopwatch.createUnstarted();
            sw.start();
            assertTrue(sw.isRunning());
        }

        @Test
        @DisplayName("start returns this for chaining")
        void startReturnsThis() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertSame(sw, sw.start());
        }

        @Test
        @DisplayName("start throws if already running")
        void startThrowsIfRunning() {
            Stopwatch sw = Stopwatch.createStarted();
            IllegalStateException ex = assertThrows(IllegalStateException.class, sw::start);
            assertEquals("Stopwatch is already running", ex.getMessage());
        }

        @Test
        @DisplayName("start can be called after stop")
        void startAfterStop() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.stop();
            assertDoesNotThrow(sw::start);
            assertTrue(sw.isRunning());
        }
    }

    // ==================== stop() Tests ====================

    @Nested
    @DisplayName("stop() Method")
    class StopMethodTests {

        @Test
        @DisplayName("stop sets isRunning to false")
        void stopSetsNotRunning() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.stop();
            assertFalse(sw.isRunning());
        }

        @Test
        @DisplayName("stop returns this for chaining")
        void stopReturnsThis() {
            Stopwatch sw = Stopwatch.createStarted();
            assertSame(sw, sw.stop());
        }

        @Test
        @DisplayName("stop throws if not running")
        void stopThrowsIfNotRunning() {
            Stopwatch sw = Stopwatch.createUnstarted();
            IllegalStateException ex = assertThrows(IllegalStateException.class, sw::stop);
            assertEquals("Stopwatch is not running", ex.getMessage());
        }

        @Test
        @DisplayName("stop preserves elapsed time")
        void stopPreservesElapsedTime() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            sw.stop();
            long elapsed = sw.elapsedNanos();
            Thread.sleep(10);
            assertEquals(elapsed, sw.elapsedNanos());
        }

        @Test
        @DisplayName("stop throws if called twice")
        void stopThrowsTwice() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.stop();
            assertThrows(IllegalStateException.class, sw::stop);
        }
    }

    // ==================== reset() Tests ====================

    @Nested
    @DisplayName("reset() Method")
    class ResetMethodTests {

        @Test
        @DisplayName("reset sets elapsed to zero")
        void resetSetsElapsedToZero() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            sw.reset();
            assertEquals(0, sw.elapsedNanos());
        }

        @Test
        @DisplayName("reset stops the stopwatch")
        void resetStopsStopwatch() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.reset();
            assertFalse(sw.isRunning());
        }

        @Test
        @DisplayName("reset returns this for chaining")
        void resetReturnsThis() {
            Stopwatch sw = Stopwatch.createStarted();
            assertSame(sw, sw.reset());
        }

        @Test
        @DisplayName("reset can be called when not running")
        void resetCanBeCalledWhenNotRunning() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertDoesNotThrow(sw::reset);
        }

        @Test
        @DisplayName("reset allows restarting")
        void resetAllowsRestart() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.reset();
            assertDoesNotThrow(sw::start);
            assertTrue(sw.isRunning());
        }
    }

    // ==================== isRunning() Tests ====================

    @Nested
    @DisplayName("isRunning() Method")
    class IsRunningMethodTests {

        @Test
        @DisplayName("isRunning returns false for unstarted")
        void isRunningFalseUnstarted() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertFalse(sw.isRunning());
        }

        @Test
        @DisplayName("isRunning returns true for started")
        void isRunningTrueStarted() {
            Stopwatch sw = Stopwatch.createStarted();
            assertTrue(sw.isRunning());
        }

        @Test
        @DisplayName("isRunning returns false after stop")
        void isRunningFalseAfterStop() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.stop();
            assertFalse(sw.isRunning());
        }

        @Test
        @DisplayName("isRunning returns false after reset")
        void isRunningFalseAfterReset() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.reset();
            assertFalse(sw.isRunning());
        }
    }

    // ==================== elapsed() Tests ====================

    @Nested
    @DisplayName("elapsed() Method")
    class ElapsedMethodTests {

        @Test
        @DisplayName("elapsed returns Duration")
        void elapsedReturnsDuration() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            sw.stop();
            Duration duration = sw.elapsed();
            assertNotNull(duration);
            assertTrue(duration.toNanos() > 0);
        }

        @Test
        @DisplayName("elapsed returns zero for unstarted")
        void elapsedZeroForUnstarted() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertEquals(Duration.ZERO, sw.elapsed());
        }
    }

    // ==================== elapsed(TimeUnit) Tests ====================

    @Nested
    @DisplayName("elapsed(TimeUnit) Method")
    class ElapsedWithUnitMethodTests {

        @Test
        @DisplayName("elapsed with NANOSECONDS")
        void elapsedNanoseconds() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            sw.stop();
            long nanos = sw.elapsed(TimeUnit.NANOSECONDS);
            assertTrue(nanos > 0);
        }

        @Test
        @DisplayName("elapsed with MICROSECONDS")
        void elapsedMicroseconds() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            sw.stop();
            long micros = sw.elapsed(TimeUnit.MICROSECONDS);
            assertTrue(micros > 0);
        }

        @Test
        @DisplayName("elapsed with MILLISECONDS")
        void elapsedMilliseconds() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(50);
            sw.stop();
            long millis = sw.elapsed(TimeUnit.MILLISECONDS);
            assertTrue(millis >= 40); // Allow some slack for timing
        }

        @Test
        @DisplayName("elapsed with SECONDS")
        void elapsedSeconds() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertEquals(0, sw.elapsed(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("elapsed with MINUTES")
        void elapsedMinutes() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertEquals(0, sw.elapsed(TimeUnit.MINUTES));
        }
    }

    // ==================== elapsedNanos() Tests ====================

    @Nested
    @DisplayName("elapsedNanos() Method")
    class ElapsedNanosMethodTests {

        @Test
        @DisplayName("elapsedNanos returns zero for unstarted")
        void elapsedNanosZero() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertEquals(0, sw.elapsedNanos());
        }

        @Test
        @DisplayName("elapsedNanos increases while running")
        void elapsedNanosIncreases() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            long first = sw.elapsedNanos();
            Thread.sleep(10);
            long second = sw.elapsedNanos();
            assertTrue(second > first);
        }

        @Test
        @DisplayName("elapsedNanos is constant when stopped")
        void elapsedNanosConstantWhenStopped() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            sw.stop();
            long first = sw.elapsedNanos();
            Thread.sleep(10);
            long second = sw.elapsedNanos();
            assertEquals(first, second);
        }
    }

    // ==================== elapsedMillis() Tests ====================

    @Nested
    @DisplayName("elapsedMillis() Method")
    class ElapsedMillisMethodTests {

        @Test
        @DisplayName("elapsedMillis returns zero for unstarted")
        void elapsedMillisZero() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertEquals(0, sw.elapsedMillis());
        }

        @Test
        @DisplayName("elapsedMillis measures time correctly")
        void elapsedMillisMeasures() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(100);
            sw.stop();
            long millis = sw.elapsedMillis();
            assertTrue(millis >= 90 && millis <= 200); // Allow slack
        }
    }

    // ==================== elapsedSeconds() Tests ====================

    @Nested
    @DisplayName("elapsedSeconds() Method")
    class ElapsedSecondsMethodTests {

        @Test
        @DisplayName("elapsedSeconds returns zero for unstarted")
        void elapsedSecondsZero() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertEquals(0, sw.elapsedSeconds());
        }

        @Test
        @DisplayName("elapsedSeconds rounds down")
        void elapsedSecondsRoundsDown() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(500); // 0.5 seconds
            sw.stop();
            assertEquals(0, sw.elapsedSeconds()); // Should round down to 0
        }
    }

    // ==================== toString() Tests ====================

    @Nested
    @DisplayName("toString() Method")
    class ToStringMethodTests {

        @Test
        @DisplayName("toString for nanoseconds")
        void toStringNanoseconds() {
            Stopwatch sw = Stopwatch.createUnstarted();
            // Unstarted should show 0 ns
            assertEquals("0 ns", sw.toString());
        }

        @Test
        @DisplayName("toString format contains unit")
        void toStringContainsUnit() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(1); // Small delay
            sw.stop();
            String str = sw.toString();
            // Should contain some time unit
            assertTrue(str.contains("ns") || str.contains("μs") || str.contains("ms"));
        }

        @Test
        @DisplayName("toString for milliseconds range")
        void toStringMilliseconds() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(50);
            sw.stop();
            String str = sw.toString();
            assertTrue(str.contains("ms"), "Expected ms unit but got: " + str);
        }

        @Test
        @DisplayName("toString while running")
        void toStringWhileRunning() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            String str = sw.toString();
            assertNotNull(str);
            assertFalse(str.isEmpty());
        }
    }

    // ==================== Accumulated Time Tests ====================

    @Nested
    @DisplayName("Accumulated Time Tests")
    class AccumulatedTimeTests {

        @Test
        @DisplayName("Time accumulates across start/stop cycles")
        void timeAccumulates() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(20);
            sw.stop();
            long first = sw.elapsedNanos();

            sw.start();
            Thread.sleep(20);
            sw.stop();
            long second = sw.elapsedNanos();

            assertTrue(second > first);
        }

        @Test
        @DisplayName("Reset clears accumulated time")
        void resetClearsAccumulated() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            sw.stop();
            assertTrue(sw.elapsedNanos() > 0);

            sw.reset();
            assertEquals(0, sw.elapsedNanos());
        }
    }

    // ==================== Fluent API Tests ====================

    @Nested
    @DisplayName("Fluent API Tests")
    class FluentApiTests {

        @Test
        @DisplayName("Fluent chaining works")
        void fluentChaining() {
            Stopwatch sw = Stopwatch.createUnstarted()
                    .start()
                    .stop()
                    .reset()
                    .start();
            assertTrue(sw.isRunning());
        }

        @Test
        @DisplayName("Full fluent workflow")
        void fullFluentWorkflow() throws InterruptedException {
            long elapsed = Stopwatch.createStarted()
                    .stop()
                    .reset()
                    .start()
                    .stop()
                    .elapsedNanos();
            assertTrue(elapsed >= 0);
        }
    }

    // ==================== Edge Cases Tests ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Multiple resets are safe")
        void multipleResets() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.reset();
            sw.reset();
            sw.reset();
            assertEquals(0, sw.elapsedNanos());
            assertFalse(sw.isRunning());
        }

        @Test
        @DisplayName("Reading elapsed while running is safe")
        void readingWhileRunning() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1);
                assertTrue(sw.elapsedNanos() > 0);
            }
            sw.stop();
        }

        @Test
        @DisplayName("Zero elapsed time string")
        void zeroElapsedTimeString() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertEquals("0 ns", sw.toString());
        }
    }

    // ==================== Suspend and Resume Tests ====================

    @Nested
    @DisplayName("suspend and resume")
    class SuspendResumeTests {

        @Test
        @DisplayName("suspend stops running")
        void suspendStopsRunning() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.suspend();
            assertThat(sw.isRunning()).isFalse();
        }

        @Test
        @DisplayName("resume restarts after suspend")
        void resumeRestartsAfterSuspend() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.suspend();
            sw.resume();
            assertThat(sw.isRunning()).isTrue();
        }

        @Test
        @DisplayName("suspended time is not counted")
        void suspendedTimeNotCounted() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(50);
            sw.suspend();
            Thread.sleep(100);
            sw.resume();
            Thread.sleep(50);
            sw.stop();
            long elapsedMs = sw.elapsedMillis();
            // Should be ~100ms (two 50ms active periods), not ~200ms
            assertThat(elapsedMs).isGreaterThanOrEqualTo(80).isLessThan(180);
        }

        @Test
        @DisplayName("suspend when not running throws")
        void suspendWhenNotRunningThrows() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertThatThrownBy(sw::suspend)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("resume when running throws")
        void resumeWhenRunningThrows() {
            Stopwatch sw = Stopwatch.createStarted();
            assertThatThrownBy(sw::resume)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ==================== Split and Laps Tests ====================

    @Nested
    @DisplayName("split and getLaps")
    class SplitLapsTests {

        @Test
        @DisplayName("split returns lap duration greater than zero")
        void splitReturnsLapDuration() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(10);
            Duration lap = sw.split();
            assertThat(lap.toNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("multiple splits are recorded")
        void multipleSplitsRecorded() throws InterruptedException {
            Stopwatch sw = Stopwatch.createStarted();
            Thread.sleep(5);
            sw.split();
            Thread.sleep(5);
            sw.split();
            Thread.sleep(5);
            sw.split();
            assertThat(sw.getLaps()).hasSize(3);
        }

        @Test
        @DisplayName("getLaps returns unmodifiable list")
        void getLapsIsUnmodifiable() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.split();
            java.util.List<Duration> laps = sw.getLaps();
            assertThatThrownBy(() -> laps.add(Duration.ZERO))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("reset clears laps")
        void resetClearsLaps() {
            Stopwatch sw = Stopwatch.createStarted();
            sw.split();
            sw.split();
            sw.reset();
            assertThat(sw.getLaps()).isEmpty();
        }

        @Test
        @DisplayName("split when not running throws")
        void splitWhenNotRunningThrows() {
            Stopwatch sw = Stopwatch.createUnstarted();
            assertThatThrownBy(sw::split)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ==================== Time Convenience Methods Tests ====================

    @Nested
    @DisplayName("time convenience methods")
    class TimeTests {

        @Test
        @DisplayName("time callable returns result and duration")
        void timeCallable() throws Exception {
            var result = Stopwatch.time(() -> "hello");
            assertThat(result.left()).isEqualTo("hello");
            assertThat(result.right().toNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("time runnable returns elapsed duration")
        void timeRunnable() {
            Duration elapsed = Stopwatch.time(() -> {
                try { Thread.sleep(10); } catch (InterruptedException ignored) { }
            });
            assertThat(elapsed.toMillis()).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("time callable propagates exception")
        void timeCallableException() {
            assertThatThrownBy(() -> Stopwatch.time(() -> { throw new java.io.IOException("fail"); }))
                    .isInstanceOf(java.io.IOException.class);
        }
    }
}
