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

/**
 * Fuzzy Match Facade - Provides convenient static methods for fuzzy matching
 * 模糊匹配门面 - 提供便捷的模糊匹配静态方法
 *
 * <p>Quick access to fuzzy matching functionality without building a matcher.</p>
 * <p>无需构建匹配器即可快速访问模糊匹配功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Quick fuzzy search - 快速模糊搜索</li>
 *   <li>Search suggestions - 搜索建议</li>
 *   <li>Find best match - 查找最佳匹配</li>
 *   <li>Typo correction - 拼写纠正</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Quick fuzzy search
 * List<String> results = OpenFuzzyMatch.search("aple",
 *     List.of("apple", "banana", "apply"));
 *
 * // Find best match
 * String best = OpenFuzzyMatch.findBest("javscript",
 *     List.of("Java", "JavaScript", "Python"));
 *
 * // Get suggestions with scores
 * List<FuzzyMatch<String>> suggestions = OpenFuzzyMatch.suggest("pythn",
 *     List.of("Python", "Ruby", "Perl"), 0.5);
 *
 * // Did you mean?
 * String correction = OpenFuzzyMatch.didYouMean("recieve",
 *     List.of("receive", "believe", "achieve"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n * m) where n, m = string lengths (Levenshtein) - O(n * m), n, m为字符串长度(编辑距离)</li>
 *   <li>Space complexity: O(min(n, m)) for DP row - 动态规划行 O(min(n, m))</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see FuzzyMatcher
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.2.0
 */
public final class OpenFuzzyMatch {

    private static final double DEFAULT_THRESHOLD = 0.6;
    private static final int DEFAULT_MAX_RESULTS = 10;

    private OpenFuzzyMatch() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ==================== Quick Search ====================

    /**
     * Performs a quick fuzzy search.
     * 执行快速模糊搜索。
     *
     * @param query      the search query | 搜索查询
     * @param candidates the candidates to search | 要搜索的候选项
     * @return list of matching strings | 匹配字符串列表
     */
    public static List<String> search(String query, Collection<String> candidates) {
        return search(query, candidates, DEFAULT_THRESHOLD);
    }

    /**
     * Performs a quick fuzzy search with custom threshold.
     * 使用自定义阈值执行快速模糊搜索。
     *
     * @param query      the search query | 搜索查询
     * @param candidates the candidates to search | 要搜索的候选项
     * @param threshold  the similarity threshold (0.0 - 1.0) | 相似度阈值
     * @return list of matching strings | 匹配字符串列表
     */
    public static List<String> search(String query, Collection<String> candidates, double threshold) {
        if (query == null || candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        return FuzzyMatcher.<String>builder()
                .addAll(candidates)
                .threshold(threshold)
                .maxResults(DEFAULT_MAX_RESULTS)
                .build()
                .suggestStrings(query);
    }

    /**
     * Performs fuzzy search on objects with custom key extractor.
     * 使用自定义键提取器对对象执行模糊搜索。
     *
     * @param query        the search query | 搜索查询
     * @param candidates   the candidates to search | 要搜索的候选项
     * @param keyExtractor function to extract search key | 提取搜索键的函数
     * @param <T>          the item type | 项目类型
     * @return list of matching items | 匹配项目列表
     */
    public static <T> List<T> search(String query, Collection<T> candidates,
                                     Function<T, String> keyExtractor) {
        return search(query, candidates, keyExtractor, DEFAULT_THRESHOLD);
    }

    /**
     * Performs fuzzy search on objects with custom key extractor and threshold.
     * 使用自定义键提取器和阈值对对象执行模糊搜索。
     *
     * @param query        the search query | 搜索查询
     * @param candidates   the candidates to search | 要搜索的候选项
     * @param keyExtractor function to extract search key | 提取搜索键的函数
     * @param threshold    the similarity threshold | 相似度阈值
     * @param <T>          the item type | 项目类型
     * @return list of matching items | 匹配项目列表
     */
    public static <T> List<T> search(String query, Collection<T> candidates,
                                     Function<T, String> keyExtractor, double threshold) {
        if (query == null || candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        return FuzzyMatcher.builder(keyExtractor)
                .addAll(candidates)
                .threshold(threshold)
                .maxResults(DEFAULT_MAX_RESULTS)
                .build()
                .suggest(query);
    }

    // ==================== Suggestions ====================

    /**
     * Gets search suggestions with match details.
     * 获取带有匹配详情的搜索建议。
     *
     * @param query      the search query | 搜索查询
     * @param candidates the candidates | 候选项
     * @param threshold  the similarity threshold | 相似度阈值
     * @return list of fuzzy matches | 模糊匹配列表
     */
    public static List<FuzzyMatch<String>> suggest(String query, Collection<String> candidates,
                                                   double threshold) {
        if (query == null || candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        return FuzzyMatcher.<String>builder()
                .addAll(candidates)
                .threshold(threshold)
                .maxResults(DEFAULT_MAX_RESULTS)
                .build()
                .match(query);
    }

    /**
     * Gets search suggestions with default threshold.
     * 使用默认阈值获取搜索建议。
     *
     * @param query      the search query | 搜索查询
     * @param candidates the candidates | 候选项
     * @return list of fuzzy matches | 模糊匹配列表
     */
    public static List<FuzzyMatch<String>> suggest(String query, Collection<String> candidates) {
        return suggest(query, candidates, DEFAULT_THRESHOLD);
    }

    // ==================== Best Match ====================

    /**
     * Finds the best matching string.
     * 查找最佳匹配字符串。
     *
     * @param query      the search query | 搜索查询
     * @param candidates the candidates | 候选项
     * @return the best match, or null if no match | 最佳匹配，如果没有匹配则返回null
     */
    public static String findBest(String query, Collection<String> candidates) {
        return findBest(query, candidates, DEFAULT_THRESHOLD);
    }

    /**
     * Finds the best matching string with custom threshold.
     * 使用自定义阈值查找最佳匹配字符串。
     *
     * @param query      the search query | 搜索查询
     * @param candidates the candidates | 候选项
     * @param threshold  the minimum similarity threshold | 最小相似度阈值
     * @return the best match, or null if no match above threshold | 最佳匹配，如果没有高于阈值的匹配则返回null
     */
    public static String findBest(String query, Collection<String> candidates, double threshold) {
        if (query == null || candidates == null || candidates.isEmpty()) {
            return null;
        }

        return FuzzyMatcher.<String>builder()
                .addAll(candidates)
                .threshold(threshold)
                .maxResults(1)
                .build()
                .matchBest(query)
                .map(FuzzyMatch::item)
                .orElse(null);
    }

    /**
     * Finds the best matching item with custom key extractor.
     * 使用自定义键提取器查找最佳匹配项。
     *
     * @param query        the search query | 搜索查询
     * @param candidates   the candidates | 候选项
     * @param keyExtractor function to extract search key | 提取搜索键的函数
     * @param <T>          the item type | 项目类型
     * @return the best match, or null if no match | 最佳匹配，如果没有匹配则返回null
     */
    public static <T> T findBest(String query, Collection<T> candidates,
                                 Function<T, String> keyExtractor) {
        if (query == null || candidates == null || candidates.isEmpty()) {
            return null;
        }

        return FuzzyMatcher.builder(keyExtractor)
                .addAll(candidates)
                .threshold(DEFAULT_THRESHOLD)
                .maxResults(1)
                .build()
                .matchBest(query)
                .map(FuzzyMatch::item)
                .orElse(null);
    }

    // ==================== Did You Mean ====================

    /**
     * Suggests a correction for a potentially misspelled string.
     * 为可能拼写错误的字符串建议更正。
     *
     * @param misspelled  the potentially misspelled string | 可能拼写错误的字符串
     * @param dictionary  the dictionary of correct strings | 正确字符串的词典
     * @return the suggested correction, or null if no good match | 建议的更正，如果没有好的匹配则返回null
     */
    public static String didYouMean(String misspelled, Collection<String> dictionary) {
        return didYouMean(misspelled, dictionary, 0.5);
    }

    /**
     * Suggests a correction with custom threshold.
     * 使用自定义阈值建议更正。
     *
     * @param misspelled  the potentially misspelled string | 可能拼写错误的字符串
     * @param dictionary  the dictionary of correct strings | 正确字符串的词典
     * @param threshold   the minimum similarity threshold | 最小相似度阈值
     * @return the suggested correction, or null if no good match | 建议的更正，如果没有好的匹配则返回null
     */
    public static String didYouMean(String misspelled, Collection<String> dictionary, double threshold) {
        if (misspelled == null || dictionary == null || dictionary.isEmpty()) {
            return null;
        }

        // Check if already in dictionary
        if (dictionary.contains(misspelled)) {
            return null;
        }

        return FuzzyMatcher.<String>builder()
                .addAll(dictionary)
                .threshold(threshold)
                .algorithm(FuzzyMatcher.MatchAlgorithm.JARO_WINKLER)
                .maxResults(1)
                .build()
                .matchBest(misspelled)
                .map(FuzzyMatch::item)
                .orElse(null);
    }

    // ==================== Similarity ====================

    /**
     * Calculates the fuzzy similarity between two strings.
     * 计算两个字符串之间的模糊相似度。
     *
     * @param s1 the first string | 第一个字符串
     * @param s2 the second string | 第二个字符串
     * @return similarity score (0.0 - 1.0) | 相似度分数（0.0 - 1.0）
     */
    public static double similarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;
        return LevenshteinDistance.similarity(s1, s2);
    }

    /**
     * Checks if two strings are similar above a threshold.
     * 检查两个字符串是否高于阈值相似。
     *
     * @param s1        the first string | 第一个字符串
     * @param s2        the second string | 第二个字符串
     * @param threshold the similarity threshold | 相似度阈值
     * @return true if similarity >= threshold | 如果相似度 >= 阈值则返回true
     */
    public static boolean isSimilar(String s1, String s2, double threshold) {
        return similarity(s1, s2) >= threshold;
    }

    // ==================== Ranking ====================

    /**
     * Ranks candidates by similarity to the query.
     * 按与查询的相似度对候选项排名。
     *
     * @param query      the search query | 搜索查询
     * @param candidates the candidates to rank | 要排名的候选项
     * @return candidates sorted by similarity (descending) | 按相似度降序排列的候选项
     */
    public static List<String> rankBySimilarity(String query, Collection<String> candidates) {
        if (query == null || candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        return candidates.stream()
                .map(c -> new AbstractMap.SimpleEntry<>(c, similarity(query, c)))
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(AbstractMap.SimpleEntry::getKey)
                .toList();
    }

    /**
     * Ranks candidates by similarity with scores.
     * 按相似度对候选项排名并附带分数。
     *
     * @param query      the search query | 搜索查询
     * @param candidates the candidates to rank | 要排名的候选项
     * @return map of candidate to similarity score | 候选项到相似度分数的映射
     */
    public static Map<String, Double> rankWithScores(String query, Collection<String> candidates) {
        if (query == null || candidates == null || candidates.isEmpty()) {
            return Map.of();
        }

        Map<String, Double> scores = new LinkedHashMap<>();
        candidates.stream()
                .map(c -> new AbstractMap.SimpleEntry<>(c, similarity(query, c)))
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .forEach(e -> scores.put(e.getKey(), e.getValue()));
        return scores;
    }
}
