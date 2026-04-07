/**
 * OpenCode Base Neural Module
 * OpenCode 神经网络前向推理模块
 *
 * <p>Provides a pure-Java neural network forward inference engine
 * with support for CNN, LSTM, and CTC decoding.</p>
 * <p>提供纯 Java 神经网络前向推理引擎，
 * 支持 CNN、LSTM 和 CTC 解码。</p>
 *
 * @since JDK 25, opencode-base-neural V1.0.0
 */
module cloud.opencode.base.neural {

    // Required modules
    requires transitive cloud.opencode.base.core;
    requires java.desktop;

    // Export public API packages (added incrementally as packages get populated)
    exports cloud.opencode.base.neural.exception;

    // These exports will be uncommented as Sprint 1-3 populate the packages:
    exports cloud.opencode.base.neural.tensor;
    exports cloud.opencode.base.neural.op;
    exports cloud.opencode.base.neural.model;
    exports cloud.opencode.base.neural.session;
    exports cloud.opencode.base.neural.loss;
    exports cloud.opencode.base.neural.init;
    exports cloud.opencode.base.neural.norm;
    exports cloud.opencode.base.neural.metric;

    // Internal packages - not exported
    // cloud.opencode.base.neural.internal
}
