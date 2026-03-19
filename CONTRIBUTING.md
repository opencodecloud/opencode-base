# Contributing to OpenCode Base

Thank you for your interest in contributing to OpenCode Base!

## Getting Started

### Prerequisites

- JDK 25+
- Maven 3.9+
- Git

### Build

```bash
git clone https://github.com/opencode-cloud/opencode-base.git
cd opencode-base
mvn compile
mvn test
```

## How to Contribute

### Reporting Bugs

- Search [existing issues](https://github.com/opencode-cloud/opencode-base/issues) first
- Include: module name, Java version, steps to reproduce, expected vs actual behavior

### Submitting Changes

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Follow the coding conventions below
4. Write tests for your changes
5. Ensure all tests pass: `mvn test`
6. Commit with a clear message
7. Open a Pull Request

## Coding Conventions

### Package Structure

```
cloud.opencode.base.{module}
├── Open{Module}.java          # Facade class (static entry point)
├── {CoreInterface}.java       # Core interfaces
├── Open{Module}Exception.java # Module exception
├── spi/                       # SPI extension interfaces
└── internal/                  # Internal implementation
```

### Naming

- Facade classes: `Open` prefix (e.g., `OpenCron`, `OpenJson`)
- Exceptions: `Open{Module}Exception`
- All facade entry points are `final` with private constructor

### Javadoc

Bilingual (English + Chinese), following the template in `doc/注释.md`:

```java
/**
 * English Description
 * 中文描述
 *
 * @param name the parameter | 参数描述
 * @return the result | 返回值描述
 * @throws OpenXxxException when ... | 当...时抛出
 */
```

### Testing

- JUnit 5 + AssertJ + Mockito
- Use `@Nested` classes for grouping
- Test method naming: `should_expectedBehavior_when_condition`
- Minimum coverage: 80%

## Code of Conduct

Be respectful, constructive, and inclusive. We follow the [Contributor Covenant](https://www.contributor-covenant.org/).

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
