package cloud.opencode.base.observability.health;

import cloud.opencode.base.observability.exception.ObservabilityException;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing and executing health checks.
 * 管理和执行健康检查的注册中心。
 *
 * <p>Maintains a thread-safe collection of named {@link HealthCheck} instances.
 * The {@link #check()} method executes all registered checks, records their duration,
 * and catches any exceptions (producing a {@link HealthStatus#DOWN} result).
 * The {@link #status()} method aggregates all results into a single worst-case status.</p>
 * <p>维护一个线程安全的命名 {@link HealthCheck} 实例集合。
 * {@link #check()} 方法执行所有已注册的检查，记录其耗时，
 * 并捕获任何异常（生成 {@link HealthStatus#DOWN} 结果）。
 * {@link #status()} 方法将所有结果聚合为单个最差状态。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public final class HealthRegistry {

    private static final int DEFAULT_MAX_CHECKS = 1000;

    private final ConcurrentHashMap<String, HealthCheck> checks = new ConcurrentHashMap<>();
    private final int maxChecks;

    private HealthRegistry(int maxChecks) {
        if (maxChecks <= 0) {
            throw new ObservabilityException("INVALID_HEALTH", "maxChecks must be positive");
        }
        this.maxChecks = maxChecks;
    }

    /**
     * Creates a new empty {@code HealthRegistry} with the default limit (1000).
     * 使用默认上限（1000）创建一个新的空 {@code HealthRegistry}。
     *
     * @return a new registry instance | 新的注册中心实例
     */
    public static HealthRegistry create() {
        return new HealthRegistry(DEFAULT_MAX_CHECKS);
    }

    /**
     * Creates a new empty {@code HealthRegistry} with the specified limit.
     * 使用指定上限创建一个新的空 {@code HealthRegistry}。
     *
     * @param maxChecks the maximum number of health checks | 最大健康检查数量
     * @return a new registry instance | 新的注册中心实例
     * @throws ObservabilityException if maxChecks is not positive | 如果 maxChecks 不为正
     */
    public static HealthRegistry create(int maxChecks) {
        return new HealthRegistry(maxChecks);
    }

    /**
     * Registers a health check under the given name, replacing any existing check with the same name.
     * 以给定名称注册健康检查，替换同名的已有检查。
     *
     * @param name  the unique name for the check | 检查的唯一名称
     * @param check the health check to register | 要注册的健康检查
     * @throws ObservabilityException if name is null/blank or check is null |
     *                                如果 name 为 null/空白或 check 为 null
     */
    public void register(String name, HealthCheck check) {
        if (name == null || name.isBlank()) {
            throw new ObservabilityException("INVALID_HEALTH", "Health check name must not be null or blank");
        }
        if (check == null) {
            throw new ObservabilityException("INVALID_HEALTH", "Health check must not be null");
        }
        // Atomic check-then-put within compute lock stripe to prevent TOCTOU race on maxChecks.
        // 在 compute 锁条带内执行原子检查+写入，防止 maxChecks 的 TOCTOU 竞态。
        checks.compute(name, (key, existing) -> {
            if (existing == null && checks.size() >= maxChecks) {
                throw new ObservabilityException("REGISTRY_FULL",
                        "HealthRegistry is full (max=" + maxChecks + ")");
            }
            return check;
        });
    }

    /**
     * Unregisters the health check with the given name.
     * 取消注册给定名称的健康检查。
     *
     * @param name the name of the check to remove | 要移除的检查名称
     * @return {@code true} if a check was removed, {@code false} otherwise |
     * 如果移除了检查则返回 {@code true}，否则返回 {@code false}
     */
    public boolean unregister(String name) {
        return checks.remove(name) != null;
    }

    /**
     * Executes all registered health checks and returns their results.
     * 执行所有已注册的健康检查并返回结果。
     *
     * <p>If a check throws an exception, a {@link HealthStatus#DOWN} result is produced
     * with the exception class name and message as detail.</p>
     * <p>如果检查抛出异常，将生成 {@link HealthStatus#DOWN} 结果，
     * 异常类名和消息作为详情。</p>
     *
     * <p><strong>Security note — No timeout enforcement | 安全注意事项 — 无超时保护:</strong>
     * Health checks are executed synchronously on the calling thread with no built-in timeout.
     * A blocking check (e.g., a database connection without a connect-timeout) will hang the
     * calling thread indefinitely, which can disrupt Kubernetes readiness/liveness probes.
     * Each registered {@link HealthCheck} implementation is responsible for enforcing its own
     * timeout (e.g., via {@link java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)}).</p>
     * <p>健康检查在调用线程上同步执行，无内置超时机制。阻塞性检查（如未设置连接超时的数据库连接）
     * 将无限挂起调用线程，可能影响 Kubernetes readiness/liveness 探针。
     * 每个 {@link HealthCheck} 实现应自行控制超时。</p>
     *
     * @return an unmodifiable map of check name to result | 检查名称到结果的不可修改映射
     */
    public Map<String, HealthResult> check() {
        Map<String, HealthResult> results = new LinkedHashMap<>();
        for (var entry : checks.entrySet()) {
            long start = System.nanoTime();
            try {
                HealthResult result = entry.getValue().check();
                results.put(entry.getKey(), result);
            } catch (Exception e) {
                Duration d = Duration.ofNanos(System.nanoTime() - start);
                // Only include exception class name — getMessage() may contain sensitive
                // infrastructure data (connection strings, hostnames, credentials).
                // 仅包含异常类名，getMessage() 可能含敏感基础设施信息（连接串、主机名、凭据）。
                results.put(entry.getKey(), HealthResult.down(entry.getKey(), e.getClass().getName(), d));
            }
        }
        return Collections.unmodifiableMap(results);
    }

    /**
     * Computes the aggregated health status of all registered checks.
     * 计算所有已注册检查的聚合健康状态。
     *
     * @return the aggregated status | 聚合状态
     */
    public HealthStatus status() {
        Map<String, HealthResult> results = check();
        return HealthStatus.aggregate(
                results.values().stream().map(HealthResult::status).toList()
        );
    }

    /**
     * Returns the names of all registered health checks.
     * 返回所有已注册健康检查的名称。
     *
     * @return an unmodifiable set of check names | 检查名称的不可修改集合
     */
    public Set<String> names() {
        return Set.copyOf(checks.keySet());
    }

    /**
     * Returns the number of registered health checks.
     * 返回已注册的健康检查数量。
     *
     * @return the count of registered checks | 已注册检查的数量
     */
    public int size() {
        return checks.size();
    }

    /**
     * Removes all registered health checks.
     * 移除所有已注册的健康检查。
     */
    public void clear() {
        checks.clear();
    }
}
