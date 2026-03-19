package cloud.opencode.base.i18n.provider;

import cloud.opencode.base.i18n.spi.MessageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * ResourceBundleProvider 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("ResourceBundleProvider 测试")
class ResourceBundleProviderTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用单个baseName创建")
        void testConstructorWithBaseName() {
            ResourceBundleProvider provider = new ResourceBundleProvider("i18n/messages");

            assertThat(provider).isNotNull();
        }

        @Test
        @DisplayName("使用多个baseNames创建")
        void testConstructorWithBaseNames() {
            ResourceBundleProvider provider = new ResourceBundleProvider(
                    List.of("i18n/messages", "i18n/errors")
            );

            assertThat(provider).isNotNull();
        }

        @Test
        @DisplayName("使用baseName和ClassLoader创建")
        void testConstructorWithClassLoader() {
            ResourceBundleProvider provider = new ResourceBundleProvider(
                    "i18n/messages",
                    Thread.currentThread().getContextClassLoader()
            );

            assertThat(provider).isNotNull();
        }

        @Test
        @DisplayName("使用多个baseNames和ClassLoader创建")
        void testConstructorWithBaseNamesAndClassLoader() {
            ResourceBundleProvider provider = new ResourceBundleProvider(
                    List.of("i18n/messages"),
                    Thread.currentThread().getContextClassLoader()
            );

            assertThat(provider).isNotNull();
        }

        @Test
        @DisplayName("null ClassLoader使用当前线程ClassLoader")
        void testConstructorWithNullClassLoader() {
            ResourceBundleProvider provider = new ResourceBundleProvider(
                    "i18n/messages",
                    null
            );

            assertThat(provider).isNotNull();
        }
    }

    @Nested
    @DisplayName("getMessageTemplate方法测试")
    class GetMessageTemplateTests {

        @Test
        @DisplayName("未找到消息返回空Optional")
        void testGetMessageTemplateNotFound() {
            ResourceBundleProvider provider = new ResourceBundleProvider("nonexistent");

            Optional<String> result = provider.getMessageTemplate("any.key", Locale.ENGLISH);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("从多个baseNames搜索")
        void testGetMessageTemplateMultipleBaseNames() {
            ResourceBundleProvider provider = new ResourceBundleProvider(
                    List.of("nonexistent1", "nonexistent2")
            );

            Optional<String> result = provider.getMessageTemplate("any.key", Locale.ENGLISH);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("containsMessage方法测试")
    class ContainsMessageTests {

        @Test
        @DisplayName("消息不存在返回false")
        void testContainsMessageFalse() {
            ResourceBundleProvider provider = new ResourceBundleProvider("nonexistent");

            boolean result = provider.containsMessage("any.key", Locale.ENGLISH);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getKeys方法测试")
    class GetKeysTests {

        @Test
        @DisplayName("不存在的bundle返回空集合")
        void testGetKeysEmpty() {
            ResourceBundleProvider provider = new ResourceBundleProvider("nonexistent");

            Set<String> keys = provider.getKeys(Locale.ENGLISH);

            assertThat(keys).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSupportedLocales方法测试")
    class GetSupportedLocalesTests {

        @Test
        @DisplayName("获取支持的Locales")
        void testGetSupportedLocales() {
            ResourceBundleProvider provider = new ResourceBundleProvider("nonexistent");

            Set<Locale> locales = provider.getSupportedLocales();

            assertThat(locales).isNotNull();
        }
    }

    @Nested
    @DisplayName("refresh方法测试")
    class RefreshTests {

        @Test
        @DisplayName("刷新缓存不抛出异常")
        void testRefresh() {
            ResourceBundleProvider provider = new ResourceBundleProvider("i18n/messages");

            assertThatCode(() -> provider.refresh()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("setUseCache方法测试")
    class SetUseCacheTests {

        @Test
        @DisplayName("禁用缓存")
        void testSetUseCacheFalse() {
            ResourceBundleProvider provider = new ResourceBundleProvider("i18n/messages");

            provider.setUseCache(false);

            // 验证不抛出异常
            assertThatCode(() -> provider.getMessageTemplate("key", Locale.ENGLISH))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("启用缓存")
        void testSetUseCacheTrue() {
            ResourceBundleProvider provider = new ResourceBundleProvider("i18n/messages");

            provider.setUseCache(true);

            assertThatCode(() -> provider.getMessageTemplate("key", Locale.ENGLISH))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("setDefaultEncoding方法测试")
    class SetDefaultEncodingTests {

        @Test
        @DisplayName("设置默认编码")
        void testSetDefaultEncoding() {
            ResourceBundleProvider provider = new ResourceBundleProvider("i18n/messages");

            assertThatCode(() -> provider.setDefaultEncoding("UTF-8"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("addBaseName方法测试")
    class AddBaseNameTests {

        @Test
        @DisplayName("添加baseName")
        void testAddBaseName() {
            ResourceBundleProvider provider = new ResourceBundleProvider("i18n/messages");

            provider.addBaseName("i18n/errors");

            // 验证不抛出异常
            assertThatCode(() -> provider.getMessageTemplate("key", Locale.ENGLISH))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现MessageProvider接口")
        void testImplementsInterface() {
            ResourceBundleProvider provider = new ResourceBundleProvider("i18n/messages");

            assertThat(provider).isInstanceOf(MessageProvider.class);
        }
    }
}
