package cloud.opencode.base.deepclone.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableDetector 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.3
 */
@DisplayName("ImmutableDetector 测试")
class ImmutableDetectorTest {

    @Nested
    @DisplayName("isImmutableCollection() - List.of() 测试")
    class ListOfTests {

        @Test
        @DisplayName("List.of()应检测为不可变")
        void emptyListOfShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(List.of())).isTrue();
        }

        @Test
        @DisplayName("List.of(1)应检测为不可变")
        void singleElementListOfShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(List.of(1))).isTrue();
        }

        @Test
        @DisplayName("List.of(1,2,3)应检测为不可变")
        void multiElementListOfShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(List.of(1, 2, 3))).isTrue();
        }
    }

    @Nested
    @DisplayName("isImmutableCollection() - Set.of() 测试")
    class SetOfTests {

        @Test
        @DisplayName("Set.of()应检测为不可变")
        void emptySetOfShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(Set.of())).isTrue();
        }

        @Test
        @DisplayName("Set.of(1)应检测为不可变")
        void singleElementSetOfShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(Set.of(1))).isTrue();
        }
    }

    @Nested
    @DisplayName("isImmutableCollection() - Map.of() 测试")
    class MapOfTests {

        @Test
        @DisplayName("Map.of()应检测为不可变")
        void emptyMapOfShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(Map.of())).isTrue();
        }

        @Test
        @DisplayName("Map.of(k,v)应检测为不可变")
        void singleEntryMapOfShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(Map.of("k", "v"))).isTrue();
        }
    }

    @Nested
    @DisplayName("isImmutableCollection() - Collections.unmodifiable* 测试")
    class UnmodifiableTests {

        @Test
        @DisplayName("Collections.unmodifiableList应检测为不可变")
        void unmodifiableListShouldBeImmutable() {
            List<Integer> list = Collections.unmodifiableList(new ArrayList<>(List.of(1, 2)));
            assertThat(ImmutableDetector.isImmutableCollection(list)).isTrue();
        }

        @Test
        @DisplayName("Collections.unmodifiableSet应检测为不可变")
        void unmodifiableSetShouldBeImmutable() {
            Set<Integer> set = Collections.unmodifiableSet(new HashSet<>(Set.of(1, 2)));
            assertThat(ImmutableDetector.isImmutableCollection(set)).isTrue();
        }

        @Test
        @DisplayName("Collections.unmodifiableMap应检测为不可变")
        void unmodifiableMapShouldBeImmutable() {
            Map<String, Integer> map = Collections.unmodifiableMap(new HashMap<>(Map.of("a", 1)));
            assertThat(ImmutableDetector.isImmutableCollection(map)).isTrue();
        }
    }

    @Nested
    @DisplayName("isImmutableCollection() - Collections.empty* 测试")
    class EmptyCollectionsTests {

        @Test
        @DisplayName("Collections.emptyList()应检测为不可变")
        void emptyListShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(Collections.emptyList())).isTrue();
        }

        @Test
        @DisplayName("Collections.emptySet()应检测为不可变")
        void emptySetShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(Collections.emptySet())).isTrue();
        }

        @Test
        @DisplayName("Collections.emptyMap()应检测为不可变")
        void emptyMapShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(Collections.emptyMap())).isTrue();
        }
    }

    @Nested
    @DisplayName("isImmutableCollection() - Collections.singleton* 测试")
    class SingletonTests {

        @Test
        @DisplayName("Collections.singletonList()应检测为不可变")
        void singletonListShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(Collections.singletonList(1))).isTrue();
        }

        @Test
        @DisplayName("Collections.singleton()应检测为不可变")
        void singletonSetShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(Collections.singleton(1))).isTrue();
        }

        @Test
        @DisplayName("Collections.singletonMap()应检测为不可变")
        void singletonMapShouldBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(Collections.singletonMap("k", 1))).isTrue();
        }
    }

    @Nested
    @DisplayName("isImmutableCollection() - 可变集合测试")
    class MutableCollectionTests {

        @Test
        @DisplayName("new ArrayList()不应检测为不可变")
        void arrayListShouldNotBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(new ArrayList<>())).isFalse();
        }

        @Test
        @DisplayName("new HashMap()不应检测为不可变")
        void hashMapShouldNotBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(new HashMap<>())).isFalse();
        }

        @Test
        @DisplayName("new HashSet()不应检测为不可变")
        void hashSetShouldNotBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(new HashSet<>())).isFalse();
        }

        @Test
        @DisplayName("new LinkedList()不应检测为不可变")
        void linkedListShouldNotBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(new LinkedList<>())).isFalse();
        }

        @Test
        @DisplayName("new TreeMap()不应检测为不可变")
        void treeMapShouldNotBeImmutable() {
            assertThat(ImmutableDetector.isImmutableCollection(new TreeMap<>())).isFalse();
        }
    }

    @Nested
    @DisplayName("isImmutableCollection() - null和非集合测试")
    class NullAndNonCollectionTests {

        @Test
        @DisplayName("null应返回false")
        void nullShouldReturnFalse() {
            assertThat(ImmutableDetector.isImmutableCollection(null)).isFalse();
        }

        @Test
        @DisplayName("非集合对象应返回false")
        void nonCollectionShouldReturnFalse() {
            assertThat(ImmutableDetector.isImmutableCollection("string")).isFalse();
            assertThat(ImmutableDetector.isImmutableCollection(42)).isFalse();
            assertThat(ImmutableDetector.isImmutableCollection(new Object())).isFalse();
        }
    }

    @Nested
    @DisplayName("isImmutableCollectionType() 测试")
    class ImmutableCollectionTypeTests {

        @Test
        @DisplayName("List.of()的类型应检测为不可变集合类型")
        void listOfTypeShouldBeImmutableType() {
            assertThat(ImmutableDetector.isImmutableCollectionType(List.of().getClass())).isTrue();
        }

        @Test
        @DisplayName("List.of(1)的类型应检测为不可变集合类型")
        void singleElementListOfTypeShouldBeImmutableType() {
            assertThat(ImmutableDetector.isImmutableCollectionType(List.of(1).getClass())).isTrue();
        }

        @Test
        @DisplayName("Collections.emptyList()的类型应检测为不可变集合类型")
        void emptyListTypeShouldBeImmutableType() {
            assertThat(ImmutableDetector.isImmutableCollectionType(Collections.emptyList().getClass())).isTrue();
        }

        @Test
        @DisplayName("Collections.unmodifiableList()的类型应检测为不可变集合类型")
        void unmodifiableListTypeShouldBeImmutableType() {
            Class<?> type = Collections.unmodifiableList(new ArrayList<>()).getClass();
            assertThat(ImmutableDetector.isImmutableCollectionType(type)).isTrue();
        }

        @Test
        @DisplayName("ArrayList.class不应检测为不可变集合类型")
        void arrayListTypeShouldNotBeImmutableType() {
            assertThat(ImmutableDetector.isImmutableCollectionType(ArrayList.class)).isFalse();
        }

        @Test
        @DisplayName("HashMap.class不应检测为不可变集合类型")
        void hashMapTypeShouldNotBeImmutableType() {
            assertThat(ImmutableDetector.isImmutableCollectionType(HashMap.class)).isFalse();
        }

        @Test
        @DisplayName("null类型应返回false")
        void nullTypeShouldReturnFalse() {
            assertThat(ImmutableDetector.isImmutableCollectionType(null)).isFalse();
        }

        @Test
        @DisplayName("List接口本身不应检测为不可变集合类型")
        void listInterfaceShouldNotBeImmutableType() {
            assertThat(ImmutableDetector.isImmutableCollectionType(List.class)).isFalse();
        }

        @Test
        @DisplayName("Map接口本身不应检测为不可变集合类型")
        void mapInterfaceShouldNotBeImmutableType() {
            assertThat(ImmutableDetector.isImmutableCollectionType(Map.class)).isFalse();
        }
    }
}
