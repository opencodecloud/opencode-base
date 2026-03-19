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
