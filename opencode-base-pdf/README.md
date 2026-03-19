# OpenCode Base PDF

**Zero-dependency PDF processing library for Java 25+**

`opencode-base-pdf` is a comprehensive PDF library that provides document creation, merging, splitting, form filling, digital signatures, and content extraction -- all without external dependencies.

## Features

### Core Features
- **Document Creation**: Fluent API for building PDF documents with text, images, tables, and shapes
- **Document Reading**: Open and parse existing PDF files from paths, streams, or byte arrays
- **Merge & Split**: Merge multiple PDFs into one or split a PDF by page ranges
- **Form Filling**: Fill interactive form fields and flatten forms
- **Digital Signatures**: Sign PDFs with PKCS#12 keystores and verify existing signatures
- **Content Extraction**: Extract text and images from PDF pages

### Content Elements
- **Text & Paragraphs**: Rich text rendering with font, size, and color control
- **Tables & Cells**: Structured table layout with cell styling
- **Images**: Embed raster images into pages
- **Shapes**: Lines, rectangles, and ellipses with stroke and fill
- **Forms**: Text fields, checkboxes, radio buttons, combo boxes, list boxes, and signature fields

### Document Features
- **Metadata**: Title, author, subject, keywords, creator, and producer
- **Page Sizes**: A4, Letter, Legal, and custom dimensions
- **Orientation**: Portrait and landscape support
- **Encryption**: Password protection (user and owner passwords)
- **Fonts**: Standard 14 PDF fonts and embedded custom fonts

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-pdf</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Create a PDF
```java
import cloud.opencode.base.pdf.OpenPdf;
import java.nio.file.Path;

OpenPdf.create()
    .title("My Document")
    .author("John Doe")
    .addPage()
        .text("Hello, World!", 100, 700)
    .endPage()
    .save(Path.of("hello.pdf"));
```

### Open and Read a PDF
```java
try (PdfDocument doc = OpenPdf.open(Path.of("document.pdf"))) {
    System.out.println("Pages: " + doc.getPageCount());
    Metadata meta = doc.getMetadata();
}
```

### Merge PDFs
```java
OpenPdf.merge(
    List.of(Path.of("doc1.pdf"), Path.of("doc2.pdf")),
    Path.of("merged.pdf")
);
```

### Split a PDF
```java
List<PdfDocument> parts = OpenPdf.split(Path.of("doc.pdf"), "1-3", "4-6");
```

### Extract Text
```java
String text = OpenPdf.extractText(Path.of("document.pdf"));
```

### Digital Signatures
```java
// Sign
PdfDocument signed = OpenPdf.sign(
    Path.of("doc.pdf"),
    Path.of("keystore.p12"),
    "password".toCharArray(),
    "alias"
);

// Verify
List<SignatureInfo> sigs = OpenPdf.verifySignatures(Path.of("signed.pdf"));
```

### Fill Forms
```java
PdfDocument filled = OpenPdf.fillForm(
    Path.of("form.pdf"),
    Map.of("name", "Alice", "email", "alice@example.com")
);
```

## Class Reference

### Root Package (`cloud.opencode.base.pdf`)
| Class | Description |
|-------|-------------|
| `OpenPdf` | Main facade providing factory methods for all PDF operations |
| `PdfDocument` | Interface representing a PDF document with pages, metadata, and forms |
| `PdfPage` | Represents a single page within a PDF document |

### Content (`cloud.opencode.base.pdf.content`)
| Class | Description |
|-------|-------------|
| `PdfElement` | Base interface for all PDF content elements |
| `PdfText` | A text element with font, size, and position |
| `PdfParagraph` | A paragraph element with line wrapping and alignment |
| `PdfImage` | An embedded image element |
| `PdfTable` | A table element with rows and columns |
| `PdfCell` | A single cell within a table |
| `PdfLine` | A line shape element |
| `PdfRectangle` | A rectangle shape element |
| `PdfEllipse` | An ellipse shape element |
| `PdfColor` | Color representation for PDF content elements |

### Document (`cloud.opencode.base.pdf.document`)
| Class | Description |
|-------|-------------|
| `DocumentBuilder` | Fluent builder for creating PDF documents |
| `PageBuilder` | Fluent builder for constructing individual pages |
| `Metadata` | Document metadata (title, author, subject, keywords) |
| `PageSize` | Predefined and custom page dimensions |
| `Orientation` | Page orientation enum (portrait, landscape) |

### Font (`cloud.opencode.base.pdf.font`)
| Class | Description |
|-------|-------------|
| `PdfFont` | Base interface for PDF fonts |
| `StandardFont` | Standard 14 built-in PDF fonts |
| `EmbeddedFont` | Custom embedded font from a font file |

### Form (`cloud.opencode.base.pdf.form`)
| Class | Description |
|-------|-------------|
| `PdfForm` | Container for interactive form fields |
| `FormField` | Base interface for form fields |
| `TextField` | Text input form field |
| `CheckBox` | Checkbox form field |
| `RadioButton` | Radio button form field |
| `ComboBox` | Drop-down combo box form field |
| `ListBox` | Multi-select list box form field |
| `SignatureField` | Digital signature form field |

### Operation (`cloud.opencode.base.pdf.operation`)
| Class | Description |
|-------|-------------|
| `PdfMerger` | Merges multiple PDF documents into one |
| `PdfSplitter` | Splits a PDF into multiple documents by page ranges |
| `PdfExtractor` | Extracts text and images from PDF pages |

### Signature (`cloud.opencode.base.pdf.signature`)
| Class | Description |
|-------|-------------|
| `PdfSigner` | Signs PDF documents with digital certificates |
| `SignatureValidator` | Validates digital signatures on PDF documents |
| `SignatureAppearance` | Visual appearance configuration for signatures |
| `SignatureInfo` | Information about a digital signature |
| `TimestampInfo` | Timestamp information associated with a signature |

### Exception (`cloud.opencode.base.pdf.exception`)
| Class | Description |
|-------|-------------|
| `OpenPdfException` | Runtime exception for PDF processing errors |

## Requirements

- Java 25+
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
