# WobbleCombat Root-Cause Analysis (Loads but appears inactive in PvP)

## Scope analyzed
- `plugin.yml`
- `WobbleCombatPlugin` bootstrap and registration path
- Combat damage event path
- Tag gating conditions (permissions/world/gamemode/config)
- Restriction and cleanup listeners

## Findings

### 1) Plugin bootstrap is healthy (not the failure)
- `onEnable()` creates managers, starts the combat tick task, and registers all listeners.
- `wobblecombat` command is present in `plugin.yml` and executor registration is strict (`Objects.requireNonNull`), so missing command would hard-fail startup rather than silently disable combat.

### 2) Core PvP trigger is wired correctly
- `CombatListener#onDamage(EntityDamageByEntityEvent)` handles player victim damage, resolves attacker (player/projectile/TNT/tameable/crystal owner), and calls `combatManager.tag(attacker, victim)`.
- This means event choice + listener registration are not the root cause for a plugin that "loads but doesn't tag".

### 3) Actual hard stop: bypass permission gate blocks ALL tagging for that player pair
- `CombatManager#tag(first, second)` returns immediately unless **both** players pass `canBeTagged(...)`.
- `canBeTagged(...)` returns false if player has `wobblecombat.bypass`.
- Therefore if either attacker or victim has bypass (directly, wildcard, group inheritance, or OP-like grant), neither player is tagged and no combat behavior appears.

### 4) Additional config-gated blockers that can make combat look inactive
- `disabled-worlds` excludes tagging in listed worlds.
- `ignore-non-survival: true` excludes CREATIVE and SPECTATOR from tag eligibility.
- `minimum-final-damage-to-tag` can suppress tags if set above actual damage profile.

## Root-cause classification
Primary observed root cause for "loads, config exists, PvP does nothing":
- **Permission/config integration issue** (bypass granted to test players), not listener wiring bug.

Possible secondary causes in some deployments:
- **Config issue** (`disabled-worlds`, `ignore-non-survival`, damage threshold).

## Minimal corrective actions (analysis-first, no large rewrite)
1. Remove `wobblecombat.bypass` from normal players/test accounts/groups.
2. Validate with `/wc debug <player>` and ensure `bypass: false`.
3. Confirm fight world is not in `disabled-worlds`.
4. Confirm both players are in SURVIVAL/ADVENTURE during test.
5. Keep `minimum-final-damage-to-tag` at `0.1` for baseline validation.

## Optional hardening (small, safe improvements)
- Add a debug log when tag is skipped due to bypass/world/gamemode (only in debug mode).
- Add an admin command output field showing **why** tag eligibility failed.

