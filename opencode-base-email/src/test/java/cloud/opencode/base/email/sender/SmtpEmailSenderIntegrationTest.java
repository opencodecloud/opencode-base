package cloud.opencode.base.email.sender;

import cloud.opencode.base.email.BatchSendResult;
import cloud.opencode.base.email.ConnectionTestResult;
import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.EmailConfig;
import cloud.opencode.base.email.SendResult;
import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import cloud.opencode.base.email.exception.EmailSendException;
import cloud.opencode.base.email.security.DkimConfig;
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
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SmtpEmailSender integration tests using a mock SMTP server
 * SmtpEmailSender 集成测试（使用模拟SMTP服务器）
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("SmtpEmailSender 集成测试")
class SmtpEmailSenderIntegrationTest {

    private ServerSocket smtpServer;
    private int smtpPort;

    @BeforeEach
    void setUp() throws Exception {
        smtpServer = new ServerSocket(0);
        smtpPort = smtpServer.getLocalPort();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (smtpServer != null && !smtpServer.isClosed()) {
            smtpServer.close();
        }
    }

    /**
     * Functional interface for mock SMTP handlers
     */
    @FunctionalInterface
    interface MockSmtpHandler {
        void handle(BufferedReader reader, PrintWriter writer) throws Exception;
    }

    /**
     * Start a mock SMTP server on a virtual thread that accepts one connection.
     * Sends the 220 greeting then delegates to the handler.
     */
    private void startMockSmtp(MockSmtpHandler handler) {
        Thread.startVirtualThread(() -> {
            try (Socket socket = smtpServer.accept();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                writer.println("220 mock-smtp ready");
                writer.flush();
                handler.handle(reader, writer);
            } catch (Exception ignored) {
                // intentionally ignored - test controls lifecycle
            }
        });
    }

    /**
     * Start a mock SMTP server that accepts multiple sequential connections.
     * Each connection gets the same handler. The latch counts down after
     * the specified number of connections have been handled.
     */
    private void startMockSmtpMulti(MockSmtpHandler handler, int connections, CountDownLatch latch) {
        Thread.startVirtualThread(() -> {
            for (int i = 0; i < connections; i++) {
                try (Socket socket = smtpServer.accept();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                    writer.println("220 mock-smtp ready");
                    writer.flush();
                    handler.handle(reader, writer);
                } catch (Exception ignored) {
                    // intentionally ignored
                }
            }
            latch.countDown();
        });
    }

    /**
     * Standard successful SMTP conversation handler.
     * Handles EHLO, AUTH PLAIN, MAIL FROM, RCPT TO, DATA, QUIT.
     * Captures all RCPT TO addresses and the full DATA body for verification.
     */
    private static MockSmtpHandler successHandler(List<String> capturedRcptTo,
                                                   List<String> capturedDataLines) {
        return (reader, writer) -> {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("EHLO")) {
                    writer.println("250-mock-smtp");
                    writer.println("250-AUTH PLAIN LOGIN XOAUTH2");
                    writer.println("250-SIZE 10485760");
                    writer.println("250 OK");
                } else if (line.startsWith("AUTH PLAIN")) {
                    writer.println("235 2.7.0 Authentication successful");
                } else if (line.startsWith("AUTH XOAUTH2")) {
                    writer.println("235 2.7.0 Authentication successful");
                } else if (line.startsWith("MAIL FROM:")) {
                    writer.println("250 2.1.0 OK");
                } else if (line.startsWith("RCPT TO:")) {
                    if (capturedRcptTo != null) {
                        capturedRcptTo.add(line);
                    }
                    writer.println("250 2.1.5 OK");
                } else if (line.equals("DATA")) {
                    writer.println("354 Go ahead");
                    // Read data lines until lone "."
                    String dataLine;
                    while ((dataLine = reader.readLine()) != null) {
                        if (dataLine.equals(".")) {
                            break;
                        }
                        if (capturedDataLines != null) {
                            capturedDataLines.add(dataLine);
                        }
                    }
                    writer.println("250 2.0.0 OK");
                } else if (line.equals("QUIT")) {
                    writer.println("221 2.0.0 Bye");
                    break;
                }
            }
        };
    }

    /**
     * Create a basic EmailConfig pointing to the mock SMTP server.
     */
    private EmailConfig basicConfig() {
        return EmailConfig.builder()
                .host("localhost")
                .port(smtpPort)
                .ssl(false)
                .starttls(false)
                .username("testuser@example.com")
                .password("testpassword")
                .defaultFrom("sender@example.com", "Test Sender")
                .timeout(Duration.ofSeconds(5))
                .connectionTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Create a basic text email.
     */
    private Email simpleTextEmail() {
        return Email.builder()
                .to("recipient@example.com")
                .subject("Test Subject")
                .text("Hello, World!")
                .build();
    }

    @Nested
    @DisplayName("SendWithResult 集成测试")
    class SendWithResultTests {

        @Test
        @DisplayName("发送简单文本邮件，验证 SendResult.success() 和 messageId")
        void testSendSimpleTextEmail() throws Exception {
            List<String> dataLines = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(null, dataLines));

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            SendResult result = sender.sendWithResult(simpleTextEmail());

            assertThat(result.success()).isTrue();
            assertThat(result.hasMessageId()).isTrue();
            assertThat(result.messageId()).isNotBlank();
            assertThat(result.sentAt()).isNotNull();

            // Verify DATA contained the subject
            String dataContent = String.join("\n", dataLines);
            assertThat(dataContent).contains("Subject: Test Subject");
            assertThat(dataContent).contains("Hello, World!");
        }

        @Test
        @DisplayName("发送HTML邮件")
        void testSendHtmlEmail() throws Exception {
            List<String> dataLines = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(null, dataLines));

            Email htmlEmail = Email.builder()
                    .to("recipient@example.com")
                    .subject("HTML Test")
                    .html("<html><body><h1>Hello</h1></body></html>")
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            SendResult result = sender.sendWithResult(htmlEmail);

            assertThat(result.success()).isTrue();

            String dataContent = String.join("\n", dataLines);
            assertThat(dataContent).contains("Content-Type: text/html");
            assertThat(dataContent).contains("<h1>Hello</h1>");
        }

        @Test
        @DisplayName("发送带附件的邮件，验证 MIME boundary 在 DATA 中")
        void testSendEmailWithAttachment() throws Exception {
            List<String> dataLines = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(null, dataLines));

            ByteArrayAttachment attachment = new ByteArrayAttachment(
                    "test.txt", "attachment content".getBytes(StandardCharsets.UTF_8), "text/plain");

            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("With Attachment")
                    .text("See attached")
                    .attach(attachment)
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();

            String dataContent = String.join("\n", dataLines);
            // Multipart message should contain boundary markers
            assertThat(dataContent).contains("Content-Type: multipart/mixed");
            assertThat(dataContent).contains("test.txt");
        }

        @Test
        @DisplayName("发送带CC和BCC的邮件，验证所有 RCPT TO 命令")
        void testSendEmailWithCcAndBcc() throws Exception {
            List<String> capturedRcptTo = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(capturedRcptTo, null));

            Email email = Email.builder()
                    .to("to@example.com")
                    .cc("cc1@example.com", "cc2@example.com")
                    .bcc("bcc@example.com")
                    .subject("Multi-Recipient Test")
                    .text("Hello everyone")
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();

            // Should have 4 RCPT TO commands: 1 TO + 2 CC + 1 BCC
            assertThat(capturedRcptTo).hasSize(4);
            assertThat(capturedRcptTo).anySatisfy(r -> assertThat(r).contains("to@example.com"));
            assertThat(capturedRcptTo).anySatisfy(r -> assertThat(r).contains("cc1@example.com"));
            assertThat(capturedRcptTo).anySatisfy(r -> assertThat(r).contains("cc2@example.com"));
            assertThat(capturedRcptTo).anySatisfy(r -> assertThat(r).contains("bcc@example.com"));
        }

        @Test
        @DisplayName("发送带自定义邮件头的邮件")
        void testSendEmailWithCustomHeaders() throws Exception {
            List<String> dataLines = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(null, dataLines));

            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("Custom Header Test")
                    .text("Content")
                    .header("X-Custom-Id", "12345")
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();

            String dataContent = String.join("\n", dataLines);
            assertThat(dataContent).contains("X-Custom-Id: 12345");
        }

        @Test
        @DisplayName("发送高优先级邮件")
        void testSendHighPriorityEmail() throws Exception {
            List<String> dataLines = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(null, dataLines));

            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("Urgent")
                    .text("Important message")
                    .priority(Email.Priority.HIGH)
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();

            String dataContent = String.join("\n", dataLines);
            assertThat(dataContent).contains("X-Priority: 1");
        }

        @Test
        @DisplayName("使用 send() 方法发送邮件（无返回值）")
        void testSendVoidMethod() throws Exception {
            startMockSmtp(successHandler(null, null));

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            // send() should not throw
            sender.send(simpleTextEmail());
        }
    }

    @Nested
    @DisplayName("SendBatch 集成测试")
    class SendBatchTests {

        @Test
        @DisplayName("批量发送3封邮件通过单连接")
        void testSendBatchOfThree() throws Exception {
            List<String> capturedRcptTo = new CopyOnWriteArrayList<>();
            // The batch uses a single connection, so one accept is enough
            startMockSmtp(successHandler(capturedRcptTo, null));

            List<Email> emails = List.of(
                    Email.builder().to("user1@example.com").subject("Batch 1").text("Content 1").build(),
                    Email.builder().to("user2@example.com").subject("Batch 2").text("Content 2").build(),
                    Email.builder().to("user3@example.com").subject("Batch 3").text("Content 3").build()
            );

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            BatchSendResult result = sender.sendBatch(emails);

            assertThat(result.totalCount()).isEqualTo(3);
            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.failureCount()).isEqualTo(0);
            assertThat(result.allSucceeded()).isTrue();
            assertThat(result.duration()).isNotNull();
            assertThat(result.startedAt()).isNotNull();

            // Verify all 3 had RCPT TO
            assertThat(capturedRcptTo).hasSize(3);
            assertThat(capturedRcptTo).anySatisfy(r -> assertThat(r).contains("user1@example.com"));
            assertThat(capturedRcptTo).anySatisfy(r -> assertThat(r).contains("user2@example.com"));
            assertThat(capturedRcptTo).anySatisfy(r -> assertThat(r).contains("user3@example.com"));
        }

        @Test
        @DisplayName("批量发送结果包含每封邮件的 messageId")
        void testBatchResultsHaveMessageIds() throws Exception {
            startMockSmtp(successHandler(null, null));

            List<Email> emails = List.of(
                    Email.builder().to("a@example.com").subject("A").text("A").build(),
                    Email.builder().to("b@example.com").subject("B").text("B").build()
            );

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            BatchSendResult result = sender.sendBatch(emails);

            assertThat(result.successes()).hasSize(2);
            for (BatchSendResult.ItemResult item : result.successes()) {
                assertThat(item.success()).isTrue();
                assertThat(item.messageId()).isNotBlank();
                assertThat(item.error()).isNull();
                assertThat(item.cause()).isNull();
            }
        }

        @Test
        @DisplayName("批量发送空列表返回空结果")
        void testSendBatchEmpty() {
            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            BatchSendResult result = sender.sendBatch(List.of());

            assertThat(result.totalCount()).isZero();
            assertThat(result.successCount()).isZero();
            assertThat(result.allSucceeded()).isTrue();
        }

        @Test
        @DisplayName("批量发送null列表返回空结果")
        void testSendBatchNull() {
            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            BatchSendResult result = sender.sendBatch(null);

            assertThat(result.totalCount()).isZero();
        }
    }

    @Nested
    @DisplayName("TestConnection 集成测试")
    class TestConnectionTests {

        @Test
        @DisplayName("测试连接到模拟服务器 - 成功")
        void testConnectionSuccess() throws Exception {
            startMockSmtp((reader, writer) -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250 OK");
                    } else if (line.startsWith("AUTH PLAIN")) {
                        writer.println("235 2.7.0 Authentication successful");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            ConnectionTestResult result = sender.testConnection();

            assertThat(result.success()).isTrue();
            assertThat(result.latency()).isNotNull();
            assertThat(result.latency().toNanos()).isPositive();
            assertThat(result.serverGreeting())
                    .contains("localhost")
                    .contains(String.valueOf(smtpPort));
            assertThat(result.errorMessage()).isNull();
            assertThat(result.cause()).isNull();
        }

        @Test
        @DisplayName("测试连接到模拟服务器（无需认证）")
        void testConnectionWithoutAuth() throws Exception {
            startMockSmtp((reader, writer) -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250 OK");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            EmailConfig noAuthConfig = EmailConfig.builder()
                    .host("localhost")
                    .port(smtpPort)
                    .ssl(false)
                    .starttls(false)
                    .timeout(Duration.ofSeconds(5))
                    .connectionTimeout(Duration.ofSeconds(5))
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(noAuthConfig);
            ConnectionTestResult result = sender.testConnection();

            assertThat(result.success()).isTrue();
            assertThat(result.latency().toNanos()).isPositive();
        }

        @Test
        @DisplayName("测试连接到不存在的服务器 - 失败")
        void testConnectionFailure() throws Exception {
            // Close the server so nothing is listening
            smtpServer.close();

            EmailConfig config = EmailConfig.builder()
                    .host("localhost")
                    .port(smtpPort)
                    .ssl(false)
                    .starttls(false)
                    .timeout(Duration.ofSeconds(2))
                    .connectionTimeout(Duration.ofSeconds(2))
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);
            ConnectionTestResult result = sender.testConnection();

            assertThat(result.success()).isFalse();
            assertThat(result.latency()).isNotNull();
            assertThat(result.errorMessage()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("Authentication 集成测试")
    class AuthenticationTests {

        @Test
        @DisplayName("使用 OAuth2 令牌认证（模拟接受 XOAUTH2）")
        void testOAuth2Authentication() throws Exception {
            List<String> capturedAuthCommands = new CopyOnWriteArrayList<>();

            startMockSmtp((reader, writer) -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250-AUTH PLAIN LOGIN XOAUTH2");
                        writer.println("250 OK");
                    } else if (line.startsWith("AUTH XOAUTH2")) {
                        capturedAuthCommands.add(line);
                        writer.println("235 2.7.0 Authentication successful");
                    } else if (line.startsWith("MAIL FROM:")) {
                        writer.println("250 2.1.0 OK");
                    } else if (line.startsWith("RCPT TO:")) {
                        writer.println("250 2.1.5 OK");
                    } else if (line.equals("DATA")) {
                        writer.println("354 Go ahead");
                        String dataLine;
                        while ((dataLine = reader.readLine()) != null) {
                            if (dataLine.equals(".")) break;
                        }
                        writer.println("250 2.0.0 OK");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            EmailConfig oauthConfig = EmailConfig.builder()
                    .host("localhost")
                    .port(smtpPort)
                    .ssl(false)
                    .starttls(false)
                    .username("user@gmail.com")
                    .oauth2Token("test-access-token-xyz")
                    .defaultFrom("user@gmail.com")
                    .timeout(Duration.ofSeconds(5))
                    .connectionTimeout(Duration.ofSeconds(5))
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(oauthConfig);
            SendResult result = sender.sendWithResult(simpleTextEmail());

            assertThat(result.success()).isTrue();
            assertThat(capturedAuthCommands).hasSize(1);
            assertThat(capturedAuthCommands.getFirst()).startsWith("AUTH XOAUTH2 ");
        }

        @Test
        @DisplayName("AUTH PLAIN 认证失败 - 模拟返回535")
        void testAuthFailure535() throws Exception {
            startMockSmtp((reader, writer) -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250-AUTH PLAIN LOGIN");
                        writer.println("250 OK");
                    } else if (line.startsWith("AUTH PLAIN")) {
                        writer.println("535 5.7.8 Authentication credentials invalid");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());

            assertThatThrownBy(() -> sender.sendWithResult(simpleTextEmail()))
                    .isInstanceOf(EmailSendException.class)
                    .hasMessageContaining("Authentication failed");
        }

        @Test
        @DisplayName("无认证配置时不发送 AUTH 命令")
        void testNoAuthWhenNotConfigured() throws Exception {
            List<String> allCommands = new CopyOnWriteArrayList<>();

            startMockSmtp((reader, writer) -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    allCommands.add(line);
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250 OK");
                    } else if (line.startsWith("MAIL FROM:")) {
                        writer.println("250 2.1.0 OK");
                    } else if (line.startsWith("RCPT TO:")) {
                        writer.println("250 2.1.5 OK");
                    } else if (line.equals("DATA")) {
                        writer.println("354 Go ahead");
                        String dataLine;
                        while ((dataLine = reader.readLine()) != null) {
                            if (dataLine.equals(".")) break;
                        }
                        writer.println("250 2.0.0 OK");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            EmailConfig noAuthConfig = EmailConfig.builder()
                    .host("localhost")
                    .port(smtpPort)
                    .ssl(false)
                    .starttls(false)
                    .defaultFrom("sender@example.com")
                    .timeout(Duration.ofSeconds(5))
                    .connectionTimeout(Duration.ofSeconds(5))
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(noAuthConfig);
            sender.sendWithResult(simpleTextEmail());

            // No AUTH command should have been sent
            assertThat(allCommands).noneMatch(cmd -> cmd.startsWith("AUTH"));
        }
    }

    @Nested
    @DisplayName("DKIM 集成测试")
    class DkimTests {

        @Test
        @DisplayName("发送带 DKIM 签名的邮件，验证 DKIM-Signature 头在 DATA 内容中")
        void testDkimSignature() throws Exception {
            List<String> dataLines = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(null, dataLines));

            // Generate an RSA key pair for DKIM signing
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            DkimConfig dkimConfig = DkimConfig.of("example.com", "mail", keyPair.getPrivate());

            EmailConfig config = EmailConfig.builder()
                    .host("localhost")
                    .port(smtpPort)
                    .ssl(false)
                    .starttls(false)
                    .username("testuser@example.com")
                    .password("testpassword")
                    .defaultFrom("sender@example.com", "Test Sender")
                    .timeout(Duration.ofSeconds(5))
                    .connectionTimeout(Duration.ofSeconds(5))
                    .dkim(dkimConfig)
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);
            SendResult result = sender.sendWithResult(simpleTextEmail());

            assertThat(result.success()).isTrue();

            String dataContent = String.join("\n", dataLines);
            assertThat(dataContent).contains("DKIM-Signature:");
            assertThat(dataContent).contains("d=example.com");
            assertThat(dataContent).contains("s=mail");
            assertThat(dataContent).contains("a=rsa-sha256");
            assertThat(dataContent).contains("bh=");
            assertThat(dataContent).contains("b=");
        }

        @Test
        @DisplayName("批量发送带 DKIM 签名的邮件")
        void testDkimBatchSend() throws Exception {
            List<String> dataLines = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(null, dataLines));

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            DkimConfig dkimConfig = DkimConfig.of("example.com", "selector1", keyPair.getPrivate());

            EmailConfig config = EmailConfig.builder()
                    .host("localhost")
                    .port(smtpPort)
                    .ssl(false)
                    .starttls(false)
                    .username("testuser@example.com")
                    .password("testpassword")
                    .defaultFrom("sender@example.com")
                    .timeout(Duration.ofSeconds(5))
                    .connectionTimeout(Duration.ofSeconds(5))
                    .dkim(dkimConfig)
                    .build();

            List<Email> emails = List.of(
                    Email.builder().to("a@example.com").subject("A").text("A").build(),
                    Email.builder().to("b@example.com").subject("B").text("B").build()
            );

            SmtpEmailSender sender = new SmtpEmailSender(config);
            BatchSendResult result = sender.sendBatch(emails);

            assertThat(result.allSucceeded()).isTrue();

            // Both emails should have DKIM headers
            String dataContent = String.join("\n", dataLines);
            // Count DKIM-Signature occurrences - should be at least 2
            long dkimCount = dataLines.stream()
                    .filter(l -> l.startsWith("DKIM-Signature:"))
                    .count();
            assertThat(dkimCount).isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("ErrorHandling 集成测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("模拟 RCPT TO 返回 550 - EmailSendException")
        void testRcptToRejected() throws Exception {
            startMockSmtp((reader, writer) -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250-AUTH PLAIN LOGIN");
                        writer.println("250 OK");
                    } else if (line.startsWith("AUTH PLAIN")) {
                        writer.println("235 2.7.0 Authentication successful");
                    } else if (line.startsWith("MAIL FROM:")) {
                        writer.println("250 2.1.0 OK");
                    } else if (line.startsWith("RCPT TO:")) {
                        writer.println("550 5.1.1 User not found");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());

            assertThatThrownBy(() -> sender.sendWithResult(simpleTextEmail()))
                    .isInstanceOf(EmailSendException.class)
                    .hasMessageContaining("RCPT TO rejected");
        }

        @Test
        @DisplayName("模拟 MAIL FROM 返回 553 - EmailSendException")
        void testMailFromRejected() throws Exception {
            startMockSmtp((reader, writer) -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250-AUTH PLAIN LOGIN");
                        writer.println("250 OK");
                    } else if (line.startsWith("AUTH PLAIN")) {
                        writer.println("235 2.7.0 Authentication successful");
                    } else if (line.startsWith("MAIL FROM:")) {
                        writer.println("553 5.1.8 Sender address rejected");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());

            assertThatThrownBy(() -> sender.sendWithResult(simpleTextEmail()))
                    .isInstanceOf(EmailSendException.class);
        }

        @Test
        @DisplayName("模拟服务器意外关闭连接")
        void testServerClosesConnection() throws Exception {
            startMockSmtp((reader, writer) -> {
                String line = reader.readLine(); // read EHLO
                if (line != null && line.startsWith("EHLO")) {
                    writer.println("250-mock-smtp");
                    writer.println("250 OK");
                }
                line = reader.readLine(); // read AUTH
                // Close connection without responding - the socket close in the
                // try-with-resources will trigger the expected error
            });

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());

            assertThatThrownBy(() -> sender.sendWithResult(simpleTextEmail()))
                    .isInstanceOf(EmailSendException.class);
        }

        @Test
        @DisplayName("模拟 DATA 阶段返回错误 - 拒绝消息")
        void testDataRejected() throws Exception {
            startMockSmtp((reader, writer) -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250-AUTH PLAIN LOGIN");
                        writer.println("250 OK");
                    } else if (line.startsWith("AUTH PLAIN")) {
                        writer.println("235 2.7.0 Authentication successful");
                    } else if (line.startsWith("MAIL FROM:")) {
                        writer.println("250 2.1.0 OK");
                    } else if (line.startsWith("RCPT TO:")) {
                        writer.println("250 2.1.5 OK");
                    } else if (line.equals("DATA")) {
                        // Reject the DATA command itself
                        writer.println("554 5.3.4 Message too big");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());

            assertThatThrownBy(() -> sender.sendWithResult(simpleTextEmail()))
                    .isInstanceOf(EmailSendException.class);
        }

        @Test
        @DisplayName("批量发送部分失败 - 验证 failures 和 successes")
        void testBatchPartialFailure() throws Exception {
            // Handler that rejects the second RCPT TO (second email in batch)
            startMockSmtp((reader, writer) -> {
                int mailFromCount = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250-AUTH PLAIN LOGIN");
                        writer.println("250 OK");
                    } else if (line.startsWith("AUTH PLAIN")) {
                        writer.println("235 2.7.0 Authentication successful");
                    } else if (line.startsWith("MAIL FROM:")) {
                        mailFromCount++;
                        writer.println("250 2.1.0 OK");
                    } else if (line.startsWith("RCPT TO:")) {
                        if (mailFromCount == 2) {
                            // Reject the second email's recipient
                            writer.println("550 5.1.1 User not found");
                        } else {
                            writer.println("250 2.1.5 OK");
                        }
                    } else if (line.equals("DATA")) {
                        writer.println("354 Go ahead");
                        String dataLine;
                        while ((dataLine = reader.readLine()) != null) {
                            if (dataLine.equals(".")) break;
                        }
                        writer.println("250 2.0.0 OK");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            List<Email> emails = List.of(
                    Email.builder().to("ok@example.com").subject("OK").text("OK").build(),
                    Email.builder().to("bad@example.com").subject("Bad").text("Bad").build(),
                    Email.builder().to("ok2@example.com").subject("OK2").text("OK2").build()
            );

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            BatchSendResult result = sender.sendBatch(emails);

            assertThat(result.totalCount()).isEqualTo(3);
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.failureCount()).isEqualTo(1);
            assertThat(result.allSucceeded()).isFalse();
            assertThat(result.failures()).hasSize(1);
            assertThat(result.failures().getFirst().error()).isNotBlank();
        }

        @Test
        @DisplayName("连接不存在的服务器 - 抛出 EmailSendException")
        void testConnectionRefused() throws Exception {
            smtpServer.close();

            EmailConfig config = EmailConfig.builder()
                    .host("localhost")
                    .port(smtpPort)
                    .ssl(false)
                    .starttls(false)
                    .username("user@example.com")
                    .password("password")
                    .defaultFrom("sender@example.com")
                    .timeout(Duration.ofSeconds(2))
                    .connectionTimeout(Duration.ofSeconds(2))
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(config);

            assertThatThrownBy(() -> sender.sendWithResult(simpleTextEmail()))
                    .isInstanceOf(EmailSendException.class);
        }
    }

    @Nested
    @DisplayName("消息内容验证测试")
    class MessageContentTests {

        @Test
        @DisplayName("验证 MAIL FROM 使用正确的发件人地址")
        void testMailFromUsesConfigDefaultFrom() throws Exception {
            List<String> allCommands = new CopyOnWriteArrayList<>();

            startMockSmtp((reader, writer) -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    allCommands.add(line);
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250-AUTH PLAIN LOGIN");
                        writer.println("250 OK");
                    } else if (line.startsWith("AUTH PLAIN")) {
                        writer.println("235 2.7.0 Authentication successful");
                    } else if (line.startsWith("MAIL FROM:")) {
                        writer.println("250 2.1.0 OK");
                    } else if (line.startsWith("RCPT TO:")) {
                        writer.println("250 2.1.5 OK");
                    } else if (line.equals("DATA")) {
                        writer.println("354 Go ahead");
                        String dataLine;
                        while ((dataLine = reader.readLine()) != null) {
                            if (dataLine.equals(".")) break;
                        }
                        writer.println("250 2.0.0 OK");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            sender.sendWithResult(simpleTextEmail());

            // Verify MAIL FROM used the config's defaultFrom
            assertThat(allCommands).anySatisfy(cmd ->
                    assertThat(cmd).isEqualTo("MAIL FROM:<sender@example.com>"));
        }

        @Test
        @DisplayName("邮件指定 from 覆盖配置的 defaultFrom")
        void testEmailFromOverridesDefault() throws Exception {
            List<String> allCommands = new CopyOnWriteArrayList<>();

            startMockSmtp((reader, writer) -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    allCommands.add(line);
                    if (line.startsWith("EHLO")) {
                        writer.println("250-mock-smtp");
                        writer.println("250-AUTH PLAIN LOGIN");
                        writer.println("250 OK");
                    } else if (line.startsWith("AUTH PLAIN")) {
                        writer.println("235 2.7.0 Authentication successful");
                    } else if (line.startsWith("MAIL FROM:")) {
                        writer.println("250 2.1.0 OK");
                    } else if (line.startsWith("RCPT TO:")) {
                        writer.println("250 2.1.5 OK");
                    } else if (line.equals("DATA")) {
                        writer.println("354 Go ahead");
                        String dataLine;
                        while ((dataLine = reader.readLine()) != null) {
                            if (dataLine.equals(".")) break;
                        }
                        writer.println("250 2.0.0 OK");
                    } else if (line.equals("QUIT")) {
                        writer.println("221 2.0.0 Bye");
                        break;
                    }
                }
            });

            Email emailWithFrom = Email.builder()
                    .from("custom@example.com")
                    .to("recipient@example.com")
                    .subject("Custom From")
                    .text("Content")
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            sender.sendWithResult(emailWithFrom);

            assertThat(allCommands).anySatisfy(cmd ->
                    assertThat(cmd).isEqualTo("MAIL FROM:<custom@example.com>"));
        }

        @Test
        @DisplayName("发送带 Reply-To 的邮件")
        void testEmailWithReplyTo() throws Exception {
            List<String> dataLines = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(null, dataLines));

            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("Reply-To Test")
                    .text("Please reply")
                    .replyTo("reply-to@example.com")
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();

            String dataContent = String.join("\n", dataLines);
            assertThat(dataContent).contains("Reply-To:");
            assertThat(dataContent).contains("reply-to@example.com");
        }

        @Test
        @DisplayName("发送 multipart/alternative 邮件（文本和HTML）")
        void testTextAndHtmlEmail() throws Exception {
            List<String> dataLines = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(null, dataLines));

            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("Alternative Test")
                    .textAndHtml("Plain text fallback", "<html><body><p>Rich HTML</p></body></html>")
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();

            String dataContent = String.join("\n", dataLines);
            assertThat(dataContent).contains("Plain text fallback");
            assertThat(dataContent).contains("Rich HTML");
        }

        @Test
        @DisplayName("发送带多个附件的邮件")
        void testMultipleAttachments() throws Exception {
            List<String> dataLines = new CopyOnWriteArrayList<>();
            startMockSmtp(successHandler(null, dataLines));

            ByteArrayAttachment att1 = new ByteArrayAttachment(
                    "file1.txt", "content1".getBytes(StandardCharsets.UTF_8), "text/plain");
            ByteArrayAttachment att2 = new ByteArrayAttachment(
                    "file2.csv", "a,b,c".getBytes(StandardCharsets.UTF_8), "text/csv");

            Email email = Email.builder()
                    .to("recipient@example.com")
                    .subject("Multiple Attachments")
                    .text("See attached files")
                    .attach(att1)
                    .attach(att2)
                    .build();

            SmtpEmailSender sender = new SmtpEmailSender(basicConfig());
            SendResult result = sender.sendWithResult(email);

            assertThat(result.success()).isTrue();

            String dataContent = String.join("\n", dataLines);
            assertThat(dataContent).contains("file1.txt");
            assertThat(dataContent).contains("file2.csv");
        }
    }
}
