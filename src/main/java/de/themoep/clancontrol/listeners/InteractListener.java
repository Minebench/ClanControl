package de.themoep.clancontrol.listeners;

import de.themoep.clancontrol.ClanControl;
import de.themoep.clancontrol.OccupiedChunk;
import de.themoep.clancontrol.Region;
import de.themoep.clancontrol.RegionStatus;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

/**
 * ClanControl
 * Copyright (C) 2015 Max Lee (https://github.com/Phoenix616/)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class InteractListener implements Listener {

    private ClanControl plugin;

    private static List<Material> allowUseMaterials = Arrays.asList(new Material[]{
            Material.BOW,
            Material.POTION,
            Material.DIAMOND_SWORD,
            Material.GOLD_SWORD,
            Material.IRON_SWORD,
            Material.STONE_SWORD,
            Material.WOOD_SWORD,
    });
    private static List<Material> containerMaterials = Arrays.asList(new Material[]{
            Material.CHEST, 
            Material.TRAPPED_CHEST, 
            Material.HOPPER, 
            Material.DROPPER, 
            Material.BEACON, 
            Material.DISPENSER, 
            Material.FURNACE, 
            Material.BREWING_STAND, 
            Material.CAULDRON,
            Material.COMMAND
    });
    private static List<Material> doorMaterials = Arrays.asList(new Material[]{
            Material.ACACIA_DOOR, 
            Material.BIRCH_DOOR, 
            Material.DARK_OAK_DOOR, 
            Material.IRON_DOOR, 
            Material.JUNGLE_DOOR, 
            Material.SPRUCE_DOOR, 
            Material.TRAP_DOOR, 
            Material.IRON_TRAPDOOR, 
            Material.WOOD_DOOR, 
            Material.WOODEN_DOOR, 
            Material.FENCE_GATE, 
            Material.ACACIA_FENCE_GATE,
            Material.BIRCH_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE,
            Material.SPRUCE_FENCE_GATE
    });
    private static List<Material> redstoneMaterials = Arrays.asList(new Material[]{
            Material.LEVER,
            Material.STONE_BUTTON,
            Material.WOOD_BUTTON,
            Material.DIODE_BLOCK_ON,
            Material.DIODE_BLOCK_OFF,
            Material.REDSTONE_COMPARATOR_ON,
            Material.REDSTONE_COMPARATOR_OFF,
            Material.COMMAND
    });
    private static List<EntityType> blockEntityTypes = Arrays.asList(new EntityType[]{
            EntityType.ARMOR_STAND,
            EntityType.PAINTING,
            EntityType.ITEM_FRAME,
            EntityType.ENDER_CRYSTAL,
            EntityType.MINECART_CHEST,
            EntityType.MINECART_FURNACE,
            EntityType.MINECART_COMMAND,
            EntityType.MINECART_HOPPER,
            EntityType.MINECART_MOB_SPAWNER,
            EntityType.MINECART_TNT,
            EntityType.MINECART
    });

    public InteractListener(ClanControl plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerRightclick(PlayerInteractEvent event) {
        if(!event.isCancelled()) {
            if(plugin.protectUse
                    && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                    && !event.getItem().getType().isEdible()
                    && !allowUseMaterials.contains(event.getItem().getType())
                    ) {
                OccupiedChunk chunk = plugin.getRegionManager().getChunk(event.getPlayer().getLocation());
                Region region = plugin.getRegionManager().getRegion(event.getPlayer().getLocation());
                String clan = plugin.getClan(event.getPlayer());
                if(chunk != null && !clan.equals(chunk.getClan()) || region != null && region.getStatus() == RegionStatus.CENTER && !clan.equals(region.getController())) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to use this here!");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInteractAtBlock(PlayerInteractEvent event) {
        if(!event.isCancelled()) {
            if(event.getClickedBlock() != null && event.getClickedBlock().getWorld().equals(plugin.getRegionManager().getWorld())) {
                Material m = event.getClickedBlock().getType();
                if(plugin.protectEverything 
                        || plugin.protectUse
                        || plugin.protectContainer && containerMaterials.contains(m) 
                        || plugin.protectDoors && doorMaterials.contains(m)
                        || plugin.protectRedstone && redstoneMaterials.contains(m)
                        ) {
                    OccupiedChunk chunk = plugin.getRegionManager().getChunk(event.getClickedBlock().getLocation());
                    Region region = plugin.getRegionManager().getRegion(event.getClickedBlock().getLocation());
                    String clan = plugin.getClan(event.getPlayer());
                    if(chunk != null && !chunk.getClan().equals(clan) || region != null && region.getStatus() == RegionStatus.CENTER && !region.getController().equals(clan)) {
                        event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to do this here!");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if(!event.isCancelled()) {
            if(event.getRightClicked() != null && event.getRightClicked().getWorld().equals(plugin.getRegionManager().getWorld())) {
                if(plugin.protectEverything
                        || plugin.protectEntities
                        || plugin.protectBlocks && blockEntityTypes.contains(event.getRightClicked().getType())
                        ) {
                    OccupiedChunk chunk = plugin.getRegionManager().getChunk(event.getRightClicked().getLocation());
                    Region region = plugin.getRegionManager().getRegion(event.getRightClicked().getLocation());
                    String clan = plugin.getClan(event.getPlayer());
                    boolean chunkNotControlledByPlayerClan = chunk != null && !chunk.getClan().equals(clan);
                    boolean regionNotControlledByPlayerClan = region != null && region.getStatus() == RegionStatus.CENTER && !region.getController().equals(clan);
                    if(chunkNotControlledByPlayerClan || regionNotControlledByPlayerClan) {
                        event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to do this here!");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!event.isCancelled() && event.getEntity().getWorld().equals(plugin.getRegionManager().getWorld())) {
            if(event.getDamager() instanceof Player) {
                if(plugin.protectEverything
                        || plugin.protectEntities
                        || plugin.protectBlocks && blockEntityTypes.contains(event.getEntityType())
                        ) {
                    OccupiedChunk chunk = plugin.getRegionManager().getChunk(event.getEntity().getLocation());
                    Region region = plugin.getRegionManager().getRegion(event.getEntity().getLocation());
                    String clan = plugin.getClan((Player) event.getDamager());
                    boolean chunkNotControlledByPlayerClan = chunk != null && !chunk.getClan().equals(clan);
                    boolean regionNotControlledByPlayerClan = region != null && region.getStatus() == RegionStatus.CENTER && !region.getController().equals(clan);
                    if(chunkNotControlledByPlayerClan || regionNotControlledByPlayerClan) {
                        event.getDamager().sendMessage(ChatColor.RED + "You are not allowed to damage " + event.getEntityType().toString() + " in this area!");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(!event.isCancelled() && event.getEntity().getWorld().equals(plugin.getRegionManager().getWorld())) {
            if(plugin.protectExplosions && (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                OccupiedChunk chunk = plugin.getRegionManager().getChunk(event.getEntity().getLocation());
                Region region = plugin.getRegionManager().getRegion(event.getEntity().getLocation());
                if(chunk != null || (region != null && region.getStatus() == RegionStatus.CENTER)) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
        }
    }
}
