package cloud.opencode.base.money.calc;

import cloud.opencode.base.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Allocation Util
 * 分摊工具类
 *
 * <p>Utility for allocating money amounts using various strategies.</p>
 * <p>使用各种策略分摊金额的工具类。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Money total = Money.of("100");
 *
 * // Allocate by ratios
 * List<Money> parts = AllocationUtil.allocate(total, 1, 2, 3);
 * // [¥16.67, ¥33.33, ¥50.00]
 *
 * // Split evenly
 * List<Money> split = AllocationUtil.split(total, 3);
 * // [¥33.33, ¥33.33, ¥33.34]
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Allocate money by ratios with remainder handling - 按比例分摊金额（含余数处理）</li>
 *   <li>Split money evenly into N parts - 均分金额为N份</li>
 *   <li>Percentage-based allocation - 基于百分比的分摊</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No, total and ratios must not be null - 空值安全: 否，总额和比例不可为null</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is the number of parts/ratios - 时间复杂度: O(n)，n 为份数/比例数量</li>
 *   <li>Space complexity: O(n) for result list - 空间复杂度: O(n) 结果列表</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public final class AllocationUtil {

    private AllocationUtil() {
        // Utility class
    }

    /**
     * Allocate money by ratios
     * 按比例分摊金额
     *
     * <p>The remainder is assigned to the last part to ensure total sum equals original amount.</p>
     * <p>余数分配给最后一份，确保总和等于原始金额。</p>
     *
     * @param total the total money | 总金额
     * @param ratios the ratios | 比例
     * @return the allocated parts | 分摊后的各部分
     */
    public static List<Money> allocate(Money total, int... ratios) {
        Objects.requireNonNull(total, "Total cannot be null");
        if (ratios == null || ratios.length == 0) {
            throw new IllegalArgumentException("Ratios cannot be empty");
        }
        for (int ratio : ratios) {
            if (ratio < 0) {
                throw new IllegalArgumentException("Ratio cannot be negative");
            }
        }

        long sum = 0;
        for (int ratio : ratios) {
            sum += ratio;
        }
        if (sum <= 0) {
            throw new IllegalArgumentException("Sum of ratios must be positive");
        }

        BigDecimal[] amounts = new BigDecimal[ratios.length];
        BigDecimal remainder = total.amount();
        int scale = total.currency().getScale();

        // Calculate each part (except the last)
        for (int i = 0; i < ratios.length - 1; i++) {
            amounts[i] = total.amount()
                .multiply(BigDecimal.valueOf(ratios[i]))
                .divide(BigDecimal.valueOf(sum), scale, RoundingMode.DOWN);

            remainder = remainder.subtract(amounts[i]);
        }

        // Assign remainder to the last part
        amounts[ratios.length - 1] = remainder;

        List<Money> result = new ArrayList<>(amounts.length);
        for (BigDecimal amount : amounts) {
            result.add(Money.of(amount, total.currency()));
        }
        return result;
    }

    /**
     * Allocate money by percentages
     * 按百分比分摊金额
     *
     * @param total the total money | 总金额
     * @param percentages the percentages (must sum to 100) | 百分比（必须加起来等于100）
     * @return the allocated parts | 分摊后的各部分
     */
    public static List<Money> allocateByPercent(Money total, int... percentages) {
        Objects.requireNonNull(total, "Total cannot be null");
        if (percentages == null || percentages.length == 0) {
            throw new IllegalArgumentException("Percentages cannot be empty");
        }

        int sum = 0;
        for (int p : percentages) {
            sum += p;
        }
        if (sum != 100) {
            throw new IllegalArgumentException("Percentages must sum to 100, got: " + sum);
        }

        return allocate(total, percentages);
    }

    /**
     * Split money evenly
     * 平均分摊金额
     *
     * @param total the total money | 总金额
     * @param parts the number of parts | 份数
     * @return the split parts | 分摊后的各部分
     */
    public static List<Money> split(Money total, int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("Parts must be positive");
        }
        if (parts > 10_000) {
            throw new IllegalArgumentException("Parts must not exceed 10000: " + parts);
        }
        int[] ratios = new int[parts];
        Arrays.fill(ratios, 1);
        return allocate(total, ratios);
    }

    /**
     * Allocate with weights
     * 按权重分摊
     *
     * @param total the total money | 总金额
     * @param weights the weights | 权重
     * @return the allocated parts | 分摊后的各部分
     */
    public static List<Money> allocateByWeights(Money total, BigDecimal... weights) {
        Objects.requireNonNull(total, "Total cannot be null");
        if (weights == null || weights.length == 0) {
            throw new IllegalArgumentException("Weights cannot be empty");
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal weight : weights) {
            if (weight == null || weight.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Weight cannot be null or negative");
            }
            sum = sum.add(weight);
        }

        if (sum.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Sum of weights cannot be zero");
        }

        BigDecimal[] amounts = new BigDecimal[weights.length];
        BigDecimal remainder = total.amount();
        int scale = total.currency().getScale();

        for (int i = 0; i < weights.length - 1; i++) {
            amounts[i] = total.amount()
                .multiply(weights[i])
                .divide(sum, scale, RoundingMode.DOWN);
            remainder = remainder.subtract(amounts[i]);
        }
        amounts[weights.length - 1] = remainder;

        List<Money> result = new ArrayList<>(amounts.length);
        for (BigDecimal amount : amounts) {
            result.add(Money.of(amount, total.currency()));
        }
        return result;
    }

    /**
     * Allocate with minimum amount per part
     * 带最小金额的分摊
     *
     * @param total the total money | 总金额
     * @param parts the number of parts | 份数
     * @param minPerPart the minimum per part | 每份最小金额
     * @return the allocated parts | 分摊后的各部分
     */
    public static List<Money> splitWithMinimum(Money total, int parts, Money minPerPart) {
        Objects.requireNonNull(total, "Total cannot be null");
        Objects.requireNonNull(minPerPart, "Minimum per part cannot be null");
        if (parts <= 0) {
            throw new IllegalArgumentException("Parts must be positive");
        }
        if (parts > 10_000) {
            throw new IllegalArgumentException("Parts must not exceed 10000: " + parts);
        }

        Money totalMinimum = minPerPart.multiply(parts);
        if (total.isLessThan(totalMinimum)) {
            throw new IllegalArgumentException(
                "Total amount is less than minimum required: " + totalMinimum);
        }

        // First, give everyone the minimum
        List<Money> result = new ArrayList<>(parts);
        Money remaining = total.subtract(totalMinimum);

        // Then split the remainder evenly
        List<Money> remainderParts = split(remaining, parts);

        for (int i = 0; i < parts; i++) {
            result.add(minPerPart.add(remainderParts.get(i)));
        }

        return result;
    }

    /**
     * Round-robin allocation (distribute cents one by one)
     * 轮询分摊（逐分分配）
     *
     * @param total the total money | 总金额
     * @param parts the number of parts | 份数
     * @return the allocated parts | 分摊后的各部分
     */
    public static List<Money> splitRoundRobin(Money total, int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("Parts must be positive");
        }
        if (parts > 10_000) {
            throw new IllegalArgumentException("Parts must not exceed 10000: " + parts);
        }

        int scale = total.currency().getScale();
        BigDecimal divisor = BigDecimal.valueOf(parts);
        BigDecimal base = total.amount().divide(divisor, scale, RoundingMode.DOWN);

        // Calculate remainder in cents
        BigDecimal baseTotal = base.multiply(divisor);
        BigDecimal remainder = total.amount().subtract(baseTotal);
        int remainderCents = remainder.movePointRight(scale).intValueExact();

        List<Money> result = new ArrayList<>(parts);
        BigDecimal centValue = BigDecimal.ONE.movePointLeft(scale);

        for (int i = 0; i < parts; i++) {
            BigDecimal amount = base;
            if (i < remainderCents) {
                amount = amount.add(centValue);
            }
            result.add(Money.of(amount, total.currency()));
        }

        return result;
    }

    /**
     * Verify allocation sums to total
     * 验证分摊总和等于原总额
     *
     * @param total the expected total | 期望的总额
     * @param parts the allocated parts | 分摊后的各部分
     * @return true if sum equals total | 如果总和等于总额返回true
     */
    public static boolean verify(Money total, List<Money> parts) {
        Money sum = MoneyCalcUtil.sum(parts);
        return sum.amount().compareTo(total.amount()) == 0;
    }
}
