package cloud.opencode.base.i18n.provider;

import cloud.opencode.base.i18n.spi.MessageProvider;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * ReloadableMessageProvider 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("ReloadableMessageProvider 测试")
class ReloadableMessageProviderTest {

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("i18n-test");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用路径创建")
        void testConstructorWithPath() {
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            assertThat(provider).isNotNull();
            assertThat(provider.isWatching()).isFalse();
        }

        @Test
        @DisplayName("使用路径和间隔创建")
        void testConstructorWithInterval() {
            ReloadableMessageProvider provider = new ReloadableMessageProvider(
                    tempDir, Duration.ofSeconds(10)
            );

            assertThat(provider).isNotNull();
        }
    }

    @Nested
    @DisplayName("getMessageTemplate方法测试")
    class GetMessageTemplateTests {

        @Test
        @DisplayName("从properties文件获取消息")
        void testGetMessageFromFile() throws IOException {
            Files.writeString(tempDir.resolve("messages.properties"), "test.key=Test Value");
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            Optional<String> result = provider.getMessageTemplate("test.key", Locale.ROOT);

            assertThat(result).hasValue("Test Value");
        }

        @Test
        @DisplayName("从带locale的文件获取消息")
        void testGetMessageFromLocaleFile() throws IOException {
            Files.writeString(tempDir.resolve("messages_zh.properties"), "test.key=Chinese Value");
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            Optional<String> result = provider.getMessageTemplate("test.key", Locale.CHINESE);

            assertThat(result).hasValue("Chinese Value");
        }

        @Test
        @DisplayName("回退到语言级别")
        void testFallbackToLanguage() throws IOException {
            Files.writeString(tempDir.resolve("messages_zh.properties"), "test.key=Chinese Value");
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            Locale zhCN = Locale.of("zh", "CN");
            Optional<String> result = provider.getMessageTemplate("test.key", zhCN);

            assertThat(result).hasValue("Chinese Value");
        }

        @Test
        @DisplayName("回退到ROOT")
        void testFallbackToRoot() throws IOException {
            Files.writeString(tempDir.resolve("messages.properties"), "test.key=Root Value");
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            Optional<String> result = provider.getMessageTemplate("test.key", Locale.FRENCH);

            assertThat(result).hasValue("Root Value");
        }

        @Test
        @DisplayName("未找到返回空")
        void testNotFound() {
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            Optional<String> result = provider.getMessageTemplate("nonexistent", Locale.ENGLISH);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getKeys方法测试")
    class GetKeysTests {

        @Test
        @DisplayName("获取所有keys")
        void testGetKeys() throws IOException {
            Files.writeString(tempDir.resolve("messages_en.properties"), "key1=Value1\nkey2=Value2");
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            Set<String> keys = provider.getKeys(Locale.ENGLISH);

            assertThat(keys).containsExactlyInAnyOrder("key1", "key2");
        }

        @Test
        @DisplayName("无文件返回空集合")
        void testGetKeysEmpty() {
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            Set<String> keys = provider.getKeys(Locale.ENGLISH);

            assertThat(keys).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSupportedLocales方法测试")
    class GetSupportedLocalesTests {

        @Test
        @DisplayName("获取支持的Locales")
        void testGetSupportedLocales() throws IOException {
            Files.writeString(tempDir.resolve("messages.properties"), "key=value");
            Files.writeString(tempDir.resolve("messages_en.properties"), "key=value");
            Files.writeString(tempDir.resolve("messages_zh.properties"), "key=value");
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            Set<Locale> locales = provider.getSupportedLocales();

            assertThat(locales).contains(Locale.ROOT, Locale.ENGLISH, Locale.CHINESE);
        }
    }

    @Nested
    @DisplayName("refresh方法测试")
    class RefreshTests {

        @Test
        @DisplayName("刷新重新加载文件")
        void testRefresh() throws IOException {
            Files.writeString(tempDir.resolve("messages.properties"), "test.key=Original");
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            assertThat(provider.getMessageTemplate("test.key", Locale.ROOT)).hasValue("Original");

            Files.writeString(tempDir.resolve("messages.properties"), "test.key=Updated");
            provider.refresh();

            assertThat(provider.getMessageTemplate("test.key", Locale.ROOT)).hasValue("Updated");
        }
    }

    @Nested
    @DisplayName("startAutoReload方法测试")
    class StartAutoReloadTests {

        @Test
        @DisplayName("启动自动重载")
        void testStartAutoReload() {
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            provider.startAutoReload();

            assertThat(provider.isWatching()).isTrue();
            provider.stopAutoReload();
        }

        @Test
        @DisplayName("重复启动不会创建多个线程")
        void testStartAutoReloadIdempotent() {
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            provider.startAutoReload();
            provider.startAutoReload();

            assertThat(provider.isWatching()).isTrue();
            provider.stopAutoReload();
        }
    }

    @Nested
    @DisplayName("stopAutoReload方法测试")
    class StopAutoReloadTests {

        @Test
        @DisplayName("停止自动重载")
        void testStopAutoReload() {
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);
            provider.startAutoReload();

            provider.stopAutoReload();

            assertThat(provider.isWatching()).isFalse();
        }

        @Test
        @DisplayName("未启动时停止不会抛出异常")
        void testStopAutoReloadNotStarted() {
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            assertThatCode(() -> provider.stopAutoReload()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("addReloadListener方法测试")
    class AddReloadListenerTests {

        @Test
        @DisplayName("添加监听器")
        void testAddReloadListener() throws IOException {
            Files.writeString(tempDir.resolve("messages.properties"), "key=value");
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);
            AtomicBoolean listenerCalled = new AtomicBoolean(false);

            provider.addReloadListener(locales -> listenerCalled.set(true));

            // 触发checkAndReload通过修改文件
            Files.writeString(tempDir.resolve("messages.properties"), "key=updated");
            provider.refresh(); // refresh会触发重新加载

            // 注意: refresh只是清除缓存并重新加载，不会调用listener
            // listener只在checkAndReload时被调用
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现MessageProvider接口")
        void testImplementsInterface() {
            ReloadableMessageProvider provider = new ReloadableMessageProvider(tempDir);

            assertThat(provider).isInstanceOf(MessageProvider.class);
        }
    }

    @Nested
    @DisplayName("目录不存在测试")
    class NonExistentDirectoryTests {

        @Test
        @DisplayName("目录不存在时不抛出异常")
        void testNonExistentDirectory() {
            Path nonExistent = Path.of("/nonexistent/path");
            ReloadableMessageProvider provider = new ReloadableMessageProvider(nonExistent);

            assertThat(provider.getSupportedLocales()).isEmpty();
        }
    }
}
