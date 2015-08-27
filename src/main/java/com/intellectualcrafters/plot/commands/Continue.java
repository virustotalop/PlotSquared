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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "continue",
        description = "Continue a plot that was previously marked as done",
        permission = "plots.continue",
        category = CommandCategory.ACTIONS,
        requiredType = RequiredType.NONE
)
public class Continue extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null || !plot.hasOwner()) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if ((!plot.isOwner(plr.getUUID())) && !Permissions.hasPermission(plr, "plots.admin.command.continue")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (!plot.getSettings().flags.containsKey("done")) {
            MainUtil.sendMessage(plr, C.DONE_NOT_DONE);
            return false;
        }
        if (FlagManager.isPlotFlagTrue(plot, "done" ) && (!Permissions.hasPermission(plr, "plots.continue") || (Settings.DONE_COUNTS_TOWARDS_LIMIT && MainUtil.getAllowedPlots(plr) >= MainUtil.getPlayerPlotCount(plr)))) {
            MainUtil.sendMessage(plr, C.DONE_ALREADY_DONE);
            return false;
        }
        if (MainUtil.runners.containsKey(plot)) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        FlagManager.removePlotFlag(plot, "done");
        MainUtil.sendMessage(plr, C.DONE_REMOVED);
        return true;
    }
}
