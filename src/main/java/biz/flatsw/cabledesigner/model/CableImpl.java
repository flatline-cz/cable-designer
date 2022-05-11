/*******************************************************************************
 * Copyright (c) 2022. originally created by flatline.cz
 * This file is a part of CABLE-DESIGNER project.
 *
 *  This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************************/

package biz.flatsw.cabledesigner.model;

public class CableImpl implements Cable {
    private final int length;
    private final PathNode startNode;
    private final PathNode endNode;

    public CableImpl(int length, PathNode startNode, PathNode endNode) {
        this.length = length;
        this.startNode = startNode;
        this.endNode = endNode;
        this.startNode.addCableConnection(this);
        this.endNode.addCableConnection(this);
//        System.err.println(this);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public PathNode getOppositeNode(PathNode node) {
        if (node == startNode)
            return endNode;
        if (node == endNode)
            return startNode;
        return null;
    }

    @Override
    public PathNode getStartNode() {
        return startNode;
    }

    @Override
    public PathNode getEndNode() {
        return endNode;
    }

    @Override
    public String toString() {
        return "Cable from "+startNode+" to "+endNode;
    }
}
