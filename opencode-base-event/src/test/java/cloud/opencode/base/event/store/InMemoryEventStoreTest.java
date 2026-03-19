package cloud.opencode.base.event.store;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * InMemoryEventStore 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("InMemoryEventStore 测试")
class InMemoryEventStoreTest {

    static class TestEvent extends Event {
        public TestEvent() {
            super();
        }

        public TestEvent(String source) {
            super(source);
        }
    }

    static class OtherEvent extends Event {}

    private InMemoryEventStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryEventStore();
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("无参构造使用默认容量")
        void testDefaultConstructor() {
            InMemoryEventStore defaultStore = new InMemoryEventStore();

            assertThat(defaultStore.getMaxCapacity()).isEqualTo(10000);
        }

        @Test
        @DisplayName("指定容量构造")
        void testCustomCapacity() {
            InMemoryEventStore customStore = new InMemoryEventStore(500);

            assertThat(customStore.getMaxCapacity()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("save() 测试")
    class SaveTests {

        @Test
        @DisplayName("保存事件返回记录")
        void testSaveReturnsRecord() {
            TestEvent event = new TestEvent("source");

            EventRecord record = store.save(event);

            assertThat(record).isNotNull();
            assertThat(record.id()).isEqualTo(event.getId());
            assertThat(record.event()).isEqualTo(event);
            assertThat(record.sequenceNumber()).isEqualTo(1L);
        }

        @Test
        @DisplayName("序列号递增")
        void testSequenceNumberIncreases() {
            EventRecord record1 = store.save(new TestEvent());
            EventRecord record2 = store.save(new TestEvent());
            EventRecord record3 = store.save(new TestEvent());

            assertThat(record1.sequenceNumber()).isEqualTo(1L);
            assertThat(record2.sequenceNumber()).isEqualTo(2L);
            assertThat(record3.sequenceNumber()).isEqualTo(3L);
        }

        @Test
        @DisplayName("超过容量自动删除旧事件")
        void testCapacityManagement() {
            InMemoryEventStore smallStore = new InMemoryEventStore(3);

            smallStore.save(new TestEvent());
            smallStore.save(new TestEvent());
            smallStore.save(new TestEvent());
            assertThat(smallStore.count()).isEqualTo(3);

            smallStore.save(new TestEvent());
            assertThat(smallStore.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("null事件抛出异常")
        void testSaveNullThrows() {
            assertThatThrownBy(() -> store.save(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("findById() 测试")
    class FindByIdTests {

        @Test
        @DisplayName("找到事件返回Optional.of")
        void testFindExisting() {
            TestEvent event = new TestEvent();
            store.save(event);

            Optional<EventRecord> result = store.findById(event.getId());

            assertThat(result).isPresent();
            assertThat(result.get().event()).isEqualTo(event);
        }

        @Test
        @DisplayName("未找到返回Optional.empty")
        void testFindNonExisting() {
            Optional<EventRecord> result = store.findById("non-existent-id");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null id返回空")
        void testFindNullId() {
            Optional<EventRecord> result = store.findById(null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByType() 测试")
    class FindByTypeTests {

        @Test
        @DisplayName("按类型查找")
        void testFindByType() {
            store.save(new TestEvent());
            store.save(new TestEvent());
            store.save(new OtherEvent());

            List<EventRecord> results = store.findByType(TestEvent.class);

            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("null类型返回空列表")
        void testFindByNullType() {
            store.save(new TestEvent());

            List<EventRecord> results = store.findByType(null);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("未找到返回空列表")
        void testFindByTypeNoMatch() {
            store.save(new TestEvent());

            List<EventRecord> results = store.findByType(OtherEvent.class);

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByTimeRange() 测试")
    class FindByTimeRangeTests {

        @Test
        @DisplayName("按时间范围查找")
        void testFindByTimeRange() throws InterruptedException {
            Instant before = Instant.now();
            Thread.sleep(10);
            store.save(new TestEvent());
            Thread.sleep(10);
            Instant after = Instant.now();

            List<EventRecord> results = store.findByTimeRange(before, after);

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("null参数返回空列表")
        void testFindByNullRange() {
            store.save(new TestEvent());

            assertThat(store.findByTimeRange(null, Instant.now())).isEmpty();
            assertThat(store.findByTimeRange(Instant.now(), null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySource() 测试")
    class FindBySourceTests {

        @Test
        @DisplayName("按来源查找")
        void testFindBySource() {
            store.save(new TestEvent("source1"));
            store.save(new TestEvent("source1"));
            store.save(new TestEvent("source2"));

            List<EventRecord> results = store.findBySource("source1");

            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("null来源返回空列表")
        void testFindByNullSource() {
            store.save(new TestEvent("source"));

            List<EventRecord> results = store.findBySource(null);

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("replay() 测试")
    class ReplayTests {

        @Test
        @DisplayName("重放指定类型事件")
        void testReplayByType() {
            store.save(new TestEvent());
            store.save(new TestEvent());
            store.save(new OtherEvent());

            AtomicInteger count = new AtomicInteger(0);
            store.replay(TestEvent.class, event -> count.incrementAndGet());

            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("null类型不处理")
        void testReplayNullType() {
            store.save(new TestEvent());
            AtomicInteger count = new AtomicInteger(0);

            store.replay(null, event -> count.incrementAndGet());

            assertThat(count.get()).isZero();
        }

        @Test
        @DisplayName("null处理器不处理")
        void testReplayNullHandler() {
            store.save(new TestEvent());

            assertThatNoException().isThrownBy(() ->
                    store.replay(TestEvent.class, null));
        }
    }

    @Nested
    @DisplayName("replayByTimeRange() 测试")
    class ReplayByTimeRangeTests {

        @Test
        @DisplayName("按时间范围重放")
        void testReplayByTimeRange() throws InterruptedException {
            Instant before = Instant.now();
            Thread.sleep(10);
            store.save(new TestEvent());
            store.save(new TestEvent());
            Thread.sleep(10);
            Instant after = Instant.now();

            AtomicInteger count = new AtomicInteger(0);
            store.replayByTimeRange(before, after, event -> count.incrementAndGet());

            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("null参数不处理")
        void testReplayNullRange() {
            store.save(new TestEvent());
            AtomicInteger count = new AtomicInteger(0);

            store.replayByTimeRange(null, Instant.now(), event -> count.incrementAndGet());
            store.replayByTimeRange(Instant.now(), null, event -> count.incrementAndGet());
            store.replayByTimeRange(Instant.now(), Instant.now(), null);

            assertThat(count.get()).isZero();
        }
    }

    @Nested
    @DisplayName("count() 测试")
    class CountTests {

        @Test
        @DisplayName("返回事件数量")
        void testCount() {
            assertThat(store.count()).isZero();

            store.save(new TestEvent());
            assertThat(store.count()).isEqualTo(1);

            store.save(new TestEvent());
            store.save(new TestEvent());
            assertThat(store.count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("clear() 测试")
    class ClearTests {

        @Test
        @DisplayName("清空所有事件")
        void testClear() {
            store.save(new TestEvent());
            store.save(new TestEvent());
            assertThat(store.count()).isEqualTo(2);

            store.clear();

            assertThat(store.count()).isZero();
        }

        @Test
        @DisplayName("clear后序列号重置")
        void testClearResetsSequence() {
            store.save(new TestEvent());
            store.save(new TestEvent());
            assertThat(store.getCurrentSequence()).isEqualTo(2);

            store.clear();

            assertThat(store.getCurrentSequence()).isZero();
        }
    }

    @Nested
    @DisplayName("getMaxCapacity() 测试")
    class GetMaxCapacityTests {

        @Test
        @DisplayName("返回最大容量")
        void testGetMaxCapacity() {
            InMemoryEventStore customStore = new InMemoryEventStore(1000);

            assertThat(customStore.getMaxCapacity()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("findAll() 测试")
    class FindAllTests {

        @Test
        @DisplayName("返回所有记录")
        void testFindAll() {
            store.save(new TestEvent());
            store.save(new TestEvent());
            store.save(new OtherEvent());

            List<EventRecord> all = store.findAll();

            assertThat(all).hasSize(3);
        }

        @Test
        @DisplayName("返回副本")
        void testFindAllReturnsCopy() {
            store.save(new TestEvent());

            List<EventRecord> all1 = store.findAll();
            List<EventRecord> all2 = store.findAll();

            assertThat(all1).isNotSameAs(all2);
        }
    }

    @Nested
    @DisplayName("getCurrentSequence() 测试")
    class GetCurrentSequenceTests {

        @Test
        @DisplayName("返回当前序列号")
        void testGetCurrentSequence() {
            assertThat(store.getCurrentSequence()).isZero();

            store.save(new TestEvent());
            assertThat(store.getCurrentSequence()).isEqualTo(1);

            store.save(new TestEvent());
            store.save(new TestEvent());
            assertThat(store.getCurrentSequence()).isEqualTo(3);
        }
    }
}
