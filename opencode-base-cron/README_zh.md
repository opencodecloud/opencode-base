# OpenCode Base Cron

面向 JDK 25+ 的全功能 Cron 表达式库。零依赖，线程安全，不可变。

## 功能特性

- 解析 5 字段和 6 字段（含秒）Cron 表达式
- 宏支持：`@yearly`、`@monthly`、`@weekly`、`@daily`、`@hourly`
- 名称别名：`MON-FRI`、`JAN-DEC`（不区分大小写）
- 特殊字符：`L`（最后）、`W`（工作日）、`#`（第 N 个）、范围回绕
- 正向和反向调度（计算下次/上次执行时间）
- 惰性 `Stream<ZonedDateTime>` 正向和反向调度流
- 过滤调度 `Predicate`（节假日排除、周末跳过等）
- 两个 Cron 表达式之间的调度重叠检测
- `TemporalAdjuster` 集成（`zonedDateTime.with(cronExpr)`）
- 可配置搜索窗口（默认 4 年，最大 100 年）
- 两个时间点之间的执行次数统计和列表
- Duration 便捷方法（距下次/上次执行的时间间隔）
- 表达式等价性检测（结构比较）
- 一站式调试 `explain()`（描述 + 下 5 次执行 + 间隔）
- 人类可读描述，支持英文和中文
- 流式构建器 API，带输入校验
- 最小间隔验证
- 线程安全，空值安全，零依赖

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-cron</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Cron 表达式语法

### 字段格式

**5 字段**（标准 Unix）：
```
┌───────────── 分钟 (0-59)
│ ┌───────────── 小时 (0-23)
│ │ ┌───────────── 月中日 (1-31)
│ │ │ ┌───────────── 月份 (1-12 或 JAN-DEC)
│ │ │ │ ┌───────────── 星期几 (0-6, 周日=0, 或 SUN-SAT)
│ │ │ │ │
* * * * *
```

**6 字段**（含秒）：
```
┌───────────── 秒 (0-59)
│ ┌───────────── 分钟 (0-59)
│ │ ┌───────────── 小时 (0-23)
│ │ │ ┌───────────── 月中日 (1-31)
│ │ │ │ ┌───────────── 月份 (1-12 或 JAN-DEC)
│ │ │ │ │ ┌───────────── 星期几 (0-6 或 SUN-SAT)
│ │ │ │ │ │
* * * * * *
```

### 特殊字符

| 字符 | 含义 | 示例 | 说明 |
|------|------|------|------|
| `*` | 任意值 | `* * * * *` | 每分钟 |
| `?` | 无特定值 | `0 0 * * ?` | 在日期字段中等同于 `*` |
| `,` | 列表 | `1,15 * * * *` | 第 1 分钟和第 15 分钟 |
| `-` | 范围 | `9-17 * * * *` | 第 9 到第 17 分钟 |
| `/` | 步长 | `*/5 * * * *` | 每 5 分钟 |
| `L` | 最后 | `0 0 L * *` | 每月最后一天 |
| `L-N` | 最后减 N | `0 0 L-3 * *` | 最后一天前 3 天 |
| `LW` | 最后工作日 | `0 0 LW * *` | 每月最后一个工作日 |
| `nW` | 最近工作日 | `0 0 15W * *` | 最接近 15 号的工作日 |
| `n#m` | 第 N 个 | `0 0 * * 5#3` | 第 3 个周五 |
| `nL` | 最后一个 | `0 0 * * 5L` | 最后一个周五 |

### 预定义宏

| 宏 | 等价表达式 | 说明 |
|----|-----------|------|
| `@yearly` / `@annually` | `0 0 1 1 *` | 每年 1 月 1 日午夜 |
| `@monthly` | `0 0 1 * *` | 每月 1 号午夜 |
| `@weekly` | `0 0 * * 0` | 每周日午夜 |
| `@daily` / `@midnight` | `0 0 * * *` | 每天午夜 |
| `@hourly` | `0 * * * *` | 每小时 |

### 月中日/星期几 OR 语义

当月中日和星期几**同时被显式设置**（非 `*` 或 `?`）时，表达式在**任一匹配时**触发（OR 逻辑），遵循标准 Unix cron 行为。

```java
// 每月 15 号或每周一中午触发
CronExpression.parse("0 12 15 * MON")
```

## API 概览

| 类名 | 说明 |
|------|------|
| `OpenCron` | 门面类 — 所有 Cron 操作的统一入口 |
| `CronExpression` | 不可变的已解析表达式，支持调度、匹配、描述 |
| `CronExplanation` | 不可变记录，一站式调试输出 |
| `CronBuilder` | 流式构建器 API |
| `CronValidator` | 语法和间隔验证工具 |
| `CronField` | 枚举，定义各字段范围和别名 |
| `CronMacro` | 宏解析（`@daily` → `0 0 * * *`） |
| `OpenCronException` | 运行时异常，携带诊断上下文 |

## 快速开始

```java
import cloud.opencode.base.cron.*;
import java.time.*;
import java.util.*;

ZonedDateTime now = ZonedDateTime.now();

// 解析并查询下次执行时间
ZonedDateTime next = OpenCron.nextExecution("0 9 * * MON-FRI", now);

// 获取下 5 次执行时间
List<ZonedDateTime> times = OpenCron.nextExecutions("30 10 * * *", now, 5);

// 验证表达式
boolean valid = OpenCron.isValid("0 0 L * *");

// 人类可读描述
OpenCron.describe("0 9 * * MON-FRI");                  // "At 09:00, Monday through Friday"
OpenCron.describe("0 9 * * MON-FRI", Locale.CHINESE);  // "在09:00，周一到周五"

// 构建器 API
CronExpression expr = OpenCron.builder().weekdays().at(9, 0).build();

// 宏支持
CronExpression daily = OpenCron.parse("@daily");
```

## Stream 惰性流式调度

```java
CronExpression expr = CronExpression.parse("*/5 * * * *");
ZonedDateTime now = ZonedDateTime.now();
ZonedDateTime deadline = now.plusDays(1);

// 惰性流 — 按需计算，支持所有 Stream 操作
List<ZonedDateTime> next10 = expr.stream(now).limit(10).toList();

// deadline 之前的所有执行
List<ZonedDateTime> beforeDeadline = expr.stream(now)
        .takeWhile(t -> t.isBefore(deadline))
        .toList();

// 反向流（过去的执行，最新在前）
List<ZonedDateTime> last5 = expr.reverseStream(now).limit(5).toList();
```

## 过滤调度

```java
CronExpression expr = CronExpression.parse("0 9 * * *"); // 每天 9:00
ZonedDateTime now = ZonedDateTime.now();

// 跳过周末
ZonedDateTime nextWeekday = expr.nextExecution(now,
        t -> t.getDayOfWeek().getValue() <= 5);

// 跳过节假日
Set<LocalDate> holidays = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 12, 25));
ZonedDateTime nextWorkday = expr.nextExecution(now,
        t -> !holidays.contains(t.toLocalDate()));

// 反向过滤
ZonedDateTime prevWeekday = expr.previousExecution(now,
        t -> t.getDayOfWeek().getValue() <= 5);
```

## 重叠检测

```java
CronExpression daily = CronExpression.parse("0 0 * * *");
CronExpression monthly = CronExpression.parse("0 0 1 * *");
ZonedDateTime now = ZonedDateTime.now();

// 查找两者同时触发的下一个时间
ZonedDateTime overlap = daily.nextOverlap(monthly, now); // 下个月1号 00:00

// 检查时间范围内是否有重叠
ZonedDateTime from = ZonedDateTime.parse("2026-01-01T00:00:00Z");
ZonedDateTime to = ZonedDateTime.parse("2026-12-31T23:59:59Z");
boolean hasOverlap = daily.hasOverlapBetween(monthly, from, to); // true
```

## TemporalAdjuster 集成

```java
CronExpression expr = CronExpression.parse("0 9 * * MON-FRI");

// 使用 java.time 原生写法
ZonedDateTime nextFire = ZonedDateTime.now().with(expr);
```

## Duration 和间隔

```java
ZonedDateTime now = ZonedDateTime.now();

// 距下次执行的时间
Duration toNext = OpenCron.timeToNextExecution("0 9 * * *", now);

// 距上次执行的时间
Duration fromLast = OpenCron.timeFromLastExecution("0 9 * * *", now);

// 预估执行间隔
Duration interval = OpenCron.getEstimatedInterval("*/5 * * * *"); // PT5M
```

## 区间执行统计

```java
ZonedDateTime from = ZonedDateTime.parse("2026-01-01T00:00:00Z");
ZonedDateTime to = ZonedDateTime.parse("2026-01-08T00:00:00Z");

// 仅计数（无 List 分配，上限 1M）
long count = OpenCron.countExecutionsBetween("0 0 * * *", from, to); // 7

// 列出所有执行时间（上限 100K）
List<ZonedDateTime> execs = OpenCron.executionsBetween("0 0 * * *", from, to);
```

## 表达式等价性

```java
// 结构比较（BitSet + 特殊字段）
boolean eq = OpenCron.isEquivalent("0 0 * * *", "@daily"); // true
```

## 一站式调试

```java
CronExplanation info = OpenCron.explain("*/5 * * * *", ZonedDateTime.now());
System.out.println(info);
// Expression : */5 * * * *
// Description: Every 5 minutes
// Interval   : PT5M
// Next executions:
//   1. 2026-04-03T10:05:00+08:00[Asia/Shanghai]
//   2. 2026-04-03T10:10:00+08:00[Asia/Shanghai]
//   ...
```

## 构建器 API

```java
// 每天 10:30
CronBuilder.every().day().at(10, 30).buildExpression();           // "30 10 * * *"

// 工作日 9:00
CronBuilder.every().weekdays().at(9, 0).buildExpression();        // "0 9 * * 1-5"

// 每 5 秒（6 字段）
CronBuilder.everySeconds(5).buildExpression();                     // "0/5 * * * * *"

// 每月最后一天 18:00
CronBuilder.create().lastDayOfMonth().at(18, 0).buildExpression(); // "0 18 L * *"

// 每月第 3 个周五 10:00
CronBuilder.create().nthDayOfWeek(DayOfWeek.FRIDAY, 3).at(10, 0).buildExpression(); // "0 10 * * 5#3"

// 日期/月份范围
CronBuilder.create().dayOfMonthRange(10, 20).at(9, 0).buildExpression(); // "0 9 10-20 * *"
CronBuilder.create().monthRange(3, 9).at(9, 0).buildExpression();        // "0 9 * 3-9 *"

// 可配置搜索窗口（仅 2 月 29 日，搜索最多 10 年）
CronExpression rare = CronExpression.parse("0 0 29 2 *");
ZonedDateTime nextLeap = rare.nextExecution(ZonedDateTime.now(), 10);
```

## API 方法参考

### OpenCron（门面类）

| 方法 | 说明 |
|------|------|
| `parse(expr)` | 解析 Cron 表达式 |
| `isValid(expr)` | 检查表达式是否有效 |
| `validate(expr)` | 验证，无效时抛出异常 |
| `validate(expr, minInterval)` | 验证并检查最小间隔 |
| `nextExecution(expr, from)` | 下次执行时间 |
| `nextExecution(expr, from, filter)` | 满足过滤条件的下次执行 |
| `nextExecutions(expr, from, count)` | 下 N 次执行时间 |
| `previousExecution(expr, from)` | 上次执行时间 |
| `previousExecution(expr, from, filter)` | 满足过滤条件的上次执行 |
| `previousExecutions(expr, from, count)` | 前 N 次执行时间 |
| `stream(expr, from)` | 正向惰性流 |
| `reverseStream(expr, from)` | 反向惰性流 |
| `timeToNextExecution(expr, from)` | 距下次执行的 Duration |
| `timeFromLastExecution(expr, from)` | 距上次执行的 Duration |
| `getEstimatedInterval(expr)` | 预估执行间隔 |
| `countExecutionsBetween(expr, from, to)` | 区间执行次数 |
| `executionsBetween(expr, from, to)` | 区间执行列表 |
| `isEquivalent(expr1, expr2)` | 结构等价检测 |
| `nextOverlap(expr1, expr2, from)` | 下次同时触发时间 |
| `hasOverlap(expr1, expr2, from, to)` | 区间内是否重叠 |
| `explain(expr, from)` | 一站式调试信息 |
| `describe(expr)` | 英文描述 |
| `describe(expr, locale)` | 本地化描述 |
| `builder()` | 创建构建器 |

### CronExpression

| 方法 | 说明 |
|------|------|
| `parse(expr)` | 解析表达式（静态工厂） |
| `matches(time)` | 检查时间是否匹配 |
| `nextExecution(from)` | 下次执行（4 年窗口） |
| `nextExecution(from, maxYears)` | 下次执行（自定义窗口） |
| `nextExecution(from, filter)` | 满足过滤条件的下次执行 |
| `nextExecutions(from, count)` | 下 N 次执行 |
| `previousExecution(from)` | 上次执行（4 年窗口） |
| `previousExecution(from, maxYears)` | 上次执行（自定义窗口） |
| `previousExecution(from, filter)` | 满足过滤条件的上次执行 |
| `previousExecutions(from, count)` | 前 N 次执行 |
| `stream(from)` | 正向惰性执行流 |
| `reverseStream(from)` | 反向惰性执行流 |
| `timeToNextExecution(from)` | 距下次执行的 Duration |
| `timeFromLastExecution(from)` | 距上次执行的 Duration |
| `countExecutionsBetween(from, to)` | 区间内执行次数 |
| `executionsBetween(from, to)` | 区间内执行列表（上限 100K） |
| `executionsBetween(from, to, limit)` | 区间内执行列表（自定义上限） |
| `isEquivalentTo(other)` | 结构等价检测 |
| `nextOverlap(other, from)` | 下次重叠时间 |
| `hasOverlapBetween(other, from, to)` | 区间内是否重叠 |
| `explain(from)` | 调试信息 |
| `describe()` | 英文描述 |
| `describe(locale)` | 本地化描述 |
| `adjustInto(temporal)` | TemporalAdjuster（通过 `.with()` 使用） |
| `getExpression()` | 原始表达式字符串 |
| `hasSeconds()` | 是否为 6 字段格式 |

## 环境要求

- Java 25+

## 开源许可

Apache License 2.0
