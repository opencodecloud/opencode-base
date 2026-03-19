package cloud.opencode.base.deepclone.spi;

import cloud.opencode.base.deepclone.strategy.CloneStrategy;

import java.util.List;

/**
 * SPI interface for providing custom clone strategies
 * 提供自定义克隆策略的SPI接口
 *
 * <p>Implementations of this interface can be discovered via ServiceLoader
 * and provide additional cloning strategies beyond the built-in ones.</p>
 * <p>此接口的实现可以通过ServiceLoader发现，并提供内置策略之外的额外克隆策略。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class CustomStrategyProvider implements CloneStrategyProvider {
 *     @Override
 *     public List<CloneStrategy> getStrategies() {
 *         return List.of(new MyCustomStrategy());
 *     }
 *
 *     @Override
 *     public int priority() {
 *         return 50; // Higher priority than default
 *     }
 * }
 * }</pre>
 *
 * <p>To register the provider, create a file:
 * META-INF/services/cloud.opencode.base.deepclone.spi.CloneStrategyProvider</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ServiceLoader-based discovery - 基于ServiceLoader的发现</li>
 *   <li>Priority-based ordering - 基于优先级排序</li>
 *   <li>Custom strategy registration - 自定义策略注册</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public interface CloneStrategyProvider {

    /**
     * Gets the list of clone strategies provided by this provider
     * 获取此提供者提供的克隆策略列表
     *
     * @return the list of strategies | 策略列表
     */
    List<CloneStrategy> getStrategies();

    /**
     * Gets the priority of this provider
     * 获取此提供者的优先级
     *
     * <p>Lower values indicate higher priority. Default is 100.</p>
     * <p>较小的值表示较高的优先级。默认值为100。</p>
     *
     * @return the priority | 优先级
     */
    default int priority() {
        return 100;
    }
}
