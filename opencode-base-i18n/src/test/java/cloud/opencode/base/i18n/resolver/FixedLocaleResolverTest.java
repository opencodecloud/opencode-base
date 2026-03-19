package cloud.opencode.base.i18n.resolver;

import cloud.opencode.base.i18n.spi.LocaleResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * FixedLocaleResolver 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("FixedLocaleResolver 测试")
class FixedLocaleResolverTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造方法使用系统Locale")
        void testDefaultConstructor() {
            FixedLocaleResolver resolver = new FixedLocaleResolver();

            assertThat(resolver.getLocale()).isEqualTo(Locale.getDefault());
        }

        @Test
        @DisplayName("指定Locale")
        void testConstructorWithLocale() {
            FixedLocaleResolver resolver = new FixedLocaleResolver(Locale.FRENCH);

            assertThat(resolver.getLocale()).isEqualTo(Locale.FRENCH);
        }
    }

    @Nested
    @DisplayName("resolve方法测试")
    class ResolveTests {

        @Test
        @DisplayName("始终返回固定Locale")
        void testResolveAlwaysReturnsSame() {
            FixedLocaleResolver resolver = new FixedLocaleResolver(Locale.GERMAN);

            assertThat(resolver.resolve()).isEqualTo(Locale.GERMAN);
            assertThat(resolver.resolve()).isEqualTo(Locale.GERMAN);
            assertThat(resolver.resolve()).isEqualTo(Locale.GERMAN);
        }
    }

    @Nested
    @DisplayName("setLocale方法测试")
    class SetLocaleTests {

        @Test
        @DisplayName("setLocale抛出UnsupportedOperationException")
        void testSetLocaleThrows() {
            FixedLocaleResolver resolver = new FixedLocaleResolver(Locale.ENGLISH);

            assertThatThrownBy(() -> resolver.setLocale(Locale.FRENCH))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("reset无操作")
        void testResetNoOp() {
            FixedLocaleResolver resolver = new FixedLocaleResolver(Locale.ITALIAN);

            resolver.reset(); // Should not throw

            assertThat(resolver.resolve()).isEqualTo(Locale.ITALIAN);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("forLanguageTag创建解析器")
        void testForLanguageTag() {
            FixedLocaleResolver resolver = FixedLocaleResolver.forLanguageTag("zh-CN");

            assertThat(resolver.resolve()).isEqualTo(Locale.forLanguageTag("zh-CN"));
        }

        @Test
        @DisplayName("chinese工厂方法")
        void testChinese() {
            FixedLocaleResolver resolver = FixedLocaleResolver.chinese();

            assertThat(resolver.resolve()).isEqualTo(Locale.CHINESE);
        }

        @Test
        @DisplayName("english工厂方法")
        void testEnglish() {
            FixedLocaleResolver resolver = FixedLocaleResolver.english();

            assertThat(resolver.resolve()).isEqualTo(Locale.ENGLISH);
        }
    }

    @Nested
    @DisplayName("getLocale方法测试")
    class GetLocaleTests {

        @Test
        @DisplayName("获取固定的Locale")
        void testGetLocale() {
            FixedLocaleResolver resolver = new FixedLocaleResolver(Locale.KOREAN);

            assertThat(resolver.getLocale()).isEqualTo(Locale.KOREAN);
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现LocaleResolver接口")
        void testImplementsInterface() {
            FixedLocaleResolver resolver = new FixedLocaleResolver();

            assertThat(resolver).isInstanceOf(LocaleResolver.class);
        }
    }
}
