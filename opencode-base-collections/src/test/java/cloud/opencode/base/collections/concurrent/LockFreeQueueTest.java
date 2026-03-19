package cloud.opencode.base.collections.concurrent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LockFreeQueue 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("LockFreeQueue 测试")
class LockFreeQueueTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空队列")
        void testCreate() {
            LockFreeQueue<String> queue = LockFreeQueue.create();

            assertThat(queue).isNotNull();
            assertThat(queue.isEmpty()).isTrue();
            assertThat(queue.size()).isZero();
        }

        @Test
        @DisplayName("create - 从集合创建")
        void testCreateFromCollection() {
            List<String> list = List.of("a", "b", "c");

            LockFreeQueue<String> queue = LockFreeQueue.create(list);

            assertThat(queue.size()).isEqualTo(3);
            assertThat(queue.poll()).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("offer 操作测试")
    class OfferOperationTests {

        @Test
        @DisplayName("offer - 添加元素")
        void testOffer() {
            LockFreeQueue<String> queue = LockFreeQueue.create();

            boolean result = queue.offer("a");

            assertThat(result).isTrue();
            assertThat(queue.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("offer - null 抛异常")
        void testOfferNull() {
            LockFreeQueue<String> queue = LockFreeQueue.create();

            assertThatThrownBy(() -> queue.offer(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("offer - 多个元素")
        void testOfferMultiple() {
            LockFreeQueue<String> queue = LockFreeQueue.create();

            queue.offer("a");
            queue.offer("b");
            queue.offer("c");

            assertThat(queue.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("poll 操作测试")
    class PollOperationTests {

        @Test
        @DisplayName("poll - 获取并移除")
        void testPoll() {
            LockFreeQueue<String> queue = LockFreeQueue.create();
            queue.offer("a");
            queue.offer("b");

            String result = queue.poll();

            assertThat(result).isEqualTo("a");
            assertThat(queue.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("poll - 空队列返回 null")
        void testPollEmpty() {
            LockFreeQueue<String> queue = LockFreeQueue.create();

            String result = queue.poll();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("poll - FIFO 顺序")
        void testPollFIFO() {
            LockFreeQueue<Integer> queue = LockFreeQueue.create();
            queue.offer(1);
            queue.offer(2);
            queue.offer(3);

            assertThat(queue.poll()).isEqualTo(1);
            assertThat(queue.poll()).isEqualTo(2);
            assertThat(queue.poll()).isEqualTo(3);
            assertThat(queue.poll()).isNull();
        }
    }

    @Nested
    @DisplayName("peek 操作测试")
    class PeekOperationTests {

        @Test
        @DisplayName("peek - 查看头元素")
        void testPeek() {
            LockFreeQueue<String> queue = LockFreeQueue.create();
            queue.offer("a");
            queue.offer("b");

            String result = queue.peek();

            assertThat(result).isEqualTo("a");
            assertThat(queue.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("peek - 空队列返回 null")
        void testPeekEmpty() {
            LockFreeQueue<String> queue = LockFreeQueue.create();

            String result = queue.peek();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("peek - 不移除元素")
        void testPeekDoesNotRemove() {
            LockFreeQueue<String> queue = LockFreeQueue.create();
            queue.offer("a");

            queue.peek();
            queue.peek();

            assertThat(queue.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("size/isEmpty 测试")
    class SizeTests {

        @Test
        @DisplayName("size - 统计元素数量")
        void testSize() {
            LockFreeQueue<String> queue = LockFreeQueue.create();

            assertThat(queue.size()).isZero();

            queue.offer("a");
            assertThat(queue.size()).isEqualTo(1);

            queue.offer("b");
            assertThat(queue.size()).isEqualTo(2);

            queue.poll();
            assertThat(queue.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("isEmpty - 空检查")
        void testIsEmpty() {
            LockFreeQueue<String> queue = LockFreeQueue.create();

            assertThat(queue.isEmpty()).isTrue();

            queue.offer("a");
            assertThat(queue.isEmpty()).isFalse();

            queue.poll();
            assertThat(queue.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("contains 测试")
    class ContainsTests {

        @Test
        @DisplayName("contains - 包含检查")
        void testContains() {
            LockFreeQueue<String> queue = LockFreeQueue.create();
            queue.offer("a");
            queue.offer("b");

            assertThat(queue.contains("a")).isTrue();
            assertThat(queue.contains("c")).isFalse();
        }

        @Test
        @DisplayName("contains - null 返回 false")
        void testContainsNull() {
            LockFreeQueue<String> queue = LockFreeQueue.create();
            queue.offer("a");

            assertThat(queue.contains(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("clear 测试")
    class ClearTests {

        @Test
        @DisplayName("clear - 清空队列")
        void testClear() {
            LockFreeQueue<String> queue = LockFreeQueue.create();
            queue.offer("a");
            queue.offer("b");
            queue.offer("c");

            queue.clear();

            assertThat(queue.isEmpty()).isTrue();
            assertThat(queue.size()).isZero();
        }
    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("iterator - 遍历元素")
        void testIterator() {
            LockFreeQueue<String> queue = LockFreeQueue.create();
            queue.offer("a");
            queue.offer("b");
            queue.offer("c");

            List<String> result = new ArrayList<>();
            for (String s : queue) {
                result.add(s);
            }

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("iterator - 空队列")
        void testIteratorEmpty() {
            LockFreeQueue<String> queue = LockFreeQueue.create();

            List<String> result = new ArrayList<>();
            for (String s : queue) {
                result.add(s);
            }

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("iterator - remove 抛异常")
        void testIteratorRemove() {
            LockFreeQueue<String> queue = LockFreeQueue.create();
            queue.offer("a");

            Iterator<String> iterator = queue.iterator();
            iterator.next();

            assertThatThrownBy(iterator::remove)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("iterator - 越界")
        void testIteratorOutOfBounds() {
            LockFreeQueue<String> queue = LockFreeQueue.create();
            queue.offer("a");

            Iterator<String> iterator = queue.iterator();
            iterator.next();

            assertThatThrownBy(iterator::next)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrentTests {

        @Test
        @DisplayName("多生产者多消费者")
        void testMultipleProducersConsumers() throws InterruptedException {
            LockFreeQueue<Integer> queue = LockFreeQueue.create();
            int producerCount = 4;
            int consumerCount = 4;
            int itemsPerProducer = 1000;
            int totalItems = producerCount * itemsPerProducer;

            AtomicInteger producedCount = new AtomicInteger(0);
            AtomicInteger consumedCount = new AtomicInteger(0);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch producerLatch = new CountDownLatch(producerCount);
            CountDownLatch consumerLatch = new CountDownLatch(consumerCount);

            // 创建生产者
            ExecutorService producers = Executors.newFixedThreadPool(producerCount);
            for (int i = 0; i < producerCount; i++) {
                final int producerId = i;
                producers.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < itemsPerProducer; j++) {
                            queue.offer(producerId * itemsPerProducer + j);
                            producedCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        producerLatch.countDown();
                    }
                });
            }

            // 创建消费者
            ExecutorService consumers = Executors.newFixedThreadPool(consumerCount);
            for (int i = 0; i < consumerCount; i++) {
                consumers.submit(() -> {
                    try {
                        startLatch.await();
                        while (consumedCount.get() < totalItems) {
                            Integer item = queue.poll();
                            if (item != null) {
                                consumedCount.incrementAndGet();
                            } else {
                                Thread.yield();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        consumerLatch.countDown();
                    }
                });
            }

            // 开始
            startLatch.countDown();

            // 等待生产者完成
            producerLatch.await(30, TimeUnit.SECONDS);

            // 等待消费者消费完所有元素
            boolean consumersFinished = consumerLatch.await(30, TimeUnit.SECONDS);

            producers.shutdown();
            consumers.shutdown();

            assertThat(producedCount.get()).isEqualTo(totalItems);
            assertThat(consumedCount.get()).isEqualTo(totalItems);
            assertThat(queue.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("单生产者多消费者")
        void testSingleProducerMultipleConsumers() throws InterruptedException {
            LockFreeQueue<Integer> queue = LockFreeQueue.create();
            int itemCount = 10000;
            int consumerCount = 4;

            AtomicInteger consumedCount = new AtomicInteger(0);
            CountDownLatch producerLatch = new CountDownLatch(1);
            CountDownLatch consumerLatch = new CountDownLatch(consumerCount);

            // 生产者
            Thread producer = new Thread(() -> {
                for (int i = 0; i < itemCount; i++) {
                    queue.offer(i);
                }
                producerLatch.countDown();
            });

            // 消费者
            ExecutorService consumers = Executors.newFixedThreadPool(consumerCount);
            for (int i = 0; i < consumerCount; i++) {
                consumers.submit(() -> {
                    try {
                        while (consumedCount.get() < itemCount) {
                            Integer item = queue.poll();
                            if (item != null) {
                                consumedCount.incrementAndGet();
                            } else {
                                Thread.yield();
                            }
                        }
                    } finally {
                        consumerLatch.countDown();
                    }
                });
            }

            producer.start();
            producerLatch.await(10, TimeUnit.SECONDS);
            consumerLatch.await(30, TimeUnit.SECONDS);

            consumers.shutdown();

            assertThat(consumedCount.get()).isEqualTo(itemCount);
        }
    }
}
