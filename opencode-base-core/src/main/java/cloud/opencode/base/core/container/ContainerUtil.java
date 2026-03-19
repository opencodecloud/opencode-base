package cloud.opencode.base.core.container;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Container Utility - Generic size/empty operations for Collection, Map, Array, CharSequence, Optional
 * 容器工具 - 对 Collection、Map、Array、CharSequence、Optional 的通用 size/empty 操作
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generic size() for Collection, Map, Array, CharSequence, Optional - 通用size()方法</li>
 *   <li>Generic isEmpty()/isNotEmpty() checks - 通用isEmpty()/isNotEmpty()检查</li>
 *   <li>Pattern matching for type dispatch - 使用模式匹配进行类型分派</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * int size = ContainerUtil.size(List.of(1, 2, 3));  // 3
 * boolean empty = ContainerUtil.isEmpty(Map.of());    // true
 * boolean notEmpty = ContainerUtil.isNotEmpty("abc"); // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes, null returns 0/true for size/isEmpty - 空值安全: 是，null对size返回0，对isEmpty返回true</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per check - 每次检查 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class ContainerUtil {

    private ContainerUtil() {
    }

    /**
     * Returns the size of the container object.
     * 返回容器对象的大小。
     *
     * <p>Supported types: Collection, Map, CharSequence, Array, Optional.</p>
     *
     * @param obj the container object | 容器对象
     * @return the size, or -1 if the type is not supported | 大小，不支持的类型返回 -1
     */
    public static int size(Object obj) {
        if (obj == null) {
            return 0;
        }
        return switch (obj) {
            case CharSequence cs -> cs.length();
            case Collection<?> c -> c.size();
            case Map<?, ?> m -> m.size();
            case Optional<?> o -> o.isPresent() ? 1 : 0;
            default -> {
                if (obj.getClass().isArray()) {
                    yield Array.getLength(obj);
                }
                yield -1;
            }
        };
    }

    /**
     * Checks if the container object is empty.
     * 检查容器对象是否为空。
     *
     * @param obj the container object | 容器对象
     * @return true if null, empty, or unsupported type | 如果为 null、空或不支持的类型返回 true
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        return switch (obj) {
            case CharSequence cs -> cs.isEmpty();
            case Collection<?> c -> c.isEmpty();
            case Map<?, ?> m -> m.isEmpty();
            case Optional<?> o -> o.isEmpty();
            default -> {
                if (obj.getClass().isArray()) {
                    yield Array.getLength(obj) == 0;
                }
                yield true;
            }
        };
    }

    /**
     * Checks if the container object is not empty.
     * 检查容器对象是否非空。
     *
     * @param obj the container object | 容器对象
     * @return true if not null and not empty | 如果非 null 且非空返回 true
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}
