package cloud.opencode.base.crypto.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CurveType 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("CurveType 测试")
class CurveTypeTest {

    @Nested
    @DisplayName("getCurveName 测试")
    class GetCurveNameTests {

        @Test
        @DisplayName("P-256曲线名")
        void testP256CurveName() {
            assertThat(CurveType.P_256.getCurveName()).isEqualTo("secp256r1");
        }

        @Test
        @DisplayName("P-384曲线名")
        void testP384CurveName() {
            assertThat(CurveType.P_384.getCurveName()).isEqualTo("secp384r1");
        }

        @Test
        @DisplayName("P-521曲线名")
        void testP521CurveName() {
            assertThat(CurveType.P_521.getCurveName()).isEqualTo("secp521r1");
        }

        @Test
        @DisplayName("secp256k1曲线名")
        void testSecp256k1CurveName() {
            assertThat(CurveType.SECP256K1.getCurveName()).isEqualTo("secp256k1");
        }

        @Test
        @DisplayName("Curve25519曲线名")
        void testCurve25519CurveName() {
            assertThat(CurveType.CURVE25519.getCurveName()).isEqualTo("curve25519");
        }

        @Test
        @DisplayName("Curve448曲线名")
        void testCurve448CurveName() {
            assertThat(CurveType.CURVE448.getCurveName()).isEqualTo("curve448");
        }

        @Test
        @DisplayName("Ed25519曲线名")
        void testEd25519CurveName() {
            assertThat(CurveType.ED25519.getCurveName()).isEqualTo("ed25519");
        }

        @Test
        @DisplayName("Ed448曲线名")
        void testEd448CurveName() {
            assertThat(CurveType.ED448.getCurveName()).isEqualTo("ed448");
        }

        @Test
        @DisplayName("SM2曲线名")
        void testSm2CurveName() {
            assertThat(CurveType.SM2.getCurveName()).isEqualTo("sm2p256v1");
        }
    }

    @Nested
    @DisplayName("getKeySize 测试")
    class GetKeySizeTests {

        @Test
        @DisplayName("P-256密钥大小256位")
        void testP256KeySize() {
            assertThat(CurveType.P_256.getKeySize()).isEqualTo(256);
        }

        @Test
        @DisplayName("P-384密钥大小384位")
        void testP384KeySize() {
            assertThat(CurveType.P_384.getKeySize()).isEqualTo(384);
        }

        @Test
        @DisplayName("P-521密钥大小521位")
        void testP521KeySize() {
            assertThat(CurveType.P_521.getKeySize()).isEqualTo(521);
        }

        @Test
        @DisplayName("secp256k1密钥大小256位")
        void testSecp256k1KeySize() {
            assertThat(CurveType.SECP256K1.getKeySize()).isEqualTo(256);
        }

        @Test
        @DisplayName("Curve25519密钥大小255位")
        void testCurve25519KeySize() {
            assertThat(CurveType.CURVE25519.getKeySize()).isEqualTo(255);
        }

        @Test
        @DisplayName("Curve448密钥大小448位")
        void testCurve448KeySize() {
            assertThat(CurveType.CURVE448.getKeySize()).isEqualTo(448);
        }

        @Test
        @DisplayName("Ed25519密钥大小255位")
        void testEd25519KeySize() {
            assertThat(CurveType.ED25519.getKeySize()).isEqualTo(255);
        }

        @Test
        @DisplayName("Ed448密钥大小448位")
        void testEd448KeySize() {
            assertThat(CurveType.ED448.getKeySize()).isEqualTo(448);
        }

        @Test
        @DisplayName("SM2密钥大小256位")
        void testSm2KeySize() {
            assertThat(CurveType.SM2.getKeySize()).isEqualTo(256);
        }
    }

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("所有枚举值存在")
        void testAllValuesExist() {
            assertThat(CurveType.values()).hasSize(9);
        }

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(CurveType.valueOf("P_256")).isEqualTo(CurveType.P_256);
            assertThat(CurveType.valueOf("ED25519")).isEqualTo(CurveType.ED25519);
            assertThat(CurveType.valueOf("SM2")).isEqualTo(CurveType.SM2);
        }

        @Test
        @DisplayName("包含所有曲线")
        void testContainsAllCurves() {
            assertThat(CurveType.values()).contains(
                    CurveType.P_256,
                    CurveType.P_384,
                    CurveType.P_521,
                    CurveType.SECP256K1,
                    CurveType.CURVE25519,
                    CurveType.CURVE448,
                    CurveType.ED25519,
                    CurveType.ED448,
                    CurveType.SM2
            );
        }
    }
}
