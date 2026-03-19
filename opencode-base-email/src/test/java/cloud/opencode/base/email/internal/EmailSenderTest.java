package cloud.opencode.base.email.internal;

import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.SendResult;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailSenderTest Tests
 * EmailSenderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailSender 接口测试")
class EmailSenderTest {

    @Nested
    @DisplayName("send方法测试")
    class SendTests {

        @Test
        @DisplayName("send发送邮件")
        void testSend() {
            List<Email> sent = new ArrayList<>();
            EmailSender sender = sent::add;
            Email email = Email.builder()
                    .from("test@example.com")
                    .to("recv@example.com")
                    .subject("Test")
                    .text("Hello")
                    .build();
            sender.send(email);
            assertThat(sent).hasSize(1);
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("sendWithResult调用send并返回成功结果")
        void testSendWithResult() {
            EmailSender sender = email -> {};
            Email email = Email.builder()
                    .from("test@example.com")
                    .to("recv@example.com")
                    .subject("Test")
                    .text("Hello")
                    .build();
            SendResult result = sender.sendWithResult(email);
            assertThat(result).isNotNull();
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("close默认不抛出异常")
        void testDefaultClose() {
            EmailSender sender = email -> {};
            assertThatNoException().isThrownBy(sender::close);
        }
    }
}
