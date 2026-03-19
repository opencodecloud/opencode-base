package cloud.opencode.base.string.desensitize;

import cloud.opencode.base.string.desensitize.annotation.*;
import cloud.opencode.base.string.desensitize.strategy.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DesensitizeProcessorTest Tests
 * DesensitizeProcessorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("DesensitizeProcessor Tests")
class DesensitizeProcessorTest {

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            Constructor<DesensitizeProcessor> constructor = DesensitizeProcessor.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class);
        }
    }

    @Nested
    @DisplayName("Process Tests")
    class ProcessTests {

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            Object result = DesensitizeProcessor.process(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return same object if not annotated")
        void shouldReturnSameObjectIfNotAnnotated() {
            NonAnnotatedClass obj = new NonAnnotatedClass();
            obj.phone = "13812345678";
            NonAnnotatedClass result = DesensitizeProcessor.process(obj);
            assertThat(result).isSameAs(obj);
            assertThat(result.phone).isEqualTo("13812345678");
        }

        @Test
        @DisplayName("Should desensitize annotated fields")
        void shouldDesensitizeAnnotatedFields() {
            AnnotatedPerson person = new AnnotatedPerson();
            person.phone = "13812345678";
            person.name = "张三";
            person.notAnnotated = "keep this";

            DesensitizeProcessor.process(person);

            assertThat(person.phone).isEqualTo("138****5678");
            assertThat(person.notAnnotated).isEqualTo("keep this");
        }

        @Test
        @DisplayName("Should handle multiple annotated fields")
        void shouldHandleMultipleAnnotatedFields() {
            AnnotatedPerson person = new AnnotatedPerson();
            person.phone = "13812345678";
            person.email = "test@example.com";

            DesensitizeProcessor.process(person);

            assertThat(person.phone).isEqualTo("138****5678");
            assertThat(person.email).isEqualTo("t***t@example.com");
        }

        @Test
        @DisplayName("Should handle null field values")
        void shouldHandleNullFieldValues() {
            AnnotatedPerson person = new AnnotatedPerson();
            person.phone = null;

            DesensitizeProcessor.process(person);

            assertThat(person.phone).isNull();
        }

        @Test
        @DisplayName("Should return same object after processing")
        void shouldReturnSameObjectAfterProcessing() {
            AnnotatedPerson person = new AnnotatedPerson();
            AnnotatedPerson result = DesensitizeProcessor.process(person);
            assertThat(result).isSameAs(person);
        }

        @Test
        @DisplayName("Should handle non-string fields gracefully")
        void shouldHandleNonStringFieldsGracefully() {
            AnnotatedWithNonString obj = new AnnotatedWithNonString();
            obj.number = 123;

            DesensitizeProcessor.process(obj);

            assertThat(obj.number).isEqualTo(123);
        }
    }

    // Test classes

    static class NonAnnotatedClass {
        String phone;
    }

    @DesensitizeBean
    static class AnnotatedPerson {
        @Desensitize(DesensitizeType.MOBILE_PHONE)
        String phone;

        @Desensitize(DesensitizeType.EMAIL)
        String email;

        @Desensitize(DesensitizeType.CHINESE_NAME)
        String name;

        String notAnnotated;
    }

    @DesensitizeBean
    static class AnnotatedWithNonString {
        @Desensitize(DesensitizeType.MOBILE_PHONE)
        Integer number;
    }
}
