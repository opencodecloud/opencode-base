# OpenCode Base Image

面向 JDK 25+ 的图片处理工具库。提供缩放、裁剪、旋转、水印、压缩、格式转换、色彩调整、边缘检测、图片拼接等操作，支持流式链式 API。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-image</artifactId>
    <version>1.0.3</version>
</dependency>
```

## 功能特性

- 流式链式 API，构建图片处理管道
- 缩放：精确尺寸、保持宽高比、百分比缩放、**渐进式高质量缩小**
- 裁剪操作：区域裁剪、居中裁剪、正方形裁剪、**宽高比裁剪**
- 旋转：任意角度、90/180/270 度、水平/垂直翻转
- 文字水印、图片水印、**平铺水印**，支持位置控制和透明度
- **亮度、对比度、棕褐色和反色**色彩滤镜
- **主色调提取**（K-Means 聚类算法）
- **图片拼接**：水平、垂直和网格布局
- **内边距和边框**添加，支持自定义颜色
- **图片叠加**合成，支持透明度控制
- 基于质量的压缩
- 格式转换：JPEG、PNG、GIF、BMP、WebP（可选插件）
- 高级滤波：高斯模糊、中值模糊、方框模糊、双边滤波、锐化
- 边缘检测：Sobel、Canny、Laplacian
- 直方图：均衡化和 CLAHE
- 二值化：固定阈值、Otsu、自适应
- 图片比较：像素差分、SSIM、MSE、PSNR
- 感知哈希：aHash、dHash、pHash
- EXIF：自动旋转和标签清除
- 形态学操作：腐蚀、膨胀、开运算、闭运算、顶帽、黑帽
- 特征检测：Harris 角点、Shi-Tomasi 角点
- 图像分析：连通域标记、轮廓查找、模板匹配
- 色彩空间转换：RGB ↔ HSV、RGB ↔ Lab
- 几何变换：仿射变换和四点透视变换
- 绘图操作：形状、线条、文字
- 响应式图片集生成（Web 场景）
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
| `Image` | 支持链式调用的图片处理包装类，支持所有操作 |
| `ImageStitch` | 将多张图片水平、垂直或按网格拼接 |
| `ImageFormat` | 支持的图片格式枚举（JPEG、PNG、GIF、BMP、WEBP），含扩展名和 MIME 类型 |
| `ImageInfo` | 包含图片元数据的不可变记录：宽度、高度、格式、文件大小、宽高比 |
| `Position` | 定义水印和叠加层位置的枚举（如 TOP_LEFT、CENTER、BOTTOM_RIGHT） |

### 内部操作

| 类 | 说明 |
|---|------|
| `CompressOp` | 基于质量的 JPEG 压缩 |
| `ConvertOp` | 格式转换和灰度操作 |
| `CropOp` | 裁剪：区域、居中、正方形、宽高比 |
| `ResizeOp` | 缩放：精确、适配、比例、渐进式多步骤 |
| `RotateOp` | 旋转和翻转操作 |
| `WatermarkOp` | 文字/图片水印应用及平铺水印 |
| `PaddingOp` | 内边距和边框添加，支持自定义颜色 |
| `OverlayOp` | 支持透明度控制的图片叠加合成 |

### 色彩操作

| 类 | 说明 |
|---|------|
| `BrightnessContrastOp` | 亮度（乘法因子）和对比度（以 128 为中心）调整 |
| `ColorFilterOp` | 棕褐色滤镜（暖色矩阵变换）和反色滤镜（255-RGB） |
| `ColorExtractor` | 通过 K-Means 聚类从降采样图像提取主色调 |
| `ColorSpaceOp` | 色彩空间转换：RGB ↔ HSV、RGB ↔ Lab |
| `GammaOp` | 伽马校正 |
| `SaturationOp` | 饱和度调整 |
| `WhiteBalanceOp` | 灰色世界白平衡校正 |

### 滤波器

| 类 | 说明 |
|---|------|
| `GaussianBlurOp` | 高斯模糊，支持可配置 sigma |
| `MedianBlurOp` | 中值模糊（基于直方图），用于去除椒盐噪声 |
| `BoxBlurOp` | 方框模糊（均值滤波） |
| `BilateralFilterOp` | 保边双边滤波 |
| `SharpenOp` | 非锐化掩模锐化 |

### 边缘检测

| 类 | 说明 |
|---|------|
| `SobelOp` | Sobel 梯度边缘检测 |
| `CannyOp` | Canny 边缘检测（含非极大值抑制和滞后阈值） |
| `LaplacianOp` | Laplacian 二阶导数边缘检测 |

### 直方图

| 类 | 说明 |
|---|------|
| `HistogramEqualizationOp` | 全局直方图均衡化 |
| `ClaheOp` | 对比度受限自适应直方图均衡化 |
| `HistogramOp` | 直方图计算与统计 |

### 二值化

| 类 | 说明 |
|---|------|
| `ThresholdOp` | 固定阈值二值化 |
| `OtsuOp` | Otsu 自动阈值选择 |
| `AdaptiveThresholdOp` | 基于块的自适应阈值（均值方法） |

### 图片比较

| 类 | 说明 |
|---|------|
| `ImageCompare` | 像素差分、MSE、PSNR、SSIM 及相似度指标 |
| `PerceptualHash` | 感知哈希：aHash、dHash、pHash，支持汉明距离 |

### 分析

| 类 | 说明 |
|---|------|
| `ConnectedComponentsOp` | 连通域标记与统计 |
| `ContourFinderOp` | 轮廓检测与边界追踪 |
| `TemplateMatchOp` | 归一化互相关模板匹配 |

### 特征检测

| 类 | 说明 |
|---|------|
| `HarrisCornerOp` | Harris 角点检测 |
| `ShiTomasiOp` | Shi-Tomasi（Good Features to Track）角点检测 |

### 形态学

| 类 | 说明 |
|---|------|
| `MorphologyOp` | 腐蚀、膨胀、开运算、闭运算、顶帽、黑帽操作 |
| `StructuringElement` | 结构元素定义（方形、十字形等） |

### 变换

| 类 | 说明 |
|---|------|
| `AffineTransformOp` | 仿射变换（缩放、旋转、剪切、平移） |
| `PerspectiveTransformOp` | 四点透视变换 |

### 绘图

| 类 | 说明 |
|---|------|
| `DrawOp` | 在图片上绘制形状、线条和文字 |

### EXIF

| 类 | 说明 |
|---|------|
| `ExifOp` | 纯 Java EXIF 解析器：读取、清除标签、自动旋转 |
| `ExifInfo` | EXIF 元数据的不可变记录 |
| `ExifTag` | 支持的 EXIF 标签枚举 |

### 响应式

| 类 | 说明 |
|---|------|
| `ResponsiveBuilder` | 生成响应式图片集（多尺寸），用于 Web 场景 |

### 内核基础设施

| 类 | 说明 |
|---|------|
| `PixelOp` | 零拷贝像素数组访问与 ARGB 通道提取 |
| `KernelOp` | 二维卷积引擎 |
| `SeparableKernelOp` | 优化的可分离（1D+1D）卷积 |
| `LookupTableOp` | 256 项查找表，O(1) 逐像素变换 |
| `IntegralImage` | 积分图（面积表），O(1) 区域查询 |
| `ChannelOp` | 通道分离/合并，用于逐通道处理 |

### 水印

| 类 | 说明 |
|---|------|
| `Watermark` | 水印定义的密封基础接口 |
| `TextWatermark` | 文字水印：字体、颜色、透明度和位置 |
| `ImageWatermark` | 图片水印：叠加图片、透明度和位置 |

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
| `ImageOperation` | 图片处理操作函数式接口（配合 `SafeImageService.process` 使用） |

### 异常

| 类 | 说明 |
|---|------|
| `ImageException` | 图片处理错误的基础异常（继承自 `OpenException`，组件="Image"） |
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

// 渐进式高质量缩小（大幅缩小时效果优于单步缩放）
Image.from(path).resizeProgressive(100, 100).save(outputPath);

// 从中心裁剪为 16:9 宽高比
Image.from(path).cropToAspectRatio(16, 9).save(outputPath);

// 色彩调整
Image.from(path)
    .brightness(1.3)    // 亮度增加 30%
    .contrast(1.2)      // 对比度增加 20%
    .save(outputPath);

// 棕褐色和反色滤镜
Image.from(path).sepia().save(Path.of("sepia.jpg"));
Image.from(path).invert().save(Path.of("inverted.png"));

// 提取主色调
List<Color> palette = ColorExtractor.dominantColors(image, 5);
Color primary = ColorExtractor.dominantColor(image);

// 平铺水印
TextWatermark wm = TextWatermark.builder()
    .text("机密")
    .opacity(0.15f)
    .build();
Image.from(path).tiledWatermark(wm, 80, 60).save(outputPath);

// 添加内边距和边框
Image.from(path)
    .pad(20, Color.WHITE)    // 四周 20px 白色内边距
    .border(2, Color.BLACK)  // 2px 黑色边框
    .save(outputPath);

// 叠加图片（50% 透明度，位置 10,10）
Image.from(basePath)
    .overlay(logoImage, 10, 10, 0.5f)
    .save(outputPath);

// 图片拼接
BufferedImage strip = ImageStitch.horizontal(img1, img2, img3);
BufferedImage column = ImageStitch.vertical(10, Color.WHITE, img1, img2);
BufferedImage grid = ImageStitch.grid(List.of(img1, img2, img3, img4), 2);

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
