#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""Erzeugt einen Markdown-Bericht zu individuellen GitLab-Beiträgen.

Hinweis:
- LOC (additions/deletions/net) kann für definierte Nutzer ausgeschlossen werden,
  damit versehentlich gepushte Repomix-Änderungen die Statistik nicht verfälschen.
"""

import urllib.request, urllib.parse, json, time, sys, re
from collections import defaultdict, OrderedDict

# ==== Konfiguration ====
BASE_URL   = "https://stl-gitlab.htwsaar.de"         # oder https://gitlab.example.com
PROJECT_ID = "todo"                      # GitLab-Projekt-ID
TOKEN      = "todo"   # Dein Personal Access Token

# Zeitraum (für Commit-Statistiken + Timelogs)
SINCE = None    # z.B. "2025-03-01" oder None
UNTIL = None    # z.B. "2025-07-31" oder None

# Nutzer, deren LOC nicht gezählt werden soll (z. B. wegen Repomix-Commits).
# Vergleich erfolgt robust über lower-case auf Name/Username.
EXCLUDED_LOC_IDENTITIES = {
    "sosa", "sosa00004", "sosa0004",
    "xuzh", "xuzh00003", "xuzh0003",
}

# -- Zusatzhelfer für Status/Typ (minimal-invasiv) --
ALLOWED_TYPE_LABELS = [
    "type::user story","type::tech story","type::nfa","type::task","type::bug","type::doc"
]
def _type_label_markdown(labels):
    # labels kann Liste aus Strings oder Dicts sein (GitLab variiert)
    names = []
    if isinstance(labels, (list, tuple, set)):
        for lb in labels:
            if isinstance(lb, str):
                names.append(lb)
            elif isinstance(lb, dict):
                n = lb.get('name') or lb.get('title')
                if n: names.append(n)
    elif isinstance(labels, dict):
        n = labels.get('name') or labels.get('title')
        if n: names.append(n)
    for t in ALLOWED_TYPE_LABELS:
        if t in names:
            return f'~"{t}"'
    for n in names:
        if isinstance(n, str) and n.lower().startswith('type::'):
            return f'~"{n}"'
    return '-'

def _status_de(state):
    return 'offen' if state=='opened' else ('geschlossen' if state=='closed' else (state or ''))

def _normalize_identity(value):
    """Normalisiert Name/Username für robusten Identity-Vergleich."""
    if not value:
        return ""
    return str(value).strip().lower()

def _is_loc_excluded_member(member_info):
    """Prüft, ob LOC für dieses Mitglied ausgeschlossen werden soll."""
    if not isinstance(member_info, dict):
        return False
    return (
        _normalize_identity(member_info.get("name")) in EXCLUDED_LOC_IDENTITIES
        or _normalize_identity(member_info.get("username")) in EXCLUDED_LOC_IDENTITIES
    )

# Commits: optionaler Branch für "main"-Sicht (None = default branch)
REF_NAME = None

# Performance/Grenzen
MAX_COMMITS_PER_USER = 3000     # harte Obergrenze pro Nutzer (nur-main-Modus)
PER_BRANCH_LIMIT      = 1000    # max Commits je Branch/Nutzer (alle-Branches-Dedupliz.)
SLEEP_SECS = 0.03               # kleine Pause zwischen Paginierungs-Calls
# =======================

def _enc(s):  # REST: fullPath codieren
    return urllib.parse.quote_plus(str(s), safe="")

def http_get(path, params=None, timeout=60):
    q = urllib.parse.urlencode(params or {})
    url = f"{BASE_URL}{path}"
    if q:
        url = f"{url}?{q}"
    req = urllib.request.Request(url)
    req.add_header("PRIVATE-TOKEN", TOKEN)
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        body = resp.read()
        data = json.loads(body.decode("utf-8"))
        hdrs = {k: v for k, v in resp.headers.items()}
        return data, hdrs

def http_post_json(path, payload, timeout=90):
    url = f"{BASE_URL}{path}"
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    req.add_header("PRIVATE-TOKEN", TOKEN)
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        body = resp.read()
        data = json.loads(body.decode("utf-8"))
        return data

def paged_get(path, params=None, limit=None):
    params = dict(params or {})
    per_page = 100
    page = 1
    count = 0
    while True:
        params.update({"per_page": per_page, "page": page})
        data, hdr = http_get(f"/api/v4{path}", params=params)
        if not isinstance(data, list):
            yield data
            return
        if not data:
            return
        for it in data:
            yield it
            count += 1
            if limit and count >= limit:
                return
        nxt = hdr.get("X-Next-Page")
        if nxt and nxt not in ("", "0"):
            page = int(nxt)
            time.sleep(SLEEP_SECS)
            continue
        return

def get_project():
    proj, _ = http_get(f"/api/v4/projects/{_enc(PROJECT_ID)}")
    return proj

def get_members():
    members = list(paged_get(f"/projects/{_enc(PROJECT_ID)}/members/all"))
    return {m["id"]: {"name": m["name"], "username": m.get("username","")} for m in members}

def get_issues():
    params = {"state":"all","scope":"all","order_by":"created_at","sort":"asc"}
    issues = list(paged_get(f"/projects/{_enc(PROJECT_ID)}/issues", params=params))
    for i in issues:
        if not i.get("web_url"):
            i["web_url"] = f'{BASE_URL}/-/issues/{i["iid"]}'
    return issues

def get_merge_requests_by_author(uid):
    counts = {"opened":0,"merged":0,"closed":0}
    for state in ("opened","merged","closed"):
        params = {"state": state, "author_id": uid, "scope":"all"}
        mrs = list(paged_get(f"/projects/{_enc(PROJECT_ID)}/merge_requests", params=params, limit=2000))
        counts[state] = len(mrs)
    return counts

# ---------- Timelogs via GraphQL ----------
def gid_for_project(pid): return f"gid://gitlab/Project/{pid}"
def projectid_value(pid):
    s = str(pid)
    if s.startswith("gid://gitlab/Project/"): return s
    if "/" in s: return s
    return gid_for_project(s)

GQL = (
    "query TL($projectId: ProjectID!, $first: Int!, $after: String, $startDate: Time, $endDate: Time) {"
    "  timelogs(projectId: $projectId, first: $first, after: $after, startDate: $startDate, endDate: $endDate) {"
    "    pageInfo { hasNextPage endCursor }"
    "    nodes { timeSpent user { id } issue { iid } }"
    "  }"
    "}"
)

def fetch_timelogs():
    vars = {"projectId": projectid_value(PROJECT_ID), "first":100, "after":None,
            "startDate": SINCE, "endDate": UNTIL}
    while True:
        data = http_post_json("/api/graphql", {"query": GQL, "variables": vars})
        if "errors" in data and data["errors"]:
            raise RuntimeError(f"GraphQL-Fehler: {data['errors']}")
        tl = data["data"]["timelogs"]
        for n in tl["nodes"]:
            yield n
        if tl["pageInfo"]["hasNextPage"]:
            vars["after"] = tl["pageInfo"]["endCursor"]
            time.sleep(SLEEP_SECS)
            continue
        break

# ---------- Commits & LOC ----------
def commits_for_author(author_str, since=None, until=None, ref_name=None, try_with_stats=True):
    params = {"author": author_str}
    if since: params["since"] = since
    if until: params["until"] = until
    if ref_name: params["ref_name"] = ref_name
    if try_with_stats: params["with_stats"] = "true"
    return list(paged_get(f"/projects/{_enc(PROJECT_ID)}/repository/commits", params=params, limit=MAX_COMMITS_PER_USER))

def commit_detail(sha):
    params = {"stats": "true"}
    c, _ = http_get(f"/api/v4/projects/{_enc(PROJECT_ID)}/repository/commits/{sha}", params=params)
    return c

def list_branches():
    branches = list(paged_get(f"/projects/{_enc(PROJECT_ID)}/repository/branches"))
    return [b["name"] for b in branches]

def commits_for_author_all_branches(author_str, since=None, until=None, try_with_stats=True, limit_per_branch=1000):
    shas_seen = set()
    commits = []
    for br in list_branches():
        params = {"author": author_str, "ref_name": br}
        if since: params["since"] = since
        if until: params["until"] = until
        if try_with_stats: params["with_stats"] = "true"
        for c in paged_get(f"/projects/{_enc(PROJECT_ID)}/repository/commits", params=params, limit=limit_per_branch):
            sha = c.get("id")
            if sha and sha not in shas_seen:
                shas_seen.add(sha)
                commits.append(c)
        time.sleep(SLEEP_SECS)
    return commits

ISSUE_REF_RE = re.compile(r"(?:#|/-/issues/)(\d+)")

def count_issue_refs_in_message(msg, known_iids_set):
    refs = set()
    if not msg: return refs
    for m in ISSUE_REF_RE.findall(msg):
        try:
            iid = int(m)
            if iid in known_iids_set:
                refs.add(iid)
        except:
            pass
    return refs

def main():
    # Projekt + Default Branch (für die "main"-Sicht)
    proj = get_project()
    default_branch = proj.get("default_branch")
    branch = REF_NAME or default_branch

    members = get_members()
    issues  = get_issues()

    # Issue-Metadaten
    issue_meta = {
        i["iid"]: {
            "title": i["title"],
            "url":   i["web_url"],
            "assignees": [a["id"] for a in (i.get("assignees") or [])],
            "state": i.get("state",""),
            "status_de": _status_de(i.get("state")),
            "type_label": _type_label_markdown(i.get("labels", []))
        } for i in issues
    }
    known_iids = set(issue_meta.keys())

    # Verantwortungen
    responsible_by_user = defaultdict(list)
    for iid, meta in issue_meta.items():
        for uid in meta["assignees"]:
            responsible_by_user[uid].append(iid)

    # Mitarbeit via Timelogs
    time_by_user_issue = defaultdict(lambda: defaultdict(int))  # uid -> iid -> seconds
    try:
        for n in fetch_timelogs():
            user_gid = (n.get("user") or {}).get("id")
            try:
                uid = int(str(user_gid).rsplit("/",1)[-1])
            except:
                uid = None
            issue = (n.get("issue") or {}).get("iid")
            sec   = int(n.get("timeSpent") or 0)
            if uid and issue and sec:
                time_by_user_issue[uid][int(issue)] += sec
    except Exception as e:
        print(f"Warnung: Timelogs konnten nicht gelesen werden: {e}", file=sys.stderr)

    # Code-Statistiken & per-issue Aggregation (alle Branches)
    code_stats = {}  # uid -> {"main": {...}, "all": {...}}
    commits_issue_all = defaultdict(lambda: defaultdict(int))  # uid -> iid -> commits
    add_issue_all     = defaultdict(lambda: defaultdict(int))  # uid -> iid -> additions
    del_issue_all     = defaultdict(lambda: defaultdict(int))  # uid -> iid -> deletions

    for uid, info in members.items():
        name = info["name"]; username = info["username"]
        exclude_loc = _is_loc_excluded_member(info)

        # A) nur main/ref_name
        commits_main = commits_for_author(name, SINCE, UNTIL, branch, try_with_stats=True)
        if not commits_main and username:
            commits_main = commits_for_author(username, SINCE, UNTIL, branch, try_with_stats=True)

        cm_commits = 0; cm_add = 0; cm_del = 0; cm_issue_ref = 0; cm_issue_refs_seen = set()
        for c in commits_main:
            cm_commits += 1
            st = c.get("stats")
            if not st:
                try:
                    detail = commit_detail(c["id"])
                    st = detail.get("stats")
                except Exception:
                    st = None
            if st and not exclude_loc:
                cm_add += int(st.get("additions", 0))
                cm_del += int(st.get("deletions", 0))
            refs = count_issue_refs_in_message(c.get("message",""), known_iids)
            if refs:
                cm_issue_ref += 1
                cm_issue_refs_seen |= refs

        # B) alle Branches (Totals + per-issue)
        commits_all = commits_for_author_all_branches(name, SINCE, UNTIL, try_with_stats=True, limit_per_branch=PER_BRANCH_LIMIT)
        if not commits_all and username:
            commits_all = commits_for_author_all_branches(username, SINCE, UNTIL, try_with_stats=True, limit_per_branch=PER_BRANCH_LIMIT)

        ca_commits = 0; ca_add = 0; ca_del = 0
        for c in commits_all:
            ca_commits += 1
            st = c.get("stats")
            if not st:
                try:
                    detail = commit_detail(c["id"])
                    st = detail.get("stats")
                except Exception:
                    st = None
            additions = int(st.get("additions", 0)) if st and not exclude_loc else 0
            deletions = int(st.get("deletions", 0)) if st and not exclude_loc else 0
            ca_add += additions
            ca_del += deletions

            refs = count_issue_refs_in_message(c.get("message",""), known_iids)
            for iid in refs:
                commits_issue_all[uid][iid] += 1
                add_issue_all[uid][iid]     += additions
                del_issue_all[uid][iid]     += deletions

        code_stats[uid] = {
            "main": {
                "branch": branch or "Default",
                "commits": cm_commits,
                "additions": cm_add,
                "deletions": cm_del,
                "net": cm_add - cm_del,
                "commit_issue_ref": cm_issue_ref,
                "issue_ids_referenced": sorted(list(cm_issue_refs_seen))[:20],
            },
            "all": {
                "commits": ca_commits,
                "additions": ca_add,
                "deletions": ca_del,
                "net": ca_add - ca_del,
            }
        }
        time.sleep(SLEEP_SECS)

    # Merge-Request-Zahlen (einmalig pro Nutzer ermitteln)
    mr_counts_by_user = {}
    for uid in members.keys():
        try:
            mr_counts_by_user[uid] = get_merge_requests_by_author(uid)
        except Exception:
            mr_counts_by_user[uid] = {"opened":0,"merged":0,"closed":0}
        time.sleep(SLEEP_SECS)

    # ---------- Übersichtstabelle (alle Mitglieder) ----------
    print("# 3. Individuelle Beiträge\n\n")
    print("## Übersicht\n\n")
    print("Folgende Tabelle zeigt eine Übersicht der individuellen Beiträge:\n\n")

    headers = [
        "Mitglied",
        "Commits main",
        "Commits alle",
        "LOC main  (+/-/=)",
        "LOC alle  (+/-/=)",
        "Merge Requests (o/m/c)",
        "Issues (Assignee)",
        "Issues (Mitarbeit)",
        "Stunden"
    ]
    print("| " + " | ".join(headers) + " |")
    print("| " + " | ".join(["---"]*len(headers)) + " |")

    ordered_users = sorted(members.items(), key=lambda kv: kv[1]["name"].lower())
    for uid, info in ordered_users:
        name = info["name"]
        resp_iids = set(responsible_by_user.get(uid, []))
        contrib_iids = set(time_by_user_issue.get(uid, {}).keys())
        hours_total = round(sum(time_by_user_issue.get(uid, {}).values())/3600.0, 2)

        cs = code_stats.get(uid, {"main":{}, "all":{}})
        cm = cs["main"]; ca = cs["all"]
        mr = mr_counts_by_user.get(uid, {"opened":0,"merged":0,"closed":0})

        loc_main = f"+{cm.get('additions',0)}/-{cm.get('deletions',0)}/{cm.get('net',0)}"
        loc_all  = f"+{ca.get('additions',0)}/-{ca.get('deletions',0)}/{ca.get('net',0)}"
        mr_col   = f"{mr['opened']}/{mr['merged']}/{mr['closed']}"

        print(
            f"| {name} | {cm.get('commits',0)} | {ca.get('commits',0)} | "
            f"{loc_main} | {loc_all} | {mr_col} | "
            f"{len(resp_iids)} | {len(contrib_iids)} | {hours_total:.2f} |"
        )

    # ---------- Detail je Mitglied ----------
    print("\nIm folgenden werden die Details für jedes Gruppenmitglied dargestellt.\n")

    for uid, info in ordered_users:
        name = info["name"]
        cs = code_stats.get(uid, {"main":{}, "all":{}})
        cm = cs["main"]; ca = cs["all"]
        print(f"\n## {name}\n")
        print("\n### Statistik\n")

        # Zuständig (Assignee) – Tabelle
        resp_iids = sorted(set(responsible_by_user.get(uid, [])))
        if resp_iids:
            rows = []
            for iid in resp_iids:
                m = issue_meta[iid]
                hours = round((time_by_user_issue[uid].get(iid, 0))/3600.0, 2)
                commits_cnt = commits_issue_all[uid].get(iid, 0)
                add = add_issue_all[uid].get(iid, 0)
                dele = del_issue_all[uid].get(iid, 0)
                net = add - dele
                rows.append((iid, m["title"], m["url"], hours, commits_cnt, add, dele, net))
            rows.sort(key=lambda t: t[3], reverse=True)
            print("**Zuständig:**\n")
            print("| Issue | Status | Typ | Stunden | Commits | LOC (+/-/=) |")
            print("|---|:--|:--|---:|---:|---:|")
            for iid, title, url, hours, commits_cnt, add, dele, net in rows:
                meta = issue_meta.get(iid, {})
                status = meta.get("status_de","")
                typ = meta.get("type_label","-")
                print(f"| [#{iid} {title}]({url}) | {status} | {typ} | {hours:.2f} | {commits_cnt} | +{add}/-{dele}/{net} |")
        else:
            print("_Keine Assignee-Issues._")
        print("")

        # Mitgearbeitet (Timelogs) – Tabelle
        contrib = time_by_user_issue.get(uid, {})
        contrib_filtered = [(iid, sec) for iid, sec in contrib.items() if iid not in resp_iids]
        if contrib_filtered:
            print("**Mitgearbeitet:**\n")
            print("| Issue | Status | Typ | Stunden | Commits | LOC (+/-/=) |")
            print("|---|:--|:--|---:|---:|---:|")
            for iid, sec in sorted(contrib_filtered, key=lambda t: t[1], reverse=True):
                m = issue_meta.get(iid)
                title = m["title"] if m else f"Issue {iid}"
                url   = m["url"] if m else ""
                hours = round(sec/3600.0, 2)
                commits_cnt = commits_issue_all[uid].get(iid, 0)
                add = add_issue_all[uid].get(iid, 0)
                dele = del_issue_all[uid].get(iid, 0)
                net = add - dele
                status = (m.get("status_de","") if m else "")
                typ = (m.get("type_label","-") if m else "-")
                print(f"| [#{iid} {title}]({url}) | {status} | {typ} | {hours:.2f} | {commits_cnt} | +{add}/-{dele}/{net}")
        else:
            print("_Keine Timelog-Mitarbeit (außer ggf. auf eigenen Assignee-Issues)._")
        print("\n### Bericht\n")



if __name__ == "__main__":
    try:
        main()
    except urllib.error.HTTPError as e:
        txt = e.read().decode("utf-8", errors="ignore")
        print(f"HTTP-Fehler: {e.code} {txt}", file=sys.stderr)
        sys.exit(2)
    except Exception as e:
        print(f"Fehler: {e}", file=sys.stderr)
        sys.exit(2)
