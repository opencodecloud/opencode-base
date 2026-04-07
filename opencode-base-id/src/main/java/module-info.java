/**
 * OpenCode Base ID Module
 * OpenCode ID生成模块
 *
 * <p>Provides distributed ID generation algorithms including
 * Snowflake, UUID, ULID, TSID, KSUID, NanoID, Segment mode,
 * Prefixed/TypedId, and JavaScript-safe Snowflake.</p>
 * <p>提供分布式ID生成算法，包括Snowflake、UUID、ULID、TSID、KSUID、NanoID、
 * 号段模式、带前缀/类型化ID以及JavaScript安全雪花ID。</p>
 *
 * @since JDK 25, opencode-base-id V1.0.3
 */
module cloud.opencode.base.id {

    // Core module dependency
    requires transitive cloud.opencode.base.core;

    // SQL support for JdbcSegmentAllocator
    requires java.sql;

    // Export all public packages
    exports cloud.opencode.base.id;
    exports cloud.opencode.base.id.exception;
    exports cloud.opencode.base.id.snowflake;
    exports cloud.opencode.base.id.uuid;
    exports cloud.opencode.base.id.ulid;
    exports cloud.opencode.base.id.tsid;
    exports cloud.opencode.base.id.ksuid;
    exports cloud.opencode.base.id.nanoid;
    exports cloud.opencode.base.id.segment;
    exports cloud.opencode.base.id.simple;
    exports cloud.opencode.base.id.prefixed;
}
