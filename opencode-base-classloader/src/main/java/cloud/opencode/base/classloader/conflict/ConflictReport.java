package cloud.opencode.base.classloader.conflict;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable report of JAR class conflicts
 * JAR 类冲突的不可变报告
 *
 * <p>Contains a mapping of fully-qualified class names to the list of JAR files in which
 * they appear. Only classes found in two or more JARs are included.</p>
 * <p>包含完全限定类名到其出现的 JAR 文件列表的映射。仅包含在两个或多个 JAR 中发现的类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deep defensive copy on construction - 构造时深度防御性复制</li>
 *   <li>Filter conflicts by JAR path - 按 JAR 路径过滤冲突</li>
 *   <li>Human-readable summary generation - 生成人类可读摘要</li>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConflictReport report = JarConflictDetector.detect(jar1, jar2);
 * if (report.hasConflicts()) {
 *     System.out.println(report.summary());
 *     Set<String> classes = report.conflictingClasses();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable, deep copy) - 线程安全: 是（不可变，深度复制）</li>
 * </ul>
 *
 * @param conflicts          className to list of JarInfo where it appears (only entries with 2+ JARs)
 *                           类名到其出现的 JarInfo 列表的映射（仅包含 2+ 个 JAR 的条目）
 * @param totalConflicts     the total number of conflicting class entries | 冲突类条目的总数
 * @param totalClassesScanned the total number of classes scanned | 扫描的类总数
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record ConflictReport(
        Map<String, List<JarInfo>> conflicts,
        int totalConflicts,
        int totalClassesScanned
) {

    /**
     * Creates a new ConflictReport with deep defensive copy
     * 创建新的 ConflictReport 并进行深度防御性复制
     *
     * @param conflicts          className to list of JarInfo | 类名到 JarInfo 列表
     * @param totalConflicts     total conflict count | 冲突总数
     * @param totalClassesScanned total classes scanned | 扫描类总数
     */
    public ConflictReport {
        Objects.requireNonNull(conflicts, "conflicts must not be null");
        // Deep defensive copy: unmodifiable map with unmodifiable list values
        Map<String, List<JarInfo>> copy = new LinkedHashMap<>();
        for (var entry : conflicts.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "class name must not be null");
            Objects.requireNonNull(entry.getValue(), "jar info list must not be null");
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        conflicts = Collections.unmodifiableMap(copy);
    }

    /**
     * Check if any conflicts were detected
     * 检查是否检测到冲突
     *
     * @return true if conflicts exist | 存在冲突返回 true
     */
    public boolean hasConflicts() {
        return !conflicts.isEmpty();
    }

    /**
     * Get the set of all conflicting class names
     * 获取所有冲突类名的集合
     *
     * @return unmodifiable set of conflicting class names | 冲突类名的不可修改集合
     */
    public Set<String> conflictingClasses() {
        return conflicts.keySet();
    }

    /**
     * Get conflicts involving a specific JAR file
     * 获取涉及特定 JAR 文件的冲突
     *
     * @param jarPath the JAR path to filter by | 要过滤的 JAR 路径
     * @return filtered conflict map | 过滤后的冲突映射
     */
    public Map<String, List<JarInfo>> getConflictsForJar(Path jarPath) {
        Objects.requireNonNull(jarPath, "jarPath must not be null");
        Path normalized = jarPath.normalize();
        Map<String, List<JarInfo>> result = new LinkedHashMap<>();
        for (var entry : conflicts.entrySet()) {
            boolean involves = entry.getValue().stream()
                    .anyMatch(info -> info.path().normalize().equals(normalized));
            if (involves) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Generate a human-readable summary of the conflict report
     * 生成冲突报告的人类可读摘要
     *
     * @return summary string | 摘要字符串
     */
    public String summary() {
        var sb = new StringBuilder();
        sb.append("JAR Conflict Report | JAR 冲突报告\n");
        sb.append("==================================\n");
        sb.append("Total classes scanned | 扫描类总数: ").append(totalClassesScanned).append('\n');
        sb.append("Total conflicts | 冲突总数: ").append(totalConflicts).append('\n');

        if (!conflicts.isEmpty()) {
            sb.append("\nConflicting classes | 冲突类:\n");
            for (var entry : conflicts.entrySet()) {
                sb.append("  ").append(entry.getKey()).append(":\n");
                for (JarInfo info : entry.getValue()) {
                    sb.append("    - ").append(info.name());
                    if (info.version() != null) {
                        sb.append(" (v").append(info.version()).append(')');
                    }
                    sb.append('\n');
                }
            }
        } else {
            sb.append("\nNo conflicts detected. | 未检测到冲突。\n");
        }

        return sb.toString();
    }
}
