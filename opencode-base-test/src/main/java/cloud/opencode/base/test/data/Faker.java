package cloud.opencode.base.test.data;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Faker - Fake data generator for realistic test data
 * Faker - 生成逼真测试数据的假数据生成器
 *
 * <p>Generates realistic-looking fake data for testing purposes.</p>
 * <p>为测试目的生成看起来逼真的假数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>English and Chinese personal data (names, phones, emails) - 英文和中文个人数据（姓名、电话、邮箱）</li>
 *   <li>Address data (US and Chinese) - 地址数据（美国和中国）</li>
 *   <li>Company and internet data (domains, URLs, IPs) - 公司和互联网数据（域名、URL、IP）</li>
 *   <li>Lorem ipsum text generation - Lorem ipsum文本生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Person data
 * String name = Faker.name();
 * String email = Faker.email();
 * String phone = Faker.phone();
 *
 * // Chinese data
 * String chineseName = Faker.chineseName();
 * String chinesePhone = Faker.chinesePhone();
 *
 * // Address data
 * String city = Faker.city();
 * String address = Faker.address();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ThreadLocalRandom) - 线程安全: 是（使用ThreadLocalRandom）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class Faker {

    // English names
    private static final String[] FIRST_NAMES = {
        "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda",
        "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
        "Thomas", "Sarah", "Charles", "Karen", "Christopher", "Nancy", "Daniel", "Lisa"
    };

    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
        "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White"
    };

    // Chinese names
    private static final String[] CHINESE_SURNAMES = {
        "王", "李", "张", "刘", "陈", "杨", "赵", "黄", "周", "吴",
        "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗"
    };

    private static final String[] CHINESE_GIVEN_NAMES = {
        "伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "军", "洋",
        "勇", "艳", "杰", "娟", "涛", "明", "超", "秀", "霞", "平",
        "刚", "桂", "英", "华", "建", "文", "辉", "力", "成", "慧"
    };

    // Email domains
    private static final String[] EMAIL_DOMAINS = {
        "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "icloud.com",
        "163.com", "qq.com", "126.com", "sina.com", "example.com"
    };

    // Chinese cities
    private static final String[] CHINESE_CITIES = {
        "北京市", "上海市", "广州市", "深圳市", "杭州市", "南京市", "苏州市",
        "成都市", "武汉市", "西安市", "重庆市", "天津市", "长沙市", "青岛市"
    };

    // US cities
    private static final String[] US_CITIES = {
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia",
        "San Antonio", "San Diego", "Dallas", "San Jose", "Austin", "Jacksonville"
    };

    // Street names
    private static final String[] STREET_NAMES = {
        "Main Street", "Oak Avenue", "Maple Drive", "Cedar Lane", "Park Road",
        "Washington Boulevard", "Lincoln Way", "Market Street", "First Avenue"
    };

    private static final String[] CHINESE_STREETS = {
        "中山路", "人民路", "解放路", "建设路", "和平路", "长江路", "南京路", "北京路"
    };

    // Company suffixes
    private static final String[] COMPANY_SUFFIXES = {
        "Inc.", "Corp.", "LLC", "Ltd.", "Group", "Technologies", "Solutions", "Systems"
    };

    private static final String[] CHINESE_COMPANY_SUFFIXES = {
        "有限公司", "股份有限公司", "科技有限公司", "集团", "实业有限公司"
    };

    private Faker() {
    }

    // ============ Person Names | 人名 ============

    /**
     * Generates random first name.
     * 生成随机名字。
     *
     * @return the first name | 名字
     */
    public static String firstName() {
        return oneOf(FIRST_NAMES);
    }

    /**
     * Generates random last name.
     * 生成随机姓氏。
     *
     * @return the last name | 姓氏
     */
    public static String lastName() {
        return oneOf(LAST_NAMES);
    }

    /**
     * Generates random full name.
     * 生成随机全名。
     *
     * @return the full name | 全名
     */
    public static String name() {
        return firstName() + " " + lastName();
    }

    /**
     * Generates random Chinese name.
     * 生成随机中文名。
     *
     * @return the Chinese name | 中文名
     */
    public static String chineseName() {
        String surname = oneOf(CHINESE_SURNAMES);
        String given = oneOf(CHINESE_GIVEN_NAMES);
        if (random().nextBoolean()) {
            given += oneOf(CHINESE_GIVEN_NAMES);
        }
        return surname + given;
    }

    // ============ Contact Info | 联系信息 ============

    /**
     * Generates random email.
     * 生成随机邮箱。
     *
     * @return the email | 邮箱
     */
    public static String email() {
        String domain = oneOf(EMAIL_DOMAINS);
        String local = firstName().toLowerCase() + "." + lastName().toLowerCase() + random().nextInt(100);
        return local + "@" + domain;
    }

    /**
     * Generates random phone number (US format).
     * 生成随机电话号码（美国格式）。
     *
     * @return the phone number | 电话号码
     */
    public static String phone() {
        var r = random();
        return String.format("(%03d) %03d-%04d",
            r.nextInt(200, 999),
            r.nextInt(200, 999),
            r.nextInt(0, 9999));
    }

    /**
     * Generates random Chinese mobile phone.
     * 生成随机中国手机号。
     *
     * @return the phone number | 手机号
     */
    public static String chinesePhone() {
        String[] prefixes = {"130", "131", "132", "133", "135", "136", "137", "138", "139",
            "150", "151", "152", "153", "155", "156", "157", "158", "159",
            "180", "181", "182", "183", "184", "185", "186", "187", "188", "189"};
        var r = random();
        StringBuilder sb = new StringBuilder(oneOf(prefixes));
        for (int i = 0; i < 8; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }

    // ============ Address | 地址 ============

    /**
     * Generates random city name.
     * 生成随机城市名。
     *
     * @return the city name | 城市名
     */
    public static String city() {
        return oneOf(US_CITIES);
    }

    /**
     * Generates random Chinese city name.
     * 生成随机中国城市名。
     *
     * @return the city name | 城市名
     */
    public static String chineseCity() {
        return oneOf(CHINESE_CITIES);
    }

    /**
     * Generates random street address.
     * 生成随机街道地址。
     *
     * @return the address | 地址
     */
    public static String streetAddress() {
        return random().nextInt(1, 9999) + " " + oneOf(STREET_NAMES);
    }

    /**
     * Generates random Chinese street address.
     * 生成随机中国街道地址。
     *
     * @return the address | 地址
     */
    public static String chineseStreetAddress() {
        return oneOf(CHINESE_STREETS) + random().nextInt(1, 999) + "号";
    }

    /**
     * Generates random full address.
     * 生成随机完整地址。
     *
     * @return the address | 地址
     */
    public static String address() {
        return streetAddress() + ", " + city() + ", " + state() + " " + zipCode();
    }

    /**
     * Generates random Chinese full address.
     * 生成随机中国完整地址。
     *
     * @return the address | 地址
     */
    public static String chineseAddress() {
        return chineseCity() + chineseStreetAddress();
    }

    /**
     * Generates random US state abbreviation.
     * 生成随机美国州缩写。
     *
     * @return the state | 州
     */
    public static String state() {
        String[] states = {"AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD"};
        return oneOf(states);
    }

    /**
     * Generates random ZIP code.
     * 生成随机邮政编码。
     *
     * @return the ZIP code | 邮政编码
     */
    public static String zipCode() {
        return String.format("%05d", random().nextInt(10000, 99999));
    }

    /**
     * Generates random Chinese postal code.
     * 生成随机中国邮政编码。
     *
     * @return the postal code | 邮政编码
     */
    public static String chinesePostalCode() {
        return String.format("%06d", random().nextInt(100000, 999999));
    }

    // ============ Company | 公司 ============

    /**
     * Generates random company name.
     * 生成随机公司名。
     *
     * @return the company name | 公司名
     */
    public static String company() {
        return lastName() + " " + oneOf(COMPANY_SUFFIXES);
    }

    /**
     * Generates random Chinese company name.
     * 生成随机中国公司名。
     *
     * @return the company name | 公司名
     */
    public static String chineseCompany() {
        String[] prefixes = {"中", "华", "国", "新", "创", "智", "恒", "盛", "鑫"};
        return oneOf(prefixes) + oneOf(CHINESE_GIVEN_NAMES) + oneOf(CHINESE_COMPANY_SUFFIXES);
    }

    // ============ Internet | 互联网 ============

    /**
     * Generates random username.
     * 生成随机用户名。
     *
     * @return the username | 用户名
     */
    public static String username() {
        return firstName().toLowerCase() + random().nextInt(100, 9999);
    }

    /**
     * Generates random domain name.
     * 生成随机域名。
     *
     * @return the domain name | 域名
     */
    public static String domainName() {
        return lastName().toLowerCase() + oneOf(".com", ".net", ".org", ".io");
    }

    /**
     * Generates random URL.
     * 生成随机URL。
     *
     * @return the URL | URL
     */
    public static String url() {
        return "https://www." + domainName() + "/" + firstName().toLowerCase();
    }

    /**
     * Generates random IPv4 address.
     * 生成随机IPv4地址。
     *
     * @return the IP address | IP地址
     */
    public static String ipv4() {
        var r = random();
        return r.nextInt(1, 255) + "." +
            r.nextInt(0, 255) + "." +
            r.nextInt(0, 255) + "." +
            r.nextInt(1, 255);
    }

    // ============ Date | 日期 ============

    /**
     * Generates random past date.
     * 生成随机过去日期。
     *
     * @param yearsBack years back from now | 距今的年数
     * @return the date | 日期
     */
    public static LocalDate pastDate(int yearsBack) {
        LocalDate start = LocalDate.now().minusYears(yearsBack);
        LocalDate end = LocalDate.now();
        long startDay = start.toEpochDay();
        long endDay = end.toEpochDay();
        return LocalDate.ofEpochDay(random().nextLong(startDay, endDay));
    }

    /**
     * Generates random future date.
     * 生成随机未来日期。
     *
     * @param yearsAhead years ahead from now | 距今的年数
     * @return the date | 日期
     */
    public static LocalDate futureDate(int yearsAhead) {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusYears(yearsAhead);
        long startDay = start.toEpochDay();
        long endDay = end.toEpochDay();
        return LocalDate.ofEpochDay(random().nextLong(startDay, endDay));
    }

    /**
     * Generates random birthday.
     * 生成随机生日。
     *
     * @param minAge minimum age | 最小年龄
     * @param maxAge maximum age | 最大年龄
     * @return the birthday | 生日
     */
    public static LocalDate birthday(int minAge, int maxAge) {
        LocalDate start = LocalDate.now().minusYears(maxAge);
        LocalDate end = LocalDate.now().minusYears(minAge);
        long startDay = start.toEpochDay();
        long endDay = end.toEpochDay();
        return LocalDate.ofEpochDay(random().nextLong(startDay, endDay));
    }

    // ============ Text | 文本 ============

    /**
     * Generates random word.
     * 生成随机单词。
     *
     * @return the word | 单词
     */
    public static String word() {
        String[] words = {"lorem", "ipsum", "dolor", "sit", "amet", "consectetur",
            "adipiscing", "elit", "sed", "do", "eiusmod", "tempor", "incididunt"};
        return oneOf(words);
    }

    /**
     * Generates random sentence.
     * 生成随机句子。
     *
     * @param wordCount word count | 单词数
     * @return the sentence | 句子
     */
    public static String sentence(int wordCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) sb.append(" ");
            sb.append(word());
        }
        String sentence = sb.toString();
        return Character.toUpperCase(sentence.charAt(0)) + sentence.substring(1) + ".";
    }

    /**
     * Generates random paragraph.
     * 生成随机段落。
     *
     * @param sentenceCount sentence count | 句子数
     * @return the paragraph | 段落
     */
    public static String paragraph(int sentenceCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentenceCount; i++) {
            if (i > 0) sb.append(" ");
            sb.append(sentence(random().nextInt(5, 15)));
        }
        return sb.toString();
    }

    // ============ Helpers | 辅助方法 ============

    private static ThreadLocalRandom random() {
        return ThreadLocalRandom.current();
    }

    @SafeVarargs
    private static <T> T oneOf(T... options) {
        return options[random().nextInt(options.length)];
    }
}
