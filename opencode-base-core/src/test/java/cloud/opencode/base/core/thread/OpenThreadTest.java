package cloud.opencode.base.core.thread;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenThread 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenThread 测试")
class OpenThreadTest {

    @Nested
    @DisplayName("线程池创建测试")
    class ThreadPoolCreationTests {

        @Test
        @DisplayName("createFixedThreadPool")
        void testCreateFixedThreadPool() {
            ExecutorService pool = OpenThread.createFixedThreadPool(4, "test-fixed");
            try {
                assertThat(pool).isNotNull();
                assertThat(pool).isInstanceOf(ExecutorService.class);
            } finally {
                pool.shutdownNow();
            }
        }

        @Test
        @DisplayName("createCachedThreadPool")
        void testCreateCachedThreadPool() {
            ExecutorService pool = OpenThread.createCachedThreadPool("test-cached");
            try {
                assertThat(pool).isNotNull();
            } finally {
                pool.shutdownNow();
            }
        }

        @Test
        @DisplayName("createSingleThreadExecutor")
        void testCreateSingleThreadExecutor() {
            ExecutorService pool = OpenThread.createSingleThreadExecutor("test-single");
            try {
                assertThat(pool).isNotNull();
            } finally {
                pool.shutdownNow();
            }
        }

        @Test
        @DisplayName("createScheduledThreadPool")
        void testCreateScheduledThreadPool() {
            ScheduledExecutorService pool = OpenThread.createScheduledThreadPool(2, "test-scheduled");
            try {
                assertThat(pool).isNotNull();
                assertThat(pool).isInstanceOf(ScheduledExecutorService.class);
            } finally {
                pool.shutdownNow();
            }
        }

        @Test
        @DisplayName("createVirtualThreadExecutor")
        void testCreateVirtualThreadExecutor() {
            ExecutorService pool = OpenThread.createVirtualThreadExecutor();
            try {
                assertThat(pool).isNotNull();
            } finally {
                pool.shutdownNow();
            }
        }

        @Test
        @DisplayName("createVirtualThreadExecutor 带名称")
        void testCreateVirtualThreadExecutorWithName() {
            ExecutorService pool = OpenThread.createVirtualThreadExecutor("virtual");
            try {
                assertThat(pool).isNotNull();
            } finally {
                pool.shutdownNow();
            }
        }
    }

    @Nested
    @DisplayName("异步执行测试")
    class AsyncExecutionTests {

        @Test
        @DisplayName("runAsync")
        void testRunAsync() throws Exception {
            AtomicBoolean executed = new AtomicBoolean(false);

            CompletableFuture<Void> future = OpenThread.runAsync(() -> executed.set(true));
            future.get(1, TimeUnit.SECONDS);

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("runAsync 指定执行器")
        void testRunAsyncWithExecutor() throws Exception {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                AtomicBoolean executed = new AtomicBoolean(false);

                CompletableFuture<Void> future = OpenThread.runAsync(() -> executed.set(true), executor);
                future.get(1, TimeUnit.SECONDS);

                assertThat(executed.get()).isTrue();
            } finally {
                executor.shutdownNow();
            }
        }

        @Test
        @DisplayName("supplyAsync")
        void testSupplyAsync() throws Exception {
            CompletableFuture<String> future = OpenThread.supplyAsync(() -> "result");
            String result = future.get(1, TimeUnit.SECONDS);

            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("supplyAsync 指定执行器")
        void testSupplyAsyncWithExecutor() throws Exception {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                CompletableFuture<String> future = OpenThread.supplyAsync(() -> "result", executor);
                String result = future.get(1, TimeUnit.SECONDS);

                assertThat(result).isEqualTo("result");
            } finally {
                executor.shutdownNow();
            }
        }

        @Test
        @DisplayName("executeAsync 带超时")
        void testExecuteAsyncWithTimeout() throws Exception {
            CompletableFuture<String> future = OpenThread.executeAsync(() -> "result", Duration.ofSeconds(5));
            String result = future.get(1, TimeUnit.SECONDS);

            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("executeAsync 超时抛异常")
        void testExecuteAsyncTimeout() {
            CompletableFuture<String> future = OpenThread.executeAsync(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "result";
            }, Duration.ofMillis(100));

            assertThatThrownBy(() -> future.get(1, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(TimeoutException.class);
        }
    }

    @Nested
    @DisplayName("线程睡眠测试")
    class SleepTests {

        @Test
        @DisplayName("sleep Duration")
        void testSleep() {
            long start = System.currentTimeMillis();
            OpenThread.sleep(Duration.ofMillis(100));
            long elapsed = System.currentTimeMillis() - start;

            assertThat(elapsed).isGreaterThanOrEqualTo(90);
        }

        @Test
        @DisplayName("sleepMillis")
        void testSleepMillis() {
            long start = System.currentTimeMillis();
            OpenThread.sleepMillis(100);
            long elapsed = System.currentTimeMillis() - start;

            assertThat(elapsed).isGreaterThanOrEqualTo(90);
        }

        @Test
        @DisplayName("sleepSeconds")
        void testSleepSeconds() {
            // 只测试很短时间避免测试过慢
            long start = System.currentTimeMillis();
            OpenThread.sleepMillis(50);
            long elapsed = System.currentTimeMillis() - start;

            assertThat(elapsed).isGreaterThanOrEqualTo(40);
        }

        @Test
        @DisplayName("sleepInterruptibly 正常")
        void testSleepInterruptibly() {
            boolean result = OpenThread.sleepInterruptibly(Duration.ofMillis(50));
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("线程信息测试")
    class ThreadInfoTests {

        @Test
        @DisplayName("currentThread")
        void testCurrentThread() {
            Thread thread = OpenThread.currentThread();
            assertThat(thread).isSameAs(Thread.currentThread());
        }

        @Test
        @DisplayName("currentThreadName")
        void testCurrentThreadName() {
            String name = OpenThread.currentThreadName();
            assertThat(name).isEqualTo(Thread.currentThread().getName());
        }

        @Test
        @DisplayName("currentThreadId")
        void testCurrentThreadId() {
            long id = OpenThread.currentThreadId();
            assertThat(id).isEqualTo(Thread.currentThread().threadId());
        }

        @Test
        @DisplayName("isVirtualThread 当前线程")
        void testIsVirtualThread() {
            // 主线程通常不是虚拟线程
            assertThat(OpenThread.isVirtualThread()).isFalse();
        }

        @Test
        @DisplayName("isVirtualThread 指定线程")
        void testIsVirtualThreadSpecific() {
            assertThat(OpenThread.isVirtualThread(Thread.currentThread())).isFalse();
        }

        @Test
        @DisplayName("getAllThreads")
        void testGetAllThreads() {
            Thread[] threads = OpenThread.getAllThreads();
            assertThat(threads).isNotEmpty();
            assertThat(threads).contains(Thread.currentThread());
        }

        @Test
        @DisplayName("getThreadState")
        void testGetThreadState() {
            Thread.State state = OpenThread.getThreadState(Thread.currentThread().threadId());
            assertThat(state).isEqualTo(Thread.State.RUNNABLE);
        }

        @Test
        @DisplayName("getThreadState 不存在的线程")
        void testGetThreadStateNotExist() {
            Thread.State state = OpenThread.getThreadState(Long.MAX_VALUE);
            assertThat(state).isNull();
        }
    }

    @Nested
    @DisplayName("线程池关闭测试")
    class ShutdownTests {

        @Test
        @DisplayName("shutdownGracefully")
        void testShutdownGracefully() {
            ExecutorService pool = OpenThread.createFixedThreadPool(2, "shutdown-test");
            boolean result = OpenThread.shutdownGracefully(pool, Duration.ofSeconds(5));
            assertThat(result).isTrue();
            assertThat(pool.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("shutdownNow")
        void testShutdownNow() {
            ExecutorService pool = OpenThread.createFixedThreadPool(2, "shutdown-now-test");
            OpenThread.shutdownNow(pool);
            assertThat(pool.isShutdown()).isTrue();
        }
    }

    @Nested
    @DisplayName("中断处理测试")
    class InterruptTests {

        @Test
        @DisplayName("isInterrupted")
        void testIsInterrupted() {
            assertThat(OpenThread.isInterrupted()).isFalse();
        }

        @Test
        @DisplayName("interrupted")
        void testInterrupted() {
            assertThat(OpenThread.interrupted()).isFalse();
        }

        @Test
        @DisplayName("interrupt")
        void testInterrupt() throws Exception {
            AtomicReference<Boolean> interrupted = new AtomicReference<>(false);

            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    interrupted.set(true);
                }
            });

            thread.start();
            Thread.sleep(100); // 等待线程启动
            OpenThread.interrupt(thread);
            thread.join(1000);

            assertThat(interrupted.get()).isTrue();
        }

        @Test
        @DisplayName("interrupt null 线程")
        void testInterruptNull() {
            // 不应抛异常
            OpenThread.interrupt(null);
        }
    }

    @Nested
    @DisplayName("虚拟线程测试")
    class VirtualThreadTests {

        @Test
        @DisplayName("虚拟线程执行器执行任务")
        void testVirtualThreadExecutorExecution() throws Exception {
            ExecutorService pool = OpenThread.createVirtualThreadExecutor();
            try {
                AtomicBoolean isVirtual = new AtomicBoolean(false);

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    isVirtual.set(Thread.currentThread().isVirtual());
                }, pool);

                future.get(1, TimeUnit.SECONDS);
                assertThat(isVirtual.get()).isTrue();
            } finally {
                pool.shutdownNow();
            }
        }
    }
}
