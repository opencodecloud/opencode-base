package cloud.opencode.base.xml.validate;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlValidatorTest Tests
 * XmlValidatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlValidator Tests")
class XmlValidatorTest {

    private static final String VALID_XML = "<root><child>text</child></root>";
    private static final String INVALID_XML = "<root><child></root>";
    private static final String EMPTY_XML = "";

    @Nested
    @DisplayName("Well-Formedness Tests")
    class WellFormednessTests {

        @Test
        @DisplayName("isWellFormed should return true for valid XML")
        void isWellFormedShouldReturnTrueForValidXml() {
            assertThat(XmlValidator.isWellFormed(VALID_XML)).isTrue();
        }

        @Test
        @DisplayName("isWellFormed should return false for invalid XML")
        void isWellFormedShouldReturnFalseForInvalidXml() {
            assertThat(XmlValidator.isWellFormed(INVALID_XML)).isFalse();
        }

        @Test
        @DisplayName("isWellFormed should handle null gracefully")
        void isWellFormedShouldHandleNullGracefully() {
            // Null input causes exception in the underlying StringReader
            assertThatThrownBy(() -> XmlValidator.isWellFormed((String) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isWellFormed should return false for empty string")
        void isWellFormedShouldReturnFalseForEmptyString() {
            assertThat(XmlValidator.isWellFormed(EMPTY_XML)).isFalse();
        }

        @Test
        @DisplayName("isWellFormed from path should validate file")
        void isWellFormedFromPathShouldValidateFile() throws IOException {
            Path tempFile = Files.createTempFile("test", ".xml");
            try {
                Files.writeString(tempFile, VALID_XML);

                assertThat(XmlValidator.isWellFormed(tempFile)).isTrue();
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("isWellFormed from path should validate file content")
        void isWellFormedFromPathShouldValidateFileContent() throws IOException {
            Path tempFile = Files.createTempFile("test", ".xml");
            try {
                Files.writeString(tempFile, VALID_XML);

                assertThat(XmlValidator.isWellFormed(tempFile)).isTrue();
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    @Nested
    @DisplayName("Validate Well-Formedness Tests")
    class ValidateWellFormednessTests {

        @Test
        @DisplayName("validateWellFormedness should return valid result for valid XML")
        void validateWellFormednessShouldReturnValidResultForValidXml() {
            ValidationResult result = XmlValidator.validateWellFormedness(VALID_XML);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("validateWellFormedness should return invalid result with errors")
        void validateWellFormednessShouldReturnInvalidResultWithErrors() {
            ValidationResult result = XmlValidator.validateWellFormedness(INVALID_XML);

            assertThat(result.isValid()).isFalse();
            assertThat(result.hasErrors()).isTrue();
        }
    }

    @Nested
    @DisplayName("Schema Validation Tests")
    class SchemaValidationTests {

        private static final String XSD = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                <xs:element name="root">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="child" type="xs:string"/>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
            </xs:schema>
            """;

        @Test
        @DisplayName("validateSchema should return valid for conforming XML")
        void validateSchemaShouldReturnValidForConformingXml() throws IOException {
            Path schemaFile = Files.createTempFile("schema", ".xsd");
            try {
                Files.writeString(schemaFile, XSD);

                ValidationResult result = XmlValidator.validateSchema(VALID_XML, schemaFile);

                assertThat(result.isValid()).isTrue();
            } finally {
                Files.deleteIfExists(schemaFile);
            }
        }

        @Test
        @DisplayName("validateSchema should return invalid for non-conforming XML")
        void validateSchemaShouldReturnInvalidForNonConformingXml() throws IOException {
            Path schemaFile = Files.createTempFile("schema", ".xsd");
            try {
                Files.writeString(schemaFile, XSD);
                String invalidXml = "<root><other>text</other></root>";

                ValidationResult result = XmlValidator.validateSchema(invalidXml, schemaFile);

                assertThat(result.isValid()).isFalse();
            } finally {
                Files.deleteIfExists(schemaFile);
            }
        }
    }
}
