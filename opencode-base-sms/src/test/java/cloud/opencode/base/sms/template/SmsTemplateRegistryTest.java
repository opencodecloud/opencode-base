package cloud.opencode.base.sms.template;

import cloud.opencode.base.sms.exception.SmsTemplateException;
import cloud.opencode.base.sms.message.SmsMessage;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsTemplateRegistryTest Tests
 * SmsTemplateRegistryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsTemplateRegistry 测试")
class SmsTemplateRegistryTest {

    private SmsTemplateRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SmsTemplateRegistry();
    }

    @Nested
    @DisplayName("register方法测试")
    class RegisterTests {

        @Test
        @DisplayName("注册模板成功")
        void testRegisterTemplate() {
            SmsTemplate template = SmsTemplate.of("verify", "Your code is ${code}");

            registry.register(template);

            assertThat(registry.contains("verify")).isTrue();
        }

        @Test
        @DisplayName("注册ID和内容")
        void testRegisterIdAndContent() {
            registry.register("test", "Test content ${var}");

            assertThat(registry.contains("test")).isTrue();
            assertThat(registry.get("test").content()).isEqualTo("Test content ${var}");
        }

        @Test
        @DisplayName("覆盖已存在的模板")
        void testRegisterOverwrite() {
            registry.register("test", "Original");
            registry.register("test", "Updated");

            assertThat(registry.get("test").content()).isEqualTo("Updated");
        }

        @Test
        @DisplayName("null模板抛出异常")
        void testRegisterNullTemplate() {
            assertThatThrownBy(() -> registry.register(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("unregister方法测试")
    class UnregisterTests {

        @Test
        @DisplayName("注销已存在的模板")
        void testUnregisterExisting() {
            registry.register("test", "content");

            registry.unregister("test");

            assertThat(registry.contains("test")).isFalse();
        }

        @Test
        @DisplayName("注销不存在的模板不抛出异常")
        void testUnregisterNonExisting() {
            assertThatNoException().isThrownBy(() -> registry.unregister("non-existent"));
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("返回已注册模板")
        void testGetExisting() {
            registry.register("verify", "Code: ${code}");

            SmsTemplate template = registry.get("verify");

            assertThat(template).isNotNull();
            assertThat(template.id()).isEqualTo("verify");
        }

        @Test
        @DisplayName("不存在的模板返回null")
        void testGetNonExisting() {
            SmsTemplate template = registry.get("non-existent");

            assertThat(template).isNull();
        }
    }

    @Nested
    @DisplayName("find方法测试")
    class FindTests {

        @Test
        @DisplayName("返回已存在模板的Optional")
        void testFindExisting() {
            registry.register("test", "content");

            Optional<SmsTemplate> result = registry.find("test");

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo("test");
        }

        @Test
        @DisplayName("不存在返回空Optional")
        void testFindNonExisting() {
            Optional<SmsTemplate> result = registry.find("non-existent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("contains方法测试")
    class ContainsTests {

        @Test
        @DisplayName("存在返回true")
        void testContainsTrue() {
            registry.register("test", "content");

            assertThat(registry.contains("test")).isTrue();
        }

        @Test
        @DisplayName("不存在返回false")
        void testContainsFalse() {
            assertThat(registry.contains("non-existent")).isFalse();
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("空注册表返回0")
        void testSizeEmpty() {
            assertThat(registry.size()).isZero();
        }

        @Test
        @DisplayName("返回正确数量")
        void testSizeWithTemplates() {
            registry.register("t1", "c1");
            registry.register("t2", "c2");
            registry.register("t3", "c3");

            assertThat(registry.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getAll方法测试")
    class GetAllTests {

        @Test
        @DisplayName("空注册表返回空映射")
        void testGetAllEmpty() {
            Map<String, SmsTemplate> all = registry.getAll();

            assertThat(all).isEmpty();
        }

        @Test
        @DisplayName("返回所有模板")
        void testGetAllWithTemplates() {
            registry.register("t1", "c1");
            registry.register("t2", "c2");

            Map<String, SmsTemplate> all = registry.getAll();

            assertThat(all).hasSize(2);
            assertThat(all).containsKey("t1");
            assertThat(all).containsKey("t2");
        }
    }

    @Nested
    @DisplayName("createMessage方法测试")
    class CreateMessageTests {

        @Test
        @DisplayName("创建消息成功")
        void testCreateMessage() {
            registry.register("verify", "Your code is ${code}");

            SmsMessage message = registry.createMessage("verify", "13800138000", Map.of("code", "123456"));

            assertThat(message.phoneNumber()).isEqualTo("13800138000");
            assertThat(message.content()).isEqualTo("Your code is 123456");
            assertThat(message.templateId()).isEqualTo("verify");
        }

        @Test
        @DisplayName("模板不存在抛出异常")
        void testCreateMessageTemplateNotFound() {
            assertThatThrownBy(() -> registry.createMessage("non-existent", "13800138000", Map.of()))
                    .isInstanceOf(SmsTemplateException.class);
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除所有模板")
        void testClear() {
            registry.register("t1", "c1");
            registry.register("t2", "c2");

            registry.clear();

            assertThat(registry.size()).isZero();
        }
    }
}
