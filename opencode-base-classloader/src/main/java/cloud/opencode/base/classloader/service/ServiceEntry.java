package cloud.opencode.base.classloader.service;

import java.util.Objects;

/**
 * Service Entry - Wrapper for a service discovered via cross-ClassLoader lookup
 * 服务条目 - 跨类加载器查找发现的服务包装器
 *
 * <p>Holds the service instance, the name of its originating ClassLoader,
 * and a priority value used for ordering.</p>
 * <p>持有服务实例、来源类加载器名称以及用于排序的优先级值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record wrapping a service instance - 不可变记录，包装服务实例</li>
 *   <li>Comparable by priority (lower = higher priority) - 按优先级比较（值越小优先级越高）</li>
 *   <li>Stores ClassLoader name, not a strong reference - 存储类加载器名称，非强引用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ServiceEntry<MyService> entry = new ServiceEntry<>(impl, "AppClassLoader", 100);
 * int p = entry.priority();
 * MyService svc = entry.service();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param service         the service instance | 服务实例
 * @param classLoaderName the name of the ClassLoader that loaded this service | 加载此服务的类加载器名称
 * @param priority        priority value (lower = higher priority) | 优先级值（值越小优先级越高）
 * @param <S>             the service type | 服务类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record ServiceEntry<S>(S service, String classLoaderName, int priority)
        implements Comparable<ServiceEntry<S>> {

    /**
     * Canonical constructor with null checks.
     * 规范构造器，包含空值检查。
     *
     * @param service         the service instance | 服务实例
     * @param classLoaderName the ClassLoader name | 类加载器名称
     * @param priority        the priority value | 优先级值
     */
    public ServiceEntry {
        Objects.requireNonNull(service, "service must not be null | service 不能为 null");
        Objects.requireNonNull(classLoaderName, "classLoaderName must not be null | classLoaderName 不能为 null");
    }

    /**
     * Compare by priority ascending (lower value = higher priority).
     * 按优先级升序比较（值越小优先级越高）。
     *
     * @param other the other entry to compare | 要比较的另一个条目
     * @return comparison result | 比较结果
     */
    @Override
    public int compareTo(ServiceEntry<S> other) {
        return Integer.compare(this.priority, other.priority);
    }
}
