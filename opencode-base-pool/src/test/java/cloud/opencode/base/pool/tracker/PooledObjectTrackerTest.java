package cloud.opencode.base.pool.tracker;

import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.factory.DefaultPooledObject;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * PooledObjectTrackerTest Tests
 * PooledObjectTrackerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("PooledObjectTracker 测试")
class PooledObjectTrackerTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造函数设置默认值")
        void testDefaultConstructor() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();

            assertThat(tracker.getTrackedCount()).isZero();
        }

        @Test
        @DisplayName("自定义构造函数设置所有参数")
        void testCustomConstructor() {
            AtomicInteger callbackCount = new AtomicInteger(0);
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>(
                    Duration.ofMinutes(5),
                    true,
                    abandoned -> callbackCount.incrementAndGet()
            );

            assertThat(tracker.getTrackedCount()).isZero();
        }
    }

    @Nested
    @DisplayName("trackBorrow方法测试")
    class TrackBorrowTests {

        @Test
        @DisplayName("追踪借用增加追踪数")
        void testTrackBorrow() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);

            assertThat(tracker.getTrackedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("追踪多个借用")
        void testTrackMultipleBorrows() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();

            tracker.trackBorrow(new DefaultPooledObject<>("test1"));
            tracker.trackBorrow(new DefaultPooledObject<>("test2"));
            tracker.trackBorrow(new DefaultPooledObject<>("test3"));

            assertThat(tracker.getTrackedCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("捕获调用栈")
        void testCaptureStackTrace() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>(
                    Duration.ofMinutes(5),
                    true,
                    null
            );
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);

            List<PooledObjectTracker.TrackedObject<String>> tracked = tracker.getTrackedObjects();
            assertThat(tracked).hasSize(1);
            assertThat(tracked.getFirst().stackTrace()).isNotNull();
        }

        @Test
        @DisplayName("不捕获调用栈")
        void testNoCaptureStackTrace() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>(
                    Duration.ofMinutes(5),
                    false,
                    null
            );
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);

            List<PooledObjectTracker.TrackedObject<String>> tracked = tracker.getTrackedObjects();
            assertThat(tracked).hasSize(1);
            assertThat(tracked.getFirst().stackTrace()).isNull();
        }
    }

    @Nested
    @DisplayName("trackReturn方法测试")
    class TrackReturnTests {

        @Test
        @DisplayName("追踪归还减少追踪数")
        void testTrackReturn() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);
            assertThat(tracker.getTrackedCount()).isEqualTo(1);

            tracker.trackReturn(pooled);
            assertThat(tracker.getTrackedCount()).isZero();
        }

        @Test
        @DisplayName("归还未追踪的对象不会报错")
        void testTrackReturnUntracked() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            assertThatCode(() -> tracker.trackReturn(pooled)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getTrackedObjects方法测试")
    class GetTrackedObjectsTests {

        @Test
        @DisplayName("返回所有追踪对象的副本")
        void testGetTrackedObjects() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();
            PooledObject<String> pooled1 = new DefaultPooledObject<>("test1");
            PooledObject<String> pooled2 = new DefaultPooledObject<>("test2");

            tracker.trackBorrow(pooled1);
            tracker.trackBorrow(pooled2);

            List<PooledObjectTracker.TrackedObject<String>> tracked = tracker.getTrackedObjects();
            assertThat(tracked).hasSize(2);
        }

        @Test
        @DisplayName("返回不可变列表")
        void testGetTrackedObjectsImmutable() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();
            tracker.trackBorrow(new DefaultPooledObject<>("test"));

            List<PooledObjectTracker.TrackedObject<String>> tracked = tracker.getTrackedObjects();

            assertThatThrownBy(() -> tracked.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("getAbandonedObjects方法测试")
    class GetAbandonedObjectsTests {

        @Test
        @DisplayName("返回超时的对象")
        void testGetAbandonedObjects() throws InterruptedException {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>(
                    Duration.ofMillis(50),
                    false,
                    null
            );
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);
            Thread.sleep(100);

            List<PooledObjectTracker.TrackedObject<String>> abandoned = tracker.getAbandonedObjects();
            assertThat(abandoned).hasSize(1);
        }

        @Test
        @DisplayName("不返回未超时的对象")
        void testGetAbandonedObjectsNotTimeout() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>(
                    Duration.ofMinutes(5),
                    false,
                    null
            );
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);

            List<PooledObjectTracker.TrackedObject<String>> abandoned = tracker.getAbandonedObjects();
            assertThat(abandoned).isEmpty();
        }
    }

    @Nested
    @DisplayName("checkAndHandleAbandoned方法测试")
    class CheckAndHandleAbandonedTests {

        @Test
        @DisplayName("检测到废弃对象时调用回调")
        void testCheckAndHandleAbandonedWithCallback() throws InterruptedException {
            List<PooledObjectTracker.TrackedObject<String>> callbackList = new ArrayList<>();
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>(
                    Duration.ofMillis(50),
                    false,
                    callbackList::add
            );
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);
            Thread.sleep(100);

            int count = tracker.checkAndHandleAbandoned();

            assertThat(count).isEqualTo(1);
            assertThat(callbackList).hasSize(1);
        }

        @Test
        @DisplayName("无回调时返回废弃数量")
        void testCheckAndHandleAbandonedNoCallback() throws InterruptedException {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>(
                    Duration.ofMillis(50),
                    false,
                    null
            );
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);
            Thread.sleep(100);

            int count = tracker.checkAndHandleAbandoned();

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("无废弃对象时返回零")
        void testCheckAndHandleAbandonedNone() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>(
                    Duration.ofMinutes(5),
                    false,
                    null
            );

            int count = tracker.checkAndHandleAbandoned();

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除所有追踪对象")
        void testClear() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();

            tracker.trackBorrow(new DefaultPooledObject<>("test1"));
            tracker.trackBorrow(new DefaultPooledObject<>("test2"));
            assertThat(tracker.getTrackedCount()).isEqualTo(2);

            tracker.clear();

            assertThat(tracker.getTrackedCount()).isZero();
        }
    }

    @Nested
    @DisplayName("TrackedObject Record测试")
    class TrackedObjectTests {

        @Test
        @DisplayName("borrowDuration返回借用时长")
        void testBorrowDuration() throws InterruptedException {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);
            Thread.sleep(50);

            PooledObjectTracker.TrackedObject<String> tracked = tracker.getTrackedObjects().getFirst();
            assertThat(tracked.borrowDuration().toMillis()).isGreaterThanOrEqualTo(50);
        }

        @Test
        @DisplayName("stackTraceString返回调用栈字符串")
        void testStackTraceStringWithCapture() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>(
                    Duration.ofMinutes(5),
                    true,
                    null
            );
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);

            PooledObjectTracker.TrackedObject<String> tracked = tracker.getTrackedObjects().getFirst();
            assertThat(tracked.stackTraceString()).contains("at ");
        }

        @Test
        @DisplayName("stackTraceString无捕获时返回N/A")
        void testStackTraceStringWithoutCapture() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>(
                    Duration.ofMinutes(5),
                    false,
                    null
            );
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);

            PooledObjectTracker.TrackedObject<String> tracked = tracker.getTrackedObjects().getFirst();
            assertThat(tracked.stackTraceString()).contains("N/A");
        }

        @Test
        @DisplayName("toString返回字符串表示")
        void testToString() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();
            PooledObject<String> pooled = new DefaultPooledObject<>("test-object");

            tracker.trackBorrow(pooled);

            PooledObjectTracker.TrackedObject<String> tracked = tracker.getTrackedObjects().getFirst();
            String str = tracked.toString();

            assertThat(str).contains("TrackedObject");
            assertThat(str).contains("test-object");
        }

        @Test
        @DisplayName("threadName返回借用线程名称")
        void testThreadName() {
            PooledObjectTracker<String> tracker = new PooledObjectTracker<>();
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            tracker.trackBorrow(pooled);

            PooledObjectTracker.TrackedObject<String> tracked = tracker.getTrackedObjects().getFirst();
            assertThat(tracked.threadName()).isEqualTo(Thread.currentThread().getName());
        }
    }
}
