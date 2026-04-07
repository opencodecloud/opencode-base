# OpenCode Base Image

Image processing utilities for JDK 25+. Provides resize, crop, rotate, watermark, compress, format conversion, color adjustment, edge detection, image stitching, and more — all with a fluent chainable API.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-image</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Features

- Fluent chainable API for image processing pipelines
- Resize with exact dimensions, aspect ratio preservation, percentage scaling, or **progressive high-quality downscaling**
- Crop operations: region, center, square, **aspect-ratio crop**
- Rotation: arbitrary angle, 90/180/270 degrees, horizontal/vertical flip
- Text, image, and **tiled watermarks** with position control and opacity
- **Brightness, contrast, sepia, and invert** color filters
- **Dominant color extraction** via K-Means clustering
- **Image stitching**: horizontal, vertical, and grid layouts
- **Padding and border** addition with configurable color
- **Image overlay** compositing with opacity control
- Quality-based compression
- Format conversion: JPEG, PNG, GIF, BMP, WebP (optional plugin)
- Advanced filters: Gaussian blur, median blur, box blur, bilateral filter, sharpen
- Edge detection: Sobel, Canny, Laplacian
- Histogram: equalization and CLAHE
- Thresholding: fixed, Otsu, adaptive
- Image comparison: pixel diff, SSIM, MSE, PSNR
- Perceptual hashing: aHash, dHash, pHash
- EXIF: auto-orientation and tag stripping
- Morphology: erode, dilate, open, close, top-hat, black-hat
- Feature detection: Harris corners, Shi-Tomasi
- Image analysis: connected components, contour finding, template matching
- Color space conversions: RGB ↔ HSV, RGB ↔ Lab
- Geometric transforms: affine and four-point perspective
- Drawing operations: shapes, lines, text
- Responsive image set generation for web
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
| `Image` | Chainable image processing wrapper with fluent API for all operations |
| `ImageStitch` | Stitch multiple images horizontally, vertically, or in a grid layout |
| `ImageFormat` | Enum of supported image formats (JPEG, PNG, GIF, BMP, WEBP) with extension and MIME type support |
| `ImageInfo` | Immutable record containing image metadata: width, height, format, file size, aspect ratio |
| `Position` | Enum defining watermark and overlay positions (e.g., TOP_LEFT, CENTER, BOTTOM_RIGHT) |

### Internal Operations

| Class | Description |
|-------|-------------|
| `CompressOp` | Quality-based JPEG compression |
| `ConvertOp` | Format conversion and grayscale operations |
| `CropOp` | Crop operations: region, center, square, aspect-ratio |
| `ResizeOp` | Resize: exact, fit, scale, progressive multi-step |
| `RotateOp` | Rotation and flip operations |
| `WatermarkOp` | Text/image watermark application and tiled watermark |
| `PaddingOp` | Padding and border addition with configurable color |
| `OverlayOp` | Opacity-controlled image overlay compositing |

### Color Operations

| Class | Description |
|-------|-------------|
| `BrightnessContrastOp` | Adjust brightness (multiplicative) and contrast (centered at 128) |
| `ColorFilterOp` | Sepia (warm matrix transform) and invert (255-RGB) filters |
| `ColorExtractor` | Extract dominant colors via K-Means clustering on downsampled image |
| `ColorSpaceOp` | Color space conversions: RGB ↔ HSV, RGB ↔ Lab |
| `GammaOp` | Gamma correction |
| `SaturationOp` | Saturation adjustment |
| `WhiteBalanceOp` | Gray World white balance correction |

### Filters

| Class | Description |
|-------|-------------|
| `GaussianBlurOp` | Gaussian blur with configurable sigma |
| `MedianBlurOp` | Median blur (histogram-based) for salt-and-pepper noise |
| `BoxBlurOp` | Box blur (mean filter) |
| `BilateralFilterOp` | Edge-preserving bilateral filter |
| `SharpenOp` | Unsharp mask sharpening |

### Edge Detection

| Class | Description |
|-------|-------------|
| `SobelOp` | Sobel gradient edge detection |
| `CannyOp` | Canny edge detection with non-maximum suppression and hysteresis |
| `LaplacianOp` | Laplacian second-derivative edge detection |

### Histogram

| Class | Description |
|-------|-------------|
| `HistogramEqualizationOp` | Global histogram equalization |
| `ClaheOp` | Contrast Limited Adaptive Histogram Equalization |
| `HistogramOp` | Histogram computation and statistics |

### Thresholding

| Class | Description |
|-------|-------------|
| `ThresholdOp` | Fixed threshold binary conversion |
| `OtsuOp` | Otsu automatic threshold selection |
| `AdaptiveThresholdOp` | Block-based adaptive thresholding (mean method) |

### Image Comparison

| Class | Description |
|-------|-------------|
| `ImageCompare` | Pixel diff, MSE, PSNR, SSIM, and similarity metrics |
| `PerceptualHash` | Perceptual hashing: aHash, dHash, pHash with Hamming distance |

### Analysis

| Class | Description |
|-------|-------------|
| `ConnectedComponentsOp` | Connected component labeling and statistics |
| `ContourFinderOp` | Contour detection and boundary tracing |
| `TemplateMatchOp` | Template matching via normalized cross-correlation |

### Feature Detection

| Class | Description |
|-------|-------------|
| `HarrisCornerOp` | Harris corner detection |
| `ShiTomasiOp` | Shi-Tomasi (Good Features to Track) corner detection |

### Morphology

| Class | Description |
|-------|-------------|
| `MorphologyOp` | Erode, dilate, open, close, top-hat, black-hat operations |
| `StructuringElement` | Structuring element definition (square, cross, etc.) |

### Transform

| Class | Description |
|-------|-------------|
| `AffineTransformOp` | Affine transformations (scale, rotate, shear, translate) |
| `PerspectiveTransformOp` | Four-point perspective transformation |

### Drawing

| Class | Description |
|-------|-------------|
| `DrawOp` | Draw shapes, lines, and text onto images |

### EXIF

| Class | Description |
|-------|-------------|
| `ExifOp` | Pure Java EXIF parser: read, strip, auto-orient |
| `ExifInfo` | Immutable record of parsed EXIF metadata |
| `ExifTag` | Enum of supported EXIF tags |

### Responsive

| Class | Description |
|-------|-------------|
| `ResponsiveBuilder` | Generate responsive image sets (multiple sizes) for web use |

### Kernel Infrastructure

| Class | Description |
|-------|-------------|
| `PixelOp` | Zero-copy pixel array access and ARGB channel extraction |
| `KernelOp` | 2D convolution engine |
| `SeparableKernelOp` | Optimized separable (1D+1D) convolution |
| `LookupTableOp` | 256-entry lookup table application for O(1) per-pixel transforms |
| `IntegralImage` | Integral image (summed-area table) for O(1) region queries |
| `ChannelOp` | Channel split/merge for per-channel processing |

### Watermark

| Class | Description |
|-------|-------------|
| `Watermark` | Sealed base interface for watermark definitions |
| `TextWatermark` | Text watermark: font, color, opacity, position |
| `ImageWatermark` | Image watermark: overlay image, opacity, position |

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
| `ImageOperation` | Functional interface for image processing operations (used with `SafeImageService.process`) |

### Exceptions

| Class | Description |
|-------|-------------|
| `ImageException` | Base exception for image processing errors (extends `OpenException`, component="Image") |
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

// Progressive high-quality downscale (better than single-step for large reductions)
Image.from(path).resizeProgressive(100, 100).save(outputPath);

// Crop to 16:9 aspect ratio from center
Image.from(path).cropToAspectRatio(16, 9).save(outputPath);

// Color adjustments
Image.from(path)
    .brightness(1.3)          // 30% brighter
    .contrast(1.2)             // 20% more contrast
    .save(outputPath);

// Sepia and invert filters
Image.from(path).sepia().save(Path.of("sepia.jpg"));
Image.from(path).invert().save(Path.of("inverted.png"));

// Extract dominant colors
List<Color> palette = ColorExtractor.dominantColors(image, 5);
Color primary = ColorExtractor.dominantColor(image);

// Tiled watermark (diagonal text across entire image)
TextWatermark wm = TextWatermark.builder()
    .text("CONFIDENTIAL")
    .opacity(0.15f)
    .build();
Image.from(path).tiledWatermark(wm, 80, 60).save(outputPath);

// Add padding and border
Image.from(path)
    .pad(20, Color.WHITE)        // 20px white padding all around
    .border(2, Color.BLACK)      // 2px black border
    .save(outputPath);

// Overlay another image with 50% opacity at position (10, 10)
Image.from(basePath)
    .overlay(logoImage, 10, 10, 0.5f)
    .save(outputPath);

// Stitch images
BufferedImage strip = ImageStitch.horizontal(img1, img2, img3);
BufferedImage column = ImageStitch.vertical(10, Color.WHITE, img1, img2);
BufferedImage grid = ImageStitch.grid(List.of(img1, img2, img3, img4), 2);

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
