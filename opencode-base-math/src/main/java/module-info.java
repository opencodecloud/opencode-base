/**
 * OpenCode Base Math - Advanced mathematics and statistics library
 * OpenCode 高级数学与统计计算库
 *
 * <p>Provides advanced mathematical functions beyond the basic arithmetic in opencode-base-core,
 * including descriptive statistics, linear algebra, interpolation, numerical integration,
 * combinatorics, probability distributions, and special functions.</p>
 * <p>提供超越 opencode-base-core 基础算术的高级数学功能，
 * 包括描述性统计、线性代数、插值、数值积分、组合数学、概率分布和特殊函数。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
module cloud.opencode.base.math {
    requires transitive cloud.opencode.base.core;

    exports cloud.opencode.base.math;
    exports cloud.opencode.base.math.analysis;
    exports cloud.opencode.base.math.stats;
    exports cloud.opencode.base.math.stats.inference;
    exports cloud.opencode.base.math.linalg;
    exports cloud.opencode.base.math.interpolation;
    exports cloud.opencode.base.math.integration;
    exports cloud.opencode.base.math.combinatorics;
    exports cloud.opencode.base.math.distribution;
    exports cloud.opencode.base.math.special;
    exports cloud.opencode.base.math.exception;
}
