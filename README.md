# Forbidden Magic: Reforged — Minecraft 1.21.1

An unofficial port of **Forbidden Magic 0.574** (Minecraft 1.7.10) to **Thaumcraft 4: Reforged** on **Minecraft 1.21.1**. This branch is developed separately from the Minecraft 26.3 port.

| Minecraft | NeoForge | Thaumcraft 4: Reforged | Mod Version | Release Tag |
| --- | --- | --- | --- | --- |
| **1.21.1** | 21.1.256 | 1.1.2 | `0.574-1.21.1-port.0.1.0` | [`1.21.1`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/1.21.1) |

> **Requires** [Curios 9.5.1+1.21.1](https://www.curseforge.com/minecraft/mc-mods/curios) (bundled in runtime classpath for GameTest).

---

## Status

- Target environment: Minecraft 1.21.1, NeoForge 21.1.x, Java 21
- Full parity with original not confirmed. Current progress and remaining tasks in [roadmap](docs/roadmap.md)
- Requires Thaumcraft 4: Reforged 1.1.2. This branch uses upstream addon aspect API.

---

## Installation

Verify the downloaded file is for **Minecraft 1.21.1 and NeoForge 21.1.x** — the 26.3 build is incompatible. Release files available at [Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases). If no suitable release exists, build from this branch.

Required for play: NeoForge, Thaumcraft 4: Reforged 1.1.2, and host dependencies including Curios. Place mod JAR in `mods/` folder.

---

## Building & Testing

Requires Git, Python 3, and JDK 21.

Clone the branch and host repository side by side:

```powershell
git clone --branch 1.21.1 https://github.com/MxxFoxx2/forbidden-magic-reforged.git forbidden-magic-reforged-1.21.1
cd forbidden-magic-reforged-1.21.1
git clone https://github.com/notlikethiss/thaumcraft-4-reforged.git ../thaumcraft-4-reforged
python tools/prepare_host.py
.\gradlew.bat build --no-daemon
.\gradlew.bat runGameTestServer --no-daemon
```

On Linux/macOS use `python3` and `./gradlew`. The `tools/prepare_host.py` script prepares the pinned host version from `host.sha` into `host/`. Gradle uses `host/` via composite build; no separate `publish` needed. Do not edit `host/` manually.

On successful build, JAR is created at `build/libs/forbidden-magic-0.574-1.21.1-port.0.1.0.jar`.

---

## Fidelity & Verification

This port aims for **1:1 logic, numbers, recipes, and text parity** with the original Forbidden Magic 0.574, within modern API limits.

- **Fidelity Ledger**: [docs/fidelity.md](docs/fidelity.md) — every known deviation, compromise, and parity gap
- **Verification Report**: [docs/verification.md](docs/verification.md) — GameTest results, CI status
- **Roadmap**: [docs/roadmap.md](docs/roadmap.md) — current priorities and blockers

> **Visual and gameplay parity is only confirmed via `runClient` and actual play.** Green compilation and GameTests are necessary but not sufficient.

---

## Project Structure

```
src/main/java/dev/visfox/forbiddenreforged/
├── core/
│   ├── aspect/      # Aspect definitions (host-neutral)
│   ├── model/       # ContentId, HostContent (host-neutral)
│   ├── recipe/      # Recipe definitions (host-neutral)
│   ├── research/    # Research definitions (host-neutral)
│   ├── wand/        # Wand behaviors (Hellfire, Blink Focus)
│   └── warp/        # Warp access abstraction
├── integration/
│   ├── api/         # HostAdapter, HostProfile, HostAdapterRegistry
│   ├── runtime/     # 15 capability interfaces (HostXxxServices)
│   └── tc4/         # Thaumcraft 4: Reforged implementation
├── client/          # Client-only code (renderers, creative tabs)
├── test/            # GameTest implementations (compile into main jar)
├── FmBlocks.java    # Block registration
├── FmItems.java     # Item registration
├── FmResearch.java  # Research registration
├── FmRecipes.java   # Recipe registration
└── ForbiddenMagic.java  # Main mod class (composition root)
```

---

## Credits & License

| Role | Author |
| --- | --- |
| **Original Forbidden Magic 0.574** | [SpitefulFox](https://github.com/SpitefulFox/ForbiddenMagic) (WTFPL) |
| **Thaumcraft 4** | Azanor |
| **Thaumcraft 4: Reforged** | [notlikethiss](https://github.com/notlikethiss/thaumcraft-4-reforged) |
| **Forbidden Magic: Reforged** | [MxxFoxx2](https://github.com/MxxFoxx2/forbidden-magic-reforged) (MIT) |

- The original mod's logic, numbers, recipes, texts, and assets are ported 1:1 where the modern API permits.
- Thaumcraft assets belong to Azanor. This repository contains ported logic and assets and is **not intended for redistribution** outside personal use.
- See [LICENSE](LICENSE) and [NOTICE](NOTICE) for full terms.

---

## Links

- **Releases**: [GitHub Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases)
- **Issues**: [GitHub Issues](https://github.com/MxxFoxx2/forbidden-magic-reforged/issues)
- **Thaumcraft 4: Reforged**: [GitHub](https://github.com/notlikethiss/thaumcraft-4-reforged)
- **Original Forbidden Magic**: [GitHub](https://github.com/SpitefulFox/ForbiddenMagic)