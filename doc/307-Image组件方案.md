# Image 组件方案

## 1. 组件概述

`opencode-base-image` 模块提供图片处理能力，包括图片读写、缩放、裁剪、旋转、翻转、水印（文字/图片）、格式转换、压缩、缩略图生成、图片信息读取、安全验证等功能。基于 JDK 25 AWT/ImageIO，核心零第三方依赖，支持链式操作 API。

## 2. 包结构

```
cloud.opencode.base.image
├── OpenImage.java                         # 门面入口类
├── Image.java                             # 图片包装类（链式操作）
├── ImageInfo.java                         # 图片信息 Record
├── ImageFormat.java                       # 图片格式枚举
├── Position.java                          # 位置枚举（9 宫格）
│
├── watermark/                             # 水印
│   ├── Watermark.java                     # 水印 sealed 接口
│   ├── TextWatermark.java                 # 文字水印 Record
│   └── ImageWatermark.java                # 图片水印 Record
│
├── thumbnail/                             # 缩略图
│   └── ThumbnailBuilder.java              # 缩略图构建器
│
├── validation/                            # 验证
│   └── ImageValidator.java                # 图片验证器
│
├── security/                              # 安全
│   ├── SafeImageService.java              # 安全处理服务（并发/超时控制）
│   ├── SafePathUtil.java                  # 安全路径工具
│   └── ImageOperation.java                # 操作函数接口
│
└── exception/                             # 异常
    ├── ImageException.java                # 异常基类
    ├── ImageErrorCode.java                # 错误码枚举
    ├── ImageIOException.java              # IO 异常
    ├── ImageReadException.java            # 读取异常
    ├── ImageWriteException.java           # 写入异常
    ├── ImageFormatException.java          # 格式异常
    ├── ImageOperationException.java       # 操作异常
    ├── ImageValidationException.java      # 验证异常
    ├── ImageTooLargeException.java        # 图片过大异常
    ├── ImageResourceException.java        # 资源异常
    └── ImageTimeoutException.java         # 超时异常
```

## 3. 核心 API

### 3.1 OpenImage

> 图片门面入口类，提供图片读写、缩放、裁剪、旋转、翻转、水印、压缩、格式转换、信息获取等静态便捷方法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static Image read(Path path)` | 从文件读取图片 |
| `static Image read(InputStream in)` | 从输入流读取图片 |
| `static Image read(byte[] bytes)` | 从字节数组读取图片 |
| `static Image fromBase64(String base64)` | 从 Base64 字符串读取图片 |
| `static BufferedImage readBufferedImage(Path path)` | 读取为 BufferedImage |
| `static void write(BufferedImage image, Path path)` | 写入图片文件 |
| `static void write(BufferedImage image, Path path, ImageFormat format)` | 写入指定格式图片 |
| `static void write(BufferedImage image, OutputStream out, ImageFormat format)` | 写入输出流 |
| `static ImageInfo getInfo(Path path)` | 获取图片信息 |
| `static int[] getDimensions(Path path)` | 获取图片尺寸 [width, height] |
| `static void resize(Path input, Path output, int width, int height)` | 缩放图片文件 |
| `static void crop(Path input, Path output, int x, int y, int width, int height)` | 裁剪图片文件 |
| `static void rotate(Path input, Path output, double degrees)` | 旋转图片文件 |
| `static void convert(Path input, Path output, ImageFormat format)` | 转换图片格式 |
| `static void compress(Path input, Path output, float quality)` | 压缩图片质量 |
| `static void thumbnail(Path input, Path output, int size)` | 生成缩略图 |
| `static ThumbnailBuilder thumbnail()` | 创建缩略图构建器 |
| `static void flipHorizontal(Path input, Path output)` | 水平翻转 |
| `static void flipVertical(Path input, Path output)` | 垂直翻转 |
| `static void grayscale(Path input, Path output)` | 灰度化 |
| `static byte[] toBytes(BufferedImage image, ImageFormat format)` | 图片转字节数组 |
| `static byte[] toBytes(Path path)` | 文件转字节数组 |
| `static ImageFormat detectFormat(Path path)` | 检测图片格式 |
| `static boolean isValidImage(Path path)` | 验证是否有效图片 |
| `static boolean isSupported(String extension)` | 判断格式是否支持 |
| `static Image createBlank(int width, int height)` | 创建空白图片 |
| `static Image createBlank(int width, int height, int color)` | 创建指定颜色空白图片 |

**示例:**

```java
// 读取并处理
Image result = OpenImage.read(Path.of("photo.jpg"))
    .resize(800, 600)
    .watermark(TextWatermark.of("Copyright"), Position.BOTTOM_RIGHT)
    .compress(0.8f);
result.save(Path.of("output.jpg"));

// 快速缩放
OpenImage.resize(inputPath, outputPath, 800, 600);

// 格式转换
OpenImage.convert(inputPath, outputPath, ImageFormat.PNG);

// 获取信息
ImageInfo info = OpenImage.getInfo(Path.of("photo.jpg"));
```

### 3.2 Image

> 图片包装类，持有 BufferedImage，支持链式操作。每个操作返回新的 Image 实例。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `Image(BufferedImage image)` | 构造（默认格式） |
| `Image(BufferedImage image, ImageFormat format)` | 构造（指定格式） |
| `static Image from(Path path)` | 从文件创建 |
| `static Image from(byte[] bytes)` | 从字节数组创建 |
| `static Image from(BufferedImage image)` | 从 BufferedImage 创建 |
| `BufferedImage getBufferedImage()` | 获取内部 BufferedImage（直接引用） |
| `BufferedImage copyBufferedImage()` | 获取 BufferedImage 的防御性拷贝 |
| `ImageFormat getFormat()` | 获取图片格式 |
| `Image format(ImageFormat format)` | 设置格式 |
| `int getWidth()` | 获取宽度 |
| `int getHeight()` | 获取高度 |
| `ImageInfo getInfo()` | 获取图片信息 |
| `Image resize(int width, int height)` | 缩放到指定尺寸 |
| `Image resizeToFit(int maxWidth, int maxHeight)` | 等比缩放适配 |
| `Image scale(double scale)` | 按比例缩放 |
| `Image scaleToWidth(int width)` | 按宽度等比缩放 |
| `Image scaleToHeight(int height)` | 按高度等比缩放 |
| `Image crop(int x, int y, int width, int height)` | 裁剪 |
| `Image cropCenter(int width, int height)` | 中心裁剪 |
| `Image cropSquare()` | 裁剪为正方形 |
| `Image rotate(double degrees)` | 旋转任意角度 |
| `Image rotate90()` | 顺时针旋转 90 度 |
| `Image rotate180()` | 旋转 180 度 |
| `Image rotate270()` | 逆时针旋转 90 度 |
| `Image flipHorizontal()` | 水平翻转 |
| `Image flipVertical()` | 垂直翻转 |
| `Image watermark(TextWatermark watermark)` | 添加文字水印 |
| `Image watermark(String text, Position position)` | 添加简单文字水印 |
| `Image watermark(ImageWatermark watermark)` | 添加图片水印 |
| `Image watermark(BufferedImage watermarkImage, Position position)` | 添加简单图片水印 |
| `Image compress(float quality)` | 压缩质量 (0.0-1.0) |
| `Image convert(ImageFormat targetFormat)` | 转换格式 |
| `Image grayscale()` | 灰度化 |
| `void save(Path path)` | 保存到文件（自动检测格式） |
| `void save(Path path, ImageFormat format)` | 保存到指定格式文件 |
| `void writeTo(OutputStream out)` | 写入输出流 |
| `void writeTo(OutputStream out, ImageFormat format)` | 写入指定格式输出流 |
| `byte[] toBytes()` | 转为字节数组 |
| `byte[] toBytes(ImageFormat format)` | 转为指定格式字节数组 |
| `String toBase64()` | 转为 Base64 字符串 |
| `String toBase64(ImageFormat format)` | 转为指定格式 Base64 |
| `Image copy()` | 深拷贝 |

**示例:**

```java
Image result = Image.from(Path.of("photo.jpg"))
    .resize(800, 600)
    .rotate90()
    .watermark("Copyright 2025", Position.BOTTOM_RIGHT)
    .compress(0.8f);
result.save(Path.of("output.jpg"));

// Base64 输出
String base64 = result.toBase64(ImageFormat.PNG);
```

### 3.3 ImageInfo

> 图片信息 Record，包含宽度、高度、格式、文件大小等信息。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `ImageInfo(int width, int height)` | 仅尺寸构造 |
| `ImageInfo(int width, int height, ImageFormat format)` | 尺寸+格式构造 |
| `ImageInfo(int width, int height, ImageFormat format, long fileSize)` | 完整构造 |
| `int width()` | 宽度 |
| `int height()` | 高度 |
| `ImageFormat format()` | 格式 |
| `long fileSize()` | 文件大小（字节） |
| `double aspectRatio()` | 宽高比 |
| `long pixels()` | 总像素数 |
| `long estimatedMemorySize()` | 估算内存占用 |
| `boolean isLandscape()` | 是否横向 |
| `boolean isPortrait()` | 是否纵向 |
| `boolean isSquare()` | 是否正方形 |
| `String fileSizeFormatted()` | 格式化文件大小 |

**示例:**

```java
ImageInfo info = OpenImage.getInfo(Path.of("photo.jpg"));
System.out.println(info.width() + "x" + info.height());
System.out.println("Format: " + info.format());
System.out.println("Size: " + info.fileSizeFormatted());
```

### 3.4 ImageFormat

> 图片格式枚举，定义支持的图片格式及其属性。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `String getExtension()` | 获取文件扩展名 |
| `String getMimeType()` | 获取 MIME 类型 |
| `boolean supportsTransparency()` | 是否支持透明度 |
| `static ImageFormat fromExtension(String extension)` | 从扩展名获取格式 |
| `static ImageFormat fromMimeType(String mimeType)` | 从 MIME 类型获取格式 |
| `static boolean isSupported(String extension)` | 判断扩展名是否支持 |
| `static boolean isWebPAvailable()` | WebP 是否可用 |
| `boolean isAvailable()` | 当前格式是否可用 |

枚举值: `JPEG`, `PNG`, `GIF`, `BMP`, `WEBP`, `TIFF`

### 3.5 Position

> 位置枚举，定义水印等覆盖物的位置（9 宫格）。

枚举值:

| 枚举值 | 描述 |
|--------|------|
| `TOP_LEFT` | 左上 |
| `TOP_CENTER` | 上中 |
| `TOP_RIGHT` | 右上 |
| `CENTER_LEFT` | 左中 |
| `CENTER` | 居中 |
| `CENTER_RIGHT` | 右中 |
| `BOTTOM_LEFT` | 左下 |
| `BOTTOM_CENTER` | 下中 |
| `BOTTOM_RIGHT` | 右下 |

**主要方法:**

| 方法 | 描述 |
|------|------|
| `boolean isLeft()` | 是否在左侧 |
| `boolean isRight()` | 是否在右侧 |
| `boolean isTop()` | 是否在顶部 |
| `boolean isBottom()` | 是否在底部 |
| `boolean isCenterHorizontal()` | 是否水平居中 |
| `boolean isCenterVertical()` | 是否垂直居中 |

### 3.6 TextWatermark

> 文字水印 Record，定义文字水印的文本、字体、颜色、透明度、边距等属性。实现 Watermark sealed 接口。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static TextWatermark of(String text)` | 创建默认文字水印 |
| `static TextWatermark of(String text, Position position)` | 创建指定位置水印 |
| `static TextWatermark of(String text, Position position, Font font)` | 创建自定义字体水印 |
| `static Builder builder()` | 创建构建器 |
| `static Builder builder(TextWatermark watermark)` | 从已有水印创建构建器 |
| `TextWatermark withText(String newText)` | 替换文本 |
| `TextWatermark withPosition(Position newPosition)` | 替换位置 |
| `TextWatermark withOpacity(float newOpacity)` | 替换透明度 |

Builder 方法: `text()`, `position()`, `font()`, `font(String, int)`, `font(String, int, int)`, `color()`, `color(int, int, int)`, `color(int, int, int, int)`, `opacity()`, `margin()`, `build()`

**示例:**

```java
TextWatermark watermark = TextWatermark.builder()
    .text("Copyright 2025")
    .position(Position.BOTTOM_RIGHT)
    .font("Arial", Font.BOLD, 24)
    .color(255, 255, 255, 180)
    .opacity(0.8f)
    .margin(20)
    .build();
```

### 3.7 ImageWatermark

> 图片水印 Record，定义图片水印的水印图像、位置、透明度、边距等属性。实现 Watermark sealed 接口。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static ImageWatermark of(BufferedImage image)` | 创建默认图片水印 |
| `static ImageWatermark of(BufferedImage image, Position position)` | 创建指定位置水印 |
| `static ImageWatermark of(BufferedImage image, Position position, float opacity)` | 创建自定义透明度水印 |
| `int getWidth()` | 获取水印宽度 |
| `int getHeight()` | 获取水印高度 |
| `ImageWatermark withPosition(Position newPosition)` | 替换位置 |
| `ImageWatermark withOpacity(float newOpacity)` | 替换透明度 |
| `ImageWatermark withMargin(int newMargin)` | 替换边距 |
| `ImageWatermark withImage(BufferedImage newImage)` | 替换水印图像 |
| `static Builder builder()` | 创建构建器 |

Builder 方法: `image()`, `position()`, `opacity()`, `margin()`, `build()`

**示例:**

```java
ImageWatermark watermark = ImageWatermark.builder()
    .image(logoImage)
    .position(Position.BOTTOM_RIGHT)
    .opacity(0.5f)
    .margin(20)
    .build();
```

### 3.8 ThumbnailBuilder

> 缩略图构建器，支持链式配置源图、目标尺寸、裁剪模式、质量、格式等，生成缩略图。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static ThumbnailBuilder of(Path path)` | 从文件创建 |
| `static ThumbnailBuilder of(Image image)` | 从 Image 创建 |
| `static ThumbnailBuilder of(BufferedImage image)` | 从 BufferedImage 创建 |
| `static ThumbnailBuilder of(byte[] bytes)` | 从字节数组创建 |
| `ThumbnailBuilder size(int width, int height)` | 设置尺寸 |
| `ThumbnailBuilder width(int width)` | 设置宽度（等比） |
| `ThumbnailBuilder height(int height)` | 设置高度（等比） |
| `ThumbnailBuilder crop(boolean crop)` | 是否裁剪适配 |
| `ThumbnailBuilder quality(float quality)` | 设置质量 |
| `ThumbnailBuilder format(ImageFormat format)` | 设置输出格式 |
| `ThumbnailBuilder source(Path path)` | 设置源文件 |
| `ThumbnailBuilder source(Image image)` | 设置源 Image |
| `ThumbnailBuilder output(Path path)` | 设置输出路径 |
| `void create()` | 创建并保存缩略图 |
| `BufferedImage build()` | 构建为 BufferedImage |
| `Image toImage()` | 构建为 Image |
| `void save(Path path)` | 保存到文件 |
| `byte[] toBytes()` | 转为字节数组 |

**示例:**

```java
ThumbnailBuilder.of(Path.of("photo.jpg"))
    .size(200, 200)
    .crop(true)
    .quality(0.9f)
    .format(ImageFormat.PNG)
    .save(Path.of("thumb.png"));
```

### 3.9 ImageValidator

> 图片验证器，验证图片文件的大小、尺寸、格式、魔数等。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static void validate(Path path)` | 验证图片文件（默认限制） |
| `static void validate(Path path, long maxFileSize, int maxWidth, int maxHeight)` | 验证（自定义限制） |
| `static void validate(byte[] bytes)` | 验证字节数组图片 |
| `static void validate(byte[] bytes, long maxFileSize, int maxWidth, int maxHeight)` | 验证字节数组（自定义限制） |
| `static boolean isValidImage(Path path)` | 判断是否有效图片 |
| `static boolean isValidImage(byte[] bytes)` | 判断字节数组是否有效图片 |
| `static boolean checkMagicNumber(byte[] bytes)` | 检查魔数 |
| `static ImageFormat detectFormat(byte[] bytes)` | 检测图片格式 |
| `static Optional<ImageFormat> detectFormatOptional(byte[] bytes)` | 检测格式（Optional） |
| `static boolean validateExtensionMatchesContent(Path path)` | 验证扩展名与内容是否匹配 |

默认限制: 最大文件 10MB, 最大尺寸 8000x8000

### 3.10 SafeImageService

> 安全图片处理服务，提供并发控制（Semaphore）和超时控制，支持自定义最大文件大小/尺寸。实现 AutoCloseable。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static Builder builder()` | 创建构建器 |
| `static SafeImageService createDefault()` | 创建默认服务 |
| `Image read(Path path)` | 安全读取图片（带验证） |
| `Image read(byte[] bytes)` | 安全读取字节数组 |
| `Image process(Image image, ImageOperation operation)` | 安全处理（带超时） |
| `void save(Image image, Path path, Path baseDir)` | 安全保存（路径验证） |
| `ImageInfo getInfo(Path path)` | 获取图片信息 |
| `int getActiveCount()` | 当前活跃数 |
| `int getAvailablePermits()` | 可用许可数 |
| `void close()` | 关闭服务释放资源 |

Builder 方法: `maxFileSize()`, `maxWidth()`, `maxHeight()`, `maxDimensions()`, `timeout()`, `maxConcurrent()`, `build()`

**示例:**

```java
SafeImageService service = SafeImageService.builder()
    .maxFileSize(5 * 1024 * 1024)
    .maxDimensions(4000, 4000)
    .timeout(Duration.ofSeconds(10))
    .maxConcurrent(4)
    .build();

Image image = service.read(path);
Image result = service.process(image, img -> img.resize(800, 600));
```

### 3.11 SafePathUtil

> 安全路径工具类，防止路径遍历攻击，验证路径安全性。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static void validatePath(Path path)` | 验证路径安全性 |
| `static void validatePath(Path path, Path baseDir)` | 验证路径在基础目录内 |
| `static boolean isSafePath(Path path, Path baseDir)` | 判断路径是否安全 |
| `static String sanitizeFilename(String filename)` | 清理文件名 |
| `static String getExtension(String filename)` | 获取扩展名 |
| `static boolean isAllowedExtension(String extension)` | 判断扩展名是否允许 |
| `static ImageFormat getFormat(Path path)` | 从路径获取格式 |
| `static Path generateOutputPath(Path inputPath, String suffix)` | 生成输出路径 |
| `static Path generateOutputPath(Path inputPath, ImageFormat format)` | 生成指定格式输出路径 |
| `static boolean ensureParentExists(Path path)` | 确保父目录存在 |

### 3.12 ImageOperation

> 图片操作函数接口，用于 SafeImageService 的操作参数。支持 `andThen` 链式组合。

```java
ImageOperation op = image -> image.resize(800, 600);
ImageOperation chain = op1.andThen(op2).andThen(op3);
BufferedImage result = chain.apply(sourceImage);
```

### 3.13 异常类

> 图片操作异常体系，所有异常继承自 ImageException。

| 异常类 | 描述 |
|--------|------|
| `ImageException` | 图片异常基类，包含 ImageErrorCode |
| `ImageErrorCode` | 错误码枚举（IO/格式/操作/验证/资源错误） |
| `ImageIOException` | IO 异常基类 |
| `ImageReadException` | 读取失败，可通过 `getPath()` 获取路径 |
| `ImageWriteException` | 写入失败，可通过 `getPath()` 获取路径 |
| `ImageFormatException` | 格式异常，可通过 `getFormat()` 获取格式 |
| `ImageOperationException` | 操作异常，可通过 `getOperation()` 获取操作名 |
| `ImageValidationException` | 验证异常 |
| `ImageTooLargeException` | 图片过大（尺寸或文件大小） |
| `ImageResourceException` | 资源异常（请求过多/内存不足） |
| `ImageTimeoutException` | 处理超时，可通过 `getTimeout()` 获取超时时长 |
