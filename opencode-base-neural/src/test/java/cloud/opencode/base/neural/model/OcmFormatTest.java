package cloud.opencode.base.neural.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OcmFormat}
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("OcmFormat")
class OcmFormatTest {

    @Nested
    @DisplayName("Magic Number")
    class MagicNumber {

        @Test
        @DisplayName("should equal 0x4F434D00 ('OCM\\0' in ASCII)")
        void magicNumberValue() {
            assertThat(OcmFormat.MAGIC).isEqualTo(0x4F434D00);
        }

        @Test
        @DisplayName("should decode to 'OCM' followed by null byte")
        void magicNumberDecoding() {
            byte b0 = (byte) ((OcmFormat.MAGIC >> 24) & 0xFF);
            byte b1 = (byte) ((OcmFormat.MAGIC >> 16) & 0xFF);
            byte b2 = (byte) ((OcmFormat.MAGIC >> 8) & 0xFF);
            byte b3 = (byte) (OcmFormat.MAGIC & 0xFF);
            assertThat(b0).isEqualTo((byte) 'O');
            assertThat(b1).isEqualTo((byte) 'C');
            assertThat(b2).isEqualTo((byte) 'M');
            assertThat(b3).isEqualTo((byte) 0);
        }
    }

    @Nested
    @DisplayName("Version")
    class Version {

        @Test
        @DisplayName("major version should be 1")
        void majorVersion() {
            assertThat(OcmFormat.VERSION_MAJOR).isEqualTo((short) 1);
        }

        @Test
        @DisplayName("minor version should be 0")
        void minorVersion() {
            assertThat(OcmFormat.VERSION_MINOR).isEqualTo((short) 0);
        }
    }

    @Nested
    @DisplayName("Header Size")
    class HeaderSize {

        @Test
        @DisplayName("should be 64 bytes")
        void headerSizeIs64() {
            assertThat(OcmFormat.HEADER_SIZE).isEqualTo(64);
        }
    }
}
