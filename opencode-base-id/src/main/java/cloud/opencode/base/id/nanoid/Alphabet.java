package cloud.opencode.base.id.nanoid;

/**
 * Predefined Alphabets for NanoID
 * NanoID预定义字母表
 *
 * <p>Provides commonly used character sets for NanoID generation.</p>
 * <p>提供常用的NanoID生成字符集。</p>
 *
 * <p><strong>Available Alphabets | 可用字母表:</strong></p>
 * <ul>
 *   <li>DEFAULT - URL-safe (64 chars) | URL安全（64字符）</li>
 *   <li>ALPHANUMERIC - Letters and digits | 字母和数字</li>
 *   <li>ALPHABETIC - Letters only | 仅字母</li>
 *   <li>NUMERIC - Digits only | 仅数字</li>
 *   <li>HEX_LOWERCASE/UPPERCASE - Hexadecimal | 十六进制</li>
 *   <li>NOLOOK_ALIKE - No ambiguous chars | 无歧义字符</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * NanoIdGenerator gen = NanoIdGenerator.builder()
 *     .alphabet(Alphabet.ALPHANUMERIC)
 *     .size(16)
 *     .build();
 *
 * String chars = Alphabet.DEFAULT.getChars();
 * int size = Alphabet.NUMERIC.size();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Predefined character sets for NanoID generation - NanoID生成的预定义字符集</li>
 *   <li>URL-safe, numeric, alphanumeric alphabets - URL安全、数字、字母数字字符集</li>
 *   <li>Custom alphabet validation - 自定义字符集验证</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public enum Alphabet {

    /**
     * Default URL-safe alphabet
     * 默认URL安全字母表
     * <p>Contains: A-Z, a-z, 0-9, _, -</p>
     */
    DEFAULT("_-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"),

    /**
     * Alphanumeric alphabet
     * 字母数字字母表
     * <p>Contains: A-Z, a-z, 0-9</p>
     */
    ALPHANUMERIC("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"),

    /**
     * Alphabetic only
     * 仅字母
     * <p>Contains: A-Z, a-z</p>
     */
    ALPHABETIC("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"),

    /**
     * Numeric only
     * 仅数字
     * <p>Contains: 0-9</p>
     */
    NUMERIC("0123456789"),

    /**
     * Hexadecimal lowercase
     * 十六进制小写
     * <p>Contains: 0-9, a-f</p>
     */
    HEX_LOWERCASE("0123456789abcdef"),

    /**
     * Hexadecimal uppercase
     * 十六进制大写
     * <p>Contains: 0-9, A-F</p>
     */
    HEX_UPPERCASE("0123456789ABCDEF"),

    /**
     * No look-alike characters
     * 无歧义字符
     * <p>Excludes: 0, O, 1, l, I to avoid confusion</p>
     * <p>排除：0, O, 1, l, I 以避免混淆</p>
     */
    NOLOOK_ALIKE("2346789ABCDEFGHJKMNPQRTUVWXYabcdefghjkmnpqrtuvwxy");

    private final String chars;

    Alphabet(String chars) {
        this.chars = chars;
    }

    /**
     * Gets the character set
     * 获取字符集
     *
     * @return character string | 字符字符串
     */
    public String getChars() {
        return chars;
    }

    /**
     * Gets the size of the alphabet
     * 获取字母表大小
     *
     * @return number of characters | 字符数量
     */
    public int size() {
        return chars.length();
    }

    /**
     * Gets a character at index
     * 获取指定索引的字符
     *
     * @param index the index | 索引
     * @return character at index | 索引处的字符
     */
    public char charAt(int index) {
        return chars.charAt(index);
    }
}
