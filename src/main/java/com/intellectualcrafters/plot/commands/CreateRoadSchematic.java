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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "createroadschematic",
        aliases = {"crs"},
        category = CommandCategory.DEBUG,
        requiredType = RequiredType.NONE,
        permission = "plots.createroadschematic",
        description = "Add a road schematic to your world using the roads around your current plot",
        usage = "/plot createroadschematic"
)
public class CreateRoadSchematic extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer player, final String ... args) {
        final Location loc = player.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!(PS.get().getPlotWorld(loc.getWorld()) instanceof HybridPlotWorld)) {
            return sendMessage(player, C.NOT_IN_PLOT_WORLD);
        }
        HybridUtils.manager.setupRoadSchematic(plot);
        MainUtil.sendMessage(player, "&6Saved new road schematic (see console for more information)");
        return true;
    }
}
