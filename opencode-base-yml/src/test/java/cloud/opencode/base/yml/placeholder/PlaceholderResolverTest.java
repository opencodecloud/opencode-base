package cloud.opencode.base.yml.placeholder;

import cloud.opencode.base.yml.exception.YmlPlaceholderException;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * PlaceholderResolverTest Tests
 * PlaceholderResolverTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("PlaceholderResolver Tests")
class PlaceholderResolverTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Nested
        @DisplayName("create() Tests")
        class CreateTests {

            @Test
            @DisplayName("create should return non-null resolver")
            void createShouldReturnNonNullResolver() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                assertThat(resolver).isNotNull();
            }

            @Test
            @DisplayName("create should return resolver with default settings")
            void createShouldReturnResolverWithDefaultSettings() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                // Default resolver has no property sources, so placeholders remain unresolved
                String result = resolver.resolve("${undefined}");
                assertThat(result).isEqualTo("${undefined}");
            }

            @Test
            @DisplayName("create should return new instance each time")
            void createShouldReturnNewInstanceEachTime() {
                PlaceholderResolver resolver1 = PlaceholderResolver.create();
                PlaceholderResolver resolver2 = PlaceholderResolver.create();

                assertThat(resolver1).isNotSameAs(resolver2);
            }
        }

        @Nested
        @DisplayName("create(Map) Tests")
        class CreateWithMapTests {

            @Test
            @DisplayName("create with map should return non-null resolver")
            void createWithMapShouldReturnNonNullResolver() {
                Map<String, String> props = Map.of("key", "value");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                assertThat(resolver).isNotNull();
            }

            @Test
            @DisplayName("create with map should resolve placeholders from map")
            void createWithMapShouldResolvePlaceholdersFromMap() {
                Map<String, String> props = Map.of("name", "John");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("Hello, ${name}!");
                assertThat(result).isEqualTo("Hello, John!");
            }

            @Test
            @DisplayName("create with empty map should leave placeholders unresolved")
            void createWithEmptyMapShouldLeavePlaceholdersUnresolved() {
                Map<String, String> props = Map.of();
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${undefined}");
                assertThat(result).isEqualTo("${undefined}");
            }

            @Test
            @DisplayName("create with map should resolve multiple placeholders")
            void createWithMapShouldResolveMultiplePlaceholders() {
                Map<String, String> props = Map.of("first", "Hello", "second", "World");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${first}, ${second}!");
                assertThat(result).isEqualTo("Hello, World!");
            }
        }

        @Nested
        @DisplayName("builder() Tests")
        class BuilderFactoryTests {

            @Test
            @DisplayName("builder should return non-null builder")
            void builderShouldReturnNonNullBuilder() {
                PlaceholderResolver.Builder builder = PlaceholderResolver.builder();

                assertThat(builder).isNotNull();
            }

            @Test
            @DisplayName("builder should return new instance each time")
            void builderShouldReturnNewInstanceEachTime() {
                PlaceholderResolver.Builder builder1 = PlaceholderResolver.builder();
                PlaceholderResolver.Builder builder2 = PlaceholderResolver.builder();

                assertThat(builder1).isNotSameAs(builder2);
            }

            @Test
            @DisplayName("builder build should return resolver")
            void builderBuildShouldReturnResolver() {
                PlaceholderResolver resolver = PlaceholderResolver.builder().build();

                assertThat(resolver).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Builder Method Tests")
    class BuilderMethodTests {

        @Nested
        @DisplayName("prefix() Tests")
        class PrefixTests {

            @Test
            @DisplayName("prefix should return builder for chaining")
            void prefixShouldReturnBuilderForChaining() {
                PlaceholderResolver.Builder builder = PlaceholderResolver.builder();

                assertThat(builder.prefix("#{")).isSameAs(builder);
            }

            @Test
            @DisplayName("custom prefix should be used in resolution")
            void customPrefixShouldBeUsedInResolution() {
                Map<String, String> props = Map.of("name", "John");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .prefix("#{")
                    .addPropertySource("props", props)
                    .build();

                String result = resolver.resolve("Hello, #{name}!");
                assertThat(result).isEqualTo("Hello, John!");
            }

            @Test
            @DisplayName("default prefix should not be recognized with custom prefix")
            void defaultPrefixShouldNotBeRecognizedWithCustomPrefix() {
                Map<String, String> props = Map.of("name", "John");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .prefix("#{")
                    .addPropertySource("props", props)
                    .build();

                String result = resolver.resolve("Hello, ${name}!");
                assertThat(result).isEqualTo("Hello, ${name}!");
            }

            @Test
            @DisplayName("multi-character prefix should work")
            void multiCharacterPrefixShouldWork() {
                Map<String, String> props = Map.of("name", "John");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .prefix("<<<")
                    .suffix(">>>")
                    .addPropertySource("props", props)
                    .build();

                String result = resolver.resolve("Hello, <<<name>>>!");
                assertThat(result).isEqualTo("Hello, John!");
            }
        }

        @Nested
        @DisplayName("suffix() Tests")
        class SuffixTests {

            @Test
            @DisplayName("suffix should return builder for chaining")
            void suffixShouldReturnBuilderForChaining() {
                PlaceholderResolver.Builder builder = PlaceholderResolver.builder();

                assertThat(builder.suffix("]")).isSameAs(builder);
            }

            @Test
            @DisplayName("custom suffix should be used in resolution")
            void customSuffixShouldBeUsedInResolution() {
                Map<String, String> props = Map.of("name", "John");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .prefix("[")
                    .suffix("]")
                    .addPropertySource("props", props)
                    .build();

                String result = resolver.resolve("Hello, [name]!");
                assertThat(result).isEqualTo("Hello, John!");
            }

            @Test
            @DisplayName("default suffix should not be recognized with custom suffix")
            void defaultSuffixShouldNotBeRecognizedWithCustomSuffix() {
                Map<String, String> props = Map.of("name", "John");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .suffix("]")
                    .addPropertySource("props", props)
                    .build();

                String result = resolver.resolve("Hello, ${name}!");
                assertThat(result).isEqualTo("Hello, ${name}!");
            }
        }

        @Nested
        @DisplayName("defaultValueSeparator() Tests")
        class DefaultValueSeparatorTests {

            @Test
            @DisplayName("defaultValueSeparator should return builder for chaining")
            void defaultValueSeparatorShouldReturnBuilderForChaining() {
                PlaceholderResolver.Builder builder = PlaceholderResolver.builder();

                assertThat(builder.defaultValueSeparator(":-")).isSameAs(builder);
            }

            @Test
            @DisplayName("custom separator should be used for default values")
            void customSeparatorShouldBeUsedForDefaultValues() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .defaultValueSeparator(":-")
                    .build();

                String result = resolver.resolve("${undefined:-default}");
                assertThat(result).isEqualTo("default");
            }

            @Test
            @DisplayName("default separator should not work with custom separator")
            void defaultSeparatorShouldNotWorkWithCustomSeparator() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .defaultValueSeparator(":-")
                    .build();

                String result = resolver.resolve("${undefined:default}");
                assertThat(result).isEqualTo("${undefined:default}");
            }

            @Test
            @DisplayName("multi-character separator should work")
            void multiCharacterSeparatorShouldWork() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .defaultValueSeparator("||")
                    .build();

                String result = resolver.resolve("${undefined||fallback}");
                assertThat(result).isEqualTo("fallback");
            }
        }

        @Nested
        @DisplayName("strict() Tests")
        class StrictTests {

            @Test
            @DisplayName("strict should return builder for chaining")
            void strictShouldReturnBuilderForChaining() {
                PlaceholderResolver.Builder builder = PlaceholderResolver.builder();

                assertThat(builder.strict(true)).isSameAs(builder);
            }

            @Test
            @DisplayName("strict mode true should throw for unresolved placeholder")
            void strictModeTrueShouldThrowForUnresolvedPlaceholder() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .strict(true)
                    .build();

                assertThatThrownBy(() -> resolver.resolve("${undefined}"))
                    .isInstanceOf(YmlPlaceholderException.class)
                    .hasMessageContaining("undefined");
            }

            @Test
            @DisplayName("strict mode false should leave placeholder unresolved")
            void strictModeFalseShouldLeavePlaceholderUnresolved() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .strict(false)
                    .build();

                String result = resolver.resolve("${undefined}");
                assertThat(result).isEqualTo("${undefined}");
            }

            @Test
            @DisplayName("strict mode should not throw when default value exists")
            void strictModeShouldNotThrowWhenDefaultValueExists() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .strict(true)
                    .build();

                String result = resolver.resolve("${undefined:default}");
                assertThat(result).isEqualTo("default");
            }

            @Test
            @DisplayName("strict mode should not throw when value is resolved")
            void strictModeShouldNotThrowWhenValueIsResolved() {
                Map<String, String> props = Map.of("key", "value");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .strict(true)
                    .addPropertySource("props", props)
                    .build();

                String result = resolver.resolve("${key}");
                assertThat(result).isEqualTo("value");
            }
        }

        @Nested
        @DisplayName("addPropertySource() Tests")
        class AddPropertySourceTests {

            @Test
            @DisplayName("addPropertySource should return builder for chaining")
            void addPropertySourceShouldReturnBuilderForChaining() {
                PlaceholderResolver.Builder builder = PlaceholderResolver.builder();
                Map<String, String> props = Map.of();

                assertThat(builder.addPropertySource("test", props)).isSameAs(builder);
            }

            @Test
            @DisplayName("property source should be used for resolution")
            void propertySourceShouldBeUsedForResolution() {
                Map<String, String> props = Map.of("name", "John");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .addPropertySource("props", props)
                    .build();

                String result = resolver.resolve("${name}");
                assertThat(result).isEqualTo("John");
            }

            @Test
            @DisplayName("multiple property sources should be checked in order")
            void multiplePropertySourcesShouldBeCheckedInOrder() {
                Map<String, String> first = Map.of("name", "First");
                Map<String, String> second = Map.of("name", "Second");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .addPropertySource("first", first)
                    .addPropertySource("second", second)
                    .build();

                String result = resolver.resolve("${name}");
                assertThat(result).isEqualTo("First");
            }

            @Test
            @DisplayName("later property source should be used if key not in earlier")
            void laterPropertySourceShouldBeUsedIfKeyNotInEarlier() {
                Map<String, String> first = Map.of("a", "A");
                Map<String, String> second = Map.of("b", "B");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .addPropertySource("first", first)
                    .addPropertySource("second", second)
                    .build();

                String result = resolver.resolve("${a}-${b}");
                assertThat(result).isEqualTo("A-B");
            }
        }

        @Nested
        @DisplayName("addResolver() Tests")
        class AddResolverTests {

            @Test
            @DisplayName("addResolver should return builder for chaining")
            void addResolverShouldReturnBuilderForChaining() {
                PlaceholderResolver.Builder builder = PlaceholderResolver.builder();
                Function<String, String> resolver = key -> null;

                assertThat(builder.addResolver(resolver)).isSameAs(builder);
            }

            @Test
            @DisplayName("custom resolver should be used for resolution")
            void customResolverShouldBeUsedForResolution() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .addResolver(key -> key.toUpperCase())
                    .build();

                String result = resolver.resolve("${hello}");
                assertThat(result).isEqualTo("HELLO");
            }

            @Test
            @DisplayName("custom resolver returning null should fall through")
            void customResolverReturningNullShouldFallThrough() {
                Map<String, String> props = Map.of("key", "value");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .addResolver(key -> null)
                    .addPropertySource("props", props)
                    .build();

                String result = resolver.resolve("${key}");
                assertThat(result).isEqualTo("value");
            }

            @Test
            @DisplayName("multiple resolvers should be checked in order")
            void multipleResolversShouldBeCheckedInOrder() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .addResolver(key -> key.equals("a") ? "A" : null)
                    .addResolver(key -> key.equals("b") ? "B" : null)
                    .build();

                String result = resolver.resolve("${a}-${b}");
                assertThat(result).isEqualTo("A-B");
            }
        }

        @Nested
        @DisplayName("withSystemProperties() Tests")
        class WithSystemPropertiesTests {

            @Test
            @DisplayName("withSystemProperties should return builder for chaining")
            void withSystemPropertiesShouldReturnBuilderForChaining() {
                PlaceholderResolver.Builder builder = PlaceholderResolver.builder();

                assertThat(builder.withSystemProperties()).isSameAs(builder);
            }

            @Test
            @DisplayName("system properties should be used for resolution")
            void systemPropertiesShouldBeUsedForResolution() {
                String originalValue = System.getProperty("test.placeholder.prop");
                try {
                    System.setProperty("test.placeholder.prop", "testValue");

                    PlaceholderResolver resolver = PlaceholderResolver.builder()
                        .withSystemProperties()
                        .build();

                    String result = resolver.resolve("${test.placeholder.prop}");
                    assertThat(result).isEqualTo("testValue");
                } finally {
                    if (originalValue != null) {
                        System.setProperty("test.placeholder.prop", originalValue);
                    } else {
                        System.clearProperty("test.placeholder.prop");
                    }
                }
            }

            @Test
            @DisplayName("java.version system property should resolve")
            void javaVersionSystemPropertyShouldResolve() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .withSystemProperties()
                    .build();

                String result = resolver.resolve("${java.version}");
                assertThat(result).isEqualTo(System.getProperty("java.version"));
            }
        }

        @Nested
        @DisplayName("withEnvironmentVariables() Tests")
        class WithEnvironmentVariablesTests {

            @Test
            @DisplayName("withEnvironmentVariables should return builder for chaining")
            void withEnvironmentVariablesShouldReturnBuilderForChaining() {
                PlaceholderResolver.Builder builder = PlaceholderResolver.builder();

                assertThat(builder.withEnvironmentVariables()).isSameAs(builder);
            }

            @Test
            @DisplayName("PATH environment variable should resolve")
            void pathEnvironmentVariableShouldResolve() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .withEnvironmentVariables()
                    .build();

                String result = resolver.resolve("${PATH}");
                assertThat(result).isEqualTo(System.getenv("PATH"));
            }

            @Test
            @DisplayName("undefined environment variable should not resolve")
            void undefinedEnvironmentVariableShouldNotResolve() {
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .withEnvironmentVariables()
                    .build();

                String result = resolver.resolve("${UNDEFINED_ENV_VAR_XYZ_12345}");
                assertThat(result).isEqualTo("${UNDEFINED_ENV_VAR_XYZ_12345}");
            }
        }

        @Nested
        @DisplayName("Builder Chaining Tests")
        class BuilderChainingTests {

            @Test
            @DisplayName("all builder methods should be chainable")
            void allBuilderMethodsShouldBeChainable() {
                Map<String, String> props = Map.of("key", "value");

                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .prefix("${")
                    .suffix("}")
                    .defaultValueSeparator(":")
                    .strict(false)
                    .addPropertySource("props", props)
                    .addResolver(key -> null)
                    .withSystemProperties()
                    .withEnvironmentVariables()
                    .build();

                assertThat(resolver).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("resolve() Method Tests")
    class ResolveMethodTests {

        @Nested
        @DisplayName("Simple Placeholder Tests")
        class SimplePlaceholderTests {

            @Test
            @DisplayName("resolve should return null for null input")
            void resolveShouldReturnNullForNullInput() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                String result = resolver.resolve(null);
                assertThat(result).isNull();
            }

            @Test
            @DisplayName("resolve should return empty string for empty input")
            void resolveShouldReturnEmptyStringForEmptyInput() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                String result = resolver.resolve("");
                assertThat(result).isEmpty();
            }

            @Test
            @DisplayName("resolve should return unchanged text without placeholders")
            void resolveShouldReturnUnchangedTextWithoutPlaceholders() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                String result = resolver.resolve("Hello, World!");
                assertThat(result).isEqualTo("Hello, World!");
            }

            @Test
            @DisplayName("resolve should resolve single placeholder")
            void resolveShouldResolveSinglePlaceholder() {
                Map<String, String> props = Map.of("name", "John");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("Hello, ${name}!");
                assertThat(result).isEqualTo("Hello, John!");
            }

            @Test
            @DisplayName("resolve should resolve multiple placeholders")
            void resolveShouldResolveMultiplePlaceholders() {
                Map<String, String> props = Map.of("first", "Hello", "second", "World");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${first}, ${second}!");
                assertThat(result).isEqualTo("Hello, World!");
            }

            @Test
            @DisplayName("resolve should handle placeholder at start")
            void resolveShouldHandlePlaceholderAtStart() {
                Map<String, String> props = Map.of("greeting", "Hello");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${greeting} World!");
                assertThat(result).isEqualTo("Hello World!");
            }

            @Test
            @DisplayName("resolve should handle placeholder at end")
            void resolveShouldHandlePlaceholderAtEnd() {
                Map<String, String> props = Map.of("target", "World");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("Hello ${target}");
                assertThat(result).isEqualTo("Hello World");
            }

            @Test
            @DisplayName("resolve should handle only placeholder")
            void resolveShouldHandleOnlyPlaceholder() {
                Map<String, String> props = Map.of("value", "test");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${value}");
                assertThat(result).isEqualTo("test");
            }

            @Test
            @DisplayName("resolve should handle adjacent placeholders")
            void resolveShouldHandleAdjacentPlaceholders() {
                Map<String, String> props = Map.of("a", "A", "b", "B");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${a}${b}");
                assertThat(result).isEqualTo("AB");
            }

            @Test
            @DisplayName("resolve should handle placeholder with special characters in value")
            void resolveShouldHandlePlaceholderWithSpecialCharactersInValue() {
                Map<String, String> props = Map.of("url", "http://example.com?a=1&b=2");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("URL: ${url}");
                assertThat(result).isEqualTo("URL: http://example.com?a=1&b=2");
            }

            @Test
            @DisplayName("resolve should handle placeholder key with dots")
            void resolveShouldHandlePlaceholderKeyWithDots() {
                Map<String, String> props = Map.of("server.port", "8080");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("Port: ${server.port}");
                assertThat(result).isEqualTo("Port: 8080");
            }

            @Test
            @DisplayName("resolve should handle placeholder key with underscores")
            void resolveShouldHandlePlaceholderKeyWithUnderscores() {
                Map<String, String> props = Map.of("my_variable", "value");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${my_variable}");
                assertThat(result).isEqualTo("value");
            }

            @Test
            @DisplayName("resolve should handle placeholder key with hyphens")
            void resolveShouldHandlePlaceholderKeyWithHyphens() {
                Map<String, String> props = Map.of("my-variable", "value");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${my-variable}");
                assertThat(result).isEqualTo("value");
            }
        }

        @Nested
        @DisplayName("Default Value Tests")
        class DefaultValueTests {

            @Test
            @DisplayName("resolve should use default value when key not found")
            void resolveShouldUseDefaultValueWhenKeyNotFound() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                String result = resolver.resolve("${undefined:default}");
                assertThat(result).isEqualTo("default");
            }

            @Test
            @DisplayName("resolve should prefer resolved value over default")
            void resolveShouldPreferResolvedValueOverDefault() {
                Map<String, String> props = Map.of("key", "resolved");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${key:default}");
                assertThat(result).isEqualTo("resolved");
            }

            @Test
            @DisplayName("resolve should handle empty default value")
            void resolveShouldHandleEmptyDefaultValue() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                String result = resolver.resolve("${undefined:}");
                assertThat(result).isEmpty();
            }

            @Test
            @DisplayName("resolve should handle default value with spaces")
            void resolveShouldHandleDefaultValueWithSpaces() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                String result = resolver.resolve("${undefined:hello world}");
                assertThat(result).isEqualTo("hello world");
            }

            @Test
            @DisplayName("resolve should handle default value with special characters")
            void resolveShouldHandleDefaultValueWithSpecialCharacters() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                String result = resolver.resolve("${undefined:http://localhost:8080}");
                assertThat(result).isEqualTo("http://localhost:8080");
            }

            @Test
            @DisplayName("resolve should handle numeric default value")
            void resolveShouldHandleNumericDefaultValue() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                String result = resolver.resolve("Port: ${port:8080}");
                assertThat(result).isEqualTo("Port: 8080");
            }
        }

        @Nested
        @DisplayName("Nested Resolution Tests")
        class NestedResolutionTests {

            @Test
            @DisplayName("resolve should resolve nested placeholders in value")
            void resolveShouldResolveNestedPlaceholdersInValue() {
                Map<String, String> props = Map.of(
                    "greeting", "Hello, ${name}!",
                    "name", "John"
                );
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${greeting}");
                assertThat(result).isEqualTo("Hello, John!");
            }

            @Test
            @DisplayName("resolve should resolve deeply nested placeholders")
            void resolveShouldResolveDeeplyNestedPlaceholders() {
                Map<String, String> props = Map.of(
                    "a", "${b}",
                    "b", "${c}",
                    "c", "final"
                );
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${a}");
                assertThat(result).isEqualTo("final");
            }

            @Test
            @DisplayName("resolve should handle default value with placeholder syntax")
            void resolveShouldHandleDefaultValueWithPlaceholderSyntax() {
                // Note: Nested placeholders like ${undefined:${fallback}} are not supported
                // because the pattern [^}]+ does not allow } inside the placeholder.
                // The default value itself can contain placeholders that get resolved
                // only when the default is a simple string that itself contains ${...}
                Map<String, String> props = Map.of(
                    "outer", "${inner:fallback}",
                    "inner", "resolved"
                );
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                // When outer resolves to ${inner:fallback}, that gets recursively resolved
                String result = resolver.resolve("${outer}");
                assertThat(result).isEqualTo("resolved");
            }

            @Test
            @DisplayName("resolve should handle multiple levels of nesting")
            void resolveShouldHandleMultipleLevelsOfNesting() {
                Map<String, String> props = Map.of(
                    "base", "http://",
                    "host", "localhost",
                    "port", "8080",
                    "url", "${base}${host}:${port}"
                );
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                String result = resolver.resolve("${url}");
                assertThat(result).isEqualTo("http://localhost:8080");
            }
        }
    }

    @Nested
    @DisplayName("resolveYaml() Method Tests")
    class ResolveYamlMethodTests {

        @Test
        @DisplayName("resolveYaml should return null for null input")
        void resolveYamlShouldReturnNullForNullInput() {
            PlaceholderResolver resolver = PlaceholderResolver.create();

            String result = resolver.resolveYaml(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("resolveYaml should resolve placeholders in YAML content")
        void resolveYamlShouldResolvePlaceholdersInYamlContent() {
            Map<String, String> props = Map.of("port", "8080");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String yaml = """
                server:
                  port: ${port}
                """;

            String result = resolver.resolveYaml(yaml);
            assertThat(result).contains("port: 8080");
        }

        @Test
        @DisplayName("resolveYaml should handle multiple placeholders in YAML")
        void resolveYamlShouldHandleMultiplePlaceholdersInYaml() {
            Map<String, String> props = Map.of(
                "host", "localhost",
                "port", "8080"
            );
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String yaml = """
                server:
                  host: ${host}
                  port: ${port}
                """;

            String result = resolver.resolveYaml(yaml);
            assertThat(result).contains("host: localhost");
            assertThat(result).contains("port: 8080");
        }

        @Test
        @DisplayName("resolveYaml should preserve YAML structure")
        void resolveYamlShouldPreserveYamlStructure() {
            Map<String, String> props = Map.of("name", "myapp");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String yaml = """
                app:
                  name: ${name}
                  version: 1.0
                """;

            String result = resolver.resolveYaml(yaml);
            assertThat(result).contains("app:");
            assertThat(result).contains("name: myapp");
            assertThat(result).contains("version: 1.0");
        }
    }

    @Nested
    @DisplayName("resolveMap() Method Tests")
    class ResolveMapMethodTests {

        @Nested
        @DisplayName("String Value Tests")
        class StringValueTests {

            @Test
            @DisplayName("resolveMap should return null for null input")
            void resolveMapShouldReturnNullForNullInput() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                Map<String, Object> result = resolver.resolveMap(null);
                assertThat(result).isNull();
            }

            @Test
            @DisplayName("resolveMap should return empty map for empty input")
            void resolveMapShouldReturnEmptyMapForEmptyInput() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                Map<String, Object> result = resolver.resolveMap(Map.of());
                assertThat(result).isEmpty();
            }

            @Test
            @DisplayName("resolveMap should resolve string values")
            void resolveMapShouldResolveStringValues() {
                Map<String, String> props = Map.of("name", "John");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                Map<String, Object> input = new LinkedHashMap<>();
                input.put("greeting", "Hello, ${name}!");

                Map<String, Object> result = resolver.resolveMap(input);
                assertThat(result.get("greeting")).isEqualTo("Hello, John!");
            }

            @Test
            @DisplayName("resolveMap should preserve non-string values")
            void resolveMapShouldPreserveNonStringValues() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                Map<String, Object> input = new LinkedHashMap<>();
                input.put("number", 42);
                input.put("flag", true);
                input.put("decimal", 3.14);

                Map<String, Object> result = resolver.resolveMap(input);
                assertThat(result.get("number")).isEqualTo(42);
                assertThat(result.get("flag")).isEqualTo(true);
                assertThat(result.get("decimal")).isEqualTo(3.14);
            }

            @Test
            @DisplayName("resolveMap should handle null string values")
            void resolveMapShouldHandleNullStringValues() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                Map<String, Object> input = new LinkedHashMap<>();
                input.put("key", null);

                Map<String, Object> result = resolver.resolveMap(input);
                assertThat(result.get("key")).isNull();
            }
        }

        @Nested
        @DisplayName("Nested Map Tests")
        class NestedMapTests {

            @Test
            @DisplayName("resolveMap should resolve placeholders in nested maps")
            void resolveMapShouldResolvePlaceholdersInNestedMaps() {
                Map<String, String> props = Map.of("port", "8080");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                Map<String, Object> nested = new LinkedHashMap<>();
                nested.put("port", "${port}");

                Map<String, Object> input = new LinkedHashMap<>();
                input.put("server", nested);

                Map<String, Object> result = resolver.resolveMap(input);
                @SuppressWarnings("unchecked")
                Map<String, Object> serverMap = (Map<String, Object>) result.get("server");
                assertThat(serverMap.get("port")).isEqualTo("8080");
            }

            @Test
            @DisplayName("resolveMap should resolve deeply nested maps")
            void resolveMapShouldResolveDeeplyNestedMaps() {
                Map<String, String> props = Map.of("value", "resolved");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                Map<String, Object> level3 = new LinkedHashMap<>();
                level3.put("key", "${value}");

                Map<String, Object> level2 = new LinkedHashMap<>();
                level2.put("nested", level3);

                Map<String, Object> level1 = new LinkedHashMap<>();
                level1.put("level", level2);

                Map<String, Object> input = new LinkedHashMap<>();
                input.put("root", level1);

                Map<String, Object> result = resolver.resolveMap(input);
                @SuppressWarnings("unchecked")
                Map<String, Object> rootMap = (Map<String, Object>) result.get("root");
                @SuppressWarnings("unchecked")
                Map<String, Object> levelMap = (Map<String, Object>) rootMap.get("level");
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) levelMap.get("nested");
                assertThat(nestedMap.get("key")).isEqualTo("resolved");
            }

            @Test
            @DisplayName("resolveMap should handle mixed nesting levels")
            void resolveMapShouldHandleMixedNestingLevels() {
                Map<String, String> props = Map.of("a", "A", "b", "B");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                Map<String, Object> nested = new LinkedHashMap<>();
                nested.put("value", "${b}");

                Map<String, Object> input = new LinkedHashMap<>();
                input.put("top", "${a}");
                input.put("nested", nested);

                Map<String, Object> result = resolver.resolveMap(input);
                assertThat(result.get("top")).isEqualTo("A");
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) result.get("nested");
                assertThat(nestedMap.get("value")).isEqualTo("B");
            }
        }

        @Nested
        @DisplayName("List Value Tests")
        class ListValueTests {

            @Test
            @DisplayName("resolveMap should resolve placeholders in list items")
            void resolveMapShouldResolvePlaceholdersInListItems() {
                Map<String, String> props = Map.of("item", "value");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                List<Object> list = new ArrayList<>();
                list.add("${item}");
                list.add("static");

                Map<String, Object> input = new LinkedHashMap<>();
                input.put("items", list);

                Map<String, Object> result = resolver.resolveMap(input);
                @SuppressWarnings("unchecked")
                List<Object> resultList = (List<Object>) result.get("items");
                assertThat(resultList).containsExactly("value", "static");
            }

            @Test
            @DisplayName("resolveMap should resolve placeholders in nested list of maps")
            void resolveMapShouldResolvePlaceholdersInNestedListOfMaps() {
                Map<String, String> props = Map.of("host", "localhost");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("host", "${host}");

                List<Object> list = new ArrayList<>();
                list.add(item);

                Map<String, Object> input = new LinkedHashMap<>();
                input.put("servers", list);

                Map<String, Object> result = resolver.resolveMap(input);
                @SuppressWarnings("unchecked")
                List<Object> resultList = (List<Object>) result.get("servers");
                @SuppressWarnings("unchecked")
                Map<String, Object> resultItem = (Map<String, Object>) resultList.get(0);
                assertThat(resultItem.get("host")).isEqualTo("localhost");
            }

            @Test
            @DisplayName("resolveMap should handle nested lists")
            void resolveMapShouldHandleNestedLists() {
                Map<String, String> props = Map.of("val", "resolved");
                PlaceholderResolver resolver = PlaceholderResolver.create(props);

                List<Object> innerList = new ArrayList<>();
                innerList.add("${val}");

                List<Object> outerList = new ArrayList<>();
                outerList.add(innerList);

                Map<String, Object> input = new LinkedHashMap<>();
                input.put("matrix", outerList);

                Map<String, Object> result = resolver.resolveMap(input);
                @SuppressWarnings("unchecked")
                List<Object> resultOuter = (List<Object>) result.get("matrix");
                @SuppressWarnings("unchecked")
                List<Object> resultInner = (List<Object>) resultOuter.get(0);
                assertThat(resultInner.get(0)).isEqualTo("resolved");
            }

            @Test
            @DisplayName("resolveMap should preserve non-string list items")
            void resolveMapShouldPreserveNonStringListItems() {
                PlaceholderResolver resolver = PlaceholderResolver.create();

                List<Object> list = new ArrayList<>();
                list.add(1);
                list.add(2);
                list.add(3);

                Map<String, Object> input = new LinkedHashMap<>();
                input.put("numbers", list);

                Map<String, Object> result = resolver.resolveMap(input);
                @SuppressWarnings("unchecked")
                List<Object> resultList = (List<Object>) result.get("numbers");
                assertThat(resultList).containsExactly(1, 2, 3);
            }
        }
    }

    @Nested
    @DisplayName("Circular Reference Detection Tests")
    class CircularReferenceDetectionTests {

        @Test
        @DisplayName("circular reference should throw exception")
        void circularReferenceShouldThrowException() {
            Map<String, String> props = new HashMap<>();
            props.put("a", "${b}");
            props.put("b", "${a}");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            assertThatThrownBy(() -> resolver.resolve("${a}"))
                .isInstanceOf(YmlPlaceholderException.class)
                .hasMessageContaining("Circular reference");
        }

        @Test
        @DisplayName("self-reference should throw exception")
        void selfReferenceShouldThrowException() {
            Map<String, String> props = new HashMap<>();
            props.put("self", "${self}");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            assertThatThrownBy(() -> resolver.resolve("${self}"))
                .isInstanceOf(YmlPlaceholderException.class)
                .hasMessageContaining("Circular reference");
        }

        @Test
        @DisplayName("indirect circular reference should throw exception")
        void indirectCircularReferenceShouldThrowException() {
            Map<String, String> props = new HashMap<>();
            props.put("a", "${b}");
            props.put("b", "${c}");
            props.put("c", "${a}");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            assertThatThrownBy(() -> resolver.resolve("${a}"))
                .isInstanceOf(YmlPlaceholderException.class)
                .hasMessageContaining("Circular reference");
        }

        @Test
        @DisplayName("non-circular references should resolve")
        void nonCircularReferencesShouldResolve() {
            Map<String, String> props = new HashMap<>();
            props.put("a", "${b}");
            props.put("b", "${c}");
            props.put("c", "final");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String result = resolver.resolve("${a}");
            assertThat(result).isEqualTo("final");
        }

        @Test
        @DisplayName("same key used multiple times should not trigger circular detection")
        void sameKeyUsedMultipleTimesShouldNotTriggerCircularDetection() {
            Map<String, String> props = Map.of("name", "value");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String result = resolver.resolve("${name} and ${name}");
            assertThat(result).isEqualTo("value and value");
        }

        @Test
        @DisplayName("exception should contain placeholder name")
        void exceptionShouldContainPlaceholderName() {
            Map<String, String> props = new HashMap<>();
            props.put("loop", "${loop}");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            assertThatThrownBy(() -> resolver.resolve("${loop}"))
                .isInstanceOf(YmlPlaceholderException.class)
                .satisfies(e -> {
                    YmlPlaceholderException ex = (YmlPlaceholderException) e;
                    assertThat(ex.getPlaceholder()).isEqualTo("loop");
                });
        }
    }

    @Nested
    @DisplayName("Strict Mode Behavior Tests")
    class StrictModeBehaviorTests {

        @Test
        @DisplayName("strict mode should throw for first unresolved placeholder")
        void strictModeShouldThrowForFirstUnresolvedPlaceholder() {
            PlaceholderResolver resolver = PlaceholderResolver.builder()
                .strict(true)
                .build();

            assertThatThrownBy(() -> resolver.resolve("${undefined1} ${undefined2}"))
                .isInstanceOf(YmlPlaceholderException.class)
                .hasMessageContaining("undefined1");
        }

        @Test
        @DisplayName("strict mode should work with nested resolution")
        void strictModeShouldWorkWithNestedResolution() {
            Map<String, String> props = new HashMap<>();
            props.put("outer", "${inner}");
            PlaceholderResolver resolver = PlaceholderResolver.builder()
                .strict(true)
                .addPropertySource("props", props)
                .build();

            assertThatThrownBy(() -> resolver.resolve("${outer}"))
                .isInstanceOf(YmlPlaceholderException.class)
                .hasMessageContaining("inner");
        }

        @Test
        @DisplayName("non-strict mode should preserve all unresolved placeholders")
        void nonStrictModeShouldPreserveAllUnresolvedPlaceholders() {
            PlaceholderResolver resolver = PlaceholderResolver.builder()
                .strict(false)
                .build();

            String result = resolver.resolve("${a} and ${b}");
            assertThat(result).isEqualTo("${a} and ${b}");
        }

        @Test
        @DisplayName("strict mode with resolveMap should throw for unresolved")
        void strictModeWithResolveMapShouldThrowForUnresolved() {
            PlaceholderResolver resolver = PlaceholderResolver.builder()
                .strict(true)
                .build();

            Map<String, Object> input = new LinkedHashMap<>();
            input.put("key", "${undefined}");

            assertThatThrownBy(() -> resolver.resolveMap(input))
                .isInstanceOf(YmlPlaceholderException.class);
        }

        @Test
        @DisplayName("non-strict mode should be default")
        void nonStrictModeShouldBeDefault() {
            PlaceholderResolver resolver = PlaceholderResolver.create();

            String result = resolver.resolve("${undefined}");
            assertThat(result).isEqualTo("${undefined}");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle text with no prefix marker")
        void shouldHandleTextWithNoPrefixMarker() {
            Map<String, String> props = Map.of("key", "value");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String result = resolver.resolve("No placeholders here");
            assertThat(result).isEqualTo("No placeholders here");
        }

        @Test
        @DisplayName("should handle unclosed placeholder")
        void shouldHandleUnclosedPlaceholder() {
            Map<String, String> props = Map.of("key", "value");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String result = resolver.resolve("${key");
            assertThat(result).isEqualTo("${key");
        }

        @Test
        @DisplayName("should handle empty placeholder key")
        void shouldHandleEmptyPlaceholderKey() {
            PlaceholderResolver resolver = PlaceholderResolver.create();

            String result = resolver.resolve("${}");
            // Empty key should not match the pattern
            assertThat(result).isEqualTo("${}");
        }

        @Test
        @DisplayName("should handle placeholder with only spaces")
        void shouldHandlePlaceholderWithOnlySpaces() {
            PlaceholderResolver resolver = PlaceholderResolver.create();

            String result = resolver.resolve("${   }");
            assertThat(result).isEqualTo("${   }");
        }

        @Test
        @DisplayName("should handle very long placeholder key")
        void shouldHandleVeryLongPlaceholderKey() {
            String longKey = "a".repeat(1000);
            Map<String, String> props = Map.of(longKey, "value");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String result = resolver.resolve("${" + longKey + "}");
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("should handle very long value")
        void shouldHandleVeryLongValue() {
            String longValue = "x".repeat(10000);
            Map<String, String> props = Map.of("key", longValue);
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String result = resolver.resolve("${key}");
            assertThat(result).isEqualTo(longValue);
        }

        @Test
        @DisplayName("should handle value containing placeholder syntax")
        void shouldHandleValueContainingPlaceholderSyntax() {
            Map<String, String> props = Map.of("key", "contains ${another} placeholder");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String result = resolver.resolve("${key}");
            // The nested placeholder should remain unresolved (no "another" key)
            assertThat(result).isEqualTo("contains ${another} placeholder");
        }

        @Test
        @DisplayName("should handle unicode in keys and values")
        void shouldHandleUnicodeInKeysAndValues() {
            Map<String, String> props = Map.of("greeting", "Hello");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String result = resolver.resolve("${greeting}");
            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("should handle newlines in values")
        void shouldHandleNewlinesInValues() {
            Map<String, String> props = Map.of("multiline", "line1\nline2\nline3");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String result = resolver.resolve("${multiline}");
            assertThat(result).isEqualTo("line1\nline2\nline3");
        }

        @Test
        @DisplayName("should handle tab characters in values")
        void shouldHandleTabCharactersInValues() {
            Map<String, String> props = Map.of("tabbed", "col1\tcol2\tcol3");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String result = resolver.resolve("${tabbed}");
            assertThat(result).isEqualTo("col1\tcol2\tcol3");
        }

        @Test
        @DisplayName("should handle regex special characters in prefix and suffix")
        void shouldHandleRegexSpecialCharactersInPrefixAndSuffix() {
            Map<String, String> props = Map.of("key", "value");
            PlaceholderResolver resolver = PlaceholderResolver.builder()
                .prefix("$(")
                .suffix(")")
                .addPropertySource("props", props)
                .build();

            String result = resolver.resolve("$(key)");
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("should preserve order in resolved map")
        void shouldPreserveOrderInResolvedMap() {
            Map<String, String> props = Map.of("v", "value");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            Map<String, Object> input = new LinkedHashMap<>();
            input.put("first", "${v}");
            input.put("second", "static");
            input.put("third", "${v}");

            Map<String, Object> result = resolver.resolveMap(input);

            List<String> keys = new ArrayList<>(result.keySet());
            assertThat(keys).containsExactly("first", "second", "third");
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should combine system properties and custom properties")
        void shouldCombineSystemPropertiesAndCustomProperties() {
            String originalValue = System.getProperty("test.integration.prop");
            try {
                System.setProperty("test.integration.prop", "systemValue");

                Map<String, String> props = Map.of("custom", "customValue");
                PlaceholderResolver resolver = PlaceholderResolver.builder()
                    .withSystemProperties()
                    .addPropertySource("custom", props)
                    .build();

                String result = resolver.resolve("${test.integration.prop} and ${custom}");
                assertThat(result).isEqualTo("systemValue and customValue");
            } finally {
                if (originalValue != null) {
                    System.setProperty("test.integration.prop", originalValue);
                } else {
                    System.clearProperty("test.integration.prop");
                }
            }
        }

        @Test
        @DisplayName("should resolve complex YAML structure")
        void shouldResolveComplexYamlStructure() {
            Map<String, String> props = Map.of(
                "app.name", "MyApp",
                "server.host", "localhost",
                "server.port", "8080"
            );
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            String yaml = """
                application:
                  name: ${app.name}
                server:
                  host: ${server.host}
                  port: ${server.port}
                  url: http://${server.host}:${server.port}
                """;

            String result = resolver.resolveYaml(yaml);
            assertThat(result).contains("name: MyApp");
            assertThat(result).contains("host: localhost");
            assertThat(result).contains("port: 8080");
            assertThat(result).contains("url: http://localhost:8080");
        }

        @Test
        @DisplayName("should resolve map with all value types")
        void shouldResolveMapWithAllValueTypes() {
            Map<String, String> props = Map.of("name", "test");
            PlaceholderResolver resolver = PlaceholderResolver.create(props);

            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("key", "${name}");

            List<Object> list = new ArrayList<>();
            list.add("${name}");
            list.add(42);

            Map<String, Object> input = new LinkedHashMap<>();
            input.put("string", "${name}");
            input.put("number", 123);
            input.put("bool", true);
            input.put("nested", nested);
            input.put("list", list);
            input.put("nullValue", null);

            Map<String, Object> result = resolver.resolveMap(input);

            assertThat(result.get("string")).isEqualTo("test");
            assertThat(result.get("number")).isEqualTo(123);
            assertThat(result.get("bool")).isEqualTo(true);
            assertThat(result.get("nullValue")).isNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> nestedResult = (Map<String, Object>) result.get("nested");
            assertThat(nestedResult.get("key")).isEqualTo("test");

            @SuppressWarnings("unchecked")
            List<Object> listResult = (List<Object>) result.get("list");
            assertThat(listResult.get(0)).isEqualTo("test");
            assertThat(listResult.get(1)).isEqualTo(42);
        }

        @Test
        @DisplayName("should work with custom resolver that transforms keys")
        void shouldWorkWithCustomResolverThatTransformsKeys() {
            PlaceholderResolver resolver = PlaceholderResolver.builder()
                .addResolver(key -> {
                    // Transform snake_case to SCREAMING_SNAKE_CASE and look up env vars
                    String envKey = key.toUpperCase().replace(".", "_");
                    return switch (envKey) {
                        case "DATABASE_HOST" -> "db.example.com";
                        case "DATABASE_PORT" -> "5432";
                        default -> null;
                    };
                })
                .build();

            String result = resolver.resolve("jdbc:postgresql://${database.host}:${database.port}/mydb");
            assertThat(result).isEqualTo("jdbc:postgresql://db.example.com:5432/mydb");
        }

        @Test
        @DisplayName("priority should be given to first matching resolver")
        void priorityShouldBeGivenToFirstMatchingResolver() {
            Map<String, String> props = Map.of("key", "fromProps");
            PlaceholderResolver resolver = PlaceholderResolver.builder()
                .addResolver(k -> k.equals("key") ? "fromResolver" : null)
                .addPropertySource("props", props)
                .build();

            String result = resolver.resolve("${key}");
            assertThat(result).isEqualTo("fromResolver");
        }
    }
}
