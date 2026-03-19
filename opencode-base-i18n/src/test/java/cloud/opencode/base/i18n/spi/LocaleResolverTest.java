package cloud.opencode.base.i18n.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * LocaleResolver 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("LocaleResolver 接口测试")
class LocaleResolverTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("是函数式接口")
        void testIsFunctionalInterface() {
            assertThat(LocaleResolver.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
        }

        @Test
        @DisplayName("可以用lambda实现")
        void testLambdaImplementation() {
            LocaleResolver resolver = () -> Locale.FRENCH;

            assertThat(resolver.resolve()).isEqualTo(Locale.FRENCH);
        }

        @Test
        @DisplayName("可以用方法引用实现")
        void testMethodReferenceImplementation() {
            LocaleResolver resolver = Locale::getDefault;

            assertThat(resolver.resolve()).isEqualTo(Locale.getDefault());
        }
    }

    @Nested
    @DisplayName("resolve方法测试")
    class ResolveTests {

        @Test
        @DisplayName("resolve是抽象方法")
        void testResolveIsAbstract() throws NoSuchMethodException {
            var method = LocaleResolver.class.getMethod("resolve");

            assertThat(method.isDefault()).isFalse();
        }

        @Test
        @DisplayName("实现resolve方法")
        void testResolveImplementation() {
            LocaleResolver resolver = new LocaleResolver() {
                @Override
                public Locale resolve() {
                    return Locale.CHINESE;
                }
            };

            assertThat(resolver.resolve()).isEqualTo(Locale.CHINESE);
        }
    }

    @Nested
    @DisplayName("setLocale默认方法测试")
    class SetLocaleTests {

        @Test
        @DisplayName("setLocale是默认方法")
        void testSetLocaleIsDefault() throws NoSuchMethodException {
            var method = LocaleResolver.class.getMethod("setLocale", Locale.class);

            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("默认实现抛出UnsupportedOperationException")
        void testSetLocaleThrows() {
            LocaleResolver resolver = () -> Locale.ENGLISH;

            assertThatThrownBy(() -> resolver.setLocale(Locale.FRENCH))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("not supported");
        }

        @Test
        @DisplayName("可以覆盖setLocale方法")
        void testSetLocaleOverride() {
            var localeHolder = new Locale[]{Locale.ENGLISH};
            LocaleResolver resolver = new LocaleResolver() {
                @Override
                public Locale resolve() {
                    return localeHolder[0];
                }

                @Override
                public void setLocale(Locale locale) {
                    localeHolder[0] = locale;
                }
            };

            resolver.setLocale(Locale.GERMAN);

            assertThat(resolver.resolve()).isEqualTo(Locale.GERMAN);
        }
    }

    @Nested
    @DisplayName("reset默认方法测试")
    class ResetTests {

        @Test
        @DisplayName("reset是默认方法")
        void testResetIsDefault() throws NoSuchMethodException {
            var method = LocaleResolver.class.getMethod("reset");

            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("默认实现不做任何操作")
        void testResetNoOp() {
            LocaleResolver resolver = () -> Locale.ENGLISH;

            assertThatCode(() -> resolver.reset()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("可以覆盖reset方法")
        void testResetOverride() {
            var resetCalled = new boolean[]{false};
            LocaleResolver resolver = new LocaleResolver() {
                @Override
                public Locale resolve() {
                    return Locale.ENGLISH;
                }

                @Override
                public void reset() {
                    resetCalled[0] = true;
                }
            };

            resolver.reset();

            assertThat(resetCalled[0]).isTrue();
        }
    }
}
