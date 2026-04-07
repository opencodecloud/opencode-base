package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.store.CaptchaStore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

/**
 * Proof-of-Work Captcha Validator — Validates PoW nonce submissions
 * 工作量证明验证码验证器 — 验证 PoW nonce 提交
 *
 * <p>Validates that SHA-256(challenge + nonce) has the required number of leading zero bits.
 * The client submits a nonce that, when concatenated with the original challenge and hashed,
 * produces a hash with the required number of leading zero bits.</p>
 * <p>验证 SHA-256(challenge + nonce) 是否具有所需数量的前导零位。
 * 客户端提交一个 nonce，将其与原始挑战连接并哈希后，
 * 产生具有所需数量前导零位的哈希值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single-use validation (challenge removed after check) - 一次性验证（检查后删除挑战）</li>
 *   <li>Bit-level leading zero verification - 位级前导零验证</li>
 *   <li>Constant-time hash computation (no timing side-channels) - 常量时间哈希计算（无时序侧信道）</li>
 *   <li>Factory method for convenient creation - 工厂方法方便创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaStore store = CaptchaStore.memory();
 * PowCaptchaValidator validator = PowCaptchaValidator.create(store);
 *
 * // After generating a PoW CAPTCHA and storing the answer:
 * // store.store(captchaId, "challenge:20", Duration.ofMinutes(5));
 *
 * // Client submits their computed nonce:
 * ValidationResult result = validator.validate(captchaId, clientNonce);
 * if (result.success()) {
 *     // PoW verified — proceed
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe store) - 线程安全: 是（委托给线程安全的存储）</li>
 *   <li>Null-safe: No (store, id, and answer must not be null) - 空值安全: 否（存储、ID和答案不能为null）</li>
 *   <li>One-time use: Challenge is atomically removed on validation attempt -
 *       一次性使用: 验证尝试时原子移除挑战</li>
 * </ul>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>Validation is O(1) — a single SHA-256 hash plus bit check -
 *       验证为 O(1) — 单次 SHA-256 哈希加位检查</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class PowCaptchaValidator implements CaptchaValidator {

    private final CaptchaStore store;

    /**
     * Creates a new PoW validator with the specified store.
     * 使用指定存储创建新的 PoW 验证器。
     *
     * @param store the CAPTCHA store for retrieving challenges | 用于检索挑战的验证码存储
     * @throws NullPointerException if store is null | 如果 store 为 null
     */
    public PowCaptchaValidator(CaptchaStore store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    /**
     * Validates a PoW nonce submission (case sensitivity is ignored for PoW).
     * 验证 PoW nonce 提交（PoW 忽略大小写敏感性）。
     *
     * @param id     the CAPTCHA ID | 验证码 ID
     * @param answer the nonce submitted by the client | 客户端提交的 nonce
     * @return the validation result | 验证结果
     */
    @Override
    public ValidationResult validate(String id, String answer) {
        return validate(id, answer, false);
    }

    /**
     * Validates a PoW nonce submission.
     * 验证 PoW nonce 提交。
     *
     * <p>The {@code caseSensitive} parameter is ignored for PoW validation,
     * as the nonce is compared via cryptographic hash, not string equality.</p>
     * <p>{@code caseSensitive} 参数在 PoW 验证中被忽略，
     * 因为 nonce 通过加密哈希比较而非字符串相等比较。</p>
     *
     * @param id            the CAPTCHA ID | 验证码 ID
     * @param answer        the nonce submitted by the client | 客户端提交的 nonce
     * @param caseSensitive ignored for PoW validation | PoW 验证中忽略此参数
     * @return the validation result | 验证结果
     */
    @Override
    public ValidationResult validate(String id, String answer, boolean caseSensitive) {
        if (id == null || id.isBlank()) {
            return ValidationResult.invalidInput();
        }
        if (answer == null || answer.isBlank()) {
            return ValidationResult.invalidInput();
        }

        // 1. Atomically retrieve and remove the stored challenge (one-time use)
        Optional<String> stored = store.getAndRemove(id);
        if (stored.isEmpty()) {
            return ValidationResult.notFound();
        }

        // 2. Parse challenge:difficulty from stored value
        String storedValue = stored.get();
        int colonIdx = storedValue.lastIndexOf(':');
        if (colonIdx < 0) {
            return ValidationResult.mismatch();
        }

        String challenge = storedValue.substring(0, colonIdx);
        int difficulty;
        try {
            difficulty = Integer.parseInt(storedValue.substring(colonIdx + 1));
        } catch (NumberFormatException e) {
            return ValidationResult.mismatch();
        }

        // 3. Compute SHA-256(challenge + nonce), where answer is the client-submitted nonce
        byte[] hash = sha256(challenge + answer);

        // 4. Check if the hash has the required number of leading zero bits
        if (hasLeadingZeros(hash, difficulty)) {
            return ValidationResult.ok();
        }
        return ValidationResult.mismatch();
    }

    /**
     * Computes SHA-256 hash of the input string.
     * 计算输入字符串的 SHA-256 哈希。
     *
     * @param input the input string | 输入字符串
     * @return the SHA-256 hash bytes | SHA-256 哈希字节
     */
    private static byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available in all JDK implementations
            throw new AssertionError("SHA-256 not available", e);
        }
    }

    /**
     * Checks if hash has at least the specified number of leading zero bits.
     * 检查哈希是否具有至少指定数量的前导零位。
     *
     * <p>The check processes full bytes first, then verifies any remaining
     * partial-byte bits using a bitmask.</p>
     * <p>检查首先处理完整字节，然后使用位掩码验证剩余的部分字节位。</p>
     *
     * @param hash the hash bytes | 哈希字节
     * @param bits the required number of leading zero bits | 所需的前导零位数
     * @return true if the hash has at least {@code bits} leading zeros |
     *         如果哈希具有至少 {@code bits} 个前导零则返回 true
     */
    static boolean hasLeadingZeros(byte[] hash, int bits) {
        if (bits <= 0) {
            return false;
        }

        int fullBytes = bits / 8;
        int remainingBits = bits % 8;

        // Constant-time check — accumulate non-zero bits without early return
        // 常量时间检查 — 累积非零位，不提前返回
        int diff = 0;

        // Check full zero bytes
        for (int i = 0; i < fullBytes; i++) {
            if (i < hash.length) {
                diff |= hash[i];
            } else {
                diff |= 1; // hash too short
            }
        }

        // Check remaining bits in the next byte
        if (remainingBits > 0) {
            if (fullBytes < hash.length) {
                int mask = (0xFF << (8 - remainingBits)) & 0xFF;
                diff |= (hash[fullBytes] & mask);
            } else {
                diff |= 1; // hash too short
            }
        }

        return diff == 0;
    }

    /**
     * Creates a new PoW validator with the specified store.
     * 使用指定存储创建新的 PoW 验证器。
     *
     * @param store the CAPTCHA store | 验证码存储
     * @return the PoW validator | PoW 验证器
     * @throws NullPointerException if store is null | 如果 store 为 null
     */
    public static PowCaptchaValidator create(CaptchaStore store) {
        return new PowCaptchaValidator(store);
    }
}
