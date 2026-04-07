package cloud.opencode.base.captcha;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaEventDispatcher Test - Unit tests for event dispatching
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
class CaptchaEventDispatcherTest {

    private CaptchaEventDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new CaptchaEventDispatcher();
    }

    @Nested
    @DisplayName("Listener Management Tests | 监听器管理测试")
    class ListenerManagementTests {

        @Test
        @DisplayName("should start with zero listeners")
        void shouldStartWithZeroListeners() {
            assertThat(dispatcher.listenerCount()).isZero();
        }

        @Test
        @DisplayName("should add listener")
        void shouldAddListener() {
            dispatcher.addListener(new CaptchaEventListener() {});

            assertThat(dispatcher.listenerCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should remove listener")
        void shouldRemoveListener() {
            CaptchaEventListener listener = new CaptchaEventListener() {};
            dispatcher.addListener(listener);

            boolean removed = dispatcher.removeListener(listener);

            assertThat(removed).isTrue();
            assertThat(dispatcher.listenerCount()).isZero();
        }

        @Test
        @DisplayName("should return false when removing non-existent listener")
        void shouldReturnFalseWhenRemovingNonExistentListener() {
            boolean removed = dispatcher.removeListener(new CaptchaEventListener() {});

            assertThat(removed).isFalse();
        }

        @Test
        @DisplayName("should reject null listener")
        void shouldRejectNullListener() {
            assertThatNullPointerException()
                .isThrownBy(() -> dispatcher.addListener(null));
        }

        @Test
        @DisplayName("should allow adding same listener multiple times")
        void shouldAllowAddingSameListenerMultipleTimes() {
            CaptchaEventListener listener = new CaptchaEventListener() {};
            dispatcher.addListener(listener);
            dispatcher.addListener(listener);

            assertThat(dispatcher.listenerCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Event Dispatch Tests | 事件分发测试")
    class EventDispatchTests {

        private Captcha createTestCaptcha() {
            return new Captcha(
                "test-id",
                CaptchaType.NUMERIC,
                new byte[]{1, 2, 3},
                "answer",
                Map.of(),
                Instant.now(),
                Instant.now().plusSeconds(300)
            );
        }

        @Test
        @DisplayName("should dispatch onGenerated to all listeners")
        void shouldDispatchOnGeneratedToAllListeners() {
            List<String> invocations = new ArrayList<>();
            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onGenerated(Captcha captcha) {
                    invocations.add("listener-1:" + captcha.id());
                }
            });
            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onGenerated(Captcha captcha) {
                    invocations.add("listener-2:" + captcha.id());
                }
            });

            dispatcher.onGenerated(createTestCaptcha());

            assertThat(invocations).containsExactly("listener-1:test-id", "listener-2:test-id");
        }

        @Test
        @DisplayName("should dispatch onValidationSuccess to all listeners")
        void shouldDispatchOnValidationSuccessToAllListeners() {
            List<String> invocations = new ArrayList<>();
            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onValidationSuccess(String captchaId) {
                    invocations.add("success:" + captchaId);
                }
            });
            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onValidationSuccess(String captchaId) {
                    invocations.add("success2:" + captchaId);
                }
            });

            dispatcher.onValidationSuccess("captcha-42");

            assertThat(invocations).containsExactly("success:captcha-42", "success2:captcha-42");
        }

        @Test
        @DisplayName("should dispatch onValidationFailure to all listeners")
        void shouldDispatchOnValidationFailureToAllListeners() {
            List<String> invocations = new ArrayList<>();
            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onValidationFailure(String captchaId, ValidationResult.ResultCode reason) {
                    invocations.add(captchaId + ":" + reason);
                }
            });

            dispatcher.onValidationFailure("captcha-99", ValidationResult.ResultCode.MISMATCH);

            assertThat(invocations).containsExactly("captcha-99:MISMATCH");
        }

        @Test
        @DisplayName("should not fail when no listeners registered")
        void shouldNotFailWhenNoListenersRegistered() {
            assertThatNoException().isThrownBy(() -> {
                dispatcher.onGenerated(createTestCaptcha());
                dispatcher.onValidationSuccess("id");
                dispatcher.onValidationFailure("id", ValidationResult.ResultCode.NOT_FOUND);
            });
        }
    }

    @Nested
    @DisplayName("Exception Isolation Tests | 异常隔离测试")
    class ExceptionIsolationTests {

        private Captcha createTestCaptcha() {
            return new Captcha(
                "test-id",
                CaptchaType.NUMERIC,
                new byte[]{1, 2, 3},
                "answer",
                Map.of(),
                Instant.now(),
                Instant.now().plusSeconds(300)
            );
        }

        @Test
        @DisplayName("should isolate exceptions in onGenerated")
        void shouldIsolateExceptionsInOnGenerated() {
            AtomicInteger callCount = new AtomicInteger();

            // First listener throws
            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onGenerated(Captcha captcha) {
                    callCount.incrementAndGet();
                    throw new RuntimeException("boom");
                }
            });
            // Second listener should still be called
            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onGenerated(Captcha captcha) {
                    callCount.incrementAndGet();
                }
            });

            assertThatNoException().isThrownBy(() -> dispatcher.onGenerated(createTestCaptcha()));
            assertThat(callCount.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("should isolate exceptions in onValidationSuccess")
        void shouldIsolateExceptionsInOnValidationSuccess() {
            AtomicInteger callCount = new AtomicInteger();

            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onValidationSuccess(String captchaId) {
                    callCount.incrementAndGet();
                    throw new RuntimeException("boom");
                }
            });
            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onValidationSuccess(String captchaId) {
                    callCount.incrementAndGet();
                }
            });

            assertThatNoException().isThrownBy(() -> dispatcher.onValidationSuccess("id"));
            assertThat(callCount.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("should isolate exceptions in onValidationFailure")
        void shouldIsolateExceptionsInOnValidationFailure() {
            AtomicInteger callCount = new AtomicInteger();

            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onValidationFailure(String captchaId, ValidationResult.ResultCode reason) {
                    callCount.incrementAndGet();
                    throw new RuntimeException("boom");
                }
            });
            dispatcher.addListener(new CaptchaEventListener() {
                @Override
                public void onValidationFailure(String captchaId, ValidationResult.ResultCode reason) {
                    callCount.incrementAndGet();
                }
            });

            assertThatNoException().isThrownBy(
                () -> dispatcher.onValidationFailure("id", ValidationResult.ResultCode.EXPIRED));
            assertThat(callCount.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("should handle multiple throwing listeners in sequence")
        void shouldHandleMultipleThrowingListenersInSequence() {
            AtomicInteger callCount = new AtomicInteger();

            for (int i = 0; i < 3; i++) {
                dispatcher.addListener(new CaptchaEventListener() {
                    @Override
                    public void onGenerated(Captcha captcha) {
                        callCount.incrementAndGet();
                        throw new RuntimeException("boom");
                    }
                });
            }

            assertThatNoException().isThrownBy(() -> dispatcher.onGenerated(createTestCaptcha()));
            assertThat(callCount.get()).isEqualTo(3);
        }
    }
}
