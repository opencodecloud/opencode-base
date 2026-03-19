package cloud.opencode.base.test.report;

import cloud.opencode.base.test.report.TestReport.TestResult;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * TestReportFormatterTest Tests
 * TestReportFormatterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("TestReportFormatter Tests")
class TestReportFormatterTest {

    private TestReport createTestReport() {
        TestReport report = new TestReport("TestSuite");
        report.addPassed("testPassed", Duration.ofMillis(100));
        report.addFailed("testFailed", Duration.ofMillis(200), new RuntimeException("Test error"));
        report.addSkipped("testSkipped", "Not implemented");
        report.complete();
        return report;
    }

    @Nested
    @DisplayName("toText Tests")
    class ToTextTests {

        @Test
        @DisplayName("Should format report as plain text")
        void shouldFormatReportAsPlainText() {
            TestReport report = createTestReport();
            String text = TestReportFormatter.toText(report);

            assertThat(text).contains("TEST REPORT: TestSuite");
            assertThat(text).contains("[PASS]");
            assertThat(text).contains("[FAIL]");
            assertThat(text).contains("[SKIP]");
        }

        @Test
        @DisplayName("Should include test names")
        void shouldIncludeTestNames() {
            TestReport report = createTestReport();
            String text = TestReportFormatter.toText(report);

            assertThat(text).contains("testPassed");
            assertThat(text).contains("testFailed");
            assertThat(text).contains("testSkipped");
        }

        @Test
        @DisplayName("Should include error message for failures")
        void shouldIncludeErrorMessageForFailures() {
            TestReport report = createTestReport();
            String text = TestReportFormatter.toText(report);

            assertThat(text).contains("ERROR:");
            assertThat(text).contains("Test error");
        }

        @Test
        @DisplayName("Should include skip reason")
        void shouldIncludeSkipReason() {
            TestReport report = createTestReport();
            String text = TestReportFormatter.toText(report);

            assertThat(text).contains("REASON:");
            assertThat(text).contains("Not implemented");
        }

        @Test
        @DisplayName("Should include summary")
        void shouldIncludeSummary() {
            TestReport report = createTestReport();
            String text = TestReportFormatter.toText(report);

            assertThat(text).contains("TestReport[TestSuite]");
        }

        @Test
        @DisplayName("Should include duration")
        void shouldIncludeDuration() {
            TestReport report = createTestReport();
            String text = TestReportFormatter.toText(report);

            assertThat(text).contains("ms");
        }

        @Test
        @DisplayName("Should handle empty report")
        void shouldHandleEmptyReport() {
            TestReport report = new TestReport("EmptyTests");
            report.complete();
            String text = TestReportFormatter.toText(report);

            assertThat(text).contains("TEST REPORT: EmptyTests");
            assertThat(text).contains("0 tests");
        }
    }

    @Nested
    @DisplayName("toJUnitXml Tests")
    class ToJUnitXmlTests {

        @Test
        @DisplayName("Should format report as JUnit XML")
        void shouldFormatReportAsJUnitXml() {
            TestReport report = createTestReport();
            String xml = TestReportFormatter.toJUnitXml(report);

            assertThat(xml).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            assertThat(xml).contains("<testsuite");
            assertThat(xml).contains("name=\"TestSuite\"");
            assertThat(xml).contains("</testsuite>");
        }

        @Test
        @DisplayName("Should include test counts")
        void shouldIncludeTestCounts() {
            TestReport report = createTestReport();
            String xml = TestReportFormatter.toJUnitXml(report);

            assertThat(xml).contains("tests=\"3\"");
            assertThat(xml).contains("failures=\"1\"");
            assertThat(xml).contains("skipped=\"1\"");
        }

        @Test
        @DisplayName("Should include passed test case")
        void shouldIncludePassedTestCase() {
            TestReport report = createTestReport();
            String xml = TestReportFormatter.toJUnitXml(report);

            assertThat(xml).contains("<testcase name=\"testPassed\"");
            assertThat(xml).contains("/>");
        }

        @Test
        @DisplayName("Should include failed test case with failure element")
        void shouldIncludeFailedTestCaseWithFailureElement() {
            TestReport report = createTestReport();
            String xml = TestReportFormatter.toJUnitXml(report);

            assertThat(xml).contains("<testcase name=\"testFailed\"");
            assertThat(xml).contains("<failure message=\"Test error\">");
            assertThat(xml).contains("</failure>");
        }

        @Test
        @DisplayName("Should include skipped test case")
        void shouldIncludeSkippedTestCase() {
            TestReport report = createTestReport();
            String xml = TestReportFormatter.toJUnitXml(report);

            assertThat(xml).contains("<testcase name=\"testSkipped\"");
            assertThat(xml).contains("<skipped message=\"Not implemented\"/>");
        }

        @Test
        @DisplayName("Should escape XML special characters")
        void shouldEscapeXmlSpecialCharacters() {
            TestReport report = new TestReport("Test<Suite>");
            report.addFailed("test&name", Duration.ofMillis(100),
                new RuntimeException("Error with <xml> & \"quotes\""));
            report.complete();

            String xml = TestReportFormatter.toJUnitXml(report);

            assertThat(xml).contains("Test&lt;Suite&gt;");
            assertThat(xml).contains("test&amp;name");
        }

        @Test
        @DisplayName("Should include time attribute")
        void shouldIncludeTimeAttribute() {
            TestReport report = createTestReport();
            String xml = TestReportFormatter.toJUnitXml(report);

            assertThat(xml).contains("time=\"");
        }

        @Test
        @DisplayName("Should handle null error message")
        void shouldHandleNullErrorMessage() {
            TestReport report = new TestReport("Tests");
            report.add(new TestResult("test", TestReport.TestStatus.FAILED, Duration.ofMillis(100), null, null));
            report.complete();

            String xml = TestReportFormatter.toJUnitXml(report);
            assertThat(xml).contains("<failure message=\"Unknown error\">");
        }
    }

    @Nested
    @DisplayName("toMarkdown Tests")
    class ToMarkdownTests {

        @Test
        @DisplayName("Should format report as Markdown")
        void shouldFormatReportAsMarkdown() {
            TestReport report = createTestReport();
            String md = TestReportFormatter.toMarkdown(report);

            assertThat(md).contains("# Test Report: TestSuite");
            assertThat(md).contains("## Summary");
            assertThat(md).contains("## Results");
        }

        @Test
        @DisplayName("Should include summary table")
        void shouldIncludeSummaryTable() {
            TestReport report = createTestReport();
            String md = TestReportFormatter.toMarkdown(report);

            assertThat(md).contains("| Metric | Value |");
            assertThat(md).contains("| Total |");
            assertThat(md).contains("| Passed |");
            assertThat(md).contains("| Failed |");
            assertThat(md).contains("| Skipped |");
            assertThat(md).contains("| Success Rate |");
            assertThat(md).contains("| Duration |");
        }

        @Test
        @DisplayName("Should include results table")
        void shouldIncludeResultsTable() {
            TestReport report = createTestReport();
            String md = TestReportFormatter.toMarkdown(report);

            assertThat(md).contains("| Status | Test | Duration |");
            assertThat(md).contains("testPassed");
            assertThat(md).contains("testFailed");
            assertThat(md).contains("testSkipped");
        }

        @Test
        @DisplayName("Should include status emojis")
        void shouldIncludeStatusEmojis() {
            TestReport report = createTestReport();
            String md = TestReportFormatter.toMarkdown(report);

            assertThat(md).containsAnyOf("✅", "❌", "⏭️");
        }

        @Test
        @DisplayName("Should include failures section when there are failures")
        void shouldIncludeFailuresSectionWhenThereAreFailures() {
            TestReport report = createTestReport();
            String md = TestReportFormatter.toMarkdown(report);

            assertThat(md).contains("## Failures");
            assertThat(md).contains("### testFailed");
            assertThat(md).contains("```");
            assertThat(md).contains("Test error");
        }

        @Test
        @DisplayName("Should not include failures section when all passed")
        void shouldNotIncludeFailuresSectionWhenAllPassed() {
            TestReport report = new TestReport("PassingTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.addPassed("test2", Duration.ofMillis(100));
            report.complete();

            String md = TestReportFormatter.toMarkdown(report);

            assertThat(md).doesNotContain("## Failures");
        }

        @Test
        @DisplayName("Should handle empty report")
        void shouldHandleEmptyReport() {
            TestReport report = new TestReport("EmptyTests");
            report.complete();

            String md = TestReportFormatter.toMarkdown(report);

            assertThat(md).contains("# Test Report: EmptyTests");
            assertThat(md).contains("| Total | 0 |");
        }
    }
}
