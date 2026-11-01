# Verification status

## Последний успешный runtime CI

- Проверенный коммит: [`74a6dcd`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/74a6dcd0e31d7c222ce2198475658e997d295e6e).
- [Java CI with Gradle #40566141998](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40566141998) завершился успешно 27 октября 2026 года.
- Хост закреплён на upstream `604767636dc318a6d81f1abb8a8a2d3e5ec3298a` (1.1.2); host patch подготовлен через `tools/prepare_host.py`, сборка использует Gradle composite build вместо локального Maven publish.
- Сборка хоста, сборка аддона и проверка provenance завершились успешно.
- С `no_lust=false`: **97/97** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **97/97** required GameTests passed; `luxuria=null`.
- Опубликованный релиз: rolling [`26.3`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/26.3). Сборка и тесты headless не заменяют ручные проверки клиента и реального геймплея.

## Host-agnostic architecture branch

- Branch: [`architecture/host-agnostic-core`](https://github.com/MxxFoxx2/forbidden-magic-reforged/tree/architecture/host-agnostic-core).
- Verified commit: [`ddfb1f5`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/ddfb1f51df8c5950e53c9de822d76c739603c7fb).
- [Java CI with Gradle #41311521396](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41311521396) completed successfully on 1 November 2026.
- The branch CI passed the host-agnostic boundary check, pinned host build, mod build, artifact provenance, generated-resource freshness and both `no_lust=false` and `no_lust=true` GameTest runs.
- This result verifies only the isolated architecture branch. It does not change or replace the latest verification of `main`.
- Follow-up [Java CI with Gradle #41312807272](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41312807272) passed on commit [`5cbbb8f`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/5cbbb8f5331c484b639a747f43ee80d9f2c9da30), including the host-free `core/wand` boundary, the neutral wand vis/parts GameTest, and both `no_lust` configurations.

## Проверки вне CI

- `tools/gen_resources.py` запускался дважды с чистым diff после генерации в pre-release проверке.
- `runClient` ещё должен подтвердить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Alchemical Cap и Equivalent Rod прошли headless-проверку, но визуальные модели и подсказки в клиенте не подтверждены.

## Статус паритета

Core parity 26.3 **не объявлен завершённым**: остаются ручные визуальные и игровые проверки. Вопрос о распознавании FM-taint blocks остаётся предметом сверки оригинального поведения, а не подтверждённым API-блокером. Отклонения и приближения перечислены в [fidelity ledger](fidelity.md); полный parity интеграций Blood Magic/Botania/Ars Magica 2/CoFH не заявляется.
