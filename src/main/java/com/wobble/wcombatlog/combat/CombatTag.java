package com.wobble.wcombatlog.combat;

import java.util.UUID;

public record CombatTag(long expiresAtMillis, UUID lastAttacker) {
}
