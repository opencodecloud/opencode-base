package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Test Captcha Generator - Generates CAPTCHAs with a predictable fixed answer for unit testing
 * 测试验证码生成器 - 生成具有可预测固定答案的验证码，用于单元测试
 *
 * <p>This generator produces CAPTCHAs with a known, fixed answer and minimal image data,
 * making it ideal for unit testing validation logic without requiring actual image rendering.</p>
 * <p>此生成器生成具有已知固定答案和最小图像数据的验证码，非常适合在不需要实际图像渲染的情况下
 * 对验证逻辑进行单元测试。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deterministic answer for repeatable tests - 确定性答案用于可重复测试</li>
 *   <li>Minimal image data (empty byte array) to avoid rendering overhead - 最小图像数据（空字节数组）避免渲染开销</li>
 *   <li>Respects config expiration time - 遵循配置的过期时间</li>
 *   <li>Metadata includes test marker for easy identification - 元数据包含测试标记便于识别</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator generator = new TestCaptchaGenerator("ABC123");
 * Captcha captcha = generator.generate(CaptchaConfig.defaults());
 * assert "ABC123".equals(captcha.answer());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable state) - 线程安全: 是（不可变状态）</li>
 *   <li>Null-safe: No (fixedAnswer must not be null) - 空值安全: 否（fixedAnswer 不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class TestCaptchaGenerator implements CaptchaGenerator {

    private final String fixedAnswer;

    /**
     * Constructs a TestCaptchaGenerator with the given fixed answer.
     * 使用给定的固定答案构造测试验证码生成器。
     *
     * @param fixedAnswer the fixed answer for all generated CAPTCHAs | 所有生成验证码的固定答案
     * @throws NullPointerException if fixedAnswer is null | 如果 fixedAnswer 为 null
     */
    public TestCaptchaGenerator(String fixedAnswer) {
        Objects.requireNonNull(fixedAnswer, "fixedAnswer must not be null");
        this.fixedAnswer = fixedAnswer;
    }

    /**
     * Generates a CAPTCHA with the fixed answer and minimal image data.
     * 使用固定答案和最小图像数据生成验证码。
     *
     * @param config the configuration (expiration time is respected) | 配置（遵循过期时间）
     * @return the generated CAPTCHA with fixed answer | 具有固定答案的生成验证码
     * @throws NullPointerException if config is null | 如果 config 为 null
     */
    @Override
    public Captcha generate(CaptchaConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        Instant now = Instant.now();
        return new Captcha(
            UUID.randomUUID().toString(),
            CaptchaType.ALPHANUMERIC,
            new byte[0],
            fixedAnswer,
            Map.of("test", true),
            now,
            now.plus(config.getExpireTime())
        );
    }

    /**
     * Gets the supported CAPTCHA type.
     * 获取支持的验证码类型。
     *
     * @return {@link CaptchaType#ALPHANUMERIC} | 字母数字类型
     */
    @Override
    public CaptchaType getType() {
        return CaptchaType.ALPHANUMERIC;
    }

    /**
     * Returns the fixed answer configured for this generator.
     * 返回此生成器配置的固定答案。
     *
     * @return the fixed answer | 固定答案
     */
    public String getFixedAnswer() {
        return fixedAnswer;
    }
}
