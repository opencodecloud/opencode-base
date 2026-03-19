/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.core;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * Joiner - Fluent string joining utility
 * 连接器 - 流式字符串连接工具
 *
 * <p>Provides a fluent API for joining strings with various options
 * for null handling and formatting.</p>
 * <p>提供用于连接字符串的流式 API，支持空值处理和格式化等选项。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic joining
 * String result = Joiner.on(',').join("a", "b", "c");  // "a,b,c"
 *
 * // Join with list
 * String result = Joiner.on(", ").join(List.of("a", "b", "c"));  // "a, b, c"
 *
 * // Skip nulls
 * String result = Joiner.on(',').skipNulls().join("a", null, "b");  // "a,b"
 *
 * // Replace nulls
 * String result = Joiner.on(',').useForNull("N/A").join("a", null, "b");  // "a,N/A,b"
 *
 * // Join to existing StringBuilder
 * StringBuilder sb = new StringBuilder("prefix: ");
 * Joiner.on(',').appendTo(sb, "a", "b", "c");  // "prefix: a,b,c"
 *
 * // Join map entries
 * Map<String, Integer> map = Map.of("a", 1, "b", 2);
 * String result = Joiner.on(',').withKeyValueSeparator('=').join(map);  // "a=1,b=2"
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API with separator configuration - 支持分隔符配置的流式API</li>
 *   <li>Null handling: skip nulls or use default text - 空值处理: 跳过或替换</li>
 *   <li>Join to StringBuilder or Appendable - 连接到StringBuilder或Appendable</li>
 *   <li>Map entry joining with key-value separator - 使用键值分隔符连接Map条目</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after creation) - 线程安全: 是（创建后不可变）</li>
 *   <li>Null-safe: Yes, with skipNulls() or useForNull() - 空值安全: 是，通过skipNulls()或useForNull()</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Splitter
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class Joiner {

    private final String separator;
    private final boolean skipNulls;
    private final String nullText;
    private final Function<Object, String> formatter;

    private Joiner(String separator) {
        this(separator, false, null, null);
    }

    private Joiner(String separator, boolean skipNulls, String nullText, Function<Object, String> formatter) {
        this.separator = Objects.requireNonNull(separator);
        this.skipNulls = skipNulls;
        this.nullText = nullText;
        this.formatter = formatter;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a joiner that uses the given separator.
     * 创建使用给定分隔符的连接器。
     *
     * @param separator the separator character - 分隔符字符
     * @return the joiner - 连接器
     */
    public static Joiner on(char separator) {
        return new Joiner(String.valueOf(separator));
    }

    /**
     * Creates a joiner that uses the given separator.
     * 创建使用给定分隔符的连接器。
     *
     * @param separator the separator string - 分隔符字符串
     * @return the joiner - 连接器
     */
    public static Joiner on(String separator) {
        return new Joiner(separator);
    }

    // ==================== Configuration Methods | 配置方法 ====================

    /**
     * Returns a joiner that skips null values.
     * 返回跳过空值的连接器。
     *
     * @return the joiner - 连接器
     */
    public Joiner skipNulls() {
        return new Joiner(separator, true, null, formatter);
    }

    /**
     * Returns a joiner that replaces null values with the given text.
     * 返回用给定文本替换空值的连接器。
     *
     * @param nullText the text to use for nulls - 用于空值的文本
     * @return the joiner - 连接器
     */
    public Joiner useForNull(String nullText) {
        Objects.requireNonNull(nullText);
        return new Joiner(separator, false, nullText, formatter);
    }

    /**
     * Returns a joiner that formats values using the given function.
     * 返回使用给定函数格式化值的连接器。
     *
     * @param formatter the formatter function - 格式化函数
     * @return the joiner - 连接器
     */
    public Joiner withFormatter(Function<Object, String> formatter) {
        return new Joiner(separator, skipNulls, nullText, formatter);
    }

    // ==================== Join Methods | 连接方法 ====================

    /**
     * Joins the given objects into a string.
     * 将给定对象连接成字符串。
     *
     * @param parts the parts to join - 要连接的部分
     * @return the joined string - 连接后的字符串
     */
    public String join(Object... parts) {
        return join(Arrays.asList(parts));
    }

    /**
     * Joins the given iterable into a string.
     * 将给定的可迭代对象连接成字符串。
     *
     * @param parts the parts to join - 要连接的部分
     * @return the joined string - 连接后的字符串
     */
    public String join(Iterable<?> parts) {
        return join(parts.iterator());
    }

    /**
     * Joins the given iterator into a string.
     * 将给定的迭代器连接成字符串。
     *
     * @param parts the parts to join - 要连接的部分
     * @return the joined string - 连接后的字符串
     */
    public String join(Iterator<?> parts) {
        StringBuilder sb = new StringBuilder();
        try {
            appendTo(sb, parts);
        } catch (IOException e) {
            // StringBuilder doesn't throw IOException
            throw new AssertionError(e);
        }
        return sb.toString();
    }

    // ==================== Append Methods | 追加方法 ====================

    /**
     * Appends the joined string to the given appendable.
     * 将连接后的字符串追加到给定的可追加对象。
     *
     * @param <A> the appendable type - 可追加类型
     * @param appendable the appendable - 可追加对象
     * @param parts the parts to join - 要连接的部分
     * @return the appendable - 可追加对象
     * @throws IOException if an I/O error occurs - 如果发生 I/O 错误
     */
    public <A extends Appendable> A appendTo(A appendable, Object... parts) throws IOException {
        return appendTo(appendable, Arrays.asList(parts));
    }

    /**
     * Appends the joined string to the given appendable.
     * 将连接后的字符串追加到给定的可追加对象。
     *
     * @param <A> the appendable type - 可追加类型
     * @param appendable the appendable - 可追加对象
     * @param parts the parts to join - 要连接的部分
     * @return the appendable - 可追加对象
     * @throws IOException if an I/O error occurs - 如果发生 I/O 错误
     */
    public <A extends Appendable> A appendTo(A appendable, Iterable<?> parts) throws IOException {
        return appendTo(appendable, parts.iterator());
    }

    /**
     * Appends the joined string to the given appendable.
     * 将连接后的字符串追加到给定的可追加对象。
     *
     * @param <A> the appendable type - 可追加类型
     * @param appendable the appendable - 可追加对象
     * @param parts the parts to join - 要连接的部分
     * @return the appendable - 可追加对象
     * @throws IOException if an I/O error occurs - 如果发生 I/O 错误
     */
    public <A extends Appendable> A appendTo(A appendable, Iterator<?> parts) throws IOException {
        Objects.requireNonNull(appendable);

        boolean first = true;
        while (parts.hasNext()) {
            Object part = parts.next();

            if (part == null) {
                if (skipNulls) {
                    continue;
                }
                if (nullText == null) {
                    throw new NullPointerException("Null element in input (use skipNulls or useForNull)");
                }
                part = nullText;
            }

            if (!first) {
                appendable.append(separator);
            }

            String text = format(part);
            appendable.append(text);
            first = false;
        }

        return appendable;
    }

    /**
     * Appends the joined string to the given StringBuilder.
     * 将连接后的字符串追加到给定的 StringBuilder。
     *
     * @param sb the StringBuilder - StringBuilder
     * @param parts the parts to join - 要连接的部分
     * @return the StringBuilder - StringBuilder
     */
    public StringBuilder appendTo(StringBuilder sb, Object... parts) {
        try {
            appendTo((Appendable) sb, parts);
            return sb;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Appends the joined string to the given StringBuilder.
     * 将连接后的字符串追加到给定的 StringBuilder。
     *
     * @param sb the StringBuilder - StringBuilder
     * @param parts the parts to join - 要连接的部分
     * @return the StringBuilder - StringBuilder
     */
    public StringBuilder appendTo(StringBuilder sb, Iterable<?> parts) {
        try {
            appendTo((Appendable) sb, parts);
            return sb;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    // ==================== Map Joining | 映射连接 ====================

    /**
     * Returns a map joiner using the given key-value separator.
     * 返回使用给定键值分隔符的映射连接器。
     *
     * @param separator the key-value separator - 键值分隔符
     * @return the map joiner - 映射连接器
     */
    public MapJoiner withKeyValueSeparator(char separator) {
        return new MapJoiner(this, String.valueOf(separator));
    }

    /**
     * Returns a map joiner using the given key-value separator.
     * 返回使用给定键值分隔符的映射连接器。
     *
     * @param separator the key-value separator - 键值分隔符
     * @return the map joiner - 映射连接器
     */
    public MapJoiner withKeyValueSeparator(String separator) {
        return new MapJoiner(this, separator);
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private String format(Object obj) {
        if (formatter != null) {
            return formatter.apply(obj);
        }
        return obj.toString();
    }

    // ==================== Map Joiner | 映射连接器 ====================

    /**
     * A joiner that produces strings from map entries.
     * 从映射条目生成字符串的连接器。
     */
    public static final class MapJoiner {
        private final Joiner joiner;
        private final String keyValueSeparator;
        private final boolean skipNullValues;
        private final String nullValueText;

        private MapJoiner(Joiner joiner, String keyValueSeparator) {
            this(joiner, keyValueSeparator, false, null);
        }

        private MapJoiner(Joiner joiner, String keyValueSeparator, boolean skipNullValues, String nullValueText) {
            this.joiner = joiner;
            this.keyValueSeparator = Objects.requireNonNull(keyValueSeparator);
            this.skipNullValues = skipNullValues;
            this.nullValueText = nullValueText;
        }

        /**
         * Returns a map joiner that skips entries with null values.
         * 返回跳过具有空值的条目的映射连接器。
         *
         * @return the map joiner - 映射连接器
         */
        public MapJoiner skipNullValues() {
            return new MapJoiner(joiner, keyValueSeparator, true, null);
        }

        /**
         * Returns a map joiner that replaces null values with the given text.
         * 返回用给定文本替换空值的映射连接器。
         *
         * @param nullText the text to use for null values - 用于空值的文本
         * @return the map joiner - 映射连接器
         */
        public MapJoiner useForNull(String nullText) {
            Objects.requireNonNull(nullText);
            return new MapJoiner(joiner, keyValueSeparator, false, nullText);
        }

        /**
         * Joins the given map into a string.
         * 将给定映射连接成字符串。
         *
         * @param map the map - 映射
         * @return the joined string - 连接后的字符串
         */
        public String join(Map<?, ?> map) {
            return join(map.entrySet());
        }

        /**
         * Joins the given map entries into a string.
         * 将给定映射条目连接成字符串。
         *
         * @param entries the entries - 条目
         * @return the joined string - 连接后的字符串
         */
        public String join(Iterable<? extends Map.Entry<?, ?>> entries) {
            StringBuilder sb = new StringBuilder();
            appendTo(sb, entries);
            return sb.toString();
        }

        /**
         * Appends the joined map to the given StringBuilder.
         * 将连接后的映射追加到给定的 StringBuilder。
         *
         * @param sb the StringBuilder - StringBuilder
         * @param map the map - 映射
         * @return the StringBuilder - StringBuilder
         */
        public StringBuilder appendTo(StringBuilder sb, Map<?, ?> map) {
            return appendTo(sb, map.entrySet());
        }

        /**
         * Appends the joined entries to the given StringBuilder.
         * 将连接后的条目追加到给定的 StringBuilder。
         *
         * @param sb the StringBuilder - StringBuilder
         * @param entries the entries - 条目
         * @return the StringBuilder - StringBuilder
         */
        public StringBuilder appendTo(StringBuilder sb, Iterable<? extends Map.Entry<?, ?>> entries) {
            try {
                appendTo((Appendable) sb, entries);
                return sb;
            } catch (IOException e) {
                // StringBuilder doesn't throw IOException
                throw new AssertionError(e);
            }
        }

        /**
         * Appends the joined map to the given Appendable.
         * 将连接后的映射追加到给定的 Appendable。
         *
         * @param <A> the appendable type - 可追加类型
         * @param appendable the appendable - 可追加对象
         * @param map the map - 映射
         * @return the appendable - 可追加对象
         * @throws IOException if an I/O error occurs - 如果发生 I/O 错误
         */
        public <A extends Appendable> A appendTo(A appendable, Map<?, ?> map) throws IOException {
            return appendTo(appendable, map.entrySet());
        }

        /**
         * Appends the joined entries to the given Appendable.
         * 将连接后的条目追加到给定的 Appendable。
         *
         * @param <A> the appendable type - 可追加类型
         * @param appendable the appendable - 可追加对象
         * @param entries the entries - 条目
         * @return the appendable - 可追加对象
         * @throws IOException if an I/O error occurs - 如果发生 I/O 错误
         */
        public <A extends Appendable> A appendTo(A appendable, Iterable<? extends Map.Entry<?, ?>> entries)
                throws IOException {
            boolean first = true;

            for (Map.Entry<?, ?> entry : entries) {
                Object key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    if (skipNullValues) {
                        continue;
                    }
                    if (nullValueText == null) {
                        throw new NullPointerException("Null value for key: " + key);
                    }
                    value = nullValueText;
                }

                if (!first) {
                    appendable.append(joiner.separator);
                }

                appendable.append(String.valueOf(key));
                appendable.append(keyValueSeparator);
                appendable.append(String.valueOf(value));
                first = false;
            }

            return appendable;
        }
    }
}
