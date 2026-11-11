#!/usr/bin/env python3
"""CodeQL OWASP Top 10 gate.

Claude Code hooks: PostToolUse (--record) notes each source file Claude edits in the session; Stop scans the
Maven modules / JS packages holding those files with CodeQL (security-extended suite) and blocks Claude from
finishing while a high/critical finding sits in a file it edited. Other uncommitted work in the clone is not
scanned. Findings are mapped to OWASP Top 10:2025 categories by CWE. Policy: codeql-policy.json.

Modes:
  --record (stdin PostToolUse event)                       Claude Code hook: remember the edited file
  (stdin Stop event)                                       Claude Code hook: scan and gate
  --sarif FILE [--sarif FILE ...] (--changed-files LIST | --all) [--summary MD]
                                                           classify existing SARIF (CI); exit 2 on blocking findings
"""
import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
POLICY_FILE = os.environ.get("CODEQL_POLICY", os.path.join(HERE, "codeql-policy.json"))
CODEQL = os.environ.get("CODEQL", "codeql")
STEP_TIMEOUT = int(os.environ.get("CODEQL_GATE_TIMEOUT", "800"))
CWE_TAG = re.compile(r"external/cwe/cwe-0*(\d+)$")


def load_policy():
    with open(POLICY_FILE, encoding="utf-8") as f:
        policy = json.load(f)
    cwe_to_owasp = {}
    for category, cwes in policy["owasp_top10_2025"].items():
        for cwe in cwes:
            cwe_to_owasp.setdefault(cwe, []).append(category)
    policy["_cwe_to_owasp"] = cwe_to_owasp
    return policy


# ---------- SARIF classification ----------

def sarif_rules(run):
    rules = {}
    driver = run.get("tool", {}).get("driver", {})
    for component in [driver] + run.get("tool", {}).get("extensions", []):
        for rule in component.get("rules", []):
            rules[rule["id"]] = rule
    return rules


def is_exempt(policy, rule_id, path):
    for exc in policy.get("exceptions", []):
        if exc.get("rule_id") in (rule_id, "*") and path.startswith(exc.get("path_prefix", "")):
            return exc
    return None


def classify(policy, sarif_paths, changed=None, prefix=""):
    """Returns findings; changed=None means every finding is in scope."""
    findings = []
    for sarif_path in sarif_paths:
        with open(sarif_path, encoding="utf-8") as f:
            sarif = json.load(f)
        for run in sarif.get("runs", []):
            rules = sarif_rules(run)
            for res in run.get("results", []):
                if res.get("suppressions"):
                    continue
                rule = rules.get(res.get("ruleId"), {})
                props = rule.get("properties", {})
                try:
                    severity = float(props.get("security-severity", 0))
                except ValueError:
                    severity = 0.0
                cwes = sorted({int(m.group(1)) for t in props.get("tags", []) for m in [CWE_TAG.search(t)] if m})
                owasp = sorted({c for cwe in cwes for c in policy["_cwe_to_owasp"].get(cwe, [])}) or ["Other"]
                loc = (res.get("locations") or [{}])[0].get("physicalLocation", {})
                path = os.path.normpath(os.path.join(prefix, loc.get("artifactLocation", {}).get("uri", "?")))
                line = loc.get("region", {}).get("startLine", 0)
                in_scope = changed is None or path in changed
                exc = is_exempt(policy, res.get("ruleId", ""), path)
                findings.append({
                    "rule": res.get("ruleId", "?"), "name": rule.get("shortDescription", {}).get("text", ""),
                    "severity": severity, "cwes": cwes, "owasp": owasp, "path": path, "line": line,
                    "message": res.get("message", {}).get("text", "").splitlines()[0][:200],
                    "in_scope": in_scope, "exempt": exc,
                    "blocking": in_scope and not exc and severity >= policy["block_severity"],
                })
    return sorted(findings, key=lambda x: (not x["blocking"], -x["severity"], x["path"], x["line"]))


def fmt(f):
    cwe = ",".join(f"CWE-{c}" for c in f["cwes"]) or "no CWE"
    return (f"{f['path']}:{f['line']}  [{f['severity']:.1f}] {f['rule']}  {cwe}  {' / '.join(f['owasp'])}\n"
            f"    {f['message']}")


def markdown(findings, policy, title):
    blocking = [f for f in findings if f["blocking"]]
    lines = [f"## {title}", "",
             f"{len(findings)} security finding(s), **{len(blocking)} blocking** "
             f"(security-severity >= {policy['block_severity']}, in scope, not exempted).", ""]
    if findings:
        lines += ["| | Severity | OWASP 2025 | Rule | CWE | Location |", "|---|---|---|---|---|---|"]
        for f in findings:
            mark = "❌" if f["blocking"] else ("➖" if f["exempt"] else "⚠️")
            lines.append(f"| {mark} | {f['severity']:.1f} | {' / '.join(f['owasp'])} | `{f['rule']}` | "
                         f"{', '.join(f'CWE-{c}' for c in f['cwes'])} | `{f['path']}:{f['line']}` |")
    return "\n".join(lines) + "\n"


# ---------- Hook mode ----------

def git(*args):
    return subprocess.run(["git", *args], capture_output=True, text=True, check=True).stdout


def cache_dir(repo):
    path = os.path.join(tempfile.gettempdir(), "codeql-owasp", hashlib.sha256(repo.encode()).hexdigest()[:12])
    os.makedirs(path, exist_ok=True)
    return path


def edits_file(repo, event):
    session = re.sub(r"[^A-Za-z0-9_-]", "", event.get("session_id", "")) or "default"
    return os.path.join(cache_dir(repo), f"edits-{session}.txt")


def language_of(policy, name):
    if "node_modules" in name.split(os.sep):
        return None
    exts = {ext: lang for lang, spec in policy["languages"].items() for ext in spec["extensions"]}
    return exts.get(os.path.splitext(name)[1])


def run_record_mode():
    try:
        event = json.load(sys.stdin)
    except ValueError:
        return 0
    path = (event.get("tool_input") or {}).get("file_path", "")
    repo = git("rev-parse", "--show-toplevel").strip()
    rel = os.path.relpath(os.path.abspath(path), repo) if path else ".."
    if rel.startswith("..") or not language_of(load_policy(), rel):
        return 0
    with open(edits_file(repo, event), "a", encoding="utf-8") as f:
        f.write(os.path.normpath(rel) + "\n")
    return 0


def changed_files(repo, policy, event):
    """Source files Claude edited this session that still exist, mapped to their CodeQL language."""
    record = edits_file(repo, event)
    if not os.path.isfile(record):
        return {}
    with open(record, encoding="utf-8") as f:
        names = {line.strip() for line in f if line.strip()}
    return {n: language_of(policy, n) for n in names if os.path.isfile(os.path.join(repo, n))}


def scan_root(repo, rel_file, marker):
    """Nearest ancestor dir of the file holding the language's root marker (pom.xml / package.json)."""
    d = os.path.dirname(rel_file)
    while d:
        if os.path.isfile(os.path.join(repo, d, marker)):
            return d
        d = os.path.dirname(d)
    return os.path.dirname(rel_file) or "."


def file_digest(path):
    with open(path, "rb") as f:
        return hashlib.sha256(f.read()).hexdigest()


def run_codeql(repo, lang, spec, root, work):
    """Create a database for one root and analyse it; returns (sarif_path, error)."""
    db, sarif = os.path.join(work, "db"), os.path.join(work, "results.sarif")
    steps = [
        [CODEQL, "database", "create", db, f"--language={lang}", f"--build-mode={spec['build_mode']}",
         f"--source-root={os.path.join(repo, root)}", "--overwrite", "--threads=0"],
        [CODEQL, "database", "analyze", db, spec["suite"], "--format=sarif-latest", f"--output={sarif}",
         "--threads=0"],
    ]
    for cmd in steps:
        try:
            proc = subprocess.run(cmd, capture_output=True, text=True, timeout=STEP_TIMEOUT)
        except subprocess.TimeoutExpired:
            return None, f"`{' '.join(cmd[:3])}` timed out after {STEP_TIMEOUT}s"
        if proc.returncode != 0:
            return None, f"`{' '.join(cmd[:3])}` exited {proc.returncode}:\n" + "\n".join(
                (proc.stderr or proc.stdout).splitlines()[-20:])
    return sarif, None


def emit(obj):
    print(json.dumps(obj))
    return 0


def run_hook_mode():
    try:
        event = json.load(sys.stdin)
    except ValueError:
        event = {}
    policy = load_policy()
    repo = git("rev-parse", "--show-toplevel").strip()
    changed = changed_files(repo, policy, event)
    if not changed:
        return 0
    if not shutil.which(CODEQL):
        print("CodeQL OWASP gate skipped: codeql not on PATH (brew install --cask codeql).", file=sys.stderr)
        return 1

    version = json.loads(subprocess.run([CODEQL, "version", "--format=json"], capture_output=True,
                                        text=True).stdout or "{}").get("version", "?")
    notes = [] if version == policy["codeql_version"] else [
        f"CodeQL {version} differs from the pinned {policy['codeql_version']}; results may differ from CI."]

    groups = {}
    for path, lang in changed.items():
        root = scan_root(repo, path, policy["languages"][lang]["root_marker"])
        groups.setdefault((lang, root), []).append(path)

    cache_base = cache_dir(repo)
    head =git("-C", repo, "rev-parse", "HEAD").strip()
    sarifs, errors = [], []
    for (lang, root), paths in sorted(groups.items()):
        spec = policy["languages"][lang]
        key = hashlib.sha256(json.dumps([version, head, spec, sorted(
            (p, file_digest(os.path.join(repo, p))) for p in paths)]).encode()).hexdigest()
        work = os.path.join(cache_base, hashlib.sha256(f"{lang}:{root}".encode()).hexdigest()[:12])
        os.makedirs(work, exist_ok=True)
        key_file, sarif = os.path.join(work, "key"), os.path.join(work, "results.sarif")
        if not (os.path.isfile(sarif) and os.path.isfile(key_file) and open(key_file).read() == key):
            sarif, error = run_codeql(repo, lang, spec, root, work)
            if error:
                errors.append(f"{lang} {root}: {error}")
                continue
            with open(key_file, "w") as f:
                f.write(key)
        sarifs.append((sarif, root))

    findings = []
    for sarif, root in sarifs:
        findings += classify(policy, [sarif], changed=set(changed), prefix=root)
    blocking = [f for f in findings if f["blocking"]]
    other = [f for f in findings if not f["blocking"] and f["severity"] >= policy["block_severity"]]
    with open(os.path.join(cache_base, "report.md"), "w", encoding="utf-8") as f:
        f.write(markdown(findings, policy, "CodeQL OWASP gate"))
    scanned = ", ".join(f"{root} ({lang})" for lang, root in sorted(groups))
    summary = (f"CodeQL OWASP gate scanned {scanned}: {len(blocking)} blocking finding(s) in changed files, "
               f"{len(other)} other high/critical finding(s) in those modules. "
               f"Report: {os.path.join(cache_base, 'report.md')}")
    if errors:
        packs = " ".join(spec["suite"].split(":")[0] for spec in policy["languages"].values())
        notes.append("Scan errors (findings for these roots unknown):\n" + "\n".join(errors)
                     + f"\nIf a query pack is missing: codeql pack download {packs}")

    fingerprint = hashlib.sha256(json.dumps(sorted(
        (f["rule"], f["path"], f["message"]) for f in blocking)).encode()).hexdigest()
    state_file = os.path.join(cache_base, "last-block")
    previous = open(state_file).read() if os.path.isfile(state_file) else ""

    if blocking and not (event.get("stop_hook_active") and previous == fingerprint):
        with open(state_file, "w") as f:
            f.write(fingerprint)
        reason = "\n".join(
            ["CodeQL OWASP gate: high/critical security findings in files you changed. Fix them before finishing "
             "(e.g. bind parameters instead of concatenating SQL, validate/normalise paths, avoid unsafe "
             "deserialization). Do not edit .claude/hooks/codeql-policy.json; if a finding is a false positive, "
             "explain why to the user."]
            + [fmt(f) for f in blocking] + [summary] + notes)
        return emit({"decision": "block", "reason": reason})

    message = summary
    if blocking:
        message = ("CodeQL OWASP gate: these findings are still open after Claude's fix attempt; review them:\n"
                   + "\n".join(fmt(f) for f in blocking) + "\n" + summary)
    return emit({"systemMessage": "\n".join([message] + notes)})


# ---------- SARIF mode (CI) ----------

def run_sarif_mode(args):
    policy = load_policy()
    changed = None
    if args.changed_files:
        with open(args.changed_files, encoding="utf-8") as f:
            changed = {os.path.normpath(l.strip()) for l in f if l.strip()}
    findings = classify(policy, args.sarif, changed=changed)
    scope = "all files" if changed is None else f"{len(changed)} changed file(s)"
    md = markdown(findings, policy, f"CodeQL OWASP gate ({scope})")
    if args.summary:
        with open(args.summary, "a", encoding="utf-8") as f:
            f.write(md)
    for f in findings:
        print(("BLOCK  " if f["blocking"] else "info   ") + fmt(f))
    blocking = sum(f["blocking"] for f in findings)
    print(f"\n{len(findings)} finding(s), {blocking} blocking (scope: {scope}).")
    return 2 if blocking else 0


if __name__ == "__main__":
    if sys.argv[1:] == ["--record"]:
        sys.exit(run_record_mode())
    if len(sys.argv) > 1:
        parser = argparse.ArgumentParser()
        parser.add_argument("--sarif", action="append", required=True)
        scope = parser.add_mutually_exclusive_group(required=True)
        scope.add_argument("--changed-files")
        scope.add_argument("--all", action="store_true")
        parser.add_argument("--summary")
        sys.exit(run_sarif_mode(parser.parse_args()))
    sys.exit(run_hook_mode())
