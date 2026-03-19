package cloud.opencode.base.string.builder;

import java.util.function.Predicate;

/**
 * Character Matcher (Guava Style)
 * 字符匹配器（Guava风格）
 *
 * <p>Represents a set of characters for matching, removing, retaining, or replacing operations.</p>
 * <p>表示用于匹配、移除、保留或替换操作的字符集合。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Predefined character sets - 预定义字符集</li>
 *   <li>Custom character matching - 自定义字符匹配</li>
 *   <li>Boolean operations (and, or, negate) - 布尔运算</li>
 *   <li>String transformation operations - 字符串转换操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Remove whitespace
 * String result = CharMatcher.whitespace().removeFrom("a b c"); // "abc"
 *
 * // Retain only digits
 * String digits = CharMatcher.digit().retainFrom("abc123"); // "123"
 *
 * // Replace characters
 * String replaced = CharMatcher.is('a').replaceFrom("banana", '*'); // "b*n*n*"
 *
 * // Collapse consecutive whitespace
 * String collapsed = CharMatcher.whitespace().collapseFrom("a   b", ' '); // "a b"
 * }</pre>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for most operations - 大多数操作时间复杂度: O(n)</li>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public abstract class CharMatcher {

    // Cached instances
    private static final CharMatcher ANY = new AnyMatcher();
    private static final CharMatcher NONE = new NoneMatcher();
    private static final CharMatcher WHITESPACE = new WhitespaceMatcher();
    private static final CharMatcher DIGIT = new DigitMatcher();
    private static final CharMatcher ASCII = new AsciiMatcher();
    private static final CharMatcher JAVA_LETTER = new JavaLetterMatcher();
    private static final CharMatcher JAVA_LETTER_OR_DIGIT = new JavaLetterOrDigitMatcher();

    // ==================== Predefined Matchers | 预定义匹配器 ====================

    public static CharMatcher any() { return ANY; }
    public static CharMatcher none() { return NONE; }
    public static CharMatcher whitespace() { return WHITESPACE; }
    public static CharMatcher breakingWhitespace() { return WHITESPACE; }
    public static CharMatcher invisible() { return new InvisibleMatcher(); }
    public static CharMatcher digit() { return DIGIT; }
    public static CharMatcher javaLetter() { return JAVA_LETTER; }
    public static CharMatcher javaDigit() { return DIGIT; }
    public static CharMatcher javaLetterOrDigit() { return JAVA_LETTER_OR_DIGIT; }
    public static CharMatcher javaUpperCase() { return new JavaUpperCaseMatcher(); }
    public static CharMatcher javaLowerCase() { return new JavaLowerCaseMatcher(); }
    public static CharMatcher ascii() { return ASCII; }

    // ==================== Factory Methods | 工厂方法 ====================

    public static CharMatcher is(char match) {
        return new IsMatcher(match);
    }

    public static CharMatcher isNot(char match) {
        return new IsMatcher(match).negate();
    }

    public static CharMatcher anyOf(CharSequence sequence) {
        return new AnyOfMatcher(sequence.toString());
    }

    public static CharMatcher noneOf(CharSequence sequence) {
        return anyOf(sequence).negate();
    }

    public static CharMatcher inRange(char startInclusive, char endInclusive) {
        return new RangeMatcher(startInclusive, endInclusive);
    }

    public static CharMatcher forPredicate(Predicate<Character> predicate) {
        return new PredicateMatcher(predicate);
    }

    // ==================== Abstract Method | 抽象方法 ====================

    public abstract boolean matches(char c);

    // ==================== Combination Methods | 组合方法 ====================

    public CharMatcher and(CharMatcher other) {
        return new AndMatcher(this, other);
    }

    public CharMatcher or(CharMatcher other) {
        return new OrMatcher(this, other);
    }

    public CharMatcher negate() {
        return new NegateMatcher(this);
    }

    // ==================== Query Methods | 查询方法 ====================

    public boolean matchesAnyOf(CharSequence sequence) {
        for (int i = 0; i < sequence.length(); i++) {
            if (matches(sequence.charAt(i))) return true;
        }
        return false;
    }

    public boolean matchesAllOf(CharSequence sequence) {
        for (int i = 0; i < sequence.length(); i++) {
            if (!matches(sequence.charAt(i))) return false;
        }
        return !sequence.isEmpty();
    }

    public boolean matchesNoneOf(CharSequence sequence) {
        return !matchesAnyOf(sequence);
    }

    // ==================== Index Methods | 索引方法 ====================

    public int indexIn(CharSequence sequence) {
        return indexIn(sequence, 0);
    }

    public int indexIn(CharSequence sequence, int start) {
        for (int i = start; i < sequence.length(); i++) {
            if (matches(sequence.charAt(i))) return i;
        }
        return -1;
    }

    public int lastIndexIn(CharSequence sequence) {
        for (int i = sequence.length() - 1; i >= 0; i--) {
            if (matches(sequence.charAt(i))) return i;
        }
        return -1;
    }

    public int countIn(CharSequence sequence) {
        int count = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if (matches(sequence.charAt(i))) count++;
        }
        return count;
    }

    // ==================== Transformation Methods | 转换方法 ====================

    public String removeFrom(CharSequence sequence) {
        StringBuilder result = new StringBuilder(sequence.length());
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            if (!matches(c)) result.append(c);
        }
        return result.toString();
    }

    public String retainFrom(CharSequence sequence) {
        return negate().removeFrom(sequence);
    }

    public String replaceFrom(CharSequence sequence, char replacement) {
        StringBuilder result = new StringBuilder(sequence.length());
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            result.append(matches(c) ? replacement : c);
        }
        return result.toString();
    }

    public String replaceFrom(CharSequence sequence, CharSequence replacement) {
        String replacementStr = replacement.toString();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            result.append(matches(c) ? replacementStr : String.valueOf(c));
        }
        return result.toString();
    }

    public String collapseFrom(CharSequence sequence, char replacement) {
        StringBuilder result = new StringBuilder(sequence.length());
        boolean inMatchingRegion = false;
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            if (matches(c)) {
                if (!inMatchingRegion) {
                    result.append(replacement);
                    inMatchingRegion = true;
                }
            } else {
                result.append(c);
                inMatchingRegion = false;
            }
        }
        return result.toString();
    }

    public String trimFrom(CharSequence sequence) {
        int len = sequence.length();
        int first = 0;
        int last = len - 1;

        while (first < len && matches(sequence.charAt(first))) first++;
        while (last > first && matches(sequence.charAt(last))) last--;

        return sequence.subSequence(first, last + 1).toString();
    }

    public String trimLeadingFrom(CharSequence sequence) {
        int len = sequence.length();
        int first = 0;
        while (first < len && matches(sequence.charAt(first))) first++;
        return sequence.subSequence(first, len).toString();
    }

    public String trimTrailingFrom(CharSequence sequence) {
        int len = sequence.length();
        int last = len - 1;
        while (last >= 0 && matches(sequence.charAt(last))) last--;
        return sequence.subSequence(0, last + 1).toString();
    }

    public String trimAndCollapseFrom(CharSequence sequence, char replacement) {
        return collapseFrom(trimFrom(sequence), replacement);
    }

    // ==================== Implementation Classes | 实现类 ====================

    private static final class AnyMatcher extends CharMatcher {
        @Override public boolean matches(char c) { return true; }
    }

    private static final class NoneMatcher extends CharMatcher {
        @Override public boolean matches(char c) { return false; }
    }

    private static final class WhitespaceMatcher extends CharMatcher {
        @Override public boolean matches(char c) { return Character.isWhitespace(c); }
    }

    private static final class InvisibleMatcher extends CharMatcher {
        @Override public boolean matches(char c) {
            return Character.isWhitespace(c) || Character.isISOControl(c);
        }
    }

    private static final class DigitMatcher extends CharMatcher {
        @Override public boolean matches(char c) { return c >= '0' && c <= '9'; }
    }

    private static final class JavaLetterMatcher extends CharMatcher {
        @Override public boolean matches(char c) { return Character.isLetter(c); }
    }

    private static final class JavaLetterOrDigitMatcher extends CharMatcher {
        @Override public boolean matches(char c) { return Character.isLetterOrDigit(c); }
    }

    private static final class JavaUpperCaseMatcher extends CharMatcher {
        @Override public boolean matches(char c) { return Character.isUpperCase(c); }
    }

    private static final class JavaLowerCaseMatcher extends CharMatcher {
        @Override public boolean matches(char c) { return Character.isLowerCase(c); }
    }

    private static final class AsciiMatcher extends CharMatcher {
        @Override public boolean matches(char c) { return c < 128; }
    }

    private static final class IsMatcher extends CharMatcher {
        private final char match;
        IsMatcher(char match) { this.match = match; }
        @Override public boolean matches(char c) { return c == match; }
    }

    private static final class AnyOfMatcher extends CharMatcher {
        private final String chars;
        AnyOfMatcher(String chars) { this.chars = chars; }
        @Override public boolean matches(char c) { return chars.indexOf(c) >= 0; }
    }

    private static final class RangeMatcher extends CharMatcher {
        private final char start, end;
        RangeMatcher(char start, char end) {
            this.start = start;
            this.end = end;
        }
        @Override public boolean matches(char c) { return c >= start && c <= end; }
    }

    private static final class PredicateMatcher extends CharMatcher {
        private final Predicate<Character> predicate;
        PredicateMatcher(Predicate<Character> predicate) {
            this.predicate = predicate;
        }
        @Override public boolean matches(char c) { return predicate.test(c); }
    }

    private static final class AndMatcher extends CharMatcher {
        private final CharMatcher first, second;
        AndMatcher(CharMatcher first, CharMatcher second) {
            this.first = first;
            this.second = second;
        }
        @Override public boolean matches(char c) {
            return first.matches(c) && second.matches(c);
        }
    }

    private static final class OrMatcher extends CharMatcher {
        private final CharMatcher first, second;
        OrMatcher(CharMatcher first, CharMatcher second) {
            this.first = first;
            this.second = second;
        }
        @Override public boolean matches(char c) {
            return first.matches(c) || second.matches(c);
        }
    }

    private static final class NegateMatcher extends CharMatcher {
        private final CharMatcher original;
        NegateMatcher(CharMatcher original) { this.original = original; }
        @Override public boolean matches(char c) { return !original.matches(c); }
    }
}
