/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.string.match;

import java.util.*;

/**
 * Aho-Corasick Multi-Pattern Matcher - Efficient multi-pattern string matching
 * Aho-Corasick 多模式匹配器 - 高效的多模式字符串匹配
 *
 * <p>Implements the Aho-Corasick algorithm for matching multiple patterns
 * simultaneously in a single pass through the text. Time complexity is O(n + m + z)
 * where n is text length, m is total pattern length, and z is number of matches.</p>
 * <p>实现 Aho-Corasick 算法，可在单次遍历文本中同时匹配多个模式。
 * 时间复杂度为 O(n + m + z)，其中 n 是文本长度，m 是模式总长度，z 是匹配数。</p>
 *
 * <p><strong>Common Use Cases | 常见用例:</strong></p>
 * <ul>
 *   <li>Sensitive word filtering - 敏感词过滤</li>
 *   <li>Multi-keyword search - 多关键词搜索</li>
 *   <li>Spam detection - 垃圾信息检测</li>
 *   <li>Content moderation - 内容审核</li>
 *   <li>Log analysis - 日志分析</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>O(n + m + z) time complexity for multi-pattern matching - O(n + m + z)时间复杂度的多模式匹配</li>
 *   <li>Case-sensitive and case-insensitive matching modes - 区分大小写和不区分大小写匹配模式</li>
 *   <li>Find all matches, first match, or check containment - 查找所有匹配、首次匹配或检查包含</li>
 *   <li>Replace, filter (mask), and highlight matched patterns - 替换、过滤（屏蔽）和高亮匹配模式</li>
 *   <li>Automatic handling of overlapping matches - 自动处理重叠匹配</li>
 *   <li>Builder pattern for flexible configuration - 构建器模式实现灵活配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build the matcher
 * AhoCorasick matcher = AhoCorasick.builder()
 *     .addPattern("bad")
 *     .addPattern("word")
 *     .addPatterns(List.of("spam", "virus"))
 *     .ignoreCase(true)
 *     .build();
 *
 * // Find all matches
 * List<PatternMatch> matches = matcher.findAll("This is a bad word");
 *
 * // Check if contains any pattern
 * boolean contains = matcher.containsAny("Clean text here");
 *
 * // Replace all matches
 * String result = matcher.replaceAll("Bad words here", "***");
 *
 * // Filter sensitive words
 * String filtered = matcher.filter("Some bad content", '*');
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see PatternMatch
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.2.0
 */
public final class AhoCorasick {

    private final TrieNode root;
    private final boolean ignoreCase;
    private final Set<String> patterns;

    private AhoCorasick(Builder builder) {
        this.ignoreCase = builder.ignoreCase;
        this.patterns = Collections.unmodifiableSet(new HashSet<>(builder.patterns));
        this.root = buildTrie(builder.patterns);
        buildFailureLinks();
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return a new builder | 新的构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a matcher from patterns.
     * 从模式创建匹配器。
     *
     * @param patterns the patterns to match | 要匹配的模式
     * @return a new matcher | 新的匹配器
     */
    public static AhoCorasick of(String... patterns) {
        return builder().addPatterns(Arrays.asList(patterns)).build();
    }

    /**
     * Creates a matcher from patterns.
     * 从模式创建匹配器。
     *
     * @param patterns the patterns to match | 要匹配的模式
     * @return a new matcher | 新的匹配器
     */
    public static AhoCorasick of(Collection<String> patterns) {
        return builder().addPatterns(patterns).build();
    }

    /**
     * Creates a case-insensitive matcher from patterns.
     * 从模式创建不区分大小写的匹配器。
     *
     * @param patterns the patterns to match | 要匹配的模式
     * @return a new matcher | 新的匹配器
     */
    public static AhoCorasick ofIgnoreCase(String... patterns) {
        return builder().addPatterns(Arrays.asList(patterns)).ignoreCase(true).build();
    }

    /**
     * Creates a case-insensitive matcher from patterns.
     * 从模式创建不区分大小写的匹配器。
     *
     * @param patterns the patterns to match | 要匹配的模式
     * @return a new matcher | 新的匹配器
     */
    public static AhoCorasick ofIgnoreCase(Collection<String> patterns) {
        return builder().addPatterns(patterns).ignoreCase(true).build();
    }

    // ==================== Match Operations ====================

    /**
     * Finds all pattern matches in the text.
     * 在文本中查找所有模式匹配。
     *
     * @param text the text to search | 要搜索的文本
     * @return list of all matches | 所有匹配的列表
     */
    public List<PatternMatch> findAll(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        List<PatternMatch> matches = new ArrayList<>();
        String searchText = ignoreCase ? text.toLowerCase() : text;
        TrieNode current = root;

        for (int i = 0; i < searchText.length(); i++) {
            char c = searchText.charAt(i);
            current = getNextState(current, c);

            // Collect all matches at this position using output links
            if (current.isEnd) {
                int start = i - current.depth + 1;
                String matchedText = text.substring(start, i + 1);
                matches.add(new PatternMatch(current.pattern, start, i + 1, matchedText));
            }
            TrieNode temp = current.outputLink;
            while (temp != null) {
                int start = i - temp.depth + 1;
                String matchedText = text.substring(start, i + 1);
                matches.add(new PatternMatch(temp.pattern, start, i + 1, matchedText));
                temp = temp.outputLink;
            }
        }

        return matches;
    }

    /**
     * Finds the first pattern match in the text.
     * 在文本中查找第一个模式匹配。
     *
     * @param text the text to search | 要搜索的文本
     * @return the first match, or empty if no match | 第一个匹配，如果没有匹配则为空
     */
    public Optional<PatternMatch> findFirst(String text) {
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }

        String searchText = ignoreCase ? text.toLowerCase() : text;
        TrieNode current = root;

        for (int i = 0; i < searchText.length(); i++) {
            char c = searchText.charAt(i);
            current = getNextState(current, c);

            // Check current node and output links for matches
            if (current.isEnd) {
                int start = i - current.depth + 1;
                String matchedText = text.substring(start, i + 1);
                return Optional.of(new PatternMatch(current.pattern, start, i + 1, matchedText));
            }
            TrieNode temp = current.outputLink;
            if (temp != null) {
                int start = i - temp.depth + 1;
                String matchedText = text.substring(start, i + 1);
                return Optional.of(new PatternMatch(temp.pattern, start, i + 1, matchedText));
            }
        }

        return Optional.empty();
    }

    /**
     * Checks if the text contains any pattern.
     * 检查文本是否包含任何模式。
     *
     * @param text the text to check | 要检查的文本
     * @return true if any pattern is found | 如果找到任何模式则返回true
     */
    public boolean containsAny(String text) {
        return findFirst(text).isPresent();
    }

    /**
     * Counts the total number of matches.
     * 统计匹配总数。
     *
     * @param text the text to search | 要搜索的文本
     * @return the number of matches | 匹配数
     */
    public int countMatches(String text) {
        return findAll(text).size();
    }

    /**
     * Gets all matched patterns (unique).
     * 获取所有匹配的模式（唯一）。
     *
     * @param text the text to search | 要搜索的文本
     * @return set of matched patterns | 匹配模式的集合
     */
    public Set<String> getMatchedPatterns(String text) {
        Set<String> matched = new LinkedHashSet<>();
        for (PatternMatch match : findAll(text)) {
            matched.add(match.pattern());
        }
        return matched;
    }

    // ==================== Replace Operations ====================

    /**
     * Replaces all matches with a replacement string.
     * 用替换字符串替换所有匹配。
     *
     * @param text        the text to process | 要处理的文本
     * @param replacement the replacement string | 替换字符串
     * @return the processed text | 处理后的文本
     */
    public String replaceAll(String text, String replacement) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        List<PatternMatch> matches = findAll(text);
        if (matches.isEmpty()) {
            return text;
        }

        // Sort by start position and merge overlapping
        matches = mergeOverlapping(matches, text);

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        for (PatternMatch match : matches) {
            result.append(text, lastEnd, match.start());
            result.append(replacement);
            lastEnd = match.end();
        }
        result.append(text.substring(lastEnd));

        return result.toString();
    }

    /**
     * Filters (masks) all matches with a mask character.
     * 用掩码字符过滤（屏蔽）所有匹配。
     *
     * @param text     the text to process | 要处理的文本
     * @param maskChar the mask character | 掩码字符
     * @return the filtered text | 过滤后的文本
     */
    public String filter(String text, char maskChar) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        List<PatternMatch> matches = findAll(text);
        if (matches.isEmpty()) {
            return text;
        }

        matches = mergeOverlapping(matches, text);

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        for (PatternMatch match : matches) {
            result.append(text, lastEnd, match.start());
            result.append(String.valueOf(maskChar).repeat(match.end() - match.start()));
            lastEnd = match.end();
        }
        result.append(text.substring(lastEnd));

        return result.toString();
    }

    /**
     * Filters with asterisks.
     * 用星号过滤。
     *
     * @param text the text to process | 要处理的文本
     * @return the filtered text | 过滤后的文本
     */
    public String filter(String text) {
        return filter(text, '*');
    }

    /**
     * Highlights all matches with tags.
     * 用标签高亮所有匹配。
     *
     * @param text     the text to process | 要处理的文本
     * @param startTag the start tag (e.g., "&lt;b&gt;") | 起始标签
     * @param endTag   the end tag (e.g., "&lt;/b&gt;") | 结束标签
     * @return the highlighted text | 高亮后的文本
     */
    public String highlight(String text, String startTag, String endTag) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        List<PatternMatch> matches = findAll(text);
        if (matches.isEmpty()) {
            return text;
        }

        matches = mergeOverlapping(matches, text);

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        for (PatternMatch match : matches) {
            result.append(text, lastEnd, match.start());
            result.append(startTag);
            result.append(text, match.start(), match.end());
            result.append(endTag);
            lastEnd = match.end();
        }
        result.append(text.substring(lastEnd));

        return result.toString();
    }

    // ==================== Utility Methods ====================

    /**
     * Returns the number of patterns.
     * 返回模式数量。
     *
     * @return pattern count | 模式数量
     */
    public int patternCount() {
        return patterns.size();
    }

    /**
     * Returns all patterns.
     * 返回所有模式。
     *
     * @return unmodifiable set of patterns | 不可修改的模式集合
     */
    public Set<String> getPatterns() {
        return patterns;
    }

    /**
     * Checks if a pattern exists.
     * 检查模式是否存在。
     *
     * @param pattern the pattern to check | 要检查的模式
     * @return true if exists | 如果存在则返回true
     */
    public boolean hasPattern(String pattern) {
        if (pattern == null) return false;
        return patterns.contains(ignoreCase ? pattern.toLowerCase() : pattern);
    }

    // ==================== Private Methods ====================

    private TrieNode buildTrie(Set<String> patterns) {
        TrieNode root = new TrieNode();

        for (String pattern : patterns) {
            if (pattern == null || pattern.isEmpty()) continue;

            String p = ignoreCase ? pattern.toLowerCase() : pattern;
            TrieNode current = root;

            for (int i = 0; i < p.length(); i++) {
                char c = p.charAt(i);
                current = current.children.computeIfAbsent(c, _ -> new TrieNode());
                current.depth = i + 1;
            }

            current.isEnd = true;
            current.pattern = pattern;  // Store original pattern
        }

        return root;
    }

    private void buildFailureLinks() {
        Queue<TrieNode> queue = new LinkedList<>();

        // Initialize first level
        for (TrieNode child : root.children.values()) {
            child.suffixLink = root;
            queue.add(child);
        }

        // BFS to build failure links
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();

            for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                char c = entry.getKey();
                TrieNode child = entry.getValue();
                queue.add(child);

                // Find failure link
                TrieNode fallback = current.suffixLink;
                while (fallback != null && !fallback.children.containsKey(c)) {
                    fallback = fallback.suffixLink;
                }

                child.suffixLink = (fallback == null) ? root : fallback.children.get(c);

                // Propagate output links
                if (child.suffixLink.isEnd) {
                    child.outputLink = child.suffixLink;
                } else {
                    child.outputLink = child.suffixLink.outputLink;
                }
            }
        }
    }

    private TrieNode getNextState(TrieNode current, char c) {
        while (current != null && !current.children.containsKey(c)) {
            current = current.suffixLink;
        }
        return (current == null) ? root : current.children.get(c);
    }

    private List<PatternMatch> mergeOverlapping(List<PatternMatch> matches, String originalText) {
        if (matches.size() <= 1) return matches;

        // Sort by start position
        List<PatternMatch> sorted = new ArrayList<>(matches);
        sorted.sort(Comparator.comparingInt(PatternMatch::start));

        List<PatternMatch> merged = new ArrayList<>();
        PatternMatch current = sorted.getFirst();

        for (int i = 1; i < sorted.size(); i++) {
            PatternMatch next = sorted.get(i);
            if (next.start() <= current.end()) {
                // Overlapping, merge by taking the longer span
                if (next.end() > current.end()) {
                    int newStart = current.start();
                    int newEnd = next.end();
                    current = new PatternMatch(
                            current.pattern() + "," + next.pattern(),
                            newStart,
                            newEnd,
                            originalText.substring(newStart, newEnd)
                    );
                }
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged;
    }

    // ==================== Inner Classes ====================

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        TrieNode suffixLink;
        TrieNode outputLink;
        boolean isEnd;
        String pattern;
        int depth;
    }

    /**
     * Builder for AhoCorasick.
     * AhoCorasick 的构建器。
     */
    public static final class Builder {
        private final Set<String> patterns = new HashSet<>();
        private boolean ignoreCase = false;

        private Builder() {
        }

        /**
         * Adds a pattern.
         * 添加模式。
         *
         * @param pattern the pattern to add | 要添加的模式
         * @return this builder | 此构建器
         */
        public Builder addPattern(String pattern) {
            if (pattern != null && !pattern.isEmpty()) {
                patterns.add(pattern);
            }
            return this;
        }

        /**
         * Adds multiple patterns.
         * 添加多个模式。
         *
         * @param patterns the patterns to add | 要添加的模式
         * @return this builder | 此构建器
         */
        public Builder addPatterns(Collection<String> patterns) {
            if (patterns != null) {
                patterns.stream()
                        .filter(p -> p != null && !p.isEmpty())
                        .forEach(this.patterns::add);
            }
            return this;
        }

        /**
         * Adds multiple patterns.
         * 添加多个模式。
         *
         * @param patterns the patterns to add | 要添加的模式
         * @return this builder | 此构建器
         */
        public Builder addPatterns(String... patterns) {
            return addPatterns(Arrays.asList(patterns));
        }

        /**
         * Sets whether to ignore case.
         * 设置是否忽略大小写。
         *
         * @param ignoreCase true to ignore case | true表示忽略大小写
         * @return this builder | 此构建器
         */
        public Builder ignoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        /**
         * Builds the AhoCorasick matcher.
         * 构建 AhoCorasick 匹配器。
         *
         * @return a new matcher | 新的匹配器
         */
        public AhoCorasick build() {
            if (patterns.isEmpty()) {
                throw new IllegalStateException("At least one pattern is required");
            }
            return new AhoCorasick(this);
        }
    }
}
