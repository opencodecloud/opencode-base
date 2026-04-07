package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.security.CaptchaSecurity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Proof-of-Work Captcha Generator — Generates computational challenge for invisible verification
 * 工作量证明验证码生成器 — 生成计算挑战用于无感验证
 *
 * <p>This generator creates a cryptographic challenge that requires the client to find a nonce
 * such that SHA-256(challenge + nonce) has a specified number of leading zero bits.</p>
 * <p>此生成器创建一个加密挑战，要求客户端找到一个 nonce，
 * 使得 SHA-256(challenge + nonce) 具有指定数量的前导零位。</p>
 *
 * <p>PoW verification is invisible to users — the client-side JavaScript performs
 * the computation automatically. A difficulty of 20 requires approximately 2^20 (~1M) hash
 * operations, taking ~100ms-500ms on modern hardware.</p>
 * <p>PoW 验证对用户不可见 — 客户端 JavaScript 自动执行计算。
 * 难度为 20 大约需要 2^20（~100万）次哈希操作，在现代硬件上大约需要 100ms-500ms。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Invisible verification (no user interaction required) - 无感验证（无需用户交互）</li>
 *   <li>Configurable difficulty via leading zero bits - 通过前导零位配置难度</li>
 *   <li>Cryptographically secure challenge generation - 加密安全的挑战生成</li>
 *   <li>No image data (zero-byte imageData) - 无图像数据（零字节 imageData）</li>
 *   <li>SHA-256 based proof-of-work - 基于 SHA-256 的工作量证明</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator generator = new PowCaptchaGenerator();
 * CaptchaConfig config = CaptchaConfig.builder()
 *     .powDifficulty(20)
 *     .expireTime(Duration.ofMinutes(5))
 *     .build();
 * Captcha captcha = generator.generate(config);
 *
 * // Client receives challenge and difficulty from metadata
 * String challenge = (String) captcha.getMetadata("challenge");
 * int difficulty = (int) captcha.getMetadata("difficulty");
 * // Client finds nonce such that SHA-256(challenge + nonce) has 'difficulty' leading zeros
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, no shared mutable state) - 线程安全: 是（无状态，无共享可变状态）</li>
 *   <li>Null-safe: No (config must be non-null) - 空值安全: 否（config 不能为空）</li>
 *   <li>Challenge uniqueness guaranteed by {@link CaptchaSecurity#generateSecureId()} -
 *       挑战唯一性由 {@link CaptchaSecurity#generateSecureId()} 保证</li>
 * </ul>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>Server-side generation is O(1) - 服务端生成为 O(1)</li>
 *   <li>Client-side solving is O(2^difficulty) on average - 客户端求解平均为 O(2^difficulty)</li>
 *   <li>Server-side validation is O(1) (single hash computation) - 服务端验证为 O(1)（单次哈希计算）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class PowCaptchaGenerator extends AbstractCaptchaGenerator implements CaptchaGenerator {

    /**
     * The hash algorithm used for proof-of-work.
     * 用于工作量证明的哈希算法。
     */
    private static final String ALGORITHM = "SHA-256";

    /**
     * {@inheritDoc}
     */
    @Override
    public CaptchaType getType() {
        return CaptchaType.POW;
    }

    /**
     * Generates a PoW CAPTCHA challenge.
     * 生成 PoW 验证码挑战。
     *
     * <p>The generated {@link Captcha} contains:</p>
     * <p>生成的 {@link Captcha} 包含：</p>
     * <ul>
     *   <li>{@code imageData}: empty byte array (no image for PoW) - 空字节数组（PoW 无图像）</li>
     *   <li>{@code answer}: "{challenge}:{difficulty}" format for validation -
     *       "{challenge}:{difficulty}" 格式用于验证</li>
     *   <li>{@code metadata}: contains "challenge", "difficulty", and "algorithm" fields -
     *       包含 "challenge"、"difficulty" 和 "algorithm" 字段</li>
     * </ul>
     *
     * @param config the configuration (uses {@code powDifficulty} and {@code expireTime}) |
     *               配置（使用 {@code powDifficulty} 和 {@code expireTime}）
     * @return the generated PoW CAPTCHA | 生成的 PoW 验证码
     */
    @Override
    public Captcha generate(CaptchaConfig config) {
        // 1. Generate a cryptographically secure random challenge
        String challenge = CaptchaSecurity.generateSecureId();

        // 2. Get difficulty from config
        int difficulty = config.getPowDifficulty();

        // 3. PoW does not produce an image
        byte[] imageData = new byte[0];

        // 4. Store challenge:difficulty as the answer for later validation
        String answer = challenge + ":" + difficulty;

        // 5. Build metadata with challenge parameters for client consumption
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("challenge", challenge);
        metadata.put("difficulty", difficulty);
        metadata.put("algorithm", ALGORITHM);

        String id = generateId();
        Instant now = Instant.now();
        return new Captcha(id, CaptchaType.POW, imageData, answer, metadata,
            now, now.plus(config.getExpireTime()));
    }
}
