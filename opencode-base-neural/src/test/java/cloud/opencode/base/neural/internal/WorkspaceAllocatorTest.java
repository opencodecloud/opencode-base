package cloud.opencode.base.neural.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkspaceAllocator — 工作区预分配器测试")
class WorkspaceAllocatorTest {

    private WorkspaceAllocator allocator;

    @BeforeEach
    void setUp() {
        allocator = new WorkspaceAllocator(1024);
    }

    @Nested
    @DisplayName("allocate — 分配区域")
    class AllocateTests {

        @Test
        @DisplayName("连续分配返回递增的偏移量")
        void allocateReturnsIncreasingOffsets() {
            int off1 = allocator.allocate(100);
            int off2 = allocator.allocate(200);
            int off3 = allocator.allocate(50);

            assertThat(off1).isEqualTo(0);
            assertThat(off2).isEqualTo(100);
            assertThat(off3).isEqualTo(300);
        }

        @Test
        @DisplayName("分配大小为 0 返回当前偏移量")
        void allocateZeroSize() {
            int off1 = allocator.allocate(100);
            int off2 = allocator.allocate(0);
            int off3 = allocator.allocate(50);

            assertThat(off1).isEqualTo(0);
            assertThat(off2).isEqualTo(100);
            assertThat(off3).isEqualTo(100);
        }

        @Test
        @DisplayName("超出容量抛出异常")
        void allocateBeyondCapacityThrows() {
            allocator.allocate(1000);
            assertThatThrownBy(() -> allocator.allocate(100))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Workspace overflow");
        }

        @Test
        @DisplayName("负数大小抛出异常")
        void allocateNegativeSizeThrows() {
            assertThatThrownBy(() -> allocator.allocate(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("精确分配满容量成功")
        void allocateExactCapacity() {
            int off = allocator.allocate(1024);
            assertThat(off).isEqualTo(0);
            assertThat(allocator.used()).isEqualTo(1024);
        }
    }

    @Nested
    @DisplayName("reset — 重置偏移量")
    class ResetTests {

        @Test
        @DisplayName("重置后偏移量归零")
        void resetBringsOffsetBackToZero() {
            allocator.allocate(500);
            assertThat(allocator.used()).isEqualTo(500);

            allocator.reset();
            assertThat(allocator.used()).isEqualTo(0);
        }

        @Test
        @DisplayName("重置后可重新分配")
        void allocateAfterReset() {
            allocator.allocate(1024);
            allocator.reset();

            int off = allocator.allocate(256);
            assertThat(off).isEqualTo(0);
            assertThat(allocator.used()).isEqualTo(256);
        }
    }

    @Nested
    @DisplayName("data — 底层数组")
    class DataTests {

        @Test
        @DisplayName("返回底层数组")
        void dataReturnsBacking() {
            float[] data = allocator.data();
            assertThat(data).hasSize(1024);
        }

        @Test
        @DisplayName("多次调用返回同一数组")
        void dataReturnsSameArray() {
            assertThat(allocator.data()).isSameAs(allocator.data());
        }

        @Test
        @DisplayName("可通过偏移量写入和读取数据")
        void writeAndReadViaOffset() {
            int off = allocator.allocate(3);
            float[] data = allocator.data();
            data[off] = 1.0f;
            data[off + 1] = 2.0f;
            data[off + 2] = 3.0f;

            assertThat(data[off]).isEqualTo(1.0f);
            assertThat(data[off + 1]).isEqualTo(2.0f);
            assertThat(data[off + 2]).isEqualTo(3.0f);
        }
    }

    @Nested
    @DisplayName("capacity 与 used — 容量和使用量")
    class CapacityUsedTests {

        @Test
        @DisplayName("初始状态容量正确且已使用为 0")
        void initialState() {
            assertThat(allocator.capacity()).isEqualTo(1024);
            assertThat(allocator.used()).isEqualTo(0);
        }

        @Test
        @DisplayName("分配后已使用量正确")
        void usedAfterAllocate() {
            allocator.allocate(300);
            allocator.allocate(200);
            assertThat(allocator.used()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("构造函数验证")
    class ConstructorTests {

        @Test
        @DisplayName("负数容量抛出异常")
        void negativeCapacityThrows() {
            assertThatThrownBy(() -> new WorkspaceAllocator(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("容量为 0 创建成功")
        void zeroCapacity() {
            WorkspaceAllocator empty = new WorkspaceAllocator(0);
            assertThat(empty.capacity()).isEqualTo(0);
            assertThat(empty.used()).isEqualTo(0);
        }
    }
}
