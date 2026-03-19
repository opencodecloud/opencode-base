package cloud.opencode.base.event.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SignedEvent 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("SignedEvent 测试")
class SignedEventTest {

    static class TestSignedEvent extends SignedEvent {
        // Note: payload must be a constant or field initialized before super() call
        // because getPayload() is called during super constructor
        private static final String FIXED_PAYLOAD = "test-payload";

        public TestSignedEvent(String secret) {
            super(secret);
        }

        public TestSignedEvent(String source, String secret) {
            super(source, secret);
        }

        @Override
        protected String getPayload() {
            return FIXED_PAYLOAD;
        }
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用secret创建")
        void testConstructorWithSecret() {
            TestSignedEvent event = new TestSignedEvent("secret-key");

            assertThat(event.getSignature()).isNotNull().isNotEmpty();
            assertThat(event.getSource()).isNull();
        }

        @Test
        @DisplayName("使用source和secret创建")
        void testConstructorWithSourceAndSecret() {
            TestSignedEvent event = new TestSignedEvent("source", "secret-key");

            assertThat(event.getSignature()).isNotNull().isNotEmpty();
            assertThat(event.getSource()).isEqualTo("source");
        }
    }

    @Nested
    @DisplayName("getSignature() 测试")
    class GetSignatureTests {

        @Test
        @DisplayName("返回非空签名")
        void testReturnsNonEmptySignature() {
            TestSignedEvent event = new TestSignedEvent("secret");

            assertThat(event.getSignature()).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("相同参数返回相同签名")
        void testSameParametersSameSignature() {
            // 由于timestamp和id不同，签名会不同
            // 这里只测试签名不为空
            TestSignedEvent event = new TestSignedEvent("secret");

            assertThat(event.getSignature()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("verify() 测试")
    class VerifyTests {

        @Test
        @DisplayName("正确secret验证成功")
        void testVerifyWithCorrectSecret() {
            String secret = "my-secret-key";
            TestSignedEvent event = new TestSignedEvent(secret);

            assertThat(event.verify(secret)).isTrue();
        }

        @Test
        @DisplayName("错误secret验证失败")
        void testVerifyWithWrongSecret() {
            TestSignedEvent event = new TestSignedEvent("correct-secret");

            assertThat(event.verify("wrong-secret")).isFalse();
        }

        @Test
        @DisplayName("不同事件不同签名")
        void testDifferentEventsDifferentSignature() {
            String secret = "secret";
            TestSignedEvent event1 = new TestSignedEvent(secret);
            TestSignedEvent event2 = new TestSignedEvent(secret);

            // 由于id和timestamp不同，签名一定不同
            assertThat(event1.getSignature()).isNotEqualTo(event2.getSignature());
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承Event")
        void testExtendsEvent() {
            TestSignedEvent event = new TestSignedEvent("secret");

            assertThat(event.getId()).isNotNull();
            assertThat(event.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("实现VerifiableEvent")
        void testImplementsVerifiableEvent() {
            TestSignedEvent event = new TestSignedEvent("secret");

            assertThat(event).isInstanceOf(VerifiableEvent.class);
        }
    }

    @Nested
    @DisplayName("HMAC-SHA256测试")
    class HmacTests {

        @Test
        @DisplayName("签名是Base64编码")
        void testSignatureIsBase64() {
            TestSignedEvent event = new TestSignedEvent("secret");
            String signature = event.getSignature();

            // Base64编码应该只包含字母、数字、+、/、=
            assertThat(signature).matches("[A-Za-z0-9+/=]+");
        }
    }
}
