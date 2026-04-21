package com.klouse.kcombatlog.combat;

public record CombatHistoryEntry(long timestampMillis, String player, String attacker, String type, String details) {
}
