package cloud.opencode.base.captcha.interactive;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JigsawCaptchaGenerator}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
@DisplayName("JigsawCaptchaGenerator Tests")
class JigsawCaptchaGeneratorTest {

    private JigsawCaptchaGenerator generator;
    private CaptchaConfig defaultConfig;

    @BeforeEach
    void setUp() {
        generator = new JigsawCaptchaGenerator();
        defaultConfig = CaptchaConfig.builder()
                .width(300)
                .height(300)
                .expireTime(Duration.ofMinutes(5))
                .build();
    }

    @Nested
    @DisplayName("Generate Tests")
    class GenerateTests {

        @Test
        @DisplayName("should generate jigsaw CAPTCHA when default config")
        void should_generateJigsawCaptcha_whenDefaultConfig() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha).isNotNull();
            assertThat(captcha.id()).isNotNull().isNotEmpty();
            assertThat(captcha.type()).isEqualTo(CaptchaType.JIGSAW);
            assertThat(captcha.answer()).isNotNull().isNotEmpty();
            assertThat(captcha.createdAt()).isNotNull();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }

        @Test
        @DisplayName("should have nine pieces in metadata (3x3 grid)")
        @SuppressWarnings("unchecked")
        void should_haveNinePieces() {
            Captcha captcha = generator.generate(defaultConfig);

            List<String> pieces = (List<String>) captcha.metadata().get("pieces");
            assertThat(pieces).hasSize(9);
        }

        @Test
        @DisplayName("should have gridSize 3 in metadata")
        void should_haveGridSize() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata().get("gridSize")).isEqualTo(3);
        }

        @Test
        @DisplayName("should have answer that differs from identity permutation")
        void should_haveShuffledAnswer() {
            // The answer represents piece positions; identity would be "0,1,2,...,8"
            String identity = "0,1,2,3,4,5,6,7,8";
            boolean foundShuffled = false;
            for (int attempt = 0; attempt < 5; attempt++) {
                Captcha captcha = generator.generate(defaultConfig);
                if (!identity.equals(captcha.answer())) {
                    foundShuffled = true;
                    break;
                }
            }
            assertThat(foundShuffled)
                    .as("answer should differ from identity permutation")
                    .isTrue();
        }

        @Test
        @DisplayName("should not expose shuffledOrder in metadata")
        void should_notExposeShuffledOrder() {
            Captcha captcha = generator.generate(defaultConfig);
            assertThat(captcha.metadata().get("shuffledOrder")).isNull();
        }

        @Test
        @DisplayName("should have non-empty image data")
        void should_haveNonEmptyImageData() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.imageData()).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("should have comma-separated numeric answer")
        void should_haveCommaSeparatedAnswer() {
            Captcha captcha = generator.generate(defaultConfig);

            // Answer format: "pos0,pos1,pos2,...,pos8" (9 numbers for 3x3 grid)
            assertThat(captcha.answer()).matches("\\d+(,\\d+){8}");
        }
    }

    @Nested
    @DisplayName("Pieces Tests")
    class PiecesTests {

        @Test
        @DisplayName("should have valid Base64 encoded piece images")
        @SuppressWarnings("unchecked")
        void should_haveValidBase64Pieces() {
            Captcha captcha = generator.generate(defaultConfig);

            List<String> pieces = (List<String>) captcha.metadata().get("pieces");
            for (String piece : pieces) {
                assertThat(piece).isNotNull().isNotEmpty();
                // Verify each piece can be decoded as valid Base64
                assertThatCode(() -> Base64.getDecoder().decode(piece))
                        .doesNotThrowAnyException();
                byte[] decoded = Base64.getDecoder().decode(piece);
                assertThat(decoded).isNotEmpty();
            }
        }

        @Test
        @DisplayName("should have at least one displaced piece (answer differs from identity)")
        void should_haveAtLeastOneDisplacement() {
            Captcha captcha = generator.generate(defaultConfig);
            String[] parts = captcha.answer().split(",");
            boolean hasDisplacement = false;
            for (int i = 0; i < parts.length; i++) {
                if (Integer.parseInt(parts[i]) != i) {
                    hasDisplacement = true;
                    break;
                }
            }
            assertThat(hasDisplacement)
                    .as("at least one piece should be displaced from its original position")
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("Small Image Tests")
    class SmallImageTests {

        @Test
        @DisplayName("should generate jigsaw with small image (30x30, each piece 10x10)")
        @SuppressWarnings("unchecked")
        void should_generateJigsaw_when_smallImage() {
            CaptchaConfig smallConfig = CaptchaConfig.builder()
                    .width(30)
                    .height(30)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(smallConfig);

            assertThat(captcha).isNotNull();
            assertThat(captcha.type()).isEqualTo(CaptchaType.JIGSAW);

            List<String> pieces = (List<String>) captcha.metadata().get("pieces");
            assertThat(pieces).hasSize(9);

            // Each piece should be a valid Base64 string decodable as PNG
            for (String piece : pieces) {
                byte[] decoded = Base64.getDecoder().decode(piece);
                assertThat(decoded).isNotEmpty();
                // PNG magic bytes: 0x89 0x50 0x4E 0x47
                assertThat(decoded[0]).isEqualTo((byte) 0x89);
                assertThat(decoded[1]).isEqualTo((byte) 0x50); // 'P'
                assertThat(decoded[2]).isEqualTo((byte) 0x4E); // 'N'
                assertThat(decoded[3]).isEqualTo((byte) 0x47); // 'G'
            }
        }
    }

    @Nested
    @DisplayName("Answer Format Tests")
    class AnswerFormatTests {

        @Test
        @DisplayName("should have answer that is a valid position mapping")
        void should_haveValidPositionMapping_when_answerParsed() {
            Captcha captcha = generator.generate(defaultConfig);

            String answer = captcha.answer();
            String[] parts = answer.split(",");
            assertThat(parts).hasSize(9);

            // Each value should be a valid position index 0-8
            int[] positions = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                positions[i] = Integer.parseInt(parts[i]);
                assertThat(positions[i]).isBetween(0, 8);
            }

            // The positions should be a permutation (each value 0-8 appears exactly once)
            java.util.Arrays.sort(positions);
            for (int i = 0; i < 9; i++) {
                assertThat(positions[i]).isEqualTo(i);
            }
        }

        @Test
        @DisplayName("should have answer that is a valid permutation of 0..8")
        void should_haveValidPermutationAnswer() {
            Captcha captcha = generator.generate(defaultConfig);
            String answer = captcha.answer();
            String[] parts = answer.split(",");

            assertThat(parts).hasSize(9);

            int[] positions = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                positions[i] = Integer.parseInt(parts[i]);
            }
            // Must be a permutation: each value 0-8 appears exactly once
            java.util.Arrays.sort(positions);
            for (int i = 0; i < 9; i++) {
                assertThat(positions[i]).isEqualTo(i);
            }
        }
    }

    @Nested
    @DisplayName("Pieces PNG Validation Tests")
    class PiecesPngValidationTests {

        @Test
        @DisplayName("should decode each piece as a valid PNG image")
        @SuppressWarnings("unchecked")
        void should_decodeAsValidPng_when_piecesDecoded() {
            Captcha captcha = generator.generate(defaultConfig);

            List<String> pieces = (List<String>) captcha.metadata().get("pieces");
            for (int i = 0; i < pieces.size(); i++) {
                byte[] decoded = Base64.getDecoder().decode(pieces.get(i));
                // Verify PNG magic bytes
                assertThat(decoded.length).as("piece %d should have non-trivial size", i).isGreaterThan(8);
                assertThat(decoded[0]).isEqualTo((byte) 0x89);
                assertThat(decoded[1]).isEqualTo((byte) 'P');
                assertThat(decoded[2]).isEqualTo((byte) 'N');
                assertThat(decoded[3]).isEqualTo((byte) 'G');
            }
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("should return JIGSAW type")
        void should_returnJigsawType() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.JIGSAW);
        }

        @Test
        @DisplayName("should be interactive type")
        void should_beInteractive() {
            assertThat(CaptchaType.JIGSAW.isInteractive()).isTrue();
        }
    }
}
