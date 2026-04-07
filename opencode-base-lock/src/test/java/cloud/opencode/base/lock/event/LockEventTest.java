package cloud.opencode.base.lock.event;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * LockEvent test - 锁事件测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.3
 */
class LockEventTest {

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("acquired() should create ACQUIRED event with wait time")
        void acquired_shouldCreateAcquiredEventWithWaitTime() {
            Duration waitTime = Duration.ofMillis(100);

            LockEvent event = LockEvent.acquired("test-lock", waitTime);

            assertThat(event.type()).isEqualTo(LockEvent.EventType.ACQUIRED);
            assertThat(event.lockName()).isEqualTo("test-lock");
            assertThat(event.waitTime()).isEqualTo(waitTime);
            assertThat(event.threadName()).isEqualTo(Thread.currentThread().getName());
            assertThat(event.threadId()).isEqualTo(Thread.currentThread().threadId());
            assertThat(event.timestamp()).isNotNull();
            assertThat(event.timestamp()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("acquired() should accept null wait time")
        void acquired_shouldAcceptNullWaitTime() {
            LockEvent event = LockEvent.acquired("test-lock", null);

            assertThat(event.type()).isEqualTo(LockEvent.EventType.ACQUIRED);
            assertThat(event.waitTime()).isNull();
        }

        @Test
        @DisplayName("acquired() should accept zero wait time")
        void acquired_shouldAcceptZeroWaitTime() {
            LockEvent event = LockEvent.acquired("test-lock", Duration.ZERO);

            assertThat(event.type()).isEqualTo(LockEvent.EventType.ACQUIRED);
            assertThat(event.waitTime()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("released() should create RELEASED event without wait time")
        void released_shouldCreateReleasedEventWithoutWaitTime() {
            LockEvent event = LockEvent.released("test-lock");

            assertThat(event.type()).isEqualTo(LockEvent.EventType.RELEASED);
            assertThat(event.lockName()).isEqualTo("test-lock");
            assertThat(event.waitTime()).isNull();
            assertThat(event.threadName()).isEqualTo(Thread.currentThread().getName());
            assertThat(event.threadId()).isEqualTo(Thread.currentThread().threadId());
            assertThat(event.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("timeout() should create TIMEOUT event with wait time")
        void timeout_shouldCreateTimeoutEventWithWaitTime() {
            Duration waitTime = Duration.ofSeconds(5);

            LockEvent event = LockEvent.timeout("test-lock", waitTime);

            assertThat(event.type()).isEqualTo(LockEvent.EventType.TIMEOUT);
            assertThat(event.lockName()).isEqualTo("test-lock");
            assertThat(event.waitTime()).isEqualTo(waitTime);
            assertThat(event.threadName()).isNotNull();
            assertThat(event.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("error() should create ERROR event without wait time")
        void error_shouldCreateErrorEventWithoutWaitTime() {
            LockEvent event = LockEvent.error("test-lock");

            assertThat(event.type()).isEqualTo(LockEvent.EventType.ERROR);
            assertThat(event.lockName()).isEqualTo("test-lock");
            assertThat(event.waitTime()).isNull();
            assertThat(event.threadName()).isNotNull();
            assertThat(event.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("factory methods should capture current thread info")
        void factoryMethods_shouldCaptureCurrentThreadInfo() {
            String expectedName = Thread.currentThread().getName();
            long expectedId = Thread.currentThread().threadId();

            LockEvent acquired = LockEvent.acquired("lock", Duration.ZERO);
            LockEvent released = LockEvent.released("lock");
            LockEvent timeout = LockEvent.timeout("lock", Duration.ofSeconds(1));
            LockEvent error = LockEvent.error("lock");

            assertThat(acquired.threadName()).isEqualTo(expectedName);
            assertThat(acquired.threadId()).isEqualTo(expectedId);
            assertThat(released.threadName()).isEqualTo(expectedName);
            assertThat(released.threadId()).isEqualTo(expectedId);
            assertThat(timeout.threadName()).isEqualTo(expectedName);
            assertThat(timeout.threadId()).isEqualTo(expectedId);
            assertThat(error.threadName()).isEqualTo(expectedName);
            assertThat(error.threadId()).isEqualTo(expectedId);
        }
    }

    @Nested
    @DisplayName("Validation Tests | 验证测试")
    class ValidationTests {

        @Test
        @DisplayName("null type should throw IllegalArgumentException")
        void nullType_shouldThrowIllegalArgumentException() {
            assertThatThrownBy(() -> new LockEvent(
                    null, "test-lock",
                    "main", 1L,
                    Instant.now(), null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Event type must not be null");
        }

        @Test
        @DisplayName("null timestamp should default to now")
        void nullTimestamp_shouldDefaultToNow() {
            Instant before = Instant.now();

            LockEvent event = new LockEvent(
                    LockEvent.EventType.ACQUIRED, "test-lock",
                    "main", 1L,
                    null, null
            );

            Instant after = Instant.now();

            assertThat(event.timestamp()).isNotNull();
            assertThat(event.timestamp()).isBetween(before, after);
        }

        @Test
        @DisplayName("null threadName should default to current thread name")
        void nullThreadName_shouldDefaultToCurrentThreadName() {
            LockEvent event = new LockEvent(
                    LockEvent.EventType.RELEASED, "test-lock",
                    null, 42L,
                    Instant.now(), null
            );

            assertThat(event.threadName()).isEqualTo(Thread.currentThread().getName());
        }

        @Test
        @DisplayName("explicit timestamp should be preserved")
        void explicitTimestamp_shouldBePreserved() {
            Instant explicitTime = Instant.parse("2026-01-01T00:00:00Z");

            LockEvent event = new LockEvent(
                    LockEvent.EventType.ACQUIRED, "test-lock",
                    "worker-1", 10L,
                    explicitTime, Duration.ofMillis(50)
            );

            assertThat(event.timestamp()).isEqualTo(explicitTime);
        }

        @Test
        @DisplayName("explicit threadName should be preserved")
        void explicitThreadName_shouldBePreserved() {
            LockEvent event = new LockEvent(
                    LockEvent.EventType.ACQUIRED, "test-lock",
                    "custom-thread", 99L,
                    Instant.now(), null
            );

            assertThat(event.threadName()).isEqualTo("custom-thread");
        }

        @Test
        @DisplayName("null lockName should be allowed")
        void nullLockName_shouldBeAllowed() {
            LockEvent event = LockEvent.acquired(null, Duration.ZERO);

            assertThat(event.lockName()).isNull();
        }
    }

    @Nested
    @DisplayName("EventType Tests | 事件类型测试")
    class EventTypeTests {

        @Test
        @DisplayName("should have all expected enum values")
        void shouldHaveAllExpectedEnumValues() {
            LockEvent.EventType[] values = LockEvent.EventType.values();

            assertThat(values).hasSize(4);
            assertThat(values).containsExactly(
                    LockEvent.EventType.ACQUIRED,
                    LockEvent.EventType.RELEASED,
                    LockEvent.EventType.TIMEOUT,
                    LockEvent.EventType.ERROR
            );
        }

        @Test
        @DisplayName("valueOf should return correct enum constant")
        void valueOf_shouldReturnCorrectEnumConstant() {
            assertThat(LockEvent.EventType.valueOf("ACQUIRED"))
                    .isEqualTo(LockEvent.EventType.ACQUIRED);
            assertThat(LockEvent.EventType.valueOf("RELEASED"))
                    .isEqualTo(LockEvent.EventType.RELEASED);
            assertThat(LockEvent.EventType.valueOf("TIMEOUT"))
                    .isEqualTo(LockEvent.EventType.TIMEOUT);
            assertThat(LockEvent.EventType.valueOf("ERROR"))
                    .isEqualTo(LockEvent.EventType.ERROR);
        }

        @Test
        @DisplayName("invalid valueOf should throw IllegalArgumentException")
        void invalidValueOf_shouldThrowIllegalArgumentException() {
            assertThatThrownBy(() -> LockEvent.EventType.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Record Behavior Tests | 记录行为测试")
    class RecordBehaviorTests {

        @Test
        @DisplayName("equals should work for identical events")
        void equals_shouldWorkForIdenticalEvents() {
            Instant timestamp = Instant.now();
            Duration waitTime = Duration.ofMillis(100);

            LockEvent event1 = new LockEvent(
                    LockEvent.EventType.ACQUIRED, "lock-1",
                    "main", 1L, timestamp, waitTime
            );
            LockEvent event2 = new LockEvent(
                    LockEvent.EventType.ACQUIRED, "lock-1",
                    "main", 1L, timestamp, waitTime
            );

            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("toString should contain meaningful information")
        void toString_shouldContainMeaningfulInformation() {
            LockEvent event = LockEvent.acquired("my-lock", Duration.ofMillis(50));

            String str = event.toString();

            assertThat(str).contains("LockEvent");
            assertThat(str).contains("ACQUIRED");
            assertThat(str).contains("my-lock");
        }
    }
}
