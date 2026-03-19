/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.captcha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for Captcha record
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("Captcha Tests")
class CaptchaTest {

    private static final String TEST_ID = "test-id-123";
    private static final String TEST_ANSWER = "ABC123";
    private static final byte[] TEST_IMAGE_DATA = "test-image-data".getBytes();

    private Captcha captcha;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        metadata = new HashMap<>();
        metadata.put("width", 160);
        metadata.put("height", 60);
        metadata.put("custom", "value");

        createdAt = Instant.now();
        expiresAt = createdAt.plusSeconds(300);

        captcha = new Captcha(
            TEST_ID,
            CaptchaType.ALPHANUMERIC,
            TEST_IMAGE_DATA,
            TEST_ANSWER,
            metadata,
            createdAt,
            expiresAt
        );
    }

    @Nested
    @DisplayName("Record Component Tests")
    class RecordComponentTests {

        @Test
        @DisplayName("id returns correct value")
        void idReturnsCorrectValue() {
            assertThat(captcha.id()).isEqualTo(TEST_ID);
        }

        @Test
        @DisplayName("type returns correct value")
        void typeReturnsCorrectValue() {
            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("imageData returns correct value")
        void imageDataReturnsCorrectValue() {
            assertThat(captcha.imageData()).isEqualTo(TEST_IMAGE_DATA);
        }

        @Test
        @DisplayName("answer returns correct value")
        void answerReturnsCorrectValue() {
            assertThat(captcha.answer()).isEqualTo(TEST_ANSWER);
        }

        @Test
        @DisplayName("metadata returns correct map")
        void metadataReturnsCorrectMap() {
            assertThat(captcha.metadata())
                .containsEntry("width", 160)
                .containsEntry("height", 60)
                .containsEntry("custom", "value");
        }

        @Test
        @DisplayName("metadata returns map with expected size")
        void metadataReturnsMapWithExpectedSize() {
            assertThat(captcha.metadata()).hasSize(3);
        }

        @Test
        @DisplayName("createdAt returns correct timestamp")
        void createdAtReturnsCorrectTimestamp() {
            assertThat(captcha.createdAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("createdAt is before or equal to now")
        void createdAtIsBeforeOrEqualToNow() {
            assertThat(captcha.createdAt()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("expiresAt returns correct value")
        void expiresAtReturnsCorrectValue() {
            assertThat(captcha.expiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("expiresAt is after createdAt")
        void expiresAtIsAfterCreatedAt() {
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }

        @Test
        @DisplayName("all components are accessible")
        void allComponentsAreAccessible() {
            assertThat(captcha.id()).isNotNull();
            assertThat(captcha.type()).isNotNull();
            assertThat(captcha.imageData()).isNotNull();
            assertThat(captcha.answer()).isNotNull();
            assertThat(captcha.metadata()).isNotNull();
            assertThat(captcha.createdAt()).isNotNull();
            assertThat(captcha.expiresAt()).isNotNull();
        }

        @Test
        @DisplayName("captcha can be created with null metadata")
        void captchaCanBeCreatedWithNullMetadata() {
            Captcha nullMetaCaptcha = new Captcha(
                TEST_ID, CaptchaType.NUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(nullMetaCaptcha.metadata()).isNull();
        }

        @Test
        @DisplayName("captcha can be created with empty metadata")
        void captchaCanBeCreatedWithEmptyMetadata() {
            Captcha emptyMetaCaptcha = new Captcha(
                TEST_ID, CaptchaType.NUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                Map.of(), Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(emptyMetaCaptcha.metadata()).isEmpty();
        }

        @Test
        @DisplayName("captcha can be created with each CaptchaType")
        void captchaCanBeCreatedWithEachType() {
            for (CaptchaType type : CaptchaType.values()) {
                Captcha typedCaptcha = new Captcha(
                    TEST_ID, type, TEST_IMAGE_DATA, TEST_ANSWER,
                    null, Instant.now(), Instant.now().plusSeconds(300)
                );
                assertThat(typedCaptcha.type()).isEqualTo(type);
            }
        }
    }

    @Nested
    @DisplayName("toBase64 Tests")
    class ToBase64Tests {

        @Test
        @DisplayName("toBase64 encodes image data correctly")
        void toBase64EncodesImageDataCorrectly() {
            String base64 = captcha.toBase64();

            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
            assertThat(Base64.getDecoder().decode(base64)).isEqualTo(TEST_IMAGE_DATA);
        }

        @Test
        @DisplayName("toBase64 returns consistent value on multiple calls")
        void toBase64ReturnsConsistentValue() {
            String base64First = captcha.toBase64();
            String base64Second = captcha.toBase64();

            assertThat(base64First).isEqualTo(base64Second);
        }

        @Test
        @DisplayName("toBase64 handles empty image data")
        void toBase64HandlesEmptyImageData() {
            Captcha emptyImageCaptcha = new Captcha(
                TEST_ID, CaptchaType.NUMERIC, new byte[0], TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            String base64 = emptyImageCaptcha.toBase64();

            assertThat(base64).isEmpty();
        }

        @Test
        @DisplayName("toBase64 produces valid base64 string")
        void toBase64ProducesValidBase64String() {
            String base64 = captcha.toBase64();

            assertThatCode(() -> Base64.getDecoder().decode(base64))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("toBase64 handles single byte image data")
        void toBase64HandlesSingleByteImageData() {
            Captcha singleByteCaptcha = new Captcha(
                TEST_ID, CaptchaType.NUMERIC, new byte[]{42}, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            String base64 = singleByteCaptcha.toBase64();

            assertThat(base64).isNotEmpty();
            assertThat(Base64.getDecoder().decode(base64)).containsExactly(42);
        }

        @Test
        @DisplayName("toBase64 handles large image data")
        void toBase64HandlesLargeImageData() {
            byte[] largeData = new byte[10000];
            for (int i = 0; i < largeData.length; i++) {
                largeData[i] = (byte) (i % 256);
            }

            Captcha largeCaptcha = new Captcha(
                TEST_ID, CaptchaType.NUMERIC, largeData, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            String base64 = largeCaptcha.toBase64();

            assertThat(base64).isNotEmpty();
            assertThat(Base64.getDecoder().decode(base64)).isEqualTo(largeData);
        }
    }

    @Nested
    @DisplayName("toBase64DataUrl Tests")
    class ToBase64DataUrlTests {

        @Test
        @DisplayName("toBase64DataUrl returns PNG data URL for non-GIF types")
        void toBase64DataUrlReturnsPngForNonGif() {
            String dataUrl = captcha.toBase64DataUrl();

            assertThat(dataUrl).startsWith("data:image/png;base64,");
            assertThat(dataUrl).contains(captcha.toBase64());
        }

        @Test
        @DisplayName("toBase64DataUrl returns GIF data URL for GIF type")
        void toBase64DataUrlReturnsGifForGifType() {
            Captcha gifCaptcha = new Captcha(
                TEST_ID, CaptchaType.GIF, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            String dataUrl = gifCaptcha.toBase64DataUrl();

            assertThat(dataUrl).startsWith("data:image/gif;base64,");
        }

        @Test
        @DisplayName("toBase64DataUrl format matches data URL pattern")
        void toBase64DataUrlFormatIsCorrect() {
            String dataUrl = captcha.toBase64DataUrl();

            assertThat(dataUrl).matches("data:image/(png|gif);base64,.+");
        }

        @Test
        @DisplayName("toBase64DataUrl contains correct base64 content after prefix")
        void toBase64DataUrlContainsCorrectContent() {
            String dataUrl = captcha.toBase64DataUrl();
            String expectedPrefix = "data:image/png;base64,";

            assertThat(dataUrl).startsWith(expectedPrefix);
            String base64Part = dataUrl.substring(expectedPrefix.length());
            assertThat(Base64.getDecoder().decode(base64Part)).isEqualTo(TEST_IMAGE_DATA);
        }

        @Test
        @DisplayName("toBase64DataUrl for NUMERIC type uses image/png")
        void toBase64DataUrlForNumericUsesPng() {
            Captcha numericCaptcha = new Captcha(
                TEST_ID, CaptchaType.NUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(numericCaptcha.toBase64DataUrl()).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("toBase64DataUrl for ALPHA type uses image/png")
        void toBase64DataUrlForAlphaUsesPng() {
            Captcha alphaCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHA, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(alphaCaptcha.toBase64DataUrl()).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("toBase64DataUrl for ARITHMETIC type uses image/png")
        void toBase64DataUrlForArithmeticUsesPng() {
            Captcha arithmeticCaptcha = new Captcha(
                TEST_ID, CaptchaType.ARITHMETIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(arithmeticCaptcha.toBase64DataUrl()).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("toBase64DataUrl for SLIDER type uses image/png")
        void toBase64DataUrlForSliderUsesPng() {
            Captcha sliderCaptcha = new Captcha(
                TEST_ID, CaptchaType.SLIDER, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(sliderCaptcha.toBase64DataUrl()).startsWith("data:image/png;base64,");
        }
    }

    @Nested
    @DisplayName("getMimeType Tests")
    class GetMimeTypeTests {

        @Test
        @DisplayName("getMimeType returns image/png for NUMERIC type")
        void getMimeTypeReturnsPngForNumeric() {
            Captcha numericCaptcha = new Captcha(
                TEST_ID, CaptchaType.NUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(numericCaptcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("getMimeType returns image/png for ALPHA type")
        void getMimeTypeReturnsPngForAlpha() {
            Captcha alphaCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHA, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(alphaCaptcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("getMimeType returns image/png for ALPHANUMERIC type")
        void getMimeTypeReturnsPngForAlphanumeric() {
            assertThat(captcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("getMimeType returns image/gif for GIF type")
        void getMimeTypeReturnsGifForGifType() {
            Captcha gifCaptcha = new Captcha(
                TEST_ID, CaptchaType.GIF, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(gifCaptcha.getMimeType()).isEqualTo("image/gif");
        }

        @Test
        @DisplayName("getMimeType returns image/png for ARITHMETIC type")
        void getMimeTypeReturnsPngForArithmetic() {
            Captcha arithmeticCaptcha = new Captcha(
                TEST_ID, CaptchaType.ARITHMETIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(arithmeticCaptcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("getMimeType returns image/png for CHINESE type")
        void getMimeTypeReturnsPngForChinese() {
            Captcha chineseCaptcha = new Captcha(
                TEST_ID, CaptchaType.CHINESE, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(chineseCaptcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("getMimeType returns image/png for SLIDER type")
        void getMimeTypeReturnsPngForSlider() {
            Captcha sliderCaptcha = new Captcha(
                TEST_ID, CaptchaType.SLIDER, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(sliderCaptcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("getMimeType returns image/png for CLICK type")
        void getMimeTypeReturnsPngForClick() {
            Captcha clickCaptcha = new Captcha(
                TEST_ID, CaptchaType.CLICK, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(clickCaptcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("getMimeType returns image/png for ROTATE type")
        void getMimeTypeReturnsPngForRotate() {
            Captcha rotateCaptcha = new Captcha(
                TEST_ID, CaptchaType.ROTATE, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(rotateCaptcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("getMimeType returns image/png for IMAGE_SELECT type")
        void getMimeTypeReturnsPngForImageSelect() {
            Captcha imageSelectCaptcha = new Captcha(
                TEST_ID, CaptchaType.IMAGE_SELECT, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(imageSelectCaptcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("only GIF type returns image/gif mime type")
        void onlyGifTypeReturnsGifMimeType() {
            for (CaptchaType type : CaptchaType.values()) {
                Captcha typedCaptcha = new Captcha(
                    TEST_ID, type, TEST_IMAGE_DATA, TEST_ANSWER,
                    null, Instant.now(), Instant.now().plusSeconds(300)
                );

                if (type == CaptchaType.GIF) {
                    assertThat(typedCaptcha.getMimeType()).isEqualTo("image/gif");
                } else {
                    assertThat(typedCaptcha.getMimeType()).isEqualTo("image/png");
                }
            }
        }
    }

    @Nested
    @DisplayName("isExpired Tests")
    class IsExpiredTests {

        @Test
        @DisplayName("isExpired returns false for non-expired captcha")
        void isExpiredReturnsFalseForNonExpired() {
            assertThat(captcha.isExpired()).isFalse();
        }

        @Test
        @DisplayName("isExpired returns true for expired captcha")
        void isExpiredReturnsTrueForExpired() {
            Captcha expiredCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now().minusSeconds(600), Instant.now().minusSeconds(300)
            );

            assertThat(expiredCaptcha.isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpired returns true when just past expiry")
        void isExpiredReturnsTrueWhenJustPastExpiry() {
            Captcha justExpiredCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now().minusSeconds(1), Instant.now().minusMillis(1)
            );

            assertThat(justExpiredCaptcha.isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpired returns false for captcha expiring far in the future")
        void isExpiredReturnsFalseForFarFutureExpiry() {
            Captcha futureCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(3600)
            );

            assertThat(futureCaptcha.isExpired()).isFalse();
        }

        @Test
        @DisplayName("isExpired returns true for long-ago expired captcha")
        void isExpiredReturnsTrueForLongAgoExpired() {
            Captcha longAgoExpired = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.EPOCH, Instant.EPOCH.plusSeconds(300)
            );

            assertThat(longAgoExpired.isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpired returns false for max future expiry")
        void isExpiredReturnsFalseForMaxFutureExpiry() {
            Captcha maxFutureCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(999999)
            );

            assertThat(maxFutureCaptcha.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("getMetadata Tests")
    class GetMetadataTests {

        @Test
        @DisplayName("getMetadata returns correct Integer value")
        void getMetadataReturnsCorrectIntegerValue() {
            Integer width = captcha.getMetadata("width");

            assertThat(width).isEqualTo(160);
        }

        @Test
        @DisplayName("getMetadata returns correct String value")
        void getMetadataReturnsCorrectStringValue() {
            String custom = captcha.getMetadata("custom");

            assertThat(custom).isEqualTo("value");
        }

        @Test
        @DisplayName("getMetadata returns null for non-existing key")
        void getMetadataReturnsNullForNonExistingKey() {
            Object value = captcha.getMetadata("nonexistent");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("getMetadata returns null when metadata map is null")
        void getMetadataReturnsNullWhenMetadataIsNull() {
            Captcha nullMetadataCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            Object value = nullMetadataCaptcha.getMetadata("width");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("getMetadata returns null for empty metadata map")
        void getMetadataReturnsNullForEmptyMetadataMap() {
            Captcha emptyMetaCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                Map.of(), Instant.now(), Instant.now().plusSeconds(300)
            );

            Object value = emptyMetaCaptcha.getMetadata("width");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("getMetadata supports generic type casting")
        void getMetadataSupportsGenericTypeCasting() {
            Map<String, Object> richMetadata = new HashMap<>();
            richMetadata.put("count", 42);
            richMetadata.put("label", "test");
            richMetadata.put("ratio", 3.14);

            Captcha richCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                richMetadata, Instant.now(), Instant.now().plusSeconds(300)
            );

            Integer count = richCaptcha.getMetadata("count");
            String label = richCaptcha.getMetadata("label");
            Double ratio = richCaptcha.getMetadata("ratio");

            assertThat(count).isEqualTo(42);
            assertThat(label).isEqualTo("test");
            assertThat(ratio).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("getWidth Tests")
    class GetWidthTests {

        @Test
        @DisplayName("getWidth returns width from metadata")
        void getWidthReturnsWidthFromMetadata() {
            assertThat(captcha.getWidth()).isEqualTo(160);
        }

        @Test
        @DisplayName("getWidth returns 0 when width not in metadata")
        void getWidthReturnsZeroWhenNotInMetadata() {
            Map<String, Object> noWidthMetadata = new HashMap<>();
            noWidthMetadata.put("height", 60);

            Captcha noWidthCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                noWidthMetadata, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(noWidthCaptcha.getWidth()).isZero();
        }

        @Test
        @DisplayName("getWidth returns 0 when metadata is null")
        void getWidthReturnsZeroWhenMetadataIsNull() {
            Captcha nullMetadataCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(nullMetadataCaptcha.getWidth()).isZero();
        }

        @Test
        @DisplayName("getWidth returns custom width value")
        void getWidthReturnsCustomWidthValue() {
            Map<String, Object> customMeta = Map.of("width", 320);
            Captcha customCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                customMeta, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(customCaptcha.getWidth()).isEqualTo(320);
        }
    }

    @Nested
    @DisplayName("getHeight Tests")
    class GetHeightTests {

        @Test
        @DisplayName("getHeight returns height from metadata")
        void getHeightReturnsHeightFromMetadata() {
            assertThat(captcha.getHeight()).isEqualTo(60);
        }

        @Test
        @DisplayName("getHeight returns 0 when height not in metadata")
        void getHeightReturnsZeroWhenNotInMetadata() {
            Map<String, Object> noHeightMetadata = new HashMap<>();
            noHeightMetadata.put("width", 160);

            Captcha noHeightCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                noHeightMetadata, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(noHeightCaptcha.getHeight()).isZero();
        }

        @Test
        @DisplayName("getHeight returns 0 when metadata is null")
        void getHeightReturnsZeroWhenMetadataIsNull() {
            Captcha nullMetadataCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(nullMetadataCaptcha.getHeight()).isZero();
        }

        @Test
        @DisplayName("getHeight returns custom height value")
        void getHeightReturnsCustomHeightValue() {
            Map<String, Object> customMeta = Map.of("height", 120);
            Captcha customCaptcha = new Captcha(
                TEST_ID, CaptchaType.ALPHANUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                customMeta, Instant.now(), Instant.now().plusSeconds(300)
            );

            assertThat(customCaptcha.getHeight()).isEqualTo(120);
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class RecordEqualityTests {

        @Test
        @DisplayName("equals returns true for same byte array reference")
        void equalsReturnsTrueForSameByteArrayReference() {
            byte[] sharedImageData = new byte[]{1, 2, 3};
            Captcha captcha1 = new Captcha(
                "id", CaptchaType.NUMERIC, sharedImageData, "123",
                Map.of("key", "value"), Instant.EPOCH, Instant.EPOCH.plusSeconds(300)
            );
            Captcha captcha2 = new Captcha(
                "id", CaptchaType.NUMERIC, sharedImageData, "123",
                Map.of("key", "value"), Instant.EPOCH, Instant.EPOCH.plusSeconds(300)
            );

            assertThat(captcha1).isEqualTo(captcha2);
        }

        @Test
        @DisplayName("equals returns false for different byte array instances with same content")
        void equalsReturnsFalseForDifferentByteArrayInstances() {
            // Records use reference equality for arrays
            Captcha captcha1 = new Captcha(
                "id", CaptchaType.NUMERIC, new byte[]{1, 2, 3}, "123",
                Map.of("key", "value"), Instant.EPOCH, Instant.EPOCH.plusSeconds(300)
            );
            Captcha captcha2 = new Captcha(
                "id", CaptchaType.NUMERIC, new byte[]{1, 2, 3}, "123",
                Map.of("key", "value"), Instant.EPOCH, Instant.EPOCH.plusSeconds(300)
            );

            // Different byte[] instances are not equal in records
            assertThat(captcha1).isNotEqualTo(captcha2);
        }

        @Test
        @DisplayName("equals returns false for different IDs")
        void equalsReturnsFalseForDifferentIds() {
            Instant now = Instant.now();
            Captcha captcha1 = new Captcha(
                "id1", CaptchaType.NUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, now, now.plusSeconds(300)
            );
            Captcha captcha2 = new Captcha(
                "id2", CaptchaType.NUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, now, now.plusSeconds(300)
            );

            assertThat(captcha1).isNotEqualTo(captcha2);
        }

        @Test
        @DisplayName("equals returns false for different types")
        void equalsReturnsFalseForDifferentTypes() {
            Instant now = Instant.now();
            Captcha captcha1 = new Captcha(
                "id", CaptchaType.NUMERIC, TEST_IMAGE_DATA, TEST_ANSWER,
                null, now, now.plusSeconds(300)
            );
            Captcha captcha2 = new Captcha(
                "id", CaptchaType.ALPHA, TEST_IMAGE_DATA, TEST_ANSWER,
                null, now, now.plusSeconds(300)
            );

            assertThat(captcha1).isNotEqualTo(captcha2);
        }

        @Test
        @DisplayName("equals returns false for different answers")
        void equalsReturnsFalseForDifferentAnswers() {
            Instant now = Instant.now();
            Captcha captcha1 = new Captcha(
                "id", CaptchaType.NUMERIC, TEST_IMAGE_DATA, "123",
                null, now, now.plusSeconds(300)
            );
            Captcha captcha2 = new Captcha(
                "id", CaptchaType.NUMERIC, TEST_IMAGE_DATA, "456",
                null, now, now.plusSeconds(300)
            );

            assertThat(captcha1).isNotEqualTo(captcha2);
        }

        @Test
        @DisplayName("equals returns false when compared with null")
        void equalsReturnsFalseWhenComparedWithNull() {
            assertThat(captcha).isNotEqualTo(null);
        }

        @Test
        @DisplayName("equals returns false when compared with different class")
        void equalsReturnsFalseWhenComparedWithDifferentClass() {
            assertThat(captcha).isNotEqualTo("not a captcha");
        }

        @Test
        @DisplayName("hashCode is consistent for equal objects with same byte array reference")
        void hashCodeIsConsistentForEqualObjects() {
            byte[] sharedImageData = new byte[]{1, 2, 3};
            Captcha captcha1 = new Captcha(
                "id", CaptchaType.NUMERIC, sharedImageData, "123",
                Map.of("key", "value"), Instant.EPOCH, Instant.EPOCH.plusSeconds(300)
            );
            Captcha captcha2 = new Captcha(
                "id", CaptchaType.NUMERIC, sharedImageData, "123",
                Map.of("key", "value"), Instant.EPOCH, Instant.EPOCH.plusSeconds(300)
            );

            assertThat(captcha1.hashCode()).isEqualTo(captcha2.hashCode());
        }

        @Test
        @DisplayName("hashCode is consistent across multiple calls")
        void hashCodeIsConsistentAcrossMultipleCalls() {
            int hash1 = captcha.hashCode();
            int hash2 = captcha.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("toString returns non-null string")
        void toStringReturnsNonNullString() {
            assertThat(captcha.toString()).isNotNull();
            assertThat(captcha.toString()).isNotEmpty();
        }

        @Test
        @DisplayName("toString contains record class name")
        void toStringContainsClassName() {
            assertThat(captcha.toString()).contains("Captcha");
        }
    }
}
