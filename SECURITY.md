# Security Policy

## Supported Versions

The following versions of DIGIT Core are currently supported with security updates:

| Version   | Supported          |
| --------- | ------------------ |
| `v2.9.x`  | :white_check_mark: |

For older versions, please consider upgrading to one of the supported versions.

## Reporting a Vulnerability

DIGIT Core takes security issues seriously. If you discover a security vulnerability, we ask you to report it responsibly by following the steps below.

1. **Do not** post publicly on GitHub issues or discussions. Instead, report privately by creating report through https://github.com/egovernments/Digit-Core/security
2. Please include a detailed description of the issue, any steps to reproduce, and potential impacts.
3. You will receive a confirmation acknowledging your report.

### Security Patches

For critical vulnerabilities, we will prioritize patches and communicate accordingly. All security patches will be backported to supported versions when possible.

### Disclosure Policy

- We are committed to responsible disclosure. Upon resolving a vulnerability, we will notify all relevant parties, update this repository, and provide detailed release notes.
- Security updates will be documented and made available as part of the regular release cycle.
- For highly sensitive vulnerabilities, a CVE (Common Vulnerabilities and Exposures) will be requested.

## Secure Development Practices

The DIGIT Core team follows best security practices in developing software, including:
- **OWASP Guidelines**: We adhere to the [OWASP Top 10](https://owasp.org/www-project-top-ten/) security guidelines to ensure the application is built and maintained securely.
- **Secure by Default**: DIGIT Core is designed to have security features enabled by default, reducing configuration errors and minimizing attack surfaces.
- **Dependency Management**: We actively monitor and patch vulnerabilities in dependencies through automated tools like Dependabot.
- **Code Reviews**: All code is reviewed for security flaws before merging to the main branch.

## Key Management and Encryption

- DIGIT Core employs strong encryption methods to protect sensitive data.
- **Key Management**: All encryption keys are securely managed using industry-standard practices. Regular key rotations are recommended.
- **Signed Audit Logs**: All critical system activities are logged and signed to enable non-repudiation.

## Third-Party Dependencies

DIGIT Core includes dependencies from various third-party libraries. We rely on trusted, actively maintained libraries, and regularly review their security posture.

## Security Recommendations for Users

1. **Enable Strong Authentication**: We recommend enabling OAuth or OpenID Connect providers for authentication.
2. **Apply Updates**: Keep your DIGIT Core instance up to date with the latest security patches.
3. **Monitor Logs**: Enable logging and monitoring to detect and respond to potential security threats.
4. **Use Firewalls and Subnets**: For installations, ensure firewall rules and subnets are properly configured.

## Additional Resources

- [OWASP Top 10 Security Risks](https://owasp.org/www-project-top-ten/)
- [CVE Database](https://cve.mitre.org/)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)
