# OpenCode Base CSV

**适用于 Java 25+ 的轻量级 CSV 处理库，符合 RFC 4180 标准**

`opencode-base-csv` 提供了一套完整的 CSV 处理工具包，包括解析、写入、对象绑定、流式处理、差异检测和公式注入保护。零外部依赖。

## 功能特性（Since V1.0.3）

### 核心功能
- **CSV 解析**：将 CSV 字符串、文件、流和 Reader 解析为不可变文档
- **CSV 写入**：将文档格式化为 CSV 字符串或写入文件、流和 Writer
- **验证**：检查 CSV 字符串有效性
- **工具方法**：行计数、标题提取、字段类型转换

### 高级功能
- **对象绑定**：使用 `@CsvColumn` / `@CsvFormat` 注解将 CSV 行绑定到 Java Record 和 POJO
- **流式处理**：适用于大文件的逐行懒加载 `CsvReader` 和增量写入 `CsvWriter`
- **CSV 差异**：按位置或按键列比较两个文档（ADDED / REMOVED / MODIFIED）
- **文档构建器**：通过 Builder 模式以编程方式构建文档
- **子文档提取**：从文档中提取行范围

### 数据处理功能（V1.0.3-R2 新增）
- **查询引擎**：流式 SQL 风格 API — `select` / `where` / `orderBy` / `groupBy` / `limit` / `offset` / `distinct`
- **转换管道**：列操作 — `renameColumn` / `reorderColumns` / `addColumn` / `removeColumns` / `mapColumn`
- **文档合并**：纵向拼接 concat + 横向连接（innerJoin / leftJoin 按键列）
- **列统计**：`count` / `sum` / `avg` / `min` / `max` / `distinct` / `frequency` / `summary`
- **数据校验**：声明式规则 — `notBlank` / `range` / `pattern` / `minLength` / `maxLength` / `oneOf` / `custom`
- **文档分割**：按大小、按条件、按列值分割
- **数据抽样**：随机抽样（Fisher-Yates）、系统抽样（每N行）、分层抽样（按组等比例）

### 安全功能
- **公式注入保护**：检测并净化以 `=`、`+`、`-`、`@`、`\t`、`\r` 开头的字段
- **大小限制**：可配置最大行数、列数和字段大小，防止 DoS 攻击
- **不可变模型**：所有核心类型（`CsvDocument`、`CsvRow`、`CsvConfig`）均不可变且线程安全

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-csv</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本解析和写入

```java
import cloud.opencode.base.csv.*;
import java.nio.file.Path;
import java.util.List;

// 解析 CSV 字符串
CsvDocument doc = OpenCsv.parse("name,age,city\nAlice,30,Beijing\nBob,25,Shanghai");

// 访问数据
List<String> headers = doc.headers();        // [name, age, city]
CsvRow row = doc.getRow(0);                  // Alice, 30, Beijing
String name = row.get(0);                    // "Alice"
List<String> ages = doc.getColumn("age");    // ["30", "25"]

// 行计数和验证
int count = doc.rowCount();                  // 2
boolean valid = OpenCsv.isValid(csvString);  // true/false

// 写入字符串
String csv = OpenCsv.dump(doc);

// 写入文件
OpenCsv.writeFile(doc, Path.of("output.csv"));
```

### 文件 I/O

```java
// 从文件解析
CsvDocument doc = OpenCsv.parseFile(Path.of("data.csv"));

// 使用指定字符集解析
CsvDocument doc = OpenCsv.parseFile(Path.of("data.csv"), StandardCharsets.ISO_8859_1);

// 从流解析
CsvDocument doc = OpenCsv.parse(inputStream);

// 使用配置写入文件
OpenCsv.writeFile(doc, Path.of("output.csv"), config);
```

### 对象绑定（Record）

```java
import cloud.opencode.base.csv.bind.annotation.*;

// 定义 Record
public record Employee(
    @CsvColumn("Name") String name,
    @CsvColumn(index = 1) int age,
    @CsvColumn(value = "Role", required = true) String role
) {}

// CSV -> 对象
List<Employee> employees = OpenCsv.bind("Name,age,Role\nAlice,30,Engineer", Employee.class);

// 文件 -> 对象
List<Employee> employees = OpenCsv.bindFile(Path.of("employees.csv"), Employee.class);

// 对象 -> CSV
String csv = OpenCsv.dumpObjects(employees, Employee.class);

// 对象 -> CsvDocument
CsvDocument doc = OpenCsv.fromObjects(employees, Employee.class);
```

### 对象绑定（带格式化的 POJO）

```java
import cloud.opencode.base.csv.bind.annotation.*;

public record Order(
    @CsvColumn("order_date") @CsvFormat(pattern = "yyyy-MM-dd") LocalDate orderDate,
    @CsvColumn("amount") @CsvFormat(pattern = "#,##0.00") BigDecimal amount,
    @CsvColumn("note") @CsvFormat(nullValue = "N/A") String note
) {}

List<Order> orders = OpenCsv.bind(csvString, Order.class);
```

### 流式处理（大文件）

```java
import cloud.opencode.base.csv.stream.*;

// 流式读取
try (CsvReader reader = OpenCsv.reader(Path.of("large.csv"))) {
    reader.stream()
          .filter(row -> !row.get(0).isEmpty())
          .forEach(row -> process(row));
}

// 流式写入
try (CsvWriter writer = OpenCsv.writer(Path.of("output.csv"))) {
    writer.writeHeader("name", "age", "city")
          .writeRow("Alice", "30", "Beijing")
          .writeRow("Bob", "25", "Shanghai");
}
```

### CSV 差异比较

```java
import cloud.opencode.base.csv.diff.*;

CsvDocument original = OpenCsv.parse("id,name\n1,Alice\n2,Bob");
CsvDocument modified = OpenCsv.parse("id,name\n1,Alice\n2,Robert\n3,Charlie");

// 位置差异
List<CsvChange> changes = OpenCsv.diff(original, modified);

// 基于键列的差异
List<CsvChange> changes = OpenCsv.diffByKey(original, modified, "id");

for (CsvChange change : changes) {
    switch (change.type()) {
        case ADDED    -> System.out.println("新增: " + change.newRow());
        case REMOVED  -> System.out.println("删除: " + change.oldRow());
        case MODIFIED -> System.out.println("修改行 " + change.rowIndex());
    }
}
```

### 查询引擎

```java
import cloud.opencode.base.csv.query.CsvQuery;

// 类 SQL 查询
CsvDocument result = CsvQuery.from(doc)
    .where(row -> Integer.parseInt(row.get(1)) > 25)
    .select("name", "age")
    .orderBy("age", true)
    .limit(10)
    .execute();

// 计算匹配行数
long count = CsvQuery.from(doc)
    .where(row -> !row.get(0).isEmpty())
    .count();

// 按列分组
Map<String, CsvDocument> groups = CsvQuery.from(doc).groupBy("role");

// 去重
CsvDocument unique = CsvQuery.from(doc).distinct("name").execute();
```

### 转换管道

```java
import cloud.opencode.base.csv.transform.CsvTransform;

CsvDocument result = CsvTransform.from(doc)
    .renameColumn("old_name", "new_name")
    .addColumn("status", "active")
    .removeColumns("temp_col")
    .mapColumn("name", String::toUpperCase)
    .reorderColumns("id", "name", "status")
    .execute();
```

### 文档合并

```java
import cloud.opencode.base.csv.merge.CsvMerge;

// 纵向拼接
CsvDocument merged = CsvMerge.concat(doc1, doc2, doc3);

// 内连接（按键列）
CsvDocument joined = CsvMerge.innerJoin(employees, departments, "dept_id");

// 左连接
CsvDocument joined = CsvMerge.leftJoin(orders, customers, "customer_id");
```

### 列统计

```java
import cloud.opencode.base.csv.stats.*;

BigDecimal total = CsvStats.sum(doc, "amount");
BigDecimal average = CsvStats.avg(doc, "price");
List<String> unique = CsvStats.distinct(doc, "category");
Map<String, Long> freq = CsvStats.frequency(doc, "status");

CsvColumnStats summary = CsvStats.summary(doc, "salary");
// summary.nonBlankCount(), summary.sum(), summary.avg(), summary.min(), summary.max()
```

### 数据校验

```java
import cloud.opencode.base.csv.validator.*;

CsvValidator validator = CsvValidator.builder()
    .notBlank("name")
    .range("age", 0, 150)
    .pattern("email", "^[\\w.]+@[\\w.]+$")
    .oneOf("status", "active", "inactive")
    .custom("score", v -> Double.parseDouble(v) >= 0, "分数不能为负数")
    .build();

CsvValidationResult result = validator.validate(doc);
if (!result.valid()) {
    result.errors().forEach(e ->
        System.out.println("第 " + e.rowIndex() + " 行: " + e.message()));
}
```

### 文档分割与抽样

```java
import cloud.opencode.base.csv.split.CsvSplit;
import cloud.opencode.base.csv.sampling.CsvSampling;

// 按每块 1000 行分割
List<CsvDocument> chunks = CsvSplit.bySize(doc, 1000);

// 按列值分组
Map<String, CsvDocument> groups = CsvSplit.byColumn(doc, "region");

// 随机抽样 100 行（可重现，指定种子）
CsvDocument sample = CsvSampling.random(doc, 100, 42L);

// 分层抽样
CsvDocument stratified = CsvSampling.stratified(doc, "category", 50);
```

### 配置

```java
CsvConfig config = CsvConfig.builder()
    .delimiter(';')                              // 分号分隔符
    .quoteChar('"')                              // 引用字符
    .charset(StandardCharsets.ISO_8859_1)        // 字符集
    .hasHeader(true)                             // 第一行为标题
    .trimFields(true)                            // 修剪空白
    .skipEmptyRows(true)                         // 跳过空行
    .nullString("NULL")                          // null 值表示
    .formulaProtection(true)                     // 净化公式注入
    .maxRows(100_000)                            // 行数限制
    .maxColumns(500)                             // 列数限制
    .maxFieldSize(65_536)                        // 字段大小限制
    .build();

CsvDocument doc = OpenCsv.parse(csv, config);
```

### 安全

```java
import cloud.opencode.base.csv.security.CsvSecurity;

// 检测公式注入
boolean dangerous = CsvSecurity.isFormulaInjection("=SUM(A1:A10)"); // true

// 净化危险值
String safe = CsvSecurity.sanitize("=cmd|' /C calc'"); // "'=cmd|' /C calc'"

// 验证限制
CsvSecurity.validateLimits(config, rowCount, columnCount, fieldSize);

// 或使用配置级保护（写入时自动净化）
CsvConfig safeConfig = CsvConfig.builder()
    .formulaProtection(true)
    .build();
```

### 编程方式构建文档

```java
CsvDocument doc = CsvDocument.builder()
    .header("name", "age", "role")
    .addRow("Alice", "30", "Engineer")
    .addRow("Bob", "25", "Designer")
    .build();

// 子文档提取
CsvDocument sub = doc.subDocument(0, 1); // 仅第一行

// 流式处理行
doc.stream().filter(row -> row.get(1).equals("30")).forEach(System.out::println);

// 字段类型转换
int age = CsvField.asInt("30");
BigDecimal amount = CsvField.asBigDecimal("1234.56");
LocalDate date = CsvField.asLocalDate("2024-01-15", DateTimeFormatter.ISO_LOCAL_DATE);
```

## API 参考

### OpenCsv（主门面）

| 方法 | 说明 |
|------|------|
| `parse(String)` | 解析 CSV 字符串为 CsvDocument |
| `parse(String, CsvConfig)` | 使用配置解析 |
| `parseFile(Path)` | 解析 CSV 文件 |
| `parseFile(Path, CsvConfig)` | 使用配置解析文件 |
| `parseFile(Path, Charset)` | 使用字符集解析文件 |
| `parse(InputStream)` | 从输入流解析 |
| `parse(InputStream, CsvConfig)` | 使用配置从流解析 |
| `parse(Reader)` | 从 Reader 解析 |
| `parse(Reader, CsvConfig)` | 使用配置从 Reader 解析 |
| `dump(CsvDocument)` | 格式化文档为 CSV 字符串 |
| `dump(CsvDocument, CsvConfig)` | 使用配置格式化 |
| `writeFile(CsvDocument, Path)` | 写入文档到文件 |
| `writeFile(CsvDocument, Path, CsvConfig)` | 使用配置写入文件 |
| `write(CsvDocument, OutputStream)` | 写入到输出流 |
| `write(CsvDocument, Writer)` | 写入到 Writer |
| `bind(String, Class<T>)` | 解析并绑定到对象 |
| `bind(String, Class<T>, CsvConfig)` | 使用配置绑定 |
| `bind(CsvDocument, Class<T>)` | 将文档行绑定到对象 |
| `bindFile(Path, Class<T>)` | 解析文件并绑定到对象 |
| `bindFile(Path, Class<T>, CsvConfig)` | 使用配置绑定文件 |
| `fromObjects(Collection<T>, Class<T>)` | 将对象转换为 CsvDocument |
| `dumpObjects(Collection<T>, Class<T>)` | 将对象转换为 CSV 字符串 |
| `dumpObjects(Collection<T>, Class<T>, CsvConfig)` | 使用配置转换 |
| `reader(Path)` | 从文件创建流式 CsvReader |
| `reader(InputStream)` | 从流创建流式 CsvReader |
| `reader(Reader)` | 从 Reader 创建流式 CsvReader |
| `writer(Path)` | 创建到文件的流式 CsvWriter |
| `writer(OutputStream)` | 创建到流的流式 CsvWriter |
| `writer(Writer)` | 创建到 Writer 的流式 CsvWriter |
| `diff(CsvDocument, CsvDocument)` | 计算位置差异 |
| `diffByKey(CsvDocument, CsvDocument, String)` | 计算基于键列的差异 |
| `isValid(String)` | 检查 CSV 有效性 |
| `rowCount(String)` | 计算数据行数 |
| `headers(String)` | 提取标题名称 |
| `query(CsvDocument)` | 创建流式查询 |
| `transform(CsvDocument)` | 创建转换管道 |
| `concat(CsvDocument...)` | 纵向拼接文档 |
| `innerJoin(CsvDocument, CsvDocument, String)` | 按键列内连接 |
| `leftJoin(CsvDocument, CsvDocument, String)` | 按键列左连接 |
| `stats(CsvDocument, String)` | 列摘要统计 |
| `split(CsvDocument, int)` | 按块大小分割 |
| `validator()` | 创建校验器构建器 |
| `builder()` | 创建 CsvDocument.Builder |
| `config()` | 创建 CsvConfig.Builder |

### CsvDocument（文档）

| 方法 | 说明 |
|------|------|
| `builder()` | 创建文档构建器 |
| `headers()` | 获取标题列表 |
| `rows()` | 获取所有行 |
| `getRow(int)` | 通过索引获取行 |
| `getColumn(String)` | 通过标题名获取列值 |
| `getColumn(int)` | 通过索引获取列值 |
| `rowCount()` | 数据行数 |
| `columnCount()` | 列数 |
| `isEmpty()` | 检查是否为空 |
| `stream()` | 行的流 |
| `subDocument(int, int)` | 提取行范围 |

### CsvRow（行）

| 方法 | 说明 |
|------|------|
| `of(String...)` | 从字段值创建 |
| `of(int, String...)` | 使用行号创建 |
| `get(int)` | 通过索引获取字段 |
| `get(String, CsvDocument)` | 通过标题名获取字段 |
| `size()` | 字段数 |
| `isEmpty()` | 检查所有字段是否为空 |
| `rowNumber()` | 1 起始行号 |
| `values()` / `fields()` | 不可修改的字段列表 |
| `stream()` | 字段值的流 |
| `toMap(List<String>)` | 转换为标题键 Map |

### CsvField（类型转换）

| 方法 | 说明 |
|------|------|
| `asInt(String)` | 转换为 int |
| `asLong(String)` | 转换为 long |
| `asDouble(String)` | 转换为 double |
| `asBoolean(String)` | 转换为 boolean |
| `asBigDecimal(String)` | 转换为 BigDecimal |
| `asLocalDate(String, DateTimeFormatter)` | 转换为 LocalDate |
| `asLocalDateTime(String, DateTimeFormatter)` | 转换为 LocalDateTime |
| `isBlank(String)` | 检查是否为空白或 null |

### CsvConfig（配置构建器）

| 构建器方法 | 说明 |
|-----------|------|
| `delimiter(char)` | 字段分隔符（默认：`,`） |
| `quoteChar(char)` | 引用字符（默认：`"`） |
| `escapeChar(char)` | 转义字符（默认：`"`） |
| `lineSeparator(String)` | 行分隔符（默认：`\r\n`） |
| `charset(Charset)` | 字符集（默认：UTF-8） |
| `hasHeader(boolean)` | 是否有标题行（默认：`true`） |
| `trimFields(boolean)` | 修剪空白（默认：`false`） |
| `skipEmptyRows(boolean)` | 跳过空行（默认：`false`） |
| `maxRows(int)` | 最大行数（默认：1,000,000） |
| `maxColumns(int)` | 最大列数（默认：10,000） |
| `maxFieldSize(int)` | 最大字段大小（默认：1 MB） |
| `formulaProtection(boolean)` | 公式注入保护（默认：`false`） |
| `nullString(String)` | null 值表示（默认：`""`） |

### CsvDiff（差异比较）

| 方法 | 说明 |
|------|------|
| `diff(CsvDocument, CsvDocument)` | 位置逐行差异 |
| `diffByKey(CsvDocument, CsvDocument, String)` | 基于键列的差异 |

`CsvChange` 记录：`type()`（ADDED / REMOVED / MODIFIED）、`rowIndex()`、`oldRow()`、`newRow()`

### CsvSecurity（安全工具）

| 方法 | 说明 |
|------|------|
| `isFormulaInjection(String)` | 检查公式注入字符 |
| `sanitize(String)` | 在危险值前添加单引号 |
| `validateLimits(CsvConfig, int, int, int)` | 验证行/列/字段限制 |

### 绑定注解

| 注解 | 说明 |
|------|------|
| `@CsvColumn(value, index, required)` | 通过名称或索引映射字段到 CSV 列 |
| `@CsvFormat(pattern, nullValue)` | 日期/数字格式和 null 值处理 |

### 流式处理

| CsvReader 方法 | 说明 |
|---------------|------|
| `of(Path, CsvConfig)` | 打开文件进行流式读取 |
| `of(InputStream, CsvConfig)` | 打开流进行读取 |
| `of(Reader, CsvConfig)` | 打开 Reader 进行读取 |
| `stream()` | 获取 CsvRow 的懒加载 Stream |

| CsvWriter 方法 | 说明 |
|---------------|------|
| `of(Path, CsvConfig)` | 打开文件进行流式写入 |
| `of(OutputStream, CsvConfig)` | 打开流进行写入 |
| `of(Writer, CsvConfig)` | 打开 Writer 进行写入 |
| `writeHeader(String...)` | 写入标题行 |
| `writeRow(String...)` | 写入数据行 |

### CsvQuery（流式查询）

| 方法 | 说明 |
|------|------|
| `from(CsvDocument)` | 从文档创建查询 |
| `select(String...)` | 选择指定列 |
| `where(Predicate<CsvRow>)` | 过滤行 |
| `orderBy(String, boolean)` | 按列排序 |
| `orderBy(String, Comparator)` | 使用自定义比较器排序 |
| `limit(int)` | 限制结果行数 |
| `offset(int)` | 跳过前 N 行 |
| `distinct()` | 移除重复行 |
| `distinct(String...)` | 按指定列去重 |
| `execute()` | 执行并返回 CsvDocument |
| `count()` | 计算匹配行数 |
| `column(String)` | 提取列值 |
| `groupBy(String)` | 按列分组 |
| `countBy(String)` | 按组计数 |

### CsvTransform（转换管道）

| 方法 | 说明 |
|------|------|
| `from(CsvDocument)` | 从文档创建管道 |
| `renameColumn(String, String)` | 重命名列 |
| `renameColumns(Map)` | 批量重命名 |
| `reorderColumns(String...)` | 重排列顺序 |
| `addColumn(String, String)` | 添加列（固定默认值） |
| `addColumn(String, Function)` | 添加计算列 |
| `removeColumns(String...)` | 移除列 |
| `mapColumn(String, UnaryOperator)` | 转换列值 |
| `mapRows(UnaryOperator)` | 转换整行 |
| `filterColumns(Predicate)` | 按谓词筛选列 |
| `execute()` | 执行管道 |

### CsvMerge（文档合并）

| 方法 | 说明 |
|------|------|
| `concat(CsvDocument...)` | 纵向拼接 |
| `concat(List<CsvDocument>)` | 从列表纵向拼接 |
| `innerJoin(CsvDocument, CsvDocument, String)` | 按键列内连接 |
| `leftJoin(CsvDocument, CsvDocument, String)` | 按键列左连接 |

### CsvStats / CsvColumnStats（列统计）

| 方法 | 说明 |
|------|------|
| `count(CsvDocument, String)` | 统计非空值数量 |
| `countAll(CsvDocument)` | 统计所有行数 |
| `sum(CsvDocument, String)` | 数值列求和 |
| `avg(CsvDocument, String)` | 数值列平均值 |
| `min(CsvDocument, String)` | 数值列最小值 |
| `max(CsvDocument, String)` | 数值列最大值 |
| `distinct(CsvDocument, String)` | 唯一值 |
| `frequency(CsvDocument, String)` | 值频率分布 |
| `summary(CsvDocument, String)` | 完整列统计 |

### CsvValidator（数据校验）

| 方法 | 说明 |
|------|------|
| `builder()` | 创建校验器构建器 |
| `.notBlank(String)` | 非空规则 |
| `.range(String, double, double)` | 数值范围规则 |
| `.pattern(String, String)` | 正则匹配规则 |
| `.minLength(String, int)` | 最小长度规则 |
| `.maxLength(String, int)` | 最大长度规则 |
| `.oneOf(String, String...)` | 允许值规则 |
| `.custom(String, Predicate, String)` | 自定义校验规则 |
| `validate(CsvDocument)` | 执行校验 |

### CsvSplit（文档分割）

| 方法 | 说明 |
|------|------|
| `bySize(CsvDocument, int)` | 按固定大小分块 |
| `byCondition(CsvDocument, Predicate)` | 按谓词分割（2 组） |
| `byColumn(CsvDocument, String)` | 按列值分组 |

### CsvSampling（数据抽样）

| 方法 | 说明 |
|------|------|
| `random(CsvDocument, int)` | 随机抽样 |
| `random(CsvDocument, int, long)` | 带种子随机抽样 |
| `systematic(CsvDocument, int)` | 系统抽样（每N行） |
| `systematic(CsvDocument, int, int)` | 带起始偏移的系统抽样 |
| `stratified(CsvDocument, String, int)` | 按列分层抽样 |
| `stratified(CsvDocument, String, int, long)` | 带种子分层抽样 |

## 环境要求

- Java 25+
- 无外部依赖

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
