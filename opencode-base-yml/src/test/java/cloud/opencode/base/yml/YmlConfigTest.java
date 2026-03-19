package cloud.opencode.base.yml;

import cloud.opencode.base.yml.YmlConfig.FlowStyle;
import cloud.opencode.base.yml.YmlConfig.ScalarStyle;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlConfigTest Tests
 * YmlConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlConfig Tests")
class YmlConfigTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("defaults should create default configuration")
        void defaultsShouldCreateDefaultConfiguration() {
            YmlConfig config = YmlConfig.defaults();

            assertThat(config).isNotNull();
            assertThat(config.getIndent()).isEqualTo(2);
            assertThat(config.isPrettyPrint()).isTrue();
            assertThat(config.isAllowDuplicateKeys()).isFalse();
            assertThat(config.isSafeMode()).isTrue();
        }

        @Test
        @DisplayName("builder should create new builder")
        void builderShouldCreateNewBuilder() {
            YmlConfig.Builder builder = YmlConfig.builder();

            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("default indent should be 2")
        void defaultIndentShouldBe2() {
            YmlConfig config = YmlConfig.defaults();

            assertThat(config.getIndent()).isEqualTo(2);
        }

        @Test
        @DisplayName("default prettyPrint should be true")
        void defaultPrettyPrintShouldBeTrue() {
            YmlConfig config = YmlConfig.defaults();

            assertThat(config.isPrettyPrint()).isTrue();
        }

        @Test
        @DisplayName("default allowDuplicateKeys should be false")
        void defaultAllowDuplicateKeysShouldBeFalse() {
            YmlConfig config = YmlConfig.defaults();

            assertThat(config.isAllowDuplicateKeys()).isFalse();
        }

        @Test
        @DisplayName("default safeMode should be true")
        void defaultSafeModeShouldBeTrue() {
            YmlConfig config = YmlConfig.defaults();

            assertThat(config.isSafeMode()).isTrue();
        }

        @Test
        @DisplayName("default maxAliasesForCollections should be 50")
        void defaultMaxAliasesForCollectionsShouldBe50() {
            YmlConfig config = YmlConfig.defaults();

            assertThat(config.getMaxAliasesForCollections()).isEqualTo(50);
        }

        @Test
        @DisplayName("default maxNestingDepth should be 100")
        void defaultMaxNestingDepthShouldBe100() {
            YmlConfig config = YmlConfig.defaults();

            assertThat(config.getMaxNestingDepth()).isEqualTo(100);
        }

        @Test
        @DisplayName("default maxDocumentSize should be 10MB")
        void defaultMaxDocumentSizeShouldBe10MB() {
            YmlConfig config = YmlConfig.defaults();

            assertThat(config.getMaxDocumentSize()).isEqualTo(10 * 1024 * 1024);
        }

        @Test
        @DisplayName("default flowStyle should be BLOCK")
        void defaultFlowStyleShouldBeBlock() {
            YmlConfig config = YmlConfig.defaults();

            assertThat(config.getDefaultFlowStyle()).isEqualTo(FlowStyle.BLOCK);
        }

        @Test
        @DisplayName("default scalarStyle should be PLAIN")
        void defaultScalarStyleShouldBePlain() {
            YmlConfig config = YmlConfig.defaults();

            assertThat(config.getDefaultScalarStyle()).isEqualTo(ScalarStyle.PLAIN);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("indent should set indent value")
        void indentShouldSetIndentValue() {
            YmlConfig config = YmlConfig.builder()
                .indent(4)
                .build();

            assertThat(config.getIndent()).isEqualTo(4);
        }

        @Test
        @DisplayName("prettyPrint should set prettyPrint value")
        void prettyPrintShouldSetPrettyPrintValue() {
            YmlConfig config = YmlConfig.builder()
                .prettyPrint(false)
                .build();

            assertThat(config.isPrettyPrint()).isFalse();
        }

        @Test
        @DisplayName("allowDuplicateKeys should set allowDuplicateKeys value")
        void allowDuplicateKeysShouldSetAllowDuplicateKeysValue() {
            YmlConfig config = YmlConfig.builder()
                .allowDuplicateKeys(true)
                .build();

            assertThat(config.isAllowDuplicateKeys()).isTrue();
        }

        @Test
        @DisplayName("safeMode should set safeMode value")
        void safeModeShouldSetSafeModeValue() {
            YmlConfig config = YmlConfig.builder()
                .safeMode(false)
                .build();

            assertThat(config.isSafeMode()).isFalse();
        }

        @Test
        @DisplayName("maxAliasesForCollections should set value")
        void maxAliasesForCollectionsShouldSetValue() {
            YmlConfig config = YmlConfig.builder()
                .maxAliasesForCollections(100)
                .build();

            assertThat(config.getMaxAliasesForCollections()).isEqualTo(100);
        }

        @Test
        @DisplayName("maxNestingDepth should set value")
        void maxNestingDepthShouldSetValue() {
            YmlConfig config = YmlConfig.builder()
                .maxNestingDepth(200)
                .build();

            assertThat(config.getMaxNestingDepth()).isEqualTo(200);
        }

        @Test
        @DisplayName("maxDocumentSize should set value")
        void maxDocumentSizeShouldSetValue() {
            YmlConfig config = YmlConfig.builder()
                .maxDocumentSize(5 * 1024 * 1024)
                .build();

            assertThat(config.getMaxDocumentSize()).isEqualTo(5 * 1024 * 1024);
        }

        @Test
        @DisplayName("defaultFlowStyle should set value")
        void defaultFlowStyleShouldSetValue() {
            YmlConfig config = YmlConfig.builder()
                .defaultFlowStyle(FlowStyle.FLOW)
                .build();

            assertThat(config.getDefaultFlowStyle()).isEqualTo(FlowStyle.FLOW);
        }

        @Test
        @DisplayName("defaultScalarStyle should set value")
        void defaultScalarStyleShouldSetValue() {
            YmlConfig config = YmlConfig.builder()
                .defaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
                .build();

            assertThat(config.getDefaultScalarStyle()).isEqualTo(ScalarStyle.DOUBLE_QUOTED);
        }

        @Test
        @DisplayName("builder should chain multiple settings")
        void builderShouldChainMultipleSettings() {
            YmlConfig config = YmlConfig.builder()
                .indent(4)
                .prettyPrint(false)
                .allowDuplicateKeys(true)
                .safeMode(false)
                .maxAliasesForCollections(100)
                .maxNestingDepth(200)
                .maxDocumentSize(20 * 1024 * 1024)
                .defaultFlowStyle(FlowStyle.AUTO)
                .defaultScalarStyle(ScalarStyle.SINGLE_QUOTED)
                .build();

            assertThat(config.getIndent()).isEqualTo(4);
            assertThat(config.isPrettyPrint()).isFalse();
            assertThat(config.isAllowDuplicateKeys()).isTrue();
            assertThat(config.isSafeMode()).isFalse();
            assertThat(config.getMaxAliasesForCollections()).isEqualTo(100);
            assertThat(config.getMaxNestingDepth()).isEqualTo(200);
            assertThat(config.getMaxDocumentSize()).isEqualTo(20 * 1024 * 1024);
            assertThat(config.getDefaultFlowStyle()).isEqualTo(FlowStyle.AUTO);
            assertThat(config.getDefaultScalarStyle()).isEqualTo(ScalarStyle.SINGLE_QUOTED);
        }

        @Test
        @DisplayName("builder should return this for fluent API")
        void builderShouldReturnThisForFluentApi() {
            YmlConfig.Builder builder = YmlConfig.builder();

            assertThat(builder.indent(2)).isSameAs(builder);
            assertThat(builder.prettyPrint(true)).isSameAs(builder);
            assertThat(builder.allowDuplicateKeys(false)).isSameAs(builder);
            assertThat(builder.safeMode(true)).isSameAs(builder);
            assertThat(builder.maxAliasesForCollections(50)).isSameAs(builder);
            assertThat(builder.maxNestingDepth(100)).isSameAs(builder);
            assertThat(builder.maxDocumentSize(1024)).isSameAs(builder);
            assertThat(builder.defaultFlowStyle(FlowStyle.BLOCK)).isSameAs(builder);
            assertThat(builder.defaultScalarStyle(ScalarStyle.PLAIN)).isSameAs(builder);
        }
    }

    @Nested
    @DisplayName("FlowStyle Enum Tests")
    class FlowStyleEnumTests {

        @Test
        @DisplayName("FlowStyle should have FLOW value")
        void flowStyleShouldHaveFlowValue() {
            assertThat(FlowStyle.FLOW).isNotNull();
            assertThat(FlowStyle.valueOf("FLOW")).isEqualTo(FlowStyle.FLOW);
        }

        @Test
        @DisplayName("FlowStyle should have BLOCK value")
        void flowStyleShouldHaveBlockValue() {
            assertThat(FlowStyle.BLOCK).isNotNull();
            assertThat(FlowStyle.valueOf("BLOCK")).isEqualTo(FlowStyle.BLOCK);
        }

        @Test
        @DisplayName("FlowStyle should have AUTO value")
        void flowStyleShouldHaveAutoValue() {
            assertThat(FlowStyle.AUTO).isNotNull();
            assertThat(FlowStyle.valueOf("AUTO")).isEqualTo(FlowStyle.AUTO);
        }

        @Test
        @DisplayName("FlowStyle values should return all values")
        void flowStyleValuesShouldReturnAllValues() {
            FlowStyle[] values = FlowStyle.values();

            assertThat(values).hasSize(3);
            assertThat(values).containsExactly(FlowStyle.FLOW, FlowStyle.BLOCK, FlowStyle.AUTO);
        }
    }

    @Nested
    @DisplayName("ScalarStyle Enum Tests")
    class ScalarStyleEnumTests {

        @Test
        @DisplayName("ScalarStyle should have PLAIN value")
        void scalarStyleShouldHavePlainValue() {
            assertThat(ScalarStyle.PLAIN).isNotNull();
            assertThat(ScalarStyle.valueOf("PLAIN")).isEqualTo(ScalarStyle.PLAIN);
        }

        @Test
        @DisplayName("ScalarStyle should have SINGLE_QUOTED value")
        void scalarStyleShouldHaveSingleQuotedValue() {
            assertThat(ScalarStyle.SINGLE_QUOTED).isNotNull();
            assertThat(ScalarStyle.valueOf("SINGLE_QUOTED")).isEqualTo(ScalarStyle.SINGLE_QUOTED);
        }

        @Test
        @DisplayName("ScalarStyle should have DOUBLE_QUOTED value")
        void scalarStyleShouldHaveDoubleQuotedValue() {
            assertThat(ScalarStyle.DOUBLE_QUOTED).isNotNull();
            assertThat(ScalarStyle.valueOf("DOUBLE_QUOTED")).isEqualTo(ScalarStyle.DOUBLE_QUOTED);
        }

        @Test
        @DisplayName("ScalarStyle should have LITERAL value")
        void scalarStyleShouldHaveLiteralValue() {
            assertThat(ScalarStyle.LITERAL).isNotNull();
            assertThat(ScalarStyle.valueOf("LITERAL")).isEqualTo(ScalarStyle.LITERAL);
        }

        @Test
        @DisplayName("ScalarStyle should have FOLDED value")
        void scalarStyleShouldHaveFoldedValue() {
            assertThat(ScalarStyle.FOLDED).isNotNull();
            assertThat(ScalarStyle.valueOf("FOLDED")).isEqualTo(ScalarStyle.FOLDED);
        }

        @Test
        @DisplayName("ScalarStyle values should return all values")
        void scalarStyleValuesShouldReturnAllValues() {
            ScalarStyle[] values = ScalarStyle.values();

            assertThat(values).hasSize(5);
            assertThat(values).containsExactly(
                ScalarStyle.PLAIN,
                ScalarStyle.SINGLE_QUOTED,
                ScalarStyle.DOUBLE_QUOTED,
                ScalarStyle.LITERAL,
                ScalarStyle.FOLDED
            );
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getIndent should return indent value")
        void getIndentShouldReturnIndentValue() {
            YmlConfig config = YmlConfig.builder().indent(8).build();

            assertThat(config.getIndent()).isEqualTo(8);
        }

        @Test
        @DisplayName("isPrettyPrint should return prettyPrint value")
        void isPrettyPrintShouldReturnPrettyPrintValue() {
            YmlConfig config = YmlConfig.builder().prettyPrint(false).build();

            assertThat(config.isPrettyPrint()).isFalse();
        }

        @Test
        @DisplayName("isAllowDuplicateKeys should return allowDuplicateKeys value")
        void isAllowDuplicateKeysShouldReturnAllowDuplicateKeysValue() {
            YmlConfig config = YmlConfig.builder().allowDuplicateKeys(true).build();

            assertThat(config.isAllowDuplicateKeys()).isTrue();
        }

        @Test
        @DisplayName("isSafeMode should return safeMode value")
        void isSafeModeShouldReturnSafeModeValue() {
            YmlConfig config = YmlConfig.builder().safeMode(false).build();

            assertThat(config.isSafeMode()).isFalse();
        }

        @Test
        @DisplayName("getMaxAliasesForCollections should return maxAliasesForCollections value")
        void getMaxAliasesForCollectionsShouldReturnMaxAliasesForCollectionsValue() {
            YmlConfig config = YmlConfig.builder().maxAliasesForCollections(75).build();

            assertThat(config.getMaxAliasesForCollections()).isEqualTo(75);
        }

        @Test
        @DisplayName("getMaxNestingDepth should return maxNestingDepth value")
        void getMaxNestingDepthShouldReturnMaxNestingDepthValue() {
            YmlConfig config = YmlConfig.builder().maxNestingDepth(150).build();

            assertThat(config.getMaxNestingDepth()).isEqualTo(150);
        }

        @Test
        @DisplayName("getMaxDocumentSize should return maxDocumentSize value")
        void getMaxDocumentSizeShouldReturnMaxDocumentSizeValue() {
            YmlConfig config = YmlConfig.builder().maxDocumentSize(1024 * 1024).build();

            assertThat(config.getMaxDocumentSize()).isEqualTo(1024 * 1024);
        }

        @Test
        @DisplayName("getDefaultFlowStyle should return defaultFlowStyle value")
        void getDefaultFlowStyleShouldReturnDefaultFlowStyleValue() {
            YmlConfig config = YmlConfig.builder().defaultFlowStyle(FlowStyle.FLOW).build();

            assertThat(config.getDefaultFlowStyle()).isEqualTo(FlowStyle.FLOW);
        }

        @Test
        @DisplayName("getDefaultScalarStyle should return defaultScalarStyle value")
        void getDefaultScalarStyleShouldReturnDefaultScalarStyleValue() {
            YmlConfig config = YmlConfig.builder().defaultScalarStyle(ScalarStyle.LITERAL).build();

            assertThat(config.getDefaultScalarStyle()).isEqualTo(ScalarStyle.LITERAL);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero indent")
        void shouldHandleZeroIndent() {
            YmlConfig config = YmlConfig.builder().indent(0).build();

            assertThat(config.getIndent()).isZero();
        }

        @Test
        @DisplayName("should handle negative indent")
        void shouldHandleNegativeIndent() {
            YmlConfig config = YmlConfig.builder().indent(-1).build();

            assertThat(config.getIndent()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should handle zero max document size")
        void shouldHandleZeroMaxDocumentSize() {
            YmlConfig config = YmlConfig.builder().maxDocumentSize(0).build();

            assertThat(config.getMaxDocumentSize()).isZero();
        }

        @Test
        @DisplayName("should handle large indent value")
        void shouldHandleLargeIndentValue() {
            YmlConfig config = YmlConfig.builder().indent(100).build();

            assertThat(config.getIndent()).isEqualTo(100);
        }

        @Test
        @DisplayName("should handle max long document size")
        void shouldHandleMaxLongDocumentSize() {
            YmlConfig config = YmlConfig.builder().maxDocumentSize(Long.MAX_VALUE).build();

            assertThat(config.getMaxDocumentSize()).isEqualTo(Long.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("Configuration Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("config should be immutable after build")
        void configShouldBeImmutableAfterBuild() {
            YmlConfig.Builder builder = YmlConfig.builder().indent(2);
            YmlConfig config = builder.build();

            builder.indent(4);
            YmlConfig newConfig = builder.build();

            assertThat(config.getIndent()).isEqualTo(2);
            assertThat(newConfig.getIndent()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("config should be usable with OpenYml dump")
        void configShouldBeUsableWithOpenYmlDump() {
            YmlConfig config = YmlConfig.builder()
                .indent(4)
                .prettyPrint(true)
                .build();

            String yaml = OpenYml.dump(java.util.Map.of("key", "value"), config);

            assertThat(yaml).isNotNull();
            assertThat(yaml).contains("key");
        }
    }
}
