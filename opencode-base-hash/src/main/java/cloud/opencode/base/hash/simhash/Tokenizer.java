package cloud.opencode.base.hash.simhash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Tokenizer interface for SimHash text processing
 * SimHash 文本处理的分词器接口
 *
 * <p>Defines how to split text into tokens for SimHash computation.
 * Provides built-in implementations for common tokenization strategies.</p>
 * <p>定义如何将文本拆分为用于 SimHash 计算的标记。
 * 提供常见分词策略的内置实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Whitespace tokenization - 空格分词</li>
 *   <li>N-gram tokenization - N-gram 分词</li>
 *   <li>Character tokenization - 字符分词</li>
 *   <li>Custom tokenization - 自定义分词</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Whitespace tokenizer
 * Tokenizer tokenizer = Tokenizer.whitespace();
 * List<String> tokens = tokenizer.tokenize("Hello World");
 *
 * // N-gram tokenizer
 * Tokenizer ngram = Tokenizer.ngram(3);
 * List<String> grams = ngram.tokenize("Hello");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(L) for whitespace/words/characters tokenization where L=input length; O(L) for ngram(n) producing L-n+1 tokens - 时间复杂度: whitespace/words/characters 分词为 O(L)，L 为输入长度；ngram(n) 生成 L-n+1 个 token，时间 O(L)</li>
 *   <li>Space complexity: O(L) for the resulting token list - 空间复杂度: 结果 token 列表为 O(L)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@FunctionalInterface
public interface Tokenizer extends Function<String, List<String>> {

    /**
     * Tokenizes the input text
     * 分词输入文本
     *
     * @param text input text | 输入文本
     * @return list of tokens | 标记列表
     */
    List<String> tokenize(String text);

    @Override
    default List<String> apply(String text) {
        return tokenize(text);
    }

    // ==================== Built-in Tokenizers | 内置分词器 ====================

    /**
     * Creates a whitespace tokenizer
     * 创建空格分词器
     *
     * @return whitespace tokenizer | 空格分词器
     */
    static Tokenizer whitespace() {
        return text -> {
            if (text == null || text.isEmpty()) {
                return List.of();
            }
            return Arrays.asList(text.split("\\s+"));
        };
    }

    /**
     * Creates an N-gram tokenizer
     * 创建 N-gram 分词器
     *
     * @param n gram size | gram 大小
     * @return n-gram tokenizer | n-gram 分词器
     */
    static Tokenizer ngram(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("N must be positive");
        }
        return text -> {
            if (text == null || text.length() < n) {
                return text == null ? List.of() : List.of(text);
            }
            List<String> grams = new ArrayList<>(text.length() - n + 1);
            for (int i = 0; i <= text.length() - n; i++) {
                grams.add(text.substring(i, i + n));
            }
            return grams;
        };
    }

    /**
     * Creates a character tokenizer
     * 创建字符分词器
     *
     * @return character tokenizer | 字符分词器
     */
    static Tokenizer characters() {
        return text -> {
            if (text == null || text.isEmpty()) {
                return List.of();
            }
            List<String> chars = new ArrayList<>(text.length());
            for (int i = 0; i < text.length(); i++) {
                chars.add(String.valueOf(text.charAt(i)));
            }
            return chars;
        };
    }

    /**
     * Creates a word boundary tokenizer (alphanumeric words)
     * 创建单词边界分词器（字母数字单词）
     *
     * @return word boundary tokenizer | 单词边界分词器
     */
    static Tokenizer words() {
        return text -> {
            if (text == null || text.isEmpty()) {
                return List.of();
            }
            List<String> words = new ArrayList<>();
            StringBuilder word = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    word.append(c);
                } else if (!word.isEmpty()) {
                    words.add(word.toString());
                    word.setLength(0);
                }
            }
            if (!word.isEmpty()) {
                words.add(word.toString());
            }
            return words;
        };
    }

    /**
     * Creates a Chinese character tokenizer (single characters for CJK)
     * 创建中文字符分词器（CJK单字符）
     *
     * @return Chinese character tokenizer | 中文字符分词器
     */
    static Tokenizer cjkCharacters() {
        return text -> {
            if (text == null || text.isEmpty()) {
                return List.of();
            }
            List<String> tokens = new ArrayList<>();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.isIdeographic(c)) {
                    tokens.add(String.valueOf(c));
                }
            }
            return tokens;
        };
    }

    /**
     * Combines multiple tokenizers
     * 组合多个分词器
     *
     * @param tokenizers tokenizers to combine | 要组合的分词器
     * @return combined tokenizer | 组合的分词器
     */
    static Tokenizer combined(Tokenizer... tokenizers) {
        return text -> {
            List<String> allTokens = new ArrayList<>();
            for (Tokenizer tokenizer : tokenizers) {
                allTokens.addAll(tokenizer.tokenize(text));
            }
            return allTokens;
        };
    }
}
