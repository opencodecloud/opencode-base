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

import cloud.opencode.base.string.similarity.LevenshteinDistance;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fuzzy String Matcher - Provides fuzzy matching and search suggestions
 * 模糊字符串匹配器 - 提供模糊匹配和搜索建议功能
 *
 * <p>Supports multiple fuzzy matching algorithms including Levenshtein distance,
 * Jaro-Winkler similarity, and prefix matching.</p>
 * <p>支持多种模糊匹配算法，包括 Levenshtein 距离、Jaro-Winkler 相似度和前缀匹配。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fuzzy search with customizable threshold - 可自定义阈值的模糊搜索</li>
 *   <li>Search suggestions with ranking - 带排名的搜索建议</li>
 *   <li>Typo tolerance - 错误容忍</li>
 *   <li>Multiple matching algorithms - 多种匹配算法</li>
 *   <li>Case-insensitive matching - 不区分大小写匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build a fuzzy matcher
 * FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
 *     .addAll(List.of("apple", "application", "banana", "apply"))
 *     .threshold(0.6)
 *     .maxResults(5)
 *     .build();
 *
 * // Find matches
 * List<FuzzyMatch<String>> results = matcher.match("aple");
 * // Returns: apple, apply, application (sorted by similarity)
 *
 * // Get suggestions
 * List<String> suggestions = matcher.suggest("app");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param <T> the type of items to match | 要匹配的项目类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.2.0
 */
public final class FuzzyMatcher<T> {

    private final List<T> items;
    private final Function<T, String> keyExtractor;
    private final double threshold;
    private final int maxResults;
    private final boolean ignoreCase;
    private final MatchAlgorithm algorithm;

    private FuzzyMatcher(Builder<T> builder) {
        this.items = new ArrayList<>(builder.items);
        this.keyExtractor = builder.keyExtractor;
        this.threshold = builder.threshold;
        this.maxResults = builder.maxResults;
        this.ignoreCase = builder.ignoreCase;
        this.algorithm = builder.algorithm;
    }

    /**
     * Creates a new builder for String items.
     * 为字符串项创建新的构建器。
     *
     * @return a new builder | 新的构建器
     */
    public static Builder<String> builder() {
        return new Builder<>(Function.identity());
    }

    /**
     * Creates a new builder with custom key extractor.
     * 使用自定义键提取器创建新的构建器。
     *
     * @param keyExtractor function to extract string key from item | 从项目中提取字符串键的函数
     * @param <T>          the item type | 项目类型
     * @return a new builder | 新的构建器
     */
    public static <T> Builder<T> builder(Function<T, String> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor must not be null");
        return new Builder<>(keyExtractor);
    }

    /**
     * Finds all items that match the query above the threshold.
     * 查找所有匹配度高于阈值的项目。
     *
     * @param query the search query | 搜索查询
     * @return list of matches sorted by similarity (descending) | 按相似度降序排列的匹配列表
     */
    public List<FuzzyMatch<T>> match(String query) {
        if (query == null || query.isEmpty()) {
            return List.of();
        }

        String normalizedQuery = ignoreCase ? query.toLowerCase() : query;

        return items.stream()
                .map(item -> {
                    String key = keyExtractor.apply(item);
                    String normalizedKey = ignoreCase ? key.toLowerCase() : key;
                    double score = calculateSimilarity(normalizedQuery, normalizedKey);
                    return new FuzzyMatch<>(item, key, score);
                })
                .filter(match -> match.score() >= threshold)
                .sorted(Comparator.comparingDouble(FuzzyMatch<T>::score).reversed())
                .limit(maxResults)
                .toList();
    }

    /**
     * Finds the best matching item.
     * 查找最佳匹配项。
     *
     * @param query the search query | 搜索查询
     * @return the best match, or empty if no match above threshold | 最佳匹配，如果没有高于阈值的匹配则为空
     */
    public Optional<FuzzyMatch<T>> matchBest(String query) {
        List<FuzzyMatch<T>> matches = match(query);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.getFirst());
    }

    /**
     * Gets search suggestions for the query.
     * 获取查询的搜索建议。
     *
     * @param query the search query | 搜索查询
     * @return list of suggested items | 建议项目列表
     */
    public List<T> suggest(String query) {
        return match(query).stream()
                .map(FuzzyMatch::item)
                .toList();
    }

    /**
     * Gets search suggestions as strings.
     * 获取字符串形式的搜索建议。
     *
     * @param query the search query | 搜索查询
     * @return list of suggested strings | 建议字符串列表
     */
    public List<String> suggestStrings(String query) {
        return match(query).stream()
                .map(FuzzyMatch::key)
                .toList();
    }

    /**
     * Checks if any item matches the query.
     * 检查是否有任何项目匹配查询。
     *
     * @param query the search query | 搜索查询
     * @return true if at least one match exists | 如果至少存在一个匹配则返回true
     */
    public boolean hasMatch(String query) {
        return matchBest(query).isPresent();
    }

    /**
     * Returns the number of items in the matcher.
     * 返回匹配器中的项目数。
     *
     * @return item count | 项目数
     */
    public int size() {
        return items.size();
    }

    private double calculateSimilarity(String s1, String s2) {
        return switch (algorithm) {
            case LEVENSHTEIN -> LevenshteinDistance.similarity(s1, s2);
            case JARO_WINKLER -> jaroWinklerSimilarity(s1, s2);
            case CONTAINS -> containsSimilarity(s1, s2);
            case PREFIX -> prefixSimilarity(s1, s2);
            case COMBINED -> combinedSimilarity(s1, s2);
        };
    }

    private double jaroWinklerSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;

        int len1 = s1.length();
        int len2 = s2.length();
        int matchWindow = Math.max(len1, len2) / 2 - 1;
        if (matchWindow < 0) matchWindow = 0;

        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];

        int matches = 0;
        int transpositions = 0;

        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchWindow);
            int end = Math.min(i + matchWindow + 1, len2);

            for (int j = start; j < end; j++) {
                if (s2Matches[j] || s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = s2Matches[j] = true;
                matches++;
                break;
            }
        }

        if (matches == 0) return 0.0;

        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }

        double jaro = (matches / (double) len1 + matches / (double) len2 +
                (matches - transpositions / 2.0) / matches) / 3.0;

        int prefix = 0;
        for (int i = 0; i < Math.min(Math.min(len1, len2), 4); i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        }

        return jaro + (prefix * 0.1 * (1.0 - jaro));
    }

    private double containsSimilarity(String query, String target) {
        if (target.contains(query)) {
            return 1.0 - (double) (target.length() - query.length()) / target.length();
        }
        return 0.0;
    }

    private double prefixSimilarity(String query, String target) {
        if (target.startsWith(query)) {
            return 1.0;
        }
        int commonPrefix = 0;
        int minLen = Math.min(query.length(), target.length());
        for (int i = 0; i < minLen; i++) {
            if (query.charAt(i) == target.charAt(i)) {
                commonPrefix++;
            } else {
                break;
            }
        }
        return (double) commonPrefix / query.length();
    }

    private double combinedSimilarity(String query, String target) {
        double levenshtein = LevenshteinDistance.similarity(query, target);
        double prefix = prefixSimilarity(query, target);
        double contains = containsSimilarity(query, target);

        // Weighted combination
        return levenshtein * 0.5 + prefix * 0.3 + contains * 0.2;
    }

    /**
     * Matching algorithm enumeration.
     * 匹配算法枚举。
     */
    public enum MatchAlgorithm {
        /**
         * Levenshtein distance based similarity
         */
        LEVENSHTEIN,
        /**
         * Jaro-Winkler similarity (better for typos)
         */
        JARO_WINKLER,
        /**
         * Contains-based similarity
         */
        CONTAINS,
        /**
         * Prefix-based similarity
         */
        PREFIX,
        /**
         * Combined algorithm (weighted combination)
         */
        COMBINED
    }

    /**
     * Builder for FuzzyMatcher.
     * FuzzyMatcher 的构建器。
     *
     * @param <T> the item type | 项目类型
     */
    public static final class Builder<T> {
        private final List<T> items = new ArrayList<>();
        private final Function<T, String> keyExtractor;
        private double threshold = 0.6;
        private int maxResults = 10;
        private boolean ignoreCase = true;
        private MatchAlgorithm algorithm = MatchAlgorithm.COMBINED;

        private Builder(Function<T, String> keyExtractor) {
            this.keyExtractor = keyExtractor;
        }

        /**
         * Adds an item to the matcher.
         * 向匹配器添加项目。
         *
         * @param item the item to add | 要添加的项目
         * @return this builder | 此构建器
         */
        public Builder<T> add(T item) {
            if (item != null) {
                items.add(item);
            }
            return this;
        }

        /**
         * Adds multiple items to the matcher.
         * 向匹配器添加多个项目。
         *
         * @param items the items to add | 要添加的项目
         * @return this builder | 此构建器
         */
        public Builder<T> addAll(Collection<? extends T> items) {
            if (items != null) {
                this.items.addAll(items.stream().filter(Objects::nonNull).toList());
            }
            return this;
        }

        /**
         * Sets the similarity threshold (0.0 - 1.0).
         * 设置相似度阈值（0.0 - 1.0）。
         *
         * @param threshold the threshold | 阈值
         * @return this builder | 此构建器
         */
        public Builder<T> threshold(double threshold) {
            if (threshold < 0.0 || threshold > 1.0) {
                throw new IllegalArgumentException("threshold must be between 0.0 and 1.0");
            }
            this.threshold = threshold;
            return this;
        }

        /**
         * Sets the maximum number of results.
         * 设置最大结果数。
         *
         * @param maxResults the max results | 最大结果数
         * @return this builder | 此构建器
         */
        public Builder<T> maxResults(int maxResults) {
            if (maxResults < 1) {
                throw new IllegalArgumentException("maxResults must be at least 1");
            }
            this.maxResults = maxResults;
            return this;
        }

        /**
         * Sets whether to ignore case.
         * 设置是否忽略大小写。
         *
         * @param ignoreCase true to ignore case | true表示忽略大小写
         * @return this builder | 此构建器
         */
        public Builder<T> ignoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        /**
         * Sets the matching algorithm.
         * 设置匹配算法。
         *
         * @param algorithm the algorithm | 算法
         * @return this builder | 此构建器
         */
        public Builder<T> algorithm(MatchAlgorithm algorithm) {
            this.algorithm = Objects.requireNonNull(algorithm);
            return this;
        }

        /**
         * Builds the FuzzyMatcher.
         * 构建 FuzzyMatcher。
         *
         * @return a new FuzzyMatcher | 新的 FuzzyMatcher
         */
        public FuzzyMatcher<T> build() {
            return new FuzzyMatcher<>(this);
        }
    }
}
