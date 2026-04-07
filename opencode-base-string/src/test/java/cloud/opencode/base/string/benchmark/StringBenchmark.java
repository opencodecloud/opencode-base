package cloud.opencode.base.string.benchmark;

import cloud.opencode.base.string.OpenString;
import cloud.opencode.base.string.OpenSlug;
import cloud.opencode.base.string.escape.HtmlUtil;
import cloud.opencode.base.string.match.AhoCorasick;
import cloud.opencode.base.string.match.FuzzyMatcher;
import cloud.opencode.base.string.naming.CaseUtil;
import cloud.opencode.base.string.similarity.CosineSimilarity;
import cloud.opencode.base.string.similarity.JaccardSimilarity;
import cloud.opencode.base.string.similarity.LevenshteinDistance;
import cloud.opencode.base.string.template.TemplateEngine;
import cloud.opencode.base.string.text.OpenTruncate;
import cloud.opencode.base.string.unicode.OpenGrapheme;

import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance benchmarks for opencode-base-string V1.0.3.
 * opencode-base-string V1.0.3 性能基准测试。
 *
 * <p>Lightweight nanoTime-loop benchmarks (non-JMH, sufficient for magnitude detection).
 * 轻量级 nanoTime 循环基准测试（非 JMH，足以检测量级问题）。</p>
 *
 * <p>Run with: {@code mvn test -pl opencode-base-string -Dtest="StringBenchmark"}</p>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-string V1.0.3
 */
class StringBenchmark {

    private static final int WARMUP     = 20_000;
    private static final int ITERATIONS = 200_000;

    /** Measure ops/ms and ns/op for a given operation. */
    private static double benchmark(String name, Runnable op) {
        for (int i = 0; i < WARMUP; i++) op.run();

        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) op.run();
        long elapsed = System.nanoTime() - start;

        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);
        double nsPerOp  = (double) elapsed / ITERATIONS;
        System.out.printf("  %-60s %9.0f ops/ms  %7.1f ns/op%n", name, opsPerMs, nsPerOp);
        return nsPerOp;
    }

    // =================== CaseUtil / TemplateEngine / Pad | 命名转换/模板/填充 ===================

    @Nested
    class NamingAndTemplateBenchmarks {

        @Test
        void caseUtil_detect_camelCase() {
            System.out.println("\n=== CaseUtil.detect (single-loop) ===");
            double ns = benchmark("detect(\"getUserNameFromDatabase\")",
                    () -> CaseUtil.detect("getUserNameFromDatabase"));
            assertThat(ns).isLessThan(3_000);
        }

        @Test
        void caseUtil_toCamelCase() {
            System.out.println("\n=== CaseUtil.toCamelCase ===");
            double ns = benchmark("toCamelCase(\"get_user_name_from_db\")",
                    () -> CaseUtil.toCamelCase("get_user_name_from_db"));
            assertThat(ns).isLessThan(5_000);
        }

        @Test
        void caseUtil_toSnakeCase() {
            double ns = benchmark("toSnakeCase(\"getUserNameFromDatabase\")",
                    () -> CaseUtil.toSnakeCase("getUserNameFromDatabase"));
            assertThat(ns).isLessThan(5_000);
        }

        @Test
        void templateEngine_simpleRender() {
            System.out.println("\n=== TemplateEngine.render (3 vars) ===");
            TemplateEngine engine = TemplateEngine.create();
            Map<String, Object> ctx = Map.of("name", "Alice", "count", 42, "status", "active");
            double ns = benchmark("render(\"${name} has ${count} items (${status})\")",
                    () -> engine.render("${name} has ${count} items (${status})", ctx));
            assertThat(ns).isLessThan(5_000);
        }

        @Test
        void padLeft_shortPad() {
            System.out.println("\n=== OpenString.padLeft ===");
            double ns = benchmark("padLeft(\"42\", 8, '0')",
                    () -> OpenString.padLeft("42", 8, '0'));
            assertThat(ns).isLessThan(1_000);
        }

        @Test
        void padLeft_longPad() {
            double ns = benchmark("padLeft(\"x\", 100, '.')",
                    () -> OpenString.padLeft("x", 100, '.'));
            assertThat(ns).isLessThan(3_000);
        }
    }

    // =================== OpenString | 字符串工具 ===================

    @Nested
    class OpenStringBenchmarks {

        @Test
        void format_slashStyle() {
            System.out.println("\n=== OpenString.format ===");
            double ns = benchmark("format(\"{} has {} items\", name, count)",
                    () -> OpenString.format("{} has {} items", "Alice", 42));
            assertThat(ns).isLessThan(5_000);
        }

        @Test
        void replaceEach_twoPatterns() {
            System.out.println("\n=== OpenString.replaceEach ===");
            String text = "aabbccaabbcc";
            String[] search  = {"aa", "bb"};
            String[] replace = {"11", "22"};
            double ns = benchmark("replaceEach(text, 2 patterns)",
                    () -> OpenString.replaceEach(text, search, replace));
            assertThat(ns).isLessThan(10_000);
        }

        @Test
        void containsAny_multiSearch() {
            System.out.println("\n=== OpenString.containsAny ===");
            double ns = benchmark("containsAny(str, 5 needles)",
                    () -> OpenString.containsAny("Hello World!", "x", "y", "z", "W", "Q"));
            assertThat(ns).isLessThan(3_000);
        }

        @Test
        void split_andJoin() {
            System.out.println("\n=== OpenString split + join ===");
            double splitNs = benchmark("split(\"a,b,c,d,e\", \",\")",
                    () -> OpenString.split("a,b,c,d,e", ","));
            double joinNs = benchmark("join(\",\", \"a\",\"b\",\"c\",\"d\",\"e\")",
                    () -> OpenString.join(",", "a", "b", "c", "d", "e"));
            assertThat(splitNs).isLessThan(5_000);
            assertThat(joinNs).isLessThan(5_000);
        }

        @Test
        void abbreviate_longString() {
            System.out.println("\n=== OpenString.abbreviate ===");
            String longStr = "This is a very long string that needs to be abbreviated properly";
            double ns = benchmark("abbreviate(64-char, maxWidth=20)",
                    () -> OpenString.abbreviate(longStr, 20));
            assertThat(ns).isLessThan(3_000);
        }
    }

    // =================== OpenGrapheme | 字素簇 ===================

    @Nested
    class OpenGraphemeBenchmarks {

        private static final String ASCII_STR   = "Hello, World! This is a test.";
        private static final String CJK_STR     = "你好世界，这是一段测试文字，包含中文字符。";
        private static final String EMOJI_STR   = "a\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66b\uD83C\uDF89c";
        private static final String MIXED_STR   = "Hi你好\uD83D\uDE00World";

        @Test
        void length_ascii() {
            System.out.println("\n=== OpenGrapheme.length ===");
            double ns = benchmark("length(ASCII 29 chars)",
                    () -> OpenGrapheme.length(ASCII_STR));
            assertThat(ns).isLessThan(5_000);
        }

        @Test
        void length_cjk() {
            double ns = benchmark("length(CJK 22 chars)",
                    () -> OpenGrapheme.length(CJK_STR));
            assertThat(ns).isLessThan(8_000);
        }

        @Test
        void length_emoji() {
            double ns = benchmark("length(emoji family sequence)",
                    () -> OpenGrapheme.length(EMOJI_STR));
            assertThat(ns).isLessThan(8_000);
        }

        @Test
        void displayWidth_mixed() {
            System.out.println("\n=== OpenGrapheme.displayWidth ===");
            double ns = benchmark("displayWidth(mixed CJK+ASCII+emoji)",
                    () -> OpenGrapheme.displayWidth(MIXED_STR));
            assertThat(ns).isLessThan(8_000);
        }

        @Test
        void truncateToWidth_cjk() {
            System.out.println("\n=== OpenGrapheme.truncateToWidth ===");
            double ns = benchmark("truncateToWidth(CJK 22 chars, maxWidth=10)",
                    () -> OpenGrapheme.truncateToWidth(CJK_STR, 10));
            assertThat(ns).isLessThan(10_000);
        }

        @Test
        void reverse_emoji() {
            System.out.println("\n=== OpenGrapheme.reverse ===");
            double ns = benchmark("reverse(emoji sequence)",
                    () -> OpenGrapheme.reverse(EMOJI_STR));
            assertThat(ns).isLessThan(10_000);
        }
    }

    // =================== OpenSlug | URL别名 ===================

    @Nested
    class OpenSlugBenchmarks {

        @Test
        void toSlug_typical() {
            System.out.println("\n=== OpenSlug.toSlug ===");
            double ns = benchmark("toSlug(\"Hello World! This is a Test.\")",
                    () -> OpenSlug.toSlug("Hello World! This is a Test."));
            assertThat(ns).isLessThan(20_000);
        }

        @Test
        void toSlug_accented() {
            double ns = benchmark("toSlug(accented French text)",
                    () -> OpenSlug.toSlug("Crème brûlée au café résumé naïve"));
            assertThat(ns).isLessThan(30_000);
        }

        @Test
        void toSlug_withMaxLength() {
            double ns = benchmark("toSlug(text, \"-\", maxLength=20)",
                    () -> OpenSlug.toSlug("a very long blog post title here", "-", 20));
            assertThat(ns).isLessThan(30_000);
        }
    }

    // =================== AhoCorasick | 多模式匹配 ===================

    @Nested
    class AhoCorasickBenchmarks {

        private static final AhoCorasick MATCHER_SMALL = AhoCorasick.of("bad", "word", "spam");
        private static final AhoCorasick MATCHER_LARGE = AhoCorasick.of(
                "error", "warn", "fatal", "exception", "timeout", "refused",
                "denied", "fail", "crash", "panic", "abort", "illegal",
                "invalid", "null", "missing", "unknown", "broken", "lost"
        );
        private static final String SHORT_TEXT = "This is a bad word in a sentence.";
        private static final String LONG_TEXT  = "2024-01-15 10:23:45 ERROR Connection refused to host. "
                + "Fatal exception occurred. Null pointer exception detected. "
                + "Operation timeout after 30s. Access denied. Invalid credentials.";

        @Test
        void findAll_smallDictionary_shortText() {
            System.out.println("\n=== AhoCorasick.findAll (3 patterns) ===");
            double ns = benchmark("findAll(33-char text, 3 patterns)",
                    () -> MATCHER_SMALL.findAll(SHORT_TEXT));
            assertThat(ns).isLessThan(10_000);
        }

        @Test
        void findAll_largeDictionary_longText() {
            System.out.println("\n=== AhoCorasick.findAll (18 patterns) ===");
            double ns = benchmark("findAll(175-char log line, 18 patterns)",
                    () -> MATCHER_LARGE.findAll(LONG_TEXT));
            assertThat(ns).isLessThan(30_000);
        }

        @Test
        void containsAny_earlyExit() {
            System.out.println("\n=== AhoCorasick.containsAny ===");
            double ns = benchmark("containsAny(text with early match)",
                    () -> MATCHER_SMALL.containsAny(SHORT_TEXT));
            assertThat(ns).isLessThan(5_000);
        }

        @Test
        void filter_replaceMatches() {
            System.out.println("\n=== AhoCorasick.filter ===");
            double ns = benchmark("filter(log line, 18 patterns)",
                    () -> MATCHER_LARGE.filter(LONG_TEXT));
            assertThat(ns).isLessThan(30_000);
        }
    }

    // =================== FuzzyMatcher | 模糊匹配 ===================

    @Nested
    class FuzzyMatcherBenchmarks {

        /** Small dictionary: 10 items (e.g. autocomplete for a dropdown) */
        private static final FuzzyMatcher<String> SMALL_DICT = FuzzyMatcher.builder()
                .addAll(List.of("apple", "application", "apply", "banana", "cherry",
                        "grape", "mango", "melon", "orange", "peach"))
                .threshold(0.5)
                .build();

        /** Large dictionary: 100 items (e.g. city autocomplete) */
        private static final FuzzyMatcher<String> LARGE_DICT;
        static {
            List<String> cities = new java.util.ArrayList<>();
            // 100 representative city names
            String[] base = {
                "Beijing", "Shanghai", "Guangzhou", "Shenzhen", "Chengdu",
                "Hangzhou", "Wuhan", "Nanjing", "Xian", "Chongqing",
                "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
                "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose",
                "Tokyo", "Osaka", "Kyoto", "Nagoya", "Sapporo",
                "London", "Manchester", "Birmingham", "Liverpool", "Leeds",
                "Paris", "Lyon", "Marseille", "Toulouse", "Bordeaux"
            };
            for (int i = 0; i < 3; i++) {
                for (String c : base) cities.add(c + (i == 0 ? "" : "_" + i));
            }
            LARGE_DICT = FuzzyMatcher.builder()
                    .addAll(cities)
                    .threshold(0.5)
                    .maxResults(5)
                    .build();
        }

        @Test
        void match_smallDict_typicalQuery() {
            System.out.println("\n=== FuzzyMatcher.match (10 items) ===");
            double ns = benchmark("match(\"aple\", 10 items, threshold=0.5)",
                    () -> SMALL_DICT.match("aple"));
            assertThat(ns).isLessThan(50_000);
        }

        @Test
        void match_largeDict_typicalQuery() {
            System.out.println("\n=== FuzzyMatcher.match (105 items) ===");
            double ns = benchmark("match(\"Bejing\", 105 items, threshold=0.5)",
                    () -> LARGE_DICT.match("Bejing"));
            assertThat(ns).isLessThan(500_000);
        }

        @Test
        void match_largeDict_noMatch() {
            double ns = benchmark("match(\"zzzzz\", 105 items — no match, early threshold rejection)",
                    () -> LARGE_DICT.match("zzzzz"));
            assertThat(ns).isLessThan(500_000);
        }

        @Test
        void matchBest_smallDict() {
            System.out.println("\n=== FuzzyMatcher.matchBest ===");
            double ns = benchmark("matchBest(\"aple\", 10 items)",
                    () -> SMALL_DICT.matchBest("aple"));
            assertThat(ns).isLessThan(50_000);
        }
    }

    // =================== Similarity | 相似度算法 ===================

    @Nested
    class SimilarityBenchmarks {

        @Test
        void levenshtein_similar() {
            System.out.println("\n=== LevenshteinDistance ===");
            double ns = benchmark("calculate(\"kitten\", \"sitting\")",
                    () -> LevenshteinDistance.calculate("kitten", "sitting"));
            assertThat(ns).isLessThan(3_000);
        }

        @Test
        void levenshtein_longer() {
            double ns = benchmark("calculate(50-char strings, 10 edits)",
                    () -> LevenshteinDistance.calculate(
                            "the quick brown fox jumps over the lazy dog",
                            "the quick brown cat jumps over the lazy log"));
            assertThat(ns).isLessThan(20_000);
        }

        @Test
        void levenshtein_bounded() {
            double ns = benchmark("boundedDistance(\"kitten\",\"sitting\", threshold=5)",
                    () -> LevenshteinDistance.boundedDistance("kitten", "sitting", 5));
            assertThat(ns).isLessThan(3_000);
        }

        @Test
        void levenshtein_bounded_earlyExit() {
            double ns = benchmark("boundedDistance(distant strings, threshold=1) — early exit",
                    () -> LevenshteinDistance.boundedDistance("abcdefghij", "zyxwvutsrq", 1));
            // Should be significantly faster than full calculate() due to early exit
            assertThat(ns).isLessThan(2_000);
        }

        @Test
        void cosine_typical() {
            System.out.println("\n=== CosineSimilarity ===");
            double ns = benchmark("calculate(\"hello world\", \"hello java\")",
                    () -> CosineSimilarity.calculate("hello world", "hello java"));
            assertThat(ns).isLessThan(10_000);
        }

        @Test
        void cosine_longer() {
            double ns = benchmark("calculate(10-word sentences)",
                    () -> CosineSimilarity.calculate(
                            "the quick brown fox jumps over the lazy dog",
                            "a slow white rabbit hops over the green fence"));
            assertThat(ns).isLessThan(20_000);
        }

        @Test
        void jaccard_typical() {
            System.out.println("\n=== JaccardSimilarity (no intersection/union alloc) ===");
            double ns = benchmark("calculate(\"kitten\", \"sitting\", bigram)",
                    () -> JaccardSimilarity.calculate("kitten", "sitting"));
            assertThat(ns).isLessThan(10_000);
        }
    }

    // =================== R4 Additions | 第四轮新增 ===================

    @Nested
    class R4Benchmarks {

        @Test
        void containsIgnoreCase_regionMatches() {
            System.out.println("\n=== containsIgnoreCase (regionMatches) ===");
            double ns = benchmark("containsIgnoreCase(\"Hello World Foo Bar\", \"foo\")",
                    () -> OpenString.containsIgnoreCase("Hello World Foo Bar", "foo"));
            assertThat(ns).isLessThan(3_000);
        }

        @Test
        void containsIgnoreCase_noMatch() {
            double ns = benchmark("containsIgnoreCase(20-char, no match)",
                    () -> OpenString.containsIgnoreCase("Hello World Foo Bar!", "xyz"));
            assertThat(ns).isLessThan(3_000);
        }

        @Test
        void truncateByBytes_longCjk() {
            System.out.println("\n=== OpenTruncate.truncateByBytes (binary search) ===");
            String cjk = "你好世界这是一段较长的中文文本用于测试字节截断性能";
            double ns = benchmark("truncateByBytes(24-CJK, 20 bytes, UTF-8)",
                    () -> OpenTruncate.truncateByBytes(cjk, 20, "UTF-8"));
            assertThat(ns).isLessThan(10_000);
        }

        @Test
        void format_batchAppend() {
            System.out.println("\n=== OpenString.format (batch append) ===");
            double ns = benchmark("format(long pattern, 5 args)",
                    () -> OpenString.format("User {} logged in from {} at {} with role {} (session {})",
                            "Alice", "192.168.1.1", "10:23:45", "admin", "abc123"));
            assertThat(ns).isLessThan(5_000);
        }
    }

    // =================== R5: HtmlUtil / CharMatcher ===================

    @Nested
    class R5Benchmarks {

        private static final String HTML_INPUT =
                "<script>alert('xss')</script> & \"hello\" <b>world</b>";
        private static final String HTML_ESCAPED =
                "&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt; &amp; &quot;hello&quot; &lt;b&gt;world&lt;/b&gt;";

        @Test
        void htmlEscape_charSwitch() {
            System.out.println("\n=== HtmlUtil.escape (char switch, no Map) ===");
            double ns = benchmark("escape(52-char HTML input)",
                    () -> HtmlUtil.escape(HTML_INPUT));
            assertThat(ns).isLessThan(3_000);
        }

        @Test
        void htmlUnescape_singlePass() {
            System.out.println("\n=== HtmlUtil.unescape (single-pass) ===");
            double ns = benchmark("unescape(escaped HTML, 9 entities)",
                    () -> HtmlUtil.unescape(HTML_ESCAPED));
            assertThat(ns).isLessThan(3_000);
        }

        @Test
        void htmlEscape_noSpecialChars() {
            double ns = benchmark("escape(plain text, no escaping needed)",
                    () -> HtmlUtil.escape("Hello World! This is plain text."));
            assertThat(ns).isLessThan(3_000);
        }

        @Test
        void htmlUnescape_noEntities() {
            double ns = benchmark("unescape(plain text, fast path — no '&')",
                    () -> HtmlUtil.unescape("Hello World! This is plain text."));
            assertThat(ns).isLessThan(500);
        }
    }
}
