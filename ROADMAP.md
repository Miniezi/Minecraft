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
