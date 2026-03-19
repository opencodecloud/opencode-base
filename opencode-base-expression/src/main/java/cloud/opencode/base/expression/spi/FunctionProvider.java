package cloud.opencode.base.expression.spi;

import cloud.opencode.base.expression.function.Function;

import java.util.Map;

/**
 * Function Provider SPI
 * 函数提供者SPI
 *
 * <p>Provides a service provider interface for registering custom functions.</p>
 * <p>为注册自定义函数提供服务提供者接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI for pluggable function registration - 可插拔函数注册的SPI</li>
 *   <li>Priority-based registration ordering - 基于优先级的注册排序</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class MyFunctionProvider implements FunctionProvider {
 *     @Override
 *     public Map<String, Function> getFunctions() {
 *         return Map.of("myfunc", args -> "result");
 *     }
 *
 *     @Override
 *     public int getPriority() {
 *         return 10;  // Higher priority registered first
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public interface FunctionProvider {

    /**
     * Get the functions provided by this provider
     * 获取此提供者提供的函数
     *
     * @return the function map | 函数映射
     */
    Map<String, Function> getFunctions();

    /**
     * Get the priority of this provider
     * 获取此提供者的优先级
     *
     * <p>Higher priority providers are registered first.
     * Default priority is 0.</p>
     * <p>优先级更高的提供者先注册。默认优先级为0。</p>
     *
     * @return the priority | 优先级
     */
    default int getPriority() {
        return 0;
    }
}
