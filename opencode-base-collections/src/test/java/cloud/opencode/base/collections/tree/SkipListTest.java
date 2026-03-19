package cloud.opencode.base.collections.tree;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SkipList 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("SkipList 测试")
class SkipListTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 SkipList")
        void testCreate() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThat(skipList).isNotNull();
            assertThat(skipList.isEmpty()).isTrue();
            assertThat(skipList.size()).isZero();
        }

        @Test
        @DisplayName("create - 自定义比较器")
        void testCreateWithComparator() {
            SkipList<Integer, String> skipList = SkipList.create(Comparator.reverseOrder());

            skipList.put(1, "one");
            skipList.put(3, "three");
            skipList.put(2, "two");

            assertThat(skipList.firstKey()).isEqualTo(3);
            assertThat(skipList.lastKey()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("put 操作测试")
    class PutOperationTests {

        @Test
        @DisplayName("put - 添加新键")
        void testPut() {
            SkipList<Integer, String> skipList = SkipList.create();

            String old = skipList.put(1, "one");

            assertThat(old).isNull();
            assertThat(skipList.size()).isEqualTo(1);
            assertThat(skipList.get(1)).isEqualTo("one");
        }

        @Test
        @DisplayName("put - 替换现有键")
        void testPutReplace() {
            SkipList<Integer, String> skipList = SkipList.create();
            skipList.put(1, "one");

            String old = skipList.put(1, "ONE");

            assertThat(old).isEqualTo("one");
            assertThat(skipList.size()).isEqualTo(1);
            assertThat(skipList.get(1)).isEqualTo("ONE");
        }

        @Test
        @DisplayName("put - null 键抛异常")
        void testPutNullKey() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThatThrownBy(() -> skipList.put(null, "value"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - 多个键")
        void testPutMultiple() {
            SkipList<Integer, String> skipList = SkipList.create();

            skipList.put(3, "three");
            skipList.put(1, "one");
            skipList.put(2, "two");

            assertThat(skipList.size()).isEqualTo(3);
            assertThat(skipList.get(1)).isEqualTo("one");
            assertThat(skipList.get(2)).isEqualTo("two");
            assertThat(skipList.get(3)).isEqualTo("three");
        }
    }

    @Nested
    @DisplayName("get 操作测试")
    class GetOperationTests {

        @Test
        @DisplayName("get - 存在的键")
        void testGet() {
            SkipList<Integer, String> skipList = SkipList.create();
            skipList.put(1, "one");

            assertThat(skipList.get(1)).isEqualTo("one");
        }

        @Test
        @DisplayName("get - 不存在的键")
        void testGetNonExistent() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThat(skipList.get(1)).isNull();
        }

        @Test
        @DisplayName("get - null 键抛异常")
        void testGetNullKey() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThatThrownBy(() -> skipList.get(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("containsKey 测试")
    class ContainsKeyTests {

        @Test
        @DisplayName("containsKey - 存在的键")
        void testContainsKey() {
            SkipList<Integer, String> skipList = SkipList.create();
            skipList.put(1, "one");

            assertThat(skipList.containsKey(1)).isTrue();
        }

        @Test
        @DisplayName("containsKey - 不存在的键")
        void testContainsKeyNonExistent() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThat(skipList.containsKey(1)).isFalse();
        }

        @Test
        @DisplayName("containsKey - null 键抛异常")
        void testContainsKeyNull() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThatThrownBy(() -> skipList.containsKey(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("remove 操作测试")
    class RemoveOperationTests {

        @Test
        @DisplayName("remove - 移除存在的键")
        void testRemove() {
            SkipList<Integer, String> skipList = SkipList.create();
            skipList.put(1, "one");

            String old = skipList.remove(1);

            assertThat(old).isEqualTo("one");
            assertThat(skipList.containsKey(1)).isFalse();
            assertThat(skipList.size()).isZero();
        }

        @Test
        @DisplayName("remove - 移除不存在的键")
        void testRemoveNonExistent() {
            SkipList<Integer, String> skipList = SkipList.create();

            String old = skipList.remove(1);

            assertThat(old).isNull();
        }

        @Test
        @DisplayName("remove - null 键抛异常")
        void testRemoveNull() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThatThrownBy(() -> skipList.remove(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            SkipList<Integer, String> skipList = SkipList.create();
            skipList.put(1, "one");
            skipList.put(2, "two");

            skipList.clear();

            assertThat(skipList.isEmpty()).isTrue();
            assertThat(skipList.size()).isZero();
        }
    }

    @Nested
    @DisplayName("firstKey/lastKey 测试")
    class FirstLastKeyTests {

        @Test
        @DisplayName("firstKey - 获取最小键")
        void testFirstKey() {
            SkipList<Integer, String> skipList = SkipList.create();
            skipList.put(3, "three");
            skipList.put(1, "one");
            skipList.put(2, "two");

            assertThat(skipList.firstKey()).isEqualTo(1);
        }

        @Test
        @DisplayName("firstKey - 空 SkipList 抛异常")
        void testFirstKeyEmpty() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThatThrownBy(skipList::firstKey)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("lastKey - 获取最大键")
        void testLastKey() {
            SkipList<Integer, String> skipList = SkipList.create();
            skipList.put(3, "three");
            skipList.put(1, "one");
            skipList.put(2, "two");

            assertThat(skipList.lastKey()).isEqualTo(3);
        }

        @Test
        @DisplayName("lastKey - 空 SkipList 抛异常")
        void testLastKeyEmpty() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThatThrownBy(skipList::lastKey)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("iterator - 按排序顺序迭代")
        void testIterator() {
            SkipList<Integer, String> skipList = SkipList.create();
            skipList.put(3, "three");
            skipList.put(1, "one");
            skipList.put(2, "two");

            List<Integer> keys = new ArrayList<>();
            for (SkipList.Entry<Integer, String> entry : skipList) {
                keys.add(entry.getKey());
            }

            assertThat(keys).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("iterator - 空 SkipList")
        void testIteratorEmpty() {
            SkipList<Integer, String> skipList = SkipList.create();

            List<Integer> keys = new ArrayList<>();
            for (SkipList.Entry<Integer, String> entry : skipList) {
                keys.add(entry.getKey());
            }

            assertThat(keys).isEmpty();
        }

        @Test
        @DisplayName("iterator - 越界")
        void testIteratorOutOfBounds() {
            SkipList<Integer, String> skipList = SkipList.create();
            skipList.put(1, "one");

            Iterator<SkipList.Entry<Integer, String>> iterator = skipList.iterator();
            iterator.next();

            assertThatThrownBy(iterator::next)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("size/isEmpty 测试")
    class SizeTests {

        @Test
        @DisplayName("size - 统计键数量")
        void testSize() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThat(skipList.size()).isZero();

            skipList.put(1, "one");
            assertThat(skipList.size()).isEqualTo(1);

            skipList.put(2, "two");
            assertThat(skipList.size()).isEqualTo(2);

            skipList.put(1, "ONE");  // replace
            assertThat(skipList.size()).isEqualTo(2);  // size unchanged
        }

        @Test
        @DisplayName("isEmpty - 空检查")
        void testIsEmpty() {
            SkipList<Integer, String> skipList = SkipList.create();

            assertThat(skipList.isEmpty()).isTrue();

            skipList.put(1, "one");
            assertThat(skipList.isEmpty()).isFalse();

            skipList.remove(1);
            assertThat(skipList.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("字符串类型测试")
    class StringKeyTests {

        @Test
        @DisplayName("字符串键排序")
        void testStringKey() {
            SkipList<String, Integer> skipList = SkipList.create();

            skipList.put("charlie", 3);
            skipList.put("alpha", 1);
            skipList.put("bravo", 2);

            assertThat(skipList.firstKey()).isEqualTo("alpha");
            assertThat(skipList.lastKey()).isEqualTo("charlie");
        }
    }

    @Nested
    @DisplayName("Entry 测试")
    class EntryTests {

        @Test
        @DisplayName("Entry - getKey/getValue")
        void testEntry() {
            SkipList<Integer, String> skipList = SkipList.create();
            skipList.put(1, "one");

            for (SkipList.Entry<Integer, String> entry : skipList) {
                assertThat(entry.getKey()).isEqualTo(1);
                assertThat(entry.getValue()).isEqualTo("one");
            }
        }
    }

    @Nested
    @DisplayName("复杂场景测试")
    class ComplexScenarioTests {

        @Test
        @DisplayName("大量数据测试")
        void testLargeData() {
            SkipList<Integer, String> skipList = SkipList.create();

            for (int i = 0; i < 1000; i++) {
                skipList.put(i, "value" + i);
            }

            assertThat(skipList.size()).isEqualTo(1000);

            for (int i = 0; i < 1000; i++) {
                assertThat(skipList.get(i)).isEqualTo("value" + i);
            }

            assertThat(skipList.firstKey()).isZero();
            assertThat(skipList.lastKey()).isEqualTo(999);
        }

        @Test
        @DisplayName("随机操作测试")
        void testRandomOperations() {
            SkipList<Integer, String> skipList = SkipList.create();
            Map<Integer, String> map = new TreeMap<>();
            Random random = new Random(42);

            for (int i = 0; i < 100; i++) {
                int key = random.nextInt(50);
                String value = "value" + i;
                skipList.put(key, value);
                map.put(key, value);
            }

            for (Integer key : map.keySet()) {
                assertThat(skipList.get(key)).isEqualTo(map.get(key));
            }

            assertThat(skipList.size()).isEqualTo(map.size());
        }
    }
}
