package cloud.opencode.base.deepclone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

/**
 * FieldFilter 函数式接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("FieldFilter 测试")
class FieldFilterTest {

    // ==================== Test Fixtures | 测试数据 ====================

    @SuppressWarnings("deprecation")
    static class TestObj {
        String name = "test";
        String password = "secret";
        int id = 1;
        @Deprecated String old = "old";
        InputStream stream = null;
    }

    private Field field(String name) {
        try {
            return TestObj.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== excludeNames() 测试 ====================

    @Nested
    @DisplayName("excludeNames() 测试")
    class ExcludeNamesTests {

        @Test
        @DisplayName("应排除指定名称的字段")
        void shouldExcludeNamedFields() {
            FieldFilter filter = FieldFilter.excludeNames("password", "old");

            assertThat(filter.accept(field("name"))).isTrue();
            assertThat(filter.accept(field("id"))).isTrue();
            assertThat(filter.accept(field("password"))).isFalse();
            assertThat(filter.accept(field("old"))).isFalse();
        }

        @Test
        @DisplayName("不排除任何字段时全部通过")
        void shouldAcceptAllWhenNoExclusions() {
            FieldFilter filter = FieldFilter.excludeNames();

            assertThat(filter.accept(field("name"))).isTrue();
            assertThat(filter.accept(field("password"))).isTrue();
        }
    }

    // ==================== excludeTypes() 测试 ====================

    @Nested
    @DisplayName("excludeTypes() 测试")
    class ExcludeTypesTests {

        @Test
        @DisplayName("应排除指定类型的字段")
        void shouldExcludeTypedFields() {
            FieldFilter filter = FieldFilter.excludeTypes(InputStream.class);

            assertThat(filter.accept(field("name"))).isTrue();
            assertThat(filter.accept(field("id"))).isTrue();
            assertThat(filter.accept(field("stream"))).isFalse();
        }

        @Test
        @DisplayName("排除String类型")
        void shouldExcludeStringType() {
            FieldFilter filter = FieldFilter.excludeTypes(String.class);

            assertThat(filter.accept(field("name"))).isFalse();
            assertThat(filter.accept(field("password"))).isFalse();
            assertThat(filter.accept(field("id"))).isTrue();
        }
    }

    // ==================== excludeAnnotated() 测试 ====================

    @Nested
    @DisplayName("excludeAnnotated() 测试")
    class ExcludeAnnotatedTests {

        @Test
        @DisplayName("应排除带有@Deprecated注解的字段")
        @SuppressWarnings("deprecation")
        void shouldExcludeDeprecatedFields() {
            FieldFilter filter = FieldFilter.excludeAnnotated(Deprecated.class);

            assertThat(filter.accept(field("name"))).isTrue();
            assertThat(filter.accept(field("password"))).isTrue();
            assertThat(filter.accept(field("id"))).isTrue();
            assertThat(filter.accept(field("old"))).isFalse();
        }
    }

    // ==================== includeNames() 测试 ====================

    @Nested
    @DisplayName("includeNames() 测试")
    class IncludeNamesTests {

        @Test
        @DisplayName("应仅包含指定名称的字段")
        void shouldOnlyIncludeNamedFields() {
            FieldFilter filter = FieldFilter.includeNames("name", "id");

            assertThat(filter.accept(field("name"))).isTrue();
            assertThat(filter.accept(field("id"))).isTrue();
            assertThat(filter.accept(field("password"))).isFalse();
            assertThat(filter.accept(field("old"))).isFalse();
            assertThat(filter.accept(field("stream"))).isFalse();
        }
    }

    // ==================== acceptAll() 测试 ====================

    @Nested
    @DisplayName("acceptAll() 测试")
    class AcceptAllTests {

        @Test
        @DisplayName("应接受所有字段")
        void shouldAcceptAllFields() {
            FieldFilter filter = FieldFilter.acceptAll();

            assertThat(filter.accept(field("name"))).isTrue();
            assertThat(filter.accept(field("password"))).isTrue();
            assertThat(filter.accept(field("id"))).isTrue();
            assertThat(filter.accept(field("old"))).isTrue();
            assertThat(filter.accept(field("stream"))).isTrue();
        }
    }

    // ==================== and() 组合测试 ====================

    @Nested
    @DisplayName("and() 组合测试")
    class AndCompositionTests {

        @Test
        @DisplayName("AND组合应同时满足两个条件")
        void shouldRequireBothConditions() {
            FieldFilter excludePassword = FieldFilter.excludeNames("password");
            FieldFilter excludeStream = FieldFilter.excludeTypes(InputStream.class);
            FieldFilter combined = excludePassword.and(excludeStream);

            assertThat(combined.accept(field("name"))).isTrue();
            assertThat(combined.accept(field("id"))).isTrue();
            assertThat(combined.accept(field("password"))).isFalse();
            assertThat(combined.accept(field("stream"))).isFalse();
        }

        @Test
        @DisplayName("AND组合两个条件都不满足时拒绝")
        void shouldRejectWhenEitherFails() {
            FieldFilter includeNameOnly = FieldFilter.includeNames("name");
            FieldFilter includeIdOnly = FieldFilter.includeNames("id");
            FieldFilter combined = includeNameOnly.and(includeIdOnly);

            // "name" passes first but fails second; "id" passes second but fails first
            assertThat(combined.accept(field("name"))).isFalse();
            assertThat(combined.accept(field("id"))).isFalse();
        }
    }

    // ==================== or() 组合测试 ====================

    @Nested
    @DisplayName("or() 组合测试")
    class OrCompositionTests {

        @Test
        @DisplayName("OR组合应满足任一条件")
        void shouldAcceptEitherCondition() {
            FieldFilter includeName = FieldFilter.includeNames("name");
            FieldFilter includeId = FieldFilter.includeNames("id");
            FieldFilter combined = includeName.or(includeId);

            assertThat(combined.accept(field("name"))).isTrue();
            assertThat(combined.accept(field("id"))).isTrue();
            assertThat(combined.accept(field("password"))).isFalse();
        }
    }

    // ==================== negate() 测试 ====================

    @Nested
    @DisplayName("negate() 测试")
    class NegateTests {

        @Test
        @DisplayName("negate应反转过滤结果")
        void shouldNegateFilter() {
            FieldFilter includeNameOnly = FieldFilter.includeNames("name");
            FieldFilter negated = includeNameOnly.negate();

            assertThat(negated.accept(field("name"))).isFalse();
            assertThat(negated.accept(field("password"))).isTrue();
            assertThat(negated.accept(field("id"))).isTrue();
        }

        @Test
        @DisplayName("acceptAll的negate应拒绝所有")
        void negateAcceptAllShouldRejectAll() {
            FieldFilter negated = FieldFilter.acceptAll().negate();

            assertThat(negated.accept(field("name"))).isFalse();
            assertThat(negated.accept(field("password"))).isFalse();
            assertThat(negated.accept(field("id"))).isFalse();
        }
    }
}
