package com.klouse.kcombatlog;

import com.klouse.kcombatlog.command.KCombatLogCommand;
import com.klouse.kcombatlog.api.service.SimpleKCombatLogService;
import com.klouse.kcombatlog.api.service.KCombatLogService;
import com.klouse.kcombatlog.combat.CombatManager;
import com.klouse.kcombatlog.combat.CrystalOwnershipTracker;
import com.klouse.kcombatlog.config.MessagesConfig;
import com.klouse.kcombatlog.hook.AuraCombatBridgeManager;
import com.klouse.kcombatlog.hook.KCombatLogPlaceholderExpansion;
import com.klouse.kcombatlog.listener.CombatListener;
import com.klouse.kcombatlog.listener.ConnectionListener;
import com.klouse.kcombatlog.listener.DeathListener;
import com.klouse.kcombatlog.listener.RestrictionListener;
import com.klouse.kcombatlog.listener.StateListener;
import com.klouse.kcombatlog.storage.CombatHistoryStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class KCombatLogPlugin extends JavaPlugin {

    private CombatManager combatManager;
    private MessagesConfig messages;
    private CombatHistoryStorage historyStorage;
    private KCombatLogPlaceholderExpansion placeholderExpansion;
    private BukkitTask historyAutosaveTask;
    private KCombatLogService combatService;
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
        this.combatService = new SimpleKCombatLogService(combatManager);
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

        PluginCommand command = Objects.requireNonNull(getCommand("kcombatlog"), "kcombatlog command missing in plugin.yml");
        KCombatLogCommand executor = new KCombatLogCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        registerPlaceholderExpansion();
        logBypassPermissionSnapshot();
        getLogger().info("KCombatLog enabled.");
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
            if (player.hasPermission("kcombatlog.bypass")) {
                bypassPlayers.add(player.getName());
            }
        }
        if (!bypassPlayers.isEmpty()) {
            getLogger().warning("Players currently inheriting kcombatlog.bypass: " + String.join(", ", bypassPlayers));
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

        placeholderExpansion = new KCombatLogPlaceholderExpansion(this);
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

    public KCombatLogService getCombatService() {
        return combatService;
    }

    public AuraCombatBridgeManager getAuraCombatBridgeManager() {
        return auraCombatBridgeManager;
    }
}
