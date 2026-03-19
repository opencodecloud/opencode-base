package cloud.opencode.base.yml.bind;

import java.lang.annotation.*;

/**
 * YML Alias - Defines alternative property names for binding
 * YML 别名 - 定义绑定的替代属性名称
 *
 * <p>This annotation allows a field to be bound from multiple YAML property names.</p>
 * <p>此注解允许字段从多个 YAML 属性名称绑定。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple alternative property paths for a single field - 单个字段的多个替代属性路径</li>
 *   <li>Supports migration from legacy config keys - 支持从遗留配置键迁移</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class DatabaseConfig {
 *     @YmlAlias({"db.url", "database.url", "jdbc.url"})
 *     private String url;
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation is inherently immutable) - 线程安全: 是（注解本身不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YmlAlias {

    /**
     * The alternative property paths.
     * 替代属性路径。
     *
     * @return the alias paths | 别名路径
     */
    String[] value();
}
