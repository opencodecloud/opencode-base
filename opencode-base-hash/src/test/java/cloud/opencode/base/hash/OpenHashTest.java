package cloud.opencode.base.hash;

import cloud.opencode.base.hash.bloom.BloomFilter;
import cloud.opencode.base.hash.bloom.CountingBloomFilter;
import cloud.opencode.base.hash.consistent.ConsistentHash;
import cloud.opencode.base.hash.consistent.HashNode;
import cloud.opencode.base.hash.simhash.SimHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenHash 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("OpenHash 测试")
class OpenHashTest {

    @Nested
    @DisplayName("Murmur3哈希函数测试")
    class Murmur3Tests {

        @Test
        @DisplayName("创建murmur3_32哈希函数")
        void testMurmur3_32() {
            HashFunction hash = OpenHash.murmur3_32();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(32);
            assertThat(hash.name()).contains("murmur3");
        }

        @Test
        @DisplayName("创建带种子的murmur3_32哈希函数")
        void testMurmur3_32WithSeed() {
            HashFunction hash = OpenHash.murmur3_32(42);

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("创建murmur3_128哈希函数")
        void testMurmur3_128() {
            HashFunction hash = OpenHash.murmur3_128();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(128);
            assertThat(hash.name()).contains("murmur3");
        }

        @Test
        @DisplayName("创建带种子的murmur3_128哈希函数")
        void testMurmur3_128WithSeed() {
            HashFunction hash = OpenHash.murmur3_128(42);

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(128);
        }
    }

    @Nested
    @DisplayName("xxHash函数测试")
    class XxHashTests {

        @Test
        @DisplayName("创建xxHash64哈希函数")
        void testXxHash64() {
            HashFunction hash = OpenHash.xxHash64();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(64);
            assertThat(hash.name()).containsIgnoringCase("xxhash");
        }

        @Test
        @DisplayName("创建带种子的xxHash64哈希函数")
        void testXxHash64WithSeed() {
            HashFunction hash = OpenHash.xxHash64(12345L);

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("FNV-1a哈希函数测试")
    class Fnv1aTests {

        @Test
        @DisplayName("创建fnv1a_32哈希函数")
        void testFnv1a_32() {
            HashFunction hash = OpenHash.fnv1a_32();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(32);
            assertThat(hash.name()).containsIgnoringCase("fnv");
        }

        @Test
        @DisplayName("创建fnv1a_64哈希函数")
        void testFnv1a_64() {
            HashFunction hash = OpenHash.fnv1a_64();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(64);
            assertThat(hash.name()).containsIgnoringCase("fnv");
        }
    }

    @Nested
    @DisplayName("CRC32哈希函数测试")
    class Crc32Tests {

        @Test
        @DisplayName("创建crc32哈希函数")
        void testCrc32() {
            HashFunction hash = OpenHash.crc32();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(32);
            assertThat(hash.name()).containsIgnoringCase("crc32");
        }

        @Test
        @DisplayName("创建crc32c哈希函数")
        void testCrc32c() {
            HashFunction hash = OpenHash.crc32c();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(32);
            assertThat(hash.name()).containsIgnoringCase("crc32c");
        }
    }

    @Nested
    @DisplayName("消息摘要哈希函数测试")
    class MessageDigestTests {

        @Test
        @DisplayName("创建md5哈希函数")
        void testMd5() {
            HashFunction hash = OpenHash.md5();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(128);
            assertThat(hash.name()).containsIgnoringCase("md5");
        }

        @Test
        @DisplayName("创建sha1哈希函数")
        void testSha1() {
            HashFunction hash = OpenHash.sha1();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(160);
            assertThat(hash.name()).containsIgnoringCase("sha");
        }

        @Test
        @DisplayName("创建sha256哈希函数")
        void testSha256() {
            HashFunction hash = OpenHash.sha256();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(256);
            assertThat(hash.name()).containsIgnoringCase("sha");
        }

        @Test
        @DisplayName("创建sha512哈希函数")
        void testSha512() {
            HashFunction hash = OpenHash.sha512();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(512);
            assertThat(hash.name()).containsIgnoringCase("sha");
        }

        @Test
        @DisplayName("创建sha3_256哈希函数")
        void testSha3_256() {
            HashFunction hash = OpenHash.sha3_256();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(256);
            assertThat(hash.name()).containsIgnoringCase("sha3");
        }

        @Test
        @DisplayName("创建sha3_512哈希函数")
        void testSha3_512() {
            HashFunction hash = OpenHash.sha3_512();

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(512);
            assertThat(hash.name()).containsIgnoringCase("sha3");
        }
    }

    @Nested
    @DisplayName("哈希结果组合测试")
    class CombineTests {

        @Test
        @DisplayName("有序组合哈希码")
        void testCombineOrdered() {
            HashCode h1 = HashCode.fromInt(1);
            HashCode h2 = HashCode.fromInt(2);
            HashCode h3 = HashCode.fromInt(3);

            HashCode combined1 = OpenHash.combineOrdered(h1, h2, h3);
            HashCode combined2 = OpenHash.combineOrdered(h3, h2, h1);

            assertThat(combined1).isNotEqualTo(combined2);
        }

        @Test
        @DisplayName("无序组合哈希码")
        void testCombineUnordered() {
            HashCode h1 = HashCode.fromInt(1);
            HashCode h2 = HashCode.fromInt(2);
            HashCode h3 = HashCode.fromInt(3);

            HashCode combined1 = OpenHash.combineUnordered(h1, h2, h3);
            HashCode combined2 = OpenHash.combineUnordered(h3, h2, h1);

            assertThat(combined1).isEqualTo(combined2);
        }
    }

    @Nested
    @DisplayName("一致性哈希测试")
    class ConsistentHashTests {

        @Test
        @DisplayName("创建一致性哈希构建器")
        void testConsistentHashBuilder() {
            var builder = OpenHash.<String>consistentHash();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("构建一致性哈希")
        void testBuildConsistentHash() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            assertThat(ch).isNotNull();
            assertThat(ch.getNodeCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("布隆过滤器测试")
    class BloomFilterTests {

        @Test
        @DisplayName("创建布隆过滤器构建器")
        void testBloomFilterBuilder() {
            var builder = OpenHash.<String>bloomFilter();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("构建布隆过滤器")
        void testBuildBloomFilter() {
            BloomFilter<String> filter = OpenHash.<String>bloomFilter()
                    .expectedInsertions(1000)
                    .fpp(0.01)
                    .build();

            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("创建计数布隆过滤器构建器")
        void testCountingBloomFilterBuilder() {
            var builder = OpenHash.<String>countingBloomFilter();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("构建计数布隆过滤器")
        void testBuildCountingBloomFilter() {
            CountingBloomFilter<String> filter = OpenHash.<String>countingBloomFilter()
                    .expectedInsertions(1000)
                    .fpp(0.01)
                    .build();

            assertThat(filter).isNotNull();
        }
    }

    @Nested
    @DisplayName("SimHash测试")
    class SimHashTests {

        @Test
        @DisplayName("创建SimHash构建器")
        void testSimHashBuilder() {
            var builder = OpenHash.simHash();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("构建SimHash")
        void testBuildSimHash() {
            SimHash simHash = OpenHash.simHash()
                    .nGram(3)
                    .bits(64)
                    .build();

            assertThat(simHash).isNotNull();
            assertThat(simHash.bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("哈希计算测试")
    class HashComputationTests {

        @Test
        @DisplayName("计算字符串哈希")
        void testHashString() {
            HashCode hash = OpenHash.murmur3_32().hashUtf8("hello");

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("相同输入产生相同哈希")
        void testConsistentHash() {
            HashFunction hf = OpenHash.murmur3_128();
            HashCode h1 = hf.hashUtf8("test");
            HashCode h2 = hf.hashUtf8("test");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInputs() {
            HashFunction hf = OpenHash.murmur3_128();
            HashCode h1 = hf.hashUtf8("hello");
            HashCode h2 = hf.hashUtf8("world");

            assertThat(h1).isNotEqualTo(h2);
        }
    }
}
