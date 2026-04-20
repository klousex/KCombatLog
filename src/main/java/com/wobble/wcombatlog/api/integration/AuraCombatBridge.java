package com.wobble.wcombatlog.api.integration;

import java.util.UUID;
import org.bukkit.entity.Player;

public interface AuraCombatBridge {

    boolean handleCombatLogout(Player player, UUID attackerUuid, String reason);
}
