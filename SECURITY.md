# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in OpenCode Base, please report it responsibly.

**Do NOT open a public GitHub issue for security vulnerabilities.**

Instead, please send an email to: **security@opencode.cloud**

Include the following information:
- Description of the vulnerability
- Steps to reproduce
- Affected module(s) and version(s)
- Potential impact assessment

## Response Timeline

- **Acknowledgment**: Within 48 hours
- **Initial Assessment**: Within 5 business days
- **Fix Release**: Within 30 days for critical issues

## Security Measures

This project has undergone 14 rounds of deep security audits covering:
- Thread safety and race conditions
- Resource leaks
- Integer overflow / underflow
- Injection vulnerabilities (SQL, CRLF, XSS)
- Cryptographic implementation correctness
- Input validation and boundary conditions
