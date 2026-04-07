package cloud.opencode.base.crypto.versioned;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link VersionedPayload}.
 */
class VersionedPayloadTest {

    @Nested
    @DisplayName("Serialization Round-Trip")
    class SerializationTests {

        @Test
        @DisplayName("serialize and deserialize round-trip preserves data")
        void roundTrip() {
            byte[] ciphertext = {1, 2, 3, 4, 5};
            VersionedPayload original = new VersionedPayload(1, "AES-256-GCM", ciphertext);
            byte[] serialized = original.serialize();
            VersionedPayload restored = VersionedPayload.deserialize(serialized);

            assertThat(restored.version()).isEqualTo(1);
            assertThat(restored.algorithm()).isEqualTo("AES-256-GCM");
            assertThat(restored.ciphertext()).isEqualTo(ciphertext);
        }

        @Test
        @DisplayName("round-trip with empty ciphertext")
        void roundTripEmptyCiphertext() {
            VersionedPayload original = new VersionedPayload(0, "NONE", new byte[0]);
            byte[] serialized = original.serialize();
            VersionedPayload restored = VersionedPayload.deserialize(serialized);

            assertThat(restored.version()).isEqualTo(0);
            assertThat(restored.algorithm()).isEqualTo("NONE");
            assertThat(restored.ciphertext()).isEmpty();
        }

        @Test
        @DisplayName("round-trip with large ciphertext")
        void roundTripLargeCiphertext() {
            byte[] large = new byte[10_000];
            for (int i = 0; i < large.length; i++) {
                large[i] = (byte) (i % 256);
            }
            VersionedPayload original = new VersionedPayload(42, "ChaCha20", large);
            byte[] serialized = original.serialize();
            VersionedPayload restored = VersionedPayload.deserialize(serialized);

            assertThat(restored.version()).isEqualTo(42);
            assertThat(restored.algorithm()).isEqualTo("ChaCha20");
            assertThat(restored.ciphertext()).isEqualTo(large);
        }

        @Test
        @DisplayName("serialization format is correct")
        void serializationFormat() {
            byte[] ciphertext = {0x10, 0x20};
            VersionedPayload payload = new VersionedPayload(5, "AES", ciphertext);
            byte[] serialized = payload.serialize();

            assertThat(serialized[0]).isEqualTo((byte) 5);  // version
            assertThat(serialized[1]).isEqualTo((byte) 3);  // "AES" length
            assertThat(serialized[2]).isEqualTo((byte) 'A');
            assertThat(serialized[3]).isEqualTo((byte) 'E');
            assertThat(serialized[4]).isEqualTo((byte) 'S');
            assertThat(serialized[5]).isEqualTo((byte) 0x10);
            assertThat(serialized[6]).isEqualTo((byte) 0x20);
        }
    }

    @Nested
    @DisplayName("Version Boundaries")
    class VersionBoundaryTests {

        @Test
        @DisplayName("version 0 is valid")
        void versionZero() {
            VersionedPayload payload = new VersionedPayload(0, "AES", new byte[]{1});
            byte[] serialized = payload.serialize();
            VersionedPayload restored = VersionedPayload.deserialize(serialized);
            assertThat(restored.version()).isEqualTo(0);
        }

        @Test
        @DisplayName("version 255 is valid")
        void version255() {
            VersionedPayload payload = new VersionedPayload(255, "AES", new byte[]{1});
            byte[] serialized = payload.serialize();
            VersionedPayload restored = VersionedPayload.deserialize(serialized);
            assertThat(restored.version()).isEqualTo(255);
        }

        @Test
        @DisplayName("version -1 is invalid")
        void versionNegative() {
            assertThatThrownBy(() -> new VersionedPayload(-1, "AES", new byte[]{1}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("-1");
        }

        @Test
        @DisplayName("version 256 is invalid")
        void version256() {
            assertThatThrownBy(() -> new VersionedPayload(256, "AES", new byte[]{1}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("256");
        }
    }

    @Nested
    @DisplayName("Invalid Data")
    class InvalidDataTests {

        @Test
        @DisplayName("null data throws NullPointerException")
        void nullData() {
            assertThatThrownBy(() -> VersionedPayload.deserialize(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("empty data throws OpenCryptoException")
        void emptyData() {
            assertThatThrownBy(() -> VersionedPayload.deserialize(new byte[0]))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("too short");
        }

        @Test
        @DisplayName("single byte data throws OpenCryptoException")
        void singleByte() {
            assertThatThrownBy(() -> VersionedPayload.deserialize(new byte[]{1}))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("too short");
        }

        @Test
        @DisplayName("data shorter than declared algorithm length throws")
        void truncatedAlgorithm() {
            // version=1, algLen=10, but only 3 bytes total
            byte[] data = {1, 10, 65};
            assertThatThrownBy(() -> VersionedPayload.deserialize(data))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("too short");
        }

        @Test
        @DisplayName("algorithm name length 0 throws")
        void zeroAlgorithmLength() {
            byte[] data = {1, 0, 65};
            assertThatThrownBy(() -> VersionedPayload.deserialize(data))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("length is 0");
        }

        @Test
        @DisplayName("null algorithm throws NullPointerException")
        void nullAlgorithm() {
            assertThatThrownBy(() -> new VersionedPayload(1, null, new byte[]{1}))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("empty algorithm throws IllegalArgumentException")
        void emptyAlgorithm() {
            assertThatThrownBy(() -> new VersionedPayload(1, "", new byte[]{1}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null ciphertext throws NullPointerException")
        void nullCiphertext() {
            assertThatThrownBy(() -> new VersionedPayload(1, "AES", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Defensive Copies")
    class DefensiveCopyTests {

        @Test
        @DisplayName("constructor makes defensive copy of ciphertext")
        void constructorDefensiveCopy() {
            byte[] original = {1, 2, 3};
            VersionedPayload payload = new VersionedPayload(1, "AES", original);
            original[0] = 99;
            assertThat(payload.ciphertext()[0]).isEqualTo((byte) 1);
        }

        @Test
        @DisplayName("ciphertext() returns defensive copy")
        void ciphertextDefensiveCopy() {
            VersionedPayload payload = new VersionedPayload(1, "AES", new byte[]{1, 2, 3});
            byte[] copy1 = payload.ciphertext();
            copy1[0] = 99;
            assertThat(payload.ciphertext()[0]).isEqualTo((byte) 1);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equal payloads are equal")
        void equalPayloads() {
            VersionedPayload a = new VersionedPayload(1, "AES", new byte[]{1, 2});
            VersionedPayload b = new VersionedPayload(1, "AES", new byte[]{1, 2});
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("different versions are not equal")
        void differentVersions() {
            VersionedPayload a = new VersionedPayload(1, "AES", new byte[]{1});
            VersionedPayload b = new VersionedPayload(2, "AES", new byte[]{1});
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("different algorithms are not equal")
        void differentAlgorithms() {
            VersionedPayload a = new VersionedPayload(1, "AES", new byte[]{1});
            VersionedPayload b = new VersionedPayload(1, "DES", new byte[]{1});
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("different ciphertexts are not equal")
        void differentCiphertexts() {
            VersionedPayload a = new VersionedPayload(1, "AES", new byte[]{1});
            VersionedPayload b = new VersionedPayload(1, "AES", new byte[]{2});
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("equals(null) returns false")
        void notEqualToNull() {
            VersionedPayload a = new VersionedPayload(1, "AES", new byte[]{1});
            assertThat(a).isNotEqualTo(null);
        }
    }
}
