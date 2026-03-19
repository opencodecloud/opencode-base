package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.internal.EmailReceiver;
import cloud.opencode.base.email.query.EmailQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * AsyncEmailReceiver 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("AsyncEmailReceiver 测试")
class AsyncEmailReceiverTest {

    private MockEmailReceiver mockReceiver;

    @BeforeEach
    void setUp() {
        mockReceiver = new MockEmailReceiver();
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用delegate创建")
        void testConstructorWithDelegate() {
            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);

            assertThat(asyncReceiver).isNotNull();
            assertThat(asyncReceiver.getDelegate()).isSameAs(mockReceiver);
        }

        @Test
        @DisplayName("null delegate抛出异常")
        void testConstructorWithNullDelegateThrows() {
            assertThatThrownBy(() -> new AsyncEmailReceiver(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("创建Builder")
        void testBuilder() {
            AsyncEmailReceiver.Builder builder = AsyncEmailReceiver.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("使用receiver构建")
        void testBuilderWithReceiver() {
            AsyncEmailReceiver asyncReceiver = AsyncEmailReceiver.builder()
                    .receiver(mockReceiver)
                    .build();

            assertThat(asyncReceiver).isNotNull();
            assertThat(asyncReceiver.getDelegate()).isSameAs(mockReceiver);
        }

        @Test
        @DisplayName("使用config构建IMAP")
        void testBuilderWithImapConfig() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .port(993)
                    .username("user")
                    .password("pass")
                    .protocol(EmailReceiveConfig.Protocol.IMAP)
                    .build();

            AsyncEmailReceiver asyncReceiver = AsyncEmailReceiver.builder()
                    .config(config)
                    .build();

            assertThat(asyncReceiver).isNotNull();
            assertThat(asyncReceiver.getDelegate()).isInstanceOf(ImapEmailReceiver.class);
        }

        @Test
        @DisplayName("使用config构建POP3")
        void testBuilderWithPop3Config() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop3.example.com")
                    .port(995)
                    .username("user")
                    .password("pass")
                    .protocol(EmailReceiveConfig.Protocol.POP3)
                    .build();

            AsyncEmailReceiver asyncReceiver = AsyncEmailReceiver.builder()
                    .config(config)
                    .build();

            assertThat(asyncReceiver).isNotNull();
            assertThat(asyncReceiver.getDelegate()).isInstanceOf(Pop3EmailReceiver.class);
        }

        @Test
        @DisplayName("没有receiver和config抛出异常")
        void testBuilderWithoutReceiverOrConfigThrows() {
            assertThatThrownBy(() -> AsyncEmailReceiver.builder().build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("receiver or config");
        }

        @Test
        @DisplayName("设置自定义executor")
        void testBuilderWithExecutor() {
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                AsyncEmailReceiver asyncReceiver = AsyncEmailReceiver.builder()
                        .receiver(mockReceiver)
                        .executor(executor)
                        .build();

                assertThat(asyncReceiver).isNotNull();
            } finally {
                executor.shutdown();
            }
        }

        @Test
        @DisplayName("设置useVirtualThreads")
        void testBuilderWithVirtualThreads() {
            AsyncEmailReceiver asyncReceiver = AsyncEmailReceiver.builder()
                    .receiver(mockReceiver)
                    .useVirtualThreads(true)
                    .build();

            assertThat(asyncReceiver).isNotNull();
        }

        @Test
        @DisplayName("不使用虚拟线程")
        void testBuilderWithoutVirtualThreads() {
            AsyncEmailReceiver asyncReceiver = AsyncEmailReceiver.builder()
                    .receiver(mockReceiver)
                    .useVirtualThreads(false)
                    .build();

            assertThat(asyncReceiver).isNotNull();
        }
    }

    @Nested
    @DisplayName("异步接收测试")
    class AsyncReceiveTests {

        @Test
        @DisplayName("receiveUnreadAsync返回未读邮件")
        void testReceiveUnreadAsync() throws Exception {
            mockReceiver.setConnected(true);
            ReceivedEmail email = createMockEmail("1");
            mockReceiver.setUnreadEmails(List.of(email));

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            CompletableFuture<List<ReceivedEmail>> future = asyncReceiver.receiveUnreadAsync();
            List<ReceivedEmail> result = future.get(5, TimeUnit.SECONDS);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).messageId()).isEqualTo("1");
        }

        @Test
        @DisplayName("receiveAsync使用查询")
        void testReceiveAsyncWithQuery() throws Exception {
            mockReceiver.setConnected(true);
            ReceivedEmail email = createMockEmail("2");
            mockReceiver.setQueryEmails(List.of(email));

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            EmailQuery query = EmailQuery.builder().build();
            CompletableFuture<List<ReceivedEmail>> future = asyncReceiver.receiveAsync(query);
            List<ReceivedEmail> result = future.get(5, TimeUnit.SECONDS);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("receiveAsync使用回调")
        void testReceiveAsyncWithCallback() throws Exception {
            mockReceiver.setConnected(true);
            ReceivedEmail email = createMockEmail("3");
            mockReceiver.setQueryEmails(List.of(email));

            AtomicReference<List<ReceivedEmail>> callbackResult = new AtomicReference<>();
            AtomicReference<Throwable> callbackError = new AtomicReference<>();

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            EmailQuery query = EmailQuery.builder().build();
            CompletableFuture<List<ReceivedEmail>> future = asyncReceiver.receiveAsync(query,
                    (emails, error) -> {
                        callbackResult.set(emails);
                        callbackError.set(error);
                    });

            future.get(5, TimeUnit.SECONDS);

            assertThat(callbackResult.get()).hasSize(1);
            assertThat(callbackError.get()).isNull();
        }

        @Test
        @DisplayName("receiveAllAsync返回所有邮件")
        void testReceiveAllAsync() throws Exception {
            mockReceiver.setConnected(true);
            mockReceiver.setQueryEmails(List.of(createMockEmail("1"), createMockEmail("2")));

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            CompletableFuture<List<ReceivedEmail>> future = asyncReceiver.receiveAllAsync();
            List<ReceivedEmail> result = future.get(5, TimeUnit.SECONDS);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("receiveByIdAsync返回特定邮件")
        void testReceiveByIdAsync() throws Exception {
            mockReceiver.setConnected(true);
            ReceivedEmail email = createMockEmail("specific-id");
            mockReceiver.setEmailById(email);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            CompletableFuture<ReceivedEmail> future = asyncReceiver.receiveByIdAsync("specific-id");
            ReceivedEmail result = future.get(5, TimeUnit.SECONDS);

            assertThat(result.messageId()).isEqualTo("specific-id");
        }

        @Test
        @DisplayName("未连接时自动连接")
        void testAutoConnectWhenNotConnected() throws Exception {
            mockReceiver.setConnected(false);
            mockReceiver.setUnreadEmails(List.of());

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            asyncReceiver.receiveUnreadAsync().get(5, TimeUnit.SECONDS);

            assertThat(mockReceiver.isConnected()).isTrue();
        }
    }

    @Nested
    @DisplayName("异步操作测试")
    class AsyncOperationTests {

        @Test
        @DisplayName("markAsReadAsync标记已读")
        void testMarkAsReadAsync() throws Exception {
            mockReceiver.setConnected(true);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            asyncReceiver.markAsReadAsync("msg-1").get(5, TimeUnit.SECONDS);

            assertThat(mockReceiver.getMarkedAsRead()).contains("msg-1");
        }

        @Test
        @DisplayName("markAsUnreadAsync标记未读")
        void testMarkAsUnreadAsync() throws Exception {
            mockReceiver.setConnected(true);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            asyncReceiver.markAsUnreadAsync("msg-2").get(5, TimeUnit.SECONDS);

            assertThat(mockReceiver.getMarkedAsUnread()).contains("msg-2");
        }

        @Test
        @DisplayName("setFlaggedAsync设置标记")
        void testSetFlaggedAsync() throws Exception {
            mockReceiver.setConnected(true);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            asyncReceiver.setFlaggedAsync("msg-3", true).get(5, TimeUnit.SECONDS);

            assertThat(mockReceiver.getFlagged()).contains("msg-3");
        }

        @Test
        @DisplayName("deleteAsync删除邮件")
        void testDeleteAsync() throws Exception {
            mockReceiver.setConnected(true);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            asyncReceiver.deleteAsync("msg-4").get(5, TimeUnit.SECONDS);

            assertThat(mockReceiver.getDeleted()).contains("msg-4");
        }

        @Test
        @DisplayName("moveToFolderAsync移动邮件")
        void testMoveToFolderAsync() throws Exception {
            mockReceiver.setConnected(true);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            asyncReceiver.moveToFolderAsync("msg-5", "Archive").get(5, TimeUnit.SECONDS);

            assertThat(mockReceiver.getMovedTo()).containsEntry("msg-5", "Archive");
        }

        @Test
        @DisplayName("listFoldersAsync列出文件夹")
        void testListFoldersAsync() throws Exception {
            mockReceiver.setConnected(true);
            mockReceiver.setFolders(List.of("INBOX", "Sent", "Drafts"));

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            CompletableFuture<List<String>> future = asyncReceiver.listFoldersAsync();
            List<String> result = future.get(5, TimeUnit.SECONDS);

            assertThat(result).containsExactly("INBOX", "Sent", "Drafts");
        }

        @Test
        @DisplayName("getMessageCountAsync获取消息数量")
        void testGetMessageCountAsync() throws Exception {
            mockReceiver.setConnected(true);
            mockReceiver.setMessageCount(42);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            CompletableFuture<Integer> future = asyncReceiver.getMessageCountAsync("INBOX");
            Integer result = future.get(5, TimeUnit.SECONDS);

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("getUnreadCountAsync获取未读数量")
        void testGetUnreadCountAsync() throws Exception {
            mockReceiver.setConnected(true);
            mockReceiver.setUnreadCount(10);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            CompletableFuture<Integer> future = asyncReceiver.getUnreadCountAsync("INBOX");
            Integer result = future.get(5, TimeUnit.SECONDS);

            assertThat(result).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("生命周期测试")
    class LifecycleTests {

        @Test
        @DisplayName("connect连接服务器")
        void testConnect() {
            mockReceiver.setConnected(false);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            asyncReceiver.connect();

            assertThat(mockReceiver.isConnected()).isTrue();
        }

        @Test
        @DisplayName("connectAsync异步连接")
        void testConnectAsync() throws Exception {
            mockReceiver.setConnected(false);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            asyncReceiver.connectAsync().get(5, TimeUnit.SECONDS);

            assertThat(mockReceiver.isConnected()).isTrue();
        }

        @Test
        @DisplayName("disconnect断开连接")
        void testDisconnect() {
            mockReceiver.setConnected(true);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            asyncReceiver.disconnect();

            assertThat(mockReceiver.isConnected()).isFalse();
        }

        @Test
        @DisplayName("isConnected返回连接状态")
        void testIsConnected() {
            mockReceiver.setConnected(true);

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);

            assertThat(asyncReceiver.isConnected()).isTrue();
        }

        @Test
        @DisplayName("close关闭资源")
        void testClose() {
            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);

            assertThatNoException().isThrownBy(asyncReceiver::close);
        }

        @Test
        @DisplayName("getDelegate返回底层接收器")
        void testGetDelegate() {
            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);

            assertThat(asyncReceiver.getDelegate()).isSameAs(mockReceiver);
        }
    }

    @Nested
    @DisplayName("回调测试")
    class CallbackTests {

        @Test
        @DisplayName("null回调不抛异常")
        void testNullCallbackNoException() throws Exception {
            mockReceiver.setConnected(true);
            mockReceiver.setQueryEmails(List.of());

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            EmailQuery query = EmailQuery.builder().build();

            CompletableFuture<List<ReceivedEmail>> future = asyncReceiver.receiveAsync(query, null);

            assertThatNoException().isThrownBy(() -> future.get(5, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("异常时回调收到错误")
        void testCallbackReceivesError() throws Exception {
            mockReceiver.setConnected(true);
            mockReceiver.setThrowOnReceive(true);

            AtomicReference<Throwable> callbackError = new AtomicReference<>();

            AsyncEmailReceiver asyncReceiver = new AsyncEmailReceiver(mockReceiver);
            EmailQuery query = EmailQuery.builder().build();

            CompletableFuture<List<ReceivedEmail>> future = asyncReceiver.receiveAsync(query,
                    (emails, error) -> callbackError.set(error));

            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }

            assertThat(callbackError.get()).isNotNull();
        }
    }

    // ==================== Mock Implementation ====================

    private ReceivedEmail createMockEmail(String messageId) {
        return ReceivedEmail.builder()
                .messageId(messageId)
                .from("sender@example.com")
                .subject("Test Subject")
                .textContent("Test body")
                .build();
    }

    private static class MockEmailReceiver implements EmailReceiver {
        private boolean connected = false;
        private boolean throwOnReceive = false;
        private List<ReceivedEmail> unreadEmails = List.of();
        private List<ReceivedEmail> queryEmails = List.of();
        private ReceivedEmail emailById = null;
        private List<String> folders = List.of("INBOX");
        private int messageCount = 0;
        private int unreadCount = 0;
        private final List<String> markedAsRead = new java.util.ArrayList<>();
        private final List<String> markedAsUnread = new java.util.ArrayList<>();
        private final List<String> flagged = new java.util.ArrayList<>();
        private final List<String> deleted = new java.util.ArrayList<>();
        private final java.util.Map<String, String> movedTo = new java.util.HashMap<>();

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public void setThrowOnReceive(boolean throwOnReceive) {
            this.throwOnReceive = throwOnReceive;
        }

        public void setUnreadEmails(List<ReceivedEmail> emails) {
            this.unreadEmails = emails;
        }

        public void setQueryEmails(List<ReceivedEmail> emails) {
            this.queryEmails = emails;
        }

        public void setEmailById(ReceivedEmail email) {
            this.emailById = email;
        }

        public void setFolders(List<String> folders) {
            this.folders = folders;
        }

        public void setMessageCount(int count) {
            this.messageCount = count;
        }

        public void setUnreadCount(int count) {
            this.unreadCount = count;
        }

        public List<String> getMarkedAsRead() {
            return markedAsRead;
        }

        public List<String> getMarkedAsUnread() {
            return markedAsUnread;
        }

        public List<String> getFlagged() {
            return flagged;
        }

        public List<String> getDeleted() {
            return deleted;
        }

        public java.util.Map<String, String> getMovedTo() {
            return movedTo;
        }

        @Override
        public List<ReceivedEmail> receiveUnread() {
            if (throwOnReceive) {
                throw new RuntimeException("Mock error");
            }
            return unreadEmails;
        }

        @Override
        public List<ReceivedEmail> receive(EmailQuery query) {
            if (throwOnReceive) {
                throw new RuntimeException("Mock error");
            }
            return queryEmails;
        }

        @Override
        public ReceivedEmail receiveById(String messageId) {
            return emailById;
        }

        @Override
        public int getMessageCount(String folder) {
            return messageCount;
        }

        @Override
        public int getUnreadCount(String folder) {
            return unreadCount;
        }

        @Override
        public void markAsRead(String messageId) {
            markedAsRead.add(messageId);
        }

        @Override
        public void markAsUnread(String messageId) {
            markedAsUnread.add(messageId);
        }

        @Override
        public void setFlagged(String messageId, boolean flagged) {
            if (flagged) {
                this.flagged.add(messageId);
            }
        }

        @Override
        public void delete(String messageId) {
            deleted.add(messageId);
        }

        @Override
        public void moveToFolder(String messageId, String targetFolder) {
            movedTo.put(messageId, targetFolder);
        }

        @Override
        public List<String> listFolders() {
            return folders;
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
}
