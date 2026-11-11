# Roadmap

Основной таргет — Minecraft 26.3. `main` — каноническая ветка. Подтверждённая реализация описывается в [fidelity ledger](fidelity.md), актуальный результат CI — в [verification](verification.md). Здесь остаются только незакрытые требования и критерии завершения.

## Неразрешённый вопрос аудита (не подтверждённый parity gap)

**Участие FM-taint blocks в host taint graph** пока нельзя объявлять доказанным расхождением. В оригинальном исходнике [`BlockLogTainted`](https://github.com/SpitefulFox/ForbiddenMagic/blob/master/src/main/java/fox/spiteful/forbidden/blocks/BlockLogTainted.java) получает `Config.taintMaterial`, а [`BlockLeavesTainted`](https://github.com/SpitefulFox/ForbiddenMagic/blob/master/src/main/java/fox/spiteful/forbidden/blocks/BlockLeavesTainted.java) реализует собственный decay-алгоритм, который распознаёт `taintLog` как опору; отдельной регистрации этих FM-блоков как источников скверны в исходнике не найдено. Современный `TaintSpread.isTaintBlock` действительно проверяет фиксированный список блоков Thaumcraft, но одного этого недостаточно, чтобы заключить, что оригинал ожидал иной результат. Не добавлять новый host hook, пока сравнительная проверка конкретного исходного поведения не докажет пропуск. Существующий GameTest подтверждает только то, что проверенные материалы FM не заменяются в протестированных путях.

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
- Последний проверенный CI зелёный: подготовка и сборка хоста через composite build, сборка аддона, provenance и обе конфигурации GameTests (актуальные номера и run — в [verification](verification.md)).
- Завершён `runClient` и записаны результаты всех ручных пунктов выше.
- Все реальные отклонения core поведения отражены в единственном [fidelity ledger](fidelity.md); отсутствующие внешние интеграции не выдаются за полный parity.