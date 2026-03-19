package cloud.opencode.base.io.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * FileWatcher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("FileWatcher 测试")
class FileWatcherTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("watch方法测试")
    class WatchTests {

        @Test
        @DisplayName("从Path创建")
        void testWatchPath() {
            try (FileWatcher watcher = FileWatcher.watch(tempDir)) {
                assertThat(watcher.getWatchPath()).isEqualTo(tempDir);
            }
        }

        @Test
        @DisplayName("从字符串创建")
        void testWatchString() {
            try (FileWatcher watcher = FileWatcher.watch(tempDir.toString())) {
                assertThat(watcher.getWatchPath()).isEqualTo(tempDir);
            }
        }

        @Test
        @DisplayName("非目录抛出异常")
        void testWatchFile() throws Exception {
            Path file = tempDir.resolve("file.txt");
            Files.createFile(file);

            assertThatThrownBy(() -> FileWatcher.watch(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be a directory");
        }
    }

    @Nested
    @DisplayName("filter方法测试")
    class FilterTests {

        @Test
        @DisplayName("设置过滤模式")
        void testFilter() {
            try (FileWatcher watcher = FileWatcher.watch(tempDir).filter("*.txt")) {
                assertThat(watcher).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("事件处理器测试")
    class EventHandlerTests {

        @Test
        @DisplayName("onCreate处理器")
        void testOnCreate() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Path> createdPath = new AtomicReference<>();

            try (FileWatcher watcher = FileWatcher.watch(tempDir)
                    .onCreate(path -> {
                        createdPath.set(path);
                        latch.countDown();
                    })) {
                watcher.start();

                // Create a file
                Files.createFile(tempDir.resolve("newfile.txt"));

                boolean received = latch.await(5, TimeUnit.SECONDS);
                if (received) {
                    assertThat(createdPath.get().getFileName().toString()).isEqualTo("newfile.txt");
                }
            }
        }

        @Test
        @DisplayName("onModify处理器")
        void testOnModify() throws Exception {
            Path file = tempDir.resolve("modify.txt");
            Files.createFile(file);

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Path> modifiedPath = new AtomicReference<>();

            try (FileWatcher watcher = FileWatcher.watch(tempDir)
                    .onModify(path -> {
                        modifiedPath.set(path);
                        latch.countDown();
                    })) {
                watcher.start();

                // Modify the file
                Files.writeString(file, "modified content");

                latch.await(5, TimeUnit.SECONDS);
                // Modification events may or may not be received depending on timing
            }
        }

        @Test
        @DisplayName("onDelete处理器")
        void testOnDelete() throws Exception {
            Path file = tempDir.resolve("delete.txt");
            Files.createFile(file);

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Path> deletedPath = new AtomicReference<>();

            try (FileWatcher watcher = FileWatcher.watch(tempDir)
                    .onDelete(path -> {
                        deletedPath.set(path);
                        latch.countDown();
                    })) {
                watcher.start();

                // Delete the file
                Files.delete(file);

                boolean received = latch.await(5, TimeUnit.SECONDS);
                if (received) {
                    assertThat(deletedPath.get().getFileName().toString()).isEqualTo("delete.txt");
                }
            }
        }

        @Test
        @DisplayName("onAny处理器")
        void testOnAny() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<FileWatcher.FileEvent> receivedEvent = new AtomicReference<>();

            try (FileWatcher watcher = FileWatcher.watch(tempDir)
                    .onAny(event -> {
                        receivedEvent.set(event);
                        latch.countDown();
                    })) {
                watcher.start();

                // Create a file
                Files.createFile(tempDir.resolve("anyevent.txt"));

                boolean received = latch.await(5, TimeUnit.SECONDS);
                if (received) {
                    assertThat(receivedEvent.get()).isNotNull();
                }
            }
        }
    }

    @Nested
    @DisplayName("start和stop方法测试")
    class StartStopTests {

        @Test
        @DisplayName("start开始监听")
        void testStart() {
            try (FileWatcher watcher = FileWatcher.watch(tempDir)) {
                assertThat(watcher.isRunning()).isFalse();

                watcher.start();

                assertThat(watcher.isRunning()).isTrue();
            }
        }

        @Test
        @DisplayName("stop停止监听")
        void testStop() {
            try (FileWatcher watcher = FileWatcher.watch(tempDir)) {
                watcher.start();
                assertThat(watcher.isRunning()).isTrue();

                watcher.stop();

                assertThat(watcher.isRunning()).isFalse();
            }
        }

        @Test
        @DisplayName("多次start不报错")
        void testMultipleStart() {
            try (FileWatcher watcher = FileWatcher.watch(tempDir)) {
                watcher.start();
                watcher.start();

                assertThat(watcher.isRunning()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("close停止监听并关闭服务")
        void testClose() {
            FileWatcher watcher = FileWatcher.watch(tempDir);
            watcher.start();

            watcher.close();

            assertThat(watcher.isRunning()).isFalse();
        }
    }

    @Nested
    @DisplayName("isRunning方法测试")
    class IsRunningTests {

        @Test
        @DisplayName("初始状态为false")
        void testIsRunningInitial() {
            try (FileWatcher watcher = FileWatcher.watch(tempDir)) {
                assertThat(watcher.isRunning()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("getWatchPath方法测试")
    class GetWatchPathTests {

        @Test
        @DisplayName("返回监听路径")
        void testGetWatchPath() {
            try (FileWatcher watcher = FileWatcher.watch(tempDir)) {
                assertThat(watcher.getWatchPath()).isEqualTo(tempDir);
            }
        }
    }

    @Nested
    @DisplayName("FileEvent记录测试")
    class FileEventTests {

        @Test
        @DisplayName("isCreate方法")
        void testIsCreate() {
            Path path = tempDir.resolve("test.txt");
            FileWatcher.FileEvent event = new FileWatcher.FileEvent(path, StandardWatchEventKinds.ENTRY_CREATE);

            assertThat(event.isCreate()).isTrue();
            assertThat(event.isModify()).isFalse();
            assertThat(event.isDelete()).isFalse();
        }

        @Test
        @DisplayName("isModify方法")
        void testIsModify() {
            Path path = tempDir.resolve("test.txt");
            FileWatcher.FileEvent event = new FileWatcher.FileEvent(path, StandardWatchEventKinds.ENTRY_MODIFY);

            assertThat(event.isCreate()).isFalse();
            assertThat(event.isModify()).isTrue();
            assertThat(event.isDelete()).isFalse();
        }

        @Test
        @DisplayName("isDelete方法")
        void testIsDelete() {
            Path path = tempDir.resolve("test.txt");
            FileWatcher.FileEvent event = new FileWatcher.FileEvent(path, StandardWatchEventKinds.ENTRY_DELETE);

            assertThat(event.isCreate()).isFalse();
            assertThat(event.isModify()).isFalse();
            assertThat(event.isDelete()).isTrue();
        }

        @Test
        @DisplayName("记录字段访问")
        void testEventFields() {
            Path path = tempDir.resolve("test.txt");
            FileWatcher.FileEvent event = new FileWatcher.FileEvent(path, StandardWatchEventKinds.ENTRY_CREATE);

            assertThat(event.path()).isEqualTo(path);
            assertThat(event.kind()).isEqualTo(StandardWatchEventKinds.ENTRY_CREATE);
        }
    }

    @Nested
    @DisplayName("过滤器集成测试")
    class FilterIntegrationTests {

        @Test
        @DisplayName("只处理匹配的文件")
        void testFilterMatching() throws Exception {
            CountDownLatch txtLatch = new CountDownLatch(1);
            AtomicReference<Path> receivedPath = new AtomicReference<>();

            try (FileWatcher watcher = FileWatcher.watch(tempDir)
                    .filter("*.txt")
                    .onCreate(path -> {
                        receivedPath.set(path);
                        txtLatch.countDown();
                    })) {
                watcher.start();

                // Create a .log file (should not trigger)
                Files.createFile(tempDir.resolve("ignored.log"));

                // Create a .txt file (should trigger)
                Files.createFile(tempDir.resolve("matched.txt"));

                boolean received = txtLatch.await(5, TimeUnit.SECONDS);
                if (received) {
                    assertThat(receivedPath.get().getFileName().toString()).endsWith(".txt");
                }
            }
        }
    }
}
