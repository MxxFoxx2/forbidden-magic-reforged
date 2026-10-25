# Forbidden Magic: Reforged

Порт Forbidden Magic 0.574 (Minecraft 1.7.10) на Thaumcraft 4: Reforged 1.2 для Minecraft 26.3. Переносит семь аспектов грехов, осколки, зачарования, хамелеоновы инструменты, материалы скверны, Mob Crystal и Wrath Cage.

## Статус

- Основной таргет — Minecraft 26.3 / Java 25. Порт на 1.21.1 отложен до завершения основного таргета.
- Паритет не объявлен завершённым. Текущие пробелы и ограничения записаны в [roadmap](docs/roadmap.md), проверенные отличия — в [fidelity ledger](docs/fidelity.md).
- Первая экспериментальная beta опубликована: [0.1.0-beta.1](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/0.1.0-beta.1). Полный паритет ещё не объявлен.
- Новые релизы публикуются из Git-тегов после сборки и обоих наборов GameTests; тесты также запускаются для pull request в `main`. Инструкция: [процесс выпуска](docs/releasing.md).
- [Thaumcraft 4: Reforged #37](https://github.com/notlikethiss/thaumcraft-4-reforged/pull/37) открыт, но ещё не слит; публичный API пока отсутствует в опубликованном upstream-хосте. Beta требует закреплённый и локально пропатченный хост.

## Установка

Публичный jar аддона не заработает со стоковым хостом, пока нужный API не попадёт в опубликованный Thaumcraft 4: Reforged. Хост имеет лицензию All Rights Reserved; его jar не распространяется этим проектом.

## Сборка разработчика

Нужны Git, Python 3, JDK 25 и интернет-доступ к репозиториям. Клонируй хост рядом с этим репозиторием, как ожидает `tools/prepare_host.py`:

```powershell
git clone https://github.com/notlikethiss/thaumcraft-4-reforged.git ../thaumcraft-4-reforged
python tools/prepare_host.py
Push-Location host
.\gradlew.bat build publish --no-daemon
Pop-Location
.\gradlew.bat build --no-daemon
.\gradlew.bat runGameTestServer --no-daemon
```

На Linux/macOS команды Gradle те же по порядку, но используются `./gradlew` и `python3`. `prepare_host.py` создаёт локальную копию хоста и применяет `patches/*.patch`; не редактируй `host/` вручную.

CI выполняет сборку и публикацию хоста в локальный Maven, сборку аддона, проверку происхождения JAR и два прогона GameTest — с включённым и отключённым `no_lust`. Визуальные проверки требуют `runClient`.

Артефакт: `build/libs/forbidden-magic-0.574-26.3-port.0.1.0.jar`.

## Использование

Открой Thaumonomicon — категория **FORBIDDEN** открывается с исследованием «Сетевые осколки». Хамелеоновы инструменты переключают наборы зачарований по Shift + ПКМ.

Конфиг `config/forbiddenmagic-startup.toml`: `no_lust = true` отключает Luxuria и связанные с Lust рецепты; `silverfish_emeralds = false` отключает изумрудные самородки с чешуйниц; `emerald_transmutation = false` отключает исследование и алхимическую трансмутацию самородков.

## Структура

- `src/` — код и ресурсы.
- `tests/` — GameTest.
- `data/` и `tools/` — источники данных и генераторы.
- `patches/` — локальные патчи хоста.
- `docs/fidelity.md`, `docs/roadmap.md`, `docs/verification.md` — паритет, оставшиеся задачи и подтверждение проверок.

## Авторы и лицензии

- **SpitefulFox** — оригинальный Forbidden Magic 0.574 (WTFPL).
- **notlikethiss** — Thaumcraft 4: Reforged (All Rights Reserved).
- **Azanor** — Thaumcraft 4.2.3.5.
- **MxxFoxx2** — порт (MIT).

Код порта распространяется по MIT; оригинал и хост имеют собственные лицензии. Подробности — в `LICENSE` и `NOTICE`.