package cloud.opencode.base.core.system;

import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of JVM runtime information.
 * JVM 运行时信息的不可变快照。
 *
 * <p>Captures JVM version, vendor, VM details, uptime, and startup arguments
 * at the time of creation.</p>
 * <p>捕获创建时的 JVM 版本、厂商、VM 详情、运行时间和启动参数。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RuntimeInfo rt = SystemInfo.runtime();
 * System.out.println("Java: " + rt.javaVersion());
 * System.out.println("VM: " + rt.vmName() + " " + rt.vmVersion());
 * System.out.println("Uptime: " + rt.uptime() + " ms");
 * System.out.println("PID: " + rt.pid());
 * }</pre>
 *
 * @param javaVersion    Java runtime version string - Java 运行时版本字符串
 * @param javaVendor     Java vendor - Java 厂商
 * @param javaHome       Java home directory - Java 安装目录
 * @param vmName         VM implementation name - VM 实现名称
 * @param vmVersion      VM implementation version - VM 实现版本
 * @param uptime         JVM uptime in milliseconds - JVM 运行时间（毫秒）
 * @param startTime      JVM start time as epoch milliseconds - JVM 启动时间（毫秒时间戳）
 * @param pid            process ID - 进程 ID
 * @param inputArguments JVM input arguments (unmodifiable) - JVM 输入参数（不可修改）
 * @author Leon Soo
 * @see SystemInfo
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record RuntimeInfo(
        String javaVersion,
        String javaVendor,
        String javaHome,
        String vmName,
        String vmVersion,
        long uptime,
        long startTime,
        long pid,
        List<String> inputArguments
) {

    /**
     * Compact canonical constructor with validation and defensive copy.
     * 带验证和防御性复制的紧凑规范构造器。
     */
    public RuntimeInfo {
        Objects.requireNonNull(javaVersion, "javaVersion must not be null");
        Objects.requireNonNull(javaVendor, "javaVendor must not be null");
        Objects.requireNonNull(javaHome, "javaHome must not be null");
        Objects.requireNonNull(vmName, "vmName must not be null");
        Objects.requireNonNull(vmVersion, "vmVersion must not be null");
        Objects.requireNonNull(inputArguments, "inputArguments must not be null");
        inputArguments = List.copyOf(inputArguments); // unmodifiable defensive copy
    }
}
