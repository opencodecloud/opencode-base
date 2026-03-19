package cloud.opencode.base.yml.bind;

import org.junit.jupiter.api.*;

import java.lang.annotation.*;
import java.lang.reflect.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for YML bind annotations
 * Tests annotation attributes, target types, and retention policies using reflection
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("Bind Annotations Tests")
class BindAnnotationsTest {

    // ==================== Test Classes for Annotation Testing ====================

    /**
     * Test class with YmlAlias on field
     */
    static class YmlAliasFieldTarget {
        @YmlAlias({"db.url", "database.url", "jdbc.url"})
        public String url;

        @YmlAlias({"single.alias"})
        public String singleAlias;

        public String noAnnotation;
    }

    /**
     * Test class with YmlAlias on method
     */
    static class YmlAliasMethodTarget {
        private String value;

        @YmlAlias({"method.alias", "alt.method.alias"})
        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Test class with YmlAlias on parameter
     */
    static class YmlAliasParameterTarget {
        public void process(@YmlAlias({"param.alias"}) String input) {
            // Method for testing parameter annotation
        }
    }

    /**
     * Test class with YmlIgnore on field
     */
    static class YmlIgnoreFieldTarget {
        public String visible;

        @YmlIgnore
        public String ignored;
    }

    /**
     * Test class with YmlIgnore on method
     */
    static class YmlIgnoreMethodTarget {
        private String secret;

        @YmlIgnore
        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    /**
     * Test class with YmlProperty on field
     */
    static class YmlPropertyFieldTarget {
        @YmlProperty("app.name")
        public String name;

        @YmlProperty(value = "app.port", defaultValue = "8080")
        public int port;

        @YmlProperty(value = "app.required", required = true)
        public String requiredField;

        @YmlProperty(value = "app.optional", defaultValue = "default", required = false)
        public String optionalWithDefault;

        public String noAnnotation;
    }

    /**
     * Test class with YmlProperty on method
     */
    static class YmlPropertyMethodTarget {
        private String host;

        @YmlProperty(value = "server.host", defaultValue = "localhost")
        public void setHost(String host) {
            this.host = host;
        }

        public String getHost() {
            return host;
        }
    }

    /**
     * Test class with YmlProperty on parameter
     */
    static class YmlPropertyParameterTarget {
        public void configure(@YmlProperty(value = "config.value", required = true) String value) {
            // Method for testing parameter annotation
        }
    }

    /**
     * Test class with YmlValue on field
     */
    static class YmlValueFieldTarget {
        @YmlValue("server.port")
        public int port;

        @YmlValue(value = "server.host", defaultValue = "localhost")
        public String host;

        public String noAnnotation;
    }

    /**
     * Test class with YmlValue on parameter
     */
    static class YmlValueParameterTarget {
        public void initialize(@YmlValue(value = "init.value", defaultValue = "default") String value) {
            // Method for testing parameter annotation
        }
    }

    /**
     * Test class with YmlNestedProperty on field
     */
    static class YmlNestedPropertyFieldTarget {
        @YmlNestedProperty(prefix = "database")
        public Object databaseConfig;

        @YmlNestedProperty(prefix = "cache.settings")
        public Object cacheConfig;

        @YmlNestedProperty  // Default prefix (empty string)
        public Object defaultPrefixConfig;

        public Object noAnnotation;
    }

    /**
     * Test class combining multiple annotations
     */
    static class MixedAnnotationsTarget {
        @YmlProperty("mixed.name")
        public String name;

        @YmlAlias({"mixed.alias1", "mixed.alias2"})
        public String aliased;

        @YmlIgnore
        public String ignored;

        @YmlValue("mixed.value")
        public String value;

        @YmlNestedProperty(prefix = "mixed.nested")
        public Object nested;
    }

    // ==================== YmlAlias Tests ====================

    @Nested
    @DisplayName("YmlAlias Tests")
    class YmlAliasTests {

        @Test
        @DisplayName("should be present on annotated field")
        void shouldBePresentOnAnnotatedField() throws Exception {
            Field field = YmlAliasFieldTarget.class.getDeclaredField("url");

            assertThat(field.isAnnotationPresent(YmlAlias.class)).isTrue();
        }

        @Test
        @DisplayName("should not be present on non-annotated field")
        void shouldNotBePresentOnNonAnnotatedField() throws Exception {
            Field field = YmlAliasFieldTarget.class.getDeclaredField("noAnnotation");

            assertThat(field.isAnnotationPresent(YmlAlias.class)).isFalse();
        }

        @Test
        @DisplayName("should retrieve value array from annotation")
        void shouldRetrieveValueArrayFromAnnotation() throws Exception {
            Field field = YmlAliasFieldTarget.class.getDeclaredField("url");
            YmlAlias annotation = field.getAnnotation(YmlAlias.class);

            assertThat(annotation.value()).containsExactly("db.url", "database.url", "jdbc.url");
        }

        @Test
        @DisplayName("should retrieve single value from annotation")
        void shouldRetrieveSingleValueFromAnnotation() throws Exception {
            Field field = YmlAliasFieldTarget.class.getDeclaredField("singleAlias");
            YmlAlias annotation = field.getAnnotation(YmlAlias.class);

            assertThat(annotation.value()).containsExactly("single.alias");
        }

        @Test
        @DisplayName("should be present on annotated method")
        void shouldBePresentOnAnnotatedMethod() throws Exception {
            Method method = YmlAliasMethodTarget.class.getDeclaredMethod("setValue", String.class);

            assertThat(method.isAnnotationPresent(YmlAlias.class)).isTrue();
        }

        @Test
        @DisplayName("should retrieve value from method annotation")
        void shouldRetrieveValueFromMethodAnnotation() throws Exception {
            Method method = YmlAliasMethodTarget.class.getDeclaredMethod("setValue", String.class);
            YmlAlias annotation = method.getAnnotation(YmlAlias.class);

            assertThat(annotation.value()).containsExactly("method.alias", "alt.method.alias");
        }

        @Test
        @DisplayName("should be present on annotated parameter")
        void shouldBePresentOnAnnotatedParameter() throws Exception {
            Method method = YmlAliasParameterTarget.class.getDeclaredMethod("process", String.class);
            Parameter parameter = method.getParameters()[0];

            assertThat(parameter.isAnnotationPresent(YmlAlias.class)).isTrue();
        }

        @Test
        @DisplayName("should retrieve value from parameter annotation")
        void shouldRetrieveValueFromParameterAnnotation() throws Exception {
            Method method = YmlAliasParameterTarget.class.getDeclaredMethod("process", String.class);
            Parameter parameter = method.getParameters()[0];
            YmlAlias annotation = parameter.getAnnotation(YmlAlias.class);

            assertThat(annotation.value()).containsExactly("param.alias");
        }

        @Test
        @DisplayName("should have RUNTIME retention policy")
        void shouldHaveRuntimeRetentionPolicy() {
            Retention retention = YmlAlias.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD, METHOD, and PARAMETER")
        void shouldTargetFieldMethodAndParameter() {
            Target target = YmlAlias.class.getAnnotation(Target.class);

            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.FIELD,
                ElementType.METHOD,
                ElementType.PARAMETER
            );
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(YmlAlias.class.isAnnotationPresent(Documented.class)).isTrue();
        }

        @Test
        @DisplayName("should have value method defined")
        void shouldHaveValueMethodDefined() throws Exception {
            Method valueMethod = YmlAlias.class.getDeclaredMethod("value");

            assertThat(valueMethod).isNotNull();
            assertThat(valueMethod.getReturnType()).isEqualTo(String[].class);
        }
    }

    // ==================== YmlIgnore Tests ====================

    @Nested
    @DisplayName("YmlIgnore Tests")
    class YmlIgnoreTests {

        @Test
        @DisplayName("should be present on annotated field")
        void shouldBePresentOnAnnotatedField() throws Exception {
            Field field = YmlIgnoreFieldTarget.class.getDeclaredField("ignored");

            assertThat(field.isAnnotationPresent(YmlIgnore.class)).isTrue();
        }

        @Test
        @DisplayName("should not be present on non-annotated field")
        void shouldNotBePresentOnNonAnnotatedField() throws Exception {
            Field field = YmlIgnoreFieldTarget.class.getDeclaredField("visible");

            assertThat(field.isAnnotationPresent(YmlIgnore.class)).isFalse();
        }

        @Test
        @DisplayName("should be present on annotated method")
        void shouldBePresentOnAnnotatedMethod() throws Exception {
            Method method = YmlIgnoreMethodTarget.class.getDeclaredMethod("getSecret");

            assertThat(method.isAnnotationPresent(YmlIgnore.class)).isTrue();
        }

        @Test
        @DisplayName("should not be present on non-annotated method")
        void shouldNotBePresentOnNonAnnotatedMethod() throws Exception {
            Method method = YmlIgnoreMethodTarget.class.getDeclaredMethod("setSecret", String.class);

            assertThat(method.isAnnotationPresent(YmlIgnore.class)).isFalse();
        }

        @Test
        @DisplayName("should have RUNTIME retention policy")
        void shouldHaveRuntimeRetentionPolicy() {
            Retention retention = YmlIgnore.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD and METHOD")
        void shouldTargetFieldAndMethod() {
            Target target = YmlIgnore.class.getAnnotation(Target.class);

            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.FIELD,
                ElementType.METHOD
            );
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(YmlIgnore.class.isAnnotationPresent(Documented.class)).isTrue();
        }

        @Test
        @DisplayName("should be a marker annotation with no methods")
        void shouldBeMarkerAnnotationWithNoMethods() {
            Method[] methods = YmlIgnore.class.getDeclaredMethods();

            assertThat(methods).isEmpty();
        }

        @Test
        @DisplayName("should retrieve annotation instance from field")
        void shouldRetrieveAnnotationInstanceFromField() throws Exception {
            Field field = YmlIgnoreFieldTarget.class.getDeclaredField("ignored");
            YmlIgnore annotation = field.getAnnotation(YmlIgnore.class);

            assertThat(annotation).isNotNull();
        }
    }

    // ==================== YmlProperty Tests ====================

    @Nested
    @DisplayName("YmlProperty Tests")
    class YmlPropertyTests {

        @Test
        @DisplayName("should be present on annotated field")
        void shouldBePresentOnAnnotatedField() throws Exception {
            Field field = YmlPropertyFieldTarget.class.getDeclaredField("name");

            assertThat(field.isAnnotationPresent(YmlProperty.class)).isTrue();
        }

        @Test
        @DisplayName("should not be present on non-annotated field")
        void shouldNotBePresentOnNonAnnotatedField() throws Exception {
            Field field = YmlPropertyFieldTarget.class.getDeclaredField("noAnnotation");

            assertThat(field.isAnnotationPresent(YmlProperty.class)).isFalse();
        }

        @Test
        @DisplayName("should retrieve value from annotation")
        void shouldRetrieveValueFromAnnotation() throws Exception {
            Field field = YmlPropertyFieldTarget.class.getDeclaredField("name");
            YmlProperty annotation = field.getAnnotation(YmlProperty.class);

            assertThat(annotation.value()).isEqualTo("app.name");
        }

        @Test
        @DisplayName("should retrieve defaultValue from annotation")
        void shouldRetrieveDefaultValueFromAnnotation() throws Exception {
            Field field = YmlPropertyFieldTarget.class.getDeclaredField("port");
            YmlProperty annotation = field.getAnnotation(YmlProperty.class);

            assertThat(annotation.defaultValue()).isEqualTo("8080");
        }

        @Test
        @DisplayName("should have empty string as default for defaultValue")
        void shouldHaveEmptyStringAsDefaultForDefaultValue() throws Exception {
            Field field = YmlPropertyFieldTarget.class.getDeclaredField("name");
            YmlProperty annotation = field.getAnnotation(YmlProperty.class);

            assertThat(annotation.defaultValue()).isEmpty();
        }

        @Test
        @DisplayName("should retrieve required attribute as true")
        void shouldRetrieveRequiredAttributeAsTrue() throws Exception {
            Field field = YmlPropertyFieldTarget.class.getDeclaredField("requiredField");
            YmlProperty annotation = field.getAnnotation(YmlProperty.class);

            assertThat(annotation.required()).isTrue();
        }

        @Test
        @DisplayName("should have false as default for required")
        void shouldHaveFalseAsDefaultForRequired() throws Exception {
            Field field = YmlPropertyFieldTarget.class.getDeclaredField("name");
            YmlProperty annotation = field.getAnnotation(YmlProperty.class);

            assertThat(annotation.required()).isFalse();
        }

        @Test
        @DisplayName("should retrieve all attributes together")
        void shouldRetrieveAllAttributesTogether() throws Exception {
            Field field = YmlPropertyFieldTarget.class.getDeclaredField("optionalWithDefault");
            YmlProperty annotation = field.getAnnotation(YmlProperty.class);

            assertThat(annotation.value()).isEqualTo("app.optional");
            assertThat(annotation.defaultValue()).isEqualTo("default");
            assertThat(annotation.required()).isFalse();
        }

        @Test
        @DisplayName("should be present on annotated method")
        void shouldBePresentOnAnnotatedMethod() throws Exception {
            Method method = YmlPropertyMethodTarget.class.getDeclaredMethod("setHost", String.class);

            assertThat(method.isAnnotationPresent(YmlProperty.class)).isTrue();
        }

        @Test
        @DisplayName("should retrieve attributes from method annotation")
        void shouldRetrieveAttributesFromMethodAnnotation() throws Exception {
            Method method = YmlPropertyMethodTarget.class.getDeclaredMethod("setHost", String.class);
            YmlProperty annotation = method.getAnnotation(YmlProperty.class);

            assertThat(annotation.value()).isEqualTo("server.host");
            assertThat(annotation.defaultValue()).isEqualTo("localhost");
        }

        @Test
        @DisplayName("should be present on annotated parameter")
        void shouldBePresentOnAnnotatedParameter() throws Exception {
            Method method = YmlPropertyParameterTarget.class.getDeclaredMethod("configure", String.class);
            Parameter parameter = method.getParameters()[0];

            assertThat(parameter.isAnnotationPresent(YmlProperty.class)).isTrue();
        }

        @Test
        @DisplayName("should retrieve attributes from parameter annotation")
        void shouldRetrieveAttributesFromParameterAnnotation() throws Exception {
            Method method = YmlPropertyParameterTarget.class.getDeclaredMethod("configure", String.class);
            Parameter parameter = method.getParameters()[0];
            YmlProperty annotation = parameter.getAnnotation(YmlProperty.class);

            assertThat(annotation.value()).isEqualTo("config.value");
            assertThat(annotation.required()).isTrue();
        }

        @Test
        @DisplayName("should have RUNTIME retention policy")
        void shouldHaveRuntimeRetentionPolicy() {
            Retention retention = YmlProperty.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD, METHOD, and PARAMETER")
        void shouldTargetFieldMethodAndParameter() {
            Target target = YmlProperty.class.getAnnotation(Target.class);

            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.FIELD,
                ElementType.METHOD,
                ElementType.PARAMETER
            );
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(YmlProperty.class.isAnnotationPresent(Documented.class)).isTrue();
        }

        @Test
        @DisplayName("should have value method defined")
        void shouldHaveValueMethodDefined() throws Exception {
            Method valueMethod = YmlProperty.class.getDeclaredMethod("value");

            assertThat(valueMethod).isNotNull();
            assertThat(valueMethod.getReturnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("should have defaultValue method defined")
        void shouldHaveDefaultValueMethodDefined() throws Exception {
            Method defaultValueMethod = YmlProperty.class.getDeclaredMethod("defaultValue");

            assertThat(defaultValueMethod).isNotNull();
            assertThat(defaultValueMethod.getReturnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("should have required method defined")
        void shouldHaveRequiredMethodDefined() throws Exception {
            Method requiredMethod = YmlProperty.class.getDeclaredMethod("required");

            assertThat(requiredMethod).isNotNull();
            assertThat(requiredMethod.getReturnType()).isEqualTo(boolean.class);
        }
    }

    // ==================== YmlValue Tests ====================

    @Nested
    @DisplayName("YmlValue Tests")
    class YmlValueTests {

        @Test
        @DisplayName("should be present on annotated field")
        void shouldBePresentOnAnnotatedField() throws Exception {
            Field field = YmlValueFieldTarget.class.getDeclaredField("port");

            assertThat(field.isAnnotationPresent(YmlValue.class)).isTrue();
        }

        @Test
        @DisplayName("should not be present on non-annotated field")
        void shouldNotBePresentOnNonAnnotatedField() throws Exception {
            Field field = YmlValueFieldTarget.class.getDeclaredField("noAnnotation");

            assertThat(field.isAnnotationPresent(YmlValue.class)).isFalse();
        }

        @Test
        @DisplayName("should retrieve value from annotation")
        void shouldRetrieveValueFromAnnotation() throws Exception {
            Field field = YmlValueFieldTarget.class.getDeclaredField("port");
            YmlValue annotation = field.getAnnotation(YmlValue.class);

            assertThat(annotation.value()).isEqualTo("server.port");
        }

        @Test
        @DisplayName("should retrieve defaultValue from annotation")
        void shouldRetrieveDefaultValueFromAnnotation() throws Exception {
            Field field = YmlValueFieldTarget.class.getDeclaredField("host");
            YmlValue annotation = field.getAnnotation(YmlValue.class);

            assertThat(annotation.defaultValue()).isEqualTo("localhost");
        }

        @Test
        @DisplayName("should have empty string as default for defaultValue")
        void shouldHaveEmptyStringAsDefaultForDefaultValue() throws Exception {
            Field field = YmlValueFieldTarget.class.getDeclaredField("port");
            YmlValue annotation = field.getAnnotation(YmlValue.class);

            assertThat(annotation.defaultValue()).isEmpty();
        }

        @Test
        @DisplayName("should be present on annotated parameter")
        void shouldBePresentOnAnnotatedParameter() throws Exception {
            Method method = YmlValueParameterTarget.class.getDeclaredMethod("initialize", String.class);
            Parameter parameter = method.getParameters()[0];

            assertThat(parameter.isAnnotationPresent(YmlValue.class)).isTrue();
        }

        @Test
        @DisplayName("should retrieve attributes from parameter annotation")
        void shouldRetrieveAttributesFromParameterAnnotation() throws Exception {
            Method method = YmlValueParameterTarget.class.getDeclaredMethod("initialize", String.class);
            Parameter parameter = method.getParameters()[0];
            YmlValue annotation = parameter.getAnnotation(YmlValue.class);

            assertThat(annotation.value()).isEqualTo("init.value");
            assertThat(annotation.defaultValue()).isEqualTo("default");
        }

        @Test
        @DisplayName("should have RUNTIME retention policy")
        void shouldHaveRuntimeRetentionPolicy() {
            Retention retention = YmlValue.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD and PARAMETER")
        void shouldTargetFieldAndParameter() {
            Target target = YmlValue.class.getAnnotation(Target.class);

            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.FIELD,
                ElementType.PARAMETER
            );
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(YmlValue.class.isAnnotationPresent(Documented.class)).isTrue();
        }

        @Test
        @DisplayName("should have value method defined")
        void shouldHaveValueMethodDefined() throws Exception {
            Method valueMethod = YmlValue.class.getDeclaredMethod("value");

            assertThat(valueMethod).isNotNull();
            assertThat(valueMethod.getReturnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("should have defaultValue method defined")
        void shouldHaveDefaultValueMethodDefined() throws Exception {
            Method defaultValueMethod = YmlValue.class.getDeclaredMethod("defaultValue");

            assertThat(defaultValueMethod).isNotNull();
            assertThat(defaultValueMethod.getReturnType()).isEqualTo(String.class);
        }
    }

    // ==================== YmlNestedProperty Tests ====================

    @Nested
    @DisplayName("YmlNestedProperty Tests")
    class YmlNestedPropertyTests {

        @Test
        @DisplayName("should be present on annotated field")
        void shouldBePresentOnAnnotatedField() throws Exception {
            Field field = YmlNestedPropertyFieldTarget.class.getDeclaredField("databaseConfig");

            assertThat(field.isAnnotationPresent(YmlNestedProperty.class)).isTrue();
        }

        @Test
        @DisplayName("should not be present on non-annotated field")
        void shouldNotBePresentOnNonAnnotatedField() throws Exception {
            Field field = YmlNestedPropertyFieldTarget.class.getDeclaredField("noAnnotation");

            assertThat(field.isAnnotationPresent(YmlNestedProperty.class)).isFalse();
        }

        @Test
        @DisplayName("should retrieve prefix from annotation")
        void shouldRetrievePrefixFromAnnotation() throws Exception {
            Field field = YmlNestedPropertyFieldTarget.class.getDeclaredField("databaseConfig");
            YmlNestedProperty annotation = field.getAnnotation(YmlNestedProperty.class);

            assertThat(annotation.prefix()).isEqualTo("database");
        }

        @Test
        @DisplayName("should retrieve nested prefix with dots")
        void shouldRetrieveNestedPrefixWithDots() throws Exception {
            Field field = YmlNestedPropertyFieldTarget.class.getDeclaredField("cacheConfig");
            YmlNestedProperty annotation = field.getAnnotation(YmlNestedProperty.class);

            assertThat(annotation.prefix()).isEqualTo("cache.settings");
        }

        @Test
        @DisplayName("should have empty string as default for prefix")
        void shouldHaveEmptyStringAsDefaultForPrefix() throws Exception {
            Field field = YmlNestedPropertyFieldTarget.class.getDeclaredField("defaultPrefixConfig");
            YmlNestedProperty annotation = field.getAnnotation(YmlNestedProperty.class);

            assertThat(annotation.prefix()).isEmpty();
        }

        @Test
        @DisplayName("should have RUNTIME retention policy")
        void shouldHaveRuntimeRetentionPolicy() {
            Retention retention = YmlNestedProperty.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target only FIELD")
        void shouldTargetOnlyField() {
            Target target = YmlNestedProperty.class.getAnnotation(Target.class);

            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(YmlNestedProperty.class.isAnnotationPresent(Documented.class)).isTrue();
        }

        @Test
        @DisplayName("should have prefix method defined")
        void shouldHavePrefixMethodDefined() throws Exception {
            Method prefixMethod = YmlNestedProperty.class.getDeclaredMethod("prefix");

            assertThat(prefixMethod).isNotNull();
            assertThat(prefixMethod.getReturnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("should have only prefix method")
        void shouldHaveOnlyPrefixMethod() {
            Method[] methods = YmlNestedProperty.class.getDeclaredMethods();

            assertThat(methods).hasSize(1);
            assertThat(methods[0].getName()).isEqualTo("prefix");
        }
    }

    // ==================== Mixed Annotations Tests ====================

    @Nested
    @DisplayName("Mixed Annotations Tests")
    class MixedAnnotationsTests {

        @Test
        @DisplayName("should detect YmlProperty on mixed class")
        void shouldDetectYmlPropertyOnMixedClass() throws Exception {
            Field field = MixedAnnotationsTarget.class.getDeclaredField("name");

            assertThat(field.isAnnotationPresent(YmlProperty.class)).isTrue();
            assertThat(field.isAnnotationPresent(YmlAlias.class)).isFalse();
            assertThat(field.isAnnotationPresent(YmlIgnore.class)).isFalse();
        }

        @Test
        @DisplayName("should detect YmlAlias on mixed class")
        void shouldDetectYmlAliasOnMixedClass() throws Exception {
            Field field = MixedAnnotationsTarget.class.getDeclaredField("aliased");

            assertThat(field.isAnnotationPresent(YmlAlias.class)).isTrue();
            assertThat(field.isAnnotationPresent(YmlProperty.class)).isFalse();
            assertThat(field.isAnnotationPresent(YmlIgnore.class)).isFalse();
        }

        @Test
        @DisplayName("should detect YmlIgnore on mixed class")
        void shouldDetectYmlIgnoreOnMixedClass() throws Exception {
            Field field = MixedAnnotationsTarget.class.getDeclaredField("ignored");

            assertThat(field.isAnnotationPresent(YmlIgnore.class)).isTrue();
            assertThat(field.isAnnotationPresent(YmlProperty.class)).isFalse();
            assertThat(field.isAnnotationPresent(YmlAlias.class)).isFalse();
        }

        @Test
        @DisplayName("should detect YmlValue on mixed class")
        void shouldDetectYmlValueOnMixedClass() throws Exception {
            Field field = MixedAnnotationsTarget.class.getDeclaredField("value");

            assertThat(field.isAnnotationPresent(YmlValue.class)).isTrue();
        }

        @Test
        @DisplayName("should detect YmlNestedProperty on mixed class")
        void shouldDetectYmlNestedPropertyOnMixedClass() throws Exception {
            Field field = MixedAnnotationsTarget.class.getDeclaredField("nested");

            assertThat(field.isAnnotationPresent(YmlNestedProperty.class)).isTrue();
        }

        @Test
        @DisplayName("should retrieve all annotations from class fields")
        void shouldRetrieveAllAnnotationsFromClassFields() {
            Field[] fields = MixedAnnotationsTarget.class.getDeclaredFields();

            int ymlPropertyCount = 0;
            int ymlAliasCount = 0;
            int ymlIgnoreCount = 0;
            int ymlValueCount = 0;
            int ymlNestedPropertyCount = 0;

            for (Field field : fields) {
                if (field.isAnnotationPresent(YmlProperty.class)) ymlPropertyCount++;
                if (field.isAnnotationPresent(YmlAlias.class)) ymlAliasCount++;
                if (field.isAnnotationPresent(YmlIgnore.class)) ymlIgnoreCount++;
                if (field.isAnnotationPresent(YmlValue.class)) ymlValueCount++;
                if (field.isAnnotationPresent(YmlNestedProperty.class)) ymlNestedPropertyCount++;
            }

            assertThat(ymlPropertyCount).isEqualTo(1);
            assertThat(ymlAliasCount).isEqualTo(1);
            assertThat(ymlIgnoreCount).isEqualTo(1);
            assertThat(ymlValueCount).isEqualTo(1);
            assertThat(ymlNestedPropertyCount).isEqualTo(1);
        }

        @Test
        @DisplayName("should allow finding annotated fields by annotation type")
        void shouldAllowFindingAnnotatedFieldsByAnnotationType() {
            Field[] fields = MixedAnnotationsTarget.class.getDeclaredFields();

            for (Field field : fields) {
                Annotation[] annotations = field.getAnnotations();
                if (annotations.length > 0) {
                    assertThat(annotations[0]).isInstanceOfAny(
                        YmlProperty.class,
                        YmlAlias.class,
                        YmlIgnore.class,
                        YmlValue.class,
                        YmlNestedProperty.class
                    );
                }
            }
        }
    }

    // ==================== Annotation Metadata Tests ====================

    @Nested
    @DisplayName("Annotation Metadata Tests")
    class AnnotationMetadataTests {

        @Test
        @DisplayName("all annotations should be in same package")
        void allAnnotationsShouldBeInSamePackage() {
            String expectedPackage = "cloud.opencode.base.yml.bind";

            assertThat(YmlAlias.class.getPackageName()).isEqualTo(expectedPackage);
            assertThat(YmlIgnore.class.getPackageName()).isEqualTo(expectedPackage);
            assertThat(YmlProperty.class.getPackageName()).isEqualTo(expectedPackage);
            assertThat(YmlValue.class.getPackageName()).isEqualTo(expectedPackage);
            assertThat(YmlNestedProperty.class.getPackageName()).isEqualTo(expectedPackage);
        }

        @Test
        @DisplayName("all annotations should be annotation types")
        void allAnnotationsShouldBeAnnotationTypes() {
            assertThat(YmlAlias.class.isAnnotation()).isTrue();
            assertThat(YmlIgnore.class.isAnnotation()).isTrue();
            assertThat(YmlProperty.class.isAnnotation()).isTrue();
            assertThat(YmlValue.class.isAnnotation()).isTrue();
            assertThat(YmlNestedProperty.class.isAnnotation()).isTrue();
        }

        @Test
        @DisplayName("all annotations should have Annotation as superinterface")
        void allAnnotationsShouldHaveAnnotationAsSuperinterface() {
            assertThat(Annotation.class.isAssignableFrom(YmlAlias.class)).isTrue();
            assertThat(Annotation.class.isAssignableFrom(YmlIgnore.class)).isTrue();
            assertThat(Annotation.class.isAssignableFrom(YmlProperty.class)).isTrue();
            assertThat(Annotation.class.isAssignableFrom(YmlValue.class)).isTrue();
            assertThat(Annotation.class.isAssignableFrom(YmlNestedProperty.class)).isTrue();
        }

        @Test
        @DisplayName("all annotations should be runtime retained")
        void allAnnotationsShouldBeRuntimeRetained() {
            assertThat(YmlAlias.class.getAnnotation(Retention.class).value())
                .isEqualTo(RetentionPolicy.RUNTIME);
            assertThat(YmlIgnore.class.getAnnotation(Retention.class).value())
                .isEqualTo(RetentionPolicy.RUNTIME);
            assertThat(YmlProperty.class.getAnnotation(Retention.class).value())
                .isEqualTo(RetentionPolicy.RUNTIME);
            assertThat(YmlValue.class.getAnnotation(Retention.class).value())
                .isEqualTo(RetentionPolicy.RUNTIME);
            assertThat(YmlNestedProperty.class.getAnnotation(Retention.class).value())
                .isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("all annotations should be documented")
        void allAnnotationsShouldBeDocumented() {
            assertThat(YmlAlias.class.isAnnotationPresent(Documented.class)).isTrue();
            assertThat(YmlIgnore.class.isAnnotationPresent(Documented.class)).isTrue();
            assertThat(YmlProperty.class.isAnnotationPresent(Documented.class)).isTrue();
            assertThat(YmlValue.class.isAnnotationPresent(Documented.class)).isTrue();
            assertThat(YmlNestedProperty.class.isAnnotationPresent(Documented.class)).isTrue();
        }
    }
}
