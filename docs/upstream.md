# Host compatibility

Хост: [`notlikethiss/thaumcraft-4-reforged`](https://github.com/notlikethiss/thaumcraft-4-reforged). Исходный коммит закреплён в `host.sha`; лицензия хоста — All Rights Reserved. Не включай host-код или ассеты в JAR аддона и не распространяй собранный host JAR.

## Локальные патчи

- `patches/0001-feat-support-block-aspect-scan-targets.patch` — единственный патч `main`: добавляет регистрацию аспектов блоков, синхронизацию и чтение block targets в `ScanManager`, чтобы `minecraft:nether_portal` имел собственную цель сканирования.
- Локальный Maven publish больше не используется. Gradle composite build подключает подготовленную папку `host/` и подменяет зависимость `thaumcraft:thaumcraft` проектом хоста. Это сохраняет проверяемую сборку хоста, не требуя публикации JAR в Maven.

Реестр аспектов, публичный API `AddonAspects`, инициализация в lifecycle и поддержка генераторов уже есть в upstream 1.1.2. Патчи, дублировавшие эти функции, удалены.

## Версия хоста и CI

- `host.sha` закрепляет `99499a8ef6cf9af0efa6a93a058ea56a42a0e7b2` (Thaumcraft 4: Reforged 1.1.3, Minecraft 26.3).
- `tools/prepare_host.py` создаёт рабочую копию указанного коммита и применяет только файлы из `patches/`.
- `settings.gradle` подменяет Maven-координаты хоста локальным composite-build-проектом. `build.gradle` сверяет `host/gradle.properties:mod_version` с `thaumcraft_version`.
- CI подготавливает хост, собирает его и аддон, проверяет происхождение JAR и запускает два набора GameTests (`no_lust=false/true`). Maven publish не выполняется.

Стоковый хост 1.1.2+ предоставляет аддонам API аспектов. Без локального block-target patch конкретно портал не получает прямой аспект блоковой цели; это ограничение не требует возвращать патчи реестра аспектов.

## API reference для main

Подробная карта фактически используемых API Thaumcraft 4: Reforged, адаптеров и ограничений — [Thaumcraft 26.3 integration reference](host-compatibility/thaumcraft-26.3.md). Документ привязан к текущему `host.sha`; это не общая гарантия совместимости будущих версий.

## API аспектов аддонов

[Thaumcraft PR #37](https://github.com/notlikethiss/thaumcraft-4-reforged/pull/37) принят в upstream коммитом [d75a946](https://github.com/notlikethiss/thaumcraft-4-reforged/commit/d75a94602100595f48bf31debab0f26c5ad521e2); соответствующий API включён в версию 1.1.2. Он добавляет регистрацию аспектов аддонов и объединение аспектов сущностей. Последующее упрощение API находится в [7df0506](https://github.com/notlikethiss/thaumcraft-4-reforged/commit/7df05066a1d8d8d5d6539a9110c9c22b1ea7053d).

Миграция ветки 1.21.1 выполнена через [PR #44](https://github.com/MxxFoxx2/forbidden-magic-reforged/pull/44), слитый коммитом [88e6329](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/88e6329f44ff15824b68bfd9b7481e2c0c2aacf8). Проверка PR завершилась успешно: хост подготовлен без патчей, сборка и provenance успешны, 13/13 GameTests прошли ([CI run #40371665525](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40371665525)). После merge запущен отдельный push CI.

## Отдельные ограничения

Upstream-коммит `d72680e` («fix: remove gametests», вошёл в 1.1.3) удалил GameTest-ассеты хоста, включая структуру `thaumcraft:test/platform`. GameTests порта используют собственную фикстуру `forbiddenmagic:test/platform` из `src/gameTest/resources`; не ссылайся на тестовые ассеты хоста.

Регистрация аспектов аддонов сама по себе не добавляет block aspect targets и не меняет распознавание блоков системой скверны. Block targets остаются отдельным локальным расширением; участие FM-taint blocks в host taint graph нужно доказывать по исходному поведению, а не выводить только из реализации host API.
