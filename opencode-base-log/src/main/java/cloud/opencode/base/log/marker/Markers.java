package cloud.opencode.base.log.marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Markers - Marker Factory and Predefined Markers
 * 标记工厂和预定义标记
 *
 * <p>This class provides factory methods for creating markers and defines
 * commonly used predefined markers for categorizing log events.</p>
 * <p>此类提供创建标记的工厂方法，并定义用于分类日志事件的常用预定义标记。</p>
 *
 * <p><strong>Predefined Markers | 预定义标记:</strong></p>
 * <ul>
 *   <li>SECURITY - Security-related events - 安全相关事件</li>
 *   <li>PERFORMANCE - Performance metrics - 性能指标</li>
 *   <li>AUDIT - Audit trail events - 审计跟踪事件</li>
 *   <li>BUSINESS - Business logic events - 业务逻辑事件</li>
 *   <li>SYSTEM - System-level events - 系统级事件</li>
 *   <li>DATABASE - Database operations - 数据库操作</li>
 *   <li>NETWORK - Network operations - 网络操作</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for creating markers - 创建标记的工厂方法</li>
 *   <li>10 predefined markers (SECURITY, AUDIT, PERFORMANCE, etc.) - 10 个预定义标记（SECURITY、AUDIT、PERFORMANCE 等）</li>
 *   <li>Thread-safe marker registry - 线程安全的标记注册表</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get predefined marker
 * Marker audit = Markers.AUDIT;
 * OpenLog.info(audit, "User action logged");
 * 
 * // Create custom marker
 * Marker custom = Markers.getMarker("MY_MARKER");
 * 
 * // Create marker with references
 * Marker child = Markers.getMarker("CHILD", Markers.SECURITY);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class Markers {

    private static final Map<String, Marker> MARKERS = new ConcurrentHashMap<>();

    // ==================== Predefined Markers ====================

    /** Security-related events - 安全相关事件 */
    public static final Marker SECURITY = getMarker("SECURITY");

    /** Performance metrics - 性能指标 */
    public static final Marker PERFORMANCE = getMarker("PERFORMANCE");

    /** Audit trail events - 审计跟踪事件 */
    public static final Marker AUDIT = getMarker("AUDIT");

    /** Business logic events - 业务逻辑事件 */
    public static final Marker BUSINESS = getMarker("BUSINESS");

    /** System-level events - 系统级事件 */
    public static final Marker SYSTEM = getMarker("SYSTEM");

    /** Database operations - 数据库操作 */
    public static final Marker DATABASE = getMarker("DATABASE");

    /** Network operations - 网络操作 */
    public static final Marker NETWORK = getMarker("NETWORK");

    /** Sensitive data marker - 敏感数据标记 */
    public static final Marker SENSITIVE = getMarker("SENSITIVE");

    /** Confidential marker - 机密标记 */
    public static final Marker CONFIDENTIAL = getMarker("CONFIDENTIAL");

    /** Entry/Exit marker - 入口/出口标记 */
    public static final Marker ENTRY_EXIT = getMarker("ENTRY_EXIT");

    private Markers() {
        // Utility class
    }

    // ==================== Factory Methods ====================

    /**
     * Gets or creates a marker with the specified name.
     * 获取或创建具有指定名称的标记。
     *
     * @param name the marker name - 标记名称
     * @return the marker - 标记
     */
    public static Marker getMarker(String name) {
        Objects.requireNonNull(name, "Marker name must not be null");
        return MARKERS.computeIfAbsent(name, BasicMarker::new);
    }

    /**
     * Gets or creates a marker with references.
     * 获取或创建带引用的标记。
     *
     * @param name       the marker name - 标记名称
     * @param references the reference markers - 引用标记
     * @return the marker - 标记
     */
    public static Marker getMarker(String name, Marker... references) {
        Marker marker = getMarker(name);
        for (Marker ref : references) {
            marker.add(ref);
        }
        return marker;
    }

    /**
     * Checks if a marker with the specified name exists.
     * 检查具有指定名称的标记是否存在。
     *
     * @param name the marker name - 标记名称
     * @return true if exists - 如果存在返回 true
     */
    public static boolean exists(String name) {
        return MARKERS.containsKey(name);
    }

    /**
     * Removes a marker from the registry.
     * 从注册表中移除标记。
     *
     * @param name the marker name - 标记名称
     * @return true if removed - 如果移除成功返回 true
     */
    public static boolean detachMarker(String name) {
        return MARKERS.remove(name) != null;
    }

    /**
     * Returns all registered marker names.
     * 返回所有注册的标记名称。
     *
     * @return the marker names - 标记名称
     */
    public static Set<String> getMarkerNames() {
        return Set.copyOf(MARKERS.keySet());
    }

    // ==================== Basic Marker Implementation ====================

    private static final class BasicMarker implements Marker {
        private final String name;
        private final List<Marker> references = new ArrayList<>();

        BasicMarker(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public synchronized void add(Marker reference) {
            if (reference == null) {
                throw new IllegalArgumentException("Marker reference cannot be null");
            }
            if (!contains(reference) && !reference.contains(this)) {
                references.add(reference);
            }
        }

        @Override
        public synchronized boolean remove(Marker reference) {
            return references.remove(reference);
        }

        @Override
        public boolean hasReferences() {
            return !references.isEmpty();
        }

        @Override
        public Iterator<Marker> iterator() {
            return Collections.unmodifiableList(new ArrayList<>(references)).iterator();
        }

        @Override
        public boolean contains(Marker other) {
            if (other == null) {
                return false;
            }
            return containsWithCycleDetection(other, Collections.newSetFromMap(new IdentityHashMap<>()));
        }

        private boolean containsWithCycleDetection(Marker other, Set<Marker> visited) {
            if (this.equals(other)) {
                return true;
            }
            if (!visited.add(this)) {
                return false; // Cycle detected, stop recursion
            }
            for (Marker ref : references) {
                if (ref instanceof BasicMarker bm) {
                    if (bm.containsWithCycleDetection(other, visited)) {
                        return true;
                    }
                } else if (ref.contains(other)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean contains(String name) {
            if (name == null) {
                return false;
            }
            return containsNameWithCycleDetection(name, Collections.newSetFromMap(new IdentityHashMap<>()));
        }

        private boolean containsNameWithCycleDetection(String targetName, Set<Marker> visited) {
            if (this.name.equals(targetName)) {
                return true;
            }
            if (!visited.add(this)) {
                return false; // Cycle detected, stop recursion
            }
            for (Marker ref : references) {
                if (ref instanceof BasicMarker bm) {
                    if (bm.containsNameWithCycleDetection(targetName, visited)) {
                        return true;
                    }
                } else if (ref.contains(targetName)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Marker other)) return false;
            return this.name.equals(other.getName());
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            if (!hasReferences()) {
                return name;
            }
            StringBuilder sb = new StringBuilder(name);
            sb.append(" [ ");
            Iterator<Marker> it = iterator();
            while (it.hasNext()) {
                sb.append(it.next().getName());
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(" ]");
            return sb.toString();
        }
    }
}
