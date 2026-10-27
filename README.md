# Forbidden Magic: Reforged

Порт Forbidden Magic 0.574 (Minecraft 1.7.10) для Thaumcraft 4: Reforged на Minecraft 26.3. Перенесённый контент включает семь аспектов грехов, осколки, зачарования, хамелеоновы инструменты, материалы скверны, Mob Crystal и Wrath Cage.

Полный паритет с оригиналом пока не подтверждён: сборка и GameTests зелёные, визуальные и полноценные игровые проверки ещё нужны.

## Установка

Требуются: Minecraft 26.3, NeoForge 26.3.0.57-beta или новее и [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) 1.1.2+. Скачайте JAR из [Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases) и поместите в `mods/` вместе с хост-модом.

Основной канал — релиз `26.3`; для Minecraft 1.21.1 есть отдельный релиз.

## Использование

В Thaumonomicon категория **FORBIDDEN** открывается после исследования «Осколки Нижнего мира». Хамелеоновы инструменты переключают наборы зачарований по **Shift + ПКМ**.

Настройки в `config/forbiddenmagic-startup.toml`:

- `no_lust = true` — отключает аспект Luxuria и связанные с Lust рецепты.
- `silverfish_emeralds = false` — отключает выпадение изумрудных самородков с чешуйниц.
- `emerald_transmutation = false` — отключает исследование и алхимическую трансмутацию самородков.

## Сборка

Нужны Git, Python 3 и JDK 25. Хост не включён в репозиторий из-за лицензии All Rights Reserved — клонируйте его рядом с этой копией, затем `tools/prepare_host.py` создаст `host/` по `host.sha` и применит патчи:

```sh
git clone https://github.com/MxxFoxx2/forbidden-magic-reforged.git
git clone https://github.com/notlikethiss/thaumcraft-4-reforged.git
cd forbidden-magic-reforged
python3 tools/prepare_host.py
(cd host && ./gradlew build --no-daemon)
./gradlew build --no-daemon
./gradlew runGameTestServer --no-daemon
```

На Windows используйте `python` и `gradlew.bat`. Composite build подключает `host/` автоматически; отдельный `publish` не требуется. Не редактируйте `host/` вручную — скрипт пересоздаёт его заново.

## Credits

- **SpitefulFox** — оригинальный [Forbidden Magic](https://github.com/SpitefulFox/ForbiddenMagic) 0.574 (WTFPL).
- **Azanor** — Thaumcraft 4.
- **notlikethiss** — [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) (All Rights Reserved).
- **MxxFoxx2** — этот порт (MIT).

Порт распространяется по MIT; лицензии оригинала и хоста остаются отдельными. Подробности — в `LICENSE` и `NOTICE`.
