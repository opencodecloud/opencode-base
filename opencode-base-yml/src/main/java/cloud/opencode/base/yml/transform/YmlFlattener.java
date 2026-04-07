package cloud.opencode.base.yml.transform;

import cloud.opencode.base.yml.exception.OpenYmlException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YAML Flattener - Flattens and unflattens nested YAML structures
 * YAML 扁平化工具 - 扁平化和还原嵌套 YAML 结构
 *
 * <p>This utility class converts between nested YAML map structures and flat
 * dot-notation key-value maps, and vice versa.</p>
 * <p>此工具类在嵌套 YAML 映射结构与扁平点号键值映射之间进行转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Flatten nested maps to dot-notation: {a:{b:1}} to {"a.b": 1} - 扁平化嵌套映射为点号表示</li>
 *   <li>Unflatten dot-notation back to nested maps - 还原点号表示为嵌套映射</li>
 *   <li>Array support: {items:[a,b]} to {"items[0]": "a", "items[1]": "b"} - 数组支持</li>
 *   <li>Custom separator support - 自定义分隔符支持</li>
 *   <li>Null values preserved - 保留空值</li>
 *   <li>Empty maps/lists become leaf values - 空映射/列表作为叶子值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Flatten
 * Map<String, Object> nested = Map.of("server", Map.of("port", 8080));
 * Map<String, Object> flat = YmlFlattener.flatten(nested);
 * // {"server.port": 8080}
 *
 * // Unflatten
 * Map<String, Object> restored = YmlFlattener.unflatten(flat);
 * // {server: {port: 8080}}
 *
 * // Custom separator
 * Map<String, Object> slashFlat = YmlFlattener.flatten(nested, "/");
 * // {"server/port": 8080}
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Depth-limited: Yes (max 50 levels) - 深度限制: 是（最多 50 层）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public final class YmlFlattener {

    /**
     * Default path separator.
     * 默认路径分隔符。
     */
    private static final String DEFAULT_SEPARATOR = ".";

    /**
     * Maximum recursion depth to prevent stack overflow.
     * 最大递归深度，防止栈溢出。
     */
    private static final int MAX_DEPTH = 50;

    /**
     * Pattern to match array index segments like {@code [0]}.
     * 匹配数组索引段的正则，如 {@code [0]}。
     */
    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("\\[(\\d+)]");

    private YmlFlattener() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Flattens a nested map to dot-notation using the default separator.
     * 使用默认分隔符将嵌套映射扁平化为点号表示。
     *
     * @param data the nested map (may be null, treated as empty) | 嵌套映射（可为 null，视为空）
     * @return unmodifiable flat map | 不可修改的扁平映射
     * @throws OpenYmlException if nesting depth exceeds limit | 当嵌套深度超过限制时
     */
    public static Map<String, Object> flatten(Map<String, Object> data) {
        return flatten(data, DEFAULT_SEPARATOR);
    }

    /**
     * Flattens a nested map to flat notation using the specified separator.
     * 使用指定分隔符将嵌套映射扁平化。
     *
     * @param data      the nested map (may be null, treated as empty) | 嵌套映射（可为 null，视为空）
     * @param separator the path separator | 路径分隔符
     * @return unmodifiable flat map | 不可修改的扁平映射
     * @throws NullPointerException if separator is null | 当分隔符为 null 时
     * @throws OpenYmlException     if nesting depth exceeds limit | 当嵌套深度超过限制时
     */
    public static Map<String, Object> flatten(Map<String, Object> data, String separator) {
        Objects.requireNonNull(separator, "separator must not be null");
        if (data == null || data.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        doFlatten(data, "", separator, result, 0);
        return Collections.unmodifiableMap(result);
    }

    /**
     * Unflattens a flat dot-notation map back to nested structure using the default separator.
     * 使用默认分隔符将扁平点号映射还原为嵌套结构。
     *
     * @param flat the flat map (may be null, treated as empty) | 扁平映射（可为 null，视为空）
     * @return unmodifiable nested map | 不可修改的嵌套映射
     */
    public static Map<String, Object> unflatten(Map<String, Object> flat) {
        return unflatten(flat, DEFAULT_SEPARATOR);
    }

    /**
     * Unflattens a flat map back to nested structure using the specified separator.
     * 使用指定分隔符将扁平映射还原为嵌套结构。
     *
     * @param flat      the flat map (may be null, treated as empty) | 扁平映射（可为 null，视为空）
     * @param separator the path separator | 路径分隔符
     * @return unmodifiable nested map | 不可修改的嵌套映射
     * @throws NullPointerException if separator is null | 当分隔符为 null 时
     */
    public static Map<String, Object> unflatten(Map<String, Object> flat, String separator) {
        Objects.requireNonNull(separator, "separator must not be null");
        if (flat == null || flat.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : flat.entrySet()) {
            setNestedValue(result, entry.getKey(), entry.getValue(), separator);
        }
        return Collections.unmodifiableMap(convertListMaps(result));
    }

    /**
     * Recursively flattens nested structures.
     * 递归扁平化嵌套结构。
     */
    @SuppressWarnings("unchecked")
    private static void doFlatten(Object value, String prefix, String separator,
                                  Map<String, Object> result, int depth) {
        if (depth > MAX_DEPTH) {
            throw new OpenYmlException(
                    "YAML flatten exceeded maximum depth of " + MAX_DEPTH
                            + "; possible cyclic structure");
        }

        if (value instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                // Empty map is a leaf value
                if (!prefix.isEmpty()) {
                    result.put(prefix, Collections.emptyMap());
                }
                return;
            }
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String path = prefix.isEmpty() ? key : prefix + separator + key;
                doFlatten(entry.getValue(), path, separator, result, depth + 1);
            }
        } else if (value instanceof List<?> list) {
            if (list.isEmpty()) {
                // Empty list is a leaf value
                if (!prefix.isEmpty()) {
                    result.put(prefix, Collections.emptyList());
                }
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                String path = prefix + "[" + i + "]";
                doFlatten(list.get(i), path, separator, result, depth + 1);
            }
        } else {
            // Scalar or null
            result.put(prefix, value);
        }
    }

    /**
     * Sets a value at a nested path in the result map, creating intermediate maps as needed.
     * 在结果映射中的嵌套路径设置值，按需创建中间映射。
     */
    @SuppressWarnings("unchecked")
    private static void setNestedValue(Map<String, Object> root, String flatKey,
                                       Object value, String separator) {
        // Split by separator, but also handle array indices
        List<String> segments = splitKey(flatKey, separator);

        Object current = root;
        for (int i = 0; i < segments.size() - 1; i++) {
            String segment = segments.get(i);
            String nextSegment = segments.get(i + 1);
            boolean nextIsIndex = isArrayIndex(nextSegment);

            if (isArrayIndex(segment)) {
                int index = parseIndex(segment);
                List<Object> list = (List<Object>) current;
                ensureListSize(list, index + 1);
                Object next = list.get(index);
                if (next == null) {
                    next = nextIsIndex ? new ArrayList<>() : new LinkedHashMap<String, Object>();
                    list.set(index, next);
                }
                current = next;
            } else {
                Map<String, Object> map = (Map<String, Object>) current;
                Object next = map.get(segment);
                if (next == null) {
                    next = nextIsIndex ? new ArrayList<>() : new LinkedHashMap<String, Object>();
                    map.put(segment, next);
                }
                current = next;
            }
        }

        // Set the final value
        String lastSegment = segments.getLast();
        if (isArrayIndex(lastSegment)) {
            int index = parseIndex(lastSegment);
            List<Object> list = (List<Object>) current;
            ensureListSize(list, index + 1);
            list.set(index, value);
        } else {
            Map<String, Object> map = (Map<String, Object>) current;
            map.put(lastSegment, value);
        }
    }

    /**
     * Splits a flat key into path segments, separating array indices.
     * 将扁平键拆分为路径段，分离数组索引。
     */
    private static List<String> splitKey(String key, String separator) {
        List<String> segments = new ArrayList<>();
        // First split by separator
        String[] parts = key.split(Pattern.quote(separator), -1);
        for (String part : parts) {
            // Check if part contains array indices like "items[0]" or "[0]"
            int bracketPos = part.indexOf('[');
            if (bracketPos < 0) {
                segments.add(part);
            } else {
                // Add the part before the bracket (if any)
                if (bracketPos > 0) {
                    segments.add(part.substring(0, bracketPos));
                }
                // Extract all [N] indices
                Matcher matcher = ARRAY_INDEX_PATTERN.matcher(part.substring(bracketPos));
                while (matcher.find()) {
                    segments.add("[" + matcher.group(1) + "]");
                }
            }
        }
        return segments;
    }

    /**
     * Checks if a segment represents an array index like [0].
     * 检查段是否表示数组索引，如 [0]。
     */
    private static boolean isArrayIndex(String segment) {
        return segment.startsWith("[") && segment.endsWith("]");
    }

    /**
     * Parses the numeric index from an array segment.
     * 从数组段解析数值索引。
     */
    private static final int MAX_ARRAY_INDEX = 65536;

    private static int parseIndex(String segment) {
        int index = Integer.parseInt(segment.substring(1, segment.length() - 1));
        if (index < 0 || index > MAX_ARRAY_INDEX) {
            throw new cloud.opencode.base.yml.exception.OpenYmlException(
                    "Array index out of bounds in flat key: " + index
                    + " (max " + MAX_ARRAY_INDEX + ")");
        }
        return index;
    }

    /**
     * Ensures a list has at least the specified size, filling with nulls.
     * 确保列表至少达到指定大小，用 null 填充。
     */
    private static void ensureListSize(List<Object> list, int requiredSize) {
        while (list.size() < requiredSize) {
            list.add(null);
        }
    }

    /**
     * Recursively converts maps that represent lists (keys are all numeric indices)
     * back to actual lists. This handles nested list structures produced during unflatten.
     * 递归将表示列表的映射（键全为数值索引）转换回实际列表。
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertListMaps(Map<String, Object> map) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> childMap) {
                result.put(entry.getKey(), convertListMaps((Map<String, Object>) childMap));
            } else if (value instanceof List<?> list) {
                result.put(entry.getKey(), convertListElements(list));
            } else {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    /**
     * Recursively processes list elements, converting nested maps and lists.
     * 递归处理列表元素，转换嵌套映射和列表。
     */
    @SuppressWarnings("unchecked")
    private static List<Object> convertListElements(List<?> list) {
        List<Object> result = new ArrayList<>(list.size());
        for (Object item : list) {
            if (item instanceof Map<?, ?> childMap) {
                result.add(convertListMaps((Map<String, Object>) childMap));
            } else if (item instanceof List<?> childList) {
                result.add(convertListElements(childList));
            } else {
                result.add(item);
            }
        }
        return result;
    }
}
