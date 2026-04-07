package cloud.opencode.base.collections.concurrent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LockFreeStack 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("LockFreeStack 测试")
class LockFreeStackTest {

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationTests {

        @Test
        @DisplayName("push + pop - LIFO 顺序")
        void testPushPopLIFO() {
            LockFreeStack<Integer> stack = new LockFreeStack<>();

            stack.push(1);
            stack.push(2);
            stack.push(3);

            assertThat(stack.pop()).isEqualTo(3);
            assertThat(stack.pop()).isEqualTo(2);
            assertThat(stack.pop()).isEqualTo(1);
            assertThat(stack.pop()).isNull();
        }

        @Test
        @DisplayName("push + peek - 不移除元素")
        void testPushPeekDoesNotRemove() {
            LockFreeStack<String> stack = new LockFreeStack<>();

            stack.push("a");
            stack.push("b");

            assertThat(stack.peek()).isEqualTo("b");
            assertThat(stack.peek()).isEqualTo("b");
            assertThat(stack.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("pop - 空栈返回 null")
        void testPopEmpty() {
            LockFreeStack<String> stack = new LockFreeStack<>();

            assertThat(stack.pop()).isNull();
        }

        @Test
        @DisplayName("peek - 空栈返回 null")
        void testPeekEmpty() {
            LockFreeStack<String> stack = new LockFreeStack<>();

            assertThat(stack.peek()).isNull();
        }

        @Test
        @DisplayName("push null - 抛 NullPointerException")
        void testPushNull() {
            LockFreeStack<String> stack = new LockFreeStack<>();

            assertThatThrownBy(() -> stack.push(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("size - 准确跟踪元素数量")
        void testSizeAccurate() {
            LockFreeStack<String> stack = new LockFreeStack<>();

            assertThat(stack.size()).isZero();
            assertThat(stack.isEmpty()).isTrue();

            stack.push("a");
            assertThat(stack.size()).isEqualTo(1);
            assertThat(stack.isEmpty()).isFalse();

            stack.push("b");
            assertThat(stack.size()).isEqualTo(2);

            stack.pop();
            assertThat(stack.size()).isEqualTo(1);

            stack.pop();
            assertThat(stack.size()).isZero();
            assertThat(stack.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("clear - 清空栈")
        void testClear() {
            LockFreeStack<String> stack = new LockFreeStack<>();
            stack.push("a");
            stack.push("b");
            stack.push("c");

            stack.clear();

            assertThat(stack.isEmpty()).isTrue();
            assertThat(stack.size()).isZero();
            assertThat(stack.pop()).isNull();
            assertThat(stack.peek()).isNull();
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrentTests {

        @Test
        @DisplayName("多线程 push - 10 个线程各 push 100 个元素")
        void testConcurrentPush() throws InterruptedException {
            LockFreeStack<Integer> stack = new LockFreeStack<>();
            int threadCount = 10;
            int itemsPerThread = 100;
            int totalItems = threadCount * itemsPerThread;

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < itemsPerThread; j++) {
                            stack.push(threadId * itemsPerThread + j);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(finished).isTrue();
            assertThat(stack.size()).isEqualTo(totalItems);
        }

        @Test
        @DisplayName("多线程 push + pop - 不丢数据、不重复")
        void testConcurrentPushPop() throws InterruptedException {
            LockFreeStack<Integer> stack = new LockFreeStack<>();
            int threadCount = 10;
            int itemsPerThread = 100;
            int totalItems = threadCount * itemsPerThread;

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch pushDoneLatch = new CountDownLatch(threadCount);

            // Phase 1: 所有线程并发 push
            ExecutorService pushExecutor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                pushExecutor.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < itemsPerThread; j++) {
                            stack.push(threadId * itemsPerThread + j);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        pushDoneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            pushDoneLatch.await(30, TimeUnit.SECONDS);
            pushExecutor.shutdown();

            assertThat(stack.size()).isEqualTo(totalItems);

            // Phase 2: 多线程并发 pop，收集所有元素
            Set<Integer> collected = ConcurrentHashMap.newKeySet();
            AtomicInteger popCount = new AtomicInteger(0);
            CountDownLatch popDoneLatch = new CountDownLatch(threadCount);

            ExecutorService popExecutor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                popExecutor.submit(() -> {
                    try {
                        while (true) {
                            Integer item = stack.pop();
                            if (item != null) {
                                collected.add(item);
                                popCount.incrementAndGet();
                            } else {
                                break;
                            }
                        }
                    } finally {
                        popDoneLatch.countDown();
                    }
                });
            }

            popDoneLatch.await(30, TimeUnit.SECONDS);
            popExecutor.shutdown();

            // 验证：不丢数据、不重复
            assertThat(collected).hasSize(totalItems);
            assertThat(popCount.get()).isEqualTo(totalItems);
            assertThat(stack.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("交替 push + pop - 无死锁")
        void testAlternatingPushPop() throws InterruptedException {
            LockFreeStack<Integer> stack = new LockFreeStack<>();
            int threadCount = 10;
            int operationsPerThread = 1000;

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            AtomicInteger pushCount = new AtomicInteger(0);
            AtomicInteger popCount = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < operationsPerThread; j++) {
                            if (j % 2 == 0) {
                                stack.push(j);
                                pushCount.incrementAndGet();
                            } else {
                                Integer item = stack.pop();
                                if (item != null) {
                                    popCount.incrementAndGet();
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // 验证无死锁、操作计数一致
            assertThat(finished).isTrue();
            assertThat(pushCount.get() - popCount.get()).isEqualTo(stack.size());
        }
    }
}
