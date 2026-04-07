package cloud.opencode.base.string.similarity;

/**
 * Levenshtein Distance - Calculates edit distance between strings.
 * 编辑距离 - 计算字符串之间的编辑距离。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Edit distance calculation - 编辑距离计算</li>
 *   <li>Similarity ratio calculation - 相似度比率计算</li>
 *   <li>O(n*m) time, O(m) space optimized - O(n*m)时间，O(m)空间优化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * int distance = LevenshteinDistance.calculate("kitten", "sitting"); // 3
 * double similarity = LevenshteinDistance.similarity("hello", "hallo"); // 0.8
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (throws IllegalArgumentException for null) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class LevenshteinDistance {

    /**
     * Maximum string length accepted by {@link #calculate} to prevent CPU/memory DoS.
     * 防止 CPU/内存 DoS，{@link #calculate} 接受的最大字符串长度。
     */
    private static final int MAX_INPUT_LENGTH = 10_000;

    private LevenshteinDistance() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static int calculate(String s1, String s2) {
        if (s1 == null) {
            throw new IllegalArgumentException("First string must not be null");
        }
        if (s2 == null) {
            throw new IllegalArgumentException("Second string must not be null");
        }
        if (s1.length() > MAX_INPUT_LENGTH || s2.length() > MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException(
                    "Input strings must not exceed " + MAX_INPUT_LENGTH + " characters"
                            + " (s1=" + s1.length() + ", s2=" + s2.length() + ")");
        }
        if (s1.equals(s2)) return 0;
        
        int len1 = s1.length();
        int len2 = s2.length();
        
        if (len1 == 0) return len2;
        if (len2 == 0) return len1;
        
        int[] prev = new int[len2 + 1];
        int[] curr = new int[len2 + 1];
        
        for (int j = 0; j <= len2; j++) {
            prev[j] = j;
        }
        
        for (int i = 1; i <= len1; i++) {
            curr[0] = i;
            for (int j = 1; j <= len2; j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        
        return prev[len2];
    }

    /**
     * Bounded Levenshtein distance with early termination.
     * 带阈值的有界Levenshtein距离，提前终止。
     *
     * <p>Returns the edit distance if {@code <= threshold}, otherwise {@code -1}.
     * Uses a banded DP algorithm that only computes a diagonal strip of width
     * {@code 2*threshold+1}, achieving O(min(m,n)*threshold) time.</p>
     * <p>如果编辑距离 {@code <= threshold} 则返回距离，否则返回 {@code -1}。
     * 使用带状DP算法，只计算对角线附近 {@code 2*threshold+1} 宽度的区域，
     * 时间复杂度为 O(min(m,n)*threshold)。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * boundedDistance("kitten", "sitting", 3) = 3
     * boundedDistance("kitten", "sitting", 2) = -1
     * boundedDistance("abc", "abc", 0)        = 0
     * </pre>
     *
     * <p><strong>Performance | 性能:</strong></p>
     * <p>Time: O(min(m,n) * threshold), Space: O(min(m,n))</p>
     *
     * @param s1        the first string | 第一个字符串
     * @param s2        the second string | 第二个字符串
     * @param threshold the maximum acceptable distance | 最大可接受距离
     * @return the edit distance if {@code <= threshold}, otherwise {@code -1} |
     *         如果编辑距离 {@code <= threshold} 则返回距离，否则返回 {@code -1}
     * @throws IllegalArgumentException if either string is null or threshold is negative |
     *                                  如果字符串为null或阈值为负数
     */
    public static int boundedDistance(String s1, String s2, int threshold) {
        if (s1 == null) {
            throw new IllegalArgumentException("First string must not be null");
        }
        if (s2 == null) {
            throw new IllegalArgumentException("Second string must not be null");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must not be negative: " + threshold);
        }
        if (s1.equals(s2)) return 0;

        int len1 = s1.length();
        int len2 = s2.length();

        // If the length difference alone exceeds the threshold, return -1
        if (Math.abs(len1 - len2) > threshold) {
            return -1;
        }

        if (len1 == 0) return len2 <= threshold ? len2 : -1;
        if (len2 == 0) return len1 <= threshold ? len1 : -1;

        // Ensure len1 <= len2 to use less memory
        if (len1 > len2) {
            String tmp = s1;
            s1 = s2;
            s2 = tmp;
            int t = len1;
            len1 = len2;
            len2 = t;
        }

        int[] prev = new int[len1 + 1];
        int[] curr = new int[len1 + 1];

        // Initialize: only positions within threshold of the diagonal
        for (int i = 0; i <= Math.min(len1, threshold); i++) {
            prev[i] = i;
        }
        for (int i = threshold + 1; i <= len1; i++) {
            prev[i] = threshold + 1; // sentinel: exceeds threshold
        }

        for (int j = 1; j <= len2; j++) {
            int jBandStart = Math.max(1, j - threshold);
            int jBandEnd = Math.min(len1, j + threshold);

            // Fill out-of-band positions with sentinel
            if (jBandStart > 1) {
                curr[jBandStart - 1] = threshold + 1;
            }
            curr[0] = j;

            int rowMin = curr[0] <= threshold ? curr[0] : threshold + 1;

            for (int i = jBandStart; i <= jBandEnd; i++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                int insert = curr[i - 1] + 1;
                int delete = prev[i] + 1;
                int replace = prev[i - 1] + cost;
                curr[i] = Math.min(Math.min(insert, delete), replace);
                if (curr[i] < rowMin) {
                    rowMin = curr[i];
                }
            }

            // Fill positions after band with sentinel
            if (jBandEnd < len1) {
                curr[jBandEnd + 1] = threshold + 1;
            }

            // Early termination: if minimum in the row exceeds threshold
            if (rowMin > threshold) {
                return -1;
            }

            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[len1] <= threshold ? prev[len1] : -1;
    }

    public static double similarity(String s1, String s2) {
        if (s1 == null) {
            throw new IllegalArgumentException("First string must not be null");
        }
        if (s2 == null) {
            throw new IllegalArgumentException("Second string must not be null");
        }
        int distance = calculate(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        return maxLen == 0 ? 1.0 : 1.0 - ((double) distance / maxLen);
    }
}
