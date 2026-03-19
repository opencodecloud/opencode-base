package cloud.opencode.base.deepclone.cloner;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.handler.TypeHandler;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * AbstractClonerTest Tests
 * AbstractClonerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("AbstractCloner 抽象基类测试")
class AbstractClonerTest {

    private ReflectiveCloner cloner;

    @BeforeEach
    void setup() {
        cloner = ReflectiveCloner.create();
    }

    @Nested
    @DisplayName("不可变类型检测测试")
    class ImmutableTypeTests {

        @Test
        @DisplayName("String是不可变类型")
        void testStringImmutable() {
            assertThat(cloner.isImmutable(String.class)).isTrue();
        }

        @Test
        @DisplayName("Integer是不可变类型")
        void testIntegerImmutable() {
            assertThat(cloner.isImmutable(Integer.class)).isTrue();
        }

        @Test
        @DisplayName("BigDecimal是不可变类型")
        void testBigDecimalImmutable() {
            assertThat(cloner.isImmutable(BigDecimal.class)).isTrue();
        }

        @Test
        @DisplayName("LocalDate是不可变类型")
        void testLocalDateImmutable() {
            assertThat(cloner.isImmutable(LocalDate.class)).isTrue();
        }

        @Test
        @DisplayName("Instant是不可变类型")
        void testInstantImmutable() {
            assertThat(cloner.isImmutable(Instant.class)).isTrue();
        }

        @Test
        @DisplayName("UUID是不可变类型")
        void testUUIDImmutable() {
            assertThat(cloner.isImmutable(UUID.class)).isTrue();
        }

        @Test
        @DisplayName("枚举类型是不可变的")
        void testEnumImmutable() {
            assertThat(cloner.isImmutable(Thread.State.class)).isTrue();
        }

        @Test
        @DisplayName("基本类型是不可变的")
        void testPrimitiveImmutable() {
            assertThat(cloner.isImmutable(int.class)).isTrue();
        }

        @Test
        @DisplayName("null类型是不可变的")
        void testNullTypeImmutable() {
            assertThat(cloner.isImmutable(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("自定义不可变类型注册测试")
    class CustomImmutableTests {

        @Test
        @DisplayName("注册自定义不可变类型")
        void testRegisterImmutable() {
            cloner.registerImmutable(StringBuilder.class);
            assertThat(cloner.isImmutable(StringBuilder.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("setMaxDepth设置最大深度")
        void testSetMaxDepth() {
            cloner.setMaxDepth(50);
            // Test that cloning still works with new max depth
            String result = cloner.clone("test");
            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("setCloneTransient设置是否克隆transient字段")
        void testSetCloneTransient() {
            assertThatNoException().isThrownBy(() -> cloner.setCloneTransient(true));
        }
    }

    @Nested
    @DisplayName("clone方法测试")
    class CloneTests {

        @Test
        @DisplayName("clone(null)返回null")
        void testCloneNull() {
            Object result = cloner.clone(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("clone不可变类型返回原对象")
        void testCloneImmutableReturnsSame() {
            String original = "hello";
            String cloned = cloner.clone(original);
            assertThat(cloned).isSameAs(original);
        }

        @Test
        @DisplayName("clone(null, context)返回null")
        void testCloneNullWithContext() {
            CloneContext context = CloneContext.create(100);
            Object result = cloner.clone(null, context);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("类型处理器管理测试")
    class TypeHandlerTests {

        @Test
        @DisplayName("注册和获取类型处理器")
        void testRegisterHandler() {
            TypeHandler<StringBuilder> handler = new TypeHandler<>() {
                @Override
                public StringBuilder clone(StringBuilder original, Cloner clonerRef, CloneContext context) {
                    return new StringBuilder(original.toString());
                }

                @Override
                public boolean supports(Class<?> type) {
                    return StringBuilder.class.isAssignableFrom(type);
                }
            };
            assertThatNoException().isThrownBy(() ->
                    cloner.registerHandler(StringBuilder.class, handler));
        }
    }

    @Nested
    @DisplayName("sealed类层次测试")
    class SealedHierarchyTests {

        @Test
        @DisplayName("AbstractCloner是sealed类")
        void testIsSealed() {
            boolean sealed = AbstractCloner.class.isSealed();
            assertThat(sealed).isTrue();
        }

        @Test
        @DisplayName("ReflectiveCloner继承AbstractCloner")
        void testReflectiveClonerExtends() {
            boolean assignable = AbstractCloner.class.isAssignableFrom(ReflectiveCloner.class);
            assertThat(assignable).isTrue();
        }

        @Test
        @DisplayName("SerializingCloner继承AbstractCloner")
        void testSerializingClonerExtends() {
            boolean assignable = AbstractCloner.class.isAssignableFrom(SerializingCloner.class);
            assertThat(assignable).isTrue();
        }

        @Test
        @DisplayName("UnsafeCloner继承AbstractCloner")
        void testUnsafeClonerExtends() {
            boolean assignable = AbstractCloner.class.isAssignableFrom(UnsafeCloner.class);
            assertThat(assignable).isTrue();
        }
    }
}
