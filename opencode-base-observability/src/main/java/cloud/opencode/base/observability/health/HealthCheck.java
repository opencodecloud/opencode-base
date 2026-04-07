package cloud.opencode.base.observability.health;

/**
 * Functional interface for performing a health check.
 * 执行健康检查的函数式接口。
 *
 * <p>Implementations should return a {@link HealthResult} describing the current
 * health of the component being checked. If the check throws an exception,
 * the {@link HealthRegistry} will catch it and produce a {@link HealthStatus#DOWN} result.</p>
 * <p>实现应返回描述被检查组件当前健康状况的 {@link HealthResult}。
 * 如果检查抛出异常，{@link HealthRegistry} 将捕获并生成 {@link HealthStatus#DOWN} 结果。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
@FunctionalInterface
public interface HealthCheck {

    /**
     * Executes the health check and returns the result.
     * 执行健康检查并返回结果。
     *
     * @return the health check result | 健康检查结果
     */
    HealthResult check();
}
