package cloud.opencode.base.test.data;

import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * FakerTest Tests
 * FakerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("Faker Tests")
class FakerTest {

    @Nested
    @DisplayName("Person Names Tests")
    class PersonNamesTests {

        @Test
        @DisplayName("firstName should return non-blank name")
        void firstNameShouldReturnNonBlankName() {
            String name = Faker.firstName();
            assertThat(name).isNotBlank();
        }

        @Test
        @DisplayName("lastName should return non-blank name")
        void lastNameShouldReturnNonBlankName() {
            String name = Faker.lastName();
            assertThat(name).isNotBlank();
        }

        @Test
        @DisplayName("name should return full name with space")
        void nameShouldReturnFullNameWithSpace() {
            String name = Faker.name();
            assertThat(name).contains(" ");
            assertThat(name.split(" ")).hasSize(2);
        }

        @Test
        @DisplayName("chineseName should return Chinese characters")
        void chineseNameShouldReturnChineseCharacters() {
            String name = Faker.chineseName();
            assertThat(name).isNotBlank();
            assertThat(name.length()).isBetween(2, 4);
        }
    }

    @Nested
    @DisplayName("Contact Info Tests")
    class ContactInfoTests {

        @Test
        @DisplayName("email should return valid email format")
        void emailShouldReturnValidEmailFormat() {
            String email = Faker.email();
            assertThat(email).contains("@");
            assertThat(email).matches("[a-z.\\d]+@[a-z.\\d]+");
        }

        @Test
        @DisplayName("phone should return US phone format")
        void phoneShouldReturnUsPhoneFormat() {
            String phone = Faker.phone();
            assertThat(phone).matches("\\(\\d{3}\\) \\d{3}-\\d{4}");
        }

        @Test
        @DisplayName("chinesePhone should return 11 digit phone")
        void chinesePhoneShouldReturn11DigitPhone() {
            String phone = Faker.chinesePhone();
            assertThat(phone).hasSize(11);
            assertThat(phone).matches("1\\d{10}");
        }
    }

    @Nested
    @DisplayName("Address Tests")
    class AddressTests {

        @Test
        @DisplayName("city should return US city")
        void cityShouldReturnUsCity() {
            String city = Faker.city();
            assertThat(city).isNotBlank();
        }

        @Test
        @DisplayName("chineseCity should return Chinese city")
        void chineseCityShouldReturnChineseCity() {
            String city = Faker.chineseCity();
            assertThat(city).isNotBlank();
            assertThat(city).endsWith("市");
        }

        @Test
        @DisplayName("streetAddress should return address with number")
        void streetAddressShouldReturnAddressWithNumber() {
            String address = Faker.streetAddress();
            assertThat(address).matches("\\d+ .+");
        }

        @Test
        @DisplayName("chineseStreetAddress should return Chinese address")
        void chineseStreetAddressShouldReturnChineseAddress() {
            String address = Faker.chineseStreetAddress();
            assertThat(address).contains("路");
            assertThat(address).endsWith("号");
        }

        @Test
        @DisplayName("address should return full US address")
        void addressShouldReturnFullUsAddress() {
            String address = Faker.address();
            assertThat(address).contains(",");
        }

        @Test
        @DisplayName("chineseAddress should return full Chinese address")
        void chineseAddressShouldReturnFullChineseAddress() {
            String address = Faker.chineseAddress();
            assertThat(address).contains("市");
            assertThat(address).endsWith("号");
        }

        @Test
        @DisplayName("state should return 2 letter abbreviation")
        void stateShouldReturn2LetterAbbreviation() {
            String state = Faker.state();
            assertThat(state).hasSize(2);
            assertThat(state).matches("[A-Z]{2}");
        }

        @Test
        @DisplayName("zipCode should return 5 digit code")
        void zipCodeShouldReturn5DigitCode() {
            String zip = Faker.zipCode();
            assertThat(zip).hasSize(5);
            assertThat(zip).matches("\\d{5}");
        }

        @Test
        @DisplayName("chinesePostalCode should return 6 digit code")
        void chinesePostalCodeShouldReturn6DigitCode() {
            String code = Faker.chinesePostalCode();
            assertThat(code).hasSize(6);
            assertThat(code).matches("\\d{6}");
        }
    }

    @Nested
    @DisplayName("Company Tests")
    class CompanyTests {

        @Test
        @DisplayName("company should return company name with suffix")
        void companyShouldReturnCompanyNameWithSuffix() {
            String company = Faker.company();
            assertThat(company).isNotBlank();
            assertThat(company).contains(" ");
        }

        @Test
        @DisplayName("chineseCompany should return Chinese company name")
        void chineseCompanyShouldReturnChineseCompanyName() {
            String company = Faker.chineseCompany();
            assertThat(company).isNotBlank();
            assertThat(company).containsAnyOf("公司", "集团");
        }
    }

    @Nested
    @DisplayName("Internet Tests")
    class InternetTests {

        @Test
        @DisplayName("username should return lowercase name with numbers")
        void usernameShouldReturnLowercaseNameWithNumbers() {
            String username = Faker.username();
            assertThat(username).matches("[a-z]+\\d+");
        }

        @Test
        @DisplayName("domainName should return valid domain format")
        void domainNameShouldReturnValidDomainFormat() {
            String domain = Faker.domainName();
            assertThat(domain).matches("[a-z]+\\.(com|net|org|io)");
        }

        @Test
        @DisplayName("url should return valid URL format")
        void urlShouldReturnValidUrlFormat() {
            String url = Faker.url();
            assertThat(url).startsWith("https://www.");
            assertThat(url).contains("/");
        }

        @Test
        @DisplayName("ipv4 should return valid IP format")
        void ipv4ShouldReturnValidIpFormat() {
            String ip = Faker.ipv4();
            assertThat(ip).matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
        }
    }

    @Nested
    @DisplayName("Date Tests")
    class DateTests {

        @Test
        @DisplayName("pastDate should return date in past")
        void pastDateShouldReturnDateInPast() {
            LocalDate date = Faker.pastDate(5);
            assertThat(date).isBeforeOrEqualTo(LocalDate.now());
            assertThat(date).isAfterOrEqualTo(LocalDate.now().minusYears(5));
        }

        @Test
        @DisplayName("futureDate should return date in future")
        void futureDateShouldReturnDateInFuture() {
            LocalDate date = Faker.futureDate(5);
            assertThat(date).isAfterOrEqualTo(LocalDate.now());
            assertThat(date).isBeforeOrEqualTo(LocalDate.now().plusYears(5));
        }

        @Test
        @DisplayName("birthday should return date for given age range")
        void birthdayShouldReturnDateForGivenAgeRange() {
            LocalDate birthday = Faker.birthday(18, 65);
            LocalDate now = LocalDate.now();
            int age = now.getYear() - birthday.getYear();
            assertThat(age).isBetween(18, 65);
        }
    }

    @Nested
    @DisplayName("Text Tests")
    class TextTests {

        @Test
        @DisplayName("word should return non-blank word")
        void wordShouldReturnNonBlankWord() {
            String word = Faker.word();
            assertThat(word).isNotBlank();
            assertThat(word).matches("[a-z]+");
        }

        @Test
        @DisplayName("sentence should return sentence with specified word count")
        void sentenceShouldReturnSentenceWithSpecifiedWordCount() {
            String sentence = Faker.sentence(5);
            assertThat(sentence).endsWith(".");
            // First letter should be uppercase
            assertThat(Character.isUpperCase(sentence.charAt(0))).isTrue();
        }

        @Test
        @DisplayName("paragraph should return multiple sentences")
        void paragraphShouldReturnMultipleSentences() {
            String paragraph = Faker.paragraph(3);
            // Should contain at least 3 periods (for 3 sentences)
            long periodCount = paragraph.chars().filter(c -> c == '.').count();
            assertThat(periodCount).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Randomness Tests")
    class RandomnessTests {

        @Test
        @DisplayName("Should generate different values on consecutive calls")
        void shouldGenerateDifferentValuesOnConsecutiveCalls() {
            String name1 = Faker.name();
            String name2 = Faker.name();
            // Very unlikely to be the same twice in a row
            // Run multiple times to reduce false positives
            boolean allSame = true;
            for (int i = 0; i < 10; i++) {
                if (!Faker.name().equals(Faker.name())) {
                    allSame = false;
                    break;
                }
            }
            assertThat(allSame).isFalse();
        }
    }
}
