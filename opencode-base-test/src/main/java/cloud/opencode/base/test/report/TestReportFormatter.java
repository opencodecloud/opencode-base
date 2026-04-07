package cloud.opencode.base.test.report;

import cloud.opencode.base.test.report.TestReport.TestResult;
import cloud.opencode.base.test.report.TestReport.TestStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Test Report Formatter
 * 测试报告格式化器
 *
 * <p>Formats test reports for output.</p>
 * <p>格式化测试报告以供输出。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Test report formatting - 测试报告格式化</li>
 *   <li>Text, XML, Markdown output - 文本、XML、Markdown输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String text = TestReportFormatter.toText(report);
 * String xml = TestReportFormatter.toJUnitXml(report);
 * String md = TestReportFormatter.toMarkdown(report);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is the number of test results - 时间复杂度: O(n)，n 为测试结果数量</li>
 *   <li>Space complexity: O(n) for output string buffer - 空间复杂度: O(n) 输出字符串缓冲区</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class TestReportFormatter {

    private TestReportFormatter() {
        // Utility class
    }

    /**
     * Format as plain text
     * 格式化为纯文本
     *
     * @param report the report | 报告
     * @return the formatted text | 格式化文本
     */
    public static String toText(TestReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(60)).append("\n");
        sb.append("TEST REPORT: ").append(report.getName()).append("\n");
        sb.append("=".repeat(60)).append("\n");

        for (TestResult result : report.getResults()) {
            String status = switch (result.status()) {
                case PASSED -> "[PASS]";
                case FAILED -> "[FAIL]";
                case SKIPPED -> "[SKIP]";
            };
            sb.append(String.format("%s %-40s %6dms%n",
                status, result.testName(), result.duration().toMillis()));

            if (result.status() == TestStatus.FAILED && result.error() != null) {
                sb.append("       ERROR: ").append(result.error().getMessage()).append("\n");
            }
            if (result.status() == TestStatus.SKIPPED && result.message() != null) {
                sb.append("       REASON: ").append(result.message()).append("\n");
            }
        }

        sb.append("-".repeat(60)).append("\n");
        sb.append(report.getSummary()).append("\n");
        sb.append("=".repeat(60)).append("\n");

        return sb.toString();
    }

    /**
     * Format as JUnit XML
     * 格式化为JUnit XML
     *
     * @param report the report | 报告
     * @return the XML | XML
     */
    public static String toJUnitXml(TestReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append(String.format("<testsuite name=\"%s\" tests=\"%d\" failures=\"%d\" skipped=\"%d\" time=\"%.3f\">\n",
            escape(report.getName()),
            report.getTotalCount(),
            report.getFailedCount(),
            report.getSkippedCount(),
            report.getTotalDuration().toMillis() / 1000.0));

        for (TestResult result : report.getResults()) {
            sb.append(String.format("  <testcase name=\"%s\" time=\"%.3f\"",
                escape(result.testName()),
                result.duration().toMillis() / 1000.0));

            if (result.status() == TestStatus.PASSED) {
                sb.append("/>\n");
            } else if (result.status() == TestStatus.SKIPPED) {
                sb.append(">\n");
                sb.append(String.format("    <skipped message=\"%s\"/>\n", escape(result.message())));
                sb.append("  </testcase>\n");
            } else {
                sb.append(">\n");
                sb.append(String.format("    <failure message=\"%s\">\n",
                    escape(result.error() != null ? result.error().getMessage() : "Unknown error")));
                if (result.error() != null) {
                    sb.append(escape(getStackTrace(result.error())));
                }
                sb.append("    </failure>\n");
                sb.append("  </testcase>\n");
            }
        }

        sb.append("</testsuite>\n");
        return sb.toString();
    }

    /**
     * Format as markdown
     * 格式化为Markdown
     *
     * @param report the report | 报告
     * @return the markdown | Markdown
     */
    public static String toMarkdown(TestReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Test Report: ").append(escapeMd(report.getName())).append("\n\n");
        sb.append("## Summary\n\n");
        sb.append("| Metric | Value |\n");
        sb.append("|--------|-------|\n");
        sb.append(String.format("| Total | %d |\n", report.getTotalCount()));
        sb.append(String.format("| Passed | %d |\n", report.getPassedCount()));
        sb.append(String.format("| Failed | %d |\n", report.getFailedCount()));
        sb.append(String.format("| Skipped | %d |\n", report.getSkippedCount()));
        sb.append(String.format("| Success Rate | %.1f%% |\n", report.getSuccessRate() * 100));
        sb.append(String.format("| Duration | %dms |\n", report.getTotalDuration().toMillis()));
        sb.append("\n");

        sb.append("## Results\n\n");
        sb.append("| Status | Test | Duration |\n");
        sb.append("|--------|------|----------|\n");

        for (TestResult result : report.getResults()) {
            String emoji = switch (result.status()) {
                case PASSED -> "✅";
                case FAILED -> "❌";
                case SKIPPED -> "⏭️";
            };
            sb.append(String.format("| %s | %s | %dms |\n",
                emoji, escapeMd(result.testName()), result.duration().toMillis()));
        }

        // Add failures section if any
        List<TestResult> failures = report.getResults().stream()
            .filter(r -> r.status() == TestStatus.FAILED)
            .toList();

        if (!failures.isEmpty()) {
            sb.append("\n## Failures\n\n");
            for (TestResult result : failures) {
                sb.append("### ").append(escapeMd(result.testName())).append("\n\n");
                if (result.error() != null) {
                    sb.append("```\n");
                    sb.append(escapeMd(result.error().getMessage())).append("\n");
                    sb.append("```\n\n");
                }
            }
        }

        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    private static String escapeMd(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("|", "\\|")
            .replace("`", "\\`");
    }

    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
