package cloud.opencode.base.geo.transform;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.CoordinateSystem;
import cloud.opencode.base.geo.CoordinateUtil;

/**
 * BD09 to WGS84 Transformer
 * BD09转WGS84转换器
 *
 * <p>Transforms coordinates from BD09 (Baidu) to WGS84 (GPS).</p>
 * <p>将坐标从BD09（百度坐标）转换到WGS84（GPS）。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CoordinateTransformer transformer = new BD09ToWGS84Transformer();
 * Coordinate wgs84 = transformer.transform(Coordinate.bd09(116.4074, 39.9042));
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>BD-09 to WGS-84 coordinate transform - BD-09到WGS-84坐标转换</li>
 *   <li>Baidu to GPS conversion - 百度到GPS转换</li>
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
public class BD09ToWGS84Transformer implements CoordinateTransformer {

    /**
     * Singleton instance
     * 单例实例
     */
    public static final BD09ToWGS84Transformer INSTANCE = new BD09ToWGS84Transformer();

    @Override
    public Coordinate transform(Coordinate source) {
        return CoordinateUtil.transform(source, CoordinateSystem.WGS84);
    }
}
