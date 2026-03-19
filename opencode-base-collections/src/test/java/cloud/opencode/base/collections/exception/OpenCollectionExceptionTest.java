package cloud.opencode.base.collections.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCollectionException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("OpenCollectionException 测试")
class OpenCollectionExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testConstructorWithMessage() {
            OpenCollectionException ex = new OpenCollectionException("test message");

            assertThat(ex.getMessage()).isEqualTo("test message");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("root cause");
            OpenCollectionException ex = new OpenCollectionException("test message", cause);

            assertThat(ex.getMessage()).isEqualTo("test message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("带原因构造")
        void testConstructorWithCause() {
            Throwable cause = new RuntimeException("root cause");
            OpenCollectionException ex = new OpenCollectionException(cause);

            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("null 消息构造")
        void testConstructorWithNullMessage() {
            OpenCollectionException ex = new OpenCollectionException((String) null);

            assertThat(ex.getMessage()).isNull();
        }

        @Test
        @DisplayName("null 原因构造")
        void testConstructorWithNullCause() {
            OpenCollectionException ex = new OpenCollectionException((Throwable) null);

            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("emptyCollection - 空集合异常")
        void testEmptyCollection() {
            OpenCollectionException ex = OpenCollectionException.emptyCollection("list");

            assertThat(ex.getMessage()).isEqualTo("Collection is empty: list");
        }

        @Test
        @DisplayName("emptyCollection - null 类型")
        void testEmptyCollectionNullType() {
            OpenCollectionException ex = OpenCollectionException.emptyCollection(null);

            assertThat(ex.getMessage()).isEqualTo("Collection is empty: null");
        }

        @Test
        @DisplayName("indexOutOfBounds - 索引越界异常")
        void testIndexOutOfBounds() {
            OpenCollectionException ex = OpenCollectionException.indexOutOfBounds(10, 5);

            assertThat(ex.getMessage()).isEqualTo("Index: 10, Size: 5");
        }

        @Test
        @DisplayName("indexOutOfBounds - 负索引")
        void testIndexOutOfBoundsNegativeIndex() {
            OpenCollectionException ex = OpenCollectionException.indexOutOfBounds(-1, 5);

            assertThat(ex.getMessage()).isEqualTo("Index: -1, Size: 5");
        }

        @Test
        @DisplayName("duplicateKey - 重复键异常")
        void testDuplicateKey() {
            OpenCollectionException ex = OpenCollectionException.duplicateKey("key1");

            assertThat(ex.getMessage()).isEqualTo("Duplicate key: key1");
        }

        @Test
        @DisplayName("duplicateKey - null 键")
        void testDuplicateKeyNull() {
            OpenCollectionException ex = OpenCollectionException.duplicateKey(null);

            assertThat(ex.getMessage()).isEqualTo("Duplicate key: null");
        }

        @Test
        @DisplayName("duplicateKey - 整数键")
        void testDuplicateKeyInteger() {
            OpenCollectionException ex = OpenCollectionException.duplicateKey(42);

            assertThat(ex.getMessage()).isEqualTo("Duplicate key: 42");
        }

        @Test
        @DisplayName("duplicateValue - 重复值异常")
        void testDuplicateValue() {
            OpenCollectionException ex = OpenCollectionException.duplicateValue("value1");

            assertThat(ex.getMessage()).isEqualTo("Duplicate value: value1");
        }

        @Test
        @DisplayName("duplicateValue - null 值")
        void testDuplicateValueNull() {
            OpenCollectionException ex = OpenCollectionException.duplicateValue(null);

            assertThat(ex.getMessage()).isEqualTo("Duplicate value: null");
        }

        @Test
        @DisplayName("nullElement - 空元素异常")
        void testNullElement() {
            OpenCollectionException ex = OpenCollectionException.nullElement();

            assertThat(ex.getMessage()).isEqualTo("Null element not allowed");
        }

        @Test
        @DisplayName("nullKey - 空键异常")
        void testNullKey() {
            OpenCollectionException ex = OpenCollectionException.nullKey();

            assertThat(ex.getMessage()).isEqualTo("Null key not allowed");
        }

        @Test
        @DisplayName("nullValue - 空值异常")
        void testNullValue() {
            OpenCollectionException ex = OpenCollectionException.nullValue();

            assertThat(ex.getMessage()).isEqualTo("Null value not allowed");
        }

        @Test
        @DisplayName("immutableCollection - 不可变集合修改异常")
        void testImmutableCollection() {
            OpenCollectionException ex = OpenCollectionException.immutableCollection();

            assertThat(ex.getMessage()).isEqualTo("Cannot modify immutable collection");
        }

        @Test
        @DisplayName("elementNotFound - 元素未找到异常")
        void testElementNotFound() {
            OpenCollectionException ex = OpenCollectionException.elementNotFound("element1");

            assertThat(ex.getMessage()).isEqualTo("Element not found: element1");
        }

        @Test
        @DisplayName("elementNotFound - null 元素")
        void testElementNotFoundNull() {
            OpenCollectionException ex = OpenCollectionException.elementNotFound(null);

            assertThat(ex.getMessage()).isEqualTo("Element not found: null");
        }

        @Test
        @DisplayName("keyNotFound - 键未找到异常")
        void testKeyNotFound() {
            OpenCollectionException ex = OpenCollectionException.keyNotFound("key1");

            assertThat(ex.getMessage()).isEqualTo("Key not found: key1");
        }

        @Test
        @DisplayName("keyNotFound - null 键")
        void testKeyNotFoundNull() {
            OpenCollectionException ex = OpenCollectionException.keyNotFound(null);

            assertThat(ex.getMessage()).isEqualTo("Key not found: null");
        }

        @Test
        @DisplayName("illegalCapacity - 非法容量异常")
        void testIllegalCapacity() {
            OpenCollectionException ex = OpenCollectionException.illegalCapacity(-5);

            assertThat(ex.getMessage()).isEqualTo("Illegal capacity: -5");
        }

        @Test
        @DisplayName("illegalCapacity - 零容量")
        void testIllegalCapacityZero() {
            OpenCollectionException ex = OpenCollectionException.illegalCapacity(0);

            assertThat(ex.getMessage()).isEqualTo("Illegal capacity: 0");
        }

        @Test
        @DisplayName("multipleElementsFound - 多元素异常")
        void testMultipleElementsFound() {
            OpenCollectionException ex = OpenCollectionException.multipleElementsFound(3);

            assertThat(ex.getMessage()).isEqualTo("Expected one element but found: 3");
        }

        @Test
        @DisplayName("multipleElementsFound - 两个元素")
        void testMultipleElementsFoundTwo() {
            OpenCollectionException ex = OpenCollectionException.multipleElementsFound(2);

            assertThat(ex.getMessage()).isEqualTo("Expected one element but found: 2");
        }

        @Test
        @DisplayName("negativeSize - 负数大小异常")
        void testNegativeSize() {
            OpenCollectionException ex = OpenCollectionException.negativeSize(-1);

            assertThat(ex.getMessage()).isEqualTo("Size cannot be negative: -1");
        }

        @Test
        @DisplayName("negativeSize - 大负数")
        void testNegativeSizeLarge() {
            OpenCollectionException ex = OpenCollectionException.negativeSize(-1000);

            assertThat(ex.getMessage()).isEqualTo("Size cannot be negative: -1000");
        }

        @Test
        @DisplayName("unsupportedOperation - 不支持的操作异常")
        void testUnsupportedOperation() {
            OpenCollectionException ex = OpenCollectionException.unsupportedOperation("delete");

            assertThat(ex.getMessage()).isEqualTo("Unsupported operation: delete");
        }

        @Test
        @DisplayName("unsupportedOperation - null 操作")
        void testUnsupportedOperationNull() {
            OpenCollectionException ex = OpenCollectionException.unsupportedOperation(null);

            assertThat(ex.getMessage()).isEqualTo("Unsupported operation: null");
        }
    }

    @Nested
    @DisplayName("异常行为测试")
    class ExceptionBehaviorTests {

        @Test
        @DisplayName("是 RuntimeException 子类")
        void testIsRuntimeException() {
            OpenCollectionException ex = new OpenCollectionException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被抛出和捕获")
        void testThrowAndCatch() {
            assertThatThrownBy(() -> {
                throw new OpenCollectionException("test");
            }).isInstanceOf(OpenCollectionException.class)
              .hasMessage("test");
        }

        @Test
        @DisplayName("工厂方法返回的异常可以被抛出")
        void testFactoryMethodExceptionCanBeThrown() {
            assertThatThrownBy(() -> {
                throw OpenCollectionException.emptyCollection("set");
            }).isInstanceOf(OpenCollectionException.class)
              .hasMessage("Collection is empty: set");
        }

        @Test
        @DisplayName("异常链完整保留")
        void testExceptionChaining() {
            IllegalStateException rootCause = new IllegalStateException("root");
            RuntimeException middleCause = new RuntimeException("middle", rootCause);
            OpenCollectionException ex = new OpenCollectionException("top", middleCause);

            assertThat(ex.getCause()).isEqualTo(middleCause);
            assertThat(ex.getCause().getCause()).isEqualTo(rootCause);
        }
    }
}
