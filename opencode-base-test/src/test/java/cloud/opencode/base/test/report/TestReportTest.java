package cloud.opencode.base.test.report;

import cloud.opencode.base.test.report.TestReport.TestResult;
import cloud.opencode.base.test.report.TestReport.TestStatus;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * TestReportTest Tests
 * TestReportTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("TestReport Tests")
class TestReportTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create report with name")
        void shouldCreateReportWithName() {
            TestReport report = new TestReport("MyTests");
            assertThat(report.getName()).isEqualTo("MyTests");
        }

        @Test
        @DisplayName("Should initialize with empty results")
        void shouldInitializeWithEmptyResults() {
            TestReport report = new TestReport("MyTests");
            assertThat(report.getResults()).isEmpty();
            assertThat(report.getTotalCount()).isZero();
        }
    }

    @Nested
    @DisplayName("add Tests")
    class AddTests {

        @Test
        @DisplayName("Should add test result")
        void shouldAddTestResult() {
            TestReport report = new TestReport("MyTests");
            TestResult result = TestResult.passed("test1", Duration.ofMillis(100));

            report.add(result);

            assertThat(report.getResults()).hasSize(1);
            assertThat(report.getResults().get(0).testName()).isEqualTo("test1");
        }
    }

    @Nested
    @DisplayName("addPassed Tests")
    class AddPassedTests {

        @Test
        @DisplayName("Should add passed test")
        void shouldAddPassedTest() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));

            assertThat(report.getPassedCount()).isEqualTo(1);
            assertThat(report.getResults().get(0).passed()).isTrue();
        }
    }

    @Nested
    @DisplayName("addFailed Tests")
    class AddFailedTests {

        @Test
        @DisplayName("Should add failed test")
        void shouldAddFailedTest() {
            TestReport report = new TestReport("MyTests");
            report.addFailed("test1", Duration.ofMillis(100), new RuntimeException("Error"));

            assertThat(report.getFailedCount()).isEqualTo(1);
            assertThat(report.getResults().get(0).passed()).isFalse();
            assertThat(report.getResults().get(0).errorMessage()).isEqualTo("Error");
        }
    }

    @Nested
    @DisplayName("addSkipped Tests")
    class AddSkippedTests {

        @Test
        @DisplayName("Should add skipped test")
        void shouldAddSkippedTest() {
            TestReport report = new TestReport("MyTests");
            report.addSkipped("test1", "Not implemented");

            assertThat(report.getSkippedCount()).isEqualTo(1);
            assertThat(report.getResults().get(0).status()).isEqualTo(TestStatus.SKIPPED);
            assertThat(report.getResults().get(0).message()).isEqualTo("Not implemented");
        }
    }

    @Nested
    @DisplayName("complete Tests")
    class CompleteTests {

        @Test
        @DisplayName("Should mark report as complete")
        void shouldMarkReportAsComplete() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.complete();

            // Duration should now be fixed
            Duration duration1 = report.getTotalDuration();
            Duration duration2 = report.getTotalDuration();
            assertThat(duration1).isEqualTo(duration2);
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {

        @Test
        @DisplayName("Should return correct total count")
        void shouldReturnCorrectTotalCount() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.addFailed("test2", Duration.ofMillis(100), new RuntimeException("Error"));
            report.addSkipped("test3", "Reason");

            assertThat(report.getTotalCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return correct passed count")
        void shouldReturnCorrectPassedCount() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.addPassed("test2", Duration.ofMillis(100));
            report.addFailed("test3", Duration.ofMillis(100), new RuntimeException("Error"));

            assertThat(report.getPassedCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return correct failed count")
        void shouldReturnCorrectFailedCount() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.addFailed("test2", Duration.ofMillis(100), new RuntimeException("Error"));
            report.addFailed("test3", Duration.ofMillis(100), new RuntimeException("Error"));

            assertThat(report.getFailedCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return correct skipped count")
        void shouldReturnCorrectSkippedCount() {
            TestReport report = new TestReport("MyTests");
            report.addSkipped("test1", "Reason1");
            report.addSkipped("test2", "Reason2");
            report.addPassed("test3", Duration.ofMillis(100));

            assertThat(report.getSkippedCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("allPassed Tests")
    class AllPassedTests {

        @Test
        @DisplayName("Should return true when all passed")
        void shouldReturnTrueWhenAllPassed() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.addPassed("test2", Duration.ofMillis(100));

            assertThat(report.allPassed()).isTrue();
        }

        @Test
        @DisplayName("Should return false when any failed")
        void shouldReturnFalseWhenAnyFailed() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.addFailed("test2", Duration.ofMillis(100), new RuntimeException("Error"));

            assertThat(report.allPassed()).isFalse();
        }

        @Test
        @DisplayName("Should return true with only skipped")
        void shouldReturnTrueWithOnlySkipped() {
            TestReport report = new TestReport("MyTests");
            report.addSkipped("test1", "Reason");

            assertThat(report.allPassed()).isTrue();
        }
    }

    @Nested
    @DisplayName("getSuccessRate Tests")
    class GetSuccessRateTests {

        @Test
        @DisplayName("Should return 1.0 when all passed")
        void shouldReturn1WhenAllPassed() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.addPassed("test2", Duration.ofMillis(100));

            assertThat(report.getSuccessRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return 0.0 when all failed")
        void shouldReturn0WhenAllFailed() {
            TestReport report = new TestReport("MyTests");
            report.addFailed("test1", Duration.ofMillis(100), new RuntimeException("Error"));
            report.addFailed("test2", Duration.ofMillis(100), new RuntimeException("Error"));

            assertThat(report.getSuccessRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return correct rate for mixed results")
        void shouldReturnCorrectRateForMixedResults() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.addFailed("test2", Duration.ofMillis(100), new RuntimeException("Error"));

            assertThat(report.getSuccessRate()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("Should exclude skipped from rate calculation")
        void shouldExcludeSkippedFromRateCalculation() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.addSkipped("test2", "Reason");

            assertThat(report.getSuccessRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return 1.0 for empty report")
        void shouldReturn1ForEmptyReport() {
            TestReport report = new TestReport("MyTests");
            assertThat(report.getSuccessRate()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("Alias Methods Tests")
    class AliasMethodsTests {

        @Test
        @DisplayName("suiteName should return name")
        void suiteNameShouldReturnName() {
            TestReport report = new TestReport("MyTests");
            assertThat(report.suiteName()).isEqualTo("MyTests");
        }

        @Test
        @DisplayName("timestamp should return non-null")
        void timestampShouldReturnNonNull() {
            TestReport report = new TestReport("MyTests");
            assertThat(report.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("totalTests should return total count")
        void totalTestsShouldReturnTotalCount() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            assertThat(report.totalTests()).isEqualTo(1);
        }

        @Test
        @DisplayName("passedTests should return passed count")
        void passedTestsShouldReturnPassedCount() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            assertThat(report.passedTests()).isEqualTo(1);
        }

        @Test
        @DisplayName("failedTests should return failed count")
        void failedTestsShouldReturnFailedCount() {
            TestReport report = new TestReport("MyTests");
            report.addFailed("test1", Duration.ofMillis(100), new RuntimeException("Error"));
            assertThat(report.failedTests()).isEqualTo(1);
        }

        @Test
        @DisplayName("skippedTests should return skipped count")
        void skippedTestsShouldReturnSkippedCount() {
            TestReport report = new TestReport("MyTests");
            report.addSkipped("test1", "Reason");
            assertThat(report.skippedTests()).isEqualTo(1);
        }

        @Test
        @DisplayName("successRate should return success rate")
        void successRateShouldReturnSuccessRate() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            assertThat(report.successRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("durationMs should return duration in milliseconds")
        void durationMsShouldReturnDurationInMilliseconds() {
            TestReport report = new TestReport("MyTests");
            assertThat(report.durationMs()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("testCases should return results")
        void testCasesShouldReturnResults() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            assertThat(report.testCases()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getSummary Tests")
    class GetSummaryTests {

        @Test
        @DisplayName("Should return summary string")
        void shouldReturnSummaryString() {
            TestReport report = new TestReport("MyTests");
            report.addPassed("test1", Duration.ofMillis(100));
            report.addFailed("test2", Duration.ofMillis(100), new RuntimeException("Error"));

            String summary = report.getSummary();
            assertThat(summary).contains("MyTests");
            assertThat(summary).contains("2 tests");
            assertThat(summary).contains("1 passed");
            assertThat(summary).contains("1 failed");
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return summary")
        void shouldReturnSummary() {
            TestReport report = new TestReport("MyTests");
            assertThat(report.toString()).isEqualTo(report.getSummary());
        }
    }

    @Nested
    @DisplayName("TestResult Tests")
    class TestResultTests {

        @Test
        @DisplayName("passed should create passed result")
        void passedShouldCreatePassedResult() {
            TestResult result = TestResult.passed("test1", Duration.ofMillis(100));

            assertThat(result.testName()).isEqualTo("test1");
            assertThat(result.status()).isEqualTo(TestStatus.PASSED);
            assertThat(result.passed()).isTrue();
            assertThat(result.duration()).isEqualTo(Duration.ofMillis(100));
            assertThat(result.error()).isNull();
            assertThat(result.message()).isNull();
        }

        @Test
        @DisplayName("failed should create failed result")
        void failedShouldCreateFailedResult() {
            RuntimeException error = new RuntimeException("Test error");
            TestResult result = TestResult.failed("test1", Duration.ofMillis(100), error);

            assertThat(result.testName()).isEqualTo("test1");
            assertThat(result.status()).isEqualTo(TestStatus.FAILED);
            assertThat(result.passed()).isFalse();
            assertThat(result.error()).isEqualTo(error);
            assertThat(result.errorMessage()).isEqualTo("Test error");
        }

        @Test
        @DisplayName("skipped should create skipped result")
        void skippedShouldCreateSkippedResult() {
            TestResult result = TestResult.skipped("test1", "Not implemented");

            assertThat(result.testName()).isEqualTo("test1");
            assertThat(result.status()).isEqualTo(TestStatus.SKIPPED);
            assertThat(result.passed()).isFalse();
            assertThat(result.duration()).isEqualTo(Duration.ZERO);
            assertThat(result.message()).isEqualTo("Not implemented");
        }

        @Test
        @DisplayName("name should return testName")
        void nameShouldReturnTestName() {
            TestResult result = TestResult.passed("test1", Duration.ofMillis(100));
            assertThat(result.name()).isEqualTo("test1");
        }

        @Test
        @DisplayName("durationMs should return duration in milliseconds")
        void durationMsShouldReturnDurationInMilliseconds() {
            TestResult result = TestResult.passed("test1", Duration.ofMillis(150));
            assertThat(result.durationMs()).isEqualTo(150);
        }

        @Test
        @DisplayName("durationMs should return 0 for null duration")
        void durationMsShouldReturn0ForNullDuration() {
            TestResult result = new TestResult("test1", TestStatus.PASSED, null, null, null);
            assertThat(result.durationMs()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("TestStatus Tests")
    class TestStatusTests {

        @Test
        @DisplayName("Should have all expected values")
        void shouldHaveAllExpectedValues() {
            assertThat(TestStatus.values()).containsExactly(
                TestStatus.PASSED, TestStatus.FAILED, TestStatus.SKIPPED
            );
        }
    }
}
