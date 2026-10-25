# Publishing releases

Releases are published from **Git tags**, not from a temporary release branch. The Release workflow checks out the exact tagged commit, prepares the pinned and locally patched Thaumcraft host, builds the add-on, verifies the JAR provenance, runs the required GameTests in both `no_lust` modes, and publishes only if every check succeeds.

## Prepare a release

1. Merge the code and `.github/workflows/release.yml` into `main`.
2. Add release notes at `docs/releases/<tag>.md` (for example, `docs/releases/0.1.0-beta.2.md`) and commit them before creating the tag.
3. Create and push a tag pointing to the commit containing the workflow and notes:
   ```bash
   git tag 0.1.0-beta.2
   git push origin 0.1.0-beta.2
   ```
4. Open the Release workflow run and confirm it passed. It is safe to keep the tag and release history; there is no temporary branch to clean up.

Use `MAJOR.MINOR.PATCH` for a normal release (for example, `0.1.0`) and `MAJOR.MINOR.PATCH-beta.N` for a beta (for example, `0.1.0-beta.2`). The workflow automatically marks tags with a suffix such as `-beta.2` as GitHub pre-releases; stable tags are published as normal releases. Supported prerelease suffixes are `alpha.N`, `beta.N`, and `rc.N`.

The workflow derives the mod artifact version from the tag at build time, so the artifact for `0.1.0-beta.2` is named `forbidden-magic-0.574-26.3-port.0.1.0-beta.2.jar`. It does not change the committed `gradle.properties`.

A manual `workflow_dispatch` run accepts an **existing tag** and runs the same checks. The workflow intentionally refuses to overwrite an existing GitHub Release. Do not move or reuse published tags. A tag only triggers the automatic workflow if the workflow file exists at that tagged commit, so merge the release workflow before tagging.
