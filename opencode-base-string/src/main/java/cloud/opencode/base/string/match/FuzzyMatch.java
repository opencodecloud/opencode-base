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
 * Fuzzy Match Result - Represents a fuzzy matching result
 * 模糊匹配结果 - 表示一个模糊匹配的结果
 *
 * <p>Contains the matched item, its string key, and the similarity score.</p>
 * <p>包含匹配的项目、其字符串键和相似度分数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Score as percentage display - 分数百分比显示</li>
 *   <li>Match strength classification (exact, strong, weak) - 匹配强度分类</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FuzzyMatch<String> match = ...;
 * String item = match.item();    // The matched item
 * String key = match.key();      // The string used for matching
 * double score = match.score();  // Similarity score (0.0 - 1.0)
 * }</pre>
 *
 * @param item  the matched item | 匹配的项目
 * @param key   the string key used for matching | 用于匹配的字符串键
 * @param score the similarity score (0.0 - 1.0) | 相似度分数（0.0 - 1.0）
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (record is immutable) - 线程安全: 是（记录不可变）</li>
 * </ul>
 *
 * @param <T>   the type of the matched item | 匹配项目的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see FuzzyMatcher
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.2.0
 */
public record FuzzyMatch<T>(T item, String key, double score) {

    /**
     * Returns a formatted score as percentage.
     * 返回格式化为百分比的分数。
     *
     * @return score as percentage string | 百分比字符串形式的分数
     */
    public String scoreAsPercent() {
        return String.format("%.1f%%", score * 100);
    }

    /**
     * Checks if this is an exact match.
     * 检查是否为精确匹配。
     *
     * @return true if score is 1.0 | 如果分数为1.0则返回true
     */
    public boolean isExactMatch() {
        return score >= 0.9999;
    }

    /**
     * Checks if this is a strong match (score >= 0.8).
     * 检查是否为强匹配（分数 >= 0.8）。
     *
     * @return true if score >= 0.8 | 如果分数 >= 0.8则返回true
     */
    public boolean isStrongMatch() {
        return score >= 0.8;
    }

    /**
     * Checks if this is a weak match ({@code score < 0.6}).
     * 检查是否为弱匹配（{@code 分数 < 0.6}）。
     *
     * @return true if {@code score < 0.6} | 如果分数小于 0.6 则返回 true
     */
    public boolean isWeakMatch() {
        return score < 0.6;
    }
}
