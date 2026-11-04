# Forbidden Magic: Reforged

Порт **Forbidden Magic 0.574** (Minecraft 1.7.10, автор SpitefulFox) на **Thaumcraft 4: Reforged** для **Minecraft 26.1.2** / **NeoForge 26.1.2.122**.

| Minecraft | NeoForge | Thaumcraft 4: Reforged | Версия мода | Релиз |
| --- | --- | --- | --- | --- |
| **26.3** | 26.3.0.57-beta | 1.1.2 | `0.574-26.3-port.0.1.0` | [`release/26.3`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release/26.3) |
| **26.1.2** | 26.1.2.122 | 1.1.2 | `0.574-26.1.2-port.0.1.0` | [`mc/26.1.2`](https://github.com/MxxFoxx2/forbidden-magic-reforged/tree/mc/26.1.2) |
| **1.21.1** | 21.1.256 | 1.1.2 | `0.574-1.21.1-port.0.1.0` | [`1.21.1`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/1.21.1) |

> Для Minecraft **1.21.1** требуется [Curios 9.5.1+1.21.1](https://www.curseforge.com/minecraft/mc-mods/curios) (включён в runtime classpath для GameTest).
> Общего бинарника между версиями Minecraft нет — каждая ветка собирается и выпускается отдельно.

---

## Установка

1. Установите **Minecraft 26.1.2** с **NeoForge 26.1.2.122**
2. Установите **Thaumcraft 4: Reforged 1.1.2** (обязательно)
3. Для 1.21.1: также установите **Curios 9.5.1+** (включён в runtime для GameTest)
4. Положите `forbidden-magic-<version>-<mc>.jar` в папку `mods/`
5. Вкладка **FORBIDDEN** появится в Тауманомиконе (нажмите **Shift**, чтобы раскрыть скрытые записи)

---

## Конфигурация

Файл конфига: `config/forbiddenmagic.toml` (создаётся при первом запуске)

| Опция | По умолчанию | Описание |
| --- | --- | --- |
| `enchantments_enabled` | `true` | Включить зачарования Гнева/Зависти/Жадности |
| `greedy_enchantment_enabled` | `true` | Включить зачарование Капиталист (Жадность) |
| `lust_disabled` | `false` | Отключить контент Похоти (Ошейник боли, рецепт набольника, осколок похоти) |
| `emerald_transmutation` | `true` | Включить рецепты изумруд ⇄ фрагмент |
| `wrath_cage` | `true` | Включить блок Клетка гнева и Кристалл моба |

> Опция `lust_disabled` — это **гейт** — она полностью удаляет контент, связанный с Похотью. Переключение через `python tools/set_startup_config.py no_lust=true|false`. Финальное состояние **обязательно `false`** для релиза.

---

## Сборка и разработка

### Требования
- **Java 25** (Java 21 для ветки 1.21.1)
- **Python 3** (для подготовки хоста, генерации ресурсов, валидации)
- Gradle wrapper включён (`./gradlew`)

### Команды
```bash
# Подготовка хоста (исходники Thaumcraft 4: Reforged + патчи)
python tools/prepare_host.py          # собрать host/
python tools/prepare_host.py --verify # только верификация

# Сборка
./gradlew build                       # собирает и хост, и аддон (composite build)

# Запуск
./gradlew runClient                   # дев-клиент
./gradlew runServer                   # дев-сервер
./gradlew runGameTestServer           # автоматизированные GameTest (headless)

# Валидация
python tools/check_host_agnostic_api.py  # проверить границы host-agnostic
python tools/check_provenance.py         # проверить происхождение JAR
python tools/gen_resources.py            # сгенерировать ресурсы (2 раза, чистый diff)
```

### Definition of Done (для контрибьюторов)
1. `python tools/prepare_host.py --verify` → `host matches sha + 2 patch commit(s)`
2. `./gradlew build` успешно
3. `./gradlew runGameTestServer` зелёный с `no_lust=false` И `no_lust=true` (потом вернуть `false`)
4. `python tools/check_host_agnostic_api.py` и `python tools/check_provenance.py` зелёные
5. Если трогали `data/` или `tools/` генераторы → `gen_resources.py` 2 раза, чистый diff `src/generated/`
6. Любое реальное отклонение от оригинала → задокументировано в `docs/fidelity.md` (не фикс теста)
7. Коммит по Conventional Commits (`feat:`, `fix:`, `docs:` и т.д.), без co-author строк

---

## Файдлити и верификация

Порт нацелен на **1:1 парность логики, чисел, рецептов и текстов** с оригинальным Forbidden Magic 0.574, в рамках возможностей современного API.

- **Fidelity Ledger**: [docs/fidelity.md](docs/fidelity.md) — каждое известное отклонение, компромисс и пробел в парности
- **Verification Report**: [docs/verification.md](docs/verification.md) — результаты GameTest, статус CI
- **Roadmap**: [docs/roadmap.md](docs/roadmap.md) — текущие приоритеты и блокеры

> **Визуальная и геймплейная парность подтверждается только через `runClient` и реальную игру.** Зелёная компиляция и GameTest — необходимо, но не достаточно.

---

## Структура проекта

```
src/main/java/dev/visfox/forbiddenreforged/
├── core/
│   ├── aspect/      # Определения аспектов (host-neutral)
│   ├── model/       # ContentId, HostContent (host-neutral)
│   ├── recipe/      # Определения рецептов (host-neutral)
│   ├── research/    # Определения исследований (host-neutral)
│   ├── wand/        # Поведения жезлов (Hellfire, Blink Focus)
│   └── warp/        # Абстракция доступа к варпу
├── integration/
│   ├── api/         # HostAdapter, HostProfile, HostAdapterRegistry
│   ├── runtime/     # 15 capability интерфейсов (HostXxxServices)
│   └── tc4/         # Реализация Thaumcraft 4: Reforged
├── client/          # Клиентский код (рендереры, креативные вкладки)
├── test/            # GameTest реализации (компилируются в основной jar)
├── FmBlocks.java    # Регистрация блоков
├── FmItems.java     # Регистрация предметов
├── FmResearch.java  # Регистрация исследований
├── FmRecipes.java   # Регистрация рецептов
└── ForbiddenMagic.java  # Главный класс мода (composition root)
```

---

## Авторы и лицензия

| Роль | Автор |
| --- | --- |
| **Оригинальный Forbidden Magic 0.574** | [SpitefulFox](https://github.com/SpitefulFox/ForbiddenMagic) (WTFPL) |
| **Thaumcraft 4** | Azanor |
| **Thaumcraft 4: Reforged** | [notlikethiss](https://github.com/notlikethiss/thaumcraft-4-reforged) |
| **Forbidden Magic: Reforged** | [MxxFoxx2](https://github.com/MxxFoxx2/forbidden-magic-reforged) (MIT) |

- Логика, числа, рецепты, тексты и ассеты оригинала портируются 1:1 там, где это позволяет современный API.
- Ассеты Thaumcraft принадлежат Azanor. Этот репозиторий содержит портированную логику и ассеты и **не предназначен для перераспространения** за пределами личного использования.
- См. [LICENSE](LICENSE) и [NOTICE](NOTICE) для полных условий.

---

## Ссылки

- **Релизы**: [GitHub Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases)
- **Issues**: [GitHub Issues](https://github.com/MxxFoxx2/forbidden-magic-reforged/issues)
- **Thaumcraft 4: Reforged**: [GitHub](https://github.com/notlikethiss/thaumcraft-4-reforged)
- **Оригинальный Forbidden Magic**: [GitHub](https://github.com/SpitefulFox/ForbiddenMagic)