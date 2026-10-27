# Forbidden Magic: Reforged

Порт Forbidden Magic 0.574 (Minecraft 1.7.10) для Thaumcraft 4: Reforged на Minecraft 26.3. Перенесённый контент включает семь аспектов грехов, осколки, зачарования, хамелеоновы инструменты, материалы скверны, Mob Crystal и Wrath Cage.

## Статус

- Целевая версия — Minecraft 26.3 / Java 25. Полный паритет с оригиналом пока не подтверждён.
- Реализованные механики и отличия: [fidelity ledger](docs/fidelity.md). Оставшиеся задачи: [roadmap](docs/roadmap.md). Результаты проверок: [verification](docs/verification.md).
- Актуальная сборка — rolling-релиз [`26.3`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/26.3). CI успешно прошёл сборку и 95/95 GameTests в обоих режимах `no_lust`; визуальные проверки клиента и полноценные игровые проверки ещё нужны.
- Thaumcraft 4: Reforged 1.1.2+ содержит upstream API аспектов аддонов. Для регистрации портала Нижнего мира как отдельной цели сканирования блока нужен локальный [host patch](patches/0001-block-aspect-targets.patch).

## Установка

Скачайте мод из [Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases) и поместите JAR в `mods/` вместе с Thaumcraft 4: Reforged 1.1.2+ для Minecraft 26.3.

Хост не включён в этот репозиторий из-за лицензии All Rights Reserved.

## Сборка

Нужны Git, Python 3 и JDK 25. Клонируйте хост рядом с этим репозиторием:

```powershell
git clone https://github.com/notlikethiss/thaumcraft-4-reforged.git ../thaumcraft-4-reforged
python tools/prepare_host.py
Push-Location host
.\gradlew.bat build --no-daemon
Pop-Location
.\gradlew.bat build --no-daemon
.\gradlew.bat runGameTestServer --no-daemon
```

На Linux/macOS используйте `./gradlew` и `python3`. Скрипт `tools/prepare_host.py` создаёт закреплённую копию хоста в `host/` и применяет патчи. Composite build использует её автоматически; отдельный `publish` не требуется. Не редактируйте `host/` вручную.

## Использование

В Thaumonomicon категория **FORBIDDEN** открывается после исследования «Осколки Нижнего мира». Хамелеоновы инструменты переключают наборы зачарований по **Shift + ПКМ**.

Настройки в `config/forbiddenmagic-startup.toml`:

- `no_lust = true` — отключает аспект Luxuria и связанные с Lust рецепты.
- `silverfish_emeralds = false` — отключает выпадение изумрудных самородков с чешуйниц.
- `emerald_transmutation = false` — отключает исследование и алхимическую трансмутацию самородков.

## Авторы и лицензии

- **SpitefulFox** — оригинальный Forbidden Magic 0.574 (WTFPL).
- **notlikethiss** — Thaumcraft 4: Reforged (All Rights Reserved).
- **Azanor** — Thaumcraft 4.2.3.5.
- **MxxFoxx2** — порт (MIT).

Порт распространяется по MIT; лицензии оригинала и хоста остаются отдельными. Подробности — в `LICENSE` и `NOTICE`.
