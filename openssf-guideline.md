# How to Improve OpenSSF Scorecard Score

This document captures the changes made to improve an OpenSSF Scorecard score, organized by check category. Use it as a repeatable playbook for any repository.

---

## What is OpenSSF Scorecard?

[OpenSSF Scorecard](https://github.com/ossf/scorecard) is an automated security health checker for open-source projects. It scores a repository across ~18 checks (0–10 each), covering supply-chain security, code quality gates, dependency hygiene, and more. Results are published to the OpenSSF REST API and displayed as a badge.

Check your score at: `https://scorecard.dev/viewer/?uri=github.com/<org>/<repo>`

---

## Changes to Make (by Scorecard Check)

### 1. Scorecard CI Workflow — `Scorecard` check

**File:** `.github/workflows/scorecard.yml`

The Scorecard tool must run as a GitHub Actions workflow for results to be published. Without this, the score cannot be computed or reported.

Key configuration decisions:
- `permissions: read-all` at top level (least privilege default)
- Job-level permissions scoped to only what's needed: `security-events: write` and `id-token: write`
- `persist-credentials: false` on checkout
- All actions pinned to full commit SHA (not mutable tags)
- `publish_results: true` to publish the badge to OpenSSF
- Triggered on `push` to `master`, `schedule` (weekly), `branch_protection_rule`, and `workflow_dispatch`

```yaml
name: Scorecard supply-chain security
on:
  branch_protection_rule:
  schedule:
    - cron: '45 1 * * 4'
  push:
    branches: [ "master" ]
  workflow_dispatch:

permissions: read-all

jobs:
  analysis:
    name: Scorecard analysis
    runs-on: ubuntu-latest
    if: github.event.repository.default_branch == github.ref_name || github.event_name == 'pull_request' || github.event_name == 'workflow_dispatch'
    permissions:
      security-events: write
      id-token: write
    steps:
      - name: "Checkout code"
        uses: actions/checkout@11bd71901bbe5b1630ceea73d27597364c9af683 # v4.2.2
        with:
          persist-credentials: false

      - name: "Run analysis"
        uses: ossf/scorecard-action@f49aabe0b5af0936a0987cfb85d86b75731b0186 # v2.4.1
        with:
          results_file: results.sarif
          results_format: sarif
          publish_results: true

      - name: "Upload artifact"
        uses: actions/upload-artifact@4cec3d8aa04e39d1a68397de0c4cd6fb9dce8ec1 # v4.6.1
        with:
          name: SARIF file
          path: results.sarif
          retention-days: 5

      - name: "Upload to code-scanning"
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: results.sarif
```

**Common pitfalls:**
- `publish_results: true` aborts the action when run on non-default branches — the `if:` condition above prevents this
- Without `workflow_dispatch`, you cannot manually trigger a run from the Actions UI
- Do not leave commented-out permission blocks in the file — the Scorecard check flags them

---

### 2. Workflow Permissions — `Token-Permissions` check

**File:** Every `.github/workflows/*.yml` file

Without a top-level `permissions` block, GitHub grants `write-all` by default. The `Token-Permissions` check requires workflows to declare permissions explicitly and use least privilege.

Add this to every workflow file:

```yaml
permissions:
  contents: read
```

Then at the job level, grant only the additional permissions that specific job needs. Everything not listed is implicitly denied.

---

### 3. Dangerous Workflow Inputs — `Dangerous-Workflow` check

**File:** Any workflow that uses `workflow_dispatch` inputs or `pull_request_target`

The `Dangerous-Workflow` check penalizes workflows that interpolate `${{ github.event.inputs.* }}` or `${{ github.event.pull_request.* }}` directly into `run:` shell steps. This is a script injection vector — a malicious input value can break out of the string and run arbitrary shell commands.

Fix by moving inputs to `env:` variables and referencing them as shell variables:

```yaml
# Before — vulnerable to script injection:
run: |
  echo "Building ${{ github.event.inputs.service }}"

# After — safe:
env:
  SERVICE: ${{ github.event.inputs.service }}
run: |
  echo "Building ${SERVICE}"
```

Also avoid storing sensitive secrets in top-level `env:` blocks that are visible to all jobs. Pass credentials only to the specific steps that need them.

---

### 4. Automated Dependency Updates — `Dependency-Update-Tool` check

**File:** `.github/dependabot.yml`

This check requires a configured automated dependency update tool (Dependabot or Renovate). Without it, the score for this check is 0.

Add a `.github/dependabot.yml` that covers every package ecosystem your repo uses. For a multi-language or multi-module repo, add one entry per ecosystem per directory:

```yaml
version: 2
updates:
  # Add one block per ecosystem + directory combination your repo uses.
  # Common ecosystems: npm, maven, gradle, pip, gomod, cargo, docker, github-actions

  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "weekly"

  - package-ecosystem: "<your-ecosystem>"   # e.g. npm, maven, pip, gomod
    directory: "/<path-to-module>"
    schedule:
      interval: "weekly"
```

**Note:** For monorepos where sub-modules are not included in a root manifest, you need a separate entry for each sub-module directory.

---

### 5. Vulnerability Fixes — `Vulnerabilities` check

The `Vulnerabilities` check queries the [OSV database](https://osv.dev/) for known CVEs in your dependencies. Each open vulnerability penalizes the score.

**How to find and fix:**

1. **Check your current exposure:**
   - GitHub → Security tab → Dependabot alerts
   - Or run `scorecard --repo <your-repo> --checks Vulnerabilities --show-details`

2. **Fix by upgrading affected dependencies** to versions with patches. Prioritize by severity (Critical > High > Medium).

3. **For transitive (indirect) dependencies** that cannot be upgraded directly, use your package manager's override/force mechanism:
   - npm: `overrides` in `package.json`
   - Maven: `<dependencyManagement>` to force a version
   - Gradle: `resolutionStrategy.force`
   - Go: `replace` directives in `go.mod`

4. **Upgrade EOL runtime/base images.** An end-of-life Docker base image or runtime carries unpatched OS-level CVEs. Always use a supported LTS version.

5. **Enable Dependabot auto-merge** for patch-level updates to reduce the backlog.

The goal is to reach **0 open CVEs** in OSV for the default branch.

---

### 6. Security Policy — `Security-Policy` check

**File:** `SECURITY.md` (repo root or `.github/SECURITY.md`)

This check requires a file that tells users how to privately report a vulnerability. Without it, the score is 0.

Minimum required content:

```markdown
# Security Policy

## Reporting a Vulnerability

**Do not create a public GitHub issue.**

Use GitHub's "Report a Vulnerability" button (Security tab of this repository)
or email: security@yourdomain.org

## What to Include
- Description of the vulnerability and its impact
- Steps to reproduce
- Affected versions (if known)

## Response SLAs

| Severity | First Response |
|---|---|
| Critical | 1 working day |
| High | 2 working days |
| Medium/Low | 4 working days |

## Responsible Disclosure
We will acknowledge your report within the SLA, keep it confidential until a fix
is released, and credit you (with consent) upon disclosure.
```

---

### 7. Pinned Dependencies — `Pinned-Dependencies` check

**Files:** All `.github/workflows/*.yml` files

GitHub Actions `uses:` references that point to a mutable tag (e.g., `@v4`) can be silently overwritten by a supply-chain attacker. Pinning to a full commit SHA prevents this — a SHA is immutable.

```yaml
# Bad — mutable tag, can be redirected:
uses: actions/checkout@v4

# Good — immutable SHA with a human-readable comment:
uses: actions/checkout@11bd71901bbe5b1630ceea73d27597364c9af683 # v4.2.2
```

Do this for **every** `uses:` line in every workflow file.

**To automate this**, use one of:
- [pin-github-action](https://github.com/mheap/pin-github-action) — one-shot CLI to pin all actions in a file
- [Renovate `pinDigests: true`](https://docs.renovatebot.com/configuration-options/#pindigests) — keeps SHAs updated automatically

---

## Summary Table

| Scorecard Check | Action Required |
|---|---|
| `Scorecard` | Add `.github/workflows/scorecard.yml` |
| `Token-Permissions` | Add `permissions: contents: read` to every workflow |
| `Dangerous-Workflow` | Move `${{ github.event.inputs.* }}` to `env:` vars |
| `Dependency-Update-Tool` | Add `.github/dependabot.yml` for every ecosystem |
| `Vulnerabilities` | Upgrade vulnerable dependencies; reach 0 open CVEs in OSV |
| `Security-Policy` | Create `SECURITY.md` with reporting instructions |
| `Pinned-Dependencies` | Pin all workflow `uses:` to full commit SHA |

---

## Additional Checks (Not Yet Addressed)

These checks offer further score improvements:

| Scorecard Check | How to Fix |
|---|---|
| `Branch-Protection` | Enable branch protection on `master`: require PRs, require status checks, dismiss stale reviews, optionally require signed commits |
| `Code-Review` | Enforce at least 1 approving review required before merge |
| `SAST` | Add a static analysis tool: CodeQL (`github/codeql-action`), Semgrep, or SonarCloud |
| `Fuzzing` | Integrate a fuzzer (OSS-Fuzz or a custom harness) |
| `Signed-Releases` | Sign release artifacts with Sigstore/cosign |
| `Binary-Artifacts` | Remove committed binaries from the repository |
| `License` | Ensure an OSI-approved `LICENSE` file exists at repo root |
| `CII-Best-Practices` | Earn an [OpenSSF Best Practices Badge](https://www.bestpractices.dev/) |

---

## How to Run Scorecard Locally

```bash
# Install
go install github.com/ossf/scorecard/v5/cmd/scorecard@latest

# Run against your repo (requires GITHUB_AUTH_TOKEN with public_repo read scope)
scorecard --repo github.com/<org>/<repo> --show-details

# Or use the Docker image
docker run -e GITHUB_AUTH_TOKEN=<your-token> \
  gcr.io/openssf/scorecard:stable \
  --repo github.com/<org>/<repo> \
  --show-details
```

---

## References

- [OpenSSF Scorecard Checks Documentation](https://github.com/ossf/scorecard/blob/main/docs/checks.md)
- [scorecard-action README](https://github.com/ossf/scorecard-action)
- [OSV Vulnerability Database](https://osv.dev/)
- [OpenSSF Best Practices Badge](https://www.bestpractices.dev/)
- [pin-github-action tool](https://github.com/mheap/pin-github-action)
- [Sigstore / cosign](https://github.com/sigstore/cosign)
