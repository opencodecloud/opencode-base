package cloud.opencode.base.config.converter.impl;

import cloud.opencode.base.config.converter.ConfigConverter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Collection Converters
 * 集合转换器
 *
 * <p>Provides converters for collection types using comma-separated format.</p>
 * <p>提供使用逗号分隔格式的集合类型转换器。</p>
 *
 * <p><strong>Format | 格式:</strong></p>
 * <pre>
 * List: "a,b,c" → List.of("a", "b", "c")
 * Set:  "a,b,c" → Set.of("a", "b", "c")
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * var listConverter = new CollectionConverters.StringListConverter();
 * List<String> hosts = listConverter.convert("host1,host2,host3");
 *
 * var setConverter = new CollectionConverters.StringSetConverter();
 * Set<String> tags = setConverter.convert("tag1,tag2,tag3");
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core CollectionConverters functionality - CollectionConverters核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = string length - O(n), n为字符串长度</li>
 *   <li>Space complexity: O(n) for result collection - 结果集合 O(n)</li>
 * </ul>
 *
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class CollectionConverters {
    
    public static class StringListConverter implements ConfigConverter<List<String>> {
        @Override
        public List<String> convert(String value) {
            return Arrays.stream(value.split(","))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableList());
        }
    }

    public static class StringSetConverter implements ConfigConverter<Set<String>> {
        @Override
        public Set<String> convert(String value) {
            return Arrays.stream(value.split(","))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
        }
    }
}
