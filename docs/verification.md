# Verification status

## Последний успешный runtime CI

- Проверенный коммит: [`74a6dcd`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/74a6dcd0e31d7c222ce2198475658e997d295e6e).
- [Java CI with Gradle #40566141998](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40566141998) завершился успешно 27 октября 2026 года.
- Хост закреплён на upstream `604767636dc318a6d81f1abb8a8a2d3e5ec3298a` (1.1.2); host patch подготовлен через `tools/prepare_host.py`, сборка использует Gradle composite build вместо локального Maven publish.
- Сборка хоста, сборка аддона и проверка provenance завершились успешно.
- С `no_lust=false`: **97/97** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **97/97** required GameTests passed; `luxuria=null`.
- Опубликованный релиз: rolling [`26.3`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/26.3). Сборка и тесты headless не заменяют ручные проверки клиента и реального геймплея.

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
