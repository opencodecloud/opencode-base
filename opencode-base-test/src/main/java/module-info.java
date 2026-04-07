/**
 * OpenCode Base Test Module
 * OpenCode 基础测试模块
 *
 * <p>Provides testing utilities including assertions, mock support,
 * test data generation, and benchmark tools.</p>
 * <p>提供测试工具，包括断言、Mock支持、测试数据生成和基准测试工具。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OpenAssert - Comprehensive assertions (basic, collection, string, exception) - 全面断言</li>
 *   <li>RecordAssert - Record component assertions by name - Record 组件断言</li>
 *   <li>MapAssert - Standalone Map assertions - 独立 Map 断言</li>
 *   <li>TimingAssert - Performance timing assertions - 性能计时断言</li>
 *   <li>SnapshotAssert - JSON snapshot testing - JSON 快照测试</li>
 *   <li>OpenMock - Interface mocking with JDK Proxy - 接口 Mock</li>
 *   <li>OpenData - Test data generation (string, number, date, Chinese) - 测试数据生成</li>
 *   <li>AutoFill - Auto-populate Record/POJO instances - 自动填充 Record/POJO 实例</li>
 *   <li>EdgeCases - Boundary value generators - 边界值生成器</li>
 *   <li>Benchmark - Performance testing with warmup and statistics - 性能测试</li>
 *   <li>SoftAssert - Collect multiple assertion failures - 软断言</li>
 *   <li>ConcurrentTester - Thread safety testing - 并发测试</li>
 *   <li>Wait/Poller - Condition polling - 条件等待</li>
 *   <li>TestFixture - Reusable test fixtures - 测试夹具</li>
 *   <li>RequestVerification - HTTP request verification - HTTP 请求验证</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-test V1.0.0
 */
module cloud.opencode.base.test {
    // Required modules
    requires transitive cloud.opencode.base.core;
    requires static jdk.httpserver;

    // Export public API packages
    exports cloud.opencode.base.test;
    exports cloud.opencode.base.test.annotation;
    exports cloud.opencode.base.test.assertion;
    exports cloud.opencode.base.test.benchmark;
    exports cloud.opencode.base.test.concurrent;
    exports cloud.opencode.base.test.data;
    exports cloud.opencode.base.test.exception;
    exports cloud.opencode.base.test.fixture;
    exports cloud.opencode.base.test.mock;
    exports cloud.opencode.base.test.report;
    exports cloud.opencode.base.test.wait;
    exports cloud.opencode.base.test.http;
}
