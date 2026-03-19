package cloud.opencode.base.config;

import cloud.opencode.base.config.bind.ConfigProperties;
import cloud.opencode.base.config.bind.NestedConfig;
import cloud.opencode.base.config.jdk25.DefaultValue;
import cloud.opencode.base.config.jdk25.Required;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for configuration annotations: ConfigProperties, NestedConfig, DefaultValue, Required.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("Config Annotations Tests")
class ConfigAnnotationsTest {

    @Nested
    @DisplayName("ConfigProperties Annotation Tests")
    class ConfigPropertiesTests {

        @Test
        @DisplayName("ConfigProperties has RUNTIME retention")
        void testRetention() {
            Retention retention = ConfigProperties.class.getAnnotation(Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("ConfigProperties targets TYPE")
        void testTarget() {
            Target target = ConfigProperties.class.getAnnotation(Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }

        @Test
        @DisplayName("ConfigProperties prefix is readable at runtime")
        void testPrefixReadable() {
            ConfigProperties annotation = AnnotatedSample.class.getAnnotation(ConfigProperties.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.prefix()).isEqualTo("database");
        }
    }

    @Nested
    @DisplayName("NestedConfig Annotation Tests")
    class NestedConfigTests {

        @Test
        @DisplayName("NestedConfig has RUNTIME retention")
        void testRetention() {
            Retention retention = NestedConfig.class.getAnnotation(Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("NestedConfig targets FIELD")
        void testTarget() {
            Target target = NestedConfig.class.getAnnotation(Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }

        @Test
        @DisplayName("NestedConfig default prefix is empty string")
        void testDefaultPrefix() throws NoSuchFieldException {
            NestedConfig annotation = AnnotatedSample.class
                    .getDeclaredField("nested")
                    .getAnnotation(NestedConfig.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.prefix()).isEmpty();
        }

        @Test
        @DisplayName("NestedConfig custom prefix is readable")
        void testCustomPrefix() throws NoSuchFieldException {
            NestedConfig annotation = AnnotatedSample.class
                    .getDeclaredField("customNested")
                    .getAnnotation(NestedConfig.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.prefix()).isEqualTo("custom");
        }
    }

    @Nested
    @DisplayName("DefaultValue Annotation Tests")
    class DefaultValueTests {

        @Test
        @DisplayName("DefaultValue has RUNTIME retention")
        void testRetention() {
            Retention retention = DefaultValue.class.getAnnotation(Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("DefaultValue targets RECORD_COMPONENT")
        void testTarget() {
            Target target = DefaultValue.class.getAnnotation(Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.RECORD_COMPONENT);
        }

        @Test
        @DisplayName("DefaultValue is readable from record component")
        void testReadableFromRecord() {
            var components = SampleRecord.class.getRecordComponents();
            assertThat(components).isNotEmpty();
            DefaultValue dv = components[0].getAnnotation(DefaultValue.class);
            assertThat(dv).isNotNull();
            assertThat(dv.value()).isEqualTo("8080");
        }
    }

    @Nested
    @DisplayName("Required Annotation Tests")
    class RequiredTests {

        @Test
        @DisplayName("Required has RUNTIME retention")
        void testRetention() {
            Retention retention = Required.class.getAnnotation(Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Required targets RECORD_COMPONENT")
        void testTarget() {
            Target target = Required.class.getAnnotation(Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.RECORD_COMPONENT);
        }

        @Test
        @DisplayName("Required is readable from record component")
        void testReadableFromRecord() {
            var components = SampleRecord.class.getRecordComponents();
            assertThat(components.length).isGreaterThanOrEqualTo(2);
            Required req = components[1].getAnnotation(Required.class);
            assertThat(req).isNotNull();
        }
    }

    // Test helpers
    @ConfigProperties(prefix = "database")
    static class AnnotatedSample {
        @NestedConfig
        Object nested;

        @NestedConfig(prefix = "custom")
        Object customNested;
    }

    record SampleRecord(@DefaultValue("8080") int port, @Required String host) {}
}
