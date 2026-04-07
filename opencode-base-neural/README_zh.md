# opencode-base-neural

**纯 Java 神经网络前向推理引擎 | JDK 25+**

轻量级、零依赖的推理引擎，完全用 Java 运行 CNN、LSTM 和 CTC 模型 — 无 native 库、无 JNI、无平台特定二进制文件。

[![Java](https://img.shields.io/badge/JDK-25%2B-blue)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0)

## 为什么选择 opencode-base-neural？

| 痛点 | 其他框架 | opencode-base-neural |
|------|---------|---------------------|
| Native 依赖地狱 | 200-500MB native 库 (PyTorch/TF) | **零 native。一个 JAR。< 500KB。** |
| 跨平台部署困难 | 每个 OS/架构需要不同二进制 | **任何 JVM，随处运行。** |
| Docker 镜像膨胀 | 200MB → 1GB+ | **零膨胀。** |
| Serverless 冷启动慢 | 加载 native 库需 10-30s | **冷启动 < 2s。** |
| API 过于复杂 | "像写 XML 配置一样" | **3 行代码：加载 → 推理 → 完成。** |

## 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-neural</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 三步推理

```java
import cloud.opencode.base.neural.session.InferenceSession;
import cloud.opencode.base.neural.tensor.Tensor;
import cloud.opencode.base.neural.tensor.Shape;

import java.nio.file.Path;
import java.util.Map;

// 1. 加载模型
try (InferenceSession session = InferenceSession.load(Path.of("model.ocm"))) {

    // 2. 准备输入
    float[] inputData = new float[1 * 3 * 224 * 224]; // 预处理后的图像数据
    Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 3, 224, 224));

    // 3. 执行推理
    Map<String, Tensor> outputs = session.run(Map.of("input", input));
    float[] scores = outputs.get("output").toFloatArray();

    // 获取预测结果
    Tensor argmax = outputs.get("output").argmax(1);
    System.out.println("预测类别: " + (int) argmax.getFloat(0));
}
```

### 图像分类（配合 opencode-base-image 使用）

```java
import cloud.opencode.base.neural.tensor.ImageToTensor;
import cloud.opencode.base.neural.tensor.ImageToTensor.ConvertOptions;
import cloud.opencode.base.neural.session.InferenceSession;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

BufferedImage img = ImageIO.read(new File("cat.jpg"));

// ImageNet 标准化
Tensor input = ImageToTensor.convert(img, ConvertOptions.builder()
    .targetWidth(224)
    .targetHeight(224)
    .mean(new float[]{0.485f, 0.456f, 0.406f})
    .std(new float[]{0.229f, 0.224f, 0.225f})
    .build());

try (InferenceSession session = InferenceSession.load(Path.of("mobilenetv2.ocm"))) {
    Map<String, Tensor> out = session.run(Map.of("input", input));
    // out.get("output") → [1, 1000] 类别概率分布
}
```

## 架构

```
┌─────────────────────────────────────────────┐
│        session/ (用户 API 层)                │  InferenceSession, SessionConfig
├─────────────────────────────────────────────┤
│        model/ (模型与计算图层)                │  OcmLoader, Graph, OcmModel
├─────────────────────────────────────────────┤
│   op/ (30 个算子)  loss/  metric/            │  Conv2D, LSTM, GELU, ...
├─────────────────────────────────────────────┤
│   tensor/  init/  norm/                      │  Tensor, WeightInit, Normalizer
├─────────────────────────────────────────────┤
│        internal/ (内部引擎)                  │  Blas, Im2Col, Activation
└─────────────────────────────────────────────┘
```

## 内置算子（30 个）

| 类别 | 算子 |
|------|------|
| **CNN** | Conv1D, Conv2D, DepthwiseConv2D, MaxPool2D, AvgPool2D, GlobalAvgPool, BatchNorm, Linear |
| **激活函数** | ReLU, Sigmoid, Tanh, Softmax, HardSigmoid, HardSwish, **LeakyReLU**, **ELU**, **SELU**, **GELU**, **Swish**, **Mish**, **Softplus** |
| **序列模型** | LSTM, BiLSTM, CTCDecode（贪心解码）, CTCBeamSearch（束搜索） |
| **结构算子** | Flatten, Reshape, Dropout, Add, Concat |

### 自定义算子注册

```java
import cloud.opencode.base.neural.op.OpRegistry;

// 在启动时注册
OpRegistry.register("MyCustomOp", () -> (inputs, attrs) -> {
    Tensor x = inputs.get(0);
    float[] data = x.toFloatArray();
    // 自定义计算逻辑
    return List.of(Tensor.fromFloat(data, x.shape()));
});
```

## 张量 API

```java
import cloud.opencode.base.neural.tensor.*;

// 创建
Tensor a = Tensor.zeros(Shape.of(2, 3));
Tensor b = Tensor.ones(Shape.of(2, 3));
Tensor c = Tensor.fromFloat(new float[]{1,2,3,4,5,6}, Shape.of(2, 3));

// 逐元素运算
Tensor sum = a.add(b);          // 加法
Tensor product = a.mul(b);      // 乘法

// 形状变换（零拷贝视图）
Tensor flat = c.flatten();           // [6]
Tensor reshaped = c.reshape(3, 2);   // [3, 2]
Tensor transposed = c.transpose(1, 0); // [3, 2]

// 规约运算
Tensor rowSum = c.sum(1);            // 按行求和 [2]
Tensor colMean = c.mean(0);          // 按列求均值 [3]
Tensor maxIdx = c.argmax(1);         // 按行取最大值索引 [2]

// 矩阵乘法
Tensor d = Tensor.fromFloat(new float[]{1,2,3,4,5,6}, Shape.of(3, 2));
Tensor result = c.matmul(d);         // [2, 2]

// 数学运算
Tensor exp = TensorMath.exp(c);
Tensor relu = TensorMath.relu(c);    // max(0, x)
Tensor clamped = TensorMath.clamp(c, 0f, 5f);

// V1.0.3 新增：扩展激活函数
Tensor gelu = TensorMath.gelu(c);
Tensor swish = TensorMath.swish(c);
Tensor mish = TensorMath.mish(c);

// 工厂方法
Tensor eye = TensorFactory.eye(3);       // 3x3 单位矩阵
Tensor range = TensorFactory.arange(10); // [0,1,...,9]
```

## 损失函数（V1.0.3）

```java
import cloud.opencode.base.neural.loss.*;

// 均方误差
LossFunction mse = new MseLoss();
Tensor loss = mse.compute(predicted, target); // 标量张量

// 交叉熵（分类任务）
LossFunction ce  = new CrossEntropyLoss();        // 多分类 [N,C]
LossFunction bce = new BinaryCrossEntropyLoss();  // 二分类 [N]

// Huber 损失（平滑 L1）
LossFunction huber = new HuberLoss(1.0f);  // delta 可配置

// 其他：MaeLoss, CosineSimilarityLoss
```

## 权重初始化（V1.0.3）

```java
import cloud.opencode.base.neural.init.WeightInit;
import java.util.Random;

Random rng = new Random(42);

// Xavier/Glorot 初始化（适合 sigmoid/tanh）
Tensor w1 = WeightInit.xavierUniform(Shape.of(256, 128), rng);
Tensor w2 = WeightInit.xavierNormal(Shape.of(256, 128), rng);

// He/Kaiming 初始化（适合 ReLU 系列）
Tensor w3 = WeightInit.heUniform(Shape.of(256, 128), rng);
Tensor w4 = WeightInit.heNormal(Shape.of(256, 128), rng);

// LeCun 初始化（适合 SELU）
Tensor w5 = WeightInit.lecunNormal(Shape.of(256, 128), rng);

// 基础初始化
Tensor zeros = WeightInit.zeros(Shape.of(128));
Tensor ones  = WeightInit.ones(Shape.of(128));
```

## 数据归一化（V1.0.3）

```java
import cloud.opencode.base.neural.norm.*;

// Min-Max 归一化 [0, 1]
Normalizer minMax = new MinMaxNormalizer();
minMax.fit(trainingData);
Tensor normalized = minMax.normalize(inputData);
Tensor original   = minMax.denormalize(normalized); // 可逆

// Z-Score 标准化（均值=0，标准差=1）
Normalizer zScore = new ZScoreNormalizer();
zScore.fit(trainingData);
Tensor standardized = zScore.normalize(inputData);

// L2 归一化（单位向量，无需 fit）
Normalizer l2 = new L2Normalizer();
Tensor unitVec = l2.normalize(inputData);
```

## 评估指标（V1.0.3）

```java
import cloud.opencode.base.neural.metric.*;

// 分类指标
Metric accuracy = new Accuracy();
Tensor acc = accuracy.compute(predictions, labels); // 0.0 ~ 1.0

Metric f1 = new F1Score();
Tensor f1Score = f1.compute(binaryPredictions, binaryLabels);

// 混淆矩阵
int[][] matrix = ConfusionMatrix.compute(predictions, labels, numClasses);
float classPrec   = ConfusionMatrix.precision(matrix, classIdx);
float classRecall = ConfusionMatrix.recall(matrix, classIdx);

// 回归指标
Tensor mse = RegressionMetrics.mse(predicted, target);
Tensor r2  = RegressionMetrics.rSquared(predicted, target);
```

## 模型格式 (.ocm)

opencode-base-neural 使用 **OpenCode Model (.ocm)** 二进制格式：

- **零外部依赖** — 不需要 protobuf、FlatBuffers
- **偏移量随机访问** — 高效加载权重
- **完整性校验** — 魔数 + 版本号验证
- **大端序** — 与 Java DataInputStream 一致

文件结构：`Header(64B) → Metadata → Graph → Weights → StringTable`

## 会话配置

```java
import cloud.opencode.base.neural.session.SessionConfig;

SessionConfig config = SessionConfig.builder()
    .threadPoolSize(4)          // 算子内并行线程数
    .tensorPoolCapacity(128)    // 张量缓冲池大小
    .enableProfiling(true)      // 启用算子级耗时统计
    .maxMemoryBytes(512_000_000) // 512MB 内存限制
    .build();

try (InferenceSession session = InferenceSession.load(path, config)) {
    session.warmup(3); // JIT 预热

    Map<String, Tensor> out = session.run(inputs);

    // 查看性能分析结果
    ProfilingResult profile = session.lastProfilingResult();
    System.out.println(profile.summary());
    System.out.printf("总耗时: %.2f ms%n", profile.totalTimeMillis());
}
```

## INT8 量化推理（内部 API）

> **注意：** 量化工具位于 `internal` 包中，未通过 JPMS 导出。
> 它由推理引擎内部用于权重压缩。
> 直接使用需要 `--add-opens` 或模块补丁。

```java
// 内部使用示例（非公开 API）
import cloud.opencode.base.neural.internal.Quantization;
import cloud.opencode.base.neural.internal.Quantization.QuantizedData;

// 量化权重：float32 → int8（内存减少 4 倍）
float[] weights = model.getWeights();
QuantizedData quantized = Quantization.quantize(weights);
// quantized.data() → byte[], quantized.scale() → float

// 量化 GEMM：float32 激活值 × int8 权重 → float32 输出
Quantization.gemmQuantized(activations, aRows, aCols,
    quantized.data(), quantized.scale(), bRows, bCols, output);
```

## 性能特性

| 策略 | 说明 | 收益 |
|------|------|------|
| im2col + GEMM | 卷积矩阵化 | 比直接滑窗快 5-10x |
| 特化 GEMM | NN/NT 快速路径，64×64 分块，4x 展开 | 消除分支预测开销 |
| 零拷贝张量 | 所有 Op 输出使用 `Tensor.wrap()` 避免 `data.clone()` | 分配量减少 ~50% |
| 连续内存快速路径 | elementwise/sum/mean/argmax 直接数组访问 | 比步幅索引快 2-7x |
| BLAS 委托 | `Tensor.matmul()` → 分块 GEMM + 自动并行 | 比标量循环快 5-10x |
| ForkJoinPool 并行 | 大矩阵自动并行处理（M×K > 65K） | 多核加速 4-8x |
| 连续性缓存 | `isContiguous()` 构造时计算一次 | 每次调用 O(rank) → O(1) |
| LSTM 门融合 | 4 个门合并为 1 次矩阵乘法 | 矩阵乘法次数减少 4x |
| INT8 量化 | float32 权重 → int8 | 内存 4x，计算加速 |
| ThreadLocal 池化 | 每线程张量缓冲区复用 | 降低 GC 压力 |

### 基准测试数据（JDK 25，Apple M 系列）

```
张量 add [256×256]        42 μs    BLAS GEMM 256×256     4.0 ms
张量 matmul [128×256]  1,005 μs    BLAS transB 128×512   3.5 ms
ReLU [100K]                 29 μs    Conv2D [1,3,32,32]      156 μs
Sigmoid [100K]             529 μs    深度可分离卷积 [16ch]    174 μs
GELU [100K]              1,188 μs    Linear [32,512→256]     921 μs
Softmax [32×1000]          153 μs    BatchNorm [1,64,16,16]   33 μs
MSE 损失 [10K]              21 μs    MaxPool2D [16,32,32]     97 μs
交叉熵 [32×100]              9 μs    GlobalAvgPool [64,7,7]    7 μs
```

## JPMS 模块系统

```java
module your.module {
    requires cloud.opencode.base.neural;
}
```

导出的包：`exception`, `tensor`, `op`, `model`, `session`, `loss`, `init`, `norm`, `metric`

## 系统要求

- **JDK 25+**（不使用预览特性）
- **零 native 依赖**
- 兼容 GraalVM native-image

## 许可证

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## 相关链接

- [OpenCode.cloud](https://opencode.cloud)
- [GitHub](https://github.com/opencode-cloud/opencode-base)
- [API 文档](https://opencode.cloud/docs/neural)
