package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandCaller;

public class Permissions {
    public static boolean hasPermission(final PlotPlayer player, final C c) {
        return hasPermission(player, c.s());
    }

    public static boolean hasPermission(final PlotPlayer player, final String perm) {
        return hasPermission((CommandCaller) player, perm);
    }

    public static boolean hasPermission(final CommandCaller player, final String perm) {
        if ((player == null) || player.hasPermission(C.PERMISSION_ADMIN.s()) || player.hasPermission(C.PERMISSION_STAR.s())) {
            return true;
        }
        if (player.hasPermission(perm)) {
            return true;
        }
        return false;
    }
    
    public static boolean hasPermission(final PlotPlayer player, final String perm, boolean notify) {
        if (!hasPermission(player, perm)) {
            if (notify) {
                MainUtil.sendMessage(player, C.NO_PERMISSION_EVENT, perm);
            }
            return false;
        }
        return true;
    }

    public static int hasPermissionRange(final PlotPlayer player, final String stub, final int range) {
        if ((player == null) || player.hasPermission(C.PERMISSION_ADMIN.s()) || player.hasPermission(C.PERMISSION_STAR.s())) {
            return Integer.MAX_VALUE;
        }
        if (player.hasPermission(stub + ".*")) {
            return Integer.MAX_VALUE;
        }
        for (int i = range; i > 0; i--) {
            if (player.hasPermission(stub + "." + i)) {
                return i;
            }
        }
        return 0;
    }
}
