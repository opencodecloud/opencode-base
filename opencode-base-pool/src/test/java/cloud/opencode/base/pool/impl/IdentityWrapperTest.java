package cloud.opencode.base.pool.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IdentityWrapper")
class IdentityWrapperTest {

    @Nested
    @DisplayName("object accessor")
    class ObjectAccessor {

        @Test
        @DisplayName("should return the wrapped object")
        void shouldReturnWrappedObject() {
            String obj = "test";
            IdentityWrapper<String> wrapper = new IdentityWrapper<>(obj);
            assertThat(wrapper.object()).isSameAs(obj);
        }
    }

    @Nested
    @DisplayName("equals - identity based")
    class EqualsTest {

        @Test
        @DisplayName("should be equal when wrapping same object instance")
        void shouldBeEqualForSameInstance() {
            String obj = new String("test");
            IdentityWrapper<String> w1 = new IdentityWrapper<>(obj);
            IdentityWrapper<String> w2 = new IdentityWrapper<>(obj);
            assertThat(w1).isEqualTo(w2);
        }

        @Test
        @DisplayName("should not be equal when wrapping different instances with same value")
        void shouldNotBeEqualForDifferentInstances() {
            String obj1 = new String("test");
            String obj2 = new String("test");
            // obj1.equals(obj2) is true, but identity should differ
            IdentityWrapper<String> w1 = new IdentityWrapper<>(obj1);
            IdentityWrapper<String> w2 = new IdentityWrapper<>(obj2);
            assertThat(w1).isNotEqualTo(w2);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToSelf() {
            IdentityWrapper<String> w = new IdentityWrapper<>("test");
            assertThat(w).isEqualTo(w);
        }

        @Test
        @DisplayName("should not be equal to non-IdentityWrapper")
        void shouldNotBeEqualToOtherType() {
            IdentityWrapper<String> w = new IdentityWrapper<>("test");
            assertThat(w).isNotEqualTo("test");
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            IdentityWrapper<String> w = new IdentityWrapper<>("test");
            assertThat(w).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("hashCode - identity based")
    class HashCodeTest {

        @Test
        @DisplayName("should use System.identityHashCode")
        void shouldUseIdentityHashCode() {
            Object obj = new Object();
            IdentityWrapper<Object> wrapper = new IdentityWrapper<>(obj);
            assertThat(wrapper.hashCode()).isEqualTo(System.identityHashCode(obj));
        }

        @Test
        @DisplayName("should return same hashCode for same object")
        void shouldReturnSameHashCodeForSameObject() {
            String obj = new String("test");
            IdentityWrapper<String> w1 = new IdentityWrapper<>(obj);
            IdentityWrapper<String> w2 = new IdentityWrapper<>(obj);
            assertThat(w1.hashCode()).isEqualTo(w2.hashCode());
        }
    }

    @Nested
    @DisplayName("use in maps")
    class MapUsage {

        @Test
        @DisplayName("should distinguish different instances in HashMap")
        void shouldDistinguishInHashMap() {
            String obj1 = new String("test");
            String obj2 = new String("test");

            java.util.Map<IdentityWrapper<String>, String> map = new java.util.HashMap<>();
            map.put(new IdentityWrapper<>(obj1), "first");
            map.put(new IdentityWrapper<>(obj2), "second");

            assertThat(map).hasSize(2);
            assertThat(map.get(new IdentityWrapper<>(obj1))).isEqualTo("first");
            assertThat(map.get(new IdentityWrapper<>(obj2))).isEqualTo("second");
        }
    }
}
