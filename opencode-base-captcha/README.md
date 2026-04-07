# OpenCode Base Captcha

Zero-dependency CAPTCHA generation and validation library with multiple types support for JDK 25+.

## Features

- 13 CAPTCHA types (text, arithmetic, Chinese, GIF, slider, rotate, click, image select, audio, jigsaw, PoW)
- Interactive CAPTCHA types (slider, rotate, click, image select, jigsaw puzzle ordering)
- Audio CAPTCHA (16kHz WAV, WCAG accessibility compliance)
- PoW invisible verification (SHA-256 proof-of-work, no user interaction)
- Anti-OCR/AI enhancements (Bezier through-character noise, sine wave warp, character overlap, random fonts per character, outline shadow)
- Custom font support (TTF/OTF loading, per-character random font selection)
- Trajectory analysis (TrajectoryAnalyzer detects bot-like mouse/touch patterns)
- Configurable dimensions, font, noise, and difficulty
- Pluggable storage (in-memory, Redis)
- Rate limiting and anti-bot behavior analysis
- Base64, image, and audio output rendering
- Time-based, behavior-based, and trajectory-based validation
- Adaptive difficulty adjustment (EASY / MEDIUM / HARD / EXTREME)
- Sealed interface design for type safety
- Metrics collection (CaptchaMetrics — generation counts, success/failure rates, response times)
- Pre-generation pool (CaptchaPool — background pre-generation for high throughput)
- Composite validator (CompositeValidator — chain multiple validators with short-circuit)
- Test mode generator (TestCaptchaGenerator — predictable answers for unit testing)
- Hashed answer store (HashedCaptchaStore — SHA-256+salt defense in depth)
- Event listener system (CaptchaEventListener — lifecycle event callbacks)

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-captcha</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API Overview

### Core

| Class | Description |
|-------|-------------|
| `OpenCaptcha` | Main entry point / facade for CAPTCHA generation and validation |
| `Captcha` | Immutable CAPTCHA data record (id, answer, image bytes, type) |
| `CaptchaConfig` | CAPTCHA configuration (type, width, height, length, expiration) |
| `CaptchaType` | CAPTCHA type enum (TEXT, ARITHMETIC, CHINESE, GIF, SLIDER, etc.) |
| `ValidationResult` | Validation result record (success, message, code) |
| `CaptchaEventListener` | Lifecycle event callback interface (onGenerated, onValidationSuccess, onValidationFailure) |
| `CaptchaEventDispatcher` | Thread-safe multi-listener event dispatcher with exception isolation |
| `CaptchaMetrics` | Lightweight metrics collector (LongAdder-based, O(1) overhead) |
| `CaptchaPool` | Pre-generation pool for high-throughput scenarios (background virtual thread) |
| `CompositeValidator` | Chains multiple validators with short-circuit execution |
| `TestCaptchaGenerator` | Predictable CAPTCHA generator for unit testing |
| `HashedCaptchaStore` | Decorator that hashes answers with SHA-256+salt before storage |
| `HashedCaptchaValidator` | Validator for HashedCaptchaStore (auto-detected by OpenCaptcha) |

<details>
<summary>Detailed Methods</summary>

#### OpenCaptcha

| Method | Description |
|--------|-------------|
| `static Captcha create()` | Create CAPTCHA with default configuration |
| `static Captcha create(CaptchaConfig config)` | Create CAPTCHA with specified configuration |
| `static Captcha create(CaptchaType type)` | Create CAPTCHA of specified type |
| `static Captcha numeric()` | Create numeric CAPTCHA |
| `static Captcha alpha()` | Create alphabetic CAPTCHA |
| `static Captcha alphanumeric()` | Create alphanumeric CAPTCHA |
| `static Captcha arithmetic()` | Create arithmetic CAPTCHA |
| `static Captcha chinese()` | Create Chinese CAPTCHA |
| `static Captcha gif()` | Create GIF animated CAPTCHA |
| `static Captcha slider()` | Create slider CAPTCHA |
| `static Captcha click()` | Create click CAPTCHA |
| `static Captcha rotate()` | Create rotate CAPTCHA |
| `static Captcha audio()` | Create audio CAPTCHA (WAV) |
| `static Captcha jigsaw()` | Create jigsaw puzzle CAPTCHA |
| `static Captcha pow()` | Create proof-of-work CAPTCHA |
| `static Builder builder()` | Create builder for advanced usage |
| `Captcha generate()` | Generate CAPTCHA with stored config and persist to store |
| `Captcha generate(CaptchaConfig config)` | Generate CAPTCHA with config and persist to store |
| `ValidationResult validate(String id, String answer)` | Validate a CAPTCHA answer |
| `void render(Captcha captcha, OutputStream out)` | Render CAPTCHA to output stream |
| `CaptchaStore getStore()` | Get the CAPTCHA store |
| `CaptchaConfig getConfig()` | Get the configuration |
| `CaptchaMetrics getMetrics()` | Get the metrics collector (null if not configured) |
| `CaptchaEventListener getEventListener()` | Get the event listener (null if not configured) |

#### Captcha (record)

| Method | Description |
|--------|-------------|
| `String id()` | Get unique identifier |
| `CaptchaType type()` | Get CAPTCHA type |
| `byte[] imageData()` | Get image data bytes |
| `String answer()` | Get correct answer |
| `Map<String, Object> metadata()` | Get additional metadata |
| `Instant createdAt()` | Get creation timestamp |
| `Instant expiresAt()` | Get expiration timestamp |
| `String toBase64()` | Convert image data to Base64 string |
| `String toBase64DataUrl()` | Convert image data to Base64 data URL |
| `String getMimeType()` | Get MIME type based on CAPTCHA type |
| `boolean isExpired()` | Check if CAPTCHA has expired |
| `<T> T getMetadata(String key)` | Get a metadata value by key |
| `int getWidth()` | Get image width from metadata |
| `int getHeight()` | Get image height from metadata |

#### CaptchaConfig

| Method | Description |
|--------|-------------|
| `static CaptchaConfig defaults()` | Create default configuration |
| `static Builder builder()` | Create configuration builder |
| `Builder toBuilder()` | Create builder from this configuration |
| `int getWidth()` | Get image width |
| `int getHeight()` | Get image height |
| `int getLength()` | Get CAPTCHA code length |
| `CaptchaType getType()` | Get CAPTCHA type |
| `Duration getExpireTime()` | Get expiration time |
| `int getNoiseLines()` | Get noise line count |
| `int getNoiseDots()` | Get noise dot count |
| `float getFontSize()` | Get font size |
| `String getFontName()` | Get font name |
| `Color getBackgroundColor()` | Get background color |
| `Color[] getFontColors()` | Get font colors |
| `boolean isCaseSensitive()` | Get case sensitivity flag |
| `int getGifFrameCount()` | Get GIF frame count |
| `int getGifDelay()` | Get GIF frame delay |
| `List<String> getCustomFontPaths()` | Get custom font file paths (TTF/OTF) |
| `boolean isRandomFontPerChar()` | Whether each character uses a random different font |
| `float getCharOverlapRatio()` | Get character overlap ratio (0.0-0.5) |
| `boolean isSineWarpEnabled()` | Whether sine wave warp distortion is enabled |
| `boolean isOutlineShadowEnabled()` | Whether character outline shadow is enabled |
| `boolean isBezierNoiseEnabled()` | Whether Bezier through-character noise is enabled |
| `int getPowDifficulty()` | Get PoW leading zero bits difficulty (10-32) |
| `float getAudioSpeedVariation()` | Get audio speed variation range (0.0-0.5) |

#### CaptchaType (enum)

| Method | Description |
|--------|-------------|
| `boolean isInteractive()` | Check if type is interactive (SLIDER, CLICK, ROTATE, IMAGE_SELECT, JIGSAW) |
| `boolean isTextBased()` | Check if type is text-based (NUMERIC, ALPHA, ALPHANUMERIC, ARITHMETIC, CHINESE) |
| `boolean isAudio()` | Check if type is audio (AUDIO) |
| `boolean isInvisible()` | Check if type is invisible (POW) |

Constants: `NUMERIC`, `ALPHA`, `ALPHANUMERIC`, `ARITHMETIC`, `CHINESE`, `GIF`, `SLIDER`, `CLICK`, `ROTATE`, `IMAGE_SELECT`, `AUDIO`, `JIGSAW`, `POW`

#### ValidationResult (record)

| Method | Description |
|--------|-------------|
| `boolean success()` | Whether validation succeeded |
| `String message()` | Result message |
| `ResultCode code()` | Result code |
| `boolean isFailed()` | Check if validation failed |
| `static ValidationResult ok()` | Create success result |
| `static ValidationResult notFound()` | Create not-found result |
| `static ValidationResult expired()` | Create expired result |
| `static ValidationResult mismatch()` | Create mismatch result |
| `static ValidationResult rateLimited()` | Create rate-limited result |
| `static ValidationResult invalidInput()` | Create invalid-input result |
| `static ValidationResult suspiciousBehavior()` | Create suspicious-behavior result |

ResultCode enum: `SUCCESS`, `NOT_FOUND`, `EXPIRED`, `MISMATCH`, `RATE_LIMITED`, `INVALID_INPUT`, `SUSPICIOUS_BEHAVIOR`

#### CaptchaMetrics

| Method | Description |
|--------|-------------|
| `static CaptchaMetrics create()` | Create a new metrics instance |
| `void recordGeneration(CaptchaType type)` | Record a CAPTCHA generation event |
| `void recordValidation(boolean success, Duration responseTime)` | Record validation result with response time |
| `void recordValidation(boolean success)` | Record validation result without response time |
| `MetricsSnapshot snapshot()` | Take an immutable point-in-time snapshot of all metrics |
| `void reset()` | Reset all counters and restart uptime clock |

MetricsSnapshot record: `totalGenerated()`, `totalValidations()`, `successfulValidations()`, `failedValidations()`, `successRate()`, `averageResponseTime()`, `generationsByType()`, `uptime()`

#### CaptchaPool

| Method | Description |
|--------|-------------|
| `static Builder builder()` | Create a new pool builder |
| `Captcha take()` | Take a CAPTCHA from pool (fallback to real-time generation if empty) |
| `int size()` | Get number of CAPTCHAs currently in pool |
| `boolean isRunning()` | Check if pool is running |
| `void close()` | Shut down pool and background thread |

Builder: `.config(CaptchaConfig)`, `.poolSize(int)` (1-10000, default 100), `.refillThreshold(float)` (0.0-1.0, default 0.2), `.build()`

#### CompositeValidator

| Method | Description |
|--------|-------------|
| `static CompositeValidator of(CaptchaValidator first, CaptchaValidator... rest)` | Create with varargs (first failure short-circuits) |
| `static CompositeValidator ofList(List<CaptchaValidator> validators)` | Create from list |
| `static Builder builder()` | Create builder for step-by-step construction |
| `ValidationResult validate(String id, String answer)` | Validate by chaining all validators |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | Validate with case sensitivity |
| `int size()` | Get number of validators in chain |
| `List<CaptchaValidator> getValidators()` | Get unmodifiable validator list |

#### TestCaptchaGenerator

| Method | Description |
|--------|-------------|
| `TestCaptchaGenerator(String fixedAnswer)` | Construct with fixed answer for all generated CAPTCHAs |
| `Captcha generate(CaptchaConfig config)` | Generate CAPTCHA with fixed answer and empty image data |
| `CaptchaType getType()` | Returns ALPHANUMERIC |
| `String getFixedAnswer()` | Get the fixed answer |

#### HashedCaptchaStore

| Method | Description |
|--------|-------------|
| `static HashedCaptchaStore wrap(CaptchaStore delegate)` | Wrap store with hashing (case-insensitive) |
| `static HashedCaptchaStore wrap(CaptchaStore delegate, boolean caseSensitive)` | Wrap store with configurable case sensitivity |
| `void store(String id, String answer, Duration ttl)` | Store answer after SHA-256+salt hashing |
| `boolean verifyAnswer(String id, String plainAnswer)` | Verify answer without removing entry |
| `boolean verifyAndRemove(String id, String plainAnswer)` | Verify answer and remove entry atomically |
| `ValidationResult verifyAndRemoveResult(String id, String plainAnswer)` | Verify and remove, returning NOT_FOUND/MISMATCH/SUCCESS |

Delegates: `get()`, `getAndRemove()`, `remove()`, `exists()`, `clearExpired()`, `clearAll()`, `size()`

#### HashedCaptchaValidator

| Method | Description |
|--------|-------------|
| `HashedCaptchaValidator(HashedCaptchaStore store)` | Construct with hashed store |
| `ValidationResult validate(String id, String answer)` | Validate against stored hash |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | Validate (caseSensitive ignored — configured on store) |

#### CaptchaEventListener (interface)

| Method | Description |
|--------|-------------|
| `default void onGenerated(Captcha captcha)` | Called when a CAPTCHA is generated |
| `default void onValidationSuccess(String captchaId)` | Called when validation succeeds |
| `default void onValidationFailure(String captchaId, ResultCode reason)` | Called when validation fails |

#### CaptchaEventDispatcher

| Method | Description |
|--------|-------------|
| `void addListener(CaptchaEventListener listener)` | Register an event listener |
| `boolean removeListener(CaptchaEventListener listener)` | Remove an event listener |
| `int listenerCount()` | Get registered listener count |
| `void onGenerated(Captcha captcha)` | Dispatch generation event to all listeners |
| `void onValidationSuccess(String captchaId)` | Dispatch validation success to all listeners |
| `void onValidationFailure(String captchaId, ResultCode reason)` | Dispatch validation failure to all listeners |

</details>

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
| `AudioCaptchaGenerator` | Audio CAPTCHA generator (16kHz WAV with tone sequences) |
| `PowCaptchaGenerator` | Proof-of-Work CAPTCHA generator (SHA-256 challenge) |

<details>
<summary>Detailed Methods</summary>

#### CaptchaGenerator (sealed interface)

| Method | Description |
|--------|-------------|
| `Captcha generate()` | Generate CAPTCHA with default configuration |
| `Captcha generate(CaptchaConfig config)` | Generate CAPTCHA with specified configuration |
| `CaptchaType getType()` | Get the supported CAPTCHA type |
| `static CaptchaGenerator forType(CaptchaType type)` | Create generator for specified type |
| `static CaptchaGenerator numeric()` | Create numeric CAPTCHA generator |
| `static CaptchaGenerator alpha()` | Create alphabetic CAPTCHA generator |
| `static CaptchaGenerator alphanumeric()` | Create alphanumeric CAPTCHA generator |
| `static CaptchaGenerator arithmetic()` | Create arithmetic CAPTCHA generator |
| `static CaptchaGenerator chinese()` | Create Chinese CAPTCHA generator |
| `static CaptchaGenerator gif()` | Create GIF CAPTCHA generator |
| `static CaptchaGenerator audio()` | Create audio CAPTCHA generator |

#### AbstractCaptchaGenerator (abstract)

| Method | Description |
|--------|-------------|
| `protected BufferedImage createImage(CaptchaConfig config)` | Create buffered image with configuration |
| `protected Graphics2D createGraphics(BufferedImage image, CaptchaConfig config)` | Create graphics for image |
| `protected void drawNoise(Graphics2D g, CaptchaConfig config)` | Draw noise on image |
| `protected byte[] toBytes(BufferedImage image)` | Convert image to PNG bytes |
| `protected String generateId()` | Generate unique CAPTCHA ID |
| `protected Map<String, Object> createMetadata(CaptchaConfig config)` | Create metadata map |
| `protected Captcha buildCaptcha(CaptchaType type, byte[] imageData, String answer, CaptchaConfig config)` | Build Captcha from generated data |

#### SpecCaptchaGenerator / ImageCaptchaGenerator / GifCaptchaGenerator / ArithmeticCaptchaGenerator / ChineseCaptchaGenerator

| Method | Description |
|--------|-------------|
| `Captcha generate(CaptchaConfig config)` | Generate CAPTCHA with configuration |
| `CaptchaType getType()` | Get the supported CAPTCHA type |

</details>

### Interactive Generators

| Class | Description |
|-------|-------------|
| `SliderCaptchaGenerator` | Slider puzzle CAPTCHA generator |
| `RotateCaptchaGenerator` | Image rotation CAPTCHA generator |
| `ClickCaptchaGenerator` | Click-on-text CAPTCHA generator |
| `ImageSelectCaptchaGenerator` | Image selection CAPTCHA generator |
| `JigsawCaptchaGenerator` | Jigsaw puzzle piece ordering CAPTCHA generator |

<details>
<summary>Detailed Methods</summary>

#### SliderCaptchaGenerator / RotateCaptchaGenerator / ClickCaptchaGenerator / ImageSelectCaptchaGenerator

| Method | Description |
|--------|-------------|
| `Captcha generate(CaptchaConfig config)` | Generate interactive CAPTCHA with configuration |
| `CaptchaType getType()` | Get the supported CAPTCHA type |

All interactive generators implement the `CaptchaGenerator` sealed interface.

</details>

### Renderers

| Class | Description |
|-------|-------------|
| `CaptchaRenderer` | Renderer interface for CAPTCHA output |
| `ImageCaptchaRenderer` | PNG image renderer |
| `GifCaptchaRenderer` | GIF image renderer |
| `Base64CaptchaRenderer` | Base64 data URL renderer |
| `AudioCaptchaRenderer` | Audio WAV renderer |

<details>
<summary>Detailed Methods</summary>

#### CaptchaRenderer (interface)

| Method | Description |
|--------|-------------|
| `void render(Captcha captcha, OutputStream out)` | Render CAPTCHA to output stream |
| `byte[] renderToBytes(Captcha captcha)` | Render CAPTCHA to byte array |
| `String renderToBase64(Captcha captcha)` | Render CAPTCHA to Base64 string |
| `String getContentType()` | Get content type of rendered output |
| `static CaptchaRenderer image()` | Create PNG image renderer |
| `static CaptchaRenderer gif()` | Create GIF image renderer |
| `static CaptchaRenderer base64()` | Create Base64 data URL renderer |
| `static CaptchaRenderer audio()` | Create audio WAV renderer |

#### ImageCaptchaRenderer / GifCaptchaRenderer / Base64CaptchaRenderer / AudioCaptchaRenderer

All implement `CaptchaRenderer` with the same method signatures. Content types:
- `ImageCaptchaRenderer` -> `image/png`
- `GifCaptchaRenderer` -> `image/gif`
- `Base64CaptchaRenderer` -> `text/plain`
- `AudioCaptchaRenderer` -> `audio/wav`

</details>

### Validators

| Class | Description |
|-------|-------------|
| `CaptchaValidator` | Validator interface |
| `SimpleCaptchaValidator` | Basic text matching validator |
| `TimeBasedCaptchaValidator` | Validator with time expiration check |
| `BehaviorCaptchaValidator` | Behavior analysis-based validator |
| `PowCaptchaValidator` | Proof-of-Work nonce validator (SHA-256 leading zeros) |
| `CaptchaRateLimiter` | Rate limiter for validation attempts |

<details>
<summary>Detailed Methods</summary>

#### CaptchaValidator (interface)

| Method | Description |
|--------|-------------|
| `ValidationResult validate(String id, String answer)` | Validate CAPTCHA answer |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | Validate with case sensitivity option |
| `static CaptchaValidator simple(CaptchaStore store)` | Create simple validator |
| `static CaptchaValidator timeBased(CaptchaStore store)` | Create time-based validator |

#### SimpleCaptchaValidator

| Method | Description |
|--------|-------------|
| `SimpleCaptchaValidator(CaptchaStore store)` | Constructor with store |
| `ValidationResult validate(String id, String answer)` | Validate answer (case-insensitive) |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | Validate with case sensitivity option |

#### TimeBasedCaptchaValidator

| Method | Description |
|--------|-------------|
| `TimeBasedCaptchaValidator(CaptchaStore store)` | Constructor with store |
| `void recordCreation(String id)` | Record CAPTCHA creation time |
| `ValidationResult validate(String id, String answer)` | Validate with timing check |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | Validate with timing and case sensitivity |
| `void clearOldRecords()` | Clear old creation time records |

#### BehaviorCaptchaValidator

| Method | Description |
|--------|-------------|
| `BehaviorCaptchaValidator(CaptchaStore store)` | Constructor with store |
| `BehaviorCaptchaValidator(CaptchaStore store, BehaviorAnalyzer analyzer)` | Constructor with custom analyzer |
| `void recordCreation(String captchaId, String clientId)` | Record CAPTCHA creation for client |
| `ValidationResult validate(String id, String answer)` | Validate with behavior check |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | Validate with behavior and case sensitivity |
| `ValidationResult validate(String id, String answer, String clientId)` | Validate with explicit client ID |
| `ValidationResult validate(String id, String answer, String clientId, boolean caseSensitive)` | Validate with client ID and case sensitivity |
| `ValidationResult validate(String id, String answer, String clientId, TrajectoryData trajectory)` | Validate with trajectory analysis |
| `ValidationResult validate(String id, String answer, String clientId, TrajectoryData trajectory, boolean caseSensitive)` | Validate with trajectory and case sensitivity |
| `BehaviorAnalyzer getAnalyzer()` | Get the behavior analyzer |
| `TrajectoryAnalyzer getTrajectoryAnalyzer()` | Get the trajectory analyzer |
| `void clearOldRecords()` | Clear old creation records |

#### PowCaptchaValidator

| Method | Description |
|--------|-------------|
| `PowCaptchaValidator(CaptchaStore store)` | Constructor with store |
| `static PowCaptchaValidator create(CaptchaStore store)` | Factory method |
| `ValidationResult validate(String id, String answer)` | Validate PoW nonce |
| `ValidationResult validate(String id, String answer, boolean caseSensitive)` | Validate PoW nonce (caseSensitive is ignored) |

#### CaptchaRateLimiter

| Method | Description |
|--------|-------------|
| `CaptchaRateLimiter()` | Constructor with defaults (10 req/min) |
| `CaptchaRateLimiter(int maxRequests, Duration window)` | Constructor with settings |
| `boolean isAllowed(String clientId)` | Check if client is allowed to request |
| `int getRemainingRequests(String clientId)` | Get remaining requests for client |
| `Duration getTimeUntilReset(String clientId)` | Get time until rate limit reset |
| `void clear(String clientId)` | Clear rate limit for client |
| `void clearExpired()` | Clear all expired entries |

</details>

### Storage

| Class | Description |
|-------|-------------|
| `CaptchaStore` | Storage interface for CAPTCHA data |
| `MemoryCaptchaStore` | In-memory store with auto-eviction |
| `RedisCaptchaStore` | Redis-based store |

<details>
<summary>Detailed Methods</summary>

#### CaptchaStore (interface)

| Method | Description |
|--------|-------------|
| `void store(String id, String answer, Duration ttl)` | Store CAPTCHA answer with TTL |
| `Optional<String> get(String id)` | Retrieve CAPTCHA answer |
| `Optional<String> getAndRemove(String id)` | Retrieve and remove CAPTCHA answer |
| `void remove(String id)` | Remove a CAPTCHA |
| `boolean exists(String id)` | Check if CAPTCHA exists |
| `void clearExpired()` | Clear all expired CAPTCHAs |
| `void clearAll()` | Clear all CAPTCHAs |
| `int size()` | Get current store size |
| `static CaptchaStore memory()` | Create in-memory store |
| `static CaptchaStore memory(int maxSize)` | Create in-memory store with max size |

#### MemoryCaptchaStore (implements CaptchaStore, AutoCloseable)

| Method | Description |
|--------|-------------|
| `MemoryCaptchaStore()` | Constructor with default max size (10000) |
| `MemoryCaptchaStore(int maxSize)` | Constructor with specified max size |
| `void shutdown()` | Shut down cleanup scheduler |
| `void close()` | Close store (AutoCloseable, delegates to shutdown) |

Plus all `CaptchaStore` interface methods.

#### RedisCaptchaStore

| Method | Description |
|--------|-------------|
| `static Builder builder()` | Create builder |
| `String getKeyPrefix()` | Get the key prefix |

Builder methods: `keyPrefix(String)`, `setter(RedisSetter)`, `getter(Function<String, String>)`, `deleter(Consumer<String>)`, `existsChecker(Function<String, Boolean>)`, `build()`.

Plus all `CaptchaStore` interface methods.

</details>

### Security

| Class | Description |
|-------|-------------|
| `CaptchaSecurity` | Security configuration and enforcement |
| `AntiBotStrategy` | Anti-bot detection strategy |
| `BehaviorAnalyzer` | User behavior analysis for bot detection |
| `TrajectoryAnalyzer` | Trajectory analysis for bot detection (speed, jitter, direction) |
| `TrajectoryData` | Immutable trajectory record (points, timestamps, duration) |

<details>
<summary>Detailed Methods</summary>

#### CaptchaSecurity (utility class)

| Method | Description |
|--------|-------------|
| `static String generateSecureId()` | Generate secure random ID |
| `static String generateSecureToken(int length)` | Generate secure random token |
| `static String hashAnswer(String answer, String salt)` | Hash answer for secure storage |
| `static boolean verifyHashedAnswer(String answer, String hashedAnswer, String salt)` | Verify hashed answer |
| `static boolean constantTimeEquals(String a, String b)` | Constant-time string comparison |
| `static String generateSalt()` | Generate random salt |

#### AntiBotStrategy

| Method | Description |
|--------|-------------|
| `AntiBotStrategy(BehaviorAnalyzer analyzer)` | Constructor with analyzer |
| `static AntiBotStrategy create()` | Create with new analyzer |
| `AntiBotStrategy withBaseStrength(CaptchaStrength strength)` | Set base strength |
| `AntiBotStrategy withBaseType(CaptchaType type)` | Set base type |
| `CaptchaStrength recommendStrength(String clientId)` | Recommend strength for client |
| `CaptchaType recommendType(String clientId)` | Recommend CAPTCHA type for client |
| `boolean shouldBlock(String clientId)` | Check if client should be blocked |
| `BehaviorAnalyzer getAnalyzer()` | Get the behavior analyzer |

#### BehaviorAnalyzer

| Method | Description |
|--------|-------------|
| `AnalysisResult analyze(String clientId, Duration responseTime, boolean success)` | Analyze client behavior |
| `ClientBehavior getBehavior(String clientId)` | Get behavior data for client |
| `void clear(String clientId)` | Clear behavior data for client |
| `void clearOld()` | Clear all old behavior data |

AnalysisResult enum: `NORMAL`, `SUSPICIOUS_TIMING`, `TOO_MANY_FAILURES`, `CONSISTENT_TIMING`

ClientBehavior methods: `getLastActivity()`, `getTotalAttempts()`, `getRecentFailures()`

#### TrajectoryAnalyzer

| Method | Description |
|--------|-------------|
| `TrajectoryResult analyze(TrajectoryData data)` | Analyze trajectory for bot detection |

TrajectoryResult enum: `HUMAN`, `BOT_LINEAR`, `BOT_NO_JITTER`, `BOT_CONSTANT_SPEED`, `BOT_TOO_FAST`, `INSUFFICIENT_DATA`

#### TrajectoryData (record)

| Method | Description |
|--------|-------------|
| `TrajectoryData(List<Point> points, List<Long> timestamps, long totalDurationMs)` | Constructor with validation |
| `List<Point> points()` | Get coordinate sequence |
| `List<Long> timestamps()` | Get timestamp sequence |
| `long totalDurationMs()` | Get total duration in milliseconds |
| `List<Double> speeds()` | Calculate speed between consecutive points |
| `List<Double> accelerations()` | Calculate acceleration sequence |
| `int directionChanges()` | Count direction changes (>15 degree threshold) |
| `double jitterStdDev()` | Calculate jitter standard deviation |

TrajectoryData.Point record: `int x()`, `int y()`

</details>

### Support

| Class | Description |
|-------|-------------|
| `CaptchaChars` | Character sets for CAPTCHA generation |
| `CaptchaFontUtil` | Font loading and management |
| `CaptchaNoiseUtil` | Noise and distortion drawing utilities |
| `CaptchaDifficultyAdapter` | Adaptive difficulty adjustment |
| `CaptchaStrength` | CAPTCHA difficulty strength enum |

<details>
<summary>Detailed Methods</summary>

#### CaptchaChars (utility class)

| Method | Description |
|--------|-------------|
| `static String generate(CaptchaType type, int length)` | Generate random characters for type |
| `static String generateFromChars(char[] chars, int length)` | Generate from character set |
| `static String generateChinese(int length)` | Generate random Chinese characters |
| `static String[] generateArithmetic()` | Generate arithmetic expression [expression, answer] |
| `static Random getRandom()` | Get the SecureRandom instance |
| `static int randomInt(int bound)` | Generate random int within bound |
| `static int randomInt(int min, int max)` | Generate random int within range |

Character set constants: `NUMERIC`, `ALPHA_LOWER`, `ALPHA_UPPER`, `ALPHA`, `ALPHANUMERIC`, `CHINESE`

#### CaptchaFontUtil (utility class)

| Method | Description |
|--------|-------------|
| `static Font getFont(CaptchaConfig config)` | Get font from configuration |
| `static Font getFont(String fontName, float fontSize)` | Get font by name and size |
| `static Font getRandomStyleFont(Font font)` | Get font with random style |
| `static Font getRotatedFont(Font font, double angle)` | Get rotated font |
| `static Font getChineseFont(float fontSize)` | Get font suitable for Chinese characters |
| `static Color getRandomColor(CaptchaConfig config)` | Get random color from config colors |
| `static Color randomColor()` | Generate random color |
| `static Color randomLightColor()` | Generate light random color |
| `static Color randomDarkColor()` | Generate dark random color |
| `static Font loadCustomFont(String path, float fontSize)` | Load custom font from TTF/OTF file |
| `static Font[] getRandomFontsPerChar(String baseFontName, List<String> customPaths, float fontSize, int charCount)` | Generate random font array for each character (anti-OCR) |

#### CaptchaNoiseUtil (utility class)

| Method | Description |
|--------|-------------|
| `static void drawNoiseLines(Graphics2D g, CaptchaConfig config)` | Draw noise lines |
| `static void drawCurveLines(Graphics2D g, CaptchaConfig config)` | Draw curve lines |
| `static void drawCubicCurveLines(Graphics2D g, CaptchaConfig config)` | Draw cubic curve lines |
| `static void drawNoiseDots(Graphics2D g, CaptchaConfig config)` | Draw noise dots |
| `static void drawBackgroundNoise(Graphics2D g, CaptchaConfig config)` | Draw background noise |
| `static void shear(Graphics2D g, CaptchaConfig config)` | Apply shear transform effect |
| `static void drawGradientBackground(Graphics2D g, CaptchaConfig config)` | Draw gradient background |
| `static void drawInterferencePattern(Graphics2D g, CaptchaConfig config)` | Draw interference grid pattern |
| `static void drawBezierNoise(Graphics2D g, int width, int height, int count)` | Draw Bezier curves through character area (anti-OCR) |
| `static BufferedImage applySineWarp(BufferedImage image, double amplitude, double period)` | Apply sine wave warp distortion |
| `static void drawOutlineShadow(Graphics2D g, String text, Font font, int x, int y, Color shadowColor)` | Draw character outline shadow (anti-OCR) |
| `static int calculateOverlapSpacing(int totalWidth, int charCount, float fontSize, float overlapRatio)` | Calculate character spacing with overlap |

#### CaptchaDifficultyAdapter

| Method | Description |
|--------|-------------|
| `CaptchaStrength getStrength(String clientId)` | Get recommended strength for client |
| `CaptchaConfig getConfig(String clientId)` | Get adapted config for client |
| `CaptchaConfig getConfig(String clientId, CaptchaConfig baseConfig)` | Get adapted config with custom base |
| `void recordAttempt(String clientId, boolean success)` | Record validation attempt |
| `void reset(String clientId)` | Reset difficulty for client |
| `double getGlobalFailureRate()` | Get global failure rate (0.0-1.0) |
| `int getFailureCount(String clientId)` | Get failure count for client |
| `int getTrackedClientCount()` | Get number of tracked clients |
| `void clearAll()` | Clear all client records |

#### CaptchaStrength (enum)

| Method | Description |
|--------|-------------|
| `int getNoiseLines()` | Get noise line count |
| `int getNoiseDots()` | Get noise dot count |
| `float getFontSize()` | Get font size |
| `boolean isRandomFontPerChar()` | Whether random font per character is enabled |
| `boolean isBezierNoiseEnabled()` | Whether Bezier noise is enabled |
| `boolean isSineWarpEnabled()` | Whether sine wave warp is enabled |
| `boolean isOutlineShadowEnabled()` | Whether outline shadow is enabled |
| `float getCharOverlapRatio()` | Get character overlap ratio |
| `CaptchaConfig.Builder applyTo(CaptchaConfig.Builder builder)` | Apply strength to config builder |
| `CaptchaConfig toConfig()` | Create config with this strength |

Constants: `EASY`, `MEDIUM`, `HARD`, `EXTREME`

HARD enables: random fonts, Bezier noise, outline shadow, 10% character overlap. EXTREME enables all HARD features plus sine wave warp, 20% overlap.

</details>

### Codec (Internal)

| Class | Description |
|-------|-------------|
| `GifEncoder` | GIF image encoder |
| `LZWEncoder` | LZW compression encoder for GIF |
| `NeuQuantEncoder` | NeuQuant neural network color quantizer |

<details>
<summary>Detailed Methods</summary>

#### GifEncoder

| Method | Description |
|--------|-------------|
| `void setDelay(int ms)` | Set frame delay in milliseconds |
| `void setDispose(int code)` | Set disposal code |
| `void setRepeat(int iter)` | Set loop count (0 = infinite) |
| `void setQuality(int quality)` | Set color quantization quality |
| `boolean start(OutputStream os)` | Start encoding to output stream |
| `boolean addFrame(BufferedImage im)` | Add a frame to the GIF |
| `boolean finish()` | Finish encoding |

</details>

### Exceptions

| Class | Description |
|-------|-------------|
| `CaptchaException` | Base CAPTCHA exception |
| `CaptchaGenerationException` | CAPTCHA generation failure |
| `CaptchaVerifyException` | CAPTCHA verification failure |
| `CaptchaExpiredException` | CAPTCHA has expired |
| `CaptchaNotFoundException` | CAPTCHA not found |
| `CaptchaRateLimitException` | Rate limit exceeded |

<details>
<summary>Detailed Methods</summary>

All exceptions extend `CaptchaException` (which extends `RuntimeException`) and provide:

| Method | Description |
|--------|-------------|
| `(String message)` | Constructor with message |
| `(String message, Throwable cause)` | Constructor with message and cause |
| `(Throwable cause)` | Constructor with cause |

</details>

## Quick Start

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.store.CaptchaStore;

// Simple text CAPTCHA (stateless, no storage)
Captcha captcha = OpenCaptcha.create();
String base64 = captcha.toBase64DataUrl();  // Use in <img> tag
String answer = captcha.answer();           // Correct answer

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

// Full workflow with storage and validation (builder pattern)
OpenCaptcha openCaptcha = OpenCaptcha.builder()
    .store(CaptchaStore.memory())
    .build();
Captcha stored = openCaptcha.generate();
ValidationResult result = openCaptcha.validate(stored.id(), userInput);
if (result.success()) {
    // Validation passed
}
```

### Audio CAPTCHA

```java
import cloud.opencode.base.captcha.*;

// Audio CAPTCHA — WAV output for WCAG accessibility
Captcha audio = OpenCaptcha.audio();
String wavBase64 = audio.toBase64DataUrl(); // data:audio/wav;base64,...
String answer = audio.answer();             // Alphanumeric code
```

### Jigsaw CAPTCHA

```java
import cloud.opencode.base.captcha.*;
import java.util.List;

// Jigsaw puzzle — user reorders shuffled image pieces
Captcha jigsaw = OpenCaptcha.jigsaw();
List<String> pieces = (List<String>) jigsaw.metadata().get("pieces"); // Base64 piece images
int gridSize = (int) jigsaw.metadata().get("gridSize");               // 3 (3x3 grid)
String answer = jigsaw.answer();                                       // e.g. "1,2,0,..."
```

### PoW (Proof-of-Work) CAPTCHA

```java
import cloud.opencode.base.captcha.*;

// PoW invisible verification — no user interaction required
Captcha pow = OpenCaptcha.pow();
String challenge = (String) pow.metadata().get("challenge");
int difficulty = (int) pow.metadata().get("difficulty");  // Leading zero bits (default 20)
// Client computes nonce such that SHA-256(challenge + nonce) has 'difficulty' leading zeros
```

### Anti-OCR Enhanced Configuration

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.support.CaptchaStrength;

// HARD strength auto-enables: random fonts, Bezier noise, outline shadow, 10% overlap
CaptchaConfig hardConfig = CaptchaStrength.HARD.toConfig();
Captcha hardCaptcha = OpenCaptcha.create(hardConfig);

// EXTREME strength: all HARD features + sine wave warp + 20% overlap
CaptchaConfig extremeConfig = CaptchaStrength.EXTREME.toConfig();
Captcha extremeCaptcha = OpenCaptcha.create(extremeConfig);
```

### Custom Fonts

```java
import cloud.opencode.base.captcha.*;

// Load custom TTF/OTF fonts with random per-character selection
CaptchaConfig customFont = CaptchaConfig.builder()
    .customFontPath("/path/to/font.ttf")
    .randomFontPerChar(true)
    .build();
Captcha captcha = OpenCaptcha.create(customFont);
```

### Trajectory Analysis

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.security.TrajectoryData;
import cloud.opencode.base.captcha.security.TrajectoryData.Point;
import cloud.opencode.base.captcha.store.CaptchaStore;
import cloud.opencode.base.captcha.validator.BehaviorCaptchaValidator;
import java.util.List;

// Validate with trajectory data for enhanced bot detection
CaptchaStore store = CaptchaStore.memory();
BehaviorCaptchaValidator validator = new BehaviorCaptchaValidator(store);

List<Point> points = List.of(
    new Point(0, 0), new Point(10, 5), new Point(20, 8),
    new Point(35, 12), new Point(50, 10), new Point(70, 15)
);
List<Long> timestamps = List.of(0L, 50L, 120L, 200L, 300L, 420L);
TrajectoryData trajectory = new TrajectoryData(points, timestamps, 420L);

ValidationResult result = validator.validate(captchaId, answer, clientId, trajectory);
if (result.success()) {
    // Human-verified
}
```

### Metrics Collection

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.support.CaptchaMetrics;
import cloud.opencode.base.captcha.support.CaptchaMetrics.MetricsSnapshot;

// Enable metrics collection
CaptchaMetrics metrics = CaptchaMetrics.create();
OpenCaptcha openCaptcha = OpenCaptcha.builder()
    .metrics(metrics)
    .build();

openCaptcha.generate();
openCaptcha.validate(id, answer);

// Snapshot current metrics
MetricsSnapshot snapshot = metrics.snapshot();
long generated = snapshot.totalGenerated();
double successRate = snapshot.successRate();
```

### Pre-generation Pool

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.support.CaptchaPool;

// Pre-generate CAPTCHAs for instant retrieval
try (CaptchaPool pool = CaptchaPool.builder()
        .config(CaptchaConfig.defaults())
        .poolSize(200)
        .build()) {
    Captcha captcha = pool.take(); // O(1) from pool, fallback to real-time
}
```

### Composite Validator

```java
import cloud.opencode.base.captcha.validator.*;
import cloud.opencode.base.captcha.store.CaptchaStore;

// Chain multiple validators — first failure short-circuits
CaptchaStore store = CaptchaStore.memory();
CaptchaValidator validator = CaptchaValidator.composite(
    CaptchaValidator.simple(store),
    CaptchaValidator.timeBased(store)
);
```

### Test Mode

```java
import cloud.opencode.base.captcha.*;
import cloud.opencode.base.captcha.generator.TestCaptchaGenerator;

// Predictable answers for unit testing
TestCaptchaGenerator testGen = new TestCaptchaGenerator("1234");
Captcha captcha = testGen.generate(CaptchaConfig.defaults());
assert captcha.answer().equals("1234");
```

### Hashed Answer Store

```java
import cloud.opencode.base.captcha.store.*;

// Hash answers before storage for defense in depth
HashedCaptchaStore store = CaptchaStore.hashed(CaptchaStore.memory());
store.store("id", "answer", Duration.ofMinutes(5));
boolean valid = store.verifyAndRemove("id", "answer"); // true
```

### Event Listener

```java
import cloud.opencode.base.captcha.*;

// Register lifecycle event callbacks
CaptchaEventDispatcher dispatcher = new CaptchaEventDispatcher();
dispatcher.addListener(new CaptchaEventListener() {
    @Override public void onGenerated(Captcha captcha) {
        log.info("CAPTCHA generated: {}", captcha.id());
    }
    @Override public void onValidationFailure(String id, ValidationResult.ResultCode reason) {
        log.warn("CAPTCHA failed: {} reason={}", id, reason);
    }
});

OpenCaptcha openCaptcha = OpenCaptcha.builder()
    .eventListener(dispatcher)
    .build();
```

## Requirements

- Java 25+

## License

Apache License 2.0
