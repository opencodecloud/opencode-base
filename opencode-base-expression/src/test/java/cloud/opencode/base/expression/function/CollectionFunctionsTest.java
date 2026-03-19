package cloud.opencode.base.expression.function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CollectionFunctions Tests
 * CollectionFunctions 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("CollectionFunctions Tests | CollectionFunctions 测试")
class CollectionFunctionsTest {

    private static Map<String, Function> functions;

    @BeforeAll
    static void setup() {
        functions = CollectionFunctions.getFunctions();
    }

    @Nested
    @DisplayName("Size/Count Tests | 大小/计数测试")
    class SizeCountTests {

        @Test
        @DisplayName("size function with list | size 函数使用列表")
        void testSizeWithList() {
            Function size = functions.get("size");
            assertThat(size.apply(List.of(1, 2, 3))).isEqualTo(3);
            assertThat(size.apply(List.of())).isEqualTo(0);
        }

        @Test
        @DisplayName("size function with map | size 函数使用映射")
        void testSizeWithMap() {
            Function size = functions.get("size");
            assertThat(size.apply(Map.of("a", 1, "b", 2))).isEqualTo(2);
        }

        @Test
        @DisplayName("size function with array | size 函数使用数组")
        void testSizeWithArray() {
            Function size = functions.get("size");
            assertThat(size.apply((Object) new String[]{"a", "b", "c"})).isEqualTo(3);
        }

        @Test
        @DisplayName("size function with string | size 函数使用字符串")
        void testSizeWithString() {
            Function size = functions.get("size");
            assertThat(size.apply("hello")).isEqualTo(5);
        }

        @Test
        @DisplayName("size function with null | size 函数使用 null")
        void testSizeWithNull() {
            Function size = functions.get("size");
            assertThat(size.apply((Object) null)).isEqualTo(0);
            assertThat(size.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("size function with other | size 函数使用其他类型")
        void testSizeWithOther() {
            Function size = functions.get("size");
            assertThat(size.apply(42)).isEqualTo(1);
        }

        @Test
        @DisplayName("count alias | count 别名")
        void testCount() {
            assertThat(functions.get("count")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Empty Check Tests | 空检查测试")
    class EmptyCheckTests {

        @Test
        @DisplayName("empty function | empty 函数")
        void testEmpty() {
            Function empty = functions.get("empty");
            assertThat(empty.apply(List.of())).isEqualTo(true);
            assertThat(empty.apply(List.of(1))).isEqualTo(false);
            assertThat(empty.apply(Map.of())).isEqualTo(true);
            assertThat(empty.apply((Object) new String[]{})).isEqualTo(true);
            assertThat(empty.apply("")).isEqualTo(true);
            assertThat(empty.apply((Object) null)).isEqualTo(true);
        }

        @Test
        @DisplayName("notempty function | notempty 函数")
        void testNotEmpty() {
            Function notempty = functions.get("notempty");
            assertThat(notempty.apply(List.of(1))).isEqualTo(true);
            assertThat(notempty.apply(List.of())).isEqualTo(false);
            assertThat(notempty.apply((Object) null)).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("First/Last Tests | 首/尾元素测试")
    class FirstLastTests {

        @Test
        @DisplayName("first function | first 函数")
        void testFirst() {
            Function first = functions.get("first");
            assertThat(first.apply(List.of(1, 2, 3))).isEqualTo(1);
            assertThat(first.apply(List.of())).isNull();
            assertThat(first.apply((Object) new String[]{"a", "b"})).isEqualTo("a");
            assertThat(first.apply(Set.of(1))).isEqualTo(1);
            assertThat(first.apply((Object) null)).isNull();
            assertThat(first.apply(42)).isEqualTo(42);
        }

        @Test
        @DisplayName("last function | last 函数")
        void testLast() {
            Function last = functions.get("last");
            assertThat(last.apply(List.of(1, 2, 3))).isEqualTo(3);
            assertThat(last.apply(List.of())).isNull();
            assertThat(last.apply((Object) new String[]{"a", "b"})).isEqualTo("b");
            assertThat(last.apply((Object) null)).isNull();
            assertThat(last.apply(42)).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Contains Tests | 包含测试")
    class ContainsTests {

        @Test
        @DisplayName("containskey function | containskey 函数")
        void testContainsKey() {
            Function containskey = functions.get("containskey");
            assertThat(containskey.apply(Map.of("a", 1), "a")).isEqualTo(true);
            assertThat(containskey.apply(Map.of("a", 1), "b")).isEqualTo(false);
            assertThat(containskey.apply(List.of(1), "a")).isEqualTo(false);
            assertThat(containskey.apply((Object) null, "a")).isEqualTo(false);
        }

        @Test
        @DisplayName("containsvalue function | containsvalue 函数")
        void testContainsValue() {
            Function containsvalue = functions.get("containsvalue");
            assertThat(containsvalue.apply(List.of(1, 2, 3), 2)).isEqualTo(true);
            assertThat(containsvalue.apply(List.of(1, 2, 3), 5)).isEqualTo(false);
            assertThat(containsvalue.apply(Map.of("a", 1), 1)).isEqualTo(true);
            assertThat(containsvalue.apply((Object) new Integer[]{1, 2, 3}, 2)).isEqualTo(true);
            assertThat(containsvalue.apply(42, 42)).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("Get Tests | 获取测试")
    class GetTests {

        @Test
        @DisplayName("get function with map | get 函数使用映射")
        void testGetWithMap() {
            Function get = functions.get("get");
            assertThat(get.apply(Map.of("a", 1), "a")).isEqualTo(1);
            assertThat(get.apply(Map.of("a", 1), "b")).isNull();
        }

        @Test
        @DisplayName("get function with list | get 函数使用列表")
        void testGetWithList() {
            Function get = functions.get("get");
            assertThat(get.apply(List.of("a", "b", "c"), 1)).isEqualTo("b");
            assertThat(get.apply(List.of("a"), 5)).isNull();
            assertThat(get.apply(List.of("a"), -1)).isNull();
        }

        @Test
        @DisplayName("get function with array | get 函数使用数组")
        void testGetWithArray() {
            Function get = functions.get("get");
            assertThat(get.apply((Object) new String[]{"a", "b"}, 0)).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("Sublist Tests | 子列表测试")
    class SublistTests {

        @Test
        @DisplayName("sublist function | sublist 函数")
        void testSublist() {
            Function sublist = functions.get("sublist");
            assertThat(sublist.apply(List.of(1, 2, 3, 4, 5), 1, 4)).isEqualTo(List.of(2, 3, 4));
            assertThat(sublist.apply(List.of(1, 2, 3), 1)).isEqualTo(List.of(2, 3));
            assertThat(sublist.apply((Object) new Integer[]{1, 2, 3}, 0, 2)).isEqualTo(List.of(1, 2));
            assertThat(sublist.apply((Object) null, 0)).isEqualTo(List.of());
        }
    }

    @Nested
    @DisplayName("Take/Skip Tests | 取/跳过测试")
    class TakeSkipTests {

        @Test
        @DisplayName("take function | take 函数")
        void testTake() {
            Function take = functions.get("take");
            assertThat(take.apply(List.of(1, 2, 3, 4, 5), 3)).isEqualTo(List.of(1, 2, 3));
            assertThat(take.apply(List.of(1, 2), 5)).isEqualTo(List.of(1, 2));
            assertThat(take.apply((Object) null, 3)).isEqualTo(List.of());
        }

        @Test
        @DisplayName("skip function | skip 函数")
        void testSkip() {
            Function skip = functions.get("skip");
            assertThat(skip.apply(List.of(1, 2, 3, 4, 5), 2)).isEqualTo(List.of(3, 4, 5));
            assertThat(skip.apply((Object) null, 2)).isEqualTo(List.of());
        }
    }

    @Nested
    @DisplayName("Distinct Tests | 去重测试")
    class DistinctTests {

        @Test
        @DisplayName("distinct function | distinct 函数")
        void testDistinct() {
            Function distinct = functions.get("distinct");
            assertThat(distinct.apply(List.of(1, 2, 2, 3, 3, 3))).isEqualTo(List.of(1, 2, 3));
            assertThat(distinct.apply((Object) null)).isEqualTo(List.of());
        }
    }

    @Nested
    @DisplayName("Sort Tests | 排序测试")
    class SortTests {

        @Test
        @DisplayName("sort function | sort 函数")
        void testSort() {
            Function sort = functions.get("sort");
            assertThat(sort.apply(List.of(3, 1, 2))).isEqualTo(List.of(1, 2, 3));
            assertThat(sort.apply(List.of("c", "a", "b"))).isEqualTo(List.of("a", "b", "c"));
            assertThat(sort.apply((Object) null)).isEqualTo(List.of());
        }
    }

    @Nested
    @DisplayName("Reverse Tests | 反转测试")
    class ReverseTests {

        @Test
        @DisplayName("reverselist function | reverselist 函数")
        void testReverseList() {
            Function reverselist = functions.get("reverselist");
            assertThat(reverselist.apply(List.of(1, 2, 3))).isEqualTo(List.of(3, 2, 1));
            assertThat(reverselist.apply((Object) null)).isEqualTo(List.of());
        }
    }

    @Nested
    @DisplayName("Flatten Tests | 扁平化测试")
    class FlattenTests {

        @Test
        @DisplayName("flatten function | flatten 函数")
        void testFlatten() {
            Function flatten = functions.get("flatten");
            assertThat(flatten.apply(List.of(1, List.of(2, 3), List.of(4, List.of(5)))))
                    .isEqualTo(List.of(1, 2, 3, 4, 5));
            assertThat(flatten.apply((Object) null)).isEqualTo(List.of());
        }
    }

    @Nested
    @DisplayName("Keys/Values/Entries Tests | 键/值/条目测试")
    class KeysValuesEntriesTests {

        @Test
        @DisplayName("keys function | keys 函数")
        void testKeys() {
            Function keys = functions.get("keys");
            Set<?> keysResult = new HashSet<>((List<?>) keys.apply(Map.of("a", 1, "b", 2)));
            assertThat(keysResult).isEqualTo(Set.of("a", "b"));
            assertThat(keys.apply(List.of())).isEqualTo(List.of());
            assertThat(keys.apply((Object) null)).isEqualTo(List.of());
        }

        @Test
        @DisplayName("values function | values 函数")
        void testValues() {
            Function values = functions.get("values");
            Set<?> valuesResult = new HashSet<>((List<?>) values.apply(Map.of("a", 1, "b", 2)));
            assertThat(valuesResult).isEqualTo(Set.of(1, 2));
            assertThat(values.apply((Object) null)).isEqualTo(List.of());
        }

        @Test
        @DisplayName("entries function | entries 函数")
        void testEntries() {
            Function entries = functions.get("entries");
            Object result = entries.apply(Map.of("a", 1));
            assertThat(result).isInstanceOf(List.class);
            assertThat(entries.apply((Object) null)).isEqualTo(List.of());
        }
    }

    @Nested
    @DisplayName("List Creation Tests | 列表创建测试")
    class ListCreationTests {

        @Test
        @DisplayName("list function | list 函数")
        void testList() {
            Function list = functions.get("list");
            assertThat(list.apply(1, 2, 3)).isEqualTo(Arrays.asList(1, 2, 3));
        }

        @Test
        @DisplayName("listof function | listof 函数")
        void testListOf() {
            Function listof = functions.get("listof");
            assertThat(listof.apply(1, 2, 3)).isEqualTo(List.of(1, 2, 3));
        }

        @Test
        @DisplayName("setof function | setof 函数")
        void testSetOf() {
            Function setof = functions.get("setof");
            assertThat(setof.apply(1, 2, 3)).isEqualTo(Set.of(1, 2, 3));
        }
    }

    @Nested
    @DisplayName("Range Tests | 范围测试")
    class RangeTests {

        @Test
        @DisplayName("range function with end | range 函数带结束")
        void testRangeWithEnd() {
            Function range = functions.get("range");
            assertThat(range.apply(5)).isEqualTo(List.of(0, 1, 2, 3, 4));
        }

        @Test
        @DisplayName("range function with start and end | range 函数带起始和结束")
        void testRangeWithStartEnd() {
            Function range = functions.get("range");
            assertThat(range.apply(2, 5)).isEqualTo(List.of(2, 3, 4));
        }

        @Test
        @DisplayName("range function with step | range 函数带步长")
        void testRangeWithStep() {
            Function range = functions.get("range");
            assertThat(range.apply(0, 10, 2)).isEqualTo(List.of(0, 2, 4, 6, 8));
            assertThat(range.apply(10, 0, -2)).isEqualTo(List.of(10, 8, 6, 4, 2));
        }

        @Test
        @DisplayName("range function with zero step | range 函数步长为零")
        void testRangeWithZeroStep() {
            Function range = functions.get("range");
            assertThat(range.apply(0, 3, 0)).isEqualTo(List.of(0, 1, 2));
        }

        @Test
        @DisplayName("range function with no args | range 函数无参数")
        void testRangeNoArgs() {
            Function range = functions.get("range");
            assertThat(range.apply()).isEqualTo(List.of());
        }
    }

    @Nested
    @DisplayName("Aggregate Tests | 聚合测试")
    class AggregateTests {

        @Test
        @DisplayName("sumlist function | sumlist 函数")
        void testSumList() {
            Function sumlist = functions.get("sumlist");
            assertThat(sumlist.apply(List.of(1, 2, 3, 4, 5))).isEqualTo(15.0);
            assertThat(sumlist.apply((Object) null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("avglist function | avglist 函数")
        void testAvgList() {
            Function avglist = functions.get("avglist");
            assertThat(avglist.apply(List.of(1, 2, 3, 4, 5))).isEqualTo(3.0);
            assertThat(avglist.apply(List.of())).isEqualTo(0.0);
            assertThat(avglist.apply((Object) null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("minlist function | minlist 函数")
        void testMinList() {
            Function minlist = functions.get("minlist");
            assertThat(minlist.apply(List.of(5, 2, 8, 1))).isEqualTo(1.0);
            assertThat(minlist.apply(List.of())).isNull();
            assertThat(minlist.apply((Object) null)).isNull();
        }

        @Test
        @DisplayName("maxlist function | maxlist 函数")
        void testMaxList() {
            Function maxlist = functions.get("maxlist");
            assertThat(maxlist.apply(List.of(5, 2, 8, 1))).isEqualTo(8.0);
            assertThat(maxlist.apply(List.of())).isNull();
            assertThat(maxlist.apply((Object) null)).isNull();
        }
    }

    @Nested
    @DisplayName("GetFunctions Tests | getFunctions 测试")
    class GetFunctionsTests {

        @Test
        @DisplayName("getFunctions returns all functions | getFunctions 返回所有函数")
        void testGetFunctionsReturnsAll() {
            Map<String, Function> funcs = CollectionFunctions.getFunctions();
            assertThat(funcs).isNotEmpty();
            assertThat(funcs).containsKey("size");
            assertThat(funcs).containsKey("first");
            assertThat(funcs).containsKey("last");
        }
    }
}
