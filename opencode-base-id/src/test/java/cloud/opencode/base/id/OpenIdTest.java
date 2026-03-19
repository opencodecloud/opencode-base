package cloud.opencode.base.id;

import cloud.opencode.base.id.snowflake.SnowflakeGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenId 门面类测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("OpenId 门面类测试")
class OpenIdTest {

    @Nested
    @DisplayName("Snowflake ID测试")
    class SnowflakeTests {

        @Test
        @DisplayName("生成Snowflake ID")
        void testSnowflakeId() {
            long id = OpenId.snowflakeId();

            assertThat(id).isPositive();
        }

        @Test
        @DisplayName("生成Snowflake ID字符串")
        void testSnowflakeIdStr() {
            String idStr = OpenId.snowflakeIdStr();

            assertThat(idStr).isNotNull();
            assertThat(idStr).matches("\\d+");
        }

        @Test
        @DisplayName("生成多个Snowflake ID唯一")
        void testSnowflakeUnique() {
            long id1 = OpenId.snowflakeId();
            long id2 = OpenId.snowflakeId();
            long id3 = OpenId.snowflakeId();

            assertThat(id1).isNotEqualTo(id2);
            assertThat(id2).isNotEqualTo(id3);
        }

        @Test
        @DisplayName("获取Snowflake生成器")
        void testGetSnowflake() {
            IdGenerator<Long> gen = OpenId.getSnowflake();

            assertThat(gen).isNotNull();
            assertThat(gen.generate()).isPositive();
        }

        @Test
        @DisplayName("创建自定义Snowflake生成器")
        void testCreateSnowflake() {
            IdGenerator<Long> gen = OpenId.createSnowflake(1, 2);

            assertThat(gen).isNotNull();
            assertThat(gen.generate()).isPositive();
        }

        @Test
        @DisplayName("解析Snowflake ID")
        void testParseSnowflakeId() {
            long id = OpenId.snowflakeId();
            var parsed = OpenId.parseSnowflakeId(id);

            assertThat(parsed).isNotNull();
            assertThat(parsed.id()).isEqualTo(id);
        }
    }

    @Nested
    @DisplayName("UUID测试")
    class UuidTests {

        @Test
        @DisplayName("生成UUID v4")
        void testUuidV4() {
            UUID uuid = OpenId.uuid();

            assertThat(uuid).isNotNull();
            assertThat(uuid.version()).isEqualTo(4);
        }

        @Test
        @DisplayName("生成UUID v4字符串")
        void testUuidStr() {
            String uuid = OpenId.uuidStr();

            assertThat(uuid).isNotNull();
            assertThat(uuid).hasSize(36);
            assertThat(uuid).contains("-");
        }

        @Test
        @DisplayName("生成简化UUID")
        void testSimpleUuid() {
            String uuid = OpenId.simpleUuid();

            assertThat(uuid).isNotNull();
            assertThat(uuid).hasSize(32);
            assertThat(uuid).doesNotContain("-");
        }

        @Test
        @DisplayName("生成UUID v7")
        void testUuidV7() {
            UUID uuid = OpenId.uuidV7();

            assertThat(uuid).isNotNull();
            assertThat(uuid.version()).isEqualTo(7);
        }

        @Test
        @DisplayName("生成UUID v7字符串")
        void testUuidV7Str() {
            String uuidStr = OpenId.uuidV7Str();

            assertThat(uuidStr).isNotNull();
            assertThat(uuidStr).hasSize(36);
        }

        @Test
        @DisplayName("获取UUID v7生成器")
        void testGetUuidV7Generator() {
            IdGenerator<UUID> gen = OpenId.getUuidV7Generator();

            assertThat(gen).isNotNull();
            assertThat(gen.generate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ULID测试")
    class UlidTests {

        @Test
        @DisplayName("生成ULID")
        void testUlid() {
            String ulid = OpenId.ulid();

            assertThat(ulid).isNotNull();
            assertThat(ulid).hasSize(26);
        }

        @Test
        @DisplayName("生成ULID字节数组")
        void testUlidBytes() {
            byte[] bytes = OpenId.ulidBytes();

            assertThat(bytes).isNotNull();
            assertThat(bytes).hasSize(16);
        }

        @Test
        @DisplayName("ULID唯一性")
        void testUlidUnique() {
            String ulid1 = OpenId.ulid();
            String ulid2 = OpenId.ulid();

            assertThat(ulid1).isNotEqualTo(ulid2);
        }

        @Test
        @DisplayName("获取ULID生成器")
        void testGetUlidGenerator() {
            IdGenerator<String> gen = OpenId.getUlidGenerator();

            assertThat(gen).isNotNull();
            assertThat(gen.generate()).hasSize(26);
        }

        @Test
        @DisplayName("解析ULID")
        void testParseUlid() {
            String ulid = OpenId.ulid();
            var parsed = OpenId.parseUlid(ulid);

            assertThat(parsed).isNotNull();
            assertThat(parsed.ulid()).isEqualTo(ulid);
        }
    }

    @Nested
    @DisplayName("TSID测试")
    class TsidTests {

        @Test
        @DisplayName("生成TSID")
        void testTsid() {
            long tsid = OpenId.tsid();

            assertThat(tsid).isPositive();
        }

        @Test
        @DisplayName("生成TSID字符串")
        void testTsidStr() {
            String tsid = OpenId.tsidStr();

            assertThat(tsid).isNotNull();
            assertThat(tsid).hasSize(13);
        }

        @Test
        @DisplayName("获取TSID生成器")
        void testGetTsidGenerator() {
            IdGenerator<Long> gen = OpenId.getTsidGenerator();

            assertThat(gen).isNotNull();
            assertThat(gen.generate()).isPositive();
        }

        @Test
        @DisplayName("解析TSID")
        void testParseTsid() {
            long tsid = OpenId.tsid();
            var parsed = OpenId.parseTsid(tsid);

            assertThat(parsed).isNotNull();
        }

        @Test
        @DisplayName("解码TSID字符串")
        void testDecodeTsid() {
            String tsidStr = OpenId.tsidStr();
            long decoded = OpenId.decodeTsid(tsidStr);

            assertThat(decoded).isPositive();
        }
    }

    @Nested
    @DisplayName("KSUID测试")
    class KsuidTests {

        @Test
        @DisplayName("生成KSUID")
        void testKsuid() {
            String ksuid = OpenId.ksuid();

            assertThat(ksuid).isNotNull();
            assertThat(ksuid).hasSize(27);
        }

        @Test
        @DisplayName("生成KSUID字节数组")
        void testKsuidBytes() {
            byte[] bytes = OpenId.ksuidBytes();

            assertThat(bytes).isNotNull();
            assertThat(bytes).hasSize(20);
        }

        @Test
        @DisplayName("获取KSUID生成器")
        void testGetKsuidGenerator() {
            IdGenerator<String> gen = OpenId.getKsuidGenerator();

            assertThat(gen).isNotNull();
            assertThat(gen.generate()).hasSize(27);
        }

        @Test
        @DisplayName("解析KSUID")
        void testParseKsuid() {
            String ksuid = OpenId.ksuid();
            var parsed = OpenId.parseKsuid(ksuid);

            assertThat(parsed).isNotNull();
        }

        @Test
        @DisplayName("验证KSUID")
        void testIsValidKsuid() {
            String ksuid = OpenId.ksuid();

            assertThat(OpenId.isValidKsuid(ksuid)).isTrue();
            assertThat(OpenId.isValidKsuid("invalid")).isFalse();
        }
    }

    @Nested
    @DisplayName("NanoID测试")
    class NanoIdTests {

        @Test
        @DisplayName("生成NanoID")
        void testNanoId() {
            String nanoId = OpenId.nanoId();

            assertThat(nanoId).isNotNull();
            assertThat(nanoId).hasSize(21);
        }

        @Test
        @DisplayName("生成指定长度NanoID")
        void testNanoIdWithSize() {
            String nanoId = OpenId.nanoId(10);

            assertThat(nanoId).hasSize(10);
        }

        @Test
        @DisplayName("生成自定义字母表NanoID")
        void testNanoIdWithAlphabet() {
            String nanoId = OpenId.nanoId(10, "abc123");

            assertThat(nanoId).hasSize(10);
            for (char c : nanoId.toCharArray()) {
                assertThat("abc123").contains(String.valueOf(c));
            }
        }

        @Test
        @DisplayName("创建NanoID构建器")
        void testNanoIdBuilder() {
            var builder = OpenId.nanoIdBuilder();

            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("简单ID测试")
    class SimpleIdTests {

        @Test
        @DisplayName("生成简单自增ID")
        void testSimpleId() {
            long id1 = OpenId.simpleId();
            long id2 = OpenId.simpleId();

            assertThat(id2).isGreaterThan(id1);
        }

        @Test
        @DisplayName("生成随机ID")
        void testRandomId() {
            String id = OpenId.randomId(16);

            assertThat(id).hasSize(16);
        }

        @Test
        @DisplayName("生成时间戳ID")
        void testTimestampId() {
            String id = OpenId.timestampId();

            assertThat(id).isNotNull();
            assertThat(id.length()).isGreaterThanOrEqualTo(17);
        }
    }

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("Snowflake构建器")
        void testSnowflakeBuilder() {
            var builder = OpenId.snowflakeBuilder();

            assertThat(builder).isNotNull();
            var gen = builder.workerId(1).datacenterId(2).build();
            assertThat(gen).isNotNull();
        }
    }

    @Nested
    @DisplayName("TSID工厂方法测试")
    class TsidFactoryTests {

        @Test
        @DisplayName("创建自定义TSID生成器")
        void testCreateTsidGenerator() {
            var gen = OpenId.createTsidGenerator(5, 3);

            assertThat(gen).isNotNull();
            assertThat(gen.generate()).isPositive();
        }
    }

    @Nested
    @DisplayName("号段模式测试")
    class SegmentModeTests {

        @Test
        @DisplayName("创建号段生成器")
        void testCreateSegmentGenerator() {
            var allocator = cloud.opencode.base.id.segment.MemorySegmentAllocator.create();
            var gen = OpenId.createSegmentGenerator(allocator);

            assertThat(gen).isNotNull();
            assertThat(gen.generate()).isNotNull();
        }

        @Test
        @DisplayName("创建带业务标识的号段生成器")
        void testCreateSegmentGeneratorWithBizTag() {
            var allocator = cloud.opencode.base.id.segment.MemorySegmentAllocator.create();
            var gen = OpenId.createSegmentGenerator(allocator, "order");

            assertThat(gen).isNotNull();
            assertThat(gen.generate()).isNotNull();
        }
    }
}
