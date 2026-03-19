package cloud.opencode.base.geo.distance;

/**
 * Distance Calculator Factory
 * 距离计算器工厂
 *
 * <p>Factory for creating distance calculators.</p>
 * <p>用于创建距离计算器的工厂。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DistanceCalculator fast = DistanceCalculatorFactory.create(DistanceAccuracy.FAST);
 * DistanceCalculator precise = DistanceCalculatorFactory.create(DistanceAccuracy.PRECISE);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create Haversine calculator (fast) - 创建Haversine计算器（快速）</li>
 *   <li>Create Vincenty calculator (precise) - 创建Vincenty计算器（精确）</li>
 *   <li>Accuracy level selection - 精度级别选择</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless factory, returns singletons) - 线程安全: 是（无状态工厂，返回单例）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public final class DistanceCalculatorFactory {

    private DistanceCalculatorFactory() {
    }

    /**
     * Distance calculation accuracy level
     * 距离计算精度级别
     */
    public enum DistanceAccuracy {
        /** Fast calculation, suitable for most cases (~0.5% error) */
        FAST,
        /** Precise calculation, suitable for surveying (~0.5mm error) */
        PRECISE
    }

    /**
     * Create a distance calculator based on accuracy level
     * 根据精度级别创建距离计算器
     *
     * @param accuracy the accuracy level | 精度级别
     * @return distance calculator | 距离计算器
     */
    public static DistanceCalculator create(DistanceAccuracy accuracy) {
        return switch (accuracy) {
            case FAST -> HaversineCalculator.INSTANCE;
            case PRECISE -> VincentyCalculator.INSTANCE;
        };
    }

    /**
     * Get the default (fast) calculator
     * 获取默认（快速）计算器
     *
     * @return Haversine calculator | Haversine计算器
     */
    public static DistanceCalculator getDefault() {
        return HaversineCalculator.INSTANCE;
    }

    /**
     * Get the precise calculator
     * 获取精确计算器
     *
     * @return Vincenty calculator | Vincenty计算器
     */
    public static DistanceCalculator getPrecise() {
        return VincentyCalculator.INSTANCE;
    }
}
