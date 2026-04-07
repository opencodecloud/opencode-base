package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SafeJsSnowflakeGenerator")
class SafeJsSnowflakeGeneratorTest {

    @Nested
    @DisplayName("create()")
    class Construction {

        @Test
        void defaultCreate() {
            SafeJsSnowflakeGenerator gen = SafeJsSnowflakeGenerator.create();
            assertThat(gen.getWorkerId()).isEqualTo(0);
        }

        @Test
        void createWithWorkerId() {
            SafeJsSnowflakeGenerator gen = SafeJsSnowflakeGenerator.create(63);
            assertThat(gen.getWorkerId()).isEqualTo(63);
        }

        @Test
        void workerIdTooHighThrows() {
            assertThatThrownBy(() -> SafeJsSnowflakeGenerator.create(64))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void negativeWorkerIdThrows() {
            assertThatThrownBy(() -> SafeJsSnowflakeGenerator.create(-1))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }

    @Nested
    @DisplayName("generate()")
    class Generate {

        @Test
        void generatedIdIsJsSafe() {
            SafeJsSnowflakeGenerator gen = SafeJsSnowflakeGenerator.create();
            for (int i = 0; i < 100; i++) {
                long id = gen.generate();
                assertThat(SafeJsSnowflakeGenerator.isJsSafe(id))
                        .as("ID %d should be JS-safe", id).isTrue();
            }
        }

        @Test
        void generatedIdIsPositive() {
            SafeJsSnowflakeGenerator gen = SafeJsSnowflakeGenerator.create();
            assertThat(gen.generate()).isPositive();
        }

        @Test
        void generateStrMatchesGenerate() {
            SafeJsSnowflakeGenerator gen = SafeJsSnowflakeGenerator.create(1);
            // generateStr returns String.valueOf(generate()), but they call generate() separately
            // Just test format
            String str = gen.generateStr();
            assertThat(str).matches("\\d+");
            assertThat(Long.parseLong(str)).isLessThanOrEqualTo(SafeJsSnowflakeGenerator.JS_MAX_SAFE_INT);
        }

        @Test
        void generateBatchAllJsSafe() {
            SafeJsSnowflakeGenerator gen = SafeJsSnowflakeGenerator.create();
            Set<Long> ids = new HashSet<>();
            for (int i = 0; i < 200; i++) {
                long id = gen.generate();
                assertThat(id).isGreaterThan(0).isLessThanOrEqualTo(SafeJsSnowflakeGenerator.JS_MAX_SAFE_INT);
                ids.add(id);
            }
            // All should be unique
            assertThat(ids).hasSize(200);
        }

        @Test
        void getTypeReturnsExpected() {
            assertThat(SafeJsSnowflakeGenerator.create().getType()).isEqualTo("SafeJsSnowflake");
        }
    }

    @Nested
    @DisplayName("isJsSafe()")
    class IsJsSafe {

        @Test
        void maxSafeIntIsJsSafe() {
            assertThat(SafeJsSnowflakeGenerator.isJsSafe(SafeJsSnowflakeGenerator.JS_MAX_SAFE_INT)).isTrue();
        }

        @Test
        void maxSafeIntPlusOneIsNotJsSafe() {
            assertThat(SafeJsSnowflakeGenerator.isJsSafe(SafeJsSnowflakeGenerator.JS_MAX_SAFE_INT + 1)).isFalse();
        }

        @Test
        void negativeIsNotJsSafe() {
            assertThat(SafeJsSnowflakeGenerator.isJsSafe(-1)).isFalse();
        }

        @Test
        void zeroIsJsSafe() {
            assertThat(SafeJsSnowflakeGenerator.isJsSafe(0)).isTrue();
        }
    }
}
