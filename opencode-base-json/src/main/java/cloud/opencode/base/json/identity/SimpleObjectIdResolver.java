
package cloud.opencode.base.json.identity;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple Object ID Resolver - Default HashMap-Based Identity Resolver
 * 简单对象 ID 解析器 - 默认基于 HashMap 的身份解析器
 *
 * <p>A straightforward implementation of {@link ObjectIdResolver} that uses
 * a {@link HashMap} to store and retrieve object identity mappings.
 * Each deserialization context should use its own instance.</p>
 * <p>一个简单的 {@link ObjectIdResolver} 实现，使用 {@link HashMap}
 * 存储和检索对象身份映射。每个反序列化上下文应使用自己的实例。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * SimpleObjectIdResolver resolver = new SimpleObjectIdResolver();
 * ObjectIdGenerator.IdKey key = new ObjectIdGenerator.IdKey(User.class, Void.class, 42);
 * resolver.bindItem(key, user);
 * Object found = resolver.resolveId(key); // returns user
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (single-threaded deserialization context) - 线程安全: 否（单线程反序列化上下文）</li>
 *   <li>Null-safe: Rejects null ID keys - 空值安全: 拒绝空 ID 键</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ObjectIdResolver
 * @since JDK 25, opencode-base-json V1.0.0
 */
public class SimpleObjectIdResolver implements ObjectIdResolver {

    /**
     * The identity map storing ID keys to object mappings.
     * 存储 ID 键到对象映射的身份映射表。
     */
    private final Map<ObjectIdGenerator.IdKey, Object> items;

    /**
     * Creates a new SimpleObjectIdResolver with an empty identity map.
     * 创建一个具有空身份映射表的 SimpleObjectIdResolver。
     */
    public SimpleObjectIdResolver() {
        this.items = new HashMap<>();
    }

    @Override
    public void bindItem(ObjectIdGenerator.IdKey id, Object pojo) {
        if (id == null) {
            throw new IllegalArgumentException("IdKey must not be null / IdKey 不能为空");
        }
        if (pojo == null) {
            throw new IllegalArgumentException("POJO must not be null / POJO 不能为空");
        }
        Object existing = items.get(id);
        if (existing != null && existing != pojo) {
            throw new IllegalStateException(
                    "Already had POJO for id (" + id + ") bound to a different object / "
                            + "已有不同对象绑定到此 ID (" + id + ")");
        }
        items.put(id, pojo);
    }

    @Override
    public Object resolveId(ObjectIdGenerator.IdKey id) {
        if (id == null) {
            throw new IllegalArgumentException("IdKey must not be null / IdKey 不能为空");
        }
        return items.get(id);
    }

    @Override
    public boolean canUseFor(ObjectIdResolver resolverType) {
        return resolverType != null && resolverType.getClass() == getClass();
    }

    @Override
    public ObjectIdResolver newForDeserialization(Object context) {
        return new SimpleObjectIdResolver();
    }
}
