package cloud.opencode.base.neural.internal;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.IntConsumer;

/**
 * Parallel Execution Engine
 * 并行执行引擎
 *
 * <p>Provides parallel loop execution for compute-intensive neural network operations.
 * Uses ForkJoinPool.commonPool() for work-stealing parallelism when the workload
 * exceeds a configurable threshold.</p>
 * <p>为计算密集型神经网络操作提供并行循环执行。当工作量超过可配置的阈值时，
 * 使用 ForkJoinPool.commonPool() 进行工作窃取并行化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Adaptive parallelization based on element count - 基于元素数量的自适应并行化</li>
 *   <li>Fork-join work stealing for balanced load distribution - Fork-Join工作窃取实现负载均衡</li>
 *   <li>Zero-overhead for small workloads (sequential fallback) - 小工作量零开销（回退顺序执行）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Threshold: 65536 elements before parallelization - 阈值: 65536个元素后并行化</li>
 *   <li>Granularity: 1024 elements per leaf task - 粒度: 每个叶任务1024个元素</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class ParallelEngine {

    private ParallelEngine() {}

    /**
     * Threshold for parallel execution.
     * 并行执行的阈值。
     *
     * <p>If element count exceeds this value, work is distributed across threads.</p>
     * <p>如果元素数量超过此值，工作将分配到多个线程。</p>
     */
    private static final int PARALLEL_THRESHOLD = 65_536;

    /**
     * Minimum elements per leaf task in the fork-join tree.
     * Fork-Join 树中每个叶任务的最小元素数。
     */
    private static final int GRAIN_SIZE = 1024;

    /**
     * Execute a parallel for loop over the range [start, end).
     * 在 [start, end) 范围上执行并行 for 循环。
     *
     * <p>If the range size exceeds {@link #PARALLEL_THRESHOLD}, the work is split
     * across threads using {@link ForkJoinPool#commonPool()}. Otherwise, the loop
     * runs sequentially on the calling thread.</p>
     * <p>如果范围大小超过 {@link #PARALLEL_THRESHOLD}，工作将通过
     * {@link ForkJoinPool#commonPool()} 分配到多个线程。否则，循环在调用线程上顺序执行。</p>
     *
     * @param start inclusive start index | 起始索引（包含）
     * @param end   exclusive end index | 结束索引（不包含）
     * @param body  the loop body to execute for each index | 每个索引要执行的循环体
     * @throws NullPointerException if body is null | 如果 body 为 null
     */
    public static void parallelFor(int start, int end, IntConsumer body) {
        java.util.Objects.requireNonNull(body, "body must not be null");
        if (start >= end) {
            return;
        }
        int count = end - start;
        if (count <= PARALLEL_THRESHOLD) {
            for (int i = start; i < end; i++) {
                body.accept(i);
            }
        } else {
            ForkJoinTask<?> task = new ParallelForAction(start, end, body);
            if (ForkJoinTask.inForkJoinPool()) {
                task.invoke();
            } else {
                ForkJoinPool.commonPool().invoke(task);
            }
        }
    }

    /**
     * Check if parallelization is worthwhile for the given element count.
     * 检查给定元素数量是否值得并行化。
     *
     * @param elementCount the number of elements to process | 要处理的元素数量
     * @return true if parallelization should be used | 如果应该使用并行化则返回 true
     */
    public static boolean shouldParallelize(int elementCount) {
        return elementCount > PARALLEL_THRESHOLD;
    }

    /**
     * Recursive fork-join action for parallel for loops.
     * 并行 for 循环的递归 Fork-Join 动作。
     */
    private static final class ParallelForAction extends RecursiveAction {

        private final int start;
        private final int end;
        private final IntConsumer body;

        ParallelForAction(int start, int end, IntConsumer body) {
            this.start = start;
            this.end = end;
            this.body = body;
        }

        @Override
        protected void compute() {
            int count = end - start;
            if (count <= GRAIN_SIZE) {
                for (int i = start; i < end; i++) {
                    body.accept(i);
                }
            } else {
                int mid = start + (count >>> 1);
                ParallelForAction left = new ParallelForAction(start, mid, body);
                ParallelForAction right = new ParallelForAction(mid, end, body);
                invokeAll(left, right);
            }
        }
    }
}
