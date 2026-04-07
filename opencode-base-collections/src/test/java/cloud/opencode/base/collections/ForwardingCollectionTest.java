package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ForwardingCollection / ForwardingList / ForwardingSet / ForwardingMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("Forwarding 装饰器测试")
class ForwardingCollectionTest {

    // ==================== 辅助类 | Helper Classes ====================

    private static class TestForwardingCollection<E> extends ForwardingCollection<E> {
        private final Collection<E> delegate;

        TestForwardingCollection(Collection<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Collection<E> delegate() {
            return delegate;
        }
    }

    private static class TestForwardingList<E> extends ForwardingList<E> {
        private final List<E> delegate;

        TestForwardingList(List<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        protected List<E> delegate() {
            return delegate;
        }
    }

    private static class TestForwardingSet<E> extends ForwardingSet<E> {
        private final Set<E> delegate;

        TestForwardingSet(Set<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Set<E> delegate() {
            return delegate;
        }
    }

    private static class TestForwardingMap<K, V> extends ForwardingMap<K, V> {
        private final Map<K, V> delegate;

        TestForwardingMap(Map<K, V> delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Map<K, V> delegate() {
            return delegate;
        }
    }

    // ==================== ForwardingCollection 测试 ====================

    @Nested
    @DisplayName("ForwardingCollection 测试")
    class ForwardingCollectionTests {

        @Test
        @DisplayName("size - 委托 size 方法")
        void testSize() {
            ArrayList<String> backing = new ArrayList<>(List.of("a", "b", "c"));
            var fc = new TestForwardingCollection<>(backing);

            assertThat(fc.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 委托 isEmpty 方法")
        void testIsEmpty() {
            var fc = new TestForwardingCollection<>(new ArrayList<>());

            assertThat(fc.isEmpty()).isTrue();

            fc.add("x");

            assertThat(fc.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains - 委托 contains 方法")
        void testContains() {
            var fc = new TestForwardingCollection<>(new ArrayList<>(List.of("a", "b")));

            assertThat(fc.contains("a")).isTrue();
            assertThat(fc.contains("z")).isFalse();
        }

        @Test
        @DisplayName("add / remove - 委托修改方法")
        void testAddRemove() {
            ArrayList<String> backing = new ArrayList<>();
            var fc = new TestForwardingCollection<>(backing);

            fc.add("hello");

            assertThat(backing).containsExactly("hello");

            fc.remove("hello");

            assertThat(backing).isEmpty();
        }

        @Test
        @DisplayName("iterator - 委托 iterator 方法")
        void testIterator() {
            var fc = new TestForwardingCollection<>(new ArrayList<>(List.of("a", "b")));

            List<String> result = new ArrayList<>();
            for (String s : fc) {
                result.add(s);
            }

            assertThat(result).containsExactly("a", "b");
        }

        @Test
        @DisplayName("toArray - 委托 toArray 方法")
        void testToArray() {
            var fc = new TestForwardingCollection<>(new ArrayList<>(List.of("a", "b")));

            assertThat(fc.toArray()).containsExactly("a", "b");

            String[] arr = fc.toArray(new String[0]);

            assertThat(arr).containsExactly("a", "b");
        }

        @Test
        @DisplayName("containsAll - 委托 containsAll 方法")
        void testContainsAll() {
            var fc = new TestForwardingCollection<>(new ArrayList<>(List.of("a", "b", "c")));

            assertThat(fc.containsAll(List.of("a", "b"))).isTrue();
            assertThat(fc.containsAll(List.of("a", "z"))).isFalse();
        }

        @Test
        @DisplayName("addAll - 委托 addAll 方法")
        void testAddAll() {
            ArrayList<String> backing = new ArrayList<>();
            var fc = new TestForwardingCollection<>(backing);

            fc.addAll(List.of("a", "b"));

            assertThat(backing).containsExactly("a", "b");
        }

        @Test
        @DisplayName("removeAll - 委托 removeAll 方法")
        void testRemoveAll() {
            ArrayList<String> backing = new ArrayList<>(List.of("a", "b", "c"));
            var fc = new TestForwardingCollection<>(backing);

            fc.removeAll(List.of("a", "c"));

            assertThat(backing).containsExactly("b");
        }

        @Test
        @DisplayName("retainAll - 委托 retainAll 方法")
        void testRetainAll() {
            ArrayList<String> backing = new ArrayList<>(List.of("a", "b", "c"));
            var fc = new TestForwardingCollection<>(backing);

            fc.retainAll(List.of("b"));

            assertThat(backing).containsExactly("b");
        }

        @Test
        @DisplayName("clear - 委托 clear 方法")
        void testClear() {
            ArrayList<String> backing = new ArrayList<>(List.of("a", "b"));
            var fc = new TestForwardingCollection<>(backing);

            fc.clear();

            assertThat(backing).isEmpty();
        }

        @Test
        @DisplayName("toString - 委托 toString 方法")
        void testToString() {
            ArrayList<String> backing = new ArrayList<>(List.of("a"));
            var fc = new TestForwardingCollection<>(backing);

            assertThat(fc.toString()).isEqualTo(backing.toString());
        }

        @Test
        @DisplayName("equals - 自身比较返回 true")
        void testEqualsSelf() {
            var fc = new TestForwardingCollection<>(new ArrayList<>(List.of("a")));

            assertThat(fc.equals(fc)).isTrue();
        }

        @Test
        @DisplayName("equals - 与委托相等的对象返回 true")
        void testEqualsDelegate() {
            ArrayList<String> backing = new ArrayList<>(List.of("a", "b"));
            var fc = new TestForwardingCollection<>(backing);

            assertThat(fc.equals(backing)).isTrue();
        }

        @Test
        @DisplayName("hashCode - 委托 hashCode 方法")
        void testHashCode() {
            ArrayList<String> backing = new ArrayList<>(List.of("a", "b"));
            var fc = new TestForwardingCollection<>(backing);

            assertThat(fc.hashCode()).isEqualTo(backing.hashCode());
        }
    }

    // ==================== ForwardingList 测试 ====================

    @Nested
    @DisplayName("ForwardingList 测试")
    class ForwardingListTests {

        @Test
        @DisplayName("get - 委托索引访问")
        void testGet() {
            var fl = new TestForwardingList<>(new ArrayList<>(List.of("a", "b", "c")));

            assertThat(fl.get(0)).isEqualTo("a");
            assertThat(fl.get(2)).isEqualTo("c");
        }

        @Test
        @DisplayName("set - 委托索引设置")
        void testSet() {
            ArrayList<String> backing = new ArrayList<>(List.of("a", "b"));
            var fl = new TestForwardingList<>(backing);

            String old = fl.set(0, "x");

            assertThat(old).isEqualTo("a");
            assertThat(backing.get(0)).isEqualTo("x");
        }

        @Test
        @DisplayName("add(index) - 委托索引添加")
        void testAddAtIndex() {
            ArrayList<String> backing = new ArrayList<>(List.of("a", "c"));
            var fl = new TestForwardingList<>(backing);

            fl.add(1, "b");

            assertThat(backing).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("addAll(index) - 委托索引批量添加")
        void testAddAllAtIndex() {
            ArrayList<String> backing = new ArrayList<>(List.of("a", "d"));
            var fl = new TestForwardingList<>(backing);

            fl.addAll(1, List.of("b", "c"));

            assertThat(backing).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("remove(index) - 委托索引删除")
        void testRemoveAtIndex() {
            ArrayList<String> backing = new ArrayList<>(List.of("a", "b", "c"));
            var fl = new TestForwardingList<>(backing);

            String removed = fl.remove(1);

            assertThat(removed).isEqualTo("b");
            assertThat(backing).containsExactly("a", "c");
        }

        @Test
        @DisplayName("indexOf / lastIndexOf - 委托索引查找")
        void testIndexOf() {
            var fl = new TestForwardingList<>(new ArrayList<>(List.of("a", "b", "a")));

            assertThat(fl.indexOf("a")).isZero();
            assertThat(fl.lastIndexOf("a")).isEqualTo(2);
            assertThat(fl.indexOf("z")).isEqualTo(-1);
        }

        @Test
        @DisplayName("listIterator - 委托列表迭代器")
        void testListIterator() {
            var fl = new TestForwardingList<>(new ArrayList<>(List.of("a", "b")));

            ListIterator<String> it = fl.listIterator();

            assertThat(it.next()).isEqualTo("a");
            assertThat(it.next()).isEqualTo("b");
        }

        @Test
        @DisplayName("listIterator(index) - 委托指定位置列表迭代器")
        void testListIteratorAtIndex() {
            var fl = new TestForwardingList<>(new ArrayList<>(List.of("a", "b", "c")));

            ListIterator<String> it = fl.listIterator(1);

            assertThat(it.next()).isEqualTo("b");
        }

        @Test
        @DisplayName("subList - 委托子列表")
        void testSubList() {
            var fl = new TestForwardingList<>(new ArrayList<>(List.of("a", "b", "c", "d")));

            List<String> sub = fl.subList(1, 3);

            assertThat(sub).containsExactly("b", "c");
        }

        @Test
        @DisplayName("size / isEmpty - 继承自 ForwardingCollection")
        void testInheritedMethods() {
            var fl = new TestForwardingList<>(new ArrayList<>(List.of("a")));

            assertThat(fl.size()).isEqualTo(1);
            assertThat(fl.isEmpty()).isFalse();
        }
    }

    // ==================== ForwardingSet 测试 ====================

    @Nested
    @DisplayName("ForwardingSet 测试")
    class ForwardingSetTests {

        @Test
        @DisplayName("add / contains / remove - 委托基本方法")
        void testBasicOperations() {
            HashSet<String> backing = new HashSet<>();
            var fs = new TestForwardingSet<>(backing);

            fs.add("a");
            fs.add("b");
            fs.add("a"); // duplicate

            assertThat(fs.size()).isEqualTo(2);
            assertThat(fs.contains("a")).isTrue();

            fs.remove("a");

            assertThat(fs.contains("a")).isFalse();
        }

        @Test
        @DisplayName("equals - Set 语义相等性")
        void testEqualsSetSemantics() {
            HashSet<String> backing = new HashSet<>(Set.of("a", "b"));
            var fs = new TestForwardingSet<>(backing);

            Set<String> other = new HashSet<>(Set.of("a", "b"));

            assertThat(fs.equals(other)).isTrue();
            assertThat(fs.equals(fs)).isTrue();
        }

        @Test
        @DisplayName("equals - 不同内容返回 false")
        void testEqualsDifferentContent() {
            var fs = new TestForwardingSet<>(new HashSet<>(Set.of("a")));

            assertThat(fs.equals(new HashSet<>(Set.of("b")))).isFalse();
        }

        @Test
        @DisplayName("hashCode - Set 语义 hashCode")
        void testHashCode() {
            HashSet<String> backing = new HashSet<>(Set.of("a", "b"));
            var fs = new TestForwardingSet<>(backing);

            assertThat(fs.hashCode()).isEqualTo(backing.hashCode());

            Set<String> other = new HashSet<>(Set.of("a", "b"));

            assertThat(fs.hashCode()).isEqualTo(other.hashCode());
        }

        @Test
        @DisplayName("addAll / removeAll - 继承自 ForwardingCollection")
        void testInheritedMethods() {
            HashSet<String> backing = new HashSet<>();
            var fs = new TestForwardingSet<>(backing);

            fs.addAll(Set.of("a", "b", "c"));

            assertThat(fs.size()).isEqualTo(3);

            fs.removeAll(Set.of("a", "c"));

            assertThat(backing).containsExactly("b");
        }
    }

    // ==================== ForwardingMap 测试 ====================

    @Nested
    @DisplayName("ForwardingMap 测试")
    class ForwardingMapTests {

        @Test
        @DisplayName("put / get - 委托基本方法")
        void testPutGet() {
            HashMap<String, Integer> backing = new HashMap<>();
            var fm = new TestForwardingMap<>(backing);

            fm.put("a", 1);
            fm.put("b", 2);

            assertThat(fm.get("a")).isEqualTo(1);
            assertThat(fm.get("b")).isEqualTo(2);
            assertThat(fm.get("c")).isNull();
        }

        @Test
        @DisplayName("size / isEmpty - 委托大小方法")
        void testSizeIsEmpty() {
            var fm = new TestForwardingMap<>(new HashMap<>());

            assertThat(fm.isEmpty()).isTrue();
            assertThat(fm.size()).isZero();

            fm.put("a", 1);

            assertThat(fm.isEmpty()).isFalse();
            assertThat(fm.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("containsKey / containsValue - 委托包含检查")
        void testContains() {
            HashMap<String, Integer> backing = new HashMap<>(Map.of("a", 1));
            var fm = new TestForwardingMap<>(backing);

            assertThat(fm.containsKey("a")).isTrue();
            assertThat(fm.containsKey("b")).isFalse();
            assertThat(fm.containsValue(1)).isTrue();
            assertThat(fm.containsValue(2)).isFalse();
        }

        @Test
        @DisplayName("remove - 委托移除方法")
        void testRemove() {
            HashMap<String, Integer> backing = new HashMap<>(Map.of("a", 1, "b", 2));
            var fm = new TestForwardingMap<>(backing);

            Integer removed = fm.remove("a");

            assertThat(removed).isEqualTo(1);
            assertThat(backing).containsOnlyKeys("b");
        }

        @Test
        @DisplayName("putAll - 委托批量放入")
        void testPutAll() {
            HashMap<String, Integer> backing = new HashMap<>();
            var fm = new TestForwardingMap<>(backing);

            fm.putAll(Map.of("a", 1, "b", 2));

            assertThat(backing).hasSize(2);
            assertThat(backing).containsEntry("a", 1).containsEntry("b", 2);
        }

        @Test
        @DisplayName("clear - 委托清除方法")
        void testClear() {
            HashMap<String, Integer> backing = new HashMap<>(Map.of("a", 1));
            var fm = new TestForwardingMap<>(backing);

            fm.clear();

            assertThat(backing).isEmpty();
        }

        @Test
        @DisplayName("keySet / values / entrySet - 委托视图方法")
        void testViews() {
            HashMap<String, Integer> backing = new HashMap<>(Map.of("a", 1, "b", 2));
            var fm = new TestForwardingMap<>(backing);

            assertThat(fm.keySet()).containsExactlyInAnyOrder("a", "b");
            assertThat(fm.values()).containsExactlyInAnyOrder(1, 2);
            assertThat(fm.entrySet()).hasSize(2);
        }

        @Test
        @DisplayName("equals - 自身比较返回 true")
        void testEqualsSelf() {
            var fm = new TestForwardingMap<>(new HashMap<>(Map.of("a", 1)));

            assertThat(fm.equals(fm)).isTrue();
        }

        @Test
        @DisplayName("equals - 与相同内容的 Map 相等")
        void testEqualsOtherMap() {
            HashMap<String, Integer> backing = new HashMap<>(Map.of("a", 1));
            var fm = new TestForwardingMap<>(backing);

            Map<String, Integer> other = new HashMap<>(Map.of("a", 1));

            assertThat(fm.equals(other)).isTrue();
        }

        @Test
        @DisplayName("hashCode - 委托 hashCode 方法")
        void testHashCode() {
            HashMap<String, Integer> backing = new HashMap<>(Map.of("a", 1));
            var fm = new TestForwardingMap<>(backing);

            assertThat(fm.hashCode()).isEqualTo(backing.hashCode());
        }

        @Test
        @DisplayName("toString - 委托 toString 方法")
        void testToString() {
            HashMap<String, Integer> backing = new HashMap<>(Map.of("a", 1));
            var fm = new TestForwardingMap<>(backing);

            assertThat(fm.toString()).isEqualTo(backing.toString());
        }
    }

    // ==================== 自定义重写测试 | Custom Override Tests ====================

    @Nested
    @DisplayName("自定义重写测试")
    class CustomOverrideTests {

        @Test
        @DisplayName("ForwardingCollection - 重写 add 添加自定义逻辑")
        void testCustomAddOverride() {
            List<String> addLog = new ArrayList<>();

            ForwardingCollection<String> logging = new ForwardingCollection<>() {
                private final Collection<String> delegate = new ArrayList<>();

                @Override
                protected Collection<String> delegate() {
                    return delegate;
                }

                @Override
                public boolean add(String e) {
                    addLog.add(e);
                    return super.add(e);
                }
            };

            logging.add("hello");
            logging.add("world");

            assertThat(addLog).containsExactly("hello", "world");
            assertThat(logging.size()).isEqualTo(2);
            assertThat(logging.contains("hello")).isTrue();
        }

        @Test
        @DisplayName("ForwardingCollection - addAll 经过覆写的 add")
        void testAddAllHonorsAddOverride() {
            List<String> addLog = new ArrayList<>();

            ForwardingCollection<String> logging = new ForwardingCollection<>() {
                private final Collection<String> delegate = new ArrayList<>();

                @Override
                protected Collection<String> delegate() {
                    return delegate;
                }

                @Override
                public boolean add(String e) {
                    addLog.add(e);
                    return super.add(e);
                }
            };

            logging.addAll(List.of("a", "b", "c"));

            assertThat(addLog).containsExactly("a", "b", "c");
            assertThat(logging.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("ForwardingMap - putAll 经过覆写的 put")
        void testPutAllHonorsPutOverride() {
            List<String> putLog = new ArrayList<>();

            ForwardingMap<String, Integer> logging = new ForwardingMap<>() {
                private final Map<String, Integer> delegate = new HashMap<>();

                @Override
                protected Map<String, Integer> delegate() {
                    return delegate;
                }

                @Override
                public Integer put(String key, Integer value) {
                    putLog.add(key);
                    return super.put(key, value);
                }
            };

            logging.putAll(Map.of("x", 1, "y", 2));

            assertThat(putLog).containsExactlyInAnyOrder("x", "y");
            assertThat(logging.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("ForwardingList - addAll(index) 经过覆写的 add(index)")
        void testAddAllAtIndexHonorsAddOverride() {
            List<String> addLog = new ArrayList<>();

            ForwardingList<String> logging = new ForwardingList<>() {
                private final List<String> delegate = new ArrayList<>(List.of("x"));

                @Override
                protected List<String> delegate() {
                    return delegate;
                }

                @Override
                public void add(int index, String element) {
                    addLog.add(element);
                    super.add(index, element);
                }
            };

            logging.addAll(1, List.of("a", "b"));

            assertThat(addLog).containsExactly("a", "b");
            assertThat(logging).containsExactly("x", "a", "b");
        }

        @Test
        @DisplayName("ForwardingList - 重写 get 返回大写")
        void testCustomGetOverride() {
            ForwardingList<String> upperList = new ForwardingList<>() {
                private final List<String> delegate = new ArrayList<>(List.of("hello", "world"));

                @Override
                protected List<String> delegate() {
                    return delegate;
                }

                @Override
                public String get(int index) {
                    return super.get(index).toUpperCase();
                }
            };

            assertThat(upperList.get(0)).isEqualTo("HELLO");
            assertThat(upperList.get(1)).isEqualTo("WORLD");
        }

        @Test
        @DisplayName("ForwardingMap - 重写 put 添加验证")
        void testCustomPutValidation() {
            ForwardingMap<String, Integer> validating = new ForwardingMap<>() {
                private final Map<String, Integer> delegate = new HashMap<>();

                @Override
                protected Map<String, Integer> delegate() {
                    return delegate;
                }

                @Override
                public Integer put(String key, Integer value) {
                    if (value < 0) {
                        throw new IllegalArgumentException("Negative value: " + value);
                    }
                    return super.put(key, value);
                }
            };

            validating.put("a", 1);

            assertThat(validating.get("a")).isEqualTo(1);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validating.put("b", -1))
                    .withMessageContaining("Negative");
        }
    }
}
