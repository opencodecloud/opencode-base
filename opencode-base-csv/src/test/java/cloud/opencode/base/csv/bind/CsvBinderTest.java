package cloud.opencode.base.csv.bind;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.bind.annotation.CsvColumn;
import cloud.opencode.base.csv.bind.annotation.CsvFormat;
import cloud.opencode.base.csv.exception.CsvBindException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for CsvBinder
 * CsvBinder测试
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-csv V1.0.3
 */
@DisplayName("CsvBinder - CSV对象绑定器")
class CsvBinderTest {

    // ==================== Test Types | 测试类型 ====================

    record PersonRecord(String name, int age, String email) {
    }

    record AnnotatedRecord(
            @CsvColumn("Full Name") String name,
            @CsvColumn(index = 2) int score,
            @CsvFormat(pattern = "yyyy/MM/dd") LocalDate birthDate
    ) {
    }

    record RequiredRecord(
            @CsvColumn(required = true) String name,
            int age
    ) {
    }

    enum Color { RED, GREEN, BLUE }

    record TypedRecord(
            String name,
            int intVal,
            long longVal,
            double doubleVal,
            boolean boolVal,
            BigDecimal decimalVal,
            Color color
    ) {
    }

    static class Person {
        private String name;
        private int age;
        private String email;

        public Person() {
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    static class AnnotatedPerson {
        @CsvColumn("Full Name")
        private String name;

        @CsvColumn(index = 1)
        private int age;

        @CsvFormat(pattern = "yyyy/MM/dd")
        private LocalDate birthDate;

        public AnnotatedPerson() {
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    }

    // ==================== Bind to Record | 绑定到Record ====================

    @Nested
    @DisplayName("绑定到Record类型")
    class BindToRecord {

        @Test
        @DisplayName("基本Record绑定")
        void bindBasicRecord() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age", "email")
                    .addRow("Alice", "30", "alice@test.com")
                    .addRow("Bob", "25", "bob@test.com")
                    .build();

            List<PersonRecord> result = CsvBinder.bind(doc, PersonRecord.class);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("Alice");
            assertThat(result.get(0).age()).isEqualTo(30);
            assertThat(result.get(0).email()).isEqualTo("alice@test.com");
            assertThat(result.get(1).name()).isEqualTo("Bob");
            assertThat(result.get(1).age()).isEqualTo(25);
        }

        @Test
        @DisplayName("Record使用@CsvColumn注解")
        void bindRecordWithAnnotation() {
            CsvDocument doc = CsvDocument.builder()
                    .header("Full Name", "unused", "score", "birthDate")
                    .addRow("Alice", "x", "95", "2000/01/15")
                    .build();

            List<AnnotatedRecord> result = CsvBinder.bind(doc, AnnotatedRecord.class);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Alice");
            assertThat(result.get(0).score()).isEqualTo(95);
            assertThat(result.get(0).birthDate()).isEqualTo(LocalDate.of(2000, 1, 15));
        }

        @Test
        @DisplayName("Record必填字段验证")
        void bindRecordRequiredField() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("", "30")
                    .build();

            assertThatThrownBy(() -> CsvBinder.bind(doc, RequiredRecord.class))
                    .isInstanceOf(CsvBindException.class)
                    .hasMessageContaining("Required field")
                    .hasMessageContaining("name");
        }
    }

    // ==================== Bind to POJO | 绑定到POJO ====================

    @Nested
    @DisplayName("绑定到POJO类型")
    class BindToPojo {

        @Test
        @DisplayName("基本POJO绑定")
        void bindBasicPojo() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age", "email")
                    .addRow("Alice", "30", "alice@test.com")
                    .addRow("Bob", "25", "bob@test.com")
                    .build();

            List<Person> result = CsvBinder.bind(doc, Person.class);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Alice");
            assertThat(result.get(0).getAge()).isEqualTo(30);
            assertThat(result.get(0).getEmail()).isEqualTo("alice@test.com");
            assertThat(result.get(1).getName()).isEqualTo("Bob");
            assertThat(result.get(1).getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("POJO使用@CsvColumn注解")
        void bindPojoWithAnnotation() {
            CsvDocument doc = CsvDocument.builder()
                    .header("Full Name", "age", "birthDate")
                    .addRow("Alice", "30", "2000/01/15")
                    .build();

            List<AnnotatedPerson> result = CsvBinder.bind(doc, AnnotatedPerson.class);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Alice");
            assertThat(result.get(0).getAge()).isEqualTo(30);
            assertThat(result.get(0).getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 15));
        }
    }

    // ==================== Type Conversion | 类型转换 ====================

    @Nested
    @DisplayName("类型转换")
    class TypeConversion {

        @Test
        @DisplayName("多种类型自动转换")
        void convertMultipleTypes() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "intVal", "longVal", "doubleVal", "boolVal", "decimalVal", "color")
                    .addRow("test", "42", "123456789", "3.14", "true", "99.99", "GREEN")
                    .build();

            List<TypedRecord> result = CsvBinder.bind(doc, TypedRecord.class);

            assertThat(result).hasSize(1);
            TypedRecord r = result.get(0);
            assertThat(r.name()).isEqualTo("test");
            assertThat(r.intVal()).isEqualTo(42);
            assertThat(r.longVal()).isEqualTo(123456789L);
            assertThat(r.doubleVal()).isCloseTo(3.14, org.assertj.core.data.Offset.offset(0.001));
            assertThat(r.boolVal()).isTrue();
            assertThat(r.decimalVal()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(r.color()).isEqualTo(Color.GREEN);
        }

        @Test
        @DisplayName("枚举类型忽略大小写")
        void convertEnumCaseInsensitive() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "intVal", "longVal", "doubleVal", "boolVal", "decimalVal", "color")
                    .addRow("test", "1", "1", "1.0", "false", "1", "blue")
                    .build();

            List<TypedRecord> result = CsvBinder.bind(doc, TypedRecord.class);

            assertThat(result.get(0).color()).isEqualTo(Color.BLUE);
        }

        @Test
        @DisplayName("日期格式化使用@CsvFormat")
        void convertDateWithFormat() {
            CsvDocument doc = CsvDocument.builder()
                    .header("Full Name", "unused", "score", "birthDate")
                    .addRow("Test", "x", "100", "1990/06/15")
                    .build();

            List<AnnotatedRecord> result = CsvBinder.bind(doc, AnnotatedRecord.class);

            assertThat(result.get(0).birthDate()).isEqualTo(LocalDate.of(1990, 6, 15));
        }
    }

    // ==================== fromObjects | 对象转CSV ====================

    @Nested
    @DisplayName("对象转CSV文档")
    class FromObjects {

        @Test
        @DisplayName("Record转CSV文档")
        void fromRecords() {
            List<PersonRecord> records = List.of(
                    new PersonRecord("Alice", 30, "alice@test.com"),
                    new PersonRecord("Bob", 25, "bob@test.com")
            );

            CsvDocument doc = CsvBinder.fromObjects(records, PersonRecord.class);

            assertThat(doc.headers()).containsExactly("name", "age", "email");
            assertThat(doc.rowCount()).isEqualTo(2);
            assertThat(doc.getRow(0).get(0)).isEqualTo("Alice");
            assertThat(doc.getRow(0).get(1)).isEqualTo("30");
            assertThat(doc.getRow(1).get(0)).isEqualTo("Bob");
        }

        @Test
        @DisplayName("POJO转CSV文档")
        void fromPojos() {
            Person p1 = new Person();
            p1.setName("Alice");
            p1.setAge(30);
            p1.setEmail("alice@test.com");

            Person p2 = new Person();
            p2.setName("Bob");
            p2.setAge(25);
            p2.setEmail("bob@test.com");

            CsvDocument doc = CsvBinder.fromObjects(List.of(p1, p2), Person.class);

            assertThat(doc.headers()).containsExactly("name", "age", "email");
            assertThat(doc.rowCount()).isEqualTo(2);
            assertThat(doc.getRow(0).get(0)).isEqualTo("Alice");
            assertThat(doc.getRow(0).get(1)).isEqualTo("30");
        }

        @Test
        @DisplayName("使用@CsvColumn注解的Record转CSV")
        void fromAnnotatedRecords() {
            List<AnnotatedRecord> records = List.of(
                    new AnnotatedRecord("Alice", 95, LocalDate.of(2000, 1, 15))
            );

            CsvDocument doc = CsvBinder.fromObjects(records, AnnotatedRecord.class);

            assertThat(doc.headers()).containsExactly("Full Name", "score", "birthDate");
            assertThat(doc.getRow(0).get(0)).isEqualTo("Alice");
            assertThat(doc.getRow(0).get(2)).isEqualTo("2000/01/15");
        }
    }

    // ==================== Edge Cases | 边界情况 ====================

    @Nested
    @DisplayName("边界情况")
    class EdgeCases {

        @Test
        @DisplayName("空文档绑定返回空列表")
        void bindEmptyDocument() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age", "email")
                    .build();

            List<PersonRecord> result = CsvBinder.bind(doc, PersonRecord.class);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("缺少列时使用默认值")
        void missingColumnUsesDefault() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .build();

            List<PersonRecord> result = CsvBinder.bind(doc, PersonRecord.class);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Alice");
            assertThat(result.get(0).age()).isEqualTo(30);
            assertThat(result.get(0).email()).isNull();
        }

        @Test
        @DisplayName("null参数校验")
        void nullParameterValidation() {
            assertThatThrownBy(() -> CsvBinder.bind(null, PersonRecord.class))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvBinder.bind(CsvDocument.builder().header("a").build(), null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvBinder.fromObjects(null, PersonRecord.class))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvBinder.fromObjects(List.of(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("标题大小写不敏感匹配")
        void headerCaseInsensitiveMatch() {
            CsvDocument doc = CsvDocument.builder()
                    .header("NAME", "AGE", "EMAIL")
                    .addRow("Alice", "30", "alice@test.com")
                    .build();

            List<PersonRecord> result = CsvBinder.bind(doc, PersonRecord.class);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Alice");
        }
    }
}
