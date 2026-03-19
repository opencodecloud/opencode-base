package cloud.opencode.base.collections.tree;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Trie - Prefix Tree Implementation
 * Trie - 前缀树实现
 *
 * <p>A trie (prefix tree) for efficient string operations.</p>
 * <p>用于高效字符串操作的字典树（前缀树）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fast prefix matching - 快速前缀匹配</li>
 *   <li>Autocomplete support - 自动完成支持</li>
 *   <li>Memory efficient for common prefixes - 共同前缀的内存高效</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Trie<Integer> trie = Trie.create();
 * trie.put("hello", 1);
 * trie.put("help", 2);
 * trie.put("world", 3);
 *
 * Integer value = trie.get("hello");  // 1
 * boolean hasPrefix = trie.hasPrefix("hel");  // true
 * List<String> words = trie.keysWithPrefix("hel");  // ["hello", "help"]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>put: O(m) where m is key length - put: O(m) 其中 m 是键长度</li>
 *   <li>get: O(m) - get: O(m)</li>
 *   <li>hasPrefix: O(m) - hasPrefix: O(m)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Values can be null, keys cannot - 空值安全: 值可以为 null，键不能</li>
 * </ul>
 *
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class Trie<V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final TrieNode<V> root;
    private int size;

    // ==================== 构造方法 | Constructors ====================

    private Trie() {
        this.root = new TrieNode<>();
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty Trie.
     * 创建空 Trie。
     *
     * @param <V> value type | 值类型
     * @return new empty Trie | 新空 Trie
     */
    public static <V> Trie<V> create() {
        return new Trie<>();
    }

    // ==================== 操作方法 | Operation Methods ====================

    /**
     * Put a key-value pair.
     * 放入键值对。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return the old value or null | 旧值或 null
     */
    public V put(String key, V value) {
        Objects.requireNonNull(key, "Key cannot be null");

        TrieNode<V> node = root;
        for (char c : key.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode<>());
        }

        V oldValue = node.value;
        node.value = value;
        if (!node.isEndOfWord) {
            node.isEndOfWord = true;
            size++;
        }
        return oldValue;
    }

    /**
     * Get the value for a key.
     * 获取键对应的值。
     *
     * @param key the key | 键
     * @return the value or null | 值或 null
     */
    public V get(String key) {
        Objects.requireNonNull(key);
        TrieNode<V> node = findNode(key);
        return (node != null && node.isEndOfWord) ? node.value : null;
    }

    /**
     * Check if the key exists.
     * 检查键是否存在。
     *
     * @param key the key | 键
     * @return true if exists | 如果存在则返回 true
     */
    public boolean containsKey(String key) {
        Objects.requireNonNull(key);
        TrieNode<V> node = findNode(key);
        return node != null && node.isEndOfWord;
    }

    /**
     * Remove a key.
     * 移除键。
     *
     * @param key the key | 键
     * @return the old value or null | 旧值或 null
     */
    public V remove(String key) {
        Objects.requireNonNull(key);
        return remove(root, key, 0);
    }

    /**
     * Check if any key starts with the prefix.
     * 检查是否有键以该前缀开头。
     *
     * @param prefix the prefix | 前缀
     * @return true if has prefix | 如果有前缀则返回 true
     */
    public boolean hasPrefix(String prefix) {
        Objects.requireNonNull(prefix);
        return findNode(prefix) != null;
    }

    /**
     * Get all keys with the prefix.
     * 获取所有具有该前缀的键。
     *
     * @param prefix the prefix | 前缀
     * @return list of keys | 键列表
     */
    public List<String> keysWithPrefix(String prefix) {
        Objects.requireNonNull(prefix);
        List<String> result = new ArrayList<>();
        TrieNode<V> node = findNode(prefix);
        if (node != null) {
            collectKeys(node, new StringBuilder(prefix), result);
        }
        return result;
    }

    /**
     * Get all keys.
     * 获取所有键。
     *
     * @return list of keys | 键列表
     */
    public List<String> keys() {
        return keysWithPrefix("");
    }

    /**
     * Get the longest prefix of the input that is in the trie.
     * 获取输入中在字典树中的最长前缀。
     *
     * @param input the input string | 输入字符串
     * @return the longest prefix | 最长前缀
     */
    public String longestPrefixOf(String input) {
        Objects.requireNonNull(input);
        int length = 0;
        TrieNode<V> node = root;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            node = node.children.get(c);
            if (node == null) {
                break;
            }
            if (node.isEndOfWord) {
                length = i + 1;
            }
        }
        return input.substring(0, length);
    }

    /**
     * Return the size of this trie.
     * 返回此字典树的大小。
     *
     * @return the size | 大小
     */
    public int size() {
        return size;
    }

    /**
     * Check if this trie is empty.
     * 检查此字典树是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clear this trie.
     * 清空此字典树。
     */
    public void clear() {
        root.children.clear();
        root.isEndOfWord = false;
        root.value = null;
        size = 0;
    }

    // ==================== 私有方法 | Private Methods ====================

    private TrieNode<V> findNode(String key) {
        TrieNode<V> node = root;
        for (char c : key.toCharArray()) {
            node = node.children.get(c);
            if (node == null) {
                return null;
            }
        }
        return node;
    }

    private V remove(TrieNode<V> node, String key, int depth) {
        if (depth == key.length()) {
            if (!node.isEndOfWord) {
                return null;
            }
            V oldValue = node.value;
            node.isEndOfWord = false;
            node.value = null;
            size--;
            return oldValue;
        }

        char c = key.charAt(depth);
        TrieNode<V> child = node.children.get(c);
        if (child == null) {
            return null;
        }

        V oldValue = remove(child, key, depth + 1);

        // Remove child if it's no longer needed
        if (!child.isEndOfWord && child.children.isEmpty()) {
            node.children.remove(c);
        }

        return oldValue;
    }

    private void collectKeys(TrieNode<V> node, StringBuilder prefix, List<String> result) {
        if (node.isEndOfWord) {
            result.add(prefix.toString());
        }
        for (Map.Entry<Character, TrieNode<V>> entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            collectKeys(entry.getValue(), prefix, result);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    // ==================== 内部类 | Internal Classes ====================

    private static class TrieNode<V> implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        Map<Character, TrieNode<V>> children = new HashMap<>();
        boolean isEndOfWord = false;
        V value;
    }
}
