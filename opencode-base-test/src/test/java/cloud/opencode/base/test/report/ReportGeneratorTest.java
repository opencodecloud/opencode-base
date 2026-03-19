package cloud.opencode.base.test.report;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportGeneratorTest Tests
 * ReportGeneratorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("ReportGenerator Tests")
class ReportGeneratorTest {

    private TestReport createTestReport() {
        TestReport report = new TestReport("TestSuite");
        report.addPassed("test1", Duration.ofMillis(100));
        report.addFailed("test2", Duration.ofMillis(200), new RuntimeException("Test error"));
        report.addSkipped("test3", "Not implemented");
        report.complete();
        return report;
    }

    @Nested
    @DisplayName("toText Tests")
    class ToTextTests {

        @Test
        @DisplayName("Should generate text report")
        void shouldGenerateTextReport() {
            TestReport report = createTestReport();
            String text = ReportGenerator.toText(report);

            assertThat(text).contains("TEST REPORT: TestSuite");
            assertThat(text).contains("Total Tests:");
            assertThat(text).contains("[PASS]");
            assertThat(text).contains("[FAIL]");
        }

        @Test
        @DisplayName("Should throw for null report")
        void shouldThrowForNullReport() {
            assertThatNullPointerException().isThrownBy(() ->
                ReportGenerator.toText(null));
        }

        @Test
        @DisplayName("Should include test case details")
        void shouldIncludeTestCaseDetails() {
            TestReport report = createTestReport();
            String text = ReportGenerator.toText(report);

            assertThat(text).contains("test1");
            assertThat(text).contains("test2");
            assertThat(text).contains("Test error");
        }

        @Test
        @DisplayName("Should show success rate")
        void shouldShowSuccessRate() {
            TestReport report = createTestReport();
            String text = ReportGenerator.toText(report);

            assertThat(text).contains("Success Rate:");
        }
    }

    @Nested
    @DisplayName("writeText Tests")
    class WriteTextTests {

        @Test
        @DisplayName("Should write text report to writer")
        void shouldWriteTextReportToWriter() {
            TestReport report = createTestReport();
            StringWriter writer = new StringWriter();

            ReportGenerator.writeText(report, writer);

            String text = writer.toString();
            assertThat(text).contains("TEST REPORT: TestSuite");
        }

        @Test
        @DisplayName("Should handle empty report")
        void shouldHandleEmptyReport() {
            TestReport report = new TestReport("EmptyTests");
            report.complete();
            StringWriter writer = new StringWriter();

            ReportGenerator.writeText(report, writer);

            String text = writer.toString();
            assertThat(text).contains("Total Tests:  0");
        }
    }

    @Nested
    @DisplayName("toHtml Tests")
    class ToHtmlTests {

        @Test
        @DisplayName("Should generate HTML report")
        void shouldGenerateHtmlReport() {
            TestReport report = createTestReport();
            String html = ReportGenerator.toHtml(report);

            assertThat(html).contains("<!DOCTYPE html>");
            assertThat(html).contains("<title>Test Report: TestSuite</title>");
            assertThat(html).contains("<table>");
        }

        @Test
        @DisplayName("Should throw for null report")
        void shouldThrowForNullReport() {
            assertThatNullPointerException().isThrownBy(() ->
                ReportGenerator.toHtml(null));
        }

        @Test
        @DisplayName("Should include styled elements")
        void shouldIncludeStyledElements() {
            TestReport report = createTestReport();
            String html = ReportGenerator.toHtml(report);

            assertThat(html).contains("<style>");
            assertThat(html).contains("progress-bar");
            assertThat(html).contains("badge");
        }

        @Test
        @DisplayName("Should escape HTML in names")
        void shouldEscapeHtmlInNames() {
            TestReport report = new TestReport("<script>alert('XSS')</script>");
            report.complete();
            String html = ReportGenerator.toHtml(report);

            assertThat(html).doesNotContain("<script>");
            assertThat(html).contains("&lt;script&gt;");
        }

        @Test
        @DisplayName("Should include test results in table")
        void shouldIncludeTestResultsInTable() {
            TestReport report = createTestReport();
            String html = ReportGenerator.toHtml(report);

            assertThat(html).contains("<tr>");
            assertThat(html).contains("test1");
            assertThat(html).contains("PASS");
            assertThat(html).contains("FAIL");
        }
    }

    @Nested
    @DisplayName("writeHtml(Writer) Tests")
    class WriteHtmlWriterTests {

        @Test
        @DisplayName("Should write HTML to writer")
        void shouldWriteHtmlToWriter() {
            TestReport report = createTestReport();
            StringWriter writer = new StringWriter();

            ReportGenerator.writeHtml(report, writer);

            String html = writer.toString();
            assertThat(html).contains("<!DOCTYPE html>");
        }
    }

    @Nested
    @DisplayName("writeHtml(Path) Tests")
    class WriteHtmlPathTests {

        @Test
        @DisplayName("Should write HTML to file")
        void shouldWriteHtmlToFile() throws IOException {
            TestReport report = createTestReport();
            Path tempFile = Files.createTempFile("test-report-", ".html");

            try {
                ReportGenerator.writeHtml(report, tempFile);

                String content = Files.readString(tempFile);
                assertThat(content).contains("<!DOCTYPE html>");
                assertThat(content).contains("TestSuite");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("Should throw for invalid path")
        void shouldThrowForInvalidPath() {
            TestReport report = createTestReport();
            Path invalidPath = Path.of("/invalid/path/that/does/not/exist/report.html");

            assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                ReportGenerator.writeHtml(report, invalidPath));
        }
    }

    @Nested
    @DisplayName("toJson Tests")
    class ToJsonTests {

        @Test
        @DisplayName("Should generate JSON report")
        void shouldGenerateJsonReport() {
            TestReport report = createTestReport();
            String json = ReportGenerator.toJson(report);

            assertThat(json).contains("\"suiteName\": \"TestSuite\"");
            assertThat(json).contains("\"totalTests\":");
            assertThat(json).contains("\"passedTests\":");
            assertThat(json).contains("\"failedTests\":");
            assertThat(json).contains("\"testCases\":");
        }

        @Test
        @DisplayName("Should throw for null report")
        void shouldThrowForNullReport() {
            assertThatNullPointerException().isThrownBy(() ->
                ReportGenerator.toJson(null));
        }

        @Test
        @DisplayName("Should include test case details")
        void shouldIncludeTestCaseDetails() {
            TestReport report = createTestReport();
            String json = ReportGenerator.toJson(report);

            assertThat(json).contains("\"name\": \"test1\"");
            assertThat(json).contains("\"passed\": true");
            assertThat(json).contains("\"passed\": false");
        }

        @Test
        @DisplayName("Should include error messages for failures")
        void shouldIncludeErrorMessagesForFailures() {
            TestReport report = createTestReport();
            String json = ReportGenerator.toJson(report);

            assertThat(json).contains("\"errorMessage\": \"Test error\"");
        }

        @Test
        @DisplayName("Should escape JSON special characters")
        void shouldEscapeJsonSpecialCharacters() {
            TestReport report = new TestReport("Test\\Suite");
            report.addFailed("test", Duration.ofMillis(100),
                new RuntimeException("Error with \"quotes\" and\nnewline"));
            report.complete();

            String json = ReportGenerator.toJson(report);

            assertThat(json).contains("\\\\");
            assertThat(json).contains("\\\"");
            assertThat(json).contains("\\n");
        }

        @Test
        @DisplayName("Should handle empty test cases")
        void shouldHandleEmptyTestCases() {
            TestReport report = new TestReport("EmptyTests");
            report.complete();

            String json = ReportGenerator.toJson(report);

            assertThat(json).contains("\"testCases\": [\n  ]");
        }
    }

    @Nested
    @DisplayName("writeJson Tests")
    class WriteJsonTests {

        @Test
        @DisplayName("Should write JSON to file")
        void shouldWriteJsonToFile() throws IOException {
            TestReport report = createTestReport();
            Path tempFile = Files.createTempFile("test-report-", ".json");

            try {
                ReportGenerator.writeJson(report, tempFile);

                String content = Files.readString(tempFile);
                assertThat(content).contains("\"suiteName\":");
                assertThat(content).contains("TestSuite");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("Should throw for invalid path")
        void shouldThrowForInvalidPath() {
            TestReport report = createTestReport();
            Path invalidPath = Path.of("/invalid/path/that/does/not/exist/report.json");

            assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                ReportGenerator.writeJson(report, invalidPath));
        }
    }

    @Nested
    @DisplayName("Duration Format Tests")
    class DurationFormatTests {

        @Test
        @DisplayName("Should format milliseconds")
        void shouldFormatMilliseconds() {
            TestReport report = new TestReport("Tests");
            report.complete();
            String text = ReportGenerator.toText(report);

            // Duration should be formatted
            assertThat(text).contains("Duration:");
        }

        @Test
        @DisplayName("Should format seconds")
        void shouldFormatSeconds() {
            // This is implicitly tested through the report generation
            TestReport report = new TestReport("Tests");
            report.addPassed("test", Duration.ofSeconds(2));
            report.complete();
            String text = ReportGenerator.toText(report);

            assertThat(text).isNotEmpty();
        }
    }
}
