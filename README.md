# Forbidden Magic: Reforged

Неофициальный порт **Forbidden Magic для современных версий Minecraft** на базе **Thaumcraft 4: Reforged**.

Проект переносит контент и игровые механики оригинального Forbidden Magic в современные сборки Minecraft и NeoForge. Для каждой поддерживаемой версии игры ведётся отдельная ветка исходников и выпускается отдельный JAR.

## Актуальные сборки

| Minecraft | NeoForge | Thaumcraft 4: Reforged | Ветка | Последний релиз |
| --- | --- | --- | --- | --- |
| **26.3** | 26.3.0.57-beta или более новая совместимая сборка 26.3 | 1.1.2 или новее | `main` | [2.0.0](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F26.3) |
| **26.1.2** | 26.1.2.122 | 1.1.2 | `mc/26.1.2` | [1.0.2](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F26.1.2) |
| **1.21.1** | 21.1.256 | 1.1.2 | `1.21.1` | [1.0.47](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F1.21.1) |

Для сборки под **Minecraft 1.21.1** также требуется **Curios 9.5.1+1.21.1**.

Каждая строка — самостоятельный релизный трек со своими исходниками, зависимостями и JAR. Используйте сборку только для указанной версии Minecraft: файлы разных треков не взаимозаменяемы.

## Архитектура

Начиная с релиза **2.0.0 для Minecraft 26.3**, проект использует архитектуру *host-agnostic core*. Её задача — отделить общие определения контента и переносимые игровые правила от API конкретной магической системы.

Это не попытка отвязать мод от Minecraft. Общие runtime-контракты по-прежнему могут использовать типы Minecraft и NeoForge; изолируется именно зависимость от API Thaumcraft или другого magic-host.

### Основные слои

| Слой | Ответственность |
| --- | --- |
| `core/` | Семантические идентификаторы `ContentId`, модели и каталоги аспектов, рецептов и исследований, общие игровые правила |
| `integration/api/` | Контракт `HostAdapter`, профиль возможностей `HostProfile` и реестр выбранного адаптера |
| `integration/runtime/` | Небольшие контракты отдельных runtime-возможностей: сущности, жезлы, варп, скверна, эссенция, предметы, аксессуары и другие |
| `integration/tc4/` | Реализация этих контрактов через реестры, типы, API и поведение Thaumcraft 4: Reforged |

Общие каталоги оперируют семантическими идентификаторами и описаниями контента, а не объектами реестров Thaumcraft. Когда требуется взаимодействие с хостом, общий код обращается к узкому runtime-контракту. Конкретный адаптер преобразует этот запрос в операции API хоста.

При запуске класс `ForbiddenMagic` устанавливает `Tc4HostAdapter` как выбранную реализацию до регистрации общих каталогов и контента. Остальные общие классы не должны напрямую выбирать или импортировать конкретный адаптер.

### Возможности и границы

Поддержка возможностей хоста описывается в `HostProfile` отдельными статусами:

- `NATIVE` — нативная поддержка;
- `ADAPTED` — поддержка через адаптацию;
- `PARTIAL` — частичная поддержка;
- `UNSUPPORTED` — возможность не поддерживается.

Это не декларация будущей совместимости: статус должен соответствовать фактической реализации адаптера и проверкам. Например, неподдерживаемые механики не маскируются приближённой реализацией.

**Что архитектура 2.0.0 означает на практике:**

- общие каталоги и вынесенные игровые правила не зависят от API Thaumcraft;
- интеграция с хостом сосредоточена в адаптере и его специализированных компонентах;
- новые host-specific операции добавляются через отдельные capability-контракты, а не через единый универсальный сервис;
- граница зависимостей проверяется автоматически, а поведение адаптера дополнительно проверяется GameTests.

**Чего она пока не означает:**

- Minecraft и NeoForge не вынесены из runtime и не заменяются абстрактным игровым движком;
- несколько версий Minecraft не объединены в один source set или JAR;
- второй магический хост пока не подключён: в релизе 2.0.0 используется Thaumcraft 4: Reforged;
- аудит совместимости с [Thaumaturge](docs/host-compatibility/thaumaturge-26.1.2.md) — это анализ API и ограничений, а не готовый production-адаптер.

Подробное описание устройства и ограничений находится в [документе архитектуры](docs/architecture/host-agnostic-core.md). Результаты проверки — в [verification report](docs/verification.md).

## Возможности мода

В порт входят аспекты грехов, осколки, зачарования, хамелеоновы инструменты, материалы и блоки скверны, Mob Crystal, Wrath Cage, жезлы и их компоненты, Blink Focus, рецепты и исследования.

Цель проекта — сохранить оригинальные игровые данные и поведение там, где это позволяет современная версия игры и API хоста. **Полный паритет с оригиналом не заявляется**: известные расхождения, адаптации и ограничения фиксируются в [fidelity ledger](docs/fidelity.md).

## Проверки

Релиз 2.0.0 для Minecraft 26.3 прошёл проверку архитектурной границы, подготовку и сборку закреплённой версии Thaumcraft, сборку мода, проверки происхождения артефакта и актуальности сгенерированных ресурсов.

В CI прошли **111/111 обязательных GameTests** с включённым Luxuria и **111/111** с отключённым Luxuria. Это подтверждает проверенные серверные сценарии и адаптерные контракты, но не заменяет ручную проверку клиента и полноценного игрового процесса. Оставшиеся проверки перечислены в [verification report](docs/verification.md).

## Установка

1. Выберите релиз для своей версии Minecraft в таблице выше.
2. Установите соответствующую версию NeoForge и Thaumcraft 4: Reforged.
3. Для Minecraft 1.21.1 дополнительно установите Curios указанной версии.
4. Поместите JAR Forbidden Magic и необходимые зависимости в папку `mods/`.

Не используйте JAR от другой версии Minecraft. Ссылки на релизы ведут на отдельные каналы; каждый из них обновляется независимо.

## Использование

В Thaumonomicon категория **FORBIDDEN** открывается после исследования «Осколки Нижнего мира». Хамелеоновы инструменты переключают наборы зачарований по **Shift + ПКМ**.

Настройки в `config/forbiddenmagic-startup.toml`:

- `no_lust = true` — отключает аспект Luxuria и связанные с Lust рецепты;
- `silverfish_emeralds = false` — отключает выпадение изумрудных самородков с чешуйниц;
- `emerald_transmutation = false` — отключает исследование и алхимическую трансмутацию самородков.

## Сборка из исходников

Инструкции ниже относятся к основной ветке `main` и треку **Minecraft 26.3**.

Требуются **Git**, **Python 3.12+** и **JDK 25**. Gradle устанавливается через Wrapper; в репозитории закреплён Gradle 9.2.1.

Исходники Thaumcraft не включены в этот репозиторий. Хост имеет отдельную лицензию All Rights Reserved. Скрипт подготовки получает закреплённый исходный коммит из `host.sha`, создаёт локальную копию `host/` и применяет патчи из `patches/`.

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
git clone https://github.com/MxxFoxx2/forbidden-magic-reforged.git
Set-Location forbidden-magic-reforged

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

Правила разработки и результаты проверок описаны в [AGENTS.md](AGENTS.md), [документе архитектуры](docs/architecture/host-agnostic-core.md) и [verification report](docs/verification.md).

## Лицензии и благодарности

- **SpitefulFox** — оригинальный [Forbidden Magic](https://github.com/SpitefulFox/ForbiddenMagic) 0.574 (WTFPL).
- **Azanor** — Thaumcraft 4.
- **notlikethiss** — [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) (All Rights Reserved).
- **MxxFoxx2** — этот порт (MIT).

Лицензии оригинала и хоста остаются отдельными. Не включайте код или ресурсы хоста в распространяемый JAR аддона. Подробности — в `LICENSE` и `NOTICE`.
