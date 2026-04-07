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
- **Text & Paragraphs**: Rich text rendering with font, size, color, rotation, underline, character/word spacing, and automatic word-wrap
- **Tables & Cells**: Structured table layout with header rows, alternating row colors, and cell styling
- **Images**: Embed JPEG (DCTDecode) and PNG (FlateDecode with alpha/SMask) images
- **Shapes**: Lines, rectangles (stroke + fill), and ellipses (Bezier curve approximation)

### V1.0.3 New Features
- **Watermark**: Diagonal or custom-angle text watermarks with opacity, color, font, and rotation control
- **Headers & Footers**: Auto page numbering with `{page}` / `{total}` placeholders; left/center/right zones with optional separator line
- **Enhanced Rendering**: Ellipses, paragraphs with word-wrap and alignment (left/center/right/justify), text rotation, underline, character/word spacing
- **Image Embedding**: JPEG (DCTDecode pass-through) and PNG (FlateDecode with alpha channel SMask)
- **PDF Parser**: Read existing PDF 1.4-1.7 files; extract text with FlateDecode, ASCII85Decode, ASCIIHexDecode and ToUnicode CMap support
- **Exception Hierarchy**: `OpenPdfException` now extends `OpenException` with component/error-code support

### Document Features
- **Metadata**: Title, author, subject, keywords, creator, and producer
- **Page Sizes**: A0-A6, B4, B5, Letter, Legal, and custom dimensions
- **Orientation**: Portrait and landscape support
- **Encryption**: Password protection (user and owner passwords)
- **Fonts**: Standard 14 PDF Type1 fonts and embedded TrueType fonts

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-pdf</artifactId>
    <version>1.0.3</version>
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

### Open and Extract Text
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.PdfDocument;
import cloud.opencode.base.pdf.document.Metadata;
import java.nio.file.Path;

// Open an existing PDF
try (PdfDocument doc = OpenPdf.open(Path.of("document.pdf"))) {
    System.out.println("Pages: " + doc.getPageCount());
    Metadata meta = doc.getMetadata();
    System.out.println("Title: " + meta.title());
}

// Extract text from all pages
String text = OpenPdf.extractText(Path.of("document.pdf"));
```

### Add a Watermark
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.content.PdfWatermark;
import java.nio.file.Path;

OpenPdf.create()
    .watermark(PdfWatermark.text("CONFIDENTIAL")
        .rotation(-45)
        .opacity(0.15f)
        .fontSize(60))
    .addPage()
        .text("Sensitive content", 100, 700)
    .endPage()
    .save(Path.of("watermarked.pdf"));
```

### Headers and Footers with Page Numbers
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.document.PdfHeader;
import cloud.opencode.base.pdf.document.PdfFooter;
import java.nio.file.Path;

OpenPdf.create()
    .header(PdfHeader.builder()
        .left("Monthly Report")
        .right("2026-04"))
    .footer(PdfFooter.builder()
        .center("Page {page} of {total}"))
    .addPage()
        .text("Page 1 content", 100, 600)
    .nextPage()
        .text("Page 2 content", 100, 600)
    .endPage()
    .save(Path.of("report.pdf"));
```

### Merge PDFs
```java
import cloud.opencode.base.pdf.OpenPdf;
import java.nio.file.Path;
import java.util.List;

OpenPdf.merge(
    List.of(Path.of("doc1.pdf"), Path.of("doc2.pdf")),
    Path.of("merged.pdf")
);
```

### Split a PDF
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.PdfDocument;
import java.nio.file.Path;
import java.util.List;

List<PdfDocument> parts = OpenPdf.split(Path.of("doc.pdf"), "1-3", "4-6");
```

### Digital Signatures
```java
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.PdfDocument;
import cloud.opencode.base.pdf.signature.SignatureInfo;
import java.nio.file.Path;
import java.util.List;

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
import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.PdfDocument;
import java.nio.file.Path;
import java.util.Map;

PdfDocument filled = OpenPdf.fillForm(
    Path.of("form.pdf"),
    Map.of("name", "Alice", "email", "alice@example.com")
);
```

## API Reference — `OpenPdf`

### Document Creation

| Method | Description |
|--------|-------------|
| `create()` | Create a new PDF document builder (default A4) |
| `create(PageSize)` | Create a document builder with specified page size |

### Document Reading

| Method | Description |
|--------|-------------|
| `open(Path)` | Open an existing PDF from file path |
| `open(InputStream)` | Open an existing PDF from input stream |
| `open(byte[])` | Open an existing PDF from byte array |
| `open(Path, String)` | Open a password-protected PDF |

### Merge & Split

| Method | Description |
|--------|-------------|
| `merger()` | Create a PDF merger |
| `merge(List<Path>, Path)` | Merge multiple PDF files into one |
| `merge(List<PdfDocument>)` | Merge multiple PDF documents into one |
| `splitter()` | Create a PDF splitter |
| `split(Path, String...)` | Split PDF by page ranges (e.g., `"1-3"`, `"5"`) |
| `splitToPages(Path)` | Split PDF into single-page documents |

### Form Operations

| Method | Description |
|--------|-------------|
| `fillForm(Path, Map)` | Fill form fields in a PDF |
| `fillAndFlatten(Path, Map)` | Fill form and flatten (make non-editable) |
| `extractFormFields(Path)` | Extract form field names and current values |

### Digital Signatures

| Method | Description |
|--------|-------------|
| `signer()` | Create a PDF signer |
| `sign(Path, Path, char[], String)` | Sign a PDF with PKCS#12 keystore |
| `verifySignatures(Path)` | Verify signatures and return results |

### Content Extraction

| Method | Description |
|--------|-------------|
| `extractor()` | Create a PDF extractor |
| `extractText(Path)` | Extract all text from a PDF |
| `extractText(Path, int...)` | Extract text from specific pages (1-based) |
| `extractImages(Path)` | Extract images as byte arrays |

### Utility Methods

| Method | Description |
|--------|-------------|
| `getPageCount(Path)` | Get the page count of a PDF |
| `getMetadata(Path)` | Get PDF metadata |
| `isEncrypted(Path)` | Check if a PDF is encrypted |
| `hasForm(Path)` | Check if a PDF contains form fields |
| `isSigned(Path)` | Check if a PDF is digitally signed |

## API Reference — `DocumentBuilder`

| Method | Description |
|--------|-------------|
| `title(String)` | Set document title |
| `author(String)` | Set document author |
| `subject(String)` | Set document subject |
| `keywords(String...)` | Set document keywords |
| `creator(String)` | Set creator application name |
| `pageSize(PageSize)` | Set default page size |
| `orientation(Orientation)` | Set default page orientation |
| `margins(float)` | Set uniform margins (points) |
| `margins(float, float, float, float)` | Set individual margins (top, right, bottom, left) |
| `defaultFont(PdfFont)` | Set default font |
| `embedFont(Path, String)` | Embed a TrueType font file |
| `watermark(PdfWatermark)` | Set text watermark for all pages |
| `watermark(String)` | Set text watermark (shorthand) |
| `header(PdfHeader)` | Set page header for all pages |
| `header(String)` | Set centered page header (shorthand) |
| `footer(PdfFooter)` | Set page footer for all pages |
| `footer(String)` | Set centered page footer (shorthand) |
| `encrypt(String, String)` | Set user and owner passwords |
| `permissions(boolean, boolean, boolean, boolean)` | Set print/copy/modify/annotate permissions |
| `addPage()` | Add a new page (returns `PageBuilder`) |
| `addPage(PageSize)` | Add a page with specific size |
| `addPage(PageSize, Orientation)` | Add a page with specific size and orientation |
| `save(Path)` | Build and save to file |
| `save(OutputStream)` | Build and write to stream |
| `toBytes()` | Build and return as byte array |

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
| `PdfElement` | Sealed base interface for all PDF content elements |
| `PdfText` | Text element with font, size, color, rotation, underline, spacing |
| `PdfParagraph` | Paragraph with word-wrap, alignment, line spacing, first-line indent |
| `PdfImage` | Embedded image element (JPEG/PNG from file, bytes, or stream) |
| `PdfTable` | Table with header rows, data rows, alternating colors, column widths |
| `PdfCell` | Table cell with alignment, colspan, rowspan, font, color |
| `PdfLine` | Line with width, color, and style (solid/dashed/dotted) |
| `PdfRectangle` | Rectangle with stroke color, fill color, rounded corners |
| `PdfEllipse` | Ellipse/circle with stroke and fill colors |
| `PdfColor` | Color (RGB/RGBA/grayscale/CMYK) with 11 predefined constants |
| `PdfWatermark` | Text watermark with rotation, opacity, color, font, and size |

### Document (`cloud.opencode.base.pdf.document`)
| Class | Description |
|-------|-------------|
| `DocumentBuilder` | Fluent builder for creating PDF documents |
| `PageBuilder` | Fluent builder for constructing individual pages |
| `Metadata` | Document metadata record (title, author, subject, keywords, dates) |
| `PageSize` | Predefined page sizes (A0-A6, B4, B5, Letter, Legal) and custom |
| `Orientation` | Page orientation enum (PORTRAIT, LANDSCAPE) |
| `PdfHeader` | Page header with left/center/right zones and optional separator line |
| `PdfFooter` | Page footer with left/center/right zones and `{page}`/`{total}` placeholders |

### Font (`cloud.opencode.base.pdf.font`)
| Class | Description |
|-------|-------------|
| `PdfFont` | Sealed base interface for PDF fonts with 14 standard font factories |
| `StandardFont` | Enum: Helvetica, Times, Courier families, Symbol, ZapfDingbats |
| `EmbeddedFont` | Custom TrueType (TTF/OTF/TTC) embedded font |

### Form (`cloud.opencode.base.pdf.form`)
| Class | Description |
|-------|-------------|
| `PdfForm` | Container for interactive form fields (AcroForm) |
| `FormField` | Sealed base interface for form fields |
| `TextField` | Text input field (max length, multiline, password) |
| `CheckBox` | Checkbox field (checked state, export value) |
| `RadioButton` | Radio button field (options, selection) |
| `ComboBox` | Dropdown menu (editable option) |
| `ListBox` | List box (single/multi-select) |
| `SignatureField` | Signature field (signer info, status) |

### Operation (`cloud.opencode.base.pdf.operation`)
| Class | Description |
|-------|-------------|
| `PdfMerger` | Merge PDFs with bookmark/annotation preservation |
| `PdfSplitter` | Split by pages, ranges, count, size, or bookmarks |
| `PdfExtractor` | Extract text (per page / all) and images |

### Signature (`cloud.opencode.base.pdf.signature`)
| Class | Description |
|-------|-------------|
| `PdfSigner` | Sign PDFs with PKCS#7, timestamp support, visual appearance |
| `SignatureValidator` | Validate signatures with revocation checking |
| `SignatureAppearance` | Visual signature appearance (image/text/position) |
| `SignatureInfo` | Signature metadata (name, reason, location, certificate chain) |
| `TimestampInfo` | Timestamp details (time, TSA certificate, hash algorithm) |

### Exception (`cloud.opencode.base.pdf.exception`)
| Class | Description |
|-------|-------------|
| `OpenPdfException` | Extends `OpenException`; error codes: PDF_PARSE, PDF_READ, PDF_WRITE, PDF_SIGN, PDF_FORM, PDF_MERGE, PDF_SPLIT, PDF_DECRYPT |

> **Internal**: `cloud.opencode.base.pdf.internal.parser` contains `PdfParser`, `PdfTokenizer`, `PdfObject`, `ContentStreamParser`, `TextExtractor`, `XrefTable`. These are **not public API** and may change between releases.

## Security

The PDF parser enforces multiple defense layers against malicious input:

| Protection | Limit |
|-----------|-------|
| File size | 500 MB |
| Stream decompression | 100 MB |
| Object recursion depth | 100 |
| Nesting depth (array/dict) | 100 |
| Collection size | 100,000 elements |
| Xref entries | 10,000,000 |
| CMap entries | 100,000 |
| Page count | 100,000 |

## Requirements

- Java 25+
- No external dependencies (zero-dep)

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
