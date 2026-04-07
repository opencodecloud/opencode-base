
package cloud.opencode.base.json.annotation;

import cloud.opencode.base.json.identity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for JSON reference and identity annotations and their supporting classes.
 * JSON 引用和身份注解及其支持类的测试。
 */
@DisplayName("JSON Reference Annotations | JSON 引用注解")
class JsonReferenceAnnotationsTest {

    // ---- Test fixtures ----

    @SuppressWarnings("unused")
    static class Parent {
        @JsonManagedReference
        private List<Child> children;

        @JsonManagedReference("custom")
        private List<Child> otherChildren;
    }

    @SuppressWarnings("unused")
    static class Child {
        @JsonBackReference
        private Parent parent;

        @JsonBackReference("custom")
        private Parent otherParent;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
    @SuppressWarnings("unused")
    static class NodeDefault {
        private String name;
    }

    @JsonIdentityInfo(
            generator = ObjectIdGenerators.UUIDGenerator.class,
            property = "nodeId",
            scope = NodeCustom.class,
            resolver = SimpleObjectIdResolver.class
    )
    @SuppressWarnings("unused")
    static class NodeCustom {
        private String name;
    }

    // ---- JsonManagedReference tests ----

    @Nested
    @DisplayName("JsonManagedReference")
    class JsonManagedReferenceTest {

        @Test
        @DisplayName("default value is 'defaultReference'")
        void defaultValue() throws NoSuchFieldException {
            Field field = Parent.class.getDeclaredField("children");
            JsonManagedReference ann = field.getAnnotation(JsonManagedReference.class);

            assertThat(ann).isNotNull();
            assertThat(ann.value()).isEqualTo("defaultReference");
        }

        @Test
        @DisplayName("custom value is preserved")
        void customValue() throws NoSuchFieldException {
            Field field = Parent.class.getDeclaredField("otherChildren");
            JsonManagedReference ann = field.getAnnotation(JsonManagedReference.class);

            assertThat(ann).isNotNull();
            assertThat(ann.value()).isEqualTo("custom");
        }

        @Test
        @DisplayName("annotation is present at runtime")
        void runtimeRetention() throws NoSuchFieldException {
            Field field = Parent.class.getDeclaredField("children");
            assertThat(field.isAnnotationPresent(JsonManagedReference.class)).isTrue();
        }
    }

    // ---- JsonBackReference tests ----

    @Nested
    @DisplayName("JsonBackReference")
    class JsonBackReferenceTest {

        @Test
        @DisplayName("default value is 'defaultReference'")
        void defaultValue() throws NoSuchFieldException {
            Field field = Child.class.getDeclaredField("parent");
            JsonBackReference ann = field.getAnnotation(JsonBackReference.class);

            assertThat(ann).isNotNull();
            assertThat(ann.value()).isEqualTo("defaultReference");
        }

        @Test
        @DisplayName("custom value is preserved")
        void customValue() throws NoSuchFieldException {
            Field field = Child.class.getDeclaredField("otherParent");
            JsonBackReference ann = field.getAnnotation(JsonBackReference.class);

            assertThat(ann).isNotNull();
            assertThat(ann.value()).isEqualTo("custom");
        }

        @Test
        @DisplayName("matching reference names pair correctly")
        void matchingPair() throws NoSuchFieldException {
            JsonManagedReference managed = Parent.class
                    .getDeclaredField("otherChildren")
                    .getAnnotation(JsonManagedReference.class);
            JsonBackReference back = Child.class
                    .getDeclaredField("otherParent")
                    .getAnnotation(JsonBackReference.class);

            assertThat(managed.value()).isEqualTo(back.value());
        }
    }

    // ---- JsonIdentityInfo tests ----

    @Nested
    @DisplayName("JsonIdentityInfo")
    class JsonIdentityInfoTest {

        @Test
        @DisplayName("default property and scope values")
        void defaults() {
            JsonIdentityInfo ann = NodeDefault.class.getAnnotation(JsonIdentityInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.generator()).isEqualTo(ObjectIdGenerators.IntSequenceGenerator.class);
            assertThat(ann.property()).isEqualTo("@id");
            assertThat(ann.scope()).isEqualTo(Void.class);
            assertThat(ann.resolver()).isEqualTo(SimpleObjectIdResolver.class);
        }

        @Test
        @DisplayName("custom annotation values")
        void customValues() {
            JsonIdentityInfo ann = NodeCustom.class.getAnnotation(JsonIdentityInfo.class);

            assertThat(ann).isNotNull();
            assertThat(ann.generator()).isEqualTo(ObjectIdGenerators.UUIDGenerator.class);
            assertThat(ann.property()).isEqualTo("nodeId");
            assertThat(ann.scope()).isEqualTo(NodeCustom.class);
            assertThat(ann.resolver()).isEqualTo(SimpleObjectIdResolver.class);
        }

        @Test
        @DisplayName("annotation is present on type at runtime")
        void runtimeRetention() {
            assertThat(NodeDefault.class.isAnnotationPresent(JsonIdentityInfo.class)).isTrue();
        }
    }

    // ---- ObjectIdGenerators tests ----

    @Nested
    @DisplayName("ObjectIdGenerators")
    class ObjectIdGeneratorsTest {

        @Test
        @DisplayName("IntSequenceGenerator produces sequential integers")
        void intSequence() {
            var gen = new ObjectIdGenerators.IntSequenceGenerator();

            assertThat(gen.generateId(new Object())).isEqualTo(1);
            assertThat(gen.generateId(new Object())).isEqualTo(2);
            assertThat(gen.generateId(new Object())).isEqualTo(3);
        }

        @Test
        @DisplayName("IntSequenceGenerator scope and compatibility")
        void intSequenceScope() {
            var gen1 = new ObjectIdGenerators.IntSequenceGenerator();
            var gen2 = new ObjectIdGenerators.IntSequenceGenerator();
            var genScoped = new ObjectIdGenerators.IntSequenceGenerator(String.class);

            assertThat(gen1.getScope()).isEqualTo(Object.class);
            assertThat(gen1.canUseFor(gen2)).isTrue();
            assertThat(gen1.canUseFor(genScoped)).isFalse();
            assertThat(gen1.canUseFor(null)).isFalse();
        }

        @Test
        @DisplayName("UUIDGenerator produces unique UUIDs")
        void uuidGenerator() {
            var gen = new ObjectIdGenerators.UUIDGenerator();

            UUID id1 = gen.generateId(new Object());
            UUID id2 = gen.generateId(new Object());

            assertThat(id1).isNotNull();
            assertThat(id2).isNotNull();
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("UUIDGenerator scope and compatibility")
        void uuidScope() {
            var gen1 = new ObjectIdGenerators.UUIDGenerator();
            var gen2 = new ObjectIdGenerators.UUIDGenerator();
            var intGen = new ObjectIdGenerators.IntSequenceGenerator();

            assertThat(gen1.canUseFor(gen2)).isTrue();
            assertThat(gen1.canUseFor(intGen)).isFalse();
        }

        @Test
        @DisplayName("PropertyGenerator throws on direct generateId call")
        void propertyGenerator() {
            var gen = new ObjectIdGenerators.PropertyGenerator();

            assertThatThrownBy(() -> gen.generateId(new Object()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("StringIdGenerator produces unique strings")
        void stringIdGenerator() {
            var gen = new ObjectIdGenerators.StringIdGenerator();

            String id1 = gen.generateId(new Object());
            String id2 = gen.generateId(new Object());

            assertThat(id1).isNotBlank();
            assertThat(id2).isNotBlank();
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("StringIdGenerator scope and compatibility")
        void stringIdScope() {
            var gen1 = new ObjectIdGenerators.StringIdGenerator();
            var gen2 = new ObjectIdGenerators.StringIdGenerator(String.class);

            assertThat(gen1.getScope()).isEqualTo(Object.class);
            assertThat(gen2.getScope()).isEqualTo(String.class);
            assertThat(gen1.canUseFor(gen2)).isFalse();
        }
    }

    // ---- SimpleObjectIdResolver tests ----

    @Nested
    @DisplayName("SimpleObjectIdResolver")
    class SimpleObjectIdResolverTest {

        @Test
        @DisplayName("bind and resolve an object by IdKey")
        void bindAndResolve() {
            var resolver = new SimpleObjectIdResolver();
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            String obj = "test-object";

            resolver.bindItem(key, obj);
            Object resolved = resolver.resolveId(key);

            assertThat(resolved).isSameAs(obj);
        }

        @Test
        @DisplayName("resolveId returns null for unknown key")
        void resolveUnknown() {
            var resolver = new SimpleObjectIdResolver();
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 999);

            assertThat(resolver.resolveId(key)).isNull();
        }

        @Test
        @DisplayName("binding same object to same key is idempotent")
        void idempotentBind() {
            var resolver = new SimpleObjectIdResolver();
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            String obj = "test-object";

            resolver.bindItem(key, obj);
            // Should not throw
            assertThatCode(() -> resolver.bindItem(key, obj)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("binding different object to same key throws")
        void conflictingBind() {
            var resolver = new SimpleObjectIdResolver();
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);

            resolver.bindItem(key, "first");

            assertThatThrownBy(() -> resolver.bindItem(key, "second"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("null id key throws on bind")
        void nullKeyBind() {
            var resolver = new SimpleObjectIdResolver();

            assertThatThrownBy(() -> resolver.bindItem(null, "obj"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null pojo throws on bind")
        void nullPojoBind() {
            var resolver = new SimpleObjectIdResolver();
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);

            assertThatThrownBy(() -> resolver.bindItem(key, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null id key throws on resolve")
        void nullKeyResolve() {
            var resolver = new SimpleObjectIdResolver();

            assertThatThrownBy(() -> resolver.resolveId(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("canUseFor returns true for same type")
        void canUseForSameType() {
            var resolver = new SimpleObjectIdResolver();

            assertThat(resolver.canUseFor(new SimpleObjectIdResolver())).isTrue();
            assertThat(resolver.canUseFor(null)).isFalse();
        }

        @Test
        @DisplayName("newForDeserialization returns fresh instance")
        void newForDeserialization() {
            var resolver = new SimpleObjectIdResolver();
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            resolver.bindItem(key, "existing");

            ObjectIdResolver newResolver = resolver.newForDeserialization(null);

            assertThat(newResolver).isNotSameAs(resolver);
            assertThat(newResolver.resolveId(key)).isNull();
        }
    }

    // ---- IdKey record tests ----

    @Nested
    @DisplayName("ObjectIdGenerator.IdKey")
    class IdKeyTest {

        @Test
        @DisplayName("record equality and hashCode")
        void equalityAndHashCode() {
            var key1 = new ObjectIdGenerator.IdKey(String.class, Void.class, 42);
            var key2 = new ObjectIdGenerator.IdKey(String.class, Void.class, 42);
            var key3 = new ObjectIdGenerator.IdKey(Integer.class, Void.class, 42);

            assertThat(key1).isEqualTo(key2);
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
            assertThat(key1).isNotEqualTo(key3);
        }

        @Test
        @DisplayName("record accessors return correct values")
        void accessors() {
            var key = new ObjectIdGenerator.IdKey(String.class, Object.class, "myKey");

            assertThat(key.type()).isEqualTo(String.class);
            assertThat(key.scope()).isEqualTo(Object.class);
            assertThat(key.key()).isEqualTo("myKey");
        }
    }
}
