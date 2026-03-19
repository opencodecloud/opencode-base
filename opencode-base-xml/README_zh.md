# OpenCode Base XML

**适用于 Java 25+ 的轻量级 XML 处理库**

`opencode-base-xml` 是基于 JDK 内置 XML API 的轻量级 XML 处理组件，支持 DOM/SAX/StAX 解析、XPath 查询、XML 与 Java 对象绑定、Schema 验证、XSLT 转换、命名空间处理和 XXE 防护。

## 功能特性

### 核心功能
- **DOM 解析**：将 XML 字符串、文件和流解析为 `XmlDocument` / `XmlElement`
- **XML 构建器**：流式 API 构建 XML 文档
- **XPath 查询**：支持命名空间的 XPath 表达式求值
- **Schema 验证**：XSD Schema 验证，提供详细结果

### 高级功能
- **SAX 解析**：事件驱动的 SAX 解析器，支持自定义处理器
- **StAX 解析**：流式 StAX 读写器，适用于大型文档
- **XML 绑定**：基于注解的 XML 与 Java 对象编组/解组
- **XSLT 转换**：从文件或字符串进行 XSLT 转换
- **命名空间支持**：命名空间上下文创建和提取
- **XML 格式化**：美化打印和压缩 XML
- **DTD 验证**：基于 DTD 的文档验证
- **安全**：通过 `SecureParserFactory` 内置 XXE 防护
- **自定义适配器**：可插拔的类型适配器（如 DateAdapter）

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-xml</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.xml.*;

// 解析 XML
XmlDocument doc = OpenXml.parse("<root><name>test</name></root>");
XmlDocument doc = OpenXml.parseFile(Path.of("data.xml"));

// 构建 XML
XmlDocument doc = OpenXml.builder("root")
    .element("name", "test")
    .element("age", "25")
    .build();

// XPath 查询
String value = OpenXml.xpath(doc, "//name/text()");
List<XmlElement> items = OpenXml.xpathElements(doc, "//item");

// 带命名空间的 XPath
String ns = OpenXml.xpath(doc, "//ns:name/text()",
    Map.of("ns", "http://example.com/ns"));

// 验证
boolean wellFormed = OpenXml.isWellFormed(xmlString);
ValidationResult result = OpenXml.validateSchema(xml, Path.of("schema.xsd"));

// 格式化和压缩
String formatted = OpenXml.format(xml, 4);
String minified = OpenXml.minify(xml);

// XSLT 转换
String transformed = OpenXml.xslt(xml, Path.of("transform.xslt"));

// 对象绑定
User user = OpenXml.unmarshal(xml, User.class);
String xml = OpenXml.marshal(user);
String pretty = OpenXml.marshal(user, 4);

// StAX 流式处理
try (StaxReader reader = OpenXml.staxReader(xml)) {
    // 流式遍历元素
}
StaxWriter writer = OpenXml.staxWriter();
```

## 类参考

### 根包 (`cloud.opencode.base.xml`)
| 类 | 说明 |
|----|------|
| `OpenXml` | 主门面：解析、构建、XPath、验证、转换、绑定、SAX、StAX、命名空间 |
| `XmlDocument` | 解析后的 XML 文档封装，提供查询和操作方法 |
| `XmlElement` | XML 元素封装，提供属性和子元素访问 |
| `XmlNode` | XML 节点基础抽象 |

### 绑定 (`xml.bind`)
| 类 | 说明 |
|----|------|
| `XmlBinder` | XML 与 Java 对象编组/解组引擎 |
| `@XmlAttribute` | 将字段映射为 XML 属性 |
| `@XmlElement` | 将字段映射为 XML 元素 |
| `@XmlElementList` | 将字段映射为 XML 元素列表 |
| `@XmlIgnore` | 在 XML 绑定中排除字段 |
| `@XmlRoot` | 指定类的根元素名称 |
| `@XmlValue` | 将字段映射为元素文本内容 |

### 绑定适配器 (`xml.bind.adapter`)
| 类 | 说明 |
|----|------|
| `XmlAdapter<T>` | XML 绑定中的自定义类型转换接口 |
| `DateAdapter` | 内置的日期类型转换适配器 |

### 构建器 (`xml.builder`)
| 类 | 说明 |
|----|------|
| `XmlBuilder` | 流式 XML 文档构建器 |
| `ElementBuilder` | 流式 XML 元素构建器 |

### DOM (`xml.dom`)
| 类 | 说明 |
|----|------|
| `DomBuilder` | DOM 文档构建工具 |
| `DomParser` | 基于 DOM 的 XML 解析器 |
| `DomUtil` | DOM 树操作工具 |

### 异常 (`xml.exception`)
| 类 | 说明 |
|----|------|
| `OpenXmlException` | XML 操作基础异常 |
| `XmlBindException` | XML 绑定错误异常 |
| `XmlParseException` | XML 解析错误异常 |
| `XmlSecurityException` | XML 安全违规异常（XXE 等） |
| `XmlTransformException` | XSLT 转换错误异常 |
| `XmlValidationException` | XML 验证错误异常 |
| `XmlXPathException` | XPath 求值错误异常 |

### 命名空间 (`xml.namespace`)
| 类 | 说明 |
|----|------|
| `NamespaceUtil` | 命名空间提取和上下文创建 |
| `OpenNamespaceContext` | 可配置的命名空间上下文，用于 XPath 查询 |

### SAX (`xml.sax`)
| 类 | 说明 |
|----|------|
| `SaxParser` | 安全的 SAX 解析器，内置 XXE 防护 |
| `SaxHandler` | SAX 事件处理器接口 |
| `SimpleSaxHandler` | 简化的 SAX 处理器，提供元素回调 |
| `SaxParseException` | SAX 特定的解析异常 |

### 安全 (`xml.security`)
| 类 | 说明 |
|----|------|
| `SecureParserFactory` | XXE 防护的 XML 解析器工厂 |
| `XmlSecurity` | XML 安全配置工具 |

### StAX (`xml.stax`)
| 类 | 说明 |
|----|------|
| `StaxReader` | 流式 XML 读取器（基于 StAX） |
| `StaxWriter` | 流式 XML 写入器（基于 StAX） |
| `StaxUtil` | StAX 工具方法 |

### 转换 (`xml.transform`)
| 类 | 说明 |
|----|------|
| `XmlTransformer` | XML 格式化（美化打印、压缩） |
| `XsltTransformer` | 从文件或字符串进行 XSLT 转换 |

### 验证 (`xml.validate`)
| 类 | 说明 |
|----|------|
| `XmlValidator` | XML 格式良好性验证 |
| `SchemaValidator` | 基于 XSD Schema 的验证 |
| `DtdValidator` | 基于 DTD 的验证 |
| `ValidationResult` | 验证结果，包含错误和警告 |

### XPath (`xml.xpath`)
| 类 | 说明 |
|----|------|
| `OpenXPath` | 支持命名空间的 XPath 表达式求值 |
| `XPathQuery` | XPath 查询构建器 |
| `XPathResult` | XPath 求值结果封装 |

## 环境要求

- Java 25+
- 无外部依赖（使用 JDK 内置 XML API）

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
