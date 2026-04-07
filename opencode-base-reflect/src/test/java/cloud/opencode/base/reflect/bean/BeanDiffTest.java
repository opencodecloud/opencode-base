package cloud.opencode.base.reflect.bean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BeanDiff")
class BeanDiffTest {

    @SuppressWarnings("unused")
    static class TestBean {
        String name;
        int age;
        String email;

        TestBean(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Ignore {}

    @SuppressWarnings("unused")
    static class AnnotatedBean {
        String name;
        @Ignore
        String secret;

        AnnotatedBean(String name, String secret) {
            this.name = name;
            this.secret = secret;
        }
    }

    @SuppressWarnings("unused")
    static class Parent {
        String parentField;
        Parent(String parentField) { this.parentField = parentField; }
    }

    @SuppressWarnings("unused")
    static class Child extends Parent {
        String childField;
        Child(String parentField, String childField) {
            super(parentField);
            this.childField = childField;
        }
    }

    @Nested
    @DisplayName("diff(before, after)")
    class DiffBasic {

        @Test
        @DisplayName("should detect changed fields")
        void shouldDetectChanges() {
            TestBean before = new TestBean("Alice", 25, "a@test.com");
            TestBean after = new TestBean("Bob", 25, "b@test.com");

            BeanDiff.Result result = BeanDiff.diff(before, after);
            assertThat(result.hasChanges()).isTrue();
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.get("name").before()).isEqualTo("Alice");
            assertThat(result.get("name").after()).isEqualTo("Bob");
            assertThat(result.get("email").before()).isEqualTo("a@test.com");
            assertThat(result.get("email").after()).isEqualTo("b@test.com");
        }

        @Test
        @DisplayName("should return no changes for identical beans")
        void shouldReturnNoChangesForIdentical() {
            TestBean before = new TestBean("Alice", 25, "a@test.com");
            TestBean after = new TestBean("Alice", 25, "a@test.com");

            BeanDiff.Result result = BeanDiff.diff(before, after);
            assertThat(result.hasChanges()).isFalse();
            assertThat(result.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return empty result for both null")
        void shouldReturnEmptyForBothNull() {
            BeanDiff.Result result = BeanDiff.diff(null, null);
            assertThat(result.hasChanges()).isFalse();
        }

        @Test
        @DisplayName("should throw for only before null")
        void shouldThrowForOnlyBeforeNull() {
            assertThatThrownBy(() -> BeanDiff.diff(null, new TestBean("a", 1, "e")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("non-null");
        }

        @Test
        @DisplayName("should throw for only after null")
        void shouldThrowForOnlyAfterNull() {
            assertThatThrownBy(() -> BeanDiff.diff(new TestBean("a", 1, "e"), null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for type mismatch")
        void shouldThrowForTypeMismatch() {
            assertThatThrownBy(() -> BeanDiff.diff("string", 42))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Type mismatch");
        }
    }

    @Nested
    @DisplayName("diff with annotation exclusion")
    class DiffWithAnnotation {

        @Test
        @DisplayName("should exclude fields with given annotation")
        void shouldExcludeAnnotatedFields() {
            AnnotatedBean before = new AnnotatedBean("Alice", "secret1");
            AnnotatedBean after = new AnnotatedBean("Bob", "secret2");

            BeanDiff.Result result = BeanDiff.diff(before, after, Ignore.class);
            assertThat(result.hasChanges()).isTrue();
            assertThat(result.get("name")).isNotNull();
            assertThat(result.get("secret")).isNull();
        }
    }

    @Nested
    @DisplayName("diff with predicate filter")
    class DiffWithPredicate {

        @Test
        @DisplayName("should filter fields using predicate")
        void shouldFilterWithPredicate() {
            TestBean before = new TestBean("Alice", 25, "a@test.com");
            TestBean after = new TestBean("Bob", 30, "b@test.com");

            BeanDiff.Result result = BeanDiff.diff(before, after, f -> f.getName().equals("name"));
            assertThat(result.size()).isEqualTo(1);
            assertThat(result.get("name")).isNotNull();
        }
    }

    @Nested
    @DisplayName("inheritance support")
    class InheritanceSupport {

        @Test
        @DisplayName("should detect changes in parent class fields")
        void shouldDetectParentChanges() {
            Child before = new Child("parent1", "child1");
            Child after = new Child("parent2", "child1");

            BeanDiff.Result result = BeanDiff.diff(before, after);
            assertThat(result.hasChanges()).isTrue();
            assertThat(result.get("parentField")).isNotNull();
            assertThat(result.get("parentField").before()).isEqualTo("parent1");
            assertThat(result.get("parentField").after()).isEqualTo("parent2");
        }
    }

    @Nested
    @DisplayName("Change record")
    class ChangeRecord {

        @Test
        @DisplayName("wasNull should return true when before is null and after is not")
        void wasNullTest() {
            BeanDiff.Change change = new BeanDiff.Change("field", null, "value");
            assertThat(change.wasNull()).isTrue();
            assertThat(change.isNowNull()).isFalse();
        }

        @Test
        @DisplayName("isNowNull should return true when before is not null and after is null")
        void isNowNullTest() {
            BeanDiff.Change change = new BeanDiff.Change("field", "value", null);
            assertThat(change.wasNull()).isFalse();
            assertThat(change.isNowNull()).isTrue();
        }

        @Test
        @DisplayName("toString should contain field name and values")
        void toStringTest() {
            BeanDiff.Change change = new BeanDiff.Change("name", "old", "new");
            assertThat(change.toString()).isEqualTo("name: old -> new");
        }

        @Test
        @DisplayName("wasNull and isNowNull both false when neither is null")
        void bothNonNull() {
            BeanDiff.Change change = new BeanDiff.Change("f", "a", "b");
            assertThat(change.wasNull()).isFalse();
            assertThat(change.isNowNull()).isFalse();
        }

        @Test
        @DisplayName("wasNull and isNowNull both false when both are null")
        void bothNull() {
            BeanDiff.Change change = new BeanDiff.Change("f", null, null);
            assertThat(change.wasNull()).isFalse();
            assertThat(change.isNowNull()).isFalse();
        }
    }

    @Nested
    @DisplayName("Result record")
    class ResultRecord {

        @Test
        @DisplayName("toString should show no changes for empty result")
        void toStringNoChanges() {
            BeanDiff.Result result = BeanDiff.diff(null, null);
            assertThat(result.toString()).contains("no changes");
        }

        @Test
        @DisplayName("toString should show changes")
        void toStringWithChanges() {
            TestBean before = new TestBean("Alice", 25, "a@test.com");
            TestBean after = new TestBean("Bob", 25, "a@test.com");

            BeanDiff.Result result = BeanDiff.diff(before, after);
            assertThat(result.toString()).contains("name: Alice -> Bob");
        }
    }
}
