package cloud.opencode.base.hash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Funnel 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("Funnel 测试")
class FunnelTest {

    @Nested
    @DisplayName("STRING_FUNNEL测试")
    class StringFunnelTests {

        @Test
        @DisplayName("字符串Funnel存在")
        void testStringFunnelExists() {
            Funnel<CharSequence> funnel = Funnel.STRING_FUNNEL;

            assertThat(funnel).isNotNull();
        }

        @Test
        @DisplayName("字符串Funnel哈希")
        void testStringFunnelHash() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode hash = hf.hashObject("hello", Funnel.STRING_FUNNEL);

            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("相同字符串产生相同哈希")
        void testStringFunnelConsistent() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode h1 = hf.hashObject("test", Funnel.STRING_FUNNEL);
            HashCode h2 = hf.hashObject("test", Funnel.STRING_FUNNEL);

            assertThat(h1).isEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("BYTE_ARRAY_FUNNEL测试")
    class ByteArrayFunnelTests {

        @Test
        @DisplayName("字节数组Funnel存在")
        void testByteArrayFunnelExists() {
            Funnel<byte[]> funnel = Funnel.BYTE_ARRAY_FUNNEL;

            assertThat(funnel).isNotNull();
        }

        @Test
        @DisplayName("字节数组Funnel哈希")
        void testByteArrayFunnelHash() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode hash = hf.hashObject(new byte[]{1, 2, 3}, Funnel.BYTE_ARRAY_FUNNEL);

            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("相同字节数组产生相同哈希")
        void testByteArrayFunnelConsistent() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode h1 = hf.hashObject(new byte[]{1, 2, 3}, Funnel.BYTE_ARRAY_FUNNEL);
            HashCode h2 = hf.hashObject(new byte[]{1, 2, 3}, Funnel.BYTE_ARRAY_FUNNEL);

            assertThat(h1).isEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("INTEGER_FUNNEL测试")
    class IntegerFunnelTests {

        @Test
        @DisplayName("整数Funnel存在")
        void testIntegerFunnelExists() {
            Funnel<Integer> funnel = Funnel.INTEGER_FUNNEL;

            assertThat(funnel).isNotNull();
        }

        @Test
        @DisplayName("整数Funnel哈希")
        void testIntegerFunnelHash() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode hash = hf.hashObject(42, Funnel.INTEGER_FUNNEL);

            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("不同整数产生不同哈希")
        void testIntegerFunnelDifferent() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode h1 = hf.hashObject(1, Funnel.INTEGER_FUNNEL);
            HashCode h2 = hf.hashObject(2, Funnel.INTEGER_FUNNEL);

            assertThat(h1).isNotEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("LONG_FUNNEL测试")
    class LongFunnelTests {

        @Test
        @DisplayName("长整数Funnel存在")
        void testLongFunnelExists() {
            Funnel<Long> funnel = Funnel.LONG_FUNNEL;

            assertThat(funnel).isNotNull();
        }

        @Test
        @DisplayName("长整数Funnel哈希")
        void testLongFunnelHash() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode hash = hf.hashObject(123456789L, Funnel.LONG_FUNNEL);

            assertThat(hash).isNotNull();
        }
    }

    @Nested
    @DisplayName("DOUBLE_FUNNEL测试")
    class DoubleFunnelTests {

        @Test
        @DisplayName("双精度Funnel存在")
        void testDoubleFunnelExists() {
            Funnel<Double> funnel = Funnel.DOUBLE_FUNNEL;

            assertThat(funnel).isNotNull();
        }

        @Test
        @DisplayName("双精度Funnel哈希")
        void testDoubleFunnelHash() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode hash = hf.hashObject(3.14, Funnel.DOUBLE_FUNNEL);

            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("不同双精度值产生不同哈希")
        void testDoubleFunnelDifferent() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode h1 = hf.hashObject(1.0, Funnel.DOUBLE_FUNNEL);
            HashCode h2 = hf.hashObject(2.0, Funnel.DOUBLE_FUNNEL);

            assertThat(h1).isNotEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("BOOLEAN_FUNNEL测试")
    class BooleanFunnelTests {

        @Test
        @DisplayName("布尔Funnel存在")
        void testBooleanFunnelExists() {
            Funnel<Boolean> funnel = Funnel.BOOLEAN_FUNNEL;

            assertThat(funnel).isNotNull();
        }

        @Test
        @DisplayName("布尔Funnel哈希")
        void testBooleanFunnelHash() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode hashTrue = hf.hashObject(true, Funnel.BOOLEAN_FUNNEL);
            HashCode hashFalse = hf.hashObject(false, Funnel.BOOLEAN_FUNNEL);

            assertThat(hashTrue).isNotEqualTo(hashFalse);
        }
    }

    @Nested
    @DisplayName("CHARACTER_FUNNEL测试")
    class CharacterFunnelTests {

        @Test
        @DisplayName("字符Funnel存在")
        void testCharacterFunnelExists() {
            Funnel<Character> funnel = Funnel.CHARACTER_FUNNEL;

            assertThat(funnel).isNotNull();
        }

        @Test
        @DisplayName("字符Funnel哈希")
        void testCharacterFunnelHash() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode hash = hf.hashObject('A', Funnel.CHARACTER_FUNNEL);

            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("不同字符产生不同哈希")
        void testCharacterFunnelDifferent() {
            HashFunction hf = OpenHash.murmur3_32();
            HashCode h1 = hf.hashObject('A', Funnel.CHARACTER_FUNNEL);
            HashCode h2 = hf.hashObject('B', Funnel.CHARACTER_FUNNEL);

            assertThat(h1).isNotEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("自定义Funnel测试")
    class CustomFunnelTests {

        record Person(String name, int age) {}

        @Test
        @DisplayName("创建自定义Funnel")
        void testCustomFunnel() {
            Funnel<Person> personFunnel = (person, hasher) -> {
                hasher.putUtf8(person.name());
                hasher.putInt(person.age());
            };

            HashFunction hf = OpenHash.murmur3_32();
            HashCode hash = hf.hashObject(new Person("Alice", 30), personFunnel);

            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("自定义Funnel一致性")
        void testCustomFunnelConsistency() {
            Funnel<Person> personFunnel = (person, hasher) -> {
                hasher.putUtf8(person.name());
                hasher.putInt(person.age());
            };

            HashFunction hf = OpenHash.murmur3_32();
            Person p = new Person("Bob", 25);
            HashCode h1 = hf.hashObject(p, personFunnel);
            HashCode h2 = hf.hashObject(p, personFunnel);

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同对象产生不同哈希")
        void testCustomFunnelDifferent() {
            Funnel<Person> personFunnel = (person, hasher) -> {
                hasher.putUtf8(person.name());
                hasher.putInt(person.age());
            };

            HashFunction hf = OpenHash.murmur3_32();
            HashCode h1 = hf.hashObject(new Person("Alice", 30), personFunnel);
            HashCode h2 = hf.hashObject(new Person("Bob", 25), personFunnel);

            assertThat(h1).isNotEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("Funnel与Hasher交互测试")
    class FunnelHasherTests {

        @Test
        @DisplayName("通过newHasher使用Funnel")
        void testFunnelWithHasher() {
            Funnel<CharSequence> funnel = Funnel.STRING_FUNNEL;
            Hasher hasher = OpenHash.murmur3_32().newHasher();
            hasher.putObject("test", funnel);
            HashCode hash = hasher.hash();

            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("多对象组合哈希")
        void testMultipleObjectsHash() {
            Hasher hasher = OpenHash.murmur3_32().newHasher();
            hasher.putObject("hello", Funnel.STRING_FUNNEL);
            hasher.putObject(42, Funnel.INTEGER_FUNNEL);
            hasher.putObject(true, Funnel.BOOLEAN_FUNNEL);
            HashCode hash = hasher.hash();

            assertThat(hash).isNotNull();
        }
    }
}
