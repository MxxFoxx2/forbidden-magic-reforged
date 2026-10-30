# Thaumaturge port

Ветка `port/thaumaturge` — отдельная сборка Forbidden Magic под [Thaumaturge](https://github.com/Leclowndu93150/Thaumaturge) 1.0.3 на Minecraft 26.1.2. Порт под Thaumcraft 4: Reforged в ветке `mc/26.1.2` не меняется.

## Сборка

Thaumaturge не публикует 1.0.3 в Maven, поэтому jar хоста собирается из локального клона `../Thaumaturge`:

```sh
cd ../Thaumaturge
JAVA_HOME=<JDK 25> ./gradlew build -x test -x spotlessCheck
cd ../forbidden-magic-reforged/.worktrees/port-thaumaturge
JAVA_HOME=<JDK 25> ./gradlew build
```

`build.gradle` ищет `thaumaturge-*-1.0.3.jar` в `../Thaumaturge/build/libs` и `../../../Thaumaturge/build/libs`; путь можно задать явно через `-Pthaumaturge_jar=<path>`. Jar хоста подключается как `compileOnly` и `localRuntime`: его классы и ассеты не входят в jar аддона (лицензия All Rights Reserved).

`lithostitched` и `curios` добавлены в `localRuntime`, потому что Thaumaturge требует их при запуске.

## Раскладка

- `src/thaumaturge/java` — код порта;
- `src/thaumaturge/tests/java` — GameTest-функции порта (компилируются в main, как и раньше);
- `src/thaumaturge/resources` — данные и ассеты порта;
- `src/main/resources/assets` — переиспользуемые текстуры оригинала;
- `src/main/java`, `src/main/resources/data` — исходный код и данные ветки `mc/26.1.2`, оставлены как справочник и в сборку не входят.

## Аспекты

Аспекты грехов — datapack-записи Thaumaturge в `data/forbiddenmagic/thaumaturge/aspect/`. Компоненты взяты из оригинала, но у Thaumaturge нет аспектов `corpus`, `fames` и `telum`, поэтому три пары заменены ближайшими существующими:

- `luxuria`: было Corpus + Fames, стало `thaumaturge:victus` + `thaumaturge:desiderium`;
- `gula`: было Fames + Vacuos, стало `thaumaturge:desiderium` + `thaumaturge:vacuos`;
- `invidia`: было Sensus + Fames, стало `thaumaturge:sensus` + `thaumaturge:desiderium`;
- `ira`: было Telum + Ignis, стало `thaumaturge:aversio` + `thaumaturge:ignis`.

Без замен: `infernus` (Ignis + Praecantatio), `superbia` (Volatus + Vacuos), `desidia` (Vinculum + Spiritus). Цвета, blend и текстуры сохранены по оригиналу. Это подтверждённое ограничение хоста, а не баг порта.

## Текущий срез

Готово: сборка под Thaumaturge, семь аспектов, восемь осколков, скверное дерево и скверные блоки (бревно, доски, камень, кирпичи, листва, саженец с генерацией дерева, блок звезды Нижнего мира), мрачная роза и куст с распространением, таумический торт, Mob Crystal с импринтом мобов, Wrath Cage с essentia-капабилити и восемь зачарований, предметы «Изумрудный фрагмент», «Гексуголь», «Скверный плод», «Чёрная краска», «Адские вилы», «Лопата очистителя», «Кирка искажения», дропы аспектных осколков в Нижнем мире, четырнадцать рецептов (пять crafting, один smelting, четыре crucible и четыре infusion, часть с research-гейтом), конфиг, своя creative-вкладка, категория `forbiddenmagic:forbidden` и девять research-записей, модели, loot-таблицы и локализация `en_us`/`ru_ru`, пятнадцать GameTest.

Отличия этого среза: у бревна пока стандартная модель `cube_column` без случайных «gross» боковых текстур; клиентский тип модели `forbiddenmagic:taint_log_faces` не перенесён. Скверные блоки реализуют `ITaintBlock.decay` (очищение ethereal bloom), но не самопроизвольное random-tick разрушение хостовых скверных блоков — в оригинале FM такого тика не было. «Скверный плод» использует `WarpHelper` Thaumaturge с типом `NORMAL` вместо `STICKY` и предупреждение `warp.thaumaturge.text.1` вместо `warp.text.15`. В crucible-рецептах отсутствующие у хоста аспекты заменены: `POISON` → `MORTUUS`, `GREED` → `DESIDERIUM`. «Лопата очистителя» определяет flux-блоки по id `thaumaturge:flux_goo`/`flux_gas` и играет ванильный `FIRE_EXTINGUISH` вместо хостового `WANDFAIL`.

Осталось: рецепты алхимии и инфузии, зачарования, остальные исследования, жезлы и насадки, Mob Crystal, Wrath Cage, дропы, остальные предметы и блоки.

Эффекты зачарований перенесены частично: `consuming`, `greedy`, `educational`, `voidtouched`-импринт и дропы черепов работают; `cluster` добавляет копию дропа с исходным шансом вместо хостового `SpecialMining`; `corrupting` остаётся инертным, потому что в Thaumaturge нет блоков infused stone; бонусный урон `wrath`/`impact` и связки с жезлами ждут переноса оружия и жезлов. Это зафиксированные приближения, а не паритет.

Mob Crystal хранит только современный namespaced id сущности; таблица мобов переведена на аспекты Thaumaturge, поэтому для зомби, скелета, слизни, крипера, паука и ряда хостовых мобов компоненты аспектов заменены ближайшими существующими (например, `CORPUS`/`FAMES`/`TELUM` отсутствуют). Wrath Cage рисует essentia через `EssentiaCapabilities.TRANSPORT`, но не реализует хостовый aspect-container view и не показывает аспекты в подсказке.

Ограничение хоста: `WandCap` и `WandRod` публично регистрируются в реестрах, но публичного item-обёртки для набалдашников и стержней в `api/wands` нет — их предметы живут во внутреннем пакете `content.wands`. Поэтому Alchemical Cap и Equivalent Rod нельзя корректно перенести через публичный API без изменений в Thaumaturge; это записано как внешнее ограничение, а не обход через форк. Публичного API силы/удачи фокуса тоже нет, поэтому бонусы `Wands.getFocusTreasure`/`getFocusPotency` в дропах и wrath-расчёте пока равны нулю; это отмечено как незакрытый пункт, а не паритет.

## Проверка

- Компиляция проверена напрямую: `javac` с classpath Thaumaturge 1.0.3 и патченного Minecraft 26.1.2 собирает все классы порта без ошибок.
- `python tools/check_thaumaturge_data.py` проверяет datapack-файлы, компоненты аспектов, ссылки research, текстуры, модели, предметные id и паритет ключей `en_us`/`ru_ru`. Проверка проходит.
- `runGameTestServer` в этом окружении не запустился: `maven.neoforged.net` недоступен, а артефакт `neoforge-26.1.2.122-moddev-config.json` не был загружен ранее. Когда сеть восстановится, гейт — `./gradlew build` и `./gradlew runGameTestServer` с зелёными четырьмя тестами.
