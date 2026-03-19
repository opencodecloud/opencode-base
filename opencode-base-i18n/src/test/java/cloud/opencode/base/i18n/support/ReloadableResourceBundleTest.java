package cloud.opencode.base.i18n.support;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * ReloadableResourceBundle 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("ReloadableResourceBundle 测试")
class ReloadableResourceBundleTest {

    private Path tempDir;
    private Path propertiesFile;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("bundle-test");
        propertiesFile = tempDir.resolve("messages.properties");
        Files.writeString(propertiesFile, "test.key=Initial Value");
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
        @DisplayName("从文件路径创建")
        void testConstructorWithPath() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            assertThat(bundle.getLocale()).isEqualTo(Locale.ENGLISH);
            assertThat(bundle.getFilePath()).isEqualTo(propertiesFile);
        }

        @Test
        @DisplayName("创建时加载内容")
        void testConstructorLoadsContent() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            assertThat(bundle.getString("test.key")).isEqualTo("Initial Value");
        }
    }

    @Nested
    @DisplayName("handleGetObject方法测试")
    class HandleGetObjectTests {

        @Test
        @DisplayName("获取存在的key")
        void testHandleGetObject() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            Object value = bundle.handleGetObject("test.key");

            assertThat(value).isEqualTo("Initial Value");
        }

        @Test
        @DisplayName("获取不存在的key返回null")
        void testHandleGetObjectNotFound() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            Object value = bundle.handleGetObject("nonexistent");

            assertThat(value).isNull();
        }
    }

    @Nested
    @DisplayName("getKeys方法测试")
    class GetKeysTests {

        @Test
        @DisplayName("获取所有keys")
        void testGetKeys() throws IOException {
            Files.writeString(propertiesFile, "key1=Value1\nkey2=Value2");
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            Enumeration<String> keys = bundle.getKeys();

            java.util.Set<String> keySet = new java.util.HashSet<>();
            while (keys.hasMoreElements()) {
                keySet.add(keys.nextElement());
            }
            assertThat(keySet).containsExactlyInAnyOrder("key1", "key2");
        }
    }

    @Nested
    @DisplayName("getLocale方法测试")
    class GetLocaleTests {

        @Test
        @DisplayName("获取Locale")
        void testGetLocale() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.FRENCH);

            assertThat(bundle.getLocale()).isEqualTo(Locale.FRENCH);
        }
    }

    @Nested
    @DisplayName("reload方法测试")
    class ReloadTests {

        @Test
        @DisplayName("重载更新的文件")
        void testReload() throws IOException {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            assertThat(bundle.getString("test.key")).isEqualTo("Initial Value");

            // 等待一小段时间确保文件修改时间不同
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }

            Files.writeString(propertiesFile, "test.key=Updated Value");

            boolean reloaded = bundle.reload();

            assertThat(reloaded).isTrue();
            assertThat(bundle.getString("test.key")).isEqualTo("Updated Value");
        }

        @Test
        @DisplayName("文件未修改时不重载")
        void testReloadNoChange() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);
            bundle.reload(); // 第一次reload设置lastModified

            boolean reloaded = bundle.reload();

            assertThat(reloaded).isFalse();
        }

        @Test
        @DisplayName("文件不存在时返回false")
        void testReloadFileNotExists() throws IOException {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            Files.delete(propertiesFile);

            boolean reloaded = bundle.reload();

            assertThat(reloaded).isFalse();
        }
    }

    @Nested
    @DisplayName("startWatching方法测试")
    class StartWatchingTests {

        @Test
        @DisplayName("启动监听")
        void testStartWatching() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            bundle.startWatching(Duration.ofSeconds(1));

            assertThat(bundle.isWatching()).isTrue();
            bundle.stopWatching();
        }

        @Test
        @DisplayName("重复启动不会创建多个线程")
        void testStartWatchingIdempotent() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            bundle.startWatching(Duration.ofSeconds(1));
            bundle.startWatching(Duration.ofSeconds(1));

            assertThat(bundle.isWatching()).isTrue();
            bundle.stopWatching();
        }
    }

    @Nested
    @DisplayName("stopWatching方法测试")
    class StopWatchingTests {

        @Test
        @DisplayName("停止监听")
        void testStopWatching() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);
            bundle.startWatching(Duration.ofSeconds(1));

            bundle.stopWatching();

            assertThat(bundle.isWatching()).isFalse();
        }

        @Test
        @DisplayName("未启动时停止不抛出异常")
        void testStopWatchingNotStarted() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            assertThatCode(() -> bundle.stopWatching()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("addReloadListener方法测试")
    class AddReloadListenerTests {

        @Test
        @DisplayName("添加监听器")
        void testAddReloadListener() throws IOException, InterruptedException {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);
            AtomicBoolean listenerCalled = new AtomicBoolean(false);

            bundle.addReloadListener(b -> listenerCalled.set(true));

            // 等待确保文件修改时间不同
            Thread.sleep(100);
            Files.writeString(propertiesFile, "test.key=Changed");
            bundle.reload();

            assertThat(listenerCalled.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("removeReloadListener方法测试")
    class RemoveReloadListenerTests {

        @Test
        @DisplayName("移除监听器")
        void testRemoveReloadListener() throws IOException, InterruptedException {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);
            AtomicBoolean listenerCalled = new AtomicBoolean(false);
            java.util.function.Consumer<ReloadableResourceBundle> listener = b -> listenerCalled.set(true);

            bundle.addReloadListener(listener);
            bundle.removeReloadListener(listener);

            Thread.sleep(100);
            Files.writeString(propertiesFile, "test.key=Changed");
            bundle.reload();

            assertThat(listenerCalled.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("getFilePath方法测试")
    class GetFilePathTests {

        @Test
        @DisplayName("获取文件路径")
        void testGetFilePath() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            assertThat(bundle.getFilePath()).isEqualTo(propertiesFile);
        }
    }

    @Nested
    @DisplayName("getLastModified方法测试")
    class GetLastModifiedTests {

        @Test
        @DisplayName("获取最后修改时间")
        void testGetLastModified() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            assertThat(bundle.getLastModified()).isPositive();
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是ResourceBundle子类")
        void testIsResourceBundle() {
            ReloadableResourceBundle bundle = new ReloadableResourceBundle(propertiesFile, Locale.ENGLISH);

            assertThat(bundle).isInstanceOf(ResourceBundle.class);
        }
    }

    @Nested
    @DisplayName("fromClasspath工厂方法测试")
    class FromClasspathTests {

        @Test
        @DisplayName("资源不存在时抛出异常")
        void testFromClasspathNotFound() {
            assertThatThrownBy(() ->
                    ReloadableResourceBundle.fromClasspath("nonexistent/resource", Locale.ENGLISH)
            ).isInstanceOf(MissingResourceException.class);
        }
    }
}
