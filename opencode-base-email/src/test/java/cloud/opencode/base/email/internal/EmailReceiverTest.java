package cloud.opencode.base.email.internal;

import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.query.EmailQuery;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailReceiverTest Tests
 * EmailReceiverTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailReceiver 接口测试")
class EmailReceiverTest {

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("close默认调用disconnect")
        void testCloseCallsDisconnect() {
            var state = new Object() { boolean disconnected = false; };
            EmailReceiver receiver = createMockReceiver(() -> state.disconnected = true);
            receiver.close();
            assertThat(state.disconnected).isTrue();
        }
    }

    @Nested
    @DisplayName("接口继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承AutoCloseable")
        void testExtendsAutoCloseable() {
            assertThat(AutoCloseable.class.isAssignableFrom(EmailReceiver.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("接口方法签名测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("接口定义所有必要方法")
        void testInterfaceHasMethods() throws NoSuchMethodException {
            assertThat(EmailReceiver.class.getMethod("receiveUnread")).isNotNull();
            assertThat(EmailReceiver.class.getMethod("receive", EmailQuery.class)).isNotNull();
            assertThat(EmailReceiver.class.getMethod("receiveAll")).isNotNull();
            assertThat(EmailReceiver.class.getMethod("receiveById", String.class)).isNotNull();
            assertThat(EmailReceiver.class.getMethod("getMessageCount", String.class)).isNotNull();
            assertThat(EmailReceiver.class.getMethod("getUnreadCount", String.class)).isNotNull();
            assertThat(EmailReceiver.class.getMethod("markAsRead", String.class)).isNotNull();
            assertThat(EmailReceiver.class.getMethod("markAsUnread", String.class)).isNotNull();
            assertThat(EmailReceiver.class.getMethod("delete", String.class)).isNotNull();
            assertThat(EmailReceiver.class.getMethod("listFolders")).isNotNull();
            assertThat(EmailReceiver.class.getMethod("isConnected")).isNotNull();
            assertThat(EmailReceiver.class.getMethod("connect")).isNotNull();
            assertThat(EmailReceiver.class.getMethod("disconnect")).isNotNull();
        }
    }

    private EmailReceiver createMockReceiver(Runnable onDisconnect) {
        return new EmailReceiver() {
            @Override public List<ReceivedEmail> receiveUnread() { return List.of(); }
            @Override public List<ReceivedEmail> receive(EmailQuery query) { return List.of(); }
            @Override public ReceivedEmail receiveById(String messageId) { return null; }
            @Override public int getMessageCount(String folder) { return 0; }
            @Override public int getUnreadCount(String folder) { return 0; }
            @Override public void markAsRead(String messageId) {}
            @Override public void markAsUnread(String messageId) {}
            @Override public void setFlagged(String messageId, boolean flagged) {}
            @Override public void delete(String messageId) {}
            @Override public void moveToFolder(String messageId, String targetFolder) {}
            @Override public List<String> listFolders() { return List.of(); }
            @Override public boolean isConnected() { return false; }
            @Override public void connect() {}
            @Override public void disconnect() { onDisconnect.run(); }
        };
    }
}
