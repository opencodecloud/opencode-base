
package cloud.opencode.base.json.identity;

/**
 * Object ID Generator - Abstract Base for Generating Object Identifiers
 * 对象 ID 生成器 - 生成对象标识符的抽象基类
 *
 * <p>This abstract class defines the contract for generating unique identifiers
 * for objects during JSON serialization. Implementations provide specific
 * strategies such as auto-increment integers, UUIDs, or property-based IDs.</p>
 * <p>此抽象类定义了在 JSON 序列化期间为对象生成唯一标识符的契约。
 * 实现提供特定策略，如自增整数、UUID 或基于属性的 ID。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * ObjectIdGenerator<Integer> gen = new ObjectIdGenerators.IntSequenceGenerator();
 * Integer id = gen.generateId(myObject);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementations should document null behavior - 空值安全: 实现应说明空值行为</li>
 * </ul>
 *
 * @param <T> the type of generated identifiers - 生成的标识符类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ObjectIdGenerators
 * @since JDK 25, opencode-base-json V1.0.0
 */
public abstract class ObjectIdGenerator<T> {

    /**
     * Generates a unique identifier for the given object.
     * 为给定对象生成唯一标识符。
     *
     * @param forPojo the object to generate an ID for - 要生成 ID 的对象
     * @return the generated identifier - 生成的标识符
     */
    public abstract T generateId(Object forPojo);

    /**
     * Returns the scope class used to determine ID uniqueness boundaries.
     * 返回用于确定 ID 唯一性边界的作用域类。
     *
     * @return the scope class - 作用域类
     */
    public abstract Class<?> getScope();

    /**
     * Determines whether this generator can be used interchangeably with another.
     * 确定此生成器是否可以与另一个互换使用。
     *
     * @param gen the other generator to check compatibility with - 要检查兼容性的另一个生成器
     * @return true if compatible - 如果兼容则返回 true
     */
    public abstract boolean canUseFor(ObjectIdGenerator<?> gen);

    /**
     * ID Key - Composite Key for Object Identity Tracking
     * ID 键 - 用于对象身份跟踪的复合键
     *
     * <p>A composite key consisting of type, scope, and the actual key value,
     * used for tracking object identity during serialization and deserialization.</p>
     * <p>由类型、作用域和实际键值组成的复合键，用于在序列化和反序列化期间跟踪对象身份。</p>
     *
     * @param type  the type of the identified object - 被标识对象的类型
     * @param scope the scope for uniqueness - 唯一性的作用域
     * @param key   the actual identity key value - 实际的身份键值
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-json V1.0.0
     */
    public record IdKey(Class<?> type, Class<?> scope, Object key) {
    }
}
