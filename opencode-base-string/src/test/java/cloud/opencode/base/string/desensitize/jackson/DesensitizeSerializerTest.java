package cloud.opencode.base.string.desensitize.jackson;

import cloud.opencode.base.string.desensitize.annotation.*;
import cloud.opencode.base.string.desensitize.strategy.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DesensitizeSerializerTest Tests
 * DesensitizeSerializerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("DesensitizeSerializer Tests")
class DesensitizeSerializerTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create serializer")
        void shouldCreateSerializer() {
            DesensitizeSerializer serializer = new DesensitizeSerializer();
            assertThat(serializer).isNotNull();
            assertThat(serializer.handledType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should register with ObjectMapper")
        void shouldRegisterWithObjectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            DesensitizeModule module = new DesensitizeModule();
            mapper.registerModule(module);

            assertThat(mapper.getRegisteredModuleIds()).contains(module.getTypeId());
        }

        @Test
        @DisplayName("Should serialize plain object")
        void shouldSerializePlainObject() throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new DesensitizeModule());

            PlainPerson person = new PlainPerson();
            person.name = "张三";
            person.phone = "13812345678";

            String json = mapper.writeValueAsString(person);
            assertThat(json).contains("张三");
            assertThat(json).contains("13812345678");
        }
    }

    static class PlainPerson {
        public String name;
        public String phone;
    }
}
