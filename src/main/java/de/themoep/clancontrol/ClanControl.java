package de.themoep.clancontrol;

import de.themoep.clancontrol.listeners.BlockListener;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
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
    
    private static ClanControl instance;
    private RegionManager regionManager;
    boolean simpleClans = false;
    
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new BlockListener(), getInstance());
        regionManager = new RegionManager(getInstance());
        getLogger().info("Searching for SimpleClans...");
        simpleClans = getServer().getPluginManager().getPlugin("SimpleClans") != null;
        if(simpleClans) {
            getLogger().info("Found SimpleClans! Using it as group provider.");
        }
    }

    public static ClanControl getInstance() {
        return instance;
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
        return null;
    }
}
