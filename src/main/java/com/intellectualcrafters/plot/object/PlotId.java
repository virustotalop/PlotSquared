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
package com.intellectualcrafters.plot.object;

public class PlotId {
    /**
     * x value
     */
    public Integer x;
    /**
     * y value
     */
    public Integer y;

    /**
     * PlotId class (PlotId x,y values do not correspond to Block locations)
     *
     * @param x The plot x coordinate
     * @param y The plot y coordinate
     */
    public PlotId(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get a Plot Id based on a string
     *
     * @param string to create id from
     *
     * @return null if the string is invalid
     */
    public static PlotId fromString(final String string) {
        int x, y;
        final String[] parts = string.split(";");
        if (parts.length < 2) {
            return null;
        }
        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
        } catch (final Exception e) {
            return null;
        }
        return new PlotId(x, y);
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
        final PlotId other = (PlotId) obj;
        return ((this.x.equals(other.x)) && (this.y.equals(other.y)));
    }

    @Override
    public String toString() {
        return this.x + ";" + this.y;
    }
    
    public static PlotId unpair(int hash) {
        if (hash >= 0) {
            if (hash % 2 == 0) {
                // + +
                hash /= 2;
                int i = (int) (Math.abs(-1 + Math.sqrt(1 + 8 * hash)) / 2);
                int idx = hash - ((i * (1 + i)) / 2);
                int idy = ((i * (3 + i)) / 2) - hash;
                return new PlotId(idx, idy);
            }
            else {
                // + -
                hash -= 1;
                hash /= 2;
                int i = (int) (Math.abs(-1 + Math.sqrt(1 + 8 * hash)) / 2);
                int idx = hash - ((i * (1 + i)) / 2);
                int idy = ((i * (3 + i)) / 2) - hash;
                return new PlotId(idx, -idy);
            }
        }
        else {
            if (hash % 2 == 0) {
                // - +
                hash /= -2;
                int i = (int) (Math.abs(-1 + Math.sqrt(1 + 8 * hash)) / 2);
                int idx = hash - ((i * (1 + i)) / 2);
                int idy = ((i * (3 + i)) / 2) - hash;
                return new PlotId(-idx, idy);
            }
            else {
                // - -
                hash += 1;
                hash /= -2;
                int i = (int) (Math.abs(-1 + Math.sqrt(1 + 8 * hash)) / 2);
                int idx = hash - ((i * (1 + i)) / 2);
                int idy = ((i * (3 + i)) / 2) - hash;
                return new PlotId(-idx, -idy);
            }
        }
    }

    private int hash;
    
    public void recalculateHash() {
        this.hash = 0;
        hashCode();
    }
    
    @Override
    public int hashCode() {
        if (hash == 0) {
            if (x >= 0) {
                if (y >= 0) {
                    hash = (x * x) + (3 * x) + (2 * x * y) + y + (y * y);
                } else {
                    final int y1 = -y;
                    hash = (x * x) + (3 * x) + (2 * x * y1) + y1 + (y1 * y1) + 1;
                }
            } else {
                final int x1 = -x;
                if (y >= 0) {
                    hash = -((x1 * x1) + (3 * x1) + (2 * x1 * y) + y + (y * y));
                } else {
                    final int y1 = -y;
                    hash = -((x1 * x1) + (3 * x1) + (2 * x1 * y1) + y1 + (y1 * y1) + 1);
                }
            }
        }
        return hash;
    }
}
