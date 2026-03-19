package cloud.opencode.base.log.enhance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ExceptionLog 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("ExceptionLog 测试")
class ExceptionLogTest {

    @BeforeEach
    void setUp() {
        ExceptionLog.clearDeduplicationTracking();
        ExceptionLog.clearRateLimitTracking();
    }

    @AfterEach
    void tearDown() {
        ExceptionLog.clearDeduplicationTracking();
        ExceptionLog.clearRateLimitTracking();
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(ExceptionLog.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = ExceptionLog.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("error方法测试")
    class ErrorTests {

        @Test
        @DisplayName("记录错误日志")
        void testError() {
            RuntimeException ex = new RuntimeException("Test error");
            assertThatCode(() -> ExceptionLog.error("Error occurred", ex))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("errorOnce方法测试")
    class ErrorOnceTests {

        @Test
        @DisplayName("记录一次性错误日志")
        void testErrorOnce() {
            RuntimeException ex = new RuntimeException("Once error");
            assertThatCode(() -> {
                ExceptionLog.errorOnce("Error once", ex);
                ExceptionLog.errorOnce("Error once", ex);
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("errorRateLimited方法测试")
    class ErrorRateLimitedTests {

        @Test
        @DisplayName("记录限速错误日志")
        void testErrorRateLimited() {
            RuntimeException ex = new RuntimeException("Rate limited error");
            assertThatCode(() -> {
                ExceptionLog.errorRateLimited("Rate limited", ex, Duration.ofMillis(100));
                ExceptionLog.errorRateLimited("Rate limited", ex, Duration.ofMillis(100));
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("warn方法测试")
    class WarnTests {

        @Test
        @DisplayName("记录警告日志")
        void testWarn() {
            RuntimeException ex = new RuntimeException("Test warning");
            assertThatCode(() -> ExceptionLog.warn("Warning occurred", ex))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getRootCause方法测试")
    class GetRootCauseTests {

        @Test
        @DisplayName("返回根因")
        void testGetRootCause() {
            SQLException root = new SQLException("DB error");
            IOException mid = new IOException("IO error", root);
            RuntimeException top = new RuntimeException("Top error", mid);

            Throwable rootCause = ExceptionLog.getRootCause(top);

            assertThat(rootCause).isSameAs(root);
        }

        @Test
        @DisplayName("无cause时返回自身")
        void testGetRootCauseNoCause() {
            RuntimeException ex = new RuntimeException("No cause");

            Throwable rootCause = ExceptionLog.getRootCause(ex);

            assertThat(rootCause).isSameAs(ex);
        }

        @Test
        @DisplayName("null输入返回null")
        void testGetRootCauseNull() {
            assertThat(ExceptionLog.getRootCause(null)).isNull();
        }
    }

    @Nested
    @DisplayName("getCausalChain方法测试")
    class GetCausalChainTests {

        @Test
        @DisplayName("返回因果链")
        void testGetCausalChain() {
            SQLException root = new SQLException("DB error");
            IOException mid = new IOException("IO error", root);
            RuntimeException top = new RuntimeException("Top error", mid);

            List<Throwable> chain = ExceptionLog.getCausalChain(top);

            assertThat(chain).hasSize(3);
            assertThat(chain.get(0)).isSameAs(top);
            assertThat(chain.get(1)).isSameAs(mid);
            assertThat(chain.get(2)).isSameAs(root);
        }

        @Test
        @DisplayName("单个异常返回单元素列表")
        void testGetCausalChainSingle() {
            RuntimeException ex = new RuntimeException("Single");

            List<Throwable> chain = ExceptionLog.getCausalChain(ex);

            assertThat(chain).hasSize(1);
            assertThat(chain.getFirst()).isSameAs(ex);
        }
    }

    @Nested
    @DisplayName("formatExceptionChain方法测试")
    class FormatExceptionChainTests {

        @Test
        @DisplayName("格式化异常链")
        void testFormatExceptionChain() {
            SQLException root = new SQLException("DB error");
            RuntimeException top = new RuntimeException("Top error", root);

            String formatted = ExceptionLog.formatExceptionChain(top);

            assertThat(formatted).contains("RuntimeException");
            assertThat(formatted).contains("SQLException");
            assertThat(formatted).contains("->");
        }
    }

    @Nested
    @DisplayName("summarize方法测试")
    class SummarizeTests {

        @Test
        @DisplayName("创建异常摘要")
        void testSummarize() {
            SQLException root = new SQLException("DB error");
            RuntimeException top = new RuntimeException("Top error", root);

            String summary = ExceptionLog.summarize(top);

            assertThat(summary).contains("RuntimeException");
            assertThat(summary).contains("Top error");
            assertThat(summary).contains("SQLException");
            assertThat(summary).contains("DB error");
        }

        @Test
        @DisplayName("null输入返回'null'")
        void testSummarizeNull() {
            assertThat(ExceptionLog.summarize(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("无消息的异常")
        void testSummarizeNoMessage() {
            RuntimeException ex = new RuntimeException((String) null);
            String summary = ExceptionLog.summarize(ex);

            assertThat(summary).contains("no message");
        }
    }

    @Nested
    @DisplayName("getStackTraceString方法测试")
    class GetStackTraceStringTests {

        @Test
        @DisplayName("获取堆栈跟踪字符串")
        void testGetStackTraceString() {
            RuntimeException ex = new RuntimeException("Test");

            String stackTrace = ExceptionLog.getStackTraceString(ex);

            assertThat(stackTrace).contains("RuntimeException");
            assertThat(stackTrace).contains("Test");
        }

        @Test
        @DisplayName("null输入返回空字符串")
        void testGetStackTraceStringNull() {
            assertThat(ExceptionLog.getStackTraceString(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCompactStackTrace方法测试")
    class GetCompactStackTraceTests {

        @Test
        @DisplayName("获取紧凑堆栈跟踪")
        void testGetCompactStackTrace() {
            RuntimeException ex = new RuntimeException("Test");

            String compact = ExceptionLog.getCompactStackTrace(ex, 3);

            assertThat(compact).contains("RuntimeException");
            assertThat(compact).contains("Test");
        }

        @Test
        @DisplayName("null输入返回空字符串")
        void testGetCompactStackTraceNull() {
            assertThat(ExceptionLog.getCompactStackTrace(null, 3)).isEmpty();
        }

        @Test
        @DisplayName("超过最大深度时显示省略")
        void testGetCompactStackTraceWithMore() {
            RuntimeException ex = new RuntimeException("Test");

            String compact = ExceptionLog.getCompactStackTrace(ex, 1);

            assertThat(compact).contains("more");
        }
    }

    @Nested
    @DisplayName("getExceptionSignature方法测试")
    class GetExceptionSignatureTests {

        @Test
        @DisplayName("获取异常签名")
        void testGetExceptionSignature() {
            RuntimeException ex = new RuntimeException("Test");

            String signature = ExceptionLog.getExceptionSignature(ex);

            assertThat(signature).isNotEmpty();
            assertThat(signature).contains("RuntimeException");
        }

        @Test
        @DisplayName("null输入返回'null'")
        void testGetExceptionSignatureNull() {
            assertThat(ExceptionLog.getExceptionSignature(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("包含根因类型")
        void testGetExceptionSignatureWithRootCause() {
            SQLException root = new SQLException("DB error");
            RuntimeException top = new RuntimeException("Top error", root);

            String signature = ExceptionLog.getExceptionSignature(top);

            assertThat(signature).contains("root:");
            assertThat(signature).contains("SQLException");
        }
    }

    @Nested
    @DisplayName("containsExceptionType方法测试")
    class ContainsExceptionTypeTests {

        @Test
        @DisplayName("链中包含指定类型返回true")
        void testContainsExceptionTypeTrue() {
            SQLException root = new SQLException("DB error");
            RuntimeException top = new RuntimeException("Top error", root);

            assertThat(ExceptionLog.containsExceptionType(top, SQLException.class)).isTrue();
        }

        @Test
        @DisplayName("链中不包含指定类型返回false")
        void testContainsExceptionTypeFalse() {
            RuntimeException ex = new RuntimeException("Test");

            assertThat(ExceptionLog.containsExceptionType(ex, SQLException.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("findExceptionOfType方法测试")
    class FindExceptionOfTypeTests {

        @Test
        @DisplayName("找到指定类型异常")
        void testFindExceptionOfTypeFound() {
            SQLException root = new SQLException("DB error");
            RuntimeException top = new RuntimeException("Top error", root);

            SQLException found = ExceptionLog.findExceptionOfType(top, SQLException.class);

            assertThat((Throwable) found).isSameAs(root);
        }

        @Test
        @DisplayName("未找到返回null")
        void testFindExceptionOfTypeNotFound() {
            RuntimeException ex = new RuntimeException("Test");

            SQLException found = ExceptionLog.findExceptionOfType(ex, SQLException.class);

            assertThat((Throwable) found).isNull();
        }
    }

    @Nested
    @DisplayName("清除追踪测试")
    class ClearTrackingTests {

        @Test
        @DisplayName("clearDeduplicationTracking清除去重追踪")
        void testClearDeduplicationTracking() {
            assertThatCode(() -> ExceptionLog.clearDeduplicationTracking()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("clearRateLimitTracking清除限速追踪")
        void testClearRateLimitTracking() {
            assertThatCode(() -> ExceptionLog.clearRateLimitTracking()).doesNotThrowAnyException();
        }
    }
}
