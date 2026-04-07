package cloud.opencode.base.config.jdk25;

import java.lang.annotation.*;

/**
 * Default Value Annotation for Record Components
 * Record组件的默认值注解
 *
 * <p>Provides a default value for record component when configuration is missing.</p>
 * <p>当配置缺失时为record组件提供默认值。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * record ServerConfig(
 *     @DefaultValue("8080") int port,
 *     @DefaultValue("localhost") String host,
 *     @DefaultValue("30s") Duration timeout
 * ) {}
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core DefaultValue functionality - DefaultValue核心功能</li>
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
 * @deprecated Use {@link cloud.opencode.base.config.bind.DefaultValue} instead,
 *             which supports both record components and POJO fields.
 *             使用 {@link cloud.opencode.base.config.bind.DefaultValue} 替代，
 *             支持 record 组件和 POJO 字段。
 */
@Deprecated(since = "1.0.3")
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {
    String value();
}
