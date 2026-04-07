package cloud.opencode.base.string.similarity;

import java.util.*;

/**
 * Jaccard Similarity - Calculates Jaccard similarity coefficient between strings.
 * Jaccard相似度 - 计算字符串之间的Jaccard相似系数。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>N-gram based Jaccard coefficient - 基于N-gram的Jaccard系数</li>
 *   <li>Configurable N-gram size (default 2) - 可配置N-gram大小（默认2）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * double sim = JaccardSimilarity.calculate("hello", "hallo"); // ~0.5
 * double sim3 = JaccardSimilarity.calculate("abc", "abd", 3);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns 0.0 for null) - 空值安全: 是（null返回0.0）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class JaccardSimilarity {
    private JaccardSimilarity() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static double calculate(String s1, String s2) {
        return calculate(s1, s2, 2);
    }

    public static double calculate(String s1, String s2, int nGram) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;

        Set<String> set1 = getNGrams(s1, nGram);
        Set<String> set2 = getNGrams(s2, nGram);

        if (set1.isEmpty() && set2.isEmpty()) return 1.0;

        // Compute |intersection| by counting, then derive |union| = |A| + |B| - |intersection|.
        // Avoids allocating two full HashSet copies just for size().
        // 通过计数求 |交集|，再推导 |并集| = |A| + |B| - |交集|，避免分配两个 HashSet 副本。
        int intersectionSize = 0;
        for (String ng : set1) {
            if (set2.contains(ng)) {
                intersectionSize++;
            }
        }
        int unionSize = set1.size() + set2.size() - intersectionSize;
        if (unionSize == 0) return 1.0;
        return (double) intersectionSize / unionSize;
    }

    private static Set<String> getNGrams(String str, int n) {
        Set<String> ngrams = new HashSet<>();
        for (int i = 0; i <= str.length() - n; i++) {
            ngrams.add(str.substring(i, i + n));
        }
        return ngrams;
    }
}
