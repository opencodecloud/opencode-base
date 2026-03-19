package cloud.opencode.base.pdf.signature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SignatureInfo 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("SignatureInfo 测试")
class SignatureInfoTest {

    @Nested
    @DisplayName("Record 构造测试")
    class RecordConstructorTests {

        @Test
        @DisplayName("创建完整的签名信息")
        void testFullConstruction() {
            Instant signDate = Instant.now();
            TimestampInfo timestamp = TimestampInfo.of(signDate);

            SignatureInfo info = new SignatureInfo(
                "Signature1",
                "Contract Approval",
                "New York",
                "contact@example.com",
                signDate,
                List.of(),
                timestamp,
                true,
                true,
                1
            );

            assertThat(info.name()).isEqualTo("Signature1");
            assertThat(info.reason()).isEqualTo("Contract Approval");
            assertThat(info.location()).isEqualTo("New York");
            assertThat(info.contactInfo()).isEqualTo("contact@example.com");
            assertThat(info.signDate()).isEqualTo(signDate);
            assertThat(info.certificates()).isEmpty();
            assertThat(info.timestampInfo()).isEqualTo(timestamp);
            assertThat(info.isValid()).isTrue();
            assertThat(info.coversWholeDoc()).isTrue();
            assertThat(info.revisionNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("certificates 为 null 时转为空列表")
        void testNullCertificatesBecomesEmptyList() {
            SignatureInfo info = new SignatureInfo(
                "Sig", null, null, null, null, null, null, false, false, 0
            );

            assertThat(info.certificates()).isEmpty();
            assertThat(info.certificates()).isNotNull();
        }

        @Test
        @DisplayName("certificates 列表是不可变的")
        void testCertificatesListIsImmutable() {
            SignatureInfo info = new SignatureInfo(
                "Sig", null, null, null, null, List.of(), null, false, false, 0
            );

            assertThatThrownBy(() -> info.certificates().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder 创建构建器")
        void testBuilderCreation() {
            SignatureInfo.Builder builder = SignatureInfo.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("完整的链式构建")
        void testFullBuilderChain() {
            Instant signDate = Instant.now();
            TimestampInfo timestamp = TimestampInfo.of(signDate);

            SignatureInfo info = SignatureInfo.builder()
                .name("TestSig")
                .reason("Testing")
                .location("Beijing")
                .contactInfo("test@test.com")
                .signDate(signDate)
                .certificates(List.of())
                .timestampInfo(timestamp)
                .valid(true)
                .coversWholeDoc(true)
                .revisionNumber(2)
                .build();

            assertThat(info.name()).isEqualTo("TestSig");
            assertThat(info.reason()).isEqualTo("Testing");
            assertThat(info.location()).isEqualTo("Beijing");
            assertThat(info.contactInfo()).isEqualTo("test@test.com");
            assertThat(info.signDate()).isEqualTo(signDate);
            assertThat(info.timestampInfo()).isEqualTo(timestamp);
            assertThat(info.isValid()).isTrue();
            assertThat(info.coversWholeDoc()).isTrue();
            assertThat(info.revisionNumber()).isEqualTo(2);
        }

        @Test
        @DisplayName("部分字段构建")
        void testPartialBuild() {
            SignatureInfo info = SignatureInfo.builder()
                .name("PartialSig")
                .valid(false)
                .build();

            assertThat(info.name()).isEqualTo("PartialSig");
            assertThat(info.reason()).isNull();
            assertThat(info.location()).isNull();
            assertThat(info.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("访问方法测试")
    class AccessorTests {

        @Test
        @DisplayName("getSignerCertificate 无证书返回 null")
        void testGetSignerCertificateEmpty() {
            SignatureInfo info = SignatureInfo.builder().build();

            assertThat(info.getSignerCertificate()).isNull();
        }

        @Test
        @DisplayName("getSignerName 无证书返回 null")
        void testGetSignerNameEmpty() {
            SignatureInfo info = SignatureInfo.builder().build();

            assertThat(info.getSignerName()).isNull();
        }

        @Test
        @DisplayName("hasTimestamp 返回 false 当无时间戳")
        void testHasTimestampFalse() {
            SignatureInfo info = SignatureInfo.builder().build();

            assertThat(info.hasTimestamp()).isFalse();
        }

        @Test
        @DisplayName("hasTimestamp 返回 true 当有时间戳")
        void testHasTimestampTrue() {
            TimestampInfo timestamp = TimestampInfo.of(Instant.now());
            SignatureInfo info = SignatureInfo.builder()
                .timestampInfo(timestamp)
                .build();

            assertThat(info.hasTimestamp()).isTrue();
        }
    }

    @Nested
    @DisplayName("Record 方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("equals 方法正确比较")
        void testEquals() {
            Instant time = Instant.parse("2024-01-01T00:00:00Z");
            SignatureInfo info1 = SignatureInfo.builder()
                .name("Sig1")
                .signDate(time)
                .build();
            SignatureInfo info2 = SignatureInfo.builder()
                .name("Sig1")
                .signDate(time)
                .build();
            SignatureInfo info3 = SignatureInfo.builder()
                .name("Sig2")
                .signDate(time)
                .build();

            assertThat(info1).isEqualTo(info2);
            assertThat(info1).isNotEqualTo(info3);
        }

        @Test
        @DisplayName("hashCode 一致性")
        void testHashCode() {
            SignatureInfo info1 = SignatureInfo.builder().name("Test").build();
            SignatureInfo info2 = SignatureInfo.builder().name("Test").build();

            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("toString 包含字段信息")
        void testToString() {
            SignatureInfo info = SignatureInfo.builder()
                .name("MySig")
                .reason("Test Reason")
                .build();

            String str = info.toString();
            assertThat(str).contains("SignatureInfo");
            assertThat(str).contains("MySig");
            assertThat(str).contains("Test Reason");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("所有字段为 null")
        void testAllFieldsNull() {
            SignatureInfo info = SignatureInfo.builder().build();

            assertThat(info.name()).isNull();
            assertThat(info.reason()).isNull();
            assertThat(info.location()).isNull();
            assertThat(info.contactInfo()).isNull();
            assertThat(info.signDate()).isNull();
            assertThat(info.certificates()).isEmpty();
            assertThat(info.timestampInfo()).isNull();
            assertThat(info.isValid()).isFalse();
            assertThat(info.coversWholeDoc()).isFalse();
            assertThat(info.revisionNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("空字符串字段")
        void testEmptyStringFields() {
            SignatureInfo info = SignatureInfo.builder()
                .name("")
                .reason("")
                .location("")
                .contactInfo("")
                .build();

            assertThat(info.name()).isEmpty();
            assertThat(info.reason()).isEmpty();
            assertThat(info.location()).isEmpty();
            assertThat(info.contactInfo()).isEmpty();
        }

        @Test
        @DisplayName("负修订号")
        void testNegativeRevisionNumber() {
            SignatureInfo info = SignatureInfo.builder()
                .revisionNumber(-1)
                .build();

            assertThat(info.revisionNumber()).isEqualTo(-1);
        }
    }
}
