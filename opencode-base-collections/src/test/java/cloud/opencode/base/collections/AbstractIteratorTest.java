package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

/**
 * AbstractIteratorTest Tests
 * AbstractIteratorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("AbstractIterator 骨架迭代器测试")
class AbstractIteratorTest {

    @Nested
    @DisplayName("Normal iteration | 正常迭代")
    class NormalIterationTests {

        @Test
        @DisplayName("迭代多个元素")
        void testNormalSequence() {
            var iter = new AbstractIterator<Integer>() {
                private int i = 0;

                @Override
                protected Integer computeNext() {
                    if (i < 3) {
                        return ++i;
                    }
                    return endOfData();
                }
            };

            List<Integer> result = new ArrayList<>();
            while (iter.hasNext()) {
                result.add(iter.next());
            }

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("单元素迭代器")
        void testSingleElement() {
            var iter = new AbstractIterator<String>() {
                private boolean returned = false;

                @Override
                protected String computeNext() {
                    if (!returned) {
                        returned = true;
                        return "only";
                    }
                    return endOfData();
                }
            };

            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.next()).isEqualTo("only");
            assertThat(iter.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("Empty iterator | 空迭代器")
    class EmptyIteratorTests {

        @Test
        @DisplayName("立即结束的迭代器")
        void testEmptyIterator() {
            var iter = new AbstractIterator<Object>() {
                @Override
                protected Object computeNext() {
                    return endOfData();
                }
            };

            assertThat(iter.hasNext()).isFalse();
        }

        @Test
        @DisplayName("空迭代器调用next()抛出NoSuchElementException")
        void testEmptyIteratorNextThrows() {
            var iter = new AbstractIterator<Object>() {
                @Override
                protected Object computeNext() {
                    return endOfData();
                }
            };

            assertThatThrownBy(iter::next)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("peek() | 预览功能")
    class PeekTests {

        @Test
        @DisplayName("peek()返回与next()相同的元素")
        void testPeekReturnsSameAsNext() {
            var iter = new AbstractIterator<String>() {
                private int i = 0;
                private final String[] data = {"a", "b"};

                @Override
                protected String computeNext() {
                    return i < data.length ? data[i++] : endOfData();
                }
            };

            assertThat(iter.peek()).isEqualTo("a");
            assertThat(iter.next()).isEqualTo("a");
            assertThat(iter.peek()).isEqualTo("b");
            assertThat(iter.next()).isEqualTo("b");
        }

        @Test
        @DisplayName("peek()多次调用返回相同元素")
        void testPeekIdempotent() {
            var iter = new AbstractIterator<Integer>() {
                private boolean returned = false;

                @Override
                protected Integer computeNext() {
                    if (!returned) {
                        returned = true;
                        return 42;
                    }
                    return endOfData();
                }
            };

            assertThat(iter.peek()).isEqualTo(42);
            assertThat(iter.peek()).isEqualTo(42);
            assertThat(iter.peek()).isEqualTo(42);
            assertThat(iter.next()).isEqualTo(42);
        }

        @Test
        @DisplayName("空迭代器peek()抛出NoSuchElementException")
        void testPeekOnEmptyThrows() {
            var iter = new AbstractIterator<Object>() {
                @Override
                protected Object computeNext() {
                    return endOfData();
                }
            };

            assertThatThrownBy(iter::peek)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("hasNext() idempotency | hasNext() 幂等性")
    class HasNextIdempotencyTests {

        @Test
        @DisplayName("多次hasNext()不改变状态")
        void testMultipleHasNextCalls() {
            var counter = new int[]{0};
            var iter = new AbstractIterator<String>() {
                @Override
                protected String computeNext() {
                    counter[0]++;
                    return counter[0] <= 1 ? "value" : endOfData();
                }
            };

            // Multiple hasNext() calls should only invoke computeNext() once
            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.hasNext()).isTrue();
            assertThat(counter[0]).isEqualTo(1);
        }

        @Test
        @DisplayName("hasNext()返回false后保持false")
        void testHasNextStaysFalseAfterDone() {
            var iter = new AbstractIterator<Object>() {
                @Override
                protected Object computeNext() {
                    return endOfData();
                }
            };

            assertThat(iter.hasNext()).isFalse();
            assertThat(iter.hasNext()).isFalse();
            assertThat(iter.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("Null element support | 空元素支持")
    class NullElementTests {

        @Test
        @DisplayName("支持null作为有效元素")
        void testNullElements() {
            var iter = new AbstractIterator<String>() {
                private int i = 0;
                private final String[] data = {null, "a", null};

                @Override
                protected String computeNext() {
                    return i < data.length ? data[i++] : endOfData();
                }
            };

            assertThat(iter.hasNext()).isTrue();
            assertThat(iter.next()).isNull();
            assertThat(iter.next()).isEqualTo("a");
            assertThat(iter.next()).isNull();
            assertThat(iter.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("next() without hasNext() | 不调用hasNext()直接调用next()")
    class NextWithoutHasNextTests {

        @Test
        @DisplayName("不调用hasNext()直接调用next()可以工作")
        void testNextWithoutHasNext() {
            var iter = new AbstractIterator<Integer>() {
                private int i = 0;

                @Override
                protected Integer computeNext() {
                    if (i < 2) {
                        return ++i;
                    }
                    return endOfData();
                }
            };

            assertThat(iter.next()).isEqualTo(1);
            assertThat(iter.next()).isEqualTo(2);
            assertThatThrownBy(iter::next)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("Exception in computeNext() | computeNext() 抛出异常")
    class ExceptionTests {

        @Test
        @DisplayName("computeNext()抛出异常后可以重试")
        void testExceptionInComputeNextAllowsRetry() {
            var callCount = new int[]{0};
            var iter = new AbstractIterator<String>() {
                @Override
                protected String computeNext() {
                    callCount[0]++;
                    if (callCount[0] == 1) {
                        throw new RuntimeException("transient error");
                    }
                    return callCount[0] == 2 ? "recovered" : endOfData();
                }
            };

            // First call throws
            assertThatThrownBy(iter::hasNext)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("transient error");

            // Retry throws IllegalStateException — iterator is permanently failed
            assertThatThrownBy(iter::hasNext)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("failed state");
        }
    }
}
