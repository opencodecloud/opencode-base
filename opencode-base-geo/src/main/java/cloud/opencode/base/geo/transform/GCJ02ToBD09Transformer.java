package cloud.opencode.base.geo.transform;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.CoordinateUtil;

/**
 * GCJ02 to BD09 Transformer
 * GCJ02转BD09转换器
 *
 * <p>Transforms coordinates from GCJ02 (China Mars) to BD09 (Baidu).</p>
 * <p>将坐标从GCJ02（中国火星坐标）转换到BD09（百度坐标）。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CoordinateTransformer transformer = new GCJ02ToBD09Transformer();
 * Coordinate bd09 = transformer.transform(Coordinate.gcj02(116.4074, 39.9042));
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>GCJ-02 to BD-09 coordinate transform - GCJ-02到BD-09坐标转换</li>
 *   <li>Amap to Baidu conversion - 高德到百度转换</li>
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
public class GCJ02ToBD09Transformer implements CoordinateTransformer {

    /**
     * Singleton instance
     * 单例实例
     */
    public static final GCJ02ToBD09Transformer INSTANCE = new GCJ02ToBD09Transformer();

    @Override
    public Coordinate transform(Coordinate source) {
        return CoordinateUtil.gcj02ToBd09(source);
    }
}
