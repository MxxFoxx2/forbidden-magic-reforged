# Exact Localization Fidelity Design

## Intent

Make the localization of implemented Forbidden Magic content match the original 1.7.10 addon where the ported behavior supports that text, while keeping the modern host's required key namespaces and documenting unavoidable behavioral differences in `docs/fidelity.md`.

## Goals

- Every registered research item has an English and Russian title and subtitle.
- The `FORBIDDEN` research category has an English and Russian title.
- Blink focus upgrades have English and Russian names and descriptions.
- Transferred items, blocks, wand parts, enchantments, research titles, research subtitles, and research pages use the original wording whenever the corresponding behavior is implemented in the port.
- Original research pages whose described behavior is implemented are registered and shown.
- TOML sources and generated JSON resources remain exactly synchronized.
- A GameTest prevents missing localization keys from recurring.

## Non-goals

- Do not import localization for Blood Magic, Botania, Ars Magica, TT, or other content that the port does not implement.
- Do not add obsolete research entries or legacy key namespaces.
- Do not copy original text that describes behavior the port intentionally does not implement.
- Do not change gameplay behavior as part of the localization correction.
- Do not add duplicate `item.forbiddenmagic.<block_id>` keys for block-items; the modern host uses `block.<namespace>.<id>` descriptions for these items.

## Evidence and confirmed defects

The port registers 31 research entries. Both language files currently contain titles and subtitles for only 12 of them, leaving 38 required `tc.research_name.*` and `tc.research_text.*` keys missing. The registered category lacks `tc.research_category.FORBIDDEN`. Blink focus upgrade types `HELLFIRE` and `PANDEMONIUM` lack the four host-derived `focus.upgrade.*` keys.

The TOML flattening and generated JSON are structurally correct, but several values are newly rewritten rather than transferred from the original. Applicable original pages `FORK.wc`, `TAINTSHOVEL.2`, and `CRYSTALWELL.1b` are absent. `ROD_infernal.2` is not restored because its stronger protection promises are only partially implemented: held-wand fire extinguishing and Wither removal work, but complete fire-damage cancellation and inventory-wide protection do not.

## Localization mapping

### Research metadata

The old namespaces are migrated exactly as follows:

- `forbidden.research_name.<KEY>` becomes `tc.research_name.<KEY>`.
- `forbidden.research_text.<KEY>` becomes `tc.research_text.<KEY>`.
- `forbidden.research_category.FORBIDDEN` becomes `tc.research_category.FORBIDDEN`.
- `forbidden.research_page.<KEY>.<PAGE>` becomes `forbiddenmagic.research_page.<KEY>.<PAGE>`.

Original English values are authoritative. Original Russian values are authoritative when present. The original Russian file lacks metadata for `ROD_profane`, `PRIMEWELL`, and `FOCUSBLINK`; their Russian strings are translated from the original English meaning and follow the existing Russian terminology.

### Items, blocks, wand parts, and enchantments

Original wording is restored under the modern IDs for implemented content, including the original English and Russian names for the Diabolist Fork and Spork, tainted tools, inkwells, Riding Crop, Ring of Nutrition, wand cores, Umbral Rose and Bush, and Voidtouched enchantment. Original `item.Wand.*` component wording is restored. The port's `black_dye` remains named as a dye because it implements sheep dyeing rather than the original shadow-ink resource; that semantic difference is documented.

### Focus upgrades

The original English upgrade descriptions are reused under the host-required namespace:

- `focus.upgrade.hellfire.name`
- `focus.upgrade.hellfire.text`
- `focus.upgrade.pandemonium.name`
- `focus.upgrade.pandemonium.text`

Russian strings are translated from those original descriptions.

## Research page policy

For every registered research item, page text is restored from the original whenever the port implements the behavior described.

Pages restored and registered:

- `forbiddenmagic.research_page.FORK.wc` after `FORK.1`.
- `forbiddenmagic.research_page.TAINTSHOVEL.2` after `TAINTSHOVEL.1`.
- `forbiddenmagic.research_page.CRYSTALWELL.1b` after `CRYSTALWELL.1`.

`forbiddenmagic.research_page.ROD_infernal.2` is not restored. The original page promises complete fire protection and Withering immunity. The port implements held-wand fire extinguishing and Wither removal, but not complete fire-damage cancellation or inventory-wide Wither protection; the partial difference is documented in `docs/fidelity.md`.

Rewritten pages describing modern behavior are either restored to the original when behavior matches or retained and explicitly documented when the original wording would misdescribe the port.

## Testing

A GameTest reads the generated English and Russian JSON and verifies:

- the research category key exists in both languages;
- every one of the 31 registered research IDs has a title and subtitle in both languages;
- all four focus-upgrade keys exist in both languages;
- all explicitly required research-page keys exist in both languages;
- both languages expose the same localization-key set.

The test must fail before production resource changes and pass after generation.

## Verification

1. Run `python tools/gen_resources.py` and confirm generated JSON changes are deterministic.
2. Run the focused GameTest and observe the expected missing-key failure before implementation and success after implementation.
3. Run `cmd //c gradlew.bat build runGameTestServer` and require `BUILD SUCCESSFUL` plus the current required-test count.
4. Run `python tools/prepare_host.py --verify` and require the pinned host plus patch-count match.
5. Launch `runClient` for visual inspection of the research browser, category, and focus-upgrade tooltip. The user performs the final visual acceptance.

## Documentation

`docs/fidelity.md` is the only deviation register. It records:

- intentional modern wording changes retained for behavior-specific pages;
- the partial Infernal Wand protection difference and therefore the omitted original page;
- the `black_dye` semantic split from the original shadow-ink resource;
- any original research page deliberately omitted because its feature is not ported.
