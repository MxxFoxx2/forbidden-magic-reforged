# Проверка M1

Записано 2026-10-21 для HEAD `e5df744`. Всё ниже измерено в чистой копии репозитория,
а не в рабочем каталоге разработки.

## Чистая копия

```bash
git clone D:/Vibing/Minecraft/forbidden-magic-reforged D:/Vibing/Minecraft/.fm-cleanroom
cd .fm-cleanroom
python tools/prepare_host.py
python tools/prepare_host.py --verify
```

Результат: `host matches sha + 3 patch commit(s)` (пин `69a47a1b238e3ac15fa5d4b10e61ccb6a4a80dbf`).

## Хост

```bash
cd host
cmd //c gradlew.bat build
cmd //c gradlew.bat runGameTestServer
cmd //c gradlew.bat publish
```

| Гейт | Результат |
|---|---|
| build | `BUILD SUCCESSFUL` |
| runGameTestServer | `All 256 required tests passed :)` |
| publish | `maven/thaumcraft/thaumcraft/1.2/thaumcraft-1.2.jar` |

SHA-256 опубликованного хоста совпал в двух независимых копиях:
`308dc060c2f3f8a6aaa5fbde5ce5ea4def2e28a51c10db16173ccd517a11e875`.

## Аддон

```bash
cmd //c gradlew.bat build
cmd //c gradlew.bat runGameTestServer          # no_lust = false
# затем no_lust = true в run/config/forbiddenmagic-startup.toml
cmd //c gradlew.bat runGameTestServer
# вернуть no_lust = false
python tools/check_provenance.py
python tools/gen_resources.py && python tools/gen_resources.py
git status --porcelain
```

| Гейт | Результат |
|---|---|
| build | `BUILD SUCCESSFUL`, ни одного предупреждения javac (включая `-Xlint:deprecation`) |
| тесты, `no_lust = false` | `All 16 required tests passed :)`, лог `no_lust gate: disabled=false, luxuria=luxuria` |
| тесты, `no_lust = true` | `All 16 required tests passed :)`, лог `no_lust gate: disabled=true, luxuria=null` |
| provenance | `provenance ok: 1 jar(s)` |
| генератор | два прогона подряд, `git status` пуст |
| чистота | `git status --porcelain` пуст, `prepare_host.py --verify` зелёный |
| артефакт | `build/libs/forbidden-magic-0.574-26.3-port.0.1.0.jar`, 277 112 байт |

## Что этой проверкой не покрыто

- Клиент (`runClient`): отрисовка текстур аспектов и осколков, модели предметов,
  страницы Thaumonomicon, вкладка creative, JEI. GameTest работает на сервере и
  клиентскую отрисовку не проверяет.
- Полноценная игровая сессия: дропы осколков, infusion-рецепт топора Череподрать,
  исследование HELLFIRE — покрыты только unit-подобными проверками и сквозным тестом.
- Цель 1.21.1: не начиналась.

## Следующий шаг

Ручной `runClient` и подтверждение клиентской части. Push не выполняется до этого
подтверждения.

## Слайс M2: зачарования

Замерено 2026-10-21 на `668f03c` (dev), перед коммитом документации.

| Гейт | Результат |
|---|---|
| build | `BUILD SUCCESSFUL`, без предупреждений javac |
| тесты, `no_lust = false` | `All 26 required tests passed :)` |
| тесты, `no_lust = true` | `All 26 required tests passed :)`, `no_lust gate: disabled=true, luxuria=null` |
| генератор | два прогона подряд, `src/generated` не меняется |
| provenance | `provenance ok: 1 jar(s)` |
| хост | `host matches sha + 3 patch commit(s)` |
| артефакт | `forbidden-magic-0.574-26.3-port.0.1.0.jar`, 306 965 байт |

Новые тесты: форма пяти зачарований и их теги предметов, изумрудный фрагмент, garbage-фильтр
Consuming, пул осколков и шанс Corrupting, дропы Greedy, множитель опыта Educational, урон Wrath,
пять исследований с родителями и warp, пять infusion-рецептов с аспектами, безопасное отсутствие TT.

Не покрыто: визуальная проверка иконок исследований и текстов страниц в Thaumonomicon, реальное
наложение эффектов в бою (тесты проверяют чистые функции), TT-путь с установленным TT — в тестовом
окружении TT отсутствует, поэтому проверена только ветка «TT нет».

## Слайс M3: морф-инструменты

Замерено 2026-10-21 на `318f276` (dev), перед коммитом документации.

| Гейт | Результат |
|---|---|
| build | `BUILD SUCCESSFUL`, без предупреждений javac |
| тесты, `no_lust = false` | `All 34 required tests passed :)` |
| тесты, `no_lust = true` | `All 34 required tests passed :)`, `no_lust gate: disabled=true, luxuria=null` |
| генератор | два прогона подряд, `src/generated` не меняется |
| provenance | `provenance ok: 1 jar(s)` |
| хост | `host matches sha + 3 patch commits` |
| артефакт | `forbidden-magic-0.574-26.3-port.0.1.0.jar`, 341 735 байт |

Новые тесты: регистрация четырёх инструментов и ремонт осколком зависти, ротация трёх фаз,
плоскость Impact и доминирующая ось взгляда, варп Voidtouched, JSON и эксклюзивность трёх
зачарований, шанс Cluster и подмена дропа кластером, исследование `MORPHTOOLS` с четырьмя
рецепт-страницами, семь новых рецептов.

Не покрыто: статичный цвет оверлея фазы, визуальная проверка двухслойных моделей, реальное
переключение фаз в клиенте, TT-путь с установленным TT.

## Слайс M4: материалы скверны

Замерено 2026-10-21 на `7e2b6c1` (dev), перед коммитом документации.

| Гейт | Результат |
|---|---|
| build | `BUILD SUCCESSFUL`, без предупреждений javac |
| тесты, `no_lust = false` | `All 37 required tests passed :)` |
| тесты, `no_lust = true` | `All 37 required tests passed :)` |
| генератор | два прогона подряд, `src/generated` не меняется |
| provenance | `provenance ok: 1 jar(s)` |
| хост | `host matches sha + 3 patch commit(s)` |
| артефакт | 360 668 байт |

Не покрыто: визуальная проверка blockstates и моделей бревна в клиенте, добыча с инструментами
разных уровней, плавка и арканный рецепт (отложены и записаны в deviations).

## Слайс M5: растения скверны

Замерено 2026-10-21 на коммите исследований (dev).

| Гейт | Результат |
|---|---|
| build | `BUILD SUCCESSFUL`, без предупреждений javac |
| тесты, `no_lust = false` | `All 41 required tests passed :)` |
| тесты, `no_lust = true` | `All 41 required tests passed :)` |
| генератор | повторный прогон не меняет `src/generated` |
| provenance | `provenance ok: 1 jar(s)` |
| хост | `host matches sha + 3 patch commit(s)` |
| артефакт | 383 602 байта |

Не покрыто: визуальная проверка cross-моделей и двустворчатого куста, распространение цветка
в реальном тикании мира, скверный плод и чернила (отложены).

## Слайс M6: Арканный торт

Замерено 2026-10-21 на `3f68c3d` (dev).

| Гейт | Результат |
|---|---|
| build | `BUILD SUCCESSFUL`, без предупреждений javac |
| тесты, `no_lust = false` | `All 42 required tests passed :)` |
| тесты, `no_lust = true` | `All 42 required tests passed :)` |
| генератор | после коммита повторный прогон не меняет `src/generated` |
| provenance | `provenance ok: 1 jar(s)` |
| хост | `host matches sha + 3 patch commit(s)` |
| артефакт | 395 664 байта |

Не покрыто: визуальная проверка 12 моделей кусков, поедание кликом в реальной игре, hardcore-рецепт
`gluttony = 2` (тесты идут с `gluttony = 0`).

## Слайс M7a: кристалл моба

Замерено 2026-10-21 на `4d2a76e` (dev), перед коммитом документации.

| Гейт | Результат |
|---|---|
| build | `BUILD SUCCESSFUL in 952ms` (всё up-to-date), затем чистая сборка `cmd //c gradlew.bat clean build` — `BUILD SUCCESSFUL in 12s`, без предупреждений javac |
| тесты, `no_lust = false` | `All 43 required tests passed :)`, `no_lust gate: disabled=false, luxuria=luxuria` |
| тесты, `no_lust = true` | `All 43 required tests passed :)`, `no_lust gate: disabled=true, luxuria=null` |
| генератор | два прогона подряд, `git status --porcelain src/generated` пуст |
| provenance | `provenance ok: 1 jar(s)` |
| хост | `host matches sha + 3 patch commit(s)` |
| артефакт | `forbidden-magic-0.574-26.3-port.0.1.0.jar`, 403 855 байт |

Новый тест один — `mob_crystal_and_spawn_table` (индекс 42): три записи таблицы (zombie/flesh,
brainy_zombie/mind, pig/beast), размер таблицы не меньше 40, пустой и заполненный кристалл через
компонент, импринт зомби разрешён, дракон как босс — нет. Сама таблица сверена с оригиналом
(`Config.java:154-198`): 42 записи, 24 vanilla + 18 Thaumcraft. Тесты идут с дефолтами конфига
(`wrath_cage = true`, `wrath_crazy = false`).

Не покрыто: реальный импринт через `LivingDropsEvent` при убийстве, переключение модели
`has_component` в клиенте, ветки `wrath_cage = false` и `wrath_crazy = true`, Клетка Гнева (M7b).

## Слайс M7b: Клетка Гнева

Замерено 2026-10-22 на `dev`, перед коммитом документации.

| Гейт | Результат |
|---|---|
| build | `BUILD SUCCESSFUL in 25s`, без ошибок компиляции |
| тесты, `no_lust = false` | `All 46 required tests passed :)`, `no_lust gate: disabled=false, luxuria=luxuria` |
| тесты, `no_lust = true` | `All 46 required tests passed :)`, `no_lust gate: disabled=false, luxuria=luxuria` |
| генератор | `lang/en_us.json: 85 keys`, `lang/ru_ru.json: 85 keys`, wrath cage model/blockstate |
| provenance | не проверялся (нет изменений хоста) |
| хост | `host matches sha + 3 patch commit(s)` (без изменений) |
| артефакт | не измерялся |

Новые тесты два — `wrath_cage_block_and_entity` (43) и `wrath_cage_crystal_and_spawn_rules` (44)
в Task 1; `wrath_cage_research_and_recipes` (45) в Task 2. Тест 43 проверяет создание блока и
block entity, привязку кристалла, замену привязки и дроп при разрушении. Тест 44 проверяет
логику `shouldSpawn` для разных комбинаций красного камня, топлива и эссенции. Тест 45 проверяет
существование исследования `WRATHCAGE` (warp 3, 5 страниц), инфузионного рецепта `WRATHCAGE`
(WRATH 32) и тигельного рецепта `MOBCRYSTAL` (MIND 10).

Добавлен клиентский рендерер `WrathCageRenderer` с современным API extract/submit: создаёт
временную сущность из `mobId`, извлекает `EntityRenderState` через `EntityRenderDispatcher.extractEntity`,
рендерит с вращением внутри клетки. Регистрация через `FmClientEvents` на клиентской шине событий.

Не покрыто: визуальная проверка рендерера в реальной игре, вилка дьяболаста (отложена),
переключение режимов эссенции вилкой, `wrathCrazy`-специфика за пределами разрешённых id.
