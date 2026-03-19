package cloud.opencode.base.web.spi;

import cloud.opencode.base.web.ResultCode;

import java.util.Collection;
import java.util.Optional;

/**
 * Result Code Provider SPI
 * 响应码提供者SPI
 *
 * <p>Service Provider Interface for custom result codes.</p>
 * <p>自定义响应码的服务提供者接口。</p>
 *
 * <p><strong>Usage | 使用方式:</strong></p>
 * <pre>{@code
 * // Implement custom provider
 * public class MyResultCodeProvider implements ResultCodeProvider {
 *     @Override
 *     public Collection<ResultCode> getResultCodes() {
 *         return Arrays.asList(MyResultCode.values());
 *     }
 * }
 *
 * // Register via META-INF/services
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI for custom result code registration - 自定义响应码注册SPI</li>
 *   <li>Code lookup by string - 按字符串查找响应码</li>
 *   <li>Priority ordering support - 优先级排序支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class MyProvider implements ResultCodeProvider {
 *     public Collection<ResultCode> getResultCodes() {
 *         return List.of(MyResultCode.values());
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 取决于实现</li>
 *   <li>Null-safe: No (code should not be null) - 否（代码不应为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public interface ResultCodeProvider {

    /**
     * Get all result codes
     * 获取所有响应码
     *
     * @return the result codes | 响应码集合
     */
    Collection<ResultCode> getResultCodes();

    /**
     * Get result code by code string
     * 通过代码字符串获取响应码
     *
     * @param code the code string | 代码字符串
     * @return the result code if found | 如果找到返回响应码
     */
    default Optional<ResultCode> getByCode(String code) {
        return getResultCodes().stream()
            .filter(rc -> rc.getCode().equals(code))
            .findFirst();
    }

    /**
     * Get the provider order
     * 获取提供者顺序
     *
     * <p>Lower values have higher priority.</p>
     * <p>值越小优先级越高。</p>
     *
     * @return the order | 顺序
     */
    default int getOrder() {
        return 0;
    }

    /**
     * Check if this provider supports the code
     * 检查此提供者是否支持该代码
     *
     * @param code the code to check | 要检查的代码
     * @return true if supported | 如果支持返回true
     */
    default boolean supports(String code) {
        return getByCode(code).isPresent();
    }
}
