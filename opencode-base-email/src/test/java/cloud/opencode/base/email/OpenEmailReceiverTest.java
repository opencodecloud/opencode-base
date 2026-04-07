package cloud.opencode.base.email;

import cloud.opencode.base.email.exception.EmailException;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.internal.EmailReceiver;
import cloud.opencode.base.email.listener.EmailIdleMonitor;
import cloud.opencode.base.email.listener.EmailListener;
import cloud.opencode.base.email.query.EmailQuery;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for OpenEmail receiver-side methods using a mock EmailReceiver.
 * OpenEmail 接收端方法测试，使用模拟 EmailReceiver。
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("OpenEmail Receiver Tests")
class OpenEmailReceiverTest {

    @AfterEach
    void reset() {
        OpenEmail.disableRateLimiting();
        OpenEmail.shutdown();
    }

    // ========================================================================
    // Mock EmailReceiver
    // ========================================================================

    /**
     * In-memory EmailReceiver for testing receiver-side OpenEmail methods.
     */
    static final class InMemoryEmailReceiver implements EmailReceiver {
        private boolean connected = false;
        private final List<ReceivedEmail> emails = new CopyOnWriteArrayList<>();
        private final List<String> markedRead = new CopyOnWriteArrayList<>();
        private final List<String> markedUnread = new CopyOnWriteArrayList<>();
        private final List<String> deleted = new CopyOnWriteArrayList<>();
        private final List<String> flagged = new CopyOnWriteArrayList<>();
        private final List<String> unflagged = new CopyOnWriteArrayList<>();
        private final List<String[]> moved = new CopyOnWriteArrayList<>();
        private final List<String> folders = new CopyOnWriteArrayList<>(
                List.of("INBOX", "Sent", "Drafts", "Trash"));

        InMemoryEmailReceiver() {
            // Pre-populate with sample emails
            emails.add(ReceivedEmail.builder()
                    .messageId("<msg1@test.com>")
                    .from("alice@test.com")
                    .fromName("Alice")
                    .to(List.of("me@test.com"))
                    .subject("Test Email 1")
                    .textContent("Hello World 1")
                    .sentDate(Instant.now().minusSeconds(3600))
                    .receivedDate(Instant.now().minusSeconds(3500))
                    .flags(EmailFlags.UNREAD)
                    .folder("INBOX")
                    .messageNumber(1)
                    .size(1024)
                    .build());
            emails.add(ReceivedEmail.builder()
                    .messageId("<msg2@test.com>")
                    .from("bob@test.com")
                    .fromName("Bob")
                    .to(List.of("me@test.com"))
                    .subject("Test Email 2")
                    .textContent("Hello World 2")
                    .sentDate(Instant.now().minusSeconds(1800))
                    .receivedDate(Instant.now().minusSeconds(1700))
                    .flags(EmailFlags.UNREAD)
                    .folder("INBOX")
                    .messageNumber(2)
                    .size(2048)
                    .build());
        }

        @Override
        public List<ReceivedEmail> receiveUnread() {
            return List.copyOf(emails.stream()
                    .filter(e -> e.flags() != null && e.flags().isUnread())
                    .toList());
        }

        @Override
        public List<ReceivedEmail> receive(EmailQuery query) {
            var result = new ArrayList<>(emails);
            if (query.limit() > 0 && result.size() > query.limit()) {
                result = new ArrayList<>(result.subList(0, query.limit()));
            }
            return List.copyOf(result);
        }

        @Override
        public ReceivedEmail receiveById(String messageId) {
            return emails.stream()
                    .filter(e -> messageId.equals(e.messageId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public int getMessageCount(String folder) {
            return (int) emails.stream()
                    .filter(e -> folder.equals(e.folder()))
                    .count();
        }

        @Override
        public int getUnreadCount(String folder) {
            return (int) emails.stream()
                    .filter(e -> folder.equals(e.folder()))
                    .filter(e -> e.flags() != null && e.flags().isUnread())
                    .count();
        }

        @Override
        public void markAsRead(String messageId) {
            markedRead.add(messageId);
        }

        @Override
        public void markAsUnread(String messageId) {
            markedUnread.add(messageId);
        }

        @Override
        public void setFlagged(String messageId, boolean flaggedVal) {
            if (flaggedVal) {
                flagged.add(messageId);
            } else {
                unflagged.add(messageId);
            }
        }

        @Override
        public void delete(String messageId) {
            deleted.add(messageId);
        }

        @Override
        public void moveToFolder(String messageId, String targetFolder) {
            moved.add(new String[]{messageId, targetFolder});
        }

        @Override
        public List<String> listFolders() {
            return List.copyOf(folders);
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public void connect() {
            connected = true;
        }

        @Override
        public void disconnect() {
            connected = false;
        }
    }

    private static EmailReceiveConfig imapConfig() {
        return EmailReceiveConfig.builder()
                .host("imap.test.local")
                .username("user@test.local")
                .password("secret")
                .imap()
                .ssl(false)
                .build();
    }

    private static EmailReceiveConfig pop3Config() {
        return EmailReceiveConfig.builder()
                .host("pop.test.local")
                .username("user@test.local")
                .password("secret")
                .pop3()
                .ssl(false)
                .build();
    }

    private InMemoryEmailReceiver configureReceiver() {
        InMemoryEmailReceiver receiver = new InMemoryEmailReceiver();
        OpenEmail.configureReceiver(imapConfig(), receiver);
        return receiver;
    }

    // ========================================================================
    // Tests
    // ========================================================================

    @Nested
    @DisplayName("ConfigureReceiverTests")
    class ConfigureReceiverTests {

        @Test
        @DisplayName("configureReceiver(config, customReceiver) sets up receiver")
        void testConfigureWithCustomReceiver() {
            InMemoryEmailReceiver receiver = new InMemoryEmailReceiver();
            EmailReceiveConfig config = imapConfig();

            OpenEmail.configureReceiver(config, receiver);

            assertThat(OpenEmail.isReceiverConfigured()).isTrue();
            assertThat(OpenEmail.getReceiveConfig()).isSameAs(config);
        }

        @Test
        @DisplayName("configureReceiver(host, user, pass, true) quick IMAP config")
        void testQuickImapConfig() {
            OpenEmail.configureReceiver("imap.quick.local", "admin", "pw", true);

            assertThat(OpenEmail.isReceiverConfigured()).isTrue();
            assertThat(OpenEmail.getReceiveConfig().host()).isEqualTo("imap.quick.local");
            assertThat(OpenEmail.getReceiveConfig().isImap()).isTrue();
            assertThat(OpenEmail.getReceiveConfig().ssl()).isTrue();
        }

        @Test
        @DisplayName("configureReceiver(host, user, pass, false) quick POP3 config")
        void testQuickPop3Config() {
            OpenEmail.configureReceiver("pop.quick.local", "admin", "pw", false);

            assertThat(OpenEmail.isReceiverConfigured()).isTrue();
            assertThat(OpenEmail.getReceiveConfig().isPop3()).isTrue();
        }

        @Test
        @DisplayName("isReceiverConfigured() before configure returns false")
        void testIsReceiverConfiguredBeforeConfigure() {
            assertThat(OpenEmail.isReceiverConfigured()).isFalse();
        }

        @Test
        @DisplayName("getReceiveConfig() returns null before configure")
        void testGetReceiveConfigBeforeConfigure() {
            assertThat(OpenEmail.getReceiveConfig()).isNull();
        }
    }

    @Nested
    @DisplayName("ReceiveTests")
    class ReceiveTests {

        private InMemoryEmailReceiver receiver;

        @BeforeEach
        void setUp() {
            receiver = configureReceiver();
        }

        @Test
        @DisplayName("receiveUnread() returns unread emails")
        void testReceiveUnread() {
            List<ReceivedEmail> emails = OpenEmail.receiveUnread();

            assertThat(emails).hasSize(2);
            assertThat(emails).allSatisfy(e ->
                    assertThat(e.flags().isUnread()).isTrue());
        }

        @Test
        @DisplayName("receive(query) returns emails matching query")
        void testReceiveWithQuery() {
            EmailQuery query = EmailQuery.builder().limit(1).build();

            List<ReceivedEmail> emails = OpenEmail.receive(query);

            assertThat(emails).hasSize(1);
        }

        @Test
        @DisplayName("receiveAll() returns all emails")
        void testReceiveAll() {
            List<ReceivedEmail> emails = OpenEmail.receiveAll();

            assertThat(emails).hasSize(2);
        }

        @Test
        @DisplayName("receiveById() returns matching email")
        void testReceiveById() {
            ReceivedEmail email = OpenEmail.receiveById("<msg1@test.com>");

            assertThat(email).isNotNull();
            assertThat(email.messageId()).isEqualTo("<msg1@test.com>");
            assertThat(email.subject()).isEqualTo("Test Email 1");
        }

        @Test
        @DisplayName("receiveById() returns null for unknown ID")
        void testReceiveByIdNotFound() {
            ReceivedEmail email = OpenEmail.receiveById("<nonexistent@test.com>");

            assertThat(email).isNull();
        }

        @Test
        @DisplayName("getMessageCount() returns correct count")
        void testGetMessageCount() {
            int count = OpenEmail.getMessageCount("INBOX");

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("getUnreadCount() returns correct count")
        void testGetUnreadCount() {
            int count = OpenEmail.getUnreadCount("INBOX");

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("getMessageCount() returns 0 for empty folder")
        void testGetMessageCountEmptyFolder() {
            int count = OpenEmail.getMessageCount("Sent");

            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("EmailManagementTests")
    class EmailManagementTests {

        private InMemoryEmailReceiver receiver;

        @BeforeEach
        void setUp() {
            receiver = configureReceiver();
        }

        @Test
        @DisplayName("markAsRead() delegates to receiver")
        void testMarkAsRead() {
            OpenEmail.markAsRead("<msg1@test.com>");

            assertThat(receiver.markedRead).containsExactly("<msg1@test.com>");
        }

        @Test
        @DisplayName("markAsUnread() delegates to receiver")
        void testMarkAsUnread() {
            OpenEmail.markAsUnread("<msg1@test.com>");

            assertThat(receiver.markedUnread).containsExactly("<msg1@test.com>");
        }

        @Test
        @DisplayName("setFlagged(true) delegates to receiver")
        void testSetFlaggedTrue() {
            OpenEmail.setFlagged("<msg1@test.com>", true);

            assertThat(receiver.flagged).containsExactly("<msg1@test.com>");
        }

        @Test
        @DisplayName("setFlagged(false) delegates to receiver")
        void testSetFlaggedFalse() {
            OpenEmail.setFlagged("<msg1@test.com>", false);

            assertThat(receiver.unflagged).containsExactly("<msg1@test.com>");
        }

        @Test
        @DisplayName("delete() delegates to receiver")
        void testDelete() {
            OpenEmail.delete("<msg1@test.com>");

            assertThat(receiver.deleted).containsExactly("<msg1@test.com>");
        }

        @Test
        @DisplayName("moveToFolder() delegates to receiver")
        void testMoveToFolder() {
            OpenEmail.moveToFolder("<msg1@test.com>", "Archive");

            assertThat(receiver.moved).hasSize(1);
            assertThat(receiver.moved.getFirst()[0]).isEqualTo("<msg1@test.com>");
            assertThat(receiver.moved.getFirst()[1]).isEqualTo("Archive");
        }

        @Test
        @DisplayName("listFolders() returns folder list")
        void testListFolders() {
            List<String> folders = OpenEmail.listFolders();

            assertThat(folders).containsExactly("INBOX", "Sent", "Drafts", "Trash");
        }
    }

    @Nested
    @DisplayName("AsyncReceiveTests")
    class AsyncReceiveTests {

        @BeforeEach
        void setUp() {
            configureReceiver();
        }

        @Test
        @DisplayName("receiveUnreadAsync() completes with emails")
        void testReceiveUnreadAsync() {
            CompletableFuture<List<ReceivedEmail>> future = OpenEmail.receiveUnreadAsync();

            assertThat(future).succeedsWithin(Duration.ofSeconds(5));
            List<ReceivedEmail> emails = future.join();
            assertThat(emails).hasSize(2);
        }

        @Test
        @DisplayName("receiveAsync(query) completes with limited emails")
        void testReceiveAsyncWithQuery() {
            EmailQuery query = EmailQuery.builder().limit(1).build();

            CompletableFuture<List<ReceivedEmail>> future = OpenEmail.receiveAsync(query);

            assertThat(future).succeedsWithin(Duration.ofSeconds(5));
            List<ReceivedEmail> emails = future.join();
            assertThat(emails).hasSize(1);
        }

        @Test
        @DisplayName("receiveAsync(query, callback) invokes callback")
        void testReceiveAsyncWithCallback() throws Exception {
            EmailQuery query = EmailQuery.builder().build();
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

            CompletableFuture<List<ReceivedEmail>> future = OpenEmail.receiveAsync(query,
                    (emails, error) -> {
                        assertThat(error).isNull();
                        assertThat(emails).isNotEmpty();
                        latch.countDown();
                    });

            assertThat(future).succeedsWithin(Duration.ofSeconds(5));
            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        }

        @Test
        @DisplayName("markAsReadAsync() completes successfully")
        void testMarkAsReadAsync() {
            CompletableFuture<Void> future = OpenEmail.markAsReadAsync("<msg1@test.com>");

            assertThat(future).succeedsWithin(Duration.ofSeconds(5));
        }

        @Test
        @DisplayName("deleteAsync() completes successfully")
        void testDeleteAsync() {
            CompletableFuture<Void> future = OpenEmail.deleteAsync("<msg1@test.com>");

            assertThat(future).succeedsWithin(Duration.ofSeconds(5));
        }
    }

    @Nested
    @DisplayName("MonitorTests")
    class MonitorTests {

        @BeforeEach
        void setUp() {
            OpenEmail.configureReceiver(imapConfig(), new InMemoryEmailReceiver());
        }

        @Test
        @DisplayName("createMonitor(listener) returns configured monitor")
        void testCreateMonitor() {
            EmailIdleMonitor monitor = OpenEmail.createMonitor(email -> {});

            assertThat(monitor).isNotNull();
            assertThat(monitor.isRunning()).isFalse();
        }

        @Test
        @DisplayName("createMonitor(folder, listener) returns configured monitor")
        void testCreateMonitorWithFolder() {
            EmailIdleMonitor monitor = OpenEmail.createMonitor("Sent", email -> {});

            assertThat(monitor).isNotNull();
            assertThat(monitor.isRunning()).isFalse();
        }

        @Test
        @DisplayName("createMonitor without receiver config throws")
        void testCreateMonitorWithoutConfig() {
            OpenEmail.shutdownReceiver();

            assertThatThrownBy(() -> OpenEmail.createMonitor(email -> {}))
                    .isInstanceOf(EmailReceiveException.class)
                    .hasMessageContaining("not configured");
        }
    }

    @Nested
    @DisplayName("NotConfiguredTests")
    class NotConfiguredTests {

        @Test
        @DisplayName("receiveUnread() without config throws EmailReceiveException")
        void testReceiveUnreadWithoutConfig() {
            assertThatThrownBy(OpenEmail::receiveUnread)
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("markAsRead() without config throws EmailReceiveException")
        void testMarkAsReadWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.markAsRead("<msg@test.com>"))
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("delete() without config throws EmailReceiveException")
        void testDeleteWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.delete("<msg@test.com>"))
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("listFolders() without config throws EmailReceiveException")
        void testListFoldersWithoutConfig() {
            assertThatThrownBy(OpenEmail::listFolders)
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("receiveUnreadAsync() without config throws EmailReceiveException")
        void testReceiveUnreadAsyncWithoutConfig() {
            assertThatThrownBy(OpenEmail::receiveUnreadAsync)
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("moveToFolder() without config throws EmailReceiveException")
        void testMoveToFolderWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.moveToFolder("<msg@test.com>", "Archive"))
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("setFlagged() without config throws EmailReceiveException")
        void testSetFlaggedWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.setFlagged("<msg@test.com>", true))
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("markAsUnread() without config throws EmailReceiveException")
        void testMarkAsUnreadWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.markAsUnread("<msg@test.com>"))
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("getMessageCount() without config throws EmailReceiveException")
        void testGetMessageCountWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.getMessageCount("INBOX"))
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("getUnreadCount() without config throws EmailReceiveException")
        void testGetUnreadCountWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.getUnreadCount("INBOX"))
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("receiveById() without config throws EmailReceiveException")
        void testReceiveByIdWithoutConfig() {
            assertThatThrownBy(() -> OpenEmail.receiveById("<msg@test.com>"))
                    .isInstanceOf(EmailReceiveException.class);
        }

        @Test
        @DisplayName("receiveAll() without config throws EmailReceiveException")
        void testReceiveAllWithoutConfig() {
            assertThatThrownBy(OpenEmail::receiveAll)
                    .isInstanceOf(EmailReceiveException.class);
        }
    }

    @Nested
    @DisplayName("SendAsyncCallbackTests")
    class SendAsyncCallbackTests {

        @Test
        @DisplayName("sendAsync with callback invokes callback on success")
        void testSendAsyncWithCallback() throws Exception {
            // Set up sender
            var sender = new OpenEmailIntegrationTest.InMemoryEmailSender();
            OpenEmail.configure(EmailConfig.builder()
                    .host("smtp.test.local")
                    .port(587)
                    .username("user")
                    .password("pass")
                    .defaultFrom("noreply@test.local")
                    .build(), sender);

            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            Email email = Email.builder()
                    .to("test@example.com")
                    .subject("Test")
                    .text("content")
                    .build();

            OpenEmail.sendAsync(email, (e, error) -> {
                assertThat(error).isNull();
                latch.countDown();
            });

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        }
    }
}
