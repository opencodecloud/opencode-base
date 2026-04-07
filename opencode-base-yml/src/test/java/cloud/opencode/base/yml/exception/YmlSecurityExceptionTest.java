package cloud.opencode.base.yml.exception;

import cloud.opencode.base.yml.exception.YmlSecurityException.SecurityViolationType;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlSecurityExceptionTest Tests
 * YmlSecurityExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlSecurityException Tests")
class YmlSecurityExceptionTest {

    @Nested
    @DisplayName("Constructor with type and message")
    class TypeAndMessageConstructorTests {

        @Test
        @DisplayName("should format message with type prefix")
        void shouldFormatMessageWithTypePrefix() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.ALIAS_LIMIT_EXCEEDED, "Too many aliases");

            assertThat(exception.getMessage())
                .contains("YAML Security violation [ALIAS_LIMIT_EXCEEDED]: Too many aliases");
        }

        @Test
        @DisplayName("should store type")
        void shouldStoreType() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.NESTING_DEPTH_EXCEEDED, "Deep nesting");

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.NESTING_DEPTH_EXCEEDED);
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.FORBIDDEN_TYPE, "Forbidden");

            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("should handle all violation types")
        void shouldHandleAllViolationTypes() {
            for (SecurityViolationType type : SecurityViolationType.values()) {
                YmlSecurityException exception = new YmlSecurityException(type, "Test message");

                assertThat(exception.getType()).isEqualTo(type);
                assertThat(exception.getMessage()).contains(type.name());
            }
        }
    }

    @Nested
    @DisplayName("Constructor with type, message and cause")
    class FullConstructorTests {

        @Test
        @DisplayName("should format message with type prefix")
        void shouldFormatMessageWithTypePrefix() {
            Throwable cause = new RuntimeException("Root cause");
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.DOCUMENT_SIZE_EXCEEDED, "Document too large", cause);

            assertThat(exception.getMessage())
                .contains("YAML Security violation [DOCUMENT_SIZE_EXCEEDED]: Document too large");
        }

        @Test
        @DisplayName("should store type")
        void shouldStoreType() {
            Throwable cause = new RuntimeException();
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.RECURSIVE_REFERENCE, "Recursive ref", cause);

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.RECURSIVE_REFERENCE);
        }

        @Test
        @DisplayName("should store cause")
        void shouldStoreCause() {
            Throwable cause = new IllegalStateException("Invalid state");
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.FORBIDDEN_TYPE, "Type error", cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("getType method")
    class GetTypeTests {

        @Test
        @DisplayName("should return ALIAS_LIMIT_EXCEEDED")
        void shouldReturnAliasLimitExceeded() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.ALIAS_LIMIT_EXCEEDED, "Message");

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.ALIAS_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("should return NESTING_DEPTH_EXCEEDED")
        void shouldReturnNestingDepthExceeded() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.NESTING_DEPTH_EXCEEDED, "Message");

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.NESTING_DEPTH_EXCEEDED);
        }

        @Test
        @DisplayName("should return DOCUMENT_SIZE_EXCEEDED")
        void shouldReturnDocumentSizeExceeded() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.DOCUMENT_SIZE_EXCEEDED, "Message");

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.DOCUMENT_SIZE_EXCEEDED);
        }

        @Test
        @DisplayName("should return FORBIDDEN_TYPE")
        void shouldReturnForbiddenType() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.FORBIDDEN_TYPE, "Message");

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.FORBIDDEN_TYPE);
        }

        @Test
        @DisplayName("should return RECURSIVE_REFERENCE")
        void shouldReturnRecursiveReference() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.RECURSIVE_REFERENCE, "Message");

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.RECURSIVE_REFERENCE);
        }
    }

    @Nested
    @DisplayName("SecurityViolationType enum")
    class SecurityViolationTypeEnumTests {

        @Test
        @DisplayName("should have five violation types")
        void shouldHaveFiveViolationTypes() {
            assertThat(SecurityViolationType.values()).hasSize(5);
        }

        @Test
        @DisplayName("should contain ALIAS_LIMIT_EXCEEDED")
        void shouldContainAliasLimitExceeded() {
            assertThat(SecurityViolationType.valueOf("ALIAS_LIMIT_EXCEEDED"))
                .isEqualTo(SecurityViolationType.ALIAS_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("should contain NESTING_DEPTH_EXCEEDED")
        void shouldContainNestingDepthExceeded() {
            assertThat(SecurityViolationType.valueOf("NESTING_DEPTH_EXCEEDED"))
                .isEqualTo(SecurityViolationType.NESTING_DEPTH_EXCEEDED);
        }

        @Test
        @DisplayName("should contain DOCUMENT_SIZE_EXCEEDED")
        void shouldContainDocumentSizeExceeded() {
            assertThat(SecurityViolationType.valueOf("DOCUMENT_SIZE_EXCEEDED"))
                .isEqualTo(SecurityViolationType.DOCUMENT_SIZE_EXCEEDED);
        }

        @Test
        @DisplayName("should contain FORBIDDEN_TYPE")
        void shouldContainForbiddenType() {
            assertThat(SecurityViolationType.valueOf("FORBIDDEN_TYPE"))
                .isEqualTo(SecurityViolationType.FORBIDDEN_TYPE);
        }

        @Test
        @DisplayName("should contain RECURSIVE_REFERENCE")
        void shouldContainRecursiveReference() {
            assertThat(SecurityViolationType.valueOf("RECURSIVE_REFERENCE"))
                .isEqualTo(SecurityViolationType.RECURSIVE_REFERENCE);
        }

        @Test
        @DisplayName("should throw for invalid type name")
        void shouldThrowForInvalidTypeName() {
            assertThatThrownBy(() -> SecurityViolationType.valueOf("INVALID_TYPE"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("aliasLimitExceeded factory method")
    class AliasLimitExceededFactoryTests {

        @Test
        @DisplayName("should create exception with formatted message")
        void shouldCreateExceptionWithFormattedMessage() {
            YmlSecurityException exception = YmlSecurityException.aliasLimitExceeded(1000, 100);

            assertThat(exception.getMessage())
                .contains("Alias count 1000 exceeds limit 100")
                .contains("YAML bomb attack");
        }

        @Test
        @DisplayName("should set type to ALIAS_LIMIT_EXCEEDED")
        void shouldSetTypeToAliasLimitExceeded() {
            YmlSecurityException exception = YmlSecurityException.aliasLimitExceeded(500, 100);

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.ALIAS_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("should include count and limit in message")
        void shouldIncludeCountAndLimitInMessage() {
            YmlSecurityException exception = YmlSecurityException.aliasLimitExceeded(200, 50);

            assertThat(exception.getMessage()).contains("200");
            assertThat(exception.getMessage()).contains("50");
        }
    }

    @Nested
    @DisplayName("nestingDepthExceeded factory method")
    class NestingDepthExceededFactoryTests {

        @Test
        @DisplayName("should create exception with formatted message")
        void shouldCreateExceptionWithFormattedMessage() {
            YmlSecurityException exception = YmlSecurityException.nestingDepthExceeded(150, 100);

            assertThat(exception.getMessage())
                .contains("Nesting depth 150 exceeds limit 100");
        }

        @Test
        @DisplayName("should set type to NESTING_DEPTH_EXCEEDED")
        void shouldSetTypeToNestingDepthExceeded() {
            YmlSecurityException exception = YmlSecurityException.nestingDepthExceeded(50, 20);

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.NESTING_DEPTH_EXCEEDED);
        }

        @Test
        @DisplayName("should include depth and limit in message")
        void shouldIncludeDepthAndLimitInMessage() {
            YmlSecurityException exception = YmlSecurityException.nestingDepthExceeded(75, 50);

            assertThat(exception.getMessage()).contains("75");
            assertThat(exception.getMessage()).contains("50");
        }
    }

    @Nested
    @DisplayName("documentSizeExceeded factory method")
    class DocumentSizeExceededFactoryTests {

        @Test
        @DisplayName("should create exception with formatted message")
        void shouldCreateExceptionWithFormattedMessage() {
            YmlSecurityException exception = YmlSecurityException.documentSizeExceeded(2000000L, 1000000L);

            assertThat(exception.getMessage())
                .contains("Document size 2000000 bytes exceeds limit 1000000 bytes");
        }

        @Test
        @DisplayName("should set type to DOCUMENT_SIZE_EXCEEDED")
        void shouldSetTypeToDocumentSizeExceeded() {
            YmlSecurityException exception = YmlSecurityException.documentSizeExceeded(5000L, 1000L);

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.DOCUMENT_SIZE_EXCEEDED);
        }

        @Test
        @DisplayName("should handle large sizes")
        void shouldHandleLargeSizes() {
            YmlSecurityException exception = YmlSecurityException.documentSizeExceeded(
                Long.MAX_VALUE, Long.MAX_VALUE - 1);

            assertThat(exception.getMessage()).contains("bytes");
        }
    }

    @Nested
    @DisplayName("forbiddenType factory method")
    class ForbiddenTypeFactoryTests {

        @Test
        @DisplayName("should create exception with formatted message")
        void shouldCreateExceptionWithFormattedMessage() {
            YmlSecurityException exception = YmlSecurityException.forbiddenType("java.lang.Runtime");

            assertThat(exception.getMessage())
                .contains("Deserialization of type 'java.lang.Runtime' is not allowed");
        }

        @Test
        @DisplayName("should set type to FORBIDDEN_TYPE")
        void shouldSetTypeToForbiddenType() {
            YmlSecurityException exception = YmlSecurityException.forbiddenType("javax.script.ScriptEngine");

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.FORBIDDEN_TYPE);
        }

        @Test
        @DisplayName("should include type name in message")
        void shouldIncludeTypeNameInMessage() {
            YmlSecurityException exception = YmlSecurityException.forbiddenType("com.malicious.Exploit");

            assertThat(exception.getMessage()).contains("com.malicious.Exploit");
        }
    }

    @Nested
    @DisplayName("unsafeType factory method")
    class UnsafeTypeFactoryTests {

        @Test
        @DisplayName("should create exception with formatted message")
        void shouldCreateExceptionWithFormattedMessage() {
            YmlSecurityException exception = YmlSecurityException.unsafeType("java.beans.EventHandler");

            assertThat(exception.getMessage())
                .contains("Type 'java.beans.EventHandler' is not allowed for safe YAML construction");
        }

        @Test
        @DisplayName("should set type to FORBIDDEN_TYPE")
        void shouldSetTypeToForbiddenType() {
            YmlSecurityException exception = YmlSecurityException.unsafeType("java.net.URL");

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.FORBIDDEN_TYPE);
        }

        @Test
        @DisplayName("should include type name in message")
        void shouldIncludeTypeNameInMessage() {
            YmlSecurityException exception = YmlSecurityException.unsafeType("javax.naming.InitialContext");

            assertThat(exception.getMessage()).contains("javax.naming.InitialContext");
        }
    }

    @Nested
    @DisplayName("Inheritance hierarchy")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenYmlException")
        void shouldExtendOpenYmlException() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.FORBIDDEN_TYPE, "Test");

            assertThat(exception).isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.FORBIDDEN_TYPE, "Test");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should be catchable as OpenYmlException")
        void shouldBeCatchableAsOpenYmlException() {
            assertThatThrownBy(() -> {
                throw new YmlSecurityException(SecurityViolationType.ALIAS_LIMIT_EXCEEDED, "Error");
            }).isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should inherit getLine method returning -1")
        void shouldInheritGetLineMethodReturningNegativeOne() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.FORBIDDEN_TYPE, "Error");

            assertThat(exception.getLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should inherit getColumn method returning -1")
        void shouldInheritGetColumnMethodReturningNegativeOne() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.FORBIDDEN_TYPE, "Error");

            assertThat(exception.getColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should inherit hasLocation returning false")
        void shouldInheritHasLocationReturningFalse() {
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.FORBIDDEN_TYPE, "Error");

            assertThat(exception.hasLocation()).isFalse();
        }
    }

    @Nested
    @DisplayName("Typical security scenarios")
    class TypicalScenarioTests {

        @Test
        @DisplayName("should represent YAML bomb attack prevention")
        void shouldRepresentYamlBombAttackPrevention() {
            YmlSecurityException exception = YmlSecurityException.aliasLimitExceeded(10000, 100);

            assertThat(exception.getMessage()).contains("YAML bomb");
            assertThat(exception.getType()).isEqualTo(SecurityViolationType.ALIAS_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("should represent denial of service prevention")
        void shouldRepresentDenialOfServicePrevention() {
            YmlSecurityException exception = YmlSecurityException.nestingDepthExceeded(1000, 100);

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.NESTING_DEPTH_EXCEEDED);
        }

        @Test
        @DisplayName("should represent deserialization attack prevention")
        void shouldRepresentDeserializationAttackPrevention() {
            YmlSecurityException exception = YmlSecurityException.forbiddenType("java.lang.ProcessBuilder");

            assertThat(exception.getMessage()).contains("not allowed");
            assertThat(exception.getType()).isEqualTo(SecurityViolationType.FORBIDDEN_TYPE);
        }

        @Test
        @DisplayName("should represent resource exhaustion prevention")
        void shouldRepresentResourceExhaustionPrevention() {
            YmlSecurityException exception = YmlSecurityException.documentSizeExceeded(
                100_000_000L, 10_000_000L);

            assertThat(exception.getType()).isEqualTo(SecurityViolationType.DOCUMENT_SIZE_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty type name in forbiddenType")
        void shouldHandleEmptyTypeNameInForbiddenType() {
            YmlSecurityException exception = YmlSecurityException.forbiddenType("");

            assertThat(exception.getMessage()).contains("''");
        }

        @Test
        @DisplayName("should handle zero limits")
        void shouldHandleZeroLimits() {
            YmlSecurityException exception = YmlSecurityException.aliasLimitExceeded(1, 0);

            assertThat(exception.getMessage()).contains("exceeds limit 0");
        }

        @Test
        @DisplayName("should handle equal count and limit")
        void shouldHandleEqualCountAndLimit() {
            YmlSecurityException exception = YmlSecurityException.nestingDepthExceeded(100, 100);

            assertThat(exception.getMessage()).contains("100 exceeds limit 100");
        }

        @Test
        @DisplayName("should handle very long type name")
        void shouldHandleVeryLongTypeName() {
            String longTypeName = "com." + "very.".repeat(100) + "LongClassName";
            YmlSecurityException exception = YmlSecurityException.forbiddenType(longTypeName);

            assertThat(exception.getMessage()).contains(longTypeName);
        }

        @Test
        @DisplayName("should handle chained causes")
        void shouldHandleChainedCauses() {
            Throwable rootCause = new SecurityException("Access denied");
            Throwable intermediateCause = new RuntimeException("Wrapper", rootCause);
            YmlSecurityException exception = new YmlSecurityException(
                SecurityViolationType.FORBIDDEN_TYPE, "Security error", intermediateCause);

            assertThat(exception.getCause()).isEqualTo(intermediateCause);
            assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
        }
    }
}
