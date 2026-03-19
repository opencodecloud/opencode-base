package cloud.opencode.base.test.data;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * DataGeneratorTest Tests
 * DataGeneratorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("DataGenerator Tests")
class DataGeneratorTest {

    @Nested
    @DisplayName("String Generation Tests")
    class StringGenerationTests {

        @Test
        @DisplayName("string should return alphanumeric string of specified length")
        void stringShouldReturnAlphanumericStringOfSpecifiedLength() {
            String result = DataGenerator.string(20);
            assertThat(result).hasSize(20);
            assertThat(result).matches("[A-Za-z0-9]+");
        }

        @Test
        @DisplayName("string should return empty for zero length")
        void stringShouldReturnEmptyForZeroLength() {
            String result = DataGenerator.string(0);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("alpha should return letters only")
        void alphaShouldReturnLettersOnly() {
            String result = DataGenerator.alpha(20);
            assertThat(result).hasSize(20);
            assertThat(result).matches("[A-Za-z]+");
        }

        @Test
        @DisplayName("numeric should return digits only")
        void numericShouldReturnDigitsOnly() {
            String result = DataGenerator.numeric(10);
            assertThat(result).hasSize(10);
            assertThat(result).matches("\\d+");
        }

        @Test
        @DisplayName("randomString should use custom character set")
        void randomStringShouldUseCustomCharacterSet() {
            String result = DataGenerator.randomString(10, "ABC");
            assertThat(result).hasSize(10);
            assertThat(result).matches("[ABC]+");
        }
    }

    @Nested
    @DisplayName("Number Generation Tests")
    class NumberGenerationTests {

        @Test
        @DisplayName("intBetween should return value in range")
        void intBetweenShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                int result = DataGenerator.intBetween(10, 20);
                assertThat(result).isBetween(10, 19);
            }
        }

        @Test
        @DisplayName("longBetween should return value in range")
        void longBetweenShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                long result = DataGenerator.longBetween(100L, 200L);
                assertThat(result).isBetween(100L, 199L);
            }
        }

        @Test
        @DisplayName("doubleBetween should return value in range")
        void doubleBetweenShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                double result = DataGenerator.doubleBetween(0.0, 10.0);
                assertThat(result).isGreaterThanOrEqualTo(0.0).isLessThan(10.0);
            }
        }

        @Test
        @DisplayName("decimal should return BigDecimal with correct scale")
        void decimalShouldReturnBigDecimalWithCorrectScale() {
            BigDecimal result = DataGenerator.decimal(0.0, 100.0, 2);
            assertThat(result.scale()).isEqualTo(2);
            assertThat(result.doubleValue()).isBetween(0.0, 100.0);
        }

        @Test
        @DisplayName("bool should return boolean")
        void boolShouldReturnBoolean() {
            Set<Boolean> values = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                values.add(DataGenerator.bool());
            }
            assertThat(values).containsExactlyInAnyOrder(true, false);
        }

        @Test
        @DisplayName("bool with probability should respect probability")
        void boolWithProbabilityShouldRespectProbability() {
            int trueCount = 0;
            for (int i = 0; i < 1000; i++) {
                if (DataGenerator.bool(0.8)) {
                    trueCount++;
                }
            }
            // Should be around 800, allow some variance
            assertThat(trueCount).isBetween(700, 900);
        }
    }

    @Nested
    @DisplayName("DateTime Generation Tests")
    class DateTimeGenerationTests {

        @Test
        @DisplayName("dateBetween should return date in range")
        void dateBetweenShouldReturnDateInRange() {
            LocalDate start = LocalDate.of(2020, 1, 1);
            LocalDate end = LocalDate.of(2020, 12, 31);
            for (int i = 0; i < 100; i++) {
                LocalDate result = DataGenerator.dateBetween(start, end);
                assertThat(result).isAfterOrEqualTo(start).isBeforeOrEqualTo(end);
            }
        }

        @Test
        @DisplayName("dateTimeBetween should return datetime in range")
        void dateTimeBetweenShouldReturnDateTimeInRange() {
            LocalDateTime start = LocalDateTime.of(2020, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2020, 12, 31, 23, 59);
            LocalDateTime result = DataGenerator.dateTimeBetween(start, end);
            assertThat(result).isAfterOrEqualTo(start).isBeforeOrEqualTo(end);
        }

        @Test
        @DisplayName("instantBetween should return instant in range")
        void instantBetweenShouldReturnInstantInRange() {
            Instant start = Instant.parse("2020-01-01T00:00:00Z");
            Instant end = Instant.parse("2020-12-31T23:59:59Z");
            Instant result = DataGenerator.instantBetween(start, end);
            assertThat(result).isAfterOrEqualTo(start).isBeforeOrEqualTo(end);
        }
    }

    @Nested
    @DisplayName("Collection Generation Tests")
    class CollectionGenerationTests {

        @Test
        @DisplayName("list should return list of correct size")
        void listShouldReturnListOfCorrectSize() {
            List<String> result = DataGenerator.list(5, () -> DataGenerator.string(10));
            assertThat(result).hasSize(5);
            assertThat(result).allMatch(s -> s.length() == 10);
        }

        @Test
        @DisplayName("map should return map of correct size")
        void mapShouldReturnMapOfCorrectSize() {
            Map<String, Integer> result = DataGenerator.map(5,
                () -> DataGenerator.string(5),
                () -> DataGenerator.intBetween(1, 100));
            assertThat(result.size()).isLessThanOrEqualTo(5); // May have duplicates
        }

        @Test
        @DisplayName("bytes should return byte array of correct length")
        void bytesShouldReturnByteArrayOfCorrectLength() {
            byte[] result = DataGenerator.bytes(16);
            assertThat(result).hasSize(16);
        }
    }

    @Nested
    @DisplayName("Selection Tests")
    class SelectionTests {

        @Test
        @DisplayName("oneOf with array should return element from array")
        void oneOfWithArrayShouldReturnElementFromArray() {
            for (int i = 0; i < 100; i++) {
                String result = DataGenerator.oneOf("a", "b", "c");
                assertThat(result).isIn("a", "b", "c");
            }
        }

        @Test
        @DisplayName("oneOf with array should throw for null")
        void oneOfWithArrayShouldThrowForNull() {
            assertThatNullPointerException().isThrownBy(() ->
                DataGenerator.oneOf((String[]) null));
        }

        @Test
        @DisplayName("oneOf with array should throw for empty")
        void oneOfWithArrayShouldThrowForEmpty() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                DataGenerator.oneOf(new String[0]));
        }

        @Test
        @DisplayName("oneOf with list should return element from list")
        void oneOfWithListShouldReturnElementFromList() {
            List<String> list = List.of("a", "b", "c");
            for (int i = 0; i < 100; i++) {
                String result = DataGenerator.oneOf(list);
                assertThat(result).isIn("a", "b", "c");
            }
        }

        @Test
        @DisplayName("oneOf with list should throw for null")
        void oneOfWithListShouldThrowForNull() {
            assertThatNullPointerException().isThrownBy(() ->
                DataGenerator.oneOf((List<String>) null));
        }

        @Test
        @DisplayName("oneOf with list should throw for empty")
        void oneOfWithListShouldThrowForEmpty() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                DataGenerator.oneOf(List.of()));
        }
    }

    @Nested
    @DisplayName("Record Generation Tests")
    class RecordGenerationTests {

        record TestRecord(String name, int age, boolean active) {}

        @Test
        @DisplayName("record should generate record with default values")
        void recordShouldGenerateRecordWithDefaultValues() {
            TestRecord result = DataGenerator.record(TestRecord.class);
            assertThat(result).isNotNull();
            assertThat(result.name()).isNotNull();
            assertThat(result.age()).isGreaterThan(0);
        }

        @Test
        @DisplayName("record should throw for null")
        void recordShouldThrowForNull() {
            assertThatNullPointerException().isThrownBy(() ->
                DataGenerator.record(null));
        }

        @Test
        @DisplayName("record should throw for non-record class")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void recordShouldThrowForNonRecordClass() {
            // Use raw type to bypass compile-time generic check and test runtime validation
            Class rawClass = String.class;
            assertThatIllegalArgumentException().isThrownBy(() ->
                DataGenerator.record(rawClass));
        }
    }
}
