package cloud.opencode.base.classloader.resource;

import java.util.Objects;

/**
 * Resource Change Event - Represents a file system change event for a resource
 * 资源变更事件 - 表示资源的文件系统变更事件
 *
 * <p>Immutable record capturing the type of change, the affected resource, and the timestamp.</p>
 * <p>不可变记录，包含变更类型、受影响的资源和时间戳。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ResourceEvent event = new ResourceEvent(
 *     ResourceEvent.Type.MODIFIED,
 *     new FileResource("/etc/config.yml"),
 *     System.currentTimeMillis()
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public record ResourceEvent(
        Type type,
        Resource resource,
        long timestamp
) {

    /**
     * Resource change event type
     * 资源变更事件类型
     */
    public enum Type {
        /** Resource created | 资源创建 */
        CREATED,
        /** Resource modified | 资源修改 */
        MODIFIED,
        /** Resource deleted | 资源删除 */
        DELETED
    }

    /**
     * Compact constructor with null validation
     * 紧凑构造器，包含空值校验
     *
     * @throws NullPointerException if type or resource is null | 如果 type 或 resource 为 null 则抛出
     */
    public ResourceEvent {
        Objects.requireNonNull(type, "Event type must not be null");
        Objects.requireNonNull(resource, "Resource must not be null");
    }
}
