package cloud.opencode.base.feature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureSnapshot 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
@DisplayName("FeatureSnapshot 测试")
class FeatureSnapshotTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("正常创建")
        void testNormalConstruction() {
            Feature f = Feature.builder("key").build();
            Instant now = Instant.now();
            FeatureSnapshot snapshot = new FeatureSnapshot(Map.of("key", f), now);

            assertThat(snapshot.features()).hasSize(1);
            assertThat(snapshot.timestamp()).isEqualTo(now);
        }

        @Test
        @DisplayName("null features使用空映射")
        void testNullFeatures() {
            FeatureSnapshot snapshot = new FeatureSnapshot(null, Instant.now());

            assertThat(snapshot.features()).isEmpty();
        }

        @Test
        @DisplayName("null timestamp使用当前时间")
        void testNullTimestamp() {
            Instant before = Instant.now();
            FeatureSnapshot snapshot = new FeatureSnapshot(Map.of(), null);
            Instant after = Instant.now();

            assertThat(snapshot.timestamp()).isBetween(before, after);
        }

        @Test
        @DisplayName("features不可变")
        void testImmutableFeatures() {
            Feature f = Feature.builder("key").build();
            FeatureSnapshot snapshot = new FeatureSnapshot(Map.of("key", f), Instant.now());

            assertThatThrownBy(() -> snapshot.features().put("key2", f))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("size() 测试")
    class SizeTests {

        @Test
        @DisplayName("返回正确大小")
        void testSize() {
            Feature f1 = Feature.builder("k1").build();
            Feature f2 = Feature.builder("k2").build();
            FeatureSnapshot snapshot = new FeatureSnapshot(Map.of("k1", f1, "k2", f2), Instant.now());

            assertThat(snapshot.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("空快照大小为0")
        void testEmptySize() {
            FeatureSnapshot snapshot = new FeatureSnapshot(Map.of(), Instant.now());

            assertThat(snapshot.size()).isZero();
        }
    }

    @Nested
    @DisplayName("contains() 测试")
    class ContainsTests {

        @Test
        @DisplayName("包含已有键")
        void testContains() {
            Feature f = Feature.builder("key").build();
            FeatureSnapshot snapshot = new FeatureSnapshot(Map.of("key", f), Instant.now());

            assertThat(snapshot.contains("key")).isTrue();
            assertThat(snapshot.contains("other")).isFalse();
        }
    }
}
