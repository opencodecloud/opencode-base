/**
 * OpenCode Base Parallel Module
 * OpenCode 基础并行模块
 *
 * <p>Provides parallel computing capabilities based on JDK 25 features
 * including Virtual Threads, Structured Concurrency, and Scoped Values.</p>
 * <p>提供基于 JDK 25 特性的并行计算能力，包括虚拟线程、结构化并发和作用域值。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Virtual Threads - 虚拟线程</li>
 *   <li>Structured Concurrency (JEP 499) - 结构化并发</li>
 *   <li>Scoped Values (JEP 501) - 作用域值</li>
 *   <li>Parallel Execution (runAll, invokeAll, invokeAny) - 并行执行</li>
 *   <li>Batch Processing (parallelMap, processBatch) - 批量处理</li>
 *   <li>Async Pipeline - 异步流水线</li>
 *   <li>Concurrency Control (Semaphore throttling) - 并发控制</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
module cloud.opencode.base.parallel {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.parallel;
    exports cloud.opencode.base.parallel.batch;
    exports cloud.opencode.base.parallel.exception;
    exports cloud.opencode.base.parallel.executor;
    exports cloud.opencode.base.parallel.pipeline;
    exports cloud.opencode.base.parallel.structured;
    exports cloud.opencode.base.parallel.deadline;
}
