package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.config.SmsConfig;
import cloud.opencode.base.sms.config.SmsProviderType;
import cloud.opencode.base.sms.exception.SmsException;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsProviderFactoryTest Tests
 * SmsProviderFactoryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsProviderFactory 测试")
class SmsProviderFactoryTest {

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("创建CONSOLE提供商")
        void testCreateConsole() {
            SmsConfig config = SmsConfig.builder()
                    .providerType(SmsProviderType.CONSOLE)
                    .accessKey("test")
                    .secretKey("test")
                    .signName("Test")
                    .build();

            SmsProvider provider = SmsProviderFactory.create(config);

            assertThat(provider).isNotNull();
            assertThat(provider).isInstanceOf(ConsoleSmsProvider.class);
        }

        @Test
        @DisplayName("null配置抛出异常")
        void testCreateWithNullConfig() {
            assertThatThrownBy(() -> SmsProviderFactory.create(null))
                    .isInstanceOf(SmsException.class);
        }

        @Test
        @DisplayName("无providerType配置抛出异常")
        void testCreateWithNullProviderType() {
            SmsConfig config = SmsConfig.builder()
                    .accessKey("test")
                    .secretKey("test")
                    .signName("Test")
                    .build();

            assertThatThrownBy(() -> SmsProviderFactory.create(config))
                    .isInstanceOf(SmsException.class);
        }
    }

    @Nested
    @DisplayName("console方法测试")
    class ConsoleTests {

        @Test
        @DisplayName("返回控制台提供商")
        void testConsole() {
            SmsProvider provider = SmsProviderFactory.console();

            assertThat(provider).isNotNull();
            assertThat(provider.getName()).isEqualToIgnoringCase("CONSOLE");
            assertThat(provider.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("registerProvider方法测试")
    class RegisterProviderTests {

        @Test
        @DisplayName("注册自定义提供商")
        void testRegisterProvider() {
            SmsProviderFactory.registerProvider(SmsProviderType.CUSTOM, config -> new TestSmsProvider());

            SmsConfig config = SmsConfig.builder()
                    .providerType(SmsProviderType.CUSTOM)
                    .accessKey("test")
                    .secretKey("test")
                    .signName("Test")
                    .build();

            SmsProvider provider = SmsProviderFactory.create(config);

            assertThat(provider).isNotNull();
            assertThat(provider.getName()).isEqualTo("TestProvider");
        }
    }

    @Nested
    @DisplayName("unregisterProvider方法测试")
    class UnregisterProviderTests {

        @Test
        @DisplayName("注销已注册的提供商")
        void testUnregisterProvider() {
            SmsProviderFactory.registerProvider(SmsProviderType.CUSTOM, config -> new TestSmsProvider());

            SmsProviderFactory.unregisterProvider(SmsProviderType.CUSTOM);

            SmsConfig config = SmsConfig.builder()
                    .providerType(SmsProviderType.CUSTOM)
                    .accessKey("test")
                    .secretKey("test")
                    .signName("Test")
                    .build();

            assertThatThrownBy(() -> SmsProviderFactory.create(config))
                    .isInstanceOf(SmsException.class);
        }
    }

    // Test helper class
    private static class TestSmsProvider implements SmsProvider {
        @Override
        public SmsResult send(SmsMessage message) {
            return SmsResult.success("test-msg-id", message.phoneNumber());
        }

        @Override
        public List<SmsResult> sendBatch(List<SmsMessage> messages) {
            return messages.stream()
                    .map(m -> SmsResult.success("test-msg-id", m.phoneNumber()))
                    .toList();
        }

        @Override
        public String getName() {
            return "TestProvider";
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}
