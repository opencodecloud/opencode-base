package cloud.opencode.base.collections.tree;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Trie 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("Trie 测试")
class TrieTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 Trie")
        void testCreate() {
            Trie<Integer> trie = Trie.create();

            assertThat(trie).isNotNull();
            assertThat(trie.isEmpty()).isTrue();
            assertThat(trie.size()).isZero();
        }
    }

    @Nested
    @DisplayName("put 操作测试")
    class PutOperationTests {

        @Test
        @DisplayName("put - 添加新键")
        void testPut() {
            Trie<Integer> trie = Trie.create();

            Integer old = trie.put("hello", 1);

            assertThat(old).isNull();
            assertThat(trie.size()).isEqualTo(1);
            assertThat(trie.get("hello")).isEqualTo(1);
        }

        @Test
        @DisplayName("put - 替换现有键")
        void testPutReplace() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            Integer old = trie.put("hello", 2);

            assertThat(old).isEqualTo(1);
            assertThat(trie.size()).isEqualTo(1);
            assertThat(trie.get("hello")).isEqualTo(2);
        }

        @Test
        @DisplayName("put - null 键抛异常")
        void testPutNullKey() {
            Trie<Integer> trie = Trie.create();

            assertThatThrownBy(() -> trie.put(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - null 值允许")
        void testPutNullValue() {
            Trie<Integer> trie = Trie.create();

            trie.put("hello", null);

            assertThat(trie.containsKey("hello")).isTrue();
            assertThat(trie.get("hello")).isNull();
        }

        @Test
        @DisplayName("put - 多个键")
        void testPutMultiple() {
            Trie<Integer> trie = Trie.create();

            trie.put("hello", 1);
            trie.put("help", 2);
            trie.put("world", 3);

            assertThat(trie.size()).isEqualTo(3);
            assertThat(trie.get("hello")).isEqualTo(1);
            assertThat(trie.get("help")).isEqualTo(2);
            assertThat(trie.get("world")).isEqualTo(3);
        }

        @Test
        @DisplayName("put - 空键")
        void testPutEmptyKey() {
            Trie<Integer> trie = Trie.create();

            trie.put("", 1);

            assertThat(trie.containsKey("")).isTrue();
            assertThat(trie.get("")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("get 操作测试")
    class GetOperationTests {

        @Test
        @DisplayName("get - 存在的键")
        void testGet() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            assertThat(trie.get("hello")).isEqualTo(1);
        }

        @Test
        @DisplayName("get - 不存在的键")
        void testGetNonExistent() {
            Trie<Integer> trie = Trie.create();

            assertThat(trie.get("hello")).isNull();
        }

        @Test
        @DisplayName("get - 前缀但不是键")
        void testGetPrefix() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            assertThat(trie.get("hel")).isNull();  // prefix exists but not a key
        }
    }

    @Nested
    @DisplayName("containsKey 测试")
    class ContainsKeyTests {

        @Test
        @DisplayName("containsKey - 存在的键")
        void testContainsKey() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            assertThat(trie.containsKey("hello")).isTrue();
        }

        @Test
        @DisplayName("containsKey - 不存在的键")
        void testContainsKeyNonExistent() {
            Trie<Integer> trie = Trie.create();

            assertThat(trie.containsKey("hello")).isFalse();
        }

        @Test
        @DisplayName("containsKey - 前缀但不是键")
        void testContainsKeyPrefix() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            assertThat(trie.containsKey("hel")).isFalse();
        }
    }

    @Nested
    @DisplayName("remove 操作测试")
    class RemoveOperationTests {

        @Test
        @DisplayName("remove - 移除存在的键")
        void testRemove() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            Integer old = trie.remove("hello");

            assertThat(old).isEqualTo(1);
            assertThat(trie.containsKey("hello")).isFalse();
            assertThat(trie.size()).isZero();
        }

        @Test
        @DisplayName("remove - 移除不存在的键")
        void testRemoveNonExistent() {
            Trie<Integer> trie = Trie.create();

            Integer old = trie.remove("hello");

            assertThat(old).isNull();
        }

        @Test
        @DisplayName("remove - 移除键后前缀仍可用")
        void testRemovePreservesPrefix() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);
            trie.put("help", 2);

            trie.remove("hello");

            assertThat(trie.hasPrefix("hel")).isTrue();
            assertThat(trie.containsKey("help")).isTrue();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);
            trie.put("help", 2);

            trie.clear();

            assertThat(trie.isEmpty()).isTrue();
            assertThat(trie.size()).isZero();
        }
    }

    @Nested
    @DisplayName("hasPrefix 测试")
    class HasPrefixTests {

        @Test
        @DisplayName("hasPrefix - 存在的前缀")
        void testHasPrefix() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);
            trie.put("help", 2);

            assertThat(trie.hasPrefix("hel")).isTrue();
            assertThat(trie.hasPrefix("hello")).isTrue();
        }

        @Test
        @DisplayName("hasPrefix - 不存在的前缀")
        void testHasPrefixNonExistent() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            assertThat(trie.hasPrefix("world")).isFalse();
        }

        @Test
        @DisplayName("hasPrefix - 空前缀")
        void testHasPrefixEmpty() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            assertThat(trie.hasPrefix("")).isTrue();
        }
    }

    @Nested
    @DisplayName("keysWithPrefix 测试")
    class KeysWithPrefixTests {

        @Test
        @DisplayName("keysWithPrefix - 匹配的键")
        void testKeysWithPrefix() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);
            trie.put("help", 2);
            trie.put("world", 3);

            List<String> keys = trie.keysWithPrefix("hel");

            assertThat(keys).containsExactlyInAnyOrder("hello", "help");
        }

        @Test
        @DisplayName("keysWithPrefix - 无匹配")
        void testKeysWithPrefixNoMatch() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            List<String> keys = trie.keysWithPrefix("world");

            assertThat(keys).isEmpty();
        }

        @Test
        @DisplayName("keysWithPrefix - 空前缀返回所有键")
        void testKeysWithEmptyPrefix() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);
            trie.put("help", 2);
            trie.put("world", 3);

            List<String> keys = trie.keysWithPrefix("");

            assertThat(keys).containsExactlyInAnyOrder("hello", "help", "world");
        }
    }

    @Nested
    @DisplayName("keys 测试")
    class KeysTests {

        @Test
        @DisplayName("keys - 获取所有键")
        void testKeys() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);
            trie.put("help", 2);
            trie.put("world", 3);

            List<String> keys = trie.keys();

            assertThat(keys).containsExactlyInAnyOrder("hello", "help", "world");
        }

        @Test
        @DisplayName("keys - 空 Trie")
        void testKeysEmpty() {
            Trie<Integer> trie = Trie.create();

            List<String> keys = trie.keys();

            assertThat(keys).isEmpty();
        }
    }

    @Nested
    @DisplayName("longestPrefixOf 测试")
    class LongestPrefixOfTests {

        @Test
        @DisplayName("longestPrefixOf - 完全匹配")
        void testLongestPrefixOfExactMatch() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            String prefix = trie.longestPrefixOf("hello");

            assertThat(prefix).isEqualTo("hello");
        }

        @Test
        @DisplayName("longestPrefixOf - 部分匹配")
        void testLongestPrefixOfPartialMatch() {
            Trie<Integer> trie = Trie.create();
            trie.put("hel", 1);
            trie.put("hello", 2);

            String prefix = trie.longestPrefixOf("helloworld");

            assertThat(prefix).isEqualTo("hello");
        }

        @Test
        @DisplayName("longestPrefixOf - 无匹配")
        void testLongestPrefixOfNoMatch() {
            Trie<Integer> trie = Trie.create();
            trie.put("hello", 1);

            String prefix = trie.longestPrefixOf("world");

            assertThat(prefix).isEmpty();
        }

        @Test
        @DisplayName("longestPrefixOf - 多个前缀")
        void testLongestPrefixOfMultiple() {
            Trie<Integer> trie = Trie.create();
            trie.put("a", 1);
            trie.put("ab", 2);
            trie.put("abc", 3);

            assertThat(trie.longestPrefixOf("abcd")).isEqualTo("abc");
            assertThat(trie.longestPrefixOf("ab")).isEqualTo("ab");
            assertThat(trie.longestPrefixOf("a")).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("size/isEmpty 测试")
    class SizeTests {

        @Test
        @DisplayName("size - 统计键数量")
        void testSize() {
            Trie<Integer> trie = Trie.create();

            assertThat(trie.size()).isZero();

            trie.put("hello", 1);
            assertThat(trie.size()).isEqualTo(1);

            trie.put("help", 2);
            assertThat(trie.size()).isEqualTo(2);

            trie.put("hello", 3);  // replace
            assertThat(trie.size()).isEqualTo(2);  // size unchanged
        }

        @Test
        @DisplayName("isEmpty - 空检查")
        void testIsEmpty() {
            Trie<Integer> trie = Trie.create();

            assertThat(trie.isEmpty()).isTrue();

            trie.put("hello", 1);
            assertThat(trie.isEmpty()).isFalse();

            trie.remove("hello");
            assertThat(trie.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("复杂场景测试")
    class ComplexScenarioTests {

        @Test
        @DisplayName("自动完成场景")
        void testAutocomplete() {
            Trie<Integer> trie = Trie.create();

            // Add some words
            trie.put("apple", 1);
            trie.put("application", 2);
            trie.put("apply", 3);
            trie.put("banana", 4);
            trie.put("band", 5);

            // Autocomplete for "app"
            List<String> suggestions = trie.keysWithPrefix("app");

            assertThat(suggestions).containsExactlyInAnyOrder("apple", "application", "apply");
        }

        @Test
        @DisplayName("字典场景")
        void testDictionary() {
            Trie<String> dictionary = Trie.create();

            dictionary.put("hello", "A greeting");
            dictionary.put("help", "To assist");
            dictionary.put("world", "The earth");

            assertThat(dictionary.get("hello")).isEqualTo("A greeting");
            assertThat(dictionary.containsKey("world")).isTrue();
            assertThat(dictionary.hasPrefix("hel")).isTrue();
        }
    }
}
