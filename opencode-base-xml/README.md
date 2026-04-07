# OpenCode Base XML

**Lightweight XML processing library for Java 25+**

`opencode-base-xml` is a lightweight XML processing component based on JDK built-in XML APIs, supporting DOM/SAX/StAX parsing, XPath queries, XML-to-Java binding, schema validation, XSLT transformation, namespace handling, and XXE protection.

## Features

### Core Features
- **DOM Parsing**: Parse XML strings, files, and streams to `XmlDocument` / `XmlElement`
- **XML Builder**: Fluent builder API for constructing XML documents
- **XPath Queries**: XPath expression evaluation with namespace support
- **Schema Validation**: XSD schema validation with detailed results

### Advanced Features
- **SAX Parsing**: Event-driven SAX parser with custom handlers
- **StAX Parsing**: Streaming StAX reader and writer for large documents
- **XML Binding**: Annotation-based XML-to-Java object marshalling/unmarshalling (supports Records)
- **XSLT Transformation**: XSLT transformation from files or strings
- **Namespace Support**: Namespace context creation and extraction
- **XML Formatting**: Pretty-print and minify XML
- **DTD Validation**: DTD-based document validation
- **Security**: Built-in XXE protection via `SecureParserFactory`
- **Custom Adapters**: Pluggable type adapters (e.g., DateAdapter)

### V1.0.3 New Features
- **XML Diff**: Compare two XML documents structurally, detect added/removed/modified elements and attributes
- **XML Merge**: Merge two XML documents with configurable strategies (OVERRIDE, APPEND, SKIP_EXISTING)
- **XML Path**: Simplified dot-notation path access (e.g., `"config.db.host"`) as a lightweight alternative to XPath
- **XML Splitter**: Stream-based large XML splitting by element name with O(1) memory usage
- **XML Canonicalization**: C14N-style canonicalization for consistent XML output
- **Record Binding**: Full Java Record support in `XmlBinder` via canonical constructor
- **Unified Exception Hierarchy**: `OpenXmlException` now extends `OpenException` (the core unified exception base)

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-xml</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Parse & Build

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import java.nio.file.Path;

// Parse from string
XmlDocument doc = OpenXml.parse("<root><name>test</name></root>");

// Parse from file
XmlDocument fileDoc = OpenXml.parseFile(Path.of("data.xml"));

// Build XML fluently
XmlDocument built = OpenXml.builder("root")
    .element("name", "test")
    .element("age", "25")
    .build();

// Access elements
XmlElement root = doc.getRoot();
String name = doc.getElementText("name");       // "test"
String xml = doc.toXml(4);                       // pretty-print
```

### XPath Queries

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import java.util.List;
import java.util.Map;

XmlDocument doc = OpenXml.parse("<users><user id='1'><name>Alice</name></user></users>");

String value = OpenXml.xpath(doc, "//name/text()");              // "Alice"
List<XmlElement> users = OpenXml.xpathElements(doc, "//user");   // [<user>...]

// With namespaces
String ns = OpenXml.xpath(doc, "//ns:name/text()",
    Map.of("ns", "http://example.com/ns"));
```

### Simplified Path Access (V1.0.3)

```java
import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import cloud.opencode.base.xml.path.XmlPath;
import java.util.List;
import java.util.Optional;

XmlDocument doc = XmlDocument.parse("""
    <config>
        <db><host>localhost</host><port>5432</port></db>
        <items><item>a</item><item>b</item><item>c</item></items>
        <users><user id="1"><name>Alice</name></user></users>
    </config>
    """);

// Dot-notation access
String host = XmlPath.getString(doc, "config.db.host");          // "localhost"
int port = XmlPath.getInt(doc, "config.db.port", 3306);          // 5432
Optional<String> opt = XmlPath.getOptional(doc, "config.db.host");

// Attribute access via @
String id = XmlPath.getAttribute(doc, "config.users.user.@id");  // "1"

// Index access
String name = XmlPath.getString(doc, "config.users.user[0].name"); // "Alice"

// List access
List<String> items = XmlPath.getStrings(doc, "config.items.item"); // [a, b, c]
List<XmlElement> elems = XmlPath.getElements(doc, "config.items.item");

// Check existence
boolean exists = XmlPath.exists(doc, "config.db.host");          // true

// Set value (creates intermediate elements)
XmlPath.set(doc, "config.cache.host", "redis.local");
```

### XML Diff (V1.0.3)

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.diff.XmlDiff;
import cloud.opencode.base.xml.diff.DiffEntry;
import cloud.opencode.base.xml.diff.DiffType;
import java.util.List;

String xml1 = "<root><name>old</name></root>";
String xml2 = "<root><name>new</name></root>";

// Compare
List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);
for (DiffEntry entry : diffs) {
    System.out.println(entry.path() + " " + entry.type() + ": "
        + entry.oldValue() + " -> " + entry.newValue());
}

// Quick equality check
boolean equal = XmlDiff.isEqual(xml1, xml2);       // false

// Via facade
boolean same = OpenXml.xmlEquals(xml1, xml2);       // false
```

### XML Merge (V1.0.3)

```java
import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.merge.XmlMerge;
import cloud.opencode.base.xml.merge.MergeStrategy;

XmlDocument base = XmlDocument.parse("<config><db><host>localhost</host></db></config>");
XmlDocument overlay = XmlDocument.parse("<config><db><host>prod-db</host><port>5432</port></db></config>");

// Override strategy (default): overlay replaces matching elements
XmlDocument merged = XmlMerge.merge(base, overlay);
// Result: <config><db><host>prod-db</host><port>5432</port></db></config>

// Append strategy: overlay elements are always appended
XmlDocument appended = XmlMerge.merge(base, overlay, MergeStrategy.APPEND);

// Skip existing: only add elements not in base
XmlDocument skipped = XmlMerge.merge(base, overlay, MergeStrategy.SKIP_EXISTING);
```

### XML Splitter (V1.0.3)

```java
import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.splitter.XmlSplitter;
import java.io.InputStream;
import java.util.List;

// Split large XML by element name — O(1) memory per fragment
XmlSplitter.split(inputStream, "record", fragment -> {
    String name = fragment.xpath("//name/text()");
    System.out.println("Processing: " + name);
});

// Collect all fragments
List<XmlDocument> records = XmlSplitter.splitAll(xml, "record");

// Count without loading into memory
int count = XmlSplitter.count(inputStream, "record");
```

### XML Canonicalization (V1.0.3)

```java
import cloud.opencode.base.xml.canonical.XmlCanonicalizer;

// Attributes sorted, whitespace normalized, no XML declaration
String canonical = XmlCanonicalizer.canonicalize("<root b='2' a='1'/>");
// Result: <root a="1" b="2"/>

// With comment removal
String noComments = XmlCanonicalizer.canonicalize(xml, true);
```

### Record Binding (V1.0.3)

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.bind.XmlBinder;
import cloud.opencode.base.xml.bind.annotation.*;

@XmlRoot("user")
record User(
    @XmlAttribute("id") int id,
    String name,
    String email
) {}

// Unmarshal XML to Record
String xml = "<user id='1'><name>Alice</name><email>alice@example.com</email></user>";
User user = OpenXml.unmarshal(xml, User.class);
// user.id() == 1, user.name() == "Alice"

// Marshal Record to XML
String output = OpenXml.marshal(user, 4);
```

### Validate & Format

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.validate.ValidationResult;
import java.nio.file.Path;

// Well-formedness check
boolean wellFormed = OpenXml.isWellFormed(xmlString);

// XSD schema validation
ValidationResult result = OpenXml.validateSchema(xml, Path.of("schema.xsd"));
if (!result.isValid()) {
    result.getErrors().forEach(e -> System.err.println(e.message()));
}

// Format and minify
String formatted = OpenXml.format(xml, 4);
String minified = OpenXml.minify(xml);
```

### XSLT & StAX

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.stax.StaxReader;
import cloud.opencode.base.xml.stax.StaxWriter;
import java.nio.file.Path;

// XSLT transformation
String transformed = OpenXml.xslt(xml, Path.of("transform.xslt"));

// StAX streaming read
try (StaxReader reader = OpenXml.staxReader(xml)) {
    while (reader.hasNext()) {
        reader.next();
        if (reader.isStartElement("user")) {
            String id = reader.getAttribute("id");
        }
    }
}

// StAX streaming write
String output = StaxWriter.create()
    .startDocument()
    .startElement("users")
        .startElement("user").attribute("id", "1")
            .element("name", "Alice")
        .endElement()
    .endElement()
    .endDocument()
    .toString();
```

## V1.0.3 New API Reference

### XmlDiff (`cloud.opencode.base.xml.diff`)

| Method | Description |
|--------|-------------|
| `XmlDiff.diff(String, String)` | Compare two XML strings, return list of `DiffEntry` |
| `XmlDiff.diff(XmlDocument, XmlDocument)` | Compare two documents |
| `XmlDiff.isEqual(String, String)` | Check structural equality |
| `XmlDiff.isEqual(XmlDocument, XmlDocument)` | Check structural equality |
| `DiffEntry.path()` | XPath-like path to the difference |
| `DiffEntry.type()` | Type: ADDED, REMOVED, MODIFIED, TEXT_MODIFIED, ATTRIBUTE_* |
| `DiffEntry.oldValue()` | Old value (null if ADDED) |
| `DiffEntry.newValue()` | New value (null if REMOVED) |

### XmlMerge (`cloud.opencode.base.xml.merge`)

| Method | Description |
|--------|-------------|
| `XmlMerge.merge(XmlDocument, XmlDocument)` | Merge with OVERRIDE strategy |
| `XmlMerge.merge(XmlDocument, XmlDocument, MergeStrategy)` | Merge with specified strategy |
| `XmlMerge.merge(String, String)` | Merge XML strings (OVERRIDE) |
| `XmlMerge.merge(String, String, MergeStrategy)` | Merge XML strings with strategy |

**MergeStrategy**: `OVERRIDE` (replace matching), `APPEND` (always append), `SKIP_EXISTING` (only add new)

### XmlPath (`cloud.opencode.base.xml.path`)

| Method | Description |
|--------|-------------|
| `XmlPath.getString(XmlDocument, String)` | Get text value at path |
| `XmlPath.getString(XmlElement, String)` | Get text value relative to element |
| `XmlPath.getString(XmlDocument, String, String)` | Get with default value |
| `XmlPath.getOptional(XmlDocument, String)` | Get as Optional |
| `XmlPath.getInt(XmlDocument, String, int)` | Get as int with default |
| `XmlPath.getBoolean(XmlDocument, String, boolean)` | Get as boolean with default |
| `XmlPath.getAttribute(XmlDocument, String)` | Get attribute via `@attr` notation |
| `XmlPath.getElement(XmlDocument, String)` | Get element at path |
| `XmlPath.getElements(XmlDocument, String)` | Get all matching elements |
| `XmlPath.getStrings(XmlDocument, String)` | Get text values of matching elements |
| `XmlPath.exists(XmlDocument, String)` | Check if path exists |
| `XmlPath.set(XmlDocument, String, String)` | Set value, creating elements as needed |

**Path syntax**: `"root.child.name"`, `"root.items[2].name"`, `"root.child.@attr"`

### XmlSplitter (`cloud.opencode.base.xml.splitter`)

| Method | Description |
|--------|-------------|
| `XmlSplitter.split(InputStream, String, Consumer)` | Split stream, callback per fragment |
| `XmlSplitter.split(Path, String, Consumer)` | Split file |
| `XmlSplitter.split(String, String, Consumer)` | Split string |
| `XmlSplitter.splitIndexed(InputStream, String, Consumer)` | Split with index via `SplitResult` |
| `XmlSplitter.splitAll(String, String)` | Collect all fragments to list |
| `XmlSplitter.count(InputStream, String)` | Count matching elements (O(1) memory) |
| `XmlSplitter.count(String, String)` | Count from string |

### XmlCanonicalizer (`cloud.opencode.base.xml.canonical`)

| Method | Description |
|--------|-------------|
| `XmlCanonicalizer.canonicalize(String)` | Canonicalize XML string (keep comments) |
| `XmlCanonicalizer.canonicalize(XmlDocument)` | Canonicalize document |
| `XmlCanonicalizer.canonicalize(String, boolean)` | Canonicalize with optional comment removal |
| `XmlCanonicalizer.canonicalize(XmlDocument, boolean)` | Canonicalize document with options |

## Class Reference

### Root Package (`cloud.opencode.base.xml`)
| Class | Description |
|-------|-------------|
| `OpenXml` | Main facade: parse, build, XPath, validate, transform, bind, diff, merge, split, path, canonicalize |
| `XmlDocument` | Parsed XML document wrapper with query and manipulation methods |
| `XmlElement` | XML element wrapper with attribute and child access |
| `XmlNode` | Base XML node abstraction |

### Bind (`xml.bind`)
| Class | Description |
|-------|-------------|
| `XmlBinder` | XML-to-Java object marshalling and unmarshalling engine (supports Records) |
| `@XmlAttribute` | Map field/record component to XML attribute |
| `@XmlElement` | Map field/record component to XML element |
| `@XmlElementList` | Map field/record component to list of XML elements |
| `@XmlIgnore` | Exclude field/record component from XML binding |
| `@XmlRoot` | Specify root element name for a class |
| `@XmlValue` | Map field/record component to element text content |

### Bind Adapter (`xml.bind.adapter`)
| Class | Description |
|-------|-------------|
| `XmlAdapter<T>` | Interface for custom type conversion in XML binding |
| `DateAdapter` | Built-in adapter for date type conversion |

### Builder (`xml.builder`)
| Class | Description |
|-------|-------------|
| `XmlBuilder` | Fluent XML document builder |
| `ElementBuilder` | Fluent XML element builder |

### DOM (`xml.dom`)
| Class | Description |
|-------|-------------|
| `DomBuilder` | DOM document builder utilities |
| `DomParser` | DOM-based XML parser |
| `DomUtil` | DOM tree manipulation utilities |

### Exception (`xml.exception`)
| Class | Description |
|-------|-------------|
| `OpenXmlException` | Base exception for XML operations (extends `OpenException`) |
| `XmlBindException` | Exception for XML binding errors |
| `XmlParseException` | Exception for XML parsing errors |
| `XmlSecurityException` | Exception for XML security violations (XXE, etc.) |
| `XmlTransformException` | Exception for XSLT transformation errors |
| `XmlValidationException` | Exception for XML validation errors |
| `XmlXPathException` | Exception for XPath evaluation errors |

### Namespace (`xml.namespace`)
| Class | Description |
|-------|-------------|
| `NamespaceUtil` | Namespace extraction and context creation |
| `OpenNamespaceContext` | Configurable namespace context for XPath queries |

### SAX (`xml.sax`)
| Class | Description |
|-------|-------------|
| `SaxParser` | Secure SAX parser with XXE protection |
| `SaxHandler` | SAX event handler interface |
| `SimpleSaxHandler` | Simplified SAX handler with element callbacks |
| `SaxParseException` | SAX-specific parse exception |

### Security (`xml.security`)
| Class | Description |
|-------|-------------|
| `SecureParserFactory` | Factory for XXE-protected XML parsers |
| `XmlSecurity` | XML security configuration utilities |

### StAX (`xml.stax`)
| Class | Description |
|-------|-------------|
| `StaxReader` | Streaming XML reader (StAX-based) |
| `StaxWriter` | Streaming XML writer (StAX-based) |
| `StaxUtil` | StAX utility methods |

### Transform (`xml.transform`)
| Class | Description |
|-------|-------------|
| `XmlTransformer` | XML formatting (pretty-print, minify) |
| `XsltTransformer` | XSLT transformation from files or strings |

### Validate (`xml.validate`)
| Class | Description |
|-------|-------------|
| `XmlValidator` | XML well-formedness validation |
| `SchemaValidator` | XSD schema-based validation |
| `DtdValidator` | DTD-based validation |
| `ValidationResult` | Validation result with errors and warnings |

### XPath (`xml.xpath`)
| Class | Description |
|-------|-------------|
| `OpenXPath` | XPath expression evaluation with namespace support |
| `XPathQuery` | XPath query builder |
| `XPathResult` | XPath evaluation result wrapper |

## Requirements

- Java 25+
- No external dependencies (uses JDK built-in XML APIs)

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
