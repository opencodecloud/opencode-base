# OpenCode Base Image

面向 JDK 25+ 的图片处理工具库。提供缩放、裁剪、旋转、水印、压缩和格式转换等操作，支持流式链式 API。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-image</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 功能特性

- 流式链式 API，构建图片处理管道
- 缩放：精确尺寸、保持宽高比、百分比缩放
- 裁剪操作：区域裁剪、居中裁剪、正方形裁剪
- 旋转：任意角度、90/180/270 度、水平/垂直翻转
- 文字和图片水印，支持位置控制和透明度
- 基于质量的压缩
- 格式转换：JPEG、PNG、GIF、BMP、WebP（可选插件）
- 加载前的图片校验和尺寸检查（防止 OOM）
- 支持 Path、InputStream、字节数组和 Base64 的读写
- 可配置选项的缩略图构建器
- 安全工具：安全路径校验和操作白名单
- 无需完整加载即可获取图片元数据

## 类参考

### 核心类

| 类 | 说明 |
|---|------|
| `OpenImage` | 图片操作静态工具门面：读取、写入、缩放、裁剪、旋转、转换、压缩、缩略图 |
| `Image` | 支持链式调用的图片处理包装类，支持缩放、裁剪、旋转、水印、压缩、转换和输出 |
| `ImageFormat` | 支持的图片格式枚举（JPEG、PNG、GIF、BMP、WEBP），含扩展名和 MIME 类型 |
| `ImageInfo` | 包含图片元数据的不可变记录：宽度、高度、格式、文件大小、宽高比 |
| `Position` | 定义水印和叠加层位置的枚举（如 TOP_LEFT、CENTER、BOTTOM_RIGHT） |

### 内部操作

| 类 | 说明 |
|---|------|
| `CompressOp` | 内部压缩操作实现 |
| `ConvertOp` | 内部格式转换和灰度操作 |
| `CropOp` | 内部裁剪操作：区域、居中、正方形 |
| `ResizeOp` | 内部缩放操作：精确、适配、比例、宽度、高度 |
| `RotateOp` | 内部旋转和翻转操作 |
| `WatermarkOp` | 内部文字和图片水印应用 |

### 水印

| 类 | 说明 |
|---|------|
| `Watermark` | 水印定义的基础接口 |
| `TextWatermark` | 文字水印配置：字体、颜色、透明度和位置 |
| `ImageWatermark` | 图片水印配置：叠加图片、透明度和位置 |

### 缩略图

| 类 | 说明 |
|---|------|
| `ThumbnailBuilder` | 缩略图构建器，支持可配置的尺寸、格式和质量 |

### 校验

| 类 | 说明 |
|---|------|
| `ImageValidator` | 校验图片文件的格式、尺寸和内容完整性 |

### 安全

| 类 | 说明 |
|---|------|
| `SafeImageService` | 安全的图片处理服务，支持操作白名单 |
| `SafePathUtil` | 路径校验工具，防止目录遍历攻击 |
| `ImageOperation` | 安全白名单中允许的图片操作枚举 |

### 异常

| 类 | 说明 |
|---|------|
| `ImageException` | 图片处理错误的基础异常 |
| `ImageIOException` | 通用图片 I/O 错误时抛出 |
| `ImageReadException` | 图片读取失败时抛出 |
| `ImageWriteException` | 图片写入失败时抛出 |
| `ImageFormatException` | 不支持或无效的图片格式时抛出 |
| `ImageOperationException` | 图片操作失败时抛出 |
| `ImageResourceException` | 图片资源访问错误时抛出 |
| `ImageTimeoutException` | 图片操作超时时抛出 |
| `ImageTooLargeException` | 图片超过大小限制时抛出 |
| `ImageValidationException` | 图片校验失败时抛出 |
| `ImageErrorCode` | 分类图片错误处理的错误码枚举 |

## 快速开始

```java
// 读取并链式操作
OpenImage.read(Path.of("photo.jpg"))
    .resize(800, 600)
    .rotate(90)
    .watermark("Copyright 2026", Position.BOTTOM_RIGHT)
    .compress(0.8f)
    .save(Path.of("output.jpg"));

// 快速缩放
OpenImage.resize(inputPath, outputPath, 800, 600);

// 快速格式转换
OpenImage.convert(inputPath, outputPath, ImageFormat.PNG);

// 创建缩略图
OpenImage.thumbnail(inputPath, outputPath, 200);

// 缩略图构建器
OpenImage.thumbnail()
    .source(Path.of("photo.jpg"))
    .size(200, 200)
    .format(ImageFormat.PNG)
    .output(Path.of("thumb.png"))
    .create();

// 无需加载完整图片获取信息
ImageInfo info = OpenImage.getInfo(Path.of("photo.jpg"));
System.out.println(info.width() + "x" + info.height());
System.out.println("格式: " + info.format());
System.out.println("大小: " + info.fileSizeFormatted());

// 从字节数组 / Base64 读取
Image img = OpenImage.read(bytes);
Image img2 = OpenImage.fromBase64(base64String);

// 转换为字节数组 / Base64
byte[] bytes = img.toBytes(ImageFormat.PNG);
String base64 = img.toBase64();

// 灰度、翻转
OpenImage.grayscale(inputPath, outputPath);
OpenImage.flipHorizontal(inputPath, outputPath);

// 校验图片
boolean valid = OpenImage.isValidImage(Path.of("file.jpg"));
```

## 环境要求

- Java 25+
- 可选：`com.twelvemonkeys.imageio:imageio-webp:3.12.0` 用于 WebP 支持

## 开源协议

Apache License 2.0
