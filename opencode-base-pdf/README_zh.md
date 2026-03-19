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
- **文本与段落**：支持字体、大小和颜色控制的富文本渲染
- **表格与单元格**：带样式的结构化表格布局
- **图片**：在页面中嵌入光栅图像
- **图形**：线段、矩形和椭圆，支持描边和填充
- **表单**：文本框、复选框、单选按钮、下拉框、列表框和签名字段

### 文档功能
- **元数据**：标题、作者、主题、关键词、创建者和生成者
- **页面大小**：A4、Letter、Legal 和自定义尺寸
- **页面方向**：纵向和横向
- **加密**：密码保护（用户密码和所有者密码）
- **字体**：PDF 标准 14 字体和嵌入自定义字体

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-pdf</artifactId>
    <version>1.0.0</version>
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

### 打开和读取 PDF
```java
try (PdfDocument doc = OpenPdf.open(Path.of("document.pdf"))) {
    System.out.println("页数: " + doc.getPageCount());
    Metadata meta = doc.getMetadata();
}
```

### 合并 PDF
```java
OpenPdf.merge(
    List.of(Path.of("doc1.pdf"), Path.of("doc2.pdf")),
    Path.of("merged.pdf")
);
```

### 拆分 PDF
```java
List<PdfDocument> parts = OpenPdf.split(Path.of("doc.pdf"), "1-3", "4-6");
```

### 提取文本
```java
String text = OpenPdf.extractText(Path.of("document.pdf"));
```

### 数字签名
```java
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
PdfDocument filled = OpenPdf.fillForm(
    Path.of("form.pdf"),
    Map.of("name", "Alice", "email", "alice@example.com")
);
```

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
| `PdfElement` | 所有 PDF 内容元素的基础接口 |
| `PdfText` | 带字体、大小和位置的文本元素 |
| `PdfParagraph` | 带自动换行和对齐的段落元素 |
| `PdfImage` | 嵌入式图片元素 |
| `PdfTable` | 带行和列的表格元素 |
| `PdfCell` | 表格中的单个单元格 |
| `PdfLine` | 线段图形元素 |
| `PdfRectangle` | 矩形图形元素 |
| `PdfEllipse` | 椭圆图形元素 |
| `PdfColor` | PDF 内容元素的颜色表示 |

### 文档 (`cloud.opencode.base.pdf.document`)
| 类 | 说明 |
|----|------|
| `DocumentBuilder` | 创建 PDF 文档的流式构建器 |
| `PageBuilder` | 构建单个页面的流式构建器 |
| `Metadata` | 文档元数据（标题、作者、主题、关键词） |
| `PageSize` | 预定义和自定义页面尺寸 |
| `Orientation` | 页面方向枚举（纵向、横向） |

### 字体 (`cloud.opencode.base.pdf.font`)
| 类 | 说明 |
|----|------|
| `PdfFont` | PDF 字体的基础接口 |
| `StandardFont` | PDF 标准 14 内置字体 |
| `EmbeddedFont` | 从字体文件加载的自定义嵌入字体 |

### 表单 (`cloud.opencode.base.pdf.form`)
| 类 | 说明 |
|----|------|
| `PdfForm` | 交互式表单字段容器 |
| `FormField` | 表单字段基础接口 |
| `TextField` | 文本输入表单字段 |
| `CheckBox` | 复选框表单字段 |
| `RadioButton` | 单选按钮表单字段 |
| `ComboBox` | 下拉框表单字段 |
| `ListBox` | 多选列表框表单字段 |
| `SignatureField` | 数字签名表单字段 |

### 操作 (`cloud.opencode.base.pdf.operation`)
| 类 | 说明 |
|----|------|
| `PdfMerger` | 将多个 PDF 文档合并为一个 |
| `PdfSplitter` | 按页面范围将 PDF 拆分为多个文档 |
| `PdfExtractor` | 从 PDF 页面提取文本和图片 |

### 签名 (`cloud.opencode.base.pdf.signature`)
| 类 | 说明 |
|----|------|
| `PdfSigner` | 使用数字证书签名 PDF 文档 |
| `SignatureValidator` | 验证 PDF 文档上的数字签名 |
| `SignatureAppearance` | 签名的可视外观配置 |
| `SignatureInfo` | 数字签名的信息 |
| `TimestampInfo` | 与签名关联的时间戳信息 |

### 异常 (`cloud.opencode.base.pdf.exception`)
| 类 | 说明 |
|----|------|
| `OpenPdfException` | PDF 处理错误的运行时异常 |

## 环境要求

- Java 25+
- 无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
