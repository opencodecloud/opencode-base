package cloud.opencode.base.web.context;

import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * UserContextTest Tests
 * UserContextTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("UserContext Tests")
class UserContextTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("of should create user context with user ID and username")
        void ofShouldCreateUserContextWithUserIdAndUsername() {
            UserContext user = UserContext.of("user1", "testuser");

            assertThat(user.userId()).isEqualTo("user1");
            assertThat(user.username()).isEqualTo("testuser");
            assertThat(user.roles()).isEmpty();
            assertThat(user.permissions()).isEmpty();
        }

        @Test
        @DisplayName("of should create user context with roles")
        void ofShouldCreateUserContextWithRoles() {
            Set<String> roles = Set.of("ADMIN", "USER");

            UserContext user = UserContext.of("user1", "testuser", roles);

            assertThat(user.roles()).containsExactlyInAnyOrder("ADMIN", "USER");
        }

        @Test
        @DisplayName("anonymous should create anonymous user context")
        void anonymousShouldCreateAnonymousUserContext() {
            UserContext user = UserContext.anonymous();

            assertThat(user.userId()).isNull();
            assertThat(user.username()).isEqualTo("anonymous");
        }

        @Test
        @DisplayName("system should create system user context")
        void systemShouldCreateSystemUserContext() {
            UserContext user = UserContext.system();

            assertThat(user.userId()).isEqualTo("SYSTEM");
            assertThat(user.username()).isEqualTo("system");
            assertThat(user.hasRole("SYSTEM")).isTrue();
            assertThat(user.hasPermission("*")).isTrue();
        }

        @Test
        @DisplayName("ANONYMOUS constant should be anonymous user")
        void anonymousConstantShouldBeAnonymousUser() {
            assertThat(UserContext.ANONYMOUS.isAnonymous()).isTrue();
        }
    }

    @Nested
    @DisplayName("Compact Constructor Tests")
    class CompactConstructorTests {

        @Test
        @DisplayName("should handle null roles")
        void shouldHandleNullRoles() {
            UserContext user = new UserContext("user1", "test", null, Set.of(), Map.of());

            assertThat(user.roles()).isEmpty();
        }

        @Test
        @DisplayName("should handle null permissions")
        void shouldHandleNullPermissions() {
            UserContext user = new UserContext("user1", "test", Set.of(), null, Map.of());

            assertThat(user.permissions()).isEmpty();
        }

        @Test
        @DisplayName("should handle null attributes")
        void shouldHandleNullAttributes() {
            UserContext user = new UserContext("user1", "test", Set.of(), Set.of(), null);

            assertThat(user.attributes()).isEmpty();
        }

        @Test
        @DisplayName("should create immutable roles set")
        void shouldCreateImmutableRolesSet() {
            UserContext user = UserContext.of("user1", "test", Set.of("ROLE"));

            assertThatThrownBy(() -> user.roles().add("NEW_ROLE"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Role Check Tests")
    class RoleCheckTests {

        @Test
        @DisplayName("hasRole should return true for existing role")
        void hasRoleShouldReturnTrueForExistingRole() {
            UserContext user = UserContext.of("user1", "test", Set.of("ADMIN", "USER"));

            assertThat(user.hasRole("ADMIN")).isTrue();
        }

        @Test
        @DisplayName("hasRole should return false for non-existing role")
        void hasRoleShouldReturnFalseForNonExistingRole() {
            UserContext user = UserContext.of("user1", "test", Set.of("USER"));

            assertThat(user.hasRole("ADMIN")).isFalse();
        }

        @Test
        @DisplayName("hasAnyRole should return true if any role matches")
        void hasAnyRoleShouldReturnTrueIfAnyRoleMatches() {
            UserContext user = UserContext.of("user1", "test", Set.of("USER"));

            assertThat(user.hasAnyRole("ADMIN", "USER", "GUEST")).isTrue();
        }

        @Test
        @DisplayName("hasAnyRole should return false if no role matches")
        void hasAnyRoleShouldReturnFalseIfNoRoleMatches() {
            UserContext user = UserContext.of("user1", "test", Set.of("USER"));

            assertThat(user.hasAnyRole("ADMIN", "SUPER_ADMIN")).isFalse();
        }
    }

    @Nested
    @DisplayName("Permission Check Tests")
    class PermissionCheckTests {

        @Test
        @DisplayName("hasPermission should return true for existing permission")
        void hasPermissionShouldReturnTrueForExistingPermission() {
            UserContext user = new UserContext("user1", "test", Set.of(),
                Set.of("read", "write"), Map.of());

            assertThat(user.hasPermission("read")).isTrue();
        }

        @Test
        @DisplayName("hasPermission should return false for non-existing permission")
        void hasPermissionShouldReturnFalseForNonExistingPermission() {
            UserContext user = new UserContext("user1", "test", Set.of(),
                Set.of("read"), Map.of());

            assertThat(user.hasPermission("delete")).isFalse();
        }

        @Test
        @DisplayName("hasAnyPermission should return true if any permission matches")
        void hasAnyPermissionShouldReturnTrueIfAnyPermissionMatches() {
            UserContext user = new UserContext("user1", "test", Set.of(),
                Set.of("read"), Map.of());

            assertThat(user.hasAnyPermission("delete", "read")).isTrue();
        }

        @Test
        @DisplayName("hasAnyPermission should return false if no permission matches")
        void hasAnyPermissionShouldReturnFalseIfNoPermissionMatches() {
            UserContext user = new UserContext("user1", "test", Set.of(),
                Set.of("read"), Map.of());

            assertThat(user.hasAnyPermission("delete", "admin")).isFalse();
        }
    }

    @Nested
    @DisplayName("Attribute Access Tests")
    class AttributeAccessTests {

        @Test
        @DisplayName("getAttribute should return attribute value")
        void getAttributeShouldReturnAttributeValue() {
            UserContext user = new UserContext("user1", "test", Set.of(), Set.of(),
                Map.of("email", "test@example.com"));

            String email = user.getAttribute("email");

            assertThat(email).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("getAttribute should return null for missing key")
        void getAttributeShouldReturnNullForMissingKey() {
            UserContext user = UserContext.of("user1", "test");

            String value = user.getAttribute("missing");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("getAttribute with default should return default for missing key")
        void getAttributeWithDefaultShouldReturnDefaultForMissingKey() {
            UserContext user = UserContext.of("user1", "test");

            String value = user.getAttribute("missing", "default");

            assertThat(value).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("Authentication State Tests")
    class AuthenticationStateTests {

        @Test
        @DisplayName("isAnonymous should return true for null user ID")
        void isAnonymousShouldReturnTrueForNullUserId() {
            UserContext user = new UserContext(null, "anonymous", Set.of(), Set.of(), Map.of());

            assertThat(user.isAnonymous()).isTrue();
        }

        @Test
        @DisplayName("isAnonymous should return true for blank user ID")
        void isAnonymousShouldReturnTrueForBlankUserId() {
            UserContext user = new UserContext("  ", "anonymous", Set.of(), Set.of(), Map.of());

            assertThat(user.isAnonymous()).isTrue();
        }

        @Test
        @DisplayName("isAnonymous should return false for valid user ID")
        void isAnonymousShouldReturnFalseForValidUserId() {
            UserContext user = UserContext.of("user1", "test");

            assertThat(user.isAnonymous()).isFalse();
        }

        @Test
        @DisplayName("isAuthenticated should be opposite of isAnonymous")
        void isAuthenticatedShouldBeOppositeOfIsAnonymous() {
            UserContext anonymous = UserContext.anonymous();
            UserContext authenticated = UserContext.of("user1", "test");

            assertThat(anonymous.isAuthenticated()).isFalse();
            assertThat(authenticated.isAuthenticated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Super Admin Tests")
    class SuperAdminTests {

        @Test
        @DisplayName("isSuperAdmin should return true for ADMIN role")
        void isSuperAdminShouldReturnTrueForAdminRole() {
            UserContext user = UserContext.of("user1", "admin", Set.of("ADMIN"));

            assertThat(user.isSuperAdmin()).isTrue();
        }

        @Test
        @DisplayName("isSuperAdmin should return true for SUPER_ADMIN role")
        void isSuperAdminShouldReturnTrueForSuperAdminRole() {
            UserContext user = UserContext.of("user1", "admin", Set.of("SUPER_ADMIN"));

            assertThat(user.isSuperAdmin()).isTrue();
        }

        @Test
        @DisplayName("isSuperAdmin should return true for wildcard permission")
        void isSuperAdminShouldReturnTrueForWildcardPermission() {
            UserContext user = new UserContext("user1", "system", Set.of(),
                Set.of("*"), Map.of());

            assertThat(user.isSuperAdmin()).isTrue();
        }

        @Test
        @DisplayName("isSuperAdmin should return false for regular user")
        void isSuperAdminShouldReturnFalseForRegularUser() {
            UserContext user = UserContext.of("user1", "user", Set.of("USER"));

            assertThat(user.isSuperAdmin()).isFalse();
        }
    }
}
