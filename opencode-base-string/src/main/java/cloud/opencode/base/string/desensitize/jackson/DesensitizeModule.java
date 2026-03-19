package cloud.opencode.base.string.desensitize.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Desensitize Module - Jackson module for desensitization support.
 * 脱敏模块 - 用于脱敏支持的Jackson模块。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Jackson serialization integration - Jackson序列化集成</li>
 *   <li>Automatic annotation-based masking during JSON output - JSON输出时自动注解脱敏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ObjectMapper mapper = new ObjectMapper();
 * mapper.registerModule(new DesensitizeModule());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (after registration) - 线程安全: 是（注册后）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public class DesensitizeModule extends SimpleModule {
    
    public DesensitizeModule() {
        super("DesensitizeModule");
        addSerializer(String.class, new DesensitizeSerializer());
    }
}
