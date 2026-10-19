# Development roadmap — MCA Industrial Villages

## Current large upgrade (October 2026)

Implemented in `mrpack-builder`:
- A full **17-stage Russian village progression** advancement tree, expanded from the initial 5-stage chain. New branches: agriculture, livestock, beds, library, defense, logistics, warehouse automation, public lighting, surveying/travel, heavy industry, prestige and harvest storage.
- **Four new two-stage cooperative MCA village projects**, added to four existing ones: street lighting, public library, trade routes and emergency clinic.
- MRPACK builder explicitly bundles and requires all progression milestones and eight village projects; GitHub Actions checks JSON presence and project phases.
- Kept Forge 1.20.1 as the current release target, while an optimal-version assessment remains open. Migration is not justified until essential add-on compatibility is confirmed.
- Existing Russian translation and Chipped exclusion unchanged.

## Important quality limitations

Advancement triggers represent survival progression *through possessing items* (not automatically detecting constructed structures, functioning Power Grid networks or real MCA villagers). The new MCA Quests project definitions follow the shape of the four existing projects, but in-game engine support should be verified by launching the client and playing with NPCs.

## Next large batch

1. Build a new custom Forge village control panel UI in the worker-chests addon, with a translated status widget and accessibility-conscious layout.
2. Introduce actual server-side event tracking for electricity and village infrastructure (rather than placeholder inventory achievements) with safe persistent state and admin diagnostic commands.
3. Create a coherent resource pack palette for village panels and technology HUD, optimized for 1.20.1 texture dimensions.
4. Build a repeatable dedicated-server smoke test to validate loading and registry compatibility; do not claim gameplay is stable until actual tests.
5. Analyze documented availability of MCA Capitals, Conversations, Quests, Create and Power Grid on newer Minecraft loaders before attempting migration.

## Build policy

Preserve the previous verified .mrpack. Do not import worlds automatically, do not include Townstead in the Capitals profile, do not translate Chipped. Run focused checks first and release a new pack only after CI succeeds.

## Worker-chests gameplay update (19 October 2026)

Implemented in the custom `mcaworkchests` Forge addon:
- **Worker status cards:** sneak/right-click an MCA villager with a **book** to inspect assigned chest, food reserve, custom hunger/comfort indicators, collected batches, delivered item count, and active/rest status.
- **Chest-side management:** sneak/right-click a linked chest with a **book** to inspect, or with a **redstone torch** to toggle between working and rest. Normal right-click without sneaking still opens the container.
- **Persistent counters:** delivered items and successful pickups saved in worker persistent NBT data. These measure work done by this addon, not MCA friendship or job stats.
- **Bed recovery:** workers ordered to rest regain custom comfort, especially near a bed. Checks occur only during throttled needs updates.
- **All status texts currently display in Russian.** A future version should move hardcoded messages to a translation JSON for accessibility and English optionality.

CI: experimental Forge compilation and main MRPACK workflow are triggered automatically by source updates. Do not claim the new features are in a tested .mrpack until those runs pass. Next big upgrade: proper translated GUI with controls, item filter/blacklist for worker collections, live integration smoke tests, and visual textures/icons.

## RPG skill tree — initial implementation (October 2026)

New independent custom Forge gameplay system in `VillageSkills.java`:
- **Charisma** — interact with MCA villagers and vanilla villagers; can unlock a short regeneration effect while socializing.
- **Stealth** — gain experience while moving carefully in sneak mode; unlock damage reduction while crouching.
- **Endurance** — develop through sprinting; unlock survival support and a movement speed perk.
- **Craft** — gain XP through crafting; potential bonus vanilla XP.
- **Engineering** — gain XP through mining and construction preparation; potential bonus vanilla XP.

Each path has 20 levels, 3 talent ranks unlocked at skill levels 2/5/9, and a per-player NBT save persisted across death. Use `/skills` to inspect and `/skills unlock charisma|stealth|endurance|craft|engineering` to unlock perks. This initial build is a **Russian text-based interactive tree in Minecraft chat**, *not yet a custom rendered GUI*, and isn't represented as native MCA relationship changes. Future: clickable chat widgets, proper full-screen skill menu, JSON-configurable XP rules, anti-farm balance, tighter integration with MCA reputation and Create machine usage.

Verification: wait for experimental addon Forge compilation run and integration CI. Do not label it fully in-game tested.

## RPG expansion: exploration and agriculture

Added two new RPG branches beyond the existing five: **Исследование** (exploration, 24 skill XP per newly discovered biome, recording up to 128 biome identifiers in per-player persistent NBT, Luck rank I / underground Night Vision rank II / Luck II rank III) and **Земледелие** (8 skill XP for harvesting mature crop blocks, escalating small vanilla XP bonuses at ranks I–III). Both expose progress, unlockable perks and Russian descriptions through `/skills`. Existing saved skills remain intact. World structures, MCA relationships, and native Power Grid data are not modified by this feature. Forge compilation and main MRPACK GitHub Actions must pass before the update is marked releasable. Next priority: dedicated GUI skill tree, trading/leadership specialization, exploit resistance and playtesting.

## Worker logistics and village pantry reliability (19 October 2026)

Changed **WorkerChests.java only**, leaving the RPG skill system untouched as requested:
- Block chest reassignment if a worker still carries a harvest, preventing silent NBT item deletion.
- Release the old chest's worker ownership when the old chest is loaded and available; refuse takeover of another worker's chest.
- Correctly respect per-slot inventory capacity and `canPlaceItem` rules, avoiding overstacking and inserting into restricted slots.
- Add food reserve replenishment from *surplus* in a linked chest: worker must be within 6 blocks, hunger at least 70, personal reserve empty and chest must contain more than 16 eligible food items; transfers one food item to personal reserve. Food replenishment remains active during worker rest; harvesting and moving items pause.

Build verification: Forge addon compilation and full MRPACK CI started automatically; review latest run conclusions before claiming ready. This logic is standalone to worker chests; it does not modify MCA's reputation, relationships, or skill trees. Next: implement capacity/backpressure diagnostics, optionally configurable permitted harvest list and an end-to-end world smoke test.
