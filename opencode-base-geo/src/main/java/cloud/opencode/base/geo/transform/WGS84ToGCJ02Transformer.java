package cloud.opencode.base.geo.transform;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.CoordinateUtil;

/**
 * WGS84 to GCJ02 Transformer
 * WGS84转GCJ02转换器
 *
 * <p>Transforms coordinates from WGS84 (GPS) to GCJ02 (China Mars coordinates).</p>
 * <p>将坐标从WGS84（GPS）转换到GCJ02（中国火星坐标）。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CoordinateTransformer transformer = new WGS84ToGCJ02Transformer();
 * Coordinate gcj02 = transformer.transform(Coordinate.wgs84(116.4074, 39.9042));
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>WGS-84 to GCJ-02 coordinate transform - WGS-84到GCJ-02坐标转换</li>
 *   <li>GPS to China offset conversion - GPS到中国偏移转换</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class WGS84ToGCJ02Transformer implements CoordinateTransformer {

    /**
     * Singleton instance
     * 单例实例
     */
    public static final WGS84ToGCJ02Transformer INSTANCE = new WGS84ToGCJ02Transformer();

    @Override
    public Coordinate transform(Coordinate source) {
        return CoordinateUtil.wgs84ToGcj02(source);
    }
}
