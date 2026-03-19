package cloud.opencode.base.core.random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenRandom 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenRandom 测试")
class OpenRandomTest {

    @Nested
    @DisplayName("基本随机数测试")
    class BasicRandomTests {

        @RepeatedTest(10)
        @DisplayName("randomInt bound")
        void testRandomIntBound() {
            int value = OpenRandom.randomInt(100);
            assertThat(value).isBetween(0, 99);
        }

        @RepeatedTest(10)
        @DisplayName("randomInt origin and bound")
        void testRandomIntOriginBound() {
            int value = OpenRandom.randomInt(10, 20);
            assertThat(value).isBetween(10, 19);
        }

        @RepeatedTest(10)
        @DisplayName("randomLong bound")
        void testRandomLongBound() {
            long value = OpenRandom.randomLong(1000);
            assertThat(value).isBetween(0L, 999L);
        }

        @RepeatedTest(10)
        @DisplayName("randomLong origin and bound")
        void testRandomLongOriginBound() {
            long value = OpenRandom.randomLong(100, 200);
            assertThat(value).isBetween(100L, 199L);
        }

        @RepeatedTest(10)
        @DisplayName("randomDouble no args")
        void testRandomDoubleNoArgs() {
            double value = OpenRandom.randomDouble();
            assertThat(value).isBetween(0.0, 1.0);
        }

        @RepeatedTest(10)
        @DisplayName("randomDouble bound")
        void testRandomDoubleBound() {
            double value = OpenRandom.randomDouble(10.0);
            assertThat(value).isBetween(0.0, 10.0);
        }

        @RepeatedTest(10)
        @DisplayName("randomDouble origin and bound")
        void testRandomDoubleOriginBound() {
            double value = OpenRandom.randomDouble(5.0, 10.0);
            assertThat(value).isBetween(5.0, 10.0);
        }

        @Test
        @DisplayName("randomBoolean 返回 true 或 false")
        void testRandomBoolean() {
            boolean hasTrue = false;
            boolean hasFalse = false;

            for (int i = 0; i < 100 && !(hasTrue && hasFalse); i++) {
                boolean value = OpenRandom.randomBoolean();
                if (value) hasTrue = true;
                else hasFalse = true;
            }

            assertThat(hasTrue && hasFalse).isTrue();
        }

        @Test
        @DisplayName("randomBytes")
        void testRandomBytes() {
            byte[] bytes = OpenRandom.randomBytes(16);
            assertThat(bytes).hasSize(16);
        }
    }

    @Nested
    @DisplayName("安全随机测试")
    class SecureRandomTests {

        @Test
        @DisplayName("secureBytes")
        void testSecureBytes() {
            byte[] bytes = OpenRandom.secureBytes(32);
            assertThat(bytes).hasSize(32);
        }

        @RepeatedTest(10)
        @DisplayName("secureInt")
        void testSecureInt() {
            int value = OpenRandom.secureInt(100);
            assertThat(value).isBetween(0, 99);
        }

        @Test
        @DisplayName("secureLong")
        void testSecureLong() {
            // 只测试不抛异常
            long value = OpenRandom.secureLong();
            assertThat(value).isNotNull();
        }
    }

    @Nested
    @DisplayName("随机字符串测试")
    class RandomStringTests {

        @Test
        @DisplayName("randomAlphanumeric")
        void testRandomAlphanumeric() {
            String str = OpenRandom.randomAlphanumeric(10);
            assertThat(str).hasSize(10);
            assertThat(str).matches("[A-Za-z0-9]+");
        }

        @Test
        @DisplayName("secureAlphanumeric")
        void testSecureAlphanumeric() {
            String str = OpenRandom.secureAlphanumeric(10);
            assertThat(str).hasSize(10);
            assertThat(str).matches("[A-Za-z0-9]+");
        }

        @Test
        @DisplayName("randomNumeric")
        void testRandomNumeric() {
            String str = OpenRandom.randomNumeric(8);
            assertThat(str).hasSize(8);
            assertThat(str).matches("[0-9]+");
        }

        @Test
        @DisplayName("randomAlphabetic")
        void testRandomAlphabetic() {
            String str = OpenRandom.randomAlphabetic(8);
            assertThat(str).hasSize(8);
            assertThat(str).matches("[A-Za-z]+");
        }

        @Test
        @DisplayName("randomUpperCase")
        void testRandomUpperCase() {
            String str = OpenRandom.randomUpperCase(8);
            assertThat(str).hasSize(8);
            assertThat(str).matches("[A-Z]+");
        }

        @Test
        @DisplayName("randomLowerCase")
        void testRandomLowerCase() {
            String str = OpenRandom.randomLowerCase(8);
            assertThat(str).hasSize(8);
            assertThat(str).matches("[a-z]+");
        }

        @Test
        @DisplayName("randomString 自定义字符集")
        void testRandomStringCustom() {
            String str = OpenRandom.randomString(5, "ABC");
            assertThat(str).hasSize(5);
            assertThat(str).matches("[ABC]+");
        }

        @Test
        @DisplayName("randomString 空参数")
        void testRandomStringEmpty() {
            assertThat(OpenRandom.randomString(0, "ABC")).isEmpty();
            assertThat(OpenRandom.randomString(-1, "ABC")).isEmpty();
            assertThat(OpenRandom.randomString(5, null)).isEmpty();
            assertThat(OpenRandom.randomString(5, "")).isEmpty();
        }

        @Test
        @DisplayName("secureString 自定义字符集")
        void testSecureStringCustom() {
            String str = OpenRandom.secureString(5, "XYZ");
            assertThat(str).hasSize(5);
            assertThat(str).matches("[XYZ]+");
        }

        @Test
        @DisplayName("secureString 空参数")
        void testSecureStringEmpty() {
            assertThat(OpenRandom.secureString(0, "ABC")).isEmpty();
            assertThat(OpenRandom.secureString(5, null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("UUID 测试")
    class UUIDTests {

        @Test
        @DisplayName("uuid")
        void testUuid() {
            String uuid = OpenRandom.uuid();
            assertThat(uuid).hasSize(36);
            assertThat(uuid).matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
        }

        @Test
        @DisplayName("simpleUUID")
        void testSimpleUUID() {
            String uuid = OpenRandom.simpleUUID();
            assertThat(uuid).hasSize(32);
            assertThat(uuid).matches("[a-f0-9]{32}");
        }

        @Test
        @DisplayName("secureUUID")
        void testSecureUUID() {
            String uuid = OpenRandom.secureUUID();
            assertThat(uuid).hasSize(36);
            assertThat(uuid).contains("-");
        }

        @Test
        @DisplayName("UUID 唯一性")
        void testUUIDUniqueness() {
            String uuid1 = OpenRandom.uuid();
            String uuid2 = OpenRandom.uuid();
            assertThat(uuid1).isNotEqualTo(uuid2);
        }
    }

    @Nested
    @DisplayName("集合随机测试")
    class CollectionRandomTests {

        @Test
        @DisplayName("randomElement List")
        void testRandomElementList() {
            List<String> list = List.of("a", "b", "c");
            String element = OpenRandom.randomElement(list);
            assertThat(element).isIn("a", "b", "c");
        }

        @Test
        @DisplayName("randomElement 空 List")
        void testRandomElementEmptyList() {
            List<String> list = List.of();
            String element = OpenRandom.randomElement(list);
            assertThat(element).isNull();
        }

        @Test
        @DisplayName("randomElement null List")
        void testRandomElementNullList() {
            String element = OpenRandom.randomElement((List<String>) null);
            assertThat(element).isNull();
        }

        @Test
        @DisplayName("randomElement Array")
        void testRandomElementArray() {
            String element = OpenRandom.randomElement("x", "y", "z");
            assertThat(element).isIn("x", "y", "z");
        }

        @Test
        @DisplayName("randomElement 空 Array")
        void testRandomElementEmptyArray() {
            String element = OpenRandom.randomElement();
            assertThat(element).isNull();
        }

        @Test
        @DisplayName("randomElements")
        void testRandomElements() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);
            List<Integer> result = OpenRandom.randomElements(list, 3);
            assertThat(result).hasSize(3);
            assertThat(list).containsAll(result);
        }

        @Test
        @DisplayName("randomElements count >= size")
        void testRandomElementsCountGreaterThanSize() {
            List<Integer> list = List.of(1, 2, 3);
            List<Integer> result = OpenRandom.randomElements(list, 10);
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("randomElements 空 List")
        void testRandomElementsEmptyList() {
            List<Integer> result = OpenRandom.randomElements(List.of(), 3);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("shuffle List")
        void testShuffleList() {
            List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
            List<Integer> original = new ArrayList<>(list);

            // 多次 shuffle 应该至少有一次顺序不同
            boolean shuffled = false;
            for (int i = 0; i < 10; i++) {
                List<Integer> copy = new ArrayList<>(original);
                OpenRandom.shuffle(copy);
                if (!copy.equals(original)) {
                    shuffled = true;
                    break;
                }
            }

            // 理论上随机不可能10次都一样（概率极低）
            assertThat(shuffled).isTrue();
        }

        @Test
        @DisplayName("shuffle Array")
        void testShuffleArray() {
            Integer[] array = {1, 2, 3, 4, 5};
            Integer[] original = array.clone();

            boolean shuffled = false;
            for (int i = 0; i < 10; i++) {
                Integer[] copy = original.clone();
                OpenRandom.shuffle(copy);
                if (!Arrays.equals(copy, original)) {
                    shuffled = true;
                    break;
                }
            }

            assertThat(shuffled).isTrue();
        }
    }

    @Nested
    @DisplayName("日期随机测试")
    class DateRandomTests {

        @RepeatedTest(10)
        @DisplayName("randomDate 年份范围")
        void testRandomDateYearRange() {
            LocalDate date = OpenRandom.randomDate(2020, 2025);
            assertThat(date.getYear()).isBetween(2020, 2024);
        }

        @RepeatedTest(10)
        @DisplayName("randomDate LocalDate 范围")
        void testRandomDateLocalDateRange() {
            LocalDate start = LocalDate.of(2023, 1, 1);
            LocalDate end = LocalDate.of(2023, 12, 31);
            LocalDate date = OpenRandom.randomDate(start, end);
            assertThat(date).isAfterOrEqualTo(start);
            assertThat(date).isBefore(end);
        }
    }
}
