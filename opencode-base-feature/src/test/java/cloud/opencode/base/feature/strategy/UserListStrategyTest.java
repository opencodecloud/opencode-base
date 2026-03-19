package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * UserListStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("UserListStrategy 测试")
class UserListStrategyTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建用户列表策略")
        void testConstructor() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1", "user2"));

            assertThat(strategy.getAllowedUsers()).containsExactlyInAnyOrder("user1", "user2");
        }

        @Test
        @DisplayName("null用户列表转为空集合")
        void testNullUserList() {
            UserListStrategy strategy = new UserListStrategy(null);

            assertThat(strategy.getAllowedUsers()).isEmpty();
        }

        @Test
        @DisplayName("空用户列表")
        void testEmptyUserList() {
            UserListStrategy strategy = new UserListStrategy(Set.of());

            assertThat(strategy.getAllowedUsers()).isEmpty();
        }

        @Test
        @DisplayName("用户列表是不可变的")
        void testImmutableUserList() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1"));

            assertThatThrownBy(() -> strategy.getAllowedUsers().add("user2"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("用户在列表中时启用")
        void testUserInListEnabled() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1", "user2"));
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("user1"))).isTrue();
            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("user2"))).isTrue();
        }

        @Test
        @DisplayName("用户不在列表中时禁用")
        void testUserNotInListDisabled() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1", "user2"));
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("user3"))).isFalse();
        }

        @Test
        @DisplayName("无用户ID时禁用")
        void testNoUserIdDisabled() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1", "user2"));
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isFalse();
        }

        @Test
        @DisplayName("空用户列表始终禁用")
        void testEmptyListAlwaysDisabled() {
            UserListStrategy strategy = new UserListStrategy(Set.of());
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("anyuser"))).isFalse();
        }
    }

    @Nested
    @DisplayName("getAllowedUsers() 测试")
    class GetAllowedUsersTests {

        @Test
        @DisplayName("获取允许的用户集合")
        void testGetAllowedUsers() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1", "user2", "user3"));

            assertThat(strategy.getAllowedUsers()).containsExactlyInAnyOrder("user1", "user2", "user3");
        }
    }

    @Nested
    @DisplayName("isUserAllowed() 测试")
    class IsUserAllowedTests {

        @Test
        @DisplayName("用户在列表中")
        void testUserAllowed() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1", "user2"));

            assertThat(strategy.isUserAllowed("user1")).isTrue();
            assertThat(strategy.isUserAllowed("user2")).isTrue();
        }

        @Test
        @DisplayName("用户不在列表中")
        void testUserNotAllowed() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1", "user2"));

            assertThat(strategy.isUserAllowed("user3")).isFalse();
        }

        @Test
        @DisplayName("null用户ID")
        void testNullUserId() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1"));

            assertThat(strategy.isUserAllowed(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("包含用户数量")
        void testToString() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1", "user2", "user3"));

            assertThat(strategy.toString())
                    .contains("UserListStrategy")
                    .contains("3 users");
        }
    }

    @Nested
    @DisplayName("实现EnableStrategy测试")
    class ImplementsEnableStrategyTests {

        @Test
        @DisplayName("实现EnableStrategy接口")
        void testImplementsEnableStrategy() {
            UserListStrategy strategy = new UserListStrategy(Set.of("user1"));

            assertThat(strategy).isInstanceOf(EnableStrategy.class);
        }
    }
}
