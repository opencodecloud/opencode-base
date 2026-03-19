package cloud.opencode.base.pdf.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SignatureField 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("SignatureField 测试")
class SignatureFieldTest {

    @Nested
    @DisplayName("接口继承测试")
    class InterfaceInheritanceTests {

        @Test
        @DisplayName("SignatureField 继承 FormField")
        void testExtendsFormField() {
            assertThat(FormField.class.isAssignableFrom(SignatureField.class)).isTrue();
        }

        @Test
        @DisplayName("SignatureField 是 non-sealed 接口")
        void testIsNonSealed() {
            assertThat(SignatureField.class.isSealed()).isFalse();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("getType 返回 SIGNATURE")
        void testGetTypeReturnsSignature() {
            SignatureField signatureField = createMockSignatureField(
                false, null, null, null, null);

            assertThat(signatureField.getType()).isEqualTo(FormField.FieldType.SIGNATURE);
        }
    }

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 isSigned 方法")
        void testHasIsSignedMethod() throws NoSuchMethodException {
            assertThat(SignatureField.class.getMethod("isSigned")).isNotNull();
        }

        @Test
        @DisplayName("声明 getSignerName 方法")
        void testHasGetSignerNameMethod() throws NoSuchMethodException {
            assertThat(SignatureField.class.getMethod("getSignerName")).isNotNull();
        }

        @Test
        @DisplayName("声明 getSignDate 方法")
        void testHasGetSignDateMethod() throws NoSuchMethodException {
            assertThat(SignatureField.class.getMethod("getSignDate")).isNotNull();
        }

        @Test
        @DisplayName("声明 getReason 方法")
        void testHasGetReasonMethod() throws NoSuchMethodException {
            assertThat(SignatureField.class.getMethod("getReason")).isNotNull();
        }

        @Test
        @DisplayName("声明 getLocation 方法")
        void testHasGetLocationMethod() throws NoSuchMethodException {
            assertThat(SignatureField.class.getMethod("getLocation")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mock 实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("未签名状态")
        void testUnsigned() {
            SignatureField field = createMockSignatureField(
                false, null, null, null, null);

            assertThat(field.isSigned()).isFalse();
            assertThat(field.getSignerName()).isNull();
            assertThat(field.getSignDate()).isNull();
            assertThat(field.getReason()).isNull();
            assertThat(field.getLocation()).isNull();
        }

        @Test
        @DisplayName("已签名状态")
        void testSigned() {
            SignatureField field = createMockSignatureField(
                true, "John Doe", "2024-01-15", "Contract Approval", "New York");

            assertThat(field.isSigned()).isTrue();
            assertThat(field.getSignerName()).isEqualTo("John Doe");
            assertThat(field.getSignDate()).isEqualTo("2024-01-15");
            assertThat(field.getReason()).isEqualTo("Contract Approval");
            assertThat(field.getLocation()).isEqualTo("New York");
        }

        @Test
        @DisplayName("部分签名信息")
        void testPartialSignatureInfo() {
            SignatureField field = createMockSignatureField(
                true, "Jane Doe", "2024-02-20", null, null);

            assertThat(field.isSigned()).isTrue();
            assertThat(field.getSignerName()).isEqualTo("Jane Doe");
            assertThat(field.getSignDate()).isEqualTo("2024-02-20");
            assertThat(field.getReason()).isNull();
            assertThat(field.getLocation()).isNull();
        }

        @Test
        @DisplayName("仅有原因")
        void testWithReasonOnly() {
            SignatureField field = createMockSignatureField(
                true, "Signer", "2024-03-01", "Review", null);

            assertThat(field.getReason()).isEqualTo("Review");
            assertThat(field.getLocation()).isNull();
        }

        @Test
        @DisplayName("仅有位置")
        void testWithLocationOnly() {
            SignatureField field = createMockSignatureField(
                true, "Signer", "2024-03-01", null, "Beijing");

            assertThat(field.getReason()).isNull();
            assertThat(field.getLocation()).isEqualTo("Beijing");
        }
    }

    private SignatureField createMockSignatureField(boolean signed, String signerName,
                                                    String signDate, String reason, String location) {
        return new SignatureField() {
            @Override
            public boolean isSigned() { return signed; }
            @Override
            public String getSignerName() { return signerName; }
            @Override
            public String getSignDate() { return signDate; }
            @Override
            public String getReason() { return reason; }
            @Override
            public String getLocation() { return location; }
            @Override
            public String getName() { return "signature"; }
            @Override
            public String getValue() { return signed ? "signed" : ""; }
            @Override
            public void setValue(String value) {}
            @Override
            public String getTooltip() { return null; }
            @Override
            public boolean isRequired() { return false; }
            @Override
            public boolean isReadOnly() { return true; }
            @Override
            public float[] getRectangle() { return new float[]{100, 100, 200, 50}; }
        };
    }
}
