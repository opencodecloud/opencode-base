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

package cloud.opencode.base.lunar.element;

/**
 * WuXing (Five Elements) - Chinese traditional five elements
 * 五行 - 中国传统五行元素
 *
 * <p>Represents the five fundamental elements in Chinese philosophy:
 * Wood, Fire, Earth, Metal, and Water.</p>
 * <p>表示中国哲学中的五种基本元素：木、火、土、金、水。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Five element definitions with properties - 五行定义（含属性）</li>
 *   <li>Generating and overcoming relationships - 相生相克关系</li>
 *   <li>Color and direction associations - 颜色和方位关联</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * WuXing wood = WuXing.WOOD;
 * WuXing generated = wood.generates();  // FIRE
 * WuXing overcome = wood.overcomes();   // EARTH
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * <p><strong>Element Relationships | 五行相生相克:</strong></p>
 * <ul>
 *   <li>Generating (相生): Wood→Fire→Earth→Metal→Water→Wood</li>
 *   <li>Overcoming (相克): Wood→Earth→Water→Fire→Metal→Wood</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public enum WuXing {

    /**
     * Wood element | 木
     */
    WOOD("木", "青", "东"),

    /**
     * Fire element | 火
     */
    FIRE("火", "赤", "南"),

    /**
     * Earth element | 土
     */
    EARTH("土", "黄", "中"),

    /**
     * Metal element | 金
     */
    METAL("金", "白", "西"),

    /**
     * Water element | 水
     */
    WATER("水", "黑", "北");

    private final String chinese;
    private final String color;
    private final String direction;

    WuXing(String chinese, String color, String direction) {
        this.chinese = chinese;
        this.color = color;
        this.direction = direction;
    }

    /**
     * Get Chinese name
     * 获取中文名
     *
     * @return Chinese name | 中文名
     */
    public String getChinese() {
        return chinese;
    }

    /**
     * Get associated color
     * 获取关联颜色
     *
     * @return the color | 颜色
     */
    public String getColor() {
        return color;
    }

    /**
     * Get associated direction
     * 获取关联方位
     *
     * @return the direction | 方位
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Get the element this one generates (相生)
     * 获取此元素生成的元素
     *
     * @return the generated element | 生成的元素
     */
    public WuXing generates() {
        return switch (this) {
            case WOOD -> FIRE;
            case FIRE -> EARTH;
            case EARTH -> METAL;
            case METAL -> WATER;
            case WATER -> WOOD;
        };
    }

    /**
     * Get the element that generates this one (被生)
     * 获取生成此元素的元素
     *
     * @return the generating element | 生成它的元素
     */
    public WuXing generatedBy() {
        return switch (this) {
            case WOOD -> WATER;
            case FIRE -> WOOD;
            case EARTH -> FIRE;
            case METAL -> EARTH;
            case WATER -> METAL;
        };
    }

    /**
     * Get the element this one overcomes (相克)
     * 获取此元素克制的元素
     *
     * @return the overcome element | 被克制的元素
     */
    public WuXing overcomes() {
        return switch (this) {
            case WOOD -> EARTH;
            case FIRE -> METAL;
            case EARTH -> WATER;
            case METAL -> WOOD;
            case WATER -> FIRE;
        };
    }

    /**
     * Get the element that overcomes this one (被克)
     * 获取克制此元素的元素
     *
     * @return the overcoming element | 克制它的元素
     */
    public WuXing overcomeBy() {
        return switch (this) {
            case WOOD -> METAL;
            case FIRE -> WATER;
            case EARTH -> WOOD;
            case METAL -> FIRE;
            case WATER -> EARTH;
        };
    }

    /**
     * Get WuXing from Heavenly Stem (天干)
     * 从天干获取五行
     *
     * @param ganIndex the Heavenly Stem index (0-9) | 天干索引
     * @return the WuXing | 五行
     */
    public static WuXing fromGan(int ganIndex) {
        return switch (ganIndex % 10) {
            case 0, 1 -> WOOD;   // 甲乙
            case 2, 3 -> FIRE;   // 丙丁
            case 4, 5 -> EARTH;  // 戊己
            case 6, 7 -> METAL;  // 庚辛
            case 8, 9 -> WATER;  // 壬癸
            default -> throw new IllegalArgumentException("Invalid gan index: " + ganIndex);
        };
    }

    /**
     * Get WuXing from Earthly Branch (地支)
     * 从地支获取五行
     *
     * @param zhiIndex the Earthly Branch index (0-11) | 地支索引
     * @return the WuXing | 五行
     */
    public static WuXing fromZhi(int zhiIndex) {
        return switch (zhiIndex % 12) {
            case 2, 3 -> WOOD;   // 寅卯
            case 5, 6 -> FIRE;   // 巳午
            case 1, 4, 7, 10 -> EARTH; // 丑辰未戌
            case 8, 9 -> METAL;  // 申酉
            case 0, 11 -> WATER; // 子亥
            default -> throw new IllegalArgumentException("Invalid zhi index: " + zhiIndex);
        };
    }

    @Override
    public String toString() {
        return chinese;
    }
}
