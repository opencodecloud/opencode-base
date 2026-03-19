package cloud.opencode.base.geo.security;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.exception.FenceNotFoundException;
import cloud.opencode.base.geo.exception.GeoSecurityException;
import cloud.opencode.base.geo.fence.GeoFence;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Secure Geo Fence Service
 * 安全地理围栏服务
 *
 * <p>Secure service for geo-fence operations with timestamp validation
 * and velocity checks to prevent location spoofing.</p>
 * <p>带时间戳验证和速度检查的安全地理围栏服务，用于防止位置欺骗。</p>
 *
 * <p><strong>Security Features | 安全特性:</strong></p>
 * <ul>
 *   <li>Timestamp validation (prevent replay attacks) | 时间戳验证（防止重放攻击）</li>
 *   <li>Velocity checks (detect impossible speeds) | 速度检查（检测不可能的速度）</li>
 *   <li>Location history tracking | 位置历史追踪</li>
 *   <li>Thread-safe operations | 线程安全操作</li>
 * </ul>
 *
 * <p><strong>Typical Velocity Limits | 典型速度限制:</strong></p>
 * <table>
 *   <tr><th>Transportation | 交通方式</th><th>Speed km/h | 速度</th></tr>
 *   <tr><td>Walking</td><td>5-7</td></tr>
 *   <tr><td>Running</td><td>15-25</td></tr>
 *   <tr><td>Cycling</td><td>20-40</td></tr>
 *   <tr><td>Driving</td><td>100-150</td></tr>
 *   <tr><td>High-speed Rail</td><td>300-400</td></tr>
 *   <tr><td>Aircraft</td><td>800-1000</td></tr>
 * </table>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SecureGeoFenceService service = new SecureGeoFenceService();
 *
 * // Register a fence
 * service.registerFence("office", new CircleFence(officeCenter, 500));
 *
 * // Check with timestamp validation
 * boolean inFence = service.checkWithTimestamp(
 *     "office",
 *     userLocation,
 *     Instant.now(),
 *     Duration.ofMinutes(5)
 * );
 *
 * // Check with velocity validation
 * service.checkWithVelocity(
 *     "user123",
 *     newLocation,
 *     Instant.now(),
 *     150.0  // max 150 km/h
 * );
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core functionality - 核心功能</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class SecureGeoFenceService {

    private final Map<String, GeoFence> fences = new ConcurrentHashMap<>();
    private final Map<String, LocationHistory> locationHistories = new ConcurrentHashMap<>();
    private final Clock clock;

    /**
     * Create a SecureGeoFenceService with system default clock
     * 使用系统默认时钟创建安全地理围栏服务
     */
    public SecureGeoFenceService() {
        this(Clock.systemUTC());
    }

    /**
     * Create a SecureGeoFenceService with custom clock (useful for testing)
     * 使用自定义时钟创建安全地理围栏服务（适用于测试）
     *
     * @param clock the clock to use | 使用的时钟
     */
    public SecureGeoFenceService(Clock clock) {
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    /**
     * Register a geo-fence
     * 注册地理围栏
     *
     * @param fenceId unique fence identifier | 唯一的围栏标识符
     * @param fence the geo-fence to register | 要注册的地理围栏
     * @throws IllegalArgumentException if fenceId or fence is null | 如果fenceId或fence为null则抛出异常
     */
    public void registerFence(String fenceId, GeoFence fence) {
        if (fenceId == null || fenceId.isBlank()) {
            throw new IllegalArgumentException("Fence ID cannot be null or blank");
        }
        if (fence == null) {
            throw new IllegalArgumentException("Fence cannot be null");
        }
        fences.put(fenceId, fence);
    }

    /**
     * Unregister a geo-fence
     * 注销地理围栏
     *
     * @param fenceId the fence identifier | 围栏标识符
     * @return the removed fence, or null if not found | 被移除的围栏，未找到时返回null
     */
    public GeoFence unregisterFence(String fenceId) {
        if (fenceId == null) {
            return null;
        }
        return fences.remove(fenceId);
    }

    /**
     * Get a registered fence
     * 获取已注册的围栏
     *
     * @param fenceId the fence identifier | 围栏标识符
     * @return Optional containing the fence, or empty if not found | 包含围栏的Optional，未找到时为空
     */
    public Optional<GeoFence> getFence(String fenceId) {
        return Optional.ofNullable(fences.get(fenceId));
    }

    /**
     * Check if point is in fence with basic validation
     * 使用基本验证检查点是否在围栏内
     *
     * @param fenceId the fence identifier | 围栏标识符
     * @param point the point to check | 要检查的点
     * @return true if point is inside fence | 如果点在围栏内返回true
     * @throws FenceNotFoundException if fence not found | 如果围栏未找到则抛出异常
     */
    public boolean check(String fenceId, Coordinate point) {
        GeoFence fence = fences.get(fenceId);
        if (fence == null) {
            throw new FenceNotFoundException(fenceId);
        }
        return fence.contains(point);
    }

    /**
     * Check with timestamp validation (prevents replay attacks)
     * 使用时间戳验证检查（防止重放攻击）
     *
     * @param fenceId the fence identifier | 围栏标识符
     * @param point the point to check | 要检查的点
     * @param timestamp the location timestamp | 位置时间戳
     * @param maxAge maximum acceptable age of the timestamp | 时间戳最大可接受年龄
     * @return true if point is inside fence | 如果点在围栏内返回true
     * @throws FenceNotFoundException if fence not found | 如果围栏未找到则抛出异常
     * @throws GeoSecurityException if timestamp validation fails | 如果时间戳验证失败则抛出异常
     */
    public boolean checkWithTimestamp(String fenceId, Coordinate point,
                                       Instant timestamp, Duration maxAge) {
        // Validate timestamp freshness
        Instant now = clock.instant();
        Duration age = Duration.between(timestamp, now).abs();

        if (age.compareTo(maxAge) > 0) {
            throw new GeoSecurityException(
                "Location timestamp too old or in future: age=" + age + ", maxAge=" + maxAge);
        }

        return check(fenceId, point);
    }

    /**
     * Check with velocity validation (detects impossible travel speeds)
     * 使用速度验证检查（检测不可能的移动速度）
     *
     * @param userId the user identifier for tracking | 用于追踪的用户标识符
     * @param point the new location point | 新位置点
     * @param timestamp the location timestamp | 位置时间戳
     * @param maxSpeedKmh maximum allowed speed in km/h | 最大允许速度（公里/小时）
     * @return true if velocity check passes | 如果速度检查通过返回true
     * @throws GeoSecurityException if impossible speed detected | 如果检测到不可能的速度则抛出异常
     */
    public boolean checkWithVelocity(String userId, Coordinate point,
                                      Instant timestamp, double maxSpeedKmh) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (maxSpeedKmh <= 0) {
            throw new IllegalArgumentException("Max speed must be positive");
        }

        LocationHistory history = locationHistories.get(userId);

        if (history != null) {
            double distanceKm = calculateDistanceKm(history.coordinate(), point);
            double hoursDiff = Duration.between(history.timestamp(), timestamp).toMillis() / 3600000.0;

            if (hoursDiff > 0) {
                double speedKmh = distanceKm / hoursDiff;
                if (speedKmh > maxSpeedKmh) {
                    throw new GeoSecurityException(
                        String.format("Impossible travel speed detected: %.2f km/h (max: %.2f km/h)",
                            speedKmh, maxSpeedKmh));
                }
            }
        }

        // Update location history
        locationHistories.put(userId, new LocationHistory(point, timestamp));
        return true;
    }

    /**
     * Combined check: fence containment + timestamp + velocity
     * 组合检查：围栏包含 + 时间戳 + 速度
     *
     * @param fenceId the fence identifier | 围栏标识符
     * @param userId the user identifier | 用户标识符
     * @param point the point to check | 要检查的点
     * @param timestamp the location timestamp | 位置时间戳
     * @param maxAge maximum acceptable timestamp age | 最大可接受时间戳年龄
     * @param maxSpeedKmh maximum allowed speed | 最大允许速度
     * @return true if all checks pass and point is in fence | 如果所有检查通过且点在围栏内返回true
     */
    public boolean checkSecure(String fenceId, String userId, Coordinate point,
                                Instant timestamp, Duration maxAge, double maxSpeedKmh) {
        // Perform all security checks
        checkWithVelocity(userId, point, timestamp, maxSpeedKmh);
        return checkWithTimestamp(fenceId, point, timestamp, maxAge);
    }

    /**
     * Clear location history for a user
     * 清除用户的位置历史
     *
     * @param userId the user identifier | 用户标识符
     */
    public void clearHistory(String userId) {
        if (userId != null) {
            locationHistories.remove(userId);
        }
    }

    /**
     * Clear all location histories
     * 清除所有位置历史
     */
    public void clearAllHistories() {
        locationHistories.clear();
    }

    /**
     * Get location history for a user
     * 获取用户的位置历史
     *
     * @param userId the user identifier | 用户标识符
     * @return Optional containing the location history, or empty if not found | 包含位置历史的Optional，未找到时为空
     */
    public Optional<LocationHistory> getHistory(String userId) {
        return Optional.ofNullable(locationHistories.get(userId));
    }

    /**
     * Get the number of registered fences
     * 获取已注册围栏的数量
     *
     * @return number of fences | 围栏数量
     */
    public int getFenceCount() {
        return fences.size();
    }

    /**
     * Calculate distance between two coordinates in kilometers
     * 计算两个坐标之间的距离（公里）
     */
    private double calculateDistanceKm(Coordinate c1, Coordinate c2) {
        // Haversine formula
        double lat1 = Math.toRadians(c1.latitude());
        double lat2 = Math.toRadians(c2.latitude());
        double dLat = lat2 - lat1;
        double dLng = Math.toRadians(c2.longitude() - c1.longitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2)
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371.0 * c; // Earth's radius in km
    }

    /**
     * Location history record for tracking user positions
     * 用于追踪用户位置的位置历史记录
     *
     * @param coordinate the coordinate | 坐标
     * @param timestamp the timestamp | 时间戳
     */
    public record LocationHistory(Coordinate coordinate, Instant timestamp) {
        public LocationHistory {
            if (coordinate == null) {
                throw new IllegalArgumentException("Coordinate cannot be null");
            }
            if (timestamp == null) {
                throw new IllegalArgumentException("Timestamp cannot be null");
            }
        }
    }
}
