package cloud.opencode.base.xml.transform;

import cloud.opencode.base.xml.XmlDocument;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XsltTransformerTest Tests
 * XsltTransformerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XsltTransformer Tests")
class XsltTransformerTest {

    private static final String SOURCE_XML = """
        <catalog>
            <book>
                <title>Java Programming</title>
                <author>John Doe</author>
            </book>
        </catalog>
        """;

    private static final String XSLT = """
        <?xml version="1.0" encoding="UTF-8"?>
        <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
            <xsl:output method="xml" indent="yes"/>
            <xsl:template match="/catalog">
                <result>
                    <xsl:for-each select="book">
                        <item>
                            <name><xsl:value-of select="title"/></name>
                        </item>
                    </xsl:for-each>
                </result>
            </xsl:template>
        </xsl:stylesheet>
        """;

    private static final String IDENTITY_XSLT = """
        <?xml version="1.0" encoding="UTF-8"?>
        <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
            <xsl:template match="@*|node()">
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:template>
        </xsl:stylesheet>
        """;

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of with String should create transformer")
        void ofWithStringShouldCreateTransformer() {
            XsltTransformer transformer = XsltTransformer.of(XSLT);

            assertThat(transformer).isNotNull();
        }

        @Test
        @DisplayName("of with Path should create transformer")
        void ofWithPathShouldCreateTransformer() throws IOException {
            Path xsltFile = Files.createTempFile("style", ".xslt");
            try {
                Files.writeString(xsltFile, XSLT);

                XsltTransformer transformer = XsltTransformer.of(xsltFile);

                assertThat(transformer).isNotNull();
            } finally {
                Files.deleteIfExists(xsltFile);
            }
        }

        @Test
        @DisplayName("of with InputStream should create transformer")
        void ofWithInputStreamShouldCreateTransformer() {
            InputStream is = new ByteArrayInputStream(XSLT.getBytes());

            XsltTransformer transformer = XsltTransformer.of(is);

            assertThat(transformer).isNotNull();
        }
    }

    @Nested
    @DisplayName("Transform Tests")
    class TransformTests {

        @Test
        @DisplayName("transform should transform XML string")
        void transformShouldTransformXmlString() {
            XsltTransformer transformer = XsltTransformer.of(XSLT);

            String result = transformer.transform(SOURCE_XML);

            assertThat(result).contains("<result>");
            assertThat(result).contains("<item>");
            assertThat(result).contains("<name>Java Programming</name>");
        }

        @Test
        @DisplayName("transform should transform XmlDocument")
        void transformShouldTransformXmlDocument() {
            XsltTransformer transformer = XsltTransformer.of(XSLT);
            XmlDocument source = XmlDocument.parse(SOURCE_XML);

            String result = transformer.transform(source);

            assertThat(result).contains("<result>");
        }

        @Test
        @DisplayName("transformFile from path should transform file")
        void transformFileFromPathShouldTransformFile() throws IOException {
            Path xmlFile = Files.createTempFile("source", ".xml");
            try {
                Files.writeString(xmlFile, SOURCE_XML);
                XsltTransformer transformer = XsltTransformer.of(XSLT);

                String result = transformer.transformFile(xmlFile);

                assertThat(result).contains("<result>");
            } finally {
                Files.deleteIfExists(xmlFile);
            }
        }

        @Test
        @DisplayName("transform to output stream should write result")
        void transformToOutputStreamShouldWriteResult() {
            XsltTransformer transformer = XsltTransformer.of(XSLT);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            transformer.transform(SOURCE_XML, baos);

            assertThat(baos.toString()).contains("<result>");
        }

        @Test
        @DisplayName("transform path to path should write file")
        void transformPathToPathShouldWriteFile() throws IOException {
            Path inputFile = Files.createTempFile("input", ".xml");
            Path outputFile = Files.createTempFile("output", ".xml");
            try {
                Files.writeString(inputFile, SOURCE_XML);
                XsltTransformer transformer = XsltTransformer.of(XSLT);

                transformer.transform(inputFile, outputFile);

                String content = Files.readString(outputFile);
                assertThat(content).contains("<result>");
            } finally {
                Files.deleteIfExists(inputFile);
                Files.deleteIfExists(outputFile);
            }
        }
    }

    @Nested
    @DisplayName("TransformToDocument Tests")
    class TransformToDocumentTests {

        @Test
        @DisplayName("transformToDocument should return XmlDocument")
        void transformToDocumentShouldReturnXmlDocument() {
            XsltTransformer transformer = XsltTransformer.of(XSLT);

            XmlDocument result = transformer.transformToDocument(SOURCE_XML);

            assertThat(result).isNotNull();
            assertThat(result.getRoot().getName()).isEqualTo("result");
        }

        @Test
        @DisplayName("transformToDocument from XmlDocument should work")
        void transformToDocumentFromXmlDocumentShouldWork() {
            XsltTransformer transformer = XsltTransformer.of(XSLT);
            XmlDocument source = XmlDocument.parse(SOURCE_XML);

            XmlDocument result = transformer.transformToDocument(source);

            assertThat(result.getRoot().getName()).isEqualTo("result");
        }
    }

    @Nested
    @DisplayName("Parameter Tests")
    class ParameterTests {

        @Test
        @DisplayName("parameter should set XSLT parameter")
        void parameterShouldSetXsltParameter() {
            String xsltWithParam = """
                <?xml version="1.0" encoding="UTF-8"?>
                <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                    <xsl:param name="prefix" select="'default'"/>
                    <xsl:template match="/">
                        <output><xsl:value-of select="$prefix"/>-result</output>
                    </xsl:template>
                </xsl:stylesheet>
                """;

            XsltTransformer transformer = XsltTransformer.of(xsltWithParam)
                .parameter("prefix", "custom");

            String result = transformer.transform("<input/>");

            assertThat(result).contains("custom-result");
        }

        @Test
        @DisplayName("multiple parameters should work")
        void multipleParametersShouldWork() {
            String xsltWithParams = """
                <?xml version="1.0" encoding="UTF-8"?>
                <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                    <xsl:param name="a"/>
                    <xsl:param name="b"/>
                    <xsl:template match="/">
                        <output><xsl:value-of select="concat($a, '-', $b)"/></output>
                    </xsl:template>
                </xsl:stylesheet>
                """;

            XsltTransformer transformer = XsltTransformer.of(xsltWithParams)
                .parameter("a", "first")
                .parameter("b", "second");

            String result = transformer.transform("<input/>");

            assertThat(result).contains("first-second");
        }
    }

    @Nested
    @DisplayName("Output Property Tests")
    class OutputPropertyTests {

        @Test
        @DisplayName("outputProperty should set transformer property")
        void outputPropertyShouldSetTransformerProperty() {
            XsltTransformer transformer = XsltTransformer.of(IDENTITY_XSLT)
                .outputProperty("indent", "yes");

            assertThat(transformer).isNotNull();
        }

        @Test
        @DisplayName("omitDeclaration should omit XML declaration")
        void omitDeclarationShouldOmitXmlDeclaration() {
            XsltTransformer transformer = XsltTransformer.of(IDENTITY_XSLT)
                .outputProperty("omit-xml-declaration", "yes");

            String result = transformer.transform("<root/>");

            assertThat(result).doesNotContain("<?xml");
        }
    }

    @Nested
    @DisplayName("Identity Transform Tests")
    class IdentityTransformTests {

        @Test
        @DisplayName("identity transform should copy XML")
        void identityTransformShouldCopyXml() {
            XsltTransformer transformer = XsltTransformer.of(IDENTITY_XSLT);

            String result = transformer.transform(SOURCE_XML);

            assertThat(result).contains("<catalog>");
            assertThat(result).contains("<book>");
            assertThat(result).contains("<title>Java Programming</title>");
        }
    }
}
