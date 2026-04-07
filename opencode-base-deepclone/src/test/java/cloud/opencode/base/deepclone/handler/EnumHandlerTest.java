package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.OpenClone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EnumHandler 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("EnumHandler 测试")
class EnumHandlerTest {

    private EnumHandler handler;
    private Cloner cloner;
    private CloneContext context;

    enum Color { RED, GREEN, BLUE }

    enum SingleValue { INSTANCE }

    @BeforeEach
    void setUp() {
        handler = new EnumHandler();
        cloner = OpenClone.getDefaultCloner();
        context = CloneContext.create();
    }

    @Nested
    @DisplayName("clone() 测试")
    class CloneTests {

        @Test
        @DisplayName("克隆枚举应返回相同实例")
        void cloneShouldReturnSameInstance() {
            Enum<?> result = handler.clone(Color.RED, cloner, context);

            assertThat(result).isSameAs(Color.RED);
        }

        @Test
        @DisplayName("克隆不同枚举值应返回对应相同实例")
        void cloneShouldReturnSameInstanceForEachValue() {
            assertThat(handler.clone(Color.RED, cloner, context)).isSameAs(Color.RED);
            assertThat(handler.clone(Color.GREEN, cloner, context)).isSameAs(Color.GREEN);
            assertThat(handler.clone(Color.BLUE, cloner, context)).isSameAs(Color.BLUE);
        }

        @Test
        @DisplayName("克隆单值枚举应返回相同实例")
        void cloneSingleValueEnumShouldReturnSameInstance() {
            Enum<?> result = handler.clone(SingleValue.INSTANCE, cloner, context);

            assertThat(result).isSameAs(SingleValue.INSTANCE);
        }

        @Test
        @DisplayName("克隆null应返回null")
        void cloneNullShouldReturnNull() {
            Enum<?> result = handler.clone(null, cloner, context);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("supports() 测试")
    class SupportsTests {

        @Test
        @DisplayName("应支持枚举类型")
        void shouldSupportEnumTypes() {
            assertThat(handler.supports(Color.class)).isTrue();
            assertThat(handler.supports(SingleValue.class)).isTrue();
        }

        @Test
        @DisplayName("不应支持非枚举类型")
        void shouldNotSupportNonEnumTypes() {
            assertThat(handler.supports(String.class)).isFalse();
            assertThat(handler.supports(Integer.class)).isFalse();
            assertThat(handler.supports(Object.class)).isFalse();
        }

        @Test
        @DisplayName("null类型应返回false")
        void shouldReturnFalseForNull() {
            assertThat(handler.supports(null)).isFalse();
        }

        @Test
        @DisplayName("不应支持Enum基类本身")
        void shouldNotSupportEnumBaseClass() {
            // Enum.class itself is not an enum (isEnum() returns false)
            assertThat(handler.supports(Enum.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("priority() 测试")
    class PriorityTests {

        @Test
        @DisplayName("优先级应为5")
        void priorityShouldBeFive() {
            assertThat(handler.priority()).isEqualTo(5);
        }

        @Test
        @DisplayName("优先级应高于默认值100")
        void priorityShouldBeHigherThanDefault() {
            assertThat(handler.priority()).isLessThan(100);
        }
    }

    @Nested
    @DisplayName("引用相等性测试")
    class ReferenceEqualityTests {

        @Test
        @DisplayName("克隆结果应与原始引用相同（==检查）")
        void clonedEnumShouldBeReferenceEqual() {
            Color original = Color.BLUE;
            Enum<?> cloned = handler.clone(original, cloner, context);

            // Strict reference equality check
            //noinspection ConstantValue
            assertThat(cloned == original).isTrue();
        }
    }
}
