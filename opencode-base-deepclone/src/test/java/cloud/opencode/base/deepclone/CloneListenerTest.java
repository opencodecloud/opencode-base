package cloud.opencode.base.deepclone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CloneListener 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("CloneListener 测试")
class CloneListenerTest {

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("默认beforeClone不应抛出异常")
        void defaultBeforeCloneShouldNotThrow() {
            CloneListener listener = new CloneListener() {};
            CloneContext context = CloneContext.create();

            assertThatCode(() -> listener.beforeClone("test", context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("默认afterClone不应抛出异常")
        void defaultAfterCloneShouldNotThrow() {
            CloneListener listener = new CloneListener() {};
            CloneContext context = CloneContext.create();

            assertThatCode(() -> listener.afterClone("original", "cloned", context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("默认onError不应抛出异常")
        void defaultOnErrorShouldNotThrow() {
            CloneListener listener = new CloneListener() {};
            CloneContext context = CloneContext.create();

            assertThatCode(() -> listener.onError("test", new RuntimeException("fail"), context))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("自定义实现测试")
    class CustomImplementationTests {

        @Test
        @DisplayName("自定义实现应接收beforeClone回调")
        void customListenerShouldReceiveBeforeClone() {
            List<Object> captured = new ArrayList<>();
            CloneListener listener = new CloneListener() {
                @Override
                public void beforeClone(Object original, CloneContext context) {
                    captured.add(original);
                }
            };
            CloneContext context = CloneContext.create();

            listener.beforeClone("hello", context);

            assertThat(captured).containsExactly("hello");
        }

        @Test
        @DisplayName("自定义实现应接收afterClone回调")
        void customListenerShouldReceiveAfterClone() {
            List<Object> originals = new ArrayList<>();
            List<Object> clones = new ArrayList<>();
            CloneListener listener = new CloneListener() {
                @Override
                public void afterClone(Object original, Object cloned, CloneContext context) {
                    originals.add(original);
                    clones.add(cloned);
                }
            };
            CloneContext context = CloneContext.create();

            listener.afterClone("orig", "copy", context);

            assertThat(originals).containsExactly("orig");
            assertThat(clones).containsExactly("copy");
        }

        @Test
        @DisplayName("自定义实现应接收onError回调")
        void customListenerShouldReceiveOnError() {
            List<Throwable> errors = new ArrayList<>();
            CloneListener listener = new CloneListener() {
                @Override
                public void onError(Object original, Throwable error, CloneContext context) {
                    errors.add(error);
                }
            };
            CloneContext context = CloneContext.create();
            RuntimeException ex = new RuntimeException("test error");

            listener.onError("obj", ex, context);

            assertThat(errors).containsExactly(ex);
        }
    }

    @Nested
    @DisplayName("跟踪监听器测试")
    class TrackingListenerTests {

        @Test
        @DisplayName("监听器应跟踪所有回调")
        void listenerShouldTrackAllCallbacks() {
            List<String> events = new ArrayList<>();
            CloneListener listener = new CloneListener() {
                @Override
                public void beforeClone(Object original, CloneContext context) {
                    events.add("before:" + original);
                }

                @Override
                public void afterClone(Object original, Object cloned, CloneContext context) {
                    events.add("after:" + original + "->" + cloned);
                }
            };
            CloneContext context = CloneContext.create();

            listener.beforeClone("original", context);
            listener.afterClone("original", "cloned", context);

            assertThat(events).containsExactly("before:original", "after:original->cloned");
        }

        @Test
        @DisplayName("onError应传递正确的参数")
        void onErrorShouldReceiveCorrectParameters() {
            List<Object> captured = new ArrayList<>();
            CloneListener listener = new CloneListener() {
                @Override
                public void onError(Object original, Throwable error, CloneContext context) {
                    captured.add(original);
                    captured.add(error);
                }
            };
            CloneContext context = CloneContext.create();
            RuntimeException error = new RuntimeException("clone failed");

            listener.onError("target", error, context);

            assertThat(captured).hasSize(2);
            assertThat(captured.get(0)).isEqualTo("target");
            assertThat(captured.get(1)).isSameAs(error);
        }
    }

    @Nested
    @DisplayName("回调参数验证测试")
    class CallbackParameterTests {

        @Test
        @DisplayName("beforeClone应接收正确的原始对象")
        void beforeCloneShouldReceiveCorrectOriginal() {
            Object original = new Object();
            CloneContext context = CloneContext.create();
            List<Object> received = new ArrayList<>();

            CloneListener listener = new CloneListener() {
                @Override
                public void beforeClone(Object orig, CloneContext ctx) {
                    received.add(orig);
                    received.add(ctx);
                }
            };

            listener.beforeClone(original, context);

            assertThat(received).hasSize(2);
            assertThat(received.get(0)).isSameAs(original);
            assertThat(received.get(1)).isSameAs(context);
        }

        @Test
        @DisplayName("afterClone应接收正确的原始和克隆对象")
        void afterCloneShouldReceiveCorrectObjects() {
            Object original = new Object();
            Object cloned = new Object();
            CloneContext context = CloneContext.create();
            List<Object> received = new ArrayList<>();

            CloneListener listener = new CloneListener() {
                @Override
                public void afterClone(Object orig, Object clone, CloneContext ctx) {
                    received.add(orig);
                    received.add(clone);
                    received.add(ctx);
                }
            };

            listener.afterClone(original, cloned, context);

            assertThat(received).hasSize(3);
            assertThat(received.get(0)).isSameAs(original);
            assertThat(received.get(1)).isSameAs(cloned);
            assertThat(received.get(2)).isSameAs(context);
        }
    }
}
