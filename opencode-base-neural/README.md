# opencode-base-neural

**Pure Java Neural Network Forward Inference Engine for JDK 25+**

A lightweight, zero-dependency inference engine that runs CNN, LSTM, and CTC models entirely in Java — no native libraries, no JNI, no platform-specific binaries.

[![Java](https://img.shields.io/badge/JDK-25%2B-blue)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0)

## Why opencode-base-neural?

| Problem | Other Frameworks | opencode-base-neural |
|---------|-----------------|---------------------|
| Native dependency hell | 200-500MB native libs (PyTorch/TF) | **Zero native. One JAR. < 500KB.** |
| Cross-platform deployment | Separate binaries per OS/arch | **Any JVM, anywhere.** |
| Docker image bloat | 200MB → 1GB+ | **Zero bloat.** |
| Serverless cold start | 10-30s native lib loading | **< 2s cold start.** |
| API complexity | "Feels like writing XML" | **3 lines: Load → Run → Done.** |

## Quick Start

### Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-neural</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Three-Step Inference

```java
import cloud.opencode.base.neural.session.InferenceSession;
import cloud.opencode.base.neural.tensor.Tensor;
import cloud.opencode.base.neural.tensor.Shape;

import java.nio.file.Path;
import java.util.Map;

// 1. Load model
try (InferenceSession session = InferenceSession.load(Path.of("model.ocm"))) {

    // 2. Prepare input
    float[] inputData = new float[1 * 3 * 224 * 224]; // your preprocessed image data
    Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 3, 224, 224));

    // 3. Run inference
    Map<String, Tensor> outputs = session.run(Map.of("input", input));
    float[] scores = outputs.get("output").toFloatArray();

    // Find top prediction
    Tensor argmax = outputs.get("output").argmax(1);
    System.out.println("Predicted class: " + (int) argmax.getFloat(0));
}
```

### Image Classification (with opencode-base-image)

```java
import cloud.opencode.base.neural.tensor.ImageToTensor;
import cloud.opencode.base.neural.tensor.ImageToTensor.ConvertOptions;
import cloud.opencode.base.neural.session.InferenceSession;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

BufferedImage img = ImageIO.read(new File("cat.jpg"));

// ImageNet normalization
Tensor input = ImageToTensor.convert(img, ConvertOptions.builder()
    .targetWidth(224)
    .targetHeight(224)
    .mean(new float[]{0.485f, 0.456f, 0.406f})
    .std(new float[]{0.229f, 0.224f, 0.225f})
    .build());

try (InferenceSession session = InferenceSession.load(Path.of("mobilenetv2.ocm"))) {
    Map<String, Tensor> out = session.run(Map.of("input", input));
    // out.get("output") → [1, 1000] class probabilities
}
```

## Architecture

```
┌─────────────────────────────────────────────┐
│          session/ (User API)                │  InferenceSession, SessionConfig
├─────────────────────────────────────────────┤
│          model/ (Model & Graph)             │  OcmLoader, Graph, OcmModel
├─────────────────────────────────────────────┤
│     op/ (30 Operators)  loss/  metric/      │  Conv2D, LSTM, GELU, ...
├─────────────────────────────────────────────┤
│     tensor/  init/  norm/                   │  Tensor, WeightInit, Normalizer
├─────────────────────────────────────────────┤
│          internal/ (Engine)                 │  Blas, Im2Col, Activation
└─────────────────────────────────────────────┘
```

## Built-in Operators (30)

| Category | Operators |
|----------|-----------|
| **CNN** | Conv1D, Conv2D, DepthwiseConv2D, MaxPool2D, AvgPool2D, GlobalAvgPool, BatchNorm, Linear |
| **Activation** | ReLU, Sigmoid, Tanh, Softmax, HardSigmoid, HardSwish, **LeakyReLU**, **ELU**, **SELU**, **GELU**, **Swish**, **Mish**, **Softplus** |
| **Sequence** | LSTM, BiLSTM, CTCDecode (greedy), CTCBeamSearch |
| **Structure** | Flatten, Reshape, Dropout, Add, Concat |

### Custom Operator Registration

```java
import cloud.opencode.base.neural.op.OpRegistry;

// Register at startup
OpRegistry.register("MyCustomOp", () -> (inputs, attrs) -> {
    Tensor x = inputs.get(0);
    float[] data = x.toFloatArray();
    // your custom computation
    return List.of(Tensor.fromFloat(data, x.shape()));
});
```

## Tensor API

```java
import cloud.opencode.base.neural.tensor.*;

// Creation
Tensor a = Tensor.zeros(Shape.of(2, 3));
Tensor b = Tensor.ones(Shape.of(2, 3));
Tensor c = Tensor.fromFloat(new float[]{1,2,3,4,5,6}, Shape.of(2, 3));

// Element-wise operations
Tensor sum = a.add(b);
Tensor product = a.mul(b);

// Reshaping (zero-copy view)
Tensor flat = c.flatten();           // [6]
Tensor reshaped = c.reshape(3, 2);   // [3, 2]
Tensor transposed = c.transpose(1, 0); // [3, 2]

// Reduction
Tensor rowSum = c.sum(1);            // [2]
Tensor colMean = c.mean(0);          // [3]
Tensor maxIdx = c.argmax(1);         // [2]

// Matrix multiplication
Tensor d = Tensor.fromFloat(new float[]{1,2,3,4,5,6}, Shape.of(3, 2));
Tensor result = c.matmul(d);         // [2, 2]

// Math operations
Tensor exp = TensorMath.exp(c);
Tensor relu = TensorMath.relu(c);
Tensor clamped = TensorMath.clamp(c, 0f, 5f);

// New in V1.0.3: extended activation functions
Tensor gelu = TensorMath.gelu(c);
Tensor swish = TensorMath.swish(c);
Tensor mish = TensorMath.mish(c);

// Factory methods
Tensor eye = TensorFactory.eye(3);       // 3x3 identity
Tensor range = TensorFactory.arange(10); // [0,1,...,9]
```

## Loss Functions (V1.0.3)

```java
import cloud.opencode.base.neural.loss.*;

// Mean Squared Error
LossFunction mse = new MseLoss();
Tensor loss = mse.compute(predicted, target); // scalar tensor

// Cross-Entropy (for classification)
LossFunction ce = new CrossEntropyLoss();     // categorical [N,C]
LossFunction bce = new BinaryCrossEntropyLoss(); // binary [N]

// Huber Loss (smooth L1)
LossFunction huber = new HuberLoss(1.0f);     // configurable delta

// Others: MaeLoss, CosineSimilarityLoss
```

## Weight Initialization (V1.0.3)

```java
import cloud.opencode.base.neural.init.WeightInit;
import java.util.Random;

Random rng = new Random(42);

// Xavier/Glorot initialization (good for sigmoid/tanh)
Tensor w1 = WeightInit.xavierUniform(Shape.of(256, 128), rng);
Tensor w2 = WeightInit.xavierNormal(Shape.of(256, 128), rng);

// He/Kaiming initialization (good for ReLU)
Tensor w3 = WeightInit.heUniform(Shape.of(256, 128), rng);
Tensor w4 = WeightInit.heNormal(Shape.of(256, 128), rng);

// LeCun initialization (good for SELU)
Tensor w5 = WeightInit.lecunNormal(Shape.of(256, 128), rng);

// Basic initialization
Tensor zeros = WeightInit.zeros(Shape.of(128));
Tensor ones = WeightInit.ones(Shape.of(128));
```

## Data Normalization (V1.0.3)

```java
import cloud.opencode.base.neural.norm.*;

// Min-Max normalization [0, 1]
Normalizer minMax = new MinMaxNormalizer();
minMax.fit(trainingData);
Tensor normalized = minMax.normalize(inputData);
Tensor original = minMax.denormalize(normalized); // inverse

// Z-Score standardization (mean=0, std=1)
Normalizer zScore = new ZScoreNormalizer();
zScore.fit(trainingData);
Tensor standardized = zScore.normalize(inputData);

// L2 normalization (unit vectors, no fit required)
Normalizer l2 = new L2Normalizer();
Tensor unitVec = l2.normalize(inputData);
```

## Metrics (V1.0.3)

```java
import cloud.opencode.base.neural.metric.*;

// Classification metrics
Metric accuracy = new Accuracy();
Tensor acc = accuracy.compute(predictions, labels); // 0.0 ~ 1.0

Metric f1 = new F1Score();
Tensor f1Score = f1.compute(binaryPredictions, binaryLabels);

// Confusion matrix
int[][] matrix = ConfusionMatrix.compute(predictions, labels, numClasses);
float classPrec = ConfusionMatrix.precision(matrix, classIdx);
float classRecall = ConfusionMatrix.recall(matrix, classIdx);

// Regression metrics
Tensor mse = RegressionMetrics.mse(predicted, target);
Tensor r2  = RegressionMetrics.rSquared(predicted, target);
```

## Model Format (.ocm)

opencode-base-neural uses the **OpenCode Model (.ocm)** binary format:

- **Zero external dependencies** — no protobuf, no FlatBuffers
- **Offset-based random access** — efficient weight loading
- **Integrity verification** — magic number + version validation
- **Big-endian** — consistent with Java's DataInputStream

Structure: `Header(64B) → Metadata → Graph → Weights → StringTable`

## Session Configuration

```java
import cloud.opencode.base.neural.session.SessionConfig;

SessionConfig config = SessionConfig.builder()
    .threadPoolSize(4)          // parallel threads for operators
    .tensorPoolCapacity(128)    // tensor buffer pool size
    .enableProfiling(true)      // operator-level timing
    .maxMemoryBytes(512_000_000) // 512MB memory limit
    .build();

try (InferenceSession session = InferenceSession.load(path, config)) {
    session.warmup(3); // JIT warmup

    Map<String, Tensor> out = session.run(inputs);

    // Profiling results
    ProfilingResult profile = session.lastProfilingResult();
    System.out.println(profile.summary());
    System.out.printf("Total: %.2f ms%n", profile.totalTimeMillis());
}
```

## INT8 Quantization (Internal API)

> **Note:** Quantization is in the `internal` package and not exported via JPMS.
> It is used internally by the inference engine for weight compression.
> Direct usage requires `--add-opens` or module patching.

```java
// Internal usage example (not part of public API)
import cloud.opencode.base.neural.internal.Quantization;
import cloud.opencode.base.neural.internal.Quantization.QuantizedData;

// Quantize weights: float32 → int8 (4x memory reduction)
float[] weights = model.getWeights();
QuantizedData quantized = Quantization.quantize(weights);
// quantized.data() → byte[], quantized.scale() → float

// Quantized GEMM: float32 activations × int8 weights → float32 output
Quantization.gemmQuantized(activations, aRows, aCols,
    quantized.data(), quantized.scale(), bRows, bCols, output);
```

## Performance Characteristics

| Strategy | Description | Benefit |
|----------|------------|---------|
| im2col + GEMM | Conv2D as matrix multiplication | 5-10x vs naive sliding window |
| Specialized GEMM | NN/NT fast paths, 64×64 tiling, 4x unrolling | Eliminates branch overhead |
| Zero-copy tensors | `Tensor.wrap()` avoids `data.clone()` in all Op outputs | ~50% less allocation |
| Contiguous fast paths | Flat-array access for elementwise/sum/mean/argmax | 2-7x vs strided indexing |
| BLAS delegation | `Tensor.matmul()` → tiled GEMM with parallelism | 5-10x vs scalar loop |
| ForkJoinPool | Auto-parallel for large matrices (M×K > 65K) | 4-8x on multi-core |
| Cached contiguity | `isContiguous()` computed once at construction | O(rank) → O(1) per call |
| Fused LSTM gates | 4 gates → 1 matmul | 4x fewer matmuls |
| INT8 quantization | float32 weights → int8 | 4x memory, faster multiply |
| ThreadLocal pooling | Per-thread tensor buffer reuse across inference calls | Reduced GC pressure |

### Benchmark Data (JDK 25, Apple M-series)

```
Tensor add [256×256]        42 μs    BLAS GEMM 256×256     4.0 ms
Tensor matmul [128×256]  1,005 μs    BLAS transB 128×512   3.5 ms
ReLU [100K]                 29 μs    Conv2D [1,3,32,32]      156 μs
Sigmoid [100K]             529 μs    DepthwiseConv [16ch]    174 μs
GELU [100K]              1,188 μs    Linear [32,512→256]     921 μs
Softmax [32×1000]          153 μs    BatchNorm [1,64,16,16]   33 μs
MSE Loss [10K]              21 μs    MaxPool2D [16,32,32]     97 μs
CrossEntropy [32×100]        9 μs    GlobalAvgPool [64,7,7]    7 μs
```

## Module System (JPMS)

```java
module your.module {
    requires cloud.opencode.base.neural;
}
```

Exported packages: `exception`, `tensor`, `op`, `model`, `session`, `loss`, `init`, `norm`, `metric`

## Requirements

- **JDK 25+** (no preview features)
- **Zero native dependencies**
- Works with GraalVM native-image

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## Links

- [OpenCode.cloud](https://opencode.cloud)
- [GitHub](https://github.com/opencode-cloud/opencode-base)
- [API Documentation](https://opencode.cloud/docs/neural)
