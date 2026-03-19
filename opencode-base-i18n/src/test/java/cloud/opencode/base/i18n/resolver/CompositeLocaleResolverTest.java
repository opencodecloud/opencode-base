package cloud.opencode.base.i18n.resolver;

import cloud.opencode.base.i18n.spi.LocaleResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * CompositeLocaleResolver 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("CompositeLocaleResolver 测试")
class CompositeLocaleResolverTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用列表创建复合解析器")
        void testConstructorWithList() {
            List<LocaleResolver> resolvers = List.of(
                    new FixedLocaleResolver(Locale.ENGLISH)
            );
            CompositeLocaleResolver composite = new CompositeLocaleResolver(resolvers);

            assertThat(composite.size()).isEqualTo(1);
            assertThat(composite.getDefaultLocale()).isEqualTo(Locale.getDefault());
        }

        @Test
        @DisplayName("使用列表和默认Locale创建复合解析器")
        void testConstructorWithDefaultLocale() {
            List<LocaleResolver> resolvers = List.of(
                    new FixedLocaleResolver(Locale.ENGLISH)
            );
            CompositeLocaleResolver composite = new CompositeLocaleResolver(resolvers, Locale.FRENCH);

            assertThat(composite.getDefaultLocale()).isEqualTo(Locale.FRENCH);
        }

        @Test
        @DisplayName("null默认Locale使用系统默认")
        void testConstructorWithNullDefaultLocale() {
            List<LocaleResolver> resolvers = List.of();
            CompositeLocaleResolver composite = new CompositeLocaleResolver(resolvers, null);

            assertThat(composite.getDefaultLocale()).isEqualTo(Locale.getDefault());
        }
    }

    @Nested
    @DisplayName("resolve方法测试")
    class ResolveTests {

        @Test
        @DisplayName("返回第一个解析器的结果")
        void testResolveFirstResolver() {
            CompositeLocaleResolver composite = new CompositeLocaleResolver(List.of(
                    new FixedLocaleResolver(Locale.GERMAN),
                    new FixedLocaleResolver(Locale.FRENCH)
            ));

            assertThat(composite.resolve()).isEqualTo(Locale.GERMAN);
        }

        @Test
        @DisplayName("解析器返回null时尝试下一个")
        void testResolveSkipsNull() {
            CompositeLocaleResolver composite = new CompositeLocaleResolver(List.of(
                    () -> null,
                    new FixedLocaleResolver(Locale.ITALIAN)
            ));

            assertThat(composite.resolve()).isEqualTo(Locale.ITALIAN);
        }

        @Test
        @DisplayName("解析器抛出异常时尝试下一个")
        void testResolveSkipsExceptions() {
            CompositeLocaleResolver composite = new CompositeLocaleResolver(List.of(
                    () -> { throw new RuntimeException("error"); },
                    new FixedLocaleResolver(Locale.KOREAN)
            ));

            assertThat(composite.resolve()).isEqualTo(Locale.KOREAN);
        }

        @Test
        @DisplayName("所有解析器失败时返回默认Locale")
        void testResolveDefaultWhenAllFail() {
            CompositeLocaleResolver composite = new CompositeLocaleResolver(
                    List.of(() -> null),
                    Locale.JAPANESE
            );

            assertThat(composite.resolve()).isEqualTo(Locale.JAPANESE);
        }

        @Test
        @DisplayName("空解析器列表返回默认Locale")
        void testResolveEmptyResolvers() {
            CompositeLocaleResolver composite = new CompositeLocaleResolver(
                    List.of(),
                    Locale.CHINESE
            );

            assertThat(composite.resolve()).isEqualTo(Locale.CHINESE);
        }
    }

    @Nested
    @DisplayName("setLocale方法测试")
    class SetLocaleTests {

        @Test
        @DisplayName("委托给第一个支持的解析器")
        void testSetLocale() {
            ThreadLocalLocaleResolver threadLocal = new ThreadLocalLocaleResolver();
            CompositeLocaleResolver composite = new CompositeLocaleResolver(List.of(
                    new FixedLocaleResolver(Locale.ENGLISH),
                    threadLocal
            ));

            composite.setLocale(Locale.FRENCH);

            assertThat(threadLocal.resolve()).isEqualTo(Locale.FRENCH);
        }

        @Test
        @DisplayName("无解析器支持时抛出异常")
        void testSetLocaleUnsupported() {
            CompositeLocaleResolver composite = new CompositeLocaleResolver(List.of(
                    new FixedLocaleResolver(Locale.ENGLISH)
            ));

            assertThatThrownBy(() -> composite.setLocale(Locale.FRENCH))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("重置所有解析器")
        void testReset() {
            ThreadLocalLocaleResolver resolver1 = new ThreadLocalLocaleResolver();
            ThreadLocalLocaleResolver resolver2 = new ThreadLocalLocaleResolver();
            resolver1.setLocale(Locale.FRENCH);
            resolver2.setLocale(Locale.GERMAN);

            CompositeLocaleResolver composite = new CompositeLocaleResolver(List.of(resolver1, resolver2));
            composite.reset();

            assertThat(resolver1.isLocaleSet()).isFalse();
            assertThat(resolver2.isLocaleSet()).isFalse();
        }

        @Test
        @DisplayName("忽略reset异常")
        void testResetIgnoresExceptions() {
            LocaleResolver throwingResolver = new LocaleResolver() {
                @Override
                public Locale resolve() {
                    return Locale.ENGLISH;
                }

                @Override
                public void reset() {
                    throw new RuntimeException("reset error");
                }
            };

            CompositeLocaleResolver composite = new CompositeLocaleResolver(List.of(throwingResolver));

            assertThatCode(() -> composite.reset()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("addResolver方法测试")
    class AddResolverTests {

        @Test
        @DisplayName("添加解析器")
        void testAddResolver() {
            CompositeLocaleResolver composite = new CompositeLocaleResolver(List.of());

            composite.addResolver(new FixedLocaleResolver(Locale.ENGLISH));

            assertThat(composite.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("链式添加")
        void testAddResolverChain() {
            CompositeLocaleResolver composite = new CompositeLocaleResolver(List.of())
                    .addResolver(new FixedLocaleResolver(Locale.ENGLISH))
                    .addResolver(new FixedLocaleResolver(Locale.FRENCH));

            assertThat(composite.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用builder构建")
        void testBuilder() {
            CompositeLocaleResolver composite = CompositeLocaleResolver.builder()
                    .add(new FixedLocaleResolver(Locale.ENGLISH))
                    .add(new FixedLocaleResolver(Locale.FRENCH))
                    .defaultLocale(Locale.GERMAN)
                    .build();

            assertThat(composite.size()).isEqualTo(2);
            assertThat(composite.getDefaultLocale()).isEqualTo(Locale.GERMAN);
        }

        @Test
        @DisplayName("使用addFirst在开头添加")
        void testBuilderAddFirst() {
            CompositeLocaleResolver composite = CompositeLocaleResolver.builder()
                    .add(new FixedLocaleResolver(Locale.ENGLISH))
                    .addFirst(new FixedLocaleResolver(Locale.FRENCH))
                    .build();

            assertThat(composite.resolve()).isEqualTo(Locale.FRENCH);
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现LocaleResolver接口")
        void testImplementsInterface() {
            CompositeLocaleResolver composite = new CompositeLocaleResolver(List.of());

            assertThat(composite).isInstanceOf(LocaleResolver.class);
        }
    }
}
