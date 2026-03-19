package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * HashMultiset 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("HashMultiset 测试")
class HashMultisetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 空创建")
        void testCreateEmpty() {
            HashMultiset<String> multiset = HashMultiset.create();
            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("create - 指定容量")
        void testCreateWithCapacity() {
            HashMultiset<String> multiset = HashMultiset.create(32);
            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("create - 负容量抛异常")
        void testCreateNegativeCapacity() {
            assertThatThrownBy(() -> HashMultiset.create(-1))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("create - 从 Iterable 创建")
        void testCreateFromIterable() {
            List<String> source = List.of("a", "a", "b");
            HashMultiset<String> multiset = HashMultiset.create(source);

            assertThat(multiset.count("a")).isEqualTo(2);
            assertThat(multiset.count("b")).isEqualTo(1);
        }

        @Test
        @DisplayName("create - 从 null Iterable 创建")
        void testCreateFromNullIterable() {
            HashMultiset<String> multiset = HashMultiset.create((Iterable<String>) null);
            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("create - 从可变参数创建")
        void testCreateFromVarargs() {
            HashMultiset<String> multiset = HashMultiset.create("a", "a", "b");

            assertThat(multiset.count("a")).isEqualTo(2);
            assertThat(multiset.count("b")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("count 操作测试")
    class CountOperationTests {

        @Test
        @DisplayName("count - 获取计数")
        void testCount() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("count - 不存在的元素返回 0")
        void testCountNonExistent() {
            HashMultiset<String> multiset = HashMultiset.create();

            assertThat(multiset.count("x")).isEqualTo(0);
        }

        @Test
        @DisplayName("count - null 元素")
        void testCountNull() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add(null, 2);

            assertThat(multiset.count(null)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("add 操作测试")
    class AddOperationTests {

        @Test
        @DisplayName("add - 添加单个")
        void testAddSingle() {
            HashMultiset<String> multiset = HashMultiset.create();

            boolean result = multiset.add("a");

            assertThat(result).isTrue();
            assertThat(multiset.count("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("add - 添加多个")
        void testAddMultiple() {
            HashMultiset<String> multiset = HashMultiset.create();

            int oldCount = multiset.add("a", 5);

            assertThat(oldCount).isEqualTo(0);
            assertThat(multiset.count("a")).isEqualTo(5);
        }

        @Test
        @DisplayName("add - 添加到已有元素")
        void testAddToExisting() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            int oldCount = multiset.add("a", 2);

            assertThat(oldCount).isEqualTo(3);
            assertThat(multiset.count("a")).isEqualTo(5);
        }

        @Test
        @DisplayName("add - 添加 0 个")
        void testAddZero() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            int oldCount = multiset.add("a", 0);

            assertThat(oldCount).isEqualTo(3);
            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("add - 负数抛异常")
        void testAddNegative() {
            HashMultiset<String> multiset = HashMultiset.create();

            assertThatThrownBy(() -> multiset.add("a", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("add - 溢出抛异常")
        void testAddOverflow() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", Integer.MAX_VALUE);

            assertThatThrownBy(() -> multiset.add("a", 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("overflow");
        }
    }

    @Nested
    @DisplayName("remove 操作测试")
    class RemoveOperationTests {

        @Test
        @DisplayName("remove - 移除单个")
        void testRemoveSingle() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            boolean result = multiset.remove("a");

            assertThat(result).isTrue();
            assertThat(multiset.count("a")).isEqualTo(2);
        }

        @Test
        @DisplayName("remove - 移除不存在的元素")
        void testRemoveNonExistent() {
            HashMultiset<String> multiset = HashMultiset.create();

            boolean result = multiset.remove("x");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("remove - 移除多个")
        void testRemoveMultiple() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 5);

            int oldCount = multiset.remove("a", 3);

            assertThat(oldCount).isEqualTo(5);
            assertThat(multiset.count("a")).isEqualTo(2);
        }

        @Test
        @DisplayName("remove - 移除全部")
        void testRemoveAll() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            int oldCount = multiset.remove("a", 5);

            assertThat(oldCount).isEqualTo(3);
            assertThat(multiset.count("a")).isEqualTo(0);
            assertThat(multiset.contains("a")).isFalse();
        }

        @Test
        @DisplayName("remove - 移除 0 个")
        void testRemoveZero() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            int oldCount = multiset.remove("a", 0);

            assertThat(oldCount).isEqualTo(3);
            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("remove - 负数抛异常")
        void testRemoveNegative() {
            HashMultiset<String> multiset = HashMultiset.create();

            assertThatThrownBy(() -> multiset.remove("a", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("remove - 移除不存在的元素多个")
        void testRemoveNonExistentMultiple() {
            HashMultiset<String> multiset = HashMultiset.create();

            int oldCount = multiset.remove("x", 5);

            assertThat(oldCount).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("setCount 操作测试")
    class SetCountOperationTests {

        @Test
        @DisplayName("setCount - 设置计数")
        void testSetCount() {
            HashMultiset<String> multiset = HashMultiset.create();

            int oldCount = multiset.setCount("a", 5);

            assertThat(oldCount).isEqualTo(0);
            assertThat(multiset.count("a")).isEqualTo(5);
        }

        @Test
        @DisplayName("setCount - 更新计数")
        void testSetCountUpdate() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            int oldCount = multiset.setCount("a", 7);

            assertThat(oldCount).isEqualTo(3);
            assertThat(multiset.count("a")).isEqualTo(7);
        }

        @Test
        @DisplayName("setCount - 设为 0 移除元素")
        void testSetCountZero() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            int oldCount = multiset.setCount("a", 0);

            assertThat(oldCount).isEqualTo(3);
            assertThat(multiset.contains("a")).isFalse();
        }

        @Test
        @DisplayName("setCount - 负数抛异常")
        void testSetCountNegative() {
            HashMultiset<String> multiset = HashMultiset.create();

            assertThatThrownBy(() -> multiset.setCount("a", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("setCount - 条件设置成功")
        void testSetCountConditionalSuccess() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            boolean result = multiset.setCount("a", 3, 5);

            assertThat(result).isTrue();
            assertThat(multiset.count("a")).isEqualTo(5);
        }

        @Test
        @DisplayName("setCount - 条件设置失败")
        void testSetCountConditionalFailure() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            boolean result = multiset.setCount("a", 2, 5);

            assertThat(result).isFalse();
            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("setCount - 条件负数抛异常")
        void testSetCountConditionalNegative() {
            HashMultiset<String> multiset = HashMultiset.create();

            assertThatThrownBy(() -> multiset.setCount("a", -1, 5))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> multiset.setCount("a", 0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("elementSet 测试")
    class ElementSetTests {

        @Test
        @DisplayName("elementSet - 获取元素集")
        void testElementSet() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            Set<String> elements = multiset.elementSet();

            assertThat(elements).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("elementSet - size")
        void testElementSetSize() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            assertThat(multiset.elementSet().size()).isEqualTo(2);
        }

        @Test
        @DisplayName("elementSet - contains")
        void testElementSetContains() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            assertThat(multiset.elementSet().contains("a")).isTrue();
            assertThat(multiset.elementSet().contains("b")).isFalse();
        }

        @Test
        @DisplayName("elementSet - remove")
        void testElementSetRemove() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            boolean removed = multiset.elementSet().remove("a");

            assertThat(removed).isTrue();
            assertThat(multiset.count("a")).isEqualTo(0);
            assertThat(multiset.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("elementSet - remove 不存在")
        void testElementSetRemoveNonExistent() {
            HashMultiset<String> multiset = HashMultiset.create();

            boolean removed = multiset.elementSet().remove("a");

            assertThat(removed).isFalse();
        }

        @Test
        @DisplayName("elementSet - clear")
        void testElementSetClear() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            multiset.elementSet().clear();

            assertThat(multiset).isEmpty();
        }
    }

    @Nested
    @DisplayName("entrySet 测试")
    class EntrySetTests {

        @Test
        @DisplayName("entrySet - 获取条目集")
        void testEntrySet() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            Set<Multiset.Entry<String>> entries = multiset.entrySet();

            assertThat(entries).hasSize(2);
        }

        @Test
        @DisplayName("entrySet - 遍历")
        void testEntrySetIteration() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            for (Multiset.Entry<String> entry : multiset.entrySet()) {
                assertThat(entry.getElement()).isEqualTo("a");
                assertThat(entry.getCount()).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("entrySet - contains")
        void testEntrySetContains() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            // Entry with correct count
            Multiset.Entry<String> matchingEntry = new Multiset.Entry<>() {
                @Override
                public String getElement() {
                    return "a";
                }

                @Override
                public int getCount() {
                    return 3;
                }
            };

            Multiset.Entry<String> wrongCountEntry = new Multiset.Entry<>() {
                @Override
                public String getElement() {
                    return "a";
                }

                @Override
                public int getCount() {
                    return 2;
                }
            };

            assertThat(multiset.entrySet().contains(matchingEntry)).isTrue();
            assertThat(multiset.entrySet().contains(wrongCountEntry)).isFalse();
            assertThat(multiset.entrySet().contains("not an entry")).isFalse();
        }

        @Test
        @DisplayName("entrySet - clear")
        void testEntrySetClear() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            multiset.entrySet().clear();

            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("Entry - equals/hashCode/toString")
        void testEntryMethods() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            for (Multiset.Entry<String> entry : multiset.entrySet()) {
                Multiset.Entry<String> same = new Multiset.Entry<>() {
                    @Override
                    public String getElement() {
                        return "a";
                    }

                    @Override
                    public int getCount() {
                        return 3;
                    }
                };

                assertThat(entry.equals(same)).isTrue();
                assertThat(entry.equals("not an entry")).isFalse();
                assertThat(entry.hashCode()).isEqualTo("a".hashCode() ^ 3);
                assertThat(entry.toString()).isEqualTo("a x 3");
            }
        }
    }

    @Nested
    @DisplayName("Collection 操作测试")
    class CollectionOperationTests {

        @Test
        @DisplayName("size - 总计数")
        void testSize() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            assertThat(multiset.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("isEmpty")
        void testIsEmpty() {
            HashMultiset<String> multiset = HashMultiset.create();

            assertThat(multiset.isEmpty()).isTrue();

            multiset.add("a");

            assertThat(multiset.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains")
        void testContains() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a");

            assertThat(multiset.contains("a")).isTrue();
            assertThat(multiset.contains("b")).isFalse();
        }

        @Test
        @DisplayName("clear")
        void testClear() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            multiset.clear();

            assertThat(multiset).isEmpty();
            assertThat(multiset.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("iterator - 遍历所有出现")
        void testIterator() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            List<String> elements = new ArrayList<>();
            for (String e : multiset) {
                elements.add(e);
            }

            assertThat(elements).hasSize(3);
            assertThat(elements).containsOnly("a");
        }

        @Test
        @DisplayName("iterator - remove")
        void testIteratorRemove() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            Iterator<String> it = multiset.iterator();
            it.next();
            it.remove();

            assertThat(multiset.count("a")).isEqualTo(2);
            assertThat(multiset.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("iterator - remove 直到为 0")
        void testIteratorRemoveUntilZero() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 2);
            multiset.add("b", 1);

            Iterator<String> it = multiset.iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }

            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("iterator - remove without next throws")
        void testIteratorRemoveWithoutNext() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a");

            Iterator<String> it = multiset.iterator();

            assertThatThrownBy(it::remove).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("iterator - double remove throws")
        void testIteratorDoubleRemove() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 2);

            Iterator<String> it = multiset.iterator();
            it.next();
            it.remove();

            assertThatThrownBy(it::remove).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("iterator - next without hasNext throws")
        void testIteratorNextWithoutHasNext() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 1);

            Iterator<String> it = multiset.iterator();
            it.next();

            assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等的 Multiset")
        void testEquals() {
            HashMultiset<String> multiset1 = HashMultiset.create();
            multiset1.add("a", 3);
            multiset1.add("b", 2);

            HashMultiset<String> multiset2 = HashMultiset.create();
            multiset2.add("a", 3);
            multiset2.add("b", 2);

            assertThat(multiset1.equals(multiset1)).isTrue();
            assertThat(multiset1.equals(multiset2)).isTrue();
        }

        @Test
        @DisplayName("equals - 不相等的 Multiset")
        void testNotEquals() {
            HashMultiset<String> multiset1 = HashMultiset.create();
            multiset1.add("a", 3);

            HashMultiset<String> multiset2 = HashMultiset.create();
            multiset2.add("a", 2);

            HashMultiset<String> multiset3 = HashMultiset.create();
            multiset3.add("b", 3);

            assertThat(multiset1.equals(multiset2)).isFalse();
            assertThat(multiset1.equals(multiset3)).isFalse();
            assertThat(multiset1.equals("not a multiset")).isFalse();
        }

        @Test
        @DisplayName("equals - 大小不同")
        void testEqualsDifferentSize() {
            HashMultiset<String> multiset1 = HashMultiset.create();
            multiset1.add("a", 3);

            HashMultiset<String> multiset2 = HashMultiset.create();
            multiset2.add("a", 2);
            multiset2.add("b", 1);

            assertThat(multiset1.equals(multiset2)).isFalse();
        }

        @Test
        @DisplayName("hashCode")
        void testHashCode() {
            HashMultiset<String> multiset1 = HashMultiset.create();
            multiset1.add("a", 3);

            HashMultiset<String> multiset2 = HashMultiset.create();
            multiset2.add("a", 3);

            assertThat(multiset1.hashCode()).isEqualTo(multiset2.hashCode());
        }

        @Test
        @DisplayName("hashCode - null 元素")
        void testHashCodeWithNull() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add(null, 2);

            // Should not throw
            assertThat(multiset.hashCode()).isNotNull();
        }

        @Test
        @DisplayName("toString")
        void testToString() {
            HashMultiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);

            String str = multiset.toString();

            assertThat(str).contains("a x 3");
            assertThat(str).startsWith("[");
            assertThat(str).endsWith("]");
        }

        @Test
        @DisplayName("toString - 空")
        void testToStringEmpty() {
            HashMultiset<String> multiset = HashMultiset.create();

            assertThat(multiset.toString()).isEqualTo("[]");
        }
    }
}
