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
 * ArrayHandler 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("ArrayHandler 测试")
class ArrayHandlerTest {

    private ArrayHandler handler;
    private Cloner cloner;
    private CloneContext context;

    @BeforeEach
    void setUp() {
        handler = new ArrayHandler();
        cloner = OpenClone.getDefaultCloner();
        context = CloneContext.create();
    }

    // Test entity
    public static class TestEntity {
        private String name;
        private int value;

        public TestEntity() {}

        public TestEntity(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    @Nested
    @DisplayName("clone() 测试")
    class CloneTests {

        @Test
        @DisplayName("克隆null返回null")
        void testCloneNull() {
            Object result = handler.clone(null, cloner, context);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("克隆int数组")
        void testCloneIntArray() {
            int[] original = {1, 2, 3, 4, 5};

            int[] cloned = (int[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("克隆long数组")
        void testCloneLongArray() {
            long[] original = {100L, 200L, 300L};

            long[] cloned = (long[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly(100L, 200L, 300L);
        }

        @Test
        @DisplayName("克隆double数组")
        void testCloneDoubleArray() {
            double[] original = {1.1, 2.2, 3.3};

            double[] cloned = (double[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly(1.1, 2.2, 3.3);
        }

        @Test
        @DisplayName("克隆float数组")
        void testCloneFloatArray() {
            float[] original = {1.1f, 2.2f, 3.3f};

            float[] cloned = (float[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly(1.1f, 2.2f, 3.3f);
        }

        @Test
        @DisplayName("克隆short数组")
        void testCloneShortArray() {
            short[] original = {1, 2, 3};

            short[] cloned = (short[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly((short) 1, (short) 2, (short) 3);
        }

        @Test
        @DisplayName("克隆byte数组")
        void testCloneByteArray() {
            byte[] original = {1, 2, 3};

            byte[] cloned = (byte[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly((byte) 1, (byte) 2, (byte) 3);
        }

        @Test
        @DisplayName("克隆char数组")
        void testCloneCharArray() {
            char[] original = {'a', 'b', 'c'};

            char[] cloned = (char[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly('a', 'b', 'c');
        }

        @Test
        @DisplayName("克隆boolean数组")
        void testCloneBooleanArray() {
            boolean[] original = {true, false, true};

            boolean[] cloned = (boolean[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly(true, false, true);
        }

        @Test
        @DisplayName("克隆String数组")
        void testCloneStringArray() {
            String[] original = {"a", "b", "c"};

            String[] cloned = (String[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("克隆对象数组并深度克隆元素")
        void testCloneObjectArray() {
            TestEntity[] original = {
                    new TestEntity("a", 1),
                    new TestEntity("b", 2)
            };

            TestEntity[] cloned = (TestEntity[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).hasSize(2);
            assertThat(cloned[0]).isNotSameAs(original[0]);
            assertThat(cloned[0].getName()).isEqualTo("a");
            assertThat(cloned[1].getName()).isEqualTo("b");
        }

        @Test
        @DisplayName("克隆空数组")
        void testCloneEmptyArray() {
            int[] original = {};

            int[] cloned = (int[]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isEmpty();
        }
    }

    @Nested
    @DisplayName("clonePrimitiveArray() 测试")
    class ClonePrimitiveArrayTests {

        @Test
        @DisplayName("克隆基本类型数组带参数")
        void testClonePrimitiveArrayWithParams() {
            int[] original = {1, 2, 3};

            int[] cloned = (int[]) handler.clonePrimitiveArray(original, int.class, 3);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("克隆基本类型数组便捷方法")
        void testClonePrimitiveArrayConvenience() {
            double[] original = {1.1, 2.2, 3.3};

            double[] cloned = (double[]) handler.clonePrimitiveArray(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly(1.1, 2.2, 3.3);
        }
    }

    @Nested
    @DisplayName("cloneObjectArray() 测试")
    class CloneObjectArrayTests {

        @Test
        @DisplayName("克隆对象数组泛型方法")
        void testCloneObjectArrayGeneric() {
            TestEntity[] original = {
                    new TestEntity("a", 1),
                    new TestEntity("b", 2)
            };

            TestEntity[] cloned = handler.cloneObjectArray(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned[0]).isNotSameAs(original[0]);
            assertThat(cloned[0].getName()).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("supports() 测试")
    class SupportsTests {

        @Test
        @DisplayName("支持数组类型")
        void testSupportsArray() {
            assertThat(handler.supports(int[].class)).isTrue();
            assertThat(handler.supports(String[].class)).isTrue();
            assertThat(handler.supports(TestEntity[].class)).isTrue();
        }

        @Test
        @DisplayName("不支持非数组类型")
        void testNotSupportsNonArray() {
            assertThat(handler.supports(String.class)).isFalse();
            assertThat(handler.supports(TestEntity.class)).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testSupportsNull() {
            assertThat(handler.supports(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("priority() 测试")
    class PriorityTests {

        @Test
        @DisplayName("优先级为10")
        void testPriority() {
            assertThat(handler.priority()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("多维数组测试")
    class MultiDimensionalArrayTests {

        @Test
        @DisplayName("克隆二维数组")
        void testClone2DArray() {
            int[][] original = {{1, 2}, {3, 4}};

            int[][] cloned = (int[][]) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned[0]).isNotSameAs(original[0]);
            assertThat(cloned[0]).containsExactly(1, 2);
            assertThat(cloned[1]).containsExactly(3, 4);
        }
    }
}
