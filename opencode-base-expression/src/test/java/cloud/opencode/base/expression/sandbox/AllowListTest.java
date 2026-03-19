package cloud.opencode.base.expression.sandbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * AllowList Tests
 * AllowList 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("AllowList Tests | AllowList 测试")
class AllowListTest {

    @Nested
    @DisplayName("Class Allow Tests | 类允许测试")
    class ClassAllowTests {

        @Test
        @DisplayName("Allow exact class | 允许精确类")
        void testAllowExactClass() {
            AllowList list = AllowList.builder()
                    .allowClass("java.lang.String")
                    .build();

            assertThat(list.isClassAllowed("java.lang.String")).isTrue();
            assertThat(list.isClassAllowed("java.lang.Integer")).isFalse();
        }

        @Test
        @DisplayName("Allow class by Class object | 通过 Class 对象允许")
        void testAllowClassByObject() {
            AllowList list = AllowList.builder()
                    .allowClass(String.class)
                    .build();

            assertThat(list.isClassAllowed(String.class)).isTrue();
        }

        @Test
        @DisplayName("Allow wildcard pattern | 允许通配符模式")
        void testAllowWildcardPattern() {
            AllowList list = AllowList.builder()
                    .allowClass("java.util.*")
                    .build();

            assertThat(list.isClassAllowed("java.util.List")).isTrue();
            assertThat(list.isClassAllowed("java.util.Map")).isTrue();
            assertThat(list.isClassAllowed("java.lang.String")).isFalse();
        }

        @Test
        @DisplayName("Allow multiple classes | 允许多个类")
        void testAllowMultipleClasses() {
            AllowList list = AllowList.builder()
                    .allowClasses("java.lang.String", "java.lang.Integer")
                    .build();

            assertThat(list.isClassAllowed("java.lang.String")).isTrue();
            assertThat(list.isClassAllowed("java.lang.Integer")).isTrue();
        }
    }

    @Nested
    @DisplayName("Method Allow Tests | 方法允许测试")
    class MethodAllowTests {

        @Test
        @DisplayName("Allow exact method | 允许精确方法")
        void testAllowExactMethod() {
            AllowList list = AllowList.builder()
                    .allowMethod("toString")
                    .build();

            assertThat(list.isMethodAllowed("toString")).isTrue();
            assertThat(list.isMethodAllowed("hashCode")).isFalse();
        }

        @Test
        @DisplayName("Allow wildcard method | 允许通配符方法")
        void testAllowWildcardMethod() {
            AllowList list = AllowList.builder()
                    .allowMethod("get*")
                    .build();

            assertThat(list.isMethodAllowed("getName")).isTrue();
            assertThat(list.isMethodAllowed("getAge")).isTrue();
            assertThat(list.isMethodAllowed("setName")).isFalse();
        }
    }

    @Nested
    @DisplayName("Property Allow Tests | 属性允许测试")
    class PropertyAllowTests {

        @Test
        @DisplayName("Allow exact property | 允许精确属性")
        void testAllowExactProperty() {
            AllowList list = AllowList.builder()
                    .allowProperty("name")
                    .build();

            assertThat(list.isPropertyAllowed("name")).isTrue();
            assertThat(list.isPropertyAllowed("age")).isFalse();
        }

        @Test
        @DisplayName("Allow multiple properties | 允许多个属性")
        void testAllowMultipleProperties() {
            AllowList list = AllowList.builder()
                    .allowProperties("name", "age", "email")
                    .build();

            assertThat(list.isPropertyAllowed("name")).isTrue();
            assertThat(list.isPropertyAllowed("age")).isTrue();
            assertThat(list.isPropertyAllowed("email")).isTrue();
        }
    }

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("Empty list denies all | 空列表拒绝所有")
        void testEmptyListDeniesAll() {
            AllowList list = AllowList.empty();

            assertThat(list.isClassAllowed("java.lang.String")).isFalse();
            assertThat(list.isMethodAllowed("toString")).isFalse();
        }

        @Test
        @DisplayName("Allow all allows everything | allowAll 允许所有")
        void testAllowAllAllowsEverything() {
            AllowList list = AllowList.allowAll();

            assertThat(list.isClassAllowed("java.lang.String")).isTrue();
            assertThat(list.isMethodAllowed("anyMethod")).isTrue();
            assertThat(list.isPropertyAllowed("anyProperty")).isTrue();
        }
    }

    @Nested
    @DisplayName("Null Handling Tests | null 处理测试")
    class NullHandlingTests {

        @Test
        @DisplayName("Null class check | null 类检查")
        void testNullClassCheck() {
            AllowList list = AllowList.allowAll();
            assertThat(list.isClassAllowed((String) null)).isFalse();
            assertThat(list.isClassAllowed((Class<?>) null)).isFalse();
        }

        @Test
        @DisplayName("Null method check | null 方法检查")
        void testNullMethodCheck() {
            AllowList list = AllowList.allowAll();
            assertThat(list.isMethodAllowed(null)).isFalse();
        }

        @Test
        @DisplayName("Null property check | null 属性检查")
        void testNullPropertyCheck() {
            AllowList list = AllowList.allowAll();
            assertThat(list.isPropertyAllowed(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter Tests | 获取器测试")
    class GetterTests {

        @Test
        @DisplayName("Get allowed classes | 获取允许的类")
        void testGetAllowedClasses() {
            AllowList list = AllowList.builder()
                    .allowClass("java.lang.String")
                    .build();

            assertThat(list.getAllowedClasses()).contains("java.lang.String");
        }

        @Test
        @DisplayName("Get allowed methods | 获取允许的方法")
        void testGetAllowedMethods() {
            AllowList list = AllowList.builder()
                    .allowMethod("toString")
                    .build();

            assertThat(list.getAllowedMethods()).contains("toString");
        }
    }
}
