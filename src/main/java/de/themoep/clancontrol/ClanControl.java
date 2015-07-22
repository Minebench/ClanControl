package de.themoep.clancontrol;

import de.themoep.clancontrol.listeners.BlockListener;
import de.themoep.clancontrol.listeners.InteractListener;
import de.themoep.clancontrol.listeners.MoveListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;
import org.kitteh.vanish.VanishPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

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
    private String tag;
    
    public boolean protectBlocks;
    public boolean protectContainer;
    public boolean protectDoors;
    public boolean protectRedstone;
    public boolean protectEntities;
    public boolean protectExplosions;
    public boolean protectUse;
    public boolean protectEverything;
    private boolean vanishNoPacket;

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(new BlockListener(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new InteractListener(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new MoveListener(getInstance()), getInstance());
        getLogger().info("Searching for SimpleClans...");
        simpleClans = getServer().getPluginManager().getPlugin("SimpleClans") != null;
        if(simpleClans) {
            getLogger().info("Found SimpleClans! Using it as group provider.");
        }
        vanishNoPacket = getServer().getPluginManager().getPlugin("VanishNoPacket") != null;
        if(vanishNoPacket) {
            getLogger().info("Found VanishNoPacket! Hiding global messages by vanished players!");
        }
        
    }
    
    private void loadConfig() {
        getLogger().log(Level.INFO, "Reloading Config and RegionManager!");
        reloadConfig();
        tag = ChatColor.translateAlternateColorCodes('&', getConfig().getString("plugintag", "&f[&cControl&f]&r"));
        protectBlocks = getConfig().getBoolean("protection.blocks");
        protectContainer = getConfig().getBoolean("protection.container");
        protectDoors = getConfig().getBoolean("protection.doors");
        protectRedstone = getConfig().getBoolean("protection.redstone");
        protectEntities = getConfig().getBoolean("protection.entities");
        protectExplosions = getConfig().getBoolean("protection.explosions");
        protectUse = getConfig().getBoolean("protection.use");
        protectEverything = getConfig().getBoolean("protection.everything");
        regionManager = new RegionManager(getInstance());
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
                } else {
                    sender.sendMessage("You don't have the permission clancontrol.command.region");
                }
                return true;
            } else if(args[0].equalsIgnoreCase("reload")) {
                if(sender.hasPermission("clancontrol.command.reload")) {
                    sender.sendMessage("Config reloaded.");
                } else {
                    sender.sendMessage("You don't have the permission clancontrol.command.reload");
                }
                return true;
            }  else if(args[0].equalsIgnoreCase("register")) {
                if(sender instanceof Player) {
                    if(sender.hasPermission("clancontrol.command.register")) {
                        if(args.length > 1) {
                            Block b = ((Player) sender).getTargetBlock((Set<Material>) null, 7);
                            if(b.getType() == Material.BEACON) {
                                if(getRegionManager().registerBeacon(args[1], b.getLocation())) {
                                    sender.sendMessage("Registered Beacon for " + getClanDisplay(args[1]) + "!");
                                } else {
                                    sender.sendMessage("Could not register Beacon for " + getClanDisplay(args[1]) + "!");
                                }
                            } else {
                                sender.sendMessage("You have to look at a Beacon block to register it!");
                            }
                        } else {
                            sender.sendMessage("Usage: /" + label + " register <clantag>");
                        }
                    } else {
                        sender.sendMessage("You don't have the permission clancontrol.command.register");
                    }
                } else {
                    sender.sendMessage("This command can only be run by a player as you need to look at a beacon!");
                }
                return true;
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

    /**
     * Get the name of the team or clan of a player
     * @param player
     * @return The name of the team/clan; null if the player doesn't have one
     */
    public String getClan(Player player) {
        if(simpleClans) {
            ClanPlayer cp = SimpleClans.getInstance().getClanManager().getClanPlayer(player);
            if(cp != null) {
                Clan c = cp.getClan();
                if(c != null) {
                    return c.getTag();
                }
            }
        } else {
            Team team = getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
            if (team != null) {
                return team.getName();
            }
        }
        return null;
    }

    /**
     * Get the display name/tag of a clan/team
     * @param clan
     * @return
     */
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

    /**
     * Check if two clans/teams are allied
     * @param clan1
     * @param clan2
     * @return True if they are allied or the same team/clan; false if not
     */
    public boolean areAllied(String clan1, String clan2) {
        if(clan1 != null && clan2 != null && !clan1.isEmpty() && !clan2.isEmpty() && !clan1.equals(clan2)) {
            if(simpleClans) {
                Clan c1 = SimpleClans.getInstance().getClanManager().getClan(clan1);
                if (c1 != null) {
                    return c1.isAlly(clan2);
                }
            }
        }
        return false;
    }

    /**
     * Send a message to all online players of a clan/team
     * @param clan
     * @param msg
     */
    public void notifyClan(String clan, String msg) {
        List<Player> playerList = new ArrayList<Player>();
        if(simpleClans) {
            Clan sClan = SimpleClans.getInstance().getClanManager().getClan(clan);
            if(sClan != null) {
                for(ClanPlayer cp : sClan.getOnlineMembers()) {
                    Player p = getServer().getPlayer(cp.getName());
                    if(p != null) {
                        playerList.add(p);
                    }
                }
            }
        } else {
            Team team = getServer().getScoreboardManager().getMainScoreboard().getTeam(clan);
            if(team != null) {
                for(OfflinePlayer p : team.getPlayers()) {
                    if(p.isOnline() && p instanceof Player) {
                        playerList.add((Player) p);
                    }
                }
            }
        }
        for(Player p : playerList) {
            p.sendMessage(msg);
        }
    }

    public String getTag() {
        return tag;
    }

    public boolean isVanished(Player player) {
        if(vanishNoPacket) {
            return ((VanishPlugin) getServer().getPluginManager().getPlugin("VanishNoPacket")).getManager().isVanished(player.getName());
        }
        return false;
    }
}
