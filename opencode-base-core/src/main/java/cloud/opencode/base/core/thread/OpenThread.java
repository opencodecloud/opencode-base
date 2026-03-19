package cloud.opencode.base.core.thread;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Thread Utility Class - Thread pool creation and management
 * 线程工具类 - 线程池创建和管理
 *
 * <p>Provides utilities for thread pool creation, async execution, and thread management.</p>
 * <p>提供线程池创建、异步执行和线程管理工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread pool creation (fixed, cached, scheduled, virtual) - 线程池创建</li>
 *   <li>Async execution (runAsync, supplyAsync, timeout) - 异步执行</li>
 *   <li>Thread sleep utilities - 线程睡眠工具</li>
 *   <li>Graceful shutdown - 优雅关闭</li>
 *   <li>Virtual thread support (JDK 21+) - 虚拟线程支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create thread pool - 创建线程池
 * ExecutorService pool = OpenThread.createFixedThreadPool(4, "worker");
 *
 * // Async execution - 异步执行
 * CompletableFuture<String> future = OpenThread.supplyAsync(() -> "result");
 *
 * // Virtual thread - 虚拟线程
 * ExecutorService virtual = OpenThread.createVirtualThreadExecutor();
 *
 * // Graceful shutdown - 优雅关闭
 * OpenThread.shutdownGracefully(pool, Duration.ofSeconds(30));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenThread {

    private OpenThread() {
    }

    // ==================== 线程池创建 ====================

    /**
     * Creates a fixed-size thread pool
     * 创建固定大小线程池
     */
    public static ExecutorService createFixedThreadPool(int nThreads, String namePrefix) {
        return Executors.newFixedThreadPool(nThreads, new NamedThreadFactory(namePrefix));
    }

    /**
     * Creates a cached thread pool
     * 创建缓存线程池
     */
    public static ExecutorService createCachedThreadPool(String namePrefix) {
        return Executors.newCachedThreadPool(new NamedThreadFactory(namePrefix));
    }

    /**
     * Creates a single-thread executor
     * 创建单线程执行器
     */
    public static ExecutorService createSingleThreadExecutor(String namePrefix) {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory(namePrefix));
    }

    /**
     * Creates a scheduled thread pool
     * 创建调度线程池
     */
    public static ScheduledExecutorService createScheduledThreadPool(int corePoolSize, String namePrefix) {
        return Executors.newScheduledThreadPool(corePoolSize, new NamedThreadFactory(namePrefix));
    }

    /**
     * Creates a virtual thread executor (JDK 21+)
     * 创建虚拟线程执行器 (JDK 21+)
     */
    public static ExecutorService createVirtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Creates a virtual thread executor (with name prefix)
     * 创建虚拟线程执行器（带名称前缀）
     */
    public static ExecutorService createVirtualThreadExecutor(String namePrefix) {
        return Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name(namePrefix, 0).factory()
        );
    }

    // ==================== 异步执行 ====================

    /**
     * Executes a task asynchronously
     * 异步执行任务
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable);
    }

    /**
     * Executes a task asynchronously (with specified executor)
     * 异步执行任务（指定执行器）
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    /**
     * Executes asynchronously and returns a result
     * 异步执行并返回结果
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

    /**
     * Executes asynchronously and returns a result (with specified executor)
     * 异步执行并返回结果（指定执行器）
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    /**
     * Executes asynchronously with timeout
     * 带超时的异步执行
     */
    public static <T> CompletableFuture<T> executeAsync(Supplier<T> supplier, Duration timeout) {
        return CompletableFuture.supplyAsync(supplier)
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    // ==================== 线程睡眠 ====================

    /**
     * Puts the thread to sleep
     * 线程睡眠
     */
    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Puts the thread to sleep (milliseconds)
     * 线程睡眠（毫秒）
     */
    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Puts the thread to sleep (seconds)
     * 线程睡眠（秒）
     */
    public static void sleepSeconds(long seconds) {
        sleepMillis(seconds * 1000);
    }

    /**
     * Interruptible sleep
     * 可中断睡眠
     */
    public static boolean sleepInterruptibly(Duration duration) {
        try {
            Thread.sleep(duration);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // ==================== 线程信息 ====================

    /**
     * Gets the current thread
     * 获取当前线程
     */
    public static Thread currentThread() {
        return Thread.currentThread();
    }

    /**
     * Gets the current thread name
     * 获取当前线程名称
     */
    public static String currentThreadName() {
        return Thread.currentThread().getName();
    }

    /**
     * Gets the current thread ID
     * 获取当前线程 ID
     */
    public static long currentThreadId() {
        return Thread.currentThread().threadId();
    }

    /**
     * Checks if the current thread is a virtual thread
     * 检查当前线程是否为虚拟线程
     */
    public static boolean isVirtualThread() {
        return Thread.currentThread().isVirtual();
    }

    /**
     * Checks if the specified thread is a virtual thread
     * 检查指定线程是否为虚拟线程
     */
    public static boolean isVirtualThread(Thread thread) {
        return thread.isVirtual();
    }

    /**
     * Gets all threads
     * 获取所有线程
     */
    public static Thread[] getAllThreads() {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }
        Thread[] threads = new Thread[rootGroup.activeCount() * 2];
        int count = rootGroup.enumerate(threads, true);
        Thread[] result = new Thread[count];
        System.arraycopy(threads, 0, result, 0, count);
        return result;
    }

    /**
     * Gets the thread state
     * 获取线程状态
     */
    public static Thread.State getThreadState(long threadId) {
        for (Thread thread : getAllThreads()) {
            if (thread.threadId() == threadId) {
                return thread.getState();
            }
        }
        return null;
    }

    // ==================== 线程池关闭 ====================

    /**
     * Gracefully shuts down the thread pool
     * 优雅关闭线程池
     */
    public static boolean shutdownGracefully(ExecutorService executor, Duration timeout) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                return executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Immediately shuts down the thread pool
     * 立即关闭线程池
     */
    public static void shutdownNow(ExecutorService executor) {
        executor.shutdownNow();
    }

    // ==================== 中断处理 ====================

    /**
     * Checks if the current thread is interrupted
     * 检查当前线程是否被中断
     */
    public static boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    /**
     * Checks and clears the interrupt status
     * 检查并清除中断状态
     */
    public static boolean interrupted() {
        return Thread.interrupted();
    }

    /**
     * Interrupts the thread
     * 中断线程
     */
    public static void interrupt(Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }
}
