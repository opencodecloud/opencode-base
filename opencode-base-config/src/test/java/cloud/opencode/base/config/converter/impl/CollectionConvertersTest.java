package cloud.opencode.base.config.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for CollectionConverters.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("CollectionConverters Tests")
class CollectionConvertersTest {

    @Nested
    @DisplayName("StringListConverter Tests")
    class StringListConverterTests {

        private final CollectionConverters.StringListConverter converter = new CollectionConverters.StringListConverter();

        @Test
        @DisplayName("converts comma-separated values to list")
        void testBasicConversion() {
            List<String> result = converter.convert("a,b,c");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("trims whitespace from elements")
        void testTrimsWhitespace() {
            List<String> result = converter.convert(" a , b , c ");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("converts single value to single-element list")
        void testSingleValue() {
            List<String> result = converter.convert("single");
            assertThat(result).containsExactly("single");
        }

        @Test
        @DisplayName("handles empty strings between commas")
        void testEmptyBetweenCommas() {
            List<String> result = converter.convert("a,,c");
            assertThat(result).hasSize(3);
            assertThat(result.get(1)).isEmpty();
        }
    }

    @Nested
    @DisplayName("StringSetConverter Tests")
    class StringSetConverterTests {

        private final CollectionConverters.StringSetConverter converter = new CollectionConverters.StringSetConverter();

        @Test
        @DisplayName("converts comma-separated values to set")
        void testBasicConversion() {
            Set<String> result = converter.convert("a,b,c");
            assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("deduplicates values")
        void testDeduplication() {
            Set<String> result = converter.convert("a,b,a,c,b");
            assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("trims whitespace from elements")
        void testTrimsWhitespace() {
            Set<String> result = converter.convert(" x , y , z ");
            assertThat(result).containsExactlyInAnyOrder("x", "y", "z");
        }

        @Test
        @DisplayName("converts single value to single-element set")
        void testSingleValue() {
            Set<String> result = converter.convert("single");
            assertThat(result).containsExactly("single");
        }
    }
}
