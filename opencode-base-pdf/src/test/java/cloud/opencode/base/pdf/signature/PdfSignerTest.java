package cloud.opencode.base.pdf.signature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfSigner 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfSigner 测试")
class PdfSignerTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("create 创建签名器")
        void testCreate() {
            PdfSigner signer = PdfSigner.create();

            assertThat(signer).isNotNull();
        }
    }

    @Nested
    @DisplayName("密钥库配置测试")
    class KeyStoreConfigTests {

        @Test
        @DisplayName("keyStore(Path) 设置密钥库路径")
        void testKeyStoreFromPath() {
            Path keyStorePath = Path.of("/keystore.p12");
            char[] password = "password".toCharArray();

            PdfSigner signer = PdfSigner.create()
                .keyStore(keyStorePath, password, "PKCS12");

            assertThat(signer.getKeyStorePath()).isEqualTo(keyStorePath);
            assertThat(signer.getKeyStoreType()).isEqualTo("PKCS12");
        }

        @Test
        @DisplayName("keyStore(InputStream) 设置密钥库流")
        void testKeyStoreFromStream() {
            InputStream stream = new ByteArrayInputStream(new byte[0]);
            char[] password = "pass".toCharArray();

            PdfSigner signer = PdfSigner.create()
                .keyStore(stream, password, "JKS");

            assertThat(signer.getKeyStorePath()).isNull();
            assertThat(signer.getKeyStoreType()).isEqualTo("JKS");
        }

        @Test
        @DisplayName("keyStore(Path) null 路径抛出异常")
        void testKeyStorePathNull() {
            assertThatThrownBy(() -> PdfSigner.create()
                .keyStore((Path) null, "pass".toCharArray(), "PKCS12"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("keyStore(Path) null 密码抛出异常")
        void testKeyStorePasswordNull() {
            assertThatThrownBy(() -> PdfSigner.create()
                .keyStore(Path.of("/ks.p12"), null, "PKCS12"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("keyStore(Path) null 类型抛出异常")
        void testKeyStoreTypeNull() {
            assertThatThrownBy(() -> PdfSigner.create()
                .keyStore(Path.of("/ks.p12"), "pass".toCharArray(), null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("alias 设置证书别名")
        void testAlias() {
            PdfSigner signer = PdfSigner.create()
                .alias("mycert");

            assertThat(signer.getAlias()).isEqualTo("mycert");
        }

        @Test
        @DisplayName("alias null 抛出异常")
        void testAliasNull() {
            assertThatThrownBy(() -> PdfSigner.create().alias(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("keyPassword 设置私钥密码")
        void testKeyPassword() {
            PdfSigner signer = PdfSigner.create()
                .keyPassword("keypass".toCharArray());

            assertThat(signer).isNotNull();
        }

        @Test
        @DisplayName("keyPassword null 允许")
        void testKeyPasswordNull() {
            PdfSigner signer = PdfSigner.create()
                .keyPassword(null);

            assertThat(signer).isNotNull();
        }
    }

    @Nested
    @DisplayName("签名外观配置测试")
    class AppearanceConfigTests {

        @Test
        @DisplayName("appearance 设置签名外观")
        void testAppearance() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance();
            PdfSigner signer = PdfSigner.create()
                .appearance(appearance);

            assertThat(signer.getAppearance()).isEqualTo(appearance);
        }

        @Test
        @DisplayName("reason 设置签名原因")
        void testReason() {
            PdfSigner signer = PdfSigner.create()
                .reason("Contract Approval");

            assertThat(signer.getReason()).isEqualTo("Contract Approval");
        }

        @Test
        @DisplayName("location 设置签名位置")
        void testLocation() {
            PdfSigner signer = PdfSigner.create()
                .location("Shanghai");

            assertThat(signer.getLocation()).isEqualTo("Shanghai");
        }

        @Test
        @DisplayName("contact 设置联系信息")
        void testContact() {
            PdfSigner signer = PdfSigner.create()
                .contact("john@example.com");

            assertThat(signer.getContact()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("fieldName 设置签名字段名")
        void testFieldName() {
            PdfSigner signer = PdfSigner.create()
                .fieldName("SignatureField1");

            assertThat(signer.getFieldName()).isEqualTo("SignatureField1");
        }
    }

    @Nested
    @DisplayName("签名位置配置测试")
    class RectangleConfigTests {

        @Test
        @DisplayName("rectangle 设置签名矩形位置")
        void testRectangle() {
            PdfSigner signer = PdfSigner.create()
                .rectangle(1, 100f, 200f, 150f, 50f);

            assertThat(signer.getPageNumber()).isEqualTo(1);
            assertThat(signer.getX()).isEqualTo(100f);
            assertThat(signer.getY()).isEqualTo(200f);
            assertThat(signer.getWidth()).isEqualTo(150f);
            assertThat(signer.getHeight()).isEqualTo(50f);
        }

        @Test
        @DisplayName("rectangle 页码小于1抛出异常")
        void testRectangleInvalidPage() {
            assertThatThrownBy(() -> PdfSigner.create()
                .rectangle(0, 100f, 200f, 150f, 50f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page number must be positive");
        }

        @Test
        @DisplayName("rectangle 负页码抛出异常")
        void testRectangleNegativePage() {
            assertThatThrownBy(() -> PdfSigner.create()
                .rectangle(-1, 100f, 200f, 150f, 50f))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("时间戳配置测试")
    class TimestampConfigTests {

        @Test
        @DisplayName("timestamp(url) 设置 TSA URL")
        void testTimestampUrl() {
            PdfSigner signer = PdfSigner.create()
                .timestamp("http://tsa.example.com");

            assertThat(signer.getTsaUrl()).isEqualTo("http://tsa.example.com");
        }

        @Test
        @DisplayName("timestamp(url) null 抛出异常")
        void testTimestampUrlNull() {
            assertThatThrownBy(() -> PdfSigner.create().timestamp(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("timestamp 带认证信息")
        void testTimestampWithAuth() {
            PdfSigner signer = PdfSigner.create()
                .timestamp("http://tsa.example.com", "user", "pass");

            assertThat(signer.getTsaUrl()).isEqualTo("http://tsa.example.com");
        }
    }

    @Nested
    @DisplayName("签名执行测试")
    class SignExecutionTests {

        @Test
        @DisplayName("sign(Path) 无配置抛出异常")
        void testSignWithoutConfig() {
            PdfSigner signer = PdfSigner.create();

            assertThatThrownBy(() -> signer.sign(Path.of("/doc.pdf")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Key store must be configured");
        }

        @Test
        @DisplayName("sign(Path) 无别名抛出异常")
        void testSignWithoutAlias() {
            PdfSigner signer = PdfSigner.create()
                .keyStore(Path.of("/ks.p12"), "pass".toCharArray(), "PKCS12");

            assertThatThrownBy(() -> signer.sign(Path.of("/doc.pdf")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Certificate alias must be configured");
        }

        @Test
        @DisplayName("sign(Path) null 抛出异常")
        void testSignNullPath() {
            assertThatThrownBy(() -> PdfSigner.create()
                .keyStore(Path.of("/ks.p12"), "pass".toCharArray(), "PKCS12")
                .alias("cert")
                .sign((Path) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("sign(Path, Path) null source 抛出异常")
        void testSignNullSource() {
            assertThatThrownBy(() -> PdfSigner.create()
                .keyStore(Path.of("/ks.p12"), "pass".toCharArray(), "PKCS12")
                .alias("cert")
                .sign(null, Path.of("/out.pdf")))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("sign(Path, Path) null target 抛出异常")
        void testSignNullTarget() {
            assertThatThrownBy(() -> PdfSigner.create()
                .keyStore(Path.of("/ks.p12"), "pass".toCharArray(), "PKCS12")
                .alias("cert")
                .sign(Path.of("/in.pdf"), null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            PdfSigner signer = PdfSigner.create()
                .keyStore(Path.of("/keystore.p12"), "password".toCharArray(), "PKCS12")
                .alias("mycert")
                .keyPassword("keypass".toCharArray())
                .reason("Approval")
                .location("Beijing")
                .contact("admin@example.com")
                .fieldName("Sig1")
                .rectangle(1, 50f, 50f, 200f, 50f)
                .appearance(SignatureAppearance.defaultAppearance())
                .timestamp("http://tsa.example.com");

            assertThat(signer.getKeyStorePath()).isEqualTo(Path.of("/keystore.p12"));
            assertThat(signer.getAlias()).isEqualTo("mycert");
            assertThat(signer.getReason()).isEqualTo("Approval");
            assertThat(signer.getLocation()).isEqualTo("Beijing");
            assertThat(signer.getContact()).isEqualTo("admin@example.com");
            assertThat(signer.getFieldName()).isEqualTo("Sig1");
            assertThat(signer.getPageNumber()).isEqualTo(1);
            assertThat(signer.getTsaUrl()).isEqualTo("http://tsa.example.com");
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认密钥库类型为 PKCS12")
        void testDefaultKeyStoreType() {
            PdfSigner signer = PdfSigner.create();

            assertThat(signer.getKeyStoreType()).isEqualTo("PKCS12");
        }

        @Test
        @DisplayName("默认无签名外观")
        void testDefaultNoAppearance() {
            PdfSigner signer = PdfSigner.create();

            assertThat(signer.getAppearance()).isNull();
        }

        @Test
        @DisplayName("默认页码为 0")
        void testDefaultPageNumber() {
            PdfSigner signer = PdfSigner.create();

            assertThat(signer.getPageNumber()).isEqualTo(0);
        }
    }
}
