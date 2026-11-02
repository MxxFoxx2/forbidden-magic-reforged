# Forbidden Magic: Reforged

Неофициальный порт **Forbidden Magic для современных версий Minecraft** на базе **Thaumcraft 4: Reforged**.

Проект переносит контент и игровые механики оригинального мода в современные версии Minecraft и NeoForge.

## Поддерживаемые версии

| Minecraft | NeoForge | Thaumcraft 4: Reforged | Релиз |
| --- | --- | --- | --- |
| **26.3** | 26.3.0.57-beta | 1.1.2 | [2.0.0](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F26.3) |
| **26.1.2** | 26.1.2.122 | 1.1.2 | [1.0.2](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F26.1.2) |
| **1.21.1** | 21.1.256 | 1.1.2 | [1.0.47](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/release%2F1.21.1) |

Для Minecraft **1.21.1** дополнительно требуется [Curios 9.5.1+1.21.1](https://www.curseforge.com/minecraft/mc-mods/curios).

Сборки разных версий Minecraft не взаимозаменяемы.

## О моде

В порт входят аспекты грехов, осколки, зачарования, хамелеоновы инструменты, материалы и блоки скверны, Mob Crystal, Wrath Cage, жезлы и их компоненты, Blink Focus, рецепты и исследования.

## Архитектура

В версии **2.0.0 для Minecraft 26.3** общие модели контента и игровые правила отделены от API Thaumcraft. Общий код использует контракт `HostAdapter` и специализированные интерфейсы возможностей, а взаимодействие с Thaumcraft сосредоточено в `integration/tc4/`.

Это отделяет мод от конкретного magic-host, но не от Minecraft и NeoForge. Сейчас в production используется только адаптер Thaumcraft 4: Reforged; второй хост и единый JAR для разных версий Minecraft не заявляются.

Подробнее — [архитектура проекта](docs/architecture/host-agnostic-core.md).

## Установка и использование

Скачайте релиз для своей версии Minecraft, установите указанные выше NeoForge и Thaumcraft (а для 1.21.1 — также Curios), затем поместите JAR мода в папку `mods/`.

В Thaumonomicon категория **FORBIDDEN** открывается после исследования «Осколки Нижнего мира». Хамелеоновы инструменты переключают наборы зачарований сочетанием **Shift + ПКМ**.

## Состояние порта

Полный паритет с оригиналом не заявляется. Проверенные отличия, адаптации и известные ограничения собраны в [Fidelity Ledger](docs/fidelity.md). Результаты сборок и автоматических тестов, а также оставшиеся ручные проверки — в [Verification Report](docs/verification.md).

## Лицензии и благодарности

- **SpitefulFox** — оригинальный [Forbidden Magic](https://github.com/SpitefulFox/ForbiddenMagic) 0.574 (WTFPL).
- **Azanor** — Thaumcraft 4.
- **notlikethiss** — [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged).
- **MxxFoxx2** — порт (MIT).

Лицензии оригинала, хоста и порта независимы. Код и ресурсы Thaumcraft не включаются в распространяемый JAR. Подробности — в [LICENSE](LICENSE) и [NOTICE](NOTICE).
