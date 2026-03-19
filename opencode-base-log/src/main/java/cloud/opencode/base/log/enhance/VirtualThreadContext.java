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

package cloud.opencode.base.log.enhance;

import cloud.opencode.base.log.exception.OpenLogException;
import cloud.opencode.base.log.context.LogContext;
import cloud.opencode.base.log.context.MDC;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Virtual Thread Context Propagation - JDK 25+
 * 虚拟线程上下文传播 - JDK 25+
 *
 * <p>Provides utilities for propagating log context (MDC/NDC) across Virtual Threads.
 * Traditional ThreadLocal-based MDC does not automatically inherit to Virtual Threads,
 * so this class provides wrapping utilities to capture and restore context.</p>
 * <p>提供跨虚拟线程传播日志上下文（MDC/NDC）的工具。
 * 传统基于 ThreadLocal 的 MDC 不会自动继承到虚拟线程，
 * 因此本类提供包装工具来捕获和恢复上下文。</p>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * LogContext.setTraceId("trace-123");
 * LogContext.setUserId("user-456");
 *
 * // Virtual Thread automatically inherits log context
 * VirtualThreadContext.startVirtualThread(() -> {
 *     // traceId and userId are accessible here
 *     OpenLog.info("Async processing");
 * });
 *
 * // Using ExecutorService
 * ExecutorService executor = VirtualThreadContext.newVirtualThreadExecutor();
 * executor.submit(() -> {
 *     OpenLog.info("Task in pool");  // Context propagated
 * });
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>MDC/NDC context propagation to virtual threads - MDC/NDC 上下文传播到虚拟线程</li>
 *   <li>Wrappers for Runnable, Callable, Function, CompletableFuture - Runnable、Callable、Function、CompletableFuture 的包装器</li>
 *   <li>Virtual thread executor with context propagation - 带上下文传播的虚拟线程执行器</li>
 *   <li>Async execution helpers (runAsync, supplyAsync) - 异步执行辅助方法（runAsync、supplyAsync）</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (captures and restores context safely) - 线程安全: 是（安全地捕获和恢复上下文）</li>
 *   <li>Null-safe: Yes (handles null context maps) - 空值安全: 是（处理 null 上下文映射）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see cloud.opencode.base.log.context.LogContext
 * @see cloud.opencode.base.log.context.MDC
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class VirtualThreadContext {

    private VirtualThreadContext() {
    }

    // ==================== Virtual Thread Creation | 虚拟线程创建 ====================

    /**
     * Creates and starts a Virtual Thread with propagated log context
     * 创建并启动带日志上下文传播的虚拟线程
     *
     * @param task the task to execute | 要执行的任务
     * @return the started virtual thread | 已启动的虚拟线程
     */
    public static Thread startVirtualThread(Runnable task) {
        return Thread.startVirtualThread(wrap(task));
    }

    /**
     * Creates a Virtual Thread builder with context propagation
     * 创建带上下文传播的虚拟线程构建器
     *
     * @param name the thread name | 线程名
     * @return thread builder | 线程构建器
     */
    public static Thread.Builder.OfVirtual virtualThreadBuilder(String name) {
        return Thread.ofVirtual().name(name);
    }

    // ==================== ExecutorService | 执行器服务 ====================

    /**
     * Creates a Virtual Thread ExecutorService with context propagation
     * 创建带上下文传播的虚拟线程执行器服务
     *
     * @return executor service | 执行器服务
     */
    public static ExecutorService newVirtualThreadExecutor() {
        return Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().factory()
        );
    }

    /**
     * Creates a named Virtual Thread ExecutorService with context propagation
     * 创建命名的带上下文传播的虚拟线程执行器服务
     *
     * @param namePrefix the thread name prefix | 线程名前缀
     * @return executor service | 执行器服务
     */
    public static ExecutorService newVirtualThreadExecutor(String namePrefix) {
        return Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name(namePrefix, 0).factory()
        );
    }

    // ==================== Wrapper Methods | 包装方法 ====================

    /**
     * Wraps a Runnable to propagate log context
     * 包装 Runnable 以传播日志上下文
     *
     * @param runnable the original runnable | 原始 runnable
     * @return wrapped runnable with context propagation | 带上下文传播的包装 runnable
     */
    public static Runnable wrap(Runnable runnable) {
        Map<String, String> capturedContext = MDC.getCopyOfContextMap();
        LogContext.ContextSnapshot snapshot = LogContext.snapshot();

        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                if (capturedContext != null) {
                    MDC.setContextMap(capturedContext);
                }
                LogContext.apply(snapshot);
                runnable.run();
            } finally {
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        };
    }

    /**
     * Wraps a Callable to propagate log context
     * 包装 Callable 以传播日志上下文
     *
     * @param callable the original callable | 原始 callable
     * @param <T>      the return type | 返回类型
     * @return wrapped callable with context propagation | 带上下文传播的包装 callable
     */
    public static <T> Callable<T> wrap(Callable<T> callable) {
        Map<String, String> capturedContext = MDC.getCopyOfContextMap();
        LogContext.ContextSnapshot snapshot = LogContext.snapshot();

        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                if (capturedContext != null) {
                    MDC.setContextMap(capturedContext);
                }
                LogContext.apply(snapshot);
                return callable.call();
            } finally {
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        };
    }

    /**
     * Wraps a Function to propagate log context
     * 包装 Function 以传播日志上下文
     *
     * @param function the original function | 原始 function
     * @param <T>      the input type | 输入类型
     * @param <R>      the return type | 返回类型
     * @return wrapped function with context propagation | 带上下文传播的包装 function
     */
    public static <T, R> Function<T, R> wrap(Function<T, R> function) {
        Map<String, String> capturedContext = MDC.getCopyOfContextMap();
        LogContext.ContextSnapshot snapshot = LogContext.snapshot();

        return input -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                if (capturedContext != null) {
                    MDC.setContextMap(capturedContext);
                }
                LogContext.apply(snapshot);
                return function.apply(input);
            } finally {
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        };
    }

    /**
     * Wraps a CompletableFuture to propagate log context to subsequent stages
     * 包装 CompletableFuture 以传播日志上下文到后续阶段
     *
     * @param future the original future | 原始 future
     * @param <T>    the result type | 结果类型
     * @return wrapped future with context propagation | 带上下文传播的包装 future
     */
    public static <T> CompletableFuture<T> wrap(CompletableFuture<T> future) {
        Map<String, String> capturedContext = MDC.getCopyOfContextMap();
        LogContext.ContextSnapshot snapshot = LogContext.snapshot();

        CompletableFuture<T> wrapped = new CompletableFuture<>();

        future.whenComplete((result, error) -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                if (capturedContext != null) {
                    MDC.setContextMap(capturedContext);
                }
                LogContext.apply(snapshot);

                if (error != null) {
                    wrapped.completeExceptionally(error);
                } else {
                    wrapped.complete(result);
                }
            } finally {
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        });

        return wrapped;
    }

    // ==================== Async Execution | 异步执行 ====================

    /**
     * Runs a task asynchronously on a Virtual Thread with context propagation
     * 在虚拟线程上异步运行任务并传播上下文
     *
     * @param task the task to run | 要运行的任务
     * @return CompletableFuture that completes when task finishes | 任务完成时的 CompletableFuture
     */
    public static CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(wrap(task), newVirtualThreadExecutor());
    }

    /**
     * Supplies a value asynchronously on a Virtual Thread with context propagation
     * 在虚拟线程上异步提供值并传播上下文
     *
     * @param supplier the supplier | 供应者
     * @param <T>      the result type | 结果类型
     * @return CompletableFuture with result | 包含结果的 CompletableFuture
     */
    public static <T> CompletableFuture<T> supplyAsync(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return wrap(supplier).call();
            } catch (Exception e) {
                throw new OpenLogException("Async supplier execution failed", e);
            }
        }, newVirtualThreadExecutor());
    }

    // ==================== Context Utilities | 上下文工具 ====================

    /**
     * Executes a task with specific context values
     * 使用特定上下文值执行任务
     *
     * @param traceId the trace ID | 追踪 ID
     * @param userId  the user ID | 用户 ID
     * @param task    the task | 任务
     */
    public static void runWithContext(String traceId, String userId, Runnable task) {
        LogContext.setTraceId(traceId);
        LogContext.setUserId(userId);
        try {
            task.run();
        } finally {
            LogContext.clear();
        }
    }

    /**
     * Executes a task with specific context values and returns result
     * 使用特定上下文值执行任务并返回结果
     *
     * @param traceId the trace ID | 追踪 ID
     * @param userId  the user ID | 用户 ID
     * @param task    the task | 任务
     * @param <T>     the result type | 结果类型
     * @return the result | 结果
     * @throws Exception if task fails | 如果任务失败
     */
    public static <T> T callWithContext(String traceId, String userId, Callable<T> task) throws Exception {
        LogContext.setTraceId(traceId);
        LogContext.setUserId(userId);
        try {
            return task.call();
        } finally {
            LogContext.clear();
        }
    }
}
