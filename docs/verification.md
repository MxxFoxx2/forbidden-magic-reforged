# Verification status

## Локальная проверка ветки `refactor/simplify-architecture` — 4 ноября 2026

Локальный прогон на коммите `e30b6e48` (ветка `refactor/simplify-architecture`, ещё не смержена в `main`; CI для неё не запускался):

- `gradlew build` — успешно; в production JAR `forbidden-magic-0.574-26.3-port.0.1.0.jar` 183 класса, ни одного класса из `forbiddenreforged/test/**` или `*GameTests*`.
- `runGameTestServer` с `no_lust=false` — **117/117** required GameTests passed.
- `runGameTestServer` с `no_lust=true` — **117/117** required GameTests passed.
- `tools/check_host_agnostic_api.py` — Host API boundaries passed (70 Java files checked).
- `tools/check_provenance.py` — provenance ok: 2 jar(s).
- `tools/check_host_pin.py` — host pin check passed; оба патча применяются к закреплённому `host.sha`, upstream `origin/main` опережает пин на 23 коммита.
- `pytest tests/python` — 15 passed, 7 subtests passed.
- Новый GameTest-бюстрап: `src/gameTest/java/dev/visfox/forbiddenreforged/test/FmGameTestBootstrap.java`. В Minecraft 26.3 `minecraft:test_function` — статический built-in реестр: `DeferredRegister` по нему не срабатывал, и все 117 функций отсутствовали в рантайме (`Trying to access missing test function`). Рабочее окно — `RegisterEvent` реестра `minecraft:test_function`, который `GameData.postRegisterEvents()` рассылает на mod bus, пока реестр разморожен; `RegisterGameTestsEvent` стреляется уже после заморозки и даёт только реестры окружений и инстансов тестов.

## Последний успешный CI — 4 ноября 2026

- Проверенный production/runtime commit: [`582285d`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/582285d046af2c4d9f94d9b267625253f6cf22b3).
- [Java CI with Gradle #661](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41739354310) завершился успешно: host-agnostic boundary check, подготовка и сборка закреплённого Thaumcraft, сборка аддона, artifact provenance, generated-resource freshness и обе конфигурации GameTests.
- С `no_lust=false`: **114/114** required GameTests passed.
- С `no_lust=true`: **114/114** required GameTests passed.
- Три дополнительных теста проверяют исходное правило заполнения каналов, приоритет расходования эссенции и AABB ограничения количества мобов.
- [Release workflow #73](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41739354306) завершился успешно на том же source SHA: обе конфигурации GameTests, provenance, подготовка assets и публикация rolling release.
- В [2.0.4 (NeoForge 26.3)](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release/26.3) опубликован [`forbidden-magic-2.0.4-26.3.jar`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/download/release/26.3/forbidden-magic-2.0.4-26.3.jar). Release metadata подтверждает source SHA `582285d046af2c4d9f94d9b267625253f6cf22b3`.
- CI остаётся headless: клиентские sparkle/spawn-particle эффекты оригинальной Wrath Cage требуют отдельной визуальной проверки в `runClient`.

## Состояние rolling release

- Текущий rolling release — **2.0.4 (NeoForge 26.3)**, source commit [`582285d`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/582285d046af2c4d9f94d9b267625253f6cf22b3).
- Предыдущая версия **2.0.3** была опубликована из [`64fabaaf`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/64fabaaf5bba2c6adbfeb65c4c2887a07dd964c7).
- Первый rolling release после перехода на 26.3 — **2.0.1**, собранный из [`50a91ed`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/50a91edc39c0ad0edeaf14cf3b50f53c28e0e557); test/tooling-only commit `d540ec7` ранее ошибочно обновил его до 2.0.2.
- Фильтр release workflow исправлен в [`b7ae154`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/b7ae154e93b413816df0865c60e9c43c4a67162a): test/docs/tooling-only changes не инициируют публикацию JAR, production Java changes проходят release workflow.
- Release workflow #71 для промежуточного commit `e45e191` завершился неуспешно, потому что новый test DeferredRegister не был подключён к mod event bus. Исправление в [`6b32d33`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/6b32d3387ee06f05326a12d81701ae8cebcccfa6) подключило новые функции к уже зарегистрированному `EntityGameTests.FUNCTIONS`; итоговые CI #660/#661 и release #73 подтвердили выполнение всех 114 тестов. Неуспешный промежуточный run не является результатом опубликованного 2.0.4.

## Предыдущий runtime CI


## Последний успешный runtime CI

- Проверенный коммит: [`24684ce`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/24684ce2770cf3d62a7a2f76ea75929485b6f29b).
- [Java CI with Gradle #419](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41369810689) завершился успешно 2 ноября 2026 года.
- Хост закреплён на upstream `604767636dc318a6d81f1abb8a8a2d3e5ec3298a` (1.1.2); host patch подготовлен через `tools/prepare_host.py`, сборка использует Gradle composite build вместо локального Maven publish.
- Сборка хоста, сборка аддона, проверка provenance и generated-resource freshness завершились успешно.
- С `no_lust=false`: **111/111** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **111/111** required GameTests passed; `luxuria=null`.
- Опубликован мажорный релиз [`2.0.0 (NeoForge 26.3)`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release/26.3); JAR: [`forbidden-magic-2.0.0-26.3.jar`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/download/release/26.3/forbidden-magic-2.0.0-26.3.jar). Сборка и тесты headless не заменяют ручные проверки клиента и реального геймплея.

## Host-agnostic architecture — 2.0.0

- Merged into `main` through [PR #63](https://github.com/MxxFoxx2/forbidden-magic-reforged/pull/63) on 2 November 2026; merge commit: [`542aab9`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/542aab9f619e3b8fe043d645069c72d580b07c09).
- Final pre-merge PR head: [`66a89ee`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/66a89ee59e620941ef17fc1877c5923d14465ef5); [Java CI with Gradle #416](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41369567530) completed successfully on 2 November 2026.
- The exact final PR-head run passed the host-agnostic boundary check, pinned host preparation and build, mod build, artifact provenance, and generated-resource freshness.
- With `no_lust=false`: **111/111** required GameTests passed (`luxuria=luxuria`).
- With `no_lust=true`: **111/111** required GameTests passed (`luxuria=null`).
- The architecture refactor is complete within its declared scope: shared content models/catalogs and extracted gameplay rules do not depend on TC4 APIs; host-specific behavior is behind the adapter and capability contracts.
- Release target: Minecraft 26.3 / NeoForge 26.3 / Thaumcraft 4: Reforged 1.1.2. No second host adapter or cross-Minecraft-version binary is claimed. manual client and gameplay checks remain separate.

## Проверки вне CI

- `tools/gen_resources.py` запускался дважды с чистым diff после генерации в pre-release проверке.
- `runClient` ещё должен подтвердить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Alchemical Cap и Equivalent Rod прошли headless-проверку, но визуальные модели и подсказки в клиенте не подтверждены.

## Статус паритета

Core parity 26.3 **не объявлен завершённым**: остаются ручные визуальные и игровые проверки. Вопрос о распознавании FM-taint blocks остаётся предметом сверки оригинального поведения, а не подтверждённым API-блокером. Отклонения и приближения перечислены в [fidelity ledger](fidelity.md); полный parity интеграций Blood Magic/Botania/Ars Magica 2/CoFH не заявляется.
