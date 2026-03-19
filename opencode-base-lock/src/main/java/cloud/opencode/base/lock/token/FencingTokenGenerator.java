package cloud.opencode.base.lock.token;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fencing Token Generator with Optional ID Module Delegation
 * 支持可选 ID 模块委托的 Fencing Token 生成器
 *
 * <p>Generates unique fencing tokens for distributed lock scenarios.
 * If the ID module (opencode-base-id) is available, it delegates to OpenId
 * for higher quality tokens (TSID/ULID). Otherwise, falls back to local generation.</p>
 * <p>为分布式锁场景生成唯一的防护令牌。
 * 如果 ID 模块可用，则委托给 OpenId 生成更高质量的令牌（TSID/ULID）。
 * 否则降级到本地生成。</p>
 *
 * <p><strong>Token Types | 令牌类型:</strong></p>
 * <ul>
 *   <li>With ID module: TSID (time-sorted, 64-bit) or ULID (128-bit) - 使用TSID或ULID</li>
 *   <li>Without ID module: UUID-based or AtomicLong - 使用UUID或AtomicLong</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Generate string token (recommended for distributed locks)
 * String token = FencingTokenGenerator.generateStringToken();
 *
 * // Generate long token (for local locks)
 * long token = FencingTokenGenerator.generateLongToken();
 *
 * // Check if ID module is available
 * boolean hasIdModule = FencingTokenGenerator.isIdModuleAvailable();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Monotonic: Yes (with ID module) - 单调递增: 是（使用ID模块时）</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generates monotonically increasing fencing tokens - 生成单调递增的防护令牌</li>
 *   <li>Prevents stale lock holders from corrupting data - 防止过期锁持有者损坏数据</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public final class FencingTokenGenerator {

    /**
     * MethodHandle for OpenId.tsidStr() - null if ID module not available
     */
    private static final MethodHandle TSID_STR_HANDLE;

    /**
     * MethodHandle for OpenId.tsid() - null if ID module not available
     */
    private static final MethodHandle TSID_HANDLE;

    /**
     * Fallback counter for local token generation
     */
    private static final AtomicLong FALLBACK_COUNTER = new AtomicLong(System.currentTimeMillis());

    static {
        TSID_STR_HANDLE = initMethodHandle("tsidStr", String.class);
        TSID_HANDLE = initMethodHandle("tsid", long.class);
    }

    private FencingTokenGenerator() {
    }

    /**
     * Initializes MethodHandle for OpenId methods if available.
     */
    private static MethodHandle initMethodHandle(String methodName, Class<?> returnType) {
        try {
            Class<?> openIdClass = Class.forName("cloud.opencode.base.id.OpenId");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findStatic(openIdClass, methodName, MethodType.methodType(returnType));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Checks if the ID module is available.
     * 检查 ID 模块是否可用
     *
     * @return true if ID module is available - 如果 ID 模块可用返回 true
     */
    public static boolean isIdModuleAvailable() {
        return TSID_STR_HANDLE != null;
    }

    /**
     * Generates a unique string token for fencing.
     * 生成用于防护的唯一字符串令牌
     *
     * <p>If ID module is available, uses TSID (time-sorted, compact).
     * Otherwise, uses UUID-based fallback.</p>
     *
     * @return unique string token - 唯一字符串令牌
     */
    public static String generateStringToken() {
        if (TSID_STR_HANDLE != null) {
            try {
                return (String) TSID_STR_HANDLE.invokeExact();
            } catch (Throwable e) {
                // Fall through to fallback
            }
        }
        // Fallback: UUID-based token
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generates a unique long token for fencing.
     * 生成用于防护的唯一长整型令牌
     *
     * <p>If ID module is available, uses TSID (time-sorted, 64-bit).
     * Otherwise, uses AtomicLong-based fallback.</p>
     *
     * @return unique long token - 唯一长整型令牌
     */
    public static long generateLongToken() {
        if (TSID_HANDLE != null) {
            try {
                return (long) TSID_HANDLE.invokeExact();
            } catch (Throwable e) {
                // Fall through to fallback
            }
        }
        // Fallback: AtomicLong counter (monotonic within JVM)
        return FALLBACK_COUNTER.incrementAndGet();
    }

    /**
     * Generates a token with specified prefix.
     * 生成带指定前缀的令牌
     *
     * @param prefix the prefix to add - 要添加的前缀
     * @return prefixed token - 带前缀的令牌
     */
    public static String generatePrefixedToken(String prefix) {
        return prefix + ":" + generateStringToken();
    }
}
