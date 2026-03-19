package cloud.opencode.base.pdf.signature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * TimestampInfo 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("TimestampInfo 测试")
class TimestampInfoTest {

    @Nested
    @DisplayName("Record 构造测试")
    class RecordConstructorTests {

        @Test
        @DisplayName("创建完整的时间戳信息")
        void testFullConstruction() {
            Instant time = Instant.now();
            TimestampInfo info = new TimestampInfo(time, null, "SHA-256", "12345", true);

            assertThat(info.time()).isEqualTo(time);
            assertThat(info.tsaCertificate()).isNull();
            assertThat(info.hashAlgorithm()).isEqualTo("SHA-256");
            assertThat(info.serialNumber()).isEqualTo("12345");
            assertThat(info.isValid()).isTrue();
        }

        @Test
        @DisplayName("time 不能为 null")
        void testTimeCannotBeNull() {
            assertThatThrownBy(() -> new TimestampInfo(null, null, "SHA-256", "123", true))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("time cannot be null");
        }

        @Test
        @DisplayName("其他字段可以为 null")
        void testOtherFieldsCanBeNull() {
            Instant time = Instant.now();
            TimestampInfo info = new TimestampInfo(time, null, null, null, false);

            assertThat(info.time()).isEqualTo(time);
            assertThat(info.tsaCertificate()).isNull();
            assertThat(info.hashAlgorithm()).isNull();
            assertThat(info.serialNumber()).isNull();
            assertThat(info.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of 创建完整时间戳信息")
        void testOfWithAllParams() {
            Instant time = Instant.parse("2024-01-15T10:30:00Z");
            TimestampInfo info = TimestampInfo.of(time, null, "SHA-512", "SN-001");

            assertThat(info.time()).isEqualTo(time);
            assertThat(info.hashAlgorithm()).isEqualTo("SHA-512");
            assertThat(info.serialNumber()).isEqualTo("SN-001");
            assertThat(info.isValid()).isTrue();
        }

        @Test
        @DisplayName("of 仅时间参数")
        void testOfWithTimeOnly() {
            Instant time = Instant.now();
            TimestampInfo info = TimestampInfo.of(time);

            assertThat(info.time()).isEqualTo(time);
            assertThat(info.tsaCertificate()).isNull();
            assertThat(info.hashAlgorithm()).isNull();
            assertThat(info.serialNumber()).isNull();
            assertThat(info.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("getTsaName 方法测试")
    class GetTsaNameTests {

        @Test
        @DisplayName("无证书时返回 null")
        void testNoTsaCertificate() {
            TimestampInfo info = TimestampInfo.of(Instant.now());

            assertThat(info.getTsaName()).isNull();
        }
    }

    @Nested
    @DisplayName("Record 方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("equals 方法正确比较")
        void testEquals() {
            Instant time = Instant.parse("2024-06-01T12:00:00Z");
            TimestampInfo info1 = new TimestampInfo(time, null, "SHA-256", "123", true);
            TimestampInfo info2 = new TimestampInfo(time, null, "SHA-256", "123", true);
            TimestampInfo info3 = new TimestampInfo(time, null, "SHA-512", "123", true);

            assertThat(info1).isEqualTo(info2);
            assertThat(info1).isNotEqualTo(info3);
        }

        @Test
        @DisplayName("hashCode 一致性")
        void testHashCode() {
            Instant time = Instant.parse("2024-06-01T12:00:00Z");
            TimestampInfo info1 = new TimestampInfo(time, null, "SHA-256", "123", true);
            TimestampInfo info2 = new TimestampInfo(time, null, "SHA-256", "123", true);

            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("toString 包含所有字段")
        void testToString() {
            Instant time = Instant.parse("2024-06-01T12:00:00Z");
            TimestampInfo info = new TimestampInfo(time, null, "SHA-256", "123", true);

            String str = info.toString();
            assertThat(str).contains("TimestampInfo");
            assertThat(str).contains("SHA-256");
            assertThat(str).contains("123");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("非常早的时间")
        void testVeryEarlyTime() {
            Instant time = Instant.parse("1970-01-01T00:00:00Z");
            TimestampInfo info = TimestampInfo.of(time);

            assertThat(info.time()).isEqualTo(time);
        }

        @Test
        @DisplayName("非常晚的时间")
        void testVeryLateTime() {
            Instant time = Instant.parse("2099-12-31T23:59:59Z");
            TimestampInfo info = TimestampInfo.of(time);

            assertThat(info.time()).isEqualTo(time);
        }

        @Test
        @DisplayName("空哈希算法")
        void testEmptyHashAlgorithm() {
            Instant time = Instant.now();
            TimestampInfo info = new TimestampInfo(time, null, "", "123", true);

            assertThat(info.hashAlgorithm()).isEmpty();
        }

        @Test
        @DisplayName("空序列号")
        void testEmptySerialNumber() {
            Instant time = Instant.now();
            TimestampInfo info = new TimestampInfo(time, null, "SHA-256", "", true);

            assertThat(info.serialNumber()).isEmpty();
        }
    }
}
