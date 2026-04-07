package cloud.opencode.base.observability.metric;

import java.util.function.Supplier;

/**
 * DefaultGauge - Gauge implementation backed by a Supplier
 * DefaultGauge - 由 Supplier 支持的仪表盘实现
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
final class DefaultGauge implements Gauge {

    private final MetricId id;
    private final Supplier<Double> supplier;

    DefaultGauge(MetricId id, Supplier<Double> supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    @Override
    public double value() {
        Double val = supplier.get();
        if (val == null) {
            return 0.0;
        }
        return val;
    }

    @Override
    public MetricId id() {
        return id;
    }
}
