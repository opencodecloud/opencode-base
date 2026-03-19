package cloud.opencode.base.geo.distance;

import cloud.opencode.base.geo.Coordinate;

/**
 * Distance Calculator Interface
 * 距离计算器接口
 *
 * <p>Interface for calculating distance between two coordinates.</p>
 * <p>用于计算两个坐标之间距离的接口。</p>
 *
 * <p><strong>Implementations | 实现:</strong></p>
 * <ul>
 *   <li>{@link HaversineCalculator} - Fast, good for most cases | 快速，适合大多数场景</li>
 *   <li>{@link VincentyCalculator} - Precise, for surveying | 精确，适合测绘</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Distance calculation between two coordinates - 计算两个坐标之间的距离</li>
 *   <li>Multiple accuracy implementations - 多种精度实现</li>
 *   <li>Functional interface for lambda usage - 函数式接口支持lambda使用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DistanceCalculator calc = HaversineCalculator.INSTANCE;
 * double meters = calc.calculate(coord1, coord2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementation-dependent - 空值安全: 取决于实现</li>
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
@FunctionalInterface
public interface DistanceCalculator {

    /**
     * Calculate distance between two coordinates
     * 计算两个坐标之间的距离
     *
     * @param c1 the first coordinate | 第一个坐标
     * @param c2 the second coordinate | 第二个坐标
     * @return distance in meters | 距离（米）
     */
    double calculate(Coordinate c1, Coordinate c2);
}
