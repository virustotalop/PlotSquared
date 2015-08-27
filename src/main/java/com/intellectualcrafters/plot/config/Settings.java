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
package com.intellectualcrafters.plot.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Updater and DB settings
 *
 * @author Citymonstret
 * @author Empire92
 */
public class Settings {
    public static boolean USE_SQLUUIDHANDLER = false;

    public static boolean ENABLE_CLUSTERS = false;
    public static boolean FAST_CLEAR = false;
    /**
     * Default UUID_FECTHING: false
     */
    public static boolean PERMISSION_CACHING = false;
    public static boolean CACHE_RATINGS = true;
    public static boolean UUID_FROM_DISK = false;
    
    /**
     * Web
     */
    public static String WEB_URL = "http://empcraft.com/plots/";
    public static String WEB_IP = "your.ip.here";
    /**
     * Ratings
     */
    public static List<String> RATING_CATEGORIES = null;
    public static boolean REQUIRE_DONE = false;
    public static boolean DONE_COUNTS_TOWARDS_LIMIT = false;
    public static boolean DONE_RESTRICTS_BUILDING = false;
    
    /**
     * PlotMe settings
     */
    public static boolean CONVERT_PLOTME = true;
    public static boolean CACHE_PLOTME = false;
    public static boolean USE_PLOTME_ALIAS = false;
    /**
     * Comment system
     */
    public static boolean COMMENT_NOTIFICATIONS = false;
    /**
     * Chunk processor
     */
    public static boolean CHUNK_PROCESSOR = false;
    public static boolean EXPERIMENTAL_FAST_ASYNC_WORLDEDIT = false;
    public static boolean CHUNK_PROCESSOR_TRIM_ON_SAVE = false;
    public static boolean CHUNK_PROCESSOR_GC = false;
    public static int CHUNK_PROCESSOR_MAX_BLOCKSTATES = 4096;
    public static int CHUNK_PROCESSOR_MAX_ENTITIES = 512;
    public static boolean CHUNK_PROCESSOR_DISABLE_PHYSICS = false;
    /**
     * TNT listener
     */
    public static boolean TNT_LISTENER = false;
    /**
     * Redstone disabler
     */
    public static boolean REDSTONE_DISABLER = false;
    public static boolean REDSTONE_DISABLER_UNOCCUPIED = false;
    /**
     * Check for falling blocks when pistons extend?
     */
    public static boolean PISTON_FALLING_BLOCK_CHECK = true;
    /**
     * Max auto claiming size
     */
    public static int MAX_AUTO_SIZE = 4;
    /**
     * Default worldedit-require-selection-in-mask: false
     */
    public static boolean REQUIRE_SELECTION = true;
    public static boolean WE_ALLOW_HELPER = false;
    public static long WE_MAX_VOLUME = 500000;
    public static long WE_MAX_ITERATIONS = 1000;
    public static List<String> WE_BLACKLIST = new ArrayList<>();
    /**
     * Default kill road mobs: true
     */
    public final static boolean KILL_ROAD_MOBS_DEFAULT = false;
    public final static boolean KILL_ROAD_VEHICLES_DEFAULT = false;
    /**
     * Default mob pathfinding: true
     */
    public final static boolean MOB_PATHFINDING_DEFAULT = true;
    /**
     * Teleport to path on login
     */
    public static boolean TELEPORT_ON_LOGIN = false;
    /**
     * Display titles
     */
    public static boolean TITLES = true;
    /**
     * Schematic Save Path
     */
    public static String SCHEMATIC_SAVE_PATH = "/var/www/schematics";
    /**
     * BO3 Save Path
     */
    public static String BO3_SAVE_PATH = "BO3";
    /**
     * Max allowed plots
     */
    public static int MAX_PLOTS = 127;
    /**
     * WorldGuard region on claimed plots
     */
    public static boolean WORLDGUARD = false;
    /**
     * metrics
     */
    public static boolean METRICS = true;
    /**
     * plot specific resource pack
     */
    public static String PLOT_SPECIFIC_RESOURCE_PACK = "";
    /**
     * Kill road mobs?
     */
    public static boolean KILL_ROAD_MOBS;
    public static boolean KILL_ROAD_VEHICLES;
    /**
     * mob pathfinding?
     */
    public static boolean MOB_PATHFINDING;
    /**
     * Delete plots on ban?
     */
    public static boolean DELETE_PLOTS_ON_BAN = false;
    /**
     * Verbose?
     */
    public static boolean DEBUG = true;
    /**
     * Have colored console messages?
     */
    public static boolean CONSOLE_COLOR = true;
    /**
     * Fancy chat e.g. for /plot list
     */
    public static boolean FANCY_CHAT = true;
    /**
     * The delay (in seconds) before teleportation commences
     */
    public static int TELEPORT_DELAY;
    /**
     * Auto clear enabled
     */
    public static boolean AUTO_CLEAR = false;
    /**
     * Days until a plot gets cleared
     */
    public static int AUTO_CLEAR_DAYS = 360;
    public static int CLEAR_THRESHOLD = 1;
    public static int CLEAR_INTERVAL = 120;
    /**
     * API Location
     */
    public static String API_URL = "http://www.intellectualsites.com/minecraft.php";
    /**
     * Use the custom API
     */
    public static boolean CUSTOM_API = true;
    /**
     * Use offline mode storage
     */
    public static boolean TWIN_MODE_UUID = false;
    public static boolean OFFLINE_MODE = false;
    public static boolean UUID_LOWERCASE = false;
    /**
     * Command confirmation
     */
    public static boolean CONFIRM_CLEAR = true;
    public static boolean CONFIRM_DELETE = true;
    public static boolean CONFIRM_UNLINK = true;
    /**
     * Use global plot limit?
     */
    public static boolean GLOBAL_LIMIT = false;

    /**
     * Database settings
     *
     * @author Citymonstret
     */
    public static class DB {
        /**
         * MongoDB enabled?
         */
        public static boolean USE_MONGO = false; /*
         * TODO: Implement Mongo
         * @Brandon
         */
        /**
         * SQLite enabled?
         */
        public static boolean USE_SQLITE = false;
        /**
         * MySQL Enabled?
         */
        public static boolean USE_MYSQL = true; /* NOTE: Fixed connector */
        /**
         * SQLite Database name
         */
        public static String SQLITE_DB = "storage";
        /**
         * MySQL Host name
         */
        public static String HOST_NAME = "localhost";
        /**
         * MySQL Port
         */
        public static String PORT = "3306";
        /**
         * MySQL DB
         */
        public static String DATABASE = "plot_db";
        /**
         * MySQL User
         */
        public static String USER = "root";
        /**
         * MySQL Password
         */
        public static String PASSWORD = "password";
        /**
         * MySQL Prefix
         */
        public static String PREFIX = "";
    }
}
