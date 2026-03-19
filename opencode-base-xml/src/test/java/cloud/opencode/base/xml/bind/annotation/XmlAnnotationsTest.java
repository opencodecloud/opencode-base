package cloud.opencode.base.xml.bind.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlAnnotationsTest Tests
 * XmlAnnotationsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XML 绑定注解测试")
class XmlAnnotationsTest {

    @Nested
    @DisplayName("XmlRoot注解测试")
    class XmlRootTests {

        @XmlRoot("user")
        static class UserClass {}

        @XmlRoot(value = "item", namespace = "http://example.com")
        static class ItemClass {}

        @XmlRoot
        static class DefaultClass {}

        @Test
        @DisplayName("value返回指定的元素名称")
        void testValue() {
            XmlRoot annotation = UserClass.class.getAnnotation(XmlRoot.class);
            assertThat(annotation.value()).isEqualTo("user");
        }

        @Test
        @DisplayName("namespace返回指定的命名空间")
        void testNamespace() {
            XmlRoot annotation = ItemClass.class.getAnnotation(XmlRoot.class);
            assertThat(annotation.namespace()).isEqualTo("http://example.com");
        }

        @Test
        @DisplayName("默认值为空字符串")
        void testDefaults() {
            XmlRoot annotation = DefaultClass.class.getAnnotation(XmlRoot.class);
            assertThat(annotation.value()).isEmpty();
            assertThat(annotation.namespace()).isEmpty();
        }

        @Test
        @DisplayName("注解可在运行时获取")
        void testRetention() {
            assertThat(XmlRoot.class.getAnnotation(java.lang.annotation.Retention.class).value())
                    .isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("注解仅适用于类型")
        void testTarget() {
            var target = XmlRoot.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }
    }

    @Nested
    @DisplayName("XmlElement注解测试")
    class XmlElementTests {

        static class TestClass {
            @XmlElement("name")
            String name;

            @XmlElement(value = "desc", cdata = true, required = true)
            String description;

            @XmlElement
            String defaultField;
        }

        @Test
        @DisplayName("value返回指定的元素名称")
        void testValue() throws NoSuchFieldException {
            XmlElement annotation = TestClass.class.getDeclaredField("name").getAnnotation(XmlElement.class);
            assertThat(annotation.value()).isEqualTo("name");
        }

        @Test
        @DisplayName("cdata和required返回正确值")
        void testCdataAndRequired() throws NoSuchFieldException {
            XmlElement annotation = TestClass.class.getDeclaredField("description").getAnnotation(XmlElement.class);
            assertThat(annotation.cdata()).isTrue();
            assertThat(annotation.required()).isTrue();
        }

        @Test
        @DisplayName("默认值正确")
        void testDefaults() throws NoSuchFieldException {
            XmlElement annotation = TestClass.class.getDeclaredField("defaultField").getAnnotation(XmlElement.class);
            assertThat(annotation.value()).isEmpty();
            assertThat(annotation.namespace()).isEmpty();
            assertThat(annotation.required()).isFalse();
            assertThat(annotation.defaultValue()).isEmpty();
            assertThat(annotation.cdata()).isFalse();
        }
    }

    @Nested
    @DisplayName("XmlAttribute注解测试")
    class XmlAttributeTests {

        static class TestClass {
            @XmlAttribute("id")
            Long id;

            @XmlAttribute(required = true)
            String type;
        }

        @Test
        @DisplayName("value返回指定的属性名称")
        void testValue() throws NoSuchFieldException {
            XmlAttribute annotation = TestClass.class.getDeclaredField("id").getAnnotation(XmlAttribute.class);
            assertThat(annotation.value()).isEqualTo("id");
        }

        @Test
        @DisplayName("required返回正确值")
        void testRequired() throws NoSuchFieldException {
            XmlAttribute annotation = TestClass.class.getDeclaredField("type").getAnnotation(XmlAttribute.class);
            assertThat(annotation.required()).isTrue();
        }
    }

    @Nested
    @DisplayName("XmlIgnore注解测试")
    class XmlIgnoreTests {

        static class TestClass {
            @XmlIgnore
            String password;

            String name;
        }

        @Test
        @DisplayName("注解存在于标记的字段上")
        void testAnnotationPresent() throws NoSuchFieldException {
            assertThat(TestClass.class.getDeclaredField("password").isAnnotationPresent(XmlIgnore.class)).isTrue();
            assertThat(TestClass.class.getDeclaredField("name").isAnnotationPresent(XmlIgnore.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("XmlValue注解测试")
    class XmlValueTests {

        static class TestClass {
            @XmlValue
            String content;
        }

        @Test
        @DisplayName("注解存在于标记的字段上")
        void testAnnotationPresent() throws NoSuchFieldException {
            assertThat(TestClass.class.getDeclaredField("content").isAnnotationPresent(XmlValue.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("XmlElementList注解测试")
    class XmlElementListTests {

        static class TestClass {
            @XmlElementList(value = "roles", itemName = "role")
            java.util.List<String> roles;

            @XmlElementList
            java.util.List<String> tags;
        }

        @Test
        @DisplayName("value和itemName返回正确值")
        void testValueAndItemName() throws NoSuchFieldException {
            XmlElementList annotation = TestClass.class.getDeclaredField("roles").getAnnotation(XmlElementList.class);
            assertThat(annotation.value()).isEqualTo("roles");
            assertThat(annotation.itemName()).isEqualTo("role");
        }

        @Test
        @DisplayName("默认值为空字符串")
        void testDefaults() throws NoSuchFieldException {
            XmlElementList annotation = TestClass.class.getDeclaredField("tags").getAnnotation(XmlElementList.class);
            assertThat(annotation.value()).isEmpty();
            assertThat(annotation.itemName()).isEmpty();
        }
    }
}
