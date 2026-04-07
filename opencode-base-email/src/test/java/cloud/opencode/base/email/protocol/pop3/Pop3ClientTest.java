package cloud.opencode.base.email.protocol.pop3;

import cloud.opencode.base.email.protocol.ProtocolException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Pop3Client unit tests using mock ServerSocket
 * Pop3Client 使用模拟 ServerSocket 的单元测试
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("Pop3Client Tests")
class Pop3ClientTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private ServerSocket serverSocket;
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();
    }

    @AfterEach
    void tearDown() throws Exception {
        serverSocket.close();
    }

    @FunctionalInterface
    interface MockServerHandler {
        void handle(BufferedReader reader, PrintWriter writer) throws Exception;
    }

    private void startMockServer(MockServerHandler handler) {
        Thread.startVirtualThread(() -> {
            try (Socket client = serverSocket.accept();
                 var reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 var writer = new PrintWriter(client.getOutputStream(), true)) {
                handler.handle(reader, writer);
            } catch (Exception ignored) {
            }
        });
    }

    /** Send the standard POP3 greeting. */
    private void sendGreeting(PrintWriter writer) {
        writer.print("+OK POP3 server ready\r\n");
        writer.flush();
    }

    /** Send greeting and handle USER/PASS login. */
    private void sendGreetingAndLogin(BufferedReader reader, PrintWriter writer) throws Exception {
        sendGreeting(writer);
        String userCmd = reader.readLine(); // USER xxx
        writer.print("+OK\r\n");
        writer.flush();
        String passCmd = reader.readLine(); // PASS xxx
        writer.print("+OK Logged in\r\n");
        writer.flush();
    }

    private Pop3Client createClient() {
        return new Pop3Client("localhost", port, false, false, TIMEOUT, TIMEOUT);
    }

    @Nested
    @DisplayName("Connect Tests")
    class ConnectTests {

        @Test
        @DisplayName("connect returns server greeting")
        void connectReturnsGreeting() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreeting(writer);
                Thread.sleep(500);
            });

            var client = createClient();
            String greeting = client.connect();

            assertThat(greeting).contains("POP3 server ready");
            assertThat(client.isConnected()).isTrue();

            client.close();
        }

        @Test
        @DisplayName("connect with -ERR greeting throws ProtocolException")
        void connectWithErrorGreeting() throws Exception {
            startMockServer((reader, writer) -> {
                writer.print("-ERR server unavailable\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();

            assertThatThrownBy(client::connect)
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("server unavailable");

            client.close();
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("successful USER/PASS login")
        void successfulLogin() throws Exception {
            var receivedCommands = new java.util.ArrayList<String>();
            startMockServer((reader, writer) -> {
                sendGreeting(writer);
                String userCmd = reader.readLine();
                receivedCommands.add(userCmd);
                writer.print("+OK\r\n");
                writer.flush();
                String passCmd = reader.readLine();
                receivedCommands.add(passCmd);
                writer.print("+OK Logged in\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatCode(() -> client.login("user@test.com", "secret"))
                    .doesNotThrowAnyException();

            client.close();
        }

        @Test
        @DisplayName("login failure with -ERR at PASS step")
        void loginFailureAtPass() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreeting(writer);
                reader.readLine(); // USER
                writer.print("+OK\r\n");
                writer.flush();
                reader.readLine(); // PASS
                writer.print("-ERR authentication failed\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.login("user@test.com", "wrong"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("authentication failed");

            client.close();
        }

        @Test
        @DisplayName("CRLF in username rejected before sending")
        void crlfInUsername() {
            var client = createClient();
            // No need to connect -- validation happens before any network I/O
            // Actually, login calls sendCommand which needs connection. Let's connect first.
            assertThatThrownBy(() -> client.login("user\r\n@test.com", "pass"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("Invalid character");
        }

        @Test
        @DisplayName("CRLF in password rejected before sending")
        void crlfInPassword() {
            var client = createClient();
            assertThatThrownBy(() -> client.login("user@test.com", "pass\nword"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("Invalid character");
        }

        @Test
        @DisplayName("null username throws NullPointerException")
        void nullUsername() {
            var client = createClient();
            assertThatThrownBy(() -> client.login(null, "pass"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null password throws NullPointerException")
        void nullPassword() {
            var client = createClient();
            assertThatThrownBy(() -> client.login("user", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("STAT Tests")
    class StatTests {

        @Test
        @DisplayName("stat returns message count and total size")
        void statReturnsCountAndSize() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                String cmd = reader.readLine(); // STAT
                writer.print("+OK 5 12345\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            int[] result = client.stat();
            assertThat(result).containsExactly(5, 12345);

            client.close();
        }

        @Test
        @DisplayName("stat with zero messages")
        void statZeroMessages() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                reader.readLine(); // STAT
                writer.print("+OK 0 0\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            int[] result = client.stat();
            assertThat(result).containsExactly(0, 0);

            client.close();
        }
    }

    @Nested
    @DisplayName("LIST Tests")
    class ListTests {

        @Test
        @DisplayName("list returns all message sizes")
        void listAllMessages() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                reader.readLine(); // LIST
                writer.print("+OK\r\n");
                writer.print("1 100\r\n");
                writer.print("2 200\r\n");
                writer.print("3 350\r\n");
                writer.print(".\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            List<int[]> result = client.list();
            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 100);
            assertThat(result.get(1)).containsExactly(2, 200);
            assertThat(result.get(2)).containsExactly(3, 350);

            client.close();
        }

        @Test
        @DisplayName("list single message returns size")
        void listSingleMessage() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                reader.readLine(); // LIST 1
                writer.print("+OK 1 512\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            int size = client.list(1);
            assertThat(size).isEqualTo(512);

            client.close();
        }

        @Test
        @DisplayName("list with invalid msgNum throws IllegalArgumentException")
        void listInvalidMsgNum() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                Thread.sleep(1000);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            assertThatThrownBy(() -> client.list(0))
                    .isInstanceOf(IllegalArgumentException.class);

            client.close();
        }
    }

    @Nested
    @DisplayName("UIDL Tests")
    class UidlTests {

        @Test
        @DisplayName("uidl returns all unique IDs")
        void uidlAll() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                reader.readLine(); // UIDL
                writer.print("+OK\r\n");
                writer.print("1 abc123\r\n");
                writer.print("2 def456\r\n");
                writer.print(".\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            List<String[]> result = client.uidl();
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsExactly("1", "abc123");
            assertThat(result.get(1)).containsExactly("2", "def456");

            client.close();
        }

        @Test
        @DisplayName("uidl single message returns unique ID")
        void uidlSingle() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                reader.readLine(); // UIDL 1
                writer.print("+OK 1 abc123\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            String uid = client.uidl(1);
            assertThat(uid).isEqualTo("abc123");

            client.close();
        }
    }

    @Nested
    @DisplayName("RETR Tests")
    class RetrTests {

        @Test
        @DisplayName("retr retrieves full message content")
        void retrFullMessage() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                reader.readLine(); // RETR 1
                writer.print("+OK 256 octets\r\n");
                writer.print("From: sender@test.com\r\n");
                writer.print("To: rcpt@test.com\r\n");
                writer.print("Subject: Test\r\n");
                writer.print("\r\n");
                writer.print("Hello World\r\n");
                writer.print(".\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            String message = client.retr(1);
            assertThat(message).contains("From: sender@test.com");
            assertThat(message).contains("Hello World");

            client.close();
        }

        @Test
        @DisplayName("retr removes byte-stuffing (.. becomes .)")
        void retrByteDestuffing() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                reader.readLine(); // RETR 1
                writer.print("+OK\r\n");
                writer.print("Subject: Test\r\n");
                writer.print("\r\n");
                writer.print("..hidden dot\r\n");
                writer.print("normal line\r\n");
                writer.print(".\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            String message = client.retr(1);
            assertThat(message).contains(".hidden dot");
            // Should NOT contain ".." (destuffed to single ".")
            assertThat(message).doesNotContain("..hidden dot");

            client.close();
        }

        @Test
        @DisplayName("retr with invalid msgNum throws IllegalArgumentException")
        void retrInvalidMsgNum() {
            var client = createClient();
            assertThatThrownBy(() -> client.retr(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client.retr(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("TOP Tests")
    class TopTests {

        @Test
        @DisplayName("top retrieves headers and partial body")
        void topRetrievesHeadersAndBody() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                String cmd = reader.readLine(); // TOP 1 5
                assertThat(cmd).isEqualTo("TOP 1 5");
                writer.print("+OK\r\n");
                writer.print("Subject: Test\r\n");
                writer.print("\r\n");
                writer.print("Line 1\r\n");
                writer.print("Line 2\r\n");
                writer.print(".\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            String content = client.top(1, 5);
            assertThat(content).contains("Subject: Test");
            assertThat(content).contains("Line 1");

            client.close();
        }

        @Test
        @DisplayName("top with negative lines throws IllegalArgumentException")
        void topNegativeLines() {
            var client = createClient();
            assertThatThrownBy(() -> client.top(1, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("DELE Tests")
    class DeleTests {

        @Test
        @DisplayName("dele marks message for deletion")
        void deleMarksMessage() throws Exception {
            var receivedCmd = new String[1];
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                receivedCmd[0] = reader.readLine(); // DELE 2
                writer.print("+OK message 2 deleted\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            assertThatCode(() -> client.dele(2)).doesNotThrowAnyException();

            client.close();
        }

        @Test
        @DisplayName("dele with invalid msgNum throws IllegalArgumentException")
        void deleInvalidMsgNum() {
            var client = createClient();
            assertThatThrownBy(() -> client.dele(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("QUIT Tests")
    class QuitTests {

        @Test
        @DisplayName("quit sends QUIT and closes connection")
        void quitClosesConnection() throws Exception {
            var receivedCmd = new String[1];
            startMockServer((reader, writer) -> {
                sendGreeting(writer);
                receivedCmd[0] = reader.readLine(); // QUIT
                writer.print("+OK Bye\r\n");
                writer.flush();
            });

            var client = createClient();
            client.connect();

            assertThatCode(client::quit).doesNotThrowAnyException();
            assertThat(client.isConnected()).isFalse();
        }

        @Test
        @DisplayName("quit with -ERR still closes connection")
        void quitWithErrorStillCloses() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreeting(writer);
                reader.readLine(); // QUIT
                writer.print("-ERR some error\r\n");
                writer.flush();
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(client::quit)
                    .isInstanceOf(ProtocolException.class);
            // Connection should still be closed (finally block)
            assertThat(client.isConnected()).isFalse();
        }
    }

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorValidationTests {

        @Test
        @DisplayName("port 0 throws IllegalArgumentException")
        void portZero() {
            assertThatThrownBy(() ->
                    new Pop3Client("localhost", 0, false, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("port -1 throws IllegalArgumentException")
        void portNegative() {
            assertThatThrownBy(() ->
                    new Pop3Client("localhost", -1, false, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("port 65536 throws IllegalArgumentException")
        void portTooLarge() {
            assertThatThrownBy(() ->
                    new Pop3Client("localhost", 65536, false, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null host throws NullPointerException")
        void nullHost() {
            assertThatThrownBy(() ->
                    new Pop3Client(null, 110, false, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null connectionTimeout throws NullPointerException")
        void nullConnectionTimeout() {
            assertThatThrownBy(() ->
                    new Pop3Client("localhost", 110, false, false, null, TIMEOUT))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null readTimeout throws NullPointerException")
        void nullReadTimeout() {
            assertThatThrownBy(() ->
                    new Pop3Client("localhost", 110, false, false, TIMEOUT, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Validate Credential Tests")
    class ValidateCredentialTests {

        @Test
        @DisplayName("\\r in credential rejected")
        void crInCredential() {
            var client = createClient();
            assertThatThrownBy(() -> client.login("user\r", "pass"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("Invalid character");
        }

        @Test
        @DisplayName("\\n in credential rejected")
        void lfInCredential() {
            var client = createClient();
            assertThatThrownBy(() -> client.login("user", "pass\n"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("Invalid character");
        }

        @Test
        @DisplayName("\\0 in credential rejected")
        void nullByteInCredential() {
            var client = createClient();
            assertThatThrownBy(() -> client.login("user\0name", "pass"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("Invalid character");
        }

        @Test
        @DisplayName("empty credential rejected")
        void emptyCredential() {
            var client = createClient();
            assertThatThrownBy(() -> client.login("", "pass"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("must not be empty");
        }
    }

    @Nested
    @DisplayName("NOOP and RSET Tests")
    class NoopAndRsetTests {

        @Test
        @DisplayName("noop keeps connection alive")
        void noopSucceeds() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                reader.readLine(); // NOOP
                writer.print("+OK\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            assertThatCode(client::noop).doesNotThrowAnyException();

            client.close();
        }

        @Test
        @DisplayName("rset unmarks messages")
        void rsetSucceeds() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndLogin(reader, writer);
                reader.readLine(); // RSET
                writer.print("+OK\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            client.login("user", "pass");

            assertThatCode(client::rset).doesNotThrowAnyException();

            client.close();
        }
    }

    @Nested
    @DisplayName("AUTH XOAUTH2 Tests")
    class AuthXOAuth2Tests {

        @Test
        @DisplayName("successful XOAUTH2 authentication")
        void successfulXOAuth2() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreeting(writer);
                String cmd = reader.readLine(); // AUTH XOAUTH2 ...
                assertThat(cmd).startsWith("AUTH XOAUTH2 ");
                writer.print("+OK\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatCode(() -> client.authXOAuth2("user@gmail.com", "ya29.token"))
                    .doesNotThrowAnyException();

            client.close();
        }

        @Test
        @DisplayName("XOAUTH2 failure with -ERR response")
        void xoauth2FailureErr() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreeting(writer);
                reader.readLine(); // AUTH XOAUTH2
                writer.print("-ERR invalid token\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.authXOAuth2("user@gmail.com", "bad-token"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("XOAUTH2 authentication failed");

            client.close();
        }

        @Test
        @DisplayName("XOAUTH2 failure with continuation challenge (+ response)")
        void xoauth2FailureContinuation() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreeting(writer);
                reader.readLine(); // AUTH XOAUTH2
                // Send continuation challenge (not +OK)
                writer.print("+ error details\r\n");
                writer.flush();
                reader.readLine(); // empty line to cancel
                writer.print("-ERR authentication failed\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.authXOAuth2("user@gmail.com", "expired"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("XOAUTH2 authentication failed");

            client.close();
        }
    }
}
