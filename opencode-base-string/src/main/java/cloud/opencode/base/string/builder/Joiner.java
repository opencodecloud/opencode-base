package cloud.opencode.base.string.builder;

import java.util.*;

/**
 * String Joiner (Guava Style)
 * 字符串连接器（Guava风格）
 *
 * <p>Joins pieces of text with a separator. Thread-safe and immutable.</p>
 * <p>使用分隔符连接文本片段。线程安全且不可变。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Flexible null handling - 灵活的null处理</li>
 *   <li>Map joining support - Map连接支持</li>
 *   <li>Append to existing builders - 追加到现有构建器</li>
 *   <li>Immutable, thread-safe - 不可变，线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic join
 * String result = Joiner.on(", ").join(Arrays.asList("a", "b", "c"));
 * // -> "a, b, c"
 *
 * // Skip nulls
 * String safe = Joiner.on(", ").skipNulls().join("a", null, "c");
 * // -> "a, c"
 *
 * // Replace nulls
 * String withDefault = Joiner.on(", ").useForNull("N/A").join("a", null, "c");
 * // -> "a, N/A, c"
 *
 * // Join Map
 * String params = Joiner.on("&").withKeyValueSeparator("=")
 *     .join(Map.of("key", "value"));
 * // -> "key=value"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Configurable (skipNulls/useForNull) - 空值安全: 可配置</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class Joiner {
    private final String separator;
    private final String nullText;
    private final boolean skipNulls;

    private Joiner(String separator, String nullText, boolean skipNulls) {
        this.separator = separator;
        this.nullText = nullText;
        this.skipNulls = skipNulls;
    }

    public static Joiner on(String separator) {
        return new Joiner(separator, null, false);
    }

    public static Joiner on(char separator) {
        return new Joiner(String.valueOf(separator), null, false);
    }

    public Joiner skipNulls() {
        if (this.skipNulls) return this;
        if (this.nullText != null) {
            throw new IllegalStateException("Cannot use skipNulls() with useForNull()");
        }
        return new Joiner(separator, null, true);
    }

    public Joiner useForNull(String nullText) {
        if (this.nullText != null) return this;
        if (this.skipNulls) {
            throw new IllegalStateException("Cannot use useForNull() with skipNulls()");
        }
        return new Joiner(separator, nullText, false);
    }

    public String join(Iterable<?> parts) {
        return join(parts.iterator());
    }

    public String join(Iterator<?> parts) {
        StringBuilder sb = new StringBuilder();
        appendTo(sb, parts);
        return sb.toString();
    }

    public String join(Object[] parts) {
        return join(Arrays.asList(parts));
    }

    public String join(Object first, Object second, Object... rest) {
        List<Object> list = new ArrayList<>();
        list.add(first);
        list.add(second);
        list.addAll(Arrays.asList(rest));
        return join(list);
    }

    public <A extends Appendable> A appendTo(A appendable, Iterable<?> parts) {
        return appendTo(appendable, parts.iterator());
    }

    public <A extends Appendable> A appendTo(A appendable, Iterator<?> parts) {
        try {
            boolean first = true;
            while (parts.hasNext()) {
                Object part = parts.next();
                if (part == null) {
                    if (skipNulls) continue;
                    if (nullText == null) {
                        throw new NullPointerException("null element in iterable");
                    }
                    part = nullText;
                }
                if (!first) {
                    appendable.append(separator);
                }
                appendable.append(part.toString());
                first = false;
            }
            return appendable;
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    public StringBuilder appendTo(StringBuilder builder, Iterable<?> parts) {
        return appendTo(builder, parts.iterator());
    }

    public MapJoiner withKeyValueSeparator(String keyValueSeparator) {
        return new MapJoiner(this, keyValueSeparator);
    }

    public MapJoiner withKeyValueSeparator(char keyValueSeparator) {
        return withKeyValueSeparator(String.valueOf(keyValueSeparator));
    }

    public static final class MapJoiner {
        private final Joiner entryJoiner;
        private final String keyValueSeparator;
        private final Joiner kvJoiner;

        private MapJoiner(Joiner entryJoiner, String keyValueSeparator, Joiner kvJoiner) {
            this.entryJoiner = entryJoiner;
            this.keyValueSeparator = keyValueSeparator;
            this.kvJoiner = kvJoiner;
        }

        private MapJoiner(Joiner entryJoiner, String keyValueSeparator) {
            this(entryJoiner, keyValueSeparator, Joiner.on(keyValueSeparator));
        }

        public String join(Map<?, ?> map) {
            return entryJoiner.join(map.entrySet().stream()
                .map(e -> kvJoiner.join(e.getKey(), e.getValue()))
                .toList());
        }

        public <A extends Appendable> A appendTo(A appendable, Map<?, ?> map) {
            return entryJoiner.appendTo(appendable, map.entrySet().stream()
                .map(e -> kvJoiner.join(e.getKey(), e.getValue()))
                .toList());
        }

        public MapJoiner useForNull(String nullText) {
            return new MapJoiner(entryJoiner.useForNull(nullText), keyValueSeparator, kvJoiner.useForNull(nullText));
        }
    }
}
