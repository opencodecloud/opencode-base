package cloud.opencode.base.web.crypto;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DecryptResult Annotation Tests
 * DecryptResult 注解测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("DecryptResult 注解测试")
class DecryptResultTest {

    @DecryptResult
    static class DefaultAnnotated {}

    @DecryptResult(keyAlias = "partner-key", algorithm = "AES-GCM")
    static class CustomAnnotated {}

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认keyAlias为空字符串")
        void defaultKeyAlias() {
            DecryptResult ann = DefaultAnnotated.class.getAnnotation(DecryptResult.class);
            assertThat(ann.keyAlias()).isEmpty();
        }

        @Test
        @DisplayName("默认algorithm为空字符串")
        void defaultAlgorithm() {
            DecryptResult ann = DefaultAnnotated.class.getAnnotation(DecryptResult.class);
            assertThat(ann.algorithm()).isEmpty();
        }
    }

    @Nested
    @DisplayName("自定义值测试")
    class CustomValueTests {

        @Test
        @DisplayName("自定义keyAlias和algorithm")
        void customValues() {
            DecryptResult ann = CustomAnnotated.class.getAnnotation(DecryptResult.class);
            assertThat(ann.keyAlias()).isEqualTo("partner-key");
            assertThat(ann.algorithm()).isEqualTo("AES-GCM");
        }
    }

    @Nested
    @DisplayName("注解目标测试")
    class TargetTests {

        void method(@DecryptResult EncryptedResult encrypted) {}

        @Test
        @DisplayName("可标注在参数上")
        void canAnnotateParameter() throws NoSuchMethodException {
            var method = TargetTests.class.getDeclaredMethod("method", EncryptedResult.class);
            var annotations = method.getParameterAnnotations()[0];
            assertThat(annotations).hasAtLeastOneElementOfType(DecryptResult.class);
        }

        @Test
        @DisplayName("可标注在类上")
        void canAnnotateClass() {
            assertThat(DefaultAnnotated.class.isAnnotationPresent(DecryptResult.class)).isTrue();
        }
    }
}
