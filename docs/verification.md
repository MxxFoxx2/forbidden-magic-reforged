# Verification status

## Последний успешный CI — 4 ноября 2026

- Проверенный production/runtime commit: [`64fabaaf`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/64fabaaf5bba2c6adbfeb65c4c2887a07dd964c7); последующие коммиты обновляют только `docs/verification.md`.
- [Java CI with Gradle #658](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41737512523) завершился успешно: host-agnostic boundary check, подготовка и сборка закреплённого хоста, сборка аддона, provenance, generated-resource freshness и обе конфигурации GameTests.
- С `no_lust=false`: **111/111** required GameTests passed.
- С `no_lust=true`: **111/111** required GameTests passed.
- Рефакторинг Host API в [`a1bfcca`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/a1bfcca7bd7a8d64542987d4623229ade32b17f1) убрал дублированную матрицу `HostProfile` из GameTests, сохранив проверки важных ограничений и фактического поведения.
- Рефакторинг Wrath Cage в [`64fabaaf`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/64fabaaf5bba2c6adbfeb65c4c2887a07dd964c7) вынес три канала эссенции и их выбор в `WrathCageEssentiaStore`. NBT-ключи и правила расходования топлива сохранены; spawn loop, мир, синхронизация и TC4 bridge остались в прежних классах.
- [Release workflow #68](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41737512531) завершился успешно: обе конфигурации GameTests, provenance и публикация rolling release.
- Headless CI не заменяет оставшиеся ручные визуальные и игровые проверки клиента, перечисленные ниже.

## Состояние rolling release

- [2.0.3 (NeoForge 26.3)](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release/26.3) опубликован из исходного commit [`64fabaaf`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/64fabaaf5bba2c6adbfeb65c4c2887a07dd964c7); JAR: [`forbidden-magic-2.0.3-26.3.jar`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/download/release/26.3/forbidden-magic-2.0.3-26.3.jar).
- Первый rolling release после перехода на 26.3 — **2.0.1**, собранный из исходного commit [`50a91ed`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/50a91edc39c0ad0edeaf14cf3b50f53c28e0e557); текущий тег `release/26.3` теперь указывает на 2.0.3.
- Старый path filter ошибочно запустил release workflow для test/tooling-only commit [`d540ec7`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/d540ec7278e002b97799716d98129fe9447f8d25), после чего rolling release обновился до 2.0.2. В production Java sources этого commit изменений не было.
- Фильтр release workflow исправлен в [`b7ae154`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/b7ae154e93b413816df0865c60e9c43c4a67162a): test/docs/tooling-only changes не инициируют публикацию JAR. Production Java changes по-прежнему проходят release workflow.

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
- Release target: Minecraft 26.3 / NeoForge 26.3 / Thaumcraft 4: Reforged 1.1.2. No second host adapter or cross-Minecraft-version binary is claimed. Thaumaturge remains an API audit, not a production adapter; manual client and gameplay checks remain separate.

## Проверки вне CI

- `tools/gen_resources.py` запускался дважды с чистым diff после генерации в pre-release проверке.
- `runClient` ещё должен подтвердить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Alchemical Cap и Equivalent Rod прошли headless-проверку, но визуальные модели и подсказки в клиенте не подтверждены.

## Статус паритета

Core parity 26.3 **не объявлен завершённым**: остаются ручные визуальные и игровые проверки. Вопрос о распознавании FM-taint blocks остаётся предметом сверки оригинального поведения, а не подтверждённым API-блокером. Отклонения и приближения перечислены в [fidelity ledger](fidelity.md); полный parity интеграций Blood Magic/Botania/Ars Magica 2/CoFH не заявляется.
