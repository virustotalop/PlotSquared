package com.plotsquared.bukkit.object;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.PlotGamemode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.util.BukkitUtil;

public class BukkitPlayer extends PlotPlayer {
    
    public final Player player;
    private UUID uuid;
    private String name;
    private long last = 0;
    public HashSet<String> hasPerm = new HashSet<>();
    public HashSet<String> noPerm = new HashSet<>();
    public boolean offline;

    /**
     * Please do not use this method. Instead use BukkitUtil.getPlayer(Player), as it caches player objects.
     * @param player
     */
    public BukkitPlayer(final Player player) {
        this.player = player;
    }
    
    public BukkitPlayer(final Player player, boolean offline) {
        this.player = player;
        this.offline = offline;
    }
    
    public long getPreviousLogin() {
        if (last == 0) {
            last = player.getLastPlayed();
        }
        return last;
    }

    @Override
    public Location getLocation() {
        Location loc = super.getLocation();
        return loc == null ? BukkitUtil.getLocation(this.player) : loc; 
    }
    
    @Override
    public UUID getUUID() {
        if (this.uuid == null) {
            this.uuid = UUIDHandler.getUUID(this);
        }
        return this.uuid;
    }
    
    @Override
    public boolean hasPermission(final String node) {
        if (Settings.PERMISSION_CACHING) {
            if (this.noPerm.contains(node)) {
                return false;
            }
            if (this.hasPerm.contains(node)) {
                return true;
            }
        }
        if (offline && EconHandler.manager != null) {
            return EconHandler.manager.hasPermission(getName(), node);
        }
        boolean value = this.player.hasPermission(node);
        if (!value) {
            final String[] nodes = node.split("\\.");
            if (!MathMan.isInteger(nodes[nodes.length - 1])) {
                final StringBuilder n = new StringBuilder();
                for (int i = 0; i < (nodes.length - 1); i++) {
                    n.append(nodes[i] + ("."));
                    if (!node.equals(n + C.PERMISSION_STAR.s())) {
                        value = player.hasPermission(n + C.PERMISSION_STAR.s());
                        if (value) {
                            break;
                        }
                    }
                }
                value = this.player.hasPermission(node);
            }
        }
        if (Settings.PERMISSION_CACHING) {
            if (value) {
                this.hasPerm.add(node);
            }
            else {
                this.noPerm.add(node);
            }
        }
        return value;
    }
    
    @Override
    public void sendMessage(final String message) {
        this.player.sendMessage(message);
    }
    
    @Override
    public void sendMessage(C c, String... args) {
        MainUtil.sendMessage(this, c, args);
    }
    
    @Override
    public void teleport(final Location loc) {
        this.player.teleport(new org.bukkit.Location(BukkitUtil.getWorld(loc.getWorld()), loc.getX() + 0.5, loc.getY(), loc.getZ() + 0.5, loc.getYaw(), loc.getPitch()));
    }
    
    @Override
    public String getName() {
        if (this.name == null) {
            this.name = this.player.getName();
        }
        return this.name;
    }
    
    @Override
    public boolean isOnline() {
        return !offline && this.player.isOnline();
    }
    
    @Override
    public void setCompassTarget(final Location loc) {
        this.player.setCompassTarget(new org.bukkit.Location(BukkitUtil.getWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ()));

    }

    @Override
    public Location getLocationFull() {
        return BukkitUtil.getLocationFull(this.player);
    }

    @Override
    public void setAttribute(String key) {
        key = "plotsquared_user_attributes." + key;
        if (EconHandler.manager == null || player.hasPermission("plotsquared_user_attributes.*")) {
            setMeta(key, true);
            return;
        }
        EconHandler.manager.setPermission(getName(), key, true);
    }

    @Override
    public boolean getAttribute(String key) {
        key = "plotsquared_user_attributes." + key;
        if (EconHandler.manager == null || player.hasPermission("plotsquared_user_attributes.*")) {
            Object v = getMeta(key);
            return v == null ? false : (Boolean) v;
        }
        Permission perm = Bukkit.getServer().getPluginManager().getPermission(key);
        if (perm == null) {
            perm = new Permission(key, PermissionDefault.FALSE);
            Bukkit.getServer().getPluginManager().addPermission(perm);
            Bukkit.getServer().getPluginManager().recalculatePermissionDefaults(perm);
        }
        return player.hasPermission(key);
    }

    @Override
    public void removeAttribute(String key) {
        key = "plotsquared_user_attributes." + key;
        if (EconHandler.manager == null || player.hasPermission("plotsquared_user_attributes.*")) {
            deleteMeta(key);
            return;
        }
        EconHandler.manager.setPermission(getName(), key, false);
    }

    @Override
    public void loadData() {
        if (!player.isOnline()) {
            player.loadData();
        }
    }

    @Override
    public void saveData() {
        player.saveData();
    }

    @Override
    public void setWeather(PlotWeather weather) {
        switch (weather) {
            case CLEAR:
                player.setPlayerWeather(WeatherType.CLEAR);
                return;
            case RAIN: {
                player.setPlayerWeather(WeatherType.DOWNFALL);
                return;
            }
            case RESET:
                player.resetPlayerWeather();
                return;
        }
    }

    @Override
    public PlotGamemode getGamemode() {
        switch (player.getGameMode()) {
            case ADVENTURE:
                return PlotGamemode.ADVENTURE;
            case CREATIVE:
                return PlotGamemode.CREATIVE;
            case SPECTATOR:
                return PlotGamemode.SPECTATOR;
            case SURVIVAL:
                return PlotGamemode.SURVIVAL;
        }
        return null;
    }

    @Override
    public void setGamemode(PlotGamemode gamemode) {
        switch (gamemode) {
            case ADVENTURE:
                player.setGameMode(GameMode.ADVENTURE);
                return;
            case CREATIVE:
                player.setGameMode(GameMode.CREATIVE);
                return;
            case SPECTATOR:
                player.setGameMode(GameMode.SPECTATOR);
                return;
            case SURVIVAL:
                player.setGameMode(GameMode.SURVIVAL);
                return;
        }
    }

    @Override
    public void setTime(long time) {
        player.setPlayerTime(time, false);
    }

    @Override
    public void setFlight(boolean fly) {
        player.setAllowFlight(fly);
    }

    @Override
    public void playMusic(Location loc, int id) {
        player.playEffect(BukkitUtil.getLocation(loc), Effect.RECORD_PLAY, id);
    }

    @Override
    public void kick(String message) {
        player.kickPlayer(message);
    }
}
