# Security Policy for DIGIT LTS

##  Reporting a Vulnerability

We take the security of the DIGIT Core platform seriously.

If you discover a security vulnerability in the DIGIT LTS (Long-Term Support) core services, **please do not create a public GitHub issue or discussion**.

### Instead, follow one of the two private channels:

1. **GitHub's Private Advisory Form**  
   Use the "Report a Vulnerability" button under the Security tab of this repository to privately disclose the issue.  
    [Click here to file a private security advisory](../../security/advisories/new)

2. **Email**  
   Contact our team directly at: `security@egovernments.org` 

---

##  What to Include

When reporting a vulnerability, please provide:

- Description of the vulnerability and its impact
- Steps to reproduce the issue
- Affected versions of DIGIT Core (if known)
- Logs or error traces, if available

Please ensure the vulnerability lies within the **DIGIT LTS Core services** only. Issues in domain services (e.g. Urban, Health) or modified forks are **not covered under LTS support**.

---

## Severity and Response SLAs

We follow the following response timelines during our support hours (Monday–Friday, 9am–6pm IST, excluding holidays):

| Severity                  | Description                                                                                                          | First Response SLA |
|---------------------------|----------------------------------------------------------------------------------------------------------------------|--------------------|
| P1: Production Halted     | Critical issue, possibly security-related, affecting live services without workaround                                | 1 working day      |
| P2: Production Degraded   | Major component impacted, possibly exploitable but with mitigations or workarounds                                   | 2 working days     |
| P3: Business Unaffected   | Low-severity security concern or non-critical issue                                                                  | 4 working days     |

 *Security vulnerabilities are always treated with priority and discretion.*

---

## Scope of Supported Services

Security reporting and support applies **only to DIGIT LTS Core services**. You can find the list of supported core services here:  
🔗 https://core.digit.org/platform/core-services

Services modified by users or issues stemming from unsupported domains (e.g., custom UI frameworks or non-core APIs) may not be eligible for official resolution but can still be discussed with the community.

---

## Responsible Disclosure

We follow [responsible disclosure principles](https://en.wikipedia.org/wiki/Responsible_disclosure). We commit to:

- Acknowledge your report in the defined SLA
- Keep the report confidential until a fix is released
- Credit the reporter (with consent) upon fix and disclosure

---

## Platform Security Guidelines

Please refer to DIGIT's platform security practices and user guidelines:  
🔗 https://core.digit.org/guides/security-and-privacy-guide

We encourage all users and partners to follow these recommendations and maintain secure deployments, including applying timely updates.

---

Thank you for helping us make DIGIT more secure and reliable!
