package cloud.opencode.base.id.segment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;

/**
 * JdbcSegmentAllocator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("JdbcSegmentAllocator 测试")
class JdbcSegmentAllocatorTest {

    /**
     * 创建一个简单的空DataSource用于测试工厂方法
     */
    private DataSource createDummyDataSource() {
        return new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("Dummy datasource");
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                throw new SQLException("Dummy datasource");
            }

            @Override
            public PrintWriter getLogWriter() {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) {
            }

            @Override
            public void setLoginTimeout(int seconds) {
            }

            @Override
            public int getLoginTimeout() {
                return 0;
            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                throw new SQLException("Not supported");
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) {
                return false;
            }
        };
    }

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            DataSource dataSource = createDummyDataSource();
            JdbcSegmentAllocator allocator = JdbcSegmentAllocator.create(dataSource);

            assertThat(allocator).isNotNull();
            assertThat(allocator.getStep()).isEqualTo(SegmentAllocator.DEFAULT_STEP);
        }

        @Test
        @DisplayName("使用自定义设置创建")
        void testCreateWithCustomSettings() {
            DataSource dataSource = createDummyDataSource();
            JdbcSegmentAllocator allocator = JdbcSegmentAllocator.create(
                    dataSource, "custom_table", 5000
            );

            assertThat(allocator).isNotNull();
            assertThat(allocator.getStep()).isEqualTo(5000);
        }
    }

    @Nested
    @DisplayName("步长测试")
    class StepTests {

        @Test
        @DisplayName("获取默认步长")
        void testGetDefaultStep() {
            DataSource dataSource = createDummyDataSource();
            JdbcSegmentAllocator allocator = JdbcSegmentAllocator.create(dataSource);

            assertThat(allocator.getStep()).isEqualTo(1000);
        }

        @Test
        @DisplayName("获取自定义步长")
        void testGetCustomStep() {
            DataSource dataSource = createDummyDataSource();
            JdbcSegmentAllocator allocator = JdbcSegmentAllocator.create(
                    dataSource, "id_segment", 2000
            );

            assertThat(allocator.getStep()).isEqualTo(2000);
        }

        @Test
        @DisplayName("获取大步长")
        void testGetLargeStep() {
            DataSource dataSource = createDummyDataSource();
            JdbcSegmentAllocator allocator = JdbcSegmentAllocator.create(
                    dataSource, "id_segment", 100000
            );

            assertThat(allocator.getStep()).isEqualTo(100000);
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现SegmentAllocator接口")
        void testImplementsInterface() {
            DataSource dataSource = createDummyDataSource();
            JdbcSegmentAllocator allocator = JdbcSegmentAllocator.create(dataSource);

            assertThat(allocator).isInstanceOf(SegmentAllocator.class);
        }
    }
}
