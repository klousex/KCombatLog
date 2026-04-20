package com.wobble.wcombatlog;

import com.wobble.wcombatlog.command.WCombatLogCommand;
import com.wobble.wcombatlog.api.service.SimpleWCombatLogService;
import com.wobble.wcombatlog.api.service.WCombatLogService;
import com.wobble.wcombatlog.combat.CombatManager;
import com.wobble.wcombatlog.combat.CrystalOwnershipTracker;
import com.wobble.wcombatlog.config.MessagesConfig;
import com.wobble.wcombatlog.hook.AuraCombatBridgeManager;
import com.wobble.wcombatlog.hook.WCombatLogPlaceholderExpansion;
import com.wobble.wcombatlog.listener.CombatListener;
import com.wobble.wcombatlog.listener.ConnectionListener;
import com.wobble.wcombatlog.listener.DeathListener;
import com.wobble.wcombatlog.listener.RestrictionListener;
import com.wobble.wcombatlog.listener.StateListener;
import com.wobble.wcombatlog.storage.CombatHistoryStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WCombatLogPlugin extends JavaPlugin {

    private CombatManager combatManager;
    private MessagesConfig messages;
    private CombatHistoryStorage historyStorage;
    private WCombatLogPlaceholderExpansion placeholderExpansion;
    private BukkitTask historyAutosaveTask;
    private WCombatLogService combatService;
    private AuraCombatBridgeManager auraCombatBridgeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.messages = new MessagesConfig(this);
        this.messages.load();

        this.historyStorage = new CombatHistoryStorage(this);
        this.historyStorage.reload();

        this.combatManager = new CombatManager(this);
        this.auraCombatBridgeManager = new AuraCombatBridgeManager(this);
        this.combatService = new SimpleWCombatLogService(combatManager);
        this.combatManager.loadHistory(historyStorage.load());
        this.combatManager.start();
        startHistoryAutosave();

        CrystalOwnershipTracker crystalOwnershipTracker = new CrystalOwnershipTracker(this);
        getServer().getPluginManager().registerEvents(crystalOwnershipTracker, this);
        getServer().getPluginManager().registerEvents(new CombatListener(this, combatManager, crystalOwnershipTracker), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(this, combatManager, auraCombatBridgeManager), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this, combatManager), this);
        getServer().getPluginManager().registerEvents(new StateListener(combatManager), this);
        getServer().getPluginManager().registerEvents(new RestrictionListener(this, combatManager), this);

        PluginCommand command = Objects.requireNonNull(getCommand("wcombatlog"), "wcombatlog command missing in plugin.yml");
        WCombatLogCommand executor = new WCombatLogCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        registerPlaceholderExpansion();
        logBypassPermissionSnapshot();
        getLogger().info("WCombatLog enabled.");
    }

    @Override
    public void onDisable() {
        if (historyAutosaveTask != null) {
            historyAutosaveTask.cancel();
            historyAutosaveTask = null;
        }
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
            placeholderExpansion = null;
        }
        if (combatManager != null) {
            saveHistoryNow();
            combatManager.shutdown();
        }
        if (historyStorage != null) {
            historyStorage.close();
        }
    }

    public void reloadAll() {
        saveHistoryNow();
        reloadConfig();
        messages.load();
        historyStorage.reload();
        combatManager.reload();
        combatManager.loadHistory(historyStorage.load());
        startHistoryAutosave();
        registerPlaceholderExpansion();
        logBypassPermissionSnapshot();
    }

    private void logBypassPermissionSnapshot() {
        List<String> bypassPlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("wcombatlog.bypass")) {
                bypassPlayers.add(player.getName());
            }
        }
        if (!bypassPlayers.isEmpty()) {
            getLogger().warning("Players currently inheriting wcombatlog.bypass: " + String.join(", ", bypassPlayers));
        }
    }

    private void startHistoryAutosave() {
        if (historyAutosaveTask != null) {
            historyAutosaveTask.cancel();
            historyAutosaveTask = null;
        }
        if (!historyStorage.isEnabled()) {
            return;
        }
        long period = Math.max(20L, historyStorage.getAutosaveSeconds() * 20L);
        historyAutosaveTask = Bukkit.getScheduler().runTaskTimer(this, this::saveHistoryNow, period, period);
    }

    public void saveHistoryNow() {
        if (historyStorage != null && historyStorage.isEnabled() && historyStorage.isDirty() && combatManager != null) {
            historyStorage.save(combatManager.snapshotHistory());
        }
    }

    private void registerPlaceholderExpansion() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
            placeholderExpansion = null;
        }

        boolean enabledInConfig = getConfig().getBoolean("settings.register-placeholderapi-expansion", true);
        if (!enabledInConfig) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        placeholderExpansion = new WCombatLogPlaceholderExpansion(this);
        placeholderExpansion.register();
        getLogger().info("Registered PlaceholderAPI expansion.");
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public MessagesConfig getMessages() {
        return messages;
    }

    public CombatHistoryStorage getHistoryStorage() {
        return historyStorage;
    }

    public WCombatLogService getCombatService() {
        return combatService;
    }

    public AuraCombatBridgeManager getAuraCombatBridgeManager() {
        return auraCombatBridgeManager;
    }
}
