# OpenCode Base Lunar

**农历工具库 - 公历农历转换、节气、节日、生肖、干支，适用于 JDK 25+**

`opencode-base-lunar` 是一个全面的中国农历库，提供公历农历双向转换、24 节气、传统节日、十二生肖、星座、天干地支和五行计算。

## 功能特性

- **公历农历转换**：公历与农历之间的双向转换
- **24 节气**：完整的节气计算、日期查询和下一节气查询
- **传统节日**：公历和农历节日检测（春节、中秋节等）
- **十二生肖**：12 生肖周期计算
- **星座查询**：按日期查询西方星座
- **干支系统**：年、月、日的天干地支
- **五行（WuXing）**：传统五行系统
- **黄道吉日**：传统黄道吉日计算
- **日期格式化**：使用传统汉字的中式日期格式化
- **SPI 扩展**：可插拔的历法、节日和占卜提供者

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-lunar</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.lunar.OpenLunar;
import cloud.opencode.base.lunar.LunarDate;
import java.time.LocalDate;

// 公历转农历
LunarDate lunar = OpenLunar.solarToLunar(LocalDate.of(2024, 2, 10));
System.out.println(lunar.format());  // 甲辰年 正月初一

// 获取今天的农历日期
LunarDate today = OpenLunar.today();

// 农历转公历
SolarDate solar = OpenLunar.lunarToSolar(2024, 1, 1, false);

// 获取生肖
Zodiac zodiac = OpenLunar.getZodiac(2024);  // DRAGON（龙）

// 获取星座
Constellation constellation = OpenLunar.getConstellation(3, 15);  // PISCES（双鱼座）

// 获取干支
GanZhi ganZhi = OpenLunar.getYearGanZhi(2024);  // 甲辰

// 获取某年所有节气
List<SolarTerm> terms = OpenLunar.getSolarTerms(2024);

// 查询节日
List<Festival> festivals = OpenLunar.getFestivals(LocalDate.of(2024, 2, 10));
boolean isFestival = OpenLunar.isFestival(LocalDate.of(2024, 2, 10));

// 查询闰月信息
int leapMonth = OpenLunar.getLeapMonth(2024);
```

## 类参考

| 类名 | 说明 |
|------|------|
| `OpenLunar` | 主门面 - 所有农历操作的静态工具类 |
| `LunarDate` | 表示农历日期的不可变记录 |
| `SolarDate` | 表示公历日期的不可变记录 |
| `DateConverter` | 公历农历日期转换引擎 |
| `Festival` | 传统节日定义和查询 |
| `Holiday` | 节假日信息和日期检查 |
| `SolarTerm` | 24 节气计算和查询 |
| `AuspiciousDay` | 传统黄道吉日判定 |
| `TimeSlot` | 传统时辰划分 |
| `YiJi` | 传统"宜忌"活动查询 |
| `MemorialDay` | 纪念日定义 |
| `WuXing` | 五行（金、木、水、火、土）系统 |
| `Gan` | 天干枚举 |
| `GanZhi` | 天干地支组合表示 |
| `Zhi` | 地支枚举 |
| `Constellation` | 西方星座查询 |
| `Zodiac` | 十二生肖计算 |
| `DateConversionException` | 日期转换失败异常 |
| `DateOutOfRangeException` | 日期超出支持范围异常 |
| `InvalidLunarDateException` | 无效农历日期值异常 |
| `LunarErrorCode` | 农历异常错误码枚举 |
| `LunarException` | 农历模块基础异常 |
| `LunarCalculator` | 内部农历日期计算引擎 |
| `LunarData` | 内部农历数据表 |
| `SolarTermData` | 内部节气数据表 |
| `CalendarProvider` | 自定义历法实现的 SPI |
| `DivinationProvider` | 自定义占卜实现的 SPI |
| `FestivalProvider` | 自定义节日定义的 SPI |

## 环境要求

- Java 25+（使用记录类、密封类）
- 无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
