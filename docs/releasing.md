# Publishing releases

Releases are built and published by the **Release** workflow that runs on every push to a release channel branch: `main` for Minecraft 26.3 and `1.21.1` for Minecraft 1.21.1. It checks out the pushed commit, prepares the pinned Thaumcraft host, builds the add-on, verifies the JAR provenance, runs the required GameTests, and publishes only if every check succeeds.

## Version numbers

`release-version.txt` holds the base `MAJOR.MINOR.PATCH`. Each build takes the higher of that base and the version of the previous build on the same channel, and when the base is not higher it bumps the previous patch. So the patch increments on its own, while `1.1.0` or `2.0.0` is released by editing that one file and pushing. The number becomes the add-on version:

- release title: `1.0.37 (NeoForge 26.3)`;
- asset name: `forbidden-magic-1.0.37-26.3.jar`;
- the JAR reports `1.0.37` in its own mod metadata, because the workflow writes it into `gradle.properties` before building;
- the release notes open with `Version 1.0.37 for Minecraft 26.3` followed by the source commit.

Versions therefore always increase without a manual step; the committed `gradle.properties` keeps its own development value, because the workflow only overrides it inside the build. The chosen version is also stored in the release notes as a `release-version` marker so the next build can continue from it.

## When a release is published

A push publishes a release only if it changes files that build into the mod or drive the release: `src/`, `tests/`, `data/`, `tools/`, `patches/`, the Gradle inputs (`build.gradle`, `settings.gradle`, `gradle.properties`, `gradle/`, `gradlew`, `gradlew.bat`), `host.sha` or `release-version.txt`. Pushes that touch only docs, the README, licenses, `.github/` or other repository service files are ignored, so documentation and CI tweaks do not create a new version; the regular CI workflow still builds and tests them. The allowlist lives in the workflow's `paths` filter — a new build input must be added there or its pushes will not release.

Manual `workflow_dispatch` runs ignore this filter, which is how a workflow change itself can be published on purpose.

## Rolling channels

There is one rolling GitHub release per Minecraft channel, tagged `release/26.3` and `release/1.21.1` (the `release/` prefix keeps the tags from colliding with the `1.21.1` branch name). Each successful build **recreates** its release with the current jar, so the release date and the asset always match the newest build. The tag is preserved across the recreation. Only the `release/26.3` release is created with the GitHub "Latest" mark — the primary channel — so the two channels do not steal the badge from each other on every build.

For rollback, every 26.3 build also pushes an `archive/releases/26.3/<source sha>` tag. A manual `workflow_dispatch` run accepts such a tag and rebuilds it; the workflow refuses to overwrite an existing numbered release.

## Older numbered releases

Releases `0.1.0-beta.1`, `0.1.0` and `0.1.1` are historical: they were cut from tags before the channel scheme existed and were removed together with those tags. Their notes remain under `docs/releases/`; new builds never create releases like that.
