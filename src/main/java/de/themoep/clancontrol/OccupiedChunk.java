package de.themoep.clancontrol;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

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
public class OccupiedChunk {
    private String worldname;
    private int x;
    private int z;

    private int beaconX;
    private int beaconY;
    private int beaconZ;
    
    private String clan;

    public OccupiedChunk(String worldname, int x, int z, String clan, int beaconX, int beaconY, int beaconZ) {
        this.worldname = worldname;
        this.x = x;
        this.z = z;
        this.clan = clan;
    }

    /**
     * @param beaconLoc The location of the beacon that claimed this chunk
     * @param clanname The name of the clan that occupies this chunk
     */
    public OccupiedChunk(Location beaconLoc, String clanname) {
        this(beaconLoc.getWorld().getName(), beaconLoc.getChunk().getX(), beaconLoc.getChunk().getZ(), clanname, (int) beaconLoc.getX(), (int) beaconLoc.getY(), (int) beaconLoc.getZ());
    }

    public String getClan() {
        return clan;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldname);
    }
    
    public Block getBeacon() {
        return getWorld().getBlockAt(beaconX, beaconY, beaconZ);
    }
}
