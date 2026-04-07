# OpenCode Base OAuth2

**OAuth 2.0 / OIDC client library - Authorization Code, Client Credentials, Device Code, PKCE, PAR, Token Introspection for JDK 25+**

`opencode-base-oauth2` is a lightweight, zero-dependency OAuth 2.0 and OpenID Connect client library with built-in support for major providers (Google, Microsoft, GitHub, Apple, Facebook), PKCE, JWT parsing, OIDC Discovery, Token Introspection (RFC 7662), Pushed Authorization Requests (RFC 9126), and pluggable token storage with automatic lifecycle management.

## Features

- **Multiple Grant Types**: Authorization Code, Client Credentials, Device Code, Refresh Token
- **PKCE Support**: Proof Key for Code Exchange (S256) for public clients
- **Pre-configured Providers**: Google, Microsoft (including tenant), GitHub, Apple, Facebook
- **Custom Providers**: Full support for any OAuth2-compliant server
- **OIDC Support**: OpenID Connect with UserInfo endpoint and JWT claims parsing
- **OIDC Discovery**: Auto-configure endpoints from `/.well-known/openid-configuration` (RFC 8414)
- **Token Management**: In-memory and file-based token storage, auto-refresh with thundering herd protection
- **Token Introspection**: Query token status from authorization server (RFC 7662)
- **Pushed Authorization Requests (PAR)**: Enhanced security via backchannel parameter submission (RFC 9126)
- **State Parameter**: Cryptographically secure CSRF protection with constant-time validation
- **Issuer Identification**: Authorization server issuer validation to prevent mix-up attacks (RFC 9207)
- **Resource Indicators**: Target resource specification for narrowly-scoped tokens (RFC 8707)
- **JWT Parsing**: Lightweight JWT claims extraction without external dependencies
- **HTTPS Enforcement**: Automatic HTTPS validation (HTTP allowed only for localhost)
- **OAuth2 Error Parsing**: Extracts `error`, `error_description`, `error_uri` from server responses
- **Thread-Safe**: All components are thread-safe
- **Builder Pattern**: Fluent API for client and configuration construction

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-oauth2</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage - Google OAuth2

```java
import cloud.opencode.base.oauth2.*;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;

// Create Google client
OAuth2Client client = OpenOAuth2.google("client-id", "client-secret")
    .redirectUri("https://yourapp.com/callback")
    .scopes("openid", "email", "profile")
    .build();

// Generate PKCE challenge and state
PkceChallenge pkce = OpenOAuth2.generatePkce();
String state = OpenOAuth2.generateState();

// Get authorization URL
String authUrl = client.getAuthorizationUrl(state, pkce);

// Validate state from callback
if (!OpenOAuth2.validateState(state, callbackState)) {
    throw new SecurityException("CSRF attack detected");
}

// Exchange code for token
OAuth2Token token = client.exchangeCode(code, pkce.verifier());

// Parse JWT claims
JwtClaims claims = OpenOAuth2.parseJwt(token.idToken());
```

### OIDC Discovery

```java
import cloud.opencode.base.oauth2.discovery.*;

// Auto-discover endpoints
DiscoveryDocument doc = OpenOAuth2.discover("https://accounts.google.com");
String tokenEndpoint = doc.tokenEndpoint();
boolean supportsPkce = doc.supportsPkce();

// Build config from discovery
OAuth2Config config = doc.toConfig("client-id", "client-secret");
```

### Token Lifecycle Management

```java
import cloud.opencode.base.oauth2.token.*;

// Create a token manager with thundering herd protection
TokenManager manager = OpenOAuth2.tokenManager()
    .tokenStore(OpenOAuth2.inMemoryTokenStore())
    .build();

// Store and retrieve tokens (auto-refreshes if expiring)
manager.store("user-123", token);
OAuth2Token valid = manager.getValidToken("user-123");

// Or obtain automatically if not present
OAuth2Token t = manager.getOrObtain("user-123", () -> client.getClientCredentialsToken());
```

### Token Introspection (RFC 7662)

```java
import cloud.opencode.base.oauth2.introspection.*;

TokenIntrospection introspection = OpenOAuth2.tokenIntrospection(
    "https://auth.example.com/introspect", "client-id", "client-secret");

IntrospectionResult result = introspection.introspect(accessToken);
if (result.active()) {
    String username = result.username();
    Set<String> scopes = result.scopes();
}
```

### Pushed Authorization Requests (RFC 9126)

```java
import cloud.opencode.base.oauth2.par.*;

PushedAuthorizationRequest par = OpenOAuth2.par(
    "https://auth.example.com/par", "client-id", "client-secret");

ParResponse response = par.push(Map.of(
    "response_type", "code",
    "redirect_uri", "https://yourapp.com/callback",
    "scope", "openid email"
));

String authUrl = PushedAuthorizationRequest.buildAuthorizationUrl(
    "https://auth.example.com/authorize", response, "client-id");
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

## Class Reference

| Class | Description |
|-------|-------------|
| `OpenOAuth2` | Main facade - factory methods for providers, PKCE, JWT, discovery, introspection, PAR |
| `OAuth2Client` | Core OAuth2 client supporting all grant types and token management |
| `OAuth2Config` | Immutable configuration (endpoints, credentials, scopes, PAR, introspection, resource) |
| `OAuth2Scope` | Standard OAuth2 scope constants |
| `OAuth2Token` | Immutable token record (access, refresh, ID token, expiry) |
| `OAuth2ErrorCode` | Error code enumeration for OAuth2 exceptions |
| `OAuth2Exception` | Exception extending OpenException with server error details |
| `DeviceCodeResponse` | Device authorization response with codes and URIs |
| `GrantType` | Grant type enumeration |
| `DiscoveryDocument` | OIDC discovery document record (RFC 8414) |
| `OidcDiscovery` | OIDC endpoint auto-discovery with caching |
| `IntrospectionResult` | Token introspection result record (RFC 7662) |
| `TokenIntrospection` | Token introspection client |
| `ParResponse` | PAR response record (RFC 9126) |
| `PushedAuthorizationRequest` | PAR client |
| `StateParameter` | Cryptographic state parameter generator/validator |
| `IssuerValidator` | Authorization server issuer validation (RFC 9207) |
| `ResourceIndicator` | Resource indicator record (RFC 8707) |
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
| `DefaultTokenManager` | Token lifecycle manager with thundering herd protection |
| `FileTokenStore` | File-based persistent token storage |
| `InMemoryTokenStore` | In-memory token storage |
| `TokenManager` | Token lifecycle management interface |
| `TokenRefresher` | Background token refresh scheduler |
| `TokenStore` | SPI interface for token persistence |

## Security

This library has undergone 6 rounds of security audit with 46 findings addressed (36 fixed, 10 accepted risk).

**Cryptographic Security:**
- Constant-time comparison (`MessageDigest.isEqual`) for nonce, state, and issuer validation — prevents timing side-channel attacks
- PKCE S256 with per-call `MessageDigest` — safe for virtual threads (no `ThreadLocal` leak)
- `SecureRandom` for all randomness (state, nonce, PKCE verifier)

**Input Validation & Defense:**
- HTTPS enforcement on all endpoints (HTTP allowed only for `localhost`/`127.0.0.1`/`::1`)
- JWT size limit (64KB) to prevent OOM DoS
- JWT requires 3-part structure (header.payload.signature)
- `iat` (issued-at) future-check prevents token pre-dating
- PKCE verifier enforces 43-128 character range (RFC 7636)
- Microsoft tenant ID validated against `[a-zA-Z0-9._-]+` to prevent URL injection
- Negative `expires_in` rejected to prevent token lifecycle manipulation

**Data Protection:**
- `OAuth2Token.toString()` and `OAuth2Config.toString()` redact secrets (access token, refresh token, client secret)
- Exception messages sanitized — no issuer values, no full URLs with query parameters
- `OidcToken.toString()` excludes PII (subject, email)
- `FileTokenStore` uses POSIX owner-only permissions (`rw-------`) with atomic write

**Protocol Compliance:**
- OIDC Discovery cache with 1-hour TTL (prevents cache poisoning)
- SSRF guard: discovery URLs must use HTTPS scheme
- HTTP redirects disabled (`Redirect.NEVER`) to prevent credential forwarding
- Response body truncated to 256 chars in error messages to prevent information leakage

**JSON Parsing:**
- Escape-aware string parsing with full JSON unescape support (`\"`, `\\`, `\/`, `\n`, `\r`, `\t`, `\b`, `\f`, `\uXXXX`)
- Fast-path optimization: no-escape strings use direct `substring` (zero allocation)
- Unified `JsonParser` for consistent parsing across all components

## Requirements

- Java 25+ (uses records, sealed classes, java.net.http.HttpClient)
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
