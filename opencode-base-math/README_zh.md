# OpenCode Base Math

**JDK 25+ 高级数学与统计计算库**

纯 Java 实现、零外部依赖的数学库，提供统计分析、线性代数、插值、数值积分、组合数学、概率分布和特殊函数。与 `opencode-base-core` 中的基础算术功能互补。

## 快速开始

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-math</artifactId>
    <version>1.0.3</version>
</dependency>
```

```java
module your.module {
    requires cloud.opencode.base.math;
}
```

## 功能特性

### 统计 (`math.stats`)
- **描述统计**: 百分位数、众数、偏度、峰度、极差、四分位距
- **相关性**: 协方差、Pearson 相关、Spearman 等级相关、Kendall tau-b（O(n log n) 归并排序算法）
- **均值**: 加权平均、几何平均、调和平均
- **回归**: 简单线性回归（R²、预测、残差）
- **百分位数**: 可配置插值方法（LINEAR、LOWER、HIGHER、NEAREST、MIDPOINT）
- **流式统计**: 基于 Welford 在线算法的均值/方差/标准差/极值，支持并行合并

### 假设检验 (`math.stats.inference`)
- **T 检验**: 单样本、Welch 双样本、配对 t 检验
- **卡方检验**: 拟合优度、独立性检验
- **方差分析**: 单因素 ANOVA F 检验
- **TestResult**: 统计量、p 值、自由度、显著性判断

### 线性代数 (`math.linalg`)
- **向量 Vector**: 点积/叉积、归一化、夹角、距离、算术运算
- **矩阵 Matrix**: 乘法、转置、行列式（LU 分解）、逆矩阵（LU 分解）、迹

### 插值 (`math.interpolation`)
- 分段线性插值（O(log n) 二分查找）
- Lagrange 多项式插值（重心形式，每次求值 O(n)）
- Newton 差商���值
- 自然三次样条插值（支持预计算/求值分离，适用于多点查询）

### 数值积分 (`math.integration`)
- 复合梯形法则
- Simpson 1/3 和 3/8 法则
- Romberg 积分（Richardson 外推）
- Gauss-Legendre 求积（2-5 点）

### 组合数学 (`math.combinatorics`)
- 二项式系数（long + BigInteger）
- 排列数、Catalan 数、第二类 Stirling 数
- Bell 数、错排数（subfactorial）

### 数值分析 (`math.analysis`)
- **求根**: 二分法、Brent 法、Newton-Raphson 法、割线法
- **数值微分**: 中心差分、二阶导数、Richardson 外推

### 概率分布 (`math.distribution`)
- **连续分布**: 正态、Student's t、卡方、F、Gamma、Beta、对数正态
- **离散分布**: 二项、泊松
- **工具方法**: 指数分布 PDF/CDF、均匀分布 PDF
- 全部支持: PDF/PMF、CDF、逆 CDF、参数访问

### 特殊函数 (`math.special`)
- Gamma 函数、log-Gamma（Lanczos 近似）
- Beta 函数
- 误差函数（erf、erfc）
- 正则化不完全 Beta 和 Gamma 函数

## 使用示例

### 统计分析

```java
import cloud.opencode.base.math.stats.Statistics;
import cloud.opencode.base.math.stats.Regression;
import cloud.opencode.base.math.stats.Percentile;

double[] data = {2.0, 4.0, 6.0, 8.0, 10.0};

// 描述统计
double p90 = Statistics.percentile(data, 90);       // 第 90 百分位
double[] modes = Statistics.mode(data);              // 众数
double skew = Statistics.skewness(data);             // 样本偏度
double kurt = Statistics.kurtosis(data);             // 超额峰度
double iqr = Statistics.interquartileRange(data);    // 四分位距 Q3 - Q1

// 相关性
double[] x = {1, 2, 3, 4, 5};
double[] y = {2, 4, 5, 4, 5};
double cov = Statistics.covariance(x, y);            // 样本协方差
double r = Statistics.correlation(x, y);             // Pearson r ∈ [-1, 1]

// 加权/几何/调和平均值
double wm = Statistics.weightedMean(data, new double[]{1, 2, 3, 2, 1});
double gm = Statistics.geometricMean(data);
double hm = Statistics.harmonicMean(data);

// 线性回归
Regression.LinearModel model = Regression.linear(x, y);
double slope = model.slope();           // 斜率
double intercept = model.intercept();   // 截距
double rSquared = model.rSquared();     // 决定系数
double predicted = model.predict(6.0);  // 预测值
double[] residuals = model.residuals(x, y);  // 残差

// 可配置百分位数
Percentile pctl = Percentile.of(data);
double median = pctl.quartile(2);                     // Q2 = 中位数
double q1 = pctl.value(25, Percentile.Method.LOWER);  // 下四分位数
```

### 线性代数

```java
import cloud.opencode.base.math.linalg.Vector;
import cloud.opencode.base.math.linalg.Matrix;

// 向量运算
Vector v1 = Vector.of(1.0, 2.0, 3.0);
Vector v2 = Vector.of(4.0, 5.0, 6.0);
double dot = v1.dot(v2);                       // 点积 = 32.0
Vector cross = v1.cross(v2);                   // 叉积 = [-3, 6, -3]
double angle = v1.angle(v2);                   // 夹角（弧度）
double dist = v1.distanceTo(v2);               // 欧几里得距离
Vector unit = v1.normalize();                  // 单位向量

// 矩阵运算
Matrix a = Matrix.of(new double[][]{
    {1, 2},
    {3, 4}
});
Matrix product = a.multiply(Matrix.identity(2));  // 矩阵乘法
Matrix transposed = a.transpose();                // 转置
double det = a.determinant();                     // 行列式 = -2.0
Matrix inv = a.inverse();                         // 逆矩阵
double tr = a.trace();                            // 迹 = 5.0

// 矩阵-向量乘法
Vector v = Vector.of(1.0, 2.0);
Vector result = a.multiplyVector(v);              // [5, 11]
```

### 插值

```java
import cloud.opencode.base.math.interpolation.Interpolation;

double[] x = {0, 1, 2, 3, 4};
double[] y = {0, 1, 4, 9, 16};    // y = x²

// 分段线性插值（O(log n) 查找）
double v1 = Interpolation.linear(x, y, 1.5);          // 2.5

// Lagrange 多项式（n-1 次多项式精确）
double v2 = Interpolation.lagrange(x, y, 1.5);        // 2.25（精确 x²）

// Newton 差商插值
double v3 = Interpolation.newtonDividedDifference(x, y, 1.5);

// 自然三次样条（光滑，C2 连续）
double v4 = Interpolation.cubicSpline(x, y, 1.5);

// 预计算样条系数，高效多点求值
var spline = Interpolation.precomputeSpline(x, y);
double a = spline.evaluate(0.5);   // 一次预计算，多次求值
double b = spline.evaluate(2.5);   // 每次查询 O(log n)，无冗余 O(n) 预处理
```

### 数值积分

```java
import cloud.opencode.base.math.integration.NumericalIntegration;

// 积分 sin(x) 从 0 到 π = 2.0
double trap = NumericalIntegration.trapezoid(Math::sin, 0, Math.PI, 100);
double simp = NumericalIntegration.simpson(Math::sin, 0, Math.PI, 100);

// 高精度 Romberg 积分
double romb = NumericalIntegration.romberg(Math::sin, 0, Math.PI, 10, 1e-12);

// Gauss-Legendre 求积（2-5 点）
double gl = NumericalIntegration.gaussLegendre(Math::sin, 0, Math.PI, 5);

// 自定义函数：积分 e^x 从 0 到 1 = e - 1
double ex = NumericalIntegration.simpson(Math::exp, 0, 1, 100);
```

### 组合数学

```java
import cloud.opencode.base.math.combinatorics.Combinatorics;

long c = Combinatorics.binomial(20, 10);        // C(20,10) = 184756
long p = Combinatorics.permutation(10, 3);      // P(10,3) = 720
long cat = Combinatorics.catalanNumber(10);     // 第 10 个 Catalan 数 = 16796
long stir = Combinatorics.stirlingSecond(5, 3); // S(5,3) = 25
long bell = Combinatorics.bellNumber(5);        // B(5) = 52
long der = Combinatorics.derangements(5);       // !5 = 44

// 任意精度（大数值）
java.math.BigInteger big = Combinatorics.binomialBig(100, 50);
```

### 假设检验

```java
import cloud.opencode.base.math.stats.inference.*;

// 单样本 t 检验：均值是否等于 5？
double[] data = {5.2, 5.8, 6.1, 4.9, 5.5};
TestResult r1 = TTest.oneSample(data, 5.0);
System.out.println(r1.pValue());           // p 值
System.out.println(r1.isSignificant());    // p < 0.05 时为 true

// Welch 双样本 t 检验
TestResult r2 = TTest.twoSample(groupA, groupB);

// 卡方拟合优度检验
TestResult r3 = ChiSquareTest.goodnessOfFit(
    new double[]{10, 20, 30},    // 观测频次
    new double[]{20, 20, 20});   // 期望频次

// 单因素方差分析
TestResult r4 = AnovaTest.oneWay(groupA, groupB, groupC);
```

### 流式统计

```java
import cloud.opencode.base.math.stats.StreamingStatistics;

// 在线计算 — 无需存储全部数据
StreamingStatistics ss = StreamingStatistics.create();
for (double value : dataStream) {
    ss.add(value);
}
double mean = ss.mean();
double stddev = ss.sampleStdDev();

// 并行合并
StreamingStatistics merged = part1.merge(part2);

// Stream 收集器
StreamingStatistics result = values.stream()
    .collect(StreamingStatistics.collector());
```

### 概率分布

```java
import cloud.opencode.base.math.distribution.*;

// 正态分布
NormalDistribution std = NormalDistribution.STANDARD;
double cdf = std.cdf(1.96);            // 0.975...

// Student t 分布
TDistribution t10 = TDistribution.of(10);
double p = t10.cdf(1.812);             // ≈ 0.95

// 卡方分布
ChiSquaredDistribution chi5 = ChiSquaredDistribution.of(5);
double critical = chi5.inverseCdf(0.95); // ≈ 11.07

// F 分布
FDistribution f = FDistribution.of(5, 10);

// 二项分布
BinomialDistribution binom = BinomialDistribution.of(10, 0.5);
double pmf = binom.pmf(5);             // ≈ 0.2461

// Gamma、Beta、对数正态分布
GammaDistribution gamma = GammaDistribution.of(2, 1);
BetaDistribution beta = BetaDistribution.of(2, 5);
LogNormalDistribution logn = LogNormalDistribution.of(0, 1);
```

### 求根与微分

```java
import cloud.opencode.base.math.analysis.RootFinder;
import cloud.opencode.base.math.analysis.Differentiation;

// 求 √2：解 x² - 2 = 0
double sqrt2 = RootFinder.brent(x -> x * x - 2, 0, 2, 1e-12);

// 牛顿法（提供解析导数）
double root = RootFinder.newton(x -> x * x - 2, x -> 2 * x, 1.0, 1e-12);

// sin(x) 在 π/4 处的数值导数
double d = Differentiation.derivative(Math::sin, Math.PI / 4);  // ≈ cos(π/4)
```

### 门面类 — 一站式导入

```java
import cloud.opencode.base.math.OpenMathLib;

double sqrt2 = OpenMathLib.findRoot(x -> x * x - 2, 0, 2);
double area = OpenMathLib.integrate(Math::sin, 0, Math.PI);
double p90 = OpenMathLib.percentile(data, 90);
long c = OpenMathLib.binomial(20, 10);
```

### 特殊函数

```java
import cloud.opencode.base.math.special.SpecialFunctions;

double g = SpecialFunctions.gamma(5.0);              // 24.0 (= 4!)
double lg = SpecialFunctions.logGamma(100.0);        // ln(99!)
double b = SpecialFunctions.beta(2.0, 3.0);          // 1/12
double erf = SpecialFunctions.erf(1.0);              // 0.8427...
double erfc = SpecialFunctions.erfc(1.0);            // 0.1573...
double rb = SpecialFunctions.regularizedBeta(0.5, 2, 3);
double rg = SpecialFunctions.regularizedGammaP(3, 2);
```

## API 参考

### `OpenMathLib` — 统一门面

| 方法 | 说明 |
|------|------|
| `percentile(data, p)` | 百分位数 |
| `correlation(x, y)` / `spearmanCorrelation(x, y)` | Pearson / Spearman 相关 |
| `linearRegression(x, y)` | 线性回归 |
| `streamingStats()` | 创建流式统计累加器 |
| `tTestOneSample(data, mu0)` / `tTestTwoSample(x, y)` | T 检验 |
| `vector(...)` / `matrix(data)` / `identityMatrix(n)` | 向量/矩阵创建 |
| `findRoot(f, a, b)` | Brent 求根 |
| `derivative(f, x)` / `integrate(f, a, b)` | 数值微分/积分 |
| `binomial(n, k)` / `permutation(n, k)` | 组合/排列 |
| `gamma(x)` / `erf(x)` / `beta(a, b)` | 特殊函数 |

### `Statistics` — 描述统计

| 方法 | 说明 |
|------|------|
| `percentile(double[] data, double p)` | 第 p 百分位数（p ∈ [0,100]），线性插值 |
| `mode(double[] data)` | 所有众数（出现频率最高的值），已排序 |
| `skewness(double[] data)` | 样本偏度（Fisher 定义） |
| `kurtosis(double[] data)` | 样本超额峰度 |
| `covariance(double[] x, double[] y)` | 样本协方差 |
| `correlation(double[] x, double[] y)` | Pearson 相关系数 [-1, 1] |
| `spearmanCorrelation(double[] x, double[] y)` | Spearman 等级相关 [-1, 1] |
| `kendallCorrelation(double[] x, double[] y)` | Kendall tau-b 相关 [-1, 1] |
| `weightedMean(double[] values, double[] weights)` | 加权算术平均 |
| `geometricMean(double[] values)` | 几何平均（值必须 > 0） |
| `harmonicMean(double[] values)` | 调和平均（值必须 > 0） |
| `range(double[] data)` | 极差 = max - min |
| `interquartileRange(double[] data)` | 四分位距 = Q3 - Q1 |

### `Regression` — 线性回归

| 方法 | 说明 |
|------|------|
| `linear(double[] x, double[] y)` | 简单线性回归，返回 `LinearModel` |
| `LinearModel.slope()` | 斜率系数 |
| `LinearModel.intercept()` | Y 轴截距 |
| `LinearModel.rSquared()` | 决定系数 [0, 1] |
| `LinearModel.predict(double x)` | 对给定 x 预测 y |
| `LinearModel.residuals(double[] x, double[] y)` | 残差数组（观测值 - 预测值） |

### `Percentile` — 可配置百分位数计算器

| 方法 | 说明 |
|------|------|
| `of(double[] data)` | 从数据创建（内部排序） |
| `value(double p)` | 使用 LINEAR 方法计算第 p 百分位数 |
| `value(double p, Method method)` | 使用指定插值方法 |
| `quartile(int q)` | Q1(q=1)、Q2/中位数(q=2)、Q3(q=3) |

插值方法: `LINEAR`、`LOWER`、`HIGHER`、`NEAREST`、`MIDPOINT`

### `Vector` — 不可变向量

| 方法 | 说明 |
|------|------|
| `of(double... components)` | 从分量创建向量 |
| `zero(int dimension)` | 指定维度的零向量 |
| `unit(int dimension, int index)` | 单位向量（index 处为 1） |
| `add(Vector)` / `subtract(Vector)` | 逐元素算术运算 |
| `scale(double)` / `negate()` | 标量运算 |
| `dot(Vector)` | 点积 |
| `cross(Vector)` | 叉积（仅限 3D） |
| `magnitude()` | 欧几里得范数 |
| `normalize()` | 同方向单位向量 |
| `angle(Vector)` | 夹角（弧度）[0, π] |
| `distanceTo(Vector)` | 欧几里得距离 |

### `Matrix` — 不可变矩阵

| 方法 | 说明 |
|------|------|
| `of(double[][])` | 从二维数组创建（防御性拷贝） |
| `identity(int n)` | n × n 单位矩阵 |
| `zero(int rows, int cols)` | 零矩阵 |
| `add(Matrix)` / `subtract(Matrix)` | 逐元素算术运算 |
| `multiply(Matrix)` | 矩阵乘法 |
| `multiplyVector(Vector)` | 矩阵-向量乘法 |
| `scalarMultiply(double)` | 标量乘法 |
| `transpose()` | 转置 |
| `determinant()` | 行列式（LU 分解） |
| `inverse()` | 逆矩阵（LU 分解 + 部分主元选取） |
| `trace()` | 对角线元素之和 |
| `isSquare()` / `isSymmetric()` | 属性检查 |

### `Interpolation` — 插值方法

| 方法 | 说明 |
|------|------|
| `linear(x[], y[], xi)` | 分段线性，O(log n) 每次查询 |
| `lagrange(x[], y[], xi)` | Lagrange 多项式（重心形式），O(n) 每次求值 |
| `newtonDividedDifference(x[], y[], xi)` | Newton 差商形式 |
| `precomputeSpline(x[], y[])` | 预计算样条系数，返回 `SplineCoefficients` |
| `cubicSpline(x[], y[], xi)` | 自然三次样条，C2 光滑（便捷单点方法） |

### `NumericalIntegration` — 数值求积

| 方法 | 说明 |
|------|------|
| `trapezoid(f, a, b, n)` | 复合梯形法则 |
| `simpson(f, a, b, n)` | Simpson 1/3 法则（n 须为偶数） |
| `simpsonThreeEighths(f, a, b, n)` | Simpson 3/8 法则（n 须被 3 整除） |
| `romberg(f, a, b, maxIter, tol)` | Romberg 积分（Richardson 外推） |
| `gaussLegendre(f, a, b, points)` | Gauss-Legendre 求积（2-5 点） |

### `Combinatorics` — 组合函数

| 方法 | 说明 |
|------|------|
| `binomial(n, k)` / `binomialBig(n, k)` | 二项式系数 C(n,k) |
| `permutation(n, k)` / `permutationBig(n, k)` | 排列数 P(n,k) |
| `catalanNumber(n)` / `catalanBig(n)` | 第 n 个 Catalan 数 |
| `stirlingSecond(n, k)` | 第二类 Stirling 数 S(n,k) |
| `bellNumber(n)` | 第 n 个 Bell 数 |
| `derangements(n)` | 错排数 !n |

### `NormalDistribution` — 正态分布

| 方法 | 说明 |
|------|------|
| `STANDARD` | 标准正态 N(0, 1) 单例 |
| `of(mean, stdDev)` | 创建自定义 N(mean, stdDev) |
| `pdf(x)` | 概率密度函数 |
| `cdf(x)` | 累积分布函数 |
| `inverseCdf(p)` | 分位数函数（p ∈ (0,1)） |
| `sample()` / `sample(n)` | 随机采样（Box-Muller） |

### `Distributions` — 分布工具

| 方法 | 说明 |
|------|------|
| `uniform(min, max)` | 均匀分布 PDF（返回 `DoubleUnaryOperator`） |
| `exponentialPdf(lambda, x)` | 指数分布 PDF |
| `exponentialCdf(lambda, x)` | 指数分布 CDF |
| `poissonPmf(lambda, k)` | 泊松分布概率质量函数 |
| `poissonCdf(lambda, k)` | 泊松分布 CDF: P(X ≤ k) |

### `StreamingStatistics` — 在线统计累加器

| 方法 | 说明 |
|------|------|
| `create()` | 创建空累加器 |
| `add(double value)` | 添加数据值 |
| `count()` / `mean()` / `sum()` | 基本统计 |
| `variance()` / `stdDev()` | 总体方差/标准差 |
| `sampleVariance()` / `sampleStdDev()` | 样本方差/标准差 (n-1) |
| `min()` / `max()` | 极值 |
| `merge(StreamingStatistics)` | 合并两个累加器（返回新实例） |
| `collector()` | 用于 Stream 的 Collector |

### `TTest` / `ChiSquareTest` / `AnovaTest` — 假设检验

| 方法 | 说明 |
|------|------|
| `TTest.oneSample(data, mu0)` | 单样本 t 检验 |
| `TTest.twoSample(x, y)` | Welch 双样本 t 检验 |
| `TTest.paired(x, y)` | 配对 t 检验 |
| `ChiSquareTest.goodnessOfFit(obs, exp)` | 卡方拟合优度检验 |
| `ChiSquareTest.independence(table)` | 卡方独立性检验 |
| `AnovaTest.oneWay(groups...)` | 单因素方差分析 |

返回 `TestResult(testName, statistic, pValue, degreesOfFreedom)`，提供 `isSignificant(alpha)` 方法。

### `RootFinder` — 方程求解

| 方法 | 说明 |
|------|------|
| `bisection(f, a, b, tol)` | 二分法（保证收敛） |
| `brent(f, a, b, tol)` | Brent 法（推荐，快速+可靠） |
| `newton(f, df, x0, tol)` | Newton-Raphson（需导数） |
| `secant(f, x0, x1, tol)` | 割线法（无需导数） |

### `Differentiation` — 数值微分

| 方法 | 说明 |
|------|------|
| `derivative(f, x)` | 一阶导数（中心差分，自动步长） |
| `derivative(f, x, h)` | 一阶导数（指定步长） |
| `secondDerivative(f, x)` | 二阶导数 |
| `richardson(f, x, order)` | 高精度导数（Richardson 外推，阶数 1-6） |

### 新增分布 — t、卡方、F、二项、Gamma、Beta、对数正态

| 分布 | 工厂方法 | 主要方法 |
|------|---------|---------|
| `TDistribution` | `of(df)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)` |
| `ChiSquaredDistribution` | `of(df)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)` |
| `FDistribution` | `of(df1, df2)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)` |
| `BinomialDistribution` | `of(n, p)` | `pmf(k)`, `cdf(k)`, `mean()`, `variance()` |
| `GammaDistribution` | `of(shape, scale)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)` |
| `BetaDistribution` | `of(alpha, beta)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)` |
| `LogNormalDistribution` | `of(mu, sigma)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)` |

### `SpecialFunctions` — 特殊数学函数

| 方法 | 说明 |
|------|------|
| `gamma(x)` | Gamma 函数（Lanczos 近似） |
| `logGamma(x)` | log-Gamma（大 x 时数值稳定） |
| `beta(a, b)` | Beta 函数 = Γ(a)·Γ(b)/Γ(a+b) |
| `erf(x)` | 误差函数 |
| `erfc(x)` | 互补误差函数 |
| `regularizedBeta(x, a, b)` | 正则化不完全 Beta 函数 I_x(a,b) |
| `regularizedGammaP(a, x)` | 下正则化不完全 Gamma 函数 P(a,x) |

## 线程安全

所有类要么是无状态工具类，要么是不可变值对象，无需同步即可安全并发使用。

## 输入校验

所有公开方法均进行输入校验：
- 拒绝 null 数组和 null 对象
- 拒绝 NaN 和 Infinity 值（抛出 `IllegalArgumentException` 或 `MathException`）
- 检测维度不匹配
- 通过 `Math.*Exact` 检测溢出
- 资源上限：矩阵最大 4096 维、向量最大 100 万维、插值最大 1 万个数据点
