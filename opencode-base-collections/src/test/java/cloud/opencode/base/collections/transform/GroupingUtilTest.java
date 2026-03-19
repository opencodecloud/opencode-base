package cloud.opencode.base.collections.transform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * GroupingUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("GroupingUtil 测试")
class GroupingUtilTest {

    // 测试用数据类
    record Person(String name, String city, int age) {}

    @Nested
    @DisplayName("基本分组测试")
    class BasicGroupingTests {

        @Test
        @DisplayName("groupBy - 按键分组")
        void testGroupBy() {
            List<Person> people = List.of(
                    new Person("Alice", "Beijing", 25),
                    new Person("Bob", "Shanghai", 30),
                    new Person("Charlie", "Beijing", 35)
            );

            Map<String, List<Person>> result = GroupingUtil.groupBy(people, Person::city);

            assertThat(result).hasSize(2);
            assertThat(result.get("Beijing")).hasSize(2);
            assertThat(result.get("Shanghai")).hasSize(1);
        }

        @Test
        @DisplayName("groupBy - 空集合")
        void testGroupByEmpty() {
            List<Person> people = List.of();

            Map<String, List<Person>> result = GroupingUtil.groupBy(people, Person::city);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("groupBy - 带值转换")
        void testGroupByWithValueFunction() {
            List<Person> people = List.of(
                    new Person("Alice", "Beijing", 25),
                    new Person("Bob", "Shanghai", 30),
                    new Person("Charlie", "Beijing", 35)
            );

            Map<String, List<String>> result = GroupingUtil.groupBy(people, Person::city, Person::name);

            assertThat(result.get("Beijing")).containsExactly("Alice", "Charlie");
            assertThat(result.get("Shanghai")).containsExactly("Bob");
        }

        @Test
        @DisplayName("groupByNested - 嵌套分组")
        void testGroupByNested() {
            List<Person> people = List.of(
                    new Person("Alice", "Beijing", 25),
                    new Person("Bob", "Beijing", 30),
                    new Person("Charlie", "Shanghai", 25),
                    new Person("David", "Beijing", 25)
            );

            Map<String, Map<Integer, List<Person>>> result =
                    GroupingUtil.groupByNested(people, Person::city, Person::age);

            assertThat(result).hasSize(2);
            assertThat(result.get("Beijing").get(25)).hasSize(2);
            assertThat(result.get("Beijing").get(30)).hasSize(1);
            assertThat(result.get("Shanghai").get(25)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("索引测试")
    class IndexingTests {

        @Test
        @DisplayName("indexBy - 按唯一键索引")
        void testIndexBy() {
            List<Person> people = List.of(
                    new Person("Alice", "Beijing", 25),
                    new Person("Bob", "Shanghai", 30),
                    new Person("Charlie", "Guangzhou", 35)
            );

            Map<String, Person> result = GroupingUtil.indexBy(people, Person::name);

            assertThat(result).hasSize(3);
            assertThat(result.get("Alice").city()).isEqualTo("Beijing");
        }

        @Test
        @DisplayName("indexBy - 重复键抛异常")
        void testIndexByDuplicateKey() {
            List<Person> people = List.of(
                    new Person("Alice", "Beijing", 25),
                    new Person("Alice", "Shanghai", 30)
            );

            assertThatThrownBy(() -> GroupingUtil.indexBy(people, Person::name))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Duplicate key");
        }

        @Test
        @DisplayName("indexByFirst - 保留第一个")
        void testIndexByFirst() {
            List<Person> people = List.of(
                    new Person("Alice", "Beijing", 25),
                    new Person("Alice", "Shanghai", 30)
            );

            Map<String, Person> result = GroupingUtil.indexByFirst(people, Person::name);

            assertThat(result).hasSize(1);
            assertThat(result.get("Alice").city()).isEqualTo("Beijing");
        }

        @Test
        @DisplayName("indexByLast - 保留最后一个")
        void testIndexByLast() {
            List<Person> people = List.of(
                    new Person("Alice", "Beijing", 25),
                    new Person("Alice", "Shanghai", 30)
            );

            Map<String, Person> result = GroupingUtil.indexByLast(people, Person::name);

            assertThat(result).hasSize(1);
            assertThat(result.get("Alice").city()).isEqualTo("Shanghai");
        }
    }

    @Nested
    @DisplayName("计数测试")
    class CountingTests {

        @Test
        @DisplayName("countBy - 按键计数")
        void testCountBy() {
            List<Person> people = List.of(
                    new Person("Alice", "Beijing", 25),
                    new Person("Bob", "Shanghai", 30),
                    new Person("Charlie", "Beijing", 35)
            );

            Map<String, Long> result = GroupingUtil.countBy(people, Person::city);

            assertThat(result.get("Beijing")).isEqualTo(2L);
            assertThat(result.get("Shanghai")).isEqualTo(1L);
        }

        @Test
        @DisplayName("count - 按谓词计数")
        void testCount() {
            List<Person> people = List.of(
                    new Person("Alice", "Beijing", 25),
                    new Person("Bob", "Shanghai", 30),
                    new Person("Charlie", "Beijing", 35)
            );

            long count = GroupingUtil.count(people, p -> p.age() >= 30);

            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("count - 无匹配")
        void testCountNoMatch() {
            List<Person> people = List.of(
                    new Person("Alice", "Beijing", 25),
                    new Person("Bob", "Shanghai", 30)
            );

            long count = GroupingUtil.count(people, p -> p.age() > 100);

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("频率分析测试")
    class FrequencyTests {

        @Test
        @DisplayName("frequency - 频率映射")
        void testFrequency() {
            List<String> items = List.of("a", "b", "a", "c", "a", "b");

            Map<String, Long> result = GroupingUtil.frequency(items);

            assertThat(result.get("a")).isEqualTo(3L);
            assertThat(result.get("b")).isEqualTo(2L);
            assertThat(result.get("c")).isEqualTo(1L);
        }

        @Test
        @DisplayName("mostFrequent - 最频繁元素")
        void testMostFrequent() {
            List<String> items = List.of("a", "b", "a", "c", "a", "b");

            String result = GroupingUtil.mostFrequent(items);

            assertThat(result).isEqualTo("a");
        }

        @Test
        @DisplayName("mostFrequent - 空集合")
        void testMostFrequentEmpty() {
            List<String> items = List.of();

            String result = GroupingUtil.mostFrequent(items);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("leastFrequent - 最不频繁元素")
        void testLeastFrequent() {
            List<String> items = List.of("a", "b", "a", "c", "a", "b");

            String result = GroupingUtil.leastFrequent(items);

            assertThat(result).isEqualTo("c");
        }

        @Test
        @DisplayName("leastFrequent - 空集合")
        void testLeastFrequentEmpty() {
            List<String> items = List.of();

            String result = GroupingUtil.leastFrequent(items);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("集合分组测试")
    class SetGroupingTests {

        @Test
        @DisplayName("groupByToSet - 按键分组为集合")
        void testGroupByToSet() {
            List<String> items = List.of("apple", "apricot", "banana", "avocado", "apple");

            Map<Character, Set<String>> result = GroupingUtil.groupByToSet(items, s -> s.charAt(0));

            assertThat(result.get('a')).containsExactlyInAnyOrder("apple", "apricot", "avocado");
            assertThat(result.get('b')).containsExactly("banana");
        }

        @Test
        @DisplayName("groupByToSet - 去重效果")
        void testGroupByToSetDedup() {
            List<String> items = List.of("a", "a", "a", "b", "b");

            Map<String, Set<String>> result = GroupingUtil.groupByToSet(items, s -> s);

            assertThat(result.get("a")).hasSize(1);
            assertThat(result.get("b")).hasSize(1);
        }
    }

    @Nested
    @DisplayName("排序分组测试")
    class SortedGroupingTests {

        @Test
        @DisplayName("groupBySorted - 按键排序分组")
        void testGroupBySorted() {
            List<Person> people = List.of(
                    new Person("Alice", "C", 25),
                    new Person("Bob", "A", 30),
                    new Person("Charlie", "B", 35)
            );

            Map<String, List<Person>> result = GroupingUtil.groupBySorted(people, Person::city);

            List<String> keys = new ArrayList<>(result.keySet());
            assertThat(keys).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("groupBySorted - 自定义比较器")
        void testGroupBySortedWithComparator() {
            List<Person> people = List.of(
                    new Person("Alice", "A", 25),
                    new Person("Bob", "C", 30),
                    new Person("Charlie", "B", 35)
            );

            Map<String, List<Person>> result = GroupingUtil.groupBySorted(people, Person::city, Comparator.reverseOrder());

            List<String> keys = new ArrayList<>(result.keySet());
            assertThat(keys).containsExactly("C", "B", "A");
        }
    }

    @Nested
    @DisplayName("整数分组测试")
    class IntegerGroupingTests {

        @Test
        @DisplayName("按整数范围分组")
        void testGroupByIntegerRange() {
            List<Integer> numbers = List.of(1, 5, 12, 23, 35, 47, 56, 68);

            Map<Integer, List<Integer>> result = GroupingUtil.groupBy(numbers, n -> n / 10);

            assertThat(result.get(0)).containsExactly(1, 5);
            assertThat(result.get(1)).containsExactly(12);
            assertThat(result.get(2)).containsExactly(23);
            assertThat(result.get(3)).containsExactly(35);
            assertThat(result.get(4)).containsExactly(47);
            assertThat(result.get(5)).containsExactly(56);
            assertThat(result.get(6)).containsExactly(68);
        }

        @Test
        @DisplayName("按奇偶分组")
        void testGroupByOddEven() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            Map<String, List<Integer>> result = GroupingUtil.groupBy(numbers, n -> n % 2 == 0 ? "even" : "odd");

            assertThat(result.get("odd")).containsExactly(1, 3, 5, 7, 9);
            assertThat(result.get("even")).containsExactly(2, 4, 6, 8, 10);
        }
    }
}
