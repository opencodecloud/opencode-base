package cloud.opencode.base.config.internal;

import cloud.opencode.base.config.ConfigChangeEvent;
import cloud.opencode.base.config.ConfigListener;
import cloud.opencode.base.config.source.CompositeConfigSource;
import cloud.opencode.base.config.source.InMemoryConfigSource;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigWatcher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigWatcher 测试")
class ConfigWatcherTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建ConfigWatcher")
        void testConstructor() {
            ConfigWatcher watcher = new ConfigWatcher(Duration.ofSeconds(5));
            assertThat(watcher).isNotNull();
            watcher.close();
        }
    }

    @Nested
    @DisplayName("监听器测试")
    class ListenerTests {

        @Test
        @DisplayName("添加监听器")
        void testAddListener() {
            ConfigWatcher watcher = new ConfigWatcher(Duration.ofSeconds(1));
            List<ConfigChangeEvent> events = new CopyOnWriteArrayList<>();

            watcher.addListener(events::add);

            assertThat(events).isEmpty(); // 只添加监听器,不触发
            watcher.close();
        }

        @Test
        @DisplayName("多个监听器")
        void testMultipleListeners() {
            ConfigWatcher watcher = new ConfigWatcher(Duration.ofSeconds(1));
            List<ConfigChangeEvent> events1 = new CopyOnWriteArrayList<>();
            List<ConfigChangeEvent> events2 = new CopyOnWriteArrayList<>();

            watcher.addListener(events1::add);
            watcher.addListener(events2::add);

            watcher.close();
        }
    }

    @Nested
    @DisplayName("watch测试")
    class WatchTests {

        @Test
        @DisplayName("监视配置源")
        void testWatch() {
            ConfigWatcher watcher = new ConfigWatcher(Duration.ofSeconds(1));
            InMemoryConfigSource source = new InMemoryConfigSource(Map.of("key", "value"));
            CompositeConfigSource composite = new CompositeConfigSource(List.of(source));

            watcher.watch(composite);

            watcher.close();
        }
    }

    @Nested
    @DisplayName("生命周期测试")
    class LifecycleTests {

        @Test
        @DisplayName("start和close")
        void testStartAndClose() {
            ConfigWatcher watcher = new ConfigWatcher(Duration.ofMillis(100));
            InMemoryConfigSource source = new InMemoryConfigSource(Map.of("key", "value"));
            CompositeConfigSource composite = new CompositeConfigSource(List.of(source));

            watcher.watch(composite);
            watcher.start();

            // 允许一些时间运行
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            watcher.close();
        }

        @Test
        @DisplayName("重复close不抛异常")
        void testMultipleClose() {
            ConfigWatcher watcher = new ConfigWatcher(Duration.ofSeconds(1));
            watcher.close();
            watcher.close(); // 不应该抛出异常
        }
    }

    @Nested
    @DisplayName("AutoCloseable测试")
    class AutoCloseableTests {

        @Test
        @DisplayName("try-with-resources")
        void testTryWithResources() {
            try (ConfigWatcher watcher = new ConfigWatcher(Duration.ofSeconds(1))) {
                InMemoryConfigSource source = new InMemoryConfigSource(Map.of("key", "value"));
                CompositeConfigSource composite = new CompositeConfigSource(List.of(source));
                watcher.watch(composite);
            }
            // 不应该抛出异常
        }
    }
}
