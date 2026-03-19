# OpenCode Base OAuth2

**OAuth 2.0 / OIDC client library - Authorization Code, Client Credentials, Device Code, PKCE support for JDK 25+**

`opencode-base-oauth2` is a lightweight, zero-dependency OAuth 2.0 and OpenID Connect client library with built-in support for major providers (Google, Microsoft, GitHub, Apple, Facebook), PKCE, JWT parsing, and pluggable token storage.

## Features

- **Multiple Grant Types**: Authorization Code, Client Credentials, Device Code, Refresh Token
- **PKCE Support**: Proof Key for Code Exchange (S256) for public clients
- **Pre-configured Providers**: Google, Microsoft (including tenant), GitHub, Apple, Facebook
- **Custom Providers**: Full support for any OAuth2-compliant server
- **OIDC Support**: OpenID Connect with UserInfo endpoint and JWT claims parsing
- **Token Management**: In-memory and file-based token storage, auto-refresh
- **JWT Parsing**: Lightweight JWT claims extraction without external dependencies
- **HTTPS Enforcement**: Automatic HTTPS validation (HTTP allowed only for localhost)
- **Thread-Safe**: All components are thread-safe
- **Builder Pattern**: Fluent API for client and configuration construction

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-oauth2</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage - Google OAuth2

```java
import cloud.opencode.base.oauth2.OpenOAuth2;
import cloud.opencode.base.oauth2.OAuth2Client;
import cloud.opencode.base.oauth2.OAuth2Token;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;

// Create Google client
OAuth2Client client = OpenOAuth2.google("client-id", "client-secret")
    .redirectUri("https://yourapp.com/callback")
    .scopes("openid", "email", "profile")
    .build();

// Generate PKCE challenge
PkceChallenge pkce = OpenOAuth2.generatePkce();

// Get authorization URL
String authUrl = client.getAuthorizationUrl("random-state", pkce);

// Exchange code for token (after user authorizes)
OAuth2Token token = client.exchangeCode(code, pkce.verifier());

// Parse JWT claims
JwtClaims claims = OpenOAuth2.parseJwt(token.idToken());
String email = claims.email();
```

### Client Credentials Flow

```java
OAuth2Client client = OpenOAuth2.client()
    .clientId("service-client-id")
    .clientSecret("service-client-secret")
    .tokenEndpoint("https://auth.example.com/token")
    .scopes("api.read", "api.write")
    .build();

OAuth2Token token = client.getClientCredentialsToken();
```

### Device Code Flow

```java
OAuth2Client client = OpenOAuth2.microsoft("client-id", "client-secret")
    .build();

DeviceCodeResponse device = client.requestDeviceCode();
System.out.println("Visit: " + device.verificationUri());
System.out.println("Enter code: " + device.userCode());

// Poll until user authorizes
Optional<OAuth2Token> token = client.pollDeviceToken(device.deviceCode());
```

### Token Storage and Auto-Refresh

```java
// File-based token storage
OAuth2Client client = OpenOAuth2.github("id", "secret")
    .redirectUri("https://yourapp.com/callback")
    .tokenStore(OpenOAuth2.fileTokenStore("myapp"))
    .build();

// Store and retrieve tokens
client.storeToken("user-123", token);
OAuth2Token valid = client.getValidToken("user-123"); // Auto-refreshes if expiring
```

## Class Reference

| Class | Description |
|-------|-------------|
| `OpenOAuth2` | Main facade - factory methods for providers, PKCE, JWT, and token stores |
| `OAuth2Client` | Core OAuth2 client supporting all grant types and token management |
| `OAuth2Config` | Immutable OAuth2 configuration (endpoints, credentials, scopes) |
| `OAuth2Scope` | Standard OAuth2 scope constants |
| `OAuth2Token` | Immutable token record (access, refresh, ID token, expiry) |
| `OAuth2ErrorCode` | Error code enumeration for OAuth2 exceptions |
| `OAuth2Exception` | Exception type for OAuth2 operation failures |
| `DeviceCodeResponse` | Device authorization response with codes and URIs |
| `GrantType` | Grant type enumeration (Authorization Code, Client Credentials, etc.) |
| `HttpClientFactory` | HTTP client factory for OAuth2 requests |
| `OAuth2HttpClient` | HTTP client for OAuth2 endpoint communication |
| `JwtClaims` | JWT token claims parser and accessor |
| `OidcClient` | OpenID Connect client with discovery and UserInfo |
| `OidcConfig` | OIDC-specific configuration |
| `OidcToken` | OIDC token with ID token support |
| `UserInfo` | OIDC UserInfo endpoint response |
| `PkceChallenge` | PKCE challenge/verifier generator (S256) |
| `CustomProvider` | Builder for custom OAuth2 provider configuration |
| `OAuth2Provider` | Provider interface defining endpoint URLs |
| `ProviderRegistry` | Registry for managing OAuth2 providers |
| `Providers` | Pre-configured providers (Google, Microsoft, GitHub, Apple, Facebook) |
| `FileTokenStore` | File-based persistent token storage |
| `InMemoryTokenStore` | In-memory token storage |
| `TokenManager` | Token lifecycle management with auto-refresh |
| `TokenRefresher` | Background token refresh scheduler |
| `TokenStore` | SPI interface for token persistence |

## Requirements

- Java 25+ (uses records, sealed classes, java.net.http.HttpClient)
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
