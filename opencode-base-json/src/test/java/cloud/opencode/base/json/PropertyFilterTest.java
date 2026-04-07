package cloud.opencode.base.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PropertyFilter")
class PropertyFilterTest {

    @Nested
    @DisplayName("includeAll")
    class IncludeAllTest {

        @Test
        @DisplayName("includes any property with any value")
        void includesEverything() {
            PropertyFilter filter = PropertyFilter.includeAll();

            assertThat(filter.includeProperty("name", "John", Object.class)).isTrue();
            assertThat(filter.includeProperty("password", "secret", String.class)).isTrue();
            assertThat(filter.includeProperty("", "empty-name", Object.class)).isTrue();
        }

        @Test
        @DisplayName("includes properties with null values")
        void includesNullValues() {
            PropertyFilter filter = PropertyFilter.includeAll();

            assertThat(filter.includeProperty("field", null, Object.class)).isTrue();
        }

        @Test
        @DisplayName("includes properties with null declaring class")
        void includesNullDeclaringClass() {
            PropertyFilter filter = PropertyFilter.includeAll();

            assertThat(filter.includeProperty("field", "value", null)).isTrue();
        }
    }

    @Nested
    @DisplayName("excludeAll")
    class ExcludeAllTest {

        @Test
        @DisplayName("excludes any property with any value")
        void excludesEverything() {
            PropertyFilter filter = PropertyFilter.excludeAll();

            assertThat(filter.includeProperty("name", "John", Object.class)).isFalse();
            assertThat(filter.includeProperty("password", "secret", String.class)).isFalse();
            assertThat(filter.includeProperty("", "", Object.class)).isFalse();
        }

        @Test
        @DisplayName("excludes properties with null values")
        void excludesNullValues() {
            PropertyFilter filter = PropertyFilter.excludeAll();

            assertThat(filter.includeProperty("field", null, Object.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("include (whitelist)")
    class IncludeTest {

        @Test
        @DisplayName("includes only specified properties")
        void includesOnlySpecified() {
            PropertyFilter filter = PropertyFilter.include("name", "email");

            assertThat(filter.includeProperty("name", "John", Object.class)).isTrue();
            assertThat(filter.includeProperty("email", "j@j.com", Object.class)).isTrue();
            assertThat(filter.includeProperty("password", "secret", Object.class)).isFalse();
            assertThat(filter.includeProperty("age", 25, Object.class)).isFalse();
        }

        @Test
        @DisplayName("empty include list matches nothing")
        void emptyInclude() {
            PropertyFilter filter = PropertyFilter.include();

            assertThat(filter.includeProperty("anything", "value", Object.class)).isFalse();
        }

        @Test
        @DisplayName("single property include")
        void singleProperty() {
            PropertyFilter filter = PropertyFilter.include("id");

            assertThat(filter.includeProperty("id", 1, Object.class)).isTrue();
            assertThat(filter.includeProperty("name", "test", Object.class)).isFalse();
        }

        @Test
        @DisplayName("include does not care about value")
        void ignoresValue() {
            PropertyFilter filter = PropertyFilter.include("name");

            assertThat(filter.includeProperty("name", null, Object.class)).isTrue();
            assertThat(filter.includeProperty("name", "", Object.class)).isTrue();
            assertThat(filter.includeProperty("name", 42, Object.class)).isTrue();
        }

        @Test
        @DisplayName("include does not care about declaring class")
        void ignoresDeclaringClass() {
            PropertyFilter filter = PropertyFilter.include("name");

            assertThat(filter.includeProperty("name", "v", String.class)).isTrue();
            assertThat(filter.includeProperty("name", "v", Integer.class)).isTrue();
            assertThat(filter.includeProperty("name", "v", null)).isTrue();
        }

        @Test
        @DisplayName("null properties array throws NullPointerException")
        void nullArrayThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PropertyFilter.include((String[]) null));
        }

        @Test
        @DisplayName("property name matching is case-sensitive")
        void caseSensitive() {
            PropertyFilter filter = PropertyFilter.include("Name");

            assertThat(filter.includeProperty("Name", "v", Object.class)).isTrue();
            assertThat(filter.includeProperty("name", "v", Object.class)).isFalse();
            assertThat(filter.includeProperty("NAME", "v", Object.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("exclude (blacklist)")
    class ExcludeTest {

        @Test
        @DisplayName("excludes specified properties, includes all others")
        void excludesSpecified() {
            PropertyFilter filter = PropertyFilter.exclude("password", "secret");

            assertThat(filter.includeProperty("name", "John", Object.class)).isTrue();
            assertThat(filter.includeProperty("email", "j@j.com", Object.class)).isTrue();
            assertThat(filter.includeProperty("password", "123", Object.class)).isFalse();
            assertThat(filter.includeProperty("secret", "abc", Object.class)).isFalse();
        }

        @Test
        @DisplayName("empty exclude list matches everything")
        void emptyExclude() {
            PropertyFilter filter = PropertyFilter.exclude();

            assertThat(filter.includeProperty("anything", "value", Object.class)).isTrue();
        }

        @Test
        @DisplayName("single property exclude")
        void singleProperty() {
            PropertyFilter filter = PropertyFilter.exclude("hidden");

            assertThat(filter.includeProperty("hidden", "v", Object.class)).isFalse();
            assertThat(filter.includeProperty("visible", "v", Object.class)).isTrue();
        }

        @Test
        @DisplayName("null properties array throws NullPointerException")
        void nullArrayThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PropertyFilter.exclude((String[]) null));
        }
    }

    @Nested
    @DisplayName("includeNonNull")
    class IncludeNonNullTest {

        @Test
        @DisplayName("includes properties with non-null values")
        void includesNonNull() {
            PropertyFilter filter = PropertyFilter.includeNonNull();

            assertThat(filter.includeProperty("name", "John", Object.class)).isTrue();
            assertThat(filter.includeProperty("count", 0, Object.class)).isTrue();
            assertThat(filter.includeProperty("flag", false, Object.class)).isTrue();
            assertThat(filter.includeProperty("empty", "", Object.class)).isTrue();
        }

        @Test
        @DisplayName("excludes properties with null values")
        void excludesNull() {
            PropertyFilter filter = PropertyFilter.includeNonNull();

            assertThat(filter.includeProperty("name", null, Object.class)).isFalse();
            assertThat(filter.includeProperty("anything", null, String.class)).isFalse();
        }

        @Test
        @DisplayName("does not care about property name")
        void ignoresPropertyName() {
            PropertyFilter filter = PropertyFilter.includeNonNull();

            assertThat(filter.includeProperty("a", "val", Object.class)).isTrue();
            assertThat(filter.includeProperty("b", "val", Object.class)).isTrue();
            assertThat(filter.includeProperty("a", null, Object.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Custom lambda filter")
    class CustomFilterTest {

        @Test
        @DisplayName("filter via lambda based on property name pattern")
        void namePatternFilter() {
            PropertyFilter filter = (name, value, clazz) -> !name.startsWith("_");

            assertThat(filter.includeProperty("name", "v", Object.class)).isTrue();
            assertThat(filter.includeProperty("_internal", "v", Object.class)).isFalse();
            assertThat(filter.includeProperty("_hidden", "v", Object.class)).isFalse();
        }

        @Test
        @DisplayName("filter via lambda based on declaring class")
        void classBasedFilter() {
            PropertyFilter filter = (name, value, clazz) ->
                    clazz != null && clazz != String.class;

            assertThat(filter.includeProperty("x", "v", Integer.class)).isTrue();
            assertThat(filter.includeProperty("x", "v", String.class)).isFalse();
        }

        @Test
        @DisplayName("filter via lambda based on value type")
        void valueTypeFilter() {
            PropertyFilter filter = (name, value, clazz) ->
                    value == null || !(value instanceof String s) || !s.isEmpty();

            assertThat(filter.includeProperty("f", "hello", Object.class)).isTrue();
            assertThat(filter.includeProperty("f", null, Object.class)).isTrue();
            assertThat(filter.includeProperty("f", "", Object.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Functional interface")
    class FunctionalInterfaceTest {

        @Test
        @DisplayName("PropertyFilter is a functional interface usable with lambdas")
        void usableAsLambda() {
            PropertyFilter filter = (name, value, clazz) -> true;

            assertThat(filter.includeProperty("test", null, Object.class)).isTrue();
        }

        @Test
        @DisplayName("PropertyFilter is a functional interface usable with method references")
        void usableWithMethodReference() {
            PropertyFilter filter = PropertyFilterTest::alwaysInclude;

            assertThat(filter.includeProperty("test", null, Object.class)).isTrue();
        }
    }

    private static boolean alwaysInclude(String name, Object value, Class<?> clazz) {
        return true;
    }
}
