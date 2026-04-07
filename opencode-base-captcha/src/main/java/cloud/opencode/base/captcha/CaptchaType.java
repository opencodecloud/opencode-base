package cloud.opencode.base.captcha;

/**
 * Captcha Type - Enumeration of supported CAPTCHA types
 * 验证码类型 - 支持的验证码类型枚举
 *
 * <p>This enum defines all available CAPTCHA generation types.</p>
 * <p>此枚举定义了所有可用的验证码生成类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Text-based types: NUMERIC, ALPHA, ALPHANUMERIC, ARITHMETIC, CHINESE - 文本类型</li>
 *   <li>Animated type: GIF - 动画类型</li>
 *   <li>Interactive types: SLIDER, CLICK, ROTATE, IMAGE_SELECT, JIGSAW - 交互式类型</li>
 *   <li>Audio type: AUDIO - 音频类型</li>
 *   <li>Invisible type: POW - 无感类型</li>
 *   <li>Type classification helpers (isInteractive, isTextBased, isAudio, isInvisible) - 类型分类辅助方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaType type = CaptchaType.ALPHANUMERIC;
 * boolean interactive = type.isInteractive(); // false
 * boolean textBased = type.isTextBased();     // true
 *
 * Captcha captcha = OpenCaptcha.create(type);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public enum CaptchaType {

    /**
     * Numeric CAPTCHA (digits only)
     * 数字验证码（仅数字）
     */
    NUMERIC,

    /**
     * Alphabetic CAPTCHA (letters only)
     * 字母验证码（仅字母）
     */
    ALPHA,

    /**
     * Alphanumeric CAPTCHA (letters and digits)
     * 字母数字验证码（字母和数字）
     */
    ALPHANUMERIC,

    /**
     * Arithmetic CAPTCHA (math expression)
     * 算术验证码（数学表达式）
     */
    ARITHMETIC,

    /**
     * Chinese CAPTCHA (Chinese characters)
     * 中文验证码（中文字符）
     */
    CHINESE,

    /**
     * GIF animated CAPTCHA
     * GIF 动画验证码
     */
    GIF,

    /**
     * Slider CAPTCHA (drag to verify)
     * 滑块验证码（拖动验证）
     */
    SLIDER,

    /**
     * Click CAPTCHA (click specific areas)
     * 点击验证码（点击特定区域）
     */
    CLICK,

    /**
     * Rotate CAPTCHA (rotate to correct angle)
     * 旋转验证码（旋转到正确角度）
     */
    ROTATE,

    /**
     * Image selection CAPTCHA (select matching images)
     * 图片选择验证码（选择匹配的图片）
     */
    IMAGE_SELECT,

    /**
     * Audio CAPTCHA (audio challenge)
     * 音频验证码（音频挑战）
     */
    AUDIO,

    /**
     * Jigsaw CAPTCHA (puzzle piece ordering)
     * 拼接验证码（碎片排序）
     */
    JIGSAW,

    /**
     * Proof-of-Work CAPTCHA (invisible computation)
     * 工作量证明验证码（无感计算）
     */
    POW;

    /**
     * Checks if this type is an interactive CAPTCHA.
     * 检查此类型是否为交互式验证码。
     *
     * @return true if interactive | 如果是交互式则返回 true
     */
    public boolean isInteractive() {
        return this == SLIDER || this == CLICK || this == ROTATE || this == IMAGE_SELECT || this == JIGSAW;
    }

    /**
     * Checks if this type is a text-based CAPTCHA.
     * 检查此类型是否为文本验证码。
     *
     * @return true if text-based | 如果是文本类型则返回 true
     */
    public boolean isTextBased() {
        return this == NUMERIC || this == ALPHA || this == ALPHANUMERIC ||
               this == ARITHMETIC || this == CHINESE;
    }

    /**
     * Checks if this type is an audio CAPTCHA.
     * 检查是否为音频验证码。
     *
     * @return true if audio | 如果是音频验证码则返回 true
     */
    public boolean isAudio() {
        return this == AUDIO;
    }

    /**
     * Checks if this type is invisible (no user interaction).
     * 检查是否为无感验证码。
     *
     * @return true if invisible | 如果是无感验证码则返回 true
     */
    public boolean isInvisible() {
        return this == POW;
    }
}
