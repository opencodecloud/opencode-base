package cloud.opencode.base.pool.factory;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultPooledObjectTest Tests
 * DefaultPooledObjectTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("DefaultPooledObject 测试")
class DefaultPooledObjectTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("构造函数初始化所有字段")
        void testConstructor() {
            String obj = "test";
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>(obj);

            assertThat(pooled.getObject()).isEqualTo("test");
            assertThat(pooled.getState()).isEqualTo(PooledObjectState.IDLE);
            assertThat(pooled.getBorrowCount()).isZero();
            assertThat(pooled.getCreateInstant()).isBeforeOrEqualTo(Instant.now());
        }
    }

    @Nested
    @DisplayName("状态管理测试")
    class StateManagementTests {

        @Test
        @DisplayName("compareAndSetState成功转换状态")
        void testCompareAndSetStateSuccess() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");

            boolean result = pooled.compareAndSetState(
                    PooledObjectState.IDLE, PooledObjectState.ALLOCATED);

            assertThat(result).isTrue();
            assertThat(pooled.getState()).isEqualTo(PooledObjectState.ALLOCATED);
        }

        @Test
        @DisplayName("compareAndSetState期望状态不匹配时失败")
        void testCompareAndSetStateFailure() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");

            boolean result = pooled.compareAndSetState(
                    PooledObjectState.ALLOCATED, PooledObjectState.RETURNING);

            assertThat(result).isFalse();
            assertThat(pooled.getState()).isEqualTo(PooledObjectState.IDLE);
        }

        @Test
        @DisplayName("setState直接设置状态")
        void testSetState() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");

            pooled.setState(PooledObjectState.INVALID);

            assertThat(pooled.getState()).isEqualTo(PooledObjectState.INVALID);
        }
    }

    @Nested
    @DisplayName("借用标记测试")
    class BorrowMarkingTests {

        @Test
        @DisplayName("markBorrowed更新借用信息")
        void testMarkBorrowed() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            Instant beforeBorrow = Instant.now();

            pooled.markBorrowed();

            assertThat(pooled.getLastBorrowInstant()).isAfterOrEqualTo(beforeBorrow);
            assertThat(pooled.getLastUseInstant()).isAfterOrEqualTo(beforeBorrow);
            assertThat(pooled.getBorrowCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("多次markBorrowed增加借用次数")
        void testMultipleBorrows() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");

            pooled.markBorrowed();
            pooled.markBorrowed();
            pooled.markBorrowed();

            assertThat(pooled.getBorrowCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("markReturned更新归还信息")
        void testMarkReturned() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.markBorrowed();
            Instant beforeReturn = Instant.now();

            pooled.markReturned();

            assertThat(pooled.getLastReturnInstant()).isAfterOrEqualTo(beforeReturn);
            assertThat(pooled.getLastUseInstant()).isAfterOrEqualTo(beforeReturn);
        }
    }

    @Nested
    @DisplayName("时长计算测试")
    class DurationTests {

        @Test
        @DisplayName("getActiveDuration在ALLOCATED状态下计算活跃时长")
        void testActiveDurationAllocated() throws InterruptedException {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.compareAndSetState(PooledObjectState.IDLE, PooledObjectState.ALLOCATED);
            pooled.markBorrowed();

            Thread.sleep(50);

            Duration duration = pooled.getActiveDuration();
            assertThat(duration.toMillis()).isGreaterThanOrEqualTo(40);
        }

        @Test
        @DisplayName("getActiveDuration在非ALLOCATED状态下返回借用到归还的时长")
        void testActiveDurationNotAllocated() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.markBorrowed();
            pooled.markReturned();

            Duration duration = pooled.getActiveDuration();
            assertThat(duration).isNotNull();
        }

        @Test
        @DisplayName("getIdleDuration在IDLE状态下计算空闲时长")
        void testIdleDurationIdle() throws InterruptedException {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.markReturned();

            Thread.sleep(50);

            Duration duration = pooled.getIdleDuration();
            assertThat(duration.toMillis()).isGreaterThanOrEqualTo(40);
        }

        @Test
        @DisplayName("getIdleDuration在非IDLE状态下返回零")
        void testIdleDurationNotIdle() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.compareAndSetState(PooledObjectState.IDLE, PooledObjectState.ALLOCATED);

            Duration duration = pooled.getIdleDuration();
            assertThat(duration).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("Getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getObject返回包装的对象")
        void testGetObject() {
            String obj = "test-object";
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>(obj);

            assertThat(pooled.getObject()).isSameAs(obj);
        }

        @Test
        @DisplayName("getCreateInstant返回创建时间")
        void testGetCreateInstant() {
            Instant before = Instant.now();
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            Instant after = Instant.now();

            assertThat(pooled.getCreateInstant())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("getLastBorrowInstant返回最后借用时间")
        void testGetLastBorrowInstant() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            Instant before = Instant.now();
            pooled.markBorrowed();

            assertThat(pooled.getLastBorrowInstant()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("getLastReturnInstant返回最后归还时间")
        void testGetLastReturnInstant() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            Instant before = Instant.now();
            pooled.markReturned();

            assertThat(pooled.getLastReturnInstant()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("getLastUseInstant返回最后使用时间")
        void testGetLastUseInstant() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            Instant before = Instant.now();
            pooled.markBorrowed();

            assertThat(pooled.getLastUseInstant()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString返回有意义的字符串表示")
        void testToString() {
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");

            String str = pooled.toString();

            assertThat(str).contains("DefaultPooledObject");
            assertThat(str).contains("test");
            assertThat(str).contains("IDLE");
        }
    }
}
