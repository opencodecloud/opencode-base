package cloud.opencode.base.json.identity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SimpleObjectIdResolver")
class SimpleObjectIdResolverTest {

    @Nested
    @DisplayName("bindItem and resolveId")
    class BindAndResolveTest {

        @Test
        @DisplayName("bind and resolve round-trip returns same instance")
        void roundTrip() {
            var resolver = new SimpleObjectIdResolver();
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            var obj = new Object();

            resolver.bindItem(key, obj);

            assertThat(resolver.resolveId(key)).isSameAs(obj);
        }

        @Test
        @DisplayName("multiple bindings with different keys all resolve correctly")
        void multipleBindings() {
            var resolver = new SimpleObjectIdResolver();
            var key1 = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            var key2 = new ObjectIdGenerator.IdKey(String.class, Void.class, 2);
            var key3 = new ObjectIdGenerator.IdKey(Integer.class, Void.class, 1);
            String obj1 = "first";
            String obj2 = "second";
            List<Integer> obj3 = List.of(1, 2, 3);

            resolver.bindItem(key1, obj1);
            resolver.bindItem(key2, obj2);
            resolver.bindItem(key3, obj3);

            assertThat(resolver.resolveId(key1)).isSameAs(obj1);
            assertThat(resolver.resolveId(key2)).isSameAs(obj2);
            assertThat(resolver.resolveId(key3)).isSameAs(obj3);
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
            String obj = "same-object";

            resolver.bindItem(key, obj);

            assertThatCode(() -> resolver.bindItem(key, obj)).doesNotThrowAnyException();
            assertThat(resolver.resolveId(key)).isSameAs(obj);
        }

        @Test
        @DisplayName("binding different object to same key throws IllegalStateException")
        void conflictingBind() {
            var resolver = new SimpleObjectIdResolver();
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);

            resolver.bindItem(key, "first");

            assertThatThrownBy(() -> resolver.bindItem(key, "second"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("different object");
        }

        @Test
        @DisplayName("keys with same value but different type are independent")
        void keysWithDifferentTypesAreIndependent() {
            var resolver = new SimpleObjectIdResolver();
            var key1 = new ObjectIdGenerator.IdKey(String.class, Void.class, 42);
            var key2 = new ObjectIdGenerator.IdKey(Integer.class, Void.class, 42);
            String obj1 = "string-obj";
            Integer obj2 = 123;

            resolver.bindItem(key1, obj1);
            resolver.bindItem(key2, obj2);

            assertThat(resolver.resolveId(key1)).isSameAs(obj1);
            assertThat(resolver.resolveId(key2)).isSameAs(obj2);
        }

        @Test
        @DisplayName("keys with same value but different scope are independent")
        void keysWithDifferentScopesAreIndependent() {
            var resolver = new SimpleObjectIdResolver();
            var key1 = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            var key2 = new ObjectIdGenerator.IdKey(String.class, Object.class, 1);
            String obj1 = "scope-void";
            String obj2 = "scope-object";

            resolver.bindItem(key1, obj1);
            resolver.bindItem(key2, obj2);

            assertThat(resolver.resolveId(key1)).isSameAs(obj1);
            assertThat(resolver.resolveId(key2)).isSameAs(obj2);
        }
    }

    @Nested
    @DisplayName("Null handling")
    class NullHandlingTest {

        @Test
        @DisplayName("bindItem with null id throws IllegalArgumentException")
        void nullIdBind() {
            var resolver = new SimpleObjectIdResolver();

            assertThatThrownBy(() -> resolver.bindItem(null, "obj"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("bindItem with null pojo throws IllegalArgumentException")
        void nullPojoBind() {
            var resolver = new SimpleObjectIdResolver();
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);

            assertThatThrownBy(() -> resolver.bindItem(key, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("resolveId with null throws IllegalArgumentException")
        void nullResolve() {
            var resolver = new SimpleObjectIdResolver();

            assertThatThrownBy(() -> resolver.resolveId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("canUseFor")
    class CanUseForTest {

        @Test
        @DisplayName("returns true for same type")
        void sameType() {
            var resolver = new SimpleObjectIdResolver();
            assertThat(resolver.canUseFor(new SimpleObjectIdResolver())).isTrue();
        }

        @Test
        @DisplayName("returns false for null")
        void nullResolver() {
            var resolver = new SimpleObjectIdResolver();
            assertThat(resolver.canUseFor(null)).isFalse();
        }

        @Test
        @DisplayName("returns false for different implementation")
        void differentImpl() {
            var resolver = new SimpleObjectIdResolver();
            ObjectIdResolver anonymous = new ObjectIdResolver() {
                @Override
                public void bindItem(ObjectIdGenerator.IdKey id, Object pojo) {}
                @Override
                public Object resolveId(ObjectIdGenerator.IdKey id) { return null; }
                @Override
                public boolean canUseFor(ObjectIdResolver resolverType) { return false; }
                @Override
                public ObjectIdResolver newForDeserialization(Object context) { return this; }
            };
            assertThat(resolver.canUseFor(anonymous)).isFalse();
        }
    }

    @Nested
    @DisplayName("newForDeserialization")
    class NewForDeserializationTest {

        @Test
        @DisplayName("returns a new instance")
        void returnsNewInstance() {
            var resolver = new SimpleObjectIdResolver();
            ObjectIdResolver newResolver = resolver.newForDeserialization(null);
            assertThat(newResolver).isNotSameAs(resolver);
            assertThat(newResolver).isInstanceOf(SimpleObjectIdResolver.class);
        }

        @Test
        @DisplayName("new instance does not share state with original")
        void doesNotShareState() {
            var resolver = new SimpleObjectIdResolver();
            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 1);
            resolver.bindItem(key, "bound-object");

            ObjectIdResolver newResolver = resolver.newForDeserialization(null);

            assertThat(newResolver.resolveId(key)).isNull();
        }

        @Test
        @DisplayName("new instance is fully functional")
        void newInstanceIsFunctional() {
            var resolver = new SimpleObjectIdResolver();
            ObjectIdResolver newResolver = resolver.newForDeserialization("some-context");

            var key = new ObjectIdGenerator.IdKey(String.class, Void.class, 42);
            String obj = "test";

            newResolver.bindItem(key, obj);

            assertThat(newResolver.resolveId(key)).isSameAs(obj);
        }

        @Test
        @DisplayName("accepts any context including null")
        void acceptsAnyContext() {
            var resolver = new SimpleObjectIdResolver();
            assertThatCode(() -> resolver.newForDeserialization(null)).doesNotThrowAnyException();
            assertThatCode(() -> resolver.newForDeserialization("context")).doesNotThrowAnyException();
            assertThatCode(() -> resolver.newForDeserialization(42)).doesNotThrowAnyException();
        }
    }
}
