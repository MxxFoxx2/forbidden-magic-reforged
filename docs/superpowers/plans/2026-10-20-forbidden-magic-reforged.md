# Forbidden Magic: Reforged Port — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Forbidden Magic's seven sin aspects exist on Thaumcraft 4: Reforged (MC 26.3) and prove the whole chain — aspect → tag → item → drop → research → recipe — on the Deadly Shards set.

**Architecture:** A standalone add-on mod in `D:\Vibing\Minecraft\forbidden-magic-reforged\`. The host stays byte-identical to its upstream clone; the one required host change (`Aspect` enum → registry-backed class) is developed in a pinned copy under `host/` and shipped as `patches/*.patch`. Everything else uses the host's documented add-on API.

**Tech Stack:** Minecraft 26.3, NeoForge 26.3.0.57-beta, Java 25, ModDevGradle 2.0.148, Gradle 9.2.1, Python 3 (generators, no datagen), GameTest as the only test harness.

**Spec:** `docs/superpowers/specs/2026-10-20-forbidden-magic-reforged-design.md` (read it with this plan; it carries the measurements and the reasoning)

## Global Constraints

Every task below obeys these. Values are copied verbatim from the spec.

- Host working tree `D:\Vibing\Minecraft\thaumcraft-4-reforged` must stay clean: `git -C ../thaumcraft-4-reforged status --porcelain` shows nothing for tracked files. Never edit it; never commit to it.
- Host is pinned by sha in `host.sha`. Current pin: `69a47a1b238e3ac15fa5d4b10e61ccb6a4a80dbf` ("fix: restore thaumonomicon creation sparkles", 2026-10-20). The host moves daily — it gained two commits on 2026-10-20 alone after our first release pin — so before every increment run `git -C ../thaumcraft-4-reforged fetch && git -C ../thaumcraft-4-reforged log --oneline HEAD..origin/main` and re-pin only when the new commits touch our patch targets (`aspect/Aspect.java`, `aspect/AspectSync.java`, `entity/AspectOrb.java`, host `build.gradle`).
- Patched host reports mod version exactly `1.2`. Not `1.1`, not `1.2-fm.1` (a pre-release qualifier sorts below `1.2` and fails our own range).
- Add-on declares `modId="thaumcraft"` dependency `type="required"`, `versionRange="[1.2,)"`, `ordering="AFTER"`.
  На Tasks 1-3 диапазон `[1.1,)`: в руках ещё непропатченный хост, который сообщает `1.1`, и с `[1.2,)`
  аддон не загрузится и `mod_loads_and_host_is_present` не запустится. Task 4 переключает диапазон на
  `[1.2,)` одновременно с переходом на maven-артефакт пропатченного хоста.
- `modId` of the port: `forbiddenmagic`. `displayName`: `Forbidden Magic (Reforged Port)`. Version: `0.574-26.3-port.0.1.0`.
- Package root: `dev.visfox.forbiddenreforged` (attribution to the original author is preserved).
- Java: 4 spaces, **no comments**. Python: 4 spaces, double quotes, `pathlib`, no comments.
- Commits in English, `feat:` / `fix:` / `docs:` / `chore:`, no co-authorship lines.
- Resources are produced by Python generators in `tools/`; datagen is not used; `src/generated/resources` is never hand-edited; re-running the generator must leave `git status` empty.
- lang: `en_us` and `ru_ru` only. Wording of the original FM is preserved.
- No host class and no host asset may end up in our jar. Enforced by `tools/check_provenance.py` in every gate.
- Parity: numbers, aspect costs, research text, recipe shapes come from the 1.7.10 source unchanged. Deviations go to `docs/deviations.md`.
- Commands run from Git Bash on this Windows box: Gradle through `cmd //c gradlew.bat <task>`, Python through `python` (3.14). `python3` does not exist here.
- Build of the host takes ~7 minutes; the first build of the add-on ~2 minutes.

## File Structure

```
forbidden-magic-reforged/
  settings.gradle                         pluginManagement + foojay, rootProject.name
  gradle.properties                       versions, mod metadata for expansion
  build.gradle                            ModDevGradle, runs, local maven, dependency on thaumcraft
  host.sha                                pinned upstream sha
  patches/0001-aspect-registry.patch      Aspect enum -> registry
  patches/0002-aspect-addon-init.patch    add-on registration point
  patches/0003-host-publish.patch         maven-publish for the host copy
  tools/prepare_host.py                   clone + patches + build + publish
  tools/check_provenance.py               host code/assets must not be in our jar
  tools/check_host_clean.py               host tree untouched
  tools/gen_resources.py                  models, blockstates, item definitions, tags
  tools/id_map.json                       1.7.10 meta -> our ids
  tools/lang/en_us.toml, tools/lang/ru_ru.toml   source of truth for lang
  src/main/templates/META-INF/neoforge.mods.toml
  src/main/java/dev/visfox/forbiddenreforged/
    ForbiddenMagic.java                   @Mod entry point, registration order
    FmConfig.java                         STARTUP config, includes no_lust
    FmItems.java                          DeferredRegister.Items
    FmAspects.java                        the seven aspects + availability check
    FmAspectTags.java                     AspectRegistrar subclass (DarkAspects port)
    FmResearch.java                       category and entries
    FmRecipes.java                        recipe registrations
    FmDrops.java                          shard drop rules
  tests/java/dev/visfox/forbiddenreforged/test/
    FmGameTests.java                      all game tests
  LICENSE                                 MIT for the port's code
  NOTICE                                  WTFPL original and host ARR provenance
  src/main/resources/assets/forbiddenmagic/  textures from FM
  src/generated/resources/                   generator output
  docs/deviations.md, docs/roadmap.md, docs/dropped.md, docs/upstream.md
```

Mapping to spec increments: increment 0 = Tasks 1-4, increment 1 = Tasks 5-6, increment 2 = Tasks 7-8, increment 3 = Task 9, increment 4 = Task 10, increment 5 = Task 11.

---

### Task 1: Repository scaffold that loads next to the host

**Files:**
- Create: `settings.gradle`, `gradle.properties`, `build.gradle`, `.gitattributes`
- Create: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties` (generic Gradle 9.2.1 wrapper, copied from the host clone; it contains no host code)
- Create: `src/main/templates/META-INF/neoforge.mods.toml`
- Create: `src/main/java/dev/visfox/forbiddenreforged/ForbiddenMagic.java`
- Create: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`
- Create: `tools/check_host_clean.py`, `tools/check_provenance.py`

**Interfaces:**
- Consumes: nothing.
- Produces: gradle tasks `build`, `runGameTestServer`; `ForbiddenMagic.MODID == "forbiddenmagic"`; `FmGameTests.register(IEventBus)` and a `FUNCTIONS` registry that later tasks add tests to.

- [ ] **Step 1: Write the failing test.** `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java` — the pattern is copied from the host's `thaumcraft/test/GearGameTests.java:47-77`:

```java
package dev.visfox.forbiddenreforged.test;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);
    private static final String[] TESTS = {"mod_loads_and_host_is_present"};

    static {
        FUNCTIONS.register(TESTS[0], () -> FmGameTests::modLoadsAndHostIsPresent);
    }

    private FmGameTests() {
    }

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
    }

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "fm_default"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.parse("thaumcraft:test/platform"), 100, 0, true))
            );
        }
    }

    private static void modLoadsAndHostIsPresent(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("thaumcraft"), "thaumcraft must be loaded");
        helper.succeed();
    }
}
```

The pattern is copied from the host's own `thaumcraft/test/GearGameTests.java:47-77`. The structure template is referenced **by identifier at runtime** (`thaumcraft:test/platform`): it ships inside the host jar, generated to `src/generated/resources/data/thaumcraft/structure/test/platform.nbt`, so the add-on copies no host file and the template resolves without packs. `--tests "${mod_id}:*"` restricts the run to our own three tests: the host's tests register into the same server through `RegisterGameTestsEvent` regardless of `neoforge.enabledGameTestNamespaces`, and one of them (`altar_guardian_population`) is sensitive to the RNG/order of the extended suite. Host tests are therefore a **separate gate**: the host's own `runGameTestServer` in `host/` runs 256/256, and the add-on's run checks only its own namespace. Verified 2026-10-21: the unfiltered run executed 259 tests and failed on the host's altar test; the filtered run executes 3 and the host-only run executes 256, both green.

**Constructor wiring contract.** The mod constructor is the only place anything registers, and later tasks append exactly one call each, in this order (it matters: the config must load before the aspects, because `no_lust` gates the `luxuria` registration itself — the original reads `Config.configurate()` before `DarkAspects.initAspects()` in `Forbidden.prelude`; then aspects must exist before the tag table references them, items before recipes reference them, the availability check before anything touches the host API):

```java
    public ForbiddenMagic(IEventBus modEventBus, ModContainer modContainer) {
        FmConfig.register(modContainer);
        FmAspects.init();
        FmAspectTags.register();
        FmItems.ITEMS.register(modEventBus);
        FmDrops.register();
        FmResearch.register();
        FmRecipes.register();
        FmGameTests.register(modEventBus);
    }
```

Task 1 lands the last line only; each task from 5 to 10 adds its own line and nothing else. `FmConfig` arrives in Task 5 carrying the `no_lust` switch alone, because the aspect guard reads it; Task 7 extends that same class with the remaining switches and must not move the registration line or redefine `no_lust`.

- [ ] **Step 2: Run it and see it fail.** `cmd //c gradlew.bat build` → expected: failure, `settings.gradle` / sources missing (no project yet). This is the red state.

- [ ] **Step 3: Write the scaffold.**

The wrapper is copied verbatim from the host clone, because the port repo starts without one and `gradlew.bat` is the only supported entry point on this machine:

```bash
cp ../thaumcraft-4-reforged/gradlew ../thaumcraft-4-reforged/gradlew.bat .
mkdir -p gradle/wrapper
cp ../thaumcraft-4-reforged/gradle/wrapper/gradle-wrapper.jar ../thaumcraft-4-reforged/gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
```

`settings.gradle`:

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}

rootProject.name = 'forbidden-magic-reforged'
```

`gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx3G
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true

java_version=25
minecraft_version=26.3
minecraft_version_range=[26.3]
neo_version=26.3.0.57-beta

mod_id=forbiddenmagic
mod_name=Forbidden Magic (Reforged Port)
mod_version=0.574-26.3-port.0.1.0
mod_group_id=dev.visfox.forbiddenreforged
mod_authors=SpitefulFox (original); port author (port)
mod_credits=Forbidden Magic by SpitefulFox; Thaumcraft 4: Reforged by its author; Thaumcraft by Azanor
mod_description=Unofficial port of Forbidden Magic 0.574 to Thaumcraft 4: Reforged. Developed with AI assistance.
thaumcraft_version=1.1
terrablender_version=26.3-26.3.0.0.9
```

`thaumcraft_version=1.1` — временно: непропатченный хост в `../thaumcraft-4-reforged/build/libs/` сообщает
именно `1.1`, и с `[1.2,)` аддон был бы отвергнут до запуска теста. Task 4 поднимает это значение до
`1.2` одновременно с переходом на опубликованный пропатченный хост.

`build.gradle` (dependency on the host comes from the local maven repo created in Task 4; until then the same jar is referenced by path, which is why `localRuntime` exists):

```groovy
plugins {
    id 'java-library'
    id 'net.neoforged.moddev' version '2.0.148'
}

version = mod_version
group = mod_group_id

def upstreamHostJar = rootDir.toPath().resolveSibling("thaumcraft-4-reforged").resolve("build/libs/thaumcraft-4-reforged-1.1-26.3.jar").toFile()

base {
    archivesName = "forbidden-magic"
}

java.toolchain.languageVersion = JavaLanguageVersion.of(Integer.parseInt(java_version))

neoForge {
    version = project.neo_version

    runs {
        client {
            client()
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        gameTestServer {
            type = "gameTestServer"
            programArgument '--tests'
            programArgument "${mod_id}:*"
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        configureEach {
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        "${mod_id}" {
            sourceSet(sourceSets.main)
        }
    }
}

repositories {
    maven { url = uri("maven") }
    maven {
        name = "Minecraft Forge"
        url = "https://maven.minecraftforge.net/"
        content {
            includeGroup "com.github.glitchfiend"
        }
    }
}

configurations {
    runtimeClasspath.extendsFrom localRuntime
}

dependencies {
    implementation "com.github.glitchfiend:TerraBlender-neoforge:${terrablender_version}"
    compileOnly files(upstreamHostJar)
    localRuntime files(upstreamHostJar)
}

tasks.named('jar', Jar) {
    archiveFileName = "forbidden-magic-${mod_version}.jar"
}

sourceSets.main.resources { srcDir('src/generated/resources') }
sourceSets.main.java { srcDir('tests/java') }

def generateModMetadata = tasks.register("generateModMetadata", ProcessResources) {
    var replaceProperties = [
        minecraft_version: minecraft_version, minecraft_version_range: minecraft_version_range,
        neo_version: neo_version, mod_id: mod_id, mod_name: mod_name, mod_version: mod_version,
        mod_authors: mod_authors, mod_credits: mod_credits, mod_description: mod_description,
        thaumcraft_version: thaumcraft_version
    ]
    inputs.properties replaceProperties
    expand replaceProperties
    from "src/main/templates"
    into "build/generated/sources/modMetadata"
}
sourceSets.main.resources.srcDir generateModMetadata
neoForge.ideSyncTask generateModMetadata

tasks.withType(JavaCompile).configureEach { options.encoding = 'UTF-8' }
tasks.register('verify', GradleBuild) { }
```

`.gitattributes`:

```
* text=auto eol=lf
*.png binary
*.jar binary
```

`src/main/templates/META-INF/neoforge.mods.toml`:

```toml
modLoader="javafml"
loaderVersion="[1,)"
license="MIT"

[[mods]]
modId="${mod_id}"
version="${mod_version}"
displayName="${mod_name}"
authors="${mod_authors}"
credits="${mod_credits}"
description='''
${mod_description}
'''

[[dependencies.${mod_id}]]
    modId="neoforge"
    type="required"
    versionRange="[${neo_version},)"
    ordering="NONE"
    side="BOTH"

[[dependencies.${mod_id}]]
    modId="minecraft"
    type="required"
    versionRange="${minecraft_version_range}"
    ordering="NONE"
    side="BOTH"

[[dependencies.${mod_id}]]
    modId="thaumcraft"
    type="required"
    versionRange="[${thaumcraft_version},)"
    ordering="AFTER"
    side="BOTH"
```

`src/main/java/dev/visfox/forbiddenreforged/ForbiddenMagic.java`:

```java
package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.test.FmGameTests;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ForbiddenMagic.MODID)
public final class ForbiddenMagic {
    public static final String MODID = "forbiddenmagic";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public ForbiddenMagic(IEventBus modEventBus, ModContainer modContainer) {
        FmGameTests.register(modEventBus);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
```

`tools/check_host_clean.py`:

```python
import subprocess
import sys
from pathlib import Path

HOST = Path(__file__).resolve().parents[2] / "thaumcraft-4-reforged"


def main() -> int:
    result = subprocess.run(
        ["git", "-C", str(HOST), "status", "--porcelain", "--untracked-files=no"],
        capture_output=True,
        text=True,
        check=True,
    )
    if result.stdout.strip():
        print("host working tree is dirty:")
        print(result.stdout)
        return 1
    print("host clean")
    return 0


if __name__ == "__main__":
    sys.exit(main())
```

`tools/check_provenance.py` — our jar must carry no host code or assets, and no class outside our own namespaces. `data/thaumcraft/` is forbidden deliberately: the sibling Reconstructed port registers its caps through `data/thaumcraft/data_maps/item/wand_caps.json`, but this host has no data-map loader, so writing into the host's namespace here would be copying their mechanism without their machinery. If a future host version adds one, lift the prefix in this file and say why in the commit.

```python
import sys
import zipfile
from pathlib import Path

FORBIDDEN_PREFIXES = ("thaumcraft/", "assets/thaumcraft/", "data/thaumcraft/")


def main() -> int:
    jars = sorted(Path("build/libs").glob("forbidden-magic-*.jar"))
    if not jars:
        print("no artifact in build/libs")
        return 1
    violations = []
    for jar in jars:
        with zipfile.ZipFile(jar) as archive:
            for name in archive.namelist():
                if name.startswith(FORBIDDEN_PREFIXES):
                    violations.append(f"{jar.name}: {name}")
                elif name.endswith(".class") and not name.startswith("dev/visfox/forbiddenreforged/"):
                    violations.append(f"{jar.name}: {name}")
    for violation in violations:
        print(violation)
    if violations:
        return 1
    print(f"provenance ok: {len(jars)} jar(s)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
```

- [ ] **Step 4: Make sure the upstream host jar matches the pin, then run the gate.** `compileOnly files(upstreamHostJar)` reads `../thaumcraft-4-reforged/build/libs/thaumcraft-4-reforged-1.1-26.3.jar`, which this session built from the release commit while the clone has since moved. Rebuilding is incremental and cheap when nothing changed, so always rebuild; it writes only into the gitignored `build/` and touches no tracked file:

```bash
git -C ../thaumcraft-4-reforged log -1 --format="%h %s"
cd ../thaumcraft-4-reforged && cmd //c gradlew.bat build && cd ../forbidden-magic-reforged
cmd //c gradlew.bat build
cmd //c gradlew.bat runGameTestServer
python tools/check_host_clean.py
python tools/check_provenance.py
```

Expected: the first line prints `69a47a1 fix: restore thaumonomicon creation sparkles`, matching `host.sha`; `BUILD SUCCESSFUL`; a `GAME TESTS COMPLETE` line with no `required tests failed` (the server does not print an "All tests successful" line); `host clean`; `provenance ok: 1 jar(s)`. Do not work around a missing or stale host jar by dropping the dependency — the dependency is the point.

- [ ] **Step 4b: One probe this plan does not assume.** Record the output in `docs/deviations.md`, because Task 4 is built on it:

```bash
cmd //c gradlew.bat dependencies --configuration localRuntime | grep -i thaumcraft
```

Expected: the host jar appears on the runtime classpath — that is the evidence that ModDevGradle treats a plain jar dependency as a loadable mod, which is what the host itself relies on for JEI (`compileOnly` + `localRuntime` in its `build.gradle`). If it does not appear, the add-on compiles but never loads, and the fix is a declared mod dependency rather than the dependency form — settle it here, not in Task 4. The second half of the earlier probe (where the structure template lives) is already answered: `src/gametest/packs` holds only the `outer_lands` datapack, and `test/platform.nbt` is generated into the host's own `data/thaumcraft/structure/test/`, so the template resolves from the jar.

- [ ] **Step 5: Commit.**

```bash
git add settings.gradle gradle.properties build.gradle .gitattributes gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.properties src/main/templates/META-INF/neoforge.mods.toml src/main/java/dev/visfox/forbiddenreforged/ForbiddenMagic.java tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java tools/check_host_clean.py tools/check_provenance.py
git commit -m "feat: scaffold the Forbidden Magic add-on project"
```

---

### Task 2: Pinned host copy and the patch pipeline

**Files:**
- Create: `host.sha`, `tools/prepare_host.py`
- Modify: `.gitignore`

**Interfaces:**
- Consumes: the pristine host clone at `../thaumcraft-4-reforged` on sha `69a47a1`.
- Produces: `host/` — a patched, buildable copy of the host; `tools/prepare_host.py --verify` exits non-zero when patches do not apply; `build/libs` jar of the host used by Task 4.

- [ ] **Step 1: Write the failing check.** `python tools/prepare_host.py --verify` must exit non-zero now, because `host/` does not exist. Run it and confirm the failure.

- [ ] **Step 2: Write the tool.**

```python
import argparse
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
HOST = ROOT / "host"
UPSTREAM = ROOT.parent / "thaumcraft-4-reforged"
PATCHES = ROOT / "patches"


def sha() -> str:
    return (ROOT / "host.sha").read_text(encoding="utf-8").strip()


def run(*args: str, cwd: Path | None = None) -> None:
    result = subprocess.run(list(args), cwd=cwd if cwd else ROOT, text=True)
    if result.returncode != 0:
        raise SystemExit(f"command failed: {' '.join(args)}")


def clone() -> None:
    if not HOST.exists():
        subprocess.run(
            ["git", "clone", "--no-hardlinks", str(UPSTREAM), str(HOST)],
            check=True,
            text=True,
        )
    run("git", "-C", str(HOST), "config", "core.autocrlf", "false")
    run("git", "-C", str(HOST), "fetch", "--all")
    run("git", "-C", str(HOST), "checkout", "--force", "--detach", sha())
    run("git", "-C", str(HOST), "read-tree", "HEAD")
    run("git", "-C", str(HOST), "checkout-index", "-a", "-f")
    run("git", "-C", str(HOST), "clean", "-f", "-d")


def apply_patches() -> None:
    for patch in sorted(PATCHES.glob("*.patch")):
        check = subprocess.run(
            ["git", "-C", str(HOST), "apply", "--check", "--ignore-space-change", str(patch)],
            text=True,
        )
        if check.returncode != 0:
            raise SystemExit(f"patch does not apply cleanly: {patch.name}")
        run("git", "-C", str(HOST), "am", "--3way", "--ignore-space-change", "--no-gpg-sign", str(patch))


def verify() -> None:
    if not HOST.exists():
        raise SystemExit("host/ missing: run without --verify first")
    expected = sha()
    ancestor = subprocess.run(
        ["git", "-C", str(HOST), "merge-base", "--is-ancestor", expected, "HEAD"],
        text=True,
    )
    if ancestor.returncode != 0:
        raise SystemExit(f"host/ does not descend from pinned sha {expected}")
    count = int(
        subprocess.run(
            ["git", "-C", str(HOST), "rev-list", "--count", f"{expected}..HEAD"],
            capture_output=True,
            text=True,
            check=True,
        ).stdout.strip()
    )
    patch_count = len(list(PATCHES.glob("*.patch")))
    if count != patch_count:
        raise SystemExit(f"host/ is {count} commits ahead, patches/ holds {patch_count}")
    dirty = subprocess.run(
        ["git", "-C", str(HOST), "status", "--porcelain", "--untracked-files=no"],
        capture_output=True,
        text=True,
    ).stdout
    if dirty.strip():
        raise SystemExit("host/ has unpatched edits, they must live in patches/ instead:\n" + dirty)
    print(f"host matches sha + {patch_count} patch commit(s)")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--verify", action="store_true")
    parser.add_argument("--rebuild", action="store_true")
    arguments = parser.parse_args()
    if arguments.verify:
        verify()
        return 0
    clone()
    apply_patches()
    if arguments.rebuild:
        run("cmd", "//c", "gradlew.bat", "build", cwd=HOST)
    return 0


if __name__ == "__main__":
    sys.exit(main())
```

`read-tree HEAD` + `checkout-index -a -f` — не украшение. На этой машине системный `core.autocrlf=true`, и после `checkout --force` stat-кэш индекса считает файлы неизменными, поэтому рабочее дерево остаётся в CRLF при LF в индексе, а `git status` при этом чист — то есть правки в `host/` молча шли бы с CRLF. `read-tree` сбрасывает stat-кэш, `checkout-index` перезаписывает все файлы при локальном `core.autocrlf=false`, и дерево становится LF. Проверено 2026-10-21: без этих двух строк `build.gradle` в `host/` оставался CRLF, с ними `git ls-files --eol` даёт `i/lf w/lf` и `git status` пуст.

- [ ] **Step 3: Create `host.sha` with the single line `69a47a1b238e3ac15fa5d4b10e61ccb6a4a80dbf`, add `host/` and `maven/` to `.gitignore`, and run:**

```bash
python tools/prepare_host.py
python tools/prepare_host.py --verify
git -C ../thaumcraft-4-reforged status --porcelain --untracked-files=no
```

Expected: clone succeeds, `host matches sha + 0 patch commit(s)` (no patches yet, so an empty pass is valid), host tree empty.

- [ ] **Step 4: Prove the patch pipeline fails loudly.** Introduce `patches/probe.patch` by hand (one dummy context line changed in a host file), run `python tools/prepare_host.py` and confirm exit non-zero with `patch does not apply cleanly: probe.patch`. Delete the probe patch afterwards. A pipeline that cannot fail is not a gate.

- [ ] **Step 5: Re-run the gate against the copy and commit.** Task 1's game test passed against the *upstream* jar; run it again now that `host/` exists, because from here on the copy is what every later task patches and builds:

```bash
cmd //c gradlew.bat runGameTestServer
git add host.sha tools/prepare_host.py .gitignore
git commit -m "feat: pin the host copy and add the patch pipeline"
```

Expected: `BUILD SUCCESSFUL` with no `required tests failed`. This run still uses the upstream jar, not `host/`; the copy's pristineness is proven by `--verify` (commit count and clean tree), not by this test. The run is a regression check, not a copy check.

---

### Task 3: Patch 0001 — `Aspect` becomes a registry-backed class

**Files:**
- Modify: `host/src/main/java/thaumcraft/aspect/Aspect.java` (full replacement below)
- Modify: `host/src/main/java/thaumcraft/entity/AspectOrb.java:41,54,60,132`
- Modify: `host/src/main/java/thaumcraft/wand/WandVis.java:31-53` (two `switch (aspect)` statements)
- Create: `patches/0001-aspect-registry.patch` via `git -C host format-patch`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces for later tasks: `Aspect.register(String tag, int color, Aspect @Nullable [] components, int blend, @Nullable Identifier texture) → Aspect`, `Aspect.values() → Aspect[]`, an **instance** `aspect.ordinal() → int` (same call syntax as the enum), `Aspect.byOrdinal(int)`, `Aspect.builtinCount()`, `Aspect.AIR … Aspect.TRAP` unchanged in name and order, and the 48 built-ins always at indices 0-47.
- Consumes: nothing from Task 2 beyond the patched copy.

- [ ] **Step 1: Write the failing test** — append to `TESTS` and add the body:

```java
    private static final String[] TESTS = {"mod_loads_and_host_is_present", "aspect_registry_keeps_builtin_order_and_combines"};

    private static void aspectRegistryKeepsBuiltinOrderAndCombines(GameTestHelper helper) {
        Aspect[] all = Aspect.values();
        helper.assertValueEqual(all.length, 48, "no add-on aspect registered yet");
        helper.assertValueEqual(all[0], Aspect.AIR, "first builtin must stay index 0");
        helper.assertValueEqual(all[47], Aspect.TRAP, "last builtin must stay index 47");
        Aspect fire = Aspect.combine(Aspect.AIR, Aspect.FIRE);
        helper.assertTrue(fire == Aspect.LIGHT, "combine(AIR, FIRE) must be LIGHT");
        helper.assertTrue(Aspect.getAspect("aer") == Aspect.AIR, "by-tag lookup must work");
        helper.succeed();
    }
```

Run `cmd //c gradlew.bat runGameTestServer` and confirm it **passes**. The test deliberately uses only the surface common to the enum and the class (`values`, `combine`, `getAspect`), so it compiles and passes against the upstream enum too — it is a regression check, not proof of the patch. The proof of patch 0001 is Step 4 (the host's own gates inside the copy) and Task 4, where the add-on is compiled against the patched artifact and starts using `Aspect.builtinCount()` and `Aspect.register`.

- [ ] **Step 2: Replace `Aspect.java` in the host copy.** Full content, keeping every existing public member and adding the registry:

```java
package thaumcraft.aspect;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;
import thaumcraft.Thaumcraft;

public final class Aspect implements StringRepresentable {
    private static final Map<String, Aspect> BY_TAG = new LinkedHashMap<>();
    public static final Aspect AIR = primal("aer", 0xFFFF7E, "e", 1);
    public static final Aspect EARTH = primal("terra", 0x56C000, "2", 1);
    public static final Aspect FIRE = primal("ignis", 0xFF5A01, "c", 1);
    public static final Aspect WATER = primal("aqua", 0x3CD4FC, "3", 1);
    public static final Aspect ORDER = primal("ordo", 0xD5D4EC, "7", 1);
    public static final Aspect ENTROPY = primal("perditio", 0x404040, "8", 771);
    public static final Aspect VOID = compound("vacuos", 0x888888, AIR, ENTROPY, 771);
    public static final Aspect LIGHT = compound("lux", 0xFFF663, AIR, FIRE, 1);
    public static final Aspect WEATHER = compound("tempestas", 0xFFFFFF, AIR, WATER, 1);
    public static final Aspect MOTION = compound("motus", 0xCDCCF4, AIR, ORDER, 1);
    public static final Aspect COLD = compound("gelum", 0xE1FFFF, FIRE, ENTROPY, 1);
    public static final Aspect CRYSTAL = compound("vitreus", 0x80FFFF, EARTH, ORDER, 1);
    public static final Aspect LIFE = compound("victus", 0xDE0005, WATER, EARTH, 1);
    public static final Aspect POISON = compound("venenum", 0x89F000, WATER, ENTROPY, 1);
    public static final Aspect ENERGY = compound("potentia", 0xC0FFFF, ORDER, FIRE, 1);
    public static final Aspect EXCHANGE = compound("permutatio", 0x578357, ENTROPY, ORDER, 1);
    public static final Aspect METAL = compound("metallum", 0xB5B5CD, EARTH, CRYSTAL, 1);
    public static final Aspect DEATH = compound("mortuus", 0x887788, LIFE, ENTROPY, 1);
    public static final Aspect FLIGHT = compound("volatus", 0xE7E7D7, AIR, MOTION, 1);
    public static final Aspect DARKNESS = compound("tenebrae", 0x222222, VOID, LIGHT, 1);
    public static final Aspect SOUL = compound("spiritus", 0xEBEBFB, LIFE, DEATH, 1);
    public static final Aspect HEAL = compound("sano", 0xFF2F34, LIFE, ORDER, 1);
    public static final Aspect TRAVEL = compound("iter", 0xE0585B, MOTION, EARTH, 1);
    public static final Aspect ELDRITCH = compound("alienis", 0x805080, VOID, DARKNESS, 1);
    public static final Aspect MAGIC = compound("praecantatio", 0x9700C0, VOID, ENERGY, 1);
    public static final Aspect AURA = compound("auram", 0xFFC0FF, MAGIC, AIR, 1);
    public static final Aspect TAINT = compound("vitium", 0x800080, MAGIC, ENTROPY, 1);
    public static final Aspect SLIME = compound("limus", 0x01F800, LIFE, WATER, 1);
    public static final Aspect PLANT = compound("herba", 0x01AC00, LIFE, EARTH, 1);
    public static final Aspect TREE = compound("arbor", 0x876531, AIR, PLANT, 1);
    public static final Aspect BEAST = compound("bestia", 0x9F6409, MOTION, LIFE, 1);
    public static final Aspect FLESH = compound("corpus", 0xEE478D, DEATH, BEAST, 1);
    public static final Aspect UNDEAD = compound("exanimis", 0x3A4000, MOTION, DEATH, 1);
    public static final Aspect MIND = compound("cognitio", 0xFFC2B3, FIRE, SOUL, 1);
    public static final Aspect SENSES = compound("sensus", 0x0FD9FF, AIR, SOUL, 1);
    public static final Aspect MAN = compound("humanus", 0xFFD7C0, BEAST, MIND, 1);
    public static final Aspect CROP = compound("messis", 0xE1B371, PLANT, MAN, 1);
    public static final Aspect MINE = compound("perfodio", 0xDCD2D8, MAN, EARTH, 1);
    public static final Aspect TOOL = compound("instrumentum", 0x4040EE, MAN, ORDER, 1);
    public static final Aspect HARVEST = compound("meto", 0xEEAD82, CROP, TOOL, 1);
    public static final Aspect WEAPON = compound("telum", 0xC05050, TOOL, FIRE, 1);
    public static final Aspect ARMOR = compound("tutamen", 0x00C0C0, TOOL, EARTH, 1);
    public static final Aspect HUNGER = compound("fames", 0x9A0305, LIFE, VOID, 1);
    public static final Aspect GREED = compound("lucrum", 0xE6BE44, MAN, HUNGER, 1);
    public static final Aspect CRAFT = compound("fabrico", 0x809D80, MAN, TOOL, 1);
    public static final Aspect CLOTH = compound("pannus", 0xEAEAC2, TOOL, BEAST, 1);
    public static final Aspect MECHANISM = compound("machina", 0x8080A0, MOTION, TOOL, 1);
    public static final Aspect TRAP = compound("vinculum", 0x9A8080, MOTION, ENTROPY, 1);

    public static final Codec<Aspect> CODEC = Codec.STRING.xmap(Aspect::byTagOrThrow, Aspect::getTag);
    public static final StreamCodec<ByteBuf, Aspect> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Aspect::byTagOrThrow, Aspect::getTag);

    private static final int BUILTIN_COUNT = BY_TAG.size();

    private final String tag;
    private final int color;
    private final @Nullable String chatcolor;
    private final Aspect @Nullable [] components;
    private final int blend;
    private final Identifier image;

    private Aspect(String tag, int color, @Nullable String chatcolor, Aspect @Nullable [] components, int blend, @Nullable Identifier image) {
        this.tag = tag;
        this.color = color;
        this.chatcolor = chatcolor;
        this.components = components;
        this.blend = blend;
        this.image = image == null ? Thaumcraft.id("textures/aspects/" + tag + ".png") : image;
    }

    private static Aspect primal(String tag, int color, @Nullable String chatcolor, int blend) {
        Aspect aspect = new Aspect(tag, color, chatcolor, null, blend, null);
        BY_TAG.put(tag, aspect);
        return aspect;
    }

    private static Aspect compound(String tag, int color, Aspect first, Aspect second, int blend) {
        Aspect aspect = new Aspect(tag, color, null, new Aspect[]{first, second}, blend, null);
        BY_TAG.put(tag, aspect);
        return aspect;
    }

    public static Aspect register(String tag, int color, Aspect @Nullable [] components, int blend, @Nullable Identifier image) {
        Aspect existing = BY_TAG.get(tag);
        if (existing != null) {
            return existing;
        }
        Aspect aspect = new Aspect(tag, color, null, components, blend, image);
        BY_TAG.put(tag, aspect);
        return aspect;
    }

    public static Aspect[] values() {
        return BY_TAG.values().toArray(new Aspect[0]);
    }

    public static int count() {
        return BY_TAG.size();
    }

    public static int builtinCount() {
        return BUILTIN_COUNT;
    }

    public int ordinal() {
        Integer index = null;
        int position = 0;
        for (String key : BY_TAG.keySet()) {
            if (key.equals(tag)) {
                index = position;
                break;
            }
            position++;
        }
        return index == null ? -1 : index;
    }

    public static @Nullable Aspect byOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= BY_TAG.size()) {
            return null;
        }
        return values()[ordinal];
    }

    public static Aspect byTagOrThrow(String tag) {
        Aspect aspect = BY_TAG.get(tag);
        if (aspect == null) {
            throw new IllegalArgumentException("Unknown aspect " + tag);
        }
        return aspect;
    }

    public String getTag() {
        return tag;
    }

    public int getColor() {
        return color;
    }

    public @Nullable String getChatcolor() {
        return chatcolor;
    }

    public Aspect @Nullable [] getComponents() {
        return components;
    }

    public int getBlend() {
        return blend;
    }

    public Identifier getImage() {
        return image;
    }

    public boolean isPrimal() {
        return components == null || components.length != 2;
    }

    public String getName() {
        return Character.toUpperCase(tag.charAt(0)) + tag.substring(1);
    }

    public Component getDescription() {
        return Component.translatable("tc.aspect." + tag);
    }

    @Override
    public String getSerializedName() {
        return tag;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Aspect aspect && aspect.tag.equals(tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public String toString() {
        return tag;
    }

    public static @Nullable Aspect getAspect(String tag) {
        return BY_TAG.get(tag);
    }

    public static List<Aspect> getPrimalAspects() {
        List<Aspect> primals = new ArrayList<>();
        for (Aspect aspect : values()) {
            if (aspect.isPrimal()) {
                primals.add(aspect);
            }
        }
        return primals;
    }

    public static List<Aspect> getCompoundAspects() {
        List<Aspect> compounds = new ArrayList<>();
        for (Aspect aspect : values()) {
            if (!aspect.isPrimal()) {
                compounds.add(aspect);
            }
        }
        return compounds;
    }

    public static @Nullable Aspect combine(Aspect first, Aspect second) {
        for (Aspect aspect : values()) {
            if (aspect.components != null
                && (aspect.components[0] == first && aspect.components[1] == second
                    || aspect.components[0] == second && aspect.components[1] == first)) {
                return aspect;
            }
        }
        return null;
    }
}
```

`BUILTIN_COUNT` is assigned after the 48 static fields are initialised, because Java runs static initialisers strictly top to bottom; anything registered later lands at index 48 and above. **This ordering is load-bearing and is the first thing to break under "cleanup":** `BY_TAG` must be declared *before* the constants, because `primal()` and `compound()` write into it while the constants are being initialised — move the map down and the class throws `NullPointerException` in `<clinit>` the moment anything touches an aspect, which is at mod construction, not at first use.

- [ ] **Step 3: Rework the four index-dependent lines in `AspectOrb.java`** so index stability is guaranteed and an out-of-range or unknown index degrades to AIR instead of throwing: `:41` and `:132` write `aspect.ordinal()` (the method now exists on the class, so they stay as written), `:54` defines the synched-data default `Aspect.AIR.ordinal()`, and `:60` is the read path, which must stop indexing `values()` by hand:

```java
    public Aspect getAspect() {
        Aspect looked = Aspect.byOrdinal(entityData.get(DATA_ASPECT));
        return looked == null ? Aspect.AIR : looked;
    }
```

- [ ] **Step 3b: Rework the two `switch (aspect)` statements in `WandVis.java`.** A class cannot be a switch selector over unqualified enum labels, so both methods become identity chains with the same behaviour (`default` returned `0` and `this`):

```java
    public int get(Aspect aspect) {
        if (aspect == Aspect.AIR) {
            return air;
        }
        if (aspect == Aspect.EARTH) {
            return earth;
        }
        if (aspect == Aspect.FIRE) {
            return fire;
        }
        if (aspect == Aspect.WATER) {
            return water;
        }
        if (aspect == Aspect.ORDER) {
            return order;
        }
        if (aspect == Aspect.ENTROPY) {
            return entropy;
        }
        return 0;
    }

    public WandVis with(Aspect aspect, int amount) {
        if (aspect == Aspect.AIR) {
            return new WandVis(amount, earth, fire, water, order, entropy);
        }
        if (aspect == Aspect.EARTH) {
            return new WandVis(air, amount, fire, water, order, entropy);
        }
        if (aspect == Aspect.FIRE) {
            return new WandVis(air, earth, amount, water, order, entropy);
        }
        if (aspect == Aspect.WATER) {
            return new WandVis(air, earth, fire, amount, order, entropy);
        }
        if (aspect == Aspect.ORDER) {
            return new WandVis(air, earth, fire, water, amount, entropy);
        }
        if (aspect == Aspect.ENTROPY) {
            return new WandVis(air, earth, fire, water, order, amount);
        }
        return this;
    }
```

- [ ] **Step 4: Run the host's own gates inside the copy**, to prove the refactor is not destructive. Do **not** call `prepare_host.py --rebuild` here: it resets `host/` to the pin and applies `patches/`, which at this point is empty, so it would silently discard the edits. Build the edited copy directly:

```bash
cd host && cmd //c gradlew.bat build && cd ..
cd host && cmd //c gradlew.bat runGameTestServer && cd ..
python host/tools/check_seams.py --root host
```

Expected: host build green, host game tests green, `seams ok: N files checked`. If a host game test fails, the refactor is wrong — fix it here, not in the add-on.

- [ ] **Step 5: Export the patch and re-verify the pipeline.**

```bash
git -C host add -A && git -C host commit -m "aspect registry" && git -C host format-patch -1 --stdout > patches/0001-aspect-registry.patch
git -C host reset --hard 69a47a1b238e3ac15fa5d4b10e61ccb6a4a80dbf
python tools/prepare_host.py && python tools/prepare_host.py --verify
```

Expected: patch file created, re-applied cleanly from a pristine checkout, `host matches sha + patch series`.

- [ ] **Step 6: Run the add-on test and see it pass.**

```bash
cmd //c gradlew.bat runGameTestServer
```

Expected: `aspect_registry_keeps_builtin_order_and_combines` passes.

- [ ] **Step 7: Commit.**

```bash
git add patches/0001-aspect-registry.patch tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: make host aspects a registry"
```

---

### Task 4: Patches 0002 and 0003 — add-on hook, maven publication, working game run

**Files:**
- Create: `patches/0002-aspect-addon-init.patch` (host: `aspect/AddonAspects.java` + one call in `AspectSync.rebuild`)
- Create: `patches/0003-host-publish.patch` (host copy `build.gradle` + `gradle.properties` version)
- Create: `maven/` (generated, gitignored)
- Test: `FmGameTests` gains `addon_aspect_survives_client_sync`

**Interfaces:**
- Produces: `thaumcraft:thaumcraft:1.2` in `maven/`, artifact name `thaumcraft-4-reforged-1.2-<shortsha>.jar`; host entry point `thaumcraft.aspect.AspectSync.addRegistrar` unchanged; `Aspect.register` callable from a mod constructor.
- Consumes: `compileOnly "thaumcraft:thaumcraft:1.2"` in our `build.gradle` (Task 1).

- [ ] **Step 1: Write the failing test.** Registration must be visible to the aspect rebuild, so the probe aspect is registered from the mod constructor. Create `src/main/java/dev/visfox/forbiddenreforged/FmAspects.java` now with only the probe (Task 5 replaces its body with the seven sins):

```java
package dev.visfox.forbiddenreforged;

import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.AddonAspects;
import thaumcraft.aspect.Aspect;

public final class FmAspects {
    public static @Nullable Aspect PROBE;

    private FmAspects() {
    }

    public static boolean available() {
        return AddonAspectsAvailable.check();
    }

    public static void init() {
        AddonAspects.addRegistrar(api -> PROBE = api.register("probe", 0x808080, new Aspect[]{Aspect.AIR, Aspect.VOID}, 1, null));
        AddonAspects.init();
    }

    private static final class AddonAspectsAvailable {
        static boolean check() {
            try {
                Class.forName("thaumcraft.aspect.AddonAspects");
                return true;
            } catch (ClassNotFoundException absent) {
                return false;
            }
        }
    }
}
```

The probe is compound on purpose: an add-on aspect with `null` components is primal, joins `Aspect.getPrimalAspects()`, and the host uses that list as the wand's six vis slots. Verified 2026-10-21: a primal probe made 14 host infusion/wand tests fail (`wand click was not handled`, vis deltas wrong); a compound probe leaves them green. All seven sin aspects are compound, so this is also the shape the real content uses.

and call `FmAspects.init()` as the first statement of the mod constructor. The Task 3 test asserted `values().length == 48`; with the probe registered that count becomes 49, so in the same edit change it to assert `Aspect.builtinCount() == 48` and keep the index checks on 0 and 47. The test:

```java
    private static void addonAspectSurvivesClientSync(GameTestHelper helper) {
        Aspect probe = FmAspects.PROBE;
        helper.assertTrue(probe != null && probe.ordinal() >= Aspect.builtinCount(), "probe aspect must be an add-on aspect");
        helper.assertValueEqual(Aspect.getAspect("probe"), probe, "by-tag lookup must find the add-on aspect");
        AspectList list = new AspectList().add(probe, 3);
        io.netty.buffer.ByteBuf buffer = io.netty.buffer.Unpooled.buffer();
        AspectList.STREAM_CODEC.encode(buffer, list);
        buffer.readerIndex(0);
        AspectList decoded = AspectList.STREAM_CODEC.decode(buffer);
        helper.assertValueEqual(decoded.getAmount(probe), 3, "aspect amount must survive a network round trip");
        helper.succeed();
    }
```

`Aspect.register` stores the tag without a namespace, so `getAspect` is called with the plain `probe` tag: use `Aspect.getAspect("probe")` if the by-tag map keys on the bare tag, which Step 2 will show — the assertion must match the implementation, not the guess.

Run `cmd //c gradlew.bat runGameTestServer`: expected red (the host copy has no `AddonAspects` yet, so `FmAspects` does not compile).

- [ ] **Step 2: Write patch 0002** in the host copy. `aspect/AddonAspects.java`:

```java
package thaumcraft.aspect;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public final class AddonAspects {
    private static final List<Consumer<Addon>> REGISTRARS = new CopyOnWriteArrayList<>();
    private static final Addon API = (tag, color, components, blend, texture) -> Aspect.register(tag, color, components, blend, texture);
    private static boolean initialised;

    private AddonAspects() {
    }

    public interface Addon {
        Aspect register(String tag, int color, @Nullable Aspect[] components, int blend, @Nullable net.minecraft.resources.Identifier texture);
    }

    public static void addRegistrar(Consumer<Addon> registrar) {
        REGISTRARS.add(registrar);
        if (initialised) {
            registrar.accept(API);
        }
    }

    public static void init() {
        if (initialised) {
            return;
        }
        initialised = true;
        REGISTRARS.forEach(registrar -> registrar.accept(API));
    }
}
```

and in `AspectSync.rebuild`, before `ModernAspects modern = new ModernAspects(registry);` insert `AddonAspects.init();`.

`FmAspects.init()` calls `AddonAspects.init()` itself, and that call is load-bearing for clients: `AspectSync.rebuild` runs on the server only, so in multiplayer a client would otherwise never create the add-on aspects and would fail to decode them from packets. The constructor call registers them on both sides at mod construction, before any packet can arrive. The call in `AspectSync.rebuild` stays as the server-side ordering guarantee; `initialised` makes it idempotent, and `addRegistrar` invokes a registrar immediately if it arrives after initialization, so a second add-on loaded later is not silently dropped.

- [ ] **Step 3: Write patch 0003**: add to the host copy `build.gradle` a `maven-publish` block and set `mod_version=1.2` in its `gradle.properties`:

```groovy
plugins {
    id 'maven-publish'
}

publishing {
    repositories {
        maven {
            name = "portLocal"
            url = uri("../maven")
        }
    }
    publications {
        thaumcraft(MavenPublication) {
            groupId = "thaumcraft"
            artifactId = "thaumcraft"
            version = project.mod_version
            artifacts tasks.named('jar')
        }
    }
}
```

- [ ] **Step 4: Export both patches the same way as Task 3 Step 5, publish, and switch the add-on onto the artifact.** Set `thaumcraft_version=1.2` in `gradle.properties` (the patched host now reports `1.2`; leaving `1.1` would let the add-on load on a stock host and silently reach `NoSuchMethodError`). Replace the two `files(upstreamHostJar)` lines in `build.gradle` and drop the now-unused `upstreamHostJar` definition:

```groovy
dependencies {
    implementation "com.github.glitchfiend:TerraBlender-neoforge:${terrablender_version}"
    compileOnly "thaumcraft:thaumcraft:${thaumcraft_version}"
    localRuntime "thaumcraft:thaumcraft:${thaumcraft_version}"
}
```

Then:

```bash
python tools/prepare_host.py
cd host && cmd //c gradlew.bat publish && cd ..
cmd //c gradlew.bat build
cmd //c gradlew.bat runGameTestServer
python tools/check_host_clean.py
python tools/check_provenance.py
```

Expected: `maven/thaumcraft/thaumcraft/1.2/` contains the jar, the add-on resolves it from `maven/`, `mod_loads_and_host_is_present` and `aspect_registry_keeps_builtin_order_and_combines` still pass (the dependency form changed, the code did not), host clean, provenance ok.

- [ ] **Step 5: Manual smoke check, since this is the first real launch.**

```bash
cmd //c gradlew.bat runClient
```

Expected: game starts, mod list shows `Forbidden Magic (Reforged Port)` with Thaumcraft loaded, no crash. Report what you actually see; do not claim it works without reading the log.

- [ ] **Step 6: Commit.**

```bash
git add patches/0002-aspect-addon-init.patch patches/0003-host-publish.patch
git commit -m "feat: expose an add-on aspect hook and publish the host locally"
```

### Task 4b: Coordination checkpoint with the host author

**Files:**
- Create: `docs/upstream/issue-draft.md`
- Modify: `docs/roadmap.md`

**Interfaces:**
- Consumes: `patches/0001`-`0003` and the green gates from Tasks 3-4.
- Produces: a decision — continue Tasks 5-11 as an add-on, or hand the patch series over and stop. Nothing else in this plan depends on the answer, and no task after this one starts before the answer exists.

Why this task exists: on 2026-10-20 the host author said in chat that he intends to port Forbidden Magic himself when he gets back to it («если вайбик найдется после перерыва то forbidden magic портану»). As of that day the host repo has no add-on content in it (one commit on `main`, no second addon, no FM issue or branch), so nothing is in flight — but the plan should not spend nine more tasks assuming it is the only attempt.

- [ ] **Step 1: Write the draft.** Three paragraphs, in this order: what we measured (`Aspect` is an `enum` of 48, so an add-on cannot register an aspect; the change is two files plus the `AspectOrb` index read and one registration point, and every one of the 1827 existing references survives), what we built (the patch series, with `build` and `runGameTestServer` output attached, plus the aspect registry game test), and the question: do you want the aspect API as a host feature, are you taking Forbidden Magic yourself, or should this stay a third-party add-on. Mention that the work is AI-assisted. Offer the seven sin aspects as the concrete use case, and note that a sibling TC4 port already ships data-driven add-on aspects with 32 add-ons on top.

- [ ] **Step 2: Hand it to the user.** This project does not send it. No fork, no issue, no comment, no PR from `MxxFoxx2` without an explicit instruction.

- [ ] **Step 3: Record the answer and branch on it in `docs/roadmap.md`:**

```
decision: <continue | hand over | wait>
date: <YYYY-MM-DD>
author reply: <quote or "no reply">
```

`continue` — Tasks 5-11 run as written. `hand over` — stop after committing the patches; the aspect layer and the drop rules stay useful as the proof in the pull request, and the content work never starts. `wait` — keep going on Tasks 5-6 only, because the aspects and the tag table are what any add-on needs regardless of who writes the items.

---

### Task 5: The seven aspects, with the availability check

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmConfig.java` (the `no_lust` switch only — the aspect guard reads it, so it must exist before `FmAspects.init()`)
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmAspects.java`
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmSetup.java` (runs the aspect registration after config load)
- Create: `src/main/resources/assets/forbiddenmagic/textures/aspects/{luxuria,infernus,superbia,gula,invidia,desidia,ira}.png` — copied from `../ForbiddenMagic/src/main/resources/assets/forbidden/textures/aspects/`
- Create: `tools/lang/en_us.toml`, `tools/lang/ru_ru.toml` (aspect names from `../ForbiddenMagic/src/main/resources/assets/forbidden/lang/en_US.lang`, keys `forbiddenmagic.aspect.<tag>`)
- Modify: `ForbiddenMagic.java` (register `FmAspects` through `AddonAspects.addRegistrar`)
- Test: `FmGameTests` — `sin_aspects_registered_with_own_textures`

**Interfaces:**
- Produces: `FmAspects.LUST/NETHER/PRIDE/GLUTTONY/ENVY/SLOTH/WRATH` (names as in the original) and `FmAspects.available() → boolean`. `LUST` is `null` when `no_lust` is on, exactly as the original leaves it unset, so every later use is a null check rather than an assumption.
- Consumes: `AddonAspects.addRegistrar`, `Aspect.register`, host patched version check.

- [ ] **Step 1: Write the failing test.**

```java
    private static void sinAspectsRegisteredWithOwnTextures(GameTestHelper helper) {
        helper.assertTrue(FmAspects.available(), "add-on aspect API missing: host too old");
        Aspect[] sins = {FmAspects.NETHER, FmAspects.WRATH, FmAspects.ENVY, FmAspects.GLUTTONY, FmAspects.PRIDE, FmAspects.SLOTH};
        for (Aspect aspect : sins) {
            helper.assertTrue(aspect.ordinal() >= Aspect.builtinCount(), aspect.getSerializedName() + " must be an add-on aspect");
            helper.assertTrue(Identifier.isValidNamespace(aspect.getImage().getNamespace()) && aspect.getImage().getNamespace().equals("forbiddenmagic"), aspect.getSerializedName() + " texture must live in the add-on namespace");
        }
        Aspect ira = Aspect.combine(Aspect.WEAPON, Aspect.FIRE);
        helper.assertTrue(ira == FmAspects.WRATH, "combine(WEAPON, FIRE) must resolve to the add-on ira");
        helper.assertTrue(Aspect.combine(Aspect.AIR, Aspect.FIRE) == Aspect.LIGHT, "adding ira must not disturb builtin combine");
        int expected = FmConfig.lustDisabled() ? 6 : 7;
        helper.assertValueEqual(Aspect.count(), Aspect.builtinCount() + expected, FmConfig.lustDisabled() ? "six add-on aspects: luxuria is switched off" : "exactly seven add-on aspects");
        helper.succeed();
    }
```

- [ ] **Step 2: Run and confirm red** (it will not compile: `Aspect.register`, the class form and `FmConfig` do not exist yet), then write `FmConfig.java` — the `no_lust` switch alone, with the original default and tooltip, so the guard below can read it:

```java
package dev.visfox.forbiddenreforged;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FmConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static ModConfigSpec.BooleanValue noLust;

    static {
        noLust = BUILDER.comment("Enable to remove Luxuria aspect and related items.").define("general.no_lust", false);
        SPEC = BUILDER.build();
    }

    private FmConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, SPEC);
    }

    public static boolean lustDisabled() {
        return SPEC.isLoaded() ? noLust.get() : noLust.getDefault();
    }
}
```

Then `FmAspects.java` with the availability check that produces a legible refusal instead of `NoSuchMethodError`:

```java
package dev.visfox.forbiddenreforged;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.AddonAspects;
import thaumcraft.aspect.Aspect;

public final class FmAspects {
    public static @Nullable Aspect LUST;
    public static @Nullable Aspect NETHER;
    public static @Nullable Aspect PRIDE;
    public static @Nullable Aspect GLUTTONY;
    public static @Nullable Aspect ENVY;
    public static @Nullable Aspect SLOTH;
    public static @Nullable Aspect WRATH;

    private FmAspects() {
    }

    public static boolean available() {
        return NETHER != null;
    }

    public static void init() {
        try {
            AddonAspects.addRegistrar(api -> {
                if (!FmConfig.lustDisabled()) {
                    LUST = api.register("luxuria", 0xFFC1CE, new Aspect[]{Aspect.FLESH, Aspect.HUNGER}, 1, texture("luxuria"));
                }
                NETHER = api.register("infernus", 0xFF0000, new Aspect[]{Aspect.FIRE, Aspect.MAGIC}, 771, texture("infernus"));
                PRIDE = api.register("superbia", 0x9639FF, new Aspect[]{Aspect.FLIGHT, Aspect.VOID}, 1, texture("superbia"));
                GLUTTONY = api.register("gula", 0xD59C46, new Aspect[]{Aspect.HUNGER, Aspect.VOID}, 1, texture("gula"));
                ENVY = api.register("invidia", 0x00BA00, new Aspect[]{Aspect.SENSES, Aspect.HUNGER}, 1, texture("invidia"));
                SLOTH = api.register("desidia", 0x6E6E6E, new Aspect[]{Aspect.TRAP, Aspect.SOUL}, 771, texture("desidia"));
                WRATH = api.register("ira", 0x870404, new Aspect[]{Aspect.WEAPON, Aspect.FIRE}, 771, texture("ira"));
            });
        } catch (NoSuchMethodError | NoClassDefFoundError absent) {
            throw new IllegalStateException("Forbidden Magic requires Thaumcraft 4: Reforged 1.2 or newer with the add-on aspect API", absent);
        }
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "textures/aspects/" + name + ".png");
    }
}
```

Colours and blends are taken verbatim from `ForbiddenMagic/src/main/java/fox/spiteful/forbidden/DarkAspects.java:26-38`. In `ForbiddenMagic.java` the constructor registers the STARTUP config and the registrar, but it does **not** invoke the registrar: the config is not loaded yet, and the host's `SPEC.isLoaded() ? value.get() : value.getDefault()` fallback would silently use the default instead of the user's `no_lust`. The invocation moves to `FMLCommonSetupEvent`, which fires after config load on both client and server (the host subscribes the same event in `AlchemyTriggers`):

```java
    public ForbiddenMagic(IEventBus modEventBus, ModContainer modContainer) {
        FmConfig.register(modContainer);
        FmAspects.init();
        FmGameTests.register(modEventBus);
    }
```

`FmSetup.java`:

```java
package dev.visfox.forbiddenreforged;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import thaumcraft.aspect.AddonAspects;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmSetup {
    private FmSetup() {
    }

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(AddonAspects::init);
    }
}
```

`AspectSync.rebuild` still calls `AddonAspects.init()`; it is idempotent and covers the server-side ordering. The client gets its registration from `FmSetup` before it can connect. Task 4's probe used the constructor shortcut because it had no config; this task replaces that shortcut with the common-setup path, so Task 4's commit must drop the `AddonAspects.init()` call along with the probe.

The `luxuria` guard is not decoration: the original registers that aspect inside `if (!Config.noLust)` (`DarkAspects.java:28`). Note the dependency this creates: the count assertion above reads the flag (`builtin + 7` normally, `builtin + 6` with `no_lust` on), so it holds in both config states, and the kill-switch test in Task 7 additionally asserts that the aspect itself is gone — `FmAspects.LUST` is null and `Aspect.getAspect("luxuria")` is absent — rather than only counting.

Retire the probe in the same commit: delete the `PROBE` field and its registration line, and re-point Task 4's `addon_aspect_survives_client_sync` at `FmAspects.NETHER` (keep the encode/decode round trip, change only the aspect). A released jar must not register a test aspect — the count assertion above (`builtin + 7`) is what enforces that.

- [ ] **Step 3: Copy the seven textures and add the lang entries**, then regenerate and run:

```bash
cmd //c gradlew.bat runGameTestServer
python tools/check_provenance.py
```

Expected: test green; provenance still ok, because textures live under `assets/forbiddenmagic/`.

- [ ] **Step 4: Commit.**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmConfig.java src/main/java/dev/visfox/forbiddenreforged/FmAspects.java src/main/java/dev/visfox/forbiddenreforged/ForbiddenMagic.java src/main/resources/assets/forbiddenmagic/textures/aspects tools/lang
git commit -m "feat: register the seven sin aspects"
```

---

### Task 6: Item and entity aspect tags (the `DarkAspects` table)

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmAspectTags.java`
- Test: `FmGameTests` — `sin_aspect_tags_merge_with_existing`

**Interfaces:**
- Produces: `FmAspectTags.register()` called from the mod constructor; every rule reads the same table the original reads.
- Consumes: `thaumcraft.aspect.AspectRegistrar` (`protected register/fill/complex/copy/entity`, targets as `"namespace:path"` or `"#namespace:tag"`), `AspectSync.addRegistrar`.

- [ ] **Step 1: Write the failing test.** The original *adds* `infernus` to netherrack without losing what TC already gave it, so the test pins merge semantics:

```java
    private static void sinAspectTagsMergeWithExisting(GameTestHelper helper) {
        thaumcraft.aspect.AspectRegistry active = thaumcraft.aspect.AspectRegistry.active();
        AspectList netherrack = active.getObjectTags(net.minecraft.world.item.Items.NETHERRACK);
        helper.assertTrue(netherrack != null && netherrack.getAmount(FmAspects.NETHER) >= 1, "netherrack must carry infernus");
        helper.assertTrue(netherrack.getAmount(Aspect.FIRE) >= 1, "netherrack must keep its built-in fire aspect");
        AspectList star = active.getObjectTags(net.minecraft.world.item.Items.NETHER_STAR);
        helper.assertTrue(star.getAmount(FmAspects.NETHER) >= 8 && star.getAmount(FmAspects.PRIDE) >= 8, "nether star must carry infernus 8 and superbia 8");
        helper.succeed();
    }
```

- [ ] **Step 2: Translate the table.** Open `../ForbiddenMagic/src/main/java/fox/spiteful/forbidden/DarkAspects.java:46-254` (method `addAspects`) and convert each `ThaumcraftApi.registerObjectTag(stack, list)` into one `register(target, aspects)` call inside a subclass, keeping every number. The rule for each line, applied in order:

- `new ItemStack(Blocks.x)` → target `"minecraft:x"`; `new ItemStack(Items.x)` → `"minecraft:x"`.
- `new ItemStack(Items.skull, 1, 1)` → `minecraft:wither_skeleton_skull`; meta 4 → `minecraft:creeper_head`. The other metas in the table are not used.
- `new ItemStack(Blocks.portal)` has no item form in the host: map it to `minecraft:obsidian` (the frame) and record that substitution in `docs/deviations.md`. The table contains no `ItemApi.getItem(...)` calls, so no `itemResource` mapping is needed here.
- `OreDictionary` names become tags: `oreQuartz` → `#c:ores/quartz`. `itemBacon` has no host equivalent; omit the rule and record it in `docs/deviations.md` rather than inventing an item.
- `OreDictionary.WILDCARD_VALUE` in the original means "any meta": with meta already split into ids in Task 7, this becomes the base id, and the deviation is recorded.
- An `else` branch of a config flag becomes a `if (!FmConfig.lustDisabled())` guard in Java, matching the original.
- The mob loop (`DarkAspects.java:178-230`) becomes `entity("thaumcraft:brainy_zombie", list, nbt)` style calls using the ids recorded in `host/tools/entity_ids.json`, keeping the exact aspect amounts from the original.

Skeleton, with the first three real entries:

```java
package dev.visfox.forbiddenreforged;

import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistrar;
import thaumcraft.aspect.AspectSync;

public final class FmAspectTags {
    private FmAspectTags() {
    }

    public static void register() {
        AspectSync.addRegistrar(registry -> new Table(registry).apply());
    }

    private static final class Table extends AspectRegistrar {
        Table(thaumcraft.aspect.AspectRegistry registry) {
            super(registry);
        }

        void apply() {
            AspectList netherrack = copy("minecraft:netherrack").add(FmAspects.NETHER, 1);
            register("minecraft:netherrack", netherrack);
            AspectList portal = copy("minecraft:obsidian").add(FmAspects.NETHER, 4);
            register("minecraft:obsidian", portal);
            register("minecraft:nether_star", copy("minecraft:nether_star").add(FmAspects.NETHER, 8).add(FmAspects.PRIDE, 8));
        }
    }
}
```

Call `FmAspectTags.register()` from the mod constructor after `FmAspects.init()`.

- [ ] **Step 3: Run the aspect rebuild and see the numbers.** `AspectSync.rebuild` logs `Aspect tags: N items, M entities (T ms)`; run `cmd //c gradlew.bat runGameTestServer` with the game test above and confirm green. If `getObjectTags` returns `null`, the registrar ran too late — an ordering bug in the constructor, not something to fix in the test.

- [ ] **Step 3b: Assert that nothing was silently skipped.** `AspectRegistrar.resolve()` does not throw on an unknown target: it logs `Aspect target {} is not registered` at **debug** and returns nothing, and `entity()` does the same. A 1.7.10 id that no longer exists — `ItemApi.getItem("itemResource", 14)`, `ConfigItems.itemBaubleBlanks`, `OreDictionary` names like `oreQuartz` or `itemBacon` — would therefore vanish from the port without failing a single gate. Add a test that counts the rules we declared against the rules that landed:

```java
    private static void everyTagTargetResolved(GameTestHelper helper) {
        List<String> unresolved = new ArrayList<>();
        for (String target : FmAspectTags.declaredTargets()) {
            if (target.startsWith("entity:")) {
                if (BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(target.substring("entity:".length()))).isEmpty()) {
                    unresolved.add(target);
                }
            } else if (target.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(target.substring(1)));
                if (BuiltInRegistries.ITEM.getTagOrEmpty(tag).stream().findAny().isEmpty()) {
                    unresolved.add(target);
                }
            } else if (BuiltInRegistries.ITEM.getOptional(Identifier.parse(target)).isEmpty()) {
                unresolved.add(target);
            }
        }
        helper.assertTrue(unresolved.isEmpty(), "unresolved aspect tag targets: " + unresolved);
        helper.succeed();
    }
```

The test needs `java.util.ArrayList`, `java.util.List`, `net.minecraft.core.registries.BuiltInRegistries`, `net.minecraft.resources.Identifier`, `net.minecraft.tags.TagKey`, and `net.minecraft.world.item.Item`; `Registries` is already imported for the GameTest registry.

`FmAspectTags.declaredTargets()` returns the literal list of target strings the table writes, so the two are kept in the same file and cannot drift apart. Entity targets get the same treatment through a second list once Task 6 ports the mob table: an id that is not in `BuiltInRegistries.ENTITY_TYPE` is a bug in our port, not an optional-mod condition, and the difference has to be visible in a test rather than in a debug line nobody reads.

- [ ] **Step 4: Record every substitution in `docs/deviations.md`** as one row per line changed from the original, with the original expression and the new target. A deviation table with fewer rows than `git diff --stat` on the converted file means rows are missing.

- [ ] **Step 5: Commit.**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmAspectTags.java docs/deviations.md
git commit -m "feat: port the add-on aspect tag table"
```

---

### Task 7: Shard items as seven ids, config, models

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmItems.java`
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmCreativeTab.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmConfig.java` (add the remaining switches; `no_lust` and its registration line stay exactly as Task 5 left them)
- Create: `tools/id_map.json`, `tools/gen_resources.py`, `tools/lang/{en_us,ru_ru}.toml` item names
- Test: `FmGameTests` — `shards_are_registered_with_seven_ids`, `no_lust_removes_luxuria_items`

**Interfaces:**
- Produces: `FmItems.DEADLY_SHARDS` (`Map<String, DeferredItem<Item>>` keyed by vice name: `wrath`, `envy`, `taint`, `pride`, `lust`, `sloth`, `greed`), `FmItems.GLUTTONY_SHARD`, `FmItems.SKULL_AXE`, `FmConfig.gluttony`, `FmItems.id(String vice) → Identifier`.
- Consumes: nothing new from the host except `Registration.food`.

- [ ] **Step 1: Write the failing test.**

```java
    private static void shardsAreRegisteredWithSevenIds(GameTestHelper helper) {
        helper.assertValueEqual(FmItems.DEADLY_SHARDS.size(), 7, "the original registers seven vices");
        for (String vice : FmItems.DEADLY_SHARDS.keySet()) {
            Item item = FmItems.DEADLY_SHARDS.get(vice).get();
            helper.assertTrue(BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("forbiddenmagic"), vice + " must be namespaced to the port");
            AspectList tags = thaumcraft.aspect.AspectRegistry.active().getObjectTags(item);
            helper.assertTrue(tags != null && tags.size() > 0, vice + " must carry an aspect tag");
        }
        helper.succeed();
    }
```

- [ ] **Step 2: Extend `FmConfig.java`** — `no_lust` and `register(ModContainer)` are already there from Task 5 and must not be touched; add only the switch this task needs, with the original default from `ForbiddenMagic/…/Config.java:29-40` (`gluttony = 0`) and the original tooltip. The result is the same class Task 5 created plus one field:

```java
package dev.visfox.forbiddenreforged;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FmConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static ModConfigSpec.BooleanValue noLust;
    public static ModConfigSpec.IntValue gluttony;

    static {
        noLust = BUILDER.comment("Enable to remove Luxuria aspect and related items.").define("general.no_lust", false);
        gluttony = BUILDER.comment("0 = Enabled, 1 = Disabled, 2 = Hardcore").defineInRange("general.gluttony", 0, 0, 2);
        SPEC = BUILDER.build();
    }

    private FmConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, SPEC);
    }

    public static boolean lustDisabled() {
        return SPEC.isLoaded() ? noLust.get() : noLust.getDefault();
    }
}
```

- [ ] **Step 3: Write `FmItems.java`**, one item per vice instead of one item with seven metas (spec 4.5). `gluttonyShard` is a food with the original values `super(2, 0.1F, false)` → `Registration.food(properties, 2, 0.1F)`:

```java
package dev.visfox.forbiddenreforged;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.platform.Registration;

public final class FmItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ForbiddenMagic.MODID);
    public static final Map<String, DeferredItem<Item>> DEADLY_SHARDS = new LinkedHashMap<>();
    public static final DeferredItem<Item> GLUTTONY_SHARD;
    public static final DeferredItem<Item> SKULL_AXE;

    static {
        for (String vice : new String[]{"wrath", "envy", "taint", "pride", "lust", "sloth", "greed"}) {
            DEADLY_SHARDS.put(vice, ITEMS.registerSimpleItem(vice + "_shard"));
        }
        GLUTTONY_SHARD = ITEMS.registerSimpleItem("gluttony_shard", properties -> Registration.food(properties, 2, 0.1F));
        SKULL_AXE = ITEMS.registerSimpleItem("skull_axe", properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    private FmItems() {
    }
}
```

`SKULL_AXE` is registered as a plain item on purpose: the behaviour of `ItemSkullAxe` (extra head drop chance) belongs to the enchantment-and-tools slice, but the `SKULLAXE` recipe of Task 10 must resolve to a registered output, and an unregistered output is exactly what the closure test in Task 10 Step 1 forbids. The deviation — id and item exist, behaviour does not yet — gets its own row in `docs/deviations.md`, and its removal is a named line in `docs/roadmap.md`.

Register `FmItems.ITEMS.register(modEventBus)` in the constructor. `docs/deviations.md` records the mapping `deadlyShards meta n → <vice>_shard` for all seven, taken from `ItemDeadlyShard.vices` at `items/ItemDeadlyShard.java:19`, and the original's `despair` substitution when `Config.noLust` is on (line 29-30) as an *availability* removal of the lust item: the registry entry stays, but the item gets no tag, no drop, no recipe and no creative-tab slot.

- [ ] **Step 3b: Add the items to the host's creative tab.** Reuse `thaumcraft:thaumcraft` rather than creating a second tab; the event listener also enforces the visible half of `no_lust`:

```java
package dev.visfox.forbiddenreforged;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import thaumcraft.registry.ModCreativeTabs;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmCreativeTab {
    private FmCreativeTab() {
    }

    @SubscribeEvent
    static void addItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != ModCreativeTabs.THAUMCRAFT.get()) {
            return;
        }
        for (var entry : FmItems.DEADLY_SHARDS.entrySet()) {
            if (!entry.getKey().equals("lust") || !FmConfig.lustDisabled()) {
                event.accept(entry.getValue().get());
            }
        }
        event.accept(FmItems.GLUTTONY_SHARD.get());
        event.accept(FmItems.SKULL_AXE.get());
    }
}
```

- [ ] **Step 4: Generate models and item definitions.** `tools/gen_resources.py` writes, for each registered item, `assets/forbiddenmagic/models/item/<name>.json` (`{"parent":"minecraft:item/generated","textures":{"layer0":"forbiddenmagic:items/<name>"}}`) and `assets/forbiddenmagic/items/<name>.json`.

The texture source filenames are read from the original, not guessed: `ItemDeadlyShard.registerIcons` (`items/ItemDeadlyShard.java:34-39`) registers `"forbidden:" + vices[x] + "shard"`, so `wrath_shard` comes from `wrathshard.png`, `envy_shard` from `envyshard.png`, `taint_shard` from `taintshard.png`, `pride_shard` from `prideshard.png`, `lust_shard` from `lustshard.png`, `sloth_shard` from `slothshard.png`, `greed_shard` from `greedshard.png`; `gluttony_shard` from `gluttonyshard.png` and `skull_axe` from `skullaxe.png` (`items/ItemGluttonyShard.java:20`, `items/tools/ItemSkullAxe.java`). Note two mismatches that must be recorded rather than patched over: when `Config.noLust` is set the original renames index 4 to `despair`, so `despairshard.png` exists but the modern port expresses that switch as a config-gated recipe (Step 6), and `prideshard.png`/`wrathshard.png` carry `.mcmeta` (animated frames) — copy the `.mcmeta` alongside, or the animation silently disappears.

The generator's item list is a literal table at the top of the file mapping our item name to the FM source filename, so the mapping is auditable instead of derived:

```python
ITEM_TEXTURES = {
    "wrath_shard": "wrathshard",
    "envy_shard": "envyshard",
    "taint_shard": "taintshard",
    "pride_shard": "prideshard",
    "lust_shard": "lustshard",
    "sloth_shard": "slothshard",
    "greed_shard": "greedshard",
    "gluttony_shard": "gluttonyshard",
    "skull_axe": "skullaxe",
}
```

Copy every listed texture (and its `.mcmeta` when present) from `../ForbiddenMagic/src/main/resources/assets/forbidden/textures/items/` into `assets/forbiddenmagic/textures/items/` under the new name, and make the generator fail when a registered item has no entry in `ITEM_TEXTURES`:

```python
missing = [name for name in registered_items if name not in ITEM_TEXTURES]
if missing:
    raise SystemExit(f"no texture mapping for {missing}")
```

- [ ] **Step 4b: Add the one helper later tasks call.** In `FmItems`:

```java
    public static net.minecraft.resources.Identifier id(String vice) {
        return Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, vice + "_shard");
    }
```

with `import net.minecraft.resources.Identifier;`, and replace the hardcoded `ForbiddenMagic.id("wrath_shard")` calls in Tasks 8 and 10 with `FmItems.id("wrath")` so the vice-to-id rule lives in one place.

```bash
python tools/gen_resources.py
python tools/gen_resources.py
git status --porcelain src/generated
```

Expected: `generated N files`, second run leaves git clean.

- [ ] **Step 5: Add lang entries** for `item.forbiddenmagic.wrath_shard` … plus the two foods, in both `en_us` and `ru_ru`, from the original `assets/forbidden/lang/en_US.lang` and `ru_RU.lang` (keys `item.NetherShard.wrath.name`, `item.GluttonyShard.name`). Then run:

```bash
cmd //c gradlew.bat runGameTestServer
```

Expected: `shards_are_registered_with_seven_ids` green.

- [ ] **Step 6: Prove the kill switch with a second server run.** A STARTUP config is read before the static aspect registry is built, and the registry is never rebuilt at runtime, so flipping `FmConfig.noLust` inside a running GameTest cannot remove `luxuria` — that would test nothing. Instead write `run/config/forbiddenmagic-startup.toml`:

```toml
[general]
no_lust = true
```

Run `runGameTestServer` again and assert, in tests that branch on `FmConfig.lustDisabled()`, that `FmAspects.LUST` is null, `Aspect.getAspect("luxuria")` is absent, the lust shard carries no `luxuria` tag, it is not offered to the creative tab, and the Task 10 recipe is absent for lust entries. Then delete the config file and re-run the default gate, which asserts seven aspects. The item record remaining registered is the documented deviation, not a silent leak: `DeferredRegister` cannot remove an entry, and the spec's increment gate says "убирает из выдачи", not "удаляет из реестра". Add the guards in `FmAspects.init`, `FmAspectTags`, `FmRecipes`, and the creative-tab listener, and record the two-run gate in `docs/deviations.md`.

- [ ] **Step 7: Commit.**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmItems.java src/main/java/dev/visfox/forbiddenreforged/FmCreativeTab.java src/main/java/dev/visfox/forbiddenreforged/FmConfig.java tools/id_map.json tools/gen_resources.py tools/lang src/main/resources/assets/forbiddenmagic/textures/items docs/deviations.md
git commit -m "feat: port the shard items and the config"
```

---

### Task 8: How a player actually gets a shard (the drop rules)

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmDrops.java`
- Test: `FmGameTests` — `mob_killed_by_player_drops_wrath_shard`, `boss_drops_pride_shards`, `unattended_death_drops_sloth_shard`

**Interfaces:**
- Produces: `FmDrops.register()` doing `net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(FmDrops::onLivingDrops)`; `FmDrops.onLivingDrops(LivingDropsEvent)`; `FmDrops.rollWrath(int wrathBonus, int roll) → boolean`, `FmDrops.prideCount(int looting, int roll) → int`, `FmDrops.wrathShardStack() → ItemStack`, so the probabilities are testable without killing 1000 mobs.
- Consumes: `FmItems`, `FmConfig`, host `thaumcraft.entity.TaintedMob`-equivalent marker from `host/src/main/java/thaumcraft/entity/TaintedMob.java`.

- [ ] **Step 1: Write the failing tests.** Probabilities are the numbers in `FMEventHandler.java:164-230`, and every rule is nether-only (`level.dimension() == Level.NETHER`, the 1.7.10 check is `dimensionId == -1`): sloth on non-player death `randy.nextInt(30) < 4`, dropping `1 + randy.nextInt(3)` shards; wrath on player-killed `IMob` `randy.nextInt(61) <= wrath` with `wrath` starting at 2 plus weapon damage and enchantments; pride on bosses `2 + randy.nextInt(1 + looting)`; greed when `greed > 0` `randy.nextInt(20) <= greed`. Expose the rolls as pure functions and test the boundaries:

```java
    private static void mobKilledByPlayerDropsWrathShard(GameTestHelper helper) {
        helper.assertTrue(FmDrops.rollWrath(2, 2), "chance 2 must drop at roll 2, the original uses nextInt(61) <= wrath");
        helper.assertFalse(FmDrops.rollWrath(2, 3), "chance 2 must not drop at roll 3");
        helper.assertValueEqual(FmDrops.prideCount(0, 0), 2, "boss without looting drops two pride shards");
        helper.assertValueEqual(FmDrops.prideCount(2, 1), 3, "looting 2 with roll 1 gives three");
        helper.assertValueEqual(FmDrops.wrathShardStack().getItem(), FmItems.DEADLY_SHARDS.get("wrath").get(), "wrath drop must be the wrath shard");
        helper.succeed();
    }
```

`prideCount(looting, roll)` mirrors `2 + randy.nextInt(1 + looting)`, so `roll` is the value `nextInt` already returned; `rollWrath(bonus, roll)` mirrors `randy.nextInt(61) <= wrath`.

- [ ] **Step 2: Implement `FmDrops`** as a direct translation of the handler, replacing `event.entityLiving instanceof IMob` with `entity instanceof Enemy`, `getCurrentEquippedItem()` with `entity.getMainHandItem()`, and the `EntityPigZombie` plus held envy shard check with `entity.getType() == EntityType.ZOMBIFIED_PIGLIN && entity.getMainHandItem().is(FmItems.DEADLY_SHARDS.get("envy").get())`. Keep the nether-only guard and the sloth count `1 + random.nextInt(3)`. Record each substitution in `docs/deviations.md`, because the loader changed the vocabulary, not the numbers.

- [ ] **Step 3: Run the tests, then verify the real path in game.** `cmd //c gradlew.bat runGameTestServer`, then kill a zombie in `runClient` with the game test namespace enabled and check the log line `forbiddenmagic: dropped forbiddenmagic:wrath_shard`. Add that log line as part of the implementation so the manual check has evidence.

- [ ] **Step 4: Commit.**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmDrops.java src/main/java/dev/visfox/forbiddenreforged/ForbiddenMagic.java docs/deviations.md
git commit -m "feat: port the shard drop rules"
```

---

### Task 9: Research category and the shard entries

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmResearch.java`
- Create: `src/main/resources/assets/forbiddenmagic/textures/misc/forbidden.png` (original `forbidden/textures/gui/forbidden.png` if present, otherwise record the substitution)
- Test: `FmGameTests` — `forbidden_category_and_shard_entries_resolve`

**Interfaces:**
- Produces: `FmResearch.register()`, entry key `NETHERSHARDS` in category `FORBIDDEN`, plus `HELLFIRE`, `SKULLAXE` (hidden/concealed flags preserved from the original).
- Consumes: `ResearchCategories.registerCategory(String, Identifier, Identifier)`, `new ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, TcStack icon)`, `setStub/setRound/setAutoUnlock/setHidden/setConcealed/setWarp/setParents/setAspectTriggers/setPages`, `ResearchPage.text/aspects/arcane/crucible/infusion/infusionEnchantment`.

- [ ] **Step 1: Write the failing test.** Positions and complexity are taken from `ForbiddenResearch.java:54-70` verbatim. Every getter below is confirmed present in the host: `key()`, `category()`, `column()`, `row()`, `complexity()`, `warp()`, `tags()`, `parents()`, `pages()`, and the flags `isSpecial/isSecondary/isRound/isStub/isVirtual/isConcealed/isHidden/isLost/isAutoUnlock` (`research/ResearchItem.java:177-259`) — no host patch is needed to observe them:

```java
    private static void forbiddenCategoryAndShardEntriesResolve(GameTestHelper helper) {
        ResearchItem shards = ResearchCategories.getResearch("NETHERSHARDS");
        helper.assertTrue(shards != null, "NETHERSHARDS must be registered");
        helper.assertValueEqual(shards.category(), "FORBIDDEN", "category");
        helper.assertValueEqual(shards.column(), -8, "column");
        helper.assertValueEqual(shards.row(), -5, "row");
        helper.assertTrue(shards.isStub() && shards.isRound() && shards.isAutoUnlock(), "stub, round and auto-unlock as in the original");
        ResearchItem axe = ResearchCategories.getResearch("SKULLAXE");
        helper.assertTrue(axe.isConcealed(), "SKULLAXE must stay concealed");
        helper.assertValueEqual(axe.warp(), 1, "SKULLAXE carries one warp point as in the original");
        helper.assertValueEqual(axe.parents(), List.of("NETHERSHARDS", "THAUMIUM", "INFUSION"), "SKULLAXE parents, in order");
        helper.succeed();
    }
```

- [ ] **Step 2: Implement `FmResearch`** with the entries in the B slice: `NETHERSHARDS` (stub, round, auto-unlock, icon `FmItems.DEADLY_SHARDS.get("wrath")`), `HELLFIRE`, `SKULLAXE`, `ROD_profane` only if Task 10 registers that rod. Page texts go to `tools/lang` under `forbiddenmagic.research_page.<KEY>.<n>`, copied from the original lang file, and `tc.research_name.<KEY>` / `tc.research_text.<KEY>` entries as the host expects them.

- [ ] **Step 3: Run and commit.**

```bash
cmd //c gradlew.bat runGameTestServer
git add src/main/java/dev/visfox/forbiddenreforged/FmResearch.java tools/lang src/main/resources/assets/forbiddenmagic/textures/misc
git commit -m "feat: add the forbidden research category and shard entries"
```

---

### Task 10: Recipes that close inside the slice

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmRecipes.java`
- Test: `FmGameTests` — `skull_axe_infusion_recipe_matches_original`, `every_ported_recipe_resolves_its_inputs`

**Interfaces:**
- Produces: registered `TcRecipes` entries keyed exactly as the original (`SKULLAXE`, `ROD_infernal`, `ARCANECAKE`, `WRATHCAGE`, `TAINTSTONE`), so `ResearchPage.infusion("SKULLAXE")` finds them. `FmRecipes.OWN_KEYS` contains exactly the keys actually registered; the other four are included only when every input has a ported or host item, otherwise they stay out of the slice and are listed in `docs/roadmap.md`.
- Consumes: `InfusionRecipe(String key, String research, TcStack result, int instability, AspectList aspects, TcIngredient central, List<TcIngredient> components)`, `ShapedArcaneRecipe(String key, String research, TcStack result, AspectList aspects, int width, int height, List<TcIngredient> grid)`, `CrucibleRecipe(String key, String research, TcStack result, TcIngredient catalyst, AspectList aspects)`, `TcIngredient.item("namespace:path")`, `TcStack.of(...)`.

- [ ] **Step 1: Write the closure test first**, because it is what makes this task honest: every ported recipe's inputs and outputs must resolve to a registered item.

```java
    private static void everyPortedRecipeResolvesItsInputs(GameTestHelper helper) {
        List<String> missing = new ArrayList<>();
        for (TcRecipe recipe : TcRecipes.all()) {
            if (!FmRecipes.OWN_KEYS.contains(recipe.key())) {
                continue;
            }
            if (recipe instanceof InfusionRecipe infusion) {
                checkIngredient(infusion.central(), missing);
                infusion.components().forEach(ingredient -> checkIngredient(ingredient, missing));
                checkStack(infusion.result(), missing);
            } else if (recipe instanceof ShapedArcaneRecipe shaped) {
                checkStack(shaped.result(), missing);
                shaped.grid().forEach(ingredient -> checkIngredient(ingredient, missing));
            } else if (recipe instanceof ShapelessArcaneRecipe shapeless) {
                checkStack(shapeless.result(), missing);
                shapeless.ingredients().forEach(ingredient -> checkIngredient(ingredient, missing));
            } else if (recipe instanceof CrucibleRecipe crucible) {
                checkIngredient(crucible.catalyst(), missing);
                checkStack(crucible.result(), missing);
            }
        }
        helper.assertTrue(missing.isEmpty(), "unresolved recipe references: " + missing);
        helper.succeed();
    }

    private static void checkIngredient(TcIngredient ingredient, List<String> missing) {
        if (!ingredient.exists()) {
            missing.add(ingredient.toString());
        }
    }

    private static void checkStack(TcStack stack, List<String> missing) {
        if (!stack.exists()) {
            missing.add(stack.item().toString());
        }
    }
```

`TcIngredient.exists()` and `TcStack.exists()` are public host methods, so the closure test needs no registry walking. Imports: `java.util.ArrayList`, `java.util.List`, and the `thaumcraft.crafting` types used (`TcRecipe`, `TcRecipes`, `InfusionRecipe`, `ShapedArcaneRecipe`, `ShapelessArcaneRecipe`, `CrucibleRecipe`, `TcIngredient`, `TcStack`). Host recipe keys are plain (`ConfigRecipes` registers `"Banner_0"`, research pages call `TcRecipes.get("...")` with the same plain string), so `OWN_KEYS` holds `"SKULLAXE"` etc., not `"forbiddenmagic:SKULLAXE"`.

- [ ] **Step 2: Port the recipe set**, numbers verbatim from `ForbiddenRecipes.java:31-58`. `SkullAxe` (line 40) becomes:

```java
        TcRecipes.register(new InfusionRecipe(
            "SKULLAXE",
            "SKULLAXE",
            TcStack.of(ForbiddenMagic.id("skull_axe").toString(), 1),
            1,
            new AspectList().add(FmAspects.WRATH, 8).add(Aspect.WEAPON, 8).add(FmAspects.NETHER, 8),
            TcIngredient.item("thaumcraft:thaumium_axe"),
            List.of(
                TcIngredient.item(ForbiddenMagic.id("wrath_shard").toString()),
                TcIngredient.item(ForbiddenMagic.id("wrath_shard").toString()),
                TcIngredient.item("minecraft:diamond"),
                TcIngredient.item("minecraft:wither_skeleton_skull")
            )
        ));
```

The inputs that pointed at the port's own not-yet-ported content (`ItemApi.getItem("itemResource", 14)`, `ConfigItems.itemBaubleBlanks`, `ForbiddenBlocks.starBlock`, `ForbiddenItems.wandCore`) are either mapped to a host item that already exists (record it in `docs/deviations.md`) or the whole recipe is left out of the slice and named in `docs/roadmap.md`. The closure test in Step 1 is what makes silently dropping them impossible.

- [ ] **Step 3: Run the cost tests.** `skull_axe_infusion_recipe_matches_original` asserts instability `1`, cost total `8 + 8 + 8` and the four components. Then:

```bash
cmd //c gradlew.bat runGameTestServer
python tools/check_provenance.py
```

Expected: green, because every id referenced is either host-owned by namespace (`thaumcraft:`, `minecraft:`) or ours.

- [ ] **Step 4: Commit.**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmRecipes.java docs/deviations.md docs/roadmap.md
git commit -m "feat: port the recipes inside the slice"
```

---

### Task 11: End-to-end chain, documentation, first release artifact

**Files:**
- Create: `README.md`, `NOTICE`, `docs/roadmap.md` (public section), `docs/upstream.md`
- Create: `docs/upstream/issue-draft.md` (Russian) and the branch push script that the user runs by hand
- Test: `FmGameTests` — `chain_from_netherrack_to_wrath_shard`

**Interfaces:**
- Produces: `build/libs/forbidden-magic-0.574-26.3-port.0.1.0.jar`; a README whose first three lines are the supported-range, what-is-missing, and tempo statements from spec section 11.
- Consumes: everything.

- [ ] **Step 0: Use the verified research surface, not guessed names.** Confirmed in `research/ResearchManager.java`: `complete(player, key)`, `complete(player, key, boolean)`, `isComplete(player, key)`, `grantAutoUnlock(player)`, `createClue(player, key)`, and `Knowledge.get(player)` — the last two appear in the host's own `WarpGameTests.java:120` and `ResearchManager.java:121-130`. Hidden research is keyed with an `@` prefix (`"@" + key`), so never assert on a bare key for a clue. If an assertion below needs a member outside this list, read the file first rather than inventing the name.

- [ ] **Step 1: Write the failing end-to-end test** — this is the criterion in spec 5.0, in one scenario:

```java
    private static void chainFromNetherrackToWrathShard(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        AspectList tags = thaumcraft.aspect.AspectRegistry.active().getObjectTags(net.minecraft.world.item.Items.NETHERRACK);
        helper.assertTrue(tags.getAmount(FmAspects.NETHER) >= 1, "netherrack yields infernus");
        helper.assertTrue(ResearchCategories.getResearch("NETHERSHARDS") != null, "shard research exists");
        thaumcraft.research.ResearchManager.grantAutoUnlock(player);
        helper.assertTrue(thaumcraft.research.ResearchManager.isComplete(player, "NETHERSHARDS"), "the auto-unlock entry is known after a join");
        ItemStack wrathShard = new ItemStack(FmItems.DEADLY_SHARDS.get("wrath").get());
        helper.assertTrue(thaumcraft.aspect.AspectRegistry.active().getObjectTags(wrathShard.getItem()).getAmount(FmAspects.WRATH) >= 2, "the wrath shard carries ira, the value DarkAspects gives it");
        helper.assertTrue(FmDrops.rollWrath(2, 2), "the wrath drop rule fires at the original boundary");
        InfusionRecipe skullAxe = (InfusionRecipe) thaumcraft.crafting.TcRecipes.get("SKULLAXE");
        helper.assertTrue(skullAxe != null, "skull axe recipe registered");
        helper.assertTrue(skullAxe.aspects().getAmount(FmAspects.WRATH) == 8, "skull axe costs eight ira");
        helper.succeed();
    }
```

Note the shape: no recipe crafts a shard, because in the original no recipe crafts a shard — the chain is tag → drop → research → a recipe that consumes it.

with the player helper taken from the host's `GearGameTests.java:79-91` — the `markClientLoaded()` call is not decoration, a mock player without a loaded connection is invulnerable and behaves inconsistently in research and warp paths:

```java
    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var cookie = net.minecraft.server.level.CommonListenerCookie.createInitial(player.getGameProfile(), false);
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(net.minecraft.network.protocol.Packet<?> packet) {
            }
        };
        player.connection.markClientLoaded();
        return player;
    }
```

- [ ] **Step 2: Run everything the gate requires.**

```bash
cmd //c gradlew.bat build
cmd //c gradlew.bat runGameTestServer
python tools/prepare_host.py --verify
python tools/check_host_clean.py
python tools/check_provenance.py
python tools/gen_resources.py && git status --porcelain src/generated
git -C ../thaumcraft-4-reforged status --porcelain --untracked-files=no
```

Expected, in order: build success, a `GAME TESTS COMPLETE` line with no `required tests failed`, `host matches sha + patch series`, `host clean`, `provenance ok`, empty git status for generated resources, empty host status. Paste the actual output into the commit body of Step 5; a claim without it is not a pass.

- [ ] **Step 3: Write `README.md`** with, in this order: what it is and that it is an unofficial fork (spec 10 table verbatim), the three obligation lines from spec 11 (supported range `Thaumcraft 4: Reforged 1.2+` on `26.3`, "what is not in the port", tempo/no-promises), how to build the host copy (`python tools/prepare_host.py`, `cd host && cmd //c gradlew.bat publish`, then `cmd //c gradlew.bat build`), the AI-assistance disclosure, and links to `docs/roadmap.md` and `docs/deviations.md`.

- [ ] **Step 4: Update `docs/upstream/issue-draft.md`** (Task 4b created it) with the final gate outputs, in Russian, structured as: the constraint (`Aspect` is an enum of 48, so an add-on cannot register an aspect), what we need (a registration point before `AspectSync.rebuild`, add-on-owned texture namespace, stable builtin indices), the precedent (Thaumcraft 4 Reconstructed already ships data-driven add-on aspects and 32 add-ons including Forbidden Magic ride on it), the patch series as proof, and the check outputs. State that the work was AI-assisted. Do not send it; the user does.

- [ ] **Step 5: Commit, then tag nothing and push nothing.**

```bash
git add README.md NOTICE docs/roadmap.md docs/upstream.md tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "docs: ship the slice with its release notes and upstream draft"
```

Report the jar path and its size. Opening an issue, forking, or pushing to any remote requires an explicit instruction from the user.

---

## Self-Review Notes

Spec coverage: section 3 and 4.2 → Tasks 3-4; 4.1 → Tasks 1-2; 4.3 → Tasks 5-10; 4.4 → the `docs/deviations.md` steps in 6, 8, 10; 4.5 → Task 7 Steps 4-5 and the regeneration check in Task 11 Step 2; 5.0 → Tasks 5-11; 6 gates → Task 11 Step 2 plus each task's own gate; 7 risks → Task 3 (ordinal), Task 4 Step 5 and Task 11 Step 2 (client/server registration), Task 5 (availability refusal), Task 8 Step 3 (manual evidence); 8 → Tasks 4 and 11; 10 → Task 11 Step 3; 11 → Task 11 Steps 3-5.

Fixed during this review, recorded so the next reader does not re-introduce them: Task 1 no longer needs the maven artifact (it compiles against the already-built upstream jar, and Task 4 switches the dependency form); `runGameTestServer` uses `--tests "${mod_id}:*"` so the add-on gate runs only its own tests; the host's tests are a separate gate (`host/runGameTestServer`, 256/256), because the combined 259-test suite perturbs the RNG/order-sensitive `altar_guardian_population` host test. Task 5 moves the aspect registration from the constructor to `FMLCommonSetupEvent`, because a STARTUP config must be loaded before `no_lust` can suppress `luxuria`; the probe aspect in Task 4 is compound, because a primal add-on aspect joins `getPrimalAspects()` and breaks the wand's six vis slots (observed as 14 failing host tests); the probe is explicitly retired in Task 5. `Codec` uses `xmap`, not `map`; `AspectOrb` is not the only enum consumer — `WandVis` has two `switch (aspect)` statements. Host recipe keys are plain strings, so `SKULLAXE` must not be namespaced. The content counts were re-measured: 30 core research entries (31 unique keys minus `SUBCOLLAR`), 40 unique text pages (82 `ResearchPage` objects including recipe pages and config branches), 21 core recipes (22 unique minus `SUBCOLLAR`). Every `ResearchItem` and `Knowledge` call in Tasks 9 and 11 goes through a read-the-signature step first. The vice-to-texture table in Task 7 is sourced from the original `registerIcons` code and the real filenames (`wrathshard.png`, `prideshard.png`, plus `.mcmeta` for the animated ones), not guessed.

Known limitation of the end-to-end test: it checks the data chain (tag → research → recipe lookup) and the pure drop boundary, but it does not construct a real `LivingDropsEvent`; the event wiring is covered by Task 8 Step 3's manual in-game check.

Deliberately deferred, recorded rather than implemented: patch `0004-taint-block-tag` (decided in spec increment 8, this plan stops before it), the heretic villager trades (the lust-shard exchange needs villager registry work and the slice already obtains shards through drops), enchantments, wands, Wrath Cage, the taint wood set, and the full behaviour of `ItemSkullAxe` (id and item registered so the recipe closes). Each is in `docs/roadmap.md` with the reason.
