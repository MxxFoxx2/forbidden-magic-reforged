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

Готово: сборка под Thaumaturge, семь аспектов, восемь осколков, своя creative-вкладка, категория `forbiddenmagic:forbidden` и autounlock-запись `forbiddenmagic:nether_shards`, модели и локализация `en_us`/`ru_ru`, четыре GameTest.

Осталось: рецепты, инфузии, зачарования, исследования, жезлы и насадки, скверна, Mob Crystal, Wrath Cage, дропы, конфиг, остальные предметы и блоки.
