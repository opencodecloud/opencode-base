/**
 * OpenCode Base Lunar Module
 * OpenCode 基础农历模块
 *
 * <p>Provides Chinese Lunar Calendar utilities including date conversion,
 * solar terms, zodiac, GanZhi, and traditional festival support.</p>
 * <p>提供中国农历工具，包括日期转换、节气、生肖、干支和传统节日支持。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Solar/Lunar date conversion (1900-2100) - 公农历转换</li>
 *   <li>24 Solar Terms calculation - 24节气计算</li>
 *   <li>Chinese Zodiac (12 animals) - 十二生肖</li>
 *   <li>Western Constellation - 十二星座</li>
 *   <li>GanZhi (Heavenly Stems &amp; Earthly Branches) - 天干地支</li>
 *   <li>WuXing (Five Elements) - 五行</li>
 *   <li>Traditional Festivals - 传统节日</li>
 *   <li>Memorial Days - 纪念日</li>
 *   <li>YiJi Divination (Auspicious/Inauspicious) - 黄历宜忌</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
module cloud.opencode.base.lunar {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.lunar;
    exports cloud.opencode.base.lunar.calendar;
    exports cloud.opencode.base.lunar.divination;
    exports cloud.opencode.base.lunar.element;
    exports cloud.opencode.base.lunar.exception;
    exports cloud.opencode.base.lunar.ganzhi;
    exports cloud.opencode.base.lunar.spi;
    exports cloud.opencode.base.lunar.zodiac;

    // SPI: Allow ServiceLoader to find provider implementations
    uses cloud.opencode.base.lunar.spi.CalendarProvider;
    uses cloud.opencode.base.lunar.spi.FestivalProvider;
    uses cloud.opencode.base.lunar.spi.DivinationProvider;
}
