package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.OpenClone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OptionalHandler 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("OptionalHandler 测试")
class OptionalHandlerTest {

    private OptionalHandler handler;
    private Cloner cloner;
    private CloneContext context;

    static class Person {
        String name;
        int age;

        Person() {}

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @BeforeEach
    void setUp() {
        handler = new OptionalHandler();
        cloner = OpenClone.getDefaultCloner();
        context = CloneContext.create();
    }

    @Nested
    @DisplayName("clone() 测试")
    class CloneTests {

        @Test
        @DisplayName("克隆Optional.empty()应返回empty")
        @SuppressWarnings("unchecked")
        void cloneEmptyShouldReturnEmpty() {
            Optional<String> original = Optional.empty();

            Optional<String> result = handler.clone(original, cloner, context);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("克隆Optional.of(字符串)应返回包含相同值的Optional")
        @SuppressWarnings("unchecked")
        void cloneStringOptionalShouldReturnOptionalWithValue() {
            Optional<String> original = Optional.of("hello");

            Optional<String> result = handler.clone(original, cloner, context);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("克隆包含复杂对象的Optional应深度克隆内容")
        @SuppressWarnings("unchecked")
        void cloneComplexOptionalShouldDeepCloneContent() {
            Person person = new Person("Alice", 30);
            Optional<Person> original = Optional.of(person);

            Optional<Person> result = handler.clone(original, cloner, context);

            assertThat(result).isPresent();
            assertThat(result.get()).isNotSameAs(person);
            assertThat(result.get().name).isEqualTo("Alice");
            assertThat(result.get().age).isEqualTo(30);
        }

        @Test
        @DisplayName("克隆null输入应返回null")
        void cloneNullShouldReturnNull() {
            Optional<?> result = handler.clone(null, cloner, context);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("克隆结果应为不同的Optional实例")
        @SuppressWarnings("unchecked")
        void cloneShouldReturnDifferentInstance() {
            Optional<String> original = Optional.of("test");

            Optional<String> result = handler.clone(original, cloner, context);

            assertThat(result).isNotSameAs(original);
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("test");
        }

        @Test
        @DisplayName("克隆包含List的Optional应深度克隆")
        @SuppressWarnings("unchecked")
        void cloneOptionalWithListShouldDeepClone() {
            List<String> list = new java.util.ArrayList<>(List.of("a", "b", "c"));
            Optional<List<String>> original = Optional.of(list);

            Optional<List<String>> result = handler.clone(original, cloner, context);

            assertThat(result).isPresent();
            assertThat(result.get()).isNotSameAs(list);
            assertThat(result.get()).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("supports() 测试")
    class SupportsTests {

        @Test
        @DisplayName("应支持Optional类型")
        void shouldSupportOptional() {
            assertThat(handler.supports(Optional.class)).isTrue();
        }

        @Test
        @DisplayName("不应支持其他类型")
        void shouldNotSupportOtherTypes() {
            assertThat(handler.supports(String.class)).isFalse();
            assertThat(handler.supports(List.class)).isFalse();
            assertThat(handler.supports(Object.class)).isFalse();
            assertThat(handler.supports(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("null类型应返回false")
        void shouldReturnFalseForNull() {
            assertThat(handler.supports(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("priority() 测试")
    class PriorityTests {

        @Test
        @DisplayName("优先级应为15")
        void priorityShouldBeFifteen() {
            assertThat(handler.priority()).isEqualTo(15);
        }

        @Test
        @DisplayName("优先级应高于默认值100")
        void priorityShouldBeHigherThanDefault() {
            assertThat(handler.priority()).isLessThan(100);
        }
    }
}
