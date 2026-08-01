package com.gachicraft.resourcesx.tree;

import com.gachicraft.resourcesx.config.ConfigManager;
import com.gachicraft.resourcesx.config.PickaxeConfig;
import com.gachicraft.resourcesx.mining.MiningLockService;
import com.gachicraft.resourcesx.packet.CrackAnimationSender;
import com.gachicraft.resourcesx.util.ActionBarUtil;
import com.gachicraft.resourcesx.util.InventoryUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
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

public final class TreeManager {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final CrackAnimationSender cracks;
    private final MiningLockService locks = new MiningLockService();
    private final TreeRespawnScheduler respawns;
    private final Map<UUID, TreeSession> sessions = new HashMap<>();

    public TreeManager(JavaPlugin plugin, ConfigManager config, CrackAnimationSender cracks) {
        this.plugin = plugin; this.config = config; this.cracks = cracks; this.respawns = new TreeRespawnScheduler(plugin, config); this.respawns.start();
    }

    public boolean handleSwing(Player player, Block block) {
        if (block == null) return false;
        TreeInstance tree = config.treeRegistry().get(block.getLocation());
        if (tree == null) return false;
        if (tree.state() != TreeState.ALIVE) return true;
        TreeBlockDef def = tree.blockAt(block.getLocation());
        if (def == null || !def.breakable()) return true;
        if (!block.getBlockData().getAsString().equals(def.blockData().getAsString())) return true;
        if (!locks.tryLock(MiningLockService.treeKey(tree.id()), player.getUniqueId())) { player.sendMessage(config.messages().format("mining.locked-by-other")); return true; }

        ItemStack tool = player.getInventory().getItemInMainHand();
        PickaxeConfig axe = config.axe(tool.getType());
        if (axe == null || axe.level() < tree.blueprint().level()) { player.sendMessage(config.messages().format("mining.too-weak-pickaxe")); locks.unlock(MiningLockService.treeKey(tree.id())); return true; }

        TreeSession session = sessions.computeIfAbsent(player.getUniqueId(), id -> new TreeSession(id, tree));
        if (session.tree().id() != tree.id()) reset(player.getUniqueId(), true);
        session = sessions.computeIfAbsent(player.getUniqueId(), id -> new TreeSession(id, tree));

        long now = System.currentTimeMillis();
        if (now - session.lastHitMillis() < config.hitCooldownMillis()) return true;
        session.setLastHitMillis(now);

        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 6, 255, false, false, false));
        applyHit(player, session, block.getLocation(), axe.force());
        return true;
    }

    private void applyHit(Player player, TreeSession session, Location hitLocation, double force) {
        session.addDamage(force);
        double percent = Math.min(100.0, (session.damage() / session.tree().blueprint().health()) * 100.0);
        if (session.crackLocation() != null && !sameBlock(session.crackLocation(), hitLocation)) cracks.clear(player, session.crackLocation());
        session.setCrackLocation(hitLocation);
        ActionBarUtil.send(player, config.messages().format("mining.progress-bar", Map.of("bar", ActionBarUtil.bar(percent, config.barLength()), "percent", String.format("%.1f", percent))));
        cracks.sendStage(player, hitLocation, percent);
        restartIdleTask(player, session);
        if (session.damage() >= session.tree().blueprint().health()) fellTree(player, session);
    }

    private void restartIdleTask(Player player, TreeSession session) {
        if (session.idleTask() != null) session.idleTask().cancel();
        session.setIdleTask(plugin.getServer().getScheduler().runTaskLater(plugin, () -> { player.sendMessage(config.messages().format("mining.progress-reset")); reset(player.getUniqueId(), false); }, config.idleResetSeconds() * 20L));
    }

    private void fellTree(Player player, TreeSession session) {
        TreeInstance tree = session.tree();
        if (session.idleTask() != null) session.idleTask().cancel();
        Material drop = dropMaterial(tree);
        boolean full = InventoryUtil.giveOrDrop(player, new ItemStack(drop, tree.randomDropAmount()), tree.center().toCenterLocation());
        if (full) player.sendMessage(config.messages().format("mining.inventory-full"));
        damageTool(player, player.getInventory().getItemInMainHand(), tree.blueprint().axeDamage());
        if (session.crackLocation() != null) cracks.clear(player, session.crackLocation());
        tree.fell();
        sessions.remove(player.getUniqueId());
        locks.unlock(MiningLockService.treeKey(tree.id()));
        respawns.schedule(tree);
    }

    public void removeTree(int id) {
        TreeInstance tree = config.treeRegistry().getById(id);
        if (tree == null) return;
        respawns.cancel(tree);
        for (UUID playerId : new java.util.HashSet<>(sessions.keySet())) if (sessions.get(playerId).tree().id() == id) reset(playerId, true);
        tree.clearAll();
        config.treeRegistry().unregister(tree);
        locks.unlock(MiningLockService.treeKey(id));
    }

    public void reloadRespawns() { respawns.reload(); }
    public void reset(UUID playerId, boolean silent) {
        TreeSession session = sessions.remove(playerId);
        if (session == null) return;
        if (session.idleTask() != null) session.idleTask().cancel();
        if (session.crackLocation() != null) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) cracks.clear(player, session.crackLocation());
        }
        locks.unlock(MiningLockService.treeKey(session.tree().id()));
    }
    public void resetAll() { for (UUID playerId : new java.util.HashSet<>(sessions.keySet())) reset(playerId, true); }
    public void shutdown() { resetAll(); respawns.shutdown(); }

    private boolean sameBlock(Location first, Location second) { return first.getWorld().equals(second.getWorld()) && first.getBlockX() == second.getBlockX() && first.getBlockY() == second.getBlockY() && first.getBlockZ() == second.getBlockZ(); }
    private Material dropMaterial(TreeInstance tree) { for (TreeBlockDef def : tree.blueprint().blocks()) if (def.breakable()) return def.blockData().getMaterial(); return Material.OAK_LOG; }

    private void damageTool(Player player, ItemStack tool, int amount) {
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable)) return;
        int maxDurability = tool.getType().getMaxDurability();
        if (maxDurability <= 0) return;
        Damageable damageable = (Damageable) meta;
        int applied = Math.max(1, amount - tool.getEnchantmentLevel(Enchantment.UNBREAKING));
        int newDamage = damageable.getDamage() + applied;
        if (newDamage >= maxDurability) { tool.setAmount(Math.max(0, tool.getAmount() - 1)); player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f); player.updateInventory(); return; }
        damageable.setDamage(newDamage); tool.setItemMeta(meta);
    }
}
