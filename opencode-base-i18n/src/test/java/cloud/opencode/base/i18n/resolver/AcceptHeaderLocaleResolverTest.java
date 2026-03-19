package cloud.opencode.base.i18n.resolver;

import cloud.opencode.base.i18n.spi.LocaleResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * AcceptHeaderLocaleResolver 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("AcceptHeaderLocaleResolver 测试")
class AcceptHeaderLocaleResolverTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用headerSupplier创建解析器")
        void testConstructorWithSupplier() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(() -> "zh-CN");

            assertThat(resolver.getDefaultLocale()).isEqualTo(Locale.getDefault());
            assertThat(resolver.getSupportedLocales()).isNull();
        }

        @Test
        @DisplayName("使用headerSupplier和默认Locale创建解析器")
        void testConstructorWithDefaultLocale() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(
                    () -> "en-US", Locale.CHINESE
            );

            assertThat(resolver.getDefaultLocale()).isEqualTo(Locale.CHINESE);
        }

        @Test
        @DisplayName("使用完整参数创建解析器")
        void testFullConstructor() {
            Set<Locale> supported = Set.of(Locale.ENGLISH, Locale.CHINESE);
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(
                    () -> "en", Locale.ENGLISH, supported
            );

            assertThat(resolver.getDefaultLocale()).isEqualTo(Locale.ENGLISH);
            assertThat(resolver.getSupportedLocales()).containsExactlyInAnyOrderElementsOf(supported);
        }

        @Test
        @DisplayName("null默认Locale使用系统默认")
        void testNullDefaultLocale() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(
                    () -> "en", null
            );

            assertThat(resolver.getDefaultLocale()).isEqualTo(Locale.getDefault());
        }
    }

    @Nested
    @DisplayName("resolve方法测试")
    class ResolveTests {

        @Test
        @DisplayName("解析简单语言标签")
        void testResolveSimple() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(() -> "en");

            assertThat(resolver.resolve()).isEqualTo(Locale.ENGLISH);
        }

        @Test
        @DisplayName("解析带地区的语言标签")
        void testResolveWithCountry() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(() -> "zh-CN");

            assertThat(resolver.resolve()).isEqualTo(Locale.forLanguageTag("zh-CN"));
        }

        @Test
        @DisplayName("解析带质量值的头")
        void testResolveWithQuality() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(
                    () -> "en;q=0.8,zh-CN;q=0.9"
            );

            Locale resolved = resolver.resolve();
            assertThat(resolved).isEqualTo(Locale.forLanguageTag("zh-CN"));
        }

        @Test
        @DisplayName("空头返回默认Locale")
        void testResolveEmptyHeader() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(
                    () -> "", Locale.FRENCH
            );

            assertThat(resolver.resolve()).isEqualTo(Locale.FRENCH);
        }

        @Test
        @DisplayName("null头返回默认Locale")
        void testResolveNullHeader() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(
                    () -> null, Locale.GERMAN
            );

            assertThat(resolver.resolve()).isEqualTo(Locale.GERMAN);
        }

        @Test
        @DisplayName("带支持Locale的解析")
        void testResolveWithSupportedLocales() {
            Set<Locale> supported = Set.of(Locale.ENGLISH, Locale.FRENCH);
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(
                    () -> "zh-CN,en;q=0.8", Locale.ENGLISH, supported
            );

            assertThat(resolver.resolve()).isEqualTo(Locale.ENGLISH);
        }

        @Test
        @DisplayName("语言匹配支持的Locale")
        void testResolveLanguageMatch() {
            Set<Locale> supported = Set.of(Locale.US);
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(
                    () -> "en-GB", Locale.FRENCH, supported
            );

            assertThat(resolver.resolve()).isEqualTo(Locale.US);
        }

        @Test
        @DisplayName("无匹配时返回默认Locale")
        void testResolveNoMatch() {
            Set<Locale> supported = Set.of(Locale.FRENCH);
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(
                    () -> "zh-CN", Locale.ENGLISH, supported
            );

            assertThat(resolver.resolve()).isEqualTo(Locale.ENGLISH);
        }
    }

    @Nested
    @DisplayName("parseAcceptLanguage方法测试")
    class ParseAcceptLanguageTests {

        @Test
        @DisplayName("解析单个语言")
        void testParseSingle() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(() -> "");
            List<AcceptHeaderLocaleResolver.LocaleQuality> result = resolver.parseAcceptLanguage("en");

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().locale()).isEqualTo(Locale.ENGLISH);
            assertThat(result.getFirst().quality()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("解析多个语言")
        void testParseMultiple() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(() -> "");
            List<AcceptHeaderLocaleResolver.LocaleQuality> result =
                    resolver.parseAcceptLanguage("en,zh-CN,fr");

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("解析带质量值")
        void testParseWithQuality() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(() -> "");
            List<AcceptHeaderLocaleResolver.LocaleQuality> result =
                    resolver.parseAcceptLanguage("en;q=0.5,zh-CN;q=0.9");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).quality()).isEqualTo(0.5);
            assertThat(result.get(1).quality()).isEqualTo(0.9);
        }

        @Test
        @DisplayName("忽略无效语言标签")
        void testParseInvalidTags() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(() -> "");
            List<AcceptHeaderLocaleResolver.LocaleQuality> result =
                    resolver.parseAcceptLanguage("123,en");

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().locale()).isEqualTo(Locale.ENGLISH);
        }
    }

    @Nested
    @DisplayName("setLocale方法测试")
    class SetLocaleTests {

        @Test
        @DisplayName("setLocale抛出UnsupportedOperationException")
        void testSetLocaleThrows() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(() -> "en");

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
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(() -> "en");

            assertThatCode(() -> resolver.reset()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("LocaleQuality记录测试")
    class LocaleQualityTests {

        @Test
        @DisplayName("LocaleQuality属性访问")
        void testLocaleQualityProperties() {
            AcceptHeaderLocaleResolver.LocaleQuality lq =
                    new AcceptHeaderLocaleResolver.LocaleQuality(Locale.ENGLISH, 0.8);

            assertThat(lq.locale()).isEqualTo(Locale.ENGLISH);
            assertThat(lq.quality()).isEqualTo(0.8);
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现LocaleResolver接口")
        void testImplementsInterface() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(() -> "en");

            assertThat(resolver).isInstanceOf(LocaleResolver.class);
        }
    }
}
