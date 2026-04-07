package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ResourceEvent
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("ResourceEvent Tests")
class ResourceEventTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create event with all fields")
        void shouldCreateEventWithAllFields() throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");
            FileResource resource = new FileResource(file);
            long timestamp = System.currentTimeMillis();

            ResourceEvent event = new ResourceEvent(ResourceEvent.Type.CREATED, resource, timestamp);

            assertThat(event.type()).isEqualTo(ResourceEvent.Type.CREATED);
            assertThat(event.resource()).isSameAs(resource);
            assertThat(event.timestamp()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("Should create event with zero timestamp")
        void shouldCreateEventWithZeroTimestamp() throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");
            FileResource resource = new FileResource(file);

            ResourceEvent event = new ResourceEvent(ResourceEvent.Type.MODIFIED, resource, 0L);

            assertThat(event.timestamp()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw on null type")
        void shouldThrowOnNullType() throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");
            FileResource resource = new FileResource(file);

            assertThatNullPointerException()
                    .isThrownBy(() -> new ResourceEvent(null, resource, System.currentTimeMillis()))
                    .withMessageContaining("Event type");
        }

        @Test
        @DisplayName("Should throw on null resource")
        void shouldThrowOnNullResource() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ResourceEvent(ResourceEvent.Type.DELETED, null, System.currentTimeMillis()))
                    .withMessageContaining("Resource");
        }
    }

    @Nested
    @DisplayName("Type Enum Tests")
    class TypeEnumTests {

        @Test
        @DisplayName("Should have all expected types")
        void shouldHaveAllExpectedTypes() {
            ResourceEvent.Type[] values = ResourceEvent.Type.values();

            assertThat(values).containsExactly(
                    ResourceEvent.Type.CREATED,
                    ResourceEvent.Type.MODIFIED,
                    ResourceEvent.Type.DELETED
            );
        }

        @Test
        @DisplayName("Should convert from string")
        void shouldConvertFromString() {
            assertThat(ResourceEvent.Type.valueOf("CREATED")).isEqualTo(ResourceEvent.Type.CREATED);
            assertThat(ResourceEvent.Type.valueOf("MODIFIED")).isEqualTo(ResourceEvent.Type.MODIFIED);
            assertThat(ResourceEvent.Type.valueOf("DELETED")).isEqualTo(ResourceEvent.Type.DELETED);
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class RecordEqualityTests {

        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");
            FileResource resource = new FileResource(file);
            long timestamp = 1234567890L;

            ResourceEvent event1 = new ResourceEvent(ResourceEvent.Type.MODIFIED, resource, timestamp);
            ResourceEvent event2 = new ResourceEvent(ResourceEvent.Type.MODIFIED, resource, timestamp);

            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");
            FileResource resource = new FileResource(file);

            ResourceEvent event = new ResourceEvent(ResourceEvent.Type.CREATED, resource, 0L);

            assertThat(event.toString()).contains("CREATED");
        }
    }
}
