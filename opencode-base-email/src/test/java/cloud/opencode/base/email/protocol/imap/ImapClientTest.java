package cloud.opencode.base.email.protocol.imap;

import cloud.opencode.base.email.protocol.ProtocolException;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * ImapClient unit tests using mock IMAP server via ServerSocket.
 * ImapClient 单元测试，通过 ServerSocket 模拟 IMAP 服务器。
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("ImapClient Tests")
class ImapClientTest {

    private ServerSocket serverSocket;
    private int port;

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

    @BeforeEach
    void setUp() throws Exception {
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    /**
     * Start a mock IMAP server in a virtual thread. The handler is called
     * after the greeting has been sent. It receives the reader/writer for the
     * accepted client connection.
     */
    private void startImapServer(MockImapHandler handler) {
        Thread.startVirtualThread(() -> {
            try (var socket = serverSocket.accept();
                 var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 var writer = new PrintWriter(socket.getOutputStream(), true)) {
                // Send greeting
                writer.print("* OK [CAPABILITY IMAP4rev1] IMAP4rev1 ready\r\n");
                writer.flush();
                handler.handle(reader, writer);
            } catch (Exception ignored) {
                // Test server shutdown — expected
            }
        });
    }

    /**
     * Start a mock IMAP server with a custom greeting.
     */
    private void startImapServerWithGreeting(String greeting, MockImapHandler handler) {
        Thread.startVirtualThread(() -> {
            try (var socket = serverSocket.accept();
                 var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 var writer = new PrintWriter(socket.getOutputStream(), true)) {
                writer.print(greeting + "\r\n");
                writer.flush();
                handler.handle(reader, writer);
            } catch (Exception ignored) {
                // Test server shutdown — expected
            }
        });
    }

    @FunctionalInterface
    interface MockImapHandler {
        void handle(BufferedReader reader, PrintWriter writer) throws Exception;
    }

    /**
     * Read one client command line and extract the tag prefix.
     */
    private static String readTag(String line) {
        int space = line.indexOf(' ');
        return space > 0 ? line.substring(0, space) : line;
    }

    private ImapClient createClient() {
        return new ImapClient("localhost", port, false, false, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    // ========================================================================
    // Constructor validation tests
    // ========================================================================

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorValidationTests {

        @Test
        @DisplayName("null host throws IllegalArgumentException")
        void testNullHost() {
            assertThatThrownBy(() -> new ImapClient(null, 143, false, false,
                    CONNECT_TIMEOUT, READ_TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Host");
        }

        @Test
        @DisplayName("blank host throws IllegalArgumentException")
        void testBlankHost() {
            assertThatThrownBy(() -> new ImapClient("  ", 143, false, false,
                    CONNECT_TIMEOUT, READ_TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Host");
        }

        @Test
        @DisplayName("port 0 throws IllegalArgumentException")
        void testInvalidPortZero() {
            assertThatThrownBy(() -> new ImapClient("localhost", 0, false, false,
                    CONNECT_TIMEOUT, READ_TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Port");
        }

        @Test
        @DisplayName("port 70000 throws IllegalArgumentException")
        void testInvalidPortTooHigh() {
            assertThatThrownBy(() -> new ImapClient("localhost", 70000, false, false,
                    CONNECT_TIMEOUT, READ_TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Port");
        }

        @Test
        @DisplayName("null connection timeout throws IllegalArgumentException")
        void testNullConnectionTimeout() {
            assertThatThrownBy(() -> new ImapClient("localhost", 143, false, false,
                    null, READ_TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("timeout");
        }

        @Test
        @DisplayName("negative connection timeout throws IllegalArgumentException")
        void testNegativeConnectionTimeout() {
            assertThatThrownBy(() -> new ImapClient("localhost", 143, false, false,
                    Duration.ofSeconds(-1), READ_TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null read timeout throws IllegalArgumentException")
        void testNullReadTimeout() {
            assertThatThrownBy(() -> new ImapClient("localhost", 143, false, false,
                    CONNECT_TIMEOUT, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("timeout");
        }

        @Test
        @DisplayName("ssl and starttls both true throws IllegalArgumentException")
        void testSslAndStarttlsBothTrue() {
            assertThatThrownBy(() -> new ImapClient("localhost", 143, true, true,
                    CONNECT_TIMEOUT, READ_TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SSL")
                    .hasMessageContaining("STARTTLS");
        }

        @Test
        @DisplayName("valid parameters do not throw")
        void testValidParameters() {
            assertThatNoException().isThrownBy(() ->
                    new ImapClient("localhost", 143, false, false, CONNECT_TIMEOUT, READ_TIMEOUT));
        }
    }

    // ========================================================================
    // Connect tests
    // ========================================================================

    @Nested
    @DisplayName("Connect Tests")
    class ConnectTests {

        @Test
        @DisplayName("connect succeeds with valid greeting")
        void testConnectSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // After greeting, client may send CAPABILITY — respond to it
                String line = reader.readLine();
                if (line != null) {
                    String tag = readTag(line);
                    writer.print("* CAPABILITY IMAP4rev1 IDLE\r\n");
                    writer.print(tag + " OK CAPABILITY completed\r\n");
                    writer.flush();
                }
            });

            ImapClient client = createClient();
            client.connect();

            assertThat(client.isConnected()).isTrue();
            client.close();
        }

        @Test
        @DisplayName("connect fails with bad greeting")
        void testConnectBadGreeting() {
            startImapServerWithGreeting("* BYE Server shutting down", (reader, writer) -> {
                // No further interaction
            });

            ImapClient client = createClient();
            assertThatThrownBy(client::connect)
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("rejected");
        }

        @Test
        @DisplayName("isConnected returns false before connect")
        void testNotConnectedByDefault() {
            ImapClient client = createClient();
            assertThat(client.isConnected()).isFalse();
        }
    }

    // ========================================================================
    // Login tests
    // ========================================================================

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("login succeeds with OK response")
        void testLoginSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // CAPABILITY command (sent during connect because greeting has caps)
                // greeting already has CAPABILITY in brackets, so client may not ask
                // LOGIN command
                String line = reader.readLine();
                String tag = readTag(line);
                if (line.contains("CAPABILITY")) {
                    writer.print("* CAPABILITY IMAP4rev1\r\n");
                    writer.print(tag + " OK CAPABILITY completed\r\n");
                    writer.flush();
                    line = reader.readLine();
                    tag = readTag(line);
                }
                // Expect LOGIN
                writer.print(tag + " OK LOGIN completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.login("user@example.com", "secret");

            // If we get here without exception, login succeeded
            assertThat(client.isConnected()).isTrue();
            client.close();
        }

        @Test
        @DisplayName("login failure with NO response throws ProtocolException")
        void testLoginFailure() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                if (line.contains("CAPABILITY")) {
                    writer.print("* CAPABILITY IMAP4rev1\r\n");
                    writer.print(tag + " OK CAPABILITY completed\r\n");
                    writer.flush();
                    line = reader.readLine();
                    tag = readTag(line);
                }
                writer.print(tag + " NO [AUTHENTICATIONFAILED] Invalid credentials\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.login("user", "wrong"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("LOGIN failed");
            client.close();
        }

        @Test
        @DisplayName("login with null username throws NullPointerException")
        void testLoginNullUsername() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.login(null, "password"))
                    .isInstanceOf(NullPointerException.class);
            client.close();
        }

        @Test
        @DisplayName("login with null password throws NullPointerException")
        void testLoginNullPassword() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.login("user", null))
                    .isInstanceOf(NullPointerException.class);
            client.close();
        }
    }

    // ========================================================================
    // Select tests
    // ========================================================================

    @Nested
    @DisplayName("Select Tests")
    class SelectTests {

        @Test
        @DisplayName("select returns EXISTS and RECENT counts")
        void testSelectSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT command
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 10 EXISTS\r\n");
                writer.print("* 2 RECENT\r\n");
                writer.print("* OK [UIDVALIDITY 3857529045]\r\n");
                writer.print(tag + " OK [READ-WRITE] SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            int[] counts = client.select("INBOX");

            assertThat(counts).hasSize(2);
            assertThat(counts[0]).isEqualTo(10);
            assertThat(counts[1]).isEqualTo(2);
            assertThat(client.getSelectedFolder()).isEqualTo("INBOX");
            client.close();
        }

        @Test
        @DisplayName("select with zero messages returns [0, 0]")
        void testSelectEmptyFolder() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 0 EXISTS\r\n");
                writer.print("* 0 RECENT\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            int[] counts = client.select("INBOX");
            assertThat(counts[0]).isZero();
            assertThat(counts[1]).isZero();
            client.close();
        }

        @Test
        @DisplayName("select failure throws ProtocolException")
        void testSelectFailure() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print(tag + " NO Mailbox does not exist\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.select("NONEXISTENT"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("failed");
            client.close();
        }

        @Test
        @DisplayName("select with null folder throws NullPointerException")
        void testSelectNullFolder() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.select(null))
                    .isInstanceOf(NullPointerException.class);
            client.close();
        }
    }

    // ========================================================================
    // Examine tests
    // ========================================================================

    @Nested
    @DisplayName("Examine Tests")
    class ExamineTests {

        @Test
        @DisplayName("examine returns EXISTS and RECENT counts (read-only)")
        void testExamineSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print("* 1 RECENT\r\n");
                writer.print(tag + " OK [READ-ONLY] EXAMINE completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            int[] counts = client.examine("INBOX");

            assertThat(counts[0]).isEqualTo(5);
            assertThat(counts[1]).isEqualTo(1);
            client.close();
        }
    }

    // ========================================================================
    // Search tests
    // ========================================================================

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("search returns matching sequence numbers")
        void testSearchSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT first
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 10 EXISTS\r\n");
                writer.print("* 0 RECENT\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // SEARCH
                line = reader.readLine();
                tag = readTag(line);
                writer.print("* SEARCH 1 3 5 7\r\n");
                writer.print(tag + " OK SEARCH completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            List<Integer> results = client.search("UNSEEN");

            assertThat(results).containsExactly(1, 3, 5, 7);
            client.close();
        }

        @Test
        @DisplayName("search returns empty list when no matches")
        void testSearchEmpty() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 10 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // SEARCH
                line = reader.readLine();
                tag = readTag(line);
                writer.print("* SEARCH\r\n");
                writer.print(tag + " OK SEARCH completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            List<Integer> results = client.search("FROM \"nobody@example.com\"");

            assertThat(results).isEmpty();
            client.close();
        }

        @Test
        @DisplayName("search without selected folder throws ProtocolException")
        void testSearchWithoutSelect() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.search("ALL"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("No folder selected");
            client.close();
        }

        @Test
        @DisplayName("search with null criteria throws NullPointerException")
        void testSearchNullCriteria() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 1 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.search(null))
                    .isInstanceOf(NullPointerException.class);
            client.close();
        }
    }

    // ========================================================================
    // Fetch tests
    // ========================================================================

    @Nested
    @DisplayName("Fetch Tests")
    class FetchTests {

        @Test
        @DisplayName("fetch returns response body with literal data")
        void testFetchWithLiteral() throws Exception {
            String body = "Hello World";
            int bodyLen = body.length();

            startImapServer((reader, writer) -> {
                // SELECT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // FETCH
                line = reader.readLine();
                tag = readTag(line);
                writer.print("* 1 FETCH (FLAGS (\\Seen) BODY[] {" + bodyLen + "}\r\n");
                writer.print(body + ")\r\n");
                writer.print(tag + " OK FETCH completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            String result = client.fetch(1, "(BODY[] FLAGS)");

            assertThat(result).contains("FETCH");
            assertThat(result).contains("\\Seen");
            assertThat(result).contains(body);
            client.close();
        }

        @Test
        @DisplayName("fetch with msgSeq < 1 throws IllegalArgumentException")
        void testFetchInvalidSeq() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.fetch(0, "(BODY[])"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(">= 1");
            client.close();
        }

        @Test
        @DisplayName("fetch without selected folder throws ProtocolException")
        void testFetchWithoutSelect() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.fetch(1, "(BODY[])"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("No folder selected");
            client.close();
        }

        @Test
        @DisplayName("fetch with null fetchItems throws NullPointerException")
        void testFetchNullItems() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 1 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.fetch(1, null))
                    .isInstanceOf(NullPointerException.class);
            client.close();
        }
    }

    // ========================================================================
    // FetchRange tests
    // ========================================================================

    @Nested
    @DisplayName("FetchRange Tests")
    class FetchRangeTests {

        @Test
        @DisplayName("fetchRange returns map of sequence numbers to response data")
        void testFetchRangeSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // FETCH range
                line = reader.readLine();
                tag = readTag(line);
                writer.print("* 1 FETCH (FLAGS (\\Seen) RFC822.SIZE 100)\r\n");
                writer.print("* 2 FETCH (FLAGS () RFC822.SIZE 200)\r\n");
                writer.print("* 3 FETCH (FLAGS (\\Recent) RFC822.SIZE 150)\r\n");
                writer.print(tag + " OK FETCH completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            Map<Integer, String> results = client.fetchRange(1, 3, "(FLAGS RFC822.SIZE)");

            assertThat(results).hasSize(3);
            assertThat(results).containsKeys(1, 2, 3);
            assertThat(results.get(1)).contains("\\Seen");
            assertThat(results.get(3)).contains("\\Recent");
            client.close();
        }

        @Test
        @DisplayName("fetchRange with from < 1 throws IllegalArgumentException")
        void testFetchRangeInvalidFrom() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.fetchRange(0, 3, "(FLAGS)"))
                    .isInstanceOf(IllegalArgumentException.class);
            client.close();
        }

        @Test
        @DisplayName("fetchRange with to < from throws IllegalArgumentException")
        void testFetchRangeToLessThanFrom() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.fetchRange(5, 3, "(FLAGS)"))
                    .isInstanceOf(IllegalArgumentException.class);
            client.close();
        }
    }

    // ========================================================================
    // Store tests
    // ========================================================================

    @Nested
    @DisplayName("Store Tests")
    class StoreTests {

        @Test
        @DisplayName("store +FLAGS succeeds")
        void testStoreSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // STORE
                line = reader.readLine();
                tag = readTag(line);
                writer.print("* 1 FETCH (FLAGS (\\Seen))\r\n");
                writer.print(tag + " OK STORE completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatNoException().isThrownBy(() ->
                    client.store(1, "+FLAGS", "(\\Seen)"));
            client.close();
        }

        @Test
        @DisplayName("store with invalid sequence throws IllegalArgumentException")
        void testStoreInvalidSeq() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.store(0, "+FLAGS", "(\\Seen)"))
                    .isInstanceOf(IllegalArgumentException.class);
            client.close();
        }

        @Test
        @DisplayName("store with null action throws NullPointerException")
        void testStoreNullAction() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.store(1, null, "(\\Seen)"))
                    .isInstanceOf(NullPointerException.class);
            client.close();
        }

        @Test
        @DisplayName("store with null flags throws NullPointerException")
        void testStoreNullFlags() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.store(1, "+FLAGS", null))
                    .isInstanceOf(NullPointerException.class);
            client.close();
        }
    }

    // ========================================================================
    // Copy tests
    // ========================================================================

    @Nested
    @DisplayName("Copy Tests")
    class CopyTests {

        @Test
        @DisplayName("copy succeeds")
        void testCopySuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // COPY
                line = reader.readLine();
                tag = readTag(line);
                writer.print(tag + " OK COPY completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatNoException().isThrownBy(() ->
                    client.copy(1, "Archive"));
            client.close();
        }

        @Test
        @DisplayName("copy with invalid sequence throws IllegalArgumentException")
        void testCopyInvalidSeq() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.copy(-1, "Archive"))
                    .isInstanceOf(IllegalArgumentException.class);
            client.close();
        }

        @Test
        @DisplayName("copy with null target folder throws NullPointerException")
        void testCopyNullTarget() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.copy(1, null))
                    .isInstanceOf(NullPointerException.class);
            client.close();
        }

        @Test
        @DisplayName("copy failure throws ProtocolException")
        void testCopyFailure() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // COPY
                line = reader.readLine();
                tag = readTag(line);
                writer.print(tag + " NO [TRYCREATE] Destination mailbox does not exist\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatThrownBy(() -> client.copy(1, "NonExistent"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("COPY failed");
            client.close();
        }
    }

    // ========================================================================
    // Expunge tests
    // ========================================================================

    @Nested
    @DisplayName("Expunge Tests")
    class ExpungeTests {

        @Test
        @DisplayName("expunge succeeds")
        void testExpungeSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 5 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // EXPUNGE
                line = reader.readLine();
                tag = readTag(line);
                writer.print("* 3 EXPUNGE\r\n");
                writer.print(tag + " OK EXPUNGE completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThatNoException().isThrownBy(client::expunge);
            client.close();
        }

        @Test
        @DisplayName("expunge without selected folder throws ProtocolException")
        void testExpungeWithoutSelect() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(client::expunge)
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("No folder selected");
            client.close();
        }
    }

    // ========================================================================
    // List tests
    // ========================================================================

    @Nested
    @DisplayName("List Tests")
    class ListTests {

        @Test
        @DisplayName("list returns folder info arrays")
        void testListSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // LIST
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* LIST (\\HasNoChildren) \"/\" \"INBOX\"\r\n");
                writer.print("* LIST (\\HasNoChildren) \"/\" \"Sent\"\r\n");
                writer.print("* LIST (\\Noselect \\HasChildren) \"/\" \"[Gmail]\"\r\n");
                writer.print(tag + " OK LIST completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            List<String[]> folders = client.list("", "*");

            assertThat(folders).hasSize(3);
            // First folder: INBOX
            assertThat(folders.get(0)[0]).contains("HasNoChildren");
            assertThat(folders.get(0)[1]).isEqualTo("/");
            assertThat(folders.get(0)[2]).isEqualTo("INBOX");
            // Second folder: Sent
            assertThat(folders.get(1)[2]).isEqualTo("Sent");
            // Third folder: [Gmail] with Noselect
            assertThat(folders.get(2)[0]).contains("Noselect");
            client.close();
        }

        @Test
        @DisplayName("list returns empty list when no folders match")
        void testListEmpty() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print(tag + " OK LIST completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            List<String[]> folders = client.list("", "NonExistent*");
            assertThat(folders).isEmpty();
            client.close();
        }

        @Test
        @DisplayName("list with null reference throws NullPointerException")
        void testListNullReference() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.list(null, "*"))
                    .isInstanceOf(NullPointerException.class);
            client.close();
        }

        @Test
        @DisplayName("list with null pattern throws NullPointerException")
        void testListNullPattern() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.list("", null))
                    .isInstanceOf(NullPointerException.class);
            client.close();
        }
    }

    // ========================================================================
    // QuoteString tests (tested indirectly through public API)
    // ========================================================================

    @Nested
    @DisplayName("QuoteString Tests")
    class QuoteStringTests {

        @Test
        @DisplayName("string with backslash is escaped in IMAP command")
        void testBackslashEscaping() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            String[] capturedCommand = new String[1];

            startImapServer((reader, writer) -> {
                // LIST command — the reference arg will contain the backslash
                String line = reader.readLine();
                capturedCommand[0] = line;
                latch.countDown();
                String tag = readTag(line);
                writer.print(tag + " OK LIST completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            client.list("test\\path", "*");
            latch.await(3, TimeUnit.SECONDS);

            // The backslash should be escaped as \\
            assertThat(capturedCommand[0]).contains("\"test\\\\path\"");
            client.close();
        }

        @Test
        @DisplayName("string with double-quote is escaped in IMAP command")
        void testQuoteEscaping() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            String[] capturedCommand = new String[1];

            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                capturedCommand[0] = line;
                latch.countDown();
                String tag = readTag(line);
                writer.print(tag + " OK LIST completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            client.list("test\"quoted", "*");
            latch.await(3, TimeUnit.SECONDS);

            // The quote should be escaped as \"
            assertThat(capturedCommand[0]).contains("test\\\"quoted");
            client.close();
        }

        @Test
        @DisplayName("string with CR throws IllegalArgumentException")
        void testCrRejected() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.list("test\rpath", "*"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid character");
            client.close();
        }

        @Test
        @DisplayName("string with LF throws IllegalArgumentException")
        void testLfRejected() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.list("test\npath", "*"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid character");
            client.close();
        }

        @Test
        @DisplayName("string with NUL throws IllegalArgumentException")
        void testNulRejected() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.list("test\0path", "*"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid character");
            client.close();
        }
    }

    // ========================================================================
    // Logout tests
    // ========================================================================

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("logout sends LOGOUT and closes connection")
        void testLogoutSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // LOGOUT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* BYE IMAP4rev1 Server logging out\r\n");
                writer.print(tag + " OK LOGOUT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            assertThatNoException().isThrownBy(client::logout);
            assertThat(client.isConnected()).isFalse();
        }
    }

    // ========================================================================
    // GetMessageCount tests
    // ========================================================================

    @Nested
    @DisplayName("GetMessageCount Tests")
    class GetMessageCountTests {

        @Test
        @DisplayName("getMessageCount returns MESSAGES count from STATUS")
        void testGetMessageCount() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 15 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // STATUS
                line = reader.readLine();
                tag = readTag(line);
                writer.print("* STATUS \"INBOX\" (MESSAGES 15)\r\n");
                writer.print(tag + " OK STATUS completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            int count = client.getMessageCount();

            assertThat(count).isEqualTo(15);
            client.close();
        }

        @Test
        @DisplayName("getMessageCount without selected folder throws ProtocolException")
        void testGetMessageCountWithoutSelect() throws Exception {
            startImapServer((reader, writer) -> {
                // ignore
            });

            ImapClient client = createClient();
            client.connect();

            assertThatThrownBy(client::getMessageCount)
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("No folder selected");
            client.close();
        }
    }

    // ========================================================================
    // GetUnreadCount tests
    // ========================================================================

    @Nested
    @DisplayName("GetUnreadCount Tests")
    class GetUnreadCountTests {

        @Test
        @DisplayName("getUnreadCount returns count of UNSEEN messages")
        void testGetUnreadCount() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 10 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // SEARCH UNSEEN
                line = reader.readLine();
                tag = readTag(line);
                writer.print("* SEARCH 2 4 6\r\n");
                writer.print(tag + " OK SEARCH completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            int count = client.getUnreadCount();

            assertThat(count).isEqualTo(3);
            client.close();
        }

        @Test
        @DisplayName("getUnreadCount returns 0 when all messages are read")
        void testGetUnreadCountZero() throws Exception {
            startImapServer((reader, writer) -> {
                // SELECT
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 10 EXISTS\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();

                // SEARCH UNSEEN — no results
                line = reader.readLine();
                tag = readTag(line);
                writer.print("* SEARCH\r\n");
                writer.print(tag + " OK SEARCH completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            int count = client.getUnreadCount();

            assertThat(count).isZero();
            client.close();
        }
    }

    // ========================================================================
    // Noop tests
    // ========================================================================

    @Nested
    @DisplayName("Noop Tests")
    class NoopTests {

        @Test
        @DisplayName("noop succeeds")
        void testNoopSuccess() throws Exception {
            startImapServer((reader, writer) -> {
                // NOOP
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print(tag + " OK NOOP completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();

            assertThatNoException().isThrownBy(client::noop);
            client.close();
        }
    }

    // ========================================================================
    // Capability tests
    // ========================================================================

    @Nested
    @DisplayName("Capability Tests")
    class CapabilityTests {

        @Test
        @DisplayName("capabilities parsed from greeting")
        void testCapabilitiesFromGreeting() throws Exception {
            startImapServer((reader, writer) -> {
                // Client may not send CAPABILITY since greeting had it
                // Just handle any command
                String line = reader.readLine();
                if (line != null) {
                    String tag = readTag(line);
                    writer.print(tag + " OK completed\r\n");
                    writer.flush();
                }
            });

            ImapClient client = createClient();
            client.connect();

            assertThat(client.hasCapability("IMAP4REV1")).isTrue();
            assertThat(client.getCapabilities()).contains("IMAP4REV1");
            client.close();
        }

        @Test
        @DisplayName("hasCapability with null returns false")
        void testHasCapabilityNull() {
            ImapClient client = createClient();
            assertThat(client.hasCapability(null)).isFalse();
        }

        @Test
        @DisplayName("hasCapability is case-insensitive")
        void testHasCapabilityCaseInsensitive() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                if (line != null) {
                    String tag = readTag(line);
                    writer.print(tag + " OK completed\r\n");
                    writer.flush();
                }
            });

            ImapClient client = createClient();
            client.connect();

            // Greeting advertised IMAP4rev1 in mixed case
            assertThat(client.hasCapability("imap4rev1")).isTrue();
            assertThat(client.hasCapability("IMAP4REV1")).isTrue();
            client.close();
        }
    }

    // ========================================================================
    // Close / AutoCloseable tests
    // ========================================================================

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

        @Test
        @DisplayName("close on unconnected client does not throw")
        void testCloseBeforeConnect() {
            ImapClient client = createClient();
            assertThatNoException().isThrownBy(client::close);
        }

        @Test
        @DisplayName("close on connected client disconnects")
        void testCloseAfterConnect() throws Exception {
            startImapServer((reader, writer) -> {
                // Handle LOGOUT during close
                String line = reader.readLine();
                if (line != null) {
                    String tag = readTag(line);
                    writer.print("* BYE\r\n");
                    writer.print(tag + " OK LOGOUT completed\r\n");
                    writer.flush();
                }
            });

            ImapClient client = createClient();
            client.connect();

            client.close();
            assertThat(client.isConnected()).isFalse();
        }

        @Test
        @DisplayName("try-with-resources auto-closes client")
        void testTryWithResources() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                if (line != null) {
                    String tag = readTag(line);
                    writer.print("* BYE\r\n");
                    writer.print(tag + " OK LOGOUT completed\r\n");
                    writer.flush();
                }
            });

            ImapClient client;
            try (ImapClient c = createClient()) {
                c.connect();
                client = c;
                assertThat(client.isConnected()).isTrue();
            }

            assertThat(client.isConnected()).isFalse();
        }
    }

    // ========================================================================
    // getSelectedFolder tests
    // ========================================================================

    @Nested
    @DisplayName("GetSelectedFolder Tests")
    class GetSelectedFolderTests {

        @Test
        @DisplayName("getSelectedFolder returns null before SELECT")
        void testNoSelectedFolder() {
            ImapClient client = createClient();
            assertThat(client.getSelectedFolder()).isNull();
        }

        @Test
        @DisplayName("getSelectedFolder returns folder name after SELECT")
        void testSelectedFolderAfterSelect() throws Exception {
            startImapServer((reader, writer) -> {
                String line = reader.readLine();
                String tag = readTag(line);
                writer.print("* 3 EXISTS\r\n");
                writer.print("* 0 RECENT\r\n");
                writer.print(tag + " OK SELECT completed\r\n");
                writer.flush();
            });

            ImapClient client = createClient();
            client.connect();
            client.select("INBOX");

            assertThat(client.getSelectedFolder()).isEqualTo("INBOX");
            client.close();
        }
    }
}
