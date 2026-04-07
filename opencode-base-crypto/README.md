# OpenCode Base Crypto

Modern cryptographic utilities for JDK 25+ with comprehensive support for symmetric/asymmetric encryption, digital signatures, password hashing, key derivation, key exchange, and more.

## Features

- Symmetric encryption: AES-GCM, AES-CBC, ChaCha20-Poly1305, SM4-GCM/CBC
- Asymmetric encryption: RSA-OAEP, RSA-PKCS1, SM2, ECC
- Digital signatures: Ed25519, Ed448, ECDSA, RSA-SHA256, RSA-PSS, SM2
- Password hashing: Argon2id, BCrypt, SCrypt, PBKDF2
- Message digests: SHA-256/384/512, SHA3-256/512, SM3, BLAKE2b, BLAKE3
- HMAC: HMAC-SHA256, HMAC-SHA512, Poly1305
- Key derivation: HKDF, PBKDF2, Argon2, Scrypt
- Key exchange: X25519, X448, ECDH
- Envelope/hybrid encryption (RSA + AES-GCM)
- JWT creation and validation
- PGP encryption and key management
- SSL/TLS context builder and certificate pinning
- Secure random, nonce generation, and key rotation
- Sealed box and secret box constructs
- Codec utilities: Base64URL, Hex, PEM
- Constant-time comparison and secure memory erasure
- Streaming AEAD encryption for large files (AES-GCM, ChaCha20-Poly1305)
- TOTP/HOTP one-time passwords (RFC 6238/4226)
- Cryptographic algorithm policy enforcement (strict/standard/legacy)
- Versioned cipher for seamless algorithm migration
- SecureBytes: auto-erasable byte container with try-with-resources
- Optional Bouncy Castle integration for SM2/SM3/SM4, BLAKE, PGP

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-crypto</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Quick Start

```java
import cloud.opencode.base.crypto.OpenCrypto;

// AES-GCM encryption
var cipher = OpenCrypto.aesGcm();
byte[] key = cipher.generateKey();
byte[] encrypted = cipher.encrypt(plaintext, key);
byte[] decrypted = cipher.decrypt(encrypted, key);

// SHA-256 digest
byte[] hash = OpenCrypto.sha256().digest("Hello".getBytes());

// Password hashing with Argon2id
var hasher = OpenCrypto.argon2();
String hashed = hasher.hash("myPassword");
boolean matches = hasher.verify("myPassword", hashed);

// Ed25519 signature
var signer = OpenCrypto.ed25519();
var keyPair = signer.generateKeyPair();
byte[] signature = signer.sign(data, keyPair.getPrivate());
boolean sigValid = signer.verify(data, signature, keyPair.getPublic());

// HMAC-SHA256
var mac = OpenCrypto.hmacSha256(secretKey);
byte[] tag = mac.compute(data);

// X25519 key exchange
var kex = OpenCrypto.x25519();
var kp = kex.generateKeyPair();
byte[] sharedSecret = kex.exchange(kp.getPrivate(), otherPublicKey);
```

## New in V1.0.3

### TOTP/HOTP One-Time Passwords

RFC 4226 (HOTP) and RFC 6238 (TOTP) compliant, compatible with Google Authenticator.

```java
import cloud.opencode.base.crypto.otp.Totp;
import cloud.opencode.base.crypto.otp.Hotp;
import cloud.opencode.base.crypto.otp.TotpSecret;

// Generate a secret key
byte[] secret = TotpSecret.generate();              // 20-byte random key
String base32 = TotpSecret.toBase32(secret);        // Base32 for QR code
byte[] decoded = TotpSecret.fromBase32(base32);     // Decode back

// TOTP — generate and verify
Totp totp = Totp.sha1();                            // SHA-1, 30s, 6 digits
String code = totp.generate(secret);                // Current time code
boolean valid = totp.verify(secret, code);          // Verify (window=1)
boolean ok = totp.verify(secret, code, 2);          // Custom window

// TOTP — custom config
Totp custom = Totp.builder()
    .algorithm("HmacSHA256")
    .period(60)
    .digits(8)
    .build();

// Generate otpauth:// URI for QR provisioning
String uri = Totp.generateUri("MyApp", "user@example.com", secret);

// HOTP — counter-based
Hotp hotp = Hotp.sha1();
String hotpCode = hotp.generate(secret, 42);        // Counter = 42
boolean hotpValid = hotp.verify(secret, 42, hotpCode, 5); // Look-ahead = 5
```

| Class | Method | Description |
|-------|--------|-------------|
| `Totp` | `sha1()` / `sha256()` / `sha512()` | Create TOTP with preset algorithm |
| `Totp` | `builder()` | Custom algorithm, period, digits |
| `Totp` | `generate(secret)` | Generate code for current time |
| `Totp` | `generate(secret, time)` | Generate code for specific time |
| `Totp` | `verify(secret, code)` | Verify with default window (1) |
| `Totp` | `verify(secret, code, windowSize)` | Verify with custom window |
| `Totp` | `verify(secret, code, time, windowSize)` | Verify at specific time |
| `Totp` | `generateUri(issuer, account, secret)` | Generate otpauth:// URI |
| `Hotp` | `sha1()` / `sha256()` / `sha512()` | Create HOTP with preset algorithm |
| `Hotp` | `generate(secret, counter)` | Generate 6-digit OTP |
| `Hotp` | `generate(secret, counter, digits)` | Generate OTP with custom digits (6-8) |
| `Hotp` | `verify(secret, counter, code, lookAhead)` | Verify with look-ahead window |
| `TotpSecret` | `generate()` / `generate(length)` | Generate random secret |
| `TotpSecret` | `toBase32(data)` / `fromBase32(str)` | Base32 encode/decode |

### Streaming AEAD for Large Files

Segment-based AEAD encryption with per-segment authentication, anti-reorder and anti-truncation protection.

```java
import cloud.opencode.base.crypto.streaming.StreamingAead;

byte[] key = new byte[32]; // AES-256 key
// ...populate key...

// AES-GCM streaming encryption
try (StreamingAead aead = StreamingAead.aesGcm(key)
        .setSegmentSize(1024 * 1024)   // 1 MB segments (default)
        .setAad("context".getBytes())) {
    aead.encryptFile(Path.of("large.bin"), Path.of("large.enc"));
    aead.decryptFile(Path.of("large.enc"), Path.of("large.dec"));
}

// ChaCha20-Poly1305 streaming
try (StreamingAead chacha = StreamingAead.chaCha20(key)) {
    chacha.encrypt(inputStream, outputStream);
}

// Stream-based API
try (StreamingAead aead = StreamingAead.aesGcm(key)) {
    aead.encrypt(inputStream, encryptedOutputStream);
    aead.decrypt(encryptedInputStream, decryptedOutputStream);
}
```

| Class | Method | Description |
|-------|--------|-------------|
| `StreamingAead` | `aesGcm(key)` | Create AES-GCM streaming encryptor (16/24/32-byte key) |
| `StreamingAead` | `chaCha20(key)` | Create ChaCha20-Poly1305 streaming encryptor (32-byte key) |
| `StreamingAead` | `setSegmentSize(bytes)` | Set segment size (256B - 64MB, default 1MB) |
| `StreamingAead` | `setAad(aad)` | Set additional authenticated data |
| `StreamingAead` | `encrypt(in, out)` | Encrypt stream |
| `StreamingAead` | `decrypt(in, out)` | Decrypt stream |
| `StreamingAead` | `encryptFile(source, target)` | Encrypt file |
| `StreamingAead` | `decryptFile(source, target)` | Decrypt file (atomic write, no partial plaintext on failure) |
| `StreamingAead` | `close()` | Erase key material from memory |

### Versioned Cipher

Self-describing encrypted payloads with version metadata for zero-downtime algorithm migration.

```java
import cloud.opencode.base.crypto.versioned.VersionedCipher;
import cloud.opencode.base.crypto.symmetric.AesGcmCipher;

// Build with multiple versions
VersionedCipher vc = VersionedCipher.builder()
    .addVersion(1, oldCipher)       // Legacy AES-128-GCM
    .addVersion(2, newCipher)       // Current AES-256-GCM
    .currentVersion(2)              // Encrypt with v2
    .build();

// Encrypt always uses current version
byte[] encrypted = vc.encrypt(plaintext);

// Decrypt auto-detects version from payload header
byte[] decrypted = vc.decrypt(encrypted);

// Base64 convenience
String base64 = vc.encryptBase64("sensitive data");
String plain = vc.decryptBase64ToString(base64);
```

| Class | Method | Description |
|-------|--------|-------------|
| `VersionedCipher` | `builder()` | Create builder |
| `VersionedCipher` | `encrypt(plaintext)` | Encrypt with current version |
| `VersionedCipher` | `decrypt(payload)` | Decrypt, auto-detecting version |
| `VersionedCipher` | `encryptBase64(plaintext)` / `encryptBase64(str)` | Encrypt to Base64 |
| `VersionedCipher` | `decryptBase64(base64)` / `decryptBase64ToString(base64)` | Decrypt from Base64 |
| `VersionedPayload` | `serialize()` / `deserialize(data)` | Binary serialization |

### Crypto Policy

Algorithm governance with predefined policies for compliance enforcement.

```java
import cloud.opencode.base.crypto.policy.CryptoPolicy;

// Predefined policies
CryptoPolicy strict = CryptoPolicy.strict();      // AES-256-GCM, Ed25519, etc.
CryptoPolicy standard = CryptoPolicy.standard();  // + AES-128-GCM, RSA-OAEP, etc.
CryptoPolicy legacy = CryptoPolicy.legacy();      // + SHA-1, MD5, 3DES

// Check algorithm compliance
strict.check("AES-256-GCM", 256);                 // OK
strict.isAllowed("MD5", 0);                        // false
// strict.check("MD5", 0);                         // throws PolicyViolationException

// Custom policy
CryptoPolicy custom = CryptoPolicy.builder()
    .allow("AES-256-GCM", "ChaCha20-Poly1305")
    .deny("DES", "RC4")
    .minKeyBits("RSA", 4096)
    .build();
```

| Class | Method | Description |
|-------|--------|-------------|
| `CryptoPolicy` | `strict()` / `standard()` / `legacy()` | Predefined policies |
| `CryptoPolicy` | `builder()` | Custom policy builder |
| `CryptoPolicy` | `check(algorithm, keyBits)` | Check compliance (throws on violation) |
| `CryptoPolicy` | `isAllowed(algorithm, keyBits)` | Check compliance (returns boolean) |
| `CryptoPolicy` | `getAllowedAlgorithms()` | Get allowed algorithm set |
| `CryptoPolicy` | `getDeniedAlgorithms()` | Get denied algorithm set |
| `CryptoPolicy` | `getMinKeyBits()` | Get minimum key size requirements |

### SecureBytes

Auto-erasable secure byte container for sensitive key material.

```java
import cloud.opencode.base.crypto.util.SecureBytes;

// Defensive copy — caller keeps original
try (SecureBytes key = SecureBytes.of(rawKeyBytes)) {
    byte[] copy = key.getBytes();       // Returns a copy
    byte[] ref = key.getBytesUnsafe();  // Returns direct reference (hot path)
    int len = key.length();
} // Internal data is zeroed here

// Zero-copy — caller transfers ownership
try (SecureBytes key = SecureBytes.wrap(generateKey())) {
    doEncrypt(key.getBytesUnsafe());
} // The original array is zeroed
```

| Class | Method | Description |
|-------|--------|-------------|
| `SecureBytes` | `of(data)` | Create with defensive copy |
| `SecureBytes` | `wrap(data)` | Create with zero-copy (transfers ownership) |
| `SecureBytes` | `getBytes()` | Get copy of data |
| `SecureBytes` | `getBytesUnsafe()` | Get direct reference (hot path, do not retain) |
| `SecureBytes` | `length()` | Get byte length |
| `SecureBytes` | `isClosed()` | Check if closed |
| `SecureBytes` | `close()` | Zero internal data |
| `SecureBytes` | `equals(other)` | Constant-time equality (returns false if closed) |

## API Overview

| Class | Description |
|-------|-------------|
| `OpenCrypto` | Facade class providing simplified access to all crypto features |
| `OpenSymmetric` | Symmetric encryption operations (AES-CBC, etc.) |
| `OpenAsymmetric` | Asymmetric encryption operations (RSA, SM2, ECC) |
| `OpenSign` | Digital signature operations (Ed25519, ECDSA, RSA, SM2) |
| `OpenDigest` | Message digest / hash operations |
| `OpenPasswordHash` | Password hashing facade |
| `OpenJwt` | JWT creation and validation |
| `OpenPgp` | PGP encryption and key management |
| `OpenSsl` | SSL/TLS facade for context and certificate operations |
| **Symmetric** | |
| `AesGcmCipher` | AES-GCM AEAD cipher (128/256-bit) |
| `AesCipher` | AES cipher with configurable mode and padding |
| `ChaChaCipher` | ChaCha20-Poly1305 AEAD cipher |
| `Sm4Cipher` | SM4 cipher (GCM/CBC modes, requires Bouncy Castle) |
| `AeadCipher` | AEAD cipher interface |
| `SymmetricCipher` | Symmetric cipher interface |
| `AesKeyValidator` | AES key size validation |
| `CipherMode` | Enum of cipher modes |
| `Padding` | Enum of padding schemes |
| **Asymmetric** | |
| `RsaCipher` | RSA-PKCS1 cipher |
| `RsaOaepCipher` | RSA-OAEP cipher |
| `EccCipher` | ECC cipher |
| `Sm2Cipher` | SM2 cipher (requires Bouncy Castle) |
| `AsymmetricCipher` | Asymmetric cipher interface |
| **Signatures** | |
| `EddsaSignature` | Ed25519/Ed448 signatures |
| `EcdsaSignature` | ECDSA signatures |
| `RsaSignature` | RSA-SHA256 signatures |
| `RsaPssSignature` | RSA-PSS signatures |
| `Sm2Signature` | SM2 signatures (requires Bouncy Castle) |
| `SignatureEngine` | Signature engine interface |
| **Hash / Digest** | |
| `Sha2Hash` | SHA-2 family hash functions |
| `Sha3Hash` | SHA-3 family hash functions |
| `Sm3Hash` | SM3 hash (requires Bouncy Castle) |
| `Blake2Hash` | BLAKE2b hash (requires Bouncy Castle) |
| `Blake3Hash` | BLAKE3 hash (requires Bouncy Castle) |
| `HashFunction` | Hash function interface |
| `HmacFunction` | HMAC function interface |
| **Password Hashing** | |
| `Argon2Hash` | Argon2id/Argon2i/Argon2d password hashing |
| `BCryptHash` | BCrypt password hashing |
| `SCryptHash` | SCrypt password hashing |
| `Pbkdf2Hash` | PBKDF2 password hashing |
| `PasswordHash` | Password hash interface |
| `PasswordPolicy` | Password strength policy |
| **Key Derivation** | |
| `Hkdf` | HKDF key derivation |
| `Pbkdf2` | PBKDF2 key derivation |
| `Argon2Kdf` | Argon2 key derivation (requires Bouncy Castle) |
| `Scrypt` | Scrypt key derivation |
| `KdfEngine` | KDF engine interface |
| **Key Management** | |
| `KeyGenerator` | Cryptographic key generation |
| `KeyPairUtil` | Key pair utilities |
| `KeyUtil` | General key utilities |
| `SecretKeyUtil` | Secret key utilities |
| `SecureKeyStore` | Secure keystore wrapper |
| **Key Exchange** | |
| `X25519Engine` | X25519 key exchange |
| `X448Engine` | X448 key exchange |
| `EcdhEngine` | ECDH key exchange |
| `KeyExchangeEngine` | Key exchange interface |
| **MAC** | |
| `HmacSha256` | HMAC-SHA256 |
| `HmacSha512` | HMAC-SHA512 |
| `Poly1305` | Poly1305 MAC |
| `Mac` | MAC interface |
| **Envelope / Hybrid** | |
| `EnvelopeCrypto` | Envelope encryption (asymmetric wraps symmetric key) |
| `HybridCrypto` | Hybrid encryption (RSA + AES) |
| `EncryptedEnvelope` | Encrypted envelope record |
| **Sealed Box** | |
| `SealedBox` | Sealed box encryption |
| `SecretBox` | Secret box encryption |
| **JWT** | |
| `JwtUtil` | JWT creation and validation utility |
| `JwtClaims` | JWT claims model |
| `JwtAlgorithm` | JWT algorithm enum |
| **PGP** | |
| `PgpCipher` | PGP encryption/decryption |
| `PgpKeyUtil` | PGP key generation and import/export |
| `PgpKeyPair` | PGP key pair record |
| `PgpAlgorithm` | PGP algorithm enum |
| **SSL/TLS** | |
| `SslContextBuilder` | Fluent SSL context builder |
| `CertificatePinner` | Certificate pinning |
| `TrustAllManager` | Trust-all manager (testing only) |
| **Key Rotation** | |
| `KeyRotation` | Key rotation management |
| **Random** | |
| `RandomBytes` | Secure random byte generation |
| `NonceGenerator` | Nonce generation |
| `SecureRandoms` | SecureRandom instance management |
| **Codec** | |
| `Base64UrlCodec` | Base64URL encoding/decoding |
| `HexCodec` | Hex encoding/decoding |
| `PemCodec` | PEM encoding/decoding |
| **Utility** | |
| `ByteUtil` | Byte array utilities |
| `ConstantTimeUtil` | Constant-time comparison |
| `CryptoDetector` | Crypto provider detection |
| `CryptoUtil` | General crypto utilities |
| `SecureEraser` | Secure memory erasure |
| `SecureBytes` | Auto-erasable secure byte container (AutoCloseable) |
| **Streaming AEAD** | |
| `StreamingAead` | Chunked AEAD encryption for large data/files |
| **OTP (One-Time Password)** | |
| `Totp` | RFC 6238 Time-based OTP generation and verification |
| `Hotp` | RFC 4226 HMAC-based OTP generation and verification |
| `TotpSecret` | OTP secret key generation and Base32 encoding |
| **Policy** | |
| `CryptoPolicy` | Algorithm whitelist/blacklist policy enforcement |
| `PolicyViolationException` | Policy violation exception |
| **Versioned Cipher** | |
| `VersionedCipher` | Multi-version cipher for algorithm migration |
| `VersionedPayload` | Versioned encrypted payload with metadata |
| **Enums** | |
| `AsymmetricAlgorithm` | Asymmetric algorithm enum |
| `CurveType` | Elliptic curve type enum |
| `DigestAlgorithm` | Digest algorithm enum |
| `PasswordHashAlgorithm` | Password hash algorithm enum |
| `SignatureAlgorithm` | Signature algorithm enum |
| `SymmetricAlgorithm` | Symmetric algorithm enum |
| **Exceptions** | |
| `OpenCryptoException` | General crypto exception |
| `OpenKeyException` | Key-related exception |
| `OpenSignatureException` | Signature-related exception |

## Requirements

- Java 25+
- Bouncy Castle (optional, for SM2/SM3/SM4, BLAKE, PGP)

## License

Apache License 2.0
