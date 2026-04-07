/*
 * Copyright 2025 Leon Soo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.opencode.base.json.util;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.OpenJson;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JSON Equals - Structural JSON Equality Comparison Utility.
 * JSON 相等性比较 - 结构化 JSON 相等性比较工具。
 *
 * <p>Provides deep structural equality comparison between {@link JsonNode} instances.
 * Objects are compared ignoring key order, while arrays respect element order by default.
 * An optional mode allows ignoring array order for set-like comparisons.</p>
 * <p>提供 {@link JsonNode} 实例之间的深度结构相等性比较。
 * 对象比较忽略键顺序，数组默认按元素顺序比较。
 * 可选模式允许忽略数组顺序，用于集合类比较。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Structural equality: objects ignore key order - 结构相等性：对象忽略键顺序</li>
 *   <li>Array order sensitive by default, with option to ignore - 数组默认顺序敏感，可选择忽略</li>
 *   <li>Number comparison by numeric value (1 == 1.0) - 数字按数值比较（1 == 1.0）</li>
 *   <li>String convenience methods for direct JSON string comparison - 字符串便利方法直接比较 JSON 字符串</li>
 *   <li>Depth guard against deeply nested structures - 深度保护防止深度嵌套结构</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * JsonNode a = JsonNode.object().put("x", 1).put("y", 2);
 * JsonNode b = JsonNode.object().put("y", 2).put("x", 1);
 * boolean eq = JsonEquals.equals(a, b); // true
 *
 * // Compare JSON strings directly
 * boolean eq2 = JsonEquals.equals("{\"a\":1}", "{\"a\":1.0}"); // true
 *
 * // Ignore array order
 * JsonNode arr1 = JsonNode.array().add(1).add(2).add(3);
 * JsonNode arr2 = JsonNode.array().add(3).add(2).add(1);
 * boolean eq3 = JsonEquals.equalsIgnoreArrayOrder(arr1, arr2); // true
 * }</pre>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>O(n) for ordered comparison, O(n^2) worst case for unordered array comparison - 有序比较 O(n)，无序数组比较最坏 O(n^2)</li>
 *   <li>Stateless and thread-safe - 无状态且线程安全</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Depth-limited to prevent stack overflow attacks - 深度限制以防止栈溢出攻击</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
public final class JsonEquals {

    /** Maximum recursion depth to prevent stack overflow. */
    private static final int MAX_DEPTH = 1000;

    private JsonEquals() {
        throw new AssertionError("No instances");
    }

    // ==================== Public API ====================

    /**
     * Compares two JSON nodes for structural equality.
     * 比较两个 JSON 节点的结构相等性。
     *
     * <p>Objects are compared ignoring key order. Arrays respect element order.
     * Numbers are compared by numeric value (e.g. 1 equals 1.0).</p>
     * <p>对象比较忽略键顺序。数组按元素顺序比较。数字按数值比较（如 1 等于 1.0）。</p>
     *
     * @param a the first JSON node - 第一个 JSON 节点
     * @param b the second JSON node - 第二个 JSON 节点
     * @return true if structurally equal - 如果结构相等则返回 true
     */
    public static boolean equals(JsonNode a, JsonNode b) {
        return deepEquals(a, b, false, 0);
    }

    /**
     * Compares two JSON strings for structural equality.
     * 比较两个 JSON 字符串的结构相等性。
     *
     * <p>Parses both strings using {@link OpenJson#parse(String)} and then
     * performs structural comparison.</p>
     * <p>使用 {@link OpenJson#parse(String)} 解析两个字符串，然后进行结构比较。</p>
     *
     * @param jsonA the first JSON string - 第一个 JSON 字符串
     * @param jsonB the second JSON string - 第二个 JSON 字符串
     * @return true if structurally equal - 如果结构相等则返回 true
     * @throws OpenJsonProcessingException if either string cannot be parsed - 如果任一字符串无法解析
     * @throws NullPointerException        if either argument is null - 如果任一参数为 null
     */
    public static boolean equals(String jsonA, String jsonB) {
        Objects.requireNonNull(jsonA, "jsonA must not be null");
        Objects.requireNonNull(jsonB, "jsonB must not be null");
        JsonNode a = OpenJson.parse(jsonA);
        JsonNode b = OpenJson.parse(jsonB);
        return equals(a, b);
    }

    /**
     * Compares two JSON nodes for structural equality, ignoring array element order.
     * 比较两个 JSON 节点的结构相等性，忽略数组元素顺序。
     *
     * <p>This is useful for set-like comparisons where the order of array
     * elements does not matter.</p>
     * <p>适用于集合类比较，数组元素的顺序无关紧要。</p>
     *
     * @param a the first JSON node - 第一个 JSON 节点
     * @param b the second JSON node - 第二个 JSON 节点
     * @return true if structurally equal ignoring array order - 如果忽略数组顺序后结构相等则返回 true
     */
    public static boolean equalsIgnoreArrayOrder(JsonNode a, JsonNode b) {
        return deepEquals(a, b, true, 0);
    }

    // ==================== Internal ====================

    /**
     * Recursive deep equality comparison.
     *
     * @param a                 first node
     * @param b                 second node
     * @param ignoreArrayOrder  whether to ignore array element order
     * @param depth             current recursion depth
     * @return true if equal
     */
    private static boolean deepEquals(JsonNode a, JsonNode b, boolean ignoreArrayOrder, int depth) {
        if (depth > MAX_DEPTH) {
            throw new OpenJsonProcessingException(
                    "Maximum comparison depth exceeded: " + MAX_DEPTH,
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }

        // Handle Java null references
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        // Both are JSON null nodes
        if (a.isNull() && b.isNull()) {
            return true;
        }

        // Type mismatch
        if (a.isObject() != b.isObject() || a.isArray() != b.isArray()
                || a.isString() != b.isString() || a.isNumber() != b.isNumber()
                || a.isBoolean() != b.isBoolean() || a.isNull() != b.isNull()) {
            return false;
        }

        // Object comparison: ignore key order
        if (a.isObject()) {
            if (a.size() != b.size()) {
                return false;
            }
            for (String key : a.keys()) {
                if (!b.has(key)) {
                    return false;
                }
                if (!deepEquals(a.get(key), b.get(key), ignoreArrayOrder, depth + 1)) {
                    return false;
                }
            }
            return true;
        }

        // Array comparison
        if (a.isArray()) {
            if (a.size() != b.size()) {
                return false;
            }
            if (ignoreArrayOrder) {
                return arrayEqualsUnordered(a, b, depth);
            } else {
                for (int i = 0; i < a.size(); i++) {
                    if (!deepEquals(a.get(i), b.get(i), false, depth + 1)) {
                        return false;
                    }
                }
                return true;
            }
        }

        // String comparison
        if (a.isString()) {
            return Objects.equals(a.asString(), b.asString());
        }

        // Number comparison: compare by BigDecimal value for cross-type equality
        if (a.isNumber()) {
            return compareNumbers(a, b);
        }

        // Boolean comparison
        if (a.isBoolean()) {
            return a.asBoolean() == b.asBoolean();
        }

        return false;
    }

    /**
     * Compares two number nodes by their numeric value.
     * Uses BigDecimal for precise cross-type comparison (e.g. 1 == 1.0).
     */
    private static boolean compareNumbers(JsonNode a, JsonNode b) {
        BigDecimal bdA = a.asBigDecimal();
        BigDecimal bdB = b.asBigDecimal();
        if (bdA == null || bdB == null) {
            return bdA == bdB;
        }
        return bdA.compareTo(bdB) == 0;
    }

    /**
     * Compares two arrays ignoring element order (set-like comparison).
     * Uses O(n^2) matching with boolean[] to avoid O(n) ArrayList.remove() cost.
     */
    private static boolean arrayEqualsUnordered(JsonNode a, JsonNode b, int depth) {
        int size = a.size();
        boolean[] matched = new boolean[size];

        for (int i = 0; i < size; i++) {
            JsonNode elemA = a.get(i);
            boolean found = false;
            for (int j = 0; j < size; j++) {
                if (!matched[j] && deepEquals(elemA, b.get(j), true, depth + 1)) {
                    matched[j] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
