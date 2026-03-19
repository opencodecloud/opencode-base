package cloud.opencode.base.xml.validate;

import cloud.opencode.base.xml.exception.XmlValidationException;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SchemaValidatorTest Tests
 * SchemaValidatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("SchemaValidator Tests")
class SchemaValidatorTest {

    private static final String VALID_XML = "<root><child>text</child></root>";
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

    private Path schemaFile;

    @BeforeEach
    void setUp() throws IOException {
        schemaFile = Files.createTempFile("schema", ".xsd");
        Files.writeString(schemaFile, XSD);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(schemaFile);
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of with Path should create validator")
        void ofWithPathShouldCreateValidator() {
            SchemaValidator validator = SchemaValidator.of(schemaFile);

            assertThat(validator).isNotNull();
        }

        @Test
        @DisplayName("of with String should create validator")
        void ofWithStringShouldCreateValidator() {
            SchemaValidator validator = SchemaValidator.of(XSD);

            assertThat(validator).isNotNull();
        }

        @Test
        @DisplayName("of with InputStream should create validator")
        void ofWithInputStreamShouldCreateValidator() {
            InputStream is = new ByteArrayInputStream(XSD.getBytes());

            SchemaValidator validator = SchemaValidator.of(is);

            assertThat(validator).isNotNull();
        }
    }

    @Nested
    @DisplayName("Validate Tests")
    class ValidateTests {

        @Test
        @DisplayName("validate should return valid result for conforming XML")
        void validateShouldReturnValidResultForConformingXml() {
            SchemaValidator validator = SchemaValidator.of(schemaFile);

            ValidationResult result = validator.validate(VALID_XML);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("validate should return invalid result for non-conforming XML")
        void validateShouldReturnInvalidResultForNonConformingXml() {
            SchemaValidator validator = SchemaValidator.of(schemaFile);
            String invalidXml = "<root><other>text</other></root>";

            ValidationResult result = validator.validate(invalidXml);

            assertThat(result.isValid()).isFalse();
            assertThat(result.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("validate from Path should work")
        void validateFromPathShouldWork() throws IOException {
            Path xmlFile = Files.createTempFile("test", ".xml");
            try {
                Files.writeString(xmlFile, VALID_XML);
                SchemaValidator validator = SchemaValidator.of(schemaFile);

                ValidationResult result = validator.validate(xmlFile);

                assertThat(result.isValid()).isTrue();
            } finally {
                Files.deleteIfExists(xmlFile);
            }
        }

        @Test
        @DisplayName("validate from InputStream should work")
        void validateFromInputStreamShouldWork() {
            SchemaValidator validator = SchemaValidator.of(schemaFile);
            InputStream is = new ByteArrayInputStream(VALID_XML.getBytes());

            ValidationResult result = validator.validate(is);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("ValidateOrThrow Tests")
    class ValidateOrThrowTests {

        @Test
        @DisplayName("validateOrThrow should not throw for valid XML")
        void validateOrThrowShouldNotThrowForValidXml() {
            SchemaValidator validator = SchemaValidator.of(schemaFile);

            assertThatCode(() -> validator.validateOrThrow(VALID_XML))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateOrThrow should throw for invalid XML")
        void validateOrThrowShouldThrowForInvalidXml() {
            SchemaValidator validator = SchemaValidator.of(schemaFile);
            String invalidXml = "<root><other>text</other></root>";

            assertThatThrownBy(() -> validator.validateOrThrow(invalidXml))
                .isInstanceOf(XmlValidationException.class);
        }
    }

    @Nested
    @DisplayName("IsValid Tests")
    class IsValidTests {

        @Test
        @DisplayName("isValid should return true for conforming XML")
        void isValidShouldReturnTrueForConformingXml() {
            SchemaValidator validator = SchemaValidator.of(schemaFile);

            assertThat(validator.isValid(VALID_XML)).isTrue();
        }

        @Test
        @DisplayName("isValid should return false for non-conforming XML")
        void isValidShouldReturnFalseForNonConformingXml() {
            SchemaValidator validator = SchemaValidator.of(schemaFile);
            String invalidXml = "<root><other>text</other></root>";

            assertThat(validator.isValid(invalidXml)).isFalse();
        }
    }
}
