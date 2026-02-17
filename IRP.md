# Incident Response Plan (IRP)

## Overview

This Incident Response Plan (IRP) provides a structured and repeatable process for identifying, analyzing, containing, and responding to security incidents. The plan is designed to minimize the impact of incidents on citizen services, safeguard sensitive data (PII/PHI), and ensure compliance with regulatory requirements.

The document is organized into four main phases that mirror the natural lifecycle of an incident:

1. **Lead Validation** – Initial detection, validation of alerts, and severity assessment.  
2. **Mitigation** – Containment actions, both short- and long-term, to stop ongoing threats.  
3. **Scoping** – Determining the breadth, depth, and impact of the incident across users, systems, and data.  
4. **Notification** – Coordinating internal and external communication, including regulatory bodies, government partners, and affected citizens.

In addition to these phases, the IRP includes reference sections with practical guidance:

- **Severity Assignment Guidelines** to ensure consistent evaluation of risks.
- **Mitigation Methods Catalog** outlining common technical and organizational measures.

---

## Phase 1: Lead Validation

### Guidelines

- Initial detection can come from monitoring tools (e.g., AWS GuardDuty, SIEM, anomaly monitoring) or reported by a partner or internal teams.
- Incidents are validated by reviewing automated alerts, logs, and comparing against baseline activity. Look for supporting evidence (e.g., a suspicious login attempt and abnormal data download).
- Try to reproduce the reported behavior if possible. Check if it needs special preconditions (e.g., unusual configs, insider access).
- Rule out false positives (misconfigured monitoring, normal but unusual business activity).
- Determine which systems are affected (IAM, APIs, databases, endpoints) and identify attack surface.
- Confirm if sensitive data (PII/PHI) is impacted. Assign the highest severity level if impacted. For the rest, see what part of the CIA triad is impacted and accordingly assign severity rating (check additional guidelines).
- Have the incident reviewed by a second analyst or security lead to avoid biases.

### Roles & Responsibilities

**Implementation SRE / Implementation Security Analyst (Level 1)**  
- Monitor incoming alerts and reports.  
- Validate against baselines, rule out false positives.  
- Document logs, suspicious activities, and initial findings.

**Implementation Lead / Security Lead**  
- Approve or reject escalation to “Incident”.  
- Review severity assignment (especially if PII/PHI impacted).  
- Ensure a second analyst reviews to avoid bias.

**Implementation DevOps Engineer**  
- Provide system logs (IAM, API gateway, infra).  
- Confirm uptime/availability impact.

### Tasks

- Validate alerts: rule out false positives.  
- Check impact on IAM, APIs, audit logs, data, uptime/availability.  
- Determine severity (Low/Medium/High as per severity matrix).  
- Decide if lead is not actionable or escalates to investigation.

### For the Record

- **Risk to CIA:** Yes – mostly Confidentiality & Availability.  
- **Data at risk:** Citizen PII/PHI, health/vaccine data.  
- **Exploit requirements:** Compromised credentials, misconfigured APIs, phishing of integrator staff.  
- **Vulnerability introduction date:** \[to be logged].  
- **Relevant system logs:** IAM, API gateway, AWS infra logs.

---

## Phase 2: Mitigation

### Guidelines

- **Short-term containment:** Isolate affected IAM and API services, suspend compromised accounts, stop data exfiltration.
- **Long-term:** Patch IAM/API configs, revoke tokens, enable secure alternative comms, enforce MDM for devices, apply code patches to address vulnerabilities.

### Tasks

- Re-assess severity after containment.  
- Check product surfaces: Cloud infra (AWS), third-party gateways, citizen-facing health apps.  
- Apply classification labels (Incident / Not-actionable).

### Roles & Responsibilities

**Implementation SRE / Implementation Security Analyst**  
- Execute short-term containment (disable accounts, revoke tokens).  
- Apply monitoring rules for unusual activity.

**Implementation DevOps Engineer**  
- Patch IAM/API configs, update firewall rules.  
- Quarantine VMs/containers.  
- Support rollback if required.

**Implementation Lead / Security Lead**  
- Decide on risk acceptance vs urgent fixes.  
- Approve long-term changes (e.g., MDM enforcement, code patch PRs).

### For the Record

- **First mitigation date:** \[log timestamp].  
- **Surfaces affected:** Core cloud infra, APIs, IAM, BYOD endpoints.  
- **Link to mitigation work:** Patch logs, AWS GuardDuty findings, GitHub PR fixing config/code issues.

---

## Phase 3: Scoping

### Guidelines

- Confirm the incident is real and proceed to measure impact.  
- Identify the number of users, organizations, or systems affected.  
- Check if data was exfiltrated, modified, or just exposed.  
- Establish a timeframe of compromise (start and end).  
- Collect key metrics: number of accounts impacted, confidence in completeness.  
- Decide if this should be escalated as an official incident requiring notification.

### Tasks

- Investigate how many users, organizations, or systems are affected.  
- Determine if data was exfiltrated, modified, or only exposed.  
- Identify the timeframe of compromise (when it began, how long it persisted).  
- Collect quantifiable metrics:  
  - Number of accounts/users impacted.  
  - Level of confidence in completeness (Low / Medium / High).  
- Confirm if this should be declared an official incident requiring notification and escalation.

### Roles & Responsibilities

**Implementation SRE / Implementation Security Analyst**  
- Collect logs, telemetry, and forensic evidence from impacted systems.  
- Run queries to determine which accounts, tenants, and systems are affected.  
- Document scope metrics (number of accounts, timeframe, confidence level).

**Implementation Lead / Security Lead**  
- Direct the scoping effort, ensure completeness of analysis.  
- Validate whether the incident meets thresholds for official declaration.  
- Approve escalation to regulators, legal, and notification phase if required.

**Implementation DevOps Engineer**  
- Provide system-level evidence (audit logs, DB access logs, API metrics).  
- Confirm if data was modified, exfiltrated, or just exposed.  
- Help estimate impact duration (when compromise began and ended).

**Data Protection Officer (DPO) / Compliance Officer (if applicable)**  
- Assess regulatory exposure (GDPR, HIPAA, CERT-In, etc.).  
- Advise whether legal notification thresholds are triggered.

**Business Owner / Product Owner**  
- Provide business context on the importance of impacted services.  
- Estimate operational and citizen-facing impact (service downtime, financial loss).

### For the Record

- **Was there a confirmed CIA breach?** (Yes/No; which part of CIA)  
- **Number of individual user accounts affected:** \[ ]  
- **Number of organizations/tenants affected:** \[ ]  
- **Timeframe of compromise:** Start \[ ], End \[ ]  
- **Confidence level in completeness of scoping:** Low / Medium / High  
- **Decision:** Escalate to Incident (Yes/No)

---

## Phase 4: Notification

### Guidelines

- Send notifications to stakeholders: internal staff, government partners, affected citizens (if data exposure confirmed).  
- SPOC (DPO) coordinates with Legal, PR, and regulators.  
- Crisis comms prepared for media inquiries.

### Tasks

- Decide whether to notify (mandatory if CIA breach confirmed).  
- Draft notification (citizens, government departments, regulators).  
- Leadership escalation (CTO + board brief).  
- Legal & PR approval.  
- Prepare FAQ for internal staff & support team.  
- Publish supporting comms if applicable (blog, changelog, gov’t circulars).

### Roles & Responsibilities

**SPOC (DPO)**  
- Coordinate external communication (regulators, govt partners, media).  
- Approve citizen notifications.

**Legal & PR**  
- Draft legal notices, regulator reports, media statements.  
- Approve language for public and citizen communication.

**Support/Operations Team**  
- Update FAQs for staff and end-users.  
- Handle inbound queries from citizens/partners.

### For the Record

- **Notification decision:** \[Yes/No].  
- **Date & time of notification:** \[to be logged].  
- **Channels:** Email, WhatsApp, direct calls, media release.  
- **Number of notifications sent:** \[to be logged].  
- **Links:** Notification content draft, regulator reports, public statement.

---

## Operational Reference

### Severity Assignment Guide

**Criticality of Systems Affected**  
- Services critical to citizen welfare → higher severity.  
- Non-critical systems → lower severity.

**Scope of Impact**  
- Number of users, organizations, or partners affected.  
- Localized issue (single user or region) vs widespread (entire state platform).

**Exploitability & Ease of Attack**  
- Publicly known exploit, low barrier (e.g., no authentication required) → higher severity.  
- Requires insider access, complex timing, or rare configurations → lower severity.

**Regulatory & Legal Exposure**  
- Breaches involving PII/PHI often trigger mandatory disclosure (GDPR, HIPAA, CERT-In). This escalates severity.

**Potential for Lateral Movement**  
- If an attacker can pivot into other systems (e.g., from SMS gateway into core citizen DB), raise severity.

**Business & Operational Disruption**  
- Service downtime affecting public governance, financial loss, or halted citizen services → higher severity.

**Detection vs Exploitation**  
- If only a vulnerability exists (no active exploitation) → moderate severity.  
- If there’s active exploitation / data exfiltration → critical severity.

### Mitigation Methods Catalog

1. **Access & Identity Controls**  
   - Revoke or rotate compromised credentials (passwords, API keys, OAuth tokens).  
   - Enforce password resets or MFA enrollment.  
   - Disable suspicious user accounts or sessions.  
   - Apply least privilege policies temporarily (restrict admin roles).

2. **Network & Infrastructure Controls**  
   - Block malicious IPs, domains, or geographies at the firewall / WAF.  
   - Segment affected networks or services (quarantine zones).  
   - Shut down or isolate compromised servers, VMs, or containers.  
   - Throttle traffic to reduce DDoS impact.

3. **Application & Platform Controls**  
   - Disable vulnerable features/modules (e.g., file upload, payments API).  
   - Apply hotfixes, config changes, or temporary patches.  
   - Increase rate limits and validation checks at API gateway.

4. **Endpoint & Device Controls**  
   - Quarantine infected BYOD or corporate devices.

5. **Data Protection Measures**  
   - Stop ongoing data exfiltration (block S3/MinIO downloads, revoke signed URLs).  
   - Restrict access to critical DBs to only essential accounts/services.

6. **Monitoring & Logging Enhancements**  
   - Enable additional logging levels for affected systems.  
   - Deploy temporary alerts for unusual access patterns.  
   - Preserve forensic evidence (don’t wipe logs).

7. **Third-Party & Vendor Coordination**  
   - Disable or limit integrations with compromised partners (SMS gateways, payment APIs).  
   - Coordinate with vendors for emergency patching.  
   - Notify cloud providers (AWS, GCP, Azure) if infrastructure-level breach is suspected.
