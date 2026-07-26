package com.gachicraft.resourcesx.command;

import com.gachicraft.resourcesx.config.ConfigManager;
import com.gachicraft.resourcesx.mining.MiningManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class ResourcesXCommand implements CommandExecutor {
    private final ConfigManager config;
    private final MiningManager mining;

    public ResourcesXCommand(ConfigManager config, MiningManager mining) {
        this.config = config;
        this.mining = mining;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("resourcesx.admin")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            mining.resetAll();
            config.reload();
            mining.reloadRespawns();
            sender.sendMessage(config.messages().format("admin.reload-success"));
            return true;
        }
        sender.sendMessage("§eUsage: /" + label + " reload");
        return true;
    }
}
