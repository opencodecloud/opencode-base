package cloud.opencode.base.string.similarity;

import java.util.*;

/**
 * String Similarity Facade - Unified entry point for string similarity calculations.
 * 字符串相似度门面 - 字符串相似度计算的统一入口。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Levenshtein distance and similarity - Levenshtein距离和相似度</li>
 *   <li>Jaccard similarity with configurable N-gram - Jaccard相似度可配置N-gram</li>
 *   <li>Cosine similarity - 余弦相似度</li>
 *   <li>Jaro-Winkler similarity - Jaro-Winkler相似度</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * double lev = OpenSimilarity.levenshteinSimilarity("hello", "hallo");
 * double jac = OpenSimilarity.jaccardSimilarity("abc", "abd");
 * double cos = OpenSimilarity.cosineSimilarity("hello world", "hello java");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Partial (depends on algorithm) - 空值安全: 部分（取决于算法）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenSimilarity {
    private OpenSimilarity() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static int levenshteinDistance(String s1, String s2) {
        return LevenshteinDistance.calculate(s1, s2);
    }

    public static double levenshteinSimilarity(String s1, String s2) {
        return LevenshteinDistance.similarity(s1, s2);
    }

    /**
     * Bounded Levenshtein distance with early termination.
     * 带阈值的有界Levenshtein距离，提前终止。
     *
     * <p>Returns the edit distance if {@code <= threshold}, otherwise {@code -1}.</p>
     * <p>如果编辑距离 {@code <= threshold} 则返回距离，否则返回 {@code -1}。</p>
     *
     * @param s1        the first string | 第一个字符串
     * @param s2        the second string | 第二个字符串
     * @param threshold the maximum acceptable distance | 最大可接受距离
     * @return the edit distance if {@code <= threshold}, otherwise {@code -1} |
     *         如果编辑距离 {@code <= threshold} 则返回距离，否则返回 {@code -1}
     */
    public static int boundedLevenshteinDistance(String s1, String s2, int threshold) {
        return LevenshteinDistance.boundedDistance(s1, s2, threshold);
    }

    public static double jaccardSimilarity(String s1, String s2) {
        return JaccardSimilarity.calculate(s1, s2);
    }

    public static double jaccardSimilarity(String s1, String s2, int nGram) {
        return JaccardSimilarity.calculate(s1, s2, nGram);
    }

    public static double cosineSimilarity(String s1, String s2) {
        return CosineSimilarity.calculate(s1, s2);
    }

    public static double jaroWinklerSimilarity(String s1, String s2) {
        // Simplified Jaro-Winkler implementation
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;
        
        int len1 = s1.length();
        int len2 = s2.length();
        int matchWindow = Math.max(len1, len2) / 2 - 1;
        
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
        
        // Winkler modification
        int prefix = 0;
        for (int i = 0; i < Math.min(Math.min(len1, len2), 4); i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        }
        
        return jaro + (prefix * 0.1 * (1.0 - jaro));
    }

    public static int longestCommonSubsequence(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    public static int longestCommonSubstring(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        
        int maxLen = 0;
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    maxLen = Math.max(maxLen, dp[i][j]);
                }
            }
        }
        
        return maxLen;
    }

    public static boolean isSimilar(String s1, String s2, double threshold) {
        return levenshteinSimilarity(s1, s2) >= threshold;
    }

    public static String findMostSimilar(String target, List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) return null;
        
        String best = null;
        double maxSimilarity = -1;
        
        for (String candidate : candidates) {
            double similarity = levenshteinSimilarity(target, candidate);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                best = candidate;
            }
        }
        
        return best;
    }

    public static List<String> findSimilar(String target, List<String> candidates, double threshold) {
        if (candidates == null) return List.of();
        
        return candidates.stream()
            .filter(c -> levenshteinSimilarity(target, c) >= threshold)
            .toList();
    }
}
