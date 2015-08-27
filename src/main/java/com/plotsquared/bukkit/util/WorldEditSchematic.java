package com.plotsquared.bukkit.util;

import java.io.File;

import org.bukkit.Bukkit;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.bukkit.BukkitMain;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;

public class WorldEditSchematic {
    public void saveSchematic(String file, final String world, final PlotId id) {
        Location bot = MainUtil.getPlotBottomLoc(world, id).add(1, 0, 1);
        Location top = MainUtil.getPlotTopLoc(world, id);
        Vector size = new Vector(top.getX() - bot.getX() + 1, top.getY() - bot.getY() - 1, top.getZ() - bot.getZ() + 1);
        Vector origin = new Vector(bot.getX(), bot.getY(), bot.getZ());
        CuboidClipboard clipboard = new CuboidClipboard(size, origin);
        Vector pos1 = new Vector(bot.getX(), bot.getY(), bot.getZ());
        Vector pos2 = new Vector(top.getX(), top.getY(), top.getZ());
        EditSession session = BukkitMain.worldEdit.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(Bukkit.getWorld(world)), 999999999);
        clipboard.copy(session);
        try {
            clipboard.saveSchematic(new File(file));
            MainUtil.sendMessage(null, "&7 - &a  success: " + id);
        } catch (Exception e) {
            e.printStackTrace();
            MainUtil.sendMessage(null, "&7 - Failed to save &c" + id);
        }
    }
}
