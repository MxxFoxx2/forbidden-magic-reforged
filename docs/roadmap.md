# Roadmap

Основной таргет — Minecraft 26.3. `main` — каноническая ветка. Подтверждённая реализация описывается в [fidelity ledger](fidelity.md), актуальный результат CI — в [verification](verification.md). Здесь остаются только незакрытые требования и критерии завершения.

## Неразрешённый вопрос аудита (не подтверждённый parity gap)

**Участие FM-taint blocks в host taint graph** пока нельзя объявлять доказанным расхождением. В оригинальном исходнике [`BlockLogTainted`](https://github.com/SpitefulFox/ForbiddenMagic/blob/master/src/main/java/fox/spiteful/forbidden/blocks/BlockLogTainted.java) получает `Config.taintMaterial`, а [`BlockLeavesTainted`](https://github.com/SpitefulFox/ForbiddenMagic/blob/master/src/main/java/fox/spiteful/forbidden/blocks/BlockLeavesTainted.java) реализует собственный decay-алгоритм, который распознаёт `taintLog` как опору; отдельной регистрации этих FM-блоков как источников скверны в исходнике не найдено. Современный `TaintSpread.isTaintBlock` действительно проверяет фиксированный список блоков Thaumcraft, но одного этого недостаточно, чтобы заключить, что оригинал ожидал иной результат. Не добавлять новый host hook, пока сравнительная проверка конкретного исходного поведения не докажет пропуск. Существующий GameTest подтверждает только то, что проверенные материалы FM не заменяются в протестированных путях.

На текущий момент этого аудита не осталось подтверждённых source-level core-функциональных блокеров. Завершение core parity всё ещё зависит от ручных проверок в клиенте и полноценного gameplay-гейта ниже.

## Уже устранено и проверено

- **Aspect scan Nether Portal:** локальный host patch [`0001-feat-support-block-aspect-scan-targets.patch`](../patches/0001-feat-support-block-aspect-scan-targets.patch) добавляет регистрацию аспектов на блоки и синхронизацию block targets. `FmAspectTags` назначает NETHER 4 современному блоку `minecraft:nether_portal`; GameTest проверяет фактическую цель `block:minecraft:nether_portal` и количество NETHER 4. Обсидиан больше не используется как прокси.
- **Alchemical Wand Cap и Equivalent Wand Rod:** восстановлены исходные параметры (discount 0.9, WATER discount 0.8, cost 7; capacity 100, cost 12), отдельные компоненты/текстуры и Equivalent creative wand с void cap. CI прошёл сборку, provenance и 97 GameTest в обоих режимах.
- Скверное дерево/саженец восстановлены по оригинальному генератору; headless-тесты покрывают стадии, форму кроны и поддержку листвы. Визуальная проверка в клиенте остаётся открытой.
- **Add-on aspect API влит в апстрим:** [PR #37](https://github.com/notlikethiss/thaumcraft-4-reforged/pull/37) смержен как [`d75a946`](https://github.com/notlikethiss/thaumcraft-4-reforged/commit/d75a94602100595f48bf31debab0f26c5ad521e2) и выпущен в `1.1.2`; следом автор упростил API (`7df0506`). Наши патчи реестра аспектов, хука `AddonAspects` и сканера удалены, порт собирается против стокового API. Отличие, которое осталось: `Aspect.count()`/`builtinCount()` заменены на `Aspect.values().length` и константу 48, а диагностика аддон-аспектов берёт канонический порядок хоста — апстрим сортирует аддонные аспекты по тегу, поэтому `desidia` идёт первой.

**Upstream обновлён:** `host.sha` передвинут с `69a47a1` через `cf0c421` и `5ea7ba3` на `6047676` (апстрим `main`, `mod_version` по-прежнему `1.1.2`). Block aspect targets остаются единственным функциональным патчем: в апстриме `AspectRegistrar` по-прежнему принимает только item-цели. При обновлении upstream проверяй применимость `0001` и тест портала.

## Незакрытые ручные проверки

- `runClient`: tint Morph Tools; Creative Wand family — модели, подсказки, creative tab и пополнение vis.
- `runClient`: Tainted Log во всех трёх ориентациях; Taint Sapling обеих стадий, форма кроны и decay support листьев; Wrath Cage render и обновление моба после смены кристалла.
- Реальная игровая проверка hooks урона жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Проверить в клиенте Alchemical Cap / Equivalent Rod: предметные и wand-модели, подсказки и Equivalent creative wand.

Headless CI не заменяет визуальный и игровой гейт.

## Ограничения, не выдаваемые за баги

- Старый Extreme Hills сопоставлен с `minecraft:windswept_hills`; точного аналога нет.
- Mob Crystal хранит современный namespaced entity ID; миграция миров 1.7.10 не заявлена.
- Blood Magic, Botania, Ars Magica 2 и CoFH не поддерживаются на 26.3 из-за отсутствия согласованных совместимых зависимостей. Без них нельзя заявлять полный паритет интеграций оригинала; это отдельный scope вне core parity.
- Ветка 1.21.1 уже переведена на upstream aspect API и composite build в PR #44; полный content parity 1.21.1 остаётся отдельной задачей.

## Гейт завершения core parity 26.3

- Нет неподтверждённых предположений, выдаваемых за баги; оставшиеся функциональные отличия доказаны исходным кодом и отражены в `docs/fidelity.md`.
- Последний проверенный CI зелёный: подготовка и сборка хоста через composite build, сборка аддона, provenance и 95/95 GameTests с `no_lust=false` и `no_lust=true` ([run #40566141998](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40566141998)).
- Завершён `runClient` и записаны результаты всех ручных пунктов выше.
- Все реальные отклонения core поведения отражены в единственном [fidelity ledger](fidelity.md); отсутствующие внешние интеграции не выдаются за полный parity.
