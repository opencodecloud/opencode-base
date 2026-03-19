package cloud.opencode.base.test.data;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SensitiveDataGeneratorTest Tests
 * SensitiveDataGeneratorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("SensitiveDataGenerator Tests")
class SensitiveDataGeneratorTest {

    @Nested
    @DisplayName("Test Phone Tests")
    class TestPhoneTests {

        @Test
        @DisplayName("testPhone should return 11 digit number")
        void testPhoneShouldReturn11DigitNumber() {
            String phone = SensitiveDataGenerator.testPhone();
            assertThat(phone).hasSize(11);
            assertThat(phone).matches("\\d{11}");
        }

        @Test
        @DisplayName("testPhone should start with 199")
        void testPhoneShouldStartWith199() {
            String phone = SensitiveDataGenerator.testPhone();
            assertThat(phone).startsWith("199");
        }

        @Test
        @DisplayName("testPhone with custom prefix should use that prefix")
        void testPhoneWithCustomPrefixShouldUseThatPrefix() {
            String phone = SensitiveDataGenerator.testPhone("138");
            assertThat(phone).hasSize(11);
            assertThat(phone).startsWith("138");
        }
    }

    @Nested
    @DisplayName("Test ID Card Tests")
    class TestIdCardTests {

        @Test
        @DisplayName("testIdCard should return 18 character ID")
        void testIdCardShouldReturn18CharacterId() {
            String idCard = SensitiveDataGenerator.testIdCard();
            assertThat(idCard).hasSize(18);
        }

        @Test
        @DisplayName("testIdCard should start with 999999")
        void testIdCardShouldStartWith999999() {
            String idCard = SensitiveDataGenerator.testIdCard();
            assertThat(idCard).startsWith("999999");
        }

        @Test
        @DisplayName("testIdCard should have valid birth date format")
        void testIdCardShouldHaveValidBirthDateFormat() {
            String idCard = SensitiveDataGenerator.testIdCard();
            String birthDate = idCard.substring(6, 14);
            // Should be 8 digits (YYYYMMDD)
            assertThat(birthDate).matches("\\d{8}");
            int year = Integer.parseInt(birthDate.substring(0, 4));
            int month = Integer.parseInt(birthDate.substring(4, 6));
            int day = Integer.parseInt(birthDate.substring(6, 8));
            assertThat(year).isBetween(1970, 2010);
            assertThat(month).isBetween(1, 12);
            assertThat(day).isBetween(1, 31);
        }

        @Test
        @DisplayName("testIdCard with birth year should use that year")
        void testIdCardWithBirthYearShouldUseThatYear() {
            String idCard = SensitiveDataGenerator.testIdCard(1990);
            String year = idCard.substring(6, 10);
            assertThat(year).isEqualTo("1990");
        }

        @Test
        @DisplayName("testIdCard should have valid check code")
        void testIdCardShouldHaveValidCheckCode() {
            String idCard = SensitiveDataGenerator.testIdCard();
            char checkCode = idCard.charAt(17);
            // Check code should be digit or 'X'
            assertThat(checkCode).matches(c ->
                Character.isDigit(c) || c == 'X');
        }
    }

    @Nested
    @DisplayName("Test Bank Card Tests")
    class TestBankCardTests {

        @Test
        @DisplayName("testBankCard should return 16 digit number")
        void testBankCardShouldReturn16DigitNumber() {
            String bankCard = SensitiveDataGenerator.testBankCard();
            assertThat(bankCard).hasSize(16);
            assertThat(bankCard).matches("\\d{16}");
        }

        @Test
        @DisplayName("testBankCard should start with 622848")
        void testBankCardShouldStartWith622848() {
            String bankCard = SensitiveDataGenerator.testBankCard();
            assertThat(bankCard).startsWith("622848");
        }

        @Test
        @DisplayName("testBankCard should have valid Luhn checksum")
        void testBankCardShouldHaveValidLuhnChecksum() {
            String bankCard = SensitiveDataGenerator.testBankCard();
            assertThat(isValidLuhn(bankCard)).isTrue();
        }

        @Test
        @DisplayName("testBankCard with custom BIN should use that BIN")
        void testBankCardWithCustomBinShouldUseThatBin() {
            String bankCard = SensitiveDataGenerator.testBankCard("438888");
            assertThat(bankCard).hasSize(16);
            assertThat(bankCard).startsWith("438888");
            assertThat(isValidLuhn(bankCard)).isTrue();
        }

        private boolean isValidLuhn(String number) {
            int sum = 0;
            boolean alternate = false;
            for (int i = number.length() - 1; i >= 0; i--) {
                int digit = number.charAt(i) - '0';
                if (alternate) {
                    digit *= 2;
                    if (digit > 9) {
                        digit -= 9;
                    }
                }
                sum += digit;
                alternate = !alternate;
            }
            return sum % 10 == 0;
        }
    }

    @Nested
    @DisplayName("Test Email Tests")
    class TestEmailTests {

        @Test
        @DisplayName("testEmail should return valid email format")
        void testEmailShouldReturnValidEmailFormat() {
            String email = SensitiveDataGenerator.testEmail();
            assertThat(email).contains("@");
            assertThat(email).endsWith("@test.example.com");
        }

        @Test
        @DisplayName("testEmail should have 8 character local part")
        void testEmailShouldHave8CharacterLocalPart() {
            String email = SensitiveDataGenerator.testEmail();
            String local = email.substring(0, email.indexOf('@'));
            assertThat(local).hasSize(8);
            assertThat(local).matches("[a-z]+");
        }

        @Test
        @DisplayName("testEmail with custom domain should use that domain")
        void testEmailWithCustomDomainShouldUseThatDomain() {
            String email = SensitiveDataGenerator.testEmail("custom.org");
            assertThat(email).endsWith("@custom.org");
        }
    }

    @Nested
    @DisplayName("Test Social Credit Code Tests")
    class TestSocialCreditCodeTests {

        @Test
        @DisplayName("testSocialCreditCode should return 19 character code")
        void testSocialCreditCodeShouldReturn19CharacterCode() {
            String code = SensitiveDataGenerator.testSocialCreditCode();
            assertThat(code).hasSize(19);
        }

        @Test
        @DisplayName("testSocialCreditCode should start with 91")
        void testSocialCreditCodeShouldStartWith91() {
            String code = SensitiveDataGenerator.testSocialCreditCode();
            assertThat(code).startsWith("91");
        }

        @Test
        @DisplayName("testSocialCreditCode should contain region code 999999")
        void testSocialCreditCodeShouldContainRegionCode999999() {
            String code = SensitiveDataGenerator.testSocialCreditCode();
            assertThat(code.substring(3, 9)).isEqualTo("999999");
        }

        @Test
        @DisplayName("testSocialCreditCode should be uppercase alphanumeric")
        void testSocialCreditCodeShouldBeUppercaseAlphanumeric() {
            String code = SensitiveDataGenerator.testSocialCreditCode();
            assertThat(code).matches("[0-9A-Z]+");
        }
    }

    @Nested
    @DisplayName("Uniqueness Tests")
    class UniquenessTests {

        @Test
        @DisplayName("Multiple calls should generate different values")
        void multipleCallsShouldGenerateDifferentValues() {
            String phone1 = SensitiveDataGenerator.testPhone();
            String phone2 = SensitiveDataGenerator.testPhone();
            // Very unlikely to be the same
            boolean foundDifferent = false;
            for (int i = 0; i < 10; i++) {
                if (!SensitiveDataGenerator.testPhone().equals(SensitiveDataGenerator.testPhone())) {
                    foundDifferent = true;
                    break;
                }
            }
            assertThat(foundDifferent).isTrue();
        }
    }
}
