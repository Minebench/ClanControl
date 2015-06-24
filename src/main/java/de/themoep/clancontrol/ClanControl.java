package de.themoep.clancontrol;

import de.themoep.clancontrol.listeners.BlockListener;
import de.themoep.clancontrol.listeners.InteractListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

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

public class ClanControl extends JavaPlugin {
    
    private static ClanControl instance;
    private RegionManager regionManager;
    boolean simpleClans = false;
    
    public static boolean protectBlocks;
    public static boolean protectContainer;
    public static boolean protectDoors;
    public static boolean protectRedstone;
    public static boolean protectEntities;
    public static boolean protectExplosions;
    public static boolean protectUse;
    public static boolean protectEverything;
    
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        protectBlocks = getConfig().getBoolean("protection.blocks");
        protectContainer = getConfig().getBoolean("protection.container");
        protectDoors = getConfig().getBoolean("protection.doors");
        protectRedstone = getConfig().getBoolean("protection.redstone");
        protectEntities = getConfig().getBoolean("protection.entities");
        protectExplosions = getConfig().getBoolean("protection.explosions");
        protectUse = getConfig().getBoolean("protection.use");
        protectEverything = getConfig().getBoolean("protection.everything");
        getServer().getPluginManager().registerEvents(new BlockListener(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new InteractListener(getInstance()), getInstance());
        regionManager = new RegionManager(getInstance());
        getLogger().info("Searching for SimpleClans...");
        simpleClans = getServer().getPluginManager().getPlugin("SimpleClans") != null;
        if(simpleClans) {
            getLogger().info("Found SimpleClans! Using it as group provider.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("map")) {
                if(sender.hasPermission("clancontrol.command.map")) {
                    Location location = null;
                    if(sender instanceof Player) {
                        Player p = (Player) sender;
                        List<BaseComponent[]> msg = getRegionManager().getRegionMap(p);
                        if(msg.size() > 0) {
                            for (BaseComponent[] row : msg) {
                                p.spigot().sendMessage(row);
                            }
                        } else {
                            sender.sendMessage("The world " + p.getWorld().getName() + " cannot be controlled!");
                        }
                    } else {
                        sender.sendMessage("This command can only be run by a player!");
                    }
                    return true;
                } else {
                    sender.sendMessage("You don't have the permission clancontrol.command.map");
                }
            } else if(args[0].equalsIgnoreCase("region")) {
                if(sender.hasPermission("clancontrol.command.region")) {
                    if(sender instanceof Player) {
                        Player p = (Player) sender;
                        Region region = null;
                        if(args.length == 1) {
                            region = getRegionManager().getRegion(p.getLocation());
                        } else if(args.length == 3) {
                            try {
                                int x = Integer.parseInt(args[1]);
                                try {
                                    int z = Integer.parseInt(args[2]);
                                    region = getRegionManager().getRegion(p.getLocation().getWorld().getName(), x, z);
                                } catch(NumberFormatException e) {
                                    sender.sendMessage(ChatColor.GOLD + args[2] + ChatColor.RED + " is not a valid number!");
                                    return true;
                                }
                            } catch(NumberFormatException e) {
                                sender.sendMessage(ChatColor.GOLD + args[1] + ChatColor.RED + " is not a valid number!");
                                return true;
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " region [<x> <z>]");
                            return true;
                        }
                        if(region != null) {
                            List<BaseComponent[]> msg = getRegionManager().getChunkMap(p, region);
                            if(msg.size() > 0) {
                                String head = "Region " + region.getX() + "/" + region.getZ();
                                head += " - Status: " + StringUtils.capitalize(region.getStatus().toString().toLowerCase());
                                if(!region.getController().isEmpty()) {
                                    head += " - Controller: " + getClanDisplay(region.getController());
                                }
                                p.spigot().sendMessage(new ComponentBuilder(head).create());
                                for (BaseComponent[] row : msg) {
                                    p.spigot().sendMessage(row);
                                }
                            } else {
                                sender.sendMessage("The world " + p.getWorld().getName() + " cannot be controlled!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "This region does not exist!");
                        }
                    } else {
                        sender.sendMessage("This command can only be run by a player!");
                    }
                    return true;
                } else {
                    sender.sendMessage("You don't have the permission clancontrol.command.region");
                }
            } else if(args[0].equalsIgnoreCase("reload")) {
                if(sender.hasPermission("clancontrol.command.reload")) {

                } else {
                    sender.sendMessage("You don't have the permission clancontrol.command.reload");
                }
            }            
        }
        return false;        
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
                    return c.getTag();
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

    public String getClanDisplay(String clan) {
        if(simpleClans) {
            Clan c = SimpleClans.getInstance().getClanManager().getClan(clan);
            if(c != null) {
                return c.getColorTag();
            }
        }
        Team team = getServer().getScoreboardManager().getMainScoreboard().getTeam(clan);
        if(team != null) {
            return team.getDisplayName();
        }
        return clan;
    }
    
    public boolean areAllied(String clan1, String clan2) {
        if(clan1 != null && clan2 != null && !clan1.isEmpty() && !clan2.isEmpty()) {
            if (simpleClans) {
                Clan c1 = SimpleClans.getInstance().getClanManager().getClan(clan1);
                if (c1 != null) {
                    return c1.isAlly(clan2);
                }
            }
        }
        return false;
    }
}
