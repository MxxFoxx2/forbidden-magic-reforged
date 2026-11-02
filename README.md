# Forbidden Magic: Reforged

Неофициальный порт **Forbidden Magic для современных версий Minecraft** на базе **Thaumcraft 4: Reforged**.

Проект развивается сразу для нескольких версий игры. Для каждой из них ведётся отдельная ветка исходников и выпускается собственная сборка.

## Актуальные сборки

| Minecraft | Ветка исходников | Последний релиз |
| --- | --- | --- |
| 26.3 | `main` | [2.0.0 — NeoForge 26.3](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F26.3) |
| 26.1.2 | `mc/26.1.2` | [1.0.2 — NeoForge 26.1.2](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F26.1.2) |
| 1.21.1 | `1.21.1` | [1.0.47 — NeoForge 1.21.1](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F1.21.1) |

У каждого направления свой релизный канал. Сборки предназначены только для указанной версии Minecraft и не взаимозаменяемы.

## Архитектура

В ветке 26.3 (релиз 2.0.0) общий слой контента и переносимые игровые правила отделены от интеграции с Thaumcraft 4: Reforged. Это не объединяет разные версии Minecraft в один JAR и не означает поддержку других магических хостов.

## Возможности

В моде представлены аспекты грехов, осколки, зачарования, хамелеоновы инструменты, материалы и блоки скверны, Mob Crystal, Wrath Cage, жезлы и их компоненты, Blink Focus, рецепты и исследования.

Проект сохраняет оригинальные игровые данные и поведение там, где это позволяет современный API. Некоторые отличия от оригинала остаются: они перечислены в [fidelity ledger](docs/fidelity.md), а результаты визуальных и игровых проверок — в [verification status](docs/verification.md).

## Установка

Инструкция ниже относится к сборке для **Minecraft 26.3**. Для неё нужны:

- Minecraft **26.3**;
- NeoForge **26.3.0.57-beta** или совместимая более новая сборка 26.3;
- [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) **1.1.2 или новее**.

Скачайте JAR из [Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases) и поместите его в `mods/` вместе с Thaumcraft.

У каждого Minecraft-канала свой тег `release/<версия>`. Ссылка на канал остаётся постоянной, а JAR обновляется после успешной сборки и тестов.

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

GitHub Actions проверяет границу host-agnostic API, подготавливает закреплённую версию Thaumcraft, собирает хост и мод, проверяет происхождение JAR и актуальность сгенерированных ресурсов, а также запускает GameTests с включённым и отключённым Luxuria.

## Лицензии и благодарности

- **SpitefulFox** — оригинальный [Forbidden Magic](https://github.com/SpitefulFox/ForbiddenMagic) 0.574 (WTFPL).
- **Azanor** — Thaumcraft 4.
- **notlikethiss** — [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) (All Rights Reserved).
- **MxxFoxx2** — этот порт (MIT).

Лицензии оригинала и хоста остаются отдельными. Не включайте код или ресурсы хоста в распространяемый JAR аддона. Подробности — в `LICENSE` и `NOTICE`.
