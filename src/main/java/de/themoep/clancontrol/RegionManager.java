package de.themoep.clancontrol;

import de.themoep.utils.ConfigAccessor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bukkit Plugins - ${project.description}
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
public class RegionManager {
    
    private ClanControl plugin;
    
    private Map<Integer, Map<Integer, OccupiedChunk>> chunkCoords;
    
    private Map<Integer, Map<Integer, Region>> regionCoords;
    
    private ConfigAccessor regionYml;
    
    // Side length of one region in chunks
    private int dimension;

    private String world;

    private int centerX;
    private int centerZ;
    private int mapradius;
    
    private double chunkRatio;

    public RegionManager(ClanControl plugin) {
        this.plugin = plugin;
        loadConfig();
        loadRegions();
    }
    
    private void loadConfig() {
        dimension = plugin.getConfig().getInt("regiondimension", 16);
        world = plugin.getConfig().getString("map.world", "world");
        mapradius = plugin.getConfig().getInt("map.radius", 2560);
        centerX = plugin.getConfig().getInt("map.center.x", 0);
        centerZ = plugin.getConfig().getInt("map.center.z", 0);
        double chunkRatio = plugin.getConfig().getDouble("chunkratio", 1);
        this.chunkRatio = (chunkRatio > 1) ? chunkRatio / 100 : chunkRatio;
    }
    
    private void loadRegions() {
        regionYml = new ConfigAccessor(plugin, "regions.yml");
        getStorage().reloadConfig();
        
        regionCoords = new HashMap<Integer, Map<Integer, Region>>();
        chunkCoords = new HashMap<Integer, Map<Integer, OccupiedChunk>>();
        
        int regionCount = 0;
        int chunkCount = 0;
        int regionFailedCount = 0;
        int chunkFailedCount = 0;
        
        plugin.getLogger().info("Start loading regions from storage!");
        
        FileConfiguration config = getStorage().getConfig();        
        for(String worldname : config.getKeys(false)) {
            
            ConfigurationSection worldsSect = config.getConfigurationSection(worldname);
            for(String xStr : worldsSect.getKeys(false)) {
                try {
                    int regionX = Integer.parseInt(xStr);
                    for(String zStr : worldsSect.getConfigurationSection(xStr).getKeys(false)) {
                        try {
                            int regionZ = Integer.parseInt(zStr);
                            List<OccupiedChunk> occupiedChunks = new ArrayList<OccupiedChunk>();
                            ConfigurationSection chunksSect = worldsSect.getConfigurationSection(xStr + "." + zStr + ".chunks");
                            for(String chunkXStr : chunksSect.getKeys(false)) {
                                try {
                                    int chunkX = Integer.parseInt(chunkXStr);
                                    for(String chunkZStr : chunksSect.getConfigurationSection(chunkXStr).getKeys(false)) {
                                        try {
                                            int chunkZ = Integer.parseInt(chunkZStr);
                                            
                                            ConfigurationSection chunkSect = chunksSect.getConfigurationSection(chunkX + "." + chunkZ);
                                            
                                            OccupiedChunk chunk = new OccupiedChunk(
                                                    worldname, 
                                                    chunkX, 
                                                    chunkZ, 
                                                    chunkSect.getString("clan"),
                                                    chunkSect.getInt("beacon.x"),
                                                    chunkSect.getInt("beacon.y"),
                                                    chunkSect.getInt("beacon.z")
                                            );
                                            occupiedChunks.add(chunk);
                                            if(!chunkCoords.containsKey(chunkX)) {
                                                chunkCoords.put(chunkX, new HashMap<Integer, OccupiedChunk>());
                                            }
                                            chunkCoords.get(chunkX).put(chunkZ, chunk);
                                            chunkCount++;
                                        } catch(NumberFormatException e) {
                                            plugin.getLogger().severe("Error while loading chunks of region " + regionX + "-" + regionZ + " from the regions.yml! '" + chunkZStr + "' is not a valid integer!");
                                            chunkFailedCount++;
                                        }
                                    }                                    
                                } catch(NumberFormatException e) {
                                    plugin.getLogger().severe("Error while loading chunks of region " + regionX + "-" + regionZ + " from the regions.yml! '" + chunkXStr + "' is not a valid integer!");
                                    chunkFailedCount++;
                                }
                            }
                            String statusStr = worldsSect.getString(xStr + "." + zStr + ".status", "FREE");
                            try {
                                RegionStatus status = RegionStatus.valueOf(statusStr);
                                String controller = worldsSect.getString(xStr + "." + zStr + ".controller", "");
                                Region region = newRegion(worldname, regionX, regionZ, status, controller, occupiedChunks);
                                if(region != null) {
                                    regionCount++;
                                } else {
                                    plugin.getLogger().severe("Failed to load region " + regionX + "-" + regionZ + "! It already exists in the RegionManager? Please contact the dev asap as this should not be able to happen!");
                                    regionFailedCount++;
                                }
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().severe("Failed to load region " + regionX + "-" + regionZ + "! '" + statusStr + "' is not a valid RegionStatus!");
                                regionFailedCount++;
                            }
                        } catch(NumberFormatException e) {
                            plugin.getLogger().severe("Error while loading the regions.yml! '" + zStr + "' is not a valid integer!");
                            regionFailedCount++;
                        }
                    }
                } catch(NumberFormatException e) {
                    plugin.getLogger().severe("Error while loading the regions.yml! '" + xStr + "' is not a valid integer!");
                    regionFailedCount++;
                }
            }
        }
        plugin.getLogger().info("Successfully loaded " + regionCount + " regions with " + chunkCount + " occupied chunks!");
        if(regionFailedCount > 0) {
            plugin.getLogger().warning("Failed to load " + regionFailedCount + " regions!");
        }
        if(chunkFailedCount > 0) {
            plugin.getLogger().warning("Failed to load " + chunkFailedCount + " chunks!");
        }
    }

    /**
     * Registers a beacon for a clan
     * @param clan
     * @param location
     * @return True if it got registered, false if not (e.g. when the location is outside the board or the chunk is already registered)
     */
    public boolean registerBeacon(String clan, Location location) {
        if(getChunk(location) == null) {
            if(Math.abs((int) (mapradius/dimension) * dimension + centerX) < Math.abs(location.getBlockX())
                    || Math.abs((int) (mapradius/dimension) * dimension + centerZ) < Math.abs(location.getBlockZ())) {
                return false;
            }
            OccupiedChunk chunk = new OccupiedChunk(location, clan);
            if(!chunkCoords.containsKey(chunk.getX())) {
                chunkCoords.put(chunk.getX(), new HashMap<Integer, OccupiedChunk>());
            } else if(chunkCoords.get(chunk.getX()).containsKey(chunk.getZ())){
                return false;
            }
            Region region = getRegion(chunk);
            if(region != null) {
                RegionStatus result = region.addChunk(chunk);
                if (result != null) {
                    chunkCoords.get(chunk.getX()).put(chunk.getZ(), chunk);
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean unregisterChunk(OccupiedChunk chunk) {
        if(chunkCoords.containsKey(chunk.getX()) && chunkCoords.get(chunk.getX()).containsKey(chunk.getZ()) && chunk.equals(chunkCoords.get(chunk.getX()).get(chunk.getZ()))) {
            chunkCoords.get(chunk.getX()).remove(chunk.getZ());
            Region region = getRegion(chunk);
            if(region != null) {
                region.removeChunk(chunk);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the OccupiedChunk from a location
     * @param location
     * @return The OccupiedChunk; null if the chunk isn't occupied
     */
    public OccupiedChunk getChunk(Location location) {        
        return getChunk(location.getChunk());
    }
    
    /**
     * Get the OccupiedChunk from a chunk
     * @param chunk
     * @return The OccupiedChunk; null if the chunk isn't occupied
     */
    public OccupiedChunk getChunk(Chunk chunk) {
        return getChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    /**
     * Get the OccupiedChunk from a chunks x and z coordinates 
     * @param chunkX
     * @param chunkZ
     * @return The OccupiedChunk; null if the chunk isn't occupied
     */
    public OccupiedChunk getChunk(String worldname, int chunkX, int chunkZ) {
        if(worldname.equalsIgnoreCase(world) && chunkCoords.containsKey(chunkX)) {
            if(chunkCoords.get(chunkX).containsKey(chunkZ)) {
                return chunkCoords.get(chunkX).get(chunkZ);
            }
        }
        return null;
    }

    /**
     * Get the Region from a Location
     * @param location
     * @return The Region this location is part of
     */
    public Region getRegion(Location location) {
        return (location == null) ? null : getRegion(location.getChunk());
    }
    
    /**
     * Get the Region from a Chunk
     * @param chunk
     * @return The Region this chunk is part of
     */
    private Region getRegion(Chunk chunk) {
        return (chunk == null) ? null : getRegion(chunk.getWorld().getName(), chunkToRegionCoord(chunk.getX()), chunkToRegionCoord(chunk.getZ()));
    }

    /**
     * Get the Region from an OccupiedChunk
     * @param chunk
     * @return The Region this occupied chunk is part of
     */
    public Region getRegion(OccupiedChunk chunk) {
        return (chunk == null) ? null : getRegion(chunk.getWorld().getName(), chunkToRegionCoord(chunk.getX()), chunkToRegionCoord(chunk.getZ()));
    }

    private int chunkToRegionCoord(int chunkCoord) {
        return ((chunkCoord < 0 ) ? chunkCoord - 16 : chunkCoord) / dimension;
    }

    /**
     * Get the Region from a worldname and x/z region coordinates
     * @param worldname
     * @param regionX
     * @param regionZ
     * @return The Region described by this worldname and x/z region coordinates; null if it is outside the board
     */
    public Region getRegion(String worldname, int regionX, int regionZ) {
        if(Math.abs(mapradius + centerX) < Math.abs(regionX * dimension * 16)
                || Math.abs(mapradius + centerZ) < Math.abs(regionZ * dimension * 16)) {
            return null;
        }
        if(worldname.equalsIgnoreCase(world) && regionCoords.containsKey(regionX)) {
            if(regionCoords.get(regionX).containsKey(regionZ)) {
                return regionCoords.get(regionX).get(regionZ);
            }
        }
        return newRegion(worldname, regionX, regionZ);
    }

    /**
     * Convenience method for generating and registering new regions
     * @param worldname
     * @param regionX
     * @param regionZ
     * @return The newly created and registered region; null if there is already a region with these coords!
     */
    private Region newRegion(String worldname, int regionX, int regionZ) {
        if(!regionCoords.containsKey(regionX)) {
            regionCoords.put(regionX, new HashMap<Integer, Region>());
        } else if(regionCoords.get(regionX).containsKey(regionZ)){
            return null;
        }
        Region region = new Region(this, worldname, regionX, regionZ);
        regionCoords.get(regionX).put(regionZ, region);
        return region;
    }
    
    /**
     * Convenience method for generating and registering new regions
     * @param worldname
     * @param regionX
     * @param regionZ
     * @return The newly created and registered region; null if there is already a region with these coords!
     */
    private Region newRegion(String worldname, int regionX, int regionZ, RegionStatus status, String controller, List<OccupiedChunk> occupiedChunks) {
        if(!regionCoords.containsKey(regionX)) {
            regionCoords.put(regionX, new HashMap<Integer, Region>());
        } else if(regionCoords.get(regionX).containsKey(regionZ)){
            return null;
        }
        Region region = new Region(this, worldname, regionX, regionZ, status, controller, occupiedChunks);
        regionCoords.get(regionX).put(regionZ, region);
        return region;
    }

    /**
     * Recalculates the game board
     * @param region The region that changed
     */
    public void recalculateBoard(Region region) {
        String controller = region.calculateControl(chunkRatio);
        if(controller != null) {

        }
    }

    public ConfigAccessor getStorage() {
        return regionYml;
    }
    
    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    /**
     * Get a map of all the regions 
     * @param location The location of the player
     * @return A list of BaseComponent arrays with each line as an entry; empty list if there is no map in this world
     */
    public List<BaseComponent[]> getRegionMap(Location location) {
        List<BaseComponent[]> msg = new ArrayList<BaseComponent[]>();
        String worldname = world;
        Region currentRegion = null;
        if(location != null) {
            worldname = location.getWorld().getName();
            currentRegion = getRegion(location);
        }
        if(worldname.equals(world)) {
            int xMin = (centerX - mapradius) / (dimension * 16);
            int xMax = (centerX + mapradius) / (dimension * 16);
            int zMin = (centerZ - mapradius) / (dimension * 16);
            int zMax = (centerZ + mapradius) / (dimension * 16);
            for (int z = zMin; z <= zMax; z++) {
                ComponentBuilder row = new ComponentBuilder("");
                for (int x = xMin; x <= xMax; x++) {
                    row.append(" ");
                    Region region = getRegion(worldname, x, z);
                    if (region != null) {
                        String hoverText = ChatColor.AQUA + "Region " + x + "/" + z + ChatColor.RESET;
                        hoverText += "\nStatus: " + StringUtils.capitalize(region.getStatus().toString().toLowerCase());
                        if (!region.getController().isEmpty()) {
                            hoverText += "\nController: " + plugin.getClanDisplay(region.getController());
                        }
                        if (region.equals(currentRegion)) {
                            row.append("x");
                            hoverText += "\n" + ChatColor.YELLOW + "You are here!" + ChatColor.RESET;
                        } else {
                            row.append("o");
                        }
                        hoverText += "\n" + ChatColor.GRAY + ChatColor.ITALIC + "Click for more Info!" + ChatColor.RESET;
                        if (region.getStatus() == RegionStatus.CONFLICT) {
                            row.color(ChatColor.LIGHT_PURPLE);
                        } else if (region.getStatus() == RegionStatus.BORDER) {
                            row.color(ChatColor.GREEN);
                        } else if (region.getStatus() == RegionStatus.CENTER) {
                            row.color(ChatColor.DARK_GREEN);
                        } else {
                            row.color(ChatColor.DARK_GRAY);
                        }
                        HoverEvent chunkHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create());
                        ClickEvent chunkClick = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/control region " + region.getX() + " " + region.getZ());
                        row.event(chunkHover);
                        row.event(chunkClick);
                    } else {
                        row.append("-");
                        row.color(ChatColor.DARK_GRAY);
                    }
                }
                msg.add(row.create());
            }
        }
        return msg;
    }

    /**
     * Get a map of all the chunks in a region
     * @param location The location of the player
     * @return A list of BaseComponent arrays with each line as an entry; empty list if there is no map in this world
     */
    public List<BaseComponent[]> getChunkMap(Region region, Location location) {
        List<BaseComponent[]> msg = new ArrayList<BaseComponent[]>();
        int xMin = region.getX() * dimension;
        int xMax = xMin + dimension;
        int zMin = region.getZ() * dimension;
        int zMax = zMin + dimension;
        String worldname = world;
        Chunk currentChunk = null;
        if(location != null) {
            worldname = location.getWorld().getName();
            currentChunk = location.getWorld().getChunkAt(location);
        }
        if(worldname.equals(world)) {
            for (int z = zMin; z < zMax; z++) {
                ComponentBuilder row = new ComponentBuilder("");
                for (int x = xMin; x < xMax; x++) {
                    row.append(" ");
                    String hoverText = ChatColor.AQUA + "Chunk " + x + "/" + z;
                    if (currentChunk != null && currentChunk.getX() == x && currentChunk.getZ() == z) {
                        row.append("x");
                        hoverText += "\n" + ChatColor.YELLOW + "You are here!";
                    } else {
                        row.append("o");
                    }
                    OccupiedChunk chunk = getChunk(worldname, x, z);
                    if (chunk != null) {
                        row.color(ChatColor.GREEN);
                        hoverText += ChatColor.RESET + "\nOwned by: " + plugin.getClanDisplay(chunk.getClan());
                    } else {
                        row.color(ChatColor.DARK_GRAY);
                    }
                    HoverEvent clanHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create());
                    row.event(clanHover);
                }
                msg.add(row.create());
            }
        }
        return msg;
    }
}
