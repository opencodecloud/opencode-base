package cloud.opencode.base.pool.policy;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ValidationPolicyTest Tests
 * ValidationPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("ValidationPolicy 测试")
class ValidationPolicyTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("构造函数设置所有字段")
        void testConstructor() {
            ValidationPolicy policy = new ValidationPolicy(true, true, false, true);

            assertThat(policy.testOnBorrow()).isTrue();
            assertThat(policy.testOnReturn()).isTrue();
            assertThat(policy.testOnCreate()).isFalse();
            assertThat(policy.testWhileIdle()).isTrue();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("none创建无验证策略")
        void testNone() {
            ValidationPolicy policy = ValidationPolicy.none();

            assertThat(policy.testOnBorrow()).isFalse();
            assertThat(policy.testOnReturn()).isFalse();
            assertThat(policy.testOnCreate()).isFalse();
            assertThat(policy.testWhileIdle()).isFalse();
        }

        @Test
        @DisplayName("onBorrow创建仅借出验证策略")
        void testOnBorrow() {
            ValidationPolicy policy = ValidationPolicy.onBorrow();

            assertThat(policy.testOnBorrow()).isTrue();
            assertThat(policy.testOnReturn()).isFalse();
            assertThat(policy.testOnCreate()).isFalse();
            assertThat(policy.testWhileIdle()).isFalse();
        }

        @Test
        @DisplayName("recommended创建推荐验证策略")
        void testRecommended() {
            ValidationPolicy policy = ValidationPolicy.recommended();

            assertThat(policy.testOnBorrow()).isTrue();
            assertThat(policy.testOnReturn()).isFalse();
            assertThat(policy.testOnCreate()).isFalse();
            assertThat(policy.testWhileIdle()).isTrue();
        }

        @Test
        @DisplayName("strict创建严格验证策略")
        void testStrict() {
            ValidationPolicy policy = ValidationPolicy.strict();

            assertThat(policy.testOnBorrow()).isTrue();
            assertThat(policy.testOnReturn()).isTrue();
            assertThat(policy.testOnCreate()).isTrue();
            assertThat(policy.testWhileIdle()).isTrue();
        }
    }

    @Nested
    @DisplayName("hasAnyValidation方法测试")
    class HasAnyValidationTests {

        @Test
        @DisplayName("无验证时返回false")
        void testNoValidation() {
            ValidationPolicy policy = ValidationPolicy.none();
            assertThat(policy.hasAnyValidation()).isFalse();
        }

        @Test
        @DisplayName("有testOnBorrow时返回true")
        void testHasTestOnBorrow() {
            ValidationPolicy policy = new ValidationPolicy(true, false, false, false);
            assertThat(policy.hasAnyValidation()).isTrue();
        }

        @Test
        @DisplayName("有testOnReturn时返回true")
        void testHasTestOnReturn() {
            ValidationPolicy policy = new ValidationPolicy(false, true, false, false);
            assertThat(policy.hasAnyValidation()).isTrue();
        }

        @Test
        @DisplayName("有testOnCreate时返回true")
        void testHasTestOnCreate() {
            ValidationPolicy policy = new ValidationPolicy(false, false, true, false);
            assertThat(policy.hasAnyValidation()).isTrue();
        }

        @Test
        @DisplayName("有testWhileIdle时返回true")
        void testHasTestWhileIdle() {
            ValidationPolicy policy = new ValidationPolicy(false, false, false, true);
            assertThat(policy.hasAnyValidation()).isTrue();
        }
    }

    @Nested
    @DisplayName("Record标准方法测试")
    class RecordStandardMethodTests {

        @Test
        @DisplayName("equals比较相同值返回true")
        void testEquals() {
            ValidationPolicy policy1 = new ValidationPolicy(true, false, true, false);
            ValidationPolicy policy2 = new ValidationPolicy(true, false, true, false);

            assertThat(policy1).isEqualTo(policy2);
        }

        @Test
        @DisplayName("hashCode相同值返回相同结果")
        void testHashCode() {
            ValidationPolicy policy1 = new ValidationPolicy(true, false, true, false);
            ValidationPolicy policy2 = new ValidationPolicy(true, false, true, false);

            assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
        }

        @Test
        @DisplayName("toString返回字符串表示")
        void testToString() {
            ValidationPolicy policy = new ValidationPolicy(true, true, true, true);

            assertThat(policy.toString()).contains("ValidationPolicy");
        }
    }
}
