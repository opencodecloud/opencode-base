package cloud.opencode.base.classloader.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for BytecodeVerifier functional interface
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("BytecodeVerifier Tests")
class BytecodeVerifierTest {

    @Nested
    @DisplayName("Functional Interface Tests")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Should work as lambda returning true")
        void shouldWorkAsLambdaReturningTrue() {
            BytecodeVerifier verifier = bytecode -> true;
            assertThat(verifier.verify(new byte[]{1, 2, 3})).isTrue();
        }

        @Test
        @DisplayName("Should work as lambda returning false")
        void shouldWorkAsLambdaReturningFalse() {
            BytecodeVerifier verifier = bytecode -> false;
            assertThat(verifier.verify(new byte[]{1, 2, 3})).isFalse();
        }

        @Test
        @DisplayName("Should work with method reference")
        void shouldWorkWithMethodReference() {
            BytecodeVerifier verifier = BytecodeVerifierTest::checkMagicNumber;
            // Valid Java class magic number 0xCAFEBABE
            byte[] valid = {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
            assertThat(verifier.verify(valid)).isTrue();

            byte[] invalid = {0, 0, 0, 0};
            assertThat(verifier.verify(invalid)).isFalse();
        }

        @Test
        @DisplayName("Should accept empty bytecode array")
        void shouldAcceptEmptyBytecodeArray() {
            BytecodeVerifier verifier = bytecode -> bytecode.length == 0;
            assertThat(verifier.verify(new byte[0])).isTrue();
        }

        @Test
        @DisplayName("Should work with size-based verification")
        void shouldWorkWithSizeBasedVerification() {
            BytecodeVerifier verifier = bytecode -> bytecode.length <= 1024;
            assertThat(verifier.verify(new byte[512])).isTrue();
            assertThat(verifier.verify(new byte[2048])).isFalse();
        }
    }

    @Nested
    @DisplayName("Composition Tests")
    class CompositionTests {

        @Test
        @DisplayName("Should support chaining via and-logic")
        void shouldSupportAndChaining() {
            BytecodeVerifier sizeCheck = bytecode -> bytecode.length > 0;
            BytecodeVerifier contentCheck = bytecode -> bytecode[0] != 0;

            // Manual composition
            BytecodeVerifier combined = bytecode -> sizeCheck.verify(bytecode) && contentCheck.verify(bytecode);

            assertThat(combined.verify(new byte[]{1})).isTrue();
            assertThat(combined.verify(new byte[]{0})).isFalse();
            assertThat(combined.verify(new byte[0])).isFalse();
        }
    }

    /**
     * Helper: checks for Java class file magic number
     */
    private static boolean checkMagicNumber(byte[] bytecode) {
        return bytecode.length >= 4
                && bytecode[0] == (byte) 0xCA
                && bytecode[1] == (byte) 0xFE
                && bytecode[2] == (byte) 0xBA
                && bytecode[3] == (byte) 0xBE;
    }
}
