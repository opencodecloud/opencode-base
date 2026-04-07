package cloud.opencode.base.email.protocol.smtp;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * SmtpClient unit tests using mock ServerSocket
 * SmtpClient 使用模拟 ServerSocket 的单元测试
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("SmtpClient Tests")
class SmtpClientTest {

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

    /**
     * Start a mock SMTP server that handles one connection.
     * The handler receives the reader/writer to script the protocol exchange.
     */
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

    @FunctionalInterface
    interface MockServerHandler {
        void handle(BufferedReader reader, PrintWriter writer) throws Exception;
    }

    /**
     * Send the standard SMTP greeting + EHLO response.
     * Responds to one EHLO command with capabilities.
     */
    private void sendGreetingAndEhlo(BufferedReader reader, PrintWriter writer,
                                     String... capabilities) throws Exception {
        // Send 220 greeting
        writer.print("220 smtp.test.com ESMTP\r\n");
        writer.flush();

        // Read EHLO command
        String ehlo = reader.readLine();
        assertThat(ehlo).startsWith("EHLO ");

        // Send multi-line 250 response with capabilities
        if (capabilities.length == 0) {
            writer.print("250 smtp.test.com\r\n");
        } else {
            writer.print("250-smtp.test.com\r\n");
            for (int i = 0; i < capabilities.length; i++) {
                if (i == capabilities.length - 1) {
                    writer.print("250 " + capabilities[i] + "\r\n");
                } else {
                    writer.print("250-" + capabilities[i] + "\r\n");
                }
            }
        }
        writer.flush();
    }

    private SmtpClient createClient() {
        return new SmtpClient("localhost", port, false, false, TIMEOUT, TIMEOUT);
    }

    @Nested
    @DisplayName("Connect Tests")
    class ConnectTests {

        @Test
        @DisplayName("connect returns greeting and parses capabilities")
        void connectReturnsGreeting() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH PLAIN LOGIN", "STARTTLS", "SIZE 10485760");
                // Keep alive briefly
                Thread.sleep(500);
            });

            var client = createClient();
            String greeting = client.connect();

            assertThat(greeting).contains("smtp.test.com");
            assertThat(client.isConnected()).isTrue();
            assertThat(client.hasCapability("AUTH")).isTrue();
            assertThat(client.hasCapability("STARTTLS")).isTrue();
            assertThat(client.hasCapability("SIZE")).isTrue();
            assertThat(client.getCapabilities()).isNotEmpty();

            client.close();
        }

        @Test
        @DisplayName("connect with no capabilities succeeds")
        void connectNoCapabilities() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer);
                Thread.sleep(500);
            });

            var client = createClient();
            String greeting = client.connect();

            assertThat(greeting).contains("smtp.test.com");
            assertThat(client.isConnected()).isTrue();

            client.close();
        }
    }

    @Nested
    @DisplayName("AUTH PLAIN Tests")
    class AuthPlainTests {

        @Test
        @DisplayName("successful AUTH PLAIN")
        void successfulAuthPlain() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH PLAIN LOGIN");
                // Read AUTH PLAIN command
                String authLine = reader.readLine();
                assertThat(authLine).startsWith("AUTH PLAIN ");
                writer.print("235 2.7.0 Authentication successful\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();
            assertThatCode(() -> client.authPlain("user@test.com", "password123"))
                    .doesNotThrowAnyException();

            client.close();
        }

        @Test
        @DisplayName("AUTH PLAIN failure with 535 reply")
        void authPlainFailure535() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH PLAIN LOGIN");
                reader.readLine(); // AUTH PLAIN
                writer.print("535 5.7.8 Authentication credentials invalid\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.authPlain("user@test.com", "wrong"))
                    .isInstanceOf(ProtocolException.class)
                    .satisfies(ex -> {
                        var pe = (ProtocolException) ex;
                        assertThat(pe.getReplyCode()).isEqualTo(535);
                        assertThat(pe.isAuthenticationFailure()).isTrue();
                    });

            client.close();
        }

        @Test
        @DisplayName("CRLF in username rejected before sending")
        void crlfInUsernameRejected() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH PLAIN");
                Thread.sleep(1000);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.authPlain("user\r\n@test.com", "pass"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("Invalid character");

            client.close();
        }

        @Test
        @DisplayName("null byte in password rejected before sending")
        void nullByteInPasswordRejected() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH PLAIN");
                Thread.sleep(1000);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.authPlain("user@test.com", "pass\0word"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("Invalid character");

            client.close();
        }

        @Test
        @DisplayName("null username throws NullPointerException")
        void nullUsername() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH PLAIN");
                Thread.sleep(1000);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.authPlain(null, "pass"))
                    .isInstanceOf(NullPointerException.class);

            client.close();
        }
    }

    @Nested
    @DisplayName("AUTH LOGIN Tests")
    class AuthLoginTests {

        @Test
        @DisplayName("successful AUTH LOGIN handshake")
        void successfulAuthLogin() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH LOGIN PLAIN");
                // AUTH LOGIN
                String authCmd = reader.readLine();
                assertThat(authCmd).isEqualTo("AUTH LOGIN");
                writer.print("334 VXNlcm5hbWU6\r\n"); // Username:
                writer.flush();

                // Base64 username
                String b64User = reader.readLine();
                assertThat(b64User).isNotEmpty();
                writer.print("334 UGFzc3dvcmQ6\r\n"); // Password:
                writer.flush();

                // Base64 password
                String b64Pass = reader.readLine();
                assertThat(b64Pass).isNotEmpty();
                writer.print("235 2.7.0 Authentication successful\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatCode(() -> client.authLogin("user@test.com", "secret"))
                    .doesNotThrowAnyException();

            client.close();
        }

        @Test
        @DisplayName("AUTH LOGIN failure at password step")
        void authLoginFailureAtPassword() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH LOGIN");
                reader.readLine(); // AUTH LOGIN
                writer.print("334 VXNlcm5hbWU6\r\n");
                writer.flush();
                reader.readLine(); // username
                writer.print("334 UGFzc3dvcmQ6\r\n");
                writer.flush();
                reader.readLine(); // password
                writer.print("535 5.7.8 Authentication failed\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.authLogin("user@test.com", "wrong"))
                    .isInstanceOf(ProtocolException.class);

            client.close();
        }
    }

    @Nested
    @DisplayName("AUTH XOAUTH2 Tests")
    class AuthXOAuth2Tests {

        @Test
        @DisplayName("successful AUTH XOAUTH2")
        void successfulXOAuth2() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH XOAUTH2");
                String authCmd = reader.readLine();
                assertThat(authCmd).startsWith("AUTH XOAUTH2 ");
                writer.print("235 2.7.0 Accepted\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatCode(() -> client.authXOAuth2("user@gmail.com", "ya29.token123"))
                    .doesNotThrowAnyException();

            client.close();
        }

        @Test
        @DisplayName("AUTH XOAUTH2 failure with 334 error then cancel")
        void xoauth2FailureWith334() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH XOAUTH2");
                reader.readLine(); // AUTH XOAUTH2
                // Server sends 334 with error details
                writer.print("334 eyJlcnJvciI6ImludmFsaWRfZ3JhbnQifQ==\r\n");
                writer.flush();
                // Client sends empty line to cancel
                reader.readLine();
                writer.print("535 5.7.8 Authentication failed\r\n");
                writer.flush();
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.authXOAuth2("user@gmail.com", "expired-token"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("XOAUTH2 authentication failed");

            client.close();
        }
    }

    @Nested
    @DisplayName("Send Message Tests")
    class SendMessageTests {

        @Test
        @DisplayName("send simple message with single recipient")
        void sendSimpleMessage() throws Exception {
            var receivedLines = new ArrayList<String>();
            var latch = new CountDownLatch(1);

            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH PLAIN", "SIZE 10485760");
                // MAIL FROM
                String mailFrom = reader.readLine();
                receivedLines.add(mailFrom);
                writer.print("250 2.1.0 OK\r\n");
                writer.flush();
                // RCPT TO
                String rcptTo = reader.readLine();
                receivedLines.add(rcptTo);
                writer.print("250 2.1.5 OK\r\n");
                writer.flush();
                // DATA
                String data = reader.readLine();
                receivedLines.add(data);
                writer.print("354 Start mail input\r\n");
                writer.flush();
                // Read message body until "."
                String line;
                while ((line = reader.readLine()) != null) {
                    receivedLines.add(line);
                    if (".".equals(line)) break;
                }
                writer.print("250 2.0.0 OK\r\n");
                writer.flush();
                latch.countDown();
            });

            var client = createClient();
            client.connect();
            client.sendMessage("sender@test.com",
                    List.of("rcpt@test.com"),
                    "Subject: Test\r\n\r\nHello World");

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(receivedLines.get(0)).isEqualTo("MAIL FROM:<sender@test.com>");
            assertThat(receivedLines.get(1)).isEqualTo("RCPT TO:<rcpt@test.com>");
            assertThat(receivedLines.get(2)).isEqualTo("DATA");

            client.close();
        }

        @Test
        @DisplayName("dot-stuffing: line starting with '.' is doubled")
        void dotStuffing() throws Exception {
            var receivedLines = new ArrayList<String>();
            var latch = new CountDownLatch(1);

            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer);
                // MAIL FROM
                reader.readLine();
                writer.print("250 OK\r\n");
                writer.flush();
                // RCPT TO
                reader.readLine();
                writer.print("250 OK\r\n");
                writer.flush();
                // DATA
                reader.readLine();
                writer.print("354 Go ahead\r\n");
                writer.flush();
                // Read body
                String line;
                while ((line = reader.readLine()) != null) {
                    receivedLines.add(line);
                    if (".".equals(line)) break;
                }
                writer.print("250 OK\r\n");
                writer.flush();
                latch.countDown();
            });

            var client = createClient();
            client.connect();
            // Message with a line starting with "." should be dot-stuffed
            client.sendMessage("a@b.com", List.of("c@d.com"),
                    "Subject: Test\r\n\r\n.hidden line\r\nnormal line");

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            // The body lines (after DATA) should have ".." for the dot-stuffed line
            assertThat(receivedLines).contains("..hidden line");

            client.close();
        }

        @Test
        @DisplayName("multiple recipients each get RCPT TO")
        void multipleRecipients() throws Exception {
            var rcptLines = new ArrayList<String>();
            var latch = new CountDownLatch(1);

            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer);
                // MAIL FROM
                reader.readLine();
                writer.print("250 OK\r\n");
                writer.flush();
                // RCPT TO x3
                for (int i = 0; i < 3; i++) {
                    String rcpt = reader.readLine();
                    rcptLines.add(rcpt);
                    writer.print("250 OK\r\n");
                    writer.flush();
                }
                // DATA
                reader.readLine();
                writer.print("354 Go ahead\r\n");
                writer.flush();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (".".equals(line)) break;
                }
                writer.print("250 OK\r\n");
                writer.flush();
                latch.countDown();
            });

            var client = createClient();
            client.connect();
            client.sendMessage("from@test.com",
                    List.of("a@test.com", "b@test.com", "c@test.com"),
                    "Subject: Multi\r\n\r\nBody");

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(rcptLines).containsExactly(
                    "RCPT TO:<a@test.com>",
                    "RCPT TO:<b@test.com>",
                    "RCPT TO:<c@test.com>");

            client.close();
        }

        @Test
        @DisplayName("empty recipients list throws IllegalArgumentException")
        void emptyRecipients() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer);
                Thread.sleep(1000);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() ->
                    client.sendMessage("a@b.com", List.of(), "body"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("recipients");

            client.close();
        }
    }

    @Nested
    @DisplayName("Validate Envelope Address Tests")
    class ValidateEnvelopeAddressTests {

        @Test
        @DisplayName("\\r in sender address rejected")
        void crInSender() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer);
                Thread.sleep(1000);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() ->
                    client.sendMessage("bad\r@test.com", List.of("ok@test.com"), "body"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("Invalid character");

            client.close();
        }

        @Test
        @DisplayName("\\n in recipient address rejected")
        void lfInRecipient() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer);
                Thread.sleep(1000);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() ->
                    client.sendMessage("ok@test.com", List.of("bad\n@test.com"), "body"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("Invalid character");

            client.close();
        }

        @Test
        @DisplayName("\\0 in sender address rejected")
        void nullByteInSender() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer);
                Thread.sleep(1000);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() ->
                    client.sendMessage("bad\0@test.com", List.of("ok@test.com"), "body"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("Invalid character");

            client.close();
        }

        @Test
        @DisplayName("blank sender address rejected")
        void blankSender() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer);
                Thread.sleep(1000);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() ->
                    client.sendMessage("  ", List.of("ok@test.com"), "body"))
                    .isInstanceOf(ProtocolException.class)
                    .hasMessageContaining("blank");

            client.close();
        }
    }

    @Nested
    @DisplayName("Quit Tests")
    class QuitTests {

        @Test
        @DisplayName("quit sends QUIT and accepts 221")
        void quitSucceeds() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer);
                String quitCmd = reader.readLine();
                assertThat(quitCmd).isEqualTo("QUIT");
                writer.print("221 2.0.0 Bye\r\n");
                writer.flush();
            });

            var client = createClient();
            client.connect();

            assertThatCode(client::quit).doesNotThrowAnyException();
            assertThat(client.isConnected()).isFalse();
        }

        @Test
        @DisplayName("quit on disconnected client is a no-op")
        void quitWhenDisconnected() {
            var client = createClient();
            assertThatCode(client::quit).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

        @Test
        @DisplayName("close on connected client sends QUIT gracefully")
        void closeOnConnected() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer);
                // Read QUIT from close()
                String quitCmd = reader.readLine();
                writer.print("221 Bye\r\n");
                writer.flush();
            });

            var client = createClient();
            client.connect();

            assertThatCode(client::close).doesNotThrowAnyException();
            assertThat(client.isConnected()).isFalse();
        }

        @Test
        @DisplayName("close on unconnected client is safe")
        void closeOnUnconnected() {
            var client = createClient();
            assertThatCode(client::close).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorValidationTests {

        @Test
        @DisplayName("null host throws NullPointerException")
        void nullHost() {
            assertThatThrownBy(() ->
                    new SmtpClient(null, 25, false, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("port 0 throws IllegalArgumentException")
        void portZero() {
            assertThatThrownBy(() ->
                    new SmtpClient("localhost", 0, false, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("port 65536 throws IllegalArgumentException")
        void portTooLarge() {
            assertThatThrownBy(() ->
                    new SmtpClient("localhost", 65536, false, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null connectionTimeout throws NullPointerException")
        void nullConnectionTimeout() {
            assertThatThrownBy(() ->
                    new SmtpClient("localhost", 25, false, false, null, TIMEOUT))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null readTimeout throws NullPointerException")
        void nullReadTimeout() {
            assertThatThrownBy(() ->
                    new SmtpClient("localhost", 25, false, false, TIMEOUT, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Capability Tests")
    class CapabilityTests {

        @Test
        @DisplayName("hasCapability is case-insensitive")
        void caseInsensitive() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH PLAIN LOGIN", "STARTTLS");
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThat(client.hasCapability("auth")).isTrue();
            assertThat(client.hasCapability("Auth")).isTrue();
            assertThat(client.hasCapability("starttls")).isTrue();
            assertThat(client.hasCapability("NONEXISTENT")).isFalse();

            client.close();
        }

        @Test
        @DisplayName("getCapabilities returns unmodifiable set")
        void unmodifiable() throws Exception {
            startMockServer((reader, writer) -> {
                sendGreetingAndEhlo(reader, writer, "AUTH PLAIN");
                Thread.sleep(500);
            });

            var client = createClient();
            client.connect();

            assertThatThrownBy(() -> client.getCapabilities().add("FAKE"))
                    .isInstanceOf(UnsupportedOperationException.class);

            client.close();
        }
    }
}
