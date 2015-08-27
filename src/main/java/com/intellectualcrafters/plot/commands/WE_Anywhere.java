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
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.bukkit.BukkitMain;
import com.plotsquared.bukkit.listeners.worldedit.WEManager;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "weanywhere",
        permission = "plots.worldedit.bypass",
        description = "Force bypass of WorldEdit",
        aliases = {"wea"},
        usage = "/plot weanywhere",
        requiredType = RequiredType.NONE,
        category = CommandCategory.DEBUG
)

@Deprecated
public class WE_Anywhere extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] arguments) {
        return MainCommand.onCommand(player, "plot", new String[] {"toggle", "worldedit"});
    }

}
