package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ConcurrentHashMultiset 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ConcurrentHashMultiset 测试")
class ConcurrentHashMultisetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 Multiset")
        void testCreate() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();

            assertThat(multiset).isEmpty();
            assertThat(multiset.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create(100);

            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("create - 从 Iterable 创建")
        void testCreateFromIterable() {
            List<String> source = List.of("a", "b", "a", "c");
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create(source);

            assertThat(multiset.size()).isEqualTo(4);
            assertThat(multiset.count("a")).isEqualTo(2);
            assertThat(multiset.count("b")).isEqualTo(1);
            assertThat(multiset.count("c")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Multiset 方法测试")
    class MultisetMethodTests {

        @Test
        @DisplayName("count - 计数")
        void testCount() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 1);

            assertThat(multiset.count("a")).isEqualTo(3);
            assertThat(multiset.count("b")).isEqualTo(1);
            assertThat(multiset.count("c")).isZero();
        }

        @Test
        @DisplayName("count - null 元素返回 0")
        void testCountNull() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a");

            assertThat(multiset.count(null)).isZero();
        }

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();

            int oldCount = multiset.add("a", 3);

            assertThat(oldCount).isZero();
            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("add - 添加零次不改变计数")
        void testAddZero() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 2);

            int oldCount = multiset.add("a", 0);

            assertThat(oldCount).isEqualTo(2);
            assertThat(multiset.count("a")).isEqualTo(2);
        }

        @Test
        @DisplayName("add - null 元素抛异常")
        void testAddNull() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();

            assertThatThrownBy(() -> multiset.add(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("add - 负数次数抛异常")
        void testAddNegative() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();

            assertThatThrownBy(() -> multiset.add("a", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("remove - 移除元素")
        void testRemove() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 5);

            int oldCount = multiset.remove("a", 2);

            assertThat(oldCount).isEqualTo(5);
            assertThat(multiset.count("a")).isEqualTo(3);
        }

        @Test
        @DisplayName("remove - 移除全部")
        void testRemoveAll() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 3);

            int oldCount = multiset.remove("a", 5);

            assertThat(oldCount).isEqualTo(3);
            assertThat(multiset.count("a")).isZero();
        }

        @Test
        @DisplayName("setCount - 设置计数")
        void testSetCount() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 2);

            int oldCount = multiset.setCount("a", 5);

            assertThat(oldCount).isEqualTo(2);
            assertThat(multiset.count("a")).isEqualTo(5);
        }

        @Test
        @DisplayName("setCount - 设置为零移除元素")
        void testSetCountZero() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 2);

            int oldCount = multiset.setCount("a", 0);

            assertThat(oldCount).isEqualTo(2);
            assertThat(multiset.contains("a")).isFalse();
        }

        @Test
        @DisplayName("setCount - 条件设置成功")
        void testSetCountConditionalSuccess() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 2);

            boolean result = multiset.setCount("a", 2, 5);

            assertThat(result).isTrue();
            assertThat(multiset.count("a")).isEqualTo(5);
        }

        @Test
        @DisplayName("setCount - 条件设置失败")
        void testSetCountConditionalFail() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 2);

            boolean result = multiset.setCount("a", 3, 5);

            assertThat(result).isFalse();
            assertThat(multiset.count("a")).isEqualTo(2);
        }

        @Test
        @DisplayName("elementSet - 元素集")
        void testElementSet() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 2);
            multiset.add("b", 1);

            Set<String> elementSet = multiset.elementSet();

            assertThat(elementSet).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("entrySet - 条目集")
        void testEntrySet() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 2);
            multiset.add("b", 1);

            var entrySet = multiset.entrySet();

            assertThat(entrySet).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Collection 方法测试")
    class CollectionMethodTests {

        @Test
        @DisplayName("size - 总大小")
        void testSize() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            assertThat(multiset.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();

            assertThat(multiset.isEmpty()).isTrue();

            multiset.add("a");

            assertThat(multiset.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains - 包含")
        void testContains() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a");

            assertThat(multiset.contains("a")).isTrue();
            assertThat(multiset.contains("b")).isFalse();
        }

        @Test
        @DisplayName("add(E) - 添加单个")
        void testAddSingle() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();

            boolean result = multiset.add("a");

            assertThat(result).isTrue();
            assertThat(multiset.count("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("remove(Object) - 移除单个")
        void testRemoveSingle() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 3);

            boolean result = multiset.remove("a");

            assertThat(result).isTrue();
            assertThat(multiset.count("a")).isEqualTo(2);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            multiset.clear();

            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("iterator - 迭代")
        void testIterator() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 2);

            List<String> result = new ArrayList<>();
            for (String s : multiset) {
                result.add(s);
            }

            assertThat(result).hasSize(2);
            assertThat(result).containsOnly("a");
        }

        @Test
        @DisplayName("iterator.remove - 不支持")
        void testIteratorRemoveNotSupported() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a");

            Iterator<String> it = multiset.iterator();
            it.next();

            assertThatThrownBy(it::remove)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            ConcurrentHashMultiset<String> multiset1 = ConcurrentHashMultiset.create();
            multiset1.add("a", 2);
            multiset1.add("b", 1);

            ConcurrentHashMultiset<String> multiset2 = ConcurrentHashMultiset.create();
            multiset2.add("a", 2);
            multiset2.add("b", 1);

            assertThat(multiset1).isEqualTo(multiset2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            ConcurrentHashMultiset<String> multiset1 = ConcurrentHashMultiset.create();
            multiset1.add("a", 2);

            ConcurrentHashMultiset<String> multiset2 = ConcurrentHashMultiset.create();
            multiset2.add("a", 2);

            assertThat(multiset1.hashCode()).isEqualTo(multiset2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("a", 2);

            assertThat(multiset.toString()).contains("a").contains("2");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发添加")
        void testConcurrentAdd() throws InterruptedException {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            int threads = 10;
            int operationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            multiset.add("element");
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(multiset.count("element")).isEqualTo(threads * operationsPerThread);
        }

        @Test
        @DisplayName("并发添加移除")
        void testConcurrentAddRemove() throws InterruptedException {
            ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
            multiset.add("element", 1000);

            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 50; j++) {
                            if (threadNum % 2 == 0) {
                                multiset.add("element");
                            } else {
                                multiset.remove("element", 1);
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            // 计数应该是正数
            assertThat(multiset.count("element")).isGreaterThanOrEqualTo(0);
        }
    }
}
