package cloud.opencode.base.captcha;

import cloud.opencode.base.captcha.generator.CaptchaGenerator;
import cloud.opencode.base.captcha.renderer.CaptchaRenderer;
import cloud.opencode.base.captcha.store.CaptchaStore;
import cloud.opencode.base.captcha.store.HashedCaptchaStore;
import cloud.opencode.base.captcha.support.CaptchaMetrics;
import cloud.opencode.base.captcha.validator.CaptchaValidator;
import cloud.opencode.base.captcha.validator.HashedCaptchaValidator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OpenCaptcha - Main entry point for CAPTCHA operations
 * OpenCaptcha - 验证码操作的主入口点
 *
 * <p>This class provides a simple API for generating and validating CAPTCHAs.</p>
 * <p>此类提供生成和验证验证码的简单 API。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple usage
 * Captcha captcha = OpenCaptcha.create();
 * String base64 = captcha.toBase64DataUrl();
 *
 * // With configuration
 * Captcha captcha = OpenCaptcha.create(CaptchaConfig.builder()
 *     .type(CaptchaType.ARITHMETIC)
 *     .width(200)
 *     .height(80)
 *     .build());
 *
 * // Using builder for advanced usage
 * OpenCaptcha openCaptcha = OpenCaptcha.builder()
 *     .store(CaptchaStore.memory())
 *     .config(CaptchaConfig.defaults())
 *     .build();
 *
 * Captcha captcha = openCaptcha.generate();
 * ValidationResult result = openCaptcha.validate(id, answer);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Static factory methods for quick CAPTCHA creation - 用于快速创建验证码的静态工厂方法</li>
 *   <li>Builder pattern for advanced configuration - 构建器模式用于高级配置</li>
 *   <li>Integrated store and validator lifecycle - 集成的存储和验证器生命周期</li>
 *   <li>Support for all CAPTCHA types (text, GIF, interactive) - 支持所有验证码类型（文本、GIF、交互式）</li>
 *   <li>Optional metrics collection (CaptchaMetrics) - 可选的指标收集</li>
 *   <li>Optional event listener (CaptchaEventListener) - 可选的事件监听器</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe components) - 线程安全: 是（委托给线程安全组件）</li>
 *   <li>Null-safe: No (parameters must be non-null) - 空值安全: 否（参数不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class OpenCaptcha {

    private final CaptchaStore store;
    private final CaptchaConfig config;
    private final CaptchaValidator validator;
    private final CaptchaMetrics metrics;
    private final CaptchaEventListener eventListener;

    private OpenCaptcha(Builder builder) {
        this.store = builder.store;
        this.config = builder.config;
        this.validator = builder.validator != null
            ? builder.validator
            : (store instanceof HashedCaptchaStore hashed
                ? new HashedCaptchaValidator(hashed)
                : CaptchaValidator.simple(store));
        this.metrics = builder.metrics;
        this.eventListener = builder.eventListener;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a CAPTCHA with default configuration.
     * 使用默认配置创建验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha create() {
        return create(CaptchaConfig.defaults());
    }

    /**
     * Creates a CAPTCHA with the specified configuration.
     * 使用指定配置创建验证码。
     *
     * @param config the configuration | 配置
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha create(CaptchaConfig config) {
        CaptchaGenerator generator = CaptchaGenerator.forType(config.getType());
        return generator.generate(config);
    }

    /**
     * Creates a CAPTCHA of the specified type.
     * 创建指定类型的验证码。
     *
     * @param type the CAPTCHA type | 验证码类型
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha create(CaptchaType type) {
        return create(CaptchaConfig.builder().type(type).build());
    }

    /**
     * Creates a numeric CAPTCHA.
     * 创建数字验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha numeric() {
        return create(CaptchaType.NUMERIC);
    }

    /**
     * Creates an alphabetic CAPTCHA.
     * 创建字母验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha alpha() {
        return create(CaptchaType.ALPHA);
    }

    /**
     * Creates an alphanumeric CAPTCHA.
     * 创建字母数字验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha alphanumeric() {
        return create(CaptchaType.ALPHANUMERIC);
    }

    /**
     * Creates an arithmetic CAPTCHA.
     * 创建算术验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha arithmetic() {
        return create(CaptchaType.ARITHMETIC);
    }

    /**
     * Creates a Chinese CAPTCHA.
     * 创建中文验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha chinese() {
        return create(CaptchaType.CHINESE);
    }

    /**
     * Creates a GIF CAPTCHA.
     * 创建 GIF 验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha gif() {
        return create(CaptchaType.GIF);
    }

    /**
     * Creates a slider CAPTCHA.
     * 创建滑块验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha slider() {
        return create(CaptchaType.SLIDER);
    }

    /**
     * Creates a click CAPTCHA.
     * 创建点击验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha click() {
        return create(CaptchaType.CLICK);
    }

    /**
     * Creates a rotate CAPTCHA.
     * 创建旋转验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha rotate() {
        return create(CaptchaType.ROTATE);
    }

    /**
     * Creates an audio CAPTCHA.
     * 创建音频验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha audio() {
        return create(CaptchaType.AUDIO);
    }

    /**
     * Creates a jigsaw CAPTCHA.
     * 创建拼接验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha jigsaw() {
        return create(CaptchaType.JIGSAW);
    }

    /**
     * Creates a PoW CAPTCHA.
     * 创建工作量证明验证码。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public static Captcha pow() {
        return create(CaptchaType.POW);
    }

    // ==================== Instance Methods ====================

    /**
     * Generates a CAPTCHA and stores it.
     * 生成验证码并存储。
     *
     * @return the generated CAPTCHA | 生成的验证码
     */
    public Captcha generate() {
        return generate(config);
    }

    /**
     * Generates a CAPTCHA with configuration and stores it.
     * 使用配置生成验证码并存储。
     *
     * @param config the configuration | 配置
     * @return the generated CAPTCHA | 生成的验证码
     */
    public Captcha generate(CaptchaConfig config) {
        Captcha captcha = create(config);
        store.store(captcha.id(), captcha.answer(), config.getExpireTime());
        if (metrics != null) {
            metrics.recordGeneration(config.getType());
        }
        if (eventListener != null) {
            try { eventListener.onGenerated(captcha); } catch (Exception ignored) { }
        }
        return captcha;
    }

    /**
     * Validates a CAPTCHA answer.
     * 验证验证码答案。
     *
     * @param id     the CAPTCHA ID | 验证码 ID
     * @param answer the provided answer | 提供的答案
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(String id, String answer) {
        ValidationResult result = validator.validate(id, answer, config.isCaseSensitive());
        if (metrics != null) {
            metrics.recordValidation(result.success());
        }
        if (eventListener != null) {
            try {
                if (result.success()) {
                    eventListener.onValidationSuccess(id);
                } else {
                    eventListener.onValidationFailure(id, result.code());
                }
            } catch (Exception ignored) { }
        }
        return result;
    }

    /**
     * Renders a CAPTCHA to an output stream.
     * 将验证码渲染到输出流。
     *
     * @param captcha the CAPTCHA | 验证码
     * @param out     the output stream | 输出流
     * @throws IOException if rendering fails | 如果渲染失败
     */
    public void render(Captcha captcha, OutputStream out) throws IOException {
        CaptchaRenderer renderer;
        if (captcha.type() != null && captcha.type().isAudio()) {
            renderer = CaptchaRenderer.audio();
        } else if (captcha.type() == CaptchaType.GIF) {
            renderer = CaptchaRenderer.gif();
        } else {
            renderer = CaptchaRenderer.image();
        }
        renderer.render(captcha, out);
    }

    /**
     * Gets the store.
     * 获取存储。
     *
     * @return the store | 存储
     */
    public CaptchaStore getStore() {
        return store;
    }

    /**
     * Gets the configuration.
     * 获取配置。
     *
     * @return the configuration | 配置
     */
    public CaptchaConfig getConfig() {
        return config;
    }

    /**
     * Gets the metrics collector, if configured.
     * 获取指标收集器（如果已配置）。
     *
     * @return the metrics collector, or null | 指标收集器，或 null
     */
    public CaptchaMetrics getMetrics() {
        return metrics;
    }

    /**
     * Gets the event listener, if configured.
     * 获取事件监听器（如果已配置）。
     *
     * @return the event listener, or null | 事件监听器，或 null
     */
    public CaptchaEventListener getEventListener() {
        return eventListener;
    }

    // ==================== Builder ====================

    /**
     * Creates a builder for OpenCaptcha.
     * 创建 OpenCaptcha 的构建器。
     *
     * @return a new builder | 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * OpenCaptcha Builder
     * OpenCaptcha 构建器
     */
    public static final class Builder {
        private CaptchaStore store = CaptchaStore.memory();
        private CaptchaConfig config = CaptchaConfig.defaults();
        private CaptchaValidator validator;
        private CaptchaMetrics metrics;
        private CaptchaEventListener eventListener;

        private Builder() {}

        /**
         * Sets the CAPTCHA store.
         * 设置验证码存储。
         *
         * @param store the store | 存储
         * @return this builder | 此构建器
         */
        public Builder store(CaptchaStore store) {
            this.store = store;
            return this;
        }

        /**
         * Sets the configuration.
         * 设置配置。
         *
         * @param config the configuration | 配置
         * @return this builder | 此构建器
         */
        public Builder config(CaptchaConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Sets the CAPTCHA type.
         * 设置验证码类型。
         *
         * @param type the type | 类型
         * @return this builder | 此构建器
         */
        public Builder type(CaptchaType type) {
            this.config = config.toBuilder().type(type).build();
            return this;
        }

        /**
         * Sets a custom validator.
         * 设置自定义验证器。
         *
         * @param validator the validator | 验证器
         * @return this builder | 此构建器
         */
        public Builder validator(CaptchaValidator validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Sets the metrics collector.
         * 设置指标收集器。
         *
         * @param metrics the metrics collector | 指标收集器
         * @return this builder | 此构建器
         */
        public Builder metrics(CaptchaMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        /**
         * Sets the event listener.
         * 设置事件监听器。
         *
         * @param eventListener the event listener | 事件监听器
         * @return this builder | 此构建器
         */
        public Builder eventListener(CaptchaEventListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        /**
         * Builds the OpenCaptcha instance.
         * 构建 OpenCaptcha 实例。
         *
         * @return the OpenCaptcha instance | OpenCaptcha 实例
         * @throws IllegalStateException if a HashedCaptchaStore is paired with a
         *         non-HashedCaptchaValidator (use auto-detection or CaptchaValidator.hashed()) |
         *         如果 HashedCaptchaStore 搭配了非 HashedCaptchaValidator（请使用自动检测或 CaptchaValidator.hashed()）
         */
        public OpenCaptcha build() {
            if (store instanceof HashedCaptchaStore && validator != null
                    && !(validator instanceof HashedCaptchaValidator)) {
                throw new IllegalStateException(
                    "HashedCaptchaStore requires HashedCaptchaValidator; " +
                    "omit .validator() for auto-detection, or use CaptchaValidator.hashed(store)");
            }
            return new OpenCaptcha(this);
        }
    }
}
