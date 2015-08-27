////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.plotsquared.bukkit.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.events.PlayerEnterPlotEvent;
import com.plotsquared.bukkit.events.PlayerLeavePlotEvent;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.listener.PlotListener;

/**
 * Created 2014-10-30 for PlotSquared
 *
 * @author Citymonstret
 */
@SuppressWarnings({ "deprecation"})
public class PlotPlusListener extends PlotListener implements Listener {
    private final static HashMap<String, Interval> feedRunnable = new HashMap<>();
    private final static HashMap<String, Interval> healRunnable = new HashMap<>();

    public static void startRunnable(final JavaPlugin plugin) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Iterator<Entry<String, Interval>> iter = healRunnable.entrySet().iterator(); iter.hasNext();){
                    Entry<String, Interval> entry = iter.next();
                    final Interval value = entry.getValue();
                    ++value.count;
                    if (value.count == value.interval) {
                        value.count = 0;
                        final Player player = Bukkit.getPlayer(entry.getKey());
                        if (player == null) {
                            iter.remove();
                            continue;
                        }
                        final double level = player.getHealth();
                        if (level != value.max) {
                            player.setHealth(Math.min(level + value.amount, value.max));
                        }
                    }
                }
                for (Iterator<Entry<String, Interval>> iter = feedRunnable.entrySet().iterator(); iter.hasNext();){
                    Entry<String, Interval> entry = iter.next();
                    final Interval value = entry.getValue();
                    ++value.count;
                    if (value.count == value.interval) {
                        value.count = 0;
                        final Player player = Bukkit.getPlayer(entry.getKey());
                        if (player == null) {
                            iter.remove();
                            continue;
                        }
                        final int level = player.getFoodLevel();
                        if (level != value.max) {
                            player.setFoodLevel(Math.min(level + value.amount, value.max));
                        }
                    }
                }
            }
        }, 0l, 20l);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(final BlockDamageEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        final Plot plot = MainUtil.getPlot(BukkitUtil.getLocation(player));
        if (plot == null) {
            return;
        }
        if (FlagManager.isBooleanFlag(plot, "instabreak", false)) {
            event.getBlock().breakNaturally();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(final EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        final Player player = (Player) event.getEntity();
        final Plot plot = MainUtil.getPlot(BukkitUtil.getLocation(player));
        if (plot == null) {
            return;
        }
        if (FlagManager.isBooleanFlag(plot, "invincible", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(final PlayerPickupItemEvent event) {
        final Player player = event.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        final Plot plot = MainUtil.getPlot(pp.getLocation());
        if (plot == null) {
            return;
        }
        final UUID uuid = pp.getUUID();
        if (plot.isAdded(uuid) && FlagManager.isBooleanFlag(plot, "drop-protection", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        final Plot plot = MainUtil.getPlot(pp.getLocation());
        if (plot == null) {
            return;
        }
        final UUID uuid = pp.getUUID();
        if (plot.isAdded(uuid) && FlagManager.isBooleanFlag(plot, "item-drop", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlotEnter(final PlayerEnterPlotEvent event) {
        Player player = event.getPlayer();
        final Plot plot = event.getPlot();
        Flag greeting = FlagManager.getPlotFlag(plot, "greeting");
        if (greeting != null) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', C.PREFIX_GREETING.s().replaceAll("%id%", plot.id + "") + greeting.getValueString()));
        }
        Flag feed = FlagManager.getPlotFlag(plot, "feed");
        if (feed != null) {
            Integer[] value = (Integer[]) feed.getValue();
            feedRunnable.put(player.getName(), new Interval(value[0], value[1], 20));
        }
        Flag heal = FlagManager.getPlotFlag(plot, "heal");
        if (heal != null) {
            Integer[] value = (Integer[]) heal.getValue();
            healRunnable.put(player.getName(), new Interval(value[0], value[1], 20));
        }
        if (FlagManager.isBooleanFlag(plot, "notify-enter", false)) {
            final Player trespasser = event.getPlayer();
            final PlotPlayer pt = BukkitUtil.getPlayer(trespasser);
            if (Permissions.hasPermission(pt, "plots.flag.notify-enter.bypass")) {
                return;
            }
            if (plot.hasOwner()) {
                for (UUID owner : PlotHandler.getOwners(plot)) {
                    final PlotPlayer pp = UUIDHandler.getPlayer(owner);
                    if (pp == null) {
                        return;
                    }
                    if (pp.getUUID().equals(pt.getUUID())) {
                        return;
                    }
                    if (pp.isOnline()) {
                        MainUtil.sendMessage(pp, C.NOTIFY_ENTER.s().replace("%player", trespasser.getName()).replace("%plot", plot.getId().toString()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final String name = player.getName();
        feedRunnable.remove(name);
        healRunnable.remove(name);
    }

    @EventHandler
    public void onPlotLeave(final PlayerLeavePlotEvent event) {
        final Player leaver = event.getPlayer();
        final Plot plot = event.getPlot();
        if (!plot.hasOwner()) {
            return;
        }
        Flag farewell = FlagManager.getPlotFlag(plot, "farewell"); 
        if (farewell != null) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', C.PREFIX_FAREWELL.s().replaceAll("%id%", plot.id + "") + farewell.getValueString()));
        }
        final PlotPlayer pl = BukkitUtil.getPlayer(leaver);
        String name = leaver.getName();
        feedRunnable.remove(name);
        healRunnable.remove(name);
        if (FlagManager.isBooleanFlag(plot, "notify-leave", false)) {
            if (Permissions.hasPermission(pl, "plots.flag.notify-leave.bypass")) {
                return;
            }
            if (plot.hasOwner()) {
                for (UUID owner : PlotHandler.getOwners(plot)) {
                    final PlotPlayer pp = UUIDHandler.getPlayer(owner);
                    if (pp == null) {
                        return;
                    }
                    if (pp.getUUID().equals(pl.getUUID())) {
                        return;
                    }
                    if (pp.isOnline()) {
                        MainUtil.sendMessage(pp, C.NOTIFY_LEAVE.s().replace("%player", leaver.getName()).replace("%plot", plot.getId().toString()));
                    }
                }
            }
        }
    }

    public static class Interval {
        public final int interval;
        public final int amount;
        public final int max;
        public int count = 0;

        public Interval(final int interval, final int amount, final int max) {
            this.interval = interval;
            this.amount = amount;
            this.max = max;
        }
    }

    /**
     * Record Meta Class
     *
     * @author Citymonstret
     */
    public static class RecordMeta {
        public final static List<RecordMeta> metaList = new ArrayList<>();
        static {
            for (int x = 3; x < 12; x++) {
                metaList.add(new RecordMeta(x + "", Material.valueOf("RECORD_" + x)));
            }
        }
        private final String name;
        private final Material material;

        public RecordMeta(final String name, final Material material) {
            this.name = name;
            this.material = material;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        public Material getMaterial() {
            return this.material;
        }
    }
}
