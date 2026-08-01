package com.gachicraft.resourcesx.mining;

import com.gachicraft.resourcesx.config.ConfigManager;
import com.gachicraft.resourcesx.config.OreTypeConfig;
import com.gachicraft.resourcesx.config.PickaxeConfig;
import com.gachicraft.resourcesx.packet.CrackAnimationSender;
import com.gachicraft.resourcesx.respawn.RespawnScheduler;
import com.gachicraft.resourcesx.util.ActionBarUtil;
import com.gachicraft.resourcesx.util.InventoryUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.Sound;
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
    private final RespawnScheduler respawns;
    private final Map<UUID, MiningSession> sessions = new HashMap<>();

    public MiningManager(JavaPlugin plugin, ConfigManager config, CrackAnimationSender cracks) {
        this.plugin = plugin;
        this.config = config;
        this.cracks = cracks;
        this.respawns = new RespawnScheduler(plugin, config);
        this.respawns.start();
    }

    public void handleSwing(Player player, Block block) {
        if (block == null) return;
        OrePoint point = config.registry().get(block.getLocation()).orElse(null);
        if (point == null || point.depleted()) return;
        if (block.getType() != point.ore()) return;
        if (!locks.tryLock(MiningLockService.oreKey(point.location()), player.getUniqueId())) { player.sendMessage(config.messages().format("mining.locked-by-other")); return; }

        ItemStack tool = player.getInventory().getItemInMainHand();
        PickaxeConfig pickaxe = config.pickaxe(tool.getType());
        OreTypeConfig oreType = config.oreType(point.ore());
        if (pickaxe == null || oreType == null || pickaxe.level() < oreType.level()) { player.sendMessage(config.messages().format("mining.too-weak-pickaxe")); locks.unlock(MiningLockService.oreKey(point.location())); return; }

        MiningSession session = sessions.computeIfAbsent(player.getUniqueId(), id -> new MiningSession(id, point));
        if (!session.orePoint().location().equals(point.location())) reset(player.getUniqueId(), true);
        session = sessions.computeIfAbsent(player.getUniqueId(), id -> new MiningSession(id, point));

        long now = System.currentTimeMillis();
        if (now - session.lastHitMillis() < config.hitCooldownMillis()) return;
        session.setLastHitMillis(now);

        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 6, 255, false, false, false));
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
        damageTool(player, player.getInventory().getItemInMainHand(), oreType.damage());
        point.location().getBlock().setType(config.replacementBlock(), false);
        point.setDepleted(true);
        cracks.clear(player, point.location());
        sessions.remove(player.getUniqueId());
        locks.unlock(MiningLockService.oreKey(point.location()));
        respawns.schedule(point, point.config().cooldownSeconds());
    }

    public void reset(UUID playerId, boolean silent) {
        MiningSession session = sessions.remove(playerId);
        if (session == null) return;
        if (session.idleTask() != null) session.idleTask().cancel();
        locks.unlock(session.orePoint().location());
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) cracks.clear(player, session.orePoint().location());
    }

    public void reloadRespawns() {
        respawns.reload();
    }

    public void resetAll() {
        for (UUID playerId : new java.util.HashSet<>(sessions.keySet())) {
            reset(playerId, true);
        }
    }

    public void shutdown() {
        resetAll();
        respawns.shutdown();
    }

    private void damageTool(Player player, ItemStack tool, int amount) {
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable)) return;
        int maxDurability = tool.getType().getMaxDurability();
        if (maxDurability <= 0) return;

        Damageable damageable = (Damageable) meta;
        int unbreaking = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
        int applied = Math.max(1, amount - unbreaking);
        int newDamage = damageable.getDamage() + applied;

        if (newDamage >= maxDurability) {
            tool.setAmount(Math.max(0, tool.getAmount() - 1));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.updateInventory();
            return;
        }

        damageable.setDamage(newDamage);
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
