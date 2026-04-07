package cloud.opencode.base.expression;

/**
 * Arithmetic Precision Mode
 * 算术精度模式
 *
 * <p>Defines the arithmetic precision mode used during expression evaluation.
 * Controls whether numeric operations use standard Java arithmetic or
 * {@link java.math.BigDecimal} for arbitrary-precision calculations.</p>
 * <p>定义表达式求值期间使用的算术精度模式。
 * 控制数值运算使用标准Java算术还是 {@link java.math.BigDecimal} 进行任意精度计算。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@link #STANDARD} - Default mode using int/long/double arithmetic for maximum performance -
 *       默认模式，使用int/long/double算术以获得最大性能</li>
 *   <li>{@link #BIG_DECIMAL} - Precision mode using BigDecimal for all numeric operations, ideal for
 *       financial calculations - 精确模式，所有数值运算使用BigDecimal，适用于金融计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Standard mode (default): fast int/long/double arithmetic
 * StandardContext ctx = StandardContext.builder()
 *     .arithmeticMode(ArithmeticMode.STANDARD)
 *     .build();
 * Object result = OpenExpression.evaluate("0.1 + 0.2", ctx);
 * // result = 0.30000000000000004 (double precision)
 *
 * // BigDecimal mode: exact arithmetic for financial use cases
 * StandardContext ctx = StandardContext.builder()
 *     .arithmeticMode(ArithmeticMode.BIG_DECIMAL)
 *     .build();
 * Object result = OpenExpression.evaluate("0.1 + 0.2", ctx);
 * // result = 0.3 (exact BigDecimal)
 *
 * // Check current mode
 * ArithmeticMode mode = ArithmeticMode.STANDARD;
 * if (mode == ArithmeticMode.BIG_DECIMAL) {
 *     // Use BigDecimal operations
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, enum constants are inherently immutable and thread-safe -
 *       线程安全: 是，枚举常量本质上是不可变和线程安全的</li>
 *   <li>Null-safe: Enum values cannot be null - 空值安全: 枚举值不能为null</li>
 * </ul>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>{@link #STANDARD}: Fastest, uses primitive arithmetic - 最快，使用原始算术</li>
 *   <li>{@link #BIG_DECIMAL}: Slower (3-10x) but exact - 较慢（3-10倍）但精确</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public enum ArithmeticMode {

    /**
     * Standard arithmetic mode
     * 标准算术模式
     *
     * <p>Uses Java's default int, long, and double arithmetic. Offers the best
     * performance but may exhibit floating-point rounding errors for decimal values.</p>
     * <p>使用Java默认的int、long和double算术。提供最佳性能，
     * 但对于十进制值可能出现浮点舍入误差。</p>
     */
    STANDARD,

    /**
     * BigDecimal arithmetic mode
     * BigDecimal算术模式
     *
     * <p>Promotes all numeric operands to {@link java.math.BigDecimal} before performing
     * arithmetic operations. Provides exact decimal arithmetic suitable for financial
     * and scientific calculations where precision is critical.</p>
     * <p>在执行算术运算前将所有数值操作数提升为 {@link java.math.BigDecimal}。
     * 提供精确的十进制算术，适用于精度至关重要的金融和科学计算。</p>
     */
    BIG_DECIMAL
}
