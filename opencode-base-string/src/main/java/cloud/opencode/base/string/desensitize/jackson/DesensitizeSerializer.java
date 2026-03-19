package cloud.opencode.base.string.desensitize.jackson;

import cloud.opencode.base.string.desensitize.OpenMask;
import cloud.opencode.base.string.desensitize.annotation.Desensitize;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/**
 * Desensitize Serializer - Jackson serializer for automatic field desensitization.
 * 脱敏序列化器 - 用于自动字段脱敏的Jackson序列化器。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Annotation-aware serialization - 注解感知序列化</li>
 *   <li>Automatic masking based on {@link Desensitize} annotation - 基于注解自动脱敏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Automatically used when DesensitizeModule is registered
 * ObjectMapper mapper = new ObjectMapper();
 * mapper.registerModule(new DesensitizeModule());
 * String json = mapper.writeValueAsString(userVO); // masked fields in JSON
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless serializer) - 线程安全: 是（无状态序列化器）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) per serialization where n is the string length - 时间复杂度: 每次序列化 O(n)，n为字符串长度</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public class DesensitizeSerializer extends StdSerializer<String> {
    
    public DesensitizeSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        BeanProperty property = (BeanProperty) provider.getAttribute("currentProperty");
        if (property != null) {
            Desensitize anno = property.getAnnotation(Desensitize.class);
            if (anno != null) {
                String desensitized = OpenMask.desensitize(value, anno.value());
                gen.writeString(desensitized);
                return;
            }
        }
        gen.writeString(value);
    }
}
