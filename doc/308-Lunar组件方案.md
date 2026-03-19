# Lunar 组件方案

## 1. 组件概述

`opencode-base-lunar` 模块提供完整的中国农历/阴历相关功能，包括公历农历互转、二十四节气、传统节日/法定假日、十二生肖、西方星座、天干地支纪年/纪月/纪日/纪时、五行、黄历宜忌、吉日查找、时辰、纪念日等。支持 1900-2100 年范围，基于 JDK 25，零第三方依赖，全部类不可变或无状态，线程安全。

## 2. 包结构

```
cloud.opencode.base.lunar
├── OpenLunar.java                         # 门面入口类
├── LunarDate.java                         # 农历日期 Record
├── SolarDate.java                         # 公历日期 Record
│
├── calendar/                              # 日历
│   ├── DateConverter.java                 # 公农历转换工具
│   ├── SolarTerm.java                     # 二十四节气枚举
│   ├── Festival.java                      # 节日 Record
│   └── Holiday.java                       # 法定假日 Record
│
├── zodiac/                                # 生肖星座
│   ├── Zodiac.java                        # 十二生肖枚举
│   └── Constellation.java                 # 十二星座枚举
│
├── ganzhi/                                # 干支
│   ├── GanZhi.java                        # 干支 Record（天干+地支组合）
│   ├── Gan.java                           # 十天干枚举
│   └── Zhi.java                           # 十二地支枚举
│
├── element/                               # 传统元素
│   ├── WuXing.java                        # 五行枚举
│   └── MemorialDay.java                   # 纪念日 Record
│
├── divination/                            # 黄历宜忌
│   ├── YiJi.java                          # 宜忌 Record
│   ├── AuspiciousDay.java                 # 吉日查找工具
│   └── TimeSlot.java                      # 时辰枚举
│
├── spi/                                   # SPI 扩展点
│   ├── CalendarProvider.java              # 日历提供者接口
│   ├── FestivalProvider.java              # 节日提供者接口
│   └── DivinationProvider.java            # 黄历提供者接口
│
└── exception/                             # 异常
    ├── LunarException.java                # 异常基类
    ├── LunarErrorCode.java                # 错误码枚举
    ├── DateConversionException.java       # 转换异常
    ├── DateOutOfRangeException.java       # 日期越界异常
    └── InvalidLunarDateException.java     # 无效农历日期异常
```

## 3. 核心 API

### 3.1 OpenLunar

> 农历组件门面入口类，提供公农历转换、节气、节日、生肖、星座、干支、闰月等常用操作的静态便捷方法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static LunarDate solarToLunar(LocalDate date)` | 公历转农历 |
| `static LunarDate solarToLunar(int year, int month, int day)` | 公历转农历（指定年月日） |
| `static SolarDate lunarToSolar(LunarDate lunar)` | 农历转公历 |
| `static SolarDate lunarToSolar(int year, int month, int day, boolean isLeap)` | 农历转公历（指定参数） |
| `static SolarTerm getSolarTerm(LocalDate date)` | 获取指定日期的节气 |
| `static List<SolarTerm> getSolarTerms(int year)` | 获取年份所有节气 |
| `static SolarTerm getNextSolarTerm(LocalDate date)` | 获取下一个节气 |
| `static List<Festival> getFestivals(LocalDate date)` | 获取日期的节日列表（公历+农历） |
| `static boolean isFestival(LocalDate date)` | 判断是否节日 |
| `static Zodiac getZodiac(int year)` | 获取生肖 |
| `static Constellation getConstellation(LocalDate date)` | 获取星座 |
| `static Constellation getConstellation(int month, int day)` | 获取星座（月日） |
| `static GanZhi getYearGanZhi(int year)` | 获取干支年 |
| `static GanZhi getMonthGanZhi(int year, int month)` | 获取干支月 |
| `static GanZhi getDayGanZhi(LocalDate date)` | 获取干支日 |
| `static int getLeapMonth(int year)` | 获取闰月（0表示无闰月） |
| `static int getLunarMonthDays(int year, int month, boolean isLeap)` | 获取农历月天数 |
| `static LunarDate today()` | 获取今日农历 |
| `static boolean isSupported(int year)` | 判断年份是否在支持范围内 |

**示例:**

```java
// 公历转农历
LunarDate lunar = OpenLunar.solarToLunar(LocalDate.of(2024, 2, 10));
System.out.println(lunar.format());  // 甲辰年 正月初一

// 生肖和星座
Zodiac zodiac = OpenLunar.getZodiac(2024);  // DRAGON
Constellation cons = OpenLunar.getConstellation(3, 15);  // PISCES

// 干支纪年
GanZhi ganZhi = OpenLunar.getYearGanZhi(2024);  // 甲辰

// 节气
List<SolarTerm> terms = OpenLunar.getSolarTerms(2024);

// 今日农历
LunarDate today = OpenLunar.today();
```

### 3.2 LunarDate

> 农历日期 Record，包含农历年、月、日及是否闰月，提供生肖、干支、月名、日名、节日、格式化等方法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `LunarDate(int year, int month, int day, boolean isLeapMonth)` | 完整构造 |
| `LunarDate(int year, int month, int day)` | 非闰月构造 |
| `int year()` | 农历年 |
| `int month()` | 农历月（1-12） |
| `int day()` | 农历日 |
| `boolean isLeapMonth()` | 是否闰月 |
| `Zodiac getZodiac()` | 获取生肖 |
| `GanZhi getYearGanZhi()` | 获取干支年 |
| `String getMonthName()` | 获取农历月名（如"正月"、"闰二月"） |
| `String getDayName()` | 获取农历日名（如"初一"、"十五"） |
| `SolarDate toSolar()` | 转换为公历 |
| `List<Festival> getFestivals()` | 获取当日节日 |
| `boolean isFestival()` | 判断是否节日 |
| `String format()` | 格式化（如"甲辰年 正月初一"） |
| `String formatSimple()` | 简单格式化 |

**示例:**

```java
LunarDate lunar = new LunarDate(2024, 1, 1, false);
System.out.println(lunar.format());        // 甲辰年 正月初一
System.out.println(lunar.getZodiac());     // DRAGON
System.out.println(lunar.getMonthName());  // 正月
System.out.println(lunar.getDayName());    // 初一
SolarDate solar = lunar.toSolar();
```

### 3.3 SolarDate

> 公历日期 Record，包含年、月、日，提供与 LocalDate 互转、星座查询、农历转换等方法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `SolarDate(int year, int month, int day)` | 构造 |
| `static SolarDate of(LocalDate date)` | 从 LocalDate 创建 |
| `static SolarDate today()` | 今日公历 |
| `LocalDate toLocalDate()` | 转为 LocalDate |
| `LunarDate toLunar()` | 转为农历 |
| `Constellation getConstellation()` | 获取星座 |
| `boolean isLeapYear()` | 是否闰年 |
| `int getDayOfWeek()` | 获取星期几（1-7） |
| `String getDayOfWeekName()` | 获取星期中文名 |
| `int getDayOfYear()` | 获取年中第几天 |
| `SolarDate plusDays(int days)` | 增加天数 |
| `SolarDate minusDays(int days)` | 减少天数 |
| `String format()` | 格式化（yyyy-MM-dd） |
| `String formatChinese()` | 中文格式化 |

**示例:**

```java
SolarDate solar = SolarDate.of(LocalDate.now());
LunarDate lunar = solar.toLunar();
Constellation cons = solar.getConstellation();
```

### 3.4 DateConverter

> 公农历转换工具类，提供公历与农历之间的转换方法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static LunarDate solarToLunar(int year, int month, int day)` | 公历转农历 |
| `static LunarDate solarToLunar(SolarDate solar)` | SolarDate 转农历 |
| `static LunarDate toLunar(LocalDate date)` | LocalDate 转农历 |
| `static SolarDate lunarToSolar(int year, int month, int day, boolean isLeapMonth)` | 农历转公历 |
| `static SolarDate lunarToSolar(LunarDate lunar)` | LunarDate 转公历 |
| `static LocalDate toLocalDate(LunarDate lunar)` | 农历转 LocalDate |
| `static LunarDate today()` | 今日农历 |

### 3.5 SolarTerm

> 二十四节气枚举，提供节气名称、所在月份、指定年份的日期计算。

枚举值: `XIAO_HAN`(小寒), `DA_HAN`(大寒), `LI_CHUN`(立春), `YU_SHUI`(雨水), `JING_ZHE`(惊蛰), `CHUN_FEN`(春分), `QING_MING`(清明), `GU_YU`(谷雨), `LI_XIA`(立夏), `XIAO_MAN`(小满), `MANG_ZHONG`(芒种), `XIA_ZHI`(夏至), `XIAO_SHU`(小暑), `DA_SHU`(大暑), `LI_QIU`(立秋), `CHU_SHU`(处暑), `BAI_LU`(白露), `QIU_FEN`(秋分), `HAN_LU`(寒露), `SHUANG_JIANG`(霜降), `LI_DONG`(立冬), `XIAO_XUE`(小雪), `DA_XUE`(大雪), `DONG_ZHI`(冬至)

**主要方法:**

| 方法 | 描述 |
|------|------|
| `String getName()` | 获取中文名 |
| `String getEnglishName()` | 获取英文名 |
| `int getTypicalMonth()` | 获取典型所在月份 |
| `LocalDate getDate(int year)` | 获取指定年份的节气日期 |
| `boolean isMajor()` | 是否为中气 |
| `boolean isMinor()` | 是否为节气 |
| `static SolarTerm of(LocalDate date)` | 获取指定日期的节气（null 表示非节气日） |
| `static List<SolarTerm> ofYear(int year)` | 获取年份所有节气 |
| `static SolarTerm next(LocalDate date)` | 获取下一个节气 |
| `static SolarTerm previous(LocalDate date)` | 获取上一个节气 |

**示例:**

```java
List<SolarTerm> terms = SolarTerm.ofYear(2024);
for (SolarTerm term : terms) {
    System.out.println(term.getName() + ": " + term.getDate(2024));
}
SolarTerm next = SolarTerm.next(LocalDate.now());
```

### 3.6 Festival

> 节日 Record，包含中英文名称、节日类型（公历/农历）和日期。预定义了常用公历节日和农历节日。

**预定义常量:**

公历节日: `NEW_YEAR`(元旦), `VALENTINE`(情人节), `WOMEN_DAY`(妇女节), `ARBOR_DAY`(植树节), `LABOR_DAY`(劳动节), `YOUTH_DAY`(青年节), `CHILDREN_DAY`(儿童节), `PARTY_DAY`(建党节), `ARMY_DAY`(建军节), `TEACHERS_DAY`(教师节), `NATIONAL_DAY`(国庆节), `CHRISTMAS`(圣诞节)

农历节日: `SPRING_FESTIVAL`(春节), `LANTERN_FESTIVAL`(元宵节), `DRAGON_HEAD`(龙抬头), `DRAGON_BOAT`(端午节), `QIXI`(七夕节), `GHOST_FESTIVAL`(中元节), `MID_AUTUMN`(中秋节), `DOUBLE_NINTH`(重阳节), `LABA`(腊八节), `LITTLE_NEW_YEAR`(小年), `NEW_YEARS_EVE`(除夕)

**主要方法:**

| 方法 | 描述 |
|------|------|
| `String name()` | 中文名 |
| `String englishName()` | 英文名 |
| `FestivalType type()` | 节日类型（SOLAR/LUNAR） |
| `MonthDay date()` | 日期（月日） |
| `static List<Festival> getSolarFestivals(LocalDate date)` | 获取公历节日 |
| `static List<Festival> getLunarFestivals(int month, int day)` | 获取农历节日 |
| `static List<Festival> getAllSolarFestivals()` | 获取所有公历节日 |
| `static List<Festival> getAllLunarFestivals()` | 获取所有农历节日 |

### 3.7 Holiday

> 法定假日 Record，定义中国法定假日信息。

**预定义常量:** `NEW_YEAR`(元旦), `SPRING_FESTIVAL`(春节), `QINGMING`(清明节), `LABOR_DAY`(劳动节), `DRAGON_BOAT`(端午节), `MID_AUTUMN`(中秋节), `NATIONAL_DAY`(国庆节)

**主要方法:**

| 方法 | 描述 |
|------|------|
| `String name()` | 中文名 |
| `String englishName()` | 英文名 |
| `int days()` | 法定假日天数 |
| `boolean lunar()` | 是否农历节日 |
| `static List<Holiday> getAll()` | 获取所有法定假日 |
| `static int getTotalVacationDays()` | 获取总假日天数 |
| `boolean isLunarBased()` | 是否基于农历 |
| `boolean isSolarBased()` | 是否基于公历 |

### 3.8 Zodiac

> 十二生肖枚举。

枚举值: `RAT`(鼠), `OX`(牛), `TIGER`(虎), `RABBIT`(兔), `DRAGON`(龙), `SNAKE`(蛇), `HORSE`(马), `GOAT`(羊), `MONKEY`(猴), `ROOSTER`(鸡), `DOG`(狗), `PIG`(猪)

**主要方法:**

| 方法 | 描述 |
|------|------|
| `String getName()` | 获取中文名 |
| `String getEnglishName()` | 获取英文名 |
| `String getEarthlyBranch()` | 获取对应地支 |
| `Zhi getZhi()` | 获取对应地支枚举 |
| `static Zodiac of(int year)` | 根据年份获取生肖 |
| `static Zodiac ofIndex(int index)` | 根据索引获取 |
| `int nextYear(int fromYear)` | 获取下一个本生肖年 |
| `int previousYear(int fromYear)` | 获取上一个本生肖年 |
| `boolean isYear(int year)` | 判断是否为本生肖年 |

**示例:**

```java
Zodiac zodiac = Zodiac.of(2024);  // DRAGON
System.out.println(zodiac.getName());  // 龙
int next = zodiac.nextYear(2024);  // 2036
```

### 3.9 Constellation

> 十二星座枚举。

枚举值: `AQUARIUS`(水瓶座), `PISCES`(双鱼座), `ARIES`(白羊座), `TAURUS`(金牛座), `GEMINI`(双子座), `CANCER`(巨蟹座), `LEO`(狮子座), `VIRGO`(处女座), `LIBRA`(天秤座), `SCORPIO`(天蝎座), `SAGITTARIUS`(射手座), `CAPRICORN`(摩羯座)

**主要方法:**

| 方法 | 描述 |
|------|------|
| `String getName()` | 获取中文名 |
| `String getEnglishName()` | 获取英文名 |
| `String getSymbol()` | 获取星座符号 |
| `MonthDay getStartDate()` | 获取起始日期 |
| `MonthDay getEndDate()` | 获取结束日期 |
| `static Constellation of(LocalDate date)` | 根据日期获取星座 |
| `static Constellation of(int month, int day)` | 根据月日获取星座 |
| `boolean contains(MonthDay md)` | 判断日期是否属于此星座 |
| `boolean isDate(LocalDate date)` | 判断日期是否属于此星座 |

**示例:**

```java
Constellation cons = Constellation.of(3, 15);  // PISCES
System.out.println(cons.getName());  // 双鱼座
System.out.println(cons.getSymbol());  // ♓
```

### 3.10 GanZhi

> 干支 Record，表示天干地支组合，用于纪年、纪月、纪日、纪时。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `GanZhi(Gan gan, Zhi zhi)` | 构造 |
| `Gan gan()` | 获取天干 |
| `Zhi zhi()` | 获取地支 |
| `static GanZhi ofYear(int year)` | 获取干支年 |
| `static GanZhi ofMonth(int year, int month)` | 获取干支月 |
| `static GanZhi ofDay(LocalDate date)` | 获取干支日 |
| `static GanZhi ofHour(GanZhi dayGanZhi, int hour)` | 获取干支时 |
| `int getCycleIndex()` | 获取六十甲子索引（0-59） |
| `static GanZhi ofCycleIndex(int index)` | 根据六十甲子索引获取 |
| `GanZhi next()` | 下一个干支 |
| `GanZhi previous()` | 上一个干支 |
| `String getName()` | 获取名称（如"甲辰"） |

**示例:**

```java
GanZhi ganZhi = GanZhi.ofYear(2024);
System.out.println(ganZhi);  // 甲辰

GanZhi dayGanZhi = GanZhi.ofDay(LocalDate.now());
GanZhi hourGanZhi = GanZhi.ofHour(dayGanZhi, 14);
```

### 3.11 Gan

> 十天干枚举。

枚举值: `JIA`(甲), `YI`(乙), `BING`(丙), `DING`(丁), `WU`(戊), `JI`(己), `GENG`(庚), `XIN`(辛), `REN`(壬), `GUI`(癸)

**主要方法:**

| 方法 | 描述 |
|------|------|
| `String getName()` | 获取中文名 |
| `String getElement()` | 获取对应五行 |
| `boolean isYang()` | 是否为阳干 |
| `boolean isYin()` | 是否为阴干 |
| `static Gan of(int index)` | 根据索引获取（0-9） |
| `static Gan ofYear(int year)` | 根据年份获取 |
| `Gan next()` | 下一个天干 |
| `Gan previous()` | 上一个天干 |

### 3.12 Zhi

> 十二地支枚举。

枚举值: `ZI`(子), `CHOU`(丑), `YIN`(寅), `MAO`(卯), `CHEN`(辰), `SI`(巳), `WU`(午), `WEI`(未), `SHEN`(申), `YOU`(酉), `XU`(戌), `HAI`(亥)

**主要方法:**

| 方法 | 描述 |
|------|------|
| `String getName()` | 获取中文名 |
| `String getElement()` | 获取对应五行 |
| `int getHourStart()` | 获取对应时辰起始小时 |
| `int getHourEnd()` | 获取对应时辰结束小时 |
| `Zodiac getZodiac()` | 获取对应生肖 |
| `static Zhi of(int index)` | 根据索引获取（0-11） |
| `static Zhi ofYear(int year)` | 根据年份获取 |
| `static Zhi ofHour(int hour)` | 根据小时获取 |
| `Zhi next()` | 下一个地支 |
| `Zhi previous()` | 上一个地支 |

### 3.13 WuXing

> 五行枚举，表示木、火、土、金、水五种元素及其相生相克关系。

枚举值: `WOOD`(木), `FIRE`(火), `EARTH`(土), `METAL`(金), `WATER`(水)

**主要方法:**

| 方法 | 描述 |
|------|------|
| `String getChinese()` | 获取中文名 |
| `String getColor()` | 获取对应颜色（如木=青） |
| `String getDirection()` | 获取对应方位（如木=东） |
| `WuXing generates()` | 获取所生元素（相生） |
| `WuXing generatedBy()` | 获取生此元素的元素 |
| `WuXing overcomes()` | 获取所克元素（相克） |
| `WuXing overcomeBy()` | 获取克此元素的元素 |
| `static WuXing fromGan(int ganIndex)` | 根据天干索引获取五行 |
| `static WuXing fromZhi(int zhiIndex)` | 根据地支索引获取五行 |

**示例:**

```java
WuXing wood = WuXing.WOOD;
System.out.println(wood.generates());    // FIRE（木生火）
System.out.println(wood.overcomes());    // EARTH（木克土）
System.out.println(wood.getDirection()); // 东
```

### 3.14 YiJi

> 黄历宜忌 Record，包含适宜和忌讳的活动列表。

**常量字段:**

| 常量 | 描述 |
|------|------|
| `JI_SI` | 祭祀 |
| `QI_FU` | 祈福 |
| `JIE_HUN` | 结婚 |
| `QI_JI` | 动土 |
| `AN_CHUANG` | 安床 |
| `ZAO_WU` | 造屋 |
| `RU_ZHAI` | 入宅 |
| `KAI_SHI` | 开市 |
| `AN_ZANG` | 安葬 |
| `XIU_ZENG` | 修造 |
| `ZAI_ZHONG` | 栽种 |
| `MU_YU` | 沐浴 |
| `JIAN_FA` | 剃头 |
| `NA_CAI` | 纳采 |

**主要方法:**

| 方法 | 描述 |
|------|------|
| `List<String> suitable()` | 获取宜列表 |
| `List<String> avoid()` | 获取忌列表 |
| `static YiJi of(LunarDate lunar)` | 根据农历日期获取宜忌 |
| `boolean isSuitable(String activity)` | 判断活动是否宜 |
| `boolean shouldAvoid(String activity)` | 判断活动是否忌 |
| `String getSuitableString()` | 宜列表字符串 |
| `String getAvoidString()` | 忌列表字符串 |

**示例:**

```java
YiJi yiji = YiJi.of(lunarDate);
List<String> suitable = yiji.suitable();   // 宜
List<String> avoid = yiji.avoid();         // 忌
boolean good = yiji.isSuitable(YiJi.JIE_HUN);
```

### 3.15 AuspiciousDay

> 吉日查找工具类，根据黄历规则查找特定活动的吉日。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static LocalDate findNext(String activity, LocalDate from)` | 查找下一个吉日 |
| `static LocalDate findNext(String activity, LocalDate from, int maxDays)` | 查找下一个吉日（限制搜索天数） |
| `static List<LocalDate> findInMonth(String activity, int year, int month)` | 查找月内吉日 |
| `static List<LocalDate> findInRange(String activity, LocalDate from, LocalDate to)` | 查找范围内吉日 |
| `static boolean isAuspicious(String activity, LocalDate date)` | 判断是否吉日 |
| `static boolean shouldAvoid(String activity, LocalDate date)` | 判断是否应避免 |
| `static YiJi getYiJi(LocalDate date)` | 获取日期宜忌 |
| `static YiJi today()` | 获取今日宜忌 |

**示例:**

```java
LocalDate weddingDay = AuspiciousDay.findNext(YiJi.JIE_HUN, LocalDate.now());
List<LocalDate> days = AuspiciousDay.findInMonth(YiJi.KAI_SHI, 2024, 2);
boolean good = AuspiciousDay.isAuspicious(YiJi.RU_ZHAI, LocalDate.now());
```

### 3.16 TimeSlot

> 时辰枚举，表示中国传统十二时辰。

枚举值: `ZI`(子时), `CHOU`(丑时), `YIN`(寅时), `MAO`(卯时), `CHEN`(辰时), `SI`(巳时), `WU`(午时), `WEI`(未时), `SHEN`(申时), `YOU`(酉时), `XU`(戌时), `HAI`(亥时)

**主要方法:**

| 方法 | 描述 |
|------|------|
| `String getChinese()` | 获取中文名 |
| `int getStartHour()` | 获取起始小时 |
| `int getEndHour()` | 获取结束小时 |
| `String getAncientName()` | 获取古名 |
| `String getZodiac()` | 获取对应生肖 |
| `String getFullName()` | 获取完整名称 |
| `String getTimeRange()` | 获取时间范围字符串 |
| `static TimeSlot fromHour(int hour)` | 根据小时获取时辰 |
| `static TimeSlot fromTime(LocalTime time)` | 根据时间获取时辰 |
| `static TimeSlot now()` | 获取当前时辰 |
| `int getIndex()` | 获取索引 |

**示例:**

```java
TimeSlot slot = TimeSlot.now();
System.out.println(slot.getChinese());    // 如"午时"
System.out.println(slot.getTimeRange());  // 如"11:00-13:00"
```

### 3.17 MemorialDay

> 纪念日 Record，用于记录和管理纪念日/周年日。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static MemorialDay of(String name, LocalDate date)` | 创建纪念日 |
| `static MemorialDay of(String name, LocalDate date, String description)` | 创建带描述纪念日 |
| `static MemorialDay ofLunar(String name, LocalDate date)` | 创建农历纪念日 |
| `long yearsSince()` | 距今年数 |
| `long daysUntilNextOccurrence()` | 距下次纪念日天数 |
| `boolean isToday()` | 是否今天 |
| `int getAnniversaryNumber()` | 当前周年数 |
| `LocalDate getNextOccurrence()` | 下次纪念日日期 |

**示例:**

```java
MemorialDay birthday = MemorialDay.of("Birthday", LocalDate.of(1990, 5, 15));
long years = birthday.yearsSince();
long daysUntil = birthday.daysUntilNextOccurrence();
```

### 3.18 异常类

> 农历组件异常体系，所有异常继承自 LunarException。

| 异常类 | 描述 |
|--------|------|
| `LunarException` | 农历异常基类，包含 LunarErrorCode |
| `LunarErrorCode` | 错误码枚举（转换/范围/验证/数据错误） |
| `DateConversionException` | 日期转换异常，提供 `solarToLunar()`/`lunarToSolar()` 工厂方法 |
| `DateOutOfRangeException` | 日期越界异常（支持范围 1900-2100），提供 `getYear()`/`getMinYear()`/`getMaxYear()` |
| `InvalidLunarDateException` | 无效农历日期异常，提供 `invalidLeapMonth()`/`invalidDay()` 工厂方法和 `getYear()`/`getMonth()`/`getDay()`/`isLeap()` |
