/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.string.unicode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Chinese Word Segmenter - 中文分词器
 *
 * <p>Provides basic Chinese text segmentation using dictionary-based and rule-based approaches.</p>
 * <p>提供基于词典和规则的基础中文文本分词功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Forward Maximum Matching (FMM) - 正向最大匹配</li>
 *   <li>Backward Maximum Matching (BMM) - 逆向最大匹配</li>
 *   <li>Bidirectional Maximum Matching - 双向最大匹配</li>
 *   <li>Custom dictionary support - 自定义词典支持</li>
 *   <li>Mixed Chinese/English text handling - 中英文混合处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic segmentation
 * List<String> words = ChineseSegmenter.segment("我爱中华人民共和国");
 * // Result: ["我", "爱", "中华人民共和国"]
 *
 * // With custom dictionary
 * ChineseSegmenter segmenter = ChineseSegmenter.builder()
 *     .addWord("人工智能")
 *     .addWord("机器学习")
 *     .maxWordLength(8)
 *     .build();
 * List<String> words = segmenter.segment("人工智能和机器学习");
 *
 * // Different algorithms
 * List<String> fmm = ChineseSegmenter.segmentFMM("研究生命起源");
 * List<String> bmm = ChineseSegmenter.segmentBMM("研究生命起源");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable instance, ConcurrentHashMap dictionary) - 线程安全: 是（不可变实例，ConcurrentHashMap词典）</li>
 *   <li>Null-safe: No (input text must not be null) - 空值安全: 否（输入文本不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see OpenChinese
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class ChineseSegmenter {

    /** Default maximum word length */
    private static final int DEFAULT_MAX_WORD_LENGTH = 6;

    /** Core dictionary - common Chinese words */
    private static final Set<String> CORE_DICTIONARY = ConcurrentHashMap.newKeySet();

    /** Default singleton instance */
    private static volatile ChineseSegmenter defaultInstance;

    /** Instance dictionary (includes core + custom words) */
    private final Set<String> dictionary;

    /** Maximum word length for matching */
    private final int maxWordLength;

    /** Whether to keep punctuation */
    private final boolean keepPunctuation;

    /** Whether to keep numbers as single tokens */
    private final boolean keepNumbers;

    // Initialize core dictionary with common words
    static {
        initializeCoreDictionary();
    }

    private ChineseSegmenter(Builder builder) {
        this.dictionary = new HashSet<>(CORE_DICTIONARY);
        this.dictionary.addAll(builder.customWords);
        this.maxWordLength = builder.maxWordLength;
        this.keepPunctuation = builder.keepPunctuation;
        this.keepNumbers = builder.keepNumbers;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the default segmenter instance.
     * 获取默认分词器实例。
     *
     * @return the default segmenter - 默认分词器
     */
    public static ChineseSegmenter getDefault() {
        if (defaultInstance == null) {
            synchronized (ChineseSegmenter.class) {
                if (defaultInstance == null) {
                    defaultInstance = builder().build();
                }
            }
        }
        return defaultInstance;
    }

    // ==================== Quick Segmentation Methods | 快速分词方法 ====================

    /**
     * Segments Chinese text using bidirectional maximum matching (default).
     * 使用双向最大匹配分词（默认算法）。
     *
     * @param text the text to segment - 待分词文本
     * @return list of segmented words - 分词结果列表
     */
    public static List<String> segment(String text) {
        return getDefault().segmentText(text);
    }

    /**
     * Segments Chinese text using Forward Maximum Matching (FMM).
     * 使用正向最大匹配算法分词。
     *
     * @param text the text to segment - 待分词文本
     * @return list of segmented words - 分词结果列表
     */
    public static List<String> segmentFMM(String text) {
        return getDefault().segmentForward(text);
    }

    /**
     * Segments Chinese text using Backward Maximum Matching (BMM).
     * 使用逆向最大匹配算法分词。
     *
     * @param text the text to segment - 待分词文本
     * @return list of segmented words - 分词结果列表
     */
    public static List<String> segmentBMM(String text) {
        return getDefault().segmentBackward(text);
    }

    /**
     * Joins segmented words with a delimiter.
     * 使用分隔符连接分词结果。
     *
     * @param text the text to segment - 待分词文本
     * @param delimiter the delimiter - 分隔符
     * @return joined string - 连接后的字符串
     */
    public static String segmentAndJoin(String text, String delimiter) {
        return String.join(delimiter, segment(text));
    }

    // ==================== Instance Segmentation Methods | 实例分词方法 ====================

    /**
     * Segments text using bidirectional maximum matching.
     * 使用双向最大匹配分词。
     *
     * @param text the text to segment - 待分词文本
     * @return list of segmented words - 分词结果列表
     */
    public List<String> segmentText(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> fmmResult = segmentForward(text);
        List<String> bmmResult = segmentBackward(text);

        // Choose the result with fewer segments (generally better)
        // If equal, prefer the one with fewer single-character words
        if (fmmResult.size() != bmmResult.size()) {
            return fmmResult.size() < bmmResult.size() ? fmmResult : bmmResult;
        }

        long fmmSingle = fmmResult.stream().filter(w -> w.length() == 1 && OpenChinese.isChinese(w.charAt(0))).count();
        long bmmSingle = bmmResult.stream().filter(w -> w.length() == 1 && OpenChinese.isChinese(w.charAt(0))).count();

        return fmmSingle <= bmmSingle ? fmmResult : bmmResult;
    }

    /**
     * Segments text using Forward Maximum Matching (FMM).
     * 使用正向最大匹配算法分词。
     *
     * @param text the text to segment - 待分词文本
     * @return list of segmented words - 分词结果列表
     */
    public List<String> segmentForward(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        int i = 0;

        while (i < text.length()) {
            char c = text.charAt(i);

            // Handle non-Chinese characters
            if (!OpenChinese.isChinese(c)) {
                String token = extractNonChineseToken(text, i);
                if (shouldKeepToken(token)) {
                    result.add(token);
                }
                i += token.length();
                continue;
            }

            // Forward Maximum Matching for Chinese
            int maxLen = Math.min(maxWordLength, text.length() - i);
            String word = null;

            for (int len = maxLen; len >= 1; len--) {
                String candidate = text.substring(i, i + len);
                if (dictionary.contains(candidate) || len == 1) {
                    word = candidate;
                    break;
                }
            }

            if (word != null) {
                result.add(word);
                i += word.length();
            } else {
                result.add(String.valueOf(c));
                i++;
            }
        }

        return result;
    }

    /**
     * Segments text using Backward Maximum Matching (BMM).
     * 使用逆向最大匹配算法分词。
     *
     * @param text the text to segment - 待分词文本
     * @return list of segmented words - 分词结果列表
     */
    public List<String> segmentBackward(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedList<String> result = new LinkedList<>();
        int i = text.length();

        while (i > 0) {
            char c = text.charAt(i - 1);

            // Handle non-Chinese characters (scan backward)
            if (!OpenChinese.isChinese(c)) {
                int end = i;
                while (i > 0 && !OpenChinese.isChinese(text.charAt(i - 1))) {
                    i--;
                }
                String token = text.substring(i, end);
                if (shouldKeepToken(token)) {
                    result.addFirst(token);
                }
                continue;
            }

            // Backward Maximum Matching for Chinese
            int maxLen = Math.min(maxWordLength, i);
            String word = null;

            for (int len = maxLen; len >= 1; len--) {
                String candidate = text.substring(i - len, i);
                if (dictionary.contains(candidate) || len == 1) {
                    word = candidate;
                    break;
                }
            }

            if (word != null) {
                result.addFirst(word);
                i -= word.length();
            } else {
                result.addFirst(String.valueOf(c));
                i--;
            }
        }

        return new ArrayList<>(result);
    }

    // ==================== Dictionary Methods | 词典方法 ====================

    /**
     * Checks if a word exists in the dictionary.
     * 检查词语是否在词典中。
     *
     * @param word the word to check - 待检查词语
     * @return true if exists - 如果存在返回true
     */
    public boolean containsWord(String word) {
        return dictionary.contains(word);
    }

    /**
     * Gets the dictionary size.
     * 获取词典大小。
     *
     * @return the dictionary size - 词典大小
     */
    public int getDictionarySize() {
        return dictionary.size();
    }

    /**
     * Adds a word to the core dictionary (affects all instances).
     * 向核心词典添加词语（影响所有实例）。
     *
     * @param word the word to add - 待添加词语
     */
    public static void addToDictionary(String word) {
        if (word != null && !word.isEmpty()) {
            CORE_DICTIONARY.add(word);
        }
    }

    /**
     * Adds multiple words to the core dictionary.
     * 向核心词典添加多个词语。
     *
     * @param words the words to add - 待添加词语集合
     */
    public static void addToDictionary(Collection<String> words) {
        if (words != null) {
            words.stream()
                    .filter(w -> w != null && !w.isEmpty())
                    .forEach(CORE_DICTIONARY::add);
        }
    }

    /**
     * Loads words from an input stream (one word per line).
     * 从输入流加载词语（每行一个词）。
     *
     * @param inputStream the input stream - 输入流
     * @throws IOException if reading fails - 如果读取失败
     */
    public static void loadDictionary(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(CORE_DICTIONARY::add);
        }
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private String extractNonChineseToken(String text, int start) {
        StringBuilder sb = new StringBuilder();
        int i = start;

        while (i < text.length()) {
            char c = text.charAt(i);
            if (OpenChinese.isChinese(c)) {
                break;
            }
            sb.append(c);
            i++;
        }

        return sb.toString();
    }

    private boolean shouldKeepToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String trimmed = token.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        // Check if it's punctuation
        if (isPunctuation(trimmed)) {
            return keepPunctuation;
        }

        // Check if it's a number
        if (isNumber(trimmed)) {
            return keepNumbers;
        }

        return true;
    }

    private boolean isPunctuation(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isWhitespace(c) &&
                    (Character.isLetterOrDigit(c) || OpenChinese.isChinese(c))) {
                return false;
            }
        }
        return true;
    }

    private boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void initializeCoreDictionary() {
        // Common Chinese words (high-frequency)
        String[] commonWords = {
                // Pronouns - 代词
                "我们", "你们", "他们", "她们", "它们", "大家", "自己", "什么", "怎么", "哪里",
                // Time - 时间
                "今天", "明天", "昨天", "现在", "以后", "以前", "时候", "时间", "一会儿",
                // Numbers - 数量
                "一个", "两个", "三个", "几个", "很多", "一些", "所有", "没有",
                // Verbs - 动词
                "可以", "需要", "应该", "能够", "必须", "希望", "知道", "认为", "觉得", "发现",
                "开始", "结束", "继续", "完成", "进行", "使用", "提供", "包括", "属于",
                // Adjectives - 形容词
                "重要", "简单", "复杂", "不同", "相同", "正确", "错误", "可能", "必要",
                // Prepositions/Conjunctions - 介词/连词
                "因为", "所以", "但是", "而且", "或者", "如果", "虽然", "不过", "然后",
                "关于", "对于", "通过", "根据", "按照",
                // Common nouns - 常用名词
                "问题", "方法", "系统", "数据", "信息", "内容", "结果", "工作", "技术",
                "功能", "服务", "产品", "公司", "用户", "客户", "项目", "过程", "情况",
                // IT/Tech terms - 技术术语
                "程序", "代码", "软件", "硬件", "网络", "数据库", "服务器", "接口", "模块",
                "算法", "框架", "平台", "应用", "开发", "测试", "部署", "配置", "文件",
                // Countries/Places - 国家/地点
                "中国", "中华", "北京", "上海", "广州", "深圳", "美国", "日本", "韩国",
                "中华人民共和国", "人民共和国",
                // Academic - 学术
                "研究", "分析", "设计", "实现", "理论", "实践", "方案", "标准", "规范",
                // Business - 商务
                "管理", "运营", "市场", "销售", "财务", "人力", "资源", "战略", "规划",
                // Common phrases - 常用短语
                "不是", "就是", "只是", "已经", "正在", "还是", "一样", "一起", "一直"
        };

        CORE_DICTIONARY.addAll(Arrays.asList(commonWords));
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for ChineseSegmenter.
     * ChineseSegmenter 构建器。
     */
    public static final class Builder {
        private final Set<String> customWords = new HashSet<>();
        private int maxWordLength = DEFAULT_MAX_WORD_LENGTH;
        private boolean keepPunctuation = false;
        private boolean keepNumbers = true;

        private Builder() {}

        /**
         * Adds a custom word to the dictionary.
         * 添加自定义词语。
         *
         * @param word the word - 词语
         * @return this builder
         */
        public Builder addWord(String word) {
            if (word != null && !word.isEmpty()) {
                customWords.add(word);
            }
            return this;
        }

        /**
         * Adds multiple custom words.
         * 添加多个自定义词语。
         *
         * @param words the words - 词语集合
         * @return this builder
         */
        public Builder addWords(Collection<String> words) {
            if (words != null) {
                words.stream()
                        .filter(w -> w != null && !w.isEmpty())
                        .forEach(customWords::add);
            }
            return this;
        }

        /**
         * Sets the maximum word length for matching.
         * 设置最大词语匹配长度。
         *
         * @param length the max length - 最大长度
         * @return this builder
         */
        public Builder maxWordLength(int length) {
            this.maxWordLength = Math.max(1, length);
            return this;
        }

        /**
         * Sets whether to keep punctuation in results.
         * 设置是否保留标点符号。
         *
         * @param keep whether to keep - 是否保留
         * @return this builder
         */
        public Builder keepPunctuation(boolean keep) {
            this.keepPunctuation = keep;
            return this;
        }

        /**
         * Sets whether to keep numbers in results.
         * 设置是否保留数字。
         *
         * @param keep whether to keep - 是否保留
         * @return this builder
         */
        public Builder keepNumbers(boolean keep) {
            this.keepNumbers = keep;
            return this;
        }

        /**
         * Builds the segmenter.
         * 构建分词器。
         *
         * @return the segmenter - 分词器
         */
        public ChineseSegmenter build() {
            return new ChineseSegmenter(this);
        }
    }
}
