package cloud.opencode.base.geo.region;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.fence.GeoFence;
import cloud.opencode.base.geo.fence.PolygonFence;

import java.util.List;
import java.util.Optional;

/**
 * Region
 * 区域实体
 *
 * <p>Represents an administrative region with geographic boundaries.</p>
 * <p>表示具有地理边界的行政区域。</p>
 *
 * <p><strong>Region Properties | 区域属性:</strong></p>
 * <ul>
 *   <li>code - Region code (e.g., "110000" for Beijing) | 区域代码</li>
 *   <li>name - Region name (e.g., "北京市") | 区域名称</li>
 *   <li>level - Administrative level | 行政级别</li>
 *   <li>center - Center coordinate | 中心坐标</li>
 *   <li>boundary - Region boundary polygon | 区域边界多边形</li>
 *   <li>parentCode - Parent region code | 父级区域代码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Region beijing = new Region(
 *     "110000",
 *     "北京市",
 *     RegionLevel.PROVINCE,
 *     Coordinate.wgs84(116.4074, 39.9042),
 *     boundaryCoordinates,
 *     "100000"
 * );
 *
 * boolean contains = beijing.contains(somePoint);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Geographic region representation - 地理区域表示</li>
 *   <li>Hierarchical region support - 层级区域支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param code region code | 区域代码
 * @param name region name | 区域名称
 * @param level administrative level | 行政级别
 * @param center center coordinate | 中心坐标
 * @param boundary boundary coordinates (null if no boundary defined) | 边界坐标（无边界定义时为null）
 * @param parentCode parent region code (null for country level) | 父级区域代码（国家级为null）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public record Region(
    String code,
    String name,
    RegionLevel level,
    Coordinate center,
    List<Coordinate> boundary,
    String parentCode
) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造函数
     */
    public Region {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Region code cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Region name cannot be null or blank");
        }
        if (level == null) {
            throw new IllegalArgumentException("Region level cannot be null");
        }
        // Make boundary immutable if present
        if (boundary != null && !boundary.isEmpty()) {
            boundary = List.copyOf(boundary);
        }
    }

    /**
     * Create a region without boundary
     * 创建无边界的区域
     *
     * @param code region code | 区域代码
     * @param name region name | 区域名称
     * @param level administrative level | 行政级别
     * @param center center coordinate | 中心坐标
     * @param parentCode parent region code | 父级区域代码
     * @return Region instance | 区域实例
     */
    public static Region of(String code, String name, RegionLevel level,
                            Coordinate center, String parentCode) {
        return new Region(code, name, level, center, null, parentCode);
    }

    /**
     * Create a region with full details
     * 创建完整详情的区域
     *
     * @param code region code | 区域代码
     * @param name region name | 区域名称
     * @param level administrative level | 行政级别
     * @param center center coordinate | 中心坐标
     * @param boundary boundary coordinates | 边界坐标
     * @param parentCode parent region code | 父级区域代码
     * @return Region instance | 区域实例
     */
    public static Region of(String code, String name, RegionLevel level,
                            Coordinate center, List<Coordinate> boundary,
                            String parentCode) {
        return new Region(code, name, level, center, boundary, parentCode);
    }

    /**
     * Check if a point is contained within this region's boundary
     * 检查点是否在此区域边界内
     *
     * @param point the point to check | 要检查的点
     * @return true if contained, false if not or no boundary defined | 如果包含返回true，否则返回false
     */
    public boolean contains(Coordinate point) {
        if (point == null || boundary == null || boundary.size() < 3) {
            return false;
        }
        GeoFence fence = new PolygonFence(boundary);
        return fence.contains(point);
    }

    /**
     * Get the boundary as a GeoFence
     * 获取边界作为地理围栏
     *
     * @return Optional containing PolygonFence, or empty if no boundary | 包含多边形围栏的Optional，无边界时为空
     */
    public Optional<GeoFence> getBoundaryFence() {
        if (boundary == null || boundary.size() < 3) {
            return Optional.empty();
        }
        return Optional.of(new PolygonFence(boundary));
    }

    /**
     * Check if this region has a defined boundary
     * 检查此区域是否有定义的边界
     *
     * @return true if boundary is defined | 如果已定义边界返回true
     */
    public boolean hasBoundary() {
        return boundary != null && boundary.size() >= 3;
    }

    /**
     * Check if this is a top-level region (no parent)
     * 检查这是否是顶级区域（无父级）
     *
     * @return true if no parent | 如果无父级返回true
     */
    public boolean isTopLevel() {
        return parentCode == null || parentCode.isBlank();
    }

    /**
     * Get a short identifier for display
     * 获取用于显示的短标识符
     *
     * @return short identifier combining code and name | 结合代码和名称的短标识符
     */
    public String getShortId() {
        return code + ":" + name;
    }
}
