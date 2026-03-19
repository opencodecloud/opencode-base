package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenObject 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenObject 测试")
class OpenObjectTest {

    @Nested
    @DisplayName("空值判断测试")
    class NullCheckTests {

        @Test
        @DisplayName("isNull 检查 null")
        void testIsNullTrue() {
            assertThat(OpenObject.isNull(null)).isTrue();
        }

        @Test
        @DisplayName("isNull 检查非 null")
        void testIsNullFalse() {
            assertThat(OpenObject.isNull("hello")).isFalse();
        }

        @Test
        @DisplayName("isNotNull 检查非 null")
        void testIsNotNullTrue() {
            assertThat(OpenObject.isNotNull("hello")).isTrue();
        }

        @Test
        @DisplayName("isNotNull 检查 null")
        void testIsNotNullFalse() {
            assertThat(OpenObject.isNotNull(null)).isFalse();
        }

        @Test
        @DisplayName("isEmpty - null")
        void testIsEmptyNull() {
            assertThat(OpenObject.isEmpty(null)).isTrue();
        }

        @Test
        @DisplayName("isEmpty - 空字符串")
        void testIsEmptyString() {
            assertThat(OpenObject.isEmpty("")).isTrue();
            assertThat(OpenObject.isEmpty("hello")).isFalse();
        }

        @Test
        @DisplayName("isEmpty - 空数组")
        void testIsEmptyArray() {
            assertThat(OpenObject.isEmpty(new int[0])).isTrue();
            assertThat(OpenObject.isEmpty(new int[]{1})).isFalse();
        }

        @Test
        @DisplayName("isEmpty - 空集合")
        void testIsEmptyCollection() {
            assertThat(OpenObject.isEmpty(new ArrayList<>())).isTrue();
            assertThat(OpenObject.isEmpty(List.of("a"))).isFalse();
        }

        @Test
        @DisplayName("isEmpty - 空 Map")
        void testIsEmptyMap() {
            assertThat(OpenObject.isEmpty(new HashMap<>())).isTrue();
            assertThat(OpenObject.isEmpty(Map.of("k", "v"))).isFalse();
        }

        @Test
        @DisplayName("isEmpty - 空 Optional")
        void testIsEmptyOptional() {
            assertThat(OpenObject.isEmpty(Optional.empty())).isTrue();
            assertThat(OpenObject.isEmpty(Optional.of("a"))).isFalse();
        }

        @Test
        @DisplayName("isNotEmpty")
        void testIsNotEmpty() {
            assertThat(OpenObject.isNotEmpty("hello")).isTrue();
            assertThat(OpenObject.isNotEmpty("")).isFalse();
        }

        @Test
        @DisplayName("isAnyNull")
        void testIsAnyNull() {
            assertThat(OpenObject.isAnyNull("a", null, "b")).isTrue();
            assertThat(OpenObject.isAnyNull("a", "b", "c")).isFalse();
            assertThat(OpenObject.isAnyNull()).isTrue();
            assertThat(OpenObject.isAnyNull((Object[]) null)).isTrue();
        }

        @Test
        @DisplayName("isAllNull")
        void testIsAllNull() {
            assertThat(OpenObject.isAllNull(null, null, null)).isTrue();
            assertThat(OpenObject.isAllNull(null, "a", null)).isFalse();
            assertThat(OpenObject.isAllNull()).isTrue();
        }

        @Test
        @DisplayName("isAnyEmpty")
        void testIsAnyEmpty() {
            assertThat(OpenObject.isAnyEmpty("a", "", "b")).isTrue();
            assertThat(OpenObject.isAnyEmpty("a", "b", "c")).isFalse();
        }

        @Test
        @DisplayName("isAllEmpty")
        void testIsAllEmpty() {
            assertThat(OpenObject.isAllEmpty("", null, new ArrayList<>())).isTrue();
            assertThat(OpenObject.isAllEmpty("", "a")).isFalse();
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("defaultIfNull 非 null")
        void testDefaultIfNullNotNull() {
            assertThat(OpenObject.defaultIfNull("hello", "default")).isEqualTo("hello");
        }

        @Test
        @DisplayName("defaultIfNull null")
        void testDefaultIfNullIsNull() {
            assertThat(OpenObject.defaultIfNull(null, "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("defaultIfNull 使用 Supplier")
        void testDefaultIfNullSupplier() {
            assertThat(OpenObject.defaultIfNull(null, () -> "supplied")).isEqualTo("supplied");
            assertThat(OpenObject.defaultIfNull("hello", () -> "supplied")).isEqualTo("hello");
        }

        @Test
        @DisplayName("defaultIfEmpty")
        void testDefaultIfEmpty() {
            assertThat(OpenObject.defaultIfEmpty("", "default")).isEqualTo("default");
            assertThat(OpenObject.defaultIfEmpty("hello", "default")).isEqualTo("hello");
        }

        @Test
        @DisplayName("firstNonNull")
        void testFirstNonNull() {
            assertThat(OpenObject.firstNonNull(null, null, "third")).isEqualTo("third");
            assertThat(OpenObject.firstNonNull("first", "second")).isEqualTo("first");
            assertThat(OpenObject.firstNonNull((Object[]) null)).isNull();
        }

        @Test
        @DisplayName("firstNonNull 使用 Supplier")
        void testFirstNonNullSupplier() {
            assertThat(OpenObject.firstNonNull(() -> "supplied", null, null)).isEqualTo("supplied");
        }

        @Test
        @DisplayName("requireNonNullElseGet")
        void testRequireNonNullElseGet() {
            assertThat(OpenObject.requireNonNullElseGet("hello", () -> "default")).isEqualTo("hello");
            assertThat(OpenObject.requireNonNullElseGet(null, () -> "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("安全获取测试")
    class NullSafeGetTests {

        @Test
        @DisplayName("nullSafeGet 成功获取")
        void testNullSafeGetSuccess() {
            String value = OpenObject.nullSafeGet("hello", String::toUpperCase);
            assertThat(value).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("nullSafeGet 对象为 null")
        void testNullSafeGetNull() {
            String value = OpenObject.nullSafeGet(null, (Function<String, String>) String::toUpperCase);
            assertThat(value).isNull();
        }

        @Test
        @DisplayName("nullSafeGet 带默认值")
        void testNullSafeGetDefault() {
            String value = OpenObject.nullSafeGet(null, (Function<String, String>) String::toUpperCase, "default");
            assertThat(value).isEqualTo("default");
        }

        @Test
        @DisplayName("nullSafeGetOptional")
        void testNullSafeGetOptional() {
            Optional<String> opt = OpenObject.nullSafeGetOptional("hello", String::toUpperCase);
            assertThat(opt).contains("HELLO");

            Optional<String> empty = OpenObject.nullSafeGetOptional(null, (Function<String, String>) String::toUpperCase);
            assertThat(empty).isEmpty();
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("equals 相等")
        void testEqualsTrue() {
            assertThat(OpenObject.equals("hello", "hello")).isTrue();
            assertThat(OpenObject.equals(null, null)).isTrue();
        }

        @Test
        @DisplayName("equals 不相等")
        void testEqualsFalse() {
            assertThat(OpenObject.equals("hello", "world")).isFalse();
            assertThat(OpenObject.equals("hello", null)).isFalse();
        }

        @Test
        @DisplayName("notEquals")
        void testNotEquals() {
            assertThat(OpenObject.notEquals("hello", "world")).isTrue();
            assertThat(OpenObject.notEquals("hello", "hello")).isFalse();
        }

        @Test
        @DisplayName("deepEquals 数组")
        void testDeepEquals() {
            int[] arr1 = {1, 2, 3};
            int[] arr2 = {1, 2, 3};
            int[] arr3 = {1, 2, 4};

            assertThat(OpenObject.deepEquals(arr1, arr2)).isTrue();
            assertThat(OpenObject.deepEquals(arr1, arr3)).isFalse();
        }

        @Test
        @DisplayName("compare Comparable")
        void testCompare() {
            assertThat(OpenObject.compare("a", "b")).isLessThan(0);
            assertThat(OpenObject.compare("b", "a")).isGreaterThan(0);
            assertThat(OpenObject.compare("a", "a")).isEqualTo(0);
        }

        @Test
        @DisplayName("compare 处理 null")
        void testCompareNull() {
            assertThat(OpenObject.compare(null, "a", false)).isLessThan(0);
            assertThat(OpenObject.compare(null, "a", true)).isGreaterThan(0);
        }

        @Test
        @DisplayName("max 和 min")
        void testMaxMin() {
            assertThat(OpenObject.max("a", "b")).isEqualTo("b");
            assertThat(OpenObject.min("a", "b")).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("类型判断测试")
    class TypeCheckTests {

        @Test
        @DisplayName("isBasicType")
        void testIsBasicType() {
            assertThat(OpenObject.isBasicType(42)).isTrue();
            assertThat(OpenObject.isBasicType("hello")).isFalse();
            assertThat(OpenObject.isBasicType(null)).isFalse();
        }

        @Test
        @DisplayName("isArray")
        void testIsArray() {
            assertThat(OpenObject.isArray(new int[]{1, 2})).isTrue();
            assertThat(OpenObject.isArray(new String[]{"a"})).isTrue();
            assertThat(OpenObject.isArray("hello")).isFalse();
        }

        @Test
        @DisplayName("isPrimitiveArray")
        void testIsPrimitiveArray() {
            assertThat(OpenObject.isPrimitiveArray(new int[]{1, 2})).isTrue();
            assertThat(OpenObject.isPrimitiveArray(new String[]{"a"})).isFalse();
        }

        @Test
        @DisplayName("isInstance")
        void testIsInstance() {
            assertThat(OpenObject.isInstance("hello", String.class)).isTrue();
            assertThat(OpenObject.isInstance("hello", Integer.class)).isFalse();
        }

        @Test
        @DisplayName("getType")
        void testGetType() {
            assertThat(OpenObject.getType("hello")).isEqualTo(String.class);
            assertThat(OpenObject.getType(null)).isNull();
        }

        @Test
        @DisplayName("isWrapperType")
        void testIsWrapperType() {
            assertThat(OpenObject.isWrapperType(Integer.class)).isTrue();
            assertThat(OpenObject.isWrapperType(String.class)).isFalse();
        }

        @Test
        @DisplayName("isPrimitiveOrWrapper")
        void testIsPrimitiveOrWrapper() {
            assertThat(OpenObject.isPrimitiveOrWrapper(int.class)).isTrue();
            assertThat(OpenObject.isPrimitiveOrWrapper(Integer.class)).isTrue();
            assertThat(OpenObject.isPrimitiveOrWrapper(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("哈希测试")
    class HashTests {

        @Test
        @DisplayName("hashCode")
        void testHashCode() {
            int hash1 = OpenObject.hashCode("a", "b", "c");
            int hash2 = OpenObject.hashCode("a", "b", "c");
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("identityHashCode")
        void testIdentityHashCode() {
            String s = "hello";
            assertThat(OpenObject.identityHashCode(s)).isEqualTo(System.identityHashCode(s));
        }
    }

    @Nested
    @DisplayName("克隆测试")
    class CloneTests {

        @Test
        @DisplayName("clone null")
        void testCloneNull() {
            assertThat((Object) OpenObject.clone(null)).isNull();
        }

        @Test
        @DisplayName("cloneIfPossible")
        void testCloneIfPossible() {
            String s = "hello";
            assertThat(OpenObject.cloneIfPossible(s)).isEqualTo(s);
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("serialize 和 deserialize")
        void testSerializeDeserialize() {
            String original = "hello";
            byte[] bytes = OpenObject.serialize(original);
            String restored = OpenObject.deserialize(bytes);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("serialize null")
        void testSerializeNull() {
            byte[] bytes = OpenObject.serialize(null);
            assertThat(bytes).isEmpty();
        }

        @Test
        @DisplayName("deserialize null")
        void testDeserializeNull() {
            assertThat((Object) OpenObject.deserialize(null)).isNull();
            assertThat((Object) OpenObject.deserialize(new byte[0])).isNull();
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class ToStringTests {

        @Test
        @DisplayName("toString 对象")
        void testToString() {
            assertThat(OpenObject.toString("hello")).isEqualTo("hello");
            assertThat(OpenObject.toString(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("toString 带默认值")
        void testToStringDefault() {
            assertThat(OpenObject.toString(null, "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("toString 数组")
        void testToStringArray() {
            int[] arr = {1, 2, 3};
            assertThat(OpenObject.toString(arr)).isEqualTo("[1, 2, 3]");
        }

        @Test
        @DisplayName("toDebugString")
        void testToDebugString() {
            String debug = OpenObject.toDebugString("hello");
            assertThat(debug).startsWith("String@");
            assertThat(debug).contains("hello");
        }
    }

    @Nested
    @DisplayName("Optional 测试")
    class OptionalTests {

        @Test
        @DisplayName("toOptional")
        void testToOptional() {
            assertThat(OpenObject.toOptional("hello")).contains("hello");
            assertThat(OpenObject.toOptional(null)).isEmpty();
        }
    }
}
