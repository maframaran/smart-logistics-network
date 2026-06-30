---
name: static-code-review
description: Run static code analysis (SonarQube + PMD) across the Maven reactor in this repo and produce a categorized, actionable report grouped by real-issue vs noise. Use whenever the user asks for static analysis, a lint pass, a code quality check, or a "static code review" of this Java codebase.
---

# Static Code Review (Maven / Java 21 Reactor)

This repo (`smart-logistics-network`) has no static analysis plugins wired into the POMs. Run the tools ad hoc via fully-qualified Maven goals — do not add them to any `pom.xml` unless the user explicitly asks for that as a separate, permanent change.

## Prerequisites

- Maven must be on `PATH`. If `mvn` is missing in this environment, install it (`brew install maven`) — see the "Environment setup" section below.
- **Always pin `JAVA_HOME` to a Java 21 JDK**, even if Homebrew's `mvn` pulled in a newer JDK as a dependency (it does — e.g. OpenJDK 26). This project requires Java 21 (`--release 21`, JPMS `module-info.java` in most modules per ADR-001/ADR-004). Building with the wrong JDK gives misleading or broken results.
  ```bash
  /usr/libexec/java_home -V                      # list installed JDKs, find a 21.x entry
  export JAVA_HOME=<path to the 21.x JDK Home>    # e.g. .../ms-21.0.11/Contents/Home
  export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
  ```
- Run `mvn clean install -DskipTests` once first (always `clean`, not just `install` — stale `target/classes` from a prior interrupted/odd build can leave duplicate-class artifacts, e.g. a stray `FleetServiceApplication 2.class`, that break `spring-boot-maven-plugin:repackage` with "Unable to find a single main class"). Per-module plugin invocations (`-pl <module>`) need sibling modules (especially `common`) already present in the local `~/.m2` repo to resolve, even though a full reactor build would handle that automatically.

## Environment Setup (Only If `mvn` Is Not Already Available)

```bash
brew install maven
```
This pulls in a recent OpenJDK as a Maven dependency — that JDK is **not** the one to build with. Find the project's actual Java 21 JDK via `/usr/libexec/java_home -V` and export `JAVA_HOME` to that, per above.

## Running SonarQube (bug/vulnerability/code-smell analysis)

This repo has no SonarQube server wired up and no `sonar-project.properties`. Run a disposable local SonarQube Community Edition via Docker, scan against it, pull results via the Web API, then tear it down — don't leave it running or add permanent project config unless the user asks for that separately.

1. **Start a local SonarQube server** (skip if one is already running on `localhost:9000`):
   ```bash
   docker run -d --name sonar-adhoc -p 9000:9000 sonarqube:lts-community
   ```
2. **Wait for it to be ready** — it takes ~30-60s to start:
   ```bash
   until curl -s http://localhost:9000/api/system/status | grep -q '"status":"UP"'; do sleep 5; done
   ```
3. **Authenticate and generate a token.** Default Community Edition credentials are `admin`/`admin`, and the server forces a password change on first login. Do this via the API (no UI needed):
   ```bash
   curl -s -u admin:admin -X POST "http://localhost:9000/api/users/change_password" \
     --data-urlencode "login=admin" --data-urlencode "previousPassword=admin" --data-urlencode "password=<new-password>"
   TOKEN=$(curl -s -u admin:<new-password> -X POST "http://localhost:9000/api/user_tokens/generate" \
     --data-urlencode "name=adhoc-review" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
   ```
4. **Run the scan from the repo root** — `sonar-maven-plugin` understands the multi-module reactor in one invocation, so no per-module loop is needed:
   ```bash
   mvn -q org.sonarsource.scanner.maven:sonar-maven-plugin:3.11.0.3922:sonar \
     -Dsonar.host.url=http://localhost:9000 -Dsonar.login="$TOKEN" \
     -Dsonar.projectKey=smart-logistics-network -DskipTests
   ```
   - Use `-Dsonar.login`, not `-Dsonar.token` — `sonarqube:lts-community` currently resolves to server version 9.9.8 LTS, whose bundled scanner API only recognizes the older `sonar.login` property; `sonar.token` (the newer name) gets rejected with "Not authorized" even with a valid token.
   - Run `mvn clean install -DskipTests` (already done in Prerequisites) before this step rather than chaining `clean verify` into the same command — running `sonar:sonar` as a separate invocation after the reactor is already built avoids re-triggering `spring-boot-maven-plugin:repackage` per module, which is irrelevant to analysis and slows things down.
   - First run on a fresh project can take a couple of minutes (Sonar computes a full baseline).
5. **Pull findings via the Web API** rather than the browser UI (keeps this scriptable/headless):
   ```bash
   curl -s -u "$TOKEN": "http://localhost:9000/api/issues/search?componentKeys=smart-logistics-network&resolved=false&ps=500" \
     | python3 -m json.tool > /tmp/sonar-issues.json
   ```
   Each issue has `component` (file path), `line`, `rule`, `severity` (`BLOCKER`/`CRITICAL`/`MAJOR`/`MINOR`/`INFO`), `type` (`BUG`/`VULNERABILITY`/`CODE_SMELL`), and `message`. Group/summarize from this JSON rather than pasting the raw blob into the report.
6. **Tear down** when done, unless the user wants the server kept around for a follow-up session:
   ```bash
   docker rm -f sonar-adhoc
   ```

Sonar's default Java ("Sonar way") quality profile covers bugs, vulnerabilities, security hotspots, and code smells in one pass — e.g. cognitive complexity, duplicated blocks, missing `@Override`, string concatenation in loops — broader than PMD's rulesets above, so expect overlap as well as Sonar-only findings.

## Running PMD (style / best-practices / error-prone rules)

The **default** `maven-pmd-plugin` version bundles an old PMD (6.x) that does not understand `targetJdk` `21` and fails immediately with `Unsupported targetJdk value '21'`. Always pin a recent plugin version that bundles PMD 7.x:

```bash
mvn org.apache.maven.plugins:maven-pmd-plugin:3.26.0:check \
  -Dpmd.rulesets=category/java/bestpractices.xml,category/java/errorprone.xml,category/java/codestyle.xml
```
- Running this across the whole reactor in one command also stops at the first module with violations even with `-fae` in some cases (the `check` mojo's failure handling is inconsistent across reactor vs single-module invocation). The reliable approach is a **per-module loop**:
  ```bash
  for m in common shipment-service fleet-service driver-service routing-service \
           warehouse-service billing-service notification-service rag-service acceptance-tests; do
    echo "=== $m ==="
    mvn -q org.apache.maven.plugins:maven-pmd-plugin:3.26.0:check \
      -Dpmd.rulesets=category/java/bestpractices.xml,category/java/errorprone.xml,category/java/codestyle.xml \
      -pl "$m" 2>&1 | grep -E "violations|BUILD SUCCESS"
  done
  ```
- On failure, PMD writes full violation details to `<module>/target/pmd.xml`. Extract them with:
  ```bash
  grep -A2 "<violation" <module>/target/pmd.xml
  ```
- Add more rulesets as needed (`category/java/design.xml`, `category/java/performance.xml`, `category/java/multithreading.xml`) — `design.xml` tends to be noisy on a hexagonal-architecture codebase (flags things like high class coupling between ports/adapters that are intentional), so don't include it by default; mention it's available if the user wants a deeper pass.

## Compiling the report

Group findings into:

1. **Real, actionable issues** — things worth fixing: unused imports/parameters, useless code, genuine mutable-state leaks on value objects/records (e.g. a `record` with a `List` field whose accessor returns the live list instead of a defensive copy), null-deref risks, resource leaks, Sonar `BUG`/`VULNERABILITY` findings.
2. **Likely noise / judgment calls** — flag these separately with a one-line reason, don't just lump them in as "to fix":
   - Mutable-state-exposure findings on constructor params that are **Spring-managed singleton beans** (`KafkaTemplate`, `ObjectMapper`, JPA repository ports, etc.) — defensively copying a singleton bean reference isn't meaningful.
   - Sonar `CODE_SMELL`/`MINOR` findings that conflict with this project's own documented conventions (e.g. hexagonal package structure, record-heavy DTOs) — call these out rather than silently following Sonar's generic Java style preferences over the codebase's established ADRs.
   - Anything in generated code, test fixtures, or `target/`.
3. **Pre-existing vs introduced by recent work** — if the user has been actively changing files in this session, cross-reference findings against those files and call out which ones are regressions from the current work vs longstanding code. This materially changes priority.

Present results as a table per tool (file:line, rule code, severity/type, one-line description) plus a short recommendation section, and let the user pick which to act on — do not auto-fix unless asked.

## Notes from prior runs (update this section as the codebase evolves)

- SonarQube baseline (`sonarqube:lts-community`, server version 9.9.8 LTS): 56 findings, all `CODE_SMELL` (no `BUG`/`VULNERABILITY`) — 8 `CRITICAL`, 27 `MAJOR`, 21 `MINOR`. Breakdown by rule: `S1481` unused local variable ×12 (all `catch (Exception ignored)` blocks in Kafka publishers' `switch` pattern-matching — the variable is intentionally unused, this is a stylistic false-positive-ish finding, not a real bug), `S107` too-many-constructor/method-parameters ×11 (on domain aggregates like `Invoice`, `Driver`, `Notification`, `Route`, `Shipment` — these are wide value objects by design per the domain model, not accidental complexity), `S1192` duplicated string literal ×8 (mix of real "extract a constant" opportunities and SQL column-name strings in `PgVectorStoreAdapter` that are arguably clearer inline), `S5838` AssertJ idiom ×8 (`isEqualTo(0)` → `isZero()`, test files only), `S2925` `Thread.sleep()` in tests ×6 (acceptance-test polling — a real anti-pattern, should poll/await instead of sleeping a fixed duration), `S5778` JUnit lambda-with-multiple-throwing-calls ×6 (test files), `S112` generic `Exception` thrown ×2, plus one each of `S1068` unused field, `S3358` nested ternary, `S1128` unused import. **Gotcha:** this server/plugin combo (SonarQube 9.9.8 LTS + `sonar-maven-plugin:3.11.0.3922`) rejects `-Dsonar.token` with "Not authorized" — use `-Dsonar.login=<token>` instead (the newer `sonar.token` property requires a newer server).
- PMD baseline: 0 violations across all 10 modules with the bestpractices/errorprone/codestyle rulesets as of the last PMD-only run.
