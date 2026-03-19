package cloud.opencode.base.test.data;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * TestDataGeneratorTest Tests
 * TestDataGeneratorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("TestDataGenerator Tests")
class TestDataGeneratorTest {

    @Nested
    @DisplayName("String Generators Tests")
    class StringGeneratorsTests {

        @Test
        @DisplayName("randomString should return alphanumeric string")
        void randomStringShouldReturnAlphanumericString() {
            String str = TestDataGenerator.randomString(20);
            assertThat(str).hasSize(20);
            assertThat(str).matches("[A-Za-z0-9]+");
        }

        @Test
        @DisplayName("randomAlpha should return letters only")
        void randomAlphaShouldReturnLettersOnly() {
            String str = TestDataGenerator.randomAlpha(20);
            assertThat(str).hasSize(20);
            assertThat(str).matches("[A-Za-z]+");
        }

        @Test
        @DisplayName("randomNumeric should return digits only")
        void randomNumericShouldReturnDigitsOnly() {
            String str = TestDataGenerator.randomNumeric(10);
            assertThat(str).hasSize(10);
            assertThat(str).matches("\\d+");
        }

        @Test
        @DisplayName("uuid should return valid UUID format")
        void uuidShouldReturnValidUuidFormat() {
            String uuid = TestDataGenerator.uuid();
            assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("shortUuid should return 8 character string")
        void shortUuidShouldReturn8CharacterString() {
            String shortUuid = TestDataGenerator.shortUuid();
            assertThat(shortUuid).hasSize(8);
            assertThat(shortUuid).matches("[0-9a-f]+");
        }
    }

    @Nested
    @DisplayName("Name Generators Tests")
    class NameGeneratorsTests {

        @Test
        @DisplayName("randomFirstName should return non-blank name")
        void randomFirstNameShouldReturnNonBlankName() {
            String name = TestDataGenerator.randomFirstName();
            assertThat(name).isNotBlank();
        }

        @Test
        @DisplayName("randomLastName should return non-blank name")
        void randomLastNameShouldReturnNonBlankName() {
            String name = TestDataGenerator.randomLastName();
            assertThat(name).isNotBlank();
        }

        @Test
        @DisplayName("randomFullName should return first and last name")
        void randomFullNameShouldReturnFirstAndLastName() {
            String name = TestDataGenerator.randomFullName();
            assertThat(name).contains(" ");
        }

        @Test
        @DisplayName("randomEmail should return valid email format")
        void randomEmailShouldReturnValidEmailFormat() {
            String email = TestDataGenerator.randomEmail();
            assertThat(email).contains("@");
            assertThat(email).matches("[a-z.]+@[a-z.]+");
        }

        @Test
        @DisplayName("randomPhone should return phone number")
        void randomPhoneShouldReturnPhoneNumber() {
            String phone = TestDataGenerator.randomPhone();
            assertThat(phone).hasSize(11);
            assertThat(phone).startsWith("1");
            assertThat(phone).matches("1\\d{10}");
        }
    }

    @Nested
    @DisplayName("Number Generators Tests")
    class NumberGeneratorsTests {

        @Test
        @DisplayName("randomInt should return integer")
        void randomIntShouldReturnInteger() {
            int value = TestDataGenerator.randomInt();
            assertThat(value).isNotNull();
        }

        @Test
        @DisplayName("randomInt with max should return value in range")
        void randomIntWithMaxShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                int value = TestDataGenerator.randomInt(100);
                assertThat(value).isGreaterThanOrEqualTo(0).isLessThan(100);
            }
        }

        @Test
        @DisplayName("randomInt with min and max should return value in range")
        void randomIntWithMinAndMaxShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                int value = TestDataGenerator.randomInt(10, 20);
                assertThat(value).isGreaterThanOrEqualTo(10).isLessThan(20);
            }
        }

        @Test
        @DisplayName("randomLong should return long")
        void randomLongShouldReturnLong() {
            long value = TestDataGenerator.randomLong();
            assertThat(value).isNotNull();
        }

        @Test
        @DisplayName("randomLong with max should return value in range")
        void randomLongWithMaxShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                long value = TestDataGenerator.randomLong(100L);
                assertThat(value).isGreaterThanOrEqualTo(0L).isLessThan(100L);
            }
        }

        @Test
        @DisplayName("randomDouble should return value between 0 and 1")
        void randomDoubleShouldReturnValueBetween0And1() {
            for (int i = 0; i < 100; i++) {
                double value = TestDataGenerator.randomDouble();
                assertThat(value).isGreaterThanOrEqualTo(0.0).isLessThan(1.0);
            }
        }

        @Test
        @DisplayName("randomDouble with max should return value in range")
        void randomDoubleWithMaxShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                double value = TestDataGenerator.randomDouble(100.0);
                assertThat(value).isGreaterThanOrEqualTo(0.0).isLessThan(100.0);
            }
        }

        @Test
        @DisplayName("randomBoolean should return boolean values")
        void randomBooleanShouldReturnBooleanValues() {
            Set<Boolean> values = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                values.add(TestDataGenerator.randomBoolean());
            }
            assertThat(values).containsExactlyInAnyOrder(true, false);
        }
    }

    @Nested
    @DisplayName("DateTime Generators Tests")
    class DateTimeGeneratorsTests {

        @Test
        @DisplayName("randomInstant should return instant in past")
        void randomInstantShouldReturnInstantInPast() {
            Instant instant = TestDataGenerator.randomInstant();
            assertThat(instant).isBeforeOrEqualTo(Instant.now());
            assertThat(instant).isAfterOrEqualTo(Instant.parse("2020-01-01T00:00:00Z"));
        }

        @Test
        @DisplayName("randomDate should return date in valid range")
        void randomDateShouldReturnDateInValidRange() {
            LocalDate date = TestDataGenerator.randomDate();
            assertThat(date).isAfterOrEqualTo(LocalDate.of(2020, 1, 1));
            assertThat(date).isBeforeOrEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("randomDateTime should return datetime")
        void randomDateTimeShouldReturnDateTime() {
            LocalDateTime dateTime = TestDataGenerator.randomDateTime();
            assertThat(dateTime).isNotNull();
            assertThat(dateTime.getYear()).isBetween(2020, 2025);
        }
    }

    @Nested
    @DisplayName("Collection Generators Tests")
    class CollectionGeneratorsTests {

        @Test
        @DisplayName("randomStrings should return list of strings")
        void randomStringsShouldReturnListOfStrings() {
            List<String> strings = TestDataGenerator.randomStrings(5, 10);
            assertThat(strings).hasSize(5);
            assertThat(strings).allMatch(s -> s.length() == 10);
        }

        @Test
        @DisplayName("randomInts should return list of integers")
        void randomIntsShouldReturnListOfIntegers() {
            List<Integer> ints = TestDataGenerator.randomInts(5, 100);
            assertThat(ints).hasSize(5);
            assertThat(ints).allMatch(i -> i >= 0 && i < 100);
        }

        @Test
        @DisplayName("randomBytes should return byte array")
        void randomBytesShouldReturnByteArray() {
            byte[] bytes = TestDataGenerator.randomBytes(16);
            assertThat(bytes).hasSize(16);
        }
    }

    @Nested
    @DisplayName("Choice Methods Tests")
    class ChoiceMethodsTests {

        @Test
        @DisplayName("oneOf with array should return element from array")
        void oneOfWithArrayShouldReturnElementFromArray() {
            for (int i = 0; i < 100; i++) {
                String result = TestDataGenerator.oneOf("a", "b", "c");
                assertThat(result).isIn("a", "b", "c");
            }
        }

        @Test
        @DisplayName("oneOf with list should return element from list")
        void oneOfWithListShouldReturnElementFromList() {
            List<String> list = List.of("a", "b", "c");
            for (int i = 0; i < 100; i++) {
                String result = TestDataGenerator.oneOf(list);
                assertThat(result).isIn("a", "b", "c");
            }
        }
    }
}
