package cloud.opencode.base.geo.security;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.exception.FenceNotFoundException;
import cloud.opencode.base.geo.exception.GeoSecurityException;
import cloud.opencode.base.geo.fence.CircleFence;
import cloud.opencode.base.geo.fence.GeoFence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * SecureGeoFenceService 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("SecureGeoFenceService 测试")
class SecureGeoFenceServiceTest {

    private SecureGeoFenceService service;
    private final Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
    private final GeoFence circleFence = new CircleFence(beijing, 1000);

    @BeforeEach
    void setUp() {
        service = new SecureGeoFenceService();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造函数")
        void testDefaultConstructor() {
            SecureGeoFenceService svc = new SecureGeoFenceService();
            assertThat(svc.getFenceCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("自定义时钟构造函数")
        void testClockConstructor() {
            Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
            SecureGeoFenceService svc = new SecureGeoFenceService(fixedClock);
            assertThat(svc.getFenceCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("null时钟使用默认")
        void testNullClockUsesDefault() {
            SecureGeoFenceService svc = new SecureGeoFenceService(null);
            assertThat(svc.getFenceCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("registerFence()测试")
    class RegisterFenceTests {

        @Test
        @DisplayName("注册围栏成功")
        void testRegisterFence() {
            service.registerFence("test", circleFence);

            assertThat(service.getFenceCount()).isEqualTo(1);
            assertThat(service.getFence("test")).isPresent();
        }

        @Test
        @DisplayName("null fenceId抛出异常")
        void testRegisterNullFenceId() {
            assertThatThrownBy(() -> service.registerFence(null, circleFence))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("空白fenceId抛出异常")
        void testRegisterBlankFenceId() {
            assertThatThrownBy(() -> service.registerFence("  ", circleFence))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("null fence抛出异常")
        void testRegisterNullFence() {
            assertThatThrownBy(() -> service.registerFence("test", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fence cannot be null");
        }
    }

    @Nested
    @DisplayName("unregisterFence()测试")
    class UnregisterFenceTests {

        @Test
        @DisplayName("注销存在的围栏")
        void testUnregisterExisting() {
            service.registerFence("test", circleFence);
            GeoFence removed = service.unregisterFence("test");

            assertThat(removed).isEqualTo(circleFence);
            assertThat(service.getFenceCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("注销不存在的围栏返回null")
        void testUnregisterNotExisting() {
            GeoFence removed = service.unregisterFence("nonexistent");

            assertThat(removed).isNull();
        }

        @Test
        @DisplayName("注销null返回null")
        void testUnregisterNull() {
            assertThat(service.unregisterFence(null)).isNull();
        }
    }

    @Nested
    @DisplayName("getFence()测试")
    class GetFenceTests {

        @Test
        @DisplayName("获取存在的围栏")
        void testGetExisting() {
            service.registerFence("test", circleFence);

            Optional<GeoFence> result = service.getFence("test");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(circleFence);
        }

        @Test
        @DisplayName("获取不存在的围栏返回empty")
        void testGetNotExisting() {
            Optional<GeoFence> result = service.getFence("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("check()测试")
    class CheckTests {

        @Test
        @DisplayName("点在围栏内返回true")
        void testCheckInside() {
            service.registerFence("test", circleFence);

            boolean result = service.check("test", beijing);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("点在围栏外返回false")
        void testCheckOutside() {
            service.registerFence("test", circleFence);
            Coordinate outside = Coordinate.wgs84(117.0, 40.0);

            boolean result = service.check("test", outside);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("围栏不存在抛出异常")
        void testCheckFenceNotFound() {
            assertThatThrownBy(() -> service.check("nonexistent", beijing))
                .isInstanceOf(FenceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("checkWithTimestamp()测试")
    class CheckWithTimestampTests {

        @Test
        @DisplayName("有效时间戳检查通过")
        void testValidTimestamp() {
            Instant fixedNow = Instant.parse("2024-01-15T10:00:00Z");
            Clock fixedClock = Clock.fixed(fixedNow, ZoneId.of("UTC"));
            service = new SecureGeoFenceService(fixedClock);
            service.registerFence("test", circleFence);

            Instant timestamp = Instant.parse("2024-01-15T09:58:00Z"); // 2分钟前
            boolean result = service.checkWithTimestamp("test", beijing, timestamp, Duration.ofMinutes(5));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("时间戳过旧抛出异常")
        void testTimestampTooOld() {
            Instant fixedNow = Instant.parse("2024-01-15T10:00:00Z");
            Clock fixedClock = Clock.fixed(fixedNow, ZoneId.of("UTC"));
            service = new SecureGeoFenceService(fixedClock);
            service.registerFence("test", circleFence);

            Instant oldTimestamp = Instant.parse("2024-01-15T09:00:00Z"); // 1小时前

            assertThatThrownBy(() ->
                service.checkWithTimestamp("test", beijing, oldTimestamp, Duration.ofMinutes(5)))
                .isInstanceOf(GeoSecurityException.class)
                .hasMessageContaining("too old");
        }

        @Test
        @DisplayName("围栏不存在抛出异常")
        void testTimestampFenceNotFound() {
            assertThatThrownBy(() ->
                service.checkWithTimestamp("nonexistent", beijing, Instant.now(), Duration.ofMinutes(5)))
                .isInstanceOf(FenceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("checkWithVelocity()测试")
    class CheckWithVelocityTests {

        @Test
        @DisplayName("合理速度检查通过")
        void testValidVelocity() {
            Instant t1 = Instant.parse("2024-01-15T10:00:00Z");
            Coordinate pos1 = Coordinate.wgs84(116.4074, 39.9042);

            // 第一次记录位置
            boolean result1 = service.checkWithVelocity("user1", pos1, t1, 150.0);
            assertThat(result1).isTrue();

            // 1小时后移动100km（合理速度）
            Instant t2 = Instant.parse("2024-01-15T11:00:00Z");
            Coordinate pos2 = Coordinate.wgs84(117.4074, 39.9042); // 约100km距离

            boolean result2 = service.checkWithVelocity("user1", pos2, t2, 150.0);
            assertThat(result2).isTrue();
        }

        @Test
        @DisplayName("不可能速度抛出异常")
        void testImpossibleSpeed() {
            Instant t1 = Instant.parse("2024-01-15T10:00:00Z");
            Coordinate pos1 = Coordinate.wgs84(116.4074, 39.9042);

            service.checkWithVelocity("user1", pos1, t1, 150.0);

            // 1分钟后移动1000km（不可能的速度）
            Instant t2 = Instant.parse("2024-01-15T10:01:00Z");
            Coordinate pos2 = Coordinate.wgs84(126.4074, 39.9042); // 约1000km距离

            assertThatThrownBy(() ->
                service.checkWithVelocity("user1", pos2, t2, 150.0))
                .isInstanceOf(GeoSecurityException.class)
                .hasMessageContaining("Impossible travel speed");
        }

        @Test
        @DisplayName("null userId抛出异常")
        void testNullUserId() {
            assertThatThrownBy(() ->
                service.checkWithVelocity(null, beijing, Instant.now(), 150.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID");
        }

        @Test
        @DisplayName("空白userId抛出异常")
        void testBlankUserId() {
            assertThatThrownBy(() ->
                service.checkWithVelocity("  ", beijing, Instant.now(), 150.0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testNullPoint() {
            assertThatThrownBy(() ->
                service.checkWithVelocity("user1", null, Instant.now(), 150.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Point");
        }

        @Test
        @DisplayName("null时间戳抛出异常")
        void testNullTimestamp() {
            assertThatThrownBy(() ->
                service.checkWithVelocity("user1", beijing, null, 150.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Timestamp");
        }

        @Test
        @DisplayName("非正速度抛出异常")
        void testNonPositiveSpeed() {
            assertThatThrownBy(() ->
                service.checkWithVelocity("user1", beijing, Instant.now(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("checkSecure()测试")
    class CheckSecureTests {

        @Test
        @DisplayName("全部检查通过")
        void testAllChecksPassed() {
            Instant fixedNow = Instant.parse("2024-01-15T10:00:00Z");
            Clock fixedClock = Clock.fixed(fixedNow, ZoneId.of("UTC"));
            service = new SecureGeoFenceService(fixedClock);
            service.registerFence("test", circleFence);

            Instant timestamp = Instant.parse("2024-01-15T09:58:00Z");
            boolean result = service.checkSecure("test", "user1", beijing,
                timestamp, Duration.ofMinutes(5), 150.0);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("clearHistory()测试")
    class ClearHistoryTests {

        @Test
        @DisplayName("清除指定用户历史")
        void testClearUserHistory() {
            service.checkWithVelocity("user1", beijing, Instant.now(), 150.0);

            service.clearHistory("user1");

            assertThat(service.getHistory("user1")).isEmpty();
        }

        @Test
        @DisplayName("clearHistory(null)不抛异常")
        void testClearNullHistory() {
            assertThatCode(() -> service.clearHistory(null))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("clearAllHistories()测试")
    class ClearAllHistoriesTests {

        @Test
        @DisplayName("清除所有历史")
        void testClearAllHistories() {
            service.checkWithVelocity("user1", beijing, Instant.now(), 150.0);
            service.checkWithVelocity("user2", beijing, Instant.now(), 150.0);

            service.clearAllHistories();

            assertThat(service.getHistory("user1")).isEmpty();
            assertThat(service.getHistory("user2")).isEmpty();
        }
    }

    @Nested
    @DisplayName("getHistory()测试")
    class GetHistoryTests {

        @Test
        @DisplayName("获取存在的历史")
        void testGetExistingHistory() {
            Instant timestamp = Instant.now();
            service.checkWithVelocity("user1", beijing, timestamp, 150.0);

            Optional<SecureGeoFenceService.LocationHistory> history = service.getHistory("user1");

            assertThat(history).isPresent();
            assertThat(history.get().coordinate()).isEqualTo(beijing);
            assertThat(history.get().timestamp()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("获取不存在的历史返回empty")
        void testGetNonExistingHistory() {
            Optional<SecureGeoFenceService.LocationHistory> history = service.getHistory("nonexistent");

            assertThat(history).isEmpty();
        }
    }

    @Nested
    @DisplayName("LocationHistory Record测试")
    class LocationHistoryTests {

        @Test
        @DisplayName("创建LocationHistory")
        void testCreateLocationHistory() {
            Instant timestamp = Instant.now();
            SecureGeoFenceService.LocationHistory history =
                new SecureGeoFenceService.LocationHistory(beijing, timestamp);

            assertThat(history.coordinate()).isEqualTo(beijing);
            assertThat(history.timestamp()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testNullCoordinate() {
            assertThatThrownBy(() ->
                new SecureGeoFenceService.LocationHistory(null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coordinate cannot be null");
        }

        @Test
        @DisplayName("null时间戳抛出异常")
        void testNullTimestamp() {
            assertThatThrownBy(() ->
                new SecureGeoFenceService.LocationHistory(beijing, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Timestamp cannot be null");
        }
    }
}
