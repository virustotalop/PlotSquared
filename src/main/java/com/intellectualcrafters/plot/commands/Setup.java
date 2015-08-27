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
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "setup",
        permission = "plots.admin.command.setup",
        description = "Setup wizard for plot worlds",
        usage = "/plot setup",
        aliases = {"create"},
        category = CommandCategory.ACTIONS
)
public class Setup extends SubCommand {

    public void displayGenerators(PlotPlayer plr) {
        StringBuffer message = new StringBuffer();
        message.append("&6What generator do you want?");
        for (Entry<String, PlotGenerator<?>> entry : SetupUtils.generators.entrySet()) {
            if (entry.getKey().equals("PlotSquared")) {
                message.append("\n&8 - &2" + entry.getKey() + " (Default Generator)");
            }
            else if (entry.getValue().isFull()) {
                message.append("\n&8 - &7" + entry.getKey() + " (Plot Generator)");
            }
            else {
                message.append("\n&8 - &7" + entry.getKey() + " (Unknown structure)");
            }
        }
        MainUtil.sendMessage(plr, message.toString());
    }

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        // going through setup
        String name = plr.getName();
        if (!SetupUtils.setupMap.containsKey(name)) {
            final SetupObject object = new SetupObject();
            SetupUtils.setupMap.put(name, object);
            SetupUtils.manager.updateGenerators();
            sendMessage(plr, C.SETUP_INIT);
            displayGenerators(plr);
            return false;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("cancel")) {
                SetupUtils.setupMap.remove(name);
                MainUtil.sendMessage(plr, "&aCancelled setup");
                return false;
            }
            if (args[0].equalsIgnoreCase("back")) {
                final SetupObject object = SetupUtils.setupMap.get(name);
                if (object.setup_index > 0) {
                    object.setup_index--;
                    final ConfigurationNode node = object.step[object.setup_index];
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", node.getDescription(), node.getType().getType(), node.getDefaultValue() + "");
                    return false;
                } else if (object.current > 0) {
                    object.current--;
                }
            }
        }
        final SetupObject object = SetupUtils.setupMap.get(name);
        final int index = object.current;
        switch (index) {
            case 0: { // choose generator
                if ((args.length != 1) || !SetupUtils.generators.containsKey(args[0])) {
                    final String prefix = "\n&8 - &7";
                    MainUtil.sendMessage(plr, "&cYou must choose a generator!" + prefix + StringMan.join(SetupUtils.generators.keySet(), prefix).replaceAll("PlotSquared", "&2PlotSquared"));
                    sendMessage(plr, C.SETUP_INIT);
                    return false;
                }
                object.setupGenerator = args[0];
                object.current++;
                final String partial = Settings.ENABLE_CLUSTERS ? "\n&8 - &7PARTIAL&8 - &7Vanilla with clusters of plots" : "";
                MainUtil.sendMessage(plr, "&6What world type do you want?" + "\n&8 - &2DEFAULT&8 - &7Standard plot generation" + "\n&8 - &7AUGMENTED&8 - &7Plot generation with terrain" + partial);
                break;
            }
            case 1: { // choose world type
                List<String> allTypes = Arrays.asList(new String[] { "default", "augmented", "partial"});
                List<String> allDesc = Arrays.asList(new String[] { "Standard plot generation", "Plot generation with vanilla terrain", "Vanilla with clusters of plots"});
                ArrayList<String> types = new ArrayList<>();
                if (SetupUtils.generators.get(object.setupGenerator).isFull()) {
                    types.add("default");
                }
                types.add("augmented");
                if (Settings.ENABLE_CLUSTERS) {
                    types.add("partial");
                }
                if ((args.length != 1) || !types.contains(args[0].toLowerCase())) {
                    MainUtil.sendMessage(plr, "&cYou must choose a world type!");
                    for (String type : types) {
                        int i = allTypes.indexOf(type);
                        if (type.equals("default")) {
                            MainUtil.sendMessage(plr, "&8 - &2" + type + " &8-&7 " + allDesc.get(i));
                        }
                        else {
                            MainUtil.sendMessage(plr, "&8 - &7" + type + " &8-&7 " + allDesc.get(i));
                        }
                    }
                    return false;
                }
                object.type = allTypes.indexOf(args[0].toLowerCase());
                PlotGenerator<?> gen = SetupUtils.generators.get(object.setupGenerator);
                if (object.type == 0) {
                    object.current++;
                    if (object.step == null) {
                        object.plotManager = object.setupGenerator;
                        object.step = SetupUtils.generators.get(object.plotManager).getNewPlotWorld(null).getSettingNodes();
                        SetupUtils.generators.get(object.plotManager).processSetup(object);
                    }
                    if (object.step.length == 0) {
                        object.current = 4;
                        MainUtil.sendMessage(plr, "&6What do you want your world to be called?");
                        object.setup_index = 0;
                        return true;
                    }
                    final ConfigurationNode step = object.step[object.setup_index];
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                } else {
                    if (gen.isFull()) {
                        object.plotManager = object.setupGenerator;
                        object.setupGenerator = null;
                        object.step = SetupUtils.generators.get(object.plotManager).getNewPlotWorld(null).getSettingNodes();
                        SetupUtils.generators.get(object.plotManager).processSetup(object);
                    }
                    else {
                        object.plotManager = "PlotSquared";
                        MainUtil.sendMessage(plr, "&c[WARNING] The specified generator does not identify as BukkitPlotGenerator");
                        MainUtil.sendMessage(plr, "&7 - You may need to manually configure the other plugin");
                        object.step = SetupUtils.generators.get(object.plotManager).getNewPlotWorld(null).getSettingNodes();
                    }
                    MainUtil.sendMessage(plr, "&6What terrain would you like in plots?" + "\n&8 - &2NONE&8 - &7No terrain at all" + "\n&8 - &7ORE&8 - &7Just some ore veins and trees" + "\n&8 - &7ROAD&8 - &7Terrain seperated by roads" + "\n&8 - &7ALL&8 - &7Entirely vanilla generation");
                }
                object.current++;
                break;
            }
            case 2: { // Choose terrain
                final List<String> terrain = Arrays.asList(new String[] { "none", "ore", "road", "all" });
                if ((args.length != 1) || !terrain.contains(args[0].toLowerCase())) {
                    MainUtil.sendMessage(plr, "&cYou must choose the terrain!" + "\n&8 - &2NONE&8 - &7No terrain at all" + "\n&8 - &7ORE&8 - &7Just some ore veins and trees" + "\n&8 - &7ROAD&8 - &7Terrain seperated by roads" + "\n&8 - &7ALL&8 - &7Entirely vanilla generation");
                    return false;
                }
                object.terrain = terrain.indexOf(args[0].toLowerCase());
                object.current++;
                if (object.step == null) {
                    object.step = SetupUtils.generators.get(object.plotManager).getNewPlotWorld(null).getSettingNodes();
                }
                final ConfigurationNode step = object.step[object.setup_index];
                sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                break;
            }
            case 3: { // world setup
                if (object.setup_index == object.step.length) {
                    MainUtil.sendMessage(plr, "&6What do you want your world to be called?");
                    object.setup_index = 0;
                    object.current++;
                    return true;
                }
                ConfigurationNode step = object.step[object.setup_index];
                if (args.length < 1) {
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                    return false;
                }
                final boolean valid = step.isValid(args[0]);
                if (valid) {
                    sendMessage(plr, C.SETUP_VALID_ARG, step.getConstant(), args[0]);
                    step.setValue(args[0]);
                    object.setup_index++;
                    if (object.setup_index == object.step.length) {
                        onCommand(plr, args);
                        return false;
                    }
                    step = object.step[object.setup_index];
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                    return false;
                } else {
                    sendMessage(plr, C.SETUP_INVALID_ARG, args[0], step.getConstant());
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                    return false;
                }
            }
            case 4: {
                if (args.length != 1) {
                    MainUtil.sendMessage(plr, "&cYou need to choose a world name!");
                    return false;
                }
                if (BlockManager.manager.isWorld(args[0])) {
                    if (PS.get().isPlotWorld(args[0])) {
                        MainUtil.sendMessage(plr, "&cThat world name is already taken!");
                        return false;
                    }
                    MainUtil.sendMessage(plr, "&cThe world you specified already exists. After restarting, new terrain will use PlotSquared, however you may need to reset the world for it to generate correctly!");
                }
                object.world = args[0];
                SetupUtils.setupMap.remove(name);
                final String world;
                if (object.setupManager == null) {
                    world = SetupUtils.manager.setupWorld(object);
                }
                else {
                    world = object.setupManager.setupWorld(object);
                }
                try {
                    plr.teleport(BlockManager.manager.getSpawn(world));
                } catch (final Exception e) {
                    plr.sendMessage("&cAn error occured. See console for more information");
                    e.printStackTrace();
                }
                sendMessage(plr, C.SETUP_FINISHED, object.world);
                SetupUtils.setupMap.remove(name);
            }
        }
        return false;
    }
}
