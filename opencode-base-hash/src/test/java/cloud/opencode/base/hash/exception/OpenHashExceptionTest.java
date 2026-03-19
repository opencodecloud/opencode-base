package cloud.opencode.base.hash.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenHashException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("OpenHashException 测试")
class OpenHashExceptionTest {

    @Nested
    @DisplayName("algorithmNotSupported工厂方法测试")
    class AlgorithmNotSupportedTests {

        @Test
        @DisplayName("创建算法不支持异常")
        void testAlgorithmNotSupported() {
            OpenHashException ex = OpenHashException.algorithmNotSupported("INVALID");

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("INVALID");
            assertThat(ex.algorithm()).isEqualTo("INVALID");
        }

        @Test
        @DisplayName("异常消息包含算法名")
        void testExceptionMessage() {
            OpenHashException ex = OpenHashException.algorithmNotSupported("SHA-999");

            assertThat(ex.getMessage()).containsIgnoringCase("not supported");
        }
    }

    @Nested
    @DisplayName("invalidInput工厂方法测试")
    class InvalidInputTests {

        @Test
        @DisplayName("创建无效输入异常")
        void testInvalidInput() {
            OpenHashException ex = OpenHashException.invalidInput("Input cannot be null");

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("null");
        }

        @Test
        @DisplayName("operation设置为hash")
        void testInvalidInputOperation() {
            OpenHashException ex = OpenHashException.invalidInput("Bad input");

            assertThat(ex.operation()).isEqualTo("hash");
        }
    }

    @Nested
    @DisplayName("illegalState工厂方法测试")
    class IllegalStateTests {

        @Test
        @DisplayName("创建非法状态异常")
        void testIllegalState() {
            OpenHashException ex = OpenHashException.illegalState("Hasher already used");

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("already used");
        }
    }

    @Nested
    @DisplayName("hashFailed工厂方法测试")
    class HashFailedTests {

        @Test
        @DisplayName("创建哈希失败异常")
        void testHashFailed() {
            Throwable cause = new RuntimeException("Computation error");
            OpenHashException ex = OpenHashException.hashFailed("murmur3", cause);

            assertThat(ex).isNotNull();
            assertThat(ex.algorithm()).isEqualTo("murmur3");
        }

        @Test
        @DisplayName("带原因的哈希失败异常")
        void testHashFailedWithCause() {
            Throwable cause = new RuntimeException("Internal error");
            OpenHashException ex = OpenHashException.hashFailed("sha256", cause);

            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.algorithm()).isEqualTo("sha256");
        }
    }

    @Nested
    @DisplayName("invalidBloomFilterConfig工厂方法测试")
    class InvalidBloomFilterConfigTests {

        @Test
        @DisplayName("创建无效布隆过滤器配置异常")
        void testInvalidBloomFilterConfig() {
            OpenHashException ex = OpenHashException.invalidBloomFilterConfig("Expected insertions must be positive");

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("positive");
        }
    }

    @Nested
    @DisplayName("nodeNotFound工厂方法测试")
    class NodeNotFoundTests {

        @Test
        @DisplayName("创建节点未找到异常")
        void testNodeNotFound() {
            OpenHashException ex = OpenHashException.nodeNotFound("node1");

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("node1");
        }
    }

    @Nested
    @DisplayName("hasherAlreadyUsed工厂方法测试")
    class HasherAlreadyUsedTests {

        @Test
        @DisplayName("创建Hasher已使用异常")
        void testHasherAlreadyUsed() {
            OpenHashException ex = OpenHashException.hasherAlreadyUsed();

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).containsIgnoringCase("hasher");
        }
    }

    @Nested
    @DisplayName("algorithm方法测试")
    class AlgorithmAccessorTests {

        @Test
        @DisplayName("获取算法名")
        void testGetAlgorithm() {
            OpenHashException ex = OpenHashException.algorithmNotSupported("MD4");

            assertThat(ex.algorithm()).isEqualTo("MD4");
        }

        @Test
        @DisplayName("无算法时返回null")
        void testGetAlgorithmNull() {
            OpenHashException ex = OpenHashException.invalidInput("Bad input");

            assertThat(ex.algorithm()).isNull();
        }
    }

    @Nested
    @DisplayName("operation方法测试")
    class OperationAccessorTests {

        @Test
        @DisplayName("获取操作名")
        void testGetOperation() {
            OpenHashException ex = OpenHashException.hashFailed("sha256", new RuntimeException("error"));

            assertThat(ex.operation()).isNotNull();
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            OpenHashException ex = OpenHashException.invalidInput("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被捕获为Exception")
        void testCatchAsException() {
            try {
                throw OpenHashException.invalidInput("test");
            } catch (Exception e) {
                assertThat(e).isInstanceOf(OpenHashException.class);
            }
        }
    }

    @Nested
    @DisplayName("异常链测试")
    class ExceptionChainTests {

        @Test
        @DisplayName("获取原因")
        void testGetCause() {
            IllegalArgumentException cause = new IllegalArgumentException("original");
            OpenHashException ex = new OpenHashException("wrapper", cause);

            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("无原因时返回null")
        void testNoCause() {
            OpenHashException ex = OpenHashException.invalidInput("no cause");

            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含消息")
        void testToString() {
            OpenHashException ex = OpenHashException.invalidInput("test message");

            assertThat(ex.toString()).contains("test message");
        }
    }
}
