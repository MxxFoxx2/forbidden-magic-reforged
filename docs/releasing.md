# Publishing releases

Releases are built and published by the **Release** workflow that runs on every push to a release channel branch: `main` for Minecraft 26.3, `mc/26.1.2` for Minecraft 26.1.2 and `1.21.1` for Minecraft 1.21.1. It checks out the pushed commit, prepares the pinned Thaumcraft host, builds the add-on, verifies the JAR provenance, runs the required GameTests, and publishes only if every check succeeds.

## Version numbers

`release-version.txt` holds the base `MAJOR.MINOR.PATCH`. Each build takes the higher of that base and the version of the previous build on the same channel, and when the base is not higher it bumps the previous patch. So the patch increments on its own, while `1.1.0` or `2.0.0` is released by editing that one file and pushing. The number becomes the add-on version:

- release title: `1.0.37 (NeoForge 26.3)`;
- asset name: `forbidden-magic-1.0.37-26.3.jar`;
- the JAR reports `1.0.37-26.3` in its own mod metadata, because the workflow writes `<version>-<channel>` into `gradle.properties` before building;
- the release notes open with `Version 1.0.37 for Minecraft 26.3` followed by the source commit.

Versions therefore always increase without a manual step; the committed `gradle.properties` keeps its own development value, because the workflow only overrides it inside the build. The chosen version is also stored in the release notes as a `release-version` marker so the next build can continue from it.

## When a release is published

A push publishes a release only if it changes a path in the workflow allowlist: `src/`, `data/`, `tools/gen/`, `tools/gen_resources.py`, `tools/prepare_host.py`, `patches/`, the Gradle inputs (`build.gradle`, `settings.gradle`, `gradle.properties`, `gradle/`, `gradlew`, `gradlew.bat`), `host.sha` or `release-version.txt`. Pushes that touch only documentation, the README, licenses, `.github/` or other paths outside this list do not publish a new version; the regular CI workflow still builds and tests them. The workflow's `paths` filter is authoritative — add every new build input there when it is introduced.

Manual `workflow_dispatch` runs ignore this filter, which is how a workflow change itself can be published on purpose.

## Rolling channels

There is one rolling GitHub release per Minecraft channel, tagged `release/26.3`, `release/26.1.2` and `release/1.21.1` (the `release/` prefix keeps the tags from colliding with branch names like `1.21.1`). Each successful build moves its channel tag to the tested source snapshot and **recreates** the corresponding release with the current jar, so the release date and asset match the newest build. Only the `release/26.3` release is created with the GitHub "Latest" mark — the primary channel — so the two channels do not steal the badge from each other on every build.

The workflow does not create per-build backup or rollback tags. To rebuild an earlier version, run `workflow_dispatch` from the corresponding channel branch and set `source_ref` to the desired commit SHA or an existing tag. Leave it empty to build the selected branch's current head. The selected source must still be available in Git history and must target the selected Minecraft channel; no additional retention is provided.

## Older numbered releases

Releases `0.1.0-beta.1`, `0.1.0` and `0.1.1` are historical: they were cut from tags before the channel scheme existed and were removed together with those tags. Their notes remain under `docs/releases/`; new builds never create releases like that.
