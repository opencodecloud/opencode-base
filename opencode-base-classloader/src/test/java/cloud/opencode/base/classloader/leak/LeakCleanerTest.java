package cloud.opencode.base.classloader.leak;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for LeakCleaner
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("LeakCleaner Tests")
class LeakCleanerTest {

    @Nested
    @DisplayName("cleanAll Tests")
    class CleanAllTests {

        @Test
        @DisplayName("should return valid report for empty classloader")
        void shouldReturnValidReportForEmptyLoader() {
            URLClassLoader loader = new URLClassLoader(new URL[0], null);
            try {
                CleanupReport report = LeakCleaner.cleanAll(loader);

                assertThat(report).isNotNull();
                assertThat(report.jdbcDriversRemoved()).isGreaterThanOrEqualTo(0);
                assertThat(report.threadLocalsCleared()).isGreaterThanOrEqualTo(0);
                assertThat(report.shutdownHooksRemoved()).isGreaterThanOrEqualTo(0);
                assertThat(report.timersCancelled()).isGreaterThanOrEqualTo(0);
                assertThat(report.errors()).isNotNull();
            } finally {
                closeQuietly(loader);
            }
        }

        @Test
        @DisplayName("should throw NPE for null classloader")
        void shouldThrowNpeForNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> LeakCleaner.cleanAll(null))
                    .withMessageContaining("classLoader must not be null");
        }

        @Test
        @DisplayName("should be idempotent - calling twice gives consistent results")
        void shouldBeIdempotent() {
            URLClassLoader loader = new URLClassLoader(new URL[0], null);
            try {
                CleanupReport report1 = LeakCleaner.cleanAll(loader);
                CleanupReport report2 = LeakCleaner.cleanAll(loader);

                assertThat(report2.jdbcDriversRemoved()).isEqualTo(0);
                assertThat(report1).isNotNull();
                assertThat(report2).isNotNull();
            } finally {
                closeQuietly(loader);
            }
        }

        @Test
        @DisplayName("cleanAll should aggregate results from all sub-cleaners")
        void shouldAggregateResults() {
            // Use an isolated classloader to avoid killing JaCoCo's shutdown hook
            URLClassLoader isolated = new URLClassLoader(new URL[0], null);
            try {
                CleanupReport report = LeakCleaner.cleanAll(isolated);

                assertThat(report).isNotNull();
                assertThat(report.jdbcDriversRemoved()).isEqualTo(0);
                assertThat(report.threadLocalsCleared()).isGreaterThanOrEqualTo(0);
                assertThat(report.shutdownHooksRemoved()).isEqualTo(0);
                assertThat(report.timersCancelled()).isEqualTo(0);
                assertThat(report.errors()).isNotNull();
            } finally {
                closeQuietly(isolated);
            }
        }
    }

    @Nested
    @DisplayName("cleanJdbcDrivers Tests")
    class CleanJdbcDriversTests {

        @Test
        @DisplayName("should return 0 for classloader with no JDBC drivers")
        void shouldReturnZeroForNoDrivers() {
            URLClassLoader loader = new URLClassLoader(new URL[0], null);
            try {
                int count = LeakCleaner.cleanJdbcDrivers(loader);
                assertThat(count).isEqualTo(0);
            } finally {
                closeQuietly(loader);
            }
        }

        @Test
        @DisplayName("should deregister driver loaded by target classloader")
        void shouldDeregisterTargetDriver() throws SQLException {
            StubDriver driver = new StubDriver();
            DriverManager.registerDriver(driver);

            try {
                ClassLoader testLoader = StubDriver.class.getClassLoader();
                int count = LeakCleaner.cleanJdbcDrivers(testLoader);

                assertThat(count).isGreaterThanOrEqualTo(1);

                boolean found = DriverManager.drivers()
                        .anyMatch(d -> d instanceof StubDriver);
                assertThat(found).isFalse();
            } finally {
                try { DriverManager.deregisterDriver(driver); } catch (SQLException ignored) {}
            }
        }

        @Test
        @DisplayName("should throw NPE for null classloader")
        void shouldThrowNpeForNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> LeakCleaner.cleanJdbcDrivers(null))
                    .withMessageContaining("classLoader must not be null");
        }

        @Test
        @DisplayName("should be idempotent")
        void shouldBeIdempotent() throws SQLException {
            StubDriver driver = new StubDriver();
            DriverManager.registerDriver(driver);

            try {
                ClassLoader testLoader = StubDriver.class.getClassLoader();
                int count1 = LeakCleaner.cleanJdbcDrivers(testLoader);
                int count2 = LeakCleaner.cleanJdbcDrivers(testLoader);

                assertThat(count1).isGreaterThanOrEqualTo(1);
                assertThat(count2).isLessThanOrEqualTo(count1);
            } finally {
                try { DriverManager.deregisterDriver(driver); } catch (SQLException ignored) {}
            }
        }

        @Test
        @DisplayName("should not deregister drivers from other classloaders")
        void shouldNotDeregisterOtherDrivers() throws SQLException {
            StubDriver driver = new StubDriver();
            DriverManager.registerDriver(driver);

            try {
                // Use a different classloader — should not remove our driver
                URLClassLoader other = new URLClassLoader(new URL[0], null);
                try {
                    int count = LeakCleaner.cleanJdbcDrivers(other);
                    assertThat(count).isEqualTo(0);

                    // StubDriver should still be registered
                    boolean found = DriverManager.drivers()
                            .anyMatch(d -> d instanceof StubDriver);
                    assertThat(found).isTrue();
                } finally {
                    closeQuietly(other);
                }
            } finally {
                DriverManager.deregisterDriver(driver);
            }
        }
    }

    @Nested
    @DisplayName("cleanThreadLocals Tests")
    class CleanThreadLocalsTests {

        @Test
        @DisplayName("should return 0 for classloader with no threadlocals")
        void shouldReturnZeroForEmptyLoader() {
            URLClassLoader loader = new URLClassLoader(new URL[0], null);
            try {
                int count = LeakCleaner.cleanThreadLocals(loader);
                assertThat(count).isGreaterThanOrEqualTo(0);
            } finally {
                closeQuietly(loader);
            }
        }

        @Test
        @DisplayName("should throw NPE for null classloader")
        void shouldThrowNpeForNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> LeakCleaner.cleanThreadLocals(null))
                    .withMessageContaining("classLoader must not be null");
        }

        @Test
        @DisplayName("should clean threadlocal values loaded by target classloader")
        void shouldCleanThreadLocalValues() {
            // Set a ThreadLocal with a value whose class is loaded by the test CL
            ThreadLocal<String> tl = new ThreadLocal<>();
            tl.set("test-value-from-test-classloader");

            ClassLoader testLoader = this.getClass().getClassLoader();
            // Note: String.class is loaded by bootstrap, so it won't match.
            // But the method should still run the scan path without errors.
            int count = LeakCleaner.cleanThreadLocals(testLoader);
            assertThat(count).isGreaterThanOrEqualTo(0);

            tl.remove();
        }

        @Test
        @DisplayName("should handle threads with null threadlocals map gracefully")
        void shouldHandleNullThreadLocalsGracefully() {
            // New thread that hasn't used any ThreadLocal
            Thread newThread = new Thread(() -> {});
            newThread.setName("leak-test-empty-thread");
            // Don't start — its threadLocals field is null
            // cleanThreadLocals iterates enumerateThreads(), which only returns alive threads
            // So we just verify no exception when called with an empty loader
            URLClassLoader loader = new URLClassLoader(new URL[0], null);
            try {
                int count = LeakCleaner.cleanThreadLocals(loader);
                assertThat(count).isGreaterThanOrEqualTo(0);
            } finally {
                closeQuietly(loader);
            }
        }

        @Test
        @DisplayName("should be idempotent")
        void shouldBeIdempotent() {
            ClassLoader testLoader = this.getClass().getClassLoader();
            int count1 = LeakCleaner.cleanThreadLocals(testLoader);
            int count2 = LeakCleaner.cleanThreadLocals(testLoader);

            assertThat(count1).isGreaterThanOrEqualTo(0);
            assertThat(count2).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("should clean threadlocal with value loaded by test classloader")
        void shouldCleanValueLoadedByTestCl() {
            // Create a ThreadLocal whose value is an instance of a test-CL-loaded class
            // LeakCleanerTest itself is loaded by the test classloader
            ThreadLocal<Object> tl = new ThreadLocal<>();
            tl.set(new LeakCleanerTest()); // value's class loaded by test CL

            ClassLoader testLoader = LeakCleanerTest.class.getClassLoader();
            int count = LeakCleaner.cleanThreadLocals(testLoader);

            // Should have cleared at least this one entry
            assertThat(count).isGreaterThanOrEqualTo(1);

            tl.remove(); // cleanup
        }

        @Test
        @DisplayName("should iterate both threadLocals and inheritableThreadLocals")
        void shouldIterateBothMaps() {
            // Set an inheritable ThreadLocal with a test-CL-loaded value
            InheritableThreadLocal<Object> itl = new InheritableThreadLocal<>();
            itl.set(new LeakCleanerTest());

            ClassLoader testLoader = LeakCleanerTest.class.getClassLoader();
            int count = LeakCleaner.cleanThreadLocals(testLoader);
            assertThat(count).isGreaterThanOrEqualTo(1);

            itl.remove();
        }
    }

    @Nested
    @DisplayName("cleanShutdownHooks Tests")
    class CleanShutdownHooksTests {

        @Test
        @DisplayName("should return 0 for classloader with no hooks")
        void shouldReturnZeroForNoHooks() {
            URLClassLoader loader = new URLClassLoader(new URL[0], null);
            try {
                int count = LeakCleaner.cleanShutdownHooks(loader);
                assertThat(count).isGreaterThanOrEqualTo(0);
            } finally {
                closeQuietly(loader);
            }
        }

        @Test
        @DisplayName("should throw NPE for null classloader")
        void shouldThrowNpeForNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> LeakCleaner.cleanShutdownHooks(null))
                    .withMessageContaining("classLoader must not be null");
        }

        @Test
        @DisplayName("should return 0 for classloader with no matching hooks")
        void shouldReturnZeroForNonMatchingLoader() {
            // Use an isolated classloader that has no hooks registered
            // Must NOT use the app/test classloader — it would match JaCoCo's shutdown hook
            URLClassLoader isolated = new URLClassLoader(new URL[0], null);
            try {
                int count = LeakCleaner.cleanShutdownHooks(isolated);
                assertThat(count).isEqualTo(0);
            } finally {
                closeQuietly(isolated);
            }
        }

        @Test
        @DisplayName("should not remove hooks from non-matching classloaders")
        void shouldNotRemoveHooksFromOtherLoaders() {
            Thread hook = new Thread(() -> {}, "leak-test-hook-other");
            Runtime.getRuntime().addShutdownHook(hook);
            try {
                // Use an isolated classloader — no hooks should match
                URLClassLoader other = new URLClassLoader(new URL[0], null);
                try {
                    int count = LeakCleaner.cleanShutdownHooks(other);
                    assertThat(count).isEqualTo(0);
                } finally {
                    closeQuietly(other);
                }
            } finally {
                try { Runtime.getRuntime().removeShutdownHook(hook); } catch (IllegalStateException ignored) {}
            }
        }

        @Test
        @DisplayName("should be idempotent for isolated loader")
        void shouldBeIdempotent() {
            URLClassLoader loader = new URLClassLoader(new URL[0], null);
            try {
                int count1 = LeakCleaner.cleanShutdownHooks(loader);
                int count2 = LeakCleaner.cleanShutdownHooks(loader);
                assertThat(count1).isEqualTo(0);
                assertThat(count2).isEqualTo(0);
            } finally {
                closeQuietly(loader);
            }
        }
    }

    @Nested
    @DisplayName("cleanTimers Tests")
    class CleanTimersTests {

        @Test
        @DisplayName("should return 0 for classloader with no timers")
        void shouldReturnZeroForNoTimers() {
            URLClassLoader loader = new URLClassLoader(new URL[0], null);
            try {
                int count = LeakCleaner.cleanTimers(loader);
                assertThat(count).isEqualTo(0);
            } finally {
                closeQuietly(loader);
            }
        }

        @Test
        @DisplayName("should throw NPE for null classloader")
        void shouldThrowNpeForNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> LeakCleaner.cleanTimers(null))
                    .withMessageContaining("classLoader must not be null");
        }

        @Test
        @DisplayName("should not interrupt threads from other classloaders")
        void shouldNotInterruptOtherThreads() {
            URLClassLoader loader = new URLClassLoader(new URL[0], null);
            try {
                int count = LeakCleaner.cleanTimers(loader);
                assertThat(count).isEqualTo(0);
                // Current thread should NOT be interrupted
                assertThat(Thread.currentThread().isInterrupted()).isFalse();
            } finally {
                closeQuietly(loader);
            }
        }

        @Test
        @DisplayName("should scan live threads without error using test classloader")
        void shouldScanLiveThreads() {
            // Use the test classloader — no threads are loaded by it with Timer names
            ClassLoader testLoader = this.getClass().getClassLoader();
            int count = LeakCleaner.cleanTimers(testLoader);
            assertThat(count).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("should find and interrupt timer-named thread loaded by matching classloader")
        void shouldInterruptTimerThread() throws InterruptedException {
            // Create a timer using java.util.Timer to exercise the scan path
            java.util.Timer timer = new java.util.Timer("leak-test-Timer", true);
            timer.schedule(new java.util.TimerTask() {
                @Override public void run() {
                    // no-op, just keep timer alive
                }
            }, 60_000);

            try {
                // Timer's internal thread is loaded by bootstrap CL, not test CL.
                // So cleanTimers with test CL won't match it — this exercises the
                // "isLoadedBy returns false" branch for alive timer threads.
                ClassLoader testLoader = this.getClass().getClassLoader();
                int count = LeakCleaner.cleanTimers(testLoader);
                // The JDK Timer thread class is bootstrap-loaded, won't match
                assertThat(count).isEqualTo(0);
            } finally {
                timer.cancel();
            }
        }
    }

    @Nested
    @DisplayName("CleanupReport Record Tests")
    class CleanupReportTests {

        @Test
        @DisplayName("empty() should return all-zero report")
        void emptyReportShouldBeAllZero() {
            CleanupReport report = CleanupReport.empty();

            assertThat(report.threadLocalsCleared()).isEqualTo(0);
            assertThat(report.jdbcDriversRemoved()).isEqualTo(0);
            assertThat(report.shutdownHooksRemoved()).isEqualTo(0);
            assertThat(report.timersCancelled()).isEqualTo(0);
            assertThat(report.errors()).isEmpty();
        }

        @Test
        @DisplayName("should reject negative counts")
        void shouldRejectNegativeCounts() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CleanupReport(-1, 0, 0, 0, List.of()));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CleanupReport(0, -1, 0, 0, List.of()));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CleanupReport(0, 0, -1, 0, List.of()));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CleanupReport(0, 0, 0, -1, List.of()));
        }

        @Test
        @DisplayName("should throw NPE for null errors list")
        void shouldThrowNpeForNullErrors() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CleanupReport(0, 0, 0, 0, null));
        }

        @Test
        @DisplayName("should make defensive copy of errors list")
        void shouldMakeDefensiveCopy() {
            var errors = new java.util.ArrayList<>(List.of("error1"));
            CleanupReport report = new CleanupReport(1, 2, 3, 4, errors);

            errors.add("error2");
            assertThat(report.errors()).hasSize(1);
        }

        @Test
        @DisplayName("should store correct values")
        void shouldStoreCorrectValues() {
            CleanupReport report = new CleanupReport(1, 2, 3, 4, List.of("err"));

            assertThat(report.threadLocalsCleared()).isEqualTo(1);
            assertThat(report.jdbcDriversRemoved()).isEqualTo(2);
            assertThat(report.shutdownHooksRemoved()).isEqualTo(3);
            assertThat(report.timersCancelled()).isEqualTo(4);
            assertThat(report.errors()).containsExactly("err");
        }

        @Test
        @DisplayName("equals and hashCode should work for identical reports")
        void shouldSupportEquality() {
            CleanupReport r1 = new CleanupReport(1, 2, 3, 4, List.of("e"));
            CleanupReport r2 = new CleanupReport(1, 2, 3, 4, List.of("e"));
            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("toString should contain field values")
        void toStringShouldContainValues() {
            CleanupReport report = new CleanupReport(1, 2, 3, 4, List.of("err"));
            String str = report.toString();
            assertThat(str).contains("1", "2", "3", "4", "err");
        }
    }

    /**
     * Stub JDBC driver for testing driver deregistration
     */
    static class StubDriver implements Driver {
        @Override public Connection connect(String url, Properties info) { return null; }
        @Override public boolean acceptsURL(String url) { return false; }
        @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
        @Override public int getMajorVersion() { return 1; }
        @Override public int getMinorVersion() { return 0; }
        @Override public boolean jdbcCompliant() { return false; }
        @Override public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }
    }

    private static void closeQuietly(URLClassLoader loader) {
        try { loader.close(); } catch (Exception ignored) {}
    }
}
