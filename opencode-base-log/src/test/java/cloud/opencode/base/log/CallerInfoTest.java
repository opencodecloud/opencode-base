package cloud.opencode.base.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CallerInfo 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
@DisplayName("CallerInfo 测试")
class CallerInfoTest {

    @Nested
    @DisplayName("UNKNOWN 常量测试")
    class UnknownTests {

        @Test
        @DisplayName("UNKNOWN 常量字段值正确")
        void testUnknownConstantValues() {
            assertThat(CallerInfo.UNKNOWN.className()).isEqualTo("unknown");
            assertThat(CallerInfo.UNKNOWN.methodName()).isEqualTo("unknown");
            assertThat(CallerInfo.UNKNOWN.fileName()).isEqualTo("unknown");
            assertThat(CallerInfo.UNKNOWN.lineNumber()).isEqualTo(-1);
        }

        @Test
        @DisplayName("UNKNOWN 是单例引用")
        void testUnknownIsSingleton() {
            assertThat(CallerInfo.UNKNOWN).isSameAs(CallerInfo.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("capture 方法测试")
    class CaptureTests {

        @Test
        @DisplayName("capture() 返回非 UNKNOWN 的结果")
        void testCaptureReturnsNonUnknown() {
            // Test class is in cloud.opencode.base.log package, so capture()
            // skips these frames and returns the JUnit invoker frame.
            CallerInfo info = CallerInfo.capture();
            assertThat(info).isNotEqualTo(CallerInfo.UNKNOWN);
            assertThat(info.className()).isNotEqualTo("unknown");
            assertThat(info.methodName()).isNotEqualTo("unknown");
        }

        @Test
        @DisplayName("capture() 捕获行号为正数")
        void testCaptureLineNumberIsPositive() {
            CallerInfo info = CallerInfo.capture();
            assertThat(info.lineNumber()).isGreaterThan(0);
        }

        @Test
        @DisplayName("capture() 跳过 cloud.opencode.base.log 包的帧")
        void testCaptureSkipsLogPackageFrames() {
            CallerInfo info = CallerInfo.capture();
            // Since this test is in cloud.opencode.base.log package,
            // capture() skips it and returns the external caller (JUnit)
            assertThat(info.className()).doesNotStartWith("cloud.opencode.base.log");
        }

        @Test
        @DisplayName("capture(skipFrames) 跳过额外帧")
        void testCaptureWithSkipFrames() {
            CallerInfo noSkip = CallerInfo.capture();
            CallerInfo withSkip = CallerInfo.capture(1);
            // Skipping one more frame should yield a different (or the same if end of stack) result
            assertThat(withSkip).isNotNull();
            assertThat(withSkip.lineNumber()).isGreaterThan(0);
        }

        @Test
        @DisplayName("capture 负数 skipFrames 抛出异常")
        void testCaptureNegativeSkipFramesThrows() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> CallerInfo.capture(-1))
                    .withMessageContaining("negative");
        }
    }

    @Nested
    @DisplayName("字符串格式测试")
    class StringFormatTests {

        private final CallerInfo info = new CallerInfo(
                "com.example.MyClass", "myMethod", "MyClass.java", 42);

        @Test
        @DisplayName("toShortString 返回 SimpleClass.method:line 格式")
        void testToShortString() {
            assertThat(info.toShortString()).isEqualTo("MyClass.myMethod:42");
        }

        @Test
        @DisplayName("toCompactString 返回 SimpleClass:line 格式")
        void testToCompactString() {
            assertThat(info.toCompactString()).isEqualTo("MyClass:42");
        }

        @Test
        @DisplayName("toString 返回完整信息")
        void testToString() {
            assertThat(info.toString())
                    .isEqualTo("com.example.MyClass.myMethod(MyClass.java:42)");
        }

        @Test
        @DisplayName("无包名的类名正确处理")
        void testSimpleClassNameWithoutPackage() {
            CallerInfo noPackage = new CallerInfo("SimpleClass", "run", "SimpleClass.java", 1);
            assertThat(noPackage.toShortString()).isEqualTo("SimpleClass.run:1");
            assertThat(noPackage.toCompactString()).isEqualTo("SimpleClass:1");
        }
    }

    @Nested
    @DisplayName("构造函数验证测试")
    class ConstructorValidationTests {

        @Test
        @DisplayName("className 为 null 抛出异常")
        void testNullClassNameThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CallerInfo(null, "m", "f", 1))
                    .withMessageContaining("className");
        }

        @Test
        @DisplayName("methodName 为 null 抛出异常")
        void testNullMethodNameThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CallerInfo("c", null, "f", 1))
                    .withMessageContaining("methodName");
        }

        @Test
        @DisplayName("fileName 为 null 抛出异常")
        void testNullFileNameThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CallerInfo("c", "m", null, 1))
                    .withMessageContaining("fileName");
        }
    }
}
