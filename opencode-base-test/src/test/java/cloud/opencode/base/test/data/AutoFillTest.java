package cloud.opencode.base.test.data;

import cloud.opencode.base.test.exception.DataGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AutoFill")
class AutoFillTest {

    // ==================== Test types ====================

    record SimpleRecord(String name, int age) {}

    record NestedRecord(String title, SimpleRecord author) {}

    record AllTypesRecord(
            String s, int i, long l, double d, float f, boolean b,
            byte by, short sh, char c, byte[] bytes,
            LocalDate date, LocalDateTime dateTime, Instant instant,
            Duration duration, UUID uuid, List<String> list) {}

    enum Color { RED, GREEN, BLUE }

    record EnumRecord(String label, Color color) {}

    public static class SimplePojo {
        private String name;
        private int age;

        public SimplePojo() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class NestedPojo {
        private String title;
        private SimplePojo author;

        public NestedPojo() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public SimplePojo getAuthor() { return author; }
        public void setAuthor(SimplePojo author) { this.author = author; }
    }

    // ==================== Tests ====================

    @Nested
    @DisplayName("Record filling")
    class RecordFilling {

        @Test
        @DisplayName("should fill a simple record with random data")
        void fillSimpleRecord() {
            SimpleRecord result = AutoFill.of(SimpleRecord.class).build();

            assertThat(result).isNotNull();
            assertThat(result.name()).isNotNull().isNotEmpty();
            assertThat(result.age()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should fill all supported primitive and wrapper types")
        void fillAllTypes() {
            AllTypesRecord result = AutoFill.of(AllTypesRecord.class).build();

            assertThat(result).isNotNull();
            assertThat(result.s()).isNotNull().isNotEmpty();
            assertThat(result.i()).isGreaterThan(0);
            assertThat(result.l()).isGreaterThan(0L);
            assertThat(result.d()).isGreaterThanOrEqualTo(0.0);
            assertThat(result.f()).isGreaterThanOrEqualTo(0.0f);
            assertThat(result.by()).isGreaterThanOrEqualTo((byte) 0);
            assertThat(result.sh()).isGreaterThanOrEqualTo((short) 0);
            assertThat(result.c()).isNotNull();
            assertThat(result.bytes()).isNotNull().hasSize(16);
            assertThat(result.date()).isNotNull();
            assertThat(result.dateTime()).isNotNull();
            assertThat(result.instant()).isNotNull();
            assertThat(result.duration()).isNotNull().isPositive();
            assertThat(result.uuid()).isNotNull();
            assertThat(result.list()).isNotNull();
        }

        @Test
        @DisplayName("should fill nested records recursively")
        void fillNestedRecord() {
            NestedRecord result = AutoFill.of(NestedRecord.class).build();

            assertThat(result).isNotNull();
            assertThat(result.title()).isNotNull().isNotEmpty();
            assertThat(result.author()).isNotNull();
            assertThat(result.author().name()).isNotNull().isNotEmpty();
            assertThat(result.author().age()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should fill enum fields with a valid constant")
        void fillEnumField() {
            EnumRecord result = AutoFill.of(EnumRecord.class).build();

            assertThat(result).isNotNull();
            assertThat(result.color()).isNotNull().isIn(Color.RED, Color.GREEN, Color.BLUE);
        }
    }

    @Nested
    @DisplayName("POJO filling")
    class PojoFilling {

        @Test
        @DisplayName("should fill a simple POJO via setters")
        void fillSimplePojo() {
            SimplePojo result = AutoFill.of(SimplePojo.class).build();

            assertThat(result).isNotNull();
            assertThat(result.getName()).isNotNull().isNotEmpty();
            assertThat(result.getAge()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should fill nested POJOs recursively")
        void fillNestedPojo() {
            NestedPojo result = AutoFill.of(NestedPojo.class).build();

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isNotNull().isNotEmpty();
            assertThat(result.getAuthor()).isNotNull();
            assertThat(result.getAuthor().getName()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Seed determinism")
    class SeedDeterminism {

        @Test
        @DisplayName("should produce identical results with the same seed")
        void sameSeedSameResult() {
            SimpleRecord first = AutoFill.of(SimpleRecord.class).seed(42L).build();
            SimpleRecord second = AutoFill.of(SimpleRecord.class).seed(42L).build();

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("should produce different results with different seeds")
        void differentSeedDifferentResult() {
            SimpleRecord first = AutoFill.of(SimpleRecord.class).seed(1L).build();
            SimpleRecord second = AutoFill.of(SimpleRecord.class).seed(2L).build();

            assertThat(first).isNotEqualTo(second);
        }
    }

    @Nested
    @DisplayName("Field overrides")
    class FieldOverrides {

        @Test
        @DisplayName("should override a single field in a record")
        void overrideSingleField() {
            SimpleRecord result = AutoFill.of(SimpleRecord.class)
                    .with("name", "Alice")
                    .build();

            assertThat(result.name()).isEqualTo("Alice");
            assertThat(result.age()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should override multiple fields in a record")
        void overrideMultipleFields() {
            SimpleRecord result = AutoFill.of(SimpleRecord.class)
                    .with("name", "Bob")
                    .with("age", 30)
                    .build();

            assertThat(result.name()).isEqualTo("Bob");
            assertThat(result.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("should override a field in a POJO")
        void overridePojoField() {
            SimplePojo result = AutoFill.of(SimplePojo.class)
                    .with("name", "Charlie")
                    .build();

            assertThat(result.getName()).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("should reject null fieldName")
        void rejectNullFieldName() {
            assertThatThrownBy(() -> AutoFill.of(SimpleRecord.class).with(null, "value"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("List generation")
    class ListGeneration {

        @Test
        @DisplayName("should generate a list of the requested size")
        void generateList() {
            List<SimpleRecord> results = AutoFill.of(SimpleRecord.class).list(5);

            assertThat(results).hasSize(5);
            assertThat(results).allSatisfy(r -> {
                assertThat(r.name()).isNotNull().isNotEmpty();
                assertThat(r.age()).isGreaterThan(0);
            });
        }

        @Test
        @DisplayName("should generate a single-element list")
        void generateSingleElementList() {
            List<SimpleRecord> results = AutoFill.of(SimpleRecord.class).list(1);

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("should return an unmodifiable list")
        void unmodifiableList() {
            List<SimpleRecord> results = AutoFill.of(SimpleRecord.class).list(3);

            assertThatThrownBy(() -> results.add(new SimpleRecord("x", 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should reject non-positive count")
        void rejectNonPositiveCount() {
            assertThatThrownBy(() -> AutoFill.of(SimpleRecord.class).list(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> AutoFill.of(SimpleRecord.class).list(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should produce deterministic list with seed")
        void deterministicList() {
            List<SimpleRecord> first = AutoFill.of(SimpleRecord.class).seed(99L).list(3);
            List<SimpleRecord> second = AutoFill.of(SimpleRecord.class).seed(99L).list(3);

            assertThat(first).isEqualTo(second);
        }
    }

    @Nested
    @DisplayName("Max depth control")
    class MaxDepthControl {

        @Test
        @DisplayName("should return null for nested object beyond max depth")
        void depthLimitReturnsNull() {
            NestedRecord result = AutoFill.of(NestedRecord.class)
                    .maxDepth(1)
                    .build();

            assertThat(result).isNotNull();
            assertThat(result.title()).isNotNull();
            // author is a nested record at depth 1, which is >= maxDepth(1), so null
            assertThat(result.author()).isNull();
        }

        @Test
        @DisplayName("should reject non-positive maxDepth")
        void rejectNonPositiveDepth() {
            assertThatThrownBy(() -> AutoFill.of(SimpleRecord.class).maxDepth(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> AutoFill.of(SimpleRecord.class).maxDepth(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCasesGroup {

        @Test
        @DisplayName("should reject null type")
        void rejectNullType() {
            assertThatThrownBy(() -> AutoFill.of(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw for class without no-arg constructor")
        void throwForNoConstructor() {
            // String has no no-arg constructor and is not a record
            // but it's handled as a primitive type by generateValue,
            // so use a custom class without no-arg constructor
            assertThatThrownBy(() -> AutoFill.of(NoDefaultConstructor.class).build())
                    .isInstanceOf(DataGenerationException.class);
        }

        @Test
        @DisplayName("generated string values are alphabetic and 8 chars long")
        void stringFormat() {
            SimpleRecord result = AutoFill.of(SimpleRecord.class).seed(1L).build();

            assertThat(result.name()).hasSize(8);
            assertThat(result.name()).matches("[a-z]{8}");
        }
    }

    // A class with no no-arg constructor for testing failure
    public static class NoDefaultConstructor {
        public NoDefaultConstructor(String required) {}
    }
}
