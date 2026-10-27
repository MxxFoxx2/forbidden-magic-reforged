# Verification status

## Последний успешный runtime CI

- Проверенный коммит: [`5c5aa02`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/5c5aa020e31d7c222ce2198475658e997d295e6e).
- [Java CI with Gradle #40566141998](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40566141998) завершился успешно 27 октября 2026 года.
- Хост закреплён на upstream `604767636dc318a6d81f1abb8a8a2d3e5ec3298a` (1.1.2); host patch подготовлен через `tools/prepare_host.py`, сборка использует Gradle composite build вместо локального Maven publish.
- Сборка хоста, сборка аддона и проверка provenance завершились успешно.
- С `no_lust=false`: **95/95** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **95/95** required GameTests passed; `luxuria=null`.
- Опубликованный релиз: rolling [`26.3`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/26.3). Сборка и тесты headless не заменяют ручные проверки клиента и реального геймплея.

## Проверки вне CI

- `tools/gen_resources.py` запускался дважды с чистым diff после генерации в pre-release проверке.
- `runClient` ещё должен подтвердить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Alchemical Cap и Equivalent Rod прошли headless-проверку, но визуальные модели и подсказки в клиенте не подтверждены.

## Статус паритета

Core parity 26.3 **не объявлен завершённым**: остаются ручные визуальные и игровые проверки. Вопрос о распознавании FM-taint blocks остаётся предметом сверки оригинального поведения, а не подтверждённым API-блокером. Отклонения и приближения перечислены в [fidelity ledger](fidelity.md); полный parity интеграций Blood Magic/Botania/Ars Magica 2/CoFH не заявляется.
