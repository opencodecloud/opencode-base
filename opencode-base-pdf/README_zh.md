# OpenCode Base PDF

**零依赖的 Java 25+ PDF 处理库**

`opencode-base-pdf` 是一个功能全面的 PDF 库，提供文档创建、合并、拆分、表单填充、数字签名和内容提取功能 -- 无需任何外部依赖。

## 功能特性

### 核心功能
- **文档创建**：流式 API 构建 PDF 文档，支持文本、图片、表格和图形
- **文档读取**：从文件路径、输入流或字节数组打开和解析已有 PDF 文件
- **合并与拆分**：将多个 PDF 合并为一个，或按页面范围拆分 PDF
- **表单填充**：填充交互式表单字段并扁平化表单
- **数字签名**：使用 PKCS#12 密钥库签名 PDF 并验证已有签名
- **内容提取**：从 PDF 页面提取文本和图片

### 内容元素
- **文本与段落**：支持字体、大小、颜色、旋转、下划线、字符/词间距及自动换行的富文本渲染
- **表格与单元格**：带表头行、交替行色和单元格样式的结构化表格布局
- **图片**：嵌入 JPEG（DCTDecode）和 PNG（FlateDecode，支持 alpha/SMask）图像
- **图形**：线段、矩形（描边+填充）和椭圆（贝塞尔曲线逼近）

### V1.0.3 新特性
- **水印**：支持旋转角度、透明度、颜色、字体和字号的文字水印
- **页眉页脚**：支持 `{page}` / `{total}` 占位符的自动页码；可配置左/中/右三区域，可选分隔线
- **增强渲染**：椭圆、段落自动换行与对齐（左/居中/右/两端对齐）、文字旋转、下划线、字符/词间距
- **图片嵌入**：JPEG（DCTDecode 直通）和 PNG（FlateDecode，alpha 通道 SMask）
- **PDF 解析器**：读取 PDF 1.4-1.7 文件，支持 FlateDecode、ASCII85Decode、ASCIIHexDecode 及 ToUnicode CMap 文本提取
- **异常体系**：`OpenPdfException` 继承 `OpenException`，支持组件名/错误码

### 文档功能
- **元数据**：标题、作者、主题、关键词、创建者和生成者
- **页面大小**：A0-A6、B4、B5、Letter、Legal 和自定义尺寸
- **页面方向**：纵向和横向
- **加密**：密码保护（用户密码和所有者密码）
- **字体**：PDF 标准 14 种 Type1 字体和嵌入 TrueType 字体

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-pdf</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 创建 PDF
```java
import cloud.opencode.base.pdf.OpenPdf;
import java.nio.file.Path;

OpenPdf.create()
    .title("我的文档")
    .author("张三")
    .addPage()
        .text("你好，世界！", 100, 700)
    .endPage()
    .save(Path.of("hello.pdf"));
```

### 打开并提取文本
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.PdfDocument;
import cloud.opencode.base.pdf.document.Metadata;
import java.nio.file.Path;

// 打开已有 PDF
try (PdfDocument doc = OpenPdf.open(Path.of("document.pdf"))) {
    System.out.println("页数: " + doc.getPageCount());
    Metadata meta = doc.getMetadata();
    System.out.println("标题: " + meta.title());
}

// 提取所有页面文本
String text = OpenPdf.extractText(Path.of("document.pdf"));
```

### 添加水印
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.content.PdfWatermark;
import java.nio.file.Path;

OpenPdf.create()
    .watermark(PdfWatermark.text("机密文件")
        .rotation(-45)
        .opacity(0.15f)
        .fontSize(60))
    .addPage()
        .text("敏感内容", 100, 700)
    .endPage()
    .save(Path.of("watermarked.pdf"));
```

### 页眉页脚与自动页码
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.document.PdfHeader;
import cloud.opencode.base.pdf.document.PdfFooter;
import java.nio.file.Path;

OpenPdf.create()
    .header(PdfHeader.builder()
        .left("月度报告")
        .right("2026-04"))
    .footer(PdfFooter.builder()
        .center("第 {page} 页，共 {total} 页"))
    .addPage()
        .text("第1页内容", 100, 600)
    .nextPage()
        .text("第2页内容", 100, 600)
    .endPage()
    .save(Path.of("report.pdf"));
```

### 合并 PDF
```java
import cloud.opencode.base.pdf.OpenPdf;
import java.nio.file.Path;
import java.util.List;

OpenPdf.merge(
    List.of(Path.of("doc1.pdf"), Path.of("doc2.pdf")),
    Path.of("merged.pdf")
);
```

### 拆分 PDF
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.PdfDocument;
import java.nio.file.Path;
import java.util.List;

List<PdfDocument> parts = OpenPdf.split(Path.of("doc.pdf"), "1-3", "4-6");
```

### 数字签名
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.PdfDocument;
import cloud.opencode.base.pdf.signature.SignatureInfo;
import java.nio.file.Path;
import java.util.List;

// 签名
PdfDocument signed = OpenPdf.sign(
    Path.of("doc.pdf"),
    Path.of("keystore.p12"),
    "password".toCharArray(),
    "alias"
);

// 验证
List<SignatureInfo> sigs = OpenPdf.verifySignatures(Path.of("signed.pdf"));
```

### 填充表单
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.PdfDocument;
import java.nio.file.Path;
import java.util.Map;

PdfDocument filled = OpenPdf.fillForm(
    Path.of("form.pdf"),
    Map.of("name", "Alice", "email", "alice@example.com")
);
```

## API 参考 — `OpenPdf`

### 文档创建

| 方法 | 说明 |
|------|------|
| `create()` | 创建新的 PDF 文档构建器（默认 A4） |
| `create(PageSize)` | 创建指定页面大小的文档构建器 |

### 文档读取

| 方法 | 说明 |
|------|------|
| `open(Path)` | 从文件路径打开 PDF |
| `open(InputStream)` | 从输入流打开 PDF |
| `open(byte[])` | 从字节数组打开 PDF |
| `open(Path, String)` | 打开受密码保护的 PDF |

### 合并与拆分

| 方法 | 说明 |
|------|------|
| `merger()` | 创建 PDF 合并器 |
| `merge(List<Path>, Path)` | 将多个 PDF 文件合并为一个 |
| `merge(List<PdfDocument>)` | 将多个 PDF 文档合并为一个 |
| `splitter()` | 创建 PDF 拆分器 |
| `split(Path, String...)` | 按页面范围拆分（如 `"1-3"`, `"5"`） |
| `splitToPages(Path)` | 拆分为单页文档 |

### 表单操作

| 方法 | 说明 |
|------|------|
| `fillForm(Path, Map)` | 填充 PDF 表单字段 |
| `fillAndFlatten(Path, Map)` | 填充表单并扁平化（不可编辑） |
| `extractFormFields(Path)` | 提取表单字段名和当前值 |

### 数字签名

| 方法 | 说明 |
|------|------|
| `signer()` | 创建 PDF 签名器 |
| `sign(Path, Path, char[], String)` | 使用 PKCS#12 密钥库签名 |
| `verifySignatures(Path)` | 验证签名并返回结果 |

### 内容提取

| 方法 | 说明 |
|------|------|
| `extractor()` | 创建 PDF 提取器 |
| `extractText(Path)` | 提取 PDF 全部文本 |
| `extractText(Path, int...)` | 提取指定页面文本（页码从1开始） |
| `extractImages(Path)` | 提取图片为字节数组 |

### 实用方法

| 方法 | 说明 |
|------|------|
| `getPageCount(Path)` | 获取 PDF 页数 |
| `getMetadata(Path)` | 获取 PDF 元数据 |
| `isEncrypted(Path)` | 检查 PDF 是否加密 |
| `hasForm(Path)` | 检查 PDF 是否包含表单 |
| `isSigned(Path)` | 检查 PDF 是否已签名 |

## API 参考 — `DocumentBuilder`

| 方法 | 说明 |
|------|------|
| `title(String)` | 设置文档标题 |
| `author(String)` | 设置文档作者 |
| `subject(String)` | 设置文档主题 |
| `keywords(String...)` | 设置文档关键词 |
| `creator(String)` | 设置创建应用名称 |
| `pageSize(PageSize)` | 设置默认页面大小 |
| `orientation(Orientation)` | 设置默认页面方向 |
| `margins(float)` | 设置统一边距（点） |
| `margins(float, float, float, float)` | 设置各边距（上、右、下、左） |
| `defaultFont(PdfFont)` | 设置默认字体 |
| `embedFont(Path, String)` | 嵌入 TrueType 字体文件 |
| `watermark(PdfWatermark)` | 设置所有页面的文字水印 |
| `watermark(String)` | 设置文字水印（便捷方法） |
| `header(PdfHeader)` | 设置所有页面的页眉 |
| `header(String)` | 设置居中页眉（便捷方法） |
| `footer(PdfFooter)` | 设置所有页面的页脚 |
| `footer(String)` | 设置居中页脚（便捷方法） |
| `encrypt(String, String)` | 设置用户密码和所有者密码 |
| `permissions(boolean, boolean, boolean, boolean)` | 设置打印/复制/修改/注释权限 |
| `addPage()` | 添加新页面（返回 `PageBuilder`） |
| `addPage(PageSize)` | 添加指定大小的页面 |
| `addPage(PageSize, Orientation)` | 添加指定大小和方向的页面 |
| `save(Path)` | 构建并保存到文件 |
| `save(OutputStream)` | 构建并写入流 |
| `toBytes()` | 构建并返回字节数组 |

## 类参考

### 根包 (`cloud.opencode.base.pdf`)
| 类 | 说明 |
|----|------|
| `OpenPdf` | 主门面类，提供所有 PDF 操作的工厂方法 |
| `PdfDocument` | 表示 PDF 文档的接口，包含页面、元数据和表单 |
| `PdfPage` | 表示 PDF 文档中的单个页面 |

### 内容 (`cloud.opencode.base.pdf.content`)
| 类 | 说明 |
|----|------|
| `PdfElement` | 所有 PDF 内容元素的密封基础接口 |
| `PdfText` | 文本元素：字体、大小、颜色、旋转、下划线、间距 |
| `PdfParagraph` | 段落元素：自动换行、对齐、行距、首行缩进 |
| `PdfImage` | 嵌入式图片元素（支持文件/字节/流的 JPEG/PNG） |
| `PdfTable` | 表格元素：表头行、数据行、交替行色、列宽 |
| `PdfCell` | 单元格：对齐、跨列、跨行、字体、颜色 |
| `PdfLine` | 线段：宽度、颜色、样式（实线/虚线/点线） |
| `PdfRectangle` | 矩形：描边颜色、填充颜色、圆角 |
| `PdfEllipse` | 椭圆/圆形：描边和填充颜色 |
| `PdfColor` | 颜色（RGB/RGBA/灰度/CMYK），11 个预定义常量 |
| `PdfWatermark` | 文字水印：旋转、透明度、颜色、字体、字号 |

### 文档 (`cloud.opencode.base.pdf.document`)
| 类 | 说明 |
|----|------|
| `DocumentBuilder` | 创建 PDF 文档的流式构建器 |
| `PageBuilder` | 构建单个页面的流式构建器 |
| `Metadata` | 文档元数据记录（标题、作者、主题、关键词、日期） |
| `PageSize` | 预定义页面大小（A0-A6、B4、B5、Letter、Legal）和自定义 |
| `Orientation` | 页面方向枚举（PORTRAIT 纵向、LANDSCAPE 横向） |
| `PdfHeader` | 页眉：左/中/右三区域，可选分隔线 |
| `PdfFooter` | 页脚：左/中/右三区域，`{page}`/`{total}` 占位符 |

### 字体 (`cloud.opencode.base.pdf.font`)
| 类 | 说明 |
|----|------|
| `PdfFont` | PDF 字体密封基础接口，14 种标准字体工厂 |
| `StandardFont` | 枚举：Helvetica、Times、Courier 字体族、Symbol、ZapfDingbats |
| `EmbeddedFont` | 自定义 TrueType（TTF/OTF/TTC）嵌入字体 |

### 表单 (`cloud.opencode.base.pdf.form`)
| 类 | 说明 |
|----|------|
| `PdfForm` | 交互式表单字段容器（AcroForm） |
| `FormField` | 表单字段密封基础接口 |
| `TextField` | 文本输入字段（最大长度、多行、密码） |
| `CheckBox` | 复选框字段（选中状态、导出值） |
| `RadioButton` | 单选按钮字段（选项列表、选择管理） |
| `ComboBox` | 下拉框（可编辑选项） |
| `ListBox` | 列表框（单选/多选） |
| `SignatureField` | 签名字段（签名者信息、状态） |

### 操作 (`cloud.opencode.base.pdf.operation`)
| 类 | 说明 |
|----|------|
| `PdfMerger` | 合并 PDF，保留书签和注释 |
| `PdfSplitter` | 按页面/范围/数量/大小/书签拆分 |
| `PdfExtractor` | 提取文本（按页/全部）和图片 |

### 签名 (`cloud.opencode.base.pdf.signature`)
| 类 | 说明 |
|----|------|
| `PdfSigner` | 使用 PKCS#7 签名 PDF，支持时间戳和外观 |
| `SignatureValidator` | 验证签名，支持吊销检查 |
| `SignatureAppearance` | 签名可视外观（图片/文本/位置） |
| `SignatureInfo` | 签名元数据（名称、原因、位置、证书链） |
| `TimestampInfo` | 时间戳详情（时间、TSA 证书、哈希算法） |

### 异常 (`cloud.opencode.base.pdf.exception`)
| 类 | 说明 |
|----|------|
| `OpenPdfException` | 继承 `OpenException`；错误码：PDF_PARSE、PDF_READ、PDF_WRITE、PDF_SIGN、PDF_FORM、PDF_MERGE、PDF_SPLIT、PDF_DECRYPT |

> **内部包**：`cloud.opencode.base.pdf.internal.parser` 包含 `PdfParser`、`PdfTokenizer`、`PdfObject`、`ContentStreamParser`、`TextExtractor`、`XrefTable`。这些**不属于公开 API**，可能在版本之间变更。

## 安全防护

PDF 解析器对恶意输入实施多层防护：

| 防护项 | 限制值 |
|--------|--------|
| 文件大小 | 500 MB |
| 流解压大小 | 100 MB |
| 对象递归深度 | 100 |
| 嵌套深度（数组/字典） | 100 |
| 集合元素数 | 100,000 |
| xref 条目数 | 10,000,000 |
| CMap 条目数 | 100,000 |
| 页面数 | 100,000 |

## 环境要求

- Java 25+
- 无外部依赖（零依赖）

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
