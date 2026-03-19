package cloud.opencode.base.date.holiday;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Holiday 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Holiday 测试")
class HolidayTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(date, name) 创建简单假日")
        void testOfDateName() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            Holiday holiday = Holiday.of(date, "New Year");

            assertThat(holiday.getDate()).isEqualTo(date);
            assertThat(holiday.getName()).isEqualTo("New Year");
            assertThat(holiday.getType()).isEqualTo(Holiday.HolidayType.PUBLIC);
            assertThat(holiday.isDayOff()).isTrue();
        }

        @Test
        @DisplayName("of(date, name, type) 创建带类型假日")
        void testOfDateNameType() {
            LocalDate date = LocalDate.of(2024, 12, 25);
            Holiday holiday = Holiday.of(date, "Christmas", Holiday.HolidayType.RELIGIOUS);

            assertThat(holiday.getDate()).isEqualTo(date);
            assertThat(holiday.getName()).isEqualTo("Christmas");
            assertThat(holiday.getType()).isEqualTo(Holiday.HolidayType.RELIGIOUS);
        }

        @Test
        @DisplayName("of(date, name, chineseName, type) 创建双语假日")
        void testOfDateNameChineseNameType() {
            LocalDate date = LocalDate.of(2024, 10, 1);
            Holiday holiday = Holiday.of(date, "National Day", "国庆节", Holiday.HolidayType.PUBLIC);

            assertThat(holiday.getDate()).isEqualTo(date);
            assertThat(holiday.getName()).isEqualTo("National Day");
            assertThat(holiday.getChineseName()).isEqualTo("国庆节");
            assertThat(holiday.getType()).isEqualTo(Holiday.HolidayType.PUBLIC);
        }

        @Test
        @DisplayName("of() null日期抛出异常")
        void testOfNullDate() {
            assertThatThrownBy(() -> Holiday.of(null, "Test"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of() null名称抛出异常")
        void testOfNullName() {
            assertThatThrownBy(() -> Holiday.of(LocalDate.now(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder() 完整构建")
        void testBuilderComplete() {
            LocalDate date = LocalDate.of(2024, 2, 10);
            LocalDate observedDate = LocalDate.of(2024, 2, 12);

            Holiday holiday = Holiday.builder()
                    .date(date)
                    .name("Spring Festival")
                    .chineseName("春节")
                    .type(Holiday.HolidayType.CULTURAL)
                    .observedDate(observedDate)
                    .dayOff(true)
                    .description("Chinese New Year")
                    .build();

            assertThat(holiday.getDate()).isEqualTo(date);
            assertThat(holiday.getName()).isEqualTo("Spring Festival");
            assertThat(holiday.getChineseName()).isEqualTo("春节");
            assertThat(holiday.getType()).isEqualTo(Holiday.HolidayType.CULTURAL);
            assertThat(holiday.getObservedDate()).isEqualTo(observedDate);
            assertThat(holiday.isDayOff()).isTrue();
            assertThat(holiday.getDescription()).isEqualTo("Chinese New Year");
        }

        @Test
        @DisplayName("builder() dayOff默认true")
        void testBuilderDayOffDefault() {
            Holiday holiday = Holiday.builder()
                    .date(LocalDate.now())
                    .name("Test")
                    .build();
            assertThat(holiday.isDayOff()).isTrue();
        }

        @Test
        @DisplayName("builder() dayOff设置false")
        void testBuilderDayOffFalse() {
            Holiday holiday = Holiday.builder()
                    .date(LocalDate.now())
                    .name("Observance")
                    .type(Holiday.HolidayType.OBSERVANCE)
                    .dayOff(false)
                    .build();
            assertThat(holiday.isDayOff()).isFalse();
        }

        @Test
        @DisplayName("builder() type默认PUBLIC")
        void testBuilderTypeDefault() {
            Holiday holiday = Holiday.builder()
                    .date(LocalDate.now())
                    .name("Test")
                    .type(null)
                    .build();
            assertThat(holiday.getType()).isEqualTo(Holiday.HolidayType.PUBLIC);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("getLocalizedName() 偏好中文有中文名")
        void testGetLocalizedNameChineseAvailable() {
            Holiday holiday = Holiday.of(LocalDate.of(2024, 10, 1), "National Day", "国庆节", Holiday.HolidayType.PUBLIC);
            assertThat(holiday.getLocalizedName(true)).isEqualTo("国庆节");
            assertThat(holiday.getLocalizedName(false)).isEqualTo("National Day");
        }

        @Test
        @DisplayName("getLocalizedName() 偏好中文无中文名")
        void testGetLocalizedNameNoChineseName() {
            Holiday holiday = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            assertThat(holiday.getLocalizedName(true)).isEqualTo("New Year");
        }

        @Test
        @DisplayName("getObservedDate() 无调休日期返回原日期")
        void testGetObservedDateNull() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            Holiday holiday = Holiday.of(date, "New Year");
            assertThat(holiday.getObservedDate()).isEqualTo(date);
        }

        @Test
        @DisplayName("getYear() 获取年份")
        void testGetYear() {
            Holiday holiday = Holiday.of(LocalDate.of(2024, 10, 1), "National Day");
            assertThat(holiday.getYear()).isEqualTo(2024);
        }

        @Test
        @DisplayName("getChineseName() null时返回null")
        void testGetChineseNameNull() {
            Holiday holiday = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            assertThat(holiday.getChineseName()).isNull();
        }

        @Test
        @DisplayName("getDescription() null时返回null")
        void testGetDescriptionNull() {
            Holiday holiday = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            assertThat(holiday.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("isOn() 检查日期匹配")
        void testIsOn() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            Holiday holiday = Holiday.of(date, "New Year");

            assertThat(holiday.isOn(date)).isTrue();
            assertThat(holiday.isOn(LocalDate.of(2024, 1, 2))).isFalse();
        }

        @Test
        @DisplayName("isPublicHoliday() 检查公共假日")
        void testIsPublicHoliday() {
            Holiday publicHoliday = Holiday.of(LocalDate.now(), "Public", Holiday.HolidayType.PUBLIC);
            Holiday bankHoliday = Holiday.of(LocalDate.now(), "Bank", Holiday.HolidayType.BANK);

            assertThat(publicHoliday.isPublicHoliday()).isTrue();
            assertThat(bankHoliday.isPublicHoliday()).isFalse();
        }
    }

    @Nested
    @DisplayName("HolidayType枚举测试")
    class HolidayTypeTests {

        @Test
        @DisplayName("所有类型值存在")
        void testAllTypes() {
            assertThat(Holiday.HolidayType.values()).containsExactly(
                    Holiday.HolidayType.PUBLIC,
                    Holiday.HolidayType.BANK,
                    Holiday.HolidayType.RELIGIOUS,
                    Holiday.HolidayType.CULTURAL,
                    Holiday.HolidayType.OBSERVANCE,
                    Holiday.HolidayType.COMPANY
            );
        }

        @Test
        @DisplayName("valueOf() 按名称获取")
        void testValueOf() {
            assertThat(Holiday.HolidayType.valueOf("PUBLIC")).isEqualTo(Holiday.HolidayType.PUBLIC);
            assertThat(Holiday.HolidayType.valueOf("BANK")).isEqualTo(Holiday.HolidayType.BANK);
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 按日期比较")
        void testCompareTo() {
            Holiday h1 = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            Holiday h2 = Holiday.of(LocalDate.of(2024, 10, 1), "National Day");
            Holiday h3 = Holiday.of(LocalDate.of(2024, 1, 1), "元旦");

            assertThat(h1.compareTo(h2)).isLessThan(0);
            assertThat(h2.compareTo(h1)).isGreaterThan(0);
            assertThat(h1.compareTo(h3)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等判断")
        void testEquals() {
            Holiday h1 = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            Holiday h2 = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            Holiday h3 = Holiday.of(LocalDate.of(2024, 1, 1), "元旦");
            Holiday h4 = Holiday.of(LocalDate.of(2024, 1, 2), "New Year");

            assertThat(h1).isEqualTo(h2);
            assertThat(h1).isNotEqualTo(h3); // different name
            assertThat(h1).isNotEqualTo(h4); // different date
            assertThat(h1).isEqualTo(h1);
            assertThat(h1).isNotEqualTo(null);
            assertThat(h1).isNotEqualTo("New Year");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            Holiday h1 = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            Holiday h2 = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            assertThat(h1.hashCode()).isEqualTo(h2.hashCode());
        }

        @Test
        @DisplayName("toString() 基本格式")
        void testToString() {
            Holiday holiday = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            String str = holiday.toString();
            assertThat(str).contains("2024-01-01");
            assertThat(str).contains("New Year");
            assertThat(str).contains("PUBLIC");
        }

        @Test
        @DisplayName("toString() 包含中文名和dayOff标记")
        void testToStringWithChineseNameAndDayOff() {
            Holiday holiday = Holiday.builder()
                    .date(LocalDate.of(2024, 10, 1))
                    .name("National Day")
                    .chineseName("国庆节")
                    .dayOff(true)
                    .build();
            String str = holiday.toString();
            assertThat(str).contains("国庆节");
            assertThat(str).contains("*"); // dayOff marker
        }

        @Test
        @DisplayName("toString() 不含dayOff标记")
        void testToStringNoDayOff() {
            Holiday holiday = Holiday.builder()
                    .date(LocalDate.of(2024, 5, 1))
                    .name("Labor Day")
                    .dayOff(false)
                    .build();
            String str = holiday.toString();
            assertThat(str).doesNotContain("*");
        }
    }
}
