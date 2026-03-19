package cloud.opencode.base.test;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenDataTest Tests
 * OpenDataTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("OpenData Tests")
class OpenDataTest {

    @Nested
    @DisplayName("Random Primitives Tests")
    class RandomPrimitivesTests {

        @Test
        @DisplayName("randomInt should return integer")
        void randomIntShouldReturnInteger() {
            int value = OpenData.randomInt();
            assertThat(value).isNotNull();
        }

        @Test
        @DisplayName("randomInt with bound should return value in range")
        void randomIntWithBoundShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                int value = OpenData.randomInt(100);
                assertThat(value).isGreaterThanOrEqualTo(0).isLessThan(100);
            }
        }

        @Test
        @DisplayName("randomInt with min and max should return value in range")
        void randomIntWithMinAndMaxShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                int value = OpenData.randomInt(10, 20);
                assertThat(value).isGreaterThanOrEqualTo(10).isLessThanOrEqualTo(20);
            }
        }

        @Test
        @DisplayName("randomLong should return long")
        void randomLongShouldReturnLong() {
            long value = OpenData.randomLong();
            assertThat(value).isNotNull();
        }

        @Test
        @DisplayName("randomLong with min and max should return value in range")
        void randomLongWithMinAndMaxShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                long value = OpenData.randomLong(100L, 200L);
                assertThat(value).isGreaterThanOrEqualTo(100L).isLessThanOrEqualTo(200L);
            }
        }

        @Test
        @DisplayName("randomDouble should return value between 0 and 1")
        void randomDoubleShouldReturnValueBetween0And1() {
            for (int i = 0; i < 100; i++) {
                double value = OpenData.randomDouble();
                assertThat(value).isGreaterThanOrEqualTo(0.0).isLessThan(1.0);
            }
        }

        @Test
        @DisplayName("randomDouble with min and max should return value in range")
        void randomDoubleWithMinAndMaxShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                double value = OpenData.randomDouble(10.0, 20.0);
                assertThat(value).isGreaterThanOrEqualTo(10.0).isLessThan(20.0);
            }
        }

        @Test
        @DisplayName("randomBoolean should return boolean values")
        void randomBooleanShouldReturnBooleanValues() {
            Set<Boolean> values = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                values.add(OpenData.randomBoolean());
            }
            assertThat(values).containsExactlyInAnyOrder(true, false);
        }

        @Test
        @DisplayName("randomFloat should return value between 0 and 1")
        void randomFloatShouldReturnValueBetween0And1() {
            for (int i = 0; i < 100; i++) {
                float value = OpenData.randomFloat();
                assertThat(value).isGreaterThanOrEqualTo(0.0f).isLessThan(1.0f);
            }
        }

        @Test
        @DisplayName("randomFloat with min and max should return value in range")
        void randomFloatWithMinAndMaxShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                float value = OpenData.randomFloat(10.0f, 20.0f);
                assertThat(value).isGreaterThanOrEqualTo(10.0f).isLessThan(20.0f);
            }
        }

        @Test
        @DisplayName("randomBytes should return correct length")
        void randomBytesShouldReturnCorrectLength() {
            byte[] bytes = OpenData.randomBytes(16);
            assertThat(bytes).hasSize(16);
        }
    }

    @Nested
    @DisplayName("Random Strings Tests")
    class RandomStringsTests {

        @Test
        @DisplayName("randomString should return alphanumeric string")
        void randomStringShouldReturnAlphanumericString() {
            String str = OpenData.randomString(20);
            assertThat(str).hasSize(20);
            assertThat(str).matches("[A-Za-z0-9]+");
        }

        @Test
        @DisplayName("randomString with characters should use those characters")
        void randomStringWithCharactersShouldUseThoseCharacters() {
            String str = OpenData.randomString(10, "abc");
            assertThat(str).hasSize(10);
            assertThat(str).matches("[abc]+");
        }

        @Test
        @DisplayName("randomAlphabetic should return letters only")
        void randomAlphabeticShouldReturnLettersOnly() {
            String str = OpenData.randomAlphabetic(20);
            assertThat(str).hasSize(20);
            assertThat(str).matches("[A-Za-z]+");
        }

        @Test
        @DisplayName("randomNumeric should return digits only")
        void randomNumericShouldReturnDigitsOnly() {
            String str = OpenData.randomNumeric(10);
            assertThat(str).hasSize(10);
            assertThat(str).matches("\\d+");
        }

        @Test
        @DisplayName("uuid should return valid UUID format")
        void uuidShouldReturnValidUuidFormat() {
            String uuid = OpenData.uuid();
            assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("randomHex should return hex characters only")
        void randomHexShouldReturnHexCharactersOnly() {
            String hex = OpenData.randomHex(16);
            assertThat(hex).hasSize(16);
            assertThat(hex).matches("[0-9a-f]+");
        }
    }

    @Nested
    @DisplayName("Fake Personal Data Tests")
    class FakePersonalDataTests {

        @Test
        @DisplayName("chineseName should return Chinese name")
        void chineseNameShouldReturnChineseName() {
            String name = OpenData.chineseName();
            assertThat(name).isNotBlank();
            assertThat(name.length()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("englishName should return first and last name")
        void englishNameShouldReturnFirstAndLastName() {
            String name = OpenData.englishName();
            assertThat(name).contains(" ");
        }

        @Test
        @DisplayName("email should return valid email format")
        void emailShouldReturnValidEmailFormat() {
            String email = OpenData.email();
            assertThat(email).contains("@");
            assertThat(email).matches("[a-z\\d]+@[a-z.\\d]+");
        }

        @Test
        @DisplayName("phone should return valid phone format")
        void phoneShouldReturnValidPhoneFormat() {
            String phone = OpenData.phone();
            assertThat(phone).hasSize(11);
            assertThat(phone).matches("1\\d{10}");
        }

        @Test
        @DisplayName("city should return city name")
        void cityShouldReturnCityName() {
            String city = OpenData.city();
            assertThat(city).isNotBlank();
        }

        @Test
        @DisplayName("age should return value in range")
        void ageShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                int age = OpenData.age(18, 65);
                assertThat(age).isGreaterThanOrEqualTo(18).isLessThanOrEqualTo(65);
            }
        }
    }

    @Nested
    @DisplayName("Random Dates Tests")
    class RandomDatesTests {

        @Test
        @DisplayName("pastDate should return date in past")
        void pastDateShouldReturnDateInPast() {
            LocalDate date = OpenData.pastDate(30);
            assertThat(date).isBeforeOrEqualTo(LocalDate.now());
            assertThat(date).isAfterOrEqualTo(LocalDate.now().minusDays(30));
        }

        @Test
        @DisplayName("futureDate should return date in future")
        void futureDateShouldReturnDateInFuture() {
            LocalDate date = OpenData.futureDate(30);
            assertThat(date).isAfterOrEqualTo(LocalDate.now());
            assertThat(date).isBeforeOrEqualTo(LocalDate.now().plusDays(30));
        }

        @Test
        @DisplayName("pastDateTime should return datetime in past")
        void pastDateTimeShouldReturnDateTimeInPast() {
            LocalDateTime dateTime = OpenData.pastDateTime(24);
            assertThat(dateTime).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("birthday should return valid birthday for age")
        void birthdayShouldReturnValidBirthdayForAge() {
            LocalDate birthday = OpenData.birthday(18, 65);
            LocalDate now = LocalDate.now();
            int age = now.getYear() - birthday.getYear();
            assertThat(age).isBetween(17, 66); // Allow some tolerance
        }

        @Test
        @DisplayName("randomDate should return date in range")
        void randomDateShouldReturnDateInRange() {
            LocalDate start = LocalDate.of(2020, 1, 1);
            LocalDate end = LocalDate.of(2020, 12, 31);

            for (int i = 0; i < 100; i++) {
                LocalDate date = OpenData.randomDate(start, end);
                assertThat(date).isAfterOrEqualTo(start).isBeforeOrEqualTo(end);
            }
        }

        @Test
        @DisplayName("randomDateTime should return datetime")
        void randomDateTimeShouldReturnDateTime() {
            LocalDateTime dateTime = OpenData.randomDateTime();
            assertThat(dateTime).isNotNull();
            assertThat(dateTime).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("randomDateTime with range should return datetime in range")
        void randomDateTimeWithRangeShouldReturnDateTimeInRange() {
            LocalDateTime start = LocalDateTime.of(2020, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2020, 12, 31, 23, 59);

            for (int i = 0; i < 100; i++) {
                LocalDateTime dateTime = OpenData.randomDateTime(start, end);
                assertThat(dateTime).isAfterOrEqualTo(start).isBeforeOrEqualTo(end);
            }
        }
    }

    @Nested
    @DisplayName("Random Money Tests")
    class RandomMoneyTests {

        @Test
        @DisplayName("randomMoney should return money with 2 decimal places")
        void randomMoneyShouldReturnMoneyWith2DecimalPlaces() {
            BigDecimal money = OpenData.randomMoney();
            assertThat(money.scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("randomMoney with range should return value in range")
        void randomMoneyWithRangeShouldReturnValueInRange() {
            for (int i = 0; i < 100; i++) {
                BigDecimal money = OpenData.randomMoney(100, 200);
                assertThat(money.doubleValue()).isGreaterThanOrEqualTo(100).isLessThanOrEqualTo(200);
            }
        }

        @Test
        @DisplayName("randomPrice should return formatted price")
        void randomPriceShouldReturnFormattedPrice() {
            BigDecimal price = OpenData.randomPrice(10, 100);
            int cents = price.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).intValue();
            assertThat(cents).isIn(0, 99);
        }
    }

    @Nested
    @DisplayName("Collection Utilities Tests")
    class CollectionUtilitiesTests {

        @Test
        @DisplayName("pick from array should return element from array")
        void pickFromArrayShouldReturnElementFromArray() {
            String[] array = {"a", "b", "c"};
            for (int i = 0; i < 100; i++) {
                String element = OpenData.pick(array);
                assertThat(element).isIn("a", "b", "c");
            }
        }

        @Test
        @DisplayName("pick from list should return element from list")
        void pickFromListShouldReturnElementFromList() {
            List<String> list = List.of("a", "b", "c");
            for (int i = 0; i < 100; i++) {
                String element = OpenData.pick(list);
                assertThat(element).isIn("a", "b", "c");
            }
        }

        @Test
        @DisplayName("pick from collection should return element from collection")
        void pickFromCollectionShouldReturnElementFromCollection() {
            Set<String> set = Set.of("a", "b", "c");
            for (int i = 0; i < 100; i++) {
                String element = OpenData.pick(set);
                assertThat(element).isIn("a", "b", "c");
            }
        }

        @Test
        @DisplayName("pickMany should return specified number of elements")
        void pickManyShouldReturnSpecifiedNumberOfElements() {
            List<String> list = List.of("a", "b", "c", "d", "e");
            List<String> picked = OpenData.pickMany(list, 3);
            assertThat(picked).hasSize(3);
        }

        @Test
        @DisplayName("pickMany should return all elements if count exceeds size")
        void pickManyShouldReturnAllElementsIfCountExceedsSize() {
            List<String> list = List.of("a", "b");
            List<String> picked = OpenData.pickMany(list, 10);
            assertThat(picked).hasSize(2);
        }

        @Test
        @DisplayName("shuffle should return shuffled list")
        void shuffleShouldReturnShuffledList() {
            List<String> list = List.of("a", "b", "c", "d", "e");
            List<String> shuffled = OpenData.shuffle(list);
            assertThat(shuffled).containsExactlyInAnyOrderElementsOf(list);
        }

        @Test
        @DisplayName("listOf should generate list with supplier")
        void listOfShouldGenerateListWithSupplier() {
            List<Integer> list = OpenData.listOf(5, () -> 42);
            assertThat(list).hasSize(5);
            assertThat(list).containsOnly(42);
        }
    }

    @Nested
    @DisplayName("Repeatable Random Tests")
    class RepeatableRandomTests {

        @Test
        @DisplayName("withSeed should produce same sequence")
        void withSeedShouldProduceSameSequence() {
            int[] values1 = new int[5];
            int[] values2 = new int[5];

            OpenData.withSeed(12345L, () -> {
                for (int i = 0; i < 5; i++) {
                    values1[i] = OpenData.randomInt(100);
                }
            });

            OpenData.withSeed(12345L, () -> {
                for (int i = 0; i < 5; i++) {
                    values2[i] = OpenData.randomInt(100);
                }
            });

            assertThat(values1).isEqualTo(values2);
        }

        @Test
        @DisplayName("withSeed with supplier should return result")
        void withSeedWithSupplierShouldReturnResult() {
            int result = OpenData.withSeed(12345L, () -> OpenData.randomInt(100));
            int result2 = OpenData.withSeed(12345L, () -> OpenData.randomInt(100));

            assertThat(result).isEqualTo(result2);
        }
    }
}
