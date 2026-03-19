/**
 * OpenCode Base Geo Module
 * OpenCode 基础地理信息模块
 *
 * <p>Provides geospatial utilities based on JDK 25, including coordinate transformation,
 * distance calculation, geofencing, GeoHash encoding, and spatial validation.</p>
 * <p>提供基于 JDK 25 的地理空间工具，包括坐标转换、距离计算、地理围栏、GeoHash 编码和空间校验。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Coordinate System Transformation (WGS84/GCJ02/BD09) - 坐标系转换</li>
 *   <li>Distance Calculation (Haversine, Vincenty) - 距离计算</li>
 *   <li>GeoHash Encoding/Decoding - GeoHash 编解码</li>
 *   <li>Geo-Fence (Point-in-Polygon) - 地理围栏</li>
 *   <li>Region &amp; Administrative Division - 行政区划</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
module cloud.opencode.base.geo {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.geo;
    exports cloud.opencode.base.geo.distance;
    exports cloud.opencode.base.geo.exception;
    exports cloud.opencode.base.geo.fence;
    exports cloud.opencode.base.geo.geohash;
    exports cloud.opencode.base.geo.region;
    exports cloud.opencode.base.geo.security;
    exports cloud.opencode.base.geo.transform;
    exports cloud.opencode.base.geo.validation;
}
