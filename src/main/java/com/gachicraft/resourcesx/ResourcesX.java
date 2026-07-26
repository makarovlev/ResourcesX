package com.gachicraft.resourcesx;

import com.gachicraft.resourcesx.config.ConfigManager;
import com.gachicraft.resourcesx.listener.BlockBreakGuardListener;
import com.gachicraft.resourcesx.listener.PlayerQuitListener;
import com.gachicraft.resourcesx.listener.PlayerSwingListener;
import com.gachicraft.resourcesx.listener.PlayerWorldChangeListener;
import com.gachicraft.resourcesx.mining.MiningManager;
import com.gachicraft.resourcesx.packet.CrackAnimationSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResourcesX extends JavaPlugin {
    private ConfigManager configManager;
    private MiningManager miningManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("ores.yml", false);
        saveResource("messages.yml", false);

        configManager = new ConfigManager(this);
        configManager.reload();
        miningManager = new MiningManager(this, configManager, new CrackAnimationSender(configManager));

        getServer().getPluginManager().registerEvents(new PlayerSwingListener(miningManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakGuardListener(configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(miningManager), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(miningManager), this);
    }

    @Override
    public void onDisable() {
        if (miningManager != null) {
            miningManager.shutdown();
        }
    }
}
