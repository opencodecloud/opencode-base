package cloud.opencode.base.id;

import java.time.Instant;

/**
 * ID Parser Interface
 * ID解析器接口
 *
 * <p>Interface for parsing IDs to extract embedded information
 * such as timestamps, node IDs, and sequence numbers.</p>
 * <p>用于解析ID以提取嵌入信息（如时间戳、节点ID和序列号）的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse ID to structured result - 解析ID为结构化结果</li>
 *   <li>Extract timestamp - 提取时间戳</li>
 *   <li>Validate ID format - 验证ID格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SnowflakeIdParser parser = SnowflakeIdParser.createDefault();
 * var parsed = parser.parse(123456789L);
 * Instant time = parser.extractTimestamp(123456789L);
 * boolean valid = parser.isValid(123456789L);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @param <T> the type of ID to parse | 要解析的ID类型
 * @param <R> the type of parse result | 解析结果类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public interface IdParser<T, R> {

    /**
     * Parses an ID to extract embedded information
     * 解析ID以提取嵌入信息
     *
     * @param id the ID to parse | 要解析的ID
     * @return parse result | 解析结果
     */
    R parse(T id);

    /**
     * Extracts the timestamp from an ID
     * 从ID中提取时间戳
     *
     * @param id the ID | ID
     * @return timestamp | 时间戳
     */
    Instant extractTimestamp(T id);

    /**
     * Validates the ID format
     * 验证ID格式
     *
     * @param id the ID to validate | 要验证的ID
     * @return true if valid | 如果有效返回true
     */
    boolean isValid(T id);
}
