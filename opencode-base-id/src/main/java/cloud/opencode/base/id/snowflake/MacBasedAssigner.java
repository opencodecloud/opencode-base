package cloud.opencode.base.id.snowflake;

import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * MAC Address-based Worker ID Assigner
 * 基于MAC地址的工作节点ID分配器
 *
 * <p>Assigns worker ID and datacenter ID based on the host's MAC address.
 * Provides better uniqueness across hosts compared to IP-based assignment.</p>
 * <p>基于主机的MAC地址分配工作节点ID和数据中心ID。
 * 与基于IP的分配相比，能提供更好的跨主机唯一性。</p>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <ul>
 *   <li>WorkerId = (MAC[4] * 256 + MAC[5]) % 32</li>
 *   <li>DatacenterId = (MAC[2] * 256 + MAC[3]) % 32</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * WorkerIdAssigner assigner = MacBasedAssigner.create();
 * long workerId = assigner.assignWorkerId();
 * long datacenterId = assigner.assignDatacenterId();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Assigns worker ID based on MAC address - 基于MAC地址分配工作ID</li>
 *   <li>Hardware-based unique node identification - 基于硬件的唯一节点标识</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.1.0
 */
public final class MacBasedAssigner implements WorkerIdAssigner {

    private static final int MAX_WORKER_ID = 31;
    private static final int MAX_DATACENTER_ID = 31;

    private final long workerId;
    private final long datacenterId;

    private MacBasedAssigner() {
        byte[] mac = getMacAddress();
        // Use last two bytes for workerId
        int byte4 = mac[4] & 0xFF;
        int byte5 = mac[5] & 0xFF;
        this.workerId = ((byte4 << 8) | byte5) % (MAX_WORKER_ID + 1);
        // Use middle two bytes for datacenterId
        int byte2 = mac[2] & 0xFF;
        int byte3 = mac[3] & 0xFF;
        this.datacenterId = ((byte2 << 8) | byte3) % (MAX_DATACENTER_ID + 1);
    }

    /**
     * Creates a MAC-based assigner
     * 创建基于MAC地址的分配器
     *
     * @return assigner | 分配器
     */
    public static MacBasedAssigner create() {
        return new MacBasedAssigner();
    }

    @Override
    public long assignWorkerId() {
        return workerId;
    }

    @Override
    public long assignDatacenterId() {
        return datacenterId;
    }

    @Override
    public String getStrategyName() {
        return "MAC-Based";
    }

    private static byte[] getMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null && mac.length >= 6) {
                    return mac;
                }
            }
        } catch (Exception e) {
            // Fall through to generate random MAC
        }
        // Generate pseudo-random MAC based on system properties
        long hash = System.currentTimeMillis() ^ Runtime.getRuntime().freeMemory()
                ^ Thread.currentThread().getId();
        return new byte[]{
                (byte) (hash >> 40), (byte) (hash >> 32),
                (byte) (hash >> 24), (byte) (hash >> 16),
                (byte) (hash >> 8), (byte) hash
        };
    }
}
