package com.gachicraft.resourcesx.command;

import com.gachicraft.resourcesx.config.ConfigManager;
import com.gachicraft.resourcesx.mining.MiningManager;
import com.gachicraft.resourcesx.tree.TreeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class ResourcesXCommand implements CommandExecutor {
    private final ConfigManager config;
    private final MiningManager mining;
    private final TreeManager trees;

    public ResourcesXCommand(ConfigManager config, MiningManager mining, TreeManager trees) {
        this.config = config; this.mining = mining; this.trees = trees;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("resourcesx.admin")) { sender.sendMessage("§cYou do not have permission to use this command."); return true; }
            mining.resetAll(); trees.resetAll(); config.reload(); mining.reloadRespawns(); trees.reloadRespawns();
            sender.sendMessage(config.messages().format("admin.reload-success"));
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("removetree")) {
            if (!sender.hasPermission("resourcesx.admin")) { sender.sendMessage("§cYou do not have permission to use this command."); return true; }
            try { trees.removeTree(Integer.parseInt(args[1])); sender.sendMessage("§aTree removed from runtime registry."); }
            catch (NumberFormatException exception) { sender.sendMessage("§cTree id must be an integer."); }
            return true;
        }
        sender.sendMessage("§eUsage: /" + label + " reload §7or §e/" + label + " removetree <id>");
        return true;
    }
}
