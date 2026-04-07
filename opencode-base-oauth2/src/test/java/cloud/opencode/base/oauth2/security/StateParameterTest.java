package cloud.opencode.base.oauth2.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * StateParameter Tests
 * StateParameter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("StateParameter 测试")
class StateParameterTest {

    @Nested
    @DisplayName("generate() 方法测试")
    class GenerateTests {

        @Test
        @DisplayName("generate() 创建非空 URL 安全 Base64 编码字符串")
        void testGenerateDefault() {
            String state = StateParameter.generate();

            assertThat(state).isNotNull();
            assertThat(state).isNotEmpty();
            // URL-safe Base64 without padding: no +, /, or =
            assertThat(state).doesNotContain("+");
            assertThat(state).doesNotContain("/");
            assertThat(state).doesNotEndWith("=");
        }

        @Test
        @DisplayName("generate() 生成 32 字节数据的 Base64 编码（43 字符）")
        void testGenerateDefaultLength() {
            String state = StateParameter.generate();
            // 32 bytes -> ceil(32*4/3) = 43 chars in base64 without padding
            assertThat(state).hasSize(43);
        }

        @Test
        @DisplayName("generate() 每次生成唯一值")
        void testGenerateUniqueness() {
            Set<String> states = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                states.add(StateParameter.generate());
            }
            assertThat(states).hasSize(100);
        }

        @Test
        @DisplayName("generate() 输出可 Base64 URL 解码")
        void testGenerateDecodable() {
            String state = StateParameter.generate();
            byte[] decoded = Base64.getUrlDecoder().decode(state);
            assertThat(decoded).hasSize(32);
        }
    }

    @Nested
    @DisplayName("generate(int) 方法测试")
    class GenerateWithSizeTests {

        @Test
        @DisplayName("generate(16) 使用最小字节数生成")
        void testGenerateMinBytes() {
            String state = StateParameter.generate(16);
            assertThat(state).isNotNull().isNotEmpty();
            byte[] decoded = Base64.getUrlDecoder().decode(state);
            assertThat(decoded).hasSize(16);
        }

        @Test
        @DisplayName("generate(64) 使用自定义字节数生成")
        void testGenerateCustomBytes() {
            String state = StateParameter.generate(64);
            assertThat(state).isNotNull().isNotEmpty();
            byte[] decoded = Base64.getUrlDecoder().decode(state);
            assertThat(decoded).hasSize(64);
        }

        @Test
        @DisplayName("generate(15) 小于最小字节数抛出异常")
        void testGenerateTooFewBytes() {
            assertThatThrownBy(() -> StateParameter.generate(15))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("16");
        }

        @Test
        @DisplayName("generate(0) 零字节抛出异常")
        void testGenerateZeroBytes() {
            assertThatThrownBy(() -> StateParameter.generate(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("generate(-1) 负数字节抛出异常")
        void testGenerateNegativeBytes() {
            assertThatThrownBy(() -> StateParameter.generate(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("validate() 方法测试")
    class ValidateTests {

        @Test
        @DisplayName("validate 相同值返回 true")
        void testValidateMatching() {
            String state = StateParameter.generate();
            assertThat(StateParameter.validate(state, state)).isTrue();
        }

        @Test
        @DisplayName("validate 不同值返回 false")
        void testValidateNotMatching() {
            String state1 = StateParameter.generate();
            String state2 = StateParameter.generate();
            assertThat(StateParameter.validate(state1, state2)).isFalse();
        }

        @Test
        @DisplayName("validate 相同内容不同对象返回 true")
        void testValidateSameContent() {
            String state = StateParameter.generate();
            String copy = new String(state);
            assertThat(StateParameter.validate(state, copy)).isTrue();
        }

        @Test
        @DisplayName("validate expected 为 null 返回 false")
        void testValidateNullExpected() {
            assertThat(StateParameter.validate(null, "state")).isFalse();
        }

        @Test
        @DisplayName("validate actual 为 null 返回 false")
        void testValidateNullActual() {
            assertThat(StateParameter.validate("state", null)).isFalse();
        }

        @Test
        @DisplayName("validate 两个 null 返回 false")
        void testValidateBothNull() {
            assertThat(StateParameter.validate(null, null)).isFalse();
        }

        @Test
        @DisplayName("validate 空字符串匹配")
        void testValidateEmptyStrings() {
            assertThat(StateParameter.validate("", "")).isTrue();
        }

        @Test
        @DisplayName("validate 大小写敏感")
        void testValidateCaseSensitive() {
            assertThat(StateParameter.validate("State", "state")).isFalse();
        }
    }

    @Nested
    @DisplayName("generateWithTimestamp() 方法测试")
    class GenerateWithTimestampTests {

        @Test
        @DisplayName("generateWithTimestamp 返回有效的 StateData")
        void testGenerateWithTimestamp() {
            StateParameter.StateData data = StateParameter.generateWithTimestamp();

            assertThat(data).isNotNull();
            assertThat(data.state()).isNotNull().isNotEmpty();
            assertThat(data.createdAt()).isNotNull();
            assertThat(data.createdAt()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("generateWithTimestamp 生成唯一 state")
        void testGenerateWithTimestampUnique() {
            StateParameter.StateData data1 = StateParameter.generateWithTimestamp();
            StateParameter.StateData data2 = StateParameter.generateWithTimestamp();

            assertThat(data1.state()).isNotEqualTo(data2.state());
        }
    }

    @Nested
    @DisplayName("StateData 测试")
    class StateDataTests {

        @Test
        @DisplayName("StateData 构造验证 null state")
        void testStateDataNullState() {
            assertThatThrownBy(() -> new StateParameter.StateData(null, Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("state");
        }

        @Test
        @DisplayName("StateData 构造验证 null createdAt")
        void testStateDataNullCreatedAt() {
            assertThatThrownBy(() -> new StateParameter.StateData("state", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("createdAt");
        }

        @Test
        @DisplayName("isExpired - 未过期返回 false")
        void testIsExpiredNotExpired() {
            StateParameter.StateData data = new StateParameter.StateData("state", Instant.now());
            assertThat(data.isExpired(Duration.ofMinutes(10))).isFalse();
        }

        @Test
        @DisplayName("isExpired - 已过期返回 true")
        void testIsExpiredExpired() {
            StateParameter.StateData data = new StateParameter.StateData("state",
                    Instant.now().minus(Duration.ofMinutes(15)));
            assertThat(data.isExpired(Duration.ofMinutes(10))).isTrue();
        }

        @Test
        @DisplayName("isExpired - 恰好在边界上")
        void testIsExpiredEdge() {
            Instant created = Instant.now().minus(Duration.ofMinutes(10));
            StateParameter.StateData data = new StateParameter.StateData("state", created);
            // 10 minutes old with 10 minute maxAge should be expired (now > createdAt + maxAge)
            assertThat(data.isExpired(Duration.ofMinutes(10))).isTrue();
        }

        @Test
        @DisplayName("isExpired - null maxAge 抛出异常")
        void testIsExpiredNullMaxAge() {
            StateParameter.StateData data = StateParameter.generateWithTimestamp();
            assertThatThrownBy(() -> data.isExpired(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("maxAge");
        }

        @Test
        @DisplayName("StateData record 方法工作正常")
        void testStateDataRecord() {
            Instant now = Instant.now();
            StateParameter.StateData data1 = new StateParameter.StateData("state1", now);
            StateParameter.StateData data2 = new StateParameter.StateData("state1", now);

            assertThat(data1).isEqualTo(data2);
            assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
            assertThat(data1.toString()).contains("state1");
        }
    }

    @Nested
    @DisplayName("安全性测试")
    class SecurityTests {

        @Test
        @DisplayName("生成的 state 仅包含 URL 安全字符")
        void testUrlSafeCharacters() {
            for (int i = 0; i < 50; i++) {
                String state = StateParameter.generate();
                // URL-safe Base64: [A-Za-z0-9_-]
                assertThat(state).matches("[A-Za-z0-9_\\-]+");
            }
        }

        @Test
        @DisplayName("生成的 state 具有足够的熵")
        void testSufficientEntropy() {
            // 32 bytes = 256 bits of entropy; verify no obvious patterns
            String state = StateParameter.generate();
            byte[] decoded = Base64.getUrlDecoder().decode(state);
            assertThat(decoded).hasSize(32);

            // Check not all zeros or all ones
            boolean allZero = true;
            for (byte b : decoded) {
                if (b != 0) {
                    allZero = false;
                    break;
                }
            }
            assertThat(allZero).isFalse();
        }
    }
}
