# Интеграция с Thaumcraft 4: Reforged — Minecraft 26.3

Этот документ фиксирует **фактически используемые** точки интеграции ветки main. Это не полная документация API Thaumcraft и не обещание совместимости со всеми версиями хоста.

## Зафиксированная версия

- Хост: [notlikethiss/thaumcraft-4-reforged](https://github.com/notlikethiss/thaumcraft-4-reforged).
- Версия, закреплённая в host.sha: commit [6047676](https://github.com/notlikethiss/thaumcraft-4-reforged/commit/604767636dc318a6d81f1abb8a8a2d3e5ec3298a), Thaumcraft 4: Reforged 1.1.2.
- Зависимость аддона: thaumcraft_version=1.1.2 в gradle.properties.
- Host-specific реализация находится в [integration/tc4/](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/); композиция выполняется через [Tc4HostAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4HostAdapter.java).

При обновлении хоста сверяй этот документ с новым pin и исходниками. Сигнатура, которая присутствует в текущем адаптере, не считается гарантированной для будущей версии.

## Регистрация аспектов

Используются две разные группы API.

### Аспекты, добавленные аддоном

[Tc4AspectAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4AspectAdapter.java) использует [AddonAspects](https://github.com/notlikethiss/thaumcraft-4-reforged/blob/604767636dc318a6d81f1abb8a8a2d3e5ec3298a/src/main/java/thaumcraft/aspect/AddonAspects.java):

- Регистрирует callback через AddonAspects.addRegistrar(...).
- Преобразует определения core/aspect/AspectDefinition в объекты Aspect.
- Компоненты аспектов разрешаются через таблицу соответствий ContentId → Aspect.
- Текстура преобразуется в полный Minecraft Identifier.
- Результат сохраняется как соответствие семантического ID аддона и зарегистрированного объекта хоста.
- После регистрации вызывается AddonAspects.init().

В текущем host API callback, добавленный после инициализации, вызывается сразу; до инициализации он сохраняется до init(). API добавления аспектов вошёл в хост после [Thaumcraft PR #37](https://github.com/notlikethiss/thaumcraft-4-reforged/pull/37). Не возвращай в наш патч дублирующую реализацию этого API.

### Аспекты предметов и сущностей

[Tc4AspectTagAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4AspectTagAdapter.java) использует [AspectSync](https://github.com/notlikethiss/thaumcraft-4-reforged/blob/604767636dc318a6d81f1abb8a8a2d3e5ec3298a/src/main/java/thaumcraft/aspect/AspectSync.java) и [AspectRegistrar](https://github.com/notlikethiss/thaumcraft-4-reforged/blob/604767636dc318a6d81f1abb8a8a2d3e5ec3298a/src/main/java/thaumcraft/aspect/AspectRegistrar.java):

- AspectSync.addRegistrar(...) подключает нашу таблицу к пересборке реестра.
- AspectRegistrar предоставляет регистрацию, заполнение и объединение аспектов предметов.
- entityCopy(...) добавляет аспекты в существующую запись сущности с сохранением NBT-условий.
- AspectList и Aspect задают количества и значения аспектов.

В закреплённом host source AspectSync запускает пересборку при старте сервера и при datapack sync без конкретного игрока; callbacks вызываются в процессе пересборки до вычисления derived aspects. Не редактируй активный реестр напрямую из общего кода. Вызовы регистрации и преобразование ID принадлежат TC4-адаптеру.

### Блоковые цели сканирования

Блоковые цели — отдельное локальное расширение, а не следствие API регистрации аспектов аддонов.

Патч [0001-block-aspect-targets.patch](../../patches/0001-block-aspect-targets.patch) добавляет в host AspectRegistrar.block(...) и поддержку чтения/синхронизации block targets в сканере. Сейчас адаптер использует этот путь для minecraft:nether_portal.

Tc4AspectTagAdapter намеренно ловит NoSuchMethodError для block-target метода и сообщает, что прямые block aspects недоступны. Не удаляй эту проверку, пока локальный patch не стал обязательным и гарантированным для всех поддерживаемых host pins. GameTest портала проверяет именно прямую цель block:minecraft:nether_portal, а не прокси через обсидиан.

## Жезлы и фокусы

### Регистрация частей жезлов

[Tc4WandAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4WandAdapter.java) использует [WandRegistry](https://github.com/notlikethiss/thaumcraft-4-reforged/blob/604767636dc318a6d81f1abb8a8a2d3e5ec3298a/src/main/java/thaumcraft/wand/WandRegistry.java):

- registerCap(...) регистрирует колпачок: discount, специальные аспекты/скидку, item ID, craft cost, material и texture.
- registerRod(...) регистрирует стержень: базовый tag, вместимость, item ID, craft cost, RodUpdate, glowing/staff/runes и texture.
- Повторный tag колпачка или стержня приводит к IllegalStateException.
- Callback обновления стержня делегирует общие эффекты в core/wand/WandBehaviors; не переносить TC4-типы в core.

### Хранилище vis

[Tc4WandVisAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4WandVisAdapter.java) использует [Wands](https://github.com/notlikethiss/thaumcraft-4-reforged/blob/604767636dc318a6d81f1abb8a8a2d3e5ec3298a/src/main/java/thaumcraft/wand/Wands.java):

| Метод | Использование |
|---|---|
| getMaxVis(stack) | Максимальная вместимость жезла |
| getVis(stack, aspect) | Текущее количество vis аспекта |
| addVis(stack, aspect, amount, apply) | Добавление vis через обычный host-путь |
| addRealVis(stack, aspect, amount, apply) | Добавление в шкале real-vis |
| fill(stack) | Заполнение всех primals до максимума |

В реализации хоста addVis переводит переданное количество в шкалу addRealVis с множителем 100. Оба метода принимают только primal aspects; хост ограничивает сохранённое значение максимальной вместимостью стека. Флаг apply определяет, записывать ли новое значение. Аспекты передаются в виде host Aspect, но вызывающие core-компоненты работают с ContentId. Не переносить Aspect в core/.

### Части жезла

[Tc4WandPartsAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4WandPartsAdapter.java) читает Wands.getParts(stack) и заменяет стержень через WandKeys.PARTS.set(...) с новым WandParts. При этом сохраняются существующие колпачок и флаг sceptre. Изменение этой логики должно сохранять обе части, которые не заменяются.

### Триггеры и Blink Focus

- [Tc4WandTriggerAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4WandTriggerAdapter.java) регистрирует обработчик через WandTriggers.register(Predicate<BlockState>, WandTrigger). Проверка wand/focus, исследования и потребления FIRE vis выполняется через TC4 API.
- [Tc4BlinkFocusItem](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4BlinkFocusItem.java) наследуется от FocusItem, использует FocusUpgradeType и WandItem.consumeAllVis(...). Само поведение телепортации и эффекты вынесены в host-neutral BlinkFocusBehavior.

## Warp

[Tc4WarpAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4WarpAdapter.java) использует [Warp](https://github.com/notlikethiss/thaumcraft-4-reforged/blob/604767636dc318a6d81f1abb8a8a2d3e5ec3298a/src/main/java/thaumcraft/research/Warp.java):

- Warp.registerItem(Identifier, int) регистрирует количество warp для предмета.
- Warp.add(ServerPlayer, amount, WarpType) изменяет warp игрока.
- Предметный warp применяется хостом при крафте как sticky warp, если его конфигурация не отключает это поведение.
- Есть отдельное сопоставление PERMANENT, STICKY и TEMPORARY с WarpType.PERMANENT, WarpType.STICKY и WarpType.TEMPORARY.

Не своди sticky warp к permanent или temporary: это отдельный пул и отдельная возможность профиля хоста.

## Research и recipes

### Исследования

[Tc4ResearchAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4ResearchAdapter.java) переводит общие определения через ResearchCategories.registerCategory, ResearchCategories.addResearch, ResearchItem и ResearchPage.

Текущие ограничения адаптера намеренные и проверяются явно:

- У записи поддерживается ровно одна группа страниц; другое число приводит к IllegalArgumentException.
- Stage-specific prerequisites не поддерживаются и отвергаются.
- Legacy research keys преобразуются отдельной таблицей, поскольку часть ключей оригинального Thaumcraft имеет исторический регистр и написание.
- Типы страниц переводятся в конкретные фабрики ResearchPage.

Профиль объявляет RESEARCH_STAGES как PARTIAL. Не расширяй этот статус без реализации и GameTest покрытия новых возможностей.

### Рецепты

[Tc4RecipeAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4RecipeAdapter.java) регистрирует определения через TcRecipes.register и поддерживает используемые нашим core типы:

- infusion;
- infusion enchantment;
- crucible;
- shaped и shapeless arcane;
- crafting recipe reference.

Ингредиенты преобразуются в TcIngredient, результаты — в TcStack, аспекты — в AspectList. Неизвестный тип определения и неподдерживаемый формат ингредиента заканчиваются явной ошибкой, а не молчаливой подменой.

### Scribing

[Tc4ScribingAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4ScribingAdapter.java) читает настройку сложности через Config.researchDifficulty(), получает primal aspects через Aspect.getPrimalAspects() и изменяет pool игрока через ResearchManager.addAspectPool(...).

## Smelting-бонусы

Оригинал FM регистрировал бонус «изумруд → осколок изумруда» через `ThaumcraftApi.addSmeltingBonus`. В TC4 1.1.2 и 1.1.3 таблица бонусов внутренняя: у `thaumcraft.device.SmeltingBonuses` есть только внутренние `item(...)`/`tag(...)` и публичный `get(ItemStack)` — метода регистрации для аддонов нет.

[Tc4SmeltingBonusAdapter](../../src/main/java/dev/visfox/forbiddenreforged/integration/tc4/Tc4SmeltingBonusAdapter.java) рефлективно ищет в классе хоста публичный статический метод регистрации (`addSmeltingBonus` или `register`, с аргументами `ItemStack`/`Item`) и вызывает его, когда такой метод появляется. Пока метода нет, capability остаётся неактивной, а профиль хоста объявляет `SMELTING_BONUSES` как `UNSUPPORTED`.

Регистрация выполняется в `common/FmSmeltingBonusSetup` по событию `DefaultDataComponentsBoundEvent`: host-API, принимающий `ItemStack`, читает компоненты предметов, а они привязываются только после загрузки данных сервера. Создавать `ItemStack` на этапе common setup нельзя — компоненты ещё не привязаны (`Components not bound yet`).

Что делать при обновлении хоста: если хост опубликует метод регистрации, адаптер подхватит его без изменения кода аддона; если появится метод с другой сигнатурой, её нужно добавить в список кандидатов `Tc4SmeltingBonusAdapter.CANDIDATES` и подтвердить GameTest `smelting_bonus_stays_dormant_until_host_exposes_an_api`.

Не подменяй бонус другим механизмом: пока API отсутствует, конверсия изумруд↔осколок остаётся только crafting-рецептами, как в текущем порте.

## Процедура обновления хоста

При изменении host pin для main:

1. Обновить host.sha и thaumcraft_version согласованно.
2. Запустить python3 tools/prepare_host.py; проверить, что применились все локальные патчи.
3. Собрать подготовленный host и аддон; проверить artifact provenance.
4. Запустить GameTests с no_lust=false и no_lust=true.
5. Проверить сценарии регистрации аспектов, block-target Nether Portal, vis, частей жезла, warp, research и recipes.
6. Обновить этот документ только после проверки фактической совместимости. Успешная компиляция сама по себе не доказывает сохранение поведения.

Полная автоматическая проверка задаётся CI и описана в [Verification status](../verification.md). Общие сведения о pin, патчах и composite build остаются в [Host compatibility](../upstream.md).
