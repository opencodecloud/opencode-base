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
- **XML 绑定**：基于注解的 XML 与 Java 对象编组/解组（支持 Record）
- **XSLT 转换**：从文件或字符串进行 XSLT 转换
- **命名空间支持**：命名空间上下文创建和提取
- **XML 格式化**：美化打印和压缩 XML
- **DTD 验证**：基于 DTD 的文档验证
- **安全**：通过 `SecureParserFactory` 内置 XXE 防护
- **自定义适配器**：可插拔的类型适配器（如 DateAdapter）

### V1.0.3 新增功能
- **XML 差异比较**：结构化比较两个 XML 文档，检测新增/删除/修改的元素和属性
- **XML 合并**：合并两个 XML 文档，支持可配置策略（覆盖、追加、跳过已存在）
- **XML 路径访问**：简化的点表示法路径访问（如 `"config.db.host"`），作为 XPath 的轻量级替代
- **XML 流式拆分**：基于 StAX 的大 XML 文件按元素拆分，O(1) 内存占用
- **XML 规范化**：C14N 风格的规范化，产生一致的 XML 输出
- **Record 绑定**：`XmlBinder` 完整支持 Java Record 类型
- **统一异常体系**：`OpenXmlException` 现继承 `OpenException`（核心统一异常基类）

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-xml</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 解析与构建

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import java.nio.file.Path;

// 从字符串解析
XmlDocument doc = OpenXml.parse("<root><name>test</name></root>");

// 从文件解析
XmlDocument fileDoc = OpenXml.parseFile(Path.of("data.xml"));

// 流式构建 XML
XmlDocument built = OpenXml.builder("root")
    .element("name", "test")
    .element("age", "25")
    .build();

// 访问元素
XmlElement root = doc.getRoot();
String name = doc.getElementText("name");       // "test"
String xml = doc.toXml(4);                       // 格式化输出
```

### XPath 查询

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import java.util.List;
import java.util.Map;

XmlDocument doc = OpenXml.parse("<users><user id='1'><name>Alice</name></user></users>");

String value = OpenXml.xpath(doc, "//name/text()");              // "Alice"
List<XmlElement> users = OpenXml.xpathElements(doc, "//user");   // [<user>...]

// 带命名空间
String ns = OpenXml.xpath(doc, "//ns:name/text()",
    Map.of("ns", "http://example.com/ns"));
```

### 简化路径访问（V1.0.3 新增）

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

// 点表示法访问
String host = XmlPath.getString(doc, "config.db.host");          // "localhost"
int port = XmlPath.getInt(doc, "config.db.port", 3306);          // 5432
Optional<String> opt = XmlPath.getOptional(doc, "config.db.host");

// 属性访问（@ 前缀）
String id = XmlPath.getAttribute(doc, "config.users.user.@id");  // "1"

// 索引访问
String name = XmlPath.getString(doc, "config.users.user[0].name"); // "Alice"

// 列表访问
List<String> items = XmlPath.getStrings(doc, "config.items.item"); // [a, b, c]
List<XmlElement> elems = XmlPath.getElements(doc, "config.items.item");

// 检查存在性
boolean exists = XmlPath.exists(doc, "config.db.host");          // true

// 设置值（自动创建中间元素）
XmlPath.set(doc, "config.cache.host", "redis.local");
```

### XML 差异比较（V1.0.3 新增）

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.diff.XmlDiff;
import cloud.opencode.base.xml.diff.DiffEntry;
import cloud.opencode.base.xml.diff.DiffType;
import java.util.List;

String xml1 = "<root><name>old</name></root>";
String xml2 = "<root><name>new</name></root>";

// 比较
List<DiffEntry> diffs = XmlDiff.diff(xml1, xml2);
for (DiffEntry entry : diffs) {
    System.out.println(entry.path() + " " + entry.type() + ": "
        + entry.oldValue() + " -> " + entry.newValue());
}

// 快速相等检查
boolean equal = XmlDiff.isEqual(xml1, xml2);       // false

// 通过门面调用
boolean same = OpenXml.xmlEquals(xml1, xml2);       // false
```

### XML 合并（V1.0.3 新增）

```java
import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.merge.XmlMerge;
import cloud.opencode.base.xml.merge.MergeStrategy;

XmlDocument base = XmlDocument.parse("<config><db><host>localhost</host></db></config>");
XmlDocument overlay = XmlDocument.parse("<config><db><host>prod-db</host><port>5432</port></db></config>");

// 覆盖策略（默认）：overlay 替换匹配元素
XmlDocument merged = XmlMerge.merge(base, overlay);
// 结果: <config><db><host>prod-db</host><port>5432</port></db></config>

// 追加策略：overlay 元素始终追加
XmlDocument appended = XmlMerge.merge(base, overlay, MergeStrategy.APPEND);

// 跳过已存在：仅添加 base 中不存在的元素
XmlDocument skipped = XmlMerge.merge(base, overlay, MergeStrategy.SKIP_EXISTING);
```

### XML 流式拆分（V1.0.3 新增）

```java
import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.splitter.XmlSplitter;
import java.io.InputStream;
import java.util.List;

// 按元素名拆分大 XML — 每个片段 O(1) 内存
XmlSplitter.split(inputStream, "record", fragment -> {
    String name = fragment.xpath("//name/text()");
    System.out.println("处理: " + name);
});

// 收集所有片段
List<XmlDocument> records = XmlSplitter.splitAll(xml, "record");

// 不加载到内存直接计数
int count = XmlSplitter.count(inputStream, "record");
```

### XML 规范化（V1.0.3 新增）

```java
import cloud.opencode.base.xml.canonical.XmlCanonicalizer;

// 属性排序、空白规范化、无 XML 声明
String canonical = XmlCanonicalizer.canonicalize("<root b='2' a='1'/>");
// 结果: <root a="1" b="2"/>

// 移除注释
String noComments = XmlCanonicalizer.canonicalize(xml, true);
```

### Record 绑定（V1.0.3 新增）

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

// 将 XML 解组为 Record
String xml = "<user id='1'><name>Alice</name><email>alice@example.com</email></user>";
User user = OpenXml.unmarshal(xml, User.class);
// user.id() == 1, user.name() == "Alice"

// 将 Record 编组为 XML
String output = OpenXml.marshal(user, 4);
```

### 验证与格式化

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.validate.ValidationResult;
import java.nio.file.Path;

// 格式良好性检查
boolean wellFormed = OpenXml.isWellFormed(xmlString);

// XSD Schema 验证
ValidationResult result = OpenXml.validateSchema(xml, Path.of("schema.xsd"));
if (!result.isValid()) {
    result.getErrors().forEach(e -> System.err.println(e.message()));
}

// 格式化和压缩
String formatted = OpenXml.format(xml, 4);
String minified = OpenXml.minify(xml);
```

### XSLT 与 StAX

```java
import cloud.opencode.base.xml.OpenXml;
import cloud.opencode.base.xml.stax.StaxReader;
import cloud.opencode.base.xml.stax.StaxWriter;
import java.nio.file.Path;

// XSLT 转换
String transformed = OpenXml.xslt(xml, Path.of("transform.xslt"));

// StAX 流式读取
try (StaxReader reader = OpenXml.staxReader(xml)) {
    while (reader.hasNext()) {
        reader.next();
        if (reader.isStartElement("user")) {
            String id = reader.getAttribute("id");
        }
    }
}

// StAX 流式写入
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

## V1.0.3 新增 API 参考

### XmlDiff (`cloud.opencode.base.xml.diff`)

| 方法 | 说明 |
|------|------|
| `XmlDiff.diff(String, String)` | 比较两个 XML 字符串，返回 `DiffEntry` 列表 |
| `XmlDiff.diff(XmlDocument, XmlDocument)` | 比较两个文档 |
| `XmlDiff.isEqual(String, String)` | 检查结构相等性 |
| `XmlDiff.isEqual(XmlDocument, XmlDocument)` | 检查结构相等性 |
| `DiffEntry.path()` | 差异的类 XPath 路径 |
| `DiffEntry.type()` | 类型: ADDED, REMOVED, MODIFIED, TEXT_MODIFIED, ATTRIBUTE_* |
| `DiffEntry.oldValue()` | 旧值（ADDED 时为 null） |
| `DiffEntry.newValue()` | 新值（REMOVED 时为 null） |

### XmlMerge (`cloud.opencode.base.xml.merge`)

| 方法 | 说明 |
|------|------|
| `XmlMerge.merge(XmlDocument, XmlDocument)` | 使用 OVERRIDE 策略合并 |
| `XmlMerge.merge(XmlDocument, XmlDocument, MergeStrategy)` | 使用指定策略合并 |
| `XmlMerge.merge(String, String)` | 合并 XML 字符串（OVERRIDE） |
| `XmlMerge.merge(String, String, MergeStrategy)` | 使用指定策略合并 XML 字符串 |

**合并策略**: `OVERRIDE`（替换匹配项）、`APPEND`（始终追加）、`SKIP_EXISTING`（仅添加新增）

### XmlPath (`cloud.opencode.base.xml.path`)

| 方法 | 说明 |
|------|------|
| `XmlPath.getString(XmlDocument, String)` | 获取路径处的文本值 |
| `XmlPath.getString(XmlElement, String)` | 相对元素获取文本值 |
| `XmlPath.getString(XmlDocument, String, String)` | 获取值，带默认值 |
| `XmlPath.getOptional(XmlDocument, String)` | 获取为 Optional |
| `XmlPath.getInt(XmlDocument, String, int)` | 获取为 int，带默认值 |
| `XmlPath.getBoolean(XmlDocument, String, boolean)` | 获取为 boolean，带默认值 |
| `XmlPath.getAttribute(XmlDocument, String)` | 通过 `@attr` 语法获取属性 |
| `XmlPath.getElement(XmlDocument, String)` | 获取路径处的元素 |
| `XmlPath.getElements(XmlDocument, String)` | 获取所有匹配元素 |
| `XmlPath.getStrings(XmlDocument, String)` | 获取匹配元素的文本值列表 |
| `XmlPath.exists(XmlDocument, String)` | 检查路径是否存在 |
| `XmlPath.set(XmlDocument, String, String)` | 设置值，自动创建中间元素 |

**路径语法**: `"root.child.name"`、`"root.items[2].name"`、`"root.child.@attr"`

### XmlSplitter (`cloud.opencode.base.xml.splitter`)

| 方法 | 说明 |
|------|------|
| `XmlSplitter.split(InputStream, String, Consumer)` | 拆分流，每个片段回调 |
| `XmlSplitter.split(Path, String, Consumer)` | 拆分文件 |
| `XmlSplitter.split(String, String, Consumer)` | 拆分字符串 |
| `XmlSplitter.splitIndexed(InputStream, String, Consumer)` | 带索引拆分，使用 `SplitResult` |
| `XmlSplitter.splitAll(String, String)` | 收集所有片段为列表 |
| `XmlSplitter.count(InputStream, String)` | 计数匹配元素（O(1) 内存） |
| `XmlSplitter.count(String, String)` | 从字符串计数 |

### XmlCanonicalizer (`cloud.opencode.base.xml.canonical`)

| 方法 | 说明 |
|------|------|
| `XmlCanonicalizer.canonicalize(String)` | 规范化 XML 字符串（保留注释） |
| `XmlCanonicalizer.canonicalize(XmlDocument)` | 规范化文档 |
| `XmlCanonicalizer.canonicalize(String, boolean)` | 规范化，可选移除注释 |
| `XmlCanonicalizer.canonicalize(XmlDocument, boolean)` | 规范化文档，可选移除注释 |

## 类参考

### 根包 (`cloud.opencode.base.xml`)
| 类 | 说明 |
|----|------|
| `OpenXml` | 主门面：解析、构建、XPath、验证、转换、绑定、比较、合并、拆分、路径、规范化 |
| `XmlDocument` | 解析后的 XML 文档封装，提供查询和操作方法 |
| `XmlElement` | XML 元素封装，提供属性和子元素访问 |
| `XmlNode` | XML 节点基础抽象 |

### 绑定 (`xml.bind`)
| 类 | 说明 |
|----|------|
| `XmlBinder` | XML 与 Java 对象编组/解组引擎（支持 Record） |
| `@XmlAttribute` | 将字段/Record 组件映射为 XML 属性 |
| `@XmlElement` | 将字段/Record 组件映射为 XML 元素 |
| `@XmlElementList` | 将字段/Record 组件映射为 XML 元素列表 |
| `@XmlIgnore` | 在 XML 绑定中排除字段/Record 组件 |
| `@XmlRoot` | 指定类的根元素名称 |
| `@XmlValue` | 将字段/Record 组件映射为元素文本内容 |

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
| `OpenXmlException` | XML 操作基础异常（继承 `OpenException`） |
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
