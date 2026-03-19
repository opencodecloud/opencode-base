# OpenCode Base Image

Image processing utilities for JDK 25+. Provides resize, crop, rotate, watermark, compress, and format conversion operations with a fluent chainable API.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-image</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Features

- Fluent chainable API for image processing pipelines
- Resize with exact dimensions, aspect ratio preservation, or percentage scaling
- Crop operations: region, center, and square cropping
- Rotation: arbitrary angle, 90/180/270 degrees, horizontal/vertical flip
- Text and image watermarks with position control and opacity
- Quality-based compression
- Format conversion: JPEG, PNG, GIF, BMP, WebP (optional plugin)
- Image validation and dimension checking before full load (OOM prevention)
- Read/write from Path, InputStream, byte array, and Base64
- Thumbnail builder with customizable options
- Security utilities: safe path validation and operation whitelisting
- Image metadata retrieval without full image loading

## Class Reference

### Core

| Class | Description |
|-------|-------------|
| `OpenImage` | Static utility facade for image operations: read, write, resize, crop, rotate, convert, compress, thumbnail |
| `Image` | Chainable image processing wrapper with fluent API for resize, crop, rotate, watermark, compress, convert, and output |
| `ImageFormat` | Enum of supported image formats (JPEG, PNG, GIF, BMP, WEBP) with extension and MIME type support |
| `ImageInfo` | Immutable record containing image metadata: width, height, format, file size, aspect ratio |
| `Position` | Enum defining watermark and overlay positions (e.g., TOP_LEFT, CENTER, BOTTOM_RIGHT) |

### Internal Operations

| Class | Description |
|-------|-------------|
| `CompressOp` | Internal compression operation implementation |
| `ConvertOp` | Internal format conversion and grayscale operations |
| `CropOp` | Internal crop operations: region, center, and square |
| `ResizeOp` | Internal resize operations: exact, fit, scale, width, and height |
| `RotateOp` | Internal rotation and flip operations |
| `WatermarkOp` | Internal watermark application for text and image watermarks |

### Watermark

| Class | Description |
|-------|-------------|
| `Watermark` | Base interface for watermark definitions |
| `TextWatermark` | Text watermark configuration with font, color, opacity, and position |
| `ImageWatermark` | Image watermark configuration with overlay image, opacity, and position |

### Thumbnail

| Class | Description |
|-------|-------------|
| `ThumbnailBuilder` | Builder for creating thumbnails with configurable size, format, and quality |

### Validation

| Class | Description |
|-------|-------------|
| `ImageValidator` | Validates image files for format, dimensions, and content integrity |

### Security

| Class | Description |
|-------|-------------|
| `SafeImageService` | Secure image processing service with operation whitelisting |
| `SafePathUtil` | Path validation utilities to prevent directory traversal attacks |
| `ImageOperation` | Enum of allowed image operations for security whitelisting |

### Exceptions

| Class | Description |
|-------|-------------|
| `ImageException` | Base exception for image processing errors |
| `ImageIOException` | Thrown for general image I/O errors |
| `ImageReadException` | Thrown when image reading fails |
| `ImageWriteException` | Thrown when image writing fails |
| `ImageFormatException` | Thrown for unsupported or invalid image formats |
| `ImageOperationException` | Thrown when an image operation fails |
| `ImageResourceException` | Thrown for image resource access errors |
| `ImageTimeoutException` | Thrown when an image operation times out |
| `ImageTooLargeException` | Thrown when an image exceeds size limits |
| `ImageValidationException` | Thrown when image validation fails |
| `ImageErrorCode` | Error code enum for categorized image error handling |

## Quick Start

```java
// Read and chain operations
OpenImage.read(Path.of("photo.jpg"))
    .resize(800, 600)
    .rotate(90)
    .watermark("Copyright 2026", Position.BOTTOM_RIGHT)
    .compress(0.8f)
    .save(Path.of("output.jpg"));

// Quick resize
OpenImage.resize(inputPath, outputPath, 800, 600);

// Quick format conversion
OpenImage.convert(inputPath, outputPath, ImageFormat.PNG);

// Create thumbnail
OpenImage.thumbnail(inputPath, outputPath, 200);

// Thumbnail builder
OpenImage.thumbnail()
    .source(Path.of("photo.jpg"))
    .size(200, 200)
    .format(ImageFormat.PNG)
    .output(Path.of("thumb.png"))
    .create();

// Get image info without loading full image
ImageInfo info = OpenImage.getInfo(Path.of("photo.jpg"));
System.out.println(info.width() + "x" + info.height());
System.out.println("Format: " + info.format());
System.out.println("Size: " + info.fileSizeFormatted());

// Read from byte array / Base64
Image img = OpenImage.read(bytes);
Image img2 = OpenImage.fromBase64(base64String);

// Convert to bytes / Base64
byte[] bytes = img.toBytes(ImageFormat.PNG);
String base64 = img.toBase64();

// Grayscale, flip
OpenImage.grayscale(inputPath, outputPath);
OpenImage.flipHorizontal(inputPath, outputPath);

// Validate image
boolean valid = OpenImage.isValidImage(Path.of("file.jpg"));
```

## Requirements

- Java 25+
- Optional: `com.twelvemonkeys.imageio:imageio-webp:3.12.0` for WebP support

## License

Apache License 2.0
