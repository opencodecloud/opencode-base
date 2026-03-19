package cloud.opencode.base.id.segment;

import cloud.opencode.base.id.exception.OpenIdGenerationException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * JDBC-based Segment Allocator
 * 基于JDBC的号段分配器
 *
 * <p>Allocates ID segments from a database table. Uses optimistic locking
 * for concurrent safety.</p>
 * <p>从数据库表分配ID号段。使用乐观锁确保并发安全。</p>
 *
 * <p><strong>Table Schema | 表结构:</strong></p>
 * <pre>
 * CREATE TABLE id_segment (
 *     biz_tag VARCHAR(128) NOT NULL PRIMARY KEY,
 *     max_id BIGINT NOT NULL DEFAULT 0,
 *     step INT NOT NULL DEFAULT 1000,
 *     version INT NOT NULL DEFAULT 0,
 *     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * );
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Optimistic locking - 乐观锁</li>
 *   <li>Auto table creation - 自动建表</li>
 *   <li>Configurable step size - 可配置步长</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DataSource dataSource = ...;
 * JdbcSegmentAllocator allocator = JdbcSegmentAllocator.create(
 *     dataSource,
 *     "id_segment",
 *     1000
 * );
 *
 * // Initialize table (first time only)
 * allocator.initTable();
 *
 * // Allocate segment
 * Segment segment = allocator.allocate("order");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class JdbcSegmentAllocator implements SegmentAllocator {

    private static final String DEFAULT_TABLE_NAME = "id_segment";
    private static final int MAX_RETRY = 3;

    /**
     * Pattern for valid SQL table names (alphanumeric and underscore only)
     * 有效SQL表名的模式（仅允许字母数字和下划线）
     */
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,63}$");

    private final DataSource dataSource;
    private final String tableName;
    private final int step;

    private final String selectSql;
    private final String updateSql;
    private final String insertSql;

    /**
     * Creates an allocator
     * 创建分配器
     *
     * @param dataSource the data source | 数据源
     * @param tableName  the table name | 表名
     * @param step       the step size | 步长
     */
    private JdbcSegmentAllocator(DataSource dataSource, String tableName, int step) {
        // Validate table name to prevent SQL injection
        if (tableName == null || !TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new IllegalArgumentException(
                    "Invalid table name: " + tableName + ". Table name must match pattern: " + TABLE_NAME_PATTERN.pattern());
        }

        this.dataSource = dataSource;
        this.tableName = tableName;
        this.step = step;

        this.selectSql = String.format(
                "SELECT max_id, step, version FROM %s WHERE biz_tag = ?", tableName);
        this.updateSql = String.format(
                "UPDATE %s SET max_id = max_id + ?, version = version + 1, update_time = CURRENT_TIMESTAMP WHERE biz_tag = ? AND version = ?",
                tableName);
        this.insertSql = String.format(
                "INSERT INTO %s (biz_tag, max_id, step, version) VALUES (?, ?, ?, 0)", tableName);
    }

    /**
     * Creates an allocator with default settings
     * 使用默认设置创建分配器
     *
     * @param dataSource the data source | 数据源
     * @return allocator | 分配器
     */
    public static JdbcSegmentAllocator create(DataSource dataSource) {
        return new JdbcSegmentAllocator(dataSource, DEFAULT_TABLE_NAME, DEFAULT_STEP);
    }

    /**
     * Creates an allocator with custom settings
     * 使用自定义设置创建分配器
     *
     * @param dataSource the data source | 数据源
     * @param tableName  the table name | 表名
     * @param step       the step size | 步长
     * @return allocator | 分配器
     */
    public static JdbcSegmentAllocator create(DataSource dataSource, String tableName, int step) {
        return new JdbcSegmentAllocator(dataSource, tableName, step);
    }

    @Override
    public Segment allocate(String bizTag) {
        for (int retry = 0; retry < MAX_RETRY; retry++) {
            try {
                return doAllocate(bizTag);
            } catch (SQLException e) {
                if (retry == MAX_RETRY - 1) {
                    throw OpenIdGenerationException.segmentAllocationFailed(bizTag, e);
                }
            }
        }
        throw OpenIdGenerationException.segmentAllocationFailed(bizTag, null);
    }

    @Override
    public int getStep() {
        return step;
    }

    /**
     * Initializes the table structure
     * 初始化表结构
     */
    public void initTable() {
        String createTableSql = String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    biz_tag VARCHAR(128) NOT NULL PRIMARY KEY,
                    max_id BIGINT NOT NULL DEFAULT 0,
                    step INT NOT NULL DEFAULT %d,
                    version INT NOT NULL DEFAULT 0,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """, tableName, step);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(createTableSql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table: " + tableName, e);
        }
    }

    private Segment doAllocate(String bizTag) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Select current state
                long currentMaxId;
                int currentStep;
                int version;

                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setString(1, bizTag);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            currentMaxId = rs.getLong("max_id");
                            currentStep = rs.getInt("step");
                            version = rs.getInt("version");
                        } else {
                            // First allocation, insert new record
                            insertNewBizTag(conn, bizTag);
                            currentMaxId = 0;
                            currentStep = step;
                            version = 0;
                        }
                    }
                }

                // Update with optimistic locking
                int actualStep = currentStep > 0 ? currentStep : step;
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, actualStep);
                    ps.setString(2, bizTag);
                    ps.setInt(3, version);
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        throw new SQLException("Optimistic lock failed");
                    }
                }

                conn.commit();
                return new Segment(currentMaxId, currentMaxId + actualStep, actualStep);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void insertNewBizTag(Connection conn, String bizTag) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, bizTag);   // biz_tag
            ps.setLong(2, 0);          // max_id: starts at 0; the UPDATE in doAllocate() will advance it by step
            ps.setInt(3, step);        // step
            ps.executeUpdate();
        }
    }
}
