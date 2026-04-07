package cloud.opencode.base.captcha.store;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.security.CaptchaSecurity;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Hashed Captcha Store - Decorator that hashes answers before storage
 * 哈希验证码存储 - 在存储前对答案进行哈希处理的装饰器
 *
 * <p>This decorator wraps any {@link CaptchaStore} to hash answers using SHA-256 with
 * a random salt before storing them. Even if the underlying storage is compromised,
 * the plaintext answers cannot be recovered.</p>
 * <p>此装饰器包装任意 {@link CaptchaStore}，在存储前使用 SHA-256 加随机盐对答案进行哈希。
 * 即使底层存储被泄露，也无法恢复明文答案。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Transparent hashing of answers on store - 存储时透明地对答案进行哈希</li>
 *   <li>Per-answer random salt generation - 每个答案使用随机盐</li>
 *   <li>Timing-safe verification via {@link CaptchaSecurity} - 通过 {@link CaptchaSecurity} 进行时间安全的验证</li>
 *   <li>Atomic verify-and-remove for one-time validation - 原子验证并删除用于一次性验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaStore delegate = CaptchaStore.memory();
 * HashedCaptchaStore store = HashedCaptchaStore.wrap(delegate);
 * store.store("id", "answer", Duration.ofMinutes(5));
 * boolean valid = store.verifyAnswer("id", "answer");       // true
 * boolean removed = store.verifyAndRemove("id", "answer");  // true, entry removed
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Store: O(1) + SHA-256 hash - 存储: O(1) + SHA-256 哈希</li>
 *   <li>Verify: O(1) + SHA-256 hash + constant-time compare - 验证: O(1) + SHA-256 哈希 + 常量时间比较</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on delegate - 线程安全: 取决于委托实现</li>
 *   <li>Null-safe: No (parameters must be non-null) - 空值安全: 否（参数不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class HashedCaptchaStore implements CaptchaStore {

    private static final String SEPARATOR = ":";

    private final CaptchaStore delegate;
    private final boolean caseSensitive;

    private HashedCaptchaStore(CaptchaStore delegate, boolean caseSensitive) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.caseSensitive = caseSensitive;
    }

    /**
     * Wraps a {@link CaptchaStore} with hashing (case-insensitive).
     * 使用哈希包装 {@link CaptchaStore}（不区分大小写）。
     *
     * @param delegate the store to wrap | 要包装的存储
     * @return the hashed store | 哈希存储
     * @throws NullPointerException if delegate is null | 如果委托为 null
     */
    public static HashedCaptchaStore wrap(CaptchaStore delegate) {
        return new HashedCaptchaStore(delegate, false);
    }

    /**
     * Wraps a {@link CaptchaStore} with hashing and configurable case sensitivity.
     * 使用哈希包装 {@link CaptchaStore}，可配置大小写敏感。
     *
     * @param delegate      the store to wrap | 要包装的存储
     * @param caseSensitive whether answer comparison is case-sensitive | 答案比较是否区分大小写
     * @return the hashed store | 哈希存储
     * @throws NullPointerException if delegate is null | 如果委托为 null
     */
    public static HashedCaptchaStore wrap(CaptchaStore delegate, boolean caseSensitive) {
        return new HashedCaptchaStore(delegate, caseSensitive);
    }

    /**
     * Stores a CAPTCHA answer after hashing it with a random salt.
     * 使用随机盐对验证码答案进行哈希后存储。
     *
     * <p>The stored value is {@code salt:hash} so that verification can recover
     * the salt. The plaintext answer is never persisted.</p>
     * <p>存储的值为 {@code salt:hash}，以便验证时恢复盐。明文答案永远不会被持久化。</p>
     *
     * @param id     the CAPTCHA ID | 验证码 ID
     * @param answer the plaintext answer | 明文答案
     * @param ttl    the time to live | 存活时间
     */
    @Override
    public void store(String id, String answer, Duration ttl) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(answer, "answer must not be null");
        Objects.requireNonNull(ttl, "ttl must not be null");
        String salt = CaptchaSecurity.generateSalt();
        String hash = CaptchaSecurity.hashAnswer(answer, salt, caseSensitive);
        delegate.store(id, salt + SEPARATOR + hash, ttl);
    }

    /**
     * Retrieves the stored hash for a CAPTCHA.
     * 检索验证码的存储哈希。
     *
     * <p>Returns the raw {@code salt:hash} value. For answer verification,
     * use {@link #verifyAnswer(String, String)} instead.</p>
     * <p>返回原始 {@code salt:hash} 值。要验证答案，请使用
     * {@link #verifyAnswer(String, String)}。</p>
     *
     * @param id the CAPTCHA ID | 验证码 ID
     * @return the stored hash if present | 存储的哈希（如果存在）
     */
    @Override
    public Optional<String> get(String id) {
        return delegate.get(id);
    }

    /**
     * Retrieves and removes the stored hash for a CAPTCHA.
     * 检索并删除验证码的存储哈希。
     *
     * @param id the CAPTCHA ID | 验证码 ID
     * @return the stored hash if present | 存储的哈希（如果存在）
     */
    @Override
    public Optional<String> getAndRemove(String id) {
        return delegate.getAndRemove(id);
    }

    /**
     * Verifies a plaintext answer against the stored hash without removing the entry.
     * 验证明文答案与存储的哈希是否匹配，不删除条目。
     *
     * @param id          the CAPTCHA ID | 验证码 ID
     * @param plainAnswer the plaintext answer to verify | 要验证的明文答案
     * @return true if the answer matches | 如果答案匹配返回 true
     */
    public boolean verifyAnswer(String id, String plainAnswer) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(plainAnswer, "plainAnswer must not be null");
        Optional<String> stored = delegate.get(id);
        return stored.isPresent() && verifySaltedHash(plainAnswer, stored.get());
    }

    /**
     * Verifies a plaintext answer against the stored hash and removes the entry atomically.
     * 验证明文答案与存储的哈希是否匹配并原子地删除条目。
     *
     * <p>This is the recommended method for one-time CAPTCHA validation: the entry
     * is consumed regardless of whether the answer is correct, preventing replay attacks.</p>
     * <p>这是一次性验证码验证的推荐方法：无论答案是否正确，条目都会被消耗，防止重放攻击。</p>
     *
     * @param id          the CAPTCHA ID | 验证码 ID
     * @param plainAnswer the plaintext answer to verify | 要验证的明文答案
     * @return true if the answer matches | 如果答案匹配返回 true
     */
    public boolean verifyAndRemove(String id, String plainAnswer) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(plainAnswer, "plainAnswer must not be null");
        Optional<String> stored = delegate.getAndRemove(id);
        return stored.isPresent() && verifySaltedHash(plainAnswer, stored.get());
    }

    /**
     * Verifies a plaintext answer and removes the entry, returning a detailed result.
     * 验证明文答案并删除条目，返回详细结果。
     *
     * <p>This method performs a single store lookup to distinguish between NOT_FOUND
     * (entry does not exist) and MISMATCH (entry exists but answer is wrong),
     * avoiding the overhead of separate exists() + verifyAndRemove() calls.</p>
     * <p>此方法执行单次存储查找以区分 NOT_FOUND（条目不存在）和 MISMATCH（条目存在但答案错误），
     * 避免了分别调用 exists() + verifyAndRemove() 的开销。</p>
     *
     * @param id          the CAPTCHA ID | 验证码 ID
     * @param plainAnswer the plaintext answer to verify | 要验证的明文答案
     * @return the validation result | 验证结果
     */
    public ValidationResult verifyAndRemoveResult(String id, String plainAnswer) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(plainAnswer, "plainAnswer must not be null");
        Optional<String> stored = delegate.getAndRemove(id);
        if (stored.isEmpty()) {
            return ValidationResult.notFound();
        }
        return verifySaltedHash(plainAnswer, stored.get())
            ? ValidationResult.ok()
            : ValidationResult.mismatch();
    }

    @Override
    public void remove(String id) {
        delegate.remove(id);
    }

    @Override
    public boolean exists(String id) {
        return delegate.exists(id);
    }

    @Override
    public void clearExpired() {
        delegate.clearExpired();
    }

    @Override
    public void clearAll() {
        delegate.clearAll();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * Parses the {@code salt:hash} string and verifies the answer.
     */
    // Base64 standard encoding never produces ':', so indexOf(":") always finds the
    // separator between salt and hash. This is safe because both generateSalt() and
    // hashAnswer() use Base64.getEncoder() whose alphabet is [A-Za-z0-9+/=].
    private boolean verifySaltedHash(String plainAnswer, String saltedHash) {
        int separatorIndex = saltedHash.indexOf(SEPARATOR);
        // Reject malformed entries: separator missing, empty salt, or empty hash.
        // Normal flow always produces "24-char-salt:44-char-hash" from generateSalt() + hashAnswer().
        if (separatorIndex <= 0 || separatorIndex >= saltedHash.length() - 1) {
            return false;
        }
        String salt = saltedHash.substring(0, separatorIndex);
        String hash = saltedHash.substring(separatorIndex + 1);
        return CaptchaSecurity.verifyHashedAnswer(plainAnswer, hash, salt, caseSensitive);
    }
}
