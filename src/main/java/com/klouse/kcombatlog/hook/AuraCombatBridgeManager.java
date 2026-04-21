package com.klouse.kcombatlog.hook;

import com.klouse.kcombatlog.KCombatLogPlugin;
import com.klouse.kcombatlog.api.integration.AuraCombatBridge;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class AuraCombatBridgeManager {

    private final KCombatLogPlugin plugin;

    public AuraCombatBridgeManager(KCombatLogPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean handleCombatLogout(Player player, UUID attackerUuid, String reason) {
        AuraCombatBridge bridge = Bukkit.getServicesManager().load(AuraCombatBridge.class);
        if (bridge == null) {
            return false;
        }

        try {
            return bridge.handleCombatLogout(player, attackerUuid, reason == null ? "" : reason);
        } catch (Exception exception) {
            plugin.getLogger().warning("Aura combat bridge failed during logout delegation: " + exception.getMessage());
            return false;
        }
    }
}
