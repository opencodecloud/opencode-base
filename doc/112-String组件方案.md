# String 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-string` 是一个功能丰富的字符串处理组件，在 Core 组件基础字符串工具之上，提供更高级的字符串操作功能，包括命名转换、模板渲染、文本比较、正则表达式、Unicode 处理、模糊匹配、多模式匹配、数据脱敏等。

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                         应用层                                   │
│        (日志脱敏 / 配置模板 / 数据校验 / 文本处理)                │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│  字符串增强      │   │  模板引擎       │   │  数据脱敏       │
│                 │   │                 │   │                 │
│ OpenString      │   │ OpenTemplate    │   │ OpenMask        │
│ OpenNaming      │   │ Template        │   │ DesensitizeProc │
│ Joiner/Splitter │   │ StringTemplate  │   │                 │
└─────────────────┘   └─────────────────┘   └─────────────────┘
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       功能层 (Features)                          │
│                                                                 │
│  naming/           similarity/         regex/         unicode/  │
│  ├─ NamingCase     ├─ OpenSimilarity   ├─ OpenRegex   ├─ OpenUnicode│
│  └─ ...            ├─ Levenshtein     └─ ...          ├─ OpenFullWidth│
│                    ├─ Jaccard                         └─ OpenChinese│
│                    └─ Cosine                                    │
│                                                                 │
│  format/           text/              diff/           escape/   │
│  ├─ OpenFormat     ├─ OpenText        ├─ OpenDiff     ├─ OpenEscape│
│  │                 │                  ├─ DiffResult   │         │
│  │                 │                  └─ DiffLine     │         │
│                                                                 │
│  match/            parse/             abbr/                     │
│  ├─ OpenFuzzyMatch ├─ OpenParse       └─ OpenAbbreviation       │
│  ├─ AhoCorasick    └─ CsvUtil                                   │
│  ├─ FuzzyMatcher                                                │
│  ├─ FuzzyMatch                                                  │
│  └─ PatternMatch                                                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Guava 风格构建器                           │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────────────┐  │
│  │    Joiner     │  │   Splitter    │  │    CharMatcher      │  │
│  │ (字符串连接)  │  │ (字符串分割)  │  │   (字符匹配)        │  │
│  └───────────────┘  └───────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

设计特点:
├── 多功能：命名转换/模板/脱敏/相似度/正则/模糊匹配
├── Guava 风格：Joiner/Splitter/CharMatcher
├── 模板引擎：变量替换/占位符/编译模板/过滤器
├── 脱敏集成：OpenMask 数据脱敏
├── 模糊匹配：搜索建议 + 拼写纠正
├── 多模式匹配：Aho-Corasick 敏感词过滤
└── 零依赖：纯 JDK 实现
```

### 1.3 与其他字符串库对比

| 特性 | opencode-string | Apache Commons | Guava | Hutool |
|------|----------------|----------------|-------|--------|
| **命名转换** | NamingCase | 需手动 | CaseFormat | StrUtil |
| **模板引擎** | 内置完整 | 不支持 | 不支持 | 简单模板 |
| **数据脱敏** | OpenMask | 不支持 | 不支持 | DesensitizedUtil |
| **Joiner/Splitter** | Guava 风格 | StringUtils | 原生 | StrUtil |
| **CharMatcher** | Guava 风格 | 不支持 | 原生 | 不支持 |
| **相似度** | 多算法 | Levenshtein | 不支持 | 部分 |
| **模糊匹配** | FuzzyMatcher | 不支持 | 不支持 | 不支持 |
| **多模式匹配** | AhoCorasick | 不支持 | 不支持 | 简单实现 |
| **差异对比** | DiffResult | 不支持 | 不支持 | 不支持 |
| **正则构建** | OpenRegex | 不支持 | 不支持 | ReUtil |

## 2. 包结构

```
cloud.opencode.base.string
├── OpenString.java                    // 字符串增强工具（主门面）
├── OpenNaming.java                    // 命名转换门面
├── OpenTemplate.java                  // 模板渲染门面
├── builder/
│   ├── Joiner.java                    // Guava 风格字符串连接器
│   ├── Splitter.java                  // Guava 风格字符串分割器
│   └── CharMatcher.java              // Guava 风格字符匹配器
├── naming/
│   └── NamingCase.java                // 命名约定枚举
├── template/
│   ├── StringTemplate.java            // 简单字符串模板
│   ├── Template.java                  // 编译模板（基于节点）
│   ├── PlaceholderTemplate.java       // 占位符模板
│   ├── TemplateFilter.java            // 模板过滤器接口
│   ├── TemplateUtil.java              // 模板工具
│   ├── TemplateContext.java           // 模板上下文
│   └── node/                          // 模板节点（TextNode, VariableNode等）
├── similarity/
│   ├── OpenSimilarity.java            // 相似度计算门面
│   ├── LevenshteinDistance.java       // 编辑距离
│   ├── JaccardSimilarity.java         // Jaccard 相似度
│   └── CosineSimilarity.java          // 余弦相似度
├── diff/
│   ├── OpenDiff.java                  // 差异对比门面
│   ├── DiffResult.java                // 差异结果（record）
│   └── DiffLine.java                  // 差异行（record）
├── match/
│   ├── OpenFuzzyMatch.java            // 模糊匹配门面
│   ├── AhoCorasick.java               // Aho-Corasick 多模式匹配
│   ├── FuzzyMatcher.java              // 模糊匹配器（Builder）
│   ├── FuzzyMatch.java                // 模糊匹配结果（record）
│   └── PatternMatch.java             // 模式匹配结果（record）
├── desensitize/
│   ├── OpenMask.java                  // 数据脱敏工具
│   └── DesensitizeProcessor.java      // 脱敏处理器
├── escape/
│   └── OpenEscape.java                // 转义/反转义门面
├── regex/
│   └── OpenRegex.java                 // 正则表达式门面
├── format/
│   └── OpenFormat.java                // 格式化门面
├── text/
│   └── OpenText.java                  // 文本处理工具
├── unicode/
│   ├── OpenUnicode.java               // Unicode 工具门面
│   ├── OpenFullWidth.java             // 全角/半角转换
│   └── OpenChinese.java               // 繁体/简体转换
├── parse/
│   ├── OpenParse.java                 // 解析门面
│   ├── TokenizerUtil.java             // 分词工具
│   └── CsvUtil.java                   // CSV 解析工具
└── abbr/
    └── OpenAbbreviation.java          // 缩写工具
```

## 3. 核心 API

### 3.1 OpenString - 字符串增强工具

主门面类，提供丰富的字符串操作方法。

#### 填充与对齐

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `padLeft(String, int, char)` | 字符串, 长度, 填充字符 | `String` | 左填充到指定长度 |
| `padRight(String, int, char)` | 字符串, 长度, 填充字符 | `String` | 右填充到指定长度 |
| `center(String, int, char)` | 字符串, 长度, 填充字符 | `String` | 居中对齐 |

#### 截取与提取

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `left(String, int)` | 字符串, 长度 | `String` | 取左侧 n 个字符 |
| `right(String, int)` | 字符串, 长度 | `String` | 取右侧 n 个字符 |
| `mid(String, int, int)` | 字符串, 起始, 长度 | `String` | 取中间部分 |
| `truncate(String, int)` | 字符串, 最大长度 | `String` | 截断并加省略号 |
| `truncateMiddle(String, int)` | 字符串, 最大长度 | `String` | 中间截断 |
| `truncateByBytes(String, int, Charset)` | 字符串, 字节数, 编码 | `String` | 按字节截断 |
| `substringBefore(String, String)` | 字符串, 分隔符 | `String` | 第一个分隔符之前 |
| `substringAfter(String, String)` | 字符串, 分隔符 | `String` | 第一个分隔符之后 |
| `substringBeforeLast(String, String)` | 字符串, 分隔符 | `String` | 最后一个分隔符之前 |
| `substringAfterLast(String, String)` | 字符串, 分隔符 | `String` | 最后一个分隔符之后 |
| `substringBetween(String, String, String)` | 字符串, 开始, 结束 | `String` | 两个标记之间 |
| `substringsBetween(String, String, String)` | 字符串, 开始, 结束 | `List<String>` | 两个标记之间的所有匹配 |

#### 大小写转换

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `capitalize(String)` | 字符串 | `String` | 首字母大写 |
| `uncapitalize(String)` | 字符串 | `String` | 首字母小写 |
| `swapCase(String)` | 字符串 | `String` | 大小写互换 |
| `toTitleCase(String)` | 字符串 | `String` | 标题大小写 |

#### 字符串变换

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `reverse(String)` | 字符串 | `String` | 反转 |
| `shuffle(String)` | 字符串 | `String` | 随机打乱 |
| `rotate(String, int)` | 字符串, 偏移量 | `String` | 旋转 |
| `wrap(String, String)` | 字符串, 包裹符 | `String` | 包裹字符串 |
| `unwrap(String, String)` | 字符串, 包裹符 | `String` | 去掉包裹 |
| `chomp(String)` | 字符串 | `String` | 去尾部换行 |
| `chop(String)` | 字符串 | `String` | 去尾部字符 |
| `stripAccents(String)` | 字符串 | `String` | 去重音符号 |

#### 统计与判断

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `countMatches(String, String)` | 字符串, 子串 | `int` | 子串出现次数 |
| `charFrequency(String)` | 字符串 | `Map<Character,Integer>` | 字符频率 |
| `wordFrequency(String)` | 字符串 | `Map<String,Integer>` | 单词频率 |
| `isNumeric(String)` | 字符串 | `boolean` | 是否纯数字 |
| `isAlpha(String)` | 字符串 | `boolean` | 是否纯字母 |
| `isAlphanumeric(String)` | 字符串 | `boolean` | 是否字母或数字 |
| `isAscii(String)` | 字符串 | `boolean` | 是否纯 ASCII |
| `containsChinese(String)` | 字符串 | `boolean` | 是否包含中文 |
| `isAllChinese(String)` | 字符串 | `boolean` | 是否全中文 |
| `isAllLowerCase(String)` | 字符串 | `boolean` | 是否全小写 |
| `isAllUpperCase(String)` | 字符串 | `boolean` | 是否全大写 |
| `isMixedCase(String)` | 字符串 | `boolean` | 是否混合大小写 |
| `isPalindrome(String)` | 字符串 | `boolean` | 是否回文 |
| `isPalindromeIgnoreCase(String)` | 字符串 | `boolean` | 忽略大小写回文 |
| `isRepeated(String)` | 字符串 | `boolean` | 是否重复模式 |
| `getRepeatedPattern(String)` | 字符串 | `String` | 获取重复模式 |

#### 清洗与保留

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `removeWhitespace(String)` | 字符串 | `String` | 移除空白 |
| `normalizeWhitespace(String)` | 字符串 | `String` | 规范化空白 |
| `removeInvisibleChars(String)` | 字符串 | `String` | 移除不可见字符 |
| `removeSpecialChars(String)` | 字符串 | `String` | 移除特殊字符 |
| `keepDigits(String)` | 字符串 | `String` | 仅保留数字 |
| `keepLetters(String)` | 字符串 | `String` | 仅保留字母 |
| `keepAlphanumeric(String)` | 字符串 | `String` | 仅保留字母数字 |
| `keepChinese(String)` | 字符串 | `String` | 仅保留中文 |
| `getDigits(String)` | 字符串 | `String` | 提取数字 |

#### 前后缀与查找

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `ensurePrefix(String, String)` | 字符串, 前缀 | `String` | 确保有前缀 |
| `ensureSuffix(String, String)` | 字符串, 后缀 | `String` | 确保有后缀 |
| `ensureWrap(String, String)` | 字符串, 包裹符 | `String` | 确保被包裹 |
| `removePrefix(String, String)` | 字符串, 前缀 | `String` | 移除前缀 |
| `removeSuffix(String, String)` | 字符串, 后缀 | `String` | 移除后缀 |
| `commonPrefix(String, String)` | 两个字符串 | `String` | 公共前缀 |
| `commonSuffix(String, String)` | 两个字符串 | `String` | 公共后缀 |
| `findAll(String, String)` | 字符串, 子串 | `List<Integer>` | 查找所有位置 |
| `indexOfNth(String, String, int)` | 字符串, 子串, 第N次 | `int` | 第 N 次出现位置 |
| `lastIndexOfNth(String, String, int)` | 字符串, 子串, 第N次 | `int` | 倒数第 N 次位置 |
| `difference(String, String)` | 两个字符串 | `String` | 差异部分 |
| `indexOfDifference(String, String)` | 两个字符串 | `int` | 首个差异位置 |

#### 默认值

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `defaultIfBlank(String, String)` | 字符串, 默认值 | `String` | 空白时返回默认值 |
| `defaultIfEmpty(String, String)` | 字符串, 默认值 | `String` | 空时返回默认值 |
| `firstNonBlank(String...)` | 多个字符串 | `String` | 第一个非空白 |
| `firstNonEmpty(String...)` | 多个字符串 | `String` | 第一个非空 |

**使用示例：**

```java
// 填充
OpenString.padLeft("42", 5, '0');         // "00042"
OpenString.center("Hi", 10, '-');         // "----Hi----"

// 截取
OpenString.substringBetween("<a>hello</a>", "<a>", "</a>"); // "hello"
OpenString.truncateMiddle("HelloWorld", 8); // "He...ld"

// 判断
OpenString.isPalindrome("racecar");       // true
OpenString.containsChinese("Hello你好");   // true

// 清洗
OpenString.keepDigits("abc123def456");     // "123456"
OpenString.normalizeWhitespace(" a  b ");  // "a b"
```

### 3.2 OpenNaming - 命名转换

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `toCamelCase(String)` | 字符串 | `String` | 转驼峰：`user_name` -> `userName` |
| `toPascalCase(String)` | 字符串 | `String` | 转帕斯卡：`user_name` -> `UserName` |
| `toSnakeCase(String)` | 字符串 | `String` | 转蛇形：`userName` -> `user_name` |
| `toUpperSnakeCase(String)` | 字符串 | `String` | 转大写蛇形：`userName` -> `USER_NAME` |
| `toKebabCase(String)` | 字符串 | `String` | 转短横线：`userName` -> `user-name` |
| `toDotCase(String)` | 字符串 | `String` | 转点号分隔：`userName` -> `user.name` |
| `toPathCase(String)` | 字符串 | `String` | 转路径：`userName` -> `user/name` |
| `toTitleCase(String)` | 字符串 | `String` | 转标题：`user_name` -> `User Name` |
| `toSentenceCase(String)` | 字符串 | `String` | 转句子：`user_name` -> `User name` |
| `convert(String, NamingCase, NamingCase)` | 字符串, 源, 目标 | `String` | 指定转换 |
| `detect(String)` | 字符串 | `NamingCase` | 检测命名风格 |
| `splitWords(String)` | 字符串 | `List<String>` | 拆分为单词列表 |
| `joinWords(List<String>, NamingCase)` | 单词列表, 目标风格 | `String` | 按风格连接 |
| `tableToClass(String)` | 表名 | `String` | 表名转类名 |
| `columnToField(String)` | 列名 | `String` | 列名转字段名 |
| `classToTable(String)` | 类名 | `String` | 类名转表名 |
| `fieldToColumn(String)` | 字段名 | `String` | 字段名转列名 |
| `classToPath(String)` | 类名 | `String` | 类名转路径 |

**使用示例：**

```java
OpenNaming.toCamelCase("user_name");       // "userName"
OpenNaming.toSnakeCase("userName");        // "user_name"
OpenNaming.tableToClass("sys_user_role");  // "SysUserRole"
OpenNaming.detect("userName");             // NamingCase.CAMEL_CASE
```

### 3.3 NamingCase - 命名约定枚举

| 枚举值 | 说明 | 示例 |
|--------|------|------|
| `CAMEL_CASE` | 驼峰 | `userName` |
| `PASCAL_CASE` | 帕斯卡 | `UserName` |
| `SNAKE_CASE` | 蛇形 | `user_name` |
| `UPPER_SNAKE_CASE` | 大写蛇形 | `USER_NAME` |
| `KEBAB_CASE` | 短横线 | `user-name` |
| `DOT_CASE` | 点号 | `user.name` |
| `PATH_CASE` | 路径 | `user/name` |
| `TITLE_CASE` | 标题 | `User Name` |
| `SENTENCE_CASE` | 句子 | `User name` |

每个枚举值提供方法：`getSeparator()`, `isCapitalized()`, `isCapitalizeWords()`, `isUpperCase()`, `hasSeparator()`。

### 3.4 OpenTemplate - 模板渲染

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `render(String, Map<String,Object>)` | 模板, 变量 | `String` | 渲染模板 |
| `render(String, Map<String,Object>, String)` | 模板, 变量, 默认值 | `String` | 渲染模板（带默认值） |
| `format(String, Map<String,Object>)` | 模板, 变量 | `String` | 格式化（同 render） |
| `format(String, Object...)` | 模板, 参数列表 | `String` | 索引占位符格式化 |
| `of(String)` | 模板字符串 | `StringTemplate` | 创建 StringTemplate |
| `placeholder(String, String, String)` | 模板, 前缀, 后缀 | `PlaceholderTemplate` | 自定义占位符模板 |
| `compile(String)` | 模板字符串 | `Template` | 编译模板 |
| `renderInline(String, Map<String,Object>)` | 模板, 上下文 | `String` | 内联编译并渲染 |
| `registerFilter(String, TemplateFilter)` | 名称, 过滤器 | `void` | 注册全局过滤器 |
| `getFilter(String)` | 名称 | `TemplateFilter` | 获取过滤器 |
| `register(String, String)` | 名称, 模板 | `void` | 注册命名模板 |
| `renderNamed(String, Map<String,Object>)` | 模板名, 上下文 | `String` | 渲染命名模板 |
| `clearCache()` | 无 | `void` | 清空模板缓存 |

**内置过滤器**：upper, lower, capitalize, truncate, default, escape

**使用示例：**

```java
// 基本渲染
Map<String, Object> vars = Map.of("name", "Alice", "age", 30);
OpenTemplate.render("Hello ${name}, age=${age}", vars);  // "Hello Alice, age=30"

// 索引占位符
OpenTemplate.format("Hello {0}, age={1}", "Alice", 30);  // "Hello Alice, age=30"

// StringTemplate 链式
OpenTemplate.of("Hello ${name}")
    .set("name", "Bob")
    .render();  // "Hello Bob"

// 自定义占位符
OpenTemplate.placeholder("Hello #name#", "#", "#")
    .render(Map.of("name", "Charlie"));  // "Hello Charlie"
```

### 3.5 StringTemplate - 字符串模板

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `of(String)` | 模板字符串 | `StringTemplate` | 创建模板 |
| `set(String, Object)` | 变量名, 值 | `StringTemplate` | 设置变量 |
| `setAll(Map<String,Object>)` | 变量映射 | `StringTemplate` | 批量设置变量 |
| `defaultValue(String)` | 默认值 | `StringTemplate` | 设置默认值 |
| `strict(boolean)` | 是否严格 | `StringTemplate` | 严格模式 |
| `render()` | 无 | `String` | 渲染 |
| `render(Map<String,Object>)` | 附加变量 | `String` | 渲染（合并变量） |
| `getVariables()` | 无 | `Set<String>` | 获取模板变量名 |
| `hasVariable(String)` | 变量名 | `boolean` | 是否包含变量 |

### 3.6 Template - 编译模板

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `compile(String)` | 模板字符串 | `Template` | 编译模板 |
| `render(Map<String,Object>)` | 上下文 | `String` | 渲染 |
| `render(TemplateContext)` | 模板上下文 | `String` | 渲染 |
| `getSource()` | 无 | `String` | 获取源模板 |

### 3.7 PlaceholderTemplate - 占位符模板

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `of(String, String, String)` | 模板, 前缀, 后缀 | `PlaceholderTemplate` | 创建 |
| `render(Map<String,Object>)` | 变量 | `String` | 渲染 |

### 3.8 TemplateFilter - 模板过滤器接口

```java
@FunctionalInterface
public interface TemplateFilter {
    String apply(String value, String[] args);
}
```

### 3.9 Joiner - 字符串连接器

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `on(String)` | 分隔符 | `Joiner` | 创建连接器 |
| `on(char)` | 分隔符 | `Joiner` | 创建连接器 |
| `skipNulls()` | 无 | `Joiner` | 跳过 null |
| `useForNull(String)` | 替代文本 | `Joiner` | null 替代 |
| `join(Iterable<?>)` | 可迭代对象 | `String` | 连接 |
| `join(Iterator<?>)` | 迭代器 | `String` | 连接 |
| `join(Object[])` | 数组 | `String` | 连接 |
| `join(Object, Object, Object...)` | 多个对象 | `String` | 连接 |
| `appendTo(StringBuilder, Iterable<?>)` | 目标, 可迭代对象 | `StringBuilder` | 追加到 |
| `withKeyValueSeparator(String)` | 键值分隔符 | `MapJoiner` | 转为 Map 连接器 |

**使用示例：**

```java
Joiner.on(", ").skipNulls().join(List.of("a", null, "b"));  // "a, b"
Joiner.on("&").withKeyValueSeparator("=").join(Map.of("k", "v"));  // "k=v"
```

### 3.10 Splitter - 字符串分割器

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `on(char)` | 分隔符 | `Splitter` | 创建分割器 |
| `on(String)` | 分隔符 | `Splitter` | 创建分割器 |
| `on(Pattern)` | 正则模式 | `Splitter` | 创建分割器 |
| `on(CharMatcher)` | 字符匹配器 | `Splitter` | 创建分割器 |
| `onPattern(String)` | 正则字符串 | `Splitter` | 创建分割器 |
| `fixedLength(int)` | 固定长度 | `Splitter` | 按固定长度分割 |
| `omitEmptyStrings()` | 无 | `Splitter` | 忽略空串 |
| `trimResults()` | 无 | `Splitter` | 修剪结果 |
| `limit(int)` | 最大数量 | `Splitter` | 限制分割数 |
| `split(CharSequence)` | 字符串 | `Iterable<String>` | 分割 |
| `splitToList(CharSequence)` | 字符串 | `List<String>` | 分割为列表 |
| `splitToStream(CharSequence)` | 字符串 | `Stream<String>` | 分割为流 |
| `withKeyValueSeparator(String)` | 键值分隔符 | `MapSplitter` | 转为 Map 分割器 |

**使用示例：**

```java
Splitter.on(',').trimResults().omitEmptyStrings()
    .splitToList("a, b, , c");  // ["a", "b", "c"]

Splitter.fixedLength(3).splitToList("abcdefgh");  // ["abc", "def", "gh"]
```

### 3.11 CharMatcher - 字符匹配器

#### 工厂方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `any()` | `CharMatcher` | 匹配任何字符 |
| `none()` | `CharMatcher` | 不匹配任何字符 |
| `whitespace()` | `CharMatcher` | 匹配空白字符 |
| `digit()` | `CharMatcher` | 匹配数字 |
| `ascii()` | `CharMatcher` | 匹配 ASCII |
| `javaLetter()` | `CharMatcher` | 匹配 Java 字母 |
| `is(char)` | `CharMatcher` | 匹配指定字符 |
| `isNot(char)` | `CharMatcher` | 不匹配指定字符 |
| `anyOf(CharSequence)` | `CharMatcher` | 匹配集合中的字符 |
| `noneOf(CharSequence)` | `CharMatcher` | 不匹配集合中的字符 |
| `inRange(char, char)` | `CharMatcher` | 匹配范围内字符 |
| `forPredicate(Predicate<Character>)` | `CharMatcher` | 自定义谓词 |

#### 组合操作

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `and(CharMatcher)` | `CharMatcher` | 逻辑 AND |
| `or(CharMatcher)` | `CharMatcher` | 逻辑 OR |
| `negate()` | `CharMatcher` | 取反 |

#### 查询操作

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `matchesAnyOf(CharSequence)` | 字符串 | `boolean` | 是否匹配任一字符 |
| `matchesAllOf(CharSequence)` | 字符串 | `boolean` | 是否匹配全部字符 |
| `matchesNoneOf(CharSequence)` | 字符串 | `boolean` | 是否不匹配任何字符 |
| `indexIn(CharSequence)` | 字符串 | `int` | 首次匹配位置 |
| `lastIndexIn(CharSequence)` | 字符串 | `int` | 最后匹配位置 |
| `countIn(CharSequence)` | 字符串 | `int` | 匹配字符数 |

#### 变换操作

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `removeFrom(CharSequence)` | 字符串 | `String` | 移除匹配字符 |
| `retainFrom(CharSequence)` | 字符串 | `String` | 保留匹配字符 |
| `replaceFrom(CharSequence, char)` | 字符串, 替换字符 | `String` | 替换匹配字符 |
| `collapseFrom(CharSequence, char)` | 字符串, 折叠字符 | `String` | 折叠连续匹配 |
| `trimFrom(CharSequence)` | 字符串 | `String` | 去两端匹配字符 |
| `trimLeadingFrom(CharSequence)` | 字符串 | `String` | 去前导匹配字符 |
| `trimTrailingFrom(CharSequence)` | 字符串 | `String` | 去尾部匹配字符 |
| `trimAndCollapseFrom(CharSequence, char)` | 字符串, 折叠字符 | `String` | 去两端并折叠 |

**使用示例：**

```java
CharMatcher.digit().retainFrom("abc123def456");     // "123456"
CharMatcher.whitespace().trimAndCollapseFrom("  a  b  ", ' ');  // "a b"
CharMatcher.anyOf("aeiou").removeFrom("hello world"); // "hll wrld"
```

### 3.12 OpenSimilarity - 相似度计算

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `levenshteinDistance(String, String)` | 两个字符串 | `int` | 编辑距离 |
| `levenshteinSimilarity(String, String)` | 两个字符串 | `double` | 编辑距离相似度 (0.0-1.0) |
| `jaccardSimilarity(String, String)` | 两个字符串 | `double` | Jaccard 相似度 |
| `cosineSimilarity(String, String)` | 两个字符串 | `double` | 余弦相似度 |
| `jaroWinklerSimilarity(String, String)` | 两个字符串 | `double` | Jaro-Winkler 相似度 |
| `longestCommonSubsequence(String, String)` | 两个字符串 | `String` | 最长公共子序列 |
| `longestCommonSubstring(String, String)` | 两个字符串 | `String` | 最长公共子串 |
| `isSimilar(String, String, double)` | 两个字符串, 阈值 | `boolean` | 是否相似 |
| `findMostSimilar(String, Collection<String>)` | 字符串, 候选集 | `String` | 最相似的 |
| `findSimilar(String, Collection<String>, double)` | 字符串, 候选集, 阈值 | `List<String>` | 所有相似的 |

### 3.13 OpenDiff - 差异对比

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `diff(String, String)` | 原文, 修改 | `DiffResult` | 行对比（默认） |
| `diffLines(String, String)` | 原文, 修改 | `DiffResult` | 按行对比 |
| `diffWords(String, String)` | 原文, 修改 | `DiffResult` | 按单词对比 |
| `diffChars(String, String)` | 原文, 修改 | `DiffResult` | 按字符对比 |
| `unifiedDiff(String, String)` | 原文, 修改 | `String` | 统一差异格式 |
| `unifiedDiff(String, String, String, String, int)` | 原文, 修改, 原名, 新名, 上下文行数 | `String` | 带文件名的统一差异 |
| `htmlDiff(String, String)` | 原文, 修改 | `String` | HTML 差异格式 |
| `applyPatch(String, String)` | 原文, 补丁 | `String` | 应用补丁 |

**DiffResult** (record)：`lines()`, `additions()`, `deletions()`, `modifications()`, `hasDiff()`, `toUnifiedDiff()`, `toHtml()`

**DiffLine** (record)：`type()`, `originalLine()`, `revisedLine()`, `content()`。类型：`EQUAL`, `INSERT`, `DELETE`, `MODIFY`

### 3.14 OpenFuzzyMatch - 模糊匹配门面

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `search(String, Collection<String>)` | 查询, 候选 | `List<String>` | 模糊搜索 |
| `search(String, Collection<String>, double)` | 查询, 候选, 阈值 | `List<String>` | 指定阈值搜索 |
| `suggest(String, Collection<String>)` | 查询, 候选 | `List<String>` | 搜索建议 |
| `findBest(String, Collection<String>)` | 查询, 候选 | `Optional<String>` | 最佳匹配 |
| `didYouMean(String, Collection<String>)` | 查询, 候选 | `Optional<String>` | "你是不是要找" |
| `similarity(String, String)` | 两个字符串 | `double` | 相似度 |
| `isSimilar(String, String, double)` | 两个字符串, 阈值 | `boolean` | 是否相似 |
| `rankBySimilarity(String, Collection<String>)` | 查询, 候选 | `List<String>` | 按相似度排名 |
| `rankWithScores(String, Collection<String>)` | 查询, 候选 | `List<FuzzyMatch<String>>` | 带分数排名 |

### 3.15 FuzzyMatcher - 模糊匹配器

Builder 模式构建，支持多种匹配算法。

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `builder()` | 无 | `Builder<String>` | 字符串匹配器构建器 |
| `builder(Function<T,String>)` | 键提取器 | `Builder<T>` | 自定义类型构建器 |
| `match(String)` | 查询 | `List<FuzzyMatch<T>>` | 查找匹配（按相似度降序） |
| `matchBest(String)` | 查询 | `Optional<FuzzyMatch<T>>` | 最佳匹配 |
| `suggest(String)` | 查询 | `List<T>` | 建议项目 |
| `suggestStrings(String)` | 查询 | `List<String>` | 建议字符串 |
| `hasMatch(String)` | 查询 | `boolean` | 是否有匹配 |
| `size()` | 无 | `int` | 项目数 |

**Builder 方法**：`add(T)`, `addAll(Collection)`, `threshold(double)`, `maxResults(int)`, `ignoreCase(boolean)`, `algorithm(MatchAlgorithm)`, `build()`

**MatchAlgorithm 枚举**：`LEVENSHTEIN`, `JARO_WINKLER`, `CONTAINS`, `PREFIX`, `COMBINED`

**FuzzyMatch<T>** (record)：`item()`, `key()`, `score()`, `scoreAsPercent()`, `isExactMatch()`, `isStrongMatch()`, `isWeakMatch()`

### 3.16 AhoCorasick - 多模式匹配

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `builder()` | 无 | `Builder` | 构建器 |
| `of(String...)` | 模式列表 | `AhoCorasick` | 创建匹配器 |
| `ofIgnoreCase(String...)` | 模式列表 | `AhoCorasick` | 忽略大小写 |
| `findAll(String)` | 文本 | `List<PatternMatch>` | 查找所有匹配 |
| `findFirst(String)` | 文本 | `Optional<PatternMatch>` | 查找首个匹配 |
| `containsAny(String)` | 文本 | `boolean` | 是否包含任一模式 |
| `countMatches(String)` | 文本 | `int` | 匹配次数 |
| `getMatchedPatterns(String)` | 文本 | `Set<String>` | 匹配到的模式集 |
| `replaceAll(String, String)` | 文本, 替换值 | `String` | 替换所有匹配 |
| `filter(String)` | 文本 | `String` | 过滤（用*替换） |
| `highlight(String, String, String)` | 文本, 前标记, 后标记 | `String` | 高亮匹配 |
| `patternCount()` | 无 | `int` | 模式数量 |
| `getPatterns()` | 无 | `Set<String>` | 所有模式 |
| `hasPattern(String)` | 模式 | `boolean` | 是否包含模式 |

**PatternMatch** (record)：`pattern()`, `start()`, `end()`, `matchedText()`, `length()`, `overlaps(PatternMatch)`, `contains(PatternMatch)`, `extractFrom(String)`, `toDisplayString()`

**使用示例：**

```java
AhoCorasick ac = AhoCorasick.of("java", "python", "go");
ac.findAll("I love java and python");  // [{java,7,11}, {python,16,22}]
ac.filter("bad word here");            // "*** **** here"
ac.highlight("java is great", "<b>", "</b>");  // "<b>java</b> is great"
```

### 3.17 OpenMask - 数据脱敏

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `mobile(String)` | 手机号 | `String` | 手机脱敏：`138****8888` |
| `maskMobile(String)` | 手机号 | `String` | 同 mobile |
| `idCard(String)` | 身份证 | `String` | 身份证脱敏 |
| `maskIdCard(String)` | 身份证 | `String` | 同 idCard |
| `email(String)` | 邮箱 | `String` | 邮箱脱敏 |
| `maskEmail(String)` | 邮箱 | `String` | 同 email |
| `bankCard(String)` | 银行卡 | `String` | 银行卡脱敏 |
| `maskBankCard(String)` | 银行卡 | `String` | 同 bankCard |
| `chineseName(String)` | 姓名 | `String` | 中文姓名脱敏 |
| `maskName(String)` | 姓名 | `String` | 同 chineseName |
| `maskAddress(String)` | 地址 | `String` | 地址脱敏 |
| `mask(String, int, int, char)` | 字符串, 前保留, 后保留, 掩码字符 | `String` | 自定义脱敏 |
| `maskMiddle(String, char)` | 字符串, 掩码字符 | `String` | 中间脱敏 |
| `maskByPattern(String, String, char)` | 字符串, 正则, 掩码字符 | `String` | 按正则脱敏 |
| `desensitize(String, int, int)` | 字符串, 前保留, 后保留 | `String` | 通用脱敏 |

### 3.18 OpenEscape - 转义/反转义

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `escapeHtml(String)` | 字符串 | `String` | HTML 转义 |
| `unescapeHtml(String)` | 字符串 | `String` | HTML 反转义 |
| `escapeXml(String)` | 字符串 | `String` | XML 转义 |
| `unescapeXml(String)` | 字符串 | `String` | XML 反转义 |
| `escapeJava(String)` | 字符串 | `String` | Java 转义 |
| `unescapeJava(String)` | 字符串 | `String` | Java 反转义 |
| `escapeJson(String)` | 字符串 | `String` | JSON 转义 |
| `unescapeJson(String)` | 字符串 | `String` | JSON 反转义 |
| `escapeSql(String)` | 字符串 | `String` | SQL 转义 |
| `encodeUrl(String)` | 字符串 | `String` | URL 编码 |
| `decodeUrl(String)` | 字符串 | `String` | URL 解码 |
| `escapeCsv(String)` | 字符串 | `String` | CSV 转义 |
| `unescapeCsv(String)` | 字符串 | `String` | CSV 反转义 |
| `escapeRegex(String)` | 字符串 | `String` | 正则转义 |
| `escapeShell(String)` | 字符串 | `String` | Shell 转义 |

### 3.19 OpenRegex - 正则表达式

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `matches(String, String)` | 文本, 正则 | `boolean` | 完全匹配 |
| `contains(String, String)` | 文本, 正则 | `boolean` | 包含匹配 |
| `findFirst(String, String)` | 文本, 正则 | `Optional<String>` | 第一个匹配 |
| `findAll(String, String)` | 文本, 正则 | `List<String>` | 所有匹配 |
| `findGroup(String, String, int)` | 文本, 正则, 组号 | `Optional<String>` | 分组匹配 |
| `findAllGroups(String, String)` | 文本, 正则 | `List<List<String>>` | 所有分组 |
| `findNamedGroups(String, String)` | 文本, 正则 | `Map<String,String>` | 命名分组 |
| `replaceFirst(String, String, String)` | 文本, 正则, 替换 | `String` | 替换第一个 |
| `replaceAll(String, String, String)` | 文本, 正则, 替换 | `String` | 替换全部 |
| `replaceAll(String, String, Function)` | 文本, 正则, 替换函数 | `String` | 函数式替换 |
| `split(String, String)` | 文本, 正则 | `List<String>` | 分割 |
| `split(String, String, int)` | 文本, 正则, 限制 | `List<String>` | 限制分割 |
| `escape(String)` | 字符串 | `String` | 转义正则元字符 |
| `compile(String)` | 正则 | `Pattern` | 编译正则 |
| `compileIgnoreCase(String)` | 正则 | `Pattern` | 编译（忽略大小写） |
| `countMatches(String, String)` | 文本, 正则 | `int` | 匹配次数 |
| `findPositions(String, String)` | 文本, 正则 | `List<int[]>` | 匹配位置 |
| `isEmail(String)` | 字符串 | `boolean` | 是否邮箱 |
| `isUrl(String)` | 字符串 | `boolean` | 是否 URL |
| `isMobile(String)` | 字符串 | `boolean` | 是否手机号 |
| `isIdCard(String)` | 字符串 | `boolean` | 是否身份证号 |
| `isIpv4(String)` | 字符串 | `boolean` | 是否 IPv4 |
| `isUuid(String)` | 字符串 | `boolean` | 是否 UUID |

### 3.20 OpenFormat - 格式化

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `formatNumber(Number)` | 数字 | `String` | 格式化数字 |
| `formatPercent(double)` | 小数 | `String` | 格式化百分比 |
| `formatCurrency(double)` | 金额 | `String` | 格式化货币 |
| `toChineseNumber(long)` | 数字 | `String` | 中文数字 |
| `toChineseMoney(double)` | 金额 | `String` | 中文金额 |
| `formatFileSize(long)` | 字节数 | `String` | 格式化文件大小 |
| `parseFileSize(String)` | 文件大小字符串 | `long` | 解析文件大小 |
| `formatDuration(Duration)` | 时长 | `String` | 格式化时长 |
| `formatTime(long)` | 毫秒 | `String` | 格式化时间 |
| `formatRelativeTime(Instant)` | 时间 | `String` | 相对时间（如"3分钟前"） |
| `formatMobile(String)` | 手机号 | `String` | 格式化手机号 |
| `formatIdCard(String)` | 身份证 | `String` | 格式化身份证 |
| `formatBankCard(String)` | 银行卡 | `String` | 格式化银行卡 |

### 3.21 OpenText - 文本处理

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `truncate(String, int)` | 文本, 长度 | `String` | 截断 |
| `truncateMiddle(String, int)` | 文本, 长度 | `String` | 中间截断 |
| `truncateByBytes(String, int, Charset)` | 文本, 字节数, 编码 | `String` | 按字节截断 |
| `highlight(String, String, String, String)` | 文本, 关键词, 前标记, 后标记 | `String` | 高亮 |
| `highlightHtml(String, String)` | 文本, 关键词 | `String` | HTML 高亮 |
| `wrap(String, int)` | 文本, 每行宽度 | `String` | 自动换行 |
| `indent(String, int)` | 文本, 缩进空格数 | `String` | 缩进 |

### 3.22 OpenUnicode - Unicode 工具

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `toUnicode(String)` | 字符串 | `String` | 转 Unicode 转义 |
| `fromUnicode(String)` | Unicode 字符串 | `String` | Unicode 转义还原 |
| `toHalfWidth(String)` | 字符串 | `String` | 全角转半角 |
| `toFullWidth(String)` | 字符串 | `String` | 半角转全角 |
| `toTraditional(String)` | 简体 | `String` | 简体转繁体 |
| `toSimplified(String)` | 繁体 | `String` | 繁体转简体 |
| `codePoint(char)` | 字符 | `int` | 获取码点 |
| `codePoints(String)` | 字符串 | `int[]` | 获取所有码点 |
| `fromCodePoints(int...)` | 码点列表 | `String` | 从码点构造字符串 |
| `containsEmoji(String)` | 字符串 | `boolean` | 是否包含 Emoji |
| `removeEmoji(String)` | 字符串 | `String` | 移除 Emoji |
| `displayWidth(String)` | 字符串 | `int` | 显示宽度（中文=2） |

### 3.23 OpenAbbreviation - 缩写工具

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `abbreviate(String, int)` | 字符串, 最大长度 | `String` | 缩写（加...） |
| `abbreviate(String, int, int, String)` | 字符串, 偏移, 最大长度, 省略号 | `String` | 自定义缩写 |
| `abbreviateMiddle(String, String, int)` | 字符串, 中间替代, 最大长度 | `String` | 中间缩写 |

### 3.24 OpenParse - 字符串解析

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `tokenize(String)` | 字符串 | `List<String>` | 分词（默认分隔符） |
| `tokenize(String, String)` | 字符串, 分隔符 | `List<String>` | 分词（自定义分隔符） |
| `parseCsv(String)` | CSV 内容 | `List<List<String>>` | 解析 CSV |
| `parseCsvWithHeader(String)` | CSV 内容 | `List<Map<String,String>>` | 解析带表头 CSV |

## 4. 使用示例

### 4.1 综合示例

```java
// 命名转换 + 模板渲染
String className = OpenNaming.tableToClass("sys_user");  // "SysUser"
String code = OpenTemplate.render(
    "public class ${className} { }",
    Map.of("className", className)
);

// 模糊匹配 + 搜索建议
FuzzyMatcher<String> matcher = FuzzyMatcher.<String>builder()
    .addAll(List.of("apple", "application", "banana", "apply"))
    .threshold(0.6)
    .maxResults(5)
    .algorithm(FuzzyMatcher.MatchAlgorithm.COMBINED)
    .build();
List<FuzzyMatch<String>> results = matcher.match("aple");

// 多模式匹配 / 敏感词过滤
AhoCorasick filter = AhoCorasick.of("敏感词1", "敏感词2");
String safe = filter.filter("这里有敏感词1和敏感词2");  // "这里有***和***"

// 数据脱敏
OpenMask.mobile("13812345678");   // "138****5678"
OpenMask.email("test@example.com"); // "t***@example.com"

// 差异对比
DiffResult diff = OpenDiff.diffLines(original, revised);
if (diff.hasDiff()) {
    System.out.println(diff.toUnifiedDiff());
}
```
