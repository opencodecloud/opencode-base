# OpenCode Base Geo

**Geographic utilities library for Java 25+**

`opencode-base-geo` provides comprehensive geographic operations including coordinate transformation (WGS84/GCJ02/BD09), distance calculation (Haversine/Vincenty), geo-fence checking, GeoHash encoding/decoding, and coordinate validation with security features.

## Features

### Core Features
- **Distance Calculation**: Haversine (fast, ~0.5% error) and Vincenty (precise, ~0.5mm error)
- **Coordinate Transformation**: WGS84, GCJ02 (China), BD09 (Baidu) bidirectional conversion
- **Geo-Fence**: Circle, polygon, rectangle, and cross-dateline fence containment checks
- **GeoHash**: Encoding, decoding, neighbor lookup, and bounding box calculation
- **Bearing & Destination**: Calculate bearing between points and destination from start point

### Advanced Features
- **Coordinate Validation**: Range checking, NaN/Infinity detection
- **Region Service**: Hierarchical region management (province/city/district)
- **Coordinate Masking**: Privacy-preserving coordinate obfuscation
- **Secure GeoFence Service**: Rate-limited and audited fence operations
- **Location Spoofing Detection**: Detect suspicious coordinate patterns
- **China Bounds Check**: Quick check if coordinates fall within China

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-geo</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.geo.*;

// Create coordinates
Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
Coordinate shanghai = Coordinate.wgs84(121.4737, 31.2304);

// Distance calculation
double distance = OpenGeo.distance(beijing, shanghai);
System.out.println(OpenGeo.formatDistance(distance));  // "1068.0公里"

// Precise distance (Vincenty)
double precise = OpenGeo.distancePrecise(beijing, shanghai);

// Coordinate transformation
Coordinate gcj02 = OpenGeo.wgs84ToGcj02(116.4074, 39.9042);
Coordinate bd09 = OpenGeo.gcj02ToBd09(gcj02.longitude(), gcj02.latitude());
Coordinate wgs84 = OpenGeo.bd09ToWgs84(bd09.longitude(), bd09.latitude());
```

### Geo-Fence

```java
// Circle fence
boolean inCircle = OpenGeo.inCircle(point, center, 1000);  // 1000m radius

// Polygon fence
List<Coordinate> polygon = List.of(
    Coordinate.wgs84(116.0, 39.0),
    Coordinate.wgs84(117.0, 39.0),
    Coordinate.wgs84(117.0, 40.0),
    Coordinate.wgs84(116.0, 40.0)
);
boolean inPolygon = OpenGeo.inPolygon(point, polygon);

// Rectangle fence
boolean inRect = OpenGeo.inRectangle(point, southwest, northeast);
```

### GeoHash

```java
// Encode
String hash = OpenGeo.geoHash(116.4074, 39.9042, 8);  // "wx4g0bm6"

// Decode
Coordinate decoded = OpenGeo.fromGeoHash("wx4g0bm6");

// Neighbors
List<String> neighbors = OpenGeo.geoHashNeighbors("wx4g0bm6");

// Bounding box
double[] bbox = OpenGeo.geoHashBoundingBox("wx4g0bm6");
```

### Navigation

```java
// Bearing between two points
double bearing = OpenGeo.bearing(beijing, shanghai);  // degrees, 0=North

// Destination from start point
Coordinate dest = OpenGeo.destination(beijing, 100000, 135.0);  // 100km at 135°

// Midpoint
Coordinate mid = OpenGeo.midpoint(beijing, shanghai);

// Validate coordinates
boolean valid = OpenGeo.isValidCoordinate(116.4074, 39.9042);  // true
boolean inChina = OpenGeo.isInChina(116.4074, 39.9042);        // true
```

## Class Reference

### Root Package (`cloud.opencode.base.geo`)
| Class | Description |
|-------|-------------|
| `OpenGeo` | Main facade with static methods for distance, transformation, fence, and GeoHash |
| `Coordinate` | Immutable record representing a geographic coordinate point (lng, lat, system) |
| `CoordinateSystem` | Enum of supported coordinate systems (WGS84, GCJ02, BD09) |
| `CoordinateUtil` | Low-level coordinate utility methods |
| `GeoUtil` | General geographic calculation utilities |

### Distance Package (`cloud.opencode.base.geo.distance`)
| Class | Description |
|-------|-------------|
| `DistanceCalculator` | Interface for distance calculation algorithms |
| `DistanceCalculatorFactory` | Factory with accuracy level selection (FAST/PRECISE) |
| `HaversineCalculator` | Haversine formula implementation (~0.5% accuracy, fast) |
| `VincentyCalculator` | Vincenty formula implementation (~0.5mm accuracy, precise) |

### Fence Package (`cloud.opencode.base.geo.fence`)
| Class | Description |
|-------|-------------|
| `GeoFence` | Interface for geo-fence containment checks |
| `CircleFence` | Circular geo-fence defined by center and radius |
| `PolygonFence` | Polygon geo-fence defined by vertex list |
| `RectangleFence` | Rectangle geo-fence defined by southwest and northeast corners |
| `CrossDateLineFence` | Geo-fence that handles international date line crossing |

### GeoHash Package (`cloud.opencode.base.geo.geohash`)
| Class | Description |
|-------|-------------|
| `GeoHash` | GeoHash value object with encode/decode operations |
| `GeoHashEncoder` | GeoHash encoding algorithm implementation |
| `GeoHashUtil` | Utility methods for GeoHash encoding, decoding, and neighbor lookup |

### Transform Package (`cloud.opencode.base.geo.transform`)
| Class | Description |
|-------|-------------|
| `CoordinateTransformer` | Interface for coordinate system transformers |
| `WGS84ToGCJ02Transformer` | WGS84 to GCJ02 (China) transformation |
| `GCJ02ToBD09Transformer` | GCJ02 to BD09 (Baidu) transformation |
| `BD09ToWGS84Transformer` | BD09 to WGS84 transformation |

### Region Package (`cloud.opencode.base.geo.region`)
| Class | Description |
|-------|-------------|
| `Region` | Hierarchical region entity (province/city/district) |
| `RegionLevel` | Enum of region hierarchy levels |
| `RegionService` | Region lookup and management service |

### Security Package (`cloud.opencode.base.geo.security`)
| Class | Description |
|-------|-------------|
| `CoordinateMasker` | Privacy-preserving coordinate obfuscation utility |
| `SecureGeoFenceService` | Rate-limited and audited geo-fence operations |

### Validation Package (`cloud.opencode.base.geo.validation`)
| Class | Description |
|-------|-------------|
| `CoordinateValidator` | Coordinate range, format, and consistency validation |

### Exception Package (`cloud.opencode.base.geo.exception`)
| Class | Description |
|-------|-------------|
| `GeoException` | Base exception for geographic operations |
| `CoordinateException` | Base exception for coordinate errors |
| `CoordinateOutOfRangeException` | Coordinate values outside valid range |
| `CoordinateTransformException` | Coordinate transformation failure |
| `InvalidCoordinateException` | Invalid coordinate format or values |
| `FenceException` | Base exception for fence operations |
| `FenceCheckException` | Fence containment check failure |
| `FenceNotFoundException` | Referenced fence not found |
| `InvalidFenceException` | Invalid fence definition |
| `GeoHashException` | Base exception for GeoHash operations |
| `GeoHashEncodeException` | GeoHash encoding failure |
| `InvalidGeoHashException` | Invalid GeoHash string |
| `GeoSecurityException` | Geographic security violation |
| `LocationSpoofingException` | Suspicious location pattern detected |
| `TimestampException` | Timestamp validation failure |
| `GeoErrorCode` | Enumeration of geographic error codes |

## Requirements

- Java 25+ (uses records, sealed interfaces, pattern matching)
- No external dependencies for core functionality

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
