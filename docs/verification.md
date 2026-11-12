# Verification status

## Minecraft 26.1.2 — fast unit-test gate (local, 12 November 2026)

- Added a host-neutral JUnit 5 suite under `src/test/java`, run by `./gradlew test` (part of `check`/`build`, plus a dedicated CI step). It verifies catalog data and option gating in seconds, without booting a server.
- **23 unit tests passed** across 4 suites: `AspectCatalogTest` (6), `ContentIdTest` (4), `RecipeCatalogTest` (7), `ResearchCatalogTest` (6); 0 failures, 0 skipped.
- Removed the pure-logic GameTest `core_aspect_catalog_is_host_neutral_and_complete` from `AspectGameTests`; its assertions now live in `AspectCatalogTest`. All runtime GameTests are retained.
- Local `runGameTestServer`: with `no_lust=false` **112/112** required GameTests passed; with `no_lust=true` **112/112** passed.
- Host-agnostic API boundary check passed (28 Java files).
- Local headless verification only; the change is not yet pushed to CI. Client launch, rendering/tooltips, and manual in-world gameplay checks are still required before treating the port as release-ready.

## Minecraft 26.1.2 — host-agnostic architecture port

- Verified implementation commit: [`62a3e5e`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/62a3e5ee89fba12ba7e902501725d1bc5b1eea9f).
- [Java CI with Gradle #472](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41375528543) completed successfully on 2 November 2026.
- Host-agnostic API boundary check passed.
- Pinned Thaumcraft 4: Reforged 1.1.2 preparation and build passed.
- Forbidden Magic compilation, artifact provenance, and generated-resource freshness checks passed.
- With `no_lust=false`: **111/111 GameTests passed** (`luxuria=luxuria`).
- With `no_lust=true`: **111/111 GameTests passed** (`luxuria=null`).
- The selected TC4 adapter profile reports Minecraft 26.1.2 and the support-level GameTest matrix matches all 36 declared feature flags.
- This CI is headless. Client launch, rendering/tooltips, and manual in-world gameplay checks are still required before treating the port as release-ready.


## Последний успешный runtime CI

- Проверенный коммит: [`74a6dcd`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/74a6dcd0e31d7c222ce2198475658e997d295e6e).
- [Java CI with Gradle #40566141998](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40566141998) завершился успешно 27 октября 2026 года.
- Хост закреплён на upstream `604767636dc318a6d81f1abb8a8a2d3e5ec3298a` (1.1.2); host patch подготовлен через `tools/prepare_host.py`, сборка использует Gradle composite build вместо локального Maven publish.
- Сборка хоста, сборка аддона и проверка provenance завершились успешно.
- С `no_lust=false`: **97/97** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **97/97** required GameTests passed; `luxuria=null`.
- Опубликованный релиз: rolling [`26.3`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/26.3). Сборка и тесты headless не заменяют ручные проверки клиента и реального геймплея.

## Проверки вне CI

- `tools/gen_resources.py` запускался дважды с чистым diff после генерации в pre-release проверке.
- `runClient` ещё должен подтвердить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Alchemical Cap и Equivalent Rod прошли headless-проверку, но визуальные модели и подсказки в клиенте не подтверждены.

## Статус паритета

Core parity 26.3 **не объявлен завершённым**: остаются ручные визуальные и игровые проверки. Вопрос о распознавании FM-taint blocks остаётся предметом сверки оригинального поведения, а не подтверждённым API-блокером. Отклонения и приближения перечислены в [fidelity ledger](fidelity.md); полный parity интеграций Blood Magic/Botania/CoFH не заявляется, а AM2 заменён на Ars Magica: Legacy с ограниченным набором механик (см. [roadmap](roadmap.md)).
