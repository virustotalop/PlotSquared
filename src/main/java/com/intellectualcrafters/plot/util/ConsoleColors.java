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
package com.intellectualcrafters.plot.util;

public class ConsoleColors {
    public static String fromString(String input) {
        input = input.replaceAll("&0", fromChatColor("&0")).replaceAll("&1", fromChatColor("&1")).replaceAll("&2", fromChatColor("&2")).replaceAll("&3", fromChatColor("&3")).replaceAll("&4", fromChatColor("&4")).replaceAll("&5", fromChatColor("&5")).replaceAll("&6", fromChatColor("&6")).replaceAll("&7", fromChatColor("&7")).replaceAll("&8", fromChatColor("&8")).replaceAll("&9", fromChatColor("&9")).replaceAll("&a", fromChatColor("&a")).replaceAll("&b", fromChatColor("&b")).replaceAll("&c", fromChatColor("&c")).replaceAll("&d", fromChatColor("&d")).replaceAll("&e", fromChatColor("&e")).replaceAll("&f", fromChatColor("&f")).replaceAll("&k", fromChatColor("&k")).replaceAll("&l", fromChatColor("&l")).replaceAll("&m", fromChatColor("&m"))
                .replaceAll("&n", fromChatColor("&n")).replaceAll("&o", fromChatColor("&o")).replaceAll("&r", fromChatColor("&r"));
        return input + "\u001B[0m";
    }

    /*
     * public static final String ANSI_RESET = "\u001B[0m"; public static final
     * String ANSI_BLACK = "\u001B[30m"; public static final String ANSI_RED =
     * "\u001B[31m"; public static final String ANSI_GREEN = "\u001B[32m";
     * public static final String ANSI_YELLOW = "\u001B[33m"; public static
     * final String ANSI_BLUE = "\u001B[34m"; public static final String
     * ANSI_PURPLE = "\u001B[35m"; public static final String ANSI_CYAN =
     * "\u001B[36m"; public static final String ANSI_WHITE = "\u001B[37m";
     * public static final String ANSI_BOLD = "\033[1m"; public static final
     * String ANSI_UNDERLINE = "\033[0m"; public static final String ANSI_ITALIC
     * = "\033[3m]";
     */
    public static String fromChatColor(final String color) {
        return chatColor(color).getLin();
    }

    public static ConsoleColor chatColor(final String color) {
        switch (color) {
            case "&r":
                return ConsoleColor.RESET;
            case "&7":
            case "&8":
                return ConsoleColor.WHITE;
            case "&0":
                return ConsoleColor.BLACK;
            case "&4":
            case "&c":
                return ConsoleColor.RED;
            case "&6":
            case "&e":
                return ConsoleColor.YELLOW;
            case "&a":
            case "&2":
                return ConsoleColor.GREEN;
            case "&b":
            case "&3":
                return ConsoleColor.CYAN;
            case "&d":
            case "&5":
                return ConsoleColor.PURPLE;
            case "&9":
            case "&1":
                return ConsoleColor.BLUE;
            case "&n":
                return ConsoleColor.UNDERLINE;
            case "&o":
                return ConsoleColor.ITALIC;
            case "&l":
                return ConsoleColor.BOLD;
            default:
                return ConsoleColor.RESET;
        }
    }

    static enum ConsoleColor {
        RESET("\u001B[0m"),
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m"),
        BOLD("\033[1m"),
        UNDERLINE("\033[0m"),
        ITALIC("\033[3m");
        private final String win;
        private final String lin;

        ConsoleColor(final String lin) {
            this.lin = lin;
            this.win = lin;
        }

        @SuppressWarnings("unused")
        public String getWin() {
            return this.win;
        }

        public String getLin() {
            return this.lin;
        }
    }
}
