package com.plotsquared.sponge.events;

import java.util.ArrayList;

import org.spongepowered.api.event.AbstractEvent;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.world.World;

import com.intellectualcrafters.plot.object.PlotId;

public class PlotUnlinkEvent extends AbstractEvent implements Cancellable {
    private final ArrayList<PlotId> plots;
    private final World world;
    private boolean cancelled;

    /**
     * Called when a mega-plot is unlinked.
     *
     * @param world World in which the event occurred
     * @param plots Plots that are involved in the event
     */
    public PlotUnlinkEvent(final World world, final ArrayList<PlotId> plots) {
        this.plots = plots;
        this.world = world;
    }

    /**
     * Get the plots involved
     *
     * @return PlotId
     */
    public ArrayList<PlotId> getPlots() {
        return this.plots;
    }

    public World getWorld() {
        return this.world;
    }

    @Override
    public boolean isCancelled() {
       return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
       cancelled = cancel;
    }
 }