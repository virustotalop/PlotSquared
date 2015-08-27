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
package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.plotsquared.listener.PlotListener;

/**
 * plot functions
 *
 * @author Citymonstret
 */
public class MainUtil {
    public final static HashMap<Plot, Integer> runners = new HashMap<>();
    public static boolean canSendChunk = false;
    public static boolean canSetFast = true;
    public static ArrayList<String> runners_p = new ArrayList<>();
    public static HashMap<String, PlotId> lastPlot = new HashMap<>();
    public static HashMap<String, Integer> worldBorder = new HashMap<>();
    static long state = 1;
    static PseudoRandom random = new PseudoRandom();
    
    public static short[][] x_loc;
    public static short[][] y_loc;
    public static short[][] z_loc;

    public static void initCache() {
        if (x_loc == null) {
            x_loc = new short[16][4096];
            y_loc = new short[16][4096];
            z_loc = new short[16][4096];
            for (int i = 0; i < 16; i++) {
                int i4 = i << 4;
                for (int j = 0; j < 4096; j++) {
                    final int y = (i4) + (j >> 8);
                    final int a = (j - ((y & 0xF) << 8));
                    final int z1 = (a >> 4);
                    final int x1 = a - (z1 << 4);
                    x_loc[i][j] = (short) x1;
                    y_loc[i][j] = (short) y;
                    z_loc[i][j] = (short) z1;
                }
            }
        }
    }
    
    public static boolean isPlotArea(final Location location) {
        final PlotWorld plotworld = PS.get().getPlotWorld(location.getWorld());
        if (plotworld == null) {
            return false;
        }
        if (plotworld.TYPE == 2) {
            return ClusterManager.getCluster(location) != null;
        }
        return true;
    }
    
    public static String getName(UUID owner) {
        if (owner == null) {
            return C.NONE.s();
        }
        String name = UUIDHandler.getName(owner);
        if (name == null) {
            return C.UNKNOWN.s();
        }
        return name;
    }
    
    public static List<PlotPlayer> getPlayersInPlot(Plot plot) {
        ArrayList<PlotPlayer> players = new ArrayList<>();
        for (PlotPlayer pp : UUIDHandler.getPlayers().values()) {
            if (plot.equals(pp.getCurrentPlot())) {
                players.add(pp);
            }
        }
        return players;
    }
    
    public static void reEnterPlot(final Plot plot) {
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                for (PlotPlayer pp : getPlayersInPlot(plot)) {
                    PlotListener.plotExit(pp, plot);
                    PlotListener.plotEntry(pp, plot);
                }
            }
        }, 1);
    }
    
    public static Location getPlotCenter(Plot plot) {
        Location bot = getPlotBottomLoc(plot.world, plot.id);
        Location top = getPlotTopLoc(plot.world, plot.id).add(1, 0, 1);
        return new Location(plot.world, bot.getX() + (top.getX() - bot.getX()) / 2, 0, bot.getZ() + (top.getZ() - bot.getZ()) / 2);
    }
    
    public static List<Plot> getPlotsBySearch(String search) {
        String[] split = search.split(" ");
        int size = split.length * 2;

        List<UUID> uuids = new ArrayList<>();
        PlotId id = null;
        String world = null;
        String alias = null;
        
        for (String term : split) {
            try {
                UUID uuid = UUIDHandler.getUUID(term, null);
                if (uuid == null) {
                    uuid = UUID.fromString(term);
                }
                if (uuid != null) {
                    uuids.add(uuid);
                    continue;
                }
            }
            catch (Exception e) {
                id = PlotId.fromString(term);
                if (id != null) {
                    continue;
                }
                for (String pw : PS.get().getPlotWorlds()) {
                    if (pw.equalsIgnoreCase(term)) {
                        world = pw;
                        break;
                    }
                }
                if (world == null) {
                    alias = term;
                }
            }
        }
        
        ArrayList<ArrayList<Plot>> plotList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            plotList.add(new ArrayList<Plot>());
        }
        
        for (Plot plot : PS.get().getPlots()) {
            int count = 0;
            if (uuids.size() > 0) {
                for (UUID uuid : uuids) {
                    if (plot.isOwner(uuid)) {
                        count += 2;
                    }
                    else if (plot.isAdded(uuid)) {
                        count++;
                    }
                }
            }
            if (id != null) {
                if (plot.id.equals(id)) {
                    count++;
                }
            }
            if (world != null && plot.world.equals(world)) {
                count++;
            }
            if (alias != null && alias.equals(plot.getSettings().getAlias())) {
                count+=2;
            }
            if (count != 0) {
                plotList.get(count - 1).add(plot);
            }
        }
        
        List<Plot> plots = new ArrayList<Plot>();
        for (int i = plotList.size() - 1; i >= 0; i--) {
            if (plotList.get(i).size() > 0) {
                plots.addAll(plotList.get(i));
            }
        }
        return plots;
    }
    
    public static Plot getPlotFromString(PlotPlayer player, String arg, boolean message) {
        if (arg == null) {
            if (player == null) {
                if (message) MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD);
                return null;
            }
            return getPlot(player.getLocation());
        }
        String worldname = null;
        PlotId id = null;
        if (player != null) {
            worldname = player.getLocation().getWorld();
        }
        String[] split = arg.split(";|,");
        if (split.length == 3) {
            worldname = split[0];
            id = PlotId.fromString(split[1] + ";" + split[2]);
        }
        else if (split.length == 2) {
            id = PlotId.fromString(arg);
        }
        else {
            if (worldname == null) {
                if (PS.get().getPlotWorlds().size() == 0) {
                    if (message) MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD);
                    return null;
                }
                worldname = PS.get().getPlotWorlds().iterator().next();
            }
            for (Plot p : PS.get().getPlotsInWorld(worldname)) {
                String name = p.getSettings().getAlias();
                if (name.length() != 0 && name.equalsIgnoreCase(arg)) {
                    return p;
                }
            }
            for (String world : PS.get().getPlotWorlds()) {
                if (!world.endsWith(worldname)) {
                    for (Plot p : PS.get().getPlotsInWorld(world)) {
                        String name = p.getSettings().getAlias();
                        if (name.length() != 0 && name.equalsIgnoreCase(arg)) {
                            return p;
                        }  
                    }
                }
            }
        }
        if (worldname == null || !PS.get().isPlotWorld(worldname)) {
            if (message) MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD);
            return null;
        }
        if (id == null) {
            if (message) MainUtil.sendMessage(player, C.NOT_VALID_PLOT_ID); 
            return null;
        }
        return getPlot(worldname, id);
    }
    
    /**
     * Merges all plots in the arraylist (with cost)
     *
     * @param world
     * @param plotIds
     *
     * @return boolean
     */
    public static boolean mergePlots(final PlotPlayer player, final String world, final ArrayList<PlotId> plotIds) {
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        if ((EconHandler.manager != null) && plotworld.USE_ECONOMY) {
            final double cost = plotIds.size() * plotworld.MERGE_PRICE;
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(player) < cost) {
                    MainUtil.sendMessage(player, C.CANNOT_AFFORD_MERGE, "" + cost);
                    return false;
                }
                EconHandler.manager.withdrawMoney(player, cost);
                MainUtil.sendMessage(player, C.REMOVED_BALANCE, cost + "");
            }
        }
        return MainUtil.mergePlots(world, plotIds, true, true);
    }
    
    public static boolean unlinkPlot(final Plot plot) {
        final String world = plot.world;
        final PlotId pos1 = MainUtil.getBottomPlot(plot).id;
        final PlotId pos2 = MainUtil.getTopPlot(plot).id;
        final ArrayList<PlotId> ids = MainUtil.getPlotSelectionIds(pos1, pos2);
        final boolean result = EventUtil.manager.callUnlink(world, ids);
        if (!result) {
            return false;
        }
        final PlotManager manager = PS.get().getPlotManager(world);
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        manager.startPlotUnlink(plotworld, ids);
        for (final PlotId id : ids) {
            final Plot myplot = PS.get().getPlot(world, id);
            if (plot == null) {
                continue;
            }
            if (plot.trusted != null) {
                myplot.trusted = plot.trusted;
            }
            if (plot.denied != null) {
                myplot.denied = plot.denied;
            }
            myplot.getSettings().setMerged(new boolean[] { false, false, false, false });
            DBFunc.setMerged(myplot, myplot.getSettings().getMerged());
        }
        if (plotworld.TERRAIN != 3) {
            for (int x = pos1.x; x <= pos2.x; x++) {
                for (int y = pos1.y; y <= pos2.y; y++) {
                    final boolean lx = x < pos2.x;
                    final boolean ly = y < pos2.y;
                    final Plot p = MainUtil.getPlot(world, new PlotId(x, y));
                    if (lx) {
                        manager.createRoadEast(plotworld, p);
                        if (ly) {
                            manager.createRoadSouthEast(plotworld, p);
                        }
                    }
                    if (ly) {
                        manager.createRoadSouth(plotworld, p);
                    }
                    MainUtil.setSign(UUIDHandler.getName(plot.owner), plot);
                }
            }
        }
        manager.finishPlotUnlink(plotworld, ids);
        for (final PlotId id : ids) {
            final Plot myPlot = MainUtil.getPlot(world, id);
            if (plot.hasOwner()) {
                final String name = UUIDHandler.getName(myPlot.owner);
                if (name != null) {
                    MainUtil.setSign(name, myPlot);
                }
            }
        }
        return true;
    }
    
    public static boolean isPlotAreaAbs(final Location location) {
        final PlotWorld plotworld = PS.get().getPlotWorld(location.getWorld());
        if (plotworld == null) {
            return false;
        }
        if (plotworld.TYPE == 2) {
            return ClusterManager.getClusterAbs(location) != null;
        }
        return true;
    }
    
    public static boolean isPlotRoad(final Location location) {
        final PlotWorld plotworld = PS.get().getPlotWorld(location.getWorld());
        if (plotworld.TYPE == 2) {
            PlotCluster cluster = ClusterManager.getCluster(location);
            if (cluster == null) {
                return false;
            }
        }
        PlotManager manager = PS.get().getPlotManager(location.getWorld());
        return manager.getPlotId(plotworld, location.getX(), location.getY(), location.getZ()) == null;
    }
    
    public static boolean isPlotArea(final Plot plot) {
        final PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
        if (plotworld.TYPE == 2) {
            return ClusterManager.getCluster(plot) != null;
        }
        return true;
    }
    
    public static boolean enteredPlot(final Location l1, final Location l2) {
        final PlotId p1 = MainUtil.getPlotId(l1);
        final PlotId p2 = MainUtil.getPlotId(l2);
        return (p2 != null) && ((p1 == null) || !p1.equals(p2));
    }

    public static boolean leftPlot(final Location l1, final Location l2) {
        final PlotId p1 = MainUtil.getPlotId(l1);
        final PlotId p2 = MainUtil.getPlotId(l2);
        return (p1 != null) && ((p2 == null) || !p1.equals(p2));
    }
    
    public static ArrayList<PlotId> getMaxPlotSelectionIds(final String world, PlotId pos1, PlotId pos2) {

        final Plot plot1 = PS.get().getPlot(world, pos1);
        final Plot plot2 = PS.get().getPlot(world, pos2);
        
        if (plot1 != null) {
            pos1 = getBottomPlot(plot1).id;
        }
        
        if (plot2 != null) {
            pos2 = getTopPlot(plot2).id;
        }
        
        final ArrayList<PlotId> myplots = new ArrayList<>();
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                myplots.add(new PlotId(x, y));
            }
        }
        return myplots;
    }
    
    public static boolean equals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null ^ b == null) {
            return false;
        }
        return a.equals(b);
    }

    /**
     * Get the number of plots for a player
     *
     * @param plr
     *
     * @return int plot count
     */
    public static int getPlayerPlotCount(final String world, final PlotPlayer plr) {
        final UUID uuid = plr.getUUID();
        int count = 0;
        for (final Plot plot : PS.get().getPlotsInWorld(world)) {
            if (plot.hasOwner() && plot.owner.equals(uuid) && (!Settings.DONE_COUNTS_TOWARDS_LIMIT || !plot.getSettings().flags.containsKey("done"))) {
                count++;
            }
        }
        return count;
    }

    public static int getPlayerPlotCount(final PlotPlayer plr) {
        int count = 0;
        for (final String world : PS.get().getPlotWorldsString()) {
            count += getPlayerPlotCount(world, plr);
        }
        return count;
    }
    
    public static Location getDefaultHome(Plot plot) {
        PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
        if (plotworld.DEFAULT_HOME != null) {
            final Location bot = getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
            final PlotManager manager = PS.get().getPlotManager(plot.world);
            final int x;
            final int z;
            if (plotworld.DEFAULT_HOME.x == Integer.MAX_VALUE && plotworld.DEFAULT_HOME.z == Integer.MAX_VALUE) {
                final Location top = getPlotTopLoc(plot.world, plot.id);
                x = ((top.getX() - bot.getX()) / 2) + bot.getX();
                z = ((top.getZ() - bot.getZ()) / 2) + bot.getZ();
            }
            else {
                x = bot.getX() + plotworld.DEFAULT_HOME.x;
                z = bot.getZ() + plotworld.DEFAULT_HOME.z;
            }
            final int y = Math.max(getHeighestBlock(plot.world, x, z), manager.getSignLoc(PS.get().getPlotWorld(plot.world), plot).getY());
            return new Location(plot.world, x, y + 1, z);
        }
        final Location top = getPlotTopLoc(plot.world, plot.id);
        final Location bot = getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final int x = ((top.getX() - bot.getX()) / 2) + bot.getX();
        final int z = bot.getZ();
        PlotManager manager = PS.get().getPlotManager(plot.world);
        final int y = Math.max(getHeighestBlock(plot.world, x, z), manager.getSignLoc(PS.get().getPlotWorld(plot.world), plot).getY());
        return new Location(plot.world, x, y + 1, z);
    }

    public static boolean teleportPlayer(final PlotPlayer player, final Location from, final Plot plot) {
        final Plot bot = MainUtil.getBottomPlot(plot);

        boolean result = EventUtil.manager.callTeleport(player, from, plot);

        if (result) {
            final Location location;
            if (PS.get().getPlotWorld(plot.world).HOME_ALLOW_NONMEMBER || plot.isAdded(player.getUUID())) {
                location = MainUtil.getPlotHome(bot);
            }
            else {
                location = getDefaultHome(plot);
            }
            if ((Settings.TELEPORT_DELAY == 0) || Permissions.hasPermission(player, "plots.teleport.delay.bypass")) {
                sendMessage(player, C.TELEPORTED_TO_PLOT);
                player.teleport(location);
                return true;
            }
            sendMessage(player, C.TELEPORT_IN_SECONDS, Settings.TELEPORT_DELAY + "");
            final String name = player.getName();
            TaskManager.TELEPORT_QUEUE.add(name);
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    if (!TaskManager.TELEPORT_QUEUE.contains(name)) {
                        sendMessage(player, C.TELEPORT_FAILED);
                        return;
                    }
                    TaskManager.TELEPORT_QUEUE.remove(name);
                    if (!player.isOnline()) {
                        return;
                    }
                    sendMessage(player, C.TELEPORTED_TO_PLOT);
                    player.teleport(location);
                }
            }, Settings.TELEPORT_DELAY * 20);
            return true;
        }
        return result;
    }

    public static int getBorder(final String worldname) {
        if (worldBorder.containsKey(worldname)) {
            int border = worldBorder.get(worldname) + 16;
            if (border == 0) {
                return Integer.MAX_VALUE;
            }
            else {
                return border;
            }
        }
        return Integer.MAX_VALUE;
    }

    public static void setupBorder(final String world) {
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        if (!plotworld.WORLD_BORDER) {
            return;
        }
        if (!worldBorder.containsKey(world)) {
            worldBorder.put(world, 0);
        }
        for (final Plot plot : PS.get().getPlotsInWorld(world)) {
            updateWorldBorder(plot);
        }
    }

    public static void update(String world, ChunkLoc loc) {
        BlockUpdateUtil.setBlockManager.update(world, Arrays.asList(loc));
    }
    
    public static void update(final Plot plot) {
        Location bot = getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        Location top = getPlotTopLoc(plot.world, plot.id);
        
        int bx = bot.getX() >> 4;
        int bz = bot.getZ() >> 4;
        
        int tx = (top.getX() >> 4);
        int tz = (top.getZ() >> 4);
        
        ArrayList<ChunkLoc> chunks = new ArrayList<>();
        
        for (int x = bx; x <= tx; x++) {
            for (int z = bz; z <= tz; z++) {
                chunks.add(new ChunkLoc(x, z));
            }
        }
        BlockUpdateUtil.setBlockManager.update(plot.world, chunks);
    }

    public static void createWorld(final String world, final String generator) {
    }

    public static PlotId parseId(final String arg) {
        try {
            final String[] split = arg.split(";");
            return new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * direction 0 = north, 1 = south, etc:
     *
     * @param id
     * @param direction
     *
     * @return PlotId relative
     */
    public static PlotId getPlotIdRelative(final PlotId id, final int direction) {
        switch (direction) {
            case 0:
                return new PlotId(id.x, id.y - 1);
            case 1:
                return new PlotId(id.x + 1, id.y);
            case 2:
                return new PlotId(id.x, id.y + 1);
            case 3:
                return new PlotId(id.x - 1, id.y);
        }
        return id;
    }

    public static ArrayList<PlotId> getPlotSelectionIds(final PlotId pos1, final PlotId pos2) {
        final ArrayList<PlotId> myplots = new ArrayList<>();
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                myplots.add(new PlotId(x, y));
            }
        }
        return myplots;
    }

    /**
     * Completely merges a set of plots<br> <b>(There are no checks to make sure you supply the correct
     * arguments)</b><br> - Misuse of this method can result in unusable plots<br> - the set of plots must belong to one
     * owner and be rectangular<br> - the plot array must be sorted in ascending order<br> - Road will be removed where
     * required<br> - changes will be saved to DB<br>
     *
     * @param world
     * @param plotIds
     *
     * @return boolean (success)
     */
    public static boolean mergePlots(final String world, final ArrayList<PlotId> plotIds, final boolean removeRoads, final boolean updateDatabase) {
        if (plotIds.size() < 2) {
            return false;
        }
        
//        merged plots set db before finished merging
        
        final PlotId pos1 = plotIds.get(0);
        final PlotId pos2 = plotIds.get(plotIds.size() - 1);
        final PlotManager manager = PS.get().getPlotManager(world);
        final PlotWorld plotworld = PS.get().getPlotWorld(world);

        boolean result = EventUtil.manager.callMerge(world, getPlot(world, pos1), plotIds);
        if (!result) {
            return false;
        }
        
        HashSet<UUID> trusted = new HashSet<UUID>();
        HashSet<UUID> members = new HashSet<UUID>();
        HashSet<UUID> denied = new HashSet<UUID>();

        manager.startPlotMerge(plotworld, plotIds);
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                final Plot plot = PS.get().getPlot(world, id);
                trusted.addAll(plot.getTrusted());
                members.addAll(plot.getMembers());
                denied.addAll(plot.getDenied());
                if (removeRoads) {
                    removeSign(plot);
                }
            }
        }
        members.removeAll(trusted);
        denied.removeAll(trusted);
        denied.removeAll(members);
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final boolean lx = x < pos2.x;
                final boolean ly = y < pos2.y;
                final PlotId id = new PlotId(x, y);
                final Plot plot = PS.get().getPlot(world, id);
                plot.setTrusted(trusted);
                plot.setMembers(members);
                plot.setDenied(denied);
                Plot plot2 = null;
                if (lx) {
                    if (ly) {
                        if (!plot.getSettings().getMerged(1) || !plot.getSettings().getMerged(2)) {
                            if (removeRoads) { 
                                MainUtil.removeRoadSouthEast(plotworld, plot);
                            }
                        }
                    }
                    if (!plot.getSettings().getMerged(1)) {
                        plot2 = PS.get().getPlot(world, new PlotId(x + 1, y));
                        mergePlot(world, plot, plot2, removeRoads);
                        plot.getSettings().setMerged(1, true);
                        plot2.getSettings().setMerged(3, true);
                    }
                }
                if (ly) {
                    if (!plot.getSettings().getMerged(2)) {
                        plot2 = PS.get().getPlot(world, new PlotId(x, y + 1));
                        mergePlot(world, plot, plot2, removeRoads);
                        plot.getSettings().setMerged(2, true);
                        plot2.getSettings().setMerged(0, true);
                    }
                }
            }
        }
        manager.finishPlotMerge(plotworld, plotIds);
        if (updateDatabase) {
            for (int x = pos1.x; x <= pos2.x; x++) {
                for (int y = pos1.y; y <= pos2.y; y++) {
                    final PlotId id = new PlotId(x, y);
                    final Plot plot = PS.get().getPlot(world, id);
                    DBFunc.setMerged(plot, plot.getSettings().getMerged());
                }
            }
        }
        return true;
    }
    
    public static void removeRoadSouthEast(PlotWorld plotworld, Plot plot) {
        if (plotworld.TYPE != 0 && plotworld.TERRAIN > 1) {
            if (plotworld.TERRAIN == 3) {
                return;
            }
            PlotId id = plot.id;
            PlotId id2 = new PlotId(id.x + 1, id.y + 1);
            Location pos1 = getPlotTopLocAbs(plot.world, id).add(1, 0, 1);
            Location pos2 = getPlotBottomLocAbs(plot.world, id2);
            pos1.setY(0);
            pos2.setY(256);
            ChunkManager.manager.regenerateRegion(pos1, pos2, null);
        }
        else {
            PS.get().getPlotManager(plot.world).removeRoadSouthEast(plotworld, plot);
        }
    }
    
    public static void removeRoadEast(PlotWorld plotworld, Plot plot) {
        if (plotworld.TYPE != 0 && plotworld.TERRAIN > 1) {
            if (plotworld.TERRAIN == 3) {
                return;
            }
            PlotId id = plot.id;
            PlotId id2 = new PlotId(id.x + 1, id.y);
            Location bot = getPlotBottomLocAbs(plot.world, id2);
            Location top = getPlotTopLocAbs(plot.world, id);
            Location pos1 = new Location(plot.world, top.getX() + 1, 0, bot.getZ() + 1);
            Location pos2 = new Location(plot.world, bot.getX(), 256, top.getZ());
            ChunkManager.manager.regenerateRegion(pos1, pos2, null);
        }
        else {
            PS.get().getPlotManager(plot.world).removeRoadEast(plotworld, plot);
        }
    }
    
    public static void removeRoadSouth(PlotWorld plotworld, Plot plot) {
        if (plotworld.TYPE != 0 && plotworld.TERRAIN > 1) {
            if (plotworld.TERRAIN == 3) {
                return;
            }
            PlotId id = plot.id;
            PlotId id2 = new PlotId(id.x, id.y + 1);
            Location bot = getPlotBottomLocAbs(plot.world, id2);
            Location top = getPlotTopLocAbs(plot.world, id);
            Location pos1 = new Location(plot.world, bot.getX() + 1, 0, top.getZ() + 1);
            Location pos2 = new Location(plot.world, top.getX(), 256, bot.getZ());
            ChunkManager.manager.regenerateRegion(pos1, pos2, null);
        }
        else {
            PS.get().getPlotManager(plot.world).removeRoadSouth(plotworld, plot);
        }
    }

    
    /**
     * Merges 2 plots Removes the road inbetween <br> - Assumes the first plot parameter is lower <br> - Assumes neither
     * are a Mega-plot <br> - Assumes plots are directly next to each other <br> - Does not save to DB
     *
     * @param world
     * @param lesserPlot
     * @param greaterPlot
     */
    public static void mergePlot(final String world, final Plot lesserPlot, final Plot greaterPlot, final boolean removeRoads) {
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        if (lesserPlot.id.x.equals(greaterPlot.id.x)) {
            if (!lesserPlot.getSettings().getMerged(2)) {
                lesserPlot.getSettings().setMerged(2, true);
                greaterPlot.getSettings().setMerged(0, true);
                if (removeRoads) {
                    MainUtil.removeRoadSouth(plotworld, lesserPlot);
                }
            }
        } else {
            if (!lesserPlot.getSettings().getMerged(1)) {
                lesserPlot.getSettings().setMerged(1, true);
                greaterPlot.getSettings().setMerged(3, true);
                if (removeRoads) {
                    MainUtil.removeRoadEast(plotworld, lesserPlot);
                }
            }
        }
    }

    public static void removeSign(final Plot p) {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    removeSign(p);
                }
            });
            return;
        }
        final String world = p.world;
        final PlotManager manager = PS.get().getPlotManager(world);
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        if (!plotworld.ALLOW_SIGNS) {
            return;
        }
        final Location loc = manager.getSignLoc(plotworld, p);
        BlockManager.setBlocks(world, new int[]{loc.getX()}, new int[]{loc.getY()}, new int[]{loc.getZ()}, new int[]{0}, new byte[]{0});
    }
    
    public static void setSign(final Plot p) {
        if (p.owner == null) {
            setSign(null, p);
            return;
        }
        setSign(UUIDHandler.getName(p.owner), p);
    }

    public static void setSign(final String name, final Plot p) {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    setSign(name, p);
                }
            });
            return;
        }
        String rename = name == null ? "unknown" : name; 
        final PlotManager manager = PS.get().getPlotManager(p.world);
        final PlotWorld plotworld = PS.get().getPlotWorld(p.world);
        if (plotworld.ALLOW_SIGNS) {
        final Location loc = manager.getSignLoc(plotworld, p);
            final String id = p.id.x + ";" + p.id.y;
            final String[] lines = new String[] { C.OWNER_SIGN_LINE_1.formatted().replaceAll("%id%", id), C.OWNER_SIGN_LINE_2.formatted().replaceAll("%id%", id).replaceAll("%plr%", rename), C.OWNER_SIGN_LINE_3.formatted().replaceAll("%id%", id).replaceAll("%plr%", rename), C.OWNER_SIGN_LINE_4.formatted().replaceAll("%id%", id).replaceAll("%plr%", rename) };
            BlockManager.setSign(p.world, loc.getX(), loc.getY(), loc.getZ(), lines);
        }
    }

    public static String getStringSized(final int max, final String string) {
        if (string.length() > max) {
            return string.substring(0, max);
        }
        return string;
    }

    public static void autoMerge(final Plot plot, final UUID uuid, boolean removeRoads) {
        if (plot == null) {
            return;
        }
        if (plot.owner == null) {
            return;
        }
        if (!plot.owner.equals(uuid)) {
            return;
        }
        ArrayList<PlotId> plots;
        boolean merge = true;
        int count = 0;
        ArrayList<PlotId> toUpdate = new ArrayList<>();
        while (merge) {
            if (count > 16) {
                break;
            }
            merge = false;
            count++;
            final PlotId bot = getBottomPlot(plot).id;
            final PlotId top = getTopPlot(plot).id;
            plots = getPlotSelectionIds(new PlotId(bot.x, bot.y - 1), new PlotId(top.x, top.y));
            if (ownsPlots(plot.world, plots, uuid, 0)) {
                final boolean result = mergePlots(plot.world, plots, removeRoads, false);
                if (result) {
                    toUpdate.addAll(plots);
                    merge = true;
                    continue;
                }
            }
            plots = getPlotSelectionIds(new PlotId(bot.x, bot.y), new PlotId(top.x + 1, top.y));
            if (ownsPlots(plot.world, plots, uuid, 1)) {
                final boolean result = mergePlots(plot.world, plots, removeRoads, false);
                if (result) {
                    toUpdate.addAll(plots);
                    merge = true;
                    continue;
                }
            }
            plots = getPlotSelectionIds(new PlotId(bot.x, bot.y), new PlotId(top.x, top.y + 1));
            if (ownsPlots(plot.world, plots, uuid, 2)) {
                final boolean result = mergePlots(plot.world, plots, removeRoads, false);
                if (result) {
                    toUpdate.addAll(plots);
                    merge = true;
                    continue;
                }
            }
            plots = getPlotSelectionIds(new PlotId(bot.x - 1, bot.y), new PlotId(top.x, top.y));
            if (ownsPlots(plot.world, plots, uuid, 3)) {
                final boolean result = mergePlots(plot.world, plots, removeRoads, false);
                if (result) {
                    toUpdate.addAll(plots);
                    merge = true;
                    continue;
                }
            }
        }
        for (PlotId id : toUpdate) {
            DBFunc.setMerged(plot, plot.getSettings().getMerged());
        }
    }

    private static boolean ownsPlots(final String world, final ArrayList<PlotId> plots, final UUID uuid, final int dir) {
        final PlotId id_min = plots.get(0);
        final PlotId id_max = plots.get(plots.size() - 1);
        for (final PlotId myid : plots) {
            final Plot myplot = PS.get().getPlot(world, myid);
            if ((myplot == null) || myplot.owner == null || !(myplot.owner.equals(uuid))) {
                return false;
            }
            final PlotId top = getTopPlot(myplot).id;
            if (((top.x > id_max.x) && (dir != 1)) || ((top.y > id_max.y) && (dir != 2))) {
                return false;
            }
            final PlotId bot = getBottomPlot(myplot).id;
            if (((bot.x < id_min.x) && (dir != 3)) || ((bot.y < id_min.y) && (dir != 0))) {
                return false;
            }
        }
        return true;
    }

    public static void updateWorldBorder(final Plot plot) {
        if (!worldBorder.containsKey(plot.world)) {
            return;
        }
        final String world = plot.world;
        final PlotManager manager = PS.get().getPlotManager(world);
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        PlotId id = new PlotId(Math.abs(plot.id.x) + 1, Math.abs(plot.id.x) + 1);
        final Location bot = manager.getPlotBottomLocAbs(plotworld, id);
        final Location top = manager.getPlotTopLocAbs(plotworld, id);
        final int border = worldBorder.get(plot.world);
        final int botmax = Math.max(Math.abs(bot.getX()), Math.abs(bot.getZ()));
        final int topmax = Math.max(Math.abs(top.getX()), Math.abs(top.getZ()));
        final int max = Math.max(botmax, topmax);
        if (max > border) {
            worldBorder.put(plot.world, max);
        }
    }

    /**
     * Create a plot and notify the world border and plot merger
     */
    public static boolean createPlot(final UUID uuid, final Plot plot) {
        if (MainUtil.worldBorder.containsKey(plot.world)) {
            updateWorldBorder(plot);
        }
        final String w = plot.world;
        if (PS.get().getPlot(plot.world, plot.id) != null) {
            return true;
        }
        final Plot p = new Plot(w, plot.id, uuid);
        if (p.owner == null) {
            return false;
        }
        PS.get().updatePlot(p);
        DBFunc.createPlotAndSettings(p, new Runnable() {
            @Override
            public void run() {
                final PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
                if (plotworld.AUTO_MERGE) {
                    autoMerge(p, uuid, true);
                }
            }
        });
        return true;
    }

    /**
     * Create a plot without notifying the merge function or world border manager
     */
    public static Plot createPlotAbs(final UUID uuid, final Plot plot) {
        final String w = plot.world;
        Plot p = PS.get().getPlot(plot.world, plot.id);
        if (p != null) {
            return p;
        }
        p = new Plot(w, plot.id, uuid);
        if (p.owner == null) {
            return null;
        }
        PS.get().updatePlot(p);
        DBFunc.createPlotAndSettings(p, null);
        return p;
    }

    public static String createId(final int x, final int z) {
        return x + ";" + z;
    }

    public static int square(final int x) {
        return x * x;
    }

    public static short[] getBlock(final String block) {
        if (block.contains(":")) {
            final String[] split = block.split(":");
            return new short[] { Short.parseShort(split[0]), Short.parseShort(split[1]) };
        }
        return new short[] { Short.parseShort(block), 0 };
    }

    /**
     * Clear a plot and associated sections: [sign, entities, border]
     *
     * @param plot
     * @param isDelete
     * @param whenDone
     */
    public static boolean clearAsPlayer(final Plot plot, final boolean isDelete, final Runnable whenDone) {
        if (runners.containsKey(plot)) {
            return false;
        }
        long start = System.currentTimeMillis();
        ChunkManager.manager.clearAllEntities(plot.getBottom().add(1, 0, 1), plot.getTop());
        if (isDelete) {
            removeSign(plot);
        }
        clear(plot, isDelete, whenDone);
        return true;
    }

    public static void clear(final Plot plot, final boolean isDelete, final Runnable whenDone) {
        final PlotManager manager = PS.get().getPlotManager(plot.world);
        final Location pos1 = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final int prime = 31;
        int h = 1;
        h = (prime * h) + pos1.getX();
        h = (prime * h) + pos1.getZ();
        state = h;
        System.currentTimeMillis();
        final PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
        runners.put(plot, 1);
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                if (isDelete) {
                    manager.unclaimPlot(plotworld, plot, new Runnable() {
                        @Override
                        public void run() {
                            runners.remove(plot);
                            TaskManager.runTask(whenDone);
                        }
                    });
                }
                else {
                    runners.remove(plot);
                    TaskManager.runTask(whenDone);
                }
            }
        };
        if (plotworld.TERRAIN != 0 || Settings.FAST_CLEAR) {
            final Location pos2 = MainUtil.getPlotTopLoc(plot.world, plot.id);
            ChunkManager.manager.regenerateRegion(pos1, pos2, run);
            return;
        }
        manager.clearPlot(plotworld, plot, run);
    }

    public static void setCuboid(final String world, final Location pos1, final Location pos2, final PlotBlock[] blocks) {
        if (blocks.length == 1) {
            setSimpleCuboid(world, pos1, pos2, blocks[0]);
            return;
        }
        final int length = (pos2.getX() - pos1.getX()) * (pos2.getY() - pos1.getY()) * (pos2.getZ() - pos1.getZ());
        final int[] xl = new int[length];
        final int[] yl = new int[length];
        final int[] zl = new int[length];
        final int[] ids = new int[length];
        final byte[] data = new byte[length];
        int index = 0;
        for (int y = pos1.getY(); y < pos2.getY(); y++) {
            for (int x = pos1.getX(); x < pos2.getX(); x++) {
                for (int z = pos1.getZ(); z < pos2.getZ(); z++) {
                    final int i = random.random(blocks.length);
                    xl[index] = x;
                    yl[index] = y;
                    zl[index] = z;
                    final PlotBlock block = blocks[i];
                    ids[index] = block.id;
                    data[index] = block.data;
                    index++;
                }
            }
        }
        BlockManager.setBlocks(world, xl, yl, zl, ids, data);
    }
    
    public static void setCuboidAsync(final String world, final Location pos1, final Location pos2, final PlotBlock[] blocks) {
        if (blocks.length == 1) {
            setSimpleCuboidAsync(world, pos1, pos2, blocks[0]);
            return;
        }
        for (int y = pos1.getY(); y < Math.min(256, pos2.getY()); y++) {
            for (int x = pos1.getX(); x < pos2.getX(); x++) {
                for (int z = pos1.getZ(); z < pos2.getZ(); z++) {
                    final int i = random.random(blocks.length);
                    final PlotBlock block = blocks[i];
                    SetBlockQueue.setBlock(world, x, y, z, block);
                }
            }
        }
    }

    public static void setSimpleCuboid(final String world, final Location pos1, final Location pos2, final PlotBlock newblock) {
        final int length = (pos2.getX() - pos1.getX()) * (pos2.getY() - pos1.getY()) * (pos2.getZ() - pos1.getZ());
        final int[] xl = new int[length];
        final int[] yl = new int[length];
        final int[] zl = new int[length];
        final int[] ids = new int[length];
        final byte[] data = new byte[length];
        int index = 0;
        for (int y = pos1.getY(); y < pos2.getY(); y++) {
            for (int x = pos1.getX(); x < pos2.getX(); x++) {
                for (int z = pos1.getZ(); z < pos2.getZ(); z++) {
                    xl[index] = x;
                    yl[index] = y;
                    zl[index] = z;
                    ids[index] = newblock.id;
                    data[index] = newblock.data;
                    index++;
                }
            }
        }
        BlockManager.setBlocks(world, xl, yl, zl, ids, data);
    }
    
    public static void setSimpleCuboidAsync(final String world, final Location pos1, final Location pos2, final PlotBlock newblock) {
        for (int y = pos1.getY(); y < Math.min(256, pos2.getY()); y++) {
            for (int x = pos1.getX(); x < pos2.getX(); x++) {
                for (int z = pos1.getZ(); z < pos2.getZ(); z++) {
                    SetBlockQueue.setBlock(world, x, y, z, newblock);
                }
            }
        }
    }

    public static void setBiome(final Plot plot, final String biome, final Runnable whenDone) {
        Location pos1 = plot.getBottom().add(1, 0, 1);
        Location pos2 = plot.getTop();
        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override
            public void run() {
                ChunkLoc loc = new ChunkLoc(value[0], value[1]);
                ChunkManager.manager.loadChunk(plot.world, loc, false);
                setBiome(plot.world, value[2], value[3], value[4], value[5], biome);
                ChunkManager.manager.unloadChunk(plot.world, loc, true, true);
            }
        }, new Runnable() {
            @Override
            public void run() {
                update(plot);
                TaskManager.runTask(whenDone);
            }
        }, 5);
    }
    
    public static void setBiome(final String world, int p1x, int p1z, int p2x, int p2z, final String biome) {
        final int length = (p2x - p1x + 1) * (p2z - p1z + 1);
        final int[] xl = new int[length];
        final int[] zl = new int[length];
        int index = 0;
        for (int x = p1x; x <= p2x; x++) {
            for (int z = p1z; z <= p2z; z++) {
                xl[index] = x;
                zl[index] = z;
                index++;
            }
        }
        BlockManager.setBiomes(world, xl, zl, biome);
    }
    
    public static int getHeighestBlock(final String world, final int x, final int z) {
        final int result = BlockManager.manager.getHeighestBlock(world, x, z);
        if (result == 0) {
            return 64;
        }
        return result;
    }

    /**
     * Get plot home
     *
     * @param w      World in which the plot is located
     * @param plotid Plot ID
     *
     * @return Home Location
     */
    public static Location getPlotHome(final String w, final PlotId plotid) {
        final Plot plot = getPlot(w, plotid);
        final BlockLoc home = plot.getSettings().getPosition();
        final Location bot = getPlotBottomLoc(w, plotid);
        final PlotManager manager = PS.get().getPlotManager(w);
        if ((home == null) || ((home.x == 0) && (home.z == 0))) {
            return getDefaultHome(plot);
        } else {
            Location loc = new Location(bot.getWorld(), bot.getX() + home.x, bot.getY() + home.y, bot.getZ() + home.z);
            if (BlockManager.manager.getBlock(loc).id != 0) {
                // sendConsoleMessage("ID was " + BukkitUtil.getBlock(loc).id);
                loc.setY(Math.max(getHeighestBlock(w, bot.getX(), bot.getZ()), bot.getY()));
            }
            return loc;
        }
    }

    /**
     * Get the plot home
     *
     * @param plot Plot Object
     *
     * @return Plot Home Location
     *
     */
    public static Location getPlotHome(final Plot plot) {
        return getPlotHome(plot.world, plot.id);
    }

    /**
     * Gets the top plot location of a plot (all plots are treated as small plots) - To get the top loc of a mega plot
     * use getPlotTopLoc(...)
     *
     * @param world
     * @param id
     *
     * @return Location top
     */
    public static Location getPlotTopLocAbs(final String world, final PlotId id) {
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotManager manager = PS.get().getPlotManager(world);
        return manager.getPlotTopLocAbs(plotworld, id);
    }

    /**
     * Gets the bottom plot location of a plot (all plots are treated as small plots) - To get the top loc of a mega
     * plot use getPlotBottomLoc(...)
     *
     * @param world
     * @param id
     *
     * @return Location bottom
     */
    public static Location getPlotBottomLocAbs(final String world, final PlotId id) {
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotManager manager = PS.get().getPlotManager(world);
        return manager.getPlotBottomLocAbs(plotworld, id);
    }

    /**
     * Obtains the width of a plot (x width)
     *
     * @param world
     * @param id
     *
     * @return int width of plot
     */
    public static int getPlotWidth(final String world, final PlotId id) {
        return getPlotTopLoc(world, id).getX() - getPlotBottomLoc(world, id).getX();
    }

    /**
     * Gets the top loc of a plot (if mega, returns top loc of that mega plot) - If you would like each plot treated as
     * a small plot use getPlotTopLocAbs(...)
     *
     * @param world
     * @param id
     *
     * @return Location top of mega plot
     */
    public static Location getPlotTopLoc(final String world, PlotId id) {
        final Plot plot = PS.get().getPlot(world, id);
        if (plot != null) {
            id = getTopPlot(plot).id;
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotManager manager = PS.get().getPlotManager(world);
        return manager.getPlotTopLocAbs(plotworld, id);
    }

    /**
     * Gets the bottom loc of a plot (if mega, returns bottom loc of that mega plot) - If you would like each plot
     * treated as a small plot use getPlotBottomLocAbs(...)
     *
     * @param world
     * @param id
     *
     * @return Location bottom of mega plot
     */
    public static Location getPlotBottomLoc(final String world, PlotId id) {
        final Plot plot = PS.get().getPlot(world, id);
        if (plot != null) {
            id = getBottomPlot(plot).id;
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotManager manager = PS.get().getPlotManager(world);
        return manager.getPlotBottomLocAbs(plotworld, id);
    }

    public static boolean canClaim(PlotPlayer player, String world, final PlotId pos1, final PlotId pos2) {
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                Plot plot = getPlot(world, id);
                if (!canClaim(player, plot)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean canClaim(PlotPlayer player, Plot plot) {
        if (plot == null) {
            return false;
        }
        if (Settings.ENABLE_CLUSTERS) {
            PlotCluster cluster = ClusterManager.getCluster(plot);
            if (cluster != null) {
                if (!cluster.isAdded(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.claim")) {
                    return false;
                }
            }
        }
        return plot.owner == null;
    }
    
    public static boolean isUnowned(final String world, final PlotId pos1, final PlotId pos2) {
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                if (PS.get().getPlot(world, id) != null) {
                    if (PS.get().getPlot(world, id).owner != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public static boolean swap(final String world, final PlotId current, final PlotId newPlot, final Runnable whenDone) {
        Plot p1 = PS.get().getPlot(world, current);
        Plot p2 = PS.get().getPlot(world, newPlot);
        if (p1==null || p2 == null || p1.owner == null || !p1.owner.equals(p2.owner)) {
            return false;
        }
        // Swap blocks
        ChunkManager.manager.swap(world, current, newPlot);
        // Swap cached
        PlotId temp = new PlotId(p1.id.x.intValue(), p1.id.y.intValue());
        p1.id.x = p2.id.x.intValue();
        p1.id.y = p2.id.y.intValue();
        p2.id.x = temp.x;
        p2.id.y = temp.y;
        Map<String, ConcurrentHashMap<PlotId, Plot>> raw = PS.get().getAllPlotsRaw();
        raw.get(world).remove(p1.id);
        raw.get(world).remove(p2.id);
        p1.id.recalculateHash();
        p2.id.recalculateHash();
        raw.get(world).put(p1.id, p1);
        raw.get(world).put(p2.id, p2);
        // Swap database
        DBFunc.dbManager.swapPlots(p2, p1);
        return true;
    }
    
    public static boolean swapData(final String world, final PlotId current, final PlotId newPlot, final Runnable whenDone) {
        Plot p1 = PS.get().getPlot(world, current);
        Plot p2 = PS.get().getPlot(world, newPlot);
        if (p1 == null || p1.owner == null) {
            if (p2 != null && p2.owner != null) {
                moveData(p2, getPlot(world, current), whenDone);
                return true;
            }
            return false;
        }
        if (p2 == null || p2.owner == null) {
            if (p1 != null && p1.owner != null) {
                moveData(p1, getPlot(world, newPlot), whenDone);
                return true;
            }
            return false;
        }
        // Swap cached
        PlotId temp = new PlotId(p1.id.x.intValue(), p1.id.y.intValue());
        p1.id.x = p2.id.x.intValue();
        p1.id.y = p2.id.y.intValue();
        p2.id.x = temp.x;
        p2.id.y = temp.y;
        Map<String, ConcurrentHashMap<PlotId, Plot>> raw = PS.get().getAllPlotsRaw();
        raw.get(world).remove(p1.id);
        raw.get(world).remove(p2.id);
        p1.id.recalculateHash();
        p2.id.recalculateHash();
        raw.get(world).put(p1.id, p1);
        raw.get(world).put(p2.id, p2);
        // Swap database
        DBFunc.dbManager.swapPlots(p2, p1);
        TaskManager.runTask(whenDone);
        return true;
    }

    public static boolean moveData(final Plot plot1, final Plot plot2, final Runnable whenDone) {
        if (plot1.owner == null) {
            PS.debug(plot2 +" is unowned (single)");
            TaskManager.runTask(whenDone);
            return false;
        }
        final Plot pos1 = getBottomPlot(plot1);
        final Plot pos2 = getTopPlot(plot1);
        final PlotId size = MainUtil.getSize(plot1);
        if (!MainUtil.isUnowned(plot2.world, plot2.id, new PlotId((plot2.id.x + size.x) - 1, (plot2.id.y + size.y) - 1))) {
            PS.debug(plot2 +" is unowned (multi)");
            TaskManager.runTask(whenDone);
            return false;
        }
        final int offset_x = plot2.id.x - pos1.id.x;
        final int offset_y = plot2.id.y - pos1.id.y;
        final ArrayList<PlotId> selection = getPlotSelectionIds(pos1.id, pos2.id);
        for (final PlotId id : selection) {
            String worldOriginal = plot1.world;
            PlotId idOriginal = new PlotId(id.x, id.y);
            final Plot plot = PS.get().getPlot(plot1.world, id);
            Map<String, ConcurrentHashMap<PlotId, Plot>> raw = PS.get().getAllPlotsRaw();
            raw.get(plot1.world).remove(id);
            plot.id.x += offset_x;
            plot.id.y += offset_y;
            plot.id.recalculateHash();
            raw.get(plot2.world).put(plot.id, plot);
            DBFunc.movePlot(getPlot(worldOriginal, idOriginal), getPlot(plot2.world, new PlotId(id.x + offset_x, id.y + offset_y)));
        }
        TaskManager.runTaskLater(whenDone, 1);
        return true;
    }
    
    public static boolean move(final Plot plot1, final Plot plot2, final Runnable whenDone) {
        final com.intellectualcrafters.plot.object.Location bot1 = MainUtil.getPlotBottomLoc(plot1.world, plot1.id);
        final com.intellectualcrafters.plot.object.Location bot2 = MainUtil.getPlotBottomLoc(plot2.world, plot2.id);
        final Location top = MainUtil.getPlotTopLoc(plot1.world, plot1.id);
        if (plot1.owner == null) {
            PS.debug(plot2 +" is unowned (single)");
            TaskManager.runTask(whenDone);
            return false;
        }
        final Plot pos1 = getBottomPlot(plot1);
        final Plot pos2 = getTopPlot(plot1);
        final PlotId size = MainUtil.getSize(plot1);
        if (!MainUtil.isUnowned(plot2.world, plot2.id, new PlotId((plot2.id.x + size.x) - 1, (plot2.id.y + size.y) - 1))) {
            PS.debug(plot2 +" is unowned (multi)");
            TaskManager.runTask(whenDone);
            return false;
        }
        final int offset_x = plot2.id.x - pos1.id.x;
        final int offset_y = plot2.id.y - pos1.id.y;
        final ArrayList<PlotId> selection = getPlotSelectionIds(pos1.id, pos2.id);
        for (final PlotId id : selection) {
            String worldOriginal = plot1.world;
            PlotId idOriginal = new PlotId(id.x, id.y);
            final Plot plot = PS.get().getPlot(plot1.world, id);
            Map<String, ConcurrentHashMap<PlotId, Plot>> raw = PS.get().getAllPlotsRaw();
            raw.get(plot1.world).remove(id);
            plot.id.x += offset_x;
            plot.id.y += offset_y;
            plot.id.recalculateHash();
            raw.get(plot2.world).put(plot.id, plot);
            DBFunc.movePlot(getPlot(worldOriginal, idOriginal), getPlot(plot2.world, new PlotId(id.x + offset_x, id.y + offset_y)));
        }
        ChunkManager.manager.copyRegion(bot1, top, bot2, new Runnable() {
            @Override
            public void run() {
                final Location bot = bot1.clone().add(1, 0, 1);
                ChunkManager.manager.regenerateRegion(bot, top, null);
                TaskManager.runTaskLater(whenDone, 1);
            }
        });
        return true;
    }
    
    public static boolean copy(final String world, final PlotId current, final PlotId newPlot, final Runnable whenDone) {
        final com.intellectualcrafters.plot.object.Location bot1 = MainUtil.getPlotBottomLoc(world, current);
        final com.intellectualcrafters.plot.object.Location bot2 = MainUtil.getPlotBottomLoc(world, newPlot);
        final Location top = MainUtil.getPlotTopLoc(world, current);
        final Plot currentPlot = MainUtil.getPlot(world, current);
        if (currentPlot.owner == null) {
            TaskManager.runTaskLater(whenDone, 1);
            return false;
        }
        final Plot pos1 = getBottomPlot(currentPlot);
        final Plot pos2 = getTopPlot(currentPlot);
        final PlotId size = MainUtil.getSize(currentPlot);
        if (!MainUtil.isUnowned(world, newPlot, new PlotId((newPlot.x + size.x) - 1, (newPlot.y + size.y) - 1))) {
            TaskManager.runTaskLater(whenDone, 1);
            return false;
        }
        final ArrayList<PlotId> selection = getPlotSelectionIds(pos1.id, pos2.id);
        final int offset_x = newPlot.x - pos1.id.x;
        final int offset_y = newPlot.y - pos1.id.y;
        for (final PlotId id : selection) {
            int x = id.x + offset_x;
            int y = id.y + offset_y;
            Plot plot = createPlotAbs(currentPlot.owner, getPlot(world, new PlotId(x, y)));
            if (currentPlot.getSettings().flags != null && currentPlot.getSettings().flags.size() > 0) {
                plot.getSettings().flags = currentPlot.getSettings().flags;
                DBFunc.setFlags(plot, currentPlot.getSettings().flags.values());
            }
            if (currentPlot.isMerged()) {
                plot.getSettings().setMerged(currentPlot.getSettings().getMerged());
                DBFunc.setMerged(plot, currentPlot.getSettings().getMerged());
            }
            if (currentPlot.members != null && currentPlot.members.size() > 0) {
                plot.members = currentPlot.members;
                for (UUID member : plot.members) {
                    DBFunc.setMember(plot, member);
                }
            }
            if (currentPlot.trusted != null && currentPlot.trusted.size() > 0) {
                plot.trusted = currentPlot.trusted;
                for (UUID trusted : plot.trusted) {
                    DBFunc.setTrusted(plot, trusted);
                }
            }
            if (currentPlot.denied != null && currentPlot.denied.size() > 0) {
                plot.denied = currentPlot.denied;
                for (UUID denied : plot.denied) {
                    DBFunc.setDenied(plot, denied);
                }
            }
            PS.get().updatePlot(plot);
        }
        ChunkManager.manager.copyRegion(bot1, top, bot2, whenDone);
        return true;
    }

    /**
     * Send a message to the player
     *
     * @param plr Player to recieve message
     * @param msg Message to send
     *
     * @return true Can be used in things such as commands (return PlayerFunctions.sendMessage(...))
     */
    public static boolean sendMessage(final PlotPlayer plr, final String msg) {
        return sendMessage(plr, msg, true);
    }

    public static void sendConsoleMessage(String msg) {
        sendMessage(null, msg);
    }
    
    public static void sendConsoleMessage(C caption, String... args) {
        sendMessage(null, caption, args);
    }

    public static boolean sendMessage(final PlotPlayer plr, String msg, final boolean prefix) {
        if ((msg.length() > 0) && !msg.equals("")) {
            if (plr == null) {
                PS.log((prefix ? C.PREFIX.s() : "") + msg);
            } else {
                plr.sendMessage((prefix ? C.PREFIX.s() : "") + C.color(msg));
            }
        }
        return true;
    }

    public static String[] wordWrap(final String rawString, final int lineLength) {
        if (rawString == null) {
            return new String[] { "" };
        }
        if ((rawString.length() <= lineLength) && (!rawString.contains("\n"))) {
            return new String[] { rawString };
        }
        final char[] rawChars = (rawString + ' ').toCharArray();
        StringBuilder word = new StringBuilder();
        StringBuilder line = new StringBuilder();
        final ArrayList<String> lines = new ArrayList();
        int lineColorChars = 0;
        for (int i = 0; i < rawChars.length; i++) {
            final char c = rawChars[i];
            if (c == '\u00A7') {
                word.append('\u00A7' + (rawChars[(i + 1)]));
                lineColorChars += 2;
                i++;
            } else if ((c == ' ') || (c == '\n')) {
                if ((line.length() == 0) && (word.length() > lineLength)) {
                    for (final String partialWord : word.toString().split("(?<=\\G.{" + lineLength + "})")) {
                        lines.add(partialWord);
                    }
                } else if (((line.length() + word.length()) - lineColorChars) == lineLength) {
                    line.append(word);
                    lines.add(line.toString());
                    line = new StringBuilder();
                    lineColorChars = 0;
                } else if (((line.length() + 1 + word.length()) - lineColorChars) > lineLength) {
                    for (final String partialWord : word.toString().split("(?<=\\G.{" + lineLength + "})")) {
                        lines.add(line.toString());
                        line = new StringBuilder(partialWord);
                    }
                    lineColorChars = 0;
                } else {
                    if (line.length() > 0) {
                        line.append(' ');
                    }
                    line.append(word);
                }
                word = new StringBuilder();
                if (c == '\n') {
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
            } else {
                word.append(c);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        if ((lines.get(0).length() == 0) || (lines.get(0).charAt(0) != '\u00A7')) {
            lines.set(0, "\u00A7f" + lines.get(0));
        }
        for (int i = 1; i < lines.size(); i++) {
            final String pLine = lines.get(i - 1);
            final String subLine = lines.get(i);

            final char color = pLine.charAt(pLine.lastIndexOf('\u00A7') + 1);
            if ((subLine.length() == 0) || (subLine.charAt(0) != '\u00A7')) {
                lines.set(i, '\u00A7' + (color) + subLine);
            }
        }
        return lines.toArray(new String[lines.size()]);
    }

    /**
     * Send a message to the player
     *
     * @param plr Player to recieve message
     * @param c   Caption to send
     *
     * @return boolean success
     */
    public static boolean sendMessage(final PlotPlayer plr, final C c, final String... args) {
        if (c.s().length() > 1) {
            String msg = c.s();
            if ((args != null) && (args.length > 0)) {
                msg = C.format(c, args);
            }
            if (plr == null) {
                PS.log(msg);
            } else {
                sendMessage(plr, msg, c.usePrefix());
            }
        }
        return true;
    }
    
    /**
     * Send a message to the player
     *
     * @param plr Player to recieve message
     * @param c   Caption to send
     *
     * @return boolean success
     */
    public static boolean sendMessage(final PlotPlayer plr, final C c, final Object... args) {
        if (c.s().length() > 1) {
            String msg = c.s();
            if ((args != null) && (args.length > 0)) {
                msg = C.format(c, args);
            }
            if (plr == null) {
                PS.log(msg);
            } else {
                sendMessage(plr, msg, c.usePrefix());
            }
        }
        return true;
    }

    public static Plot getBottomPlot(final Plot plot) {
        if (plot.getSettings().getMerged(0)) {
            final Plot p = PS.get().getPlot(plot.world, new PlotId(plot.id.x, plot.id.y - 1));
            if (p == null) {
                return plot;
            }
            return getBottomPlot(p);
        }
        if (plot.getSettings().getMerged(3)) {
            final Plot p = PS.get().getPlot(plot.world, new PlotId(plot.id.x - 1, plot.id.y));
            if (p == null) {
                return plot;
            }
            return getBottomPlot(p);
        }
        return plot;
    }

    public static Plot getTopPlot(final Plot plot) {
        if (plot.getSettings().getMerged(2)) {
            final Plot p = PS.get().getPlot(plot.world, new PlotId(plot.id.x, plot.id.y + 1));
            if (p == null) {
                return plot;
            }
            return getTopPlot(p);
        }
        if (plot.getSettings().getMerged(1)) {
            final Plot p = PS.get().getPlot(plot.world, new PlotId(plot.id.x + 1, plot.id.y));
            if (p == null) {
                return plot;
            }
            return getTopPlot(p);
        }
        return plot;
        
    }

    public static PlotId getSize(final Plot plot) {
        if (!plot.isMerged()) {
            return new PlotId(1, 1);
        }
        final Plot top = getTopPlot(plot);
        final Plot bot = getBottomPlot(plot);
        return new PlotId((top.id.x - bot.id.x) + 1, (top.id.y - bot.id.y) + 1);
    }

    /**
     * Fetches the plot from the main class
     */
    public static Plot getPlot(final String world, final PlotId id) {
        if (id == null) {
            return null;
        }
        Plot plot = PS.get().getPlot(world, id);
        if (plot != null) {
            return plot;
        }
        return new Plot(world, id, null);
    }

    /**
     * Returns the plot at a location (mega plots are not considered, all plots are treated as small plots)
     * @param loc
     * @return PlotId underlying plot id of loc
     */
    public static PlotId getPlotAbs(final Location loc) {
        final String world = loc.getWorld();
        final PlotManager manager = PS.get().getPlotManager(world);
        if (manager == null) {
            return null;
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        return manager.getPlotIdAbs(plotworld, loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Returns the plot id at a location (mega plots are considered)
     * @param loc
     * @return PlotId PlotId observed id
     */
    public static PlotId getPlotId(final Location loc) {
        final String world = loc.getWorld();
        final PlotManager manager = PS.get().getPlotManager(world);
        if (manager == null) {
            return null;
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotId id = manager.getPlotId(plotworld, loc.getX(), loc.getY(), loc.getZ());
        if ((id != null) && (plotworld.TYPE == 2)) {
            if (ClusterManager.getCluster(world, id) == null) {
                return null;
            }
        }
        return id;
    }

    /**
     * Get the maximum number of plots a player is allowed
     *
     * @param p
     * @return int
     */
    public static int getAllowedPlots(final PlotPlayer p) {
        return Permissions.hasPermissionRange(p, "plots.plot", Settings.MAX_PLOTS);
    }

    public static Plot getPlot(final Location loc) {
        final PlotId id = getPlotId(loc);
        if (id == null) {
            return null;
        }
        return getPlot(loc.getWorld(), id);
    }

    public static double getAverageRating(Plot plot) {
        HashMap<UUID, Integer> rating;
        if (plot.getSettings().ratings != null) {
            rating = plot.getSettings().ratings;
        }
        else if (Settings.CACHE_RATINGS) {
            rating = new HashMap<>();
        }
        else {
            rating = DBFunc.getRatings(plot);
        }
        if (rating == null || rating.size() == 0) {
            return 0;
        }
        double val = 0;
        int size = 0;
        for (Entry<UUID, Integer> entry : rating.entrySet()) {
            int current = entry.getValue();
            if (Settings.RATING_CATEGORIES == null || Settings.RATING_CATEGORIES.size() == 0) {
                val += current;
                size++;
            }
            else {
                for (int i = 0 ; i < Settings.RATING_CATEGORIES.size(); i++) {
                    val += (current % 10) - 1;
                    current /= 10;
                    size++;
                }
            }
        }
        return val / (double) size;
    }
    
    public static double[] getAverageRatings(Plot plot) {
        HashMap<UUID, Integer> rating;
        if (plot.getSettings().ratings != null) {
            rating = plot.getSettings().ratings;
        }
        else if (Settings.CACHE_RATINGS) {
            rating = new HashMap<>();
        }
        else {
            rating = DBFunc.getRatings(plot);
        }
        int size = 1;
        if (Settings.RATING_CATEGORIES != null) {
            size = Math.max(1, Settings.RATING_CATEGORIES.size());
        }
        double[] ratings = new double[size];
        if (rating == null || rating.size() == 0) {
            return ratings;
        }
        for (Entry<UUID, Integer> entry : rating.entrySet()) {
            int current = entry.getValue();
            if (Settings.RATING_CATEGORIES == null || Settings.RATING_CATEGORIES.size() == 0) {
                ratings[0] += current;
            }
            else {
                for (int i = 0 ; i < Settings.RATING_CATEGORIES.size(); i++) {
                    ratings[i] += (current % 10) - 1;
                    current /= 10;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            ratings[i] /= (double) rating.size();
        }
        return ratings;
    }

    public static void setComponent(Plot plot, String component, PlotBlock[] blocks) {
        PS.get().getPlotManager(plot.world).setComponent(PS.get().getPlotWorld(plot.world), plot.id, component, blocks);
    }
}
