package it.zZalix.orbitalstrike;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class FishingRodListener implements Listener {

    private final OrbitalStrikePlugin plugin;
    private final Map<UUID, Location> hookLandings = new ConcurrentHashMap<>();

    public FishingRodListener(OrbitalStrikePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof FishHook hook) {
            Location hitLoc = null;
            if (event.getHitBlock() != null) {
                hitLoc = event.getHitBlock().getLocation();
            } else if (event.getHitEntity() != null) {
                hitLoc = event.getHitEntity().getLocation();
            }
            if (hitLoc != null) {
                hookLandings.put(hook.getUniqueId(), hitLoc);
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.FISHING_ROD) {
            item = player.getInventory().getItemInOffHand();
        }

        if (item.getType() != Material.FISHING_ROD) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey typeKey = new NamespacedKey(plugin, "orbital_type");

        if (!pdc.has(typeKey, PersistentDataType.STRING)) return;

        String type = pdc.get(typeKey, PersistentDataType.STRING);
        NamespacedKey extraKey = new NamespacedKey(plugin, "orbital_extra");
        String extra = pdc.has(extraKey, PersistentDataType.STRING) ? pdc.get(extraKey, PersistentDataType.STRING) : "";

        if (event.getState() == PlayerFishEvent.State.REEL_IN
                || event.getState() == PlayerFishEvent.State.IN_GROUND
                || event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {

            FishHook hook = event.getHook();
            Location targetLoc = hookLandings.remove(hook.getUniqueId());

            if (targetLoc == null) {
                Block targetBlock = player.getTargetBlockExact(150);
                if (targetBlock != null) {
                    targetLoc = targetBlock.getLocation();
                } else {
                    targetLoc = hook.getLocation();
                }
            }

            boolean used = executeStrike(player, type, extra, targetLoc);

            if (used) {
                item.setAmount(item.getAmount() - 1);
                event.getHook().remove();
            }
        }
    }

    private boolean executeStrike(Player player, String type, String extra, Location target) {
        World world = target.getWorld();
        if (world == null) return false;

        switch (type.toUpperCase()) {
            case "NUKE" -> triggerNuke(world, target);
            case "STAB" -> triggerStab(world, target);
            case "WOLF" -> triggerWolf(player, world, target);
            case "STASIS" -> triggerStasis(player, extra);
            case "CUSTOM" -> triggerCustom(world, target, extra);
            default -> {
                return false;
            }
        }
        return true;
    }

    private void triggerNuke(World world, Location target) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int[] ringCounts = {12, 24, 36, 48, 60, 72, 84, 96, 108, 120};
                double radiusStep = 4.0;
                double spawnY = target.getY() + 100.0;
                ThreadLocalRandom random = ThreadLocalRandom.current();

                // Spawna esattamente 10 TNT compresse e sovrapposte al centro esatto
                Location centerLoc = new Location(world, target.getX(), spawnY, target.getZ());
                for (int c = 0; c < 10; c++) {
                    TNTPrimed centerTnt = world.spawn(centerLoc, TNTPrimed.class);
                    centerTnt.setVelocity(new org.bukkit.util.Vector(0, -1.5, 0));
                    centerTnt.setFuseTicks(85);
                }

                for (int ring = 0; ring < ringCounts.length; ring++) {
                    int count = ringCounts[ring];
                    double radius = (ring + 1) * radiusStep;

                    Set<Integer> offsetIndices = new HashSet<>();
                    while (offsetIndices.size() < 6 && offsetIndices.size() < count) {
                        offsetIndices.add(random.nextInt(count));
                    }

                    for (int i = 0; i < count; i++) {
                        double angle = i * (2 * Math.PI / count);

                        if (offsetIndices.contains(i)) {
                            double shift = -0.12 - (random.nextDouble() * 0.08);
                            angle += shift;
                        }

                        double offsetX = Math.cos(angle) * radius;
                        double offsetZ = Math.sin(angle) * radius;

                        Location spawnLoc = new Location(world, target.getX() + offsetX, spawnY, target.getZ() + offsetZ);
                        TNTPrimed tnt = world.spawn(spawnLoc, TNTPrimed.class);

                        tnt.setVelocity(new org.bukkit.util.Vector(0, -1.5, 0));
                        tnt.setFuseTicks(85);
                    }
                }
            }
        }.runTask(plugin);
    }

    private void triggerStab(World world, Location target) {
        new BukkitRunnable() {
            int currentY = Math.min(world.getMaxHeight(), target.getBlockY() + 10);
            final int minHeight = world.getMinHeight();

            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    if (currentY < minHeight) {
                        this.cancel();
                        return;
                    }

                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            Block block = target.clone().add(x, currentY - target.getBlockY(), z).getBlock();
                            if (block.getType() != Material.BEDROCK) {
                                block.setType(Material.AIR);
                            }
                        }
                    }

                    if (currentY % 6 == 0) {
                        world.createExplosion(target.clone().add(0, currentY - target.getBlockY(), 0), 4F, false, false);
                    }
                    currentY--;
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void triggerWolf(Player player, World world, Location target) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < 200; i++) {
                    Wolf wolf = world.spawn(target.clone().add(0, 1, 0), Wolf.class);
                    wolf.setTamed(true);
                    wolf.setOwner(player);
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 1));
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 1));
                }
            }
        }.runTask(plugin);
    }

    private void triggerStasis(Player player, String extra) {
        if (extra == null || extra.isEmpty()) return;
        String[] parts = extra.split(",");
        if (parts.length < 3) return;

        try {
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);

            Location loc = new Location(player.getWorld(), x, y, z);
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(loc);
                }
            }.runTask(plugin);
        } catch (NumberFormatException ignored) {}
    }

    private void triggerCustom(World world, Location target, String mobName) {
        EntityType type;
        try {
            String cleanName = mobName.replace("minecraft:", "").toUpperCase();
            type = EntityType.valueOf(cleanName);
        } catch (Exception e) {
            type = EntityType.ZOMBIE;
        }

        final EntityType finalType = type;
        new BukkitRunnable() {
            @Override
            public void run() {
                int[] ringCounts = {12, 24, 36, 48, 60, 72, 84, 96, 108, 120};
                double radiusStep = 4.0;
                double spawnY = target.getY() + 100.0;
                ThreadLocalRandom random = ThreadLocalRandom.current();

                // Spawna esattamente 10 mob compressi e sovrapposti al centro esatto
                Location centerLoc = new Location(world, target.getX(), spawnY, target.getZ());
                for (int c = 0; c < 10; c++) {
                    Entity centerMob = world.spawnEntity(centerLoc, finalType);
                    centerMob.setVelocity(new org.bukkit.util.Vector(0, -1.5, 0));
                    if (centerMob instanceof LivingEntity living) {
                        living.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 4));
                    }
                }

                for (int ring = 0; ring < ringCounts.length; ring++) {
                    int count = ringCounts[ring];
                    double radius = (ring + 1) * radiusStep;

                    Set<Integer> offsetIndices = new HashSet<>();
                    while (offsetIndices.size() < 6 && offsetIndices.size() < count) {
                        offsetIndices.add(random.nextInt(count));
                    }

                    for (int i = 0; i < count; i++) {
                        double angle = i * (2 * Math.PI / count);

                        if (offsetIndices.contains(i)) {
                            double shift = -0.12 - (random.nextDouble() * 0.08);
                            angle += shift;
                        }

                        double offsetX = Math.cos(angle) * radius;
                        double offsetZ = Math.sin(angle) * radius;

                        Location spawnLoc = new Location(world, target.getX() + offsetX, spawnY, target.getZ() + offsetZ);
                        Entity spawned = world.spawnEntity(spawnLoc, finalType);

                        spawned.setVelocity(new org.bukkit.util.Vector(0, -1.5, 0));

                        if (spawned instanceof LivingEntity living) {
                            living.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 4));
                        }
                    }
                }
            }
        }.runTask(plugin);
    }
}