package cloud.opencode.base.feature.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * MetricsFeatureListener 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("MetricsFeatureListener 测试")
class MetricsFeatureListenerTest {

    private MetricsFeatureListener listener;

    @BeforeEach
    void setUp() {
        listener = new MetricsFeatureListener();
    }

    @Nested
    @DisplayName("onFeatureChanged() 测试")
    class OnFeatureChangedTests {

        @Test
        @DisplayName("增加总变更计数")
        void testIncrementsTotalChanges() {
            listener.onFeatureChanged("f1", false, true);
            listener.onFeatureChanged("f2", true, false);

            assertThat(listener.getTotalChanges()).isEqualTo(2);
        }

        @Test
        @DisplayName("启用时增加启用计数")
        void testIncrementsEnableCount() {
            listener.onFeatureChanged("feature", false, true);

            assertThat(listener.getEnableCount("feature")).isEqualTo(1);
        }

        @Test
        @DisplayName("禁用时增加禁用计数")
        void testIncrementsDisableCount() {
            listener.onFeatureChanged("feature", true, false);

            assertThat(listener.getDisableCount("feature")).isEqualTo(1);
        }

        @Test
        @DisplayName("相同状态不计入启用/禁用计数")
        void testSameStateNotCounted() {
            listener.onFeatureChanged("feature", true, true);

            assertThat(listener.getEnableCount("feature")).isZero();
            assertThat(listener.getDisableCount("feature")).isZero();
            assertThat(listener.getTotalChanges()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getTotalChanges() 测试")
    class GetTotalChangesTests {

        @Test
        @DisplayName("初始为0")
        void testInitialZero() {
            assertThat(listener.getTotalChanges()).isZero();
        }
    }

    @Nested
    @DisplayName("getEnableCount() 测试")
    class GetEnableCountTests {

        @Test
        @DisplayName("未使用的功能返回0")
        void testUnusedFeatureReturnsZero() {
            assertThat(listener.getEnableCount("nonexistent")).isZero();
        }

        @Test
        @DisplayName("多次启用累计")
        void testMultipleEnables() {
            listener.onFeatureChanged("feature", false, true);
            listener.onFeatureChanged("feature", false, true);
            listener.onFeatureChanged("feature", false, true);

            assertThat(listener.getEnableCount("feature")).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getDisableCount() 测试")
    class GetDisableCountTests {

        @Test
        @DisplayName("未使用的功能返回0")
        void testUnusedFeatureReturnsZero() {
            assertThat(listener.getDisableCount("nonexistent")).isZero();
        }

        @Test
        @DisplayName("多次禁用累计")
        void testMultipleDisables() {
            listener.onFeatureChanged("feature", true, false);
            listener.onFeatureChanged("feature", true, false);

            assertThat(listener.getDisableCount("feature")).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getAllEnableCounts() 测试")
    class GetAllEnableCountsTests {

        @Test
        @DisplayName("返回所有启用计数")
        void testGetAllEnableCounts() {
            listener.onFeatureChanged("f1", false, true);
            listener.onFeatureChanged("f2", false, true);
            listener.onFeatureChanged("f2", false, true);

            Map<String, Long> counts = listener.getAllEnableCounts();

            assertThat(counts).containsEntry("f1", 1L);
            assertThat(counts).containsEntry("f2", 2L);
        }
    }

    @Nested
    @DisplayName("getAllDisableCounts() 测试")
    class GetAllDisableCountsTests {

        @Test
        @DisplayName("返回所有禁用计数")
        void testGetAllDisableCounts() {
            listener.onFeatureChanged("f1", true, false);
            listener.onFeatureChanged("f2", true, false);

            Map<String, Long> counts = listener.getAllDisableCounts();

            assertThat(counts).containsEntry("f1", 1L);
            assertThat(counts).containsEntry("f2", 1L);
        }
    }

    @Nested
    @DisplayName("reset() 测试")
    class ResetTests {

        @Test
        @DisplayName("重置所有指标")
        void testReset() {
            listener.onFeatureChanged("f1", false, true);
            listener.onFeatureChanged("f2", true, false);

            listener.reset();

            assertThat(listener.getTotalChanges()).isZero();
            assertThat(listener.getEnableCount("f1")).isZero();
            assertThat(listener.getDisableCount("f2")).isZero();
            assertThat(listener.getAllEnableCounts()).isEmpty();
            assertThat(listener.getAllDisableCounts()).isEmpty();
        }
    }
}
