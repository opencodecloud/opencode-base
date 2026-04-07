# OpenCode Base CSV

**Lightweight CSV processing library for Java 25+, RFC 4180 compliant**

`opencode-base-csv` provides a comprehensive CSV processing toolkit with parsing, writing, object binding, streaming, diff detection, and formula injection protection. Zero external dependencies.

## Features (Since V1.0.3)

### Core Features
- **CSV Parsing**: Parse CSV strings, files, streams, and readers to immutable documents
- **CSV Writing**: Format documents to CSV strings or write to files, streams, and writers
- **Validation**: Check CSV string validity
- **Utilities**: Row counting, header extraction, field type conversion

### Advanced Features
- **Object Binding**: Bind CSV rows to Java Records and POJOs with `@CsvColumn` / `@CsvFormat` annotations
- **Streaming**: Lazy row-by-row `CsvReader` and incremental `CsvWriter` for large files
- **CSV Diff**: Compare two documents by position or by key column (ADDED / REMOVED / MODIFIED)
- **Document Builder**: Programmatic document construction via builder pattern
- **Sub-document Extraction**: Extract row ranges from a document

### Data Processing Features (New in V1.0.3-R2)
- **Query Engine**: Fluent SQL-like API — `select` / `where` / `orderBy` / `groupBy` / `limit` / `offset` / `distinct`
- **Transform Pipeline**: Column operations — `renameColumn` / `reorderColumns` / `addColumn` / `removeColumns` / `mapColumn`
- **Document Merge**: Vertical concat + horizontal joins (innerJoin / leftJoin by key column)
- **Column Statistics**: `count` / `sum` / `avg` / `min` / `max` / `distinct` / `frequency` / `summary`
- **Data Validation**: Declarative rules — `notBlank` / `range` / `pattern` / `minLength` / `maxLength` / `oneOf` / `custom`
- **Document Split**: Split by size, by condition, or by column value
- **Sampling**: Random (Fisher-Yates), systematic (every Nth row), stratified (proportional by group)

### Security Features
- **Formula Injection Protection**: Detect and sanitize fields starting with `=`, `+`, `-`, `@`, `\t`, `\r`
- **Size Limits**: Configurable max rows, columns, and field size to prevent DoS
- **Immutable Model**: All core types (`CsvDocument`, `CsvRow`, `CsvConfig`) are immutable and thread-safe

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-csv</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Parsing and Writing

```java
import cloud.opencode.base.csv.*;
import java.nio.file.Path;
import java.util.List;

// Parse CSV string
CsvDocument doc = OpenCsv.parse("name,age,city\nAlice,30,Beijing\nBob,25,Shanghai");

// Access data
List<String> headers = doc.headers();        // [name, age, city]
CsvRow row = doc.getRow(0);                  // Alice, 30, Beijing
String name = row.get(0);                    // "Alice"
List<String> ages = doc.getColumn("age");    // ["30", "25"]

// Row count and validation
int count = doc.rowCount();                  // 2
boolean valid = OpenCsv.isValid(csvString);  // true/false

// Write to string
String csv = OpenCsv.dump(doc);

// Write to file
OpenCsv.writeFile(doc, Path.of("output.csv"));
```

### File I/O

```java
// Parse from file
CsvDocument doc = OpenCsv.parseFile(Path.of("data.csv"));

// Parse with charset
CsvDocument doc = OpenCsv.parseFile(Path.of("data.csv"), StandardCharsets.ISO_8859_1);

// Parse from stream
CsvDocument doc = OpenCsv.parse(inputStream);

// Write to file with config
OpenCsv.writeFile(doc, Path.of("output.csv"), config);
```

### Object Binding (Record)

```java
import cloud.opencode.base.csv.bind.annotation.*;

// Define a Record
public record Employee(
    @CsvColumn("Name") String name,
    @CsvColumn(index = 1) int age,
    @CsvColumn(value = "Role", required = true) String role
) {}

// CSV -> Objects
List<Employee> employees = OpenCsv.bind("Name,age,Role\nAlice,30,Engineer", Employee.class);

// File -> Objects
List<Employee> employees = OpenCsv.bindFile(Path.of("employees.csv"), Employee.class);

// Objects -> CSV
String csv = OpenCsv.dumpObjects(employees, Employee.class);

// Objects -> CsvDocument
CsvDocument doc = OpenCsv.fromObjects(employees, Employee.class);
```

### Object Binding (POJO with Formatting)

```java
import cloud.opencode.base.csv.bind.annotation.*;

public record Order(
    @CsvColumn("order_date") @CsvFormat(pattern = "yyyy-MM-dd") LocalDate orderDate,
    @CsvColumn("amount") @CsvFormat(pattern = "#,##0.00") BigDecimal amount,
    @CsvColumn("note") @CsvFormat(nullValue = "N/A") String note
) {}

List<Order> orders = OpenCsv.bind(csvString, Order.class);
```

### Streaming (Large Files)

```java
import cloud.opencode.base.csv.stream.*;

// Streaming read
try (CsvReader reader = OpenCsv.reader(Path.of("large.csv"))) {
    reader.stream()
          .filter(row -> !row.get(0).isEmpty())
          .forEach(row -> process(row));
}

// Streaming write
try (CsvWriter writer = OpenCsv.writer(Path.of("output.csv"))) {
    writer.writeHeader("name", "age", "city")
          .writeRow("Alice", "30", "Beijing")
          .writeRow("Bob", "25", "Shanghai");
}
```

### CSV Diff

```java
import cloud.opencode.base.csv.diff.*;

CsvDocument original = OpenCsv.parse("id,name\n1,Alice\n2,Bob");
CsvDocument modified = OpenCsv.parse("id,name\n1,Alice\n2,Robert\n3,Charlie");

// Positional diff
List<CsvChange> changes = OpenCsv.diff(original, modified);

// Key-based diff
List<CsvChange> changes = OpenCsv.diffByKey(original, modified, "id");

for (CsvChange change : changes) {
    switch (change.type()) {
        case ADDED    -> System.out.println("Added: " + change.newRow());
        case REMOVED  -> System.out.println("Removed: " + change.oldRow());
        case MODIFIED -> System.out.println("Modified row " + change.rowIndex());
    }
}
```

### Query Engine

```java
import cloud.opencode.base.csv.query.CsvQuery;

// SQL-like query
CsvDocument result = CsvQuery.from(doc)
    .where(row -> Integer.parseInt(row.get(1)) > 25)
    .select("name", "age")
    .orderBy("age", true)
    .limit(10)
    .execute();

// Count matching rows
long count = CsvQuery.from(doc)
    .where(row -> !row.get(0).isEmpty())
    .count();

// Group by column
Map<String, CsvDocument> groups = CsvQuery.from(doc).groupBy("role");

// Distinct values
CsvDocument unique = CsvQuery.from(doc).distinct("name").execute();
```

### Transform Pipeline

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

### Document Merge

```java
import cloud.opencode.base.csv.merge.CsvMerge;

// Vertical concatenation
CsvDocument merged = CsvMerge.concat(doc1, doc2, doc3);

// Inner join on key column
CsvDocument joined = CsvMerge.innerJoin(employees, departments, "dept_id");

// Left join
CsvDocument joined = CsvMerge.leftJoin(orders, customers, "customer_id");
```

### Column Statistics

```java
import cloud.opencode.base.csv.stats.*;

BigDecimal total = CsvStats.sum(doc, "amount");
BigDecimal average = CsvStats.avg(doc, "price");
List<String> unique = CsvStats.distinct(doc, "category");
Map<String, Long> freq = CsvStats.frequency(doc, "status");

CsvColumnStats summary = CsvStats.summary(doc, "salary");
// summary.nonBlankCount(), summary.sum(), summary.avg(), summary.min(), summary.max()
```

### Data Validation

```java
import cloud.opencode.base.csv.validator.*;

CsvValidator validator = CsvValidator.builder()
    .notBlank("name")
    .range("age", 0, 150)
    .pattern("email", "^[\\w.]+@[\\w.]+$")
    .oneOf("status", "active", "inactive")
    .custom("score", v -> Double.parseDouble(v) >= 0, "Score must be non-negative")
    .build();

CsvValidationResult result = validator.validate(doc);
if (!result.valid()) {
    result.errors().forEach(e ->
        System.out.println("Row " + e.rowIndex() + ": " + e.message()));
}
```

### Document Split & Sampling

```java
import cloud.opencode.base.csv.split.CsvSplit;
import cloud.opencode.base.csv.sampling.CsvSampling;

// Split into chunks of 1000 rows
List<CsvDocument> chunks = CsvSplit.bySize(doc, 1000);

// Split by column value
Map<String, CsvDocument> groups = CsvSplit.byColumn(doc, "region");

// Random sample of 100 rows (reproducible with seed)
CsvDocument sample = CsvSampling.random(doc, 100, 42L);

// Stratified sampling
CsvDocument stratified = CsvSampling.stratified(doc, "category", 50);
```

### Configuration

```java
CsvConfig config = CsvConfig.builder()
    .delimiter(';')                              // semicolon separator
    .quoteChar('"')                              // quote character
    .charset(StandardCharsets.ISO_8859_1)        // charset
    .hasHeader(true)                             // first row is header
    .trimFields(true)                            // trim whitespace
    .skipEmptyRows(true)                         // skip blank lines
    .nullString("NULL")                          // null representation
    .formulaProtection(true)                     // sanitize formula injection
    .maxRows(100_000)                            // row limit
    .maxColumns(500)                             // column limit
    .maxFieldSize(65_536)                        // field size limit
    .build();

CsvDocument doc = OpenCsv.parse(csv, config);
```

### Security

```java
import cloud.opencode.base.csv.security.CsvSecurity;

// Detect formula injection
boolean dangerous = CsvSecurity.isFormulaInjection("=SUM(A1:A10)"); // true

// Sanitize dangerous values
String safe = CsvSecurity.sanitize("=cmd|' /C calc'"); // "'=cmd|' /C calc'"

// Validate limits
CsvSecurity.validateLimits(config, rowCount, columnCount, fieldSize);

// Or use config-level protection (auto-sanitizes on write)
CsvConfig safeConfig = CsvConfig.builder()
    .formulaProtection(true)
    .build();
```

### Building Documents Programmatically

```java
CsvDocument doc = CsvDocument.builder()
    .header("name", "age", "role")
    .addRow("Alice", "30", "Engineer")
    .addRow("Bob", "25", "Designer")
    .build();

// Sub-document extraction
CsvDocument sub = doc.subDocument(0, 1); // first row only

// Stream rows
doc.stream().filter(row -> row.get(1).equals("30")).forEach(System.out::println);

// Field type conversion
int age = CsvField.asInt("30");
BigDecimal amount = CsvField.asBigDecimal("1234.56");
LocalDate date = CsvField.asLocalDate("2024-01-15", DateTimeFormatter.ISO_LOCAL_DATE);
```

## API Reference

### OpenCsv (Main Facade)

| Method | Description |
|--------|-------------|
| `parse(String)` | Parse CSV string to CsvDocument |
| `parse(String, CsvConfig)` | Parse with configuration |
| `parseFile(Path)` | Parse CSV file |
| `parseFile(Path, CsvConfig)` | Parse file with configuration |
| `parseFile(Path, Charset)` | Parse file with charset |
| `parse(InputStream)` | Parse from input stream |
| `parse(InputStream, CsvConfig)` | Parse stream with configuration |
| `parse(Reader)` | Parse from reader |
| `parse(Reader, CsvConfig)` | Parse reader with configuration |
| `dump(CsvDocument)` | Format document to CSV string |
| `dump(CsvDocument, CsvConfig)` | Format with configuration |
| `writeFile(CsvDocument, Path)` | Write document to file |
| `writeFile(CsvDocument, Path, CsvConfig)` | Write to file with configuration |
| `write(CsvDocument, OutputStream)` | Write to output stream |
| `write(CsvDocument, Writer)` | Write to writer |
| `bind(String, Class<T>)` | Parse and bind to objects |
| `bind(String, Class<T>, CsvConfig)` | Bind with configuration |
| `bind(CsvDocument, Class<T>)` | Bind document rows to objects |
| `bindFile(Path, Class<T>)` | Parse file and bind to objects |
| `bindFile(Path, Class<T>, CsvConfig)` | Bind file with configuration |
| `fromObjects(Collection<T>, Class<T>)` | Convert objects to CsvDocument |
| `dumpObjects(Collection<T>, Class<T>)` | Convert objects to CSV string |
| `dumpObjects(Collection<T>, Class<T>, CsvConfig)` | Convert with configuration |
| `reader(Path)` | Create streaming CsvReader from file |
| `reader(InputStream)` | Create streaming CsvReader from stream |
| `reader(Reader)` | Create streaming CsvReader from reader |
| `writer(Path)` | Create streaming CsvWriter to file |
| `writer(OutputStream)` | Create streaming CsvWriter to stream |
| `writer(Writer)` | Create streaming CsvWriter to writer |
| `diff(CsvDocument, CsvDocument)` | Compute positional diff |
| `diffByKey(CsvDocument, CsvDocument, String)` | Compute key-based diff |
| `isValid(String)` | Check CSV validity |
| `rowCount(String)` | Count data rows |
| `headers(String)` | Extract header names |
| `query(CsvDocument)` | Create fluent query |
| `transform(CsvDocument)` | Create transformation pipeline |
| `concat(CsvDocument...)` | Concatenate documents vertically |
| `innerJoin(CsvDocument, CsvDocument, String)` | Inner join by key column |
| `leftJoin(CsvDocument, CsvDocument, String)` | Left join by key column |
| `stats(CsvDocument, String)` | Column summary statistics |
| `split(CsvDocument, int)` | Split by chunk size |
| `validator()` | Create validation builder |
| `builder()` | Create CsvDocument.Builder |
| `config()` | Create CsvConfig.Builder |

### CsvDocument

| Method | Description |
|--------|-------------|
| `builder()` | Create document builder |
| `headers()` | Get header list |
| `rows()` | Get all rows |
| `getRow(int)` | Get row by index |
| `getColumn(String)` | Get column values by header name |
| `getColumn(int)` | Get column values by index |
| `rowCount()` | Number of data rows |
| `columnCount()` | Number of columns |
| `isEmpty()` | Check if empty |
| `stream()` | Stream of rows |
| `subDocument(int, int)` | Extract row range |

### CsvRow

| Method | Description |
|--------|-------------|
| `of(String...)` | Create from field values |
| `of(int, String...)` | Create with row number |
| `get(int)` | Get field by index |
| `get(String, CsvDocument)` | Get field by header name |
| `size()` | Number of fields |
| `isEmpty()` | Check if all fields empty |
| `rowNumber()` | 1-based row number |
| `values()` / `fields()` | Unmodifiable field list |
| `stream()` | Stream of field values |
| `toMap(List<String>)` | Convert to header-keyed Map |

### CsvField (Type Conversion)

| Method | Description |
|--------|-------------|
| `asInt(String)` | Convert to int |
| `asLong(String)` | Convert to long |
| `asDouble(String)` | Convert to double |
| `asBoolean(String)` | Convert to boolean |
| `asBigDecimal(String)` | Convert to BigDecimal |
| `asLocalDate(String, DateTimeFormatter)` | Convert to LocalDate |
| `asLocalDateTime(String, DateTimeFormatter)` | Convert to LocalDateTime |
| `isBlank(String)` | Check if blank or null |

### CsvConfig (Builder)

| Builder Method | Description |
|----------------|-------------|
| `delimiter(char)` | Field delimiter (default: `,`) |
| `quoteChar(char)` | Quote character (default: `"`) |
| `escapeChar(char)` | Escape character (default: `"`) |
| `lineSeparator(String)` | Line separator (default: `\r\n`) |
| `charset(Charset)` | Character set (default: UTF-8) |
| `hasHeader(boolean)` | Header row present (default: `true`) |
| `trimFields(boolean)` | Trim whitespace (default: `false`) |
| `skipEmptyRows(boolean)` | Skip blank lines (default: `false`) |
| `maxRows(int)` | Max rows (default: 1,000,000) |
| `maxColumns(int)` | Max columns (default: 10,000) |
| `maxFieldSize(int)` | Max field size in bytes (default: 1 MB) |
| `formulaProtection(boolean)` | Formula injection protection (default: `false`) |
| `nullString(String)` | Null value representation (default: `""`) |

### CsvDiff

| Method | Description |
|--------|-------------|
| `diff(CsvDocument, CsvDocument)` | Positional row-by-row diff |
| `diffByKey(CsvDocument, CsvDocument, String)` | Key-column-based diff |

`CsvChange` record: `type()` (ADDED / REMOVED / MODIFIED), `rowIndex()`, `oldRow()`, `newRow()`

### CsvSecurity

| Method | Description |
|--------|-------------|
| `isFormulaInjection(String)` | Check for formula injection characters |
| `sanitize(String)` | Prepend single quote to dangerous values |
| `validateLimits(CsvConfig, int, int, int)` | Validate row/column/field limits |

### Annotations

| Annotation | Description |
|------------|-------------|
| `@CsvColumn(value, index, required)` | Map field to CSV column by name or index |
| `@CsvFormat(pattern, nullValue)` | Date/number format and null handling |

### Streaming

| CsvReader Method | Description |
|------------------|-------------|
| `of(Path, CsvConfig)` | Open file for streaming read |
| `of(InputStream, CsvConfig)` | Open stream for reading |
| `of(Reader, CsvConfig)` | Open reader for reading |
| `stream()` | Get lazy Stream of CsvRow |

| CsvWriter Method | Description |
|------------------|-------------|
| `of(Path, CsvConfig)` | Open file for streaming write |
| `of(OutputStream, CsvConfig)` | Open stream for writing |
| `of(Writer, CsvConfig)` | Open writer for writing |
| `writeHeader(String...)` | Write header row |
| `writeRow(String...)` | Write data row |

### CsvQuery (Fluent Query)

| Method | Description |
|--------|-------------|
| `from(CsvDocument)` | Create query from document |
| `select(String...)` | Select specific columns |
| `where(Predicate<CsvRow>)` | Filter rows |
| `orderBy(String, boolean)` | Sort by column |
| `orderBy(String, Comparator)` | Sort with custom comparator |
| `limit(int)` | Limit result rows |
| `offset(int)` | Skip first N rows |
| `distinct()` | Remove duplicate rows |
| `distinct(String...)` | Distinct by specific columns |
| `execute()` | Execute and return CsvDocument |
| `count()` | Count matching rows |
| `column(String)` | Extract column values |
| `groupBy(String)` | Group rows by column |
| `countBy(String)` | Count by group |

### CsvTransform (Transform Pipeline)

| Method | Description |
|--------|-------------|
| `from(CsvDocument)` | Create pipeline from document |
| `renameColumn(String, String)` | Rename a column |
| `renameColumns(Map)` | Batch rename |
| `reorderColumns(String...)` | Reorder columns |
| `addColumn(String, String)` | Add column with default value |
| `addColumn(String, Function)` | Add computed column |
| `removeColumns(String...)` | Remove columns |
| `mapColumn(String, UnaryOperator)` | Transform column values |
| `mapRows(UnaryOperator)` | Transform entire rows |
| `filterColumns(Predicate)` | Filter columns by predicate |
| `execute()` | Execute pipeline |

### CsvMerge

| Method | Description |
|--------|-------------|
| `concat(CsvDocument...)` | Vertical concatenation |
| `concat(List<CsvDocument>)` | Vertical concatenation from list |
| `innerJoin(CsvDocument, CsvDocument, String)` | Inner join by key column |
| `leftJoin(CsvDocument, CsvDocument, String)` | Left join by key column |

### CsvStats / CsvColumnStats

| Method | Description |
|--------|-------------|
| `count(CsvDocument, String)` | Count non-blank values |
| `countAll(CsvDocument)` | Count all rows |
| `sum(CsvDocument, String)` | Sum numeric column |
| `avg(CsvDocument, String)` | Average numeric column |
| `min(CsvDocument, String)` | Minimum numeric value |
| `max(CsvDocument, String)` | Maximum numeric value |
| `distinct(CsvDocument, String)` | Unique values |
| `frequency(CsvDocument, String)` | Value frequency map |
| `summary(CsvDocument, String)` | Full column statistics |

### CsvValidator

| Method | Description |
|--------|-------------|
| `builder()` | Create validator builder |
| `.notBlank(String)` | Not blank rule |
| `.range(String, double, double)` | Numeric range rule |
| `.pattern(String, String)` | Regex pattern rule |
| `.minLength(String, int)` | Minimum length rule |
| `.maxLength(String, int)` | Maximum length rule |
| `.oneOf(String, String...)` | Allowed values rule |
| `.custom(String, Predicate, String)` | Custom validation rule |
| `validate(CsvDocument)` | Execute validation |

### CsvSplit

| Method | Description |
|--------|-------------|
| `bySize(CsvDocument, int)` | Split into fixed-size chunks |
| `byCondition(CsvDocument, Predicate)` | Split by predicate (2 groups) |
| `byColumn(CsvDocument, String)` | Split by column value |

### CsvSampling

| Method | Description |
|--------|-------------|
| `random(CsvDocument, int)` | Random sampling |
| `random(CsvDocument, int, long)` | Seeded random sampling |
| `systematic(CsvDocument, int)` | Systematic sampling (every Nth) |
| `systematic(CsvDocument, int, int)` | Systematic with start offset |
| `stratified(CsvDocument, String, int)` | Stratified sampling by column |
| `stratified(CsvDocument, String, int, long)` | Seeded stratified sampling |

## Requirements

- Java 25+
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
