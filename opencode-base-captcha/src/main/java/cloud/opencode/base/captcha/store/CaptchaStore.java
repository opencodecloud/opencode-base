package cloud.opencode.base.captcha.store;

import java.time.Duration;
import java.util.Optional;

/**
 * Captcha Store - Interface for CAPTCHA storage
 * 验证码存储 - 验证码存储接口
 *
 * <p>This interface defines the contract for storing and retrieving CAPTCHA data.</p>
 * <p>此接口定义了存储和检索验证码数据的契约。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Store and retrieve CAPTCHA answers with TTL - 存储和检索带 TTL 的验证码答案</li>
 *   <li>Atomic get-and-remove for one-time validation - 原子获取并删除用于一次性验证</li>
 *   <li>Expiration management - 过期管理</li>
 *   <li>Factory methods for memory-based stores - 基于内存存储的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaStore store = CaptchaStore.memory();
 * store.store("captcha-id", "answer", Duration.ofMinutes(5));
 * Optional<String> answer = store.getAndRemove("captcha-id");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (parameters must be non-null) - 空值安全: 否（参数不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public interface CaptchaStore {

    /**
     * Stores a CAPTCHA answer.
     * 存储验证码答案。
     *
     * @param id     the CAPTCHA ID | 验证码 ID
     * @param answer the answer | 答案
     * @param ttl    the time to live | 存活时间
     */
    void store(String id, String answer, Duration ttl);

    /**
     * Retrieves a CAPTCHA answer.
     * 检索验证码答案。
     *
     * @param id the CAPTCHA ID | 验证码 ID
     * @return the answer if present | 答案（如果存在）
     */
    Optional<String> get(String id);

    /**
     * Retrieves and removes a CAPTCHA answer.
     * 检索并删除验证码答案。
     *
     * @param id the CAPTCHA ID | 验证码 ID
     * @return the answer if present | 答案（如果存在）
     */
    Optional<String> getAndRemove(String id);

    /**
     * Removes a CAPTCHA.
     * 删除验证码。
     *
     * @param id the CAPTCHA ID | 验证码 ID
     */
    void remove(String id);

    /**
     * Checks if a CAPTCHA exists.
     * 检查验证码是否存在。
     *
     * @param id the CAPTCHA ID | 验证码 ID
     * @return true if exists | 如果存在返回 true
     */
    boolean exists(String id);

    /**
     * Clears all expired CAPTCHAs.
     * 清除所有过期的验证码。
     */
    void clearExpired();

    /**
     * Clears all CAPTCHAs.
     * 清除所有验证码。
     */
    void clearAll();

    /**
     * Gets the current size.
     * 获取当前大小。
     *
     * @return the size | 大小
     */
    int size();

    /**
     * Creates a memory-based store.
     * 创建基于内存的存储。
     *
     * @return the store | 存储
     */
    static CaptchaStore memory() {
        return new MemoryCaptchaStore();
    }

    /**
     * Creates a memory-based store with max size.
     * 创建具有最大大小的基于内存的存储。
     *
     * @param maxSize the maximum size | 最大大小
     * @return the store | 存储
     */
    static CaptchaStore memory(int maxSize) {
        return new MemoryCaptchaStore(maxSize);
    }
}
