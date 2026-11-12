# Verification status

## Minecraft 26.3 — быстрый unit-test гейт (локально, 12 ноября 2026)

- Перенос JUnit-гейта с `mc/26.1.2` вертикальным срезом: `gradlew test` проверяет host-neutral каталоги без запуска сервера.
- `build.gradle`: добавлены `mavenCentral()`, JUnit 5 (`junit-bom:5.11.4`) и задача `test` (`useJUnitPlatform`).
- 4 класса в `src/test/java` — **23 теста**: `RecipeCatalogTest` (7, адаптирован под 6-параметровый `Options` без `arsMagicaLoaded`), `ResearchCatalogTest` (6), `AspectCatalogTest` (6), `ContentIdTest` (4).
- Дублирующий GameTest `core_aspect_catalog_is_host_neutral_and_complete` удалён из `src/gameTest/.../AspectGameTests.java`: его host-neutral покрытие перенесено в `AspectCatalogTest`. Из-за этого число required GameTests уменьшилось со 118 до 117.
- CI: шаг «Run unit tests» (`./gradlew test`) добавлен в `.github/workflows/gradle.yml` между сборкой хоста и сборкой аддона.
- Локальные результаты (offline, JDK 25): `gradlew test` — **23/23** passed, 0 failures, 0 errors; `tools/check_host_agnostic_api.py` — Host API boundaries passed (72 Java files).
- `runGameTestServer` с `no_lust=false` — **117/117** required GameTests passed (`luxuria=luxuria`); с `no_lust=true` — **117/117** (`luxuria=null`, `registeredAddon=6`).
- Проверка локальная и headless; CI на этом коммите ещё не запускался. Клиентские визуальные и игровые проверки из разделов ниже остаются в силе.

## Последний успешный CI — 10 ноября 2026

- Проверенный production/runtime commit: [`a7a4f05`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/a7a4f051702bbc227998017f3f118d33c1f4cc91) (bump пина хоста до Thaumcraft 1.1.3).
- [Java CI with Gradle #677](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/42726269063) завершился успешно: host-agnostic boundary check, подготовка и сборка закреплённого Thaumcraft 1.1.3, сборка аддона, artifact provenance, generated-resource freshness и обе конфигурации GameTests.
- С `no_lust=false`: **118/118** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **118/118** required GameTests passed; `luxuria=null`.
- [Release workflow #81](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/42726269083) завершился успешно на том же source SHA: обе конфигурации GameTests, provenance, подготовка assets и публикация rolling release.
- CI остаётся headless: клиентские sparkle/spawn-particle эффекты оригинальной Wrath Cage требуют отдельной визуальной проверки в `runClient`.

## Состояние rolling release

- Текущий rolling release — **2.0.8 (NeoForge 26.3)**, source commit [`a7a4f05`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/a7a4f051702bbc227998017f3f118d33c1f4cc91), JAR [`forbidden-magic-2.0.8-26.3.jar`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/download/release/26.3/forbidden-magic-2.0.8-26.3.jar). Release metadata подтверждает source SHA `a7a4f051702bbc227998017f3f118d33c1f4cc91`.

## Локальная проверка bump хоста 1.1.3 — 10 ноября 2026

Локальный прогон на рабочем дереве поверх `main` (`1b676737`), до коммита:

- Пин передвинут `6047676` → `99499a8` (вершина upstream `main`, `mod_version=1.1.3`); `tools/check_host_pin.py` — патч `0001` применяется, дрейф upstream — 0 коммитов.
- `tools/prepare_host.py` + `--verify` — `host matches sha + 1 patch commit(s)`. Локальный `main` соседнего upstream-репозитория отстаёт от GitHub, и пин существовал там только в detached HEAD: объект догружен в `host/` разовым `git fetch origin HEAD` (соседний репозиторий не изменялся).
- Сборка патченного хоста — BUILD SUCCESSFUL (4m 15s); сборка аддона — BUILD SUCCESSFUL (3m 48s).
- `tools/check_provenance.py` — provenance ok: 2 jar(s).
- `tools/gen_resources.py` + `git diff --exit-code -- src/generated` — чисто.
- `tools/check_host_agnostic_api.py` — Host API boundaries passed (72 Java files checked); `python -m unittest discover -s tests/python` — 15 tests OK.
- `runGameTestServer` с `no_lust=false` — **118/118** required GameTests passed; с `no_lust=true` — **118/118** (`luxuria=disabled-by-config`); конфигурация возвращена в `false`.
- Smelting-capability остаётся спящей на 1.1.3: в логе `Host exposes no smelting-bonus API; the emerald bonus stays inactive`; синтетический API-хот подтверждает пробуждение адаптера при появлении метода регистрации.

Найдено и исправлено по ходу bump:

1. Upstream-коммит `d72680e` («fix: remove gametests», вошёл в 1.1.3) удалил тестовые структуры хоста, включая `thaumcraft:test/platform` — все 118 GameTests падали на размещении структуры, ещё до проверки поведения. Тесты переведены на собственную фикстуру `forbiddenmagic:test/platform` (`src/gameTest/resources/data/forbiddenmagic/structure/test/platform.nbt`: 7×7×7, каменный пол y=0, 49 блоков; сгенерирована нами по снятым размерам, не скопирована из хоста). Фикстура живёт в gameTest source set и в релизный JAR не попадает.

## Статус паритета

Core parity 26.3 **не объявлен завершённым**: остаются ручные визуальные и игровые проверки (список — в [roadmap](roadmap.md)). Вопрос о распознавании FM-taint blocks остаётся предметом сверки оригинального поведения, а не подтверждённым API-блокером. Отклонения и приближения перечислены в [fidelity ledger](fidelity.md); полный parity интеграций Blood Magic/Botania/Ars Magica 2/CoFH не заявляется.