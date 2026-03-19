package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenArray 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenArray 测试")
class OpenArrayTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("空数组常量")
        void testEmptyArrayConstants() {
            assertThat(OpenArray.EMPTY_INT_ARRAY).isEmpty();
            assertThat(OpenArray.EMPTY_LONG_ARRAY).isEmpty();
            assertThat(OpenArray.EMPTY_DOUBLE_ARRAY).isEmpty();
            assertThat(OpenArray.EMPTY_FLOAT_ARRAY).isEmpty();
            assertThat(OpenArray.EMPTY_SHORT_ARRAY).isEmpty();
            assertThat(OpenArray.EMPTY_BYTE_ARRAY).isEmpty();
            assertThat(OpenArray.EMPTY_CHAR_ARRAY).isEmpty();
            assertThat(OpenArray.EMPTY_BOOLEAN_ARRAY).isEmpty();
            assertThat(OpenArray.EMPTY_STRING_ARRAY).isEmpty();
            assertThat(OpenArray.EMPTY_OBJECT_ARRAY).isEmpty();
        }
    }

    @Nested
    @DisplayName("创建测试")
    class CreateTests {

        @Test
        @DisplayName("newArray 创建数组")
        void testNewArray() {
            String[] arr = OpenArray.newArray(String.class, 5);
            assertThat(arr).hasSize(5);
            assertThat(arr).containsOnlyNulls();
        }

        @Test
        @DisplayName("of 创建数组")
        void testOf() {
            String[] arr = OpenArray.of("a", "b", "c");
            assertThat(arr).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("nullToEmpty 对象数组")
        void testNullToEmptyObject() {
            String[] nullArr = null;
            String[] result = OpenArray.nullToEmpty(nullArr, String[].class);
            assertThat(result).isEmpty();

            String[] nonNull = {"a"};
            assertThat(OpenArray.nullToEmpty(nonNull, String[].class)).isSameAs(nonNull);
        }

        @Test
        @DisplayName("nullToEmpty int 数组")
        void testNullToEmptyInt() {
            assertThat(OpenArray.nullToEmpty((int[]) null)).isSameAs(OpenArray.EMPTY_INT_ARRAY);
            int[] arr = {1, 2};
            assertThat(OpenArray.nullToEmpty(arr)).isSameAs(arr);
        }

        @Test
        @DisplayName("nullToEmpty long 数组")
        void testNullToEmptyLong() {
            assertThat(OpenArray.nullToEmpty((long[]) null)).isSameAs(OpenArray.EMPTY_LONG_ARRAY);
        }
    }

    @Nested
    @DisplayName("添加/插入测试")
    class AddInsertTests {

        @Test
        @DisplayName("add 在末尾添加")
        void testAdd() {
            String[] arr = {"a", "b"};
            String[] result = OpenArray.add(arr, "c");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("add null 数组")
        void testAddNullArray() {
            String[] result = OpenArray.add(null, "a");
            assertThat(result).containsExactly("a");
        }

        @Test
        @DisplayName("add 在指定位置插入")
        void testAddAtIndex() {
            String[] arr = {"a", "c"};
            String[] result = OpenArray.add(arr, 1, "b");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("addAll 合并数组")
        void testAddAll() {
            String[] arr1 = {"a", "b"};
            String[] arr2 = {"c", "d"};
            String[] result = OpenArray.addAll(arr1, arr2);
            assertThat(result).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("addAll null 处理")
        void testAddAllNull() {
            assertThat(OpenArray.addAll(null, (String[]) null)).isNull();
            String[] arr = {"a"};
            assertThat(OpenArray.addAll(null, arr)).containsExactly("a");
            assertThat(OpenArray.addAll(arr, (String[]) null)).containsExactly("a");
        }

        @Test
        @DisplayName("insert 插入多个元素")
        void testInsert() {
            String[] arr = {"a", "d"};
            String[] result = OpenArray.insert(1, arr, "b", "c");
            assertThat(result).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("insert int 数组")
        void testInsertInt() {
            int[] arr = {1, 4};
            int[] result = OpenArray.insert(1, arr, 2, 3);
            assertThat(result).containsExactly(1, 2, 3, 4);
        }
    }

    @Nested
    @DisplayName("删除测试")
    class RemoveTests {

        @Test
        @DisplayName("remove 按索引删除")
        void testRemove() {
            String[] arr = {"a", "b", "c"};
            String[] result = OpenArray.remove(arr, 1);
            assertThat(result).containsExactly("a", "c");
        }

        @Test
        @DisplayName("remove 空数组")
        void testRemoveEmpty() {
            String[] arr = {};
            assertThat(OpenArray.remove(arr, 0)).isSameAs(arr);
        }

        @Test
        @DisplayName("removeElement 按元素删除")
        void testRemoveElement() {
            String[] arr = {"a", "b", "c"};
            String[] result = OpenArray.removeElement(arr, "b");
            assertThat(result).containsExactly("a", "c");
        }

        @Test
        @DisplayName("removeElement 元素不存在")
        void testRemoveElementNotFound() {
            String[] arr = {"a", "b"};
            assertThat(OpenArray.removeElement(arr, "z")).isSameAs(arr);
        }

        @Test
        @DisplayName("removeAll 删除多个索引")
        void testRemoveAll() {
            String[] arr = {"a", "b", "c", "d", "e"};
            String[] result = OpenArray.removeAll(arr, 1, 3);
            assertThat(result).containsExactly("a", "c", "e");
        }

        @Test
        @DisplayName("removeAll int 数组")
        void testRemoveAllInt() {
            int[] arr = {1, 2, 3, 4, 5};
            int[] result = OpenArray.removeAll(arr, 0, 2, 4);
            assertThat(result).containsExactly(2, 4);
        }
    }

    @Nested
    @DisplayName("子数组测试")
    class SubarrayTests {

        @Test
        @DisplayName("subarray 对象数组")
        void testSubarray() {
            String[] arr = {"a", "b", "c", "d", "e"};
            String[] result = OpenArray.subarray(arr, 1, 4);
            assertThat(result).containsExactly("b", "c", "d");
        }

        @Test
        @DisplayName("subarray null")
        void testSubarrayNull() {
            assertThat(OpenArray.subarray((String[]) null, 0, 1)).isNull();
        }

        @Test
        @DisplayName("subarray 边界处理")
        void testSubarrayBounds() {
            String[] arr = {"a", "b", "c"};
            assertThat(OpenArray.subarray(arr, -1, 10)).containsExactly("a", "b", "c");
            assertThat(OpenArray.subarray(arr, 2, 1)).isEmpty();
        }

        @Test
        @DisplayName("subarray int")
        void testSubarrayInt() {
            int[] arr = {1, 2, 3, 4, 5};
            int[] result = OpenArray.subarray(arr, 1, 4);
            assertThat(result).containsExactly(2, 3, 4);
        }

        @Test
        @DisplayName("subarray long")
        void testSubarrayLong() {
            long[] arr = {1L, 2L, 3L, 4L, 5L};
            long[] result = OpenArray.subarray(arr, 1, 3);
            assertThat(result).containsExactly(2L, 3L);
        }

        @Test
        @DisplayName("subarray byte")
        void testSubarrayByte() {
            byte[] arr = {1, 2, 3, 4, 5};
            byte[] result = OpenArray.subarray(arr, 2, 4);
            assertThat(result).containsExactly((byte) 3, (byte) 4);
        }
    }

    @Nested
    @DisplayName("交换测试")
    class SwapTests {

        @Test
        @DisplayName("swap 对象数组")
        void testSwap() {
            String[] arr = {"a", "b", "c"};
            OpenArray.swap(arr, 0, 2);
            assertThat(arr).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("swap int 数组")
        void testSwapInt() {
            int[] arr = {1, 2, 3};
            OpenArray.swap(arr, 0, 2);
            assertThat(arr).containsExactly(3, 2, 1);
        }

        @Test
        @DisplayName("swap 指定长度")
        void testSwapWithLength() {
            Object[] arr = {"a", "b", "c", "d"};
            OpenArray.swap(arr, 0, 2, 2);
            assertThat(arr).containsExactly("c", "d", "a", "b");
        }
    }

    @Nested
    @DisplayName("操作测试")
    class OperationTests {

        @Test
        @DisplayName("reverse 对象数组")
        void testReverse() {
            String[] arr = {"a", "b", "c"};
            OpenArray.reverse(arr);
            assertThat(arr).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("reverse int 数组")
        void testReverseInt() {
            int[] arr = {1, 2, 3, 4};
            OpenArray.reverse(arr);
            assertThat(arr).containsExactly(4, 3, 2, 1);
        }

        @Test
        @DisplayName("shuffle 打乱数组")
        void testShuffle() {
            String[] arr = {"a", "b", "c", "d", "e"};
            String[] original = arr.clone();
            OpenArray.shuffle(arr);
            assertThat(arr).containsExactlyInAnyOrder(original);
        }

        @Test
        @DisplayName("rotate 旋转数组")
        void testRotate() {
            Object[] arr = {"a", "b", "c", "d"};
            OpenArray.rotate(arr, 1);
            assertThat(arr).containsExactly("d", "a", "b", "c");
        }

        @Test
        @DisplayName("rotate 负数距离")
        void testRotateNegative() {
            Object[] arr = {"a", "b", "c", "d"};
            OpenArray.rotate(arr, -1);
            assertThat(arr).containsExactly("b", "c", "d", "a");
        }
    }

    @Nested
    @DisplayName("搜索测试")
    class SearchTests {

        @Test
        @DisplayName("contains 对象数组")
        void testContains() {
            String[] arr = {"a", "b", "c"};
            assertThat(OpenArray.contains(arr, "b")).isTrue();
            assertThat(OpenArray.contains(arr, "z")).isFalse();
        }

        @Test
        @DisplayName("contains int 数组")
        void testContainsInt() {
            int[] arr = {1, 2, 3};
            assertThat(OpenArray.contains(arr, 2)).isTrue();
            assertThat(OpenArray.contains(arr, 9)).isFalse();
        }

        @Test
        @DisplayName("indexOf")
        void testIndexOf() {
            String[] arr = {"a", "b", "c", "b"};
            assertThat(OpenArray.indexOf(arr, "b")).isEqualTo(1);
            assertThat(OpenArray.indexOf(arr, "z")).isEqualTo(-1);
        }

        @Test
        @DisplayName("indexOf 从指定位置开始")
        void testIndexOfStartIndex() {
            String[] arr = {"a", "b", "c", "b"};
            assertThat(OpenArray.indexOf(arr, "b", 2)).isEqualTo(3);
        }

        @Test
        @DisplayName("lastIndexOf")
        void testLastIndexOf() {
            String[] arr = {"a", "b", "c", "b"};
            assertThat(OpenArray.lastIndexOf(arr, "b")).isEqualTo(3);
            assertThat(OpenArray.lastIndexOf(arr, "z")).isEqualTo(-1);
        }

        @Test
        @DisplayName("getFirst")
        void testGetFirst() {
            String[] arr = {"a", "b", "c"};
            assertThat(OpenArray.getFirst(arr)).contains("a");
            assertThat(OpenArray.getFirst(new String[0])).isEmpty();
        }

        @Test
        @DisplayName("getLast")
        void testGetLast() {
            String[] arr = {"a", "b", "c"};
            assertThat(OpenArray.getLast(arr)).contains("c");
            assertThat(OpenArray.getLast(new String[0])).isEmpty();
        }
    }

    @Nested
    @DisplayName("判断测试")
    class CheckTests {

        @Test
        @DisplayName("isEmpty 对象数组")
        void testIsEmpty() {
            assertThat(OpenArray.isEmpty((Object[]) null)).isTrue();
            assertThat(OpenArray.isEmpty(new String[0])).isTrue();
            assertThat(OpenArray.isEmpty(new String[]{"a"})).isFalse();
        }

        @Test
        @DisplayName("isEmpty int 数组")
        void testIsEmptyInt() {
            assertThat(OpenArray.isEmpty((int[]) null)).isTrue();
            assertThat(OpenArray.isEmpty(new int[0])).isTrue();
            assertThat(OpenArray.isEmpty(new int[]{1})).isFalse();
        }

        @Test
        @DisplayName("isNotEmpty")
        void testIsNotEmpty() {
            assertThat(OpenArray.isNotEmpty(new String[]{"a"})).isTrue();
            assertThat(OpenArray.isNotEmpty(new String[0])).isFalse();
        }

        @Test
        @DisplayName("isSameLength")
        void testIsSameLength() {
            assertThat(OpenArray.isSameLength(new String[]{"a"}, new String[]{"b"})).isTrue();
            assertThat(OpenArray.isSameLength(new String[]{"a"}, new String[]{"b", "c"})).isFalse();
            assertThat(OpenArray.isSameLength(null, null)).isTrue();
        }

        @Test
        @DisplayName("isSorted int")
        void testIsSortedInt() {
            assertThat(OpenArray.isSorted(new int[]{1, 2, 3})).isTrue();
            assertThat(OpenArray.isSorted(new int[]{1, 3, 2})).isFalse();
            assertThat(OpenArray.isSorted((int[]) null)).isTrue();
        }

        @Test
        @DisplayName("isSorted Comparable")
        void testIsSortedComparable() {
            assertThat(OpenArray.isSorted(new String[]{"a", "b", "c"})).isTrue();
            assertThat(OpenArray.isSorted(new String[]{"a", "c", "b"})).isFalse();
        }
    }

    @Nested
    @DisplayName("类型转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toPrimitive Integer")
        void testToPrimitiveInteger() {
            Integer[] arr = {1, 2, null, 4};
            int[] result = OpenArray.toPrimitive(arr);
            assertThat(result).containsExactly(1, 2, 0, 4);
        }

        @Test
        @DisplayName("toPrimitive Integer 带默认值")
        void testToPrimitiveIntegerDefault() {
            Integer[] arr = {1, null, 3};
            int[] result = OpenArray.toPrimitive(arr, -1);
            assertThat(result).containsExactly(1, -1, 3);
        }

        @Test
        @DisplayName("toPrimitive Long")
        void testToPrimitiveLong() {
            Long[] arr = {1L, 2L, null};
            long[] result = OpenArray.toPrimitive(arr);
            assertThat(result).containsExactly(1L, 2L, 0L);
        }

        @Test
        @DisplayName("toPrimitive Double")
        void testToPrimitiveDouble() {
            Double[] arr = {1.0, 2.0, null};
            double[] result = OpenArray.toPrimitive(arr);
            assertThat(result).containsExactly(1.0, 2.0, 0.0);
        }

        @Test
        @DisplayName("toPrimitive Boolean")
        void testToPrimitiveBoolean() {
            Boolean[] arr = {true, false, null};
            boolean[] result = OpenArray.toPrimitive(arr);
            assertThat(result).containsExactly(true, false, false);
        }

        @Test
        @DisplayName("toObject int")
        void testToObjectInt() {
            int[] arr = {1, 2, 3};
            Integer[] result = OpenArray.toObject(arr);
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("toObject long")
        void testToObjectLong() {
            long[] arr = {1L, 2L};
            Long[] result = OpenArray.toObject(arr);
            assertThat(result).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("toObject double")
        void testToObjectDouble() {
            double[] arr = {1.0, 2.0};
            Double[] result = OpenArray.toObject(arr);
            assertThat(result).containsExactly(1.0, 2.0);
        }

        @Test
        @DisplayName("toObject boolean")
        void testToObjectBoolean() {
            boolean[] arr = {true, false};
            Boolean[] result = OpenArray.toObject(arr);
            assertThat(result).containsExactly(true, false);
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionTests {

        @Test
        @DisplayName("toList")
        void testToList() {
            List<String> list = OpenArray.toList("a", "b", "c");
            assertThat(list).containsExactly("a", "b", "c");
            list.add("d"); // 验证可变
            assertThat(list).hasSize(4);
        }

        @Test
        @DisplayName("toList null")
        void testToListNull() {
            assertThat(OpenArray.toList((String[]) null)).isEmpty();
        }

        @Test
        @DisplayName("toSet")
        void testToSet() {
            Set<String> set = OpenArray.toSet("a", "b", "c", "a");
            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("toMap")
        void testToMap() {
            Object[][] data = {{"k1", "v1"}, {"k2", "v2"}};
            Map<String, String> map = OpenArray.toMap(data);
            assertThat(map).containsEntry("k1", "v1").containsEntry("k2", "v2");
        }
    }

    @Nested
    @DisplayName("函数式操作测试")
    class FunctionalTests {

        @Test
        @DisplayName("filter 过滤数组")
        void testFilter() {
            Integer[] arr = {1, 2, 3, 4, 5};
            Integer[] result = OpenArray.filter(arr, n -> n % 2 == 0);
            assertThat(result).containsExactly(2, 4);
        }

        @Test
        @DisplayName("filter 空数组")
        void testFilterEmpty() {
            String[] arr = {};
            assertThat(OpenArray.filter(arr, s -> true)).isSameAs(arr);
        }

        @Test
        @DisplayName("map 映射数组")
        void testMap() {
            String[] arr = {"a", "b", "c"};
            String[] result = OpenArray.map(arr, String::toUpperCase, String.class);
            assertThat(result).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("map null")
        void testMapNull() {
            assertThat(OpenArray.map((String[]) null, String::toUpperCase, String.class)).isNull();
        }
    }
}
