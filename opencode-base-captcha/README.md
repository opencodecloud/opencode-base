# OpenCode Base Captcha

Zero-dependency CAPTCHA generation and validation library with multiple types support for JDK 25+.

## Features

- Multiple CAPTCHA types (text, arithmetic, Chinese, GIF animated)
- Interactive CAPTCHA types (slider, rotate, click, image select)
- Configurable dimensions, font, noise, and difficulty
- Pluggable storage (in-memory, Redis)
- Rate limiting and anti-bot behavior analysis
- Base64 and image output rendering
- Time-based and behavior-based validation
- Adaptive difficulty adjustment
- Sealed interface design for type safety

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-captcha</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API Overview

### Core

| Class | Description |
|-------|-------------|
| `OpenCaptcha` | Main entry point / facade for CAPTCHA generation and validation |
| `Captcha` | Immutable CAPTCHA data record (id, code, image bytes, type) |
| `CaptchaConfig` | CAPTCHA configuration (type, width, height, length, expiration) |
| `CaptchaType` | CAPTCHA type enum (TEXT, ARITHMETIC, CHINESE, GIF, SLIDER, etc.) |
| `ValidationResult` | Validation result record (success, message, metadata) |

### Generators

| Class | Description |
|-------|-------------|
| `CaptchaGenerator` | Sealed interface for CAPTCHA generators |
| `AbstractCaptchaGenerator` | Abstract base class for generators |
| `SpecCaptchaGenerator` | Standard PNG text CAPTCHA generator |
| `ImageCaptchaGenerator` | Image-based CAPTCHA generator |
| `GifCaptchaGenerator` | Animated GIF CAPTCHA generator |
| `ArithmeticCaptchaGenerator` | Math expression CAPTCHA generator |
| `ChineseCaptchaGenerator` | Chinese character CAPTCHA generator |

### Interactive Generators

| Class | Description |
|-------|-------------|
| `SliderCaptchaGenerator` | Slider puzzle CAPTCHA generator |
| `RotateCaptchaGenerator` | Image rotation CAPTCHA generator |
| `ClickCaptchaGenerator` | Click-on-text CAPTCHA generator |
| `ImageSelectCaptchaGenerator` | Image selection CAPTCHA generator |

### Renderers

| Class | Description |
|-------|-------------|
| `CaptchaRenderer` | Renderer interface for CAPTCHA output |
| `ImageCaptchaRenderer` | PNG image renderer |
| `GifCaptchaRenderer` | GIF image renderer |
| `Base64CaptchaRenderer` | Base64 data URL renderer |

### Validators

| Class | Description |
|-------|-------------|
| `CaptchaValidator` | Validator interface |
| `SimpleCaptchaValidator` | Basic text matching validator |
| `TimeBasedCaptchaValidator` | Validator with time expiration check |
| `BehaviorCaptchaValidator` | Behavior analysis-based validator |
| `CaptchaRateLimiter` | Rate limiter for validation attempts |

### Storage

| Class | Description |
|-------|-------------|
| `CaptchaStore` | Storage interface for CAPTCHA data |
| `MemoryCaptchaStore` | In-memory store with auto-eviction |
| `RedisCaptchaStore` | Redis-based store |

### Security

| Class | Description |
|-------|-------------|
| `CaptchaSecurity` | Security configuration and enforcement |
| `AntiBotStrategy` | Anti-bot detection strategy |
| `BehaviorAnalyzer` | User behavior analysis for bot detection |

### Support

| Class | Description |
|-------|-------------|
| `CaptchaChars` | Character sets for CAPTCHA generation |
| `CaptchaFontUtil` | Font loading and management |
| `CaptchaNoiseUtil` | Noise and distortion drawing utilities |
| `CaptchaDifficultyAdapter` | Adaptive difficulty adjustment |
| `CaptchaStrength` | CAPTCHA difficulty strength enum |

### Codec (Internal)

| Class | Description |
|-------|-------------|
| `GifEncoder` | GIF image encoder |
| `LZWEncoder` | LZW compression encoder for GIF |
| `NeuQuantEncoder` | NeuQuant neural network color quantizer |

### Exceptions

| Class | Description |
|-------|-------------|
| `CaptchaException` | Base CAPTCHA exception |
| `CaptchaGenerationException` | CAPTCHA generation failure |
| `CaptchaVerifyException` | CAPTCHA verification failure |
| `CaptchaExpiredException` | CAPTCHA has expired |
| `CaptchaNotFoundException` | CAPTCHA not found |
| `CaptchaRateLimitException` | Rate limit exceeded |

## Quick Start

```java
import cloud.opencode.base.captcha.*;

// Simple text CAPTCHA
Captcha captcha = OpenCaptcha.create();
String base64 = captcha.toBase64DataUrl();  // Use in <img> tag
String code = captcha.code();               // Answer for validation

// Arithmetic CAPTCHA
Captcha mathCaptcha = OpenCaptcha.create(CaptchaConfig.builder()
    .type(CaptchaType.ARITHMETIC)
    .width(200)
    .height(80)
    .build());

// GIF animated CAPTCHA
Captcha gifCaptcha = OpenCaptcha.create(CaptchaConfig.builder()
    .type(CaptchaType.GIF)
    .length(5)
    .build());

// Validate
boolean valid = OpenCaptcha.verify(captchaId, userInput);
```

## Requirements

- Java 25+

## License

Apache License 2.0
