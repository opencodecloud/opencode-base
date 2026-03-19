package cloud.opencode.base.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleContext Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("RuleContext Tests")
class RuleContextTest {

    private RuleContext context;

    @BeforeEach
    void setUp() {
        context = RuleContext.create();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create() should return empty context")
        void createShouldReturnEmptyContext() {
            RuleContext ctx = RuleContext.create();
            assertThat(ctx).isNotNull();
            assertThat(ctx.getVariables()).isEmpty();
            assertThat(ctx.getResults()).isEmpty();
        }

        @Test
        @DisplayName("of(Map) should create context with variables")
        void ofMapShouldCreateContextWithVariables() {
            Map<String, Object> values = Map.of("name", "John", "age", 30);
            RuleContext ctx = RuleContext.of(values);

            assertThat(ctx.<String>get("name")).isEqualTo("John");
            assertThat(ctx.<Integer>get("age")).isEqualTo(30);
        }

        @Test
        @DisplayName("of(Object...) should create context with key-value pairs")
        void ofVarargsShouldCreateContextWithKeyValuePairs() {
            RuleContext ctx = RuleContext.of("key1", "value1", "key2", 100);

            assertThat(ctx.<String>get("key1")).isEqualTo("value1");
            assertThat(ctx.<Integer>get("key2")).isEqualTo(100);
        }

        @Test
        @DisplayName("of(Object...) should throw on odd number of arguments")
        void ofVarargsShouldThrowOnOddArguments() {
            assertThatThrownBy(() -> RuleContext.of("key1", "value1", "key2"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("even number");
        }

        @Test
        @DisplayName("withFacts() should create context with fact objects")
        void withFactsShouldCreateContextWithFacts() {
            String fact1 = "string-fact";
            Integer fact2 = 42;
            RuleContext ctx = RuleContext.withFacts(fact1, fact2);

            assertThat(ctx.getFact(String.class)).contains("string-fact");
            assertThat(ctx.getFact(Integer.class)).contains(42);
        }
    }

    @Nested
    @DisplayName("Variable Operations Tests")
    class VariableOperationsTests {

        @Test
        @DisplayName("put() should store variable")
        void putShouldStoreVariable() {
            context.put("name", "Alice");
            assertThat(context.<String>get("name")).isEqualTo("Alice");
        }

        @Test
        @DisplayName("put() should return context for chaining")
        void putShouldReturnContextForChaining() {
            RuleContext result = context.put("key", "value");
            assertThat(result).isSameAs(context);
        }

        @Test
        @DisplayName("get() should return null for missing key")
        void getShouldReturnNullForMissingKey() {
            assertThat(context.<Object>get("missing")).isNull();
        }

        @Test
        @DisplayName("get() with default should return default for missing key")
        void getWithDefaultShouldReturnDefaultForMissingKey() {
            assertThat(context.get("missing", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("get() with default should return value if present")
        void getWithDefaultShouldReturnValueIfPresent() {
            context.put("key", "actual");
            assertThat(context.get("key", "default")).isEqualTo("actual");
        }

        @Test
        @DisplayName("contains() should return true for existing key")
        void containsShouldReturnTrueForExistingKey() {
            context.put("key", "value");
            assertThat(context.contains("key")).isTrue();
        }

        @Test
        @DisplayName("contains() should return false for missing key")
        void containsShouldReturnFalseForMissingKey() {
            assertThat(context.contains("missing")).isFalse();
        }

        @Test
        @DisplayName("getVariables() should return immutable copy")
        void getVariablesShouldReturnImmutableCopy() {
            context.put("key", "value");
            Map<String, Object> variables = context.getVariables();

            assertThat(variables).containsEntry("key", "value");
            assertThatThrownBy(() -> variables.put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("clearVariables() should remove all variables")
        void clearVariablesShouldRemoveAllVariables() {
            context.put("key1", "value1").put("key2", "value2");
            context.clearVariables();

            assertThat(context.getVariables()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Fact Operations Tests")
    class FactOperationsTests {

        @Test
        @DisplayName("addFact() should store typed fact")
        void addFactShouldStoreTypedFact() {
            context.addFact("test-string");
            assertThat(context.getFact(String.class)).contains("test-string");
        }

        @Test
        @DisplayName("addFact() should return context for chaining")
        void addFactShouldReturnContextForChaining() {
            RuleContext result = context.addFact("test");
            assertThat(result).isSameAs(context);
        }

        @Test
        @DisplayName("addFact(name, fact) should store named fact")
        void addNamedFactShouldStoreNamedFact() {
            context.addFact("myFact", "value");
            assertThat(context.<String>get("myFact")).isEqualTo("value");
        }

        @Test
        @DisplayName("getFact() should return empty for missing type")
        void getFactShouldReturnEmptyForMissingType() {
            assertThat(context.getFact(Double.class)).isEmpty();
        }

        @Test
        @DisplayName("getFacts() should return all facts of type")
        void getFactsShouldReturnAllFactsOfType() {
            context.addFact("first");
            context.addFact("second");
            context.addFact(100);

            List<String> strings = context.getFacts(String.class);
            assertThat(strings).containsExactly("first", "second");
        }

        @Test
        @DisplayName("contains() should check named facts")
        void containsShouldCheckNamedFacts() {
            context.addFact("factName", "factValue");
            assertThat(context.contains("factName")).isTrue();
        }

        @Test
        @DisplayName("facts() should return fact store")
        void factsShouldReturnFactStore() {
            assertThat(context.facts()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Result Operations Tests")
    class ResultOperationsTests {

        @Test
        @DisplayName("setResult() should store result")
        void setResultShouldStoreResult() {
            context.setResult("discount", 0.15);
            assertThat(context.<Double>getResult("discount")).isEqualTo(0.15);
        }

        @Test
        @DisplayName("setResult() should return context for chaining")
        void setResultShouldReturnContextForChaining() {
            RuleContext result = context.setResult("key", "value");
            assertThat(result).isSameAs(context);
        }

        @Test
        @DisplayName("getResult() should return null for missing key")
        void getResultShouldReturnNullForMissingKey() {
            assertThat(context.<Object>getResult("missing")).isNull();
        }

        @Test
        @DisplayName("getResult() with default should return default for missing key")
        void getResultWithDefaultShouldReturnDefaultForMissingKey() {
            assertThat(context.getResult("missing", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("getResults() should return immutable copy")
        void getResultsShouldReturnImmutableCopy() {
            context.setResult("key", "value");
            Map<String, Object> results = context.getResults();

            assertThat(results).containsEntry("key", "value");
            assertThatThrownBy(() -> results.put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("clearResults() should remove all results")
        void clearResultsShouldRemoveAllResults() {
            context.setResult("key1", "value1").setResult("key2", "value2");
            context.clearResults();

            assertThat(context.getResults()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("get() should fall back to named facts")
        void getShouldFallBackToNamedFacts() {
            context.addFact("factKey", "factValue");
            assertThat(context.<String>get("factKey")).isEqualTo("factValue");
        }

        @Test
        @DisplayName("variables should take precedence over named facts")
        void variablesShouldTakePrecedenceOverNamedFacts() {
            context.addFact("key", "factValue");
            context.put("key", "variableValue");

            assertThat(context.<String>get("key")).isEqualTo("variableValue");
        }
    }
}
