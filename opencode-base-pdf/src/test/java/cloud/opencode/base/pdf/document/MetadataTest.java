package cloud.opencode.base.pdf.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Metadata 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("Metadata 测试")
class MetadataTest {

    @Nested
    @DisplayName("Record 构造测试")
    class RecordConstructorTests {

        @Test
        @DisplayName("完整构造函数")
        void testFullConstructor() {
            Instant created = Instant.now();
            Instant modified = Instant.now();
            List<String> keywords = List.of("pdf", "test");

            Metadata metadata = new Metadata(
                "Title", "Author", "Subject", keywords,
                "Creator", "Producer", created, modified
            );

            assertThat(metadata.title()).isEqualTo("Title");
            assertThat(metadata.author()).isEqualTo("Author");
            assertThat(metadata.subject()).isEqualTo("Subject");
            assertThat(metadata.keywords()).containsExactly("pdf", "test");
            assertThat(metadata.creator()).isEqualTo("Creator");
            assertThat(metadata.producer()).isEqualTo("Producer");
            assertThat(metadata.creationDate()).isEqualTo(created);
            assertThat(metadata.modDate()).isEqualTo(modified);
        }

        @Test
        @DisplayName("允许 null 值")
        void testNullValues() {
            Metadata metadata = new Metadata(null, null, null, List.of(), null, null, null, null);

            assertThat(metadata.title()).isNull();
            assertThat(metadata.author()).isNull();
            assertThat(metadata.subject()).isNull();
            assertThat(metadata.keywords()).isEmpty();
            assertThat(metadata.creator()).isNull();
            assertThat(metadata.producer()).isNull();
            assertThat(metadata.creationDate()).isNull();
            assertThat(metadata.modDate()).isNull();
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("empty 创建空元数据")
        void testEmpty() {
            Metadata metadata = Metadata.empty();

            assertThat(metadata.title()).isNull();
            assertThat(metadata.author()).isNull();
            assertThat(metadata.subject()).isNull();
            assertThat(metadata.keywords()).isEmpty();
            assertThat(metadata.creator()).isNull();
            assertThat(metadata.producer()).isEqualTo(Metadata.DEFAULT_PRODUCER);
            assertThat(metadata.creationDate()).isNull();
            assertThat(metadata.modDate()).isNull();
        }

        @Test
        @DisplayName("ofTitle 创建带标题的元数据")
        void testOfTitle() {
            Metadata metadata = Metadata.ofTitle("My Document");

            assertThat(metadata.title()).isEqualTo("My Document");
            assertThat(metadata.author()).isNull();
            assertThat(metadata.producer()).isEqualTo(Metadata.DEFAULT_PRODUCER);
            assertThat(metadata.creationDate()).isNotNull();
        }

        @Test
        @DisplayName("DEFAULT_PRODUCER 常量")
        void testDefaultProducerConstant() {
            assertThat(Metadata.DEFAULT_PRODUCER).isEqualTo("OpenCode PDF");
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder 创建构建器")
        void testBuilder() {
            Metadata.Builder builder = Metadata.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("构建器设置所有属性")
        void testBuilderAllProperties() {
            Instant created = Instant.parse("2024-01-15T10:00:00Z");
            Instant modified = Instant.parse("2024-01-16T10:00:00Z");

            Metadata metadata = Metadata.builder()
                .title("Test Title")
                .author("Test Author")
                .subject("Test Subject")
                .keywords("keyword1", "keyword2")
                .creator("Test Creator")
                .producer("Test Producer")
                .creationDate(created)
                .modDate(modified)
                .build();

            assertThat(metadata.title()).isEqualTo("Test Title");
            assertThat(metadata.author()).isEqualTo("Test Author");
            assertThat(metadata.subject()).isEqualTo("Test Subject");
            assertThat(metadata.keywords()).containsExactly("keyword1", "keyword2");
            assertThat(metadata.creator()).isEqualTo("Test Creator");
            assertThat(metadata.producer()).isEqualTo("Test Producer");
            assertThat(metadata.creationDate()).isEqualTo(created);
            assertThat(metadata.modDate()).isEqualTo(modified);
        }

        @Test
        @DisplayName("构建器默认值")
        void testBuilderDefaults() {
            Metadata metadata = Metadata.builder().build();

            assertThat(metadata.title()).isNull();
            assertThat(metadata.author()).isNull();
            assertThat(metadata.producer()).isEqualTo(Metadata.DEFAULT_PRODUCER);
            assertThat(metadata.creationDate()).isNotNull(); // 默认设置当前时间
        }

        @Test
        @DisplayName("keywords 使用列表")
        void testKeywordsWithList() {
            List<String> keywords = List.of("a", "b", "c");

            Metadata metadata = Metadata.builder()
                .keywords(keywords)
                .build();

            assertThat(metadata.keywords()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("keywords 使用可变参数")
        void testKeywordsWithVarargs() {
            Metadata metadata = Metadata.builder()
                .keywords("x", "y", "z")
                .build();

            assertThat(metadata.keywords()).containsExactly("x", "y", "z");
        }

        @Test
        @DisplayName("链式调用")
        void testFluentApi() {
            Metadata metadata = Metadata.builder()
                .title("T")
                .author("A")
                .subject("S")
                .build();

            assertThat(metadata.title()).isEqualTo("T");
            assertThat(metadata.author()).isEqualTo("A");
            assertThat(metadata.subject()).isEqualTo("S");
        }
    }

    @Nested
    @DisplayName("with 方法测试")
    class WithMethodTests {

        @Test
        @DisplayName("withTitle 更新标题")
        void testWithTitle() {
            Metadata original = Metadata.builder()
                .title("Original")
                .author("Author")
                .build();

            Metadata updated = original.withTitle("Updated");

            assertThat(updated.title()).isEqualTo("Updated");
            assertThat(updated.author()).isEqualTo("Author"); // 保持不变
            assertThat(original.title()).isEqualTo("Original"); // 原对象不变
        }

        @Test
        @DisplayName("withAuthor 更新作者")
        void testWithAuthor() {
            Metadata original = Metadata.builder()
                .title("Title")
                .author("Original Author")
                .build();

            Metadata updated = original.withAuthor("New Author");

            assertThat(updated.author()).isEqualTo("New Author");
            assertThat(updated.title()).isEqualTo("Title"); // 保持不变
            assertThat(original.author()).isEqualTo("Original Author"); // 原对象不变
        }

        @Test
        @DisplayName("withTitle 设置为 null")
        void testWithTitleNull() {
            Metadata original = Metadata.ofTitle("Title");
            Metadata updated = original.withTitle(null);

            assertThat(updated.title()).isNull();
        }
    }

    @Nested
    @DisplayName("Record 特性测试")
    class RecordFeatureTests {

        @Test
        @DisplayName("equals 方法")
        void testEquals() {
            Instant time = Instant.now();
            Metadata m1 = new Metadata("T", "A", "S", List.of(), "C", "P", time, null);
            Metadata m2 = new Metadata("T", "A", "S", List.of(), "C", "P", time, null);

            assertThat(m1).isEqualTo(m2);
        }

        @Test
        @DisplayName("hashCode 方法")
        void testHashCode() {
            Instant time = Instant.now();
            Metadata m1 = new Metadata("T", "A", "S", List.of(), "C", "P", time, null);
            Metadata m2 = new Metadata("T", "A", "S", List.of(), "C", "P", time, null);

            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("toString 方法")
        void testToString() {
            Metadata metadata = Metadata.builder().title("Test").build();

            assertThat(metadata.toString()).contains("Metadata");
            assertThat(metadata.toString()).contains("Test");
        }

        @Test
        @DisplayName("不同对象不相等")
        void testNotEquals() {
            Metadata m1 = Metadata.builder().title("A").build();
            Metadata m2 = Metadata.builder().title("B").build();

            assertThat(m1).isNotEqualTo(m2);
        }
    }

    @Nested
    @DisplayName("不可变性测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("keywords 列表是不可变的")
        void testKeywordsImmutable() {
            Metadata metadata = Metadata.builder()
                .keywords("a", "b")
                .build();

            List<String> keywords = metadata.keywords();

            assertThatThrownBy(() -> keywords.add("c"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
