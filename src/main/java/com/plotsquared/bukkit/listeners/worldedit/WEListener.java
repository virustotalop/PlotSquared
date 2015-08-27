package com.plotsquared.bukkit.listeners.worldedit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.plotsquared.bukkit.BukkitMain;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.SetBlockFast;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.Selection;
 
public class WEListener implements Listener {
    
    public final HashSet<String> rad1 = new HashSet<>(Arrays.asList("forestgen", "pumpkins", "drain", "fixwater", "fixlava", "replacenear", "snow", "thaw", "ex", "butcher", "size"));
    public final HashSet<String> rad2 = new HashSet<>(Arrays.asList("fill", "fillr", "removenear", "remove"));
    public final HashSet<String> rad2_1 = new HashSet<>(Arrays.asList("hcyl", "cyl"));
    public final HashSet<String> rad2_2 = new HashSet<>(Arrays.asList("sphere", "pyramid"));
    public final HashSet<String> rad2_3 = new HashSet<>(Arrays.asList("brush smooth"));
    public final HashSet<String> rad3_1 = new HashSet<>(Arrays.asList("brush gravity"));
    public final HashSet<String> rad3_2 = new HashSet<>(Arrays.asList("brush sphere", "brush cylinder"));
    
    public final HashSet<String> region  = new HashSet<>(Arrays.asList("move", "set", "replace", "overlay", "walls", "outline", "deform", "hollow", "smooth", "naturalize", "paste", "count", "distr", "regen", "copy", "cut", "green", "setbiome"));
    public final HashSet<String> regionExtend = new HashSet<>(Arrays.asList("stack"));
    public final HashSet<String> unregioned = new HashSet<>(Arrays.asList("paste", "redo", "undo", "rotate", "flip", "generate", "schematic", "schem"));
    public final HashSet<String> unsafe1 =  new HashSet<>(Arrays.asList("cs", ".s", "restore", "snapshot", "delchunks", "listchunks"));
    public final HashSet<String> restricted = new HashSet<>(Arrays.asList("up"));
    public final HashSet<String> other = new HashSet<>(Arrays.asList("undo", "redo"));
    
    public boolean checkCommand(List<String> list, String cmd) {
        for (String identifier : list) {
            if (("/" + identifier).equals(cmd) || ("//" + identifier).equals(cmd) || ("/worldedit:/" + identifier).equals(cmd) || ("/worldedit:" + identifier).equals(cmd)) {
                return true;
            }
        }
        return false;
    }
    
    public String reduceCmd(String cmd, boolean single) {
        if (cmd.startsWith("/worldedit:/")) {
            return cmd.substring(12);
        }
        if (cmd.startsWith("/worldedit:")) {
            return cmd.substring(11);
        }
        if (cmd.startsWith("//")) {
            return cmd.substring(2);
        }
        if (single && cmd.startsWith("/")) {
            return cmd.substring(1);
        }
        return cmd;
    }
    
    public int getInt(String s) {
        try {
            int max = 0;
            String[] split = s.split(",");
            for (String rad : split) {
                int val = Integer.parseInt(rad);
                if (val > max) {
                    max = val;
                }
            }
            return max;
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public boolean checkVolume(PlotPlayer player, long volume, long max, Cancellable e) {
        if (volume > max) {
            MainUtil.sendMessage(player, C.WORLDEDIT_VOLUME.s().replaceAll("%current%", volume + "").replaceAll("%max%", max + ""));
            e.setCancelled(true);
        }
        if (Permissions.hasPermission(player, "plots.worldedit.bypass")) {
            MainUtil.sendMessage(player, C.WORLDEDIT_BYPASS);
        }
        return true;
    }
    
    public boolean checkSelection(Player p, PlotPlayer pp, int modifier, long max, Cancellable e) {
        final Selection selection = BukkitMain.worldEdit.getSelection(p);
        if (selection == null) {
            return true;
        }
        final BlockVector pos1 = selection.getNativeMinimumPoint().toBlockVector();
        final BlockVector pos2 = selection.getNativeMaximumPoint().toBlockVector();
        HashSet<RegionWrapper> mask = WEManager.getMask(pp);
        RegionWrapper region = new RegionWrapper(pos1.getBlockX(), pos2.getBlockX(), pos1.getBlockZ(), pos2.getBlockZ());
        if (Settings.REQUIRE_SELECTION) {
            String arg = null;
            if (!WEManager.regionContains(region, mask)) {
                arg = "pos1 + pos2";
            }
            else if (!WEManager.maskContains(mask, pos1.getBlockX(), pos1.getBlockZ())) {
                arg = "pos1";
            }
            else if (!WEManager.maskContains(mask, pos2.getBlockX(), pos2.getBlockZ())) {
                arg = "pos2";
            }
            if (arg != null) {
                e.setCancelled(true);
                MainUtil.sendMessage(pp, C.REQUIRE_SELECTION_IN_MASK, arg);
                if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                    MainUtil.sendMessage(pp, C.WORLDEDIT_BYPASS);
                }
                return true;
            }
            if (!WEManager.regionContains(region, mask)) {
                MainUtil.sendMessage(pp, C.REQUIRE_SELECTION_IN_MASK, "pos1 + pos2");
                e.setCancelled(true);
                if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                    MainUtil.sendMessage(pp, C.WORLDEDIT_BYPASS);
                }
                return true;
            }
        }
        long volume = Math.abs((pos1.getBlockX() - pos2.getBlockX()) * (pos1.getBlockY() - pos2.getBlockY()) * (pos1.getBlockZ() - pos2.getBlockZ())) * modifier;
        return checkVolume(pp, volume, max, e);
    }
    
    private boolean set = false;
    
    public boolean delay(final Player player, final String command, boolean delayed) {
        if (!Settings.EXPERIMENTAL_FAST_ASYNC_WORLDEDIT || set) {
            return false;
        }
        boolean free = SetBlockQueue.addNotify(null);
        if (free) {
            if (delayed) {
                MainUtil.sendMessage(BukkitUtil.getPlayer(player), C.WORLDEDIT_RUN, command);
                Bukkit.getServer().dispatchCommand(player, command.substring(1));
            }
            else {
                return false;
            }
        }
        else {
            if (!delayed) {
                MainUtil.sendMessage(BukkitUtil.getPlayer(player), C.WORLDEDIT_DELAYED);
            }
            SetBlockQueue.addNotify(new Runnable() {
                @Override
                public void run() {
                    delay(player, command, true);
                }
            });
        }
        return true;
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public boolean onPlayerCommand(final PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        if (!PS.get().isPlotWorld(p.getWorld().getName())) {
            return true;
        }
        String message = e.getMessage();
        String cmd = message.toLowerCase();
        boolean single = true;
        String[] split = cmd.split(" ");
        
        long maxVolume = Settings.WE_MAX_VOLUME;
        long maxIterations = Settings.WE_MAX_ITERATIONS;
        if (pp.getAttribute("worldedit")) {
            return true;
        }
        if (split.length >= 2) {
            String reduced = reduceCmd(split[0], single);
            String reduced2 = reduceCmd(split[0] + " " + split[1], single);
            if (rad1.contains(reduced)) {
                if (delay(p, message, false)) { e.setCancelled(true); return true; }
                long volume = getInt(split[1]) * 256;
                return checkVolume(pp, volume, maxVolume, e);
            }
            if (rad2.contains(reduced)) {
                if (delay(p, message, false)) { e.setCancelled(true); return true; }
                if (split.length >= 3) {
                    long volume = getInt(split[2]) * 256;
                    return checkVolume(pp, volume, maxVolume, e);
                }
                return true;
            }
            if (rad2_1.contains(reduced)) {
                if (delay(p, message, false)) { e.setCancelled(true); return true; }
                if (split.length >= 4) {
                    long volume = getInt(split[2]) * getInt(split[3]);
                    return checkVolume(pp, volume, maxVolume, e);
                }
                return true;
            }
            if (rad2_2.contains(reduced)) {
                if (delay(p, message, false)) { e.setCancelled(true); return true; }
                if (split.length >= 3) {
                    long radius = getInt(split[2]);
                    long volume = radius * radius;
                    return checkVolume(pp, volume, maxVolume, e);
                }
                return true;
            }
            if (rad2_3.contains(reduced2)) {
                if (delay(p, message, false)) { e.setCancelled(true); return true; }
                if (split.length >= 3) {
                    if (split.length == 4) {
                        int iterations = getInt(split[3]);
                        if (iterations > maxIterations) {
                            MainUtil.sendMessage(pp, C.WORLDEDIT_ITERATIONS.s().replaceAll("%current%", iterations + "").replaceAll("%max%", maxIterations + ""));
                            e.setCancelled(true);
                            if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                                MainUtil.sendMessage(pp, C.WORLDEDIT_BYPASS);
                            }
                            return true;
                        }
                    }
                    long radius = getInt(split[2]);
                    long volume = radius * radius;
                    return checkVolume(pp, volume, maxVolume, e);
                }
                return true;
            }
            if (rad3_1.contains(reduced2)) {
                if (delay(p, message, false)) { e.setCancelled(true); return true; }
                if (split.length >= 3) {
                    int i = 2;
                    if (split[i].equalsIgnoreCase("-h")) {
                        i = 3;
                    }
                    long radius = getInt(split[i]);
                    long volume = radius * radius;
                    return checkVolume(pp, volume, maxVolume, e);
                }
                return true;
            }
            if (rad3_2.contains(reduced2)) {
                if (delay(p, message, false)) { e.setCancelled(true); return true; }
                if (split.length >= 4) {
                    int i = 3;
                    if (split[i].equalsIgnoreCase("-h")) {
                        i = 4;
                    }
                    long radius = getInt(split[i]);
                    long volume = radius * radius;
                    return checkVolume(pp, volume, maxVolume, e);
                }
                return true;
            }
            if (regionExtend.contains(reduced)) {
                if (delay(p, message, false)) { e.setCancelled(true); return true; }
                return checkSelection(p, pp, getInt(split[1]), maxVolume, e);
            }
        }
        String reduced = reduceCmd(split[0], single);
        if (Settings.WE_BLACKLIST.contains(reduced)) {
            MainUtil.sendMessage(pp, C.WORLDEDIT_UNSAFE);
            e.setCancelled(true);
            if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                MainUtil.sendMessage(pp, C.WORLDEDIT_BYPASS);
            }
        }
        if (restricted.contains(reduced)) {
            Plot plot = MainUtil.getPlot(pp.getLocation());
            if (plot != null && plot.isAdded(pp.getUUID())) {
                if (delay(p, message, false)) { e.setCancelled(true); return true; }
                return true;
            }
            e.setCancelled(true);
            MainUtil.sendMessage(pp, C.NO_PLOT_PERMS);
            if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                MainUtil.sendMessage(pp, C.WORLDEDIT_BYPASS);
            }
            return true;
        }
        if (region.contains(reduced)) {
            if (delay(p, message, false)) { e.setCancelled(true); return true; }
            return checkSelection(p, pp, 1, maxVolume, e);
        }
        if (other.contains(reduced)) {
            if (delay(p, message, false)) { e.setCancelled(true); return true; }
        }
        return true;
    }
}
 
