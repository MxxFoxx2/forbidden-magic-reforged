# Forbidden Magic: Reforged

A port of **Forbidden Magic 0.574** (Minecraft 1.7.10, by SpitefulFox) to **Thaumcraft 4: Reforged** for **Minecraft 26.3** / **NeoForge 26.3.0.57-beta**.

| Minecraft | NeoForge | Thaumcraft 4: Reforged | Release |
| --- | --- | --- | --- |
| **26.3** | 26.3.0.57-beta | 1.1.3 | [`release/26.3`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release/26.3) |
| **26.1.2** | 26.1.2.122 | 1.1.2 | [`release/26.1.2`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release/26.1.2) |
| **1.21.1** | 21.1.256 | 1.1.2 | [`release/1.21.1`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release/1.21.1) |

> The current mod version lives on the channel's rolling release page: the numbers auto-increment on each publish. A local dev build uses `mod_version` from `gradle.properties` and does not match the release number.
> Minecraft **1.21.1** requires [Curios 9.5.1+1.21.1](https://www.curseforge.com/minecraft/mc-mods/curios).
> There is no shared binary between Minecraft versions — each branch is built and released separately.

---

## Installation

1. Install **Minecraft 26.3** with **NeoForge 26.3.0.57-beta**
2. Install **Thaumcraft 4: Reforged 1.1.3** (required)
3. For 1.21.1: also install **Curios 9.5.1+**
4. Drop `forbidden-magic-<version>-<mc>.jar` into the `mods/` folder
5. The **Apocrypha** tab appears in the Thaumonomicon

---

## Configuration

Config file: `config/forbiddenmagic-startup.toml` (created on first launch). Each option carries its own description and default in the file.

> `no_lust`, `enchanting`, `greedy_ench`, `emerald_transmutation`, and `wrath_cage` are **gates**: they decide what gets registered, so they are read at startup and synced to the client. A mismatch disconnects the client instead of diverging registries.

---

## Building and development

### Requirements
- **Java 25** (Java 21 for the 1.21.1 branch)
- **Python 3** (for host preparation, resource generation, validation)
- Gradle wrapper included (`./gradlew`)

### Commands
```bash
# Host preparation (Thaumcraft 4: Reforged sources + patches)
python tools/prepare_host.py          # build host/
python tools/prepare_host.py --verify # verification only

# Build
./gradlew build                       # builds both the host and the addon (composite build)

# Run
./gradlew runClient                   # dev client
./gradlew runGameTestServer           # automated GameTests (headless)

# Validation
python tools/check_host_agnostic_api.py  # check host-agnostic boundaries
python tools/check_provenance.py         # check JAR provenance
python tools/check_host_pin.py           # check the host pin and patch applicability
python tools/gen_resources.py            # regenerate resources (must leave a clean diff)
```

The full verification gate and contribution rules are in [AGENTS.md](AGENTS.md).

---

## Project structure

```
src/main/java/dev/visfox/forbiddenreforged/
├── core/
│   ├── aspect/      # Aspect definitions (host-neutral)
│   ├── model/       # ContentId, HostContent (host-neutral)
│   ├── recipe/      # Recipe definitions (host-neutral)
│   ├── research/    # Research definitions (host-neutral)
│   ├── wand/        # Wand behaviours (Hellfire, Blink Focus)
│   └── warp/        # Warp access abstraction
├── integration/
│   ├── api/         # HostAdapter, HostProfile, HostServices, HostAdapterRegistry
│   ├── runtime/     # 16 capability ports (HostXxxServices)
│   └── tc4/         # Thaumcraft 4: Reforged implementation (Tc4Services + narrow adapters)
├── common/          # Shared blocks, items, Fm* registrations, config, sync
├── client/          # Client code (renderers, creative tabs)
└── ForbiddenMagic.java  # Main mod class (composition root)

src/gameTest/java/dev/visfox/forbiddenreforged/
└── test/            # GameTest implementations (separate source set, not included in the JAR)
```

---

## Authors and license

| Role | Author |
| --- | --- |
| **Original Forbidden Magic 0.574** | [SpitefulFox](https://github.com/SpitefulFox/ForbiddenMagic) (WTFPL) |
| **Thaumcraft 4** | Azanor |
| **Thaumcraft 4: Reforged** | [notlikethiss](https://github.com/notlikethiss/thaumcraft-4-reforged) |
| **Forbidden Magic: Reforged** | [MxxFoxx2](https://github.com/MxxFoxx2/forbidden-magic-reforged) (MIT) |

- The original's logic, numbers, recipes, texts, and assets are ported 1:1 wherever the modern API allows.
- Thaumcraft assets belong to Azanor. This repository contains ported logic and assets and is **not intended for redistribution** beyond personal use.
- See [LICENSE](LICENSE) and [NOTICE](NOTICE) for the full terms.

---

## Links

- **Releases**: [GitHub Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases)
- **Issues**: [GitHub Issues](https://github.com/MxxFoxx2/forbidden-magic-reforged/issues)
- **Thaumcraft 4: Reforged**: [GitHub](https://github.com/notlikethiss/thaumcraft-4-reforged)
- **Original Forbidden Magic**: [GitHub](https://github.com/SpitefulFox/ForbiddenMagic)
- **Documentation**: [deviations](docs/fidelity.md), [verification](docs/verification.md), [roadmap](docs/roadmap.md)
