package cloud.opencode.base.id.snowflake;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * IP-based Worker ID Assigner
 * 基于IP的工作节点ID分配器
 *
 * <p>Assigns worker ID based on the last two octets of the host IP address.
 * DatacenterId uses the third octet. This is suitable for most cloud environments.</p>
 * <p>基于主机IP地址的后两个字节分配工作节点ID。数据中心ID使用第三个字节。
 * 这适用于大多数云环境。</p>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <ul>
 *   <li>WorkerId = (IP[2] * 256 + IP[3]) % 32</li>
 *   <li>DatacenterId = IP[2] % 32</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * WorkerIdAssigner assigner = IpBasedAssigner.create();
 * long workerId = assigner.assignWorkerId();
 * long datacenterId = assigner.assignDatacenterId();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Assigns worker ID based on local IP address - 基于本地IP地址分配工作ID</li>
 *   <li>Deterministic assignment for consistent node identification - 确定性分配以实现一致的节点标识</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.1.0
 */
public final class IpBasedAssigner implements WorkerIdAssigner {

    private static final int MAX_WORKER_ID = 31;
    private static final int MAX_DATACENTER_ID = 31;

    private final long workerId;
    private final long datacenterId;

    private IpBasedAssigner() {
        byte[] ip = getLocalIpAddress();
        // Use last two octets for workerId
        int lastOctet = ip[3] & 0xFF;
        int thirdOctet = ip[2] & 0xFF;
        this.workerId = ((thirdOctet << 8) | lastOctet) % (MAX_WORKER_ID + 1);
        this.datacenterId = thirdOctet % (MAX_DATACENTER_ID + 1);
    }

    /**
     * Creates an IP-based assigner
     * 创建基于IP的分配器
     *
     * @return assigner | 分配器
     */
    public static IpBasedAssigner create() {
        return new IpBasedAssigner();
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
        return "IP-Based";
    }

    private static byte[] getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr.getAddress().length == 4) {
                        return addr.getAddress();
                    }
                }
            }
            // Fallback to localhost
            return InetAddress.getLocalHost().getAddress();
        } catch (Exception e) {
            // Fallback to a default value based on system properties
            long hash = System.currentTimeMillis() ^ Runtime.getRuntime().freeMemory();
            return new byte[]{0, 0, (byte) (hash >> 8), (byte) hash};
        }
    }
}
