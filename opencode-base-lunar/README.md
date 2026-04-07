# OpenCode Base Lunar

**Chinese lunar calendar library for JDK 25+ — date conversion, solar terms, BaZi, GanZhi, festivals, zodiac, NaYin**

`opencode-base-lunar` is a comprehensive Chinese lunar calendar library providing solar-lunar date conversion, 24 solar terms, BaZi (Four Pillars), GanZhi (Heavenly Stems and Earthly Branches), NaYin, traditional festivals, Chinese zodiac, constellations, and Five Elements calculations.

## Features

- **Solar-Lunar Conversion**: Bidirectional conversion between Gregorian and Chinese lunar dates (1900-2100)
- **LunarDate API**: `Comparable`, `plusDays`/`minusDays`/`daysUntil`, `of()`/`from()` factory methods
- **LunarYear / LunarMonth**: Year and month level information (total days, leap month, month listing)
- **24 Solar Terms**: Solar term calculation with `Optional` return, `SolarTermInfo`, `getClosest()`
- **BaZi (Four Pillars)**: Year/month/day/hour pillar calculation with 立春 boundary
- **GanZhi System**: Year/month/day/hour stems & branches, month uses solar term boundaries
- **NaYin (纳音)**: 60 JiaZi NaYin Five Elements lookup
- **Traditional Festivals**: Solar + lunar festivals, dynamic 除夕 (adapts to 29/30 day month 12)
- **Chinese Zodiac**: 12-animal zodiac cycle
- **Constellations**: Western zodiac constellation lookup
- **Five Elements (WuXing)**: Generating/overcoming relationships, direction/color associations
- **Auspicious Day**: Traditional Chinese auspicious day calculation
- **SPI Extension**: Pluggable providers for calendar, festival, and divination

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-lunar</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.lunar.*;
import cloud.opencode.base.lunar.ganzhi.*;
import cloud.opencode.base.lunar.calendar.*;
import java.time.LocalDate;

// Convert solar to lunar
LunarDate lunar = OpenLunar.solarToLunar(LocalDate.of(2024, 2, 10));
System.out.println(lunar.format());  // 甲辰年 正月初一

// Factory methods and date arithmetic
LunarDate d = LunarDate.of(2024, 1, 1);
LunarDate d2 = d.plusDays(30);
long days = d.daysUntil(d2);  // 30

// LunarYear / LunarMonth info
LunarYear year = LunarYear.of(2024);
System.out.println(year.getName());       // 甲辰年
System.out.println(year.hasLeapMonth());  // false
List<LunarMonth> months = year.getMonths();

// BaZi (Four Pillars)
BaZi bazi = OpenLunar.getBaZi(LocalDate.of(2024, 2, 10), 14);
System.out.println(bazi.format());           // 甲辰 丙寅 壬午 丁未
System.out.println(bazi.getDayMaster());     // WATER

// GanZhi with solar term boundaries
GanZhi monthGZ = OpenLunar.getMonthGanZhi(LocalDate.of(2024, 3, 15));
GanZhi dayGZ = OpenLunar.getDayGanZhi(LocalDate.of(2024, 2, 10));

// NaYin (纳音)
NaYin nayin = GanZhi.ofYear(2024).getNaYin();
System.out.println(nayin.getName());  // 覆灯火

// Solar terms (Optional return)
Optional<SolarTerm> term = OpenLunar.getSolarTerm(LocalDate.of(2024, 2, 4));
List<SolarTermInfo> terms = OpenLunar.getSolarTerms(2024);

// Festivals (year-aware 除夕)
List<Festival> festivals = OpenLunar.getFestivals(LocalDate.of(2024, 2, 10));

// Zodiac & Constellation
Zodiac zodiac = OpenLunar.getZodiac(2024);  // DRAGON
Constellation constellation = OpenLunar.getConstellation(3, 15);  // PISCES
```

## Class Reference

| Class | Description |
|-------|-------------|
| `OpenLunar` | Main facade — static utility for all lunar calendar operations |
| `LunarDate` | Immutable record for lunar date (`Comparable`, date arithmetic) |
| `SolarDate` | Immutable record for solar date (LocalDate interop) |
| `LunarYear` | Lunar year information (total days, months, leap month, GanZhi) |
| `LunarMonth` | Lunar month information (days, name, navigation) |
| `BaZi` | Four Pillars of Destiny (year/month/day/hour pillars) |
| `GanZhi` | Heavenly Stems + Earthly Branches (year/month/day/hour) |
| `NaYin` | 60 JiaZi NaYin Five Elements |
| `Gan` | Heavenly Stems (天干) enumeration |
| `Zhi` | Earthly Branches (地支) enumeration |
| `SolarTerm` | 24 solar terms calculation and lookup |
| `SolarTermInfo` | Solar term + date combination |
| `Festival` | Traditional festival definitions and lookup |
| `Holiday` | Legal holiday information |
| `WuXing` | Five Elements system |
| `Zodiac` | Chinese zodiac (12-animal cycle) |
| `Constellation` | Western zodiac constellation lookup |
| `AuspiciousDay` | Traditional auspicious day determination |
| `TimeSlot` | Traditional time period (时辰) divisions |
| `YiJi` | Suitable/unsuitable activities for a day |
| `MemorialDay` | Memorial and commemorative day definitions |
| `DateConverter` | Solar-lunar date conversion utility |
| `LunarException` | Base exception (extends `OpenException`) |

## Requirements

- Java 25+ (uses records)
- No external dependencies (only depends on `opencode-base-core`)

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
