package cloud.opencode.base.json.identity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ObjectIdGenerators")
class ObjectIdGeneratorsTest {

    @Nested
    @DisplayName("IntSequenceGenerator")
    class IntSequenceGeneratorTest {

        @Test
        @DisplayName("sequence starts from 1")
        void startsFromOne() {
            var gen = new ObjectIdGenerators.IntSequenceGenerator();
            assertThat(gen.generateId("anything")).isEqualTo(1);
        }

        @Test
        @DisplayName("generates strictly incrementing sequence")
        void incrementsSequentially() {
            var gen = new ObjectIdGenerators.IntSequenceGenerator();
            for (int i = 1; i <= 100; i++) {
                assertThat(gen.generateId(null)).isEqualTo(i);
            }
        }

        @Test
        @DisplayName("each instance has its own counter")
        void independentCounters() {
            var gen1 = new ObjectIdGenerators.IntSequenceGenerator();
            var gen2 = new ObjectIdGenerators.IntSequenceGenerator();

            gen1.generateId(null);
            gen1.generateId(null);

            assertThat(gen2.generateId(null)).isEqualTo(1);
        }

        @Test
        @DisplayName("accepts null forPojo")
        void acceptsNullPojo() {
            var gen = new ObjectIdGenerators.IntSequenceGenerator();
            assertThat(gen.generateId(null)).isEqualTo(1);
        }

        @Test
        @DisplayName("default scope is Object.class")
        void defaultScope() {
            var gen = new ObjectIdGenerators.IntSequenceGenerator();
            assertThat(gen.getScope()).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("custom scope is preserved")
        void customScope() {
            var gen = new ObjectIdGenerators.IntSequenceGenerator(String.class);
            assertThat(gen.getScope()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("canUseFor same class and same scope")
        void canUseForSameClassSameScope() {
            var gen1 = new ObjectIdGenerators.IntSequenceGenerator();
            var gen2 = new ObjectIdGenerators.IntSequenceGenerator();
            assertThat(gen1.canUseFor(gen2)).isTrue();
        }

        @Test
        @DisplayName("canUseFor same class but different scope returns false")
        void canUseForDifferentScope() {
            var gen1 = new ObjectIdGenerators.IntSequenceGenerator(Object.class);
            var gen2 = new ObjectIdGenerators.IntSequenceGenerator(String.class);
            assertThat(gen1.canUseFor(gen2)).isFalse();
        }

        @Test
        @DisplayName("canUseFor different generator class returns false")
        void canUseForDifferentClass() {
            var intGen = new ObjectIdGenerators.IntSequenceGenerator();
            var uuidGen = new ObjectIdGenerators.UUIDGenerator();
            assertThat(intGen.canUseFor(uuidGen)).isFalse();
        }

        @Test
        @DisplayName("canUseFor null returns false")
        void canUseForNull() {
            var gen = new ObjectIdGenerators.IntSequenceGenerator();
            assertThat(gen.canUseFor(null)).isFalse();
        }

        @Test
        @DisplayName("thread-safe concurrent generation produces unique values")
        void threadSafety() throws InterruptedException {
            var gen = new ObjectIdGenerators.IntSequenceGenerator();
            int threadCount = 8;
            int idsPerThread = 500;
            Set<Integer> allIds = ConcurrentHashMap.newKeySet();
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                for (int t = 0; t < threadCount; t++) {
                    executor.submit(() -> {
                        try {
                            for (int i = 0; i < idsPerThread; i++) {
                                allIds.add(gen.generateId(null));
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                latch.await();
            }

            assertThat(allIds).hasSize(threadCount * idsPerThread);
        }
    }

    @Nested
    @DisplayName("UUIDGenerator")
    class UUIDGeneratorTest {

        @Test
        @DisplayName("generates non-null UUIDs")
        void generatesNonNull() {
            var gen = new ObjectIdGenerators.UUIDGenerator();
            assertThat(gen.generateId(new Object())).isNotNull();
        }

        @Test
        @DisplayName("generates unique UUIDs across many calls")
        void uniqueAcrossManyCalls() {
            var gen = new ObjectIdGenerators.UUIDGenerator();
            Set<UUID> ids = new HashSet<>();
            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generateId(null));
            }
            assertThat(ids).hasSize(1000);
        }

        @Test
        @DisplayName("accepts null forPojo")
        void acceptsNullPojo() {
            var gen = new ObjectIdGenerators.UUIDGenerator();
            assertThat(gen.generateId(null)).isNotNull();
        }

        @Test
        @DisplayName("default scope is Object.class")
        void defaultScope() {
            assertThat(new ObjectIdGenerators.UUIDGenerator().getScope()).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("custom scope is preserved")
        void customScope() {
            assertThat(new ObjectIdGenerators.UUIDGenerator(Integer.class).getScope()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("canUseFor same class and scope")
        void canUseForSame() {
            var gen1 = new ObjectIdGenerators.UUIDGenerator();
            var gen2 = new ObjectIdGenerators.UUIDGenerator();
            assertThat(gen1.canUseFor(gen2)).isTrue();
        }

        @Test
        @DisplayName("canUseFor different scope returns false")
        void canUseForDifferentScope() {
            var gen1 = new ObjectIdGenerators.UUIDGenerator(Object.class);
            var gen2 = new ObjectIdGenerators.UUIDGenerator(String.class);
            assertThat(gen1.canUseFor(gen2)).isFalse();
        }

        @Test
        @DisplayName("canUseFor different generator type returns false")
        void canUseForDifferentType() {
            var uuidGen = new ObjectIdGenerators.UUIDGenerator();
            var strGen = new ObjectIdGenerators.StringIdGenerator();
            assertThat(uuidGen.canUseFor(strGen)).isFalse();
        }

        @Test
        @DisplayName("canUseFor null returns false")
        void canUseForNull() {
            assertThat(new ObjectIdGenerators.UUIDGenerator().canUseFor(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("StringIdGenerator")
    class StringIdGeneratorTest {

        @Test
        @DisplayName("generates non-null non-blank strings")
        void generatesNonBlank() {
            var gen = new ObjectIdGenerators.StringIdGenerator();
            String id = gen.generateId(null);
            assertThat(id).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("generates unique strings across many calls")
        void uniqueAcrossManyCalls() {
            var gen = new ObjectIdGenerators.StringIdGenerator();
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generateId(null));
            }
            assertThat(ids).hasSize(1000);
        }

        @Test
        @DisplayName("generated strings are valid UUID format")
        void uuidFormat() {
            var gen = new ObjectIdGenerators.StringIdGenerator();
            String id = gen.generateId(null);
            assertThatCode(() -> UUID.fromString(id)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("accepts null forPojo")
        void acceptsNullPojo() {
            var gen = new ObjectIdGenerators.StringIdGenerator();
            assertThat(gen.generateId(null)).isNotNull();
        }

        @Test
        @DisplayName("default scope is Object.class")
        void defaultScope() {
            assertThat(new ObjectIdGenerators.StringIdGenerator().getScope()).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("custom scope is preserved")
        void customScope() {
            assertThat(new ObjectIdGenerators.StringIdGenerator(Long.class).getScope()).isEqualTo(Long.class);
        }

        @Test
        @DisplayName("canUseFor same class and scope returns true")
        void canUseForSame() {
            var gen1 = new ObjectIdGenerators.StringIdGenerator();
            var gen2 = new ObjectIdGenerators.StringIdGenerator();
            assertThat(gen1.canUseFor(gen2)).isTrue();
        }

        @Test
        @DisplayName("canUseFor different scope returns false")
        void canUseForDifferentScope() {
            var gen1 = new ObjectIdGenerators.StringIdGenerator(Object.class);
            var gen2 = new ObjectIdGenerators.StringIdGenerator(String.class);
            assertThat(gen1.canUseFor(gen2)).isFalse();
        }

        @Test
        @DisplayName("canUseFor null returns false")
        void canUseForNull() {
            assertThat(new ObjectIdGenerators.StringIdGenerator().canUseFor(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("PropertyGenerator")
    class PropertyGeneratorTest {

        @Test
        @DisplayName("generateId throws UnsupportedOperationException")
        void generateIdThrows() {
            var gen = new ObjectIdGenerators.PropertyGenerator();
            assertThatThrownBy(() -> gen.generateId(new Object()))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("PropertyGenerator");
        }

        @Test
        @DisplayName("generateId with null also throws")
        void generateIdWithNullThrows() {
            var gen = new ObjectIdGenerators.PropertyGenerator();
            assertThatThrownBy(() -> gen.generateId(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("default scope is Object.class")
        void defaultScope() {
            assertThat(new ObjectIdGenerators.PropertyGenerator().getScope()).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("custom scope is preserved")
        void customScope() {
            assertThat(new ObjectIdGenerators.PropertyGenerator(String.class).getScope()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("canUseFor same class and scope returns true")
        void canUseForSame() {
            var gen1 = new ObjectIdGenerators.PropertyGenerator();
            var gen2 = new ObjectIdGenerators.PropertyGenerator();
            assertThat(gen1.canUseFor(gen2)).isTrue();
        }

        @Test
        @DisplayName("canUseFor different scope returns false")
        void canUseForDifferentScope() {
            var gen1 = new ObjectIdGenerators.PropertyGenerator(Object.class);
            var gen2 = new ObjectIdGenerators.PropertyGenerator(String.class);
            assertThat(gen1.canUseFor(gen2)).isFalse();
        }

        @Test
        @DisplayName("canUseFor different type returns false")
        void canUseForDifferentType() {
            var propGen = new ObjectIdGenerators.PropertyGenerator();
            var intGen = new ObjectIdGenerators.IntSequenceGenerator();
            assertThat(propGen.canUseFor(intGen)).isFalse();
        }

        @Test
        @DisplayName("canUseFor null returns false")
        void canUseForNull() {
            assertThat(new ObjectIdGenerators.PropertyGenerator().canUseFor(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("IdKey")
    class IdKeyTest {

        @Test
        @DisplayName("equals for identical fields")
        void equalsIdentical() {
            var key1 = new ObjectIdGenerator.IdKey(String.class, Object.class, 42);
            var key2 = new ObjectIdGenerator.IdKey(String.class, Object.class, 42);
            assertThat(key1).isEqualTo(key2);
        }

        @Test
        @DisplayName("not equals when type differs")
        void notEqualsType() {
            var key1 = new ObjectIdGenerator.IdKey(String.class, Object.class, 42);
            var key2 = new ObjectIdGenerator.IdKey(Integer.class, Object.class, 42);
            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("not equals when scope differs")
        void notEqualsScope() {
            var key1 = new ObjectIdGenerator.IdKey(String.class, Object.class, 42);
            var key2 = new ObjectIdGenerator.IdKey(String.class, Void.class, 42);
            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("not equals when key differs")
        void notEqualsKey() {
            var key1 = new ObjectIdGenerator.IdKey(String.class, Object.class, 42);
            var key2 = new ObjectIdGenerator.IdKey(String.class, Object.class, 99);
            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("hashCode is consistent for equal keys")
        void hashCodeConsistent() {
            var key1 = new ObjectIdGenerator.IdKey(String.class, Void.class, "abc");
            var key2 = new ObjectIdGenerator.IdKey(String.class, Void.class, "abc");
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }

        @Test
        @DisplayName("works correctly as HashMap key")
        void worksAsMapKey() {
            var key1 = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            var key2 = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            var map = new java.util.HashMap<ObjectIdGenerator.IdKey, String>();
            map.put(key1, "value");
            assertThat(map.get(key2)).isEqualTo("value");
        }

        @Test
        @DisplayName("toString contains type, scope, and key info")
        void toStringContainsInfo() {
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 42);
            String str = key.toString();
            assertThat(str).contains("String").contains("Void").contains("42");
        }

        @Test
        @DisplayName("supports null key value")
        void nullKeyValue() {
            var key1 = new ObjectIdGenerator.IdKey(String.class, Void.class, null);
            var key2 = new ObjectIdGenerator.IdKey(String.class, Void.class, null);
            assertThat(key1).isEqualTo(key2);
        }

        @Test
        @DisplayName("null key not equal to non-null key")
        void nullKeyNotEqualNonNull() {
            var key1 = new ObjectIdGenerator.IdKey(String.class, Void.class, null);
            var key2 = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("different key types with same toString are not equal")
        void differentKeyTypesNotEqual() {
            var key1 = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            var key2 = new ObjectIdGenerator.IdKey(String.class, Void.class, "1");
            assertThat(key1).isNotEqualTo(key2);
        }
    }

    @Nested
    @DisplayName("Cross-generator compatibility")
    class CrossGeneratorTest {

        @Test
        @DisplayName("no generator type is compatible with a different generator type")
        void noCrossTypeCompatibility() {
            var intGen = new ObjectIdGenerators.IntSequenceGenerator();
            var uuidGen = new ObjectIdGenerators.UUIDGenerator();
            var strGen = new ObjectIdGenerators.StringIdGenerator();
            var propGen = new ObjectIdGenerators.PropertyGenerator();

            assertThat(intGen.canUseFor(uuidGen)).isFalse();
            assertThat(intGen.canUseFor(strGen)).isFalse();
            assertThat(intGen.canUseFor(propGen)).isFalse();
            assertThat(uuidGen.canUseFor(intGen)).isFalse();
            assertThat(uuidGen.canUseFor(strGen)).isFalse();
            assertThat(uuidGen.canUseFor(propGen)).isFalse();
            assertThat(strGen.canUseFor(intGen)).isFalse();
            assertThat(strGen.canUseFor(uuidGen)).isFalse();
            assertThat(strGen.canUseFor(propGen)).isFalse();
            assertThat(propGen.canUseFor(intGen)).isFalse();
            assertThat(propGen.canUseFor(uuidGen)).isFalse();
            assertThat(propGen.canUseFor(strGen)).isFalse();
        }
    }
}
