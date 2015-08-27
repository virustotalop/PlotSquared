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


/**
 * Wrapper class for blocks, using pure data rather than the object.
 * 
 * Useful for NMS
 *
 * @author Empire92
 * @author Citymonstret
 */
public class BlockWrapper {
    /**
     * X Coordinate
     */
    public final int x;
    /**
     * Y Coordinate
     */
    public final int y;
    /**
     * Z Coordinate
     */
    public final int z;
    /**
     * Block ID
     */
    public final int id;
    /**
     * Block Data Value
     */
    public final byte data;

    /**
     * Constructor
     *
     * @param x    X Loc Value
     * @param y    Y Loc Value
     * @param z    Z Loc Value
     * @param id   Material ID
     * @param data Data Value
     */
    public BlockWrapper(final int x, final int y, final int z, final short id, final byte data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.data = data;
    }
}
