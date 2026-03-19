package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenEnum 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenEnum 测试")
class OpenEnumTest {

    // 测试用枚举
    enum Status {
        ACTIVE, INACTIVE, PENDING
    }

    enum Priority {
        LOW(1), MEDIUM(2), HIGH(3);

        private final int code;

        Priority(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    @Nested
    @DisplayName("获取枚举值测试")
    class GetEnumTests {

        @Test
        @DisplayName("getEnumByName")
        void testGetEnumByName() {
            Status status = OpenEnum.getEnumByName(Status.class, "ACTIVE");
            assertThat(status).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("getEnumByName 无效名称抛异常")
        void testGetEnumByNameInvalid() {
            assertThatThrownBy(() -> OpenEnum.getEnumByName(Status.class, "UNKNOWN"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("getEnumByNameSafely")
        void testGetEnumByNameSafely() {
            assertThat(OpenEnum.getEnumByNameSafely(Status.class, "ACTIVE", Status.PENDING))
                    .isEqualTo(Status.ACTIVE);
            assertThat(OpenEnum.getEnumByNameSafely(Status.class, "UNKNOWN", Status.PENDING))
                    .isEqualTo(Status.PENDING);
            assertThat(OpenEnum.getEnumByNameSafely(Status.class, null, Status.PENDING))
                    .isEqualTo(Status.PENDING);
            assertThat(OpenEnum.getEnumByNameSafely(null, "ACTIVE", Status.PENDING))
                    .isEqualTo(Status.PENDING);
        }

        @Test
        @DisplayName("getEnumByNameOptional")
        void testGetEnumByNameOptional() {
            assertThat(OpenEnum.getEnumByNameOptional(Status.class, "ACTIVE"))
                    .contains(Status.ACTIVE);
            assertThat(OpenEnum.getEnumByNameOptional(Status.class, "UNKNOWN"))
                    .isEmpty();
            assertThat(OpenEnum.getEnumByNameOptional(Status.class, null))
                    .isNotPresent();
            assertThat(OpenEnum.getEnumByNameOptional((Class<Status>) null, "ACTIVE"))
                    .isNotPresent();
        }

        @Test
        @DisplayName("getEnumByNameIgnoreCase")
        void testGetEnumByNameIgnoreCase() {
            assertThat(OpenEnum.getEnumByNameIgnoreCase(Status.class, "active"))
                    .isEqualTo(Status.ACTIVE);
            assertThat(OpenEnum.getEnumByNameIgnoreCase(Status.class, "ACTIVE"))
                    .isEqualTo(Status.ACTIVE);
            assertThat(OpenEnum.getEnumByNameIgnoreCase(Status.class, "Active"))
                    .isEqualTo(Status.ACTIVE);
            assertThat(OpenEnum.getEnumByNameIgnoreCase(Status.class, "unknown"))
                    .isNull();
            assertThat(OpenEnum.getEnumByNameIgnoreCase(Status.class, null))
                    .isNull();
            assertThat(OpenEnum.getEnumByNameIgnoreCase((Class<Status>) null, "active"))
                    .isNull();
        }

        @Test
        @DisplayName("getEnumByValue")
        void testGetEnumByValue() {
            Priority priority = OpenEnum.getEnumByValue(Priority.class, 2, Priority::getCode);
            assertThat(priority).isEqualTo(Priority.MEDIUM);

            assertThat(OpenEnum.getEnumByValue(Priority.class, 99, Priority::getCode))
                    .isNull();
            assertThat(OpenEnum.getEnumByValue(null, 1, Priority::getCode))
                    .isNull();
            assertThat(OpenEnum.getEnumByValue(Priority.class, 1, null))
                    .isNull();
        }

        @Test
        @DisplayName("getEnumByValue 带默认值")
        void testGetEnumByValueDefault() {
            assertThat(OpenEnum.getEnumByValue(Priority.class, 99, Priority::getCode, Priority.LOW))
                    .isEqualTo(Priority.LOW);
            assertThat(OpenEnum.getEnumByValue(Priority.class, 2, Priority::getCode, Priority.LOW))
                    .isEqualTo(Priority.MEDIUM);
        }
    }

    @Nested
    @DisplayName("获取枚举集合测试")
    class GetEnumCollectionTests {

        @Test
        @DisplayName("getEnumList")
        void testGetEnumList() {
            List<Status> list = OpenEnum.getEnumList(Status.class);
            assertThat(list).containsExactly(Status.ACTIVE, Status.INACTIVE, Status.PENDING);
        }

        @Test
        @DisplayName("getEnumList null")
        void testGetEnumListNull() {
            assertThat(OpenEnum.getEnumList(null)).isEmpty();
        }

        @Test
        @DisplayName("getEnumSet")
        void testGetEnumSet() {
            EnumSet<Status> set = OpenEnum.getEnumSet(Status.class);
            assertThat(set).containsExactly(Status.ACTIVE, Status.INACTIVE, Status.PENDING);
        }

        @Test
        @DisplayName("getEnumSet null 抛异常")
        void testGetEnumSetNull() {
            assertThatThrownBy(() -> OpenEnum.getEnumSet(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("getEnumValueMap")
        void testGetEnumValueMap() {
            Map<Integer, Priority> map = OpenEnum.getEnumValueMap(Priority.class, Priority::getCode);
            assertThat(map).hasSize(3);
            assertThat(map.get(1)).isEqualTo(Priority.LOW);
            assertThat(map.get(2)).isEqualTo(Priority.MEDIUM);
            assertThat(map.get(3)).isEqualTo(Priority.HIGH);
        }

        @Test
        @DisplayName("getEnumValueMap null")
        void testGetEnumValueMapNull() {
            assertThat(OpenEnum.getEnumValueMap(null, Priority::getCode)).isEmpty();
            assertThat(OpenEnum.getEnumValueMap(Priority.class, null)).isEmpty();
        }

        @Test
        @DisplayName("getEnumNames")
        void testGetEnumNames() {
            List<String> names = OpenEnum.getEnumNames(Status.class);
            assertThat(names).containsExactly("ACTIVE", "INACTIVE", "PENDING");
        }

        @Test
        @DisplayName("getEnumNames null")
        void testGetEnumNamesNull() {
            assertThat(OpenEnum.getEnumNames(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("isValidEnum")
        void testIsValidEnum() {
            assertThat(OpenEnum.isValidEnum(Status.class, "ACTIVE")).isTrue();
            assertThat(OpenEnum.isValidEnum(Status.class, "UNKNOWN")).isFalse();
            assertThat(OpenEnum.isValidEnum(Status.class, null)).isFalse();
        }

        @Test
        @DisplayName("isValidEnumIgnoreCase")
        void testIsValidEnumIgnoreCase() {
            assertThat(OpenEnum.isValidEnumIgnoreCase(Status.class, "active")).isTrue();
            assertThat(OpenEnum.isValidEnumIgnoreCase(Status.class, "ACTIVE")).isTrue();
            assertThat(OpenEnum.isValidEnumIgnoreCase(Status.class, "unknown")).isFalse();
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityTests {

        @Test
        @DisplayName("ordinal")
        void testOrdinal() {
            assertThat(OpenEnum.ordinal(Status.ACTIVE)).isEqualTo(0);
            assertThat(OpenEnum.ordinal(Status.INACTIVE)).isEqualTo(1);
            assertThat(OpenEnum.ordinal(Status.PENDING)).isEqualTo(2);
            assertThat(OpenEnum.ordinal(null)).isEqualTo(-1);
        }

        @Test
        @DisplayName("name")
        void testName() {
            assertThat(OpenEnum.name(Status.ACTIVE)).isEqualTo("ACTIVE");
            assertThat(OpenEnum.name(null)).isNull();
        }

        @Test
        @DisplayName("getByOrdinal")
        void testGetByOrdinal() {
            assertThat(OpenEnum.getByOrdinal(Status.class, 0)).isEqualTo(Status.ACTIVE);
            assertThat(OpenEnum.getByOrdinal(Status.class, 1)).isEqualTo(Status.INACTIVE);
            assertThat(OpenEnum.getByOrdinal(Status.class, 2)).isEqualTo(Status.PENDING);
            assertThat(OpenEnum.getByOrdinal(Status.class, -1)).isNull();
            assertThat(OpenEnum.getByOrdinal(Status.class, 99)).isNull();
            assertThat(OpenEnum.getByOrdinal((Class<Status>) null, 0)).isNull();
        }
    }
}
