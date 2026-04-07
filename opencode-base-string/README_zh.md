# OpenCode Base String

**适用于 Java 25+ 的综合字符串处理库**

`opencode-base-string` 是一个功能丰富的字符串操作库，提供命名转换、模板引擎、相似度算法、模糊匹配、数据脱敏、正则表达式工具等功能。

## 功能特性

### 核心功能
- **字符串操作**：填充、截断、大小写转换、反转、打乱、包围/拆包
- **空值安全检查**：`isBlank()`、`isEmpty()`、`isNotBlank()`、`isNotEmpty()`（V1.0.3）
- **批量匹配**：`containsAny()`、`startsWithAny()`、`endsWithAny()`、忽略大小写变体（V1.0.3）
- **单趟多模式替换**：`replaceEach()` — 一次扫描，不递归替换（V1.0.3）
- **SLF4J 风格格式化**：`format("{} has {} items", name, count)`（V1.0.3）
- **命名转换**：camelCase、PascalCase、snake_case、kebab-case、SCREAMING_SNAKE_CASE
- **模板引擎**：变量插值、if/for/include 节点、自定义过滤器
- **相似度算法**：Levenshtein 距离（+有界计算）、Jaccard、余弦、Jaro-Winkler
- **模糊匹配**：Aho-Corasick 多模式匹配、带评分的模糊搜索

### 高级功能
- **字素簇操作**：emoji安全的长度、子串、反转、显示宽度计算（V1.0.3）
- **URL 别名生成**：NFD标准化、去除变音符号、可配置分隔符（V1.0.3）
- **数据脱敏**：注解驱动的手机号、邮箱、身份证、银行卡等掩码处理
- **Jackson 集成**：自定义序列化器实现 JSON 透明脱敏
- **正则工具**：预编译模式、常用验证（邮箱、手机号、IP、URL）
- **字符串差异比较**：逐行 diff 比较，统一 diff 输出
- **转义工具**：HTML、Java、SQL 转义/反转义
- **Unicode 支持**：中文检测、全角/半角转换、中文分词
- **格式化工具**：时间段、文件大小、数字格式化
- **CSV/分词器**：CSV 解析和可配置的字符串分词
- **编解码**：键值对字符串编码/解码

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-string</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.string.*;
import cloud.opencode.base.string.similarity.LevenshteinDistance;
import cloud.opencode.base.string.unicode.OpenGrapheme;

// 填充和截断
String padded = OpenString.padLeft("42", 5, '0');     // "00042"
String truncated = OpenString.truncate("Hello World", 8); // "Hello..."

// 大小写转换
String title = OpenString.toTitleCase("hello world");  // "Hello World"
String swapped = OpenString.swapCase("Hello");         // "hELLO"

// 命名转换
String camel = OpenNaming.toCamelCase("user_name");    // "userName"
String snake = OpenNaming.toSnakeCase("userName");     // "user_name"

// 模板引擎
String result = OpenTemplate.render("Hello, ${name}!", Map.of("name", "World"));

// SLF4J 风格格式化 (V1.0.3)
String msg = OpenString.format("{} has {} items", "Alice", 3); // "Alice has 3 items"

// 空值安全检查 (V1.0.3)
boolean blank = OpenString.isBlank(null);    // true
boolean has = OpenString.containsAny("abc", "x", "b"); // true

// 单趟多模式替换 (V1.0.3)
String replaced = OpenString.replaceEach("aabbcc",
    new String[]{"aa", "bb"}, new String[]{"11", "22"}); // "1122cc"

// 字素簇操作 (V1.0.3)
int len = OpenGrapheme.length("a👨‍👩‍👧‍👦b");    // 3（而非11）
String rev = OpenGrapheme.reverse("a👨‍👩‍👧‍👦b"); // "b👨‍👩‍👧‍👦a"
int width = OpenGrapheme.displayWidth("Hi你好");  // 6

// 分割与连接 (V1.0.3)
List<String> parts = OpenString.split("a,b,c", ",");        // ["a", "b", "c"]
String joined = OpenString.joinSkipBlanks(", ", "a", "", "b"); // "a, b"

// 简写 (V1.0.3)
String abbr = OpenString.abbreviate("Hello World Test String", 15); // "Hello World..."

// URL 别名生成 (V1.0.3)
String slug = OpenSlug.toSlug("Hello World!");    // "hello-world"

// 相似度计算
double score = OpenSimilarity.levenshtein("kitten", "sitting");

// 有界 Levenshtein 距离 (V1.0.3) — 超过阈值提前退出
int dist = LevenshteinDistance.boundedDistance("kitten", "sitting", 5); // 3
int far  = LevenshteinDistance.boundedDistance("abc", "xyz", 1);       // -1（超出阈值）

// 数据脱敏
String masked = OpenMask.maskPhone("13812345678");     // "138****5678"
String email = OpenMask.maskEmail("test@example.com"); // "t***t@example.com"
```

## 类参考

### 根包 (`cloud.opencode.base.string`)
| 类 | 说明 |
|----|------|
| `OpenString` | 核心字符串操作门面：填充、截断、大小写转换、提取、清理、验证、空值安全检查、批量匹配、replaceEach、SLF4J格式化、split/join |
| `OpenNaming` | 命名约定转换：camelCase、snake_case、kebab-case、PascalCase |
| `OpenSlug` | URL友好别名生成，支持去除变音符号和自定义分隔符 |
| `OpenTemplate` | 简单模板渲染，支持变量替换 |

### 缩写 (`string.abbr`)
| 类 | 说明 |
|----|------|
| `OpenAbbreviation` | 字符串缩写和缩短工具 |

### 构建器 (`string.builder`)
| 类 | 说明 |
|----|------|
| `CharMatcher` | 字符匹配谓词，用于过滤和测试 |
| `Joiner` | 可配置的字符串连接器，支持分隔符、前缀、后缀 |
| `Splitter` | 可配置的字符串分割器，支持修剪、空值过滤 |

### 编解码 (`string.codec`)
| 类 | 说明 |
|----|------|
| `KeyValueCodec` | 键值对与字符串之间的编码/解码 |

### 脱敏 (`string.desensitize`)
| 类 | 说明 |
|----|------|
| `DesensitizeProcessor` | 应用脱敏规则的核心处理器 |
| `OpenMask` | 常用掩码操作门面（手机号、邮箱、身份证等） |
| `@Desensitize` | 字段级注解，指定脱敏类型 |
| `@DesensitizeBean` | 类级注解，启用 Bean 脱敏 |
| `DesensitizeException` | 脱敏错误异常 |
| `CollectionHandler` | 集合字段脱敏处理器 |
| `NumberHandler` | 数字字段脱敏处理器 |
| `StringHandler` | 字符串字段脱敏处理器 |
| `DesensitizeModule` | Jackson 模块，实现透明 JSON 脱敏 |
| `DesensitizeSerializer` | 脱敏字段的 Jackson 序列化器 |
| `DesensitizeStrategy` | 自定义脱敏策略接口 |
| `DesensitizeType` | 内置脱敏类型枚举 |
| `MaskPattern` | 可配置的掩码模式定义 |
| `StrategyRegistry` | 脱敏策略注册表 |

### 差异比较 (`string.diff`)
| 类 | 说明 |
|----|------|
| `DiffLine` | 表示单行差异及其变更类型 |
| `DiffResult` | 差异比较结果，包含所有差异行 |
| `OpenDiff` | 字符串差异操作门面 |

### 转义 (`string.escape`)
| 类 | 说明 |
|----|------|
| `HtmlUtil` | HTML 转义和反转义工具 |
| `JavaUtil` | Java 字符串转义和反转义工具 |
| `OpenEscape` | 统一转义门面（HTML、Java、SQL） |
| `SqlUtil` | SQL 转义工具，防止注入 |

### 异常 (`string.exception`)
| 类 | 说明 |
|----|------|
| `OpenStringException` | 字符串模块基础异常 |

### 格式化 (`string.format`)
| 类 | 说明 |
|----|------|
| `OpenDuration` | 人类可读的时间段格式化 |
| `OpenFileSize` | 人类可读的文件大小格式化（KB、MB、GB） |
| `OpenFormat` | 通用字符串格式化工具 |
| `OpenNumberFormat` | 支持区域设置的数字格式化 |

### 匹配 (`string.match`)
| 类 | 说明 |
|----|------|
| `AhoCorasick` | Aho-Corasick 算法，多模式字符串匹配 |
| `FuzzyMatch` | 模糊匹配结果记录 |
| `FuzzyMatcher` | 可配置的模糊字符串匹配器 |
| `OpenFuzzyMatch` | 模糊匹配操作门面 |
| `PatternMatch` | 基于模式的字符串匹配工具 |

### 命名 (`string.naming`)
| 类 | 说明 |
|----|------|
| `CaseUtil` | 底层大小写转换工具 |
| `NamingCase` | 命名约定枚举（CAMEL、SNAKE、KEBAB 等） |
| `WordUtil` | 单词级字符串操作（按约定分割、连接） |

### 解析 (`string.parse`)
| 类 | 说明 |
|----|------|
| `CsvUtil` | CSV 解析和生成工具 |
| `OpenParse` | 字符串解析操作门面 |
| `TokenizerUtil` | 可配置的字符串分词器 |
| `NamedParameterParser` | SQL 风格模板的命名参数解析 |

### 正则 (`string.regex`)
| 类 | 说明 |
|----|------|
| `MatcherUtil` | 正则匹配辅助工具 |
| `OpenRegex` | 正则操作门面 |
| `OpenVerify` | 常用验证模式（邮箱、手机号、IP、URL 等） |
| `RegexPattern` | 预编译的常用正则模式 |
| `RegexUtil` | 正则编译和匹配工具 |

### 相似度 (`string.similarity`)
| 类 | 说明 |
|----|------|
| `CosineSimilarity` | 余弦相似度算法 |
| `JaccardSimilarity` | Jaccard 相似度系数 |
| `LevenshteinDistance` | Levenshtein 编辑距离算法 |
| `OpenSimilarity` | 字符串相似度算法门面 |

### 模板 (`string.template`)
| 类 | 说明 |
|----|------|
| `ContextBuilder` | 模板上下文变量构建器 |
| `PlaceholderTemplate` | 简单占位符模板 |
| `StringTemplate` | 带表达式的高级字符串模板 |
| `Template` | 模板接口 |
| `TemplateContext` | 模板执行上下文 |
| `TemplateEngine` | 全功能模板引擎，支持解析和渲染 |
| `TemplateFilter` | 模板输出过滤器接口 |
| `TemplateUtil` | 模板工具方法 |
| `ForNode` | 模板 AST 节点（for 循环） |
| `IfNode` | 模板 AST 节点（条件判断） |
| `IncludeNode` | 模板 AST 节点（包含） |
| `TemplateNode` | 模板 AST 基础节点 |
| `TextNode` | 模板 AST 节点（静态文本） |
| `VariableNode` | 模板 AST 节点（变量插值） |

### 文本 (`string.text`)
| 类 | 说明 |
|----|------|
| `OpenHighlight` | 文本高亮，标记搜索词 |
| `OpenText` | 文本处理工具 |
| `OpenTruncate` | 高级文本截断策略 |
| `OpenWrap` | 文本换行和断行工具 |

### Unicode (`string.unicode`)
| 类 | 说明 |
|----|------|
| `ChineseSegmenter` | 基础中文分词 |
| `OpenChinese` | 中文字符检测和转换 |
| `OpenFullWidth` | 全角/半角字符转换 |
| `OpenUnicode` | Unicode 字符工具 |
| `OpenGrapheme` | 字素簇操作：emoji安全的长度、子串、反转、显示宽度 |

## 环境要求

- Java 25+
- 核心功能无外部依赖
- 可选：`com.fasterxml.jackson.core:jackson-databind`（用于 Jackson 脱敏集成）

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
