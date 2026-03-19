package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Collection Assert - Fluent assertions for collections
 * 集合断言 - 集合的流式断言
 *
 * <p>Provides comprehensive assertion methods for Collection types including
 * List, Set, and other Collection implementations.</p>
 * <p>为Collection类型提供全面的断言方法，包括List、Set和其他Collection实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent assertion API for collections - 集合的流式断言API</li>
 *   <li>Size, containment, ordering checks - 大小、包含、排序检查</li>
 *   <li>Predicate-based matching (allMatch, anyMatch, noneMatch) - 基于谓词的匹配</li>
 *   <li>Duplicate detection - 重复检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CollectionAssert.assertThat(list)
 *     .isNotEmpty()
 *     .hasSize(3)
 *     .contains("apple")
 *     .containsAll("apple", "banana")
 *     .doesNotContain("cherry");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for concurrent use) - 线程安全: 否（非设计用于并发使用）</li>
 *   <li>Null-safe: Yes (validates non-null collection) - 空值安全: 是（验证非空集合）</li>
 * </ul>
 *
 * @param <E> the element type | 元素类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class CollectionAssert<E> {

    private final Collection<E> actual;

    private CollectionAssert(Collection<E> actual) {
        this.actual = actual;
    }

    /**
     * Creates assertion for collection.
     * 为集合创建断言。
     *
     * @param actual the actual collection | 实际集合
     * @param <E>    the element type | 元素类型
     * @return the assertion | 断言
     */
    public static <E> CollectionAssert<E> assertThat(Collection<E> actual) {
        return new CollectionAssert<>(actual);
    }

    /**
     * Asserts that collection is null.
     * 断言集合为null。
     *
     * @return this | 此对象
     */
    public CollectionAssert<E> isNull() {
        if (actual != null) {
            throw new AssertionException("Expected null but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that collection is not null.
     * 断言集合不为null。
     *
     * @return this | 此对象
     */
    public CollectionAssert<E> isNotNull() {
        if (actual == null) {
            throw new AssertionException("Expected not null but was null");
        }
        return this;
    }

    /**
     * Asserts that collection is empty.
     * 断言集合为空。
     *
     * @return this | 此对象
     */
    public CollectionAssert<E> isEmpty() {
        isNotNull();
        if (!actual.isEmpty()) {
            throw new AssertionException("Expected empty but had " + actual.size() + " elements");
        }
        return this;
    }

    /**
     * Asserts that collection is not empty.
     * 断言集合不为空。
     *
     * @return this | 此对象
     */
    public CollectionAssert<E> isNotEmpty() {
        isNotNull();
        if (actual.isEmpty()) {
            throw new AssertionException("Expected not empty but was empty");
        }
        return this;
    }

    /**
     * Asserts that collection has specified size.
     * 断言集合有指定大小。
     *
     * @param expectedSize the expected size | 期望大小
     * @return this | 此对象
     */
    public CollectionAssert<E> hasSize(int expectedSize) {
        isNotNull();
        if (actual.size() != expectedSize) {
            throw new AssertionException("Expected size " + expectedSize + " but was " + actual.size());
        }
        return this;
    }

    /**
     * Asserts that collection has size greater than.
     * 断言集合大小大于。
     *
     * @param size the size | 大小
     * @return this | 此对象
     */
    public CollectionAssert<E> hasSizeGreaterThan(int size) {
        isNotNull();
        if (actual.size() <= size) {
            throw new AssertionException("Expected size > " + size + " but was " + actual.size());
        }
        return this;
    }

    /**
     * Asserts that collection has size less than.
     * 断言集合大小小于。
     *
     * @param size the size | 大小
     * @return this | 此对象
     */
    public CollectionAssert<E> hasSizeLessThan(int size) {
        isNotNull();
        if (actual.size() >= size) {
            throw new AssertionException("Expected size < " + size + " but was " + actual.size());
        }
        return this;
    }

    /**
     * Asserts that collection contains element.
     * 断言集合包含元素。
     *
     * @param element the element | 元素
     * @return this | 此对象
     */
    public CollectionAssert<E> contains(E element) {
        isNotNull();
        if (!actual.contains(element)) {
            throw new AssertionException("Expected to contain " + element + " but did not");
        }
        return this;
    }

    /**
     * Asserts that collection does not contain element.
     * 断言集合不包含元素。
     *
     * @param element the element | 元素
     * @return this | 此对象
     */
    public CollectionAssert<E> doesNotContain(E element) {
        isNotNull();
        if (actual.contains(element)) {
            throw new AssertionException("Expected not to contain " + element + " but did");
        }
        return this;
    }

    /**
     * Asserts that collection contains all elements.
     * 断言集合包含所有元素。
     *
     * @param elements the elements | 元素
     * @return this | 此对象
     */
    @SafeVarargs
    public final CollectionAssert<E> containsAll(E... elements) {
        isNotNull();
        for (E element : elements) {
            if (!actual.contains(element)) {
                throw new AssertionException("Expected to contain " + element + " but did not");
            }
        }
        return this;
    }

    /**
     * Asserts that collection contains exactly the given elements.
     * 断言集合恰好包含给定元素。
     *
     * @param elements the elements | 元素
     * @return this | 此对象
     */
    @SafeVarargs
    public final CollectionAssert<E> containsExactly(E... elements) {
        isNotNull();
        if (actual.size() != elements.length) {
            throw new AssertionException("Expected " + elements.length + " elements but had " + actual.size());
        }
        List<E> expectedList = Arrays.asList(elements);
        Iterator<E> actualIt = actual.iterator();
        int index = 0;
        for (E expected : expectedList) {
            if (!actualIt.hasNext()) {
                throw new AssertionException("Missing element at index " + index);
            }
            E actualElement = actualIt.next();
            if (!Objects.equals(expected, actualElement)) {
                throw new AssertionException("At index " + index + ": expected " + expected + " but was " + actualElement);
            }
            index++;
        }
        return this;
    }

    /**
     * Asserts that collection contains exactly the given elements in any order.
     * 断言集合恰好包含给定元素（任意顺序）。
     *
     * @param elements the elements | 元素
     * @return this | 此对象
     */
    @SafeVarargs
    public final CollectionAssert<E> containsExactlyInAnyOrder(E... elements) {
        isNotNull();
        if (actual.size() != elements.length) {
            throw new AssertionException("Expected " + elements.length + " elements but had " + actual.size());
        }
        Set<E> expectedSet = new HashSet<>(Arrays.asList(elements));
        Set<E> actualSet = new HashSet<>(actual);
        if (!expectedSet.equals(actualSet)) {
            throw new AssertionException("Expected elements " + expectedSet + " but was " + actualSet);
        }
        return this;
    }

    /**
     * Asserts that collection contains only elements matching predicate.
     * 断言集合只包含匹配谓词的元素。
     *
     * @param predicate the predicate | 谓词
     * @return this | 此对象
     */
    public CollectionAssert<E> allMatch(Predicate<E> predicate) {
        isNotNull();
        for (E element : actual) {
            if (!predicate.test(element)) {
                throw new AssertionException("Element " + element + " does not match predicate");
            }
        }
        return this;
    }

    /**
     * Asserts that collection contains any element matching predicate.
     * 断言集合包含任何匹配谓词的元素。
     *
     * @param predicate the predicate | 谓词
     * @return this | 此对象
     */
    public CollectionAssert<E> anyMatch(Predicate<E> predicate) {
        isNotNull();
        for (E element : actual) {
            if (predicate.test(element)) {
                return this;
            }
        }
        throw new AssertionException("No element matches predicate");
    }

    /**
     * Asserts that collection contains no element matching predicate.
     * 断言集合不包含匹配谓词的元素。
     *
     * @param predicate the predicate | 谓词
     * @return this | 此对象
     */
    public CollectionAssert<E> noneMatch(Predicate<E> predicate) {
        isNotNull();
        for (E element : actual) {
            if (predicate.test(element)) {
                throw new AssertionException("Element " + element + " matches predicate but should not");
            }
        }
        return this;
    }

    /**
     * Asserts that collection has no duplicates.
     * 断言集合没有重复元素。
     *
     * @return this | 此对象
     */
    public CollectionAssert<E> hasNoDuplicates() {
        isNotNull();
        Set<E> seen = new HashSet<>();
        for (E element : actual) {
            if (!seen.add(element)) {
                throw new AssertionException("Found duplicate element: " + element);
            }
        }
        return this;
    }

    /**
     * Asserts that collection is sorted.
     * 断言集合已排序。
     *
     * @param comparator the comparator | 比较器
     * @return this | 此对象
     */
    public CollectionAssert<E> isSorted(Comparator<E> comparator) {
        isNotNull();
        E previous = null;
        for (E element : actual) {
            if (previous != null && comparator.compare(previous, element) > 0) {
                throw new AssertionException("Collection is not sorted: " + previous + " > " + element);
            }
            previous = element;
        }
        return this;
    }

    /**
     * Asserts that collection equals another.
     * 断言集合等于另一个。
     *
     * @param expected the expected collection | 期望集合
     * @return this | 此对象
     */
    public CollectionAssert<E> isEqualTo(Collection<E> expected) {
        if (!Objects.equals(actual, expected)) {
            throw new AssertionException("Expected " + expected + " but was " + actual);
        }
        return this;
    }
}
