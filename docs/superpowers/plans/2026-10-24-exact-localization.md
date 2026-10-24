# Exact Localization Fidelity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Restore exact original localization for implemented Forbidden Magic content and prevent missing research or focus-upgrade translations.

**Architecture:** Keep human-authored strings in `data/lang/en_us.toml` and `data/lang/ru_ru.toml`, migrate legacy namespaces to the host's required conventions, and generate JSON through `tools/gen_resources.py`. Use one GameTest to verify the required key inventory and parity between English and Russian; register only original pages whose behavior exists in the port.

**Tech Stack:** Java 25, NeoForge/Minecraft 26.3, GameTest, TOML, Python resource generator.

**Spec:** `docs/superpowers/specs/2026-10-24-exact-localization-design.md`

## Global Constraints

- Original 1.7.10 sources in `../ForbiddenMagic` are the behavioral and textual source of truth.
- Only `en_us` and `ru_ru` languages are supported.
- TOML files are the source; generated JSON must be produced by `python tools/gen_resources.py`.
- Do not import localization for features not implemented in the port.
- Do not add duplicate `item.forbiddenmagic.<block_id>` keys for block-items.
- Record unavoidable differences only in `docs/fidelity.md`.
- Tests are GameTests only; use four-space indentation and no code comments.

## Review Focus

- Every registered research entry has a title and subtitle in both languages.
- English and Russian expose the same key set.
- Restored research pages describe behavior actually implemented by the port.
- Original image namespaces are the only intentional change inside restored page text.
- No localization for unported external-mod features leaks into the port.

---

### Task 1: Localization Completeness Regression Test

**Files:**
- Modify: `tests/java/dev/visfox/forbiddenreforged/test/ItemGameTests.java`

**Interfaces:**
- Consumes: generated resources `/assets/forbiddenmagic/lang/en_us.json` and `/assets/forbiddenmagic/lang/ru_ru.json`.
- Produces: GameTest `research_translation_keys_are_complete`.

- [x] **Step 1: Write the failing test**

Add a GameTest that asserts, in both languages:

- `tc.research_category.FORBIDDEN` exists;
- all 31 registered research IDs have `tc.research_name.<ID>` and `tc.research_text.<ID>`;
- all four `focus.upgrade.hellfire/pandemonium` keys exist;
- `forbiddenmagic.research_page.FORK.wc`, `forbiddenmagic.research_page.TAINTSHOVEL.2`, and `forbiddenmagic.research_page.CRYSTALWELL.1b` exist;
- English and Russian key sets are identical.

- [x] **Step 2: Run the test and verify RED**

Run: `cmd //c gradlew.bat runGameTestServer --console=plain`

Expected: FAIL on the first missing `tc.research_name.FORK` key.

### Task 2: Restore Original Metadata and Focus-Upgrade Strings

**Files:**
- Modify: `data/lang/en_us.toml`
- Modify: `data/lang/ru_ru.toml`
- Generate: `src/generated/resources/assets/forbiddenmagic/lang/en_us.json`
- Generate: `src/generated/resources/assets/forbiddenmagic/lang/ru_ru.json`

**Interfaces:**
- Consumes: original values from `../ForbiddenMagic/src/main/resources/assets/forbidden/lang/en_US.lang` and `ru_RU.lang`.
- Produces: complete `tc.research_name.*`, `tc.research_text.*`, `tc.research_category.FORBIDDEN`, and `focus.upgrade.*` keys.

- [x] **Step 1: Add the original research metadata**

Add all 19 missing research names and subtitles to both language files. Use original English and Russian values; translate the three entries absent from the original Russian file from their original English meaning.

- [x] **Step 2: Add category and focus-upgrade strings**

Add `tc.research_category.FORBIDDEN` and the four `focus.upgrade.*` strings using the original English upgrade descriptions and matching Russian translations.

- [x] **Step 3: Generate resources**

Run: `python tools/gen_resources.py`

Expected: both language JSON files are regenerated and now contain the required keys.

- [x] **Step 4: Run the GameTest**

Run: `cmd //c gradlew.bat runGameTestServer --console=plain`

Expected: the new completeness test passes.

### Task 3: Restore Implemented Content Wording and Pages

**Files:**
- Modify: `data/lang/en_us.toml`
- Modify: `data/lang/ru_ru.toml`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmResearch.java`
- Generate: `src/generated/resources/assets/forbiddenmagic/lang/en_us.json`
- Generate: `src/generated/resources/assets/forbiddenmagic/lang/ru_ru.json`
- Modify: `docs/fidelity.md`

**Interfaces:**
- Consumes: original values from the two original `.lang` files.
- Produces: original wording for implemented items, blocks, wand parts, enchantments, metadata, and research pages.

- [x] **Step 1: Restore original values for implemented items and blocks**

Map legacy keys to modern IDs and restore exact original English/Russian wording for the Diabolist Fork and Spork, tainted tools, inkwells, Riding Crop, Ring of Nutrition, wand cores, Umbral Rose and Bush, and Voidtouched enchantment. Restore original `item.Wand.*` wording. Keep `black_dye` as `Black Dye`/`Чёрная краска` because the item's behavior differs from the original shadow ink.

- [x] **Step 2: Restore original research-page values**

Replace rewritten page summaries with exact original English and Russian text, changing only the research image namespace from `forbidden:` to `forbiddenmagic:` where an `<IMG>` tag occurs.

- [x] **Step 3: Register applicable original pages**

Register `FORK.wc` after `FORK.1` and `TAINTSHOVEL.2` after `TAINTSHOVEL.1`. Select exactly one Crystalwell behavior page as the original does: `CRYSTALWELL.1b` in easy research difficulty and `CRYSTALWELL.1` otherwise. Do not register `ROD_infernal.2`.

- [x] **Step 4: Document unavoidable differences**

Update `docs/fidelity.md` to record the omitted Infernal Wand protection page, the modern black-dye semantic split, and any retained behavior-specific wording that cannot use the original text without becoming false.

- [x] **Step 5: Generate and test**

Run: `python tools/gen_resources.py`

Run: `cmd //c gradlew.bat runGameTestServer --console=plain`

Expected: generated resources are synchronized and all required GameTests pass.

### Task 4: Full Verification and Review

**Files:**
- Review all files changed by Tasks 1-3.

**Interfaces:**
- Consumes: completed implementation.
- Produces: verified, reviewable localization slice.

- [x] **Step 1: Inspect the diff**

Run: `git diff --check` and `git diff -- data/lang src/generated/resources/assets/forbiddenmagic/lang tests/java/dev/visfox/forbiddenreforged/test/ItemGameTests.java src/main/java/dev/visfox/forbiddenreforged/FmResearch.java docs/fidelity.md docs/superpowers`

Expected: no whitespace errors or unrelated changes.

- [x] **Step 2: Run the full gate**

Run: `cmd //c gradlew.bat build runGameTestServer --console=plain`

Expected: `BUILD SUCCESSFUL` and `All N required tests passed` with no skipped required test.

- [x] **Step 3: Verify the pinned host**

Run: `python tools/prepare_host.py --verify`

Expected: the host matches the pinned SHA plus the current patch count.

- [x] **Step 4: Commit explicit paths**

Stage only the files changed by this plan and commit with an English message such as `fix: restore original localization fidelity`.

- [x] **Step 5: Request visual verification**

Launch `runClient` and ask the user to inspect the research browser, the `FORBIDDEN` category title, all 31 research titles/subtitles, restored pages, and Blink Focus upgrade tooltips.
