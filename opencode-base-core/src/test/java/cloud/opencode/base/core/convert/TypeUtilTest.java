package cloud.opencode.base.core.convert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("TypeUtil 测试")
class TypeUtilTest {

    // 测试用类
    static class TestClass extends ArrayList<String> implements Comparable<TestClass> {
        List<Integer> listField;
        Map<String, Double> mapField;

        public List<String> getList() { return null; }
        public Map<Integer, Boolean> getMap() { return null; }

        @Override
        public int compareTo(TestClass o) { return 0; }
    }

    @Nested
    @DisplayName("原始类型判断测试")
    class PrimitiveCheckTests {

        @Test
        @DisplayName("isPrimitive")
        void testIsPrimitive() {
            assertThat(TypeUtil.isPrimitive(int.class)).isTrue();
            assertThat(TypeUtil.isPrimitive(long.class)).isTrue();
            assertThat(TypeUtil.isPrimitive(double.class)).isTrue();
            assertThat(TypeUtil.isPrimitive(float.class)).isTrue();
            assertThat(TypeUtil.isPrimitive(boolean.class)).isTrue();
            assertThat(TypeUtil.isPrimitive(byte.class)).isTrue();
            assertThat(TypeUtil.isPrimitive(short.class)).isTrue();
            assertThat(TypeUtil.isPrimitive(char.class)).isTrue();
            assertThat(TypeUtil.isPrimitive(void.class)).isTrue();

            assertThat(TypeUtil.isPrimitive(Integer.class)).isFalse();
            assertThat(TypeUtil.isPrimitive(String.class)).isFalse();
            assertThat(TypeUtil.isPrimitive(null)).isFalse();
        }

        @Test
        @DisplayName("isWrapper")
        void testIsWrapper() {
            assertThat(TypeUtil.isWrapper(Integer.class)).isTrue();
            assertThat(TypeUtil.isWrapper(Long.class)).isTrue();
            assertThat(TypeUtil.isWrapper(Double.class)).isTrue();
            assertThat(TypeUtil.isWrapper(Float.class)).isTrue();
            assertThat(TypeUtil.isWrapper(Boolean.class)).isTrue();
            assertThat(TypeUtil.isWrapper(Byte.class)).isTrue();
            assertThat(TypeUtil.isWrapper(Short.class)).isTrue();
            assertThat(TypeUtil.isWrapper(Character.class)).isTrue();
            assertThat(TypeUtil.isWrapper(Void.class)).isTrue();

            assertThat(TypeUtil.isWrapper(int.class)).isFalse();
            assertThat(TypeUtil.isWrapper(String.class)).isFalse();
            assertThat(TypeUtil.isWrapper(null)).isFalse();
        }

        @Test
        @DisplayName("isPrimitiveOrWrapper")
        void testIsPrimitiveOrWrapper() {
            assertThat(TypeUtil.isPrimitiveOrWrapper(int.class)).isTrue();
            assertThat(TypeUtil.isPrimitiveOrWrapper(Integer.class)).isTrue();
            assertThat(TypeUtil.isPrimitiveOrWrapper(long.class)).isTrue();
            assertThat(TypeUtil.isPrimitiveOrWrapper(Long.class)).isTrue();

            assertThat(TypeUtil.isPrimitiveOrWrapper(String.class)).isFalse();
            assertThat(TypeUtil.isPrimitiveOrWrapper(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("数字类型判断测试")
    class NumberCheckTests {

        @Test
        @DisplayName("isNumber 原始类型")
        void testIsNumberPrimitive() {
            assertThat(TypeUtil.isNumber(int.class)).isTrue();
            assertThat(TypeUtil.isNumber(long.class)).isTrue();
            assertThat(TypeUtil.isNumber(double.class)).isTrue();
            assertThat(TypeUtil.isNumber(float.class)).isTrue();
            assertThat(TypeUtil.isNumber(byte.class)).isTrue();
            assertThat(TypeUtil.isNumber(short.class)).isTrue();
        }

        @Test
        @DisplayName("isNumber 包装类型")
        void testIsNumberWrapper() {
            assertThat(TypeUtil.isNumber(Integer.class)).isTrue();
            assertThat(TypeUtil.isNumber(Long.class)).isTrue();
            assertThat(TypeUtil.isNumber(Double.class)).isTrue();
            assertThat(TypeUtil.isNumber(Float.class)).isTrue();
            assertThat(TypeUtil.isNumber(Byte.class)).isTrue();
            assertThat(TypeUtil.isNumber(Short.class)).isTrue();
        }

        @Test
        @DisplayName("isNumber 大数类型")
        void testIsNumberBigNumber() {
            assertThat(TypeUtil.isNumber(BigDecimal.class)).isTrue();
            assertThat(TypeUtil.isNumber(BigInteger.class)).isTrue();
        }

        @Test
        @DisplayName("isNumber 非数字类型")
        void testIsNumberNonNumber() {
            assertThat(TypeUtil.isNumber(String.class)).isFalse();
            assertThat(TypeUtil.isNumber(boolean.class)).isFalse();
            assertThat(TypeUtil.isNumber(char.class)).isFalse();
            assertThat(TypeUtil.isNumber(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("集合类型判断测试")
    class CollectionCheckTests {

        @Test
        @DisplayName("isCollection")
        void testIsCollection() {
            assertThat(TypeUtil.isCollection(List.class)).isTrue();
            assertThat(TypeUtil.isCollection(Set.class)).isTrue();
            assertThat(TypeUtil.isCollection(ArrayList.class)).isTrue();
            assertThat(TypeUtil.isCollection(HashSet.class)).isTrue();
            assertThat(TypeUtil.isCollection(Queue.class)).isTrue();

            assertThat(TypeUtil.isCollection(Map.class)).isFalse();
            assertThat(TypeUtil.isCollection(String.class)).isFalse();
            assertThat(TypeUtil.isCollection(null)).isFalse();
        }

        @Test
        @DisplayName("isMap")
        void testIsMap() {
            assertThat(TypeUtil.isMap(Map.class)).isTrue();
            assertThat(TypeUtil.isMap(HashMap.class)).isTrue();
            assertThat(TypeUtil.isMap(TreeMap.class)).isTrue();
            assertThat(TypeUtil.isMap(LinkedHashMap.class)).isTrue();

            assertThat(TypeUtil.isMap(List.class)).isFalse();
            assertThat(TypeUtil.isMap(String.class)).isFalse();
            assertThat(TypeUtil.isMap(null)).isFalse();
        }

        @Test
        @DisplayName("isArray")
        void testIsArray() {
            assertThat(TypeUtil.isArray(int[].class)).isTrue();
            assertThat(TypeUtil.isArray(String[].class)).isTrue();
            assertThat(TypeUtil.isArray(Object[].class)).isTrue();

            assertThat(TypeUtil.isArray(List.class)).isFalse();
            assertThat(TypeUtil.isArray(String.class)).isFalse();
            assertThat(TypeUtil.isArray(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("字符串类型判断测试")
    class StringCheckTests {

        @Test
        @DisplayName("isString")
        void testIsString() {
            assertThat(TypeUtil.isString(String.class)).isTrue();
            assertThat(TypeUtil.isString(CharSequence.class)).isTrue();

            assertThat(TypeUtil.isString(StringBuilder.class)).isFalse();
            assertThat(TypeUtil.isString(Integer.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("日期时间类型判断测试")
    class DateTimeCheckTests {

        @Test
        @DisplayName("isDateTime")
        void testIsDateTime() {
            assertThat(TypeUtil.isDateTime(LocalDate.class)).isTrue();
            assertThat(TypeUtil.isDateTime(LocalDateTime.class)).isTrue();
            assertThat(TypeUtil.isDateTime(LocalTime.class)).isTrue();
            assertThat(TypeUtil.isDateTime(Instant.class)).isTrue();
            assertThat(TypeUtil.isDateTime(ZonedDateTime.class)).isTrue();
            assertThat(TypeUtil.isDateTime(OffsetDateTime.class)).isTrue();
            assertThat(TypeUtil.isDateTime(Date.class)).isTrue();

            assertThat(TypeUtil.isDateTime(String.class)).isFalse();
            assertThat(TypeUtil.isDateTime(Long.class)).isFalse();
            assertThat(TypeUtil.isDateTime(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("类型转换测试")
    class TypeConversionTests {

        @Test
        @DisplayName("getWrapperClass")
        void testGetWrapperClass() {
            assertThat(TypeUtil.getWrapperClass(int.class)).isEqualTo(Integer.class);
            assertThat(TypeUtil.getWrapperClass(long.class)).isEqualTo(Long.class);
            assertThat(TypeUtil.getWrapperClass(double.class)).isEqualTo(Double.class);
            assertThat(TypeUtil.getWrapperClass(float.class)).isEqualTo(Float.class);
            assertThat(TypeUtil.getWrapperClass(boolean.class)).isEqualTo(Boolean.class);
            assertThat(TypeUtil.getWrapperClass(byte.class)).isEqualTo(Byte.class);
            assertThat(TypeUtil.getWrapperClass(short.class)).isEqualTo(Short.class);
            assertThat(TypeUtil.getWrapperClass(char.class)).isEqualTo(Character.class);
            assertThat(TypeUtil.getWrapperClass(void.class)).isEqualTo(Void.class);
        }

        @Test
        @DisplayName("getPrimitiveClass")
        void testGetPrimitiveClass() {
            assertThat(TypeUtil.getPrimitiveClass(Integer.class)).isEqualTo(int.class);
            assertThat(TypeUtil.getPrimitiveClass(Long.class)).isEqualTo(long.class);
            assertThat(TypeUtil.getPrimitiveClass(Double.class)).isEqualTo(double.class);
            assertThat(TypeUtil.getPrimitiveClass(Float.class)).isEqualTo(float.class);
            assertThat(TypeUtil.getPrimitiveClass(Boolean.class)).isEqualTo(boolean.class);
            assertThat(TypeUtil.getPrimitiveClass(Byte.class)).isEqualTo(byte.class);
            assertThat(TypeUtil.getPrimitiveClass(Short.class)).isEqualTo(short.class);
            assertThat(TypeUtil.getPrimitiveClass(Character.class)).isEqualTo(char.class);
            assertThat(TypeUtil.getPrimitiveClass(Void.class)).isEqualTo(void.class);
        }

        @Test
        @DisplayName("getDefaultValue")
        void testGetDefaultValue() {
            assertThat(TypeUtil.getDefaultValue(int.class)).isEqualTo(0);
            assertThat(TypeUtil.getDefaultValue(long.class)).isEqualTo(0L);
            assertThat(TypeUtil.getDefaultValue(double.class)).isEqualTo(0.0d);
            assertThat(TypeUtil.getDefaultValue(float.class)).isEqualTo(0.0f);
            assertThat(TypeUtil.getDefaultValue(boolean.class)).isEqualTo(false);
            assertThat(TypeUtil.getDefaultValue(byte.class)).isEqualTo((byte) 0);
            assertThat(TypeUtil.getDefaultValue(short.class)).isEqualTo((short) 0);
            assertThat(TypeUtil.getDefaultValue(char.class)).isEqualTo('\0');

            assertThat(TypeUtil.getDefaultValue(String.class)).isNull();
            assertThat(TypeUtil.getDefaultValue((Class<?>) null)).isNull();
        }
    }

    @Nested
    @DisplayName("convert 测试")
    class ConvertTests {

        @Test
        @DisplayName("convert 基本类型")
        void testConvert() {
            assertThat(TypeUtil.convert("123", Integer.class)).isEqualTo(123);
            assertThat(TypeUtil.convert(123, String.class)).isEqualTo("123");
            assertThat(TypeUtil.convert("true", Boolean.class)).isTrue();
        }

        @Test
        @DisplayName("convert null")
        void testConvertNull() {
            assertThat(TypeUtil.convert(null, Integer.class)).isNull();
        }

        @Test
        @DisplayName("convert 同类型")
        void testConvertSameType() {
            String str = "hello";
            assertThat(TypeUtil.convert(str, String.class)).isSameAs(str);
        }

        @Test
        @DisplayName("convertOptional")
        void testConvertOptional() {
            Optional<Integer> result = TypeUtil.convertOptional("123", Integer.class);
            assertThat(result).contains(123);

            Optional<Integer> empty = TypeUtil.convertOptional(null, Integer.class);
            assertThat(empty).isEmpty();
        }
    }

    @Nested
    @DisplayName("泛型类型获取测试")
    class GenericTypeTests {

        @Test
        @DisplayName("getGenericTypes")
        void testGetGenericTypes() throws Exception {
            Field listField = TestClass.class.getDeclaredField("listField");
            Type[] types = TypeUtil.getGenericTypes(listField.getGenericType());
            assertThat(types).containsExactly(Integer.class);

            Field mapField = TestClass.class.getDeclaredField("mapField");
            Type[] mapTypes = TypeUtil.getGenericTypes(mapField.getGenericType());
            assertThat(mapTypes).containsExactly(String.class, Double.class);
        }

        @Test
        @DisplayName("getGenericTypes 非参数化类型")
        void testGetGenericTypesNonParameterized() {
            Type[] types = TypeUtil.getGenericTypes(String.class);
            assertThat(types).isEmpty();
        }

        @Test
        @DisplayName("getRawType")
        void testGetRawType() throws Exception {
            Field listField = TestClass.class.getDeclaredField("listField");
            Class<?> rawType = TypeUtil.getRawType(listField.getGenericType());
            assertThat(rawType).isEqualTo(List.class);
        }

        @Test
        @DisplayName("getRawType Class")
        void testGetRawTypeClass() {
            assertThat(TypeUtil.getRawType(String.class)).isEqualTo(String.class);
        }

        @Test
        @DisplayName("getFieldGenericType")
        void testGetFieldGenericType() throws Exception {
            Field listField = TestClass.class.getDeclaredField("listField");
            Type type = TypeUtil.getFieldGenericType(listField);
            assertThat(type.getTypeName()).contains("List");
            assertThat(type.getTypeName()).contains("Integer");
        }

        @Test
        @DisplayName("getMethodReturnGenericType")
        void testGetMethodReturnGenericType() throws Exception {
            Method getList = TestClass.class.getDeclaredMethod("getList");
            Type type = TypeUtil.getMethodReturnGenericType(getList);
            assertThat(type.getTypeName()).contains("List");
            assertThat(type.getTypeName()).contains("String");

            Method getMap = TestClass.class.getDeclaredMethod("getMap");
            Type mapType = TypeUtil.getMethodReturnGenericType(getMap);
            assertThat(mapType.getTypeName()).contains("Map");
            assertThat(mapType.getTypeName()).contains("Integer");
            assertThat(mapType.getTypeName()).contains("Boolean");
        }

        @Test
        @DisplayName("getSuperclassGenericTypes")
        void testGetSuperclassGenericTypes() {
            Type[] types = TypeUtil.getSuperclassGenericTypes(TestClass.class);
            assertThat(types).containsExactly(String.class);
        }

        @Test
        @DisplayName("getSuperclassGenericTypes 非泛型父类")
        void testGetSuperclassGenericTypesNonGeneric() {
            Type[] types = TypeUtil.getSuperclassGenericTypes(String.class);
            assertThat(types).isEmpty();
        }

        @Test
        @DisplayName("getInterfaceGenericTypes")
        void testGetInterfaceGenericTypes() {
            Type[] types = TypeUtil.getInterfaceGenericTypes(TestClass.class, Comparable.class);
            assertThat(types).containsExactly(TestClass.class);
        }

        @Test
        @DisplayName("getInterfaceGenericTypes 不存在的接口")
        void testGetInterfaceGenericTypesNotFound() {
            Type[] types = TypeUtil.getInterfaceGenericTypes(TestClass.class, Runnable.class);
            assertThat(types).isEmpty();
        }
    }

    @Nested
    @DisplayName("类型兼容性测试")
    class AssignabilityTests {

        @Test
        @DisplayName("isAssignable 基本类型")
        void testIsAssignableBasic() {
            assertThat(TypeUtil.isAssignable(Object.class, String.class)).isTrue();
            assertThat(TypeUtil.isAssignable(Number.class, Integer.class)).isTrue();
            assertThat(TypeUtil.isAssignable(List.class, ArrayList.class)).isTrue();

            assertThat(TypeUtil.isAssignable(String.class, Object.class)).isFalse();
            assertThat(TypeUtil.isAssignable(Integer.class, String.class)).isFalse();
        }

        @Test
        @DisplayName("isAssignable 原始类型和包装类型")
        void testIsAssignablePrimitiveWrapper() {
            assertThat(TypeUtil.isAssignable(int.class, Integer.class)).isTrue();
            assertThat(TypeUtil.isAssignable(Integer.class, int.class)).isTrue();
            assertThat(TypeUtil.isAssignable(long.class, Long.class)).isTrue();
            assertThat(TypeUtil.isAssignable(Long.class, long.class)).isTrue();
        }

        @Test
        @DisplayName("isAssignable null")
        void testIsAssignableNull() {
            assertThat(TypeUtil.isAssignable(null, String.class)).isFalse();
            assertThat(TypeUtil.isAssignable(String.class, null)).isFalse();
            assertThat(TypeUtil.isAssignable(null, null)).isFalse();
        }
    }
}
