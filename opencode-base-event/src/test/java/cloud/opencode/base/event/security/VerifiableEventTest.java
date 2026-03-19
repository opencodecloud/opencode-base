package cloud.opencode.base.event.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * VerifiableEvent 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("VerifiableEvent 测试")
class VerifiableEventTest {

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("可实现为Lambda")
        void testImplementableAsLambda() {
            // VerifiableEvent不是函数式接口，但可以匿名实现
            VerifiableEvent event = new VerifiableEvent() {
                @Override
                public String getSignature() {
                    return "test-signature";
                }

                @Override
                public boolean verify(String secret) {
                    return "correct-secret".equals(secret);
                }
            };

            assertThat(event.getSignature()).isEqualTo("test-signature");
            assertThat(event.verify("correct-secret")).isTrue();
            assertThat(event.verify("wrong-secret")).isFalse();
        }
    }
}
