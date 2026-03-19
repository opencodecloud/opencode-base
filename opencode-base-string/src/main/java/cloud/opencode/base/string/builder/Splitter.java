package cloud.opencode.base.string.builder;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * String Splitter - Fluent string splitting with configurable options.
 * 字符串分割器 - 提供可配置选项的流式字符串分割。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Split by string, char, pattern, or CharMatcher - 按字符串、字符、正则或CharMatcher分割</li>
 *   <li>Fixed-length splitting - 固定长度分割</li>
 *   <li>Trim and omit empty strings - 去空格和忽略空串</li>
 *   <li>Map splitting with key-value separators - 键值对分割</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic split
 * List<String> parts = Splitter.on(",").splitToList("a,b,c"); // ["a","b","c"]
 *
 * // Trim and omit empty
 * List<String> clean = Splitter.on(",").trimResults().omitEmptyStrings()
 *     .splitToList("a, ,b,,c"); // ["a","b","c"]
 *
 * // Map split
 * Map<String, String> map = Splitter.on("&").withKeyValueSeparator("=")
 *     .split("key=value&foo=bar"); // {key=value, foo=bar}
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No (input must not be null) - 空值安全: 否（输入不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class Splitter {
    private final Strategy strategy;
    private final boolean omitEmptyStrings;
    private final CharMatcher trimmer;
    private final int limit;

    private interface Strategy {
        Iterator<String> split(CharSequence sequence);
    }

    private Splitter(Strategy strategy, boolean omitEmptyStrings, CharMatcher trimmer, int limit) {
        this.strategy = strategy;
        this.omitEmptyStrings = omitEmptyStrings;
        this.trimmer = trimmer;
        this.limit = limit;
    }

    public static Splitter on(char separator) {
        return on(String.valueOf(separator));
    }

    public static Splitter on(String separator) {
        return new Splitter(new StringStrategy(separator), false, null, Integer.MAX_VALUE);
    }

    public static Splitter on(Pattern pattern) {
        return new Splitter(new PatternStrategy(pattern), false, null, Integer.MAX_VALUE);
    }

    public static Splitter onPattern(String regex) {
        return on(Pattern.compile(regex));
    }

    public static Splitter on(CharMatcher separatorMatcher) {
        return new Splitter(new CharMatcherStrategy(separatorMatcher), false, null, Integer.MAX_VALUE);
    }

    public static Splitter fixedLength(int length) {
        return new Splitter(new FixedLengthStrategy(length), false, null, Integer.MAX_VALUE);
    }

    public Splitter omitEmptyStrings() {
        return new Splitter(strategy, true, trimmer, limit);
    }

    public Splitter trimResults() {
        return trimResults(CharMatcher.whitespace());
    }

    public Splitter trimResults(CharMatcher trimmer) {
        return new Splitter(strategy, omitEmptyStrings, trimmer, limit);
    }

    public Splitter limit(int maxItems) {
        return new Splitter(strategy, omitEmptyStrings, trimmer, maxItems);
    }

    public Iterable<String> split(CharSequence sequence) {
        return () -> {
            Iterator<String> rawIterator = strategy.split(sequence);
            return new Iterator<>() {
                String nextValue = null;
                boolean hasNextCached = false;

                @Override
                public boolean hasNext() {
                    if (!hasNextCached) {
                        while (rawIterator.hasNext()) {
                            String token = processToken(rawIterator.next());
                            if (token != null) {
                                nextValue = token;
                                hasNextCached = true;
                                return true;
                            }
                        }
                        return false;
                    }
                    return true;
                }

                @Override
                public String next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    hasNextCached = false;
                    return nextValue;
                }
            };
        };
    }

    public List<String> splitToList(CharSequence sequence) {
        List<String> result = new ArrayList<>();
        for (String part : split(sequence)) {
            result.add(part);
        }
        return result;
    }

    public Stream<String> splitToStream(CharSequence sequence) {
        return StreamSupport.stream(split(sequence).spliterator(), false);
    }

    public MapSplitter withKeyValueSeparator(String keyValueSeparator) {
        return new MapSplitter(this, Splitter.on(keyValueSeparator));
    }

    public MapSplitter withKeyValueSeparator(char keyValueSeparator) {
        return withKeyValueSeparator(String.valueOf(keyValueSeparator));
    }

    private static class StringStrategy implements Strategy {
        private final String separator;
        StringStrategy(String separator) { this.separator = separator; }

        @Override
        public Iterator<String> split(CharSequence sequence) {
            return new AbstractIterator<>() {
                int position = 0;
                boolean finished = false;

                @Override
                protected String computeNext() {
                    if (finished) {
                        return endOfData();
                    }

                    int nextSep = indexOf(sequence, separator, position);
                    if (nextSep < 0) {
                        String token = sequence.subSequence(position, sequence.length()).toString();
                        finished = true;
                        return token;
                    } else {
                        String token = sequence.subSequence(position, nextSep).toString();
                        position = nextSep + separator.length();
                        return token;
                    }
                }
            };
        }

        private int indexOf(CharSequence seq, String target, int start) {
            String str = seq.toString();
            return str.indexOf(target, start);
        }
    }

    private String processToken(String token) {
        if (trimmer != null) {
            token = trimmer.trimFrom(token);
        }
        if (omitEmptyStrings && token.isEmpty()) {
            return null;
        }
        return token;
    }

    private static class PatternStrategy implements Strategy {
        private final Pattern pattern;
        PatternStrategy(Pattern pattern) { this.pattern = pattern; }

        @Override
        public Iterator<String> split(CharSequence sequence) {
            String[] parts = pattern.split(sequence);
            return Arrays.asList(parts).iterator();
        }
    }

    private static class CharMatcherStrategy implements Strategy {
        private final CharMatcher matcher;
        CharMatcherStrategy(CharMatcher matcher) { this.matcher = matcher; }

        @Override
        public Iterator<String> split(CharSequence sequence) {
            return new AbstractIterator<>() {
                int position = 0;

                @Override
                protected String computeNext() {
                    if (position >= sequence.length()) {
                        return endOfData();
                    }

                    int start = position;
                    while (position < sequence.length() && !matcher.matches(sequence.charAt(position))) {
                        position++;
                    }
                    String token = sequence.subSequence(start, position).toString();
                    if (position < sequence.length()) position++; // skip separator
                    return token;
                }
            };
        }
    }

    private static class FixedLengthStrategy implements Strategy {
        private final int length;
        FixedLengthStrategy(int length) { this.length = length; }

        @Override
        public Iterator<String> split(CharSequence sequence) {
            return new AbstractIterator<>() {
                int position = 0;

                @Override
                protected String computeNext() {
                    if (position >= sequence.length()) {
                        return endOfData();
                    }
                    int end = Math.min(position + length, sequence.length());
                    String token = sequence.subSequence(position, end).toString();
                    position = end;
                    return token;
                }
            };
        }
    }

    private abstract static class AbstractIterator<T> implements Iterator<T> {
        private T next;
        private boolean hasNext;
        private boolean computed;

        protected abstract T computeNext();

        protected final T endOfData() {
            hasNext = false;
            return null;
        }

        @Override
        public boolean hasNext() {
            if (!computed) {
                next = computeNext();
                computed = true;
                hasNext = (next != null);
            }
            return hasNext;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            computed = false;
            return next;
        }
    }

    public static final class MapSplitter {
        private final Splitter entrySplitter;
        private final Splitter kvSplitter;

        MapSplitter(Splitter entrySplitter, Splitter kvSplitter) {
            this.entrySplitter = entrySplitter;
            this.kvSplitter = kvSplitter;
        }

        public Map<String, String> split(CharSequence sequence) {
            Map<String, String> map = new LinkedHashMap<>();
            for (String entry : entrySplitter.split(sequence)) {
                List<String> kv = kvSplitter.splitToList(entry);
                if (kv.size() == 2) {
                    map.put(kv.get(0), kv.get(1));
                } else if (kv.size() == 1) {
                    map.put(kv.get(0), "");
                }
            }
            return map;
        }
    }
}
