package com.gachicraft.resourcesx.mining;

import com.gachicraft.resourcesx.config.ConfigManager;
import com.gachicraft.resourcesx.config.OreTypeConfig;
import com.gachicraft.resourcesx.config.PickaxeConfig;
import com.gachicraft.resourcesx.packet.CrackAnimationSender;
import com.gachicraft.resourcesx.util.ActionBarUtil;
import com.gachicraft.resourcesx.util.InventoryUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class MiningManager {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final CrackAnimationSender cracks;
    private final MiningLockService locks = new MiningLockService();
    private final Map<UUID, MiningSession> sessions = new HashMap<>();

    public MiningManager(JavaPlugin plugin, ConfigManager config, CrackAnimationSender cracks) { this.plugin = plugin; this.config = config; this.cracks = cracks; }

    public void handleSwing(Player player, Block block) {
        if (block == null) return;
        OrePoint point = config.registry().get(block.getLocation()).orElse(null);
        if (point == null || point.depleted()) return;
        if (!locks.tryLock(point.location(), player.getUniqueId())) { player.sendMessage(config.messages().format("mining.locked-by-other")); return; }

        ItemStack tool = player.getInventory().getItemInMainHand();
        PickaxeConfig pickaxe = config.pickaxe(tool.getType());
        OreTypeConfig oreType = config.oreType(point.ore());
        if (pickaxe == null || oreType == null || pickaxe.level() < oreType.level()) { player.sendMessage(config.messages().format("mining.too-weak-pickaxe")); locks.unlock(point.location()); return; }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, config.idleResetSeconds() * 20 + 10, 255, false, false, false));
        MiningSession session = sessions.computeIfAbsent(player.getUniqueId(), id -> new MiningSession(id, point));
        if (!session.orePoint().location().equals(point.location())) reset(player.getUniqueId(), true);
        session = sessions.computeIfAbsent(player.getUniqueId(), id -> new MiningSession(id, point));
        applyHit(player, session, pickaxe.force(), oreType);
    }

    private void applyHit(Player player, MiningSession session, double force, OreTypeConfig oreType) {
        session.addDamage(force);
        double percent = Math.min(100.0, (session.damage() / oreType.health()) * 100.0);
        ActionBarUtil.send(player, config.messages().format("mining.progress-bar", Map.of("bar", ActionBarUtil.bar(percent, config.barLength()), "percent", String.format("%.1f", percent))));
        cracks.sendStage(player, session.orePoint().location(), percent);
        restartIdleTask(player, session);
        if (session.damage() >= oreType.health()) complete(player, session, oreType);
    }

    private void restartIdleTask(Player player, MiningSession session) {
        if (session.idleTask() != null) session.idleTask().cancel();
        session.setIdleTask(plugin.getServer().getScheduler().runTaskLater(plugin, () -> { player.sendMessage(config.messages().format("mining.progress-reset")); reset(player.getUniqueId(), false); }, config.idleResetSeconds() * 20L));
    }

    private void complete(Player player, MiningSession session, OreTypeConfig oreType) {
        OrePoint point = session.orePoint();
        if (session.idleTask() != null) session.idleTask().cancel();
        Material drop = dropFor(point.ore());
        boolean full = InventoryUtil.giveOrDrop(player, new ItemStack(drop, point.randomDropCount()), point.location().toCenterLocation());
        if (full) player.sendMessage(config.messages().format("mining.inventory-full"));
        damageTool(player.getInventory().getItemInMainHand(), oreType.damage());
        point.location().getBlock().setType(config.replacementBlock(), false);
        point.setDepleted(true);
        cracks.clear(player, point.location());
        sessions.remove(player.getUniqueId());
        locks.unlock(point.location());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> { point.location().getBlock().setType(point.ore(), false); point.setDepleted(false); }, point.config().cooldownSeconds() * 20L);
    }

    public void reset(UUID playerId, boolean silent) {
        MiningSession session = sessions.remove(playerId);
        if (session == null) return;
        if (session.idleTask() != null) session.idleTask().cancel();
        locks.unlock(session.orePoint().location());
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) cracks.clear(player, session.orePoint().location());
    }

    public void resetAll() {
        for (UUID playerId : new java.util.HashSet<>(sessions.keySet())) {
            reset(playerId, true);
        }
    }

    public void shutdown() { resetAll(); }

    private void damageTool(ItemStack tool, int amount) {
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable)) return;
        Damageable damageable = (Damageable) meta;
        int unbreaking = tool.getEnchantmentLevel(Enchantment.DURABILITY);
        int applied = Math.max(1, amount - unbreaking);
        damageable.setDamage(damageable.getDamage() + applied);
        tool.setItemMeta(meta);
    }

    private Material dropFor(Material ore) {
        switch (ore) {
            case IRON_ORE:
                return Material.RAW_IRON;
            case GOLD_ORE:
                return Material.RAW_GOLD;
            case COPPER_ORE:
                return Material.RAW_COPPER;
            case COAL_ORE:
                return Material.COAL;
            case DIAMOND_ORE:
                return Material.DIAMOND;
            case EMERALD_ORE:
                return Material.EMERALD;
            case LAPIS_ORE:
                return Material.LAPIS_LAZULI;
            default:
                return ore;
        }
    }
}
