package de.themoep.clancontrol;

import de.themoep.clancontrol.listeners.BlockListener;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

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

public class ClanControl extends JavaPlugin {

    private RegionManager regionManager;
    
    boolean simpleClans = false;
    
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new BlockListener(), ClanControl.getInstance());
        regionManager = new RegionManager(
                getConfig().getInt("regiondimension", 256),
                getConfig().getString("map.world", "world"),
                getConfig().getInt("map.radius", 2560),
                getConfig().getInt("map.center.x", 0),
                getConfig().getInt("map.center.z", 0),
                getConfig().getDouble("chunkratio", 1)
        );
        simpleClans = getServer().getPluginManager().isPluginEnabled("SimpleClans");
    }

    public static ClanControl getInstance() {
        return (ClanControl) Bukkit.getServer().getPluginManager().getPlugin("ClanControl");
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public String getClan(Player player) {
        if(simpleClans) {
            ClanPlayer cp = SimpleClans.getInstance().getClanManager().getClanPlayer(player);
            if(cp != null) {
                Clan c = cp.getClan();
                if(c != null) {
                    return c.getName();
                }
            }
            return null;
        }
        Team team = getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
        if(team != null) {
            return team.getName();
        }
        return player.getName();
    }
}
