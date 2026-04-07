package cloud.opencode.base.classloader.leak;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable report of a detected ClassLoader leak
 * 检测到的 ClassLoader 泄漏的不可变报告
 *
 * <p>Contains diagnostic information about a ClassLoader that was garbage-collected
 * without being explicitly closed, indicating a potential resource leak.</p>
 *
 * <p>包含未显式关闭就被垃圾回收的 ClassLoader 的诊断信息，
 * 表明存在潜在的资源泄漏。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变记录)</li>
 * </ul>
 *
 * @param name             the name of the leaked ClassLoader | 泄漏的 ClassLoader 名称
 * @param level            the detection level used | 使用的检测级别
 * @param creationStack    the stack trace at creation time, or empty array if level is SIMPLE |
 *                         创建时的栈轨迹，如果级别为 SIMPLE 则为空数组
 * @param loadedClassCount the number of classes loaded at tracking time | 追踪时已加载的类数量
 * @param createdAtNanos   the creation timestamp in nanoseconds (System.nanoTime) |
 *                         创建时间戳（纳秒，System.nanoTime）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record LeakReport(
        String name,
        LeakDetection level,
        StackTraceElement[] creationStack,
        int loadedClassCount,
        long createdAtNanos
) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造器
     *
     * @throws NullPointerException     if name, level or creationStack is null |
     *                                  当 name、level 或 creationStack 为 null 时
     * @throws IllegalArgumentException if loadedClassCount is negative |
     *                                  当 loadedClassCount 为负数时
     */
    public LeakReport {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(level, "level must not be null");
        Objects.requireNonNull(creationStack, "creationStack must not be null");
        if (loadedClassCount < 0) {
            throw new IllegalArgumentException("loadedClassCount must not be negative: " + loadedClassCount);
        }
        // Defensive copy to preserve immutability
        creationStack = creationStack.clone();
    }

    /**
     * Returns a defensive copy of the creation stack trace
     * 返回创建栈轨迹的防御性副本
     *
     * @return copy of the creation stack trace | 创建栈轨迹的副本
     */
    @Override
    public StackTraceElement[] creationStack() {
        return creationStack.clone();
    }

    /**
     * Format the leak report as a human-readable string
     * 将泄漏报告格式化为人类可读的字符串
     *
     * @return formatted report string | 格式化的报告字符串
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("LeakReport{name='").append(name).append('\'');
        sb.append(", level=").append(level);
        sb.append(", loadedClassCount=").append(loadedClassCount);
        sb.append(", createdAtNanos=").append(createdAtNanos);
        if (creationStack.length > 0) {
            sb.append(", creationStack=[");
            for (int i = 0; i < creationStack.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(creationStack[i]);
            }
            sb.append(']');
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Custom equals that compares creationStack by content
     * 自定义 equals，按内容比较 creationStack
     *
     * @param o the object to compare | 要比较的对象
     * @return true if equal | 如果相等返回 true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeakReport that)) return false;
        return loadedClassCount == that.loadedClassCount
                && createdAtNanos == that.createdAtNanos
                && name.equals(that.name)
                && level == that.level
                && Arrays.equals(creationStack, that.creationStack);
    }

    /**
     * Custom hashCode consistent with equals
     * 与 equals 一致的自定义 hashCode
     *
     * @return hash code | 哈希码
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(name, level, loadedClassCount, createdAtNanos);
        result = 31 * result + Arrays.hashCode(creationStack);
        return result;
    }
}
