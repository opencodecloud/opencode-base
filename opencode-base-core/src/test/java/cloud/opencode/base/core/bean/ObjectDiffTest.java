package cloud.opencode.base.core.bean;

import cloud.opencode.base.core.exception.OpenIllegalStateException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ObjectDiff}, {@link Diff}, {@link DiffResult}, and {@link ChangeType}.
 */
class ObjectDiffTest {

    // ==================== Test Beans ====================

    static class SimpleBean {
        private String name;
        private int age;
        private String email;

        SimpleBean() {}

        SimpleBean(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    static class Address {
        private String city;
        private String street;

        Address() {}

        Address(String city, String street) {
            this.city = city;
            this.street = street;
        }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Address a)) return false;
            return Objects.equals(city, a.city) && Objects.equals(street, a.street);
        }

        @Override
        public int hashCode() { return Objects.hash(city, street); }
    }

    static class NestedBean {
        private String name;
        private Address address;

        NestedBean() {}

        NestedBean(String name, Address address) {
            this.name = name;
            this.address = address;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }
    }

    static class SelfRef {
        private String value;
        private SelfRef next;

        SelfRef() {}

        SelfRef(String value) { this.value = value; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public SelfRef getNext() { return next; }
        public void setNext(SelfRef next) { this.next = next; }
    }

    static class WithCollection {
        private String label;
        private List<String> tags;

        WithCollection() {}

        WithCollection(String label, List<String> tags) {
            this.label = label;
            this.tags = tags;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    static class DeepLevel1 {
        private DeepLevel2 child;

        DeepLevel1() {}
        DeepLevel1(DeepLevel2 child) { this.child = child; }

        public DeepLevel2 getChild() { return child; }
        public void setChild(DeepLevel2 child) { this.child = child; }
    }

    static class DeepLevel2 {
        private DeepLevel3 child;

        DeepLevel2() {}
        DeepLevel2(DeepLevel3 child) { this.child = child; }

        public DeepLevel3 getChild() { return child; }
        public void setChild(DeepLevel3 child) { this.child = child; }
    }

    static class DeepLevel3 {
        private String value;

        DeepLevel3() {}
        DeepLevel3(String value) { this.value = value; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    record PersonRecord(String name, int age) {}

    // ==================== Tests ====================

    @Nested
    class SimpleCompare {

        @Test
        void sameObjects_noDiffs() {
            SimpleBean bean = new SimpleBean("Alice", 30, "alice@test.com");
            DiffResult<SimpleBean> result = ObjectDiff.compare(bean, bean);

            assertThat(result.hasDiffs()).isFalse();
            assertThat(result.type()).isEqualTo(SimpleBean.class);
            assertThat(result.getModified()).isEmpty();
        }

        @Test
        void equalObjects_noDiffs() {
            SimpleBean a = new SimpleBean("Alice", 30, "alice@test.com");
            SimpleBean b = new SimpleBean("Alice", 30, "alice@test.com");
            DiffResult<SimpleBean> result = ObjectDiff.compare(a, b);

            assertThat(result.hasDiffs()).isFalse();
        }

        @Test
        void differentFields_reportsModified() {
            SimpleBean a = new SimpleBean("Alice", 30, "alice@test.com");
            SimpleBean b = new SimpleBean("Bob", 25, "bob@test.com");
            DiffResult<SimpleBean> result = ObjectDiff.compare(a, b);

            assertThat(result.hasDiffs()).isTrue();
            assertThat(result.getModified()).hasSize(3);

            List<String> modifiedFields = result.getModified().stream()
                    .map(Diff::fieldName)
                    .toList();
            assertThat(modifiedFields).contains("name", "age", "email");
        }

        @Test
        void partialDifference() {
            SimpleBean a = new SimpleBean("Alice", 30, "same@test.com");
            SimpleBean b = new SimpleBean("Bob", 30, "same@test.com");
            DiffResult<SimpleBean> result = ObjectDiff.compare(a, b);

            assertThat(result.hasDiffs()).isTrue();
            assertThat(result.getModified()).hasSize(1);
            assertThat(result.getModified().getFirst().fieldName()).isEqualTo("name");
        }

        @Test
        void nullOldValue_reportsAdded() {
            SimpleBean a = new SimpleBean(null, 30, null);
            SimpleBean b = new SimpleBean("Bob", 30, "bob@test.com");
            DiffResult<SimpleBean> result = ObjectDiff.compare(a, b);

            assertThat(result.getAdded()).hasSize(2);
            List<String> addedFields = result.getAdded().stream()
                    .map(Diff::fieldName)
                    .toList();
            assertThat(addedFields).contains("name", "email");
        }

        @Test
        void nullNewValue_reportsRemoved() {
            SimpleBean a = new SimpleBean("Alice", 30, "alice@test.com");
            SimpleBean b = new SimpleBean(null, 30, null);
            DiffResult<SimpleBean> result = ObjectDiff.compare(a, b);

            assertThat(result.getRemoved()).hasSize(2);
            List<String> removedFields = result.getRemoved().stream()
                    .map(Diff::fieldName)
                    .toList();
            assertThat(removedFields).contains("name", "email");
        }

        @Test
        void bothNull_emptyResult() {
            DiffResult<SimpleBean> result = ObjectDiff.compare(null, null);
            assertThat(result.hasDiffs()).isFalse();
            assertThat(result.diffs()).isEmpty();
        }
    }

    @Nested
    class DeepCompare {

        @Test
        void nestedObjects_deepTrue_detectsChange() {
            NestedBean a = new NestedBean("Alice", new Address("Beijing", "Main St"));
            NestedBean b = new NestedBean("Alice", new Address("Shanghai", "Main St"));

            DiffResult<NestedBean> result = ObjectDiff.builder(a, b)
                    .deep(true)
                    .compare();

            assertThat(result.hasDiffs()).isTrue();
            List<String> modifiedFields = result.getModified().stream()
                    .map(Diff::fieldName)
                    .toList();
            assertThat(modifiedFields).contains("address");
        }

        @Test
        void nestedObjects_deepFalse_usesEquals() {
            Address addr1 = new Address("Beijing", "Main St");
            Address addr2 = new Address("Beijing", "Main St");
            NestedBean a = new NestedBean("Alice", addr1);
            NestedBean b = new NestedBean("Alice", addr2);

            DiffResult<NestedBean> result = ObjectDiff.builder(a, b)
                    .deep(false)
                    .compare();

            // Address implements equals, so they should be UNCHANGED
            assertThat(result.hasDiffs()).isFalse();
        }

        @Test
        void nestedObjects_sameContent_noDiff() {
            NestedBean a = new NestedBean("Alice", new Address("Beijing", "Main St"));
            NestedBean b = new NestedBean("Alice", new Address("Beijing", "Main St"));

            DiffResult<NestedBean> result = ObjectDiff.builder(a, b)
                    .deep(true)
                    .compare();

            assertThat(result.hasDiffs()).isFalse();
        }
    }

    @Nested
    class CircularReferenceDetection {

        @Test
        void selfReference_detected() {
            SelfRef a = new SelfRef("A");
            a.setNext(a); // circular

            SelfRef b = new SelfRef("A");
            b.setNext(b); // circular

            DiffResult<SelfRef> result = ObjectDiff.builder(a, b)
                    .deep(true)
                    .compare();

            // Should detect circular reference and not stackoverflow
            boolean hasCircular = result.diffs().stream()
                    .anyMatch(d -> d.changeType() == ChangeType.CIRCULAR_REFERENCE);
            assertThat(hasCircular).isTrue();
        }

        @Test
        void mutualReference_detected() {
            SelfRef a1 = new SelfRef("A1");
            SelfRef a2 = new SelfRef("A2");
            a1.setNext(a2);
            a2.setNext(a1); // mutual circular

            SelfRef b1 = new SelfRef("B1");
            SelfRef b2 = new SelfRef("B2");
            b1.setNext(b2);
            b2.setNext(b1);

            // Should not throw StackOverflowError
            DiffResult<SelfRef> result = ObjectDiff.builder(a1, b1)
                    .deep(true)
                    .compare();

            assertThat(result).isNotNull();
        }
    }

    @Nested
    class MaxDepthExceeded {

        @Test
        void exceedsMaxDepth_throwsException() {
            DeepLevel3 l3Old = new DeepLevel3("old");
            DeepLevel2 l2Old = new DeepLevel2(l3Old);
            DeepLevel1 l1Old = new DeepLevel1(l2Old);

            DeepLevel3 l3New = new DeepLevel3("new");
            DeepLevel2 l2New = new DeepLevel2(l3New);
            DeepLevel1 l1New = new DeepLevel1(l2New);

            assertThatThrownBy(() -> ObjectDiff.builder(l1Old, l1New)
                    .deep(true)
                    .maxDepth(1)
                    .compare())
                    .isInstanceOf(OpenIllegalStateException.class)
                    .hasMessageContaining("Max depth");
        }

        @Test
        void withinMaxDepth_succeeds() {
            DeepLevel3 l3Old = new DeepLevel3("old");
            DeepLevel2 l2Old = new DeepLevel2(l3Old);
            DeepLevel1 l1Old = new DeepLevel1(l2Old);

            DeepLevel3 l3New = new DeepLevel3("new");
            DeepLevel2 l2New = new DeepLevel2(l3New);
            DeepLevel1 l1New = new DeepLevel1(l2New);

            DiffResult<DeepLevel1> result = ObjectDiff.builder(l1Old, l1New)
                    .deep(true)
                    .maxDepth(5)
                    .compare();

            assertThat(result.hasDiffs()).isTrue();
        }
    }

    @Nested
    class FieldFiltering {

        @Test
        void includeFields() {
            SimpleBean a = new SimpleBean("Alice", 30, "alice@test.com");
            SimpleBean b = new SimpleBean("Bob", 25, "bob@test.com");

            DiffResult<SimpleBean> result = ObjectDiff.builder(a, b)
                    .include("name")
                    .compare();

            assertThat(result.diffs()).hasSize(1);
            assertThat(result.diffs().getFirst().fieldName()).isEqualTo("name");
        }

        @Test
        void excludeFields() {
            SimpleBean a = new SimpleBean("Alice", 30, "alice@test.com");
            SimpleBean b = new SimpleBean("Bob", 25, "bob@test.com");

            DiffResult<SimpleBean> result = ObjectDiff.builder(a, b)
                    .exclude("age", "email")
                    .compare();

            assertThat(result.diffs()).hasSize(1);
            assertThat(result.diffs().getFirst().fieldName()).isEqualTo("name");
        }

        @Test
        void includeAndExclude_includeWins() {
            SimpleBean a = new SimpleBean("Alice", 30, "alice@test.com");
            SimpleBean b = new SimpleBean("Bob", 25, "bob@test.com");

            DiffResult<SimpleBean> result = ObjectDiff.builder(a, b)
                    .include("name", "age")
                    .exclude("age")
                    .compare();

            // include limits to name and age, exclude removes age -> only name
            assertThat(result.diffs()).hasSize(1);
            assertThat(result.diffs().getFirst().fieldName()).isEqualTo("name");
        }
    }

    @Nested
    class CollectionDiffTest {

        @Test
        void collectionDiff_enabled_detectsElementChange() {
            WithCollection a = new WithCollection("A", List.of("java", "kotlin"));
            WithCollection b = new WithCollection("A", List.of("java", "scala"));

            DiffResult<WithCollection> result = ObjectDiff.builder(a, b)
                    .deep(true)
                    .collectionDiff(true)
                    .compare();

            assertThat(result.hasDiffs()).isTrue();
        }

        @Test
        void collectionDiff_disabled_usesEquals() {
            WithCollection a = new WithCollection("A", List.of("java", "kotlin"));
            WithCollection b = new WithCollection("A", List.of("java", "kotlin"));

            DiffResult<WithCollection> result = ObjectDiff.builder(a, b)
                    .deep(true)
                    .collectionDiff(false)
                    .compare();

            assertThat(result.hasDiffs()).isFalse();
        }

        @Test
        void collectionDiff_differentSizes() {
            WithCollection a = new WithCollection("A", List.of("java"));
            WithCollection b = new WithCollection("A", List.of("java", "kotlin", "scala"));

            DiffResult<WithCollection> result = ObjectDiff.builder(a, b)
                    .deep(true)
                    .collectionDiff(true)
                    .compare();

            assertThat(result.hasDiffs()).isTrue();
        }

        @Test
        void largeCollection_skippedWhenExceedsMax() {
            List<String> large = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                large.add("item" + i);
            }
            WithCollection a = new WithCollection("A", large);

            List<String> large2 = new ArrayList<>(large);
            large2.set(50, "changed");
            WithCollection b = new WithCollection("A", large2);

            DiffResult<WithCollection> result = ObjectDiff.builder(a, b)
                    .deep(true)
                    .collectionDiff(true)
                    .maxCollectionSize(10)
                    .compare();

            // tags field exceeds maxCollectionSize so deep element comparison is skipped,
            // but equals-based comparison still detects the difference → MODIFIED
            boolean tagsModified = result.diffs().stream()
                    .filter(d -> d.fieldName().equals("tags"))
                    .anyMatch(d -> d.changeType() == ChangeType.MODIFIED);
            assertThat(tagsModified).isTrue();
        }
    }

    @Nested
    class EmptyObjects {

        @Test
        void emptyBeans_noDiffs() {
            SimpleBean a = new SimpleBean();
            SimpleBean b = new SimpleBean();
            DiffResult<SimpleBean> result = ObjectDiff.compare(a, b);

            assertThat(result.hasDiffs()).isFalse();
        }

        @Test
        void emptyVsPopulated_reportsDiffs() {
            SimpleBean a = new SimpleBean();
            SimpleBean b = new SimpleBean("Bob", 25, "bob@test.com");
            DiffResult<SimpleBean> result = ObjectDiff.compare(a, b);

            assertThat(result.hasDiffs()).isTrue();
            // name and email are added (null -> value), age is modified (0 -> 25)
            assertThat(result.getAdded()).isNotEmpty();
        }
    }

    @Nested
    class RecordComparison {

        @Test
        void sameRecords_noDiffs() {
            PersonRecord a = new PersonRecord("Alice", 30);
            PersonRecord b = new PersonRecord("Alice", 30);

            DiffResult<PersonRecord> result = ObjectDiff.compare(a, b);
            assertThat(result.hasDiffs()).isFalse();
        }

        @Test
        void differentRecords_reportsModified() {
            PersonRecord a = new PersonRecord("Alice", 30);
            PersonRecord b = new PersonRecord("Bob", 25);

            DiffResult<PersonRecord> result = ObjectDiff.compare(a, b);
            assertThat(result.hasDiffs()).isTrue();
            assertThat(result.getModified()).isNotEmpty();
        }
    }

    @Nested
    class DiffResultMethods {

        @Test
        void diffs_isImmutable() {
            SimpleBean a = new SimpleBean("Alice", 30, "a@test.com");
            SimpleBean b = new SimpleBean("Bob", 25, "b@test.com");
            DiffResult<SimpleBean> result = ObjectDiff.compare(a, b);

            assertThatThrownBy(() -> result.diffs().add(new Diff<>("x", null, null, ChangeType.UNCHANGED)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void typeIsPreserved() {
            SimpleBean a = new SimpleBean("Alice", 30, "a@test.com");
            SimpleBean b = new SimpleBean("Bob", 25, "b@test.com");
            DiffResult<SimpleBean> result = ObjectDiff.compare(a, b);

            assertThat(result.type()).isEqualTo(SimpleBean.class);
        }
    }

    @Nested
    class BuilderValidation {

        @Test
        void negativeMaxDepth_throws() {
            SimpleBean a = new SimpleBean();
            SimpleBean b = new SimpleBean();

            assertThatThrownBy(() -> ObjectDiff.builder(a, b).maxDepth(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void negativeMaxCollectionSize_throws() {
            SimpleBean a = new SimpleBean();
            SimpleBean b = new SimpleBean();

            assertThatThrownBy(() -> ObjectDiff.builder(a, b).maxCollectionSize(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class DiffRecordValidation {

        @Test
        void nullFieldName_throws() {
            assertThatThrownBy(() -> new Diff<>(null, "a", "b", ChangeType.MODIFIED))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullChangeType_throws() {
            assertThatThrownBy(() -> new Diff<>("field", "a", "b", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
