package cloud.opencode.base.id;

import cloud.opencode.base.id.ksuid.KsuidGenerator;
import cloud.opencode.base.id.ksuid.KsuidParser;
import cloud.opencode.base.id.nanoid.NanoIdBuilder;
import cloud.opencode.base.id.nanoid.NanoIdGenerator;
import cloud.opencode.base.id.prefixed.PrefixedId;
import cloud.opencode.base.id.prefixed.TypedIdGenerator;
import cloud.opencode.base.id.segment.SegmentAllocator;
import cloud.opencode.base.id.segment.SegmentIdGenerator;
import cloud.opencode.base.id.simple.AtomicIdGenerator;
import cloud.opencode.base.id.simple.RandomIdGenerator;
import cloud.opencode.base.id.simple.TimestampIdGenerator;
import cloud.opencode.base.id.snowflake.SafeJsSnowflakeGenerator;
import cloud.opencode.base.id.snowflake.SnowflakeBuilder;
import cloud.opencode.base.id.snowflake.SnowflakeFriendlyId;
import cloud.opencode.base.id.snowflake.SnowflakeGenerator;
import cloud.opencode.base.id.snowflake.SnowflakeIdParser;
import cloud.opencode.base.id.tsid.TsidGenerator;
import cloud.opencode.base.id.tsid.TsidParser;
import cloud.opencode.base.id.ulid.UlidGenerator;
import cloud.opencode.base.id.ulid.UlidParser;
import cloud.opencode.base.id.uuid.OpenUuid;
import cloud.opencode.base.id.uuid.UuidParser;
import cloud.opencode.base.id.uuid.UuidV7Generator;

import java.util.UUID;

/**
 * ID Utility Facade Class
 * ID工具门面类
 *
 * <p>Provides a unified, simple API for generating various types of unique identifiers.
 * This is the main entry point for all ID generation operations.</p>
 * <p>提供统一、简洁的API来生成各种类型的唯一标识符。
 * 这是所有ID生成操作的主入口。</p>
 *
 * <p><strong>Supported ID Types | 支持的ID类型:</strong></p>
 * <ul>
 *   <li>Snowflake - 64-bit time-ordered | 64位时间有序</li>
 *   <li>UUID v4/v7 - 128-bit universal | 128位通用</li>
 *   <li>ULID - 128-bit lexicographically sortable | 128位字典序可排序</li>
 *   <li>TSID - 64-bit time-sorted | 64位时间排序</li>
 *   <li>KSUID - 160-bit K-sortable | 160位K可排序</li>
 *   <li>NanoID - Compact URL-friendly | 紧凑URL友好</li>
 *   <li>Segment - Database sequence | 数据库序列</li>
 *   <li>Simple (Atomic/Timestamp/Random) | 简单（原子/时间戳/随机）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Snowflake ID
 * long snowflakeId = OpenId.snowflakeId();
 * String snowflakeStr = OpenId.snowflakeIdStr();
 *
 * // UUID
 * UUID uuid = OpenId.uuid();
 * UUID uuidV7 = OpenId.uuidV7();
 *
 * // ULID
 * String ulid = OpenId.ulid();
 *
 * // TSID
 * long tsid = OpenId.tsid();
 * String tsidStr = OpenId.tsidStr();
 *
 * // KSUID
 * String ksuid = OpenId.ksuid();
 *
 * // NanoID
 * String nanoId = OpenId.nanoId();
 * String shortId = OpenId.nanoId(10);
 *
 * // Simple IDs
 * long simpleId = OpenId.simpleId();
 * String timestampId = OpenId.timestampId();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified entry point for all ID generation strategies - 所有ID生成策略的统一入口</li>
 *   <li>Support for Snowflake, ULID, UUID, NanoID, KSUID, TSID - 支持Snowflake、ULID、UUID、NanoID、KSUID、TSID</li>
 *   <li>Configurable ID generation parameters - 可配置的ID生成参数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class OpenId {

    private static final SnowflakeGenerator DEFAULT_SNOWFLAKE = SnowflakeGenerator.create();
    private static final SafeJsSnowflakeGenerator DEFAULT_SAFE_JS_SNOWFLAKE = SafeJsSnowflakeGenerator.create();
    private static final UlidGenerator DEFAULT_ULID = UlidGenerator.create();
    private static final TsidGenerator DEFAULT_TSID = TsidGenerator.create();
    private static final KsuidGenerator DEFAULT_KSUID = KsuidGenerator.create();
    private static final NanoIdGenerator DEFAULT_NANOID = NanoIdGenerator.create();
    private static final AtomicIdGenerator DEFAULT_ATOMIC = AtomicIdGenerator.create();
    private static final TimestampIdGenerator DEFAULT_TIMESTAMP = TimestampIdGenerator.create();

    private OpenId() {
    }

    // ==================== Snowflake ====================

    /**
     * Generates a Snowflake ID using default configuration
     * 使用默认配置生成雪花ID
     *
     * @return 64-bit Snowflake ID | 64位雪花ID
     */
    public static long snowflakeId() {
        return DEFAULT_SNOWFLAKE.generate();
    }

    /**
     * Generates a Snowflake ID as string
     * 生成雪花ID字符串
     *
     * @return Snowflake ID string | 雪花ID字符串
     */
    public static String snowflakeIdStr() {
        return DEFAULT_SNOWFLAKE.generateStr();
    }

    /**
     * Gets the default Snowflake generator
     * 获取默认雪花ID生成器
     *
     * @return generator | 生成器
     */
    public static IdGenerator<Long> getSnowflake() {
        return DEFAULT_SNOWFLAKE;
    }

    /**
     * Creates a Snowflake generator with worker and datacenter IDs
     * 使用工作节点ID和数据中心ID创建雪花ID生成器
     *
     * @param workerId     the worker node ID (0-31) | 工作节点ID（0-31）
     * @param datacenterId the datacenter ID (0-31) | 数据中心ID（0-31）
     * @return generator | 生成器
     */
    public static IdGenerator<Long> createSnowflake(long workerId, long datacenterId) {
        return SnowflakeGenerator.create(workerId, datacenterId);
    }

    /**
     * Creates a Snowflake builder for customized configuration
     * 创建用于自定义配置的雪花ID构建器
     *
     * @return builder | 构建器
     */
    public static SnowflakeBuilder snowflakeBuilder() {
        return new SnowflakeBuilder();
    }

    /**
     * Parses a Snowflake ID
     * 解析雪花ID
     *
     * @param id the Snowflake ID | 雪花ID
     * @return parsed result | 解析结果
     */
    public static SnowflakeIdParser.ParsedId parseSnowflakeId(long id) {
        return DEFAULT_SNOWFLAKE.parse(id);
    }

    // ==================== Safe JavaScript Snowflake ====================

    /**
     * Generates a JavaScript-safe Snowflake ID (value always ≤ 2^53-1)
     * 生成JavaScript安全的雪花ID（值始终≤ 2^53-1）
     *
     * <p>Prevents silent rounding when the ID is serialized as a JSON number
     * and consumed by a JavaScript frontend.</p>
     * <p>防止ID被序列化为JSON数字并被JavaScript前端消费时的静默四舍五入。</p>
     *
     * @return JavaScript-safe ID | JavaScript安全的ID
     */
    public static long safeJsSnowflakeId() {
        return DEFAULT_SAFE_JS_SNOWFLAKE.generate();
    }

    /**
     * Generates a JavaScript-safe Snowflake ID as string
     * 生成JavaScript安全的雪花ID字符串
     *
     * @return ID string | ID字符串
     */
    public static String safeJsSnowflakeIdStr() {
        return DEFAULT_SAFE_JS_SNOWFLAKE.generateStr();
    }

    /**
     * Gets the default JavaScript-safe Snowflake generator
     * 获取默认JavaScript安全雪花ID生成器
     *
     * @return generator | 生成器
     */
    public static SafeJsSnowflakeGenerator getSafeJsSnowflake() {
        return DEFAULT_SAFE_JS_SNOWFLAKE;
    }

    // ==================== Snowflake Friendly ID ====================

    /**
     * Returns a SnowflakeFriendlyId converter using the default configuration
     * 使用默认配置返回SnowflakeFriendlyId转换器
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * String readable = OpenId.snowflakeFriendly().toFriendly(snowflakeId);
     * // → "2024-01-15T10:30:00.123Z#0-0-42"
     * </pre>
     *
     * @return SnowflakeFriendlyId converter | SnowflakeFriendlyId转换器
     */
    public static SnowflakeFriendlyId snowflakeFriendly() {
        return SnowflakeFriendlyId.ofDefault();
    }

    // ==================== Typed / Prefixed ID ====================

    /**
     * Creates a TypedIdGenerator with the specified prefix and underlying generator
     * 创建带指定前缀和底层生成器的TypedIdGenerator
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * IdGenerator&lt;String&gt; gen = OpenId.typedIdGenerator("usr", UlidGenerator.create());
     * String userId = gen.generate(); // → "usr_01ARZ3NDEKTSV4RRFFQ69G5FAV"
     * </pre>
     *
     * @param prefix the lowercase entity-type prefix (e.g., "usr", "order") | 小写实体类型前缀（如"usr"、"order"）
     * @param inner  the underlying string ID generator | 底层字符串ID生成器
     * @return TypedIdGenerator | 类型化ID生成器
     * @throws cloud.opencode.base.id.exception.OpenIdGenerationException if prefix is invalid | 前缀无效时抛出
     */
    public static TypedIdGenerator typedIdGenerator(String prefix, IdGenerator<String> inner) {
        return TypedIdGenerator.of(prefix, inner);
    }

    /**
     * Parses a prefixed ID string (e.g., "usr_01ARZ3NDEK") into a PrefixedId
     * 将带前缀的ID字符串（如"usr_01ARZ3NDEK"）解析为PrefixedId
     *
     * @param prefixedId the full prefixed ID string | 完整的带前缀ID字符串
     * @return parsed PrefixedId | 解析后的PrefixedId
     * @throws cloud.opencode.base.id.exception.OpenIdGenerationException if the format is invalid | 格式无效时抛出
     */
    public static PrefixedId parsePrefixedId(String prefixedId) {
        return PrefixedId.fromString(prefixedId);
    }

    // ==================== UUID ====================

    /**
     * Generates a UUID v4 (random)
     * 生成UUID v4（随机）
     *
     * @return UUID object | UUID对象
     */
    public static UUID uuid() {
        return UUID.randomUUID();
    }

    /**
     * Generates a UUID v4 as string
     * 生成UUID v4字符串
     *
     * @return UUID string | UUID字符串
     */
    public static String uuidStr() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a simple UUID (no hyphens)
     * 生成简化UUID（无连字符）
     *
     * @return simple UUID string (32 chars) | 简化UUID字符串（32字符）
     */
    public static String simpleUuid() {
        return OpenUuid.toSimpleString(UUID.randomUUID());
    }

    /**
     * Generates a UUID v7 (time-ordered)
     * 生成UUID v7（时间有序）
     *
     * @return UUID object | UUID对象
     */
    public static UUID uuidV7() {
        return UuidV7Generator.create().generate();
    }

    /**
     * Generates a UUID v7 as string
     * 生成UUID v7字符串
     *
     * @return UUID string | UUID字符串
     */
    public static String uuidV7Str() {
        return uuidV7().toString();
    }

    /**
     * Gets the UUID v7 generator
     * 获取UUID v7生成器
     *
     * @return generator | 生成器
     */
    public static IdGenerator<UUID> getUuidV7Generator() {
        return UuidV7Generator.create();
    }

    /**
     * Parses a UUID to extract version, variant, and timestamp information
     * 解析UUID以提取版本、变体和时间戳信息
     *
     * @param uuid the UUID to parse | 要解析的UUID
     * @return parsed UUID info | 解析后的UUID信息
     */
    public static UuidParser.ParsedUuid parseUuid(UUID uuid) {
        return UuidParser.create().parse(uuid);
    }

    // ==================== ULID ====================

    /**
     * Generates a ULID
     * 生成ULID
     *
     * @return ULID string (26 chars) | ULID字符串（26字符）
     */
    public static String ulid() {
        return DEFAULT_ULID.generate();
    }

    /**
     * Generates a ULID as byte array
     * 生成ULID字节数组
     *
     * @return 16-byte array | 16字节数组
     */
    public static byte[] ulidBytes() {
        return DEFAULT_ULID.generateBytes();
    }

    /**
     * Gets the ULID generator
     * 获取ULID生成器
     *
     * @return generator | 生成器
     */
    public static IdGenerator<String> getUlidGenerator() {
        return DEFAULT_ULID;
    }

    /**
     * Parses a ULID
     * 解析ULID
     *
     * @param ulid the ULID string | ULID字符串
     * @return parsed result | 解析结果
     */
    public static UlidParser.ParsedUlid parseUlid(String ulid) {
        return UlidParser.create().parse(ulid);
    }

    // ==================== TSID ====================

    /**
     * Generates a TSID (Time-Sorted ID)
     * 生成TSID（时间排序ID）
     *
     * @return 64-bit TSID | 64位TSID
     */
    public static long tsid() {
        return DEFAULT_TSID.generate();
    }

    /**
     * Generates a TSID as Crockford's Base32 string
     * 生成Crockford Base32编码的TSID字符串
     *
     * @return TSID string (13 characters) | TSID字符串（13字符）
     */
    public static String tsidStr() {
        return DEFAULT_TSID.generateStr();
    }

    /**
     * Gets the default TSID generator
     * 获取默认TSID生成器
     *
     * @return generator | 生成器
     */
    public static IdGenerator<Long> getTsidGenerator() {
        return DEFAULT_TSID;
    }

    /**
     * Creates a TSID generator with node configuration
     * 使用节点配置创建TSID生成器
     *
     * @param nodeBits the number of bits for node ID (0-22) | 节点ID位数（0-22）
     * @param nodeId   the node ID | 节点ID
     * @return generator | 生成器
     */
    public static TsidGenerator createTsidGenerator(int nodeBits, long nodeId) {
        return TsidGenerator.create(nodeBits, nodeId);
    }

    /**
     * Parses a TSID string
     * 解析TSID字符串
     *
     * @param tsidStr the TSID string | TSID字符串
     * @return parsed result | 解析结果
     */
    public static TsidParser.ParsedTsid parseTsid(String tsidStr) {
        return TsidParser.create().parse(tsidStr);
    }

    /**
     * Parses a TSID long value
     * 解析TSID长整型值
     *
     * @param tsid the TSID value | TSID值
     * @return parsed result | 解析结果
     */
    public static TsidParser.ParsedTsid parseTsid(long tsid) {
        return TsidParser.create().parse(tsid);
    }

    /**
     * Decodes a TSID string to long value
     * 将TSID字符串解码为长整型值
     *
     * @param tsidStr the TSID string | TSID字符串
     * @return TSID value | TSID值
     */
    public static long decodeTsid(String tsidStr) {
        return TsidGenerator.decode(tsidStr);
    }

    // ==================== KSUID ====================

    /**
     * Generates a KSUID (K-Sortable Unique Identifier)
     * 生成KSUID（K可排序唯一标识符）
     *
     * @return KSUID string (27 characters) | KSUID字符串（27字符）
     */
    public static String ksuid() {
        return DEFAULT_KSUID.generate();
    }

    /**
     * Generates a KSUID as raw bytes
     * 生成KSUID原始字节
     *
     * @return 20-byte array | 20字节数组
     */
    public static byte[] ksuidBytes() {
        return DEFAULT_KSUID.generateBytes();
    }

    /**
     * Gets the KSUID generator
     * 获取KSUID生成器
     *
     * @return generator | 生成器
     */
    public static IdGenerator<String> getKsuidGenerator() {
        return DEFAULT_KSUID;
    }

    /**
     * Parses a KSUID
     * 解析KSUID
     *
     * @param ksuid the KSUID string | KSUID字符串
     * @return parsed result | 解析结果
     */
    public static KsuidParser.ParsedKsuid parseKsuid(String ksuid) {
        return KsuidParser.create().parse(ksuid);
    }

    /**
     * Validates a KSUID string
     * 验证KSUID字符串
     *
     * @param ksuid the KSUID string | KSUID字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidKsuid(String ksuid) {
        return KsuidGenerator.isValid(ksuid);
    }

    // ==================== NanoID ====================

    /**
     * Generates a NanoID (default 21 chars)
     * 生成NanoID（默认21字符）
     *
     * @return NanoID string | NanoID字符串
     */
    public static String nanoId() {
        return DEFAULT_NANOID.generate();
    }

    /**
     * Generates a NanoID with specific length
     * 使用指定长度生成NanoID
     *
     * @param size the length | 长度
     * @return NanoID string | NanoID字符串
     */
    public static String nanoId(int size) {
        return NanoIdGenerator.randomNanoId(size);
    }

    /**
     * Generates a NanoID with custom alphabet
     * 使用自定义字母表生成NanoID
     *
     * @param size     the length | 长度
     * @param alphabet the character set | 字符集
     * @return NanoID string | NanoID字符串
     */
    public static String nanoId(int size, String alphabet) {
        return NanoIdGenerator.randomNanoId(size, alphabet);
    }

    /**
     * Creates a NanoID builder
     * 创建NanoID构建器
     *
     * @return builder | 构建器
     */
    public static NanoIdBuilder nanoIdBuilder() {
        return new NanoIdBuilder();
    }

    // ==================== Simple ID ====================

    /**
     * Generates a simple incremental ID
     * 生成简单自增ID
     *
     * @return incremental ID | 自增ID
     */
    public static long simpleId() {
        return DEFAULT_ATOMIC.generate();
    }

    /**
     * Generates a timestamp-based ID
     * 生成基于时间戳的ID
     *
     * @return timestamp ID string | 时间戳ID字符串
     */
    public static String timestampId() {
        return DEFAULT_TIMESTAMP.generate();
    }

    /**
     * Generates a random ID
     * 生成随机ID
     *
     * @param length the length | 长度
     * @return random ID string | 随机ID字符串
     */
    public static String randomId(int length) {
        return RandomIdGenerator.create(length).generate();
    }

    // ==================== Segment Mode ====================

    /**
     * Creates a segment mode generator
     * 创建号段模式生成器
     *
     * @param allocator the segment allocator | 号段分配器
     * @return generator | 生成器
     */
    public static IdGenerator<Long> createSegmentGenerator(SegmentAllocator allocator) {
        return SegmentIdGenerator.create(allocator);
    }

    /**
     * Creates a segment mode generator with business tag
     * 使用业务标识创建号段模式生成器
     *
     * @param allocator the segment allocator | 号段分配器
     * @param bizTag    the business tag | 业务标识
     * @return generator | 生成器
     */
    public static IdGenerator<Long> createSegmentGenerator(SegmentAllocator allocator, String bizTag) {
        return SegmentIdGenerator.create(allocator, bizTag);
    }
}
