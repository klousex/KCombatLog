package com.wobble.wcombatlog.hook;

import com.wobble.wcombatlog.WCombatLogPlugin;
import com.wobble.wcombatlog.api.integration.AuraCombatBridge;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class AuraCombatBridgeManager {

    private final WCombatLogPlugin plugin;

    public AuraCombatBridgeManager(WCombatLogPlugin plugin) {
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
