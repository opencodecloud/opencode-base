package cloud.opencode.base.classloader.graalvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ReflectConfig
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("ReflectConfig Tests")
class ReflectConfigTest {

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Should create with all fields")
        void shouldCreateWithAllFields() {
            ReflectConfig config = new ReflectConfig(
                    "com.example.MyClass", true, true, true, true
            );

            assertThat(config.name()).isEqualTo("com.example.MyClass");
            assertThat(config.allDeclaredConstructors()).isTrue();
            assertThat(config.allDeclaredMethods()).isTrue();
            assertThat(config.allDeclaredFields()).isTrue();
            assertThat(config.allPublicMethods()).isTrue();
        }

        @Test
        @DisplayName("Should create with mixed boolean values")
        void shouldCreateWithMixedValues() {
            ReflectConfig config = new ReflectConfig(
                    "com.example.MyClass", true, false, true, false
            );

            assertThat(config.allDeclaredConstructors()).isTrue();
            assertThat(config.allDeclaredMethods()).isFalse();
            assertThat(config.allDeclaredFields()).isTrue();
            assertThat(config.allPublicMethods()).isFalse();
        }

        @Test
        @DisplayName("Should reject null name")
        void shouldRejectNullName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ReflectConfig(null, true, true, true, true))
                    .withMessageContaining("Class name must not be null");
        }
    }

    @Nested
    @DisplayName("JSON Output Tests")
    class JsonOutputTests {

        @Test
        @DisplayName("Should produce valid JSON with all true")
        void shouldProduceValidJsonAllTrue() {
            ReflectConfig config = new ReflectConfig(
                    "com.example.MyClass", true, true, true, true
            );

            String json = config.toJson();

            assertThat(json).isEqualTo(
                    "{\"name\":\"com.example.MyClass\"," +
                            "\"allDeclaredConstructors\":true," +
                            "\"allDeclaredMethods\":true," +
                            "\"allDeclaredFields\":true," +
                            "\"allPublicMethods\":true}"
            );
        }

        @Test
        @DisplayName("Should produce valid JSON with all false")
        void shouldProduceValidJsonAllFalse() {
            ReflectConfig config = new ReflectConfig(
                    "com.example.MyClass", false, false, false, false
            );

            String json = config.toJson();

            assertThat(json).contains("\"allDeclaredConstructors\":false");
            assertThat(json).contains("\"allDeclaredMethods\":false");
            assertThat(json).contains("\"allDeclaredFields\":false");
            assertThat(json).contains("\"allPublicMethods\":false");
        }

        @Test
        @DisplayName("Should escape special characters in class name")
        void shouldEscapeSpecialCharacters() {
            ReflectConfig config = new ReflectConfig(
                    "com.example.My\"Class", true, true, true, true
            );

            String json = config.toJson();

            assertThat(json).contains("\"name\":\"com.example.My\\\"Class\"");
        }

        @Test
        @DisplayName("Should handle inner class names with dollar sign")
        void shouldHandleInnerClassNames() {
            ReflectConfig config = new ReflectConfig(
                    "com.example.Outer$Inner", true, true, true, true
            );

            String json = config.toJson();

            assertThat(json).contains("\"name\":\"com.example.Outer$Inner\"");
        }

        @Test
        @DisplayName("JSON should start with { and end with }")
        void shouldBeValidJsonObject() {
            ReflectConfig config = new ReflectConfig(
                    "com.example.Test", true, false, true, false
            );

            String json = config.toJson();

            assertThat(json).startsWith("{");
            assertThat(json).endsWith("}");
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal with same values")
        void shouldBeEqualWithSameValues() {
            ReflectConfig a = new ReflectConfig("com.example.A", true, true, true, true);
            ReflectConfig b = new ReflectConfig("com.example.A", true, true, true, true);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("Should not be equal with different values")
        void shouldNotBeEqualWithDifferentValues() {
            ReflectConfig a = new ReflectConfig("com.example.A", true, true, true, true);
            ReflectConfig b = new ReflectConfig("com.example.B", true, true, true, true);

            assertThat(a).isNotEqualTo(b);
        }
    }
}
