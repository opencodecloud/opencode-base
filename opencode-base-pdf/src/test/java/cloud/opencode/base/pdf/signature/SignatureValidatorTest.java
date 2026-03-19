package cloud.opencode.base.pdf.signature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SignatureValidator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("SignatureValidator 测试")
class SignatureValidatorTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("create 创建验证器")
        void testCreate() {
            SignatureValidator validator = SignatureValidator.create();

            assertThat(validator).isNotNull();
        }
    }

    @Nested
    @DisplayName("信任配置测试")
    class TrustConfigTests {

        @Test
        @DisplayName("trustedCertificates 设置受信任证书列表")
        void testTrustedCertificates() {
            SignatureValidator validator = SignatureValidator.create()
                .trustedCertificates(List.of());

            assertThat(validator.getTrustedCertificates()).isEmpty();
        }

        @Test
        @DisplayName("trustedCertificates null 清空列表")
        void testTrustedCertificatesNull() {
            SignatureValidator validator = SignatureValidator.create()
                .trustedCertificates(null);

            assertThat(validator.getTrustedCertificates()).isEmpty();
        }

        @Test
        @DisplayName("addTrustedCertificate null 抛出异常")
        void testAddTrustedCertificateNull() {
            assertThatThrownBy(() -> SignatureValidator.create().addTrustedCertificate(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("trustStore 设置信任库")
        void testTrustStore() {
            Path trustStorePath = Path.of("/truststore.jks");
            SignatureValidator validator = SignatureValidator.create()
                .trustStore(trustStorePath, "password".toCharArray());

            assertThat(validator.getTrustStorePath()).isEqualTo(trustStorePath);
        }

        @Test
        @DisplayName("trustStore null 路径抛出异常")
        void testTrustStoreNullPath() {
            assertThatThrownBy(() -> SignatureValidator.create()
                .trustStore(null, "pass".toCharArray()))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("trustStore null 密码允许")
        void testTrustStoreNullPassword() {
            SignatureValidator validator = SignatureValidator.create()
                .trustStore(Path.of("/ts.jks"), null);

            assertThat(validator.getTrustStorePath()).isNotNull();
        }
    }

    @Nested
    @DisplayName("验证选项测试")
    class ValidationOptionsTests {

        @Test
        @DisplayName("checkRevocation 启用吊销检查")
        void testCheckRevocationTrue() {
            SignatureValidator validator = SignatureValidator.create()
                .checkRevocation(true);

            assertThat(validator.isCheckRevocation()).isTrue();
        }

        @Test
        @DisplayName("checkRevocation 禁用吊销检查")
        void testCheckRevocationFalse() {
            SignatureValidator validator = SignatureValidator.create()
                .checkRevocation(false);

            assertThat(validator.isCheckRevocation()).isFalse();
        }

        @Test
        @DisplayName("checkOcsp 启用 OCSP 检查")
        void testCheckOcspTrue() {
            SignatureValidator validator = SignatureValidator.create()
                .checkOcsp(true);

            assertThat(validator.isCheckOcsp()).isTrue();
        }

        @Test
        @DisplayName("checkOcsp 禁用 OCSP 检查")
        void testCheckOcspFalse() {
            SignatureValidator validator = SignatureValidator.create()
                .checkOcsp(false);

            assertThat(validator.isCheckOcsp()).isFalse();
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidateMethodTests {

        @Test
        @DisplayName("validate(Path) null 抛出异常")
        void testValidatePathNull() {
            assertThatThrownBy(() -> SignatureValidator.create().validate((Path) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("validate(Path, String) null source 抛出异常")
        void testValidateWithFieldNullSource() {
            assertThatThrownBy(() -> SignatureValidator.create().validate(null, "field"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("validate(Path, String) null fieldName 抛出异常")
        void testValidateWithFieldNullFieldName() {
            assertThatThrownBy(() -> SignatureValidator.create()
                .validate(Path.of("/doc.pdf"), null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("ValidationResult Record 测试")
    class ValidationResultTests {

        @Test
        @DisplayName("valid 创建有效结果")
        void testValidResult() {
            SignatureInfo sigInfo = SignatureInfo.builder().name("TestSig").build();
            SignatureValidator.ValidationResult result = SignatureValidator.ValidationResult.valid(sigInfo);

            assertThat(result.signatureInfo()).isEqualTo(sigInfo);
            assertThat(result.integrityValid()).isTrue();
            assertThat(result.certificateValid()).isTrue();
            assertThat(result.chainValid()).isTrue();
            assertThat(result.timestampValid()).isTrue();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("invalid 创建无效结果")
        void testInvalidResult() {
            SignatureInfo sigInfo = SignatureInfo.builder().name("FailedSig").build();
            SignatureValidator.ValidationResult result = SignatureValidator.ValidationResult.invalid(
                sigInfo, "Certificate expired");

            assertThat(result.signatureInfo()).isEqualTo(sigInfo);
            assertThat(result.integrityValid()).isFalse();
            assertThat(result.certificateValid()).isFalse();
            assertThat(result.chainValid()).isFalse();
            assertThat(result.timestampValid()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("Certificate expired");
        }

        @Test
        @DisplayName("isFullyValid 所有验证通过返回 true")
        void testIsFullyValidTrue() {
            SignatureValidator.ValidationResult result = new SignatureValidator.ValidationResult(
                null, true, true, true, true, null);

            assertThat(result.isFullyValid()).isTrue();
        }

        @Test
        @DisplayName("isFullyValid 完整性无效返回 false")
        void testIsFullyValidIntegrityFalse() {
            SignatureValidator.ValidationResult result = new SignatureValidator.ValidationResult(
                null, false, true, true, true, null);

            assertThat(result.isFullyValid()).isFalse();
        }

        @Test
        @DisplayName("isFullyValid 证书无效返回 false")
        void testIsFullyValidCertificateFalse() {
            SignatureValidator.ValidationResult result = new SignatureValidator.ValidationResult(
                null, true, false, true, true, null);

            assertThat(result.isFullyValid()).isFalse();
        }

        @Test
        @DisplayName("isFullyValid 证书链无效返回 false")
        void testIsFullyValidChainFalse() {
            SignatureValidator.ValidationResult result = new SignatureValidator.ValidationResult(
                null, true, true, false, true, null);

            assertThat(result.isFullyValid()).isFalse();
        }

        @Test
        @DisplayName("isFullyValid 不检查时间戳有效性")
        void testIsFullyValidIgnoresTimestamp() {
            SignatureValidator.ValidationResult result = new SignatureValidator.ValidationResult(
                null, true, true, true, false, null);

            assertThat(result.isFullyValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            SignatureValidator validator = SignatureValidator.create()
                .trustedCertificates(List.of())
                .trustStore(Path.of("/truststore.jks"), "password".toCharArray())
                .checkRevocation(true)
                .checkOcsp(true);

            assertThat(validator.getTrustedCertificates()).isEmpty();
            assertThat(validator.getTrustStorePath()).isEqualTo(Path.of("/truststore.jks"));
            assertThat(validator.isCheckRevocation()).isTrue();
            assertThat(validator.isCheckOcsp()).isTrue();
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认不检查吊销")
        void testDefaultCheckRevocation() {
            SignatureValidator validator = SignatureValidator.create();

            assertThat(validator.isCheckRevocation()).isFalse();
        }

        @Test
        @DisplayName("默认不检查 OCSP")
        void testDefaultCheckOcsp() {
            SignatureValidator validator = SignatureValidator.create();

            assertThat(validator.isCheckOcsp()).isFalse();
        }

        @Test
        @DisplayName("默认无信任库")
        void testDefaultTrustStore() {
            SignatureValidator validator = SignatureValidator.create();

            assertThat(validator.getTrustStorePath()).isNull();
        }

        @Test
        @DisplayName("默认无受信任证书")
        void testDefaultTrustedCertificates() {
            SignatureValidator validator = SignatureValidator.create();

            assertThat(validator.getTrustedCertificates()).isEmpty();
        }
    }
}
