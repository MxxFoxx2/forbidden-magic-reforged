# Forbidden Magic: Reforged — Minecraft 1.21.1

Неофициальный порт Forbidden Magic 0.574 (Minecraft 1.7.10) для Thaumcraft 4: Reforged на Minecraft 1.21.1. Эта ветка развивается отдельно от порта для Minecraft 26.3.

## Статус

- Целевая среда: Minecraft 1.21.1, NeoForge 21.1.x и Java 21.
- Полный паритет с оригиналом не подтверждён. Текущие этапы и оставшиеся задачи описаны в [roadmap](docs/roadmap.md).
- Требуется Thaumcraft 4: Reforged 1.1.2. Эта ветка использует upstream API аспектов аддонов.

## Установка

Проверьте, что загружаемый файл предназначен именно для Minecraft 1.21.1 и NeoForge 21.1.x: сборка для Minecraft 26.3 несовместима. Файлы релизов доступны в [Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases). Если подходящего релиза нет, соберите мод из этой ветки.

Для игры нужны NeoForge, Thaumcraft 4: Reforged 1.1.2 и зависимости хоста, включая Curios. Поместите JAR мода в папку `mods/`.

## Сборка и тесты

Требуются Git, Python 3 и JDK 21.

Клонируйте ветку и репозиторий хоста рядом друг с другом:

```powershell
git clone --branch 1.21.1 https://github.com/MxxFoxx2/forbidden-magic-reforged.git forbidden-magic-reforged-1.21.1
cd forbidden-magic-reforged-1.21.1
git clone https://github.com/notlikethiss/thaumcraft-4-reforged.git ../thaumcraft-4-reforged
python tools/prepare_host.py
.\gradlew.bat build --no-daemon
.\gradlew.bat runGameTestServer --no-daemon
```

На Linux/macOS используйте `python3` и `./gradlew`. Скрипт `tools/prepare_host.py` подготавливает в `host/` закреплённую версию хоста, указанную в `host.sha`. Gradle использует `host/` через composite build; отдельный `publish` не требуется. Не редактируйте `host/` вручную.

При успешной сборке JAR создаётся в `build/libs/forbidden-magic-0.574-1.21.1-port.0.1.0.jar`.

## Авторы и лицензии

- **SpitefulFox** — оригинальный Forbidden Magic 0.574 (WTFPL).
- **notlikethiss** — Thaumcraft 4: Reforged (All Rights Reserved).
- **Azanor** — Thaumcraft 4.2.3.5.
- **MxxFoxx2** — порт (MIT).

Лицензия MIT применяется к оригинальным изменениям порта; лицензии оригинального Forbidden Magic и Thaumcraft 4: Reforged остаются отдельными. См. `LICENSE` и `NOTICE`.
