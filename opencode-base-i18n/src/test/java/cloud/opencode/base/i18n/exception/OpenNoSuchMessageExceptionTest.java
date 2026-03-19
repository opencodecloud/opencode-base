package cloud.opencode.base.i18n.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenNoSuchMessageException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("OpenNoSuchMessageException 测试")
class OpenNoSuchMessageExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用key和locale创建异常")
        void testConstructorWithKeyAndLocale() {
            OpenNoSuchMessageException ex = new OpenNoSuchMessageException("test.key", Locale.CHINESE);

            assertThat(ex.key()).isEqualTo("test.key");
            assertThat(ex.locale()).isEqualTo(Locale.CHINESE);
            assertThat(ex.getMessage()).contains("test.key");
            assertThat(ex.getMessage()).contains("zh");
        }

        @Test
        @DisplayName("使用key、locale和cause创建异常")
        void testConstructorWithCause() {
            RuntimeException cause = new RuntimeException("root cause");
            OpenNoSuchMessageException ex = new OpenNoSuchMessageException("error.key", Locale.ENGLISH, cause);

            assertThat(ex.key()).isEqualTo("error.key");
            assertThat(ex.locale()).isEqualTo(Locale.ENGLISH);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("访问器方法测试")
    class AccessorTests {

        @Test
        @DisplayName("获取key")
        void testKey() {
            OpenNoSuchMessageException ex = new OpenNoSuchMessageException("user.name", Locale.KOREAN);

            assertThat(ex.key()).isEqualTo("user.name");
        }

        @Test
        @DisplayName("获取locale")
        void testLocale() {
            OpenNoSuchMessageException ex = new OpenNoSuchMessageException("greeting", Locale.JAPANESE);

            assertThat(ex.locale()).isEqualTo(Locale.JAPANESE);
        }
    }

    @Nested
    @DisplayName("messageNotFound工厂方法测试")
    class MessageNotFoundTests {

        @Test
        @DisplayName("创建消息未找到异常")
        void testMessageNotFound() {
            OpenNoSuchMessageException ex = OpenNoSuchMessageException.messageNotFound("missing.key", Locale.FRENCH);

            assertThat(ex).isNotNull();
            assertThat(ex.key()).isEqualTo("missing.key");
            assertThat(ex.locale()).isEqualTo(Locale.FRENCH);
        }
    }

    @Nested
    @DisplayName("formatFailed工厂方法测试")
    class FormatFailedTests {

        @Test
        @DisplayName("创建格式化失败异常")
        void testFormatFailed() {
            IllegalArgumentException cause = new IllegalArgumentException("bad format");
            OpenNoSuchMessageException ex = OpenNoSuchMessageException.formatFailed("template.key", Locale.GERMAN, cause);

            assertThat(ex).isNotNull();
            assertThat(ex.key()).isEqualTo("template.key");
            assertThat(ex.locale()).isEqualTo(Locale.GERMAN);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("bundleLoadFailed工厂方法测试")
    class BundleLoadFailedTests {

        @Test
        @DisplayName("创建资源包加载失败异常")
        void testBundleLoadFailed() {
            RuntimeException cause = new RuntimeException("IO error");
            OpenException ex = OpenNoSuchMessageException.bundleLoadFailed("i18n/messages", Locale.ITALIAN, cause);

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("i18n/messages");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            OpenNoSuchMessageException ex = new OpenNoSuchMessageException("key", Locale.ROOT);

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            OpenNoSuchMessageException ex = new OpenNoSuchMessageException("key", Locale.ROOT);

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
