
package cloud.opencode.base.json;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mixin Source - Manages Mixin Annotation Mappings
 * 混入源 - 管理混入注解映射
 *
 * <p>This class manages mixin annotations, allowing annotations from one class
 * (the mixin) to be applied to another class (the target) without modifying
 * the target class. This is useful for adding JSON serialization annotations
 * to third-party classes.</p>
 * <p>此类管理混入注解，允许将一个类（混入）的注解应用到另一个类（目标）上，
 * 而无需修改目标类。这对于向第三方类添加 JSON 序列化注解非常有用。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * MixinSource mixins = new MixinSource();
 *
 * // Define a mixin class with annotations
 * abstract class UserMixin {
 *     @JsonProperty("user_name")
 *     abstract String getName();
 *
 *     @JsonIgnore
 *     abstract String getPassword();
 * }
 *
 * // Apply mixin to target class
 * mixins.addMixin(User.class, UserMixin.class);
 *
 * // Check if mixin exists
 * boolean hasMixin = mixins.hasMixin(User.class); // true
 * Class<?> mixin = mixins.getMixin(User.class);   // UserMixin.class
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe mixin registration and lookup - 线程安全的混入注册和查找</li>
 *   <li>Add, remove, and query mixin mappings - 添加、移除和查询混入映射</li>
 *   <li>Unmodifiable view of all registered mixins - 所有注册混入的不可修改视图</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap) - 线程安全: 是（ConcurrentHashMap）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class MixinSource {

    /**
     * Mixin mappings: target class -> mixin class
     * 混入映射：目标类 -> 混入类
     */
    private final ConcurrentHashMap<Class<?>, Class<?>> mixins = new ConcurrentHashMap<>();

    /**
     * Constructs an empty MixinSource.
     * 构造空的 MixinSource。
     */
    public MixinSource() {
    }

    /**
     * Registers a mixin annotation class for a target type.
     * 为目标类型注册混入注解类。
     *
     * @param target the target class to apply mixin to - 要应用混入的目标类
     * @param mixin  the mixin class containing annotations - 包含注解的混入类
     * @throws NullPointerException if target or mixin is null - 如果 target 或 mixin 为 null
     */
    public void addMixin(Class<?> target, Class<?> mixin) {
        Objects.requireNonNull(target, "Target class must not be null");
        Objects.requireNonNull(mixin, "Mixin class must not be null");
        mixins.put(target, mixin);
    }

    /**
     * Removes the mixin mapping for a target type.
     * 移除目标类型的混入映射。
     *
     * @param target the target class to remove mixin from - 要移除混入的目标类
     * @throws NullPointerException if target is null - 如果 target 为 null
     */
    public void removeMixin(Class<?> target) {
        Objects.requireNonNull(target, "Target class must not be null");
        mixins.remove(target);
    }

    /**
     * Returns the mixin class for the given target type.
     * 返回给定目标类型的混入类。
     *
     * @param target the target class - 目标类
     * @return the mixin class, or null if no mixin is registered - 混入类，如果未注册混入则返回 null
     * @throws NullPointerException if target is null - 如果 target 为 null
     */
    public Class<?> getMixin(Class<?> target) {
        Objects.requireNonNull(target, "Target class must not be null");
        return mixins.get(target);
    }

    /**
     * Returns whether a mixin is registered for the given target type.
     * 返回是否为给定目标类型注册了混入。
     *
     * @param target the target class - 目标类
     * @return true if a mixin is registered - 如果注册了混入则返回 true
     * @throws NullPointerException if target is null - 如果 target 为 null
     */
    public boolean hasMixin(Class<?> target) {
        Objects.requireNonNull(target, "Target class must not be null");
        return mixins.containsKey(target);
    }

    /**
     * Returns an unmodifiable view of all mixin mappings.
     * 返回所有混入映射的不可修改视图。
     *
     * @return unmodifiable map of target -> mixin mappings - 目标 -> 混入映射的不可修改 Map
     */
    public Map<Class<?>, Class<?>> getMixins() {
        return Collections.unmodifiableMap(mixins);
    }

    /**
     * Removes all mixin mappings.
     * 移除所有混入映射。
     */
    public void clear() {
        mixins.clear();
    }

    /**
     * Returns the number of registered mixin mappings.
     * 返回注册的混入映射数量。
     *
     * @return the number of mixins - 混入数量
     */
    public int size() {
        return mixins.size();
    }

    @Override
    public String toString() {
        return "MixinSource{mixins=" + mixins.size() + "}";
    }
}
