# OpenCode Base Lunar

**中国农历库 - 公历农历转换、节气、八字、干支、纳音、节日、生肖，适用于 JDK 25+**

`opencode-base-lunar` 是一个全面的中国农历库，提供公历农历双向转换、24 节气、八字四柱、天干地支、纳音五行、传统节日、十二生肖、星座和五行计算。

## 功能特性

- **公历农历转换**：公历与农历之间的双向转换（1900-2100）
- **LunarDate API**：`Comparable`、`plusDays`/`minusDays`/`daysUntil`、`of()`/`from()` 工厂方法
- **LunarYear / LunarMonth**：年月级信息（总天数、闰月、月份列表）
- **24 节气**：节气计算，`Optional` 返回值，`SolarTermInfo`，`getClosest()`
- **八字（四柱）**：年柱/月柱/日柱/时柱计算（以立春为界）
- **干支系统**：年/月/日/时干支，月干支以节气为界
- **纳音五行**：六十甲子纳音查询
- **传统节日**：公历+农历节日，动态除夕（适应腊月29/30天）
- **十二生肖**：12 生肖周期计算
- **星座查询**：按日期查询西方星座
- **五行（WuXing）**：相生相克关系、方位颜色关联
- **黄道吉日**：传统黄道吉日计算
- **SPI 扩展**：可插拔的历法、节日和占卜提供者

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-lunar</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.lunar.*;
import cloud.opencode.base.lunar.ganzhi.*;
import cloud.opencode.base.lunar.calendar.*;
import java.time.LocalDate;

// 公历转农历
LunarDate lunar = OpenLunar.solarToLunar(LocalDate.of(2024, 2, 10));
System.out.println(lunar.format());  // 甲辰年 正月初一

// 工厂方法与日期算术
LunarDate d = LunarDate.of(2024, 1, 1);
LunarDate d2 = d.plusDays(30);
long days = d.daysUntil(d2);  // 30

// 农历年/月信息
LunarYear year = LunarYear.of(2024);
System.out.println(year.getName());       // 甲辰年
System.out.println(year.hasLeapMonth());  // false
List<LunarMonth> months = year.getMonths();

// 八字（四柱）
BaZi bazi = OpenLunar.getBaZi(LocalDate.of(2024, 2, 10), 14);
System.out.println(bazi.format());           // 甲辰 丙寅 壬午 丁未
System.out.println(bazi.getDayMaster());     // WATER

// 月干支（以节气为界）
GanZhi monthGZ = OpenLunar.getMonthGanZhi(LocalDate.of(2024, 3, 15));

// 纳音
NaYin nayin = GanZhi.ofYear(2024).getNaYin();
System.out.println(nayin.getName());  // 覆灯火

// 节气（Optional 返回值）
Optional<SolarTerm> term = OpenLunar.getSolarTerm(LocalDate.of(2024, 2, 4));
List<SolarTermInfo> terms = OpenLunar.getSolarTerms(2024);

// 节日（年份感知除夕）
List<Festival> festivals = OpenLunar.getFestivals(LocalDate.of(2024, 2, 10));

// 生肖与星座
Zodiac zodiac = OpenLunar.getZodiac(2024);  // DRAGON（龙）
Constellation constellation = OpenLunar.getConstellation(3, 15);  // PISCES（双鱼座）
```

## 类参考

| 类名 | 说明 |
|------|------|
| `OpenLunar` | 主门面 — 所有农历操作的静态工具类 |
| `LunarDate` | 农历日期不可变记录（`Comparable`、日期算术） |
| `SolarDate` | 公历日期不可变记录（LocalDate 互操作） |
| `LunarYear` | 农历年信息（总天数、月份列表、闰月、干支） |
| `LunarMonth` | 农历月信息（天数、名称、导航） |
| `BaZi` | 八字四柱（年柱/月柱/日柱/时柱） |
| `GanZhi` | 天干地支组合（年/月/日/时） |
| `NaYin` | 六十甲子纳音五行 |
| `Gan` | 天干枚举 |
| `Zhi` | 地支枚举 |
| `SolarTerm` | 24 节气计算和查询 |
| `SolarTermInfo` | 节气+日期组合 |
| `Festival` | 传统节日定义和查询 |
| `Holiday` | 法定节假日信息 |
| `WuXing` | 五行系统 |
| `Zodiac` | 十二生肖计算 |
| `Constellation` | 西方星座查询 |
| `AuspiciousDay` | 传统黄道吉日判定 |
| `TimeSlot` | 传统时辰划分 |
| `YiJi` | 传统宜忌活动查询 |
| `MemorialDay` | 纪念日定义 |
| `DateConverter` | 公历农历日期转换工具 |
| `LunarException` | 基础异常（继承自 `OpenException`） |

## 环境要求

- Java 25+（使用记录类）
- 无外部依赖（仅依赖 `opencode-base-core`）

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
