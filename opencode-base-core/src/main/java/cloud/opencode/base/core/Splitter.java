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

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Splitter - Fluent string splitting utility
 * 分割器 - 流式字符串分割工具
 *
 * <p>Provides a fluent API for splitting strings with various options
 * for trimming, omitting empty strings, and limiting results.</p>
 * <p>提供用于分割字符串的流式 API，支持修剪、省略空字符串和限制结果等选项。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic splitting
 * List<String> parts = Splitter.on(',').splitToList("a,b,c");  // ["a", "b", "c"]
 *
 * // Trim whitespace
 * List<String> trimmed = Splitter.on(',').trimResults().splitToList("a , b , c");
 *
 * // Omit empty strings
 * List<String> nonEmpty = Splitter.on(',').omitEmptyStrings().splitToList("a,,b,c");
 *
 * // Fixed length splitting
 * List<String> fixed = Splitter.fixedLength(3).splitToList("abcdefg");  // ["abc", "def", "g"]
 *
 * // Limit results
 * List<String> limited = Splitter.on(',').limit(2).splitToList("a,b,c,d");  // ["a", "b,c,d"]
 *
 * // Split to map
 * Map<String, String> map = Splitter.on(',').withKeyValueSeparator('=')
 *     .split("a=1,b=2,c=3");  // {a=1, b=2, c=3}
 *
 * // Pattern-based splitting
 * List<String> regex = Splitter.onPattern("\\s+").splitToList("a  b   c");
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API for string splitting - 字符串分割的流式API</li>
 *   <li>Trim, omit empty, and limit options - 修剪、省略空串和限制选项</li>
 *   <li>Fixed-length and regex splitting - 固定长度和正则表达式分割</li>
 *   <li>Key-value map splitting - 键值Map分割</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after creation) - 线程安全: 是（创建后不可变）</li>
 *   <li>Null-safe: No, input must not be null - 空值安全: 否，输入不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Joiner
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class Splitter {

    private final Strategy strategy;
    private final boolean omitEmptyStrings;
    private final boolean trimResults;
    private final Function<String, String> trimFunction;
    private final int limit;

    private Splitter(Strategy strategy) {
        this(strategy, false, false, null, Integer.MAX_VALUE);
    }

    private Splitter(Strategy strategy, boolean omitEmptyStrings, boolean trimResults,
                     Function<String, String> trimFunction, int limit) {
        this.strategy = strategy;
        this.omitEmptyStrings = omitEmptyStrings;
        this.trimResults = trimResults;
        this.trimFunction = trimFunction;
        this.limit = limit;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a splitter that uses the given character as separator.
     * 创建使用给定字符作为分隔符的分割器。
     *
     * @param separator the separator character - 分隔符字符
     * @return the splitter - 分割器
     */
    public static Splitter on(char separator) {
        return new Splitter(new CharStrategy(separator));
    }

    /**
     * Creates a splitter that uses the given string as separator.
     * 创建使用给定字符串作为分隔符的分割器。
     *
     * @param separator the separator string - 分隔符字符串
     * @return the splitter - 分割器
     */
    public static Splitter on(String separator) {
        if (separator.length() == 1) {
            return on(separator.charAt(0));
        }
        return new Splitter(new StringStrategy(separator));
    }

    /**
     * Creates a splitter that uses the given pattern as separator.
     * 创建使用给定模式作为分隔符的分割器。
     *
     * @param pattern the pattern - 模式
     * @return the splitter - 分割器
     */
    public static Splitter on(Pattern pattern) {
        return new Splitter(new PatternStrategy(pattern));
    }

    /**
     * Creates a splitter that uses the given regex pattern as separator.
     * 创建使用给定正则表达式模式作为分隔符的分割器。
     *
     * @param regex the regex pattern - 正则表达式模式
     * @return the splitter - 分割器
     */
    public static Splitter onPattern(String regex) {
        return on(Pattern.compile(regex));
    }

    /**
     * Creates a splitter that splits strings into fixed-length parts.
     * 创建将字符串分割为固定长度部分的分割器。
     *
     * @param length the fixed length - 固定长度
     * @return the splitter - 分割器
     */
    public static Splitter fixedLength(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive: " + length);
        }
        return new Splitter(new FixedLengthStrategy(length));
    }

    // ==================== Configuration Methods | 配置方法 ====================

    /**
     * Returns a splitter that omits empty strings from results.
     * 返回从结果中省略空字符串的分割器。
     *
     * @return the splitter - 分割器
     */
    public Splitter omitEmptyStrings() {
        return new Splitter(strategy, true, trimResults, trimFunction, limit);
    }

    /**
     * Returns a splitter that trims whitespace from results.
     * 返回从结果中修剪空白的分割器。
     *
     * @return the splitter - 分割器
     */
    public Splitter trimResults() {
        return new Splitter(strategy, omitEmptyStrings, true, String::strip, limit);
    }

    /**
     * Returns a splitter that trims results using the given function.
     * 返回使用给定函数修剪结果的分割器。
     *
     * @param trimFunction the trim function - 修剪函数
     * @return the splitter - 分割器
     */
    public Splitter trimResults(Function<String, String> trimFunction) {
        return new Splitter(strategy, omitEmptyStrings, true, trimFunction, limit);
    }

    /**
     * Returns a splitter that limits the number of parts.
     * 返回限制部分数量的分割器。
     *
     * @param limit the limit - 限制
     * @return the splitter - 分割器
     */
    public Splitter limit(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be at least 1: " + limit);
        }
        return new Splitter(strategy, omitEmptyStrings, trimResults, trimFunction, limit);
    }

    // ==================== Splitting Methods | 分割方法 ====================

    /**
     * Splits the given string and returns an iterable of parts.
     * 分割给定字符串并返回部分的可迭代对象。
     *
     * @param input the input string - 输入字符串
     * @return the iterable of parts - 部分的可迭代对象
     */
    public Iterable<String> split(CharSequence input) {
        return () -> new SplitIterator(input);
    }

    /**
     * Splits the given string and returns a list of parts.
     * 分割给定字符串并返回部分的列表。
     *
     * @param input the input string - 输入字符串
     * @return the list of parts - 部分的列表
     */
    public List<String> splitToList(CharSequence input) {
        List<String> result = new ArrayList<>();
        for (String part : split(input)) {
            result.add(part);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Splits the given string and returns a stream of parts.
     * 分割给定字符串并返回部分的流。
     *
     * @param input the input string - 输入字符串
     * @return the stream of parts - 部分的流
     */
    public Stream<String> splitToStream(CharSequence input) {
        return StreamSupport.stream(split(input).spliterator(), false);
    }

    // ==================== Map Splitting | 映射分割 ====================

    /**
     * Returns a map splitter using the given key-value separator.
     * 返回使用给定键值分隔符的映射分割器。
     *
     * @param separator the key-value separator - 键值分隔符
     * @return the map splitter - 映射分割器
     */
    public MapSplitter withKeyValueSeparator(char separator) {
        return new MapSplitter(this, Splitter.on(separator));
    }

    /**
     * Returns a map splitter using the given key-value separator.
     * 返回使用给定键值分隔符的映射分割器。
     *
     * @param separator the key-value separator - 键值分隔符
     * @return the map splitter - 映射分割器
     */
    public MapSplitter withKeyValueSeparator(String separator) {
        return new MapSplitter(this, Splitter.on(separator));
    }

    /**
     * Returns a map splitter using the given splitter for key-value pairs.
     * 返回使用给定分割器处理键值对的映射分割器。
     *
     * @param kvSplitter the key-value splitter - 键值分割器
     * @return the map splitter - 映射分割器
     */
    public MapSplitter withKeyValueSeparator(Splitter kvSplitter) {
        return new MapSplitter(this, kvSplitter);
    }

    // ==================== Iterator Implementation | 迭代器实现 ====================

    private class SplitIterator implements Iterator<String> {
        private final CharSequence input;
        private int position;
        private int partCount;
        private String next;
        private boolean finished;

        SplitIterator(CharSequence input) {
            this.input = input;
            this.position = 0;
            this.partCount = 0;
            this.finished = false;
            advance();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public String next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            String result = next;
            advance();
            return result;
        }

        private void advance() {
            next = null;

            while (next == null && !finished) {
                if (partCount >= limit - 1) {
                    // Last part - take everything remaining
                    if (position < input.length()) {
                        next = input.subSequence(position, input.length()).toString();
                        position = input.length();
                    }
                    finished = true;
                } else {
                    int[] bounds = strategy.nextBounds(input, position);
                    if (bounds == null) {
                        // No more separators
                        if (position < input.length()) {
                            next = input.subSequence(position, input.length()).toString();
                            position = input.length();
                        }
                        finished = true;
                    } else {
                        next = input.subSequence(position, bounds[0]).toString();
                        position = bounds[1];
                    }
                }

                if (next != null) {
                    if (trimResults && trimFunction != null) {
                        next = trimFunction.apply(next);
                    }
                    if (omitEmptyStrings && next.isEmpty()) {
                        next = null;
                    } else {
                        partCount++;
                    }
                }
            }
        }
    }

    // ==================== Strategy Interface | 策略接口 ====================

    private interface Strategy {
        /**
         * Returns [start, end] of the next separator, or null if none.
         */
        int[] nextBounds(CharSequence input, int start);
    }

    private record CharStrategy(char separator) implements Strategy {
        @Override
        public int[] nextBounds(CharSequence input, int start) {
            for (int i = start; i < input.length(); i++) {
                if (input.charAt(i) == separator) {
                    return new int[]{i, i + 1};
                }
            }
            return null;
        }
    }

    private record StringStrategy(String separator) implements Strategy {
        @Override
        public int[] nextBounds(CharSequence input, int start) {
            String str = input.toString();
            int idx = str.indexOf(separator, start);
            return idx >= 0 ? new int[]{idx, idx + separator.length()} : null;
        }
    }

    private record PatternStrategy(Pattern pattern) implements Strategy {
        @Override
        public int[] nextBounds(CharSequence input, int start) {
            var matcher = pattern.matcher(input);
            if (matcher.find(start)) {
                return new int[]{matcher.start(), matcher.end()};
            }
            return null;
        }
    }

    private record FixedLengthStrategy(int length) implements Strategy {
        @Override
        public int[] nextBounds(CharSequence input, int start) {
            int end = start + length;
            return end < input.length() ? new int[]{end, end} : null;
        }
    }

    // ==================== Map Splitter | 映射分割器 ====================

    /**
     * A splitter that produces a map from key-value pair strings.
     * 从键值对字符串生成映射的分割器。
     */
    public static final class MapSplitter {
        private final Splitter outerSplitter;
        private final Splitter entrySplitter;

        private MapSplitter(Splitter outerSplitter, Splitter entrySplitter) {
            this.outerSplitter = outerSplitter;
            this.entrySplitter = entrySplitter.limit(2);
        }

        /**
         * Splits the input into a map.
         * 将输入分割成映射。
         *
         * @param input the input string - 输入字符串
         * @return the map - 映射
         */
        public Map<String, String> split(CharSequence input) {
            Map<String, String> result = new LinkedHashMap<>();
            for (String entry : outerSplitter.split(input)) {
                List<String> keyValue = entrySplitter.splitToList(entry);
                if (keyValue.size() == 2) {
                    result.put(keyValue.get(0), keyValue.get(1));
                } else if (keyValue.size() == 1 && !keyValue.get(0).isEmpty()) {
                    result.put(keyValue.get(0), "");
                }
            }
            return Collections.unmodifiableMap(result);
        }
    }
}
