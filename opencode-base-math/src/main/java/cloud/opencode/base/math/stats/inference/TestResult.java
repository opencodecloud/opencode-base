package cloud.opencode.base.math.stats.inference;

/**
 * Immutable record holding the result of a statistical hypothesis test.
 * 不可变记录，保存统计假设检验的结果
 *
 * <p>Contains the test name, test statistic, p-value, and degrees of freedom.
 * Use {@link #isSignificant(double)} to check significance at a given alpha level.</p>
 * <p>包含检验名称、检验统计量、p 值和自由度。
 * 使用 {@link #isSignificant(double)} 在给定显著性水平下检查显著性。</p>
 *
 * @param testName        the name of the test / 检验名称
 * @param statistic       the test statistic value / 检验统计量值
 * @param pValue          the p-value / p 值
 * @param degreesOfFreedom the degrees of freedom (NaN if not applicable) / 自由度（不适用时为 NaN）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public record TestResult(
        String testName,
        double statistic,
        double pValue,
        double degreesOfFreedom
) {

    /**
     * Checks whether the result is statistically significant at the given alpha level.
     * 检查结果在给定显著性水平下是否具有统计显著性
     *
     * @param alpha the significance level (e.g., 0.05) / 显著性水平（例如 0.05）
     * @return true if pValue &lt; alpha / 如果 p 值小于 alpha 则返回 true
     */
    public boolean isSignificant(double alpha) {
        return pValue < alpha;
    }

    /**
     * Checks whether the result is statistically significant at the default alpha = 0.05.
     * 检查结果在默认显著性水平 alpha = 0.05 下是否具有统计显著性
     *
     * @return true if pValue &lt; 0.05 / 如果 p 值小于 0.05 则返回 true
     */
    public boolean isSignificant() {
        return isSignificant(0.05);
    }
}
