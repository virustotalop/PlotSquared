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
package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.Bukkit;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotItemStack;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.events.PlotRateEvent;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "rate",
        permission = "plots.rate",
        description = "Rate the plot",
        usage = "/plot rate [#|next]",
        aliases = {"rt"},
        category = CommandCategory.ACTIONS,
        requiredType = RequiredType.NONE
)
public class Rate extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer player, final String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("next")) {
                ArrayList<Plot> plots = new ArrayList<>(PS.get().getPlots());
                Collections.sort(plots, new Comparator<Plot>() {
                    @Override
                    public int compare(Plot p1, Plot p2) {
                        double v1 = 0;
                        double v2 = 0;
                        if (p1.getSettings().ratings != null) {
                            for (Entry<UUID, Rating> entry : p1.getRatings().entrySet()) {
                                v1 -= 11 - entry.getValue().getAverageRating();
                            }
                        }
                        if (p2.getSettings().ratings != null) {
                            for (Entry<UUID, Rating> entry : p2.getRatings().entrySet()) {
                                v2 -= 11 - entry.getValue().getAverageRating();
                            }
                        }
                        return v2 > v1 ? 1 : -1;
                    }
                });
                UUID uuid = player.getUUID();
                for (Plot p : plots) {
                    if ((!Settings.REQUIRE_DONE || p.getSettings().flags.containsKey("done")) && p.isBasePlot() && (p.getSettings().ratings == null || !p.getSettings().ratings.containsKey(uuid)) && !p.isAdded(uuid)) {
                        MainUtil.teleportPlayer(player, player.getLocation(), p);
                        MainUtil.sendMessage(player, C.RATE_THIS);
                        return true;
                    }
                }
                MainUtil.sendMessage(player, C.FOUND_NO_PLOTS);
                return false;
            }
        }
        final Location loc = player.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            sendMessage(player, C.RATING_NOT_OWNED);
            return false;
        }
        if (plot.isOwner(player.getUUID())) {
            sendMessage(player, C.RATING_NOT_YOUR_OWN);
            return false;
        }
        if (Settings.REQUIRE_DONE && !plot.getSettings().flags.containsKey("done")) {
            sendMessage(player, C.RATING_NOT_DONE);
            return false;
        }
        if (Settings.RATING_CATEGORIES != null && Settings.RATING_CATEGORIES.size() != 0) {
            final Runnable run = new Runnable() {
                @Override
                public void run() {
                    if (plot.getSettings().ratings.containsKey(player.getUUID())) {
                        sendMessage(player, C.RATING_ALREADY_EXISTS, plot.getId().toString());
                        return;
                    }
                    final MutableInt index = new MutableInt(0);
                    final MutableInt rating = new MutableInt(0);
                    String title = Settings.RATING_CATEGORIES.get(0);
                    PlotInventory inventory = new PlotInventory(player, 1, title) {
                        public boolean onClick(int i) {
                            rating.add((i + 1) * Math.pow(10, index.intValue()));
                            index.increment();
                            if (index.intValue() >= Settings.RATING_CATEGORIES.size()) {
                                close();
                                int rV = rating.intValue();
                                Rating result = EventUtil.manager.callRating(player, plot, new Rating(rV));
                                plot.getSettings().ratings.put(player.getUUID(), result.getAggregate());
                                DBFunc.setRating(plot, player.getUUID(), rV);
                                sendMessage(player, C.RATING_APPLIED, plot.getId().toString());
                                sendMessage(player, C.RATING_APPLIED, plot.getId().toString());
                                return false;
                            }
                            setTitle(Settings.RATING_CATEGORIES.get(index.intValue()));
                            if (Permissions.hasPermission(player, "plots.comment")) {
                                Command<PlotPlayer> command = MainCommand.getInstance().getCommand("comment");
                                if (command != null) {
                                    MainUtil.sendMessage(player, C.COMMENT_THIS, command.getUsage().replaceAll("{label}", "plot"));
                                }
                            }
                            return false;
                        }
                    };
                    inventory.setItem(0, new PlotItemStack(35, (short) 12, 0, "0/8"));
                    inventory.setItem(1, new PlotItemStack(35, (short) 14, 1, "1/8"));
                    inventory.setItem(2, new PlotItemStack(35, (short) 1, 2, "2/8"));
                    inventory.setItem(3, new PlotItemStack(35, (short) 4, 3, "3/8"));
                    inventory.setItem(4, new PlotItemStack(35, (short) 5, 4, "4/8"));
                    inventory.setItem(5, new PlotItemStack(35, (short) 9, 5, "5/8"));
                    inventory.setItem(6, new PlotItemStack(35, (short) 11, 6, "6/8"));
                    inventory.setItem(7, new PlotItemStack(35, (short) 10, 7, "7/8"));
                    inventory.setItem(8, new PlotItemStack(35, (short) 2, 8, "8/8"));
                    inventory.openInventory();
                }
            };
            if (plot.getSettings().ratings == null) {
                if (!Settings.CACHE_RATINGS) {
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            plot.getSettings().ratings = DBFunc.getRatings(plot);
                            run.run();
                        }
                    });
                    return true;
                }
                plot.getSettings().ratings = new HashMap<UUID, Integer>();
            }
            run.run();
            return true;
        }
        if (args.length < 1) {
            sendMessage(player, C.RATING_NOT_VALID);
            return true;
        }
        final String arg = args[0];
        final int rating;
        if (MathMan.isInteger(arg) && arg.length() < 3 && arg.length() > 0) {
            rating = Integer.parseInt(arg);
            if (rating > 10) {
                sendMessage(player, C.RATING_NOT_VALID);
                return false;
            }
        }
        else {
            sendMessage(player, C.RATING_NOT_VALID);
            return false;
        }
        final UUID uuid = player.getUUID();
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                if (plot.getSettings().ratings.containsKey(uuid)) {
                    sendMessage(player, C.RATING_ALREADY_EXISTS, plot.getId().toString());
                    return;
                }
                Rating result = EventUtil.manager.callRating(player, plot, new Rating(rating));
                int resultVal = result.getAggregate();
                plot.getSettings().ratings.put(uuid, resultVal);
                DBFunc.setRating(plot, uuid, resultVal);
                sendMessage(player, C.RATING_APPLIED, plot.getId().toString());
            }
        };
        if (plot.getSettings().ratings == null) {
            if (!Settings.CACHE_RATINGS) {
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        plot.getSettings().ratings = DBFunc.getRatings(plot);
                        run.run();
                    }
                });
                return true;
            }
            plot.getSettings().ratings = new HashMap<UUID, Integer>();
        }
        run.run();
        return true;
    }
}
