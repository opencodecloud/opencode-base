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
- Optional Bouncy Castle integration for SM2/SM3/SM4, BLAKE, PGP

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-crypto</artifactId>
    <version>1.0.0</version>
</dependency>
```

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
boolean valid = signer.verify(data, signature, keyPair.getPublic());

// HMAC-SHA256
var mac = OpenCrypto.hmacSha256(secretKey);
byte[] tag = mac.compute(data);

// X25519 key exchange
var kex = OpenCrypto.x25519();
var kp = kex.generateKeyPair();
byte[] sharedSecret = kex.exchange(kp.getPrivate(), otherPublicKey);
```

## Requirements

- Java 25+
- Bouncy Castle (optional, for SM2/SM3/SM4, BLAKE, PGP)

## License

Apache License 2.0
