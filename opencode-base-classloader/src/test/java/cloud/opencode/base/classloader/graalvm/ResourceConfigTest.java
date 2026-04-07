package cloud.opencode.base.classloader.graalvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ResourceConfig
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("ResourceConfig Tests")
class ResourceConfigTest {

    @Nested
    @DisplayName("Pattern Tests")
    class PatternTests {

        @Test
        @DisplayName("Should create pattern with value")
        void shouldCreatePatternWithValue() {
            ResourceConfig.Pattern pattern = new ResourceConfig.Pattern("config/.*");

            assertThat(pattern.pattern()).isEqualTo("config/.*");
        }

        @Test
        @DisplayName("Should reject null pattern")
        void shouldRejectNullPattern() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ResourceConfig.Pattern(null))
                    .withMessageContaining("Pattern must not be null");
        }

        @Test
        @DisplayName("Should produce valid JSON for pattern")
        void shouldProduceValidPatternJson() {
            ResourceConfig.Pattern pattern = new ResourceConfig.Pattern("config/.*");

            assertThat(pattern.toJson()).isEqualTo("{\"pattern\":\"config/.*\"}");
        }

        @Test
        @DisplayName("Should escape special characters in pattern")
        void shouldEscapeSpecialCharsInPattern() {
            ResourceConfig.Pattern pattern = new ResourceConfig.Pattern("path/with\"quote");

            assertThat(pattern.toJson()).contains("path/with\\\"quote");
        }
    }

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Should create with empty includes list")
        void shouldCreateWithEmptyList() {
            ResourceConfig config = new ResourceConfig(List.of());

            assertThat(config.includes()).isEmpty();
        }

        @Test
        @DisplayName("Should create with multiple patterns")
        void shouldCreateWithMultiplePatterns() {
            List<ResourceConfig.Pattern> patterns = List.of(
                    new ResourceConfig.Pattern("config/.*"),
                    new ResourceConfig.Pattern("templates/.*")
            );

            ResourceConfig config = new ResourceConfig(patterns);

            assertThat(config.includes()).hasSize(2);
        }

        @Test
        @DisplayName("Should reject null includes list")
        void shouldRejectNullIncludes() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ResourceConfig(null))
                    .withMessageContaining("Includes list must not be null");
        }

        @Test
        @DisplayName("Should make defensive copy of includes list")
        void shouldMakeDefensiveCopy() {
            List<ResourceConfig.Pattern> mutableList = new ArrayList<>();
            mutableList.add(new ResourceConfig.Pattern("config/.*"));

            ResourceConfig config = new ResourceConfig(mutableList);
            mutableList.add(new ResourceConfig.Pattern("extra/.*"));

            assertThat(config.includes()).hasSize(1);
        }

        @Test
        @DisplayName("Includes list should be unmodifiable")
        void shouldReturnUnmodifiableList() {
            ResourceConfig config = new ResourceConfig(List.of(
                    new ResourceConfig.Pattern("config/.*")
            ));

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> config.includes().add(new ResourceConfig.Pattern("extra/.*")));
        }
    }

    @Nested
    @DisplayName("JSON Output Tests")
    class JsonOutputTests {

        @Test
        @DisplayName("Should produce correct JSON with single pattern")
        void shouldProduceJsonWithSinglePattern() {
            ResourceConfig config = new ResourceConfig(List.of(
                    new ResourceConfig.Pattern("config/.*")
            ));

            String json = config.toJson();

            assertThat(json).isEqualTo(
                    "{\"resources\":{\"includes\":[{\"pattern\":\"config/.*\"}]}}"
            );
        }

        @Test
        @DisplayName("Should produce correct JSON with multiple patterns")
        void shouldProduceJsonWithMultiplePatterns() {
            ResourceConfig config = new ResourceConfig(List.of(
                    new ResourceConfig.Pattern("config/.*"),
                    new ResourceConfig.Pattern("templates/.*")
            ));

            String json = config.toJson();

            assertThat(json).isEqualTo(
                    "{\"resources\":{\"includes\":[" +
                            "{\"pattern\":\"config/.*\"}," +
                            "{\"pattern\":\"templates/.*\"}" +
                            "]}}"
            );
        }

        @Test
        @DisplayName("Should produce correct JSON with empty includes")
        void shouldProduceJsonWithEmptyIncludes() {
            ResourceConfig config = new ResourceConfig(List.of());

            String json = config.toJson();

            assertThat(json).isEqualTo("{\"resources\":{\"includes\":[]}}");
        }

        @Test
        @DisplayName("JSON should match GraalVM resource-config.json format")
        void shouldMatchGraalVmFormat() {
            ResourceConfig config = new ResourceConfig(List.of(
                    new ResourceConfig.Pattern("config/.*"),
                    new ResourceConfig.Pattern("templates/.*")
            ));

            String json = config.toJson();

            assertThat(json).startsWith("{\"resources\":{\"includes\":[");
            assertThat(json).endsWith("]}}");
            assertThat(json).contains("\"pattern\":");
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal with same patterns")
        void shouldBeEqualWithSamePatterns() {
            ResourceConfig a = new ResourceConfig(List.of(
                    new ResourceConfig.Pattern("config/.*")
            ));
            ResourceConfig b = new ResourceConfig(List.of(
                    new ResourceConfig.Pattern("config/.*")
            ));

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }
}
