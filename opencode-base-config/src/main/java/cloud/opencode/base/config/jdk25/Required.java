package cloud.opencode.base.config.jdk25;

import java.lang.annotation.*;

/**
 * Required Configuration Annotation for Record Components
 * Record组件的必填配置注解
 *
 * <p>Marks a record component as required. Binding will fail if the
 * configuration value is missing.</p>
 * <p>标记record组件为必填。如果配置值缺失，绑定将失败。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * record DatabaseConfig(
 *     @Required String url,
 *     @Required String username,
 *     @Required String password,
 *     int poolSize  // optional
 * ) {}
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core Required functionality - Required核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
public @interface Required {
}
