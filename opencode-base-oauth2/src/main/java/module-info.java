/**
 * OpenCode Base OAuth2 Module
 * OpenCode 基础 OAuth2 模块
 *
 * <p>Lightweight OAuth 2.0 / OIDC client library for JDK 25+.</p>
 * <p>轻量级 OAuth 2.0 / OIDC 客户端库，适用于 JDK 25+。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Authorization Code Flow (with PKCE) - 授权码流程</li>
 *   <li>Client Credentials Flow - 客户端凭证流程</li>
 *   <li>Device Code Flow - 设备码流程</li>
 *   <li>Token Management (auto-refresh, storage) - Token 管理</li>
 *   <li>Built-in Providers (Google, Microsoft, GitHub, Apple, Facebook) - 内置提供者</li>
 *   <li>OIDC Support (UserInfo, JWT parsing) - OIDC 支持</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
module cloud.opencode.base.oauth2 {
    // Required modules
    requires transitive cloud.opencode.base.core;
    requires transitive java.net.http;

    // Export public API packages
    exports cloud.opencode.base.oauth2;
    exports cloud.opencode.base.oauth2.exception;
    exports cloud.opencode.base.oauth2.grant;
    exports cloud.opencode.base.oauth2.http;
    exports cloud.opencode.base.oauth2.oidc;
    exports cloud.opencode.base.oauth2.pkce;
    exports cloud.opencode.base.oauth2.provider;
    exports cloud.opencode.base.oauth2.token;
}
