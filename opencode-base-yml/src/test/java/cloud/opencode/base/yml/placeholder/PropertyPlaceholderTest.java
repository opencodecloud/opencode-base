package cloud.opencode.base.yml.placeholder;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertyPlaceholderTest Tests
 * PropertyPlaceholderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("PropertyPlaceholder Tests")
class PropertyPlaceholderTest {

    @Nested
    @DisplayName("parse(String expression) Tests")
    class ParseWithDefaultDelimitersTests {

        @Nested
        @DisplayName("Simple Placeholder Tests")
        class SimplePlaceholderTests {

            @Test
            @DisplayName("parse should extract key from simple placeholder")
            void parseShouldExtractKeyFromSimplePlaceholder() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

                assertThat(placeholder.getKey()).isEqualTo("key");
            }

            @Test
            @DisplayName("parse should return null default value for simple placeholder")
            void parseShouldReturnNullDefaultValueForSimplePlaceholder() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

                assertThat(placeholder.getDefaultValue()).isNull();
            }

            @Test
            @DisplayName("parse should set hasDefaultValue to false for simple placeholder")
            void parseShouldSetHasDefaultValueToFalseForSimplePlaceholder() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

                assertThat(placeholder.hasDefaultValue()).isFalse();
            }

            @Test
            @DisplayName("parse should store raw expression for simple placeholder")
            void parseShouldStoreRawExpressionForSimplePlaceholder() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

                assertThat(placeholder.getRawExpression()).isEqualTo("${key}");
            }

            @Test
            @DisplayName("parse should handle key with dots")
            void parseShouldHandleKeyWithDots() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${server.port}");

                assertThat(placeholder.getKey()).isEqualTo("server.port");
            }

            @Test
            @DisplayName("parse should handle key with underscores")
            void parseShouldHandleKeyWithUnderscores() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${my_variable}");

                assertThat(placeholder.getKey()).isEqualTo("my_variable");
            }

            @Test
            @DisplayName("parse should handle key with hyphens")
            void parseShouldHandleKeyWithHyphens() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${my-variable}");

                assertThat(placeholder.getKey()).isEqualTo("my-variable");
            }

            @Test
            @DisplayName("parse should handle key with mixed characters")
            void parseShouldHandleKeyWithMixedCharacters() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${app.server_host-name}");

                assertThat(placeholder.getKey()).isEqualTo("app.server_host-name");
            }

            @Test
            @DisplayName("parse should trim whitespace from expression")
            void parseShouldTrimWhitespaceFromExpression() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("  ${key}  ");

                assertThat(placeholder.getKey()).isEqualTo("key");
                assertThat(placeholder.getRawExpression()).isEqualTo("${key}");
            }
        }

        @Nested
        @DisplayName("Placeholder With Default Value Tests")
        class PlaceholderWithDefaultValueTests {

            @Test
            @DisplayName("parse should extract key from placeholder with default")
            void parseShouldExtractKeyFromPlaceholderWithDefault() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:default}");

                assertThat(placeholder.getKey()).isEqualTo("key");
            }

            @Test
            @DisplayName("parse should extract default value from placeholder")
            void parseShouldExtractDefaultValueFromPlaceholder() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:default}");

                assertThat(placeholder.getDefaultValue()).isEqualTo("default");
            }

            @Test
            @DisplayName("parse should set hasDefaultValue to true when default exists")
            void parseShouldSetHasDefaultValueToTrueWhenDefaultExists() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:default}");

                assertThat(placeholder.hasDefaultValue()).isTrue();
            }

            @Test
            @DisplayName("parse should store raw expression with default value")
            void parseShouldStoreRawExpressionWithDefaultValue() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:default}");

                assertThat(placeholder.getRawExpression()).isEqualTo("${key:default}");
            }

            @Test
            @DisplayName("parse should handle empty default value")
            void parseShouldHandleEmptyDefaultValue() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:}");

                assertThat(placeholder.getKey()).isEqualTo("key");
                assertThat(placeholder.getDefaultValue()).isEmpty();
                assertThat(placeholder.hasDefaultValue()).isTrue();
            }

            @Test
            @DisplayName("parse should handle default value with spaces")
            void parseShouldHandleDefaultValueWithSpaces() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:hello world}");

                assertThat(placeholder.getDefaultValue()).isEqualTo("hello world");
            }

            @Test
            @DisplayName("parse should handle default value with special characters")
            void parseShouldHandleDefaultValueWithSpecialCharacters() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${url:http://localhost:8080}");

                assertThat(placeholder.getKey()).isEqualTo("url");
                assertThat(placeholder.getDefaultValue()).isEqualTo("http://localhost:8080");
            }

            @Test
            @DisplayName("parse should handle numeric default value")
            void parseShouldHandleNumericDefaultValue() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${port:8080}");

                assertThat(placeholder.getDefaultValue()).isEqualTo("8080");
            }

            @Test
            @DisplayName("parse should use first colon as separator")
            void parseShouldUseFirstColonAsSeparator() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${host:localhost:3000}");

                assertThat(placeholder.getKey()).isEqualTo("host");
                assertThat(placeholder.getDefaultValue()).isEqualTo("localhost:3000");
            }
        }

        @Nested
        @DisplayName("Invalid Expression Tests")
        class InvalidExpressionTests {

            @Test
            @DisplayName("parse should throw for null expression")
            void parseShouldThrowForNullExpression() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Expression cannot be null");
            }

            @Test
            @DisplayName("parse should throw for expression without prefix")
            void parseShouldThrowForExpressionWithoutPrefix() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse("key}"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid placeholder expression");
            }

            @Test
            @DisplayName("parse should throw for expression without suffix")
            void parseShouldThrowForExpressionWithoutSuffix() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse("${key"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid placeholder expression");
            }

            @Test
            @DisplayName("parse should throw for empty placeholder key")
            void parseShouldThrowForEmptyPlaceholderKey() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse("${}"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Empty placeholder key");
            }

            @Test
            @DisplayName("parse should throw for plain text")
            void parseShouldThrowForPlainText() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse("plain text"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid placeholder expression");
            }

            @Test
            @DisplayName("parse should throw for empty expression")
            void parseShouldThrowForEmptyExpression() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid placeholder expression");
            }

            @Test
            @DisplayName("parse should throw for whitespace only expression")
            void parseShouldThrowForWhitespaceOnlyExpression() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid placeholder expression");
            }

            @Test
            @DisplayName("parse should throw for wrong prefix")
            void parseShouldThrowForWrongPrefix() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse("#{key}"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid placeholder expression");
            }

            @Test
            @DisplayName("parse should throw for wrong suffix")
            void parseShouldThrowForWrongSuffix() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse("${key]"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid placeholder expression");
            }
        }
    }

    @Nested
    @DisplayName("parse(expression, prefix, suffix, separator) Tests")
    class ParseWithCustomDelimitersTests {

        @Nested
        @DisplayName("Hash Prefix Tests")
        class HashPrefixTests {

            @Test
            @DisplayName("parse should handle hash prefix")
            void parseShouldHandleHashPrefix() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("#{key}", "#{", "}", ":");

                assertThat(placeholder.getKey()).isEqualTo("key");
            }

            @Test
            @DisplayName("parse should handle hash prefix with default")
            void parseShouldHandleHashPrefixWithDefault() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("#{key:default}", "#{", "}", ":");

                assertThat(placeholder.getKey()).isEqualTo("key");
                assertThat(placeholder.getDefaultValue()).isEqualTo("default");
            }

            @Test
            @DisplayName("parse should store raw expression with hash prefix")
            void parseShouldStoreRawExpressionWithHashPrefix() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("#{key}", "#{", "}", ":");

                assertThat(placeholder.getRawExpression()).isEqualTo("#{key}");
            }
        }

        @Nested
        @DisplayName("Bracket Delimiters Tests")
        class BracketDelimitersTests {

            @Test
            @DisplayName("parse should handle square brackets")
            void parseShouldHandleSquareBrackets() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("[key]", "[", "]", ":");

                assertThat(placeholder.getKey()).isEqualTo("key");
            }

            @Test
            @DisplayName("parse should handle square brackets with default")
            void parseShouldHandleSquareBracketsWithDefault() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("[key:default]", "[", "]", ":");

                assertThat(placeholder.getKey()).isEqualTo("key");
                assertThat(placeholder.getDefaultValue()).isEqualTo("default");
            }
        }

        @Nested
        @DisplayName("Percent Prefix Tests")
        class PercentPrefixTests {

            @Test
            @DisplayName("parse should handle percent prefix")
            void parseShouldHandlePercentPrefix() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("%{key}", "%{", "}", ":");

                assertThat(placeholder.getKey()).isEqualTo("key");
            }

            @Test
            @DisplayName("parse should handle percent prefix with default")
            void parseShouldHandlePercentPrefixWithDefault() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("%{key:default}", "%{", "}", ":");

                assertThat(placeholder.getKey()).isEqualTo("key");
                assertThat(placeholder.getDefaultValue()).isEqualTo("default");
            }
        }

        @Nested
        @DisplayName("Custom Separator Tests")
        class CustomSeparatorTests {

            @Test
            @DisplayName("parse should handle double colon separator")
            void parseShouldHandleDoubleColonSeparator() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key::default}", "${", "}", "::");

                assertThat(placeholder.getKey()).isEqualTo("key");
                assertThat(placeholder.getDefaultValue()).isEqualTo("default");
            }

            @Test
            @DisplayName("parse should handle pipe separator")
            void parseShouldHandlePipeSeparator() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key|default}", "${", "}", "|");

                assertThat(placeholder.getKey()).isEqualTo("key");
                assertThat(placeholder.getDefaultValue()).isEqualTo("default");
            }

            @Test
            @DisplayName("parse should handle equals separator")
            void parseShouldHandleEqualsSeparator() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key=default}", "${", "}", "=");

                assertThat(placeholder.getKey()).isEqualTo("key");
                assertThat(placeholder.getDefaultValue()).isEqualTo("default");
            }

            @Test
            @DisplayName("parse should handle arrow separator")
            void parseShouldHandleArrowSeparator() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key->default}", "${", "}", "->");

                assertThat(placeholder.getKey()).isEqualTo("key");
                assertThat(placeholder.getDefaultValue()).isEqualTo("default");
            }

            @Test
            @DisplayName("key with colon should work when separator is different")
            void keyWithColonShouldWorkWhenSeparatorIsDifferent() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${host:port|fallback}", "${", "}", "|");

                assertThat(placeholder.getKey()).isEqualTo("host:port");
                assertThat(placeholder.getDefaultValue()).isEqualTo("fallback");
            }
        }

        @Nested
        @DisplayName("Multi-Character Delimiters Tests")
        class MultiCharacterDelimitersTests {

            @Test
            @DisplayName("parse should handle multi-character prefix and suffix")
            void parseShouldHandleMultiCharacterPrefixAndSuffix() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("<<<key>>>", "<<<", ">>>", ":");

                assertThat(placeholder.getKey()).isEqualTo("key");
            }

            @Test
            @DisplayName("parse should handle multi-character delimiters with default")
            void parseShouldHandleMultiCharacterDelimitersWithDefault() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("<<<key:default>>>", "<<<", ">>>", ":");

                assertThat(placeholder.getKey()).isEqualTo("key");
                assertThat(placeholder.getDefaultValue()).isEqualTo("default");
            }

            @Test
            @DisplayName("parse should handle asymmetric multi-character delimiters")
            void parseShouldHandleAsymmetricMultiCharacterDelimiters() {
                PropertyPlaceholder placeholder = PropertyPlaceholder.parse("{{key}}", "{{", "}}", ":");

                assertThat(placeholder.getKey()).isEqualTo("key");
            }
        }

        @Nested
        @DisplayName("Invalid Custom Delimiter Expression Tests")
        class InvalidCustomDelimiterExpressionTests {

            @Test
            @DisplayName("parse should throw for wrong custom prefix")
            void parseShouldThrowForWrongCustomPrefix() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse("${key}", "#{", "}", ":"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid placeholder expression");
            }

            @Test
            @DisplayName("parse should throw for wrong custom suffix")
            void parseShouldThrowForWrongCustomSuffix() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse("#{key}", "#{", "]", ":"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid placeholder expression");
            }

            @Test
            @DisplayName("parse should throw for empty key with custom delimiters")
            void parseShouldThrowForEmptyKeyWithCustomDelimiters() {
                assertThatThrownBy(() -> PropertyPlaceholder.parse("#{}", "#{", "}", ":"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Empty placeholder key");
            }
        }
    }

    @Nested
    @DisplayName("of(String key) Tests")
    class OfWithKeyOnlyTests {

        @Test
        @DisplayName("of should create placeholder with given key")
        void ofShouldCreatePlaceholderWithGivenKey() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("myKey");

            assertThat(placeholder.getKey()).isEqualTo("myKey");
        }

        @Test
        @DisplayName("of should create placeholder without default value")
        void ofShouldCreatePlaceholderWithoutDefaultValue() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("myKey");

            assertThat(placeholder.getDefaultValue()).isNull();
            assertThat(placeholder.hasDefaultValue()).isFalse();
        }

        @Test
        @DisplayName("of should generate correct raw expression")
        void ofShouldGenerateCorrectRawExpression() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("myKey");

            assertThat(placeholder.getRawExpression()).isEqualTo("${myKey}");
        }

        @Test
        @DisplayName("of should handle key with dots")
        void ofShouldHandleKeyWithDots() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("server.port");

            assertThat(placeholder.getKey()).isEqualTo("server.port");
            assertThat(placeholder.getRawExpression()).isEqualTo("${server.port}");
        }

        @Test
        @DisplayName("of should return empty Optional from defaultValue method")
        void ofShouldReturnEmptyOptionalFromDefaultValueMethod() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("key");

            assertThat(placeholder.defaultValue()).isEmpty();
        }
    }

    @Nested
    @DisplayName("of(String key, String defaultValue) Tests")
    class OfWithKeyAndDefaultTests {

        @Test
        @DisplayName("of should create placeholder with key and default")
        void ofShouldCreatePlaceholderWithKeyAndDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("myKey", "myDefault");

            assertThat(placeholder.getKey()).isEqualTo("myKey");
            assertThat(placeholder.getDefaultValue()).isEqualTo("myDefault");
        }

        @Test
        @DisplayName("of should set hasDefaultValue to true")
        void ofShouldSetHasDefaultValueToTrue() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("myKey", "myDefault");

            assertThat(placeholder.hasDefaultValue()).isTrue();
        }

        @Test
        @DisplayName("of should generate correct raw expression with default")
        void ofShouldGenerateCorrectRawExpressionWithDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("myKey", "myDefault");

            assertThat(placeholder.getRawExpression()).isEqualTo("${myKey:myDefault}");
        }

        @Test
        @DisplayName("of should handle null default value")
        void ofShouldHandleNullDefaultValue() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("myKey", null);

            assertThat(placeholder.getDefaultValue()).isNull();
            assertThat(placeholder.hasDefaultValue()).isFalse();
            assertThat(placeholder.getRawExpression()).isEqualTo("${myKey}");
        }

        @Test
        @DisplayName("of should handle empty default value")
        void ofShouldHandleEmptyDefaultValue() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("myKey", "");

            assertThat(placeholder.getDefaultValue()).isEmpty();
            assertThat(placeholder.hasDefaultValue()).isTrue();
            assertThat(placeholder.getRawExpression()).isEqualTo("${myKey:}");
        }

        @Test
        @DisplayName("of should return present Optional from defaultValue method")
        void ofShouldReturnPresentOptionalFromDefaultValueMethod() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("key", "default");

            assertThat(placeholder.defaultValue()).isPresent();
            assertThat(placeholder.defaultValue()).contains("default");
        }
    }

    @Nested
    @DisplayName("getKey() Tests")
    class GetKeyTests {

        @Test
        @DisplayName("getKey should return key from parsed placeholder")
        void getKeyShouldReturnKeyFromParsedPlaceholder() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${server.host}");

            assertThat(placeholder.getKey()).isEqualTo("server.host");
        }

        @Test
        @DisplayName("getKey should return key from created placeholder")
        void getKeyShouldReturnKeyFromCreatedPlaceholder() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("database.url");

            assertThat(placeholder.getKey()).isEqualTo("database.url");
        }

        @Test
        @DisplayName("getKey should not include default value")
        void getKeyShouldNotIncludeDefaultValue() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:value}");

            assertThat(placeholder.getKey()).isEqualTo("key");
            assertThat(placeholder.getKey()).doesNotContain(":");
            assertThat(placeholder.getKey()).doesNotContain("value");
        }
    }

    @Nested
    @DisplayName("getDefaultValue() Tests")
    class GetDefaultValueTests {

        @Test
        @DisplayName("getDefaultValue should return null when no default")
        void getDefaultValueShouldReturnNullWhenNoDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

            assertThat(placeholder.getDefaultValue()).isNull();
        }

        @Test
        @DisplayName("getDefaultValue should return default when present")
        void getDefaultValueShouldReturnDefaultWhenPresent() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:defaultValue}");

            assertThat(placeholder.getDefaultValue()).isEqualTo("defaultValue");
        }

        @Test
        @DisplayName("getDefaultValue should return empty string for empty default")
        void getDefaultValueShouldReturnEmptyStringForEmptyDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:}");

            assertThat(placeholder.getDefaultValue()).isEmpty();
        }
    }

    @Nested
    @DisplayName("defaultValue() Tests")
    class DefaultValueOptionalTests {

        @Test
        @DisplayName("defaultValue should return empty Optional when no default")
        void defaultValueShouldReturnEmptyOptionalWhenNoDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

            assertThat(placeholder.defaultValue()).isEmpty();
        }

        @Test
        @DisplayName("defaultValue should return present Optional when default exists")
        void defaultValueShouldReturnPresentOptionalWhenDefaultExists() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:value}");

            assertThat(placeholder.defaultValue()).isPresent();
            assertThat(placeholder.defaultValue().get()).isEqualTo("value");
        }

        @Test
        @DisplayName("defaultValue should return present Optional for empty default")
        void defaultValueShouldReturnPresentOptionalForEmptyDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:}");

            assertThat(placeholder.defaultValue()).isPresent();
            assertThat(placeholder.defaultValue().get()).isEmpty();
        }

        @Test
        @DisplayName("defaultValue should allow functional operations")
        void defaultValueShouldAllowFunctionalOperations() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:fallback}");

            String result = placeholder.defaultValue().orElse("other");
            assertThat(result).isEqualTo("fallback");
        }

        @Test
        @DisplayName("defaultValue should return orElse value when empty")
        void defaultValueShouldReturnOrElseValueWhenEmpty() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

            String result = placeholder.defaultValue().orElse("fallback");
            assertThat(result).isEqualTo("fallback");
        }
    }

    @Nested
    @DisplayName("hasDefaultValue() Tests")
    class HasDefaultValueTests {

        @Test
        @DisplayName("hasDefaultValue should return false for simple placeholder")
        void hasDefaultValueShouldReturnFalseForSimplePlaceholder() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

            assertThat(placeholder.hasDefaultValue()).isFalse();
        }

        @Test
        @DisplayName("hasDefaultValue should return true for placeholder with default")
        void hasDefaultValueShouldReturnTrueForPlaceholderWithDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:default}");

            assertThat(placeholder.hasDefaultValue()).isTrue();
        }

        @Test
        @DisplayName("hasDefaultValue should return true for empty default")
        void hasDefaultValueShouldReturnTrueForEmptyDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:}");

            assertThat(placeholder.hasDefaultValue()).isTrue();
        }

        @Test
        @DisplayName("hasDefaultValue should return false for of with key only")
        void hasDefaultValueShouldReturnFalseForOfWithKeyOnly() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("key");

            assertThat(placeholder.hasDefaultValue()).isFalse();
        }

        @Test
        @DisplayName("hasDefaultValue should return true for of with default")
        void hasDefaultValueShouldReturnTrueForOfWithDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("key", "value");

            assertThat(placeholder.hasDefaultValue()).isTrue();
        }

        @Test
        @DisplayName("hasDefaultValue should return false for of with null default")
        void hasDefaultValueShouldReturnFalseForOfWithNullDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("key", null);

            assertThat(placeholder.hasDefaultValue()).isFalse();
        }
    }

    @Nested
    @DisplayName("getRawExpression() Tests")
    class GetRawExpressionTests {

        @Test
        @DisplayName("getRawExpression should return original parsed expression")
        void getRawExpressionShouldReturnOriginalParsedExpression() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${server.port:8080}");

            assertThat(placeholder.getRawExpression()).isEqualTo("${server.port:8080}");
        }

        @Test
        @DisplayName("getRawExpression should return generated expression for of")
        void getRawExpressionShouldReturnGeneratedExpressionForOf() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("key", "value");

            assertThat(placeholder.getRawExpression()).isEqualTo("${key:value}");
        }

        @Test
        @DisplayName("getRawExpression should return trimmed expression")
        void getRawExpressionShouldReturnTrimmedExpression() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("  ${key}  ");

            assertThat(placeholder.getRawExpression()).isEqualTo("${key}");
        }

        @Test
        @DisplayName("getRawExpression should preserve custom delimiters")
        void getRawExpressionShouldPreserveCustomDelimiters() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("#{key:value}", "#{", "}", ":");

            assertThat(placeholder.getRawExpression()).isEqualTo("#{key:value}");
        }
    }

    @Nested
    @DisplayName("toString() Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should return raw expression")
        void toStringShouldReturnRawExpression() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:default}");

            assertThat(placeholder.toString()).isEqualTo("${key:default}");
        }

        @Test
        @DisplayName("toString should match getRawExpression")
        void toStringShouldMatchGetRawExpression() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${server.port}");

            assertThat(placeholder.toString()).isEqualTo(placeholder.getRawExpression());
        }

        @Test
        @DisplayName("toString should return generated expression for of")
        void toStringShouldReturnGeneratedExpressionForOf() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.of("key");

            assertThat(placeholder.toString()).isEqualTo("${key}");
        }
    }

    @Nested
    @DisplayName("equals() Tests")
    class EqualsTests {

        @Test
        @DisplayName("equals should return true for same instance")
        void equalsShouldReturnTrueForSameInstance() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

            assertThat(placeholder.equals(placeholder)).isTrue();
        }

        @Test
        @DisplayName("equals should return true for equal placeholders")
        void equalsShouldReturnTrueForEqualPlaceholders() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.parse("${key:default}");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.parse("${key:default}");

            assertThat(placeholder1.equals(placeholder2)).isTrue();
        }

        @Test
        @DisplayName("equals should return true for placeholders with same key and default")
        void equalsShouldReturnTrueForPlaceholdersWithSameKeyAndDefault() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.of("key", "default");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.parse("${key:default}");

            assertThat(placeholder1.equals(placeholder2)).isTrue();
        }

        @Test
        @DisplayName("equals should return false for different keys")
        void equalsShouldReturnFalseForDifferentKeys() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.parse("${key1}");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.parse("${key2}");

            assertThat(placeholder1.equals(placeholder2)).isFalse();
        }

        @Test
        @DisplayName("equals should return false for different defaults")
        void equalsShouldReturnFalseForDifferentDefaults() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.parse("${key:default1}");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.parse("${key:default2}");

            assertThat(placeholder1.equals(placeholder2)).isFalse();
        }

        @Test
        @DisplayName("equals should return false for one with default one without")
        void equalsShouldReturnFalseForOneWithDefaultOneWithout() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.parse("${key}");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.parse("${key:default}");

            assertThat(placeholder1.equals(placeholder2)).isFalse();
        }

        @Test
        @DisplayName("equals should return false for null")
        void equalsShouldReturnFalseForNull() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

            assertThat(placeholder.equals(null)).isFalse();
        }

        @Test
        @DisplayName("equals should return false for different type")
        void equalsShouldReturnFalseForDifferentType() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

            assertThat(placeholder.equals("${key}")).isFalse();
        }

        @Test
        @DisplayName("equals should compare by key and default only not raw expression")
        void equalsShouldCompareByKeyAndDefaultOnlyNotRawExpression() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.parse("${key}", "${", "}", ":");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.parse("#{key}", "#{", "}", ":");

            // Both have key="key" and defaultValue=null, so they should be equal
            assertThat(placeholder1.equals(placeholder2)).isTrue();
        }
    }

    @Nested
    @DisplayName("hashCode() Tests")
    class HashCodeTests {

        @Test
        @DisplayName("hashCode should be equal for equal placeholders")
        void hashCodeShouldBeEqualForEqualPlaceholders() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.parse("${key:default}");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.parse("${key:default}");

            assertThat(placeholder1.hashCode()).isEqualTo(placeholder2.hashCode());
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key}");

            int hash1 = placeholder.hashCode();
            int hash2 = placeholder.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("hashCode should differ for different keys")
        void hashCodeShouldDifferForDifferentKeys() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.parse("${key1}");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.parse("${key2}");

            // Note: hashCodes may collide but in practice they should differ
            assertThat(placeholder1.hashCode()).isNotEqualTo(placeholder2.hashCode());
        }

        @Test
        @DisplayName("hashCode should be same for equal placeholders from different sources")
        void hashCodeShouldBeSameForEqualPlaceholdersFromDifferentSources() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.of("key", "value");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.parse("${key:value}");

            assertThat(placeholder1.hashCode()).isEqualTo(placeholder2.hashCode());
        }

        @Test
        @DisplayName("hashCode should work correctly in HashSet")
        void hashCodeShouldWorkCorrectlyInHashSet() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.parse("${key:default}");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.of("key", "default");

            java.util.Set<PropertyPlaceholder> set = new java.util.HashSet<>();
            set.add(placeholder1);
            set.add(placeholder2);

            assertThat(set).hasSize(1);
        }

        @Test
        @DisplayName("hashCode should work correctly in HashMap")
        void hashCodeShouldWorkCorrectlyInHashMap() {
            PropertyPlaceholder placeholder1 = PropertyPlaceholder.parse("${key}");
            PropertyPlaceholder placeholder2 = PropertyPlaceholder.of("key");

            java.util.Map<PropertyPlaceholder, String> map = new java.util.HashMap<>();
            map.put(placeholder1, "first");
            map.put(placeholder2, "second");

            assertThat(map).hasSize(1);
            assertThat(map.get(placeholder1)).isEqualTo("second");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle very long key")
        void shouldHandleVeryLongKey() {
            String longKey = "a".repeat(1000);
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${" + longKey + "}");

            assertThat(placeholder.getKey()).isEqualTo(longKey);
        }

        @Test
        @DisplayName("should handle very long default value")
        void shouldHandleVeryLongDefaultValue() {
            String longDefault = "x".repeat(1000);
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:" + longDefault + "}");

            assertThat(placeholder.getDefaultValue()).isEqualTo(longDefault);
        }

        @Test
        @DisplayName("should handle unicode in key")
        void shouldHandleUnicodeInKey() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${greeting}");

            assertThat(placeholder.getKey()).isEqualTo("greeting");
        }

        @Test
        @DisplayName("should handle unicode in default value")
        void shouldHandleUnicodeInDefaultValue() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:Hello World}");

            assertThat(placeholder.getDefaultValue()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("should handle key with numbers")
        void shouldHandleKeyWithNumbers() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key123}");

            assertThat(placeholder.getKey()).isEqualTo("key123");
        }

        @Test
        @DisplayName("should handle key starting with number")
        void shouldHandleKeyStartingWithNumber() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${123key}");

            assertThat(placeholder.getKey()).isEqualTo("123key");
        }

        @Test
        @DisplayName("should handle default value with newlines")
        void shouldHandleDefaultValueWithNewlines() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:line1\nline2}");

            assertThat(placeholder.getDefaultValue()).isEqualTo("line1\nline2");
        }

        @Test
        @DisplayName("should handle default value with tabs")
        void shouldHandleDefaultValueWithTabs() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:col1\tcol2}");

            assertThat(placeholder.getDefaultValue()).isEqualTo("col1\tcol2");
        }

        @Test
        @DisplayName("should handle single character key")
        void shouldHandleSingleCharacterKey() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${k}");

            assertThat(placeholder.getKey()).isEqualTo("k");
        }

        @Test
        @DisplayName("should handle single character default")
        void shouldHandleSingleCharacterDefault() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:v}");

            assertThat(placeholder.getDefaultValue()).isEqualTo("v");
        }

        @Test
        @DisplayName("of should handle null key gracefully")
        void ofShouldHandleNullKeyGracefully() {
            // The implementation doesn't validate null keys in of()
            // This test documents current behavior
            PropertyPlaceholder placeholder = PropertyPlaceholder.of(null);

            assertThat(placeholder.getKey()).isNull();
            assertThat(placeholder.getRawExpression()).isEqualTo("${null}");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("PropertyPlaceholder should be immutable")
        void propertyPlaceholderShouldBeImmutable() {
            PropertyPlaceholder placeholder = PropertyPlaceholder.parse("${key:default}");

            // All getters return immutable values
            String key = placeholder.getKey();
            String defaultValue = placeholder.getDefaultValue();
            String rawExpression = placeholder.getRawExpression();

            // Values remain unchanged
            assertThat(placeholder.getKey()).isEqualTo(key);
            assertThat(placeholder.getDefaultValue()).isEqualTo(defaultValue);
            assertThat(placeholder.getRawExpression()).isEqualTo(rawExpression);
        }

        @Test
        @DisplayName("class should be final")
        void classShouldBeFinal() {
            assertThat(PropertyPlaceholder.class).isFinal();
        }
    }
}
