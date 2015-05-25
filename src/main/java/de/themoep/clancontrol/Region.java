package de.themoep.clancontrol;

import org.bukkit.Bukkit;
import org.bukkit.World;

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
public class Region {
    private int hash;
    
    private RegionManager rm;
    
    private String worldname;
    private int x;
    private int z;
    
    private RegionStatus status;
    
    private List<Region> surroundingRegions;
    
    private List<OccupiedChunk> occupiedCunks = new ArrayList<OccupiedChunk>();
    
    private String controller = "";

    public Region(RegionManager rm, String worldname, int x, int z) {
        this(rm, worldname, x, z, RegionStatus.FREE);
    }

    public Region(RegionManager rm, String worldname, int x, int z, RegionStatus status) {
        this.rm = rm;
        this.worldname = worldname;
        this.x = x;
        this.z = z;
        this.status = status;
    }

    public RegionManager getRegionManager() {
        return rm;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public RegionStatus getStatus() {
        return status;
    }
    
    public List<OccupiedChunk> getChunks() {
        return occupiedCunks;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldname);
    }

    public String getController() {
        return controller;
    }

    /**
     * Sets the controller of this region
     * @param controller
     * @return The new controller; null if it didn't change
     */
    private String setController(String controller) {
        if(!getController().equals(controller)) {
            this.controller = controller;
            return controller;
        }
        return null;
    }
    
    /**
     * Add an OccupiedChunk to this region
     * @param chunk
     * @return The resulting status of this region; null if something impossible happened
     */
    public RegionStatus addChunk(OccupiedChunk chunk) {
        if(status == RegionStatus.CENTER && getController().equals(chunk.getClan())) {
            return null;
        }
        occupiedCunks.add(chunk);
        ClanControl.getInstance().getRegionManager().recalculateBoard(this);
        return status;
    }

    /**
     * Calculate the status and the controller of this region
     * @param chunkRatio The ratio of chunks a clan needs to occupy before he can control a region
     * @return The name of the resulting controller; empty string there is none; null if no change
     */
    public String calculateControl(double chunkRatio) {
        if (getStatus() == RegionStatus.FREE && getChunks().size() == 1) {
            setController(getChunks().get(0).getClan());
            calculateStatus();
            return getController();
        } else if(getStatus() != RegionStatus.CENTER) {
            status = RegionStatus.CONFLICT;
            Map<String, Integer> weights = new HashMap<String, Integer>();
            for(OccupiedChunk chunk : getChunks()) {
                if(!weights.containsKey(chunk.getClan())) {
                    weights.put(chunk.getClan(), 1);
                } else {
                    weights.put(chunk.getClan(), weights.get(chunk.getClan()) + 1);
                }
            }
            String newController = "";
            for(Map.Entry<String, Integer> weight : weights.entrySet()) {
                if(weight.getValue() / getChunks().size() > chunkRatio){
                    if(newController == null) {
                        newController = weight.getKey();
                        status = RegionStatus.BORDER;
                    } else if(!weights.containsKey(newController) || weights.get(newController) < weight.getValue()){
                        newController = weight.getKey();
                        status = RegionStatus.BORDER;
                    } else if(weights.containsKey(newController) && weights.get(newController) == weight.getValue()) {
                        newController = weight.getKey();
                        status = RegionStatus.CONFLICT;
                    }
                }
            }
            if(getStatus() == RegionStatus.CONFLICT) {
                newController = "";
            }
            calculateStatus();
            return setController(newController);
        }
        return null;
    }
    
    public RegionStatus calculateStatus() {
        if(!getController().isEmpty()) {
            if(getSurroundingRegions().size() == getSurroundingRegions(RegionStatus.BORDER, getController()).size()) {
                status = RegionStatus.CENTER;
            } else {
                status = RegionStatus.BORDER;
            }
        } else if(getChunks().size() > 0) {
            status = RegionStatus.CONFLICT;
            for(Region r : getSurroundingRegions(RegionStatus.CENTER, getController())) {
                r.calculateStatus();
            }
        } else {
            status = RegionStatus.FREE;
        }
        return status;
    }

    public List<Region> getSurroundingRegions(RegionStatus status, String clan) {
        List<Region> regions = new ArrayList<Region>();
        for(Region r : getSurroundingRegions(status)) {
            if(r.getController().equals(clan)) {
                regions.add(r);
            }
        }
        return regions;
    }

    public List<Region> getSurroundingRegions(RegionStatus status) {
        List<Region> regions = new ArrayList<Region>();
        for(Region r : getSurroundingRegions()) {
            if(r.getStatus() == status) {
                regions.add(r);
            }
        }
        return regions;
    }
    
    public List<Region> getSurroundingRegions() {
        List<Region> regions = new ArrayList<Region>();
        if(surroundingRegions != null) {
            regions = surroundingRegions;
        } else {
            for (int[] i : new int[][]{{1, 0}, {0, -1}, {-1, 0}, {0, 1}}) {
                Region r = getRegionManager().getRegion(worldname, getX() + i[0], getZ() + i[1]);
                if (r != null) {
                    regions.add(r);
                }
            }
            surroundingRegions = regions;
        }
        return regions;
    }
    
    public int hashCode() {
        int h = hash;
        if(h == 0) {
            h = (worldname + " - " + getX() + ":" + getZ()).hashCode();
            hash = h;
        }
        return h;
    }
}
