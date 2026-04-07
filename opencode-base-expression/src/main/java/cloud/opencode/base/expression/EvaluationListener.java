package cloud.opencode.base.expression;

import cloud.opencode.base.expression.ast.Node;
import cloud.opencode.base.expression.context.EvaluationContext;

import java.util.Objects;

/**
 * Evaluation Listener Interface
 * 求值监听器接口
 *
 * <p>Interface for monitoring and observing expression evaluation. Listeners receive
 * callbacks before and after each node evaluation, and on errors. This enables
 * debugging, profiling, logging, and custom instrumentation of the expression engine.</p>
 * <p>用于监控和观察表达式求值的接口。监听器在每个节点求值前后以及出错时接收回调。
 * 这支持表达式引擎的调试、性能分析、日志记录和自定义检测。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Before/after evaluation hooks for each AST node - 每个AST节点的求值前/后钩子</li>
 *   <li>Error notification with full context - 带完整上下文的错误通知</li>
 *   <li>Default no-op implementations for all methods - 所有方法的默认空操作实现</li>
 *   <li>Static factory for no-op listener - 空操作监听器的静态工厂</li>
 *   <li>Static factory for composite listener - 组合监听器的静态工厂</li>
 *   <li>Exception isolation in composite listener - 组合监听器中的异常隔离</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a logging listener
 * EvaluationListener logger = new EvaluationListener() {
 *     @Override
 *     public void beforeEvaluate(Node node, EvaluationContext context) {
 *         System.out.println("Evaluating: " + node.toExpressionString());
 *     }
 *
 *     @Override
 *     public void afterEvaluate(Node node, EvaluationContext context, Object result) {
 *         System.out.println("Result: " + result);
 *     }
 *
 *     @Override
 *     public void onError(Node node, EvaluationContext context, Exception error) {
 *         System.err.println("Error evaluating " + node.toExpressionString()
 *             + ": " + error.getMessage());
 *     }
 * };
 *
 * // Create a timing listener (only override afterEvaluate)
 * EvaluationListener timer = new EvaluationListener() {
 *     private final Map<String, Long> startTimes = new ConcurrentHashMap<>();
 *
 *     @Override
 *     public void beforeEvaluate(Node node, EvaluationContext context) {
 *         startTimes.put(node.toExpressionString(), System.nanoTime());
 *     }
 *
 *     @Override
 *     public void afterEvaluate(Node node, EvaluationContext context, Object result) {
 *         long elapsed = System.nanoTime() - startTimes.remove(node.toExpressionString());
 *         System.out.println(node.toExpressionString() + " took " + elapsed + "ns");
 *     }
 * };
 *
 * // Combine multiple listeners
 * EvaluationListener combined = EvaluationListener.composite(logger, timer);
 *
 * // Use no-op listener as default
 * EvaluationListener noop = EvaluationListener.noOp();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: The interface itself is stateless; thread safety depends on implementation -
 *       接口本身无状态；线程安全取决于实现</li>
 *   <li>Null-safe: Static factories reject null arguments; composite isolates listener exceptions -
 *       静态工厂拒绝null参数；组合监听器隔离监听器异常</li>
 * </ul>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>No-op listener has zero overhead - 空操作监听器零开销</li>
 *   <li>Composite listener iterates through delegates with O(n) per callback - 组合监听器每次回调O(n)遍历委托</li>
 *   <li>Exception isolation prevents one listener failure from affecting others - 异常隔离防止一个监听器失败影响其他监听器</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public interface EvaluationListener {

    /**
     * Called before a node is evaluated
     * 节点求值前调用
     *
     * @param node the node about to be evaluated | 即将被求值的节点
     * @param context the evaluation context | 求值上下文
     */
    default void beforeEvaluate(Node node, EvaluationContext context) {
        // no-op by default
    }

    /**
     * Called after a node is successfully evaluated
     * 节点成功求值后调用
     *
     * @param node the node that was evaluated | 已求值的节点
     * @param context the evaluation context | 求值上下文
     * @param result the evaluation result (may be null) | 求值结果（可能为null）
     */
    default void afterEvaluate(Node node, EvaluationContext context, Object result) {
        // no-op by default
    }

    /**
     * Called when an error occurs during node evaluation
     * 节点求值过程中发生错误时调用
     *
     * @param node the node that caused the error | 导致错误的节点
     * @param context the evaluation context | 求值上下文
     * @param error the exception that occurred | 发生的异常
     */
    default void onError(Node node, EvaluationContext context, Exception error) {
        // no-op by default
    }

    /**
     * Return a no-op listener that does nothing
     * 返回一个不执行任何操作的空操作监听器
     *
     * @return a no-op evaluation listener | 空操作求值监听器
     */
    static EvaluationListener noOp() {
        return NoOpListener.INSTANCE;
    }

    /**
     * Create a composite listener that delegates to multiple listeners
     * 创建一个委托给多个监听器的组合监听器
     *
     * <p>The composite listener invokes each delegate in order. If any delegate
     * throws an exception, it is caught and suppressed so that subsequent
     * delegates are still invoked.</p>
     * <p>组合监听器按顺序调用每个委托。如果任何委托抛出异常，
     * 异常将被捕获和抑制，以确保后续委托仍然被调用。</p>
     *
     * @param listeners the listeners to compose | 要组合的监听器
     * @return a composite listener | 组合监听器
     * @throws NullPointerException if listeners array or any element is null | 如果监听器数组或任何元素为null
     */
    static EvaluationListener composite(EvaluationListener... listeners) {
        Objects.requireNonNull(listeners, "listeners cannot be null");
        if (listeners.length == 0) {
            return noOp();
        }
        if (listeners.length == 1) {
            return Objects.requireNonNull(listeners[0], "listener cannot be null");
        }
        // Defensive copy to prevent external modification
        EvaluationListener[] copy = new EvaluationListener[listeners.length];
        for (int i = 0; i < listeners.length; i++) {
            copy[i] = Objects.requireNonNull(listeners[i], "listener[" + i + "] cannot be null");
        }
        return new CompositeListener(copy);
    }
}

/**
 * Singleton no-op listener implementation.
 * 单例空操作监听器实现。
 */
enum NoOpListener implements EvaluationListener {
    INSTANCE
}

/**
 * Composite listener that delegates to multiple listeners with exception isolation.
 * 带异常隔离的组合监听器，委托给多个监听器。
 */
final class CompositeListener implements EvaluationListener {

    private final EvaluationListener[] delegates;

    CompositeListener(EvaluationListener[] delegates) {
        this.delegates = delegates;
    }

    @Override
    public void beforeEvaluate(Node node, EvaluationContext context) {
        for (EvaluationListener delegate : delegates) {
            try {
                delegate.beforeEvaluate(node, context);
            } catch (Exception _) {
                // Isolate listener exceptions to prevent cascade failures
            }
        }
    }

    @Override
    public void afterEvaluate(Node node, EvaluationContext context, Object result) {
        for (EvaluationListener delegate : delegates) {
            try {
                delegate.afterEvaluate(node, context, result);
            } catch (Exception _) {
                // Isolate listener exceptions to prevent cascade failures
            }
        }
    }

    @Override
    public void onError(Node node, EvaluationContext context, Exception error) {
        for (EvaluationListener delegate : delegates) {
            try {
                delegate.onError(node, context, error);
            } catch (Exception _) {
                // Isolate listener exceptions to prevent cascade failures
            }
        }
    }
}
