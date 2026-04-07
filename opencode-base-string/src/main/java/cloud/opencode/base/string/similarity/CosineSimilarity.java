package cloud.opencode.base.string.similarity;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Cosine Similarity - Calculates cosine similarity between strings.
 * 余弦相似度 - 计算字符串之间的余弦相似度。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Word-vector-based cosine similarity - 基于词向量的余弦相似度</li>
 *   <li>Returns value in [0.0, 1.0] range - 返回[0.0, 1.0]范围的值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * double sim = CosineSimilarity.calculate("hello world", "hello java"); // ~0.5
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
public final class CosineSimilarity {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private CosineSimilarity() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static double calculate(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;
        
        Map<String, Integer> vec1 = getWordVector(s1);
        Map<String, Integer> vec2 = getWordVector(s2);
        
        double dotProduct = 0.0;
        double mag1 = 0.0;
        double mag2 = 0.0;

        // entrySet() avoids a second map lookup per word (vs keySet() + get()).
        // entrySet() 替代 keySet() + get()，每个词减少一次哈希查找。
        for (Map.Entry<String, Integer> e : vec1.entrySet()) {
            int count1 = e.getValue();
            Integer v2 = vec2.get(e.getKey());
            int count2 = v2 == null ? 0 : v2;
            dotProduct += (double) count1 * count2;
            mag1 += (double) count1 * count1;
        }

        for (int count : vec2.values()) {
            mag2 += (double) count * count;
        }
        
        if (mag1 == 0 || mag2 == 0) return 0.0;
        return dotProduct / (Math.sqrt(mag1) * Math.sqrt(mag2));
    }

    private static Map<String, Integer> getWordVector(String str) {
        Map<String, Integer> vector = new HashMap<>();
        String[] words = WHITESPACE_PATTERN.split(str.toLowerCase());
        for (String word : words) {
            if (!word.isEmpty()) {
                vector.put(word, vector.getOrDefault(word, 0) + 1);
            }
        }
        return vector;
    }
}
