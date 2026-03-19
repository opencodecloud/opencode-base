package cloud.opencode.base.config.jdk25;

import java.util.Map;
import java.util.Optional;

/**
 * Configuration Context using JDK 25 Scoped Values
 * 使用JDK 25作用域值的配置上下文
 *
 * <p>Provides thread-local-like configuration context using JDK 25 ScopedValue feature.
 * Useful for multi-tenant, multi-profile, and test override scenarios.</p>
 * <p>使用JDK 25 ScopedValue特性提供类似线程本地的配置上下文。适用于多租户、多环境和测试覆盖场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configuration overrides per execution context - 每个执行上下文的配置覆盖</li>
 *   <li>Profile-specific context - 特定环境的上下文</li>
 *   <li>Multi-tenant configuration - 多租户配置</li>
 *   <li>Virtual thread friendly - 虚拟线程友好</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Run with tenant context
 * ConfigContext.withTenant("tenant-123", () -> {
 *     String apiKey = OpenConfig.getString("api.key");
 *     return processRequest(apiKey);
 * });
 *
 * // Run with config overrides
 * Map<String, String> testOverrides = Map.of("database.url", "jdbc:h2:mem:test");
 * ConfigContext.withOverrides(testOverrides, () -> {
 *     runTests();
 *     return null;
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Inheritance-safe: Via ScopedValue semantics - 继承安全: 通过ScopedValue语义</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public final class ConfigContext {

    /** Configuration overrides | 配置覆盖 */
    public static final ScopedValue<Map<String, String>> OVERRIDES = ScopedValue.newInstance();

    /** Current profile | 当前环境 */
    public static final ScopedValue<String> PROFILE = ScopedValue.newInstance();

    /** Tenant ID for multi-tenant scenarios | 多租户场景的租户ID */
    public static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();

    private ConfigContext() {
        // Prevent instantiation
    }

    /**
     * Execute task with configuration overrides
     * 使用配置覆盖执行任务
     *
     * @param <T> result type | 结果类型
     * @param <X> exception type | 异常类型
     * @param overrides configuration overrides | 配置覆盖
     * @param task task to execute | 要执行的任务
     * @return task result | 任务结果
     * @throws X if task throws exception | 如果任务抛出异常
     */
    public static <T, X extends Throwable> T withOverrides(
            Map<String, String> overrides,
            ScopedValue.CallableOp<T, X> task) throws X {
        return ScopedValue.where(OVERRIDES, overrides).call(task);
    }

    /**
     * Execute task with profile context
     * 使用环境上下文执行任务
     *
     * @param <T> result type | 结果类型
     * @param <X> exception type | 异常类型
     * @param profile profile name | 环境名称
     * @param task task to execute | 要执行的任务
     * @return task result | 任务结果
     * @throws X if task throws exception | 如果任务抛出异常
     */
    public static <T, X extends Throwable> T withProfile(
            String profile,
            ScopedValue.CallableOp<T, X> task) throws X {
        return ScopedValue.where(PROFILE, profile).call(task);
    }

    /**
     * Execute task with tenant context
     * 使用租户上下文执行任务
     *
     * @param <T> result type | 结果类型
     * @param <X> exception type | 异常类型
     * @param tenantId tenant identifier | 租户标识符
     * @param task task to execute | 要执行的任务
     * @return task result | 任务结果
     * @throws X if task throws exception | 如果任务抛出异常
     */
    public static <T, X extends Throwable> T withTenant(
            String tenantId,
            ScopedValue.CallableOp<T, X> task) throws X {
        return ScopedValue.where(TENANT_ID, tenantId).call(task);
    }

    /**
     * Execute task with overrides (void returning)
     * 使用配置覆盖执行任务(无返回值)
     *
     * @param overrides configuration overrides | 配置覆盖
     * @param task task to execute | 要执行的任务
     */
    public static void runWithOverrides(Map<String, String> overrides, Runnable task) {
        ScopedValue.where(OVERRIDES, overrides).run(task);
    }

    /**
     * Execute task with profile context (void returning)
     * 使用环境上下文执行任务(无返回值)
     *
     * @param profile profile name | 环境名称
     * @param task task to execute | 要执行的任务
     */
    public static void runWithProfile(String profile, Runnable task) {
        ScopedValue.where(PROFILE, profile).run(task);
    }

    /**
     * Execute task with tenant context (void returning)
     * 使用租户上下文执行任务(无返回值)
     *
     * @param tenantId tenant identifier | 租户标识符
     * @param task task to execute | 要执行的任务
     */
    public static void runWithTenant(String tenantId, Runnable task) {
        ScopedValue.where(TENANT_ID, tenantId).run(task);
    }

    /**
     * Get current configuration overrides
     * 获取当前配置覆盖
     *
     * @return optional overrides map | 可选的覆盖映射
     */
    public static Optional<Map<String, String>> currentOverrides() {
        return OVERRIDES.isBound() ? Optional.of(OVERRIDES.get()) : Optional.empty();
    }

    /**
     * Get current profile
     * 获取当前环境
     *
     * @return optional profile name | 可选的环境名称
     */
    public static Optional<String> currentProfile() {
        return PROFILE.isBound() ? Optional.of(PROFILE.get()) : Optional.empty();
    }

    /**
     * Get current tenant ID
     * 获取当前租户ID
     *
     * @return optional tenant ID | 可选的租户ID
     */
    public static Optional<String> currentTenant() {
        return TENANT_ID.isBound() ? Optional.of(TENANT_ID.get()) : Optional.empty();
    }
}
