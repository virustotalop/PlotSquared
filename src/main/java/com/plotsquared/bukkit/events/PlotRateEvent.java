package com.plotsquared.bukkit.events;

import org.bukkit.event.HandlerList;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;

/**
 * Created 2015-07-13 for PlotSquaredGit
 *
 * @author Citymonstret
 */
public class PlotRateEvent extends PlotEvent {

    private static HandlerList handlers = new HandlerList();
    private final PlotPlayer rater;
    private Rating rating;

    public PlotRateEvent(final PlotPlayer rater, final Rating rating, final Plot plot) {
        super(plot);
        this.rater = rater;
        this.rating = rating;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlotPlayer getRater() {
        return this.rater;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public Rating getRating() {
        return this.rating;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
