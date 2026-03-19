package cloud.opencode.base.test.report;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Report Generator - Generates test reports in various formats
 * 报告生成器 - 以各种格式生成测试报告
 *
 * <p>Generates test reports in text, HTML, and JSON formats.</p>
 * <p>以文本、HTML和JSON格式生成测试报告。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Text report generation - 文本报告生成</li>
 *   <li>HTML report with styled output - 带样式的HTML报告</li>
 *   <li>JSON report for programmatic consumption - JSON报告用于程序化消费</li>
 *   <li>File output support - 文件输出支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TestReport report = new TestReport("MyTests", 10, 8, 2, Duration.ofSeconds(5));
 *
 * // Generate text report
 * String text = ReportGenerator.toText(report);
 *
 * // Generate HTML report
 * String html = ReportGenerator.toHtml(report);
 *
 * // Write to file
 * ReportGenerator.writeHtml(report, Path.of("report.html"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (validates non-null report) - 空值安全: 是（验证非空报告）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class ReportGenerator {

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private ReportGenerator() {
    }

    // ============ Text Format | 文本格式 ============

    /**
     * Generates text report.
     * 生成文本报告。
     *
     * @param report the test report | 测试报告
     * @return the text | 文本
     */
    public static String toText(TestReport report) {
        Objects.requireNonNull(report, "report cannot be null");
        StringWriter writer = new StringWriter();
        writeText(report, writer);
        return writer.toString();
    }

    /**
     * Writes text report to writer.
     * 将文本报告写入writer。
     *
     * @param report the test report | 测试报告
     * @param writer the writer | writer
     */
    public static void writeText(TestReport report, Writer writer) {
        try {
            writer.write("=".repeat(60) + "\n");
            writer.write("TEST REPORT: " + report.suiteName() + "\n");
            writer.write("=".repeat(60) + "\n");
            writer.write("\n");
            writer.write(String.format("Generated: %s%n", formatTimestamp(report.timestamp())));
            writer.write("\n");
            writer.write("SUMMARY\n");
            writer.write("-".repeat(40) + "\n");
            writer.write(String.format("Total Tests:  %d%n", report.totalTests()));
            writer.write(String.format("Passed:       %d%n", report.passedTests()));
            writer.write(String.format("Failed:       %d%n", report.failedTests()));
            writer.write(String.format("Skipped:      %d%n", report.skippedTests()));
            writer.write(String.format("Success Rate: %.1f%%%n", report.successRate() * 100));
            writer.write(String.format("Duration:     %s%n", formatDuration(report.durationMs())));
            writer.write("\n");

            if (!report.testCases().isEmpty()) {
                writer.write("TEST CASES\n");
                writer.write("-".repeat(60) + "\n");
                for (TestReport.TestResult tc : report.testCases()) {
                    String status = tc.passed() ? "[PASS]" : "[FAIL]";
                    writer.write(String.format("%s %s (%.3fs)%n", status, tc.name(), tc.durationMs() / 1000.0));
                    if (!tc.passed() && tc.errorMessage() != null) {
                        writer.write(String.format("       Error: %s%n", tc.errorMessage()));
                    }
                }
            }

            writer.write("=".repeat(60) + "\n");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write text report", e);
        }
    }

    // ============ HTML Format | HTML格式 ============

    /**
     * Generates HTML report.
     * 生成HTML报告。
     *
     * @param report the test report | 测试报告
     * @return the HTML | HTML
     */
    public static String toHtml(TestReport report) {
        Objects.requireNonNull(report, "report cannot be null");
        StringWriter writer = new StringWriter();
        writeHtml(report, writer);
        return writer.toString();
    }

    /**
     * Writes HTML report to writer.
     * 将HTML报告写入writer。
     *
     * @param report the test report | 测试报告
     * @param writer the writer | writer
     */
    public static void writeHtml(TestReport report, Writer writer) {
        try {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html lang=\"en\">\n");
            writer.write("<head>\n");
            writer.write("  <meta charset=\"UTF-8\">\n");
            writer.write("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            writer.write("  <title>Test Report: " + escapeHtml(report.suiteName()) + "</title>\n");
            writer.write("  <style>\n");
            writer.write(getStyles());
            writer.write("  </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("  <div class=\"container\">\n");
            writer.write("    <h1>Test Report: " + escapeHtml(report.suiteName()) + "</h1>\n");
            writer.write("    <p class=\"timestamp\">Generated: " + formatTimestamp(report.timestamp()) + "</p>\n");

            // Summary
            writer.write("    <div class=\"summary\">\n");
            writer.write("      <div class=\"stat\">\n");
            writer.write("        <span class=\"label\">Total</span>\n");
            writer.write("        <span class=\"value\">" + report.totalTests() + "</span>\n");
            writer.write("      </div>\n");
            writer.write("      <div class=\"stat passed\">\n");
            writer.write("        <span class=\"label\">Passed</span>\n");
            writer.write("        <span class=\"value\">" + report.passedTests() + "</span>\n");
            writer.write("      </div>\n");
            writer.write("      <div class=\"stat failed\">\n");
            writer.write("        <span class=\"label\">Failed</span>\n");
            writer.write("        <span class=\"value\">" + report.failedTests() + "</span>\n");
            writer.write("      </div>\n");
            writer.write("      <div class=\"stat\">\n");
            writer.write("        <span class=\"label\">Success Rate</span>\n");
            writer.write("        <span class=\"value\">" + String.format("%.1f%%", report.successRate() * 100) + "</span>\n");
            writer.write("      </div>\n");
            writer.write("    </div>\n");

            // Progress bar
            int passPercent = (int) (report.successRate() * 100);
            writer.write("    <div class=\"progress\">\n");
            writer.write("      <div class=\"progress-bar\" style=\"width: " + passPercent + "%\"></div>\n");
            writer.write("    </div>\n");

            // Test cases
            if (!report.testCases().isEmpty()) {
                writer.write("    <h2>Test Cases</h2>\n");
                writer.write("    <table>\n");
                writer.write("      <tr><th>Status</th><th>Name</th><th>Duration</th><th>Message</th></tr>\n");
                for (TestReport.TestResult tc : report.testCases()) {
                    String statusClass = tc.passed() ? "pass" : "fail";
                    String status = tc.passed() ? "PASS" : "FAIL";
                    writer.write("      <tr class=\"" + statusClass + "\">\n");
                    writer.write("        <td><span class=\"badge " + statusClass + "\">" + status + "</span></td>\n");
                    writer.write("        <td>" + escapeHtml(tc.name()) + "</td>\n");
                    writer.write("        <td>" + String.format("%.3fs", tc.durationMs() / 1000.0) + "</td>\n");
                    writer.write("        <td>" + escapeHtml(tc.errorMessage() != null ? tc.errorMessage() : "") + "</td>\n");
                    writer.write("      </tr>\n");
                }
                writer.write("    </table>\n");
            }

            writer.write("  </div>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML report", e);
        }
    }

    /**
     * Writes HTML report to file.
     * 将HTML报告写入文件。
     *
     * @param report the test report | 测试报告
     * @param path   the file path | 文件路径
     */
    public static void writeHtml(TestReport report, Path path) {
        try {
            Files.writeString(path, toHtml(report));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML report to file: " + path, e);
        }
    }

    private static String getStyles() {
        return """
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }
            .container { max-width: 900px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            h1 { margin-top: 0; color: #333; }
            h2 { color: #555; margin-top: 30px; }
            .timestamp { color: #888; font-size: 14px; }
            .summary { display: flex; gap: 20px; margin: 20px 0; }
            .stat { flex: 1; padding: 15px; background: #f8f9fa; border-radius: 6px; text-align: center; }
            .stat .label { display: block; font-size: 12px; color: #666; text-transform: uppercase; }
            .stat .value { display: block; font-size: 28px; font-weight: bold; color: #333; }
            .stat.passed .value { color: #28a745; }
            .stat.failed .value { color: #dc3545; }
            .progress { height: 8px; background: #e9ecef; border-radius: 4px; overflow: hidden; margin: 20px 0; }
            .progress-bar { height: 100%; background: linear-gradient(90deg, #28a745, #20c997); }
            table { width: 100%; border-collapse: collapse; margin-top: 15px; }
            th, td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
            th { background: #f8f9fa; font-weight: 600; }
            tr.fail td { background: #fff5f5; }
            .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
            .badge.pass { background: #d4edda; color: #155724; }
            .badge.fail { background: #f8d7da; color: #721c24; }
            """;
    }

    // ============ JSON Format | JSON格式 ============

    /**
     * Generates JSON report.
     * 生成JSON报告。
     *
     * @param report the test report | 测试报告
     * @return the JSON | JSON
     */
    public static String toJson(TestReport report) {
        Objects.requireNonNull(report, "report cannot be null");
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"suiteName\": \"").append(escapeJson(report.suiteName())).append("\",\n");
        sb.append("  \"timestamp\": \"").append(report.timestamp()).append("\",\n");
        sb.append("  \"totalTests\": ").append(report.totalTests()).append(",\n");
        sb.append("  \"passedTests\": ").append(report.passedTests()).append(",\n");
        sb.append("  \"failedTests\": ").append(report.failedTests()).append(",\n");
        sb.append("  \"skippedTests\": ").append(report.skippedTests()).append(",\n");
        sb.append("  \"successRate\": ").append(String.format("%.4f", report.successRate())).append(",\n");
        sb.append("  \"durationMs\": ").append(report.durationMs()).append(",\n");
        sb.append("  \"testCases\": [\n");

        List<TestReport.TestResult> cases = report.testCases();
        for (int i = 0; i < cases.size(); i++) {
            TestReport.TestResult tc = cases.get(i);
            sb.append("    {\n");
            sb.append("      \"name\": \"").append(escapeJson(tc.name())).append("\",\n");
            sb.append("      \"passed\": ").append(tc.passed()).append(",\n");
            sb.append("      \"durationMs\": ").append(tc.durationMs());
            if (tc.errorMessage() != null) {
                sb.append(",\n      \"errorMessage\": \"").append(escapeJson(tc.errorMessage())).append("\"");
            }
            sb.append("\n    }");
            if (i < cases.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Writes JSON report to file.
     * 将JSON报告写入文件。
     *
     * @param report the test report | 测试报告
     * @param path   the file path | 文件路径
     */
    public static void writeJson(TestReport report, Path path) {
        try {
            Files.writeString(path, toJson(report));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON report to file: " + path, e);
        }
    }

    // ============ Helpers | 辅助方法 ============

    private static String formatTimestamp(Instant timestamp) {
        return DATE_FORMAT.format(timestamp);
    }

    private static String formatDuration(long ms) {
        if (ms < 1000) {
            return ms + "ms";
        } else if (ms < 60000) {
            return String.format("%.2fs", ms / 1000.0);
        } else {
            long minutes = ms / 60000;
            long seconds = (ms % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
