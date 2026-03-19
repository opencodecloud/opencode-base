package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.JsonNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * Built-in JSON Serializer - Zero-Dependency Object to JSON String Serializer
 * 内置JSON序列化器 - 零依赖的对象到JSON字符串序列化器
 *
 * <p>Converts Java objects to JSON strings. Supports primitives, Map, List, arrays,
 * JsonNode, Enum, java.time, UUID, and POJO (via {@link BeanMapper}).</p>
 * <p>将Java对象转换为JSON字符串。支持基本类型、Map、List、数组、
 * JsonNode、枚举、java.time、UUID和POJO（通过 {@link BeanMapper}）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 8259 compliant JSON output - 符合 RFC 8259 的JSON输出</li>
 *   <li>Pretty printing with configurable indentation - 可配置缩进的美化打印</li>
 *   <li>Proper string escaping (control chars, unicode) - 正确的字符串转义</li>
 *   <li>NaN/Infinity serialized as null - NaN/Infinity 序列化为null</li>
 *   <li>POJO support via BeanMapper reflection - 通过BeanMapper反射支持POJO</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (create new instance per serialization) - 线程安全: 否</li>
 *   <li>Nesting depth limited to 512 - 嵌套深度限制为512</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n=total size of the object graph being serialized - 时间复杂度: O(n)，n 为被序列化对象图的总大小</li>
 *   <li>Space complexity: O(d) for the call stack where d=nesting depth (max 512), plus O(n) for the output StringBuilder - 空间复杂度: 调用栈 O(d)，d 为嵌套深度（最大 512），加上输出 StringBuilder O(n)</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // See class-level documentation for usage
 * // 参见类级文档了解用法
 * }</pre>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
final class JsonSerializer {

    private static final int MAX_DEPTH = 512;

    private final StringBuilder sb;
    private final boolean prettyPrint;
    private int depth;

    JsonSerializer(boolean prettyPrint) {
        this.sb = new StringBuilder(256);
        this.prettyPrint = prettyPrint;
        this.depth = 0;
    }

    /**
     * Serializes an object to a JSON string
     * 将对象序列化为JSON字符串
     *
     * @param obj the object | 对象
     * @return the JSON string | JSON字符串
     */
    String serialize(Object obj) {
        writeValue(obj);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void writeValue(Object obj) {
        if (obj == null) {
            sb.append("null");
            return;
        }

        switch (obj) {
            case JsonNode node -> writeJsonNode(node);
            case String s -> writeString(s);
            case Boolean b -> sb.append(b);
            case Number n -> writeNumber(n);
            case Map<?, ?> map -> writeMap((Map<String, Object>) map);
            case Collection<?> coll -> writeCollection(coll);
            case Object[] arr -> writeArray(arr);
            case int[] arr -> writePrimitiveArray(arr);
            case long[] arr -> writePrimitiveArray(arr);
            case double[] arr -> writePrimitiveArray(arr);
            case boolean[] arr -> writePrimitiveArray(arr);
            case Enum<?> e -> writeString(e.name());
            case Temporal t -> writeString(t.toString());
            case java.util.UUID u -> writeString(u.toString());
            default -> {
                // POJO: convert to JsonNode via BeanMapper, then serialize
                JsonNode node = BeanMapper.toTree(obj);
                writeJsonNode(node);
            }
        }
    }

    private void writeJsonNode(JsonNode node) {
        switch (node) {
            case JsonNode.ObjectNode obj -> {
                sb.append('{');
                pushDepth();
                boolean first = true;
                for (String key : obj.keys()) {
                    if (!first) sb.append(',');
                    newline();
                    writeString(key);
                    sb.append(':');
                    if (prettyPrint) sb.append(' ');
                    writeJsonNode(obj.get(key));
                    first = false;
                }
                popDepth();
                if (!first) newline();
                sb.append('}');
            }
            case JsonNode.ArrayNode arr -> {
                sb.append('[');
                pushDepth();
                boolean first = true;
                for (int i = 0; i < arr.size(); i++) {
                    if (!first) sb.append(',');
                    newline();
                    writeJsonNode(arr.get(i));
                    first = false;
                }
                popDepth();
                if (!first) newline();
                sb.append(']');
            }
            case JsonNode.StringNode s -> writeString(s.value());
            case JsonNode.NumberNode n -> writeNumber(n.value());
            case JsonNode.BooleanNode b -> sb.append(b.value());
            case JsonNode.NullNode _ -> sb.append("null");
        }
    }

    private void writeMap(Map<String, Object> map) {
        sb.append('{');
        pushDepth();
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(',');
            newline();
            writeString(entry.getKey());
            sb.append(':');
            if (prettyPrint) sb.append(' ');
            writeValue(entry.getValue());
            first = false;
        }
        popDepth();
        if (!first) newline();
        sb.append('}');
    }

    private void writeCollection(Collection<?> coll) {
        sb.append('[');
        pushDepth();
        boolean first = true;
        for (Object item : coll) {
            if (!first) sb.append(',');
            newline();
            writeValue(item);
            first = false;
        }
        popDepth();
        if (!first) newline();
        sb.append(']');
    }

    private void writeArray(Object[] arr) {
        sb.append('[');
        pushDepth();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            newline();
            writeValue(arr[i]);
        }
        popDepth();
        if (arr.length > 0) newline();
        sb.append(']');
    }

    private void writePrimitiveArray(int[] arr) {
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        sb.append(']');
    }

    private void writePrimitiveArray(long[] arr) {
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        sb.append(']');
    }

    private void writePrimitiveArray(double[] arr) {
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            writeNumber(arr[i]);
        }
        sb.append(']');
    }

    private void writePrimitiveArray(boolean[] arr) {
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        sb.append(']');
    }

    private void writeString(String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append("\\u").append(String.format("%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private void writeNumber(Number n) {
        if (n instanceof Double d) {
            if (d.isNaN() || d.isInfinite()) {
                sb.append("null");
            } else {
                sb.append(n);
            }
        } else if (n instanceof Float f) {
            if (f.isNaN() || f.isInfinite()) {
                sb.append("null");
            } else {
                sb.append(n);
            }
        } else if (n instanceof BigDecimal bd) {
            sb.append(bd.toPlainString());
        } else if (n instanceof BigInteger) {
            sb.append(n);
        } else {
            sb.append(n);
        }
    }

    private void pushDepth() {
        if (++depth > MAX_DEPTH) {
            throw new IllegalStateException("Nesting depth exceeds " + MAX_DEPTH);
        }
    }

    private void popDepth() {
        depth--;
    }

    private void newline() {
        if (prettyPrint) {
            sb.append('\n');
            sb.append("  ".repeat(depth));
        }
    }
}
