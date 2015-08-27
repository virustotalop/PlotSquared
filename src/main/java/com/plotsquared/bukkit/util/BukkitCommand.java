package com.plotsquared.bukkit.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.commands.DebugUUID;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringComparison;
import com.plotsquared.general.commands.Command;

/**
 * Created 2015-02-20 for PlotSquared
 *
 * @author Citymonstret
 */
public class BukkitCommand implements CommandExecutor, TabCompleter {
    
    public BukkitCommand() {
        MainCommand.getInstance().addCommand(new DebugUUID());
    }
    
    @Override
    public boolean onCommand(final CommandSender commandSender, final org.bukkit.command.Command command, final String commandLabel, final String[] args) {
        if (commandSender instanceof Player) {
            return MainCommand.onCommand(BukkitUtil.getPlayer((Player) commandSender), commandLabel, args);
        }
        return MainCommand.onCommand(ConsolePlayer.getConsole(), commandLabel, args);
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, final org.bukkit.command.Command command, final String s, final String[] strings) {
        if (!(commandSender instanceof Player)) {
            return null;
        }
        final PlotPlayer player = BukkitUtil.getPlayer((Player) commandSender);
        if (strings.length < 1) {
            if ((strings.length == 0) || "plots".startsWith(s)) {
                return Collections.singletonList("plots");
            }
        }
        if (strings.length > 1) {
            return null;
        }
        if (!command.getLabel().equalsIgnoreCase("plots")) {
            return null;
        }
        final Set<String> tabOptions = new HashSet<>();
        ArrayList<Command<PlotPlayer>> commands = MainCommand.getInstance().getCommands();
        String best = new StringComparison(strings[0], commands).getBestMatch();
        tabOptions.add(best);
        final String arg = strings[0].toLowerCase();
        for (final Command<PlotPlayer> cmd : MainCommand.getInstance().getCommands()) {
            String label = cmd.getCommand();
            if (!label.equalsIgnoreCase(best)) {
                if (label.startsWith(arg)) {
                    if (Permissions.hasPermission(player, cmd.getPermission())) {
                        tabOptions.add(cmd.getCommand());
                    } else if (cmd.getAliases().size() > 0) {
                        for (String alias : cmd.getAliases()) {
                            if (alias.startsWith(arg)) {
                                tabOptions.add(label);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (tabOptions.size() > 0) {
            return new ArrayList<>(tabOptions);
        }
        return null;
    }
}
