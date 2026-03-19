# OpenCode Base Lunar

**Lunar calendar utilities - solar/lunar conversion, solar terms, festivals, zodiac, GanZhi for JDK 25+**

`opencode-base-lunar` is a comprehensive Chinese lunar calendar library providing solar-lunar date conversion, 24 solar terms, traditional festivals, Chinese zodiac, constellations, GanZhi (Heavenly Stems and Earthly Branches), and Five Elements calculations.

## Features

- **Solar-Lunar Conversion**: Bidirectional conversion between Gregorian and Chinese lunar dates
- **24 Solar Terms**: Full solar term calculation with date lookup and next-term queries
- **Traditional Festivals**: Both solar and lunar festival detection (Spring Festival, Mid-Autumn, etc.)
- **Chinese Zodiac**: 12-animal zodiac cycle calculation
- **Constellations**: Western zodiac constellation lookup by date
- **GanZhi System**: Heavenly Stems and Earthly Branches for year, month, and day
- **Five Elements (WuXing)**: Traditional Five Elements system
- **Auspicious Day**: Traditional Chinese auspicious day calculation
- **Date Formatting**: Chinese-style date formatting with traditional characters
- **SPI Extension**: Pluggable providers for calendar, festival, and divination

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-lunar</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.lunar.OpenLunar;
import cloud.opencode.base.lunar.LunarDate;
import java.time.LocalDate;

// Convert solar to lunar
LunarDate lunar = OpenLunar.solarToLunar(LocalDate.of(2024, 2, 10));
System.out.println(lunar.format());  // 甲辰年 正月初一

// Get today's lunar date
LunarDate today = OpenLunar.today();

// Convert lunar to solar
SolarDate solar = OpenLunar.lunarToSolar(2024, 1, 1, false);

// Get zodiac
Zodiac zodiac = OpenLunar.getZodiac(2024);  // DRAGON

// Get constellation
Constellation constellation = OpenLunar.getConstellation(3, 15);  // PISCES

// Get GanZhi
GanZhi ganZhi = OpenLunar.getYearGanZhi(2024);  // 甲辰

// Get solar terms for a year
List<SolarTerm> terms = OpenLunar.getSolarTerms(2024);

// Check festivals
List<Festival> festivals = OpenLunar.getFestivals(LocalDate.of(2024, 2, 10));
boolean isFestival = OpenLunar.isFestival(LocalDate.of(2024, 2, 10));

// Get leap month info
int leapMonth = OpenLunar.getLeapMonth(2024);
```

## Class Reference

| Class | Description |
|-------|-------------|
| `OpenLunar` | Main facade - static utility for all lunar calendar operations |
| `LunarDate` | Immutable record representing a Chinese lunar calendar date |
| `SolarDate` | Immutable record representing a Gregorian solar date |
| `DateConverter` | Solar-lunar date conversion engine |
| `Festival` | Traditional festival definitions and lookup |
| `Holiday` | Holiday information and date checking |
| `SolarTerm` | 24 solar terms calculation and lookup |
| `AuspiciousDay` | Traditional Chinese auspicious day determination |
| `TimeSlot` | Traditional Chinese time period (Shi Chen) divisions |
| `YiJi` | Traditional "suitable/unsuitable" activities for a day |
| `MemorialDay` | Memorial and commemorative day definitions |
| `WuXing` | Five Elements (Metal, Wood, Water, Fire, Earth) system |
| `Gan` | Heavenly Stems (Tian Gan) enumeration |
| `GanZhi` | Combined Heavenly Stems and Earthly Branches representation |
| `Zhi` | Earthly Branches (Di Zhi) enumeration |
| `Constellation` | Western zodiac constellation lookup |
| `Zodiac` | Chinese zodiac (12-animal cycle) calculation |
| `DateConversionException` | Exception for date conversion failures |
| `DateOutOfRangeException` | Exception for dates outside supported range |
| `InvalidLunarDateException` | Exception for invalid lunar date values |
| `LunarErrorCode` | Error code enumeration for lunar exceptions |
| `LunarException` | Base exception for lunar module |
| `LunarCalculator` | Internal lunar date calculation engine |
| `LunarData` | Internal lunar calendar data tables |
| `SolarTermData` | Internal solar term data tables |
| `CalendarProvider` | SPI for custom calendar implementations |
| `DivinationProvider` | SPI for custom divination implementations |
| `FestivalProvider` | SPI for custom festival definitions |

## Requirements

- Java 25+ (uses records, sealed classes)
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
