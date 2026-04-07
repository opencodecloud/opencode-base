package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.query.EmailQuery;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * Pop3EmailReceiver Integration Test
 * Pop3EmailReceiver 集成测试
 *
 * <p>Uses a mock POP3 server (plain-text ServerSocket) to test full receive flows.</p>
 * <p>使用模拟POP3服务器（纯文本ServerSocket）测试完整接收流程。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("Pop3EmailReceiver Integration Tests")
class Pop3EmailReceiverIntegrationTest {

    // Two test messages in raw MIME format
    static final String MSG1 = "From: sender@test.com\r\n"
            + "To: me@test.com\r\n"
            + "Subject: Test1\r\n"
            + "Message-ID: <msg1@test>\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + "Hello 1";

    static final String MSG2 = "From: other@test.com\r\n"
            + "To: me@test.com\r\n"
            + "Subject: Test2\r\n"
            + "Message-ID: <msg2@test>\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + "Hello 2";

    // Header-only versions (for TOP command, 0 body lines)
    static final String MSG1_HEADERS = "From: sender@test.com\r\n"
            + "To: me@test.com\r\n"
            + "Subject: Test1\r\n"
            + "Message-ID: <msg1@test>\r\n"
            + "Content-Type: text/plain";

    static final String MSG2_HEADERS = "From: other@test.com\r\n"
            + "To: me@test.com\r\n"
            + "Subject: Test2\r\n"
            + "Message-ID: <msg2@test>\r\n"
            + "Content-Type: text/plain";

    /**
     * Simple mock POP3 server that responds to basic POP3 commands.
     */
    static final class MockPop3Server implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final int port;
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final List<Thread> clientThreads = new CopyOnWriteArrayList<>();
        private final List<String> messages;
        private final List<String> headerOnlyMessages;
        private volatile boolean deleteReceived = false;
        private volatile int deletedMsgNum = -1;

        MockPop3Server(List<String> messages, List<String> headerOnlyMessages) throws IOException {
            this.serverSocket = new ServerSocket(0); // random free port
            this.port = serverSocket.getLocalPort();
            this.messages = messages;
            this.headerOnlyMessages = headerOnlyMessages;
        }

        int getPort() {
            return port;
        }

        boolean wasDeleteReceived() {
            return deleteReceived;
        }

        int getDeletedMsgNum() {
            return deletedMsgNum;
        }

        void start() {
            Thread acceptThread = Thread.ofVirtual().name("mock-pop3-accept").start(() -> {
                while (running.get()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        Thread clientThread = Thread.ofVirtual()
                                .name("mock-pop3-client")
                                .start(() -> handleClient(clientSocket));
                        clientThreads.add(clientThread);
                    } catch (IOException e) {
                        if (running.get()) {
                            // unexpected
                        }
                    }
                }
            });
            clientThreads.add(acceptThread);
        }

        private void handleClient(Socket clientSocket) {
            try (clientSocket;
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                 OutputStream out = clientSocket.getOutputStream()) {

                // Send greeting
                writeLine(out, "+OK Mock POP3 Server ready");

                String line;
                while ((line = reader.readLine()) != null) {
                    String upper = line.toUpperCase();

                    if (upper.startsWith("USER ")) {
                        writeLine(out, "+OK User accepted");

                    } else if (upper.startsWith("PASS ")) {
                        writeLine(out, "+OK Pass accepted");

                    } else if (upper.equals("STAT")) {
                        int totalSize = 0;
                        for (String msg : messages) {
                            totalSize += msg.length();
                        }
                        writeLine(out, "+OK " + messages.size() + " " + totalSize);

                    } else if (upper.startsWith("TOP ")) {
                        // TOP msgNum lines
                        String[] parts = line.split("\\s+");
                        int msgNum = Integer.parseInt(parts[1]);
                        if (msgNum >= 1 && msgNum <= headerOnlyMessages.size()) {
                            writeLine(out, "+OK");
                            // Send headers line by line, then terminator
                            String headers = headerOnlyMessages.get(msgNum - 1);
                            for (String headerLine : headers.split("\r\n")) {
                                writeLine(out, headerLine);
                            }
                            writeLine(out, ".");
                        } else {
                            writeLine(out, "-ERR No such message");
                        }

                    } else if (upper.startsWith("RETR ")) {
                        String[] parts = line.split("\\s+");
                        int msgNum = Integer.parseInt(parts[1]);
                        if (msgNum >= 1 && msgNum <= messages.size()) {
                            writeLine(out, "+OK " + messages.get(msgNum - 1).length() + " octets");
                            // Send message line by line, then terminator
                            String msg = messages.get(msgNum - 1);
                            for (String msgLine : msg.split("\r\n")) {
                                // Byte-stuffing: lines starting with "." get an extra "."
                                if (msgLine.startsWith(".")) {
                                    writeLine(out, "." + msgLine);
                                } else {
                                    writeLine(out, msgLine);
                                }
                            }
                            writeLine(out, ".");
                        } else {
                            writeLine(out, "-ERR No such message");
                        }

                    } else if (upper.startsWith("DELE ")) {
                        String[] parts = line.split("\\s+");
                        int msgNum = Integer.parseInt(parts[1]);
                        if (msgNum >= 1 && msgNum <= messages.size()) {
                            deleteReceived = true;
                            deletedMsgNum = msgNum;
                            writeLine(out, "+OK Message " + msgNum + " deleted");
                        } else {
                            writeLine(out, "-ERR No such message");
                        }

                    } else if (upper.equals("QUIT")) {
                        writeLine(out, "+OK Bye");
                        break;

                    } else if (upper.equals("NOOP")) {
                        writeLine(out, "+OK");

                    } else if (upper.equals("RSET")) {
                        writeLine(out, "+OK");

                    } else {
                        writeLine(out, "-ERR Unknown command");
                    }
                }
            } catch (IOException e) {
                // Client disconnected
            }
        }

        private void writeLine(OutputStream out, String line) throws IOException {
            out.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        @Override
        public void close() {
            running.set(false);
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
            for (Thread t : clientThreads) {
                t.interrupt();
            }
        }
    }

    private MockPop3Server server;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockPop3Server(
                List.of(MSG1, MSG2),
                List.of(MSG1_HEADERS, MSG2_HEADERS)
        );
        port = server.getPort();
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.close();
        }
    }

    private EmailReceiveConfig createConfig() {
        return EmailReceiveConfig.builder()
                .host("localhost")
                .port(port)
                .username("user@test.com")
                .password("password")
                .pop3()
                .ssl(false)
                .connectionTimeout(Duration.ofSeconds(5))
                .timeout(Duration.ofSeconds(5))
                .build();
    }

    @Nested
    @DisplayName("ConnectAndReceiveTests")
    class ConnectAndReceiveTests {

        @Test
        @DisplayName("Connect and receive all messages")
        void testConnectAndReceiveAll() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                assertThat(receiver.isConnected()).isTrue();

                List<ReceivedEmail> emails = receiver.receiveAll();

                assertThat(emails).hasSize(2);

                ReceivedEmail first = emails.get(0);
                assertThat(first.from()).isEqualTo("sender@test.com");
                assertThat(first.subject()).isEqualTo("Test1");
                assertThat(first.messageId()).isEqualTo("msg1@test");
                assertThat(first.textContent()).isEqualTo("Hello 1");
                assertThat(first.folder()).isEqualTo("INBOX");
                assertThat(first.to()).contains("me@test.com");

                ReceivedEmail second = emails.get(1);
                assertThat(second.from()).isEqualTo("other@test.com");
                assertThat(second.subject()).isEqualTo("Test2");
                assertThat(second.messageId()).isEqualTo("msg2@test");
                assertThat(second.textContent()).isEqualTo("Hello 2");
            }
        }

        @Test
        @DisplayName("getMessageCount returns correct count")
        void testGetMessageCount() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                int count = receiver.getMessageCount("INBOX");

                assertThat(count).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("getUnreadCount same as message count for POP3")
        void testGetUnreadCount() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                int unread = receiver.getUnreadCount("INBOX");

                assertThat(unread).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("receiveUnread returns messages")
        void testReceiveUnread() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                List<ReceivedEmail> emails = receiver.receiveUnread();

                assertThat(emails).isNotEmpty();
                assertThat(emails.size()).isLessThanOrEqualTo(config.maxMessages());
            }
        }
    }

    @Nested
    @DisplayName("ReceiveByIdTests")
    class ReceiveByIdTests {

        @Test
        @DisplayName("receiveById finds message by Message-ID")
        void testReceiveById() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                ReceivedEmail email = receiver.receiveById("msg1@test");

                assertThat(email).isNotNull();
                assertThat(email.messageId()).isEqualTo("msg1@test");
                assertThat(email.from()).isEqualTo("sender@test.com");
                assertThat(email.subject()).isEqualTo("Test1");
                assertThat(email.textContent()).isEqualTo("Hello 1");
            }
        }

        @Test
        @DisplayName("receiveById for second message")
        void testReceiveByIdSecondMessage() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                ReceivedEmail email = receiver.receiveById("msg2@test");

                assertThat(email).isNotNull();
                assertThat(email.messageId()).isEqualTo("msg2@test");
                assertThat(email.subject()).isEqualTo("Test2");
            }
        }

        @Test
        @DisplayName("receiveById returns null for non-existent ID")
        void testReceiveByIdNotFound() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                ReceivedEmail email = receiver.receiveById("nonexistent@test");

                assertThat(email).isNull();
            }
        }

        @Test
        @DisplayName("receiveById returns null for null ID")
        void testReceiveByIdNull() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                ReceivedEmail email = receiver.receiveById(null);

                assertThat(email).isNull();
            }
        }

        @Test
        @DisplayName("receiveById returns null for blank ID")
        void testReceiveByIdBlank() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                ReceivedEmail email = receiver.receiveById("   ");

                assertThat(email).isNull();
            }
        }
    }

    @Nested
    @DisplayName("DeleteTests")
    class DeleteTests {

        @Test
        @DisplayName("delete by message ID sends DELE")
        void testDelete() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                receiver.delete("msg1@test");

                assertThat(server.wasDeleteReceived()).isTrue();
                assertThat(server.getDeletedMsgNum()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("delete second message by ID")
        void testDeleteSecondMessage() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                receiver.delete("msg2@test");

                assertThat(server.wasDeleteReceived()).isTrue();
                assertThat(server.getDeletedMsgNum()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("delete non-existent message throws exception")
        void testDeleteNotFound() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                assertThatThrownBy(() -> receiver.delete("nonexistent@test"))
                        .isInstanceOf(EmailReceiveException.class);
            }
        }
    }

    @Nested
    @DisplayName("ReceiveWithQueryTests")
    class ReceiveWithQueryTests {

        @Test
        @DisplayName("Query with limit returns at most limit messages")
        void testQueryWithLimit() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                EmailQuery query = EmailQuery.builder()
                        .limit(1)
                        .build();

                List<ReceivedEmail> emails = receiver.receive(query);

                assertThat(emails).hasSize(1);
            }
        }

        @Test
        @DisplayName("Query with subject filter matches")
        void testQueryWithSubjectFilter() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                EmailQuery query = EmailQuery.builder()
                        .subjectContains("Test1")
                        .build();

                List<ReceivedEmail> emails = receiver.receive(query);

                assertThat(emails).hasSize(1);
                assertThat(emails.getFirst().subject()).isEqualTo("Test1");
            }
        }

        @Test
        @DisplayName("Query with subject filter no match returns empty")
        void testQueryWithSubjectFilterNoMatch() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                EmailQuery query = EmailQuery.builder()
                        .subjectContains("NonExistentSubject")
                        .build();

                List<ReceivedEmail> emails = receiver.receive(query);

                assertThat(emails).isEmpty();
            }
        }

        @Test
        @DisplayName("Query with from filter")
        void testQueryWithFromFilter() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                EmailQuery query = EmailQuery.builder()
                        .from("sender@test.com")
                        .build();

                List<ReceivedEmail> emails = receiver.receive(query);

                assertThat(emails).hasSize(1);
                assertThat(emails.getFirst().from()).isEqualTo("sender@test.com");
            }
        }

        @Test
        @DisplayName("Query with offset skips messages")
        void testQueryWithOffset() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                EmailQuery query = EmailQuery.builder()
                        .offset(1)
                        .limit(10)
                        .build();

                List<ReceivedEmail> emails = receiver.receive(query);

                // 2 total messages, skip 1 = 1 remaining
                assertThat(emails).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("DisconnectTests")
    class DisconnectTests {

        @Test
        @DisplayName("connect then disconnect sets isConnected to false")
        void testConnectDisconnect() {
            EmailReceiveConfig config = createConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            receiver.connect();
            assertThat(receiver.isConnected()).isTrue();

            receiver.disconnect();
            assertThat(receiver.isConnected()).isFalse();
        }

        @Test
        @DisplayName("double disconnect does not throw")
        void testDoubleDisconnect() {
            EmailReceiveConfig config = createConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);
            receiver.connect();

            assertThatNoException().isThrownBy(() -> {
                receiver.disconnect();
                receiver.disconnect();
            });
        }

        @Test
        @DisplayName("close() also disconnects")
        void testCloseDisconnects() {
            EmailReceiveConfig config = createConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);
            receiver.connect();

            receiver.close();

            assertThat(receiver.isConnected()).isFalse();
        }
    }

    @Nested
    @DisplayName("AutoReconnectTests")
    class AutoReconnectTests {

        @Test
        @DisplayName("ensureConnected auto-connects when needed")
        void testAutoConnect() {
            EmailReceiveConfig config = createConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            // Do not call connect() - receiveAll should auto-connect
            List<ReceivedEmail> emails = receiver.receiveAll();

            assertThat(emails).hasSize(2);
            assertThat(receiver.isConnected()).isTrue();

            receiver.disconnect();
        }
    }

    @Nested
    @DisplayName("ListFoldersTests")
    class ListFoldersTests {

        @Test
        @DisplayName("POP3 returns only INBOX")
        void testListFolders() {
            EmailReceiveConfig config = createConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            List<String> folders = receiver.listFolders();

            assertThat(folders).containsExactly("INBOX");
        }
    }

    @Nested
    @DisplayName("MoveToFolderTests")
    class MoveToFolderTests {

        @Test
        @DisplayName("moveToFolder throws for POP3")
        void testMoveNotSupported() {
            EmailReceiveConfig config = createConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            assertThatThrownBy(() -> receiver.moveToFolder("msg1@test", "Archive"))
                    .isInstanceOf(EmailReceiveException.class)
                    .hasMessageContaining("not supported by POP3");
        }
    }

    @Nested
    @DisplayName("FlagOperationsTests")
    class FlagOperationsTests {

        @Test
        @DisplayName("Flag operations are no-ops for POP3")
        void testFlagOperationsNoOp() {
            EmailReceiveConfig config = createConfig();
            Pop3EmailReceiver receiver = new Pop3EmailReceiver(config);

            assertThatNoException().isThrownBy(() -> {
                receiver.markAsRead("msg1@test");
                receiver.markAsUnread("msg1@test");
                receiver.setFlagged("msg1@test", true);
                receiver.setFlagged("msg1@test", false);
            });
        }
    }

    @Nested
    @DisplayName("ReceivedEmailFieldsTests")
    class ReceivedEmailFieldsTests {

        @Test
        @DisplayName("Received email fields are correctly populated")
        void testReceivedEmailFields() {
            EmailReceiveConfig config = createConfig();
            try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
                receiver.connect();

                List<ReceivedEmail> emails = receiver.receiveAll();
                ReceivedEmail email = emails.getFirst();

                assertThat(email.messageId()).isEqualTo("msg1@test");
                assertThat(email.from()).isEqualTo("sender@test.com");
                assertThat(email.to()).contains("me@test.com");
                assertThat(email.subject()).isEqualTo("Test1");
                assertThat(email.textContent()).isEqualTo("Hello 1");
                assertThat(email.folder()).isEqualTo("INBOX");
                assertThat(email.messageNumber()).isEqualTo(1);
                assertThat(email.isUnread()).isTrue();
                assertThat(email.hasTextContent()).isTrue();
            }
        }
    }
}
