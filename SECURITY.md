# Security Policy

Thank you for helping keep this project and its users safe.

Note: This project is currently an alpha/development build. The latest pre-release is 0.0.1-a. It is not configured for public-facing endpoints in this repository and is not deployed publicly by default.

## Supported Versions

- Active development: main branch (HEAD) and latest pre-release (e.g., 0.0.1-a)
- Older commits, tags, and pre-releases are not supported for security fixes

Given the pre-release status, fixes are provided on a best-effort basis.

## Reporting a Vulnerability

Please use private reporting and avoid disclosing details publicly.

Preferred (private):
- GitHub Private Vulnerability Report:  
  https://github.com/Macweese/digg-service/security/advisories/new

If the private report form is unavailable:
- Do not open a public issue with sensitive details.
- Instead, open a minimal issue asking for a secure contact channel, without disclosing the vulnerability content, or contact the repository owner to establish a private channel.

When reporting, please include:
- A clear description of the issue and impact
- Steps to reproduce (PoC), affected paths/files, and any logs
- Version/commit (e.g., tag like 0.0.1-a or commit SHA)
- Environment details (OS, runtime, build flags)
- Any suggested mitigations or patches (if available)

- We will acknowledge receipt within 3 business days (best effort) and keep you informed of remediation progress. We appreciate responsible disclosure and can credit reporters unless anonymity is requested.

## Scope

In scope:
- Vulnerabilities in this repository’s source code that could lead to security issues in typical deployments (e.g., auth bypass, injection, RCE, privilege escalation, data exposure)
- Supply-chain issues related to this repo (e.g., dependency confusion, accidental secret leakage within the repo)
- Misconfigurations in provided code or default configurations that would be insecure when deployed

Out of scope (non-exhaustive):
- Issues requiring public-facing endpoints not configured or provided by this repository
- Denial of Service due to resource exhaustion in local/dev-only setups
- Social engineering, physical attacks, or stolen devices
- Vulnerabilities in third-party dependencies without a demonstrable exploit path via this project
- Scans or automated testing against networks or systems not owned by you or not authorized for testing

## Coordinated disclosure

- We prefer coordinated disclosure. If a fix is required, we may request an embargo period until a patch is available.
- We aim to publish advisories and release notes describing impact and remediation steps once a fix is released.

## Non-security issues

For bugs, feature requests, and performance problems that are not security-related, please create a standard GitHub issue in this repository.