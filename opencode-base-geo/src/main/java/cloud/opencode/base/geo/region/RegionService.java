package cloud.opencode.base.geo.region;

import cloud.opencode.base.geo.Coordinate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Region Service
 * 区域服务
 *
 * <p>Service for managing and querying administrative regions.</p>
 * <p>管理和查询行政区域的服务。</p>
 *
 * <p><strong>Features | 功能:</strong></p>
 * <ul>
 *   <li>Register and unregister regions | 注册和注销区域</li>
 *   <li>Query regions by code or name | 按代码或名称查询区域</li>
 *   <li>Find regions containing a point | 查找包含某点的区域</li>
 *   <li>Get child/parent regions | 获取子级/父级区域</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This service is thread-safe and can be used in concurrent environments.</p>
 * <p>此服务是线程安全的，可以在并发环境中使用。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RegionService service = new RegionService();
 *
 * // Register regions
 * service.register(beijing);
 * service.register(haidian);
 *
 * // Find region by code
 * Optional<Region> region = service.findByCode("110000");
 *
 * // Find regions containing a point
 * List<Region> regions = service.findByCoordinate(somePoint);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Region query and lookup service - 区域查询和查找服务</li>
 *   <li>Coordinate-to-region mapping - 坐标到区域映射</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class RegionService {

    private final Map<String, Region> regionsByCode = new ConcurrentHashMap<>();
    private final Map<String, List<Region>> regionsByParent = new ConcurrentHashMap<>();

    /**
     * Create a new RegionService
     * 创建新的区域服务
     */
    public RegionService() {
    }

    /**
     * Register a region
     * 注册区域
     *
     * @param region the region to register | 要注册的区域
     * @throws IllegalArgumentException if region is null | 如果区域为null则抛出异常
     */
    public void register(Region region) {
        if (region == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }
        regionsByCode.put(region.code(), region);

        // Index by parent
        String parentCode = region.parentCode();
        if (parentCode != null && !parentCode.isBlank()) {
            regionsByParent.computeIfAbsent(parentCode, _ -> new CopyOnWriteArrayList<>()).add(region);
        }
    }

    /**
     * Register multiple regions
     * 注册多个区域
     *
     * @param regions the regions to register | 要注册的区域列表
     */
    public void registerAll(List<Region> regions) {
        if (regions != null) {
            regions.forEach(this::register);
        }
    }

    /**
     * Unregister a region by code
     * 按代码注销区域
     *
     * @param code the region code | 区域代码
     * @return the removed region, or null if not found | 被移除的区域，未找到时返回null
     */
    public Region unregister(String code) {
        if (code == null) {
            return null;
        }
        Region removed = regionsByCode.remove(code);
        if (removed != null && removed.parentCode() != null) {
            List<Region> siblings = regionsByParent.get(removed.parentCode());
            if (siblings != null) {
                siblings.remove(removed);
            }
        }
        return removed;
    }

    /**
     * Find a region by code
     * 按代码查找区域
     *
     * @param code the region code | 区域代码
     * @return Optional containing the region, or empty if not found | 包含区域的Optional，未找到时为空
     */
    public Optional<Region> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(regionsByCode.get(code));
    }

    /**
     * Find regions by name (partial match, case-insensitive)
     * 按名称查找区域（部分匹配，不区分大小写）
     *
     * @param name the name to search | 要搜索的名称
     * @return list of matching regions | 匹配的区域列表
     */
    public List<Region> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Collections.emptyList();
        }
        String lowerName = name.toLowerCase();
        return regionsByCode.values().stream()
            .filter(r -> r.name().toLowerCase().contains(lowerName))
            .toList();
    }

    /**
     * Find regions by administrative level
     * 按行政级别查找区域
     *
     * @param level the region level | 区域级别
     * @return list of regions at that level | 该级别的区域列表
     */
    public List<Region> findByLevel(RegionLevel level) {
        if (level == null) {
            return Collections.emptyList();
        }
        return regionsByCode.values().stream()
            .filter(r -> r.level() == level)
            .toList();
    }

    /**
     * Find all regions containing a coordinate point
     * 查找包含某坐标点的所有区域
     *
     * @param coordinate the coordinate to check | 要检查的坐标
     * @return list of regions containing the point, sorted by level (highest first) | 包含该点的区域列表，按级别排序（最高级别在前）
     */
    public List<Region> findByCoordinate(Coordinate coordinate) {
        if (coordinate == null) {
            return Collections.emptyList();
        }
        return regionsByCode.values().stream()
            .filter(r -> r.contains(coordinate))
            .sorted((r1, r2) -> Integer.compare(r1.level().getCode(), r2.level().getCode()))
            .toList();
    }

    /**
     * Get child regions of a parent region
     * 获取父级区域的子区域
     *
     * @param parentCode the parent region code | 父级区域代码
     * @return list of child regions | 子区域列表
     */
    public List<Region> getChildren(String parentCode) {
        if (parentCode == null) {
            return Collections.emptyList();
        }
        List<Region> children = regionsByParent.get(parentCode);
        return children != null ? new ArrayList<>(children) : Collections.emptyList();
    }

    /**
     * Get the parent region of a region
     * 获取区域的父级区域
     *
     * @param code the region code | 区域代码
     * @return Optional containing the parent region, or empty if not found or is top-level | 包含父级区域的Optional，未找到或顶级时为空
     */
    public Optional<Region> getParent(String code) {
        return findByCode(code)
            .filter(r -> r.parentCode() != null && !r.parentCode().isBlank())
            .flatMap(r -> findByCode(r.parentCode()));
    }

    /**
     * Get all ancestors of a region (from immediate parent to top-level)
     * 获取区域的所有祖先（从直接父级到顶级）
     *
     * @param code the region code | 区域代码
     * @return list of ancestor regions in order from immediate parent to top | 祖先区域列表，从直接父级到顶级排序
     */
    public List<Region> getAncestors(String code) {
        List<Region> ancestors = new ArrayList<>();
        Optional<Region> current = findByCode(code);

        while (current.isPresent()) {
            Region region = current.get();
            if (region.parentCode() == null || region.parentCode().isBlank()) {
                break;
            }
            Optional<Region> parent = findByCode(region.parentCode());
            parent.ifPresent(ancestors::add);
            current = parent;
        }

        return ancestors;
    }

    /**
     * Get the total number of registered regions
     * 获取已注册区域的总数
     *
     * @return number of regions | 区域数量
     */
    public int size() {
        return regionsByCode.size();
    }

    /**
     * Check if a region with the given code exists
     * 检查给定代码的区域是否存在
     *
     * @param code the region code | 区域代码
     * @return true if exists | 如果存在返回true
     */
    public boolean exists(String code) {
        return code != null && regionsByCode.containsKey(code);
    }

    /**
     * Clear all registered regions
     * 清除所有已注册的区域
     */
    public void clear() {
        regionsByCode.clear();
        regionsByParent.clear();
    }

    /**
     * Get all registered regions
     * 获取所有已注册的区域
     *
     * @return list of all regions | 所有区域列表
     */
    public List<Region> getAll() {
        return new ArrayList<>(regionsByCode.values());
    }
}
