package cloud.opencode.base.serialization.filter;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ClassFilterTest Tests
 * ClassFilterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
@DisplayName("ClassFilter Tests")
class ClassFilterTest {

    @Nested
    @DisplayName("allowAll() Tests")
    class AllowAllTests {

        @Test
        @DisplayName("allowAll should accept any class")
        void allowAllShouldAcceptAnyClass() {
            ClassFilter filter = ClassFilter.allowAll();

            assertThat(filter.isAllowed(String.class.getName())).isTrue();
            assertThat(filter.isAllowed(Integer.class.getName())).isTrue();
            assertThat(filter.isAllowed(Object.class.getName())).isTrue();
        }

        @Test
        @DisplayName("allowAll should accept custom classes")
        void allowAllShouldAcceptCustomClasses() {
            ClassFilter filter = ClassFilter.allowAll();

            assertThat(filter.isAllowed(ClassFilterTest.class.getName())).isTrue();
        }

        @Test
        @DisplayName("allowAll should accept array types")
        void allowAllShouldAcceptArrayTypes() {
            ClassFilter filter = ClassFilter.allowAll();

            assertThat(filter.isAllowed(String[].class.getName())).isTrue();
            assertThat(filter.isAllowed(int[].class.getName())).isTrue();
        }
    }

    @Nested
    @DisplayName("denyAll() Tests")
    class DenyAllTests {

        @Test
        @DisplayName("denyAll should reject any class")
        void denyAllShouldRejectAnyClass() {
            ClassFilter filter = ClassFilter.denyAll();

            assertThat(filter.isAllowed(String.class.getName())).isFalse();
            assertThat(filter.isAllowed(Integer.class.getName())).isFalse();
            assertThat(filter.isAllowed(Object.class.getName())).isFalse();
        }

        @Test
        @DisplayName("denyAll should reject custom classes")
        void denyAllShouldRejectCustomClasses() {
            ClassFilter filter = ClassFilter.denyAll();

            assertThat(filter.isAllowed(ClassFilterTest.class.getName())).isFalse();
        }
    }

    @Nested
    @DisplayName("and() Composition Tests")
    class AndTests {

        @Test
        @DisplayName("and with allowAll and allowAll should allow")
        void andWithAllowAllAndAllowAllShouldAllow() {
            ClassFilter combined = ClassFilter.allowAll().and(ClassFilter.allowAll());

            assertThat(combined.isAllowed(String.class.getName())).isTrue();
        }

        @Test
        @DisplayName("and with allowAll and denyAll should deny")
        void andWithAllowAllAndDenyAllShouldDeny() {
            ClassFilter combined = ClassFilter.allowAll().and(ClassFilter.denyAll());

            assertThat(combined.isAllowed(String.class.getName())).isFalse();
        }

        @Test
        @DisplayName("and with denyAll and allowAll should deny")
        void andWithDenyAllAndAllowAllShouldDeny() {
            ClassFilter combined = ClassFilter.denyAll().and(ClassFilter.allowAll());

            assertThat(combined.isAllowed(String.class.getName())).isFalse();
        }

        @Test
        @DisplayName("and with denyAll and denyAll should deny")
        void andWithDenyAllAndDenyAllShouldDeny() {
            ClassFilter combined = ClassFilter.denyAll().and(ClassFilter.denyAll());

            assertThat(combined.isAllowed(String.class.getName())).isFalse();
        }

        @Test
        @DisplayName("and should chain multiple filters")
        void andShouldChainMultipleFilters() {
            ClassFilter combined = ClassFilter.allowAll()
                    .and(ClassFilter.allowAll())
                    .and(ClassFilter.allowAll());

            assertThat(combined.isAllowed(String.class.getName())).isTrue();
        }
    }

    @Nested
    @DisplayName("or() Composition Tests")
    class OrTests {

        @Test
        @DisplayName("or with allowAll and denyAll should allow")
        void orWithAllowAllAndDenyAllShouldAllow() {
            ClassFilter combined = ClassFilter.allowAll().or(ClassFilter.denyAll());

            assertThat(combined.isAllowed(String.class.getName())).isTrue();
        }

        @Test
        @DisplayName("or with denyAll and allowAll should allow")
        void orWithDenyAllAndAllowAllShouldAllow() {
            ClassFilter combined = ClassFilter.denyAll().or(ClassFilter.allowAll());

            assertThat(combined.isAllowed(String.class.getName())).isTrue();
        }

        @Test
        @DisplayName("or with denyAll and denyAll should deny")
        void orWithDenyAllAndDenyAllShouldDeny() {
            ClassFilter combined = ClassFilter.denyAll().or(ClassFilter.denyAll());

            assertThat(combined.isAllowed(String.class.getName())).isFalse();
        }

        @Test
        @DisplayName("or with allowAll and allowAll should allow")
        void orWithAllowAllAndAllowAllShouldAllow() {
            ClassFilter combined = ClassFilter.allowAll().or(ClassFilter.allowAll());

            assertThat(combined.isAllowed(String.class.getName())).isTrue();
        }

        @Test
        @DisplayName("or should chain multiple filters")
        void orShouldChainMultipleFilters() {
            ClassFilter combined = ClassFilter.denyAll()
                    .or(ClassFilter.denyAll())
                    .or(ClassFilter.allowAll());

            assertThat(combined.isAllowed(String.class.getName())).isTrue();
        }
    }

    @Nested
    @DisplayName("negate() Tests")
    class NegateTests {

        @Test
        @DisplayName("negate of allowAll should deny")
        void negateOfAllowAllShouldDeny() {
            ClassFilter filter = ClassFilter.allowAll().negate();

            assertThat(filter.isAllowed(String.class.getName())).isFalse();
        }

        @Test
        @DisplayName("negate of denyAll should allow")
        void negateOfDenyAllShouldAllow() {
            ClassFilter filter = ClassFilter.denyAll().negate();

            assertThat(filter.isAllowed(String.class.getName())).isTrue();
        }

        @Test
        @DisplayName("double negate should return to original behavior")
        void doubleNegateShouldReturnToOriginalBehavior() {
            ClassFilter filter = ClassFilter.allowAll().negate().negate();

            assertThat(filter.isAllowed(String.class.getName())).isTrue();
        }

        @Test
        @DisplayName("negate combined with and should work correctly")
        void negateCombinedWithAndShouldWorkCorrectly() {
            // allowAll AND (NOT denyAll) = allowAll AND allowAll = allow
            ClassFilter combined = ClassFilter.allowAll().and(ClassFilter.denyAll().negate());

            assertThat(combined.isAllowed(String.class.getName())).isTrue();
        }

        @Test
        @DisplayName("negate combined with or should work correctly")
        void negateCombinedWithOrShouldWorkCorrectly() {
            // denyAll OR (NOT allowAll) = denyAll OR denyAll = deny
            ClassFilter combined = ClassFilter.denyAll().or(ClassFilter.allowAll().negate());

            assertThat(combined.isAllowed(String.class.getName())).isFalse();
        }
    }
}
