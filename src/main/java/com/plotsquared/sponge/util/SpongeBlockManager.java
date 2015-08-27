package com.plotsquared.sponge.util;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.StringComparison;
import com.plotsquared.sponge.SpongeMain;

public class SpongeBlockManager extends BlockManager {
    
    @Override
    public boolean isBlockSolid(PlotBlock block) {
        BlockState state = SpongeMain.THIS.getBlockState(block);
        BlockType type = state.getType();
        return type.isSolidCube() && !type.isAffectedByGravity();
    }
    
    @Override
    public StringComparison<PlotBlock>.ComparisonResult getClosestBlock(String name) {
        try {
            double match;
            short id;
            byte data;
            String[] split = name.split(":");
            if (split.length == 2) {
                data = Byte.parseByte(split[1]);
                name = split[0];
            }
            else {
                data = 0;
            }
            if (MathMan.isInteger(split[0])) {
                id = Short.parseShort(split[0]);
                match = 0;
            }
            else {
                StringComparison<BlockState>.ComparisonResult comparison = new StringComparison<BlockState>(name, SpongeMain.THIS.getAllStates()) {
                    public String getString(BlockState o) {
                        return o.getType().getId();
                    };
                }.getBestMatchAdvanced();
                match = comparison.match;
                id = SpongeMain.THIS.getPlotBlock(comparison.best).id;
            }
            PlotBlock block = new PlotBlock(id, data);
            StringComparison<PlotBlock> outer = new StringComparison<PlotBlock>();
            return outer.new ComparisonResult(match, block);
            
        }
        catch (Exception e) {}
        return null;
    }
    
    @Override
    public String getClosestMatchingName(PlotBlock block) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String[] getBiomeList() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean addItems(String world, PlotItem items) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public int getBiomeFromString(String biome) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public PlotBlock getPlotBlockFromString(String block) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int getHeighestBlock(String worldname, int x, int z) {
        World world = SpongeUtil.getWorld(worldname);
        if (world == null) {
            return 64;
        }
        for (int y = 255; y > 0; y--) {
            BlockState block = world.getBlock(x, y, z);
            if (block != null && block.getType() != BlockTypes.AIR) {
                return y+1;
            }
        }
        return 64;
    }
    
    @Override
    public String getBiome(String world, int x, int z) {
        return SpongeUtil.getWorld(world).getBiome(x, z).getName().toUpperCase();
    }
    
    @Override
    public PlotBlock getBlock(Location loc) {
        BlockState state = SpongeUtil.getWorld(loc.getWorld()).getBlock(loc.getX(), loc.getY(), loc.getZ());
        PlotBlock block = SpongeMain.THIS.getPlotBlock(state);
        if (block == null) {
            block = SpongeMain.THIS.registerBlock(state);
        }
        return block;
    }
    
    @Override
    public Location getSpawn(String world) {
        World worldObj = SpongeUtil.getWorld(world);
        org.spongepowered.api.world.Location loc = worldObj.getSpawnLocation();
        Location result = SpongeUtil.getLocation(world, SpongeUtil.getWorld(world).getSpawnLocation());
        result.setY(getHeighestBlock(world, result.getX(), result.getZ()));
        return result;
    }
    
    @Override
    public String[] getSign(Location loc) {
        World world = SpongeUtil.getWorld(loc.getWorld());
        Optional<TileEntity> block = world.getTileEntity(loc.getX(), loc.getY(), loc.getZ());
        if (!block.isPresent()) {
            return null;
        }
        TileEntity tile = block.get();
        if (!(tile instanceof Sign)) {
            return null;
        }
        Sign sign = (Sign) tile;
        Optional<SignData> optional = sign.getOrCreate(SignData.class);
        if (!optional.isPresent()) {
            return null;
        }
        String[] result = new String[4];
        ListValue<Text> lines = optional.get().lines();
        for (int i = 0; i < 4; i++) {
            result[i] = lines.get(i).toString();
        }
        return result;
    }
    
    @Override
    public boolean isWorld(String world) {
        return SpongeUtil.getWorld(world) != null;
    }
    
    @Override
    public void functionSetBlocks(String worldname, int[] xv, int[] yv, int[] zv, int[] id, byte[] data) {
        for (int i = 0; i < xv.length; i++) {
            functionSetBlock(worldname, xv[i], yv[i], zv[i], id[i], data[i]);
        }
    }
    
    @Override
    public void functionSetSign(String worldname, int x, int y, int z, String[] lines) {
        World world = SpongeUtil.getWorld(worldname);
        world.setBlock(x, y, z, BlockTypes.WALL_SIGN.getDefaultState());
        Optional<TileEntity> block = world.getTileEntity(x, y, z);
        if (!block.isPresent()) {
            return;
        }
        TileEntity tile = block.get();
        if (!(tile instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) tile;
        List<Text> text = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            text.add(SpongeMain.THIS.getText(lines[i]));
        }
        try {
            Optional<SignData> optional = sign.getOrCreate(SignData.class);
            if(optional.isPresent()) {
                SignData offering = optional.get();
                offering.lines().set(0, SpongeMain.THIS.getText(lines[0]));
                offering.lines().set(1, SpongeMain.THIS.getText(lines[1]));
                offering.lines().set(2, SpongeMain.THIS.getText(lines[2]));
                offering.lines().set(3, SpongeMain.THIS.getText(lines[3]));
                sign.offer(offering);
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void functionSetBlock(String worldname, int x, int y, int z, int id, byte data) {
        BlockState state;
        if (data == 0) {
            state = SpongeMain.THIS.getBlockState(id);
        }
        else {
            state = SpongeMain.THIS.getBlockState(new PlotBlock((short) id, data));
        }
        if (state == null) {
            return;
        }
        World world = SpongeUtil.getWorld(worldname);
        BlockState block = world.getBlock(x, y, z);
        if (block != state) {
            world.setBlock(x, y, z, state);
        }
        
    }
    
    @Override
    public void functionSetBiomes(String worldname, int[] xv, int[] zv, String biomeName) {
        BiomeType biome;
        try {
            biome = (BiomeType) BiomeTypes.class.getField(biomeName.toUpperCase()).get(null);
        } catch (Exception e) {
            e.printStackTrace();
            biome = BiomeTypes.FOREST;
        }
        for (int i = 0; i < xv.length; i++) {
            SpongeUtil.getWorld(worldname).setBiome(xv[i], zv[i], biome);
        }
    }
    
}
