package cloud.opencode.base.string.desensitize.annotation;

import cloud.opencode.base.string.desensitize.strategy.DesensitizeType;
import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * DesensitizeAnnotationsTest Tests
 * DesensitizeAnnotationsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("脱敏注解测试")
class DesensitizeAnnotationsTest {

    @Desensitize(value = DesensitizeType.MOBILE_PHONE, startKeep = 3, endKeep = 4, maskChar = '#')
    private String phone;

    @Desensitize(DesensitizeType.EMAIL)
    private String email;

    @Desensitize(value = DesensitizeType.CUSTOM, customStrategy = "myStrategy")
    private String custom;

    @DesensitizeBean(enabled = true)
    static class EnabledBean {}

    @DesensitizeBean(enabled = false)
    static class DisabledBean {}

    @DesensitizeBean
    static class DefaultBean {}

    @Nested
    @DisplayName("@Desensitize 注解测试")
    class DesensitizeTests {

        @Test
        @DisplayName("获取脱敏类型")
        void testGetValue() throws NoSuchFieldException {
            Desensitize ann = DesensitizeAnnotationsTest.class
                    .getDeclaredField("phone")
                    .getAnnotation(Desensitize.class);
            assertThat(ann.value()).isEqualTo(DesensitizeType.MOBILE_PHONE);
        }

        @Test
        @DisplayName("获取startKeep")
        void testGetStartKeep() throws NoSuchFieldException {
            Desensitize ann = DesensitizeAnnotationsTest.class
                    .getDeclaredField("phone")
                    .getAnnotation(Desensitize.class);
            assertThat(ann.startKeep()).isEqualTo(3);
        }

        @Test
        @DisplayName("获取endKeep")
        void testGetEndKeep() throws NoSuchFieldException {
            Desensitize ann = DesensitizeAnnotationsTest.class
                    .getDeclaredField("phone")
                    .getAnnotation(Desensitize.class);
            assertThat(ann.endKeep()).isEqualTo(4);
        }

        @Test
        @DisplayName("获取maskChar")
        void testGetMaskChar() throws NoSuchFieldException {
            Desensitize ann = DesensitizeAnnotationsTest.class
                    .getDeclaredField("phone")
                    .getAnnotation(Desensitize.class);
            assertThat(ann.maskChar()).isEqualTo('#');
        }

        @Test
        @DisplayName("默认startKeep为0")
        void testDefaultStartKeep() throws NoSuchFieldException {
            Desensitize ann = DesensitizeAnnotationsTest.class
                    .getDeclaredField("email")
                    .getAnnotation(Desensitize.class);
            assertThat(ann.startKeep()).isEqualTo(0);
        }

        @Test
        @DisplayName("默认endKeep为0")
        void testDefaultEndKeep() throws NoSuchFieldException {
            Desensitize ann = DesensitizeAnnotationsTest.class
                    .getDeclaredField("email")
                    .getAnnotation(Desensitize.class);
            assertThat(ann.endKeep()).isEqualTo(0);
        }

        @Test
        @DisplayName("默认maskChar为*")
        void testDefaultMaskChar() throws NoSuchFieldException {
            Desensitize ann = DesensitizeAnnotationsTest.class
                    .getDeclaredField("email")
                    .getAnnotation(Desensitize.class);
            assertThat(ann.maskChar()).isEqualTo('*');
        }

        @Test
        @DisplayName("获取customStrategy")
        void testGetCustomStrategy() throws NoSuchFieldException {
            Desensitize ann = DesensitizeAnnotationsTest.class
                    .getDeclaredField("custom")
                    .getAnnotation(Desensitize.class);
            assertThat(ann.customStrategy()).isEqualTo("myStrategy");
        }

        @Test
        @DisplayName("默认customStrategy为空")
        void testDefaultCustomStrategy() throws NoSuchFieldException {
            Desensitize ann = DesensitizeAnnotationsTest.class
                    .getDeclaredField("email")
                    .getAnnotation(Desensitize.class);
            assertThat(ann.customStrategy()).isEmpty();
        }

        @Test
        @DisplayName("@Desensitize是RUNTIME保留")
        void testRetention() {
            var retention = Desensitize.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("@Desensitize可用于FIELD和METHOD")
        void testTarget() {
            var target = Desensitize.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target.value()).containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD);
        }
    }

    @Nested
    @DisplayName("@DesensitizeBean 注解测试")
    class DesensitizeBeanTests {

        @Test
        @DisplayName("enabled为true")
        void testEnabled() {
            DesensitizeBean ann = EnabledBean.class.getAnnotation(DesensitizeBean.class);
            assertThat(ann.enabled()).isTrue();
        }

        @Test
        @DisplayName("enabled为false")
        void testDisabled() {
            DesensitizeBean ann = DisabledBean.class.getAnnotation(DesensitizeBean.class);
            assertThat(ann.enabled()).isFalse();
        }

        @Test
        @DisplayName("默认enabled为true")
        void testDefaultEnabled() {
            DesensitizeBean ann = DefaultBean.class.getAnnotation(DesensitizeBean.class);
            assertThat(ann.enabled()).isTrue();
        }

        @Test
        @DisplayName("@DesensitizeBean是RUNTIME保留")
        void testRetention() {
            var retention = DesensitizeBean.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("@DesensitizeBean只能用于TYPE")
        void testTarget() {
            var target = DesensitizeBean.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }
    }
}
