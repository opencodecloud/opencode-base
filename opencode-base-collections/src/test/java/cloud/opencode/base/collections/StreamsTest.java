package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Streams 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("Streams 测试")
class StreamsTest {

    @Nested
    @DisplayName("Zip 操作测试")
    class ZipOperationsTests {

        @Test
        @DisplayName("zip - 合并两个流")
        void testZipStreams() {
            Stream<String> names = Stream.of("Alice", "Bob", "Charlie");
            Stream<Integer> ages = Stream.of(30, 25, 35);

            List<String> result = Streams.zip(names, ages, (name, age) -> name + ":" + age)
                    .toList();

            assertThat(result).containsExactly("Alice:30", "Bob:25", "Charlie:35");
        }

        @Test
        @DisplayName("zip - 不同长度的流取最小长度")
        void testZipDifferentLengths() {
            Stream<String> names = Stream.of("Alice", "Bob");
            Stream<Integer> ages = Stream.of(30, 25, 35, 40);

            List<String> result = Streams.zip(names, ages, (name, age) -> name + ":" + age)
                    .toList();

            assertThat(result).containsExactly("Alice:30", "Bob:25");
        }

        @Test
        @DisplayName("zip - null参数抛异常")
        void testZipNullStreams() {
            assertThatThrownBy(() -> Streams.zip(null, Stream.of(1), (a, b) -> a))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Streams.zip(Stream.of(1), null, (a, b) -> a))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Streams.zip(Stream.of(1), Stream.of(2), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("zip - 合并两个集合")
        void testZipCollections() {
            List<String> names = List.of("Alice", "Bob");
            List<Integer> ages = List.of(30, 25);

            List<String> result = Streams.zip(names, ages, (name, age) -> name + ":" + age)
                    .toList();

            assertThat(result).containsExactly("Alice:30", "Bob:25");
        }

        @Test
        @DisplayName("zipWithIndex - 带索引")
        void testZipWithIndex() {
            List<Streams.IndexedElement<String>> result = Streams.zipWithIndex(
                            Stream.of("a", "b", "c"))
                    .toList();

            assertThat(result).hasSize(3);
            assertThat(result.get(0).index()).isEqualTo(0);
            assertThat(result.get(0).value()).isEqualTo("a");
            assertThat(result.get(1).index()).isEqualTo(1);
            assertThat(result.get(2).index()).isEqualTo(2);
        }

        @Test
        @DisplayName("IndexedElement.indexAsInt - 转换为int")
        void testIndexedElementIndexAsInt() {
            Streams.IndexedElement<String> element = new Streams.IndexedElement<>(5L, "test");
            assertThat(element.indexAsInt()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("FindLast 操作测试")
    class FindLastTests {

        @Test
        @DisplayName("findLast - 查找流最后元素")
        void testFindLastStream() {
            Optional<String> result = Streams.findLast(Stream.of("a", "b", "c"));
            assertThat(result).hasValue("c");
        }

        @Test
        @DisplayName("findLast - 空流返回空")
        void testFindLastEmptyStream() {
            Optional<String> result = Streams.findLast(Stream.empty());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findLast - null流抛异常")
        void testFindLastNullStream() {
            assertThatThrownBy(() -> Streams.findLast((Stream<String>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("findLast - 从Iterable查找")
        void testFindLastIterable() {
            Optional<String> result = Streams.findLast(List.of("a", "b", "c"));
            assertThat(result).hasValue("c");
        }

        @Test
        @DisplayName("findLast - 从空Iterable返回空")
        void testFindLastEmptyIterable() {
            Optional<String> result = Streams.findLast(List.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findLast - 从Deque查找")
        void testFindLastDeque() {
            Deque<String> deque = new ArrayDeque<>(List.of("a", "b", "c"));
            Optional<String> result = Streams.findLast(deque);
            assertThat(result).hasValue("c");
        }

        @Test
        @DisplayName("findLast - 从普通Iterable查找")
        void testFindLastPlainIterable() {
            Iterable<String> iterable = () -> List.of("a", "b", "c").iterator();
            Optional<String> result = Streams.findLast(iterable);
            assertThat(result).hasValue("c");
        }
    }

    @Nested
    @DisplayName("MapWithIndex 操作测试")
    class MapWithIndexTests {

        @Test
        @DisplayName("mapWithIndex - 带索引映射")
        void testMapWithIndex() {
            List<String> result = Streams.mapWithIndex(
                            Stream.of("a", "b", "c"),
                            (element, index) -> index + ":" + element)
                    .toList();

            assertThat(result).containsExactly("0:a", "1:b", "2:c");
        }

        @Test
        @DisplayName("mapWithIndex - null参数抛异常")
        void testMapWithIndexNull() {
            assertThatThrownBy(() -> Streams.mapWithIndex(null, (e, i) -> e))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Streams.mapWithIndex(Stream.of(1), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("filterWithIndex - 带索引过滤")
        void testFilterWithIndex() {
            List<String> result = Streams.filterWithIndex(
                            Stream.of("a", "b", "c", "d"),
                            (element, index) -> index % 2 == 0)
                    .toList();

            assertThat(result).containsExactly("a", "c");
        }
    }

    @Nested
    @DisplayName("Pair 操作测试")
    class PairOperationsTests {

        @Test
        @DisplayName("forEachPair - 处理连续对")
        void testForEachPair() {
            List<String> pairs = new ArrayList<>();
            Streams.forEachPair(Stream.of(1, 2, 3, 4),
                    (a, b) -> pairs.add(a + "-" + b));

            assertThat(pairs).containsExactly("1-2", "2-3", "3-4");
        }

        @Test
        @DisplayName("forEachPair - 空流不处理")
        void testForEachPairEmptyStream() {
            List<String> pairs = new ArrayList<>();
            Streams.forEachPair(Stream.empty(),
                    (a, b) -> pairs.add(a + "-" + b));

            assertThat(pairs).isEmpty();
        }

        @Test
        @DisplayName("forEachPair - 单元素流不处理")
        void testForEachPairSingleElement() {
            List<String> pairs = new ArrayList<>();
            Streams.forEachPair(Stream.of(1),
                    (a, b) -> pairs.add(a + "-" + b));

            assertThat(pairs).isEmpty();
        }

        @Test
        @DisplayName("mapPairs - 映射连续对")
        void testMapPairs() {
            List<Integer> result = Streams.mapPairs(Stream.of(1, 2, 3, 4),
                            (a, b) -> a + b)
                    .toList();

            assertThat(result).containsExactly(3, 5, 7);
        }

        @Test
        @DisplayName("mapPairs - 空流返回空")
        void testMapPairsEmptyStream() {
            List<Integer> result = Streams.mapPairs(Stream.<Integer>empty(),
                            (a, b) -> a + b)
                    .toList();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Concat 操作测试")
    class ConcatOperationsTests {

        @Test
        @DisplayName("concat - 连接多个流")
        void testConcatStreams() {
            List<Integer> result = Streams.concat(
                    Stream.of(1, 2),
                    Stream.of(3, 4),
                    Stream.of(5, 6)
            ).toList();

            assertThat(result).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("concat - 空参数返回空流")
        void testConcatEmpty() {
            @SuppressWarnings("unchecked")
            Stream<Integer>[] emptyStreams = new Stream[0];
            List<Integer> result = Streams.concat(emptyStreams).toList();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("concat - null参数返回空流")
        void testConcatNull() {
            List<Integer> result = Streams.concat((Stream<Integer>[]) null).toList();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("concat - 单个流")
        void testConcatSingle() {
            Stream<Integer> single = Stream.of(1, 2, 3);
            List<Integer> result = Streams.concat(single).toList();
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("concat - 连接多个Iterable")
        void testConcatIterables() {
            List<Integer> result = Streams.concat(
                    List.of(1, 2),
                    List.of(3, 4),
                    List.of(5, 6)
            ).toList();

            assertThat(result).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("concat - 空Iterable数组返回空流")
        void testConcatEmptyIterables() {
            List<Integer> result = Streams.<Integer>concat(new Iterable[0]).toList();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Interleave 操作测试")
    class InterleaveOperationsTests {

        @Test
        @DisplayName("interleave - 交错两个流")
        void testInterleave() {
            List<Integer> result = Streams.interleave(
                    Stream.of(1, 3, 5),
                    Stream.of(2, 4, 6)
            ).toList();

            assertThat(result).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("interleave - 不同长度")
        void testInterleaveDifferentLengths() {
            List<Integer> result = Streams.interleave(
                    Stream.of(1, 3, 5, 7),
                    Stream.of(2, 4)
            ).toList();

            assertThat(result).containsExactly(1, 2, 3, 4, 5, 7);
        }

        @Test
        @DisplayName("interleave - null参数抛异常")
        void testInterleaveNull() {
            assertThatThrownBy(() -> Streams.interleave(null, Stream.of(1)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Streams.interleave(Stream.of(1), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("流创建测试")
    class StreamCreationTests {

        @Test
        @DisplayName("stream - 从Optional创建")
        void testStreamFromOptional() {
            List<String> result = Streams.stream(Optional.of("hello")).toList();
            assertThat(result).containsExactly("hello");
        }

        @Test
        @DisplayName("stream - 从空Optional创建")
        void testStreamFromEmptyOptional() {
            List<String> result = Streams.stream(Optional.<String>empty()).toList();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("stream - 从Iterator创建")
        void testStreamFromIterator() {
            List<String> result = Streams.stream(List.of("a", "b", "c").iterator()).toList();
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("stream - 从Iterable创建")
        void testStreamFromIterable() {
            Iterable<String> iterable = () -> List.of("a", "b", "c").iterator();
            List<String> result = Streams.stream(iterable).toList();
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("stream - 从Collection创建")
        void testStreamFromCollection() {
            List<String> result = Streams.stream((Iterable<String>) List.of("a", "b")).toList();
            assertThat(result).containsExactly("a", "b");
        }

        @Test
        @DisplayName("stream - 从Enumeration创建")
        void testStreamFromEnumeration() {
            Vector<String> vector = new Vector<>(List.of("a", "b", "c"));
            List<String> result = Streams.stream(vector.elements()).toList();
            assertThat(result).containsExactly("a", "b", "c");
        }
    }
}
