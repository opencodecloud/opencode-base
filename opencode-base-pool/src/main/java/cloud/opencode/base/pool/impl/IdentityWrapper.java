package cloud.opencode.base.pool.impl;

/**
 * Identity Wrapper - Uses object identity for map key equality
 * 身份包装器 - 使用对象身份进行 Map 键的相等性判断
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Identity-based equality (== instead of equals) - 基于身份的相等性（==而非equals）</li>
 *   <li>Identity-based hashCode (System.identityHashCode) - 基于身份的哈希码</li>
 *   <li>Used internally for pool object tracking - 内部用于池对象追踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * IdentityWrapper<Connection> wrapper = new IdentityWrapper<>(conn);
 * map.put(wrapper, pooledObject);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @param object the wrapped object | 包装的对象
 * @param <T> the object type | 对象类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
record IdentityWrapper<T>(T object) {
    @Override
    public int hashCode() {
        return System.identityHashCode(object);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IdentityWrapper<?> other)) return false;
        return this.object == other.object;
    }
}
