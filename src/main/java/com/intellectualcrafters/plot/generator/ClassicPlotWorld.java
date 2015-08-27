package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.StringMan;

public abstract class ClassicPlotWorld extends SquarePlotWorld {
    public static int ROAD_HEIGHT_DEFAULT = 64;
    public static int PLOT_HEIGHT_DEFAULT = 64;
    public static int WALL_HEIGHT_DEFAULT = 64;
    public static PlotBlock[] MAIN_BLOCK_DEFAULT = new PlotBlock[] { new PlotBlock((short) 1, (byte) 0) };
    public static PlotBlock[] TOP_BLOCK_DEFAULT = new PlotBlock[] { new PlotBlock((short) 2, (byte) 0) };
    public static PlotBlock WALL_BLOCK_DEFAULT = new PlotBlock((short) 44, (byte) 0);
    public final static PlotBlock CLAIMED_WALL_BLOCK_DEFAULT = new PlotBlock((short) 44, (byte) 1);
    public static PlotBlock WALL_FILLING_DEFAULT = new PlotBlock((short) 1, (byte) 0);
    public final static PlotBlock ROAD_BLOCK_DEFAULT = new PlotBlock((short) 155, (byte) 0);
    public int ROAD_HEIGHT;
    public int PLOT_HEIGHT;
    public int WALL_HEIGHT;
    public PlotBlock[] MAIN_BLOCK;
    public PlotBlock[] TOP_BLOCK;
    public PlotBlock WALL_BLOCK;
    public PlotBlock CLAIMED_WALL_BLOCK;
    public PlotBlock WALL_FILLING;
    public PlotBlock ROAD_BLOCK;
    public boolean PLOT_BEDROCK;

    /**
     * CONFIG NODE | DEFAULT VALUE | DESCRIPTION | CONFIGURATION TYPE | REQUIRED FOR INITIAL SETUP
     * 
     * Set the last boolean to false if you do not require a specific config node to be set while using the setup
     * command - this may be useful if a config value can be changed at a later date, and has no impact on the actual
     * world generation
     */
    @Override
    public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[] { new ConfigurationNode("plot.height", ClassicPlotWorld.PLOT_HEIGHT_DEFAULT, "Plot height", Configuration.INTEGER, true), new ConfigurationNode("plot.size", SquarePlotWorld.PLOT_WIDTH_DEFAULT, "Plot width", Configuration.INTEGER, true), new ConfigurationNode("plot.filling", ClassicPlotWorld.MAIN_BLOCK_DEFAULT, "Plot block", Configuration.BLOCKLIST, true), new ConfigurationNode("plot.floor", ClassicPlotWorld.TOP_BLOCK_DEFAULT, "Plot floor block", Configuration.BLOCKLIST, true), new ConfigurationNode("wall.block", ClassicPlotWorld.WALL_BLOCK_DEFAULT, "Top wall block", Configuration.BLOCK, true), new ConfigurationNode("wall.block_claimed", ClassicPlotWorld.CLAIMED_WALL_BLOCK_DEFAULT, "Wall block (claimed)", Configuration.BLOCK, true), new ConfigurationNode("road.width", SquarePlotWorld.ROAD_WIDTH_DEFAULT, "Road width", Configuration.INTEGER, true),
                new ConfigurationNode("road.height", ClassicPlotWorld.ROAD_HEIGHT_DEFAULT, "Road height", Configuration.INTEGER, true), new ConfigurationNode("road.block", ClassicPlotWorld.ROAD_BLOCK_DEFAULT, "Road block", Configuration.BLOCK, true), new ConfigurationNode("wall.filling", ClassicPlotWorld.WALL_FILLING_DEFAULT, "Wall filling block", Configuration.BLOCK, true), new ConfigurationNode("wall.height", ClassicPlotWorld.WALL_HEIGHT_DEFAULT, "Wall height", Configuration.INTEGER, true), new ConfigurationNode("plot.bedrock", true, "Plot bedrock generation", Configuration.BOOLEAN, true) };
    }

    /**
     * This method is called when a world loads. Make sure you set all your constants here. You are provided with the
     * configuration section for that specific world.
     */
    @Override
    public void loadConfiguration(final ConfigurationSection config) {
        super.loadConfiguration(config);
        this.PLOT_BEDROCK = config.getBoolean("plot.bedrock");
        this.PLOT_HEIGHT = Math.min(255, config.getInt("plot.height"));
        this.MAIN_BLOCK = (PlotBlock[]) Configuration.BLOCKLIST.parseString(StringMan.join(config.getStringList("plot.filling"), ','));
        this.TOP_BLOCK = (PlotBlock[]) Configuration.BLOCKLIST.parseString(StringMan.join(config.getStringList("plot.floor"), ','));
        this.WALL_BLOCK = (PlotBlock) Configuration.BLOCK.parseString(config.getString("wall.block"));
        this.ROAD_HEIGHT = Math.min(255, config.getInt("road.height"));
        this.ROAD_BLOCK = (PlotBlock) Configuration.BLOCK.parseString(config.getString("road.block"));
        this.WALL_FILLING = (PlotBlock) Configuration.BLOCK.parseString(config.getString("wall.filling"));
        this.WALL_HEIGHT = Math.min(254, config.getInt("wall.height"));
        this.CLAIMED_WALL_BLOCK = (PlotBlock) Configuration.BLOCK.parseString(config.getString("wall.block_claimed"));
    }

    public ClassicPlotWorld(final String worldname) {
        super(worldname);
    }
}
