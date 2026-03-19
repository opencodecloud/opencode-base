package cloud.opencode.base.log.marker;

import java.util.Iterator;

/**
 * Marker Interface - Log Event Marker
 * 标记接口 - 日志事件标记
 *
 * <p>Markers are named objects used to enrich log statements. A marker can reference
 * other markers, creating a hierarchy of markers.</p>
 * <p>标记是用于丰富日志语句的命名对象。标记可以引用其他标记，创建标记层次结构。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Create and use a marker
 * Marker security = Markers.getMarker("SECURITY");
 * OpenLog.info(security, "User {} authenticated", userId);
 *
 * // Create a marker with references
 * Marker audit = Markers.getMarker("AUDIT", security);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Named marker for log event categorization - 用于日志事件分类的命名标记</li>
 *   <li>Hierarchical marker references - 分层标记引用</li>
 *   <li>Contains check by marker or name - 按标记或名称检查包含关系</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (throws on null reference) - 空值安全: 否（null 引用抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public interface Marker {

    /**
     * Returns the name of this marker.
     * 返回此标记的名称。
     *
     * @return the marker name - 标记名称
     */
    String getName();

    /**
     * Adds a reference to another marker.
     * 添加对另一个标记的引用。
     *
     * @param reference the marker to reference - 要引用的标记
     */
    void add(Marker reference);

    /**
     * Removes a reference to another marker.
     * 移除对另一个标记的引用。
     *
     * @param reference the marker reference to remove - 要移除的标记引用
     * @return true if the reference was removed - 如果移除成功返回 true
     */
    boolean remove(Marker reference);

    /**
     * Checks if this marker has any references.
     * 检查此标记是否有任何引用。
     *
     * @return true if this marker has references - 如果有引用返回 true
     */
    boolean hasReferences();

    /**
     * Returns an iterator over the referenced markers.
     * 返回引用标记的迭代器。
     *
     * @return iterator of referenced markers - 引用标记的迭代器
     */
    Iterator<Marker> iterator();

    /**
     * Checks if this marker contains the specified marker.
     * 检查此标记是否包含指定的标记。
     *
     * @param other the marker to check - 要检查的标记
     * @return true if this marker contains the other marker - 如果包含返回 true
     */
    boolean contains(Marker other);

    /**
     * Checks if this marker contains a marker with the specified name.
     * 检查此标记是否包含具有指定名称的标记。
     *
     * @param name the marker name to check - 要检查的标记名称
     * @return true if this marker contains a marker with the name - 如果包含返回 true
     */
    boolean contains(String name);

    /**
     * Checks if this marker is equal to the specified marker.
     * 检查此标记是否等于指定的标记。
     *
     * <p>Two markers are equal if they have the same name.</p>
     * <p>如果两个标记具有相同的名称，则它们相等。</p>
     *
     * @param other the object to compare - 要比较的对象
     * @return true if equal - 如果相等返回 true
     */
    @Override
    boolean equals(Object other);

    /**
     * Returns the hash code of this marker.
     * 返回此标记的哈希码。
     *
     * @return the hash code - 哈希码
     */
    @Override
    int hashCode();
}
