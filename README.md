# Forbidden Magic: Reforged

> Ветка `port/thaumaturge` — отдельная сборка под Thaumaturge 1.0.3 (Minecraft 26.1.2). Она покрывает первый рабочий срез: компиляция и проверка данных проходят, `runGameTestServer` и `runClient` на этом канале ещё не запускались. Актуальный статус, замены и отличия описаны в [docs/thaumaturge.md](docs/thaumaturge.md). Текст ниже относится к каналу `26.1.2` на Thaumcraft 4: Reforged.

Порт Forbidden Magic 0.574 (Minecraft 1.7.10) для Thaumcraft 4: Reforged на Minecraft 26.1.2. Перенесённый контент включает семь аспектов грехов, осколки, зачарования, хамелеоновы инструменты, материалы скверны, Mob Crystal и Wrath Cage.

Полный паритет с оригиналом пока не подтверждён: сборка и GameTests зелёные, визуальные и полноценные игровые проверки ещё нужны.

## Установка

Требуются: Minecraft 26.1.2, NeoForge 26.1.2.122 или новее и [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) 1.1.2+. Скачайте JAR из [Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases) и поместите в `mods/` вместе с хост-модом.

Эта ветка — канал `26.1.2` (rolling-релиз под тегом `release/26.1.2`). Основной канал — `26.3` в ветке `main`; для Minecraft 1.21.1 есть отдельный релиз `release/1.21.1`.

## Использование

В Thaumonomicon категория **FORBIDDEN** открывается после исследования «Осколки Нижнего мира». Хамелеоновы инструменты переключают наборы зачарований по **Shift + ПКМ**.

Настройки в `config/forbiddenmagic-startup.toml`:

- `no_lust = true` — отключает аспект Luxuria и связанные с Lust рецепты.
- `silverfish_emeralds = false` — отключает выпадение изумрудных самородков с чешуйниц.
- `emerald_transmutation = false` — отключает исследование и алхимическую трансмутацию самородков.

## Сборка

Нужны Git, Python 3.11+ и JDK 25. Хост не включён в репозиторий из-за лицензии All Rights Reserved — `tools/prepare_host.py` клонирует его сам (в sibling-папку `../thaumcraft-4-reforged`, если её ещё нет), создаст `host/` по `host.sha` и применит патчи:

```sh
git clone https://github.com/MxxFoxx2/forbidden-magic-reforged.git
cd forbidden-magic-reforged
python3 tools/prepare_host.py
(cd host && ./gradlew build --no-daemon)
./gradlew build --no-daemon
./gradlew runGameTestServer --no-daemon
```

На Windows используйте `python` и `gradlew.bat`. Composite build подключает `host/` автоматически; отдельный `publish` не требуется. Не редактируйте `host/` вручную — скрипт пересоздаёт его заново. Правки хоста живут в `patches/`: после изменений в `host/` их пересобирает `python3 tools/prepare_host.py --export`.

## Credits

- **SpitefulFox** — оригинальный [Forbidden Magic](https://github.com/SpitefulFox/ForbiddenMagic) 0.574 (WTFPL).
- **Azanor** — Thaumcraft 4.
- **notlikethiss** — [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) (All Rights Reserved).
- **MxxFoxx2** — этот порт (MIT).

Порт распространяется по MIT; лицензии оригинала и хоста остаются отдельными. Подробности — в `LICENSE` и `NOTICE`.
