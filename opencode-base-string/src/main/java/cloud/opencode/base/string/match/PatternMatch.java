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

/**
 * Pattern Match Result - Represents a pattern match found in text
 * 模式匹配结果 - 表示在文本中找到的模式匹配
 *
 * <p>Contains the matched pattern, its position, and the actual matched text.</p>
 * <p>包含匹配的模式、位置和实际匹配的文本。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Match position and length tracking - 匹配位置和长度跟踪</li>
 *   <li>Overlap detection between matches - 匹配重叠检测</li>
 *   <li>Text extraction from original source - 从原始源提取文本</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PatternMatch match = ...;
 * String pattern = match.pattern();         // The pattern that matched
 * int start = match.start();                // Start index (inclusive)
 * int end = match.end();                    // End index (exclusive)
 * String text = match.matchedText();        // The actual matched text
 * int length = match.length();              // Length of the match
 * }</pre>
 *
 * @param pattern     the pattern that was matched | 匹配的模式
 * @param start       the start index in the text (inclusive) | 在文本中的起始索引（包含）
 * @param end         the end index in the text (exclusive) | 在文本中的结束索引（不包含）
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (record is immutable) - 线程安全: 是（记录不可变）</li>
 * </ul>
 *
 * @param matchedText the actual text that was matched | 实际匹配的文本
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see AhoCorasick
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.2.0
 */
public record PatternMatch(String pattern, int start, int end, String matchedText) {

    /**
     * Returns the length of the match.
     * 返回匹配的长度。
     *
     * @return match length | 匹配长度
     */
    public int length() {
        return end - start;
    }

    /**
     * Checks if this match overlaps with another.
     * 检查此匹配是否与另一个重叠。
     *
     * @param other the other match | 另一个匹配
     * @return true if overlapping | 如果重叠则返回true
     */
    public boolean overlaps(PatternMatch other) {
        return this.start < other.end && other.start < this.end;
    }

    /**
     * Checks if this match contains another.
     * 检查此匹配是否包含另一个。
     *
     * @param other the other match | 另一个匹配
     * @return true if this contains other | 如果此匹配包含另一个则返回true
     */
    public boolean contains(PatternMatch other) {
        return this.start <= other.start && this.end >= other.end;
    }

    /**
     * Extracts the matched portion from the original text.
     * 从原始文本中提取匹配部分。
     *
     * @param originalText the original text | 原始文本
     * @return the matched portion | 匹配部分
     */
    public String extractFrom(String originalText) {
        if (originalText == null || start < 0 || end > originalText.length()) {
            return matchedText;
        }
        return originalText.substring(start, end);
    }

    /**
     * Returns a formatted string representation.
     * 返回格式化的字符串表示。
     *
     * @return formatted string | 格式化字符串
     */
    public String toDisplayString() {
        return String.format("'%s' at [%d-%d]", matchedText, start, end);
    }
}
