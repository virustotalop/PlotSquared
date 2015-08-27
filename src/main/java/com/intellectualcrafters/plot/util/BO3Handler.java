package com.intellectualcrafters.plot.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.ClassicPlotWorld;
import com.intellectualcrafters.plot.object.BO3;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.SchematicHandler.Dimension;

public class BO3Handler {
    
    /**
     * @see #saveBO3(null, Plot)
     * @param plot
     * @return if successfully exported 
     */
    public static boolean saveBO3(Plot plot) {
        return saveBO3(null, plot);
    }
    
    /**
     * Save a plot as a BO3 file<br>
     *  - Use null for the player object if no player is applicable
     * @param plr
     * @param plot
     * @return
     */
    public static boolean saveBO3(PlotPlayer plr, Plot plot) {
        PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
        if (!(plotworld instanceof ClassicPlotWorld) || plotworld.TYPE != 0) {
            MainUtil.sendMessage(plr, "BO3 exporting only supports type 0 classic generation.");
            return false;
        }
        String alias = plot.toString();
        Location pos1 = plot.getBottom().add(1, 0, 1);
        Location pos2 = plot.getTop();
        ClassicPlotWorld cpw = (ClassicPlotWorld) plotworld;
        int height = cpw.PLOT_HEIGHT;
        
        int cx = (pos1.getX() + pos2.getX()) / 2;
        int cz = (pos1.getZ() + pos2.getZ()) / 2;
        
        HashMap<ChunkLoc, BO3> map = new HashMap<>();
        
        boolean content = false;
        for (int x = pos1.getX(); x <= pos2.getX(); x++) {
            int X = ((x + 7) - cx) >> 4;
            int xx = (x - cx) % 16;
            for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                int Z = ((z + 7) - cz) >> 4;
                int zz = (z - cz) % 16;
                ChunkLoc loc = new ChunkLoc(X, Z);
                BO3 bo3 = map.get(loc);
                for (int y = height + 1; y < 256; y++) {
                    PlotBlock block = BlockManager.manager.getBlock(new Location(plot.world, x, y, z));
                    if (block != null && block.id != 0) {
                        if (bo3 == null) {
                            bo3 = new BO3(alias, loc);
                            map.put(loc, bo3);
                            content = true;
                        }
                        bo3.addBlock(xx, y - height - 1, zz, block);
                    }
                }
            }
        }
        
        if (!content) {
            MainUtil.sendMessage(plr, "No content found!");
            return false;
        }
        
        for (Entry<ChunkLoc, BO3> entry : map.entrySet()) {
            ChunkLoc chunk = entry.getKey();
            BO3 bo3 = entry.getValue();
            if (chunk.x == 0 && chunk.z == 0) {
                continue;
            }
            int x = chunk.x;
            int z = chunk.z;
            if (Math.abs(chunk.x) > Math.abs(chunk.z)) {
                x += chunk.x > 0 ? -1 : 1;
            }
            else {
                z += chunk.z > 0 ? -1 : 1;
            }
            ChunkLoc parentLoc = new ChunkLoc(x, z);
            if (!map.containsKey(parentLoc)) {
                parentLoc = null;
                for (Entry<ChunkLoc, BO3> entry2 : map.entrySet()) {
                    ChunkLoc other = entry2.getKey();
                    if ((other.x == chunk.x - 1 && other.z == chunk.z) || (other.z == chunk.z - 1 && other.x == chunk.x)) {
                        parentLoc = other;
                    }
                }
                if (parentLoc == null) {
                    MainUtil.sendMessage(plr, "Exporting BO3 cancelled due to detached chunk: " + chunk + " - Make sure you only have one object per plot");
                    return false;
                }
            }
            map.get(parentLoc).addChild(bo3);
        }
        
        for (Entry<ChunkLoc, BO3> entry : map.entrySet()) {
            save(plot, entry.getValue());
        }
        
        MainUtil.sendMessage(plr, "BO3 exporting was successful!");
        return true;
    }
    
    public static boolean save(Plot plot, BO3 bo3) {
        File base = getBaseFile(plot.world);
        try {
            List<String> lines = Files.readAllLines(base.toPath(), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                String result = StringMan.replaceAll(line, "%owner%", MainUtil.getName(plot.owner), "%alias%", plot.toString(), "%blocks%", bo3.getBlocks(), "%branches%", bo3.getChildren(), "%flags%", StringMan.join(FlagManager.getPlotFlags(plot).values(), ","));
                if (!StringMan.isEqual(result, line)) {
                    lines.set(i, result);
                }
            }
            File bo3File;
            if (bo3.getLoc().x == 0 && bo3.getLoc().z == 0) {
                bo3File = new File(base.getParentFile(), bo3.getName() + ".bo3");
            }
            else {
                bo3File = new File(base.getParentFile(), bo3.getName() + "_" + bo3.getLoc().x + "_" + bo3.getLoc().z + ".bo3");
            }
            bo3File.createNewFile();
            Files.write(bo3File.toPath(), StringMan.join(lines, System.getProperty("line.separator")).getBytes(), StandardOpenOption.WRITE);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static File getBaseFile(String category) {
        File base = new File(PS.get().IMP.getDirectory(), Settings.BO3_SAVE_PATH + File.separator + category + File.separator + "base.yml");
        if (!base.exists()) {
            PS.get().copyFile("base.yml", Settings.BO3_SAVE_PATH + File.separator + category);
        }
        return base;
    }
}
