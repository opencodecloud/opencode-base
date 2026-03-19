package cloud.opencode.base.i18n.support;

import cloud.opencode.base.i18n.LocaleContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * LocaleContextHolder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("LocaleContextHolder 测试")
class LocaleContextHolderTest {

    @BeforeEach
    void setUp() {
        LocaleContextHolder.reset();
        LocaleContextHolder.setDefaultLocale(Locale.getDefault());
        LocaleContextHolder.setDefaultTimeZone(TimeZone.getDefault());
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.reset();
    }

    @Nested
    @DisplayName("setLocaleContext方法测试")
    class SetLocaleContextTests {

        @Test
        @DisplayName("设置LocaleContext")
        void testSetLocaleContext() {
            LocaleContext context = LocaleContext.of(Locale.FRENCH);

            LocaleContextHolder.setLocaleContext(context);

            assertThat(LocaleContextHolder.getLocaleContext()).isEqualTo(context);
        }

        @Test
        @DisplayName("设置null清除Context")
        void testSetNullContext() {
            LocaleContextHolder.setLocaleContext(LocaleContext.of(Locale.FRENCH));

            LocaleContextHolder.setLocaleContext(null);

            assertThat(LocaleContextHolder.getLocaleContext()).isNull();
        }

        @Test
        @DisplayName("可继承模式设置")
        void testSetLocaleContextInheritable() {
            LocaleContext context = LocaleContext.of(Locale.GERMAN);

            LocaleContextHolder.setLocaleContext(context, true);

            assertThat(LocaleContextHolder.getLocaleContext()).isEqualTo(context);
        }
    }

    @Nested
    @DisplayName("getLocaleContext方法测试")
    class GetLocaleContextTests {

        @Test
        @DisplayName("未设置时返回null")
        void testGetLocaleContextNull() {
            assertThat(LocaleContextHolder.getLocaleContext()).isNull();
        }

        @Test
        @DisplayName("获取设置的Context")
        void testGetLocaleContext() {
            LocaleContext context = LocaleContext.of(Locale.ITALIAN);
            LocaleContextHolder.setLocaleContext(context);

            LocaleContext result = LocaleContextHolder.getLocaleContext();

            assertThat(result).isEqualTo(context);
        }
    }

    @Nested
    @DisplayName("setLocale方法测试")
    class SetLocaleTests {

        @Test
        @DisplayName("设置Locale")
        void testSetLocale() {
            LocaleContextHolder.setLocale(Locale.JAPANESE);

            assertThat(LocaleContextHolder.getLocale()).isEqualTo(Locale.JAPANESE);
        }

        @Test
        @DisplayName("可继承模式设置Locale")
        void testSetLocaleInheritable() {
            LocaleContextHolder.setLocale(Locale.KOREAN, true);

            assertThat(LocaleContextHolder.getLocale()).isEqualTo(Locale.KOREAN);
        }
    }

    @Nested
    @DisplayName("getLocale方法测试")
    class GetLocaleTests {

        @Test
        @DisplayName("未设置时返回默认Locale")
        void testGetLocaleDefault() {
            Locale result = LocaleContextHolder.getLocale();

            assertThat(result).isEqualTo(LocaleContextHolder.getDefaultLocale());
        }

        @Test
        @DisplayName("获取设置的Locale")
        void testGetLocale() {
            LocaleContextHolder.setLocale(Locale.CHINESE);

            Locale result = LocaleContextHolder.getLocale();

            assertThat(result).isEqualTo(Locale.CHINESE);
        }
    }

    @Nested
    @DisplayName("setTimeZone方法测试")
    class SetTimeZoneTests {

        @Test
        @DisplayName("设置TimeZone")
        void testSetTimeZone() {
            TimeZone tz = TimeZone.getTimeZone("America/New_York");

            LocaleContextHolder.setTimeZone(tz);

            assertThat(LocaleContextHolder.getTimeZone()).isEqualTo(tz);
        }

        @Test
        @DisplayName("可继承模式设置TimeZone")
        void testSetTimeZoneInheritable() {
            TimeZone tz = TimeZone.getTimeZone("Europe/London");

            LocaleContextHolder.setTimeZone(tz, true);

            assertThat(LocaleContextHolder.getTimeZone()).isEqualTo(tz);
        }
    }

    @Nested
    @DisplayName("getTimeZone方法测试")
    class GetTimeZoneTests {

        @Test
        @DisplayName("未设置时返回默认TimeZone")
        void testGetTimeZoneDefault() {
            TimeZone result = LocaleContextHolder.getTimeZone();

            assertThat(result).isEqualTo(LocaleContextHolder.getDefaultTimeZone());
        }

        @Test
        @DisplayName("获取设置的TimeZone")
        void testGetTimeZone() {
            TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
            LocaleContextHolder.setTimeZone(tz);

            TimeZone result = LocaleContextHolder.getTimeZone();

            assertThat(result).isEqualTo(tz);
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("重置后返回默认值")
        void testReset() {
            LocaleContextHolder.setLocale(Locale.FRENCH);
            LocaleContextHolder.setTimeZone(TimeZone.getTimeZone("UTC"));

            LocaleContextHolder.reset();

            assertThat(LocaleContextHolder.getLocaleContext()).isNull();
        }
    }

    @Nested
    @DisplayName("默认值设置测试")
    class DefaultValueTests {

        @Test
        @DisplayName("设置默认Locale")
        void testSetDefaultLocale() {
            LocaleContextHolder.setDefaultLocale(Locale.FRENCH);

            assertThat(LocaleContextHolder.getDefaultLocale()).isEqualTo(Locale.FRENCH);
        }

        @Test
        @DisplayName("null默认Locale使用系统默认")
        void testSetNullDefaultLocale() {
            LocaleContextHolder.setDefaultLocale(null);

            assertThat(LocaleContextHolder.getDefaultLocale()).isEqualTo(Locale.getDefault());
        }

        @Test
        @DisplayName("设置默认TimeZone")
        void testSetDefaultTimeZone() {
            TimeZone tz = TimeZone.getTimeZone("UTC");

            LocaleContextHolder.setDefaultTimeZone(tz);

            assertThat(LocaleContextHolder.getDefaultTimeZone()).isEqualTo(tz);
        }

        @Test
        @DisplayName("null默认TimeZone使用系统默认")
        void testSetNullDefaultTimeZone() {
            LocaleContextHolder.setDefaultTimeZone(null);

            assertThat(LocaleContextHolder.getDefaultTimeZone()).isEqualTo(TimeZone.getDefault());
        }
    }

    @Nested
    @DisplayName("线程隔离测试")
    class ThreadIsolationTests {

        @Test
        @DisplayName("不同线程有独立的Context")
        void testThreadIsolation() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Locale> otherThreadLocale = new AtomicReference<>();

            LocaleContextHolder.setLocale(Locale.ENGLISH);

            Thread thread = new Thread(() -> {
                otherThreadLocale.set(LocaleContextHolder.getLocale());
                latch.countDown();
            });
            thread.start();
            latch.await();

            assertThat(LocaleContextHolder.getLocale()).isEqualTo(Locale.ENGLISH);
            assertThat(otherThreadLocale.get()).isEqualTo(LocaleContextHolder.getDefaultLocale());
        }
    }

    @Nested
    @DisplayName("setInheritable方法测试")
    class SetInheritableTests {

        @Test
        @DisplayName("切换到可继承模式")
        void testSetInheritable() {
            LocaleContextHolder.setLocale(Locale.FRENCH);

            LocaleContextHolder.setInheritable(true);

            assertThat(LocaleContextHolder.getLocale()).isEqualTo(Locale.FRENCH);
        }

        @Test
        @DisplayName("无Context时设置不会抛出异常")
        void testSetInheritableNoContext() {
            assertThatCode(() -> LocaleContextHolder.setInheritable(true))
                    .doesNotThrowAnyException();
        }
    }
}
