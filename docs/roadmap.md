# Roadmap

Основной таргет — Minecraft 26.3. `main` — каноническая ветка. Подтверждённая реализация описывается в [fidelity ledger](fidelity.md), актуальный результат CI — в [verification](verification.md). Здесь остаются только незакрытые требования и критерии завершения.

## Завершённая host-agnostic архитектура

- Общие модели аспектов, рецептов и исследований заведены; TC4-адаптеры переводят их в API хоста. Ингредиенты, принадлежащие хосту, представлены в core как `host:item/...` и `host:tag/...`; `Tc4ContentAdapter` разрешает их в реальные TC4 registry IDs. Boundary-check запрещает жёстко прописывать namespace хоста внутри `core`. Изменение warp проходит по цепочке `core/warp/WarpAccess` → `HostWarpServices` → `Tc4WarpAdapter`; регистрацию warp-предметов выполняет host bootstrap. `core/warp` больше не публикует NeoForge events и не зависит от event bus.
- Wrath Cage больше не наследуется от `TcBlockEntity` и не реализует `AspectSource`/`EssentiaTransport` в общем gameplay-классе. TC4 bridge-subclass реализует эти интерфейсы, а общая логика принимает `ContentId` и callback-источники эссенции.
- Поведения Tainted, Infernal, Profane и creative rods используют общие `WandVisAccess`/`WandPartsAccess`; Profane Contract хранится через `WandContractAccess`, а `FmWandContractAccess` связывает этот порт с общей data component вне `core`. TC4-адаптер переводит wand-операции в `Wands`/`WandKeys` и сохраняет регистрацию rods/caps. GameTest прогоняет Profane transition: 25 000 Vis, смену rod, очистку контракта и sticky warp через injected ports.
- Runtime-поведение разделено на узкие capabilities: `HostEntityAspectServices` / `HostEntityClassificationServices`, `HostAspectServices`, `HostMorphToolServices`, `HostBlockEntityServices`, `HostWandInspectionServices` / `HostWandVisServices` / `HostAccessoryServices`, `HostTaintBlockServices` / `HostEnchantmentEffectServices`, `HostScribingServices`, `HostWarpServices`, `HostItemPropertyServices` / `HostItemFactoryServices` и `HostBootstrapServices`. Прежние агрегаты `HostRuntimeServices`, `HostEntityServices`, `HostItemServices`, `HostWandServices` и `HostTaintServices` удалены. `FmSpawnerMobs` использует отдельный типизированный каталог аспектов сущностей и capability-классификатор; `Tc4EntityAdapter` владеет TC4-таксономией. TC4 item factories, Baubles/RunicArmor, ScribingTools, Morph Tool, taint behavior, Essentia bridge и warp mapping остаются в `integration/tc4`. `HostProfile` явно описывает поддержку каждой функции: все 36 feature flags имеют ровно одну запись; `TAINT_ORIGINS` явно помечен `UNSUPPORTED`, поскольку адаптер происхождения скверны не реализован. Composition root устанавливает `Tc4HostAdapter`; общий gameplay использует только capability-интерфейсы. Подробная схема, ограничения и правила расширения записаны в [host-agnostic core architecture](architecture/host-agnostic-core.md).
- Триггер Hellfire вынесен в общий `core/wand/HellfireWandBehavior`: проверка направления, предмета-жезла, фокуса, исследования, разрешения на установку и расход vis выполняется через узкий `WandTriggerAccess`. `integration/tc4/Tc4WandTriggerAdapter` сохраняет регистрацию на обсидиане/незераке и переводит семантические ID исследования и аспекта в API TC4.
- Blink Focus разделён на общий `core/wand/BlinkFocusBehavior` (поиск цели, телепортация, Hellfire и Pandemonium) и `integration/tc4/Tc4BlinkFocusItem` (`FocusItem`, upgrade registry, aspect costs и vis consumption через TC4). Прямые вызовы `Wands` остаются допустимыми только в TC4-адаптерах и должны проходить аудит по мере переноса потребителей.
- Host-agnostic архитектура завершена в заявленном объёме и влита в `main` через [PR #63](https://github.com/MxxFoxx2/forbidden-magic-reforged/pull/63), merge commit [`542aab9`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/542aab9f619e3b8fe043d645069c72d580b07c09), 2 ноября 2026 года. Мажорный релиз [**2.0.0 (NeoForge 26.3)**](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release/26.3) опубликован; доступен [JAR-файл](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/download/release/26.3/forbidden-magic-2.0.0-26.3.jar). Второй host-адаптер и multi-version binary в этот релиз не входят.
- Критерии каждого среза: общая модель не импортирует хост; адаптер сохраняет существующие ключи и игровое поведение; оба режима GameTest проходят; все неполные соответствия явно документированы.
- Не объединять версии Minecraft в один source set или бинарник. Отдельные release tracks сохраняются.

## Неразрешённый вопрос аудита (не подтверждённый parity gap)

**Участие FM-taint blocks в host taint graph** пока нельзя объявлять доказанным расхождением. В оригинальном исходнике [`BlockLogTainted`](https://github.com/SpitefulFox/ForbiddenMagic/blob/master/src/main/java/fox/spiteful/forbidden/blocks/BlockLogTainted.java) получает `Config.taintMaterial`, а [`BlockLeavesTainted`](https://github.com/SpitefulFox/ForbiddenMagic/blob/master/src/main/java/fox/spiteful/forbidden/blocks/BlockLeavesTainted.java) реализует собственный decay-алгоритм, который распознаёт `taintLog` как опору; отдельной регистрации этих FM-блоков как источников скверны в исходнике не найдено. Современный `TaintSpread.isTaintBlock` действительно проверяет фиксированный список блоков Thaumcraft, но одного этого недостаточно, чтобы заключить, что оригинал ожидал иной результат. Не добавлять новый host hook, пока сравнительная проверка конкретного исходного поведения не докажет пропуск. Существующий GameTest подтверждает только то, что проверенные материалы FM не заменяются в протестированных путях.

В пределах заявленного scope — независимое от magic-host API ядро для текущего Minecraft/NeoForge таргета — архитектурная миграция завершена: общие каталоги и вынесенные правила не зависят от TC4 API, а host-specific поведение проходит через адаптерные контракты. Второй адаптер и межверсионная совместимость не заявляются. Core parity при этом ещё не завершён: остаются ручные визуальные и игровые проверки ниже.

## Уже устранено и проверено

- **Aspect scan Nether Portal:** локальный host patch [`0001-feat-support-block-aspect-scan-targets.patch`](../patches/0001-feat-support-block-aspect-scan-targets.patch) добавляет регистрацию аспектов на блоки и синхронизацию block targets. `Tc4AspectTagAdapter` назначает NETHER 4 современному блоку `minecraft:nether_portal`; GameTest проверяет фактическую цель `block:minecraft:nether_portal` и количество NETHER 4. Обсидиан больше не используется как прокси.
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
