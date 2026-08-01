package com.gachicraft.resourcesx;

import com.gachicraft.resourcesx.command.ResourcesXCommand;
import com.gachicraft.resourcesx.config.ConfigManager;
import com.gachicraft.resourcesx.listener.BlockBreakGuardListener;
import com.gachicraft.resourcesx.listener.PlayerQuitListener;
import com.gachicraft.resourcesx.listener.PlayerSwingListener;
import com.gachicraft.resourcesx.listener.PlayerWorldChangeListener;
import com.gachicraft.resourcesx.mining.MiningManager;
import com.gachicraft.resourcesx.packet.CrackAnimationSender;
import com.gachicraft.resourcesx.tree.TreeManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResourcesX extends JavaPlugin {
    private ConfigManager configManager;
    private MiningManager miningManager;
    private TreeManager treeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("ores.yml", false);
        saveResource("messages.yml", false);
        saveResource("trees.yml", false);

        configManager = new ConfigManager(this);
        configManager.reload();
        CrackAnimationSender crackAnimationSender = new CrackAnimationSender(configManager);
        miningManager = new MiningManager(this, configManager, crackAnimationSender);
        treeManager = new TreeManager(this, configManager, crackAnimationSender);

        getServer().getPluginManager().registerEvents(new PlayerSwingListener(treeManager, miningManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakGuardListener(configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(miningManager, treeManager), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(miningManager, treeManager), this);
        getCommand("resourcesx").setExecutor(new ResourcesXCommand(configManager, miningManager, treeManager));
    }

    @Override
    public void onDisable() {
        if (treeManager != null) treeManager.shutdown();
        if (miningManager != null) miningManager.shutdown();
    }
}
