package cloud.opencode.base.neural.benchmark;

import cloud.opencode.base.neural.internal.Blas;
import cloud.opencode.base.neural.loss.CrossEntropyLoss;
import cloud.opencode.base.neural.loss.LossFunction;
import cloud.opencode.base.neural.loss.MseLoss;
import cloud.opencode.base.neural.norm.ZScoreNormalizer;
import cloud.opencode.base.neural.op.BatchNormOp;
import cloud.opencode.base.neural.op.Conv2DOp;
import cloud.opencode.base.neural.op.DepthwiseConv2DOp;
import cloud.opencode.base.neural.op.GlobalAvgPoolOp;
import cloud.opencode.base.neural.op.LinearOp;
import cloud.opencode.base.neural.op.MaxPool2DOp;
import cloud.opencode.base.neural.op.Op;
import cloud.opencode.base.neural.op.OpAttribute;
import cloud.opencode.base.neural.op.ReluOp;
import cloud.opencode.base.neural.op.SoftmaxOp;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import cloud.opencode.base.neural.tensor.TensorMath;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Neural Module Performance Benchmark
 * 神经网络模块性能基准测试
 *
 * <p>Measures throughput and latency of core tensor operations, BLAS GEMM,
 * operator forward passes, loss functions, and normalization.</p>
 * <p>测量核心张量运算、BLAS GEMM、算子前向传播、损失函数和归一化的吞吐量与延迟。</p>
 *
 * <p>Usage: Run as a JUnit test. Each benchmark reports ops/s and ns/op.</p>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.3
 */
class NeuralBenchmark {

    // ==================== Configuration | 配置 ====================

    private static final int WARMUP = 50;
    private static final int ITERATIONS = 200;
    private static final Random RNG = new Random(42);

    // ==================== Helpers | 辅助方法 ====================

    private static float[] randomFloats(int size) {
        float[] data = new float[size];
        for (int i = 0; i < size; i++) {
            data[i] = RNG.nextFloat() * 2 - 1;
        }
        return data;
    }

    private static Tensor randomTensor(int... dims) {
        Shape shape = Shape.of(dims);
        return Tensor.fromFloat(randomFloats(shape.size()), shape);
    }

    private static long bench(Runnable task, String name) {
        // Warmup
        for (int i = 0; i < WARMUP; i++) {
            task.run();
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            task.run();
        }
        long elapsed = System.nanoTime() - start;
        long nsPerOp = elapsed / ITERATIONS;
        double opsPerMs = ITERATIONS * 1_000_000.0 / elapsed;

        System.out.printf("  %-40s %,10d ns/op   %,.1f ops/ms%n", name, nsPerOp, opsPerMs);
        return nsPerOp;
    }

    // ==================== Benchmark Entry Point | 基准测试入口 ====================

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Neural Performance Benchmark — 神经网络性能基准")
    void runAllBenchmarks() {
        System.out.println("=== Neural Module Benchmark ===");
        System.out.println("Warmup: " + WARMUP + ", Iterations: " + ITERATIONS);
        System.out.println();

        benchTensorOps();
        benchBlas();
        benchActivations();
        benchConvOps();
        benchPoolOps();
        benchLinearBatchNorm();
        benchLossFunctions();
        benchNormalization();

        System.out.println("\n=== Benchmark Complete ===");
    }

    // ==================== Tensor Core Operations | 张量核心运算 ====================

    private void benchTensorOps() {
        System.out.println("[Tensor Core Ops]");

        Tensor a = randomTensor(256, 256);
        Tensor b = randomTensor(256, 256);

        bench(() -> { var r = a.add(b); }, "add [256x256] contiguous");
        bench(() -> { var r = a.mul(b); }, "mul [256x256] contiguous");
        bench(() -> { var r = a.sum(); }, "sum [256x256]");
        bench(() -> { var r = a.mean(); }, "mean [256x256]");
        bench(() -> { var r = a.argmax(1); }, "argmax axis=1 [256x256]");

        // Matmul
        Tensor m1 = randomTensor(128, 256);
        Tensor m2 = randomTensor(256, 128);
        bench(() -> { var r = m1.matmul(m2); }, "matmul [128x256] @ [256x128]");

        // Non-contiguous elementwise (transpose view)
        Tensor t = randomTensor(128, 128).transpose(1, 0);
        Tensor u = randomTensor(128, 128).transpose(1, 0);
        bench(() -> { var r = t.add(u); }, "add [128x128] non-contiguous");

        System.out.println();
    }

    // ==================== BLAS GEMM | BLAS 矩阵乘法 ====================

    private void benchBlas() {
        System.out.println("[BLAS GEMM]");

        // Small matrix
        float[] a64 = randomFloats(64 * 64);
        float[] b64 = randomFloats(64 * 64);
        bench(() -> Blas.matmul(a64, 64, 64, b64, 64, 64), "GEMM 64x64");

        // Medium matrix
        float[] a256 = randomFloats(256 * 256);
        float[] b256 = randomFloats(256 * 256);
        bench(() -> Blas.matmul(a256, 256, 256, b256, 256, 256), "GEMM 256x256");

        // Rectangular
        float[] aR = randomFloats(128 * 512);
        float[] bR = randomFloats(512 * 64);
        bench(() -> Blas.matmul(aR, 128, 512, bR, 512, 64), "GEMM 128x512 @ 512x64");

        // TransB path (used by LinearOp)
        float[] cT = new float[128 * 256];
        float[] wT = randomFloats(256 * 512);
        float[] iT = randomFloats(128 * 512);
        bench(() -> Blas.gemm(1.0f, iT, 128, 512, false, wT, 256, 512, true, 0.0f, cT),
                "GEMM 128x512 @ 512x256^T (transB)");

        System.out.println();
    }

    // ==================== Activation Functions | 激活函数 ====================

    private void benchActivations() {
        System.out.println("[Activations via TensorMath]");

        Tensor t1k = randomTensor(1000);
        Tensor t100k = randomTensor(100_000);

        bench(() -> TensorMath.relu(t1k), "relu [1K]");
        bench(() -> TensorMath.relu(t100k), "relu [100K]");
        bench(() -> TensorMath.sigmoid(t100k), "sigmoid [100K]");
        bench(() -> TensorMath.gelu(t100k), "gelu [100K]");
        bench(() -> TensorMath.mish(t100k), "mish [100K]");
        bench(() -> TensorMath.softplus(t100k), "softplus [100K]");

        // Softmax Op
        Op softmax = new SoftmaxOp();
        Tensor softIn = randomTensor(32, 1000);
        OpAttribute emptyAttr = OpAttribute.empty();
        bench(() -> softmax.forward(List.of(softIn), emptyAttr), "SoftmaxOp [32x1000]");

        System.out.println();
    }

    // ==================== Convolution Operators | 卷积算子 ====================

    private void benchConvOps() {
        System.out.println("[Convolution Ops]");

        // Conv2D: [1, 3, 32, 32] * [16, 3, 3, 3] → [1, 16, 30, 30]
        Tensor convIn = randomTensor(1, 3, 32, 32);
        Tensor convW = randomTensor(16, 3, 3, 3);
        Tensor convB = randomTensor(16);
        Op conv2d = new Conv2DOp();
        OpAttribute convAttr = OpAttribute.builder().put("stride", 1).put("padding", 0).build();
        bench(() -> conv2d.forward(List.of(convIn, convW, convB), convAttr),
                "Conv2D [1,3,32,32] k=3 → [1,16,30,30]");

        // Conv2D larger: [1, 16, 16, 16] * [32, 16, 3, 3]
        Tensor convIn2 = randomTensor(1, 16, 16, 16);
        Tensor convW2 = randomTensor(32, 16, 3, 3);
        bench(() -> conv2d.forward(List.of(convIn2, convW2), convAttr),
                "Conv2D [1,16,16,16] k=3 → [1,32,14,14]");

        // Depthwise Conv2D: [1, 16, 32, 32] * [16, 1, 3, 3]
        Tensor dwIn = randomTensor(1, 16, 32, 32);
        Tensor dwW = randomTensor(16, 1, 3, 3);
        Op dwConv = new DepthwiseConv2DOp();
        bench(() -> dwConv.forward(List.of(dwIn, dwW), convAttr),
                "DepthwiseConv2D [1,16,32,32] k=3");

        System.out.println();
    }

    // ==================== Pooling Operators | 池化算子 ====================

    private void benchPoolOps() {
        System.out.println("[Pooling Ops]");

        Tensor poolIn = randomTensor(1, 16, 32, 32);
        OpAttribute poolAttr = OpAttribute.builder().put("kernel_size", 2).put("stride", 2).build();

        Op maxPool = new MaxPool2DOp();
        bench(() -> maxPool.forward(List.of(poolIn), poolAttr),
                "MaxPool2D [1,16,32,32] k=2 s=2");

        Op globalAvg = new GlobalAvgPoolOp();
        Tensor gapIn = randomTensor(1, 64, 7, 7);
        bench(() -> globalAvg.forward(List.of(gapIn), OpAttribute.empty()),
                "GlobalAvgPool [1,64,7,7]");

        System.out.println();
    }

    // ==================== Linear + BatchNorm | 全连接 + 批归一化 ====================

    private void benchLinearBatchNorm() {
        System.out.println("[Linear + BatchNorm]");

        // Linear: [32, 512] @ [256, 512]^T + bias
        Tensor linIn = randomTensor(32, 512);
        Tensor linW = randomTensor(256, 512);
        Tensor linB = randomTensor(256);
        Op linear = new LinearOp();
        bench(() -> linear.forward(List.of(linIn, linW, linB), OpAttribute.empty()),
                "Linear [32,512] → [32,256]");

        // BatchNorm: [1, 64, 16, 16]
        Tensor bnIn = randomTensor(1, 64, 16, 16);
        Tensor bnScale = randomTensor(64);
        Tensor bnBias = randomTensor(64);
        Tensor bnMean = randomTensor(64);
        Tensor bnVar = Tensor.fromFloat(randomFloats(64).clone(), Shape.of(64));
        // Ensure variance is positive
        float[] varData = bnVar.toFloatArray();
        for (int i = 0; i < varData.length; i++) varData[i] = Math.abs(varData[i]) + 0.1f;
        bnVar = Tensor.fromFloat(varData, Shape.of(64));

        Op batchNorm = new BatchNormOp();
        Tensor finalBnVar = bnVar;
        bench(() -> batchNorm.forward(List.of(bnIn, bnScale, bnBias, bnMean, finalBnVar), OpAttribute.empty()),
                "BatchNorm [1,64,16,16]");

        System.out.println();
    }

    // ==================== Loss Functions | 损失函数 ====================

    private void benchLossFunctions() {
        System.out.println("[Loss Functions]");

        // MSE Loss: 10K elements
        Tensor pred = randomTensor(10_000);
        Tensor tgt = randomTensor(10_000);
        LossFunction mse = new MseLoss();
        bench(() -> mse.compute(pred, tgt), "MSE Loss [10K]");

        // CrossEntropy Loss: [32, 100]
        Tensor cePred = randomTensor(32, 100);
        // Make predictions look like probabilities
        float[] ceData = cePred.toFloatArray();
        for (int i = 0; i < ceData.length; i++) ceData[i] = Math.abs(ceData[i]) + 0.01f;
        cePred = Tensor.fromFloat(ceData, Shape.of(32, 100));
        Tensor ceTgt = randomTensor(32, 100);
        float[] tgtData = ceTgt.toFloatArray();
        // One-hot targets
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 100; j++) tgtData[i * 100 + j] = 0;
            tgtData[i * 100 + RNG.nextInt(100)] = 1;
        }
        ceTgt = Tensor.fromFloat(tgtData, Shape.of(32, 100));

        LossFunction ce = new CrossEntropyLoss();
        Tensor fPred = cePred, fTgt = ceTgt;
        bench(() -> ce.compute(fPred, fTgt), "CrossEntropy [32x100]");

        System.out.println();
    }

    // ==================== Normalization | 归一化 ====================

    private void benchNormalization() {
        System.out.println("[Normalization]");

        Tensor normData = randomTensor(1000, 64);
        ZScoreNormalizer zNorm = new ZScoreNormalizer();

        bench(() -> {
            ZScoreNormalizer z = new ZScoreNormalizer();
            z.fit(normData);
        }, "ZScore fit [1000x64]");

        zNorm.fit(normData);
        bench(() -> zNorm.normalize(normData), "ZScore normalize [1000x64]");

        System.out.println();
    }
}
