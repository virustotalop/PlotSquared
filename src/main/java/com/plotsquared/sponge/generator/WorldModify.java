package com.plotsquared.sponge.generator;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.ClusterManager;

public class WorldModify implements WorldGeneratorModifier {

    private SpongePlotGenerator plotgen;
    private boolean augment;

    public WorldModify(SpongePlotGenerator plotgen, boolean augment) {
        this.plotgen = plotgen;
        this.augment = augment;
    }

    @Override
    public void modifyWorldGenerator(WorldCreationSettings world, DataContainer settings, WorldGenerator gen) {
        if (augment) {
            String worldname = plotgen.world;
            PlotWorld plotworld = plotgen.getNewPlotWorld(worldname);
            if (plotworld.TYPE == 2) {
                for (PlotCluster cluster : ClusterManager.getClusters(worldname)) {
                    new AugmentedPopulator(worldname, gen, plotgen, cluster, plotworld.TERRAIN == 2, plotworld.TERRAIN != 2);
                }
            }
            else {
                new AugmentedPopulator(worldname, gen, plotgen, null, plotworld.TERRAIN == 2, plotworld.TERRAIN != 2);
            }
        }
        else {
            gen.getGeneratorPopulators().clear();
            gen.getPopulators().clear();
            gen.setBaseGeneratorPopulator(plotgen.getBaseGeneratorPopulator());
            gen.setBiomeGenerator(plotgen.getBiomeGenerator());
        }
    }
    
    @Override
    public String getName() {
        return "PlotSquared";
    }

    @Override
    public String getId() {
        return "PlotSquared";
    }
}
