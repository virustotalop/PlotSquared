package com.intellectualcrafters.plot.util;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotBlock;

public class SetBlockQueue {
    
    private volatile static HashMap<ChunkWrapper, PlotBlock[][]> blocks;
    private volatile static int allocate = 25;
    private volatile static boolean running = false;
    private volatile static boolean locked = false;
    private volatile static ArrayDeque<Runnable> runnables;
    private volatile static boolean slow = false;
    private static long last;
    private static int lastInt = 0;
    private static PlotBlock lastBlock = new PlotBlock((short) 0, (byte) 0);
    
    public synchronized static void allocate(int t) {
        allocate = t;
    }
    
    public static int getAllocate() {
        return allocate;
    }
    
    public static void setSlow(boolean value) {
        slow = value;
    }
    
    public synchronized static boolean addNotify(Runnable whenDone) {
        if (runnables == null) {
            if (blocks == null) {
                if (whenDone != null) {
                    whenDone.run();
                }
                slow = false;
                locked = false;
                return true;
            }
            runnables = new ArrayDeque<>();
        }
        if (whenDone != null) {
            runnables.add(whenDone);
        }
        return false;
    }

    public synchronized static void init() {
        if (blocks == null) {
            if (MainUtil.x_loc == null) {
                MainUtil.initCache();
            }
            blocks = new HashMap<>();
            runnables = new ArrayDeque<>();
        }
        if (!running) {
            TaskManager.index.incrementAndGet();
            final int current = TaskManager.index.intValue();
            int task = TaskManager.runTaskRepeat(new Runnable() {
                @Override
                public void run() {
                    if (locked) {
                        return;
                    }
                    if (blocks == null || blocks.size() == 0) {
                        PS.get().TASK.cancelTask(TaskManager.tasks.get(current));
                        ArrayDeque<Runnable> tasks = runnables;
                        lastInt = -1;
                        lastBlock = null;
                        runnables = null;
                        blocks = null;
                        running = false;
                        slow = false;
                        if (tasks != null) {
                            for (Runnable runnable : tasks) {
                                runnable.run();
                            }
                        }
                        return;
                    }
                    long newLast = System.currentTimeMillis();
                    last = Math.max(newLast - 50, last);
                    while (blocks.size() > 0 && (System.currentTimeMillis() - last < 50 + allocate)) {
                        if (locked) {
                            return;
                        }
                        Iterator<Entry<ChunkWrapper, PlotBlock[][]>> iter = blocks.entrySet().iterator();
                        if (!iter.hasNext()) {
                            return;
                        }
                        Entry<ChunkWrapper, PlotBlock[][]> n = iter.next();
                        ChunkWrapper chunk = n.getKey();
                        PlotBlock[][] blocks = n.getValue();
                        int X = chunk.x << 4;
                        int Z = chunk.z << 4;
                        String world = chunk.world;
                        if (slow) {
                            boolean once = false;
                            for (int j = 0; j < blocks.length; j++) {
                                PlotBlock[] blocksj = blocks[j];
                                if (blocksj != null) {
                                    long start = System.currentTimeMillis();
                                    for (int k = 0; k < blocksj.length; k++) {
                                        if (once && (System.currentTimeMillis() - start > allocate)) {
                                            SetBlockQueue.blocks.put(n.getKey(), blocks);
                                            return;
                                        }
                                        PlotBlock block = blocksj[k];
                                        if (block != null) {
                                            int x = MainUtil.x_loc[j][k];
                                            int y = MainUtil.y_loc[j][k];
                                            int z = MainUtil.z_loc[j][k];
                                            BlockManager.manager.functionSetBlock(world, X + x, y, Z + z, block.id, block.data);
                                            blocks[j][k] = null;
                                            once = true;
                                        }
                                    }
                                }
                            }
                            SetBlockQueue.blocks.remove(n.getKey());
                            return;
                        }
                        SetBlockQueue.blocks.remove(n.getKey());
                        for (int j = 0; j < blocks.length; j++) {
                            PlotBlock[] blocksj = blocks[j];
                            if (blocksj != null) {
                                for (int k = 0; k < blocksj.length; k++) {
                                    PlotBlock block = blocksj[k];
                                    if (block != null) {
                                        int x = MainUtil.x_loc[j][k];
                                        int y = MainUtil.y_loc[j][k];
                                        int z = MainUtil.z_loc[j][k];
                                        BlockManager.manager.functionSetBlock(world, X + x, y, Z + z, block.id, block.data);
                                    }
                                }
                            }
                        }
                    }
                }
            }, 1);
            TaskManager.tasks.put(current, task);
            running = true;
        }
    }
    
    public static void setChunk(final String world, ChunkLoc loc, PlotBlock[][] result) {
        locked = true;
        if (!running) {
            init();
        }
        ChunkWrapper wrap = new ChunkWrapper(world, loc.x, loc.z);
        blocks.put(wrap, result);
        locked = false;
    }

    public static void setBlock(final String world, int x, final int y, int z, final PlotBlock block) {
        locked = true;
        if (!running) {
            init();
        }
        int X = x >> 4;
        int Z = z >> 4;
        x -= X << 4;
        z -= Z << 4;

        ChunkWrapper wrap = new ChunkWrapper(world, X, Z);
        PlotBlock[][] result;
        result = blocks.get(wrap);
        if (!blocks.containsKey(wrap)) {
            result = new PlotBlock[16][];
            blocks.put(wrap, result);
        }

        if (result[y >> 4] == null) {
            result[y >> 4] = new PlotBlock[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = block;
        locked = false;
    }
    
    public static void setData(final String world, int x, final int y, int z, final byte data) {
        locked = true;
        if (!running) {
            init();
        }
        int X = x >> 4;
        int Z = z >> 4;
        x -= X << 4;
        z -= Z << 4;
        ChunkWrapper wrap = new ChunkWrapper(world, X, Z);
        PlotBlock[][] result;
        result = blocks.get(wrap);
        if (!blocks.containsKey(wrap)) {
            result = new PlotBlock[16][];
            blocks.put(wrap, result);
        }
        if (result[y >> 4] == null) {
            result[y >> 4] = new PlotBlock[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = new PlotBlock((short) -1, data);
        locked = false;
    }
    
    public static void setBlock(final String world, int x, final int y, int z, final int id) {
        locked = true;
        if (!running) {
            init();
        }
        int X = x >> 4;
        int Z = z >> 4;
        x -= X << 4;
        z -= Z << 4;
        ChunkWrapper wrap = new ChunkWrapper(world, X, Z);
        PlotBlock[][] result;
        result = blocks.get(wrap);
        if (!blocks.containsKey(wrap)) {
            result = new PlotBlock[16][];
            blocks.put(wrap, result);
        }
        if (result[y >> 4] == null) {
            result[y >> 4] = new PlotBlock[4096];
        }
        if (id == lastInt) {
            result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = lastBlock;
        }
        else {
            lastInt = id;
            lastBlock = new PlotBlock((short) id, (byte) 0);
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = lastBlock;
        locked = false;
    }
    
    public static class ChunkWrapper {
        public final int x; 
        public final int z;
        public final String world;
        
        public ChunkWrapper(String world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }
        
        @Override
        public int hashCode() {
            int result;
            if (this.x >= 0) {
                if (this.z >= 0) {
                    result = (this.x * this.x) + (3 * this.x) + (2 * this.x * this.z) + this.z + (this.z * this.z);
                } else {
                    final int y1 = -this.z;
                    result = (this.x * this.x) + (3 * this.x) + (2 * this.x * y1) + y1 + (y1 * y1) + 1;
                }
            } else {
                final int x1 = -this.x;
                if (this.z >= 0) {
                    result = -((x1 * x1) + (3 * x1) + (2 * x1 * this.z) + this.z + (this.z * this.z));
                } else {
                    final int y1 = -this.z;
                    result = -((x1 * x1) + (3 * x1) + (2 * x1 * y1) + y1 + (y1 * y1) + 1);
                }
            }
            result = result * 31 + world.hashCode();
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ChunkWrapper other = (ChunkWrapper) obj;
            return ((this.x == other.x) && (this.z == other.z) && (this.world.equals(other.world)));
        }
    }
}
