# OpenCode Base Geo

**Geographic utilities library for Java 25+**

`opencode-base-geo` provides comprehensive geographic operations including coordinate transformation (WGS84/GCJ02/BD09), distance calculation (Haversine/Vincenty), geo-fence checking, GeoHash encoding/decoding, polyline encoding, track simplification, WKT interop, and coordinate validation with security features.

## Features

### Core Features
- **Distance Calculation**: Haversine (fast, ~0.5% error) and Vincenty (precise, ~0.5mm error)
- **Coordinate Transformation**: WGS84, GCJ02 (China), BD09 (Baidu) bidirectional conversion
- **Geo-Fence**: Circle, polygon, rectangle, and cross-dateline fence containment checks
- **GeoHash**: Encoding, decoding, neighbor lookup, proximity search, and precision levels
- **BoundingBox**: First-class immutable type with contains/intersects/union/expand operations
- **Bearing & Destination**: Calculate bearing between points and destination from start point

### Advanced Features
- **Polyline Codec**: Google Encoded Polyline format encoding/decoding for GPS tracks
- **Track Simplification**: Ramer-Douglas-Peucker algorithm for reducing GPS track points
- **WKT Support**: Lightweight Well-Known Text parsing/serialization (POINT/LINESTRING/POLYGON)
- **GeoHash Search**: Proximity-based hash search solving boundary edge cases
- **Coordinate Validation**: Range checking, NaN/Infinity detection
- **Region Service**: Hierarchical region management (province/city/district)
- **Coordinate Masking**: Privacy-preserving coordinate obfuscation
- **Secure GeoFence Service**: Fence operations with timestamp validation and velocity checks
- **Location Spoofing Detection**: Detect suspicious coordinate patterns
- **China Bounds Check**: Quick check if coordinates fall within China

### Geometry Utilities
- **Centroid**: Geographic center of a coordinate set (vector averaging)
- **Interpolation**: Point along great-circle path between two coordinates
- **Point-to-Segment Distance**: Shortest distance from a point to a line segment
- **Point-to-Polyline Distance**: Shortest distance from a point to a polyline
- **Compass Direction**: 16-point compass direction from bearing
- **Total Distance**: Sum distance along a coordinate path
- **Polygon Area/Circumference**: Spherical area and perimeter calculation

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-geo</artifactId>
    <version>1.0.3</version>
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

### BoundingBox

```java
// Create from coordinates
BoundingBox bbox = BoundingBox.fromCoordinates(coordinateList);

// Create from center + radius
BoundingBox search = BoundingBox.fromCenter(beijing, 5000);  // 5km radius

// Operations
boolean contains = bbox.contains(point);
boolean overlaps = bbox.intersects(otherBox);
BoundingBox merged = bbox.union(otherBox);
BoundingBox expanded = bbox.expand(1000);  // expand by 1km
Coordinate center = bbox.center();
Set<String> hashes = bbox.toGeoHashes(6);  // GeoHash coverage
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

// Named precision levels
GeoHashPrecision precision = GeoHashPrecision.CITY;  // precision 5, ~5km
int value = precision.getValue();  // 5

// Auto-select precision for a given radius
GeoHashPrecision auto = GeoHashPrecision.forRadius(2.0);  // NEIGHBORHOOD (6)

// Proximity search (solves boundary edge case)
Set<String> hashes = OpenGeo.geoHashSearch(39.9042, 116.4074, 5000);  // 5km radius
Set<String> hashes2 = OpenGeo.geoHashSearch(39.9042, 116.4074, 5000, 6);  // specified precision
```

### Polyline Encoding/Decoding

```java
// Encode GPS track to Google Encoded Polyline
List<Coordinate> track = List.of(
    Coordinate.wgs84(-120.2, 38.5),
    Coordinate.wgs84(-120.95, 40.7),
    Coordinate.wgs84(-126.453, 43.252)
);
String encoded = OpenGeo.encodePolyline(track);

// Decode back
List<Coordinate> decoded = OpenGeo.decodePolyline(encoded);
```

### Track Simplification

```java
// Simplify GPS track using Ramer-Douglas-Peucker
List<Coordinate> simplified = OpenGeo.simplifyTrack(gpsTrack, 50);  // 50m tolerance

// Total track distance
double totalDist = OpenGeo.trackDistance(gpsTrack);
```

### WKT (Well-Known Text) Interop

```java
import cloud.opencode.base.geo.wkt.WktCodec;

// Parse from PostGIS
Coordinate point = WktCodec.parsePoint("POINT(116.4074 39.9042)");
List<Coordinate> line = WktCodec.parseLineString("LINESTRING(116.0 39.0, 117.0 40.0)");

// Parse polygon
List<List<Coordinate>> rings = WktCodec.parsePolygon(
    "POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))");

// Serialize for database
String wkt = WktCodec.toWkt(beijing);  // "POINT(116.4074 39.9042)"
String lineWkt = WktCodec.lineStringToWkt(coordinates);
String polyWkt = WktCodec.polygonToWkt(exteriorRing);
```

### Coordinate Masking (Privacy)

```java
import cloud.opencode.base.geo.security.CoordinateMasker;

// Random offset within 500 meters
Coordinate masked = CoordinateMasker.mask(location, 500);

// Reduce to ~100m precision (3 decimal places)
Coordinate reduced = CoordinateMasker.reducePrecision(location, 3);

// Align to GeoHash grid (~1.2km)
Coordinate gridAligned = CoordinateMasker.maskByGeoHash(location, 6);

// Convenience: city-level (~5km), neighborhood (~1km), block (~150m)
Coordinate city = CoordinateMasker.maskToCity(location);
Coordinate neighborhood = CoordinateMasker.maskToNeighborhood(location);
Coordinate block = CoordinateMasker.maskToBlock(location);
```

### Navigation & Geometry

```java
// Bearing between two points
double bearing = OpenGeo.bearing(beijing, shanghai);
String direction = OpenGeo.compassDirection(bearing);  // "SE", "NNW", etc.

// Destination from start point
Coordinate dest = OpenGeo.destination(beijing, 100000, 135.0);

// Midpoint & interpolation
Coordinate mid = OpenGeo.midpoint(beijing, shanghai);
Coordinate quarter = OpenGeo.interpolate(beijing, shanghai, 0.25);

// Centroid of multiple points
Coordinate center = OpenGeo.centroid(points);

// Point-to-segment distance
double dist = OpenGeo.distanceToSegment(point, segStart, segEnd);
double dist2 = OpenGeo.distanceToPolyline(point, polyline);
```

## Class Reference

### Root Package (`cloud.opencode.base.geo`)
| Class | Description |
|-------|-------------|
| `OpenGeo` | Main facade with static methods for all geographic operations |
| `Coordinate` | Immutable record representing a geographic coordinate point (lng, lat, system) |
| `CoordinateSystem` | Enum of supported coordinate systems (WGS84, GCJ02, BD09) |
| `CoordinateUtil` | Low-level coordinate utility methods |
| `GeoUtil` | General geographic calculation utilities (centroid, distance, interpolation, compass) |
| `BoundingBox` | Immutable record for axis-aligned bounding box with rich operations |

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
| `GeoHash` | GeoHash interface with encode/decode operations |
| `GeoHashEncoder` | GeoHash encoding algorithm implementation |
| `GeoHashUtil` | Utility methods for GeoHash encoding, decoding, and neighbor lookup |
| `GeoHashPrecision` | Named precision levels (CONTINENT to DOOR) with human-readable descriptions |
| `GeoHashSearch` | Proximity-based GeoHash search solving boundary edge cases |

### Polyline Package (`cloud.opencode.base.geo.polyline`)
| Class | Description |
|-------|-------------|
| `PolylineCodec` | Google Encoded Polyline format encoder/decoder |
| `TrackSimplifier` | Ramer-Douglas-Peucker track simplification algorithm |

### WKT Package (`cloud.opencode.base.geo.wkt`)
| Class | Description |
|-------|-------------|
| `WktCodec` | Lightweight WKT (Well-Known Text) parser and serializer |
| `WktType` | Enum of supported WKT geometry types (POINT, LINESTRING, POLYGON) |

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
| `SecureGeoFenceService` | Geo-fence operations with timestamp validation and velocity checks |

### Validation Package (`cloud.opencode.base.geo.validation`)
| Class | Description |
|-------|-------------|
| `CoordinateValidator` | Coordinate range, format, and consistency validation |

### Exception Package (`cloud.opencode.base.geo.exception`)
| Class | Description |
|-------|-------------|
| `GeoException` | Base exception for geographic operations (extends `OpenException`) |
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
