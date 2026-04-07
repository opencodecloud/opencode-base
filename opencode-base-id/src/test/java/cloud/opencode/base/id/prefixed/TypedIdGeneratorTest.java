package cloud.opencode.base.id.prefixed;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import cloud.opencode.base.id.nanoid.NanoIdGenerator;
import cloud.opencode.base.id.ulid.UlidGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TypedIdGenerator")
class TypedIdGeneratorTest {

    @Nested
    @DisplayName("of() construction")
    class Construction {

        @Test
        void validPrefixAndGenerator() {
            TypedIdGenerator gen = TypedIdGenerator.of("usr", UlidGenerator.create());
            assertThat(gen.getPrefix()).isEqualTo("usr");
            assertThat(gen.getType()).isEqualTo("TypedId[usr]");
        }

        @Test
        void invalidPrefixThrows() {
            assertThatThrownBy(() -> TypedIdGenerator.of("USR", NanoIdGenerator.create()))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void nullInnerThrows() {
            assertThatThrownBy(() -> TypedIdGenerator.of("usr", null))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void nullPrefixThrows() {
            assertThatThrownBy(() -> TypedIdGenerator.of(null, NanoIdGenerator.create()))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }

    @Nested
    @DisplayName("generate()")
    class Generate {

        @Test
        void generateHasCorrectPrefix() {
            TypedIdGenerator gen = TypedIdGenerator.of("order", NanoIdGenerator.create());
            String id = gen.generate();
            assertThat(id).startsWith("order_");
        }

        @Test
        void generateParseable() {
            TypedIdGenerator gen = TypedIdGenerator.of("inv", UlidGenerator.create());
            String id = gen.generate();
            PrefixedId parsed = PrefixedId.fromString(id);
            assertThat(parsed.prefix()).isEqualTo("inv");
            assertThat(parsed.rawId()).isNotEmpty();
        }

        @Test
        void generatePrefixedReturnsPrefixedId() {
            TypedIdGenerator gen = TypedIdGenerator.of("cust", NanoIdGenerator.create());
            PrefixedId pid = gen.generatePrefixed();
            assertThat(pid.prefix()).isEqualTo("cust");
            assertThat(pid.rawId()).isNotEmpty();
        }

        @Test
        void generateBatchReturnsRequestedCount() {
            TypedIdGenerator gen = TypedIdGenerator.of("x", NanoIdGenerator.create());
            List<String> ids = gen.generateBatch(10);
            assertThat(ids).hasSize(10).allMatch(id -> id.startsWith("x_"));
        }

        @Test
        void generateBatchZeroReturnsEmpty() {
            TypedIdGenerator gen = TypedIdGenerator.of("x", NanoIdGenerator.create());
            assertThat(gen.generateBatch(0)).isEmpty();
        }

        @Test
        void consecutiveIdsAreUnique() {
            TypedIdGenerator gen = TypedIdGenerator.of("usr", NanoIdGenerator.create());
            String id1 = gen.generate();
            String id2 = gen.generate();
            assertThat(id1).isNotEqualTo(id2);
        }
    }
}
