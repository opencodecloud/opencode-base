# OpenCode Base Math

**Advanced mathematics and statistics library for JDK 25+**

A pure-Java, zero-dependency math library providing statistics, linear algebra, interpolation, numerical integration, combinatorics, probability distributions, and special functions. Complements the basic arithmetic in `opencode-base-core`.

## Quick Start

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

## Features

### Statistics (`math.stats`)
- **Descriptive**: percentile, mode, skewness, kurtosis, range, IQR
- **Correlation**: covariance, Pearson, Spearman rank, Kendall tau-b (O(n log n) merge-sort algorithm)
- **Means**: weighted, geometric, harmonic
- **Regression**: simple linear regression with R-squared, predictions, residuals
- **Percentile**: configurable interpolation (LINEAR, LOWER, HIGHER, NEAREST, MIDPOINT)
- **Streaming**: online mean/variance/stddev/min/max via Welford's algorithm, mergeable for parallel use

### Hypothesis Testing (`math.stats.inference`)
- **T-Test**: one-sample, Welch's two-sample, paired t-test
- **Chi-Square**: goodness-of-fit, independence test
- **ANOVA**: one-way F-test
- **TestResult**: statistic, p-value, degrees of freedom, significance check

### Linear Algebra (`math.linalg`)
- **Vector**: dot/cross product, normalize, angle, distance, arithmetic operations
- **Matrix**: multiply, transpose, determinant (LU decomposition), inverse (LU decomposition), trace

### Interpolation (`math.interpolation`)
- Piecewise linear interpolation (O(log n) binary search)
- Lagrange polynomial interpolation (barycentric form, O(n) per evaluation)
- Newton's divided difference interpolation
- Natural cubic spline interpolation (with precompute/evaluate split for multi-point queries)

### Numerical Integration (`math.integration`)
- Composite trapezoidal rule
- Simpson's 1/3 and 3/8 rules
- Romberg integration (Richardson extrapolation)
- Gauss-Legendre quadrature (2-5 points)

### Combinatorics (`math.combinatorics`)
- Binomial coefficients (long + BigInteger)
- Permutations, Catalan numbers, Stirling numbers of the second kind
- Bell numbers, derangements (subfactorial)

### Numerical Analysis (`math.analysis`)
- **Root Finding**: bisection, Brent's method, Newton-Raphson, secant method
- **Differentiation**: central difference, second derivative, Richardson extrapolation

### Probability Distributions (`math.distribution`)
- **Continuous**: Normal, Student's t, Chi-squared, F, Gamma, Beta, LogNormal
- **Discrete**: Binomial, Poisson
- **Utilities**: Exponential PDF/CDF, Uniform PDF
- All with: PDF/PMF, CDF, inverse CDF, parameter accessors

### Special Functions (`math.special`)
- Gamma and log-Gamma (Lanczos approximation)
- Beta function
- Error function (erf, erfc)
- Regularized incomplete Beta and Gamma functions

## Usage Examples

### Statistics

```java
import cloud.opencode.base.math.stats.Statistics;
import cloud.opencode.base.math.stats.Regression;
import cloud.opencode.base.math.stats.Percentile;

double[] data = {2.0, 4.0, 6.0, 8.0, 10.0};

// Descriptive statistics
double p90 = Statistics.percentile(data, 90);       // 90th percentile
double[] modes = Statistics.mode(data);              // most frequent values
double skew = Statistics.skewness(data);             // sample skewness
double kurt = Statistics.kurtosis(data);             // excess kurtosis
double iqr = Statistics.interquartileRange(data);    // Q3 - Q1

// Correlation
double[] x = {1, 2, 3, 4, 5};
double[] y = {2, 4, 5, 4, 5};
double cov = Statistics.covariance(x, y);            // sample covariance
double r = Statistics.correlation(x, y);             // Pearson r in [-1, 1]

// Weighted / geometric / harmonic mean
double wm = Statistics.weightedMean(data, new double[]{1, 2, 3, 2, 1});
double gm = Statistics.geometricMean(data);
double hm = Statistics.harmonicMean(data);

// Linear regression
Regression.LinearModel model = Regression.linear(x, y);
double slope = model.slope();
double intercept = model.intercept();
double rSquared = model.rSquared();
double predicted = model.predict(6.0);
double[] residuals = model.residuals(x, y);

// Configurable percentile
Percentile pctl = Percentile.of(data);
double median = pctl.quartile(2);                    // Q2 = median
double q1 = pctl.value(25, Percentile.Method.LOWER);
```

### Linear Algebra

```java
import cloud.opencode.base.math.linalg.Vector;
import cloud.opencode.base.math.linalg.Matrix;

// Vector operations
Vector v1 = Vector.of(1.0, 2.0, 3.0);
Vector v2 = Vector.of(4.0, 5.0, 6.0);
double dot = v1.dot(v2);                       // 32.0
Vector cross = v1.cross(v2);                   // [-3, 6, -3]
double angle = v1.angle(v2);                   // angle in radians
double dist = v1.distanceTo(v2);               // Euclidean distance
Vector unit = v1.normalize();                  // unit vector

// Matrix operations
Matrix a = Matrix.of(new double[][]{
    {1, 2},
    {3, 4}
});
Matrix b = Matrix.identity(2);
Matrix product = a.multiply(b);                // matrix multiplication
Matrix transposed = a.transpose();
double det = a.determinant();                  // -2.0
Matrix inv = a.inverse();                      // inverse matrix
double tr = a.trace();                         // 5.0

// Matrix-vector multiplication
Vector v = Vector.of(1.0, 2.0);
Vector result = a.multiplyVector(v);           // [5, 11]
```

### Interpolation

```java
import cloud.opencode.base.math.interpolation.Interpolation;

double[] x = {0, 1, 2, 3, 4};
double[] y = {0, 1, 4, 9, 16};    // y = x^2

// Piecewise linear interpolation (O(log n) lookup)
double v1 = Interpolation.linear(x, y, 1.5);          // 2.5

// Lagrange polynomial (exact for degree <= n-1)
double v2 = Interpolation.lagrange(x, y, 1.5);        // 2.25 (exact x^2)

// Newton's divided difference
double v3 = Interpolation.newtonDividedDifference(x, y, 1.5);

// Natural cubic spline (smooth, C2 continuous)
double v4 = Interpolation.cubicSpline(x, y, 1.5);

// Precomputed spline for efficient multi-point evaluation
var spline = Interpolation.precomputeSpline(x, y);
double a = spline.evaluate(0.5);   // precompute once, evaluate many times
double b = spline.evaluate(2.5);   // O(log n) per query, no redundant O(n) setup
```

### Numerical Integration

```java
import cloud.opencode.base.math.integration.NumericalIntegration;

// Integrate sin(x) from 0 to PI = 2.0
double trap = NumericalIntegration.trapezoid(Math::sin, 0, Math.PI, 100);
double simp = NumericalIntegration.simpson(Math::sin, 0, Math.PI, 100);

// High-accuracy Romberg integration
double romb = NumericalIntegration.romberg(Math::sin, 0, Math.PI, 10, 1e-12);

// Gauss-Legendre quadrature (2-5 points)
double gl = NumericalIntegration.gaussLegendre(Math::sin, 0, Math.PI, 5);

// Custom function: integrate e^x from 0 to 1 = e - 1
double ex = NumericalIntegration.simpson(Math::exp, 0, 1, 100);
```

### Combinatorics

```java
import cloud.opencode.base.math.combinatorics.Combinatorics;

long c = Combinatorics.binomial(20, 10);        // 184756
long p = Combinatorics.permutation(10, 3);      // 720
long cat = Combinatorics.catalanNumber(10);     // 16796
long stir = Combinatorics.stirlingSecond(5, 3); // 25
long bell = Combinatorics.bellNumber(5);        // 52
long der = Combinatorics.derangements(5);       // 44

// Arbitrary precision for large values
java.math.BigInteger big = Combinatorics.binomialBig(100, 50);
```

### Hypothesis Testing

```java
import cloud.opencode.base.math.stats.inference.*;

// One-sample t-test: is the mean equal to 5?
double[] data = {5.2, 5.8, 6.1, 4.9, 5.5};
TestResult r1 = TTest.oneSample(data, 5.0);
System.out.println(r1.pValue());           // p-value
System.out.println(r1.isSignificant());    // true if p < 0.05

// Welch's two-sample t-test
double[] groupA = {23, 25, 28, 30, 32};
double[] groupB = {18, 20, 22, 24, 26};
TestResult r2 = TTest.twoSample(groupA, groupB);

// Chi-square goodness-of-fit
TestResult r3 = ChiSquareTest.goodnessOfFit(
    new double[]{10, 20, 30},    // observed
    new double[]{20, 20, 20});   // expected

// One-way ANOVA
TestResult r4 = AnovaTest.oneWay(groupA, groupB, new double[]{15, 17, 19, 21, 23});
```

### Streaming Statistics

```java
import cloud.opencode.base.math.stats.StreamingStatistics;

// Online computation — no need to store all values
StreamingStatistics ss = StreamingStatistics.create();
for (double value : dataStream) {
    ss.add(value);
}
double mean = ss.mean();
double stddev = ss.sampleStdDev();
long n = ss.count();

// Parallel merge
StreamingStatistics part1 = StreamingStatistics.create();
StreamingStatistics part2 = StreamingStatistics.create();
// ... add values to each ...
StreamingStatistics merged = part1.merge(part2);

// Stream collector
StreamingStatistics result = values.stream()
    .collect(StreamingStatistics.collector());
```

### Probability Distributions

```java
import cloud.opencode.base.math.distribution.*;

// Normal distribution
NormalDistribution std = NormalDistribution.STANDARD;
double cdf = std.cdf(1.96);            // 0.975...
double z = std.inverseCdf(0.975);       // 1.96...

// Student's t-distribution
TDistribution t10 = TDistribution.of(10);
double p = t10.cdf(1.812);             // ≈ 0.95

// Chi-squared distribution
ChiSquaredDistribution chi5 = ChiSquaredDistribution.of(5);
double critical = chi5.inverseCdf(0.95); // ≈ 11.07

// F-distribution
FDistribution f = FDistribution.of(5, 10);
double fCdf = f.cdf(3.326);            // ≈ 0.95

// Binomial distribution
BinomialDistribution binom = BinomialDistribution.of(10, 0.5);
double pmf = binom.pmf(5);             // ≈ 0.2461

// Gamma, Beta, LogNormal
GammaDistribution gamma = GammaDistribution.of(2, 1);
BetaDistribution beta = BetaDistribution.of(2, 5);
LogNormalDistribution logn = LogNormalDistribution.of(0, 1);
```

### Root Finding & Differentiation

```java
import cloud.opencode.base.math.analysis.RootFinder;
import cloud.opencode.base.math.analysis.Differentiation;

// Find √2 by solving x² - 2 = 0
double sqrt2 = RootFinder.brent(x -> x * x - 2, 0, 2, 1e-12);

// Newton's method (with analytical derivative)
double root = RootFinder.newton(x -> x * x - 2, x -> 2 * x, 1.0, 1e-12);

// Numerical derivative of sin(x) at π/4
double d = Differentiation.derivative(Math::sin, Math.PI / 4);  // ≈ cos(π/4)

// High-accuracy Richardson extrapolation
double dRich = Differentiation.richardson(Math::sin, Math.PI / 4, 4);
```

### Facade — One-Stop Import

```java
import cloud.opencode.base.math.OpenMathLib;

// Everything through a single class
double sqrt2 = OpenMathLib.findRoot(x -> x * x - 2, 0, 2);
double area = OpenMathLib.integrate(Math::sin, 0, Math.PI);
double p90 = OpenMathLib.percentile(data, 90);
long c = OpenMathLib.binomial(20, 10);
double g = OpenMathLib.gamma(5.0);
```

### Special Functions

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

## API Reference

### `OpenMathLib` — Unified Facade

| Method | Description |
|--------|-------------|
| `percentile(data, p)` | p-th percentile |
| `correlation(x, y)` / `spearmanCorrelation(x, y)` | Pearson / Spearman correlation |
| `covariance(x, y)` | Sample covariance |
| `linearRegression(x, y)` | Simple linear regression |
| `streamingStats()` | Create streaming accumulator |
| `tTestOneSample(data, mu0)` / `tTestTwoSample(x, y)` | T-tests |
| `vector(...)` / `matrix(data)` / `identityMatrix(n)` | Linear algebra creation |
| `findRoot(f, a, b)` | Brent's root finding |
| `derivative(f, x)` | Numerical derivative |
| `integrate(f, a, b)` | Simpson's integration (n=1000) |
| `interpolate(x, y, xi)` | Linear interpolation |
| `binomial(n, k)` / `permutation(n, k)` | Combinatorics |
| `standardNormal()` | N(0,1) distribution |
| `gamma(x)` / `erf(x)` / `beta(a, b)` | Special functions |

### `Statistics` — Descriptive Statistics

| Method | Description |
|--------|-------------|
| `percentile(double[] data, double p)` | p-th percentile (p in [0,100]), linear interpolation |
| `mode(double[] data)` | All mode values (most frequent), sorted |
| `skewness(double[] data)` | Sample skewness (Fisher's definition) |
| `kurtosis(double[] data)` | Sample excess kurtosis |
| `covariance(double[] x, double[] y)` | Sample covariance |
| `correlation(double[] x, double[] y)` | Pearson correlation coefficient [-1, 1] |
| `spearmanCorrelation(double[] x, double[] y)` | Spearman rank correlation [-1, 1] |
| `kendallCorrelation(double[] x, double[] y)` | Kendall tau-b correlation [-1, 1] |
| `weightedMean(double[] values, double[] weights)` | Weighted arithmetic mean |
| `geometricMean(double[] values)` | Geometric mean (values must be > 0) |
| `harmonicMean(double[] values)` | Harmonic mean (values must be > 0) |
| `range(double[] data)` | max - min |
| `interquartileRange(double[] data)` | Q3 - Q1 |

### `Regression` — Linear Regression

| Method | Description |
|--------|-------------|
| `linear(double[] x, double[] y)` | Simple linear regression, returns `LinearModel` |
| `LinearModel.slope()` | Slope coefficient |
| `LinearModel.intercept()` | Y-intercept |
| `LinearModel.rSquared()` | Coefficient of determination [0, 1] |
| `LinearModel.predict(double x)` | Predict y for given x |
| `LinearModel.residuals(double[] x, double[] y)` | Array of residuals (observed - predicted) |

### `Percentile` — Configurable Percentile Calculator

| Method | Description |
|--------|-------------|
| `of(double[] data)` | Create from data (sorted internally) |
| `value(double p)` | p-th percentile using LINEAR method |
| `value(double p, Method method)` | p-th percentile with specified interpolation |
| `quartile(int q)` | Q1 (q=1), Q2/median (q=2), Q3 (q=3) |

Methods: `LINEAR`, `LOWER`, `HIGHER`, `NEAREST`, `MIDPOINT`

### `Vector` — Immutable Vector

| Method | Description |
|--------|-------------|
| `of(double... components)` | Create vector from components |
| `zero(int dimension)` | Zero vector of given dimension |
| `unit(int dimension, int index)` | Unit vector (1 at index, 0 elsewhere) |
| `add(Vector)` / `subtract(Vector)` | Element-wise arithmetic |
| `scale(double)` / `negate()` | Scalar operations |
| `dot(Vector)` | Dot product |
| `cross(Vector)` | Cross product (3D only) |
| `magnitude()` | Euclidean norm |
| `normalize()` | Unit vector in same direction |
| `angle(Vector)` | Angle in radians [0, PI] |
| `distanceTo(Vector)` | Euclidean distance |

### `Matrix` — Immutable Matrix

| Method | Description |
|--------|-------------|
| `of(double[][])` | Create from 2D array (defensive copy) |
| `identity(int n)` | n x n identity matrix |
| `zero(int rows, int cols)` | Zero matrix |
| `add(Matrix)` / `subtract(Matrix)` | Element-wise arithmetic |
| `multiply(Matrix)` | Matrix multiplication |
| `multiplyVector(Vector)` | Matrix-vector multiplication |
| `scalarMultiply(double)` | Scalar multiplication |
| `transpose()` | Transpose |
| `determinant()` | Determinant (LU decomposition) |
| `inverse()` | Inverse (LU decomposition with partial pivoting) |
| `trace()` | Sum of diagonal elements |
| `isSquare()` / `isSymmetric()` | Property checks |

### `Interpolation` — Interpolation Methods

| Method | Description |
|--------|-------------|
| `linear(x[], y[], xi)` | Piecewise linear, O(log n) per query |
| `lagrange(x[], y[], xi)` | Lagrange polynomial (barycentric form), O(n) per eval |
| `newtonDividedDifference(x[], y[], xi)` | Newton form, equivalent to Lagrange |
| `precomputeSpline(x[], y[])` | Precompute spline coefficients, returns `SplineCoefficients` |
| `cubicSpline(x[], y[], xi)` | Natural cubic spline, C2 smooth (convenience single-point) |

### `NumericalIntegration` — Numerical Quadrature

| Method | Description |
|--------|-------------|
| `trapezoid(f, a, b, n)` | Composite trapezoidal rule |
| `simpson(f, a, b, n)` | Simpson's 1/3 rule (n must be even) |
| `simpsonThreeEighths(f, a, b, n)` | Simpson's 3/8 rule (n divisible by 3) |
| `romberg(f, a, b, maxIter, tol)` | Romberg with Richardson extrapolation |
| `gaussLegendre(f, a, b, points)` | Gauss-Legendre quadrature (2-5 points) |

### `Combinatorics` — Combinatorial Functions

| Method | Description |
|--------|-------------|
| `binomial(n, k)` / `binomialBig(n, k)` | C(n,k) binomial coefficient |
| `permutation(n, k)` / `permutationBig(n, k)` | P(n,k) permutation |
| `catalanNumber(n)` / `catalanBig(n)` | n-th Catalan number |
| `stirlingSecond(n, k)` | Stirling number of the second kind S(n,k) |
| `bellNumber(n)` | n-th Bell number |
| `derangements(n)` | Subfactorial !n |

### `NormalDistribution` — Normal (Gaussian) Distribution

| Method | Description |
|--------|-------------|
| `STANDARD` | Standard normal N(0, 1) singleton |
| `of(mean, stdDev)` | Create custom N(mean, stdDev) |
| `pdf(x)` | Probability density function |
| `cdf(x)` | Cumulative distribution function |
| `inverseCdf(p)` | Quantile function (p in (0,1)) |
| `sample()` / `sample(n)` | Random sampling (Box-Muller) |

### `Distributions` — Distribution Utilities

| Method | Description |
|--------|-------------|
| `uniform(min, max)` | Uniform PDF as `DoubleUnaryOperator` |
| `exponentialPdf(lambda, x)` | Exponential PDF |
| `exponentialCdf(lambda, x)` | Exponential CDF |
| `poissonPmf(lambda, k)` | Poisson probability mass function |
| `poissonCdf(lambda, k)` | Poisson CDF: P(X <= k) |

### `StreamingStatistics` — Online Statistics Accumulator

| Method | Description |
|--------|-------------|
| `create()` | Create new empty accumulator |
| `add(double value)` | Add a value |
| `count()` / `mean()` / `sum()` | Basic statistics |
| `variance()` / `stdDev()` | Population variance / standard deviation |
| `sampleVariance()` / `sampleStdDev()` | Sample variance / standard deviation (n-1) |
| `min()` / `max()` | Extremes |
| `merge(StreamingStatistics)` | Merge two accumulators (returns new instance) |
| `reset()` | Clear all state |
| `collector()` | `Collector<Double, ?, StreamingStatistics>` for streams |

### `TTest` / `ChiSquareTest` / `AnovaTest` — Hypothesis Testing

| Method | Description |
|--------|-------------|
| `TTest.oneSample(data, mu0)` | One-sample t-test (H0: mean = mu0) |
| `TTest.twoSample(x, y)` | Welch's two-sample t-test |
| `TTest.paired(x, y)` | Paired t-test |
| `ChiSquareTest.goodnessOfFit(obs, exp)` | Chi-square goodness-of-fit |
| `ChiSquareTest.independence(table)` | Chi-square independence test |
| `AnovaTest.oneWay(groups...)` | One-way ANOVA F-test |

Returns `TestResult(testName, statistic, pValue, degreesOfFreedom)` with `isSignificant(alpha)`.

### `RootFinder` — Equation Solving

| Method | Description |
|--------|-------------|
| `bisection(f, a, b, tol)` | Bisection method (guaranteed convergence) |
| `brent(f, a, b, tol)` | Brent's method (recommended, fast + reliable) |
| `newton(f, df, x0, tol)` | Newton-Raphson (requires derivative) |
| `secant(f, x0, x1, tol)` | Secant method (no derivative needed) |

### `Differentiation` — Numerical Derivatives

| Method | Description |
|--------|-------------|
| `derivative(f, x)` | First derivative (central difference, auto step) |
| `derivative(f, x, h)` | First derivative with explicit step size |
| `secondDerivative(f, x)` | Second derivative (auto step) |
| `secondDerivative(f, x, h)` | Second derivative with explicit step |
| `richardson(f, x, order)` | High-accuracy derivative (Richardson extrapolation, order 1-6) |

### New Distributions — t, Chi-squared, F, Binomial, Gamma, Beta, LogNormal

| Distribution | Factory | Key Methods |
|-------------|---------|-------------|
| `TDistribution` | `of(df)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)` |
| `ChiSquaredDistribution` | `of(df)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)` |
| `FDistribution` | `of(df1, df2)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)` |
| `BinomialDistribution` | `of(n, p)` | `pmf(k)`, `cdf(k)`, `mean()`, `variance()` |
| `GammaDistribution` | `of(shape, scale)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)`, `mean()`, `variance()` |
| `BetaDistribution` | `of(alpha, beta)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)`, `mean()`, `variance()` |
| `LogNormalDistribution` | `of(mu, sigma)` | `pdf(x)`, `cdf(x)`, `inverseCdf(p)`, `mean()`, `variance()` |

### `SpecialFunctions` — Mathematical Special Functions

| Method | Description |
|--------|-------------|
| `gamma(x)` | Gamma function (Lanczos approximation) |
| `logGamma(x)` | Log-Gamma (numerically stable for large x) |
| `beta(a, b)` | Beta function = Gamma(a)*Gamma(b)/Gamma(a+b) |
| `erf(x)` | Error function |
| `erfc(x)` | Complementary error function |
| `regularizedBeta(x, a, b)` | Regularized incomplete beta I_x(a,b) |
| `regularizedGammaP(a, x)` | Lower regularized incomplete gamma P(a,x) |

## Thread Safety

All classes are either stateless utility classes or immutable value objects. They are safe for concurrent use without synchronization.

## Input Validation

All public methods validate inputs:
- Null arrays and null objects are rejected
- NaN and Infinity values are rejected (throws `IllegalArgumentException` or `MathException`)
- Dimension mismatches are detected
- Overflow is detected via `Math.*Exact` where applicable
- Resource bounds enforced: Matrix max 4096, Vector max 1M, Interpolation max 10K points
