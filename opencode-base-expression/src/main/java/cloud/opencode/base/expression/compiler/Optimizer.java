package cloud.opencode.base.expression.compiler;

import cloud.opencode.base.expression.ast.*;
import cloud.opencode.base.expression.eval.OperatorEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * AST Optimizer
 * AST 优化器
 *
 * <p>Optimizes Abstract Syntax Trees to improve evaluation performance.
 * Applies various optimization techniques including constant folding and
 * short-circuit optimization.</p>
 * <p>优化抽象语法树以提高求值性能。应用各种优化技术，包括常量折叠和短路优化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Constant folding: evaluate constant expressions at compile time - 常量折叠: 编译时计算常量表达式</li>
 *   <li>Short-circuit optimization for logical operators - 逻辑运算符的短路优化</li>
 *   <li>Dead code elimination - 死代码消除</li>
 *   <li>Configurable optimization passes - 可配置的优化传递</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Optimizer optimizer = Optimizer.builder()
 *     .constantFolding(true)
 *     .shortCircuit(true)
 *     .build();
 * Node optimized = optimizer.optimize(ast);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No, mutable configuration state - 线程安全: 否，可变配置状态</li>
 *   <li>Null-safe: Yes, null node returns null - 空值安全: 是，null节点返回null</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for optimize where n is the number of AST nodes - 时间复杂度: optimize 为 O(n)，n为 AST 节点数量</li>
 *   <li>Space complexity: O(n) for the optimized AST - 空间复杂度: O(n)，存储优化后的 AST</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class Optimizer {

    private boolean constantFoldingEnabled = true;
    private boolean shortCircuitEnabled = true;
    private boolean deadCodeEliminationEnabled = true;

    /**
     * Create optimizer with default settings
     * 使用默认设置创建优化器
     */
    public Optimizer() {
    }

    /**
     * Optimize an AST node
     * 优化 AST 节点
     *
     * @param node the node to optimize | 要优化的节点
     * @return the optimized node | 优化后的节点
     */
    public Node optimize(Node node) {
        if (node == null) {
            return null;
        }

        Node optimized = node;

        // Apply constant folding
        if (constantFoldingEnabled) {
            optimized = foldConstants(optimized);
        }

        // Apply short-circuit optimization
        if (shortCircuitEnabled) {
            optimized = optimizeShortCircuit(optimized);
        }

        return optimized;
    }

    /**
     * Constant Folding
     * 常量折叠
     *
     * <p>Evaluates constant expressions at compile time.</p>
     * <p>在编译时计算常量表达式。</p>
     *
     * @param node the node | 节点
     * @return the optimized node | 优化后的节点
     */
    public Node foldConstants(Node node) {
        if (node == null) {
            return null;
        }

        // Handle binary operations
        if (node instanceof BinaryOpNode bin) {
            Node left = foldConstants(bin.left());
            Node right = foldConstants(bin.right());

            // If both are literals, evaluate at compile time
            if (left instanceof LiteralNode l && right instanceof LiteralNode r) {
                try {
                    Object result = OperatorEvaluator.evaluateBinary(
                            bin.operator(), l.value(), r.value());
                    return LiteralNode.of(result);
                } catch (Exception e) {
                    // If evaluation fails, return the original with optimized children
                    return BinaryOpNode.of(left, bin.operator(), right);
                }
            }

            // Return with optimized children
            if (left != bin.left() || right != bin.right()) {
                return BinaryOpNode.of(left, bin.operator(), right);
            }
        }

        // Handle unary operations
        if (node instanceof UnaryOpNode unary) {
            Node operand = foldConstants(unary.operand());

            // If operand is literal, evaluate at compile time
            if (operand instanceof LiteralNode l) {
                try {
                    Object result = OperatorEvaluator.evaluateUnary(
                            unary.operator(), l.value());
                    return LiteralNode.of(result);
                } catch (Exception e) {
                    return UnaryOpNode.of(unary.operator(), operand);
                }
            }

            if (operand != unary.operand()) {
                return UnaryOpNode.of(unary.operator(), operand);
            }
        }

        // Handle ternary operations
        if (node instanceof TernaryOpNode ternary) {
            Node condition = foldConstants(ternary.condition());
            Node trueExpr = foldConstants(ternary.trueValue());
            Node falseExpr = foldConstants(ternary.falseValue());

            // If condition is a boolean literal, return the appropriate branch
            if (condition instanceof LiteralNode l) {
                Object value = l.value();
                if (Boolean.TRUE.equals(value)) {
                    return trueExpr;
                } else if (Boolean.FALSE.equals(value)) {
                    return falseExpr;
                }
            }

            if (condition != ternary.condition() ||
                trueExpr != ternary.trueValue() ||
                falseExpr != ternary.falseValue()) {
                return TernaryOpNode.of(condition, trueExpr, falseExpr);
            }
        }

        // Handle list literals
        if (node instanceof ListLiteralNode list) {
            List<Node> optimizedElements = new ArrayList<>();
            boolean changed = false;
            for (Node element : list.elements()) {
                Node optimized = foldConstants(element);
                optimizedElements.add(optimized);
                if (optimized != element) {
                    changed = true;
                }
            }
            if (changed) {
                return ListLiteralNode.of(optimizedElements);
            }
        }

        return node;
    }

    /**
     * Short-Circuit Optimization
     * 短路优化
     *
     * <p>Optimizes logical operations that can be determined from the left operand.</p>
     * <p>优化可以从左操作数确定结果的逻辑运算。</p>
     *
     * @param node the node | 节点
     * @return the optimized node | 优化后的节点
     */
    public Node optimizeShortCircuit(Node node) {
        if (node == null) {
            return null;
        }

        if (node instanceof BinaryOpNode bin) {
            String op = bin.operator();
            Node left = optimizeShortCircuit(bin.left());

            // Logical AND short-circuit
            if ("&&".equals(op) || "and".equals(op)) {
                if (left instanceof LiteralNode l) {
                    if (Boolean.FALSE.equals(l.value())) {
                        return LiteralNode.of(false);
                    }
                    if (Boolean.TRUE.equals(l.value())) {
                        return optimizeShortCircuit(bin.right());
                    }
                }
            }

            // Logical OR short-circuit
            if ("||".equals(op) || "or".equals(op)) {
                if (left instanceof LiteralNode l) {
                    if (Boolean.TRUE.equals(l.value())) {
                        return LiteralNode.of(true);
                    }
                    if (Boolean.FALSE.equals(l.value())) {
                        return optimizeShortCircuit(bin.right());
                    }
                }
            }

            Node right = optimizeShortCircuit(bin.right());
            if (left != bin.left() || right != bin.right()) {
                return BinaryOpNode.of(left, op, right);
            }
        }

        return node;
    }

    /**
     * Check if constant folding is enabled
     * 检查是否启用常量折叠
     *
     * @return true if enabled | 如果启用返回 true
     */
    public boolean isConstantFoldingEnabled() {
        return constantFoldingEnabled;
    }

    /**
     * Set constant folding enabled
     * 设置是否启用常量折叠
     *
     * @param enabled true to enable | true 表示启用
     * @return this optimizer for chaining | 用于链式调用的优化器
     */
    public Optimizer setConstantFoldingEnabled(boolean enabled) {
        this.constantFoldingEnabled = enabled;
        return this;
    }

    /**
     * Check if short-circuit optimization is enabled
     * 检查是否启用短路优化
     *
     * @return true if enabled | 如果启用返回 true
     */
    public boolean isShortCircuitEnabled() {
        return shortCircuitEnabled;
    }

    /**
     * Set short-circuit optimization enabled
     * 设置是否启用短路优化
     *
     * @param enabled true to enable | true 表示启用
     * @return this optimizer for chaining | 用于链式调用的优化器
     */
    public Optimizer setShortCircuitEnabled(boolean enabled) {
        this.shortCircuitEnabled = enabled;
        return this;
    }

    /**
     * Check if dead code elimination is enabled
     * 检查是否启用死代码消除
     *
     * @return true if enabled | 如果启用返回 true
     */
    public boolean isDeadCodeEliminationEnabled() {
        return deadCodeEliminationEnabled;
    }

    /**
     * Set dead code elimination enabled
     * 设置是否启用死代码消除
     *
     * @param enabled true to enable | true 表示启用
     * @return this optimizer for chaining | 用于链式调用的优化器
     */
    public Optimizer setDeadCodeEliminationEnabled(boolean enabled) {
        this.deadCodeEliminationEnabled = enabled;
        return this;
    }

    /**
     * Create builder for Optimizer
     * 创建优化器构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for Optimizer
     * 优化器构建器
     */
    public static class Builder {
        private boolean constantFolding = true;
        private boolean shortCircuit = true;
        private boolean deadCodeElimination = true;

        /**
         * Enable or disable constant folding
         * 启用或禁用常量折叠
         *
         * @param enabled true to enable | true 表示启用
         * @return this builder | 此构建器
         */
        public Builder constantFolding(boolean enabled) {
            this.constantFolding = enabled;
            return this;
        }

        /**
         * Enable or disable short-circuit optimization
         * 启用或禁用短路优化
         *
         * @param enabled true to enable | true 表示启用
         * @return this builder | 此构建器
         */
        public Builder shortCircuit(boolean enabled) {
            this.shortCircuit = enabled;
            return this;
        }

        /**
         * Enable or disable dead code elimination
         * 启用或禁用死代码消除
         *
         * @param enabled true to enable | true 表示启用
         * @return this builder | 此构建器
         */
        public Builder deadCodeElimination(boolean enabled) {
            this.deadCodeElimination = enabled;
            return this;
        }

        /**
         * Disable all optimizations
         * 禁用所有优化
         *
         * @return this builder | 此构建器
         */
        public Builder noOptimizations() {
            this.constantFolding = false;
            this.shortCircuit = false;
            this.deadCodeElimination = false;
            return this;
        }

        /**
         * Build the optimizer
         * 构建优化器
         *
         * @return the optimizer | 优化器
         */
        public Optimizer build() {
            Optimizer optimizer = new Optimizer();
            optimizer.setConstantFoldingEnabled(constantFolding);
            optimizer.setShortCircuitEnabled(shortCircuit);
            optimizer.setDeadCodeEliminationEnabled(deadCodeElimination);
            return optimizer;
        }
    }
}
