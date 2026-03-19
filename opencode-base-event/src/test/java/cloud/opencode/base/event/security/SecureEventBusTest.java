package cloud.opencode.base.event.security;

import cloud.opencode.base.event.Event;
import cloud.opencode.base.event.annotation.Subscribe;
import cloud.opencode.base.event.exception.EventSecurityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * SecureEventBus 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("SecureEventBus 测试")
class SecureEventBusTest {

    static class TestEvent extends Event {}

    static class TestSignedEvent extends SignedEvent {
        public TestSignedEvent(String secret) {
            super(secret);
        }

        @Override
        protected String getPayload() {
            return "test-payload";
        }
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("无参构造")
        void testDefaultConstructor() {
            SecureEventBus bus = new SecureEventBus();

            assertThat(bus).isNotNull();
            assertThat(bus.getEventBus()).isNotNull();
        }

        @Test
        @DisplayName("带RateLimiter构造")
        void testConstructorWithRateLimiter() {
            EventRateLimiter limiter = new EventRateLimiter(100);

            SecureEventBus bus = new SecureEventBus(limiter);

            assertThat(bus).isNotNull();
        }

        @Test
        @DisplayName("带RateLimiter和verification secret构造")
        void testConstructorWithRateLimiterAndSecret() {
            EventRateLimiter limiter = new EventRateLimiter(100);

            SecureEventBus bus = new SecureEventBus(limiter, "secret-key");

            assertThat(bus).isNotNull();
        }
    }

    @Nested
    @DisplayName("addToWhitelist() 测试")
    class WhitelistTests {

        @Test
        @DisplayName("白名单中的类可注册")
        void testWhitelistedClassCanRegister() {
            SecureEventBus bus = new SecureEventBus();

            class AllowedHandler {
                @Subscribe
                public void onEvent(TestEvent event) {}
            }

            bus.addToWhitelist(AllowedHandler.class);

            assertThatNoException().isThrownBy(() ->
                    bus.register(new AllowedHandler()));
        }

        @Test
        @DisplayName("非白名单类在有白名单时被拒绝")
        void testNonWhitelistedRejected() {
            SecureEventBus bus = new SecureEventBus();

            class AllowedHandler {
                @Subscribe
                public void onEvent(TestEvent event) {}
            }

            class NotAllowedHandler {
                @Subscribe
                public void onEvent(TestEvent event) {}
            }

            bus.addToWhitelist(AllowedHandler.class);

            assertThatThrownBy(() -> bus.register(new NotAllowedHandler()))
                    .isInstanceOf(EventSecurityException.class)
                    .hasMessageContaining("not in whitelist");
        }
    }

    @Nested
    @DisplayName("addAllowedPackage() 测试")
    class PackageTests {

        @Test
        @DisplayName("允许包中的类可注册")
        void testAllowedPackageCanRegister() {
            SecureEventBus bus = new SecureEventBus();

            bus.addAllowedPackage("cloud.opencode.base.event.security");

            class LocalHandler {
                @Subscribe
                public void onEvent(TestEvent event) {}
            }

            assertThatNoException().isThrownBy(() ->
                    bus.register(new LocalHandler()));
        }

        @Test
        @DisplayName("非允许包被拒绝")
        void testDisallowedPackageRejected() {
            SecureEventBus bus = new SecureEventBus();

            bus.addAllowedPackage("com.other.package");

            class LocalHandler {
                @Subscribe
                public void onEvent(TestEvent event) {}
            }

            assertThatThrownBy(() -> bus.register(new LocalHandler()))
                    .isInstanceOf(EventSecurityException.class)
                    .hasMessageContaining("not in allowed package");
        }
    }

    @Nested
    @DisplayName("on() 测试")
    class OnTests {

        @Test
        @DisplayName("Lambda监听器可注册")
        void testLambdaListenerCanRegister() {
            SecureEventBus bus = new SecureEventBus();
            AtomicBoolean called = new AtomicBoolean(false);

            bus.on(TestEvent.class, event -> called.set(true));
            bus.publish(new TestEvent());

            assertThat(called.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("unregister() 测试")
    class UnregisterTests {

        @Test
        @DisplayName("注销订阅者")
        void testUnregister() {
            SecureEventBus bus = new SecureEventBus();
            AtomicBoolean called = new AtomicBoolean(false);

            class Handler {
                @Subscribe
                public void onEvent(TestEvent event) {
                    called.set(true);
                }
            }

            Handler handler = new Handler();
            bus.register(handler);
            bus.unregister(handler);
            bus.publish(new TestEvent());

            assertThat(called.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("publish() 测试")
    class PublishTests {

        @Test
        @DisplayName("发布事件成功")
        void testPublishSuccess() {
            SecureEventBus bus = new SecureEventBus();
            AtomicBoolean received = new AtomicBoolean(false);

            bus.on(TestEvent.class, event -> received.set(true));
            bus.publish(new TestEvent());

            assertThat(received.get()).isTrue();
        }

        @Test
        @DisplayName("频率限制超出拒绝发布")
        void testRateLimitExceededRejects() {
            EventRateLimiter limiter = new EventRateLimiter(2);
            SecureEventBus bus = new SecureEventBus(limiter);

            bus.publish(new TestEvent());
            bus.publish(new TestEvent());

            assertThatThrownBy(() -> bus.publish(new TestEvent()))
                    .isInstanceOf(EventSecurityException.class)
                    .hasMessageContaining("Rate limit exceeded");
        }

        @Test
        @DisplayName("签名验证失败拒绝发布")
        void testVerificationFailedRejects() {
            SecureEventBus bus = new SecureEventBus(null, "correct-secret");

            TestSignedEvent event = new TestSignedEvent("wrong-secret");

            assertThatThrownBy(() -> bus.publish(event))
                    .isInstanceOf(EventSecurityException.class)
                    .hasMessageContaining("signature verification failed");
        }

        @Test
        @DisplayName("签名验证成功允许发布")
        void testVerificationSuccessAllows() {
            String secret = "shared-secret";
            SecureEventBus bus = new SecureEventBus(null, secret);
            AtomicBoolean received = new AtomicBoolean(false);

            bus.on(TestSignedEvent.class, event -> received.set(true));

            TestSignedEvent event = new TestSignedEvent(secret);
            bus.publish(event);

            assertThat(received.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("getEventBus() 测试")
    class GetEventBusTests {

        @Test
        @DisplayName("返回底层OpenEvent")
        void testReturnsUnderlyingEventBus() {
            SecureEventBus bus = new SecureEventBus();

            assertThat(bus.getEventBus()).isNotNull();
        }
    }
}
