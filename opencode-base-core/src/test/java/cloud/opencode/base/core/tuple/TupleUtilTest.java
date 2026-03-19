package cloud.opencode.base.core.tuple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TupleUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("TupleUtil 测试")
class TupleUtilTest {

    @Nested
    @DisplayName("Pair 创建测试")
    class PairCreationTests {

        @Test
        @DisplayName("pair 创建二元组")
        void testPair() {
            Pair<String, Integer> pair = TupleUtil.pair("hello", 42);

            assertThat(pair.left()).isEqualTo("hello");
            assertThat(pair.right()).isEqualTo(42);
        }

        @Test
        @DisplayName("pair 从 Map.Entry 创建")
        void testPairFromEntry() {
            Map.Entry<String, Integer> entry = Map.entry("key", 100);
            Pair<String, Integer> pair = TupleUtil.pair(entry);

            assertThat(pair.left()).isEqualTo("key");
            assertThat(pair.right()).isEqualTo(100);
        }

        @Test
        @DisplayName("emptyPair 创建空二元组")
        void testEmptyPair() {
            Pair<String, Integer> pair = TupleUtil.emptyPair();

            assertThat(pair.left()).isNull();
            assertThat(pair.right()).isNull();
        }

        @Test
        @DisplayName("pair 包含 null 值")
        void testPairWithNull() {
            Pair<String, Integer> pair = TupleUtil.pair(null, null);

            assertThat(pair.left()).isNull();
            assertThat(pair.right()).isNull();
        }
    }

    @Nested
    @DisplayName("Triple 创建测试")
    class TripleCreationTests {

        @Test
        @DisplayName("triple 创建三元组")
        void testTriple() {
            Triple<String, Integer, Boolean> triple = TupleUtil.triple("hello", 42, true);

            assertThat(triple.first()).isEqualTo("hello");
            assertThat(triple.second()).isEqualTo(42);
            assertThat(triple.third()).isTrue();
        }

        @Test
        @DisplayName("emptyTriple 创建空三元组")
        void testEmptyTriple() {
            Triple<String, Integer, Boolean> triple = TupleUtil.emptyTriple();

            assertThat(triple.first()).isNull();
            assertThat(triple.second()).isNull();
            assertThat(triple.third()).isNull();
        }

        @Test
        @DisplayName("triple 包含 null 值")
        void testTripleWithNull() {
            Triple<String, Integer, Boolean> triple = TupleUtil.triple(null, null, null);

            assertThat(triple.first()).isNull();
            assertThat(triple.second()).isNull();
            assertThat(triple.third()).isNull();
        }
    }

    @Nested
    @DisplayName("Quadruple 创建测试")
    class QuadrupleCreationTests {

        @Test
        @DisplayName("quadruple 创建四元组")
        void testQuadruple() {
            Quadruple<String, Integer, Boolean, Double> quad = TupleUtil.quadruple("hello", 42, true, 3.14);

            assertThat(quad.first()).isEqualTo("hello");
            assertThat(quad.second()).isEqualTo(42);
            assertThat(quad.third()).isTrue();
            assertThat(quad.fourth()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("emptyQuadruple 创建空四元组")
        void testEmptyQuadruple() {
            Quadruple<String, Integer, Boolean, Double> quad = TupleUtil.emptyQuadruple();

            assertThat(quad.first()).isNull();
            assertThat(quad.second()).isNull();
            assertThat(quad.third()).isNull();
            assertThat(quad.fourth()).isNull();
        }

        @Test
        @DisplayName("quadruple 包含 null 值")
        void testQuadrupleWithNull() {
            Quadruple<String, Integer, Boolean, Double> quad = TupleUtil.quadruple(null, null, null, null);

            assertThat(quad.first()).isNull();
            assertThat(quad.second()).isNull();
            assertThat(quad.third()).isNull();
            assertThat(quad.fourth()).isNull();
        }
    }

    @Nested
    @DisplayName("返回类型测试")
    class ReturnTypeTests {

        @Test
        @DisplayName("pair 返回 Pair 类型")
        void testPairReturnType() {
            var pair = TupleUtil.pair("a", 1);
            assertThat(pair).isInstanceOf(Pair.class);
        }

        @Test
        @DisplayName("triple 返回 Triple 类型")
        void testTripleReturnType() {
            var triple = TupleUtil.triple("a", 1, true);
            assertThat(triple).isInstanceOf(Triple.class);
        }

        @Test
        @DisplayName("quadruple 返回 Quadruple 类型")
        void testQuadrupleReturnType() {
            var quad = TupleUtil.quadruple("a", 1, true, 3.14);
            assertThat(quad).isInstanceOf(Quadruple.class);
        }
    }
}
