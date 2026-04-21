package com.klouse.kcombatlog.combat;

import java.util.UUID;

public record CombatTag(long expiresAtMillis, UUID lastAttacker) {
}
