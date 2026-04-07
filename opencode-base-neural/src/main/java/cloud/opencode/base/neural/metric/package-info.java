/**
 * Neural Network Evaluation Metrics
 * 神经网络评估指标
 *
 * <p>Provides classification and regression metrics for evaluating neural network
 * model performance. Includes accuracy, precision, recall, F1 score, confusion matrix,
 * and common regression metrics (MSE, MAE, RMSE, R-squared).</p>
 * <p>提供用于评估神经网络模型性能的分类和回归指标。包括准确率、精确率、召回率、
 * F1 分数、混淆矩阵以及常用回归指标（MSE、MAE、RMSE、R-squared）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.neural.metric.Accuracy} - Classification accuracy | 分类准确率</li>
 *   <li>{@link cloud.opencode.base.neural.metric.BinaryAccuracy} - Binary classification accuracy | 二分类准确率</li>
 *   <li>{@link cloud.opencode.base.neural.metric.Precision} - Binary precision | 二分类精确率</li>
 *   <li>{@link cloud.opencode.base.neural.metric.Recall} - Binary recall | 二分类召回率</li>
 *   <li>{@link cloud.opencode.base.neural.metric.F1Score} - F1 score | F1 分数</li>
 *   <li>{@link cloud.opencode.base.neural.metric.ConfusionMatrix} - Multi-class confusion matrix | 多分类混淆矩阵</li>
 *   <li>{@link cloud.opencode.base.neural.metric.RegressionMetrics} - Regression metrics | 回归指标</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
package cloud.opencode.base.neural.metric;
