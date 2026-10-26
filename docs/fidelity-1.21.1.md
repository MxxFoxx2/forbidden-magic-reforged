# Forbidden Magic Reforged — 1.21.1 parity notes

This file tracks the 1.21.1 backport-specific checks separately from the 26.3 fidelity ledger.

## Restored behavior and metadata

- FORK uses the Infernus/NETHER aspect for its infusion cost and research requirement.
- WRATHCAGE depends on FORK; TAINTSHOVEL and ROD_tainted use the restored research aspects and prerequisite paths; ROD_profane uses the round research icon.
- Ring of Nutrition only triggers from Baubles ring slots 1 and 2. The amulet and belt slots do not activate its food bonus.
- Blink Focus Hellfire and Pandemonium upgrades point at their original icon textures, included in the resource pack.
- Witchwood wand and staff cores use an optional reflection-based Ars Magica 2 mana adapter. The wand recharges every 100 ticks at 140 mana per vis point; the staff recharges every 40 ticks at 100 mana per vis point. Vinteum caps reduce those costs to 80 and 50. If Ars Magica 2 or its API is absent, the adapter safely does nothing and does not prevent startup.

## Automated coverage

GameTests check Witchwood updater registration, its tick intervals and mana-cost rules, and the Ring of Nutrition ring-slot predicate. These tests do not replace a client run to visually verify item models, focus-upgrade icons, and research-browser pages.
