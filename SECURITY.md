# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| 1.x | ✅ |
| < 1.0 | ⚠️ Not yet released |

## Reporting a Vulnerability

Pulse is a client-side optimisation mod and does not process network data or execute remote code. However, if you discover a security-relevant issue:

1. **Do not** open a public GitHub issue.
2. Email the maintainers directly or use GitHub's private vulnerability reporting.
3. Include a description of the issue, reproduction steps, and the affected version.
4. You will receive an acknowledgement within 48 hours and a fix timeline within 5 business days.

## Scope

- Configuration injection via malicious config files
- Unsafe deserialization in config import/export
- Mod API misuse leading to arbitrary code execution
- Dependency supply chain vulnerabilities

## Out of Scope

- Issues caused by other mods or mod combinations that do not involve Pulse's code
- Vulnerabilities in Minecraft itself
- Social engineering of Pulse contributors

## Preferred Languages

We prefer all communications in English.
