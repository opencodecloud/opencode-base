
package cloud.opencode.base.json.identity;

/**
 * Object ID Resolver - Resolves Object Identity References
 * 对象 ID 解析器 - 解析对象身份引用
 *
 * <p>This interface defines the contract for binding and resolving object identity
 * references during JSON deserialization. When an object is first encountered,
 * its ID is bound to the object instance. Subsequent references to the same ID
 * resolve to the already-deserialized instance.</p>
 * <p>此接口定义了在 JSON 反序列化期间绑定和解析对象身份引用的契约。
 * 当首次遇到对象时，其 ID 绑定到对象实例。后续对同一 ID 的引用
 * 将解析为已反序列化的实例。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * ObjectIdResolver resolver = new SimpleObjectIdResolver();
 * ObjectIdGenerator.IdKey key = new ObjectIdGenerator.IdKey(MyClass.class, Void.class, 1);
 * resolver.bindItem(key, myObject);
 * Object resolved = resolver.resolveId(key); // returns myObject
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementations should reject null IDs - 空值安全: 实现应拒绝空 ID</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see SimpleObjectIdResolver
 * @since JDK 25, opencode-base-json V1.0.0
 */
public interface ObjectIdResolver {

    /**
     * Binds an object identity key to a POJO instance.
     * 将对象身份键绑定到 POJO 实例。
     *
     * @param id   the identity key - 身份键
     * @param pojo the object instance to bind - 要绑定的对象实例
     */
    void bindItem(ObjectIdGenerator.IdKey id, Object pojo);

    /**
     * Resolves an object identity key to a previously bound POJO instance.
     * 将对象身份键解析为之前绑定的 POJO 实例。
     *
     * @param id the identity key to resolve - 要解析的身份键
     * @return the bound object, or {@code null} if not found - 绑定的对象，未找到则返回 null
     */
    Object resolveId(ObjectIdGenerator.IdKey id);

    /**
     * Determines whether this resolver can be used interchangeably with another.
     * 确定此解析器是否可以与另一个互换使用。
     *
     * @param resolverType the other resolver to check compatibility with - 要检查兼容性的另一个解析器
     * @return true if compatible - 如果兼容则返回 true
     */
    boolean canUseFor(ObjectIdResolver resolverType);

    /**
     * Creates a new resolver instance for a deserialization context.
     * 为反序列化上下文创建新的解析器实例。
     *
     * @param context the deserialization context - 反序列化上下文
     * @return a new resolver instance - 新的解析器实例
     */
    ObjectIdResolver newForDeserialization(Object context);
}
