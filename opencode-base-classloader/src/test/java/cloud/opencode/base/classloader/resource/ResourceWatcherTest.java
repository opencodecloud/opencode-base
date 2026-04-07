package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ResourceWatcher
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("ResourceWatcher Tests")
class ResourceWatcherTest {

    @TempDir
    Path tempDir;

    private ResourceWatcher watcher;

    @BeforeEach
    void setUp() throws IOException {
        watcher = new ResourceWatcher();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (watcher != null) {
            watcher.close();
        }
    }

    @Nested
    @DisplayName("File Modification Tests")
    class FileModificationTests {

        @Test
        @DisplayName("Should notify on file modification")
        void shouldNotifyOnFileModification() throws Exception {
            Path file = tempDir.resolve("config.txt");
            Files.writeString(file, "initial");

            CountDownLatch latch = new CountDownLatch(1);
            List<ResourceEvent> events = new CopyOnWriteArrayList<>();

            watcher.watch(file, event -> {
                events.add(event);
                latch.countDown();
            });

            // Allow watch registration to settle
            Thread.sleep(200);

            Files.writeString(file, "modified");

            boolean received = latch.await(5, TimeUnit.SECONDS);
            assertThat(received).isTrue();
            assertThat(events).isNotEmpty();
            assertThat(events.getFirst().type()).isEqualTo(ResourceEvent.Type.MODIFIED);
        }
    }

    @Nested
    @DisplayName("File Creation Tests")
    class FileCreationTests {

        @Test
        @DisplayName("Should notify on file creation via pattern watch")
        void shouldNotifyOnFileCreationViaPatternWatch() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            List<ResourceEvent> events = new CopyOnWriteArrayList<>();

            watcher.watchPattern(tempDir, "*.txt", event -> {
                events.add(event);
                latch.countDown();
            });

            // Allow watch registration to settle
            Thread.sleep(200);

            Files.writeString(tempDir.resolve("newfile.txt"), "new content");

            boolean received = latch.await(5, TimeUnit.SECONDS);
            assertThat(received).isTrue();
            assertThat(events).isNotEmpty();
            assertThat(events.getFirst().type()).isEqualTo(ResourceEvent.Type.CREATED);
        }
    }

    @Nested
    @DisplayName("File Deletion Tests")
    class FileDeletionTests {

        @Test
        @DisplayName("Should notify on file deletion")
        void shouldNotifyOnFileDeletion() throws Exception {
            Path file = tempDir.resolve("to-delete.txt");
            Files.writeString(file, "delete me");

            CountDownLatch latch = new CountDownLatch(1);
            List<ResourceEvent> events = new CopyOnWriteArrayList<>();

            watcher.watch(file, event -> {
                if (event.type() == ResourceEvent.Type.DELETED) {
                    events.add(event);
                    latch.countDown();
                }
            });

            // Allow watch registration to settle
            Thread.sleep(200);

            Files.delete(file);

            boolean received = latch.await(5, TimeUnit.SECONDS);
            assertThat(received).isTrue();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst().type()).isEqualTo(ResourceEvent.Type.DELETED);
        }
    }

    @Nested
    @DisplayName("Debounce Tests")
    class DebounceTests {

        @Test
        @DisplayName("Should debounce rapid events within 100ms window")
        void shouldDebounceRapidEvents() throws Exception {
            Path file = tempDir.resolve("debounce.txt");
            Files.writeString(file, "initial");

            CountDownLatch latch = new CountDownLatch(1);
            List<ResourceEvent> events = new CopyOnWriteArrayList<>();

            watcher.watch(file, event -> {
                events.add(event);
                latch.countDown();
            });

            // Allow watch registration to settle
            Thread.sleep(200);

            // Rapid successive writes — should be debounced
            Files.writeString(file, "change1");
            Files.writeString(file, "change2");
            Files.writeString(file, "change3");

            // Wait for events to arrive
            latch.await(5, TimeUnit.SECONDS);
            // Extra wait to see if additional events leak through
            Thread.sleep(500);

            // Due to debouncing, we should see fewer events than 3
            // At minimum 1, at most could be 2 depending on timing
            assertThat(events.size()).isLessThanOrEqualTo(2);
            assertThat(events).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Watchers Tests")
    class MultipleWatchersTests {

        @Test
        @DisplayName("Should notify multiple watchers on same file")
        void shouldNotifyMultipleWatchers() throws Exception {
            Path file = tempDir.resolve("multi.txt");
            Files.writeString(file, "initial");

            CountDownLatch latch1 = new CountDownLatch(1);
            CountDownLatch latch2 = new CountDownLatch(1);
            List<ResourceEvent> events1 = new CopyOnWriteArrayList<>();
            List<ResourceEvent> events2 = new CopyOnWriteArrayList<>();

            watcher.watch(file, event -> {
                events1.add(event);
                latch1.countDown();
            });
            watcher.watch(file, event -> {
                events2.add(event);
                latch2.countDown();
            });

            // Allow watch registration to settle
            Thread.sleep(200);

            Files.writeString(file, "modified");

            assertThat(latch1.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(latch2.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(events1).isNotEmpty();
            assertThat(events2).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Watch Pattern Tests")
    class WatchPatternTests {

        @Test
        @DisplayName("Should only match files matching glob pattern")
        void shouldOnlyMatchGlobPattern() throws Exception {
            CountDownLatch ymlLatch = new CountDownLatch(1);
            List<ResourceEvent> ymlEvents = new CopyOnWriteArrayList<>();

            watcher.watchPattern(tempDir, "*.yml", event -> {
                ymlEvents.add(event);
                ymlLatch.countDown();
            });

            // Allow watch registration to settle
            Thread.sleep(200);

            // Create non-matching file first
            Files.writeString(tempDir.resolve("ignored.txt"), "should be ignored");
            // Create matching file
            Files.writeString(tempDir.resolve("config.yml"), "key: value");

            boolean received = ymlLatch.await(5, TimeUnit.SECONDS);
            assertThat(received).isTrue();
            assertThat(ymlEvents).isNotEmpty();

            // Verify only yml events captured
            for (ResourceEvent event : ymlEvents) {
                assertThat(event.resource().getFilename()).endsWith(".yml");
            }
        }
    }

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

        @Test
        @DisplayName("Should stop watching after close")
        void shouldStopWatchingAfterClose() throws Exception {
            Path file = tempDir.resolve("close-test.txt");
            Files.writeString(file, "initial");

            List<ResourceEvent> events = new CopyOnWriteArrayList<>();
            watcher.watch(file, events::add);

            // Close the watcher
            watcher.close();
            watcher = null; // prevent double close in tearDown

            // Modify file after close
            Thread.sleep(200);
            Files.writeString(file, "modified after close");
            Thread.sleep(1000);

            // Should not receive events after close
            assertThat(events).isEmpty();
        }

        @Test
        @DisplayName("Handle close should remove watch")
        void handleCloseShouldRemoveWatch() throws Exception {
            Path file = tempDir.resolve("handle-close.txt");
            Files.writeString(file, "initial");

            List<ResourceEvent> events = new CopyOnWriteArrayList<>();
            ResourceWatchHandle handle = watcher.watch(file, events::add);

            assertThat(watcher.getWatchCount()).isEqualTo(1);

            handle.close();

            assertThat(watcher.getWatchCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Watch Count Tests")
    class WatchCountTests {

        @Test
        @DisplayName("Should track watch count correctly")
        void shouldTrackWatchCountCorrectly() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "content1");
            Files.writeString(file2, "content2");

            assertThat(watcher.getWatchCount()).isEqualTo(0);

            ResourceWatchHandle handle1 = watcher.watch(file1, e -> {});
            assertThat(watcher.getWatchCount()).isEqualTo(1);

            ResourceWatchHandle handle2 = watcher.watch(file2, e -> {});
            assertThat(watcher.getWatchCount()).isEqualTo(2);

            ResourceWatchHandle handle3 = watcher.watchPattern(tempDir, "*.yml", e -> {});
            assertThat(watcher.getWatchCount()).isEqualTo(3);

            handle1.close();
            assertThat(watcher.getWatchCount()).isEqualTo(2);

            handle2.close();
            handle3.close();
            assertThat(watcher.getWatchCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return zero for new watcher")
        void shouldReturnZeroForNewWatcher() {
            assertThat(watcher.getWatchCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw on null file path")
        void shouldThrowOnNullFilePath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> watcher.watch(null, e -> {}));
        }

        @Test
        @DisplayName("Should throw on null callback for watch")
        void shouldThrowOnNullCallbackForWatch() {
            assertThatNullPointerException()
                    .isThrownBy(() -> watcher.watch(tempDir.resolve("test.txt"), null));
        }

        @Test
        @DisplayName("Should throw on null directory for watchPattern")
        void shouldThrowOnNullDirectoryForWatchPattern() {
            assertThatNullPointerException()
                    .isThrownBy(() -> watcher.watchPattern(null, "*.txt", e -> {}));
        }

        @Test
        @DisplayName("Should throw on null glob pattern")
        void shouldThrowOnNullGlobPattern() {
            assertThatNullPointerException()
                    .isThrownBy(() -> watcher.watchPattern(tempDir, null, e -> {}));
        }

        @Test
        @DisplayName("Should throw on null callback for watchPattern")
        void shouldThrowOnNullCallbackForWatchPattern() {
            assertThatNullPointerException()
                    .isThrownBy(() -> watcher.watchPattern(tempDir, "*.txt", null));
        }
    }

    @Nested
    @DisplayName("Event Content Tests")
    class EventContentTests {

        @Test
        @DisplayName("Should include resource and timestamp in event")
        void shouldIncludeResourceAndTimestamp() throws Exception {
            Path file = tempDir.resolve("event-content.txt");
            Files.writeString(file, "initial");

            CountDownLatch latch = new CountDownLatch(1);
            List<ResourceEvent> events = new CopyOnWriteArrayList<>();

            long beforeWatch = System.currentTimeMillis();

            watcher.watch(file, event -> {
                events.add(event);
                latch.countDown();
            });

            Thread.sleep(200);
            Files.writeString(file, "modified");

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(events).isNotEmpty();

            ResourceEvent event = events.getFirst();
            assertThat(event.resource()).isNotNull();
            assertThat(event.resource().getFilename()).isEqualTo("event-content.txt");
            assertThat(event.timestamp()).isGreaterThanOrEqualTo(beforeWatch);
        }
    }
}
