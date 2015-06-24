package de.themoep.clancontrol.listeners;

import de.themoep.clancontrol.ClanControl;
import de.themoep.clancontrol.OccupiedChunk;
import de.themoep.clancontrol.Region;
import de.themoep.clancontrol.RegionStatus;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
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
public class BlockListener implements Listener {

    private ClanControl plugin;
    
    private static List<Material> beaconBaseMaterial = Arrays.asList(new Material[] {Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK});

    public BlockListener(ClanControl plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(!event.isCancelled()) {
            if(event.getBlock().getWorld().equals(plugin.getRegionManager().getWorld())) {
                OccupiedChunk chunk = plugin.getRegionManager().getChunk(event.getBlock().getLocation());
                Region region = plugin.getRegionManager().getRegion(event.getBlock().getLocation());
                String clan = plugin.getClan(event.getPlayer());
                boolean chunkNotControlledByPlayerClan = chunk != null && !chunk.getClan().equals(clan);
                boolean regionNotControlledByPlayerClan = region != null && region.getStatus() == RegionStatus.CENTER && !region.getController().equals(clan);
                if (plugin.protectBlocks && (chunkNotControlledByPlayerClan || regionNotControlledByPlayerClan)) {
                    event.setCancelled(true);
                } else if (clan != null && (event.getBlock().getType() == Material.BEACON || beaconBaseMaterial.contains(event.getBlock().getType()))) {
                    List<Block> beacons = getCompletedBeacons(event.getBlock());
                    for (Block b : beacons) {
                        boolean success = plugin.getRegionManager().registerBeacon(clan, b.getLocation());
                        if (success) {
                            event.getPlayer().sendMessage(ChatColor.YELLOW + "You registered this chunk for " + plugin.getClanDisplay(clan));
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the Beacon from a block which could be contained in a beacon structure
     * @param block The block to get a potential beacon from (the base or the beacon itself)
     * @return The a list of beacon blocks (empty list if none found)
     */
    private List<Block> getCompletedBeacons(Block block) {
        List<Block> beacons = new ArrayList<Block>();
        if(block != null) {
            if(block.getType() == Material.BEACON) {
                int x = block.getX() - 1;
                int y = block.getY() - 1;
                int z = block.getZ() - 1;
                for(int i = 0; i < 3; i++) {
                    for(int j = 0; j < 3; j++) {
                        if(!beaconBaseMaterial.contains(block.getWorld().getBlockAt(x + i, y, z + j).getType())) {
                            return beacons;
                        }
                    }
                }
                beacons.add(block);
            } else if(beaconBaseMaterial.contains(block.getType())) {
                List<Block> foundBeacons = new ArrayList<Block>();
                int x = block.getX() - 1;
                int y = block.getY() + 1;
                int z = block.getZ() - 1;
                for(int i = 0; i < 3; i++) {
                    for(int j = 0; j < 3; j++) {
                        Block check = block.getWorld().getBlockAt(x + i, y, z + j);
                        if(check.getType() == Material.BEACON) {
                            foundBeacons.add(check);
                        }
                    }
                }
                for(Block b : foundBeacons) {
                    List<Block> completedBeacons = getCompletedBeacons(b);
                    if(completedBeacons != null) {
                        beacons.addAll(completedBeacons);
                    }
                }
            }
        }
        return beacons;
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if(!event.isCancelled()) {
            if(event.getBlock().getWorld().equals(plugin.getRegionManager().getWorld())) {
                OccupiedChunk chunk = plugin.getRegionManager().getChunk(event.getBlock().getLocation());
                Region region = plugin.getRegionManager().getRegion(event.getBlock().getLocation());
                String clan = plugin.getClan(event.getPlayer());
                boolean chunkNotControlledByPlayerClan = chunk != null && !chunk.getClan().equals(clan);
                boolean regionNotControlledByPlayerClan = region != null && region.getStatus() == RegionStatus.CENTER && !region.getController().equals(clan);
                if(chunkNotControlledByPlayerClan || regionNotControlledByPlayerClan) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
    
    @EventHandler 
    public void onBlockDestroy(BlockBreakEvent event) {
        if(!event.isCancelled()) {
            if(event.getBlock().getWorld().equals(plugin.getRegionManager().getWorld())) {
                OccupiedChunk chunk = plugin.getRegionManager().getChunk(event.getBlock().getLocation());
                Region region = plugin.getRegionManager().getRegion(event.getBlock().getLocation());
                String clan = plugin.getClan(event.getPlayer());
                boolean chunkNotControlledByPlayerClan = chunk != null && !chunk.getClan().equals(clan);
                boolean regionNotControlledByPlayerClan = region != null && region.getStatus() == RegionStatus.CENTER && !region.getController().equals(clan);
                if(chunkNotControlledByPlayerClan || regionNotControlledByPlayerClan) {
                    event.setCancelled(true);
                    return;
                } else if(chunk != null && chunk.getClan().equals(clan)) {
                    boolean unregistered = false;
                    if(event.getBlock().getType() == Material.BEACON && event.getBlock().equals(chunk.getBeacon())) {
                        unregistered = plugin.getRegionManager().unregisterChunk(chunk);
                    } else if(beaconBaseMaterial.contains(event.getBlock().getType())) {
                        List<Block> beacons = getCompletedBeacons(event.getBlock());
                        for(Block b : beacons) {
                            if(b.equals(chunk.getBeacon())) {
                                unregistered = plugin.getRegionManager().unregisterChunk(chunk);
                                break;
                            }
                        }
                    }
                    if (unregistered) {
                        event.getPlayer().sendMessage(ChatColor.YELLOW + "You unregistered this chunk for " + plugin.getClanDisplay(clan));
                    }

                }
            }
        }
    }
}
