package cloud.opencode.base.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Position 枚举测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("Position 枚举测试")
class PositionTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含所有9个位置值")
        void testAllValues() {
            assertThat(Position.values()).hasSize(9);
        }

        @Test
        @DisplayName("包含正确的值")
        void testContainsCorrectValues() {
            assertThat(Position.values()).containsExactly(
                Position.TOP_LEFT,
                Position.TOP_CENTER,
                Position.TOP_RIGHT,
                Position.CENTER_LEFT,
                Position.CENTER,
                Position.CENTER_RIGHT,
                Position.BOTTOM_LEFT,
                Position.BOTTOM_CENTER,
                Position.BOTTOM_RIGHT
            );
        }

        @Test
        @DisplayName("valueOf方法正常工作")
        void testValueOf() {
            assertThat(Position.valueOf("TOP_LEFT")).isEqualTo(Position.TOP_LEFT);
            assertThat(Position.valueOf("CENTER")).isEqualTo(Position.CENTER);
            assertThat(Position.valueOf("BOTTOM_RIGHT")).isEqualTo(Position.BOTTOM_RIGHT);
        }
    }

    @Nested
    @DisplayName("isLeft方法测试")
    class IsLeftTests {

        @Test
        @DisplayName("TOP_LEFT返回true")
        void testTopLeft() {
            assertThat(Position.TOP_LEFT.isLeft()).isTrue();
        }

        @Test
        @DisplayName("CENTER_LEFT返回true")
        void testCenterLeft() {
            assertThat(Position.CENTER_LEFT.isLeft()).isTrue();
        }

        @Test
        @DisplayName("BOTTOM_LEFT返回true")
        void testBottomLeft() {
            assertThat(Position.BOTTOM_LEFT.isLeft()).isTrue();
        }

        @Test
        @DisplayName("非左侧位置返回false")
        void testNonLeftPositions() {
            assertThat(Position.TOP_CENTER.isLeft()).isFalse();
            assertThat(Position.TOP_RIGHT.isLeft()).isFalse();
            assertThat(Position.CENTER.isLeft()).isFalse();
            assertThat(Position.CENTER_RIGHT.isLeft()).isFalse();
            assertThat(Position.BOTTOM_CENTER.isLeft()).isFalse();
            assertThat(Position.BOTTOM_RIGHT.isLeft()).isFalse();
        }
    }

    @Nested
    @DisplayName("isRight方法测试")
    class IsRightTests {

        @Test
        @DisplayName("TOP_RIGHT返回true")
        void testTopRight() {
            assertThat(Position.TOP_RIGHT.isRight()).isTrue();
        }

        @Test
        @DisplayName("CENTER_RIGHT返回true")
        void testCenterRight() {
            assertThat(Position.CENTER_RIGHT.isRight()).isTrue();
        }

        @Test
        @DisplayName("BOTTOM_RIGHT返回true")
        void testBottomRight() {
            assertThat(Position.BOTTOM_RIGHT.isRight()).isTrue();
        }

        @Test
        @DisplayName("非右侧位置返回false")
        void testNonRightPositions() {
            assertThat(Position.TOP_LEFT.isRight()).isFalse();
            assertThat(Position.TOP_CENTER.isRight()).isFalse();
            assertThat(Position.CENTER_LEFT.isRight()).isFalse();
            assertThat(Position.CENTER.isRight()).isFalse();
            assertThat(Position.BOTTOM_LEFT.isRight()).isFalse();
            assertThat(Position.BOTTOM_CENTER.isRight()).isFalse();
        }
    }

    @Nested
    @DisplayName("isTop方法测试")
    class IsTopTests {

        @Test
        @DisplayName("TOP_LEFT返回true")
        void testTopLeft() {
            assertThat(Position.TOP_LEFT.isTop()).isTrue();
        }

        @Test
        @DisplayName("TOP_CENTER返回true")
        void testTopCenter() {
            assertThat(Position.TOP_CENTER.isTop()).isTrue();
        }

        @Test
        @DisplayName("TOP_RIGHT返回true")
        void testTopRight() {
            assertThat(Position.TOP_RIGHT.isTop()).isTrue();
        }

        @Test
        @DisplayName("非顶部位置返回false")
        void testNonTopPositions() {
            assertThat(Position.CENTER_LEFT.isTop()).isFalse();
            assertThat(Position.CENTER.isTop()).isFalse();
            assertThat(Position.CENTER_RIGHT.isTop()).isFalse();
            assertThat(Position.BOTTOM_LEFT.isTop()).isFalse();
            assertThat(Position.BOTTOM_CENTER.isTop()).isFalse();
            assertThat(Position.BOTTOM_RIGHT.isTop()).isFalse();
        }
    }

    @Nested
    @DisplayName("isBottom方法测试")
    class IsBottomTests {

        @Test
        @DisplayName("BOTTOM_LEFT返回true")
        void testBottomLeft() {
            assertThat(Position.BOTTOM_LEFT.isBottom()).isTrue();
        }

        @Test
        @DisplayName("BOTTOM_CENTER返回true")
        void testBottomCenter() {
            assertThat(Position.BOTTOM_CENTER.isBottom()).isTrue();
        }

        @Test
        @DisplayName("BOTTOM_RIGHT返回true")
        void testBottomRight() {
            assertThat(Position.BOTTOM_RIGHT.isBottom()).isTrue();
        }

        @Test
        @DisplayName("非底部位置返回false")
        void testNonBottomPositions() {
            assertThat(Position.TOP_LEFT.isBottom()).isFalse();
            assertThat(Position.TOP_CENTER.isBottom()).isFalse();
            assertThat(Position.TOP_RIGHT.isBottom()).isFalse();
            assertThat(Position.CENTER_LEFT.isBottom()).isFalse();
            assertThat(Position.CENTER.isBottom()).isFalse();
            assertThat(Position.CENTER_RIGHT.isBottom()).isFalse();
        }
    }

    @Nested
    @DisplayName("isCenterHorizontal方法测试")
    class IsCenterHorizontalTests {

        @Test
        @DisplayName("TOP_CENTER返回true")
        void testTopCenter() {
            assertThat(Position.TOP_CENTER.isCenterHorizontal()).isTrue();
        }

        @Test
        @DisplayName("CENTER返回true")
        void testCenter() {
            assertThat(Position.CENTER.isCenterHorizontal()).isTrue();
        }

        @Test
        @DisplayName("BOTTOM_CENTER返回true")
        void testBottomCenter() {
            assertThat(Position.BOTTOM_CENTER.isCenterHorizontal()).isTrue();
        }

        @Test
        @DisplayName("非水平居中位置返回false")
        void testNonCenterHorizontalPositions() {
            assertThat(Position.TOP_LEFT.isCenterHorizontal()).isFalse();
            assertThat(Position.TOP_RIGHT.isCenterHorizontal()).isFalse();
            assertThat(Position.CENTER_LEFT.isCenterHorizontal()).isFalse();
            assertThat(Position.CENTER_RIGHT.isCenterHorizontal()).isFalse();
            assertThat(Position.BOTTOM_LEFT.isCenterHorizontal()).isFalse();
            assertThat(Position.BOTTOM_RIGHT.isCenterHorizontal()).isFalse();
        }
    }

    @Nested
    @DisplayName("isCenterVertical方法测试")
    class IsCenterVerticalTests {

        @Test
        @DisplayName("CENTER_LEFT返回true")
        void testCenterLeft() {
            assertThat(Position.CENTER_LEFT.isCenterVertical()).isTrue();
        }

        @Test
        @DisplayName("CENTER返回true")
        void testCenter() {
            assertThat(Position.CENTER.isCenterVertical()).isTrue();
        }

        @Test
        @DisplayName("CENTER_RIGHT返回true")
        void testCenterRight() {
            assertThat(Position.CENTER_RIGHT.isCenterVertical()).isTrue();
        }

        @Test
        @DisplayName("非垂直居中位置返回false")
        void testNonCenterVerticalPositions() {
            assertThat(Position.TOP_LEFT.isCenterVertical()).isFalse();
            assertThat(Position.TOP_CENTER.isCenterVertical()).isFalse();
            assertThat(Position.TOP_RIGHT.isCenterVertical()).isFalse();
            assertThat(Position.BOTTOM_LEFT.isCenterVertical()).isFalse();
            assertThat(Position.BOTTOM_CENTER.isCenterVertical()).isFalse();
            assertThat(Position.BOTTOM_RIGHT.isCenterVertical()).isFalse();
        }
    }

    @Nested
    @DisplayName("CENTER位置特殊测试")
    class CenterPositionTests {

        @Test
        @DisplayName("CENTER同时是水平和垂直居中")
        void testCenterIsBothCentered() {
            assertThat(Position.CENTER.isCenterHorizontal()).isTrue();
            assertThat(Position.CENTER.isCenterVertical()).isTrue();
        }

        @Test
        @DisplayName("CENTER不是左右上下")
        void testCenterIsNotEdge() {
            assertThat(Position.CENTER.isLeft()).isFalse();
            assertThat(Position.CENTER.isRight()).isFalse();
            assertThat(Position.CENTER.isTop()).isFalse();
            assertThat(Position.CENTER.isBottom()).isFalse();
        }
    }
}
