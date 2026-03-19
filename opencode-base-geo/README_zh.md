# OpenCode Base Geo

**地理工具库，适用于 Java 25+**

`opencode-base-geo` 提供全面的地理操作，包括坐标转换（WGS84/GCJ02/BD09）、距离计算（Haversine/Vincenty）、地理围栏检查、GeoHash 编解码以及带安全特性的坐标验证。

## 功能特性

### 核心功能
- **距离计算**：Haversine（快速，误差约 0.5%）和 Vincenty（精确，误差约 0.5mm）
- **坐标转换**：WGS84、GCJ02（国测局）、BD09（百度）双向转换
- **地理围栏**：圆形、多边形、矩形和跨日期线围栏包含检查
- **GeoHash**：编码、解码、邻居查找和边界框计算
- **方位角与目标点**：计算两点间方位角和从起点出发的目标点

### 高级功能
- **坐标验证**：范围检查、NaN/Infinity 检测
- **区域服务**：层次化区域管理（省/市/区）
- **坐标脱敏**：隐私保护的坐标模糊化
- **安全围栏服务**：限流和审计的围栏操作
- **位置欺骗检测**：检测可疑坐标模式
- **中国范围检查**：快速判断坐标是否在中国境内

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-geo</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.geo.*;

// 创建坐标
Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
Coordinate shanghai = Coordinate.wgs84(121.4737, 31.2304);

// 距离计算
double distance = OpenGeo.distance(beijing, shanghai);
System.out.println(OpenGeo.formatDistance(distance));  // "1068.0公里"

// 精确距离（Vincenty）
double precise = OpenGeo.distancePrecise(beijing, shanghai);

// 坐标转换
Coordinate gcj02 = OpenGeo.wgs84ToGcj02(116.4074, 39.9042);
Coordinate bd09 = OpenGeo.gcj02ToBd09(gcj02.longitude(), gcj02.latitude());
Coordinate wgs84 = OpenGeo.bd09ToWgs84(bd09.longitude(), bd09.latitude());
```

### 地理围栏

```java
// 圆形围栏
boolean inCircle = OpenGeo.inCircle(point, center, 1000);  // 半径1000米

// 多边形围栏
List<Coordinate> polygon = List.of(
    Coordinate.wgs84(116.0, 39.0),
    Coordinate.wgs84(117.0, 39.0),
    Coordinate.wgs84(117.0, 40.0),
    Coordinate.wgs84(116.0, 40.0)
);
boolean inPolygon = OpenGeo.inPolygon(point, polygon);

// 矩形围栏
boolean inRect = OpenGeo.inRectangle(point, southwest, northeast);
```

### GeoHash

```java
// 编码
String hash = OpenGeo.geoHash(116.4074, 39.9042, 8);  // "wx4g0bm6"

// 解码
Coordinate decoded = OpenGeo.fromGeoHash("wx4g0bm6");

// 邻居
List<String> neighbors = OpenGeo.geoHashNeighbors("wx4g0bm6");

// 边界框
double[] bbox = OpenGeo.geoHashBoundingBox("wx4g0bm6");
```

### 导航

```java
// 两点间方位角
double bearing = OpenGeo.bearing(beijing, shanghai);  // 度数，0=北

// 从起点计算目标点
Coordinate dest = OpenGeo.destination(beijing, 100000, 135.0);  // 100公里，方位角135°

// 中点
Coordinate mid = OpenGeo.midpoint(beijing, shanghai);

// 验证坐标
boolean valid = OpenGeo.isValidCoordinate(116.4074, 39.9042);  // true
boolean inChina = OpenGeo.isInChina(116.4074, 39.9042);        // true
```

## 类参考

### 根包 (`cloud.opencode.base.geo`)
| 类 | 说明 |
|---|------|
| `OpenGeo` | 主门面类，提供距离、转换、围栏和 GeoHash 的静态方法 |
| `Coordinate` | 表示地理坐标点的不可变记录（经度、纬度、坐标系） |
| `CoordinateSystem` | 支持的坐标系枚举（WGS84、GCJ02、BD09） |
| `CoordinateUtil` | 底层坐标工具方法 |
| `GeoUtil` | 通用地理计算工具 |

### 距离包 (`cloud.opencode.base.geo.distance`)
| 类 | 说明 |
|---|------|
| `DistanceCalculator` | 距离计算算法接口 |
| `DistanceCalculatorFactory` | 带精度级别选择的工厂（FAST/PRECISE） |
| `HaversineCalculator` | Haversine 公式实现（精度约 0.5%，快速） |
| `VincentyCalculator` | Vincenty 公式实现（精度约 0.5mm，精确） |

### 围栏包 (`cloud.opencode.base.geo.fence`)
| 类 | 说明 |
|---|------|
| `GeoFence` | 地理围栏包含检查接口 |
| `CircleFence` | 由圆心和半径定义的圆形地理围栏 |
| `PolygonFence` | 由顶点列表定义的多边形地理围栏 |
| `RectangleFence` | 由西南角和东北角定义的矩形地理围栏 |
| `CrossDateLineFence` | 处理国际日期变更线穿越的地理围栏 |

### GeoHash 包 (`cloud.opencode.base.geo.geohash`)
| 类 | 说明 |
|---|------|
| `GeoHash` | GeoHash 值对象，支持编解码操作 |
| `GeoHashEncoder` | GeoHash 编码算法实现 |
| `GeoHashUtil` | GeoHash 编码、解码和邻居查找工具方法 |

### 转换包 (`cloud.opencode.base.geo.transform`)
| 类 | 说明 |
|---|------|
| `CoordinateTransformer` | 坐标系转换器接口 |
| `WGS84ToGCJ02Transformer` | WGS84 到 GCJ02（国测局）转换 |
| `GCJ02ToBD09Transformer` | GCJ02 到 BD09（百度）转换 |
| `BD09ToWGS84Transformer` | BD09 到 WGS84 转换 |

### 区域包 (`cloud.opencode.base.geo.region`)
| 类 | 说明 |
|---|------|
| `Region` | 层次化区域实体（省/市/区） |
| `RegionLevel` | 区域层次级别枚举 |
| `RegionService` | 区域查找和管理服务 |

### 安全包 (`cloud.opencode.base.geo.security`)
| 类 | 说明 |
|---|------|
| `CoordinateMasker` | 隐私保护的坐标模糊化工具 |
| `SecureGeoFenceService` | 限流和审计的地理围栏操作 |

### 验证包 (`cloud.opencode.base.geo.validation`)
| 类 | 说明 |
|---|------|
| `CoordinateValidator` | 坐标范围、格式和一致性验证 |

### 异常包 (`cloud.opencode.base.geo.exception`)
| 类 | 说明 |
|---|------|
| `GeoException` | 地理操作的基础异常 |
| `CoordinateException` | 坐标错误的基础异常 |
| `CoordinateOutOfRangeException` | 坐标值超出有效范围 |
| `CoordinateTransformException` | 坐标转换失败 |
| `InvalidCoordinateException` | 无效的坐标格式或值 |
| `FenceException` | 围栏操作的基础异常 |
| `FenceCheckException` | 围栏包含检查失败 |
| `FenceNotFoundException` | 引用的围栏未找到 |
| `InvalidFenceException` | 无效的围栏定义 |
| `GeoHashException` | GeoHash 操作的基础异常 |
| `GeoHashEncodeException` | GeoHash 编码失败 |
| `InvalidGeoHashException` | 无效的 GeoHash 字符串 |
| `GeoSecurityException` | 地理安全违规 |
| `LocationSpoofingException` | 检测到可疑位置模式 |
| `TimestampException` | 时间戳验证失败 |
| `GeoErrorCode` | 地理错误码枚举 |

## 环境要求

- Java 25+（使用 record、密封接口、模式匹配）
- 核心功能无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
