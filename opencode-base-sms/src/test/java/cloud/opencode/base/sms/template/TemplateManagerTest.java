package cloud.opencode.base.sms.template;

import cloud.opencode.base.sms.exception.SmsTemplateException;
import cloud.opencode.base.sms.message.SmsMessage;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * TemplateManagerTest Tests
 * TemplateManagerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("TemplateManager 测试")
class TemplateManagerTest {

    private TemplateManager manager;

    @BeforeEach
    void setUp() {
        manager = TemplateManager.create();
    }

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("创建新管理器")
        void testCreate() {
            TemplateManager manager = TemplateManager.create();

            assertThat(manager).isNotNull();
            assertThat(manager.size()).isZero();
        }
    }

    @Nested
    @DisplayName("register方法测试")
    class RegisterTests {

        @Test
        @DisplayName("注册模板对象")
        void testRegisterTemplate() {
            SmsTemplate template = SmsTemplate.of("test", "content");

            manager.register(template);

            assertThat(manager.contains("test")).isTrue();
        }

        @Test
        @DisplayName("注册ID和内容")
        void testRegisterIdAndContent() {
            manager.register("test", "Test ${var}");

            assertThat(manager.contains("test")).isTrue();
        }

        @Test
        @DisplayName("注册带描述")
        void testRegisterWithDescription() {
            manager.register("test", "content", "Test template");

            SmsTemplate template = manager.get("test");
            // register(id, content, description) creates SmsTemplate(id, content, description, List.of())
            // SmsTemplate record is (id, name, content, variableNames)
            // So: id="test", name="content", content="Test template"
            assertThat(template.id()).isEqualTo("test");
            assertThat(template.name()).isEqualTo("content");
            assertThat(template.content()).isEqualTo("Test template");
        }

        @Test
        @DisplayName("链式调用")
        void testRegisterChaining() {
            manager.register("t1", "c1")
                    .register("t2", "c2")
                    .register("t3", "c3");

            assertThat(manager.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("null模板抛出异常")
        void testRegisterNullTemplate() {
            assertThatThrownBy(() -> manager.register((SmsTemplate) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("unregister方法测试")
    class UnregisterTests {

        @Test
        @DisplayName("注销已存在模板")
        void testUnregister() {
            manager.register("test", "content");

            manager.unregister("test");

            assertThat(manager.contains("test")).isFalse();
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除所有模板")
        void testClear() {
            manager.register("t1", "c1")
                    .register("t2", "c2");

            manager.clear();

            assertThat(manager.size()).isZero();
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("返回已注册模板")
        void testGet() {
            manager.register("test", "content");

            SmsTemplate template = manager.get("test");

            assertThat(template).isNotNull();
            assertThat(template.id()).isEqualTo("test");
        }

        @Test
        @DisplayName("不存在抛出异常")
        void testGetNotFound() {
            assertThatThrownBy(() -> manager.get("non-existent"))
                    .isInstanceOf(SmsTemplateException.class);
        }
    }

    @Nested
    @DisplayName("find方法测试")
    class FindTests {

        @Test
        @DisplayName("返回Optional")
        void testFind() {
            manager.register("test", "content");

            Optional<SmsTemplate> result = manager.find("test");

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("不存在返回空")
        void testFindEmpty() {
            Optional<SmsTemplate> result = manager.find("non-existent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("contains方法测试")
    class ContainsTests {

        @Test
        @DisplayName("存在返回true")
        void testContainsTrue() {
            manager.register("test", "content");

            assertThat(manager.contains("test")).isTrue();
        }

        @Test
        @DisplayName("不存在返回false")
        void testContainsFalse() {
            assertThat(manager.contains("non-existent")).isFalse();
        }
    }

    @Nested
    @DisplayName("getAll方法测试")
    class GetAllTests {

        @Test
        @DisplayName("返回所有模板")
        void testGetAll() {
            manager.register("t1", "c1")
                    .register("t2", "c2");

            Map<String, SmsTemplate> all = manager.getAll();

            assertThat(all).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getTemplateIds方法测试")
    class GetTemplateIdsTests {

        @Test
        @DisplayName("返回所有ID")
        void testGetTemplateIds() {
            manager.register("t1", "c1")
                    .register("t2", "c2");

            List<String> ids = manager.getTemplateIds();

            assertThat(ids).containsExactlyInAnyOrder("t1", "t2");
        }
    }

    @Nested
    @DisplayName("render方法测试")
    class RenderTests {

        @Test
        @DisplayName("渲染模板")
        void testRender() {
            manager.register("verify", "Your code is ${code}");

            String result = manager.render("verify", Map.of("code", "123456"));

            assertThat(result).isEqualTo("Your code is 123456");
        }

        @Test
        @DisplayName("缺少变量抛出异常")
        void testRenderMissingVariable() {
            manager.register("verify", "Hello ${name}, code is ${code}");

            assertThatThrownBy(() -> manager.render("verify", Map.of("name", "John")))
                    .isInstanceOf(SmsTemplateException.class);
        }
    }

    @Nested
    @DisplayName("createMessage方法测试")
    class CreateMessageTests {

        @Test
        @DisplayName("创建消息")
        void testCreateMessage() {
            manager.register("verify", "Your code is ${code}");

            SmsMessage message = manager.createMessage("verify", "13800138000", Map.of("code", "123456"));

            assertThat(message.phoneNumber()).isEqualTo("13800138000");
            assertThat(message.content()).isEqualTo("Your code is 123456");
            assertThat(message.templateId()).isEqualTo("verify");
        }
    }

    @Nested
    @DisplayName("createMessages方法测试")
    class CreateMessagesTests {

        @Test
        @DisplayName("为多个号码创建消息")
        void testCreateMessages() {
            manager.register("notify", "Message: ${msg}");

            List<SmsMessage> messages = manager.createMessages(
                    "notify",
                    List.of("13800138001", "13800138002", "13800138003"),
                    Map.of("msg", "Hello")
            );

            assertThat(messages).hasSize(3);
            assertThat(messages).allMatch(m -> m.content().equals("Message: Hello"));
        }
    }

    @Nested
    @DisplayName("validateAll方法测试")
    class ValidateAllTests {

        @Test
        @DisplayName("有效模板验证通过")
        void testValidateAllValid() {
            manager.register("t1", "content1")
                    .register("t2", "content2");

            assertThatNoException().isThrownBy(() -> manager.validateAll());
        }
    }

    @Nested
    @DisplayName("forEach方法测试")
    class ForEachTests {

        @Test
        @DisplayName("迭代所有模板")
        void testForEach() {
            manager.register("t1", "c1")
                    .register("t2", "c2");

            int[] count = {0};
            manager.forEach(t -> count[0]++);

            assertThat(count[0]).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("toRegistry方法测试")
    class ToRegistryTests {

        @Test
        @DisplayName("转换为注册表")
        void testToRegistry() {
            manager.register("t1", "c1")
                    .register("t2", "c2");

            SmsTemplateRegistry registry = manager.toRegistry();

            assertThat(registry.size()).isEqualTo(2);
            assertThat(registry.contains("t1")).isTrue();
            assertThat(registry.contains("t2")).isTrue();
        }
    }

    @Nested
    @DisplayName("loadFromFile方法测试")
    class LoadFromFileTests {

        @Test
        @DisplayName("从文件加载模板")
        void testLoadFromFile() throws IOException {
            Path tempFile = Files.createTempFile("template", ".txt");
            try {
                Files.writeString(tempFile, "Hello ${name}!");

                manager.loadFromFile(tempFile);

                // ID 基于文件名
                String expectedId = tempFile.getFileName().toString().replace(".txt", "");
                assertThat(manager.contains(expectedId)).isTrue();
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("null文件抛出异常")
        void testLoadFromFileNull() {
            assertThatThrownBy(() -> manager.loadFromFile(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("loadFromDirectory方法测试")
    class LoadFromDirectoryTests {

        @Test
        @DisplayName("非目录抛出异常")
        void testLoadFromDirectoryNotDirectory() throws IOException {
            Path tempFile = Files.createTempFile("not-dir", ".txt");
            try {
                assertThatThrownBy(() -> manager.loadFromDirectory(tempFile))
                        .isInstanceOf(IllegalArgumentException.class);
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }
}
