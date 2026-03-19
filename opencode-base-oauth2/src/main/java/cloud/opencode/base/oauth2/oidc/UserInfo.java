package cloud.opencode.base.oauth2.oidc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * OpenID Connect User Info
 * OpenID Connect 用户信息
 *
 * <p>Represents user information from the OIDC userinfo endpoint.</p>
 * <p>表示来自 OIDC userinfo 端点的用户信息。</p>
 *
 * <p><strong>Standard Claims | 标准声明:</strong></p>
 * <ul>
 *   <li>sub - Subject identifier - 主题标识符</li>
 *   <li>name - Full name - 全名</li>
 *   <li>email - Email address - 电子邮件地址</li>
 *   <li>picture - Profile picture URL - 头像 URL</li>
 *   <li>And more... - 更多...</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * UserInfo userInfo = client.getUserInfo(token);
 *
 * String userId = userInfo.sub();
 * String email = userInfo.email();
 * String name = userInfo.name();
 *
 * // Get custom claim
 * Optional<String> locale = userInfo.getClaim("locale");
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OIDC UserInfo endpoint response representation - OIDC用户信息端点响应表示</li>
 *   <li>Standard profile, email, address claims - 标准配置文件、电子邮件、地址声明</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">OIDC Standard Claims</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public record UserInfo(
        String sub,
        String name,
        String givenName,
        String familyName,
        String middleName,
        String nickname,
        String preferredUsername,
        String profile,
        String picture,
        String website,
        String email,
        Boolean emailVerified,
        String gender,
        String birthdate,
        String zoneinfo,
        String locale,
        String phoneNumber,
        Boolean phoneNumberVerified,
        Map<String, Object> claims
) {

    /**
     * Compact constructor
     * 紧凑构造器
     */
    public UserInfo {
        claims = claims != null ? Map.copyOf(claims) : Map.of();
    }

    /**
     * Get a custom claim value
     * 获取自定义声明值
     *
     * @param name the claim name | 声明名称
     * @return the claim value | 声明值
     */
    public Optional<Object> getClaim(String name) {
        return Optional.ofNullable(claims.get(name));
    }

    /**
     * Get a custom claim value as string
     * 获取自定义声明值作为字符串
     *
     * @param name the claim name | 声明名称
     * @return the claim value as string | 声明值作为字符串
     */
    public Optional<String> getClaimAsString(String name) {
        return getClaim(name).map(Object::toString);
    }

    /**
     * Check if email is verified
     * 检查电子邮件是否已验证
     *
     * @return true if email is verified | 如果电子邮件已验证返回 true
     */
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    /**
     * Check if phone number is verified
     * 检查电话号码是否已验证
     *
     * @return true if phone number is verified | 如果电话号码已验证返回 true
     */
    public boolean isPhoneNumberVerified() {
        return Boolean.TRUE.equals(phoneNumberVerified);
    }

    /**
     * Get display name (name or preferredUsername or email)
     * 获取显示名称（姓名或首选用户名或电子邮件）
     *
     * @return the display name | 显示名称
     */
    public String displayName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }
        if (email != null && !email.isBlank()) {
            return email.split("@")[0];
        }
        return sub;
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Parse user info from JSON response
     * 从 JSON 响应解析用户信息
     *
     * @param json the JSON string | JSON 字符串
     * @return the user info | 用户信息
     */
    public static UserInfo fromJson(String json) {
        Builder builder = builder();
        Map<String, Object> claims = new HashMap<>();

        // Parse JSON manually (simple implementation)
        parseJsonToClaims(json, claims);

        builder.sub(getStringClaim(claims, "sub"));
        builder.name(getStringClaim(claims, "name"));
        builder.givenName(getStringClaim(claims, "given_name"));
        builder.familyName(getStringClaim(claims, "family_name"));
        builder.middleName(getStringClaim(claims, "middle_name"));
        builder.nickname(getStringClaim(claims, "nickname"));
        builder.preferredUsername(getStringClaim(claims, "preferred_username"));
        builder.profile(getStringClaim(claims, "profile"));
        builder.picture(getStringClaim(claims, "picture"));
        builder.website(getStringClaim(claims, "website"));
        builder.email(getStringClaim(claims, "email"));
        builder.emailVerified(getBooleanClaim(claims, "email_verified"));
        builder.gender(getStringClaim(claims, "gender"));
        builder.birthdate(getStringClaim(claims, "birthdate"));
        builder.zoneinfo(getStringClaim(claims, "zoneinfo"));
        builder.locale(getStringClaim(claims, "locale"));
        builder.phoneNumber(getStringClaim(claims, "phone_number"));
        builder.phoneNumberVerified(getBooleanClaim(claims, "phone_number_verified"));
        builder.claims(claims);

        return builder.build();
    }

    private static void parseJsonToClaims(String json, Map<String, Object> claims) {
        // Simple JSON parser for flat objects
        if (json == null || json.isBlank()) return;

        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return;

        json = json.substring(1, json.length() - 1);

        int pos = 0;
        while (pos < json.length()) {
            // Skip whitespace
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
            if (pos >= json.length()) break;

            // Find key
            if (json.charAt(pos) != '"') {
                pos++;
                continue;
            }
            int keyStart = pos + 1;
            int keyEnd = json.indexOf('"', keyStart);
            if (keyEnd < 0) break;
            String key = json.substring(keyStart, keyEnd);
            pos = keyEnd + 1;

            // Find colon
            while (pos < json.length() && json.charAt(pos) != ':') pos++;
            if (pos >= json.length()) break;
            pos++;

            // Skip whitespace
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
            if (pos >= json.length()) break;

            // Parse value
            char ch = json.charAt(pos);
            if (ch == '"') {
                int valueStart = pos + 1;
                int valueEnd = json.indexOf('"', valueStart);
                if (valueEnd < 0) break;
                claims.put(key, json.substring(valueStart, valueEnd));
                pos = valueEnd + 1;
            } else if (ch == 't' || ch == 'f') {
                // Boolean
                if (json.substring(pos).startsWith("true")) {
                    claims.put(key, true);
                    pos += 4;
                } else if (json.substring(pos).startsWith("false")) {
                    claims.put(key, false);
                    pos += 5;
                }
            } else if (ch == 'n') {
                // Null
                if (json.substring(pos).startsWith("null")) {
                    pos += 4;
                }
            } else if (Character.isDigit(ch) || ch == '-') {
                // Number
                int valueStart = pos;
                while (pos < json.length() && (Character.isDigit(json.charAt(pos)) ||
                        json.charAt(pos) == '.' || json.charAt(pos) == '-')) {
                    pos++;
                }
                String numStr = json.substring(valueStart, pos);
                try {
                    if (numStr.contains(".")) {
                        claims.put(key, Double.parseDouble(numStr));
                    } else {
                        claims.put(key, Long.parseLong(numStr));
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }

            // Skip to next key-value pair
            while (pos < json.length() && json.charAt(pos) != ',') pos++;
            if (pos < json.length()) pos++;
        }
    }

    private static String getStringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value != null ? value.toString() : null;
    }

    private static Boolean getBooleanClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return null;
    }

    /**
     * UserInfo Builder
     * UserInfo 构建器
     */
    public static class Builder {
        private String sub;
        private String name;
        private String givenName;
        private String familyName;
        private String middleName;
        private String nickname;
        private String preferredUsername;
        private String profile;
        private String picture;
        private String website;
        private String email;
        private Boolean emailVerified;
        private String gender;
        private String birthdate;
        private String zoneinfo;
        private String locale;
        private String phoneNumber;
        private Boolean phoneNumberVerified;
        private Map<String, Object> claims = new HashMap<>();

        public Builder sub(String sub) {
            this.sub = sub;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder givenName(String givenName) {
            this.givenName = givenName;
            return this;
        }

        public Builder familyName(String familyName) {
            this.familyName = familyName;
            return this;
        }

        public Builder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder preferredUsername(String preferredUsername) {
            this.preferredUsername = preferredUsername;
            return this;
        }

        public Builder profile(String profile) {
            this.profile = profile;
            return this;
        }

        public Builder picture(String picture) {
            this.picture = picture;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder emailVerified(Boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder birthdate(String birthdate) {
            this.birthdate = birthdate;
            return this;
        }

        public Builder zoneinfo(String zoneinfo) {
            this.zoneinfo = zoneinfo;
            return this;
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder phoneNumberVerified(Boolean phoneNumberVerified) {
            this.phoneNumberVerified = phoneNumberVerified;
            return this;
        }

        public Builder claims(Map<String, Object> claims) {
            this.claims = claims != null ? new HashMap<>(claims) : new HashMap<>();
            return this;
        }

        public Builder claim(String name, Object value) {
            this.claims.put(name, value);
            return this;
        }

        public UserInfo build() {
            return new UserInfo(
                    sub, name, givenName, familyName, middleName, nickname,
                    preferredUsername, profile, picture, website, email,
                    emailVerified, gender, birthdate, zoneinfo, locale,
                    phoneNumber, phoneNumberVerified, claims
            );
        }
    }
}
