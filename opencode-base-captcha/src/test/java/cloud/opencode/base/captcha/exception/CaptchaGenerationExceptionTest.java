package cloud.opencode.base.captcha.exception;

import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaGenerationException Test - Unit tests for CAPTCHA generation exception
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaGenerationExceptionTest {

    @Nested
    @DisplayName("Message Only Constructor Tests")
    class MessageOnlyConstructorTests {

        @Test
        @DisplayName("should create exception with message only")
        void shouldCreateExceptionWithMessageOnly() {
            CaptchaGenerationException ex = new CaptchaGenerationException("Failed to generate");

            assertThat(ex.getMessage()).isEqualTo("Failed to generate");
            assertThat(ex.getType()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create exception with empty message")
        void shouldCreateExceptionWithEmptyMessage() {
            CaptchaGenerationException ex = new CaptchaGenerationException("");

            assertThat(ex.getMessage()).isEmpty();
            assertThat(ex.getType()).isNull();
        }

        @Test
        @DisplayName("should create exception with null message")
        void shouldCreateExceptionWithNullMessage() {
            CaptchaGenerationException ex = new CaptchaGenerationException((String) null);

            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getType()).isNull();
        }

        @Test
        @DisplayName("should have null type when created with message only")
        void shouldHaveNullTypeWhenCreatedWithMessageOnly() {
            CaptchaGenerationException ex = new CaptchaGenerationException("some error");

            assertThat(ex.getType()).isNull();
        }

        @Test
        @DisplayName("should preserve detailed error message")
        void shouldPreserveDetailedErrorMessage() {
            String message = "Font rendering failed: unable to load system fonts for CAPTCHA image";
            CaptchaGenerationException ex = new CaptchaGenerationException(message);

            assertThat(ex.getMessage()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("Message and Type Constructor Tests")
    class MessageAndTypeConstructorTests {

        @Test
        @DisplayName("should create exception with message and NUMERIC type")
        void shouldCreateExceptionWithMessageAndNumericType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Generation failed", CaptchaType.NUMERIC);

            assertThat(ex.getType()).isEqualTo(CaptchaType.NUMERIC);
            assertThat(ex.getMessage()).isEqualTo("Generation failed (type: NUMERIC)");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create exception with message and ALPHA type")
        void shouldCreateExceptionWithMessageAndAlphaType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Font not found", CaptchaType.ALPHA);

            assertThat(ex.getType()).isEqualTo(CaptchaType.ALPHA);
            assertThat(ex.getMessage()).isEqualTo("Font not found (type: ALPHA)");
        }

        @Test
        @DisplayName("should create exception with message and ALPHANUMERIC type")
        void shouldCreateExceptionWithMessageAndAlphanumericType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Image rendering failed", CaptchaType.ALPHANUMERIC);

            assertThat(ex.getType()).isEqualTo(CaptchaType.ALPHANUMERIC);
            assertThat(ex.getMessage()).contains("ALPHANUMERIC");
        }

        @Test
        @DisplayName("should create exception with message and ARITHMETIC type")
        void shouldCreateExceptionWithMessageAndArithmeticType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Invalid expression", CaptchaType.ARITHMETIC);

            assertThat(ex.getType()).isEqualTo(CaptchaType.ARITHMETIC);
            assertThat(ex.getMessage()).isEqualTo("Invalid expression (type: ARITHMETIC)");
        }

        @Test
        @DisplayName("should create exception with message and CHINESE type")
        void shouldCreateExceptionWithMessageAndChineseType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Chinese font missing", CaptchaType.CHINESE);

            assertThat(ex.getType()).isEqualTo(CaptchaType.CHINESE);
            assertThat(ex.getMessage()).isEqualTo("Chinese font missing (type: CHINESE)");
        }

        @Test
        @DisplayName("should create exception with message and GIF type")
        void shouldCreateExceptionWithMessageAndGifType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "GIF encoding failed", CaptchaType.GIF);

            assertThat(ex.getType()).isEqualTo(CaptchaType.GIF);
            assertThat(ex.getMessage()).isEqualTo("GIF encoding failed (type: GIF)");
        }

        @Test
        @DisplayName("should create exception with message and SLIDER type")
        void shouldCreateExceptionWithMessageAndSliderType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Slider image error", CaptchaType.SLIDER);

            assertThat(ex.getType()).isEqualTo(CaptchaType.SLIDER);
            assertThat(ex.getMessage()).isEqualTo("Slider image error (type: SLIDER)");
        }

        @Test
        @DisplayName("should create exception with message and CLICK type")
        void shouldCreateExceptionWithMessageAndClickType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Click area error", CaptchaType.CLICK);

            assertThat(ex.getType()).isEqualTo(CaptchaType.CLICK);
            assertThat(ex.getMessage()).isEqualTo("Click area error (type: CLICK)");
        }

        @Test
        @DisplayName("should create exception with message and ROTATE type")
        void shouldCreateExceptionWithMessageAndRotateType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Rotation error", CaptchaType.ROTATE);

            assertThat(ex.getType()).isEqualTo(CaptchaType.ROTATE);
            assertThat(ex.getMessage()).isEqualTo("Rotation error (type: ROTATE)");
        }

        @Test
        @DisplayName("should create exception with message and IMAGE_SELECT type")
        void shouldCreateExceptionWithMessageAndImageSelectType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Image selection error", CaptchaType.IMAGE_SELECT);

            assertThat(ex.getType()).isEqualTo(CaptchaType.IMAGE_SELECT);
            assertThat(ex.getMessage()).isEqualTo("Image selection error (type: IMAGE_SELECT)");
        }

        @Test
        @DisplayName("should format message as 'message (type: TYPE)'")
        void shouldFormatMessageWithTypeAppended() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Error occurred", CaptchaType.NUMERIC);

            assertThat(ex.getMessage()).isEqualTo("Error occurred (type: NUMERIC)");
        }

        @Test
        @DisplayName("should create exception with message and null type")
        void shouldCreateExceptionWithMessageAndNullType() {
            CaptchaType nullType = null;
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Unknown error", nullType);

            assertThat(ex.getType()).isNull();
            assertThat(ex.getMessage()).isEqualTo("Unknown error (type: null)");
        }

        @Test
        @DisplayName("should create exception with null message and valid type")
        void shouldCreateExceptionWithNullMessageAndValidType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                null, CaptchaType.GIF);

            assertThat(ex.getType()).isEqualTo(CaptchaType.GIF);
            assertThat(ex.getMessage()).isEqualTo("null (type: GIF)");
        }
    }

    @Nested
    @DisplayName("Message and Cause Constructor Tests")
    class MessageAndCauseConstructorTests {

        @Test
        @DisplayName("should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("File not found");
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Image generation failed", cause);

            assertThat(ex.getMessage()).isEqualTo("Image generation failed");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getType()).isNull();
        }

        @Test
        @DisplayName("should create exception with message and null cause")
        void shouldCreateExceptionWithMessageAndNullCause() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Generation error", (Throwable) null);

            assertThat(ex.getMessage()).isEqualTo("Generation error");
            assertThat(ex.getCause()).isNull();
            assertThat(ex.getType()).isNull();
        }

        @Test
        @DisplayName("should have null type when created with message and cause")
        void shouldHaveNullTypeWhenCreatedWithMessageAndCause() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Error", new RuntimeException());

            assertThat(ex.getType()).isNull();
        }

        @Test
        @DisplayName("should preserve cause message")
        void shouldPreserveCauseMessage() {
            RuntimeException cause = new RuntimeException("IO error during generation");
            CaptchaGenerationException ex = new CaptchaGenerationException("Wrapper", cause);

            assertThat(ex.getCause().getMessage()).isEqualTo("IO error during generation");
        }

        @Test
        @DisplayName("should preserve deep cause chain")
        void shouldPreserveDeepCauseChain() {
            IllegalArgumentException root = new IllegalArgumentException("bad arg");
            RuntimeException middle = new RuntimeException("processing", root);
            CaptchaGenerationException ex = new CaptchaGenerationException("top", middle);

            assertThat(ex.getCause()).isEqualTo(middle);
            assertThat(ex.getCause().getCause()).isEqualTo(root);
        }
    }

    @Nested
    @DisplayName("Message, Type and Cause Constructor Tests")
    class MessageTypeAndCauseConstructorTests {

        @Test
        @DisplayName("should create exception with message, type and cause")
        void shouldCreateExceptionWithMessageTypeAndCause() {
            RuntimeException cause = new RuntimeException("Rendering error");
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Failed to generate CAPTCHA", CaptchaType.IMAGE_SELECT, cause);

            assertThat(ex.getType()).isEqualTo(CaptchaType.IMAGE_SELECT);
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getMessage()).isEqualTo("Failed to generate CAPTCHA (type: IMAGE_SELECT)");
        }

        @Test
        @DisplayName("should create exception with all null parameters")
        void shouldCreateExceptionWithAllNullParameters() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                null, null, null);

            assertThat(ex.getType()).isNull();
            assertThat(ex.getCause()).isNull();
            assertThat(ex.getMessage()).isEqualTo("null (type: null)");
        }

        @Test
        @DisplayName("should create exception with ROTATE type and cause")
        void shouldCreateExceptionWithRotateTypeAndCause() {
            IllegalStateException cause = new IllegalStateException("Invalid state");
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Rotation failed", CaptchaType.ROTATE, cause);

            assertThat(ex.getType()).isEqualTo(CaptchaType.ROTATE);
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getMessage()).isEqualTo("Rotation failed (type: ROTATE)");
        }

        @Test
        @DisplayName("should create exception with CLICK type and cause")
        void shouldCreateExceptionWithClickTypeAndCause() {
            NullPointerException cause = new NullPointerException("Null image");
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Click position error", CaptchaType.CLICK, cause);

            assertThat(ex.getType()).isEqualTo(CaptchaType.CLICK);
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getMessage()).isEqualTo("Click position error (type: CLICK)");
        }

        @Test
        @DisplayName("should create exception with null type but valid message and cause")
        void shouldCreateExceptionWithNullTypeButValidMessageAndCause() {
            RuntimeException cause = new RuntimeException("error");
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Failed", null, cause);

            assertThat(ex.getType()).isNull();
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getMessage()).isEqualTo("Failed (type: null)");
        }

        @Test
        @DisplayName("should format message consistently with type-appended pattern")
        void shouldFormatMessageConsistentlyWithTypeAppendedPattern() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Error", CaptchaType.ARITHMETIC, new RuntimeException());

            assertThat(ex.getMessage()).matches("Error \\(type: ARITHMETIC\\)");
        }
    }

    @Nested
    @DisplayName("getType Method Tests")
    class GetTypeMethodTests {

        @Test
        @DisplayName("should return null type when created with message only")
        void shouldReturnNullTypeWhenCreatedWithMessageOnly() {
            CaptchaGenerationException ex = new CaptchaGenerationException("Error");

            assertThat(ex.getType()).isNull();
        }

        @Test
        @DisplayName("should return null type when created with message and cause")
        void shouldReturnNullTypeWhenCreatedWithMessageAndCause() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Error", new RuntimeException());

            assertThat(ex.getType()).isNull();
        }

        @Test
        @DisplayName("should return exact type reference")
        void shouldReturnExactType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Error", CaptchaType.ALPHANUMERIC);

            assertThat(ex.getType()).isSameAs(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("should return interactive captcha type")
        void shouldReturnInteractiveCaptchaType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Slider error", CaptchaType.SLIDER);

            assertThat(ex.getType()).isEqualTo(CaptchaType.SLIDER);
            assertThat(ex.getType().isInteractive()).isTrue();
        }

        @Test
        @DisplayName("should return text-based captcha type")
        void shouldReturnTextBasedCaptchaType() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Numeric error", CaptchaType.NUMERIC);

            assertThat(ex.getType()).isEqualTo(CaptchaType.NUMERIC);
            assertThat(ex.getType().isTextBased()).isTrue();
        }

        @Test
        @DisplayName("should return consistent type on multiple calls")
        void shouldReturnConsistentTypeOnMultipleCalls() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Error", CaptchaType.GIF);

            assertThat(ex.getType()).isSameAs(ex.getType());
            assertThat(ex.getType()).isEqualTo(CaptchaType.GIF);
        }

        @Test
        @DisplayName("should return type from three-arg constructor")
        void shouldReturnTypeFromThreeArgConstructor() {
            CaptchaGenerationException ex = new CaptchaGenerationException(
                "Error", CaptchaType.CHINESE, new RuntimeException());

            assertThat(ex.getType()).isEqualTo(CaptchaType.CHINESE);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should extend CaptchaException")
        void shouldExtendCaptchaException() {
            CaptchaGenerationException ex = new CaptchaGenerationException("Error");

            assertThat(ex).isInstanceOf(CaptchaException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            CaptchaGenerationException ex = new CaptchaGenerationException("Error");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should extend Exception")
        void shouldExtendException() {
            CaptchaGenerationException ex = new CaptchaGenerationException("Error");

            assertThat(ex).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should extend Throwable")
        void shouldExtendThrowable() {
            CaptchaGenerationException ex = new CaptchaGenerationException("Error");

            assertThat(ex).isInstanceOf(Throwable.class);
        }

        @Test
        @DisplayName("should be catchable as CaptchaException")
        void shouldBeCatchableAsCaptchaException() {
            assertThatCode(() -> {
                try {
                    throw new CaptchaGenerationException("Error", CaptchaType.GIF);
                } catch (CaptchaException e) {
                    assertThat(e).isInstanceOf(CaptchaGenerationException.class);
                    assertThat(((CaptchaGenerationException) e).getType()).isEqualTo(CaptchaType.GIF);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should be throwable with assertThatThrownBy")
        void shouldBeThrowableWithAssertThatThrownBy() {
            assertThatThrownBy(() -> {
                throw new CaptchaGenerationException("gen failed", CaptchaType.NUMERIC);
            }).isInstanceOf(CaptchaGenerationException.class)
              .isInstanceOf(CaptchaException.class)
              .hasMessageContaining("gen failed")
              .hasMessageContaining("NUMERIC");
        }

        @Test
        @DisplayName("should preserve type when caught as CaptchaException")
        void shouldPreserveTypeWhenCaughtAsCaptchaException() {
            assertThatCode(() -> {
                try {
                    throw new CaptchaGenerationException("Error", CaptchaType.SLIDER);
                } catch (CaptchaException e) {
                    CaptchaGenerationException genEx = (CaptchaGenerationException) e;
                    assertThat(genEx.getType()).isEqualTo(CaptchaType.SLIDER);
                    assertThat(genEx.getType().isInteractive()).isTrue();
                }
            }).doesNotThrowAnyException();
        }
    }
}
