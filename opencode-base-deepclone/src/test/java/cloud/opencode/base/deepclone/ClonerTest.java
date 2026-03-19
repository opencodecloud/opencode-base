package cloud.opencode.base.deepclone;

import cloud.opencode.base.deepclone.cloner.ReflectiveCloner;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ClonerTest Tests
 * ClonerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("Cloner 接口测试")
class ClonerTest {

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("clone(T)克隆对象")
        void testClone() {
            Cloner cloner = ReflectiveCloner.create();
            String original = "hello";
            String cloned = cloner.clone(original);
            // String is immutable so it returns same reference
            assertThat(cloned).isEqualTo(original);
        }

        @Test
        @DisplayName("clone(null)返回null")
        void testCloneNull() {
            Cloner cloner = ReflectiveCloner.create();
            Object result = cloner.clone(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("clone(T, CloneContext)使用上下文克隆")
        void testCloneWithContext() {
            Cloner cloner = ReflectiveCloner.create();
            CloneContext context = CloneContext.create(100);
            String original = "test";
            String cloned = cloner.clone(original, context);
            assertThat(cloned).isEqualTo(original);
        }

        @Test
        @DisplayName("getStrategyName返回策略名称")
        void testGetStrategyName() {
            Cloner cloner = ReflectiveCloner.create();
            assertThat(cloner.getStrategyName()).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("supports检查类型支持")
        void testSupports() {
            Cloner cloner = ReflectiveCloner.create();
            assertThat(cloner.supports(String.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("接口方法签名测试")
    class InterfaceSignatureTests {

        @Test
        @DisplayName("Cloner是接口")
        void testIsInterface() {
            assertThat(Cloner.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("接口包含4个方法")
        void testMethodCount() {
            assertThat(Cloner.class.getDeclaredMethods()).hasSize(4);
        }
    }
}
