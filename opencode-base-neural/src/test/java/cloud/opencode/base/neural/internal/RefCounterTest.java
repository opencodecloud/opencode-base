package cloud.opencode.base.neural.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RefCounter — 引用计数器测试")
class RefCounterTest {

    private TensorPool pool;
    private RefCounter counter;

    @BeforeEach
    void setUp() {
        pool = new TensorPool(64);
        counter = new RefCounter(8, pool);
    }

    @Nested
    @DisplayName("init — 初始化引用计数")
    class InitTests {

        @Test
        @DisplayName("设置引用计数")
        void initSetsCount() {
            counter.init(0, 3);
            assertThat(counter.get(0)).isEqualTo(3);
        }

        @Test
        @DisplayName("设置为 0 允许")
        void initToZero() {
            counter.init(0, 0);
            assertThat(counter.get(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("负数引用计数抛出异常")
        void initNegativeThrows() {
            assertThatThrownBy(() -> counter.init(0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("越界索引抛出异常")
        void initOutOfBoundsThrows() {
            assertThatThrownBy(() -> counter.init(8, 1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> counter.init(-1, 1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("decrement — 递减引用计数")
    class DecrementTests {

        @Test
        @DisplayName("递减减少计数")
        void decrementReducesCount() {
            counter.init(0, 3);
            counter.decrement(0, new float[10]);
            assertThat(counter.get(0)).isEqualTo(2);
        }

        @Test
        @DisplayName("递减到 0 时数据释放到池中")
        void decrementToZeroReleasesToPool() {
            counter.init(0, 1);
            float[] data = new float[16];
            assertThat(pool.pooledCount()).isEqualTo(0);

            counter.decrement(0, data);

            assertThat(counter.get(0)).isEqualTo(0);
            assertThat(pool.pooledCount()).isEqualTo(1);

            // Verify it's the same array
            float[] reused = pool.acquire(16);
            assertThat(reused).isSameAs(data);
        }

        @Test
        @DisplayName("递减到 0 但 data 为 null 时不释放")
        void decrementToZeroWithNullDataDoesNotRelease() {
            counter.init(0, 1);
            counter.decrement(0, null);
            assertThat(counter.get(0)).isEqualTo(0);
            assertThat(pool.pooledCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("多次递减直到 0")
        void multipleDecrements() {
            counter.init(2, 3);
            float[] data = new float[8];

            counter.decrement(2, data);
            assertThat(counter.get(2)).isEqualTo(2);

            counter.decrement(2, data);
            assertThat(counter.get(2)).isEqualTo(1);

            counter.decrement(2, data);
            assertThat(counter.get(2)).isEqualTo(0);
            // Data released on last decrement
            assertThat(pool.pooledCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("已经为 0 时再递减抛出异常")
        void decrementBelowZeroThrows() {
            counter.init(0, 0);
            assertThatThrownBy(() -> counter.decrement(0, new float[4]))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already zero");
        }

        @Test
        @DisplayName("越界索引抛出异常")
        void decrementOutOfBoundsThrows() {
            assertThatThrownBy(() -> counter.decrement(8, new float[4]))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("get — 获取引用计数")
    class GetTests {

        @Test
        @DisplayName("初始值为 0")
        void defaultCountIsZero() {
            assertThat(counter.get(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("越界索引抛出异常")
        void getOutOfBoundsThrows() {
            assertThatThrownBy(() -> counter.get(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> counter.get(8))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("构造函数验证")
    class ConstructorTests {

        @Test
        @DisplayName("负数节点数抛出异常")
        void negativeNodeCountThrows() {
            assertThatThrownBy(() -> new RefCounter(-1, pool))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 池抛出异常")
        void nullPoolThrows() {
            assertThatThrownBy(() -> new RefCounter(4, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("节点数为 0 创建成功")
        void zeroNodeCount() {
            RefCounter empty = new RefCounter(0, pool);
            assertThatThrownBy(() -> empty.get(0))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }
}
