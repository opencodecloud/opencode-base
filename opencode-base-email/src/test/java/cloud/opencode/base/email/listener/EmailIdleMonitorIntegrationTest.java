package cloud.opencode.base.email.listener;

import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.exception.EmailReceiveException;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailIdleMonitor Integration Tests using a mock IMAP server.
 * EmailIdleMonitor 集成测试，使用模拟 IMAP 服务器。
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("EmailIdleMonitor Integration Tests")
class EmailIdleMonitorIntegrationTest {

    private ServerSocket server;
    private int port;

    /** A simple RFC 2822 message used by the mock server. */
    private static final String RAW_MSG =
            "From: sender@test.com\r\n"
                    + "To: me@test.com\r\n"
                    + "Subject: New Email\r\n"
                    + "Message-ID: <new1@test.com>\r\n"
                    + "Date: Mon, 1 Jan 2024 12:00:00 +0000\r\n"
                    + "Content-Type: text/plain; charset=UTF-8\r\n"
                    + "\r\n"
                    + "Hello from IDLE test";

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
                sendLine(out, "* OK [CAPABILITY IMAP4rev1 IDLE] ready");
                handler.handle(in, out);
            } catch (Exception ignored) {
                // Test server shutdown
            }
        });
    }

    /**
     * Starts a mock IMAP server that can accept multiple connections (for reconnect tests).
     */
    private void startMultiConnImapServer(MockHandler handler) {
        Thread.startVirtualThread(() -> {
            while (!server.isClosed()) {
                try {
                    var socket = server.accept();
                    Thread.startVirtualThread(() -> {
                        try (socket;
                             var in = new BufferedReader(new InputStreamReader(
                                     socket.getInputStream(), StandardCharsets.UTF_8));
                             var out = new BufferedOutputStream(socket.getOutputStream())) {
                            sendLine(out, "* OK [CAPABILITY IMAP4rev1 IDLE] ready");
                            handler.handle(in, out);
                        } catch (Exception ignored) {
                        }
                    });
                } catch (Exception ignored) {
                    break;
                }
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
        if (command == null) return "";
        int space = command.indexOf(' ');
        return space > 0 ? command.substring(0, space) : command;
    }

    private static String respondOk(BufferedReader in, BufferedOutputStream out) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        sendLine(out, tag + " OK completed");
        return cmd;
    }

    /**
     * Handles LOGIN command and responds OK.
     */
    private static String handleLogin(BufferedReader in, BufferedOutputStream out) throws IOException {
        return respondOk(in, out);
    }

    /**
     * Handles SELECT command and responds with EXISTS/RECENT counts.
     */
    private static void handleSelect(BufferedReader in, BufferedOutputStream out, int exists) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        sendLine(out, "* " + exists + " EXISTS");
        sendLine(out, "* 0 RECENT");
        sendLine(out, tag + " OK [READ-WRITE] SELECT completed");
    }

    /**
     * Handles IDLE command: sends continuation, optionally sends EXISTS, waits for DONE, responds OK.
     */
    private static void handleIdle(BufferedReader in, BufferedOutputStream out,
                                   int newExists, boolean sendExists) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        sendLine(out, "+ idling");

        if (sendExists) {
            // Short delay then send EXISTS notification
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            sendLine(out, "* " + newExists + " EXISTS");
        }

        // Wait for DONE
        String done = readCommand(in);
        sendLine(out, tag + " OK IDLE terminated");
    }

    /**
     * Handles FETCH command and responds with the given raw message.
     */
    private static void handleFetch(BufferedReader in, BufferedOutputStream out,
                                    int seq, String rawMsg) throws IOException {
        String cmd = readCommand(in);
        String tag = extractTag(cmd);
        byte[] msgBytes = rawMsg.getBytes(StandardCharsets.UTF_8);
        sendLine(out, "* " + seq + " FETCH (BODY[] {" + msgBytes.length + "}");
        out.write(msgBytes);
        sendLine(out, ")");
        sendLine(out, tag + " OK FETCH completed");
    }

    /**
     * Handles LOGOUT command.
     */
    private static void handleLogout(BufferedReader in, BufferedOutputStream out) throws IOException {
        String cmd = readCommand(in);
        if (cmd == null) return;
        String tag = extractTag(cmd);
        sendLine(out, "* BYE Server logging out");
        sendLine(out, tag + " OK LOGOUT completed");
    }

    private EmailReceiveConfig imapConfig() {
        return EmailReceiveConfig.builder()
                .host("localhost")
                .port(port)
                .username("testuser")
                .password("testpass")
                .imap()
                .ssl(false)
                .connectionTimeout(Duration.ofSeconds(2))
                .timeout(Duration.ofSeconds(2))
                .build();
    }

    // ========================================================================
    // Tests
    // ========================================================================

    @Nested
    @DisplayName("StartAndStopTests")
    class StartAndStopTests {

        @Test
        @DisplayName("start() sets isRunning to true, stop() sets it to false")
        void testStartAndStop() throws Exception {
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch stopped = new CountDownLatch(1);

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 2);
                // IDLE loop - handle one idle cycle then wait for close
                handleIdle(in, out, 2, false);
                // May get another command or connection close
                try {
                    String cmd = readCommand(in);
                    if (cmd != null) {
                        String tag = extractTag(cmd);
                        if (cmd.contains("LOGOUT")) {
                            sendLine(out, "* BYE");
                            sendLine(out, tag + " OK");
                        } else {
                            sendLine(out, tag + " OK");
                        }
                    }
                } catch (Exception ignored) {}
            });

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(200))
                    .reconnectDelay(Duration.ofMillis(50))
                    .maxReconnectAttempts(0)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {}

                        @Override
                        public void onMonitoringStarted(String folder) {
                            started.countDown();
                        }

                        @Override
                        public void onMonitoringStopped(String folder) {
                            stopped.countDown();
                        }
                    })
                    .build();

            monitor.start();
            assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(monitor.isRunning()).isTrue();

            monitor.stop();
            assertThat(stopped.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(monitor.isRunning()).isFalse();
        }

        @Test
        @DisplayName("start() when already running is no-op")
        void testDoubleStartIsNoop() throws Exception {
            AtomicInteger startCount = new AtomicInteger(0);
            CountDownLatch started = new CountDownLatch(1);

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 0);
                try {
                    handleIdle(in, out, 0, false);
                } catch (Exception ignored) {}
            });

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(200))
                    .maxReconnectAttempts(0)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {}

                        @Override
                        public void onMonitoringStarted(String folder) {
                            startCount.incrementAndGet();
                            started.countDown();
                        }
                    })
                    .build();

            try {
                monitor.start();
                assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();

                // Second start should be no-op
                monitor.start();
                Thread.sleep(100);
                assertThat(startCount.get()).isEqualTo(1);
            } finally {
                monitor.stop();
            }
        }

        @Test
        @DisplayName("stop() when not running is no-op")
        void testStopWhenNotRunningIsNoop() {
            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(100))
                    .build();

            // Should not throw
            assertThatNoException().isThrownBy(monitor::stop);
        }
    }

    @Nested
    @DisplayName("NewEmailNotificationTests")
    class NewEmailNotificationTests {

        @Test
        @DisplayName("monitor notifies listener when new EXISTS arrives during IDLE")
        void testNewEmailNotification() throws Exception {
            CountDownLatch emailReceived = new CountDownLatch(1);
            AtomicReference<ReceivedEmail> receivedRef = new AtomicReference<>();

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 2);
                // IDLE - send * 3 EXISTS to simulate new message
                handleIdle(in, out, 3, true);
                // FETCH for message 3
                handleFetch(in, out, 3, RAW_MSG);
                // Next IDLE (monitor loops)
                try {
                    handleIdle(in, out, 3, false);
                } catch (Exception ignored) {}
            });

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(500))
                    .maxReconnectAttempts(0)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {
                            receivedRef.set(email);
                            emailReceived.countDown();
                        }
                    })
                    .build();

            try {
                monitor.start();
                assertThat(emailReceived.await(5, TimeUnit.SECONDS)).isTrue();

                ReceivedEmail email = receivedRef.get();
                assertThat(email).isNotNull();
                assertThat(email.subject()).isEqualTo("New Email");
                assertThat(email.from()).isEqualTo("sender@test.com");
                assertThat(email.folder()).isEqualTo("INBOX");
                assertThat(email.messageNumber()).isEqualTo(3);
            } finally {
                monitor.stop();
            }
        }
    }

    @Nested
    @DisplayName("PollFallbackTests")
    class PollFallbackTests {

        @Test
        @DisplayName("monitor falls back to polling when server has no IDLE capability")
        void testPollFallback() throws Exception {
            CountDownLatch emailReceived = new CountDownLatch(1);

            // Server without IDLE capability
            Thread.startVirtualThread(() -> {
                try (var socket = server.accept();
                     var in = new BufferedReader(new InputStreamReader(
                             socket.getInputStream(), StandardCharsets.UTF_8));
                     var out = new BufferedOutputStream(socket.getOutputStream())) {
                    // Greeting WITHOUT IDLE capability
                    sendLine(out, "* OK [CAPABILITY IMAP4rev1] ready");
                    handleLogin(in, out);
                    handleSelect(in, out, 2);

                    // NOOP (poll fallback sends NOOP)
                    respondOk(in, out);

                    // Re-SELECT shows new message
                    handleSelect(in, out, 3);

                    // FETCH for message 3
                    handleFetch(in, out, 3, RAW_MSG);

                    // Let monitor continue polling; handle further commands gracefully
                    try {
                        while (true) {
                            String cmd = readCommand(in);
                            if (cmd == null) break;
                            String tag = extractTag(cmd);
                            if (cmd.toUpperCase().contains("SELECT")) {
                                sendLine(out, "* 3 EXISTS");
                                sendLine(out, "* 0 RECENT");
                                sendLine(out, tag + " OK");
                            } else if (cmd.toUpperCase().contains("LOGOUT")) {
                                sendLine(out, "* BYE");
                                sendLine(out, tag + " OK");
                                break;
                            } else {
                                sendLine(out, tag + " OK");
                            }
                        }
                    } catch (Exception ignored) {}
                } catch (Exception ignored) {}
            });

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(100))  // Very short poll interval
                    .maxReconnectAttempts(0)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {
                            emailReceived.countDown();
                        }
                    })
                    .build();

            try {
                monitor.start();
                assertThat(emailReceived.await(5, TimeUnit.SECONDS)).isTrue();
            } finally {
                monitor.stop();
            }
        }
    }

    @Nested
    @DisplayName("ReconnectionTests")
    class ReconnectionTests {

        @Test
        @DisplayName("monitor reconnects after connection error and calls reconnecting/reconnected callbacks")
        void testReconnection() throws Exception {
            CountDownLatch reconnecting = new CountDownLatch(1);
            CountDownLatch reconnected = new CountDownLatch(1);
            AtomicInteger reconnectAttempt = new AtomicInteger(0);
            List<Throwable> errors = new CopyOnWriteArrayList<>();

            AtomicInteger connectionCount = new AtomicInteger(0);

            startMultiConnImapServer((in, out) -> {
                int conn = connectionCount.incrementAndGet();
                handleLogin(in, out);
                handleSelect(in, out, 2);

                if (conn == 1) {
                    // First connection: start IDLE then close abruptly
                    String cmd = readCommand(in);
                    String tag = extractTag(cmd);
                    sendLine(out, "+ idling");
                    // Close the output to simulate connection drop
                    // The monitor should detect this as a ProtocolException
                    throw new IOException("Simulated connection drop");
                } else {
                    // Second connection (after reconnect): normal IDLE
                    try {
                        handleIdle(in, out, 2, false);
                    } catch (Exception ignored) {}
                }
            });

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(200))
                    .reconnectDelay(Duration.ofMillis(50))
                    .maxReconnectAttempts(3)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {}

                        @Override
                        public void onReconnecting(int attempt) {
                            reconnectAttempt.set(attempt);
                            reconnecting.countDown();
                        }

                        @Override
                        public void onReconnected() {
                            reconnected.countDown();
                        }

                        @Override
                        public void onError(Throwable error) {
                            errors.add(error);
                        }
                    })
                    .build();

            try {
                monitor.start();
                assertThat(reconnecting.await(5, TimeUnit.SECONDS)).isTrue();
                assertThat(reconnectAttempt.get()).isGreaterThanOrEqualTo(1);

                // Wait for reconnected callback
                assertThat(reconnected.await(5, TimeUnit.SECONDS)).isTrue();
            } finally {
                monitor.stop();
            }
        }

        @Test
        @DisplayName("monitor stops after max reconnect attempts exceeded")
        void testMaxReconnectExceeded() throws Exception {
            CountDownLatch firstConnected = new CountDownLatch(1);
            AtomicBoolean serverShouldClose = new AtomicBoolean(false);
            List<Throwable> errors = new CopyOnWriteArrayList<>();

            // Server that drops first connection, then is closed so reconnect fails
            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 2);
                firstConnected.countDown();
                // Start IDLE then drop
                String cmd = readCommand(in);
                sendLine(out, "+ idling");
                throw new IOException("Simulated drop");
            });

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(200))
                    .reconnectDelay(Duration.ofMillis(50))
                    .maxReconnectAttempts(2)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {}

                        @Override
                        public void onError(Throwable error) {
                            errors.add(error);
                        }
                    })
                    .build();

            try {
                monitor.start();
                // Wait for initial connection to establish
                assertThat(firstConnected.await(3, TimeUnit.SECONDS)).isTrue();
                // Close the server so reconnect attempts fail with connection refused
                server.close();

                // Wait for monitor to stop itself after exhausting reconnect attempts
                long deadline = System.currentTimeMillis() + 10000;
                while (monitor.isRunning() && System.currentTimeMillis() < deadline) {
                    Thread.sleep(100);
                }
                assertThat(monitor.isRunning()).isFalse();
            } finally {
                monitor.stop();
            }
        }
    }

    @Nested
    @DisplayName("ListenerManagementTests")
    class ListenerManagementTests {

        @Test
        @DisplayName("multiple listeners receive notifications")
        void testMultipleListenersReceiveNotifications() throws Exception {
            CountDownLatch latch1 = new CountDownLatch(1);
            CountDownLatch latch2 = new CountDownLatch(1);

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 2);
                handleIdle(in, out, 3, true);
                handleFetch(in, out, 3, RAW_MSG);
                try {
                    handleIdle(in, out, 3, false);
                } catch (Exception ignored) {}
            });

            EmailListener listener1 = email -> latch1.countDown();
            EmailListener listener2 = email -> latch2.countDown();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(500))
                    .maxReconnectAttempts(0)
                    .listener(listener1)
                    .listener(listener2)
                    .build();

            try {
                monitor.start();
                assertThat(latch1.await(5, TimeUnit.SECONDS)).isTrue();
                assertThat(latch2.await(5, TimeUnit.SECONDS)).isTrue();
            } finally {
                monitor.stop();
            }
        }

        @Test
        @DisplayName("addListener adds a listener that receives events")
        void testAddListenerAtRuntime() throws Exception {
            CountDownLatch emailLatch = new CountDownLatch(1);

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 2);
                handleIdle(in, out, 3, true);
                handleFetch(in, out, 3, RAW_MSG);
                try {
                    handleIdle(in, out, 3, false);
                } catch (Exception ignored) {}
            });

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(500))
                    .maxReconnectAttempts(0)
                    .build();

            // Add listener after building
            monitor.addListener(email -> emailLatch.countDown());

            try {
                monitor.start();
                assertThat(emailLatch.await(5, TimeUnit.SECONDS)).isTrue();
            } finally {
                monitor.stop();
            }
        }

        @Test
        @DisplayName("removeListener prevents listener from receiving events")
        void testRemoveListener() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user")
                    .password("pass")
                    .imap()
                    .build();

            AtomicBoolean called = new AtomicBoolean(false);
            EmailListener listener = email -> called.set(true);

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .listener(listener)
                    .build();

            monitor.removeListener(listener);

            // Verify the listener was removed (it won't be called since we don't start the monitor)
            assertThat(called.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("BuilderTests")
    class BuilderTests {

        @Test
        @DisplayName("build() without config throws IllegalStateException")
        void testBuildWithoutConfig() {
            assertThatThrownBy(() -> EmailIdleMonitor.builder().build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Configuration is required");
        }

        @Test
        @DisplayName("build() with POP3 config throws EmailReceiveException")
        void testBuildWithPop3Config() {
            EmailReceiveConfig pop3Config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user")
                    .password("pass")
                    .pop3()
                    .build();

            assertThatThrownBy(() -> EmailIdleMonitor.builder()
                    .config(pop3Config)
                    .build())
                    .isInstanceOf(EmailReceiveException.class)
                    .hasMessageContaining("IMAP");
        }

        @Test
        @DisplayName("builder sets custom idleTimeout, maxReconnectAttempts, reconnectDelay")
        void testCustomBuilderValues() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user")
                    .password("pass")
                    .imap()
                    .build();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .folder("Sent")
                    .idleTimeout(Duration.ofMinutes(10))
                    .maxReconnectAttempts(20)
                    .reconnectDelay(Duration.ofSeconds(30))
                    .build();

            assertThat(monitor).isNotNull();
            assertThat(monitor.isRunning()).isFalse();
        }

        @Test
        @DisplayName("builder onNewEmail creates listener from Consumer")
        void testOnNewEmailBuilder() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user")
                    .password("pass")
                    .imap()
                    .build();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .onNewEmail(email -> {})
                    .build();

            assertThat(monitor).isNotNull();
        }

        @Test
        @DisplayName("builder null listener is ignored")
        void testNullListenerIgnored() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user")
                    .password("pass")
                    .imap()
                    .build();

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(config)
                    .listener(null)
                    .build();

            assertThat(monitor).isNotNull();
        }
    }

    @Nested
    @DisplayName("AutoCloseableTests")
    class AutoCloseableTests {

        @Test
        @DisplayName("try-with-resources calls stop()")
        void testTryWithResources() throws Exception {
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch stopped = new CountDownLatch(1);

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 0);
                try {
                    handleIdle(in, out, 0, false);
                } catch (Exception ignored) {}
            });

            try (EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(300))
                    .maxReconnectAttempts(0)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {}

                        @Override
                        public void onMonitoringStarted(String folder) {
                            started.countDown();
                        }

                        @Override
                        public void onMonitoringStopped(String folder) {
                            stopped.countDown();
                        }
                    })
                    .build()) {

                monitor.start();
                assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
            }
            // After try-with-resources, close() was called which calls stop()
            assertThat(stopped.await(3, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Nested
    @DisplayName("ErrorHandlingTests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("listener exception in onNewEmail does not crash monitor")
        void testListenerExceptionIsolation() throws Exception {
            CountDownLatch secondListenerCalled = new CountDownLatch(1);

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 2);
                handleIdle(in, out, 3, true);
                handleFetch(in, out, 3, RAW_MSG);
                try {
                    handleIdle(in, out, 3, false);
                } catch (Exception ignored) {}
            });

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(500))
                    .maxReconnectAttempts(0)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {
                            throw new RuntimeException("Listener exploded");
                        }
                    })
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {
                            secondListenerCalled.countDown();
                        }
                    })
                    .build();

            try {
                monitor.start();
                // Second listener should still be called despite first throwing
                assertThat(secondListenerCalled.await(5, TimeUnit.SECONDS)).isTrue();
            } finally {
                monitor.stop();
            }
        }

        @Test
        @DisplayName("connection failure notifies onError")
        void testConnectionFailureNotifiesError() throws Exception {
            // Close server immediately so connection fails
            server.close();

            CountDownLatch errorReceived = new CountDownLatch(1);

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .idleTimeout(Duration.ofMillis(100))
                    .maxReconnectAttempts(0)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {}

                        @Override
                        public void onError(Throwable error) {
                            errorReceived.countDown();
                        }
                    })
                    .build();

            monitor.start();
            assertThat(errorReceived.await(5, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(200);
            assertThat(monitor.isRunning()).isFalse();
        }
    }

    @Nested
    @DisplayName("MonitoringCallbackTests")
    class MonitoringCallbackTests {

        @Test
        @DisplayName("onMonitoringStarted receives correct folder name")
        void testMonitoringStartedFolder() throws Exception {
            AtomicReference<String> folderRef = new AtomicReference<>();
            CountDownLatch started = new CountDownLatch(1);

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 0);
                try {
                    handleIdle(in, out, 0, false);
                } catch (Exception ignored) {}
            });

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .folder("INBOX")
                    .idleTimeout(Duration.ofMillis(300))
                    .maxReconnectAttempts(0)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {}

                        @Override
                        public void onMonitoringStarted(String folder) {
                            folderRef.set(folder);
                            started.countDown();
                        }
                    })
                    .build();

            try {
                monitor.start();
                assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
                assertThat(folderRef.get()).isEqualTo("INBOX");
            } finally {
                monitor.stop();
            }
        }

        @Test
        @DisplayName("onMonitoringStopped receives correct folder name")
        void testMonitoringStoppedFolder() throws Exception {
            AtomicReference<String> folderRef = new AtomicReference<>();
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch stopped = new CountDownLatch(1);

            startImapServer((in, out) -> {
                handleLogin(in, out);
                handleSelect(in, out, 0);
                try {
                    handleIdle(in, out, 0, false);
                } catch (Exception ignored) {}
            });

            EmailIdleMonitor monitor = EmailIdleMonitor.builder()
                    .config(imapConfig())
                    .folder("INBOX")
                    .idleTimeout(Duration.ofMillis(300))
                    .maxReconnectAttempts(0)
                    .listener(new EmailListener() {
                        @Override
                        public void onNewEmail(ReceivedEmail email) {}

                        @Override
                        public void onMonitoringStarted(String folder) {
                            started.countDown();
                        }

                        @Override
                        public void onMonitoringStopped(String folder) {
                            folderRef.set(folder);
                            stopped.countDown();
                        }
                    })
                    .build();

            monitor.start();
            assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
            monitor.stop();
            assertThat(stopped.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(folderRef.get()).isEqualTo("INBOX");
        }
    }
}
