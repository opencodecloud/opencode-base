# OpenCode Base Cron

面向 JDK 25+ 的 Cron 表达式解析、构建、验证、调度和人类可读描述库。

## 功能特性

- 解析 5 字段和 6 字段（含秒）Cron 表达式
- 宏支持：`@yearly`、`@monthly`、`@weekly`、`@daily`、`@hourly`
- 名称别名：`MON-FRI`、`JAN-DEC`（不区分大小写）
- 特殊字符：`L`（最后）、`W`（工作日）、`#`（第 N 个）、范围回绕
- 正向和反向调度（计算下次/上次执行时间）
- 生成人类可读的英文描述
- 流式构建器 API，带输入校验
- 最小间隔验证
- 线程安全，空值安全

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-cron</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API 概览

| 类名 | 说明 |
|------|------|
| `OpenCron` | 门面类，提供所有 Cron 操作的统一入口 |
| `CronExpression` | 不可变的已解析 Cron 表达式，支持匹配、调度和描述 |
| `CronBuilder` | 流式构建器 API，带输入校验，用于构建 Cron 表达式 |
| `CronValidator` | 验证工具，支持语法检查和最小间隔强制 |
| `CronDescriber` | 从已解析表达式生成人类可读的英文描述 |
| `CronField` | 枚举，定义每个 Cron 字段的有效范围、显示名称和别名 |
| `CronMacro` | 将预定义宏（`@daily`、`@yearly` 等）解析为 Cron 表达式 |
| `OpenCronException` | 运行时异常，携带诊断上下文（表达式、字段名称） |

## 快速开始

```java
import cloud.opencode.base.cron.OpenCron;
import cloud.opencode.base.cron.CronExpression;
import java.time.ZonedDateTime;
import java.util.List;

// 解析并查询下次执行时间
ZonedDateTime next = OpenCron.nextExecution("0 9 * * MON-FRI", ZonedDateTime.now());

// 获取下 5 次执行时间
List<ZonedDateTime> times = OpenCron.nextExecutions("30 10 * * *", ZonedDateTime.now(), 5);

// 验证表达式
boolean valid = OpenCron.isValid("0 0 L * *");

// 人类可读描述
String desc = OpenCron.describe("0 9 * * MON-FRI"); // "At 09:00, Monday through Friday"

// 构建器 API
CronExpression expr = OpenCron.builder().weekdays().at(9, 0).build();

// 宏支持
CronExpression daily = OpenCron.parse("@daily");

// 每 5 秒执行（6 字段格式）
String cron = CronBuilder.everySeconds(5).buildExpression(); // "0/5 * * * * *"
```

## 环境要求

- Java 25+

## 开源许可

Apache License 2.0
