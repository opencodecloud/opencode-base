package cloud.opencode.base.i18n.resolver;

import cloud.opencode.base.i18n.spi.LocaleResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * ThreadLocalLocaleResolver 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("ThreadLocalLocaleResolver 测试")
class ThreadLocalLocaleResolverTest {

    private ThreadLocalLocaleResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ThreadLocalLocaleResolver();
        resolver.reset();
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造方法使用系统Locale")
        void testDefaultConstructor() {
            ThreadLocalLocaleResolver r = new ThreadLocalLocaleResolver();

            assertThat(r.getDefaultLocale()).isEqualTo(Locale.getDefault());
        }

        @Test
        @DisplayName("指定默认Locale")
        void testConstructorWithDefaultLocale() {
            ThreadLocalLocaleResolver r = new ThreadLocalLocaleResolver(Locale.FRENCH);

            assertThat(r.getDefaultLocale()).isEqualTo(Locale.FRENCH);
        }

        @Test
        @DisplayName("null默认Locale使用系统Locale")
        void testConstructorWithNullDefaultLocale() {
            ThreadLocalLocaleResolver r = new ThreadLocalLocaleResolver(null);

            assertThat(r.getDefaultLocale()).isEqualTo(Locale.getDefault());
        }

        @Test
        @DisplayName("可继承模式")
        void testInheritableConstructor() {
            ThreadLocalLocaleResolver r = new ThreadLocalLocaleResolver(Locale.GERMAN, true);

            assertThat(r.getDefaultLocale()).isEqualTo(Locale.GERMAN);
        }
    }

    @Nested
    @DisplayName("resolve方法测试")
    class ResolveTests {

        @Test
        @DisplayName("未设置时返回默认Locale")
        void testResolveDefault() {
            Locale result = resolver.resolve();

            assertThat(result).isEqualTo(Locale.getDefault());
        }

        @Test
        @DisplayName("设置后返回设置的Locale")
        void testResolveAfterSet() {
            resolver.setLocale(Locale.KOREAN);

            Locale result = resolver.resolve();

            assertThat(result).isEqualTo(Locale.KOREAN);
        }
    }

    @Nested
    @DisplayName("setLocale方法测试")
    class SetLocaleTests {

        @Test
        @DisplayName("设置Locale")
        void testSetLocale() {
            resolver.setLocale(Locale.JAPANESE);

            assertThat(resolver.resolve()).isEqualTo(Locale.JAPANESE);
            assertThat(resolver.isLocaleSet()).isTrue();
        }

        @Test
        @DisplayName("设置null时移除Locale")
        void testSetNullLocale() {
            resolver.setLocale(Locale.ITALIAN);
            resolver.setLocale(null);

            assertThat(resolver.isLocaleSet()).isFalse();
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("重置后返回默认Locale")
        void testReset() {
            resolver.setLocale(Locale.CHINESE);
            resolver.reset();

            assertThat(resolver.resolve()).isEqualTo(Locale.getDefault());
            assertThat(resolver.isLocaleSet()).isFalse();
        }
    }

    @Nested
    @DisplayName("isLocaleSet方法测试")
    class IsLocaleSetTests {

        @Test
        @DisplayName("初始未设置")
        void testInitiallyNotSet() {
            assertThat(resolver.isLocaleSet()).isFalse();
        }

        @Test
        @DisplayName("设置后为true")
        void testSetThenIsSet() {
            resolver.setLocale(Locale.CANADA);

            assertThat(resolver.isLocaleSet()).isTrue();
        }
    }

    @Nested
    @DisplayName("线程隔离测试")
    class ThreadIsolationTests {

        @Test
        @DisplayName("不同线程有独立的Locale")
        void testThreadIsolation() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Locale> otherThreadLocale = new AtomicReference<>();

            resolver.setLocale(Locale.ENGLISH);

            Thread thread = new Thread(() -> {
                otherThreadLocale.set(resolver.resolve());
                latch.countDown();
            });
            thread.start();
            latch.await();

            assertThat(resolver.resolve()).isEqualTo(Locale.ENGLISH);
            assertThat(otherThreadLocale.get()).isEqualTo(Locale.getDefault());
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现LocaleResolver接口")
        void testImplementsInterface() {
            assertThat(resolver).isInstanceOf(LocaleResolver.class);
        }
    }
}
