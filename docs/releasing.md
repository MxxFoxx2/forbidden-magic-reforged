# Publishing releases

Releases are built and published by the **Release** workflow that runs on every push to a release channel branch: `main` for Minecraft 26.3 and `1.21.1` for Minecraft 1.21.1. It checks out the pushed commit, prepares the pinned Thaumcraft host, builds the add-on, verifies the JAR provenance, runs the required GameTests, and publishes only if every check succeeds.

## Version numbers

Every build gets its own version, `1.0.<workflow run number>`, and that number is the add-on version:

- release title: `1.0.37 (NeoForge 26.3)`;
- asset name: `forbidden-magic-1.0.37-26.3.jar`;
- the JAR reports `1.0.37` in its own mod metadata, because the workflow writes it into `gradle.properties` before building;
- the release notes open with `Version 1.0.37 for Minecraft 26.3` followed by the source commit.

The run number is monotonic, so versions always increase and never need to be bumped by hand. The committed `gradle.properties` keeps its own development value; the workflow only overrides it inside the build.

## Rolling channels

There is one rolling GitHub release per Minecraft channel, tagged `26.3` and `1.21.1`. Each successful build **recreates** its release with the current jar, so the release date and the asset always match the newest build. The tag is preserved across the recreation.

For rollback, every 26.3 build also pushes an `archive/releases/26.3/<source sha>` tag. A manual `workflow_dispatch` run accepts such a tag and rebuilds it; the workflow refuses to overwrite an existing numbered release.

## Older numbered releases

Releases `0.1.0-beta.1`, `0.1.0` and `0.1.1` are historical: they were cut from tags before the channel scheme existed. They stay published, but new builds never create releases like that.
