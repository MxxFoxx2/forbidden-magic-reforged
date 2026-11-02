# Forbidden Magic: Reforged

Неофициальный порт **Forbidden Magic 0.574** (оригинал для Minecraft 1.7.10) на **Thaumcraft 4: Reforged** для современных версий Minecraft и NeoForge.

Основной исходный трек репозитория — **Minecraft 26.3** (`main`). Релиз **[2.0.0 для NeoForge 26.3](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F26.3)** уже опубликован.

## Возможности

Порт включает семь аспектов грехов, осколки, зачарования, хамелеоновы инструменты, материалы и блоки скверны, Mob Crystal, Wrath Cage, жезлы и их компоненты, Blink Focus, рецепты и исследования.

Проект сохраняет оригинальные игровые данные и поведение там, где это позволяет современный API. **Полный паритет с оригиналом не заявляется**: оставшиеся расхождения и приближения перечислены в [fidelity ledger](docs/fidelity.md), а визуальные и игровые проверки — в [verification status](docs/verification.md).

## Версии и релизы

| Minecraft | Канал исходников | Релиз |
| --- | --- | --- |
| 26.3 | `main` | [2.0.0 / rolling release](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F26.3) |
| 26.1.2 | `mc/26.1.2` | [Rolling release](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F26.1.2) |
| 1.21.1 | `1.21.1` | [Rolling release](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F1.21.1) |

Это **отдельные build/release tracks**, а не один общий бинарник. Архитектура 2.0.0 отделяет общие модели и переносимые правила от TC4-specific интеграции, но сама по себе не означает совместимость между версиями Minecraft или наличие второго host-адаптера.

## Установка

Для основного релиза нужны:

- Minecraft **26.3**;
- NeoForge **26.3.0.57-beta** или совместимая более новая сборка 26.3;
- [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) **1.1.2 или новее**.

Скачайте JAR из [Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases) и поместите его в `mods/` вместе с Thaumcraft. Для установки опубликованного JAR **не нужно** клонировать исходники хоста, запускать Gradle или выполнять `prepare_host.py`.

В каждом Minecraft-канале используется rolling release под тегом `release/<версия>`: ссылка на конкретный канал остаётся постоянной, а JAR обновляется после успешной сборки и тестов.

## Использование

В Thaumonomicon категория **FORBIDDEN** открывается после исследования «Осколки Нижнего мира». Хамелеоновы инструменты переключают наборы зачарований по **Shift + ПКМ**.

Настройки в `config/forbiddenmagic-startup.toml`:

- `no_lust = true` — отключает аспект Luxuria и связанные с Lust рецепты;
- `silverfish_emeralds = false` — отключает выпадение изумрудных самородков с чешуйниц;
- `emerald_transmutation = false` — отключает исследование и алхимическую трансмутацию самородков.

## Сборка из исходников

Требуются **Git**, **Python 3.12+** и **JDK 25**. Gradle устанавливается через Wrapper (в репозитории закреплён Gradle 9.2.1).

Исходники Thaumcraft не включены в этот репозиторий. Хост имеет отдельную лицензию All Rights Reserved; скрипт подготовки получает закреплённый исходный коммит из `host.sha`, создаёт локальную копию `host/` и применяет патчи из `patches/`.

Linux/macOS:

```bash
git clone https://github.com/MxxFoxx2/forbidden-magic-reforged.git
cd forbidden-magic-reforged

python3 tools/prepare_host.py
python3 tools/prepare_host.py --verify

(cd host && ./gradlew build)
./gradlew build
python3 tools/check_provenance.py
python3 tools/gen_resources.py

./gradlew runGameTestServer
python3 tools/set_startup_config.py no_lust=true
./gradlew runGameTestServer
```

Windows:

```powershell
python tools/prepare_host.py
python tools/prepare_host.py --verify
Set-Location host
.\gradlew.bat build
Set-Location ..
.\gradlew.bat build
python tools/check_provenance.py
python tools/gen_resources.py
.\gradlew.bat runGameTestServer
python tools/set_startup_config.py no_lust=true
.\gradlew.bat runGameTestServer
```

Не редактируйте `host/` вручную: следующая подготовка пересоздаёт эту папку. Изменения хоста храните в `patches/`; после редактирования подготовленной копии экспортируйте их командой `python3 tools/prepare_host.py --export` (на Windows — `python`).

Подробные правила разработки и проверки находятся в [AGENTS.md](AGENTS.md), актуальные результаты — в [docs/verification.md](docs/verification.md).

## CI/CD

Основной workflow выполняет проверку границы host-agnostic API, подготовку закреплённого хоста, сборку хоста и аддона, проверку происхождения JAR, проверку актуальности сгенерированных ресурсов и GameTests в обоих режимах `no_lust`.

Gradle использует build cache и configuration cache. В GitHub Actions применяется официальный `gradle/actions/setup-gradle`; второй независимый механизм кэширования Gradle User Home не подключается. Документационные изменения не запускают тяжёлую сборку и не публикуют новый релиз.

## Лицензии и благодарности

- **SpitefulFox** — оригинальный [Forbidden Magic](https://github.com/SpitefulFox/ForbiddenMagic) 0.574 (WTFPL).
- **Azanor** — Thaumcraft 4.
- **notlikethiss** — [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) (All Rights Reserved).
- **MxxFoxx2** — этот порт (MIT).

Лицензии оригинала и хоста остаются отдельными. Не включайте код или ресурсы хоста в распространяемый JAR аддона. Подробности — в `LICENSE` и `NOTICE`.
