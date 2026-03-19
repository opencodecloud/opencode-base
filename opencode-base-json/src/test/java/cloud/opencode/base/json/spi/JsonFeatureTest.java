package cloud.opencode.base.json.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonFeature 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonFeature 测试")
class JsonFeatureTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("序列化特性存在")
        void testSerializationFeatures() {
            assertThat(JsonFeature.PRETTY_PRINT).isNotNull();
            assertThat(JsonFeature.WRITE_DATES_AS_ISO8601).isNotNull();
            assertThat(JsonFeature.WRITE_ENUMS_USING_NAME).isNotNull();
            assertThat(JsonFeature.WRITE_NULL_MAP_VALUES).isNotNull();
            assertThat(JsonFeature.WRITE_EMPTY_ARRAYS_FOR_NULL).isNotNull();
            assertThat(JsonFeature.SORT_MAP_KEYS).isNotNull();
            assertThat(JsonFeature.ESCAPE_NON_ASCII).isNotNull();
            assertThat(JsonFeature.INCLUDE_NULL_PROPERTIES).isNotNull();
            assertThat(JsonFeature.INCLUDE_EMPTY_COLLECTIONS).isNotNull();
        }

        @Test
        @DisplayName("反序列化特性存在")
        void testDeserializationFeatures() {
            assertThat(JsonFeature.IGNORE_UNKNOWN_PROPERTIES).isNotNull();
            assertThat(JsonFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY).isNotNull();
            assertThat(JsonFeature.ACCEPT_EMPTY_STRING_AS_NULL).isNotNull();
            assertThat(JsonFeature.FAIL_ON_NULL_FOR_PRIMITIVES).isNotNull();
            assertThat(JsonFeature.FAIL_ON_NUMBERS_FOR_ENUMS).isNotNull();
            assertThat(JsonFeature.USE_BIG_DECIMAL_FOR_FLOATS).isNotNull();
            assertThat(JsonFeature.USE_BIG_INTEGER_FOR_INTS).isNotNull();
            assertThat(JsonFeature.ALLOW_COMMENTS).isNotNull();
            assertThat(JsonFeature.ALLOW_TRAILING_COMMA).isNotNull();
            assertThat(JsonFeature.ALLOW_UNQUOTED_FIELD_NAMES).isNotNull();
            assertThat(JsonFeature.ALLOW_SINGLE_QUOTES).isNotNull();
        }

        @Test
        @DisplayName("安全特性存在")
        void testSecurityFeatures() {
            assertThat(JsonFeature.LIMIT_STRING_LENGTH).isNotNull();
            assertThat(JsonFeature.LIMIT_NESTING_DEPTH).isNotNull();
            assertThat(JsonFeature.LIMIT_ENTRY_COUNT).isNotNull();
        }
    }

    @Nested
    @DisplayName("getCategory方法测试")
    class GetCategoryTests {

        @Test
        @DisplayName("序列化特性返回SERIALIZATION")
        void testSerializationCategory() {
            assertThat(JsonFeature.PRETTY_PRINT.getCategory())
                .isEqualTo(JsonFeature.Category.SERIALIZATION);
            assertThat(JsonFeature.WRITE_DATES_AS_ISO8601.getCategory())
                .isEqualTo(JsonFeature.Category.SERIALIZATION);
            assertThat(JsonFeature.SORT_MAP_KEYS.getCategory())
                .isEqualTo(JsonFeature.Category.SERIALIZATION);
        }

        @Test
        @DisplayName("反序列化特性返回DESERIALIZATION")
        void testDeserializationCategory() {
            assertThat(JsonFeature.IGNORE_UNKNOWN_PROPERTIES.getCategory())
                .isEqualTo(JsonFeature.Category.DESERIALIZATION);
            assertThat(JsonFeature.ALLOW_COMMENTS.getCategory())
                .isEqualTo(JsonFeature.Category.DESERIALIZATION);
        }

        @Test
        @DisplayName("安全特性返回SECURITY")
        void testSecurityCategory() {
            assertThat(JsonFeature.LIMIT_STRING_LENGTH.getCategory())
                .isEqualTo(JsonFeature.Category.SECURITY);
            assertThat(JsonFeature.LIMIT_NESTING_DEPTH.getCategory())
                .isEqualTo(JsonFeature.Category.SECURITY);
        }
    }

    @Nested
    @DisplayName("isEnabledByDefault方法测试")
    class IsEnabledByDefaultTests {

        @Test
        @DisplayName("PRETTY_PRINT默认禁用")
        void testPrettyPrintDisabled() {
            assertThat(JsonFeature.PRETTY_PRINT.isEnabledByDefault()).isFalse();
        }

        @Test
        @DisplayName("WRITE_DATES_AS_ISO8601默认启用")
        void testWriteDatesEnabled() {
            assertThat(JsonFeature.WRITE_DATES_AS_ISO8601.isEnabledByDefault()).isTrue();
        }

        @Test
        @DisplayName("IGNORE_UNKNOWN_PROPERTIES默认启用")
        void testIgnoreUnknownEnabled() {
            assertThat(JsonFeature.IGNORE_UNKNOWN_PROPERTIES.isEnabledByDefault()).isTrue();
        }

        @Test
        @DisplayName("ALLOW_COMMENTS默认禁用")
        void testAllowCommentsDisabled() {
            assertThat(JsonFeature.ALLOW_COMMENTS.isEnabledByDefault()).isFalse();
        }

        @Test
        @DisplayName("安全特性默认启用")
        void testSecurityFeaturesEnabled() {
            assertThat(JsonFeature.LIMIT_STRING_LENGTH.isEnabledByDefault()).isTrue();
            assertThat(JsonFeature.LIMIT_NESTING_DEPTH.isEnabledByDefault()).isTrue();
            assertThat(JsonFeature.LIMIT_ENTRY_COUNT.isEnabledByDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("isSerializationFeature方法测试")
    class IsSerializationFeatureTests {

        @Test
        @DisplayName("序列化特性返回true")
        void testSerializationFeatureReturnsTrue() {
            assertThat(JsonFeature.PRETTY_PRINT.isSerializationFeature()).isTrue();
            assertThat(JsonFeature.SORT_MAP_KEYS.isSerializationFeature()).isTrue();
            assertThat(JsonFeature.ESCAPE_NON_ASCII.isSerializationFeature()).isTrue();
        }

        @Test
        @DisplayName("非序列化特性返回false")
        void testNonSerializationFeatureReturnsFalse() {
            assertThat(JsonFeature.IGNORE_UNKNOWN_PROPERTIES.isSerializationFeature()).isFalse();
            assertThat(JsonFeature.LIMIT_STRING_LENGTH.isSerializationFeature()).isFalse();
        }
    }

    @Nested
    @DisplayName("isDeserializationFeature方法测试")
    class IsDeserializationFeatureTests {

        @Test
        @DisplayName("反序列化特性返回true")
        void testDeserializationFeatureReturnsTrue() {
            assertThat(JsonFeature.IGNORE_UNKNOWN_PROPERTIES.isDeserializationFeature()).isTrue();
            assertThat(JsonFeature.ALLOW_COMMENTS.isDeserializationFeature()).isTrue();
            assertThat(JsonFeature.USE_BIG_DECIMAL_FOR_FLOATS.isDeserializationFeature()).isTrue();
        }

        @Test
        @DisplayName("非反序列化特性返回false")
        void testNonDeserializationFeatureReturnsFalse() {
            assertThat(JsonFeature.PRETTY_PRINT.isDeserializationFeature()).isFalse();
            assertThat(JsonFeature.LIMIT_NESTING_DEPTH.isDeserializationFeature()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSecurityFeature方法测试")
    class IsSecurityFeatureTests {

        @Test
        @DisplayName("安全特性返回true")
        void testSecurityFeatureReturnsTrue() {
            assertThat(JsonFeature.LIMIT_STRING_LENGTH.isSecurityFeature()).isTrue();
            assertThat(JsonFeature.LIMIT_NESTING_DEPTH.isSecurityFeature()).isTrue();
            assertThat(JsonFeature.LIMIT_ENTRY_COUNT.isSecurityFeature()).isTrue();
        }

        @Test
        @DisplayName("非安全特性返回false")
        void testNonSecurityFeatureReturnsFalse() {
            assertThat(JsonFeature.PRETTY_PRINT.isSecurityFeature()).isFalse();
            assertThat(JsonFeature.ALLOW_COMMENTS.isSecurityFeature()).isFalse();
        }
    }

    @Nested
    @DisplayName("Category枚举测试")
    class CategoryEnumTests {

        @Test
        @DisplayName("所有类别存在")
        void testAllCategories() {
            assertThat(JsonFeature.Category.values())
                .containsExactlyInAnyOrder(
                    JsonFeature.Category.SERIALIZATION,
                    JsonFeature.Category.DESERIALIZATION,
                    JsonFeature.Category.SECURITY
                );
        }

        @Test
        @DisplayName("valueOf方法")
        void testValueOf() {
            assertThat(JsonFeature.Category.valueOf("SERIALIZATION"))
                .isEqualTo(JsonFeature.Category.SERIALIZATION);
        }
    }

    @Nested
    @DisplayName("标准枚举方法测试")
    class StandardEnumMethodsTests {

        @Test
        @DisplayName("values返回所有值")
        void testValues() {
            JsonFeature[] values = JsonFeature.values();

            assertThat(values).hasSizeGreaterThan(20);
        }

        @Test
        @DisplayName("valueOf方法")
        void testValueOf() {
            assertThat(JsonFeature.valueOf("PRETTY_PRINT"))
                .isEqualTo(JsonFeature.PRETTY_PRINT);
        }

        @Test
        @DisplayName("name方法")
        void testName() {
            assertThat(JsonFeature.PRETTY_PRINT.name()).isEqualTo("PRETTY_PRINT");
        }

        @Test
        @DisplayName("ordinal方法")
        void testOrdinal() {
            assertThat(JsonFeature.PRETTY_PRINT.ordinal()).isGreaterThanOrEqualTo(0);
        }
    }
}
