package com.intellectualcrafters.plot.object;

public class BlockLoc {
    public int x;
    public int y;
    public int z;

    public float yaw, pitch;

    public BlockLoc(final int x, final int y, final int z, final float yaw, final float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.yaw = yaw;
        this.pitch = pitch;
    }

    public BlockLoc(final int x, final int y, final int z) {
        this(x, y, z, 0f, 0f);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.x;
        result = (prime * result) + this.y;
        result = (prime * result) + this.z;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BlockLoc other = (BlockLoc) obj;
        return ((this.x == other.x) && (this.y == other.y) && (this.z == other.z));
    }

    @Override
    public String toString() {
        return
                x + "," + y + "," + z + "," + yaw + "," + pitch;
    }

    public static BlockLoc fromString(final String string) {
        String[] parts = string.split(",");

        float yaw, pitch;
        if (parts.length == 3) {
            yaw = 0f;
            pitch = 0f;
        } if (parts.length == 5) {
            yaw = Float.parseFloat(parts[3]);
            pitch = Float.parseFloat(parts[4]);
        } else {
            return new BlockLoc(0, 0, 0);
        }
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);

        return new BlockLoc(x, y, z, yaw, pitch);
    }
}
