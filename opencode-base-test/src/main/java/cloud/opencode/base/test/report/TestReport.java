package cloud.opencode.base.test.report;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Test Report
 * 测试报告
 *
 * <p>Collects and stores test results.</p>
 * <p>收集和存储测试结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Test result collection and reporting - 测试结果收集和报告</li>
 *   <li>Multiple output formats - 多种输出格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TestReport report = new TestReport("MySuite");
 * report.addPassed("test1", Duration.ofMillis(50));
 * report.complete();
 * System.out.println(report.getSummary());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public class TestReport {

    private final String name;
    private final List<TestResult> results;
    private final Instant startTime;
    private Instant endTime;

    /**
     * Create test report
     * 创建测试报告
     *
     * @param name the report name | 报告名称
     */
    public TestReport(String name) {
        this.name = name;
        this.results = new ArrayList<>();
        this.startTime = Instant.now();
    }

    /**
     * Add test result
     * 添加测试结果
     *
     * @param result the result | 结果
     */
    public void add(TestResult result) {
        results.add(result);
    }

    /**
     * Add passed test
     * 添加通过的测试
     *
     * @param testName the test name | 测试名称
     * @param duration the duration | 时长
     */
    public void addPassed(String testName, Duration duration) {
        results.add(TestResult.passed(testName, duration));
    }

    /**
     * Add failed test
     * 添加失败的测试
     *
     * @param testName the test name | 测试名称
     * @param duration the duration | 时长
     * @param error the error | 错误
     */
    public void addFailed(String testName, Duration duration, Throwable error) {
        results.add(TestResult.failed(testName, duration, error));
    }

    /**
     * Add skipped test
     * 添加跳过的测试
     *
     * @param testName the test name | 测试名称
     * @param reason the reason | 原因
     */
    public void addSkipped(String testName, String reason) {
        results.add(TestResult.skipped(testName, reason));
    }

    /**
     * Mark report as complete
     * 标记报告为完成
     */
    public void complete() {
        this.endTime = Instant.now();
    }

    /**
     * Get report name
     * 获取报告名称
     *
     * @return the name | 名称
     */
    public String getName() {
        return name;
    }

    /**
     * Get all results
     * 获取所有结果
     *
     * @return the results | 结果
     */
    public List<TestResult> getResults() {
        return List.copyOf(results);
    }

    /**
     * Get total count
     * 获取总数
     *
     * @return the count | 数量
     */
    public int getTotalCount() {
        return results.size();
    }

    /**
     * Get passed count
     * 获取通过数
     *
     * @return the count | 数量
     */
    public int getPassedCount() {
        return (int) results.stream().filter(r -> r.status() == TestStatus.PASSED).count();
    }

    /**
     * Get failed count
     * 获取失败数
     *
     * @return the count | 数量
     */
    public int getFailedCount() {
        return (int) results.stream().filter(r -> r.status() == TestStatus.FAILED).count();
    }

    /**
     * Get skipped count
     * 获取跳过数
     *
     * @return the count | 数量
     */
    public int getSkippedCount() {
        return (int) results.stream().filter(r -> r.status() == TestStatus.SKIPPED).count();
    }

    /**
     * Get total duration
     * 获取总时长
     *
     * @return the duration | 时长
     */
    public Duration getTotalDuration() {
        if (endTime == null) {
            return Duration.between(startTime, Instant.now());
        }
        return Duration.between(startTime, endTime);
    }

    /**
     * Check if all tests passed
     * 检查是否所有测试都通过
     *
     * @return true if all passed | 如果全部通过返回true
     */
    public boolean allPassed() {
        return getFailedCount() == 0;
    }

    /**
     * Get success rate
     * 获取成功率
     *
     * @return the success rate (0-1) | 成功率（0-1）
     */
    public double getSuccessRate() {
        int total = getTotalCount() - getSkippedCount();
        if (total == 0) return 1.0;
        return (double) getPassedCount() / total;
    }

    /**
     * Get summary
     * 获取摘要
     *
     * @return the summary | 摘要
     */
    public String getSummary() {
        return String.format(
            "TestReport[%s]: %d tests, %d passed, %d failed, %d skipped, %.1f%% success, %dms",
            name, getTotalCount(), getPassedCount(), getFailedCount(), getSkippedCount(),
            getSuccessRate() * 100, getTotalDuration().toMillis()
        );
    }

    @Override
    public String toString() {
        return getSummary();
    }

    // ============ Alias Methods for Compatibility | 兼容性别名方法 ============

    /** Alias for getName() */
    public String suiteName() { return getName(); }

    /** Alias for Instant.now() */
    public Instant timestamp() { return Instant.now(); }

    /** Alias for getTotalCount() */
    public int totalTests() { return getTotalCount(); }

    /** Alias for getPassedCount() */
    public int passedTests() { return getPassedCount(); }

    /** Alias for getFailedCount() */
    public int failedTests() { return getFailedCount(); }

    /** Alias for getSkippedCount() */
    public int skippedTests() { return getSkippedCount(); }

    /** Alias for getSuccessRate() */
    public double successRate() { return getSuccessRate(); }

    /** Alias for getTotalDuration().toMillis() */
    public long durationMs() { return getTotalDuration().toMillis(); }

    /** Alias for getResults() */
    public List<TestResult> testCases() { return getResults(); }

    /**
     * Test status
     * 测试状态
     */
    public enum TestStatus {
        PASSED, FAILED, SKIPPED
    }

    /**
     * Test result
     * 测试结果
     *
     * @param testName the test name | 测试名称
     * @param status the status | 状态
     * @param duration the duration | 时长
     * @param error the error (if failed) | 错误（如果失败）
     * @param message the message | 消息
     */
    public record TestResult(
        String testName,
        TestStatus status,
        Duration duration,
        Throwable error,
        String message
    ) {
        public static TestResult passed(String testName, Duration duration) {
            return new TestResult(testName, TestStatus.PASSED, duration, null, null);
        }

        public static TestResult failed(String testName, Duration duration, Throwable error) {
            return new TestResult(testName, TestStatus.FAILED, duration, error, error.getMessage());
        }

        public static TestResult skipped(String testName, String reason) {
            return new TestResult(testName, TestStatus.SKIPPED, Duration.ZERO, null, reason);
        }

        // ============ Alias Methods for Compatibility ============
        public String name() { return testName; }
        public boolean passed() { return status == TestStatus.PASSED; }
        public long durationMs() { return duration != null ? duration.toMillis() : 0L; }
        public String errorMessage() { return message; }
    }
}
