package cloud.opencode.base.oauth2.provider;

import java.util.Set;

/**
 * Built-in OAuth2 Providers
 * 内置 OAuth2 提供者
 *
 * <p>Pre-configured OAuth2 providers for popular identity providers.</p>
 * <p>流行身份提供者的预配置 OAuth2 提供者。</p>
 *
 * <p><strong>Available Providers | 可用提供者:</strong></p>
 * <ul>
 *   <li>GOOGLE - Google OAuth2/OIDC</li>
 *   <li>MICROSOFT - Microsoft Identity Platform</li>
 *   <li>GITHUB - GitHub OAuth</li>
 *   <li>APPLE - Sign in with Apple</li>
 *   <li>FACEBOOK - Facebook Login</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use Google provider
 * OAuth2Client client = OAuth2Client.builder()
 *     .provider(Providers.GOOGLE)
 *     .clientId("your-client-id")
 *     .clientSecret("your-client-secret")
 *     .redirectUri("https://yourapp.com/callback")
 *     .build();
 *
 * // Use Microsoft provider
 * OAuth2Config config = Providers.MICROSOFT.toConfig(
 *     "client-id", "client-secret", "redirect-uri");
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pre-configured OAuth2 provider definitions - 预配置的OAuth2提供商定义</li>
 *   <li>Built-in support for Google, GitHub, Microsoft - 内置支持Google、GitHub、Microsoft</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public final class Providers {

    private Providers() {
        // Utility class
    }

    /**
     * Google OAuth2/OIDC Provider
     * Google OAuth2/OIDC 提供者
     *
     * @see <a href="https://developers.google.com/identity/protocols/oauth2">Google OAuth 2.0</a>
     */
    public static final OAuth2Provider GOOGLE = new OAuth2Provider() {
        @Override
        public String name() {
            return "Google";
        }

        @Override
        public String authorizationEndpoint() {
            return "https://accounts.google.com/o/oauth2/v2/auth";
        }

        @Override
        public String tokenEndpoint() {
            return "https://oauth2.googleapis.com/token";
        }

        @Override
        public String userInfoEndpoint() {
            return "https://openidconnect.googleapis.com/v1/userinfo";
        }

        @Override
        public String revocationEndpoint() {
            return "https://oauth2.googleapis.com/revoke";
        }

        @Override
        public String deviceAuthorizationEndpoint() {
            return "https://oauth2.googleapis.com/device/code";
        }

        @Override
        public Set<String> defaultScopes() {
            return Set.of("openid", "email", "profile");
        }

        @Override
        public boolean requiresPkce() {
            return true;
        }
    };

    /**
     * Microsoft Identity Platform Provider
     * Microsoft 身份平台提供者
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/active-directory/develop/">Microsoft Identity Platform</a>
     */
    public static final OAuth2Provider MICROSOFT = new OAuth2Provider() {
        @Override
        public String name() {
            return "Microsoft";
        }

        @Override
        public String authorizationEndpoint() {
            return "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
        }

        @Override
        public String tokenEndpoint() {
            return "https://login.microsoftonline.com/common/oauth2/v2.0/token";
        }

        @Override
        public String userInfoEndpoint() {
            return "https://graph.microsoft.com/oidc/userinfo";
        }

        @Override
        public String deviceAuthorizationEndpoint() {
            return "https://login.microsoftonline.com/common/oauth2/v2.0/devicecode";
        }

        @Override
        public Set<String> defaultScopes() {
            return Set.of("openid", "email", "profile", "offline_access");
        }

        @Override
        public boolean requiresPkce() {
            return true;
        }
    };

    /**
     * GitHub OAuth Provider
     * GitHub OAuth 提供者
     *
     * @see <a href="https://docs.github.com/en/developers/apps/building-oauth-apps">GitHub OAuth Apps</a>
     */
    public static final OAuth2Provider GITHUB = new OAuth2Provider() {
        @Override
        public String name() {
            return "GitHub";
        }

        @Override
        public String authorizationEndpoint() {
            return "https://github.com/login/oauth/authorize";
        }

        @Override
        public String tokenEndpoint() {
            return "https://github.com/login/oauth/access_token";
        }

        @Override
        public String userInfoEndpoint() {
            return "https://api.github.com/user";
        }

        @Override
        public Set<String> defaultScopes() {
            return Set.of("read:user", "user:email");
        }

        @Override
        public boolean requiresPkce() {
            return false;
        }
    };

    /**
     * Apple Sign In Provider
     * Apple 登录提供者
     *
     * @see <a href="https://developer.apple.com/sign-in-with-apple/">Sign in with Apple</a>
     */
    public static final OAuth2Provider APPLE = new OAuth2Provider() {
        @Override
        public String name() {
            return "Apple";
        }

        @Override
        public String authorizationEndpoint() {
            return "https://appleid.apple.com/auth/authorize";
        }

        @Override
        public String tokenEndpoint() {
            return "https://appleid.apple.com/auth/token";
        }

        @Override
        public String revocationEndpoint() {
            return "https://appleid.apple.com/auth/revoke";
        }

        @Override
        public Set<String> defaultScopes() {
            return Set.of("openid", "email", "name");
        }

        @Override
        public boolean requiresPkce() {
            return true;
        }
    };

    /**
     * Facebook Login Provider
     * Facebook 登录提供者
     *
     * @see <a href="https://developers.facebook.com/docs/facebook-login/">Facebook Login</a>
     */
    public static final OAuth2Provider FACEBOOK = new OAuth2Provider() {
        @Override
        public String name() {
            return "Facebook";
        }

        @Override
        public String authorizationEndpoint() {
            return "https://www.facebook.com/v18.0/dialog/oauth";
        }

        @Override
        public String tokenEndpoint() {
            return "https://graph.facebook.com/v18.0/oauth/access_token";
        }

        @Override
        public String userInfoEndpoint() {
            return "https://graph.facebook.com/me?fields=id,name,email";
        }

        @Override
        public Set<String> defaultScopes() {
            return Set.of("email", "public_profile");
        }

        @Override
        public boolean requiresPkce() {
            return false;
        }
    };

    /**
     * Create a custom provider for a specific Microsoft tenant
     * 为特定 Microsoft 租户创建自定义提供者
     *
     * @param tenantId the Azure AD tenant ID | Azure AD 租户 ID
     * @return the provider | 提供者
     */
    public static OAuth2Provider microsoftTenant(String tenantId) {
        return new OAuth2Provider() {
            @Override
            public String name() {
                return "Microsoft (" + tenantId + ")";
            }

            @Override
            public String authorizationEndpoint() {
                return "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/authorize";
            }

            @Override
            public String tokenEndpoint() {
                return "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
            }

            @Override
            public String userInfoEndpoint() {
                return "https://graph.microsoft.com/oidc/userinfo";
            }

            @Override
            public String deviceAuthorizationEndpoint() {
                return "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/devicecode";
            }

            @Override
            public Set<String> defaultScopes() {
                return Set.of("openid", "email", "profile", "offline_access");
            }

            @Override
            public boolean requiresPkce() {
                return true;
            }
        };
    }
}
