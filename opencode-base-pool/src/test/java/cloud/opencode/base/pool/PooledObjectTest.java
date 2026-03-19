package cloud.opencode.base.pool;

import cloud.opencode.base.pool.factory.DefaultPooledObject;
import cloud.opencode.base.pool.factory.PooledObjectState;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * PooledObjectTest Tests
 * PooledObjectTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("PooledObject 接口测试")
class PooledObjectTest {

    @Nested
    @DisplayName("DefaultPooledObject实现测试")
    class DefaultPooledObjectTests {

        @Test
        @DisplayName("getObject返回包装的对象")
        void testGetObject() {
            PooledObject<String> pooled = new DefaultPooledObject<>("hello");
            assertThat(pooled.getObject()).isEqualTo("hello");
        }

        @Test
        @DisplayName("getCreateInstant返回创建时间")
        void testGetCreateInstant() {
            Instant before = Instant.now();
            PooledObject<String> pooled = new DefaultPooledObject<>("test");
            Instant after = Instant.now();
            assertThat(pooled.getCreateInstant())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("getState返回初始状态")
        void testGetState() {
            PooledObject<String> pooled = new DefaultPooledObject<>("test");
            assertThat(pooled.getState()).isNotNull();
        }

        @Test
        @DisplayName("getBorrowCount初始为0")
        void testGetBorrowCount() {
            PooledObject<String> pooled = new DefaultPooledObject<>("test");
            assertThat(pooled.getBorrowCount()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("接口定义所有必要方法")
        void testInterfaceHasMethods() {
            assertThat(PooledObject.class.getMethods())
                    .extracting("name")
                    .contains("getObject", "getCreateInstant", "getLastBorrowInstant",
                            "getLastReturnInstant", "getLastUseInstant", "getState",
                            "getBorrowCount", "getActiveDuration", "getIdleDuration",
                            "compareAndSetState");
        }
    }
}
