@AGENTS.md

## Claude Code

`AGENTS.md` — единственный источник обязательных правил проекта. Не копируй сюда архитектурные ограничения, release policy или build instructions: обновляй их в `AGENTS.md`.

Для основной работы сначала проверь актуальные `main`, рабочее дерево и нужный version track. `main` — Minecraft 26.3; не смешивай изменения с ветками 26.1.2 и 1.21.1.

При работе со сборкой сохраняй configuration cache, build cache и Gradle daemon. В CI используй только `gradle/actions/setup-gradle` для Gradle User Home cache; не возвращай `--no-daemon` к каждой команде и не отключай обязательные проверки хоста, provenance, генерации ресурсов или обоих GameTest-режимов ради видимого сокращения workflow.

Сверяй поведение с оригиналом и API хоста. Не добавляй одноразовые планы/specs или журналы сессий: фиксируй подтверждённые расхождения, текущие задачи и свежие результаты в `docs/fidelity.md`, `docs/roadmap.md` и `docs/verification.md`.
