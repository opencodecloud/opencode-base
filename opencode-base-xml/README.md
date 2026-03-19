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
- **XML Binding**: Annotation-based XML-to-Java object marshalling/unmarshalling
- **XSLT Transformation**: XSLT transformation from files or strings
- **Namespace Support**: Namespace context creation and extraction
- **XML Formatting**: Pretty-print and minify XML
- **DTD Validation**: DTD-based document validation
- **Security**: Built-in XXE protection via `SecureParserFactory`
- **Custom Adapters**: Pluggable type adapters (e.g., DateAdapter)

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-xml</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.xml.*;

// Parse XML
XmlDocument doc = OpenXml.parse("<root><name>test</name></root>");
XmlDocument doc = OpenXml.parseFile(Path.of("data.xml"));

// Build XML
XmlDocument doc = OpenXml.builder("root")
    .element("name", "test")
    .element("age", "25")
    .build();

// XPath queries
String value = OpenXml.xpath(doc, "//name/text()");
List<XmlElement> items = OpenXml.xpathElements(doc, "//item");

// XPath with namespaces
String ns = OpenXml.xpath(doc, "//ns:name/text()",
    Map.of("ns", "http://example.com/ns"));

// Validate
boolean wellFormed = OpenXml.isWellFormed(xmlString);
ValidationResult result = OpenXml.validateSchema(xml, Path.of("schema.xsd"));

// Format and minify
String formatted = OpenXml.format(xml, 4);
String minified = OpenXml.minify(xml);

// XSLT transformation
String transformed = OpenXml.xslt(xml, Path.of("transform.xslt"));

// Object binding
User user = OpenXml.unmarshal(xml, User.class);
String xml = OpenXml.marshal(user);
String pretty = OpenXml.marshal(user, 4);

// StAX streaming
try (StaxReader reader = OpenXml.staxReader(xml)) {
    // stream through elements
}
StaxWriter writer = OpenXml.staxWriter();
```

## Class Reference

### Root Package (`cloud.opencode.base.xml`)
| Class | Description |
|-------|-------------|
| `OpenXml` | Main facade: parse, build, XPath, validate, transform, bind, SAX, StAX, namespace |
| `XmlDocument` | Parsed XML document wrapper with query and manipulation methods |
| `XmlElement` | XML element wrapper with attribute and child access |
| `XmlNode` | Base XML node abstraction |

### Bind (`xml.bind`)
| Class | Description |
|-------|-------------|
| `XmlBinder` | XML-to-Java object marshalling and unmarshalling engine |
| `@XmlAttribute` | Map field to XML attribute |
| `@XmlElement` | Map field to XML element |
| `@XmlElementList` | Map field to list of XML elements |
| `@XmlIgnore` | Exclude field from XML binding |
| `@XmlRoot` | Specify root element name for a class |
| `@XmlValue` | Map field to element text content |

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
| `OpenXmlException` | Base exception for XML operations |
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
