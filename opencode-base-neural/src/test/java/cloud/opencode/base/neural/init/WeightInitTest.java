package cloud.opencode.base.neural.init;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link WeightInit}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("WeightInit — 权重初始化工具")
class WeightInitTest {

    private static final float EPSILON = 1e-5f;
    private static final long SEED = 42L;

    @Nested
    @DisplayName("Xavier/Glorot 初始化")
    class XavierTest {

        @Test
        @DisplayName("xavierUniform 生成正确范围的值")
        void xavierUniformRange() {
            Shape shape = Shape.of(256, 128);
            Tensor t = WeightInit.xavierUniform(shape, new Random(SEED));
            float limit = (float) Math.sqrt(6.0 / (128 + 256));
            float[] data = t.toFloatArray();
            assertThat(data).hasSize(256 * 128);
            for (float v : data) {
                assertThat(v).isBetween(-limit, limit);
            }
        }

        @Test
        @DisplayName("xavierNormal 均值接近0")
        void xavierNormalMeanNearZero() {
            Shape shape = Shape.of(512, 256);
            Tensor t = WeightInit.xavierNormal(shape, new Random(SEED));
            float[] data = t.toFloatArray();
            double mean = 0;
            for (float v : data) mean += v;
            mean /= data.length;
            assertThat((float) mean).isCloseTo(0.0f, within(0.01f));
        }

        @Test
        @DisplayName("xavierUniform 1D shape: fanIn = fanOut = dim(0)")
        void xavierUniform1D() {
            Shape shape = Shape.of(100);
            Tensor t = WeightInit.xavierUniform(shape, new Random(SEED));
            float limit = (float) Math.sqrt(6.0 / (100 + 100));
            for (float v : t.toFloatArray()) {
                assertThat(v).isBetween(-limit, limit);
            }
        }
    }

    @Nested
    @DisplayName("He/Kaiming 初始化")
    class HeTest {

        @Test
        @DisplayName("heUniform 生成正确范围的值")
        void heUniformRange() {
            Shape shape = Shape.of(64, 32);
            Tensor t = WeightInit.heUniform(shape, new Random(SEED));
            float limit = (float) Math.sqrt(6.0 / 32);
            for (float v : t.toFloatArray()) {
                assertThat(v).isBetween(-limit, limit);
            }
        }

        @Test
        @DisplayName("heNormal 标准差接近理论值")
        void heNormalStd() {
            Shape shape = Shape.of(1000, 500);
            Tensor t = WeightInit.heNormal(shape, new Random(SEED));
            float[] data = t.toFloatArray();
            double mean = 0;
            for (float v : data) mean += v;
            mean /= data.length;
            double variance = 0;
            for (float v : data) variance += (v - mean) * (v - mean);
            variance /= data.length;
            float expectedStd = (float) Math.sqrt(2.0 / 500);
            assertThat((float) Math.sqrt(variance)).isCloseTo(expectedStd, within(0.01f));
        }

        @Test
        @DisplayName("heUniform 4D conv shape 正确计算 receptiveFieldSize")
        void heUniform4DConv() {
            // Shape [outChannels=16, inChannels=3, kernelH=5, kernelW=5]
            Shape shape = Shape.of(16, 3, 5, 5);
            // fanIn = 3 * 5 * 5 = 75
            Tensor t = WeightInit.heUniform(shape, new Random(SEED));
            float limit = (float) Math.sqrt(6.0 / 75);
            for (float v : t.toFloatArray()) {
                assertThat(v).isBetween(-limit, limit);
            }
        }
    }

    @Nested
    @DisplayName("LeCun 初始化")
    class LeCunTest {

        @Test
        @DisplayName("lecunUniform 生成正确范围的值")
        void lecunUniformRange() {
            Shape shape = Shape.of(64, 32);
            Tensor t = WeightInit.lecunUniform(shape, new Random(SEED));
            float limit = (float) Math.sqrt(3.0 / 32);
            for (float v : t.toFloatArray()) {
                assertThat(v).isBetween(-limit, limit);
            }
        }

        @Test
        @DisplayName("lecunNormal 均值接近0")
        void lecunNormalMeanNearZero() {
            Shape shape = Shape.of(500, 200);
            Tensor t = WeightInit.lecunNormal(shape, new Random(SEED));
            float[] data = t.toFloatArray();
            double mean = 0;
            for (float v : data) mean += v;
            mean /= data.length;
            assertThat((float) mean).isCloseTo(0.0f, within(0.01f));
        }
    }

    @Nested
    @DisplayName("基础初始化")
    class BasicTest {

        @Test
        @DisplayName("uniform 生成指定范围内的值")
        void uniformRange() {
            Tensor t = WeightInit.uniform(Shape.of(100), -2.0f, 3.0f, new Random(SEED));
            for (float v : t.toFloatArray()) {
                assertThat(v).isBetween(-2.0f, 3.0f);
            }
        }

        @Test
        @DisplayName("uniform low >= high 抛异常")
        void uniformInvalidRange() {
            assertThatThrownBy(() -> WeightInit.uniform(Shape.of(10), 5.0f, 3.0f, new Random()))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("low");
        }

        @Test
        @DisplayName("normal 生成正态分布")
        void normalDistribution() {
            Tensor t = WeightInit.normal(Shape.of(10000), 5.0f, 2.0f, new Random(SEED));
            float[] data = t.toFloatArray();
            double mean = 0;
            for (float v : data) mean += v;
            mean /= data.length;
            assertThat((float) mean).isCloseTo(5.0f, within(0.1f));
        }

        @Test
        @DisplayName("normal std <= 0 抛异常")
        void normalInvalidStd() {
            assertThatThrownBy(() -> WeightInit.normal(Shape.of(10), 0, -1.0f, new Random()))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("std");
        }

        @Test
        @DisplayName("zeros 创建全零张量")
        void zerosAllZero() {
            Tensor t = WeightInit.zeros(Shape.of(3, 4));
            for (float v : t.toFloatArray()) {
                assertThat(v).isEqualTo(0.0f);
            }
        }

        @Test
        @DisplayName("ones 创建全一张量")
        void onesAllOne() {
            Tensor t = WeightInit.ones(Shape.of(2, 5));
            for (float v : t.toFloatArray()) {
                assertThat(v).isEqualTo(1.0f);
            }
        }

        @Test
        @DisplayName("constant 创建指定常量张量")
        void constantFill() {
            Tensor t = WeightInit.constant(Shape.of(3, 3), 3.14f);
            for (float v : t.toFloatArray()) {
                assertThat(v).isCloseTo(3.14f, within(EPSILON));
            }
        }
    }

    @Nested
    @DisplayName("Fan 计算")
    class FanTest {

        @Test
        @DisplayName("2D shape: fanIn=dim(1), fanOut=dim(0)")
        void fan2D() {
            int[] fan = WeightInit.computeFan(Shape.of(64, 32));
            assertThat(fan[0]).isEqualTo(32);  // fanIn
            assertThat(fan[1]).isEqualTo(64);  // fanOut
        }

        @Test
        @DisplayName("4D conv shape: fanIn=dim(1)*kH*kW")
        void fan4D() {
            int[] fan = WeightInit.computeFan(Shape.of(16, 3, 5, 5));
            assertThat(fan[0]).isEqualTo(3 * 25);  // fanIn = 3 * 5 * 5
            assertThat(fan[1]).isEqualTo(16 * 25);  // fanOut = 16 * 5 * 5
        }

        @Test
        @DisplayName("1D shape: fanIn = fanOut = dim(0)")
        void fan1D() {
            int[] fan = WeightInit.computeFan(Shape.of(128));
            assertThat(fan[0]).isEqualTo(128);
            assertThat(fan[1]).isEqualTo(128);
        }
    }

    @Nested
    @DisplayName("输入验证")
    class ValidationTest {

        @Test
        @DisplayName("null shape 抛异常")
        void nullShape() {
            assertThatThrownBy(() -> WeightInit.xavierUniform(null, new Random()))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("Shape");
        }

        @Test
        @DisplayName("null rng 抛异常")
        void nullRng() {
            assertThatThrownBy(() -> WeightInit.heNormal(Shape.of(10), null))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("Random");
        }

        @Test
        @DisplayName("zeros null shape 抛异常")
        void zerosNullShape() {
            assertThatThrownBy(() -> WeightInit.zeros(null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("ones null shape 抛异常")
        void onesNullShape() {
            assertThatThrownBy(() -> WeightInit.ones(null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("constant null shape 抛异常")
        void constantNullShape() {
            assertThatThrownBy(() -> WeightInit.constant(null, 1.0f))
                    .isInstanceOf(NeuralException.class);
        }
    }

    @Nested
    @DisplayName("确定性与可重复性")
    class ReproducibilityTest {

        @Test
        @DisplayName("相同种子生成相同结果")
        void sameSeedSameResult() {
            Shape shape = Shape.of(10, 20);
            Tensor t1 = WeightInit.xavierUniform(shape, new Random(123));
            Tensor t2 = WeightInit.xavierUniform(shape, new Random(123));
            assertThat(t1.toFloatArray()).containsExactly(t2.toFloatArray());
        }

        @Test
        @DisplayName("不同种子生成不同结果")
        void differentSeedDifferentResult() {
            Shape shape = Shape.of(10, 20);
            Tensor t1 = WeightInit.heNormal(shape, new Random(1));
            Tensor t2 = WeightInit.heNormal(shape, new Random(2));
            assertThat(t1.toFloatArray()).isNotEqualTo(t2.toFloatArray());
        }
    }

    @Nested
    @DisplayName("单元素张量")
    class SingleElementTest {

        @Test
        @DisplayName("1x1 shape 正常工作")
        void singleElement() {
            Shape shape = Shape.of(1, 1);
            Tensor t = WeightInit.xavierUniform(shape, new Random(SEED));
            assertThat(t.toFloatArray()).hasSize(1);
        }

        @Test
        @DisplayName("constant 单元素")
        void constantSingleElement() {
            Tensor t = WeightInit.constant(Shape.of(1), 42.0f);
            assertThat(t.toFloatArray()).containsExactly(42.0f);
        }
    }
}
