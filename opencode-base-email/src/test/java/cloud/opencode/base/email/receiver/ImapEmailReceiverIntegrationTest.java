package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.query.EmailQuery;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ImapEmailReceiver using a mock IMAP server.
 * ImapEmailReceiver 集成测试，使用模拟 IMAP 服务器。
 *
 * <p>Each test spins up a minimal mock IMAP server on a random local port
 * and exercises the receiver through the real TCP/IMAP protocol path.</p>
 *
 * <p>Note: All multi-message tests use non-contiguous sequence numbers
 * (e.g., 1, 3) to exercise the {@code fetchIndividually} path, which
 * correctly handles IMAP literal responses.  The batch {@code fetchRange}
 * path in the production code uses a regex that does not handle multi-line
 * literal data; this is a known limitation documented here for clarity.</p>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("ImapEmailReceiver Integration Tests")
class ImapEmailReceiverIntegrationTest {

    private ServerSocket server;
    private int port;

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    static final String MSG1 =
            "From: sender@test.com\r\n"
                    + "To: me@test.com\r\n"
                    + "Subject: Test Message 1\r\n"
                    + "Message-ID: <msg1@test.com>\r\n"
                    + "Date: Mon, 1 Jan 2024 12:00:00 +0000\r\n"
                    + "Content-Type: text/plain; charset=UTF-8\r\n"
                    + "\r\n"
                    + "Hello World 1";

    static final String MSG2 =
            "From: other@test.com\r\n"
                    + "To: me@test.com\r\n"
                    + "Subject: Test Message 2\r\n"
                    + "Message-ID: <msg2@test.com>\r\n"
                    + "Date: Tue, 2 Jan 2024 12:00:00 +0000\r\n"
                    + "Content-Type: text/plain; charset=UTF-8\r\n"
                    + "\r\n"
                    + "Hello World 2";

    @BeforeEach
    void setUp() throws Exception {
        server = new ServerSocket(0);
        port = server.getLocalPort();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null && !server.isClosed()) {
            server.close();
        }
    }

    // ========================================================================
    // Mock IMAP server infrastructure
    // ========================================================================

    @FunctionalInterface
    interface MockHandler {
        void handle(BufferedReader in, BufferedOutputStream out) throws Exception;
    }

    private void startImapServer(MockHandler handler) {
        Thread.startVirtualThread(() -> {
            try (var socket = server.accept();
                 var in = new BufferedReader(new InputStreamReader(
                         socket.getInputStream(), StandardCharsets.UTF_8));
                 var out = new BufferedOutputStream(socket.getOutputStream())) {
                sendLine(out, "* OK [CAPABILITY IMAP4rev1] ready");
                handler.handle(in, out);
            } catch (Exception ignored) {
                // Test server shutdown
            }
        });
    }

    private static void sendLine(BufferedOutputStream out, String line) throws IOException {
        out.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private static String readCommand(BufferedReader in) throws IOException {
        return in.readLine();
    }

    private static String extractTag(String command) {
        if (command == null) {
            return "";
        }
        int space = command.indexOf(' ');
        return space > 0 ? command.substring(0, space) : command;
    }

    private static String respondOk(BufferedReader in, BufferedOutputStream out) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        sendLine(out, tag + " OK completed");
        return cmd;
    }

    private static void handleLogin(BufferedReader in, BufferedOutputStream out) throws IOException {
        respondOk(in, out);
    }

    private static void handleSelect(BufferedReader in, BufferedOutputStream out,
                                      int exists, int recent) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        sendLine(out, "* " + exists + " EXISTS");
        sendLine(out, "* " + recent + " RECENT");
        sendLine(out, "* OK [UIDVALIDITY 1] UIDs valid");
        sendLine(out, tag + " OK [READ-WRITE] SELECT completed");
    }

    private static void handleSearch(BufferedReader in, BufferedOutputStream out,
                                      int... seqNums) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        StringBuilder searchLine = new StringBuilder("* SEARCH");
        for (int seq : seqNums) {
            searchLine.append(" ").append(seq);
        }
        sendLine(out, searchLine.toString());
        sendLine(out, tag + " OK SEARCH completed");
    }

    private static String handleSearchReturning(BufferedReader in, BufferedOutputStream out,
                                                 int... seqNums) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        StringBuilder searchLine = new StringBuilder("* SEARCH");
        for (int seq : seqNums) {
            searchLine.append(" ").append(seq);
        }
        sendLine(out, searchLine.toString());
        sendLine(out, tag + " OK SEARCH completed");
        return cmd;
    }

    private static void handleEmptySearch(BufferedReader in, BufferedOutputStream out) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        sendLine(out, "* SEARCH");
        sendLine(out, tag + " OK SEARCH completed");
    }

    /**
     * Handle FETCH for a single message with literal body.
     * The ImapClient reads the literal via readLiteralContinuation.
     */
    private static void handleFetch(BufferedReader in, BufferedOutputStream out,
                                     int seq, String rawMessage) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        int size = rawMessage.getBytes(StandardCharsets.UTF_8).length;
        sendLine(out, "* " + seq + " FETCH (FLAGS (\\Seen) RFC822.SIZE " + size
                + " BODY[] {" + size + "}");
        out.write(rawMessage.getBytes(StandardCharsets.UTF_8));
        out.flush();
        sendLine(out, ")");
        sendLine(out, tag + " OK FETCH completed");
    }

    private static void handleLogout(BufferedReader in, BufferedOutputStream out) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        sendLine(out, "* BYE IMAP4rev1 Server logging out");
        sendLine(out, tag + " OK LOGOUT completed");
    }

    private static void drainCommands(BufferedReader in, BufferedOutputStream out) throws IOException {
        try {
            while (true) {
                String cmd = readCommand(in);
                if (cmd == null) {
                    break;
                }
                String tag = extractTag(cmd);
                if (cmd.toUpperCase().contains("LOGOUT")) {
                    sendLine(out, "* BYE logging out");
                    sendLine(out, tag + " OK LOGOUT completed");
                    break;
                }
                sendLine(out, tag + " OK completed");
            }
        } catch (Exception ignored) {
            // Connection closed
        }
    }

    private static void handleList(BufferedReader in, BufferedOutputStream out,
                                    String[][] folders) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        for (String[] folder : folders) {
            sendLine(out, "* LIST (" + folder[0] + ") \"" + folder[1] + "\" " + folder[2]);
        }
        sendLine(out, tag + " OK LIST completed");
    }

    private EmailReceiveConfig createConfig() {
        return EmailReceiveConfig.builder()
                .host("localhost")
                .port(port)
                .username("user")
                .password("pass")
                .imap()
                .ssl(false)
                .starttls(false)
                .timeout(TIMEOUT)
                .connectionTimeout(TIMEOUT)
                .markAsReadAfterReceive(false)
                .deleteAfterReceive(false)
                .build();
    }

    // ========================================================================
    // Test classes
    // ========================================================================

    @Nested
    @DisplayName("Connect and Disconnect Tests")
    class ConnectAndDisconnectTests {

        @Test
        @DisplayName("connect succeeds and isConnected returns true")
        void connectSucceeds() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                drainCommands(in, out);
            });

            var config = createConfig();
            var receiver = new ImapEmailReceiver(config);

            assertThat(receiver.isConnected()).isFalse();
            receiver.connect();
            assertThat(receiver.isConnected()).isTrue();

            receiver.disconnect();
            assertThat(receiver.isConnected()).isFalse();
        }

        @Test
        @DisplayName("disconnect on already disconnected is safe")
        void disconnectWhenNotConnected() {
            var config = createConfig();
            var receiver = new ImapEmailReceiver(config);

            assertThatNoException().isThrownBy(receiver::disconnect);
        }

        @Test
        @DisplayName("connect is idempotent when already connected")
        void connectIdempotent() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                drainCommands(in, out);
            });

            var config = createConfig();
            var receiver = new ImapEmailReceiver(config);
            receiver.connect();
            receiver.connect(); // no-op
            assertThat(receiver.isConnected()).isTrue();

            receiver.disconnect();
        }
    }

    @Nested
    @DisplayName("receiveUnread Tests")
    class ReceiveUnreadTests {

        @Test
        @DisplayName("receive two unread messages from INBOX via individual fetch")
        void receiveTwoUnreadMessages() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 5, 0);
                // Non-contiguous sequences to trigger fetchIndividually
                handleSearch(in, out, 1, 3);
                handleFetch(in, out, 1, MSG1);
                handleFetch(in, out, 3, MSG2);
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<ReceivedEmail> emails = receiver.receiveUnread();

                assertThat(emails).hasSize(2);

                // Default sort is NEWEST_FIRST, so MSG2 (Jan 2) comes before MSG1 (Jan 1)
                ReceivedEmail newer = emails.get(0);
                assertThat(newer.from()).isEqualTo("other@test.com");
                assertThat(newer.subject()).isEqualTo("Test Message 2");
                assertThat(newer.textContent()).isEqualTo("Hello World 2");
                assertThat(newer.messageId()).isEqualTo("msg2@test.com");

                ReceivedEmail older = emails.get(1);
                assertThat(older.from()).isEqualTo("sender@test.com");
                assertThat(older.subject()).isEqualTo("Test Message 1");
                assertThat(older.textContent()).isEqualTo("Hello World 1");
                assertThat(older.messageId()).isEqualTo("msg1@test.com");
                assertThat(older.folder()).isEqualTo("INBOX");
            }
        }

        @Test
        @DisplayName("receive returns empty list when no unseen messages")
        void receiveNoMessages() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 0, 0);
                handleEmptySearch(in, out);
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<ReceivedEmail> emails = receiver.receiveUnread();

                assertThat(emails).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("receive with EmailQuery Tests")
    class ReceiveWithQueryTests {

        @Test
        @DisplayName("query with subjectContains sends SUBJECT search")
        void queryWithSubject() throws Exception {
            var capturedCommands = new CopyOnWriteArrayList<String>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 5, 0);
                // Non-contiguous so fetchIndividually is used
                String searchCmd = handleSearchReturning(in, out, 1, 4);
                capturedCommands.add(searchCmd);
                handleFetch(in, out, 1, MSG1);
                handleFetch(in, out, 4, MSG2);
                drainCommands(in, out);
            });

            var config = createConfig();
            var query = EmailQuery.builder()
                    .folder("INBOX")
                    .subjectContains("Test")
                    .limit(10)
                    .build();

            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<ReceivedEmail> emails = receiver.receive(query);

                assertThat(emails).isNotEmpty();
                // Both messages match "Test"; default sort is NEWEST_FIRST
                assertThat(emails.getFirst().subject()).startsWith("Test Message");
            }

            assertThat(capturedCommands).isNotEmpty();
            assertThat(capturedCommands.getFirst()).containsIgnoringCase("SUBJECT");
        }

        @Test
        @DisplayName("query with unreadOnly sends UNSEEN search")
        void queryWithUnreadOnly() throws Exception {
            var capturedCommands = new CopyOnWriteArrayList<String>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 5, 0);
                String searchCmd = handleSearchReturning(in, out, 2, 4);
                capturedCommands.add(searchCmd);
                handleFetch(in, out, 2, MSG1);
                handleFetch(in, out, 4, MSG2);
                drainCommands(in, out);
            });

            var config = createConfig();
            var query = EmailQuery.builder()
                    .folder("INBOX")
                    .unreadOnly()
                    .limit(10)
                    .build();

            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                receiver.receive(query);
            }

            assertThat(capturedCommands).isNotEmpty();
            assertThat(capturedCommands.getFirst()).containsIgnoringCase("UNSEEN");
        }

        @Test
        @DisplayName("query with limit returns only N results")
        void queryWithLimit() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 5, 0);
                // Non-contiguous
                handleSearch(in, out, 1, 3);
                handleFetch(in, out, 1, MSG1);
                handleFetch(in, out, 3, MSG2);
                drainCommands(in, out);
            });

            var config = createConfig();
            var query = EmailQuery.builder()
                    .folder("INBOX")
                    .limit(1)
                    .build();

            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<ReceivedEmail> emails = receiver.receive(query);

                assertThat(emails).hasSize(1);
            }
        }

        @Test
        @DisplayName("query with NEWEST_FIRST sort returns newest first")
        void queryWithNewestFirst() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 5, 0);
                handleSearch(in, out, 1, 3);
                handleFetch(in, out, 1, MSG1);
                handleFetch(in, out, 3, MSG2);
                drainCommands(in, out);
            });

            var config = createConfig();
            var query = EmailQuery.builder()
                    .folder("INBOX")
                    .sortBy(EmailQuery.SortOrder.NEWEST_FIRST)
                    .limit(10)
                    .build();

            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<ReceivedEmail> emails = receiver.receive(query);

                assertThat(emails).hasSize(2);
                // MSG2 date (Jan 2) > MSG1 date (Jan 1), so MSG2 comes first
                assertThat(emails.get(0).subject()).isEqualTo("Test Message 2");
                assertThat(emails.get(1).subject()).isEqualTo("Test Message 1");
            }
        }

        @Test
        @DisplayName("query with OLDEST_FIRST sort returns oldest first")
        void queryWithOldestFirst() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 5, 0);
                handleSearch(in, out, 1, 3);
                handleFetch(in, out, 1, MSG1);
                handleFetch(in, out, 3, MSG2);
                drainCommands(in, out);
            });

            var config = createConfig();
            var query = EmailQuery.builder()
                    .folder("INBOX")
                    .sortBy(EmailQuery.SortOrder.OLDEST_FIRST)
                    .limit(10)
                    .build();

            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<ReceivedEmail> emails = receiver.receive(query);

                assertThat(emails).hasSize(2);
                assertThat(emails.get(0).subject()).isEqualTo("Test Message 1");
                assertThat(emails.get(1).subject()).isEqualTo("Test Message 2");
            }
        }
    }

    @Nested
    @DisplayName("receiveById Tests")
    class ReceiveByIdTests {

        @Test
        @DisplayName("receiveById returns correct message")
        void receiveByIdFound() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 2, 0);
                handleSearch(in, out, 1);
                // receiveById calls client.fetch() (not fetchRange), so literals work
                handleFetch(in, out, 1, MSG1);
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                ReceivedEmail email = receiver.receiveById("<msg1@test.com>");

                assertThat(email).isNotNull();
                assertThat(email.messageId()).isEqualTo("msg1@test.com");
                assertThat(email.subject()).isEqualTo("Test Message 1");
                assertThat(email.from()).isEqualTo("sender@test.com");
            }
        }

        @Test
        @DisplayName("receiveById returns null for not found")
        void receiveByIdNotFound() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 0, 0);
                handleEmptySearch(in, out);
                // LIST folders to search other folders
                handleList(in, out, new String[][]{
                        {"", "/", "INBOX"}
                });
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                ReceivedEmail email = receiver.receiveById("<nonexistent@test.com>");

                assertThat(email).isNull();
            }
        }

        @Test
        @DisplayName("receiveById with null returns null")
        void receiveByIdNull() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                ReceivedEmail email = receiver.receiveById(null);

                assertThat(email).isNull();
            }
        }

        @Test
        @DisplayName("receiveById with blank returns null")
        void receiveByIdBlank() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                ReceivedEmail email = receiver.receiveById("   ");

                assertThat(email).isNull();
            }
        }
    }

    @Nested
    @DisplayName("markAsRead Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("markAsRead sends +FLAGS (\\Seen)")
        void markAsRead() throws Exception {
            var capturedCommands = new CopyOnWriteArrayList<String>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 1, 0);
                handleSearch(in, out, 1);
                String storeCmd = readCommand(in);
                capturedCommands.add(storeCmd);
                String tag = extractTag(storeCmd);
                sendLine(out, tag + " OK STORE completed");
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                receiver.markAsRead("<msg1@test.com>");
            }

            assertThat(capturedCommands).isNotEmpty();
            String storeCmd = capturedCommands.getFirst();
            assertThat(storeCmd).contains("+FLAGS");
            assertThat(storeCmd).contains("\\Seen");
        }
    }

    @Nested
    @DisplayName("markAsUnread Tests")
    class MarkAsUnreadTests {

        @Test
        @DisplayName("markAsUnread sends -FLAGS (\\Seen)")
        void markAsUnread() throws Exception {
            var capturedCommands = new CopyOnWriteArrayList<String>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 1, 0);
                handleSearch(in, out, 1);
                String storeCmd = readCommand(in);
                capturedCommands.add(storeCmd);
                String tag = extractTag(storeCmd);
                sendLine(out, tag + " OK STORE completed");
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                receiver.markAsUnread("<msg1@test.com>");
            }

            assertThat(capturedCommands).isNotEmpty();
            String storeCmd = capturedCommands.getFirst();
            assertThat(storeCmd).contains("-FLAGS");
            assertThat(storeCmd).contains("\\Seen");
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("delete sends STORE +FLAGS (\\Deleted) then EXPUNGE")
        void deleteMessage() throws Exception {
            var capturedCommands = new CopyOnWriteArrayList<String>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 1, 0);
                handleSearch(in, out, 1);
                String storeCmd = readCommand(in);
                capturedCommands.add(storeCmd);
                sendLine(out, extractTag(storeCmd) + " OK STORE completed");
                String expungeCmd = readCommand(in);
                capturedCommands.add(expungeCmd);
                sendLine(out, extractTag(expungeCmd) + " OK EXPUNGE completed");
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                receiver.delete("<msg1@test.com>");
            }

            assertThat(capturedCommands).hasSize(2);
            assertThat(capturedCommands.get(0)).contains("+FLAGS").contains("\\Deleted");
            assertThat(capturedCommands.get(1)).containsIgnoringCase("EXPUNGE");
        }

        @Test
        @DisplayName("delete throws when message not found")
        void deleteNotFound() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 0, 0);
                handleEmptySearch(in, out);
                handleList(in, out, new String[][]{
                        {"", "/", "INBOX"}
                });
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                assertThatThrownBy(() -> receiver.delete("<nonexistent@test.com>"))
                        .isInstanceOf(EmailReceiveException.class)
                        .hasMessageContaining("not found");
            }
        }
    }

    @Nested
    @DisplayName("moveToFolder Tests")
    class MoveToFolderTests {

        @Test
        @DisplayName("moveToFolder sends COPY, STORE +FLAGS (\\Deleted), EXPUNGE")
        void moveToFolder() throws Exception {
            var capturedCommands = new CopyOnWriteArrayList<String>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 1, 0);
                handleSearch(in, out, 1);
                // COPY
                String copyCmd = readCommand(in);
                capturedCommands.add(copyCmd);
                sendLine(out, extractTag(copyCmd) + " OK COPY completed");
                // STORE
                String storeCmd = readCommand(in);
                capturedCommands.add(storeCmd);
                sendLine(out, extractTag(storeCmd) + " OK STORE completed");
                // EXPUNGE
                String expungeCmd = readCommand(in);
                capturedCommands.add(expungeCmd);
                sendLine(out, extractTag(expungeCmd) + " OK EXPUNGE completed");
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                receiver.moveToFolder("<msg1@test.com>", "Archive");
            }

            assertThat(capturedCommands).hasSize(3);
            assertThat(capturedCommands.get(0)).containsIgnoringCase("COPY");
            assertThat(capturedCommands.get(0)).contains("Archive");
            assertThat(capturedCommands.get(1)).contains("+FLAGS").contains("\\Deleted");
            assertThat(capturedCommands.get(2)).containsIgnoringCase("EXPUNGE");
        }
    }

    @Nested
    @DisplayName("listFolders Tests")
    class ListFoldersTests {

        @Test
        @DisplayName("listFolders returns selectable folders, filters out \\Noselect")
        void listFoldersFiltersNoselect() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleList(in, out, new String[][]{
                        {"", "/", "INBOX"},
                        {"", "/", "Sent"},
                        {"\\Noselect", "/", "NoSelect-Folder"},
                        {"", "/", "Trash"}
                });
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<String> folders = receiver.listFolders();

                assertThat(folders).containsExactly("INBOX", "Sent", "Trash");
                assertThat(folders).doesNotContain("NoSelect-Folder");
            }
        }

        @Test
        @DisplayName("listFolders removes quotes from folder names")
        void listFoldersRemovesQuotes() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                String cmd = readCommand(in);
                String tag = extractTag(cmd);
                sendLine(out, "* LIST () \"/\" \"INBOX\"");
                sendLine(out, "* LIST () \"/\" \"Sent Items\"");
                sendLine(out, tag + " OK LIST completed");
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<String> folders = receiver.listFolders();

                assertThat(folders).containsExactly("INBOX", "Sent Items");
            }
        }
    }

    @Nested
    @DisplayName("getMessageCount Tests")
    class GetMessageCountTests {

        @Test
        @DisplayName("getMessageCount returns correct count from STATUS")
        void getMessageCount() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                // EXAMINE INBOX
                String examineCmd = readCommand(in);
                String examineTag = extractTag(examineCmd);
                sendLine(out, "* 5 EXISTS");
                sendLine(out, "* 0 RECENT");
                sendLine(out, examineTag + " OK [READ-ONLY] EXAMINE completed");
                // STATUS INBOX (MESSAGES)
                String statusCmd = readCommand(in);
                String statusTag = extractTag(statusCmd);
                sendLine(out, "* STATUS \"INBOX\" (MESSAGES 5)");
                sendLine(out, statusTag + " OK STATUS completed");
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                int count = receiver.getMessageCount("INBOX");

                assertThat(count).isEqualTo(5);
            }
        }
    }

    @Nested
    @DisplayName("getUnreadCount Tests")
    class GetUnreadCountTests {

        @Test
        @DisplayName("getUnreadCount returns count of UNSEEN messages")
        void getUnreadCount() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                // EXAMINE
                String examineCmd = readCommand(in);
                String examineTag = extractTag(examineCmd);
                sendLine(out, "* 5 EXISTS");
                sendLine(out, "* 2 RECENT");
                sendLine(out, examineTag + " OK [READ-ONLY] EXAMINE completed");
                // SEARCH UNSEEN
                handleSearch(in, out, 1, 3, 5);
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                int count = receiver.getUnreadCount("INBOX");

                assertThat(count).isEqualTo(3);
            }
        }
    }

    @Nested
    @DisplayName("setFlagged Tests")
    class SetFlaggedTests {

        @Test
        @DisplayName("setFlagged true sends +FLAGS (\\Flagged)")
        void setFlaggedTrue() throws Exception {
            var capturedCommands = new CopyOnWriteArrayList<String>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 1, 0);
                handleSearch(in, out, 1);
                String storeCmd = readCommand(in);
                capturedCommands.add(storeCmd);
                sendLine(out, extractTag(storeCmd) + " OK STORE completed");
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                receiver.setFlagged("<msg1@test.com>", true);
            }

            assertThat(capturedCommands.getFirst()).contains("+FLAGS").contains("\\Flagged");
        }

        @Test
        @DisplayName("setFlagged false sends -FLAGS (\\Flagged)")
        void setFlaggedFalse() throws Exception {
            var capturedCommands = new CopyOnWriteArrayList<String>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 1, 0);
                handleSearch(in, out, 1);
                String storeCmd = readCommand(in);
                capturedCommands.add(storeCmd);
                sendLine(out, extractTag(storeCmd) + " OK STORE completed");
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                receiver.setFlagged("<msg1@test.com>", false);
            }

            assertThat(capturedCommands.getFirst()).contains("-FLAGS").contains("\\Flagged");
        }
    }

    @Nested
    @DisplayName("Auto-connect Tests")
    class AutoConnectTests {

        @Test
        @DisplayName("calling receive without connect triggers auto-connect")
        void autoConnectOnReceive() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 0, 0);
                handleEmptySearch(in, out);
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                List<ReceivedEmail> emails = receiver.receiveUnread();

                assertThat(receiver.isConnected()).isTrue();
                assertThat(emails).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Post-receive Action Tests")
    class PostReceiveActionTests {

        @Test
        @DisplayName("markAsReadAfterReceive sends STORE +FLAGS (\\Seen)")
        void markAsReadAfterReceive() throws Exception {
            var capturedCommands = new CopyOnWriteArrayList<String>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 5, 0);
                // Non-contiguous for fetchIndividually
                handleSearch(in, out, 1, 4);
                handleFetch(in, out, 1, MSG1);
                handleFetch(in, out, 4, MSG2);
                // STORE +FLAGS (\Seen) for each message
                String store1 = readCommand(in);
                capturedCommands.add(store1);
                sendLine(out, extractTag(store1) + " OK STORE completed");
                String store2 = readCommand(in);
                capturedCommands.add(store2);
                sendLine(out, extractTag(store2) + " OK STORE completed");
                drainCommands(in, out);
            });

            var config = EmailReceiveConfig.builder()
                    .host("localhost")
                    .port(port)
                    .username("user")
                    .password("pass")
                    .imap()
                    .ssl(false)
                    .starttls(false)
                    .timeout(TIMEOUT)
                    .connectionTimeout(TIMEOUT)
                    .markAsReadAfterReceive(true)
                    .deleteAfterReceive(false)
                    .build();

            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<ReceivedEmail> emails = receiver.receiveUnread();

                assertThat(emails).hasSize(2);
            }

            assertThat(capturedCommands).isNotEmpty();
            assertThat(capturedCommands.getFirst()).contains("+FLAGS").contains("\\Seen");
        }

        @Test
        @DisplayName("deleteAfterReceive sends STORE +FLAGS (\\Deleted) and EXPUNGE")
        void deleteAfterReceive() throws Exception {
            var capturedCommands = new CopyOnWriteArrayList<String>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 5, 0);
                handleSearch(in, out, 1, 4);
                handleFetch(in, out, 1, MSG1);
                handleFetch(in, out, 4, MSG2);
                // STORE +FLAGS (\Deleted) for each message
                String store1 = readCommand(in);
                capturedCommands.add(store1);
                sendLine(out, extractTag(store1) + " OK STORE completed");
                String store2 = readCommand(in);
                capturedCommands.add(store2);
                sendLine(out, extractTag(store2) + " OK STORE completed");
                // EXPUNGE
                String expungeCmd = readCommand(in);
                capturedCommands.add(expungeCmd);
                sendLine(out, extractTag(expungeCmd) + " OK EXPUNGE completed");
                drainCommands(in, out);
            });

            var config = EmailReceiveConfig.builder()
                    .host("localhost")
                    .port(port)
                    .username("user")
                    .password("pass")
                    .imap()
                    .ssl(false)
                    .starttls(false)
                    .timeout(TIMEOUT)
                    .connectionTimeout(TIMEOUT)
                    .markAsReadAfterReceive(false)
                    .deleteAfterReceive(true)
                    .build();

            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<ReceivedEmail> emails = receiver.receiveUnread();

                assertThat(emails).hasSize(2);
            }

            assertThat(capturedCommands).hasSizeGreaterThanOrEqualTo(3);
            assertThat(capturedCommands.get(0)).contains("+FLAGS").contains("\\Deleted");
            assertThat(capturedCommands.getLast()).containsIgnoringCase("EXPUNGE");
        }
    }

    @Nested
    @DisplayName("Non-contiguous Sequence Number Tests")
    class NonContiguousSeqTests {

        @Test
        @DisplayName("non-contiguous sequence numbers fetch individually")
        void nonContiguousSeqNums() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 5, 0);
                handleSearch(in, out, 1, 3);
                handleFetch(in, out, 1, MSG1);
                handleFetch(in, out, 3, MSG2);
                drainCommands(in, out);
            });

            var config = createConfig();
            var query = EmailQuery.builder()
                    .folder("INBOX")
                    .limit(10)
                    .build();

            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<ReceivedEmail> emails = receiver.receive(query);

                assertThat(emails).hasSize(2);
            }
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("offset skips first N results")
        void offsetSkipsResults() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 5, 0);
                handleSearch(in, out, 1, 3);
                handleFetch(in, out, 1, MSG1);
                handleFetch(in, out, 3, MSG2);
                drainCommands(in, out);
            });

            var config = createConfig();
            var query = EmailQuery.builder()
                    .folder("INBOX")
                    .sortBy(EmailQuery.SortOrder.OLDEST_FIRST)
                    .offset(1)
                    .limit(10)
                    .build();

            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                List<ReceivedEmail> emails = receiver.receive(query);

                assertThat(emails).hasSize(1);
                assertThat(emails.getFirst().subject()).isEqualTo("Test Message 2");
            }
        }
    }

    @Nested
    @DisplayName("Message Parsing Tests")
    class MessageParsingTests {

        @Test
        @DisplayName("parsed email has correct to recipients and flags")
        void parsedEmailHasCorrectFields() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 2, 0);
                // receiveById uses client.fetch() which works with literals
                handleSearch(in, out, 1);
                handleFetch(in, out, 1, MSG1);
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                ReceivedEmail email = receiver.receiveById("<msg1@test.com>");

                assertThat(email).isNotNull();
                assertThat(email.to()).contains("me@test.com");
                assertThat(email.sentDate()).isNotNull();
                assertThat(email.size()).isGreaterThan(0);
                assertThat(email.flags()).isNotNull();
                assertThat(email.flags().seen()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("receiveById searches other folders Tests")
    class ReceiveByIdOtherFoldersTests {

        @Test
        @DisplayName("receiveById searches other folders when not in default")
        void receiveByIdSearchesOtherFolders() throws Exception {
            startImapServer((in, out) -> {
                handleLogin(in, out);
                // SELECT INBOX
                handleSelect(in, out, 0, 0);
                // SEARCH in INBOX returns empty
                handleEmptySearch(in, out);
                // LIST folders
                handleList(in, out, new String[][]{
                        {"", "/", "INBOX"},
                        {"", "/", "Sent"}
                });
                // SELECT Sent
                handleSelect(in, out, 1, 0);
                // SEARCH in Sent finds it
                handleSearch(in, out, 1);
                // FETCH from Sent
                handleFetch(in, out, 1, MSG2);
                drainCommands(in, out);
            });

            var config = createConfig();
            try (var receiver = new AutoCloseableReceiver(config)) {
                receiver.connect();
                ReceivedEmail email = receiver.receiveById("<msg2@test.com>");

                assertThat(email).isNotNull();
                assertThat(email.messageId()).isEqualTo("msg2@test.com");
                assertThat(email.folder()).isEqualTo("Sent");
            }
        }
    }

    // ========================================================================
    // AutoCloseable wrapper for ImapEmailReceiver
    // ========================================================================

    private static class AutoCloseableReceiver implements AutoCloseable {
        private final ImapEmailReceiver delegate;

        AutoCloseableReceiver(EmailReceiveConfig config) {
            this.delegate = new ImapEmailReceiver(config);
        }

        void connect() { delegate.connect(); }
        boolean isConnected() { return delegate.isConnected(); }
        List<ReceivedEmail> receiveUnread() { return delegate.receiveUnread(); }
        List<ReceivedEmail> receive(EmailQuery query) { return delegate.receive(query); }
        ReceivedEmail receiveById(String messageId) { return delegate.receiveById(messageId); }
        void markAsRead(String messageId) { delegate.markAsRead(messageId); }
        void markAsUnread(String messageId) { delegate.markAsUnread(messageId); }
        void setFlagged(String messageId, boolean flagged) { delegate.setFlagged(messageId, flagged); }
        void delete(String messageId) { delegate.delete(messageId); }
        void moveToFolder(String messageId, String targetFolder) { delegate.moveToFolder(messageId, targetFolder); }
        List<String> listFolders() { return delegate.listFolders(); }
        int getMessageCount(String folder) { return delegate.getMessageCount(folder); }
        int getUnreadCount(String folder) { return delegate.getUnreadCount(folder); }

        @Override
        public void close() { delegate.disconnect(); }
    }
}
