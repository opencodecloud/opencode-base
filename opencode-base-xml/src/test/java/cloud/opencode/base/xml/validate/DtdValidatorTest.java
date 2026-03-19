package cloud.opencode.base.xml.validate;

import cloud.opencode.base.xml.exception.XmlValidationException;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DtdValidatorTest Tests
 * DtdValidatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("DtdValidator Tests")
class DtdValidatorTest {

    private static final String DTD = """
        <!ELEMENT root (child)>
        <!ELEMENT child (#PCDATA)>
        """;

    private static final String VALID_XML_WITH_DTD = """
        <?xml version="1.0"?>
        <!DOCTYPE root [
            <!ELEMENT root (child)>
            <!ELEMENT child (#PCDATA)>
        ]>
        <root><child>text</child></root>
        """;

    private static final String INVALID_XML_WITH_DTD = """
        <?xml version="1.0"?>
        <!DOCTYPE root [
            <!ELEMENT root (child)>
            <!ELEMENT child (#PCDATA)>
        ]>
        <root><other>text</other></root>
        """;

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create should create default validator")
        void createShouldCreateDefaultValidator() {
            DtdValidator validator = DtdValidator.create();

            assertThat(validator).isNotNull();
        }

        @Test
        @DisplayName("withExternalDtd should create validator with external DTD")
        void withExternalDtdShouldCreateValidatorWithExternalDtd() throws IOException {
            Path dtdFile = Files.createTempFile("test", ".dtd");
            try {
                Files.writeString(dtdFile, DTD);

                DtdValidator validator = DtdValidator.create().withExternalDtd(dtdFile);

                assertThat(validator).isNotNull();
            } finally {
                Files.deleteIfExists(dtdFile);
            }
        }
    }

    @Nested
    @DisplayName("Validate Inline DTD Tests")
    class ValidateInlineDtdTests {

        @Test
        @DisplayName("validate should return valid for conforming XML")
        void validateShouldReturnValidForConformingXml() {
            DtdValidator validator = DtdValidator.create();

            ValidationResult result = validator.validate(VALID_XML_WITH_DTD);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("validate should return invalid for non-conforming XML")
        void validateShouldReturnInvalidForNonConformingXml() {
            DtdValidator validator = DtdValidator.create();

            ValidationResult result = validator.validate(INVALID_XML_WITH_DTD);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("ValidateOrThrow Tests")
    class ValidateOrThrowTests {

        @Test
        @DisplayName("validateOrThrow should not throw for valid XML")
        void validateOrThrowShouldNotThrowForValidXml() {
            DtdValidator validator = DtdValidator.create();

            assertThatCode(() -> validator.validateOrThrow(VALID_XML_WITH_DTD))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateOrThrow should throw for invalid XML")
        void validateOrThrowShouldThrowForInvalidXml() {
            DtdValidator validator = DtdValidator.create();

            assertThatThrownBy(() -> validator.validateOrThrow(INVALID_XML_WITH_DTD))
                .isInstanceOf(XmlValidationException.class);
        }
    }

    @Nested
    @DisplayName("IsValid Tests")
    class IsValidTests {

        @Test
        @DisplayName("isValid should return true for conforming XML")
        void isValidShouldReturnTrueForConformingXml() {
            DtdValidator validator = DtdValidator.create();

            assertThat(validator.isValid(VALID_XML_WITH_DTD)).isTrue();
        }

        @Test
        @DisplayName("isValid should return false for non-conforming XML")
        void isValidShouldReturnFalseForNonConformingXml() {
            DtdValidator validator = DtdValidator.create();

            assertThat(validator.isValid(INVALID_XML_WITH_DTD)).isFalse();
        }
    }

    @Nested
    @DisplayName("Security Settings Tests")
    class SecuritySettingsTests {

        @Test
        @DisplayName("allowExternalEntities should configure external entity handling")
        void allowExternalEntitiesShouldConfigureExternalEntityHandling() {
            DtdValidator validator = DtdValidator.create()
                .allowExternalEntities(false);

            assertThat(validator).isNotNull();
        }
    }

    @Nested
    @DisplayName("Validate From Path Tests")
    class ValidateFromPathTests {

        @Test
        @DisplayName("validate from Path should work")
        void validateFromPathShouldWork() throws IOException {
            Path xmlFile = Files.createTempFile("test", ".xml");
            try {
                Files.writeString(xmlFile, VALID_XML_WITH_DTD);
                DtdValidator validator = DtdValidator.create();

                ValidationResult result = validator.validate(xmlFile);

                assertThat(result.isValid()).isTrue();
            } finally {
                Files.deleteIfExists(xmlFile);
            }
        }
    }

    @Nested
    @DisplayName("Validate From InputStream Tests")
    class ValidateFromInputStreamTests {

        @Test
        @DisplayName("validate from InputStream should work")
        void validateFromInputStreamShouldWork() {
            DtdValidator validator = DtdValidator.create();
            InputStream is = new ByteArrayInputStream(VALID_XML_WITH_DTD.getBytes());

            ValidationResult result = validator.validate(is);

            assertThat(result.isValid()).isTrue();
        }
    }
}
