package cloud.opencode.base.email.protocol;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * MailConnection unit tests using mock ServerSocket
 * MailConnection 使用模拟 ServerSocket 的单元测试
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("MailConnection Tests")
class MailConnectionTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorValidationTests {

        @Test
        @DisplayName("null host throws IllegalArgumentException")
        void nullHost() {
            assertThatThrownBy(() -> new MailConnection(null, 25, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("blank host throws IllegalArgumentException")
        void blankHost() {
            assertThatThrownBy(() -> new MailConnection("  ", 25, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("port 0 throws IllegalArgumentException")
        void portZero() {
            assertThatThrownBy(() -> new MailConnection("localhost", 0, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("port -1 throws IllegalArgumentException")
        void portNegative() {
            assertThatThrownBy(() -> new MailConnection("localhost", -1, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("port 65536 throws IllegalArgumentException")
        void portTooLarge() {
            assertThatThrownBy(() -> new MailConnection("localhost", 65536, false, TIMEOUT, TIMEOUT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null connectionTimeout throws NullPointerException")
        void nullConnectionTimeout() {
            assertThatThrownBy(() -> new MailConnection("localhost", 25, false, null, TIMEOUT))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null readTimeout throws NullPointerException")
        void nullReadTimeout() {
            assertThatThrownBy(() -> new MailConnection("localhost", 25, false, TIMEOUT, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("valid params do not throw")
        void validParams() {
            assertThatCode(() -> new MailConnection("localhost", 25, false, TIMEOUT, TIMEOUT))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Connect Tests")
    class ConnectTests {

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

        @Test
        @DisplayName("connect succeeds and isConnected returns true")
        void connectSucceeds() throws Exception {
            var ready = new CountDownLatch(1);
            Thread.startVirtualThread(() -> {
                try (Socket client = serverSocket.accept()) {
                    ready.countDown();
                    // Keep connection alive until test finishes
                    Thread.sleep(2000);
                } catch (Exception ignored) {
                }
            });

            var conn = new MailConnection("localhost", port, false, TIMEOUT, TIMEOUT);
            conn.connect();
            ready.await(5, TimeUnit.SECONDS);

            assertThat(conn.isConnected()).isTrue();
            assertThat(conn.getHost()).isEqualTo("localhost");
            assertThat(conn.getPort()).isEqualTo(port);

            conn.close();
        }

        @Test
        @DisplayName("readLine returns server greeting")
        void readLineReturnsGreeting() throws Exception {
            Thread.startVirtualThread(() -> {
                try (Socket client = serverSocket.accept();
                     var writer = new PrintWriter(client.getOutputStream(), true)) {
                    writer.print("220 test.server ESMTP\r\n");
                    writer.flush();
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            });

            var conn = new MailConnection("localhost", port, false, TIMEOUT, TIMEOUT);
            conn.connect();

            String line = conn.readLine();
            assertThat(line).isEqualTo("220 test.server ESMTP");

            conn.close();
        }

        @Test
        @DisplayName("writeLine sends data to server")
        void writeLineSendsData() throws Exception {
            var receivedLine = new String[1];
            var latch = new CountDownLatch(1);

            Thread.startVirtualThread(() -> {
                try (Socket client = serverSocket.accept();
                     var reader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                    receivedLine[0] = reader.readLine();
                    latch.countDown();
                } catch (Exception ignored) {
                }
            });

            var conn = new MailConnection("localhost", port, false, TIMEOUT, TIMEOUT);
            conn.connect();
            conn.writeLine("EHLO localhost");

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(receivedLine[0]).isEqualTo("EHLO localhost");

            conn.close();
        }
    }

    @Nested
    @DisplayName("readExact Tests")
    class ReadExactTests {

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

        @Test
        @DisplayName("readExact(5) returns exactly 5 characters")
        void readExactFive() throws Exception {
            Thread.startVirtualThread(() -> {
                try (Socket client = serverSocket.accept()) {
                    client.getOutputStream().write("HELLO".getBytes());
                    client.getOutputStream().flush();
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            });

            var conn = new MailConnection("localhost", port, false, TIMEOUT, TIMEOUT);
            conn.connect();

            String data = conn.readExact(5);
            assertThat(data).isEqualTo("HELLO");

            conn.close();
        }

        @Test
        @DisplayName("readExact(0) returns empty string")
        void readExactZero() throws Exception {
            Thread.startVirtualThread(() -> {
                try (Socket client = serverSocket.accept()) {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            });

            var conn = new MailConnection("localhost", port, false, TIMEOUT, TIMEOUT);
            conn.connect();

            String data = conn.readExact(0);
            assertThat(data).isEmpty();

            conn.close();
        }
    }

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

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

        @Test
        @DisplayName("close sets isConnected to false")
        void closeSetsNotConnected() throws Exception {
            Thread.startVirtualThread(() -> {
                try (Socket client = serverSocket.accept()) {
                    Thread.sleep(2000);
                } catch (Exception ignored) {
                }
            });

            var conn = new MailConnection("localhost", port, false, TIMEOUT, TIMEOUT);
            conn.connect();
            assertThat(conn.isConnected()).isTrue();

            conn.close();
            assertThat(conn.isConnected()).isFalse();
        }

        @Test
        @DisplayName("double close does not throw")
        void doubleCloseNoException() throws Exception {
            Thread.startVirtualThread(() -> {
                try (Socket client = serverSocket.accept()) {
                    Thread.sleep(2000);
                } catch (Exception ignored) {
                }
            });

            var conn = new MailConnection("localhost", port, false, TIMEOUT, TIMEOUT);
            conn.connect();

            assertThatCode(() -> {
                conn.close();
                conn.close();
            }).doesNotThrowAnyException();
        }
    }
}
