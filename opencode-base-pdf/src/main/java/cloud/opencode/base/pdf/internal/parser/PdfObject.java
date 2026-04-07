package cloud.opencode.base.pdf.internal.parser;

import java.util.*;

/**
 * PDF Object Model - Represents all PDF object types as defined in ISO 32000
 * PDF 对象模型 - 表示 ISO 32000 定义的所有 PDF 对象类型
 *
 * <p>A sealed interface hierarchy that models the eight fundamental PDF object types:
 * null, boolean, number, string, name, array, dictionary, stream, and indirect reference.</p>
 * <p>密封接口层次结构，建模八种基本 PDF 对象类型：空值、布尔值、数字、字符串、名称、
 * 数组、字典、流和间接引用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed type hierarchy for exhaustive pattern matching - 密封类型层次结构，支持穷举模式匹配</li>
 *   <li>Immutable record types for thread safety - 不可变记录类型，保证线程安全</li>
 *   <li>Convenience accessors on PdfDictionary for common lookups - PdfDictionary 上的便捷访问方法</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — all types are immutable records - 线程安全: 是 — 所有类型为不可变记录</li>
 *   <li>PdfStream defensively copies byte data - PdfStream 防御性复制字节数据</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
public sealed interface PdfObject
        permits PdfObject.PdfNull, PdfObject.PdfBoolean, PdfObject.PdfNumber,
                PdfObject.PdfString, PdfObject.PdfName, PdfObject.PdfArray,
                PdfObject.PdfDictionary, PdfObject.PdfStream, PdfObject.PdfReference {

    /**
     * PDF null object
     * PDF 空对象
     */
    record PdfNull() implements PdfObject {}

    /**
     * PDF boolean object
     * PDF 布尔对象
     *
     * @param value boolean value | 布尔值
     */
    record PdfBoolean(boolean value) implements PdfObject {}

    /**
     * PDF numeric object (integer or real)
     * PDF 数字对象（整数或实数）
     *
     * @param value numeric value | 数值
     */
    record PdfNumber(double value) implements PdfObject {

        /**
         * Returns value as int
         * 返回整数值
         *
         * @return int value | 整数值
         */
        public int intValue() {
            return (int) value;
        }

        /**
         * Returns value as float
         * 返回浮点数值
         *
         * @return float value | 浮点数值
         */
        public float floatValue() {
            return (float) value;
        }

        /**
         * Returns value as long
         * 返回长整数值
         *
         * @return long value | 长整数值
         */
        public long longValue() {
            return (long) value;
        }
    }

    /**
     * PDF string object (literal or hexadecimal)
     * PDF 字符串对象（文字或十六进制）
     *
     * @param value decoded string value | 解码后的字符串值
     */
    record PdfString(String value) implements PdfObject {
        public PdfString {
            Objects.requireNonNull(value, "value cannot be null");
        }

        /**
         * Returns raw bytes of the string using ISO-8859-1 encoding
         * 使用 ISO-8859-1 编码返回字符串的原始字节
         *
         * @return raw bytes | 原始字节
         */
        public byte[] rawBytes() {
            return value.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        }
    }

    /**
     * PDF name object (e.g. /Type, /Page)
     * PDF 名称对象（如 /Type, /Page）
     *
     * @param value name value without leading slash | 不含前导斜杠的名称值
     */
    record PdfName(String value) implements PdfObject {
        public PdfName {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }

    /**
     * PDF array object
     * PDF 数组对象
     *
     * @param elements array elements | 数组元素
     */
    record PdfArray(List<PdfObject> elements) implements PdfObject {
        public PdfArray {
            Objects.requireNonNull(elements, "elements cannot be null");
            elements = List.copyOf(elements);
        }

        /**
         * Gets element at index
         * 获取指定索引处的元素
         *
         * @param index element index | 元素索引
         * @return element | 元素
         */
        public PdfObject get(int index) {
            return elements.get(index);
        }

        /**
         * Gets array size
         * 获取数组大小
         *
         * @return size | 大小
         */
        public int size() {
            return elements.size();
        }

        /**
         * Checks if array is empty
         * 检查数组是否为空
         *
         * @return true if empty | 为空返回 true
         */
        public boolean isEmpty() {
            return elements.isEmpty();
        }
    }

    /**
     * PDF dictionary object
     * PDF 字典对象
     *
     * @param entries dictionary entries (key is name without slash) | 字典条目（键为不含斜杠的名称）
     */
    record PdfDictionary(Map<String, PdfObject> entries) implements PdfObject {
        public PdfDictionary {
            Objects.requireNonNull(entries, "entries cannot be null");
            entries = Map.copyOf(entries);
        }

        /**
         * Gets entry by key
         * 按键获取条目
         *
         * @param key entry key | 条目键
         * @return entry value, or null if not found | 条目值，未找到返回 null
         */
        public PdfObject get(String key) {
            return entries.get(key);
        }

        /**
         * Checks if dictionary contains key
         * 检查字典是否包含指定键
         *
         * @param key entry key | 条目键
         * @return true if contains | 包含返回 true
         */
        public boolean containsKey(String key) {
            return entries.containsKey(key);
        }

        /**
         * Gets string value for key
         * 获取键对应的字符串值
         *
         * @param key entry key | 条目键
         * @return string value, or null if not found or not a string/name | 字符串值，未找到或类型不匹配返回 null
         */
        public String getString(String key) {
            PdfObject obj = entries.get(key);
            if (obj instanceof PdfString s) return s.value();
            if (obj instanceof PdfName n) return n.value();
            return null;
        }

        /**
         * Gets integer value for key with default
         * 获取键对应的整数值，带默认值
         *
         * @param key          entry key | 条目键
         * @param defaultValue default value | 默认值
         * @return integer value | 整数值
         */
        public int getInt(String key, int defaultValue) {
            PdfObject obj = entries.get(key);
            if (obj instanceof PdfNumber n) return n.intValue();
            return defaultValue;
        }

        /**
         * Gets float value for key with default
         * 获取键对应的浮点值，带默认值
         *
         * @param key          entry key | 条目键
         * @param defaultValue default value | 默认值
         * @return float value | 浮点值
         */
        public float getFloat(String key, float defaultValue) {
            PdfObject obj = entries.get(key);
            if (obj instanceof PdfNumber n) return n.floatValue();
            return defaultValue;
        }

        /**
         * Gets array value for key
         * 获取键对应的数组值
         *
         * @param key entry key | 条目键
         * @return array, or null if not found or not an array | 数组，未找到或类型不匹配返回 null
         */
        public PdfArray getArray(String key) {
            PdfObject obj = entries.get(key);
            if (obj instanceof PdfArray a) return a;
            return null;
        }

        /**
         * Gets dictionary value for key
         * 获取键对应的字典值
         *
         * @param key entry key | 条目键
         * @return dictionary, or null if not found or not a dictionary | 字典，未找到或类型不匹配返回 null
         */
        public PdfDictionary getDictionary(String key) {
            PdfObject obj = entries.get(key);
            if (obj instanceof PdfDictionary d) return d;
            return null;
        }

        /**
         * Gets boolean value for key with default
         * 获取键对应的布尔值，带默认值
         *
         * @param key          entry key | 条目键
         * @param defaultValue default value | 默认值
         * @return boolean value | 布尔值
         */
        public boolean getBoolean(String key, boolean defaultValue) {
            PdfObject obj = entries.get(key);
            if (obj instanceof PdfBoolean b) return b.value();
            return defaultValue;
        }

        /**
         * Gets the number of entries
         * 获取条目数量
         *
         * @return entry count | 条目数量
         */
        public int size() {
            return entries.size();
        }
    }

    /**
     * PDF stream object (dictionary + decoded data bytes)
     * PDF 流对象（字典 + 解码后的数据字节）
     *
     * @param dictionary stream dictionary | 流字典
     * @param data       decoded stream data | 解码后的流数据
     */
    record PdfStream(PdfDictionary dictionary, byte[] data) implements PdfObject {
        public PdfStream {
            Objects.requireNonNull(dictionary, "dictionary cannot be null");
            Objects.requireNonNull(data, "data cannot be null");
            data = data.clone();
        }

        @Override
        public byte[] data() {
            return data.clone();
        }

        /**
         * Gets the length of decoded data
         * 获取解码后数据的长度
         *
         * @return data length | 数据长度
         */
        public int dataLength() {
            return data.length;
        }
    }

    /**
     * PDF indirect reference (object number + generation number)
     * PDF 间接引用（对象号 + 代号）
     *
     * @param objectNumber     object number | 对象号
     * @param generationNumber generation number | 代号
     */
    record PdfReference(int objectNumber, int generationNumber) implements PdfObject {}
}
