package com.klouse.kcombatlog.api.service;

import com.klouse.kcombatlog.combat.CombatHistoryEntry;
import com.klouse.kcombatlog.combat.CombatManager;
import org.bukkit.entity.Player;

import java.util.List;

public final class SimpleKCombatLogService implements KCombatLogService {

    private final CombatManager combatManager;

    public SimpleKCombatLogService(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @Override
    public boolean isTagged(Player player) {
        return combatManager.isTagged(player);
    }

    @Override
    public int getRemainingSeconds(Player player) {
        return combatManager.getRemainingSeconds(player);
    }

    @Override
    public String getLastAttackerName(Player player) {
        return combatManager.getLastAttackerName(player);
    }

    @Override
    public List<CombatHistoryEntry> getHistory(Player player) {
        return combatManager.getHistory(player);
    }

    @Override
    public void clearTag(Player player) {
        combatManager.clear(player);
    }

    @Override
    public void forceTag(Player attacker, Player victim) {
        combatManager.tag(attacker, victim);
    }

    @Override
    public int getDurationSeconds() {
        return combatManager.getDurationSeconds();
    }
}
