package com.wobble.wcombatlog.api.service;

import com.wobble.wcombatlog.combat.CombatHistoryEntry;
import org.bukkit.entity.Player;

import java.util.List;

public interface WCombatLogService {
    boolean isTagged(Player player);
    int getRemainingSeconds(Player player);
    String getLastAttackerName(Player player);
    List<CombatHistoryEntry> getHistory(Player player);
    void clearTag(Player player);
    void forceTag(Player attacker, Player victim);
    int getDurationSeconds();
}
