package cloud.opencode.base.web.context;

import java.util.Map;
import java.util.Set;

/**
 * User Context
 * 用户上下文
 *
 * <p>Holds current user information for request processing.</p>
 * <p>保存当前用户信息用于请求处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable user information record - 不可变用户信息记录</li>
 *   <li>Role and permission checking - 角色和权限检查</li>
 *   <li>Extensible attributes map - 可扩展的属性映射</li>
 *   <li>Built-in anonymous and system user support - 内置匿名和系统用户支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create user context
 * UserContext user = UserContext.of("123", "john");
 *
 * // Check roles
 * boolean isAdmin = user.hasRole("ADMIN");
 * boolean hasAccess = user.hasAnyPermission("read", "write");
 *
 * // System and anonymous users
 * UserContext system = UserContext.system();
 * UserContext anon = UserContext.anonymous();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (userId and username can be null for anonymous) - 空值安全: 部分（匿名用户的 userId 和 username 可为 null）</li>
 * </ul>
 *
 * @param userId the user ID | 用户ID
 * @param username the username | 用户名
 * @param roles the user roles | 用户角色
 * @param permissions the user permissions | 用户权限
 * @param attributes additional attributes | 附加属性
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public record UserContext(
    String userId,
    String username,
    Set<String> roles,
    Set<String> permissions,
    Map<String, Object> attributes
) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造函数
     */
    public UserContext {
        roles = roles != null ? Set.copyOf(roles) : Set.of();
        permissions = permissions != null ? Set.copyOf(permissions) : Set.of();
        attributes = attributes != null ? Map.copyOf(attributes) : Map.of();
    }

    /**
     * Create user context
     * 创建用户上下文
     *
     * @param userId the user ID | 用户ID
     * @param username the username | 用户名
     * @return the user context | 用户上下文
     */
    public static UserContext of(String userId, String username) {
        return new UserContext(userId, username, Set.of(), Set.of(), Map.of());
    }

    /**
     * Create user context with roles
     * 创建带角色的用户上下文
     *
     * @param userId the user ID | 用户ID
     * @param username the username | 用户名
     * @param roles the roles | 角色
     * @return the user context | 用户上下文
     */
    public static UserContext of(String userId, String username, Set<String> roles) {
        return new UserContext(userId, username, roles, Set.of(), Map.of());
    }

    /**
     * Check if user has role
     * 检查用户是否拥有角色
     *
     * @param role the role to check | 要检查的角色
     * @return true if has role | 如果拥有角色返回true
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Check if user has any of the roles
     * 检查用户是否拥有任一角色
     *
     * @param checkRoles the roles to check | 要检查的角色
     * @return true if has any role | 如果拥有任一角色返回true
     */
    public boolean hasAnyRole(String... checkRoles) {
        for (String role : checkRoles) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has permission
     * 检查用户是否拥有权限
     *
     * @param permission the permission to check | 要检查的权限
     * @return true if has permission | 如果拥有权限返回true
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Check if user has any of the permissions
     * 检查用户是否拥有任一权限
     *
     * @param checkPermissions the permissions to check | 要检查的权限
     * @return true if has any permission | 如果拥有任一权限返回true
     */
    public boolean hasAnyPermission(String... checkPermissions) {
        for (String permission : checkPermissions) {
            if (permissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get attribute
     * 获取属性
     *
     * @param key the attribute key | 属性键
     * @param <T> the attribute type | 属性类型
     * @return the attribute value or null | 属性值或null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * Get attribute with default value
     * 获取属性（带默认值）
     *
     * @param key the attribute key | 属性键
     * @param defaultValue the default value | 默认值
     * @param <T> the attribute type | 属性类型
     * @return the attribute value or default | 属性值或默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        Object value = attributes.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Check if anonymous user
     * 检查是否匿名用户
     *
     * @return true if anonymous | 如果是匿名用户返回true
     */
    public boolean isAnonymous() {
        return userId == null || userId.isBlank();
    }

    /**
     * Check if authenticated
     * 检查是否已认证
     *
     * @return true if authenticated | 如果已认证返回true
     */
    public boolean isAuthenticated() {
        return !isAnonymous();
    }

    /**
     * Anonymous user constant
     * 匿名用户常量
     */
    public static final UserContext ANONYMOUS = new UserContext(null, null, Set.of(), Set.of(), Map.of());

    /**
     * Get anonymous user context
     * 获取匿名用户上下文
     *
     * @return the anonymous user context | 匿名用户上下文
     */
    public static UserContext anonymous() {
        return new UserContext(null, "anonymous", Set.of(), Set.of(), Map.of());
    }

    /**
     * Get system user context
     * 获取系统用户上下文
     *
     * @return the system user context | 系统用户上下文
     */
    public static UserContext system() {
        return new UserContext("SYSTEM", "system", Set.of("SYSTEM"), Set.of("*"), Map.of());
    }

    /**
     * Check if user is super admin
     * 检查是否超级管理员
     *
     * @return true if super admin | 如果是超级管理员返回true
     */
    public boolean isSuperAdmin() {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN") || hasPermission("*");
    }
}
