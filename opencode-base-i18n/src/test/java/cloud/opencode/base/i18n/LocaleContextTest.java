package cloud.opencode.base.i18n;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.*;

/**
 * LocaleContext 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("LocaleContext 测试")
class LocaleContextTest {

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("仅使用Locale创建上下文")
        void testOfWithLocale() {
            LocaleContext ctx = LocaleContext.of(Locale.CHINESE);

            assertThat(ctx.locale()).isEqualTo(Locale.CHINESE);
            assertThat(ctx.timeZone()).isNull();
        }

        @Test
        @DisplayName("使用Locale和TimeZone创建上下文")
        void testOfWithLocaleAndTimeZone() {
            TimeZone tz = TimeZone.getTimeZone("America/New_York");
            LocaleContext ctx = LocaleContext.of(Locale.US, tz);

            assertThat(ctx.locale()).isEqualTo(Locale.US);
            assertThat(ctx.timeZone()).isEqualTo(tz);
        }

        @Test
        @DisplayName("Locale为null时可以创建")
        void testOfWithNullLocale() {
            LocaleContext ctx = LocaleContext.of(null);

            assertThat(ctx.locale()).isNull();
        }
    }

    @Nested
    @DisplayName("getDefault工厂方法测试")
    class GetDefaultTests {

        @Test
        @DisplayName("获取默认上下文")
        void testGetDefault() {
            LocaleContext ctx = LocaleContext.getDefault();

            assertThat(ctx.locale()).isEqualTo(Locale.getDefault());
            assertThat(ctx.timeZone()).isEqualTo(TimeZone.getDefault());
        }
    }

    @Nested
    @DisplayName("withDefaultTimeZone工厂方法测试")
    class WithDefaultTimeZoneTests {

        @Test
        @DisplayName("创建使用默认时区的上下文")
        void testWithDefaultTimeZone() {
            LocaleContext ctx = LocaleContext.withDefaultTimeZone(Locale.JAPAN);

            assertThat(ctx.locale()).isEqualTo(Locale.JAPAN);
            assertThat(ctx.timeZone()).isEqualTo(TimeZone.getDefault());
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            LocaleContext ctx1 = LocaleContext.of(Locale.ENGLISH, tz);
            LocaleContext ctx2 = LocaleContext.of(Locale.ENGLISH, tz);
            LocaleContext ctx3 = LocaleContext.of(Locale.FRENCH, tz);

            assertThat(ctx1).isEqualTo(ctx2);
            assertThat(ctx1.hashCode()).isEqualTo(ctx2.hashCode());
            assertThat(ctx1).isNotEqualTo(ctx3);
        }

        @Test
        @DisplayName("toString包含信息")
        void testToString() {
            LocaleContext ctx = LocaleContext.of(Locale.KOREAN);

            assertThat(ctx.toString()).contains("LocaleContext");
            assertThat(ctx.toString()).contains("ko");
        }
    }

    @Nested
    @DisplayName("访问器方法测试")
    class AccessorTests {

        @Test
        @DisplayName("获取locale")
        void testLocale() {
            LocaleContext ctx = LocaleContext.of(Locale.GERMAN);

            assertThat(ctx.locale()).isEqualTo(Locale.GERMAN);
        }

        @Test
        @DisplayName("获取timeZone")
        void testTimeZone() {
            TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
            LocaleContext ctx = LocaleContext.of(Locale.CHINA, tz);

            assertThat(ctx.timeZone()).isEqualTo(tz);
        }
    }
}
