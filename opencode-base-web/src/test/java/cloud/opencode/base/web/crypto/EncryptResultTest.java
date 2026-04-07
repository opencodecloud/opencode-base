package cloud.opencode.base.web.crypto;

import org.junit.jupiter.api.*;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.*;

/**
 * EncryptResult Annotation Tests
 * EncryptResult 注解测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("EncryptResult 注解测试")
class EncryptResultTest {

    @EncryptResult
    static class DefaultAnnotated {}

    @EncryptResult(keyAlias = "partner-key", algorithm = "AES-GCM", enabled = true)
    static class CustomAnnotated {}

    @EncryptResult(enabled = false)
    static class DisabledAnnotated {}

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认keyAlias为空字符串")
        void defaultKeyAlias() {
            EncryptResult ann = DefaultAnnotated.class.getAnnotation(EncryptResult.class);
            assertThat(ann.keyAlias()).isEmpty();
        }

        @Test
        @DisplayName("默认algorithm为空字符串")
        void defaultAlgorithm() {
            EncryptResult ann = DefaultAnnotated.class.getAnnotation(EncryptResult.class);
            assertThat(ann.algorithm()).isEmpty();
        }

        @Test
        @DisplayName("默认enabled为true")
        void defaultEnabled() {
            EncryptResult ann = DefaultAnnotated.class.getAnnotation(EncryptResult.class);
            assertThat(ann.enabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("自定义值测试")
    class CustomValueTests {

        @Test
        @DisplayName("自定义keyAlias")
        void customKeyAlias() {
            EncryptResult ann = CustomAnnotated.class.getAnnotation(EncryptResult.class);
            assertThat(ann.keyAlias()).isEqualTo("partner-key");
        }

        @Test
        @DisplayName("自定义algorithm")
        void customAlgorithm() {
            EncryptResult ann = CustomAnnotated.class.getAnnotation(EncryptResult.class);
            assertThat(ann.algorithm()).isEqualTo("AES-GCM");
        }

        @Test
        @DisplayName("enabled设为false可禁用")
        void disabledAnnotation() {
            EncryptResult ann = DisabledAnnotated.class.getAnnotation(EncryptResult.class);
            assertThat(ann.enabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("注解目标测试")
    class TargetTests {

        @EncryptResult
        void annotatedMethod() {}

        @Test
        @DisplayName("可标注在方法上")
        void canAnnotateMethod() throws NoSuchMethodException {
            var method = TargetTests.class.getDeclaredMethod("annotatedMethod");
            assertThat(method.isAnnotationPresent(EncryptResult.class)).isTrue();
        }

        @Test
        @DisplayName("可标注在类上")
        void canAnnotateClass() {
            assertThat(DefaultAnnotated.class.isAnnotationPresent(EncryptResult.class)).isTrue();
        }
    }
}
